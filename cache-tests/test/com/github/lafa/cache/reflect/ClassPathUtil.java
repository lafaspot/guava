/*
 * Copyright (C) 2017 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.lafa.cache.reflect;

import static com.github.lafa.cache.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.github.lafa.cache.base.StandardSystemProperty.PATH_SEPARATOR;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.github.lafa.cache.base.Splitter;

// TODO(b/65488446): Make this a public API.
/** Utility method to parse the system class path. */
final public class ClassPathUtil {
	private ClassPathUtil() {
	}

	/**
	 * Returns the URLs in the class path specified by the {@code java.class.path}
	 * {@linkplain System#getProperty system property}.
	 */
	// TODO(b/65488446): Make this a public API.
	public static URL[] parseJavaClassPath() {
		List<URL> urls = new ArrayList<>();
		for (String entry : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
			try {
				try {
					urls.add(new File(entry).toURI().toURL());
				} catch (SecurityException e) { // File.toURI checks to see if the file is a directory
					urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
				}
			} catch (MalformedURLException e) {
				AssertionError error = new AssertionError("malformed class path entry: " + entry);
				error.initCause(e);
				throw error;
			}
		}
		return urls.toArray(new URL[0]);
	}

	/** Returns the URLs in the class path. */
	public static URL[] getClassPathUrls() {
		return ClassPathUtil.class.getClassLoader() instanceof URLClassLoader
				? ((URLClassLoader) ClassPathUtil.class.getClassLoader()).getURLs()
				: parseJavaClassPath();
	}
}