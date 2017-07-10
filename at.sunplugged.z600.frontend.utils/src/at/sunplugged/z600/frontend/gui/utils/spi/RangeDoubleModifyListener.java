package at.sunplugged.z600.frontend.gui.utils.spi;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class RangeDoubleModifyListener implements ModifyListener {

    private final Double minValue;

    private final Double maxValue;

    public RangeDoubleModifyListener() {
        this.minValue = null;
        this.maxValue = null;
    }

    public RangeDoubleModifyListener(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void modifyText(ModifyEvent e) {
        Text txtInputField = (Text) e.getSource();
        try {
            String currentText = txtInputField.getText();
            double value = Double.valueOf(currentText.replace(",", "."));
            if (maxValue != null && minValue != null) {
                if (value > maxValue) {
                    txtInputField.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                    txtInputField.setToolTipText("Value greater than max allowed " + maxValue);
                    reactToError();
                } else if (value < minValue) {
                    txtInputField.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                    txtInputField.setToolTipText("Value smaller that min allowed " + minValue);
                    reactToError();
                } else {
                    txtInputField.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                    txtInputField.setToolTipText("");
                    reactToCorrect();
                }
            } else {
                txtInputField.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                txtInputField.setToolTipText("");
                reactToCorrect();
            }

        } catch (NumberFormatException e1) {
            txtInputField.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
            txtInputField.setToolTipText("Format not allowed");
            reactToError();
        }
    }

    protected void reactToError() {

    }

    protected void reactToCorrect() {

    }
}
