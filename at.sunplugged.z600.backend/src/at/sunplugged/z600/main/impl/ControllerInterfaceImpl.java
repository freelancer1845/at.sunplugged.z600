package at.sunplugged.z600.main.impl;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.main.api.ControllerInterface;
import at.sunplugged.z600.main.controlling.MainController;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class ControllerInterfaceImpl implements ControllerInterface {

    /** This will be the thread which does the work of main controlling. */
    private Thread mainControllerThread;

    /** The DataService Service. */
    private static DataService dataService;

    private static LogService logService;

    private static SrmCommunicator srmCommunicator;

    private static MachineStateService machineStateService;

    public ControllerInterfaceImpl() {

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

    @Override
    public void startMainControl() {
        mainControllerThread.start();
    }

    public static DataService getDataService() {
        return dataService;
    }

    public static LogService getLogService() {
        return logService;
    }

    public synchronized void setLogService(LogService logService) {
        ControllerInterfaceImpl.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (ControllerInterfaceImpl.logService == logService) {
            ControllerInterfaceImpl.logService = null;
        }
    }

    public synchronized void setDataService(DataService dataService) {
        ControllerInterfaceImpl.dataService = dataService;
    }

    public synchronized void unsetDataService(DataService dataService) {
        if (ControllerInterfaceImpl.dataService == dataService) {
            ControllerInterfaceImpl.dataService = null;
        }
    }

    public synchronized void setSrmCommunicator(SrmCommunicator srmCommunicator) {
        ControllerInterfaceImpl.srmCommunicator = srmCommunicator;
    }

    public synchronized void unsetSrmCommunicator(SrmCommunicator srmCommunicator) {
        if (ControllerInterfaceImpl.srmCommunicator == srmCommunicator) {
            ControllerInterfaceImpl.srmCommunicator = null;
        }
    }

    public static SrmCommunicator getSrmCommunicator() {
        return srmCommunicator;
    }

    public synchronized void setMachineStateService(MachineStateService machineStateService) {
        ControllerInterfaceImpl.machineStateService = machineStateService;
    }

    public synchronized void unsetMachineStateService(MachineStateService machineStateService) {
        if (ControllerInterfaceImpl.machineStateService == machineStateService) {
            ControllerInterfaceImpl.machineStateService = null;
        }
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

}
