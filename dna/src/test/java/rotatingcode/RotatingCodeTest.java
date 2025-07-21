package rotatingcode;

import core.BaseSequence;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.dnaconvertors.RotatingTre;

public class RotatingCodeTest {
    public static void main(String[] args) {
        RotatingQuattro rCode1 = RotatingQuattro.INSTANCE;
        RotatingTre rCode2 = RotatingTre.INSTANCE;
        BaseSequence seq1 = rCode1.encode("Hello World!");
        BaseSequence seq2 = rCode2.encode("Hello World!");
        String s1 = rCode1.decode(seq1);
        String s2 = rCode2.decode(seq2);
        System.out.println("seq1=" + seq1);
        System.out.println("seq2=" + seq2);
        System.out.println("s1=" + s1);
        System.out.println("s2=" + s2);
        System.out.println("s1.equals(s2)=" + s1.equals(s2));
    }
}
