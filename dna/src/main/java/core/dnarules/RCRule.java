package core.dnarules;

import core.Base;
import core.BaseSequence;
import dnacoders.headercoders.PermutationCoder;
import dnacoders.headercoders.SSACoder;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.w3c.dom.ls.LSOutput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static dnacoders.headercoders.SSACoder.*;

 public class RCRule implements DNARule{
   int m;
   int k;

    public RCRule(int m, int k){
    this.m = m;
    this.k = k;
    }

     /**
      * counts the rcError between the original sequence and the encoded sequence;
      * if the score is next to 1, less RC pair
      * @param dnaSeq DNA Sequence
      * @param encodedSeq encoded DNA Sequence
      * @param m stem length
      * @param k loop length
      * @return score
      */

    public static float rcErrorRate(BaseSequence dnaSeq, BaseSequence encodedSeq, int m, int k) {
        int ogCount = countSeparatedReverseComplements(dnaSeq, m, k);
        int encodedCount = countSeparatedReverseComplements(encodedSeq,m,k);
        System.out.println( " original sequence reverse complement with length m " + ogCount + " reverse complement with length m in encoded seq " + encodedCount );
        return  (ogCount !=0)? (float) ((ogCount - encodedCount) / ogCount) : 0f;

    }

     /**
      * counts the rcError Rate if the score is next to 1, less RC pair
      * @param dnaSeq DNA Sequence
      * @param m stem length
      * @param k loop length
      * @return score
      */

     public static float rcErrorRate(BaseSequence dnaSeq, Object instance, int m, int k) {
         if(m < 2*Math.log(dnaSeq.length()) + 2 || k <4){
             throw new RuntimeException("m should be bigger than  2*Math.log(dnaSequence.length() + 2 or k has to be bigger than 4");
         }

         BaseSequence encodedSeq = null;
         if( instance instanceof SSACoder) {
             SSACoder ssaCoder = new SSACoder(m, k);
              encodedSeq = ssaCoder.encode(dnaSeq);
         }
         if( instance instanceof PermutationCoder) {
             var ssaRule = new RCRule(m,k);
             PermutationCoder permutationCoder = new PermutationCoder(true, 4, ssaRule::evalErrorProbability);
             encodedSeq = permutationCoder.encode(dnaSeq);
         }

         int ogCount = countSeparatedReverseComplements(dnaSeq,m,k);
         int encodedCount = countSeparatedReverseComplements(encodedSeq,m,k);

         return  (ogCount !=0)? (float) ((ogCount - encodedCount) / ogCount) : 0f;
     }

     /**
      * counts the rcError Rate for SSA Coder if the score is next to 1, less RC pair
      * @param dnaSeq DNA Sequence
      * @param m stem length
      * @param k loop length
      * @return score
      */

     public static float rcErrorRate(BaseSequence dnaSeq,  int m, int k) {
         SSACoder ssaCoder = new SSACoder(m, k);
         BaseSequence encodedSeq = ssaCoder.encode(dnaSeq);

         int ogCount = countSeparatedReverseComplements(dnaSeq,m,k);
         int encodedCount = countSeparatedReverseComplements(encodedSeq,m,k);
         return  (ogCount !=0)? (float) ((ogCount - encodedCount) / ogCount) : 0f;
     }

     /**
      * counts the RC pair
      * @param dna DNA Sequence
      * @param m stem length
      * @param k loop length
      * @return score
      */

     public static int countSeparatedReverseComplements(BaseSequence dna, int m, int k) {
         if (dna == null || dna.length() < 2) {
             return 0;
         }

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

     private static int findNearestNonOverlappingIndex(String dna, String target, int minIndex) {
         int index = dna.indexOf(target, minIndex);
         return index;
     }


     /**
      * rc rate error with 0 being the worst value
      * @param seq the DNA sequence.
      * @return error value
      */
     @Override
    public float evalErrorProbability(BaseSequence seq) {
        return 1f - rcErrorRate(seq,m,k);
    }


}
