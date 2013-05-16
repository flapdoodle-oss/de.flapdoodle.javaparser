package de.flapdoodle.javaparser.tree;


public class MethodDeclaration extends AbstractMarked {

	private final String _name;

	public MethodDeclaration(Marker marker, String name) {
		super(marker);
		_name = name.trim();
	}

	
	public String name() {
		return _name;
	}
}
