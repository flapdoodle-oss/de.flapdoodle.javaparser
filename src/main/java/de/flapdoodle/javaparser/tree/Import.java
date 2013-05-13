package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Import {

	private final boolean _static;
	private final String _importDecl;

	public Import(boolean isStatic, String importDecl) {
		_static = isStatic;
		_importDecl = importDecl;
	}

	public boolean isStatic() {
		return _static;
	}

	public String importDecl() {
		return _importDecl;
	}
	
	@Override
	public String toString() {
		return "import "+(_static ? "static ": "")+_importDecl+";";
	}
}
