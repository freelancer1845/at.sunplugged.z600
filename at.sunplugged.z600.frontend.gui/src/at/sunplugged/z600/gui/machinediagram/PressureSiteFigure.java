package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class PressureSiteFigure extends Figure implements MachineEventHandler {

    private static final int WIDTH = 50;

    private static final int HEIGHT = 15;

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

        this.add(label);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        // TODO Auto-generated method stub

    }

}
