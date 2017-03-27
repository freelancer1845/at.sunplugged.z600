package at.sunplugged.z600.conveyor.impl;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class RelativePositionMeasurement implements MachineEventHandler {

    private final ConveyorControlService conveyorControlService;

    private double rightPosition = 0;

    private double leftPosition = 0;

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
        if ((boolean) event.getValue() == true) {
            if (event.getOrigin() == WagoAddresses.DigitalInput.LEFT_SPEED_TRIGGER) {
                if (conveyorControlService.getActiveMode() == Mode.LEFT_TO_RIGHT) {
                    leftPosition += SpeedLogger.LEFT_DISTANCE_PER_HOLE;
                } else if (conveyorControlService.getActiveMode() == Mode.RIGHT_TO_LEFT) {
                    leftPosition -= SpeedLogger.LEFT_DISTANCE_PER_HOLE;
                }
            } else if (event.getOrigin() == WagoAddresses.DigitalInput.RIGHT_SPEED_TRIGGER) {
                if (conveyorControlService.getActiveMode() == Mode.LEFT_TO_RIGHT) {
                    rightPosition += SpeedLogger.RIGHT_DISTANCE_PER_HOLE;
                } else if (conveyorControlService.getActiveMode() == Mode.RIGHT_TO_LEFT) {
                    rightPosition -= SpeedLogger.RIGHT_DISTANCE_PER_HOLE;
                }
            }

        }
    }

    public double getPosition() {
        return (leftPosition + rightPosition) / 2;
    }

    public void setPosition(double position) {
        leftPosition = position;
        rightPosition = position;
    }

}
