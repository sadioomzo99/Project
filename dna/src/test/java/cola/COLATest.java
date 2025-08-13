package cola;

import datastructures.cola.COLA;
import datastructures.cola.PartitionedCOLA;
import datastructures.cola.run.managers.BasicRunsManager;
import datastructures.cola.run.managers.RunsManager;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import datastructures.cola.searchstrategy.COLARangeQueryStrategy;
import datastructures.cola.searchstrategy.COLAStabbingQueryStrategy;
import utils.FuncUtils;
import java.util.stream.IntStream;

public class COLATest {
    public static void main(String[] args) {
        RunBuilderStrategy<Integer, Long> runBuilderStrategy = RunBuilderStrategy.memory();

        //RunsManager<Integer, CsvLine> rm = new LRURunsManager<>(8, runBuilderStrategy);
        RunsManager<Integer, Long> rm = new BasicRunsManager<>(runBuilderStrategy);
        COLA<Integer, Long> cola = new PartitionedCOLA<>(rm, 5, true);

        int[] indexes = IntStream.range(0, 100).toArray();
        FuncUtils.shuffle(indexes);
        for (int i = 0; i < indexes.length; i++)
            cola.insert(i, (long) i);

        System.out.println(cola);

        int query = 61;
        System.out.println("query=" + query);
        System.out.println("Search -> " + cola.search(query));
        System.out.println("FCSearch -> " + cola.search(query, COLAStabbingQueryStrategy::fractionalCascade));
        System.out.println("BotUpSearch -> " + cola.search(query, COLAStabbingQueryStrategy::bottomUp));
        System.out.println("TopDownSearch -> " + cola.search(query, COLAStabbingQueryStrategy::topDown));
        System.out.println("TopDownSearch range -> " + cola.searchRange(query, query + 2, COLARangeQueryStrategy::topDown));
        System.out.println("BottomUpSearcg range -> " + cola.searchRange(query, query + 2, COLARangeQueryStrategy::bottomUp));
        FuncUtils.tryOrElse(
                () -> System.out.println("FCSearch range -> " + cola.searchRange(query, query + 2, COLARangeQueryStrategy::fractionalCascade)),
                () -> System.out.println("FCSearch range not supported"));
    }
}
