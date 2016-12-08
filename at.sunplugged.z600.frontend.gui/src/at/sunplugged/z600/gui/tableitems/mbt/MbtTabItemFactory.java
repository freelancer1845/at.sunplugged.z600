package at.sunplugged.z600.gui.tableitems.mbt;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.mbt.api.MBTController;

public class MbtTabItemFactory {

    private final MBTController mbtController;

    private final LogService logService;

    private final MachineStateService machineStateService;
    // Debug TabItem
    private Combo comboDigitalOutput;
    private Text textReadCoilValue;
    private Combo comboReadAnalogInput;
    private Text textReadAnalogInput;
    private Combo comboReadDigitalInput;
    private Text textReadDiscreteInputValue;
    private Combo comboWriteDigitalOutput;
    private Text textWriteCoilValue;
    private Text textWriteRegisterAddress;
    private Text textWriteRegisterValue;
    private Text textIPAddress;
    private Text textConnectionState;

    public MbtTabItemFactory(MBTController mbtController, LogService logService,
            MachineStateService machineStateService) {
        this.mbtController = mbtController;
        this.logService = logService;
        this.machineStateService = machineStateService;
    }

    /**
     * @wbp.parser.entryPoint
     */
    public TabItem createDebugMbtTabItem(TabFolder parent, int style) {

        TabItem tbtmMbtDebug = new TabItem(parent, SWT.NONE);
        tbtmMbtDebug.setText("MBT Debug");

        Composite mbtDebugComposite = new Composite(parent, SWT.NONE);
        tbtmMbtDebug.setControl(mbtDebugComposite);
        mbtDebugComposite.setLayout(new GridLayout(1, false));

        Group groupConnect = new Group(mbtDebugComposite, SWT.NONE);
        groupConnect.setText("Connect");
        groupConnect.setLayout(new GridLayout(3, false));

        textIPAddress = new Text(groupConnect, SWT.BORDER);
        textIPAddress.setText("localhost");
        GridData gdTextIpAddress = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdTextIpAddress.minimumWidth = 200;
        textIPAddress.setLayoutData(gdTextIpAddress);

        Button buttonConnect = new Button(groupConnect, SWT.NONE);
        GridData gdButtonConnect = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdButtonConnect.widthHint = 75;
        buttonConnect.setLayoutData(gdButtonConnect);
        buttonConnect.setText("Connect");
        buttonConnect.setSelection(false);
        buttonConnect.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            mbtController.connect(textIPAddress.getText());
                        } catch (IOException e1) {
                            logService.log(LogService.LOG_WARNING, e1.getMessage());
                        }
                        if (mbtController.isConnected()) {
                            textConnectionState.setText("Connected");
                        } else {
                            textConnectionState.setText("Not Connected");
                        }
                    }

                });

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        textConnectionState = new Text(groupConnect, SWT.BORDER);
        textConnectionState.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
        textConnectionState.setText("Value");
        textConnectionState.setEditable(false);
        GridData gdTextConnectionState = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdTextConnectionState.widthHint = 75;
        textConnectionState.setLayoutData(gdTextConnectionState);

        Group grpReadCoil = new Group(mbtDebugComposite, SWT.NONE);
        grpReadCoil.setText("Read Digital Output");
        grpReadCoil.setLayout(new GridLayout(3, false));

        comboDigitalOutput = new Combo(grpReadCoil, SWT.NONE);
        DigitalOutput[] digOuts = WagoAddresses.DigitalOutput.values();
        String[] digOutsString = new String[digOuts.length];
        for (int i = 0; i < digOuts.length; i++) {
            digOutsString[i] = digOuts[i].toString();
        }
        comboDigitalOutput.setItems(digOutsString);
        GridData gdComboDigitalOutput = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdComboDigitalOutput.minimumWidth = 200;
        comboDigitalOutput.setLayoutData(gdComboDigitalOutput);
        comboDigitalOutput.setText("Choose output to check... ");

        Button btnReadCoil = new Button(grpReadCoil, SWT.NONE);
        GridData gd_btnReadCoil = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnReadCoil.widthHint = 75;
        btnReadCoil.setLayoutData(gd_btnReadCoil);
        btnReadCoil.setText("Read");
        btnReadCoil.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean answer = machineStateService.getDigitalOutputState()
                        .get(DigitalOutput.values()[comboDigitalOutput.getSelectionIndex()].getAddress());
                textReadCoilValue.setText(String.valueOf(answer));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        textReadCoilValue = new Text(grpReadCoil, SWT.BORDER);
        textReadCoilValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
        textReadCoilValue.setEditable(false);
        textReadCoilValue.setText("Value");
        GridData gd_textReadCoilValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_textReadCoilValue.widthHint = 75;
        textReadCoilValue.setLayoutData(gd_textReadCoilValue);

        Group groupWriteDigitalOutput = new Group(mbtDebugComposite, SWT.NONE);
        groupWriteDigitalOutput.setText("Write Digital Output");
        groupWriteDigitalOutput.setLayout(new GridLayout(3, false));

        comboWriteDigitalOutput = new Combo(groupWriteDigitalOutput, SWT.NONE);
        comboWriteDigitalOutput.setText("Choose Digital Output");
        comboWriteDigitalOutput.setItems(digOutsString);
        GridData gdTextWriteCoilAddress = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdTextWriteCoilAddress.minimumWidth = 200;
        comboWriteDigitalOutput.setLayoutData(gdTextWriteCoilAddress);

        Button buttonWriteDigitalOutput = new Button(groupWriteDigitalOutput, SWT.NONE);
        GridData gdButtonWriteDigitalOutput = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdButtonWriteDigitalOutput.widthHint = 75;
        buttonWriteDigitalOutput.setLayoutData(gdButtonWriteDigitalOutput);
        buttonWriteDigitalOutput.setText("Write");
        buttonWriteDigitalOutput.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    mbtController.writeDigOut(digOuts[comboWriteDigitalOutput.getSelectionIndex()].getAddress(),
                            Boolean.valueOf(textWriteCoilValue.getText()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        textWriteCoilValue = new Text(groupWriteDigitalOutput, SWT.BORDER);
        textWriteCoilValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        textWriteCoilValue.setText("Value");
        textWriteCoilValue.setEditable(true);
        GridData gdTextWriteCoilValue = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gdTextWriteCoilValue.widthHint = 75;
        textWriteCoilValue.setLayoutData(gdTextWriteCoilValue);

        Group grpReadDigitalInput = new Group(mbtDebugComposite, SWT.NONE);
        grpReadDigitalInput.setText("Read Digital Input");
        grpReadDigitalInput.setLayout(new GridLayout(3, false));

        comboReadDigitalInput = new Combo(grpReadDigitalInput, SWT.NONE);
        comboReadDigitalInput.setText("Choose Digital Input");
        GridData gdComboReadDigitalInput = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdComboReadDigitalInput.minimumWidth = 200;
        comboReadDigitalInput.setLayoutData(gdComboReadDigitalInput);
        DigitalInput[] digIns = WagoAddresses.DigitalInput.values();
        String[] digInsString = new String[digIns.length];
        for (int i = 0; i < digInsString.length; i++) {
            digInsString[i] = digIns[i].name();
        }
        comboReadDigitalInput.setItems(digInsString);

        Button btnReadDiscreteInput = new Button(grpReadDigitalInput, SWT.NONE);
        GridData gdbtnReadDiscreteInput = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdbtnReadDiscreteInput.widthHint = 75;
        btnReadDiscreteInput.setLayoutData(gdbtnReadDiscreteInput);
        btnReadDiscreteInput.setText("Read");
        btnReadDiscreteInput.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean answer = machineStateService.getDigitalInputState()
                        .get(digIns[comboReadDigitalInput.getSelectionIndex()].getAddress());
                textReadDiscreteInputValue.setText(String.valueOf(answer));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        textReadDiscreteInputValue = new Text(grpReadDigitalInput, SWT.BORDER);
        textReadDiscreteInputValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
        textReadDiscreteInputValue.setEditable(false);
        textReadDiscreteInputValue.setText("Value");
        GridData gdTextReadDiscreteInputValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdTextReadDiscreteInputValue.widthHint = 75;
        textReadDiscreteInputValue.setLayoutData(gdTextReadDiscreteInputValue);

        Group groupReadAnalogInput = new Group(mbtDebugComposite, SWT.NONE);
        groupReadAnalogInput.setText("Read Analog Input");
        groupReadAnalogInput.setLayout(new GridLayout(3, false));

        comboReadAnalogInput = new Combo(groupReadAnalogInput, SWT.NONE);
        comboReadAnalogInput.setText("Choose Analog Input...");
        GridData gdReadAnalogInput = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdReadAnalogInput.minimumWidth = 200;
        comboReadAnalogInput.setLayoutData(gdReadAnalogInput);
        AnalogInput[] anaIns = WagoAddresses.AnalogInput.values();
        String[] anaInsString = new String[anaIns.length];
        for (int i = 0; i < anaIns.length; i++) {
            anaInsString[i] = anaIns[i].name();
        }
        comboReadAnalogInput.setItems(anaInsString);

        Button buttonReadAnalogInput = new Button(groupReadAnalogInput, SWT.NONE);
        GridData gdButtonReadAnalogInput = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdButtonReadAnalogInput.widthHint = 75;
        buttonReadAnalogInput.setLayoutData(gdButtonReadAnalogInput);
        buttonReadAnalogInput.setText("Read");
        buttonReadAnalogInput.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int answer = machineStateService.getAnalogInputState()
                        .get(anaIns[comboReadAnalogInput.getSelectionIndex()].getAddress());
                textReadAnalogInput.setText(String.valueOf(answer));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        textReadAnalogInput = new Text(groupReadAnalogInput, SWT.BORDER);
        textReadAnalogInput.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
        textReadAnalogInput.setText("Value");
        textReadAnalogInput.setEditable(false);
        GridData gdTextReadAnalogInput = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gdTextReadAnalogInput.widthHint = 75;
        textReadAnalogInput.setLayoutData(gdTextReadAnalogInput);

        Group groupWriteRegister = new Group(mbtDebugComposite, SWT.NONE);
        groupWriteRegister.setText("Write Register");
        groupWriteRegister.setLayout(new GridLayout(3, false));

        textWriteRegisterAddress = new Text(groupWriteRegister, SWT.BORDER);
        textWriteRegisterAddress.setText("Address");
        GridData gdTextWriteRegisterAddress = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdTextWriteRegisterAddress.minimumWidth = 200;
        textWriteRegisterAddress.setLayoutData(gdTextWriteRegisterAddress);

        Button buttonWriteRegisterAddress = new Button(groupWriteRegister, SWT.NONE);
        GridData gd_button_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_button_1.widthHint = 75;
        buttonWriteRegisterAddress.setLayoutData(gd_button_1);
        buttonWriteRegisterAddress.setText("Write");
        buttonWriteRegisterAddress.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    mbtController.writeOutputRegister(Integer.valueOf(textWriteRegisterAddress.getText()),
                            Integer.valueOf(textWriteRegisterValue.getText()));
                } catch (NumberFormatException | IOException e1) {
                    logService.log(LogService.LOG_ERROR, "Error writing register: " + e1.getMessage());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        textWriteRegisterValue = new Text(groupWriteRegister, SWT.BORDER);
        textWriteRegisterValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        textWriteRegisterValue.setText("Value");
        textWriteRegisterValue.setEditable(true);
        GridData gdTextWriteRegisterValue = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gdTextWriteRegisterValue.widthHint = 75;
        textWriteRegisterValue.setLayoutData(gdTextWriteRegisterValue);

        Group grpLogOutput = new Group(mbtDebugComposite, SWT.NONE);
        grpLogOutput.setLayout(new GridLayout(1, false));
        grpLogOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpLogOutput.setText("Log Output");

        StyledText styledTextLogOutput = new StyledText(grpLogOutput,
                SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        styledTextLogOutput.setDoubleClickEnabled(false);
        styledTextLogOutput.setEditable(false);
        styledTextLogOutput.setText("Console Output");
        styledTextLogOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                styledTextLogOutput.append(String.valueOf((char) b));
            }
        };
        // System.setOut(new PrintStream(outputStream));
        return tbtmMbtDebug;
    }

}
