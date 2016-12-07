package at.sunplugged.z600.core.machinestate.api;

import java.util.Date;

import at.sunplugged.z600.backend.dataservice.api.DataService;

/**
 * Contains getters and setters for all states, like V1 Open or closed and so
 * on. Also contains all measurement values. By that the interface provides
 * access to all values of the machine.
 * 
 * @author Jascha Riedel
 *
 */
public interface MachineStateService {

    /**
     * Updates the current state of the machine (i. e. Outlets, Measurement
     * Values and so on). Measurements are automatically saved in the
     * {@linkplain DataService}.
     * 
     * @param Date
     *            snapShotdate date of the data acquisition
     */
    public void update(Date snapShotDate);

    /**
     * Returns the current pressure at the specified location.
     * 
     * @param at
     *            which point in the machine.
     * @return the value when the last update was done.
     */
    public double getPressureValue(int at);

    /**
     * 
     * @return the {@linkplain OutletControl} Interface.
     */
    public OutletControl getOutletControl();

    /**
     * 
     * @return the {@linkplain PumpControl} Interface.
     */
    public PumpControl getPumpControl();

}
