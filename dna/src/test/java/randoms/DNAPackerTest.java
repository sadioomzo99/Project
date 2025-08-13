package randoms;

import utils.DNAPacker;

public class DNAPackerTest {

    public static void main(String[] args) {
        byte n = -1;
        System.out.println(DNAPacker.pack(n));
        System.out.println(DNAPacker.unpack(DNAPacker.pack(n)));
        System.out.println(DNAPacker.LengthBase.minimize(n));
        var x = DNAPacker.LengthBase.minimize(n);
        System.out.println(DNAPacker.pack(x));
        System.out.println(DNAPacker.unpack(DNAPacker.pack(x)));
    }
}
