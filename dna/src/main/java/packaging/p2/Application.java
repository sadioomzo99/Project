package packaging.p2;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.DNARule;
import core.dnarules.SuperBasicDNARules;
import utils.AddressedDNA;
import dnacoders.AttributeMapper;
import dnacoders.DistanceCoder;
import dnacoders.P2SegmentationCoder;
import dnacoders.attributecoders.AbstractAttributeCoder;
import dnacoders.attributecoders.NonUniqueAttributeCoder;
import dnacoders.attributecoders.UniqueAttributeCoder;
import dnacoders.dnaconvertors.Bin;
import dnacoders.dnaconvertors.NaiveQuattro;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.dnaconvertors.RotatingTre;
import dnacoders.headercoders.BasicDNAPadder;
import dnacoders.headercoders.PermutationCoder;
import utils.*;
import utils.compression.DeltaCode;
import utils.compression.GZIP;
import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import utils.fasta.ReadableFASTAFile;
import utils.fasta.WriteableFASTAFile;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import utils.rq.RQCoder;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Application {

    public static void main(String... args) {
        ConfigFile config = new ConfigFile(FuncUtils.tryOrElse(() -> args[0], () -> "params.ini"));
        boolean force = FuncUtils.tryOrElse(() -> Boolean.valueOf(args[1]), () -> false);

        System.out.println("Parsed the following parameters:\n-----------------------------------------------------------------\n" + config);
        System.out.println();
        if (!force && !approveParameters()) {
            System.out.println("Parameters were not approved. Exiting!");
            return;
        }
        ConfigFile.APP_MODE mode = config.getMode();

        if (mode.doEncode()) {
            Instant startEncoding = Instant.now();
            encode(config);
            System.out.println("\n-----------------------------------------------------------------\nEncoding took: " + FuncUtils.asPrettyString(Duration.between(startEncoding, Instant.now())) + "\n-----------------------------------------------------------------\n");
        }

        if (mode.doDecode()) {
            Instant startDecoding = Instant.now();
            decode(config);
            System.out.println("\n-----------------------------------------------------------------\nDecoding took: " + FuncUtils.asPrettyString(Duration.between(startDecoding, Instant.now())) + "\n-----------------------------------------------------------------\n");
        }
    }

    public static void encode(ConfigFile config) {
        float rqMaxError = config.getRqMaxError();
        boolean parallel = config.getParallel();
        String tablePath = config.getTablePath();
        String encodePath = config.getEncodePath();

        DNARule basicRules = BasicDNARules.INSTANCE;
        DNARule superBasicRules = SuperBasicDNARules.INSTANCE;

        LSH<BaseSequence> lsh = MinHashLSH.newSeqAmpLSHTraditional(config.getLshK(), config.getLshR(), config.getLshB(), LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        Coder<BaseSequence, BaseSequence> distanceCoder = getDistanceCoder(config, basicRules, lsh);

        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeCoder = attributeEncoder(
                false,
                        config.getCBBsMinimumLength(),
                        config.getCBBsPermutations(),
                        extractDnaConvertor(config.getCBBsDnaConvertor()),
                        basicRules,
                        lsh
        );

        Coder<BaseSequence, BaseSequence[]> segmentationCoder = Coder.fuse(
                new P2SegmentationCoder(config.getSegmentationLength() - DNAPacker.pack(config.getSegmentationPermutations() - 1).length(), config.getSegmentationGcCorrections()),
                Coder.arrayMapper(
                        distanceCoder,
                        BaseSequence[]::new,
                        BaseSequence[]::new
                )
        );


        Supplier<BufferedCsvReader> sourceSupplier = () -> new BufferedCsvReader(tablePath);
        FuncUtils.safeCall(() -> new File(encodePath).mkdirs());
        System.out.println("[encoding unique attributes]");
        int counter = 0;
        int seqsCounter;
        for (ConfigFile.UniqueAttribute att : config.getUniqueAttributes()) {
            seqsCounter = 0;
            WriteableFASTAFile writer = new WriteableFASTAFile(encodePath + "/u_mapping_" + (counter++) + "_" + att.getKey() + ".fa", false);
            UniqueAttributeCoder coder = new UniqueAttributeCoder(
                    attributeCoder,
                    rq(att.getGZIP(), rqMaxError, superBasicRules),
                    att.getSegmentation() ? segmentationCoder : DoNothingP2Segmentation.INSTANCE,
                    att.getKey(),
                    att.getMappingAttributes(),
                    att.getMappingAttributesTypes()
            );

            System.out.println("-> encoding: " + att);
            var it = encode(coder, sourceSupplier.get(), parallel).iterator();
            while(it.hasNext()) {
                for (AddressedDNA encodedLine : it.next()) {
                    seqsCounter++;
                    writer.append(new BaseSequence(encodedLine.getT1(), encodedLine.getT2()), seqsCounter + "-" + encodedLine.getT1().length());
                }
            }
            writer.close();
        }

        System.out.println("[encoding non-unique attributes]");
        counter = 0;
        for (ConfigFile.NonUniqueAttribute att : config.getNonUniqueAttributes()) {
            seqsCounter = 0;
            WriteableFASTAFile writer = new WriteableFASTAFile(encodePath + "/mapping_" + (counter++) + "_" + att.getKey() + ".fa", false);
            NonUniqueAttributeCoder coder = new NonUniqueAttributeCoder(
                    att.getKey(),
                    att.getMappingAttribute(),
                    attributeCoder,
                    rq(att.getGZIP(), rqMaxError, superBasicRules),
                    DeltaCode.DEFAULT_RANGE_DELTA_COMPRESSOR,
                    att.getSegmentation() ? segmentationCoder : DoNothingP2Segmentation.INSTANCE
            );
            System.out.println("-> encoding: " + att);
            var it = encode(coder, sourceSupplier.get(), parallel).iterator();
            while(it.hasNext()) {
                for (AddressedDNA encodedLine : it.next()) {
                    seqsCounter++;
                    writer.append(new BaseSequence(encodedLine.getT1(), encodedLine.getT2()), seqsCounter + "-" + encodedLine.getT1().length());
                }
            }
            writer.close();
        }
    }

    private static Coder<BaseSequence, BaseSequence> getDistanceCoder(ConfigFile config, DNARule basicRules, LSH<BaseSequence> lsh) {
        if (config.getSegmentationPermutations() <= 0)
            return new Coder<>() {
                @Override
                public BaseSequence encode(BaseSequence seq) {
                    return seq;
                }
                @Override
                public BaseSequence decode(BaseSequence seq) {
                    return seq;
                }
            };

        return new DistanceCoder(
                config.getSegmentationPermutations(),
                lsh,
                basicRules,
                config.getDistanceCoderCError(),
                config.getDistanceCoderCDistance()
        );
    }


    public static void decode(ConfigFile config) {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeDecoder = attributeDecoder(
                config.getParallel(),
                0,
                config.getCBBsPermutations(),
                extractDnaConvertor(config.getCBBsDnaConvertor())
        );

        Coder<BaseSequence, BaseSequence> distanceCoder = getDistanceCoder(config, null, null);

        Coder<BaseSequence, BaseSequence[]> segmentationCoder = Coder.fuse(
                new P2SegmentationCoder(10, 0),
                Coder.arrayMapper(
                        distanceCoder,
                        BaseSequence[]::new,
                        BaseSequence[]::new
                )
        );


        String decodePath = config.getDecodePath();

        int counter = 0;
        FuncUtils.safeCall(() -> new File(decodePath).mkdirs());
        System.out.println("[decoding unique attributes]");
        for (ConfigFile.UniqueAttribute att : config.getUniqueAttributes()) {
            System.out.println("-> decoding: " + att);
            BufferedCsvWriter writer = new BufferedCsvWriter(decodePath + "/u_mapping_" + counter + "_" + att.getKey() + "_decoded.txt", false);
            UniqueAttributeCoder coder = new UniqueAttributeCoder(
                    attributeDecoder,
                    rq(att.getGZIP(), 1f, seq -> 0f),
                    att.getSegmentation() ? segmentationCoder : DoNothingP2Segmentation.INSTANCE,
                    att.getKey(),
                    att.getMappingAttributes(),
                    att.getMappingAttributesTypes()
            );

            decode(coder, new ReadableFASTAFile(config.getEncodePath() + "/u_mapping_" + (counter++) + "_" + att.getKey() + ".fa"), config.getParallel()).map(Pair::toString).forEach(writer::appendNewLine);
            writer.close();
        }

        counter = 0;
        System.out.println("decoding non-unique attributes");
        for (ConfigFile.NonUniqueAttribute att : config.getNonUniqueAttributes()) {
            System.out.println("-> decoding: " + att);
            BufferedCsvWriter writer = new BufferedCsvWriter(config.getDecodePath() + "/mapping_" + counter + "_" + att.getKey() + "_decoded.txt", false);
            NonUniqueAttributeCoder coder = new NonUniqueAttributeCoder(
                    att.getKey(),
                    att.getMappingAttribute(),
                    attributeDecoder,
                    rq(att.getGZIP(), 1f, seq -> 0f),
                    DeltaCode.DEFAULT_RANGE_DELTA_COMPRESSOR,
                    att.getSegmentation() ? segmentationCoder : DoNothingP2Segmentation.INSTANCE
            );

            decode(coder, new ReadableFASTAFile(config.getEncodePath() + "/mapping_" + (counter++) + "_" + att.getKey() + ".fa"), config.getParallel()).map(Pair::toString).forEach(writer::appendNewLine);
            writer.close();
        }
    }

    static Coder<String, BaseSequence> extractDnaConvertor(String dnaConvertorName) {
        if (dnaConvertorName.equalsIgnoreCase(RotatingQuattro.class.getSimpleName()))
            return RotatingQuattro.INSTANCE;
        if (dnaConvertorName.equalsIgnoreCase(RotatingTre.class.getSimpleName()))
            return RotatingTre.INSTANCE;
        if (dnaConvertorName.equalsIgnoreCase(Bin.class.getSimpleName()))
            return Bin.INSTANCE;
        if (dnaConvertorName.equalsIgnoreCase(NaiveQuattro.class.getSimpleName()))
            return NaiveQuattro.INSTANCE;
        else
            throw new IllegalArgumentException("Cannot find DNA convertor: " + dnaConvertorName);
    }


    static Coder<byte[], BaseSequence> rq(boolean useGZIP, float rqMaxError, DNARule rules) {
        var rq = new RQCoder(
                seq -> rules.evalErrorProbability(seq) <= rqMaxError,
                seq -> rules.evalErrorProbability(seq) <= rqMaxError
        );

        return useGZIP ? Coder.fuse(GZIP.INSTANCE, rq) : rq;
    }

    static Stream<List<AddressedDNA>> encode(AbstractAttributeCoder<?> coder, BufferedCsvReader reader, boolean parallel) {
        return parallel ? coder.encodeParallel(reader.stream()) : coder.encode(reader.stream());
    }

    static Stream<? extends AbstractAttributeCoder.DecodedLine<?>> decode(AbstractAttributeCoder<?> coder, ReadableFASTAFile reader, boolean parallel) {
        Stream<AddressedDNA> encodedLines = reader.stream().map(e -> {
            int spLength = Integer.parseInt(e.getCaption().split("-")[1]);
            return new AddressedDNA(e.getSeq().window(0, spLength), e.getSeq().window(spLength));
        });

        return parallel ? coder.decodeFromUnorderedParallel(encodedLines) : coder.decodeFromUnordered(encodedLines);
    }

    static boolean approveParameters() {
        System.out.println("-> Note: Any existing file with the same name will be overridden");
        System.out.println("Are these parameters correct? [y/n]");
        String answer = FuncUtils.superSafeCall(() -> new Scanner(System.in).nextLine());
        return (answer != null) && (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("true") || answer.equals("1") || answer.equalsIgnoreCase("yes"));
    }


    public static AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeEncoder(boolean parallel, int targetLength, int permsCount, Coder<String, BaseSequence> dnaConvertor, DNARule rules, LSH<BaseSequence> lsh) {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder =  AsymmetricCoder.fuse(
                AttributeMapper.newInstance(dnaConvertor),
                new BasicDNAPadder(targetLength - DNAPacker.pack(permsCount - 1).length())
        );
        Coder<BaseSequence, BaseSequence> lshUpdater = new Coder<>() {
            @Override
            public BaseSequence encode(BaseSequence seq) {
                lsh.insert(seq);
                return seq;
            }
            @Override
            public BaseSequence decode(BaseSequence seq) {
                return seq;
            }
        };

        if (permsCount == 0)
            return AsymmetricCoder.fuse(coder, lshUpdater);

        return AsymmetricCoder.fuse(
                coder,
                new PermutationCoder(parallel, permsCount, seq -> -rules.evalErrorProbability(seq)),
                lshUpdater
        );
    }

    public static AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeDecoder(boolean parallel, int targetLength, int permsCount, Coder<String, BaseSequence> dnaConvertor) {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder =  AsymmetricCoder.fuse(
                AttributeMapper.newInstance(dnaConvertor),
                new BasicDNAPadder(targetLength - DNAPacker.pack(permsCount - 1).length())
        );

        if (permsCount == 0)
            return coder;

        return AsymmetricCoder.fuse(
                coder,
                new PermutationCoder(parallel, permsCount, seq -> 0f)
        );
    }

    private static class DoNothingP2Segmentation extends P2SegmentationCoder {
        final static DoNothingP2Segmentation INSTANCE = new DoNothingP2Segmentation();
        private DoNothingP2Segmentation() {
            super(100, 0);
        }
        @Override
        public BaseSequence[] encode(BaseSequence seq) {
            return new BaseSequence[] {seq};
        }
        @Override
        public BaseSequence decode(BaseSequence[] seqs) {
            return seqs[0];
        }
        @Override
        public int numSegments(int len) {
            return 1;
        }
    }
}
