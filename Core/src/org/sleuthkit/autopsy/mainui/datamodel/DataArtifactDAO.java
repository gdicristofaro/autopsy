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
package org.sleuthkit.autopsy.mainui.datamodel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.beans.PropertyChangeEvent;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sleuthkit.autopsy.coreutils.Logger;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.datamodel.EmailExtracted;
import org.sleuthkit.autopsy.ingest.ModuleDataEvent;
import org.sleuthkit.autopsy.mainui.datamodel.TreeResultsDTO.TreeItemDTO;
import org.sleuthkit.autopsy.mainui.nodes.DAOFetcher;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.DataArtifact;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * DAO for providing data about data artifacts to populate the results viewer.
 */
public class DataArtifactDAO extends BlackboardArtifactDAO {

    private static Logger logger = Logger.getLogger(DataArtifactDAO.class.getName());

    private static DataArtifactDAO instance = null;

    synchronized static DataArtifactDAO getInstance() {
        if (instance == null) {
            instance = new DataArtifactDAO();
        }

        return instance;
    }

    /**
     * @return The set of types that are not shown in the tree.
     */
    public static Set<BlackboardArtifact.Type> getIgnoredTreeTypes() {
        return BlackboardArtifactDAO.getIgnoredTreeTypes();
    }

    private final Cache<SearchParams<BlackboardArtifactSearchParam>, DataArtifactTableSearchResultsDTO> dataArtifactCache = CacheBuilder.newBuilder().maximumSize(1000).build();

    private DataArtifactTableSearchResultsDTO fetchDataArtifactsForTable(SearchParams<BlackboardArtifactSearchParam> cacheKey) throws NoCurrentCaseException, TskCoreException {

        SleuthkitCase skCase = getCase();
        Blackboard blackboard = skCase.getBlackboard();
        BlackboardArtifact.Type artType = cacheKey.getParamData().getArtifactType();

        String pagedWhereClause = getWhereClause(cacheKey);

        List<BlackboardArtifact> arts = new ArrayList<>();
        arts.addAll(blackboard.getDataArtifactsWhere(pagedWhereClause));
        blackboard.loadBlackboardAttributes(arts);

        long totalResultsCount = getTotalResultsCount(cacheKey, arts.size());

        TableData tableData = createTableData(artType, arts);
        return new DataArtifactTableSearchResultsDTO(artType, tableData.columnKeys, tableData.rows, cacheKey.getStartItem(), totalResultsCount);
    }

    @Override
    RowDTO createRow(BlackboardArtifact artifact, Content srcContent, Content linkedFile, boolean isTimelineSupported, List<Object> cellValues, long id) throws IllegalArgumentException {
        if (!(artifact instanceof DataArtifact)) {
            throw new IllegalArgumentException("Can not make row for artifact with ID: " + artifact.getId() + " - artifact must be a data artifact");
        }
        return new DataArtifactRowDTO((DataArtifact) artifact, srcContent, linkedFile, isTimelineSupported, cellValues, id);
    }

    public DataArtifactTableSearchResultsDTO getDataArtifactsForTable(DataArtifactSearchParam artifactKey, long startItem, Long maxCount, boolean hardRefresh) throws ExecutionException, IllegalArgumentException {
        BlackboardArtifact.Type artType = artifactKey.getArtifactType();

        if (artType == null || artType.getCategory() != BlackboardArtifact.Category.DATA_ARTIFACT
                || (artifactKey.getDataSourceId() != null && artifactKey.getDataSourceId() < 0)) {
            throw new IllegalArgumentException(MessageFormat.format("Illegal data.  "
                    + "Artifact type must be non-null and data artifact.  Data source id must be null or > 0.  "
                    + "Received artifact type: {0}; data source id: {1}", artType, artifactKey.getDataSourceId() == null ? "<null>" : artifactKey.getDataSourceId()));
        }

        SearchParams<BlackboardArtifactSearchParam> searchParams = new SearchParams<>(artifactKey, startItem, maxCount);
        if (hardRefresh) {
            this.dataArtifactCache.invalidate(searchParams);
        }

        return dataArtifactCache.get(searchParams, () -> fetchDataArtifactsForTable(searchParams));
    }

