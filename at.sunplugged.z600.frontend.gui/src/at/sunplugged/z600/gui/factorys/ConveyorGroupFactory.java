package at.sunplugged.z600.gui.factorys;

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
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor.StopMode;
import at.sunplugged.z600.conveyor.api.ConveyorPositionCorrectionService;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.api.EngineException;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.frontend.gui.utils.spi.RangeDoubleModifyListener;
import at.sunplugged.z600.gui.dialogs.ValueDialog;
import at.sunplugged.z600.gui.dialogs.ValueDialog.Answer;
import at.sunplugged.z600.gui.views.MainView;

@Component(immediate = true)
public final class ConveyorGroupFactory {

    private static ConveyorControlService conveyorService;

    private static ConveyorPositionCorrectionService conveyorPositionService;

    private static StandardThreadPoolService threadPool;

    private static ConveyorMonitor conveyorMonitor;

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
                    speed = Double.valueOf(speedText.getText().replace(",", "."));
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

        Group finalPositionGroup = createFinalPositionGroup(group);
        finalPositionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Button btnCalculateSpeedFromTimeUnderCathode = new Button(group, SWT.NONE);
        btnCalculateSpeedFromTimeUnderCathode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnCalculateSpeedFromTimeUnderCathode.setText("Calculate Speed from Time Under Cathode[s]");

        Text timeUnderCathodeText = new Text(group, SWT.BORDER);
        timeUnderCathodeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        timeUnderCathodeText.setText("time under cathode in [s]...");
        timeUnderCathodeText.setToolTipText("Time under cathode in [s]");

        btnCalculateSpeedFromTimeUnderCathode.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                double value = Double.valueOf(timeUnderCathodeText.getText());

                double lengthOfCathode = MainView.getSettings().getPropertAsDouble(ParameterIds.CATHODE_LENGTH_MM);
                double speed = lengthOfCathode / value;

