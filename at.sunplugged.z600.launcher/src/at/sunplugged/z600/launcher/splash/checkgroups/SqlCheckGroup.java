package at.sunplugged.z600.launcher.splash.checkgroups;

public class SqlCheckGroup extends AbstractCheckGroup {

    public SqlCheckGroup() {
        super("Waiting for sql connect...", "Connected to SQL Server!", "Failed to connect to SQL Server!");
    }

}
