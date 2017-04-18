package at.sunplugged.z600.conveyor.api;

public interface ConveyorPositionCorrectionService {

    public void start();

    public void stop();

    public boolean isRunning();

    public long getRuntimeLeft();

    public long getRuntimeRight();

    public void setRuntimeLeft(long ms);

    public void setRuntimeRight(long ms);

}
