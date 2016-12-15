package at.sunplugged.z600.conveyor.speedlogging;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.mbt.api.MbtService;

@Component
public class SpeedLoggerService implements SpeedLogger {

    @Override
    public double getCurrentSpeed() {
        return 0;
    }

    @Reference(unbind = "unbindMbtControllerService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindMbtControllerService(MbtService mbtController) {

    }

    public synchronized void unbindMbtControllerService(MbtService mbtController) {

    }
}
