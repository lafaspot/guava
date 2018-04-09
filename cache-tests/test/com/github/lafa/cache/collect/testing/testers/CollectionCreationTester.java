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

package com.github.lafa.cache.collect.testing.testers;

import static com.github.lafa.cache.collect.testing.features.CollectionFeature.ALLOWS_NULL_VALUES;
import static com.github.lafa.cache.collect.testing.features.CollectionSize.ZERO;

import java.lang.reflect.Method;

import org.junit.Ignore;

import com.github.lafa.cache.collect.testing.AbstractCollectionTester;
import com.github.lafa.cache.collect.testing.Helpers;
import com.github.lafa.cache.collect.testing.features.CollectionFeature;
import com.github.lafa.cache.collect.testing.features.CollectionSize;


/**
 * A generic JUnit test which tests creation (typically through a constructor or static factory
 * method) of a collection. Can't be invoked directly; please see {@link
 * com.github.lafa.cache.collect.testing.CollectionTestSuiteBuilder}.
 *
 * @author Chris Povirk
 */
@Ignore // Affects only Android test runner, which respects JUnit 4 annotations on JUnit 3 tests.
public class CollectionCreationTester<E> extends AbstractCollectionTester<E> {
  @CollectionFeature.Require(ALLOWS_NULL_VALUES)
  @CollectionSize.Require(absent = ZERO)
  public void testCreateWithNull_supported() {
    E[] array = createArrayWithNullElement();
    collection = getSubjectGenerator().create(array);
    expectContents(array);
  }

  @CollectionFeature.Require(absent = ALLOWS_NULL_VALUES)
  @CollectionSize.Require(absent = ZERO)
  public void testCreateWithNull_unsupported() {
    E[] array = createArrayWithNullElement();

    try {
      getSubjectGenerator().create(array);
      fail("Creating a collection containing null should fail");
    } catch (NullPointerException expected) {
    }
  }

  /**
   * Returns the {@link Method} instance for {@link #testCreateWithNull_unsupported()} so that tests
   * can suppress it with {@code FeatureSpecificTestSuiteBuilder.suppressing()} until <a
   * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5045147">Sun bug 5045147</a> is fixed.
   */
   // reflection
  public static Method getCreateWithNullUnsupportedMethod() {
    return Helpers.getMethod(CollectionCreationTester.class, "testCreateWithNull_unsupported");
  }
}
