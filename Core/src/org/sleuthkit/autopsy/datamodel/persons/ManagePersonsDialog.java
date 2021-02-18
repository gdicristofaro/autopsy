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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.ListModel;
import org.apache.commons.collections4.CollectionUtils;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.persons.Bundle;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.Person;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Dialog for managing CRUD operations with persons from the UI.
 */
@Messages({
    "ManagePersonsDialog_title_text=Manage Persons"
})
public class ManagePersonsDialog extends javax.swing.JDialog {

    /**
     * List item to be used with jlist.
     */
    private static class PersonListItem {

        private final Person person;
        private final List<DataSource> dataSources;

        /**
         * Main constructor.
         *
         * @param person The person.
         * @param dataSources The data sources that are children of this person.
         */
        PersonListItem(Person person, List<DataSource> dataSources) {
            this.person = person;
            this.dataSources = dataSources;
        }

        /**
         * @return The person.
         */
        Person getPerson() {
            return person;
        }

        /**
         * @return The data sources associated with this person.
         */
        List<DataSource> getDataSources() {
            return dataSources;
        }

        @Override
        public String toString() {
            return person == null ? "" : person.getName();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.person == null ? 0 : this.person.getId());
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
            final PersonListItem other = (PersonListItem) obj;
            if (this.person == null || other.getPerson() == null) {
                return this.person == null && other.getPerson() == null;
            }

