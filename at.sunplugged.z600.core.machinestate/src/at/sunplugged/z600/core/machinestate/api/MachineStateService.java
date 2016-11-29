package at.sunplugged.z600.core.machinestate.api;

import java.util.Date;

import at.sunplugged.z600.backend.dataservice.api.DataService;

/**
 * Contains getters and setters for all states, like V1 Open or closed and so on. Also contains all
 * measurement values.
 * 
 * @author Jascha Riedel
 *
 */
public interface MachineStateService {

    // put all the states of the machine here via getters and/or setters

    /**
     * Updates the current state of the machine (i. e. Ventils, Measurement Values and so on).
     * Measurements are autmatically saved in the {@linkplain DataService}.
     * 
     * @param Date snapShotdate date of the data aquisition
     */
    public void update(Date snapShotDate);

}
