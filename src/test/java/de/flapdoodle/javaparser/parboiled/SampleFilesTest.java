package de.flapdoodle.javaparser.parboiled;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;

import de.flapdoodle.javaparser.parboiled.samples.SampleClassWithMethods;
import de.flapdoodle.javaparser.tree.Source;

public class SampleFilesTest extends AbstractJavaParserTest {

	@Test
	public void classWithMethods() {
		Source result = parse(SampleClassWithMethods.class);
	}
	
	Source parse(Class<?> classWithSource) {
		return parse(classSourceFromMavenTestDirectory(classWithSource));
	}

	static String classSourceFromMavenTestDirectory(Class<?> classWithSourceInClassPath) {
		Path filePath = Paths.get("src", "test", "java").resolve(classAsFileName(classWithSourceInClassPath));
		try {
			return read(new FileInputStream(filePath.toFile()));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static String read(InputStream sourceAsStream) {
		Assert.assertNotNull(sourceAsStream);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(sourceAsStream, Charsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();

			int read;
			char[] cb = new char[512];
			CharBuffer buf = CharBuffer.wrap(cb);

			while ((read = reader.read(cb)) != -1) {
				sb.append(buf.subSequence(0, read));
			}
			return sb.toString();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String classAsFileName(Class<?> classWithSourceInClassPath) {
		return classWithSourceInClassPath.getName().replace('.', '/') + ".java";
	}
}
