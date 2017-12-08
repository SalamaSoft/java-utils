package MetoXML.Base;

public class XmlTagContent extends XmlTag{
    private String content = "";

	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}

	public XmlTagContent() {
    	super();
        this.type.setValue(TagType.ContentPart);
	}
	
    public XmlTagContent(String contentText)
    {
    	super();
        this.type.setValue(TagType.ContentPart);

        this.content = XmlContentEncoder.DecodeContent(contentText);
    }

}
