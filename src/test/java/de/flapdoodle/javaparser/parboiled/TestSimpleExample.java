package de.flapdoodle.javaparser.parboiled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.parboiled.support.ParsingResult;

import de.flapdoodle.javaparser.tree.Source;

public class TestSimpleExample {

	@Test
	public void withoutImport() {
		String sourceText="" +
				"public class AClass {\n" +
				"\n" +
				"	/** some crazy comment\n" +
				"	 * with multiple lines\n" +
				"	 */\n" +
				"	public String add(int idx) {\n" +
				" 	return \"\";\n" +
				"	}\n" +
				"}";
		
		ParsingResult<Source> result = JavaParser.parse(sourceText);
		assertFalse(result.hasErrors());
		Source source = result.resultValue;
		assertFalse(source.javaPackage().isPresent());
		
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
		
		ParsingResult<Source> result = JavaParser.parse(sourceText);
		assertFalse(result.hasErrors());
		Source source = result.resultValue;
		System.out.println(">"+source);
		
		assertTrue(source.javaPackage().isPresent());
		assertEquals("myPackage",source.javaPackage().get().name());
	}
	

}
