import ga.framework.model.NoSolutionException;
import ga.framework.model.Problem;
import ga.framework.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KnapsackProblem implements Problem {
    int backPackSize;
    List<Item> items;

    public KnapsackProblem(int backPackSize, List<Item> items) {
        this.backPackSize = backPackSize;
        this.items = items;
    }

    @Override
    public Solution createNewSolution() throws NoSolutionException {
        List<Item> backPackItem= new ArrayList<>();
        List<Item> itemToInsert=new ArrayList<>(items);
        int currentSize =0;
        Random random=new Random();
        while (currentSize<backPackSize && !itemToInsert.isEmpty()){
            Item currentItem=itemToInsert.get(random.nextInt(itemToInsert.size()));
            if(currentItem.size +currentSize<backPackSize){
                backPackItem.add(currentItem);
                currentSize+=currentItem.size;
            }
            itemToInsert.remove(currentItem);

        }
        if(!backPackItem.isEmpty()){
            return new KnapsackSolution(this,backPackItem);
        }else {
            throw  new NoSolutionException("No Solution Found");
        }
    }

    public int getBackPackSize() {
        return backPackSize;
    }

    public void setBackPackSize(int backPackSize) {
        this.backPackSize = backPackSize;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    static class Item{
    int size;
    int cost;

        public Item(int size, int cost) {
            this.size = size;
            this.cost = cost;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }
    }

}
