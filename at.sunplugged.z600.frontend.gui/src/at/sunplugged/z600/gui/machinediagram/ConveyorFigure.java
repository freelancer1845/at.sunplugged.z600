package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMachineEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.gui.views.MainView;

public class ConveyorFigure extends Figure implements MachineEventHandler {

    private static int WIDTH = 420;

    private static int HEIGHT = 135;

    private Label speedLeftLabel;

    private Label speedCombindedLabel;

    private Label speedRightLabel;

    private Label positionLeftLabel;

    private Label positionCombinedLabel;

    private Label positionRightLabel;

    private Label correctionLeftLabel;

    private Label directionLabel;

    private Label correctionRightLabel;

    public ConveyorFigure(int x, int y) {
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        this.setBorder(new LineBorder());
        this.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        this.setOpaque(true);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;

        this.setLayoutManager(gridLayout);
        createLabels();

    }

    private void createLabels() {

        Label speedLabel = new Label("Geschwindigkeit in [mm/s]");
        GridData speedLabelGd = new GridData();
        speedLabelGd.horizontalSpan = 3;
        speedLabelGd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
        this.add(speedLabel, speedLabelGd);

        speedLeftLabel = labelFactory("speedLeftLabel");

        speedCombindedLabel = labelFactory("speedCombinedLabel");

        speedRightLabel = labelFactory("speedRightLabel");

        Label positionLabel = new Label("Position in [m]");
        GridData positionLabelGd = new GridData();
        positionLabelGd.horizontalSpan = 3;
        positionLabelGd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
        this.add(positionLabel, positionLabelGd);

        positionLeftLabel = labelFactory(String.format("%.4f", MainView.getConveyorControlService().getPosition()));

        positionCombinedLabel = labelFactory(String.format("%.4f", MainView.getConveyorControlService().getPosition()));

        positionRightLabel = labelFactory(String.format("%.4f", MainView.getConveyorControlService().getPosition()));

        Label correctionLabel = new Label("Korrektur und Richtung");
        GridData correctionLabelGd = new GridData();
        correctionLabelGd.horizontalSpan = 3;
        correctionLabelGd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
        this.add(correctionLabel, correctionLabelGd);

        correctionLeftLabel = labelFactory("correctionLeftLabel");

        directionLabel = labelFactory("directionLabel");

        correctionRightLabel = labelFactory("correctionRightLabel");

        Display display = Display.getDefault();

        display.timerExec(1000, new Runnable() {
            @Override
            public void run() {
                positionLeftLabel.setText(String.format("%.4f", MainView.getConveyorControlService().getPosition()));
                positionCombinedLabel
                        .setText(String.format("%.4f", MainView.getConveyorControlService().getPosition()));
                positionRightLabel.setText(String.format("%.4f", MainView.getConveyorControlService().getPosition()));
            }
        });

        display.timerExec(500, new Runnable() {

            @Override
            public void run() {
                correctionLeftLabel
                        .setText(String.valueOf(MainView.getConveyorPositionCorrectionService().getRuntimeLeft()));
                correctionRightLabel
                        .setText(String.valueOf(MainView.getConveyorPositionCorrectionService().getRuntimeRight()));
                display.timerExec(500, this);
            }

        });
    }

    private Label labelFactory(String text) {
        Label label = new Label(text);
        label.setBorder(new LineBorder());
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        this.add(label, gridData);
        return label;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType() == Type.CONVEYOR_EVENT) {
            ConveyorControlService conveyorService = MainView.getConveyorControlService();
            ConveyorMachineEvent conveyorEvent = (ConveyorMachineEvent) event;
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    switch (conveyorEvent.getConveyorEventType()) {
                    case LEFT_SPEED_CHANGED:
                        speedLeftLabel.setText(String.format("%.2f", conveyorEvent.getValue()));
                        speedCombindedLabel.setText(String.format("%.2f", conveyorService.getCurrentSpeed()));
                        break;
                    case RIGHT_SPEED_CHANGED:
                        speedRightLabel.setText(String.format("%.2f", conveyorEvent.getValue()));
                        speedCombindedLabel.setText(String.format("%.2f", conveyorService.getCurrentSpeed()));
                        break;
                    case NEW_DISTANCE:
                        positionLeftLabel.setText(String.format("%.4f", conveyorService.getLeftPosition()));
                        positionCombinedLabel.setText(String.format("%.4f", conveyorService.getPosition()));
                        positionRightLabel.setText(String.format("%.4f", conveyorService.getRightPosition()));
                        break;
                    case MODE_CHANGED:
                        Mode mode = (Mode) conveyorEvent.getValue();
                        switch (mode) {
                        case LEFT_TO_RIGHT:
                            directionLabel.setText("-->>");
                            break;
                        case RIGHT_TO_LEFT:
                            directionLabel.setText("<<--");
                            break;
                        case STOP:
                            directionLabel.setText("Stopp");
                            break;
                        default:
                            break;
                        }
                    default:
                        break;
                    }
                }

            });

        }
    }

}
