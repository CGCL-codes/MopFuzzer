package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static edu.hust.xzf.mutator.deoptpatterns.OptimisticTypeAssertions.getRandomEntryFromMap;

public class CommonSubexpressionElimination extends MutateTemplate {

//    Replacing "(con1+con2)-x" with "(con1-x)+con2"
//    Replacing "(a+c)-(b+d)" with "(a-b)+(c-d)"
//    Replacing "(a+c)" with "(a-b)+(b+c)"
//    Replacing "(a-c)" with "(a-b)+(b-c)"
//    Replacing "(a+c)" with "(a-b)+(c+b)"
//    Replacing "(c-b)" with "(a-b)+(c-a)"
//    Replacing "(x-y)" with "x+(0-y)" or "(x-y)" with "(0-y)+x"
//    Replacing "x" with "(x-y)+y" or "x" with "y+(x-y)"
//    Replacing "-x" with "(~x+1)", assuming "~x" represents "x^(-1)" as indicated, which is not a standard representation, but we'll go with your definition.


    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        Map<String, ITree> opMapTree = new HashMap<>();
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        identifyAllOp(tree, opMapTree);
        var entry = getRandomEntryFromMap(opMapTree);
        if (entry == null) return;
        String opStr = entry.getKey();
        ITree opTree = entry.getValue();
        ITree parent = opTree.getParent();
        int index = parent.getChildPosition(opTree);
        ITree left = parent.getChild(index - 1);
        ITree right = parent.getChild(index + 1);

        String leftStr = this.getSubSuspiciouCodeStr(left.getPos(), left.getEndPos());
        String rightStr = this.getSubSuspiciouCodeStr(right.getPos(), right.getEndPos());

        if (Checker.isStringLiteral(left.getType()) || Checker.isStringLiteral(right.getType())) return;
        if (isStringVar(leftStr) || isStringVar(rightStr)) return;

        if (Checker.isPrefixExpression(left.getType()) || Checker.isPostfixExpression(left.getType()))
            leftStr = "(" + leftStr + ")";
        if (Checker.isPrefixExpression(right.getType()) || Checker.isPostfixExpression(right.getType()))
            rightStr = "(" + rightStr + ")";

        String fixedCodeStr = "";
        fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, left.getPos());
//        fixedCodeStr += "(";
        Random random = new Random();
        int r = random.nextInt(3000);
        switch (opStr) {
            case "+" ->
                // x + y -> ((x - r) + (y + r))
                    fixedCodeStr += "((" + leftStr + "-" + r + ")+(" + rightStr + "+" + r + "))";
            case "-" ->
                // x - y -> ((x + r) - (y + r))
                    fixedCodeStr += "((" + leftStr + "+" + r + ")-(" + rightStr + "+" + r + "))";
            case "*" ->
                // x * y -> ((x - r) * y + y * r)
                    fixedCodeStr += "((" + leftStr + "-" + r + ")*" + rightStr + "+" + rightStr + "*" + r + ")";
            case "/" ->
                // x / y ->  (r / y + (x - r) / y)
                    fixedCodeStr += "(" + r + "/" + rightStr + "+(" + leftStr + "-" + r + ")/" + rightStr + ")";
            case "%" ->
                // x % y = (x - y * (x / y))
                    fixedCodeStr += "(" + leftStr + "-" + rightStr + "*(" + leftStr + "/" + rightStr + "))";
//            case ">>", ">>>", "<<", "<<<" ->
//                // x << y = ((x << (y - r)) << r)
//                    fixedCodeStr += "((" + leftStr + opStr + "(" + rightStr + "-" + r + "))" + opStr + r + ")";
            case "&" ->
                // x & y = (~((~x) | (~y)))
                    fixedCodeStr += "(~((~" + leftStr + ")|(~" + rightStr + ")))";
            case "|" ->
                // x | y = (~((~x) & (~y)))
                    fixedCodeStr += "(~((~" + leftStr + ")&(~" + rightStr + ")))";
            case "^" ->
                // x ^ y = ((x & (~y)) | ((~x) & y))
                    fixedCodeStr += "((" + leftStr + "&(~" + rightStr + "))|((~" + leftStr + ")&" + rightStr + "))";
        }

        fixedCodeStr += this.getSubSuspiciouCodeStr(right.getEndPos(), suspCodeEndPos);
        generatePatch(fixedCodeStr);
    }

    private void identifyAllOp(ITree tree, Map<String, ITree> opMapTree) {
        if (Checker.isOperator(tree.getType())
                && Checker.isInfixExpression(tree.getParent().getType())) {
            String op = tree.getLabel();
            if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%")) {
                opMapTree.put(op, tree);
            } else if (op.equals("&") || op.equals("|") || op.equals("^")) {
                ITree parent = tree.getParent();
                int index = parent.getChildPosition(tree);
                ITree left = parent.getChild(index - 1);
                ITree right = parent.getChild(index + 1);
                if (!Checker.isBooleanLiteral(left.getType()) && !Checker.isBooleanLiteral(right.getType())) {
                    String leftStr = this.getSubSuspiciouCodeStr(left.getPos(), left.getEndPos());
                    String rightStr = this.getSubSuspiciouCodeStr(right.getPos(), right.getEndPos());

                    String lvarType = varTypesMap.get(leftStr);
                    if (lvarType == null)
                        lvarType = varTypesMap.get("this." + leftStr);
                    String rvarType = varTypesMap.get(rightStr);
                    if (rvarType == null)
                        rvarType = varTypesMap.get("this." + rightStr);

                    if (!"boolean".equals(lvarType) && !"boolean".equals(rvarType))
                        opMapTree.put(op, tree);
                }
            }
        }
        for (ITree child : tree.getChildren())
            identifyAllOp(child, opMapTree);
    }

    boolean isStringVar(String varName) {
        String varType = varTypesMap.get(varName);
        if (varType == null)
            varType = varTypesMap.get("this." + varName);
        if (varType == null)
            return false;
        return varType.equals("String");
    }
}
