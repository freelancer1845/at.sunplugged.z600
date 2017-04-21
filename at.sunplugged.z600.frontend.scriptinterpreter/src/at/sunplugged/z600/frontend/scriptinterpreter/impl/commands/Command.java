package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

public interface Command {

    public enum State {
        PRE_START, EXECUTING, FINISHED, FAILED;
    }

    public void execute() throws Exception;

    public long getEstimededTimeNeeded(TimeUnit unit);

    public String name();

    public State getState();

}
