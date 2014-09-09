import java.util.ArrayList;
import java.util.List;

public class Ant {

    public static final int TOP = 10;

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
    int tourLength = 0;
    Graph graph;

    public Ant(Graph graph, AntBrainz antBrainz, int vertexCount,
	    int currentVertex) {
	super();

	this.tabu = new boolean[vertexCount + TOP];
	this.edgeVisited = new boolean[vertexCount + TOP][vertexCount + TOP];
	this.graph = graph;
	this.antBrainz = antBrainz;
	this.vertexCount = vertexCount;
	this.startingVertex = currentVertex;
	this.curVertex = currentVertex;
	this.visitedCount = 1;
	this.tour.clear();
	this.tour.add(currentVertex);
	
	for (int i = 1; i <= vertexCount; i++) {

	    if (i != currentVertex) {
		availableVertexes.add(i);
	    } else {
	    }
	}
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

	// remove vertex from available vertexes list
	for (int i = availableVertexes.size() - 1; i >= 0; i--) {

	    int vertexIndex = availableVertexes.get(i);
	    
	    if (tabu[vertexIndex]) {
		availableVertexes.remove(i);
	    }
	}

	curVertex = newCurVertex;

    }

    public boolean isEdgeVisited(int v1, int v2) {

	return edgeVisited[v1][v2];
    }

    public long getTourLength() {

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

}
