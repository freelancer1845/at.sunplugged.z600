package at.sunplugged.z600.main.controlling;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.backend.dataservice.api.VariableIdentifiers;
import at.sunplugged.z600.main.MainActivator;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class MainController implements Runnable {

    private boolean isRunning = false;

    private int tickrate = 2;

    private LogService logService;

    private DataService dataService;

    private SrmCommunicator srmCommunicator;

    @Override
    public void run() {

        getServices();
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

                acquireAllData();
                processAllData();
                sendAllData();

            } catch (RuntimeException e2) {
                logService.log(LogService.LOG_ERROR, "Unhandle Runtime Exception in MainControl", e2);
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, "IO Exception during acquiring Data. Stopping MainControl.", e);
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

    private void getServices() {
        logService = MainActivator.getLogService();
        this.dataService = getService(DataService.class);
        this.srmCommunicator = getService(SrmCommunicator.class);

    }

    private <T> T getService(Class<T> clazz) {
        ServiceReference<T> reference = MainActivator.getContext().getServiceReference(clazz);
        return MainActivator.getContext().getService(reference);
    }

    private void connectHardware() throws IOException {
        srmCommunicator.connect("COM2");
    }

    private void acquireAllData() throws IOException {
        Date snapShotDate = new Date();

        acquireSrmData(snapShotDate);
    }

    private void processAllData() {

    }

    private void sendAllData() {

    }

    private void acquireSrmData(Date snapshotDate) throws IOException {
        List<Double> data = srmCommunicator.readChannels();
        for (int i = 0; i < data.size(); i++) {
            try {
                dataService.saveData(VariableIdentifiers.SRM_CHANNEL + (i + 1), snapshotDate, data.get(i));
            } catch (DataServiceException e) {
                logService.log(LogService.LOG_ERROR, "Error during acquiring SRM Data for Channel: " + i, e);
            }
        }

    }

}
