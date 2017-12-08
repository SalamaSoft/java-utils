package MetoXML.Base;

import java.util.ArrayList;
import java.util.List;

import MetoXML.Util.StringUtil;

public class XmlTag {
    public static char[] BlankChrs = new char[]{' ', '\t', '\r', '\n'};

    public static String[] TagOperatorStrs = new String[] 
        { "<?", "?>", 
          "</", ">", 
          "<", "/>",
          "<", ">"
        };

    protected TagType type = new TagType(TagType.NodeBegin);

    private String name = "";

    private List<XmlNodeAttribute> attributes = new ArrayList<XmlNodeAttribute>();

    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TagType getType() {
		return type;
	}

	public List<XmlNodeAttribute> getAttributes() {
		return attributes;
	}

	protected XmlTag()
    { 
    }

    public XmlTag(String tagText) throws XmlParseException
    {
        ParseTagText(tagText);
    }

    private void ParseTagText(String tagText) throws XmlParseException
    { 
        for(int i = 0; i < 7; i = i + 2)
        {
            if (tagText.startsWith(TagOperatorStrs[i]) && tagText.endsWith(TagOperatorStrs[i + 1]))
            {
                type.setValue(i);
                if (type.getValue() == TagType.NodeBegin 
                		|| type.getValue() == TagType.NodeAllInOne 
                		|| type.getValue() == TagType.HeadNode)
                {
                    //Attributes
                    int prevIndex = 0;
                    int index = 0;
                    boolean isFoundName = false;
                    int attributeLen = 0;
                    int indexOfAttributeValueBegin = 0;
                    int indexOfAttributeEnd = 0;
                    while (true)
                    {
                        index = StringUtil.IndexOfAny(tagText, BlankChrs, index + 1);
                        if (index < 0)
                        {
                            if (!isFoundName)
                            {
                                name = Trim(
                                			tagText.substring(
                                					TagOperatorStrs[i].length(),
                                					TagOperatorStrs[i].length() + 
                                					(tagText.length() - TagOperatorStrs[i].length() 
                                						- TagOperatorStrs[i + 1].length())
                                			)
                                    	);
                            }

                            if (prevIndex > 0)
                            {
                                //attribute
                                attributeLen = tagText.length() - TagOperatorStrs[i + 1].length() - prevIndex - 1;

                                //a=""
                                if (attributeLen > 0)
                                {
                                    String attributeText = tagText.substring(prevIndex + 1, prevIndex + 1 + attributeLen);
                                    if (attributeLen > 4)
                                    {
                                        this.attributes.add(ParseAttributeText(attributeText));
                                    }
                                    else
                                    {
                                        throw new XmlParseException("Format of attribute is not correct :[" + attributeText + "]");
                                    }
                                }
                            }
                            break;
                        }
                        else
                        {
                            if (!isFoundName)
                            {
                                name = Trim(tagText.substring(
                                		TagOperatorStrs[i].length(), 
                                		TagOperatorStrs[i].length() + (index - TagOperatorStrs[i].length())
                                		));
                                isFoundName = true;
                            }

                            if (prevIndex > 0)
                            {
                                //attribute
                                attributeLen = index - prevIndex - 1;

                                if (attributeLen > 0)
                                {
                                    //a=""
                                    String attributeText = tagText.substring(prevIndex + 1, prevIndex + 1 + attributeLen);

                                    indexOfAttributeValueBegin = attributeText.indexOf("=\"");

                                    if (indexOfAttributeValueBegin <= 0)
                                    {
                                        throw new XmlParseException("Format of attribute is not correct. There is no '\"' after '=' :[" + attributeText + "]");
                                    }

                                    indexOfAttributeEnd = attributeText.indexOf("\"", indexOfAttributeValueBegin + 2);
                                    if (indexOfAttributeEnd > 0)
                                    {
                                        if (attributeLen > 4 && (indexOfAttributeEnd + prevIndex + 1) == (index - 1))
                                        {
                                            this.attributes.add(ParseAttributeText(attributeText));
                                        }
                                        else
                                        {
                                            throw new XmlParseException("Format of attribute is not correct :[" + attributeText + "]");
                                        }
                                    }
                                    else
                                    {
                                        continue;
                                    }
                                }
                            }

                            prevIndex = index;
                        }

                    }
                }
                else
                {
                    name = Trim(tagText.substring(
                        TagOperatorStrs[i].length(),
                        TagOperatorStrs[i].length() + 
                        (tagText.length() - TagOperatorStrs[i].length() - TagOperatorStrs[i + 1].length())
                        ));
                    break;
                }

                break;
            }
        }
    }

    /// <summary>
    /// Trim these chars: ' ', '\t', '\r', '\n' 
    /// </summary>
    /// <param name="str"></param>
    /// <returns></returns>
    public static String Trim(String str)
    {
        if (str.length() == 0) return str;
        
        int index1 = -1;
        int index2 = -1;


        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) != BlankChrs[0] && str.charAt(i) != BlankChrs[1]
                && str.charAt(i) != BlankChrs[2] && str.charAt(i) != BlankChrs[3])
            {
                index1 = i;
                break;
            }
        }
        for (int i = str.length() - 1; i >= 0; i--)
        {
            if (str.charAt(i) != BlankChrs[0] && str.charAt(i) != BlankChrs[1]
                && str.charAt(i) != BlankChrs[2] && str.charAt(i) != BlankChrs[3])
            {
                index2 = i;
                break;
            }
        }

        if (index1 < 0 || index2 < 0)
        {
            return "";
        }
        else
        {
            return str.substring(index1, index1 + index2 - index1 + 1);
        }
    }

    public static String TrimLeft(String str)
    {
        if (str.length() == 0) return str;

        int index1 = -1;


        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) != BlankChrs[0] && str.charAt(i) != BlankChrs[1]
                && str.charAt(i) != BlankChrs[2] && str.charAt(i) != BlankChrs[3])
            {
                index1 = i;
                break;
            }
        }

        if (index1 < 0)
        {
            return "";
        }
        else
        {
            return str.substring(index1, index1 + str.length() - index1);
        }
    }

    //A="B"
    private XmlNodeAttribute ParseAttributeText(String attributeText) throws XmlParseException
    {
        int indexOfEqual = attributeText.indexOf('=');
        if (indexOfEqual <= 0)
        {
            throw new XmlParseException("Format of attribute is not correct. There is no '=' :[" + attributeText + "]");
        }

        if (attributeText.charAt(indexOfEqual + 1) != '"')
        {
            throw new XmlParseException("Format of attribute is not correct. There is no '\"' after '=' :[" + attributeText + "]");
        }

        if (!attributeText.endsWith("\""))
        {
            throw new XmlParseException("Format of attribute is not correct. Does not end with '\"' :[" + attributeText + "]");
        }

        XmlNodeAttribute attribute = new XmlNodeAttribute();
        attribute.setName(attributeText.substring(0, indexOfEqual));
        int valueLen = attributeText.length() - 1 - indexOfEqual - 2;
        if (valueLen == 0)
        {
            attribute.setValue("");
        }
        else
        {
            attribute.setValue(XmlContentEncoder.DecodeContent(attributeText.substring(indexOfEqual + 2, indexOfEqual + 2 + valueLen)));
        }

        return attribute;
    }
}
