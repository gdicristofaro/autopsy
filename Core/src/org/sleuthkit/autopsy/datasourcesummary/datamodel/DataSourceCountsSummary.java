/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 - 2020 Basis Technology Corp.
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

import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * Provides information to populate the DataSourceSummaryCountsPanel.
 */
public class DataSourceCountsSummary {
    private static final Logger logger = Logger.getLogger(DataSourceCountsSummary.class.getName());
    
    /**
     * Get count of regular files (not directories) in a data source.
     *
     * @param currentDataSource The data source.
     *
     * @return The count.
     */
    public static Long getCountOfFiles(DataSource currentDataSource) {
        return DataSourceInfoUtilities.getCountOfRegularFiles(currentDataSource, null,
                "Unable to get count of files, providing empty results");
    }

    /**
     * Get count of allocated files in a data source.
     *
     * @param currentDataSource The data source.
     *
     * @return The count.
     */
    public static Long getCountOfAllocatedFiles(DataSource currentDataSource) {
        return DataSourceInfoUtilities.getCountOfRegularFiles(currentDataSource,
                DataSourceInfoUtilities.getMetaFlagsContainsStatement(TskData.TSK_FS_META_FLAG_ENUM.ALLOC),
                "Unable to get counts of unallocated files for datasource, providing empty results");
    }

    /**
     * Get count of unallocated files in a data source.
     *
     * @param currentDataSource The data source.
     *
     * @return The count.
     */
    public static Long getCountOfUnallocatedFiles(DataSource currentDataSource) {
        return DataSourceInfoUtilities.getCountOfRegularFiles(currentDataSource,
                DataSourceInfoUtilities.getMetaFlagsContainsStatement(TskData.TSK_FS_META_FLAG_ENUM.UNALLOC)
                + " AND type<>" + TskData.TSK_DB_FILES_TYPE_ENUM.SLACK.getFileType(),
                "Unable to get counts of unallocated files for datasource, providing empty results");
    }

    /**
     * Get count of directories in a data source.
     *
     * @param currentDataSource The data source.
     *
     * @return The count.
     */
    public static Long getCountOfDirectories(DataSource currentDataSource) {
        return DataSourceInfoUtilities.getCountOfTskFiles(currentDataSource,
                "meta_type=" + TskData.TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_DIR.getValue()
                + " AND type<>" + TskData.TSK_DB_FILES_TYPE_ENUM.VIRTUAL_DIR.getFileType(),
                "Unable to get count of directories for datasource, providing empty results");
    }

    /**
     * Get count of slack files in a data source.
     *
     * @param currentDataSource The data source.
     *
     * @return The count.
     */
    public static Long getCountOfSlackFiles(DataSource currentDataSource) {
        return DataSourceInfoUtilities.getCountOfRegularFiles(currentDataSource,
                DataSourceInfoUtilities.getMetaFlagsContainsStatement(TskData.TSK_FS_META_FLAG_ENUM.UNALLOC)
                + " AND type=" + TskData.TSK_DB_FILES_TYPE_ENUM.SLACK.getFileType(),
                "Unable to get count of slack files for datasources, providing empty results");
    }
}
