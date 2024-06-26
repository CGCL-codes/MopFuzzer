package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.HashMap;
import java.util.Map;

import static edu.hust.xzf.mutator.deoptpatterns.OptimisticTypeAssertions.getRandomEntryFromMap;

public class OperatorStrengthReduction extends MutateTemplate {
    @Override
    public void generatePatches() {
        ITree suspCodeTree = this.getSuspiciousCodeTree();
        Map<String, ITree> maps = new HashMap<>();
        identifyExpectOperator(suspCodeTree, maps);
        var random = getRandomEntryFromMap(maps);
        ITree operator = random.getValue();
        ITree parent = operator.getParent();
        int index = parent.getChildPosition(operator);
        ITree rexp = parent.getChild(index + 1);
        if (Checker.isNumberLiteral(rexp.getType())) {
            double powof2;
            String varStr = rexp.getLabel();
            if (varStr.startsWith("0x") || varStr.startsWith("0X"))
                powof2 = Math.pow(2, Integer.decode(varStr));
            else if (varStr.endsWith("F") || varStr.endsWith("f"))
                powof2 = Math.pow(2, Float.parseFloat(varStr));
            else if (varStr.endsWith("D") || varStr.endsWith("d") || varStr.contains("."))
                powof2 = Math.pow(2, Double.parseDouble(varStr));
            else if (varStr.endsWith("L") || varStr.endsWith("l"))
                powof2 = Math.pow(2, Long.parseLong(varStr));
            else {
                powof2 = Math.pow(2, Integer.parseInt(varStr));
            }

            int posOp = operator.getPos();
            int expEnd = rexp.getEndPos();
            String newStr = "";
            newStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, posOp);

            int powInt = (int) powof2;

            switch (random.getKey()) {
                case "<<" -> newStr += " * " + powInt;
                case ">>" -> newStr += " / " + powInt;
            }
            newStr += this.getSubSuspiciouCodeStr(expEnd, suspCodeEndPos);
            this.generatePatch(newStr);
        }
    }

    private void identifyExpectOperator(ITree tree, Map<String, ITree> maps) {
        if (Checker.isOperator(tree.getType())) {
            String op = tree.getLabel();
            switch (op) {
                case ">>", ">>>":
                    maps.put(">>", tree);
                case "<<", "<<<":
                    maps.put("<<", tree);
            }
        }
        for (ITree child : tree.getChildren()) {
            identifyExpectOperator(child, maps);
        }
    }


}
