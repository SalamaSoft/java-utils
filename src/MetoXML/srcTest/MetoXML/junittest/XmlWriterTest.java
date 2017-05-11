package MetoXML.junittest;

import static org.junit.Assert.*;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Properties;

import org.junit.Test;

import MetoXML.XmlReader;
import MetoXML.XmlWriter;
import MetoXML.Base.XmlDocument;
import MetoXML.Base.XmlParseException;
import MetoXML.junittest.data.TestData;
import MetoXML.junittest.data.TestEnumData.ActivityType;

public class XmlWriterTest {

	@Test
	public void testWriteXmlDocumentXmlDocumentString() {
		try {
			TestData data = new TestData();
			data.setId(234);
			
			int k = 234;
			
			String s = ((Object)k).toString();
			s = ((Object)k).getClass().getName();
			
			
			String DateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSSZ";
			SimpleDateFormat simpDF = new SimpleDateFormat(DateTimeFormat);
			String dateStr = simpDF.format(new Date());
			

			String[] propNames = new String[]{"id", "car", "bar", "iar", "iarar", "listD", "date", "tData", "t2DataAr"};
			
			for(int i = 0; i < propNames.length; i++) {
				String propName = propNames[i];
				PropertyDescriptor property = new PropertyDescriptor(propName, TestData.class);
				Type t = property.getPropertyType();
				//String tN = t.toString();
				String tN = ((Class)t).getName();
				
				System.out.println(propName + ":" + tN);
			}
			
			
			XmlReader reader = new XmlReader();
			XmlDocument xmlDoc = reader.ReadXml(new File("testData", "testData.xml").getAbsolutePath());
			
			XmlWriter writer = new XmlWriter();
			writer.WriteXmlDocument(xmlDoc, new File("testData", "testDataOutput.xml").getAbsolutePath());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (XmlParseException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		} catch (IntrospectionException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		}
	}

}
