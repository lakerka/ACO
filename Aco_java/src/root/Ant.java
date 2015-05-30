package root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ant {

    /**
     * Array for flaging that vertex has been visited.
     */
    boolean tabu[];
    boolean edgeVisited[][];
    List<Integer> availableVertexes = new ArrayList<>();
    List<Integer> tour = new ArrayList<>();
    
    int startingVertex;
    int vertexCount;
    int visitedCount;
    int curVertex;
    AntBrainz antBrainz;
    double tourLength = 0;
    Graph graph;

    public Ant(Graph graph, AntBrainz antBrainz, int vertexCount,
	    int startingVertex) {
	super();
	final int TOP = 10;
	this.tabu = new boolean[vertexCount + TOP];
	this.edgeVisited = new boolean[vertexCount + TOP][vertexCount + TOP];
	this.graph = graph;
	this.antBrainz = antBrainz;
	this.vertexCount = vertexCount;
	this.startingVertex = startingVertex;
	this.curVertex = startingVertex;
	this.visitedCount = 1;
	this.tour.clear();
	this.tour.add(startingVertex);
	
	for (int i = 1; i <= vertexCount; i++) {
	    if (i != startingVertex) {
		availableVertexes.add(i);
	    }
	}
	addVertexToTabu(startingVertex);
    }

    /**
     * Changes current vertex and add edge length of added tour.
     */
    public void chooseAnVisitNextVertex() {

	int newCurVertex = antBrainz.chooseNextVertexForAnt(availableVertexes,
		curVertex);

	tourLength += graph.getDistance(curVertex, newCurVertex);
	
	tour.add(newCurVertex);
	edgeVisited[curVertex][newCurVertex] = true;
	edgeVisited[newCurVertex][curVertex] = true;
        tabu[newCurVertex] = true;
	availableVertexes.remove(new Integer(newCurVertex));
	curVertex = newCurVertex;
    }

    public boolean isEdgeVisited(int v1, int v2) {

	return edgeVisited[v1][v2];
    }

    public double getTourLength() {

	return tourLength + graph.getDistance(startingVertex, curVertex);
    }

    public List<Integer> getTour() {
	
	return this.tour;
    }
    
    /**
     * Makes vertex tabu thus preventing ant from returning to it.
     */
    public void addVertexToTabu(int v) {
	tabu[v] = true;
    }

    public void removeVertexFromTabu(int v) {
	tabu[v] = false;
    }
    
    public void reset(int startingVertex) {
        Arrays.fill(tabu, false);
        for (boolean[] row: edgeVisited) {
            Arrays.fill(row, false);
        }
        
        this.startingVertex = startingVertex;
        this.curVertex = startingVertex;
        this.visitedCount = 1;
        this.tour.clear();
        this.tour.add(startingVertex);
        this.tourLength = 0;
        
        this.availableVertexes.clear();
        for (int i = 1; i <= vertexCount; i++) {
            this.availableVertexes.add(i);
        }
        this.availableVertexes.remove(new Integer(startingVertex));
        tabu[startingVertex] = true;
    }

}
