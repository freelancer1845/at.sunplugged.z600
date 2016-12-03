package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.backend.dataservice.api.VariableIdentifiers;
import at.sunplugged.z600.frontend.gui.utils.spi.UpdatableChart;

public class SrmUpdatableChart extends UpdatableChart {

    private DataService dataService = null;

    private LogService logService = null;

    private final int channel;

    public SrmUpdatableChart(SrmTabItemFactory srmTabItemFactory, Composite parent, String title, int channel) {
        super(parent, title);
        this.channel = channel;
        this.logService = srmTabItemFactory.getLogService();
        this.dataService = srmTabItemFactory.getDataService();
    }

    @Override
    protected void addNewData() {
        try {

            dataList = dataService.getData(VariableIdentifiers.SRM_CHANNEL + channel, Double.class);
        } catch (DataServiceException e) {
            logService.log(LogService.LOG_WARNING, "Updating chart " + this.title + " stopped: " + e.getMessage());
            this.stopUpdating();
        }
    }

}
