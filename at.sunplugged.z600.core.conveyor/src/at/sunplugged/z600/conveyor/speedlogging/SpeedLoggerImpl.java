package at.sunplugged.z600.conveyor.speedlogging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.conveyor.impl.ConveyorControlServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public class SpeedLoggerImpl extends Thread implements SpeedLogger {

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
        this.setName("Speed Logger Thread");
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

        long leftLastTickTime = 0;
        long rightLastTickTime = 0;

        double leftTickDifference = -1;
        double rightTickDifference = -1;

        while (running) {
            try {
                states = mbtService.readDigIns(LEFT_DIGITAL_IN_ADDRESS, 2);

                newLeftState = states.get(LEFT_DIGITAL_IN_ADDRESS);
                newRightState = states.get(RIGHT_DIGITAL_IN_ADDRESS);

                long now = System.nanoTime();
                if (oldLeftState != newLeftState && newLeftState == true) {
                    leftTickDifference = (now - leftLastTickTime) / 1000000000.0;
                    leftLastTickTime = now;
                    addToLeftMeasurementList(leftTickDifference);
                }
                if (oldRightState != newRightState && newRightState == true) {
                    rightTickDifference = (now - rightLastTickTime) / 1000000000.0;
                    rightLastTickTime = now;
                    addToRightMeasurementList(rightTickDifference);
                }
                if (now - leftLastTickTime > 60000000000L) {
                    leftLastTickTime = now;
                    addToLeftMeasurementList(-1);
                }
                if (now - rightLastTickTime > 60000000000L) {
                    rightLastTickTime = now;
                    addToRightMeasurementList(-1);
                }

                oldLeftState = newLeftState;
                oldRightState = newRightState;

                Thread.sleep(100);

            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, "SpeedLogger failed. Shutting Down.", e);
                running = false;
            } catch (InterruptedException e) {
                logService.log(LogService.LOG_ERROR, "Interrupted!", e);
            }

        }
    }

    private void addToLeftMeasurementList(double tickDifference) {
        double speed;
        if (tickDifference < 0) {
            speed = 0;
        } else {
            speed = LEFT_DISTANCE_PER_HOLE / tickDifference * 1000;
        }
        leftSpeedMeasurements.add(0, speed);
        if (leftSpeedMeasurements.size() > 2) {
            leftSpeedMeasurements.remove(leftSpeedMeasurements.size() - 1);
        }
        leftSpeedValue = speed;
        SpeedChangeUtilityClass.submitLeftSpeedChange(speed);
    }

    private void addToRightMeasurementList(double tickDifference) {
        double speed;
        if (tickDifference < 0) {
            speed = 0;
        } else {
            speed = RIGHT_DISTANCE_PER_HOLE / tickDifference * 1000;

        }
        rightSpeedMeasurements.add(0, speed);
        if (rightSpeedMeasurements.size() > 2) {
            rightSpeedMeasurements.remove(rightSpeedMeasurements.size() - 1);
        }
        rightSpeedValue = speed;
        SpeedChangeUtilityClass.submitRightSpeedChange(speed);
    }

    @Override
    public double getRightSpeedMean() {
        return rightSpeedMeasurements.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    @Override
    public double getLeftSpeedMean() {
        return leftSpeedMeasurements.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

}
