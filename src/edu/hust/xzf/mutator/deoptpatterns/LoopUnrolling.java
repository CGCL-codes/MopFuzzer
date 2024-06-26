package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static edu.hust.xzf.mutator.utils.CodeUtils.countChar;


public class LoopUnrolling extends MutateTemplate {

    String varType;

    public LoopUnrolling() {
        super();
//        hitMaxTimes = 2;
    }

    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        String fixedCodeStr = "";

        if (Checker.isExpressionStatement(tree.getType())) {
            ITree assignmentExp = tree.getChild(0);
            if (Checker.isAssignment(assignmentExp.getType())) {
                ITree var = assignmentExp.getChild(0);
                ITree op = assignmentExp.getChild(1);
                ITree right = assignmentExp.getChild(2);
                String varName = this.getSubSuspiciouCodeStr(var.getPos(), var.getEndPos());
                String varType = varTypesMap.get(varName);
                if (varType == null)
                    varType = varTypesMap.get("this." + varName);
                if (varType != null) {
                    String opStr = op.getLabel();
                    String rightStr = this.getSubSuspiciouCodeStr(right.getPos(), right.getEndPos());
                    fixedCodeStr = genComplexLoopParttern(varType, varName, opStr, rightStr);
                }
            }
        }
        if (fixedCodeStr.isEmpty()) {
            // for non assignment expression
            List<ITree> suspVars = new ArrayList<>();
            ContextReader.identifySuspiciousVariables(tree, suspVars, new ArrayList<>());
            if (!suspVars.isEmpty()) {
                ITree variable = getRandomElementFromList(suspVars);
                if (variable != null) {
                    fixedCodeStr = genComplexLoopParttern(variable);
                }
            }
        }

        if (fixedCodeStr.isEmpty())
            fixedCodeStr = genComplexLoopParttern();

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

        int N = 8;
        int select = new Random().nextInt(3);
        String i = generateUniqueVarName();
        String condition = switch (select) {
            case 0 -> i + " == 0";
            case 1 -> i + " == " + new Random().nextInt(N);
            case 2 -> i + " == " + (N - 1);
            default -> throw new IllegalStateException("Unexpected value: " + select);
        };

