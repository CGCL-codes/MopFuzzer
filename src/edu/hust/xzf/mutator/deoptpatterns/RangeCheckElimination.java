package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.entity.Pair;
import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static edu.hust.xzf.mutator.deoptpatterns.OptimisticArrayLengthStrengthening.identifyAllSuspiciousArrayAccesses;
import static edu.hust.xzf.mutator.utils.CodeUtils.countChar;
import static edu.hust.xzf.mutator.utils.CodeUtils.getRandomElementFromList;

public class RangeCheckElimination extends MutateTemplate {

    @Override
    public void generatePatches() {
        ITree tree = this.getSuspiciousCodeTree();
        List<Pair<ITree, ITree>> allSuspiciousArrayVars = identifyAllSuspiciousArrayAccesses(tree);
        var randomPair = getRandomElementFromList(allSuspiciousArrayVars);
        if (randomPair == null)
            return;
        ITree suspArrayExp = randomPair.getFirst();
        ITree indexExp = randomPair.getSecond();
        int suspArrayExpStartPos = suspArrayExp.getPos();
        int suspArrayExpEndPos = suspArrayExpStartPos + suspArrayExp.getLength();
        int indexExpStartPos = indexExp.getPos();
        int indexExpEndPos = indexExpStartPos + indexExp.getLength();

        String suspArrayExpStr = getSubSuspiciouCodeStr(suspArrayExpStartPos, suspArrayExpEndPos);
        String indexExpStr = getSubSuspiciouCodeStr(indexExpStartPos, indexExpEndPos);
        if (indexExpStr.contains("+") || indexExpStr.contains("-"))
            indexExpStr = "(" + indexExpStr + ")";
        String fixedCodeStr = genComplexRangerChecker(suspArrayExpStr, indexExpStr);
        generatePatch(suspCodeEndPos, fixedCodeStr);
        offset += countChar(fixedCodeStr, '\n');
    }

