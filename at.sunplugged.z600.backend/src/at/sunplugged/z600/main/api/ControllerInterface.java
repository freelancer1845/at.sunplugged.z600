package at.sunplugged.z600.main.api;

import at.sunplugged.z600.backend.dataservice.api.DataServiceException;

/**
 * Provides the interface between the frontend and the backend. All commands from the frontend
 * should use one of the functions defined here.
 * 
 * @author Jascha Riedel
 *
 */
public interface ControllerInterface {

    public enum MachineState {
        STOPPED(), PREPARING_START(), STARTING(), CREATING_PRE_VAKUUM(), CREATING_VAKUUM_FOR_TMP(), CREATING_FINAL_VAKUUM(), CLEARING_CRYO_PUMPS(), STARTING_KATHODES(), PROCESS_RUNNING, SHUTTING_DOWN();
    }

    /**
     * Starts the main Control thread.
     */
    public void startMainControl();

    /**
     * This method starts logging of all registered Variables to the SQL Log.
     * 
     */
    public void startSQLLogging(String hostname, String username, String password) throws DataServiceException;

    /**
     * This Method is only temporary. It allows for the logging of the SRM Data to an existing SQL
     * Table. The Table will be chosen automatically.
     * 
     * @deprecated
     * @param hostname of the SQL Server.
     * @param username of the account.
     * @param password of the accout.
     */
    public void startSrmSqlLogging(String hostname, String username, String password) throws DataServiceException;

}
