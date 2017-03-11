package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class ChamberFigure extends Figure implements MachineEventHandler {

    private static final int WIDTH = 320;

    private static final int HEIGHT = 60;

    private PressureSiteFigure pressureSiteFigure;

    public ChamberFigure(int x, int y) {

        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        this.setBorder(new LineBorder());
        this.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        this.setOpaque(true);

        createPressureSiteFigure();
        createKathodeFigures();
    }

    private void createPressureSiteFigure() {
        Rectangle bounds = getBounds().getCopy();
        this.pressureSiteFigure = new PressureSiteFigure(bounds.x + 5, bounds.y + 40, PressureMeasurementSite.CHAMBER);
        this.add(pressureSiteFigure);
    }

    private void createKathodeFigures() {
        Rectangle bounds = getBounds().getCopy();
        this.add(new KathodeFigure(bounds.x + 8, bounds.y + 10, "Kathode 1"));
        this.add(new KathodeFigure(bounds.x + 88, bounds.y + 10, "Kathode 2"));
        this.add(new KathodeFigure(bounds.x + 168, bounds.y + 10, "Kathode 3"));
        this.add(new KathodeFigure(bounds.x + 248, bounds.y + 10, "Kathode 4"));
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        pressureSiteFigure.handleEvent(event);
    }

}
