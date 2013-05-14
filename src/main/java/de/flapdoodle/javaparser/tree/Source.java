package de.flapdoodle.javaparser.tree;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Immutable
public class Source extends AbstractMarked {
	
	private final Optional<JavaPackage> _jpackage;
	private final List<Import> _imports;
	private final List<AbstractType> _types;

	public Source(Marker marker, JavaPackage jpackage, List<Import> imports, List<AbstractType> types) {
		super(marker);
		_types = ImmutableList.copyOf(types);
		_jpackage = Optional.fromNullable(jpackage);
		_imports = ImmutableList.copyOf(imports);
	}

	public Optional<JavaPackage> javaPackage() {
		return _jpackage;
	}
	
	public List<Import> imports() {
		return _imports;
	}
	
	public List<AbstractType> types() {
		return _types;
	}
}
