package ga.framework;

import ga.framework.model.NoSolutionException;
import ga.framework.model.Problem;
import ga.framework.model.Solution;
import ga.framework.operators.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    Problem problem;
    int populationSize;
    List<EvolutionaryOperator> evolutionaryOperatorList;
    FitnessEvaluator fitnessEvaluator;
    SurvivalOperator survivalOperator;
    SelectionOperator selectorOperator;
    int evolutionSteps;
    List<Solution> solutionList = new ArrayList<>();
    List<Solution> NachkommenList = new ArrayList<>();

    public GeneticAlgorithm() {}

    public GeneticAlgorithm(Problem problem, int populationSize, List<EvolutionaryOperator> evolutionaryOperatorList,
                            FitnessEvaluator fitnessEvaluator, SurvivalOperator survivalOperator, SelectionOperator selectorOperator, int iterationNum) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.evolutionaryOperatorList = evolutionaryOperatorList;
        this.fitnessEvaluator = fitnessEvaluator;
        this.survivalOperator = survivalOperator;
        this.evolutionSteps = iterationNum;
        this.selectorOperator=selectorOperator;
    }

    public List<Solution> runOptimization() {
        try{
        List<Solution> optimizedPopulation = new ArrayList<>();

        if (allFieldInit()) {
            for (int i = 0; i < populationSize; i++) {
                solutionList.add(problem.createNewSolution());
            }
            fitnessEvaluator.evaluate(solutionList);

            for (int j = 0; j < evolutionSteps; j++) {
                Random random = new Random();
                EvolutionaryOperator evo = evolutionaryOperatorList.get(random.nextInt(evolutionaryOperatorList.size()));
                for (Solution s : solutionList) {
                    NachkommenList.add(evo.evolve(s));
                }

                fitnessEvaluator.evaluate(NachkommenList);
                solutionList.addAll(NachkommenList);
                optimizedPopulation = survivalOperator.selectPopulation(solutionList, populationSize);
            }
            return optimizedPopulation;
        }
        } catch (SurvivalException | NoSolutionException | EvolutionException e) {
            e.printStackTrace();
        }
        return null;
    }

public boolean allFieldInit(){
    return problem != null && populationSize != 0 && evolutionaryOperatorList != null && fitnessEvaluator != null && survivalOperator != null && selectorOperator != null && evolutionSteps != 0;

}
    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public List<EvolutionaryOperator> getEvolutionaryOperatorList() {
        return evolutionaryOperatorList;
    }

    public void setEvolutionaryOperatorList(List<EvolutionaryOperator> evolutionaryOperatorList) {
        this.evolutionaryOperatorList = evolutionaryOperatorList;
    }

    public FitnessEvaluator getFitnessEvaluator() {
        return fitnessEvaluator;
    }

    public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
        this.fitnessEvaluator = fitnessEvaluator;
    }

    public SurvivalOperator getSurvivalOperator() {
        return survivalOperator;
    }

    public void setSurvivalOperator(SurvivalOperator survivalOperator) {
        this.survivalOperator = survivalOperator;
    }

    public SelectionOperator getSelectorOperator() {
        return selectorOperator;
    }

    public void setSelectorOperator(SelectionOperator selectorOperator) {
        this.selectorOperator = selectorOperator;
    }
    public int getEvolutionSteps() {
        return evolutionSteps;
    }

    public void setEvolutionSteps(int evolutionSteps) {
        this.evolutionSteps = evolutionSteps;
    }
    public PopulationOfSize solve(Problem problem){
                this.setProblem(problem);
       return new PopulationOfSize(this);
    }

    public class PopulationOfSize {
        private GeneticAlgorithm instance;

        public PopulationOfSize(GeneticAlgorithm instance) {
            this.instance = instance;
        }

        public SolutionsWith withPopulationOfSize(int sizePopulation) {
            instance.setPopulationSize(sizePopulation);
            return new SolutionsWith(instance);
        }
    }
    public class SolutionsWith {
        private GeneticAlgorithm instance;

        public SolutionsWith(GeneticAlgorithm instance) {
            this.instance = instance;
        }

        public SolutionsWith evolvingSolutionsWith(EvolutionaryOperator evolutionaryOperator) {
            instance.getEvolutionaryOperatorList().add(evolutionaryOperator);
            return new SolutionsWith(instance);
        }

        public SurvivalWith evolvingSolutionsBy(FitnessEvaluator fitnessEvaluator) {
            instance.setFitnessEvaluator(fitnessEvaluator);
            return new SurvivalWith(instance);
        }
    }

    public class SurvivalWith {
        private GeneticAlgorithm instance;

        public SurvivalWith(GeneticAlgorithm instance) {
            this.instance = instance;
        }

        public SelectionWith  performingSurvivalWith(SurvivalOperator survivalOperator) {
            instance.setSurvivalOperator(survivalOperator);
            return new SelectionWith(instance);
        }

    }
    public class SelectionWith {
        private GeneticAlgorithm instance;

        public SelectionWith(GeneticAlgorithm instance) {
            this.instance = instance;
        }

        public Evolution  performingSelectionWith(SelectionOperator selectionOperator) {
            instance.setSelectorOperator(selectionOperator);
            return new Evolution(instance);
        }

    }
    public class Evolution {
        private GeneticAlgorithm instance;

        public Evolution(GeneticAlgorithm instance) {
            this.instance = instance;
        }

        public Optimization stoppingAtEvolution(int evolutionSteps) {
            instance.setEvolutionSteps(evolutionSteps);
            return new Optimization(instance);
        }

    }
    public class Optimization {
        private GeneticAlgorithm instance;

        public Optimization(GeneticAlgorithm instance) {
            this.instance = instance;
        }

        public List<Solution> runOptimization() throws Exception {

            return instance.runOptimization();
        }

    }

    public static void main(String[] args) {
        GeneticAlgorithm ga = new GeneticAlgorithm() ;
        List<Solution>res=
                ga.solve(ga.getProblem())
                .withPopulationOfSize(10)
                .evolvingSolutionsWith()
                .evolvingSolutionsBy().performingSurvivalWith()
                .performingSelectionWith().stoppingAtEvolution().runOptimization();
    }

}
