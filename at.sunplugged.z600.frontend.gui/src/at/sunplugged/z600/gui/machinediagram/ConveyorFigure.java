package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class ConveyorFigure extends Figure implements MachineEventHandler {

    private static int WIDTH = 420;

    private static int HEIGHT = 100;

    private static int LABEL_WIDTH = WIDTH / 3;

    private static int LABEL_HEIGHT = 100 / 5;

    private Label speedLeftLabel;

    private Label positionLeftLabel;

    private Label correctionLeftLabel;

    public ConveyorFigure(int x, int y) {
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        this.setBorder(new LineBorder());
        this.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        this.setOpaque(true);
        createLabels();

    }

    private void createLabels() {
        Rectangle parent = getBounds().getCopy();
        speedLeftLabel = new Label("speedLeftLabel");
        speedLeftLabel.setBounds(new Rectangle(parent.x + 5, parent.y + 5, LABEL_WIDTH, LABEL_HEIGHT));
        this.add(speedLeftLabel);
        positionLeftLabel = new Label("positionLeftLabel");
        positionLeftLabel
                .setBounds(new Rectangle(parent.x + 5, parent.y + 5 + LABEL_HEIGHT, LABEL_WIDTH, LABEL_HEIGHT));
        this.add(positionLeftLabel);
        correctionLeftLabel = new Label("correctionLeftLabel");
        correctionLeftLabel
                .setBounds(new Rectangle(parent.x + 5, parent.y + 5 + 2 * LABEL_HEIGHT, LABEL_WIDTH, LABEL_HEIGHT));
        this.add(correctionLeftLabel);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        // TODO Auto-generated method stub

    }

}
