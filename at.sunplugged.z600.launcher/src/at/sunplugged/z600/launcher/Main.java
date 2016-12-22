package at.sunplugged.z600.launcher;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.gui.views.MainView;
import at.sunplugged.z600.launcher.splash.SplashWindow;

@Component(immediate = true)
public class Main implements IApplication, BundleActivator {

    private static BundleContext context;

    private LogService logService;

    @Override
    public Object start(IApplicationContext context) throws Exception {
        context.applicationRunning();

        // First check for all services to run
        if (!splashWindowLoop()) {
            return EXIT_OK;
        }

        // Main SWT Loop
        mainWindowLoop();

        return EXIT_OK;
    }

    @Override
    public void stop() {
        System.out.println("Application stopped");
    }

    private boolean splashWindowLoop() {
        Display display = Display.getDefault();
        Shell shell = SplashWindow.getShell();
        Rectangle splashRect = shell.getBounds();
        Rectangle displayRect = display.getBounds();
        int x = (displayRect.width - splashRect.width) / 2;
        int y = (displayRect.height - splashRect.height) / 2;
        shell.setLocation(x, y);
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (ProgrammShutdownException e) {
                shell.dispose();
                return false;
            } catch (RuntimeException e) {
                MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
                messageBox.setMessage("Unhandled Loop Exception: " + e.getMessage());
                messageBox.setText("Unhandled Loop Exeception");
                messageBox.open();
                logService.log(LogService.LOG_ERROR, e.getMessage(), e);
            }

        }
        return true;
    }

    private void mainWindowLoop() {
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

    @Override
    public void start(BundleContext context) throws Exception {
        Main.context = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Main.context = null;
    }

    public static BundleContext getContext() {
        return context;
    }

}
