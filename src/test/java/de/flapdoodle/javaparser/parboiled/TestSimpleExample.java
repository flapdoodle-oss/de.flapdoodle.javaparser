package de.flapdoodle.javaparser.parboiled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.parboiled.support.ParsingResult;

import com.google.common.base.Optional;

import de.flapdoodle.javaparser.tree.Import;
import de.flapdoodle.javaparser.tree.Source;

public class TestSimpleExample {

	@Test
	public void withoutPackage() {
		String sourceText="public class AClass {}";
		
		Source source = parse(sourceText);
		
		assertFalse(source.javaPackage().isPresent());
	}

	@Test
	public void withImports() {
		String sourceText="package some;" +
				"" +
				"import some.other.*;" +
				"import some.other.Class;" +
				"import static some.other.Class.method;" +
				"" +
				"public class AClass {}";
		
		Source source = parse(sourceText);
		
		assertTrue(source.javaPackage().isPresent());
		List<Import> imports = source.imports();
		
		assertEquals(3,imports.size());
		
		Import wildcardImport = imports.get(0);
		assertFalse(wildcardImport.isStatic());
		assertEquals("some.other.*",wildcardImport.importDecl());
		
		Import classImport = imports.get(1);
		assertFalse(classImport.isStatic());
		assertEquals("some.other.Class",classImport.importDecl());
		
		Import staticImport = imports.get(2);
		assertTrue(staticImport.isStatic());
		assertEquals("some.other.Class.method",staticImport.importDecl());
	}
	
	@Test
	public void simpleExample() {
		String sourceText="package myPackage;\n" +
				"public class AClass {\n" +
				"\n" +
				"	/** some crazy comment\n" +
				"	 * with multiple lines\n" +
				"	 */\n" +
				"	public String add(int idx) {\n" +
				" 	return \"\";\n" +
				"	}\n" +
				"}";
		
		Source source = parse(sourceText);
		System.out.println(">"+source);
		
		assertTrue(source.javaPackage().isPresent());
		assertEquals("myPackage",source.javaPackage().get().name());
	}
	

	private Source parse(String sourceText) {
		Optional<Source> result = JavaParser.asSource(sourceText);
		assertTrue(result.isPresent());
		Source source = result.get();
		return source;
	}
	
}
