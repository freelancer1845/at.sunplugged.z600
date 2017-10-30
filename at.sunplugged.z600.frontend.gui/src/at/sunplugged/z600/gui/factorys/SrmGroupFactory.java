package at.sunplugged.z600.gui.factorys;

import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import at.sunplugged.z600.frontend.gui.utils.spi.UpdatableChart;
import at.sunplugged.z600.gui.views.MainView;
import at.sunplugged.z600.srm50.api.Commands;

public class SrmGroupFactory {

    /**
     * @wbp.factory
     */
    public static Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        group.setText("SRM");
        GridLayout layout = new GridLayout(2, true);
        group.setLayout(layout);

        int channels = 3;
        UpdatableChart[] charts = new UpdatableChart[channels];
        for (int i = 0; i < channels; i++) {
            Composite srmChannelComposite = new Composite(group, SWT.NONE);
            GridData srmChannelCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
            srmChannelComposite.setLayoutData(srmChannelCompositeGd);
            srmChannelComposite.setLayout(new GridLayout(2, false));
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
                    List<Double> data = MainView.getSrmCommunicator().getData();
                    if (data != null) {
                        return data.get(channelIndex);
                    } else {
                        return -1;
                    }

                }

            };
            srmChart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

            Button zeroButton = new Button(srmChannelComposite, SWT.PUSH);
            zeroButton.setText("ZERO");
            zeroButton.addSelectionListener(new ZeroButtonSelectionAdapter(i + 1));
            GridData gridData = new GridData(SWT.FILL, SWT.BOTTOM, false, true);
            gridData.widthHint = 150;
            zeroButton.setLayoutData(gridData);

            Button calibrateButton = new Button(srmChannelComposite, SWT.PUSH);
            calibrateButton.setText("Calibrate");
            calibrateButton
                    .addSelectionListener(new CalibrateButtonSelectionAdapter(i + 1, srmChannelComposite.getShell()));
            calibrateButton.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
            charts[i] = srmChart;

        }

        Button updateButton = new Button(group, SWT.PUSH);
        updateButton.setText("Toggel Chart Updating");
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

    private final static class ZeroButtonSelectionAdapter extends SelectionAdapter {

        private final int channel;

        public ZeroButtonSelectionAdapter(int channel) {
            this.channel = channel;

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            MainView.getSrmCommunicator().issueCommandAsyn(Commands.ZERO_X + channel);
        }

    }

    private final static class CalibrateButtonSelectionAdapter extends SelectionAdapter {
        private final int channel;
        private Shell parentShell;

        public CalibrateButtonSelectionAdapter(int channel, Shell parentShell) {
            this.channel = channel;
            this.parentShell = parentShell;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            InputDialog dialog = new InputDialog(parentShell, "Calibrate...",
                    "Set calibration value for channel \"" + channel + "\"", "0.0", new IInputValidator() {

                        @Override
                        public String isValid(String newText) {
                            try {
                                Double.valueOf(newText);
                            } catch (NumberFormatException e) {
                                return "Must comply with java double format...";
                            }
                            return null;
                        }
                    });
            if (dialog.open() == Window.OK) {
                double value = Double.valueOf(dialog.getValue());
                MainView.getSrmCommunicator().issueCommandAsyn(Commands.UCAL_X_Y + channel + "," + value);
            }

        }
    }

    private SrmGroupFactory() {

    }
}
