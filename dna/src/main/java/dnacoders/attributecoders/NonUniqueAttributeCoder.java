package dnacoders.attributecoders;

import core.Attribute;
import core.BaseSequence;
import utils.AddressedDNA;
import utils.*;
import utils.compression.DeltaCode;
import utils.csv.CsvLine;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NonUniqueAttributeCoder extends AbstractAttributeCoder<List<Integer>> {

    protected final Coder<List<Integer>, CharSequence> rangeCompressor;
    protected final String attributeName;
    protected final Collector<CsvLine, ?, Map<CharSequence, CharSequence>> encodingGrouperSequential;
    protected final Collector<CsvLine, ?, ConcurrentMap<CharSequence, CharSequence>> encodingGrouperParallel;

    /**
     * Creates an encoder for a non-unique attribute.
     * @param attributeName the non-unique attribute's name to be encoded.
     * @param keyName the unique attribute's name this encoder will map to.
     * @param attributeCoder the instance to encode attributes.
     * @param byteCoder the instance to encode a byte array (e.g. RQCoder).
     * @param rangeCompressor the delta compressor to compress integers.
     * @param segmentationCoder the instance to apply segmentation.
     */
    public NonUniqueAttributeCoder(String attributeName,
                                   String keyName,
                                   AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeCoder,
                                   Coder<byte[], BaseSequence> byteCoder,
                                   Coder<List<Integer>, CharSequence> rangeCompressor,
                                   Coder<BaseSequence, BaseSequence[]> segmentationCoder) {

        super(keyName, attributeCoder, segmentationCoder, byteCoder);
        this.rangeCompressor = rangeCompressor;
        this.attributeName = attributeName;
        Function<CsvLine, String> classifier = line -> line.get(attributeName);
        Function<CsvLine, String> valueExtractor = line -> line.get(keyName);
        Function<Boolean, Collector<CharSequence, List<Integer>, CharSequence>> rangeCollector = sorted -> new DeltaCode.RangeCollector(rangeCompressor, sorted);

        this.encodingGrouperSequential = Collectors.groupingBy(classifier, Collectors.mapping(valueExtractor, rangeCollector.apply(true)));
        this.encodingGrouperParallel = Collectors.groupingByConcurrent(classifier, Collectors.mapping(valueExtractor, rangeCollector.apply(false)));
    }

    @Override
    protected Stream<List<AddressedDNA>> encode(Stream<CsvLine> lines, boolean parallel) {
        final PooledCompletionService<List<AddressedDNA>> service = new PooledCompletionService<>(FuncUtils.pool(parallel));
        FuncUtils.executeAsync(() -> {
            FuncUtils.stream(lines, parallel)
                    .collect(getEncodingGrouper(parallel))
                    .forEach((key, value) -> service.submit(() -> finalizeEncodingBytes(
                            value.toString().getBytes(StandardCharsets.UTF_8),
                            attributeName,
                            key.toString())));
            service.shutdown();
        });

        return service.stream();
    }

    private Collector<CsvLine, ?, ? extends Map<CharSequence, CharSequence>> getEncodingGrouper(boolean isParallel) {
        return isParallel ? encodingGrouperParallel : encodingGrouperSequential;
    }

    @Override
    protected DecodedLine<List<Integer>> decodeEntry(Attribute<?> attribute, Collection<? extends Pair<?, BaseSequence>> pairs) {
        return new DecodedLine<>(attribute, rangeCompressor.decode(new String(byteCoder.decode(segmentationCoder.decode(pairs.stream().map(Pair::getT2).toArray(BaseSequence[]::new))))));
    }
}
