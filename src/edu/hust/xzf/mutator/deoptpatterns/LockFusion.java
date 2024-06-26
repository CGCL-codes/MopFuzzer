package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.Random;

public class LockFusion extends MutateTemplate {
    protected static ITree isStatementInSynchronizedBlock(ITree suspCodeTree) {
        ITree parent = suspCodeTree.getParent();
        if (Checker.isSynchronizedStatement(parent.getType()))
            return parent;
        return null;
    }

    @Override
    public void generatePatches() {
        ITree suspCodeTree = this.getSuspiciousCodeTree();
        ITree parent = isStatementInSynchronizedBlock(suspCodeTree);
        if (parent == null) return;
        var children = parent.getChildren();
        // no other block
        if (children.size() == 2) return;
        int index = parent.getChildPosition(suspCodeTree);
        ITree condition = parent.getChild(0);
        String synchronizedStr = this.getFileCodeStr(condition.getPos(), condition.getEndPos());
        String fixedCodeStr = "\n}synchronized(" + synchronizedStr + "){\n";


        for (int i = 1; i < children.size(); i++) {
            var child = children.get(i);
            if (child == suspCodeTree) break;
            if (Checker.isVariableDeclarationStatement(child.getType()))
                fixedCodeStr = fixedCodeStr + this.getFileCodeStr(child.getPos(), child.getEndPos()) + "\n";
        }

        boolean flag1 = false, flag2 = false;

        if (index >= 2) { // there is a block before
            flag1 = true;
            // no variable declaration statement before
            for (int i = 0; i <= index - 1; i++) {
                int preChildType = children.get(i).getType();
                if (Checker.isVariableDeclarationStatement(preChildType))
                    flag1 = false;
            }
        }
        if (index < children.size() - 2) // there is a block after
            flag2 = true;
        if (!flag1 && !flag2) return;

        if (Checker.isVariableDeclarationStatement(suspCodeTree.getType())) {
            if (flag1) {
                generatePatch(suspCodeEndPos, fixedCodeStr);
                offset += 2;
            }
        } else {

            int select = new Random().nextInt(2);
            if (select == 0 && flag1) {
                // insert before the anchor
                generatePatch(suspCodeEndPos, fixedCodeStr);
                offset += 2;
            } else if (flag2) {
                // insert after the anchor
                generatePatch(suspCodeStartPos, suspCodeEndPos, "", fixedCodeStr);
            } else if (flag1) {
                generatePatch(suspCodeEndPos, fixedCodeStr);
                offset += 2;
            }
        }
    }
}
