package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.PumpControl.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpControl.Pumps;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;

public class PumpFigure extends Figure implements MachineEventHandler {

    private static final int WIDTH = 60;

    private static final int HEIGHT = 60;

    private static final double INNER_CIRCLE_SCALE = 4;

    private final Pumps pump;

    private Ellipse innerCircle;

    public PumpFigure(String name, int x, int y, Pumps pump) {
        this.pump = pump;
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        // this.setLocation(new Point(x, y));
        // this.setSize(WIDTH, HEIGHT);
        createShape();
        createLabel(name);
    }

    private void createLabel(String name) {
        Label label = new Label();
        Rectangle bounds = getBounds().getCopy();
        label.setBounds(new Rectangle(bounds.x + WIDTH / 2 - 20, bounds.y + (int) (HEIGHT / 1.7), 40, 20));
        label.setText(name);
        label.setFont(SWTResourceManager.getFont("tahoma", 8, SWT.NONE));
        this.add(label);

    }

    private void createShape() {
        Rectangle bounds = getBounds().getCopy();

        Shape shape = new Ellipse();
        shape.setSize(WIDTH - 1, HEIGHT - 1);
        shape.setBounds(getBounds().getCopy());
        this.add(shape);
        innerCircle = new Ellipse();
        innerCircle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_GRAY));
        innerCircle.setLineWidth(0);
        innerCircle.setAntialias(100);
        innerCircle.setBounds(new Rectangle(bounds.x + bounds.width / 2 - (int) (WIDTH / INNER_CIRCLE_SCALE / 2),
                bounds.y + bounds.height / 2 - (int) (HEIGHT / INNER_CIRCLE_SCALE / 2),
                (int) (WIDTH / INNER_CIRCLE_SCALE), (int) (WIDTH / INNER_CIRCLE_SCALE)));

        this.add(innerCircle);

    }

    protected void setState(PumpState state) {
        switch (state) {
        case ON:
            innerCircle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
            break;
        case OFF:
            innerCircle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
            break;
        case FAILED:
            innerCircle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
            break;
        case STARTING:
            innerCircle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW));
            break;
        case STOPPING:
            innerCircle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            break;
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.PUMP_STATUS_CHANGED) {
            if (((PumpStateEvent) event).getPump() == pump) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        setState(((PumpStateEvent) event).getState());
                    }

                });

            }
        }
    }
}
