package MetoXML.Cast;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import MetoXML.Util.Base64Decoder;
import MetoXML.Util.Base64Encoder;
import MetoXML.Util.Base64FormatException;
import MetoXML.Util.CachedPropertyDescriptor;

public class BaseTypesMapping {
    //public final static String DateTimeFormat = "yyyy-MM-ddTHH:mm:ss.SSS00ZZ:ZZ";

    private static SimpleDateFormat dateFormatForParse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    private static SimpleDateFormat dateFormatYMDPart = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dateFormatHMSPart = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat dateFormatMSPart = new SimpleDateFormat("SSS");
    private static SimpleDateFormat dateFormatTimeZonePart = new SimpleDateFormat("Z");

    private static Class<?>[] _baseTypes = new Class<?>[] {
    	boolean.class,
    	byte.class, 
        byte.class, 
        short.class,
        short.class,
        int.class,
        int.class,
        long.class,
        long.class,
        float.class,
        double.class,
        char.class,
        String.class,
        String.class,
        java.util.Date.class,
        BigDecimal.class,
        java.sql.Date.class,
        java.sql.Timestamp.class
        };
    
    private static Class<?>[] _baseClassTypes = new Class<?>[] {
    	Boolean.class,
    	Byte.class, 
        Byte.class, 
        Short.class,
        Short.class,
        Integer.class,
        Integer.class,
        Long.class,
        Long.class,
        Float.class,
        Double.class,
        Character.class,
        String.class,
        String.class,
        java.util.Date.class,
        BigDecimal.class,
        java.sql.Date.class,
        java.sql.Timestamp.class
        };
    

    private static String[] _baseTypeNames = new String[] {
    	"bool",
        "byte", 
        "sbyte",
        "short",
        "ushort",
        "int",
        "uint",
        "long",
        "ulong",
        "float",
        "double",
        "char",
        "String",
        "String",
        "DateTime",
        "decimal",
        "DateTime",
        "DateTime"
        };

    private static String[] _baseTypeDisplayNames = new String[] {
    	"bool",
        "byte", 
        "byte",
        "short",
        "short",
        "int",
        "int",
        "long",
        "long",
        "float",
        "double",
        "char",
        "String",
        "String",
        "DateTime",
        "decimal",
        "DateTime",
        "DateTime"
        };

    private static HashMap<String, String> _typeToNameMapping = new HashMap<String, String>();

    private static HashMap<String, Class<?>> _nameToTypeMapping = new HashMap<String, Class<?>>();

    private static HashMap<String, String> _typeToDisplayNameMapping = new HashMap<String, String>();

    static 
    {
        for (int i = 0; i < _baseTypes.length; i++)
        {
            _typeToNameMapping.put(_baseTypes[i].getName(), _baseTypeNames[i]);
        }
        for (int i = 0; i < _baseClassTypes.length; i++)
        {
            _typeToNameMapping.put(_baseClassTypes[i].getName(), _baseTypeNames[i]);
        }

        for (int i = 0; i < _baseTypes.length; i++)
        {
            _nameToTypeMapping.put(_baseTypeNames[i], _baseTypes[i]);
        }

        for (int i = 0; i < _baseTypes.length; i++)
        {
            _typeToDisplayNameMapping.put(_baseTypes[i].getName(), _baseTypeDisplayNames[i]);
        }
        for (int i = 0; i < _baseClassTypes.length; i++)
        {
        	_typeToDisplayNameMapping.put(_baseClassTypes[i].getName(), _baseTypeDisplayNames[i]);
        }
    }

    /*U
    public static boolean IsSupportedFieldType(Class t)
    {
        return _typeToNameMapping.containsKey(t.getName());
    }
    */

    public static String GetSupportedFieldTypeDisplayName(Class<?> t)
    {
        return (String)_typeToDisplayNameMapping.get(t.getName());
    }

    public static Class<?> GetSupportedTypeByDisplayName(String name) {
    	return _nameToTypeMapping.get(name);
    }
    
