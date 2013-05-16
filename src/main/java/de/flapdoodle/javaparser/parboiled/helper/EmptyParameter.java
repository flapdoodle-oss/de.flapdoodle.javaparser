package de.flapdoodle.javaparser.parboiled.helper;

import com.google.common.base.Optional;

import de.flapdoodle.javaparser.tree.Parameter;


public class EmptyParameter extends AbstractParameter {

	@Override
	protected Optional<AbstractParameter> child() {
		return Optional.absent();
	}
	
	@Override
	protected Optional<Parameter> asParameter() {
		return Optional.absent();
	}
}
