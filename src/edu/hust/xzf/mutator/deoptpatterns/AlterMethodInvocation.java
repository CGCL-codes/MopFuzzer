package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.AST.ASTGenerator;
import edu.hust.xzf.AST.ASTGenerator.TokenType;
import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.context.Method;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The method invocations are limited to all methods defined in the current program.
 *
 * @author kui.liu
 */
public abstract class AlterMethodInvocation extends MutateTemplate {

    ITree classDeclarationAst = null;
    String packageName = "";
    String className = null;
    private Map<String, List<String>> couldBeReplacedMethods = null; // replace method name.
    private List<ITree> differentParaMethods = null;   // add or delete parameter(s);



    private void identifySuspiciousMethodInvocationExps(ITree suspCodeTree, List<ITree> susNodes) {
        int suspCodeTreeType = suspCodeTree.getType();

        List<ITree> children = suspCodeTree.getChildren();
        // for method call
        if (Checker.isMethodInvocation(suspCodeTreeType) && children.size() > 1) {
            if (Checker.isSimpleName(children.get(0).getType()) &&
                    Checker.isSimpleName(children.get(1).getType())) {
                if (!isValidInstance(children.get(0))) return;
                ITree methodNode = children.get(1);
                if (methodNode.getLabel().startsWith("MethodName:")) {
                    susNodes.add(suspCodeTree);
                }
            }
        }
        // for field access
        if (Checker.isFieldAccess(suspCodeTreeType) || Checker.isQualifiedName(suspCodeTreeType)) {
            susNodes.add(suspCodeTree);
        }
        // for class instance creation
        if (Checker.isClassInstanceCreation(suspCodeTreeType)) {
            susNodes.add(suspCodeTree);
        }
        for (ITree child : children) {
            identifySuspiciousMethodInvocationExps(child, susNodes);
        }
    }

    private boolean isValidInstance(ITree tree) {
        String varName = ContextReader.readVariableName(tree);
        if (varName == null)
            return false;
        String varType = varTypesMap.getOrDefault(varName, null);
        if (varType == null)
            return false;
        return varType.equals(className);
    }

    private boolean checkParameterTypes(List<String> paraTypeStrs, List<String> targetParaTypeList) {
        boolean matched = true;
        for (int i = 0, size = paraTypeStrs.size(); i < size; i++) {
            String paraType = paraTypeStrs.get(i);
            String targetType = targetParaTypeList.get(i);
            matched = matchParameterType(paraType, targetType);
            if (!matched) break;
        }
        return matched;
    }

    protected boolean matchParameterType(String paraType, String targetType) {
        if (paraType.equals("Object")) {
            // fuzzy matching.
        } else if (paraType.equals(targetType)) {
        } else if ((paraType.equals("char") || paraType.equals("Character"))
                && targetType.equals("char") || targetType.equals("Character")) {
        } else if ((paraType.equals("int") || paraType.equals("Integer"))
                && targetType.equals("int") || targetType.equals("Integer")) {
        } else if (paraType.equalsIgnoreCase("boolean") && targetType.equalsIgnoreCase("boolean")) {
        } else if (paraType.equalsIgnoreCase("byte") && targetType.equalsIgnoreCase("byte")) {
        } else if (paraType.equalsIgnoreCase("short") && targetType.equalsIgnoreCase("short")) {
        } else if (paraType.equalsIgnoreCase("long") && targetType.equalsIgnoreCase("long")) {
        } else if (paraType.equalsIgnoreCase("float") && targetType.equalsIgnoreCase("float")) {
        } else if (paraType.equalsIgnoreCase("double") && targetType.equalsIgnoreCase("double")) {
        } else if (!paraType.contains("-" + targetType + "-")) {
        } else if (paraType.contains("," + targetType + ",")) {
        } else {
            return false;
        }
        return true;
    }

    /**
     * Read the parameter types of the suspicious method invocation expression.
     *
     * @param parameters
     * @return Empty_ArrayList - zero parameter.
     * other - parameter types.
     */
    private List<String> readMethodParameterTypes(List<ITree> parameters) {
        List<String> paraTypeStrs = new ArrayList<>();
        if (parameters == null || parameters.isEmpty()) {
            // no parameter.
        } else {
            for (ITree para : parameters) {
                paraTypeStrs.add(readParameterType(para));
            }
        }
        return paraTypeStrs;
    }

