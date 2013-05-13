package de.flapdoodle.javaparser.parboiled;

import java.util.Collection;
import java.util.List;

import org.parboiled.support.Var;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


public class CollectionVar<T> extends Var<Collection<T>> {
	
	public boolean add(T value) {
		Collection<T> elements = Lists.newArrayList();
		if (isSet()) {
			elements=get();
		}
		return set(ImmutableList.<T>builder().addAll(elements).add(value).build());
	}

	public List<T> asList() {
		if (isSet()) {
			return ImmutableList.copyOf(get());
		}
		return ImmutableList.of();
	}

}
