package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.*;

public class OptimisticTypeAssertions extends MutateTemplate {
    protected static Map<ITree, String> identifyCastExpressions(ITree codeAst) {
        Map<ITree, String> castExps = new HashMap<>();

        List<ITree> children = codeAst.getChildren();
        if (children == null || children.isEmpty()) return castExps;

        int astNodeType = codeAst.getType();
        if (Checker.isVariableDeclarationStatement(astNodeType)) {
            boolean isType = true; // Identity data type
            for (ITree child : children) {
                int childNodeType = child.getType();
                if (Checker.isModifier(childNodeType)) {
                    continue;
                }
                if (isType) { // Type Node.
                    isType = false;
                } else if (Checker.isStatement(childNodeType)) {
                    break;
                } else { //VariableDeclarationFragment(s)
                    String varName = child.getChild(0).getLabel();
                    if (child.getChildren().size() > 1) {
                        ITree assignedExp = child.getChild(1);
                        if (Checker.isCastExpression(assignedExp.getType())) {
                            castExps.put(assignedExp, varName);
                        }
                        castExps.putAll(identifyCastExpressions(assignedExp));
                    }
                }
            }
        } else if (Checker.isExpressionStatement(astNodeType)) {
            ITree expAst = children.get(0);
            int expAstType = expAst.getType();
            if (Checker.isAssignment(expAstType)) {
                String varName = expAst.getChild(0).getLabel();
                ITree subExpAst = expAst.getChild(2);
                int subExpType = subExpAst.getType();
                if (Checker.isCastExpression(subExpType)) {
                    castExps.put(subExpAst, varName);
                }
                castExps.putAll(identifyCastExpressions(subExpAst));
            } else { // Other expressions.
                castExps.putAll(identifyCastExpressions(expAst));
            }
        } else if (Checker.isReturnStatement(astNodeType)) {
            ITree exp = children.get(0);
            int expType = exp.getType();
            if (Checker.isReturnStatement(expType)) { // Empty return statement, i.e., "return;".
            } else {
                if (Checker.isCastExpression(expType)) {
                    castExps.put(exp, "");
                }
                castExps.putAll(identifyCastExpressions(exp));
            }
        } else if (Checker.isFieldDeclaration(astNodeType)) {
            // FIXME: we ignore this situation in the current version.
        } else if (Checker.isComplexExpression(astNodeType) || Checker.isSimpleName(astNodeType)) { // expressions
            for (ITree child : children) {
                int childType = child.getType();
                if (Checker.isComplexExpression(childType)) {
                    if (Checker.isCastExpression(childType)) {
                        castExps.put(child, "");
                    }
                    castExps.putAll(identifyCastExpressions(child));
                } else if (Checker.isSimpleName(childType) && child.getLabel().startsWith("MethodName:")) {
                    castExps.putAll(identifyCastExpressions(child));
                } else if (Checker.isStatement(childType)) break;
            }
        }
        return castExps;
    }

    public static <K, V> Map.Entry<K, V> getRandomEntryFromMap(Map<K, V> map) {
        // 将Map的键值对转换为一个Entry列表
        List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());

        // 如果Map为空，返回null
        if (entryList.isEmpty()) {
            return null;
        }

        // 使用java.util.Random类生成一个随机索引
        Random random = new Random();
        int randomIndex = random.nextInt(entryList.size());

        // 使用生成的随机索引从列表中选择一个元素
        return entryList.get(randomIndex);
    }

    public void generatePatches() {
        ITree suspStmtTree = this.getSuspiciousCodeTree();
        Map<ITree, String> castExps = identifyCastExpressions(suspStmtTree);

        if (castExps.isEmpty()) return;
        Map.Entry<ITree, String> entry = getRandomEntryFromMap(castExps);

//        for (Map.Entry<ITree, String> entity : castExps.entrySet()) {
        //Generate Patches with CastExpression.
        ITree castExp = entry.getKey();
        String varName = entry.getValue();
        ITree castingType = castExp.getChild(0);
        int castTypeStartPos = castingType.getPos();
        int castTypeEndPos = castTypeStartPos + castingType.getLength();
        String castTypeStr = this.getSubSuspiciouCodeStr(castTypeStartPos, castTypeEndPos);
        if (castTypeStr.equals("double") || castTypeStr.equals("float") || castTypeStr.equals("long") || castTypeStr.equals("boolean")
                || castTypeStr.equals("int") || castTypeStr.equals("short") || castTypeStr.equals("byte") || castTypeStr.equals("char"))
            return;
        ITree castedExp = castExp.getChild(1);
        int castedExpStartPos = castedExp.getPos();
        int castedExpEndPos = castedExpStartPos + castedExp.getLength();
        String castedExpStr = this.getSubSuspiciouCodeStr(castedExpStartPos, castedExpEndPos);
        int castedExpType = castedExp.getType();
        if (castTypeStr.endsWith(">"))
            castTypeStr = castTypeStr.substring(0, castTypeStr.indexOf("<"));


        String fixedCodeStr1 = "";
        if (Checker.isSimpleName(castedExpType) || Checker.isFieldAccess(castedExpType)
                || Checker.isQualifiedName(castedExpType) || Checker.isSuperFieldAccess(castedExpType)) {
            // BC_UNCONFIRMED_CAST, PAR
            fixedCodeStr1 = "assert " + castedExpStr + " instanceof " + castTypeStr + ";\n";
//            fixedCodeStr1 = "if (" + castedExpStr + " instanceof " + castTypeStr + ") {\n\t";
//            String fixedCodeStr2 = "\n\t} else {\n\tthrow new IllegalArgumentException(\"Illegal argument: " + castedExpStr + "\");\n}\n";
            generatePatch(suspCodeEndPos, fixedCodeStr1);
            offset += 1;
        } else if (Checker.isComplexExpression(castedExpType)) {
            var newVar = this.generateUniqueVarName();
            // PAR
            fixedCodeStr1 = "Object " + newVar + "= " + castedExpStr + ";\n" +
                    "assert " + newVar + " instanceof " + castTypeStr + ";\n";
//            fixedCodeStr1 = "Object " + newVar + "= " + castedExpStr + ";\n\t" +
//                    "if (" + newVar + " instanceof " + castTypeStr + ") {\n\t";
//            String fixedCodeStr2 = "\n\t} else {\n\tthrow new IllegalArgumentException(\"Illegal argument: " + castedExpStr + "\");\n}\n";
            generatePatch(suspCodeEndPos, fixedCodeStr1);
            offset += 2;
        }
    }


}
