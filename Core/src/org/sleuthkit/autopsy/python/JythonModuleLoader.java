/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.python;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.python.util.PythonInterpreter;
import org.sleuthkit.autopsy.core.RuntimeProperties;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.report.GeneralReportModule;

/**
 * Finds and loads Autopsy modules written using the Jython variant of the
 * Python scripting language.
 */
public final class JythonModuleLoader {
    
    /**
     * Contains a record of a Jython module including the last modified time and path for caching purposes.
     * @param <T>   The type of Jython module.
     */
    private static class ModuleRecord<T> {
        private final String filePath;
        private final long lastModified;
        private final T factory;

        /**
         * Main constructor for a module record.
         * @param filePath      The path for the Jython file.
         * @param lastModified  The date in milliseconds from epoch.
         * @param factory       The in-memory Jython module.
         */
        public ModuleRecord(String filePath, long lastModified, T factory) {
            this.filePath = filePath;
            this.lastModified = lastModified;
            this.factory = factory;
        }

        /**
         * Retrieves the path for the Jython file.
         * @return  The path for the Jython file.
         */
        public String getFilePath() {
            return filePath;
        }

        /**
         * Retrieves the last modified time as milliseconds from epoch.
         * @return  The last modified time as milliseconds from epoch.
         */
        public long getLastModified() {
            return lastModified;
        }

        /**
         * Retrieves the in-memory Jython module.
         * @return      The in-memory Jython module.
         */
        public T getFactory() {
            return factory;
        }
    }

    
    private static final Logger logger = Logger.getLogger(JythonModuleLoader.class.getName());


    /**
     * Checks the cache to see if file exists in cache and the file has not been modified since last cache. 
     * If file is not cached or should be refreshed, the converter is used to load the file.
     * 
     * @param <T>           The module type.
     * @param cache         The object cache.
     * @param scriptFile    The file to be checked in the cache.
     * @param loader        If file is not cached or file's last modified date is not the 
     *                      same as cached, then this will create a new module from the file.
     * @return              The generated module record.
     * @throws Exception    If the loader throws an exception.  
     */
    private static <T> ModuleRecord<T> getCachedOrNew(Map<String, ModuleRecord<T>> cache, File scriptFile, Callable<T> loader) throws Exception {
        String absPath = scriptFile.getAbsolutePath();
        long lastModified = scriptFile.lastModified();
        ModuleRecord<T> cachedItem = cache.get(absPath);
        if (cachedItem != null && scriptFile.lastModified() == cachedItem.getLastModified()) {
            return cachedItem;
        }
        else {
            return new ModuleRecord(absPath, lastModified, loader.call());
        }
    }

    
    private static Map<String, ModuleRecord<IngestModuleFactory>> cacheIngestModules = new HashMap<>();
    
    /**
     * Get ingest module factories implemented using Jython.
     *
     * @return A list of objects that implement the IngestModuleFactory
     *         interface.
     */
    public static List<IngestModuleFactory> getIngestModuleFactories() {
        synchronized (cacheIngestModules) {
            cacheIngestModules = getInterfaceImplementations(cacheIngestModules, new IngestModuleFactoryDefFilter(), IngestModuleFactory.class);
            return new ArrayList(cacheIngestModules.values());
        }
    }
    
    private static Map<String, ModuleRecord<GeneralReportModule>> cacheReportModules = new HashMap<>();

    /**
     * Get general report modules implemented using Jython.
     *
     * @return A list of objects that implement the GeneralReportModule
     *         interface.
     */
    public static List<GeneralReportModule> getGeneralReportModules() {
        synchronized (cacheReportModules) {
            cacheReportModules = getInterfaceImplementations(cacheReportModules, new GeneralReportModuleDefFilter(), GeneralReportModule.class);
            return new ArrayList(cacheReportModules.values());   
        }
    }
    
