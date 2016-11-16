package at.sunplugged.z600.gui.srm;

import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

import at.sunplugged.z600.gui.Activator;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class SrmTabItemFactory {

    private static final int SAVE_SIZE = 500;

    private static final int UPDATE_TICKRATE = 10;

    Vector<Double> chart1Vector = new Vector<>(SAVE_SIZE);
    Vector<Double> chart2Vector = new Vector<>(SAVE_SIZE);
    Vector<Double> chart3Vector = new Vector<>(SAVE_SIZE);

    private boolean dragging = false;

    private boolean[] recentlyMoved = new boolean[] { false, false, false };

    public TabItem createSrmTabItem(TabFolder parent, int style) {
        TabItem tabItem = new TabItem(parent, style);
        tabItem.setText("SRM");

        Composite composite_1 = new Composite(parent, SWT.NONE);
        tabItem.setControl(composite_1);
        GridLayout gl_composite_1 = new GridLayout(3, false);
        composite_1.setLayout(gl_composite_1);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        Button btnConnect = new Button(composite_1, SWT.NONE);
        btnConnect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnConnect.setText("Verbinden");
        new Label(composite_1, SWT.NONE);

        Combo combo = new Combo(composite_1, SWT.NONE);
        combo.setItems(new String[] { "COM1", "COM2", "COM3", "COM4" });
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo.setText("Com Ausw\u00E4hlen...");

        Label lblAktuell = new Label(composite_1, SWT.NONE);
        lblAktuell.setText("Aktuell");
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        Label lblMessartaktuell = new Label(composite_1, SWT.NONE);
        lblMessartaktuell.setText("Messart Aktuell");
        new Label(composite_1, SWT.NONE);

        Combo combo_messart = new Combo(composite_1, SWT.NONE);
        combo_messart
                .setToolTipText("SRM = Wiederstand;\r\nOD = Optische Dichte;\r\nCRT = Wiederstand und Optische Dichte");
        combo_messart.setItems(new String[] { "Aus", "SRM", "OD", "CRT" });
        combo_messart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo_messart.setText("Messart...");

        Label lblMesswerteinheitAktuell = new Label(composite_1, SWT.NONE);
        lblMesswerteinheitAktuell.setText("Messwerteinheit Aktuell");
        new Label(composite_1, SWT.NONE);

        Combo combo_einheit = new Combo(composite_1, SWT.NONE);
        combo_einheit.setItems(new String[] { "Siemens/Fl\u00E4che", "Widerstand/Fl\u00E4che" });
        combo_einheit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo_einheit.setText("Messwerteinheit...");

        Label lblAbstandskompensationAktuell = new Label(composite_1, SWT.NONE);
        lblAbstandskompensationAktuell.setText("Abstandskompensation Aktuell");
        new Label(composite_1, SWT.NONE);

        Button btnAbstandskompensation = new Button(composite_1, SWT.CHECK | SWT.RIGHT);
        btnAbstandskompensation.setAlignment(SWT.LEFT);
        btnAbstandskompensation.setText("Abstandskompensation");
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        Composite composite_chart1 = new Composite(composite_1, SWT.NONE);
        GridData gd_composite_chart1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gd_composite_chart1.heightHint = 130;
        gd_composite_chart1.widthHint = 380;
        composite_chart1.setLayoutData(gd_composite_chart1);
        composite_chart1.setLayout(new FillLayout());
        Chart chart1 = createChart(0, composite_chart1);

        chart1.getPlotArea().addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent arg0) {
                dragging = false;
            }

            @Override
            public void mouseDown(MouseEvent arg0) {
                dragging = true;
            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }
        });

        chart1.getPlotArea().addMouseMoveListener(new MouseMoveListener() {

            private int lastPosition = 0;
            private int newPosition;

            private boolean newDrag = true;

            @Override
            public void mouseMove(MouseEvent arg0) {

                if (dragging) {
                    if (newDrag) {
                        lastPosition = arg0.x;
                        newDrag = false;
                    }

                    newPosition = arg0.x;
                    IAxis axis = chart1.getAxisSet().getXAxis(0);
                    Range oldRange = axis.getRange();
                    int change = (int) ((lastPosition - newPosition) * 0.6);
                    axis.setRange(new Range(oldRange.lower + change, oldRange.upper + change));
                    chart1.redraw();
                    lastPosition = arg0.x;
                    recentlyMoved[0] = true;
                } else {
                    newDrag = true;
                }

            }
        });

        Composite composite_chart2 = new Composite(composite_1, SWT.NONE);
        GridData gd_composite_chart2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gd_composite_chart2.heightHint = 130;
        gd_composite_chart2.widthHint = 380;
        composite_chart2.setLayoutData(gd_composite_chart2);
        composite_chart2.setLayout(new FillLayout());
        Chart chart2 = createChart(1, composite_chart2);

        Composite composite_chart3 = new Composite(composite_1, SWT.NONE);
        GridData gd_composite_chart3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gd_composite_chart3.heightHint = 130;
        gd_composite_chart3.widthHint = 380;
        composite_chart3.setLayoutData(gd_composite_chart3);
        composite_chart3.setLayout(new FillLayout());
        Chart chart3 = createChart(2, composite_chart3);

        new Label(composite_1, SWT.NONE);

        Button btnUpdateCharts = new Button(composite_1, SWT.CHECK);
        btnUpdateCharts.setAlignment(SWT.LEFT);
        btnUpdateCharts.setText("Update Charts");
        btnUpdateCharts.addSelectionListener(new SelectionListener() {

            private final int moveTickerTime = UPDATE_TICKRATE * 3;

            private Thread thread;

            private boolean running = false;

            private int[] moveTicker = new int[] { moveTickerTime, moveTickerTime, moveTickerTime };

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (((Button) arg0.getSource()).getSelection()) {
                    if (running == false) {
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                org.osgi.framework.ServiceReference reference = (org.osgi.framework.ServiceReference) Activator
                                        .getContext().getServiceReference(SrmCommunicator.class);
                                SrmCommunicator srmCommunicator = (SrmCommunicator) Activator.getContext()
                                        .getService(reference);
                                running = true;
                                double lastTime = System.nanoTime();
                                while (running) {
                                    try {
                                        chart1Vector.addElement(srmCommunicator.readChannel(0));
                                        if (chart1Vector.size() > SAVE_SIZE - 1) {
                                            chart1Vector.remove(0);
                                        }
                                        double[] yArray1 = new double[chart1Vector.size()];
                                        for (int i = 0; i < chart1Vector.size(); i++) {
                                            yArray1[i] = chart1Vector.get(i);
                                        }
                                        chart2Vector.addElement(srmCommunicator.readChannel(1));
                                        if (chart2Vector.size() > SAVE_SIZE - 1) {
                                            chart2Vector.remove(0);
                                        }
                                        double[] yArray2 = new double[chart2Vector.size()];
                                        for (int i = 0; i < chart2Vector.size(); i++) {
                                            yArray2[i] = chart2Vector.get(i);
                                        }

                                        chart3Vector.addElement(srmCommunicator.readChannel(2));
                                        if (chart3Vector.size() > SAVE_SIZE - 1) {
                                            chart3Vector.remove(0);
                                        }
                                        double[] yArray3 = new double[chart3Vector.size()];
                                        for (int i = 0; i < chart3Vector.size(); i++) {
                                            yArray3[i] = chart3Vector.get(i);
                                        }

                                        Display.getDefault().asyncExec(new Runnable() {

                                            @Override
                                            public void run() {
                                                ISeriesSet seriesSet = chart1.getSeriesSet();
                                                seriesSet.createSeries(SeriesType.LINE, "line series");
                                                ISeries yseries = seriesSet.getSeries("line series");
                                                yseries.setYSeries(yArray1);
                                                chart1.getAxisSet().getYAxis(0).adjustRange();

                                                if (!recentlyMoved[0]) {
                                                    moveTicker[0] = moveTickerTime;
                                                    if (yArray1.length > 90) {
                                                        chart1.getAxisSet().getXAxis(0).setRange(new Range(
                                                                yArray1.length - 100 + 10, yArray1.length + 10));
                                                    } else {
                                                        chart1.getAxisSet().getXAxis(0).setRange(new Range(0, 100));
                                                    }
                                                } else {
                                                    moveTicker[0]--;
                                                    if (moveTicker[0] <= 0) {
                                                        recentlyMoved[0] = false;
                                                    }
                                                }

                                                chart1.redraw();

                                                seriesSet = chart2.getSeriesSet();
                                                seriesSet.createSeries(SeriesType.LINE, "line series");
                                                yseries = seriesSet.getSeries("line series");
                                                yseries.setYSeries(yArray2);
                                                chart2.getAxisSet().getYAxis(0).adjustRange();
                                                if (!recentlyMoved[1]) {
                                                    if (yArray2.length > 90) {
                                                        chart2.getAxisSet().getXAxis(0).setRange(new Range(
                                                                yArray2.length - 100 + 10, yArray2.length + 10));
                                                    } else {
                                                        chart2.getAxisSet().getXAxis(0).setRange(new Range(0, 100));
                                                    }
                                                } else {
                                                    moveTicker[1]--;
                                                    if (moveTicker[1] == 0) {
                                                        recentlyMoved[1] = false;
                                                    }
                                                }
                                                chart2.redraw();

                                                seriesSet = chart3.getSeriesSet();
                                                seriesSet.createSeries(SeriesType.LINE, "line series");
                                                yseries = seriesSet.getSeries("line series");
                                                yseries.setYSeries(yArray3);
                                                chart3.getAxisSet().getYAxis(0).adjustRange();
                                                if (!recentlyMoved[2]) {
                                                    if (yArray3.length > 90) {
                                                        chart3.getAxisSet().getXAxis(0).setRange(new Range(
                                                                yArray3.length - 100 + 10, yArray3.length + 10));
                                                    } else {
                                                        chart3.getAxisSet().getXAxis(0).setRange(new Range(0, 100));
                                                    }
                                                } else {
                                                    moveTicker[2]--;
                                                    if (moveTicker[2] == 0) {
                                                        recentlyMoved[2] = false;
                                                    }
                                                }
                                                chart3.redraw();
                                            }

                                        });

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    double delta = 1000 / UPDATE_TICKRATE - (System.nanoTime() - lastTime) / 1000000;
                                    if (delta > 0) {
                                        try {
                                            Thread.sleep((long) (delta));
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    lastTime = System.nanoTime();
                                }

                            }

                        });
                        thread.start();
                    }
                } else {
                    running = false;
                    thread = null;
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);

        Button btnKalibirieren = new Button(composite_1, SWT.NONE);
        GridData gd_btnKalibirieren = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_btnKalibirieren.widthHint = 73;
        btnKalibirieren.setLayoutData(gd_btnKalibirieren);
        btnKalibirieren.setText("Kalibirieren");
        btnKalibirieren.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                System.out.println("Button Selected");

            }

        });

        return tabItem;
    }

    private Chart createChart(int channel, Composite parent) {
        Chart chart = new Chart(parent, SWT.NONE);

        double[] ySeries = { 0.3, 1.4, 1.3, 1.9, 2.1 };
        ISeriesSet seriesSet = chart.getSeriesSet();
        ISeries series = seriesSet.createSeries(SeriesType.LINE, "line series");
        series.setYSeries(ySeries);

        IAxisSet axisSet = chart.getAxisSet();
        Font tickFont = SWTResourceManager.getFont("Tahoma", 5, SWT.NONE);
        axisSet.getXAxis(0).getTick().setFont(tickFont);
        axisSet.getYAxis(0).getTick().setFont(tickFont);
        axisSet.getXAxis(0).getTitle().setVisible(false);
        axisSet.getYAxis(0).getTitle().setVisible(false);

        chart.getTitle().setText("Channel " + channel);
        chart.getTitle().setFont(SWTResourceManager.getFont("Tahoma", 7, SWT.NONE));

        chart.getLegend().setVisible(false);

        axisSet.getYAxis(0).setRange(new Range(0, 1000));
        axisSet.getXAxis(0).setRange(new Range(0, 100));

        return chart;
    }

    private void startChartUpdating() {

    }

}
