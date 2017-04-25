package at.sunplugged.z600.conveyor.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.event.Event;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.utils.Events;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.api.EngineException;
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

    private int currentMaximumSpeed = 0;

    private InputStream inputStream;

    private OutputStream outputStream;

    private StandardThreadPoolService threadPool;

    private LogService logService;

    private ReentrantLock lock = new ReentrantLock(true);

    private boolean ignoreCommands = true;

    private Direction direction = Direction.CLOCKWISE;

    private boolean running = false;

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
        } catch (Exception e) {
            postConnectEvent(false, e);
            throw e;
        }

        ignoreCommands = false;
        postConnectEvent(true, null);
        initializeEngine();

    }

    private void postConnectEvent(boolean successful, Throwable e) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("success", successful);
        if (!successful) {
            properties.put("Error", e);
        }
        ConveyorControlServiceImpl.getEventAdmin().postEvent(new Event(Events.ENGINE_CONNECT_EVENT, properties));
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

    public void setMaximumSpeed(int speed) throws EngineException {
        if (speed <= 60 || speed >= 25000) {
            logService.log(LogService.LOG_WARNING,
                    "Setting maximum speed to \"" + speed + "\" is not allowed (61- 24999)");
            throw new EngineException("Setting maximum speed to \"" + speed + "\" is not allowed (61- 24999)");
        }
        int maximumSpeed = Integer.valueOf(
                ConveyorControlServiceImpl.getSettingsService().getProperty(ParameterIds.ENGINE_MAXIMUM_SPEED));

        if (speed > maximumSpeed) {
            logService.log(LogService.LOG_DEBUG,
                    "Setting maximum speed higher than DEBUG value: \"" + maximumSpeed + "\"");
            throw new EngineException("Setting maximum speed to \"" + speed + "\" is not allowed (61- 24999)");
        }
        sendCommand("o" + speed);
        currentMaximumSpeed = speed;

    }

    @Override
    public int getCurrentMaximumSpeed() {
        return currentMaximumSpeed;
    }

    public void startEngine() {
        sendCommand("A");
        running = true;
    }

    public void setLoose() {
        sendCommand("S");
        running = false;
    }

    public void stopEngine() {
        sendCommand("S1");
        running = false;
    }

    public void stopEngineHard() {
        sendCommand("S0");
        running = false;
    }

    public void setDirection(int direction) {
        if (direction != 0 && direction != 1) {
            throw new IllegalStateException("Not allowed direction change: " + direction);
        }
        sendCommand("d" + direction);
    }

    @Override
    public void initializeEngine() {
        this.setPositionMode();
        this.setEngineMode(EngineConstants.INITIAL_ENGINE_MODE);
        this.setRampMode();
        this.setBreakMode();
        setClosedLoopFalse();
        resetPositionError();
    }

    private void setPositionMode() {
        sendCommand("p" + EngineConstants.INITIAL_POSITION_MODE);
    }

    private void setRampMode() {
        sendCommand(":ramp_mode=" + EngineConstants.INITIAL_RAMP_MODE);
    }

    private void setBreakMode() {
        sendCommand("P" + EngineConstants.INITIAL_BREAK_MODE);
    }

    private void setClosedLoopFalse() {
        sendCommand(":CL_enable=" + 0);
    }

    private void resetPositionError() {
        sendCommand("D");
    }

    private void sendCommand(String command) {
        if (ignoreCommands) {
            return;
        }
        if (!isConnected()) {
            logService.log(LogService.LOG_WARNING, "Engine is not conncted! Ignoringing all further commands.");
            ignoreCommands = true;
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
                        Thread.sleep(50);
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

    @Override
    public void setDirection(Direction direction) {
        if (direction == Direction.COUNTER_CLOCKWISE) {
            setDirection(0);
        } else if (direction == Direction.CLOCKWISE) {
            setDirection(1);
        }
        this.direction = direction;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

}
