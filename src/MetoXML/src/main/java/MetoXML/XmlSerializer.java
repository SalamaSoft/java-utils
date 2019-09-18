package MetoXML;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import MetoXML.Base.XmlDocument;
import MetoXML.Base.XmlNode;
import MetoXML.Base.XmlNodeAttribute;
import MetoXML.Cast.BaseTypesMapping;
import MetoXML.Util.ITreeNode;

public class XmlSerializer extends AbstractReflectInfoCachedSerializer{
	public static final Charset DefaultCharset = Charset.forName("UTF-8");

	//public static final String TAG_NAME_ARRAY = "Array";
	public static final String TAG_NAME_ARRAY = "List";
	
	public static final String TAG_NAME_LIST = "List";
	public static final String TAG_NAME_MAP = "Map";
	
	public static final String TAG_NAME_BYTE_ARRAY = "byte[]";
	
	public static final String ATTR_NAME_TYPE = "type";

	public void Serialize(String filePath, Object obj, Class<?> typeOfObj, Charset charset) 
    throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		Serialize(filePath, obj, typeOfObj, charset, false);
    }	

	public void Serialize(String filePath, Object obj, Class<?> typeOfObj, Charset charset, boolean isUseClassFullName) 
    throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException
    {
		FileOutputStream fs = null;
		OutputStreamWriter writer = null;
		
		try {
			fs = new FileOutputStream(filePath, false);
			writer = new OutputStreamWriter(fs, charset);
			Serialize(writer, obj, typeOfObj, isUseClassFullName);
		} finally {
			try {
				fs.close();
			} catch(Exception ex) {
			}
			try {
				writer.close();
			} catch(Exception ex) {
			}
		}
		
    }
	
	public void Serialize(OutputStreamWriter sw, Object obj, Class<?> typeOfObj) 
    throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		Serialize(sw, obj, typeOfObj, false);
	}
	
	
	public void Serialize(OutputStreamWriter sw, Object obj, Class<?> typeOfObj, boolean isUseClassFullName) 
    throws IOException, IntrospectionException, IllegalAccessException, InvocationTargetException
    {
        // Modified on 2010/10/23
        //XmlNode node = ConvertObjectToXmlNode(GetClassName(typeOfObj.getName()), obj, typeOfObj);
        XmlNode node = ConvertObjectToXmlNode(GetTypeTagName(typeOfObj, isUseClassFullName), obj, typeOfObj, isUseClassFullName);
        
//        node.getAttributes().add(new XmlNodeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));
//        node.getAttributes().add(new XmlNodeAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema"));

        XmlDocument xmlDoc = new XmlDocument();
        xmlDoc.setRootNode(node);

        XmlWriter xmlWriter = new XmlWriter();
        xmlWriter.WriteXmlDocument(xmlDoc, sw);
    }

	public static String objectToString(Object obj)
	throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException {
		if(obj == null) {
			return "";
		} else {
			return objectToString(obj, obj.getClass(), false, false);
		}
	}	
	
	public static String objectToString(Object obj, Class<?> typeOfObj)
	throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException {
		return objectToString(obj, typeOfObj, false, false);
	}	

	public static String objectToString(Object obj, Class<?> typeOfObj, boolean isUseClassFullName)
	throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException 
	{
		return objectToString(obj, typeOfObj, isUseClassFullName, false);
	}	
	
	public static String objectToString(Object obj, Class<?> typeOfObj, boolean isUseClassFullName, boolean isMakeFirstCharUpperCase)
	throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException 
	{
		try {
			XmlSerializer xmlSer = new XmlSerializer();
			XmlNode rootNode = xmlSer.ConvertObjectToXmlNode(XmlSerializer.GetTypeTagName(typeOfObj, isUseClassFullName), 
					obj, typeOfObj, isUseClassFullName, isMakeFirstCharUpperCase);
			
			XmlWriter xmlWriter = new XmlWriter();
			return xmlWriter.XmlNodeToString(rootNode, DefaultCharset);
		} catch (IntrospectionException e) {
			//log.error("XMLUtil.objectToString() error.", e);
			throw e;
		} catch (IllegalAccessException e) {
			//log.error("XMLUtil.objectToString() error.", e);
			throw e;
		} catch (InvocationTargetException e) {
			//log.error("XMLUtil.objectToString() error.", e);
			throw e;
		} catch (IOException e) {
			//log.error("XMLUtil.objectToString() error.", e);
			throw e;
		}
	}
    
    public XmlNode ConvertObjectToXmlNode(String nodeName, Object obj, Class<?> typeOfObj) 
    throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException
    {
    	return doConvertObjectToXmlNode(nodeName, obj, typeOfObj, false, false);
    }

    public XmlNode ConvertObjectToXmlNode(String nodeName, Object obj, Class<?> typeOfObj, boolean isUseClassFullName) 
    throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException
    {
    	return doConvertObjectToXmlNode(nodeName, obj, typeOfObj, isUseClassFullName, false);
    }	
    
    public XmlNode ConvertObjectToXmlNode(String nodeName, Object obj, Class<?> typeOfObj, boolean isUseClassFullName, boolean isMakeFirstCharUpperCase) 
    throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException
    {
    	return doConvertObjectToXmlNode(nodeName, obj, typeOfObj, isUseClassFullName, isMakeFirstCharUpperCase);
    }	
    
    /// <summary>
    /// Do not use the recursive method, so the code is more complicated than recursive method.
    /// </summary>
    /// <param name="nodeName"></param>
    /// <param name="obj"></param>
    /// <param name="typeOfObj"></param>
    /// <returns></returns>
    protected XmlNode doConvertObjectToXmlNode(String nodeName, Object obj, Class<?> typeOfObj, boolean isUseClassFullName, boolean isMakeFirstCharUpperCase) 
    throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException
    {
        XmlNode rootNode = new XmlNode();
        rootNode.setName(nodeName);

        XmlNode nodeTmp = null;
        String tagName = null;
        PropertyDescriptor pInf = null;
        NodeInfoData classNode = null;
        XmlNode prevNode = null;
        int arrayLen = 0;
        
        Object objTmp = obj;

        // Modified on 2010/10/23 start
        //PropertyDescriptor[] properties = Introspector.getBeanInfo(typeOfObj).getPropertyDescriptors();
        //if (properties == null || properties.length == 0) return rootNode;

        //List<NodeInfoData> classNodeStack = new ArrayList<NodeInfoData>();
        //classNodeStack.add(new NodeInfoData(obj, 0, rootNode, properties));

        if (obj == null)
        {
            rootNode.setContent("");
            return rootNode;
        }

        if (BaseTypesMapping.IsSupportedBaseType(typeOfObj))
        {
            //base type
            rootNode.setContent(BaseTypesMapping.ConvertBaseTypeValueToStr(typeOfObj, obj));

            return rootNode;
        }
        else if (typeOfObj.getName().equals(byte[].class.getName()))
        {
            rootNode.setContent(BaseTypesMapping.EncodeBase64((byte[])obj));

            return rootNode;
        }

        List<NodeInfoData> classNodeStack = new ArrayList<NodeInfoData>();
        if (typeOfObj.isArray())
        {
            //Array
            classNode = new NodeInfoData(obj, 0, rootNode, NodeType.ElementInArray);
            classNodeStack.add(classNode);
        }
        else if (IsList(typeOfObj))
        {
            //List
            classNode = new NodeInfoData(obj, 0, rootNode, NodeType.ElementInList);
            classNodeStack.add(classNode);
        } else if (IsMap(typeOfObj)) {
        	//Map
            classNode = new NodeInfoData(obj, 0, rootNode, ((Map<String, Object>)obj).entrySet().toArray(new Map.Entry[0]));
            classNodeStack.add(classNode);
        }
        else
        {
            //Data
            //PropertyDescriptor[] properties = Introspector.getBeanInfo(typeOfObj).getPropertyDescriptors();
        	PropertyDescriptor[] properties = findPropertyDescriptorArray(typeOfObj);
            if (properties == null || properties.length == 0) return rootNode;

            classNodeStack.add(new NodeInfoData(obj, 0, rootNode, properties));
        }
        // Modified on 2010/10/23 end



        while (true)
        {
            classNode = classNodeStack.get(classNodeStack.size() - 1);

            if (classNode.type == NodeType.Property)
            {
                arrayLen = classNode.propInfArray.length;
            }
            else if (classNode.type == NodeType.ElementInArray)
            {
                arrayLen = Array.getLength(classNode.obj);
            } else if (classNode.type == NodeType.EntryInMap) {
            	arrayLen = classNode.mapEntryArray.length;
            }
            else
            {
                arrayLen = ((List)classNode.obj).size();
            }

            if (classNode.index >= arrayLen)
            {
                if (classNodeStack.size() == 1)
                {
                    break;
                }
                else
                {
                    prevNode = classNodeStack.get(classNodeStack.size() - 1).node;

                    classNodeStack.remove(classNodeStack.size() - 1);
                    classNode = classNodeStack.get(classNodeStack.size() - 1);
                    continue;
                }
            }
            else
            {
                if (classNode.type == NodeType.Property)
                {
                    pInf = classNode.propInfArray[classNode.index];
                    classNode.index++;

                    if (pInf.getReadMethod() != null && pInf.getWriteMethod() != null)
                    {
                        objTmp = pInf.getReadMethod().invoke(classNode.obj);

                        if (objTmp == null)
                        {
                            continue;
                        }

                        tagName = GetPropertyDisplayName(pInf, isMakeFirstCharUpperCase);
                    }
                    else
                    {
                        continue;
                    }
                    
                }
                else if (classNode.type == NodeType.ElementInArray)
                {
                    objTmp = Array.get(classNode.obj, classNode.index);
                    tagName = "";
                    classNode.index++;
                }
                else if (classNode.type == NodeType.ElementInList) {
                    objTmp = ((List)classNode.obj).get(classNode.index);
                    tagName = "";

                    classNode.index++;
                } else if (classNode.type == NodeType.EntryInMap) {
                	Map.Entry<String, Object> entry = classNode.mapEntryArray[classNode.index];
                	objTmp = entry.getValue();
                	tagName = entry.getKey();
                	
                	classNode.index ++;

                	if(objTmp == null) {
                	    continue;
                    }
                } else {
                	throw new RuntimeException("Not supported type:" + classNode.obj.getClass().getName());
                }

            }

            if (BaseTypesMapping.IsSupportedBaseType(objTmp.getClass()))
            {
                //Base type
                nodeTmp = new XmlNode();
                if (tagName.length() > 0)
                {
                    nodeTmp.setName(tagName);
                }
                else
                {
                    nodeTmp.setName(BaseTypesMapping.GetSupportedFieldTypeDisplayName(objTmp.getClass()));
                }

                nodeTmp.setContent(
                		BaseTypesMapping.ConvertBaseTypeValueToStr(objTmp.getClass(), objTmp));

                AddNextNode(classNode.node, prevNode, nodeTmp);
                prevNode = nodeTmp;
            }
            else if (IsArray(objTmp.getClass()))
            {
                //Array
                if (objTmp.getClass().getComponentType().getName().equals(byte.class.getName()))
                {
                    nodeTmp = new XmlNode();
                    if (tagName.length() > 0)
                    {
                        nodeTmp.setName(tagName);
                    }
                    else
                    {
                    	//modified 2012/05/22
                        //nodeTmp.setName(TAG_NAME_ARRAY);
                    	nodeTmp.setName(TAG_NAME_BYTE_ARRAY);
                    }
                    nodeTmp.setContent(BaseTypesMapping.EncodeBase64((byte[])objTmp));

                    AddNextNode(classNode.node, prevNode, nodeTmp);
                    prevNode = nodeTmp;
                }
                else
                {
                    //Array
                    nodeTmp = new XmlNode();
                    if (tagName.length() > 0)
                    {
                        nodeTmp.setName(tagName);
                    }
                    else
                    {
                        nodeTmp.setName(TAG_NAME_ARRAY);
                    }

                    if(classNode.type == NodeType.EntryInMap) {
                    	//to support parse map
                    	addAttrTypeListForMapEntry(nodeTmp);
                    }
                    
                    AddNextNode(classNode.node, prevNode, nodeTmp);

                    classNode = new NodeInfoData(objTmp, 0, nodeTmp, NodeType.ElementInArray);
                    classNodeStack.add(classNode);

                    prevNode = null;
                }
            }
            else if (IsList(objTmp.getClass()))
            {
                //List
                nodeTmp = new XmlNode();
                if (tagName.length() > 0)
                {
                    nodeTmp.setName(tagName);
                }
                else
                {
                    nodeTmp.setName(TAG_NAME_LIST);
                }
                
                if(classNode.type == NodeType.EntryInMap) {
                	//to support parse map
                	addAttrTypeListForMapEntry(nodeTmp);
                }

                AddNextNode(classNode.node, prevNode, nodeTmp);

                classNode = new NodeInfoData(objTmp, 0, nodeTmp, NodeType.ElementInList);
                classNodeStack.add(classNode);

                prevNode = null;
            } else if (IsMap(objTmp.getClass())) {
                nodeTmp = new XmlNode();
                if (tagName.length() > 0)
                {
                    nodeTmp.setName(tagName);
                }
                else
                {
                    nodeTmp.setName(TAG_NAME_MAP);
                }

                AddNextNode(classNode.node, prevNode, nodeTmp);

                //classNode = new NodeInfoData(objTmp, 0, nodeTmp, Introspector.getBeanInfo(objTmp.getClass()).getPropertyDescriptors());
                classNode = new NodeInfoData(objTmp, 0, nodeTmp, ((Map<String, Object>)objTmp).entrySet().toArray(new Map.Entry[0]));
                classNodeStack.add(classNode);

                prevNode = null;
            }
            else
            {
                //Other class
                nodeTmp = new XmlNode();
                if (tagName.length() > 0)
                {
                    nodeTmp.setName(tagName);
                }
                else
                {
                    nodeTmp.setName(GetClassName(objTmp.getClass(), isUseClassFullName));
                }

                AddNextNode(classNode.node, prevNode, nodeTmp);

                //classNode = new NodeInfoData(objTmp, 0, nodeTmp, Introspector.getBeanInfo(objTmp.getClass()).getPropertyDescriptors());
                classNode = new NodeInfoData(objTmp, 0, nodeTmp, findPropertyDescriptorArray(objTmp.getClass()));
                classNodeStack.add(classNode);

                prevNode = null;
            }

        }//while

        return rootNode;
    }

    private static void AddNextNode(XmlNode parentNode, XmlNode prevNode, XmlNode nodeForAdd)
    {
        nodeForAdd.setParentNode(parentNode);

        if (prevNode == null)
        {
            nodeForAdd.getParentNode().setFirstChildNode(nodeForAdd);
            nodeForAdd.getParentNode().setLastChildNode(nodeForAdd);
        }
        else
        {
            nodeForAdd.getParentNode().setLastChildNode(nodeForAdd);

            nodeForAdd.setPreviousNode(prevNode);
            prevNode.setNextNode(nodeForAdd);
        }
    }
    
    private static void addAttrTypeListForMapEntry(XmlNode nodeTmp) {
    	nodeTmp.getAttributes().add(new XmlNodeAttribute(ATTR_NAME_TYPE, TAG_NAME_LIST));
    }

    /// <summary>
    /// Modified on 2010/10/23
    /// </summary>
    /// <param name="type"></param>
    /// <returns></returns>
    public static String GetTypeTagName(Class<?> type, boolean isUseClassFullName)
    {
    	if(BaseTypesMapping.IsSupportedBaseType(type)) {
    		return BaseTypesMapping.GetSupportedFieldTypeDisplayName(type);
    	} else if (type.isArray())
        {
            return TAG_NAME_ARRAY;
        }
        else if (IsList(type))
        {
            return TAG_NAME_LIST;
        } else if (IsMap(type)) {
        	return TAG_NAME_MAP;
        }
        else
        {
        	if(isUseClassFullName) {
                return type.getName();
        	} else {
                return type.getSimpleName();
        	}
        }
    }
    
    protected static String GetClassName(Class<?> type, boolean isUseClassFullName) {
    	/*
    	int index = fullName.lastIndexOf('.');
    	if(index >= 0) {
    		return fullName.substring(index + 1);
    	} else {
    		return fullName;
    	}
    	*/
    	if(isUseClassFullName) {
            return type.getName();
    	} else {
            return type.getSimpleName();
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

    protected static String GetPropertyDisplayName(PropertyDescriptor property, boolean isMakeFirstCharUpperCase) {
    	String name = property.getDisplayName();
    	if(isMakeFirstCharUpperCase) {
        	name = name.substring(0, 1).toUpperCase() + name.substring(1);
    	}
    	return name;
    }

    
    /// <summary>
    /// Modified on 2010/10/23
    /// </summary>
    /// <param name="valueType"></param>
    /// <param name="value"></param>
    /// <returns></returns>
    
    public enum NodeType {Property, ElementInArray, ElementInList, EntryInMap};

    protected class NodeInfoData
    {
        public NodeType type = NodeType.Property;

        /// <summary>
        /// The object which array belongs to or properties array belongs to.
        /// </summary>
        public Object obj = null;

        public PropertyDescriptor[] propInfArray = null;
        
        public Map.Entry<String, Object>[] mapEntryArray = null;

        /// <summary>
        /// The xml node of the object
        /// </summary>
        public XmlNode node = null;

        /// <summary>
        /// Index in array or properties array
        /// </summary>
        public int index = -1;


        public NodeInfoData()
        { 
        }

        public NodeInfoData(Object objForArray, int indexInArray, XmlNode nodeOfObj, NodeType type)
        {
            this.type = NodeType.ElementInArray;
            this.obj = objForArray;
            this.index = indexInArray;
            this.node = nodeOfObj;
            this.type = type;
        }

        public NodeInfoData(Object objForProperties, int indexInProperties, XmlNode nodeOfObj, PropertyDescriptor[] propertyInfoArray)
        {
            this.type = NodeType.Property;
            this.obj = objForProperties;
            this.propInfArray = propertyInfoArray;
            this.index = indexInProperties;
            this.node = nodeOfObj;
        }
        
        public NodeInfoData(Object objForMap, int indexInProperties, XmlNode nodeOfObj, Map.Entry<String, Object>[] mapEntryArray)
        {
            this.type = NodeType.EntryInMap;
            this.obj = objForMap;
            this.mapEntryArray = mapEntryArray;
            this.index = indexInProperties;
            this.node = nodeOfObj;
        }
        
    }

	@Override
	protected void ForwardToNode(ITreeNode node, int depth, boolean isLeafNode) {
		//Nothing to do
	}

	@Override
	protected void BackwardToNode(ITreeNode node, int depth) {
		//Nothing to do
	}
}
