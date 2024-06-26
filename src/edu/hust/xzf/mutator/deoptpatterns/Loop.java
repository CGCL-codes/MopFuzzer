package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static edu.hust.xzf.mutator.utils.CodeUtils.countChar;


public class Loop extends MutateTemplate {
    public String varType;

    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        ContextReader.readAllVariablesAndFields(tree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
        List<SuspNullExpStr> suspNullExpStrs = new ArrayList<>();
        List<String> vars = new ArrayList<>();
        identifySuspiciousVariables(tree, vars, suspNullExpStrs);
        String varName = getRandomElementFromList(vars);
        if (varName == null) {
            String fixedCodeStr = genComplexLoopPartternWithoutVar();
            generatePatch(suspCodeEndPos, fixedCodeStr);
            offset += countChar(fixedCodeStr, '\n');
            return;
        }

        if (!varType.equals("int"))
            varName = "(int)" + varName;

        // arr[System.nanoTime() % arr.length]
        if (varType.contains("["))
            varName = varName + "[0]";


        String fixedCodeStr = genComplexLoopParttern(varName);
        generatePatch(suspCodeEndPos, fixedCodeStr);
        offset += countChar(fixedCodeStr, '\n');
    }

    private String genComplexLoopPartternWithoutVar() {
        String statement = this.getSuspiciousCodeStr();
        String i = generateUniqueVarName();
        String j = generateUniqueVarName();
        String b = generateUniqueVarName();
        String c = generateUniqueVarName();
        String e = generateUniqueVarName();
        String f = generateUniqueVarName();

        // for (int i = 2; i < 8; i+=2) {
        //    for (int j = 1; j < 8 - i; j+=2) {
        //        int b = i * j;
        //        int c = i + j;
        //        int e = i / i;
        //        int f = i - j;
        //        statement
        //        if (i % 2 == 0 && j % 3 == 1) {
        //            break;
        //        }
        //    }
        //}
        return "for (int " + i + " = 2; " + i + " < 8; " + i + "+=2) {\n"
                + "for (int " + j + " = 1; " + j + " < 8 - " + i + "; " + j + "+=2) {\n"
                + "int " + b + " = " + i + " * " + j + ";\n"
                + "int " + c + " = " + i + " + " + j + ";\n"
                + "int " + e + " = " + i + " / " + i + ";\n"
                + "int " + f + " = " + i + " - " + j + ";\n"
                + statement + "\n"
                + "if (" + i + " % 2 == 0 && " + j + " % 3 == 1) {\n"
                + "break;\n"
                + "}}}\n";
    }

