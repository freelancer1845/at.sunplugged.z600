package at.sunplugged.z600.conveyor.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.Engine;
import at.sunplugged.z600.conveyor.api.SpeedLogger;
import at.sunplugged.z600.conveyor.constants.EngineConstants;
import at.sunplugged.z600.conveyor.engine.EngineSerialCom;
import at.sunplugged.z600.conveyor.speedlogging.SpeedLoggerImpl;
import at.sunplugged.z600.mbt.api.MbtService;

@Component(immediate = true)
public class ConveyorControlServiceImpl implements ConveyorControlService {

    private static StandardThreadPoolService threadPoolService;

    private static LogService logService;

    private static MbtService mbtService;

    private EngineSerialCom engineOne;

    private EngineSerialCom engineTwo;

    private SpeedLogger speedLogger;

    @Activate
    protected void activate() {
        engineOne = new EngineSerialCom(EngineConstants.ENGINE_ONE_PORT, 2);
        engineTwo = new EngineSerialCom(EngineConstants.ENGINE_TWO_PORT, 1);
        speedLogger = new SpeedLoggerImpl();
    }

    @Deactivate
    protected void deactivate() {
        ((SpeedLoggerImpl) speedLogger).stopSpeedLogger();
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
        return speedLogger.getCurrentSpeed();
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

    @Reference(unbind = "unbindStandardThreadPoolService", cardinality = ReferenceCardinality.MANDATORY)
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

    @Reference(unbind = "unbindMbtService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindMbtService(MbtService mbtService) {
        ConveyorControlServiceImpl.mbtService = mbtService;
    }

    public synchronized void unbindMbtService(MbtService mbtService) {
        if (ConveyorControlServiceImpl.mbtService.equals(mbtService)) {
            ConveyorControlServiceImpl.mbtService = null;
        }
    }

    public static MbtService getMbtService() {
        return mbtService;
    }

    @Override
    public Engine getEngineOne() {
        return engineOne;
    }

    @Override
    public Engine getEngineTwo() {
        return engineTwo;
    }

    @Override
    public SpeedLogger getSpeedLogger() {
        // TODO Auto-generated method stub
        return null;
    }

}
