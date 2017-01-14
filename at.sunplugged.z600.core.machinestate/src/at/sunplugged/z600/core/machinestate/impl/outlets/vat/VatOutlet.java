package at.sunplugged.z600.core.machinestate.impl.outlets.vat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class VatOutlet {

    private static final int BAUDRATE = 19200;

    private static final String COMMAND_CLOSE = "C:";

    private static final String COMMAND_OPEN = "O:";

    private final String portName;

    private CommPort commPort;

    private InputStream inputStream;

    private OutputStream outputStream;

    private boolean state = false;

    private int currentPosition = 0;

    private StandardThreadPoolService threadPool;

    public VatOutlet(String portName) {
        connectToSerialPort(portName);
        threadPool = MachineStateServiceImpl.getStandardThreadPoolService();
        this.portName = portName;
    }

    public boolean isOpen() {
        return state;
    }

    public void open() {
    }

    public void close() {

    }

    public void setPosition(int position) {
        sendCommand("R:" + String.format("%06d", position));
    }

    public int getPosition() {
        return currentPosition;
    }

    private void sendCommand(String command) {
        if (commPort == null) {
            MachineStateServiceImpl.getLogService().log(LogService.LOG_WARNING,
                    "Vat Outlet is not connected! Ignoring Command.");
            return;
        }
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    String fullCommand = command + "\r\n";
                    outputStream.write(fullCommand.getBytes(StandardCharsets.US_ASCII));
                    int timer = 0;
                    while (inputStream.available() < 1 && timer != 1000) {
                        Thread.sleep(1);
                        timer++;
                    }
                    if (timer == 1000) {
                        MachineStateServiceImpl.getLogService().log(LogService.LOG_ERROR,
                                "No answer from vat on command: " + command + " - Port: \"" + portName + "\"");
                        return;
                    }

                } catch (IOException e) {
                    MachineStateServiceImpl.getLogService().log(LogService.LOG_ERROR,
                            "Error when sending command: \"" + command + "\"", e);
                } catch (InterruptedException e) {
                    MachineStateServiceImpl.getLogService().log(LogService.LOG_DEBUG,
                            "Sending command to VAT Ventil interrupted");
                }
                switch (command) {
                case COMMAND_CLOSE:
                    state = false;
                    currentPosition = 0;
                    break;
                case COMMAND_OPEN:
                    state = true;
                    currentPosition = 100;
                    break;
                default:
                    currentPosition = Integer.valueOf(command.substring(2));
                    break;
                }
            }

        });

    }

    private void connectToSerialPort(String portName) {
        if (commPort != null) {
            throw new IllegalStateException("Engine already connected. Create a new enginge to reconnect!");
        }

        CommPortIdentifier portIdentifier = null;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            this.commPort = portIdentifier.open(this.getClass().getName(), 2000);
            if (this.commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_ODD);
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
            } else {
                disconnect();
                throw new IllegalStateException("Not a Serial Port specified");
            }

        } catch (NoSuchPortException e) {
            disconnect();
            throw new IllegalStateException("There is no port with name: " + portName);
        } catch (PortInUseException e) {
            disconnect();
            throw new IllegalStateException("The port is already owned by: " + portIdentifier.getCurrentOwner());
        } catch (UnsupportedCommOperationException e) {
            disconnect();
            throw new IllegalStateException(e);
        } catch (IOException e) {
            disconnect();
            throw new IllegalStateException("Failed to open input and outputStream to serialPort!");
        }
    }

    private void disconnect() {
        if (commPort != null) {
            commPort.close();
        }
        commPort = null;
    }
}