    public boolean isDataArtifactInvalidating(DataArtifactSearchParam key, ModuleDataEvent eventData) {
        return key.getArtifactType().equals(eventData.getBlackboardArtifactType());
    }

    public void dropDataArtifactCache() {
        dataArtifactCache.invalidateAll();
    }

    /**
     * Returns a search results dto containing rows of counts data.
     *
     * @param dataSourceId The data source object id for which the results
     *                     should be filtered or null if no data source
     *                     filtering.
     *
     * @return The results where rows are CountsRowDTO of
     *         DataArtifactSearchParam.
     *
     * @throws ExecutionException
     */
    public TreeResultsDTO<DataArtifactSearchParam> getDataArtifactCounts(Long dataSourceId) throws ExecutionException {
        try {
            // get row dto's sorted by display name
            Map<BlackboardArtifact.Type, Long> typeCounts = getCounts(BlackboardArtifact.Category.DATA_ARTIFACT, dataSourceId);
            List<TreeResultsDTO.TreeItemDTO<DataArtifactSearchParam>> treeItemRows = typeCounts.entrySet().stream()
                    .map(entry -> {
                        return new TreeResultsDTO.TreeItemDTO<>(
                                BlackboardArtifact.Category.DATA_ARTIFACT.name(),
                                new DataArtifactSearchParam(entry.getKey(), dataSourceId),
                                entry.getKey().getTypeID(),
                                entry.getKey().getDisplayName(),
                                entry.getValue());
                    })
                    .sorted(Comparator.comparing(countRow -> countRow.getDisplayName()))
                    .collect(Collectors.toList());

            // return results
            return new TreeResultsDTO<>(treeItemRows);

        } catch (NoCurrentCaseException | TskCoreException ex) {
            throw new ExecutionException("An error occurred while fetching data artifact counts.", ex);
        }
    }

    public TreeResultsDTO<AccountSearchParams> getAccountsCounts(Long dataSourceId) throws ExecutionException {
        String query = "SELECT res.account_type AS account_type, MIN(res.account_display_name) AS account_display_name, COUNT(*) AS count\n"
                + "FROM (\n"
                + "  SELECT MIN(account_types.type_name) AS account_type, MIN(account_types.display_name) AS account_display_name\n"
                + "  FROM blackboard_artifacts\n"
                + "  LEFT JOIN blackboard_attributes ON blackboard_artifacts.artifact_id = blackboard_attributes.artifact_id\n"
                + "  LEFT JOIN account_types ON blackboard_artifacts.value_text = account_types.type_name\n"
                + "  WHERE blackboard_artifacts.artifact_type_id = " + BlackboardArtifact.Type.TSK_ACCOUNT.getTypeID() + "\n"
                + "  AND blackboard_attributes.attribute_type_id = " + BlackboardAttribute.Type.TSK_ACCOUNT_TYPE.getTypeID() + "\n"
                + (dataSourceId != null && dataSourceId > 0 ? "  AND blackboard_artifacts.data_source_obj_id = " + dataSourceId + " " : " ") + "\n"
                + "  -- group by artifact_id to ensure only one account type per artifact\n"
                + "  GROUP BY blackboard_artifacts.artifact_id\n"
                + ") res\n"
                + "GROUP BY res.account_type\n"
                + "ORDER BY MIN(res.account_display_name)";

        List<TreeItemDTO<AccountSearchParams>> accountParams = new ArrayList<>();
        try {
            getCase().getCaseDbAccessManager().select(query, (resultSet) -> {
                try {
                    while (resultSet.next()) {
                        String accountType = resultSet.getString("account_type");
                        String accountDisplayName = resultSet.getString("account_display_name");
                        long count = resultSet.getLong("count");
                        accountParams.add(new TreeItemDTO<>(
                                accountType,
                                new AccountSearchParams(accountType, dataSourceId),
                                accountType,
                                accountDisplayName,
                                count));
                    }
                } catch (SQLException ex) {
                    logger.log(Level.WARNING, "An error occurred while fetching artifact type counts.", ex);
                }
            });

            // return results
            return new TreeResultsDTO<>(accountParams);

        } catch (NoCurrentCaseException | TskCoreException ex) {
            throw new ExecutionException("An error occurred while fetching data artifact counts.", ex);
        }
    }

    
    public TreeResultsDTO<CreditCardBinParams> getCreditCardBinCounts(Long dataSourceId) throws ExecutionException {
        
    }
    
