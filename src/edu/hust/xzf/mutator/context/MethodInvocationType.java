package edu.hust.xzf.mutator.context;

public enum MethodInvocationType { 
	Constructor,     // Constructor of the current class.
	SuperConstructor,
	TypeConstructor, // Constructor of other class.
}
