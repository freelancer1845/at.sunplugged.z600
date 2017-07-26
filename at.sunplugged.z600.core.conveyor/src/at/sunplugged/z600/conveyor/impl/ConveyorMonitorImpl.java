package at.sunplugged.z600.conveyor.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor;

@Component(immediate = true)
public class ConveyorMonitorImpl implements ConveyorMonitor {

    @Reference(policy = ReferencePolicy.STATIC)
    private LogService logService;

    @Reference(policy = ReferencePolicy.STATIC)
    private ConveyorControlService conveyorControlService;

    @Reference(policy = ReferencePolicy.STATIC)
    private StandardThreadPoolService threadPool;

    private StopMode mode = StopMode.OFF;

    private double stopPosition;

    private LocalDateTime stopTime;

    private ExecutorService executor;

    private boolean runMonitor = true;

    @Activate
    protected synchronized void activateMonitor() {
        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Monitor Thread");
                return thread;
            }
        });
        createMonitorThread();
    }

    @Deactivate
    protected synchronized void deactivateMonitor() {
        runMonitor = false;
    }

    private void createMonitorThread() {
        logService.log(LogService.LOG_DEBUG, "Creating Monitor Thread...");
        runMonitor = true;
        executor.execute(new MonitorRunnable());
    }

    private class MonitorRunnable implements Runnable {

        @Override
        public void run() {
            while (runMonitor == true) {
                try {

                    switch (mode) {
                    case DISTANCE_REACHED:
                        distanceReachedTick();
                        break;
                    case TIME_REACHED:
                        timeReachedTick();
                        break;
                    case OFF:
                    default:
                        // Nohting to be done
                        break;
                    }

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "Conveyor Monitor sleep interrupted...", e);
                    runMonitor = false;
                } catch (Exception e1) {
                    logService.log(LogService.LOG_DEBUG, "Unexpected exception in Monitor Thread...", e1);
                    runMonitor = false;
                }
            }
            logService.log(LogService.LOG_DEBUG, "Monitor Thread finished... restarting it in 10s");
            threadPool.timedExecute(() -> createMonitorThread(), 10, TimeUnit.SECONDS);
        }

        private void timeReachedTick() {
            ConveyorControlService.Mode conveyorMode = conveyorControlService.getActiveMode();
            if (conveyorMode == Mode.STOP) {
                // nothing further to be done
                return;
            }
            LocalDateTime currentTime = LocalDateTime.now();
            if (stopTime.isAfter(currentTime)) {
                stopConveyor();
            }
        }

        private void distanceReachedTick() {
            ConveyorControlService.Mode conveyorMode = conveyorControlService.getActiveMode();
            if (conveyorMode == Mode.STOP) {
                // nothing further to be done
                return;
            }
            double currentPosition = conveyorControlService.getPosition();
            if (conveyorMode == Mode.LEFT_TO_RIGHT) {
                if (currentPosition >= stopPosition) {
                    stopConveyor();
                }
            } else if (conveyorMode == Mode.RIGHT_TO_LEFT) {
                if (currentPosition <= stopPosition) {
                    stopConveyor();
                }
            }

        }

        private void stopConveyor() {
            logService.log(LogService.LOG_INFO, "ConveyorMonitor stopping conveyor...");
            conveyorControlService.stop();
        }

    }

    @Override
    public void setStopPosition(double position) {
        logService.log(LogService.LOG_INFO, String.format("New Stopposition submitted: %.2f", position));
        EstimatedFinishTimer.getInstance().submitTragetPosition(position);
        this.stopPosition = position;
    }

    @Override
    public void setStopTime(LocalDateTime stopTime) {
        logService.log(LogService.LOG_INFO,
                String.format("New Stoptime submitted: ", stopTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        this.stopTime = stopTime;
    }

    @Override
    public void setStopMode(StopMode mode) {
        logService.log(LogService.LOG_DEBUG, "Conveyor Monitor new Mode: \"" + mode.toString() + "\"");
        this.mode = mode;
    }

    @Override
    public StopMode getStopMode() {
        return mode;
    }

    @Override
    public double getCurrentStopPosition() {
        return stopPosition;
    }

    @Override
    public LocalDateTime getCurrentStopTime() {
        return stopTime;
    }

    @Override
    public String getFormattedMessage() {
        switch (mode) {
        case OFF:
            return "ConveyorMonitor is deactivated.";
        case DISTANCE_REACHED:
            return String.format("Conveyor will be stopped when %.2fm are reached", stopPosition);
        case TIME_REACHED:
            return "Conveyor will be stoppted at \"" + stopTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"";
        default:
            return "Unkown state";
        }
    }

    @Override
    public long getETCinMs() {

        ConveyorControlService.Mode conveyorMode = conveyorControlService.getActiveMode();
        double currentSpeed = conveyorControlService.getCurrentSpeed();
        double currentPosition = conveyorControlService.getPosition();

        if (mode == StopMode.DISTANCE_REACHED) {
            double distanceToGo = 0;
            if (conveyorMode == Mode.LEFT_TO_RIGHT) {
                distanceToGo = stopPosition - currentPosition;
            } else if (conveyorMode == Mode.RIGHT_TO_LEFT) {
                distanceToGo = currentPosition - stopPosition;
            } else {
                return 0L;
            }
            return (long) (distanceToGo / currentSpeed * 1000000);
        } else if (mode == StopMode.TIME_REACHED) {
            LocalDateTime now = LocalDateTime.now();
            return (long) stopTime.until(now, ChronoUnit.MILLIS);
        } else {
            return 0;
        }

    }

    @Override
    public String getFormattedETCMessage() {
        long etcInMs = getETCinMs();
        if (etcInMs == 0) {
            return "---";
        }

        LocalTime now = LocalTime.now();
        now = now.plusSeconds(TimeUnit.SECONDS.convert(etcInMs, TimeUnit.MILLISECONDS));

        return String.format("%02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond());
    }

}
