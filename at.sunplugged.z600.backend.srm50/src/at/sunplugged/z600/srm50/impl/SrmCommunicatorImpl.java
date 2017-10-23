package at.sunplugged.z600.srm50.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.Events;
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
@Component(immediate = true)
public class SrmCommunicatorImpl implements SrmCommunicator {

    private CommPort commPort;

    private InputStream inputStream;

    private OutputStream outputStream;

    private LogService logService;

    private Pattern measurementPattern = Pattern.compile("[0-9\\.]+");

    private String commPortName;

    private List<Double> currentChannelValues = new ArrayList<>();

    private boolean running = false;

    @Reference
    private StandardThreadPoolService threadPool;

    @Reference
    private SettingsService settings;

    @Reference
    private EventAdmin eventAdmin;

    @Activate
    protected void activate() {

        String port = settings.getProperty(NetworkComIds.SRM_COM_PORT);
        try {
            connect(port);

        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to connect to srm (PORT: " + port + ")");
        }
    }

    @Override
    public void reconnect() throws IOException {
        /*
         * disconnect(); String port =
         * settings.getProperty(NetworkComIds.SRM_COM_PORT); connect(port);
         */
        logService.log(LogService.LOG_ERROR, "Function disabled --- produces fatal error...");
    }

    @Override
    public void connect() throws IOException {
        String port = settings.getProperty(NetworkComIds.SRM_COM_PORT);
        connect(port);
    }

    @Override
    public void connect(String comPort) throws IOException {

        if (commPort != null && comPort.equals(this.commPortName)) {
            throw new IOException("Connection to this Port is already open");
        }

        CommPortIdentifier commPortIdentifier;

        try {
            commPortIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
        } catch (NoSuchPortException e) {
            postConnectEvent(false, e);
            throw new IOException(e.getMessage() + " - Getting Identifer failed.");
        }

        if (commPortIdentifier.isCurrentlyOwned()) {
            IOException error = new IOException("Port Is Already In Use");
            postConnectEvent(false, error);
            throw error;
        }
        logService.log(LogService.LOG_DEBUG, "Connecting to port " + comPort);
        try {
            this.commPort = commPortIdentifier.open(this.getClass().getName(), 2000);
        } catch (PortInUseException e) {
            this.commPort = null;
            postConnectEvent(false, e);
            throw new IOException(e.getMessage() + " - Openen Port failed.");
        }
        if (this.commPort instanceof SerialPort) {
            SerialPort serialPort = (SerialPort) commPort;

            try {
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                throw new IOException(e.getMessage() + " - failed to set Parameters.");
            }

            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            this.commPortName = comPort;
        }

        try {
            doCommand(Commands.ACTI, false);
        } catch (IOException e1) {
            running = false;
            disconnect();
            postConnectEvent(false, e1);
            throw e1;
        }
        running = true;
        new Thread(new ChannelUpdater()).start();
        postConnectEvent(true, null);
    }

    private void postConnectEvent(boolean successful, Throwable e) {

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("success", successful);
        if (!successful) {
            properties.put("Error", e);
        }
        eventAdmin.postEvent(new Event(Events.SRM_CONNECT_EVENT, properties));
    }

    @Override
    public synchronized void disconnect() throws IOException {
        running = false;
        inputStream.close();
        outputStream.close();
        commPort.disableReceiveFraming();
        inputStream = null;
        outputStream = null;
        if (commPort != null) {
            commPort.close();
        }
        commPort = null;
    }

    @Override
    public List<Double> readChannels() throws IOException {
        if (running == false) {
            return null;
        }
        if (currentChannelValues.size() == 0) {
            return null;
        }
        return Collections.unmodifiableList(currentChannelValues);
    }

    @Override
    public String[] getPortNames() {
        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> identifiers = CommPortIdentifier.getPortIdentifiers();

        List<String> list = new ArrayList<>();
        while (identifiers.hasMoreElements()) {
            CommPortIdentifier identifier = identifiers.nextElement();
            if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                list.add(identifier.getName());
            }
        }
        return list.toArray(new String[] {});
    }

    private synchronized String doCommand(String command, boolean repeatIfFailed) throws IOException {
        if (commPort == null) {
            logService.log(LogService.LOG_ERROR, "Command Issued when there was no Port open.");
            throw new IOException("Command Issued when there was no Port open.");
        }
        byte[] commandArray = new String(command + (char) 13).getBytes();
        outputStream.write(commandArray);
        String answer = "";
        try {
            Thread.sleep(250);

            while (inputStream.available() > 0) {
                answer += (char) inputStream.read();
            }

            if (answer.equals("")) {
                Thread.sleep(50);
                while (inputStream.available() > 0) {
                    answer += (char) inputStream.read();
                }
            }
        } catch (InterruptedException e) {
            logService.log(LogService.LOG_ERROR, "doCommand Interrupted during waiting for answer.", e);
        }

        if (!answer.startsWith(command)) {
            if (repeatIfFailed) {
                doCommand(command, false);
            } else {
                throw new IOException("Answer didn't start with command issued, \"" + answer + "");
            }
        }

        if (command.equals("READ") == false) {
            logService.log(LogService.LOG_DEBUG, "Command executed: " + command);
        }
        return answer.substring(command.length());
    }

    @Override
    public String issueCommand(String string) throws IOException {
        return doCommand(string, true);
    }

    @Reference(unbind = "unsetLogService")
    public synchronized void setLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (this.logService == logService) {
            logService = null;
        }
    }

    @Deactivate
    public synchronized void deactivate() {
        if (commPort != null) {
            try {
                this.disconnect();
            } catch (IOException e) {
                this.logService.log(LogService.LOG_WARNING, "Error disconnecting com port.");
            }
        }
    }

    private class ChannelUpdater implements Runnable {

        private int errors = 0;

        private boolean error = false;

        @Override
        public void run() {

            Thread.currentThread().setName("SRM Reader Thread");
            while (running) {
                error = false;
                String answer;
                try {
                    answer = doCommand(Commands.MEASUREMENT, false);
                    Matcher matcher = measurementPattern.matcher(answer);
                    currentChannelValues.clear();
                    while (matcher.find()) {
                        String currentValue = matcher.group();
                        currentChannelValues.add(Double.valueOf(currentValue));
                    }
                } catch (IOException e1) {
                    errors++;
                    error = true;
                    logService.log(LogService.LOG_ERROR, "Failed to read data in channelupdater");
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_ERROR, "Channelupdater interupted...", e);
                }
                if (errors > 5) {
                    try {
                        disconnect();
                    } catch (IOException e) {
                    }
                    logService.log(LogService.LOG_ERROR, "Stopping srm reader thread... too many erros");
                }
                if (error == false) {
                    errors = 0;
                }
            }

        }

    }
}
