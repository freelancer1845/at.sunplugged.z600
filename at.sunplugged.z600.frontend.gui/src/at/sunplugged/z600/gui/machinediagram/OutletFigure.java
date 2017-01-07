package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class OutletFigure extends Figure implements MachineEventHandler {

    private static final int WIDTH = 60;

    private static final int HEIGHT = 20;

    private final Outlet outlet;

    private Label label;

    private Polygon polygonShape;

    // private final Shape shape;

    public OutletFigure(String name, int x, int y, Outlet outlet, boolean vertical) {
        this.outlet = outlet;
        if (vertical) {
            this.setBounds(new Rectangle(x, y, HEIGHT, WIDTH));
        } else {
            this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        }
        createShape(vertical);
        createLabel(name, vertical);

    }

    private void setState(boolean state) {
        if (state == true) {
            polygonShape.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
        } else {
            polygonShape.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        }

    }

    private void createLabel(String name, boolean vertical) {
        label = new Label();
        Rectangle r = getBounds().getCopy();
        label.setBounds(new Rectangle(r.x, r.y, r.width, r.height));
        label.setText(name);
        label.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        if (vertical) {
            label.setLabelAlignment(Label.TOP);
            label.setTextAlignment(Label.TOP);
        } else {
            label.setTextAlignment(Label.LEFT);
            label.setLabelAlignment(Label.LEFT);
        }
        this.add(label);
    }

    private void createShape(boolean vertical) {
        Point upperLeftCorner;
        Point lowerLeftCorner;
        Point middlePoint;
        Point lowerRightCorner;
        Point upperRightCorner;
        Rectangle bounds = this.getBounds().getCopy();
        bounds.width -= 1;
        bounds.height -= 1;
        if (!vertical) {

            bounds.x += 15;
            bounds.width -= 30;

        } else {
            bounds.y += 15;
            bounds.height -= 30;
        }
        upperLeftCorner = new Point(bounds.x, bounds.y);
        lowerLeftCorner = new Point(bounds.x, bounds.y + bounds.height);
        middlePoint = new Point(bounds.x + bounds.width / 2, +bounds.y + bounds.height / 2);
        lowerRightCorner = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
        upperRightCorner = new Point(bounds.x + bounds.width, bounds.y);
        polygonShape = new Polygon();
        if (!vertical) {

            polygonShape.addPoint(upperLeftCorner);
            polygonShape.addPoint(lowerLeftCorner);
            polygonShape.addPoint(middlePoint);
            polygonShape.addPoint(lowerRightCorner);
            polygonShape.addPoint(upperRightCorner);
            polygonShape.addPoint(middlePoint);
        } else {
            polygonShape.addPoint(upperLeftCorner);
            polygonShape.addPoint(middlePoint);
            polygonShape.addPoint(lowerLeftCorner);
            polygonShape.addPoint(lowerRightCorner);
            polygonShape.addPoint(middlePoint);
            polygonShape.addPoint(upperRightCorner);
        }
        polygonShape.setFill(true);
        polygonShape.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.add(polygonShape);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (this.outlet == null) {
            return;
        }
        if (event.getType() == Type.DIGITAL_OUTPUT_CHANGED) {
            if (event.getDigitalOutput() == this.outlet.getDigitalOutput()) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        setState((boolean) event.getValue());
                    }

                });

            }
        }
    }
}
