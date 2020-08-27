/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.LocalDirectory;
import org.sleuthkit.datamodel.LocalFile;
import org.sleuthkit.datamodel.LocalFilesDataSource;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * Utility methods for creating SleuthkitCase objects.
 */
public class ArtifactCreationUtils {

    private static final Logger logger = Logger.getLogger(ArtifactCreationUtils.class.getName());
    private static final String DEVICE_DIR_PREFIX = "DATA_SOURCE_";
    private static final String DEFAULT_INGEST_COMMENT = "TOP_PROGRAMS_FICTITIOUS_RESULT";
    private static final String DEFAULT_INGEST_NAME = "TOP_PROGRAMS_TEST_DATA";
    
    /**
     * Creates an artifact of the given type as a child of the parent content.
     * @param parent The content to which this artifact is attached.
     * @param type The artifact type.
     * @param attributes The attributes attached to the artifact.
     * @return The created artifact.
     */
    public static BlackboardArtifact createArtifact(Content parent, BlackboardArtifact.ARTIFACT_TYPE type, Collection<BlackboardAttribute> attributes) {
        BlackboardArtifact artifact = null;
        try {
            artifact = parent.newArtifact(type);
            artifact.addAttributes(attributes);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Unable to create artifact", ex);
        }
        return artifact;
    }

    /**
     * Creates a local files data source programmatically.  Attempts to fetch the actual filesystem content of the datasource will fail.
     * @param skCase The sleuthkitcase object.
     * @param id The id to use with the datasource
     * @return 
     */
    public static LocalFilesDataSource createLocalFilesDataSource(SleuthkitCase skCase, int id) {
        LocalFilesDataSource dataSource = null;
        try {
            SleuthkitCase.CaseDbTransaction trans = skCase.beginTransaction();
            dataSource = skCase.addLocalFilesDataSource(UUID.randomUUID().toString(), DEVICE_DIR_PREFIX + id, "", trans);
            trans.commit();
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Unable to create a data source", ex);
        }
        return dataSource;
    }


    public static LocalFile createLocalFile(SleuthkitCase skCase, AbstractFile parent, String fileName, String path) {
        LocalFile file = null;
        try {
            file = skCase.addLocalFile(fileName, Paths.get(path, fileName).toString(), 4096, 0, 0, 0, 0, true, TskData.EncodingType.NONE, parent);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Unable to create a local file", ex);
        }
        return file;
    }
    
    public static LocalDirectory createLocalDirectory(SleuthkitCase skCase, AbstractFile parent, String fileName) {
        LocalDirectory tskDir = null;
        try {
             tskDir = skCase.addLocalDirectory(parent.getId(), fileName);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Unable to create a local directory", ex);
        }
        return tskDir;
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

        Collection<Entry<String, DirectoryTree<T>>> getBranches() {
            return Collections.unmodifiableCollection(branches.entrySet());
        }
    }

    public static void createTopProgramResult(SleuthkitCase skCase, long dataSourceId, AbstractFile rootParent, Collection<TopProgramsResult> toAdd) {
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

        for (Entry<String, DirectoryTree<TopProgramsResult>> childDir : tree.getBranches()) {
            AbstractFile tskDir = createLocalDirectory(skCase, parent, childDir.getKey());
            createTopProgramsResult(skCase, tskDir, childDir.getValue());
        }
    }



    public static void addTopProgramsResult(SleuthkitCase skCase, AbstractFile parent, TopProgramsResult child) {
        String path = parent.getParentPath();
        AbstractFile file = createLocalFile(skCase, parent, path, child.getProgramName());

        createArtifact(file, ARTIFACT_TYPE.TSK_PROG_RUN, Arrays.asList(
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
