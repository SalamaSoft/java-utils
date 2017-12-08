package MetoXML.Base;

public class TagType {
	public final static int ContentPart = -1;
	public final static int HeadNode = 0; 
	public final static int NodeEnd = 2;
	public final static int NodeAllInOne = 4;
	public final static int NodeBegin = 6;
	
	private int value = 0;
	
	public TagType() {
		
	}
	
	public TagType(int val) {
		this.value = val;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean equals(TagType type) {
		return (value == type.getValue());
	}
}
