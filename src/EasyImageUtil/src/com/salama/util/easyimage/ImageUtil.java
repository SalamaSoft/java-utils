package com.salama.util.easyimage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.builders.BufferedImageBuilder;

/**
 * 
 * @author XingGu Liu
 *
 */
public class ImageUtil {
	public enum ImageType {UNKNOWN, PNG, JPEG, GIF, BMP};
	
	public static final String IMAGE_FORMAT_PNG = "png";
	public static final String IMAGE_FORMAT_JPEG = "jpeg";
	public static final String IMAGE_FORMAT_GIF = "gif";

	private ImageUtil() {
		
	}

	public static ImageType getImageType(File inputFile) throws FileNotFoundException {
		FileInputStream input = null;
		
		try {
			input = new FileInputStream(inputFile);

			return getImageType(input);
		} finally {
			try {
				input.close();
			} catch(Exception e) {
			}
		}
	}
	
	public static ImageType getImageType(InputStream input) {
		byte[] header = new byte[8];
		
		int readCnt;
		try {
			readCnt = input.read(header, 0, header.length);
			
			if(readCnt < 8) {
				return ImageType.UNKNOWN;
			} else {
				if(header[0] == 0x89 
						&& header[1] == 0x50
						&& header[2] == 0x4e
						&& header[3] == 0x47
						&& header[4] == 0x0d
						&& header[5] == 0x0a
						&& header[6] == 0x1a
						&& header[7] == 0x0a
						) {
					return ImageType.PNG;
				} else if(header[0] == 0x47 
						&& header[1] == 0x49
						&& header[2] == 0x46
						&& header[3] == 0x38
						&& (header[4] == 0x39 || header[4] == 0x37)
						&& header[5] == 0x61
						) {
					return ImageType.GIF;
				} else if(header[0] == 0xff
						&& header[1] == 0xd8) {
					return ImageType.JPEG;
				} else if(header[0] == 0x42
						&& header[1] == 0x4d) {
					return ImageType.BMP;
				} else {
					return ImageType.UNKNOWN;
				}
			}
		} catch (IOException e) {
			return ImageType.UNKNOWN;
		}
	}
	
	public static boolean isImageTypePNG(InputStream input) {
		byte[] header = new byte[4];
		
		int readCnt;
		try {
			readCnt = input.read(header, 0, header.length);
			
			if(readCnt < header.length) {
				return false;
			} else {
				if(header[0] == 0x89 
						&& header[1] == 0x50
						&& header[2] == 0x4e
						&& header[3] == 0x47
						&& header[4] == 0x0d
						&& header[5] == 0x0a
						&& header[6] == 0x1a
						&& header[7] == 0x0a
						) {
					return true;
				} else {
					return false;
				}
			}
			
		} catch (IOException e) {
			return false;
		}
	}

	public static void convertImageToPNG(File imageFile) throws FileNotFoundException, IOException {
		convertImageToPNG(imageFile, imageFile);
	}
	
	public static void convertImageToPNG(File srcImageFile, File destImageFile) throws FileNotFoundException, IOException {
		BufferedImage srcImage = ImageIO.read(srcImageFile);
		ImageIO.write(srcImage, IMAGE_FORMAT_PNG, destImageFile);
	}
	
	public static void convertImageToJPEG(File imageFile) throws FileNotFoundException, IOException {
		convertImageToJPEG(imageFile, imageFile);
	}

	public static void convertImageToJPEG(File srcImageFile, File destImageFile) throws FileNotFoundException, IOException {
		BufferedImage srcImage = ImageIO.read(srcImageFile);
		ImageIO.write(srcImage, IMAGE_FORMAT_JPEG, destImageFile);
	}

	public static void createImage(File originalImage, 
			int toWidth, int toHeight, File outputFile, String format) throws IOException {
		createImage(originalImage, toWidth, toHeight, outputFile, false, format);
	}
	
	public static void createImage(File originalImage, 
			int toWidth, int toHeight, File outputFile, boolean imgAllowEmptyRegion, String format) throws IOException {
		Builder<File> builder = Thumbnails.of(originalImage);
		
		BufferedImage img = ImageIO.read(originalImage);
		int originalWidth =  img.getWidth();
		int originalHeight = img.getHeight();
		
		createImage(builder, originalWidth, originalHeight, toWidth, toHeight, outputFile, imgAllowEmptyRegion, format);
	}

