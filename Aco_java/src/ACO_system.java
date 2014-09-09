import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * The pheromone update is only done after all the ants had constructed the
 * tours and the amount of pheromone deposited by each ant is set to be a
 * function of the tour quality.
 *
 */
public class ACO_system implements AntBrainz {

    /**
     * Pheromone influence level. The higher this value is the higher increase
     * in probability that ant will choose edge with small advantage of
     * pheromone over other edges.
     */
    public static final double ALPHA = 2;

    /**
     * Randomness level. The higher this value is the more random ants edge
     * choosing pattern is.
     */
    public static final double BETA = 20;

    /**
     * Evaporation rate. Must be in range [0; 1] Used for limiting pheromone
     * accumulation on edges. if != 0 ants can forget bad paths.
     */
    public static final double EVAPORATION = 0.2;

    public static final double INITIAL_PHEROMONE = 2;

    private int antCount;
    private List<Ant> ants = new ArrayList<>();
    private Graph g;
    private int vertexCount;
    private int iterationCount;
    private static final long INF = -1;
    private long globalBestTourLength = INF;

    public ACO_system(int antCount, int iterationCount, Graph g) {
	super();

	this.iterationCount = iterationCount;
	this.antCount = antCount;
	this.g = g;
	this.vertexCount = g.getvCount();
	initPheromone();
    }

    public void initPheromone() {

	double[][] pheromone = g.getPheromoneArray();
	for (int i = 0; i < pheromone.length; i++) {
	    for (int j = i + 1; j < pheromone.length; j++) {
		setArrayValue(INITIAL_PHEROMONE, pheromone, i, j);
	    }
	}

    }

    String paramsString = "alpha: " + ALPHA + ", beta: " + BETA
	    + ", evaporation: " + EVAPORATION;

    String tmpParamsString = "";

    public void start() {

	for (int i = 0; i < iterationCount; i++) {

	    System.out.println("Iteration: " + i);

	    ants.clear();
	    initAnts();

	    for (int j = 0; j < ants.size(); j++) {

		// System.out.println("Ant: " + j);

		Ant ant = ants.get(j);

		for (int j2 = 1; j2 <= vertexCount - 1; j2++) {

		    // System.out.println("Vertex: " + j2);

		    ant.chooseAnVisitNextVertex();
		}
	    }

	    Ant bestIterationAnt = getBestPath(ants);
	    long bestIterationTourLenght = bestIterationAnt.getTourLength();

	    if (bestIterationTourLenght < globalBestTourLength
		    || globalBestTourLength == INF) {
		globalBestTourLength = bestIterationTourLenght;

		bestIterationAnt = getBestPath(ants);

		if (true) {
		    g.plot(bestIterationAnt.getTour(), i + 1, antCount,
			    tmpParamsString);
		}
	    }

	    System.out.println("Best route length: " + globalBestTourLength
		    + "");

	}
	System.out.println("Best: " + globalBestTourLength);
    }

    public Ant getBestPath(List<Ant> ants) {

	long INF = -1;
	long bestRouteLength = INF;
	Ant bestAnt = null;

	for (int i = 0; i < ants.size(); i++) {

	    Ant ant = ants.get(i);

	    if (bestRouteLength == INF || ant.getTourLength() < bestRouteLength) {

		bestRouteLength = ant.getTourLength();
		bestAnt = ant;
	    }
	}

	tmpParamsString = paramsString + ", route length: " + bestRouteLength;

	return bestAnt;
    }

    public void initAnts() {

	for (int i = 0; i < antCount; i++) {

	    // initially place ants randomly
	    int currentAntVertex = randInt(1, vertexCount);
	    // System.out.println(currentAntVertex);
	    ants.add(new Ant(g, this, vertexCount, currentAntVertex));
	}
    }

    /**
     * Pheromone trail is updated after all ant have contructed path.
     */
    public void updatePheromoneTrail() {

	double[][] pheromone = g.getPheromoneArray();

	for (int i = 1; i <= vertexCount; i++) {
	    for (int j = i + 1; j <= vertexCount; j++) {

		setArrayValue(0, pheromone, i, j);
		setArrayValue((1.0 - EVAPORATION) * g.getPheromone(i, j)
			+ bonusForUsedByAntEdge(i, j), pheromone, i, j);

	    }
	}

    }

