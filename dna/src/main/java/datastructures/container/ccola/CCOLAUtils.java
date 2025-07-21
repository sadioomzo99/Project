package datastructures.container.ccola;

import core.BaseSequence;
import datastructures.cola.COLA;
import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.core.RunBuilder;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.cola.run.MemRun;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import datastructures.container.DNAContainer;
import utils.Coder;
import utils.DNAPacker;
import utils.FuncUtils;
import utils.Pair;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static datastructures.container.utils.DNAContainerUtils.*;

public final class CCOLAUtils {

    public static <K extends Comparable<K>, V> long putAOCOLA(DNAContainer container, COLA<K, V> cola, int fromLevel, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        Long[] ids = IntStream.range(fromLevel, cola.getRunsManager().size()).parallel().mapToObj(l -> {
            BaseSequence header = generateCOLAHeaderSeq(container, cola, entryMapper, l);
            return container.put(header);

        }).toArray(Long[]::new);

        return putList(container, DNAPacker.pack(ids));
    }

    private static <K extends Comparable<K>, V> BaseSequence generateCOLAHeaderSeq(DNAContainer container, COLA<K, V> cola, Coder<KVEntry<K, V>, BaseSequence> entryMapper, int l) {
        var run = cola.getRun(l);
        if (run == null)
            run = new MemRun<>(new ArrayList<>(), l);

        var size = run.size();
        var level = run.level();
        var seqs = run.parallelStream().map(entryMapper::encode).toArray(BaseSequence[]::new);
        long arrayId = putArrayParallel(container, seqs);
        return DNAPacker.pack(arrayId, level, size);
    }

    public static <K extends Comparable<K>, V> List<Run<K, V>> getAOCOLA(DNAContainer container, long colaId, RunBuilderStrategy<K, V> rbs, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return assembleCOLARunsFromHeaders(container, getAOCOLAHeaders(container, colaId), rbs, entryMapper);
    }

    public static <K extends Comparable<K>, V> long appendAOCOLARun(DNAContainer container, long colaId, Run<K, V> run, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return appendAOCOLARun(container, colaId, run, entryMapper, false);
    }

    public static <K extends Comparable<K>, V> long appendAOCOLARun(DNAContainer container, long colaId, Run<K, V> run, Coder<KVEntry<K, V>, BaseSequence> entryMapper, boolean parallel) {
        var r = (parallel ? run.parallelStream() : run.stream()).map(entryMapper).toArray(BaseSequence[]::new);
        long arrayId = putArray(container, parallel, r);
        BaseSequence header = DNAPacker.pack(arrayId, run.level(), run.size());
        long appendRunId = container.put(header);
        appendToList(container, colaId, DNAPacker.pack(appendRunId));
        return arrayId;
    }

    public static <K extends Comparable<K>, V> List<KVEntry<K, V>> getAOCOLARun(DNAContainer container, long colaId, int level, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return getAOCOLARunStream(container, colaId, level, entryMapper).toList();
    }

    public static <K extends Comparable<K>, V> Stream<KVEntry<K, V>> getAOCOLARunStream(DNAContainer container, long colaId, int level, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        var arrayId = getAOCOLARunDataArrayId(container, colaId, level);
        return FuncUtils.stream(() -> Objects.requireNonNull(getArrayIterator(container, arrayId))).map(entryMapper::decode);
    }

    public static long getAOCOLARunDataArrayId(DNAContainer container, long colaId, int level) {
        var runId = getCOLARunId(container, colaId, level);
        return DNAPacker.unpack(container.get(runId), false).longValue();
    }

    public static <K extends Comparable<K>, V> KVEntry<K, V> getAOCOLAEntry(DNAContainer container, long colaId, int level, int pos, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        var arrayId = getAOCOLARunDataArrayId(container, colaId, level);
        return entryMapper.decode(getArrayPos(container, arrayId, pos));
    }

    public static <K extends Comparable<K>, V> StabbingSearchResult<K, V> searchStabbingAOCOLA(DNAContainer container, long colaId, K key, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return searchStabbingCOLA(container, key, getAOCOLAHeaders(container, colaId), entryMapper);
    }

    public static <K extends Comparable<K>, V> RangeSearchResult<K, V> searchRangeAOCOLA(DNAContainer container, long colaId, K keyLow, K keyHigh, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return searchRangeCOLA(container, getAOCOLAHeaders(container, colaId), keyLow, keyHigh, entryMapper);
    }

