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

import java.util.Date;
import java.util.Objects;

/**
 * Describes a result of a program run on a datasource.
 */
public class TopProgramsResult {

    private final String programName;
    private final String programPath;
    private final Long runTimes;
    private final Date lastRun;

    /**
     * Main constructor.
     *
     * @param programName The name of the program.
     * @param programPath The path of the program.
     * @param runTimes    The number of runs.
     */
    TopProgramsResult(String programName, String programPath, Long runTimes, Date lastRun) {
        this.programName = programName;
        this.programPath = programPath;
        this.runTimes = runTimes;
        this.lastRun = lastRun;
    }

    /**
     * @return The name of the program
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * @return The path of the program.
     */
    public String getProgramPath() {
        return programPath;
    }

    /**
     * @return The number of run times or null if not present.
     */
    public Long getRunTimes() {
        return runTimes;
    }

    /**
     * @return The last time the program was run or null if not present.
     */
    public Date getLastRun() {
        return lastRun;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.programName);
        hash = 43 * hash + Objects.hashCode(this.programPath);
        hash = 43 * hash + Objects.hashCode(this.runTimes);
        hash = 43 * hash + Objects.hashCode(this.lastRun);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TopProgramsResult other = (TopProgramsResult) obj;
        if (!Objects.equals(this.programName, other.programName)) {
            return false;
        }
        if (!Objects.equals(this.programPath, other.programPath)) {
            return false;
        }
        if (!Objects.equals(this.runTimes, other.runTimes)) {
            return false;
        }
        if (!Objects.equals(this.lastRun, other.lastRun)) {
            return false;
        }
        return true;
    }
    
    
}
