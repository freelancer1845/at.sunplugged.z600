package at.sunplugged.z600.gui.views;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WaterControl.WaterOutlet;
import at.sunplugged.z600.gui.factorys.ConveyorGroupFactory;
import at.sunplugged.z600.gui.factorys.PowerSupplyBasicFactory;
import at.sunplugged.z600.gui.factorys.SystemOutputFactory;
import at.sunplugged.z600.gui.factorys.VacuumTabitemFactory;
import at.sunplugged.z600.gui.machinediagram.Viewer;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MainView {

    protected Shell shell;

    private static LogService logService;

    private static SrmCommunicator srmCommunicator;

    private static MbtService mbtController;

    private static MachineStateService machineStateService;

    private static ConveyorControlService conveyorControlService;

    private static StandardThreadPoolService threadPool;

    private static VacuumService vacuumService;

    private static SettingsService settings;

    private static BundleContext context;

    private static Viewer diagramViewer;
    private static Text text;
    private static Text textAddress;
    private static Text textUsername;
    private static Text textPassword;
    private static Text textStatement;
    private static Text text_1;

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
        GridData gdTabFolder = new GridData(SWT.RIGHT, SWT.TOP, true, true, 1, 2);
        gdTabFolder.heightHint = 718;
        gdTabFolder.widthHint = 438;
        tabFolder.setLayoutData(gdTabFolder);

        TabItem tbtmMain = new TabItem(tabFolder, SWT.NONE);
        tbtmMain.setText("Main");

        Composite composite_1 = new Composite(tabFolder, SWT.NONE);
        tbtmMain.setControl(composite_1);
        composite_1.setLayout(new GridLayout(1, false));

        Group groupVacuum = new Group(composite_1, SWT.NONE);
        groupVacuum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        groupVacuum.setText("Vakuum");
        groupVacuum.setLayout(new GridLayout(1, false));

        Button btnEvakuieren = new Button(groupVacuum, SWT.NONE);
        btnEvakuieren.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

            }
        });
        btnEvakuieren.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnEvakuieren.setText("Start");

        Group grpBandlauf = ConveyorGroupFactory.createGroup(composite_1);

        Group grpSystemOutput = new Group(composite_1, SWT.NONE);
        grpSystemOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpSystemOutput.setText("System Output");
        grpSystemOutput.setLayout(new GridLayout(1, false));

        StyledText styledText = SystemOutputFactory.createStyledText(grpSystemOutput);

        TabItem tbtmVacuum = new TabItem(tabFolder, SWT.NONE);
        tbtmVacuum.setText("Vacuum");

        Composite VacuumComposite = VacuumTabitemFactory.createComposite(tabFolder);
        tbtmVacuum.setControl(VacuumComposite);
        VacuumComposite.setLayout(new GridLayout(1, false));

        TabItem tbtmMachinedebug = new TabItem(tabFolder, SWT.NONE);
        tbtmMachinedebug.setText("MachineDebug");

        Composite composite = new Composite(tabFolder, SWT.NONE);
        tbtmMachinedebug.setControl(composite);
        composite.setLayout(new GridLayout(3, true));

        Button toggelOutletOne = new Button(composite, SWT.NONE);
        toggelOutletOne.addSelectionListener(new OutletAdapter(Outlet.OUTLET_ONE));
        toggelOutletOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        toggelOutletOne.setText("V1");

        Button toggelOutletTwo = new Button(composite, SWT.NONE);
        toggelOutletTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletTwo.setText("V2");
        toggelOutletTwo.addSelectionListener(new OutletAdapter(Outlet.OUTLET_TWO));

        Button toggelOutletThree = new Button(composite, SWT.NONE);
        toggelOutletThree.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletThree.setText("V3");
        toggelOutletThree.addSelectionListener(new OutletAdapter(Outlet.OUTLET_THREE));

        Button toggleOutletFour = new Button(composite, SWT.NONE);
        toggleOutletFour.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletFour.setText("V4");
        toggleOutletFour.addSelectionListener(new OutletAdapter(Outlet.OUTLET_FOUR));

        Button toggelOutletFive = new Button(composite, SWT.NONE);
        toggelOutletFive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletFive.setText("V5");
        toggelOutletFive.addSelectionListener(new OutletAdapter(Outlet.OUTLET_FIVE));

        Button toggleOutletSix = new Button(composite, SWT.NONE);
        toggleOutletSix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletSix.setText("V6");
        toggleOutletSix.addSelectionListener(new OutletAdapter(Outlet.OUTLET_SIX));

        Button toggleOutletSeven = new Button(composite, SWT.NONE);
        toggleOutletSeven.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletSeven.setText("V7");
        toggleOutletSeven.addSelectionListener(new OutletAdapter(Outlet.OUTLET_SEVEN));

        Button toggleOutletEight = new Button(composite, SWT.NONE);
        toggleOutletEight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleOutletEight.setText("V8");
        toggleOutletEight.addSelectionListener(new OutletAdapter(Outlet.OUTLET_EIGHT));

        Button toggelOutletNine = new Button(composite, SWT.NONE);
        toggelOutletNine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggelOutletNine.setText("V9");
        toggelOutletNine.addSelectionListener(new OutletAdapter(Outlet.OUTLET_NINE));

        Button togglePrePumpOne = new Button(composite, SWT.NONE);
        togglePrePumpOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        togglePrePumpOne.setText("Toggle Pre Pump Pone");
        togglePrePumpOne.addSelectionListener(new PumpAdapter(PumpIds.PRE_PUMP_ONE));

        Button togglePrePumpRoots = new Button(composite, SWT.NONE);
        togglePrePumpRoots.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        togglePrePumpRoots.setText("Toggle Pre Pump Roots");
        togglePrePumpRoots.addSelectionListener(new PumpAdapter(PumpIds.PRE_PUMP_ROOTS));

        Button togglepPrePumpTwo = new Button(composite, SWT.NONE);
        togglepPrePumpTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        togglepPrePumpTwo.setText("Toggle Pre Pump Two");
        togglepPrePumpTwo.addSelectionListener(new PumpAdapter(PumpIds.PRE_PUMP_TWO));

        Button toggleTurboPump = new Button(composite, SWT.NONE);
        toggleTurboPump.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleTurboPump.setText("Toggle Turbo Pump");
        toggleTurboPump.addSelectionListener(new PumpAdapter(PumpIds.TURBO_PUMP));

        Button toggleCryoOne = new Button(composite, SWT.NONE);
        toggleCryoOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        toggleCryoOne.setText("Toggle Cryo One");
        toggleCryoOne.addSelectionListener(new PumpAdapter(PumpIds.CRYO_ONE));

        Button toggleCryoTwo = new Button(composite, SWT.NONE);
        toggleCryoTwo.setText("Toggle Cryo Two");
        toggleCryoTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath1Label = new Label(composite, SWT.NONE);
        waterKath1Label.setText("Water KATH1: false");
        waterKath1Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath2Label = new Label(composite, SWT.NONE);
        waterKath2Label.setText("Water KATH2: false");
        waterKath2Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath3Label = new Label(composite, SWT.NONE);
        waterKath3Label.setText("Water KATH3: false");
        waterKath3Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label waterKath4Label = new Label(composite, SWT.NONE);
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

        Button toggleWaterTurboPump = new Button(composite, SWT.NONE);
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

        Button toggleWaterKath1Pump = new Button(composite, SWT.NONE);
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

        Button toggleWaterKath2Pump = new Button(composite, SWT.NONE);
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

        Button toggleWaterKath3Pump = new Button(composite, SWT.NONE);
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

        Button toggleWaterShieldPump = new Button(composite, SWT.NONE);
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

        Button toggleDigOut = new Button(composite, SWT.NONE);
        toggleDigOut.setText("Toggle Dig Out [2][0]");
        toggleDigOut.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
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

        TabItem tbtmPowerSupply = new TabItem(tabFolder, SWT.NONE);
        tbtmPowerSupply.setText("Power Supply");
        Composite powerSupplyComposite = new Composite(tabFolder, SWT.NONE);

        powerSupplyComposite.setLayout(new GridLayout(1, false));
        tbtmPowerSupply.setControl(powerSupplyComposite);

        PowerSupplyBasicFactory.createPowerSupplyGroup(powerSupplyComposite, PowerSourceId.PINNACLE);
        PowerSupplyBasicFactory.createPowerSupplyGroup(powerSupplyComposite, PowerSourceId.SSV1);
        PowerSupplyBasicFactory.createPowerSupplyGroup(powerSupplyComposite, PowerSourceId.SSV2);

        new Label(shell, SWT.NONE);
        toggleCryoTwo.addSelectionListener(new PumpAdapter(PumpIds.CRYO_TWO));

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
                // TODO Auto-generated catch block
                e1.printStackTrace();
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
}
