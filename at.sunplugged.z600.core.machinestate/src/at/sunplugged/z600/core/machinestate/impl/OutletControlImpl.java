package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.mbt.api.MbtService;

public class OutletControlImpl implements OutletControl {

    private final MachineStateService machineStateService;

    private MbtService mbtController;

    private SettingsService settingsService;

    private LogService logService;

    public OutletControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtController = MachineStateServiceImpl.getMbtService();
        this.settingsService = MachineStateServiceImpl.getSettingsService();
        this.logService = MachineStateServiceImpl.getLogService();
    }

    @Override
    public boolean isOutletOpen(Outlet outlet) {
        return machineStateService.getDigitalOutputState(outlet.getDigitalOutput());
    }

    @Override
    public void closeOutlet(Outlet outlet) throws IOException {
        mbtController.writeDigOut(outlet.getDigitalOutput().getAddress(), false);
    }

    @Override
    public void openOutlet(Outlet outlet) throws IOException {
        if (IsSafetyProtocolEnabeld()) {
            if (checkSafetyProtocol(outlet, true) == false) {
                logService.log(LogService.LOG_WARNING, "Opening " + outlet.name() + " failed.");
                return;
            }
        }
        mbtController.writeDigOut(outlet.getDigitalOutput().getAddress(), true);
    }

    private boolean IsSafetyProtocolEnabeld() {
        return Boolean.getBoolean(settingsService.getProperty(SettingsIds.SAFETY_PROTOCOLS_OUTLETS));
    }

    private boolean checkSafetyProtocol(Outlet outlet, boolean newState) {

        switch (outlet) {
        case OUTLET_ONE:
            if (newState == true) {
                double chamberPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CHAMBER);
                double turboPumpPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.TURBO_PUMP);

                if (chamberPressure <= 0 && turboPumpPressure <= 0) {
                    logService.log(LogService.LOG_INFO,
                            safetyProtocolMessage(outlet, newState, "Pressures were negative!"));
                    return false;
                }
                if (chamberPressure < turboPumpPressure) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Pressure in Chamber is lower than pressure at the Turbo Pump!"));
                    return false;
                }
            }
            break;
        case OUTLET_TWO:
            if (newState == true) {
                if (isOutletOpen(Outlet.OUTLET_ONE) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Opening Outlet Two when Outlet One is open is forbidden. Could result in rapid pressure decrease at Pre Pumps!"));
                    return false;
                }
            }
            break;
        case OUTLET_THREE:
        case OUTLET_FOUR:
            if (newState == true) {
                double chamberPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CHAMBER);
                double turboPumpPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.TURBO_PUMP);
                if (chamberPressure < turboPumpPressure) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Pressure in chamber is potentially lower than at pre Pumps!"));
                    return false;
                }
            }
            break;
        case OUTLET_FIVE:
            if (newState == true) {
                double cryoOnePressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE);
                double turboPumpPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.TURBO_PUMP);
                if (cryoOnePressure < turboPumpPressure) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Pressure in at CryoPump One is potentially lower than at pre Pumps!"));
                    return false;
                }
            }
            break;
        case OUTLET_SIX:
            if (newState == true) {
                double cryoTwoPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO);
                double turboPumpPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.TURBO_PUMP);
                if (cryoTwoPressure < turboPumpPressure) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Pressure in at CryoPump Two is potentially lower than at pre Pumps!"));
                    return false;
                }
            }
            break;

        default:
            break;
        }
        return true;
    }

    private String safetyProtocolMessage(Outlet outlet, boolean newState, String reason) {
        String message = "SafetyProtocol failed for ";
        if (newState == true) {
            message += "opening ";
        } else {
            message += "closing ";
        }
        message += "Outlet: ";
        message += outlet.name();
        message += " . Reason: \"";
        message += "reason" + "\"";
        return message;
    }

}
