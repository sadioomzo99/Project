package dnacoders.headercoders;

import core.Base;
import core.BaseSequence;
import utils.Coder;

import java.util.concurrent.ForkJoinPool;

public class SSACoder implements Coder<BaseSequence, BaseSequence> {
    private final int m;
    private final int k;
    private final int t;

    public SSACoder(int m, int k){
        this.m = m;
        this.k = k;
        this.t = m/2;
    }

     static void condCheck(int length, int m, int k) {
        if(m < 2*Math.log(length) + 2 || k <4){
            throw new RuntimeException("m should be bigger than  2*Math.log(dnaSequence.length() + 2 or k has to be bigger than 4");
        }
    }


    /**
     * encodes the dna Sequence in SSA(m,k);
     * @param dnaSequence given dna Sequence
     * @return encoded dna Sequence
     * @throws RuntimeException in add suffix if the length of the encoded sequence bigger than the original sequence
     */
    public BaseSequence encode(BaseSequence dnaSequence){
        condCheck(dnaSequence.length(),m,k);
        BaseSequence x = new BaseSequence("T" + dnaSequence);
        int ogLength = x.length();
        ForkJoinPool forkJoinPool = new ForkJoinPool(); // Für parallele Verarbeitung

        while (!isSSASequence(x)) {
            int t = (int) Math.ceil(m / 2.0);

            while (!hasTKcomplementSequences(x, t, k).isEmpty()) {
                x = new BaseSequence(Creplacement(hasTKcomplementSequences(x,t,k), x, ogLength));
            }

            int tHalb = t / 2;
            while (!hasX1X2Sequence(x, tHalb).toString().isEmpty()) {
                x = new BaseSequence(AReplacement(hasX1X2Sequence(x, tHalb), x, tHalb));
            }

            x = new BaseSequence(addSuffix("T" + dnaSequence, x.toString()));
        }

        forkJoinPool.shutdown();
        return x;
    }

    /**
     * decodes the given encoded Sequence
     * @param dnaSequence given dna Sequence
     * @return decoded dnaSequence
     * @throws RuntimeException when the sequence starts with G
     */
    public BaseSequence decode(BaseSequence dnaSequence){
        condCheck(dnaSequence.length(),m,k);
        // Input validation
        validateInput(dnaSequence, m, k);

        // Base case - starts with 'T'
        if (dnaSequence.toString().charAt(0) == 'T') {
            return dnaSequence.subSequence(1);
        }

        BaseSequence processed;
        switch (dnaSequence.toString().charAt(0)) {
            case 'C':
                processed = processCaseC(dnaSequence, m);
                break;
            case 'A':
                processed = processCaseA(dnaSequence, m);
                break;
            case 'G':
                throw new IllegalArgumentException("Invalid starting nucleotide G");
            default:
                throw new IllegalArgumentException("Invalid starting nucleotide");
        }

        // Truncate to original length BEFORE recursion
        BaseSequence truncated = processed.subSequence(0, dnaSequence.length());

        // Continue decoding recursively
        return decode(truncated);
    }


    private static void validateInput(BaseSequence dnaSequence, int m, int k) {
        if (dnaSequence == null || dnaSequence.toString().isEmpty()) {
            throw new IllegalArgumentException("DNA sequence cannot be null or empty");
        }

        double minM = 2 * Math.log(dnaSequence.length() + 2);
        if (m < minM || k < 4) {
            throw new IllegalArgumentException(
                    String.format("Invalid parameters: m must be >= %.2f and k must be >= 4", minM));
        }
    }


    /**
     * decodes the pointer Q
     * @param sequence original dna Sequence
     * @param m given stem length
     * @return decoded Sequence
     */
    private static BaseSequence processCaseA(BaseSequence sequence, int m) {
        double prefixLength = Math.ceil(3 + 0.5 * (Math.log(sequence.length()) / Math.log(2)));
        BaseSequence prefix = sequence.subSequence(0, (int) prefixLength);

        // Extract components
        BaseSequence x1x2 = prefix.subSequence(1, 3);
        int beta = DNAtoNumConverter(prefix.subSequence(3));
        int t = (int) Math.ceil(m / 2.0);

        // Process sequence
        BaseSequence result = sequence.replace(prefix, new BaseSequence(""));

        // Create and insert repeat pattern
        String repeatPattern = x1x2.toString().repeat(t / 2);
        result.insert(beta, new BaseSequence(repeatPattern));

        return result;
    }

