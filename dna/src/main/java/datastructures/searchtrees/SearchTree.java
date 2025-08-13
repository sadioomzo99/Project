package datastructures.searchtrees;

import java.util.List;

public interface SearchTree<K extends Comparable<K>> {
    SearchNode<K> getRoot();
    void insert(K key);
    SearchNode<K> find(K key);

    interface SearchNode<K extends Comparable<K>> {
        K getKey();
        List<? extends SearchNode<K>> getKids();
    }
}
