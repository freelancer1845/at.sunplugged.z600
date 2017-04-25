package at.sunplugged.z600.utils.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class IntegerRangeModifyListener implements ModifyListener {
    private final Text text;

    private final int min;

    private final int max;

    public IntegerRangeModifyListener(Text text, int min, int max) {
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
            int value = Integer.valueOf(currentText.replace(",", "."));
            if (value > max) {
                setFieldRed();
                text.setToolTipText("Value greater than max allowed " + text);
            } else if (value < min) {
                setFieldRed();
                text.setToolTipText("Value smaller that min allowed " + text);
            } else {
                setFieldWhite();
                text.setToolTipText("");
            }
        } catch (NumberFormatException e1) {
            setFieldRed();
            text.setToolTipText("Format not allowed");
        }
    }

    private void setFieldRed() {
        text.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
    }

    private void setFieldWhite() {
        text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    }

}
