package at.sunplugged.z600.gui.factorys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.gui.views.MainView;

public class PowerSupplyBasicFactory {

    public static Group createPowerSupplyGroup(Composite parent, PowerSourceId id) {
        MachineStateService machineStateService = MainView.getMachineStateService();

        Group grpPinnacle = new Group(parent, SWT.NONE);
        grpPinnacle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpPinnacle.setText(id.name());
        grpPinnacle.setLayout(new GridLayout(1, false));

        Group grpData = new Group(grpPinnacle, SWT.NONE);
        grpData.setLayout(new GridLayout(2, true));
        grpData.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpData.setText("Data");

        Label lblVoltage = new Label(grpData, SWT.NONE);
        lblVoltage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblVoltage.setText("Voltage:");

        Label lblv = new Label(grpData, SWT.NONE);
        lblv.setText("0.00V");

        Label lblCurrent = new Label(grpData, SWT.NONE);
        lblCurrent.setText("Current:");

        Label lbla = new Label(grpData, SWT.NONE);
        lbla.setText("0.00A");

        Label lblPower = new Label(grpData, SWT.NONE);
        lblPower.setText("Power:");

        Label lblkw = new Label(grpData, SWT.NONE);
        lblkw.setText("0.00kW");

        Display.getDefault().timerExec(500, new Runnable() {

            @Override
            public void run() {
                lblv.setText(
                        Double.toString(machineStateService.getPowerSourceRegistry().getPowerSource(id).getVoltage()));
                lbla.setText(
                        Double.toString(machineStateService.getPowerSourceRegistry().getPowerSource(id).getCurrent()));
                lblkw.setText(
                        Double.toString(machineStateService.getPowerSourceRegistry().getPowerSource(id).getPower()));
                Display.getDefault().timerExec(500, this);
            }
        });

        Group grpControl = new Group(grpPinnacle, SWT.NONE);
        grpControl.setLayout(new GridLayout(2, true));
        grpControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpControl.setText("Control");

        Button btnStart = new Button(grpControl, SWT.NONE);
        btnStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnStart.setText("START");
        btnStart.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                machineStateService.getPowerSourceRegistry().getPowerSource(id).on();
            }

        });

        Button btnStop = new Button(grpControl, SWT.NONE);
        btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStop.setText("STOP");
        btnStop.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                machineStateService.getPowerSourceRegistry().getPowerSource(id).off();
            }

        });

        Text text_1 = new Text(grpControl, SWT.BORDER);
        text_1.setText("0.00");
        text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnSetInKw = new Button(grpControl, SWT.NONE);
        btnSetInKw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSetInKw.setText("Set in kW");
        btnSetInKw.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                machineStateService.getPowerSourceRegistry().getPowerSource(id)
                        .setPower(Double.valueOf(text_1.getText()));
            }

        });
        return grpPinnacle;
    }

}
