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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;

import de.flapdoodle.javaparser.tree.Source;


public abstract class AbstractJavaParserTest {
	
	protected static String classSourceFromMavenTestDirectory(Class<?> classWithSourceInClassPath) {
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
	
			while ((read = reader.read(cb)) > 0) {
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

	protected Source parse(String sourceText) {
		Optional<Source> result = JavaParser.asSource(sourceText);
		assertTrue(result.isPresent());
		Source source = result.get();
		return source;
	}

}
