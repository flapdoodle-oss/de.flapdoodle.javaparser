package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public class JavaPackage extends AbstractMarked {

	private final String _name;

	public JavaPackage(Marker marker, String name) {
		super(marker);
		_name = name;
	}

	public String name() {
		return _name;
	}

}
