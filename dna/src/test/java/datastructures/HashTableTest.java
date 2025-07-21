package datastructures;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import datastructures.container.utils.CHashTable;
import datastructures.hashtable.HashTable;
import utils.Coder;
import utils.Pair;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import utils.rq.RQCoder;
import utils.serializers.FixedSizeSerializer;
import java.util.stream.IntStream;

public class HashTableTest {

    public static void main(String[] args) {
        HashTable<Pair<Integer, Integer>> hashTable = new HashTable<>(10);
        IntStream.range(0, 10).mapToObj(i -> new Pair<>(i, i)).forEach(hashTable::put);
        Coder<Pair<Integer, Integer>, BaseSequence> m = Coder.fuse(
                FixedSizeSerializer.fuse(FixedSizeSerializer.INT, FixedSizeSerializer.INT),
                RQCoder.DEFAULT_RQ
        );


        System.out.println("org:");
        System.out.println(hashTable);

        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(80)
                .build();

        DNAContainer container = SizedDNAContainer.builder().setAddressManager(atm).setPayloadSize(150).build();
        long id = CHashTable.putHashTable(container, hashTable, m);

        System.out.println("\ndecoded:");
        System.out.println(CHashTable.getHashTable(container, id, m));


        System.out.println("DNA HashTable:");
        System.out.println(CHashTable.getHashTable(container, id, m));

        CHashTable.putToHashTable(container, id, 9, m.encode(new Pair<>(-1, -1)));
        System.out.println("after putting to DNA HashTable:");
        System.out.println(CHashTable.getHashTable(container, id, m));
    }
}
