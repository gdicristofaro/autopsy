/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
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

    /**
     * Creates an artifact of the given type as a child of the parent content.
     *
     * @param parent     The content to which this artifact is attached.
     * @param type       The artifact type.
     * @param attributes The attributes attached to the artifact.
     *
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
     * Creates a local files data source programmatically. Attempts to fetch the
     * actual filesystem content of the datasource will fail.
     *
     * @param skCase The SleuthkitCase object.
     * @param id     The id to use with the datasource.
     *
     * @return The programmatically created LocalFilesDataSource.
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

    /**
     * Create a local file programmatically. Attempts to fetch the actual file
     * contents will fail.
     *
     * @param skCase   The SleuthkitCase object.
     * @param parent   The parent of the file being created.
     * @param fileName The name of the file.
     * @param path     The path of the file.
     *
     * @return The created file.
     */
    public static LocalFile createLocalFile(SleuthkitCase skCase, AbstractFile parent, String fileName, String path) {
        LocalFile file = null;
        try {
            file = skCase.addLocalFile(fileName, Paths.get(path, fileName).toString(), 4096, 0, 0, 0, 0, true, TskData.EncodingType.NONE, parent);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Unable to create a local file", ex);
        }
        return file;
    }

    /**
     * Create a local directory programmatically. This will be a fictitious
     * directory and not created on the file system.
     *
     * @param skCase   The SleuthkitCase object.
     * @param parent   The parent of the directry being created.
     * @param fileName The name of the directory.
     *
     * @return The created directory.
     */
    public static LocalDirectory createLocalDirectory(SleuthkitCase skCase, AbstractFile parent, String fileName) {
        LocalDirectory tskDir = null;
        try {
            tskDir = skCase.addLocalDirectory(parent.getId(), fileName);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Unable to create a local directory", ex);
        }
        return tskDir;
    }
}
