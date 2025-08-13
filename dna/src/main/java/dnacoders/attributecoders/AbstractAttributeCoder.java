package dnacoders.attributecoders;

import core.Attribute;
import core.BaseSequence;
import utils.AddressedDNA;
import utils.*;
import utils.csv.CsvLine;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractAttributeCoder<D> implements AsymmetricCoder<Stream<CsvLine>, Stream<AbstractAttributeCoder.DecodedLine<D>>, Stream<List<AddressedDNA>>> {
    public static final char SEGMENTATION_ID_SEPARATOR = '.';
    protected static final Collector<Pair<Attribute<String>, BaseSequence>, ?, ? extends Map<Attribute<String>, TreeSet<Pair<Attribute<String>, BaseSequence>>>> DECODE_UNORDERED_GROUPER_PARALLEL = decodingUnorderedGrouper(true);
    protected static final Collector<Pair<Attribute<String>, BaseSequence>, ?, ? extends Map<Attribute<String>, TreeSet<Pair<Attribute<String>, BaseSequence>>>> DECODE_UNORDERED_GROUPER_SEQUENTIAL = decodingUnorderedGrouper(false);

    protected final AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeCoder;
    protected final Coder<byte[], BaseSequence> byteCoder;
    protected final Coder<BaseSequence, BaseSequence[]> segmentationCoder;
    protected final String keyName;

    public AbstractAttributeCoder(String keyName, AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeCoder, Coder<BaseSequence, BaseSequence[]> segmentationCoder, Coder<byte[], BaseSequence> byteCoder) {
        this.attributeCoder = attributeCoder;
        this.segmentationCoder = segmentationCoder;
        this.keyName = keyName;
        this.byteCoder = byteCoder;
    }

    private static Collector<Pair<Attribute<String>, BaseSequence>, ?, ? extends Map<Attribute<String>, TreeSet<Pair<Attribute<String>, BaseSequence>>>> decodingUnorderedGrouper(boolean isParallel) {
        var mapping = Collectors.mapping((Pair<Attribute<String>, BaseSequence> p) -> p,
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(p -> {
                    String value = p.getT1().getValue();
                    int index = value.lastIndexOf(SEGMENTATION_ID_SEPARATOR);
                    return index >= 0 ? Integer.parseInt(value.substring(index + 1)) : Integer.MIN_VALUE;
                }))));

        Function<Pair<Attribute<String>, BaseSequence>, Attribute<String>> attRestorer = p -> {
            String key = p.getT1().getName();
            String value = p.getT1().getValue();
            int index = value.lastIndexOf(SEGMENTATION_ID_SEPARATOR);
            return index >= 0 ? new Attribute<>(key, value.substring(0, index)) : new Attribute<>(key, value);
        };

        return isParallel ? Collectors.groupingByConcurrent(attRestorer, mapping) : Collectors.groupingBy(attRestorer, mapping);
    }


    protected abstract Stream<List<AddressedDNA>> encode(Stream<CsvLine> lines, boolean parallel);


    /**
     * Encodes the given csv lines in sequential mode. Due to segmentation, each "line" is encoded to a list of EncodedLine.
     * @param lines the input vsc lines to be encoded.
     * @return a stream of List < EncodedLine >.
     */
    @Override
    public Stream<List<AddressedDNA>> encode(Stream<CsvLine> lines) {
        return encode(lines, false);
    }

    /**
     * Encodes the given csv lines in parallel mode. Due to segmentation, each "line" is encoded to a list of EncodedLine.
     * @param lines the input vsc lines to be encoded.
     * @return a stream of List < EncodedLine >.
     */
    public Stream<List<AddressedDNA>> encodeParallel(Stream<CsvLine> lines) {
        return encode(lines, true);
    }

    protected abstract DecodedLine<D> decodeEntry(Attribute<?> att, Collection<? extends Pair<?, BaseSequence>> pairs);

    /**
     * Decodes the given Encoded lines' lists in sequential mode. This method does not require any ordering of the segments. This method is useful to parse and decode a large number of EncodedLine that need to be grouped and sorted.
     * @param encodedLines the encoded lines' stream.
     * @return the decoded stream of DecodedLine < ? >
     */
    public Stream<DecodedLine<D>> decodeFromUnordered(Stream<AddressedDNA> encodedLines) {
        return doDecodeFromUnordered(encodedLines.sequential(), DECODE_UNORDERED_GROUPER_SEQUENTIAL, false);
    }

    /**
     * Decodes the given Encoded lines' lists in parallel mode. This method does not require any ordering of the segments. This method is useful to parse and decode a large number of EncodedLine that need to be grouped and sorted.
     * @param encodedLines the encoded lines' stream.
     * @return the decoded stream of DecodedLine < ? >
     */
    public Stream<DecodedLine<D>> decodeFromUnorderedParallel(Stream<AddressedDNA> encodedLines) {
        return doDecodeFromUnordered(encodedLines.parallel(), DECODE_UNORDERED_GROUPER_PARALLEL, true);
    }

    protected Stream<DecodedLine<D>> doDecodeFromUnordered(Stream<AddressedDNA> encodedLines, Collector<Pair<Attribute<String>, BaseSequence>, ?, ? extends Map<Attribute<String>, TreeSet<Pair<Attribute<String>, BaseSequence>>>> grouper, boolean isParallel) {
        PooledCompletionService<Stream<DecodedLine<D>>> service = new PooledCompletionService<>(FuncUtils.pool(false));
        service.submitAndShutDown(() -> FuncUtils.callParallelAsync(() -> {
            var groups = FuncUtils.stream(encodedLines, isParallel)
                    .map(line -> new Pair<>(attributeCoder.decode(line.getT1()), line.getT2()))
                    .collect(grouper);
            return FuncUtils.stream(groups.entrySet().stream(), isParallel).map(e -> decodeEntry(e.getKey(), e.getValue()));
        }).get());

        return service.stream().flatMap(Function.identity());
    }

    /**
     * Decodes the given Encoded lines' lists in parallel mode. This method requires that each list contains the corresponding segments of an encoded csv line in order.
     * @param encodedLines the encoded lines' stream.
     * @return the decoded stream of DecodedLine < ? >
     */
    public Stream<DecodedLine<D>> decodeParallel(Stream<List<AddressedDNA>> encodedLines) {
        return decode(encodedLines, true);
    }

    protected Stream<DecodedLine<D>> decode(Stream<List<AddressedDNA>> encodedLines, boolean parallel) {
        PooledCompletionService<DecodedLine<D>> service = new PooledCompletionService<>(FuncUtils.pool(parallel));
        FuncUtils.executeAsync(() -> {
            encodedLines.forEach(p -> service.submit(() -> decodeEntry(attributeCoder.decode(p.get(0).getT1()), p)));
            service.shutdown();
        });

        return service.stream();
    }

    /**
     * Decodes the given Encoded lines' lists in sequential mode. This method requires that each list contains the corresponding segments of an encoded csv line in order.
     * @param encodedLines the encoded stream.
     * @return the decoded stream of DecodedLine < D >
     */
    @Override
    public Stream<DecodedLine<D>> decode(Stream<List<AddressedDNA>> encodedLines) {
        return decode(encodedLines, false);
    }

    protected List<AddressedDNA> finalizeEncodingBytes(byte[] bytes, String attributeName, String attributeValue) {
        BaseSequence byteCoded = byteCoder.encode(bytes);
        List<AddressedDNA> result = new ArrayList<>();
        BaseSequence[] partitions = segmentationCoder.encode(byteCoded);
        result.add(new AddressedDNA(attributeCoder.encode(new Attribute<>(attributeName, attributeValue)), partitions[0]));
        for (int i = 1; i < partitions.length; i++)
            result.add(new AddressedDNA(attributeCoder.encode(new Attribute<>(attributeName, attributeValue + SEGMENTATION_ID_SEPARATOR + i)), partitions[i]));

        return result;
    }

    public static class DecodedLine<M> extends Pair<Attribute<?>, M> {
        public DecodedLine(Attribute<?> attribute, M mapping) {
            super(attribute, mapping);
        }
    }
}
