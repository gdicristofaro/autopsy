/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2017 Basis Technology Corp.
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
package org.sleuthkit.autopsy.modules.interestingitems;

import java.io.Serializable;
import java.util.Map;

/**
 * Class for wrapping a map which stores FilesSets as values with a String key.  This is
 * being kept for backwards compatibility when file sets definitions were written to disk
 * through object serialization.
 */
class FileSetsDefinitions implements Serializable {

    
    private static final long serialVersionUID = 1L;
    //By wrapping the map in this class we avoid warnings for unchecked casting when serializing
    private final Map<String, FilesSet> filesSets;

    FileSetsDefinitions(Map<String, FilesSet> filesSets) {
        this.filesSets = filesSets;
    }

    /**
     * @return the filesSets
     */
    Map<String, FilesSet> getFilesSets() {
        return filesSets;
    }
}