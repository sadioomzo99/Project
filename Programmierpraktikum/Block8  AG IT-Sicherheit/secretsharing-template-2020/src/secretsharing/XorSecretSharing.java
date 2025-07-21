/**
 * 
 */
package secretsharing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * This class implements the simple XOR-based (n,n) secret sharing.
 * 
 * Secrets and shares are both represented as byte[] arrays.
 * 
 * Randomness is taken from a {@link java.security.SecureRandom} object.
 * 
 * @see SecureRandom
 * 
 */
public class XorSecretSharing {

	/**
	 * Creates a XOR secret sharing object for n shares
	 * 
	 * @param n
	 *            number of shares to use. Needs to fulfill n >= 2.
	 */
	public XorSecretSharing(int n) {
		assert (n >= 2);
		this.n = n;
		this.rng = new SecureRandom();
	}

	/**
	 * Shares the secret into n parts.
	 * 
	 * @param secret
	 *            The secret to share.
	 * 
	 * @return An array of the n shares.
	 */
	public byte[][] share(final byte[] secret) {
		int l = secret.length;
		byte[][] shares = new byte[n][l];
		shares[n-1]=secret;
		for (int i=0; i< n-1;i++){
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(shares[i]);
		}
		for (int x=0;x<n-1;x++){
			for (int y=0;y<l;y++){
				shares[n-1][y]= (byte) (shares[x][y]^shares[n-1][y]);
			}
		}
		return shares;

	}

	/**
	 * Recombines the given shares into the secret.
	 * 
	 * @param shares
	 *            The complete set of n shares for this secret.
	 * 
	 * @return The reconstructed secret.
	 */
	public byte[] combine(final byte[][] shares) {
byte[] secret = new byte[shares[0].length];
secret=shares[0];
for (int i=1;i< shares.length;i++){
	for (int j=0; j< secret.length;j++) {
		secret[j]= (byte) (secret[j] ^ shares[i][j]);
	}
}

		return secret;
	}

	private final int n;

	public int getN() {
		return n;
	}

	private final Random rng;

	public static void main(String[] args) throws IOException {
		XorSecretSharing xorSecretSharing= new XorSecretSharing(3);
		byte[][] shares = {{1,1,1,1},{1,0,1,0},{1,1,0,0}};
		byte[] secret = {1,0,0,1};
		byte[][] result = xorSecretSharing.share(secret);
		xorSecretSharing.combine(shares);


		byte[][] sharess= xorSecretSharing.share(Files.readAllBytes(Path.of(("C:\\Users\\sadio\\Bureau\\shares.txt"))));

		for (int i =0; i< sharess.length;i++){
			Files.write(Paths.get("C:\\Users\\sadio\\Bureau\\secretsharing\\"+ "-"+i+".txt"),sharess[i]);
		}

		File file = new File("C:\\Users\\sadio\\Bureau\\secretsharing");
		int n = Objects.requireNonNull(file.listFiles()).length;
		int l= (int) Objects.requireNonNull(file.listFiles())[0].length();

		byte[][] newShares = new byte[n][l];
		for (int i =0; i<n;i++){
		newShares[i]=Files.readAllBytes(Path.of(String.valueOf(Objects.requireNonNull(file.listFiles())[i])));
		}
		Files.write(Paths.get("C:\\Users\\sadio\\Bureau\\combines.txt"),
		xorSecretSharing.combine(newShares));



	}
}