    public static <K extends Comparable<K>, V> StabbingSearchResult<K, V> searchStabbingMCOLA(DNAContainer container, long colaId, K key, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return searchStabbingCOLA(container, key, getMCOLAHeaders(container, colaId), entryMapper);
    }

    private static <K extends Comparable<K>, V> StabbingSearchResult<K, V> searchStabbingCOLA(DNAContainer container, K key, Stream<COLAHeader> colaHeaders, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return colaHeaders
                .map(h -> binarySearchCOLARun(container, h.arrayId, h.level, h.size, key, entryMapper))
                .filter(StabbingSearchResult::isMatch)
                .findFirst()
                .orElse(StabbingSearchResult.noMatch(-1));
    }

    private static <K extends Comparable<K>, V> RangeSearchResult<K, V> searchRangeCOLA(DNAContainer container, Stream<COLAHeader> headers, K keyLow, K keyHigh, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return new RangeSearchResult<>(headers.filter(h -> h.size > 0).map(h -> binarySearchRangeCOLARun(container, h.arrayId, h.level, h.size, keyLow, keyHigh, entryMapper)).filter(Objects::nonNull));
    }

    public static <K extends Comparable<K>, V> RangeSearchResult<K, V> searchRangeMCOLA(DNAContainer container, long colaId, K keyLow, K keyHigh, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return searchRangeCOLA(container, getMCOLAHeaders(container, colaId), keyLow, keyHigh, entryMapper);
    }

