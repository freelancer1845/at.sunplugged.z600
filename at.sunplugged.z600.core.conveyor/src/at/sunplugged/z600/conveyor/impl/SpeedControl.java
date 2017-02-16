package at.sunplugged.z600.conveyor.impl;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.Engine;

public class SpeedControl {

    private final ConveyorControlService conveyorControlService;

    private final LogService logService;

    private final Engine engineOne;

    private final Engine engineTwo;

    private double setPointSpeed = 0;

    private Mode currentMode = Mode.STOP;

    private boolean running = true;

    private Thread controlThread;

    public SpeedControl(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
        this.logService = ConveyorControlServiceImpl.getLogService();
        this.engineOne = conveyorControlService.getEngineOne();
        this.engineTwo = conveyorControlService.getEngineTwo();
        controlThread = new Thread(new ControlRunnable());
        controlThread.setName("Speed Control Thread");

        if (engineOne.isConnected() && engineTwo.isConnected()) {
            controlThread.start();
        } else {
            logService.log(LogService.LOG_ERROR, "SpeedControl not started -- Engines not connected.");
        }
    }

    public void setSetpoint(double speed) {
        if (speed <= 0) {
            currentMode = Mode.STOP;
        }
        setPointSpeed = speed;
    }

    public void setMode(Mode mode) {
        currentMode = mode;

        switch (mode) {
        case LEFT_TO_RIGHT:
            engineOne.setDirection(1);
            engineTwo.setDirection(1);
            engineTwo.setLoose();

            engineOne.setMaximumSpeed(100);
            engineOne.startEngine();
            break;
        case RIGHT_TO_LEFT:
            engineOne.setDirection(0);
            engineTwo.setDirection(0);
            engineOne.setLoose();

            engineTwo.setMaximumSpeed(100);
            engineTwo.startEngine();
            break;
        case STOP:
            engineOne.stopEngine();
            engineTwo.stopEngine();
            break;
        }
    }

    public void deactivate() {
        running = false;
    }

    private class ControlRunnable implements Runnable {

        private long speedCheckTimer = 0;

        @Override
        public void run() {

            running = true;
            logService.log(LogService.LOG_DEBUG, "SpeedControl started.");

            while (running) {
                if (currentMode != Mode.STOP && checkForBandError()) {
                    tick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logService.log(LogService.LOG_ERROR,
                                "Speed Control Thread interrupted during waiting for wake up");
                    }
                }
            }
        }

        private boolean checkForBandError() {
            double currentSpeed = conveyorControlService.getCurrentSpeed();
            if (currentSpeed == 0 && speedCheckTimer == 0) {
                speedCheckTimer = System.currentTimeMillis();
            } else if (currentSpeed == 0) {
                if (System.currentTimeMillis() - speedCheckTimer > 5000) {
                    logService.log(LogService.LOG_ERROR, "Tried to start band but no movement was detected!");
                    currentMode = Mode.STOP;
                    return false;
                }
            } else {
                speedCheckTimer = 0;
            }

            return true;
        }

        private void tick() {
            switch (currentMode) {
            case STOP:
                // Case is unreachable and only for completion
                break;
            case LEFT_TO_RIGHT:
                leftToRightMotion();
                break;
            case RIGHT_TO_LEFT:
                rightToLeftMotion();
                break;
            default:
                logService.log(LogService.LOG_DEBUG, "Unkown Conveyor movement Mode used: " + currentMode.name());
                break;
            }

        }

        private void leftToRightMotion() {
            double currentSpeed = conveyorControlService.getCurrentSpeed();
            int currentDrivingEngineSpeed = engineTwo.getCurrentMaximumSpeed();

            int drivingEngineSpeed = calculateNewEngineSpeed(currentSpeed, currentDrivingEngineSpeed);

            engineTwo.setMaximumSpeed(drivingEngineSpeed);

        }

        private void rightToLeftMotion() {
            double currentSpeed = conveyorControlService.getCurrentSpeed();
            int currentDrivingEngineSpeed = engineOne.getCurrentMaximumSpeed();

            int drivingEngineSpeed = calculateNewEngineSpeed(currentSpeed, currentDrivingEngineSpeed);

            engineOne.setMaximumSpeed(drivingEngineSpeed);
        }

        private int calculateNewEngineSpeed(double currentSpeed, int currentEngineSpeed) {
            if (currentEngineSpeed == 0) {
                return 500;
            }
            if (currentSpeed == 0) {
                return currentEngineSpeed;
            }
            if (Math.abs(currentSpeed - setPointSpeed) > 0.0003) {
                double ratio = setPointSpeed / currentSpeed;
                if (ratio > 1.05) {
                    return (int) (currentEngineSpeed + 100);
                } else if (ratio < 0.95) {
                    return (int) (currentEngineSpeed - 100);
                }

                return (int) (currentEngineSpeed * ratio);
            }

            return currentEngineSpeed;
        }
    }

}
