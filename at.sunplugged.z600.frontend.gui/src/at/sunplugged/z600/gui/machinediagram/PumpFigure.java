package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;

import at.sunplugged.z600.core.machinestate.api.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.PumpControl.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpControl.Pumps;
import at.sunplugged.z600.core.machinestate.api.PumpStateEvent;

public class PumpFigure extends Figure implements MachineEventHandler {

    private static final int WIDTH = 60;

    private static final int HEIGHT = 60;

    private final Pumps pump;

    public PumpFigure(String name, int x, int y, Pumps pump) {
        this.pump = pump;
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        this.setBorder(new LineBorder());
        this.add(createLabel(name));
    }

    private Label createLabel(String name) {
        Label label = new Label();
        Rectangle bounds = getBounds().getCopy();
        label.setBounds(new Rectangle(bounds.x + 5, bounds.y + 5, 40, 20));
        label.setText(name);
        return label;
    }

    private void setState(PumpState state) {

    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.PUMP_STATUS_CHANGED) {
            if (((PumpStateEvent) event).getPump() == pump) {
                setState(((PumpStateEvent) event).getState());
            }
        }
    }
}
