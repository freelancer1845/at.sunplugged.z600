package at.sunplugged.z600.mbt.api;

import java.io.IOException;
import java.util.List;

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

    public boolean isConnected();

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
     * @param startAddress
     * @param outsToRead
     * @return {@linkplain List<Boolean>} The index of that list is shifted by
     *         parameter stardAddress
     * @throws IOException
     *             if there is a connection error.
     */
    public List<Boolean> readDigOuts(int startAddress, int outsToRead) throws IOException;

    /**
     * Reads the current state of a digIn
     * 
     * @param stardAddress
     * @param insToRead
     * @return {@link List<Boolean>} The index of that list is shifted by
     *         parameter stardAddress.
     * @throws IOException
     *             if there is a connection error.
     */
    public List<Boolean> readDigIns(int stardAddress, int insToRead) throws IOException;

    public List<Integer> readInputRegister(int startAddress, int insRegsToRead) throws IOException;

    public void writeOutputRegister(int address, int value) throws IOException;

}
