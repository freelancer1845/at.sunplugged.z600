package at.sunplugged.z600.srm50.impl;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.srm50.api.Commands;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class SrmPortManager {

    public static SrmCommPort getCommPort(String commPort, LogService logService) throws IOException {
        SrmCommPort srmPort = null;
        try {
            NRSerialPort port = new NRSerialPort(commPort, 9600);
            if (port.connect() == false) {
                throw new IOException("Failed to connect to port: " + commPort);
            }
            try {
                port.getSerialPortInstance().setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                throw new IOException("Failed to open Port...", e);
            }
            // CommPort port = connect(commPort);
            srmPort = SrmCommPort.createCommPort(logService, port);
            testPort(srmPort);
        } catch (IOException e) {
            logService.log(LogService.LOG_DEBUG, "Failed to get commpPort: \"" + e.getMessage() + "\"", e);
            if (srmPort != null) {
                srmPort.close();
            }
            throw e;
        }

        return srmPort;
    }

    private static void testPort(SrmCommPort srmPort) throws IOException {
        try {
            srmPort.doCommand(Commands.ACTI, true);
        } catch (IOException e) {
            throw new IOException("Testing SrmPort failed...", e);
        }
    }

    private static CommPort connect(String commPortName) throws IOException {

        CommPort commPort;
        CommPortIdentifier commPortIdentifier;

        try {
            commPortIdentifier = CommPortIdentifier.getPortIdentifier(commPortName);
        } catch (NoSuchPortException e) {
            throw new IOException(e.getMessage() + " - Getting Identifer failed.");
        }

        if (commPortIdentifier.isCurrentlyOwned()) {
            IOException error = new IOException("Port Is Already In Use");
            throw error;
        }
        try {
            Thread.sleep(100);
            commPort = commPortIdentifier.open(SrmPortManager.class.getName(), 2000);
            Thread.sleep(100);
        } catch (PortInUseException e) {
            commPort = null;
            throw new IOException(e.getMessage() + " - Opening Port failed.");
        } catch (InterruptedException e) {
            commPort = null;
            e.printStackTrace();
        }
        if (commPort instanceof SerialPort) {
            SerialPort serialPort = (SerialPort) commPort;

            try {
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                throw new IOException(e.getMessage() + " - failed to set Parameters.");
            }

        }
        return commPort;
    }

}
