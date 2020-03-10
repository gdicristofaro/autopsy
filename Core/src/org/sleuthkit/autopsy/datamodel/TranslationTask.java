/*
 * Autopsy Forensic Browser
 *
 * Copyright 2018-2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.datamodel;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.utils.BackgroundTaskRunner;
import org.sleuthkit.autopsy.events.AutopsyEvent;
import org.sleuthkit.autopsy.texttranslation.NoServiceProviderException;
import org.sleuthkit.autopsy.texttranslation.TextTranslationService;
import org.sleuthkit.autopsy.texttranslation.TranslationException;

/**
 * Completes the tasks needed to populate the Translation columns in the
 * background so that the UI is not blocked while waiting for responses from the
 * translation service. Once the event is done, it fires a PropertyChangeEvent
 * to let the AbstractAbstractFileNode know it's time to update.
 */
class TranslationTask implements BackgroundTaskRunner.NodeTask {
    private static final Logger logger = Logger.getLogger(TranslationTask.class.getName());
    
    private final String origString;
    private final String eventName;
    private final TextTranslationService tts;

    public TranslationTask(String origString, String eventName) {
        this(origString, eventName, TextTranslationService.getInstance());
    }
        
    public TranslationTask(String origString, String eventName, TextTranslationService tts) {
        this.origString = origString;
        this.eventName = eventName;
        this.tts = tts;
    }

    
    private String translate(Future<?> future, String orig) {
        //If already in complete English, don't translate.
        if (orig.matches("^\\p{ASCII}+$")) {
            return "";
        }

        if (tts.hasProvider()) {
            //Seperate out the base and ext from the contents file name.
            String base = FilenameUtils.getBaseName(orig);
            try {
                if (future.isCancelled())
                    return null;
                        
                String translation = tts.translate(base);
                if (future.isCancelled())
                    return null;
                
                String ext = FilenameUtils.getExtension(orig);

                //If we have no extension, then we shouldn't add the .
                String extensionDelimiter = (ext.isEmpty()) ? "" : ".";

                //Talk directly to this nodes pcl, fire an update when the translation
                //is complete. 
                if (!translation.isEmpty()) {
                    return translation + extensionDelimiter + ext;
                }
            } catch (NoServiceProviderException noServiceEx) {
                logger.log(Level.WARNING, "Translate unsuccessful because no TextTranslator "
                        + "implementation was provided.", noServiceEx.getMessage());
            } catch (TranslationException noTranslationEx) {
                logger.log(Level.WARNING, "Could not successfully translate file name "
                        + orig, noTranslationEx.getMessage());
            }
        }
        return "";
    }
    
    
    @Override
    public PropertyChangeEvent run(Future<?> future) throws Exception {
        if (future.isCancelled())
            return null;

        String translatedFileName = translate(future, origString);
        if (!translatedFileName.isEmpty()) {
            //Only fire if the result is meaningful and the listener is not a stale reference
            
            return new PropertyChangeEvent(AutopsyEvent.SourceType.LOCAL.toString(), 
                    eventName, null, translatedFileName);
        }
        else {
            return null;
        }
    }
}
