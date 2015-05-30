package root;

import java.util.Random;

public class Worker implements Runnable {

    private Graph graph;
    private Ant ant;
    
    public Worker(Ant ant, Graph graph) {
        super();
        this.ant = ant;
        this.graph = graph;
    }
    
    public Ant getAnt() {
        return this.ant;
    }
    
    @Override
    public void run() {
        int vertexCount = graph.getVertexCount();
        int randomVertex = randInt(1, vertexCount);
        ant.reset(randomVertex);
        for (int k = 1; k <= vertexCount - 1; k++) {
            ant.chooseAnVisitNextVertex();
        }
    }

    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
