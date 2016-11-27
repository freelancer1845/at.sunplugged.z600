package at.sunplugged.z600.backend.dataservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.impl.DataServiceImpl;
import at.sunplugged.z600.backend.dataservice.impl.SqlConnection;

public class DataServiceActivator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    private static LogService logService;

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
        DataServiceActivator.context = bundleContext;
        DataService dataService = new DataServiceImpl();
        context.registerService(DataService.class, dataService, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        DataServiceActivator.context = null;

    }

}
