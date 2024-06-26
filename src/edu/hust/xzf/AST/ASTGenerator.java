package edu.hust.xzf.AST;


import edu.hust.xzf.jdt.generator.ExpJdtTreeGenerator;
import edu.hust.xzf.jdt.tree.ITree;
import edu.hust.xzf.jdt.tree.TreeContext;
import org.eclipse.jdt.core.dom.ASTParser;

import java.io.File;
import java.io.IOException;

/**
 * The AST generator of Java code.
 * 
 * @author kui.liu
 *
 */
public class ASTGenerator {
	
	public enum TokenType {
		EXP_JDT,
		RAW_TOKEN,
	}
	
	/**
	 * Generate AST for Java code file.
	 * 
	 * @param javaFile
	 * @param type
	 * @return
	 */
	public ITree generateTreeForJavaFile(File javaFile, TokenType type) {
		ITree asTree = null;
		try {
			TreeContext tc = null;
			switch (type) {
			case EXP_JDT:
				tc = new ExpJdtTreeGenerator().generateFromFile(javaFile);
				break;
			default:
				break;
			}
			
			if (tc != null){
				asTree = tc.getRoot();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return asTree;
	}
	
	public ITree generateTreeForJavaFile(String javaFile, TokenType type) {
		ITree asTree = null;
		try {
			TreeContext tc = null;
			switch (type) {
			case EXP_JDT:
				tc = new ExpJdtTreeGenerator().generateFromFile(javaFile);
				break;
			default:
				break;
			}
			
			if (tc != null){
				asTree = tc.getRoot();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return asTree;
	}
	
	public ITree generateTreeForCodeFragment(String codeBlock, TokenType type) {
		ITree asTree = null;
		try {
			TreeContext tc = null;
			switch (type) {
			case EXP_JDT:
				tc = new ExpJdtTreeGenerator().generateFromCodeFragment(codeBlock, ASTParser.K_STATEMENTS);
				break;
			default:
				break;
			}
			
			if (tc != null){
				asTree = tc.getRoot();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return asTree;
	}
	
	public ITree generateTreeForJavaFileContent(String codeBlock, TokenType type) {
		ITree asTree = null;
		try {
			TreeContext tc = null;
			switch (type) {
			case EXP_JDT:
				tc = new ExpJdtTreeGenerator().generateFromCodeString(codeBlock);
				break;
			default:
				break;
			}
			
			if (tc != null){
				asTree = tc.getRoot();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return asTree;
	}

}
