package MetoXML.Util;

public class StringUtil {
	public static int IndexOfAny(String str, char[] chars, int startIndex) {
		if(str == null || str.length() == 0) return -1;
		
		int index = -1;
		char chr = 0;
		boolean isMatched = false;
		
		for(int i = startIndex; i < str.length(); i++) {
			chr = str.charAt(i);
			
			isMatched = false;
			for(int k = 0; k < chars.length; k++) {
				if(chars[k] == chr) {
					isMatched = true;
					break;
				}
			}
			
			if(isMatched) {
				index = i;
				break;
			}
		}
		
		return index;
	}
}
