package at.sunplugged.z600.srm50.impl;

import java.util.List;
import java.util.concurrent.Future;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

/**
 * Class implementing {@link SrmCommunicator}.
 * 
 * @author Jascha Riedel
 *
 */
@Component(immediate = true)
public class SrmCommunicatorImpl implements SrmCommunicator {

    @Reference
    private StandardThreadPoolService threadPool;

    @Reference
    private SettingsService settings;

    @Reference
    private EventAdmin eventAdmin;

    @Reference
    private LogService logService;

    private SrmDataAquisitionRunnable srmDataAquisitionRunnable;

    @Activate
    protected void activate() {
        srmDataAquisitionRunnable = new SrmDataAquisitionRunnable(logService, settings, eventAdmin);
        threadPool.execute(srmDataAquisitionRunnable);
    }

    @Deactivate
    protected void deactivate() {
        srmDataAquisitionRunnable.stop();
    }

    @Override
    public List<Double> getData() {
        return srmDataAquisitionRunnable.getData();
    }

    @Override
    public Future<String> issueCommandAsyn(String string) {
        return srmDataAquisitionRunnable.queueCommand(string);
    }

}
