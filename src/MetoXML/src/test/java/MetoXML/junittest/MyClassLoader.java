package MetoXML.junittest;

public class MyClassLoader extends ClassLoader {
	public MyClassLoader() {
		super();
	}

	public MyClassLoader(ClassLoader loader) {
		super(loader);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return super.loadClass(name);
		} catch (ClassNotFoundException e) {
			Package[] paks = getPackages();
			Class cls = null;
			for(int i = 0; i < paks.length; i++) {
				try {
					cls = super.loadClass(paks[i].getName() + "." + name);
				} catch (ClassNotFoundException e2) {
				}
				if(cls != null) {
					return cls;
				}
			}
			
			throw e;
		}
	}
}
