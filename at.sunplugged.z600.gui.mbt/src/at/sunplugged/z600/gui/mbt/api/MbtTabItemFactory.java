package at.sunplugged.z600.gui.mbt.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.gui.mbt.impl.MbtCheckButton;
import at.sunplugged.z600.mbt.api.MBTController;

public class MbtTabItemFactory {

    private final MBTController mbtController;

    private final LogService logService;
    // Debug TabItem
    private Text textReadCoilAddress;
    private Text textReadCoilValue;
    private Text textReadRegisterAddress;
    private Text textReadRegisterValue;
    private Text textWriteCoilAddress;
    private Text textWriteCoilValue;
    private Text textWriteRegisterAddress;
    private Text textWriteRegisterValue;
    private Text textIPAddress;
    private Text textConnectionState;

    public MbtTabItemFactory(MBTController mbtController, LogService logService) {
        this.mbtController = mbtController;
        this.logService = logService;
    }

    public TabItem createMbtTabItem(TabFolder tabFolder, int style) {
        TabItem tbtmMbt = new TabItem(tabFolder, style);
        tbtmMbt.setText("MBT");

        Composite composite_1 = new Composite(tabFolder, SWT.BORDER);
        tbtmMbt.setControl(composite_1);
        GridLayout gl_composite_1 = new GridLayout(6, false);
        gl_composite_1.marginWidth = 20;
        gl_composite_1.marginHeight = 20;
        composite_1.setLayout(gl_composite_1);

        Label lblMbtDebugControl = new Label(composite_1, SWT.BORDER);
        GridData gd_lblMbtDebugControl = new GridData(SWT.LEFT, SWT.CENTER, true, false, 6, 1);
        gd_lblMbtDebugControl.horizontalIndent = 20;
        gd_lblMbtDebugControl.verticalIndent = 20;
        lblMbtDebugControl.setLayoutData(gd_lblMbtDebugControl);
        lblMbtDebugControl.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
        lblMbtDebugControl.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        lblMbtDebugControl.setText("MBT Debug Control");

        return tbtmMbt;
    }

    private void createButtons(Composite parent) {
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j < 7; j++) {
                new MbtCheckButton(parent, "Out:" + i + ":" + j) {

                    @Override
                    protected void buttonSpecificWidgetSelected(SelectionEvent e) {
                        System.out.println("Selection Changed: " + "Out:");
                    }

                };
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 7; j++) {
                new MbtCheckButton(parent, "In:" + i + ":" + j) {

                    @Override
                    protected void buttonSpecificWidgetSelected(SelectionEvent e) {
                        System.out.println("Selection Changed: " + "In:");

                    }
                };
            }
        }

    }

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
        textIPAddress.setText("Host Address");
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
        grpReadCoil.setText("Read Coil");
        grpReadCoil.setLayout(new GridLayout(3, false));

        textReadCoilAddress = new Text(grpReadCoil, SWT.BORDER);
        textReadCoilAddress.setText("Address");
        GridData gd_textReadCoilAddress = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_textReadCoilAddress.minimumWidth = 200;
        textReadCoilAddress.setLayoutData(gd_textReadCoilAddress);

        Button btnReadCoil = new Button(grpReadCoil, SWT.NONE);
        GridData gd_btnReadCoil = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnReadCoil.widthHint = 75;
        btnReadCoil.setLayoutData(gd_btnReadCoil);
        btnReadCoil.setText("Read");

        textReadCoilValue = new Text(grpReadCoil, SWT.BORDER);
        textReadCoilValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
        textReadCoilValue.setEditable(false);
        textReadCoilValue.setText("Value");
        GridData gd_textReadCoilValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_textReadCoilValue.widthHint = 75;
        textReadCoilValue.setLayoutData(gd_textReadCoilValue);

        Group groupWriteCoil = new Group(mbtDebugComposite, SWT.NONE);
        groupWriteCoil.setText("Write Coil");
        groupWriteCoil.setLayout(new GridLayout(3, false));

        textWriteCoilAddress = new Text(groupWriteCoil, SWT.BORDER);
        textWriteCoilAddress.setText("Address");
        GridData gdTextWriteCoilAddress = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdTextWriteCoilAddress.minimumWidth = 200;
        textWriteCoilAddress.setLayoutData(gdTextWriteCoilAddress);

        Button buttonWriteCoil = new Button(groupWriteCoil, SWT.NONE);
        GridData gdButtonWriteCoil = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdButtonWriteCoil.widthHint = 75;
        buttonWriteCoil.setLayoutData(gdButtonWriteCoil);
        buttonWriteCoil.setText("Write");

        textWriteCoilValue = new Text(groupWriteCoil, SWT.BORDER);
        textWriteCoilValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        textWriteCoilValue.setText("Value");
        textWriteCoilValue.setEditable(false);
        GridData gdTextWriteCoilValue = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gdTextWriteCoilValue.widthHint = 75;
        textWriteCoilValue.setLayoutData(gdTextWriteCoilValue);

        Group groupReadRegister = new Group(mbtDebugComposite, SWT.NONE);
        groupReadRegister.setText("Read Register");
        groupReadRegister.setLayout(new GridLayout(3, false));

        textReadRegisterAddress = new Text(groupReadRegister, SWT.BORDER);
        textReadRegisterAddress.setText("Address");
        GridData gdReadRegisterAddress = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdReadRegisterAddress.minimumWidth = 200;
        textReadRegisterAddress.setLayoutData(gdReadRegisterAddress);

        Button buttonReadRegister = new Button(groupReadRegister, SWT.NONE);
        GridData gdButtonReadRegister = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdButtonReadRegister.widthHint = 75;
        buttonReadRegister.setLayoutData(gdButtonReadRegister);
        buttonReadRegister.setText("Read");

        textReadRegisterValue = new Text(groupReadRegister, SWT.BORDER);
        textReadRegisterValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
        textReadRegisterValue.setText("Value");
        textReadRegisterValue.setEditable(false);
        GridData gdTextReadRegisterValue = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gdTextReadRegisterValue.widthHint = 75;
        textReadRegisterValue.setLayoutData(gdTextReadRegisterValue);

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

        textWriteRegisterValue = new Text(groupWriteRegister, SWT.BORDER);
        textWriteRegisterValue.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        textWriteRegisterValue.setText("Value");
        textWriteRegisterValue.setEditable(false);
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
        System.setOut(new PrintStream(outputStream));
        return tbtmMbtDebug;
    }

}
