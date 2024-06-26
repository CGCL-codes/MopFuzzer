package edu.hust.xzf.mutator.mutatetemplate;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.Dictionary;
import edu.hust.xzf.mutator.info.Patch;

import java.util.List;

/**
 * FixTemplate interface.
 *
 * @author kui.liu
 */
public interface IMutateTemplate_B {

    public String getSuspiciousCodeStr();

    public void setSuspiciousCodeStr(String suspiciousCodeStr);

    public ITree getSuspiciousCodeTree();

    public void setSuspiciousCodeTree(ITree suspiciousCodeTree);

    public void generatePatches();

    public List<Patch> getPatches();

    public String getSubSuspiciouCodeStr(int startPos, int endPos);

    public void setDictionary(Dictionary dic);
}
