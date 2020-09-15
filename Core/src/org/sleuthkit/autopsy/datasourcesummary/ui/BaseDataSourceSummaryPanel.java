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
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.datasourcesummary.datamodel.IngestModuleCheckUtil.NotIngestedWithModuleException;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchResult;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchResult.ResultType;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.LoadableComponent;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.SwingWorkerSequentialExecutor;
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
    private DataSource dataSource;

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
     * When a new dataSource is added, this method is called.
     *
     * @param dataSource The new dataSource.
     */
    protected abstract void onNewDataSource(DataSource dataSource);

    protected String getDefaultNoIngestMessage(NotIngestedWithModuleException exception) {
        return Bundle.BaseDataSourceSummaryPanel_defaultNotIngestMessage(exception.getModuleDisplayName());
    }

    /**
     * 
     * @param <T>
     * @param component
     * @param result 
     */
    protected <T> void handleIngestModuleResult(LoadableComponent<T> component, DataFetchResult<T> result) {
        if (result.getResultType() == ResultType.ERROR
                && result.getException() instanceof NotIngestedWithModuleException) {

            component.showMessage(getDefaultNoIngestMessage((NotIngestedWithModuleException) result.getException()));
        } else {
            component.showDataFetchResult(result);
        }
    }

    protected <T> void handleIngestModuleResult(LoadableComponent<T> component, DataFetchResult<T> result, String onIngestError, String onError, String onEmpty) {
        if (result.getResultType() == ResultType.ERROR
                && result.getException() instanceof NotIngestedWithModuleException) {

            component.showMessage(onIngestError);
        } else {
            component.showDataFetchResult(result, onError, onEmpty);
        }
    }
}
