package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static edu.hust.xzf.mutator.utils.CodeUtils.countChar;


public class LoopUnswitch extends MutateTemplate {

    String varType;

    public LoopUnswitch() {
        super();
//        hitMaxTimes = 2;
    }

    protected static boolean isStaticMethod(ITree suspCodeTree) {
        ITree parent = suspCodeTree.getParent();
        while (parent != null) {
            if (Checker.isMethodDeclaration(parent.getType())) {
                for (ITree child : parent.getChildren()) {
                    if (Checker.isModifier(child.getType())) {
                        String modifier = child.getLabel();
                        if (modifier.equals("static")) {
                            return true;
                        }
                    }
                }
                break;
            }
            parent = parent.getParent();
        }
        return false;
    }

    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        String field = generateUniqueVarName();

        String fixedCodeStr = genComplexLoopParttern(field);
        boolean flag = isStaticMethod(tree);

        generatePatch(suspCodeEndPos, fixedCodeStr);
        setImportPos(locateNewField(this.getSuspiciousCodeTree()));
        addNewField((flag ? "static" : "") + " int " + field + " = 0;\n");
        offset += 1;
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

    private int locateNewField(ITree tree) {
        ITree parent = tree.getParent();
        while (parent != null) {
            if (Checker.isMethodDeclaration(parent.getType())) {
                for (ITree child : parent.getChildren()) {
                    if (Checker.isModifier(child.getType())) {
                        String modifier = child.getLabel();
                        if (modifier.equals("static")) {
                            break;
                        }
                    }
                }
                break;
            }
            parent = parent.getParent();
        }
        return parent.getPos();
    }

    // without any var
    public String genComplexLoopParttern(String field) {


        int N = 20000;
        String i = generateUniqueVarName();
        String condition = i + " == " + N / 2;

        String flag = generateUniqueVarName();
        String init = "boolean " + flag + " = true;\n";
        String flagTrue = "if(" + flag + ")";

        Supplier<String>[] lambdaArray = new Supplier[]{
                () -> {
                    //   for (int i = 0; i < N; ++i) {
//                        if (condition(i)) {
//                            stmp
//                        }
//                        i = i;
//                    }
                    return "for (int " + i + " = 0; " + i + " < 20000; ++" + i + ") {\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "}\n" +
                            flagTrue +
                            field + "++;\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    while(i<N) {
//                        if (condition(i)) {
//                            stmt
//                        }
//                        i++
//                    }
                    return "int " + i + " = 0;\n" +
                            "while (" + i + " < 20000) {\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "}\n" +
                            flagTrue +
                            field + "++;\n" +
                            i + "++;\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    do {
                    //  if (condition(i)) {
                    //        stmt
                    //   }
                    //    i++
                    //    } while (i < N);
                    return "int " + i + " = 0;\n" +
                            "do {\n" +
                            i + "++;\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "}\n" +
                            flagTrue +
                            field + "++;\n" +
                            "} while (" + i + " < 20000);\n";
                }};
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return init + lambdaArray[randomIndex].get() + "\n";
    }


    public boolean isBaseType(String varType) {
        return varType.equals("int") || varType.equals("long") || varType.equals("short") || varType.equals("byte")
                || varType.equals("float") || varType.equals("double") || varType.equals("char") || varType.equals("boolean");
    }

}
