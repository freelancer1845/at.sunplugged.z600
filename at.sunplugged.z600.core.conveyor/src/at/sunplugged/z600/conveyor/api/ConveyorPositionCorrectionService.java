package at.sunplugged.z600.conveyor.api;

public interface ConveyorPositionCorrectionService {

    public void start();

    public void stop();

    public void setExplicitLeft(boolean value);

    public void setExplicitRight(boolean value);

    public boolean isRunning();

    public long getRuntimeLeft();

    public long getRuntimeRight();

    public void setRuntimeLeft(long ms);

    public void setRuntimeRight(long ms);

    public void centerLeft();

    public void centerRight();

    public void startLeftForward();

    public void startLeftBackward();

    public void startRightForward();

    public void startRightBackward();

    public void stopManualMove();

}
