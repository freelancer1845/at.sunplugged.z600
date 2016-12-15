package at.sunplugged.z600.gui.views;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.mbt.api.MBTController;
import at.sunplugged.z600.srm50.api.SrmCommunicator;
import org.eclipse.swt.widgets.Label;

@Component
public class MainApplication extends Thread {

    protected Shell shell;

    private LogService logService;

    private SrmCommunicator srmCommunicator;

    private MBTController mbtController;

    private DataService dataService;

    private MachineStateService machineStateService;

    private ConveyorControlService conveyorControlService;

    private static BundleContext context;
    private Text text;
    private Text text_1;
    private Text txtValue;
    private Text txtValue_1;

    public LogService getLogService() {
        return logService;
    }

    public SrmCommunicator getSrmCommunicator() {
        return srmCommunicator;
    }

    public MBTController getMbtController() {
        return mbtController;
    }

    public static BundleContext getContext() {
        return MainApplication.context;
    }

    @Activate
    public synchronized void activateGui(BundleContext context) {
        this.setName("Gui Thread");
        this.start();
        MainApplication.context = context;
    }

    @Override
    public void run() {
        this.open();
    }

    /**
     * Open the window.
     */
    public void open() {

        Display display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (RuntimeException e) {
                MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
                messageBox.setMessage("Unhandled Loop Exception: " + e.getMessage());
                messageBox.setText("Unhandled Loop Exeception");
                messageBox.open();
                logService.log(LogService.LOG_ERROR, e.getMessage(), e);
            }
        }
        try {
            MainApplication.getContext().getBundle(0).stop();
        } catch (BundleException e) {
            logService.log(LogService.LOG_ERROR, "BundleException while shuting down System Bundle.", e);
        }
    }

    /**
     * Create contents of the window.
     * 
     * @wbp.parser.entryPoint
     */
    protected void createContents() {
        shell = new Shell();
        shell.setMinimumSize(new Point(1024, 764));
        shell.setSize(800, 600);
        shell.setText("SWT Application");
        shell.setLayout(new GridLayout(2, false));

        Composite composite = new Composite(shell, SWT.NONE);
        GridData gdComposite = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        gdComposite.widthHint = 550;
        composite.setLayoutData(gdComposite);

        TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
        tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
        GridData gdTabFolder = new GridData(SWT.RIGHT, SWT.TOP, true, true, 1, 2);
        gdTabFolder.heightHint = 718;
        gdTabFolder.widthHint = 438;
        tabFolder.setLayoutData(gdTabFolder);

        TabItem tbtmEnginedebug = new TabItem(tabFolder, SWT.NONE);
        tbtmEnginedebug.setText("EngineDebug");

        Composite composite_1 = new Composite(tabFolder, SWT.NONE);
        tbtmEnginedebug.setControl(composite_1);
        GridLayout gl_composite_1 = new GridLayout(1, false);
        gl_composite_1.marginWidth = 20;
        gl_composite_1.marginHeight = 20;
        composite_1.setLayout(gl_composite_1);

        Group grpEngine = new Group(composite_1, SWT.NONE);
        grpEngine.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpEngine.setText("Engine 1");
        grpEngine.setLayout(new GridLayout(2, true));

        Button btnStart = new Button(grpEngine, SWT.NONE);
        btnStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStart.setText("Start");
        btnStart.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().startEngine();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Button btnStop = new Button(grpEngine, SWT.NONE);
        btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStop.setText("Stop");
        btnStop.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().stopEngine();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Button btnIncrease = new Button(grpEngine, SWT.NONE);
        btnIncrease.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnIncrease.setText("Increase");
        btnIncrease.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().increaseSpeed();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Button btnDecrease = new Button(grpEngine, SWT.NONE);
        btnDecrease.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnDecrease.setText("Decrease");
        btnDecrease.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().decreaseSpeed();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Button btnLeftdirection = new Button(grpEngine, SWT.NONE);
        btnLeftdirection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnLeftdirection.setText("LeftDirection");
        btnLeftdirection.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().setDirection(0);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Button btnRightdirection = new Button(grpEngine, SWT.NONE);
        btnRightdirection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnRightdirection.setText("RightDirection");
        btnRightdirection.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().setDirection(1);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        Button btnSetspeed = new Button(grpEngine, SWT.NONE);
        btnSetspeed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSetspeed.setText("SetSpeed");
        btnSetspeed.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().setMaximumSpeed(Integer.valueOf(text.getText()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });

        text = new Text(grpEngine, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button buttonConnect = new Button(grpEngine, SWT.NONE);
        buttonConnect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        buttonConnect.setText("Connected");
        buttonConnect.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorControlService.getEngineOne().connect();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });

        Group grpEngine_1 = new Group(composite_1, SWT.NONE);
        grpEngine_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpEngine_1.setText("Engine 2");
        grpEngine_1.setLayout(new GridLayout(2, true));

        Button button = new Button(grpEngine_1, SWT.NONE);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button.setText("Start");

        Button button_1 = new Button(grpEngine_1, SWT.NONE);
        button_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_1.setText("Stop");

        Button button_2 = new Button(grpEngine_1, SWT.NONE);
        button_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_2.setText("Increase");

        Button button_3 = new Button(grpEngine_1, SWT.NONE);
        button_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_3.setText("Decrease");

        Button button_4 = new Button(grpEngine_1, SWT.NONE);
        button_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_4.setText("LeftDirection");

        Button button_5 = new Button(grpEngine_1, SWT.NONE);
        button_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_5.setText("RightDirection");

        Button button_6 = new Button(grpEngine_1, SWT.NONE);
        button_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        button_6.setText("SetSpeed");

        text_1 = new Text(grpEngine_1, SWT.BORDER);
        text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Group grpVelocity = new Group(composite_1, SWT.NONE);
        grpVelocity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpVelocity.setText("Velocity");
        grpVelocity.setLayout(new GridLayout(2, true));

        Label lblDigin = new Label(grpVelocity, SWT.NONE);
        lblDigin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDigin.setText("DigIn4.7");

        txtValue = new Text(grpVelocity, SWT.BORDER);
        txtValue.setText("Value");
        txtValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblDigin_1 = new Label(grpVelocity, SWT.NONE);
        lblDigin_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDigin_1.setText("DigIn5.0");

        txtValue_1 = new Text(grpVelocity, SWT.BORDER);
        txtValue_1.setText("Value");
        txtValue_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        try {
            mbtController.connect("192.168.1.54");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        updateSpeedValueValue(0);

        // SrmTabItemFactory srmTabItemFactory = new
        // SrmTabItemFactory(srmCommunicator, logService, dataService);
        // srmTabItemFactory.createSrmTabItem(tabFolder, SWT.NONE);
        // MbtTabItemFactory mbtTabItemFactory = new
        // MbtTabItemFactory(mbtController, logService, machineStateService);
        // TabItem tbtmMbt = mbtTabItemFactory.createMbtTabItem(tabFolder,
        // SWT.NONE);
        // mbtTabItemFactory.createDebugMbtTabItem(tabFolder, SWT.NONE);
        Composite compositeOne = new Composite(shell, SWT.NONE);
        compositeOne.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    @Reference(unbind = "unbindSrmCommunicator", cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindSrmCommunicator(SrmCommunicator srmCommunicator) {
        this.srmCommunicator = srmCommunicator;
    }

    public synchronized void unbindSrmCommunicator(SrmCommunicator srmCommunicator) {
        if (this.srmCommunicator == srmCommunicator) {
            this.srmCommunicator = null;
        }
    }

    @Reference(unbind = "unbindMBTController")
    public synchronized void bindMBTController(MBTController mbtController) {
        this.mbtController = mbtController;
    }

    public synchronized void unbindMBTController(MBTController mbtController) {
        if (this.mbtController == mbtController) {
            this.mbtController = null;
        }
    }

    @Reference(unbind = "unbindDataService")
    public synchronized void bindDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public synchronized void unbindDataService(DataService dataService) {
        if (this.dataService == dataService) {
            this.dataService = null;
        }
    }

    @Reference(unbind = "unbindMachineStateService", cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (this.machineStateService == machineStateService) {
            this.machineStateService = null;
        }
    }

    @Reference(unbind = "unbindConveyorControlService", cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (this.conveyorControlService == conveyorControlService) {
            this.conveyorControlService = null;
        }
    }

    private void updateSpeedValueValue(int i) {

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (i == 100000) {
                    try {
                        txtValue.setText(String.valueOf(mbtController.readDigIns(39, 1).get(39)));
                        txtValue_1.setText(String.valueOf(mbtController.readDigIns(40, 1).get(40)));
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, e.getMessage(), e);
                    }
                    updateSpeedValueValue(0);
                } else {
                    updateSpeedValueValue(i + 1);
                }
            }

        });

    }
}
