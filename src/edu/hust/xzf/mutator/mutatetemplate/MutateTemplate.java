package edu.hust.xzf.mutator.mutatetemplate;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.code.analyser.JavaCodeFileParser;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.context.Dictionary;
import edu.hust.xzf.mutator.context.Field;
import edu.hust.xzf.mutator.context.Method;
import edu.hust.xzf.mutator.deoptpatterns.SuspNullExpStr;
import edu.hust.xzf.mutator.info.Patch;
import edu.hust.xzf.mutator.utils.Checker;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MutateTemplate implements IMutateTemplate {

    private static final Logger log = LoggerFactory.getLogger(MutateTemplate.class);

    private static int newVarIndex = 0;

    protected String sourceCodePath;
    protected String suspJavaFileCode;
    protected Dictionary dic;
    private File sourceCodeFile;
    private String suspiciousCodeStr;
    private Patch patch;
    protected int suspCodeStartPos, suspCodeEndPos;
    protected Map<String, String> varTypesMap = new HashMap<>();
    protected List<String> allVarNamesList = new ArrayList<>();
    protected Map<String, List<String>> allVarNamesMap = new HashMap<>();
    private ITree suspiciousCodeTree;

    protected int offset = 0;

    protected String packageName = "";

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        if (!packageName.isEmpty())
            this.packageName = packageName + ".";
    }


    public void updateMutatorPoint() {
        patch.setOffset(offset);
    }


    protected void generatePatch(String fixedCodeStr1) {
        log.debug("Patch Candidate: " + fixedCodeStr1);
        // replace buggyCodeStr with fixedCodeStr1;
        Patch patch = new Patch(this.getClass().getSimpleName());
        patch.setFixedCodeStr1(fixedCodeStr1);
        this.patch = patch;
    }

    protected void generatePatch(int suspCodeEndPos, String fixedCodeStr1) {
        log.debug("Patch Candidate: " + fixedCodeStr1);
        // insert fixedCodeStr1 before the buggyCodeStr.
        Patch patch = new Patch(this.getClass().getSimpleName());
        patch.setBuggyCodeEndPos(suspCodeEndPos);
        patch.setFixedCodeStr1(fixedCodeStr1);
        this.patch = patch;
    }

    protected void generatePatch(int suspCodeStartPos, int suspCodeEndPos, String fixedCodeStr1, String fixedCodeStr2) {
        log.debug("Patch Candidate: " + fixedCodeStr1 + "\n" + fixedCodeStr2);
        // 1. suspCodeEndPos > suspCodeStartPos && suspCodeStartPos == suspNode.suspCodeStartPos Insert a block-held statement to surround the buggy code.
        // 2. suspCodeEndPos > suspCodeStartPos && suspCodeStartPos < suspNode.suspCodeStartPos && suspCodeStartPos > 0: remove method declaration.
        // 3. suspCodeEndPos > suspCodeStartPos && "MOVE-BUGGY-STATEMENT".equals(fixedCodeStr2): move statement position.
        Patch patch = new Patch(this.getClass().getSimpleName());
        patch.setBuggyCodeStartPos(suspCodeStartPos);
        patch.setBuggyCodeEndPos(suspCodeEndPos);
        patch.setFixedCodeStr1(fixedCodeStr1);
        patch.setFixedCodeStr2(fixedCodeStr2);
        this.patch = patch;
    }

    public void setSourceCodePath(String sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
    }

    public void setSourceCodePath(String sourceCodePath, File sourceCodeFile) {
        this.sourceCodePath = sourceCodePath;
        this.sourceCodeFile = sourceCodeFile;
        try {
            this.suspJavaFileCode = FileUtils.readFileToString(sourceCodeFile, "UTF-8");
        } catch (IOException e) {
            this.suspJavaFileCode = "";
        }
    }

    @Override
    public String getSuspiciousCodeStr() {
        return suspiciousCodeStr;
    }

    @Override
    public void setSuspiciousCodeStr(String suspiciousCodeStr) {
        this.suspiciousCodeStr = suspiciousCodeStr;
    }

    @Override
    public ITree getSuspiciousCodeTree() {
        return suspiciousCodeTree;
    }


    @Override
    public void setSuspiciousCodeTree(ITree suspiciousCodeTree) {
        this.suspiciousCodeTree = suspiciousCodeTree;
        suspCodeStartPos = suspiciousCodeTree.getPos();
        suspCodeEndPos = suspCodeStartPos + suspiciousCodeTree.getLength();
    }

    @Override
    public Patch getPatch() {
        return patch;
    }

    @Override
    public String getSubSuspiciouCodeStr(int startPos, int endPos) {
        int beginIndex = startPos - suspCodeStartPos;
        int endIndex = endPos - suspCodeStartPos;
        return this.suspiciousCodeStr.substring(beginIndex, endIndex);
    }

    @Override
    public String getFileCodeStr(int startPos, int endPos) {
        return this.suspJavaFileCode.substring(startPos, endPos);
    }

    @Override
    public void setDictionary(Dictionary dictionary) {
        ITree classNodeTree = this.suspiciousCodeTree;
        while (true) {
            if (Checker.isTypeDeclaration(classNodeTree.getType())) break;
            classNodeTree = classNodeTree.getParent();
            if (classNodeTree == null) return;
        }
//        String classPath = ContextReader.readClassNameAndPath(classNodeTree);
        this.dic = new Dictionary();
        if (dictionary == null) {
//            String sourceCodeFileName = sourceCodeFile.getName();
//            sourceCodeFileName = sourceCodeFileName.substring(0, sourceCodeFileName.length() - 5);
//            if (!classPath.endsWith(sourceCodeFileName)) {
//                classPath = classPath.substring(0, classPath.lastIndexOf(sourceCodeFileName + ".")) + sourceCodeFileName;
//            }
            JavaCodeFileParser jcfp = new JavaCodeFileParser(this.sourceCodeFile);
            dic.setAllFields(jcfp.fields);
            dic.setImportedDependencies(jcfp.importMaps);
            dic.setMethods(jcfp.methods);
            dic.setSuperClasses(jcfp.superClassNames);

//			List<String> importedClasses = jcfp.importDeclarations;
//			for (String importedClass : importedClasses) {
//				File javaFile = new File(this.sourceCodePath + importedClass.replace(".", "/") + ".java");
//				if (!javaFile.exists()) continue;
//				jcfp = new JavaCodeFileParser(javaFile);
//				dic.setAllFields(jcfp.fields);
//				dic.setImportedDependencies(jcfp.importMaps);
//				dic.setMethods(jcfp.methods);
//				dic.setSuperClasses(jcfp.superClassNames);
//			}
        }
//        else {
//            String superClass = dictionary.findSuperClassName(classPath);
//            List<String> importedDependencies = dictionary.findImportedDependencies(classPath);
//            addToDictionary(dictionary, classPath);
//
//            while (superClass != null) {
//                addToDictionary(dictionary, superClass);
//                String superClass_ = dictionary.findSuperClassName(superClass);
//                superClass = superClass_;
//            }
//
//            if (importedDependencies != null) {
//                for (String importedDependency : importedDependencies) {
//                    addToDictionary(dictionary, importedDependency);
//                }
//            }
//        }
    }

    private void addToDictionary(Dictionary dictionary, String classPath) {
        String superClass = dictionary.findSuperClassName(classPath);
        List<String> importedDependencies = dictionary.findImportedDependencies(classPath);
        List<Field> fields = dictionary.findFieldsByClassPath(classPath);
        List<Method> methods = dictionary.getMethods().get(classPath);

        dic.getAllFields().put(classPath, fields);
        dic.getMethods().put(classPath, methods);
        if (superClass != null) dic.getSuperClasses().put(classPath, superClass);
        dic.getImportedDependencies().put(classPath, importedDependencies);
    }

    protected void clear() {
        allVarNamesMap.clear();
        varTypesMap.clear();
        allVarNamesList.clear();
        if (dic != null) dic.clear();
    }

    protected void identifySuspiciousVariables(ITree suspCodeAst, List<String> allSuspVariables, List<SuspNullExpStr> suspNullExpStrs) {
        List<ITree> children = suspCodeAst.getChildren();
        for (ITree child : children) {
            int childType = child.getType();
            if (Checker.isSimpleName(childType)) {
                int parentType = suspCodeAst.getType();
                if ((Checker.isAssignment(parentType) || Checker.isVariableDeclarationFragment(parentType))
                        && suspCodeAst.getChildPosition(child) == 0) {
                    continue;
                }
                if ((Checker.isQualifiedName(parentType) || Checker.isFieldAccess(parentType) || Checker.isSuperFieldAccess(parentType)) &&
                        suspCodeAst.getChildPosition(child) == children.size() - 1) {
                    continue;
                }

                String varName = ContextReader.readVariableName(child);
                if (varName != null && !varName.endsWith(".length")) {
                    int startPos = child.getPos();
                    int endPos = startPos + child.getLength();
                    SuspNullExpStr snes = new SuspNullExpStr(varName, startPos, endPos);
                    if (!suspNullExpStrs.contains(snes) && !Checker.isAssignment(suspCodeAst.getType())
                            && !Checker.isVariableDeclarationFragment(suspCodeAst.getType())) suspNullExpStrs.add(snes);
                    if (!allSuspVariables.contains(varName)) allSuspVariables.add(varName);
                } else identifySuspiciousVariables(child, allSuspVariables, suspNullExpStrs);
            } else if (Checker.isMethodInvocation(childType)) {
                List<ITree> subChildren = child.getChildren();
                if (subChildren.size() > 2) {
                    int startPos = child.getPos();
                    ITree subChild = subChildren.get(subChildren.size() - 2);
                    int endPos = subChild.getPos() + subChild.getLength();
                    String suspExpStr = this.getSubSuspiciouCodeStr(startPos, endPos);
                    SuspNullExpStr snes = new SuspNullExpStr(suspExpStr, startPos, endPos);
                    if (!suspNullExpStrs.contains(snes) && !Checker.isAssignment(suspCodeAst.getType())
                            && !Checker.isVariableDeclarationFragment(suspCodeAst.getType())) suspNullExpStrs.add(snes);
                }
                identifySuspiciousVariables(child, allSuspVariables, suspNullExpStrs);
            } else if (Checker.isArrayAccess(childType)) {
                int startPos = child.getPos();
                int endPos = startPos + child.getLength();
                String suspExpStr = this.getSubSuspiciouCodeStr(startPos, endPos);
                SuspNullExpStr snes = new SuspNullExpStr(suspExpStr, startPos, endPos);
                if (!suspNullExpStrs.contains(snes) && !Checker.isAssignment(suspCodeAst.getType())
                        && !Checker.isVariableDeclarationFragment(suspCodeAst.getType())) suspNullExpStrs.add(snes);
                identifySuspiciousVariables(child, allSuspVariables, suspNullExpStrs);
            } else if (Checker.isQualifiedName(childType) || Checker.isFieldAccess(childType) || Checker.isSuperFieldAccess(childType)) {
                int parentType = suspCodeAst.getType();
                if ((Checker.isAssignment(parentType) || Checker.isVariableDeclarationFragment(parentType))
                        && suspCodeAst.getChildPosition(child) == 0) {
                    continue;
                }
                int startPos = child.getPos();
                int endPos = startPos + child.getLength();
                String suspExpStr = this.getSubSuspiciouCodeStr(startPos, endPos);

                if (!suspExpStr.endsWith(".length")) {
                    SuspNullExpStr snes = new SuspNullExpStr(suspExpStr, startPos, endPos);
                    if (!suspNullExpStrs.contains(snes) && !Checker.isAssignment(suspCodeAst.getType())
                            && !Checker.isVariableDeclarationFragment(suspCodeAst.getType())) suspNullExpStrs.add(snes);
                    if (!allSuspVariables.contains(suspExpStr)) allSuspVariables.add(suspExpStr);
                }
                int index1 = suspExpStr.indexOf(".");
                int index2 = suspExpStr.lastIndexOf(".");
                if (index1 != index2) identifySuspiciousVariables(child, allSuspVariables, suspNullExpStrs);
            } else if (Checker.isFieldAccess(childType) || Checker.isSuperFieldAccess(childType)) {
                int parentType = suspCodeAst.getType();
                if ((Checker.isAssignment(parentType) || Checker.isVariableDeclarationFragment(parentType))
                        && suspCodeAst.getChildPosition(child) == 0) {
                    continue;
                }
                String nameStr = child.getLabel(); // "this."/"super." + varName
                if (!allSuspVariables.contains(nameStr)) allSuspVariables.add(nameStr);
            } else if (Checker.isComplexExpression(childType)) {
                identifySuspiciousVariables(child, allSuspVariables, suspNullExpStrs);
            } else if (Checker.isStatement(childType)) break;
        }
    }


    protected String generateUniqueVarName() {
        return "var" + (newVarIndex++);
    }

    protected String generateUniqueClassName() {
        return "Class" + (newVarIndex++);
    }

    protected String generateUniqueMethodName() {
        return "method" + (newVarIndex++);
    }

    private boolean checkTryCatch(ITree tree) {
        boolean flag = false;
        ITree parent = tree.getParent();

        while (parent != null) {
            if (Checker.isTryStatement(parent.getType())) {
                int size = parent.getChildren().size();
                String catchStmt = parent.getChild(size - 1).getChild(0).getChild(1).getLabel();
                if (catchStmt.equals("eeeeeeee")) {
                    flag = true;
                    break;
                }
            }
            parent = parent.getParent();
        }
        return flag;
    }

    private boolean checkImport(ITree tree, String str) {
        boolean flag = false;
        ITree parent = tree.getParent();

        while (parent != null) {
            if (Checker.isCompilationUnit(parent.getType())) {
                for (ITree child : parent.getChildren()) {
                    if (Checker.isTypeDeclaration(child.getType())) {
                        String classLabel = child.getLabel();
                        String className = classLabel.substring(classLabel.indexOf("ClassName:") + 10);
                        className = className.substring(0, className.indexOf(", "));
                        if (className.equals(str)) {
                            flag = true;
                            break;
                        }
                    }
                }
            }
            parent = parent.getParent();
        }
        return flag;
    }

    public void setImportTryCatch(boolean importTryCatch) {
        boolean imported = checkTryCatch(suspiciousCodeTree);
        if (!imported) {
            patch.setInsertTryCatch(importTryCatch);
        }
    }

    public void setImportPos(int importPos) {
        patch.setImportPos(importPos);
    }

    public void setImportPos2(int importPos) {
        patch.setImportPos2(importPos);
    }

    public void setImportBoolean(boolean importBoolean) {
        boolean imported = checkImport(suspiciousCodeTree, "MyBoolean");
        if (!imported) {
            patch.setImportBoolean(importBoolean);
        }
    }

    public void setImportChar(boolean importChar) {
        boolean imported = checkImport(suspiciousCodeTree, "MyChar");
        if (!imported) {
            patch.setImportChar(importChar);
        }
    }

    public void setImportByte(boolean importByte) {
        boolean imported = checkImport(suspiciousCodeTree, "MyByte");
        if (!imported) {
            patch.setImportByte(importByte);
        }
    }

    public void setImportShort(boolean importShort) {
        boolean imported = checkImport(suspiciousCodeTree, "MyShort");
        if (!imported) {
            patch.setImportShort(importShort);
        }
    }

    public void setImportInt(boolean importInt) {
        boolean imported = checkImport(suspiciousCodeTree, "MyInteger");
        if (!imported) {
            patch.setImportInt(importInt);
        }
    }

    public void setImportLong(boolean importLong) {
        boolean imported = checkImport(suspiciousCodeTree, "MyLong");
        if (!imported) {
            patch.setImportLong(importLong);
        }
    }

    public void setImportFloat(boolean importFloat) {
        boolean imported = checkImport(suspiciousCodeTree, "MyFloat");
        if (!imported) {
            patch.setImportFloat(importFloat);
        }
    }

    public void setImportDouble(boolean importDouble) {
        boolean imported = checkImport(suspiciousCodeTree, "MyDouble");
        if (!imported) {
            patch.setImportDouble(importDouble);
        }
    }

    public void setImportString(boolean importString) {
        boolean imported = checkImport(suspiciousCodeTree, "MyString");
        if (!imported) {
            patch.setImportString(importString);
        }
    }

    public void setIsInlined(String isInlined) {
        patch.setInlined(isInlined);
    }

    public void addNewField(String newFieldName) {
        patch.setNewFieldName(newFieldName);
    }


}
