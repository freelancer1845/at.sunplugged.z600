package at.sunplugged.z600.launcher.splash;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;

/**
 * This selection Listener tries to get references for all services and starts
 * them correctly.
 * 
 * @author Jascha Riedel
 *
 */
@Component
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
        shell.dispose();
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

    @Reference(unbind = "unbindMachineStateService", cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        ContinueButtonSelectionListener.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (ContinueButtonSelectionListener.machineStateService == machineStateService) {
            ContinueButtonSelectionListener.machineStateService = null;
        }
    }

}
