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

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * The options panel controller for Bulk file set import / export.
 */
@Messages({
    "OptionsCategory_Name_BulkFileImportExport=Bulk File Set Import/Export",
    "OptionsCategory_Keywords_BulkFileImportExport=BulkFileSetImportExport",})
@OptionsPanelController.TopLevelRegistration(
        categoryName = "#OptionsCategory_Name_BulkFileImportExport",
        iconBase = "org/sleuthkit/autopsy/images/interesting_item_32x32.png",
        keywords = "#OptionsCategory_Keywords_BulkFileImportExport",
        keywordsCategory = "BulkFileImportExport",
        position = 99
)
public final class BulkFileOptionsPanelController extends OptionsPanelController {

    private BulkFileSettingsPanel panel;

    /**
     * Component should load its data here.
     */
    @Override
    public void update() {
        getPanel().load();
    }

    /**
     * This method is called when both the Ok and Apply buttons are pressed. It
     * applies to any of the panels that have been opened in the process of
     * using the options pane.
     */
    @Override
    public void applyChanges() {
        // panel changes happen within import functionality
    }

    /**
     * This method is called when the Cancel button is pressed. It applies to
     * any of the panels that have been opened in the process of using the
     * options pane.
     */
    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * Used to determine whether any changes have been made to this controller's
     * panel.
     *
     * @return Whether or not a change has been made.
     */
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // no action needs to take place
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // no action needs to take place
    }

    private BulkFileSettingsPanel getPanel() {
        if (panel == null) {
            panel = new BulkFileSettingsPanel();
        }
        return panel;
    }

    void changed() {
        // changed internally in panel.
    }

}
