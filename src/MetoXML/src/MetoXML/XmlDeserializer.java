package MetoXML;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MetoXML.Base.XmlDocument;
import MetoXML.Base.XmlNode;
import MetoXML.Base.XmlNodeAttribute;
import MetoXML.Base.XmlParseException;
import MetoXML.Cast.BaseTypesMapping;
import MetoXML.Util.Base64FormatException;
import MetoXML.Util.ClassFinder;
import MetoXML.Util.DataClassFinder;
import MetoXML.Util.ITreeNode;

public class XmlDeserializer extends AbstractReflectInfoCachedSerializer {
	static {
		System.out.println("XmlDeserializer v2.0.1. lastModified:20170513");
	}
	
	public static final Charset DefaultCharset = Charset.forName("UTF-8");
	
	//cache the propertyDescriptor
	//private static HashMap<String, PropertyDescriptor> _propertyDescriptorCache = 
	//		new HashMap<String, PropertyDescriptor>();
	
	//private CultureInfo _cultureInfo = null;
    private List<NodeInfoData> _nodeInfoStack = new ArrayList<NodeInfoData>();
    private ClassFinder _classFinder = null;  
    //private Class _typeOfObject = null;

    private NodeInfoData rootNodeInfo = null;

    private XmlNode parentOfRootNode = null;
    private XmlNode prevOfRootNode = null;
    private XmlNode nextOfRootNode = null;
    
