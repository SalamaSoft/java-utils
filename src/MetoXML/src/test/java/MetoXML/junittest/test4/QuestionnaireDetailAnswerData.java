package MetoXML.junittest.test4;

public class QuestionnaireDetailAnswerData {
	// 回答ID
	private String answerId;
	// 回答内容
	private String answerContent;
	// 回答分值
	private String answerScore;
	// 回答详细ID
	private String answerDetailId;
	// 360度评估标尺答案
	private String answer360TestR;

	public String getAnswer360TestR() {
		return answer360TestR;
	}
	public void setAnswer360TestR(String answer360TestR) {
		this.answer360TestR = answer360TestR;
	}
	public String getAnswerDetailId() {
		return answerDetailId;
	}
	public void setAnswerDetailId(String answerDetailId) {
		this.answerDetailId = answerDetailId;
	}
	public String getAnswerId() {
		return answerId;
	}
	public void setAnswerId(String answerId) {
		this.answerId = answerId;
	}
	public String getAnswerContent() {
		return answerContent;
	}
	public void setAnswerContent(String answerContent) {
		this.answerContent = answerContent;
	}
	public String getAnswerScore() {
		return answerScore;
	}
	public void setAnswerScore(String answerScore) {
		this.answerScore = answerScore;
	}
}
