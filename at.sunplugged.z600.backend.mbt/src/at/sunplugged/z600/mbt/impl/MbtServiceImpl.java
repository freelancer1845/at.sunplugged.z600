package at.sunplugged.z600.mbt.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.Events;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.mbt.api.MbtServiceException;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteCoilResponse;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

@Component
public class MbtServiceImpl implements MbtService {

    /** The Connection. */
    private TCPMasterConnection connection;

    /** Lock that will be used to prevent concurrent actions. */
    private Object lock = new Object();

    /** address of the slave. */
    private InetAddress addr = null;

    /** Port to be used. */
    private int port = Modbus.DEFAULT_PORT;

    private LogService logService;

    private SettingsService settingsService;

    private EventAdmin eventAdmin;

    private StandardThreadPoolService threadPool;

    @Activate
    protected synchronized void activate(BundleContext context) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    connect();
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to connect to Modbus Controller.", e);
                }
            }
        });
    }

    @Deactivate
    protected synchronized void deactivate() {
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {

                    disconnect();
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to disconnect from Modbus Controller.", e);
                }

            }

        });
    }

    private void connect() throws IOException {
        addr = InetAddress.getByName(settingsService.getProperty(NetworkComIds.MBT_CONTROLLER_IP));
        connection = new TCPMasterConnection(addr);
        connection.setPort(port);
        try {
            connection.connect();
            logService.log(LogService.LOG_DEBUG, "Successfully conntected to MBT Controller");
            postConnectEvent(true, null);
        } catch (Exception e) {
            postConnectEvent(false, e);
            throw new MbtServiceException("Failed to connect to MBT", e);
        }

    }

    private void disconnect() throws IOException {
        connection.close();
        logService.log(LogService.LOG_DEBUG, "Disconnected successfully from MBT Controller.");
    }

    @Override
    public void writeDigOut(int digOut, boolean value) throws IOException {
        if (!isConnected()) {
            throw new MbtServiceException("No MBT Connection open!");
        }
        WriteCoilRequest writeCoilRequest = new WriteCoilRequest(digOut, value);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        WriteCoilResponse writeCoilResponse;

        modbusTransaction.setRequest(writeCoilRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MbtServiceException("Failed to write DigOut. DigOut: " + digOut, e);
            }
        }
        writeCoilResponse = (WriteCoilResponse) modbusTransaction.getResponse();
        if (writeCoilResponse.getCoil() != value) {
            throw new MbtServiceException(
                    "Failed to write DigOut. DigOut: " + digOut + ". Response does not equal desired value.");
        }
    }

    @Override
    public List<Boolean> readDigOuts(int startAddress, int outsToRead) throws IOException {
        if (!isConnected()) {
            throw new MbtServiceException("No MBT Connection open!");
        }
        ReadCoilsRequest readCoilsRequest = new ReadCoilsRequest(startAddress, outsToRead);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadCoilsResponse readCoilsResponse;

        modbusTransaction.setRequest(readCoilsRequest);
        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MbtServiceException("Failed to read DigOuts.", e);
            }
        }
        readCoilsResponse = (ReadCoilsResponse) modbusTransaction.getResponse();

        List<Boolean> returnList = new ShiftedArrayList<>(startAddress);
        for (int i = 0; i < readCoilsResponse.getBitCount(); i++) {
            returnList.add(readCoilsResponse.getCoilStatus(i));
        }
        return returnList;

    }

    @Override
    public List<Boolean> readDigIns(int startAddress, int insToRead) throws IOException {
        if (!isConnected()) {
            throw new MbtServiceException("No MBT Connection open!");
        }
        ReadInputDiscretesRequest readInputDiscretesRequest = new ReadInputDiscretesRequest(startAddress, insToRead);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadInputDiscretesResponse readInputDiscretesResponse;

        modbusTransaction.setRequest(readInputDiscretesRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MbtServiceException("Failed to read DigIn.", e);
            }
        }

        readInputDiscretesResponse = (ReadInputDiscretesResponse) modbusTransaction.getResponse();
        List<Boolean> returnList = new ShiftedArrayList<>(startAddress);
        for (int i = 0; i < readInputDiscretesResponse.getDiscretes().size(); i++) {
            returnList.add(readInputDiscretesResponse.getDiscreteStatus(i));
        }
        return returnList;
    }

    @Override
    public List<Integer> readInputRegister(int startAddress, int insRegsToRead) throws IOException {
        if (!isConnected()) {
            throw new MbtServiceException("No MBT Connection open!");
        }

        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadInputRegistersRequest readInputRegistersRequest = new ReadInputRegistersRequest(startAddress,
                insRegsToRead);
        ReadInputRegistersResponse readInputRegistersResponse;

        modbusTransaction.setRequest(readInputRegistersRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MbtServiceException("Failed to read Analog In.", e);
            }
        }
        readInputRegistersResponse = (net.wimpi.modbus.msg.ReadInputRegistersResponse) modbusTransaction.getResponse();
        List<Integer> returnList = new ShiftedArrayList<>(startAddress);
        for (int i = 0; i < readInputRegistersResponse.getWordCount(); i++) {
            returnList.add((int) (readInputRegistersResponse.getRegisterValue(i) / 8.0));
        }
        return returnList;
    }

    @Override
    public void writeOutputRegister(int anaOut, int value) throws IOException {
        if (!isConnected()) {
            throw new MbtServiceException("No MBT Connection open!");
        }

        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        Register registerValue = new SimpleRegister(value * 8);
        WriteSingleRegisterRequest writeSingleRegisterRequest = new WriteSingleRegisterRequest(anaOut, registerValue);
        WriteSingleRegisterResponse writeSingleRegisterResponse;

        modbusTransaction.setRequest(writeSingleRegisterRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MbtServiceException("Failed to write Analog Out. AnOut: " + anaOut + ". Value: " + value, e);
            }
        }
        writeSingleRegisterResponse = (WriteSingleRegisterResponse) modbusTransaction.getResponse();
        if (writeSingleRegisterResponse.getRegisterValue() != value) {
            throw new MbtServiceException("Failed to write Analog Out. AnOut: " + anaOut + ". Value: " + value
                    + ". Repsone value is unequal desired value.");
        }
    }

    @Override
    public List<Integer> readOutputRegister(int startAddress, int outRegsToRead) throws IOException {
        if (!isConnected()) {
            throw new MbtServiceException("No MBT Connection open!");
        }
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadMultipleRegistersRequest readMultipleRegistersRequest = new ReadMultipleRegistersRequest(startAddress,
                outRegsToRead);
        ReadMultipleRegistersResponse readMultipleRegistersResponse;

        modbusTransaction.setRequest(readMultipleRegistersRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MbtServiceException("Failed to read Analog out!", e);
            }
        }
        readMultipleRegistersResponse = (ReadMultipleRegistersResponse) modbusTransaction.getResponse();
        List<Integer> returnList = new ShiftedArrayList<>(startAddress);
        for (int i = 0; i < readMultipleRegistersResponse.getWordCount(); i++) {
            returnList.add((int) (readMultipleRegistersResponse.getRegisterValue(i) / 8.0));
        }
        return returnList;
    }

    @Override
    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        return connection.isConnected();
    }

    private void postConnectEvent(boolean successful, Throwable e) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("IP", addr.getHostAddress());
        properties.put("success", successful);
        if (!successful) {
            properties.put("Error", e);
        }
        eventAdmin.postEvent(new Event(Events.MBT_CONNECT_EVENT, properties));
    }

    /** Bind method for LogService. */
    @Reference(unbind = "unsetLogService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void setLogService(LogService logService) {
        this.logService = logService;
    }

    /** Unbind method for LogService. */
    public synchronized void unsetLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    @Reference(unbind = "unbindSettingsService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public synchronized void unbindSettingsService(SettingsService settingsService) {
        if (this.settingsService == settingsService) {
            this.settingsService = null;
        }
    }

    @Reference(unbind = "unbindEventAdmin")
    public synchronized void bindEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public synchronized void unbindEventAdmin(EventAdmin eventAdmin) {
        if (this.eventAdmin == eventAdmin) {
            this.eventAdmin = null;
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

}
