package at.sunplugged.z600.main.impl;

import org.osgi.framework.ServiceReference;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.main.MainActivator;
import at.sunplugged.z600.main.api.ControllerInterface;
import at.sunplugged.z600.main.controlling.MainController;

public class ControllerInterfaceImpl implements ControllerInterface {

    /** This will be the thread which does the work of main controlling. */
    private Thread mainControllerThread;

    /** The DataService Service. */
    private DataService dataService;

    public ControllerInterfaceImpl() {
        getServices();

        mainControllerThread = new Thread(new MainController());
        mainControllerThread.setName("Main Controller Thread");

    }

    @Override
    public void startSQLLogging(String hostname, String username, String password) throws DataServiceException {
        // Empty For Now
    }

    @Override
    public void startSrmSqlLogging(String hostname, String username, String password) throws DataServiceException {
        dataService.startAddingSrmDataToTable();
    }

    private void getServices() {
        ServiceReference<DataService> dataServiceReference = MainActivator.getContext()
                .getServiceReference(DataService.class);
        dataService = MainActivator.getContext().getService(dataServiceReference);

    }

    @Override
    public void startMainControl() {
        mainControllerThread.start();
    }

}
