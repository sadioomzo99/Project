package datastructures.searchtrees;

import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class BinarySearchTree<K extends Comparable<K>> extends AbstractSearchTree<K, BinarySearchTree.BinarySearchTreeNode<K>> {

    private final AtomicInteger itemsCount;
    public BinarySearchTree() {
        this(null, 0);
    }

    private BinarySearchTree(BinarySearchTreeNode<K> root, int itemsCount) {
        super(root);
        this.itemsCount = new AtomicInteger(itemsCount);
    }

    public static <K extends Comparable<K>> BinarySearchTree<K> of(BinarySearchTree.BinarySearchTreeNode<K> root) {
        return new BinarySearchTree<>(root, -1);
    }

    @Override
    public Iterator<BinarySearchTreeNode<K>> iterator() {
        return new Iterator<>() {
            private BinarySearchTreeNode<K> current = getRoot();
            private final Stack<BinarySearchTreeNode<K>> stack = new Stack<>();

            @Override
            public boolean hasNext() {
                return !stack.isEmpty() || current != null;
            }

            @Override
            public BinarySearchTreeNode<K> next() {
                do {
                    if (current != null) {
                        stack.push(current);
                        current = current.getLeftNode();
                    }
                    else {
                        current = stack.pop();
                        BinarySearchTreeNode<K> toReturn = current;
                        current = current.getRightNode();
                        return toReturn;
                    }
                } while(!stack.isEmpty() || current != null);

                throw new RuntimeException("concurrent modification? Iterator cannot find promised node");
            }
        };
    }

    @Override
    public BinarySearchTreeNode<K> find(K k) {
        if (k == null)
            return null;

        BinarySearchTreeNode<K> binNode = super.root;
        int compared;
        while (binNode != null) {
            compared = binNode.getKey().compareTo(k);
            if (compared == 0)
                return binNode;
            if (compared < 0)
                binNode = binNode.getRightNode();
            else
                binNode = binNode.getLeftNode();
        }

        return null;
    }

    @Override
    public void insert(K k) {
        BinarySearchTreeNode<K> parent = findLockedParentToInsertIntoSafe(k);
        BinarySearchTreeNode<K> binNode = new BinarySearchTreeNode<>(null, null);
        binNode.key = k;
        if (parent == null)
            root = binNode;
        else {
            binNode.parent = parent;
            if (parent.key.compareTo(k) > 0)
                parent.setLeftNode(binNode);
            else
                parent.setRightNode(binNode);

            parent.unlock();
        }
        itemsCount.incrementAndGet();
    }

    public void insertParallel(Stream<K> keys) {
        keys.parallel().forEach(this::insert);
    }


    private BinarySearchTreeNode<K> findLockedParentToInsertIntoSafe(K key) {
        BinarySearchTreeNode<K> n = super.root;
        if (n == null)
            return null;
        BinarySearchTreeNode<K> parent;
        int compared;
        while (true) {
            parent = n;
            compared = n.getKey().compareTo(key);
            parent.lock();
            if (compared > 0)
                n = n.getLeftNode();
            else if (compared < 0)
                n = n.getRightNode();
            else
                throw new RuntimeException("found duplicated key for " + key);

            if (n != null)
                parent.unlock();
            else
                break;
        }

        return parent;
    }

    public static class BinarySearchTreeNode<K extends Comparable<K>> extends AbstractNode<K, BinarySearchTreeNode<K>> {
        private static final int LEFT_KID_INDEX  = 0;
        private static final int RIGHT_KID_INDEX = 1;

        private final ReentrantLock lock;

        public BinarySearchTreeNode(BinarySearchTreeNode<K> parent, K key) {
            super(key, parent);
            this.lock = new ReentrantLock();
        }

        public BinarySearchTreeNode<K> getLeftNode() {
            return getKid(LEFT_KID_INDEX);
        }
        public BinarySearchTreeNode<K> getRightNode() {
            return getKid(RIGHT_KID_INDEX);
        }
        public void setLeftNode(BinarySearchTreeNode<K> kid) {
            setKid(LEFT_KID_INDEX, kid);
        }
        public void setRightNode(BinarySearchTreeNode<K> kid) {
            setKid(RIGHT_KID_INDEX, kid);
        }
        public void lock() {
            this.lock.lock();
        }
        public void unlock() {
            this.lock.unlock();
        }
    }
}
