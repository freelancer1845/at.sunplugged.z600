package at.sunplugged.z600.gui.factorys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSource.State;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.frontend.gui.utils.spi.UpdatableChart;
import at.sunplugged.z600.gui.dialogs.ValueDialog;
import at.sunplugged.z600.gui.views.MainView;

public class PowerSupplyBasicFactory {

    public static Group createPowerSupplyGroup(Composite parent, PowerSourceId id) {
        MachineStateService machineStateService = MainView.getMachineStateService();

        Group grpPinnacle = new Group(parent, SWT.NONE);
        grpPinnacle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpPinnacle.setText(id.name());
        grpPinnacle.setLayout(new GridLayout(1, false));

        Group grpData = new Group(grpPinnacle, SWT.NONE);
        grpData.setLayout(new GridLayout(7, true));
        grpData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpData.setText("Data");

        Label lblVoltage = new Label(grpData, SWT.NONE);
        lblVoltage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblVoltage.setText("Voltage:");

        Label lblv = new Label(grpData, SWT.NONE);
        lblv.setText("0.00V");
        lblv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblCurrent = new Label(grpData, SWT.NONE);
        lblCurrent.setText("Current:");
        lblv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lbla = new Label(grpData, SWT.NONE);
        lbla.setText("0.00A");
        lbla.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPower = new Label(grpData, SWT.NONE);
        lblPower.setText("Power:");
        lblPower.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblkw = new Label(grpData, SWT.NONE);
        lblkw.setText("0.00kW");
        lblkw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

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

        Combo targetIdcombo = new Combo(grpData, SWT.NONE);
        targetIdcombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        targetIdcombo.setText("Choose Target...");
        targetIdcombo.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                String[] targetMaterials = MainView.getDataService().getTargetMaterials();
                String[] completeList = new String[targetMaterials.length + 1];
                for (int i = 0; i < targetMaterials.length; i++) {
                    completeList[i] = targetMaterials[i];
                }
                completeList[targetMaterials.length] = "none";

                targetIdcombo.setItems(completeList);
            }
        });

        targetIdcombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String text = targetIdcombo.getText();
                if (text.equals("none")) {
                    MainView.getDataService().mapTargetToPowersource(id, null);
                } else {
                    MainView.getDataService().mapTargetToPowersource(id, text);
                }
            }
        });

        Composite pinnacleChartComposite = new Composite(grpData, SWT.NONE);
        GridData pinnacleChartCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true, 7, 1);
        pinnacleChartComposite.setLayoutData(pinnacleChartCompositeGd);
        pinnacleChartComposite.setLayout(new FillLayout());

        UpdatableChart pinnalceChart = new UpdatableChart(pinnacleChartComposite, "Power") {

            private double currentTenth = 0.0;

            @Override
            protected double addNewDataX() {
                currentTenth = currentTenth + 0.1;
                return currentTenth;
            }

            @Override
            protected double addNewDataY() {
                return MainView.getMachineStateService().getPowerSourceRegistry().getPowerSource(id).getPower();
            }

        };

        machineStateService.registerMachineEventHandler(new MachineEventHandler() {

            @Override
            public void handleEvent(MachineStateEvent event) {
                if (event.getType() == MachineStateEvent.Type.POWER_SOURCE_STATE_CHANGED) {
                    if (event.getOrigin() == id) {
                        PowerSource.State state = (State) event.getValue();
                        if (state != State.OFF) {
                            if (pinnalceChart.isUpdating() == false) {
                                pinnalceChart.resetChart();
                                pinnalceChart.startUpdating();
                            }
                        } else {
                            pinnalceChart.stopUpdating();
                        }
                    }
                }
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

        Label setPointLabel = new Label(grpControl, SWT.NONE);
        setPointLabel.setText("Setpoint Power [kW]: 0.00");
        setPointLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Display.getDefault().timerExec(1000, new Runnable() {

            @Override
            public void run() {
                setPointLabel.setText(String.format("Setpoint Power [kW]: %.2f",
                        machineStateService.getPowerSourceRegistry().getPowerSource(id).getSetPointpower()));
                Display.getDefault().timerExec(500, this);
            }

        });

        Button btnSetInKw = new Button(grpControl, SWT.NONE);
        btnSetInKw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSetInKw.setText("Set in kW");
        btnSetInKw.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ValueDialog valueDialog = new ValueDialog("Set Power [kW]", "Choose power in kW", 0.03,
                        MainView.getSettings().getPropertAsDouble(ParameterIds.MAX_POWER), parent.getShell());
                if (valueDialog.open() == ValueDialog.Answer.OK) {
                    machineStateService.getPowerSourceRegistry().getPowerSource(id).setPower(valueDialog.getValue());
                }
            }

        });
        return grpPinnacle;
    }

}
