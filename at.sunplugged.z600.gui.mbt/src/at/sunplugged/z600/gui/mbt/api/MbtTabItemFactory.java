package at.sunplugged.z600.gui.mbt.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.gui.mbt.impl.MbtCheckButton;

public class MbtTabItemFactory {

    public TabItem createMbtTabItem(TabFolder tabFolder, int style) {
        TabItem tbtmMbt = new TabItem(tabFolder, style);
        tbtmMbt.setText("MBT");

        Composite composite_1 = new Composite(tabFolder, SWT.BORDER);
        tbtmMbt.setControl(composite_1);
        GridLayout gl_composite_1 = new GridLayout(6, false);
        gl_composite_1.marginWidth = 20;
        gl_composite_1.marginHeight = 20;
        composite_1.setLayout(gl_composite_1);

        Label lblMbtDebugControl = new Label(composite_1, SWT.BORDER);
        GridData gd_lblMbtDebugControl = new GridData(SWT.LEFT, SWT.CENTER, true, false, 6, 1);
        gd_lblMbtDebugControl.horizontalIndent = 20;
        gd_lblMbtDebugControl.verticalIndent = 20;
        lblMbtDebugControl.setLayoutData(gd_lblMbtDebugControl);
        lblMbtDebugControl.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        lblMbtDebugControl.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblMbtDebugControl.setText("MBT Debug Control");

        return tbtmMbt;
    }

    private void createButtons(Composite parent) {
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j < 7; j++) {
                new MbtCheckButton(parent, "Out:" + i + ":" + j) {

                    @Override
                    protected void buttonSpecificWidgetSelected(SelectionEvent e) {
                        System.out.println("Selection Changed: " + "Out:");
                    }

                };
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 7; j++) {
                new MbtCheckButton(parent, "In:" + i + ":" + j) {

                    @Override
                    protected void buttonSpecificWidgetSelected(SelectionEvent e) {
                        System.out.println("Selection Changed: " + "In:");

                    }
                };
            }
        }

    }

}
