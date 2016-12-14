package at.sunplugged.z600.backend.logging;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

@Component(immediate = true)
public class ConsoleLogger implements LogListener {

    private LogReaderService logReaderService;

    private ExtendedLogReaderService extendedLogReaderService;

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
        switch (entry.getLevel()) {
        case LogService.LOG_DEBUG:
            System.out.println(LocalDate.now().format(DateTimeFormatter.ISO_DATE) + " - "
                    + entry.getBundle().getSymbolicName() + " - " + entry.getMessage());
            if (entry.getException() != null) {
                entry.getException().printStackTrace();
            }
            break;
        case LogService.LOG_ERROR:
            System.out.println(LocalDate.now().format(DateTimeFormatter.ISO_DATE) + " - "
                    + entry.getBundle().getSymbolicName() + " - " + entry.getMessage());
            if (entry.getException() != null) {
                entry.getException().printStackTrace();
            }
            break;
        case LogService.LOG_INFO:
            System.out.println(LocalDate.now().format(DateTimeFormatter.ISO_DATE) + " - "
                    + entry.getBundle().getSymbolicName() + " - " + entry.getMessage());
            if (entry.getException() != null) {
                entry.getException().printStackTrace();
            }
            break;
        case LogService.LOG_WARNING:
            System.out.println(LocalDate.now().format(DateTimeFormatter.ISO_DATE) + " - "
                    + entry.getBundle().getSymbolicName() + " - " + entry.getMessage());
            if (entry.getException() != null) {
                entry.getException().printStackTrace();
            }
            break;
        default:
            break;
        }
    }
}
