package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import org.eclipse.swt.widgets.Composite;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.frontend.gui.spi.UpdatableChart;
import at.sunplugged.z600.frontend.gui.srm50.SrmGuiActivator;

public class SrmUpdatableChart extends UpdatableChart {

    DataService dataService = null;

    public SrmUpdatableChart(Composite parent, String title) {
        super(parent, title);

    }

    @Override
    protected void addNewData() {
        try {
            if (dataService == null) {
                dataService = SrmGuiActivator.getDataService();
            }
            dataList = dataService.getData("TestVariable", Double.class);
        } catch (DataServiceException e) {
            e.printStackTrace();
        }
    }

}
