/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lafa.cache.common.testing;

import static com.github.lafa.cache.base.Preconditions.checkArgument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.github.lafa.cache.base.CharMatcher;
import com.github.lafa.cache.base.Equivalence;
import com.github.lafa.cache.base.Joiner;
import com.github.lafa.cache.base.Predicate;
import com.github.lafa.cache.base.Predicates;
import com.github.lafa.cache.base.Splitter;
import com.github.lafa.cache.base.Stopwatch;
import com.github.lafa.cache.base.Ticker;
import com.github.lafa.cache.collect.ClassToInstanceMap;

/**
 * Supplies an arbitrary "default" instance for a wide range of types, often
 * useful in testing utilities.
 *
 * <p>
 * Covers arrays, enums and common types defined in {@code java.lang},
 * {@code java.lang.reflect}, {@code java.io}, {@code java.nio},
 * {@code java.math}, {@code java.util}, {@code
 * java.util.concurrent}, {@code java.util.regex},
 * {@code com.google.common.base}, {@code
 * com.google.common.collect} and {@code com.google.common.primitives}. In
 * addition, if the type exposes at least one public static final constant of
 * the same type, one of the constants will be used; or if the class exposes a
 * public parameter-less constructor then it will be "new"d and returned.
 *
 * <p>
 * All default instances returned by {@link #get} are generics-safe. Clients
 * won't get type errors for using {@code get(Comparator.class)} as a
 * {@code Comparator<Foo>}, for example. Immutable empty instances are returned
 * for collection types; {@code ""} for string; {@code 0} for number types;
 * reasonable default instance for other stateless types. For mutable types, a
 * fresh instance is created each time {@code get()} is called.
 *
 * @author Kevin Bourrillion
 * @author Ben Yu
 * @since 12.0
 */

public final class ArbitraryInstances {

	private static final Comparator<Field> BY_FIELD_NAME = new Comparator<Field>() {
		@Override
		public int compare(Field left, Field right) {
			return left.getName().compareTo(right.getName());
		}
	};

	/**
	 * Returns a new {@code MatchResult} that corresponds to a successful match.
	 * Apache Harmony (used in Android) requires a successful match in order to
	 * generate a {@code MatchResult}: http://goo.gl/5VQFmC
	 */
	private static MatchResult newMatchResult() {
		Matcher matcher = Pattern.compile(".").matcher("X");
		matcher.find();
		return matcher.toMatchResult();
	}

	final static ClassToInstanceMap<Object> DEFAULTS = new ClassToInstanceMap<>();

	/**
	 * type → implementation. Inherently mutable interfaces and abstract classes are
	 * mapped to their default implementations and are "new"d upon get().
	 */
	final static ConcurrentMap<Class<?>, Class<?>> implementations = new ConcurrentHashMap<>();
	static {
		// primitives
		DEFAULTS.put(Object.class, "");
		DEFAULTS.put(Number.class, 0);
		DEFAULTS.put(BigInteger.class, BigInteger.ZERO);
		DEFAULTS.put(BigDecimal.class, BigDecimal.ZERO);
		DEFAULTS.put(CharSequence.class, "");
		DEFAULTS.put(String.class, "");
		DEFAULTS.put(Pattern.class, Pattern.compile(""));
		DEFAULTS.put(MatchResult.class, newMatchResult());
		DEFAULTS.put(TimeUnit.class, TimeUnit.SECONDS);
		DEFAULTS.put(Charset.class, StandardCharsets.UTF_8);
		DEFAULTS.put(Currency.class, Currency.getInstance(Locale.US));
		DEFAULTS.put(Locale.class, Locale.US);
		DEFAULTS.put(Optional.class, Optional.empty());
		DEFAULTS.put(OptionalInt.class, OptionalInt.empty());
		DEFAULTS.put(OptionalLong.class, OptionalLong.empty());
		DEFAULTS.put(OptionalDouble.class, OptionalDouble.empty());
		// common.base
		DEFAULTS.put(CharMatcher.class, CharMatcher.none());
		DEFAULTS.put(Joiner.class, Joiner.on(','));
		DEFAULTS.put(Splitter.class, Splitter.on(','));
		DEFAULTS.put(Predicate.class, Predicates.alwaysTrue());
		DEFAULTS.put(Equivalence.class, Equivalence.equals());
		DEFAULTS.put(Ticker.class, Ticker.systemTicker());
		DEFAULTS.put(Stopwatch.class, Stopwatch.createUnstarted());
		// io types
		DEFAULTS.put(InputStream.class, new ByteArrayInputStream(new byte[0]));
		DEFAULTS.put(ByteArrayInputStream.class, new ByteArrayInputStream(new byte[0]));
		DEFAULTS.put(Readable.class, new StringReader(""));
		DEFAULTS.put(Reader.class, new StringReader(""));
		DEFAULTS.put(StringReader.class, new StringReader(""));
		DEFAULTS.put(Buffer.class, ByteBuffer.allocate(0));
		DEFAULTS.put(CharBuffer.class, CharBuffer.allocate(0));
		DEFAULTS.put(ByteBuffer.class, ByteBuffer.allocate(0));
		DEFAULTS.put(ShortBuffer.class, ShortBuffer.allocate(0));
		DEFAULTS.put(IntBuffer.class, IntBuffer.allocate(0));
		DEFAULTS.put(LongBuffer.class, LongBuffer.allocate(0));
		DEFAULTS.put(FloatBuffer.class, FloatBuffer.allocate(0));
		DEFAULTS.put(DoubleBuffer.class, DoubleBuffer.allocate(0));
		DEFAULTS.put(File.class, new File(""));
		// All collections are immutable empty. So safe for any type parameter.
		DEFAULTS.put(Iterator.class, new HashSet<>().iterator());
		DEFAULTS.put(Iterable.class, new HashSet<>());
		DEFAULTS.put(Collection.class, new ArrayList<>());
		DEFAULTS.put(List.class, new ArrayList<>());
		DEFAULTS.put(Set.class, new HashSet<>());
		DEFAULTS.put(SortedSet.class, new TreeSet<>());
		DEFAULTS.put(NavigableSet.class, new TreeSet<>());
		DEFAULTS.put(Map.class, new HashMap<>());
		DEFAULTS.put(SortedMap.class, new TreeMap<>());
		DEFAULTS.put(NavigableMap.class, new TreeMap<>());
		// reflect
		DEFAULTS.put(AnnotatedElement.class, Object.class);
		DEFAULTS.put(GenericDeclaration.class, Object.class);
		DEFAULTS.put(Type.class, Object.class);
	}

