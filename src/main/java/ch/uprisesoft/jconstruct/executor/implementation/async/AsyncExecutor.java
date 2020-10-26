/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.async;

import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.executor.implementation.sync.SshExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author rma
 */
public class AsyncExecutor implements Executor {

    private Executor syncExecutor;
    private ExecutorService executor;
    private Future tasks;

    public AsyncExecutor(Executor syncExecutor) {
        this(syncExecutor, 1);
    }

    public AsyncExecutor(Executor syncExecutor, Integer threads) {
        this.syncExecutor = syncExecutor;
        executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public void runCommands() {
        tasks = executor.submit(() -> {
            this.syncExecutor.runCommands();
        });
    }

    public Boolean isDone() {
        if (tasks != null) {
            return tasks.isDone();
        }
        return false;
    }

}
