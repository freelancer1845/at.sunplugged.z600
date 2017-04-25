package at.sunplugged.z600.backend.vaccum.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService;
import at.sunplugged.z600.backend.vaccum.impl.starting.CryoPumpsStartThread;
import at.sunplugged.z600.backend.vaccum.impl.starting.TurboPumpStartThread;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;

@Component
public class VacuumServiceImpl implements VacuumService {

    private static MachineStateService machineStateService;

    private static LogService logService;

    private static SettingsService settingsService;

    private static volatile State state = State.READY;

    private static CryoPumpsThreadState cryoState = CryoPumpsThreadState.INIT_STATE;

    private static TurboPumpThreadState turboState = TurboPumpThreadState.INIT_STATE;

    private static Map<Interlocks, Boolean> interlocksMap = new HashMap<>();

    private CryoPumpsStartThread cryoPumpThread;

    private TurboPumpStartThread turboPumpThread;

    public VacuumServiceImpl() {
        initInterlocksMap();
    }

    @Override
    public State getState() {
        return state;

    }

    public static void transmitState(CryoPumpsThreadState state) {
        cryoState = state;
        updateState();
    }

    public static void transmitState(TurboPumpThreadState state) {
        turboState = state;
        updateState();
    }

    private static void updateState() {
        if (cryoState == CryoPumpsThreadState.CRYO_RUNNING && turboState == TurboPumpThreadState.TURBO_PUMP_RUNNING) {
            state = State.EVACUATING;
            if (machineStateService.getGasFlowControl().getState() != GasFlowControl.State.STOP
                    && machineStateService.getGasFlowControl().getState() != GasFlowControl.State.STOPPING) {
                state = State.PRESSURE_CONTROL_RUNNING;
            }
            return;
        }
        if (cryoState == CryoPumpsThreadState.CANCELED || turboState == TurboPumpThreadState.CANCELED) {
            state = State.FAILED;
            return;
        }
        if (cryoState == CryoPumpsThreadState.INIT_STATE && turboState == TurboPumpThreadState.INIT_STATE) {
            state = State.READY;
            return;
        }
        if (cryoState == CryoPumpsThreadState.SHUTDOWN || turboState == TurboPumpThreadState.SHUTDOWN) {
            state = State.SHUTTING_DOWN;
            return;
        }
        state = State.STARTING;
    }

    @Override
    public void setInterlock(Interlocks interlock, boolean value) {
        interlocksMap.put(interlock, value);
    }

    @Override
    public void startEvacuation() {
        if (state == State.READY || state == State.FAILED) {
            turboPumpThread = new TurboPumpStartThread();
            turboPumpThread.start();
            cryoPumpThread = new CryoPumpsStartThread();
            cryoPumpThread.start();
            state = State.STARTING;
        } else if (state == State.SHUTTING_DOWN) {
            if (cryoState == CryoPumpsThreadState.INIT_STATE && cryoPumpThread.isAlive()) {
                cryoPumpThread = new CryoPumpsStartThread();
                cryoPumpThread.start();
            } else {
                cryoPumpThread.restart();
            }
            if (turboState == TurboPumpThreadState.INIT_STATE) {
                turboPumpThread = new TurboPumpStartThread();
                turboPumpThread.start();
            } else {
                turboPumpThread.restart();
            }

            logService.log(LogService.LOG_DEBUG, "Restarting evacuation.");
        } else {
            logService.log(LogService.LOG_WARNING,
                    "Tried to start vacuum thread but is not in acceptable state: \"" + state.name() + "\"");
        }
    }

    @Override
    public void shutdown() {
        state = State.SHUTTING_DOWN;
        stopPressureControl();
        cryoPumpThread.shutdown();
        turboPumpThread.shutdown();

    }

    @Override
    public void stopEvacuationHard() {
        if (state != State.FAILED) {
            turboPumpThread.interrupt();
            cryoPumpThread.interrupt();

            state = State.READY;
        } else {
            logService.log(LogService.LOG_WARNING,
                    "Tried to stop vacuum thread but is not in acceeptable state: \"" + state.name() + "\"");
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

    @Override
    public void startPressureControl() {
        if (VacuumUtils.isPressureControlLimitReached() == true) {
            machineStateService.getGasFlowControl().startGasFlowControl();
        } else {
            logService.log(LogService.LOG_WARNING, "PressureControl Limit not reached!");
        }
    }

    @Override
    public void startPressureControl(double setPointPressure) {
        if (VacuumUtils.isPressureControlLimitReached() == true) {
            machineStateService.getGasFlowControl().startGasFlowControl();
            machineStateService.getGasFlowControl().setGasflowDesiredPressure(setPointPressure);
        } else {
            logService.log(LogService.LOG_WARNING, "PressureControl Limit not reached!");
        }
    }

    @Override
    public void setSetpointPressure(double setPoint) {
        machineStateService.getGasFlowControl().setGasflowDesiredPressure(setPoint);
    }

    @Override
    public void stopPressureControl() {
        machineStateService.getGasFlowControl().stopGasFlowControl();
    }

    @Override
    public CryoPumpsThreadState getCryoPumpThreadState() {
        return cryoState;
    }

    @Override
    public TurboPumpThreadState getTurboPumpThreadState() {
        return turboState;
    }

}
