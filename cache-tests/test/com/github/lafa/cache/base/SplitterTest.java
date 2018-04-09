/*
 * Copyright (C) 2009 The Guava Authors
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

package com.github.lafa.cache.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;

import com.github.lafa.cache.common.testing.NullPointerTester;

import junit.framework.TestCase;

/** @author Julien Silland */
public class SplitterTest extends TestCase {

	private static final Splitter COMMA_SPLITTER = Splitter.on(',');

	public void testSplitNullString() {
		try {
			COMMA_SPLITTER.split(null);
			fail();
		} catch (NullPointerException expected) {
		}
	}

	public void testCharacterSimpleSplit() {
		String simple = "a,b,c";
		Iterable<String> letters = COMMA_SPLITTER.split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c"));
	}

	/**
	 * All of the infrastructure of split and splitToString is identical, so we do
	 * one test of splitToString. All other cases should be covered by testing of
	 * split.
	 *
	 * <p>
	 * TODO(user): It would be good to make all the relevant tests run on both split
	 * and splitToString automatically.
	 */
	public void testCharacterSimpleSplitToList() {
		String simple = "a,b,c";
		List<String> letters = COMMA_SPLITTER.splitToList(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c"));
	}

	public void testToString() {
		assertEquals("[]", COMMA_SPLITTER.split("").toString());
		assertEquals("[a, b, c]", COMMA_SPLITTER.split("a,b,c").toString());
		assertEquals("[yam, bam, jam, ham]", Splitter.on(", ").split("yam, bam, jam, ham").toString());
	}

	public void testCharacterSimpleSplitWithNoDelimiter() {
		String simple = "a,b,c";
		Iterable<String> letters = Splitter.on('.').split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a,b,c"));
	}

	public void testCharacterSplitWithDoubleDelimiter() {
		String doubled = "a,,b,c";
		Iterable<String> letters = COMMA_SPLITTER.split(doubled);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "", "b", "c"));
	}

	public void testCharacterSplitWithDoubleDelimiterAndSpace() {
		String doubled = "a,, b,c";
		Iterable<String> letters = COMMA_SPLITTER.split(doubled);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "", " b", "c"));
	}

	public void testCharacterSplitWithTrailingDelimiter() {
		String trailing = "a,b,c,";
		Iterable<String> letters = COMMA_SPLITTER.split(trailing);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c", ""));
	}

	public void testCharacterSplitWithLeadingDelimiter() {
		String leading = ",a,b,c";
		Iterable<String> letters = COMMA_SPLITTER.split(leading);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("", "a", "b", "c"));
	}

	public void testCharacterSplitWithMultipleLetters() {
		Iterable<String> testCharacteringMotto = Splitter.on('-').split("Testing-rocks-Debugging-sucks");
		Assert.assertEquals(SplitterTest.toList(testCharacteringMotto),
				Arrays.asList("Testing", "rocks", "Debugging", "sucks"));
	}

	public void testCharacterSplitWithMatcherDelimiter() {
		Iterable<String> testCharacteringMotto = Splitter.on(CharMatcher.whitespace())
				.split("Testing\nrocks\tDebugging sucks");
		Assert.assertEquals(SplitterTest.toList(testCharacteringMotto),
				Arrays.asList("Testing", "rocks", "Debugging", "sucks"));
	}

	public void testCharacterSplitWithDoubleDelimiterOmitEmptyStrings() {
		String doubled = "a..b.c";
		Iterable<String> letters = Splitter.on('.').omitEmptyStrings().split(doubled);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c"));
	}

	public void testCharacterSplitEmptyToken() {
		String emptyToken = "a. .c";
		Iterable<String> letters = Splitter.on('.').trimResults().split(emptyToken);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "", "c"));
	}

	public void testCharacterSplitEmptyTokenOmitEmptyStrings() {
		String emptyToken = "a. .c";
		Iterable<String> letters = Splitter.on('.').omitEmptyStrings().trimResults().split(emptyToken);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "c"));
	}

	public void testCharacterSplitOnEmptyString() {
		Iterable<String> nothing = Splitter.on('.').split("");
		Assert.assertEquals(SplitterTest.toList(nothing), Arrays.asList(""));
	}

	public void testCharacterSplitOnEmptyStringOmitEmptyStrings() {
		Assert.assertEquals(SplitterTest.toList(Splitter.on('.').omitEmptyStrings().split("")), Arrays.asList());
	}

	public void testCharacterSplitOnOnlyDelimiter() {
		Iterable<String> blankblank = Splitter.on('.').split(".");
		List<String> list = SplitterTest.toList(blankblank);
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0), new String());
		Assert.assertEquals(list.get(1), "");
	}

	public void testCharacterSplitOnOnlyDelimitersOmitEmptyStrings() {
		Iterable<String> empty = Splitter.on('.').omitEmptyStrings().split("...");
		Assert.assertEquals(SplitterTest.toList(empty).size(), 0);
	}

	public void testCharacterSplitWithTrim() {
		String jacksons = "arfo(Marlon)aorf, (Michael)orfa, afro(Jackie)orfa, " + "ofar(Jemaine), aff(Tito)";
		Iterable<String> family = COMMA_SPLITTER.trimResults(CharMatcher.anyOf("afro").or(CharMatcher.whitespace()))
				.split(jacksons);
		Assert.assertEquals(SplitterTest.toList(family),
				Arrays.asList("(Marlon)", "(Michael)", "(Jackie)", "(Jemaine)", "(Tito)"));
	}

	public void testStringSimpleSplit() {
		String simple = "a,b,c";
		Iterable<String> letters = Splitter.on(",").split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c"));
	}

	public void testStringSimpleSplitWithNoDelimiter() {
		String simple = "a,b,c";
		Iterable<String> letters = Splitter.on(".").split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a,b,c"));
	}

	public void testStringSplitWithDoubleDelimiter() {
		String doubled = "a,,b,c";
		Iterable<String> letters = Splitter.on(",").split(doubled);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "", "b", "c"));
	}

	public void testStringSplitWithDoubleDelimiterAndSpace() {
		String doubled = "a,, b,c";
		Iterable<String> letters = Splitter.on(",").split(doubled);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "", " b", "c"));
	}

	public void testStringSplitWithTrailingDelimiter() {
		String trailing = "a,b,c,";
		Iterable<String> letters = Splitter.on(",").split(trailing);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c", ""));
	}

	public void testStringSplitWithLeadingDelimiter() {
		String leading = ",a,b,c";
		Iterable<String> letters = Splitter.on(",").split(leading);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("", "a", "b", "c"));
	}

	public void testStringSplitWithMultipleLetters() {
		Iterable<String> testStringingMotto = Splitter.on("-").split("Testing-rocks-Debugging-sucks");
		Assert.assertEquals(SplitterTest.toList(testStringingMotto),
				Arrays.asList("Testing", "rocks", "Debugging", "sucks"));
	}

	public void testStringSplitWithDoubleDelimiterOmitEmptyStrings() {
		String doubled = "a..b.c";
		Iterable<String> letters = Splitter.on(".").omitEmptyStrings().split(doubled);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c"));
	}

	public void testStringSplitEmptyToken() {
		String emptyToken = "a. .c";
		Iterable<String> letters = Splitter.on(".").trimResults().split(emptyToken);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "", "c"));
	}

	public void testStringSplitEmptyTokenOmitEmptyStrings() {
		String emptyToken = "a. .c";
		Iterable<String> letters = Splitter.on(".").omitEmptyStrings().trimResults().split(emptyToken);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "c"));
	}

	public void testStringSplitWithLongDelimiter() {
		String longDelimiter = "a, b, c";
		Iterable<String> letters = Splitter.on(", ").split(longDelimiter);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c"));
	}

	public void testStringSplitWithLongLeadingDelimiter() {
		String longDelimiter = ", a, b, c";
		Iterable<String> letters = Splitter.on(", ").split(longDelimiter);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("", "a", "b", "c"));
	}

	public void testStringSplitWithLongTrailingDelimiter() {
		String longDelimiter = "a, b, c, ";
		Iterable<String> letters = Splitter.on(", ").split(longDelimiter);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c", ""));
	}

	public void testStringSplitWithDelimiterSubstringInValue() {
		String fourCommasAndFourSpaces = ",,,,    ";
		Iterable<String> threeCommasThenThreeSpaces = Splitter.on(", ").split(fourCommasAndFourSpaces);
		Assert.assertEquals(SplitterTest.toList(threeCommasThenThreeSpaces), Arrays.asList(",,,", "   "));
	}

	public void testStringSplitWithEmptyString() {
		try {
			Splitter.on("");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testStringSplitOnEmptyString() {
		Iterable<String> notMuch = Splitter.on(".").split("");
		Assert.assertEquals(SplitterTest.toList(notMuch), Arrays.asList(""));
	}

	public void testStringSplitOnEmptyStringOmitEmptyString() {
		Assert.assertEquals(SplitterTest.toList(Splitter.on(".").omitEmptyStrings().split("")), Arrays.asList());
	}

	public void testStringSplitOnOnlyDelimiter() {
		Iterable<String> blankblank = Splitter.on(".").split(".");
		Assert.assertEquals(SplitterTest.toList(blankblank), Arrays.asList("", ""));
	}

	public void testStringSplitOnOnlyDelimitersOmitEmptyStrings() {
		Iterable<String> empty = Splitter.on(".").omitEmptyStrings().split("...");
		Assert.assertEquals(SplitterTest.toList(empty), Arrays.asList());
	}

	public void testStringSplitWithTrim() {
		String jacksons = "arfo(Marlon)aorf, (Michael)orfa, afro(Jackie)orfa, " + "ofar(Jemaine), aff(Tito)";
		Iterable<String> family = Splitter.on(",").trimResults(CharMatcher.anyOf("afro").or(CharMatcher.whitespace()))
				.split(jacksons);
		Assert.assertEquals(SplitterTest.toList(family),
				Arrays.asList("(Marlon)", "(Michael)", "(Jackie)", "(Jemaine)", "(Tito)"));
	}

	public void testSplitterIterableIsUnmodifiable_char() {
		assertIteratorIsUnmodifiable(COMMA_SPLITTER.split("a,b").iterator());
	}

	public void testSplitterIterableIsUnmodifiable_string() {
		assertIteratorIsUnmodifiable(Splitter.on(",").split("a,b").iterator());
	}

	private void assertIteratorIsUnmodifiable(Iterator<?> iterator) {
		iterator.next();
		try {
			iterator.remove();
			fail();
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testSplitterIterableIsLazy_char() {
		assertSplitterIterableIsLazy(COMMA_SPLITTER);
	}

	public void testSplitterIterableIsLazy_string() {
		assertSplitterIterableIsLazy(Splitter.on(","));
	}

	/**
	 * This test really pushes the boundaries of what we support. In general the
	 * splitter's behaviour is not well defined if the char sequence it's splitting
	 * is mutated during iteration.
	 */
	private void assertSplitterIterableIsLazy(Splitter splitter) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = splitter.split(builder).iterator();

		builder.append("A,");
		assertEquals("A", iterator.next());
		builder.append("B,");
		assertEquals("B", iterator.next());
		builder.append("C");
		assertEquals("C", iterator.next());
		assertFalse(iterator.hasNext());
	}

	public void testFixedLengthSimpleSplit() {
		String simple = "abcde";
		Iterable<String> letters = Splitter.fixedLength(2).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("ab", "cd", "e"));
	}

	public void testFixedLengthSplitEqualChunkLength() {
		String simple = "abcdef";
		Iterable<String> letters = Splitter.fixedLength(2).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("ab", "cd", "ef"));
	}

	public void testFixedLengthSplitOnlyOneChunk() {
		String simple = "abc";
		Iterable<String> letters = Splitter.fixedLength(3).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("abc"));
	}

	public void testFixedLengthSplitSmallerString() {
		String simple = "ab";
		Iterable<String> letters = Splitter.fixedLength(3).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("ab"));
	}

	public void testFixedLengthSplitEmptyString() {
		String simple = "";
		Iterable<String> letters = Splitter.fixedLength(3).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList(""));
	}

	public void testFixedLengthSplitEmptyStringWithOmitEmptyStrings() {
		Assert.assertEquals(SplitterTest.toList(Splitter.fixedLength(3).omitEmptyStrings().split("")),
				new ArrayList<String>());
	}

	public void testFixedLengthSplitIntoChars() {
		String simple = "abcd";
		Iterable<String> letters = Splitter.fixedLength(1).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c", "d"));
	}

	public void testFixedLengthSplitZeroChunkLen() {
		try {
			Splitter.fixedLength(0);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testFixedLengthSplitNegativeChunkLen() {
		try {
			Splitter.fixedLength(-1);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testLimitLarge() {
		String simple = "abcd";
		Iterable<String> letters = Splitter.fixedLength(1).limit(100).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "b", "c", "d"));
	}

	public void testLimitOne() {
		String simple = "abcd";
		Iterable<String> letters = Splitter.fixedLength(1).limit(1).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("abcd"));
	}

	public void testLimitFixedLength() {
		String simple = "abcd";
		Iterable<String> letters = Splitter.fixedLength(1).limit(2).split(simple);
		Assert.assertEquals(SplitterTest.toList(letters), Arrays.asList("a", "bcd"));
	}

	public void testLimitSeparator() {
		String simple = "a,b,c,d";
		Iterable<String> items = COMMA_SPLITTER.limit(2).split(simple);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a", "b,c,d"));
	}

	public void testLimitExtraSeparators() {
		String text = "a,,,b,,c,d";
		Iterable<String> items = COMMA_SPLITTER.limit(2).split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a", ",,b,,c,d"));
	}

	public void testLimitExtraSeparatorsOmitEmpty() {
		String text = "a,,,b,,c,d";
		Iterable<String> items = COMMA_SPLITTER.limit(2).omitEmptyStrings().split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a", "b,,c,d"));
	}

	public void testLimitExtraSeparatorsOmitEmpty3() {
		String text = "a,,,b,,c,d";
		Iterable<String> items = COMMA_SPLITTER.limit(3).omitEmptyStrings().split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a", "b", "c,d"));
	}

	public void testLimitExtraSeparatorsTrim() {
		String text = ",,a,,  , b ,, c,d ";
		Iterable<String> items = COMMA_SPLITTER.limit(2).omitEmptyStrings().trimResults().split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a", "b ,, c,d"));
	}

	public void testLimitExtraSeparatorsTrim3() {
		String text = ",,a,,  , b ,, c,d ";
		Iterable<String> items = COMMA_SPLITTER.limit(3).omitEmptyStrings().trimResults().split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a", "b", "c,d"));
	}

	public void testLimitExtraSeparatorsTrim1() {
		String text = ",,a,,  , b ,, c,d ";
		Iterable<String> items = COMMA_SPLITTER.limit(1).omitEmptyStrings().trimResults().split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList("a,,  , b ,, c,d"));
	}

	public void testLimitExtraSeparatorsTrim1NoOmit() {
		String text = ",,a,,  , b ,, c,d ";
		Iterable<String> items = COMMA_SPLITTER.limit(1).trimResults().split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList(",,a,,  , b ,, c,d"));
	}

	public void testLimitExtraSeparatorsTrim1Empty() {
		String text = "";
		Iterable<String> items = COMMA_SPLITTER.limit(1).split(text);
		Assert.assertEquals(SplitterTest.toList(items), Arrays.asList(""));
	}

	public void testLimitExtraSeparatorsTrim1EmptyOmit() {
		String text = "";
		Iterable<String> items = COMMA_SPLITTER.omitEmptyStrings().limit(1).split(text);
		Assert.assertEquals(SplitterTest.toList(items).size(), 0);
	}

	public void testInvalidZeroLimit() {
		try {
			COMMA_SPLITTER.limit(0);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	// NullPointerTester
	public void testNullPointers() {
		NullPointerTester tester = new NullPointerTester();
		tester.testAllPublicStaticMethods(Splitter.class);
		tester.testAllPublicInstanceMethods(COMMA_SPLITTER);
		tester.testAllPublicInstanceMethods(COMMA_SPLITTER.trimResults());
	}

	public void testMapSplitter_trimmedBoth() {
		Map<String, String> m = COMMA_SPLITTER.trimResults().withKeyValueSeparator(Splitter.on(':').trimResults())
				.split("boy  : tom , girl: tina , cat  : kitty , dog: tommy ");
		final Map<String, String> expected = new HashMap<String, String>();
		expected.put("boy", "tom");
		expected.put("girl", "tina");
		expected.put("cat", "kitty");
		expected.put("dog", "tommy");
		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));
	}

	public void testMapSplitter_trimmedEntries() {
		Map<String, String> m = COMMA_SPLITTER.trimResults().withKeyValueSeparator(":")
				.split("boy  : tom , girl: tina , cat  : kitty , dog: tommy ");
		final Map<String, String> expected = new HashMap<String, String>();
		expected.put("boy  ", " tom");
		expected.put("girl", " tina");
		expected.put("cat  ", " kitty");
		expected.put("dog", " tommy");

		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));
	}

	public void testMapSplitter_trimmedKeyValue() {
		Map<String, String> m = COMMA_SPLITTER.withKeyValueSeparator(Splitter.on(':').trimResults())
				.split("boy  : tom , girl: tina , cat  : kitty , dog: tommy ");
		final Map<String, String> expected = new HashMap<String, String>();
		expected.put("boy", "tom");
		expected.put("girl", "tina");
		expected.put("cat", "kitty");
		expected.put("dog", "tommy");
		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));
	}

	public void testMapSplitter_notTrimmed() {
		Map<String, String> m = COMMA_SPLITTER.withKeyValueSeparator(":")
				.split(" boy:tom , girl: tina , cat :kitty , dog:  tommy ");
		assertTrue(m.containsKey(" boy"));
		assertTrue(m.containsKey(" girl"));
		assertTrue(m.containsKey(" cat "));
		assertTrue(m.containsKey(" dog"));
		
		assertEquals(m.get(" boy"), "tom ");
		assertEquals(m.get(" girl"), " tina ");
		assertEquals(m.get(" cat "), "kitty ");
		assertEquals(m.get(" dog"), "  tommy ");
	}

	public void testMapSplitter_CharacterSeparator() {
		// try different delimiters.
		Map<String, String> m = Splitter.on(",").withKeyValueSeparator(':')
				.split("boy:tom,girl:tina,cat:kitty,dog:tommy");
		final Map<String, String> expected = new HashMap<String, String>();
		expected.put("boy", "tom");
		expected.put("girl", "tina");
		expected.put("cat", "kitty");
		expected.put("dog", "tommy");

		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));
	}

	public void testMapSplitter_multiCharacterSeparator() {
		// try different delimiters.
		Map<String, String> m = Splitter.on(",").withKeyValueSeparator(":^&")
				.split("boy:^&tom,girl:^&tina,cat:^&kitty,dog:^&tommy");
		final Map<String, String> expected = new HashMap<String, String>();
		expected.put("boy", "tom");
		expected.put("girl", "tina");
		expected.put("cat", "kitty");
		expected.put("dog", "tommy");

		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));
	}

	public void testMapSplitter_emptySeparator() {
		try {
			COMMA_SPLITTER.withKeyValueSeparator("");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testMapSplitter_malformedEntry() {
		try {
			COMMA_SPLITTER.withKeyValueSeparator("=").split("a=1,b,c=2");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testMapSplitter_orderedResults() {
		Map<String, String> m = COMMA_SPLITTER.withKeyValueSeparator(":")
				.split("boy:tom,girl:tina,cat:kitty,dog:tommy");

		final Map<String, String> expected = new HashMap<String, String>();
		expected.put("boy", "tom");
		expected.put("girl", "tina");
		expected.put("cat", "kitty");
		expected.put("dog", "tommy");
		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));

		// try in a different order
		m = COMMA_SPLITTER.withKeyValueSeparator(":").split("girl:tina,boy:tom,dog:tommy,cat:kitty");

		Assert.assertTrue(m.equals(expected));
		Assert.assertTrue(m.entrySet().equals(expected.entrySet()));
	}

	public void testMapSplitter_duplicateKeys() {
		try {
			COMMA_SPLITTER.withKeyValueSeparator(":").split("a:1,b:2,a:3");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public static <T> List<T> toList(final Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
	}
}
