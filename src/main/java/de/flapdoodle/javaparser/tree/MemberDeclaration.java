package de.flapdoodle.javaparser.tree;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class MemberDeclaration extends AbstractMarked {

	private final List<AbstractType> _types;

	public MemberDeclaration(Marker marker, List<AbstractType> types) {
		super(marker);
		_types = ImmutableList.copyOf(types);
	}

	public List<AbstractType> types() {
		return _types;
	}

}