                speedText.setText(String.format("%.3f", speed));
                conveyorService.setSetpointSpeed(speed);
            }
        });

        timeUnderCathodeText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if (timeUnderCathodeText.getText().isEmpty() == true) {
                    timeUnderCathodeText.setText("time under cathode in [s]...");
                }

            }

            @Override
            public void focusGained(FocusEvent e) {
                if (timeUnderCathodeText.getText().equals("time under cathode in [s]...")) {
                    timeUnderCathodeText.setText("");
                }
            }
        });

        timeUnderCathodeText.addModifyListener(new RangeDoubleModifyListener() {
            @Override
            protected void reactToCorrect() {
                btnCalculateSpeedFromTimeUnderCathode.setEnabled(true);
            }

            @Override
            protected void reactToError() {
                btnCalculateSpeedFromTimeUnderCathode.setEnabled(false);
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

        Button createStopCathodeHook = new Button(group, SWT.CHECK);
        createStopCathodeHook.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        createStopCathodeHook.setText("Create Stop-Powersource Hook");
        createStopCathodeHook.setToolTipText(
                "This creates a hook that will stop all powersources as soon as the conveyor stops moving. If the conveyor isn't moving no hook is created. (See log-Info)");
        createStopCathodeHook.addSelectionListener(new SelectionAdapter() {

            private MachineEventHandler machineEventHandler = new MachineEventHandler() {

                @Override
                public void handleEvent(MachineStateEvent event) {
                    if (event.getType().equals(MachineStateEvent.Type.CONVEYOR_EVENT)) {
                        ConveyorMachineEvent conveyorEvent = (ConveyorMachineEvent) event;
                        if (conveyorEvent.getConveyorEventType().equals(ConveyorMachineEvent.Type.MODE_CHANGED)) {
                            if (conveyorEvent.getValue() == ConveyorControlService.Mode.STOP) {
                                MainView.getLogService().log(LogService.LOG_INFO,
                                        "Powersource shutdown hook activated. Shutting down powersources.");
                                for (PowerSourceId id : PowerSourceId.values()) {
                                    PowerSource powerSource = MainView.getMachineStateService().getPowerSourceRegistry()
                                            .getPowerSource(id);
                                    if (powerSource.getState() != PowerSource.State.OFF) {
                                        powerSource.off();
                                    }
                                }
                                MainView.getMachineStateService().unregisterMachineEventHandler(this);
                            }
                        }
                    }
                }
            };

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (createStopCathodeHook.getSelection() == true) {
                    MainView.getMachineStateService().registerMachineEventHandler(machineEventHandler);
                    MainView.getLogService().log(LogService.LOG_INFO, "Powersource shutdown hook set.");
                }
                if (createStopCathodeHook.getSelection() == false) {
                    MainView.getMachineStateService().unregisterMachineEventHandler(machineEventHandler);
                    MainView.getLogService().log(LogService.LOG_INFO, "Powersource shutdown hook removed by user.");
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

        /*
         * TODO : Decide whether these buttons are still necessary
         * 
         * Button btnCenterLeft = new Button(positionGroup, SWT.NONE);
         * btnCenterLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
         * false, 1, 1)); btnCenterLeft.setText("Recenter Left");
         * btnCenterLeft.addSelectionListener(new SelectionAdapter() {
         * 
         * @Override public void widgetSelected(SelectionEvent e) {
         * conveyorPositionService.centerLeft(); } });
         * 
         * Button btnCenterRight = new Button(positionGroup, SWT.NONE);
         * btnCenterRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
         * false, false, 1, 1)); btnCenterRight.setText("Recenter Right");
         * btnCenterRight.addSelectionListener(new SelectionAdapter() {
         * 
         * @Override public void widgetSelected(SelectionEvent e) {
         * conveyorPositionService.centerRight(); } });
         */

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

    private static Group createFinalPositionGroup(Group parent) {

        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setText("Stop at Position");

        Button btnSetFinalPosition = new Button(group, SWT.NONE);
        btnSetFinalPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnSetFinalPosition.setText("Set stop Position [m]");
        btnSetFinalPosition.setEnabled(false);

        // Text finalPositionText = new Text(group, SWT.BORDER);
        // finalPositionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
        // true, false, 1, 1));
        // finalPositionText.setText("Final Position in [cm]...");
        // finalPositionText.setToolTipText("Final Position in [cm]");
        // finalPositionText.setEnabled(false);

        Button checkButton = new Button(group, SWT.CHECK);
        checkButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        checkButton.setSelection(false);

        Label infoLabel = new Label(group, SWT.NONE);
        infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        updateInfoLabelConveyorMonitor(infoLabel);

        // finalPositionText.addFocusListener(new FocusListener() {
        //
        // @Override
        // public void focusLost(FocusEvent e) {
        // if (finalPositionText.getText().isEmpty() == true) {
        // finalPositionText.setText("Final Position in [cm]...");
        // }
        // }
        //
        // @Override
        // public void focusGained(FocusEvent e) {
        // if (finalPositionText.getText().equals("Final Position in [cm]..."))
        // {
        // finalPositionText.setText("");
        // }
        // }
        // });
        //
        // finalPositionText.addModifyListener(new RangeDoubleModifyListener() {
        // @Override
        // protected void reactToCorrect() {
        // btnSetFinalPosition.setEnabled(true);
        // }
        //
        // @Override
        // protected void reactToError() {
        // btnSetFinalPosition.setEnabled(false);
        // }
        // });

        btnSetFinalPosition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // double value = Double.valueOf(finalPositionText.getText());
                ValueDialog valueDialog = new ValueDialog("Choose distance to stop at.",
                        "Decide at which position the conveyor should stop automatically in [m]", -Double.MAX_VALUE,
                        Double.MAX_VALUE, group.getShell());
                if (valueDialog.open() == Answer.OK) {
                    conveyorMonitor.setStopMode(StopMode.DISTANCE_REACHED);
                    conveyorMonitor.setStopPosition(valueDialog.getValue());
                    updateInfoLabelConveyorMonitor(infoLabel);
                }
            }
        });

        checkButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkButton.getSelection() == false) {
                    conveyorMonitor.setStopMode(StopMode.OFF);
                    btnSetFinalPosition.setEnabled(false);
                    // finalPositionText.setEnabled(false);
                    updateInfoLabelConveyorMonitor(infoLabel);
                } else {
                    btnSetFinalPosition.setEnabled(true);
                    // finalPositionText.setEnabled(true);
                }
            }
        });

        return group;
    }

    private static void updateInfoLabelConveyorMonitor(Label label) {
        switch (conveyorMonitor.getStopMode()) {
        case DISTANCE_REACHED:
            label.setText(String.format("Conveyor will stop at %.2f[m].", conveyorMonitor.getCurrentStopPosition()));
            break;
        case OFF:
            label.setText("Conveyor won't stop automatically.");
            break;
        case TIME_REACHED:
        default:
            label.setText("ConveyorMonitor is in time reached mode...");
            break;
        }
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
                try {
                    driveEngine.setMaximumSpeed((int) (576000 * speed / (2 * Math.PI * (80 + 3))));
                } catch (EngineException e1) {
                    MainView.getLogService().log(LogService.LOG_ERROR, "Failed to set maximum speed. Stopping Engine.");
                    driveEngine.stopEngine();
                    otherEngine.stopEngine();
                    return;
                }
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

    @Reference(unbind = "unbindConveyorMonitorService")
    public synchronized void bindConveyorMonitorService(ConveyorMonitor conveyorMonitor) {
        ConveyorGroupFactory.conveyorMonitor = conveyorMonitor;
    }

    public synchronized void unbindConveyorMonitorService(ConveyorMonitor conveyorMonitor) {
        if (ConveyorGroupFactory.conveyorMonitor == conveyorMonitor) {
            ConveyorGroupFactory.conveyorMonitor = null;
        }
    }

}