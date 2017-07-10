package at.sunplugged.z600.frontend.gui.utils.api;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class ServiceRegistryAccess {

    private static BundleContext bundleContext;

    @Activate
    public synchronized void activateComponent(BundleContext bundleContext) {
        ServiceRegistryAccess.bundleContext = bundleContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> clazz) {
        ServiceReference<?> serviceReference = bundleContext.getServiceReference(clazz.getName());
        if (serviceReference == null) {
            throw new IllegalStateException("Found no registered service for class " + clazz.getName());
        }
        return (T) bundleContext.getService(serviceReference);
    }

}
