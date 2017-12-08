package MetoXML.Util;

public interface ITreeNode {
    ITreeNode GetParent();
    ITreeNode GetNext();
    ITreeNode GetPrevious();
    ITreeNode GetFirstChild();
    ITreeNode GetLastChild();
}
