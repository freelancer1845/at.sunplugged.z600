package at.sunplugged.z600.conveyor.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.Engine;

public class CalibrationRunnable implements Runnable {

    private static final String CALIBRATION_FILE_NAME = "speed_calibration.cfg";

    private ConveyorControlService conveyorControlService;

    private volatile boolean cancel = false;

    private long traveldStepsInDirectionOne = 0;

    private Engine engineOne = conveyorControlService.getEngineOne();

    private Engine engineTwo = conveyorControlService.getEngineTwo();

    private LogService logService;

    public CalibrationRunnable(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
        engineOne = conveyorControlService.getEngineOne();
        engineTwo = conveyorControlService.getEngineTwo();
        logService = ConveyorControlServiceImpl.getLogService();
    }

    @Override
    public void run() {

        try {
            doTestRun(400, 1, 20000);
            doTestRun(400, 0, 20000);
            doTestRun(800, 1, 15000);
            doTestRun(800, 0, 15000);
            doTestRun(1200, 1, 10000);
            doTestRun(1200, 0, 10000);
        } catch (InterruptedException e) {
            logService.log(LogService.LOG_DEBUG, "Calibration interrupted!");
        } finally {
            engineOne.stopEngine();
            engineTwo.startEngine();
        }

    }

    private void doTestRun(int maximumFrequency, int direction, long delay) throws InterruptedException {
        logService.log(LogService.LOG_DEBUG, "Doing Calibration run. MaximumFrequency: \"" + maximumFrequency
                + "\" Direction: \"" + direction + "\" Delay(ms): \"" + delay + "\"");
        if (direction == 1) {
            engineTwo.setLoose();
            engineOne.setDirection(1);
            engineOne.setMaximumSpeed(maximumFrequency);

            long startTime = System.currentTimeMillis();
            engineOne.startEngine();

            // wait 10s

            Thread.sleep(delay);
            long runTime = System.currentTimeMillis() - startTime;
            traveldStepsInDirectionOne += runTime / 1000.0 * maximumFrequency;
        } else if (direction == 0) {
            engineOne.setLoose();
            engineTwo.setDirection(0);
            engineTwo.setMaximumSpeed(maximumFrequency);

            long startTime = System.currentTimeMillis();
            engineTwo.startEngine();

            Thread.sleep(delay);
            long runTime = System.currentTimeMillis() - startTime;
            traveldStepsInDirectionOne -= runTime / 1000.0 * maximumFrequency;
        }
        writeNewDataPoint(maximumFrequency, conveyorControlService.getCurrentSpeed());
        engineOne.stopEngine();
        engineTwo.stopEngine();
    }

    private void writeNewDataPoint(int maximumFrequency, double speed) {
        logService.log(LogService.LOG_DEBUG,
                "Calibration: Writing datapoint: \"" + maximumFrequency + "," + speed + "\"");
        File file = ConveyorControlServiceImpl.getBundleContext().getDataFile(CALIBRATION_FILE_NAME);
        try (FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);) {

            out.println(maximumFrequency + "," + speed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
