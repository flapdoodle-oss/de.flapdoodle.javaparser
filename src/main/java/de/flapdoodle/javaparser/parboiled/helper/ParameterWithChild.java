package de.flapdoodle.javaparser.parboiled.helper;

import com.google.common.base.Optional;

import de.flapdoodle.javaparser.tree.Parameter;


public class ParameterWithChild extends AbstractParameter {

	private final AbstractParameter _child;

	public ParameterWithChild(AbstractParameter child) {
		_child = child;
	}

	@Override
	protected Optional<AbstractParameter> child() {
		return Optional.fromNullable(_child);
	}
	
	@Override
	protected Optional<Parameter> asParameter() {
		return Optional.of(new Parameter());
	}
}
