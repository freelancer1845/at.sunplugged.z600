package at.sunplugged.z600.conveyor.impl.position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorPositionService;
import at.sunplugged.z600.conveyor.constants.EngineConstants;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.mbt.api.MbtService;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

@Component(immediate = true)
public class ConveyorPositionServiceImpl implements ConveyorPositionService, MachineEventHandler {

    private static final int LEFT_CHANNEL = 55;

    private static final int RIGHT_CHANNEL = 54;

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private SettingsService settingsService;

    private StandardThreadPoolService threadPool;

    private ConveyorControlService conveyorControlService;

    private CommPort commPort;

    private InputStream inputStream;

    private BufferedReader reader;

    private OutputStream outputStream;

    private ScheduledFuture<?> scheduledFuture;

    private PositionControl positionControl;

    private boolean controlPosition = false;

    @Override
    public void start() {

        // if (commPort == null) {
        // connect(settingsService.getProperty(NetworkComIds.XPLAINED_COM_PORT));
        // }
        // if (commPort == null) {
        // throw new IllegalStateException("Can't start ConveyorPosition
        // control... Can't connect to xplained.");
        // }
        scheduledFuture = threadPool.timedPeriodicExecute(new Runnable() {

            @Override
            public void run() {

                try {
                    tick();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    stop();
                }
            }

        }, 100, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        try {
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void tick() throws IOException, InterruptedException {
        // byte[] buffer = new byte[1];
        // String answer;
        //
        // buffer[0] = LEFT_CHANNEL;
        // outputStream.write(buffer);
        // Thread.sleep(30);
        // answer = reader.readLine();
        // positionControl.addLeftPosition(Double.valueOf(answer));
        //
        // buffer[0] = RIGHT_CHANNEL;
        // outputStream.write(buffer);
        // Thread.sleep(30);
        // answer = reader.readLine();
        // positionControl.addRightPosition(Double.valueOf(answer));

        if (controlPosition == true) {
            positionControl.tick();
        }
    }

    @Activate
    protected void activate() {
        positionControl = new PositionControl(machineStateService, mbtService, conveyorControlService);
        machineStateService.registerMachineEventHandler(this);
    }

    private void connect(String portName) {
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
                    reader = new BufferedReader(new InputStreamReader(inputStream));
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
            throw e;
        }
    }

    private void disconnect() {
        if (commPort != null) {
            commPort.close();
            commPort = null;
        }
    }

    @Override
    public double getLeftPosition() {
        return positionControl.getLeftPosition();
    }

    @Override
    public double getRightPosition() {
        return positionControl.getRightPosition();
    }

    @Reference(unbind = "unbindMachineStateService")
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (this.machineStateService == machineStateService) {
            this.machineStateService = null;
        }
    }

    @Reference(unbind = "unbindSettingsService")
    public synchronized void bindSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public synchronized void unbindSettingsService(SettingsService settingsService) {
        if (this.settingsService == settingsService) {
            this.settingsService = null;
        }
    }

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        this.threadPool = threadPool;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        if (this.threadPool == threadPool) {
            this.threadPool = null;
        }
    }

    @Reference(unbind = "unbindMbtService")
    public synchronized void bindMbtService(MbtService mbtService) {
        this.mbtService = mbtService;
    }

    public synchronized void unbindMbtService(MbtService mbtService) {
        if (this.mbtService == mbtService) {
            this.mbtService = null;
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        // if (event.getType() == MachineStateEvent.Type.DIGITAL_INPUT_CHANGED)
        // {
        // if ((boolean) event.getValue() == true) {
        // try {
        // if (event.getOrigin().equals(DigitalInput.LIMIT_SWITCH_LEFT_BACK)) {
        // mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(),
        // false);
        // } else if
        // (event.getOrigin().equals(DigitalInput.LIMIT_SWITCH_LEFT_FRONT)) {
        // mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(),
        // false);
        // } else if
        // (event.getOrigin().equals(DigitalInput.LIMIT_SWITCH_RIGHT_BACK)) {
        // mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(),
        // false);
        // } else if
        // (event.getOrigin().equals(DigitalInput.LIMIT_SWITCH_RIGHT_FRONT)) {
        // mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(),
        // false);
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        //
        // }
        // }
    }

    @Override
    public void togglePositionControl(boolean state) {
        this.controlPosition = state;
    }

    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (this.conveyorControlService == conveyorControlService) {
            this.conveyorControlService = null;
        }
    }

}