    private String genComplexRangerChecker(String arr, String index) {
        String statement = this.getSuspiciousCodeStr();
        String idx1 = generateUniqueVarName();
        String idx2 = generateUniqueVarName();
        String i = generateUniqueVarName();
        String j = generateUniqueVarName();
        String k = generateUniqueVarName();

        Supplier<String>[] lambdaArray = new Supplier[]{

                //  for (int i = 0; i < arr.length; i++) {
                //    for (int j = 0; j < arr.length; j++) {
                //      int idx1 = (index + i * j) % arr.length;
                //      int idx2 = (index - i * j) % arr.length;
                //      if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * " + j + ") % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * " + j + ") % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",


                //  for (int i = 0; i < arr.length; i++) {
                //    for (int j = 0; j < arr.length; j++) {
                //      int idx1 = (index + i * j) % arr.length;
                //      int idx2 = (index - i * j * 3 + arr.length) % arr.length;
                //      if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * " + j + ") % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * " + j + " * 3 + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

                //  for (int i = 0; i < arr.length; i++) {
                //    for (int j = 0; j < arr.length; j++) {
                //      for (int k = 0; k < arr.length; k++) {
                //        int idx1 = (index + i * j * k) % arr.length;
                //        int idx2 = (index - i * j * k + arr.length) % arr.length;
                //        if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //          arr[idx1] = arr[idx2];
                //        }
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        "for (int " + k + " = 0; " + k + " < " + arr + ".length; " + k + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * " + j + " * " + k + ") % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * " + j + " * " + k + " + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}}\n",


                //  for (int i = 0; i < arr.length; i++) {
                //    for (int j = 0; j < arr.length; j++) {
                //      for (int k = 0; k < arr.length; k++) {
                //        int idx1 = (index + i * j * k) % arr.length;
                //        int idx2 = (index - i * j * k * 2 + arr.length) % arr.length;
                //        if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //          arr[idx1] = arr[idx2];
                //        }
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        "for (int " + k + " = 0; " + k + " < " + arr + ".length; " + k + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * " + j + " * " + k + ") % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * " + j + " * " + k + " * 2 + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}}\n",

                //  for (int i = 0; i < arr.length; i++) {
                //    int idx1 = (complexExpression() + i * (arr.length - i)) % arr.length;
                //    int idx2 = (complexExpression() - i * (arr.length - i) + arr.length) % arr.length;
                //    if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //      arr[idx1] = arr[idx2];
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * (" + arr + ".length - " + i + ")) % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * (" + arr + ".length - " + i + ") + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}\n",

                //  for (int i = 0; i < arr.length; i++) {
                //    for (int j = 0; j < i; j++) {
                //      int idx1 = (index + i * j * (arr.length - j)) % arr.length;
                //      int idx2 = (index - i * j * (arr.length - j) + arr.length) % arr.length;
                //      if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + i + "; " + j + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * " + j + " * (" + arr + ".length - " + j + ")) % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * " + j + " * (" + arr + ".length - " + j + ") + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

                //  for (int i = arr.length - 1; i >= 0; i--) {
                //    for (int j = 0; j < arr.length; j++) {
                //      int idx1 = (index - i * j) % arr.length;
                //      int idx2 = (index + i * j + arr.length) % arr.length;
                //      if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = " + arr + ".length - 1; " + i + " >= 0; " + i + "--) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        "int " + idx1 + " = (" + index + " - " + i + " * " + j + ") % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " + " + i + " * " + j + " + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

                //  for (int i = arr.length - 1; i >= 0; i--) {
                //    for (int j = arr.length - 1; j >= 0; j--) {
                //      int idx1 = (index + i * j) % arr.length;
                //      int idx2 = (index - i * j + arr.length) % arr.length;
                //      if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = " + arr + ".length - 1; " + i + " >= 0; " + i + "--) {\n" +
                        "for (int " + j + " = " + arr + ".length - 1; " + j + " >= 0; " + j + "--) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * " + j + ") % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * " + j + " + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

                //  for (int i = 0; i < arr.length; i++) {
                //    int idx1 = (index + i * (arr.length - i)) % arr.length;
                //    int idx2 = (index - i * (arr.length - i) + arr.length) % arr.length;
                //    if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //      for (int j = 0; j < arr.length; j++) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * (" + arr + ".length - " + i + ")) % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * (" + arr + ".length - " + i + ") + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

                //  for (int i = arr.length - 1; i >= 0; i--) {
                //    for (int j = 0; j < arr.length; j++) {
                //      int idx1 = (index * (i + 1) * (j + 1)) % arr.length;
                //      int idx2 = (index * (i + 1) * (j + 1) + arr.length) % arr.length;
                //      if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = " + arr + ".length - 1; " + i + " >= 0; " + i + "--) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        "int " + idx1 + " = (" + index + " * (" + i + " + 1) * (" + j + " + 1)) % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " * (" + i + " + 1) * (" + j + " + 1) + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

                //  for (int i = 0; i < arr.length; i++) {
                //    int idx1 = (index + i * (arr.length - i)) % arr.length;
                //    int idx2 = (index - i * (arr.length - i) + arr.length) % arr.length;
                //    if (idx1 >= 0 && idx1 < arr.length && idx2 >= 0 && idx2 < arr.length) {
                //      for (int j = 0; j < arr.length; j++) {
                //        arr[idx1] = arr[idx2];
                //      }
                //    }
                //  }
                () -> "for (int " + i + " = 0; " + i + " < " + arr + ".length; " + i + "++) {\n" +
                        "int " + idx1 + " = (" + index + " + " + i + " * (" + arr + ".length - " + i + ")) % " + arr + ".length;\n" +
                        "int " + idx2 + " = (" + index + " - " + i + " * (" + arr + ".length - " + i + ") + " + arr + ".length) % " + arr + ".length;\n" +
                        "if (" + idx1 + " >= 0 && " + idx1 + " < " + arr + ".length && " + idx2 + " >= 0 && " + idx2 + " < " + arr + ".length) {\n" +
                        "for (int " + j + " = 0; " + j + " < " + arr + ".length; " + j + "++) {\n" +
                        arr + "[" + idx1 + "] = " + arr + "[" + idx2 + "];\n" +
                        "}}}\n",

        };
        int randomIndex = new Random().nextInt(lambdaArray.length);
        return lambdaArray[randomIndex].get();


    }
}
