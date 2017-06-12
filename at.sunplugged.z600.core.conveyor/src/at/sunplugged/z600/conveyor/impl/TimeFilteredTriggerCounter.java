package at.sunplugged.z600.conveyor.impl;

import org.osgi.service.log.LogService;

/**
 * 
 * Filters False trigger events that are fast than specified timeWindow (default
 * 1000ms).
 * 
 * @author Jascha Riedel
 *
 */
public class TimeFilteredTriggerCounter {

    public static void setTimeWIndowInMs(int ms) {
        timeWindowInMs = ms;
    }

    private static int timeWindowInMs = 250;

    private int triggers = 0;

    private boolean currentState = false;

    private long timeOfSwitchToFalse = 0;

    private boolean countingUp = true;

    public void submitTriggerValue(boolean value) {
        if (value == true) {
            handleTrue();
        } else {
            handleFalse();
        }
    }

    public void reset() {
        triggers = 0;
        timeOfSwitchToFalse = 0;
    }

    private void handleFalse() {
        if (currentState == true) {
            currentState = false;
            timeOfSwitchToFalse = System.nanoTime();
        }
    }

    private void handleTrue() {
        if (currentState == false) {
            currentState = true;
            if (System.nanoTime() - timeWindowInMs * 1000000L > timeOfSwitchToFalse) {
                if (countingUp == true) {
                    triggers++;
                } else {
                    triggers--;
                }

            } else {
                ConveyorControlServiceImpl.getLogService().log(LogService.LOG_DEBUG,
                        "Trigger switched faster than " + timeWindowInMs + " ms. Ignoring event.");
            }
        }
    }

    public void setCountDirectionUp() {
        countingUp = true;
    }

    public void setCountDirectionDown() {
        countingUp = false;
    }

    public int getTriggers() {
        return triggers;
    }

    public void setTimeWindowInMs(int timeWindowInMs) {
        this.timeWindowInMs = timeWindowInMs;
    }
}
