package at.sunplugged.z600.mbt.impl;

import java.io.IOException;
import java.net.InetAddress;

import at.sunplugged.z600.mbt.api.MBTController;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

public class MBTControllerImpl implements MBTController {

    private TCPMasterConnection con; // the connection
    private ModbusTCPTransaction trans; // the transaction
    private ReadInputDiscretesRequest req; // the request
    private ReadInputDiscretesResponse res; // the response

    /* Variables for storing the parameters */
    private InetAddress addr = null; // the slave's address
    private int port = Modbus.DEFAULT_PORT;
    private int ref = 0; // the reference; offset where to start reading from
    private int count = 0; // the number of DI's to read
    private int repeat = 1; // a loop for repeating the transaction

    @Override
    public void connect(String address) throws IOException {
        addr = InetAddress.getByName(address);
        con = new TCPMasterConnection(addr);
        con.setPort(port);
        System.out.println("MBT Connection test done");

    }

    @Override
    public void disconnect() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeDigOut(int digOut, boolean value) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean readDigOut(int digOut) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean readDigIn(int digIn) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

}
