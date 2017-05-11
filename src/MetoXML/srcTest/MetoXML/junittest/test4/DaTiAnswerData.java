package MetoXML.junittest.test4;

import java.util.ArrayList;
import java.util.List;

public class DaTiAnswerData {
	private String tmType;
	
	public String getTmType() {
		return tmType;
	}

	public void setTmType(String tmType) {
		this.tmType = tmType;
	}

	private String testSyTime;
	
	public String getTestSyTime() {
		return testSyTime;
	}

	public void setTestSyTime(String testSyTime) {
		this.testSyTime = testSyTime;
	}
	
	private String post;

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	private String currentStrObject;

	public String getCurrentStrObject() {
		return currentStrObject;
	}

	public void setCurrentStrObject(String currentStrObject) {
		this.currentStrObject = currentStrObject;
	}
	
	private String preStrObject;
	
	public String getPreStrObject() {
		return preStrObject;
	}

	public void setPreStrObject(String preStrObject) {
		this.preStrObject = preStrObject;
	}
	
	private String nextStrObject;

	public String getNextStrObject() {
		return nextStrObject;
	}

	public void setNextStrObject(String nextStrObject) {
		this.nextStrObject = nextStrObject;
	}

	private String actionFlag;
	
	public String getActionFlag() {
		return actionFlag;
	}

	public void setActionFlag(String actionFlag) {
		this.actionFlag = actionFlag;
	}

	private List<QuestionnaireDetailAnswerData> questionnaireDetailAnswerList = new ArrayList<QuestionnaireDetailAnswerData>();

	public List<QuestionnaireDetailAnswerData> getQuestionnaireDetailAnswerList() {
		return questionnaireDetailAnswerList;
	}

	public void setQuestionnaireDetailAnswerList(
			List<QuestionnaireDetailAnswerData> questionnaireDetailAnswerList) {
		this.questionnaireDetailAnswerList = questionnaireDetailAnswerList;
	}
}
