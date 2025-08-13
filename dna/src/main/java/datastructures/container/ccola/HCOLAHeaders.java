package datastructures.container.ccola;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.lightweight.index.ColumnImprint;
import datastructures.lightweight.index.PSMA;
import dnacoders.dnaconvertors.Bin;
import dnacoders.dnaconvertors.NaiveQuattro;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.*;
import java.util.*;
import java.util.stream.Stream;

public class HCOLAHeaders {

    public static final Coder<COLAHeader<?>, BaseSequence> COLA_HEADER_CODER                      = new COLAHeaderCoder();
    public static final Coder<COLAHeader<PSMA>, BaseSequence> PSMA_COLA_HEADER_CODER              = new PSMACOLAHeaderCoder();
    public static final Coder<COLAHeader<ColumnImprint>, BaseSequence> CI_COLA_HEADER_CODER       = new CICOLAHeaderCoder();

    public static <K extends Comparable<K>, V> FuncUtils.TriFunction<Integer, Integer, Stream<KVEntry<K, V>>, COLAHeader<?>> generator() {
        return (size, level, __) -> new COLAHeader<>(size, level);
    }

    public static <V> FuncUtils.TriFunction<Integer, Integer, Stream<KVEntry<Long, V>>, COLAHeader<PSMA>> generatorPSMA(boolean isSorted) {
        return (size, level, s) -> new COLAHeader<>(size, level, new PSMA(s.map(KVEntry::key).toList(), isSorted));
    }

    public static <K extends Number & Comparable<K>, V> FuncUtils.TriFunction<Integer, Integer, Stream<KVEntry<K, V>>, COLAHeader<ColumnImprint>> generatorCI(int numBits) {
        return (size, level, s) -> new COLAHeader<>(size, level, new ColumnImprint(numBits, s.map(e -> e.key().doubleValue()).toList()));
    }

    public static class COLAHeader<H> {
        protected int size;
        protected int level;
        protected H h;
        public COLAHeader(int size, int level, H h) {
            this(size, level);
            this.h = h;
        }
        public COLAHeader(int size, int level) {
            this.size = size;
            this.level = level;
        }
        public int getSize() {
            return size;
        }
        public int getLevel() {
            return level;
        }
        public H getH() {
            return h;
        }

        @Override
        public String toString() {
            return "COLAHeader{" +
                    "size=" + size +
                    ", level=" + level +
                    ", header=" + h +
                    '}';
        }
    }


    private static class COLAHeaderCoder implements Coder<COLAHeader<?>, BaseSequence> {
        @Override
        public BaseSequence encode(COLAHeader<?> h) {
            return DNAPacker.pack(h.size, h.level);
        }

        @Override
        public COLAHeader<?> decode(BaseSequence seq) {
            var unpacked = DNAPacker.unpackAll(seq, false);
            return new COLAHeader<>(unpacked.get(0).intValue(), unpacked.get(1).intValue());
        }
    }

    private static class PSMACOLAHeaderCoder implements Coder<COLAHeader<PSMA>, BaseSequence> {

        @Override
        public BaseSequence encode(COLAHeader<PSMA> h) {
            BaseSequence encodedPSMA = h.h.mappingStream().map(e -> DNAPacker.pack(e.getKey(), e.getValue().getT1(), e.getValue().getT2())).collect(BaseSequence.COLLECTOR_SEQS);
            return BaseSequence.join(DNAPacker.pack(h.size, h.level, h.h.getMin(), h.h.getMax()), encodedPSMA);
        }

        @Override
        public COLAHeader<PSMA> decode(BaseSequence encodedHeader) {
            List<Number> unpacked = DNAPacker.unpackAll(encodedHeader, false);
            int i = 0;
            int size = unpacked.get(i++).intValue();
            int level = unpacked.get(i++).intValue();
            long min = unpacked.get(i++).longValue();
            long max = unpacked.get(i++).longValue();

            Map<Integer, Range<Integer>> lookup = new HashMap<>();

            int key;
            int lo;
            int hi;

            int c = unpacked.size();
            while(i < c) {
                key = unpacked.get(i++).intValue();
                lo = unpacked.get(i++).intValue();
                hi = unpacked.get(i++).intValue();
                lookup.put(key, new Range<>(lo, hi));
            }

            return new COLAHeader<>(size, level, new PSMA(lookup, new Range<>(min, max)));
        }
    }

    private static class CICOLAHeaderCoder implements Coder<COLAHeader<ColumnImprint>, BaseSequence> {
        private static final String SEPARATOR = "|";
        private static final Coder<String, BaseSequence> STRING_CODER = Bin.INSTANCE;

        @Override
        public BaseSequence encode(COLAHeader<ColumnImprint> h) {
            String minMax = h.h.getMin() + SEPARATOR + h.h.getMax();
            BaseSequence encodedMinMax = STRING_CODER.encode(minMax);
            return BaseSequence.join(
                    DNAPacker.pack(encodedMinMax.length() - 1),
                    encodedMinMax,
                    DNAPacker.pack(h.size, h.level, h.h.getBitsCount()),
                    DNAPacker.pack(h.h.setBitsPositionsNumbers())
            );
        }

        @Override
        public COLAHeader<ColumnImprint> decode(BaseSequence encodedHeader) {
            DNAPacker.LengthBase minMaxsizeLb = DNAPacker.LengthBase.parsePrefix(encodedHeader);
            int totalSize = minMaxsizeLb.totalSize();
            int minMaxlength = minMaxsizeLb.unpackSingle(encodedHeader, false).intValue() + 1;
            int stringEnd = totalSize + minMaxlength;
            String minMaxString = STRING_CODER.decode(encodedHeader.window(totalSize, stringEnd));
            int delimPos = minMaxString.indexOf(SEPARATOR);
            double min = Double.parseDouble(minMaxString.substring(0, delimPos));
            double max = Double.parseDouble(minMaxString.substring(delimPos + 1));

            encodedHeader = encodedHeader.window(stringEnd);
            DNAPacker.LengthBase sizeLb = DNAPacker.LengthBase.parsePrefix(encodedHeader);
            int size = sizeLb.unpackSingle(encodedHeader, false).intValue();

            encodedHeader = encodedHeader.window(sizeLb.totalSize());
            DNAPacker.LengthBase levelLb = DNAPacker.LengthBase.parsePrefix(encodedHeader);
            int level = levelLb.unpackSingle(encodedHeader, false).intValue();

            encodedHeader = encodedHeader.window(levelLb.totalSize());
            DNAPacker.LengthBase numBitsLb = DNAPacker.LengthBase.parsePrefix(encodedHeader);
            int numBits = numBitsLb.unpackSingle(encodedHeader, false).intValue();

            encodedHeader = encodedHeader.window(numBitsLb.totalSize());
            List<Integer> bitPositions = DNAPacker.unpackAllStream(encodedHeader, false).map(Number::intValue).toList();
            ColumnImprint ci = new ColumnImprint(numBits, new Range<>(min, max), bitPositions);
            return new COLAHeader<>(size, level, ci);
        }
    }
}
