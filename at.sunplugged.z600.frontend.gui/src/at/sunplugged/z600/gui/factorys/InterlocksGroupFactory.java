package at.sunplugged.z600.gui.factorys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public final class InterlocksGroupFactory {
    /**
     * @wbp.factory
     */
    public static Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Interlocks");
        group.setLayout(new GridLayout(1, true));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        return group;
    }
}