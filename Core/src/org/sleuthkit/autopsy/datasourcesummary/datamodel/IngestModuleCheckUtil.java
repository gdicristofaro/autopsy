/*
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
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

import org.apache.commons.lang3.StringUtils;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.IngestJobInfo;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Utilities for checking if an ingest module has been run on a datasource.
 */
public class IngestModuleCheckUtil {

    /**
     * Exception that is thrown in the event that a data source has not been
     * ingested with a particular ingest module.
     */
    public static class NotIngestedWithModuleException extends Exception {

        private static final long serialVersionUID = 1L;

        private final String moduleDisplayName;

        /**
         * Constructor.
         *
         * @param moduleDisplayName The module display name.
         * @param message           The message for the exception.
         */
        public NotIngestedWithModuleException(String moduleDisplayName, String message) {
            super(message);
            this.moduleDisplayName = moduleDisplayName;
        }

        /**
         * Constructor.
         *
         * @param moduleDisplayName The module display name.
         * @param message           The message for the exception.
         * @param thrwbl            Inner exception if applicable.
         */
        public NotIngestedWithModuleException(String moduleDisplayName, String message, Throwable thrwbl) {
            super(message, thrwbl);
            this.moduleDisplayName = moduleDisplayName;
        }

        /**
         * @return The module display name.
         */
        public String getModuleDisplayName() {
            return moduleDisplayName;
        }
    }

    /**
     * Whether or not the ingest job info contains the ingest modulename.
     * @param info The IngestJobInfo.
     * @param moduleName The module name.
     * @return True if the ingest module name is contained in the data.
     */
    private static boolean hasIngestModule(IngestJobInfo info, String moduleName) {
        if (info == null || info.getIngestModuleInfo() == null) {
            return false;
        }

        return info.getIngestModuleInfo().stream()
                .anyMatch((moduleInfo) -> {
                    return StringUtils.isNotBlank(moduleInfo.getDisplayName())
                            && moduleInfo.getDisplayName().trim().equalsIgnoreCase(moduleName);
                });
    }

    /**
     * Whether or not a data source has been ingested with a particular ingest module.
     * @param skCase The pertinent SleuthkitCase.
     * @param dataSource The datasource.
     * @param moduleName The module name.
     * @return Whether or not a data source has been ingested with a particular ingest module.
     * @throws TskCoreException 
     */
    public static boolean isModuleIngested(SleuthkitCase skCase, DataSource dataSource, String moduleName)
            throws TskCoreException {
        if (dataSource == null) {
            return false;
        }

        long dataSourceId = dataSource.getId();

        return skCase.getIngestJobs().stream()
                .anyMatch((ingestJob) -> {
                    return ingestJob != null
                            && ingestJob.getObjectId() == dataSourceId
                            && hasIngestModule(ingestJob, moduleName);
                });

    }

    /**
     * Throws a NotIngestedWithModuleException if data source has not been ingested with modulename.
     * @param skCase The SleuthkitCase.
     * @param dataSource The datasource.
     * @param moduleName The module name.
     * @throws TskCoreException
     * @throws NotIngestedWithModuleException 
     */
    public static void throwOnNotModuleIngested(SleuthkitCase skCase, DataSource dataSource, String moduleName)
            throws TskCoreException, NotIngestedWithModuleException {

        if (!isModuleIngested(skCase, dataSource, moduleName)) {
            String objectId = (dataSource == null) ? "<null>" : String.valueOf(dataSource.getId());
            String message = String.format("Data source: %s has not been ingested with the %s Ingest Module.", objectId, moduleName);
            throw new NotIngestedWithModuleException(moduleName, message);
        }
    }
    

    private IngestModuleCheckUtil() {
    }
}
