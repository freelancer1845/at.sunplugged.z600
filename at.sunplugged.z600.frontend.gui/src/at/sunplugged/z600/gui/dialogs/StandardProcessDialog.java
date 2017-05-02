package at.sunplugged.z600.gui.dialogs;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.gui.views.MainView;
import at.sunplugged.z600.utils.listener.DoubleRangeModifyListener;
import at.sunplugged.z600.utils.listener.IntegerRangeModifyListener;

public class StandardProcessDialog {

    private static final String TITLE = "Process Wizard";

    private static final int WIDTH = 600;

    private static final int HEIGHT = 500;

    private static final String POWER_SORUCE_COMMAND_FORMAT = Commands.SETPOINT_POWERSOURCE + "(%s, %.5f)";

    private Shell shell;

    private final Shell parentShell;

    private int answer = SWT.CANCEL;

    private Text pressureText;

    private Button leftToRight;

    private Button rightToLeft;

    private Text distanceText;

    private Text timeText;

    private Button pinnacleButton;

    private Text pinnacleText;

    private Button ssvOneButton;

    private Text ssvOneText;

    private Button ssvTwoButton;

    private Text ssvTwoText;

    private String script = null;

    public StandardProcessDialog(Shell parentShell) {
        this.parentShell = parentShell;
    }

    public int open() {
        shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(TITLE);
        Rectangle parentBounds = parentShell.getBounds();
        shell.setLocation(parentBounds.x + parentBounds.width / 2 - WIDTH / 2,
                parentBounds.y + parentBounds.height / 2 - HEIGHT / 2);

        shell.setSize(WIDTH, HEIGHT);
        shell.setLayout(new GridLayout(1, false));

        Composite mainComposite = new Composite(shell, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        createHeaderComposite(mainComposite);

        Label label = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        label.setText("");

        createContentComposite(mainComposite);

        Label labelBottom = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        labelBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        labelBottom.setText("");

        createButtonsBottomComposite(mainComposite);

        shell.open();
        Display display = parentShell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return answer;
    }

    public String getScript() {
        return script;
    }

    private String createScript() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(createPressureCommand());

        stringBuilder.append(createPowerSourceStartCommand());

        stringBuilder.append(createConveyorCommand());

        stringBuilder.append(createPowerSourceStopCommand());
        return stringBuilder.toString();
    }

