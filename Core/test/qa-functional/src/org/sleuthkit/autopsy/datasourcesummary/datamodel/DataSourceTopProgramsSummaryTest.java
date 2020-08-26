/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.testutils.CaseUtils;

/**
 *
 * @author gregd
 */
public class DataSourceTopProgramsSummaryTest extends NbTestCase {
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(DataSourceTopProgramsSummaryTest.class).
                clusters(".*").
                enableModules(".*");
        return conf.suite();
    }

    public DataSourceTopProgramsSummaryTest(String name) {
        super(name);
    }
    
    private void testTopProgramsData() {
        String caseName = "testTopProgramsData";
        Case tskCase = CaseUtils.createAsCurrentCase(caseName);
        
        DataSourceTopProgramsSummary topProgsSumm = new DataSourceTopProgramsSummary(() -> tskCase.getSleuthkitCase());
        
    }
}
