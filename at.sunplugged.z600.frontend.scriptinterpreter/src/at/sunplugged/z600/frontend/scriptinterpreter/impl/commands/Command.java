package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

public interface Command {

    public void execute() throws Exception;

    public String name();

}
