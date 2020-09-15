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
package org.sleuthkit.autopsy.datasourcesummary.ui;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.datasourcesummary.datamodel.IngestModuleCheckUtil.NotIngestedWithModuleException;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchResult.ResultType;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchResult;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchWorker;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchWorker.DataFetchComponents;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.EventUpdateHandler;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.LoadableComponent;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.SwingWorkerSequentialExecutor;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.UpdateGovernor;
import org.sleuthkit.datamodel.DataSource;

/**
 * Base class from which other tabs in data source summary derive.
 */
@Messages({
    "# {0} - module name",
    "BaseDataSourceSummaryPanel_defaultNotIngestMessage=The {0} Ingest Module has not been run on this datasource."
})
abstract class BaseDataSourceSummaryPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final SwingWorkerSequentialExecutor executor = new SwingWorkerSequentialExecutor();
    private final EventUpdateHandler updateHandler;

    private DataSource dataSource;

    /**
     * Main constructor.
     *
     * @param governors The items governing when this panel should receive
     *                  updates.
     */
    protected BaseDataSourceSummaryPanel(UpdateGovernor... governors) {
        this.updateHandler = new EventUpdateHandler(this::onRefresh, governors);
        this.updateHandler.register();
    }

    /**
     * Sets datasource to visualize in the panel.
     *
     * @param dataSource The datasource to use in this panel.
     */
    synchronized void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.executor.cancelRunning();
        onNewDataSource(this.dataSource);
    }

    /**
     * Submits the following swing workers for execution in sequential order. If
     * there are any previous workers, those workers are cancelled.
     *
     * @param workers The workers to submit for execution.
     */
    protected void submit(List<? extends SwingWorker<?, ?>> workers) {
        executor.submit(workers);
    }

    /**
     * When a data source is updated this function is triggered.
     *
     * @param dataSource The data source.
     */
    synchronized void onRefresh() {
        // don't update the data source if it is already trying to load
        if (!executor.isRunning()) {
            // trigger on new data source with the current data source
            fetchInformation(this.dataSource);
        }
    }

    /**
     * Action that is called when information needs to be retrieved (on refresh
     * or on new data source).
     *
     * @param dataSource The datasource to fetch information about.
     */
    protected abstract void fetchInformation(DataSource dataSource);

    /**
     * Utility method to be called when solely updating information (not showing
     * a loading screen) that creates swing workers from the data source
     * argument and data fetch components and then submits them to run.
     *
     * @param dataFetchComponents The components to be run.
     * @param dataSource          The data source argument.
     */
    protected void fetchInformation(List<DataFetchComponents<DataSource, ?>> dataFetchComponents, DataSource dataSource) {
        // create swing workers to run for each loadable item
        List<DataFetchWorker<?, ?>> workers = dataFetchComponents
                .stream()
                .map((components) -> new DataFetchWorker<>(components, dataSource))
                .collect(Collectors.toList());

        // submit swing workers to run
        if (workers.size() > 0) {
            submit(workers);
        }
    }

    /**
     * When a new dataSource is added, this method is called.
     *
     * @param dataSource The new dataSource.
     */
    protected abstract void onNewDataSource(DataSource dataSource);

    /**
     * Get default message when there is a NotIngestedWithModuleException.
     *
     * @param exception The moduleName.
     *
     * @return Message specifying that the ingest module was not run.
     */
    protected String getDefaultNoIngestMessage(String moduleName) {
        return Bundle.BaseDataSourceSummaryPanel_defaultNotIngestMessage(moduleName);
    }

    /**
     * Utility method that will handle NotIngestedWithModuleException's by
     * showing the message from getDefaultNoIngestMessage. Otherwise,
     * component.showDataFetchResult will be called.
     *
     * @param component The component.
     * @param result    The result of data fetching including the possible
     *                  exception.
     */
    protected <T> void handleIngestModuleResult(LoadableComponent<T> component, DataFetchResult<T> result) {
        if (result.getResultType() == ResultType.ERROR
                && result.getException() instanceof NotIngestedWithModuleException) {

            String moduleDisplayName = ((NotIngestedWithModuleException) result.getException()).getModuleDisplayName();
            component.showMessage(getDefaultNoIngestMessage(moduleDisplayName));
        } else {
            component.showDataFetchResult(result);
        }
    }

    /**
     * Utility method that will handle NotIngestedWithModuleException's by
     * showing the message from getDefaultNoIngestMessage. Otherwise,
     * component.showDataFetchResult will be called.
     *
     * @param component     The component.
     * @param result        The result of data fetching including the possible
     *                      exception.
     * @param onIngestError Message to be shown if there is an ingest error.
     * @param onError       Message to be shown if there is a general error of
     *                      any other sort.
     * @param onEmpty       Message to be shown if there is no data.
     */
    protected <T> void handleIngestModuleResult(LoadableComponent<T> component, DataFetchResult<T> result, String onIngestError, String onError, String onEmpty) {
        if (result.getResultType() == ResultType.ERROR
                && result.getException() instanceof NotIngestedWithModuleException) {

            component.showMessage(onIngestError);
        } else {
            component.showDataFetchResult(result, onError, onEmpty);
        }
    }

    /**
     * Utility method that shows a loading screen with loadable components,
     * create swing workers from the datafetch components and data source
     * argument and submits them to be executed.
     *
     * @param dataFetchComponents The components to register.
     * @param loadableComponents  The components to set to a loading screen.
     * @param dataSource          The data source argument.
     */
    protected void onNewDataSource(
            List<DataFetchComponents<DataSource, ?>> dataFetchComponents,
            List<? extends LoadableComponent<?>> loadableComponents,
            DataSource dataSource) {
        // if no data source is present or the case is not open,
        // set results for tables to null.
        if (dataSource == null || !Case.isCaseOpen()) {
            dataFetchComponents.forEach((item) -> item.getResultHandler()
                    .accept(DataFetchResult.getSuccessResult(null)));

        } else {
            // set tables to display loading screen
            loadableComponents.forEach((table) -> table.showDefaultLoadingMessage());

            fetchInformation(dataSource);
        }
    }
}
