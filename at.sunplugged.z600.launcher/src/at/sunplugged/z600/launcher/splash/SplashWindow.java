package at.sunplugged.z600.launcher.splash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import at.sunplugged.z600.common.utils.Events;
import at.sunplugged.z600.launcher.ProgrammShutdownException;
import at.sunplugged.z600.launcher.splash.checkgroups.CheckGroup;
import at.sunplugged.z600.launcher.splash.checkgroups.EngineCheckGroup;
import at.sunplugged.z600.launcher.splash.checkgroups.MbtCheckGroup;
import at.sunplugged.z600.launcher.splash.checkgroups.SRMCheckGroup;
import at.sunplugged.z600.launcher.splash.checkgroups.SqlCheckGroup;
import at.sunplugged.z600.launcher.splash.checkgroups.VatCheckGroup;

@Component(immediate = true, property = { EventConstants.EVENT_TOPIC + "=" + Events.MBT_CONNECT_EVENT,
        EventConstants.EVENT_TOPIC + "=" + Events.SQL_CONNECT_EVENT,
        EventConstants.EVENT_TOPIC + "=" + Events.ENGINE_CONNECT_EVENT,
        EventConstants.EVENT_TOPIC + "=" + Events.VAT_CONNECT_EVENT,
        EventConstants.EVENT_TOPIC + "=" + Events.SRM_CONNECT_EVENT })
public class SplashWindow implements EventHandler {

    private static Shell shell;

    private static ProgressBar progressBar;

    private static Label label;
    private static Composite information_composite;

    private static CheckGroup mbtCheckGroup = new MbtCheckGroup();
    private static CheckGroup sqlCheckGroup = new SqlCheckGroup();
    private static CheckGroup engineCheckGroup = new EngineCheckGroup();
    private static CheckGroup vatCheckGroup = new VatCheckGroup();
    private static CheckGroup srmCheckGroup = new SRMCheckGroup();

    private static Label label_1;
    private static Composite composite;

    public SplashWindow() {

    }

    /**
     * @wbp.parser.entryPoint
     */
    public static Shell getShell() {
        shell = new Shell(SWT.ON_TOP);
        shell.setSize(358, 274);
        shell.setLayout(new GridLayout(1, false));

        Label lblZ = new Label(shell, SWT.NONE);
        lblZ.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
        GridData gd_lblZ = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_lblZ.horizontalIndent = 20;
        lblZ.setLayoutData(gd_lblZ);
        lblZ.setText("Z600");

        label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        information_composite = new Composite(shell, SWT.NONE);
        information_composite.setLayout(new GridLayout(2, false));
        information_composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        mbtCheckGroup.create(information_composite);
        sqlCheckGroup.create(information_composite);
        engineCheckGroup.create(information_composite);
        vatCheckGroup.create(information_composite);
        srmCheckGroup.create(information_composite);

        progressBar = new ProgressBar(information_composite, SWT.NONE);
        progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        progressBar.setSelection(0);

        label_1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        Button btnAbbrechen = new Button(composite, SWT.NONE);
        GridData gd_btnAbbrechen = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
        gd_btnAbbrechen.widthHint = 75;
        btnAbbrechen.setLayoutData(gd_btnAbbrechen);
        btnAbbrechen.setText("Abbrechen");
        btnAbbrechen.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                throw new ProgrammShutdownException("Aborted by user.");
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Button btnWeiter = new Button(composite, SWT.NONE);
        GridData gd_btnWeiter = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_btnWeiter.widthHint = 75;
        btnWeiter.setLayoutData(gd_btnWeiter);
        btnWeiter.setText("Weiter");
        btnWeiter.addSelectionListener(new ContinueButtonSelectionListener(shell));
        initializeShell();
        createContents();
        updateSplashWindow();
        return shell;
    }

    public static void dispose() {
        shell.dispose();

    }

    private static void updateSplashWindow() {
        mbtCheckGroup.update();
        sqlCheckGroup.update();
        engineCheckGroup.update();
        vatCheckGroup.update();
        srmCheckGroup.update();
    }

    private static void initializeShell() {

    }

    private static void createContents() {

    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic() == Events.MBT_CONNECT_EVENT) {
            mbtCheckGroup.setEvent(event);
        } else if (event.getTopic() == Events.SQL_CONNECT_EVENT) {
            sqlCheckGroup.setEvent(event);
        } else if (event.getTopic() == Events.ENGINE_CONNECT_EVENT) {
            engineCheckGroup.setEvent(event);
        } else if (event.getTopic() == Events.VAT_CONNECT_EVENT) {
            vatCheckGroup.setEvent(event);
        } else if (event.getTopic() == Events.SRM_CONNECT_EVENT) {
            srmCheckGroup.setEvent(event);
        }
        if (shell != null && shell.isDisposed() == false) {
            shell.getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    updateSplashWindow();
                }

            });
        }
    }

}
