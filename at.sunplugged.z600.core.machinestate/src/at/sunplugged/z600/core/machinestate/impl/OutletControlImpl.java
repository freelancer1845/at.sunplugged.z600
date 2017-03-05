package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.ParameterIds;
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
            this.vatSeven = new VatOutlet(settingsService.getProperty(NetworkComIds.VAT_SEVEN_COM_PORT),
                    machineStateService);
        } catch (IllegalStateException e) {
            logService.log(LogService.LOG_ERROR, "Couldn't connect to VAT Outlet Seven", e);
        }
        try {
            this.vatEight = new VatOutlet(settingsService.getProperty(NetworkComIds.VAT_EIGHT_COM_PORT),
                    machineStateService);
        } catch (IllegalStateException e) {
            logService.log(LogService.LOG_ERROR, "Couldn't connect to VAT Outlet Eight", e);
        }

    }

    @Override
    public boolean isOutletOpen(Outlet outlet) {
        switch (outlet) {
        case OUTLET_SEVEN:
            if (vatSeven == null) {
                return false;
            }
            return vatSeven.isOpen();
        case OUTLET_EIGHT:
            if (vatEight == null) {
                return false;
            }
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
            if (vatSeven == null) {
                return;
            }
            vatSeven.close();
            break;
        case OUTLET_EIGHT:
            if (vatEight == null) {
                return;
            }
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
            break;
        }
        machineStateService.fireMachineStateEvent(new OutletChangedEvent(outlet, false));

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
            break;
        }
        machineStateService.fireMachineStateEvent(new OutletChangedEvent(outlet, true));
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
        return Boolean.valueOf(settingsService.getProperty(ParameterIds.SAFETY_PROTOCOLS_OUTLETS));
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
                if (isOutletOpen(Outlet.OUTLET_THREE)) {
                    logService.log(LogService.LOG_INFO,
                            safetyProtocolMessage(outlet, newState, "Outlet Three is open!"));
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
                if (isOutletOpen(Outlet.OUTLET_THREE) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Can't open outlet two when outlet three is open!"));
                    return false;
                }
            }
            break;
        case OUTLET_THREE:
            if (newState == true) {
                double chamberPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CHAMBER);
                double turboPumpTrigger = Double
                        .valueOf(settingsService.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP));
                if (chamberPressure < 0.8 * turboPumpTrigger) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Chamer pressure is lower than 80% of turbo pump trigger pressure. Won't open outlet three."));
                    return false;
                }
                if (isOutletOpen(Outlet.OUTLET_ONE) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Opening Outlet Three when Outlet One is open is forbidden!"));
                    return false;
                }
                if (isOutletOpen(Outlet.OUTLET_TWO) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Can't open outlet three when outlet two is open!"));
                    return false;
                }
            }
            break;
        case OUTLET_FOUR:
            if (newState == true) {
                double chamberPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CHAMBER);
                double turboPumpTrigger = Double
                        .valueOf(settingsService.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP));
                if (chamberPressure < 0.8 * turboPumpTrigger) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Chamer pressure is lower than 80% of turbo pump trigger pressure. Won't open outlet four."));
                    return false;
                }
                if (isOutletOpen(Outlet.OUTLET_FIVE) == true || isOutletOpen(Outlet.OUTLET_SIX) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Opening outlet four when outlet five or six is open is forbidden for safety reasons!"));
                    return false;
                }
            }
            break;
        case OUTLET_FIVE:
            if (newState == true) {
                double cryoOnePressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE);
                double cryoTriggerPressure = Double
                        .valueOf(settingsService.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER));
                if (cryoOnePressure < cryoTriggerPressure) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Pressure at CryoPump One is low than trigger pressure. No reason to open Outlet Five!"));
                    return false;
                }
                if (isOutletOpen(Outlet.OUTLET_FOUR) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Opening outlet five when outlet four is open is forbidden for safety reasons!"));
                    return false;
                }
            }
            break;
        case OUTLET_SIX:
            if (newState == true) {
                double cryoTwoPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO);
                double cryoTriggerPressure = Double
                        .valueOf(settingsService.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER));
                if (cryoTwoPressure < cryoTriggerPressure) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Pressure at CryoPump Two is low than trigger pressure. No reason to open Outlet Six!"));
                    return false;
                }
                if (isOutletOpen(Outlet.OUTLET_FOUR) == true) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Opening outlet six when outlet four is open is forbidden for safety reasons!"));
                    return false;
                }
            }
            break;
        case OUTLET_SEVEN:
            if (newState == true) {
                if (isOutletOpen(Outlet.OUTLET_FIVE)) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Outlet five needs to be closed to open outlet seven!"));
                    return false;
                }
            }
            break;
        case OUTLET_EIGHT:
            if (newState == true) {
                if (isOutletOpen(Outlet.OUTLET_SIX)) {
                    logService.log(LogService.LOG_INFO, safetyProtocolMessage(outlet, newState,
                            "Outlet six needs to be closed to open outlet eight!"));
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
        message += reason + "\"";
        return message;
    }

}