    private static <K extends Comparable<K>, V> StabbingSearchResult<K, V> binarySearchCOLARun(DNAContainer container, long arrayId, int level, int size, K key, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        if (size == 0)
            return StabbingSearchResult.noMatch(-1);

        int low = 0;
        int high = size - 1;

        int compared;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            KVEntry<K, V> midVal = entryMapper.decode(getArrayPosUnchecked(container, arrayId, mid));
            compared = midVal.key().compareTo(key);
            if (compared < 0)
                low = mid + 1;
            else if (compared > 0)
                high = mid - 1;
            else
                return StabbingSearchResult.match(midVal, mid, level);
        }
        return StabbingSearchResult.noMatch(low);
    }

    private static <K extends Comparable<K>, V> RangeSearchResult.ResultPatch<K, V> binarySearchRangeCOLARun(DNAContainer container, long arrayId, int level, int size, K keyLow, K keyHigh, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        StabbingSearchResult<K, V> left = binarySearchCOLARun(container, arrayId, level, size, keyLow, entryMapper);
        int leftIndex = left.getIndex();
        if (left.isNoMatch()) {
            if (leftIndex >= size)
                return null;
            if (leftIndex > 0) {
                var s = IntStream.range(leftIndex, getArrayLength(container, arrayId))
                        .mapToObj(i -> new Pair<>(i, entryMapper.decode(getArrayPosUnchecked(container, arrayId, i))))
                        .takeWhile(p -> p.getT2().key().compareTo(keyHigh) <= 0);

                return new RangeSearchResult.ResultPatch<>(level, s);
            }
        }

        StabbingSearchResult<K, V> right = binarySearchCOLARun(container, arrayId, level, size, keyHigh, entryMapper);
        int rightIndex = right.getIndex();
        if (right.isNoMatch() && rightIndex == 0)
            return null;
        
        if (rightIndex == size)
            rightIndex = rightIndex - 1;

        var s = IntStream.iterate(rightIndex, i -> i >= 0, i -> i - 1)
                .mapToObj(i -> new Pair<>(i, entryMapper.decode(getArrayPosUnchecked(container, arrayId, i))))
                .takeWhile(p -> p.getT2().key().compareTo(keyHigh) <= 0);

        return new RangeSearchResult.ResultPatch<>(level, s);
    }


    public static <K extends Comparable<K>, V> long putMCOLA(DNAContainer container, COLA<K, V> cola, int fromLevel, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        Long[] ids = IntStream.range(fromLevel, cola.getRunsManager().size()).mapToObj(l -> {
            BaseSequence header = generateCOLAHeaderSeq(container, cola, entryMapper, l);
            return putList(container, header);

        }).toArray(Long[]::new);

        return putList(container, DNAPacker.pack(ids));
    }

    public static <K extends Comparable<K>, V> List<Run<K, V>> getMCOLA(DNAContainer container, long colaId, RunBuilderStrategy<K, V> rbs, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return assembleCOLARunsFromHeaders(container, getMCOLAHeaders(container, colaId), rbs, entryMapper);
    }

    private static Stream<COLAHeader> getMCOLAHeaders(DNAContainer container, long colaId) {
        return getList(container, colaId).stream()
                .flatMapToLong(seq -> DNAPacker.unpackAll(seq, false).stream().mapToLong(Number::longValue))
                .mapToObj(i -> getList(container, i))
                .map(list -> DNAPacker.unpack(list.get(list.size() - 1), 3, false))
                .map(COLAHeader::new);
    }

    private static Stream<COLAHeader> getAOCOLAHeaders(DNAContainer container, long colaId) {
        return getList(container, colaId).stream()
                .flatMapToLong(seq -> DNAPacker.unpackAll(seq, false).stream().mapToLong(Number::longValue))
                .mapToObj(container::get)
                .map(header -> DNAPacker.unpack(header, 3,false))
                .map(COLAHeader::new);
    }

    private static <K extends Comparable<K>, V> List<Run<K, V>> assembleCOLARunsFromHeaders(DNAContainer container, Stream<COLAHeader> headers, RunBuilderStrategy<K, V> rbs, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return headers.map(h -> {
            RunBuilder<K, V> rb = rbs.getRunBuilder(h.size, h.level);
            Arrays.stream(Objects.requireNonNull(getArray(container, h.arrayId))).parallel().map(entryMapper::decode).forEach(rb::add);
            return rb.build();
        }).toList();
    }

    public static <K extends Comparable<K>, V> void replaceMCOLARun(DNAContainer container, long id, Run<K, V> run, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        long runId = getCOLARunId(container, id, run.level());
        long arrayId = putArray(container, run.stream().map(entryMapper).toArray(BaseSequence[]::new));
        BaseSequence header = DNAPacker.pack(arrayId, run.level(), run.size());
        appendToList(container, runId, header);
    }

    public static <K extends Comparable<K>, V> void appendMCOLARun(DNAContainer container, long colaId, Run<K, V> run, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        Iterator<BaseSequence> headerIt = getListIterator(container, colaId);
        var runId =  FuncUtils.stream(() -> headerIt).flatMap(seq -> DNAPacker.unpackAllStream(seq, false)).skip(run.level()).findFirst().orElse(-1L).longValue();
        if (runId >= 0L)
            throw new RuntimeException("Run at level: " + run.level() + " already allocated! Try replacing the run instead.");

        long arrayId = putArray(container, run.stream().map(entryMapper).toArray(BaseSequence[]::new));
        BaseSequence header = DNAPacker.pack(arrayId, run.level(), run.size());
        runId = putList(container, header);
        appendToList(container, colaId, DNAPacker.pack(runId));
    }

    public static <K extends Comparable<K>, V> List<KVEntry<K, V>> getMCOLARun(DNAContainer container, long colaId, int level, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        return getMCOLARunStream(container, colaId, level, entryMapper).toList();
    }

    public static <K extends Comparable<K>, V> Stream<KVEntry<K, V>> getMCOLARunStream(DNAContainer container, long colaId, int level, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        var arrayId = getMCOLARunDataArrayId(container, colaId, level);
        return FuncUtils.stream(() -> Objects.requireNonNull(getArrayIterator(container, arrayId))).map(entryMapper::decode);
    }

    public static <K extends Comparable<K>, V> KVEntry<K, V> getMCOLAEntry(DNAContainer container, long colaId, int level, int pos, Coder<KVEntry<K, V>, BaseSequence> entryMapper) {
        var arrayId = getMCOLARunDataArrayId(container, colaId, level);
        return entryMapper.decode(getArrayPos(container, arrayId, pos));
    }

    private static long getCOLARunId(DNAContainer container, long colaId, int level) {
        Iterator<BaseSequence> headerIt = getListIterator(container, colaId);
        return FuncUtils.stream(() -> headerIt).flatMap(seq -> DNAPacker.unpackAllStream(seq, false)).skip(level).findFirst().map(Number::longValue).orElseThrow(() -> new RuntimeException("there is no run at level: " + level));
    }

    private static long getMCOLARunDataArrayId(DNAContainer container, long colaId, int level) {
        var runId = getCOLARunId(container, colaId, level);
        Iterator<BaseSequence> runIt = getListIterator(container, runId);
        BaseSequence last = null;
        while(runIt.hasNext())
            last = runIt.next();

        return DNAPacker.unpack(last, false).longValue();
    }

    private record COLAHeader(long arrayId, int level, int size) {
        private COLAHeader(Number[] header) {
            this(header[0].longValue(), header[1].intValue(), header[2].intValue());
        }
    }
}
