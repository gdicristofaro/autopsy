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
package org.sleuthkit.autopsy.guiutils.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.swing.SwingWorker;

/**
 * Runs a list of swing workers in sequential order. Also, provides the ability
 * to reset or cancel a run.
 *
 * Based on:
 * https://stackoverflow.com/questions/31580805/java-swingworker-one-after-another-and-update-gui
 */
public class SwingWorkerSequentialRunner {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private List<? extends SwingWorker<?, ?>> workers = Collections.emptyList();
    private List<Future<?>> futures = Collections.emptyList();

    /**
     * Cancels currently running operations and starts running the new list of
     * swing workers.
     *
     * @param submittedWorkers The list of submitted swing workers.
     */
    public synchronized void resetLoad(List<? extends SwingWorker<?, ?>> submittedWorkers) {
        cancelRunning();
        if (submittedWorkers == null) {
            return;
        }

        this.workers = Collections.unmodifiableList(submittedWorkers);
        this.futures = this.workers.stream()
                .map((w) -> executorService.submit(w))
                .collect(Collectors.toList());
    }

    /**
     * Cancels currently running items.
     */
    public synchronized void cancelRunning() {
        futures.forEach((f) -> f.cancel(true));
        workers = Collections.emptyList();
        futures = Collections.emptyList();
    }
}