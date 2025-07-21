package datastructures.searchtrees;

import datastructures.KVEntry;
import utils.FuncUtils;
import utils.Streamable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BPlusTree<K extends Comparable<K>, V> implements BPTreeQuery<K, V>, Streamable<BPlusTree.Node<K, V>> {
    private Node<K, V> root;
    private int size;
    private int height;
    private int numNodes;

    private final int minNodeKids;
    private final int minLeafEntries;
    private final int maxNodeKids;
    private final int maxLeafEntries;

    public BPlusTree(int b, int c) {
        this(b, 2 * b - 1, c, 2 * c - 1);
    }

    public Node<K, V> getRoot() {
        return root;
    }

    private BPlusTree(int minNodeKids, int maxNodeKids, int minLeafEntries, int maxLeafEntries) {
        if (minLeafEntries < 1 || minNodeKids < 2)
            throw new RuntimeException("invalid parameters");
        this.minNodeKids = minNodeKids;
        this.minLeafEntries = minLeafEntries;
        this.maxNodeKids = maxNodeKids;
        this.maxLeafEntries = maxLeafEntries;
        this.height = 0;
        this.numNodes = 0;
    }

    public void insert(K key, V value) {
        Leaf<K, V> leaf = findLeaf(key);
        if (leaf == null) {
            this.root = newLeaf().add(key, value);
            height++;
            numNodes++;
        }
        else {
            leaf.add(key, value);
            handleOverflowBotUp(leaf);
        }
        size++;
    }

    private Leaf<K, V> newLeaf() {
        return new Leaf<>(this);
    }

    private Leaf<K, V> newLeaf(InternalNode<K, V> parent, Node<K, V> left, Node<K, V> right, List<K> keys, List<V> values) {
        return new Leaf<>(this, parent, left, right, keys, values);
    }

    private InternalNode<K, V> newInternalNode(InternalNode<K, V> parent, Node<K, V> left, Node<K, V> right, List<K> keys, List<Node<K, V>> kids) {
        return new InternalNode<>(this, parent, left, right, keys, kids);
    }

    public static <K extends Comparable<K>, V> BPlusTree<K, V> bulkLoad(Stream<KVEntry<K, V>> sortedStream, int b, int c) {
        BPlusTree<K, V> tree = new BPlusTree<>(b, c);
        AtomicInteger size = new AtomicInteger(0);
        List<? extends Node<K, V>> nodes = FuncUtils.chunkConservative(sortedStream, tree.maxLeafEntries)
                .peek(ch -> size.addAndGet(ch.size()))
                .map(ch -> tree.newLeaf(null, null, null,
                        ch.stream().map(KVEntry::key).collect(Collectors.toCollection(ArrayList::new)),
                        ch.stream().map(KVEntry::value).collect(Collectors.toCollection(ArrayList::new))))
                .toList();

        connectNodes(nodes);
        int height = nodes.size() > 0 ? 1 : 0;
        int numNodes = nodes.size();
        Set<Node<K, V>> potentialOverflows = new HashSet<>();
        potentialOverflows.add(nodes.get(nodes.size() - 1));

        while (nodes.size() > 1) {
            nodes = FuncUtils.chunkConservative(nodes.stream(), tree.maxNodeKids)
                    .map(ch -> {
                        InternalNode<K, V> parent = tree.newInternalNode(null, null, null, new ArrayList<>(tree.maxNodeKids - 1), new ArrayList<>(tree.maxNodeKids));
                        addToParent(ch, parent);
                        return parent;
                    }).collect(Collectors.toCollection(ArrayList::new));

            potentialOverflows.add(nodes.get(nodes.size() - 1));
            height++;
            numNodes += nodes.size();
            connectNodes(nodes);
        }

        height += handleBulkLoadingOverflow(tree, potentialOverflows);
        if (tree.root == null)
            tree.root = nodes.get(0);
        tree.size = size.get();
        tree.height = height;
        tree.numNodes += numNodes;
        return tree;
    }

    private static <K extends Comparable<K>, V> void connectNodes(List<? extends Node<K, V>> nodes) {
        Node<K, V> iNode = nodes.get(0);
        Node<K, V> jNode;
        for (int j = 1; j < nodes.size(); j++) {
            jNode = nodes.get(j);
            iNode.right = jNode;
            jNode.left = iNode;
            iNode = jNode;
        }
    }

    private static <K extends Comparable<K>, V> void addToParent(List<? extends Node<K, V>> kidsNodes, InternalNode<K, V> parent) {
        int lastKidIndex = kidsNodes.size() - 1;
        Node<K, V> left = kidsNodes.get(0);
        parent.addKid(left);
        left.parent = parent;
        Node<K, V> right;
        parent.keys.add(left.keys.get(left.keys.size() - 1));
        for (int j = 1; j < lastKidIndex; j++) {
            right = kidsNodes.get(j);
            left.right = right;
            right.left = left;
            right.parent = parent;
            left = right;
            parent.addKid(right);
            parent.keys.add(right.keys.get(right.keys.size() - 1));
        }
        if (lastKidIndex != 0) {
            right = kidsNodes.get(lastKidIndex);
            left.right = right;
            right.left = left;
            right.parent = parent;
            parent.addKid(right);
        }
    }

    private static <K extends Comparable<K>, V> int handleBulkLoadingOverflow(BPlusTree<K, V> tree, Set<Node<K, V>> nodes) {
        int h = 0;
        Set<Node<K, V>> finished = new HashSet<>();
        int nodesSize = nodes.size();
        for (Node<K, V> n : nodes) {
            if (finished.size() >= nodesSize)
                return h;
            if (finished.contains(n))
                continue;
            while (n.isOverflown()) {
                finished.add(n);
                n = n.split();
                if (n == tree.root)
                    h++;
            }
        }
        return h;
    }


    public int getMinNodeKids() {
        return minNodeKids;
    }
    public int getMinLeafEntries() {
        return minLeafEntries;
    }
    public int getMaxNodeKids() {
        return maxNodeKids;
    }
    public int getMaxLeafEntries() {
        return maxLeafEntries;
    }

    private void handleOverflowBotUp(Node<K, V> n) {
        while (n.isOverflown())
            n = n.split();
    }

    private Leaf<K, V> findLeaf(K key) {
        if (root == null)
            return null;

        Node<K, V> n = root;
        while(!n.isLeaf())
            n = n.asInternalNode().findKid(key);

        return n.asLeafNode();
    }

    @Override
    public V search(K key) {
        Leaf<K, V> leaf = findLeaf(key);
        return leaf != null ? leaf.search(key) : null;
    }

    @Override
    public Stream<V> search(K low, K high) {
        Iterator<Stream<V>> it = new Iterator<>() {
            Node<K, V> n = findLeaf(low);
            boolean hasNext = n != null;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Stream<V> next() {
                if (!hasNext)
                    throw new NoSuchElementException("iterator exhausted");

                Leaf<K, V> leaf = n.asLeafNode();
                var result = leaf.search(low, high);
                if (result.isEmpty())
                    hasNext = false;
                else {
                    n = n.right;
                    hasNext = n != null;
                }
                return result.stream();
            }
        };

        return FuncUtils.stream(() -> it).flatMap(__ -> __);
    }

    public void printLevelOrder() {
        printLevelLeftToRight(root);
        Node<K, V> n = root;
        while (!n.isLeaf()) {
            n = n.asInternalNode().kids.get(0);
            printLevelLeftToRight(n);
        }
    }

    public Iterator<List<Node<K, V>>> bottomUpLevelIterator() {
        if (root == null)
            return Collections.emptyIterator();

        Node<K, V> n = root;
        while (!n.isLeaf())
            n = n.asInternalNode().kids.get(0);

        final Node<K, V> fn = n;
        return new Iterator<>() {
            Node<K, V> node = fn;
            Node<K, V> n0;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public List<Node<K, V>> next() {
                List<Node<K, V>> levelNodes = new ArrayList<>();
                n0 = node;
                while (node != null) {
                    levelNodes.add(node);
                    node = node.right;
                }
                node = n0.parent;
                return levelNodes;
            }
        };
    }

    @Override
    public Iterator<Node<K, V>> iterator() {
        return new Iterator<>() {
            Node<K, V> n = root;
            Node<K, V> n0 = n;
            @Override
            public boolean hasNext() {
                return n != null || !n0.isLeaf();
            }

            @Override
            public Node<K, V> next() {
                if (n == null && !n0.isLeaf()) {
                    n0 = n0.asInternalNode().kids.get(0);
                    n = n0;
                }
                Node<K, V> r = n;
                n = n.right;
                return r;
            }
        };
    }

    private void printLevelLeftToRight(Node<K, V> n) {
        StringJoiner joiner = new StringJoiner(" ");
        while(n != null) {
            joiner.add(n.toString());
            n = n.right;
        }
        System.out.println(joiner);
    }

    public int size() {
        return size;
    }

    public int getHeight() {
        return height;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public static abstract class Node<K extends Comparable<K>, V> {
        protected BPlusTree<K, V> tree;
        protected InternalNode<K, V> parent;
        protected Node<K, V> left;
        protected Node<K, V> right;
        protected List<K> keys;

        protected Node(BPlusTree<K, V> tree, InternalNode<K, V> parent, Node<K, V> left, Node<K, V> right, List<K> keys) {
            this.tree = tree;
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.keys = keys;
        }
        protected abstract boolean isOverflown();
        protected abstract boolean isFull();
        protected abstract boolean isUnderflown();
        protected abstract InternalNode<K, V> split();
        public abstract boolean isLeaf();

        public int size() {
            return keys.size();
        }
        public final InternalNode<K, V> asInternalNode() {
            return (InternalNode<K, V>) this;
        }

        public final Leaf<K, V> asLeafNode() {
            return (Leaf<K, V>) this;
        }

        public boolean isAboveLeaf() {
            return !isLeaf() && asInternalNode().getKids().get(0).isLeaf();
        }

        protected InternalNode<K, V> adjustParent(int midIndex, Node<K, V> leftNode, Node<K, V> rightNode) {
            int index = parent.kids.indexOf(this);
            parent.keys.add(index, keys.get(midIndex));
            parent.kids.set(index, leftNode);
            parent.kids.add(index + 1, rightNode);
            return parent;
        }

        public InternalNode<K, V> getParent() {
            return parent;
        }
        public Node<K, V> getLeft() {
            return left;
        }
        public Node<K, V> getRight() {
            return right;
        }

        public List<K> getKeys() {
            return keys;
        }

        @Override
        public String toString() {
            return Objects.toString(keys);
        }

        public Iterator<Node<K, V>> leftRightIterator() {
            return new Iterator<>() {
                Node<K, V> current = Node.this;
                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public Node<K, V> next() {
                    var c = current;
                    current = current.right;
                    return c;
                }
            };
        }

        public Iterator<Node<K, V>> rightLeftIterator() {
            return new Iterator<>() {
                Node<K, V> current = Node.this;
                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public Node<K, V> next() {
                    var c = current;
                    current = current.left;
                    return c;
                }
            };
        }
    }

    public static class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
        protected List<Node<K, V>> kids;

        protected InternalNode(BPlusTree<K, V> tree, InternalNode<K, V> parent, Node<K, V> left, Node<K, V> right, List<K> keys, List<Node<K, V>> kids) {
            super(tree, parent, left, right, keys);
            this.kids = kids;
        }

        @Override
        protected boolean isOverflown() {
            return kids.size() > tree.maxNodeKids;
        }

        @Override
        protected boolean isFull() {
            return kids.size() == tree.maxNodeKids;
        }

        @Override
        protected boolean isUnderflown() {
            return kids.size() < tree.minNodeKids;
        }

        public Node<K, V> findKid(K key) {
            int index = Collections.binarySearch(keys, key);
            if (index < 0)
                return kids.get(-index - 1);

            return kids.get(index);
        }

        protected void addKid(Node<K, V> kid) {
            this.kids.add(kid);
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        protected InternalNode<K, V> split() {
            tree.numNodes++;
            int keySize = keys.size();
            int midIndex = keySize >>> 1;
            if ((keySize & 1) != 1)
                midIndex--;

            var leftKids = new ArrayList<>(kids.subList(0, midIndex + 1));
            var rightKids = new ArrayList<>(kids.subList(midIndex + 1, kids.size()));
            var leftNode = tree.newInternalNode(parent, left, null, new ArrayList<>(keys.subList(0, midIndex)), leftKids);
            var rightNode = tree.newInternalNode(parent, null, right, new ArrayList<>(keys.subList(midIndex + 1, keySize)), rightKids);
            leftNode.right = rightNode;
            rightNode.left = leftNode;
            if (left != null)
                left.right = leftNode;
            if (right != null)
                right.left = rightNode;

            leftKids.forEach(kid -> kid.parent = leftNode);
            rightKids.forEach(kid -> kid.parent = rightNode);

            if (parent == null) { // root split
                var _root = tree.newInternalNode(null, null, null, new ArrayList<>(Collections.singletonList(keys.get(midIndex))), new ArrayList<>(Arrays.asList(leftNode, rightNode)));
                leftNode.parent = _root;
                rightNode.parent = _root;
                tree.root = _root;
                tree.height++;
                tree.numNodes++;
                return _root;
            }

            return adjustParent(midIndex, leftNode, rightNode);
        }

        @Override
        public InternalNode<K, V> getLeft() {
            return left != null? left.asInternalNode() : null;
        }
        @Override
        public InternalNode<K, V> getRight() {
            return right != null? right.asInternalNode() : null;
        }
        public List<Node<K, V>> getKids() {
            return Collections.unmodifiableList(kids);
        }
    }

    public static class Leaf<K extends Comparable<K>, V> extends Node<K, V> {
        protected List<V> values;
        protected Leaf(BPlusTree<K, V> tree, InternalNode<K, V> parent, Node<K, V> left, Node<K, V> right, List<K> keys, List<V> values) {
            super(tree, parent, left, right, keys);
            this.values = values;
        }

        protected Leaf(BPlusTree<K, V> tree) {
            this(tree, null, null, null, new ArrayList<>(tree.maxLeafEntries), new ArrayList<>(tree.maxLeafEntries));
        }

        @Override
        protected boolean isOverflown() {
            return values.size() > tree.maxLeafEntries;
        }

        @Override
        protected boolean isFull() {
            return values.size() == tree.maxLeafEntries;
        }

        @Override
        protected boolean isUnderflown() {
            return values.size() < tree.minLeafEntries;
        }

        protected Leaf<K, V> add(K key, V value) {
            int index = -Collections.binarySearch(keys, key) - 1;
            if (index < 0)
                throw new RuntimeException("duplicate keys exception: " + key);

            this.keys.add(index, key);
            this.values.add(index, value);
            return this;
        }

        public V search(K key) {
            int index = Collections.binarySearch(keys, key);
            if (index < 0)
                return null;

            return values.get(index);
        }

        public List<V> search(K keyLow, K keyHigh) {
            int size = size();
            int indexL = Collections.binarySearch(keys, keyLow);
            if (indexL < 0)
                indexL = -indexL - 1;
            if (indexL >= size)
                return Collections.emptyList();

            int indexR = Collections.binarySearch(keys.subList(indexL, size), keyHigh);

            if (indexR == -1)
                return Collections.emptyList();

            if (indexR < 0)
                indexR = -indexR - 1 + indexL;
            else
                indexR += indexL + 1;

            return values.subList(indexL, indexR);
        }

        @Override
        public Leaf<K, V> getLeft() {
            return left == null ? null : left.asLeafNode();
        }

        @Override
        public Leaf<K, V> getRight() {
            return right == null ? null : right.asLeafNode();
        }

        @Override
        protected InternalNode<K, V> split() {
            tree.numNodes++;
            int keySize = keys.size();
            int midIndex = keySize >>> 1;
            var leftLeaf = tree.newLeaf(parent, left, null, new ArrayList<>(keys.subList(0, midIndex)), new ArrayList<>(values.subList(0, midIndex)));
            var rightLeaf = tree.newLeaf(parent, null, right, new ArrayList<>(keys.subList(midIndex, keySize)), new ArrayList<>(values.subList(midIndex, values.size())));
            leftLeaf.right = rightLeaf;
            rightLeaf.left = leftLeaf;
            if (left != null)
                left.right = leftLeaf;
            if (right != null)
                right.left = rightLeaf;

            if (parent == null) { // root is Leaf
                var _root = tree.newInternalNode(null, null, null, new ArrayList<>(leftLeaf.keys.subList(midIndex - 1, midIndex)), new ArrayList<>(Arrays.asList(leftLeaf, rightLeaf)));
                leftLeaf.parent = _root;
                rightLeaf.parent = _root;
                tree.root = _root;
                tree.height++;
                tree.numNodes++;
                return _root;
            }

            return adjustParent(midIndex - 1, leftLeaf, rightLeaf);
        }

        @Override
        public boolean isLeaf() {
            return true;
        }
        public List<V> getValues() {
            return Collections.unmodifiableList(values);
        }
    }
}
