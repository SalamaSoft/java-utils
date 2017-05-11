package MetoXML.junittest.generic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchPageResult<DataType> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5714139101262463026L;

	protected PageControl _pageControl = new PageControl();
	
	protected List<DataType> _dataList = new ArrayList<DataType>(); 
	
	public PageControl getPageControl() {
		return _pageControl;
	}

	public void setPageControl(PageControl page) {
		_pageControl = page;
	}

	public List<DataType> getDataList() {
		return _dataList;
	}

	public void setDataList(List<DataType> dataList) {
		_dataList = dataList;
	}
	
}