	public static void createImage(InputStream originalImage, 
			int toWidth, int toHeight, File outputFile, String format) throws IOException {
		createImage(originalImage, toWidth, toHeight, outputFile, false, format);
	}
	
	public static void createImage(InputStream originalImage, 
			int toWidth, int toHeight, File outputFile, boolean imgAllowEmptyRegion, String format) throws IOException {
		Builder<? extends InputStream> builder = Thumbnails.of(originalImage);
		
		BufferedImage img = ImageIO.read(originalImage);
		int originalWidth =  img.getWidth();
		int originalHeight = img.getHeight();

		createImage(builder, originalWidth, originalHeight, toWidth, toHeight, outputFile, imgAllowEmptyRegion, format);
	}
	
	protected static void createImage(Builder<?> builder, int originalWidth, int originalHeight, 
			int toWidth, int toHeight, File outputFile, boolean imgAllowEmptyRegion, String format) throws IOException {
		int toW, toH;
		
		if(toWidth == 0 || toHeight == 0) {
			toW = Math.abs(toWidth);
			toH = Math.abs(toHeight);
		} else {
			if(toWidth < 0 || toHeight < 0) {
				//keep the orignal aspect ratio
				if(toWidth < 0 && toHeight < 0) {
					//make the longer original edge length to toSize
					if(originalWidth > originalHeight) {
						toW = Math.abs(toWidth);
						toH = 0;
					} else {
						toH = Math.abs(toHeight);
						toW = 0;
					}
				} else {
					//make the shorter original edge length to toSize
					if(originalWidth < originalHeight) {
						toW = Math.abs(toWidth);
						toH = 0;
					} else {
						toH = Math.abs(toHeight);
						toW = 0;
					}
				}
			} else {
				//all > 0
				toW = toWidth;
				toH = toHeight;
			}
		}
		
		
		if(toW == 0 && toH == 0) {
			builder.outputFormat(format).toFile(outputFile);
		} else {
			if (toW == 0 && toH != 0) {
				toW = toH * originalWidth / originalHeight;
				if(toW == 0) {
					toW = 1;
				}
			} else if (toW != 0 && toH == 0) {
				toH = toW * originalHeight / originalWidth;
				if(toH == 0) {
					toH = 1;
				}
			}
			
			if((toWidth < 0 || toHeight < 0) && !imgAllowEmptyRegion) {
				if(originalWidth >= Math.abs(toWidth) && originalHeight >= Math.abs(toHeight)) {
					if(toW == Math.abs(toWidth) && toH < Math.abs(toHeight)) {
						toW = toW * Math.abs(toHeight) / toH;
						toH = Math.abs(toHeight);
					} else if (toH == Math.abs(toHeight) && toW < Math.abs(toWidth)) {
						toH = toH * Math.abs(toWidth) / toW;
						toW = Math.abs(toWidth);
					}
				}
			}

			builder.size(toW, toH)
			.outputFormat(format).toFile(outputFile);
		} 
	}

	public static void createImage(File originalImage, 
			int xOfOriginalImage, int yOfOriginalImage, int widthOfOriginalImage, int heightOfOriginalImage,
			int toWidth, int toHeight, File outputFile, 
			String format) throws IOException {
		createImage(originalImage, 
				xOfOriginalImage, yOfOriginalImage, widthOfOriginalImage, heightOfOriginalImage,
				toWidth, toHeight, outputFile, 
				false, format);
	}

	public static void createImage(File originalImage, 
			int xOfOriginalImage, int yOfOriginalImage, int widthOfOriginalImage, int heightOfOriginalImage,
			int toWidth, int toHeight, File outputFile, 
			boolean imgAllowEmptyRegion, String format) throws IOException {
		Builder<File> builder = Thumbnails.of(originalImage);
		createImage(builder, xOfOriginalImage, yOfOriginalImage, widthOfOriginalImage, 
				heightOfOriginalImage, toWidth, toHeight, outputFile, imgAllowEmptyRegion, format);
	}
	
