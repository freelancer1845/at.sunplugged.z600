package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.backend.dataservice.api.VariableIdentifiers;
import at.sunplugged.z600.frontend.gui.spi.UpdatableChart;
import at.sunplugged.z600.frontend.gui.srm50.SrmGuiActivator;

public class SrmUpdatableChart extends UpdatableChart {

    private DataService dataService = null;

    private final int channel;

    public SrmUpdatableChart(Composite parent, String title, int channel) {
        super(parent, title);
        this.channel = channel;

    }

    @Override
    protected void addNewData() {
        try {
            if (dataService == null) {
                dataService = SrmGuiActivator.getDataService();
            }
            dataList = dataService.getData(VariableIdentifiers.SRM_CHANNEL + channel, Double.class);
        } catch (DataServiceException e) {
            SrmGuiActivator.getLogService().log(LogService.LOG_WARNING,
                    "Updating chart " + this.title + " stopped: " + e.getMessage());
            this.stopUpdating();
        }
    }

}
