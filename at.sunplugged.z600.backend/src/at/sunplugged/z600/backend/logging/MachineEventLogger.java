package at.sunplugged.z600.backend.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.OutletChangedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PowerSourceEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PressureChangedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;

@Component(immediate = true)
public class MachineEventLogger implements MachineEventHandler {

    private static final String EVENT_LOG_FILENAME = "eventLog.txt";

    private MachineStateService machineStateService;

    private StandardThreadPoolService threadPool;

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        this.threadPool = threadPool;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        if (this.threadPool == threadPool) {
            this.threadPool = null;
        }
    }

    @Reference(unbind = "unbindMachineStateService", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        machineStateService.registerMachineEventHandler(this);
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (this.machineStateService == machineStateService) {
            this.machineStateService = null;
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                logToFile(event);
            }
        });
    }

    private void logToFile(MachineStateEvent event) {
        File eventLog = new File(EVENT_LOG_FILENAME);
        if (eventLog.exists() == false) {
            try {
                eventLog.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try (FileWriter fw = new FileWriter(EVENT_LOG_FILENAME, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);) {

            String logEntry = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            logEntry += " - Type: \"";
            logEntry += event.getType().name();
            logEntry += "\" - ";
            switch (event.getType()) {
            case ANALOG_INPUT_CHANGED:
            case ANALOG_OUTPUT_CHANGED:
            case DIGITAL_INPUT_CHANGED:
            case DIGITAL_OUTPUT_CHANGED:
                logEntry += ((Enum<?>) event.getOrigin()).name() + " Value: \"" + event.getValue() + "\"";
                break;
            case CONVEYOR_EVENT:
                logEntry += event.getValue().toString();
                break;
            case GAS_FLOW_STATE_CHANGED:
                logEntry += event.getValue().toString();
                break;
            case OUTLET_CHANGED:
                OutletChangedEvent outletEvent = (OutletChangedEvent) event;
                logEntry += outletEvent.getOutlet().name() + " New State: " + outletEvent.getValue().toString();
                break;
            case PRESSURE_CHANGED:
                PressureChangedEvent pressureChangedEvent = (PressureChangedEvent) event;
                logEntry += pressureChangedEvent.getOrigin().name() + " New Pressure: "
                        + String.valueOf(pressureChangedEvent.getValue());
                break;
            case PUMP_STATUS_CHANGED:
                PumpStateEvent pumpStateEvent = (PumpStateEvent) event;
                logEntry += pumpStateEvent.getOrigin().name() + " New State: " + pumpStateEvent.getValue().name();
                break;
            case POWER_SOURCE_STATE_CHANGED:
                PowerSourceEvent powerSourceEvent = (PowerSourceEvent) event;
                logEntry += powerSourceEvent.getOrigin().name() + " New State: " + powerSourceEvent.getValue().name();
                break;
            default:
                logEntry += "Unlogged Event...";
                break;
            }
            out.println(logEntry);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
