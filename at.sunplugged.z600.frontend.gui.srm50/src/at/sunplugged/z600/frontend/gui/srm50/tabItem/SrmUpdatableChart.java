package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import java.util.Random;

import org.eclipse.swt.widgets.Composite;

import at.sunplugged.z600.frontend.gui.spi.UpdatableChart;

public class SrmUpdatableChart extends UpdatableChart {

    public SrmUpdatableChart(Composite parent, String title) {
        super(parent, title);

    }

    @Override
    protected void addNewData() {
        dataList.add(new Random().nextDouble() * 9999);
    }

}
