package at.sunplugged.z600.backend.dataservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.impl.DataServiceImpl;

public class DataServiceActivator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework. BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        DataServiceActivator.context = bundleContext;
        DataService dataService = new DataServiceImpl();
        context.registerService(DataService.class, dataService, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        DataServiceActivator.context = null;

    }

}
