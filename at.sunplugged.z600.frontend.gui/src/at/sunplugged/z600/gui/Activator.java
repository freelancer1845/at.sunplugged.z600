package at.sunplugged.z600.gui;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import at.sunplugged.z600.gui.views.MainApplication;

public class Activator implements BundleActivator {

    private static BundleContext context;

    public static BundleContext getContext() {
        return context;
    }

    private static LogService logService;

    private ServiceTracker logServiceTracker;

    public static LogService getLogService() {
        return logService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;

        logServiceTracker = new ServiceTracker<>(bundleContext, LogService.class, null);
        logServiceTracker.open();
        logService = (LogService) logServiceTracker.getService();

        MainApplication mainApplication = new MainApplication();
        Thread guiThread = new Thread(new Runnable() {

            @Override
            public void run() {
                mainApplication.open();

            }

        });
        guiThread.setName("Gui Thread");
        guiThread.start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        logServiceTracker.close();
    }

}
