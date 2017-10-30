package at.sunplugged.z600.common.execution.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;

@Component
public class StandardThreadPoolServiceImpl implements StandardThreadPoolService {

    private static final int THREAD_POOL_SIZE = 25;

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;

    public StandardThreadPoolServiceImpl() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        scheduledExecutorService = Executors.newScheduledThreadPool(10);
    }

    @Deactivate
    protected void deactivate() {
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        scheduledExecutorService.shutdown();
    }

    @Override
    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }

    @Override
    public Future<?> submit(Callable<?> callable) {
        return executorService.submit(callable);
    }

    @Override
    public ScheduledFuture<?> timedExecute(Runnable runnable, long delay, TimeUnit unit) {

        return scheduledExecutorService.schedule(runnable, delay, unit);

    }

    @Override
    public ScheduledFuture<?> timedPeriodicExecute(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }

}
