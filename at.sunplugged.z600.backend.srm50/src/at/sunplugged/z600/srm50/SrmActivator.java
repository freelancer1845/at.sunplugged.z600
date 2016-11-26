package at.sunplugged.z600.srm50;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import at.sunplugged.z600.srm50.api.SrmCommunicator;
import at.sunplugged.z600.srm50.impl.SrmCommunicatorImpl;

public class SrmActivator implements BundleActivator {

    private static BundleContext context;

    private static LogService logService;

    private ServiceRegistration<SrmCommunicator> srmCommunicatorService;

    private SrmCommunicator srmCommunicator;

    static BundleContext getContext() {
        return context;
    }

    public static LogService getLogService() {
        if (logService == null) {
            ServiceReference<LogService> reference = context.getServiceReference(LogService.class);
            logService = context.getService(reference);
        }
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
        srmCommunicator = new SrmCommunicatorImpl();
        srmCommunicatorService = context.registerService(SrmCommunicator.class, srmCommunicator, null);

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
        if (srmCommunicator != null) {
            srmCommunicator.disconnect();
        }
    }

}
