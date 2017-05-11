package MetoXML.Base;

public class XmlDocument {
    /// <summary>
    /// Default verson is 1.0.
    /// </summary>
    private String version = "1.0";


    /// <summary>
    /// utf-8 is the default encoding
    /// </summary>
    private String encoding = "utf-8"; 


    private XmlNode rootNode = null;


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public String getEncoding() {
		return encoding;
	}


	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}


	public XmlNode getRootNode() {
		return rootNode;
	}


	public void setRootNode(XmlNode rootNode) {
		this.rootNode = rootNode;
	}
    
    

}
