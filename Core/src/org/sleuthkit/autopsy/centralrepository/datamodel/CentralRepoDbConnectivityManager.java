/*
 * Central Repository
 *
 * Copyright 2015-2020 Basis Technology Corp.
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
package org.sleuthkit.autopsy.centralrepository.datamodel;

/**
 * common interface for settings pertaining to the database in central repository
 */
public interface CentralRepoDbConnectivityManager {
    void loadSettings();
    
    void saveSettings();
        
    boolean createDatabase();

    boolean deleteDatabase();

    /**
     * Use the current settings and the validation query to test the connection
     * to the database.
     *
     * @return true if successfull connection, else false.
     */
    boolean verifyConnection();

    /**
     * Check to see if the database exists.
     *
     * @return true if exists, else false
     */
    boolean verifyDatabaseExists();

    /**
     * Use the current settings and the schema version query to test the
     * database schema.
     *
     * @return true if successful connection, else false.
     */
    boolean verifyDatabaseSchema();
    
    DatabaseTestResult testStatus();
    
}