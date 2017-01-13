package at.sunplugged.z600.conveyor.speedlogging;

import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent.Type;
import at.sunplugged.z600.conveyor.impl.ConveyorControlServiceImpl;

public class SpeedChangeUtilityClass {

    private static final double THRESHOLD = 1e-4;

    private static double lastFiredSpeedRight = 0;

    private static double lastFiredSpeedLeft = 0;

    public static void submitLeftSpeedChange(double newSpeed) {
        if (Math.abs(newSpeed - lastFiredSpeedLeft) >= THRESHOLD) {
            ConveyorControlServiceImpl.getMachineStateService()
                    .fireMachineStateEvent(new ConveyorMachineEvent(Type.LEFT_SPEED_CHANGED, newSpeed));
            lastFiredSpeedLeft = newSpeed;
        }
    }

    public static void submitRightSpeedChange(double newSpeed) {
        if (Math.abs(newSpeed - lastFiredSpeedRight) >= THRESHOLD) {
            ConveyorControlServiceImpl.getMachineStateService()
                    .fireMachineStateEvent(new ConveyorMachineEvent(Type.RIGHT_SPEED_CHANGED, newSpeed));
            lastFiredSpeedRight = newSpeed;
        }
    }

    private SpeedChangeUtilityClass() {

    }
}