    private String readParameterType(ITree para) {
        int paraAstType = para.getType();
        String paraLabel = para.getLabel();
        if (Checker.isArrayAccess(paraAstType)) {
            return readArrayAccessParameterReturnType(para);
        } else if (Checker.isBooleanLiteral(paraAstType)) {
            return "boolean";
        } else if (Checker.isCastExpression(paraAstType)) {
            return ContextReader.readType(para.getChild(0).getLabel());
        } else if (Checker.isCharacterLiteral(paraAstType)) {
            return "char";
        } else if (Checker.isClassInstanceCreation(paraAstType)) {
            if (Checker.isNewKeyword(para.getChild(0).getType())) {
                return ContextReader.readSimpleNameOfDataType(para.getChild(1).getLabel());
            } else {
                return ContextReader.readSimpleNameOfDataType(para.getChild(2).getLabel());
            }
        } else if (Checker.isFieldAccess(paraAstType)) {
            paraLabel = para.getChild(1).getLabel();
            String dataType = this.varTypesMap.get("this." + paraLabel);
            if (dataType != null) return dataType;
            else {
//				ITree exp = para.getChild(0);
//				String type = readParameterType(exp);
//				if ("Object".equals(type)) {
//					return type;
//				} else {
//					// the field paraLabel. TODO
//					// how to get the class path of type.
//					return null;
//				}
                return "Object";
            }
        } else if (Checker.isInfixExpression(paraAstType)) {
            return infixExpressionReturnType(para);
        } else if (Checker.isInstanceofExpression(paraAstType)) {
            return "boolean";
        } else if (Checker.isMethodDeclaration(paraAstType)) {
            // return type of method(...), var.method(...),
            return "Object";// TODO
        } else if (Checker.isSimpleName(paraAstType)) {
            paraLabel = ContextReader.readVariableName(para);
            String dataType = varTypesMap.get(paraLabel);
            if (dataType == null) dataType = this.varTypesMap.get("this." + paraLabel);
            if (dataType == null) {
                // TODO
                return "Object";
            } else return dataType;
        } else if (Checker.isQualifiedName(paraAstType)) {
            String dataType = this.varTypesMap.get(paraLabel);
            if (dataType != null) {
                return dataType;
            } else {// TODO
                return "Object";
            }
        } else if (Checker.isNullLiteral(paraAstType)) { // NullLiteral
            return "-int-short-byte-long-float-double-boolean-char-"; // it won't be one of this type.
//			return "Object"; // TODO
        } else if (Checker.isNumberLiteral(paraAstType)) { // NumberLiteral
            String lastChar = paraLabel.substring(paraLabel.length() - 1, paraLabel.length());
            if ("l".equalsIgnoreCase(lastChar)) {
                return "long";
            } else if ("f".equalsIgnoreCase(lastChar)) {
                return "float";
            } else if ("d".equalsIgnoreCase(lastChar)) {
                return "double";
            } else if (paraLabel.contains(".")) {
                return ",double,float,";
            } else { // Any possible number type.
                return ",int,Integer,long,Long,short,Short,char,Character,byte,Byte,float,Float,double,Double,";
            }
        } else if (Checker.isParenthesizedExpression(paraAstType)) {
            return readParameterType(para.getChild(0));
        } else if (Checker.isPostfixExpression(paraAstType)) {
            return ",int,Integer,long,Long,short,Short,char,Character,byte,Byte,float,Float,double,Double,"; //--, ++.
        } else if (Checker.isPrefixExpression(paraAstType)) {
            String op = para.getChild(0).getLabel();
            if ("!".equals(op)) {
                return "boolean";
            } else {// ~, -, +, --, ++.
                return ",int,Integer,long,Long,short,Short,char,Character,byte,Byte,float,Float,double,Double,";
            }
        } else if (Checker.isStringLiteral(paraAstType)) { // ThisExpression
            return "String";
        } else if (Checker.isSuperFieldAccess(paraAstType)) {
            paraLabel = para.getChildren().get(para.getChildren().size() - 1).getLabel();
            String dataType = this.varTypesMap.get("this." + paraLabel);
            return dataType == null ? "Object" : dataType; // null seems impossible.
        } else if (Checker.isSuperMethodInvocation(paraAstType)) {
            return "Object";// TODO
        } else if (Checker.isThisExpression(paraAstType)) {
            return className;
        } else {// Other Complex Expressions.
            // FIXME: Is it possible to get the data type of the return value of the complex expression?
            return "Object";
        }
    }

