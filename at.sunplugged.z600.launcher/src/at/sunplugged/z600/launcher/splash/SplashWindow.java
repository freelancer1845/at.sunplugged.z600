package at.sunplugged.z600.launcher.splash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
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

import at.sunplugged.z600.launcher.ProgrammShutdownException;

@Component(immediate = true, property = { EventConstants.EVENT_TOPIC + "=at/sunplugged/z600/mbt/connect",
        EventConstants.EVENT_TOPIC + "=at/sunplugged/z600/sql/connect" })
public class SplashWindow implements EventHandler {

    private static Shell shell;

    private static ProgressBar progressBar;

    private static Label label;
    private static Composite information_composite;

    private static Event mbtServiceEvent;
    private static StyledText mbt_styled_text;
    private static Button mbt_error_button;
    private static ErrorSelectionListener mbtSelectionListener;

    private static Event sqlServiceEvent;
    private static StyledText sql_styled_text;
    private static Button sql_error_button;
    private static ErrorSelectionListener sqlSelectionListener;

    private static StyledText styledText_2;
    private static StyledText styledText_3;
    private static StyledText styledText_4;

    private static Button button_1;
    private static Button button_2;
    private static Button button_3;
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

        mbt_styled_text = new StyledText(information_composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        mbt_styled_text.setEnabled(false);
        mbt_styled_text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        mbt_styled_text.setText("Trying to connect to modbus...");
        mbt_styled_text.setStyleRange(new StyleRange(0, mbt_styled_text.getText().length(),
                SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW), null));
        mbt_styled_text.setAlwaysShowScrollBars(false);
        mbt_styled_text.setEditable(false);
        mbt_styled_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        mbt_error_button = new Button(information_composite, SWT.NONE);
        mbt_error_button.setEnabled(false);
        mbt_error_button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        mbt_error_button.setText("Fehler Anzeigen");
        mbtSelectionListener = new ErrorSelectionListener(shell);
        mbt_error_button.addSelectionListener(mbtSelectionListener);

        sql_styled_text = new StyledText(information_composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        sql_styled_text.setEnabled(false);
        sql_styled_text.setText("Trying to connect to sql server...");
        sql_styled_text.setEditable(false);
        sql_styled_text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        sql_styled_text.setAlwaysShowScrollBars(false);
        sql_styled_text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        sql_error_button = new Button(information_composite, SWT.NONE);
        sql_error_button.setEnabled(false);
        sql_error_button.setText("Fehler Anzeigen");
        sqlSelectionListener = new ErrorSelectionListener(shell);
        sql_error_button.addSelectionListener(sqlSelectionListener);

        styledText_2 = new StyledText(information_composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        styledText_2.setEnabled(false);
        styledText_2.setText("Modbus Controller... connected");
        styledText_2.setEditable(false);
        styledText_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        styledText_2.setAlwaysShowScrollBars(false);
        styledText_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        button_1 = new Button(information_composite, SWT.NONE);
        button_1.setEnabled(false);
        button_1.setText("Fehler Anzeigen");

        styledText_3 = new StyledText(information_composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        styledText_3.setEnabled(false);
        styledText_3.setText("Modbus Controller... connected");
        styledText_3.setEditable(false);
        styledText_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        styledText_3.setAlwaysShowScrollBars(false);
        styledText_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        button_2 = new Button(information_composite, SWT.NONE);
        button_2.setEnabled(false);
        button_2.setText("Fehler Anzeigen");

        styledText_4 = new StyledText(information_composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        styledText_4.setEnabled(false);
        styledText_4.setText("Modbus Controller... connected");
        styledText_4.setEditable(false);
        styledText_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        styledText_4.setAlwaysShowScrollBars(false);
        styledText_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        button_3 = new Button(information_composite, SWT.NONE);
        button_3.setEnabled(false);
        button_3.setText("Fehler Anzeigen");

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
        if (SplashWindow.mbtServiceEvent != null) {
            if ((boolean) SplashWindow.mbtServiceEvent.getProperty("success") == true) {
                mbt_styled_text.setText("Modbus verbunden!");
                mbt_styled_text.setStyleRange(new StyleRange(0, mbt_styled_text.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN), null));
                mbt_error_button.setEnabled(false);
            } else {
                mbt_styled_text.setText("Modbus nicht verbunden...");
                mbt_styled_text.setStyleRange(new StyleRange(0, mbt_styled_text.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_RED), null));
                mbt_error_button.setEnabled(true);
                mbtSelectionListener.setError((Throwable) SplashWindow.mbtServiceEvent.getProperty("Error"));
            }
            mbtServiceEvent = null;
        }
        if (SplashWindow.sqlServiceEvent != null) {
            if ((boolean) sqlServiceEvent.getProperty("success") == true) {
                sql_styled_text.setText("SQL Server verbunden!");
                sql_styled_text.setStyleRange(new StyleRange(0, sql_styled_text.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN), null));
                sql_error_button.setEnabled(false);
            } else {
                sql_styled_text.setText("SQL Server nicht verbunden...");
                sql_styled_text.setStyleRange(new StyleRange(0, sql_styled_text.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_RED), null));
                sql_error_button.setEnabled(true);
                sqlSelectionListener.setError((Throwable) sqlServiceEvent.getProperty("Error"));
            }
            sqlServiceEvent = null;
        }
    }

    private static void initializeShell() {

    }

    private static void createContents() {

    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic() == "at/sunplugged/z600/mbt/connect") {
            SplashWindow.mbtServiceEvent = event;
        } else if (event.getTopic() == "at/sunplugged/z600/sql/connect") {
            SplashWindow.sqlServiceEvent = event;
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
