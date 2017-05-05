package at.sunplugged.z600.gui.machinediagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.OutletChangedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;
import at.sunplugged.z600.gui.views.MainView;

public class Viewer implements MachineEventHandler {

    private static final boolean DEBUG_MODE = true;

    private final int widthRatio;

    private ScalableLayeredPane contents;

    private OutletFigure[] outletFigures = new OutletFigure[10];

    private PumpFigure[] pumpFigures = new PumpFigure[5];

    private PressureSiteFigure[] pressureSiteFigures = new PressureSiteFigure[4];

    private ChamberFigure chamberFigure;

    private ConveyorFigure conveyorFigure;

    private MiscInformationFigure miscInformationFigure;

    private Map<String, VacuumConnection> connections = new HashMap<>();

    private List<MachineEventHandler> eventHandlingFigures = new ArrayList<>();

    public Viewer(Canvas parent) {
        org.eclipse.swt.graphics.Rectangle shellSize = parent.getShell().getClientArea();
        org.eclipse.swt.graphics.Rectangle canvasSize = parent.getClientArea();
        widthRatio = canvasSize.width / shellSize.width;

        LightweightSystem ls = new LightweightSystem(parent);
        contents = new ScalableLayeredPane();
        parent.getShell().addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                org.eclipse.swt.graphics.Rectangle shellSize = parent.getShell().getClientArea();
                int shellWidth = shellSize.width;
                contents.setScale(((double) shellWidth / 1800 + 0.54));
            }
        });
        contents.setScale(1.0);
        XYLayout contentsLayout = new XYLayout();
        contents.setLayoutManager(contentsLayout);

        createOutletFigures();
        createPumpFigures();
        createPressureSiteFigures();
        createChamberFigure();
        createConnections();
        createConveyorFigure();
        createMiscInformationFigure();

        for (String connectionName : connections.keySet()) {
            if (connectionName.equals("2M1-V4")) {
                RectangleFigure crossingFigure = new RectangleFigure();
                crossingFigure.setBounds(new Rectangle(new Point(outletFigures[1].getBounds().getCenter().x - 5,
                        outletFigures[4].getBounds().getCenter().y - 5), new Dimension(10, 10)));
                crossingFigure.setOpaque(true);
                crossingFigure.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
                crossingFigure.setOutline(false);
                contents.add(crossingFigure);
            }
            contents.add(connections.get(connectionName));
            eventHandlingFigures.add(connections.get(connectionName));
        }

        for (int i = 0; i < outletFigures.length; i++) {
            if (outletFigures[i] != null) {
                contents.add(outletFigures[i]);
                eventHandlingFigures.add(outletFigures[i]);
            }
        }

        for (int i = 0; i < pumpFigures.length; i++) {
            if (pumpFigures[i] != null) {
                contents.add(pumpFigures[i]);
                eventHandlingFigures.add(pumpFigures[i]);
            }
        }

        for (int i = 0; i < pressureSiteFigures.length; i++) {
            if (pressureSiteFigures[i] != null) {
                contents.add(pressureSiteFigures[i]);
                eventHandlingFigures.add(pressureSiteFigures[i]);
            }
        }

        contents.add(chamberFigure);
        contents.add(conveyorFigure);
        contents.add(miscInformationFigure);
        eventHandlingFigures.add(chamberFigure);
        eventHandlingFigures.add(conveyorFigure);

        ls.setContents(contents);
        if (DEBUG_MODE) {
            Thread viewerDebugThread = new Thread(new DebugRunnable());
            viewerDebugThread.setName("Viewer Debug Thread");
            viewerDebugThread.start();
        }
    }

    private void createOutletFigures() {
        outletFigures[0] = new OutletFigure("V1", 130, 120, Outlet.OUTLET_ONE, false) {

        };
        outletFigures[1] = new OutletFigure("V2", 130, 245, Outlet.OUTLET_TWO, false);
        outletFigures[2] = new OutletFigure("V3", 180, 245, Outlet.OUTLET_THREE, false);
        outletFigures[3] = new OutletFigure("V4", 250, 220, Outlet.OUTLET_FOUR, false);
        outletFigures[4] = new OutletFigure("V5", 90, 280, Outlet.OUTLET_FIVE, true);
        outletFigures[5] = new OutletFigure("V6", 390, 280, Outlet.OUTLET_SIX, true);
        outletFigures[6] = new OutletFigure("V7", 30, 220, Outlet.OUTLET_SEVEN, false);
        outletFigures[7] = new OutletFigure("V8", 420, 220, Outlet.OUTLET_EIGHT, false);

        outletFigures[8] = new OutletFigure("V9", 300, 140, Outlet.OUTLET_NINE, false);

        outletFigures[9] = new OutletFigure("Vent", 360, 140, null, false);

    }

    private void createPumpFigures() {
        pumpFigures[0] = new PumpFigure("1M1", 130, 420, PumpIds.PRE_PUMP_ONE);
        pumpFigures[1] = new PumpFigure("1M2", 130, 350, PumpIds.PRE_PUMP_ROOTS);
        pumpFigures[2] = new PumpFigure("2M2", 230, 350, PumpIds.PRE_PUMP_TWO);
        pumpFigures[3] = new PumpFigure("TMP", 130, 154, PumpIds.TURBO_PUMP);
        pumpFigures[4] = new PumpFigure("Wasser", 280, 420, null) {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType() == Type.DIGITAL_INPUT_CHANGED) {
                    if (event.getOrigin().equals(DigitalInput.COOLING_PUMP_OK)) {
                        if ((boolean) event.getValue() == true) {
                            setState(PumpState.ON);
                        } else {
                            setState(PumpState.OFF);
                        }
                    }
                }
            }

        };
    }

    private void createPressureSiteFigures() {
        pressureSiteFigures[0] = new PressureSiteFigure(136, 220, PressureMeasurementSite.TURBO_PUMP);
        pressureSiteFigures[1] = new PressureSiteFigure(40, 243, PressureMeasurementSite.CRYO_PUMP_ONE);
        pressureSiteFigures[2] = new PressureSiteFigure(430, 243, PressureMeasurementSite.CRYO_PUMP_TWO);

    }

    private void createConnections() {
        connections.put("1M1-1M2",
                new VacuumConnection(pumpFigures[0].getBounds().getCenter(), pumpFigures[1].getBounds().getCenter()) {

                    @Override
                    public void handleEvent(MachineStateEvent event) {
                        if (event.getType().equals(Type.PUMP_STATUS_CHANGED)) {
                            PumpStateEvent pumpEvent = (PumpStateEvent) event;
                            if (pumpEvent.getOrigin().equals(PumpIds.PRE_PUMP_ONE)) {
                                if (pumpEvent.getValue().equals(PumpState.ON)) {
                                    this.setState(true);
                                } else if (pumpEvent.getValue().equals(PumpState.OFF)) {
                                    this.setState(false);
                                }
                            }
                        }
                    }

                    @Override
                    protected void initSpecific() {
                        PumpState state = MainView.getMachineStateService().getPumpRegistry()
                                .getPump(PumpIds.PRE_PUMP_ONE).getState();
                        if (state == PumpState.ON) {
                            this.setState(true);
                        } else {
                            this.setState(false);
                        }
                    }

                });
        connections.put("1M2-V2",
                new VacuumConnection(pumpFigures[1].getBounds().getCenter(), outletFigures[1].getBounds().getCenter(),
                        new Point(outletFigures[1].getBounds().getCenter().x, outletFigures[1].getBounds().y + 28),
                        new Point(outletFigures[2].getBounds().getCenter().x, outletFigures[1].getBounds().y + 28),
                        outletFigures[2].getBounds().getCenter()) {

                    @Override
                    public void handleEvent(MachineStateEvent event) {
                        if (event.getType().equals(Type.PUMP_STATUS_CHANGED)) {
                            PumpStateEvent pumpEvent = (PumpStateEvent) event;
                            if (pumpEvent.getOrigin().equals(PumpIds.PRE_PUMP_ROOTS)) {
                                if (pumpEvent.getValue().equals(PumpState.ON)) {
                                    this.setState(true);
                                } else if (pumpEvent.getValue().equals(PumpState.OFF)) {
                                    this.setState(false);
                                }
                            }
                        }
                    }

                    @Override
                    protected void initSpecific() {
                        PumpState state = MainView.getMachineStateService().getPumpRegistry()
                                .getPump(PumpIds.PRE_PUMP_ROOTS).getState();
                        if (state == PumpState.ON) {
                            this.setState(true);
                        } else {
                            this.setState(false);
                        }
                    }

                });

        connections.put("2M1-V4", new VacuumConnection(pumpFigures[2].getBounds().getCenter(),
                new Point(pumpFigures[2].getBounds().getCenter().x, outletFigures[4].getBounds().getCenter().y),
                outletFigures[5].getBounds().getCenter(), outletFigures[4].getBounds().getCenter(),
                new Point(outletFigures[3].getBounds().getCenter().x, outletFigures[4].getBounds().getCenter().y),
                outletFigures[3].getBounds().getCenter()) {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.PUMP_STATUS_CHANGED)) {
                    PumpStateEvent pumpEvent = (PumpStateEvent) event;
                    if (pumpEvent.getOrigin().equals(PumpIds.PRE_PUMP_TWO)) {
                        if (pumpEvent.getValue().equals(PumpState.ON)) {
                            this.setState(true);
                        } else if (pumpEvent.getValue().equals(PumpState.OFF)) {
                            this.setState(false);
                        }
                    }
                }
            }

            @Override
            protected void initSpecific() {
                PumpState state = MainView.getMachineStateService().getPumpRegistry().getPump(PumpIds.PRE_PUMP_TWO)
                        .getState();
                if (state == PumpState.ON) {
                    this.setState(true);
                } else {
                    this.setState(false);
                }
            }

        });
        connections.put("V2-TMP",
                new VacuumConnection(outletFigures[1].getBounds().getCenter(), pumpFigures[3].getBounds().getCenter()) {

                    @Override
                    public void handleEvent(MachineStateEvent event) {
                        if (event.getType().equals(Type.OUTLET_CHANGED)) {
                            OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                            if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_TWO)) {
                                if ((boolean) outletChangedEvent.getValue() == true) {
                                    this.setState(true);
                                } else {
                                    this.setState(false);
                                }
                            }
                        }
                    }

                });
        connections.put("TMP-V1",
                new VacuumConnection(pumpFigures[3].getBounds().getCenter(), outletFigures[0].getBounds().getCenter()) {

                    @Override
                    public void handleEvent(MachineStateEvent event) {
                        if (event.getType().equals(Type.OUTLET_CHANGED)) {
                            OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                            if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_TWO)) {
                                if ((boolean) outletChangedEvent.getValue() == true) {
                                    this.setState(true);
                                } else {
                                    this.setState(false);
                                }
                            }
                        }
                    }

                });
        connections.put("V1-Chamber", new VacuumConnection(outletFigures[0].getBounds().getCenter(),
                new Point(outletFigures[0].getBounds().getCenter().x, chamberFigure.getBounds().getCenter().y)) {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.OUTLET_CHANGED)) {
                    OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                    if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_ONE)) {
                        if ((boolean) outletChangedEvent.getValue() == true) {
                            this.setState(true);
                        } else {
                            this.setState(false);
                        }
                    }
                }
            }

        });
        connections.put("V3-Chamber", new VacuumConnection(outletFigures[2].getBounds().getCenter(),
                new Point(outletFigures[2].getBounds().getCenter().x, chamberFigure.getBounds().getCenter().y)) {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.OUTLET_CHANGED)) {
                    OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                    if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_THREE)) {
                        if ((boolean) outletChangedEvent.getValue() == true) {
                            this.setState(true);
                        } else {
                            this.setState(false);
                        }
                    }
                }
            }

        });
        connections.put("V4-Chamber", new VacuumConnection(outletFigures[3].getBounds().getCenter(),
                new Point(outletFigures[3].getBounds().getCenter().x, chamberFigure.getBounds().getCenter().y)) {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.OUTLET_CHANGED)) {
                    OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                    if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_FOUR)) {
                        if ((boolean) outletChangedEvent.getValue() == true) {
                            this.setState(true);
                        } else {
                            this.setState(false);
                        }
                    }
                }
            }

        });
        connections.put("V9-Chamber", new VacuumConnection(outletFigures[8].getBounds().getCenter(),
                new Point(outletFigures[8].getBounds().getCenter().x, chamberFigure.getBounds().getCenter().y)) {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType().equals(Type.OUTLET_CHANGED)) {
                    OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                    if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_NINE)) {
                        if ((boolean) outletChangedEvent.getValue() == true) {
                            this.setState(true);
                        } else {
                            this.setState(false);
                        }
                    }
                }
            }

        });
        connections.put("Gas-V9",
                new VacuumConnection(
                        new Point(outletFigures[8].getBounds().getCenter().x,
                                outletFigures[8].getBounds().getCenter().y + 25),
                        outletFigures[8].getBounds().getCenter()));
        connections.put("V7-Chamber",
                new VacuumConnection(outletFigures[6].getBounds().getCenter(),
                        new Point(outletFigures[6].getBounds().getCenter().x, chamberFigure.getBounds().getCenter().y),
                        chamberFigure.getBounds().getCenter()) {

                    @Override
                    public void handleEvent(MachineStateEvent event) {
                        if (event.getType().equals(Type.OUTLET_CHANGED)) {
                            OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                            if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_SEVEN)) {
                                if ((boolean) outletChangedEvent.getValue() == true) {
                                    this.setState(true);
                                } else {
                                    this.setState(false);
                                }
                            }
                        }
                    }

                });
        connections.put("V8-Chamber",
                new VacuumConnection(outletFigures[7].getBounds().getCenter(),
                        new Point(outletFigures[7].getBounds().getCenter().x, chamberFigure.getBounds().getCenter().y),
                        chamberFigure.getBounds().getCenter()) {

                    @Override
                    public void handleEvent(MachineStateEvent event) {
                        if (event.getType().equals(Type.OUTLET_CHANGED)) {
                            OutletChangedEvent outletChangedEvent = (OutletChangedEvent) event;
                            if (outletChangedEvent.getOutlet().equals(Outlet.OUTLET_EIGHT)) {
                                if ((boolean) outletChangedEvent.getValue() == true) {
                                    this.setState(true);
                                } else {
                                    this.setState(false);
                                }
                            }
                        }
                    }

                });
    }

    private void createChamberFigure() {

        chamberFigure = new ChamberFigure(90, 50);

    }

    private void createConveyorFigure() {
        conveyorFigure = new ConveyorFigure(50, 500);
    }

    private void createMiscInformationFigure() {
        miscInformationFigure = new MiscInformationFigure(380, 440);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (DEBUG_MODE == true) {
            return;
        }
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
                        // System.out.println(contents.getSize().toString());
                        contents.removeAll();
                        createConnections();
                        createOutletFigures();
                        createPumpFigures();
                        createPressureSiteFigures();
                        createChamberFigure();
                        createConveyorFigure();
                        createMiscInformationFigure();

                        for (String connectionName : connections.keySet()) {
                            contents.add(connections.get(connectionName));
                        }

                        for (int i = 0; i < outletFigures.length; i++) {
                            if (outletFigures[i] != null) {
                                contents.add(outletFigures[i]);
                                eventHandlingFigures.add(outletFigures[i]);
                            }
                        }

                        for (int i = 0; i < pumpFigures.length; i++) {
                            if (pumpFigures[i] != null) {
                                contents.add(pumpFigures[i]);
                                eventHandlingFigures.add(pumpFigures[i]);
                            }
                        }

                        for (int i = 0; i < pressureSiteFigures.length; i++) {
                            if (pressureSiteFigures[i] != null) {
                                contents.add(pressureSiteFigures[i]);
                                eventHandlingFigures.add(pressureSiteFigures[i]);
                            }
                        }

                        contents.add(chamberFigure);
                        contents.add(conveyorFigure);
                        contents.add(miscInformationFigure);
                        eventHandlingFigures.add(chamberFigure);
                        eventHandlingFigures.add(conveyorFigure);
                    }

                });
            }

        }

    }
}
