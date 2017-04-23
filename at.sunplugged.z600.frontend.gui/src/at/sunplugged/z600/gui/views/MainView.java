package at.sunplugged.z600.gui.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.backend.vaccum.api.VacuumService;
import at.sunplugged.z600.backend.vaccum.api.VacuumService.Interlocks;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.FileAccessUtils;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorPositionCorrectionService;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.WaterControl.WaterOutlet;
import at.sunplugged.z600.frontend.gui.utils.spi.UpdatableChart;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptInterpreterService;
import at.sunplugged.z600.gui.dialogs.ValueDialog;
import at.sunplugged.z600.gui.factorys.ConveyorGroupFactory;
import at.sunplugged.z600.gui.factorys.PowerSupplyBasicFactory;
import at.sunplugged.z600.gui.factorys.SystemOutputFactory;
import at.sunplugged.z600.gui.machinediagram.Viewer;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;
import org.eclipse.swt.widgets.Text;

@Component
public class MainView {

    protected Shell shell;

    private static LogService logService;

    private static SrmCommunicator srmCommunicator;

    private static MbtService mbtController;

    private static MachineStateService machineStateService;

    private static ConveyorControlService conveyorControlService;

    public static ConveyorControlService getConveyorControlService() {
        return conveyorControlService;
    }

    private static ConveyorPositionCorrectionService conveyorPositionCorrectionService;

    public static ConveyorPositionCorrectionService getConveyorPositionCorrectionService() {
        return conveyorPositionCorrectionService;
    }

    private static StandardThreadPoolService threadPool;

    private static VacuumService vacuumService;

    private static SettingsService settings;

    public static SettingsService getSettings() {
        return settings;
    }

    private static ScriptInterpreterService scriptInterpreterService;

    private static DataService dataService;

    private static BundleContext context;

    private static Viewer diagramViewer;
    private static Text textAnalogOutput;
    private static Text textAnalogInput;

    public static LogService getLogService() {
        return logService;
    }

    public SrmCommunicator getSrmCommunicator() {
        return srmCommunicator;
    }

    public MbtService getMbtController() {
        return mbtController;
    }

    public static StandardThreadPoolService getStandardThreadPoolService() {
        return threadPool;
    }

    public static BundleContext getContext() {
        return MainView.context;
    }

    @Activate
    public synchronized void activateGui(BundleContext context) {
        MainView.context = context;
    }

    public static Shell createMainWindow() {
        Shell shell = createContents();
        return shell;
    }

