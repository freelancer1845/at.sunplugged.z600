package at.sunplugged.z600.main;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class MainActivator implements BundleActivator {

    private static BundleContext context;

    public static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        MainActivator.context = bundleContext;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        MainActivator.context = null;

    }

}
