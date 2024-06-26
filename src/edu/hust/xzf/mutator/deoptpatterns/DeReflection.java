package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.context.Field;
import edu.hust.xzf.mutator.context.Method;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;

import static edu.hust.xzf.mutator.deoptpatterns.LockElision.nextCaseOrDefaultStmt;
import static edu.hust.xzf.mutator.utils.CodeUtils.getRandomElementFromList;


public class DeReflection extends MutateTemplate {

    String currentMethodName = null;
    String currentClassName = null;

    public DeReflection() {

    }

    @Override
    public void generatePatches() {
        ITree suspCodeTree = this.getSuspiciousCodeTree();
        readCurrentMethodAndClassName(suspCodeTree);
        ContextReader.readAllVariablesAndFields(suspCodeTree, allVarNamesMap, varTypesMap, allVarNamesList, this.sourceCodePath, null);

        setDictionary(dic);
        List<ReflectionExp> susNodes = new ArrayList<>();
        identifySuspiciousMethodInvocationExps(suspCodeTree, susNodes);

        if (susNodes.isEmpty()) return;
        ReflectionExp random = getRandomElementFromList(susNodes);
        if (random == null)
            return;

        String fixedCodeStr = "";
        String clazz = generateUniqueClassName();
        fixedCodeStr += "Class <?> " + clazz + " = Class.forName(\"" + random.className + "\");\n";
        fixedCodeStr += getSubSuspiciouCodeStr(suspCodeStartPos, random.targetTree.getPos());
        if (random.isMethod) {
            // tt.getDeclaredMethod("test2", int.class, int[].class).invoke(this, a, b)
            Method method = random.method;
            String invoke = clazz + ".getDeclaredMethod(\"" + random.method.getMethodName() + "\"";
            for (String paramType : method.getParameterTypes()) {
                // 泛型
//                if (!paramType.equals("E"))
                invoke += ", " + paramType + ".class";
//                else
//                invoke += ", " + "Object.class";
            }
            invoke += ").invoke(" + random.varStr;
            for (String argStr : random.argumentStrs) {
                invoke += ", " + argStr;
            }
            invoke += ")";

            if (random.targetTree.getPos() == suspCodeTree.getPos()) {
                fixedCodeStr += invoke;
            } else {
                fixedCodeStr += "((" + random.method.getReturnType() + ")" + invoke + ")";
            }
            offset += 1;
        } else if (random.isField) {

            // tt.getDeclaredField("i1").get(this);
            Field field = random.field;
            fixedCodeStr += "((" + random.field.getDataType() + ")" +
                    clazz + ".getDeclaredField(\"" + random.fieldStr + "\").get(" + random.varStr + "))";
            offset += 1;
        }
        fixedCodeStr += getSubSuspiciouCodeStr(random.targetTree.getEndPos(), suspCodeEndPos);
        generatePatch(fixedCodeStr);


        int codeEndPos = suspCodeEndPos;
        int parentType = suspCodeTree.getParent().getType();
        List<String> varNames = null;
        if (!((Checker.isSwitchCase(parentType)) || Checker.isSwitchStatement(parentType))) {
            if (Checker.isExpressionStatement(suspCodeTree.getType())
                    && Checker.isAssignment(suspCodeTree.getChild(0).getType())
                    || Checker.isVariableDeclarationStatement(suspCodeTree.getType())) {
                varNames = ContextReader.identifyVariableNames(suspCodeTree);
            }
            if (varNames != null) {
                codeEndPos = ContextReader.identifyRelatedStatements(suspCodeTree, varNames, this.suspCodeEndPos).getFirst();
                // can ignore return statement
            }
        } else {
            // find next case statement or default statement
            codeEndPos = nextCaseOrDefaultStmt(suspCodeTree);
        }
        setImportPos(suspCodeTree.getPos());
        setImportPos2(codeEndPos + fixedCodeStr.length() - getSuspiciousCodeStr().length());
        offset += 2;
        setImportTryCatch(true);

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
            }
            parent = parent.getParent();
        }
    }


    private void identifySuspiciousMethodInvocationExps(ITree suspCodeTree, List<ReflectionExp> susNodes) {
        int suspCodeTreeType = suspCodeTree.getType();

        List<ITree> children = suspCodeTree.getChildren();
        // for instance virtual call
        // (name/QualifiedName).(MethodName) and the name shouble be an valid instance
        if (Checker.isMethodInvocation(suspCodeTreeType) && !Checker.isLambdaExpression(suspCodeTree.getParent().getType())) {
            if (children.size() == 1 && Checker.isSimpleName(children.get(0).getType())) {
                // Test.foo(); or foo(1);
                ITree methodNode = children.get(0);
                handleStaticCall(methodNode, susNodes, suspCodeTree);
            }
            if (children.isEmpty()) {
                // foo();
                handleStaticCall(suspCodeTree, susNodes, suspCodeTree);
            }
            if (children.size() > 1 && Checker.isSimpleName(children.get(1).getType())) {
                // tt.foo(); Test.foo(1);Test.foo();
                ITree varNode = children.get(0);
                String varName = ContextReader.readVariableName(varNode);
                String varStr = getSubSuspiciouCodeStr(varNode.getPos(), varNode.getEndPos());
                String varType;
                if (dic.getMethods().keySet().contains(this.packageName + varName)) {
                    // static call
                    varStr = "null";
                    varType = this.packageName + varName;
                } else if (varName.equals("this")) {
                    // this.foo();
                    varType = this.packageName + currentClassName;
                } else {
                    // tt.foo();
                    varType = varTypesMap.get(varName);
                    if (varType == null) {
                        varType = varTypesMap.get("this." + varName);
                    }
                }
                if (varType != null) {
                    String fullClassName = varType;
                    List<Method> methods = dic.getMethods().get(varType);
                    if (methods == null) {
                        methods = dic.getMethods().get(this.packageName + varType);
                        fullClassName = this.packageName + varType;
                    }

                    if (methods != null) {
                        ITree methodNode = children.get(1);
                        if (methodNode.getLabel().startsWith("MethodName:")) {
                            String methodName = methodNode.getLabel().substring(11);
                            if (methodName.contains(":"))
                                methodName = methodName.substring(0, methodName.indexOf(":"));

                            String myMethodName = methodName + ":" + methodNode.getChildren().size();

                            for (Method method : methods) {
                                if (method.toString().equals(myMethodName)) {
                                    List<String> argumentStrs = new ArrayList<>();
                                    for (int i = 0; i < methodNode.getChildren().size(); i++) {
                                        ITree arg = methodNode.getChildren().get(i);
                                        String argStr = getSubSuspiciouCodeStr(arg.getPos(), arg.getEndPos());
                                        if (Checker.isLambdaExpression(arg.getType()))
                                            argumentStrs.add("(" + method.getParameterTypes().get(i) + ")(" + argStr + ")");
                                        else
                                            argumentStrs.add(argStr);
                                    }

                                    susNodes.add(new ReflectionExp(suspCodeTree, fullClassName, method, varStr, argumentStrs));
                                }
                            }
                        }
                    }
                }
            }
        }
        // for field access
        // (fieldAccess/QualifiedName).(fieldName) and the name shouble be an valid instance
        if (Checker.isFieldAccess(suspCodeTreeType) || Checker.isQualifiedName(suspCodeTreeType)) {
            if (children.size() < 2)
                return;
            int parentType = suspCodeTree.getParent().getType();
            if (Checker.isPrefixExpression(parentType) || Checker.isPostfixExpression(parentType)) {
                // ++i; i++; --i; i--;
                return;
            }
            ITree varNode = children.get(0);
            String varName = ContextReader.readVariableName(varNode);
            String varStr = getSubSuspiciouCodeStr(varNode.getPos(), varNode.getEndPos());
            String varType = varTypesMap.get(varName);
            if (varType == null)
                varType = varTypesMap.get("this." + varName);

            String fullClassName = varType;
            List<Field> fields = dic.getAllFields().get(varType);
            if (fields == null) {
                fields = dic.getAllFields().get(this.packageName + varType);
                fullClassName = this.packageName + varType;
            }

            if (varType != null && fields != null) {
                ITree fieldNode = children.get(1);
                String fieldStr = getSubSuspiciouCodeStr(fieldNode.getPos(), fieldNode.getEndPos());
                for (Field field : fields) {
                    if (field.toString().equals(fieldStr) || field.toString().equals("this." + fieldStr))
                        susNodes.add(new ReflectionExp(suspCodeTree, fullClassName, field, fieldStr, varStr));
                }
            }
        }
//        // for class instance creation
//        if (Checker.isClassInstanceCreation(suspCodeTreeType)) {
//            susNodes.add(suspCodeTree);
//        }
        for (ITree child : children) {
            if (!Checker.isAssignment(child.getType()))
                identifySuspiciousMethodInvocationExps(child, susNodes);
            else {
                // only handle right hand of assignment
                identifySuspiciousMethodInvocationExps(child.getChild(2), susNodes);
            }
        }
    }

    private void handleStaticCall(ITree methodNode, List<ReflectionExp> susNodes, ITree suspCodeTree) {
        if (methodNode.getLabel().startsWith("MethodName:")) {
            String methodName = methodNode.getLabel().substring(11);
            if (methodName.contains(":"))
                methodName = methodName.substring(0, methodName.indexOf(":"));
            if (methodName.startsWith("method"))
                return;

            String myMethodName = methodName + ":" + methodNode.getChildren().size();

            String fullClassName = currentClassName;
            List<Method> methods = dic.getMethods().get(currentClassName);
            if (methods == null) {
                methods = dic.getMethods().get(this.packageName + this.currentClassName);
                fullClassName = this.packageName + this.currentClassName;
            }
            if (methods == null) {
                return;
            }
            // identify the method is static call or instance call (this call)
            for (Method method : methods) {
                if (method.toString().equals(myMethodName)) {
                    String varName;
                    if (method.isStatic()) {
                        varName = "null";
                    } else {
                        varName = "this";
                    }

                    List<String> argumentStrs = new ArrayList<>();
                    for (int i = 0; i < methodNode.getChildren().size(); i++) {
                        ITree arg = methodNode.getChildren().get(i);
                        String argStr = getSubSuspiciouCodeStr(arg.getPos(), arg.getEndPos());
                        if (Checker.isLambdaExpression(arg.getType()))
                            argumentStrs.add("(" + method.getParameterTypes().get(i) + ")(" + argStr + ")");
                        else
                            argumentStrs.add(argStr);
                    }
                    susNodes.add(new ReflectionExp(suspCodeTree, fullClassName, method, varName, argumentStrs));
                }
            }
        }
    }


    private boolean isNumberType(String dataType) {
        if ("int".equals(dataType)) return true;
        if ("Integer".equals(dataType)) return true;
        if ("long".equalsIgnoreCase(dataType)) return true;
        if ("float".equalsIgnoreCase(dataType)) return true;
        if ("double".equalsIgnoreCase(dataType)) return true;
        if ("short".equalsIgnoreCase(dataType)) return true;
        if ("byte".equalsIgnoreCase(dataType)) return true;
        return false;
    }

}

class ReflectionExp {
    String className;
    Method method;
    Field field;

    String fieldStr;

    // (className) varStr.method
    // (className) varStr.field
    String varStr;

    List<String> argumentStrs;

    boolean isMethod;
    boolean isField;

    ITree targetTree;

    public ReflectionExp(ITree targetTree, String className, Method method, String varStr, List<String> argumentStrs) {
        this.targetTree = targetTree;
        this.className = className;
        this.method = method;
        this.varStr = varStr;
        this.argumentStrs = argumentStrs;
        isMethod = true;
    }

    public ReflectionExp(ITree targetTree, String className, Field field, String fieldStr, String varStr) {
        this.targetTree = targetTree;
        this.className = className;
        this.fieldStr = fieldStr;
        this.field = field;
        this.varStr = varStr;
        isField = true;
    }
}