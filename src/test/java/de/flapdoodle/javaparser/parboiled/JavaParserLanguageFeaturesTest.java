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
package de.flapdoodle.javaparser.parboiled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.flapdoodle.javaparser.tree.AbstractType;
import de.flapdoodle.javaparser.tree.AnnotationType;
import de.flapdoodle.javaparser.tree.ClassType;
import de.flapdoodle.javaparser.tree.EnumType;
import de.flapdoodle.javaparser.tree.InterfaceType;
import de.flapdoodle.javaparser.tree.Source;


public class JavaParserLanguageFeaturesTest extends AbstractJavaParserTest {
	@Test
	public void notEmbeddedClassesInOneFile() {
		String sourceText="public class AClass {}; class B{}";
		
		Source source = parse(sourceText);
		
		assertFalse(source.javaPackage().isPresent());
		assertTrue(source.imports().isEmpty());
		assertEquals(2,source.types().size());
		assertEquals("public class AClass {}",source.types().get(0).marker().marked(sourceText));
		assertEquals("class B{}",source.types().get(1).marker().marked(sourceText));
	}
	
	@Test
	public void classType() {
		String sourceText="public class AClass {}";
		
		Source source = parse(sourceText);
		
		assertFalse(source.javaPackage().isPresent());
		assertTrue(source.imports().isEmpty());
		assertEquals(1,source.types().size());
		AbstractType type = source.types().get(0);
		assertTrue(type instanceof ClassType);
		assertEquals("public class AClass {}",type.marker().marked(sourceText));
		assertEquals("AClass",type.name());
	}

	@Test
	public void interfaceType() {
		String sourceText="public interface AClass {}";
		
		Source source = parse(sourceText);
		
		assertFalse(source.javaPackage().isPresent());
		assertTrue(source.imports().isEmpty());
		assertEquals(1,source.types().size());
		AbstractType type = source.types().get(0);
		assertTrue(type instanceof InterfaceType);
		assertEquals("public interface AClass {}",type.marker().marked(sourceText));
	}

	@Test
	public void enumType() {
		String sourceText="public enum AClass {}";
		
		Source source = parse(sourceText);
		
		assertFalse(source.javaPackage().isPresent());
		assertTrue(source.imports().isEmpty());
		assertEquals(1,source.types().size());
		AbstractType type = source.types().get(0);
		assertTrue(type instanceof EnumType);
		assertEquals("public enum AClass {}",type.marker().marked(sourceText));
	}

	@Test
	public void annotationType() {
		String sourceText="public @interface AClass {}";
		
		Source source = parse(sourceText);
		
		assertFalse(source.javaPackage().isPresent());
		assertTrue(source.imports().isEmpty());
		assertEquals(1,source.types().size());
		AbstractType type = source.types().get(0);
		assertTrue(type instanceof AnnotationType);
		assertEquals("public @interface AClass {}",type.marker().marked(sourceText));
	}


}
