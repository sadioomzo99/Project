package datastructures.lightweight.index;

import datastructures.lightweight.LightWeightIndex;
import utils.Range;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public class ColumnImprint implements LightWeightIndex<Double> {

    private final BitSet bits;
    private final double binSize;
    private final Range<Double> domainRange;
    private final int bitsCount;


    public ColumnImprint(int bitsCount, List<Double> data) {
        this(bitsCount, data, false);
    }

    public ColumnImprint(int bitsCount, List<Double> data, boolean sorted) {
        if (sorted)
            this.domainRange = new Range<>(data.get(0), data.get(data.size() - 1));
        else {
            this.domainRange = new Range<>(Double.MAX_VALUE, Double.MIN_VALUE);
            data.forEach(domainRange::extend);
        }
        this.bitsCount = bitsCount;
        this.binSize =  (domainRange.getT2() - domainRange.getT1()) / (double) bitsCount;
        this.bits = new BitSet(bitsCount);
        data.forEach(this::setBitOf);
    }

    public ColumnImprint(int bitsCount, Range<Double> domainRange, List<Integer> bitsToSet) {
        this.bitsCount = bitsCount;
        this.domainRange = domainRange;
        this.binSize = (domainRange.getT2() - domainRange.getT1()) / (double) bitsCount;
        this.bits = new BitSet(bitsCount);
        bitsToSet.forEach(bits::set);
    }

    @Override
    public boolean simplePointQuery(Double d) {
        return domainRange.intersects(d) && this.bits.get(getBitPosOf(d));
    }

    @Override
    public boolean simpleRangeQuery(Range<Double> range) {
        if (!domainRange.intersects(range))
            return false;

        double d1 = range.getT1();
        double d2 = range.getT2();
        int startBit = getBitPosOf(d1);
        int endBit = getBitPosOf(d2);
        return IntStream.rangeClosed(startBit, endBit).anyMatch(this.bits::get);
    }

    private int getBitPosOf(double d) {
        if (d <= domainRange.getT1())
            return 0;
        if (d >= domainRange.getT2())
            return bitsCount - 1;

        return (int) ((d - domainRange.getT1()) / binSize);
    }

    public int[] setBitsPositions() {
        return bits.stream().limit(bitsCount).toArray();
    }

    public Number[] setBitsPositionsNumbers() {
        return bits.stream().limit(bitsCount).boxed().toArray(Number[]::new);
    }

    @Override
    public String toString() {
        return "range: " + domainRange + ", bits: " + IntStream.range(0, bitsCount).mapToObj(bits::get).map(b -> b ? '1' : '0').collect(Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
    }

    private void setBitOf(double d) {
        this.bits.set(getBitPosOf(d));
    }
    public int getBitsCount() {
        return bitsCount;
    }
    public float getSaturation() {
        return this.bits.cardinality() / (float) this.bitsCount;
    }
    public double getMin() {
        return domainRange.getT1();
    }
    public double getMax() {
        return domainRange.getT2();
    }
    public BitSet getBitSet() {
        return bits;
    }
    public double getBinSize() {
        return binSize;
    }
}
