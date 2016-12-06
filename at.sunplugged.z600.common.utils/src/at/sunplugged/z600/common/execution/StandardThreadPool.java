package at.sunplugged.z600.common.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StandardThreadPool {

    private static final int THREAD_POOL_SIZE = 5;

    private static StandardThreadPool instance = new StandardThreadPool();

    private ExecutorService executorService;

    public StandardThreadPool() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public static StandardThreadPool getInstance() {
        return instance;
    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }

}
