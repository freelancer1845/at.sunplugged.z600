package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;

public class PumpFigure extends Figure {

    private static final int WIDTH = 60;

    private static final int HEIGHT = 60;

    public PumpFigure(String name, int x, int y) {
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
}