            return this.person.getId() == other.getPerson().getId();
        }

    }

    private static final Logger logger = Logger.getLogger(ManagePersonsDialog.class.getName());
    private static final long serialVersionUID = 1L;

    private Map<Person, List<DataSource>> personChildrenMap = Collections.emptyMap();

    /**
     * Main constructor.
     *
     * @param parent The parent frame.
     */
    public ManagePersonsDialog(java.awt.Frame parent) {
        super(parent, Bundle.ManagePersonsDialog_title_text(), true);
        init();
    }

    /**
     * Main constructor.
     *
     * @param parent The parent dialog.
     */
    public ManagePersonsDialog(Dialog parent) {
        super(parent, Bundle.ManagePersonsDialog_title_text(), true);
        init();
    }

    /**
     * Initializes components, loads person data, and sets up list listener.
     */
    private void init() {
        initComponents();
        refresh();

        // refreshes UI when selection changes including button enabled state and data.
        this.personList.addListSelectionListener((evt) -> refreshComponents());
    }

    /**
     * @return The currently selected person in the list or null if no person is
     * selected.
     */
    Person getSelectedPerson() {
        return (personList.getSelectedValue() == null) ? null : personList.getSelectedValue().getPerson();
    }

    /**
     * Shows add/edit dialog, and if a value is returned, creates a new Person.
     */
    private void addPerson() {
        String newPersonName = getAddEditDialogName(null);
        if (newPersonName != null) {
            Long selectedId = null;
            try {
                Person newPerson = Case.getCurrentCaseThrows().getSleuthkitCase().getPersonManager().createPerson(newPersonName);
                selectedId = newPerson == null ? null : newPerson.getId();
            } catch (NoCurrentCaseException | TskCoreException e) {
                logger.log(Level.WARNING, String.format("Unable to add new person '%s' at this time.", newPersonName), e);
            }
            refresh();
            setSelectedPersonById(selectedId);
        }
    }

    /**
     * Deletes the selected person if possible.
     *
     * @param selectedPerson
     */
    private void deletePerson(Person selectedPerson) {
        if (selectedPerson != null && selectedPerson.getName() != null) {
            try {
                Case.getCurrentCaseThrows().getSleuthkitCase().getPersonManager().deletePerson(selectedPerson.getName());
            } catch (NoCurrentCaseException | TskCoreException e) {
                logger.log(Level.WARNING, String.format("Unable to delete person '%s' at this time.", selectedPerson.getName()), e);
            }
            refresh();
        }
    }

    /**
     * Selects the person with the given id. If no matching id found in list.
     *
     * @param selectedId The id of the person to select.
     */
    private void setSelectedPersonById(Long selectedId) {
        ListModel<PersonListItem> model = personList.getModel();

        if (selectedId == null) {
            personList.clearSelection();
        }

        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (!(o instanceof PersonListItem)) {
                continue;
            }

            Person person = ((PersonListItem) o).getPerson();
            if (person == null) {
                continue;
            }

            if (person.getId() == selectedId) {
                personList.setSelectedIndex(i);
                return;
            }
        }

        personList.clearSelection();
    }

    /**
     * Shows add/edit dialog, and if a value is returned, creates a new Person.
     *
     * @param selectedPerson The selected person.
     */
    private void editPerson(Person selectedPerson) {

        if (selectedPerson != null) {
            String newPersonName = getAddEditDialogName(selectedPerson);
            if (newPersonName != null) {
                selectedPerson.setName(newPersonName);
                try {
                    Case.getCurrentCaseThrows().getSleuthkitCase().getPersonManager().updatePerson(selectedPerson);
                } catch (NoCurrentCaseException | TskCoreException e) {
                    logger.log(Level.WARNING, String.format("Unable to update person '%s' with id: %d at this time.", selectedPerson.getName(), selectedPerson.getId()), e);
                }

                PersonListItem selectedItem = personList.getSelectedValue();
                Long selectedId = selectedItem == null || selectedItem.getPerson() == null ? null : selectedItem.getPerson().getId();

                refresh();

                setSelectedPersonById(selectedId);
            }
        }
    }

    /**
     * Shows the dialog to add or edit the name of a person.
     *
     * @param origValue The original values for the person or null if adding a
     * person.
     * @return The new name for the person or null if operation was cancelled.
     */
    private String getAddEditDialogName(Person origValue) {
        JFrame parent = (this.getRootPane() != null && this.getRootPane().getParent() instanceof JFrame)
                ? (JFrame) this.getRootPane().getParent()
                : null;

        AddEditPersonDialog addEditDialog = new AddEditPersonDialog(parent, personChildrenMap.keySet(), origValue);
        addEditDialog.setResizable(false);
        addEditDialog.setLocationRelativeTo(parent);
        addEditDialog.setVisible(true);
        addEditDialog.toFront();

        if (addEditDialog.isChanged()) {
            String newPersonName = addEditDialog.getValue();
            return newPersonName;
        }

        return null;
    }

    /**
     * Refreshes the data and ui components for this dialog.
     */
    private void refresh() {
        refreshData();
        refreshComponents();
    }

    /**
     * Refreshes the data for this dialog and updates the person JList with the
     * persons.
     */
    private void refreshData() {

        personChildrenMap = getPersonListData();

        Vector<PersonListItem> jlistData = personChildrenMap.entrySet().stream()
                .sorted((a, b) -> getNameOrEmpty(a.getKey()).compareTo(getNameOrEmpty(b.getKey())))
                .map(entry -> new PersonListItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(Vector::new));

        personList.setListData(jlistData);
    }

    /**
     * Returns the name of the person or an empty string if the person or name of
     * person is null.
     *
     * @param h The person.
     * @return The name of the person or empty string.
     */
    private String getNameOrEmpty(Person h) {
        return (h == null || h.getName() == null) ? "" : h.getName();
    }

    /**
     * Retrieves the current list of persons for the case.
     *
     * @return The list of persons to be displayed in the list (sorted
     * alphabetically).
     */
    private Map<Person, List<DataSource>> getPersonListData() {
        Map<Person, List<DataSource>> personMapping = new HashMap<>();
        try {
            SleuthkitCase curCase = Case.getCurrentCaseThrows().getSleuthkitCase();
            List<Person> persons = curCase.getPersonManager().getPersons();
            List<DataSource> dataSources = curCase.getDataSources();

            if (dataSources != null) {
                for (DataSource ds : dataSources) {
                    List<DataSource> personDataSources = personMapping.computeIfAbsent(ds.getPerson(), (d) -> new ArrayList<>());
                    personDataSources.add(ds);
                }

            }

            if (persons != null) {
                for (Person person : persons) {
                    personMapping.putIfAbsent(person, Collections.emptyList());
                }
            }

        } catch (TskCoreException | NoCurrentCaseException ex) {
            logger.log(Level.WARNING, "There was an error while fetching persons for current case.", ex);
        }

        return personMapping;
    }

    /**
     * Refreshes component's enabled state and displayed person data.
     */
    private void refreshComponents() {
        PersonListItem selectedItem = personList.getSelectedValue();
        Person selectedPerson = selectedItem == null ? null : selectedItem.getPerson();
        List<DataSource> dataSources = selectedItem == null ? null : selectedItem.getDataSources();
        this.editButton.setEnabled(selectedPerson != null);
        this.deleteButton.setEnabled(selectedPerson != null && CollectionUtils.isEmpty(dataSources));
        String nameTextFieldStr = selectedPerson != null && selectedPerson.getName() != null ? selectedPerson.getName() : "";
        this.personNameTextField.setText(nameTextFieldStr);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane managePersonsScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel managePersonsPanel = new javax.swing.JPanel();
        javax.swing.JScrollPane personListScrollPane = new javax.swing.JScrollPane();
        personList = new javax.swing.JList<>();
        javax.swing.JScrollPane personDescriptionScrollPane = new javax.swing.JScrollPane();
        personDescriptionTextArea = new javax.swing.JTextArea();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        javax.swing.JLabel personListLabel = new javax.swing.JLabel();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JLabel personNameLabel = new javax.swing.JLabel();
        personNameTextField = new javax.swing.JTextField();
        editButton = new javax.swing.JButton();
        javax.swing.JLabel personDetailsLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 450));

        managePersonsScrollPane.setMinimumSize(new java.awt.Dimension(600, 450));
        managePersonsScrollPane.setPreferredSize(new java.awt.Dimension(600, 450));

        managePersonsPanel.setPreferredSize(new java.awt.Dimension(527, 407));

        personList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        personListScrollPane.setViewportView(personList);

        personDescriptionTextArea.setEditable(false);
        personDescriptionTextArea.setBackground(new java.awt.Color(240, 240, 240));
        personDescriptionTextArea.setColumns(20);
        personDescriptionTextArea.setLineWrap(true);
        personDescriptionTextArea.setRows(3);
        personDescriptionTextArea.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.personDescriptionTextArea.text")); // NOI18N
        personDescriptionTextArea.setWrapStyleWord(true);
        personDescriptionScrollPane.setViewportView(personDescriptionTextArea);

        newButton.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.newButton.text")); // NOI18N
        newButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        newButton.setMaximumSize(new java.awt.Dimension(70, 23));
        newButton.setMinimumSize(new java.awt.Dimension(70, 23));
        newButton.setPreferredSize(new java.awt.Dimension(70, 23));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.deleteButton.text")); // NOI18N
        deleteButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        deleteButton.setMaximumSize(new java.awt.Dimension(70, 23));
        deleteButton.setMinimumSize(new java.awt.Dimension(70, 23));
        deleteButton.setPreferredSize(new java.awt.Dimension(70, 23));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        closeButton.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        personListLabel.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.personListLabel.text")); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        personNameLabel.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.personNameLabel.text")); // NOI18N

        personNameTextField.setEditable(false);

        editButton.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.editButton.text")); // NOI18N
        editButton.setMaximumSize(new java.awt.Dimension(70, 23));
        editButton.setMinimumSize(new java.awt.Dimension(70, 23));
        editButton.setPreferredSize(new java.awt.Dimension(70, 23));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        personDetailsLabel.setText(org.openide.util.NbBundle.getMessage(ManagePersonsDialog.class, "ManagePersonsDialog.personDetailsLabel.text")); // NOI18N

        javax.swing.GroupLayout managePersonsPanelLayout = new javax.swing.GroupLayout(managePersonsPanel);
        managePersonsPanel.setLayout(managePersonsPanelLayout);
        managePersonsPanelLayout.setHorizontalGroup(
            managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(managePersonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(personDescriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(personListLabel)
                    .addGroup(managePersonsPanelLayout.createSequentialGroup()
                        .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(personListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(managePersonsPanelLayout.createSequentialGroup()
                        .addGroup(managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(managePersonsPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(closeButton))
                            .addGroup(managePersonsPanelLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(personNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(personNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(managePersonsPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(personDetailsLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        managePersonsPanelLayout.setVerticalGroup(
            managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(managePersonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(managePersonsPanelLayout.createSequentialGroup()
                        .addComponent(personDetailsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(personNameLabel)
                            .addComponent(personNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addComponent(jSeparator1)
                    .addGroup(managePersonsPanelLayout.createSequentialGroup()
                        .addComponent(personDescriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(personListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(personListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(managePersonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sleuthkit/autopsy/datamodel/persons/Bundle"); // NOI18N
        newButton.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.newButton.text")); // NOI18N
        deleteButton.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.deleteButton.text")); // NOI18N
        closeButton.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.closeButton.text")); // NOI18N
        personListLabel.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.personListLabel.text")); // NOI18N
        personNameLabel.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.personNameLabel.text")); // NOI18N
        editButton.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.editButton.text")); // NOI18N
        personDetailsLabel.getAccessibleContext().setAccessibleName(bundle.getString("ManagePersonsDialog.personDetailsLabel.text")); // NOI18N

        managePersonsScrollPane.setViewportView(managePersonsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(managePersonsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(managePersonsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        addPerson();
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        PersonListItem listItem = this.personList.getSelectedValue();
        if (listItem != null && listItem.getPerson() != null) {
            deletePerson(listItem.getPerson());
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        PersonListItem listItem = this.personList.getSelectedValue();
        if (listItem != null && listItem.getPerson() != null) {
            editPerson(listItem.getPerson());
        }
    }//GEN-LAST:event_editButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JTextArea personDescriptionTextArea;
    private javax.swing.JList<org.sleuthkit.autopsy.datamodel.persons.ManagePersonsDialog.PersonListItem> personList;
    private javax.swing.JTextField personNameTextField;
    private javax.swing.JButton newButton;
    // End of variables declaration//GEN-END:variables
}
