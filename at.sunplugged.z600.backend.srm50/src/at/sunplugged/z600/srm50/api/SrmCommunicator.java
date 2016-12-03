package at.sunplugged.z600.srm50.api;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

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
     * @param comPort identifier.
     * @throws IOException if connections fails.
     */
    public void connect(String comPort) throws IOException;

    /**
     * Disconnects from the COM Port.
     * 
     * @throws IOException if there is no connection or the disconnect failed.
     */
    public void disconnect() throws IOException;

    /**
     * Reads out the all channels and returns a List of Doubles.
     *
     * @return {@linkplain List\<{@linkplain Double}\>} of values.
     * @throws IOException if retrieving failed.
     */
    public List<Double> readChannels() throws IOException;

    /**
     * Get Available Ports.
     * 
     * @return {@link Enumeration}.
     */
    public String[] getPortNames();

    /**
     * Issues the given command and returns the Answer.
     * 
     * @param string {@linkplain String} the command (A list of commands is
     *            available at
     *            {@linkplain at.sunplugged.z600.srm50.api.Commands}
     * @return {@linkplain String} answer.
     */
    public String issueCommand(String string) throws IOException;
}
