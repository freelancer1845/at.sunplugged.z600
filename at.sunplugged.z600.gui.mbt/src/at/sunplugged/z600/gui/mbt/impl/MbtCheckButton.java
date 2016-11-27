package at.sunplugged.z600.gui.mbt.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class MbtCheckButton {

    private final Button button;

    public MbtCheckButton(Composite parent, String text) {
        this.button = new Button(parent, SWT.CHECK);
        this.button.setText(text);

        this.button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                buttonSpecificWidgetSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });
    }

    protected abstract void buttonSpecificWidgetSelected(SelectionEvent e);
}
