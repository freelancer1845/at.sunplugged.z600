package at.sunplugged.z600.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.gui.tableitems.mbt.MbtTabItemFactory;
import at.sunplugged.z600.gui.tableitems.srm.SrmTabItemFactory;
import at.sunplugged.z600.mbt.api.MBTController;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MainApplication extends Thread {

    protected Shell shell;

    private LogService logService;

    private SrmCommunicator srmCommunicator;

    private MBTController mbtController;

    private DataService dataService;

    private static BundleContext context;

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
        shell.dispose();
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

        SrmTabItemFactory srmTabItemFactory = new SrmTabItemFactory(srmCommunicator, logService, dataService);
        srmTabItemFactory.createSrmTabItem(tabFolder, SWT.NONE);
        MbtTabItemFactory mbtTabItemFactory = new MbtTabItemFactory(mbtController, logService);
        // TabItem tbtmMbt = mbtTabItemFactory.createMbtTabItem(tabFolder,
        // SWT.NONE);
        mbtTabItemFactory.createDebugMbtTabItem(tabFolder, SWT.NONE);
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

    @Reference(unbind = "unbindSrmCommunicator")
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
}
