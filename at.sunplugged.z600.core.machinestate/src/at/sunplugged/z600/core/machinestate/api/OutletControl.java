package at.sunplugged.z600.core.machinestate.api;

import java.io.IOException;

public interface OutletControl {

    /**
     * Updates the state.
     * 
     * @throws IOException
     */
    public void update() throws IOException;

    /**
     * Returns whether the outlet is open.
     * 
     * @param number identifying the outlet.
     * @return true if open, false if closed.
     */
    public boolean isOutletOpen(int number);

    /**
     * Closes the outlet.
     * 
     * @param number identifying the outlet.
     */
    public void closeOutlet(int number);

    /**
     * Opens the outlet.
     * 
     * @param number identifying the outlet.
     */
    public void openOutlet(int number);
}