    public TreeResultsDTO<CreditCardNumberParams> getCreditCardNumberCounts(Long dataSourceId, String bin) throws ExecutionException {
        
        
    }
    
    public TreeResultsDTO<CreditCardByFileParams> getCreditCardByFileCounts(Long dataSourceId) {
        
    }
    
//        switch (skCase.getDatabaseType()) {
//        case POSTGRESQL:
//            mimeType = "SPLIT_PART(mime_type, '/', 1)";
//            break;
//        case SQLITE:
//            mimeType = "SUBSTR(mime_type, 0, instr(mime_type, '/'))";
//            break;
//        default:
//            throw new IllegalArgumentException("Unknown database type: " + skCase.getDatabaseType());
//    }
                            
    
        /**
     * Parse the path of the email msg to get the account name and folder in
     * which the email is contained.
     *
     * @param path - the TSK_PATH to the email msg
     *
     * @return a map containg the account and folder which the email is stored
     *         in
     */
//    public static final Map<String, String> parsePath(String path) {
//        Map<String, String> parsed = new HashMap<>();
//        String[] split = path == null ? new String[0] : path.split(MAIL_PATH_SEPARATOR);
//        if (split.length < 4) {
//            parsed.put(MAIL_ACCOUNT, NbBundle.getMessage(EmailExtracted.class, "EmailExtracted.defaultAcct.text"));
//            parsed.put(MAIL_FOLDER, NbBundle.getMessage(EmailExtracted.class, "EmailExtracted.defaultFolder.text"));
//            return parsed;
//        }
//        parsed.put(MAIL_ACCOUNT, split[2]);
//        parsed.put(MAIL_FOLDER, split[3]);
//        return parsed;
//    }
//    private static final String MAIL_PATH_SEPARATOR = "/";
    
//    public TreeResultsDTO<EmailSearchParams> getEmailCounts(EmailSearchParams searchParams) throws ExecutionException {
//        private final Map<String, Map<String, List<Long>>> accounts = new LinkedHashMap<>();
//
//        EmailResults() {
//            update();
//        }
//
//        public Set<String> getAccounts() {
//            synchronized (accounts) {
//                return accounts.keySet();
//            }
//        }
//
//        public Set<String> getFolders(String account) {
//            synchronized (accounts) {
//                return accounts.get(account).keySet();
//            }
//        }
//
//        public List<Long> getArtifactIds(String account, String folder) {
//            synchronized (accounts) {
//                return accounts.get(account).get(folder);
//            }
//        }
        
        
//        String query = "SELECT \n"
//                + "	art.artifact_obj_id AS artifact_obj_id,\n"
//                + "	(SELECT value_text FROM blackboard_attributes attr\n"
//                + "	WHERE attr.artifact_id = art.artifact_id AND attr.attribute_type_id = " + pathAttrId + "\n"
//                + "	LIMIT 1) AS value_text\n"
//                + "FROM \n"
//                + "	blackboard_artifacts art\n"
//                + "	WHERE art.artifact_type_id = " + emailArtifactId + "\n"
//                + ((filteringDSObjId > 0) ? "	AND art.data_source_obj_id = " + filteringDSObjId : "");
//    }

    /*
     * Handles fetching and paging of data artifacts.
     */
    public static class DataArtifactFetcher extends DAOFetcher<DataArtifactSearchParam> {

        /**
         * Main constructor.
         *
         * @param params Parameters to handle fetching of data.
         */
        public DataArtifactFetcher(DataArtifactSearchParam params) {
            super(params);
        }

        @Override
        public SearchResultsDTO getSearchResults(int pageSize, int pageIdx, boolean hardRefresh) throws ExecutionException {
            return MainDAO.getInstance().getDataArtifactsDAO().getDataArtifactsForTable(this.getParameters(), pageIdx * pageSize, (long) pageSize, hardRefresh);
        }

        @Override
        public boolean isRefreshRequired(PropertyChangeEvent evt) {
            ModuleDataEvent dataEvent = this.getModuleDataFromEvt(evt);
            if (dataEvent == null) {
                return false;
            }

            return MainDAO.getInstance().getDataArtifactsDAO().isDataArtifactInvalidating(this.getParameters(), dataEvent);
        }
    }
}
