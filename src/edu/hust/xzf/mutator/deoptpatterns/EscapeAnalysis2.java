package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;
import edu.hust.xzf.mutator.utils.Checker;

import java.util.ArrayList;
import java.util.List;

import static edu.hust.xzf.mutator.utils.CodeUtils.getRandomElementFromList;

public class EscapeAnalysis2 extends MutateTemplate {

    private boolean isStatic = false;

    List<ITree> news = new ArrayList<>();

    @Override
    public void generatePatches() {
        ITree tree = getSuspiciousCodeTree();
        identifyNews(tree);
        ITree select = getRandomElementFromList(news);
        if (select == null) return;
        ITree type;
        if (Checker.isClassInstanceCreation(select.getType()))
            type = select.getChild(1);
        else
            type = select.getChild(0);
        String typeStr = type.getLabel();
        String fixedCodeStr = "";
        String newStr = getSubSuspiciouCodeStr(select.getPos(), select.getEndPos());
        fixedCodeStr += getSubSuspiciouCodeStr(suspCodeStartPos, select.getPos());
        String remainStr = getSubSuspiciouCodeStr(select.getEndPos(), suspCodeEndPos);
        String var = generateUniqueVarName();

        if (fixedCodeStr.isEmpty() && remainStr.contentEquals(";")) {
            // new Clazz()
            fixedCodeStr += var + " = " + newStr;
        } else {
            // (type)(var = newStr)
            fixedCodeStr += "((" + typeStr + ")(" + var + " = " + newStr + "))";
        }
//        fixedCodeStr += "((" + typeStr + ")(" + var + " = " + newStr + "))";
        fixedCodeStr += remainStr;
        generatePatch(fixedCodeStr);
        setImportPos(locateNewField(select));
        addNewField((isStatic ? "static" : "") + " Object " + var + ";\n");
        offset += 1;
    }

    private int locateNewField(ITree tree) {
        ITree parent = tree.getParent();
        while (parent != null) {
            if (Checker.isMethodDeclaration(parent.getType())) {
                for (ITree child : parent.getChildren()) {
                    if (Checker.isModifier(child.getType())) {
                        String modifier = child.getLabel();
                        if (modifier.equals("static")) {
                            isStatic = true;
                            break;
                        }
                    }
                }
                break;
            }
            parent = parent.getParent();
        }
        return parent.getPos();
    }

    private void identifyNews(ITree tree) {
        for (ITree child : tree.getChildren()) {
            if (Checker.isClassInstanceCreation(child.getType())
                    || Checker.isArrayCreation(child.getType())) {
                ITree type = child.getChild(1);
                String typeStr = type.getLabel();
                if (!typeStr.startsWith("My"))
                    news.add(child);
            }
        }
        for (ITree child : tree.getChildren()) {
            identifyNews(child);
        }
    }

}
