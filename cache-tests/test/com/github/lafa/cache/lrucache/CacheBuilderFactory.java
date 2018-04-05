/*
 * Copyright (C) 2011 The Guava Authors
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

package com.github.lafa.cache.lrucache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.github.lafa.cache.base.Function;
import com.github.lafa.cache.base.MoreObjects;
import com.github.lafa.cache.base.Objects;
import com.github.lafa.cache.base.Preconditions;
import com.github.lafa.cache.lrucache.LocalCache.Strength;

/**
 * Helper class for creating {@link CacheBuilder} instances with all
 * combinations of several sets of parameters.
 *
 * @author mike nonemacher
 */
class CacheBuilderFactory {
	// Default values contain only 'null', which means don't call the CacheBuilder
	// method (just give
	// the CacheBuilder default).
	private Set<Integer> concurrencyLevels = new HashSet<>();
	private Set<Integer> initialCapacities = new HashSet<>();
	private Set<Integer> maximumSizes = new HashSet<>();
	private Set<DurationSpec> expireAfterWrites = new HashSet<>();
	private Set<DurationSpec> expireAfterAccesses = new HashSet<>();
	private Set<DurationSpec> refreshes = new HashSet<>();
	private Set<Strength> keyStrengths = new HashSet<>();
	private Set<Strength> valueStrengths = new HashSet<>();

	CacheBuilderFactory withConcurrencyLevels(Set<Integer> concurrencyLevels) {
		this.concurrencyLevels = new LinkedHashSet<>(concurrencyLevels);
		return this;
	}

	CacheBuilderFactory withInitialCapacities(Set<Integer> initialCapacities) {
		this.initialCapacities = new LinkedHashSet<>(initialCapacities);
		return this;
	}

	CacheBuilderFactory withMaximumSizes(Set<Integer> maximumSizes) {
		this.maximumSizes = new LinkedHashSet<>(maximumSizes);
		return this;
	}

	CacheBuilderFactory withExpireAfterWrites(Set<DurationSpec> durations) {
		this.expireAfterWrites = new LinkedHashSet<>(durations);
		return this;
	}

	CacheBuilderFactory withExpireAfterAccesses(Set<DurationSpec> durations) {
		this.expireAfterAccesses = new LinkedHashSet<>(durations);
		return this;
	}

	CacheBuilderFactory withRefreshes(Set<DurationSpec> durations) {
		this.refreshes = new LinkedHashSet<>(durations);
		return this;
	}

	CacheBuilderFactory withKeyStrengths(Set<Strength> keyStrengths) {
		this.keyStrengths = new LinkedHashSet<>(keyStrengths);
		Preconditions.checkArgument(!this.keyStrengths.contains(Strength.SOFT));
		return this;
	}

	CacheBuilderFactory withValueStrengths(Set<Strength> valueStrengths) {
		this.valueStrengths = new LinkedHashSet<>(valueStrengths);
		return this;
	}

	Iterable<CacheBuilder<Object, Object>> buildAllPermutations() {
		final Set<?> sets = new HashSet<>(Arrays.asList(initialCapacities, maximumSizes, expireAfterWrites,
				expireAfterAccesses, refreshes, keyStrengths, valueStrengths));
		List<List<Object>> combinations = _cartesianProduct(0, sets);

		List<CacheBuilder<Object, Object>> caches = new ArrayList<>();
		for (final List<Object> combination : combinations) {
			caches.add(createCacheBuilder((Integer) combination.get(0), (Integer) combination.get(1),
					(Integer) combination.get(2), (DurationSpec) combination.get(3), (DurationSpec) combination.get(4),
					(DurationSpec) combination.get(5), (Strength) combination.get(6), (Strength) combination.get(7)));
		}
		return caches;
	}

	private static final Function<Object, Optional<?>> NULLABLE_TO_OPTIONAL = new Function<Object, Optional<?>>() {
		@Override
		public Optional<?> apply(@NullableDecl Object obj) {
			return Optional.of(obj);
		}
	};

	private static final Function<Optional<?>, Object> OPTIONAL_TO_NULLABLE = new Function<Optional<?>, Object>() {
		@Override
		public Object apply(Optional<?> optional) {
			return optional.orElse(null);
		}
	};

	private CacheBuilder<Object, Object> createCacheBuilder(Integer concurrencyLevel, Integer initialCapacity,
			Integer maximumSize, DurationSpec expireAfterWrite, DurationSpec expireAfterAccess, DurationSpec refresh,
			Strength keyStrength, Strength valueStrength) {

		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		if (concurrencyLevel != null) {
			builder.concurrencyLevel(concurrencyLevel);
		}
		if (initialCapacity != null) {
			builder.initialCapacity(initialCapacity);
		}
		if (maximumSize != null) {
			builder.maximumSize(maximumSize);
		}
		if (expireAfterWrite != null) {
			builder.expireAfterWrite(expireAfterWrite.duration, expireAfterWrite.unit);
		}
		if (expireAfterAccess != null) {
			builder.expireAfterAccess(expireAfterAccess.duration, expireAfterAccess.unit);
		}
		if (refresh != null) {
			builder.refreshAfterWrite(refresh.duration, refresh.unit);
		}
		if (keyStrength != null) {
			builder.setKeyStrength(keyStrength);
		}
		if (valueStrength != null) {
			builder.setValueStrength(valueStrength);
		}
		return builder;
	}

	static class DurationSpec {
		private final long duration;
		private final TimeUnit unit;

		private DurationSpec(long duration, TimeUnit unit) {
			this.duration = duration;
			this.unit = unit;
		}

		public static DurationSpec of(long duration, TimeUnit unit) {
			return new DurationSpec(duration, unit);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(duration, unit);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof DurationSpec) {
				DurationSpec that = (DurationSpec) o;
				return unit.toNanos(duration) == that.unit.toNanos(that.duration);
			}
			return false;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("duration", duration).add("unit", unit).toString();
		}
	}

	public static List<List<Object>> cartesianProduct(Set<?>... sets) {
		if (sets.length < 2)
			throw new IllegalArgumentException("Can't have a product of fewer than two sets (got " + sets.length + ")");

		return _cartesianProduct(0, sets);
	}

	private static List<List<Object>> _cartesianProduct(int index, Set<?>... sets) {
		List<List<Object>> ret = new ArrayList<>();
		if (index == sets.length) {
			ret.add(new ArrayList<Object>());
		} else {
			for (Object obj : sets[index]) {
				for (List<Object> set : _cartesianProduct(index + 1, sets)) {
					set.add(obj);
					ret.add(set);
				}
			}
		}
		return ret;
	}
}
