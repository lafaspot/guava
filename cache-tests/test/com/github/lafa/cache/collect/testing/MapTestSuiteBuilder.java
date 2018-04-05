/*
 * Copyright (C) 2008 The Guava Authors
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

package com.github.lafa.cache.collect.testing;

import static com.github.lafa.cache.collect.testing.DerivedCollectionGenerators.keySetGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.lafa.cache.collect.testing.DerivedCollectionGenerators.MapEntrySetGenerator;
import com.github.lafa.cache.collect.testing.DerivedCollectionGenerators.MapValueCollectionGenerator;
import com.github.lafa.cache.collect.testing.features.CollectionFeature;
import com.github.lafa.cache.collect.testing.features.CollectionSize;
import com.github.lafa.cache.collect.testing.features.Feature;
import com.github.lafa.cache.collect.testing.features.MapFeature;
import com.github.lafa.cache.collect.testing.testers.MapClearTester;
import com.github.lafa.cache.collect.testing.testers.MapComputeIfAbsentTester;
import com.github.lafa.cache.collect.testing.testers.MapComputeIfPresentTester;
import com.github.lafa.cache.collect.testing.testers.MapComputeTester;
import com.github.lafa.cache.collect.testing.testers.MapContainsKeyTester;
import com.github.lafa.cache.collect.testing.testers.MapContainsValueTester;
import com.github.lafa.cache.collect.testing.testers.MapCreationTester;
import com.github.lafa.cache.collect.testing.testers.MapEntrySetTester;
import com.github.lafa.cache.collect.testing.testers.MapEqualsTester;
import com.github.lafa.cache.collect.testing.testers.MapForEachTester;
import com.github.lafa.cache.collect.testing.testers.MapGetOrDefaultTester;
import com.github.lafa.cache.collect.testing.testers.MapGetTester;
import com.github.lafa.cache.collect.testing.testers.MapHashCodeTester;
import com.github.lafa.cache.collect.testing.testers.MapIsEmptyTester;
import com.github.lafa.cache.collect.testing.testers.MapPutAllTester;
import com.github.lafa.cache.collect.testing.testers.MapPutIfAbsentTester;
import com.github.lafa.cache.collect.testing.testers.MapPutTester;
import com.github.lafa.cache.collect.testing.testers.MapRemoveEntryTester;
import com.github.lafa.cache.collect.testing.testers.MapRemoveTester;
import com.github.lafa.cache.collect.testing.testers.MapReplaceAllTester;
import com.github.lafa.cache.collect.testing.testers.MapReplaceEntryTester;
import com.github.lafa.cache.collect.testing.testers.MapReplaceTester;
import com.github.lafa.cache.collect.testing.testers.MapSerializationTester;
import com.github.lafa.cache.collect.testing.testers.MapSizeTester;
import com.github.lafa.cache.collect.testing.testers.MapToStringTester;
import com.github.lafa.cache.common.testing.SerializableTester;

import junit.framework.TestSuite;

/**
 * Creates, based on your criteria, a JUnit test suite that exhaustively tests a
 * Map implementation.
 *
 * @author George van den Driessche
 */
