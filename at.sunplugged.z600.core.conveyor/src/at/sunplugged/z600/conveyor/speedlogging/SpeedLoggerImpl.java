package at.sunplugged.z600.conveyor.speedlogging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.conveyor.impl.ConveyorControlServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public class SpeedLoggerImpl extends Thread implements SpeedLogger {

    private static final int LEFT_DIGITAL_IN_ADDRESS = 39;

    private static final int RIGHT_DIGITAL_IN_ADDRESS = 40;

    private static final double LEFT_HOLE_PLATE_RADIUS = 0.5;

    private static final double LEFT_DRUM_RADIUS = 0.8;

    private static final int LEFT_NUMBER_OF_HOLES = 30;

    private static final double LEFT_DISTANCE_PER_HOLE = LEFT_HOLE_PLATE_RADIUS * 2 * Math.PI / LEFT_NUMBER_OF_HOLES;

    private static final double LEFT_TRANSMISSION_RATIO = LEFT_DRUM_RADIUS / LEFT_HOLE_PLATE_RADIUS;

    private static final double RIGHT_HOLE_PLATE_RADIUS = 0.5;

    private static final double RIGHT_DRUM_RADIUS = 0.8;

    private static final int RIGHT_NUMBER_OF_HOLES = 30;

    private static final double RIGHT_DISTANCE_PER_HOLE = RIGHT_HOLE_PLATE_RADIUS * 2 * Math.PI / RIGHT_NUMBER_OF_HOLES;

    private static final double RIGHT_TRANSMISSION_RATION = RIGHT_DRUM_RADIUS / RIGHT_HOLE_PLATE_RADIUS;

    private boolean running = false;

    private MbtService mbtService;

    private LogService logService;

    private List<Double> leftSpeedMeasurements = new ArrayList<>();

    private double leftSpeedValue = 0;

    private List<Double> rightSpeedMeasurements = new ArrayList<>();

    private double rightSpeedValue = 0;

    public SpeedLoggerImpl() {
        this.mbtService = ConveyorControlServiceImpl.getMbtService();
        this.logService = ConveyorControlServiceImpl.getLogService();
        running = true;
        this.start();
    }

    public void stopSpeedLogger() {
        running = false;
    }

    @Override
    public double getCurrentSpeed() {
        return (rightSpeedValue + leftSpeedValue) / 2;
    }

    @Override
    public double getRightSpeed() {
        return rightSpeedValue;
    }

    @Override
    public double getLeftSpeed() {
        return leftSpeedValue;
    }

    @Override
    public void run() {
        List<Boolean> states;
        boolean oldLeftState = false;
        boolean oldRightState = false;

        boolean newLeftState = false;
        boolean newRightState = false;

        long leftlastTickTime = 0;
        long rightlastTickTime = 0;

        double leftTickDifference = -1;
        double rightTickDifference = -1;

        while (running) {
            try {
                states = mbtService.readDigIns(LEFT_DIGITAL_IN_ADDRESS, 2);

                newLeftState = states.get(LEFT_DIGITAL_IN_ADDRESS);
                newRightState = states.get(RIGHT_DIGITAL_IN_ADDRESS);

                long now = System.nanoTime();
                if (oldLeftState != newLeftState && newLeftState == true) {
                    leftTickDifference = (now - leftlastTickTime) / 1000000000.0;
                    leftlastTickTime = now;
                    addToLeftMeasurementList(leftTickDifference);
                }
                if (oldRightState != newRightState && newRightState == true) {
                    rightTickDifference = (now - rightlastTickTime) / 1000000000.0;
                    rightlastTickTime = now;
                    addToRightMeasurementList(rightTickDifference);
                }

                oldLeftState = newLeftState;
                oldRightState = newRightState;

                Thread.sleep(100);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private void addToLeftMeasurementList(double tickDifference) {

        double speed = LEFT_DISTANCE_PER_HOLE / tickDifference * LEFT_TRANSMISSION_RATIO;
        leftSpeedMeasurements.add(0, speed);
        if (leftSpeedMeasurements.size() > 30) {
            leftSpeedMeasurements.remove(leftSpeedMeasurements.size() - 1);
        }
        leftSpeedValue = speed;
        logService.log(LogService.LOG_DEBUG, "New Left Speed: " + leftSpeedValue);
    }

    private void addToRightMeasurementList(double tickDifference) {

        double speed = RIGHT_DISTANCE_PER_HOLE / tickDifference * RIGHT_TRANSMISSION_RATION;
        rightSpeedMeasurements.add(0, speed);
        if (rightSpeedMeasurements.size() > 30) {
            rightSpeedMeasurements.remove(rightSpeedMeasurements.size() - 1);
        }
        rightSpeedValue = speed;
        logService.log(LogService.LOG_DEBUG, "New Right Speed: " + rightSpeedValue);
    }

}