    /**
     * decodes the pointer P
     * @param sequence original dna Sequence
     * @param m given stem length
     * @return decoded Sequence
     */
    private static BaseSequence processCaseC(BaseSequence sequence, int m) {
        double prefixLength = 1 + (Math.log(sequence.length()) / Math.log(2));
        BaseSequence prefix = sequence.subSequence(0, (int) prefixLength);

        // Extract position and alpha values
        int pos = (prefix.length() - 1) / 2;
        int kP = DNAtoNumConverter(prefix.subSequence(1, 1 + pos));
        int alpha = DNAtoNumConverter(prefix.subSequence(1 + pos));

        // Calculate insertion position and length
        int t = (int) Math.ceil(m / 2.0);
        int j = alpha + kP + t;

        // Process sequence
        BaseSequence remainingSeq = sequence.replace(prefix, new BaseSequence(""));
        BaseSequence reverseComp = reverseComplement(remainingSeq.subSequence(alpha, alpha + t));

        // Insert reverse complement
        remainingSeq.insert(j, reverseComp);
        return remainingSeq;
    }


    // Step1

    /**
     * checks if a DNA sequence is an (𝑚, 𝑘)-SSA Sequence
     * @param dnaSequence given dna Sequence
     * @return true if it's the case or false if not
     */
    public boolean isSSASequence(BaseSequence dnaSequence){
        condCheck(dnaSequence.length(),m,k);
        if (dnaSequence == null || dnaSequence.length() < 2) {
            return false;
        }


        // Alle möglichen Teilsequenzen durchsuchen
        for (int len = 2; len <= dnaSequence.length(); len++) { // Mindestens 2 Basen lang
            for (int start = 0; start <= dnaSequence.length() - len; start++) {
                BaseSequence subSequence = dnaSequence.subSequence(start, start + len);
                BaseSequence reverseComplement = reverseComplement(subSequence);

                // Suche das Reverse-Komplement an einer anderen Stelle
                int firstIndex = start;
                int secondIndex = findNearestNonOverlappingIndex(dnaSequence, reverseComplement, firstIndex + + len + k);


                if (secondIndex != -1 && subSequence.length() >= m) {
                    System.out.println("problem " + subSequence + " - " + reverseComplement);
                    System.out.println("pos1 " + firstIndex + " pos2 " + secondIndex);
                    return false;
                }
            }
        }


        return true;
    }



    //Step2 t = 0.5m

    /**
     * checks  the earliest occurrence
     * of a pair of non-overlapping subsequences, denoted as 𝐲 and 𝐳,
     * each with a length of 𝑡, that meet the condition 𝐳 = 𝑅𝐶(𝐲). Here,
     * 𝐲 initiates at position 𝑖, 𝐳 begins at position 𝑗, and the separation
     * between these starting positions, denoted as 𝑗 − 𝑖, must be greater
     * than or equal to 𝑡 + 𝑘.
     * @param dnaSequence given dna Sequence
     * @return string with the position i-j and the z subsequence
     */
    public static String hasX1X2Sequence(BaseSequence dnaSequence, int tHalb) {
        int count = 1;
        int idx = 2;
        int i = 0;

        while (idx < dnaSequence.length() -1 && i < dnaSequence.length() -3) {
            if (dnaSequence.subSequence(idx, idx + 2).equals(dnaSequence.subSequence(i, i + 2))){
                count++;
                idx += 2;
                if (count == tHalb){
                    return i + "-" + dnaSequence.subSequence(i, i + 2);
                }
            } else {
                i++;
                idx = i+2;
                count=1;
            }

        }
        return "";
    }

    //CASE1

    /**
     * proceeds to the CReplacement
     * @param reversed contains the infos of (T-K) Complement Sequence
     * @param dnaSequence given dna Sequence
     * @return dna Sequence after CKAlpha replacement
     */
    static BaseSequence Creplacement(String reversed, BaseSequence dnaSequence, int oglenth){
        String[] reverseInfo = reversed.split("-");
        return createReplacementSequence(Integer.parseInt(reverseInfo[0]),Integer.parseInt(reverseInfo[1]), dnaSequence,new BaseSequence(reverseInfo[2]), oglenth);
    }

