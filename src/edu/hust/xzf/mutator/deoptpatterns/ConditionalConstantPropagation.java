package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.Random;

public class ConditionalConstantPropagation extends MutateTemplate {


//    final List<String > primitiveTypes = new ArrayList<>(){"double", "float", "long", "int", "short", "char", "byte"};


    @Override
    public void generatePatches() {
        ITree stmtAst = this.getSuspiciousCodeTree();
        if (!Checker.isExpressionStatement(stmtAst.getType())) return;
        ITree assignmentExp = stmtAst.getChild(0);
        if (!Checker.isAssignment(assignmentExp.getType())) return;
        ITree var = assignmentExp.getChild(0);
        ITree op = assignmentExp.getChild(1);
        String opExp = op.getLabel();
        ITree exp = assignmentExp.getChild(2);
        String varName = var.getLabel();
        ContextReader.readAllVariablesAndFields(stmtAst, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        String varType = varTypesMap.get(varName);
        if (varType == null)
            varType = varTypesMap.get("this." + varName);
        if (varType == null) return;
        if ("int".equals(varType) || "Integer".equals(varType) || "byte".equalsIgnoreCase(varType)
                || "char".equalsIgnoreCase(varType) || "Character".equals(varType)
                || "short".equalsIgnoreCase(varType) || "long".equalsIgnoreCase(varType)
                || "float".equalsIgnoreCase(varType) || "double".equalsIgnoreCase(varType)) {
            if (varType.equals("Integer"))
                varType = "int";
            else if (varType.equals("Character"))
                varType = "char";
            else
                varType = varType.toLowerCase();

            String fixedCodeStr = "";
            String newVar = generateUniqueVarName();
//            int pos1 = var.getPos() + var.getLength();
            int pos2 = exp.getPos();
            String expStr = getSubSuspiciouCodeStr(pos2, suspCodeEndPos - 1);
//            String subSuspCodeStr = getSubSuspiciouCodeStr(suspCodeStartPos, pos2 - 1);
            int random = new Random().nextInt(100);
            fixedCodeStr += varType + " " + newVar + "=(" + varType + ")(" + expStr + "-(" + random + "));\n";
            fixedCodeStr += "if (" + newVar + " <= (" + expStr + ")) {\n";
            if (varType.equals("byte") || varType.equals("short") || varType.equals("char")) {
                fixedCodeStr += varName + opExp + " (" + varType + ")(" + newVar + "+" + random + ");\n}";
            } else
                fixedCodeStr += varName + opExp + " " + newVar + "+" + random + ";\n}";

            if (varType.equals("double"))
                fixedCodeStr += "else{" + varName + "=0.0;}";
            else if (varType.equals("float"))
                fixedCodeStr += "else{" + varName + "=0.0f;}";
            else if (varType.equals("long"))
                fixedCodeStr += "else{" + varName + "=0L;}";
            else
                fixedCodeStr += "else{" + varName + "=0;}";

            generatePatch(fixedCodeStr);
//            offset += 2;
        }
    }


//    private ITree identifyAdjacentAssignment(ITree suspCodeTree) {
//        ITree adj = null;
//        ITree parent = suspCodeTree.getParent();
//        int pos = parent.getChildPosition(suspCodeTree);
//        var childern = parent.getChildren();
//        if ((pos - 1) >= 0
//                && Checker.isExpressionStatement(childern.get(pos - 1).getType())
//                && Checker.isAssignment(childern.get(pos - 1).getChild(0).getType())) {
//            var varName = childern.get(pos - 1).getChild(0).getLabel();
//            String varType = ContextReader.readVariableType(suspCodeTree, varName);
//            if (varType != null && varType.equals(this.varType)) {
//                adj = childern.get(pos - 1);
//                return adj;
//            }
//        }
//        if (pos + 1 < childern.size()
//                && Checker.isExpressionStatement(childern.get(pos + 1).getType())
//                && Checker.isAssignment(childern.get(pos + 1).getChild(0).getType())) {
//            var varName = childern.get(pos + 1).getChild(0).getLabel();
//            String varType = ContextReader.readVariableType(suspCodeTree, varName);
//            if (varType != null && varType.equals(this.varType)) {
//                adj = childern.get(pos + 1);
//                return adj;
//            }
//            adj = childern.get(pos + 1);
//        }
//        return adj;
//    }

}
