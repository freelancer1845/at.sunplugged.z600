package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.Kind;
import at.sunplugged.z600.core.machinestate.constants.Times;
import at.sunplugged.z600.mbt.api.MBTController;

public class OutletControlImpl implements OutletControl {

    private Object lock = new Object();

    private final MBTController mbtController;

    private final LogService logService;

    private Map<DigitalOutput, Boolean> outletState = new HashMap<>();

    private Long lastUpdateTime = System.currentTimeMillis() - Times.TIME_OUTDATED;

    public OutletControlImpl(MBTController mbtController, LogService logService) {
        this.mbtController = mbtController;
        this.logService = logService;
    }

    @Override
    public boolean isOutletOpen(DigitalOutput digitalOutput) throws IOException {
        if (!digitalOutput.getKind().equals(Kind.OUTLET)) {
            logService.log(LogService.LOG_DEBUG,
                    "Used Outlet Control to ask for sth. that is not of kind Outlet. Kind: "
                            + digitalOutput.getKind().toString());
        }

        checkForUpdated(digitalOutput);
        synchronized (lock) {
            return outletState.get(digitalOutput);
        }
    }

    @Override
    public void closeOutlet(DigitalOutput digitalOutput) throws IOException {
        synchronized (lock) {
            mbtController.writeDigOut(digitalOutput.getAddress(), false);
        }
        update(digitalOutput);
    }

    @Override
    public void openOutlet(DigitalOutput digitalOutput) throws IOException {
        synchronized (lock) {
            mbtController.writeDigOut(digitalOutput.getAddress(), true);
        }
        update(digitalOutput);

    }

    private void checkForUpdated(WagoAddresses.DigitalOutput digitalOutput) throws IOException {
        Long now = System.currentTimeMillis();
        if (now - lastUpdateTime > Times.TIME_OUTDATED) {
            update(digitalOutput);
        }
    }

    private void update(WagoAddresses.DigitalOutput digitalOutput) throws IOException {
        synchronized (lock) {
            if (!outletState.containsKey(digitalOutput)) {
                outletState.put(digitalOutput, false);
            }
            int[] minAndMaxAddress = findLowestAndHighestAddress();
            List<Boolean> currentState = mbtController.readDigOuts(minAndMaxAddress[0], minAndMaxAddress[1]);
            for (DigitalOutput output : outletState.keySet()) {
                if (currentState.contains(output.getAddress())) {
                    outletState.put(output, currentState.get(output.getAddress()));
                }
            }
        }
    }

    private int[] findLowestAndHighestAddress() {
        int lowestAddress = 10000;
        int highestAddress = 0;
        for (DigitalOutput output : outletState.keySet()) {
            int address = output.getAddress();
            if (address < lowestAddress) {
                lowestAddress = address;
            } else if (address > highestAddress) {
                highestAddress = address;
            }
        }
        return new int[] { lowestAddress, highestAddress };
    }

}