    public void setArrayValue(double value, double arr[][], int firstIndex,
	    int secondIndex) {

	arr[firstIndex][secondIndex] = value;
	arr[secondIndex][firstIndex] = value;
    }

    public double bonusForUsedByAntEdge(int v1, int v2) {

	double bonus = 0;
	for (int i = 0; i < ants.size(); i++) {

	    Ant ant = ants.get(i);

	    if (ant.isEdgeVisited(v1, v2)) {
		bonus = bonus + 1.0 / ant.getTourLength();
	    }
	}

	return bonus;
    }

    public int chooseNextVertexForAnt(List<Integer> availableVertexes,
	    int currentVertexIndex) {

	double prob[] = new double[availableVertexes.size()];

	double prevProb = 0;

	for (int i = 0; i < availableVertexes.size(); i++) {

	    int nextVertexIndex = availableVertexes.get(i);
	    double lowerPart = lowerPart(currentVertexIndex, availableVertexes);
	    if (lowerPart > 0) {
		prob[i] = prevProb
			+ upperPart(currentVertexIndex, nextVertexIndex)
			/ lowerPart;
	    } else {
		prob[i] = prevProb;
	    }

	    prevProb = prob[i];
	}

	double error = 0.000000001;
	// this variable is primary used for desiding if there was hit
	double hitDecisionRandonNumber = getRandomNumber();
	int decisionIndex = -1;

	boolean decisionWasMade = false;

	while (!decisionWasMade) {

	    for (int i = 0; i < availableVertexes.size(); i++) {

		decisionWasMade = isHit(hitDecisionRandonNumber, prob[i], error);

		if (decisionWasMade) {
		    decisionIndex = availableVertexes.get(i);
		    break;
		}
	    }

	    if (error < 0.01) {
		error *= 10;
	    }

	}

	return decisionIndex;
    }

    public static double getRandomNumber() {
	Random random = new Random();
	return random.nextDouble();
    }

    /**
     * 
     * @return true if currentProb <= upperLimit
     */
    public boolean isHit(double currentProb, double upperLimit, double error) {

	if (currentProb < upperLimit
		|| Math.abs(currentProb - upperLimit) < error) {
	    return true;
	}

	return false;
    }

    /**
     * Calculates upper part of forumula for probability of ant choosing next
     * vertex.
     */
    public double upperPart(int fromVertexIndex, int toVertexIndex) {

	double inverseDistanceRaisedToPower = getInverseDistanceRaisedToPower(
		fromVertexIndex, toVertexIndex);
	double pheromoneRaisedToPower = Math.pow(
		g.getPheromone(fromVertexIndex, toVertexIndex), ALPHA);

	return pheromoneRaisedToPower * inverseDistanceRaisedToPower;
    }

    /**
     * Calculates lower part of forumula for probability of ant choosing next
     * vertex.
     */
    public double lowerPart(int fromVertexIndex, List<Integer> availableVertexes) {

	double sum = 0;

	for (int i = 0; i < availableVertexes.size(); i++) {

	    int toVertexIndex = availableVertexes.get(i);
	    double inverseDistanceRaisedToPower = getInverseDistanceRaisedToPower(
		    fromVertexIndex, toVertexIndex);
	    double pheromoneRaisedToPower = Math.pow(
		    g.getPheromone(fromVertexIndex, toVertexIndex), ALPHA);

	    sum = sum + pheromoneRaisedToPower * inverseDistanceRaisedToPower;

	}

	return sum;
    }

    public double getInverseDistanceRaisedToPower(int fromVertexIndex,
	    int toVertexIndex) {

	double inverseDistance = 1.0 / g.getDistance(fromVertexIndex,
		toVertexIndex);
	double inverseDistanceRaisedToPower = Math.pow(inverseDistance, BETA);

	return inverseDistanceRaisedToPower;

    }

    private void updateTrail() {

    }

    public int getAntCount() {
	return this.antCount;
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

}
