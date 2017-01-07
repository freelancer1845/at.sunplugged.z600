package at.sunplugged.z600.gui.machinediagram;

import java.text.DecimalFormat;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PressureChangedEvent;

public class PressureSiteFigure extends Figure implements MachineEventHandler {

    private static final int WIDTH = 50;

    private static final int HEIGHT = 15;

    private static final DecimalFormat FORMAT = new DecimalFormat("0.###E0");

    private final PressureMeasurementSite site;

    private Label label;

    public PressureSiteFigure(int x, int y, PressureMeasurementSite site) {
        this.site = site;
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        createLabel();
    }

    private void createLabel() {
        label = new Label();
        label.setBounds(getBounds().getCopy());
        label.setText(0 + " ");
        label.setBorder(new LineBorder());
        label.setFont(SWTResourceManager.getFont("tahoma", 7, SWT.NONE));
        label.setTextAlignment(Label.RIGHT);
        label.setLabelAlignment(Label.RIGHT);

        label.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        label.setOpaque(true);

        this.add(label);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.PRESSURE_CHANGED_EVENT) {
            if (((PressureChangedEvent) event).getSite() == site) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        updateValue((double) event.getValue());
                    }

                });

            }
        }
    }

    private void updateValue(double value) {
        label.setText(FORMAT.format(value) + " ");
    }

}
