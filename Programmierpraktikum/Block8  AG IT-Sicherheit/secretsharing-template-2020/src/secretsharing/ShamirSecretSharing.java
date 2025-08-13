package secretsharing;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class implements Shamir's (t,n) secret sharing.
 * 
 * Secrets are represented as BigInteger objects, shares as ShamirShare objects.
 * 
 * Randomness is taken from a {@link java.security.SecureRandom} object.
 *
 * @see secretsharing.ShamirShare
 * @see BigInteger
 * @see SecureRandom
 * 
 * @author ewti
 * 
 */
public class ShamirSecretSharing {

	/**
	 * Creates a (t,n) Shamir secret sharing object for n shares with threshold t.
	 * 
	 * @param t
	 *            threshold: any subset of t <= i <= n shares can recover the
	 *            secret.
	 * @param n
	 *            number of shares to use. Needs to fulfill n >= 2.
	 */
	public ShamirSecretSharing(int t, int n) {
		assert (t >= 2);
		assert (n >= t);

		this.t = t;
		this.n = n;
		this.rng = new SecureRandom();

		// use p = 2^2048 + 981
		this.p = BigInteger.ONE.shiftLeft(2048).add(BigInteger.valueOf(981));
	}

	/**
	 * Shares the secret into n parts.
	 * 
	 * @param secret
	 *            The secret to share.
	 * 
	 * @return An array of the n shares.
	 */
	public ShamirShare[] share(BigInteger secret) {
	BigInteger[] a = new BigInteger[t];
	a[0]=secret;
	for (int i =1; i<t;i++){
		a[i] =  new BigInteger(p.bitLength(), rng);
	}

	ShamirShare[] samirShare = new ShamirShare[n];
	for (int j=0;j<n;j++){
	BigInteger f = horner(BigInteger.valueOf(j+1),a);
		samirShare[j] = new ShamirShare(BigInteger.valueOf(j+1),f);
	}

		return samirShare;
	}

	/**
	 * Evaluates the polynomial a[0] + a[1]*x + ... + a[t-1]*x^(t-1) modulo p at
	 * point x using Horner's rule.
	 * 
	 * @param x
	 *            point at which to evaluate the polynomial
	 * @param a
	 *            array of coefficients
	 * @return value of the polynomial at point x
	 */
	private BigInteger horner(BigInteger x, BigInteger[] a) {
		BigInteger c = a[a.length-1];
		for (int i =a.length-2; i>=0;i--){
			c= c.multiply(x).add(a[i]).mod(p);
		}
		return c;
	}

	/**
	 * Recombines the given shares into the secret.
	 * 
	 * @param shares
	 *            A set of at least t out of the n shares for this secret.
	 * 
	 * @return The reconstructed secret.
	 */
	public BigInteger combine(ShamirShare[] shares) {
		BigInteger mul = BigInteger.valueOf(1);
		BigInteger res = BigInteger.valueOf(0);
		// TODO: implement this
		for (int i=0; i<shares.length;i++){

			BigInteger vi = shares[i].s;
			for (int j =0; j<shares.length;j++){

				if(i!=j){
						BigInteger s =shares[j].x;

						mul= mul.multiply(s.negate().multiply(shares[i].x.subtract(s).modInverse(p)));
						vi = vi.multiply(mul).mod(p);
				}
			}
			//BigInteger bigIntegers=shares[i].s.multiply(mul);
nm 			res = res.add(vi).mod(p);

		}

		return res;
	}

	public int getT() {
		return t;
	}

	public int getN() {
		return n;
	}

	private final int t;
	private final int n;
	private final SecureRandom rng;
	private final BigInteger p;

}
