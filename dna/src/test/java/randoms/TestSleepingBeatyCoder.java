package randoms;

import core.BaseSequence;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.Coder;
import utils.compression.GZIP;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestSleepingBeatyCoder {
    public static void main(String[] args) throws IOException {
        byte[] data = Files.readAllBytes(Path.of("D:/Dorn.txt"));
        Coder<String, BaseSequence> rCode = RotatingQuattro.INSTANCE;
        String text = new String(data);
        BaseSequence seq = rCode.encode(text);
        System.out.println("Org file:");
        System.out.println("org text len=" + text.length());
        System.out.println("rotated len=" + seq.length());

        GZIP c = new GZIP();
        byte[] compressed = c.encode(data);
        String textCompressed = new String(compressed);
        BaseSequence seqCompressed = rCode.encode(textCompressed);
        System.out.println("\nCompressed file:");
        System.out.println("org text len=" + text.length());
        System.out.println("compressed text len=" + textCompressed.length());
        System.out.println("rotated len=" + seqCompressed.length());

    }
}
