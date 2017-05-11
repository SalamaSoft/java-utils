package MetoXML;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import MetoXML.Base.XmlContentEncoder;
import MetoXML.Base.XmlDocument;
import MetoXML.Base.XmlNode;
import MetoXML.Base.XmlNodeAttribute;

public class XmlWriter {
    private final static String RETURN_LINE_STR = "\r\n";
    private final static String DEPTH_STR = "  ";

    public void WriteXmlDocument(XmlDocument xmlDocument, String filePath) throws FileNotFoundException, IOException
    {
        OutputStreamWriter sw = null;
        FileOutputStream fos = null;
        try
        {
        	fos = new FileOutputStream(filePath);
            Charset enc = Charset.forName(xmlDocument.getEncoding().toUpperCase());
            sw = new OutputStreamWriter(fos, enc);
            WriteXmlDocument(xmlDocument, sw);
        }
        finally
        {
        	if(fos != null) {
        		try {
					fos.close();
				} catch (IOException e) {
				}
        	}
            if (sw != null) {
            	try {
					sw.close();
				} catch (IOException e) {
				}
            }
        }
    }

    public void WriteXmlDocument(XmlDocument xmlDocument, ByteArrayOutputStream memStream) throws IOException
    {
        OutputStreamWriter sw = null;

        try
        {
            Charset enc = Charset.forName(xmlDocument.getEncoding().toUpperCase());
            sw = new OutputStreamWriter(memStream, enc);
            WriteXmlDocument(xmlDocument, sw);
        }
        finally
        {
            if (sw != null) {
            	try {
					sw.close();
				} catch (IOException e) {
				}
            }
        }
    }

    public void WriteXmlDocument(XmlDocument xmlDocument, OutputStreamWriter sw) throws IOException
    {
        //head
        sw.write(GetXmlHead(xmlDocument));
        sw.write(RETURN_LINE_STR);
        sw.flush();

        WriteXmlNode(xmlDocument.getRootNode(), 0, sw);
    }

    public void WriteXmlNode(XmlNode node, OutputStreamWriter sw) throws IOException
    {
        WriteXmlNode(node, 0, sw);
    }

    public String XmlNodeToString(XmlNode node, Charset encoding)
    throws IOException
    {

        ByteArrayOutputStream memStream = null;
        OutputStreamWriter sw = null;

        try
        {
        	memStream = new ByteArrayOutputStream();
            sw = new OutputStreamWriter(memStream, encoding);

            WriteXmlNode(node, sw);

            sw.flush();

            return new String(memStream.toByteArray(), encoding.name());
        }
        finally
        {
        	if(memStream != null) {
        		try {
        			memStream.close();
				} catch (IOException e) {
				}
        	}
            if (sw != null) {
            	try {
					sw.close();
				} catch (IOException e) {
				}
            }
        }
    	
    }
    
    private void WriteXmlNode(XmlNode node, int depth, OutputStreamWriter sw) throws IOException
    {
        XmlNode nodeTmp = node;
        int depthTmp = depth;
        boolean backwardFlg = false;

        while (nodeTmp != null)
        {
            if (nodeTmp.getFirstChildNode() != null)
            {
                if (backwardFlg)
                {
                    if (depthTmp < depth) break;

                    sw.write(GetNodeEndXml(nodeTmp, depthTmp));
                    sw.write(RETURN_LINE_STR);
                    sw.flush();

                    if (nodeTmp.getNextNode() != null)
                    {
                        nodeTmp = nodeTmp.getNextNode();
                        backwardFlg = false;
                    }
                    else
                    {
                        nodeTmp = nodeTmp.getParentNode();
                        backwardFlg = true;
                        depthTmp--;
                    }
                }
                else
                {
                    sw.write(GetNodeBeginXml(nodeTmp, depthTmp));
                    sw.write(RETURN_LINE_STR);
                    sw.flush();

                    nodeTmp = nodeTmp.getFirstChildNode();
                    backwardFlg = false;
                    depthTmp++;
                }
            }
            else
            {
                //leaf node
                sw.write(GetNodeBeginXml(nodeTmp, depthTmp));
                sw.write(XmlContentEncoder.EncodeContent(nodeTmp.getContent()));
                sw.write(GetNodeEndXml(nodeTmp, 0));
                sw.write(RETURN_LINE_STR);
                sw.flush();

                if (nodeTmp.getNextNode() != null)
                {
                    nodeTmp = nodeTmp.getNextNode();
                    backwardFlg = false;
                }
                else
                {
                    nodeTmp = nodeTmp.getParentNode();
                    backwardFlg = true;
                    depthTmp--;
                }
            }
        }//while
    }

    /* recursive search costs more memory. Abanded.
    private static void WriteXmlNode(XmlNode node, int depth, StreamWriter sw)
    {
        if (node.ChildNodes.Count > 0)
        {
            sw.WriteLine(GetNodeBeginXml(node, depth));
        }
        else
        {
            sw.Write(GetNodeBeginXml(node, depth));
        }

        if (node.ChildNodes.Count > 0)
        {
            for (int i = 0; i < node.ChildNodes.Count; i++)
            {
                WriteXmlNode(node.ChildNodes[i], depth + 1, sw);
            }
        }
        else
        {
            WriteNodeContent(node, sw);
        }

        if (node.ChildNodes.Count > 0)
        {
            sw.Write(RETURN_LINE_STR);
            sw.WriteLine(GetNodeEndXml(node, depth));
        }
        else
        {
            sw.WriteLine(GetNodeEndXml(node, depth));
        }
        
    }
    */

    private static String GetXmlHead(XmlDocument xmlDocument)
    {
        return "<?xml version=\"" + xmlDocument.getVersion() + "\" encoding=\"" + xmlDocument.getEncoding() + "\"?>";
    }

    private static String GetNodeBeginXml(XmlNode node, int depth)
    {
        String beginXml = GetNodeDepthStr(depth) + "<" + node.getName();

        for (int i = 0; i < node.getAttributes().size(); i++)
        {
            beginXml += " " + GetNodeAttributeXml(node.getAttributes().get(i));
        }
        beginXml += ">";

        return beginXml;
    }

    public static String GetNodeAttributeXml(XmlNodeAttribute attribute)
    {
        return attribute.getName() + "=" + "\"" + XmlContentEncoder.EncodeContent(attribute.getValue()) + "\"";
    }

    private static String GetNodeEndXml(XmlNode node, int depth)
    {
        return GetNodeDepthStr(depth) + "</" + node.getName() + ">";
    }

    private static String GetNodeDepthStr(int depth)
    {
        if (depth <= 0) return "";

        String depthStr = "";
        for (int i = 0; i < depth; i++)
        {
            depthStr += DEPTH_STR;
        }

        return depthStr;
    }

    /*
    private static void WriteNodeContent(XmlNode node, Writer sw) throws IOException
    {
        sw.write(XmlContentEncoder.EncodeContent(node.getContent()));
    }
    */
}
