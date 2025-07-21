package datastructures.reference;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface IDNASketch {

    BaseSequence[] addresses();
    int sizeInBytes();

    class HashSketch implements IDNASketch {
        protected final long seed;
        protected final int n;
        protected final int[] badIndices;
        protected BaseSequence[] addresses;

        public HashSketch(long seed, int n, int[] badIndices, BaseSequence[] addresses) {
            this.seed = seed;
            this.n = n;
            this.badIndices = badIndices;
            this.addresses = addresses;
        }

        public static IntStream computeUsedIds(int n, int[] badIndices) {
            Set<Integer> badSet = Arrays.stream(badIndices).boxed().collect(Collectors.toSet());
            return IntStream.range(0, n + badIndices.length).filter(i -> !badSet.contains(i));
        }

        public static HashSketch lazy(long seed, int n, int[] badIndices, Function<HashSketch, BaseSequence[]> genFunc) {
            return new HashSketch(seed, n, badIndices, null) {
                @Override
                public BaseSequence[] addresses() {
                    if (super.addresses == null)
                        super.addresses = genFunc.apply(this);

                    return addresses;
                }
            };
        }

        @Override
        public int sizeInBytes() {
            return Long.BYTES + Integer.BYTES + badIndices.length * Integer.BYTES;
        }

        public long seed() {
            return seed;
        }

        public int n() {
            return n;
        }

        public int[] badIndices() {
            return badIndices;
        }

        @Override
        public BaseSequence[] addresses() {
            return addresses;
        }
    }

    record ContainerIdSketch(long id, DNAContainer container) implements IDNASketch {

        @Override
        public BaseSequence[] addresses() {
            return container.getAddresses(id);
        }

        @Override
        public int sizeInBytes() {
            return Long.BYTES;
        }
    }
}
