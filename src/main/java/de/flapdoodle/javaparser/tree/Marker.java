/**
 * Copyright (C) 2013
 *   Michael Mosmann <michael@mosmann.de>
 *
 * with contributions from
 * 	-
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
