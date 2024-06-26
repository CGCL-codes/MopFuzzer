package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static edu.hust.xzf.mutator.context.ContextReader.readMethodReturnType;
import static edu.hust.xzf.mutator.deoptpatterns.LockElision.nextCaseOrDefaultStmt;


public class NullCheckElimination extends MutateTemplate {
    String varType;

    @Override
    public void generatePatches() {
        ITree suspCodeTree = this.getSuspiciousCodeTree();
        List<String> vars = new ArrayList<>();
        List<SuspNullExpStr> suspNullExpStrs = new ArrayList<>();
        ContextReader.readAllVariablesAndFields(suspCodeTree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        identifySuspiciousVariables(suspCodeTree, vars, suspNullExpStrs);
        var randomVar = getRandomElementFromList(suspNullExpStrs);
        if (randomVar == null) return;
        String expStr = randomVar.expStr;

        List<String> varNames = null;
        int codeEndPos = suspCodeEndPos;
        String fixedCodeStr2 = "\n}";
        int parentType = suspCodeTree.getParent().getType();
        if (!((Checker.isSwitchCase(parentType)) || Checker.isSwitchStatement(parentType))) {
            if (Checker.isExpressionStatement(suspCodeTree.getType())
                    && Checker.isAssignment(suspCodeTree.getChild(0).getType())
                    || Checker.isVariableDeclarationStatement(suspCodeTree.getType())) {
                varNames = ContextReader.identifyVariableNames(suspCodeTree);
            }
            if (varNames != null) {
                var pair = ContextReader.identifyRelatedStatements(suspCodeTree, varNames, codeEndPos);
                codeEndPos = pair.getFirst();
                if (pair.getSecond()) {
                    String returnType = readMethodReturnType(suspCodeTree);
                    switch (returnType) {
                        case "boolean":
                            fixedCodeStr2 += "else{return false;}\n";
                            break;
                        case "int":
                        case "long":
                        case "short":
                        case "byte":
                        case "float":
                        case "double":
                        case "char":
                            fixedCodeStr2 += "else{return 0;}\n";
                            break;
                        case "void":
                        case "=CONSTRUCTOR=":
                            fixedCodeStr2 += "else{return;}\n";
                            break;
                        default:
                            fixedCodeStr2 += "else{return null;}\n";
                            break;
                    }
                }
            }
        } else {
            // find next case statement or default statement
            codeEndPos = nextCaseOrDefaultStmt(suspCodeTree);
        }

        if (expStr.contains("["))
            expStr = expStr.substring(0, expStr.indexOf('['));

        String nullCheckStr = "if (" + expStr + " != null) {\n";
        generatePatch(suspCodeStartPos, codeEndPos, nullCheckStr, fixedCodeStr2);
        offset += 1;
    }

    public SuspNullExpStr getRandomElementFromList(List<SuspNullExpStr> list) {
        // 如果List为空，返回null
        if (list.isEmpty()) {
            return null;
        }
        Random random = new Random();

        while (!list.isEmpty()) {
            int randomIndex = random.nextInt(list.size());
            var select = list.get(randomIndex);

            varType = this.varTypesMap.get(select.expStr);
            if (varType == null) {
                varType = varTypesMap.get("this." + select.expStr);
                if (varType == null) {
                    list.remove(randomIndex);
                    continue;
                }
            }
            if (Character.isUpperCase(varType.charAt(0))) {
                list.remove(randomIndex);
                continue;
            }

            if (!("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
                    || "float".equals(varType) || "double".equals(varType) || "char".equals(varType) || "boolean".equals(varType))
                    && !select.expStr.endsWith("value")) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }


}
