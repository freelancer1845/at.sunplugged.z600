package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;

public class OutletFigure extends Figure implements MachineEventHandler {

    private static final int WIDHT = 30;

    private static final int HEIGHT = 30;

    private final Outlet outlet;

    // private final Shape shape;

    public OutletFigure(String name, int x, int y, Outlet outlet) {
        this.outlet = outlet;
        this.setBounds(new Rectangle(x, y, WIDHT, HEIGHT));
        this.setBorder(new LineBorder());
        this.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_BLUE));
        this.setOpaque(false);
        this.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_BLUE));
        this.add(createLabel(name));
        // shape = createShape();

    }

    public void setState(boolean state) {
        if (state == true) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                }

            });
        } else {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
                }

            });
        }

    }

    private Label createLabel(String name) {
        Label label = new Label();
        Rectangle r = getBounds().getCopy();
        label.setBounds(new Rectangle(r.x + 2, r.y + 2, r.width, r.height));
        label.setText(name);
        label.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        return label;
    }

    private Shape createShape() {
        Polygon polygonShape = new Polygon();
        Rectangle bounds = this.getBounds().getCopy();
        Point upperLeftCorner = new Point(bounds.x + 1, bounds.y + 1);
        Point upperRightCorner = new Point(bounds.x + bounds.width - 1, bounds.y + 1);
        Point middlePointLeft = new Point(bounds.x + bounds.width / 2 - 2, bounds.y + bounds.height * 2 / 3);
        Point middlePointRight = new Point(bounds.x + bounds.width / 2 + 2, bounds.y + bounds.height * 2 / 3);
        Point lowerLeftCorner = new Point(bounds.x + 1, bounds.y + bounds.height - 1);
        Point lowerRightCorner = new Point(bounds.x + bounds.width - 1, bounds.y + bounds.height - 1);
        polygonShape.addPoint(upperLeftCorner);

        polygonShape.addPoint(middlePointLeft);

        polygonShape.addPoint(lowerLeftCorner);
        polygonShape.addPoint(lowerRightCorner);

        polygonShape.addPoint(middlePointRight);
        polygonShape.addPoint(upperRightCorner);
        // polygonShape.setBorder(new LineBorder());

        return polygonShape;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.DIGITAL_OUTPUT_CHANGED) {
            if (event.getDigitalOutput() == this.outlet.getDigitalOutput()) {
                this.setState((boolean) event.getValue());
            }
        }
    }
}
