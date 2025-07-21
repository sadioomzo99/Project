import ga.framework.GeneticAlgorithm;
import ga.framework.model.Solution;
import ga.framework.operators.TopSurvival;
import ga.framework.operators.TournamentSelection;

import java.util.ArrayList;
import java.util.List;

public class ConcreteProblem {
    public static void main(String[] args) throws Exception {
        KnapsackProblem.Item g1=new KnapsackProblem.Item(5,10);
        KnapsackProblem.Item g2=new KnapsackProblem.Item(4,8);
        KnapsackProblem.Item g3=new KnapsackProblem.Item(4,6);
        KnapsackProblem.Item g4=new KnapsackProblem.Item(4,4);
        KnapsackProblem.Item g5=new KnapsackProblem.Item(3,7);
        KnapsackProblem.Item g6=new KnapsackProblem.Item(3,4);
        KnapsackProblem.Item g7=new KnapsackProblem.Item(2,6);
        KnapsackProblem.Item g8=new KnapsackProblem.Item(2,3);
        KnapsackProblem.Item g9=new KnapsackProblem.Item(1,3);
        KnapsackProblem.Item g10=new KnapsackProblem.Item(1,1);
        List<KnapsackProblem.Item> list =new ArrayList<KnapsackProblem.Item>();
        list.add(g1);
        list.add(g2);
        list.add(g3);
        list.add(g4);
        list.add(g5);
        list.add(g6);
        list.add(g7);
        list.add(g8);
        list.add(g9);
        list.add(g10);
        KnapsackProblem knapsackProblem=new KnapsackProblem(11,list);
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();
       List<Solution> res= geneticAlgorithm.solve(knapsackProblem)
                .withPopulationOfSize(4)
                .evolvingSolutionsWith(new KnapsackMutation())
                .evolvingSolutionsBy(new KnapsackFitnessEvaluator())
                .performingSurvivalWith(new TopSurvival(2))
                .performingSelectionWith(new TournamentSelection())
                .stoppingAtEvolution(10)
                .  runOptimization();

            for (Solution s :res){
                KnapsackSolution knapsackSolution=(KnapsackSolution) s;
                System.out.println("Fitness:" +s.getFitness()+"\n" +"item:"+ ((KnapsackSolution)s).getKnapsackProblemList());
            }
    }
}
