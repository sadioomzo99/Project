package generators.barcodes;

import core.BaseSequence;
import dnacoders.DistanceCoder;
import generators.SeqGenerator;
import utils.lsh.LSH;

public class BarcodesGeneratorLSH implements SeqGenerator {

    private final float minDist;
    private final SeqGenerator generator;
    private final LSH<BaseSequence> lsh;

    public BarcodesGeneratorLSH(SeqGenerator generator, LSH<BaseSequence> lsh, float minDist) {
        this.generator = generator;
        this.minDist = minDist;
        this.lsh = lsh;
    }

    private synchronized boolean tryAdd(BaseSequence seq) {
        if (DistanceCoder.distanceScore(seq, lsh) >= minDist) {
            lsh.insert(seq);
            return true;
        }

        return false;
    }

    @Override
    public BaseSequence generate() {
        return generator.stream().filter(this::tryAdd).findFirst().orElseThrow();
    }
}
