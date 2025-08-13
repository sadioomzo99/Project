package dnacoders.attributecoders;

import core.Attribute;
import core.BaseSequence;
import utils.AddressedDNA;
import utils.*;
import utils.csv.CsvLine;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class UniqueAttributeCoder extends AbstractAttributeCoder<CsvLine> {

    private final Coder<CsvLine, byte[]> csvLinePacker;

    /**
     * Creates an encoder for a unique attribute.
     * @param attributeCoder the instance to encode attributes.
     * @param byteCoder the instance to encode a byte array (e.g. RQCoder).
     * @param segmentationCoder the instance to apply segmentation.
     * @param key the unique attribute's name to be encoded.
     * @param values the other attributes' names this key will map to.
     * @param types the other attributes' types.
     */
    public UniqueAttributeCoder(AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeCoder, Coder<byte[], BaseSequence> byteCoder, Coder<BaseSequence, BaseSequence[]> segmentationCoder, String key, String[] values, Packer.Type[] types) {
        super(key, attributeCoder, segmentationCoder, byteCoder);
        if (values.length != types.length)
            throw new RuntimeException("values.length != types.length");
        this.csvLinePacker = CSVLinePacker.fromTypeByAttributeNameArrays(values, types);
    }

    @Override
    protected Stream<List<AddressedDNA>> encode(Stream<CsvLine> lines, boolean parallel) {
        PooledCompletionService<List<AddressedDNA>> service = new PooledCompletionService<>(FuncUtils.pool(parallel));
        FuncUtils.executeAsync(() -> {
            lines.sequential().forEach(line -> service.submit(() -> finalizeEncodingBytes(
                    csvLinePacker.encode(line),
                    keyName,
                    line.get(keyName))));
            service.shutdown();
        });

        return service.stream();
    }

    @Override
    protected DecodedLine<CsvLine> decodeEntry(Attribute<?> att, Collection<? extends Pair<?, BaseSequence>> pairs) {
        CsvLine decoded = csvLinePacker.decode(byteCoder.decode(segmentationCoder.decode(pairs.stream().map(Pair::getT2).toArray(BaseSequence[]::new))));
        return new DecodedLine<>(
                att,
                decoded
        );
    }
}