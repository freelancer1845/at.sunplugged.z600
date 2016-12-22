package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;

public class Viewer implements MachineEventHandler {

    private VentilFigure ventilOne;

    public Viewer(Canvas parent) {
        LightweightSystem ls = new LightweightSystem(parent);
        Figure contents = new Figure();
        XYLayout contentsLayout = new XYLayout();
        contents.setLayoutManager(contentsLayout);
        ventilOne = new VentilFigure("V1", 10, 10);
        IFigure v2 = new VentilFigure("V2", 60, 10);
        IFigure pumpFigure = new PumpFigure("M1P1", 30, 100);
        contents.add(ventilOne);
        contents.add(v2);
        contents.add(pumpFigure);

        PolylineConnection connection = new PolylineConnection();
        ChopboxAnchor sourceAnchor = new ChopboxAnchor(ventilOne);
        ChopboxAnchor targetAnchor = new ChopboxAnchor(pumpFigure);
        connection.setSourceAnchor(sourceAnchor);
        connection.setTargetAnchor(targetAnchor);
        connection.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        contents.add(connection);

        ls.setContents(contents);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.DIGITAL_OUTPUT_CHANGED) {
            if (event.getDigitalOutput() == Outlet.OUTLET_ONE.getDigitalOutput()) {
                ventilOne.setState((boolean) event.getValue());
            }
        }
    }
}