    public String genComplexLoopParttern(String varName) {
        String statement = this.getSuspiciousCodeStr();
        String i = generateUniqueVarName();
        String j = generateUniqueVarName();
        String b = generateUniqueVarName();
        String c = generateUniqueVarName();
        String d = generateUniqueVarName();
        String e = generateUniqueVarName();
        String f = generateUniqueVarName();
        Supplier<String>[] lambdaArray = new Supplier[]{
                // for (int i = 0; i < varName % 8; i+=2) {
                //    for (int j = 0; j < varName % 8 - i; j+=2) {
                //        int b = i * j;
                //        int c = i + j;
                //        int e = a - i;
                //        int f = a - j;
                //        statement
                //        if (i % 2 == 0 && j % 3 == 1) {
                //            break;
                //        }
                //    }
                //}
                () -> "for (int " + i + " = 0; " + i + " < " + varName + " % 8; " + i + "+=2) {\n"
                        + "for (int " + j + " = 0; " + j + " < " + varName + " % 8 - " + i + "; " + j + "+=2) {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + "if (" + i + " % 2 == 0 && " + j + " % 3 == 1) {\n"
                        + "break;\n"
                        + "}}}\n",

                // int i = 0;
                // while (i < varName % 8) {
                //    int j = 0;
                //    while (j < varName % 8 - i) {
                //        int b = i * j;
                //        int c = i + j;
                //        int e = varName - i;
                //        int f = varName - j;
                //        statement
                //        j+=2;
                //        if ((j == 3 && i % 2 == 0) || (j == 4 && i % 3 == 1)) {
                //            break;
                //        }
                //    }
                //    i+=2;
                //    if (i == 5 || i == 7) {
                //        continue;
                //    }
                //}
                () -> "int " + i + " = 0;\n"
                        + "while (" + i + " < " + varName + " % 8) {\n"
                        + "int " + j + " = 0;\n"
                        + "while (" + j + " < " + varName + " % 8 - " + i + ") {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + j + "+=2;\n"
                        + "if ((" + j + " == 3 && " + i + " % 2 == 0) || (" + j + " == 4 && " + i + " % 3 == 1)) {\n"
                        + "break;\n"
                        + "}}\n"
                        + i + "+=2;\n"
                        + "if (" + i + " == 5 || " + i + " == 7) {\n"
                        + "continue;\n"
                        + "}}\n",

                // for (int i = 0; i < varName % 8; i+=2) {
                //    if (i % 2 == 0) {
                //        int b = i * (varName - i);
                //        int c = i * (varName + i);
                //        int e = a - i;
                //        int f = a - i - 1;
                //        statement
                //    } else {
                //        for (int j = 0; j < i; j++) {
                //            int b = i * j;
                //            int c = i + j;
                //            int e = varName - j;
                //            int f = varName - i - j;
                //            statement
                //        }
                //    }
                //}
                () -> "for (int " + i + " = 0; " + i + " < " + varName + " % 8; " + i + "+=2) {\n"
                        + "if (" + i + " % 2 == 0) {\n"
                        + "int " + b + " = " + i + " * (" + varName + " - " + i + ");\n"
                        + "int " + c + " = " + i + " * (" + varName + " + " + i + ");\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + i + " - 1;\n"
                        + statement + "\n"
                        + "} else {\n"
                        + "for (int " + j + " = 0; " + j + " < " + i + "; " + j + "++) {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + j + ";\n"
                        + "int " + f + " = " + varName + " - " + i + " - " + j + ";\n"
                        + statement + "\n"
                        + "}}}\n",


                // for (int i = 0; i < varName % 8; i+=2) {
                //    for (int j = 0; j < varName % 8 - i; j+=2) {
                //        int b = i * j;
                //        int c = i + j;
                //        int e = varName - i;
                //        int f = varName - j;
                //        statement
                //        if (i % 2 == 0) {
                //            continue;
                //        }
                //        if (j % 3 == 1) {
                //            break;
                //        }
                //    }
                //}
                () -> "for (int " + i + " = 0; " + i + " < " + varName + " % 8; " + i + "+=2) {\n"
                        + "for (int " + j + " = 0; " + j + " < " + varName + " % 8 - " + i + "; " + j + "+=2) {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + "if (" + i + " % 2 == 0) {\n"
                        + "continue;\n"
                        + "}\n"
                        + "if (" + j + " % 3 == 1) {\n"
                        + "break;\n"
                        + "}}}\n",

                // int i = 0;
                //while (i < varName % 8) {
                //    int j = 0;
                //    while (j < varName % 8 - i) {
                //        int b = i * j;
                //        int c = i + j;
                //        int e = varName - i;
                //        int f = varName - j;
                //        statement
                //        j+=2;
                //        if (j % 2 == 1 && i % 3 == 0) {
                //            continue;
                //        } else if (j % 4 == 0 && i % 5 == 1) {
                //            break;
                //        }
                //    }
                //    i+=1;
                //}
                () -> "int " + i + " = 0;\n"
                        + "while (" + i + " < " + varName + " % 8) {\n"
                        + "int " + j + " = 0;\n"
                        + "while (" + j + " < " + varName + " % 8 - " + i + ") {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + j + "+=2;\n"
                        + "if (" + j + " % 2 == 1 && " + i + " % 3 == 0) {\n"
                        + "continue;\n"
                        + "} else if (" + j + " % 4 == 0 && " + i + " % 5 == 1) {\n"
                        + "break;\n"
                        + "}}\n"
                        + i + "++;\n"
                        + "}\n",

                // for (int i = 0; i < varName % 8; i+=2) {
                //    if (i % 2 == 0) {
                //        for (int j = 0; j < varName % 8 - i; j+=2) {
                //            int b = i * j;
                //            int c = i + j;
                //            int e = varName - i;
                //            int f = varName - j;
                //            statement
                //            if (j % 3 == 1) {
                //                break;
                //            }
                //        }
                //    } else {
                //        int j = 0;
                //        while (j < varName % 8 - i) {
                //            int b = i * j;
                //            int c = i + j;
                //            int e = varName - i;
                //            int f = varName - j;
                //            statement
                //            j++;
                //            if (j % 5 == 2) {
                //                break;
                //            }
                //        }
                //    }
                //}
                () -> "for (int " + i + " = 0; " + i + " < " + varName + " % 8; " + i + "+=2) {\n"
                        + "if (" + i + " % 2 == 0) {\n"
                        + "for (int " + j + " = 0; " + j + " < " + varName + " % 8 - " + i + "; " + j + "+=2) {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + "if (" + j + " % 3 == 1) {\n"
                        + "break;}}\n"
                        + "} else {\n"
                        + "int " + j + " = 0;\n"
                        + "while (" + j + " < " + varName + " % 8 - " + i + ") {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + j + "++;\n"
                        + "if (" + j + " % 5 == 2) {\n"
                        + "break;}}}}\n",

                // int i = 0;
                //while (i < varName % 4) {
                //    int j = 0;
                //    while (j < varName % 4 - i) {
                //        int b = i * j;
                //        int c = i + j;
                //        int e = varName - i;
                //        int f = varName - j;
                //        statement
                //        j++;
                //        if (i % 2 == 0 && j % 3 == 1) {
                //            break;
                //        }
                //    }
                //    i++;
                //    if (i % 4 == 3) {
                //        continue;
                //    }
                //}
                () -> "int " + i + " = 0;\n"
                        + "while (" + i + " < " + varName + " % 4) {\n"
                        + "int " + j + " = 0;\n"
                        + "while (" + j + " < " + varName + " % 4 - " + i + ") {\n"
                        + "int " + b + " = " + i + " * " + j + ";\n"
                        + "int " + c + " = " + i + " + " + j + ";\n"
                        + "int " + e + " = " + varName + " - " + i + ";\n"
                        + "int " + f + " = " + varName + " - " + j + ";\n"
                        + statement + "\n"
                        + j + "++;\n"
                        + "if (" + i + " % 2 == 0 && " + j + " % 3 == 1) {\n"
                        + "break;\n"
                        + "}}\n"
                        + i + "++;\n"
                        + "if (" + i + " % 4 == 3) {\n"
                        + "continue;\n"
                        + "}}\n",
        };
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return lambdaArray[randomIndex].get();
    }

    public String getRandomElementFromList(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        Random random = new Random();

        while (!list.isEmpty()) {
            int randomIndex = random.nextInt(list.size());
            var select = list.get(randomIndex);
            varType = this.varTypesMap.get(select);
            if (varType == null)
                varType = this.varTypesMap.get("this." + select);
            if (varType == null) {
                list.remove(randomIndex);
                continue;
            }

            if ("int".equals(varType) || "long".equals(varType) || "short".equals(varType)
                    || "byte".equals(varType) || "float".equals(varType) || "double".equals(varType) || "char".equals(varType)
                    || "int[]".equals(varType) || "long[]".equals(varType) || "short[]".equals(varType) || "byte[]".equals(varType)
                    || "float[]".equals(varType) || "double[]".equals(varType) || "char[]".equals(varType)) {
                return select;
            }
            list.remove(randomIndex);
        }
        return null;
    }
}
