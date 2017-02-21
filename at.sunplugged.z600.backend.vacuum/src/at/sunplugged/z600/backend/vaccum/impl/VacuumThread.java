package at.sunplugged.z600.backend.vaccum.impl;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;

public class VacuumThread extends Thread {

    private enum VacuumStates {
        STARTING_PUMPS;
    }

    private VacuumStates vacuumState;

    private MachineStateService machineStateService;

    private LogService logService;

    public VacuumThread() {
        this.machineStateService = VacuumServiceImpl.getMachineStateService();
        this.logService = VacuumServiceImpl.getLogService();
    }

    @Override
    public void run() {
        switch (vacuumState) {

        }

    }

}
