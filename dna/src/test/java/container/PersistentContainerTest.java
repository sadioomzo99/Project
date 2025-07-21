package container;

import datastructures.container.Container;
import datastructures.container.impl.PersistentContainer;
import utils.serializers.FixedSizeSerializer;

public class PersistentContainerTest {
    public static void main(String[] args) {
        load();
    }

    static void load() {
        Container<Long, Long> container = new PersistentContainer<>("test.txt", FixedSizeSerializer.LONG);
        for (long i = 0L; i < 10L; i++)
            container.put(i, i);

        Container<Long, Long> loaded = PersistentContainer.load("test.txt", FixedSizeSerializer.LONG);

        loaded.put(11L, -1L);

        loaded.forEach(System.out::println);
    }
}
