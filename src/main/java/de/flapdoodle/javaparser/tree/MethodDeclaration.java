package de.flapdoodle.javaparser.tree;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class MethodDeclaration extends AbstractMarked {

	private final String _name;
	private final List<Parameter> _parameters;

	public MethodDeclaration(Marker marker, String name, List<Parameter> parameters) {
		super(marker);
		_parameters = ImmutableList.copyOf(parameters);
		_name = name.trim();
	}

	public String name() {
		return _name;
	}

	public List<Parameter> parameters() {
		return _parameters;
	}
}
