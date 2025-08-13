package dnacoders.headercoders;

import core.Base;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.RCRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class SSACoderTest {
    private static final char[] BASES = {'A', 'T', 'C', 'G'};
    private static Random random = new Random();

    public static String generateRandomDNA(int length) {
        StringBuilder dna = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            dna.append(BASES[random.nextInt(4)]);
        }
        return dna.toString();
    }
    public static int countHomopolymers(String sequence, int L) {

        Map<Character, Integer> homopolymerCounts = new HashMap<>();
        char[] bases = {'A', 'T', 'G', 'C'};
        int count = 0;
        for (char base : bases) {
            homopolymerCounts.put(base, 0);
        }

        int n = sequence.length();
        for (int i = 0; i <= n - L; i++) {
            String subSeq = sequence.substring(i, i + L);
            char base = subSeq.charAt(0);

            // Check if all characters are the same
            if (subSeq.chars().allMatch(c -> c == base)) {

                homopolymerCounts.put(base, homopolymerCounts.get(base) + 1);
            }
        }
        for (char c: homopolymerCounts.keySet()) {
            count += homopolymerCounts.get(c);
        }
        return count;
    }

    public static String simulateHomopolymerProbability(int N, int L, int trials) throws Exception {

        var ssaRule = new RCRule(20,4);
        BasicDNARules rules = new BasicDNARules();
        rules.addRule(new RCRule(18,4));
        int successCount = 0;

        for (int i = 0; i < trials; i++) {
            String dna = generateRandomDNA(N);
            int count1 = countHomopolymers(dna,L);
            PermutationCoder pCoder = new PermutationCoder(false, 4, ssaRule::evalErrorProbability);

            String dnamk = pCoder.encode(new BaseSequence(dna)).toString();
            int count2 =countHomopolymers(dnamk,L);
            if (count1 < count2 ) {
                successCount++;
            }

        }
        double estimatedProbability= (double) successCount / trials;

        return "Geschätzte Wahrscheinlichkeit, dass es mehr Homopolymer der Länge " + L +
                " in einer DNA-Sequenz der Länge " + N + " nach der Permutation Kodierung generiert wurde : " + estimatedProbability;
    }

    public static void main(String[] args) throws Exception {
        int m = 23; // Minimum length condition m >= 2*log(n) +2
        int k = 4; // Maximum distance condition

        String dnaTest = "ATCGACGAATGTCGA"; // Replace with your DNA sequence
        String seq1 = "TATGGGATTGCTTGGGGGTGCCTATGGCATACAACAGCAGGTAGTGTAGGGACAATGCGGAAAGATCAGTTCCATTAACGCTCCTTCGGACAGCTGAGGCAGCGGGACTCGACGTAACGCGAACCTACTTTGGGACTTGACATAGCTAAAGCTGTCCTTTTGCAGTCGACGGGTGGCGTGCGACTCTCGTCGTAGAAAACGGCCTACACTTTAGTGGCATCGTCAGGCAGGGTGCCGGCGAACTAAGAGAGGGATCTCTCGGAATATGCGTACGTCGCCGCAATTGGGCGGAGCAGAGCATTCGAGCCGTAAAGGCCACATTGACGGTGAGCCATTTGTGTAGGCCATCAGTCCCCGAAGCTCGCCCCCCCCCTGGGAGCTCCCACTCAGTCAGGCACGTTCTGCTTTGAACGGCACCGGATCGAAGTATCGTCCGTGGCCACTTTCACTCGGCGGGATCCTACGTGCGTTCAGCACCGAATTCTTTCTGCTCAGCTGGTGGGGAGCCCAGAGAGAAAAGAGAGGAGTTTGCAGATAAGATTTGGGCTCCCCACCAGCTGAGCAGAAAGAATTCGGTGCTGAACGCACGTAGGATCCCGCCGAGTGAAAGTGGCCACGGACGATACTTCGATCCGGTGCCGTTCAAAGCAGAACGTGCCTGACTGAGTGGGAGCTCCCAGGGGGGGGGCGAGCTTCGGGGACTGATGGCCTACACAAATGGCTCACCGTCAATGTGGCCTTTACGGCTCGAATGCTCTGCTCCGCCCAATTGCGGCGACGTACGCATATTCCGAGAGATCCCTCTCTTAGTTCGCCGGCACCCTGCCTGACGATGCCACTAAAGTGTAGGCCGTTTTCTACGACGAGAGTCGCACGCCACCCGTCGACTGCAAAAGGACAGCTTTAGCTATGTCAAGTCCCAAAGTAGGTTCGCGTTACGTCGAGTCCCGCTGCCTCAGCTGTCCGAAGGAGCGTTAATGGAACTGATCTTTCCGCATTGTCCCTACACTACCTGCTGTTGTATGCCATAGGCACCCCCAAGCAATCCCATA";

        BaseSequence dnaSeq = new BaseSequence(dnaTest);
        BaseSequence sequence = BaseSequence.join(dnaSeq, new BaseSequence("GAGAGAAAAGAGAGGAGTTTGCAGATAAGATT"), dnaSeq.reverse().complement());
        SSACoder ss = new SSACoder(m,k);

        // System.out.println("Is the DNA sequence an (m, k)-SSA sequence? " + coder.isSSASequence(seq));

        //Coder Test
        BaseSequence enc = ss.encode(new BaseSequence(seq1));
        // ss.encode(sequence);

        //  System.out.println("result " + bEncode);
        //  System.out.println("resultLength " + bEncode.length());

        //  BaseSequence dEncode = coder.decode(bEncode);
        //  System.out.println("decoded " + dEncode);
        //  System.out.println(countDifferences(dEncode.toString(),toTest.toString()));



        String enc1 = "CAACGAGCGATCATATAGCTCCCATTGAGCAAGCATGTAGTCGACACAGAGTTTTCACCTAGAGGCCACGGAGACTGCAGTTAGATAACAGCGACGGCTCTAATACGCACCTATGACGACGCTAGTACCGTACTTAGACCTGTCTTCTACCATCCTTGGACTCGGCTCTTACTTCACTCCGACTAATCTGATACACCCCTGTGACATAGCTGGTATGGGACCAAGATGCTTCCACTATGAGCCCAGGATCGTGCCTTTATCCAACCTCGATCACTCCCATATTGACCCCTGATTTCGCCCGTATTATACCGAGATACGTCCGCTATATTCCCGGGAAGGGGCGATTAAGCCACGACGAAGTATCGTATAACGCCCGTTGAACCAGCGTGTAACAGACGCAGAATGTTCGCCTAATTGCCGCGGAATATGCGGTTAAAGAACGGCGAAATCTCTAAATAAAAATTTATGGGATTGCTTGGGGGTGCCTATGGCATACAACAGCAGGTAGTGTAGGGACAATGCGGAAAGATCAGTTCCATTAACGCTCCTTCGGACAGCTGAGGCAGCGGGACTCGACGTAACGCGAACCTACTTTGGGACTTGACATAGCTAAAGCTGTCCTTTTGCAGTCGACGGGTGGCGTGCGACTCTCGTCGTAGAAAACGGCCTACACTTTAGTGGCATCGTCAGGCAGGGTGCCGGCGAACTAAGAGAGGGATCTCTCGGAATATGCGTACGTCGCCGCAATTGGGCGGAGCAGAGCATTCGAGCCGTAAAGGCCACATTGACGGTGAGCCATTTGTGTAGGCCATCAGTCCCCGAAGCTCGCCCCCCCCCTGGGAGCTCCCACTCAGTCAGGCACGTTCTGCTTTGAACGGCACCGGATCGAAGTATCGTCCGTGGCCACTTTCACTCGGCGGGATCCTACGTGCGTTCAGCACCGAATTCTTTCTGCTCAGCTGGTGGGGAGCCCAGAGAGAAAAGAGAGGAGTTTGCAGATAAGATTTGGGCTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTG";
        System.out.println(SSACoder.countDifferences(enc, new BaseSequence(enc1)));

    }

}