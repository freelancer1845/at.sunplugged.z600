package at.sunplugged.z600.launcher;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Main implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {
        System.out.println("Application Started");
        return EXIT_OK;
    }

    @Override
    public void stop() {
        System.out.println("Application stopped");
    }

}
