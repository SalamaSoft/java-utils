package MetoXML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import MetoXML.Base.TagType;
import MetoXML.Base.XmlDocument;
import MetoXML.Base.XmlNode;
import MetoXML.Base.XmlNodeAttribute;
import MetoXML.Base.XmlParseException;
import MetoXML.Base.XmlTag;
import MetoXML.Base.XmlTagContent;
import MetoXML.Util.CharArray;

public class XmlReader {
    private final static String DEFAULT_ENCODING = "utf-8";
//    private final static String RETURN_LINE_STR = "\r\n";
//    private final static String DEPTH_STR = "  ";

    private static String[] XmlCommentStrs = new String[] {"<!--", "-->" };

    private static char[] TagOperatorChrs = new char[] {'<', '>'};
//    private static String[] TagOperatorStrs = new String[] { "<", ">" };

    
    //private static String[] OperatorXmlHeadTag = new String[] { "<?", "?>" };
    //private static String[] OperatorNodeBeginTag = new String[] {"<", ">" };
    //private static String[] OperatorNodeEndTag = new String[] { "</", ">" };
    //private static String[] OperatorNodeOneTag = new String[] { "<", "/>" };

    /*
    public delegate void ReachNodeBeginDelegate(String nodeName, String tagStr);
    public delegate void ReachNodeEndDelegate(String nodeName, String tagStr);
    public delegate void FinishParseNodeDelegate(XmlNode xmlNode);

    public ReachNodeBeginDelegate ReachNodeBegin = null;
    public ReachNodeEndDelegate ReachNodeEnd = null;
    public FinishParseNodeDelegate FinishParseNode = null;
    */

    private int _tagOperatorBeginIndex = -1;
    private LinkedList<XmlTag> _tagStack = new LinkedList<XmlTag>();
    private List<XmlNode> _nodeStack = new ArrayList<XmlNode>();

    //private StringBuilder _tagTextBuff = new StringBuilder();
    private MetoXML.Util.StringBuffer _tagTextBuff = new MetoXML.Util.StringBuffer(1024);
    private boolean _isCommentBegin = false;
    private int _commentBeginIndex = -1;
    private CharArray _commentScanFlag = new CharArray(XmlCommentStrs[0].length());
    
    public XmlReader()
    { 
    }

    public XmlDocument ReadXml(String filePath) throws FileNotFoundException, IOException, XmlParseException
    {
    	FileInputStream fis = null;
        InputStreamReader sr = null;
        String xmlHead = "";

        try
        {
        	fis = new FileInputStream(filePath);
            sr = new InputStreamReader(fis, Charset.forName(DEFAULT_ENCODING.toUpperCase()));
            xmlHead = GetXmlHead(sr);
        }
        finally
        {
            if(fis != null)
            {
                try {
                	fis.close();
				} catch (IOException e) {
				}
            }
            if(sr != null)
            {
                try {
					sr.close();
				} catch (IOException e) {
				}
            }
        }

        String encoding = DEFAULT_ENCODING;
        if (xmlHead.length() != 0)
        {
            encoding = GetXmlEncodingFromHead(xmlHead);
        }

        try
        {
        	fis = new FileInputStream(filePath);
            sr = new InputStreamReader(fis, Charset.forName(encoding.toUpperCase()));
            return ReadXml(sr);
        }
        finally
        {
            if(fis != null)
            {
                try {
                	fis.close();
				} catch (IOException e) {
				}
            }
            if (sr != null)
            {
                try {
					sr.close();
				} catch (RuntimeException e) {
				}
            }
        }
        
    }

    public XmlDocument ReadXml(ByteArrayInputStream memStream) throws IOException, XmlParseException
    {
        InputStreamReader sr1 = null;

        try {
            memStream.mark(0);
            sr1 = new InputStreamReader(memStream, Charset.forName(DEFAULT_ENCODING.toUpperCase()));

//            String xmlHead = "";
//            String encoding = DEFAULT_ENCODING;
//            xmlHead = GetXmlHead(sr1);
//
//            if (xmlHead.length() != 0)
//            {
//                encoding = GetXmlEncodingFromHead(xmlHead);
//            }

            memStream.mark(0);
            return ReadXml(sr1);
        } finally {
            if (sr1 != null)
            {
                try {
					sr1.close();
				} catch (RuntimeException e) {
				}
            }
        }
    }

