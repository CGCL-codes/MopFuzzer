package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.Random;

public class DeadCodeElimination extends MutateTemplate {
    @Override
    public void generatePatches() {

        ITree stmtAst = this.getSuspiciousCodeTree();
        String fixedCodeStr1 = "";
        String[] falseExp = new String[]{"false", "((18 * 19 + 20 * 20) & 0x1FF) == 121"
                , "(21 + 22) << 1 != 86", "(52 + 53) << 1 != 210", "(54 | 55) == 63",
                "(10 * 11 - 3 * 3) / 5 != 20", "(39 * 40 - 41 * 42) % 43 == 11",
                "Long.MAX_VALUE < Integer.MAX_VALUE"};
        int select2 = new Random().nextInt(falseExp.length);
        if (Checker.isExpressionStatement(stmtAst.getType())
                && Checker.isAssignment(stmtAst.getChild(0).getType())) {
            ITree assignmentExp = stmtAst.getChild(0);
            ITree var = assignmentExp.getChild(0);

            ITree exp = assignmentExp.getChild(2);
            int pos2 = exp.getPos();
            String expStr = getSubSuspiciouCodeStr(pos2, suspCodeEndPos);

            String newVar = this.generateUniqueVarName();
            ContextReader.readAllVariablesAndFields(stmtAst, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, dic);
            String varType = varTypesMap.get(var.getLabel());
            int bound;
            if (varType == null)
                varType = varTypesMap.get("this." + var.getLabel());

            if (varType == null)
                bound = 1;
            else bound = 2;


            int select = new Random().nextInt(bound);
            switch (select) {
                case 0 -> {
                    // if statement
                    fixedCodeStr1 = "\nif (" + falseExp[select2] + ") {\n";
                    fixedCodeStr1 += this.getSuspiciousCodeStr();
                    fixedCodeStr1 += "\n}\n";
                    offset += 4;
                }
//                case 1 -> {
//                    // while statement
//                    fixedCodeStr1 = "while (" + falseExp[select2] + ") {\n";
//                    fixedCodeStr1 += this.getSuspiciousCodeStr();
//                    fixedCodeStr1 += "\n}\n";
//                    offset += 3;
//                }
//                case 2 -> {
//                    // add exp1 = exp1
//                    fixedCodeStr1 = "\n" + var.getLabel() + " = " + var.getLabel() + ";\n";
//                    offset += 2;
//                }
                case 1 -> {
                    String repeatType = "";
                    if (varType.equals("boolean"))
                        repeatType = "boolean";
                    else
                        repeatType = "double";

                    if (varType.contains("["))
                        repeatType = varType;

                    // add arr[index] = right, repeat the steatement
                    if (Checker.isArrayAccess(var.getType())) {
                        fixedCodeStr1 = this.getSuspiciousCodeStr() + "\n";
                    } else
                        fixedCodeStr1 += repeatType + " " + newVar + " = " + expStr + "\n";
                    offset += 1;
                }
            }
            generatePatch(this.suspCodeEndPos, fixedCodeStr1);

        } else {
            // insert an always true/false condition statement
            int select = new Random().nextInt(2);
            switch (select) {
                case 0 -> {
                    // if statement
                    fixedCodeStr1 = "\nif (" + falseExp[select2] + ") {\n";
                    fixedCodeStr1 += this.getSuspiciousCodeStr();
                    fixedCodeStr1 += "\n}\n";
                    offset += 4;
                }
//                case 1 -> {
//                    // while statement
//                    fixedCodeStr1 = "while (" + falseExp[select2] + ") {\n";
//                    fixedCodeStr1 += this.getSuspiciousCodeStr();
//                    fixedCodeStr1 += "\n}\n";
//                    offset += 3;
//                }
                case 1 -> {
                    // repeat statement
                    if (Checker.isVariableDeclarationStatement(stmtAst.getType())) {
                        var children = stmtAst.getChildren();
                        // varType
                        ITree type = children.get(0);
                        fixedCodeStr1 += type.getLabel() + " ";

                        for (int i = 1; i < children.size(); i++) {
                            ITree child = children.get(i);
                            ITree name = child.getChild(0);
                            ITree exp = child.getChild(1);
                            fixedCodeStr1 += generateUniqueVarName() + " = " + this.getSubSuspiciouCodeStr(exp.getPos(), exp.getEndPos());
                            if (i != children.size() - 1)
                                fixedCodeStr1 += ", ";
                            else fixedCodeStr1 += ";";
                        }
                        fixedCodeStr1 += "\n";
                        offset += 1;
                    }
                }
            }
            this.generatePatch(this.suspCodeEndPos, fixedCodeStr1);
        }

    }


}
