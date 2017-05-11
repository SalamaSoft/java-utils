package MetoXML.Base;

import java.util.ArrayList;
import java.util.List;

import CollectionCommon.ITreeNode;

public class XmlNode implements ITreeNode{
    private String name = "";

    private List<XmlNodeAttribute> attributes = new ArrayList<XmlNodeAttribute>();

    private XmlNode parentNode = null;

    private XmlNode previousNode = null;

    private XmlNode nextNode = null;

    private XmlNode firstChildNode = null;

    private XmlNode lastChildNode = null;

    private String content = "";

    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<XmlNodeAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<XmlNodeAttribute> attributes) {
		this.attributes = attributes;
	}

	public XmlNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(XmlNode parentNode) {
		this.parentNode = parentNode;
	}

	public XmlNode getPreviousNode() {
		return previousNode;
	}

	public void setPreviousNode(XmlNode previousNode) {
		this.previousNode = previousNode;
	}

	public XmlNode getNextNode() {
		return nextNode;
	}

	public void setNextNode(XmlNode nextNode) {
		this.nextNode = nextNode;
	}

	public XmlNode getFirstChildNode() {
		return firstChildNode;
	}

	public void setFirstChildNode(XmlNode firstChildNode) {
		this.firstChildNode = firstChildNode;
	}

	public XmlNode getLastChildNode() {
		return lastChildNode;
	}

	public void setLastChildNode(XmlNode lastChildNode) {
		this.lastChildNode = lastChildNode;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ITreeNode GetParent()
    {
        return parentNode;
    }

    public ITreeNode GetNext()
    {
        return nextNode;
    }

    public ITreeNode GetPrevious()
    {
        return previousNode;
    }

    public ITreeNode GetFirstChild()
    {
        return firstChildNode;
    }

    public ITreeNode GetLastChild()
    {
        return lastChildNode;
    }
}
