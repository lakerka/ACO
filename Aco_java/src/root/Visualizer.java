package root;

import java.awt.Color;
import java.awt.Shape;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;

public class Visualizer extends ApplicationFrame {

    private static final long serialVersionUID = -6754436015453195809L;

    private XYSeriesCollection dataSetCollection = new XYSeriesCollection();
    private final JFreeChart chart = createChart(dataSetCollection);

    Graph graph;

    public Visualizer(String title, Graph graph) {
	super(title);

	this.graph = graph;

	final ChartPanel chartPanel = new ChartPanel(chart);
	chartPanel.setPreferredSize(new java.awt.Dimension(720, 560));
	setContentPane(chartPanel);
    }

    public void plot(List<Integer> path, boolean redraw, String title) {

	List<Vertex> vertexList = graph.parseDataForVisualizer(path);

	final XYSeries series = new XYSeries(title, false, true);

	Double minX = 1e9;
	Double minY = 1e9;
	Double maxX = -1e9;
	Double maxY = -1e9;
	
	for (int i = 0; i < vertexList.size(); i++) {
	    
	    Vertex v = vertexList.get(i);
	    
	    
	    double x = v.getX();
	    double y = v.getY();
	    

	    XYDataItem dataItem = new XYDataItem(x, y);
	    
	    
	    
	    minX = Math.min(minX, x);
	    minY = Math.min(minY, y);
	    
	    maxX = Math.max(maxX, x);
	    maxY = Math.max(maxY, y);
	    
//	    series.add(x, y);
	    series.add(dataItem);
	}
	
	if (redraw) {
	    dataSetCollection.removeAllSeries();
	}
	dataSetCollection.addSeries(series);
	this.repaint();
    }
    
    private JFreeChart createChart(final XYDataset dataset) {

	// create the chart...
	final JFreeChart chart = ChartFactory.createXYLineChart("Path", // chart
									// title
		"X", // x axis label
		"Y", // y axis label
		dataset, // data
		PlotOrientation.VERTICAL, true, // include legend
		true, // tooltips
		false // urls
		);

	// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
	chart.setBackgroundPaint(Color.white);

	// final StandardLegend legend = (StandardLegend) chart.getLegend();
	// legend.setDisplaySeriesShapes(true);

	// get a reference to the plot for further customisation...
	final XYPlot plot = chart.getXYPlot();
	plot.setBackgroundPaint(Color.white);
	// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
	// plot.setDomainGridlinePaint(Color.white);
	// plot.setRangeGridlinePaint(Color.blue);

	final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	// renderer.setSeriesLinesVisible(0, true);
	// renderer.setSeries
	Shape shape = ShapeUtilities.createDiagonalCross(3, 1);
	renderer.setSeriesShape(0, shape);
	plot.setRenderer(renderer);

	// change the auto tick unit selection to integer units only...
	final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	// OPTIONAL CUSTOMISATION COMPLETED.

	return chart;
    }

}
