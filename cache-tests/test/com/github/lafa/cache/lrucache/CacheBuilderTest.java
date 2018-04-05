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
package com.github.lafa.cache.lrucache;

import static com.github.lafa.cache.lrucache.TestingRemovalListeners.nullRemovalListener;
import static com.github.lafa.cache.lrucache.TestingWeighers.constantWeigher;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Set;

import com.github.lafa.cache.base.Ticker;
import com.github.lafa.cache.common.testing.NullPointerTester;

import junit.framework.TestCase;

/** Unit tests for CacheBuilder. */
public class CacheBuilderTest extends TestCase {

	public void testInitialCapacity_negative() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.initialCapacity(-1);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testInitialCapacity_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().initialCapacity(16);
		try {
			// even to the same value is not allowed
			builder.initialCapacity(16);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testInitialCapacity_large() {
		CacheBuilder.newBuilder().initialCapacity(Integer.MAX_VALUE);
		// that the builder didn't blow up is enough;
		// don't actually create this monster!
	}

	public void testConcurrencyLevel_zero() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.concurrencyLevel(0);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testConcurrencyLevel_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().concurrencyLevel(16);
		try {
			// even to the same value is not allowed
			builder.concurrencyLevel(16);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testConcurrencyLevel_large() {
		CacheBuilder.newBuilder().concurrencyLevel(Integer.MAX_VALUE);
		// don't actually build this beast
	}

	public void testMaximumSize_negative() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.maximumSize(-1);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testMaximumSize_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumSize(16);
		try {
			// even to the same value is not allowed
			builder.maximumSize(16);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	// maximumWeight
	public void testMaximumSize_andWeight() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumSize(16);
		try {
			builder.maximumWeight(16);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	// maximumWeight
	public void testMaximumWeight_negative() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.maximumWeight(-1);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	// maximumWeight
	public void testMaximumWeight_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumWeight(16);
		try {
			// even to the same value is not allowed
			builder.maximumWeight(16);
			fail();
		} catch (IllegalStateException expected) {
		}
		try {
			builder.maximumSize(16);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	// weigher
	public void testWeigher_withMaximumSize() {
		try {
			CacheBuilder.newBuilder().weigher(constantWeigher(42)).maximumSize(1);
			fail();
		} catch (IllegalStateException expected) {
		}
		try {
			CacheBuilder.newBuilder().maximumSize(1).weigher(constantWeigher(42));
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	// weakKeys
	public void testKeyStrengthSetTwice() {
		CacheBuilder<Object, Object> builder1 = CacheBuilder.newBuilder().weakKeys();
		try {
			builder1.weakKeys();
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	// weakValues
	public void testValueStrengthSetTwice() {
		CacheBuilder<Object, Object> builder1 = CacheBuilder.newBuilder().weakValues();
		try {
			builder1.weakValues();
			fail();
		} catch (IllegalStateException expected) {
		}
		try {
			builder1.softValues();
			fail();
		} catch (IllegalStateException expected) {
		}

		CacheBuilder<Object, Object> builder2 = CacheBuilder.newBuilder().softValues();
		try {
			builder2.softValues();
			fail();
		} catch (IllegalStateException expected) {
		}
		try {
			builder2.weakValues();
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testTimeToLive_negative() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.expireAfterWrite(-1, SECONDS);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testTimeToLive_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().expireAfterWrite(3600, SECONDS);
		try {
			// even to the same value is not allowed
			builder.expireAfterWrite(3600, SECONDS);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testTimeToIdle_negative() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.expireAfterAccess(-1, SECONDS);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testTimeToIdle_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().expireAfterAccess(3600, SECONDS);
		try {
			// even to the same value is not allowed
			builder.expireAfterAccess(3600, SECONDS);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	// refreshAfterWrite
	public void testRefresh_zero() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		try {
			builder.refreshAfterWrite(0, SECONDS);
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	// refreshAfterWrite
	public void testRefresh_setTwice() {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().refreshAfterWrite(3600, SECONDS);
		try {
			// even to the same value is not allowed
			builder.refreshAfterWrite(3600, SECONDS);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testTicker_setTwice() {
		Ticker testTicker = Ticker.systemTicker();
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().ticker(testTicker);
		try {
			// even to the same instance is not allowed
			builder.ticker(testTicker);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testRemovalListener_setTwice() {
		RemovalListener<Object, Object> testListener = nullRemovalListener();
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().removalListener(testListener);
		try {
			// even to the same instance is not allowed
			builder = builder.removalListener(testListener);
			fail();
		} catch (IllegalStateException expected) {
		}
	}

	public void testValuesIsNotASet() {
		assertFalse(CacheBuilder.newBuilder().build().asMap().values() instanceof Set);
	}

	// "Basher tests", where we throw a bunch of stuff at a LoadingCache and check
	// basic invariants.

	// NullPointerTester
	public void testNullParameters() throws Exception {
		NullPointerTester tester = new NullPointerTester();
		tester.setDefault(Long.class, 1L);
		tester.setDefault(long.class, 1L);
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		tester.testAllPublicInstanceMethods(builder);
	}
}
