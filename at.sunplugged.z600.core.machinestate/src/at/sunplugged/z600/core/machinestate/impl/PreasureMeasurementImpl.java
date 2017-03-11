package at.sunplugged.z600.core.machinestate.impl;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PressureChangedEvent;

public class PreasureMeasurementImpl implements PressureMeasurement, MachineEventHandler {

    private final MachineStateService machineStateService;

    public PreasureMeasurementImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        machineStateService.registerMachineEventHandler(this);
    }

    @Override
    public double getCurrentValue(PressureMeasurementSite site) {
        switch (site) {
        case TURBO_PUMP:
        case CRYO_PUMP_ONE:
        case CRYO_PUMP_TWO:
            return tm201Site(site, machineStateService.getAnalogInputState(site.getAnalogInput()));
        case CHAMBER:
            return mks979bSite(site, machineStateService.getAnalogInputState(site.getAnalogInput()));
        default:
            return -1;
        }
    }

    private double tm201Site(PressureMeasurementSite site, int measuredValue) {

        double value;
        if (measuredValue < 410) {
            value = Math.exp(((measuredValue / 4095.0) * 10 - 2.7107) / 0.3965);
        } else if (measuredValue >= 410 && measuredValue < 3653) {
            value = Math.exp(((measuredValue / 4095.0) * 10 - 5.8798) / 1.0272);
        } else if (measuredValue >= 3653 && measuredValue < 3931) {
            value = Math.exp(((measuredValue / 4095.0) * 10 - 7.0655) / 0.6191);
        } else if (measuredValue >= 3931 && measuredValue < 4005) {
            value = Math.exp(((measuredValue / 4095.0) * 10 - 8.1573) / 0.3524);
        } else if (measuredValue >= 4005 && measuredValue <= 4095) {
            value = Math.exp(((measuredValue / 4095.0) * 10 - 9.34) / 0.0955);
        } else {
            value = -1;
        }

        return value;
    }

    private double mks979bSite(PressureMeasurementSite site, int measuredValue) {
        return 1.33 * Math.pow(10, ((measuredValue * 10 / 4096.0) * 2) - 11);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.ANALOG_INPUT_CHANGED) {
            switch ((AnalogInput) event.getOrigin()) {
            case PREASURE_CHAMBER:
                machineStateService.fireMachineStateEvent(new PressureChangedEvent(PressureMeasurementSite.CHAMBER,
                        mks979bSite(PressureMeasurementSite.CHAMBER, (int) event.getValue())));
                break;
            case PREASURE_TURBO_PUMP:
                machineStateService.fireMachineStateEvent(new PressureChangedEvent(PressureMeasurementSite.TURBO_PUMP,
                        tm201Site(PressureMeasurementSite.TURBO_PUMP, (int) event.getValue())));
                break;
            case PREASURE_CRYO_ONE:
                machineStateService
                        .fireMachineStateEvent(new PressureChangedEvent(PressureMeasurementSite.CRYO_PUMP_ONE,
                                tm201Site(PressureMeasurementSite.CRYO_PUMP_ONE, (int) event.getValue())));
                break;
            case PREASURE_CRYO_TWO:
                machineStateService
                        .fireMachineStateEvent(new PressureChangedEvent(PressureMeasurementSite.CRYO_PUMP_TWO,
                                tm201Site(PressureMeasurementSite.CRYO_PUMP_TWO, (int) event.getValue())));
                break;
            default:
                break;
            }
        }
    }

}
