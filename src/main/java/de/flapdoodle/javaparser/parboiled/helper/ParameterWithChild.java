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
package de.flapdoodle.javaparser.parboiled.helper;

import com.google.common.base.Optional;

import de.flapdoodle.javaparser.tree.Parameter;


public class ParameterWithChild extends AbstractParameter {

	private final AbstractParameter _child;
	private final String _typeAsString;

	public ParameterWithChild(String typeAsString, AbstractParameter child) {
		_typeAsString = typeAsString;
		_child = child;
	}

	@Override
	protected Optional<AbstractParameter> child() {
		return Optional.fromNullable(_child);
	}
	
	@Override
	protected Optional<Parameter> asParameter() {
		return Optional.of(new Parameter(_typeAsString));
	}
}
