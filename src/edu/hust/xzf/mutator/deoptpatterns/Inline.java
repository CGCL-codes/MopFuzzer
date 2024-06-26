package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static edu.hust.xzf.mutator.deoptpatterns.LoopUnswitch.isStaticMethod;

public class Inline extends MutateTemplate {

    List<ITree> suspVars = new ArrayList<>();
    private String newM = "";

    boolean isStatic = false;

    private int newMethodPos = -1;

    public static boolean isValidOp(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*")
                || op.equals("/") || op.equals("&") || op.equals("|")
                || op.equals("<<") || op.equals(">>") || op.equals("^");
    }

    /*
    number:
        Replacing x ^ x with 0 (bitwise XOR operation, where ^ denotes XOR)
        Replacing x * 2 / x with 2 when x ≠ 0 (division by itself results in 1 when the divisor is non-zero)
    single variable:
        Replacing x with fun(x)-con  fun(x) = x+con
        Replacing x with fun(x)
        fun(x) = x*1;x+0; x/1; x&x; x|x; x<<0; x>>0

    two variable:
    x op y = fun(x,y)
    fun(x,y)= x op y
    */
    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        isStatic = isStaticMethod(tree);

        ContextReader.identifySuspiciousVariables(tree, suspVars, new ArrayList<String>());
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        List<ITree> nums = new ArrayList<>();
        List<ITree> ops = new ArrayList<>();
        identifyNumberAndValidOP(tree, nums, ops);
        ITree select = getRandomElementFromList(suspVars, nums, ops);
        if (select == null)
            return;
        int type = select.getType();
        String fixedCodeStr = "";
        if (Checker.isNumberLiteral(type)) {
            ITree validVar = getRandomElementFromList(suspVars);
            String suspVarName = validVar.getLabel();
            if (suspVarName.startsWith("Name:"))
                suspVarName = suspVarName.substring(5);
            String varType = varTypesMap.get(suspVarName);
            if (varType == null) {
                varType = varTypesMap.get("this." + suspVarName);
            }
            String varStr = this.getSubSuspiciouCodeStr(validVar.getPos(),
                    validVar.getEndPos());
            String uniqueMethodName = this.generateUniqueMethodName();
            fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, select.getPos());
            fixedCodeStr += uniqueMethodName + "(" + varStr + ")";
            fixedCodeStr += this.getSubSuspiciouCodeStr(select.getEndPos(), suspCodeEndPos);
            generatePatch(fixedCodeStr);
            int number = Integer.parseInt(select.getLabel());
            generateNumberMethod(varType, uniqueMethodName, number);

