package datastructures.container.utils;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.searchtrees.AbstractSearchTree;
import datastructures.searchtrees.BinarySearchTree;
import utils.Coder;
import utils.DNAPacker;
import utils.PooledCompletionService;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CBinSearchTree {
    public static <K extends Comparable<K>> long putBinaryTree(DNAContainer container, BinarySearchTree<K> binTree, Coder<K, BaseSequence> keyMapper) {
        var map = binTree.stream().parallel().collect(Collectors.toConcurrentMap(AbstractSearchTree.AbstractNode::getKey, __ -> container.registerId()));
        var root = binTree.getRoot();
        long rootId = putBinaryTreeNode(container, root, keyMapper, map);
        var service = new PooledCompletionService<>(Executors.newWorkStealingPool());
        binTree.stream().filter(node -> node != root).forEach(node -> service.submit(() -> putBinaryTreeNode(container, node, keyMapper, map)));
        service.shutdown();
        service.waitFinish();
        return rootId;
    }

    private static <K extends Comparable<K>> long putBinaryTreeNode(DNAContainer container, BinarySearchTree.BinarySearchTreeNode<K> node, Coder<K, BaseSequence> keyMapper, Map<K, Long> map) {
        var encodedKey = keyMapper.encode(node.getKey());
        var encodedKeyLength = DNAPacker.pack(encodedKey.length());
        long leftId = -1L;
        long rightId = -1L;
        BaseSequence encodedLeftRightIds = new BaseSequence();
        var leftNode = node.getLeftNode();
        var rightNode = node.getRightNode();
        if (leftNode != null)
            leftId = map.get(leftNode.getKey());
        if (rightNode != null)
            rightId = map.get(rightNode.getKey());

        encodedLeftRightIds.append(DNAPacker.pack(leftId, rightId));

        long id = map.get(node.getKey());
        container.put(id, BaseSequence.join(encodedKeyLength, encodedKey, encodedLeftRightIds));
        return id;
    }

    public static <K extends Comparable<K>> Stream<K> decodeKeyStream(DNAContainer container, long treeId, Coder<K, BaseSequence> keyMapper) {
        var node = decodeBinaryTreeNode(container, treeId, keyMapper);
        if (node == null)
            return Stream.empty();

        return Stream.concat(Stream.of(node.key), Stream.concat(decodeKeyStream(container, node.leftId, keyMapper), decodeKeyStream(container, node.rightId, keyMapper)));
    }

    public static <K extends Comparable<K>> BinarySearchTree<K> getBinSearchTree(DNAContainer container, long rootId, Coder<K, BaseSequence> keyMapper) {
        return BinarySearchTree.of(decode(container, decodeBinaryTreeNode(container, rootId, keyMapper), null, keyMapper));
    }

    private static <K extends Comparable<K>> BinarySearchTree.BinarySearchTreeNode<K> decode(DNAContainer container, DecodedDNABinaryTreeNode<K> n, BinarySearchTree.BinarySearchTreeNode<K> parent, Coder<K, BaseSequence> keyMapper) {
        if (n == null)
            return null;

        var node = new BinarySearchTree.BinarySearchTreeNode<>(parent, n.key);
        node.setLeftNode(decode(container, decodeBinaryTreeNode(container, n.leftId, keyMapper), node, keyMapper));
        node.setRightNode(decode(container, decodeBinaryTreeNode(container, n.rightId, keyMapper), node, keyMapper));
        return node;
    }

    public static <K extends Comparable<K>> DecodedDNABinaryTreeNode<K> findBinaryTreeKey(DNAContainer container, long treeId, K key, Coder<K, BaseSequence> keyMapper) {
        var node = decodeBinaryTreeNode(container, treeId, keyMapper);
        int compared;
        while (node != null) {
            compared = node.key.compareTo(key);
            if (compared == 0)
                return node;
            if (compared < 0)
                node = decodeBinaryTreeNode(container, node.rightId, keyMapper);
            else
                node = decodeBinaryTreeNode(container, node.leftId, keyMapper);
        }

        return null;
    }

    public static <K extends Comparable<K>> DecodedDNABinaryTreeNode<K> decodeBinaryTreeNode(DNAContainer container, long nodeId, Coder<K, BaseSequence> keyMapper) {
        var nodeSeq = container.get(nodeId);
        if (nodeSeq == null)
            return null;
        var lb = DNAPacker.LengthBase.parsePrefix(nodeSeq);
        var keyLength = lb.unpackSingle(nodeSeq, false).intValue();
        var offset = lb.totalSize();
        var endEncodedKey = offset + keyLength;
        var leftIdSeq = nodeSeq.window(endEncodedKey);
        var lbLeft = DNAPacker.LengthBase.parsePrefix(leftIdSeq);
        var lbRightSeq = nodeSeq.window(endEncodedKey + lbLeft.totalSize());
        var lbRight = DNAPacker.LengthBase.parsePrefix(lbRightSeq);

        return new DecodedDNABinaryTreeNode<>(
                keyMapper.decode(nodeSeq.window(offset, endEncodedKey)),
                lbLeft.unpackSingle(leftIdSeq, false).longValue(),
                lbRight.unpackSingle(lbRightSeq, false).longValue()
        );
    }

    public record DecodedDNABinaryTreeNode<K extends Comparable<K>>(K key, long leftId, long rightId) {
    }
}