    public XmlDeserializer()
    {
    	//_cultureInfo = CultureInfo.CurrentCulture;
    }

    
    public Object Deserialize(String filePath, Class<?> typeOfObj, Charset charset) 
    throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
    	return Deserialize(filePath, typeOfObj, charset, null); 
    }
    
    public Object Deserialize(String filePath, Class<?> typeOfObj, Charset charset, ClassFinder classFinder) 
    throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		FileInputStream fs = null;
		InputStreamReader reader = null;
		
		try {
			fs = new FileInputStream(filePath);
			reader = new InputStreamReader(fs, charset);

			return Deserialize(reader, typeOfObj, classFinder);
		} finally {
			try {
				fs.close();
			} catch(Exception e) {
			}
			try {
				reader.close();
			} catch(Exception e) {
			}
		}
    }
    
    public Object Deserialize(InputStreamReader sr, Class<?> typeOfObj) 
    throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
    {
    	return Deserialize(sr, typeOfObj, null);
    }
    
    public Object Deserialize(InputStreamReader sr, Class<?> typeOfObj, ClassFinder classFinder) 
    throws XmlParseException, IOException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
    { 
        XmlReader xmlReader = new XmlReader();
        XmlDocument xmlDoc = xmlReader.ReadXml(sr);

        return ConvertXmlNodeToObject(xmlDoc.getRootNode(), typeOfObj, classFinder);
    }

	public static Object stringToObject(String xmlStr, Class<?> typeOfObj)
	throws IOException, XmlParseException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
	{
		return stringToObject(xmlStr, typeOfObj, null);
	}
	public static Object stringToObject(String xmlStr, Class<?> typeOfObj, ClassFinder classFinder)
	throws IOException, XmlParseException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
	{
		try {
			XmlReader xmlReader = new XmlReader();
			XmlNode rootNode = xmlReader.StringToXmlNode(xmlStr, DefaultCharset);
						
			XmlDeserializer xmlDes = new XmlDeserializer();
			return xmlDes.ConvertXmlNodeToObject(rootNode, typeOfObj, classFinder);
		} catch (IOException e) {
			//log.error("XMLUtil.stringToObject() error.", e);
			throw e;
		} catch (XmlParseException e) {
			//log.error("XMLUtil.stringToObject() error.", e);
			throw e;
		} catch (InvocationTargetException e) {
			//log.error("XMLUtil.stringToObject() error.", e);
			throw e;
		} catch (IllegalAccessException e) {
			//log.error("XMLUtil.stringToObject() error.", e);
			throw e;
		} catch (InstantiationException e) {
			//log.error("XMLUtil.stringToObject() error.", e);
			throw e;
		} catch (NoSuchMethodException e) {
			//log.error("XMLUtil.stringToObject() error.", e);
			throw e;
		}
	}

    public Object ConvertXmlNodeToObject(XmlNode rootNode, Class<?> typeOfObj) 
    throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
    {
    	return ConvertXmlNodeToObject(rootNode, typeOfObj, null);
    }

    public Object ConvertXmlNodeToObject(XmlNode rootNode, Class<?> typeOfObj, ClassFinder classFinder) 
    throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException
    {
    	//debugLog("ConvertXmlNodeToObject() begin");
        // Modified on 2010/10/23 start
        if (BaseTypesMapping.IsSupportedBaseType(typeOfObj))
        {
            //base type
            try {
				return ConvertStrToValue(false, typeOfObj, rootNode.getContent());
			} catch (Exception e) {
				return null;
			}
        }
        else if (typeOfObj.isArray() 
    			&& (typeOfObj.getComponentType() == byte.class) )
        {
            try {
                return ConvertStrToValue(false, typeOfObj, rootNode.getContent());
			} catch (Exception e) {
				return null;
			}
        }
        // Modified on 2010/10/23 end
    	//debugLog("ConvertXmlNodeToObject() 1 ----------------------");

        //this._assembly = new DataClassLoader(typeOfObj.getClassLoader());
        //this._assembly = new DataClassLoader(GetDataAssembly(typeOfObj));
        if(classFinder != null) {
        	this._classFinder = classFinder;
        } else {
        	this._classFinder = GetDataClassFinder(typeOfObj);
        }
        //this._typeOfObject = typeOfObj;
        
    	//debugLog("ConvertXmlNodeToObject() 2 ----------------------");

    	// Modified on 2010/10/28
        try {
            ClearRootNodeParentNode(rootNode);
        	
            //Object obj = typeOfObj.getConstructor(null).newInstance(null);
            //_nodeInfoStack.add(new NodeInfoData(obj, typeOfObj));
            PushNodeInfoStack(typeOfObj);
            rootNodeInfo = _nodeInfoStack.get(0);

        	//debugLog("ConvertXmlNodeToObject() 3 ----------------------");
            
            //init list
            if (IsList(rootNodeInfo.objType))
            {
                rootNodeInfo.obj = rootNodeInfo.objType.newInstance();
            }
            else if (IsMap(rootNodeInfo.objType)) {
            	rootNodeInfo.obj = new HashMap<String, Object>();
            }

            //debugLog("ConvertXmlNodeToObject() before VisitAllNode()");
            
            VisitAllNode(rootNode);

            //return obj;
            return rootNodeInfo.obj;
        } finally {
            RecoverRootNodeParentNode(rootNode);
            //debugLog("ConvertXmlNodeToObject() finally");
        }

    }

    protected void ForwardToNode(ITreeNode node, int depth, boolean isLeafNode)
    {
    	//debugLog("ForwardToNode() begin");
    	
        if (node.GetParent() != null)
        {
            //it is not root node
            NodeInfoData nodeInfo = _nodeInfoStack.get(_nodeInfoStack.size() - 1);
            if (IsArray(nodeInfo.objType))
            { 
            	Class<?> currentObjType = nodeInfo.objType.getComponentType();

                try {
	                if (isLeafNode)
	                {
	                    nodeInfo.valueList.add(ConvertStrToValue(true, currentObjType, ((XmlNode)node).getContent()));
	                }
	                else
	                {
						PushNodeInfoStack(currentObjType);
	                }
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				} catch (Base64FormatException e) {
					throw new RuntimeException(e);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
            }
            else if (IsList((Class<?>)nodeInfo.objType))
            { 
                try {
                	Class<?> currentObjType = GetTypeByName(((XmlNode)node).getName());

					if (isLeafNode)
					{
					    nodeInfo.valueList.add(ConvertStrToValue(true, currentObjType, ((XmlNode)node).getContent()));
					}
					else
					{
						if(currentObjType == null) {
							//to support map entry
							currentObjType = HashMap.class;
						}
						
					    PushNodeInfoStack(currentObjType);
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				} catch (Base64FormatException e) {
					throw new RuntimeException(e);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
            } else if (IsMap((Class<?>)nodeInfo.objType)) {
            	//parent is map
            	if(isLeafNode) {
            		((Map)nodeInfo.obj).put(((XmlNode)node).getName(), ((XmlNode)node).getContent());
            	} else {
            		try {
						PushNodeInfoStack(GetTypeForManEntry((XmlNode)node));
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					}
            	}
            }
            else 
            { 
                //parent is object
				try {
					//PropertyDescriptor pInf = new PropertyDescriptor(((XmlNode)node).getName(), nodeInfo.objType);
					PropertyDescriptor pInf = findPropertyDescriptor(((XmlNode)node).getName(), nodeInfo.objType);
					
					Class<?> currentObjType = pInf.getPropertyType();

					if (isLeafNode)
					{
					    //pInf.SetValue(nodeInfo.obj, ConvertStrToValue(currentObjType, ((XmlNode)node).getContent()), null);
						SetPropertyValue(nodeInfo.obj, pInf, ((XmlNode)node).getContent());
					}
					else
					{
					    PushNodeInfoStack(currentObjType);
					}
				} catch (IntrospectionException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
            }

        }
        
        //debugLog( "ForwardToNode() end");
    }

    protected void BackwardToNode(ITreeNode node, int depth)
    {
    	//debugLog("BackwardToNode() begin");
    	
        if (node.GetParent() != null)
        {
            //it is not root node

            NodeInfoData nodeInfo = _nodeInfoStack.get(_nodeInfoStack.size() - 1);
            _nodeInfoStack.remove(_nodeInfoStack.size() - 1);

            NodeInfoData parentNodeInfo = _nodeInfoStack.get(_nodeInfoStack.size() - 1);

            if (IsArray(nodeInfo.objType))
            {
                if(nodeInfo.valueList.size() > 0)
                {
                    nodeInfo.obj = Array.newInstance(nodeInfo.objType.getComponentType(), nodeInfo.valueList.size());

                    for (int i = 0; i < nodeInfo.valueList.size(); i++)
                    {
                        Array.set(nodeInfo.obj, i, nodeInfo.valueList.get(i));
                    }
                }
            }
            else if (IsList(nodeInfo.objType))
            {
                if (nodeInfo.valueList.size() > 0)
                {
                	if(nodeInfo.objType.isInterface()) {
                    	//nodeInfo.obj = new ArrayList();
                		nodeInfo.obj = nodeInfo.valueList;
                	} else {
                		try {
							nodeInfo.obj = nodeInfo.objType.newInstance();
							((List)nodeInfo.obj).addAll(nodeInfo.valueList);
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						} 
                	}

                	/*
                    for (int i = 0; i < nodeInfo.valueList.size(); i++)
                    {
                        ((List)nodeInfo.obj).add(nodeInfo.valueList.get(i));
                    }
                    */
                	
                }
            }
            else
            {
                //object
            }

            if (IsArray(parentNodeInfo.objType))
            {
                parentNodeInfo.valueList.add(nodeInfo.obj);
            }
            else if (IsList(parentNodeInfo.objType))
            {
                parentNodeInfo.valueList.add(nodeInfo.obj);
            }
            else if (IsMap(parentNodeInfo.objType)) {
            	((Map)parentNodeInfo.obj).put(((XmlNode)node).getName(), nodeInfo.obj);
            }
            else
            {
                //Parent is object
            	try {
					//PropertyDescriptor property = new PropertyDescriptor(((XmlNode)node).getName(), parentNodeInfo.objType);
            		PropertyDescriptor property = findPropertyDescriptor(((XmlNode)node).getName(), parentNodeInfo.objType);
					property.getWriteMethod().invoke(parentNodeInfo.obj, nodeInfo.obj);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IntrospectionException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
            }
        }
        else //Modified on 2010/10/28
        {
            //Root node
            if (rootNodeInfo.objType.isArray())
            {
                if (rootNodeInfo.valueList.size() > 0)
                {
                    rootNodeInfo.obj = Array.newInstance(rootNodeInfo.valueList.get(0).getClass(), rootNodeInfo.valueList.size());

                    for (int i = 0; i < rootNodeInfo.valueList.size(); i++)
                    {
                    	Array.set(rootNodeInfo.obj, i, rootNodeInfo.valueList.get(i));
                    }
                }
            }
            else if (IsList(rootNodeInfo.objType))
            {
                if (rootNodeInfo.valueList.size() > 0)
                {
                    try {
						rootNodeInfo.obj = rootNodeInfo.objType.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}

                    for (int i = 0; i < rootNodeInfo.valueList.size(); i++)
                    {
                        ((List)rootNodeInfo.obj).add(rootNodeInfo.valueList.get(i));
                    }
                }
            }
        }
        
        //debugLog("BackwardToNode() end");
    }

    private void ClearRootNodeParentNode(XmlNode rootNode)
    {
        this.parentOfRootNode = rootNode.getParentNode();
        this.prevOfRootNode = rootNode.getPreviousNode();
        this.nextOfRootNode = rootNode.getNextNode();

        rootNode.setParentNode(null);
        rootNode.setPreviousNode(null);
        rootNode.setNextNode(null);
        
        _nodeInfoStack.clear();
    }

    private void RecoverRootNodeParentNode(XmlNode rootNode)
    {
        rootNode.setParentNode(this.parentOfRootNode);
        rootNode.setPreviousNode(this.prevOfRootNode);
        rootNode.setNextNode(this.nextOfRootNode);

        this.parentOfRootNode = null;
        this.prevOfRootNode = null;
        this.nextOfRootNode = null;
    }

    private DataClassFinder GetDataClassFinder(Class<?> typeOfObj)
    {
        Class<?> curType = typeOfObj;

        while (true)
        {
            if (curType.isArray())
            {
                curType = curType.getComponentType();
            }
            else if (IsList(curType))
            {
                curType = curType.getComponentType();
                if(curType == null) {
                    return new DataClassFinder();
                } else {
                	return new DataClassFinder(curType.getPackage());
                }
            }
            else if (BaseTypesMapping.IsSupportedBaseType(curType))
            {
            	return new DataClassFinder();
            }
            else
            {
                return new DataClassFinder(curType.getPackage());
            }
        }

    }
    
    private Class<?> GetTypeByName(String typeStr) throws ClassNotFoundException
    {
    	if(typeStr.equals(XmlSerializer.TAG_NAME_LIST)) {
    		return List.class;
    	} else if(typeStr.equals(XmlSerializer.TAG_NAME_MAP)) {
    		return Map.class;
    	}
    	
    	Class<?> cls = (Class<?>) BaseTypesMapping.GetSupportedTypeByDisplayName(typeStr);
    	if(cls != null) { 
    		return cls;
    	} else {
    		return _classFinder.findClass(typeStr);
    	}
    	
    }

    private Class<?> GetTypeForManEntry(XmlNode node) {
	    List<XmlNodeAttribute> attrs = node.getAttributes();
    	int attrSize = attrs.size();
	    
	    if(attrs != null) {
	    	if(attrSize == 1) {
	    		if(XmlSerializer.ATTR_NAME_TYPE.equals(attrs.get(0).getName())) {
	    			return ArrayList.class;
	    		}
	    	} else if(attrSize > 1) {
	    		for (XmlNodeAttribute attr : attrs) {
		    		if(XmlSerializer.ATTR_NAME_TYPE.equals(attr.getName())) {
		    			return ArrayList.class;
		    		}
	    		}
	    	}
	    }
	    
	    //find out if children's names are same
	    XmlNode child0 = node.getFirstChildNode();
	    if(child0 != null) {
	    	XmlNode child1 = child0.getNextNode();
	    	if(child1 != null) {
	    		if(child0.getName().equals(child1.getName())) {
	    			//children's names are same
	    			return ArrayList.class;
	    		}
	    	}
	    }
	    
	    //default
	    return HashMap.class;
    }
    

    private void PushNodeInfoStack(Class<?> type) throws IllegalAccessException, InstantiationException
    {
        NodeInfoData nodeInf = null;

        if (IsArray(type))
        {
            nodeInf = new NodeInfoData(null, type);
            nodeInf.valueList = new ArrayList<Object>();
        }
        else if (IsList(type))
        {
            //List
            nodeInf = new NodeInfoData(null, type);
            nodeInf.valueList = new ArrayList<Object>();
        } else if (IsMap(type)) {
            nodeInf = new NodeInfoData(new HashMap<String, Object>(), type);
        }
        else
        {
            //Object 
            nodeInf = new NodeInfoData(type.newInstance(), type);
        }

        _nodeInfoStack.add(nodeInf);
    }

    protected void SetPropertyValue(Object obj, PropertyDescriptor property, String valueStr) {
    	Type type = property.getPropertyType();
    	Class<?> cls = (Class<?>) type;

    	try {
			if(BaseTypesMapping.IsSupportedBaseType(cls)) {
				//primitive type
				BaseTypesMapping.SetPropertyValueOfPrimitiveType(obj, property, valueStr);
			} else if(cls.isArray() 
					&& cls.getComponentType().getName().equals(byte.class.getName())) {
				//byte[]
				property.getWriteMethod().invoke(obj, BaseTypesMapping.DecodeBase64(valueStr));
			} else if(IsList(cls)) {
            	if(cls.isInterface()) {
    				property.getWriteMethod().invoke(obj, new ArrayList());
            	} else {
    				property.getWriteMethod().invoke(obj, cls.newInstance());
            	}
			} else if (BaseTypesMapping.IsSupportedBaseType(cls)) {
				BaseTypesMapping.SetPropertyValueOfPrimitiveType(obj, property, valueStr);
			} else {
				//Do nothing
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Base64FormatException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
    }
    
    private Object ConvertStrToValue(boolean isElementOfList, Type type, String valueStr) 
    throws IllegalAccessException, ParseException, Base64FormatException, 
    UnsupportedEncodingException, IOException, InstantiationException
    {
    	Class<?> cls = (Class<?>) type;

    	if(BaseTypesMapping.IsSupportedBaseType(cls)) {
    		//supported base type
    		return BaseTypesMapping.Convert(type, valueStr);
    	} else if(cls.isArray() 
    			&& cls.getComponentType().getName().equals(byte.class.getName())) {
    		//byte[]
    		return BaseTypesMapping.DecodeBase64(valueStr);
    	} else if(IsList(cls)) {
    		return cls.newInstance();
    	} else if (IsMap(cls)) {
    		return cls.newInstance();
    	} else {
    		if(isElementOfList) {
    			return cls.newInstance();
    		} else {
        		return null;
    		}
    	}
    }
    
    private static boolean IsArray(Class<?> cls) {
    	return cls.isArray();
    }

    private static boolean IsList(Class<?> cls) {
    	//return IsInterfaceType(cls, List.class);
    	return List.class.isAssignableFrom(cls);
    }
    
    private static boolean IsMap(Class<?> cls) {
    	return Map.class.isAssignableFrom(cls);
    }
    
    private static boolean debugLog(String msg) {
    	System.out.println(System.currentTimeMillis() + " " + msg);
    	
    	return true;
    }

/*    
    private static PropertyDescriptor findPropertyDescriptor(String propertyName, Class<?> dataClass) 
    		throws IntrospectionException {
    	String propertyKey = dataClass.getName() + "." + propertyName;
    	PropertyDescriptor desc = _propertyDescriptorCache.get(propertyKey);
    	
    	if(desc == null) {
    		desc = new PropertyDescriptor(propertyName, dataClass);
    		_propertyDescriptorCache.put(propertyKey, desc);
    	}
    	
    	return desc;
    }
*/    
    /*
    private static boolean IsInterfaceType(Class<?> cls, Class<?> interfaceClass) {
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
    */

    protected class NodeInfoData
    {
        public Class<?> objType;

        public Object obj = null;

        public List<Object> valueList = null;

        public NodeInfoData()
        { 
        }

        public NodeInfoData(Object obj, Class<?> type)
        {
            this.objType = type;

            this.obj = obj;
        }
    }
    
//    protected class FieldValue {
//    	public String valueStr = null;
//    	public Type type = null;
//    	
//    	public FieldValue(Type type, String valueStr) {
//    		this.type = type;
//    		this.valueStr = valueStr;
//    	}
//    }
}
