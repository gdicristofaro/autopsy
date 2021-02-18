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
package org.sleuthkit.autopsy.datamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.Host;
import org.sleuthkit.datamodel.Person;
import org.sleuthkit.datamodel.PersonManager;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * A node to be displayed in the UI tree for a person and persons grouped in
 * this host.
 */
@NbBundle.Messages(value = {"PersonNode_unknownPersonNode_title=Unknown Persons"})
public class PersonGroupingNode extends DisplayableItemNode {
    private static final String ICON_PATH = "org/sleuthkit/autopsy/images/person.png";

    /**
     * Responsible for creating the host children of this person.
     */
    private static class PersonChildren extends ChildFactory.Detachable<HostGrouping> {

        private static final Logger logger = Logger.getLogger(PersonChildren.class.getName());

        private final Person person;

        /**
         * Main constructor.
         *
         * @param person The person record.
         */
        PersonChildren(Person person) {
            this.person = person;
        }

        /**
         * Listener for handling DATA_SOURCE_ADDED and DATA_SOURCE_DELETED
         * events.
         */
        private final PropertyChangeListener pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String eventType = evt.getPropertyName();
                if (eventType.equals(Case.Events.DATA_SOURCE_ADDED.toString())
                        || eventType.equals(Case.Events.DATA_SOURCE_DELETED.toString())) {
                    refresh(true);
                }
            }
        };

        @Override
        protected void addNotify() {
            Case.addEventTypeSubscriber(EnumSet.of(Case.Events.DATA_SOURCE_ADDED), pcl);
        }

        @Override
        protected void removeNotify() {
            Case.removeEventTypeSubscriber(EnumSet.of(Case.Events.DATA_SOURCE_ADDED), pcl);
        }

        @Override
        protected HostNode createNodeForKey(HostGrouping key) {
            return key == null ? null : new HostNode(key);
        }

        @Override
        protected boolean createKeys(List<HostGrouping> toPopulate) {
            Set<Host> hosts = null;
            //try {
                hosts = new HashSet<>();
                // NOTE: This code will be used when person manager exists
                // hosts = Case.getCurrentCaseThrows().getSleuthkitCase().getPersonManager().getHostsForPerson(person);
//            } catch (TskCoreException ex) {
//                String personName = person == null || person.getName() == null ? "<unknown>" : person.getName();
//                logger.log(Level.WARNING, String.format("Unable to get data sources for host: %s", personName), ex);
//            }

            if (hosts != null) {
                hosts.stream()
                        .map(HostGrouping::new)
                        .sorted()
                        .forEach(toPopulate::add);
            }

            return true;
        }
    }

    /**
     * Main constructor.
     *
     * @param person The person record to be represented.
     */
    PersonGroupingNode(Person person) {
        super(Children.create(new PersonChildren(person), false), person == null ? null : Lookups.singleton(person));

        String safeName = (person == null || person.getName() == null)
                ? Bundle.PersonNode_unknownPersonNode_title()
                : person.getName();

        super.setName(safeName);
        super.setDisplayName(safeName);
        this.setIconBaseWithExtension(ICON_PATH);
    }

    @Override
    public boolean isLeafTypeNode() {
        return false;
    }

    @Override
    public String getItemType() {
        return getClass().getName();
    }

    @Override
    public <T> T accept(DisplayableItemNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
