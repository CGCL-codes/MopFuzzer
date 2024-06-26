package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.context.Method;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.List;
import java.util.Random;

public class LockElision extends MutateTemplate {

    String currentMethodName = null;
    String currentClassName = null;



    protected static String readClassName(ITree tree) {
        ITree parent = tree.getParent();
        while (parent != null) {
            if (Checker.isTypeDeclaration(parent.getType())) {
                return ContextReader.readClassName(parent);
            }
            parent = parent.getParent();
        }
        return null;
    }

    protected static int nextCaseOrDefaultStmt(ITree suspCodeTree) {
        ITree parent = suspCodeTree.getParent();
        int index = parent.getChildPosition(suspCodeTree);
        var children = parent.getChildren();
        for (int i = index + 1; i < children.size(); i++) {
            var child = children.get(i);
            if (Checker.isSwitchCase(child.getType()) || Checker.isBreakStatement(child.getType())) {
                return child.getPos();
            }
        }
        return children.get(children.size() - 1).getEndPos();
    }

    @Override
    public void generatePatches() {
        ITree suspCodeTree = this.getSuspiciousCodeTree();
//        int bound = 1;
//        ITree method = isStatementInSynchronizedMethod(suspCodeTree);
//        if (method != null)
//            bound++;
//        int select = new Random().nextInt(bound);
        // synchronized block
        List<String> varNames = null;
        int codeEndPos = suspCodeEndPos;
        int parentType = suspCodeTree.getParent().getType();
        if (!((Checker.isSwitchCase(parentType)) || Checker.isSwitchStatement(parentType))) {
            if (Checker.isExpressionStatement(suspCodeTree.getType())
                    && Checker.isAssignment(suspCodeTree.getChild(0).getType())
                    || Checker.isVariableDeclarationStatement(suspCodeTree.getType())) {
                varNames = ContextReader.identifyVariableNames(suspCodeTree);
            }
            if (varNames != null) {
                codeEndPos = ContextReader.identifyRelatedStatements(suspCodeTree, varNames, codeEndPos).getFirst();
            }
        } else {
            // find next case statement or default statement
            codeEndPos = nextCaseOrDefaultStmt(suspCodeTree);
        }
        readCurrentMethodAndClassName(suspCodeTree);

        setDictionary(dic);

        String fullClassName = this.packageName + currentClassName;
        List<Method> methods = dic.getMethods().get(fullClassName);
        boolean canNew = false;
        boolean hasConstructor = false;
        if (methods != null) {
            for (Method method : methods) {
                if (method.isConstructor()) {
                    hasConstructor = true;
                    if (method.getParameterTypes().isEmpty()) {
                        canNew = true;
                        break;
                    }
                }
            }
        }
        if (!hasConstructor)
            canNew = true;

        String select = null;
        String clazzName = readClassName(suspCodeTree);
        if (clazzName.contains("Thread"))
            canNew = false;
        String fixedCodeStr2 = "}\n";
        if (canNew) {
            switch (new Random().nextInt(3)) {
                case 0 -> select = "synchronized (new " + clazzName + "()) {\n";
                case 1 -> select = "synchronized (new Double(1.1f)) {\n";
                case 2 -> select = "synchronized (" + clazzName + ".class){\n ";
            }
        } else {
            switch (new Random().nextInt(2)) {
                case 0 -> select = "synchronized (new Double(1.1f)) {\n";
                case 1 -> select = "synchronized (" + clazzName + ".class){\n ";
            }
        }
        offset += 1;
        generatePatch(suspCodeStartPos, codeEndPos, select, fixedCodeStr2);
    }

    private void readCurrentMethodAndClassName(ITree tree) {
        ITree parent = tree.getParent();
        while (parent != null) {
            if (Checker.isMethodDeclaration(parent.getType())) {
                String mLabel = parent.getLabel();
                if (currentMethodName == null) {
                    // private, @@int, MethodName:test, @@Argus:int+a+int[]+b+
                    String[] split = mLabel.split(", ");
                    for (String s : split) {
                        if (s.startsWith("MethodName:")) {
                            currentMethodName = s.substring(11);
                            break;
                        }
                    }
                }
            } else if (Checker.isTypeDeclaration(parent.getType())) {
                if (currentClassName == null)
                    currentClassName = ContextReader.readClassName(parent);
                else
                    currentClassName = ContextReader.readClassName(parent) + "." + currentClassName;
            }
            parent = parent.getParent();
        }
    }
}
