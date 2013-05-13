package de.flapdoodle.javaparser.tree;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Immutable
public class Source extends AbstractMarked {
	
	private final Optional<JavaPackage> _jpackage;
	private final List<Import> _imports;

	public Source(Marker marker, JavaPackage jpackage, List<Import> imports) {
		super(marker);
		_jpackage = Optional.fromNullable(jpackage);
		_imports = ImmutableList.copyOf(imports);
	}

	public Optional<JavaPackage> javaPackage() {
		return _jpackage;
	}
	
	public List<Import> imports() {
		return _imports;
	}
}
