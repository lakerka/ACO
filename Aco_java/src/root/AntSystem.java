package root;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jfree.ui.RefineryUtilities;

/**
 * The pheromone update is only done after all the ants had constructed the
 * tours and the amount of pheromone deposited by each ant is set to be a
 * function of the tour quality.
 */
public class AntSystem implements AntBrainz {

    public static final boolean GENERATE_SUMMARY = false;
    public static final boolean DISPAY_PLOT = false;

    /**
     * Pheromone influence level. The higher this value is the higher increase
     * in probability that ant will choose edge with small advantage of
     * pheromone over other edges.
     */
    public double alpha = 2;

    /**
     * Randomness level. The higher this value is the more random ants edge
     * choosing pattern is.
     */
    public double beta = 12;

    /**
     * Evaporation rate. Must be in range [0; 1] Used for limiting pheromone
     * accumulation on edges. if != 0 ants can forget bad paths.
     */
    public double evaporation = 0.5;

    public double initialPheromone = 1.0;

    private int antCount;
    private List<Ant> ants = new ArrayList<Ant>();
    private List<Worker> workers;
    
    /**
     * Inverse distance raised to power.
     */
    private double euristic[][];
    private Graph graph;
    private int vertexCount;
    private int iterationCount;
    private int threadCount;
    private static final double INF = 1e9;
    private double globalBestTourLength = INF;
    private Visualizer visualizer;

    public AntSystem(double alpha, double beta, double evaporation,
            double initialPheromone, int antCount,
            int iterationCount, int threadCount,
            Graph graph) {

        this.alpha = alpha;
        this.beta = beta;
        this.evaporation = evaporation;
        this.initialPheromone = initialPheromone;
        
	this.iterationCount = iterationCount;
	this.threadCount = threadCount;
	this.antCount = antCount;
	this.graph = graph;
	this.vertexCount = graph.getVertexCount();

	final int TOP = 10;
	euristic = new double[vertexCount + TOP][vertexCount + TOP];
	initPheromone();
	initEuristic();

        ants.clear();
        this.ants = getNewAnts(antCount);
        this.workers = getNewWorkers(ants);
        
	
	if (DISPAY_PLOT) {
        	visualizer = new Visualizer("Ant system", graph);
        	visualizer.pack();
        	RefineryUtilities.centerFrameOnScreen(visualizer);
        	visualizer.setVisible(DISPAY_PLOT);  
        }
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

	double[][] pheromone = graph.getPheromoneArray();
	for (int i = 0; i < pheromone.length; i++) {
	    for (int j = i + 1; j < pheromone.length; j++) {
		setArrayValue(initialPheromone, pheromone, i, j);
	    }
	}
    }

    String paramsString = "alpha: " + alpha + ", beta: " + beta
	    + ", evaporation: " + evaporation;

    String tmpParamsString = "";
    
    public void start() {
        
	for (int i = 1; i <= iterationCount; i++) {
//            System.out.println("Iteration: " + i);

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadCount,
                    threadCount, 0, TimeUnit.NANOSECONDS,
                    new LinkedBlockingQueue<Runnable>());
           
            
            for (Worker worker : workers) {
                threadPoolExecutor.execute(worker);
            }
            threadPoolExecutor.shutdown();
            
            try {
                threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
	    updatePheromoneTrail();

	    Ant bestIterationAnt = getAntWithShortestPath(ants);
	    double bestIterationTourLenght = bestIterationAnt.getTourLength();

	    if (bestIterationTourLenght < globalBestTourLength
		    || globalBestTourLength == INF) {
		globalBestTourLength = bestIterationTourLenght;

		bestIterationAnt = getAntWithShortestPath(ants);
		
		if (DISPAY_PLOT) {
		    DecimalFormat twoDigitsPrecisionFormat = new DecimalFormat("#.##");
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

    private List<Ant> getNewAnts(int antCount) {
        List<Ant> ants = new ArrayList<Ant>();
	for (int i = 0; i < antCount; i++) {
	    // initially place ants randomly
	    int currentAntVertex = randInt(1, vertexCount);
	    ants.add(new Ant(graph, this, vertexCount, currentAntVertex));
	}
	return ants;
    }
    
    public List<Worker> getNewWorkers(List<Ant> ants) {
        List<Worker> workers = new ArrayList<Worker>();
        for (Ant ant : ants) {
            Worker worker = new Worker(ant, graph);
            workers.add(worker);
        }
        return workers;
    }

    /**
     * Pheromone trail is updated when all ants have tour of visited vertexes.
     */
    public void updatePheromoneTrail() {

	double[][] pheromone = graph.getPheromoneArray();

	for (int i = 1; i <= vertexCount; i++) {
	    for (int j = i + 1; j <= vertexCount; j++) {

		setArrayValue(0, pheromone, i, j);
		setArrayValue((1.0 - evaporation) * graph.getPheromone(i, j)
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

//        if (true) {
//            return availableVertexes.get(randInt(0, availableVertexes.size() - 1));
//        }
        
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

	
	double error = 0.000000001d;

	// this variable is primary used for deciding if there was hit
	double hitDecisionRandonNumber = getRandomNumber();
	int decisionIndex = -1;
	boolean decisionWasMade = false;

        final double EPS = 0.01;
        boolean allHaveVeryLowProbability = true;
        
        for (int i = 0; i < availableVertexes.size(); i++) {
            double probToBeChosen = prob[i];
            if (probToBeChosen > EPS) {
                allHaveVeryLowProbability = false;
                break;
            }
        }
        
        if (allHaveVeryLowProbability) {
            decisionIndex = availableVertexes.get(randInt(0, availableVertexes.size() - 1));
            decisionWasMade = true;
        }
        
	while (!decisionWasMade) {
	    for (int i = 0; i < availableVertexes.size(); i++) {

		decisionWasMade = isHit(hitDecisionRandonNumber, prob[i], error);
		if (decisionWasMade) {
		    decisionIndex = availableVertexes.get(i);
		    break;
		}
	    }
	    error *= 10;
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

	double inverseDistanceRaisedToPower = euristic[fromVertexIndex][toVertexIndex];
	double pheromone = graph.getPheromone(fromVertexIndex, toVertexIndex);
	double pheromoneRaisedToPower = Math.pow(pheromone, alpha);
	
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
		    graph.getPheromone(fromVertexIndex, toVertexIndex), alpha);

	    sum = sum + pheromoneRaisedToPower * inverseDistanceRaisedToPower;
	}
	return sum;
    }

    public double getInverseDistanceRaisedToPower(int fromVertexIndex,
	    int toVertexIndex) {

	double inverseDistance = 1.0 / graph.getDistance(fromVertexIndex,
		toVertexIndex);
	double inverseDistanceRaisedToPower = Math.pow(inverseDistance, beta);

	return inverseDistanceRaisedToPower;
    }

    public int getAntCount() {
	return this.antCount;
    }

    public static int randInt(int min, int max) {
	Random rand = new Random();
	int randomNum = rand.nextInt((max - min) + 1) + min;
	return randomNum;
    }

}
