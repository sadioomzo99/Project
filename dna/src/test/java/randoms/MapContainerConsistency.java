package randoms;

import datastructures.container.Container;
import java.util.stream.LongStream;

public class MapContainerConsistency {
    public static void main(String[] args) {
        Container.MapContainer<Long, Long> container = new Container.MapContainer<>();
        LongStream.range(0L, 1000_000L).parallel().forEach(i -> container.put(i, i));
        System.out.println(LongStream.range(0L, 1000_000L).parallel().allMatch(i -> container.get(i) == i));
    }
}
