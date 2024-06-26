package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EscapeAnalysis extends MutateTemplate {
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
            String str = "";
            String str2 = ")).value";
            String fixedCodeStr1 = "";
            String dataType = type.getLabel();

            switch (dataType) {
                case "int" -> str += "new MyInteger((int)(";
                case "float" -> str += "new MyFloat((float)(";
                case "long" -> str += "new MyLong((long)(";
                case "double" -> str += "new MyDouble((double)(";
                case "boolean" -> str += "new MyBoolean((boolean)(";
                case "char" -> str += "new MyChar((char)(";
                case "short" -> str += "new MyShort((short)(";
                case "byte" -> str += "new MyByte((byte)(";
                case "String" -> str += "new MyString((String)(";
                default -> {
                    return;
                }
            }
            String typeStr = getSubSuspiciouCodeStr(type.getPos(), type.getEndPos());
            fixedCodeStr1 += typeStr + " ";
            int index = codeAst.getChildPosition(type) + 1;
            for (; index < children.size(); index++) {
                ITree varDefine = children.get(index);
                ITree var = varDefine.getChild(0);
                ITree exp = varDefine.getChild(1);
//                int varStart = var.getPos();
//                int varEnd = varStart + var.getLength();
//                String varName = this.getSubSuspiciouCodeStr(varStart, varEnd);
                String expStr = this.getSubSuspiciouCodeStr(exp.getPos(), exp.getEndPos());
                String varDef = this.getSubSuspiciouCodeStr(var.getPos(), var.getEndPos());
                fixedCodeStr1 += varDef + " = " + str + expStr + str2;
                if (index != children.size() - 1)
                    fixedCodeStr1 += ",";
                else fixedCodeStr1 += ";";
            }
            generatePatch(fixedCodeStr1);
            setImport(dataType);
        } else if (Checker.isExpressionStatement(codeAst.getType())) {
            ITree assignmentExp = codeAst.getChild(0);
            if (Checker.isAssignment(assignmentExp.getType())) {
                ContextReader.readAllVariablesAndFields(codeAst, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
                ITree var = assignmentExp.getChild(0);
                String varName = var.getLabel();
                if (Checker.isQualifiedName(var.getType())) {
                    varName = varName.substring(varName.lastIndexOf(".") + 1);
                }
                String varType = varTypesMap.get(varName);
                if (varType == null) {
                    varType = varTypesMap.get("this." + varName);
                }
                if (varType != null) {
                    String str = "";
                    String str2 = ")).value";
                    String fixedCodeStr1 = "";
                    switch (varType) {
                        case "int" -> str += "new MyInteger((int)(";
                        case "float" -> str += "new MyFloat((float)(";
                        case "long" -> str += "new MyLong((long)(";
                        case "double" -> str += "new MyDouble((double)(";
                        case "boolean" -> str += "new MyBoolean((boolean)(";
                        case "char" -> str += "new MyChar((char)(";
                        case "short" -> str += "new MyShort((short)(";
                        case "byte" -> str += "new MyByte((byte)(";
                        case "String" -> str += "new MyString((String)(";
                        default -> {
                            return;
                        }
                    }
                    ITree exp = assignmentExp.getChild(2);
                    String expStr = this.getSubSuspiciouCodeStr(exp.getPos(), exp.getEndPos());
                    fixedCodeStr1 += var.getLabel() + " = " + str + expStr + str2 + ";";
                    generatePatch(fixedCodeStr1);
                    setImport(varType);
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
            String str = "";
            String str2 = ").value";
            int varStart = variable.getPos();
            int varEnd = variable.getEndPos();
            String varStr = ContextReader.readVariableName(variable);
            fixedCodeStr += this.getSubSuspiciouCodeStr(suspCodeStartPos, varStart);
            // no need to cast here
            switch (varType) {
                case "int" -> str += "new MyInteger(";
                case "float" -> str += "new MyFloat(";
                case "long" -> str += "new MyLong(";
                case "double" -> str += "new MyDouble(";
                case "boolean" -> str += "new MyBoolean(";
                case "char" -> str += "new MyChar(";
                case "short" -> str += "new MyShort(";
                case "byte" -> str += "new MyByte(";
                case "String" -> str += "new MyString(";
                default -> {
                    return;
                }
            }
            fixedCodeStr += str + varStr + str2;
            fixedCodeStr += this.getSubSuspiciouCodeStr(varEnd, suspCodeEndPos);
            generatePatch(fixedCodeStr);
            setImport(varType);
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
            if ("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
                    || "float".equals(varType) || "double".equals(varType) || "char".equals(varType)) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }

    private void setImport(String type) {
        switch (type) {
            case "int" -> setImportInt(true);
            case "float" -> setImportFloat(true);
            case "long" -> setImportLong(true);
            case "double" -> setImportDouble(true);
            case "boolean" -> setImportBoolean(true);
            case "char" -> setImportChar(true);
            case "short" -> setImportShort(true);
            case "byte" -> setImportByte(true);
            case "String" -> setImportString(true);
        }
    }


}
