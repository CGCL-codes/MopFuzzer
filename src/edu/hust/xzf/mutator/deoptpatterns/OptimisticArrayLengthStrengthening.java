package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.entity.Pair;
import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;

import static edu.hust.xzf.mutator.context.ContextReader.readMethodReturnType;
import static edu.hust.xzf.mutator.deoptpatterns.LockElision.nextCaseOrDefaultStmt;
import static edu.hust.xzf.mutator.utils.CodeUtils.getRandomElementFromList;

public class OptimisticArrayLengthStrengthening extends MutateTemplate {

    @Override
    public void generatePatches() {
        ITree suspCodeTree = this.getSuspiciousCodeTree();
        List<Pair<ITree, ITree>> allSuspiciousArrayVars = identifyAllSuspiciousArrayAccesses(suspCodeTree);
        var randomPair = getRandomElementFromList(allSuspiciousArrayVars);
        if (randomPair == null)
            return;
        ITree suspArrayExp = randomPair.getFirst();
        ITree indexExp = randomPair.getSecond();
        int suspArrayExpStartPos = suspArrayExp.getPos();
        int suspArrayExpEndPos = suspArrayExpStartPos + suspArrayExp.getLength();
        int indexExpStartPos = indexExp.getPos();
        int indexExpEndPos = indexExpStartPos + indexExp.getLength();

        String fixedCodeStr2 = "\n}";
        String suspArrayExpStr = getSubSuspiciouCodeStr(suspArrayExpStartPos, suspArrayExpEndPos);
        String indexExpStr = getSubSuspiciouCodeStr(indexExpStartPos, indexExpEndPos);
        List<String> varNames = null;
        int codeEndPos = suspCodeEndPos;
        int parentType = suspCodeTree.getParent().getType();
        if (!((Checker.isSwitchCase(parentType)) || Checker.isSwitchStatement(parentType))) {
            if (Checker.isVariableDeclarationStatement(suspCodeTree.getType()) ||
                    (Checker.isExpressionStatement(suspCodeTree.getType()) && Checker.isAssignment(suspCodeTree.getChild(0).getType()))) {
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
//                        break;
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

        String fixedCodeStr1 = "if ((" + indexExpStr + ") < " + suspArrayExpStr + ".length) {\n";

        generatePatch(suspCodeStartPos, codeEndPos, fixedCodeStr1, fixedCodeStr2);
        offset += 1;
    }

    protected static List<Pair<ITree, ITree>> identifyAllSuspiciousArrayAccesses(ITree suspCodeTree) {
        List<Pair<ITree, ITree>> allSuspiciousArrayVars = new ArrayList<>();
        List<ITree> children = suspCodeTree.getChildren();
        for (ITree child : children) {
            int type = child.getType();
            if (Checker.isStatement(type)) break;
            else if (Checker.isArrayAccess(type)) {
                ITree arrayExp = child.getChild(0);
                ITree indexExp = child.getChild(1);
                if (Checker.isComplexExpression(arrayExp.getType())) {
                    allSuspiciousArrayVars.addAll(identifyAllSuspiciousArrayAccesses(arrayExp));
                }
                allSuspiciousArrayVars.add(new Pair<>(arrayExp, indexExp));
            } else if (Checker.isComplexExpression(type)) {
                allSuspiciousArrayVars.addAll(identifyAllSuspiciousArrayAccesses(child));
            } else if (Checker.isSimpleName(type) && child.getLabel().startsWith("MethodName:")) {
                allSuspiciousArrayVars.addAll(identifyAllSuspiciousArrayAccesses(child));
            }
        }
        return allSuspiciousArrayVars;
    }


}
