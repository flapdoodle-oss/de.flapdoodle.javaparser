package de.flapdoodle.javaparser.tree;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class MemberDeclaration extends AbstractMarked {

	private final List<AbstractType> _types;
	private final List<MethodDeclaration> _methods;

	public MemberDeclaration(Marker marker, List<AbstractType> types, List<MethodDeclaration> methods) {
		super(marker);
		_methods = ImmutableList.copyOf(methods);
		_types = ImmutableList.copyOf(types);
	}

	public List<AbstractType> types() {
		return _types;
	}
	
	public List<MethodDeclaration> methods() {
		return _methods;
	}

}
