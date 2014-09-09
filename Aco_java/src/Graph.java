import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;

public class Graph {

    // optimal
    /*
     * 1 8 38 31 44 18 7 28 6 37 19 27 17 43 30 36 46 33 20 47 21 32 39 48 5 42
     * 24 10 45 35 4 26 2 29 34 41 16 22 3 23 14 25 13 11 12 15 40 9
     */

    public static final int TOP = 10;

    private int vCount = 0;

    private Vertex v[];

    /**
     * Distance array
     */
    long d[][];

    /**
     * Pheromone quantity on edge.
     */
    double p[][];

    public Graph(String path) {

	readGraphFromFile(path);
	initDistances(vCount);
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

    public void plot(List<Integer> path, int iterationCount, int antCount, 
	    String params) {

	JavaPlot p = new JavaPlot();

	PlotStyle myPlotStyle = new PlotStyle();
	myPlotStyle.setStyle(Style.LINESPOINTS);
	
	DataSetPlot s = parsePathToDataSetPlot(path);
	
	myPlotStyle.setLineWidth(1);
	
	String title = "iteration count: " + iterationCount + ", ant count: " + antCount;
	if (params != null && !params.isEmpty()) {
	    title += ", " + params;
	}
	
	s.setTitle(title);
	s.setPlotStyle(myPlotStyle);
	p.addPlot(s);
	p.newGraph();
	p.plot();
    }
    
    /**
     * If path is: 1 2 3 1 provided path must be 1 2 3.
     * @param path hamiltonian path.
     */
    public DataSetPlot parsePathToDataSetPlot(List<Integer> path) {
	
	if (path == null || path.isEmpty()) {
	    throw new IllegalArgumentException("Path that is to be plotted must"
	    	+ "be non null and non empty !");
	}
	
	int data[][] = new int[vCount + 1][2];
	
	for (int i = 0; i < path.size(); i++) {
	    
	    int index = path.get(i);
	    
	    Vertex curVertex = v[index];
	    
	    data[i][0] = curVertex.getX();
	    data[i][1] = curVertex.getY();
	}
	
	//must return to first vertex
	Vertex firstVertex = v[path.get(0)];
	
	int index = path.size();
	data[index][0] = firstVertex.getX();
	data[index][1] = firstVertex.getY();
	
	return new DataSetPlot(data);
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
		scanner.nextLine();
	    }
	    for (int i = 0; i < 2; i++) {
		scanner.next();
	    }

	    vCount = scanner.nextInt();

	    for (int i = 0; i < 3; i++) {
		scanner.nextLine();
	    }

	    v = new Vertex[vCount + TOP];

	    for (int i = 0; i < vCount; i++) {

		int id = scanner.nextInt();
		int x = scanner.nextInt();
		int y = scanner.nextInt();

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

    private void initDistances(int vertexCount) {

	d = new long[vertexCount + TOP][vertexCount + TOP];

	for (int i = 1; i <= vertexCount; i++) {
	    for (int j = i + 1; j <= vertexCount; j++) {

		long distance = euclideanDistance(v[i], v[j]);
		d[i][j] = distance;
		d[j][i] = distance;
	    }
	}
    }

    private long euclideanDistance(Vertex a, Vertex b) {

	return sqr(a.getX() - b.getX()) + sqr(a.getY() - b.getY());
    }

    private long sqr(int value) {

	return value * value;
    }

    
    /**
     * Calulates route length.
     * 
     * @param path
     *            path. If full path would be 1 2 3 1 then such path must be
     *            provided: 1 2 3
     * @return distance of hamiltonian path.
     */
    public long calcDistance(List<Integer> path) {

	

	if (path == null || path.isEmpty()) {
	    return 0;
	}

	long totalDist = 0;

	for (int i = 1; i < path.size(); i++) {

	    int curVert = path.get(i);
	    int prevVert = path.get(i - 1);
	    long dist = d[prevVert][curVert];
	    totalDist += dist;
	}

	int startVertex = path.get(0);
	int endVertex = path.get(path.size() - 1);

	totalDist += d[endVertex][startVertex];

	return totalDist;
    }

    public int getvCount() {
        return this.vCount;
    }
    
    public long getDistance(int a, int b) {
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
