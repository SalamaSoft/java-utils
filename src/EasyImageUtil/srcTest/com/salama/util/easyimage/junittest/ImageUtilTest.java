package com.salama.util.easyimage.junittest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.salama.util.easyimage.ImageUtil;

public class ImageUtilTest {

	@Ignore
	public void testCreatePngImage() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1.PNG"), 
					50, 100, 
					400, 500, 
					200, 300, 
					new File("testImages/Test1_out.PNG"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore
	public void testCreatePngImage2() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1"), 
					50, 100, 
					400, 500, 
					200, 300, 
					new File("testImages/Test1_out2.PNG"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void testCreatePngImage3() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1.jpg"), 
					50, 100, 
					400, 500, 
					200, 300, 
					new File("testImages/Test1_out3.PNG"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void testCreatePngImage4() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1.PNG"), 
					50, 100, 
					400, 500, 
					200, 300, 
					new File("testImages/Test1_out.jpg"),
					ImageUtil.IMAGE_FORMAT_JPEG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void testCreatePngImage5() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1.PNG"), 
					50, 100, 
					400, 500, 
					200, 300, 
					new File("testImages/Test1_out5.gif"),
					ImageUtil.IMAGE_FORMAT_GIF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore
	public void testCreatePngImage6() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1.PNG"), 
					50, 100, 
					400, 500, 
					200, 300, 
					new File("testImages/Test1_out.gif"),
					ImageUtil.IMAGE_FORMAT_GIF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void testCreatePngImage7() {
		try {
			ImageUtil.createImage(
					new File("testImages/Test1.PNG"), 
					200, 0, 
					new File("testImages/Test1_out7.gif"),
					ImageUtil.IMAGE_FORMAT_GIF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void testCreatePngImage8() {
		try {
			ImageUtil.createImage(
					new File("testImages/ff7d"), 
					200, 0, 
					new File("testImages/Test1_out8.png"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore
	public void testCreatePngImage9() {
		try {
			ImageUtil.createImage(
					new File("testImages/ff7d"), 
					-200, 200, 
					new File("testImages/Test1_out9.png"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore
	public void testCreatePngImage10() {
		try {
			ImageUtil.createImage(
					new File("testImages/ff7d"), 
					-200, -200, 
					new File("testImages/Test1_out10.png"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreatePngImage11() {
		try {
			ImageUtil.createImage(
					new File("testImages/ff74.png"), 
					-200, -200, 
					new File("testImages/Test1_out11.png"),
					ImageUtil.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Ignore
	public void testConvertImageToPNG() {
		File imageFile = new File("testImages/Test2");
		
		FileInputStream fis1;
		FileInputStream fis2;
		try {
			fis1 = new FileInputStream(imageFile);
			fis2 = new FileInputStream(imageFile);
			
			byte[] tempBuffer1 = new byte[24];
			byte[] tempBuffer2 = new byte[24];
			
			fis1.read(tempBuffer1);
			fis2.read(tempBuffer2);
			
			System.out.println("file read concurrently");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			ImageUtil.convertImageToPNG(
					imageFile, 
					imageFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
