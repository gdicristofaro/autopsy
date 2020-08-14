/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.casemodule.datasourcesummary;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import org.sleuthkit.autopsy.coreutils.Logger;

    


/**
 *
 * @author gregd
 */
class DataFetchWorker<A, R> extends SwingWorker<R, Void> {
    static class DataFetchExecutor<R1,A1> {
        private final DataProcessor<A1,R1> processor;
        private final Consumer<R1> resultHandler;

        private DataFetchWorker<A1, R1> worker = null;

        DataFetchExecutor(DataProcessor<A1, R1> processor, Consumer<R1> resultHandler) {
            this.processor = processor;
            this.resultHandler = resultHandler;
        }
        
        private boolean isWorkerRunning() {
            return worker != null && (!worker.isDone() || worker.isCancelled());
        }
        
        synchronized void setArgsAndRun(A1 args) {
            cancelRunning();
            worker = new DataFetchWorker<>(args, processor, resultHandler);
        }
        
        synchronized void cancelRunning() {
            if (isWorkerRunning()) {
                worker.cancel(true);
            }
        }
    }
    
    
    @FunctionalInterface
    interface DataProcessor<I,O> {    
        static <I1,O1> DataProcessor<I1,O1> wrap(Function<I1,O1> toBeWrapped) {
            return new DataProcessor<I1,O1>() {
                @Override
                public O1 process(I1 input) throws InterruptedException {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    O1 output = toBeWrapped.apply(input);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    return output;
                }
            };
        }

        O process(I input) throws InterruptedException;
    }
      
    private static final Logger logger = Logger.getLogger(DataFetchWorker.class.getName());
    
    private final A args;
    private final DataProcessor<A,R> processor;
    private final Consumer<R> resultHandler;

    public DataFetchWorker(A args, DataProcessor<A, R> processor, Consumer<R> resultHandler) {
        this.args = args;
        this.processor = processor;
        this.resultHandler = resultHandler;
    }
    
    
    
    @Override
    protected R doInBackground() throws Exception {
        if (Thread.interrupted() || isCancelled()) {
            throw new InterruptedException();
        }
        
        R result = processor.process(args);
        
        if (Thread.interrupted() || isCancelled()) {
            throw new InterruptedException();
        }
        
        return result;
    }

    @Override
    protected void done() {               
        R result = null;
        try {
            result = get();
        } catch (InterruptedException ignored) {
            // if cancelled, just return
            return;
        } catch (ExecutionException ex) {
            logger.log(Level.WARNING, "There was an error while fetching results.", ex);
            return;
        }

        if (Thread.interrupted() || isCancelled()) {
            return;
        }
        
        resultHandler.accept(result);
    }
}
