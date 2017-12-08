package MetoXML.Base;

public class XmlContentEncoder {
    private static char[] MetaChars = new char[] { '<', '>', '&', '\'', '"' };
    //private static String[] MetaCharStrs = new String[] { "<", ">", "&", "'", "\"" };
    private static String[] EncodeStrs = new String[] { "&lt;", "&gt;", "&amp;", "&apos;", "&quot;" };

    public static String EncodeContent(String content)
    {
        if(content == null) return content;

        StringBuilder sb = new StringBuilder();
        int indexOfMeta = 0;

        for (int i = 0; i < content.length(); i++)
        {
            for (indexOfMeta = 0; indexOfMeta < 5; indexOfMeta++)
            {
                if (content.charAt(i) == MetaChars[indexOfMeta])
                {
                    break;
                }
            }

            if (indexOfMeta < 5)
            {
                sb.append(EncodeStrs[indexOfMeta]);
            }
            else
            {
                sb.append(content.charAt(i));
            }
        }

        return sb.toString();
    }

    public static String DecodeContent(String content)
    {
        if (content == null) return content;

        StringBuilder sb = new StringBuilder();
        int indexOfEncodeStr = 0;
        String strTmp = "";
        int lenTmp = 0;

        for (int i = 0; i < content.length(); i++)
        {
            if (content.charAt(i) == '&')
            {
                for (indexOfEncodeStr = 0; indexOfEncodeStr < EncodeStrs.length; indexOfEncodeStr++)
                {
                    lenTmp = EncodeStrs[indexOfEncodeStr].length();
                    if ((i + lenTmp) <= content.length())
                    {
                        strTmp = content.substring(i, i + lenTmp);
                        if (strTmp.equals(EncodeStrs[indexOfEncodeStr]))
                        {
                            break;
                        }
                    }
                }

                if (indexOfEncodeStr < EncodeStrs.length)
                {
                    sb.append(MetaChars[indexOfEncodeStr]);
                    i += EncodeStrs[indexOfEncodeStr].length() - 1;
                }
                else
                {
                    sb.append(content.charAt(i));
                }
            }
            else
            {
                sb.append(content.charAt(i));
            }
        }

        return sb.toString();
    }
}
