package at.sunplugged.z600.srm50;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import at.sunplugged.z600.srm50.api.SrmCommunicator;
import at.sunplugged.z600.srm50.impl.SrmCommunicatorImpl;

public class SrmActivator implements BundleActivator {

    private static BundleContext context;

    private static LogService logService;

    private ServiceTracker logServiceTracker;

    private ServiceRegistration<SrmCommunicator> srmCommunicatorService;

    static BundleContext getContext() {
        return context;
    }

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
        SrmActivator.context = bundleContext;
        SrmCommunicator srmCommunciator = new SrmCommunicatorImpl();
        srmCommunicatorService = context.registerService(SrmCommunicator.class, srmCommunciator, null);

        logServiceTracker = new ServiceTracker<>(bundleContext, LogService.class, null);
        logServiceTracker.open();
        logService = (LogService) logServiceTracker.getService();
        srmCommunciator.connect("COM1");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        SrmActivator.context = null;
        srmCommunicatorService.unregister();
        logServiceTracker.close();

    }

}
