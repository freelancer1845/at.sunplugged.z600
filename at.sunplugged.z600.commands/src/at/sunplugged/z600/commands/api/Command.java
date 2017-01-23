package at.sunplugged.z600.commands.api;

public interface Command {

    public void execute();

    public void cancle();

    public boolean isFinished();

}
