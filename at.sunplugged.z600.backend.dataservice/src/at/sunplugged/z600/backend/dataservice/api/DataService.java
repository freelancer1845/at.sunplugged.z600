package at.sunplugged.z600.backend.dataservice.api;

import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;

public interface DataService {

    public void startUpdate() throws DataServiceException;

    public void stopUpdate();

    public String[] getTargetMaterials();

    public void mapTargetToPowersource(PowerSourceId id, String material);

}
