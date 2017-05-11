package MetoXML.junittest.data;

import java.sql.Date;
import java.sql.Timestamp;

public class Test3Data {
	private Long l1 = Long.valueOf(0);

	private Date sqlDate = null;
	
	private Timestamp timeStamp = null;
	
	public Long getL1() {
		return l1;
	}

	public void setL1(Long l1) {
		this.l1 = l1;
	}

	public Date getSqlDate() {
		return sqlDate;
	}

	public void setSqlDate(Date sqlDate) {
		this.sqlDate = sqlDate;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	
}
