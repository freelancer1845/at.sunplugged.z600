package at.sunplugged.z600.backend.vaccum.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;

@Component
public class VacuumServiceImpl implements VacuumService {

    private static MachineStateService machineStateService;

    private static LogService logService;

    private State state = State.STOPPED;

    private Map<Interlocks, Boolean> interlocksMap = new HashMap<>();

    public VacuumServiceImpl() {
        initInterlocksMap();
    }

    @Override
    public State getState() {
        return state;

    }

    @Override
    public void setInterlock(Interlocks interlock, boolean value) {
        interlocksMap.put(interlock, value);
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    private void initInterlocksMap() {
        Interlocks[] locks = Interlocks.values();
        for (Interlocks lock : locks) {
            interlocksMap.put(lock, false);
        }
    }

    @Reference(unbind = "unbindMachineStateService")
    public synchronized void bindMachineStateService(MachineStateService service) {
        machineStateService = service;
    }

    public synchronized void unbindMachineStateService(MachineStateService service) {
        if (machineStateService.equals(service)) {
            machineStateService = null;
        }
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService service) {
        logService = service;
    }

    public synchronized void unbindLogService(LogService service) {
        if (logService.equals(service)) {
            logService = null;
        }
    }

    public static LogService getLogService() {
        return logService;
    }

}
