package at.sunplugged.z600.conveyor.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.constants.EngineConstants;
import at.sunplugged.z600.conveyor.impl.ConveyorControlServiceImpl;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class EngineSerialCom implements Engine {

    private final String portName;

    private final int engineAddress;

    private CommPort commPort = null;

    private InputStream inputStream;

    private OutputStream outputStream;

    private StandardThreadPoolService threadPool;

    private LogService logService;

    private ReentrantLock lock = new ReentrantLock(true);

    public EngineSerialCom(String portName, int engineAddress) {
        this.portName = portName;
        this.engineAddress = engineAddress;
        logService = ConveyorControlServiceImpl.getLogService();
        threadPool = ConveyorControlServiceImpl.getStandardThreadPoolService();
    }

    public void connect() {
        if (commPort != null) {
            throw new IllegalStateException("Engine already connected. Create a new enginge to reconnect!");
        }

        CommPortIdentifier portIdentifier = null;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            this.commPort = portIdentifier.open(this.getClass().getName(), 2000);
            if (this.commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(EngineConstants.SERIAL_BAUDRATE, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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
        initializeEngine();

    }

    public void disconnect() {
        if (commPort != null) {
            commPort.close();
            commPort = null;
        }
    }

    public boolean isConnected() {
        if (commPort == null) {
            return false;
        }
        return true;
    }

    public void setEngineMode(int mode) {
        sendCommand("!" + mode);
    }

    public void increaseSpeed() {
        sendCommand("+");
    }

    public void decreaseSpeed() {
        sendCommand("-");
    }

    public void setMaximumSpeed(int speed) {
        if (speed <= 60 || speed >= 25000) {
            logService.log(LogService.LOG_WARNING,
                    "Setting maximum speed to \"" + speed + "\" is not allowed (61- 24999)");
            return;
        }

        sendCommand("o" + speed);
    }

    public void startEngine() {
        sendCommand("A");
    }

    public void stopEngine() {
        sendCommand("S1");
    }

    public void stopEngineHard() {
        sendCommand("S0");
    }

    public void setDirection(int direction) {
        if (direction != 0 && direction != 1) {
            throw new IllegalStateException("Not allowed direction change: " + direction);
        }
        sendCommand("d" + direction);
    }

    private void initializeEngine() {
        this.setPositionMode();
        this.setEngineMode(EngineConstants.INITIAL_ENGINE_MODE);
        this.setRampMode();

    }

    private void setPositionMode() {
        sendCommand("p" + EngineConstants.INITIAL_POSITION_MODE);
    }

    private void setRampMode() {
        sendCommand(":ramp_mode=" + EngineConstants.INITIAL_RAMP_MODE);
    }

    private void sendCommand(String command) {
        if (!isConnected()) {
            logService.log(LogService.LOG_WARNING, "Engine is not conncted! Ignoring command.");
            return;
        }
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                lock.lock();
                try {
                    String fullCommand = "#" + engineAddress + command + "\r";
                    outputStream.write(fullCommand.getBytes(StandardCharsets.US_ASCII));
                    int timer = 0;
                    while (inputStream.available() < 1 && timer != 1000) {
                        Thread.sleep(1);
                        timer++;
                    }
                    if (timer == 1000) {
                        logService.log(LogService.LOG_ERROR, "No answer from engine on command: " + command
                                + " - Port: \"" + portName + "\" - Address: \"" + engineAddress + "\"");
                        return;
                    }
                    String answer = "";
                    while (inputStream.available() > 0) {
                        answer += (char) inputStream.read();
                    }
                    if (answer.substring(answer.length() - 3).equals("?\r")) {
                        logService.log(LogService.LOG_ERROR, "Engine did not understand the command: \"" + command
                                + "\". Answer: \"" + answer + "\"");
                    } else {
                        logService.log(LogService.LOG_DEBUG,
                                "Successfully issued command(" + command + ") to engine(" + portName + ")");
                    }

                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to send command to Engine!! " + command, e);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_ERROR,
                            "Sending command to motor interrupted after writing to outputStream!");
                } finally {
                    lock.unlock();
                }
            }
        });
    }

}
