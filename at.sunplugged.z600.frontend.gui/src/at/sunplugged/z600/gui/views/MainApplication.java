package at.sunplugged.z600.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.gui.Activator;
import at.sunplugged.z600.gui.srm.SrmTabItemFactory;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Composite;

public class MainApplication {

    protected Shell shell;

    private LogService logService = Activator.getLogService();

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
                logService.log(LogService.LOG_ERROR, e.getMessage(), e);
            }
        }
        shell.dispose();
        System.exit(0);
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

        SrmTabItemFactory srmTabItemFactory = new SrmTabItemFactory();
        
        Composite composite = new Composite(shell, SWT.NONE);
        GridData gd_composite = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        gd_composite.widthHint = 550;
        composite.setLayoutData(gd_composite);
        
                TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
                tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
                GridData gd_tabFolder = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
                gd_tabFolder.heightHint = 687;
                gd_tabFolder.widthHint = 438;
                tabFolder.setLayoutData(gd_tabFolder);
                
                        TabItem tbtmMain = srmTabItemFactory.createSrmTabItem(tabFolder, SWT.NONE);
                        
                        Composite composite_1 = new Composite(shell, SWT.NONE);
                        composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                        new Label(shell, SWT.NONE);

    }
}
