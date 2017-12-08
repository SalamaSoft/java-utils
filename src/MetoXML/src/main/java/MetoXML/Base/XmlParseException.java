package MetoXML.Base;

public class XmlParseException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7799129110968416133L;
	private String _errorMsg = "";

    public XmlParseException(String errorMsg)
    {
        this._errorMsg = errorMsg;
    }

    @Override
    public String getMessage() {
    	return _errorMsg;
    }
}