    private String createConveyorCommand() {
        StringBuilder stringBuilder = new StringBuilder();
        if (leftToRight.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, Commands.START_CONVEYOR_TIME_UNDER_CATHODE + "(%s, %.4f, %d)",
                    "LEFT_TO_RIGHT", Double.valueOf(distanceText.getText()), Integer.valueOf(timeText.getText())));
        } else if (rightToLeft.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, Commands.START_CONVEYOR_TIME_UNDER_CATHODE + "(%s, %.4f, %d)",
                    "RIGHT_TO_LEFT", Double.valueOf(distanceText.getText()), Integer.valueOf(timeText.getText())));
        }
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(Commands.WAIT_FOR_CONVEYOR + "()");
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }

    private Object createPowerSourceStopCommand() {
        StringBuilder stringBuilder = new StringBuilder();
        if (pinnacleButton.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, POWER_SORUCE_COMMAND_FORMAT, PowerSourceId.PINNACLE, 0.0));
            stringBuilder.append(System.lineSeparator());
        }
        if (ssvOneButton.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, POWER_SORUCE_COMMAND_FORMAT, PowerSourceId.SSV1, 0.0));
            stringBuilder.append(System.lineSeparator());
        }
        if (ssvTwoButton.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, POWER_SORUCE_COMMAND_FORMAT, PowerSourceId.SSV2, 0.0));
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    private String createPowerSourceStartCommand() {
        StringBuilder stringBuilder = new StringBuilder();
        if (pinnacleButton.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, POWER_SORUCE_COMMAND_FORMAT, PowerSourceId.PINNACLE,
                    Double.valueOf(pinnacleText.getText())));
            stringBuilder.append(System.lineSeparator());
        }
        if (ssvOneButton.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, POWER_SORUCE_COMMAND_FORMAT, PowerSourceId.SSV1,
                    Double.valueOf(ssvOneText.getText())));
            stringBuilder.append(System.lineSeparator());
        }
        if (ssvTwoButton.getSelection() == true) {
            stringBuilder.append(String.format(Locale.US, POWER_SORUCE_COMMAND_FORMAT, PowerSourceId.SSV2,
                    Double.valueOf(ssvTwoText.getText())));
            stringBuilder.append(System.lineSeparator());
        }
        if (pinnacleButton.getSelection() == true) {
            stringBuilder.append(Commands.WAIT_FOR_STABLE_POWERSOURCE + "(" + PowerSourceId.PINNACLE + ")");
            stringBuilder.append(System.lineSeparator());
        }
        if (ssvOneButton.getSelection() == true) {
            stringBuilder.append(Commands.WAIT_FOR_STABLE_POWERSOURCE + "(" + PowerSourceId.SSV1 + ")");
            stringBuilder.append(System.lineSeparator());
        }
        if (ssvTwoButton.getSelection() == true) {
            stringBuilder.append(Commands.WAIT_FOR_STABLE_POWERSOURCE + "(" + PowerSourceId.SSV2 + ")");
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    private String createPressureCommand() {

        return String.format(Locale.US, Commands.SET_PRESSURE + "(%.5f)", Double.valueOf(pressureText.getText()))
                + System.lineSeparator();
    }

    private void createHeaderComposite(Composite mainComposite) {
        Composite headerComposite = new Composite(mainComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        gridData.heightHint = 50;
        headerComposite.setLayoutData(gridData);
        headerComposite.setLayout(new FillLayout());
        Label mainLabel = new Label(headerComposite, SWT.NONE);
        mainLabel.setText("Wizard for creating a single process cycle.");

    }

    private void createContentComposite(Composite mainComposite) {
        Composite contentComposite = new Composite(mainComposite, SWT.NONE);
        contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        contentComposite.setLayout(new GridLayout(1, true));

        Group pressureGroup = new Group(contentComposite, SWT.NONE);
        pressureGroup.setText("Pressure");

        pressureGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pressureGroup.setLayout(new GridLayout(2, true));

        Label pressureLabel = new Label(pressureGroup, SWT.NONE);
        pressureLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pressureLabel.setText("Desired Pressure: ");

        pressureText = new Text(pressureGroup, SWT.BORDER);
        pressureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pressureText.setText("0.003");
        pressureText.addModifyListener(new DoubleRangeModifyListener(pressureText,
                MainView.getSettings().getPropertAsDouble(ParameterIds.VACUUM_LOWER_LIMIT_MBAR),
                MainView.getSettings().getPropertAsDouble(ParameterIds.VACUUM_UPPER_LIMIT_MBAR)));

        Group conveyorGroup = new Group(contentComposite, SWT.NONE);
        conveyorGroup.setText("Conveyor");
        conveyorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        conveyorGroup.setLayout(new GridLayout(2, true));

        leftToRight = new Button(conveyorGroup, SWT.RADIO);
        leftToRight.setText("Left To Right");
        leftToRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        leftToRight.setSelection(true);

        rightToLeft = new Button(conveyorGroup, SWT.RADIO);
        rightToLeft.setText("Right To Left");
        rightToLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        rightToLeft.setSelection(false);

        Label distanceLabel = new Label(conveyorGroup, SWT.NONE);
        distanceLabel.setText("Distance in [cm]");
        distanceLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        distanceText = new Text(conveyorGroup, SWT.BORDER);
        distanceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        distanceText.setText("0");
        distanceText.addModifyListener(new DoubleRangeModifyListener(distanceText, 0, Double.MAX_VALUE));

        Label timeUnderCathodeLabel = new Label(conveyorGroup, SWT.NONE);
        timeUnderCathodeLabel.setText(String.format("Time under cathode (length=%.0f mm)",
                MainView.getSettings().getPropertAsDouble(ParameterIds.CATHODE_LENGTH_MM)));
        timeUnderCathodeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        timeText = new Text(conveyorGroup, SWT.BORDER);
        timeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        timeText.setText("0");
        timeText.addModifyListener(new IntegerRangeModifyListener(distanceText, 0, Integer.MAX_VALUE));

        Group powerSourceGroup = new Group(contentComposite, SWT.NONE);
        powerSourceGroup.setText("Powersource");
        powerSourceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        powerSourceGroup.setLayout(new GridLayout(2, true));

        pinnacleButton = new Button(powerSourceGroup, SWT.CHECK);
        pinnacleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pinnacleButton.setText("Pinnacle");

        pinnacleText = new Text(powerSourceGroup, SWT.BORDER);
        pinnacleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pinnacleText.setText("0.0");
        pinnacleText.addModifyListener(new DoubleRangeModifyListener(pinnacleText,
                MainView.getSettings().getPropertAsDouble(ParameterIds.LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE),
                MainView.getSettings().getPropertAsDouble(ParameterIds.MAX_POWER)));
        pinnacleText.setEnabled(false);
        pinnacleButton.addSelectionListener(new CheckButtonTextFieldSelectionListener(pinnacleText));

        ssvOneButton = new Button(powerSourceGroup, SWT.CHECK);
        ssvOneButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ssvOneButton.setText("SSV One");

        ssvOneText = new Text(powerSourceGroup, SWT.BORDER);
        ssvOneText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ssvOneText.setText("0.0");
        ssvOneText.addModifyListener(new DoubleRangeModifyListener(ssvOneText,
                MainView.getSettings().getPropertAsDouble(ParameterIds.LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE),
                MainView.getSettings().getPropertAsDouble(ParameterIds.MAX_POWER)));
        ssvOneText.setEnabled(false);
        ssvOneButton.addSelectionListener(new CheckButtonTextFieldSelectionListener(ssvOneText));

        ssvTwoButton = new Button(powerSourceGroup, SWT.CHECK);
        ssvTwoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ssvTwoButton.setText("SSV Two");

        ssvTwoText = new Text(powerSourceGroup, SWT.BORDER);
        ssvTwoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ssvTwoText.setText("0.0");
        ssvTwoText.addModifyListener(new DoubleRangeModifyListener(ssvTwoText,
                MainView.getSettings().getPropertAsDouble(ParameterIds.LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE),
                MainView.getSettings().getPropertAsDouble(ParameterIds.MAX_POWER)));
        ssvTwoText.setEnabled(false);
        ssvTwoButton.addSelectionListener(new CheckButtonTextFieldSelectionListener(ssvTwoText));

    }

    private void createButtonsBottomComposite(Composite mainComposite) {
        Composite compositeBottomButton = new Composite(mainComposite, SWT.NONE);
        compositeBottomButton.setLayout(new GridLayout(2, true));
        compositeBottomButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        compositeBottomButton.setBounds(0, 0, 64, 64);

        Button btnOk = new Button(compositeBottomButton, SWT.NONE);
        btnOk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnOk.setText("Ok");
        btnOk.setEnabled(true);
        btnOk.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                answer = SWT.OK;
                try {
                    script = createScript();
                } catch (NumberFormatException e1) {
                    MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
                    messageBox.setText("Error parsing Arguments.");
                    messageBox.setMessage(
                            "Failed to parse all numbers. Wrong format. Error: \"" + e1.getMessage() + "\"");
                    messageBox.open();
                    answer = SWT.CANCEL;
                    return;
                }

                shell.dispose();
            }

        });

        Button btnAbbrechen = new Button(compositeBottomButton, SWT.NONE);
        btnAbbrechen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnAbbrechen.setText("Cancel");
        btnAbbrechen.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                answer = SWT.CANCEL;
                shell.dispose();
            }

        });
    }

    private class CheckButtonTextFieldSelectionListener implements SelectionListener {

        private final Text text;

        public CheckButtonTextFieldSelectionListener(Text text) {
            this.text = text;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (((Button) e.getSource()).getSelection() == true) {
                text.setEnabled(true);
            } else {
                text.setEnabled(false);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }

    }
}
