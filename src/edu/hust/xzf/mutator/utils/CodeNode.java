package edu.hust.xzf.mutator.utils;

import edu.hust.xzf.jdt.tree.ITree;

public class CodeNode {
    public int startPos;
    public int endPos;
    public ITree codeAstNode;
    public String codeStr;
    public int line;

    public CodeNode(int startPos, int endPos,
                    ITree codeAstNode, String codeStr, int line) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.codeAstNode = codeAstNode;
        this.codeStr = codeStr;
        this.line = line;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof CodeNode node) {
            if (startPos != node.startPos) return false;
            if (endPos != node.endPos) return false;
            return codeStr.equals(node.codeStr);
        }
        return false;
    }
}