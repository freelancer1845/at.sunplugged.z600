package at.sunplugged.z600.common.execution.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.osgi.service.component.annotations.Component;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;

@Component
public class StandardThreadPoolServiceImpl implements StandardThreadPoolService {

    private static final int THREAD_POOL_SIZE = 25;

    private ExecutorService executorService;

    public StandardThreadPoolServiceImpl() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
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

}
