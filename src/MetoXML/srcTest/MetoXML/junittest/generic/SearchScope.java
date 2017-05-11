package MetoXML.junittest.generic;

import java.io.Serializable;

public class SearchScope implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 133388133653879289L;

	private int _startIndex = 0;
	
	private int _endIndex = 0;
	
	public int getSearchCount() {
		return _endIndex - _startIndex + 1;
	}

	public int getStartIndex() {
		return _startIndex;
	}

	public void setStartIndex(int startIndex) {
		_startIndex = startIndex;
	}

	public int getEndIndex() {
		return _endIndex;
	}

	public void setEndIndex(int endIndex) {
		_endIndex = endIndex;
	}
}