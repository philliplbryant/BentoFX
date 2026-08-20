package software.coley.bentofx.persistence.core.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockingLayoutPersistenceTest {

    /**
     * This module publishes no {@code DockingLayoutPersistenceProvider}
     * service of its own - only an implementation module would - so resolving
     * one from here deterministically finds none.
     */
    @Test
    void providerThrowsIllegalStateExceptionWhenNoImplementationIsOnTheModulePath() {
        assertThatThrownBy(DockingLayoutPersistence::provider)
                .describedAs("provider() with no implementation module present")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No DockingLayoutPersistenceProvider implementation was found");
    }

    /**
     * A utility class with only static members has no reason to be
     * instantiated; the private constructor exists to say so rather than to
     * silently allow it.
     */
    @Test
    void utilityClassConstructorThrowsIllegalStateException() throws Exception {
        final Constructor<DockingLayoutPersistence> constructor =
                DockingLayoutPersistence.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .describedAs("reflective instantiation of the utility class")
                .isInstanceOf(InvocationTargetException.class)
                .cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Utility class");
    }
}
