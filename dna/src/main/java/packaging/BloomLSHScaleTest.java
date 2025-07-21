package packaging;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.DNARulesCollection;
import datastructures.hashtable.BloomFilter;
import dnacoders.DistanceCoder;
import dnacoders.dnaconvertors.RotatingTre;
import generators.SeqGenerator;
import generators.SeqGeneratorAttributed;
import generators.SeqGeneratorWithRulesProb;
import utils.FuncUtils;
import utils.lsh.LSH;
import utils.lsh.minhash.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class BloomLSHScaleTest {

    final static float MAX_ERROR = 0.6f;
    final static float MIN_DISTANCE = 0.3f;

    final static boolean GEN_TYPE_ATTRIBUTED = true;

    public final static DecimalFormat FORMATTER = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    public final static DecimalFormatSymbols FORMATTER_SYMBOLS = FORMATTER.getDecimalFormatSymbols();

    static {
        FORMATTER_SYMBOLS.setGroupingSeparator(',');
        FORMATTER.setDecimalFormatSymbols(FORMATTER_SYMBOLS);
    }

    public static void main(String[] args) {
        long count = Long.parseLong(args[0]);
        double fpp = FuncUtils.tryOrElse(() -> Double.parseDouble(args[1]), () -> 0.1d);
        boolean genType = FuncUtils.tryOrElse(() -> Boolean.parseBoolean(args[2]), () -> !GEN_TYPE_ATTRIBUTED);

        int k = 5;
        int r = 5;



        long numBits = count * 2L;
        System.out.println("BloomMinHashLSH with:");
        System.out.println("nElements: " + FORMATTER.format(count));
        System.out.println("numBits: " + FORMATTER.format(numBits) + " (" + numBits / 8000000000f + " GBs)");
        System.out.println("fpp: " + fpp);
        System.out.println("bits/element: " + numBits / (double) count);
        System.out.println("max error: " + MAX_ERROR);
        System.out.println("min dist: " + MIN_DISTANCE);
        System.out.println("generator type: " + (genType ? "attributed" : "prob"));
        System.out.println("..initializing LSH..");

        System.out.println();
        LSH<BaseSequence> lsh = MinHashLSH.newSeqLSHBloom(k, r, numBits, BloomFilter.numHashFunctions(0.001d));
        //LSH<BaseSequence> lsh = MinHashLSH.newLSHForBaseSequences(5, 5, 1);
        //LSH<BaseSequence> lsh = MinHashLSHLight.newLSHForBaseSequences(5, 5, 1);
        int seqLength = 80;

        SeqGenerator gen = generator(seqLength, lsh, genType);
        AtomicLong generatedCount = new AtomicLong(0L);
        AtomicLong lastGeneratedCount = new AtomicLong(0L);
        System.out.println("---------------------------\ngenerating...\n");
        AtomicLong t1 = new AtomicLong(System.currentTimeMillis());
        String timeAsPrettyString = FuncUtils.timeAsPrettyString(() -> gen.stream().parallel().limit(count).forEach(seq -> {
            long generated = generatedCount.getAndIncrement();

            if (generated > 0L && generated % 10_000L == 0L) {
                long currentT1 = System.currentTimeMillis();
                long generatedSinceLastRun = generated - lastGeneratedCount.get();
                System.out.println("generated " + FORMATTER.format(generated) + " sequences @ " + FORMATTER.format(generatedSinceLastRun / ((currentT1 - t1.get()) / 1000.0f)) + " seqs / sec");
                t1.set(currentT1);
                lastGeneratedCount.set(generated);
            }
        }));
        System.out.println("done generating " + FORMATTER.format(count) + " sequences");
        System.out.println(timeAsPrettyString);
    }

    static SeqGenerator generator(int length, LSH<BaseSequence> lsh, boolean genType) {
        if (genType == GEN_TYPE_ATTRIBUTED)
            return new SeqGeneratorAttributed(length, seq -> adheresToRules(seq, lsh), RotatingTre.INSTANCE);

        DNARulesCollection rules = new DNARulesCollection();
        rules.addRule(seq -> adheresToRules(seq, lsh) ? 0.0f : 1.0f);
        return new SeqGeneratorWithRulesProb(0.5f, rules);
    }

    static boolean adheresToRules(BaseSequence seq, LSH<BaseSequence> lsh) {
        if (BasicDNARules.INSTANCE.evalErrorProbability(seq) <= MAX_ERROR && DistanceCoder.distanceScore(seq, lsh) >= MIN_DISTANCE) {
            lsh.insert(seq);
            return true;
        }

        return false;
    }
}
