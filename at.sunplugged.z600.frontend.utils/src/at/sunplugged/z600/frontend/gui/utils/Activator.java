package at.sunplugged.z600.frontend.gui.utils;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator {

    private static BundleContext context;

    private static LogService logService;

    public static BundleContext getContext() {
        return context;
    }

    public static LogService getLogService() {
        return logService;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        ServiceReference<LogService> reference = context.getServiceReference(LogService.class);
        logService = context.getService(reference);

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Activator.context = null;
        logService = null;
    }

}
