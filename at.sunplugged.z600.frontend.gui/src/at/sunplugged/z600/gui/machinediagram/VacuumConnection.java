package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class VacuumConnection extends Polyline implements MachineEventHandler {

    public VacuumConnection(Point... points) {
        for (int i = 0; i < points.length; i++) {
            this.addPoint(points[i]);
        }
        this.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.setLineWidth(2);
    }

    public void setState(boolean state) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (state) {
                    setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                } else {
                    setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
                }
            }

        });

    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        // TODO Auto-generated method stub

    }

}
