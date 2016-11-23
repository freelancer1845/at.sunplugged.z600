package at.sunplugged.z600.backend.dataservice.api;

import java.util.Date;
import java.util.List;

/**
 * This service provides an interface to save data and retrieve it again. It also allows a
 * connection to an SQL server to save the data manually or automatically. This is a singleton
 * service (i. e. every service instance provides access to the same database).
 * 
 * 
 * @author Jascha Riedel
 *
 */
public interface DataService {

    /**
     * Opens a connection to an SQL server.
     * 
     * @param address of the server to use.
     * @param username of the account to use.
     * @param password of the account to use.
     * @throws DataServiceException if connecting to the server failed.
     */
    public void connectToSqlServer(String address, String username, String password) throws DataServiceException;

    /**
     * Starts the automatic updating or creation (if table doesn't exit) of given table in the
     * connected SQL Database.
     * 
     * @param tickrate updates that are done per min.
     * @param variableName of the table in the database (will be added if non existent).
     * @param columns that will be created. Must be the same names as are used in addData(...).
     * @throws DataServiceException if either there is no connection or the updating failed.
     */
    public void startAutomaticSqlTableUpdating(int tickrate, String variableName, String... columns)
            throws DataServiceException;

    /**
     * Stops the automatic table updating of the given table.
     * 
     * @param variableName to stop updating.
     * @throws DataServiceException if table doesn't exit or there is no updating for this table
     *             done.
     */
    public void stopAutomaticSqlTableUpdating(String variableName) throws DataServiceException;

    /**
     * Creates a snapshot of the current data contained in the DataService.
     * 
     * @param filePath of the outputFile. Will be appended by the current date.
     * @throws DataServiceException if this fails.
     */
    public void createDataBaseSnapshot(String filePath) throws DataServiceException;

    /**
     * Saves the data provided to the given variableName or creates one if there is none.
     * 
     * @param variableName unique identifier of this variable. The variableName will be used for the
     *            SQL database saving. Remember it!
     * @throws DataServiceException if fails.
     */
    public void saveData(String variableName, Date date, Object data) throws DataServiceException;

    /**
     * Returns a list containing the current data set.
     * 
     * @param variableName of the data set.
     * @param type of the data set (i. e. Double.class)
     * @return A List of that data.
     * @throws DataServiceException if this fails.
     */
    public <T> List<T> getData(String variableName, Class<T> type) throws DataServiceException;

    /**
     * Clears database. Only locally not the SQL Database!!
     */
    public void clearDatabase();
}
