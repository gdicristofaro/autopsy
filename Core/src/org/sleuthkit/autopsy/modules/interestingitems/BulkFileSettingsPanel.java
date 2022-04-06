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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.guiutils.JFileChooserFactory;
import org.sleuthkit.autopsy.modules.interestingitems.BulkFileSetImportExport.FileSetsData;

/**
 * Panel for handling file set import / export.
 */
@Messages({
    "BulkFileSettingsPanel_fileExtDescription=Bulk File Set File"
})
class BulkFileSettingsPanel extends javax.swing.JPanel {

    private static final Logger logger = Logger.getLogger(BulkFileSettingsPanel.class.getName());

    private final JFileChooserFactory chooserFactory = new JFileChooserFactory();
    private final BulkFileSetImportExport fileImportExport = BulkFileSetImportExport.getInstance();
    private final DefaultListModel<FilesSet> interestingFileSetsModel = new DefaultListModel<FilesSet>();
    private final DefaultListModel<FilesSet> fileIngestFilterModel = new DefaultListModel<FilesSet>();
    
    private Map<String, FilesSet> interestingFilesSetMap = new HashMap<>();
    private Map<String, FilesSet> fileIngestFilterMap = new HashMap<>();
    
    private JFileChooser fileChooser = null;
    private FileFilter fileFilter = new FileNameExtensionFilter(Bundle.BulkFileSettingsPanel_fileExtDescription(), "xml");

    /**
     * Creates new form BulkFileSettingsPanel.
     */
    public BulkFileSettingsPanel() {
        initComponents();

        // set up listener for changes in selection to enable or disable
        fileIngestFilterList.addListSelectionListener(event -> setEnabled());
        interestingFileSetsList.addListSelectionListener(event -> setEnabled());
    }

    /**
     * @return A file chooser for importing or exporting.
     */
    private JFileChooser getChooser() {
        if (fileChooser == null) {
            fileChooser = chooserFactory.getChooser();
            fileChooser.setDragEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.addChoosableFileFilter(fileFilter);
        }

        return fileChooser;
    }

    /**
     * Loads all data to display in UI and performs refresh as necessary.
     */
    void load() {
        interestingFilesSetMap.clear();
        fileIngestFilterMap.clear();
        refresh();
    }
    
    /**
     * Refreshes the view without clearing out any loaded data.
     */
    void refresh() {
        try {
            FilesSetsManager setsManager = FilesSetsManager.getInstance();
            // filter out any standard sets
            Map<String, FilesSet> curInterestingFiles = setsManager.getInterestingFilesSets().entrySet().stream()
                    .filter(e -> !e.getValue().isStandardSet())
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (a,b) -> a));
            
            // add in any unsaved sets
            curInterestingFiles.putAll(interestingFilesSetMap);
            
