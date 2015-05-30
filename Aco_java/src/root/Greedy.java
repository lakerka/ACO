package root;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Greedy implements AntBrainz {

    public static final int TOP = 10;

    private int antCount;
    private List<Ant> ants = new ArrayList<>();
    /**
     * Inverse distance raised to power.
     */
    private Graph g;
    private int vertexCount;
    private int iterationCount;
    private static final long INF = -1;
    private double globalBestTourLength = INF;

    public Greedy(int antCount, int iterationCount, Graph g) {

	this.iterationCount = iterationCount;
	this.antCount = antCount;
	this.g = g;
	this.vertexCount = g.getVertexCount();

    }

    public void start() {

	for (int i = 1; i <= iterationCount; i++) {

	    ants.clear();
	    initAnts();

	    for (int j = 0; j < ants.size(); j++) {

		Ant ant = ants.get(j);

		for (int j2 = 1; j2 <= vertexCount - 1; j2++) {
		    ant.chooseAnVisitNextVertex();
		}
	    }

	    Ant bestIterationAnt = getBestPath(ants);
	    double bestIterationTourLenght = bestIterationAnt.getTourLength();

	    if (bestIterationTourLenght < globalBestTourLength
		    || globalBestTourLength == INF) {
		globalBestTourLength = bestIterationTourLenght;

		bestIterationAnt = getBestPath(ants);

		g.plot(bestIterationAnt.getTour(), true, i, antCount, null);

		System.out.println("Iteration: " + i);
		System.out.println("Best route length: " + globalBestTourLength
			+ "");
	    }

	}
	System.out.println("DONE. BEST: " + globalBestTourLength);
    }
    
    public void initAnts() {

 	for (int i = 0; i < antCount; i++) {

 	    // initially place ants randomly
 	    int currentAntVertex = randInt(1, vertexCount);
 	    // System.out.println(currentAntVertex);
 	    ants.add(new Ant(g, this, vertexCount, currentAntVertex));
 	}
     }

    public static int randInt(int min, int max) {

	// NOTE: Usually this should be a field rather than a method
	// variable so that it is not re-seeded every call.
	Random rand = new Random();

	// nextInt is normally exclusive of the top value,
	// so add 1 to make it inclusive
	int randomNum = rand.nextInt((max - min) + 1) + min;

	return randomNum;
    }

    @Override
    public int chooseNextVertexForAnt(List<Integer> availableVertexes,
	    int currentVertexIndex) {

	int leastDistance = (int) (1e9);
	int bestVertex = -1;

	for (int i = 0; i < availableVertexes.size(); i++) {

	    int nextVertexIndex = availableVertexes.get(i);

	    double dist = g.getDistance(currentVertexIndex, nextVertexIndex);

	    if (dist < leastDistance) {
		dist = leastDistance;
		bestVertex = nextVertexIndex;
	    }
	}
	return bestVertex;
    }

    public Ant getBestPath(List<Ant> ants) {

	double bestRouteLength = 1e9;
	Ant bestAnt = null;

	for (int i = 0; i < ants.size(); i++) {

	    Ant ant = ants.get(i);

	    if (i == 0 || ant.getTourLength() < bestRouteLength) {
		bestRouteLength = ant.getTourLength();
		bestAnt = ant;
	    }
	}
	return bestAnt;
    }

}
