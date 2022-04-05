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
package org.sleuthkit.autopsy.modules.interestingitems;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.collections4.MapUtils;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.XMLUtil;
import static org.sleuthkit.autopsy.modules.interestingitems.InterestingItemsFilesSetSettings.readFilesSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles bulk import and export of file ingest filters and interesting item
 * file sets.
 */
class BulkFileSetImportExport {

    private static final BulkFileSetImportExport instance = new BulkFileSetImportExport();

    private static final Logger logger = Logger.getLogger(BulkFileSetImportExport.class.getName());

    private static final String ROOT_ELEMENT = "BulkImport";
    private static final String INTERESTING_FILES_ELEMENT = "InterestingFiles";
    private static final String INGEST_FILTERS_ELEMENT = "IngestFilters";
    private static final String RESOURCE_NAME = "BulkFileSetImportExport";

    private static final String XML_ENCODING = "UTF-8";

    /**
     * @return Singleton instance of this class.
     */
    public static BulkFileSetImportExport getInstance() {
        return instance;
    }

    private BulkFileSetImportExport() {
    }

    /**
     * Import a bulk files sets file. This will overwrite any interesting file
     * sets or file ingest filters with the same name as the imported file.
     *
     * @param input The input file.
     *
     * @return True if operation was successful.
     */
    public boolean importFileSets(File input) {
        Document doc = XMLUtil.loadDoc(BulkFileSetImportExport.class, input.getPath());

        Element root = getChildElement(doc.getElementsByTagName(ROOT_ELEMENT));
        if (root == null) {
            logger.log(Level.WARNING, MessageFormat.format("Root element: {0} cannot be found in file: {1}.", ROOT_ELEMENT, input.getPath()));
            return false;
        }

        Map<String, FilesSet> interestingFilesSets = getFilesSet(root, INTERESTING_FILES_ELEMENT, input.getPath());
        Map<String, FilesSet> ingestFiltersSets = getFilesSet(root, INGEST_FILTERS_ELEMENT, input.getPath());

        if (MapUtils.isEmpty(ingestFiltersSets) && MapUtils.isEmpty(interestingFilesSets)) {
            logger.log(Level.WARNING, MessageFormat.format("No interesting file sets or ingest filters could be found in file: {1}.", ROOT_ELEMENT, input.getPath()));
            return false;
        }

        FilesSetsManager setsManager = FilesSetsManager.getInstance();
        try {
            if (MapUtils.isNotEmpty(ingestFiltersSets)) {
                setsManager.setCustomFileIngestFilters(getMerged(setsManager.getCustomFileIngestFilters(), ingestFiltersSets));
            }

            if (MapUtils.isNotEmpty(interestingFilesSets)) {
                setsManager.setInterestingFilesSets(getMerged(setsManager.getInterestingFilesSets(), interestingFilesSets));
            }
        } catch (FilesSetsManager.FilesSetsManagerException ex) {
            logger.log(Level.WARNING, "There was an error saving file sets to disk.", ex);
            return false;
        }

        return true;
    }

    /**
     * Returns all found file sets from the parent tag element within the root
     * element.
     *
     * @param root      The root element.
     * @param parentTag The parent tag that should be a child of root and
     *                  contains file sets.
     *
     * @return The map of files set names to files sets.
     */
    private static Map<String, FilesSet> getFilesSet(Element root, String parentTag, String filePath) {
        Element parentEl = getChildElement(root.getElementsByTagName(parentTag));
        if (parentEl == null) {
            logger.log(Level.WARNING, MessageFormat.format("Element of name: {0} could not be found in root element: {1} of file: {2}.", ROOT_ELEMENT, parentTag, filePath));
            return Collections.emptyMap();
        }

        NodeList setElems = parentEl.getElementsByTagName(InterestingItemsFilesSetSettings.FILE_SET_TAG);
        if (setElems == null) {
            logger.log(Level.WARNING, MessageFormat.format("Could not file set tags within element: {0} could not be found in root element: {1} of file: {2}.", ROOT_ELEMENT, parentTag, filePath));
            return Collections.emptyMap();
        }

        Map<String, FilesSet> toRet = new HashMap<>();
        for (int i = 0; i < setElems.getLength(); ++i) {
            try {
                readFilesSet((Element) setElems.item(i), toRet, RESOURCE_NAME);
            } catch (FilesSetsManager.FilesSetsManagerException ex) {
                logger.log(Level.WARNING, MessageFormat.format("File set {0} in {1} could not be properly parsed in file {2}.", i, parentTag, filePath));
                logger.log(Level.WARNING, "There was an error reading file at index " + i + " from file.", ex);
                return Collections.emptyMap();
            }
        }
        return toRet;
    }

    /**
     * Given a node list, returns the first node as element. Otherwise, returns
     * null.
     *
     * @param nodelist The node list.
     *
     * @return The element or null.
     */
    private static Element getChildElement(NodeList nodelist) {
        return nodelist == null || nodelist.getLength() < 1 || (!(nodelist.item(0) instanceof Element))
                ? null
                : (Element) nodelist.item(0);
    }

    /**
     * Returns a hashmap combining original and new items with new items
     * overwriting any conflicts.
     *
     * @param orig     The original items.
     * @param newItems The new items.
     *
     * @return The derived hashmap.
     */
    private static Map<String, FilesSet> getMerged(Map<String, FilesSet> orig, Map<String, FilesSet> newItems) {
        Map<String, FilesSet> dest = new HashMap<>(orig);
        StandardInterestingFilesSetsLoader.copyOnNewer(newItems, dest);
        return dest;
    }

    /**
     * Export file sets to a file.
     *
     * @param output                 The output file.
     * @param fileIngestFilters      The file ingest filters to be exported.
     * @param interestingItemFilters The interesting file sets to be exported.
     *
     * @return True if operation was successful.
     */
    public boolean exportFileSets(File output, List<FilesSet> fileIngestFilters, List<FilesSet> interestingItemFilters) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(ROOT_ELEMENT);
            doc.appendChild(rootElement);

            Element ingestFiltersEl = doc.createElement(INGEST_FILTERS_ELEMENT);
            rootElement.appendChild(rootElement);
            InterestingItemsFilesSetSettings.writeXmlSets(doc, ingestFiltersEl, fileIngestFilters);

            Element interestingFilesEl = doc.createElement(INTERESTING_FILES_ELEMENT);
            rootElement.appendChild(rootElement);
            InterestingItemsFilesSetSettings.writeXmlSets(doc, interestingFilesEl, interestingItemFilters);

            return XMLUtil.saveDoc(InterestingItemsFilesSetSettings.class, output.getPath(), XML_ENCODING, doc);
        } catch (ParserConfigurationException ex) {
            logger.log(Level.WARNING, "There was an error exporting file sets to disk.", ex);
            return false;
        }
    }
}
