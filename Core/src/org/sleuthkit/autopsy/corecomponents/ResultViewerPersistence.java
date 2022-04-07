/*
 * Autopsy Forensic Browser
 *
 * Copyright 2017-2021 Basis Technology Corp.
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
package org.sleuthkit.autopsy.corecomponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SortOrder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;
import org.sleuthkit.autopsy.corecomponentinterfaces.ColumnSort;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.mainui.datamodel.SearchResultsDTO;

final class ResultViewerPersistence {

    private static final String SORT_RANK_SUFFIX = ".sortRank";
    private static final String SORT_ORDER_SUFFIX = ".sortOrder";

    private static final Logger logger = Logger.getLogger(ResultViewerPersistence.class.getName());

    private ResultViewerPersistence() {
    }

    /**
     * Returns the column sort order.
     *
     * @param signature The signature to use when looking up keys in the
     *                  properties.
     *
     * @return The list of columns to be sorted in order of highest to lowest
     *         priority or empty.
     */
    static List<ColumnSort> getColumnSorting(String signature) {
        if (signature == null) {
            return Collections.emptyList();
        }

        final Preferences preferences = NbPreferences.forModule(DataResultViewerTable.class);
        if (preferences == null) {
            return Collections.emptyList();
        }

        String prefix = stripNonAlphanumeric(signature) + ".";

        Map<String, Boolean> isAscendingMap = new HashMap<>();
        Map<String, Integer> sortOrderMap = new HashMap<>();
        try {
            for (String key : preferences.keys()) {
                if (key.startsWith(prefix)) {
                    String keySubstr = key.substring(prefix.length());
                    if (keySubstr.endsWith(SORT_RANK_SUFFIX)) {
                        String column = keySubstr.substring(0, keySubstr.length() - SORT_RANK_SUFFIX.length());
                        int sortOrder = preferences.getInt(key, -1);
                        if (sortOrder >= 0) {
                            sortOrderMap.put(column, sortOrder);
                        }
                    } else if (keySubstr.endsWith(SORT_ORDER_SUFFIX)) {
                        String column = keySubstr.substring(0, keySubstr.length() - SORT_RANK_SUFFIX.length());
                        isAscendingMap.put(column, preferences.getBoolean(key, true));
                    }
                }
            }

        } catch (BackingStoreException ex) {
            logger.log(Level.WARNING, "There was an error fetching column sorting for signature: " + signature, ex);
            return Collections.emptyList();
        }

        List<ColumnSort> toRet = new ArrayList<>();
        for (Entry<String, Integer> sortOrderEntry : sortOrderMap.entrySet()) {
            String column = sortOrderEntry.getKey();
            if (column == null) {
                continue;
            }

            Integer sortOrder = sortOrderEntry.getValue();
            if (sortOrder == null) {
                continue;
            }

            Boolean isAsc = isAscendingMap.get(column);
            if (isAsc == null) {
                isAsc = true;
            }

            toRet.add(new ColumnSort(isAsc, column, sortOrder));
        }

        Collections.sort(toRet, Comparator.comparing(ColumnSort::getSortRank));
        return toRet;
    }

    /**
     * Gets a key for the given node and a property of its child nodes to store
     * the column position into a preference file.
     *
     * @param node     The node whose type will be used to generate the key
     * @param propName The property used to generate the key.
     *
     * @return A generated key for the preference file
     */
    static String getColumnPositionKey(TableFilterNode node, String propName) {
        return getColumnKeyBase(node.getColumnOrderKey(), propName) + ".column";
    }
    
    static String getColumnPositionKey(SearchResultsDTO searchResult, String propName) {
        return getColumnKeyBase(searchResult.getSignature(), propName) + ".column";
    }

    /**
     * Gets a key for the given node and a property of its child nodes to store
     * the sort order (ascending/descending) into a preference file.
     *
     * @param node     The node whose type will be used to generate the key
     * @param propName The property used to generate the key.
     *
     * @return A generated key for the preference file
     */
    static String getColumnSortOrderKey(TableFilterNode node, String propName) {
        return getColumnKeyBase(node.getColumnOrderKey(), propName) + SORT_ORDER_SUFFIX;
    }
    
    /**
     * Provides a properties key for the sort order of a property.
     * @param searchResult The search result.
     * @param propName The property name.
     * @return The properties key for the sort order.
     */
    static String getColumnSortOrderKey(SearchResultsDTO searchResult, String propName) {
        return getColumnSortOrderKey(searchResult.getSignature(), propName);
    }

    /**
     * Provides a properties key for the sort order of a property.
     * @param signature The search result signature.
     * @param propName The property name.
     * @return The properties key for the sort order.
     */    
    static String getColumnSortOrderKey(String signature, String propName) {
        return getColumnKeyBase(signature, propName) + SORT_ORDER_SUFFIX;
    }

    /**
     * Gets a key for the given node and a property of its child nodes to store
     * the sort rank into a preference file.
     *
     * @param node     The node whose type will be used to generate the key
     * @param propName The property used to generate the key.
     *
     * @return A generated key for the preference file
     */
    static String getColumnSortRankKey(TableFilterNode node, String propName) {
        return getColumnKeyBase(node.getColumnOrderKey(), propName) + SORT_RANK_SUFFIX;
    }
    
    /**
     * Provides a column sort rank key.
     * @param searchResult The search result.
     * @param propName The property name.
     * @return The properties key to use.
     */
    static String getColumnSortRankKey(SearchResultsDTO searchResult, String propName) {
        return getColumnSortRankKey(searchResult.getSignature(), propName);
    }
    
    /**
     * Provides a column sort rank key.
     * @param searchResult The search result.
     * @param propName The property name.
     * @return The properties key to use.
     */
    static String getColumnSortRankKey(String signature, String propName) {
        return getColumnKeyBase(signature, propName) + SORT_RANK_SUFFIX;
    }

    /**
     * Gets a key for the given node and a property of its child nodes to store
     * the visibility into a preference file.
     *
     * @param node     The node whose type will be used to generate the key
     * @param propName The property used to generate the key.
     *
     * @return A generated key for the preference file
     */
    static String getColumnHiddenKey(TableFilterNode node, String propName) {
        return getColumnKeyBase(node.getColumnOrderKey(), propName) + ".hidden";
    }
    
    static String getColumnHiddenKey(SearchResultsDTO searchResult, String propName) {
        return getColumnKeyBase(searchResult.getSignature(), propName) + ".hidden";
    }
    
    private static String getColumnKeyBase(String signature, String propName) {
        return stripNonAlphanumeric(signature) + "." + stripNonAlphanumeric(propName);
    }

    private static String stripNonAlphanumeric(String str) {
        return str.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * Gets property set properties from all children and, recursively,
     * subchildren of a Node. Note: won't work out the box for lazy load - you
     * need to set all children props for the parent by hand
     *
     * @param node    Node with at least one child to get properties from
     * @param maxRows max number of rows to retrieve properties for (can be used
     *                for memory optimization)
     *
     * @return A List of properties discovered on all the children and recursive
     *         subchildren.
     */
    static List<Node.Property<?>> getAllChildProperties(Node node, int maxRows) {
        // This is a set because we add properties of up to 100 child nodes, and we want unique properties
        Set<Node.Property<?>> propertiesAcc = new LinkedHashSet<>();
        getAllChildPropertiesHelper(node, maxRows, propertiesAcc);
        return new ArrayList<>(propertiesAcc);
    }

    /**
     * Gets property set properties from all children and, recursively,
     * subchildren of a Node. Note: won't work out the box for lazy load - you
     * need to set all children props for the parent by hand
     *
     * @param node          Node with at least one child to get properties from
     * @param maxRows       max number of rows to retrieve properties for (can
     *                      be used for memory optimization)
     * @param propertiesAcc Accumulator for discovered properties.
     */
    static private void getAllChildPropertiesHelper(Node node, int maxRows, Set<Node.Property<?>> propertiesAcc) {
        Children children = node.getChildren();
        int childCount = 0;
        for (Node child : children.getNodes(true)) {
            childCount++;
            if (childCount > maxRows) {
                return;
            }
            for (Node.PropertySet ps : child.getPropertySets()) {
                final Node.Property<?>[] props = ps.getProperties();
                final int propsNum = props.length;
                for (int j = 0; j < propsNum; ++j) {
                    propertiesAcc.add(props[j]);
                }
            }
            getAllChildPropertiesHelper(child, maxRows, propertiesAcc);
        }
    }

    /**
     * Load the persisted sort criteria for nodes of the same type as the given
     * node.
     *
     * @param node The node whose type will be used to load the persisted sort
     *             criteria.
     *
     * @return A map from sort rank to sort criterion, where rank 1 means that
     *         this is the most important sort criteria, 2 means second etc.
     */
    static List< SortCriterion> loadSortCriteria(TableFilterNode node) {
        List<Node.Property<?>> availableProperties = ResultViewerPersistence.getAllChildProperties(node, 100);
        final Preferences preferences = NbPreferences.forModule(DataResultViewerTable.class);
        java.util.SortedMap<Integer, SortCriterion> criteriaMap = new TreeMap<>();
        availableProperties.forEach(prop -> {
            //if the sort rank is undefined, it will be defaulted to 0 => unsorted.
            Integer sortRank = preferences.getInt(ResultViewerPersistence.getColumnSortRankKey(node, prop.getName()), 0);

            if (sortRank != 0) {
                final SortOrder sortOrder = preferences.getBoolean(ResultViewerPersistence.getColumnSortOrderKey(node, prop.getName()), true)
                        ? SortOrder.ASCENDING
                        : SortOrder.DESCENDING;
                final SortCriterion sortCriterion = new SortCriterion(prop, sortOrder, sortRank);
                criteriaMap.put(sortRank, sortCriterion);
            }
        });
        return new ArrayList<>(criteriaMap.values());
    }

    /**
     * Encapsulate the property, sort order, and sort rank into one data bag.
     */
    static class SortCriterion {

        private final Node.Property<?> prop;
        private final SortOrder order;
        private final int rank;

        int getSortRank() {
            return rank;
        }

        Node.Property<?> getProperty() {
            return prop;
        }

        SortOrder getSortOrder() {
            return order;
        }

        SortCriterion(Node.Property<?> prop, SortOrder order, int rank) {
            this.prop = prop;
            this.order = order;
            this.rank = rank;
        }

        @Override
        public String toString() {
            return getSortRank() + ". "
                    + getProperty().getName() + " "
                    + (getSortOrder() == SortOrder.ASCENDING
                            ? "\u25B2" // /\
                            : "\u25BC");// \/
        }
    }
}
