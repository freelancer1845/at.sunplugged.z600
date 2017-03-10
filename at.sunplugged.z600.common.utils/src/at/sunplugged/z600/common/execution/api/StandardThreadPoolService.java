package at.sunplugged.z600.common.execution.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface StandardThreadPoolService {

    public void execute(Runnable runnable);

    public Future<?> submit(Callable<?> callable);

    public Future<?> submit(Runnable runnable);

    /**
     * Executes the given runnable once after the delay.
     * 
     * @param runnable
     * @param delay
     * @param unit
     * @return {@linkplain ScheduledFuture} that can be used to suppress
     *         execution.
     */
    public ScheduledFuture<?> timedExecute(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Schedules a periodic execution. The delay is fixed (i. e. the start
     * points are fixed intervals). To cancel use the returned ScheduledFuture.
     * 
     * @param runnable
     * @param initialDelay
     * @param period
     * @param unit
     * @return
     */
    public ScheduledFuture<?> timedPeriodicExecute(Runnable runnable, long initialDelay, long period, TimeUnit unit);
}
