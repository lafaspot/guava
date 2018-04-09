package com.github.lafa.cache.base;

import static com.github.lafa.cache.base.StandardSystemProperty.JAVA_SPECIFICATION_VERSION;
import static com.github.lafa.cache.base.Throwables.getStackTraceAsString;
import static com.github.lafa.cache.base.Throwables.lazyStackTrace;
import static com.github.lafa.cache.base.Throwables.lazyStackTraceIsLazy;
import static com.github.lafa.cache.base.Throwables.throwIfInstanceOf;
import static com.github.lafa.cache.base.Throwables.throwIfUnchecked;
import static java.util.regex.Pattern.quote;

import java.util.Arrays;
import java.util.List;

import com.github.lafa.cache.common.testing.NullPointerTester;

import junit.framework.TestCase;

/**
 * Unit test for {@link Throwables}.
 *
 * @author Kevin Bourrillion
 */
public class ThrowablesTest extends TestCase {
	public void testThrowIfUnchecked_Unchecked() {
		try {
			throwIfUnchecked(new SomeUncheckedException());
			fail();
		} catch (SomeUncheckedException expected) {
		}
	}

	public void testThrowIfUnchecked_Error() {
		try {
			throwIfUnchecked(new SomeError());
			fail();
		} catch (SomeError expected) {
		}
	}

	public void testThrowIfUnchecked_Checked() {
		throwIfUnchecked(new SomeCheckedException());
	}

