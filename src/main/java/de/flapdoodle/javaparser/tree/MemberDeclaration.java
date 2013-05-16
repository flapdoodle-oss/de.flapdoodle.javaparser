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

import java.util.List;

import com.google.common.collect.ImmutableList;

public class MemberDeclaration extends AbstractMarked {

	private final List<AbstractType> _types;
	private final List<MethodDeclaration> _methods;

	public MemberDeclaration(Marker marker, List<AbstractType> types, List<MethodDeclaration> methods) {
		super(marker);
		_methods = ImmutableList.copyOf(methods);
		_types = ImmutableList.copyOf(types);
	}

	public List<AbstractType> types() {
		return _types;
	}
	
	public List<MethodDeclaration> methods() {
		return _methods;
	}

}