	public static void createImage(InputStream originalImage, 
			int xOfOriginalImage, int yOfOriginalImage, int widthOfOriginalImage, int heightOfOriginalImage,
			int toWidth, int toHeight, File outputFile, boolean imgAllowEmptyRegion, String format) throws IOException {
		Builder<? extends InputStream> builder = Thumbnails.of(originalImage);

		createImage(builder, xOfOriginalImage, yOfOriginalImage, widthOfOriginalImage, 
				heightOfOriginalImage, toWidth, toHeight, outputFile, imgAllowEmptyRegion, format);
	}

	protected static void createImage(Builder<?> builder, 
			int xOfOriginalImage, int yOfOriginalImage, int widthOfOriginalImage, int heightOfOriginalImage,
			int toWidth, int toHeight, File outputFile, boolean imgAllowEmptyRegion, String format) throws IOException {
		
		int toW, toH;
		
		if(toWidth == 0 || toHeight == 0) {
			toW = Math.abs(toWidth);
			toH = Math.abs(toHeight);
		} else {
			if(toWidth < 0 || toHeight < 0) {
				//keep the orignal aspect ratio
				if(toWidth < 0 && toHeight < 0) {
					//make the longer original edge length to toSize
					if(widthOfOriginalImage > heightOfOriginalImage) {
						toW = Math.abs(toWidth);
						toH = 0;
					} else {
						toH = Math.abs(toHeight);
						toW = 0;
					}
				} else {
					//make the shorter original edge length to toSize
					if(widthOfOriginalImage < heightOfOriginalImage) {
						toW = Math.abs(toWidth);
						toH = 0;
					} else {
						toH = Math.abs(toHeight);
						toW = 0;
					}
				}
			} else {
				//all > 0
				toW = toWidth;
				toH = toHeight;
			}
		}

		if(toW == 0 && toH == 0) {
			builder.sourceRegion(
					xOfOriginalImage, yOfOriginalImage, 
					widthOfOriginalImage, heightOfOriginalImage).size(widthOfOriginalImage, heightOfOriginalImage)
					.outputFormat(format).toFile(outputFile);
		} else {
			if (toW == 0 && toH != 0) {
				int originalW = widthOfOriginalImage;
				int originalH = heightOfOriginalImage;
				toW = toH * originalW / originalH;
				if(toW == 0) {
					toW = 1;
				}
			} else if (toW != 0 && toH == 0) {
				int originalW = widthOfOriginalImage;
				int originalH = heightOfOriginalImage;
				toH = toW * originalH / originalW;
				if(toH == 0) {
					toH = 1;
				}
			}
			
			if((toWidth < 0 || toHeight < 0) && !imgAllowEmptyRegion) {
				if(widthOfOriginalImage >= Math.abs(toWidth) && heightOfOriginalImage >= Math.abs(toHeight)) {
					if(toW == Math.abs(toWidth) && toH < Math.abs(toHeight)) {
						toW = toW * Math.abs(toHeight) / toH;
						toH = Math.abs(toHeight);
					} else if (toH == Math.abs(toHeight) && toW < Math.abs(toWidth)) {
						toH = toH * Math.abs(toWidth) / toW;
						toW = Math.abs(toWidth);
					}
				}
			}

			builder.sourceRegion(
					xOfOriginalImage, yOfOriginalImage, 
					widthOfOriginalImage, heightOfOriginalImage).size(toW, toH)
					.outputFormat(format).toFile(outputFile);
		} 
	}
	
	private static void copyFile(File srcFile, File destFile) throws IOException {
		int bufferLen = 1024;
		byte[] tempBuffer = new byte[bufferLen];
		
		int readCnt = 0;
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			
			while(true) {
				readCnt = fis.read(tempBuffer, 0, bufferLen);
				
				if(readCnt < 0) {
					break;
				}
				
				if(readCnt != 0) {
					fos.write(tempBuffer, 0, readCnt);
					fos.flush();
				}
			}
		} finally {
			try {
				fis.close();
			} catch(Exception e) {
			}
			try {
				fos.close();
			} catch(Exception e) {
			}
		}
	}
	
	
}
