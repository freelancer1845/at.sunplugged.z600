package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.KathodeControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;
import at.sunplugged.z600.core.machinestate.impl.kathode.KathodeInterface;
import at.sunplugged.z600.core.machinestate.impl.kathode.KathodeOne;
import at.sunplugged.z600.core.machinestate.impl.kathode.KathodeThree;
import at.sunplugged.z600.core.machinestate.impl.kathode.KathodeTwo;
import at.sunplugged.z600.mbt.api.MbtService;

/**
 * Implementing class of the {@linkplain KathodeControl} Interface.
 * 
 * @author Jascha Riedel
 *
 */
public class KathodeControlImpl implements KathodeControl, MachineEventHandler {

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private LogService logService;

    private SettingsService settings;

    private PowerControlThread powerControlThread;

    private KathodeInterface kathodeOne;

    private KathodeInterface kathodeTwo;

    private KathodeInterface kathodeThree;

    public KathodeControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.logService = MachineStateServiceImpl.getLogService();
        this.settings = MachineStateServiceImpl.getSettingsService();

        kathodeOne = new KathodeOne(logService, machineStateService, settings, mbtService);
        kathodeTwo = new KathodeTwo(logService, machineStateService, settings, mbtService);
        kathodeThree = new KathodeThree(logService, machineStateService, settings, mbtService);

    }

    @Override
    public void setPowerSetpoint(Kathode kathode, double power) {
        switch (kathode) {
        case KATHODE_ONE:
            kathodeOne.setPowerSetpoint(power);
            break;
        case KATHODE_TWO:
            kathodeTwo.setPowerSetpoint(power);
            break;
        case KATHODE_THREE:
            kathodeThree.setPowerSetpoint(power);
            break;
        default:
            logService.log(LogService.LOG_DEBUG, "No Setpoint Variable for kathode: " + kathode.name());
        }
    }

    @Override
    public double getPowerSetpoint(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            return kathodeOne.getPowerSetpoint();
        case KATHODE_TWO:
            return kathodeTwo.getPowerSetpoint();
        case KATHODE_THREE:
            return kathodeThree.getPowerSetpoint();
        default:
            logService.log(LogService.LOG_DEBUG, "No Setpoint Variable for kathode: " + kathode.name());
            return -1;
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.ANALOG_INPUT_CHANGED)) {
            safetyChecks(event);
        }
    }

    @Override
    public void startKathode(Kathode kathode) throws InvalidKathodeStateException {
        if (powerControlThread.isRunning() == false) {
            logService.log(LogService.LOG_DEBUG, "Starting Power Control Thread");
            startPowerControlThread();
        }
        switch (kathode) {
        case KATHODE_ONE:
            kathodeOne.startKathode();
            break;
        case KATHODE_TWO:
            kathodeTwo.startKathode();
            break;
        case KATHODE_THREE:
            kathodeThree.startKathode();
            break;
        }
    }

    @Override
    public void stopKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            kathodeOne.stopKathode();
            break;
        case KATHODE_TWO:
            kathodeTwo.stopKathode();
            break;
        case KATHODE_THREE:
            kathodeThree.stopKathode();
            break;
        }

    }

    @Override
    public double getVoltageAtKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            return kathodeOne.getVoltageAtKathode();
        case KATHODE_TWO:
            return kathodeTwo.getVoltageAtKathode();
        case KATHODE_THREE:
            return kathodeThree.getVoltageAtKathode();
        default:
            logService.log(LogService.LOG_ERROR, "Unkown kathode: " + kathode.name());
            return 0;
        }
    }

    @Override
    public double getCurrentAtKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            return kathodeOne.getCurrentAtKathode();
        case KATHODE_TWO:
            return kathodeTwo.getCurrentAtKathode();
        case KATHODE_THREE:
            return kathodeThree.getCurrentAtKathode();
        default:
            logService.log(LogService.LOG_ERROR, "Unkown kathode: " + kathode.name());
            return 0;
        }
    }

    @Override
    public double getPowerAtKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            return kathodeOne.getPowerAtKathode();
        case KATHODE_TWO:
            return kathodeTwo.getPowerAtKathode();
        case KATHODE_THREE:
            return kathodeThree.getPowerAtKathode();
        default:
            logService.log(LogService.LOG_ERROR, "Unkown kathode: " + kathode.name());
            return 0;
        }
    }

    private void safetyChecks(MachineStateEvent event) {

        // TODO :
        // switch (event.getAnalogInput()) {
        // case VOLTAGE_KATHODE_ONE:
        // case CURRENT_KATHODE_ONE:
        // double currentPower = getPowerAtKathode(Kathode.KATHODE_ONE);
        //
        // }
    }

    private void startPowerControlThread() {
        if (this.powerControlThread == null) {
            this.powerControlThread = new PowerControlThread();
            powerControlThread.start();
        } else if (this.powerControlThread.isRunning() == false) {
            this.powerControlThread = new PowerControlThread();
            powerControlThread.start();
        }
    }

    private void stopPowerControlThread() {
        if (this.powerControlThread != null) {
            this.powerControlThread.stopSoft();
            this.powerControlThread = null;
        }
    }

    private class PowerControlThread extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            this.setName("Kathode-Power-Control-Thread");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            running = true;
            while (running) {
                try {
                    if (kathodeOne.getPowerSetpoint() != 0 || kathodeOne.getPowerAtKathode() > 1e-3) {
                        kathodeOne.tickPowerControl();
                    }
                    if (kathodeTwo.getPowerSetpoint() != 0 || kathodeTwo.getPowerAtKathode() > 1e-3) {
                        kathodeTwo.tickPowerControl();
                    }
                    if (kathodeThree.getPowerSetpoint() != 0 || kathodeThree.getPowerAtKathode() > 1e-3) {
                        kathodeThree.tickPowerControl();
                    }
                } catch (IOException e) {
                    running = false;
                    logService.log(LogService.LOG_ERROR, "Kathode Control Thread failed!", e);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "Kathode Control Thread wait was interrupted");
                    e.printStackTrace();
                }
            }

        }

        public void stopSoft() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

    }

}
