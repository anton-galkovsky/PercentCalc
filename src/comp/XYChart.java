package comp;

import javafx.geometry.Point2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;

import static comp.Producer.graphs;

class XYChart extends ApplicationFrame {

    XYChart(final String title) {
        super(title);
        final XYSeriesCollection data = new XYSeriesCollection();
        for (Graph graph : graphs) {
            final XYSeries series = new XYSeries(graph.name);
            for (Point2D p : graph.points)
                series.add(p.getX(), p.getY());
            data.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Time",
                "Function",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new Dimension(1900, 900));
        setContentPane(chartPanel);
    }
}