package at.sunplugged.z600.gui.factorys;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.frontend.gui.utils.spi.UpdatableChart;
import at.sunplugged.z600.gui.views.MainView;

public class SrmGroupFactory {

    /**
     * @wbp.factory
     */
    public static Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        group.setText("SRM Debug");
        GridLayout layout = new GridLayout(2, true);
        group.setLayout(layout);

        Combo portCombo = new Combo(group, SWT.NONE);
        portCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        portCombo.setItems(MainView.getSrmCommunicator().getPortNames());

        Button connectButton = new Button(group, SWT.PUSH);
        connectButton.setText("Connect/Reconnect");
        connectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        connectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    MainView.getSrmCommunicator().disconnect();
                    MainView.getSrmCommunicator().connect(portCombo.getItem(portCombo.getSelectionIndex()));

                } catch (IOException e1) {
                    MainView.getLogService().log(LogService.LOG_ERROR, "Srm Connect failed.", e1);
                }

            }
        });

        int channels = 3;
        UpdatableChart[] charts = new UpdatableChart[channels];
        for (int i = 0; i < channels; i++) {
            Composite srmChannelComposite = new Composite(group, SWT.NONE);
            GridData srmChannelCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
            srmChannelComposite.setLayoutData(srmChannelCompositeGd);
            srmChannelComposite.setLayout(new FillLayout());
            final int channelIndex = i;

            UpdatableChart srmChart = new UpdatableChart(srmChannelComposite, "Channel: " + (i + 1)) {

                private double currentTenth = 0.0;

                @Override
                protected double addNewDataX() {
                    currentTenth = currentTenth + 0.1;
                    return currentTenth;
                }

                @Override
                protected double addNewDataY() {
                    try {
                        return MainView.getSrmCommunicator().readChannels().get(channelIndex);
                    } catch (IOException e) {
                        MainView.getLogService().log(LogService.LOG_ERROR, "Failed to read data from srm.");
                        return 0.0;
                    }
                }

            };
            charts[i] = srmChart;

        }

        Button updateButton = new Button(group, SWT.PUSH);
        updateButton.setText("Toggel Updating");
        updateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (int i = 0; i < charts.length; i++) {
                    if (charts[i] != null) {
                        if (charts[i].isUpdating() == false) {
                            charts[i].startUpdating();
                        } else {
                            charts[i].stopUpdating();
                        }
                    }
                }
            }
        });

        return group;
    }

    private SrmGroupFactory() {

    }
}
