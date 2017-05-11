package MetoXML.junittest.generic;

public class PageControl {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8064693723946984716L;

	/**
	 * Record count of one page
	 */
	protected int _pageSize = 20;
	
	/**
	 * Total page count
	 */
	protected int _totalPageCount = 0;
	
	/**
	 * Current page index (Start from 1)
	 */
	protected int _currentPageIndex = 0;
	
	protected int _totalRecordCount = 0;

	public int getPageSize() {
		return _pageSize;
	}

	public void setPageSize(int pageSize) {
		_pageSize = pageSize;
	}

	public int getTotalPageCount() {
		return _totalPageCount;
	}

	public void setTotalPageCount(int totalPageCount) {
		_totalPageCount = totalPageCount;
	}


	public int getCurrentPageIndex() {
		return _currentPageIndex;
	}

	public void setCurrentPageIndex(int currentPageIndex) {
		_currentPageIndex = currentPageIndex;
	}
	
	public int getTotalRecordCount() {
		return _totalRecordCount;
	}

	public void setTotalRecordCount(int totalRecordCount) {
		_totalRecordCount = totalRecordCount;
	}
}
