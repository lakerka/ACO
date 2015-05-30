package root;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.ui.RefineryUtilities;

/**
 * The pheromone update is only done after all the ants had constructed the
 * tours and the amount of pheromone deposited by each ant is set to be a
 * function of the tour quality.
 */
public class AntSystem implements AntBrainz {

    public static final int TOP = 10;

    public static final boolean DISPAY_PLOT = true;

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
    public static final double BETA = 12;

    /**
     * Evaporation rate. Must be in range [0; 1] Used for limiting pheromone
     * accumulation on edges. if != 0 ants can forget bad paths.
     */
    public static final double EVAPORATION = 0.5;

    public static final double INITIAL_PHEROMONE = 1.0;

    private int antCount;
    private List<Ant> ants = new ArrayList<>();
    /**
     * Inverse distance raised to power.
     */
    private double euristic[][];
    private Graph g;
    private int vertexCount;
    private int iterationCount;
    private static final double INF = 1e9;
    private double globalBestTourLength = INF;
    final Visualizer visualizer;

    public AntSystem(int antCount, int iterationCount, Graph g) {
	super();

	this.iterationCount = iterationCount;
	this.antCount = antCount;
	this.g = g;
	this.vertexCount = g.getVertexCount();
	euristic = new double[vertexCount + TOP][vertexCount + TOP];
	initPheromone();
	initEuristic();

	visualizer = new Visualizer("Ant system", g);
	visualizer.pack();
	RefineryUtilities.centerFrameOnScreen(visualizer);
	visualizer.setVisible(DISPAY_PLOT);

    }

