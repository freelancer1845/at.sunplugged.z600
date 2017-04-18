package at.sunplugged.z600.conveyor.impl.position;

import java.io.IOException;
import java.util.concurrent.Future;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.mbt.api.MbtService;

public class PositionControl {

    private static PositionControl instance;

    public static PositionControl getInstance() {
        if (instance == null) {
            instance = new PositionControl();
        }
        return instance;
    }

    private final MachineStateService machineStateService;

    private final MbtService mbtService;

    private final ConveyorControlService conveyorControlService;

    private final StandardThreadPoolService threadPool;

    private final LogService logService;

    private Timer leftTimerForward = new Timer();

    private Timer leftTimerBackward = new Timer();

    private Timer rightTimerForward = new Timer();

    private Timer rightTimerBackward = new Timer();

    private Future<?> moveFuture;

    public PositionControl() {
        this.machineStateService = ConveyorPositionCorrectionServiceImpl.getMachineStateService();
        this.mbtService = ConveyorPositionCorrectionServiceImpl.getMbtService();
        this.conveyorControlService = ConveyorPositionCorrectionServiceImpl.getConveyorControlService();
        this.threadPool = ConveyorPositionCorrectionServiceImpl.getThreadPool();
        this.logService = ConveyorPositionCorrectionServiceImpl.getLogService();
    }

