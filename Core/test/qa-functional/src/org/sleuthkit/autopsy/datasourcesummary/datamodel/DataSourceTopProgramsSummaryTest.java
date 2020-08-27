/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.sleuthkit.autopsy.testutils.CaseUtils;
import org.sleuthkit.datamodel.SleuthkitCase;

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
    
    //        VirtualDirectory rootDirectory = dataSource;
//        AbstractFile dataSourceRoot = rootDirectory;	// Let the root directory be the source for all artifacts
    
    
    private void testTopProgramsData() {
        String caseName = "testTopProgramsData";
        SleuthkitCase tskCase = CaseUtils.createAsCurrentCase(caseName).getSleuthkitCase();
        
        DataSourceTopProgramsSummary topProgsSumm = new DataSourceTopProgramsSummary(() -> tskCase);
        
        // is only datasource being queried?
        
        // are only PROG_RUN artifacts being queried?
        
        // are top programs being sorted on correct metrics?
        
        // are only top X being returned?
    }
}
