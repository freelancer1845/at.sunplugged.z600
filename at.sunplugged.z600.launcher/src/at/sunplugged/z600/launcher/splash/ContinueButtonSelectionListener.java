package at.sunplugged.z600.launcher.splash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;

/**
 * This selection Listener tries to get references for all services and starts
 * them correctly.
 * 
 * @author Jascha Riedel
 *
 */
@Component(immediate = true)
public class ContinueButtonSelectionListener implements SelectionListener {

    private static MachineStateService machineStateService;

    private Shell shell;

    public ContinueButtonSelectionListener() {
        // Empty constructor for DS.
    }

    public ContinueButtonSelectionListener(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        startServices();
        checkForCriticalErrors();
        shell.dispose();
    }

    private void checkForCriticalErrors() {
        if (machineStateService.getDigitalInputState(DigitalInput.PRESSURE_FOR_OUTLETS) == false) {
            MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
            messageBox.setText("Pressure for Outlets");
            messageBox.setMessage("The Pressure for the outlets may be off. Please Check and continue with caution!");
            messageBox.open();
        }
        if (machineStateService.getDigitalInputState(DigitalInput.COOLING_PUMP_OK) == false) {
            MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
            messageBox.setMessage("Cooling Pump Error");
            messageBox.setMessage("Cooling may not be running!");
            messageBox.open();
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    private void startServices() {
        if (machineStateService != null) {
            machineStateService.start();
        }
    }

    @Reference(unbind = "unbindMachineStateService", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        ContinueButtonSelectionListener.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (ContinueButtonSelectionListener.machineStateService == machineStateService) {
            ContinueButtonSelectionListener.machineStateService = null;
        }
    }

}
