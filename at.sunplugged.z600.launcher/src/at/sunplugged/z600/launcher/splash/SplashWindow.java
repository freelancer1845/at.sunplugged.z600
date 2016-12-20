package at.sunplugged.z600.launcher.splash;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.eclipse.swt.widgets.ProgressBar;

@Component(immediate = true, property = { EventConstants.EVENT_TOPIC + "=mbtServiceConnect" })
public class SplashWindow implements EventHandler {

    private static Shell shell;

    private static ProgressBar progressBar;

    private static Event mbtServiceEvent;

    public SplashWindow() {

    }

    public static Shell getShell() {
        shell = new Shell(SWT.ON_TOP);
        shell.setSize(347, 241);
        shell.setLayout(new GridLayout(1, false));

        Label lblZ = new Label(shell, SWT.NONE);
        lblZ.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
        GridData gd_lblZ = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_lblZ.horizontalIndent = 20;
        lblZ.setLayoutData(gd_lblZ);
        lblZ.setText("Z600");

        progressBar = new ProgressBar(shell, SWT.NONE);
        progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        progressBar.setSelection(0);
        initializeShell();
        createContents();
        updateSplashWindow();
        return shell;
    }

    public void dispose() {
        shell.dispose();

    }

    private static void updateSplashWindow() {
        if (SplashWindow.mbtServiceEvent != null) {
            if ((boolean) SplashWindow.mbtServiceEvent.getProperty("success") == true) {
                progressBar.setSelection(50);
            } else {
                progressBar.setSelection(20);
            }
        }
    }

    private static void initializeShell() {

    }

    private static void createContents() {

    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic() == "mbtServiceConnect") {
            SplashWindow.mbtServiceEvent = event;
        }
        if (shell != null) {
            shell.getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    updateSplashWindow();
                }

            });
        }
    }

}
