/*
 * Autopsy Forensic Browser
 *
 * Copyright 2022 Basis Technology Corp.
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
package org.sleuthkit.autopsy.corecomponentinterfaces;

/**
 * Describes the sorting based on a column.
 */
public class ColumnSort {

    private final boolean ascending;
    private final String columnKey;
    private final int sortRank;

    /**
     * Main constructor.
     * @param ascending If sorting should be ascending or descending.
     * @param columnKey The key of the column that should be sorted.
     * @param sortRank The sort rank (lower is higher in priority).
     */
    public ColumnSort(boolean ascending, String columnKey, int sortRank) {
        this.ascending = ascending;
        this.columnKey = columnKey;
        this.sortRank = sortRank;
    }

    /**
     * @return If sorting should be ascending or descending.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @return The key of the column that should be sorted.
     */
    public String getColumnKey() {
        return columnKey;
    }

    /**
     * @return The sort rank (lower is higher in priority).
     */
    public int getSortRank() {
        return sortRank;
    }
}
