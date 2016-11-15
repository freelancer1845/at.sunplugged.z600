package at.sunplugged.z600.srm50.impl;

import java.io.IOException;
import java.util.Random;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.srm50.SrmActivator;
import at.sunplugged.z600.srm50.api.SrmCommunicator;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

/**
 * Class implementing {@link SrmCommunicator}.
 * 
 * @author Jascha Riedel
 *
 */
public class SrmCommunicatorImpl implements SrmCommunicator {

    private String comPort;

    private LogService logService = SrmActivator.getLogService();

    /** Only used for testing purposes. */
    private Random random = new Random();

    @Override
    public void connect(String comPort) throws IOException {

        try {
            CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
        } catch (NoSuchPortException e) {
            throw new IOException(e.getMessage());
        }

        System.out.println("Connecting to port: " + comPort);
        this.comPort = comPort;
    }

    @Override
    public void disconnect() throws IOException {
        System.out.println("Disconnection from port: " + comPort);

    }

    @Override
    public double readChannel(int channel) throws IOException {

        return random.nextDouble() * 9999.9;
    }

}