	private static <T> void setImplementation(Class<T> type, Class<? extends T> implementation) {
		checkArgument(type != implementation, "Don't register %s to itself!", type);
		checkArgument(!DEFAULTS.containsKey(type), "A default value was already registered for %s", type);
		checkArgument(implementations.put(type, implementation) == null, "Implementation for %s was already registered",
				type);
	}

	static {
		setImplementation(Appendable.class, StringBuilder.class);
		setImplementation(BlockingQueue.class, LinkedBlockingDeque.class);
		setImplementation(BlockingDeque.class, LinkedBlockingDeque.class);
		setImplementation(ConcurrentMap.class, ConcurrentHashMap.class);
		setImplementation(ConcurrentNavigableMap.class, ConcurrentSkipListMap.class);
		setImplementation(CountDownLatch.class, Dummies.DummyCountDownLatch.class);
		setImplementation(Deque.class, ArrayDeque.class);
		setImplementation(OutputStream.class, ByteArrayOutputStream.class);
		setImplementation(PrintStream.class, Dummies.InMemoryPrintStream.class);
		setImplementation(PrintWriter.class, Dummies.InMemoryPrintWriter.class);
		setImplementation(Queue.class, ArrayDeque.class);
		setImplementation(ScheduledThreadPoolExecutor.class, Dummies.DummyScheduledThreadPoolExecutor.class);
		setImplementation(ThreadPoolExecutor.class, Dummies.DummyScheduledThreadPoolExecutor.class);
		setImplementation(Writer.class, StringWriter.class);
	}

	@SuppressWarnings("unchecked") // it's a subtype map
	@NullableDecl
	private static <T> Class<? extends T> getImplementation(Class<T> type) {
		return (Class<? extends T>) implementations.get(type);
	}

	private static final Logger logger = Logger.getLogger(ArbitraryInstances.class.getName());

	/**
	 * Returns an arbitrary instance for {@code type}, or {@code null} if no
	 * arbitrary instance can be determined.
	 */
	@NullableDecl
	public static <T> T get(Class<T> type) {
		T defaultValue = DEFAULTS.getInstance(type);
		if (defaultValue != null) {
			return defaultValue;
		}
		Class<? extends T> implementation = getImplementation(type);
		if (implementation != null) {
			return get(implementation);
		}
		if (type == Stream.class) {
			return type.cast(Stream.empty());
		}
		if (type.isEnum()) {
			T[] enumConstants = type.getEnumConstants();
			return (enumConstants.length == 0) ? null : enumConstants[0];
		}
		if (type.isArray()) {
			return createEmptyArray(type);
		}
		if (Modifier.isAbstract(type.getModifiers()) || !Modifier.isPublic(type.getModifiers())) {
			return arbitraryConstantInstanceOrNull(type);
		}
		final Constructor<T> constructor;
		try {
			constructor = type.getConstructor();
		} catch (NoSuchMethodException e) {
			return arbitraryConstantInstanceOrNull(type);
		}
		constructor.setAccessible(true); // accessibility check is too slow
		try {
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException impossible) {
			throw new AssertionError(impossible);
		} catch (InvocationTargetException e) {
			logger.log(Level.WARNING, "Exception while invoking default constructor.", e.getCause());
			return arbitraryConstantInstanceOrNull(type);
		}
	}

	@NullableDecl
	private static <T> T arbitraryConstantInstanceOrNull(Class<T> type) {
		Field[] fields = type.getDeclaredFields();
		Arrays.sort(fields, BY_FIELD_NAME);
		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())) {
				if (field.getGenericType() == field.getType() && type.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					try {
						T constant = type.cast(field.get(null));
						if (constant != null) {
							return constant;
						}
					} catch (IllegalAccessException impossible) {
						throw new AssertionError(impossible);
					}
				}
			}
		}
		return null;
	}

	private static <T> T createEmptyArray(Class<T> arrayType) {
		return arrayType.cast(Array.newInstance(arrayType.getComponentType(), 0));
	}

	// Internal implementations of some classes, with public default constructor
	// that get() needs.
	private static final class Dummies {

		public static final class InMemoryPrintStream extends PrintStream {
			public InMemoryPrintStream() {
				super(new ByteArrayOutputStream());
			}
		}

		public static final class InMemoryPrintWriter extends PrintWriter {
			public InMemoryPrintWriter() {
				super(new StringWriter());
			}
		}

		public static final class DummyScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
			public DummyScheduledThreadPoolExecutor() {
				super(1);
			}
		}

		public static final class DummyCountDownLatch extends CountDownLatch {
			public DummyCountDownLatch() {
				super(0);
			}
		}
	}
}
