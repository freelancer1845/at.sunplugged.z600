package at.sunplugged.z600.conveyor.api;

public interface Engine {

    public void connect();

    public void setEngineMode(int mode);

    public void setDirection(int direction);

    public void increaseSpeed();

    public void decreaseSpeed();

    public void setMaximumSpeed(int speed);

    public void startEngine();

    public void stopEngine();

    public void stopEngineHard();

}
