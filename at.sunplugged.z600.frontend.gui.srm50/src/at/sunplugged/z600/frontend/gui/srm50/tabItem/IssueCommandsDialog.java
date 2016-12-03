package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class IssueCommandsDialog extends TitleAreaDialog {
    private Text text;

    private StyledText styledText;

    private SrmCommunicator srmCommunicator;

    private LogService logService;

    private Shell parentShell;

    private Button sendButton;

    protected IssueCommandsDialog(SrmTabItemFactory srmTabItemFactory, Shell parentShell) {
        super(parentShell);
        this.parentShell = parentShell;
        srmCommunicator = srmTabItemFactory.getSrmCommunicator();
        if (srmCommunicator == null) {
            this.close();
        }
        this.logService = srmTabItemFactory.getLogService();
    }

    @Override
    public void create() {
        super.create();
        setTitle("SRM Command Interface");
        setMessage("Issue Commands directly to the SRM");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // TODO Auto-generated method stub

        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        text = new Text(container, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        text.addModifyListener(new ModifyListener() {
            private Pattern commandPattern = Pattern.compile("[a-z]+");

            @Override
            public void modifyText(ModifyEvent e) {
                Matcher matcher = commandPattern.matcher(text.getText());
                if (matcher.find()) {
                    sendButton.setEnabled(false);
                    text.setToolTipText("Only upper case characters are allowed.");
                } else {
                    sendButton.setEnabled(true);
                    text.setToolTipText("");
                }
            }
        });

        sendButton = new Button(container, SWT.PUSH);
        sendButton.setText("Send Command");
        sendButton.addSelectionListener(new SendCommandListener());

        styledText = new StyledText(container, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
        styledText.setEditable(false);
        new Label(container, SWT.NONE);

        return area;

    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 400);
    }

    private class SendCommandListener implements SelectionListener {

        private Pattern commandPattern = Pattern.compile("[A-Z,0-9]+");

        private static final String commandEndString = "\n***\n";

        @Override
        public void widgetSelected(SelectionEvent e) {
            String command = text.getText();
            Matcher matcher = commandPattern.matcher(command);
            List<String> commandList = new ArrayList<String>();
            while (matcher.find()) {
                commandList.add(matcher.group());
            }

            try {

                for (String singleCommand : commandList) {

                    logService.log(LogService.LOG_DEBUG, "Issuing srm command: " + singleCommand);
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date();
                    String commandLine = singleCommand + " - " + dateFormat.format(date);
                    styledText.append(commandLine);
                    styledText.setStyleRange(new StyleRange(styledText.getCharCount() - commandLine.length(),
                            commandLine.length(), SWTResourceManager.getColor(0, 55, 255),
                            SWTResourceManager.getColor(SWT.COLOR_WHITE)));
                    styledText.append("\n");
                    String answer = srmCommunicator.issueCommand(singleCommand);
                    styledText.append(answer);
                    styledText.append(commandEndString);
                    styledText.setStyleRange(new StyleRange(styledText.getCharCount() - commandEndString.length(),
                            commandEndString.length(), SWTResourceManager.getColor(0, 255, 55),
                            SWTResourceManager.getColor(SWT.COLOR_WHITE)));
                    styledText.setTopIndex(styledText.getLineCount() - 1);
                    logService.log(LogService.LOG_DEBUG, "Answer: " + answer);

                }

            } catch (IOException e1) {
                MessageBox messageBox = new MessageBox(parentShell, SWT.ERROR);
                messageBox.setMessage("Failed to send command: " + e1.getMessage());
                messageBox.setText("Unhandled Loop Exeception");
                messageBox.open();
                logService.log(LogService.LOG_ERROR, e1.getMessage(), e1);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }

    }

}
