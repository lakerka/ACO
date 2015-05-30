package root;

public class SummaryElement {
    
     int iteration;
     double evaluation;
  
    
    public SummaryElement(int iteration, double value) {
	
	this.iteration = iteration;
	this.evaluation = value;
    }


    public int getIteration() {
        return this.iteration;
    }


    public double getEvaluation() {
        return this.evaluation;
    }
}