    public static boolean IsSupportedBaseType(Class<?> t)
    {
        return _typeToNameMapping.containsKey(t.getName());
    	/*
        if (t == boolean.class) {
    		return true;
    	} else if (t == byte.class) {
    		return true;
    	} else if (t == short.class) {
    		return true;
    	} else if (t == int.class) {
    		return true;
    	} else if (t == long.class) {
    		return true;
    	} else if (t == float.class) {
    		return true;
    	} else if (t == double.class) {
    		return true;
    	} else if (t == char.class) {
    		return true;
    	} else if (t == String.class) {
    		return true;
    	} else if (t == java.util.Date.class) {
    		return true;
    	} else if (t == BigDecimal.class) {
    		return true;
    	} else if (t == java.sql.Date.class) {
    		return true;
    	} else if (t == java.sql.Timestamp.class) {
    		return true;
    	} else if (t == Boolean.class) {
    		return true;
    	} else if (t == Byte.class) {
    		return true;
    	} else if (t == Short.class) {
    		return true;
    	} else if (t == Integer.class) {
    		return true;
    	} else if (t == Long.class) {
    		return true;
    	} else if (t == Float.class) {
    		return true;
    	} else if (t == Double.class) {
    		return true;
    	} else if (t == Character.class) {
    		return true;
    	} else {
    		return false;
    	}
        */
     }

    public static String ConvertBaseTypeValueToStr(Class<?> valueType, Object value)
    {
        if (value.getClass().getName().equals(char.class.getName()) ) {
            return Short.toString((short)(((Character)value).charValue()));
        }
        else if (value.getClass().getName().equals(Character.class.getName())) {
            return Short.toString((short)(((Character)value).charValue()));
        }
        else if (value.getClass().getName().equals(float.class.getName()))
        {
            return ((Float)value).toString();
        }
        else if (value.getClass().getName().equals(Date.class.getName()))
        {
            return BaseTypesMapping.ConvertDateToStr((Date)value);
        }
        else if (value.getClass().getName().equals(java.sql.Date.class.getName()))
        {
            return BaseTypesMapping.ConvertSqlDateToStr((java.sql.Date)value);
        }
        else if (value.getClass().getName().equals(java.sql.Timestamp.class.getName()))
        {
            return BaseTypesMapping.ConvertTimeStampToStr((java.sql.Timestamp)value);
        }
        else
        {
            return value.toString();
        }
    }
    
    protected static String ConvertDateToStr(Date date) {
    	String timeZone = dateFormatTimeZonePart.format(date);
    	timeZone = timeZone.substring(0, 3) + ":" + timeZone.substring(3);
    	
    	return dateFormatYMDPart.format(date) + "T" 
    		+ dateFormatHMSPart.format(date) + "." 
    		+ dateFormatMSPart.format(date) + "00"
    		+ timeZone;
    }

    public static String ConvertSqlDateToStr(java.sql.Date sqlDate) {
    	Date date = new Date(sqlDate.getTime());
    	
    	return ConvertDateToStr(date);
    }
    
    public static String ConvertTimeStampToStr(java.sql.Timestamp timeStamp) {
    	Date date = new Date(timeStamp.getTime());
    	
    	return ConvertDateToStr(date);
    }

    public static Date ConvertStrToDate(String dateStr) throws ParseException {
    	String str = dateStr;
    	
    	str = dateStr.replace('T', ' ');
    	int index = str.indexOf('.');

    	str = str.substring(0, index + 3 + 1) 
    		+ str.substring(index + 3 + 3, index + 3 + 3 + 3) 
    		+ str.substring(index + 3 + 3 + 4);
    	
    	return dateFormatForParse.parse(str);
    }

    public static java.sql.Date ConvertStrToSqlDate(String dateStr) throws ParseException {
    	Date date = ConvertStrToDate(dateStr);
    	java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    	
    	return sqlDate;
    }

    public static java.sql.Timestamp ConvertStrToTimeStamp(String dateStr) throws ParseException {
    	Date date = ConvertStrToDate(dateStr);
    	java.sql.Timestamp timeStamp = new java.sql.Timestamp(date.getTime());
    	
    	return timeStamp;
    }

