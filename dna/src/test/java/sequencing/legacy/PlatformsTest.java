package sequencing.legacy;

import utils.csv.BufferedCsvWriter;
import utils.sequencing.SequencingPlatform;
import utils.sequencing.SequencingResult;
import utils.sequencing.SequencingRunResult;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PlatformsTest {



    public static void main(String[] args) {


        List<SequencingPlatform> NGS = Arrays.asList(
                SequencingPlatform.ILLUMINA_ISEQ_100,
                SequencingPlatform.ILLUMINA_MINI_SEQ,
                SequencingPlatform.ILLUMINA_NEXT_SEQ_550,
                SequencingPlatform.ILLUMINA_NEXT_SEQ_1000,
                SequencingPlatform.ILLUMINA_NEXT_SEQ_2000,
                SequencingPlatform.ILLUMINA_NOVA_SEQ_6000

        );

        List<SequencingPlatform> SANGER = List.of(
                SequencingPlatform.SANGER
        );

        List<SequencingPlatform> ONT = List.of(
                SequencingPlatform.ONT_FONGLE,
                SequencingPlatform.ONT_MINION,
                SequencingPlatform.ONT_GRID_ION
        );


        BufferedCsvWriter writer = new BufferedCsvWriter("sequencing_analysis.csv", false);
        if (writer.isEmpty())
            writer.appendNewLine(
                "total cycles",
                "length",
                "count",
                "coverage",
                "Generation",
                "cost in $",
                "time in hours"
        );

        for (long numNucleotides : List.of(1000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L, 10_000_000_000L)) {
            int sangerLength = 1000;
            long sangerNumSequences = numNucleotides / sangerLength;

            int ngsLength = 150;
            long ngsNumSequences = numNucleotides / ngsLength;

            int ontLength = 1000;
            long ontNumSequences = numNucleotides / ontLength;

            SequencingResult sanger = sequence(SANGER, sangerNumSequences, sangerLength, 1);
            SequencingResult ngs = sequence(NGS, ngsNumSequences, ngsLength, 1);
            SequencingResult ont = sequence(ONT, ontNumSequences, ontLength, 1);

            System.out.println("cycles: " + numNucleotides / 1000_000_000d + "Gb");
            System.out.println("Sanger: ");
            System.out.println(sanger);
            System.out.println("NGS: ");
            System.out.println(ngs);
            System.out.println("ONT: ");
            System.out.println(ont);

            System.out.println("\n");


            /*
            write(writer, sanger, sangerNumSequences, sangerLength, 1);
            write(writer, ngs, ngsNumSequences, ngsLength, 1);
            write(writer, ont, ontNumSequences, ontLength, 1);

             */
        }

        writer.close();

    }

    public static void write(BufferedCsvWriter writer, SequencingResult sequencingResult, long numSequences, int length, int coverage) {
        if (sequencingResult == null)
            return;

        writer.appendNewLine(asRow(sequencingResult, length, numSequences, coverage));
    }

    public static String[] asRow(SequencingResult sequencingResult, int length, long numSequences, int coverage) {
        return Stream.of(
                numSequences * length * coverage,
                length,
                numSequences,
                coverage,
                sequencingResult.sequencingRunResults().stream().map(s -> s.platform().getGeneration()).findFirst().orElseThrow(),
                sequencingResult.cost(),
                sequencingResult.sequencingRunResults().stream().map(SequencingRunResult::duration).mapToDouble(Duration::toHours).sum()
        ).map(Objects::toString).toArray(String[]::new);
    }

    public static SequencingResult sequence(List<SequencingPlatform> platforms, long count, int length, int coverage) {
        return platforms.stream()
                .filter(platform -> platform.isFeasible(count, length, coverage))
                .map(p -> p.sequence(count, length, coverage))
                .min(Comparator.comparingDouble(SequencingResult::cost))
                .orElse(null);
    }
}
