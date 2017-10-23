package at.sunplugged.z600.frontend.gui.utils.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

public abstract class UpdatableChart {

    /** Update Rate for the charts. */
    private static final int UPDATE_TICKRATE = 10;

    /**
     * The time the charts wait to set the Range to the standard values (end of
     * chart).
     */
    private static final int MOVE_TICKER_TIME = UPDATE_TICKRATE * 3;

    /** The underlying Chart. */
    private final Chart chart;

    /** Title of the chart. */
    protected final String title;

    /** Underlying DataList. */
    protected List<Double> yDataList = new ArrayList<>();

    protected List<Double> xDataList = new ArrayList<>();

    /** Lock preventing race conditions. */
    private Lock listLock = new ReentrantLock();

    /** Array used for transferring data to the chart. */
    private double[] yArray = new double[] { 0 };

    private double[] xArray = new double[] { 0 };

    /** Maximum data point saved. */
    private int maxPoints = 10000;

    /** Updater Thread. */
    private Thread updaterThread;

    /** Updater Runnable. */
    private UpdaterRunnable updaterRunnable;

    /** Saving whether the chart is currently moved via the mouse. */
    private boolean isTouched = false;

    /** If the chart position has been changed by the user recently. */
    private boolean recentlyMoved = false;

    /** Variable for the move ticker. */
    private int moveTicker;

    public UpdatableChart(Composite parent, String title) {
        this.chart = new Chart(parent, SWT.NONE);
        this.title = title;
        setupBasicChart();
        addListeners();
    }

    public void setLayoutData(Object layoutData) {
        chart.setLayoutData(layoutData);
    }

    /**
     * The Implementation should return the xData. It is called at the same time
     * as addNewDataY();
     */
    protected abstract double addNewDataX();

    /**
     * THe Implementation should return the yData belonging to the xdata.
     * 
     */
    protected abstract double addNewDataY();

    private void addNewData() {
        try {
            listLock.lock();
            xDataList.add(addNewDataX());
            yDataList.add(addNewDataY());
        } catch (Exception e) {
            throw e;
        } finally {
            listLock.unlock();
        }
    }

    private void removeOldData() {
        if (yDataList.size() > maxPoints) {
            int difference = yDataList.size() - maxPoints;
            if (listLock.tryLock() == true) {
                for (int i = 0; i < difference; i++) {
                    yDataList.remove(0);
                    xDataList.remove(0);
                }
                listLock.unlock();
            }
        }

    }

    public void startUpdating() {
        if (updaterRunnable != null && updaterThread != null) {
            throw new IllegalStateException("Chart is already updating!");
        }

        updaterRunnable = new UpdaterRunnable();
        updaterThread = new Thread(updaterRunnable);
        updaterThread.setName("ChartUpdaterThread");
        updaterThread.start();
    }

    public void stopUpdating() {
        if (updaterRunnable == null && updaterThread == null) {
            throw new IllegalStateException("Chart is currently not updating!");
        }
        updaterRunnable.stopThread();
        updaterThread.interrupt();
        updaterThread = null;
        updaterRunnable = null;
    }

    public boolean isUpdating() {
        if (updaterRunnable != null) {
            return updaterRunnable.isRunning();
        } else {
            return false;
        }
    }

    public void resetChart() {
        yDataList.clear();
        xDataList.clear();
    }

    protected void setupBasicChart() {
        IAxisSet axisSet = chart.getAxisSet();
        Font tickFont = SWTResourceManager.getFont("Tahoma", 5, SWT.NONE);
        axisSet.getXAxis(0).getTick().setFont(tickFont);
        axisSet.getYAxis(0).getTick().setFont(tickFont);
        axisSet.getXAxis(0).getTitle().setVisible(false);
        axisSet.getYAxis(0).getTitle().setVisible(false);

        chart.getTitle().setText(this.title);
        chart.getTitle().setFont(SWTResourceManager.getFont("Tahoma", 7, SWT.NONE));

        chart.getLegend().setVisible(false);

        axisSet.getYAxis(0).setRange(new Range(0, 1000));
        axisSet.getXAxis(0).setRange(new Range(0, 100));
    }

    protected void addListeners() {
        chart.getPlotArea().addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent arg0) {
                isTouched = false;
            }

            @Override
            public void mouseDown(MouseEvent arg0) {
                isTouched = true;
            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }
        });

        chart.getPlotArea().addMouseMoveListener(new MouseMoveListener() {

            private int lastPosition = 0;
            private int newPosition;

            private boolean newDrag = true;

            @Override
            public void mouseMove(MouseEvent arg0) {

                if (isTouched) {
                    if (newDrag) {
                        lastPosition = arg0.x;
                        newDrag = false;
                    }

                    newPosition = arg0.x;
                    IAxis axis = chart.getAxisSet().getXAxis(0);
                    Range oldRange = axis.getRange();
                    int change = (int) ((lastPosition - newPosition) * 0.6);
                    axis.setRange(new Range(oldRange.lower + change, oldRange.upper + change));
                    chart.redraw();
                    lastPosition = arg0.x;
                    recentlyMoved = true;
                } else {
                    newDrag = true;
                }

            }
        });
    }

    protected void updateChart() {
        ISeriesSet seriesSet = chart.getSeriesSet();
        ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, "line series");
        series.setSymbolSize(1);
        series.setXSeries(xArray);
        series.setYSeries(yArray);
        if (!isTouched) {
            if (!recentlyMoved) {
                moveTicker = MOVE_TICKER_TIME;

                chart.getAxisSet().getYAxis(0).adjustRange();
                double maxXValue = xDataList.get(xDataList.size() - 1);
                if (maxXValue > 90) {
                    chart.getAxisSet().getXAxis(0).setRange(new Range(maxXValue - 100 + 10, maxXValue + 10));
                } else {
                    chart.getAxisSet().getXAxis(0).setRange(new Range(0, 100));
                }

            } else {
                moveTicker--;
                if (moveTicker <= 0) {
                    recentlyMoved = false;
                }
            }

        }
        chart.redraw();

    }

    private void transferDataToDoubleArray() {
        try {
            listLock.lock();
            yArray = new double[yDataList.size()];
            for (int i = 0; i < yDataList.size(); i++) {
                yArray[i] = yDataList.get(i);
            }
            xArray = new double[xDataList.size()];
            for (int i = 0; i < xDataList.size(); i++) {
                xArray[i] = xDataList.get(i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            listLock.unlock();
        }

    }

    private final class UpdaterRunnable implements Runnable {

        private boolean running = false;

        public boolean isRunning() {
            return running;
        }

        public void stopThread() {
            running = false;
        }

        @Override
        public void run() {

            running = true;
            double lastTime = System.nanoTime();
            while (running) {
                addNewData();
                transferDataToDoubleArray();
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateChart();
                        } catch (Exception e) {
                            // Do nohting if that happens
                        }
                    }
                });
                removeOldData();

                double delta = 1000 / UPDATE_TICKRATE - (System.nanoTime() - lastTime) / 1000000;
                if (delta > 0) {
                    try {
                        Thread.sleep((long) (delta));
                    } catch (InterruptedException e) {
                        // Do nothing. This usually happens when the updating is
                        // interrupted.
                    }
                }
                lastTime = System.nanoTime();
            }

        }

    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

}
