package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class OptimisticNullnessAssertions extends MutateTemplate {

    public String varType;

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
//        int suspCodeEndPos = ContextReader.identifyRelatedStatements(suspCodeTree, expStr, this.suspCodeEndPos);
        if (expStr.contains("["))
            expStr = expStr.substring(0, expStr.indexOf('['));
        String assertStr = "assert " + expStr + " != null;\n";
        generatePatch(suspCodeEndPos, assertStr);
        offset += 1;
//        String nullCheckStr = "if (" + expStr + " != null) {\n";
//        generatePatch(suspCodeStartPos, suspCodeEndPos, nullCheckStr, "\n\t}\n");
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
                varType = this.varTypesMap.get("this." + select.expStr);
                if (varType == null) {
                    list.remove(randomIndex);
                    continue;
                }
            }

            if (Character.isUpperCase(varType.charAt(0))) {
                list.remove(randomIndex);
                continue;
            }

            if (!("int".equals(varType) || "long".equals(varType) || "short".equals(varType)
                    || "byte".equals(varType) || "float".equals(varType)
                    || "double".equals(varType) || "char".equals(varType)
                    || "boolean".equals(varType)) && !select.expStr.endsWith(".value")) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }



}