    @Messages({"JythonModuleLoader.pythonInterpreterError.title=Python Modules",
                "JythonModuleLoader.pythonInterpreterError.msg=Failed to load python modules, See log for more details"})
    private static <T> Map<String, ModuleRecord<T>> getInterfaceImplementations(Map<String, ModuleRecord<T>> cache, LineFilter filter, final Class<T> interfaceClass) {
        Map<String, ModuleRecord<T>> objects = new HashMap<>();
        Set<File> pythonModuleDirs = new HashSet<>();
        PythonInterpreter interpreterLoader = null;
        // This method has previously thrown unchecked exceptions when it could not load because of non-latin characters.
        try {
            interpreterLoader = new PythonInterpreter();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to load python Intepreter. Cannot load python modules", ex);
            if(RuntimeProperties.runningWithGUI()){
                MessageNotifyUtil.Notify.show(Bundle.JythonModuleLoader_pythonInterpreterError_title(),Bundle.JythonModuleLoader_pythonInterpreterError_msg(), MessageNotifyUtil.MessageType.ERROR);
            }
            return objects;
        }
        
        final PythonInterpreter interpreter = interpreterLoader;
        
        // add python modules from 'autospy/build/cluster/InternalPythonModules' folder
        // which are copied from 'autopsy/*/release/InternalPythonModules' folders.
        for (File f : InstalledFileLocator.getDefault().locateAll("InternalPythonModules", "org.sleuthkit.autopsy.core", false)) { //NON-NLS
            Collections.addAll(pythonModuleDirs, f.listFiles());
        }
        // add python modules from 'testuserdir/python_modules' folder
        Collections.addAll(pythonModuleDirs, new File(PlatformUtil.getUserPythonModulesPath()).listFiles());

        for (File file : pythonModuleDirs) {
            if (file.isDirectory()) {
                File[] pythonScripts = file.listFiles(new PythonScriptFileFilter());
                for (File script : pythonScripts) {
                        try (Scanner fileScanner = new Scanner(script)) {
                        while (fileScanner.hasNextLine()) {
                            String line = fileScanner.nextLine();
                            if (line.startsWith("class ") && filter.accept(line)) { //NON-NLS
                                final String className = line.substring(6, line.indexOf("("));
                                try {
                                    Callable<T> converter = () -> createObjectFromScript(interpreter, script, className, interfaceClass);
                                    ModuleRecord<T> jythonScript = getCachedOrNew(cache, script, converter);
                                    objects.put(jythonScript.getFilePath(), jythonScript);
                                } catch (Exception ex) {
                                    logger.log(Level.SEVERE, String.format("Failed to load %s from %s", className, script.getAbsolutePath()), ex); //NON-NLS
                                    // NOTE: using ex.toString() because the current version is always returning null for ex.getMessage().
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                            NbBundle.getMessage(JythonModuleLoader.class, "JythonModuleLoader.errorMessages.failedToLoadModule", className, ex.toString()),
                                            NotifyDescriptor.ERROR_MESSAGE));
                                }
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.SEVERE, String.format("Failed to open %s", script.getAbsolutePath()), ex); //NON-NLS
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(JythonModuleLoader.class, "JythonModuleLoader.errorMessages.failedToOpenModule", script.getAbsolutePath()),
                                NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            }
        }
        return objects;
    }

    private static <T> T createObjectFromScript(PythonInterpreter interpreter, File script, String className, Class<T> interfaceClass) {
        // Add the directory where the Python script resides to the Python
        // module search path to allow the script to use other scripts bundled
        // with it.
        interpreter.exec("import sys"); //NON-NLS
        String path = Matcher.quoteReplacement(script.getParent());
        interpreter.exec("sys.path.append('" + path + "')"); //NON-NLS
        String moduleName = script.getName().replaceAll("\\.py$", ""); //NON-NLS

        // reload the module so that the changes made to it can be loaded.
        interpreter.exec("import " + moduleName); //NON-NLS
        interpreter.exec("reload(" + moduleName + ")"); //NON-NLS

        // Importing the appropriate class from the Py Script which contains multiple classes.
        interpreter.exec("from " + moduleName + " import " + className); //NON-NLS
        interpreter.exec("obj = " + className + "()"); //NON-NLS

        T obj = interpreter.get("obj", interfaceClass); //NON-NLS

        // Remove the directory where the Python script resides from the Python
        // module search path.
        interpreter.exec("sys.path.remove('" + path + "')"); //NON-NLS

        return obj;
    }

    private static class PythonScriptFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".py"); //NON-NLS
        } //NON-NLS
    }

    private static interface LineFilter {

        boolean accept(String line);
    }

    private static class IngestModuleFactoryDefFilter implements LineFilter {

        @Override
        public boolean accept(String line) {
            return (line.contains("IngestModuleFactoryAdapter") || line.contains("IngestModuleFactory")); //NON-NLS
        }
    }

    private static class GeneralReportModuleDefFilter implements LineFilter {

        @Override
        public boolean accept(String line) {
            return (line.contains("GeneralReportModuleAdapter") || line.contains("GeneralReportModule")); //NON-NLS
        }
    }
}
