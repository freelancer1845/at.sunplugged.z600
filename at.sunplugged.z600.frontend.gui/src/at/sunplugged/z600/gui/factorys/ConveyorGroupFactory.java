package at.sunplugged.z600.gui.factorys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;

@Component(immediate = true)
public final class ConveyorGroupFactory {

    private static ConveyorControlService conveyorService;

    private static Text text;

    /**
     * @wbp.factory
     */
    public static Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        group.setText("Bandlauf");
        group.setLayout(new GridLayout(2, true));

        Label lblGeschwindigkeit = new Label(group, SWT.NONE);
        lblGeschwindigkeit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblGeschwindigkeit.setText("Geschwindigkeit in mm/s");

        text = new Text(group, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

        Button btnLinks = new Button(group, SWT.NONE);
        btnLinks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnLinks.setText("Links");

        Button btnRechts = new Button(group, SWT.NONE);
        btnRechts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnRechts.setText("Rechts");

        btnLinks.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        btnLinks.setEnabled(false);
                        btnRechts.setEnabled(true);
                    }

                });
            }
        });

        btnRechts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        btnLinks.setEnabled(true);
                        btnRechts.setEnabled(false);
                    }

                });

            }
        });

        Button btnStart = new Button(group, SWT.NONE);
        btnStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStart.setText("Start");
        btnStart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                double speed;
                try {
                    speed = Double.valueOf(text.getText());
                } catch (NumberFormatException e1) {
                    return;
                }
                if (btnRechts.isEnabled() == false) {

                    conveyorService.start(speed, Mode.RIGHT_TO_LEFT);
                } else if (btnLinks.isEnabled() == false) {
                    conveyorService.start(speed, Mode.LEFT_TO_RIGHT);
                }
            }
        });

        Button btnStop = new Button(group, SWT.NONE);
        btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStop.setText("Stopp");
        btnStop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorService.stop();
            }
        });
        return group;
    }

    @Reference(unbind = "unbindConveyorControlService")
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        ConveyorGroupFactory.conveyorService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (ConveyorGroupFactory.conveyorService == conveyorControlService) {
            ConveyorGroupFactory.conveyorService = null;
        }
    }

}