    public static byte[] DecodeBase64(String base64String) 
    	throws UnsupportedEncodingException, IOException, Base64FormatException {
    	byte[] bytesInput = base64String.getBytes("ISO-8859-1");
	    ByteArrayInputStream inStream = new ByteArrayInputStream(bytesInput);
	    
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    Base64Decoder base64decoder = new Base64Decoder(inStream, outStream);
    	base64decoder.process();
    	
    	return outStream.toByteArray();
    }

    public static String EncodeBase64(byte[] bytes) throws IOException {
    	return EncodeBase64(new ByteArrayInputStream(bytes));
    }
    
    public static String EncodeBase64(ByteArrayInputStream byteInputStream) throws IOException {
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    Base64Encoder base64Encoder = new Base64Encoder(byteInputStream, outStream);
    	base64Encoder.process();
    	
    	return outStream.toString();
    } 
    
    public static Object Convert(Type type, String valueStr) throws IllegalAccessException, ParseException {
		Class<?> cls = (Class<?>) type;
		
		if(cls.getName().equals(boolean.class.getName())) {
			return Boolean.valueOf(valueStr);
		} else if(cls.getName().equals(byte.class.getName())) {
	    	return Byte.valueOf(valueStr);
    	} else if(cls.getName().equals(short.class.getName())) {
    		return Short.valueOf(valueStr);
		} else if(cls.getName().equals(int.class.getName())) {
			return Integer.valueOf(valueStr);
		} else if(cls.getName().equals(long.class.getName())) {
			return Long.valueOf(valueStr);
		} else if(cls.getName().equals(float.class.getName())) {
			return Float.valueOf(valueStr);
		} else if(cls.getName().equals(double.class.getName())) {
			return Double.valueOf(valueStr);
		} else if(cls.getName().equals(char.class.getName())) {
			return (char)Integer.parseInt(valueStr);
		} else if(cls.getName().equals(Boolean.class.getName())) {
			return Boolean.valueOf(valueStr);
		} else if(cls.getName().equals(Byte.class.getName())) {
	    	return Byte.valueOf(valueStr);
    	} else if(cls.getName().equals(Short.class.getName())) {
    		return Short.valueOf(valueStr);
		} else if(cls.getName().equals(Integer.class.getName())) {
			return Integer.valueOf(valueStr);
		} else if(cls.getName().equals(Long.class.getName())) {
			return Long.valueOf(valueStr);
		} else if(cls.getName().equals(Float.class.getName())) {
			return Float.valueOf(valueStr);
		} else if(cls.getName().equals(Double.class.getName())) {
			return Double.valueOf(valueStr);
		} else if(cls.getName().equals(Character.class.getName())) {
			return (char)Integer.parseInt(valueStr);
		} else if(cls.getName().equals(Date.class.getName())) {
			return ConvertStrToDate(valueStr);
		} else if(cls.getName().equals(java.sql.Date.class.getName())) {
			return ConvertStrToSqlDate(valueStr);
		} else if(cls.getName().equals(java.sql.Timestamp.class.getName())) {
			return ConvertStrToTimeStamp(valueStr);
		} else if(cls.getName().equals(BigDecimal.class.getName())) {
			return new BigDecimal(valueStr);
		} else {
			return cls.cast(valueStr);
		}
	}

    public static void SetPropertyValueOfPrimitiveType(Object obj, PropertyDescriptor property, String valueStr) 
    	throws IllegalAccessException, InvocationTargetException, ParseException {
    	Type type = property.getPropertyType();
    	//Class cls = (Class) type;
    	
    	property.getWriteMethod().invoke(obj, Convert(type, valueStr));
    }


    public static void SetPropertyValueOfPrimitiveType(Object obj, CachedPropertyDescriptor property, String valueStr)
            throws IllegalAccessException, InvocationTargetException, ParseException {
        Type type = property._propertyType;
        //Class cls = (Class) type;

        property._writeMethod.invoke(obj, Convert(type, valueStr));
    }

}
