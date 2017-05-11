package MetoXML.Util;

import java.util.List;

public class DataClassFinder extends ClassLoader implements ClassFinder {
    private Package _defaultPackage = null;

    public DataClassFinder() {
    	super(getDefaultClassLoader());
	}

	public DataClassFinder(Package defaultPackage) {
		super(getDefaultClassLoader());
		_defaultPackage = defaultPackage;
	}

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader classLoader = null;

		try {
			classLoader = Thread.currentThread().getContextClassLoader();
		} catch (Throwable e) {
		}
		
		if (classLoader == null) {
			classLoader = DataClassFinder.class.getClassLoader();
		}
		
		return classLoader;
	}
	
	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
    	//At first find class from the data object package
		Class<?> cls = null;
		
		try {
			if(className.indexOf(".") <= 0 && _defaultPackage != null) {
	        	cls = getParent().loadClass(this._defaultPackage.getName() + "." + className);
	    		if(cls != null) {
	    			return cls;
	    		}
			} else {
				cls = getParent().loadClass(className);
			}
			
			return cls;
		} catch(ClassNotFoundException ex) {
			Package[] paks = getPackages();
			for(int i = 0; i < paks.length; i++) {
				try {
					cls = getParent().loadClass(paks[i].getName() + "." + className);
					if(cls != null) {
						return cls;
					}
				} catch(ClassNotFoundException ex2) {
				}
			}
			
			return null;
		}
	}
	
    public static boolean IsList(Class<?> cls) {
    	return IsInterfaceType(cls, List.class);
    }
    
    public static boolean IsInterfaceType(Class<?> cls, Class<?> interfaceClass) {
    	if(cls.isInterface() && cls.getName().equals(interfaceClass.getName())) {
    		return true;
    	} else {
        	Class<?>[] interfaces = cls.getInterfaces();
        	for(int i = 0; i < interfaces.length; i++) {
        		if(interfaces[i].getName().equals(interfaceClass.getName())) {
        			return true;
        		}
        	}
        	
        	return false;
    	}
    }
	
}
