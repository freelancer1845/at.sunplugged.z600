package at.sunplugged.z600.launcher.lifecycle;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;

public class Manager {

    @PostContextCreate
    public void postContextCreate(IApplicationContext context) {
        System.out.println("Post Context Create");
    }

}
