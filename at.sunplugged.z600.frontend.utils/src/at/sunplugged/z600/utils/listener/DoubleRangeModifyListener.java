package at.sunplugged.z600.utils.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class DoubleRangeModifyListener implements ModifyListener {

    private final Text text;

    private final double min;

    private final double max;

    public DoubleRangeModifyListener(Text text, double min, double max) {
        this.text = text;
        this.min = min;
        this.max = max;
    }

    @Override
    public void modifyText(ModifyEvent e) {
        try {
            String currentText = text.getText();
            if (currentText.isEmpty() == true) {
                setFieldRed();
                return;
            } else {
                setFieldWhite();
            }
            double value = Double.valueOf(currentText);
            if (value > max) {
                setFieldRed();
                text.setToolTipText("Value greater than max allowed " + text);
                reactToError();
            } else if (value < min) {
                setFieldRed();
                text.setToolTipText("Value smaller that min allowed " + text);
                reactToError();
            } else {
                setFieldWhite();
                text.setToolTipText("");
                reactToCorrect();
            }
        } catch (NumberFormatException e1) {
            setFieldRed();
            text.setToolTipText("Format not allowed");
            reactToError();
        }
    }

    protected void reactToError() {

    }

    protected void reactToCorrect() {

    }

    private void setFieldRed() {
        text.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
    }

    private void setFieldWhite() {
        text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    }

}