    public void tick() throws IOException {

        if (conveyorControlService.getActiveMode() == Mode.LEFT_TO_RIGHT) {
            boolean lightSwitchLeftFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_LEFT_FRONT);
            boolean lightSwitchLeftBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_LEFT_BACK);
            boolean limitSwitchLeftFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_LEFT_FRONT);
            boolean limitSwitchLeftBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_LEFT_BACK);

            if (lightSwitchLeftFront == true && limitSwitchLeftFront == false) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), true);
                leftTimerBackward.start();
            } else if (lightSwitchLeftFront == false || limitSwitchLeftFront == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
                leftTimerBackward.stop();
            }

            if (lightSwitchLeftBack == false && limitSwitchLeftBack == false) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), true);
                leftTimerForward.start();
            } else if (lightSwitchLeftBack == true || limitSwitchLeftBack == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
                leftTimerForward.stop();
            }

        } else if (conveyorControlService.getActiveMode() == Mode.RIGHT_TO_LEFT) {
            boolean lightSwitchRightFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_RIGHT_FRONT);
            boolean lightSwitchRightBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_RIGHT_BACK);
            boolean limitSwitchRightFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_RIGHT_FRONT);
            boolean limitSwitchRightBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_RIGHT_BACK);
            if (lightSwitchRightFront == true && limitSwitchRightFront == false) {
                rightTimerBackward.start();
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), true);
            } else if (lightSwitchRightFront == false || limitSwitchRightBack == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
                rightTimerBackward.stop();
            }
            if (lightSwitchRightBack == false && limitSwitchRightBack == false) {
                rightTimerForward.start();
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), true);
            } else if (lightSwitchRightBack == true || limitSwitchRightBack == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
                rightTimerForward.stop();
            }
        } else {
            leftTimerForward.stop();
            leftTimerBackward.stop();
            rightTimerForward.stop();
            rightTimerForward.stop();

            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
        }
    }

    public long getRuntimeRight() {
        return rightTimerForward.getCurrentValue() - rightTimerBackward.getCurrentValue();
    }

    public void setRuntimeRight(long value) {
        if (value >= 0) {
            rightTimerForward.setCurrentValue(value);
            rightTimerBackward.setCurrentValue(0);
        } else {
            rightTimerForward.setCurrentValue(0);
            rightTimerBackward.setCurrentValue(value);
        }
    }

    public long getRuntimeLeft() {
        return leftTimerForward.getCurrentValue() - leftTimerBackward.getCurrentValue();
    }

    public void setRuntimeLeft(long value) {
        if (value >= 0) {
            leftTimerForward.setCurrentValue(value);
            leftTimerBackward.setCurrentValue(0);
        } else {
            leftTimerForward.setCurrentValue(0);
            leftTimerBackward.setCurrentValue(value);
        }
    }

    public void stopTimer() {
        leftTimerForward.stop();
        leftTimerBackward.stop();
        rightTimerBackward.stop();
        rightTimerForward.stop();

    }

    public void startLeftMoveForward() {
        if (moveFuture == null || moveFuture.isDone()) {
            moveFuture = threadPool.submit(() -> {
                try {
                    mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), true);
                    leftTimerForward.start();
                    while (Thread.interrupted() == false) {
                        Thread.sleep(100);
                        if (machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_LEFT_FRONT)) {
                            throw new InterruptedException();
                        }
                    }
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "IO error in leftStartMoveForward...", e);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "LeftMoveFoward stopped.");
                } finally {
                    try {
                        mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
                        leftTimerForward.stop();
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "IO error in leftStartMoveForward...", e);
                    }
                }

            });
        } else {
            logService.log(LogService.LOG_ERROR, "Already moving...");
        }
    }

    public void startLeftMoveBackward() {
        if (moveFuture == null || moveFuture.isDone()) {
            moveFuture = threadPool.submit(() -> {
                try {
                    mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), true);
                    leftTimerBackward.start();
                    while (Thread.interrupted() == false) {
                        Thread.sleep(100);
                        if (machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_LEFT_BACK)) {
                            throw new InterruptedException();
                        }
                    }
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "IO error in leftStartMoveBackward...", e);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "LeftMoveFoward stopped.");
                } finally {
                    try {
                        mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
                        leftTimerBackward.stop();
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "IO error in leftStartMoveBackward...", e);
                    }
                }

            });
        } else {
            logService.log(LogService.LOG_ERROR, "Already moving...");
        }
    }

    public void startRightMoveForward() {
        if (moveFuture == null || moveFuture.isDone()) {
            moveFuture = threadPool.submit(() -> {
                try {
                    mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), true);
                    rightTimerForward.start();
                    while (Thread.interrupted() == false) {
                        Thread.sleep(100);
                        if (machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_RIGHT_FRONT)) {
                            throw new InterruptedException();
                        }
                    }
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "IO error in rightStartMoveForward...", e);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "LeftMoveFoward stopped.");
                } finally {
                    try {
                        mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
                        rightTimerForward.stop();
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "IO error in rightStartMoveForward...", e);
                    }
                }

            });
        } else {
            logService.log(LogService.LOG_ERROR, "Already moving...");
        }
    }

    public void startRightMoveBackward() {
        if (moveFuture == null || moveFuture.isDone()) {
            moveFuture = threadPool.submit(() -> {
                try {
                    mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), true);
                    rightTimerBackward.start();
                    while (Thread.interrupted() == false) {
                        Thread.sleep(100);
                        if (machineStateService.getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_RIGHT_BACK)) {
                            throw new InterruptedException();
                        }
                    }
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "IO error in rightStartMoveBackward...", e);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "LeftMoveFoward stopped.");
                } finally {
                    try {
                        mbtService.writeDigOut(WagoAddresses.DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
                        rightTimerBackward.stop();
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "IO error in rightStartMoveBackward...", e);
                    }
                }

            });
        } else {
            logService.log(LogService.LOG_ERROR, "Already moving...");
        }
    }

    public void stopManualMove() {
        if (moveFuture != null && moveFuture.isDone() == false) {
            moveFuture.cancel(true);
        }
    }

    private class Timer {

        private long startTime = 0;

        private long runtime = 0;

        public void start() {
            stop();
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
        }

        public void stop() {
            if (startTime != 0) {
                runtime += (System.currentTimeMillis() - startTime);
                startTime = 0;
            }
        }

        public long getCurrentValue() {
            if (startTime != 0) {
                return runtime + System.currentTimeMillis() - startTime;
            }
            return runtime;
        }

        public void setCurrentValue(long value) {
            runtime = value;
        }

    }

}
