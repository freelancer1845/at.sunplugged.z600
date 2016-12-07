package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.backend.dataservice.api.VariableIdentifiers;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.mbt.api.MBTController;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MachineStateServiceImpl implements MachineStateService {

    private DataService dataService;

    private LogService logService;

    private MBTController mbtController;

    private SrmCommunicator srmCommunicator;

    private StandardThreadPoolService standardThreadPoolService;

    private final OutletControl outletControl;

    private final PumpControl pumpControl;

    public MachineStateServiceImpl() {
        this.outletControl = new OutletControlImpl(mbtController, logService);
        this.pumpControl = new PumpControlImpl(mbtController, logService, standardThreadPoolService);
    }

    @Override
    public void update(Date snapShotDate) {

        try {
            getSrmData(snapShotDate);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed getting SrmData", e);
        }

        getMBTUpdate(snapShotDate);
    }

    private void getSrmData(Date snapShotDate) throws IOException {
        List<Double> data = srmCommunicator.readChannels();
        for (int i = 0; i < data.size(); i++) {
            try {
                dataService.saveData(VariableIdentifiers.SRM_CHANNEL + (i + 1), snapShotDate, data.get(i));
            } catch (DataServiceException e) {
                logService.log(LogService.LOG_ERROR, "Error during acquiring SRM Data for Channel: " + i, e);
            }
        }
    }

    private void getMBTUpdate(Date snapShotDate) {

    }

    @Reference(unbind = "unsetDataService")
    public synchronized void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public synchronized void unsetDataService(DataService dataService) {
        if (this.dataService == dataService) {
            this.dataService = null;
        }
    }

    @Reference(unbind = "unsetLogService")
    public synchronized void setLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    @Reference(unbind = "unsetMBTController")
    public synchronized void setMBTController(MBTController mbtController) {
        this.mbtController = mbtController;
    }

    public synchronized void unsetMBTController(MBTController mbtController) {
        if (this.mbtController == mbtController) {
            this.mbtController = null;
        }
    }

    @Reference(unbind = "unsetSrmCommunicator")
    public synchronized void setSrmCommunicator(SrmCommunicator srmCommunicator) {
        this.srmCommunicator = srmCommunicator;
    }

    public synchronized void unsetSrmCommunicator(SrmCommunicator srmCommunicator) {
        if (this.srmCommunicator == srmCommunicator) {
            this.srmCommunicator = null;
        }
    }

    @Reference(unbind = "unsetStandardThreadPoolService")
    public synchronized void setStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        this.standardThreadPoolService = standardThreadPoolService;
    }

    public synchronized void unsetStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        if (this.standardThreadPoolService == standardThreadPoolService) {
            this.standardThreadPoolService = null;
        }
    }

    @Override
    public double getPressureValue(int at) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public PumpControl getPumpControl() {
        return pumpControl;
    }

    @Override
    public OutletControl getOutletControl() {
        return outletControl;
    }

}
