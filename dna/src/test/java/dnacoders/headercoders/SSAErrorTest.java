package dnacoders.headercoders;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.RCRule;

public class SSAErrorTest {

        public static void main(String[] args) throws Exception {
            var ssaRule = new RCRule(18,4);
            SSACoder coder = new SSACoder(18, 4);

            String toEncode = "CGGCCTGCGGCATGCCCGACTGCGAGCGACGCAAGCTGTGGCGGTGTCCTCCCCGGTCGCTCGCTCGAGGATCGAGCGGCGGCCCCAATCGCACGCCCCGGGGTACACCGCTGTCGAGAGAAAAGAGAGGAGTTTGCAGAGACAGCGGTGTACCCCGGGGCGTGCGATTGGGGCCGCCGCTCGATCCTCGAGCGAGCGACCGGGGAGGACACCGCCACAGCTTGCGTCGCTCGCAGTCGGGCATGCCGCAGGCCG";
            BaseSequence toTest = new BaseSequence(toEncode);
            PermutationCoder pCoder = new PermutationCoder(false, 4, ssaRule::evalErrorProbability);

            BaseSequence bEncode = coder.encode(toTest);
            BasicDNARules rules = new BasicDNARules();
            rules.addRule(ssaRule);

            float rcError = RCRule.rcErrorRate(toTest, toTest, 10,4);
            float rcError2 = RCRule.rcErrorRate(toTest, coder.encode(toTest), 10,4);
            float rcErrorPermutation = RCRule.rcErrorRate(toTest, pCoder.encode(toTest), 10, 4);

            System.out.println("rcerror " + rcError);
            System.out.println("rcError2 " +rcError2);
            System.out.println("rcErroPer " +rcErrorPermutation);


            System.out.println("normal Sequenz " + BasicDNARules.INSTANCE.evalErrorProbability(toTest));
            System.out.println("encoded " + BasicDNARules.INSTANCE.evalErrorProbability(bEncode));
            System.out.println("permutationcoder " + BasicDNARules.INSTANCE.evalErrorProbability(pCoder.encode(toTest)));

        }

}
