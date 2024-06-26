package edu.hust.xzf.mutator.deoptpatterns;

public class SuspNullExpStr {
    public String expStr;
    public Integer startPos;
    public Integer endPos;

    public SuspNullExpStr(String expStr, Integer startPos, Integer endPos) {
        this.expStr = expStr;
        this.startPos = startPos;
        this.endPos = endPos;
    }

//    @Override
//    public int compareTo(SuspNullExpStr o) {
//        int result = this.startPos.compareTo(o.startPos);
//        if (result == 0) {
//            result = this.endPos.compareTo(o.endPos);
//        }
//        return result;
//    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SuspNullExpStr)) return false;
        return this.expStr.equals(((SuspNullExpStr) obj).expStr);
    }

}