package at.sunplugged.z600.launcher.splash.checkgroups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.event.Event;

import at.sunplugged.z600.launcher.splash.ErrorSelectionListener;

public abstract class AbstractCheckGroup implements CheckGroup {

    protected Event event;
    protected StyledText informationText;
    protected Button errorButton;
    protected ErrorSelectionListener errorButtonSelectionListener;

    protected final String initialText;

    protected final String successText;

    protected final String failText;

    public AbstractCheckGroup(String initialText, String successText, String failText) {
        this.initialText = initialText;
        this.successText = successText;
        this.failText = failText;

    }

    public void create(Composite parent) {
        informationText = new StyledText(parent, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        informationText.setEnabled(false);
        informationText.setText(initialText);
        informationText.setEditable(false);
        informationText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        informationText.setAlwaysShowScrollBars(false);
        informationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        errorButton = new Button(parent, SWT.NONE);
        errorButton.setEnabled(false);
        errorButton.setText("Fehler Anzeigen");
        errorButtonSelectionListener = new ErrorSelectionListener(parent.getShell());
        errorButton.addSelectionListener(errorButtonSelectionListener);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void update() {
        if (event != null) {
            if ((boolean) event.getProperty("success") == true) {
                informationText.setText(successText);
                informationText.setStyleRange(new StyleRange(0, informationText.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN), null));
                errorButton.setEnabled(false);
            } else {
                informationText.setText(failText);
                informationText.setStyleRange(new StyleRange(0, informationText.getText().length(),
                        SWTResourceManager.getColor(SWT.COLOR_RED), null));
                errorButton.setEnabled(true);
                errorButtonSelectionListener.setError((Throwable) event.getProperty("Error"));
            }
            event = null;
        }
    }

}
