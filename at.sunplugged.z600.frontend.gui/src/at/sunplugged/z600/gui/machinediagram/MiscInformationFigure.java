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

import at.sunplugged.z600.gui.views.MainView;

public class MiscInformationFigure extends Figure {

    private static int WIDTH = 200;

    private static int HEIGHT = 50;

    private LabeledLabel gasFLowSccmLabel;

    private LabeledLabel estimatedFinishTime;

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
        gasFLowSccmLabel = new LabeledLabel(this, "Gasflow [sccm]", "0.01416468");
        estimatedFinishTime = new LabeledLabel(this, "ETC", "12:42:32");

        Display.getDefault().timerExec(2000, new Runnable() {
            @Override
            public void run() {
                gasFLowSccmLabel.setText(String.format("%.3f",
                        MainView.getMachineStateService().getGasFlowControl().getCurrentGasFlowInSccm()));
                estimatedFinishTime.setText(MainView.getConveyorControlService().getExtimatedFinishTime());

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
            this.label.setText(text);
        }

    }

}
