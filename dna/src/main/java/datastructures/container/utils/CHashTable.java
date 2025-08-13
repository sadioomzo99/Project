package datastructures.container.utils;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.hashtable.HashTable;
import utils.Coder;
import utils.DNAPacker;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static datastructures.container.utils.DNAContainerUtils.*;

public final class CHashTable {

    public static <T> long putHashTable(DNAContainer container, HashTable<T> hashTable, Coder<T, BaseSequence> itemMapper) {
        var bucketsIds = IntStream.range(0, hashTable.size())
                .mapToObj(i -> hashTable.getBucket(i).stream().map(itemMapper::encode).toList()).map(encodedBucket -> putList(container, encodedBucket)).toList();

        return container.put(DNAPacker.pack(bucketsIds));
    }

    public static <T> HashTable<T> getHashTable(DNAContainer container, long id, Coder<T, BaseSequence> itemMapper) {
        var header = container.get(id);
        return new HashTable<>(
                DNAPacker.unpackAllStream(header, false)
                .map(bucketId -> getList(container, bucketId.longValue())
                        .stream()
                        .map(itemMapper::decode)
                        .collect(Collectors.toCollection(HashSet::new)))
                .toList()
        );
    }

    public static void putToHashTable(DNAContainer container, long id, int bucketId, BaseSequence encodedItem) {
        var header = container.get(id);
        var bucketIds = DNAPacker.unpack(header, bucketId + 1, false);
        appendToList(container, bucketIds[bucketId].longValue(), encodedItem);
    }
}
