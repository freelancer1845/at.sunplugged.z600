package at.sunplugged.z600.backend.dataservice.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;

/**
 * Standard implementation of the DataService interface.
 * 
 * @author Jascha Riedel
 *
 */
public class DataServiceImpl implements DataService {

    private final Map<String, VariableSlot<?>> variableSlots = new HashMap<>();

    @Override
    public void connectToSqlServer(String address, String username, String password) throws DataServiceException {
        // TODO Auto-generated method stub
    }

    @Override
    public void startAutomaticSqlTableUpdating(int tickrate, String variableName, String... columns)
            throws DataServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopAutomaticSqlTableUpdating(String variableName) throws DataServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createDataBaseSnapshot(String filePath) throws DataServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveData(String variableName, Date date, Object data) throws DataServiceException {
        if (!variableSlots.containsKey(variableName)) {
            variableSlots.put(variableName, createVariableSlot(variableName, data.getClass().getName()));
        }
        variableSlots.get(variableName).addData(date, data);
    }

    @Override
    public void clearDatabase() {
        variableSlots.clear();
    }

    private VariableSlot<?> createVariableSlot(String variableName, String typeName) throws DataServiceException {

        switch (typeName) {
        case "Double":
            return new VariableSlot<Double>(variableName);
        default:
            throw new DataServiceException("The type of this data is not supported: \"" + typeName + "\"");
        }
    }

}
