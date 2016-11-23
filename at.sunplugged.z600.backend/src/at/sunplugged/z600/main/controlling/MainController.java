package at.sunplugged.z600.main.controlling;

import java.util.Date;
import java.util.Random;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.main.MainActivator;

public class MainController implements Runnable {

    private boolean isRunning = false;

    private int tickrate = 2;

    private LogService logService;

    private DataService dataService;

    @Override
    public void run() {

        getServices();

        isRunning = true;

        logService.log(LogService.LOG_INFO, "Main Controller Thread Started");
        double lastTime = System.nanoTime();

        while (isRunning) {

            try {
                dataService.saveData("TestVariable", new Date(), new Random().nextDouble() * 1000);
            } catch (DataServiceException e1) {
                e1.printStackTrace();
            }

            // Do update steps

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
        ServiceReference<?> dataServiceReference = MainActivator.getContext()
                .getServiceReference(DataService.class.getName());
        this.dataService = (DataService) MainActivator.getContext().getService(dataServiceReference);

    }

}
