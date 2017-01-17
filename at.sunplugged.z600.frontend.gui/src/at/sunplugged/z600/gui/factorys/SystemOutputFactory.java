package at.sunplugged.z600.gui.factorys;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

@Component(immediate = true)
public final class SystemOutputFactory implements LogListener {

    private static final int CONSOLE_BUFFER = 1000;

    private static LogReaderService logReaderService;

    private static ExtendedLogReaderService extendedLogReaderService;

    private static StyledText styledText;

    /**
     * @wbp.factory
     */
    public static StyledText createStyledText(Composite parent) {
        styledText = new StyledText(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        styledText.setRightMargin(5);
        styledText.setLeftMargin(5);
        styledText.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        styledText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event e) {
                styledText.setBottomMargin(20);
                styledText.setTopIndex(styledText.getLineCount() - 1);
            }
        });
        return styledText;
    }

    @Override
    public void logged(LogEntry entry) {
        if (styledText != null) {
            switch (entry.getLevel()) {
            case LogService.LOG_INFO:
                logInfo(entry);
                break;
            case LogService.LOG_DEBUG:
                logDebug(entry);
                break;
            case LogService.LOG_WARNING:
                logWarning(entry);
                break;
            case LogService.LOG_ERROR:
                logError(entry);
                break;
            default:

                break;
            }
        }
    }

    private void logInfo(LogEntry entry) {
        logWithColor(entry, SWT.COLOR_WIDGET_LIGHT_SHADOW);
    }

    private void logDebug(LogEntry entry) {
        logWithColor(entry, SWT.COLOR_CYAN);
    }

    private void logWarning(LogEntry entry) {
        logWithColor(entry, SWT.COLOR_YELLOW);
    }

    private void logError(LogEntry entry) {
        logWithColor(entry, SWT.COLOR_RED);
    }

    private void logWithColor(LogEntry entry, int systemColor) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {

                String timeString = createTimeStampe(entry);
                String logText = timeString + entry.getMessage();
                styledText.append(logText + "\n");

                int lastTextPosition = styledText.getCharCount();
                StyleRange styleRange = new StyleRange(lastTextPosition - logText.length() + timeString.length() - 1,
                        logText.length() - timeString.length(), SWTResourceManager.getColor(systemColor),
                        styledText.getBackground());
                styledText.setStyleRange(styleRange);
            }

        });
    }

    private String createTimeStampe(LogEntry entry) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date resultDate = new Date(entry.getTime());
        switch (entry.getLevel()) {
        case LogService.LOG_INFO:
            return simpleDateFormat.format(resultDate) + " ERROR ->";
        case LogService.LOG_DEBUG:
            return simpleDateFormat.format(resultDate) + " DEBUG ->";
        case LogService.LOG_WARNING:
            return simpleDateFormat.format(resultDate) + " WARNING ->";
        case LogService.LOG_ERROR:
            return simpleDateFormat.format(resultDate) + " ERROR ->";
        default:
            return null;
        }
    }

    @Reference(unbind = "unbindLogReaderService", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public synchronized void bindLogReaderService(LogReaderService logReaderService) {
        SystemOutputFactory.logReaderService = logReaderService;
        SystemOutputFactory.logReaderService.addLogListener(this);
    }

    public synchronized void unbindLogReaderService(LogReaderService logReaderService) {
        if (SystemOutputFactory.logReaderService == logReaderService) {
            SystemOutputFactory.logReaderService.removeLogListener(this);
            SystemOutputFactory.logReaderService = null;
        }
    }

    @Reference(unbind = "unbindExtendedLogReaderService", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public synchronized void bindExtendedLogReaderService(ExtendedLogReaderService extendedLogReaderService) {
        SystemOutputFactory.extendedLogReaderService = extendedLogReaderService;
        SystemOutputFactory.extendedLogReaderService.addLogListener(this);
    }

    public synchronized void unbindExtendedLogReaderService(ExtendedLogReaderService extendedLogReaderService) {
        if (SystemOutputFactory.extendedLogReaderService == extendedLogReaderService) {
            SystemOutputFactory.extendedLogReaderService.removeLogListener(this);
            SystemOutputFactory.extendedLogReaderService = null;
        }
    }

}
