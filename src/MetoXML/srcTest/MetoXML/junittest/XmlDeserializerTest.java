package MetoXML.junittest;

import static org.junit.Assert.*;

import java.beans.DesignMode;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import MetoXML.Base.XmlDocument;
import MetoXML.Base.XmlParseException;
import MetoXML.Cast.BaseTypesMapping;
import MetoXML.Util.DataClassFinder;
import MetoXML.junittest.data.Test2Data;
import MetoXML.junittest.data.Test3Data;
import MetoXML.junittest.data.TestData;
import MetoXML.junittest.generic.PageControl;
import MetoXML.junittest.generic.SearchPageResult;
import MetoXML.junittest.test4.DaTiAnswerData;

public class XmlDeserializerTest {

	private static File makeTestFile(String fileName) {
		return new File("testData", fileName);
	}

	@Test
	public void test1() {
		File inputFile = makeTestFile("testDataoutput.xml");
		File outputFile = makeTestFile("testDataoutputJ.xml");
		
		testDeserialize(inputFile, outputFile, TestData.class);
	}

	@Test
	public void test4() {
		File inputFile = makeTestFile("testDataoutput4.xml");
		File outputFile = makeTestFile("testDataoutput4J.xml");

		testDeserialize(inputFile, outputFile, String.class);
	}

	@Test
	public void test5() {
		File inputFile = makeTestFile("testDataoutput5.xml");
		File outputFile = makeTestFile("testDataoutput5J.xml");

		testDeserialize(inputFile, outputFile, String[].class);
	}

	@Test
	public void test6() {
		File inputFile = makeTestFile("testDataoutput6.xml");
		File outputFile = makeTestFile("testDataoutput6J.xml");

		testDeserialize(inputFile, outputFile, Test2Data[].class);
	}

	@Test
	public void test7() {
		File inputFile = makeTestFile("testDataoutput7.xml");
		File outputFile = makeTestFile("testDataoutput7J.xml");

		List<String> a = new ArrayList<String>();
		String listClassName = (new ArrayList<String>()).getClass().getName();
		System.out.println("listClassName:" + listClassName);
		testDeserialize(inputFile, outputFile, (new ArrayList<String>()).getClass());
	}

	@Test
	public void test8() {
		File inputFile = makeTestFile("testDataoutput8.xml");
		File outputFile = makeTestFile("testDataoutput8J.xml");

		Test3Data data = new Test3Data();
		data.setL1(1313L);
		data.setSqlDate(new java.sql.Date((new Date()).getTime()));
		data.setTimeStamp(new Timestamp((new Date()).getTime()));

		serialize(inputFile, data, Test3Data.class);

		Test3Data data2 = (Test3Data) deserialize(inputFile, Test3Data.class);

		serialize(outputFile, data2, Test3Data.class);
	}

