package at.sunplugged.z600.common.execution.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface StandardThreadPoolService {

    public void execute(Runnable runnable);

    public Future<?> submit(Callable<?> callable);

    public Future<?> submit(Runnable runnable);

}
