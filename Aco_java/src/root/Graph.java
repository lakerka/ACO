package root;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;


public class Graph {

    public static final int TOP = 10;
    public static final JavaPlot javaPlot = new JavaPlot();
    public static boolean flag = false;
    private int vCount = 0;

    private Vertex v[];

    /**
     * Distance array
     */
    double d[][];

    /**
     * Pheromone quantity on edge.
     */
    double p[][];

    public Graph(String path) {

	readGraphFromFile(path);
	initDistances();
	p = new double[vCount + TOP][vCount + TOP];
    }

    public void evaporate(int aVert, int bVert, int by) {

	double pheromone = p[aVert][bVert];

	if (pheromone > by) {
	    pheromone -= by;
	} else {
	    pheromone = 0;
	}

	p[aVert][bVert] = pheromone;
	p[bVert][aVert] = pheromone;
    }

    public void plot(List<Integer> path, boolean redraw, int iterationCount,
	    int antCount, String params) {
        
	PlotStyle myPlotStyle = new PlotStyle();
	myPlotStyle.setStyle(Style.LINESPOINTS);

	DataSetPlot dataSet = parsePathToDataSetPlot(path);

	myPlotStyle.setLineWidth(1);

	String title = "";

	if (iterationCount > 0) {
	    title += "iteration count: " + iterationCount;
	}
	if (antCount > 0) {
	    title += ", ant count: " + antCount;
	}
	if (params != null && !params.isEmpty()) {
	    title += ", " + params;
	}

	if (title != null && !title.isEmpty()) {
	    dataSet.setTitle(title);
	}
	dataSet.setPlotStyle(myPlotStyle);
	
	javaPlot.addPlot(dataSet);
	javaPlot.newGraph();
	javaPlot.plot();
	
    }

    /**
     * If path is: 1 2 3 1 provided path must be 1 2 3.
     * 
     * @param path
     *            hamiltonian path.
     */
    public DataSetPlot parsePathToDataSetPlot(List<Integer> path) {

	

	if (path == null || path.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Path that is to be plotted must"
			    + "be non null and non empty !");
	}

	double data[][] = new double[vCount + 1][2];

	for (int i = 0; i < path.size(); i++) {

	    int index = path.get(i);

	    Vertex curVertex = v[index];

	    data[i][0] = curVertex.getX();
	    data[i][1] = curVertex.getY();
	}

	// must return to first vertex
	Vertex firstVertex = v[path.get(0)];

	int index = path.size();
	data[index][0] = firstVertex.getX();
	data[index][1] = firstVertex.getY();

	return new DataSetPlot(data);
    }

    public List<Vertex> parseDataForVisualizer(List<Integer> path) {
	

	if (path == null || path.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Path that is to be plotted must"
			    + "be non null and non empty !");
	}

	List<Vertex> vertexList = new ArrayList<Vertex>();

	for (int i = 0; i < path.size(); i++) {

	    int index = path.get(i);
	    Vertex curVertex = v[index];
	    vertexList.add(curVertex);
	}

	// must return to first vertex
	Vertex firstVertex = v[path.get(0)];
	vertexList.add(firstVertex);
	
	return vertexList;
    }
    /**
     * Initialized vertex count and vertex array.
     * 
     * @param path
     *            full path.
     */
    private void readGraphFromFile(String path) {

	try {

	    Scanner scanner = new Scanner(new File(path));

	    for (int i = 0; i < 3; i++) {
		String line = scanner.nextLine();
		// print(line);
	    }
	    for (int i = 0; i < 2; i++) {
		String nextString = scanner.next();
		// print(nextString);
	    }

	    vCount = scanner.nextInt();

	    for (int i = 0; i < 3; i++) {
		scanner.nextLine();
	    }

	    v = new Vertex[vCount + TOP];

	    for (int i = 0; i < vCount; i++) {

		int id = scanner.nextInt();
		double x = scanner.nextDouble();
		double y = scanner.nextDouble();

		v[i + 1] = new Vertex(x, y);
	    }

	    scanner.close();

	} catch (IOException e) {
	    System.err.println(e);
	}
    }

    public static void print(String s) {
	System.out.println(s);
    }

    private void initDistances() {

	d = new double[vCount + TOP][vCount + TOP];

	for (int i = 1; i <= vCount; i++) {
	    for (int j = i + 1; j <= vCount; j++) {

		double distance = euclideanDistance(v[i], v[j]);
		d[i][j] = distance;
		d[j][i] = distance;
	    }
	}
    }

    private double euclideanDistance(Vertex a, Vertex b) {

	double xd = a.getX() - b.getX();
	double yd = a.getY() - b.getY();

	double squareRoot = Math.sqrt(sqr(xd) + sqr(yd));

	return squareRoot;
    }

    private double sqr(double value) {

	return value * value;
    }

    /**
     * Calulates route length.
     * 
     * @param path
     *            path. If full path would be 1 2 3 1 then such path must be
     *            provided: 1 2 3
     * @return distance of TSP path.
     */
    public double calcDistance(List<Integer> path) {

	if (path == null || path.isEmpty()) {
	    return 0;
	}

	double totalDist = 0;
	
	int prevVertex = path.get(0);
	for (int i = 1; i < path.size(); i++) {
	    int curVertex = path.get(i);
	    double dist = d[prevVertex][curVertex];
	    totalDist += dist;
	    prevVertex = curVertex;
	}

	int startVertex = path.get(0);
	int endVertex = path.get(path.size() - 1);

	totalDist += d[endVertex][startVertex];

	return totalDist;
    }

    public int getVertexCount() {
	return this.vCount;
    }

    public double getDistance(int a, int b) {
	return d[a][b];
    }

    public double getPheromone(int a, int b) {
	return p[a][b];
    }

    public void setPheromone(double value, int firstIndex, int secondIndex) {
	p[firstIndex][secondIndex] = value;
    }

    public double[][] getPheromoneArray() {
	return p;
    }

    public Vertex getVertex(int index) {
	return v[index];
    }

}
