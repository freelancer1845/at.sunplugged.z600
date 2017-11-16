package at.sunplugged.z600.srm50.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.osgi.service.log.LogService;

import gnu.io.CommPort;
import gnu.io.NRSerialPort;

public class SrmCommPort {

    private final LogService logService;

    private final CommPort commPort;

    private final NRSerialPort nrCommPort;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final Thread allowedThread;

    public static SrmCommPort createCommPort(LogService logService, CommPort commPort) throws IOException {
        return new SrmCommPort(logService, commPort);
    }

    public static SrmCommPort createCommPort(LogService logService, NRSerialPort commPort) {
        return new SrmCommPort(logService, commPort);
    }

    private SrmCommPort(LogService logService, CommPort commPort) throws IOException {
        this.logService = logService;
        this.commPort = commPort;
        this.inputStream = commPort.getInputStream();
        this.outputStream = commPort.getOutputStream();
        this.allowedThread = Thread.currentThread();

        this.nrCommPort = null;
    }

    private SrmCommPort(LogService logService, NRSerialPort commPort) {
        this.logService = logService;
        this.nrCommPort = commPort;
        this.inputStream = commPort.getInputStream();
        this.outputStream = commPort.getOutputStream();
        this.allowedThread = Thread.currentThread();

        this.commPort = null;
    }

    public String doCommand(String command, boolean repeatIfFailed) throws IOException {
        checkThread();
        if (commPort == null && nrCommPort == null) {
            logService.log(LogService.LOG_ERROR, "Command Issued when there was no Port open.");
            throw new IOException("Command Issued when there was no Port open.");
        }
        byte[] commandArray = new String(command + (char) 13).getBytes();
        outputStream.write(commandArray);
        String answer = "";
        try {
            Thread.sleep(250);

            while (inputStream.available() > 0) {
                answer += (char) inputStream.read();
            }

            if (answer.equals("")) {
                Thread.sleep(50);
                while (inputStream.available() > 0) {
                    answer += (char) inputStream.read();
                }
            }
        } catch (InterruptedException e) {
            logService.log(LogService.LOG_ERROR, "doCommand Interrupted during waiting for answer.", e);
        }

        if (!answer.startsWith(command)) {
            if (repeatIfFailed) {
                doCommand(command, false);
            } else {
                throw new IOException("Answer didn't start with command issued, \"" + answer + "");
            }
        }

        if (command.equals("READ") == false) {
            logService.log(LogService.LOG_DEBUG, "Command executed: " + command);
        }
        return answer.substring(command.length());
    }

    public void close() {
        checkThread();
        if (commPort != null) {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
            }

            commPort.close();
        }
        if (nrCommPort != null) {
            nrCommPort.disconnect();
        }
    }

    private void checkThread() {
        if (!this.allowedThread.equals(Thread.currentThread())) {
            throw new IllegalStateException("Illegal Thread Access...");
        }
    }
}
