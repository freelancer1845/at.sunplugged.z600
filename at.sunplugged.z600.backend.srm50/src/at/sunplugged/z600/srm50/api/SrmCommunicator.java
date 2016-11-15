package at.sunplugged.z600.srm50.api;

import java.io.IOException;

/**
 * This Interface represents a communication point for the SRM-50-3LD SN 105598.
 * 
 * @author Jascha Riedel
 *
 */
public interface SrmCommunicator {

    /**
     * Connects to the COM Port specified.
     * 
     * @param comPort
     *            identifier.
     * @throws IOException
     *             if connections fails.
     */
    public void connect(String comPort) throws IOException;

    /**
     * Disconnects from the COM Port.
     * 
     * @throws IOException
     *             if there is no connection or the disconnect failed.
     */
    public void disconnect() throws IOException;

    /**
     * Reads out the specified channel.
     * 
     * @param channel
     *            to be read out.
     * @return Measured value at this channel.
     * @throws IOException
     *             if retrieving failed.
     */
    public double readChannel(int channel) throws IOException;

}
