package at.sunplugged.z600.gui.machinediagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.PumpControl.Pumps;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class Viewer implements MachineEventHandler {

    private static final boolean DEBUG_MODE = true;

    private static final int INITIAL_WIDTH = 549;

    private ScalableLayeredPane contents;

    private OutletFigure[] outletFigures = new OutletFigure[10];

    private PumpFigure[] pumpFigures = new PumpFigure[4];

    private PressureSiteFigure[] pressureSiteFigures = new PressureSiteFigure[4];

    private Map<String, VaccumConnection> connections = new HashMap<>();

    private List<MachineEventHandler> eventHandlingFigures = new ArrayList<>();

    public Viewer(Canvas parent) {
        LightweightSystem ls = new LightweightSystem(parent);
        contents = new ScalableLayeredPane();
        XYLayout contentsLayout = new XYLayout();
        contents.setLayoutManager(contentsLayout);

        createOutletFigures();
        createPumpFigures();
        createPressureSiteFigures();

        for (String connectionName : connections.keySet()) {
            contents.add(connections.get(connectionName));
        }

        for (int i = 0; i < outletFigures.length; i++) {
            if (outletFigures[i] != null) {
                contents.add(outletFigures[i]);
            }
        }

        for (int i = 0; i < pumpFigures.length; i++) {
            if (pumpFigures[i] != null) {
                contents.add(pumpFigures[i]);
            }
        }

        for (int i = 0; i < pressureSiteFigures.length; i++) {
            if (pressureSiteFigures[i] != null) {
                contents.add(pressureSiteFigures[i]);
            }
        }

        ls.setContents(contents);
        if (DEBUG_MODE) {
            Thread viewerDebugThread = new Thread(new DebugRunnable());
            viewerDebugThread.setName("Viewer Debug Thread");
            viewerDebugThread.start();
        }
    }

    private void createOutletFigures() {
        outletFigures[0] = new OutletFigure("V1", 130, 120, Outlet.OUTLET_ONE, false);
        outletFigures[1] = new OutletFigure("V2", 130, 220, Outlet.OUTLET_TWO, false);
        outletFigures[2] = new OutletFigure("V3", 180, 220, Outlet.OUTLET_THREE, false);
        outletFigures[3] = new OutletFigure("V4", 250, 220, Outlet.OUTLET_FOUR, false);
        outletFigures[4] = new OutletFigure("V5", 80, 280, Outlet.OUTLET_FIVE, true);
        outletFigures[5] = new OutletFigure("V6", 390, 280, Outlet.OUTLET_SIX, true);
        outletFigures[6] = new OutletFigure("V7", 30, 220, null, false);
        outletFigures[7] = new OutletFigure("V8", 420, 220, null, false);

        outletFigures[8] = new OutletFigure("V9", 300, 140, Outlet.OUTLET_NINE, false);

        outletFigures[9] = new OutletFigure("Vent", 360, 140, null, false);

    }

    private void createPumpFigures() {
        pumpFigures[0] = new PumpFigure("1M1", 135, 420, Pumps.PRE_PUMP_ONE);
        pumpFigures[1] = new PumpFigure("1M2", 135, 350, Pumps.PRE_PUMP_ROOTS);
        pumpFigures[2] = new PumpFigure("2M2", 230, 350, Pumps.PRE_PUMP_TWO);
        pumpFigures[3] = new PumpFigure("TMP", 134, 154, Pumps.TURBO_PUMP);
    }

    private void createPressureSiteFigures() {
        pressureSiteFigures[0] = new PressureSiteFigure(10, 10, PressureMeasurementSite.CHAMBER);
    }

    private void createConnections() {
        connections.put("1M1-1M2",
                new VaccumConnection(pumpFigures[0].getBounds().getCenter(), pumpFigures[1].getBounds().getCenter()));
        connections.put("1M2-V2",
                new VaccumConnection(pumpFigures[1].getBounds().getCenter(), outletFigures[1].getBounds().getCenter(),
                        new Point(outletFigures[1].getBounds().getCenter().x, outletFigures[1].getBounds().y + 28),
                        new Point(outletFigures[2].getBounds().getCenter().x, outletFigures[1].getBounds().y + 28),
                        outletFigures[2].getBounds().getCenter()));
        connections.put("2M1-V4", new VaccumConnection(pumpFigures[2].getBounds().getCenter(),
                new Point(pumpFigures[2].getBounds().getCenter().x, outletFigures[4].getBounds().getCenter().y),
                outletFigures[5].getBounds().getCenter(), outletFigures[4].getBounds().getCenter(),
                new Point(outletFigures[3].getBounds().getCenter().x, outletFigures[4].getBounds().getCenter().y),
                outletFigures[3].getBounds().getCenter()));
        connections.put("V2-TMP",
                new VaccumConnection(outletFigures[1].getBounds().getCenter(), pumpFigures[3].getBounds().getCenter()));
        connections.put("TMP-V1",
                new VaccumConnection(pumpFigures[3].getBounds().getCenter(), outletFigures[0].getBounds().getCenter()));
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        for (MachineEventHandler figure : eventHandlingFigures) {
            figure.handleEvent(event);
        }
    }

    private class DebugRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println(contents.getSize().toString());
                        contents.removeAll();
                        createConnections();
                        createOutletFigures();
                        createPumpFigures();
                        createPressureSiteFigures();

                        for (String connectionName : connections.keySet()) {
                            contents.add(connections.get(connectionName));
                        }

                        for (int i = 0; i < outletFigures.length; i++) {
                            if (outletFigures[i] != null) {
                                contents.add(outletFigures[i]);
                            }
                        }

                        for (int i = 0; i < pumpFigures.length; i++) {
                            if (pumpFigures[i] != null) {
                                contents.add(pumpFigures[i]);
                            }
                        }

                        for (int i = 0; i < pressureSiteFigures.length; i++) {
                            if (pressureSiteFigures[i] != null) {
                                contents.add(pressureSiteFigures[i]);
                            }
                        }
                    }

                });
            }

        }

    }
}
