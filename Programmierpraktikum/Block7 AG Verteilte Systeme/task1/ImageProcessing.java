package de.umr.ds.task1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessing {

	/**
	 * Loads an image from the given file path
	 *
	 * @param path The path to load the image from
	 * @return BufferedImage
	 */
	private static BufferedImage loadImage(String path) throws IOException {

		// TODO Task 1a)

		return ImageIO.read(new File(path));
	}


	/**
	 * Converts the input RGB image to a single-channel gray scale array.
	 * 
	 * @param img The input RGB image
	 * @return A 2-D array with intensities
	 */
	private static int[][] convertToGrayScaleArray(BufferedImage img) {
		
		// TODO Task 1b)
		int[][] pixels = new int[img.getHeight()][img.getWidth()];

		for(int i=0;i< img.getHeight();i++){
			for (int j=0; j<img.getWidth();j++){
				Color c = new Color(img.getRGB(j,i));
				double red = c.getRed()*0.299;
				double green = c.getGreen()*0.587;
				double blue = c.getBlue()*0.114;
				double grey = red+blue+green;
				Color newColor = new Color((int) grey,
						(int) grey,
						(int) grey);
				img.setRGB(j,i,newColor.getRGB());
		        pixels[i][j]=newColor.getRGB();
			}
		}
		
		return pixels;
	}

	/**
	 * Converts a single-channel (gray scale) array to an RGB image.
	 * 
	 * @param img The input image array
	 * @return BufferedImage
	 */
	private static BufferedImage convertToBufferedImage(int[][] img) {
		
		// TODO Task 1c)
		int width= img[0].length;
		int  height= img.length;
		BufferedImage image= new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0; i<height; i++) {
			image.setRGB(0,i,width,1,img[i],0,width );
		}
		return image;
	}

	private static void saveImage(BufferedImage img, String path) throws IOException {

		// TODO Task 1c)
		if(img!= null){
			File file = new File(path);
			if(file.createNewFile()){
				ImageIO.write(img,"jpg",file);
			}
		}
	}

	/**
	 * Converts input image to gray scale and applies the kernel.
	 * 
	 * @param img The RGB input image
	 * @param kernel The kernel to apply
	 * @return The convolved gray-scale image
	 */
	private static BufferedImage filter(BufferedImage img, Kernel kernel) {

		// TODO Task 1f)
		int [][] pixels = convertToGrayScaleArray(img);


//				new int[img.getHeight()][img.getWidth()];
//		for(int row=0; row< img.getHeight();row++){
//			img.getRGB(0,row,img.getWidth(),1,pixels[row],0,img.getWidth() )
//		}

		return convertToBufferedImage(kernel.convolve(pixels));
	}


	// TODO Task 1g)


	public static void main(String[] args) throws IOException {


		saveImage(filter(loadImage("C:\\Users\\sadio\\Downloads\\ProPra22-AG_DS_Aufgaben\\ProPra22-Aufgaben\\example.jpg"),new Kernel ( new double[][] {{0, 0, 3},
			{0, 2, 0},
				{1, 0, 0}} )),"C:\\Users\\sadio\\Bureau\\grayImage.jpg");
		Kernel kernel= Kernels.MotionBlur();
		Kernel kernel1= Kernels.BoxBlur3x3();

		saveImage(convertToBufferedImage(convertToGrayScaleArray(loadImage("C:\\Users\\sadio\\Downloads\\ProPra22-AG_DS_Aufgaben\\ProPra22-Aufgaben\\example.jpg"))),"C:\\Users\\sadio\\Bureau\\grayImage2.jpg");
		saveImage(filter(loadImage("C:\\Users\\sadio\\Downloads\\ProPra22-AG_DS_Aufgaben\\ProPra22-Aufgaben\\example.jpg"),kernel),"C:\\Users\\sadio\\Bureau\\grayImage1.jpg");


		saveImage(filter(loadImage("C:\\Users\\sadio\\Downloads\\ProPra22-AG_DS_Aufgaben\\ProPra22-Aufgaben\\example.jpg"),kernel1),"C:\\Users\\sadio\\Bureau\\grayImage3.jpg");


		}


	}
