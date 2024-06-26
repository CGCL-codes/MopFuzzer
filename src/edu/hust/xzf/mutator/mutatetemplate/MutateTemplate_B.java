package edu.hust.xzf.mutator.mutatetemplate;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.code.analyser.JavaCodeFileParser;
import edu.hust.xzf.mutator.context.ContextReader;
import edu.hust.xzf.mutator.context.Dictionary;
import edu.hust.xzf.mutator.context.Field;
import edu.hust.xzf.mutator.context.Method;
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

public abstract class MutateTemplate_B implements IMutateTemplate_B {

    private static final Logger log = LoggerFactory.getLogger(MutateTemplate_B.class);
    private final List<Patch> patchesList = new ArrayList<>();
    protected String sourceCodePath;
    protected String suspJavaFileCode;
    protected Dictionary dic;
    protected int suspCodeStartPos, suspCodeEndPos;
    protected Map<String, String> varTypesMap = new HashMap<>();
    protected List<String> allVarNamesList = new ArrayList<>();
    protected Map<String, List<String>> allVarNamesMap = new HashMap<>();
    private File sourceCodeFile;
    private String suspiciousCodeStr;
    private ITree suspiciousCodeTree;

    protected void generatePatch(String fixedCodeStr1) {
        log.debug("Patch Candidate: " + fixedCodeStr1);
        // replace buggyCodeStr with fixedCodeStr1;
        Patch patch = new Patch(this.getClass().getSimpleName());
        patch.setFixedCodeStr1(fixedCodeStr1);
        this.patchesList.add(patch);
    }

    protected void generatePatch(int suspCodeEndPos, String fixedCodeStr1) {
        log.debug("Patch Candidate: " + fixedCodeStr1);
        // insert fixedCodeStr1 before the buggyCodeStr.
        Patch patch = new Patch(this.getClass().getSimpleName());
        patch.setBuggyCodeEndPos(suspCodeEndPos);
        patch.setFixedCodeStr1(fixedCodeStr1);
        this.patchesList.add(patch);
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
        this.patchesList.add(patch);
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
    public List<Patch> getPatches() {
        return patchesList;
    }

    @Override
    public String getSubSuspiciouCodeStr(int startPos, int endPos) {
        int beginIndex = startPos - suspCodeStartPos;
        int endIndex = endPos - suspCodeStartPos;
        return this.suspiciousCodeStr.substring(beginIndex, endIndex);
    }

    @Override
    public void setDictionary(Dictionary dictionary) {
        ITree classNodeTree = this.suspiciousCodeTree;
        while (true) {
            if (Checker.isTypeDeclaration(classNodeTree.getType())) break;
            classNodeTree = classNodeTree.getParent();
            if (classNodeTree == null) return;
        }
        String classPath = ContextReader.readClassNameAndPath(classNodeTree);
        this.dic = new Dictionary();
        if (dictionary == null) {
            String sourceCodeFileName = sourceCodeFile.getName();
            sourceCodeFileName = sourceCodeFileName.substring(0, sourceCodeFileName.length() - 5);
            if (!classPath.endsWith(sourceCodeFileName)) {
                classPath = classPath.substring(0, classPath.lastIndexOf(sourceCodeFileName + ".")) + sourceCodeFileName;
            }
            File javaCodeFile = new File(this.sourceCodePath + "/" + classPath.replace(".", "/") + ".java");
            JavaCodeFileParser jcfp = new JavaCodeFileParser(javaCodeFile);
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
        } else {
            String superClass = dictionary.findSuperClassName(classPath);
            List<String> importedDependencies = dictionary.findImportedDependencies(classPath);
            addToDictionary(dictionary, classPath);

            while (superClass != null) {
                addToDictionary(dictionary, superClass);
                String superClass_ = dictionary.findSuperClassName(superClass);
                superClass = superClass_;
            }

            if (importedDependencies != null) {
                for (String importedDependency : importedDependencies) {
                    addToDictionary(dictionary, importedDependency);
                }
            }
        }
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

}