    private String readArrayAccessParameterReturnType(ITree para) {
        ITree exp = para.getChild(0);
        int i = 1;
        while (true) {
            if (!Checker.isArrayAccess(exp.getType())) break;
            exp = exp.getChild(0);
            i++;
        }
        String dataType = readParameterType(exp);

        int index = dataType.indexOf("[");
        if (index > 0) dataType = dataType.substring(0, index);
        for (int a = 0; a < i; a++) dataType += "[]";
        return dataType;
    }

    private String infixExpressionReturnType(ITree para) {
        String returnType;
        String op = para.getChild(1).getLabel();
        if ("&&".equals(op) || "||".equals(op) || "==".equals(op) || "!=".equals(op)
                || "<".equals(op) || "<=".equals(op) || ">".equals(op) || ">=".equals(op)) {
            returnType = "boolean";
        } else if ("-".equals(op) || "*".equals(op) || "/".equals(op) || "%".equals(op)) {
            returnType = ",int,Integer,long,Long,short,Short,char,Character,byte,Byte,float,Float,double,Double,";
        } else if ("&".equals(op) || "|".equals(op) || "^".equals(op)
                || ">>".equals(op) || "<<".equals(op) || ">>>".equals(op)) {
            // the integer types, long, int, short, char, and byte.
            returnType = ",int,Integer,long,Long,short,Short,char,Character,byte,Byte,";
        } else if ("+".equals(op)) {
            returnType = ",String,int,Integer,long,Long,short,Short,char,Character,byte,Byte,float,Float,double,Double,";
        } else {
            // TODO: +
            returnType = "Object";
        }
        return returnType;
    }

    /**
     * Read the class declaration AST of the suspicious code.
     *
     * @param suspCodeTree
     */
    private void readClassDeclaration(ITree suspCodeTree) {
        ITree parent = suspCodeTree.getParent();
        while (true) {
            if (Checker.isTypeDeclaration(parent.getType())) {
                this.classDeclarationAst = parent;
            }
            parent = parent.getParent();
            if (parent == null) break;
        }
    }

    /**
     * Read the class name of the suspicious code.
     *
     * @param suspCodeTree
     */
    private void readClassName(ITree suspCodeTree) {
        if (this.classDeclarationAst == null) {
            readClassDeclaration(suspCodeTree);
        }
        if (this.classDeclarationAst == null) {
            // FIXME non-type declaration file.
            className = null;
            return;
        }
        List<ITree> classChildren = this.classDeclarationAst.getChildren();
        for (ITree classChild : classChildren) {
            if (Checker.isSimpleName(classChild.getType())) {
                className = classChild.getLabel().substring(10);
                break;
            }
        }
    }

    /**
     * Read the package name of the suspicious code.
     */
    private void readPackageName() {
        ITree parent = this.classDeclarationAst.getParent();
        while (true) {
            ITree packageDeclaration = parent.getChild(0);
            if (Checker.isPackageDeclaration(packageDeclaration.getType())) {
                this.packageName = packageDeclaration.getLabel();
                break;
            }
            parent = parent.getParent();
            if (parent == null) break;
        }
    }

    /**
     * Read the information of suspicious method invocations.
     *
     * @return
     */
    private String readFieldType(ITree rootTree, String dataType, String fieldName) {
        String varType = null;
        List<ITree> children = rootTree.getChildren();
        children = children.get(children.size() - 1).getChildren();
        for (ITree child : children) {
            if (Checker.isFieldDeclaration(child.getType())) { // Field declaration
                List<ITree> subChildren = child.getChildren();
                boolean isFound = false;
                for (int i = 1, size = subChildren.size(); i < size; i++) {
                    ITree varDeclaration = subChildren.get(i);
                    if (Checker.isVariableDeclarationFragment(varDeclaration.getType())) {
                        if (varType == null) varType = subChildren.get(i - 1).getLabel();
                        if (varDeclaration.getChild(0).getLabel().equals(fieldName)) {
                            isFound = true;
                            break;
                        }
                    }
                }
                if (isFound) break;
                varType = null;
            }
        }
        return varType;
    }

