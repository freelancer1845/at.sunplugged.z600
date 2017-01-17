package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.eventhandling.OutletChangedEvent;
import at.sunplugged.z600.core.machinestate.impl.outlets.vat.VatOutlet;
import at.sunplugged.z600.mbt.api.MbtService;

public class OutletControlImpl implements OutletControl {

    private final MachineStateService machineStateService;

    private MbtService mbtController;

    private SettingsService settingsService;

    private LogService logService;

    private VatOutlet vatSeven;

    private VatOutlet vatEight;

    public OutletControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtController = MachineStateServiceImpl.getMbtService();
        this.settingsService = MachineStateServiceImpl.getSettingsService();
        this.logService = MachineStateServiceImpl.getLogService();
        try {
            this.vatSeven = new VatOutlet("COM3", machineStateService);
            this.vatEight = new VatOutlet("COM4", machineStateService);
        } catch (IllegalStateException e) {
            logService.log(LogService.LOG_ERROR, "Couldn't connect to VAT Outlet", e);
        }

    }

    @Override
    public boolean isOutletOpen(Outlet outlet) {
        switch (outlet) {
        case OUTLET_SEVEN:
            return vatSeven.isOpen();
        case OUTLET_EIGHT:
            return vatEight.isOpen();
        case OUTLET_ONE:
        case OUTLET_TWO:
        case OUTLET_THREE:
        case OUTLET_FOUR:
        case OUTLET_FIVE:
        case OUTLET_SIX:
        case OUTLET_NINE:
            return machineStateService.getDigitalOutputState(outlet.getDigitalOutput());
        default:
            return false;
        }

    }

    @Override
    public void closeOutlet(Outlet outlet) throws IOException {
        switch (outlet) {
        case OUTLET_SEVEN:
            vatSeven.close();
            break;
        case OUTLET_EIGHT:
            vatEight.close();
            break;
        case OUTLET_ONE:
        case OUTLET_TWO:
        case OUTLET_THREE:
        case OUTLET_FOUR:
        case OUTLET_FIVE:
        case OUTLET_SIX:
        case OUTLET_NINE:
            mbtController.writeDigOut(outlet.getDigitalOutput().getAddress(), false);
            machineStateService.fireMachineStateEvent(new OutletChangedEvent(outlet, false));
            break;
        }

    }

    @Override
    public void openOutlet(Outlet outlet) throws IOException {
        if (IsSafetyProtocolEnabeld()) {
            if (checkSafetyProtocol(outlet, true) == false) {
                logService.log(LogService.LOG_WARNING, "Opening " + outlet.name() + " failed.");
                return;
            }
        }
        switch (outlet) {
        case OUTLET_SEVEN:
            vatSeven.open();
            break;
        case OUTLET_EIGHT:
            vatEight.open();
            break;
        case OUTLET_ONE:
        case OUTLET_TWO:
        case OUTLET_THREE:
        case OUTLET_FOUR:
        case OUTLET_FIVE:
        case OUTLET_SIX:
        case OUTLET_NINE:
            mbtController.writeDigOut(outlet.getDigitalOutput().getAddress(), true);
            machineStateService.fireMachineStateEvent(new OutletChangedEvent(outlet, true));
            break;
        }
    }

    @Override
    public void setVatOutletPosition(Outlet outlet, int position) {
        switch (outlet) {
        case OUTLET_SEVEN:
            vatSeven.setPosition(position);
            break;
        case OUTLET_EIGHT:
            vatEight.setPosition(position);
            break;
        default:
            logService.log(LogService.LOG_ERROR, "Outlet: \"" + outlet.name()
                    + "\" is not a VAT Outlet. Can't set position to: \"" + position + "\"");
            throw new InvalidParameterException("Not a VAT Outlet");
        }
    }

    @Override
    public int getVatOutletPosition(Outlet outlet) {
        switch (outlet) {
        case OUTLET_SEVEN:
            return vatSeven.getPosition();
        case OUTLET_EIGHT:
            return vatEight.getPosition();
        default:
            logService.log(LogService.LOG_ERROR,
                    "Outlet: \"" + outlet.name() + "\" is not a VAT Outlet. Can't get position.");
            throw new InvalidParameterException("Not a VAT Outlet");
        }
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
