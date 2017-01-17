package at.sunplugged.z600.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.gui.factorys.ConveyorGroupFactory;
import at.sunplugged.z600.gui.factorys.InterlocksGroupFactory;
import at.sunplugged.z600.gui.factorys.SystemOutputFactory;
import at.sunplugged.z600.gui.machinediagram.Viewer;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MainView {

    protected Shell shell;

    private static LogService logService;

    private static SrmCommunicator srmCommunicator;

    private static MbtService mbtController;

    private static DataService dataService;

    private static MachineStateService machineStateService;

    private static ConveyorControlService conveyorControlService;

    private static StandardThreadPoolService threadPool;

    private static BundleContext context;

    private static Viewer diagramViewer;
    private static Text text_left_to_right_speed;
    private static Text text_right_to_left_speed;
    private static Text text;

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

        TabItem tbtmConveyorDebug = new TabItem(tabFolder, SWT.NONE);
        tbtmConveyorDebug.setText("Conveyor Debug");

        Composite composite = new Composite(tabFolder, SWT.NONE);
        tbtmConveyorDebug.setControl(composite);
        composite.setLayout(new GridLayout(1, true));

        Group grpLeftToRight = new Group(composite, SWT.NONE);
        grpLeftToRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpLeftToRight.setText("Left To Right");
        grpLeftToRight.setLayout(new GridLayout(3, true));

        Label lblSpeedInMms = new Label(grpLeftToRight, SWT.NONE);
        lblSpeedInMms.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSpeedInMms.setText("Speed in mm/s");

        text_left_to_right_speed = new Text(grpLeftToRight, SWT.BORDER);
        text_left_to_right_speed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Button button_left_to_right = new Button(grpLeftToRight, SWT.NONE);
        button_left_to_right.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_left_to_right.setText("START");
        button_left_to_right.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.start(Double.valueOf(text_left_to_right_speed.getText()) / 1000,
                        Mode.LEFT_TO_RIGHT);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Group grpRightToLeft = new Group(composite, SWT.NONE);
        grpRightToLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpRightToLeft.setText("Right To Left");
        grpRightToLeft.setLayout(new GridLayout(3, true));

        Label label = new Label(grpRightToLeft, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        label.setText("Speed in mm/s");

        text_right_to_left_speed = new Text(grpRightToLeft, SWT.BORDER);
        text_right_to_left_speed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Button button_right_to_left = new Button(grpRightToLeft, SWT.NONE);
        button_right_to_left.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_right_to_left.setText("START");

        Group grpMonitoring = new Group(composite, SWT.NONE);
        grpMonitoring.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpMonitoring.setText("Monitoring");
        grpMonitoring.setLayout(new GridLayout(2, true));

        Label lblLeftSpeed = new Label(grpMonitoring, SWT.NONE);
        lblLeftSpeed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblLeftSpeed.setText("Left Speed in mm/s");

        Label label_left_speed = new Label(grpMonitoring, SWT.BORDER);
        label_left_speed.setAlignment(SWT.RIGHT);
        label_left_speed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        label_left_speed.setText("0.00");
        machineStateService.registerMachineEventHandler(new MachineEventHandler() {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.CONVEYOR_EVENT)) {
                    if (((ConveyorMachineEvent) event).getConveyorEventType()
                            .equals(ConveyorMachineEvent.Type.LEFT_SPEED_CHANGED)) {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                label_left_speed.setText("" + (Double) event.getValue() * 1000);
                            }

                        });

                    }
                }
            }
        });

        Label lblRightSpeedIn = new Label(grpMonitoring, SWT.NONE);
        lblRightSpeedIn.setText("Right Speed in mm/s");

        Label label__right_speed = new Label(grpMonitoring, SWT.BORDER);
        label__right_speed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        label__right_speed.setText("0.00");
        label__right_speed.setAlignment(SWT.RIGHT);
        machineStateService.registerMachineEventHandler(new MachineEventHandler() {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.CONVEYOR_EVENT)) {
                    if (((ConveyorMachineEvent) event).getConveyorEventType()
                            .equals(ConveyorMachineEvent.Type.RIGHT_SPEED_CHANGED)) {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                label__right_speed.setText("" + ((Double) event.getValue() * 1000.0));
                            }

                        });
                    }
                }
            }
        });

        Label lblLeftMotorMaximum = new Label(grpMonitoring, SWT.NONE);
        lblLeftMotorMaximum.setText("Left Motor Maximum Speed");

        Label label_left_engine_maximum_speed = new Label(grpMonitoring, SWT.BORDER);
        label_left_engine_maximum_speed.setAlignment(SWT.RIGHT);
        label_left_engine_maximum_speed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        label_left_engine_maximum_speed.setText("0");
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                int currentMaximumSpeed = conveyorControlService.getEngineOne().getCurrentMaximumSpeed();
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        label_left_engine_maximum_speed.setText("" + currentMaximumSpeed);
                    }

                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                threadPool.execute(this);
            }

        });

        Label lblRightMotorMaximum = new Label(grpMonitoring, SWT.NONE);
        lblRightMotorMaximum.setText("Right Motor Maximum Speed");

        Label label_right_engine_maximum_speed = new Label(grpMonitoring, SWT.BORDER);
        label_right_engine_maximum_speed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        label_right_engine_maximum_speed.setText("0");
        label_right_engine_maximum_speed.setAlignment(SWT.RIGHT);
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                int currentMaximumSpeed = conveyorControlService.getEngineTwo().getCurrentMaximumSpeed();
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        label_right_engine_maximum_speed.setText("" + currentMaximumSpeed);
                    }

                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                threadPool.execute(this);
            }

        });

        Button buttonStop = new Button(composite, SWT.NONE);
        buttonStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        buttonStop.setText("STOP");

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
                logService.log(LogService.LOG_WARNING, "Evakuieren Gedrückt!!!");
            }
        });
        btnEvakuieren.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnEvakuieren.setText("Evakuieren");

        Group grpBandlauf = ConveyorGroupFactory.createGroup(composite_1);

       

        Group grpInterlocks = InterlocksGroupFactory.createGroup(composite_1);
        GridLayout gridLayout = (GridLayout) grpInterlocks.getLayout();
        gridLayout.numColumns = 2;

        Label lblPinnacle = new Label(grpInterlocks, SWT.NONE);
        GridData gd_lblPinnacle = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblPinnacle.widthHint = 100;
        lblPinnacle.setLayoutData(gd_lblPinnacle);
        lblPinnacle.setText("Pinnacle");

        Button button = new Button(grpInterlocks, SWT.CHECK);

        Label lblMdx = new Label(grpInterlocks, SWT.NONE);
        lblMdx.setText("MDX");

        Button button_1 = new Button(grpInterlocks, SWT.CHECK);

        Group grpSystemOutput = new Group(composite_1, SWT.NONE);
        grpSystemOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpSystemOutput.setText("System Output");
        grpSystemOutput.setLayout(new GridLayout(1, false));

        StyledText styledText = SystemOutputFactory.createStyledText(grpSystemOutput);
        new Label(shell, SWT.NONE);

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

    @Reference(unbind = "unbindDataService")
    public synchronized void bindDataService(DataService dataService) {
        MainView.dataService = dataService;
    }

    public synchronized void unbindDataService(DataService dataService) {
        if (MainView.dataService == dataService) {
            MainView.dataService = null;
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
}
