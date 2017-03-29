package at.sunplugged.z600.conveyor.impl;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.conveyor.engine.EngineSerialCom;
import at.sunplugged.z600.conveyor.speedlogging.SpeedLoggerImpl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.mbt.api.MbtService;

@Component(immediate = true)
public class ConveyorControlServiceImpl implements ConveyorControlService {

    private static BundleContext context;

    private static StandardThreadPoolService threadPoolService;

    private static LogService logService;

    private static MbtService mbtService;

    private static MachineStateService machineStateService;

    private static SettingsService settingsService;

    private static EventAdmin eventAdmin;

    private EngineSerialCom engineOne;

    private EngineSerialCom engineTwo;

    private SpeedLogger speedLogger;

    private SpeedControl speedControl;

    private RelativePositionMeasurement relativePositionMeasurement;

    private Future<?> startWithDistanceFuture = null;

    private ScheduledFuture<?> startWithTimeFuture = null;

    @Activate
    protected void activate(BundleContext context) {
        try {
            engineOne = new EngineSerialCom(settingsService.getProperty(NetworkComIds.LEFT_ENGINE_COM_PORT), 2);
            engineTwo = new EngineSerialCom(settingsService.getProperty(NetworkComIds.RIGHT_ENGINE_COM_PORT), 1);
            engineOne.connect();
            engineTwo.connect();
        } catch (IllegalStateException e) {
            logService.log(LogService.LOG_ERROR, "Couldnt connect engines!!!", e);
        }

        speedLogger = new SpeedLoggerImpl();
        speedControl = new SpeedControl(this);
        machineStateService.registerMachineEventHandler(speedControl);

        relativePositionMeasurement = new RelativePositionMeasurement(this);
        machineStateService.registerMachineEventHandler(relativePositionMeasurement);
    }

    @Deactivate
    protected void deactivate() {
        ((SpeedLoggerImpl) speedLogger).stopSpeedLogger();
    }

    @Override
    public void start(double speed, Mode direction) {
        if (speedControl.getMode() != Mode.STOP) {
            logService.log(LogService.LOG_ERROR, "Conveyor already running!");
            return;
        }
        speedControl.setMode(direction);
        speedControl.setSetpoint(speed);
    }

    @Override
    public void start(double speed, Mode direction, double distance) {
        if (speedControl.getMode() != Mode.STOP) {
            logService.log(LogService.LOG_ERROR, "Conveyor already running!");
            return;
        }
        start(speed, direction);

        startWithDistanceFuture = threadPoolService.submit(new Runnable() {

            @Override
            public void run() {
                double currentPosition = relativePositionMeasurement.getPosition();
                double targetPosition = currentPosition;
                if (direction == Mode.LEFT_TO_RIGHT) {
                    targetPosition += distance;
                } else if (direction == Mode.RIGHT_TO_LEFT) {
                    targetPosition -= distance;
                }
                while (true) {
                    try {

                        Thread.sleep(100);
                        if (direction == Mode.LEFT_TO_RIGHT) {
                            if (relativePositionMeasurement.getPosition() > targetPosition) {
                                stop();
                                break;
                            }
                        } else if (direction == Mode.RIGHT_TO_LEFT) {
                            if (relativePositionMeasurement.getPosition() < targetPosition) {
                                stop();
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        logService.log(LogService.LOG_DEBUG, "Waiting for distance to be traveled interrupted.");
                    }
                }
            }

        });

    }

    @Override
    public void start(double speed, Mode direction, long time, TimeUnit unit) {
        if (speedControl.getMode() != Mode.STOP) {
            logService.log(LogService.LOG_ERROR, "Conveyor already running!");
            return;
        }
        start(speed, direction);
        startWithTimeFuture = threadPoolService.timedExecute(new Runnable() {

            @Override
            public void run() {
                stop();
            }

        }, time, unit);

    }

    @Override
    public void stop() {
        speedControl.setMode(Mode.STOP);
        if (startWithTimeFuture != null) {
            startWithTimeFuture.cancel(true);
        }
        if (startWithDistanceFuture != null) {
            startWithDistanceFuture.cancel(true);
        }
    }

    @Override
    public double getCurrentSpeed() {
        return speedLogger.getCurrentSpeed();
    }

    @Override
    public double getSetpointSpeed() {
        return speedControl.getSetpoint();
    }

    @Override
    public Mode getActiveMode() {
        return speedControl.getMode();
    }

    @Override
    public Future<?> calibrate() {
        return threadPoolService.submit(new CalibrationRunnable(this));
    }

    @Reference(unbind = "unbindStandardThreadPoolService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        ConveyorControlServiceImpl.threadPoolService = standardThreadPoolService;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        if (ConveyorControlServiceImpl.threadPoolService == standardThreadPoolService) {
            ConveyorControlServiceImpl.threadPoolService = null;
        }
    }

    public static StandardThreadPoolService getStandardThreadPoolService() {
        return ConveyorControlServiceImpl.threadPoolService;
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService logService) {
        ConveyorControlServiceImpl.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (ConveyorControlServiceImpl.logService == logService) {
            ConveyorControlServiceImpl.logService = null;
        }
    }

    public static LogService getLogService() {
        return logService;
    }

    @Reference(unbind = "unbindMbtService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindMbtService(MbtService mbtService) {
        ConveyorControlServiceImpl.mbtService = mbtService;
    }

    public synchronized void unbindMbtService(MbtService mbtService) {
        if (ConveyorControlServiceImpl.mbtService.equals(mbtService)) {
            ConveyorControlServiceImpl.mbtService = null;
        }
    }

    @Reference(unbind = "unbindMachineStateService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        ConveyorControlServiceImpl.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (ConveyorControlServiceImpl.machineStateService.equals(machineStateService)) {
            ConveyorControlServiceImpl.machineStateService = null;
        }
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

    public static MbtService getMbtService() {
        return mbtService;
    }

    @Reference(unbind = "unbindSettingsService")
    public synchronized void bindSettingsService(SettingsService settingsService) {
        ConveyorControlServiceImpl.settingsService = settingsService;
    }

    public synchronized void unbindSettingsService(SettingsService settingsService) {
        if (ConveyorControlServiceImpl.settingsService == settingsService) {
            ConveyorControlServiceImpl.settingsService = null;
        }
    }

    public static SettingsService getSettingsService() {
        return settingsService;
    }

    @Reference(unbind = "unbindEventAdmin")
    public synchronized void bindEventAdmin(EventAdmin service) {
        eventAdmin = service;
    }

    public synchronized void unbindEventAdmin(EventAdmin service) {
        if (eventAdmin == service) {
            eventAdmin = null;
        }
    }

    public static EventAdmin getEventAdmin() {
        return eventAdmin;
    }

    public static BundleContext getBundleContext() {
        return context;
    }

    @Override
    public Engine getEngineOne() {
        return engineOne;
    }

    @Override
    public Engine getEngineTwo() {
        return engineTwo;
    }

    @Override
    public SpeedLogger getSpeedLogger() {
        return this.speedLogger;
    }

    @Override
    public double getPosition() {
        return relativePositionMeasurement.getPosition();
    }

    @Override
    public void setPosition(double value) {
        relativePositionMeasurement.setPosition(value);
    }

}
