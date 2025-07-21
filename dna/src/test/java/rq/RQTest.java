package rq;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.DNARule;
import core.dnarules.SuperBasicDNARules;
import dnacoders.DistanceCoder;
import dnacoders.P2SegmentationCoder;
import utils.Coder;
import utils.DNAPacker;
import utils.FuncUtils;
import utils.compression.GZIP;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import utils.rq.RQCoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class RQTest {
    public static void main(String[] args) {
        DoubleStream.Builder gc = DoubleStream.builder();
        DoubleStream.Builder err = DoubleStream.builder();
        final float maxSuperBasicError = 0.2f;
        DNARule superBasicDNARules = SuperBasicDNARules.INSTANCE;
        DNARule basicDNARules = BasicDNARules.INSTANCE;
        RQCoder rq = new RQCoder(seq -> superBasicDNARules.evalErrorProbability(seq) <= maxSuperBasicError,
                seq -> superBasicDNARules.evalErrorProbability(seq) <= maxSuperBasicError
        );
        Coder<byte[], BaseSequence> rqCoder1 = Coder.fuse(new GZIP(), rq);
        Coder<byte[], BaseSequence> rqCoder2 = rq;

        Function<Coder<String, BaseSequence>, Coder<byte[], BaseSequence>> rCoder = stringCoder -> Coder.fuse(
                new Coder<byte[], String>() {
                    @Override
                    public String encode(byte[] bytes) {
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                    @Override
                    public byte[] decode(String s) {
                        return s.getBytes(StandardCharsets.UTF_8);
                    }
                },
                stringCoder
        );
        String dorn = FuncUtils.safeCall(() -> Files.readString(Path.of("D:/Dorn.txt")));

        var coder = rqCoder1;
        //var coder = rCoder.apply(RotatingQuattro.INSTANCE);
        BaseSequence encoded = coder.encode(dorn.getBytes(StandardCharsets.UTF_8));
        System.out.println("raw rq error: " + Arrays.stream(encoded.splitEvery(250)).mapToDouble(basicDNARules::evalErrorProbability).summaryStatistics());
        DistanceCoder distanceCoder = new DistanceCoder(
                16,
                MinHashLSH.newSeqAmpLSHTraditional(6, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR),
                basicDNARules
        );
        Coder<BaseSequence, BaseSequence[]> mySegmentationCoder = Coder.fuse(
                new P2SegmentationCoder(250 - DNAPacker.pack(16).length(), 10),
                Coder.arrayMapper(
                        distanceCoder,
                        BaseSequence[]::new,
                        BaseSequence[]::new
                )
        );

        var partitions = mySegmentationCoder.encode(encoded);

        System.out.println("encoded(len=" + encoded.length() + "): " + encoded);
        System.out.println("error: " + basicDNARules.evalErrorProbability(encoded));

        System.out.println();
        for (int i = 0; i < partitions.length; i++) {
            float error = basicDNARules.evalErrorProbability(partitions[i]);
            float gcContent = partitions[i].gcContent();

            err.add(error);
            gc.add(gcContent);
            System.out.println("partition_" + (i + 1) + "_(len=" + partitions[i].length() + "): " + partitions[i]);
            System.out.println("partition_" + (i + 1) + "_error: " + error + ", GC: " + gcContent);
        }
        System.out.println();
        System.out.println("is consistent? " + new String(coder.decode(mySegmentationCoder.decode(partitions)), StandardCharsets.UTF_8).equals(dorn));
        System.out.println("error: " + err.build().summaryStatistics());
        System.out.println("gc   : " + gc.build().summaryStatistics());
    }
}
