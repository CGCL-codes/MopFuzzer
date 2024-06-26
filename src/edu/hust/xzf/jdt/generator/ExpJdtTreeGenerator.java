package edu.hust.xzf.jdt.generator;

import edu.hust.xzf.jdt.visitor.AbstractJdtVisitor;
import edu.hust.xzf.jdt.visitor.ExpJdtVisitor;

@Register(id = "java-jdt-exp")
public class ExpJdtTreeGenerator extends AbstractJdtTreeGenerator {
    @Override
    protected AbstractJdtVisitor createVisitor() {
        return new ExpJdtVisitor();
    }
}
