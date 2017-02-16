package at.sunplugged.z600.conveyor.api;

public interface Engine {

    public void connect();

    public boolean isConnected();

    public void setEngineMode(int mode);

    public void setDirection(int direction);

    public void setMaximumSpeed(int speed);

    public int getCurrentMaximumSpeed();

    public void startEngine();

    public void setLoose();

    public void stopEngine();

    public void stopEngineHard();

}
