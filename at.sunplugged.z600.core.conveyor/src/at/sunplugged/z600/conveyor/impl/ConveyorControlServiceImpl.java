package at.sunplugged.z600.conveyor.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.engine.EngineSerialCom;

@Component
public class ConveyorControlServiceImpl implements ConveyorControlService {

    private static StandardThreadPoolService threadPoolService;

    private static LogService logService;

    private EngineSerialCom engineOne;

    private EngineSerialCom engineTwo;

    @Activate
    public synchronized void activate() {
        engineOne = new EngineSerialCom("COM5");
        engineTwo = new EngineSerialCom("COM6");
    }

    @Override
    public void start(double speed, Mode direction) {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public double getCurrentSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getSetpointSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Mode getActiveMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        ConveyorControlServiceImpl.threadPoolService = standardThreadPoolService;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        if (ConveyorControlServiceImpl.threadPoolService == standardThreadPoolService) {
            ConveyorControlServiceImpl.threadPoolService = null;
        }
    }

    public static StandardThreadPoolService getStandardThreadPoolService() {
        return ConveyorControlServiceImpl.threadPoolService;
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService logService) {
        ConveyorControlServiceImpl.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (ConveyorControlServiceImpl.logService == logService) {
            ConveyorControlServiceImpl.logService = null;
        }
    }

    public static LogService getLogService() {
        return logService;
    }

    @Override
    public Engine getEngineOne() {
        return engineOne;
    }

    @Override
    public Engine getEngineTwo() {
        return engineTwo;
    }

}
