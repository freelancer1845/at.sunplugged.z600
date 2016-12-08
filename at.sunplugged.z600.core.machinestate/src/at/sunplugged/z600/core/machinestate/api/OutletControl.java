package at.sunplugged.z600.core.machinestate.api;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public interface OutletControl {

    public enum Outlet {
        OUTLET_ONE(DigitalOutput.OUTLET_ONE),
        OUTLET_TWO(DigitalOutput.OUTLET_TWO),
        OUTLET_THREE(DigitalOutput.OUTLET_THREE),
        OUTLET_FOUR(DigitalOutput.OUTLET_FOUR),
        OUTLET_FIVE(DigitalOutput.OUTLET_FIVE),
        OUTLET_SIX(DigitalOutput.OUTLET_SIX),
        OUTLET_NINE(DigitalOutput.OUTLET_NINE);

        private final DigitalOutput digitalOutput;

        private Outlet(DigitalOutput digitalOutput) {
            this.digitalOutput = digitalOutput;
        }

        public DigitalOutput getDigitalOutput() {
            return digitalOutput;
        }
    }

    /**
     * Returns whether the outlet is open.
     * 
     * @param number
     *            identifying the outlet.
     * @return true if open, false if closed.
     */
    public boolean isOutletOpen(Outlet outlet);

    /**
     * Closes the outlet.
     * 
     * @param number
     *            identifying the outlet.
     * @throws IOException
     */
    public void closeOutlet(Outlet outlet) throws IOException;

    /**
     * Opens the outlet.
     * 
     * @param number
     *            identifying the outlet.
     * @throws IOException
     */
    public void openOutlet(Outlet outlet) throws IOException;
}
