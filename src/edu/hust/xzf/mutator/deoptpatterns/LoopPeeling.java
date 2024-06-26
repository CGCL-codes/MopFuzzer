package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static edu.hust.xzf.mutator.utils.CodeUtils.countChar;


public class LoopPeeling extends MutateTemplate {

    String varType;

    public LoopPeeling() {
        super();
//        hitMaxTimes = 2;
    }


    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        String fixedCodeStr = genComplexLoopParttern();
        generatePatch(suspCodeEndPos, fixedCodeStr);
        offset += countChar(fixedCodeStr, '\n');
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

    // without any var
    public String genComplexLoopParttern() {

        int N = 10000;
        int select = new Random().nextInt(3);
        String i = generateUniqueVarName();
        String condition = i + "<" + (3 + new Random().nextInt(5));
//                switch (select) {
//            case 1 -> i + " == " + new Random().nextInt(N);
//            case 2 -> i + " == " + (N - 1);
//            default -> throw new IllegalStateException("Unexpected value: " + select);
//        };

        Supplier<String>[] lambdaArray = new Supplier[]{
                () -> {
                    //   for (int i = 0; i < N; ++i) {
//                        if (condition(i)) {
//                            stmp
//                             continue;
//                        }
//                        i = i;
//                    }
                    return "for (int " + i + " = 0; " + i + " < 10000; ++" + i + ") {\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "continue;\n" +
                            "}\n" +

                            "}\n";
                },
                () -> {
                    // i = 0
                    //    while(i<N) {
                    //                        i++
//                        if (condition(i)) {
//                            stmt
//                       continue;
//                        }
//                    }
                    return "int " + i + " = 0;\n" +
                            "while (" + i + " < 10000) {\n" +
                            i + "++;\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "continue;\n" +
                            "}\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    do {
                    // i++;
                    //  if (condition(i)) {
                    //        stmt
//                    continue;
                    //   }
                    //    } while (i < N);
                    return "int " + i + " = 0;\n" +
                            "do {\n" +
                            i + "++;\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "continue;\n" +
                            "}\n" +
                            "} while (" + i + " < 10000);\n";
                }};
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return lambdaArray[randomIndex].get() + "\n";
    }


    public boolean isBaseType(String varType) {
        return varType.equals("int") || varType.equals("long") || varType.equals("short") || varType.equals("byte")
                || varType.equals("float") || varType.equals("double") || varType.equals("char") || varType.equals("boolean");
    }

}