    static BaseSequence createReplacementSequence(int startIndex, int endIndex, BaseSequence dnaSequence,
                                            BaseSequence complementarySeq, int originalLength) {
        // Input validation
        if (startIndex < 0 || endIndex <= startIndex || dnaSequence == null || complementarySeq == null) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        int distance = endIndex - (startIndex + complementarySeq.length());
        BaseSequence distanceEncoding = NumToDNAConverter(distance, originalLength);
        BaseSequence startIndexEncoding = NumToDNAConverter(startIndex, originalLength);

        // Build the prefix: "C" + distanceEncoding + startIndexEncoding
        StringBuilder prefix = new StringBuilder()
                .append('C')
                .append(distanceEncoding)
                .append(startIndexEncoding);

        // Build the final sequence
        StringBuilder result = new StringBuilder(dnaSequence.toString());

        if (endIndex + complementarySeq.length() > result.length()) {
            throw new IllegalArgumentException("Replacement would exceed sequence bounds");
        }

        result.delete(endIndex, endIndex + complementarySeq.length());
        return new BaseSequence(prefix.append(result).toString());
    }
    // CASE2
    /**
     * proceeds to the AReplacement
     * @param markerInfo contains the index of s and the subsequence x1x2
     * @param dnaSequence given dna Sequence
     * @return dna Sequence after 𝐴𝑥1𝑥2beta replacement
     */
    static BaseSequence AReplacement(String markerInfo, BaseSequence dnaSequence, int halfLength) {
        // Input validation
        if (markerInfo == null || dnaSequence == null || halfLength <= 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        String[] markerParts = markerInfo.split("-");
        if (markerParts.length != 2) {
            throw new IllegalArgumentException("Marker info must be in format 'index-sequence'");
        }

        try {
            int startIndex = Integer.parseInt(markerParts[0]);
            String sequenceMarker = markerParts[1];

            // Build the prefix: "A" + sequenceMarker + encodedIndex
            StringBuilder prefix = new StringBuilder()
                    .append('A')
                    .append(sequenceMarker)
                    .append(NumToDNAConverter(startIndex, dnaSequence.length()));

            // Verify deletion bounds
            int deletionEnd = startIndex + (halfLength * 2);
            if (startIndex < 0 || deletionEnd > dnaSequence.length()) {
                throw new IllegalArgumentException("Replacement would exceed sequence bounds");
            }

            // Build the result
            StringBuilder resultSequence = new StringBuilder(dnaSequence.toString())
                    .delete(startIndex, deletionEnd);

            return new BaseSequence(prefix.append(resultSequence).toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("First part of marker must be a valid integer", e);
        }
    }

    //Step3

    /**
     * completes the encoded dna Sequence with G and C until the original length is reached
     * if ns is even complete with GC else add G at the end
     * @param originalSequence original dna Sequence
     * @param modifiedSequence encoded dna Sequence
     * @return dna Sequence with original length
     */
    static String addSuffix(String originalSequence, String modifiedSequence) {
        int lengthDifference = originalSequence.length() - modifiedSequence.length();

        if (lengthDifference < 0) {
            throw new IllegalArgumentException(
                    String.format("Modified sequence length (%d) cannot exceed original sequence length (%d)",
                            modifiedSequence.length(), originalSequence.length()));
        }

        StringBuilder suffix = new StringBuilder();

        // Add "GT" pairs for each pair of nucleotides needed
        int gtPairsCount = lengthDifference / 2;
        for (int i = 0; i < gtPairsCount; i++) {
            suffix.append("GT");
        }

        // Add final "G" if odd length difference
        if (lengthDifference % 2 != 0) {
            suffix.append("G");
        }

        return modifiedSequence + suffix.toString();
    }


    //HILFSMETHODEN

    /**
     * computes the loop distance
     * @param seq1Pos position i
     * @param mainSeq dna Sequence
     * @param seq2 subsequence
     * @return distance
     */
    public static int computeLoopDistance1(int seq1Pos, String mainSeq, String seq2) {
        int index = mainSeq.indexOf(seq2, seq1Pos + seq2.length());
        return (index != -1) ? index - (seq1Pos + seq2.length()) : -1;
    }

    /**
     * gives Index of the dna Subsequence in a Dna Sequence
     * @param mainSequence main dna Sequence
     * @param target dna subsequence
     * @param minIndex search from this index
     * @return index of subsequence
     */
    public static int findNearestNonOverlappingIndex(BaseSequence mainSequence, BaseSequence target, int minIndex) {
        int index = mainSequence.toString().indexOf(target.toString(), minIndex);
        return index;
    }

    /**
     * Converts a number in BaseSequence after converting it first to base 4.
     * @param num given number
     * @param dnaSequenceLength dna sequence length
     * @return the dna representation of this num
     */
    static BaseSequence NumToDNAConverter(int num, int dnaSequenceLength) {
        // Input validation
        if (num < 0) {
            throw new IllegalArgumentException("Input number must be non-negative.");
        }
        if (dnaSequenceLength <= 0) {
            throw new IllegalArgumentException("DNA sequence length must be positive.");
        }

        // Edge case: num = 0 → returns a sequence of 'A's of the required length
        double req = (Math.log(dnaSequenceLength) / Math.log(4));
        if (num == 0) {
            return new BaseSequence("A".repeat((int) req));
        }

        // Convert num to base-4
        StringBuilder base4result = new StringBuilder();
        while (num > 0) {
            base4result.insert(0, num % 4);
            num /= 4;
        }

        // Pad with leading zeros if needed
        int requiredLength = (int) req;
        while (base4result.length() < requiredLength) {
            base4result.insert(0, '0');
        }

        // Map base-4 digits to DNA bases
        StringBuilder dnaSeq = new StringBuilder();
        for (char c : base4result.toString().toCharArray()) {
            switch (c) {
                case '0' -> dnaSeq.append('A');
                case '1' -> dnaSeq.append('T');
                case '2' -> dnaSeq.append('C');
                case '3' -> dnaSeq.append('G');
                default -> throw new IllegalStateException("Unexpected digit in base-4 conversion: " + c);
            }
        }
        return new BaseSequence(dnaSeq.toString());
    }


    /**
     * Converts this BaseSequence to a number .
     * @param dnaString dna Sequence
     * @return the base 10 representation of this BaseSequence
     */
    static int DNAtoNumConverter(BaseSequence dnaString){
        StringBuilder num = new StringBuilder();
        for(int i=0; i< dnaString.length(); i++){
            switch (dnaString.toString().charAt(i)) {
                case 'A' -> num.append('0');
                case 'T' -> num.append('1');
                case 'C' -> num.append('2');
                case 'G' -> num.append('3');
            }
        }

        return Integer.parseInt(String.valueOf(num), 4);
    }

    /**
     * For DNA, complement of A is T, C is G, G is C, and T is A
     * Reverse the sequence and replace each character with its complement
     * Example: ATCG -> CGAT
     * @param sequence given dna Sequence
     * @return reversed Complement of given dna Sequence
     */

    // reverse() + complement()
    public static BaseSequence reverseComplement(BaseSequence sequence) {
        return sequence.reverse().complement();
    }

    /**
     * checks if given dna sequence contains an initial occurrence of a subsequence 𝐬 within the input 𝐱 that
     * conforms to the pattern 𝐬 = (𝑥1𝑥2)^𝑡∕2,
     * @param dnaSequence given dna Sequence
     * @return string with the index of s and the subsequence x1x2
     */
    public static String hasTKcomplementSequences(BaseSequence dnaSequence, int t, int k) {
        for (int i = 0; i + t <= dnaSequence.length(); i++) {
            BaseSequence subsequenceY = dnaSequence.subSequence(i, i + t);
            BaseSequence subsequenceZ = reverseComplement(subsequenceY);

            int secondIndex = findNearestNonOverlappingIndex(dnaSequence, subsequenceZ, i+t+k);
            if (secondIndex != -1) {
                return i + "-" + secondIndex + "-" + subsequenceZ;
            }
        }

        return "";
    }

    public static int countDifferences(BaseSequence str1, BaseSequence str2) {
        int length = Math.min(str1.length(), str2.length());
        int count = 0;

        for (int i = 0; i < length; i++) {
            if (str1.toString().charAt(i) != str2.toString().charAt(i)) {
                count++;
            }
        }

        return count;
    }


}



