package at.sunplugged.z600.launcher;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.gui.views.MainView;

@Component
public class Main implements IApplication {

    private LogService logService;

    @Override
    public Object start(IApplicationContext context) throws Exception {
        System.out.println("Application Started");
        context.applicationRunning();

        // First check for all services to run

        // Main SWT Loop
        Display display = Display.getDefault();
        Shell shell = MainView.createMainWindow();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (RuntimeException e) {
                MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
                messageBox.setMessage("Unhandled Loop Exception: " + e.getMessage());
                messageBox.setText("Unhandled Loop Exeception");
                messageBox.open();
                logService.log(LogService.LOG_ERROR, e.getMessage(), e);
            }
        }

        return EXIT_OK;
    }

    @Override
    public void stop() {
        System.out.println("Application stopped");
    }

    @Reference(unbind = "unbindLogService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

}
