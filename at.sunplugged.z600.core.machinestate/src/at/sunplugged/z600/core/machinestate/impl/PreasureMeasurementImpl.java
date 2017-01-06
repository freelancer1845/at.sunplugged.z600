package at.sunplugged.z600.core.machinestate.impl;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;

public class PreasureMeasurementImpl implements PressureMeasurement {

    private final MachineStateService machineStateService;

    public PreasureMeasurementImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
    }

    @Override
    public double getCurrentValue(PressureMeasurementSite site) {
        switch (site) {
        case TURBO_PUMP:
        case CRYO_PUMP_ONE:
        case CRYO_PUMP_TWO:
            return tm201Site(site);
        case CHAMBER:
            return mks979bSite(site);
        default:
            return -1;
        }
    }

    private double tm201Site(PressureMeasurementSite site) {
        int measuredValue = machineStateService.getAnalogInputState(site.getAnalogInput());

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

    private double mks979bSite(PressureMeasurementSite site) {
        int measuredValue = machineStateService.getAnalogInputState(site.getAnalogInput());

        return 1.33 * Math.pow(10, ((measuredValue * 10 / 4096.0) * 2) - 11);
    }

}