    /**
     * Create contents of the window.
     * 
     * @wbp.parser.entryPoint
     */
    protected static Shell createContents() {
        Shell shell = new Shell();
        shell.setMinimumSize(new Point(1024, 764));
        shell.setSize(800, 600);
        shell.setText("SWT Application");
        shell.setLayout(new GridLayout(2, false));

        Composite machineDiagramComposite = new Composite(shell, SWT.NONE);
        machineDiagramComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        GridData gd_machineDiagramComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_machineDiagramComposite.widthHint = 550;
        machineDiagramComposite.setLayoutData(gd_machineDiagramComposite);

        Canvas canvas = new Canvas(machineDiagramComposite, SWT.NONE);

        diagramViewer = new Viewer(canvas);
        machineStateService.registerMachineEventHandler(diagramViewer);

        TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
        tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
        GridData gdTabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
        gdTabFolder.heightHint = 718;
        gdTabFolder.widthHint = 438;
        tabFolder.setLayoutData(gdTabFolder);

        TabItem tbtmMain = new TabItem(tabFolder, SWT.NONE);
        tbtmMain.setText("Main");

        Composite composite_1 = new Composite(tabFolder, SWT.NONE);
        tbtmMain.setControl(composite_1);
        composite_1.setLayout(new GridLayout(1, false));

        Group groupVacuum = new Group(composite_1, SWT.NONE);
        groupVacuum.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        groupVacuum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        groupVacuum.setText("Vakuum");
        groupVacuum.setLayout(new GridLayout(2, true));

        Button btnCryoEins = new Button(groupVacuum, SWT.CHECK);
        btnCryoEins.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                vacuumService.setInterlock(Interlocks.CRYO_ONE, btnCryoEins.getSelection());
            }
        });
        btnCryoEins.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        btnCryoEins.setText("Cryo Eins");

        Button btnCryoZwei = new Button(groupVacuum, SWT.CHECK);
        btnCryoZwei.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                vacuumService.setInterlock(Interlocks.CRYO_TWO, btnCryoZwei.getSelection());
            }
        });
        btnCryoZwei.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        btnCryoZwei.setText("Cryo Zwei");

        Button btnEvakuieren = new Button(groupVacuum, SWT.NONE);
        btnEvakuieren.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
        btnEvakuieren.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                VacuumService.State state = vacuumService.getState();
                if (state == VacuumService.State.READY) {
                    vacuumService.startEvacuation();
                } else {
                    vacuumService.shutdown();
                }
            }
        });
        Display.getDefault().timerExec(500, new Runnable() {
            @Override
            public void run() {
                VacuumService.State state = vacuumService.getState();
                switch (state) {
                case READY:
                    setBtnText("Start", state);
                    break;
                case STARTING:
                case EVACUATING:
                case PRESSURE_CONTROL_RUNNING:
                    setBtnText("Stopp", state);
                    break;
                case FAILED:
                    btnEvakuieren.setText("Vacuum Service failed!");
                    break;
                default:
                    btnEvakuieren.setText(String.format("Unexpected State: %s", state.toString()));
                    break;
                }
                Display.getDefault().timerExec(500, this);
            }

            private void setBtnText(String prefix, VacuumService.State state) {
                btnEvakuieren.setText(String.format("%s --- State: %s", prefix, state.toString()));
            }
        });

        GridData gd_btnEvakuieren = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_btnEvakuieren.heightHint = 50;
        btnEvakuieren.setLayoutData(gd_btnEvakuieren);
        btnEvakuieren.setText("Start");

        Label lblGasfluss = new Label(groupVacuum, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblGasfluss.setText("Gasfluss");
        GridData gd_lblGasfluss = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_lblGasfluss.heightHint = 20;
        lblGasfluss.setLayoutData(gd_lblGasfluss);

        Label lblLabelsetpointpressure = new Label(groupVacuum, SWT.NONE);
        lblLabelsetpointpressure.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
        lblLabelsetpointpressure.setText("labelSetpointPressure");
        lblLabelsetpointpressure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Display.getDefault().timerExec(500, new Runnable() {
            @Override
            public void run() {
                GasFlowControl gasFlowControl = machineStateService.getGasFlowControl();
                GasFlowControl.State state = gasFlowControl.getState();
                lblLabelsetpointpressure.setText(String.format("%s --- %.4f --- %.4f [mBar]", state.toString(),
                        gasFlowControl.getCurrentGasFlowValue(), gasFlowControl.getGasflowDesiredPressure()));
                Display.getDefault().timerExec(500, this);
            }
        });

        Button btnSetpressure = new Button(groupVacuum, SWT.NONE);
        btnSetpressure.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ValueDialog dialog = new ValueDialog("Druck", "Gewünschten Druck in [mbar] setzen.",
                        settings.getPropertAsDouble(ParameterIds.VACUUM_LOWER_LIMIT_MBAR),
                        settings.getPropertAsDouble(ParameterIds.VACUUM_UPPER_LIMIT_MBAR), shell);
                if (dialog.open() == ValueDialog.Answer.OK) {
                    vacuumService.setSetpointPressure(dialog.getValue());
                }
            }
        });

        btnSetpressure.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        btnSetpressure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSetpressure.setText("Druck setzen");

        Button btnStartPresssureControl = new Button(groupVacuum, SWT.NONE);
        GridData gd_btnStartPresssureControl = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gd_btnStartPresssureControl.heightHint = 50;
        btnStartPresssureControl.setLayoutData(gd_btnStartPresssureControl);
        btnStartPresssureControl.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        btnStartPresssureControl.setText("Start/Stop");
        btnStartPresssureControl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                VacuumService.State state = vacuumService.getState();
                if (state != VacuumService.State.PRESSURE_CONTROL_RUNNING) {
                    vacuumService.startPressureControl();
                } else {
                    vacuumService.stopPressureControl();
                }
            }

        });
        new Label(groupVacuum, SWT.NONE);
        new Label(groupVacuum, SWT.NONE);

        Group grpBandlauf = new Group(composite_1, SWT.NONE);
        grpBandlauf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpBandlauf.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        grpBandlauf.setText("Bandlauf");
        grpBandlauf.setLayout(new GridLayout(2, true));

        Label lblLabelsetpointspeed = new Label(grpBandlauf, SWT.NONE);
        lblLabelsetpointspeed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblLabelsetpointspeed.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        lblLabelsetpointspeed.setText(String.format("%.2f [mm/s]", conveyorControlService.getSetpointSpeed()));

        Button btnSet = new Button(grpBandlauf, SWT.NONE);
        btnSet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSet.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        btnSet.setText("V setzen [mm/s]");
        btnSet.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ValueDialog dialog = new ValueDialog("Bandlauf Geschwindigkeit",
                        "Bandlauf Geschwindigkeit in [mm/s] setzen.", 0, 30, shell);
                if (dialog.open() == ValueDialog.Answer.OK) {
                    conveyorControlService.setSetpointSpeed(dialog.getValue());
                    lblLabelsetpointspeed.setText(dialog.getValue() + " [mm/s]");
                }
            }

        });

        Composite grpControl = new Composite(grpBandlauf, SWT.NONE);
        grpControl.setLayout(new GridLayout(3, true));
        grpControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        grpControl.setFont(SWTResourceManager.getFont("Segoe UI", 13, SWT.NORMAL));

        Button button = new Button(grpControl, SWT.NONE);
        GridData gd_button = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_button.heightHint = 40;
        button.setLayoutData(gd_button);
        button.setText("<< Rechts nach Links");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.start(conveyorControlService.getSetpointSpeed(), Mode.RIGHT_TO_LEFT);
            }

        });

        Button btnStop = new Button(grpControl, SWT.NONE);
        GridData gd_btnStop = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnStop.heightHint = 40;
        btnStop.setLayoutData(gd_btnStop);
        btnStop.setText("Stopp");
        btnStop.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.stop();
            }

        });

        Button btnLinksNachRechts = new Button(grpControl, SWT.NONE);
        GridData gd_btnLinksNachRechts = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnLinksNachRechts.heightHint = 40;
        btnLinksNachRechts.setLayoutData(gd_btnLinksNachRechts);
        btnLinksNachRechts.setText("Links nach Rechts >>");
        btnLinksNachRechts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.start(conveyorControlService.getSetpointSpeed(), Mode.LEFT_TO_RIGHT);
            }
        });
        new Label(grpBandlauf, SWT.NONE);
        new Label(grpBandlauf, SWT.NONE);

        Group grpSkriptAusfhrung = new Group(composite_1, SWT.NONE);
        grpSkriptAusfhrung.setLayout(new GridLayout(2, false));
        grpSkriptAusfhrung.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpSkriptAusfhrung.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        grpSkriptAusfhrung.setText("Skript Ausf\u00FChrung");

        Combo combo = new Combo(grpSkriptAusfhrung, SWT.NONE);
        combo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo.setText("Skripte...");
        combo.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                String[] scriptNames = FileAccessUtils.getScriptNames();
                if (scriptNames.length == 0) {
                    combo.setItems("");
                    combo.setText("No scripts found...");
                } else {

                    combo.setItems(FileAccessUtils.getScriptNames());

                }
            }
        });

        Button btnAusfhren = new Button(grpSkriptAusfhrung, SWT.NONE);
        btnAusfhren.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        GridData gd_btnAusfhren = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnAusfhren.widthHint = 120;
        btnAusfhren.setLayoutData(gd_btnAusfhren);
        btnAusfhren.setText("Ausf\u00FChren");
        btnAusfhren.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = combo.getSelectionIndex();
                if (selectedIndex != -1) {
                    String scriptName = combo.getItem(combo.getSelectionIndex());
                    if (scriptName.matches(".+\\.sc$")) {
                        try {
                            try {
                                scriptInterpreterService.executeScript(FileAccessUtils.getScriptByName(scriptName));
                            } catch (ParseError e1) {
                                MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
                                messageBox.setText("Failed to parse Script");
                                messageBox.setMessage(e1.getMessage());
                                messageBox.open();
                            }
                        } catch (IOException e1) {
                            logService.log(LogService.LOG_ERROR, String.format(
                                    "Failed to execute Script \"%s\". Unable to load from file.", scriptName), e1);
                        }
                    }
                }

            }
        });

        Label lblCurrentinstruction = new Label(grpSkriptAusfhrung, SWT.NONE);
        lblCurrentinstruction.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        lblCurrentinstruction.setText("currentInstruction");
        lblCurrentinstruction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Display.getDefault().timerExec(300, new Runnable() {

            @Override
            public void run() {
                String currentCommand = scriptInterpreterService.getCurrentCommandName();
                if (currentCommand != null) {
                    lblCurrentinstruction.setText(scriptInterpreterService.getCurrentCommandName());
                } else {
                    lblCurrentinstruction.setText("Ready");
                }

                Display.getDefault().timerExec(300, this);
            }

        });

        Button btnStoppen = new Button(grpSkriptAusfhrung, SWT.NONE);
        btnStoppen.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        btnStoppen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStoppen.setText("Stoppen");
        btnStoppen.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                scriptInterpreterService.stopExecution();
            }
        });

        Group grpSql = new Group(composite_1, SWT.NONE);
        grpSql.setLayout(new GridLayout(1, false));
        grpSql.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpSql.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        grpSql.setText("SQL");

        Button btnStartSqlLogging = new Button(grpSql, SWT.NONE);
        btnStartSqlLogging.addSelectionListener(new SelectionAdapter() {
            boolean state = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (state == false) {
                    try {
                        dataService.startUpdate();
                        state = true;
                        btnStartSqlLogging.setText("Stop SQL Logging");
                    } catch (DataServiceException e1) {
                        logService.log(LogService.LOG_ERROR, "Failed to start sql logging.", e1);
                    }
                } else {
                    dataService.stopUpdate();
                    state = false;
                    btnStartSqlLogging.setText("Start SQL Logging");
                }
            }
        });
        btnStartSqlLogging.setFont(SWTResourceManager.getFont("Segoe UI", 13, SWT.NORMAL));
        GridData gd_btnStartSqlLogging = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_btnStartSqlLogging.heightHint = 40;
        btnStartSqlLogging.setLayoutData(gd_btnStartSqlLogging);
        btnStartSqlLogging.setText("Start SQL Logging");

        Label label = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label label_1 = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label label_2 = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label label_4 = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label label_3 = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Button btnNotAus = new Button(composite_1, SWT.NONE);
        btnNotAus.setFont(SWTResourceManager.getFont("Segoe UI", 24, SWT.NORMAL));
        btnNotAus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        btnNotAus.setText("Not Aus");
        btnNotAus.setEnabled(false);

        // TabItem tbtmVacuum = new TabItem(tabFolder, SWT.NONE);
        // tbtmVacuum.setText("Vacuum");
        //
        // Composite VacuumComposite =
        // VacuumTabitemFactory.createComposite(tabFolder);
        // tbtmVacuum.setControl(VacuumComposite);
        // VacuumComposite.setLayout(new GridLayout(1, false));

        TabItem tabItemLog = new TabItem(tabFolder, SWT.NONE);
        tabItemLog.setText("Log");

        Composite logComposite = new Composite(tabFolder, SWT.NONE);
        logComposite.setLayout(new GridLayout(1, false));
        tabItemLog.setControl(logComposite);
        Group logLevelgroup = new Group(logComposite, SWT.NONE);
        logLevelgroup.setText("Log Levels");
        logLevelgroup.setLayout(new GridLayout(4, true));

        Button infoCheck = new Button(logLevelgroup, SWT.CHECK);
        infoCheck.setText("Log Info");
        infoCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        infoCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (infoCheck.getSelection() == true) {
                    SystemOutputFactory.activateLogLevel(LogService.LOG_INFO);
                } else {
                    SystemOutputFactory.deactivateLogLevel(LogService.LOG_INFO);
                }
            }
        });
        SystemOutputFactory.activateLogLevel(LogService.LOG_INFO);
        infoCheck.setSelection(true);

        Button warningCheck = new Button(logLevelgroup, SWT.CHECK);
        warningCheck.setText("Log Warning");
        warningCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        warningCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (warningCheck.getSelection() == true) {
                    SystemOutputFactory.activateLogLevel(LogService.LOG_WARNING);
                } else {
                    SystemOutputFactory.deactivateLogLevel(LogService.LOG_WARNING);
                }
            }
        });
        SystemOutputFactory.activateLogLevel(LogService.LOG_WARNING);
        warningCheck.setSelection(true);

        Button errorCheck = new Button(logLevelgroup, SWT.CHECK);
        errorCheck.setText("Log Error");
        errorCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        errorCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (errorCheck.getSelection() == true) {
                    SystemOutputFactory.activateLogLevel(LogService.LOG_ERROR);
                } else {
                    SystemOutputFactory.deactivateLogLevel(LogService.LOG_ERROR);
                }
            }
        });
        SystemOutputFactory.activateLogLevel(LogService.LOG_ERROR);
        errorCheck.setSelection(true);

        Button debugCheck = new Button(logLevelgroup, SWT.CHECK);
        debugCheck.setText("Log Debug");
        debugCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        debugCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (debugCheck.getSelection() == true) {
                    SystemOutputFactory.activateLogLevel(LogService.LOG_DEBUG);
                } else {
                    SystemOutputFactory.deactivateLogLevel(LogService.LOG_DEBUG);
                }
            }
        });
        SystemOutputFactory.deactivateLogLevel(LogService.LOG_DEBUG);
        debugCheck.setSelection(false);

        SystemOutputFactory.createStyledText(logComposite);

        TabItem conveyorDebugTabItem = new TabItem(tabFolder, SWT.NONE);
        conveyorDebugTabItem.setText("Conveyor");

        Composite conveyorDebugComposite = new Composite(tabFolder, SWT.NONE);
        conveyorDebugComposite.setLayout(new GridLayout(1, false));
        conveyorDebugTabItem.setControl(conveyorDebugComposite);
        ConveyorGroupFactory.createGroup(conveyorDebugComposite);

        TabItem tbtmPowerSupply = new TabItem(tabFolder, SWT.NONE);
        tbtmPowerSupply.setText("Power Supply");
        Composite powerSupplyComposite = new Composite(tabFolder, SWT.NONE);

        powerSupplyComposite.setLayout(new GridLayout(1, false));
        tbtmPowerSupply.setControl(powerSupplyComposite);

        PowerSupplyBasicFactory.createPowerSupplyGroup(powerSupplyComposite, PowerSourceId.PINNACLE);
        PowerSupplyBasicFactory.createPowerSupplyGroup(powerSupplyComposite, PowerSourceId.SSV1);
        PowerSupplyBasicFactory.createPowerSupplyGroup(powerSupplyComposite, PowerSourceId.SSV2);

        TabItem tbtmScriptpage = new TabItem(tabFolder, SWT.NONE);
        tbtmScriptpage.setText("ScriptPage");

        Composite compositeScriptPage = new Composite(tabFolder, SWT.NONE);
        tbtmScriptpage.setControl(compositeScriptPage);
        compositeScriptPage.setLayout(new GridLayout(1, false));

        Group group = new Group(compositeScriptPage, SWT.NONE);
        group.setLayout(new GridLayout(3, true));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnExecuteScript = new Button(group, SWT.NONE);
        btnExecuteScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnExecuteScript.setText("Execute Script");

        Button btnLoadScript = new Button(group, SWT.NONE);
        btnLoadScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnLoadScript.setText("Load Script");

        Button btnSaveScript = new Button(group, SWT.NONE);
        btnSaveScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSaveScript.setText("Save Script");

        Label lblCurrentCommand = new Label(group, SWT.NONE);
        lblCurrentCommand.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        lblCurrentCommand.setText("Current Command:");

        Button btnStopExecution = new Button(group, SWT.NONE);
        btnStopExecution.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                scriptInterpreterService.stopExecution();
            }
        });
        btnStopExecution.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStopExecution.setText("Stop Execution");
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);

        StyledText styledTextScriptInput = new StyledText(compositeScriptPage, SWT.BORDER);
        styledTextScriptInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        styledTextScriptInput.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                styledTextScriptInput.setStyleRange(new StyleRange(0, styledTextScriptInput.getCharCount(),
                        SWTResourceManager.getColor(SWT.COLOR_BLACK), SWTResourceManager.getColor(SWT.COLOR_WHITE)));
                styledTextScriptInput.setToolTipText("");
                try {
                    scriptInterpreterService.checkScript(styledTextScriptInput.getText());
                } catch (ParseError e1) {
                    if (e1.getLine() > -1) {
                        int start = styledTextScriptInput.getOffsetAtLine(e1.getLine());
                        int length = styledTextScriptInput.getLine(e1.getLine()).length();
                        styledTextScriptInput.setStyleRange(
                                new StyleRange(start, length, SWTResourceManager.getColor(SWT.COLOR_BLACK),
                                        SWTResourceManager.getColor(SWT.COLOR_RED)));
                        styledTextScriptInput.setToolTipText(e1.getMessage());
                    }
                }
            }
        });

        btnLoadScript.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
                fd.setText("Load");

                String[] filter = { "*.sc" };
                fd.setFilterExtensions(filter);
                String selected = fd.open();
                if (selected == null) {
                    return;
                }
                try (BufferedReader reader = new BufferedReader(new FileReader(selected))) {
                    StringBuilder sb = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = reader.readLine();
                    }
                    styledTextScriptInput.setText(sb.toString());

                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to load file.", e1);
                }
            }

        });

        btnSaveScript.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
                fd.setText("Save");

                String[] filter = { "*.sc" };
                fd.setFilterExtensions(filter);
                String selected = fd.open();
                if (selected == null) {
                    return;
                }
                try {
                    File file = new File(selected);
                    if (!file.exists()) {
                        file.createNewFile();
                    } else {
                        file.delete();
                        file.createNewFile();
                    }
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to save script.", e1);
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selected))) {
                    writer.write(styledTextScriptInput.getText());

                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to save script.", e1);
                }
            }

        });

        btnExecuteScript.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    scriptInterpreterService.executeScript(styledTextScriptInput.getText());
                } catch (ParseError e1) {
                    MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
                    messageBox.setText("Failed to parse Script");
                    messageBox.setMessage(e1.getMessage());
                    messageBox.open();

                }
            }

        });
        Display.getDefault().timerExec(500, new Runnable() {

            @Override
            public void run() {
                String currentCommand = scriptInterpreterService.getCurrentCommandName();
                if (currentCommand != null) {
                    lblCurrentCommand.setText("Current: " + currentCommand);
                } else {
                    lblCurrentCommand.setText("Ready");
                }
                Display.getDefault().timerExec(500, this);

            }

        });

        new Label(shell, SWT.NONE);

        TabItem tbtmMachinedebug = new TabItem(tabFolder, SWT.NONE);
        tbtmMachinedebug.setText("MachineDebug");
        tabFolder.addSelectionListener(new SelectionAdapter() {

            boolean done = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (done == false) {
                    if (tabFolder.getSelection()[0] == tbtmMachinedebug) {
                        MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
                        messageBox.setText("Debug Warning");
                        messageBox.setMessage(
                                "Using functions in this view may result in unexpected behaviour of the program and the machine. Use with caution!");
                        int answer = messageBox.open();
                        if (answer == SWT.OK) {
                            done = true;
                        } else {
                            tabFolder.setSelection(0);
                        }

                    }
                }
            }
        });

        Composite machineDebugComposite = createMachineDebugViewComposite(tabFolder);
        tbtmMachinedebug.setControl(machineDebugComposite);

        return shell;
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService logService) {
        MainView.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (MainView.logService == logService) {
            MainView.logService = null;
        }
    }

    @Reference(unbind = "unbindSrmCommunicator", cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindSrmCommunicator(SrmCommunicator srmCommunicator) {
        MainView.srmCommunicator = srmCommunicator;
    }

    public synchronized void unbindSrmCommunicator(SrmCommunicator srmCommunicator) {
        if (MainView.srmCommunicator == srmCommunicator) {
            MainView.srmCommunicator = null;
        }
    }

    @Reference(unbind = "unbindMBTController")
    public synchronized void bindMBTController(MbtService mbtController) {
        MainView.mbtController = mbtController;
    }

    public synchronized void unbindMBTController(MbtService mbtController) {
        if (MainView.mbtController == mbtController) {
            MainView.mbtController = null;
        }
    }

    @Reference(unbind = "unbindMachineStateService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        MainView.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (MainView.machineStateService == machineStateService) {
            MainView.machineStateService = null;
        }
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

    @Reference(unbind = "unbindConveyorControlService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        MainView.conveyorControlService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (MainView.conveyorControlService == conveyorControlService) {
            MainView.conveyorControlService = null;
        }
    }

    @Reference(unbind = "unbindStandardThreadPoolService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService service) {
        threadPool = service;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService service) {
        if (threadPool.equals(service)) {
            threadPool = null;
        }
    }

    @Reference(unbind = "unbindSettingsService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindSettingsService(SettingsService settingsService) {
        settings = settingsService;
    }

    public synchronized void unbindSettingsService(SettingsService settingsService) {
        if (settings.equals(settingsService)) {
            settings = null;
        }
    }

    @Reference(unbind = "unbindVacuumService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindVacuumService(VacuumService service) {
        vacuumService = service;
    }

    public synchronized void unbindVacuumService(VacuumService service) {
        if (vacuumService == service) {
            vacuumService = null;
        }
    }

    public static VacuumService getVacuumService() {
        return vacuumService;
    }

    @Reference(unbind = "unbindScriptInterpreterService")
    public synchronized void bindScriptInterpreterService(ScriptInterpreterService scriptInterpreterService) {
        MainView.scriptInterpreterService = scriptInterpreterService;
    }

    public synchronized void unbindScriptInterpreterService(ScriptInterpreterService scriptInterpreterService) {
        if (MainView.scriptInterpreterService == scriptInterpreterService) {
            MainView.scriptInterpreterService = null;
        }
    }

    @Reference(unbind = "unbindDataService")
    public synchronized void bindDataService(DataService dataService) {
        MainView.dataService = dataService;
    }

    public synchronized void unbindDataService(DataService dataService) {
        if (MainView.dataService == dataService) {
            MainView.dataService = null;
        }
    }

    @Reference(unbind = "unbindConveyorPositionCorrectionService")
    public synchronized void bindConveyorPositionCorrectionService(
            ConveyorPositionCorrectionService conveyorPositionCorrectionService) {
        MainView.conveyorPositionCorrectionService = conveyorPositionCorrectionService;
    }

    public synchronized void unbindConveyorPositionCorrectionService(
            ConveyorPositionCorrectionService conveyorPositionCorrectionService) {
        if (MainView.conveyorPositionCorrectionService == conveyorPositionCorrectionService) {
            MainView.conveyorPositionCorrectionService = null;
        }
    }

    private final static class OutletAdapter extends SelectionAdapter {

        private final Outlet outlet;

        private int lastClick = 0;

        public OutletAdapter(Outlet outlet) {
            this.outlet = outlet;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                if (lastClick == 0) {

                    machineStateService.getOutletControl().closeOutlet(outlet);
                    lastClick = 1;
                } else {
                    machineStateService.getOutletControl().openOutlet(outlet);
                    lastClick = 0;
                }
            } catch (IOException e1) {
                logService.log(LogService.LOG_ERROR, "Failed to open outlet from machineDebugView.", e1);
            }
        }
    }

    private final static class PumpAdapter extends SelectionAdapter {

        private final Pump pump;

        public PumpAdapter(PumpIds pumpId) {
            this.pump = machineStateService.getPumpRegistry().getPump(pumpId);
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (pump.getState() == PumpState.OFF) {
                pump.startPump();
            } else {
                pump.stopPump();
            }
        }

    }

    private static Composite createMachineDebugViewComposite(TabFolder tabFolder) {

        Composite machineDebugComposite = new Composite(tabFolder, SWT.NONE);
        machineDebugComposite.setLayout(new GridLayout(3, true));

        Button toggelOutletOne = new Button(machineDebugComposite, SWT.NONE);
        toggelOutletOne.addSelectionListener(new OutletAdapter(Outlet.OUTLET_ONE));
        toggelOutletOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        toggelOutletOne.setText("V1");

        Button toggelOutletTwo = new Button(machineDebugComposite, SWT.NONE);
        toggelOutletTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletTwo.setText("V2");
        toggelOutletTwo.addSelectionListener(new OutletAdapter(Outlet.OUTLET_TWO));

        Button toggelOutletThree = new Button(machineDebugComposite, SWT.NONE);
        toggelOutletThree.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletThree.setText("V3");
        toggelOutletThree.addSelectionListener(new OutletAdapter(Outlet.OUTLET_THREE));

        Button toggleOutletFour = new Button(machineDebugComposite, SWT.NONE);
        toggleOutletFour.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletFour.setText("V4");
        toggleOutletFour.addSelectionListener(new OutletAdapter(Outlet.OUTLET_FOUR));

        Button toggelOutletFive = new Button(machineDebugComposite, SWT.NONE);
        toggelOutletFive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletFive.setText("V5");
        toggelOutletFive.addSelectionListener(new OutletAdapter(Outlet.OUTLET_FIVE));

        Button toggleOutletSix = new Button(machineDebugComposite, SWT.NONE);
        toggleOutletSix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletSix.setText("V6");
        toggleOutletSix.addSelectionListener(new OutletAdapter(Outlet.OUTLET_SIX));

        Button toggleOutletSeven = new Button(machineDebugComposite, SWT.NONE);
        toggleOutletSeven.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletSeven.setText("V7");
        toggleOutletSeven.addSelectionListener(new OutletAdapter(Outlet.OUTLET_SEVEN));

        Button toggleOutletEight = new Button(machineDebugComposite, SWT.NONE);
        toggleOutletEight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletEight.setText("V8");
        toggleOutletEight.addSelectionListener(new OutletAdapter(Outlet.OUTLET_EIGHT));

        Button toggelOutletNine = new Button(machineDebugComposite, SWT.NONE);
        toggelOutletNine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletNine.setText("V9");
        toggelOutletNine.addSelectionListener(new OutletAdapter(Outlet.OUTLET_NINE));

        Button togglePrePumpOne = new Button(machineDebugComposite, SWT.NONE);
        togglePrePumpOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        togglePrePumpOne.setText("Toggle Pre Pump Pone");
        togglePrePumpOne.addSelectionListener(new PumpAdapter(PumpIds.PRE_PUMP_ONE));

        Button togglePrePumpRoots = new Button(machineDebugComposite, SWT.NONE);
        togglePrePumpRoots.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        togglePrePumpRoots.setText("Toggle Pre Pump Roots");
        togglePrePumpRoots.addSelectionListener(new PumpAdapter(PumpIds.PRE_PUMP_ROOTS));

        Button togglepPrePumpTwo = new Button(machineDebugComposite, SWT.NONE);
        togglepPrePumpTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        togglepPrePumpTwo.setText("Toggle Pre Pump Two");
        togglepPrePumpTwo.addSelectionListener(new PumpAdapter(PumpIds.PRE_PUMP_TWO));

        Button toggleTurboPump = new Button(machineDebugComposite, SWT.NONE);
        toggleTurboPump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleTurboPump.setText("Toggle Turbo Pump");
        toggleTurboPump.addSelectionListener(new PumpAdapter(PumpIds.TURBO_PUMP));

        Button toggleCryoOne = new Button(machineDebugComposite, SWT.NONE);
        toggleCryoOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleCryoOne.setText("Toggle Cryo One");
        toggleCryoOne.addSelectionListener(new PumpAdapter(PumpIds.CRYO_ONE));

        Button toggleCryoTwo = new Button(machineDebugComposite, SWT.NONE);
        toggleCryoTwo.setText("Toggle Cryo Two");
        toggleCryoTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath1Label = new Label(machineDebugComposite, SWT.NONE);
        waterKath1Label.setText("Water KATH1: false");
        waterKath1Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath2Label = new Label(machineDebugComposite, SWT.NONE);
        waterKath2Label.setText("Water KATH2: false");
        waterKath2Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath3Label = new Label(machineDebugComposite, SWT.NONE);
        waterKath3Label.setText("Water KATH3: false");
        waterKath3Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath4Label = new Label(machineDebugComposite, SWT.NONE);
        waterKath4Label.setText("Water KATH4: false");
        waterKath4Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Display.getDefault().timerExec(500, new Runnable() {

            @Override
            public void run() {
                waterKath1Label.setText("LS Left Back: "
                        + machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_LEFT_BACK));

                waterKath2Label.setText("LS Left Front: "
                        + machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_LEFT_FRONT));

                waterKath3Label.setText("LS Right Back: "
                        + machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_RIGHT_BACK));

                waterKath4Label.setText("LS Right Front: "
                        + machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_RIGHT_FRONT));
                Display.getDefault().timerExec(500, this);
            }

        });

        Button toggleWaterTurboPump = new Button(machineDebugComposite, SWT.NONE);
        toggleWaterTurboPump.setText("Toggle Water Turbo");
        toggleWaterTurboPump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleWaterTurboPump.addSelectionListener(new SelectionAdapter() {
            private boolean lastState = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.TURBO_PUMP, !lastState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                lastState = !lastState;
            }

        });

        Button toggleWaterKath1Pump = new Button(machineDebugComposite, SWT.NONE);
        toggleWaterKath1Pump.setText("Toggle Water Kath1");
        toggleWaterKath1Pump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleWaterKath1Pump.addSelectionListener(new SelectionAdapter() {
            private boolean lastState = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.KATH_ONE, !lastState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                lastState = !lastState;
            }

        });

        Button toggleWaterKath2Pump = new Button(machineDebugComposite, SWT.NONE);
        toggleWaterKath2Pump.setText("Toggle Water Kath2");
        toggleWaterKath2Pump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleWaterKath2Pump.addSelectionListener(new SelectionAdapter() {
            private boolean lastState = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.KATH_TWO, !lastState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                lastState = !lastState;
            }

        });

        Button toggleWaterKath3Pump = new Button(machineDebugComposite, SWT.NONE);
        toggleWaterKath3Pump.setText("Toggle Water Kath3");
        toggleWaterKath3Pump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleWaterKath3Pump.addSelectionListener(new SelectionAdapter() {
            private boolean lastState = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.KATH_THREE, !lastState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                lastState = !lastState;
            }

        });

        Button toggleWaterShieldPump = new Button(machineDebugComposite, SWT.NONE);
        toggleWaterShieldPump.setText("Toggle Water Shield");
        toggleWaterShieldPump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleWaterShieldPump.addSelectionListener(new SelectionAdapter() {
            private boolean lastState = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.SHIELD, !lastState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                lastState = !lastState;
            }

        });

        Button toggleDigOut = new Button(machineDebugComposite, SWT.NONE);
        toggleDigOut.setText("Toggle Dig Out [2][0]");
        toggleDigOut.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(machineDebugComposite, SWT.NONE);
        new Label(machineDebugComposite, SWT.NONE);
        toggleDigOut.addSelectionListener(new SelectionAdapter() {
            private boolean lastState = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    mbtController.writeDigOut(WagoAddresses.DigitalOutput.SUPPLY_CONVEYOR_MEASURMENT.getAddress(),
                            !lastState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                lastState = !lastState;
            }

        });

        Button startSql = new Button(machineDebugComposite, SWT.NONE);
        startSql.setText("Start SQL");
        startSql.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        startSql.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    dataService.startUpdate();
                } catch (DataServiceException e1) {
                    e1.printStackTrace();
                }
            }

        });

        Button stopSql = new Button(machineDebugComposite, SWT.NONE);
        stopSql.setText("Stop SQL");
        stopSql.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(machineDebugComposite, SWT.NONE);

        Group grpDigitalIo = new Group(machineDebugComposite, SWT.NONE);
        grpDigitalIo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        grpDigitalIo.setText("Digital I/O");
        grpDigitalIo.setLayout(new GridLayout(2, false));

        Combo digitalOutputCombo = new Combo(grpDigitalIo, SWT.NONE);
        digitalOutputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        digitalOutputCombo.setText("DigitalOutput");
        List<String> digitalOutputAddresses = new ArrayList<>();
        for (DigitalOutput digitalOutput : WagoAddresses.DigitalOutput.values()) {
            digitalOutputAddresses.add(digitalOutput.toString());
        }
        digitalOutputCombo.setItems(digitalOutputAddresses.toArray(new String[0]));

        Button digitalOutputCheck = new Button(grpDigitalIo, SWT.CHECK | SWT.CENTER);
        digitalOutputCheck.setAlignment(SWT.LEFT);
        GridData gd_digitalOutputCheck = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_digitalOutputCheck.widthHint = 50;
        digitalOutputCheck.setLayoutData(gd_digitalOutputCheck);
        digitalOutputCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    digitalOutputCheck.setEnabled(true);
                    digitalOutputCheck.setSelection(machineStateService
                            .readDigitalOutput(WagoAddresses.DigitalOutput.valueOf(digitalOutputCombo.getText())));
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to read digitalOutput for machineDebug view.", e1);
                    digitalOutputCheck.setEnabled(false);
                }
            }
        });

        digitalOutputCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    machineStateService.writeDigitalOutput(
                            WagoAddresses.DigitalOutput.valueOf(digitalOutputCombo.getText()),
                            digitalOutputCheck.getSelection());
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to write DigitalOutput for machineDebug view.", e1);
                }
            }

        });

        Combo digitalInputCombo = new Combo(grpDigitalIo, SWT.NONE);
        digitalInputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        digitalInputCombo.setText("DigitalInput");

        List<String> digitalInputList = new ArrayList<>();
        for (DigitalInput digitalInput : WagoAddresses.DigitalInput.values()) {
            digitalInputList.add(digitalInput.toString());
        }

        digitalInputCombo.setItems(digitalInputList.toArray(new String[0]));

        Button digitalInputCheck = new Button(grpDigitalIo, SWT.CHECK);
        digitalInputCheck.setEnabled(false);
        digitalInputCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    digitalInputCheck.setSelection(machineStateService
                            .readDigitalIntput(WagoAddresses.DigitalInput.valueOf(digitalInputCombo.getText())));
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to read DigitalInput for machineDebugView.", e1);
                }

            }
        });

        Group grpAnalogIo = new Group(machineDebugComposite, SWT.NONE);
        grpAnalogIo.setLayout(new GridLayout(2, false));
        grpAnalogIo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        grpAnalogIo.setText("Analog I/O");

        Combo analogOutputCombo = new Combo(grpAnalogIo, SWT.NONE);
        analogOutputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        analogOutputCombo.setText("AnalogOutput");

        List<String> analogOutputList = new ArrayList<>();
        for (AnalogOutput analogOutput : AnalogOutput.values()) {
            analogOutputList.add(analogOutput.toString());
        }
        analogOutputCombo.setItems(analogOutputList.toArray(new String[0]));

        textAnalogOutput = new Text(grpAnalogIo, SWT.BORDER);
        GridData gd_textAnlogOutput = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_textAnlogOutput.widthHint = 70;
        textAnalogOutput.setLayoutData(gd_textAnlogOutput);

        analogOutputCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    textAnalogOutput.setText(String.valueOf(
                            machineStateService.readAnalogOutput(AnalogOutput.valueOf(analogOutputCombo.getText()))));
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to read analog output state for machineDebugView.",
                            e1);
                }
            }
        });

        textAnalogOutput.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                textAnalogOutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            }
        });

        textAnalogOutput.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                try {
                    machineStateService.writeAnalogOutput(AnalogOutput.valueOf(analogOutputCombo.getText()),
                            Integer.valueOf(textAnalogOutput.getText()));
                    textAnalogOutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                } catch (NumberFormatException e1) {
                    textAnalogOutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to write AnalogOutput in machineDebugView.", e1);
                }
            }
        });

        Combo analogInputCombo = new Combo(grpAnalogIo, SWT.NONE);
        analogInputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        analogInputCombo.setText("AnalogInput");

        List<String> analogInputList = new ArrayList<>();
        for (AnalogInput analogInput : AnalogInput.values()) {
            analogInputList.add(analogInput.toString());
        }
        analogInputCombo.setItems(analogInputList.toArray(new String[0]));

        textAnalogInput = new Text(grpAnalogIo, SWT.BORDER);
        textAnalogInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        analogInputCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    textAnalogInput.setText(String.valueOf(
                            machineStateService.readAnalogInput(AnalogInput.valueOf(analogInputCombo.getText()))));
                } catch (IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Failed to read AnalogInput in machineDebugView.", e1);
                }
            }
        });

        stopSql.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                dataService.stopUpdate();

            }

        });

        toggleCryoTwo.addSelectionListener(new PumpAdapter(PumpIds.CRYO_TWO));

        return machineDebugComposite;
    }
}
