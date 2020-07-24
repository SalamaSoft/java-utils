package MetoXML;

import MetoXML.Util.CachedPropertyDescriptor;
import MetoXML.Util.TreeDeepPriorVisitor;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractReflectInfoCachedSerializer2 extends TreeDeepPriorVisitor {
	private static ConcurrentHashMap<String, CachedPropertyDescriptor[]> _propertyDescriptorArrayCache =
			new ConcurrentHashMap<>();
	
	private static ConcurrentHashMap<String, CachedPropertyDescriptor> _propertyDescriptorCache =
			new ConcurrentHashMap<>();
	
    protected static CachedPropertyDescriptor[] findPropertyDescriptorArray(Class<?> dataClass)
    		throws IntrospectionException {
    	return _propertyDescriptorArrayCache.computeIfAbsent(
				dataClass.getName(),
				(key) -> {
					try {
						PropertyDescriptor[] pArray = Introspector.getBeanInfo(dataClass).getPropertyDescriptors();
						CachedPropertyDescriptor[] cpArray = new CachedPropertyDescriptor[pArray.length];
						final int size = cpArray.length;
						for(int i = 0; i < size; i++) {
							PropertyDescriptor p = pArray[i];
							cpArray[i] = createCachedPropertyDescriptor(p);
						}

						return cpArray;
					} catch (IntrospectionException e) {
						throw new RuntimeException(e);
					}
				}
		);
    }
    
    protected static CachedPropertyDescriptor findPropertyDescriptor(
    		String propertyName, Class<?> dataClass) 
    		throws IntrospectionException {
    	return _propertyDescriptorCache.computeIfAbsent(
				dataClass.getName().concat(".").concat(propertyName),
				(key) -> {
					try {
						PropertyDescriptor p = new PropertyDescriptor(propertyName, dataClass);
						return createCachedPropertyDescriptor(p);
					} catch (IntrospectionException e) {
						throw new RuntimeException(e);
					}
				}
		);
    }

    private static CachedPropertyDescriptor createCachedPropertyDescriptor(PropertyDescriptor p) {
		CachedPropertyDescriptor cp = new CachedPropertyDescriptor();
		cp._name = p.getName();
		cp._displayName = p.getDisplayName();
		cp._propertyType = p.getPropertyType();
		cp._readMethod = p.getReadMethod();
		cp._writeMethod = p.getWriteMethod();

		return cp;
	}
}
