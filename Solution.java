import java.util.ArrayList;

public class Solution {

    private ArrayList<SolutionStep> solutionSteps = new ArrayList<SolutionStep>();

    public void addSolutionStep(SolutionStep solutionStep) {
	solutionSteps.add(solutionStep);
    }

    public ArrayList<SolutionStep> getSolutionSteps() {
	return solutionSteps;
    }

}
