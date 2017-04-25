package at.sunplugged.z600.conveyor.api;

public interface Engine {

    public enum Direction {
        CLOCKWISE, COUNTER_CLOCKWISE;
    }

    public void connect();

    public boolean isConnected();

    public void setEngineMode(int mode);

    public void setDirection(int direction);

    /**
     * Clockwise is 1, Counter_Clockwise 0 looking at the engine from the front.
     * 
     * @param direction
     */
    public void setDirection(Direction direction);

    public void setMaximumSpeed(int speed) throws EngineException;

    public int getCurrentMaximumSpeed();

    public boolean isRunning();

    public Direction getDirection();

    public void startEngine();

    public void setLoose();

    public void stopEngine();

    public void stopEngineHard();

    public void initializeEngine();

}
