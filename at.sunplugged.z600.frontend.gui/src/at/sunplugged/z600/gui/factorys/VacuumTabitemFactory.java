package at.sunplugged.z600.gui.factorys;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.backend.vaccum.api.VacuumService;
import at.sunplugged.z600.backend.vaccum.api.VacuumService.Interlocks;
import at.sunplugged.z600.gui.views.MainView;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public final class VacuumTabitemFactory {

    private static int UPDATE_STATUS_TIME_MILLIS = 100;

    private static Text textSetpoint;

    /**
     * @wbp.factory
     */
    public static Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group vacuumGroupInterlocks = createInterlocksGroup(composite);

        Group groupEvacuate = createEvacuateGroup(composite);

        Group groupGasflow = createGasflowGroup(composite);
        return composite;
    }

    private static Group createInterlocksGroup(Composite composite) {
        Group vacuumGroupInterlocks = new Group(composite, SWT.NONE);
        vacuumGroupInterlocks.setLayout(new GridLayout(2, true));
        vacuumGroupInterlocks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        vacuumGroupInterlocks.setText("Interlocks");

        Button checkButtonCryoOne = new Button(vacuumGroupInterlocks, SWT.CHECK);
        checkButtonCryoOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        checkButtonCryoOne.setText("Cryo Eins");
        checkButtonCryoOne.addSelectionListener(new InterlockCheckListener(checkButtonCryoOne, Interlocks.CRYO_ONE));

        Button checkButtonCryoTwo = new Button(vacuumGroupInterlocks, SWT.CHECK);
        checkButtonCryoTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        checkButtonCryoTwo.setText("Cryo Zwei");
        checkButtonCryoTwo.addSelectionListener(new InterlockCheckListener(checkButtonCryoTwo, Interlocks.CRYO_TWO));
        return vacuumGroupInterlocks;
    }

    private static Group createEvacuateGroup(Composite composite) {
        Group groupEvacuate = new Group(composite, SWT.NONE);
        groupEvacuate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        groupEvacuate.setText("Evakuieren");
        groupEvacuate.setLayout(new GridLayout(2, true));

        Label labelMainStatus = new Label(groupEvacuate, SWT.NONE);
        labelMainStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        labelMainStatus.setText("Status");

        Label labelMainStatusValue = new Label(groupEvacuate, SWT.NONE);
        labelMainStatusValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        labelMainStatusValue.setText("READY");
        Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, new Runnable() {

            @Override
            public void run() {
                if (labelMainStatusValue.isDisposed() == false) {
                    labelMainStatusValue.setText(MainView.getVacuumService().getState().name());
                    Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, this);
                }

            }

        });

        Label labelTurboPumpThreadStatus = new Label(groupEvacuate, SWT.NONE);
        labelTurboPumpThreadStatus.setText("TurbPump Thread:");

        Label labelTurboPumpThreadStatusValue = new Label(groupEvacuate, SWT.NONE);
        labelTurboPumpThreadStatusValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        labelTurboPumpThreadStatusValue.setText("READY");
        Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, new Runnable() {

            @Override
            public void run() {
                if (labelTurboPumpThreadStatusValue.isDisposed() == false) {
                    labelTurboPumpThreadStatusValue
                            .setText(MainView.getVacuumService().getTurboPumpThreadState().name());
                    Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, this);
                }

            }

        });

        Label labelCryoPumpThreadStatus = new Label(groupEvacuate, SWT.NONE);
        labelCryoPumpThreadStatus.setText("CryoPump Thread:");

        Label labelCryoPumpThreadStatusValue = new Label(groupEvacuate, SWT.NONE);
        labelCryoPumpThreadStatusValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        labelCryoPumpThreadStatusValue.setText("READY");
        Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, new Runnable() {

            @Override
            public void run() {
                if (labelCryoPumpThreadStatusValue.isDisposed() == false) {
                    labelCryoPumpThreadStatusValue.setText(MainView.getVacuumService().getCryoPumpThreadState().name());
                    Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, this);
                }

            }

        });

        Label label_1 = new Label(groupEvacuate, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        Button buttonStart = new Button(groupEvacuate, SWT.NONE);
        buttonStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        buttonStart.setText("Start");
        buttonStart.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (buttonStart.getText().equals("Start")) {
                    MainView.getVacuumService().startEvacuation();
                    buttonStart.setText("Beenden");
                } else if (buttonStart.getText().equals("Beenden")) {
                    MainView.getVacuumService().shutdown();
                    buttonStart.setText("Start");
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Button vacuumButtonStop = new Button(groupEvacuate, SWT.NONE);
        vacuumButtonStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        vacuumButtonStop.setText("Stop");
        vacuumButtonStop.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MainView.getVacuumService().stopEvacuation();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        return groupEvacuate;
    }

    private static Group createGasflowGroup(Composite composite) {
        Group groupGasflow = new Group(composite, SWT.NONE);
        groupGasflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        groupGasflow.setText("Gasfluss");
        groupGasflow.setLayout(new GridLayout(2, true));

        Label labelGasflowStatus = new Label(groupGasflow, SWT.NONE);
        labelGasflowStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        labelGasflowStatus.setText("Status");

        Label labelGasflussStatusValue = new Label(groupGasflow, SWT.NONE);
        labelGasflussStatusValue.setText("READY");
        Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, new Runnable() {

            @Override
            public void run() {
                if (labelGasflussStatusValue.isDisposed() == false) {
                    labelGasflussStatusValue
                            .setText(MainView.getMachineStateService().getGasFlowControl().getState().name());
                    Display.getDefault().timerExec(UPDATE_STATUS_TIME_MILLIS, this);
                }

            }

        });

        Label label_2 = new Label(groupGasflow, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        Label labelSetpoint = new Label(groupGasflow, SWT.NONE);
        labelSetpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        labelSetpoint.setText("Setpoint");

        textSetpoint = new Text(groupGasflow, SWT.BORDER | SWT.RIGHT);
        textSetpoint.setText("0.003");
        textSetpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textSetpoint.setToolTipText("Zum übernehmen \"Enter\" drücken.");
        textSetpoint.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    double setPoint = Double.valueOf(textSetpoint.getText());
                    if (!textSetpoint.getBackground().equals(SWTResourceManager.getColor(SWT.COLOR_WHITE))) {
                        textSetpoint.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                        textSetpoint.setToolTipText("Zum übernehmen \"Enter\" drücken.");
                    }
                    if (setPoint > 0.007 || setPoint < 0.001) {
                        textSetpoint.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                        textSetpoint.setToolTipText("Darf nicht größer als 0.007 oder kleiner als 0.001 seien!");
                    }

                } catch (NumberFormatException e1) {
                    textSetpoint.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                    textSetpoint.setToolTipText("Format nicht erkannt. Muss Java Double.valueOf(String s) erfüllen.");
                }
            }
        });
        textSetpoint.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (!textSetpoint.getBackground().equals(SWTResourceManager.getColor(SWT.COLOR_RED))) {
                    if (e.keyCode == SWT.CR) {
                        MainView.getVacuumService().setSetpointPressure(Double.valueOf(textSetpoint.getText()));
                        textSetpoint.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                    }
                }

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });

        Label label_3 = new Label(groupGasflow, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        Button buttonStartGasflow = new Button(groupGasflow, SWT.NONE);
        buttonStartGasflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        buttonStartGasflow.setText("Start");
        buttonStartGasflow.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MainView.getVacuumService().startPressureControl(0.003);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Button buttonStopGasflow = new Button(groupGasflow, SWT.NONE);
        buttonStopGasflow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        buttonStopGasflow.setText("Stop");
        buttonStopGasflow.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MainView.getVacuumService().stopPressureControl();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        return groupGasflow;
    }

    private static class InterlockCheckListener implements SelectionListener {

        private final Button button;

        private final Interlocks interlock;

        public InterlockCheckListener(Button button, Interlocks interlock) {
            this.button = button;
            this.interlock = interlock;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            MainView.getVacuumService().setInterlock(interlock, button.getSelection());
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }

    }
}