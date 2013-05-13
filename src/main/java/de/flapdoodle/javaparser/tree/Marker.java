package de.flapdoodle.javaparser.tree;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

@Immutable
public class Marker {

	private final int _start;
	private final int _end;

	public Marker(int start, int end) {
		Preconditions.checkArgument(start <= end, "start > end");
		_start = start;
		_end = end;
	}

	public int start() {
		return _start;
	}

	public int end() {
		return _end;
	}
	
	public String marked(String source) {
		return source.substring(_start,_end);
	}
	
	@Override
	public String toString() {
		return "["+_start+":"+_end+"]";
	}
}
