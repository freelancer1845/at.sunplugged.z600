package at.sunplugged.z600.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import at.sunplugged.z600.main.controlling.MainController;

public class MainActivator implements BundleActivator {

    private Thread mainControllerThread;

    private static BundleContext context;

    public static BundleContext getContext() {
        return context;
    }

    private static LogService logService;

    private ServiceTracker logServiceTracker;

    public static LogService getLogService() {
        return logService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        MainActivator.context = bundleContext;
        initilizeLogListener(bundleContext);
        mainControllerThread = new Thread(new MainController());
        mainControllerThread.setName("Main Controller Thread");
        mainControllerThread.start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        MainActivator.context = null;
        mainControllerThread.interrupt();

    }

    // TODO: Move to a Bundle that saves the log and displays it.
    private void initilizeLogListener(BundleContext bundleContext) {
        logServiceTracker = new ServiceTracker<>(bundleContext, LogService.class, null);
        logServiceTracker.open();
        logService = (LogService) logServiceTracker.getService();

        ServiceReference readerServiceReference = bundleContext.getServiceReference(LogReaderService.class);
        LogReaderService readerService = (LogReaderService) bundleContext.getService(readerServiceReference);

        readerService.addLogListener(new LogListener() {

            @Override
            public void logged(LogEntry arg0) {
                switch (arg0.getLevel()) {
                    case LogService.LOG_DEBUG:
                        System.out.println("DEBUG - " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                                + " - " + arg0.getBundle().getSymbolicName() + " - " + ": " + arg0.getMessage());
                        break;
                    case LogService.LOG_ERROR:
                        System.out.println("ERROR - " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                                + " - " + arg0.getBundle().getSymbolicName() + " - " + ": " + arg0.getMessage());
                        if(arg0.getException() != null) {
                        	arg0.getException().printStackTrace();
                        }
                        break;
                    case LogService.LOG_WARNING:
                        System.out.println("WARNING - " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                                + " - " + arg0.getBundle().getSymbolicName() + " - " + ": " + arg0.getMessage());
                        break;
                    case LogService.LOG_INFO:
                        System.out.println("INFO - " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                                + " - " + arg0.getBundle().getSymbolicName() + " - " + ": " + arg0.getMessage());
                        break;
                    default:
                        break;
                }
            }

        });
    }

}
