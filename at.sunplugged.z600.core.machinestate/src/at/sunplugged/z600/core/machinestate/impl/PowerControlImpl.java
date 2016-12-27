package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerControl;
import at.sunplugged.z600.mbt.api.MbtService;

/**
 * Implementing class of the {@linkplain PowerControl} interface.
 * 
 * @author Jascha Riedel
 *
 */
public class PowerControlImpl implements PowerControl {

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private LogService logService;

    public PowerControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.logService = MachineStateServiceImpl.getLogService();
    }

    @Override
    public void start(PowerUnit powerUnit) {
        if (powerUnit.getInterlockOutput() != null) {
            if (machineStateService.getDigitalOutputState(powerUnit.getInterlockOutput())) {
                logService.log(LogService.LOG_WARNING,
                        "Tried to start a powerUnit without its interlock set true! : " + powerUnit.name());
                return;
            }
        }
        try {
            mbtService.writeDigOut(powerUnit.getStartOutput().getAddress(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void stop(PowerUnit powerUnit) {
        try {
            mbtService.writeDigOut(powerUnit.getStartOutput().getAddress(), false);
            if (powerUnit.getOffOutput() != null) {
                mbtService.writeDigOut(powerUnit.getOffOutput().getAddress(), false);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void setInterlock(PowerUnit powerUnit, boolean interlock) {
        if (powerUnit.getInterlockOutput() != null) {
            try {
                mbtService.writeDigOut(powerUnit.getInterlockOutput().getAddress(), interlock);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            logService.log(LogService.LOG_WARNING,
                    "Tried to set interlock of a power unit without an interlock! : " + powerUnit.name());
        }
    }

    @Override
    public boolean getState(PowerUnit powerUnit) {

        // TODO Fill cases!!
        switch (powerUnit) {
        case SSV_ONE:
            break;
        case SSV_TWO:
            break;
        case PINNACLE:
            break;
        }
        return false;
    }

}
