package datastructures.lightweight.index;

import datastructures.lightweight.LightWeightIndex;
import utils.Range;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class PSMA implements LightWeightIndex<Long> {

    private final Map<Integer, Range<Integer>> lookup;
    private final Range<Long> domainRange;

    private static final int HIGHEST_BYTE_POSITION_LONG  = Long.BYTES - 1;
    public static final int LOOKUP_SIZE                  = (2 << HIGHEST_BYTE_POSITION_LONG) * Long.BYTES;
    private static final long HIGHEST_BYTE_FF_LONG       = (long) 0xFF << (Long.SIZE - Byte.SIZE);

    public PSMA(List<Long> sortedData) {
        this(sortedData, true);
    }

    public PSMA(List<Long> data, boolean isSorted) {
        if (!isSorted)
            Collections.sort(data);

        this.lookup = new HashMap<>(LOOKUP_SIZE);
        this.domainRange = new Range<>(data.get(0), data.get(data.size() - 1));
        initializeLookup();
        buildLookupTableFrom(data);
    }

    public PSMA(Map<Integer, Range<Integer>> lookup, Range<Long> domainRange) {
        this.lookup = lookup;
        this.domainRange = domainRange;
    }

    private void initializeLookup() {
        for (int i = 0; i < LOOKUP_SIZE; i++)
            this.lookup.put(i, new Range<>(0, 0));
    }

    public List<Map.Entry<Integer, Range<Integer>>> mappings() {
        return mappingStream().toList();
    }

    public Stream<Map.Entry<Integer, Range<Integer>>> mappingStream() {
        return lookup.entrySet().stream().filter(e -> !isEmptyRange(e.getValue()));
    }

    public Range<Integer> findDataRangeAsRange(long probe) {
        int i = calculatePositionI(probe, getMin());
        return lookup.get(i);
    }

    public Range<Integer> findDataRangeAsRange(long lo, long hi) {
        return LongStream.rangeClosed(lo, hi).mapToObj(this::findDataRangeAsRange).filter(r -> !isEmptyRange(r)).reduce((r1, r2) -> {
            r1.extend(r2);
            return r1;
        }).orElse(new Range<>(0, 0));
    }

    public IntStream findDataRange(long probe) {
        Range<Integer> range = findDataRangeAsRange(probe);
        return IntStream.range(range.getT1(), range.getT2());
    }

    public IntStream findDataRange(long lo, long hi) {
        return LongStream.rangeClosed(lo, hi).boxed().flatMapToInt(this::findDataRange).distinct();
    }

    private static int calculatePositionI(long probe, long min) {
        long dist = probe - min;
        int valuePos = getHighestNonZeroByteWithPosition(dist);
        int pos = valuePos & 0xFF;
        int highestNonZeroByte = valuePos >> Byte.SIZE;
        return highestNonZeroByte + pos * 256;
    }

    private void buildLookupTableFrom(List<Long> dataArray) {
        long min = getMin();

        int i;
        Range<Integer> range;
        for (int pos = 0; pos < dataArray.size(); pos++) {
            i = calculatePositionI(dataArray.get(pos), min);
            range = lookup.get(i);
            if (isEmptyRange(range)) {
                range.setT1(pos);
                range.setT2(pos + 1);
            }
            else {
                if (range.getT2() <= pos)
                    range.extend(pos + 1);
                else
                    range.extend(pos);
            }
        }
    }

    @Override
    public boolean simplePointQuery(Long d) {
        return domainRange.intersects(d);
    }
    @Override
    public boolean simpleRangeQuery(Range<Long> range) {
        return domainRange.intersects(range);
    }

    private static boolean isEmptyRange(Range<Integer> r) {
        return r.getT2() == 0;
    }

    private static int getHighestNonZeroByteWithPosition(long l) {
        if (l == 0)
            return 0;
        long mask = HIGHEST_BYTE_FF_LONG;
        for (int i = HIGHEST_BYTE_POSITION_LONG; i >= 0; i--) {
            long b = l & mask;
            if (b != 0) {
                int byteShifts = i - 1;
                if (byteShifts < 0)
                    return i | (int) (b << Byte.SIZE);

                return i | (int) (b >>> (byteShifts * Byte.SIZE));
            }
            mask >>>= Byte.SIZE;
        }
        throw new RuntimeException("failed calculating highestNonZeroByteWithPosition for: " + Long.toBinaryString(l));
    }

    public long getMin() {
        return domainRange.getT1();
    }
    public long getMax() {
        return domainRange.getT2();
    }

    public double getSaturation() {
        return lookup.values().stream().filter(v -> !isEmptyRange(v)).count() / (double) lookup.size();
    }

    @Override
    public String toString() {
        return lookup.entrySet().stream().filter(e -> !isEmptyRange(e.getValue())).map(Objects::toString).collect(Collectors.joining(", "));
    }
}
