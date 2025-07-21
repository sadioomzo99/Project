package de.umr.ds.task1;

import java.util.Arrays;

/**
 * A kernel is a 2D-array. The array is transposed after initialization which
 * enables a more intuitive way of initializing a kernel. E.g a non-symmetric
 * kernel can be initialized by Kernel({{0,0,1} {0,1,0} {1,0,0}}) although the
 * array dimensions are actually [height][width].
 *
 */
public class Kernel {

	private double[][] k;
	private int height;
	private int width;

	public double[][] getK() {
		return k;
	}

	/**
	 * Initializes the kernel by its transpose.
	 * 
	 * @param k 2D array
	 */
	Kernel(double[][] k) {
		// transpose
		this.k = new double[k[0].length][k.length];
		for (int x = 0; x < k[0].length; x++)
			for (int y = 0; y < k.length; y++)
				this.k[x][y] = k[y][x];
		this.width = this.k.length;
		this.height = this.k[0].length;

		if (this.width % 2 != 1 || this.height % 2 != 1)
			throw new IllegalArgumentException("Kernel size need to be odd-numbered");
		if (this.width < 3 || this.height < 3)
			throw new IllegalArgumentException("Minimum dimension is 3");
	}

	/**
	 * Convolves a single-channel image with the kernel.
	 * 
	 * @param img A single-channel image
	 * @return The convolved image
	 */
	public int[][] convolve(int[][] img) {
		int resultHeight= img[0].length - height +1;
		int resultWidth  = img.length - width +1;
		int[][] result = new int[resultWidth][resultHeight];
		int aX = height-1;
		int aY = width-1;
		// TODO Task 1d)
		for(int x=0;x< resultWidth;x++) {
			for (int y = 0; y < resultHeight; y++) {
				double s=0;
				for(int i=0;i<width ;i++) {
					for (int j = 0; j <height ; j++) {
						s+= img[x + aX - i][y + aY - j] *  k[i][j];
					}
				}
				result[x][y]=(int) s;
			}

		}
//		int[][] newImage = new int[resultHeight+ getHeight()-1][resultWidth+ getWidth()-1];
//		for(int i=0; i<resultHeight;i++){
//			for (int j=0; j<resultWidth;j++){
//				newImage[i][j]=result[i][j];
//			}
//		}
		return result;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public static void main(String[] args) {
		Kernel k = new Kernel ( new double[][] {{1, 0, 1},
				{0, 1, 0},
				{1, 0, 1}} );
		int[][] img = new int[][]{ {1, 0,0 },
				{1, 1, 0},
				{1, 1, 1},
				{1,2,3}
		};

		System.out.println(Arrays.toString(k.convolve(img)));
	}

}