public class MapTestSuiteBuilder<K, V> extends
		PerCollectionSizeTestSuiteBuilder<MapTestSuiteBuilder<K, V>, TestMapGenerator<K, V>, Map<K, V>, Entry<K, V>> {
	public static <K, V> MapTestSuiteBuilder<K, V> using(TestMapGenerator<K, V> generator) {
		return new MapTestSuiteBuilder<K, V>().usingGenerator(generator);
	}

	@SuppressWarnings("unchecked") // Class parameters must be raw.
	@Override
	protected List<Class<? extends AbstractTester>> getTesters() {
		return Arrays.<Class<? extends AbstractTester>>asList(MapClearTester.class, MapComputeTester.class,
				MapComputeIfAbsentTester.class, MapComputeIfPresentTester.class, MapContainsKeyTester.class,
				MapContainsValueTester.class, MapCreationTester.class, MapEntrySetTester.class, MapEqualsTester.class,
				MapForEachTester.class, MapGetTester.class, MapGetOrDefaultTester.class, MapHashCodeTester.class,
				MapIsEmptyTester.class, MapPutTester.class, MapPutAllTester.class, MapPutIfAbsentTester.class,
				MapRemoveTester.class, MapRemoveEntryTester.class, MapReplaceTester.class, MapReplaceAllTester.class,
				MapReplaceEntryTester.class, MapSerializationTester.class, MapSizeTester.class,
				MapToStringTester.class);
	}

	@Override
	protected List<TestSuite> createDerivedSuites(
			FeatureSpecificTestSuiteBuilder<?, ? extends OneSizeTestContainerGenerator<Map<K, V>, Entry<K, V>>> parentBuilder) {
		// TODO: Once invariant support is added, supply invariants to each of the
		// derived suites, to check that mutations to the derived collections are
		// reflected in the underlying map.

		List<TestSuite> derivedSuites = super.createDerivedSuites(parentBuilder);

		if (parentBuilder.getFeatures().contains(CollectionFeature.SERIALIZABLE)) {
			derivedSuites.add(
					MapTestSuiteBuilder.using(new ReserializedMapGenerator<K, V>(parentBuilder.getSubjectGenerator()))
							.withFeatures(computeReserializedMapFeatures(parentBuilder.getFeatures()))
							.named(parentBuilder.getName() + " reserialized")
							.suppressing(parentBuilder.getSuppressedTests()).createTestSuite());
		}

		derivedSuites
				.add(createDerivedEntrySetSuite(new MapEntrySetGenerator<K, V>(parentBuilder.getSubjectGenerator()))
						.withFeatures(computeEntrySetFeatures(parentBuilder.getFeatures()))
						.named(parentBuilder.getName() + " entrySet").suppressing(parentBuilder.getSuppressedTests())
						.createTestSuite());

		derivedSuites.add(createDerivedKeySetSuite(keySetGenerator(parentBuilder.getSubjectGenerator()))
				.withFeatures(computeKeySetFeatures(parentBuilder.getFeatures()))
				.named(parentBuilder.getName() + " keys").suppressing(parentBuilder.getSuppressedTests())
				.createTestSuite());

		derivedSuites.add(createDerivedValueCollectionSuite(
				new MapValueCollectionGenerator<K, V>(parentBuilder.getSubjectGenerator()))
						.named(parentBuilder.getName() + " values")
						.withFeatures(computeValuesCollectionFeatures(parentBuilder.getFeatures()))
						.suppressing(parentBuilder.getSuppressedTests()).createTestSuite());

		return derivedSuites;
	}

	protected SetTestSuiteBuilder<Entry<K, V>> createDerivedEntrySetSuite(
			TestSetGenerator<Entry<K, V>> entrySetGenerator) {
		return SetTestSuiteBuilder.using(entrySetGenerator);
	}

	protected SetTestSuiteBuilder<K> createDerivedKeySetSuite(TestSetGenerator<K> keySetGenerator) {
		return SetTestSuiteBuilder.using(keySetGenerator);
	}

	protected CollectionTestSuiteBuilder<V> createDerivedValueCollectionSuite(
			TestCollectionGenerator<V> valueCollectionGenerator) {
		return CollectionTestSuiteBuilder.using(valueCollectionGenerator);
	}

	private static Set<Feature<?>> computeReserializedMapFeatures(Set<Feature<?>> mapFeatures) {
		Set<Feature<?>> derivedFeatures = Helpers.copyToSet(mapFeatures);
		derivedFeatures.remove(CollectionFeature.SERIALIZABLE);
		derivedFeatures.remove(CollectionFeature.SERIALIZABLE_INCLUDING_VIEWS);
		return derivedFeatures;
	}

	private static Set<Feature<?>> computeEntrySetFeatures(Set<Feature<?>> mapFeatures) {
		Set<Feature<?>> entrySetFeatures = computeCommonDerivedCollectionFeatures(mapFeatures);
		if (mapFeatures.contains(MapFeature.ALLOWS_NULL_ENTRY_QUERIES)) {
			entrySetFeatures.add(CollectionFeature.ALLOWS_NULL_QUERIES);
		}
		return entrySetFeatures;
	}

	private static Set<Feature<?>> computeKeySetFeatures(Set<Feature<?>> mapFeatures) {
		Set<Feature<?>> keySetFeatures = computeCommonDerivedCollectionFeatures(mapFeatures);

		// TODO(lowasser): make this trigger only if the map is a submap
		// currently, the KeySetGenerator won't work properly for a subset of a keyset
		// of a submap
		keySetFeatures.add(CollectionFeature.SUBSET_VIEW);
		if (mapFeatures.contains(MapFeature.ALLOWS_NULL_KEYS)) {
			keySetFeatures.add(CollectionFeature.ALLOWS_NULL_VALUES);
		} else if (mapFeatures.contains(MapFeature.ALLOWS_NULL_KEY_QUERIES)) {
			keySetFeatures.add(CollectionFeature.ALLOWS_NULL_QUERIES);
		}

		return keySetFeatures;
	}

	private static Set<Feature<?>> computeValuesCollectionFeatures(Set<Feature<?>> mapFeatures) {
		Set<Feature<?>> valuesCollectionFeatures = computeCommonDerivedCollectionFeatures(mapFeatures);
		if (mapFeatures.contains(MapFeature.ALLOWS_NULL_VALUE_QUERIES)) {
			valuesCollectionFeatures.add(CollectionFeature.ALLOWS_NULL_QUERIES);
		}
		if (mapFeatures.contains(MapFeature.ALLOWS_NULL_VALUES)) {
			valuesCollectionFeatures.add(CollectionFeature.ALLOWS_NULL_VALUES);
		}

		return valuesCollectionFeatures;
	}

	public static Set<Feature<?>> computeCommonDerivedCollectionFeatures(Set<Feature<?>> mapFeatures) {
		mapFeatures = new HashSet<>(mapFeatures);
		Set<Feature<?>> derivedFeatures = new HashSet<>();
		mapFeatures.remove(CollectionFeature.SERIALIZABLE);
		if (mapFeatures.remove(CollectionFeature.SERIALIZABLE_INCLUDING_VIEWS)) {
			derivedFeatures.add(CollectionFeature.SERIALIZABLE);
		}
		if (mapFeatures.contains(MapFeature.SUPPORTS_REMOVE)) {
			derivedFeatures.add(CollectionFeature.SUPPORTS_REMOVE);
		}
		if (mapFeatures.contains(MapFeature.REJECTS_DUPLICATES_AT_CREATION)) {
			derivedFeatures.add(CollectionFeature.REJECTS_DUPLICATES_AT_CREATION);
		}
		if (mapFeatures.contains(MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)) {
			derivedFeatures.add(CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION);
		}
		// add the intersection of CollectionFeature.values() and mapFeatures
		for (CollectionFeature feature : CollectionFeature.values()) {
			if (mapFeatures.contains(feature)) {
				derivedFeatures.add(feature);
			}
		}
		// add the intersection of CollectionSize.values() and mapFeatures
		for (CollectionSize size : CollectionSize.values()) {
			if (mapFeatures.contains(size)) {
				derivedFeatures.add(size);
			}
		}
		return derivedFeatures;
	}

	private static class ReserializedMapGenerator<K, V> implements TestMapGenerator<K, V> {
		private final OneSizeTestContainerGenerator<Map<K, V>, Entry<K, V>> mapGenerator;

		public ReserializedMapGenerator(OneSizeTestContainerGenerator<Map<K, V>, Entry<K, V>> mapGenerator) {
			this.mapGenerator = mapGenerator;
		}

		@Override
		public SampleElements<Entry<K, V>> samples() {
			return mapGenerator.samples();
		}

		@Override
		public Entry<K, V>[] createArray(int length) {
			return mapGenerator.createArray(length);
		}

		@Override
		public Iterable<Entry<K, V>> order(List<Entry<K, V>> insertionOrder) {
			return mapGenerator.order(insertionOrder);
		}

		@Override
		public Map<K, V> create(Object... elements) {
			return SerializableTester.reserialize(mapGenerator.create(elements));
		}

		@Override
		public K[] createKeyArray(int length) {
			return ((TestMapGenerator<K, V>) mapGenerator.getInnerGenerator()).createKeyArray(length);
		}

		@Override
		public V[] createValueArray(int length) {
			return ((TestMapGenerator<K, V>) mapGenerator.getInnerGenerator()).createValueArray(length);
		}
	}
}
