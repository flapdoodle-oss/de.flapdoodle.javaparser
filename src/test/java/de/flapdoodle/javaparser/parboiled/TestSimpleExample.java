package de.flapdoodle.javaparser.parboiled;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class TestSimpleExample {
	
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
		
		JavaParser parser = Parboiled.createParser(JavaParser.class);
		Rule rootRule = parser.CompilationUnit();
		ParsingResult<Node<?>> result = new ReportingParseRunner(rootRule).run(sourceText);
		assertFalse(result.hasErrors());
	}
	

}
