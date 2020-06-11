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
package org.sleuthkit.autopsy.contentviewers;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * This class displays TSK_CONTACT artifact.
 */
@ServiceProvider(service = ArtifactContentViewer.class)
public class ContactArtifactViewer extends javax.swing.JPanel implements ArtifactContentViewer {

    private final static Logger logger = Logger.getLogger(ContactArtifactViewer.class.getName());
    private static final long serialVersionUID = 1L;

    /**
     * Creates new form for ContactArtifactViewer
     */
    public ContactArtifactViewer() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        namePanel = new javax.swing.JPanel();
        contactNameLabel = new javax.swing.JLabel();
        phonesLabel = new javax.swing.JLabel();
        phoneNumbersPanel = new javax.swing.JPanel();
        emailsLabel = new javax.swing.JLabel();
        emailsPanel = new javax.swing.JPanel();
        othersLabel = new javax.swing.JLabel();
        otherAttrsPanel = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));

        setLayout(new java.awt.GridBagLayout());

        contactNameLabel.setFont(contactNameLabel.getFont().deriveFont((contactNameLabel.getFont().getStyle() | java.awt.Font.ITALIC) | java.awt.Font.BOLD, contactNameLabel.getFont().getSize()+6));
        org.openide.awt.Mnemonics.setLocalizedText(contactNameLabel, org.openide.util.NbBundle.getMessage(ContactArtifactViewer.class, "ContactArtifactViewer.contactNameLabel.text")); // NOI18N

        javax.swing.GroupLayout namePanelLayout = new javax.swing.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contactNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contactNameLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(namePanel, gridBagConstraints);

        phonesLabel.setFont(phonesLabel.getFont().deriveFont(phonesLabel.getFont().getStyle() | java.awt.Font.BOLD, phonesLabel.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(phonesLabel, org.openide.util.NbBundle.getMessage(ContactArtifactViewer.class, "ContactArtifactViewer.phonesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 0, 0);
        add(phonesLabel, gridBagConstraints);

        phoneNumbersPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 0, 0);
        add(phoneNumbersPanel, gridBagConstraints);

        emailsLabel.setFont(emailsLabel.getFont().deriveFont(emailsLabel.getFont().getStyle() | java.awt.Font.BOLD, emailsLabel.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(emailsLabel, org.openide.util.NbBundle.getMessage(ContactArtifactViewer.class, "ContactArtifactViewer.emailsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 0, 0);
        add(emailsLabel, gridBagConstraints);

        emailsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 0, 0);
        add(emailsPanel, gridBagConstraints);

        othersLabel.setFont(othersLabel.getFont().deriveFont(othersLabel.getFont().getStyle() | java.awt.Font.BOLD, othersLabel.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(othersLabel, org.openide.util.NbBundle.getMessage(ContactArtifactViewer.class, "ContactArtifactViewer.othersLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 0, 0);
        add(othersLabel, gridBagConstraints);

        otherAttrsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 0, 0);
        add(otherAttrsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.weightx = 1.0;
        add(filler2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void setArtifact(BlackboardArtifact artifact) {

        // wipe the panel clean
        this.removeAll();
        initComponents();

        List<BlackboardAttribute> phoneNumList = new ArrayList<>();
        List<BlackboardAttribute> emailList = new ArrayList<>();
        List<BlackboardAttribute> nameList = new ArrayList<>();
        List<BlackboardAttribute> otherList = new ArrayList<>();

        try {
            // Get all the attributes and group them by the section panels they go in
            for (BlackboardAttribute bba : artifact.getAttributes()) {
                if (bba.getAttributeType().getTypeName().startsWith("TSK_PHONE")) {
                    phoneNumList.add(bba);
                } else if (bba.getAttributeType().getTypeName().startsWith("TSK_EMAIL")) {
                    emailList.add(bba);
                } else if (bba.getAttributeType().getTypeName().startsWith("TSK_NAME")) {
                    nameList.add(bba);
                } else {
                    otherList.add(bba);
                }
            }
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting attributes for artifact (artifact_id=%d, obj_id=%d)", artifact.getArtifactID(), artifact.getObjectID()), ex);
        }

        // update name section
        updateNamePanel(nameList);
        
        // update contact attributes sections
        updateSection(phoneNumList, this.phonesLabel, this.phoneNumbersPanel);
        updateSection(emailList, this.emailsLabel, this.emailsPanel);
        updateSection(otherList, this.othersLabel, this.otherAttrsPanel);

        // repaint
        this.revalidate();
    }

    @Override
    public Component getComponent() {
        // Slap a vertical scrollbar on the panel.
        return new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Checks if the given artifact is supported by this viewer.
     * This viewer supports TSK_CONTACT artifacts.
     * 
     * @param artifact artifact to check.
     * @return True if the artifact is supported, false otherwise.
     */
    @Override
    public boolean isSupported(BlackboardArtifact artifact) {
        return artifact.getArtifactTypeID() == BlackboardArtifact.ARTIFACT_TYPE.TSK_CONTACT.getTypeID();
    }

    /**
     * Updates the contact name in the view.
     * 
     * @param attributesList 
     */
    private void updateNamePanel(List<BlackboardAttribute> attributesList) {
        for (BlackboardAttribute bba : attributesList) {
            if (bba.getAttributeType().getTypeName().startsWith("TSK_NAME")) {
                contactNameLabel.setText(bba.getDisplayString());
                break;
            }
        }

        contactNameLabel.revalidate();
    }

    /**
     * Updates the view by displaying the given list of attributes in the given section panel.
     * 
     * @param sectionAttributesList list of attributes to display.
     * @param sectionLabel section name label.
     * @param sectionPanel section panel to display the attributes in.
     */
    private void updateSection(List<BlackboardAttribute> sectionAttributesList, JLabel sectionLabel, JPanel sectionPanel) {

        // If there are no attributes for tis section, hide the section panel and the section label
        if (sectionAttributesList.isEmpty()) {
            sectionLabel.setVisible(false);
            sectionPanel.setVisible(false);
            return;
        }

        // create a gridbag layout to show each attribute on one line
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridy = 0;
        constraints.insets = new java.awt.Insets(4, 12, 0, 0);
        for (BlackboardAttribute bba : sectionAttributesList) {
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0;
            
            constraints.gridx = 0;

            // Add a label for attribute type
            javax.swing.JLabel attrTypeLabel = new javax.swing.JLabel();
            String attrLabel = bba.getAttributeType().getDisplayName();
            attrTypeLabel.setText(attrLabel);

            // make type label bold - uncomment if needed.
            //attrTypeLabel.setFont(attrTypeLabel.getFont().deriveFont(Font.BOLD, attrTypeLabel.getFont().getSize() ));

            gridBagLayout.setConstraints(attrTypeLabel, constraints);
            sectionPanel.add(attrTypeLabel);

            // Add the attribute value
            constraints.gridx++;
            javax.swing.JLabel attrValueLabel = new javax.swing.JLabel();
            attrValueLabel.setText(bba.getValueString());
            gridBagLayout.setConstraints(attrValueLabel, constraints);
            sectionPanel.add(attrValueLabel);

            // add a filler to take up rest of the space
            constraints.gridx++;
            constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            sectionPanel.add(new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0)));
            
            constraints.gridy++;
        }
        sectionPanel.setLayout(gridBagLayout);
        sectionPanel.revalidate();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel contactNameLabel;
    private javax.swing.JLabel emailsLabel;
    private javax.swing.JPanel emailsPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JPanel namePanel;
    private javax.swing.JPanel otherAttrsPanel;
    private javax.swing.JLabel othersLabel;
    private javax.swing.JPanel phoneNumbersPanel;
    private javax.swing.JLabel phonesLabel;
    // End of variables declaration//GEN-END:variables
}
