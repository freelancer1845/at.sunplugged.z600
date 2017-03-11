package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

public class KathodeFigure extends Figure {

    private static final int WIDTH = 65;

    private static final int HEIGHT = 22;

    private RectangleFigure innerRectangle;

    public KathodeFigure(int x, int y, String name) {
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        createShape();
        createLabel(name);
    }

    private void createShape() {
        Polyline outerLine = new Polyline();
        Rectangle bounds = getBounds().getCopy();
        bounds.width -= 1;
        bounds.height -= 1;

        Point lowerLeftPoint = new Point(bounds.x, bounds.y + bounds.height);
        Point upperLeftPoint = new Point(bounds.x, bounds.y);
        Point upperRightPoint = new Point(bounds.x + bounds.width, bounds.y);
        Point lowerRightPoint = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
        outerLine.addPoint(lowerLeftPoint);
        outerLine.addPoint(upperLeftPoint);
        outerLine.addPoint(upperRightPoint);
        outerLine.addPoint(lowerRightPoint);

        this.add(outerLine);

        innerRectangle = new RectangleFigure();
        innerRectangle.setBounds(new Rectangle(bounds.x + 10, (int) (bounds.y + bounds.height * (1 - 1 / 3.0)),
                bounds.width - 20, bounds.height / 3));
        innerRectangle.setFill(true);
        innerRectangle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_CYAN));
        this.add(innerRectangle);
    }

    private void createLabel(String name) {
        Label label = new Label();
        Rectangle bounds = getBounds().getCopy();
        bounds.y += 2;
        label.setBounds(bounds);
        label.setText(name);
        label.setTextAlignment(Label.TOP);
        label.setFont(SWTResourceManager.getFont("tahoma", 7, SWT.NONE));
        this.add(label);
    }

}
