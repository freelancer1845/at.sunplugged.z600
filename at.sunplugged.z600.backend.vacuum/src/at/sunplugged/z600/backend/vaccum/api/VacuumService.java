package at.sunplugged.z600.backend.vaccum.api;

public interface VacuumService {

    public enum State {
        STARTING, READY, FAILED, RUNNING;
    }

    public enum Interlocks {
        CRYO_ONE, CRYO_TWO, TURBO_PUMP;
    }

    public State getState();

    public void setInterlock(Interlocks interlock, boolean state);

    public void start();

    public void stop();

}
