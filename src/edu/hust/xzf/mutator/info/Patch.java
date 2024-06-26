package edu.hust.xzf.mutator.info;

/**
 * Store the information of generated patches.
 *
 * @author kui.liu
 */
public class Patch {

    public String buggyFileName;
    private String fixPattern;
    private String buggyCodeStr = "";
    private String fixedCodeStr1 = "";
    private String fixedCodeStr2 = null;
    private int buggyCodeStartPos = -1;
    private boolean isImportBoolean = false;
    private boolean isImportChar = false;
    private boolean isImportByte = false;
    private boolean isImportShort = false;
    private boolean isImportInt = false;
    private boolean isImportLong = false;
    private boolean isImportFloat = false;
    private boolean isImportDouble = false;
    private boolean isImportString = false;

    private String newFieldName = null;

    private String isInlined = null;
    private int importPos = -1;
    // 0: never, 1: to insert 2: inserted
    public boolean insertTryCatch = false;
    private int importPos2 = -1;

    private int offset = 0;
    /*
     * if (buggyCodeEndPos == buggyCodeStartPos) then
     * 		replace buggyCodeStr with fixedCodeStr1;
     * else if (buggyCodeEndPos > buggyCodeStartPos && buggyCodeStartPos == -1) then
     * 		if (buggyCodeEndPos == 0) then
     * 			insert the missing override method. // FIXME: This is removed.
     * 		else if (buggyCodeEndPos == originalBuggyCodeStartPos) then
     * 			insert fixedCodeStr1 before buggyCodeStr;
     * else if (buggyCodeEndPos > buggyCodeStartPos && buggyCodeStartPos == originalBuggyCodeStartPos) then
     * 		fixedCodeStr1 + buggCodeStr + fixedCodeStr2;
     * else if (buggyCodeEndPos > buggyCodeStartPos && buggyCodeStartPos < originalBuggyCodeStartPos) then
     * 		remove the buggy method declaration.
     */
    private int buggyCodeEndPos = -1;

    public Patch(String fixPattern) {
        this.fixPattern = fixPattern;
    }

    public String getBuggyCodeStr() {
        return buggyCodeStr;
    }

    public void setBuggyCodeStr(String buggyCodeStr) {
        this.buggyCodeStr = buggyCodeStr;
    }

    public String getFixedCodeStr1() {
        return fixedCodeStr1;
    }

    public void setFixedCodeStr1(String fixedCodeStr1) {
        this.fixedCodeStr1 = fixedCodeStr1;
    }

    public String getFixedCodeStr2() {
        return fixedCodeStr2;
    }

    public void setFixedCodeStr2(String fixedCodeStr2) {
        this.fixedCodeStr2 = fixedCodeStr2;
    }

    public int getBuggyCodeStartPos() {
        return buggyCodeStartPos;
    }

    public void setBuggyCodeStartPos(int buggyCodeStartPos) {
        this.buggyCodeStartPos = buggyCodeStartPos;
    }

    public int getBuggyCodeEndPos() {
        return buggyCodeEndPos;
    }

    public void setBuggyCodeEndPos(int buggyCodeEndPos) {
        this.buggyCodeEndPos = buggyCodeEndPos;
    }

    public String getFixPattern() {
        return fixPattern;
    }

    public void setFixPattern(String fixPattern) {
        this.fixPattern = fixPattern;
    }

    public String getBuggyFileName() {
        return buggyFileName;
    }

    public void setBuggyFileName(String buggyFileName) {
        this.buggyFileName = buggyFileName;
    }

