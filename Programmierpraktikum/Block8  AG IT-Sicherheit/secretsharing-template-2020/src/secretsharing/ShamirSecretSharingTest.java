package secretsharing;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class ShamirSecretSharingTest {

    @org.junit.Test
    public void share() {
        ShamirSecretSharing shamirSecretSharing = new ShamirSecretSharing(2,3);

        ShamirShare[] fg = shamirSecretSharing.share(BigInteger.valueOf(12));
       BigInteger b = shamirSecretSharing.combine(fg);
       assertEquals(BigInteger.valueOf(12),b);
    }

    @org.junit.Test
    public void combine() {
    }
}