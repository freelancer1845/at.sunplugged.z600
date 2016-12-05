package at.sunplugged.z600.mbt.impl;

import java.io.IOException;
import java.net.InetAddress;

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
    public void writeDigOut(int coil, int digOut, boolean value) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }
        WriteCoilRequest writeCoilRequest = new WriteCoilRequest((coil * 6) + digOut, value);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        WriteCoilResponse writeCoilResponse;

        modbusTransaction.setRequest(writeCoilRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to write DigOut. Coil: " + coil + ". DigOut: " + digOut, e);
            }
        }
        writeCoilResponse = (WriteCoilResponse) modbusTransaction.getResponse();
        if (writeCoilResponse.getCoil() != value) {
            throw new MBTControllerException("Failed to write DigOut. Coil: " + coil + ". DigOut: " + digOut
                    + ". Response does not equal desired value.");
        }
    }

    @Override
    public boolean readDigOut(int coil, int digOut) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }
        ReadCoilsRequest readCoilsRequest = new ReadCoilsRequest(coil * 6 + digOut, 1);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadCoilsResponse readCoilsResponse;

        modbusTransaction.setRequest(readCoilsRequest);
        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to read DigOut. Coil: " + coil + ". DigOut: " + digOut, e);
            }
        }
        readCoilsResponse = (ReadCoilsResponse) modbusTransaction.getResponse();
        return readCoilsResponse.getCoilStatus(0);

    }

    @Override
    public boolean readDigIn(int coil, int digIn) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }
        ReadInputDiscretesRequest readInputDiscretesRequest = new ReadInputDiscretesRequest(coil * 6 + digIn, 1);
        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadInputDiscretesResponse readInputDiscretesResponse;

        modbusTransaction.setRequest(readInputDiscretesRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to read DigIn. Coil: " + coil + ". DigIn: " + digIn, e);
            }
        }

        readInputDiscretesResponse = (ReadInputDiscretesResponse) modbusTransaction.getResponse();
        return readInputDiscretesResponse.getDiscreteStatus(0);
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
    public int readInputRegister(int register, int anaIn) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }

        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        ReadInputRegistersRequest readInputRegistersRequest = new ReadInputRegistersRequest(register * 4 + anaIn, 1);
        ReadInputRegistersResponse readInputRegistersResponse;

        modbusTransaction.setRequest(readInputRegistersRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException(
                        "Failed to read Analog In. Register: " + register + ". AnaIn: " + anaIn, e);
            }
        }
        readInputRegistersResponse = (net.wimpi.modbus.msg.ReadInputRegistersResponse) modbusTransaction.getResponse();
        return readInputRegistersResponse.getRegisterValue(0);
    }

    @Override
    public void writeOutputRegister(int register, int anaOut, int value) throws IOException {
        if (!isConnected()) {
            throw new MBTControllerException("No MBT Connection open!");
        }

        ModbusTCPTransaction modbusTransaction = new ModbusTCPTransaction(connection);
        Register registerValue = new SimpleRegister(value);
        WriteSingleRegisterRequest writeSingleRegisterRequest = new WriteSingleRegisterRequest(register * 4 + anaOut,
                registerValue);
        WriteSingleRegisterResponse writeSingleRegisterResponse;

        modbusTransaction.setRequest(writeSingleRegisterRequest);

        synchronized (lock) {
            try {
                modbusTransaction.execute();
            } catch (ModbusException e) {
                throw new MBTControllerException("Failed to write Analog Out. Register: " + register + ". AnOut: "
                        + anaOut + ". Value: " + value, e);
            }
        }
        writeSingleRegisterResponse = (WriteSingleRegisterResponse) modbusTransaction.getResponse();
        if (writeSingleRegisterResponse.getRegisterValue() != value) {
            throw new MBTControllerException("Failed to write Analog Out. Register: " + register + ". AnOut: " + anaOut
                    + ". Value: " + value + ". Repsone value is unequal desired value.");
        }
    }

    @Override
    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        return connection.isConnected();
    }

}
