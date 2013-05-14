package de.flapdoodle.javaparser.parboiled;

import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;

import de.flapdoodle.javaparser.tree.Source;


public abstract class AbstractJavaParserTest {
	
	protected Source parse(String sourceText) {
		Optional<Source> result = JavaParser.asSource(sourceText);
		assertTrue(result.isPresent());
		Source source = result.get();
		return source;
	}

}