    public XmlDocument ReadXml(InputStreamReader sr) throws XmlParseException, IOException
    {
        char[] buffTmp = new char[1024];
        int readCnt = 0;

        //Init variables for statuses
        _tagOperatorBeginIndex = -1;
        _tagStack.clear();
        _nodeStack.clear();
        _tagTextBuff.Clear();
        _isCommentBegin = false;
        _commentBeginIndex = -1;
        _commentScanFlag.Clear();

        XmlDocument xmlDoc = new XmlDocument();
        XmlNode nodeTmp = null;
        XmlTag tag = null;
        XmlNodeAttribute attribute = null;

        while (true)
        {
            readCnt = sr.read(buffTmp, 0, buffTmp.length);
            if (readCnt <= 0) break;

            ScanXmlText(buffTmp, 0, readCnt);

            //for(int i = 0; i < _tagStack.Count; i++)
            while(_tagStack.size() > 0)
            {
                tag = _tagStack.poll();

                if (tag.getType().getValue() == TagType.NodeBegin)
                {
                    nodeTmp = new XmlNode();
                    nodeTmp.setName(tag.getName());
                    nodeTmp.getAttributes().addAll(tag.getAttributes());

                    _nodeStack.add(nodeTmp);
                }
                else if (tag.getType().getValue() == TagType.ContentPart)
                {
                    if (_nodeStack.size() > 0)
                    {
                        _nodeStack.get(_nodeStack.size() - 1).setContent(((XmlTagContent)tag).getContent());
                    }
                }
                else if (tag.getType().getValue() == TagType.NodeEnd)
                {
                    if (FinishOneNode()) break;
                }
                else if (tag.getType().getValue() == TagType.NodeAllInOne)
                {
                    nodeTmp = new XmlNode();
                    nodeTmp.setName(tag.getName());
                    nodeTmp.getAttributes().addAll(tag.getAttributes());

                    _nodeStack.add(nodeTmp);

                    if(FinishOneNode()) break;
                }
                else if (tag.getType().getValue() == TagType.HeadNode)
                {
                    for (int k = 0; k < tag.getAttributes().size(); k++)
                    {
                        attribute = tag.getAttributes().get(k);
                        if (attribute.getName().equals("version"))
                        {
                            xmlDoc.setVersion(attribute.getValue());
                        }
                        else
                        {
                            xmlDoc.setEncoding(attribute.getValue());
                        }
                    }
                }//if

            }//while
        }//while

        if (_nodeStack.size() != 1)
        {
            throw new XmlParseException("Format of xml Tag is not correct: Root node");
        }

        xmlDoc.setRootNode(_nodeStack.get(0));

        return xmlDoc;
    }

    public XmlNode StringToXmlNode(String nodeXml, Charset encoding)
    throws IOException, XmlParseException
    {
    	ByteArrayInputStream memInputStream = null;
    	ByteArrayOutputStream memOutStream = null;
        OutputStreamWriter sw = null;

        try
        {
            memOutStream = new ByteArrayOutputStream();
            sw = new OutputStreamWriter(memOutStream, encoding);
            
            sw.write(nodeXml);
            sw.flush();
            
            memInputStream = new ByteArrayInputStream(memOutStream.toByteArray());
            memInputStream.mark(0);
            
            XmlDocument xmlDoc = ReadXml(memInputStream);
            return xmlDoc.getRootNode();
        }
        finally
        {
            if (memOutStream != null)
            {
            	try {
					memOutStream.close();
				} catch (IOException e) {
				}
            }

            if (memInputStream != null)
            {
            	try {
					memInputStream.close();
				} catch (IOException e) {
				}
            }

            if (sw != null)
            {
                try {
					sw.close();
				} catch (IOException e) {
				}
            }
        }

    }
    
