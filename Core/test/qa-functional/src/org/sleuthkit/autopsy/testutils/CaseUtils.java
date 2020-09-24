/*
 * Autopsy Forensic Browser
 *
 * Copyright 2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.testutils;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.openide.util.Exceptions;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.CaseActionException;
import org.sleuthkit.autopsy.casemodule.CaseDetails;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.core.UserPreferences;
import org.sleuthkit.autopsy.core.UserPreferencesException;
import org.sleuthkit.autopsy.coreutils.TimeStampUtils;
import org.sleuthkit.datamodel.CaseDbConnectionInfo;
import org.sleuthkit.datamodel.TskData;

/**
 * Class with utility methods for opening and closing cases for functional
 * testing purposes.
 */
public final class CaseUtils {

    /**
     * Appends a time stamp to the given case name for uniqueness and creates a
     * case as the current case in the temp directory. Asserts if there is an
     * error creating the case.
     *
     * @param caseName The case name.
     *
     * @return The new case.
     */
    public static Case createAsCurrentCase(String caseName) {
        String uniqueCaseName = caseName + "_" + TimeStampUtils.createTimeStamp();
        Path caseDirectoryPath = Paths.get(System.getProperty("java.io.tmpdir"), uniqueCaseName);
        Case currentCase = null;
        try {
            Case.createAsCurrentCase(Case.CaseType.SINGLE_USER_CASE, caseDirectoryPath.toString(), new CaseDetails(uniqueCaseName));
            currentCase = Case.getCurrentCaseThrows();
        } catch (CaseActionException | NoCurrentCaseException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail(String.format("Failed to create case %s at %s: %s", uniqueCaseName, caseDirectoryPath, ex.getMessage()));
        }
        return currentCase;
    }

    private static String MU_SERVER_HOST_KEY = "QA_TEST_MU_SERVER_HOST";
    private static String MU_SERVER_PORT_KEY = "QA_TEST_MU_SERVER_PORT";
    private static String MU_SERVER_USERNAME_KEY = "QA_TEST_MU_SERVER_USERNAME";
    private static String MU_SERVER_PASSWORD_KEY = "QA_TEST_MU_SERVER_PASSWORD";
    private static String MU_TEST_PREFIX = "QA_TEST_TEMP_DB_";

    public static String getGlobalProp(String key) {
        String envVar = System.getenv(key);
        return StringUtils.isNotBlank(envVar)
                ? envVar
                : System.getProperty(key);
    }

    public static CaseDbConnectionInfo getMUConnFromEnv() {
        String hostNameOrIP = getGlobalProp(MU_SERVER_HOST_KEY);
        String portNumber = getGlobalProp(MU_SERVER_PORT_KEY);
        String userName = getGlobalProp(MU_SERVER_USERNAME_KEY);
        String password = getGlobalProp(MU_SERVER_PASSWORD_KEY);

        return new CaseDbConnectionInfo(hostNameOrIP, portNumber, userName, password, TskData.DbType.POSTGRESQL);
    }

    public static Case createAsCurrentMultiUserCase(String caseName) {
        return createAsCurrentMultiUserCase(getMUConnFromEnv(), caseName);
    }

    /**
     * Appends a time stamp to the given case name for uniqueness and creates a
     * case as the current case in the temp directory. Asserts if there is an
     * error creating the case.
     *
     * @param caseName The case name.
     *
     * @return The new case.
     */
    public static Case createAsCurrentMultiUserCase(CaseDbConnectionInfo info, String caseName) {
        if (info != null) {
            try {
                UserPreferences.setDatabaseConnectionInfo(info);
            } catch (UserPreferencesException ex) {
                Exceptions.printStackTrace(ex);
                Assert.fail("Failed to set connection settings.");
            }
        }

        String uniqueCaseName = MU_TEST_PREFIX + caseName + "_" + TimeStampUtils.createTimeStamp();
        Path caseDirectoryPath = Paths.get(System.getProperty("java.io.tmpdir"), uniqueCaseName);
        Case currentCase = null;
        try {
            CaseDetails caseDetails = new CaseDetails(uniqueCaseName);
            Case.createAsCurrentCase(Case.CaseType.MULTI_USER_CASE, caseDirectoryPath.toString(), caseDetails);
            currentCase = Case.getCurrentCaseThrows();
        } catch (CaseActionException | NoCurrentCaseException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail(String.format("Failed to create case %s at %s: %s", uniqueCaseName, caseDirectoryPath, ex.getMessage()));
        }
        return currentCase;
    }

    /**
     * Closes the current case, and optionally deletes it. Asserts if there is
     * no current case or if there is an error closing the current case.
     */
    public static void closeCurrentCase() {
        closeCurrentCase(false);
    }

    public static void closeCurrentCase(boolean delete) {
        Case currentCase;
        try {
            currentCase = Case.getCurrentCaseThrows();
        } catch (NoCurrentCaseException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail("Failed to get current case");
            return;
        }

        String caseName = currentCase.getName();
        String caseDirectory = currentCase.getCaseDirectory();
        try {
            if (delete) {
                Case.deleteCurrentCase();
            } else {
                Case.closeCurrentCase();
            }
        } catch (CaseActionException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail(String.format("Failed to close case %s at %s: %s", caseName, caseDirectory, ex.getMessage()));
        }
    }

    /**
     * Private constructor to prevent utility class object instantiation.
     */
    private CaseUtils() {
    }

}
