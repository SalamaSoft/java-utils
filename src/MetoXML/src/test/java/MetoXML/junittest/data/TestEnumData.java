package MetoXML.junittest.data;

public class TestEnumData {
    public enum ActivityType {Application, LoopApplication, SubFlow};

    private String _id = "";

    private String _name = "";

    private String _description = "";

    private ActivityType _type = ActivityType.Application;

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public ActivityType getType() {
		return _type;
	}

	public void setType(ActivityType type) {
		_type = type;
	}

    
}
