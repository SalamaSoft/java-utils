package MetoXML.junittest;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;

import MetoXML.junittest.data.TestEnumData;

public class TestEnumReflect {

	public static void main()
	{
		try {
			PropertyDescriptor property = new PropertyDescriptor("type", TestEnumData.class);
			Type t = property.getPropertyType();
			//String tN = t.toString();
			String tN = ((Class)t).getName();
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void test1(int f1, String f2) {
		System.out.println("f1:" + f1 + ";f2:" + f2);
	}
	
	public static void test1() {
		System.out.println("No parameter");
	}
}
