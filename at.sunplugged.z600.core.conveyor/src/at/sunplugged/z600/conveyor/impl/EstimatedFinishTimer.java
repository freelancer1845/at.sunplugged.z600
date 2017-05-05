package at.sunplugged.z600.conveyor.impl;

import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;

public class EstimatedFinishTimer {

    private static EstimatedFinishTimer instance = new EstimatedFinishTimer();

    public static EstimatedFinishTimer getInstance() {
        return instance;
    }

    private double targetPosition = 0;

    private boolean activated = false;

    public void submitTragetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }

    public void activate() {
        activated = true;
    }

    public void deactivate() {
        activated = false;
    }

    public long getNeededTimeInMs() {
        if (activated == false) {
            return 0;
        }

        Mode mode = ConveyorControlServiceImpl.getInstance().getActiveMode();
        if (mode == Mode.STOP) {
            return 0;
        }

        double currentSpeed = ConveyorControlServiceImpl.getInstance().getCurrentSpeed();
        double currentPosition = ConveyorControlServiceImpl.getInstance().getPosition();

        double distanceToGo = 0;
        if (mode == Mode.LEFT_TO_RIGHT) {
            distanceToGo = targetPosition - currentPosition;
        } else if (mode == Mode.RIGHT_TO_LEFT) {
            distanceToGo = currentPosition - targetPosition;
        }

        return (long) (distanceToGo / currentSpeed * 1000000);
    }

}
