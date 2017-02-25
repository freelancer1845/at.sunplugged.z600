package at.sunplugged.z600.launcher.splash.checkgroups;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;

public interface CheckGroup {

    public void create(Composite parent);

    public void setEvent(Event event);

    public void update();

}
