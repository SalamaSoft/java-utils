package MetoXML;

public class NodeOperatorType {
    public final static int HeadNodeBegin = 0;
    public final static int HeadNodeEnd = 1;
    public final static int NodeType1Begin = 2;
    public final static int NodeTYpe1End = 3;
    public final static int NodeType2Begin = 4;
    public final static int NodeTYpe2End = 5;
    public final static int NodeType3Begin = 6;
    public final static int NodeTYpe3End = 7;

	private int value = 0;
	
	public NodeOperatorType() {
		
	}
	
	public NodeOperatorType(int val) {
		this.value = val;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean equals(NodeOperatorType type) {
		return (value == type.getValue());
	}
}
