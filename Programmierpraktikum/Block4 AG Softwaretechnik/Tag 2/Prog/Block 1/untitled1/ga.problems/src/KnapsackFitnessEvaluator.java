import ga.framework.model.Solution;
import ga.framework.operators.FitnessEvaluator;

import java.util.List;

public class KnapsackFitnessEvaluator implements FitnessEvaluator {


    @Override
    public void evaluate(List<Solution> population) {

        population.stream().forEach(solution->{
            if(solution==null){
                return;
            }
                KnapsackSolution knapsackSolution =(KnapsackSolution)solution;
                solution.setFitness(knapsackSolution.getSumOfCost());

        });
    }
}
