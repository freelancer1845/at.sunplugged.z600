package at.sunplugged.z600.main.controlling;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.main.MainActivator;

public class MainController implements Runnable {

    private boolean isRunning = false;

    private int tickrate = 2;

    private LogService logService;

    @Override
    public void run() {
        logService = MainActivator.getLogService();
        isRunning = true;

        logService.log(LogService.LOG_INFO, "Main Controller Thread Started");
        double lastTime = System.nanoTime();

        while (isRunning) {
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

}
