package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class AbstractType extends AbstractMarked {

	private final String _name;

	protected AbstractType(Marker marker, String name) {
		super(marker);
		// because of rules from the parser there is a space at the end
		_name = name.trim();
	}

	public String name() {
		return _name;
	}

}