    /**
     * Identify the java file path of a data type.
     *
     * @param classDeclarationAst
     * @param varType
     * @return
     */
    private String identifyJavaFilePath(ITree classDeclarationAst, String varType) {
        while (true) {
            if (classDeclarationAst == null) break;
            if (!Checker.isCompilationUnit(classDeclarationAst.getType())) {
                classDeclarationAst = classDeclarationAst.getParent();
            } else break;
        }
        if (classDeclarationAst == null) return null;

        List<ITree> rootTreeChildren = classDeclarationAst.getChildren();
        String path = null;
        String packageName = "";
        List<String> paths = new ArrayList<>();
        for (ITree child : rootTreeChildren) {
            int childType = child.getType();
            String childLabel = child.getLabel();
            if (Checker.isPackageDeclaration(childType)) { // package name.
                packageName = child.getLabel().replace(".", "/");
                path = this.sourceCodePath + packageName + "/" + varType + ".java";
                if (new File(path).exists()) break;
                else {
                    path = null;
                    paths.add(this.sourceCodePath + child.getLabel().replace(".", "/") + "/");
                }
            } else if (Checker.isImportDeclaration(childType)) { // import declarations.
                if (childLabel.endsWith("." + varType)) {
                    path = this.sourceCodePath + child.getLabel().replace(".", "/") + ".java";
                    if (!new File(path).exists()) path = null;
                    break;
                } else {
                    paths.add(this.sourceCodePath + child.getLabel().replace(".", "/") + "/");
                }
            } else if (Checker.isTypeDeclaration(childType)) {
                // Check its super class.
                String label = child.getLabel();
                int index = label.indexOf("@@SuperClass:");
                if (index > 0) {
                    label = label.substring(index + 13);
                    index = label.indexOf("@@Interface:");
                    if (index > 0) {
                        index = index - 2;
                    } else index = label.length() - 2;
                    String superClassName = ContextReader.readSimpleNameOfDataType(label.substring(0, index));
                    if (!paths.isEmpty()) {
                        String packagePath = paths.get(0);
                        String superClassPath = packagePath + superClassName + ".java";
                        if (!new File(superClassPath).exists()) {
                            superClassPath = null;
                            superClassName = "/" + superClassName;
                            for (String p : paths) {
                                if (p.endsWith(superClassName)) {
                                    superClassPath = packagePath + ".java";
                                    break;
                                }
                            }
                            if (superClassPath != null && !new File(superClassPath).exists()) superClassPath = null;
                        }

                        if (superClassPath != null) {
                            path = identifyJavaFilePath(new ASTGenerator().generateTreeForJavaFile(superClassPath, TokenType.EXP_JDT), varType);
                        } else path = null;
                    }
                }

                if (path == null) { // inner class.
                    String className = child.getLabel();
                    className = className.substring(className.indexOf("ClassName:") + 10);
                    className = className.substring(0, className.indexOf(", "));
                    List<ITree> children = child.getChildren();
                    packageName += "/" + className + ".java";
                    for (ITree tree : children) {
                        if (Checker.isTypeDeclaration(tree.getType()) && tree.getLabel().contains("ClassName:" + varType + ", ")) {
                            path = packageName + "INNER-CLASS";
                            break;
                        }
                    }
                    if (path != null) break;
                } else break;
            }
        }
        return path;
    }

