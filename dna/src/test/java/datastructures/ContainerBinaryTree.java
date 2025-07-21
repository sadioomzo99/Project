package datastructures;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import datastructures.container.utils.CBinSearchTree;
import datastructures.searchtrees.BinarySearchTree;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.Coder;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;

public class ContainerBinaryTree {

    public static void main(String[] args) {

        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(80)
                .build();

        DNAContainer container = SizedDNAContainer.builder().setAddressManager(atm).setPayloadSize(150).build();

        BinarySearchTree<Integer> binTree = new BinarySearchTree<>();
        binTree.insert(40);
        binTree.insert(0);
        binTree.insert(7);

        Coder<Integer, BaseSequence> keyCoder = Coder.fuse(
                Coder.of(Object::toString, Integer::parseInt),
                RotatingQuattro.INSTANCE
        );
        var treeId = CBinSearchTree.putBinaryTree(container, binTree, keyCoder);
        System.out.println(CBinSearchTree.findBinaryTreeKey(container, treeId, 7, keyCoder));
        CBinSearchTree.getBinSearchTree(container, treeId, keyCoder).stream().forEach(System.out::println);
    }
}
