package edu.hust.xzf.mutator.context;

import java.util.List;

/**
 * Information of method defined in code program.
 *
 * @author kui.liu
 */
public class Method {

    private String packageName;
    private String className;
    private ModifierType modifier;
    private String returnType;
    private String methodName;
    private List<String> parameterTypes;
    private boolean isConstructor;
    private boolean isStatic;

    public Method(String className, ModifierType modifier, String returnType, String methodName,
                  List<String> parameterTypes, boolean isConstructor, boolean isStatic) {
        super();
//		this.packageName = packageName;
        this.className = className;
        this.modifier = modifier;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.isConstructor = isConstructor;
        this.isStatic = isStatic;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public ModifierType getModifier() {
        return modifier;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    @Override
    public String toString() {
        return methodName + ":" + parameterTypes.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Method) {
            Method method = (Method) obj;
            return this.methodName.equals(method.methodName) && this.parameterTypes.size() == method.parameterTypes.size();
        }
        return false;
    }

}
