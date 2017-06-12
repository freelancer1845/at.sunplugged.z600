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

    private static int timeWindowInMs = 100;

    private int triggers = 0;

    private boolean currentState = false;

    private long timeOfSwitchToFalse = 0;

    private boolean countingUp = true;

    public void submitTriggerValue(boolean value, long triggerTime) {
        if (value == true) {
            handleTrue(triggerTime);
        } else {
            handleFalse(triggerTime);
        }
    }

    public void reset() {
        triggers = 0;
        timeOfSwitchToFalse = 0;
    }

    private void handleFalse(long triggerTime) {
        if (currentState == true) {
            currentState = false;
            timeOfSwitchToFalse = triggerTime;
        }
    }

    private void handleTrue(long triggerTime) {
        if (currentState == false) {

            if (triggerTime - timeWindowInMs * 1000000L > timeOfSwitchToFalse) {
                if (countingUp == true) {
                    triggers++;
                } else {
                    triggers--;
                }
                currentState = true;
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

}