            setIsInlined(newM);
            setImportPos(locateNewMethod(select));
        } else if (Checker.isOperator(type)) {
            String op = select.getLabel();
            ITree parent = select.getParent();
            int index = parent.getChildPosition(select);
            ITree left = parent.getChild(index - 1);
            ITree right = parent.getChild(index + 1);
            String lvarType;
            if (Checker.isNumberLiteral(left.getType())) {
                String lvar = left.getLabel();
                if (lvar.startsWith("0x") || lvar.startsWith("0X"))
                    lvarType = "int";
                else if (lvar.endsWith("F") || lvar.endsWith("f"))
                    lvarType = "float";
                else if (lvar.endsWith("D") || lvar.endsWith("d") || lvar.contains("."))
                    lvarType = "double";
                else if (lvar.endsWith("L") || lvar.endsWith("l"))
                    lvarType = "long";
                else {
                    lvarType = "int";
                }
            } else {
                String lvar = left.getLabel();
                if (lvar.startsWith("Name:"))
                    lvar = lvar.substring(5);
                lvarType = varTypesMap.get(lvar);
                if (lvarType == null) {
                    lvarType = varTypesMap.get("this." + lvar);
                }
                if (lvarType.equals("Boolean") || lvarType.equals("Integer") || lvarType.equals("Long")
                        || lvarType.equals("Float") || lvarType.equals("Double") || lvarType.equals("Short")
                        || lvarType.equals("Byte") || lvarType.equals("Character")) {
                    if (lvarType.equals("Integer"))
                        lvarType = "int";
                    else if (lvarType.equals("Character"))
                        lvarType = "char";
                    else
                        lvarType = lvarType.toLowerCase();
                }
            }
            String lvarStr = this.getSubSuspiciouCodeStr(left.getPos(), left.getEndPos());

            String rvarType;
            if (Checker.isNumberLiteral(right.getType())) {
                String rvar = right.getLabel();
                if (rvar.startsWith("0x") || rvar.startsWith("0X"))
                    rvarType = "int";
                else if (rvar.endsWith("F") || rvar.endsWith("f"))
                    rvarType = "float";
                else if (rvar.endsWith("D") || rvar.endsWith("d") || rvar.contains("."))
                    rvarType = "double";
                else if (rvar.endsWith("L") || rvar.endsWith("l"))
                    rvarType = "long";
                else {
                    rvarType = "int";
                }
            } else {
                String rvar = right.getLabel();
                if (rvar.startsWith("Name:"))
                    rvar = rvar.substring(5);
                rvarType = varTypesMap.get(rvar);
                if (rvarType == null) {
                    rvarType = varTypesMap.get("this." + rvar);
                }
                if (rvarType.equals("Boolean") || rvarType.equals("Integer") || rvarType.equals("Long")
                        || rvarType.equals("Float") || rvarType.equals("Double") || rvarType.equals("Short")
                        || rvarType.equals("Byte") || rvarType.equals("Character")) {
                    if (rvarType.equals("Integer"))
                        rvarType = "int";
                    else if (rvarType.equals("Character"))
                        rvarType = "char";
                    else
                        rvarType = rvarType.toLowerCase();
                }
            }
            String rvarStr = this.getSubSuspiciouCodeStr(right.getPos(), right.getEndPos());

            fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, left.getPos());
            String uniqueMethodName = this.generateUniqueMethodName();
            fixedCodeStr += uniqueMethodName + "(" + lvarStr + "," + rvarStr + ")";
            fixedCodeStr += this.getSubSuspiciouCodeStr(right.getEndPos(), suspCodeEndPos);
            generatePatch(fixedCodeStr);
            generateOpMethod(lvarType, rvarType, uniqueMethodName, op);
            setIsInlined(newM);
            setImportPos(locateNewMethod(select));

        } else {
            String suspVarName = select.getLabel();
            if (suspVarName.startsWith("Name:"))
                suspVarName = suspVarName.substring(5);
            String varType = varTypesMap.get(suspVarName);
            if (varType == null) {
                varType = varTypesMap.get("this." + suspVarName);
            }
            String varStr = this.getSubSuspiciouCodeStr(select.getPos(), select.getEndPos());
            String uniqueMethodName = this.generateUniqueMethodName();
            Random random = new Random();

            fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, select.getPos());
            int r = random.nextInt(2);
            if (r == 0) {
                int constant = Math.abs(random.nextInt(10000));
                if (varType.equals("char") || varType.equals("byte") || varType.equals("short"))
                    fixedCodeStr += "(" + varType + ")(" + uniqueMethodName + "(" + varStr + ")-" + constant + ")";
                else
                    fixedCodeStr += "(" + uniqueMethodName + "(" + varStr + ")-" + constant + ")";

                generateSingleVarMethod1(varType, uniqueMethodName, constant);
            } else {
                fixedCodeStr += uniqueMethodName + "(" + varStr + ")";
                generateSingleVarMethod2(varType, uniqueMethodName);
            }

            fixedCodeStr += this.getSubSuspiciouCodeStr(select.getEndPos(), suspCodeEndPos);
            generatePatch(fixedCodeStr);
            setIsInlined(newM);
            setImportPos(locateNewMethod(select));
        }
    }

    private void generateSingleVarMethod1(String varType, String methodName, int constant) {
        boolean r = new Random().nextBoolean();
        String methodStr = "\npublic " + (isStatic ? "static " : "") + (r ? "synchronized " : "") + varType + " " + methodName +
                "(" + varType + " var) {\n";
        String caseStrStart = "";
        String caseStrEnd = "";
        if (varType.equals("char") || varType.equals("byte") || varType.equals("short")) {
            caseStrStart += "(" + varType + ")(";
            caseStrEnd += ")";
        }
        methodStr += "return " + caseStrStart + "var + " + constant + caseStrEnd + ";\n";

//        methodStr += "return " + "var + " + constant + ";\n";
        methodStr += "}\n";
        newM = methodStr;
        offset += 4;
    }

    private void generateSingleVarMethod2(String varType, String methodName) {
        boolean r = new Random().nextBoolean();
        String methodStr = "\npublic " + (isStatic ? "static " : "") + (r ? "synchronized " : "") + varType + " " + methodName +
                "(" + varType + " var) {\n";
        String caseStrStart = "";
        String caseStrEnd = "";
        if (varType.equals("char") || varType.equals("byte") || varType.equals("short")) {
            caseStrStart += "(" + varType + ")(";
            caseStrEnd += ")";
        }
        int bound = 9;
        if (varType.equals("float") || varType.equals("double")) {
            bound = 4;
        }

        // x*1;x+0; x/1; x&x; x|x; x<<0; x>>0; x^0; x-0;
        switch (new Random().nextInt(bound)) {
            case 0 -> methodStr += "return " + caseStrStart + "var * 1" + caseStrEnd + ";\n";
            case 1 -> methodStr += "return " + caseStrStart + "var + 0" + caseStrEnd + ";\n";
            case 2 -> methodStr += "return " + caseStrStart + "var / 1" + caseStrEnd + ";\n";
            case 3 -> methodStr += "return " + caseStrStart + "var - 0" + caseStrEnd + ";\n";
            case 4 -> methodStr += "return " + caseStrStart + "var & var" + caseStrEnd + ";\n";
            case 5 -> methodStr += "return " + caseStrStart + "var | var" + caseStrEnd + ";\n";
            case 6 -> methodStr += "return " + caseStrStart + "var << 0" + caseStrEnd + ";\n";
            case 7 -> methodStr += "return " + caseStrStart + "var >> 0" + caseStrEnd + ";\n";
            case 8 -> methodStr += "return " + caseStrStart + "var ^ 0" + caseStrEnd + ";\n";
        }
        methodStr += "}\n";
        newM = methodStr;
        offset += 4;
    }

    private void generateNumberMethod(String varType, String methodName, int number) {
        boolean r = new Random().nextBoolean();
        String methodStr = "\npublic " + (isStatic ? " static " : "") + (r ? "synchronized " : "") + " int " + methodName +
                "(" + varType + " var) {\n";
        String caseStrStart = "";
        String caseStrEnd = "";
        boolean isLarge = false;
        if (varType.equals("long") || varType.equals("double") || varType.equals("float")) {
            caseStrStart += "(int)(";
            caseStrEnd += ")";
            isLarge = true;
        }

        if (number == 0 && !isLarge)
            methodStr += "return " + "var ^ var" + ";\n";
        else {
            String body = caseStrStart + "var * " + number + "/var" + caseStrEnd;
            methodStr += "return var == 0? " + number + ":" + body + ";\n";
        }
//            methodStr += "return " + "var * " + number + "/var" + ";\n";
        methodStr += "}\n";
        newM = methodStr;
        offset += 4;
    }

    private void generateOpMethod(String lvarType, String rvarType, String methodName, String op) {
        boolean r = new Random().nextBoolean();
        String methodStr = "\npublic " + (isStatic ? " static " : "") + (r ? "synchronized " : "") + lvarType + " " + methodName +
                "(" + lvarType + " var1," + rvarType + " var2) {\n";
        methodStr += "return (" + lvarType + ")(var1 " + op + " var2);\n";
        methodStr += "}\n";
        newM = methodStr;
        offset += 4;
    }

    private int locateNewMethod(ITree tree) {
        ITree parent = tree.getParent();
        while (!Checker.isMethodDeclaration(parent.getType())) {
            parent = parent.getParent();
        }
        return parent.getPos();
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
            String varType = varTypesMap.get(suspVarName);
            if (varType == null) {
                varType = varTypesMap.get("this." + suspVarName);
            }
            if (varType == null) {
                list.remove(randomIndex);
                continue;
            }
            if (("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
                    || "float".equals(varType) || "double".equals(varType) || "char".equals(varType))) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }

    private void identifyNumberAndValidOP(ITree tree, List<ITree> nums, List<ITree> ops) {
        int type = tree.getType();
        if (Checker.isNumberLiteral(type)) {
            String num = tree.getLabel();
            if (!(num.contains(".") || num.contains("F") || num.contains("f")
                    || num.contains("D") || num.contains("d") || num.contains("L") || num.contains("l")))
                nums.add(tree);
        } else if (Checker.isOperator(type)) {
            String op = tree.getLabel();
            if (isValidOp(op) && Checker.isInfixExpression(tree.getParent().getType())) {
                ITree parent = tree.getParent();
                int index = parent.getChildPosition(tree);
                ITree left = parent.getChild(index - 1);
                ITree right = parent.getChild(index + 1);
                if (checkValidOperand(left) && checkValidOperand(right)) {
                    ops.add(tree);
                }
            }
        }
        for (ITree child : tree.getChildren()) {
            identifyNumberAndValidOP(child, nums, ops);
        }

    }

    private ITree getRandomElementFromList(List<ITree> vars, List<ITree> nums
            , List<ITree> ops) {
        List<ITree> newlists = new ArrayList<>();
        for (ITree var : vars) {
            String suspVarName = var.getLabel();
            if (suspVarName.startsWith("Name:"))
                suspVarName = suspVarName.substring(5);
            String varType = varTypesMap.get(suspVarName);
            if (varType == null) {
                varType = varTypesMap.get("this." + suspVarName);
            }
            if (varType == null) {
                continue;
            }
            if (Checker.isPrefixExpression(var.getParent().getType()) || Checker.isPostfixExpression(var.getParent().getType()))
                continue;
            if (("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
                    || "float".equals(varType) || "double".equals(varType) || "char".equals(varType))) {
                newlists.add(var);
            }
        }
        if (!newlists.isEmpty())
            newlists.addAll(nums);
        newlists.addAll(ops);
        if (newlists.isEmpty())
            return null;
        Random random = new Random();
        return newlists.get(random.nextInt(newlists.size()));
    }

    private boolean checkValidOperand(ITree tree) {
        int type = tree.getType();
        if (Checker.isNumberLiteral(type)) {
            return true;
        } else if (Checker.isSimpleName(type) || Checker.isQualifiedName(type)) {
            String varName = tree.getLabel();
            if (varName.startsWith("Name:"))
                varName = varName.substring(5);
            if (allVarNamesList.contains(varName)) {
                return true;
            } else return allVarNamesList.contains("this." + varName);
        }
        return false;
    }

    public String getNewM() {
        return newM;
    }

    public void setNewM(String newM) {
        this.newM = newM;
    }

    public int getNewMethodPos() {
        return newMethodPos;
    }

    public void setNewMethodPos(int newMethodPos) {
        this.newMethodPos = newMethodPos;
    }
}
