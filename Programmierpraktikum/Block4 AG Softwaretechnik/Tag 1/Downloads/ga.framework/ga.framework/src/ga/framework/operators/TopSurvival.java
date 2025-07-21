package ga.framework.operators;

import ga.framework.model.Solution;
import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TopSurvival implements SurvivalOperator{
    int k;
TopSurvival(int k){
    this.k=k;
}
    @Override
    public List<Solution> selectPopulation(List<Solution> candidates, int populationSize) throws SurvivalException {
      List<Solution>list =new ArrayList<>();
        List<Solution> sortedCandidates = candidates.stream().sorted(Comparator.comparing(Solution::getFitness).reversed()).collect(Collectors.toList());

        if(populationSize>k){
          for(int i=0;i<k;i++){
              list.add(sortedCandidates.get(i));
          }
          for(int j=0; j<(populationSize-k);j++){
              Random random = new Random();
              list.add(sortedCandidates.get(random.nextInt(sortedCandidates.size())));
          }
       }else{
            throw new SurvivalException("k größer als populationSize");

        }
        return list;
    }
}
