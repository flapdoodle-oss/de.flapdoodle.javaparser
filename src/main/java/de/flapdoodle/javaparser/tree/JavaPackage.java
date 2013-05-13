package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public class JavaPackage {

	private final String _name;

	public JavaPackage(String name) {
		_name = name;
	}

	public String name() {
		return _name;
	}

}
