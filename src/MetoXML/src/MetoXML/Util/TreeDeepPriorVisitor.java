package MetoXML.Util;

public abstract class TreeDeepPriorVisitor {
    protected abstract void ForwardToNode(ITreeNode node, int depth, boolean isLeafNode);
    protected abstract void BackwardToNode(ITreeNode node, int depth);

    /// <summary>
    /// Deep prior search
    /// </summary>
    /// <param name="node"></param>
    public void VisitAllNode(ITreeNode rootNode)
    {
        ITreeNode nodeTmp = rootNode;
        boolean isBackward = false;
        int depth = 0;

        while (nodeTmp != null)
        {
            if (nodeTmp.GetFirstChild() != null)
            {
                if (isBackward)
                {
                    if (depth < 0) break;

                    BackwardToNode(nodeTmp, depth);

                    isBackward = false;

                    if (nodeTmp.GetNext() == null)
                    {
                        isBackward = true;

                        nodeTmp = nodeTmp.GetParent();
                        depth--;
                    }
                    else
                    {
                        nodeTmp = nodeTmp.GetNext();
                    }
                }
                else
                {
                    ForwardToNode(nodeTmp, depth, false);

                    nodeTmp = nodeTmp.GetFirstChild();

                    depth++;
                }
            }
            else
            {
                //Leaf node
                ForwardToNode(nodeTmp, depth, true);

                if (nodeTmp.GetNext() == null)
                {
                    isBackward = true;
                    nodeTmp = nodeTmp.GetParent();
                    depth--;
                }
                else
                {
                    nodeTmp = nodeTmp.GetNext();
                }
            }
        }
    }
}