        Supplier<String>[] lambdaArray = new Supplier[]{
                () -> {
                    //   for (int i = 0; i < N; ++i) {
//                        if (condition(i)) {
//                            stmp
//                        }
//                        i = i;
//                    }
                    return "for (int " + i + " = 0; " + i + " < 8; ++" + i + ") {\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "}\n" +
                            i + " = " + i + ";\n" +
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
                            "while (" + i + " < 8) {\n" +
                            "if (" + condition + ") {\n" +
                            this.getSuspiciousCodeStr() + "\n" +
                            "}\n" +
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
                            "} while (" + i + " < 8);\n";
                }};
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return lambdaArray[randomIndex].get() + "\n";
    }


    // use one var from stmt (non-assignment)
    public String genComplexLoopParttern(ITree variable) {
        String varName = this.getSubSuspiciouCodeStr(variable.getPos(), variable.getEndPos());
        String tmp = generateUniqueVarName();
        String initialValue = "0";
        if (varType.equals("boolean")) initialValue = "false";
        // varType tmp = m if op != = and 0 else ;
        String fixedCodeStr = varType + " " + tmp + " = " + initialValue + ";\n";
        int N = 8;
        int select = new Random().nextInt(3);
        String i = generateUniqueVarName();
        String condition = switch (select) {
            case 0 -> i + " == 0";
            case 1 -> i + " == " + new Random().nextInt(N);
            case 2 -> i + " == " + (N - 1);
            default -> throw new IllegalStateException("Unexpected value: " + select);
        };
        String newStmt = this.getSubSuspiciouCodeStr(suspCodeStartPos, variable.getPos()) + tmp +
                this.getSubSuspiciouCodeStr(variable.getEndPos(), suspCodeEndPos);

        Supplier<String>[] lambdaArray = new Supplier[]{
                () -> {
//                    for (int i = 0; i < N; ++i) {
//                        if (condition(i)) {
//                            tmp op right
//                        }
//                        var = tmp;
//                    }
                    return "for (int " + i + " = 0; " + i + " < 8; ++" + i + ") {\n" +
                            "if (" + condition + ") {\n" +
                            newStmt + "\n" +
                            "}\n" +
                            tmp + " = " + varName + ";\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    while(i<N) {
//                        if (condition(i)) {
//                            newStmt
//                        }
//                        tmp = varName;
//                    }
                    return "int " + i + " = 0;\n" +
                            "while (" + i + " < 8) {\n" +
                            "if (" + condition + ") {\n" +
                            newStmt + "\n" +
                            "}\n" +
                            tmp + " = " + varName + ";\n" +
                            i + "++;\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    do {
                    //  if (condition(i)) {
                    //        newStmt
                    //   }
                    //    tmp = varName;
                    //    } while (i < N);
                    return "int " + i + " = 0;\n" +
                            "do {\n" +
                            i + "++;\n" +
                            "if (" + condition + ") {\n" +
                            newStmt + "\n" +
                            "}\n" +
                            tmp + " = " + varName + ";\n" +
                            "} while (" + i + " < 10000);\n";

                }};
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return fixedCodeStr + lambdaArray[randomIndex].get() + "\n";
    }


    // use one var from stmt (assignment)
    public String genComplexLoopParttern(String varType, String varName, String op, String right) {
        String tmp = generateUniqueVarName();
        String initialValue = isBaseType(varType) ? "0" : right;
        if (varType.equals("boolean")) initialValue = "false";
        // varType tmp = m if op != = and 0 else ;
        String fixedCodeStr = varType + " " + tmp + " = " + (op.equals("=") ? initialValue : varName) + ";\n";
        int N = 8;
        int select = new Random().nextInt(3);
        String i = generateUniqueVarName();
        String condition = switch (select) {
            case 0 -> i + " == 0";
            case 1 -> i + " == " + new Random().nextInt(N);
            case 2 -> i + " == " + (N - 1);
            default -> throw new IllegalStateException("Unexpected value: " + select);
        };

        Supplier<String>[] lambdaArray = new Supplier[]{
                () -> {
//                    for (int i = 0; i < N; ++i) {
//                        if (condition(i)) {
//                            tmp op right
//                        }
//                        var = tmp;
//                    }
                    return "for (int " + i + " = 0; " + i + " < 8; ++" + i + ") {\n" +
                            "if (" + condition + ") {\n" +
                            tmp + " " + op + " " + right + ";\n" +
                            "}\n" +
                            varName + " = " + tmp + ";\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    while(i<N) {
//                        if (condition(i)) {
//                            tmp op right
//                        }
//                        var = tmp;
//                    }
                    return "int " + i + " = 0;\n" +
                            "while (" + i + " < 8) {\n" +
                            "if (" + condition + ") {\n" +
                            tmp + " " + op + " " + right + ";\n" +
                            "}\n" +
                            varName + " = " + tmp + ";\n" +
                            i + "++;\n" +
                            "}\n";
                },
                () -> {
                    // i = 0
                    //    do {
                    //  if (condition(i)) {
                    //        tmp op right
                    //   }
                    //    var = tmp;
                    //    } while (i < N);
                    return "int " + i + " = 0;\n" +
                            "do {\n" +
                            i + "++;\n" +
                            "if (" + condition + ") {\n" +
                            tmp + " " + op + " " + right + ";\n" +
                            "}\n" +
                            varName + " = " + tmp + ";\n" +
                            "} while (" + i + " < 8);\n";
                }
        };
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return fixedCodeStr + lambdaArray[randomIndex].get() + "\n";
    }

    public boolean isBaseType(String varType) {
        return varType.equals("int") || varType.equals("long") || varType.equals("short") || varType.equals("byte")
                || varType.equals("float") || varType.equals("double") || varType.equals("char") || varType.equals("boolean");
    }

}
