package at.sunplugged.z600.conveyor.api;

public interface ConveyorPositionCorrectionService {

    public void start();

    public void stop();

    public double getRuntimeLeft();

    public double getRuntimeRight();

    public void setRuntimeLeft(long ms);

    public void setRuntimeRight(long ms);

}
