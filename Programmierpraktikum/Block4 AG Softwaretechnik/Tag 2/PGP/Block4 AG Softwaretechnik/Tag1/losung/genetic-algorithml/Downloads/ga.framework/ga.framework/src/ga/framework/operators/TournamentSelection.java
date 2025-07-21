package ga.framework.operators;

import ga.framework.model.Solution;

import java.util.List;
import java.util.Random;

public class TournamentSelection implements SelectionOperator {
    @Override
    public Solution selectParent(List<Solution> candidates) {
        Random random = new Random();
        Solution s1 = candidates.get(random.nextInt(candidates.size()));
        Solution s2 = candidates.get(random.nextInt(candidates.size()));

        if (s1.equals(s2)) {
            return s1;
        } else if (s1.getFitness() > s2.getFitness()) {
            return s1;
        } else if (s1.getFitness() < s2.getFitness()) {
            return s2;
        } else if (s1.getFitness() == s2.getFitness()) {
            return s1;
        }
        return null;
    }
}
