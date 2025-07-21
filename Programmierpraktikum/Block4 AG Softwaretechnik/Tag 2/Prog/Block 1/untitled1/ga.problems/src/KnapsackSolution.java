import ga.framework.model.Problem;
import ga.framework.model.Solution;

import java.util.ArrayList;
import java.util.List;

public class KnapsackSolution extends Solution {

    List<KnapsackProblem.Item> knapsackProblemList;
  KnapsackSolution(Problem problem,List<KnapsackProblem.Item>knapsackProblems){
      super(problem);
      this.knapsackProblemList=knapsackProblems;
  }


  KnapsackSolution getCoppy (){
    return new KnapsackSolution(this.getProblem(),knapsackProblemList);
  }

    public int getSumOfCost(){
      int cost=0;
        for (KnapsackProblem.Item item:knapsackProblemList) {
            cost+= item.getCost();

        }
        return cost;
    }
    public int getCurrentItemSize(){
        int cost=0;
        for (KnapsackProblem.Item item:knapsackProblemList) {
            cost+= item.getSize();

        }
        return cost;
    }


    public List<KnapsackProblem.Item> getKnapsackProblemList() {
        return knapsackProblemList;
    }

    public void setKnapsackProblemList(List<KnapsackProblem.Item> knapsackProblemList) {
        this.knapsackProblemList = knapsackProblemList;
    }
}
