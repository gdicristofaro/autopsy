/*
 * Autopsy Forensic Browser
 *
 * Copyright 2021 Basis Technology Corp.
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
package org.sleuthkit.autopsy.datamodel.persons;

import java.awt.Dialog;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.persons.Bundle;
import org.sleuthkit.datamodel.Person;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Panel to be displayed as a part of the add datasource wizard. Provides the
 * ability to select current person.
 */
public class SelectPersonPanel extends javax.swing.JPanel {

    /**
     * A combo box item for a person (or null for default).
     */
    @Messages({
        "SelectPersonPanel_PersonCbItem_defaultPerson=Default"
    })
    private static class PersonCbItem {

        private final Person person;

        /**
         * Main constructor.
         *
         * @param person The person.
         */
        PersonCbItem(Person person) {
            this.person = person;
        }

        /**
         * @return The person.
         */
        Person getPerson() {
            return person;
        }

        @Override
        public String toString() {
            if (person == null) {
                return Bundle.SelectPersonPanel_PersonCbItem_defaultPerson();
            } else if (person.getName() == null) {
                return "";
            } else {
                return person.getName();
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Objects.hashCode(this.person == null ? 0 : this.person.getId());
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PersonCbItem other = (PersonCbItem) obj;
            if (!Objects.equals(
                    this.person == null ? 0 : this.person.getId(),
                    other.person == null ? 0 : other.person.getId())) {

                return false;
            }
            return true;
        }

    }

    private static final Logger logger = Logger.getLogger(SelectPersonPanel.class.getName());

    /**
     * Creates new form SelectPersonPanel
     */
    public SelectPersonPanel() {
        initComponents();
        loadPersonData();
        this.comboBoxPersonName.addItem(new PersonCbItem(null));
    }

    /**
     * @return The currently selected person or null if no selection.
     */
    public Person getSelectedPerson() {
        return comboBoxPersonName.getSelectedItem() instanceof PersonCbItem
                ? ((PersonCbItem) comboBoxPersonName.getSelectedItem()).getPerson()
                : null;
    }

    /**
     * Loads persons from database and displays in combo box.
     */
    private void loadPersonData() {
        Stream<PersonCbItem> itemsStream;
        try {
            itemsStream = Case.getCurrentCaseThrows().getSleuthkitCase().getPersonManager().getPersons().stream()
                    .filter(h -> h != null)
                    .sorted((a, b) -> getNameOrEmpty(a).compareToIgnoreCase(getNameOrEmpty(b)))
                    .map((h) -> new PersonCbItem(h));

            Vector<PersonCbItem> persons = Stream.concat(Stream.of(new PersonCbItem(null)), itemsStream)
                    .collect(Collectors.toCollection(Vector::new));

            comboBoxPersonName.setModel(new DefaultComboBoxModel<>(persons));
        } catch (NoCurrentCaseException | TskCoreException ex) {
            logger.log(Level.WARNING, "Unable to display person items with no current case.", ex);
        }
    }

    /**
     * Returns the name of the person or an empty string if the person or person name
     * is null.
     *
     * @param person The person.
     * @return The person name or empty string.
     */
    private String getNameOrEmpty(Person person) {
        return person == null || person.getName() == null ? "" : person.getName();
    }

    /**
     * Sets the selected person in the combo box with the specified person id. If
     * person id is null or person id is not found in list, 'default' will be
     * selected.
     *
     * @param personId The person id.
     */
    private void setSelectedPersonById(Long personId) {
        int itemCount = comboBoxPersonName.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            PersonCbItem curItem = comboBoxPersonName.getItemAt(i);
            if (curItem == null) {
                continue;
            }

            Long curId = curItem.getPerson() == null ? null : curItem.getPerson().getId();
            if (curId == personId) {
                comboBoxPersonName.setSelectedIndex(i);
                return;
            }
        }

        // set to first item which should be 'Default'
        comboBoxPersonName.setSelectedIndex(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        comboBoxPersonName = new javax.swing.JComboBox<>();
        javax.swing.JButton bnManagePersons = new javax.swing.JButton();

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));

        comboBoxPersonName.setMaximumSize(new java.awt.Dimension(32767, 22));
        comboBoxPersonName.setMinimumSize(new java.awt.Dimension(200, 22));
        comboBoxPersonName.setPreferredSize(new java.awt.Dimension(200, 22));
        add(comboBoxPersonName);

        org.openide.awt.Mnemonics.setLocalizedText(bnManagePersons, org.openide.util.NbBundle.getMessage(SelectPersonPanel.class, "SelectPersonPanel.bnManagePersons.text")); // NOI18N
        bnManagePersons.setMargin(new java.awt.Insets(2, 6, 2, 6));
        bnManagePersons.setMaximumSize(new java.awt.Dimension(140, 23));
        bnManagePersons.setMinimumSize(new java.awt.Dimension(140, 23));
        bnManagePersons.setPreferredSize(new java.awt.Dimension(140, 23));
        bnManagePersons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnManagePersonsActionPerformed(evt);
            }
        });
        add(bnManagePersons);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectPersonPanel.class, "SelectPersonPanel.title")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void bnManagePersonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bnManagePersonsActionPerformed
        ManagePersonsDialog dialog = new ManagePersonsDialog((Dialog) SwingUtilities.getWindowAncestor(this));
        dialog.setResizable(false);
        if (this.getParent() != null) {
            dialog.setLocationRelativeTo(this.getParent());
        }
        dialog.setVisible(true);
        dialog.toFront();
        loadPersonData();
        if (dialog.getSelectedPerson() != null) {
            setSelectedPersonById(dialog.getSelectedPerson().getId());
        }
    }//GEN-LAST:event_bnManagePersonsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<PersonCbItem> comboBoxPersonName;
    // End of variables declaration//GEN-END:variables
}
