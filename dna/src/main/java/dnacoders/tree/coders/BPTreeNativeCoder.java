package dnacoders.tree.coders;

import core.Base;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.DNARule;
import datastructures.container.BlockingContainer;
import datastructures.container.Container;
import datastructures.reference.IDNASketch;
import datastructures.searchtrees.BPlusTree;
import dnacoders.BasicSegmentationCoder;
import dnacoders.PayloadDistanceCoder;
import dnacoders.headercoders.BasicDNAPadder;
import dnacoders.tree.wrappers.tree.EncodedBPTree;
import dnacoders.tree.wrappers.tree.LNALNativeEncodedTree;
import dnacoders.tree.encodednodestorage.EncodedNodeNativeMapStorage;
import dnacoders.tree.sketchers.IDNASketcher;
import dnacoders.tree.wrappers.node.*;
import utils.*;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BPTreeNativeCoder<K extends Comparable<K>, V, S extends IDNASketch> implements BPTreeAsymmetricCoder<K, V, S, BaseSequence[]> {

    private final Base leafMarker;
    private final Base internalNodeWithRightMarker;
    private final Base internalNodeWithoutRightMarker;


    private final LSH<BaseSequence> lsh;
    private final DNARule errorRule;

    private final int payloadSize;

    private final float errorWeight;
    protected final float distWeight;

    private final int payloadPermutations;

    private final Function<Integer, Integer> toleranceFunctionInternalNodes;
    private final Function<Integer, Integer> toleranceFunctionLeaves;


    private final IDNASketcher<S> sketcher;
    private final AsymmetricCoder<S, IDNASketcher.DecodedSketch<S>, BaseSequence> sketchCoder;
    private final boolean isParallel;

    private final int numGcCorrections;

    private final boolean partitionedPayload;
    private final int partitionedPayloadSize;

    private final int seedTrials;
    private final float targetScore;

    private final Coder<List<K>, BaseSequence> keyCoder;
    private final Coder<List<V>, BaseSequence> valueCoder;
    private final Coder<BaseSequence, BaseSequence[]> segmentationCoder;
    private final Coder<AddressedDNA[], AddressedDNA[]> distanceCoder;


    private BPTreeNativeCoder(
            int payloadSize,
            Base leafMarker,
            Base internalNodeWithRightMarker,
            Base internalNodeWithoutRightMarker,
            int payloadPermutations,
            LSH<BaseSequence> lsh,
            float errorWeight,
            float distWeight,
            DNARule errorRule,
            Function<Integer, Integer> toleranceFunctionInternalNodes,
            Function<Integer, Integer> toleranceFunctionLeaves,
            Coder<List<K>, BaseSequence> keyCoder,
            Coder<List<V>, BaseSequence> valueCoder,
            int seedTrials,
            float targetScore,
            IDNASketcher<S> sketcher,
            boolean isParallel,
            int numGcCorrections,
            boolean partitionedPayload,
            int partitionedPayloadSize) {

        this.payloadSize = payloadSize;
        this.leafMarker = leafMarker;
        this.internalNodeWithRightMarker = internalNodeWithRightMarker;
        this.internalNodeWithoutRightMarker = internalNodeWithoutRightMarker;
        this.payloadPermutations = payloadPermutations;
        this.lsh = lsh;
        this.errorWeight = errorWeight;
        this.distWeight = distWeight;
        this.errorRule = errorRule;
        this.toleranceFunctionInternalNodes = toleranceFunctionInternalNodes;
        this.toleranceFunctionLeaves = toleranceFunctionLeaves;
        this.keyCoder = keyCoder;
        this.valueCoder = valueCoder;
        this.sketcher = sketcher;
        this.seedTrials = seedTrials;
        this.targetScore = targetScore;
        this.isParallel = isParallel;
        this.sketchCoder = sketcher.coder();
        this.numGcCorrections = numGcCorrections;
        this.partitionedPayload = partitionedPayload;
        this.partitionedPayloadSize = partitionedPayloadSize;

        PayloadDistanceCoder payloadDistanceCoder = new PayloadDistanceCoder(false, partitionedPayload, partitionedPayloadSize, lsh, errorRule, payloadPermutations, errorWeight, distWeight);

        this.segmentationCoder = new BasicSegmentationCoder(
                payloadSize - payloadDistanceCoder.permutationOverhead(),
                numGcCorrections,
                BasicDNAPadder.FILLER_RANDOM
        );
        this.distanceCoder = isParallel ?
                Coder.arrayMapperParallel(payloadDistanceCoder, AddressedDNA[]::new, AddressedDNA[]::new) :
                Coder.arrayMapper(payloadDistanceCoder, AddressedDNA[]::new, AddressedDNA[]::new);
    }

    @Override
    public EncodedBPTree<K, V, BaseSequence[], S> encode(BPlusTree<K, V> tree) {
        return isParallel ? encodeParallel(tree) : encodeSequentially(tree);
    }


    public LNALNativeEncodedTree<K, V, S> encodeSequentially(BPlusTree<K, V> tree) {
        if (tree == null || tree.size() <= 0)
            return new LNALNativeEncodedTree<>(this::decodeNode, new EncodedNodeNativeMapStorage<>(Collections.emptyList(), null, isParallel));

        List<EncodedNode<S>> nodesList = new ArrayList<>(tree.getNumNodes());
        if (tree.getRoot().isLeaf()) {
            encodeNode(tree.getRoot().asLeafNode(), null, null, nodesList);
            return new LNALNativeEncodedTree<>(this::decodeNode, new EncodedNodeNativeMapStorage<>(nodesList, nodesList.get(nodesList.size() - 1), isParallel));
        }


        Iterator<List<BPlusTree.Node<K, V>>> it = tree.bottomUpLevelIterator();
        List<BPlusTree.Node<K, V>> leaves = it.next();

        Map<BPlusTree.Node<K, V>, EncodedNode<S>> map = new HashMap<>();

        ListIterator<BPlusTree.Node<K, V>> leavesIt = leaves.listIterator(leaves.size());
        BPlusTree.Node<K, V> right = leavesIt.previous();
        BPlusTree.Node<K, V> left = leavesIt.previous();
        EncodedNode<S> encodedRightLeaf = encodeNode(right, null, null, nodesList);
        LinkedList<EncodedNode<S>> encodedLeaves = new LinkedList<>();
        encodedLeaves.add(encodedRightLeaf);
        BPlusTree.InternalNode<K, V> parent = right.getParent();
        EncodedNode<S> encodedRightParent = null;
        do {
            if (left.getParent() != parent) {
                encodedRightParent = encodeNode(parent, encodedRightParent, encodedLeaves, nodesList);
                map.put(parent, encodedRightParent);
                encodedLeaves = new LinkedList<>();
                parent = left.getParent();
            }

            encodedRightLeaf = encodeNode(left, null, null, nodesList);
            encodedLeaves.addFirst(encodedRightLeaf);
            left = left.getLeft();
            if (left == null) {
                EncodedNode<S> encodedParent = encodeNode(parent, encodedRightParent, encodedLeaves, nodesList);
                map.put(parent, encodedParent);
                break;
            }

        } while (true);

        parent = leaves.get(0).getParent();
        Set<BPlusTree.InternalNode<K, V>> nodeGroups;
        while (parent.getParent() != null) {
            nodeGroups = Stream.iterate(parent, Objects::nonNull, BPlusTree.InternalNode::getRight).map(BPlusTree.Node::getParent).collect(Collectors.toSet());

            for (var k : nodeGroups) {
                EncodedNode<S> encodedParent = encodeNode(k, null, k.getKids().stream().map(map::get).toList(), nodesList);
                map.put(k, encodedParent);
            }

            parent = parent.getParent();
            if (parent == null)
                break;

            nodeGroups.forEach(node -> node.getKids().forEach(map::remove));
        }

        return new LNALNativeEncodedTree<>(this::decodeNode, new EncodedNodeNativeMapStorage<>(nodesList, nodesList.get(nodesList.size() - 1), isParallel));
    }


    public LNALNativeEncodedTree<K, V, S> encodeParallel(BPlusTree<K, V> tree) {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        if (tree == null || tree.size() <= 0) {
            pool.close();
            return new LNALNativeEncodedTree<>(this::decodeNode, new EncodedNodeNativeMapStorage<>(Collections.emptyList(), null, isParallel));
        }

        Queue<EncodedNode<S>> nodesQueue = new ArrayBlockingQueue<>(tree.getNumNodes());
        if (tree.getRoot().isLeaf()) {
            var root = encodeNode(tree.getRoot(), null, null, nodesQueue);
            pool.close();
            return new LNALNativeEncodedTree<>(this::decodeNode, new EncodedNodeNativeMapStorage<>(nodesQueue.stream().toList(), root, isParallel));
        }


        Iterator<List<BPlusTree.Node<K, V>>> it = tree.bottomUpLevelIterator();
        List<BPlusTree.Node<K, V>> leaves = it.next();

        Container<BPlusTree.Node<K, V>, Future<EncodedNode<S>>> map = new BlockingContainer<>(new Container.MapContainer<>());

        ListIterator<BPlusTree.Node<K, V>> leavesIt = leaves.listIterator(leaves.size());
        BPlusTree.Node<K, V> right = leavesIt.previous();
        BPlusTree.Node<K, V> left = leavesIt.previous();
        BPlusTree.InternalNode<K, V> parent = right.getParent();

        Future<EncodedNode<S>> encodedRightLeaf = pool.submit(() -> encodeNode(right, null, null, nodesQueue));
        LinkedList<Future<EncodedNode<S>>> encodedLeaves = new LinkedList<>();
        encodedLeaves.add(encodedRightLeaf);
        Future<EncodedNode<S>> encodedRightParent = null;
        do {
            if (left.getParent() != parent) {
                BPlusTree.InternalNode<K, V> finalParent = parent;
                Future<EncodedNode<S>> finalEncodedRightParent = encodedRightParent;
                LinkedList<Future<EncodedNode<S>>> finalEncodedLeaves = encodedLeaves;
                Future<EncodedNode<S>> encodedParent = pool.submit(
                        () -> encodeNode(
                                finalParent,
                                finalEncodedRightParent != null ? finalEncodedRightParent.get() : null,
                                finalEncodedLeaves.stream().map(f -> FuncUtils.safeCall(f::get)).toList(),
                                nodesQueue
                        )
                );
                BPlusTree.InternalNode<K, V> finalParent3 = parent;
                pool.submit(() -> map.put(finalParent3, encodedParent));
                encodedLeaves = new LinkedList<>();
                parent = left.getParent();
                encodedRightParent = encodedParent;
            }

            BPlusTree.Node<K, V> finalLeft = left;

            encodedRightLeaf = pool.submit(() -> encodeNode(finalLeft, null, null, nodesQueue));
            encodedLeaves.addFirst(encodedRightLeaf);
            left = left.getLeft();
            if (left == null) {
                Future<EncodedNode<S>> finalEncodedRightParent1 = encodedRightParent;
                BPlusTree.InternalNode<K, V> finalParent1 = parent;
                LinkedList<Future<EncodedNode<S>>> finalEncodedLeaves1 = encodedLeaves;
                Future<EncodedNode<S>> encodedParent = pool.submit(() -> encodeNode(finalParent1, finalEncodedRightParent1 != null ? finalEncodedRightParent1.get() : null, finalEncodedLeaves1.stream().map(f -> FuncUtils.safeCall(f::get)).toList(), nodesQueue));
                pool.submit(() -> map.put(finalParent1, encodedParent));
                break;
            }
        } while (true);


        parent = leaves.get(0).getParent();
        Set<BPlusTree.InternalNode<K, V>> nodeGroups;
        while (parent.getParent() != null) {
            nodeGroups = Stream.iterate(parent, Objects::nonNull, BPlusTree.InternalNode::getRight).map(BPlusTree.Node::getParent).collect(Collectors.toSet());
            nodeGroups.forEach(p -> {
                Future<EncodedNode<S>> encodedParent = pool.submit(
                        () -> encodeNode(
                                p,
                                null,
                                p.getKids().stream().map(map::get).map(f -> FuncUtils.safeCall(f::get)).toList(),
                                nodesQueue
                        )
                );
                pool.submit(() -> map.put(p, encodedParent));
            });

            parent = parent.getParent();
            if (parent == null)
                break;

            nodeGroups.forEach(node -> pool.submit(() -> node.getKids().forEach(map::remove)));
        }
        pool.close();

        List<EncodedNode<S>> nodes = nodesQueue.stream().toList();
        return new LNALNativeEncodedTree<>(this::decodeNode, new EncodedNodeNativeMapStorage<>(nodes, nodes.get(nodes.size() - 1), isParallel));
    }

    public Stream<DecodedNode<K, S>> decodeOligoStream(Stream<BaseSequence[]> oligos) {
        int addrSize = getAddressSize();
        return oligos.map(as -> Arrays.stream(as).map(s -> new AddressedDNA(s.window(0, addrSize), s.window(addrSize))).toArray(AddressedDNA[]::new)).map(this::decodeOligos);
    }

    @Override
    public Stream<DecodedNode<K, S>> decode(EncodedBPTree<K, V, BaseSequence[], S> encodedTree) {
        return encodedTree.getEncodedNodeStorage().stream().map(this::decodeNode);
    }

    private EncodedNode<S> encodeNode(BPlusTree.Node<K, V> node, EncodedNode<S> rightNode, List<EncodedNode<S>> encodedKids, Collection<EncodedNode<S>> nodesCollection) {
        List<K> keys = node.getKeys();
        BaseSequence seq = new BaseSequence();

        BaseSequence encodedKeys = keyCoder.encode(keys);
        DNAPacker.packUnsigned(seq, encodedKeys.length() - 1);
        seq.append(encodedKeys);

        if (node.isLeaf()) {
            seq.append(leafMarker);
            seq.append(valueCoder.encode(node.asLeafNode().getValues()));
            return finalizeAsEncodedNode(seq, true, nodesCollection);
        }

        if (rightNode != null) {
            seq.append(internalNodeWithRightMarker);
            seq.append(sketchCoder.encode(rightNode.sketch()));
        }
        else {
            seq.append(internalNodeWithoutRightMarker);
        }

        for (EncodedNode<S> encodedKid : encodedKids)
            seq.append(sketchCoder.encode(encodedKid.sketch()));

        return finalizeAsEncodedNode(seq, false, nodesCollection);
    }

    private EncodedNodeTrial asEncodedNodeTrial(BaseSequence[] payloads, boolean isLeaf) {
        S sketch = sketcher.createSketch(payloads.length, toleranceFunctionLeaves.apply(payloads.length));
        BaseSequence[] addresses = sketch.addresses();
        AddressedDNA[] oligos = distanceCoder.encode(FuncUtils.zip(Arrays.stream(addresses), Arrays.stream(payloads), AddressedDNA::new).toArray(AddressedDNA[]::new));
        return new EncodedNodeTrial(
                oligos,
                () -> new EncodedNode<>(
                        isLeaf,
                        Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new),
                        sketch,
                        Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new)
                )
        );
    }

    private EncodedNode<S> finalizeAsEncodedNode(BaseSequence seq, boolean isLeaf, Collection<EncodedNode<S>> nodesCollection) {
        BaseSequence[] payloads = segmentationCoder.encode(seq);
        if (seedTrials == 1) {
            var encodedNode = asEncodedNodeTrial(payloads, isLeaf).getEncodedNode();
            nodesCollection.add(encodedNode);
            return encodedNode;
        }

        List<EncodedNodeTrial> trials = new ArrayList<>(seedTrials);
        for (int i = 0; i < seedTrials; i++) {
            var ent = asEncodedNodeTrial(payloads, isLeaf);
            if (ent.fulfillsTargetScore()) {
                var encodedNode = ent.getEncodedNode();
                nodesCollection.add(encodedNode);
                return encodedNode;
            }

            trials.add(ent);
        }

        return trials.stream()
                .map(ent ->
                        new Pair<>(
                                ent,
                                Arrays.stream(ent.oligos).mapToDouble(o -> ((PayloadDistanceCoder.ScoredAddressedDNA) o).score()).sum()
                        )
                )
                .max(Comparator.comparing(Pair::getT2))
                .map(p -> {
                    var result = p.getT1().getEncodedNode();
                    nodesCollection.add(result);
                    return result;
                })
                .orElseThrow();
    }

    @Override
    public DecodedNode<K, S> decodeNode(EncodedNode<S> node) {
        return decodeOligos(node.oligos());
    }

    private DecodedNode<K, S> decodeOligos(AddressedDNA[] oligos) {
        var distanceReversed = Arrays.stream(distanceCoder.decode(oligos)).map(AddressedDNA::payload).toArray(BaseSequence[]::new);
        var seq = segmentationCoder.decode(distanceReversed);
        DNAPacker.LengthBase lb = DNAPacker.LengthBase.parsePrefix(seq);
        int encodedKeysLen = lb.unpackSingle(seq, false).intValue() + 1;
        int lbTotalSize = lb.totalSize();
        int lastIndex = lbTotalSize + encodedKeysLen;
        List<K> keys = keyCoder.decode(seq.window(lbTotalSize, lastIndex));
        BaseSequence window = seq.window(lastIndex);
        Base marker = window.get(0);
        window = window.window(1);
        if (marker== leafMarker)
            return new DecodedLeafNode<>(keys, valueCoder.decode(window));

        S rightHash = null;
        if (marker == internalNodeWithRightMarker) {
            IDNASketcher.DecodedSketch<S> decodedRightHash = sketchCoder.decode(window);
            rightHash = decodedRightHash.sketch();
            window = window.window(decodedRightHash.serializedSize());
        }

        Stream.Builder<S> kidsHashesBuilder = Stream.builder();
        while (window.length() > 0) {
            IDNASketcher.DecodedSketch<S> decodedKidHash = sketchCoder.decode(window);
            kidsHashesBuilder.add(decodedKidHash.sketch());
            window = window.window(decodedKidHash.serializedSize());
        }

        List<S> kidsHashes = kidsHashesBuilder.build().toList();
        if (rightHash == null)
            return new DecodedInternalNode<>(keys, kidsHashes);

        return new DecodedInternalNodeAboveLeaf<>(keys, kidsHashes, rightHash);
    }

    @Override
    public boolean isParallel() {
        return isParallel;
    }

    public DNARule getErrorRule() {
        return errorRule;
    }

    public float getErrorWeight() {
        return errorWeight;
    }

    public float getDistWeight() {
        return distWeight;
    }

    public int getPayloadPermutations() {
        return payloadPermutations;
    }

    public Function<Integer, Integer> getToleranceFunctionInternalNodes() {
        return toleranceFunctionInternalNodes;
    }

    public Function<Integer, Integer> getToleranceFunctionLeaves() {
        return toleranceFunctionLeaves;
    }

    public boolean isPartitionedPayload() {
        return partitionedPayload;
    }

    public int getPartitionedPayloadSize() {
        return partitionedPayloadSize;
    }

    public int getSeedTrials() {
        return seedTrials;
    }

    public float getTargetScore() {
        return targetScore;
    }

    public Coder<List<K>, BaseSequence> getKeyCoder() {
        return keyCoder;
    }

    public Coder<List<V>, BaseSequence> getValueCoder() {
        return valueCoder;
    }

    public AsymmetricCoder<S, IDNASketcher.DecodedSketch<S>, BaseSequence> getSketchCoder() {
        return sketchCoder;
    }

    public IDNASketcher<S> getSketcher() {
        return sketcher;
    }

    public LSH<BaseSequence> getLsh() {
        return lsh;
    }

    public int getNumGcCorrections() {
        return numGcCorrections;
    }

    public int getAddressSize() {
        return sketcher.addressSize();
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public int getOligoSize() {
        return getAddressSize() + getPayloadSize();
    }

    private class EncodedNodeTrial {
        private final AddressedDNA[] oligos;
        private final FutureTask<EncodedNode<S>> encodedNodeFuture;
        private final AtomicBoolean isStarted;

        public EncodedNodeTrial(AddressedDNA[] oligos, Callable<EncodedNode<S>> encodedNodeFuture) {
            this.oligos = oligos;
            this.encodedNodeFuture = new FutureTask<>(encodedNodeFuture);
            this.isStarted = new AtomicBoolean(false);
        }

        public boolean fulfillsTargetScore() {
            return !Float.isInfinite(targetScore) && Arrays.stream(oligos).allMatch(o -> ((PayloadDistanceCoder.ScoredAddressedDNA) o).score() >= targetScore);
        }

        public EncodedNode<S> getEncodedNode() {
            if (!isStarted.getAndSet(true))
                encodedNodeFuture.run();

            return FuncUtils.safeCall(encodedNodeFuture::get);
        }
    }

    public static class Builder<K extends Comparable<K>, V, S extends IDNASketch> {

        public static final Base DEFAULT_LEAF_MARKER_BASE = Base.G;
        public static final Base DEFAULT_INTERNAL_NODE_WITH_RIGHT_MARKER = Base.C;
        public static final Base DEFAULT_INTERNAL_NODE_WITHOUT_RIGHT_MARKER = Base.T;

        public static final int DEFAULT_PAYLOAD_PERMUTATIONS = 8;
        public static final int DEFAULT_SEED_TRIALS = 1;
        public static final float DEFAULT_TARGET_SCORE = 1;
        public static final int DEFAULT_NUM_GC_CORRECTIONS = 0;

        public static final BiFunction<Integer, Integer, LSH<BaseSequence>> DEFAULT_LSH = (addrSize, payloadSize) -> MinHashLSH.newSeqLSHTraditional(1 + 4 * Math.max(1, (addrSize + payloadSize) / 200), 5);

        public static final int DEFAULT_PAYLOAD_SIZE = 170;

        public static final float DEFAULT_DISTANCE_CODER_ERROR_WEIGHT = 1.0f;
        public static final float DEFAULT_DISTANCE_CODER_DIST_WEIGHT = 1.0f;

        public static final boolean DEFAULT_PARALLEL = true;

        public static final DNARule DEFAULT_DISTANCE_CODER_ERROR_RULES = BasicDNARules.INSTANCE;

        public static final int DEFAULT_PARTITIONED_PAYLOAD_SIZE = 300;
        public static final Function<Integer, Boolean> DEFAULT_PARTITIONED_PAYLOAD_DISTANCE_CODER = size -> size > DEFAULT_PARTITIONED_PAYLOAD_SIZE;


        public static final Function<Integer, Integer> DEFAULT_TOLERANCE_FUNCTION_LEAVES = n -> (int) (0.1f * n);
        public static final Function<Integer, Integer> DEFAULT_TOLERANCE_FUNCTION_INTERNAL_NODES = n -> (int) (0.1f * n);



        private Base leafMarker;
        private Base internalNodeWithRightMarker;
        private Base internalNodeWithoutRightMarker;

        private Integer payloadPermutations;
        private LSH<BaseSequence> lsh;

        private Integer payloadSize;

        private Float distanceCoderErrorWeight;
        private Float distanceCoderDistWeight;

        private DNARule distanceCoderErrorRules;

        private Boolean isParallel;

        private Boolean partitionedPayloadDistanceCoder;
        private Integer partitionedPayloadSize;

        private Integer seedTrials;
        private Float targetScore;

        private Integer numGcCorrections;

        private Function<Integer, Integer> toleranceFunctionLeaves;
        private Function<Integer, Integer> toleranceFunctionInternalNodes;

        private Coder<List<K>, BaseSequence> keyCoder;
        private Coder<List<V>, BaseSequence> valueCoder;

        private IDNASketcher<S> sketcher;

        public BPTreeNativeCoder<K, V, S> build() {
            if (sketcher == null)
                throw new RuntimeException("IDNASketcher must be set");

            this.leafMarker = FuncUtils.nullEscape(leafMarker, () -> DEFAULT_LEAF_MARKER_BASE);
            this.internalNodeWithRightMarker = FuncUtils.nullEscape(internalNodeWithRightMarker, () -> DEFAULT_INTERNAL_NODE_WITH_RIGHT_MARKER);
            this.internalNodeWithoutRightMarker = FuncUtils.nullEscape(internalNodeWithoutRightMarker, () -> DEFAULT_INTERNAL_NODE_WITHOUT_RIGHT_MARKER);

            this.payloadPermutations = FuncUtils.conditionOrElse(p -> p != null && p >= 0, payloadPermutations, () -> DEFAULT_PAYLOAD_PERMUTATIONS);

            this.payloadSize = FuncUtils.conditionOrElse(p -> p != null && p > 0, payloadSize, () -> DEFAULT_PAYLOAD_SIZE);

            this.distanceCoderErrorWeight = FuncUtils.nullEscape(distanceCoderErrorWeight, () -> DEFAULT_DISTANCE_CODER_ERROR_WEIGHT);
            this.distanceCoderDistWeight = FuncUtils.nullEscape(distanceCoderDistWeight, () -> DEFAULT_DISTANCE_CODER_DIST_WEIGHT);

            this.distanceCoderErrorRules = FuncUtils.nullEscape(distanceCoderErrorRules, () -> DEFAULT_DISTANCE_CODER_ERROR_RULES);

            this.lsh = FuncUtils.nullEscape(lsh, () -> DEFAULT_LSH.apply(sketcher.addressSize(), payloadSize));

            this.isParallel = FuncUtils.nullEscape(isParallel, () -> DEFAULT_PARALLEL);

            this.numGcCorrections = FuncUtils.nullEscape(numGcCorrections, DEFAULT_NUM_GC_CORRECTIONS);
            this.toleranceFunctionLeaves = FuncUtils.nullEscape(toleranceFunctionLeaves, () -> DEFAULT_TOLERANCE_FUNCTION_LEAVES);
            this.toleranceFunctionInternalNodes = FuncUtils.nullEscape(toleranceFunctionInternalNodes, () -> DEFAULT_TOLERANCE_FUNCTION_INTERNAL_NODES);

            this.seedTrials = FuncUtils.conditionOrElse(t -> t != null && t >= 1, seedTrials, () -> DEFAULT_SEED_TRIALS);
            this.targetScore = FuncUtils.nullEscape(targetScore, () -> DEFAULT_TARGET_SCORE);

            this.partitionedPayloadDistanceCoder = FuncUtils.nullEscape(partitionedPayloadDistanceCoder, DEFAULT_PARTITIONED_PAYLOAD_DISTANCE_CODER.apply(payloadSize));
            this.partitionedPayloadSize = FuncUtils.nullEscape(partitionedPayloadSize, DEFAULT_PARTITIONED_PAYLOAD_SIZE);

            if (keyCoder == null)
                keyCoder = listCoder();

            if (valueCoder == null)
                valueCoder = listCoder();

            return new BPTreeNativeCoder<>(
                    payloadSize,
                    leafMarker,
                    internalNodeWithRightMarker,
                    internalNodeWithoutRightMarker,
                    payloadPermutations,
                    lsh,
                    distanceCoderErrorWeight,
                    distanceCoderDistWeight,
                    distanceCoderErrorRules,
                    toleranceFunctionInternalNodes,
                    toleranceFunctionLeaves,
                    keyCoder,
                    valueCoder,
                    seedTrials,
                    targetScore,
                    sketcher,
                    isParallel,
                    numGcCorrections,
                    partitionedPayloadDistanceCoder,
                    partitionedPayloadSize
            );

        }

        public Builder<K, V, S> setPartitionedPayloadDistanceCoder(boolean partitionedPayloadDistanceCoder) {
            this.partitionedPayloadDistanceCoder = partitionedPayloadDistanceCoder;
            return this;
        }

        public Builder<K, V, S> setPartitionedPayloadSize(int partitionedPayloadSize) {
            this.partitionedPayloadSize = partitionedPayloadSize;
            return this;
        }

        public Builder<K, V, S> setDistanceCoderErrorRules(DNARule distanceCoderErrorRules) {
            this.distanceCoderErrorRules = distanceCoderErrorRules;
            return this;
        }

        public Builder<K, V, S> setPayloadPermutations(Integer payloadPermutations) {
            this.payloadPermutations = payloadPermutations;
            return this;
        }

        public Builder<K, V, S> setPayloadSize(Integer payloadSize) {
            this.payloadSize = payloadSize;
            return this;
        }

        public Builder<K, V, S> setDistanceCoderErrorWeight(Float distanceCoderErrorWeight) {
            this.distanceCoderErrorWeight = distanceCoderErrorWeight;
            return this;
        }

        public Builder<K, V, S> setDistanceCoderDistWeight(Float distanceCoderDistWeight) {
            this.distanceCoderDistWeight = distanceCoderDistWeight;
            return this;
        }

        public static <S> Coder<List<S>, BaseSequence> listCoder() {
            return Coder.of(
                    list -> SeqBitStringConverter.transform(Packer.withBytePadding(new BitString(FuncUtils.serializeToByteArraySafe((Serializable) list)))),
                    seq -> FuncUtils.deserializeFromByteArraySafe(Packer.withoutBytePadding(SeqBitStringConverter.transform(seq)).toBytes())
            );
        }

        public Builder<K, V, S> setNumGcCorrections(int numGcCorrections) {
            this.numGcCorrections = numGcCorrections;
            return this;
        }

        public Builder<K, V, S> setKeyCoder(Coder<List<K>, BaseSequence> keyCoder) {
            this.keyCoder = keyCoder;
            return this;
        }

        public Builder<K, V, S> setValueCoder(Coder<List<V>, BaseSequence> valueCoder) {
            this.valueCoder = valueCoder;
            return this;
        }

        public Builder<K, V, S> setSeedTrialsAndTargetScore(int seedTrials, float targetScore) {
            this.seedTrials = seedTrials;
            this.targetScore = targetScore;
            return this;
        }

        public Builder<K, V, S> setSeedTrialsWithMaxScore(int seedTrials) {
            return setSeedTrialsAndTargetScore(seedTrials, Float.POSITIVE_INFINITY);
        }

        public Builder<K, V, S> setTargetScoreWithMaxTrials(float targetScore) {
            return setSeedTrialsAndTargetScore(Integer.MAX_VALUE, targetScore);
        }


        public Builder<K, V, S> setNodeMarkers(Base leafMarker, Base internalNodeWithRightMarker, Base internalNodeWithoutRightMarker) {
            if (EnumSet.of(leafMarker, internalNodeWithRightMarker, internalNodeWithoutRightMarker).size() == 3) {
                this.leafMarker = leafMarker;
                this.internalNodeWithRightMarker = internalNodeWithRightMarker;
                this.internalNodeWithoutRightMarker = internalNodeWithoutRightMarker;
                return this;
            }

            throw new RuntimeException("duplicate markers found: " + leafMarker +  ", " + internalNodeWithRightMarker + ", " + internalNodeWithoutRightMarker);
        }

        public Builder<K, V, S> setPayloadPermutations(int payloadPermutations) {
            this.payloadPermutations = payloadPermutations;
            return this;
        }

        public Builder<K, V, S> setLsh(LSH<BaseSequence> lsh) {
            this.lsh = lsh;
            return this;
        }

        public Builder<K, V, S> setPayloadSize(int payloadSize) {
            this.payloadSize = payloadSize;
            return this;
        }

        public Builder<K, V, S> setParallel(boolean parallel) {
            isParallel = parallel;
            return this;
        }

        public Builder<K, V, S> setToleranceFunctionLeaves(Function<Integer, Integer> toleranceFunctionLeaves) {
            this.toleranceFunctionLeaves = toleranceFunctionLeaves;
            return this;
        }

        public Builder<K, V, S> setToleranceFunctionInternalNodes(Function<Integer, Integer> toleranceFunctionInternalNodes) {
            this.toleranceFunctionInternalNodes = toleranceFunctionInternalNodes;
            return this;
        }

        public Builder<K, V, S> setSketcher(IDNASketcher<S> sketcher) {
            this.sketcher = sketcher;
            return this;
        }
    }
}
