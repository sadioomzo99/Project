package datastructures.searchtrees;

import utils.Streamable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSearchTree<K extends Comparable<K>, N extends SearchTree.SearchNode<K>> implements SearchTree<K> , Streamable<N> {
    protected N root;

    public AbstractSearchTree(N root) {
        this.root = root;
    }

    public AbstractSearchTree() {
        this(null);
    }

    @Override
    public N getRoot() {
        return root;
    }

    public static abstract class AbstractNode<K extends Comparable<K>, N extends SearchNode<K>> implements SearchNode<K> {
        protected N parent;
        protected K key;
        protected List<N> kids;

        public AbstractNode(K key, N parent) {
            this.parent = parent;
            this.key = key;
        }

        public void setKid(int i, N kid) {
            ensureKidsAllocated(i);
            this.kids.set(i, kid);
        }

        @Override
        public List<N> getKids() {
            return kids;
        }

        public N getKid(int i) {
            return (this.kids == null || i >= this.kids.size()) ? null : this.kids.get(i);
        }

        public void setKids(List<N> kids) {
            this.kids = kids;
        }

        protected void ensureKidsAllocated(int i) {
            if (kids == null)
                kids = Stream.generate(() -> (N) null).limit(i + 1).collect(Collectors.toCollection(ArrayList::new));
            else if (kids.size() <= i)
                Stream.generate(() -> (N) null).limit(i - kids.size() + 1).forEach(kids::add);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{"
                    + "key=" + key
                    + ", parent=" + (parent != null? parent.getKey() : "null")
                    + ", kids' keys=" + (kids == null ? "[]" : kids.stream().map(n -> n == null? "null" : n.getKey().toString()).collect(Collectors.joining(", ")))
                    + '}';
        }

        @Override
        public K getKey() {
            return key;
        }
        public N getParentNode() {
            return parent;
        }
        public void setKey(K key) {
            this.key = key;
        }
        public void setParentNode(N parent) {
            this.parent = parent;
        }
    }
}
