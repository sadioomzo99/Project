package dnacoders.tree.coders;

import core.Base;
import core.BaseSequence;
import datastructures.container.Container;
import datastructures.container.DNAContainer;
import datastructures.reference.IDNASketch;
import datastructures.container.BlockingContainer;
import datastructures.searchtrees.BPlusTree;
import dnacoders.tree.wrappers.tree.EncodedBPTree;
import dnacoders.tree.wrappers.tree.LNALContainerEncodedTree;
import dnacoders.tree.encodednodestorage.EncodedNodeContainerStorage;
import dnacoders.tree.wrappers.node.DecodedInternalNodeAboveLeaf;
import dnacoders.tree.wrappers.node.DecodedLeafNode;
import dnacoders.tree.wrappers.node.DecodedNode;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class BPTreeContainerCoder<K extends Comparable<K>, V> implements BPTreeAsymmetricCoder<K, V, IDNASketch.ContainerIdSketch, Long> {

    public static final Base LEAF_MARKER_BASE = Base.G;
    public static final Base INTERNAL_NODE_HAS_RIGHT_MARKER = Base.C;
    public static final Base INTERNAL_NODE_HAS_NO_RIGHT_MARKER = Base.T;

    private final DNAContainer container;
    private final Coder<List<K>, BaseSequence> keyCoder;
    private final Coder<List<V>, BaseSequence> valueCoder;

    public BPTreeContainerCoder(DNAContainer container, Coder<List<K>, BaseSequence> keyCoder, Coder<List<V>, BaseSequence> valueCoder) {
        this.container = container;
        this.keyCoder = keyCoder;
        this.valueCoder = valueCoder;
    }

    public BPTreeContainerCoder(DNAContainer container) {
        this(container, BPTreeNativeCoder.Builder.listCoder(), BPTreeNativeCoder.Builder.listCoder());
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public LNALContainerEncodedTree<K, V> encode(BPlusTree<K, V> tree) {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        if (tree == null || tree.size() <= 0) {
            pool.close();
            return new LNALContainerEncodedTree<>(new EncodedNodeContainerStorage(container, -1L, Collections.singleton(-1L)), this::decodedNode);
        }
        if (tree.getRoot().isLeaf()) {
            long id = container.registerId();
            encodeNode(id, tree.getRoot(), null, null);
            pool.close();
            return new LNALContainerEncodedTree<>(new EncodedNodeContainerStorage(container, id, Collections.singleton(id)), this::decodedNode);
        }

        int numIds = tree.getNumNodes();
        List<Long> ids = LongStream.of(container.registerIds(numIds)).boxed().toList();
        ArrayBlockingQueue<Long> queue = new ArrayBlockingQueue<>(numIds);
        queue.addAll(ids);

        long rootId = ids.get(numIds - 1);

        Iterator<List<BPlusTree.Node<K, V>>> it = tree.bottomUpLevelIterator();
        List<BPlusTree.Node<K, V>> leaves = it.next();

        Container<BPlusTree.Node<K, V>, Future<EncodedNode<IDNASketch.ContainerIdSketch>>> map = new BlockingContainer<>(new Container.MapContainer<>());

        ListIterator<BPlusTree.Node<K, V>> leavesIt = leaves.listIterator(leaves.size());
        BPlusTree.Node<K, V> right = leavesIt.previous();
        BPlusTree.Node<K, V> left = leavesIt.previous();
        BPlusTree.InternalNode<K, V> parent = right.getParent();

        Future<EncodedNode<IDNASketch.ContainerIdSketch>> encodedRightLeaf = pool.submit(() -> encodeNode(FuncUtils.safeCall(queue::poll), right, null, null));
        LinkedList<Future<EncodedNode<IDNASketch.ContainerIdSketch>>> encodedLeaves = new LinkedList<>();
        encodedLeaves.add(encodedRightLeaf);
        Future<EncodedNode<IDNASketch.ContainerIdSketch>> encodedRightParent = null;
        do {
            if (left.getParent() != parent) {
                BPlusTree.InternalNode<K, V> finalParent = parent;
                Future<EncodedNode<IDNASketch.ContainerIdSketch>> finalEncodedRightParent = encodedRightParent;
                LinkedList<Future<EncodedNode<IDNASketch.ContainerIdSketch>>> finalEncodedLeaves = encodedLeaves;
                Future<EncodedNode<IDNASketch.ContainerIdSketch>> encodedParent = pool.submit(
                        () -> encodeNode(
                                FuncUtils.safeCall(queue::poll),
                                finalParent,
                                finalEncodedRightParent != null ? finalEncodedRightParent.get().sketch().id() : null,
                                finalEncodedLeaves.stream().map(f -> FuncUtils.safeCall(f::get)).map(en -> en.sketch().id()).toList()
                        )
                );
                BPlusTree.InternalNode<K, V> finalParent3 = parent;
                pool.submit(() -> map.put(finalParent3, encodedParent));
                encodedLeaves = new LinkedList<>();
                parent = left.getParent();
                encodedRightParent = encodedParent;
            }

            BPlusTree.Node<K, V> finalLeft = left;

            encodedRightLeaf = pool.submit(() -> encodeNode(FuncUtils.safeCall(queue::poll), finalLeft, null, null));
            encodedLeaves.addFirst(encodedRightLeaf);
            left = left.getLeft();
            if (left == null) {
                Future<EncodedNode<IDNASketch.ContainerIdSketch>> finalEncodedRightParent1 = encodedRightParent;
                BPlusTree.InternalNode<K, V> finalParent1 = parent;
                LinkedList<Future<EncodedNode<IDNASketch.ContainerIdSketch>>> finalEncodedLeaves1 = encodedLeaves;
                Future<EncodedNode<IDNASketch.ContainerIdSketch>> encodedParent = pool.submit(() -> encodeNode(FuncUtils.safeCall(queue::poll), finalParent1, finalEncodedRightParent1 != null ? finalEncodedRightParent1.get().sketch().id() : null, finalEncodedLeaves1.stream().map(f -> FuncUtils.safeCall(f::get)).map(en -> en.sketch().id()).toList()));
                pool.submit(() -> map.put(finalParent1, encodedParent));
                break;
            }
        } while (true);


        parent = leaves.get(0).getParent();
        Set<BPlusTree.InternalNode<K, V>> nodeGroups;
        while (parent.getParent() != null) {
            nodeGroups = Stream.iterate(parent, Objects::nonNull, BPlusTree.InternalNode::getRight).map(BPlusTree.Node::getParent).collect(Collectors.toSet());
            nodeGroups.forEach(p -> {
                Future<EncodedNode<IDNASketch.ContainerIdSketch>> encodedParent = pool.submit(
                        () -> encodeNode(
                                FuncUtils.safeCall(queue::poll),
                                p,
                                null,
                                p.getKids().stream().map(map::get).map(f -> FuncUtils.safeCall(f::get)).map(en -> en.sketch().id()).toList()
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
        return new LNALContainerEncodedTree<>(new EncodedNodeContainerStorage(container, rootId, ids), this::decodedNode);
    }

    private EncodedNode<IDNASketch.ContainerIdSketch> encodeNode(long id, BPlusTree.Node<K, V> node, Long rightId, List<Long> kidIds) {
        BaseSequence encoded = new BaseSequence();
        BaseSequence encodedKeys = keyCoder.encode(node.getKeys());
        if (kidIds == null) {
            encoded.append(LEAF_MARKER_BASE);
            DNAPacker.packUnsigned(encoded, encodedKeys.length());
            encoded.append(encodedKeys);
            BaseSequence encodedValues = valueCoder.encode(node.asLeafNode().getValues());
            encoded.append(encodedValues);
            container.put(id, encoded);
            var oligos = container.getOligos(id);
            return new EncodedNode<>(true, Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new), new IDNASketch.ContainerIdSketch(id, container), Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new));
        }
        if (rightId != null) {
            encoded.append(INTERNAL_NODE_HAS_RIGHT_MARKER);
            DNAPacker.packUnsigned(encoded, rightId);
        }
        else {
            encoded.append(INTERNAL_NODE_HAS_NO_RIGHT_MARKER);
        }

        DNAPacker.packUnsigned(encoded, kidIds.size());
        DNAPacker.packUnsigned(encoded, kidIds);
        encoded.append(encodedKeys);
        container.put(id, encoded);
        var oligos = container.getOligos(id);
        return new EncodedNode<>(
                false,
                Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new),
                new IDNASketch.ContainerIdSketch(id, container),
                Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new)
        );
    }

    private DecodedNode<K, IDNASketch.ContainerIdSketch> decodedNode(EncodedNode<IDNASketch.ContainerIdSketch> en) {
        return decodedNode(container.assembleFromOligos(en.joinedOligos()));
    }

    private DecodedNode<K, IDNASketch.ContainerIdSketch> decodedNode(BaseSequence seq) {
        Base marker = seq.get(0);
        seq = seq.window(1);
        int keySize;
        List<K> keyList;
        Long rightId = null;
        DNAPacker.LengthBase lb;
        if (marker == LEAF_MARKER_BASE) {
            lb = DNAPacker.LengthBase.parsePrefix(seq);
            keySize = lb.unpackSingle(seq, false).intValue();
            int totalSize = lb.totalSize();
            int valueIndex = totalSize + keySize;
            keyList = keyCoder.decode(seq.window(totalSize, valueIndex));
            List<V> valueList = valueCoder.decode(seq.window(valueIndex));
            return new DecodedLeafNode<>(keyList, valueList);
        }
        if (marker == INTERNAL_NODE_HAS_RIGHT_MARKER) {
            lb = DNAPacker.LengthBase.parsePrefix(seq);
            rightId = lb.unpackSingle(seq, false).longValue();
            seq = seq.window(lb.totalSize());
        }

        lb = DNAPacker.LengthBase.parsePrefix(seq);
        int kidSize = lb.unpackSingle(seq, false).intValue();
        seq = seq.window(lb.totalSize());
        Pair<BaseSequence, long[]> seqAndKidIds = unpack(seq, kidSize);

        keyList = keyCoder.decode(seqAndKidIds.getT1());
        return new DecodedInternalNodeAboveLeaf<>(keyList, LongStream.of(seqAndKidIds.getT2()).mapToObj(id -> new IDNASketch.ContainerIdSketch(id, container)).toArray(IDNASketch.ContainerIdSketch[]::new), rightId == null ? null : new IDNASketch.ContainerIdSketch(rightId, container));
    }

    public DNAContainer getContainer() {
        return container;
    }

    public DecodedNode<K, IDNASketch.ContainerIdSketch> decodeNodeById(long id) {
       return decodedNode(container.get(id));
    }

    @Override
    public DecodedNode<K, IDNASketch.ContainerIdSketch> decodeNode(EncodedNode<IDNASketch.ContainerIdSketch> en) {
        return decodedNode(container.assembleFromOligos(en.joinedOligos()));
    }

    private Pair<BaseSequence, long[]> unpack(BaseSequence seq, int count) {
        DNAPacker.LengthBase lb;
        int c = 0;
        long[] keys = new long[count];
        while (count-- > 0 && seq.length() > 0) {
            lb = DNAPacker.LengthBase.parsePrefix(seq);
            keys[c++] = lb.unpackSingle(seq, false).intValue();
            seq = seq.window(lb.totalSize());
        }
        return new Pair<>(seq, keys);
    }


    @Override
    public Stream<DecodedNode<K, IDNASketch.ContainerIdSketch>> decode(EncodedBPTree<K, V, Long, IDNASketch.ContainerIdSketch> encodedTree) {
        return encodedTree.stream().map(this::decodeNode);
    }
}
