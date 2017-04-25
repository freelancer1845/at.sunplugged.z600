package at.sunplugged.z600.gui.factorys;

import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorPositionCorrectionService;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.gui.dialogs.ValueDialog;
import at.sunplugged.z600.gui.dialogs.ValueDialog.Answer;

@Component(immediate = true)
public final class ConveyorGroupFactory {

    private static ConveyorControlService conveyorService;

    private static ConveyorPositionCorrectionService conveyorPositionService;

    private static StandardThreadPoolService threadPool;

    private static Text speedText;

    /**
     * @wbp.factory
     */
    public static Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        group.setText("Bandlauf");
        GridLayout layout = new GridLayout(2, true);
        layout.verticalSpacing = 15;
        group.setLayout(layout);

        Label lblGeschwindigkeit = new Label(group, SWT.NONE);
        lblGeschwindigkeit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblGeschwindigkeit.setText("Geschwindigkeit in mm/s [Setpoint = 0]");
        Display.getDefault().timerExec(5000, new Runnable() {

            @Override
            public void run() {
                lblGeschwindigkeit.setText(
                        String.format("Geschwindigkeit in mm/s [Setpoint = %.2f]", conveyorService.getSetpointSpeed()));
                Display.getDefault().timerExec(200, this);
            }

        });

        speedText = new Text(group, SWT.BORDER | SWT.RIGHT);
        speedText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        speedText.setText("0.0");

        Button btnLinks = new Button(group, SWT.NONE);
        btnLinks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnLinks.setText("Links");
        btnLinks.setEnabled(false);

        Button btnRechts = new Button(group, SWT.NONE);
        btnRechts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnRechts.setText("Rechts");