    /// <summary>
    /// 
    /// </summary>
    /// <returns>true: root node is finished  false:</returns>
    private boolean FinishOneNode() throws XmlParseException
    {
        XmlNode nodeTmp = null;
        XmlNode nodeTmp2 = null;

        if (_nodeStack.size() == 1)
        {
            if (_nodeStack.get(0).getFirstChildNode() != null)
            {
                _nodeStack.get(0).setContent(XmlTag.TrimLeft(_nodeStack.get(0).getContent()));
                if (_nodeStack.get(0).getContent().length() > 0)
                {
                    throw new XmlParseException("Format of xml Tag is not correct:Parent node can not have content:<" 
                    		+ _nodeStack.get(0).getName() + ">" + _nodeStack.get(0).getContent());
                }
            }
            //break;
            return true;
        }
        else
        {
            nodeTmp = _nodeStack.get(_nodeStack.size() - 1);
            _nodeStack.remove(_nodeStack.size() - 1);

            if (nodeTmp.getFirstChildNode() != null)
            {
                nodeTmp.setContent(XmlTag.TrimLeft(nodeTmp.getContent()));
                if (nodeTmp.getContent().length() > 0)
                {
                    throw new XmlParseException("Format of xml Tag is not correct:Parent node can not have content:<" 
                    		+ nodeTmp.getName() + ">" + nodeTmp.getContent());
                }
            }

            nodeTmp2 = _nodeStack.get(_nodeStack.size() - 1);
            if (nodeTmp2.getFirstChildNode() == null)
            {
                nodeTmp2.setFirstChildNode(nodeTmp);
                nodeTmp2.setLastChildNode(nodeTmp);

                nodeTmp.setParentNode(nodeTmp2);
            }
            else
            {
                nodeTmp2.getLastChildNode().setNextNode(nodeTmp);
                nodeTmp.setPreviousNode(nodeTmp2.getLastChildNode());
                nodeTmp.setParentNode(nodeTmp2);

                nodeTmp2.setLastChildNode(nodeTmp);
            }

            return false;
        }
    }

    private void ScanXmlText(char[] chrBuff, int offset, int len) throws XmlParseException
    {
        char chr = '\0';
        int k = 0;
        boolean isMatched = false;

        for (int i = offset; i < len; i++)
        {
            chr = chrBuff[i];

            if (chr == TagOperatorChrs[1])
            {
                // '>'
                _tagTextBuff.Append(chr);

                if (_isCommentBegin)
                {
                    //Scan comment: "<!--", "-->"
                    if (_tagTextBuff.EndsWith(XmlCommentStrs[1]))
                    {
                        //Comment end
                        _tagTextBuff.Remove(_commentBeginIndex, _tagTextBuff.Length() - _commentBeginIndex);
                        _commentBeginIndex = -1;
                        _isCommentBegin = false;
                    }
                    continue;
                }
                else
                {
                    if (_tagOperatorBeginIndex >= 0)
                    {
                        if (_tagOperatorBeginIndex > 0)
                        {
                            XmlTagContent tagContent = new XmlTagContent(
                            		_tagTextBuff.SubString(0, _tagOperatorBeginIndex));
                            _tagStack.add((XmlTag) tagContent);
                        }

                        XmlTag tag = new XmlTag(
                        		_tagTextBuff.SubString(_tagOperatorBeginIndex, 
                        				_tagOperatorBeginIndex + _tagTextBuff.Length() - _tagOperatorBeginIndex));
                        _tagStack.add(tag);

                        _tagTextBuff.Remove(0, _tagTextBuff.Length());
                        _tagOperatorBeginIndex = -1;
                    }
                    else
                    {
                        //format error
                        throw new XmlParseException("Format of xml Tag is not correct:[" 
                        		+ new String(chrBuff, offset, len - offset) + "]");
                    }
                }
            }
            else
            {
                _tagTextBuff.Append(chr);

                if(!_isCommentBegin)
                {
                    //Comment does not begin
                    if (chr == TagOperatorChrs[0])
                    {
                        //'<'
                        _commentScanFlag.Clear();
                        _commentScanFlag.Add(chr);

                        _tagOperatorBeginIndex = _tagTextBuff.Length() - 1;
                    }
                    else
                    {
                        if (_commentScanFlag.Length() > 0)
                        {
                            _commentScanFlag.Add(chr);

                            if (_commentScanFlag.Length() == _commentScanFlag.getCapacity())
                            {
                                //Comment judge
                                isMatched = true;
                                for (k = 0; k < _commentScanFlag.Length(); k++)
                                {
                                    if (_commentScanFlag.Array()[k] != XmlCommentStrs[0].charAt(k))
                                    {
                                        isMatched = false;
                                        break;
                                    }
                                }

                                if (isMatched)
                                {
                                    _isCommentBegin = true;
                                    _commentBeginIndex = _tagTextBuff.Length() - XmlCommentStrs[0].length();

                                    _tagOperatorBeginIndex = -1;
                                }
                                else
                                {
                                    _isCommentBegin = false;

                                }

                                _commentScanFlag.Clear();
                            }
                        }
                    }
                }


            }

        }
    }