    /**
     * @param classDeclarationAst
     * @param varType
     * @param methodName
     * @param paraTypeStrs
     * @return
     */
    private Map<List<String>, String> identifyPossibleReturnTypes(ITree classDeclarationAst, String varType, String methodName, List<String> paraTypeStrs) {
        String path = null;
        int constructorIndex = varType.indexOf("=CONSTRUCTOR=");
        boolean isConstructor = false;
        boolean isSuperClass = false;
        if (constructorIndex > 0) {
            isConstructor = true;
            varType = varType.substring(0, constructorIndex);
        }
        int superIndex = varType.indexOf("+Super");
        if (superIndex > 0) {
            varType = varType.substring(0, superIndex);
            isSuperClass = true;
        }
        if ("this".equals(varType)) {
            path = this.sourceCodePath + this.packageName.replace(".", "/") + "/" + this.className + ".java";
        } else {
            path = identifyJavaFilePath(classDeclarationAst, varType);
            if (path == null) return null;
            if (path.endsWith("INNER-CLASS")) {
                path = path.substring(0, path.length() - 11);
            } else {

            }
            if (!new File(path).exists()) return null;
            classDeclarationAst = new ASTGenerator().generateTreeForJavaFile(path, TokenType.EXP_JDT);
            List<ITree> children = classDeclarationAst.getChildren();
            classDeclarationAst = children.get(children.size() - 1);
        }

        return identifyPossibleReturnTypes(classDeclarationAst, methodName, paraTypeStrs, path, isConstructor, isSuperClass);
    }

    /**
     * @param classDeclarationAst
     * @param methodName
     * @param paraTypeStrs
     * @return
     */
    private Map<List<String>, String> identifyPossibleReturnTypes(ITree classDeclarationAst, String methodName, List<String> paraTypeStrs,
                                                                  String path, boolean isConstructor, boolean isSuperClass) {
        couldBeReplacedMethods = new HashMap<>(); // replace method name.
        differentParaMethods = new ArrayList<>();   // add or delete parameter(s);
        List<String> possibleReturnTypes = new ArrayList<>();
        String superConstructor = "";
        if (!isSuperClass) {
            List<ITree> children = classDeclarationAst.getChildren();
            for (ITree child : children) {
                if (Checker.isMethodDeclaration(child.getType())) {
                    // Match method name.
                    String label = child.getLabel();
                    int indexOfMethodName = label.indexOf("MethodName:");
                    int indexOrPara = label.indexOf("@@Argus:");
                    String currentMethodName = label.substring(indexOfMethodName + 11, indexOrPara - 2);

                    // Match parameter data types.
                    String paraStr = label.substring(indexOrPara + 8);
                    if (paraStr.startsWith("null")) {
                        paraStr = null;
                    } else {
                        int indexExp = paraStr.indexOf("@@Exp:");
                        if (indexExp > 0) paraStr = paraStr.substring(0, indexExp);
                    }

                    // Read return type.
                    String returnType = label.substring(label.indexOf("@@") + 2, indexOfMethodName - 2);
                    int index = returnType.indexOf("@@tp:");
                    if (index > 0) returnType = returnType.substring(0, index - 2);

                    if (isConstructor) {
                        if (!"=CONSTRUCTOR=".equals(returnType)) {// Constructor.
                            continue;
                        }
//					} else if (!currentMethodName.equals(methodName)) continue;
                    } else if ("=CONSTRUCTOR=".equals(returnType)) continue;
                    else returnType = ContextReader.readType(returnType);

                    // Match possible return types.
                    if (paraTypeStrs.isEmpty()) {
                        if (paraStr == null) {
                            if (currentMethodName.equals(methodName)) {
                                possibleReturnTypes.add(returnType);
                            } else { // methods with same parameter and same(possible) return type
                                List<String> methodNamesList = couldBeReplacedMethods.get(returnType);
                                if (methodNamesList == null) methodNamesList = new ArrayList<String>();
                                methodNamesList.add(currentMethodName);
                                couldBeReplacedMethods.put(returnType, methodNamesList);
                            }
                        } else if (currentMethodName.equals(methodName)) {
                            differentParaMethods.add(child);
                        }
                    } else if (paraStr != null) {
                        List<String> targetParaTypeList = ContextReader.parseMethodParameterTypes(paraStr, "\\+");
                        if (targetParaTypeList.size() == paraTypeStrs.size()) {
                            boolean matched = checkParameterTypes(paraTypeStrs, targetParaTypeList);
                            if (matched) {
                                if (currentMethodName.equals(methodName)) {
                                    possibleReturnTypes.add(returnType + "+" + paraStr);
                                } else { // methods with same parameter and same(possible) return type
                                    List<String> methodNamesList = couldBeReplacedMethods.get(returnType);
                                    if (methodNamesList == null) methodNamesList = new ArrayList<String>();
                                    methodNamesList.add(currentMethodName);
                                    couldBeReplacedMethods.put(returnType, methodNamesList);
                                }
                            }
                        } else if (currentMethodName.equals(methodName)) {
                            differentParaMethods.add(child);
                        }
                    } else if (currentMethodName.equals(methodName)) {
                        differentParaMethods.add(child);
                    }
                }
            }
        } else if (isConstructor) {
            superConstructor = "=CONSTRUCTOR=";
        }


        if (possibleReturnTypes.isEmpty()) {
            String label = classDeclarationAst.getLabel();
            int index = label.indexOf("@@SuperClass:");
            if (index > 0) {
                String superClassName = label.substring(index + 13);
                index = superClassName.indexOf("@@Interface:");
                if (index < 0) index = superClassName.length();
                superClassName = superClassName.substring(0, index - 2) + superConstructor;
                Map<List<String>, String> map = identifyPossibleReturnTypes(classDeclarationAst, superClassName, methodName, paraTypeStrs);
                if (map != null) {
                    possibleReturnTypes = map.entrySet().iterator().next().getKey();
                }
            }
        }
        Map<List<String>, String> map = new HashMap<>();
        map.put(possibleReturnTypes, path);
        return map;
    }