            Map<String, FilesSet> curFileFilters = new HashMap<>(setsManager.getCustomFileIngestFilters());
            // add in any unsaved sets
            curFileFilters.putAll(fileIngestFilterMap);
            
            
            loadList(interestingFileSetsModel, curInterestingFiles);
            loadList(fileIngestFilterModel, curFileFilters);
            setEnabled();
        } catch (FilesSetsManager.FilesSetsManagerException ex) {
            logger.log(Level.WARNING, "There was an error loading current file sets.", ex);
        }        
    }
    
    /**
     * Stores data in settings.
     */
    void store() {
        fileImportExport.importFileSets(fileIngestFilterMap, interestingFilesSetMap);
    }

    /**
     * Loads a list of files sets.
     *
     * @param listModel   The list model to populate.
     * @param fileSetList The file set map to load.
     */
    private void loadList(DefaultListModel<FilesSet> listModel, Map<String, FilesSet> fileSetList) {
        listModel.clear();
        if (fileSetList != null) {
            fileSetList.entrySet().stream()
                    .sorted(Comparator.comparing(e -> StringUtils.defaultString(e.getKey())))
                    .map(e -> e.getValue())
                    .forEachOrdered(listModel::addElement);
        }
    }

    /**
     * Sets export button enabled if there is a file set to export.
     */
    private void setEnabled() {
        this.exportSetButton.setEnabled(
                fileIngestFilterList.getSelectedIndex() >= 0
                || interestingFileSetsList.getSelectedIndex() >= 0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane parentScrollPanel = new javax.swing.JScrollPane();
        javax.swing.JPanel parentPanel = new javax.swing.JPanel();
        javax.swing.JLabel description = new javax.swing.JLabel();
        javax.swing.JPanel importBulkFileSetPanel = new javax.swing.JPanel();
        javax.swing.JLabel overwriteWarningLabel = new javax.swing.JLabel();
        javax.swing.JButton importSetButton = new javax.swing.JButton();
        javax.swing.JPanel importBulkFileSetPanel1 = new javax.swing.JPanel();
        javax.swing.JScrollPane fileIngestFilterScrollPane = new javax.swing.JScrollPane();
        fileIngestFilterList = new javax.swing.JList<>();
        javax.swing.JLabel fileIngestFiltersLabel = new javax.swing.JLabel();
        javax.swing.JLabel interestingFileSetLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane interestingFileSetsScrollPane = new javax.swing.JScrollPane();
        interestingFileSetsList = new javax.swing.JList<>();
        javax.swing.JLabel holdControlLabel = new javax.swing.JLabel();
        exportSetButton = new javax.swing.JButton();

        parentScrollPanel.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(description, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.description.text")); // NOI18N

        importBulkFileSetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.importBulkFileSetPanel.border.title"))); // NOI18N

        overwriteWarningLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(overwriteWarningLabel, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.overwriteWarningLabel.text")); // NOI18N

        importSetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/import16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(importSetButton, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.importSetButton.text")); // NOI18N
        importSetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        importSetButton.setMaximumSize(new java.awt.Dimension(111, 25));
        importSetButton.setMinimumSize(new java.awt.Dimension(111, 25));
        importSetButton.setPreferredSize(new java.awt.Dimension(111, 25));
        importSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importSetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout importBulkFileSetPanelLayout = new javax.swing.GroupLayout(importBulkFileSetPanel);
        importBulkFileSetPanel.setLayout(importBulkFileSetPanelLayout);
        importBulkFileSetPanelLayout.setHorizontalGroup(
            importBulkFileSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(importBulkFileSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(importBulkFileSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(overwriteWarningLabel)
                    .addComponent(importSetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        importBulkFileSetPanelLayout.setVerticalGroup(
            importBulkFileSetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(importBulkFileSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(overwriteWarningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importSetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        importBulkFileSetPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.importBulkFileSetPanel1.border.title"))); // NOI18N

        fileIngestFilterList.setModel(fileIngestFilterModel);
        fileIngestFilterScrollPane.setViewportView(fileIngestFilterList);

        org.openide.awt.Mnemonics.setLocalizedText(fileIngestFiltersLabel, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.fileIngestFiltersLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(interestingFileSetLabel, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.interestingFileSetLabel.text")); // NOI18N

        interestingFileSetsList.setModel(interestingFileSetsModel);
        interestingFileSetsScrollPane.setViewportView(interestingFileSetsList);

        org.openide.awt.Mnemonics.setLocalizedText(holdControlLabel, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.holdControlLabel.text")); // NOI18N

        exportSetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/export16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(exportSetButton, org.openide.util.NbBundle.getMessage(BulkFileSettingsPanel.class, "BulkFileSettingsPanel.exportSetButton.text")); // NOI18N
        exportSetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        exportSetButton.setMaximumSize(new java.awt.Dimension(111, 25));
        exportSetButton.setMinimumSize(new java.awt.Dimension(111, 25));
        exportSetButton.setPreferredSize(new java.awt.Dimension(111, 25));
        exportSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout importBulkFileSetPanel1Layout = new javax.swing.GroupLayout(importBulkFileSetPanel1);
        importBulkFileSetPanel1.setLayout(importBulkFileSetPanel1Layout);
        importBulkFileSetPanel1Layout.setHorizontalGroup(
            importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(importBulkFileSetPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(importBulkFileSetPanel1Layout.createSequentialGroup()
                        .addGroup(importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileIngestFilterScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fileIngestFiltersLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(interestingFileSetLabel)
                            .addComponent(interestingFileSetsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(holdControlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportSetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        importBulkFileSetPanel1Layout.setVerticalGroup(
            importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, importBulkFileSetPanel1Layout.createSequentialGroup()
                .addGroup(importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileIngestFiltersLabel)
                    .addComponent(interestingFileSetLabel))
                .addGap(9, 9, 9)
                .addGroup(importBulkFileSetPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(interestingFileSetsScrollPane)
                    .addComponent(fileIngestFilterScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(holdControlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exportSetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout parentPanelLayout = new javax.swing.GroupLayout(parentPanel);
        parentPanel.setLayout(parentPanelLayout);
        parentPanelLayout.setHorizontalGroup(
            parentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(description)
                    .addComponent(importBulkFileSetPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(importBulkFileSetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        parentPanelLayout.setVerticalGroup(
            parentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(description)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importBulkFileSetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importBulkFileSetPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        parentScrollPanel.setViewportView(parentPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(parentScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(parentScrollPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    @Messages({
        "BulkFileSettingsPanel_importSetButtonActionPerformed_noFile_title=File Does Not Exist",
        "# {0} - filePath",
        "BulkFileSettingsPanel_importSetButtonActionPerformed_noFile_description=No file exists at {0}.  Please try an existing file.",
        "BulkFileSettingsPanel_importSetButtonActionPerformed_failure_title=Import Failure",
        "# {0} - filePath",
        "BulkFileSettingsPanel_importSetButtonActionPerformed_failure_description=There was an error importing file at: {0}.",})
    private void importSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importSetButtonActionPerformed
        JFileChooser fileChooser = getChooser();
        int returnState = fileChooser.showOpenDialog(this);
        if (returnState == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.exists()) {
                JOptionPane.showMessageDialog(this,
                        Bundle.BulkFileSettingsPanel_importSetButtonActionPerformed_noFile_description(selectedFile.getPath()),
                        Bundle.BulkFileSettingsPanel_importSetButtonActionPerformed_noFile_title(),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            FileSetsData fileSetData = fileImportExport.getFileSets(selectedFile);
            if (fileSetData == null) {
                JOptionPane.showMessageDialog(this,
                        Bundle.BulkFileSettingsPanel_importSetButtonActionPerformed_failure_description(selectedFile.getPath()),
                        Bundle.BulkFileSettingsPanel_importSetButtonActionPerformed_failure_title(),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.fileIngestFilterMap.putAll(fileSetData.getFileFilterSets());
            this.interestingFilesSetMap.putAll(fileSetData.getInterestingFileSets());
            
            firePropertyChange(OptionsPanelController.PROP_CHANGED, null, null);

            refresh();
        }
    }//GEN-LAST:event_importSetButtonActionPerformed

    @Messages({
        "BulkFileSettingsPanel_exportSetButtonActionPerformed_overwrite_title=File Exists",
        "# {0} - filePath",
        "BulkFileSettingsPanel_exportSetButtonActionPerformed_overwrite_description=Destination file {0} already exists, overwrite?",
        "BulkFileSettingsPanel_exportSetButtonActionPerformed_noSelected_title=No File Sets Selected",
        "BulkFileSettingsPanel_exportSetButtonActionPerformed_noSelected_description=Please select at least one file set to export.",
        "BulkFileSettingsPanel_exportSetButtonActionPerformed_failure_title=Export Failure",
        "BulkFileSettingsPanel_exportSetButtonActionPerformed_failure_description=There was an error exporting the file sets.",})
    private void exportSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSetButtonActionPerformed
        JFileChooser fileChooser = getChooser();
        int returnState = fileChooser.showSaveDialog(this);
        if (returnState == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            if (!FilenameUtils.getExtension(selectedFile.getName()).equalsIgnoreCase("xml")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".xml");
            }

            if (selectedFile.exists() && JOptionPane.showConfirmDialog(this,
                    Bundle.BulkFileSettingsPanel_exportSetButtonActionPerformed_overwrite_description(selectedFile.getPath()),
                    Bundle.BulkFileSettingsPanel_exportSetButtonActionPerformed_overwrite_title(),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            List<FilesSet> fileIngestFilters = new ArrayList<>(fileIngestFilterList.getSelectedValuesList());
            List<FilesSet> interestingItemFilters = new ArrayList<>(interestingFileSetsList.getSelectedValuesList());

            if (fileIngestFilters.isEmpty() && interestingItemFilters.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        Bundle.BulkFileSettingsPanel_exportSetButtonActionPerformed_noSelected_description(),
                        Bundle.BulkFileSettingsPanel_exportSetButtonActionPerformed_noSelected_title(),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!fileImportExport.exportFileSets(selectedFile, fileIngestFilters, interestingItemFilters)) {
                JOptionPane.showMessageDialog(this,
                        Bundle.BulkFileSettingsPanel_exportSetButtonActionPerformed_failure_description(),
                        Bundle.BulkFileSettingsPanel_exportSetButtonActionPerformed_failure_title(),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            fileIngestFilterList.clearSelection();
            interestingFileSetsList.clearSelection();
        }
    }//GEN-LAST:event_exportSetButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportSetButton;
    private javax.swing.JList<FilesSet> fileIngestFilterList;
    private javax.swing.JList<FilesSet> interestingFileSetsList;
    // End of variables declaration//GEN-END:variables
}