    /*
    private NodeOperatorType MatchTagOperator(char[] chrBuff, int offset)
    { 
    }
    */

    /*
    private static String GetXmlVersionFromHead(String head)
    {
        String versionStart = "version=\"";
        int index = head.indexOf(versionStart);
        if (index < 0)
        {
            return "1.0";
        }
        else
        {
            int indexOfValue = index + versionStart.length();
            int index2 = head.indexOf("\"", indexOfValue);

            if (index2 < 0)
            {
                return "1.0";
            }
            else
            {
                String version = head.substring(indexOfValue, indexOfValue + index2 - indexOfValue);
                return version;
            }
        }
    }
     */
    
    private static String GetXmlEncodingFromHead(String head)
    {
        String encodingStart = "encoding=\"";
        int index = head.indexOf(encodingStart);
        if (index < 0)
        {
            return "1.0";
        }
        else
        {
            int indexOfValue = index + encodingStart.length();
            int index2 = head.indexOf("\"", indexOfValue);

            if (index2 < 0)
            {
                return "1.0";
            }
            else
            {
                String encoding = head.substring(indexOfValue, indexOfValue + index2 - indexOfValue);
                return encoding;
            }
        }
    }

    private static String GetXmlHead(Reader sr) throws XmlParseException, IOException
    {
        char[] buffTmp = new char[100];
        int readCnt = 0;

        readCnt = sr.read(buffTmp, 0, buffTmp.length);
        if (readCnt <= 0)
        {
            throw new XmlParseException("Xml is empty.");
        }

        if (readCnt < 7) return "";

        int index1 = -1;
        int index2 = -1;
        char chr = '\0';
        char chr2 = '\0';
        for(int i = 0; i < buffTmp.length - 1; i++)
        {
            chr = buffTmp[i];
            chr2 = buffTmp[i+1];
            if(chr != ' ' &&  chr != '\t')
            {
                if(index1 == -1)
                {
                    if(chr == '<' && chr2 == '?')
                    {
                        index1 = i;
                        i++;
                        continue;
                    }
                    else
                    {
                        return "";
                    }
                }
                else
                {
                    if(chr == '?' && chr2 == '>')
                    {
                        index2 = i;
                        break;
                    }
                }
            }
        }

        if (index1 != -1)
        {
            if (index2 != -1)
            {
                String head = new String(buffTmp, index1, index2 + 1 + 1 - index1);
                if (!head.toLowerCase().startsWith("<?xml"))
                {
                    throw new XmlParseException("Format of xml head(<?xml ..... ?>) is not correct.");
                }

                return head;
            }
            else
            {
                throw new XmlParseException("Format of xml head(<?xml ..... ?>) is not correct.");
            }
        }
        else
        {
            return "";
        }

    }
}
