package at.sunplugged.z600.backend.vaccum.api;

public interface VacuumService {

    public enum State {
        STARTING, READY, FAILED, EVACUATING, PRESSURE_CONTROL_RUNNING, SHUTTING_DOWN;
    }

    public enum CryoPumpsThreadState {
        INIT_STATE,
        START_PRE_PUMP,
        EVACUATE_CRYO,
        EVACUATE_CHAMBER,
        START_COOLING,
        WAIT_FOR_CRYO_COOL,
        CRYO_RUNNING,
        GAS_FLOW_RUNNING,
        SHUTDOWN,
        CANCELED;
    }

    public enum TurboPumpThreadState {
        INIT_STATE,
        START_PRE_PUMPS,
        EVACUATE_CHAMBER,
        EVACUATE_TURBO_PUMP,
        START_TURBO_PUMP,
        TURBO_PUMP_RUNNING,
        SHUTDOWN,
        CANCELED;
    }

    public enum Interlocks {
        CRYO_ONE, CRYO_TWO, TURBO_PUMP;
    }

    public State getState();

    public CryoPumpsThreadState getCryoPumpThreadState();

    public TurboPumpThreadState getTurboPumpThreadState();

    public void setInterlock(Interlocks interlock, boolean state);

    public void startEvacuation();

    /**
     * Stops the evacuation hard. One should use shutdown()!
     */
    public void stopEvacuationHard();

    public void startPressureControl();

    public void startPressureControl(double setPointPressure);

    public void setSetpointPressure(double setPoint);

    /**
     * Stops the pressure control.
     */
    public void stopPressureControl();

    /**
     * This gradually shuts down the vacuum control. This is the preferred
     * method for ending the vacuum control!
     */
    public void shutdown();

}
