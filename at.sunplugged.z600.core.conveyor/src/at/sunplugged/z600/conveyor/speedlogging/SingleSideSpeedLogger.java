package at.sunplugged.z600.conveyor.speedlogging;

import java.util.ArrayList;
import java.util.List;

import at.sunplugged.z600.conveyor.api.SpeedLogger;

public class SingleSideSpeedLogger {

    private long lastTimeTriggered = Long.MIN_VALUE;

    private List<Double> speedMeasurements = new ArrayList<>();

    private final Side side;

    public enum Side {
        LEFT(SpeedLogger.LEFT_DISTANCE_PER_HOLE), RIGHT(SpeedLogger.RIGHT_DISTANCE_PER_HOLE);

        private Side(double distancePerHole) {
            this.distancePerHole = distancePerHole;
        }

        public final double distancePerHole;
    }

    public SingleSideSpeedLogger(Side side) {
        this.side = side;
    }

    public void reset() {
        lastTimeTriggered = Long.MIN_VALUE;
        speedMeasurements.clear();
    }

    public void trigger(long triggerTime) {
        if (lastTimeTriggered == Long.MIN_VALUE) {
            lastTimeTriggered = System.nanoTime();
        } else {
            long tickDifference = triggerTime - lastTimeTriggered;
            addToMeasurementList(tickDifference);
            lastTimeTriggered = triggerTime;
        }
    }

    public double getCurrentSpeed() {
        if (speedMeasurements.size() > 0) {
            return speedMeasurements.get(speedMeasurements.size() - 1);
        } else {
            return 0;
        }
    }

    public double getMeanSpeed() {
        if (speedMeasurements.size() == 0) {
            return 0;
        }
        return speedMeasurements.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    private void addToMeasurementList(double tickDifference) {
        double speed;
        if (tickDifference < 0) {
            speed = 0;
        } else {
            speed = side.distancePerHole / tickDifference * 1000000000 * 1000;
        }
        speedMeasurements.add(0, speed);
        if (speedMeasurements.size() > 2) {
            speedMeasurements.remove(speedMeasurements.size() - 1);
        }
        if (side == Side.LEFT) {
            SpeedChangeUtilityClass.submitLeftSpeedChange(speed);
        } else if (side == Side.RIGHT) {
            SpeedChangeUtilityClass.submitRightSpeedChange(speed);
        }

    }
}
