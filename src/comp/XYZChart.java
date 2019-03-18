package comp;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

class XYZChart {

    private static double sup = 1000;
    private static int k1;
    private static int k2;

    XYZChart(String title, double[][] arr, int k1, int k2, String x_name, String y_name) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XYZChart.k1 = k1;
        XYZChart.k2 = k2;

        ChartPanel chartPanel = new ChartPanel(createChart(createDataset(arr), title, x_name, y_name)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1900, 900);
            }
        };
        chartPanel.setMouseZoomable(true, false);
        f.add(chartPanel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static JFreeChart createChart(XYDataset dataset, String title, String x_name, String y_name) {
        NumberAxis xAxis = new NumberAxis(x_name);
        NumberAxis yAxis = new NumberAxis(y_name);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        XYBlockRenderer r = new XYBlockRenderer();
        SpectrumPaintScale ps = new SpectrumPaintScale(0, sup);
        r.setPaintScale(ps);
        r.setBlockHeight(k2);
        r.setBlockWidth(k1);
        plot.setRenderer(r);
        JFreeChart chart = new JFreeChart(title,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        PaintScaleLegend legend = new PaintScaleLegend(ps, scaleAxis);
        legend.setSubdivisionCount(128);
        legend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(20);
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setBackgroundPaint(Color.WHITE);
        chart.addSubtitle(legend);
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    private static XYZDataset createDataset(double[][] arr) {
        int n = arr.length;
        int m = arr[0].length;
        sup = 0;
        for (double[] anArr : arr)
            for (double anAnArr : anArr)
                sup = Math.max(sup, anAnArr);

        sup /= 0.85;

        DefaultXYZDataset dataset = new DefaultXYZDataset();
        for (int i = 0; i < n; i = i + 1) {
            double[][] data = new double[3][m];
            for (int j = 0; j < m; j = j + 1) {
                data[0][j] = j * k1;
                data[1][j] = i * k2;
                data[2][j] = arr[i][j];
            }
            dataset.addSeries("Series" + i, data);
        }
        return dataset;

    }

    private static class SpectrumPaintScale implements PaintScale {

        private static final float H1 = 0f;
        private static final float H2 = 1f;
        private final double lowerBound;
        private final double upperBound;

        SpectrumPaintScale(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public double getLowerBound() {
            return lowerBound;
        }

        @Override
        public double getUpperBound() {
            return upperBound;
        }

        @Override
        public Paint getPaint(double value) {
            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
            float scaledH = H1 + scaledValue * (H2 - H1);
            if (scaledH > 0)
                return Color.getHSBColor(scaledH, 1f, 1f);
            else if (scaledH < 0)
                return Color.getHSBColor(scaledH, 1f, 0f);
            else
                return Color.WHITE;
        }
    }
}