    @Override
    public String toString() {
        return this.fixedCodeStr1 + "\n" + this.fixedCodeStr2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Patch p) {
            if (!buggyFileName.equals(p.buggyFileName)) return false;
            if (!buggyCodeStr.equals(p.buggyCodeStr)) return false;
            if (buggyCodeStartPos != p.buggyCodeStartPos) return false;
            if (buggyCodeEndPos != p.buggyCodeEndPos) return false;
            if (!fixedCodeStr1.equals(p.fixedCodeStr1)) return false;
            if (fixedCodeStr2 == null) {
                return p.fixedCodeStr2 == null;
            } else return fixedCodeStr2.equals(p.fixedCodeStr2);
        } else return false;
    }

    public void setImportBoolean(boolean importBoolean) {
        this.isImportBoolean = importBoolean;
    }

    public void setImportChar(boolean importChar) {
        this.isImportChar = importChar;
    }

    public void setImportByte(boolean importByte) {
        this.isImportByte = importByte;
    }

    public void setImportShort(boolean importShort) {
        this.isImportShort = importShort;
    }

    public void setImportInt(boolean importInt) {
        this.isImportInt = importInt;
    }

    public void setImportLong(boolean importLong) {
        this.isImportLong = importLong;
    }

    public void setImportFloat(boolean importFloat) {
        this.isImportFloat = importFloat;
    }

    public void setImportDouble(boolean importDouble) {
        this.isImportDouble = importDouble;
    }

    public void setImportString(boolean importString) {
        this.isImportString = importString;
    }

    public void setInsertTryCatch(boolean insertTryCatch) {
        this.insertTryCatch = insertTryCatch;
    }

    public String doImportTryCatch(String patchedJavaFile) {

        if (insertTryCatch) {
            String importStr1 = "\ntry {\n";
            String importStr2 = "\n}catch (Exception eeeeeeee){" +
                    "throw new RuntimeException(eeeeeeee);}\n";

            return patchedJavaFile.substring(0, importPos)
                    + importStr1 + patchedJavaFile.substring(importPos, importPos2) + importStr2
                    + patchedJavaFile.substring(importPos2);

        } else
            return patchedJavaFile;
    }

    public String doImport(String patchedJavaFile) {
        String importStr = "";
        if (isImportBoolean) {
            importStr += "\nclass MyBoolean {\n" +
                    "public boolean value;\n" +
                    "    public MyBoolean(boolean value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public boolean v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportShort) {
            importStr += "\nclass MyShort {\n" +
                    "public short value;\n" +
                    "    public MyShort(short value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public short v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportByte) {
            importStr += "\nclass MyByte {\n" +
                    "public byte value;\n" +
                    "    public MyByte(byte value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public byte v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportChar) {
            importStr += "\nclass MyChar {\n" +
                    "public char value;\n" +
                    "    public MyChar(char value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public char v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportInt) {
            importStr += "\nclass MyInteger {\n" +
                    "public int value;\n" +
                    "    public MyInteger(int value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public int v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportLong) {
            importStr += "\nclass MyLong {\n" +
                    "public long value;\n" +
                    "    public MyLong(long value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public long v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportFloat) {
            importStr += "\nclass MyFloat {\n" +
                    "public float value;\n" +
                    "    public MyFloat(float value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public float v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportDouble) {
            importStr += "\nclass MyDouble {\n" +
                    "public double value;\n" +
                    "    public MyDouble(double value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public double v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        } else if (isImportString) {
            importStr += "\nclass MyString {\n" +
                    "public String value;\n" +
                    "    public MyString(String value) {\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "    public String v() {\n" +
                    "        return value;\n" +
                    "    }\n" +
                    "}\n";
        }
        patchedJavaFile += importStr;
        return patchedJavaFile;
    }

    public String doImportInlined(String patchedJavaFile) {
        if (this.isInlined == null)
            return patchedJavaFile;
        return patchedJavaFile.substring(0, importPos)
                + this.isInlined + patchedJavaFile.substring(importPos);

    }

    public int getImportPos() {
        return importPos;
    }

    public void setImportPos(int importPos) {
        this.importPos = importPos;
    }

    public int getImportPos2() {
        return importPos2;
    }

    public void setImportPos2(int importPos) {
        this.importPos2 = importPos;
    }

    public void setInlined(String inlined) {
        this.isInlined = inlined;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setNewFieldName(String newFieldName) {
        this.newFieldName = newFieldName;
    }

    public String doAddNewFiled(String patchedJavaFile) {
        if (this.newFieldName == null)
            return patchedJavaFile;
        return patchedJavaFile.substring(0, importPos)
                + this.newFieldName + patchedJavaFile.substring(importPos);
    }

}
