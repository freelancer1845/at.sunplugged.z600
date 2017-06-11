package at.sunplugged.z600.conveyor.speedlogging;

import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.conveyor.speedlogging.SingleSideSpeedLogger.Side;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class SpeedLoggerImpl implements SpeedLogger, MachineEventHandler {

    private static SpeedLoggerImpl instance = null;

    public static SpeedLoggerImpl getInstance() {
        if (instance == null) {
            instance = new SpeedLoggerImpl();
        }
        return instance;
    }

    private SingleSideSpeedLogger leftLogger = new SingleSideSpeedLogger(Side.LEFT);

    private SingleSideSpeedLogger rightLogger = new SingleSideSpeedLogger(Side.RIGHT);

    public void reset() {
        leftLogger.reset();
        rightLogger.reset();
    }

    public void submitTriggerLeft() {
        leftLogger.trigger();
    }

    public void submitTriggerRight() {
        rightLogger.trigger();
    }

    @Override
    public double getCurrentSpeed() {
        return (leftLogger.getCurrentSpeed() + rightLogger.getCurrentSpeed()) / 2;
    }

    @Override
    public double getRightSpeed() {
        return rightLogger.getCurrentSpeed();
    }

    @Override
    public double getLeftSpeed() {
        return leftLogger.getCurrentSpeed();
    }

    @Override
    public double getRightSpeedMean() {
        return rightLogger.getMeanSpeed();
    }

    @Override
    public double getLeftSpeedMean() {
        return leftLogger.getMeanSpeed();
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == MachineStateEvent.Type.CONVEYOR_EVENT) {
            ConveyorMachineEvent cEvent = (ConveyorMachineEvent) event;
            if (cEvent.getConveyorEventType() == ConveyorMachineEvent.Type.MODE_CHANGED) {
                rightLogger.reset();
                leftLogger.reset();
            }
        }
    }

}
