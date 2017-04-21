package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;

public class WaitCommand extends AbstractCommand {

    private final int waitTime;

    public WaitCommand(int waitTimeInSeconds) {
        this.waitTime = waitTimeInSeconds;
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        Thread.sleep(waitTime * 1000);
    }

    @Override
    public String name() {
        return Commands.WAIT + "(" + waitTime + ")";
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert(waitTime, TimeUnit.SECONDS);
    }

}
