package at.sunplugged.z600.launcher.splash.checkgroups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.event.Event;

public class VatCheckGroup extends AbstractCheckGroup {

    private Event secondEvent;

    public VatCheckGroup() {
        super("Waiting for VAT Outlet Connection...", "VAT Outlets Connected!", "VAT Outlets Connection Failed!");
    }

    @Override
    public void setEvent(Event event) {
        if (event == null) {
            super.setEvent(event);
        } else {
            secondEvent = event;
        }
    }

    @Override
    public void update() {
        if (event != null) {
            if ((boolean) event.getProperty("success") == false) {
                informationText.setText(failText);
                informationText.setStyleRange(new StyleRange(0, informationText.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_RED), null));
                errorButton.setEnabled(false);
                errorButtonSelectionListener.setError((Throwable) event.getProperty("Error"));
                event = null;
                secondEvent = null;
            }
        } else if (secondEvent != null) {
            if ((boolean) secondEvent.getProperty("success") == false) {
                informationText.setText(failText);
                informationText.setStyleRange(new StyleRange(0, informationText.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_RED), null));
                errorButton.setEnabled(true);
                errorButtonSelectionListener.setError((Throwable) secondEvent.getProperty("Error"));
                event = null;
                secondEvent = null;
            } else {
                informationText.setText(successText);
                informationText.setStyleRange(new StyleRange(0, informationText.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN), null));
                errorButton.setEnabled(true);
                event = null;
                secondEvent = null;
            }
        }
    }
}
