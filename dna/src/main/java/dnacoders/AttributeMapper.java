package dnacoders;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.DNARule;
import dnacoders.headercoders.BasicDNAPadder;
import dnacoders.headercoders.PermutationCoder;
import utils.AsymmetricCoder;
import utils.Coder;
import utils.DNAPacker;
import utils.FuncUtils;

/**
 * AttributeMapper is an attribute's coder that keeps the encoding as short as possible and encodes an attribute simply by:
 * 1. Mapping the attribute onto a String as <code>"key=value"</code>.
 * 2. Flipping this String to <code>"eulav=yek"</code>.
 * 3. Encoding this String to a BaseSequence with the given DNA convertor.
 */
public class AttributeMapper {

    public static final String MAPPING_STRING = "=";

    /**
     * Creates an attribute's mapper with a given DNA convertor.
     * @param dnaConvertor the DNA convertor.
     * @return the attribute's mapper.
     */
    public static AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> newInstance(Coder<String, BaseSequence> dnaConvertor) {
        return new AsymmetricCoder<>() {
            @Override
            public BaseSequence encode(Attribute<?> attribute) {
                return dnaConvertor.encode(FuncUtils.reverseCharSequence(attribute.getName() + MAPPING_STRING + attribute.getValue()).toString());
            }

            @Override
            public Attribute<String> decode(BaseSequence seq) {
                return parseAttribute(FuncUtils.reverseCharSequence(dnaConvertor.decode(seq)).toString());
            }
        };
    }

    public static AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeEncoder(boolean parallel, int targetLength, int permsCount, Coder<String, BaseSequence> dnaConvertor, DNARule rules) {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder =  AsymmetricCoder.fuse(
                AttributeMapper.newInstance(dnaConvertor),
                new BasicDNAPadder(targetLength - DNAPacker.pack(permsCount - 1).length())
        );

        if (permsCount == 0)
            return coder;

        return AsymmetricCoder.fuse(
                coder,
                new PermutationCoder(parallel, permsCount, seq -> -rules.evalErrorProbability(seq))
        );
    }


    public static Attribute<String> parseAttribute(String s) {
        if (s == null)
            throw new RuntimeException("failed parsing attribute from null string");

        String[] split = s.split(MAPPING_STRING);
        if (split.length != 2)
            throw new RuntimeException("failed parsing attribute: " + s);

        return new Attribute<>(split[0], split[1]);
    }
}
