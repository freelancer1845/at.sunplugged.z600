package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

public class VaccumConnection extends Polyline {

    public VaccumConnection(Point... points) {
        for (int i = 0; i < points.length; i++) {
            this.addPoint(points[i]);
        }
        this.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.setLineWidth(2);
    }

    public void setState(boolean state) {
        if (state) {
            this.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
        } else {
            this.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        }
    }

}
