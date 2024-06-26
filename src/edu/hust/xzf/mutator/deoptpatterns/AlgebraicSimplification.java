package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlgebraicSimplification extends MutateTemplate {
    String varType;
    /*
        Replacing x ^ x with 0 (bitwise XOR operation, where ^ denotes XOR)
        Replacing x * 2 / x with 2 when x ≠ 0 (division by itself results in 1 when the divisor is non-zero)

        Replacing x * 1 with x
        Replacing x + 0 with x
        Replacing x / 1 with x
        Replacing x & x with x (bitwise AND operation, where & denotes AND)
        Replacing x | x with x (bitwise OR operation, where | denotes OR)
        Replacing x << 0 with x (bitwise left shift by 0 has no effect)
        Replacing x >> 0 with x (bitwise right shift by 0 has no effect)
        Replacing (cons + x) - cons with x (adding and subtracting the same value cancel each other out)
     */


    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        List<ITree> suspVars = new ArrayList<>();
        ContextReader.identifySuspiciousVariables(tree, suspVars, new ArrayList<String>());
        if (suspVars.isEmpty()) return;
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        ITree variable = getRandomElementFromList(suspVars);
        if (variable == null)
            return;
        List<ITree> numbers = new ArrayList<>();
        identifyNumberLiteral(tree, numbers);
        int bound = 8;
        ITree number = null;
        if (!numbers.isEmpty()) {
            bound += 1;
            number = numbers.get(new Random().nextInt(numbers.size()));
        }
        int varStart = variable.getPos();
        int varEnd = variable.getEndPos();
        String varStr = ContextReader.readVariableName(variable);

        String fixedCodeStr = "";
        fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, varStart);

        Random random = new Random();
        int select = random.nextInt(bound);
        switch (select) {
            case 0 ->
                // Replacing x * 1 with x
                    fixedCodeStr += "(" + varType + ")(" + varStr + " * 1)";
            case 1 ->
                // Replacing x + 0 with x
                    fixedCodeStr += "(" + varType + ")(" + varStr + " + 0)";
            case 2 ->
                // Replacing x / 1 with x
                    fixedCodeStr += "(" + varType + ")(" + varStr + " / 1)";
            case 3 -> {
                int con = random.nextInt(200);
                // Replacing (cons + x) - cons with x (adding and subtracting the same value cancel each other out)
                fixedCodeStr += "(" + varType + ")((" + varStr + " + " + con + ") - " + con + ")";
            }
            case 4 -> {
                // Replacing x & x with x (bitwise AND operation, where & denotes AND)
                if (varType.equals("float") || varType.equals("double")) {
                    fixedCodeStr += "(" + varType + ")(" + varStr + " * 1)";
                } else
                    fixedCodeStr += "(" + varType + ")(" + varStr + " & " + varStr + ")";
            }
            case 5 -> {
                // Replacing x | x with x (bitwise OR operation, where | denotes OR)
                if (varType.equals("float") || varType.equals("double")) {
                    fixedCodeStr += "(" + varType + ")(" + varStr + " * 1)";
                } else
                    fixedCodeStr += "(" + varType + ")(" + varStr + " | " + varStr + ")";
            }
            case 6 -> {
                // Replacing (x << 0) with x (bitwise left shift by 0 has no effect)
                if (varType.equals("float") || varType.equals("double")) {
                    fixedCodeStr += "(" + varType + ")(" + varStr + " * 1)";
                } else
                    fixedCodeStr += "(" + varType + ")(" + varStr + " << 0)";
            }
            case 7 -> {
                // Replacing x >> 0 with x (bitwise right shift by 0 has no effect)
                if (varType.equals("float") || varType.equals("double")) {
                    fixedCodeStr += "(" + varType + ")(" + varStr + " * 1)";
                } else
                    fixedCodeStr += "(" + varType + ")(" + varStr + " >> 0)";
            }
            // Replacing (cons + x) - cons with x (adding and subtracting the same value cancel each other out)
            case 8 -> {
                // Replacing x ^ x with 0 (bitwise XOR operation, where ^ denotes XOR)
                if (number == null)
                    return;
                String numStr = number.getLabel();
                boolean isLarge = numStr.endsWith("L") || numStr.endsWith("l")
                        || numStr.endsWith("F") || numStr.endsWith("f")
                        || numStr.endsWith("D") || numStr.endsWith("d")
                        || numStr.endsWith(".");
                if (numStr.startsWith("0x") || numStr.startsWith("0X")) {
                    isLarge = false;
                }

                String fixedCodeStr2 = "";
                fixedCodeStr2 += this.getSubSuspiciouCodeStr(suspCodeStartPos, number.getPos());
                if (numStr.equals("0")) {
                    if (varType.equals("float") || varType.equals("double")) {
                        fixedCodeStr2 += "(" + varStr + " * 0" + ")";
                    } else
                        fixedCodeStr2 += "(" + varStr + " ^ " + varStr + ")";
                } else if (!isLarge) {
                    // normal integer number
                    String exp = "(" + varStr + "==0?" + numStr + ":" + "(" + varStr + " * " + numStr + " / " + varStr + "))";
                    if (varType.equals("long") || varType.equals("float") || varType.equals("double")) {
                        exp = "(int)" + exp;
                    }
                    fixedCodeStr2 += exp;
                } else {
                    fixedCodeStr2 += "(" + varType + ")(" + varStr + "==0?" + numStr + ":(" + varStr + " * " + numStr + " / " + varStr + "))";
                }

//
                fixedCodeStr2 += this.getSubSuspiciouCodeStr(number.getEndPos(), suspCodeEndPos);
                generatePatch(fixedCodeStr2);
                return;
            }
        }

        fixedCodeStr += this.getSubSuspiciouCodeStr(varEnd, suspCodeEndPos);
        generatePatch(fixedCodeStr);
    }

    private void identifyNumberLiteral(ITree tree, List<ITree> numbers) {
        if (Checker.isNumberLiteral(tree.getType())) {
            numbers.add(tree);
        }
        for (ITree child : tree.getChildren()) {
            identifyNumberLiteral(child, numbers);
        }
    }


    public ITree getRandomElementFromList(List<ITree> list) {
        // 如果List为空，返回null
        if (list.isEmpty()) {
            return null;
        }
        Random random = new Random();

        while (!list.isEmpty()) {
            int randomIndex = random.nextInt(list.size());
            var select = list.get(randomIndex);
            String suspVarName = select.getLabel();
            if (suspVarName.startsWith("Name:"))
                suspVarName = suspVarName.substring(5);
            varType = varTypesMap.get(suspVarName);
            if (varType == null) {
                varType = varTypesMap.get("this." + suspVarName);
            }
            if (varType == null) {
                list.remove(randomIndex);
                continue;
            }
            int ptype = select.getParent().getType();
            if (("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
                    || "float".equals(varType) || "double".equals(varType) || "char".equals(varType))
                    && !Checker.isPrefixExpression(ptype) && !Checker.isPostfixExpression(ptype)) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }


}
