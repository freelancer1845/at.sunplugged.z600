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

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorPositionService;

@Component(immediate = true)
public final class ConveyorGroupFactory {

    private static ConveyorControlService conveyorService;

    private static ConveyorPositionService conveyorPositionService;

    private static StandardThreadPoolService threadPool;

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

        Button btnStartPositionControl = new Button(group, SWT.NONE);
        btnStartPositionControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStartPositionControl.setText("Start Position Control");
        btnStartPositionControl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.start();
            }

        });
        Button btnStopPositionControl = new Button(group, SWT.NONE);
        btnStopPositionControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStopPositionControl.setText("Stop Position Control");
        btnStopPositionControl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.stop();
            }

        });

        Button btnStartPositionControlling = new Button(group, SWT.NONE);
        btnStartPositionControlling.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStartPositionControlling.setText("Start Position controlling");
        btnStartPositionControlling.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.togglePositionControl(true);
            }

        });

        Button btnStopPositionControlling = new Button(group, SWT.NONE);
        btnStopPositionControlling.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnStopPositionControlling.setText("Stop Position controlling");
        btnStopPositionControlling.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                conveyorPositionService.togglePositionControl(false);
            }

        });

        Label lblLeftPosition = new Label(group, SWT.NONE);
        lblLeftPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblLeftPosition.setText("Left Position: 0");

        Label lblRightPosition = new Label(group, SWT.NONE);
        lblRightPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblRightPosition.setText("Right Position: 0");

        Label lblLeftSpeed = new Label(group, SWT.NONE);
        lblLeftSpeed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblLeftSpeed.setText("Speed Left: 0");

        Label lblRightSpeed = new Label(group, SWT.NONE);
        lblRightSpeed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblRightSpeed.setText("Speed Right: 0");

        Display.getDefault().timerExec(500, new Runnable() {

            @Override
            public void run() {
                lblLeftPosition.setText("Left Position: " + conveyorPositionService.getLeftPosition());
                lblRightPosition.setText("Right Positon: " + conveyorPositionService.getRightPosition());
                lblLeftSpeed.setText("Speed Left: " + conveyorService.getSpeedLogger().getLeftSpeed());
                lblRightSpeed.setText("Speed Right: " + conveyorService.getSpeedLogger().getRightSpeed());
                Display.getDefault().timerExec(500, this);
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

    @Reference(unbind = "unbindConveyorPositionService")
    public synchronized void bindConveyorPositionService(ConveyorPositionService conveyorPositionService) {
        ConveyorGroupFactory.conveyorPositionService = conveyorPositionService;
    }

    public synchronized void unbindConveyorPositionService(ConveyorPositionService conveyorPositionService) {
        if (ConveyorGroupFactory.conveyorPositionService == conveyorPositionService) {
            ConveyorGroupFactory.conveyorPositionService = null;
        }
    }

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        ConveyorGroupFactory.threadPool = threadPool;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        if (ConveyorGroupFactory.threadPool == threadPool) {
            ConveyorGroupFactory.threadPool = null;
        }
    }

}