package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;

public abstract class AbstractCommand implements Command {

    @Override
    public void execute() throws Exception {

        executeCommandSpecific();

    }

    protected abstract void executeCommandSpecific() throws InterruptedException, ScriptExecutionException;

}
