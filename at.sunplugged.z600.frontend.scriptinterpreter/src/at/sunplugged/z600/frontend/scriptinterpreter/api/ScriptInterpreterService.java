package at.sunplugged.z600.frontend.scriptinterpreter.api;

import java.util.concurrent.Future;

public interface ScriptInterpreterService {

    public Future<?> executeScript(String script) throws ParseError;

    public void stopExecution();

    public String checkScript(String script) throws ParseError;

    public String getCurrentCommandName();

}
