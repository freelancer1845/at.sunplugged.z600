package at.sunplugged.z600.core.machinestate.api;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public interface OutletControl {

    /**
     * Returns whether the outlet is open.
     * 
     * @param number
     *            identifying the outlet.
     * @return true if open, false if closed.
     * @throws IOException
     */
    public boolean isOutletOpen(DigitalOutput digitalOutput) throws IOException;

    /**
     * Closes the outlet.
     * 
     * @param number
     *            identifying the outlet.
     * @throws IOException
     */
    public void closeOutlet(DigitalOutput digitalOutput) throws IOException;

    /**
     * Opens the outlet.
     * 
     * @param number
     *            identifying the outlet.
     * @throws IOException
     */
    public void openOutlet(DigitalOutput digitalOutput) throws IOException;
}
