package at.sunplugged.z600.frontend.gui.utils.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.frontend.gui.utils.api.SettingsService;

@Component(immediate = true)
public class SettingsServiceImpl implements SettingsService {

    private LogService logService;

    @Activate
    protected void activate() {
    }

    @Deactivate
    protected void deactivate() {

    }

    @Override
    public String getValue(String setting) {
        // TODO Auto-generated method stub
        return null;
    }

    @Reference(unbind = "unbindLogService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (this.logService.equals(logService)) {
            this.logService = null;
        }
    }

}