	// propagateIfPossible
	public void testPropagateIfPossible_NoneDeclared_NoneThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatDoesntThrowAnything();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect no exception to be thrown
		sample.noneDeclared();
	}

	// propagateIfPossible
	public void testPropagateIfPossible_NoneDeclared_UncheckedThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatThrowsUnchecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the unchecked exception to propagate as-is
		try {
			sample.noneDeclared();
			fail();
		} catch (SomeUncheckedException expected) {
		}
	}

	// propagateIfPossible
	public void testPropagateIfPossible_NoneDeclared_UndeclaredThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatThrowsUndeclaredChecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the undeclared exception to have been chained inside another
		try {
			sample.noneDeclared();
			fail();
		} catch (SomeChainingException expected) {
		}
	}

	// propagateIfPossible(Throwable, Class)
	public void testPropagateIfPossible_OneDeclared_NoneThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatDoesntThrowAnything();
				} catch (Throwable t) {
					// yes, this block is never reached, but for purposes of illustration
					// we're keeping it the same in each test
					Throwables.propagateIfPossible(t, SomeCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect no exception to be thrown
		sample.oneDeclared();
	}

	// propagateIfPossible(Throwable, Class)
	public void testPropagateIfPossible_OneDeclared_UncheckedThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatThrowsUnchecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the unchecked exception to propagate as-is
		try {
			sample.oneDeclared();
			fail();
		} catch (SomeUncheckedException expected) {
		}
	}

	// propagateIfPossible(Throwable, Class)
	public void testPropagateIfPossible_OneDeclared_CheckedThrown() {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatThrowsChecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the checked exception to propagate as-is
		try {
			sample.oneDeclared();
			fail();
		} catch (SomeCheckedException expected) {
		}
	}

	// propagateIfPossible(Throwable, Class)
	public void testPropagateIfPossible_OneDeclared_UndeclaredThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatThrowsUndeclaredChecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the undeclared exception to have been chained inside another
		try {
			sample.oneDeclared();
			fail();
		} catch (SomeChainingException expected) {
		}
	}

	// propagateIfPossible(Throwable, Class, Class)
	public void testPropagateIfPossible_TwoDeclared_NoneThrown()
			throws SomeCheckedException, SomeOtherCheckedException {
		Sample sample = new Sample() {
			@Override
			public void twoDeclared() throws SomeCheckedException, SomeOtherCheckedException {
				try {
					methodThatDoesntThrowAnything();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class, SomeOtherCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect no exception to be thrown
		sample.twoDeclared();
	}

	// propagateIfPossible(Throwable, Class, Class)
	public void testPropagateIfPossible_TwoDeclared_UncheckedThrown()
			throws SomeCheckedException, SomeOtherCheckedException {
		Sample sample = new Sample() {
			@Override
			public void twoDeclared() throws SomeCheckedException, SomeOtherCheckedException {
				try {
					methodThatThrowsUnchecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class, SomeOtherCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the unchecked exception to propagate as-is
		try {
			sample.twoDeclared();
			fail();
		} catch (SomeUncheckedException expected) {
		}
	}

	// propagateIfPossible(Throwable, Class, Class)
	public void testPropagateIfPossible_TwoDeclared_CheckedThrown() throws SomeOtherCheckedException {
		Sample sample = new Sample() {
			@Override
			public void twoDeclared() throws SomeCheckedException, SomeOtherCheckedException {
				try {
					methodThatThrowsChecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class, SomeOtherCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the checked exception to propagate as-is
		try {
			sample.twoDeclared();
			fail();
		} catch (SomeCheckedException expected) {
		}
	}

	// propagateIfPossible(Throwable, Class, Class)
	public void testPropagateIfPossible_TwoDeclared_OtherCheckedThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void twoDeclared() throws SomeCheckedException, SomeOtherCheckedException {
				try {
					methodThatThrowsOtherChecked();
				} catch (Throwable t) {
					Throwables.propagateIfPossible(t, SomeCheckedException.class, SomeOtherCheckedException.class);
					throw new SomeChainingException(t);
				}
			}
		};

		// Expect the checked exception to propagate as-is
		try {
			sample.twoDeclared();
			fail();
		} catch (SomeOtherCheckedException expected) {
		}
	}

	public void testThrowIfUnchecked_null() throws SomeCheckedException {
		try {
			throwIfUnchecked(null);
			fail();
		} catch (NullPointerException expected) {
		}
	}

	// propagateIfPossible
	public void testPropageIfPossible_null() throws SomeCheckedException {
		Throwables.propagateIfPossible(null);
	}

	// propagateIfPossible(Throwable, Class)
	public void testPropageIfPossible_OneDeclared_null() throws SomeCheckedException {
		Throwables.propagateIfPossible(null, SomeCheckedException.class);
	}

	// propagateIfPossible(Throwable, Class, Class)
	public void testPropageIfPossible_TwoDeclared_null() throws SomeCheckedException {
		Throwables.propagateIfPossible(null, SomeCheckedException.class, SomeUncheckedException.class);
	}

	// propagate
	public void testPropagate_NoneDeclared_NoneThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatDoesntThrowAnything();
				} catch (Throwable t) {
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect no exception to be thrown
		sample.noneDeclared();
	}

	// propagate
	public void testPropagate_NoneDeclared_UncheckedThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatThrowsUnchecked();
				} catch (Throwable t) {
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect the unchecked exception to propagate as-is
		try {
			sample.noneDeclared();
			fail();
		} catch (SomeUncheckedException expected) {
		}
	}

	// propagate
	public void testPropagate_NoneDeclared_ErrorThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatThrowsError();
				} catch (Throwable t) {
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect the error to propagate as-is
		try {
			sample.noneDeclared();
			fail();
		} catch (SomeError expected) {
		}
	}

	// propagate
	public void testPropagate_NoneDeclared_CheckedThrown() {
		Sample sample = new Sample() {
			@Override
			public void noneDeclared() {
				try {
					methodThatThrowsChecked();
				} catch (Throwable t) {
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect the undeclared exception to have been chained inside another
		try {
			sample.noneDeclared();
			fail();
		} catch (RuntimeException expected) {
			assertTrue(expected.getCause() instanceof SomeCheckedException);
		}
	}

	// throwIfInstanceOf
	public void testThrowIfInstanceOf_Unchecked() throws SomeCheckedException {
		throwIfInstanceOf(new SomeUncheckedException(), SomeCheckedException.class);
	}

	// throwIfInstanceOf
	public void testThrowIfInstanceOf_CheckedDifferent() throws SomeCheckedException {
		throwIfInstanceOf(new SomeOtherCheckedException(), SomeCheckedException.class);
	}

	// throwIfInstanceOf
	public void testThrowIfInstanceOf_CheckedSame() {
		try {
			throwIfInstanceOf(new SomeCheckedException(), SomeCheckedException.class);
			fail();
		} catch (SomeCheckedException expected) {
		}
	}

	// throwIfInstanceOf
	public void testThrowIfInstanceOf_CheckedSubclass() {
		try {
			throwIfInstanceOf(new SomeCheckedException() {
			}, SomeCheckedException.class);
			fail();
		} catch (SomeCheckedException expected) {
		}
	}

	// throwIfInstanceOf
	public void testPropagateIfInstanceOf_NoneThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatDoesntThrowAnything();
				} catch (Throwable t) {
					Throwables.propagateIfInstanceOf(t, SomeCheckedException.class);
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect no exception to be thrown
		sample.oneDeclared();
	}

	// throwIfInstanceOf
	public void testPropagateIfInstanceOf_DeclaredThrown() {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatThrowsChecked();
				} catch (Throwable t) {
					Throwables.propagateIfInstanceOf(t, SomeCheckedException.class);
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect declared exception to be thrown as-is
		try {
			sample.oneDeclared();
			fail();
		} catch (SomeCheckedException expected) {
		}
	}

	// throwIfInstanceOf
	public void testPropagateIfInstanceOf_UncheckedThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatThrowsUnchecked();
				} catch (Throwable t) {
					Throwables.propagateIfInstanceOf(t, SomeCheckedException.class);
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect unchecked exception to be thrown as-is
		try {
			sample.oneDeclared();
			fail();
		} catch (SomeUncheckedException expected) {
		}
	}

	// throwIfInstanceOf
	public void testPropagateIfInstanceOf_UndeclaredThrown() throws SomeCheckedException {
		Sample sample = new Sample() {
			@Override
			public void oneDeclared() throws SomeCheckedException {
				try {
					methodThatThrowsOtherChecked();
				} catch (Throwable t) {
					Throwables.propagateIfInstanceOf(t, SomeCheckedException.class);
					throw Throwables.propagate(t);
				}
			}
		};

		// Expect undeclared exception wrapped by RuntimeException to be thrown
		try {
			sample.oneDeclared();
			fail();
		} catch (RuntimeException expected) {
			assertTrue(expected.getCause() instanceof SomeOtherCheckedException);
		}
	}

	// throwIfInstanceOf
	public void testThrowIfInstanceOf_null() throws SomeCheckedException {
		try {
			throwIfInstanceOf(null, SomeCheckedException.class);
			fail();
		} catch (NullPointerException expected) {
		}
	}

	// throwIfInstanceOf
	public void testPropageIfInstanceOf_null() throws SomeCheckedException {
		Throwables.propagateIfInstanceOf(null, SomeCheckedException.class);
	}

	public void testGetRootCause_NoCause() {
		SomeCheckedException exception = new SomeCheckedException();
		assertSame(exception, Throwables.getRootCause(exception));
	}

	public void testGetRootCause_SingleWrapped() {
		SomeCheckedException cause = new SomeCheckedException();
		SomeChainingException exception = new SomeChainingException(cause);
		assertSame(cause, Throwables.getRootCause(exception));
	}

	public void testGetRootCause_DoubleWrapped() {
		SomeCheckedException cause = new SomeCheckedException();
		SomeChainingException exception = new SomeChainingException(new SomeChainingException(cause));
		assertSame(cause, Throwables.getRootCause(exception));
	}

	public void testGetRootCause_Loop() {
		Exception cause = new Exception();
		Exception exception = new Exception(cause);
		cause.initCause(exception);
		try {
			Throwables.getRootCause(cause);
			fail("Should have throw IAE");
		} catch (IllegalArgumentException expected) {
			assertEquals(expected.getCause(), cause);
		}
	}

	private static class SomeError extends Error {
	}

	private static class SomeCheckedException extends Exception {
	}

	private static class SomeOtherCheckedException extends Exception {
	}

	private static class SomeUncheckedException extends RuntimeException {
	}

	private static class SomeUndeclaredCheckedException extends Exception {
	}

	private static class SomeChainingException extends RuntimeException {
		public SomeChainingException(Throwable cause) {
			super(cause);
		}
	}

	static class Sample {
		void noneDeclared() {
		}

		void oneDeclared() throws SomeCheckedException {
		}

		void twoDeclared() throws SomeCheckedException, SomeOtherCheckedException {
		}
	}

	static void methodThatDoesntThrowAnything() {
	}

	static void methodThatThrowsError() {
		throw new SomeError();
	}

	static void methodThatThrowsUnchecked() {
		throw new SomeUncheckedException();
	}

	static void methodThatThrowsChecked() throws SomeCheckedException {
		throw new SomeCheckedException();
	}

	static void methodThatThrowsOtherChecked() throws SomeOtherCheckedException {
		throw new SomeOtherCheckedException();
	}

	static void methodThatThrowsUndeclaredChecked() throws SomeUndeclaredCheckedException {
		throw new SomeUndeclaredCheckedException();
	}

	// getStackTraceAsString(Throwable)
	public void testGetStackTraceAsString() {
		class StackTraceException extends Exception {
			StackTraceException(String message) {
				super(message);
			}
		}

		StackTraceException e = new StackTraceException("my message");

		String firstLine = quote(e.getClass().getName() + ": " + e.getMessage());
		String secondLine = "\\s*at " + ThrowablesTest.class.getName() + "\\..*";
		String moreLines = "(?:.*\n?)*";
		String expected = firstLine + "\n" + secondLine + "\n" + moreLines;
		String msg = getStackTraceAsString(e);
		assertTrue(msg.matches(expected));
	}

	public void testGetCausalChain() {
		SomeUncheckedException sue = new SomeUncheckedException();
		IllegalArgumentException iae = new IllegalArgumentException(sue);
		RuntimeException re = new RuntimeException(iae);
		IllegalStateException ex = new IllegalStateException(re);

		assertEquals(Arrays.asList(ex, re, iae, sue), Throwables.getCausalChain(ex));
		assertSame(sue, Throwables.getCausalChain(sue).get(0));

		List<Throwable> causes = Throwables.getCausalChain(ex);
		try {
			causes.add(new RuntimeException());
			fail("List should be unmodifiable");
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testGetCasualChainNull() {
		try {
			Throwables.getCausalChain(null);
			fail("Should have throw NPE");
		} catch (NullPointerException expected) {
		}
	}

	public void testGetCasualChainLoop() {
		Exception cause = new Exception();
		Exception exception = new Exception(cause);
		cause.initCause(exception);
		try {
			Throwables.getCausalChain(cause);
			fail("Should have throw IAE");
		} catch (IllegalArgumentException expected) {
			assertEquals(expected.getCause(), cause);
		}
	}

	// Throwables.getCauseAs(Throwable, Class)
	public void testGetCauseAs() {
		SomeCheckedException cause = new SomeCheckedException();
		SomeChainingException thrown = new SomeChainingException(cause);

		assertEquals(thrown.getCause(), cause);
		assertTrue(thrown.getCause() instanceof SomeCheckedException);
		assertTrue(thrown.getCause() instanceof Exception);

		try {
			Throwables.getCauseAs(thrown, IllegalStateException.class);
			fail("Should have thrown CCE");
		} catch (ClassCastException expected) {
			assertEquals(expected.getCause(), thrown);
		}
	}

	// No getJavaLangAccess in Android (at least not in the version we use).
	// lazyStackTraceIsLazy()
	public void testLazyStackTraceWorksInProd() {
		// TODO(b/64442212): Remove this guard once lazyStackTrace() works in Java 9.
		if (JAVA_SPECIFICATION_VERSION.value().equals("9")) {
			return;
		}
		// Obviously this isn't guaranteed in every environment, but it works well
		// enough for now:
		assertTrue(lazyStackTraceIsLazy());
	}

	// lazyStackTrace(Throwable)
	public void testLazyStackTrace() {
		Exception e = new Exception();
		StackTraceElement[] originalStackTrace = e.getStackTrace();
		assertEquals(lazyStackTrace(e), Arrays.asList(originalStackTrace));

		try {
			lazyStackTrace(e).set(0, null);
			fail();
		} catch (UnsupportedOperationException expected) {
		}

		// Now we test a property that holds only for the lazy implementation.

		if (!lazyStackTraceIsLazy()) {
			return;
		}

		e.setStackTrace(new StackTraceElement[0]);
		assertEquals(lazyStackTrace(e), Arrays.asList(originalStackTrace));
	}

	// NullPointerTester
	public void testNullPointers() {
		new NullPointerTester().testAllPublicStaticMethods(Throwables.class);
	}
}
