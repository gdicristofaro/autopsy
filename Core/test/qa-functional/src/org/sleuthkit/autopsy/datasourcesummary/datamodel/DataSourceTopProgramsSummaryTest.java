/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import static org.sleuthkit.autopsy.datasourcesummary.datamodel.ArtifactCreationUtils.createArtifact;
import static org.sleuthkit.autopsy.datasourcesummary.datamodel.ArtifactCreationUtils.createLocalDirectory;
import static org.sleuthkit.autopsy.datasourcesummary.datamodel.ArtifactCreationUtils.createLocalFile;
import org.sleuthkit.autopsy.testutils.CaseUtils;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.LocalFilesDataSource;
import org.sleuthkit.datamodel.SleuthkitCase;

/**
 *
 * @author gregd
 */
public class DataSourceTopProgramsSummaryTest extends NbTestCase {
    private static final String DEFAULT_INGEST_COMMENT = "TOP_PROGRAMS_FICTITIOUS_RESULT";
    private static final String DEFAULT_INGEST_NAME = "TOP_PROGRAMS_TEST_DATA";
    
    private static final Function<String, SleuthkitCase> CREATE_SQLITE = (caseName) -> CaseUtils.createAsCurrentCase(caseName).getSleuthkitCase();
    private static final Function<String, SleuthkitCase> CREATE_POSTGRES = (caseName) -> CaseUtils.createAsCurrentMultiUserCase(caseName).getSleuthkitCase();
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(DataSourceTopProgramsSummaryTest.class).
                clusters(".*").
                enableModules(".*");
        return conf.suite();
    }

    public DataSourceTopProgramsSummaryTest(String name) {
        super(name);
    }
        
    private static final List<TopProgramsResult> FAKE_PROGS = Arrays.asList(
        new TopProgramsResult("prog1.exe", "C:\\Program Files\\", 6L, new Date(1 * 24 * 60 * 60)),
        new TopProgramsResult("prog2.exe", "C:\\Windows\\", 5L, new Date(2 * 24 * 60 * 60)),
        new TopProgramsResult("prog3.exe", "C:\\Program Files (x86)\\", 4L, new Date(3 * 24 * 60 * 60)),
        new TopProgramsResult("prog4.exe", "C:\\Users\\testuser\\AppData\\SampleProgram\\", 3L, new Date(4 * 24 * 60 * 60)),
        new TopProgramsResult("prog5.exe", "C:\\Users\\testuser\\AppData\\SampleProgram\\", 2L, new Date(5 * 24 * 60 * 60)),
        new TopProgramsResult("prog6.exe", "C:\\Users\\testuser\\AppData\\SampleProgram\\", 1L, new Date(6 * 24 * 60 * 60))
    );


    
    private <T> void assertEqualSets(Collection<T> collection1, Collection<T> collection2, String id1, String id2) {
        if (collection1 == null && collection2 == null) {
            return;
        } 
        
        assertTrue(
                String.format("Expected both items to be non-null, %s: %s; %s: %s", id1, collection1, id2, collection2),
                collection1 != null && collection2 != null);
        
        assertEquals(
                String.format("Expected %s: size %d to be the same size as %s: size %d", id1, collection1.size(), id2, collection2.size()), 
                collection1.size(), collection2.size());
        
        Set<T> firstItems = new HashSet<T>(collection1);
        firstItems.retainAll(collection2);
        assertEquals(String.format("Not all elements are the same in %s and %s", id1, id2), collection1.size(), firstItems.size());
    }
 
    
    public void testCorrectDatasources() {
        testCorrectDatasources(CREATE_SQLITE);
        testCorrectDatasources(CREATE_POSTGRES);
    }
    
    private void testCorrectDatasources(Function<String, SleuthkitCase> caseCreator) {
        // get dependencies
        String caseName = "testCorrectDatasources";
        SleuthkitCase tskCase = caseCreator.apply(caseName);
        
        DataSourceTopProgramsSummary topProgsSumm = new DataSourceTopProgramsSummary(() -> tskCase);
        
        // create datasources
        LocalFilesDataSource ds1 = ArtifactCreationUtils.createLocalFilesDataSource(tskCase, 1);
        LocalFilesDataSource ds2 = ArtifactCreationUtils.createLocalFilesDataSource(tskCase, 2);
        
        // add half the items to one data source and half to the other
        List<TopProgramsResult> ds1Items = FAKE_PROGS.subList(0, 3);
        List<TopProgramsResult> ds2Items = FAKE_PROGS.subList(4, 6);
        
        createTopProgramResult(tskCase, ds1, ds1Items);
        createTopProgramResult(tskCase, ds2, ds2Items);
        
        // get the results from each data source
        List<TopProgramsResult> ds1Results = topProgsSumm.getTopPrograms(ds1, FAKE_PROGS.size());
        List<TopProgramsResult> ds2Results = topProgsSumm.getTopPrograms(ds1, FAKE_PROGS.size());

        // make sure the sets 
        assertEqualSets(ds1Items, ds1Results, "ds1 items added", "ds1 items retrieved");
        assertEqualSets(ds2Items, ds2Results, "ds2 items added", "ds2 items retrieved");
    }
    
    
    
    public static List<String> getDirectoryComponents(String path) {
        List<String> toRet = new ArrayList<>();
        File f = new File(path);
        do {
            toRet.add(f.getName());
            f = f.getParentFile();
        } while (f.getParentFile() != null);

        Collections.reverse(toRet);
        return toRet;
    }

    private static class DirectoryTree<T> {

        private final Set<T> nodes = new HashSet<>();
        private final Map<String, DirectoryTree<T>> branches = new HashMap<>();

        void addNode(T node) {
            this.nodes.add(node);
        }

        Collection<T> getNodes() {
            return Collections.unmodifiableCollection(nodes);
        }

        DirectoryTree<T> getOrCreateBranch(String key) {
            DirectoryTree<T> branch = branches.get(key);
            if (branch != null) {
                return branch;
            }

            DirectoryTree<T> toRet = new DirectoryTree<>();
            branches.put(key, toRet);
            return toRet;
        }

        Collection<Map.Entry<String, DirectoryTree<T>>> getBranches() {
            return Collections.unmodifiableCollection(branches.entrySet());
        }
    }

    private static void createTopProgramResult(SleuthkitCase skCase, AbstractFile rootParent, Collection<TopProgramsResult> toAdd) {
        DirectoryTree<TopProgramsResult> directoryTree = new DirectoryTree<>();
        for (TopProgramsResult topProg : toAdd) {
            List<String> pathEls = getDirectoryComponents(topProg.getProgramPath());
            DirectoryTree<TopProgramsResult> folderDirTree = directoryTree;

            for (String folder : pathEls) {
                folderDirTree = directoryTree.getOrCreateBranch(folder);
            }

            folderDirTree.addNode(topProg);
        }

        createTopProgramsResult(skCase, rootParent, directoryTree);
    }

    private static void createTopProgramsResult(SleuthkitCase skCase, AbstractFile parent, DirectoryTree<TopProgramsResult> tree) {
        for (TopProgramsResult res : tree.getNodes()) {
            addTopProgramsResult(skCase, parent, res);
        }

        for (Map.Entry<String, DirectoryTree<TopProgramsResult>> childDir : tree.getBranches()) {
            AbstractFile tskDir = createLocalDirectory(skCase, parent, childDir.getKey());
            createTopProgramsResult(skCase, tskDir, childDir.getValue());
        }
    }

    private static void addTopProgramsResult(SleuthkitCase skCase, AbstractFile parent, TopProgramsResult child) {
        String path = parent.getParentPath();
        AbstractFile file = createLocalFile(skCase, parent, path, child.getProgramName());

        createArtifact(file, BlackboardArtifact.ARTIFACT_TYPE.TSK_PROG_RUN, Arrays.asList(
                new BlackboardAttribute(
                        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROG_NAME,
                        DEFAULT_INGEST_NAME,
                        child.getProgramName()),//NON-NLS
                new BlackboardAttribute(
                        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PATH,
                        DEFAULT_INGEST_NAME,
                        child.getProgramPath()),
                new BlackboardAttribute(
                        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME,
                        DEFAULT_INGEST_NAME,
                        child.getLastRun().getTime()),
                new BlackboardAttribute(
                        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COUNT,
                        DEFAULT_INGEST_NAME,
                        child.getRunTimes()),
                new BlackboardAttribute(
                        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT,
                        DEFAULT_INGEST_NAME,
                        DEFAULT_INGEST_COMMENT)
        ));
    }
}
