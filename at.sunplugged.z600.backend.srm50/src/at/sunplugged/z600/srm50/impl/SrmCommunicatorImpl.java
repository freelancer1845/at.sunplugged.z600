package at.sunplugged.z600.srm50.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.srm50.SrmActivator;
import at.sunplugged.z600.srm50.api.Commands;
import at.sunplugged.z600.srm50.api.SrmCommunicator;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Class implementing {@link SrmCommunicator}.
 * 
 * @author Jascha Riedel
 *
 */
public class SrmCommunicatorImpl implements SrmCommunicator {

    private CommPort commPort;

    private InputStream inputStream;

    private InputStreamReader inputStreamReader;

    private OutputStream outputStream;

    private OutputStreamWriter outputStreamWriter;

    private LogService logService = SrmActivator.getLogService();

    private Pattern measurementPattern = Pattern.compile("[0-9\\.\\s]{7}");

    @Override
    public void connect(String comPort) throws IOException {

        CommPortIdentifier commPortIdentifier;
        try {
            commPortIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
        } catch (NoSuchPortException e) {
            throw new IOException(e.getMessage() + " - Getting Identifer failed.");
        }
        if (commPortIdentifier.isCurrentlyOwned()) {
            throw new IOException("Port Is Already In Use");
        }

        System.out.println("Connecting to port: " + comPort);
        try {
            this.commPort = commPortIdentifier.open(this.getClass().getName(), 2000);
        } catch (PortInUseException e) {
            throw new IOException(e.getMessage() + " - Openen Port failed.");
        } finally {
            this.commPort = null;
        }
        if (this.commPort instanceof SerialPort) {
            SerialPort serialPort = (SerialPort) commPort;

            try {
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                e.printStackTrace();
                throw new IOException(e.getMessage() + " - failed to set Parameters.");
            }

            inputStream = serialPort.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            outputStream = serialPort.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
        }
    }

    @Override
    public void disconnect() throws IOException {
        commPort.close();
        inputStream = null;
        inputStreamReader = null;
        outputStream = null;
        outputStreamWriter = null;
        commPort = null;

    }

    @Override
    public List<Double> readChannels() throws IOException {
        List<Double> returnList = new ArrayList<>(3);

        String answer = doCommand(Commands.MEASUREMENT, true);
        Matcher matcher = measurementPattern.matcher(answer);
        while (matcher.find()) {
            String currentValue = matcher.group();
            returnList.add(Double.valueOf(currentValue));
        }
        return returnList;
    }

    @Override
    public Enumeration getPortIdentifiers() {
        return CommPortIdentifier.getPortIdentifiers();
    }

    private String doCommand(String command, boolean repeatIfFailed) throws IOException {
        if (commPort == null) {
            logService.log(LogService.LOG_ERROR, "Command Issued when there was no Port open.");
            throw new IOException("Command Issued when there was no Port open.");
        }
        outputStreamWriter.write(command);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            logService.log(LogService.LOG_ERROR, "doCommand Interrupted during waiting for answer.", e);
        }
        String answer = "";
        int currentChar;
        while ((currentChar = inputStreamReader.read()) > -1) {
            answer += (char) currentChar;
        }
        if (!answer.startsWith(command)) {
            if (repeatIfFailed) {
                doCommand(command, false);
            } else {
                throw new IOException("Answer didn't start with command issued");
            }
        }

        return answer.substring(command.length() + 1);
    }

    @Override
    public String issueCommand(String string) throws IOException {
        return doCommand(string, true);
    }

}
