package at.sunplugged.z600.launcher.splash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class ErrorSelectionListener implements SelectionListener {

    private Throwable error;

    private Shell shell;

    public ErrorSelectionListener(Shell shell) {
        this.shell = shell;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (error != null) {
            MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
            messageBox.setMessage("Error: " + error.getMessage());
            messageBox.setText("Error");
            messageBox.open();
        }

    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

}
