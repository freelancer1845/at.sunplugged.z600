package at.sunplugged.z600.backend.dataservice.api;

public interface DataService {

    public void startUpdate() throws DataServiceException;

    public void stopUpdate();

}
