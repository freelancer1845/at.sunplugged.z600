package at.sunplugged.z600.gui.speciallisteners;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.WaterControl.WaterOutlet;
import at.sunplugged.z600.gui.views.MainView;

public class EmergencyOffListener implements SelectionListener {

    private Shell parentShell;

    public EmergencyOffListener(Shell parentShell) {
        this.parentShell = parentShell;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {

        MessageBox warningMessage = new MessageBox(parentShell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
        warningMessage.setText("Emergency Off!");
        warningMessage.setMessage("Are you sure?");
        int answer = warningMessage.open();

        if (answer == SWT.OK) {
            emergencyOff();
        }

    }

    private void emergencyOff() {
        MainView.getVacuumService().stopEvacuationHard();
        closeAllOutlets();
        closeWaterOutlets();
        setAllAnalogOutputsToZero();
        setAllDigitalOutputsToFalse();
        stopMotors();
    }

    private void stopMotors() {
        MainView.getConveyorControlService().stop();
        MainView.getConveyorControlService().getEngineOne().stopEngineHard();
        MainView.getConveyorControlService().getEngineTwo().stopEngineHard();
    }

    private void setAllDigitalOutputsToFalse() {
        for (DigitalOutput digitalOutput : DigitalOutput.values()) {
            try {
                MainView.getMachineStateService().writeDigitalOutput(digitalOutput, false);
            } catch (IOException e) {
                handleError("Failed to write DigitalOutput to zero: " + digitalOutput.toString(), e);
            }
        }
    }

    private void setAllAnalogOutputsToZero() {
        for (AnalogOutput analogOutput : AnalogOutput.values()) {
            try {
                MainView.getMachineStateService().writeAnalogOutput(analogOutput, 0);
            } catch (IOException e) {
                handleError("Failed to set AnalogOutput to zero: " + analogOutput.toString(), e);
            }
        }

    }

    private void closeWaterOutlets() {
        for (WaterOutlet outlet : WaterOutlet.values()) {
            try {
                MainView.getMachineStateService().getWaterControl().setOutletState(outlet, false);
            } catch (IOException e) {
                handleError("Failed to close waterOutlet " + outlet.toString(), e);
            }
        }
    }

    private void closeAllOutlets() {
        for (Outlet outlet : Outlet.values()) {
            try {
                MainView.getMachineStateService().getOutletControl().closeOutlet(outlet);
            } catch (IOException e) {
                handleError("Failed to close outlet " + outlet.toString(), e);
            }
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {

    }

    private void handleError(String message, Throwable e) {
        MainView.getLogService().log(LogService.LOG_ERROR, "Error in emergency shutdown: " + message, e);
    }

}
