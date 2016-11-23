package at.sunplugged.z600.mbt.api;

import java.io.IOException;

/**
 * Interface providing the functions of the MBT Controller specifically adjusted
 * for the use in the z600 machine.
 * 
 * 
 * @author Jascha Riedel
 *
 */
public interface MBTController {

    /**
     * Establishes a connection to a TCP/IP Modbus controller.
     * 
     * @param address
     *            of the Modbus controller (i. e. "192.168.1.219")
     * @throws IOException
     *             if connections fails.
     */
    public void connect(String address) throws IOException;

    /**
     * Disconencts the mtb.
     * 
     * @throws IOException
     *             if there is no open connection.
     */
    public void disconnect() throws IOException;

    /**
     * Write to a digOut.
     * 
     * @param digOut
     *            "name" of the digOut.
     * @param value
     *            New value for the digOut.
     * @throws IOException
     *             if there is a connection error.
     */
    public void writeDigOut(int digOut, boolean value) throws IOException;

    /**
     * Reads the current state of a digOut.
     * 
     * @param digOut
     *            "name" of the digOut to be read.
     * @return The "value" of this digOut.
     * @throws IOException
     *             if there is a connection error.
     */
    public boolean readDigOut(int digOut) throws IOException;

    /**
     * Reads the current state of a digIn
     * 
     * @param digIn
     *            "name" of the digIn.
     * @return The "value" of this digIn.
     * @throws IOException
     *             if there is a connection error.
     */
    public boolean readDigIn(int digIn) throws IOException;
}
