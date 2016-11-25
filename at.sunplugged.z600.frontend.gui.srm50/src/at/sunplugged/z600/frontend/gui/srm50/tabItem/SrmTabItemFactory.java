package at.sunplugged.z600.frontend.gui.srm50.tabItem;

import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.frontend.gui.srm50.SrmGuiActivator;

public class SrmTabItemFactory {

    private static final int SAVE_SIZE = 500;

    private static final int UPDATE_TICKRATE = 10;

    Vector<Double> chart1Vector = new Vector<>(SAVE_SIZE);
    Vector<Double> chart2Vector = new Vector<>(SAVE_SIZE);
    Vector<Double> chart3Vector = new Vector<>(SAVE_SIZE);

    private boolean dragging = false;

    private boolean[] recentlyMoved = new boolean[] { false, false, false };

    /**
     * Creates the tab item for the srm view.
     * 
     * @param parent {@linkplain TabFolder}
     * @param style Stylee of the {@linkplain TabItem}.
     * @return the create {@linkplain TabItem}.
     */
    public TabItem createSrmTabItem(TabFolder parent, int style) {
        TabItem tabItem = new TabItem(parent, style);
        tabItem.setText("SRM");
        Composite compositeOne = new Composite(parent, SWT.NONE);
        tabItem.setControl(compositeOne);
        GridLayout glCompositeOne = new GridLayout(3, false);
        compositeOne.setLayout(glCompositeOne);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);

        Button btnConnect = new Button(compositeOne, SWT.NONE);
        btnConnect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnConnect.setText("Verbinden");
        new Label(compositeOne, SWT.NONE);

        Combo combo = new Combo(compositeOne, SWT.NONE);
        combo.setItems(new String[] { "COM1", "COM2", "COM3", "COM4" });
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo.setText("Com Auswählen...");

        btnConnect.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = combo.getSelectionIndex();
                if (selectedIndex != -1) {
                    try {
                        SrmGuiActivator.getSrmCommunicator().connect(combo.getItem(selectedIndex));
                    } catch (IOException e1) {
                        SrmGuiActivator.getLogService().log(LogService.LOG_ERROR, "Failed on user connect: ", e1);
                    }
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        Label lblAktuell = new Label(compositeOne, SWT.NONE);
        lblAktuell.setText("Aktuell");
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);

        Label lblMessartaktuell = new Label(compositeOne, SWT.NONE);
        lblMessartaktuell.setText("Messart Aktuell");
        new Label(compositeOne, SWT.NONE);

        Combo comboMessart = new Combo(compositeOne, SWT.NONE);
        comboMessart
                .setToolTipText("SRM = Wiederstand;\r\nOD = Optische Dichte;\r\nCRT = Wiederstand und Optische Dichte");
        comboMessart.setItems(new String[] { "Aus", "SRM", "OD", "CRT" });
        comboMessart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboMessart.setText("Messart...");

        Label lblMesswerteinheitAktuell = new Label(compositeOne, SWT.NONE);
        lblMesswerteinheitAktuell.setText("Messwerteinheit Aktuell");
        new Label(compositeOne, SWT.NONE);

        Combo comboEinheit = new Combo(compositeOne, SWT.NONE);
        comboEinheit.setItems(new String[] { "Siemens/Fläche", "Widerstand/Fläche" });
        comboEinheit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboEinheit.setText("Messwerteinheit...");

        Label lblAbstandskompensationAktuell = new Label(compositeOne, SWT.NONE);
        lblAbstandskompensationAktuell.setText("Abstandskompensation Aktuell");
        new Label(compositeOne, SWT.NONE);

        Button btnAbstandskompensation = new Button(compositeOne, SWT.CHECK | SWT.RIGHT);
        btnAbstandskompensation.setAlignment(SWT.LEFT);
        btnAbstandskompensation.setText("Abstandskompensation");
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);

        Composite compositeChartOne = new Composite(compositeOne, SWT.NONE);
        GridData gdCompositeChartOne = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gdCompositeChartOne.heightHint = 130;
        gdCompositeChartOne.widthHint = 380;
        compositeChartOne.setLayoutData(gdCompositeChartOne);
        compositeChartOne.setLayout(new FillLayout());
        SrmUpdatableChart chart1 = new SrmUpdatableChart(compositeChartOne, "Channel 1", 1);

        Composite compositeChartTwo = new Composite(compositeOne, SWT.NONE);
        GridData gdCompositeChartTwo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gdCompositeChartTwo.heightHint = 130;
        gdCompositeChartTwo.widthHint = 380;
        compositeChartTwo.setLayoutData(gdCompositeChartTwo);
        compositeChartTwo.setLayout(new FillLayout());
        SrmUpdatableChart chart2 = new SrmUpdatableChart(compositeChartTwo, "Channel 2", 2);

        Composite compositeChartThree = new Composite(compositeOne, SWT.NONE);
        GridData gdCompositeChartThree = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gdCompositeChartThree.heightHint = 130;
        gdCompositeChartThree.widthHint = 380;
        compositeChartThree.setLayoutData(gdCompositeChartThree);
        compositeChartThree.setLayout(new FillLayout());
        SrmUpdatableChart chart3 = new SrmUpdatableChart(compositeChartThree, "Channel 3", 3);

        new Label(compositeOne, SWT.NONE);

        Button btnUpdateCharts = new Button(compositeOne, SWT.CHECK);
        btnUpdateCharts.setAlignment(SWT.LEFT);
        btnUpdateCharts.setText("Update Charts");
        btnUpdateCharts.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.getSource()).getSelection()) {
                    if (!chart1.isUpdating()) {
                        chart1.startUpdating();
                    }
                    if (!chart2.isUpdating()) {
                        chart2.startUpdating();
                    }
                    if (!chart3.isUpdating()) {
                        chart3.startUpdating();
                    }
                } else {
                    if (chart1.isUpdating()) {
                        chart1.stopUpdating();
                    }
                    if (chart2.isUpdating()) {
                        chart2.stopUpdating();
                    }
                    if (chart3.isUpdating()) {
                        chart3.stopUpdating();
                    }
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);
        new Label(compositeOne, SWT.NONE);

        Button btnKalibirieren = new Button(compositeOne, SWT.NONE);
        GridData gdBtnKalibirieren = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gdBtnKalibirieren.widthHint = 73;
        btnKalibirieren.setLayoutData(gdBtnKalibirieren);
        btnKalibirieren.setText("Kalibirieren");
        btnKalibirieren.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                System.out.println("Button Selected");
                throw new RuntimeException("Test");

            }

        });

        new Label(compositeOne, SWT.NONE);
        Button issueCommandsButton = new Button(compositeOne, SWT.PUSH);
        GridData gdIssueCommandsButton = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        issueCommandsButton.setLayoutData(gdIssueCommandsButton);
        issueCommandsButton.setText("SRM Interface");
        issueCommandsButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IssueCommandsDialog issueCommandsDialog = new IssueCommandsDialog(parent.getShell());
                issueCommandsDialog.open();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });

        return tabItem;
    }

}
