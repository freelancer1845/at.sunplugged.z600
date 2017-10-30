package at.sunplugged.z600.srm50.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.Events;
import at.sunplugged.z600.srm50.api.Commands;

public class SrmDataAquisitionRunnable implements Runnable {

    private final LogService logService;

    private final SettingsService settings;

    private final EventAdmin eventAdmin;

    private String commPortName;

    private SrmCommPort srmPort;

    private Pattern measurementPattern = Pattern.compile("[0-9\\.]+");

    private List<Double> currentChannelValues = new ArrayList<>();

    private ConcurrentLinkedDeque<AsyncCommand> commandQueue = new ConcurrentLinkedDeque<>();

    private enum State {
        DISCONNECTED, EXECUTING_COMMANDS, ACQUIERING_DATA, CONNECTED, RUNNING
    };

    private EnumSet<State> state = EnumSet.of(State.DISCONNECTED);

    public SrmDataAquisitionRunnable(LogService logService, SettingsService settingsService, EventAdmin eventAdmin) {
        this.logService = logService;
        this.settings = settingsService;
        this.eventAdmin = eventAdmin;
    }

    public void stop() {
        state.remove(State.RUNNING);
    }

    public Future<String> queueCommand(String command) {
        AsyncCommand asyncCommand = new AsyncCommand(command);
        commandQueue.add(asyncCommand);
        return asyncCommand;
    }

    @Override
    public void run() {

        Thread.currentThread().setName("Srm Data Aquisiton Thread");
        state.add(State.RUNNING);

        try {
            long startTime;
            long endTime;

            while (state.contains(State.RUNNING)) {
                startTime = System.currentTimeMillis();
                if (state.contains(State.CONNECTED)) {
                    try {
                        mainLoop();
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "SrmDataAquisition Failed. Current State: \""
                                + Arrays.toString(state.toArray()) + "\".", e);
                        state.remove(State.ACQUIERING_DATA);
                        state.remove(State.EXECUTING_COMMANDS);
                        disconnect();
                    }
                } else if (state.contains(State.DISCONNECTED)) {
                    reconnectLoop();
                }

                endTime = System.currentTimeMillis();
                if ((endTime - startTime) < 1000 && (endTime - startTime) > 0) {
                    Thread.sleep(endTime - startTime);
                }

            }
        } catch (InterruptedException e) {
            logService.log(LogService.LOG_WARNING, "Srm Data Thread interuppted!");
        }

    }

    private void mainLoop() throws IOException {
        if (commandQueue.isEmpty() == false) {
            state.add(State.EXECUTING_COMMANDS);
            AsyncCommand command;

            while ((command = commandQueue.poll()) != null) {
                command.run();
            }
            state.remove(State.EXECUTING_COMMANDS);
        }
        state.add(State.ACQUIERING_DATA);
        updateData();
        state.remove(State.ACQUIERING_DATA);
    }

    private void updateData() throws IOException {
        String answer = srmPort.doCommand(Commands.MEASUREMENT, false);
        Matcher matcher = measurementPattern.matcher(answer);
        List<Double> newChannelValues = new ArrayList<>();
        while (matcher.find()) {
            String currentValue = matcher.group();
            newChannelValues.add(Double.valueOf(currentValue));
        }
        synchronized (currentChannelValues) {
            currentChannelValues.clear();
            currentChannelValues.addAll(newChannelValues);
        }
    }

    public List<Double> getData() {
        if (state.contains(State.CONNECTED)) {
            if (currentChannelValues.isEmpty()) {
                return null;
            }
            synchronized (currentChannelValues) {
                return Collections.unmodifiableList(currentChannelValues);
            }
        } else {
            return null;
        }

    }

    private void disconnect() {
        if (srmPort != null) {
            srmPort.close();
            postConnectEvent(false, null);
        }
        state.remove(State.CONNECTED);
        state.add(State.DISCONNECTED);
    }

    private void reconnectLoop() throws InterruptedException {
        commPortName = settings.getProperty(NetworkComIds.SRM_COM_PORT);
        logService.log(LogService.LOG_DEBUG, "Trying to connect to SRM on Port: \"" + commPortName + "\"...");
        try {
            srmPort = SrmPortManager.getCommPort(commPortName, logService);
            postConnectEvent(true, null);
            state.add(State.CONNECTED);
            state.remove(State.DISCONNECTED);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Reconnecting failed. Trying again in 5s");
            Thread.sleep(5000);
            postConnectEvent(false, e);
        }

    }

    private void postConnectEvent(boolean successful, Throwable e) {

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("success", successful);
        if (e != null) {
            properties.put("Error", e);
        }
        eventAdmin.postEvent(new Event(Events.SRM_CONNECT_EVENT, properties));
    }

    private final class AsyncCommand extends FutureTask<String> {

        public AsyncCommand(String command) {
            super(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return srmPort.doCommand(command, true);
                }

            });
        }

    }

}
