package core.dnarules;

import core.Base;
import core.BaseSequence;

import java.util.HashMap;
import java.util.Map;

import static dnacoders.headercoders.SSACoder.reverseComplement;

public class DNASecondaryStructureIndicator implements DNARule {
    int m;
    int k;

    public DNASecondaryStructureIndicator(int m, int k){
        this.m = m;
        this.k = k;
    }

    // Methode zur Zählung invers komplementärer Regionen
    public static int countSeparatedReverseComplements(BaseSequence dna, int m) {
        if (checkCond(dna)) return 0;

        int count = 0;
        Map<BaseSequence, Integer> reverseComplementMap = new HashMap<>();

        // Alle möglichen Teilsequenzen durchsuchen
        for (int len = 2; len <= dna.length(); len++) { // Mindestens 2 Basen lang
            for (int start = 0; start <= dna.length() - len; start++) {
                BaseSequence subSequence = dna.subSequence(start, start + len);
                BaseSequence reverseComplement = reverseComplement(subSequence);

                // Suche das Reverse-Komplement an einer anderen Stelle
                int firstIndex = start;
                int secondIndex = findNearestNonOverlappingIndex(dna.toString(), reverseComplement.toString(), firstIndex+len);


                while (secondIndex != -1) {
                    if (Math.abs(firstIndex - secondIndex) >= len) { // Sicherstellen, dass sie getrennt sind
                        reverseComplementMap.put(subSequence, reverseComplementMap.getOrDefault(subSequence, 0) + 1);
                    }
                    secondIndex = findNearestNonOverlappingIndex(dna.toString(), reverseComplement.toString(), secondIndex + len);
                }

            }
        }

        for (Map.Entry<BaseSequence, Integer> entry : reverseComplementMap.entrySet()) {
            if (entry.getKey().length() >= m) {
                count += entry.getValue();
            }
        }

        return count;
    }

    private static boolean checkCond(BaseSequence dna) {
        if (dna == null || dna.length() < 2) {
            return true;
        }
        return false;
    }

    public static int countPossibleComplements(BaseSequence dna) {
        if (checkCond(dna)) return 0;

        int count = 0;
        // Alle möglichen Teilsequenzen durchsuchen
        for (int len = 2; len <= dna.length(); len++) { // Mindestens 2 Basen lang
            for (int start = 0; start <= dna.length() - len; start++) {
                BaseSequence subSequence = dna.subSequence(start, start + len);
                count++;
            }
        }
        return count;
    }

    private static int findNearestNonOverlappingIndex(String dna, String target, int minIndex) {
        int index = dna.indexOf(target, minIndex);
        return index;
    }

    /**
     * Methode zur Berechnung des Strukturindikators
     * @param dnaSeq given dna sequence
     * @param m stem
     * @param k loop
     * @return structure indicator
     */

    public static double structureIndicator(BaseSequence dnaSeq, int m, int k) {
        if(m < 2*Math.log(dnaSeq.length()) + 2 || k <4){
            throw new RuntimeException("m should be bigger than  2*Math.log(dnaSequence.length() + 2 or k has to be bigger than 4");
        }

        int n = dnaSeq.length();
        int gcCount = dnaSeq.gcCount();


        // Anzahl invers komplementärer Regionen berechnen
        int actualRC = countSeparatedReverseComplements(dnaSeq,m);
        int possibleRC = countPossibleComplements(dnaSeq);

        double gcContent = (double) gcCount / n ;
        double normalizedM = (double)  (actualRC) / possibleRC;
        double normalizedParams = (double) m / (k +m);   // [0, 1]

        // Final indicator (product of normalized terms)
        return gcContent * normalizedM * normalizedParams ;
    }

    /**
     * gives secondary structure indicator
     * with values closer to 0 meaning less risk of secondary structure formation
     * @param seq the DNA sequence.
     * @return error value
     */
    @Override
    public float evalErrorProbability(BaseSequence seq) {
        return (float) structureIndicator(seq, m ,k);
    }


}




