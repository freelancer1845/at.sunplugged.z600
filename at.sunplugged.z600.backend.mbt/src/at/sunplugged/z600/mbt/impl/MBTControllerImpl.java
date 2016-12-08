package at.sunplugged.z600.mbt.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.mbt.api.MBTController;
import at.sunplugged.z600.mbt.api.MBTControllerException;
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

public class MBTControllerImpl implements MBTController {

    /** The Connection. */
    private TCPMasterConnection connection;

    /** Lock that will be used to prevent concurrent actions. */
    private Object lock = new Object();

    /** address of the slave. */
    private InetAddress addr = null;

    /** Port to be used. */
    private int port = Modbus.DEFAULT_PORT;

    private LogService logService;

    @Override
    public void connect(String address) throws IOException {
        addr = InetAddress.getByName(address);
        connection = new TCPMasterConnection(addr);
        connection.setPort(port);
        try {
            connection.connect();
        } catch (Exception e) {
            throw new MBTControllerException("Failed to connect to MBT", e);
        }

    }

    @Override
    public void disconnect() throws IOException {
        connection.close();

    }

    @Override
    public void writeDigOut(int digOut, boolean value) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }
        WriteCoilRequest writeCoilRequest = new WriteCoilRequest(digOut, value);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        WriteCoilResponse writeCoilResponse;

        modbusTransaction.setRequest(writeCoilRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to write DigOut. DigOut: " + digOut, e);
            }
        }
        writeCoilResponse = (WriteCoilResponse) modbusTransaction.getResponse();
        if (writeCoilResponse.getCoil() != value) {
            throw new MBTControllerException(
                    "Failed to write DigOut. DigOut: " + digOut + ". Response does not equal desired value.");
        }
    }

    @Override
    public List<Boolean> readDigOuts(int startAddress, int outsToRead) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }
        ReadCoilsRequest readCoilsRequest = new ReadCoilsRequest(startAddress, outsToRead);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadCoilsResponse readCoilsResponse;

        modbusTransaction.setRequest(readCoilsRequest);
        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to read DigOuts.", e);
            }
        }
        readCoilsResponse = (ReadCoilsResponse) modbusTransaction.getResponse();

        List<Boolean> returnList = createReturnList(startAddress);
        for (int i = 0; i < readCoilsResponse.getBitCount(); i++) {
            returnList.add(readCoilsResponse.getCoilStatus(i));
        }
        return returnList;

    }

    @Override
    public List<Boolean> readDigIns(int startAddress, int insToRead) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }
        ReadInputDiscretesRequest readInputDiscretesRequest = new ReadInputDiscretesRequest(startAddress, insToRead);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadInputDiscretesResponse readInputDiscretesResponse;

        modbusTransaction.setRequest(readInputDiscretesRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to read DigIn.", e);
            }
        }

        readInputDiscretesResponse = (ReadInputDiscretesResponse) modbusTransaction.getResponse();
        List<Boolean> returnList = createReturnList(startAddress);
        for (int i = 0; i < readInputDiscretesResponse.getDiscretes().size(); i++) {
            returnList.add(readInputDiscretesResponse.getDiscreteStatus(i));
        }
        return returnList;
    }

    /** Bind method for LogService. */
    public synchronized void setLogService(LogService logService) {
        this.logService = logService;
    }

    /** Unbind method for LogService. */
    public synchronized void unsetLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    @Override
    public List<Integer> readInputRegister(int startAddress, int insRegsToRead) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
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
                throw new MBTControllerException("Failed to read Analog In.", e);
            }
        }
        readInputRegistersResponse = (net.wimpi.modbus.msg.ReadInputRegistersResponse) modbusTransaction.getResponse();
        List<Integer> returnList = createReturnList(startAddress);
        for (int i = 0; i < readInputRegistersResponse.getWordCount(); i++) {
            returnList.add(readInputRegistersResponse.getRegisterValue(i));
        }
        return returnList;
    }

    @Override
    public void writeOutputRegister(int anaOut, int value) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }

        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        Register registerValue = new SimpleRegister(value);
        WriteSingleRegisterRequest writeSingleRegisterRequest = new WriteSingleRegisterRequest(anaOut, registerValue);
        WriteSingleRegisterResponse writeSingleRegisterResponse;

        modbusTransaction.setRequest(writeSingleRegisterRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to write Analog Out. AnOut: " + anaOut + ". Value: " + value,
                        e);
            }
        }
        writeSingleRegisterResponse = (WriteSingleRegisterResponse) modbusTransaction.getResponse();
        if (writeSingleRegisterResponse.getRegisterValue() != value) {
            throw new MBTControllerException("Failed to write Analog Out. AnOut: " + anaOut + ". Value: " + value
                    + ". Repsone value is unequal desired value.");
        }
    }

    @Override
    public List<Integer> readOutputRegister(int startAddress, int outRegsToRead) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
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
                throw new MBTControllerException("Failed to read Analog out!", e);
            }
        }
        readMultipleRegistersResponse = (ReadMultipleRegistersResponse) modbusTransaction.getResponse();
        List<Integer> returnList = createReturnList(startAddress);
        for (int i = 0; i < readMultipleRegistersResponse.getWordCount(); i++) {
            returnList.add(readMultipleRegistersResponse.getRegisterValue(i));
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

    private <T> List<T> createReturnList(int startAddress) {
        List<T> returnList = new ArrayList<>();

        for (int i = 0; i < startAddress; i++) {
            returnList.add(null);
        }
        return returnList;
    }

}
