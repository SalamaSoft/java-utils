package MetoXML.Util;

import java.lang.reflect.Method;

/**
 * @author XingGu_Liu on 2020/7/24.
 */
public class CachedPropertyDescriptor {

    public String _name;
    public String _displayName;
    public Class<?> _propertyType;
    public Method _readMethod = null;
    public Method _writeMethod = null;

}
