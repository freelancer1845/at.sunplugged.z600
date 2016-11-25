package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import java.io.IOException;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.frontend.gui.srm50.SrmGuiActivator;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class IssueCommandsDialog extends TitleAreaDialog {
    private Text text;

    private StyledText styledText;

    private SrmCommunicator srmCommunicator;

    private Shell parentShell;

    protected IssueCommandsDialog(Shell parentShell) {
        super(parentShell);
        this.parentShell = parentShell;
        ServiceReference<SrmCommunicator> serviceReference = SrmGuiActivator.getContext()
                .getServiceReference(SrmCommunicator.class);

        srmCommunicator = SrmGuiActivator.getContext().getService(serviceReference);
        if (srmCommunicator == null) {
            this.close();
        }
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

        Button sendButton = new Button(container, SWT.PUSH);
        sendButton.setText("Send Command");
        sendButton.addSelectionListener(new SendCommandListener());

        styledText = new StyledText(container, SWT.BORDER);
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

        @Override
        public void widgetSelected(SelectionEvent e) {
            String command = text.getText();
            SrmGuiActivator.getLogService().log(LogService.LOG_DEBUG, "Issuing srm command: " + command);
            try {
                String answer = srmCommunicator.issueCommand(command);
                styledText.setText(answer);
                SrmGuiActivator.getLogService().log(LogService.LOG_DEBUG, "Answer: " + answer);
            } catch (IOException e1) {
                MessageBox messageBox = new MessageBox(parentShell, SWT.ERROR);
                messageBox.setMessage("Failed to send command: " + e1.getMessage());
                messageBox.setText("Unhandled Loop Exeception");
                messageBox.open();
                SrmGuiActivator.getLogService().log(LogService.LOG_ERROR, e1.getMessage(), e1);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // TODO Auto-generated method stub

        }

    }

}
