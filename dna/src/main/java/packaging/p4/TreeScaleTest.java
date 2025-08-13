package packaging.p4;

import core.BaseSequence;
import core.dnarules.SuperBasicDNARules;
import datastructures.container.DNAContainer;
import datastructures.container.translation.DNAAddrManager;
import datastructures.reference.IDNASketch;
import datastructures.KVEntry;
import datastructures.searchtrees.BPlusTree;
import dnacoders.DistanceCoder;
import dnacoders.dnaconvertors.RotatingTre;
import dnacoders.tree.coders.BPTreeContainerCoder;
import dnacoders.tree.coders.BPTreeNativeCoder;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import dnacoders.tree.wrappers.node.DecodedNode;
import dnacoders.tree.wrappers.node.EncodedNode;
import dnacoders.tree.wrappers.tree.EncodedBPTree;
import utils.AsymmetricCoder;
import utils.Coder;
import utils.DNAPacker;
import utils.FuncUtils;
import utils.analyzing.Aggregator;
import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import utils.csv.CsvLine;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TreeScaleTest {

    public static void main(String[] args) {
        int count = Integer.parseInt(args[0]);
        BufferedCsvReader reader = new BufferedCsvReader("../gfbio.csv");
        List<CsvLine> list = reader.stream().limit(count).toList();
        reader.close();

        final long keySize = Integer.BYTES;
        final long pointerSize = Integer.BYTES;



        Coder<List<Integer>, BaseSequence> keysCoder = new Coder<>() {
            @Override
            public BaseSequence encode(List<Integer> sortedList) {

                BaseSequence seq = new BaseSequence();
                int id0 = sortedList.get(0);
                int id;
                int size = sortedList.size();
                DNAPacker.packUnsigned(seq, id0);
                for (int i = 1; i < size; i++) {
                    id = sortedList.get(i);
                    DNAPacker.packUnsigned(seq, id - id0);
                    id0 = id;
                }

                return seq;
            }

            @Override
            public List<Integer> decode(BaseSequence seq) {
                DNAPacker.LengthBase lb = DNAPacker.LengthBase.parsePrefix(seq);
                List<Integer> keys = new ArrayList<>();
                int delta = lb.unpackSingle(seq, false).intValue();
                keys.add(delta);
                seq = seq.window(lb.totalSize());

                while (seq.length() > 0) {
                    lb = DNAPacker.LengthBase.parsePrefix(seq);
                    delta += lb.unpackSingle(seq, false).intValue();
                    keys.add(delta);
                    seq = seq.window(lb.totalSize());
                }

                return keys;
            }
        };
        Coder<List<CsvLine>, BaseSequence> valuesCoder = new Coder<>() {
            Map<String, Integer> colMapping;
            @Override
            public BaseSequence encode(List<CsvLine> csvLines) {
                colMapping = csvLines.get(0).getColMapping();

                return RotatingTre.INSTANCE.encode(csvLines.stream().map(CsvLine::getLine).collect(Collectors.joining("\n")));
            }

            @Override
            public List<CsvLine> decode(BaseSequence seq) {
                return Arrays.stream(RotatingTre.INSTANCE.decode(seq).split("\n")).map(line -> new CsvLine(line, colMapping)).toList();
            }
        };

        BufferedCsvWriter writer = new BufferedCsvWriter("results.csv", true);
        if (writer.isEmpty())
            writer.appendNewLine(
                    "coder",
                    "time in seconds",
                    "tol leaves type",
                    "tol value leaf nodes",
                    "tol internal nodes type",
                    "tol value internal nodes",
                    "payload size",
                    "payload permutations",
                    "address permutations",
                    "num key-value pairs",
                    "sketcher",
                    "bytes pointers",
                    "bytes in leaves",
                    "total bytes",
                    "bit rate",
                    "bit rate leaves",
                    "bit rate internals",
                    "b",
                    "c",
                    "seed trials",
                    "error weight",
                    "dist weight",
                    "num nodes",
                    "num oligos",
                    "num oligos / node",
                    "num oligos of internal nodes",
                    "num oligos / internal node",
                    "num oligos of leaf nodes",
                    "num oligos / leaf",

                    "avg error addresses",
                    "avg error payloads",
                    "avg error oligos",
                    "avg error internal node oligos",
                    "avg error leaf node oligos",
                    "avg dist addresses",
                    "avg dist payloads",
                    "avg dist oligos",
                    "avg dist internal node oligos",
                    "avg dist leaf node oligos",
                    "avg score addresses",
                    "avg score payloads",
                    "avg score oligos",
                    "avg score internal node oligos",
                    "avg score leaf node oligos",

                    "min error addresses",
                    "min error payloads",
                    "min error oligos",
                    "min error internal node oligos",
                    "min error leaf node oligos",
                    "min dist addresses",
                    "min dist payloads",
                    "min dist oligos",
                    "min dist internal node oligos",
                    "min dist leaf node oligos",
                    "min score addresses",
                    "min score payloads",
                    "min score oligos",
                    "min score internal node oligos",
                    "min score leaf node oligos",

                    "max error addresses",
                    "max error payloads",
                    "max error oligos",
                    "max error internal node oligos",
                    "max error leaf node oligos",
                    "max dist addresses",
                    "max dist payloads",
                    "max dist oligos",
                    "max dist internal node oligos",
                    "max dist leaf node oligos",
                    "max score addresses",
                    "max score payloads",
                    "max score oligos",
                    "max score internal node oligos",
                    "max score leaf node oligos",


                    "sd error addresses",
                    "sd error payloads",
                    "sd error oligos",
                    "sd error internal node oligos",
                    "sd error leaf node oligos",
                    "sd dist addresses",
                    "sd dist payloads",
                    "sd dist oligos",
                    "sd dist internal node oligos",
                    "sd dist leaf node oligos",
                    "sd score addresses",
                    "sd score payloads",
                    "sd score oligos",
                    "sd score internal node oligos",
                    "sd score leaf node oligos"
            );

        float errorWeight = 1.0f;
        float distWeight = 1.0f;
        int addressSize = 80;
        var tolFactors = Stream.of(0.0f).map(f -> new Tol(n -> (int) (n * f), TolType.FACTOR, f)).toArray(Tol[]::new);


        for (int b = 2; b <= 1024; b *= 2) {
            for (int c = 2; c <= 1024; c *= 2) {
                for (int payloadSize : new int[] {150 - addressSize, 300 - addressSize, 1000 - addressSize, 10_000 - addressSize, 20_000 - addressSize}) {
                    for (int permutations : Arrays.asList(2)) {
                        BPlusTree<Integer, CsvLine> tree = BPlusTree.bulkLoad(FuncUtils.zip(IntStream.range(0, list.size()), list.stream(), KVEntry::new), b, c);

                        long bytesPointers = 0L;
                        long bytesLeaves = tree.bottomUpLevelIterator().next().stream().mapToLong(BPlusTree.Node::size).sum() * keySize
                                + list.stream().mapToLong(line -> line.getLine().length()).sum();
                        long totalBytes;

                        if (tree.getHeight() <= 1) {
                            totalBytes = bytesLeaves;
                        }
                        else {
                            bytesPointers = FuncUtils.stream(tree::bottomUpLevelIterator)
                                    .skip(1L)
                                    .flatMapToLong(level -> level.stream()
                                            .mapToLong(node -> {
                                                var internalNode = node.asInternalNode();
                                                long rPointer = internalNode.isAboveLeaf() ? 1L : 0L;
                                                return (rPointer + internalNode.getKids().size()) * pointerSize + internalNode.size() * keySize;
                                            }))
                                    .sum();
                            totalBytes = bytesLeaves + bytesPointers;
                        }

                        System.out.println("tree height: " + tree.getHeight());
                        System.out.println("num nodes: " + tree.getNumNodes());
                        System.out.println("num entries: " + tree.size());

                        var containerCoder = new BPTreeContainerCoder<>(
                                DNAContainer
                                        .builder()
                                        .setNumPayloadPermutations(permutations)
                                        .setPayloadSize(payloadSize)
                                        .setDnaRules(SuperBasicDNARules.INSTANCE)
                                        .setOligoLSH(MinHashLSH.newSeqLSHTraditional(6, 5))
                                        .setParallel(true)
                                        .setAddressManager(
                                                DNAAddrManager
                                                        .builder()
                                                        .setAddrSize(addressSize)
                                                        .setNumPermutations(permutations)
                                                        .build()
                                        )
                                        .build(),
                                keysCoder,
                                valuesCoder
                        );
                        System.out.println("coder: " + containerCoder.getClass().getSimpleName() + ", tol: 0 (disabled)");
                        System.out.println("tree (b, c): (" + b + ", " + c + ")\naddress size: " + addressSize + ", payload size: " + payloadSize + ", permutations: " + permutations + "\n");
                        test(
                                b,
                                c,
                                count,
                                tree,
                                totalBytes,
                                bytesPointers,
                                bytesLeaves,
                                writer,
                                addressSize,
                                null,
                                payloadSize,
                                permutations,
                                permutations,
                                new Tol(null, TolType.NONE, Float.NaN),
                                new Tol(null, TolType.NONE, Float.NaN),
                                -1,
                                errorWeight,
                                distWeight,
                                containerCoder
                        );

                        for (int trials : Arrays.asList(1)) {
                            for (var leafTol : tolFactors) {
                                for (var internalNodeTol : tolFactors) {
                                    if (internalNodeTol != leafTol)
                                        continue;
                                    for (var sketcher : Arrays.asList(
                                            AbstractHashSketcher.builder()
                                                    .setFlavor(AbstractHashSketcher.Builder.Flavor.F1)
                                                    .setAddressSize(addressSize)
                                                    .setLsh(MinHashLSH.newSeqLSHTraditional(6, 5))
                                                    .build()
                                    )) {

                                        var coder = new BPTreeNativeCoder.Builder<Integer, CsvLine, IDNASketch.HashSketch>()
                                                .setPayloadSize(payloadSize)
                                                .setSketcher(sketcher)
                                                .setLsh(MinHashLSH.newSeqLSHTraditional(6, 5))
                                                .setToleranceFunctionLeaves(leafTol.tolFunc)
                                                .setToleranceFunctionInternalNodes(internalNodeTol.tolFunc)
                                                .setKeyCoder(keysCoder)
                                                .setSeedTrialsWithMaxScore(1)
                                                .setPayloadPermutations(permutations)
                                                .setValueCoder(valuesCoder)
                                                .setDistanceCoderErrorRules(SuperBasicDNARules.INSTANCE)
                                                .build();

                                        System.out.println("coder: " + coder.getClass().getSimpleName());
                                        System.out.println("B+ tree config(b, c): (" + b + ", " + c + ")\naddress size: " + addressSize + ", payload size: " + payloadSize + ", payload permutations: " + permutations + "\nsketcher: " + sketcher.getClass().getSimpleName() + ", tols(leaf, internal node): (" + leafTol.value + "_" + leafTol.type + ", " + internalNodeTol.value + "_" + internalNodeTol.type + "), trials: " + trials + "\n");
                                        test(
                                                b,
                                                c,
                                                count,
                                                tree,
                                                totalBytes,
                                                bytesPointers,
                                                bytesLeaves,
                                                writer,
                                                addressSize,
                                                sketcher.getClass().getSimpleName(),
                                                payloadSize,
                                                permutations,
                                                permutations,
                                                leafTol,
                                                internalNodeTol,
                                                trials,
                                                errorWeight,
                                                distWeight,
                                                coder
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        writer.close();
    }

    private static <S extends IDNASketch, K extends Comparable<K>, V> void test(
            int b,
            int c,
            int count,
            BPlusTree<K, V> tree,
            long totalBytes,
            long internalsBytes,
            long leavesBytes,
            BufferedCsvWriter writer,
            int addressSize,
            String sketcherName,
            int payloadSize,
            int addressPermutations,
            int payloadPermutations,
            Tol tolLeaves,
            Tol tolInternalNodes,
            int trials,
            float errorWeight,
            float distWeight,
            AsymmetricCoder<BPlusTree<K, V>, Stream<DecodedNode<K, S>>, ? extends EncodedBPTree<K, V, ?, S>> coder
    ) {
        var encodedTreeTime = FuncUtils.time(() -> coder.encode(tree).getEncodedNodeStorage().collect());
        var encodedTree = encodedTreeTime.getT2();
        var time = encodedTreeTime.getT1() / 1000.0f;

        var addressesLSH = MinHashLSH.newSeqLSHTraditional(6, 5);
        var payloadsLSH = MinHashLSH.newSeqLSHTraditional(6, 5);
        var oligosLSH = MinHashLSH.newSeqLSHTraditional(6, 5);

        var oligos = encodedTree.stream().parallel().flatMap(en -> Arrays.stream(en.joinedOligos())).toList();
        var addresses = oligos.stream().parallel().map(o -> o.window(0, addressSize)).toList();
        var payloads = oligos.stream().parallel().map(o -> o.window(addressSize, o.length())).toList();

        var leafOligos = encodedTree.stream().parallel().filter(EncodedNode::isLeaf).flatMap(en -> Arrays.stream(en.joinedOligos())).toList();
        var internalNodeOligos = encodedTree.stream().parallel().filter(en -> !en.isLeaf()).flatMap(en -> Arrays.stream(en.joinedOligos())).toList();

        addressesLSH.insertParallel(addresses);
        payloadsLSH.insertParallel(payloads);
        oligosLSH.insertParallel(oligos);


        long numLeafNodes = tree.getHeight() > 1 ? tree.bottomUpLevelIterator().next().size() : 1;
        long numInternalNodes = tree.getNumNodes() - numLeafNodes;
        int numOligosOfInternalNodes = encodedTree.stream().filter(en -> !en.isLeaf()).mapToInt(en -> en.payloads().length).sum();
        int numOligosOfLeafNodes = encodedTree.stream().filter(EncodedNode::isLeaf).mapToInt(en -> en.payloads().length).sum();
        int numOligos = numOligosOfInternalNodes + numOligosOfLeafNodes;

        var errorAddresses = Aggregator.aggregateNumbers(addresses.stream().parallel().mapToDouble(TreeScaleTest::errorScore).toArray(), true);
        var errorPayloads = Aggregator.aggregateNumbers(payloads.stream().parallel().mapToDouble(TreeScaleTest::errorScore).toArray(), true);
        var errorOligos = Aggregator.aggregateNumbers(oligos.stream().parallel().mapToDouble(TreeScaleTest::errorScore).toArray(), true);
        var errorInternalNodeOligos = Aggregator.aggregateNumbers(internalNodeOligos.stream().parallel().mapToDouble(TreeScaleTest::errorScore).toArray(), true);
        var errorLeafNodesOligos = Aggregator.aggregateNumbers(leafOligos.stream().parallel().mapToDouble(TreeScaleTest::errorScore).toArray(), true);
        var distAddresses = Aggregator.aggregateNumbers(addresses.stream().parallel().mapToDouble(s -> distScore(s, addressesLSH)).toArray(), true);
        var distPayloads = Aggregator.aggregateNumbers(payloads.stream().parallel().mapToDouble(s -> distScore(s, payloadsLSH)).toArray(), true);
        var distOligos = Aggregator.aggregateNumbers(oligos.stream().parallel().mapToDouble(s -> distScore(s, oligosLSH)).toArray(), true);
        var distInternalNodeOligos = Aggregator.aggregateNumbers(internalNodeOligos.stream().parallel().mapToDouble(s -> distScore(s, oligosLSH)).toArray(), true);
        var distLeafNodeOligos = Aggregator.aggregateNumbers(leafOligos.stream().parallel().mapToDouble(s -> distScore(s, oligosLSH)).toArray(), true);
        var scoreAddresses = Aggregator.aggregateNumbers(addresses.stream().parallel().mapToDouble(seq -> score(seq, errorWeight, distWeight, oligosLSH)).toArray(), true);
        var scorePayloads = Aggregator.aggregateNumbers(payloads.stream().parallel().mapToDouble(seq -> score(seq, errorWeight, distWeight, oligosLSH)).toArray(), true);
        var scoreOligos = Aggregator.aggregateNumbers(oligos.stream().parallel().mapToDouble(seq -> score(seq, errorWeight, distWeight, oligosLSH)).toArray(), true);
        var scoreInternalNodeOligos = Aggregator.aggregateNumbers(internalNodeOligos.stream().parallel().mapToDouble(seq -> score(seq, errorWeight, distWeight, oligosLSH)).toArray(), true);
        var scoreLeafNodeOligos = Aggregator.aggregateNumbers(leafOligos.stream().parallel().mapToDouble(seq -> score(seq, errorWeight, distWeight, oligosLSH)).toArray(), true);



        writer.appendNewLine(
                Stream.of(
                        coder.getClass().getSimpleName(),
                                time,
                                tolLeaves.type,
                                tolLeaves.value,
                                tolInternalNodes.type,
                                tolInternalNodes.value,
                                payloadSize,
                                addressPermutations,
                                payloadPermutations,
                                count,
                                sketcherName,
                                internalsBytes,
                                leavesBytes,
                                totalBytes,
                                totalBytes * 8f / (numOligos * (payloadSize + addressSize)),
                                leavesBytes * 8f / (numOligosOfLeafNodes * (payloadSize + addressSize)),
                                internalsBytes * 8f / (numOligosOfInternalNodes * (payloadSize + addressSize)),
                                b,
                                c,
                                trials,
                                errorWeight,
                                distWeight,
                                encodedTree.size(),
                                numOligos,
                                numOligos / (double) encodedTree.size(),
                                numOligosOfInternalNodes,
                                numOligosOfInternalNodes / (double) numInternalNodes,
                                numOligosOfLeafNodes,
                                numOligosOfLeafNodes / (double) numLeafNodes,

                                errorAddresses.avg(),
                                errorPayloads.avg(),
                                errorOligos.avg(),
                                errorInternalNodeOligos.avg(),
                                errorLeafNodesOligos.avg(),
                                distAddresses.avg(),
                                distPayloads.avg(),
                                distOligos.avg(),
                                distInternalNodeOligos.avg(),
                                distLeafNodeOligos.avg(),
                                scoreAddresses.avg(),
                                scorePayloads.avg(),
                                scoreOligos.avg(),
                                scoreInternalNodeOligos.avg(),
                                scoreLeafNodeOligos.avg(),

                                errorAddresses.min(),
                                errorPayloads.min(),
                                errorOligos.min(),
                                errorInternalNodeOligos.min(),
                                errorLeafNodesOligos.min(),
                                distAddresses.min(),
                                distPayloads.min(),
                                distOligos.min(),
                                distInternalNodeOligos.min(),
                                distLeafNodeOligos.min(),
                                scoreAddresses.min(),
                                scorePayloads.min(),
                                scoreOligos.min(),
                                scoreInternalNodeOligos.min(),
                                scoreLeafNodeOligos.min(),

                                errorAddresses.max(),
                                errorPayloads.max(),
                                errorOligos.max(),
                                errorInternalNodeOligos.max(),
                                errorLeafNodesOligos.max(),
                                distAddresses.max(),
                                distPayloads.max(),
                                distOligos.max(),
                                distInternalNodeOligos.max(),
                                distLeafNodeOligos.max(),
                                scoreAddresses.max(),
                                scorePayloads.max(),
                                scoreOligos.max(),
                                scoreInternalNodeOligos.max(),
                                scoreLeafNodeOligos.max(),

                                errorAddresses.stdDev(),
                                errorPayloads.stdDev(),
                                errorOligos.stdDev(),
                                errorInternalNodeOligos.stdDev(),
                                errorLeafNodesOligos.stdDev(),
                                distAddresses.stdDev(),
                                distPayloads.stdDev(),
                                distOligos.stdDev(),
                                distInternalNodeOligos.stdDev(),
                                distLeafNodeOligos.stdDev(),
                                scoreAddresses.stdDev(),
                                scorePayloads.stdDev(),
                                scoreOligos.stdDev(),
                                scoreInternalNodeOligos.stdDev(),
                                scoreLeafNodeOligos.stdDev()
                        )
                        .map(Objects::toString).toArray(String[]::new)
        );
        writer.flush();
    }

    public static float errorScore(BaseSequence seq) {
        return seq.getProperty(AbstractHashSketcher.ERROR_PROPERTY_NAME, () -> SuperBasicDNARules.INSTANCE.evalErrorProbability(seq));
    }

    public static float distScore(BaseSequence seq, LSH<BaseSequence> lsh) {
        return seq.getProperty(AbstractHashSketcher.DIST_PROPERTY_NAME, () -> Math.min(DistanceCoder.distanceScoreExclusive(seq, lsh), DistanceCoder.distanceScore(seq.complement(), lsh)));
    }

    public static float score(BaseSequence seq, float errorWeight, float distanceWeight, LSH<BaseSequence> lsh) {
        return seq.getProperty(AbstractHashSketcher.SCORE_PROPERTY_NAME, () -> -errorWeight * errorScore(seq) + distanceWeight * distScore(seq, lsh));
    }

    enum TolType {
        FACTOR, ABSOLUTE, NONE
    }
    record Tol(Function<Integer, Integer> tolFunc, TolType type, float value) {

    }
}
