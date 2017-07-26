package at.sunplugged.z600.gui.machinediagram;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import at.sunplugged.z600.conveyor.api.ConveyorMonitor;
import at.sunplugged.z600.frontend.gui.utils.api.ServiceRegistryAccess;
import at.sunplugged.z600.gui.views.MainView;

public class MiscInformationFigure extends Figure {

    private static int WIDTH = 200;

    private static int HEIGHT = 75;

    private LabeledLabel gasFLowSccmLabel;

    private LabeledLabel estimatedFinishTime;

    private LabeledLabel positionControlRunning;

    private LabeledLabel sqlRunning;

    public MiscInformationFigure(int x, int y) {
        this.setBounds(new Rectangle(x, y, WIDTH, HEIGHT));
        this.setBorder(new LineBorder());
        this.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        this.setOpaque(true);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;

        this.setLayoutManager(gridLayout);
        createLabels();

    }

    private void createLabels() {
        gasFLowSccmLabel = new LabeledLabel(this, "Gasflow [sccm]", "0.0");
        estimatedFinishTime = new LabeledLabel(this, "ETC", "---");
        positionControlRunning = new LabeledLabel(this, "PostionControl: ", "OFF");
        // sqlRunning = new LabeledLabel(this, "SQL", "OFF");

        Display.getDefault().timerExec(2000, new Runnable() {
            @Override
            public void run() {
                gasFLowSccmLabel.setText(String.format("%.3f",
                        MainView.getMachineStateService().getGasFlowControl().getCurrentGasFlowInSccm()));
                estimatedFinishTime
                        .setText(ServiceRegistryAccess.getService(ConveyorMonitor.class).getFormattedETCMessage());

                if (MainView.getConveyorPositionCorrectionService().isRunning() == true) {
                    positionControlRunning.setText("ON");
                    positionControlRunning.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                } else {
                    positionControlRunning.setText("OFF");
                    positionControlRunning.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                }

                Display.getDefault().timerExec(1000, this);
            }
        });

    }

    private final class LabeledLabel {

        private Label label;

        private Label text;

        private Figure parentFigure;

        public LabeledLabel(Figure parentFigure, String label, String initialText) {
            this.parentFigure = parentFigure;
            this.label = new Label();
            GridData labelGd = new GridData();
            labelGd.horizontalSpan = 1;
            labelGd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
            labelGd.grabExcessHorizontalSpace = true;
            this.label.setText(label);

            parentFigure.add(this.label, labelGd);

            this.text = new Label();
            GridData textGd = new GridData();
            textGd.horizontalSpan = 1;
            textGd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
            textGd.grabExcessHorizontalSpace = true;
            textGd.widthHint = 70;
            this.text.setText(initialText);

            parentFigure.add(this.text, textGd);

        }

        public void setText(String text) {
            this.text.setText(text);
        }

        public void setForegroundColor(Color color) {
            text.setForegroundColor(color);
        }

    }

}
