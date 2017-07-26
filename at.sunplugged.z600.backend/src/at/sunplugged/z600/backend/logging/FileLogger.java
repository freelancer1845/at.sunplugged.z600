package at.sunplugged.z600.backend.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class FileLogger implements LogListener {

    private static final String LOG_FILE_NAME_PREFIX = "log";

    private static final String LOG_FILE_SUFFIX = ".txt";

    private LogReaderService logReaderService;

    private ExtendedLogReaderService extendedLogReaderService;

    private ExecutorService executor;

    private boolean errorLogged = false;

    private final String sessionFileName;

    public FileLogger() {

        this.sessionFileName = LOG_FILE_NAME_PREFIX
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMuuuuHHmmss")) + LOG_FILE_SUFFIX;

    }

    @Activate
    public void activateLogger() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Deactivate
    public void deactivate() {
        if (errorLogged == false) {
            File logFile = new File(sessionFileName);
            if (logFile.exists()) {
                // logFile.delete();
            }
        }
    }

    @Reference(unbind = "unbindLogReaderService", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public synchronized void bindLogReaderService(LogReaderService logReaderService) {
        this.logReaderService = logReaderService;
        this.logReaderService.addLogListener(this);
    }

    public synchronized void unbindLogReaderService(LogReaderService logReaderService) {
        if (this.logReaderService == logReaderService) {
            this.logReaderService.removeLogListener(this);
            this.logReaderService = null;
        }
    }

    @Reference(unbind = "unbindExtendedLogReaderService", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public synchronized void bindExtendedLogReaderService(ExtendedLogReaderService extendedLogReaderService) {
        this.extendedLogReaderService = extendedLogReaderService;
        this.extendedLogReaderService.addLogListener(this);
    }

    public synchronized void unbindExtendedLogReaderService(ExtendedLogReaderService extendedLogReaderService) {
        if (this.extendedLogReaderService == extendedLogReaderService) {
            this.extendedLogReaderService.removeLogListener(this);
            this.extendedLogReaderService = null;
        }
    }

    @Override
    public void logged(LogEntry entry) {
        executor.submit(() -> {
            logToFile(entry);
        });
    }

    private void logToFile(LogEntry entry) {
        File eventLog = new File(sessionFileName);
        if (eventLog.exists() == false) {
            try {
                eventLog.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String logEntry = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        switch (entry.getLevel()) {
        case LogService.LOG_DEBUG:
            logEntry += " - DEBUG - ";
            break;
        case LogService.LOG_INFO:
            logEntry += " - INFO - ";
            break;
        case LogService.LOG_WARNING:
            logEntry += " - WARNING - ";
            break;
        case LogService.LOG_ERROR:
            logEntry += " - ERROR - ";
            errorLogged = true;
            break;
        default:
            logEntry += " - UNKOWN LOG LEVEL - ";
            break;
        }
        logEntry += "MESSAGE: \"";
        logEntry += entry.getMessage();
        logEntry += "\"";

        try (FileWriter fw = new FileWriter(sessionFileName, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);) {

            out.println(logEntry);
            Throwable cause = entry.getException();
            if (cause != null) {
                cause.printStackTrace(out);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
