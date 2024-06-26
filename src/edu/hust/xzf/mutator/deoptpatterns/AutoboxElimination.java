package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoboxElimination extends MutateTemplate {

    String varType;

    @Override
    public void generatePatches() {
        ITree codeAst = this.getSuspiciousCodeTree();
        if (Checker.isVariableDeclarationStatement(codeAst.getType())) {
            ITree type = null;
            List<ITree> children = codeAst.getChildren();
            for (ITree child : children) {
                if (Checker.isModifier(child.getType())) continue;
                type = child;
                break;
            }
            String fixedCodeStr = "";
            String dataType = type.getLabel();

            // skip serializable type and var keyword
            if (dataType.contains("<") || dataType.equals("var")) return;

            fixedCodeStr += dataType + " ";
            String fullType = switch (dataType) {
                case "int" -> "Integer.valueOf(";
                case "float" -> "Float.valueOf(";
                case "long" -> "Long.valueOf(";
                case "double" -> "Double.valueOf(";
                case "boolean" -> "Boolean.valueOf(";
                case "char" -> "Character.valueOf((char)";
                case "short" -> "Short.valueOf((short)";
                case "byte" -> "Byte.valueOf((byte)";
                default -> "(" + dataType + ")(";
            };
            int index = codeAst.getChildPosition(type) + 1;
            for (; index < children.size(); index++) {
                ITree varDefine = children.get(index);
                ITree var = varDefine.getChild(0);
                ITree exp = varDefine.getChild(1);
                int varStart = var.getPos();
                int varEnd = var.getEndPos();
                String varName = this.getSubSuspiciouCodeStr(varStart, varEnd);
                int expStart = exp.getPos();
                int expEnd = expStart + exp.getLength();
                String expStr = this.getSubSuspiciouCodeStr(expStart, expEnd);
                fixedCodeStr += varName + "=" + fullType + expStr + ")";
                if (index != children.size() - 1)
                    fixedCodeStr += ",";
                else fixedCodeStr += ";";
            }
            generatePatch(fixedCodeStr);
        } else if (Checker.isExpressionStatement(codeAst.getType())) {
            ITree assignmentExp = codeAst.getChild(0);
            if (Checker.isAssignment(assignmentExp.getType())) {
                ContextReader.readAllVariablesAndFields(codeAst, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
                String opStr = assignmentExp.getChild(1).getLabel();
                if (!opStr.equals("=")) return;
                ITree var = assignmentExp.getChild(0);
                String varName = var.getLabel();
                String varStr = this.getSubSuspiciouCodeStr(var.getPos(), var.getEndPos());
                if (Checker.isQualifiedName(var.getType())) {
                    varName = varName.substring(varName.lastIndexOf(".") + 1);
                }
                String varType = varTypesMap.get(varName);
                if (varType == null) {
                    varType = varTypesMap.get("this." + varName);
                }
                if (varType != null) {
                    String fullType = switch (varType) {
                        case "int" -> "Integer.valueOf(";
                        case "float" -> "Float.valueOf(";
                        case "long" -> "Long.valueOf(";
                        case "double" -> "Double.valueOf(";
                        case "boolean" -> "Boolean.valueOf(";
                        case "char" -> "Character.valueOf((char)";
                        case "short" -> "Short.valueOf((short)";
                        case "byte" -> "Byte.valueOf((byte)";
                        default -> null;
                    };
                    if (fullType != null) {
                        ITree exp = assignmentExp.getChild(2);
                        int pos2 = exp.getPos();
                        String expStr = getSubSuspiciouCodeStr(pos2, suspCodeEndPos - 1);
                        String fixedCodeStr = varStr + " = " + fullType + expStr + ");";
                        generatePatch(fixedCodeStr);
                    }
                }
            }
        }
        if (getPatch() == null) {
            // select a primitive type variable and replace it with a wrapper class
            List<ITree> suspVars = new ArrayList<>();
            ContextReader.identifySuspiciousVariables(codeAst, suspVars, new ArrayList<>());
            if (suspVars.isEmpty()) return;
            ITree variable = getRandomElementFromList(suspVars);
            if (variable == null)
                return;
            String fixedCodeStr = "";
            String varStr = ContextReader.readVariableName(variable);
            int varStart = variable.getPos();
            int varEnd = variable.getEndPos();
            fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, varStart);
            String fullStr = switch (varType) {
                case "int" -> "(int)Integer.valueOf(";
                case "float" -> "(float)Float.valueOf(";
                case "long" -> "(long)Long.valueOf(";
                case "double" -> "(double)Double.valueOf(";
                case "boolean" -> "(boolean)Boolean.valueOf(";
                case "char" -> "(char) Character.valueOf((char)";
                case "short" -> "(short)Short.valueOf((short)";
                case "byte" -> "(byte)Byte.valueOf((byte)";
                default -> null;
            };
            if (fullStr == null) return;
            fixedCodeStr += fullStr + varStr + ")";
            fixedCodeStr += this.getSubSuspiciouCodeStr(varEnd, suspCodeEndPos);
            generatePatch(fixedCodeStr);
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
            if (varType == null || varType.equals("Class") || varType.equals("Entry")
                    || varType.equals("T") || varType.equals("var")) {
                list.remove(randomIndex);
                continue;
            }
            int parentType = select.getParent().getType();
            if (Checker.isPrefixExpression(parentType) || Checker.isPostfixExpression(parentType)) {
                list.remove(randomIndex);
                continue;
            }
            if ("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
                    || "float".equals(varType) || "double".equals(varType) || "char".equals(varType)) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }

}
