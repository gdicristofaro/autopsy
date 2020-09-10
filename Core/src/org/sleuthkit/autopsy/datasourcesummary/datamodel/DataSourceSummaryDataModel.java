/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import java.util.Collections;
import java.util.Set;
import org.sleuthkit.autopsy.casemodule.Case;

/**
 * Interface for data model classes in data source summary.
 */
public interface DataSourceSummaryDataModel {
    /**
     * @return The set of Case Events for which data should be updated.
     */
    default Set<Case.Events> getCaseEventUpdates() {
        return Collections.emptySet();
    }

    /**
     * @return The set of BlackboardArtifact id's for which data shoulde be updated.
     */    
    default Set<Integer> getArtifactIdUpdates() {
        return Collections.emptySet();
    }
    
    /**
     * Whether or not the content should be updated on receiving new content.
     * @return True if it should refresh on new content.
     */
    default boolean shouldRefreshOnNewContent() {
        return false;
    }
}