    public void initEuristic() {

	if (vertexCount <= 0) {
	    throw new IllegalStateException("Vertex count must be initialize!");
	}

	for (int i = 1; i <= vertexCount; i++) {
	    for (int j = i + 1; j <= vertexCount; j++) {

		double euristicValue = getInverseDistanceRaisedToPower(i, j);
		euristic[i][j] = euristicValue;
		euristic[j][i] = euristicValue;
	    }
	}
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

    List<SummaryElement> summaryElements = new ArrayList<SummaryElement>();

    public void start() {

        DecimalFormat twoDigitsPrecisionFormat = new DecimalFormat("#.##");
	summaryElements.clear();

	for (int i = 1; i <= iterationCount; i++) {

	    ants.clear();
	    initAnts();

	    for (int j = 0; j < ants.size(); j++) {

		Ant ant = ants.get(j);

		for (int j2 = 1; j2 <= vertexCount - 1; j2++) {
		    ant.chooseAnVisitNextVertex();
		}
	    }

	    Ant bestIterationAnt = getAntWithShortestPath(ants);
	    double bestIterationTourLenght = bestIterationAnt.getTourLength();

	    if (bestIterationTourLenght < globalBestTourLength
		    || globalBestTourLength == INF) {
		globalBestTourLength = bestIterationTourLenght;

		bestIterationAnt = getAntWithShortestPath(ants);

		summaryElements.add(new SummaryElement(i, bestIterationAnt
			.getTourLength()));

		if (DISPAY_PLOT) {
		    // g.plot(bestIterationAnt.getTour(), true, i, antCount,
		    // tmpParamsString);
		    
		    tmpParamsString = paramsString + ", route length: "
		    + twoDigitsPrecisionFormat.format(bestIterationTourLenght);
		    visualizer.plot(bestIterationAnt.getTour(), true, tmpParamsString);

		}

		System.out.println("Iteration: " + i);
		System.out.println("Best route length: " + globalBestTourLength
			+ "");
	    }

	}
	System.out.println("DONE. BEST: " + globalBestTourLength);
	processSummary();
    }

    public void processSummary() {

	SummaryElement bestSummaryElement = summaryElements.get(summaryElements
		.size() - 1);
	double bestValue = bestSummaryElement.getEvaluation();

	double _50 = bestValue * 0.5 + bestValue;
	double _75 = bestValue * 0.25 + bestValue;
	double _90 = bestValue * 0.1 + bestValue;
	double _95 = bestValue * 0.05 + bestValue;
	double _98 = bestValue * 0.02 + bestValue;
	double _100 = bestValue;

	String summary = "";
	int index = 0;

	int summaryIndex = 1;
	for (index = 0; index < summaryElements.size(); index++) {

	    SummaryElement summaryEl = summaryElements.get(index);
	    double summaryEval = summaryEl.getEvaluation();

	    if (summaryEval <= _50) {
		summary = summary + "[" + summaryIndex + "?" + summaryEval
			+ " <= " + _50 + " iter: " + summaryEl.getIteration()
			+ "]";
		break;
	    }
	}

	summaryIndex += 1;
	while (index < summaryElements.size()) {

	    double val = _75;

	    SummaryElement summaryEl = summaryElements.get(index);
	    double summaryEval = summaryEl.getEvaluation();

	    if (summaryEval <= val) {
		summary = summary
			+ getSummaryStr(summaryIndex, summaryEval, val,
				summaryEl.getIteration());
		break;
	    }
	    index++;
	}

	summaryIndex += 1;
	while (index < summaryElements.size()) {

	    double val = _90;

	    SummaryElement summaryEl = summaryElements.get(index);
	    double summaryEval = summaryEl.getEvaluation();

	    if (summaryEval <= val) {
		summary = summary
			+ getSummaryStr(summaryIndex, summaryEval, val,
				summaryEl.getIteration());
		break;
	    }

	    index++;
	}

	summaryIndex += 1;
	while (index < summaryElements.size()) {

	    double val = _95;

	    SummaryElement summaryEl = summaryElements.get(index);
	    double summaryEval = summaryEl.getEvaluation();

	    if (summaryEval <= val) {
		summary = summary
			+ getSummaryStr(summaryIndex, summaryEval, val,
				summaryEl.getIteration());
		break;
	    }

	    index++;
	}

	summaryIndex += 1;
	while (index < summaryElements.size()) {

	    double val = _98;

	    SummaryElement summaryEl = summaryElements.get(index);
	    double summaryEval = summaryEl.getEvaluation();

	    if (summaryEval <= val) {
		summary = summary
			+ getSummaryStr(summaryIndex, summaryEval, val,
				summaryEl.getIteration());
		break;
	    }

	    index++;
	}

	summaryIndex += 1;
	while (index < summaryElements.size()) {

	    double val = _100;

	    SummaryElement summaryEl = summaryElements.get(index);
	    double summaryEval = summaryEl.getEvaluation();

	    if (summaryEval <= val) {
		// summary = summary + "[" + summaryIndex + "?" + summaryEval +
		// " <= " + val + " iter: "
		// + summaryEl.getIteration() + "]";
		summary = summary
			+ getSummaryStr(summaryIndex, summaryEval, val,
				summaryEl.getIteration());
		break;
	    }

	    index++;
	}

	summary += "\n" + bestValue / 426.0;

	System.out.println(summary);
    }

    public String getSummaryStr(int summaryIndex, double summaryEval, double val,
	    int iteration) {

	return "\n" + "[" + summaryIndex + "?" + summaryEval + " <= " + val
		+ " iter: " + iteration + "]";
    }

    public Ant getAntWithShortestPath(List<Ant> ants) {

	double bestRouteLength = INF;
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

    @Override
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

	validateVertexIndex(fromVertexIndex);
	validateVertexIndex(toVertexIndex);

	double inverseDistanceRaisedToPower = euristic[fromVertexIndex][toVertexIndex];
	double pheromoneRaisedToPower = Math.pow(
		g.getPheromone(fromVertexIndex, toVertexIndex), ALPHA);

	return pheromoneRaisedToPower * inverseDistanceRaisedToPower;
    }

    public void validateVertexIndex(int index) {
	if (index <= 0) {
	    throw new IllegalStateException("Vertex index is "
		    + "must be greater than zero!");
	}
	if (index > vertexCount) {
	    throw new IllegalStateException("Vertex index is higher "
		    + "than number of vertexes");
	}
    }

    /**
     * Calculates lower part of forumula for probability of ant choosing next
     * vertex.
     */
    public double lowerPart(int fromVertexIndex, List<Integer> availableVertexes) {

	validateVertexIndex(fromVertexIndex);

	double sum = 0;

	for (int i = 0; i < availableVertexes.size(); i++) {

	    int toVertexIndex = availableVertexes.get(i);
	    validateVertexIndex(toVertexIndex);

	    double inverseDistanceRaisedToPower = euristic[fromVertexIndex][toVertexIndex];
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
