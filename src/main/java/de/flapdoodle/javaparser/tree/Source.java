package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;

@Immutable
public class Source {
	
	private final Optional<JavaPackage> _jpackage;

	public Source(Optional<JavaPackage> jpackage) {
		_jpackage = jpackage;
	}

	public Optional<JavaPackage> javaPackage() {
		return _jpackage;
	}
}
