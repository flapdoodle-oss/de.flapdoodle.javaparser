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

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Immutable
public class Source extends AbstractMarked {
	
	private final Optional<JavaPackage> _jpackage;
	private final List<Import> _imports;
	private final List<AbstractType> _types;

	public Source(Marker marker, JavaPackage jpackage, List<Import> imports, List<AbstractType> types) {
		super(marker);
		_types = ImmutableList.copyOf(types);
		_jpackage = Optional.fromNullable(jpackage);
		_imports = ImmutableList.copyOf(imports);
	}

	public Optional<JavaPackage> javaPackage() {
		return _jpackage;
	}
	
	public List<Import> imports() {
		return _imports;
	}
	
	public List<AbstractType> types() {
		return _types;
	}
}