        btnLinks.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        btnLinks.setEnabled(false);
                        btnRechts.setEnabled(true);
                    }

                });
            }
        });

        btnRechts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        btnLinks.setEnabled(true);
                        btnRechts.setEnabled(false);
                    }

                });

            }
        });

        Button btnStart = new Button(group, SWT.NONE);
        btnStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStart.setText("Start");
        btnStart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                double speed;
                try {
                    speed = Double.valueOf(speedText.getText());
                } catch (NumberFormatException e1) {
                    return;
                }
                if (btnRechts.isEnabled() == false) {

                    conveyorService.start(speed, Mode.LEFT_TO_RIGHT);
                } else if (btnLinks.isEnabled() == false) {
                    conveyorService.start(speed, Mode.RIGHT_TO_LEFT);
                }
            }
        });

        Button btnStop = new Button(group, SWT.NONE);
        btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStop.setText("Stopp");
        btnStop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorService.stop();
            }
        });

        Text distanceDriveText = new Text(group, SWT.BORDER);
        distanceDriveText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        distanceDriveText.setText("distance in [cm]...");
        distanceDriveText.setToolTipText("Distance in [cm]");
        distanceDriveText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (distanceDriveText.getText().equals("distance in [cm]...")) {
                    distanceDriveText.setText("");
                }
            }
        });

        Text timeDriveText = new Text(group, SWT.BORDER);
        timeDriveText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        timeDriveText.setText("time under cathode in [s]...");
        timeDriveText.setToolTipText("Time under cathode in [s]");
        timeDriveText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (timeDriveText.getText().equals("time under cathode in [s]...")) {
                    timeDriveText.setText("");
                }
            }
        });

        Button btnDistanceDrive = new Button(group, SWT.NONE);
        btnDistanceDrive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnDistanceDrive.setText("Drive Distance[cm] Start");
        btnDistanceDrive.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (btnRechts.isEnabled() == false) {
                    conveyorService.start(Double.valueOf(speedText.getText()), Mode.LEFT_TO_RIGHT,
                            Double.valueOf(distanceDriveText.getText()));
                } else if (btnLinks.isEnabled() == false) {
                    conveyorService.start(Double.valueOf(speedText.getText()), Mode.RIGHT_TO_LEFT,
                            Double.valueOf(distanceDriveText.getText()));
                }
            }
        });

        Button btnTimeDrive = new Button(group, SWT.NONE);
        btnTimeDrive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnTimeDrive.setText("Drive time[s] under cathode Start");
        btnTimeDrive.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (btnRechts.isEnabled() == false) {
                    conveyorService.start(Mode.LEFT_TO_RIGHT, Double.valueOf(distanceDriveText.getText()),
                            Long.valueOf(timeDriveText.getText()), TimeUnit.SECONDS);
                } else if (btnLinks.isEnabled() == false) {
                    conveyorService.start(Mode.RIGHT_TO_LEFT, Double.valueOf(distanceDriveText.getText()),
                            Long.valueOf(timeDriveText.getText()), TimeUnit.SECONDS);
                }
            }
        });

        Button setCurrentPosition = new Button(group, SWT.NONE);
        setCurrentPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        setCurrentPosition.setText("Set the current Position in [m]");
        setCurrentPosition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ValueDialog valueDialog = new ValueDialog("Set Conveyor Position", "Set the conveyor Position in m",
                        -Double.MAX_VALUE, Double.MAX_VALUE, parent.getShell());
                if (valueDialog.open() == Answer.OK) {
                    conveyorService.setPosition(valueDialog.getValue());
                }
            }
        });

        Group positionGroup = new Group(parent, SWT.NONE);
        positionGroup.setLayout(new GridLayout(2, true));
        positionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        positionGroup.setText("Conveyor Belt Centralization");

        Button btnStartPositionControl = new Button(positionGroup, SWT.NONE);
        btnStartPositionControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
        btnStartPositionControl.setText("Start Position Control");
        btnStartPositionControl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.start();
            }

        });

        Button btnStopPositionControl = new Button(positionGroup, SWT.NONE);
        btnStopPositionControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStopPositionControl.setText("Stop Position Control");
        btnStopPositionControl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.stop();
            }

        });
        Label labelPositionControlState = new Label(positionGroup, SWT.NONE);
        labelPositionControlState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        labelPositionControlState.setText("PositionControlState");
        Display.getDefault().timerExec(500, new Runnable() {

            @Override
            public void run() {
                if (conveyorPositionService.isRunning()) {
                    labelPositionControlState.setText("PositionControlState: Running");
                } else {
                    labelPositionControlState.setText("PositionControlState: Not Running");
                }
                Display.getDefault().timerExec(500, this);
            }

        });

        Button btnCenterLeft = new Button(positionGroup, SWT.NONE);
        btnCenterLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnCenterLeft.setText("Recenter Left");
        btnCenterLeft.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.centerLeft();
            }
        });

        Button btnCenterRight = new Button(positionGroup, SWT.NONE);
        btnCenterRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnCenterRight.setText("Recenter Right");
        btnCenterRight.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.centerRight();
            }
        });

        Group manualGroup = new Group(positionGroup, SWT.NONE);
        manualGroup.setText("Manual Position Control");
        manualGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        manualGroup.setLayout(new GridLayout(6, true));

        Button leftForward = new Button(manualGroup, SWT.NONE);
        leftForward.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        leftForward.setText("Forward");
        leftForward.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.startLeftForward();
            }
        });

        Button leftBackward = new Button(manualGroup, SWT.NONE);
        leftBackward.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        leftBackward.setText("Backward");
        leftBackward.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.startLeftBackward();
            }
        });

        Button stopMove = new Button(manualGroup, SWT.NONE);
        stopMove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        stopMove.setText("Stop");
        stopMove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.stopManualMove();
            }
        });

        Button resetTimer = new Button(manualGroup, SWT.NONE);
        resetTimer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        resetTimer.setText("ResetTimer");
        resetTimer.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.setRuntimeLeft(0);
                conveyorPositionService.setRuntimeRight(0);
            }
        });
        Button rightForward = new Button(manualGroup, SWT.NONE);
        rightForward.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        rightForward.setText("Forward");
        rightForward.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.startRightForward();
            }
        });

        Button rightBackward = new Button(manualGroup, SWT.NONE);
        rightBackward.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        rightBackward.setText("Backward");
        rightBackward.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.startRightBackward();
            }
        });

        createEngineGroup(parent, conveyorService.getEngineOne(), conveyorService.getEngineTwo(), "Left Engine", false);
        createEngineGroup(parent, conveyorService.getEngineTwo(), conveyorService.getEngineOne(), "Right Engine", true);

        return group;
    }

    private static Group createEngineGroup(Composite parent, Engine driveEngine, Engine otherEngine, String name,
            boolean isRightEngine) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, true));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        group.setText(name);

        Button leftRadio = new Button(group, SWT.RADIO);
        leftRadio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        leftRadio.setSelection(true);
        leftRadio.setText("Clockwise");

        Button rightRadio = new Button(group, SWT.RADIO);
        rightRadio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        rightRadio.setSelection(false);
        rightRadio.setText("Counter-Clockwise");

        Label speedLabel = new Label(group, SWT.NONE);
        speedLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        speedLabel.setText("Speed ~[mm/s]");

        Text speedText = new Text(group, SWT.BORDER);
        speedText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        speedText.setText("0");

        Button startButton = new Button(group, SWT.NONE);
        startButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        startButton.setText("Start");
        startButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                double speed;
                try {
                    speed = Double.valueOf(speedText.getText());

                } catch (NumberFormatException e1) {
                    return;
                }
                if (leftRadio.getSelection() == true) {
                    driveEngine.setDirection(1);
                } else if (rightRadio.getSelection() == true) {
                    driveEngine.setDirection(0);
                }
                conveyorPositionService.start();
                driveEngine.setMaximumSpeed((int) (576000 * speed / (2 * Math.PI * (80 + 3))));
                otherEngine.setLoose();
                driveEngine.startEngine();
            }
        });

        Button stopButton = new Button(group, SWT.NONE);
        stopButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        stopButton.setText("Stop");
        stopButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                driveEngine.stopEngine();
                conveyorPositionService.stop();
            }
        });

        Button positionControl = new Button(group, SWT.CHECK);
        positionControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        positionControl.setText("Use position Control");
        positionControl.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isRightEngine) {
                    conveyorPositionService.setExplicitRight(positionControl.getSelection());
                } else {
                    conveyorPositionService.setExplicitLeft(positionControl.getSelection());
                }
            }
        });

        return group;
    }

    @Reference(unbind = "unbindConveyorControlService")
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        ConveyorGroupFactory.conveyorService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (ConveyorGroupFactory.conveyorService == conveyorControlService) {
            ConveyorGroupFactory.conveyorService = null;
        }
    }

    @Reference(unbind = "unbindConveyorPositionService")
    public synchronized void bindConveyorPositionService(ConveyorPositionCorrectionService conveyorPositionService) {
        ConveyorGroupFactory.conveyorPositionService = conveyorPositionService;
    }

    public synchronized void unbindConveyorPositionService(ConveyorPositionCorrectionService conveyorPositionService) {
        if (ConveyorGroupFactory.conveyorPositionService == conveyorPositionService) {
            ConveyorGroupFactory.conveyorPositionService = null;
        }
    }

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        ConveyorGroupFactory.threadPool = threadPool;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        if (ConveyorGroupFactory.threadPool == threadPool) {
            ConveyorGroupFactory.threadPool = null;
        }
    }

}