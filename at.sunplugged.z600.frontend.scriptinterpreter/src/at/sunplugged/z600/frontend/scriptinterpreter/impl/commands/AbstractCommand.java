package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public abstract class AbstractCommand implements Command {

    private State state = State.PRE_START;

    protected void setState(State state) {
        this.state = state;
        ScriptInterpreterServiceImpl.getLogService().log(LogService.LOG_DEBUG,
                String.format("New State for command \"%s\" --- \"%s\"", toString(), state));
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void execute() throws Exception {
        setState(State.EXECUTING);
        executeCommandSpecific();
        setState(State.FINISHED);

    }

    protected abstract void executeCommandSpecific() throws InterruptedException, ScriptExecutionException;

    @Override
    public String toString() {
        return name();
    }

}
