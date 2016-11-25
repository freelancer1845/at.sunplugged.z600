package at.sunplugged.z600.frontend.gui.srm50;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;

public class SrmGuiActivator implements BundleActivator {

    private static BundleContext context = null;

    private static DataService dataService = null;

    private static LogService logService = null;

    public static BundleContext getContext() {
        return context;
    }

    public static LogService getLogService() {
        return logService;
    }

    public static DataService getDataService() {
        if (dataService == null) {
            ServiceReference<DataService> serviceReference = context.getServiceReference(DataService.class);
            dataService = context.getService(serviceReference);
        }
        return dataService;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        SrmGuiActivator.context = context;
        ServiceReference<LogService> logServiceReference = context.getServiceReference(LogService.class);
        logService = context.getService(logServiceReference);

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context = null;
    }

}
