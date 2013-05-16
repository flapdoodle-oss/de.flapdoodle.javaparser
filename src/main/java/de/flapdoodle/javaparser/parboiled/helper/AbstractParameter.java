package de.flapdoodle.javaparser.parboiled.helper;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import de.flapdoodle.javaparser.tree.Parameter;


public abstract class AbstractParameter {

	public List<Parameter> asParameterList() {
		List<Parameter> ret = Lists.newArrayList();
		
		Optional<AbstractParameter> cur=Optional.of(this);
		
		Optional<Parameter> parameter;
		do {
			parameter = cur.get().asParameter();
			if (parameter.isPresent()) {
				ret.add(parameter.get());
				cur=cur.get().child();
			}
		}
		while (cur.isPresent() && parameter.isPresent());
		
		return ret;
	}

	protected abstract Optional<Parameter> asParameter();
	
	protected abstract Optional<AbstractParameter> child();
}
