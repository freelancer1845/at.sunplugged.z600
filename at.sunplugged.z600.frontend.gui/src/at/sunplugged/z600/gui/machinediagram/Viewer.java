package at.sunplugged.z600.gui.machinediagram;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PumpControl.Pumps;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class Viewer implements MachineEventHandler {

    private OutletFigure ventilOne;

    private List<MachineEventHandler> eventHandlingFigures = new ArrayList<>();

    public Viewer(Canvas parent) {
        LightweightSystem ls = new LightweightSystem(parent);
        Figure contents = new Figure();
        XYLayout contentsLayout = new XYLayout();
        contents.setLayoutManager(contentsLayout);
        ventilOne = new OutletFigure("V1", 10, 10, Outlet.OUTLET_ONE, false);
        IFigure v2 = new OutletFigure("V2", 60, 10, Outlet.OUTLET_TWO, true);
        eventHandlingFigures.add(ventilOne);
        eventHandlingFigures.add((MachineEventHandler) v2);

        IFigure pumpFigure = new PumpFigure("M1P1", 30, 100, Pumps.PRE_PUMP_ONE);
        eventHandlingFigures.add((MachineEventHandler) pumpFigure);
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
        for (MachineEventHandler figure : eventHandlingFigures) {
            figure.handleEvent(event);
        }
    }
}
