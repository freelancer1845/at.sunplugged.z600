package at.sunplugged.z600.conveyor.impl;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.conveyor.speedlogging.SpeedLoggerImpl;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class RelativePositionMeasurement implements MachineEventHandler {

    private final ConveyorControlService conveyorControlService;

    private double positionOffset = 0;

    private TimeFilteredTriggerCounter leftCounter = new TimeFilteredTriggerCounter();

    private TimeFilteredTriggerCounter rightCounter = new TimeFilteredTriggerCounter();

    public RelativePositionMeasurement(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.DIGITAL_INPUT_CHANGED) {
            handleTriggerEvent(event);
        }
    }

    private void handleTriggerEvent(MachineStateEvent event) {

        int countsLeft = leftCounter.getTriggers();
        int countsRight = rightCounter.getTriggers();

        if (event.getOrigin() == WagoAddresses.DigitalInput.LEFT_SPEED_TRIGGER) {
            if (conveyorControlService.getActiveMode() == Mode.LEFT_TO_RIGHT) {
                leftCounter.setCountDirectionUp();
            } else if (conveyorControlService.getActiveMode() == Mode.RIGHT_TO_LEFT) {
                leftCounter.setCountDirectionDown();
            }

            leftCounter.submitTriggerValue((boolean) event.getValue());
        } else if (event.getOrigin() == WagoAddresses.DigitalInput.RIGHT_SPEED_TRIGGER) {
            if (conveyorControlService.getActiveMode() == Mode.LEFT_TO_RIGHT) {
                rightCounter.setCountDirectionUp();
            } else if (conveyorControlService.getActiveMode() == Mode.RIGHT_TO_LEFT) {
                rightCounter.setCountDirectionDown();
            }

            rightCounter.submitTriggerValue((boolean) event.getValue());
        }
        if (countsLeft != leftCounter.getTriggers()) {
            SpeedLoggerImpl.getInstance().submitTriggerLeft();
        }
        if (countsRight != rightCounter.getTriggers()) {
            SpeedLoggerImpl.getInstance().submitTriggerRight();
        }
        if (countsLeft != leftCounter.getTriggers() || countsRight != rightCounter.getTriggers()) {
            ConveyorControlServiceImpl.getMachineStateService().fireMachineStateEvent(
                    new ConveyorMachineEvent(ConveyorMachineEvent.Type.NEW_DISTANCE, getPosition()));
        }

    }

    public double getPosition() {
        return (getLeftPosition() + getRightPosition()) / 2;
    }

    public double getLeftPosition() {
        return leftCounter.getTriggers() * SpeedLogger.LEFT_DISTANCE_PER_HOLE + positionOffset;
    }

    public double getRightPosition() {
        return rightCounter.getTriggers() * SpeedLogger.RIGHT_DISTANCE_PER_HOLE + positionOffset;
    }

    public void setPosition(double position) {
        leftCounter.reset();
        rightCounter.reset();
        positionOffset = position;
        ConveyorControlServiceImpl.getMachineStateService()
                .fireMachineStateEvent(new ConveyorMachineEvent(ConveyorMachineEvent.Type.NEW_DISTANCE, getPosition()));
    }

}
