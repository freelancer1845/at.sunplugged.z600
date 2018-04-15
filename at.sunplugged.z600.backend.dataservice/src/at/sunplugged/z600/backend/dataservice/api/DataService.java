package at.sunplugged.z600.backend.dataservice.api;

import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;

public interface DataService {

    public static final String TABLE_NAME = "PROCESS_DATA";

    public static final String SCHEMA = "z600";

    public void startUpdate() throws DataServiceException;

    public void stopUpdate();

    public String[] getTargetMaterials();

    public void mapTargetToPowersource(PowerSourceId id, String material);

}
