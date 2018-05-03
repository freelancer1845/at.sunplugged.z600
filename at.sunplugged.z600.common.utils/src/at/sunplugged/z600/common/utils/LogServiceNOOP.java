package at.sunplugged.z600.common.utils;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class LogServiceNOOP implements LogService {

    @Override
    public void log(int arg0, String arg1) {
        System.out.println("LogLevel: " + arg0 + " --- " + arg1);

    }

    @Override
    public void log(int arg0, String arg1, Throwable arg2) {
        System.out.println("LogLevel: " + arg0 + " --- " + arg1);
        arg2.printStackTrace();
    }

    @Override
    public void log(ServiceReference arg0, int arg1, String arg2) {
        System.out.println(
                "Bundle: " + arg0.getBundle().getSymbolicName() + " --- " + "LogLevel: " + arg0 + " --- " + arg1);
    }

    @Override
    public void log(ServiceReference arg0, int arg1, String arg2, Throwable arg3) {
        System.out.println(
                "Bundle: " + arg0.getBundle().getSymbolicName() + " --- " + "LogLevel: " + arg0 + " --- " + arg1);
        arg3.printStackTrace();

    }

}
