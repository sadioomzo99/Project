import ga.framework.model.Solution;
import ga.framework.operators.EvolutionException;
import ga.framework.operators.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class KnapsackMutation implements EvolutionaryOperator {
    List<KnapsackProblem.Item> items;

    @Override
    public Solution evolve(Solution solution) throws EvolutionException {
        KnapsackSolution knapsackSolution = new RemoveRandomItem((KnapsackSolution) solution).removeRandomItem();
        if (knapsackSolution == null) {
            knapsackSolution = new AddRandomItem((KnapsackSolution) solution).addRandomItem();
        } if (knapsackSolution == null) {
            throw new EvolutionException("No Solution was found");
        }

        return knapsackSolution;

    }

    class AddRandomItem {
        KnapsackSolution knapsackSolution;
        Random random = new Random();

        public AddRandomItem(KnapsackSolution knapsackSolution) {
            this.knapsackSolution = knapsackSolution;
        }

        public KnapsackSolution addRandomItem() {
            KnapsackSolution knapsackSolutionCoppy=knapsackSolution.getCoppy();
            KnapsackProblem knapsackProblem = (KnapsackProblem) knapsackSolutionCoppy.getProblem();
            List<KnapsackProblem.Item> itemInBackPack = new ArrayList<>(knapsackSolutionCoppy.getKnapsackProblemList());
            List<KnapsackProblem.Item> itemToInsert = new ArrayList<>(knapsackProblem.getItems());
            itemToInsert.removeAll(itemInBackPack);
            List<KnapsackProblem.Item> availableItem = new ArrayList<>(itemToInsert); //not Selected yet
            availableItem= availableItem.stream().filter(x->x.size +knapsackSolutionCoppy.getCurrentItemSize() < knapsackProblem.getBackPackSize())
                    .collect(Collectors.toList());
            if (!availableItem.isEmpty()) {
                KnapsackProblem.Item chosenItem = availableItem.get(random.nextInt(availableItem.size()));
                itemInBackPack.add(chosenItem);
                availableItem.remove(chosenItem);
                return new KnapsackSolution(knapsackSolutionCoppy.getProblem(), itemInBackPack);
            }else {
                return null;
            }

        }
    }


    class RemoveRandomItem {
        Random random = new Random();
        KnapsackSolution knapsackSolution;

        public RemoveRandomItem(KnapsackSolution knapsackSolution) {
            this.knapsackSolution = knapsackSolution;
        }

        public KnapsackSolution removeRandomItem() {
            KnapsackSolution knapsackSolutionCoppy = knapsackSolution.getCoppy();
            List<KnapsackProblem.Item> itemInBackpack = new ArrayList<>(knapsackSolutionCoppy.getKnapsackProblemList());
            if (!itemInBackpack.isEmpty()) {
                KnapsackProblem.Item remove = itemInBackpack.get(random.nextInt(itemInBackpack.size()));
                itemInBackpack.remove(remove);
                return new KnapsackSolution(knapsackSolutionCoppy.getProblem(), itemInBackpack);
            } else {
                return null;
            }
        }
    }
}
