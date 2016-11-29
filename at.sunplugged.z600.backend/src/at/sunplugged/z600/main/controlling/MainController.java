package at.sunplugged.z600.main.controlling;

import java.io.IOException;
import java.util.Date;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.main.impl.ControllerInterfaceImpl;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class MainController implements Runnable {

    private boolean isRunning = false;

    private int tickrate = 2;

    private LogService logService = ControllerInterfaceImpl.getLogService();

    private DataService dataService = ControllerInterfaceImpl.getDataService();

    private SrmCommunicator srmCommunicator = ControllerInterfaceImpl.getSrmCommunicator();

    private MachineStateService machineStateService = ControllerInterfaceImpl.getMachineStateService();

    @Override
    public void run() {

        try {
            connectHardware();
            isRunning = true;
        } catch (IOException e1) {
            logService.log(LogService.LOG_ERROR, "Failed to connect to all necessary Hardware!", e1);
            isRunning = false;
        }

        logService.log(LogService.LOG_INFO, "Main Controller Thread Started");
        double lastTime = System.nanoTime();

        while (isRunning) {
            try {
                Date snapShotDate = new Date();

                machineStateService.update(snapShotDate);
                processAllData();
                sendAllData();

            } catch (RuntimeException e2) {
                logService.log(LogService.LOG_ERROR, "Unhandled Runtime Exception in MainControl", e2);
            }

            double delta = System.nanoTime() - lastTime;
            double waitTime = 1.0f / tickrate * 1000000000 - delta;
            if (waitTime > 0) {
                try {
                    Thread.sleep((long) waitTime / 1000000);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_INFO, "Main Controller Thread Interrupted");
                    isRunning = false;
                }
            }
            lastTime = System.nanoTime();

        }

    }

    private void connectHardware() throws IOException {
        srmCommunicator.connect("COM2");
    }

    private void processAllData() {

    }

    private void sendAllData() {

    }

}
