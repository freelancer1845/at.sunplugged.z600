package at.sunplugged.z600.backend.vaccum.impl;

import java.io.IOException;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;

public class VacuumUtils {

    public static boolean isCryoEvacuated(PumpIds pump) {
        switch (pump) {
        case CRYO_ONE:
            return VacuumServiceImpl.getMachineStateService().getPressureMeasurmentControl()
                    .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE) < getCryoPumpPressureTrigger();
        case CRYO_TWO:
            return VacuumServiceImpl.getMachineStateService().getPressureMeasurmentControl()
                    .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO) < getCryoPumpPressureTrigger();
        default:
            throw new IllegalArgumentException("Only cryo pumps allowed! (\"" + pump.name() + "\")");
        }
    }

    public static boolean hasChamberPressureReachedTurboPumpStartTrigger() {
        return hasChamberPressureReachedTurboPumpStartTrigger(1.0);
    }

    public static boolean hasChamberPressureReachedTurboPumpStartTrigger(double factor) {
        double chamberPressure = VacuumServiceImpl.getMachineStateService().getPressureMeasurmentControl()
                .getCurrentValue(PressureMeasurementSite.CHAMBER);

        return chamberPressure < getTurboPumpStartTrigger() * factor;
    }

    public static boolean hasTurboPumpReachedTurboPumpStartTrigger(double factor) {
        double turboPumpPressure = VacuumServiceImpl.getMachineStateService().getPressureMeasurmentControl()
                .getCurrentValue(PressureMeasurementSite.TURBO_PUMP);

        return turboPumpPressure < getTurboPumpStartTrigger() * factor;
    }

    public static boolean hasTurboPumpReachedTurboPumpStartTrigger() {
        return hasTurboPumpReachedTurboPumpStartTrigger(1.0);
    }

    public static double getCryoPumpPressureTrigger() {
        return Double
                .valueOf(VacuumServiceImpl.getSettingsService().getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER));
    }

    public static double getTurboPumpStartTrigger() {
        return Double
                .valueOf(VacuumServiceImpl.getSettingsService().getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP));
    }

    public static void closeAllOutlets(OutletControl outletControl) throws IOException {
        for (Outlet outlet : Outlet.values()) {
            outletControl.closeOutlet(outlet);
        }
    }

    public static void closeAllOutletsButSpecified(OutletControl outletControl, Outlet... outlets) throws IOException {
        for (Outlet outlet : Outlet.values()) {
            for (Outlet notToClose : outlets) {
                if (!outlet.equals(notToClose)) {
                    outletControl.closeOutlet(outlet);
                } else {
                    break;
                }
            }
        }
    }

    private VacuumUtils() {

    }

}
