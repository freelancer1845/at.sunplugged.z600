package at.sunplugged.z600.frontend.gui.api;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.log.LogService;

public class ServiceRegistry {

    private static ServiceRegistry instance = new ServiceRegistry();

    private LogService logService;

    public static ServiceRegistry getInstance() {
        return instance;
    }

    private static Map<String, Object> serviceMap = new HashMap<>();

    public static <T> T getService(Class<T> clazz) {
        if (!serviceMap.containsKey(clazz.getName())) {
        }
        return (T) serviceMap.get(clazz.getName());
    }

    public synchronized void bindService(Object service) {
        if (!serviceMap.containsKey(service.getClass().getName())) {
            serviceMap.put(service.getClass().getName(), service);
        }
        if (service.getClass().equals(LogService.class)) {
            this.logService = (LogService) service;
        }
    }

    public synchronized void unbindService(Object service) {
        if (serviceMap.containsKey(service.getClass().getName())) {
            serviceMap.remove(service.getClass().getName());
        }
    }

    private ServiceRegistry() {

    }

}
