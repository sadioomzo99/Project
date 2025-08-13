package cola;

import datastructures.KVEntry;
import datastructures.cola.run.FCRun;
import datastructures.cola.run.MemRun;

public class COLAFCRunTest {
    public static void main(String[] args) {
        MemRun.Builder<Integer, Long> b1 = new MemRun.Builder<>(4, 4);
        MemRun.Builder<Integer, Long> b2 = new MemRun.Builder<>(8, 5);
        b1.add(new KVEntry<>(0, 0L));
        b1.add(new KVEntry<>(10, 10L));
        b1.add(new KVEntry<>(20, 20L));
        b1.add(new KVEntry<>(30, 30L));
        b1.add(new KVEntry<>(33, 33L));
        b1.add(new KVEntry<>(44, 44L));
        b1.add(new KVEntry<>(55, 55L));
        b1.add(new KVEntry<>(66, 66L));
        var r1 = b1.build();

        b2.add(new KVEntry<>(-5, -5L));
        b2.add(new KVEntry<>(5, 5L));
        b2.add(new KVEntry<>(15, 15L));
        b2.add(new KVEntry<>(25, 25L));
        b2.add(new KVEntry<>(35, 35L));
        b2.add(new KVEntry<>(45, 45L));
        b2.add(new KVEntry<>(55, 55L));
        b2.add(new KVEntry<>(65, 65L));
        b2.add(new KVEntry<>(100, 100L));
        b2.add(new KVEntry<>(110, 110L));
        b2.add(new KVEntry<>(120, 120L));
        b2.add(new KVEntry<>(130, 130L));
        b2.add(new KVEntry<>(140, 140L));
        b2.add(new KVEntry<>(150, 150L));
        b2.add(new KVEntry<>(160, 160L));
        b2.add(new KVEntry<>(170, 170L));
        var r2 = b2.build();


        FCRun<Integer, Long> r3 = new FCRun<>(r1, r2);
        System.out.println(r3.search(-6));
        System.out.println(r3.search(-1));
        System.out.println(r3.search(33));
        System.out.println(r3.search(44));
        System.out.println(r3.search(56));
        System.out.println(r3.search(172));
    }
}
