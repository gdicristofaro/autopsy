/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.modules.embeddedfileextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import org.openide.modules.InstalledFileLocator;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * Provides utility to initialize SevenZip bindings utilizing known dll path
 * locations in order to avoid read/write errors. See 6528 for more details.
 */
public class SevenZipInitUtil {

    private static final String SEVENZIP_BINDINGS_FOLDER = "sevenzip";
    private static final String PLATFORMS_SUBFOLDER = "platforms";
    private static final String SEVENZIP_PROP_FILE = "sevenzipjbinding-lib.properties";
    private static final String SEVENZIP_LIB_PROPERTY = "lib.%d.name";

    // We won't load library 1000.
    private static final int MAX_LIMIT = 1000;

    /**
     * Initializes SevenZip java bindings without extracting native libraries to
     * temp files. This does require the native libraries to be in the expected
     * directory structure: thirdparty\sevenzip\platforms\{architecture}\.
     *
     * @return True if SevenZip java bindings successfully initialized.
     *
     * @throws SevenZipNativeInitializationException Thrown if there are issues
     *                                               in loading java bindings.
     */
    public static boolean initialize() throws SevenZipNativeInitializationException {
        String platform = SevenZip.getPlatformBestMatch();

        String propsFileLoc = Paths.get(SEVENZIP_BINDINGS_FOLDER, PLATFORMS_SUBFOLDER, platform, SEVENZIP_PROP_FILE).toString();

        Properties props = loadPropertiesFile(propsFileLoc, platform);

        loadNativeLibraries(props, propsFileLoc, platform);

        SevenZip.initLoadedLibraries();

        return true;
    }

    /**
     * Attempts to run System.load on any lib files found in the properties
     * file.
     *
     * @param props        The properties file.
     * @param propsFileLoc The path location of the properties file.
     * @param platform     The platform currently being used.
     *
     * @throws SevenZipNativeInitializationException Thrown if there are issues
     *                                               in loading java bindings.
     */
    private static void loadNativeLibraries(Properties props, String propsFileLoc, String platform) throws SevenZipNativeInitializationException {
        for (int libNum = 1; libNum < MAX_LIMIT; libNum++) {
            String propKey = String.format(SEVENZIP_LIB_PROPERTY, libNum);
            String libFileName = props.getProperty(propKey);
            if (libFileName == null) {
                if (libNum == 1) {
                    throw new SevenZipNativeInitializationException(
                            String.format("No libraries to load could be found in properties file: %s.", propsFileLoc));
                } else {
                    break;
                }
            } else {
                String libFileLoc = Paths.get(SEVENZIP_BINDINGS_FOLDER, PLATFORMS_SUBFOLDER, platform, libFileName).toString();
                File libToLoad = InstalledFileLocator.getDefault().locate(libFileLoc, SevenZipInitUtil.class.getPackage().getName(), false);
                if (libToLoad == null) {
                    throw new SevenZipNativeInitializationException(
                            String.format("Lib file: %s could not be found in platform directory: %s.", libFileName, platform));
                }
                try {
                    System.load(libToLoad.getAbsolutePath());
                } catch (Throwable t) {
                    throw new SevenZipNativeInitializationException(
                            String.format("There was an error while loading lib file: %s could not be found in platform directory: %s.", libFileName, platform),
                            t);
                }
            }
        }
    }

    /**
     * Attempts to load the properties file at the given location for the given
     * platform.
     *
     * @param propsFileLoc The absolute path to the properties file.
     * @param platform     The platform being used.
     *
     * @return The loaded properties object.
     *
     * @throws SevenZipNativeInitializationException Thrown if there are
     *                                               exceptions while loading
     *                                               the properties file.
     */
    private static Properties loadPropertiesFile(String propsFileLoc, String platform) throws SevenZipNativeInitializationException {
        File propsFile = InstalledFileLocator.getDefault().locate(propsFileLoc, SevenZipInitUtil.class.getPackage().getName(), false);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propsFile));
        } catch (FileNotFoundException ex) {
            throw new SevenZipNativeInitializationException(
                    String.format("Properties file for architecture: %s could not be found at %s.", platform, propsFileLoc),
                    ex);

        } catch (IOException ex) {
            throw new SevenZipNativeInitializationException(
                    String.format("Properties at %s had IO difficulties.", propsFileLoc),
                    ex);
        }
        return props;
    }

}
