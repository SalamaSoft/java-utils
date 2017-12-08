package MetoXML;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.concurrent.ConcurrentHashMap;

import MetoXML.Util.TreeDeepPriorVisitor;

public abstract class AbstractReflectInfoCachedSerializer extends TreeDeepPriorVisitor {
	private static ConcurrentHashMap<String, PropertyDescriptor[]> _propertyDescriptorArrayCache = 
			new ConcurrentHashMap<String, PropertyDescriptor[]>();
	
	private static ConcurrentHashMap<String, PropertyDescriptor> _propertyDescriptorCache =
			new ConcurrentHashMap<String, PropertyDescriptor>();
	
    protected static PropertyDescriptor[] findPropertyDescriptorArray(Class<?> dataClass) 
    		throws IntrospectionException {
    	String propertyArrayKey = dataClass.getName();
    	PropertyDescriptor[] descs = _propertyDescriptorArrayCache.get(propertyArrayKey);
    	
    	if(descs == null) {
        	descs = Introspector.getBeanInfo(dataClass).getPropertyDescriptors();
    		_propertyDescriptorArrayCache.put(dataClass.getName(), descs);
    	}
    	
    	return descs;
    }
    
	/*
    private static PropertyDescriptor[] createPropertyDescriptors(Class<?> dataClass) throws IntrospectionException {
    	PropertyDescriptor[] descs = Introspector.getBeanInfo(dataClass).getPropertyDescriptors();
		_propertyDescriptorArrayCache.put(dataClass.getName(), descs);
		
		String dataClassName = dataClass.getName();
		for(int i = 0; i < descs.length; i++) {
			_propertyDescriptorCache.put(dataClassName + "." + descs[i].getName(), descs[i]);
		}
		
		return descs;
    }
	*/
    /*
    protected static HashMap<String,PropertyDescriptor> findPropertyDescriptorMap(
    		Class<?> dataClass) 
    		throws IntrospectionException {
    	String propertyArrayKey = dataClass.getName();
    	HashMap<String,PropertyDescriptor> map = _propertyDescriptorMapCache.get(propertyArrayKey);
    	
    	if(map == null) {
    		PropertyDescriptor[] descs = Introspector.getBeanInfo(dataClass).getPropertyDescriptors();
    		_propertyDescriptorArrayCache.put(propertyArrayKey, descs);
    		
    		map = new HashMap<String, PropertyDescriptor>();
    		for(int i = 0; i < descs.length; i++) {
    			map.put(descs[i].getName(), descs[i]);
    		}
    		_propertyDescriptorMapCache.put(propertyArrayKey, map);
    	}
    	
    	return map;
    }
    */

    protected static PropertyDescriptor findPropertyDescriptor(
    		String propertyName, Class<?> dataClass) 
    		throws IntrospectionException {
    	String propertyKey = dataClass.getName().concat(".").concat(propertyName);
    	PropertyDescriptor desc = _propertyDescriptorCache.get(propertyKey);
    	
    	if(desc == null) {
    		desc = new PropertyDescriptor(propertyName, dataClass);
    		_propertyDescriptorCache.put(propertyKey, desc);
    	}
    	
    	return desc;
    }
}
