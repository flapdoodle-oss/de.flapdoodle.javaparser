package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public class AbstractMarked {

	private final Marker _marker;

	protected AbstractMarked(Marker marker) {
		_marker = marker;
	}

	public Marker marker() {
		return _marker;
	}
}
