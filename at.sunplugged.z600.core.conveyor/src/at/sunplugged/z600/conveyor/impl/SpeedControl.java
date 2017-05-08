package at.sunplugged.z600.conveyor.impl;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent.Type;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.api.EngineException;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class SpeedControl implements MachineEventHandler {

    private final Engine engineOne;

    private final Engine engineTwo;

    private final ConveyorControlService conveyorControlService;

    private double setPointSpeed = 0;

    private Mode currentMode = Mode.STOP;

    public SpeedControl(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
        this.engineOne = conveyorControlService.getEngineOne();
        this.engineTwo = conveyorControlService.getEngineTwo();

    }

    public void setSetpoint(double speed) {
        if (speed <= 0) {
            currentMode = Mode.STOP;
        }
        setPointSpeed = speed;
    }

    public double getSetpoint() {
        return setPointSpeed;
    }

    public void setMode(Mode mode) {
        currentMode = mode;
        ConveyorControlServiceImpl.getMachineStateService()
                .fireMachineStateEvent(new ConveyorMachineEvent(Type.MODE_CHANGED, mode));
        try {
            switch (mode) {
            case RIGHT_TO_LEFT:
                engineOne.initializeEngine();
                engineTwo.initializeEngine();
                engineOne.setDirection(0);
                engineTwo.setDirection(0);
                engineTwo.setLoose();
                // TODO : For now this is only copy paste from previous program.
                // Implement some calibration
                engineOne.setMaximumSpeed((int) (576000 * setPointSpeed / (2 * Math.PI * (80 + 3))));
                engineOne.startEngine();
                break;
            case LEFT_TO_RIGHT:
                engineOne.initializeEngine();
                engineTwo.initializeEngine();
                engineOne.setDirection(1);
                engineTwo.setDirection(1);
                engineOne.setLoose();
                // TODO : For now this is only copy paste from previous program.
                // Implement some calibration
                engineTwo.setMaximumSpeed((int) (576000 * setPointSpeed / (2 * Math.PI * (80 + 3))));
                engineTwo.startEngine();
                break;
            case STOP:
                engineOne.stopEngine();
                engineTwo.stopEngine();
                break;
            }
        } catch (EngineException e) {
            ConveyorControlServiceImpl.getLogService().log(LogService.LOG_ERROR,
                    "Engine exception caught while setting mode.", e);
            engineOne.stopEngine();
            engineTwo.stopEngine();
            currentMode = Mode.STOP;
            ConveyorControlServiceImpl.getMachineStateService()
                    .fireMachineStateEvent(new ConveyorMachineEvent(Type.MODE_CHANGED, currentMode));
        }

    }

    public Mode getMode() {
        return currentMode;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (currentMode == Mode.STOP) {
            return;
        }
        if (event.getType() == MachineStateEvent.Type.CONVEYOR_EVENT) {
            ConveyorMachineEvent conveyorEvent = (ConveyorMachineEvent) event;
            if (conveyorEvent.getConveyorEventType() == Type.LEFT_SPEED_CHANGED
                    || conveyorEvent.getConveyorEventType() == Type.RIGHT_SPEED_CHANGED) {
                try {
                    handleSpeedChange(conveyorEvent);
                } catch (EngineException e) {
                    ConveyorControlServiceImpl.getLogService().log(LogService.LOG_ERROR,
                            "Engine Exception caught while updating speed.", e);
                }
            }
        }

    }

    private void handleSpeedChange(ConveyorMachineEvent conveyorEvent) throws EngineException {
        int currentEngineSpeed;
        int newEngineSpeed;
        if (currentMode == Mode.RIGHT_TO_LEFT) {
            if (conveyorEvent.getConveyorEventType() == Type.RIGHT_SPEED_CHANGED) {
                return;
            }
            currentEngineSpeed = engineOne.getCurrentMaximumSpeed();
            newEngineSpeed = calculateNewEngineSpeed(conveyorControlService.getSpeedLogger().getRightSpeedMean(),
                    currentEngineSpeed);
            engineOne.setMaximumSpeed(newEngineSpeed);
        } else if (currentMode == Mode.LEFT_TO_RIGHT) {
            if (conveyorEvent.getConveyorEventType() == Type.LEFT_SPEED_CHANGED) {
                return;
            }
            currentEngineSpeed = engineTwo.getCurrentMaximumSpeed();
            newEngineSpeed = calculateNewEngineSpeed(conveyorControlService.getSpeedLogger().getLeftSpeedMean(),
                    currentEngineSpeed);
            engineTwo.setMaximumSpeed(newEngineSpeed);
        }
    }

    private int calculateNewEngineSpeed(double currentSpeed, int currentEngineSpeed) {
        if (currentEngineSpeed == 0) {
            return 500;
        }
        if (currentSpeed == 0) {
            return currentEngineSpeed;
        }
        if (Math.abs(currentSpeed - setPointSpeed) > 0.0003) {
            double ratio = setPointSpeed / currentSpeed;
            if (ratio > 1.05) {
                return (int) (currentEngineSpeed + 10);
            } else if (ratio < 0.95) {
                return (int) (currentEngineSpeed - 10);
            }

            return (int) (currentEngineSpeed * ratio);
        }

        return currentEngineSpeed;
    }

}
