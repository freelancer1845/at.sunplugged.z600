package at.sunplugged.z600.gui;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import at.sunplugged.z600.gui.start.GuiStarter;

public class Activator implements BundleActivator {

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
        Activator.context = bundleContext;

        GuiStarter guiStarter = new GuiStarter();
        Thread thread = new Thread(guiStarter);
        thread.setName("Gui Thread");
        thread.start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
