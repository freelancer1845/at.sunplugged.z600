package at.sunplugged.z600.backend.dataservice.api;

import at.sunplugged.z600.backend.dataservice.impl.DataServiceException;

public interface DataService {

    public void startUpdate() throws DataServiceException;

    public void stopUpdate();

}
