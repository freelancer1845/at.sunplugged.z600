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
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.frontend.gui.srm50.tabItem.SrmTabItemFactory;
import at.sunplugged.z600.gui.Activator;
import at.sunplugged.z600.gui.mbt.api.MbtTabItemFactory;

public class MainApplication {

    protected Shell shell;

    private LogService logService;

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
            Activator.getContext().getBundle(0).stop();
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
        GridData gdTabFolder = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 2);
        gdTabFolder.heightHint = 718;
        gdTabFolder.widthHint = 438;
        tabFolder.setLayoutData(gdTabFolder);

        SrmTabItemFactory srmTabItemFactory = new SrmTabItemFactory();
        TabItem tbtmMain = srmTabItemFactory.createSrmTabItem(tabFolder, SWT.NONE);
        MbtTabItemFactory mbtTabItemFactory = new MbtTabItemFactory();
        TabItem tbtmMbt = mbtTabItemFactory.createMbtTabItem(tabFolder, SWT.NONE);

        Composite compositeOne = new Composite(shell, SWT.NONE);
        compositeOne.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    }

}
