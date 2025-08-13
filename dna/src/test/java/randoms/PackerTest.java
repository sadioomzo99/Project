package randoms;

import utils.BitString;
import utils.Packer;

public class PackerTest {

    public static void main(String[] args) {
        BitString bs = new BitString();
        Packer.packMinimal(5L, bs);
        Packer.packString("a".repeat(255), bs);
        Packer.packMinimal(-0.44254f, bs);
        Packer.packString("b".repeat(12), bs);
        Packer.packString("c".repeat(13), bs);
        Packer.packMinimal(5, bs);
        Packer.packMinimal((byte) 5, bs);
        Packer.packMinimal((short) 5, bs);
        Packer.packMinimal(516340545404L, bs);
        Packer.packMinimal(-0.445040434d, bs);
        Packer.packString("5.56846", bs);
        Packer.packString("5.56846", bs);

        Packer.unpackLazy(bs).forEach(System.out::println);
    }
}