    private ITree findClassTree(ITree rootTree, String dataType) {
        List<ITree> children = rootTree.getChildren();
        for (ITree child : children) {
            if (Checker.isTypeDeclaration(child.getType())) {
                if (child.getLabel().contains("ClassName:" + dataType + ", ")) {
                    return child;
                } else {
                    ITree tree = findClassTree(child, dataType);
                    if (tree != null) return tree;
                }
            }
        }
        return null;
    }

    public class MethodInvocationExpression {

        /*
         * PackageName,
         * ClassName,
         * ReturnType,
         * MethodName,
         * Parameter Types.
         */

        private String codePath;
        private List<String> possibleReturnTypes = new ArrayList<>(); //"=CONSTRUCTOR="
        private String methodName;
        private List<String> possibleParameterTypes;
        private List<List<String>> parameterTypes = new ArrayList<>();
        private ITree codeAst;
        private Map<String, List<String>> couldBeReplacedMethods = null; // replace method name.
        private List<ITree> differentParaMethods = null;  // add or delete parameter(s);
        private List<Method> canBeReplacedMethods = null; // replace method name;
        private List<Method> diffParameterMethods = null; // add or delete parameter(s);

        public String getCodePath() {
            return codePath;
        }

        public void setCodePath(String codePath) {
            this.codePath = codePath;
        }

        public List<String> getPossibleReturnTypes() {
            return possibleReturnTypes;
        }

        public void setPossibleReturnTypes(List<String> possibleReturnTypes) {
            this.possibleReturnTypes = possibleReturnTypes;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public List<String> getPossibleParameterTypes() {
            return possibleParameterTypes;
        }

        public void setPossibleParameterTypes(List<String> possibleParameterTypes) {
            this.possibleParameterTypes = possibleParameterTypes;
        }

        public List<List<String>> getParameterTypes() {
            return parameterTypes;
        }

        public ITree getCodeAst() {
            return codeAst;
        }

        public void setCodeAst(ITree codeAst) {
            this.codeAst = codeAst;
        }

        public Map<String, List<String>> getCouldBeReplacedMethods() {
            return couldBeReplacedMethods;
        }

        public void setCouldBeReplacedMethods(Map<String, List<String>> couldBeReplacedMethods) {
            this.couldBeReplacedMethods = couldBeReplacedMethods;
        }

        public List<ITree> getDifferentParaMethods() {
            return differentParaMethods;
        }

        public void setDifferentParaMethods(List<ITree> differentParaMethods) {
            this.differentParaMethods = differentParaMethods;
        }

        public List<Method> getCanBeReplacedMethods() {
            return canBeReplacedMethods;
        }

        public void setCanBeReplacedMethods(List<Method> canBeReplacedMethods) {
            this.canBeReplacedMethods = canBeReplacedMethods;
        }

        public List<Method> getDiffParameterMethods() {
            return diffParameterMethods;
        }

        public void setDiffParameterMethods(List<Method> diffParameterMethods) {
            this.diffParameterMethods = diffParameterMethods;
        }
    }

}
