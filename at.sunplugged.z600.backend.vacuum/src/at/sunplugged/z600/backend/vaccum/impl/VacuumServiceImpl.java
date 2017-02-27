package at.sunplugged.z600.backend.vaccum.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;

@Component
public class VacuumServiceImpl implements VacuumService {

    private static MachineStateService machineStateService;

    private static LogService logService;

    private static SettingsService settingsService;

    private volatile State state = State.READY;

    private static Map<Interlocks, Boolean> interlocksMap = new HashMap<>();

    private Thread cryoPumpThread;

    private Thread turboPumpThread;

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
        if (state == State.READY) {
            // cryoPumpThread = new CryoPumpsThread();
            // cryoPumpThread.start();
            turboPumpThread = new TurboPumpThread();
            turboPumpThread.start();
            cryoPumpThread = new CryoPumpsThread();
            cryoPumpThread.start();
            state = State.RUNNING;
        } else {
            logService.log(LogService.LOG_WARNING,
                    "Tried to start vacuum thread but is not in acceptable state: \"" + state.name() + "\"");
        }
    }

    @Override
    public void stop() {
        if (state != State.FAILED) {
            // cryoPumpThread.interrupt();
            turboPumpThread.interrupt();
            cryoPumpThread.interrupt();
            state = State.READY;
        } else {
            logService.log(LogService.LOG_WARNING,
                    "Tried to stop bvacuum thread but is not in acceeptable state: \"" + state.name() + "\"");
        }
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

    @Reference(unbind = "unbindSettingsService")
    public synchronized void bindSettingsService(SettingsService service) {
        settingsService = service;
    }

    public synchronized void unbindSettingsService(SettingsService service) {
        if (settingsService == service) {
            settingsService = null;
        }
    }

    public static SettingsService getSettingsService() {
        return settingsService;
    }

    public static Map<Interlocks, Boolean> getInterlocksMap() {
        return interlocksMap;
    }

}