	@Test
	public void test9() {
		try {
			/*
			 * PropertyDescriptor propDesc = new PropertyDescriptor("bar",
			 * TestData.class); propDesc = new PropertyDescriptor("Bar",
			 * TestData.class); propDesc = new PropertyDescriptor("BAR",
			 * TestData.class);
			 */
			Method method = TestEnumReflect.class.getMethod("test1", new Class[] { int.class, String.class });
			method.invoke(null, new Object[] { 1333, "1231" });

			Method method2 = TestEnumReflect.class.getMethod("test1", new Class[0]);

			System.out.println("TestEnumReflect.class.tostring:" + TestEnumReflect.class.toString());
			System.out.println("TestEnumReflect.class.getName:" + TestEnumReflect.class.getName());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test10() {
		File inputFile = makeTestFile("testDataoutput10.xml");

		File outputFile = makeTestFile("testDataoutput10J.xml");

		String s = "testStringType";

		serialize(inputFile, s, String.class);

		String s2 = (String) deserialize(inputFile, String.class);

		serialize(outputFile, s2, String.class);
	}

	@Test
	public void Test11() {
		try {
			String xmlStr = "<String>a</String>";

			String s = (String) XmlDeserializer.stringToObject(xmlStr, java.lang.String.class);

			String xmlStr2 = XmlSerializer.objectToString(s, String.class);

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test12() {

		File inputFile = makeTestFile("testDataoutput12.xml");

		File outputFile = makeTestFile("testDataoutput12J.xml");

		SearchPageResult<Test2Data> searchResult = new SearchPageResult<Test2Data>();
		Test2Data data2 = null;
		for (int i = 0; i < 3; i++) {
			data2 = new Test2Data();
			searchResult.getDataList().add(data2);
		}

		serialize(outputFile, searchResult, SearchPageResult.class);

		SearchPageResult<Test2Data> result = (SearchPageResult<Test2Data>) deserialize(outputFile,
				SearchPageResult.class);
		System.out.println("result.getDataList().size():" + result.getDataList().size());
	}

	public static void main(String[] args) {
		BaseTypesMapping.IsSupportedBaseType(byte.class);
		testData4MultiThreads();
		// for(int i = 0; i < 100; i++) {
		// testData4();
		// }
	}

	public static void testData4MultiThreads() {
		// final XmlDeserializer xmlDes = new XmlDeserializer();
		final DataClassFinder dataClassFinder = new DataClassFinder(DaTiAnswerData.class.getPackage());
		for (int k = 0; k < 10; k++) {
			for (int i = 0; i < 10; i++) {
				Thread t = new Thread(new Runnable() {
					public void run() {
						// XmlDeserializerTest.testData4();

						File inputFile = makeTestFile("testDataOutput4.xml");

						long beginTime = System.currentTimeMillis();

						try {
							XmlDeserializer xmlDes = new XmlDeserializer();
							DaTiAnswerData data = (DaTiAnswerData) xmlDes.Deserialize(inputFile.getAbsolutePath(),
									DaTiAnswerData.class, XmlDeserializer.DefaultCharset, dataClassFinder);
							System.out.println("testData4MultiThreads() time usage:"
									+ Long.toString(System.currentTimeMillis() - beginTime));
						} catch (XmlParseException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				});
				t.start();
			}
		}

		System.out.println("testData4MultiThreads() end");
	}

	public static void testData4() {
		File inputFile = makeTestFile("testData4.xml");
		long beginTime = System.currentTimeMillis();

		XmlDeserializer xmlDes = new XmlDeserializer();
		DataClassFinder dataClassFinder = new DataClassFinder(DaTiAnswerData.class.getPackage());

		try {
			DaTiAnswerData data = (DaTiAnswerData) xmlDes.Deserialize(inputFile.getAbsolutePath(), DaTiAnswerData.class,
					XmlDeserializer.DefaultCharset, dataClassFinder);
			System.out.println("testData4() time usage:" + Long.toString(System.currentTimeMillis() - beginTime));
		} catch (XmlParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private void testDeserialize(File inputFile, File outputFile, Class type) {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		Object data = null;

		FileOutputStream fos = null;
		OutputStreamWriter writer = null;

		try {
			fis = new FileInputStream(inputFile);
			reader = new InputStreamReader(fis, "UTF-8");

			XmlDeserializer xmlDes = new XmlDeserializer();
			data = xmlDes.Deserialize(reader, type);

			fos = new FileOutputStream(outputFile);
			writer = new OutputStreamWriter(fos, "UTF-8");
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(writer, data, type);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (XmlParseException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (InstantiationException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private Object deserialize(File file, Class type) {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		Object data = null;

		try {
			fis = new FileInputStream(file);
			reader = new InputStreamReader(fis, "UTF-8");

			XmlDeserializer xmlDes = new XmlDeserializer();
			data = xmlDes.Deserialize(reader, type);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (XmlParseException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (InstantiationException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return data;
	}

	private void serialize(File file, Object data, Class type) {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;

		try {
			fos = new FileOutputStream(file);
			writer = new OutputStreamWriter(fos, "UTF-8");
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(writer, data, type);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
