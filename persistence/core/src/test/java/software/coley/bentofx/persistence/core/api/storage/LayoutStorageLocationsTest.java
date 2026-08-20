package software.coley.bentofx.persistence.core.api.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static software.coley.bentofx.persistence.core.api.storage.LayoutStorageLocations.*;

/**
 * Coverage for resolving where storage providers keep their data.
 *
 * <p>{@link LayoutStorageLocations#readSetting} is tested directly, against a
 * made-up system property paired with a real, reliably-set environment
 * variable ({@code NUMBER_OF_PROCESSORS} on Windows), rather than trying to
 * control {@code BENTOFX_PERSISTENCE_HOME}/{@code BENTOFX_PERSISTENCE_NAMESPACE}
 * themselves - a JVM cannot change its own environment once it is running, so
 * that is the only way to exercise the "falls back to the environment
 * variable" branch deterministically.
 *
 * @author Phil Bryant
 */
class LayoutStorageLocationsTest {

    private static final String NAMESPACE = "acme-widgets";

    /**
     * Reliably set on Windows to a simple digit string.
     */
    private static final String RELIABLE_ENV_VARIABLE = "NUMBER_OF_PROCESSORS";

    /**
     * A system property name this suite never sets by itself, kept apart from the constant it stands in for.
     */
    private static final String UNSET_TEST_PROPERTY = "bentofx.persistence.test.unset-marker";

    private String realUserHome;
    private String realHomeOverride;
    private String realNamespace;

    @BeforeEach
    void setUp() {
        realUserHome = System.getProperty(USER_HOME_PROPERTY);
        realHomeOverride = System.getProperty(HOME_DIRECTORY_PROPERTY);
        realNamespace = System.getProperty(NAMESPACE_PROPERTY);

        System.clearProperty(HOME_DIRECTORY_PROPERTY);
        System.clearProperty(NAMESPACE_PROPERTY);
    }

    @AfterEach
    void tearDown() {
        setOrClear(USER_HOME_PROPERTY, realUserHome);
        setOrClear(HOME_DIRECTORY_PROPERTY, realHomeOverride);
        setOrClear(NAMESPACE_PROPERTY, realNamespace);
    }

    @Test
    void resolvesUnderUserHomeByDefault() {
        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() with neither property set")
                .isEqualTo(
                        Path.of(
                                        System.getProperty(USER_HOME_PROPERTY),
                                        BENTOFX_DIRECTORY_NAME
                                )
                                .toAbsolutePath()
                                .normalize());
    }

    @Test
    void homePropertyOverridesTheBaseDirectory(@TempDir final Path customHome) {
        System.setProperty(HOME_DIRECTORY_PROPERTY, customHome.toString());

        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() with the home property set")
                .isEqualTo(customHome.toAbsolutePath().normalize());
    }

    @Test
    void namespacePropertyAddsASubdirectoryOfTheDefaultBase() {
        System.setProperty(NAMESPACE_PROPERTY, NAMESPACE);

        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() with the namespace property set")
                .isEqualTo(
                        Path.of(
                                        System.getProperty(USER_HOME_PROPERTY),
                                        BENTOFX_DIRECTORY_NAME,
                                        NAMESPACE
                                )
                                .toAbsolutePath()
                                .normalize());
    }

    @Test
    void homeAndNamespacePropertiesCombine(@TempDir final Path customHome) {
        System.setProperty(HOME_DIRECTORY_PROPERTY, customHome.toString());
        System.setProperty(NAMESPACE_PROPERTY, NAMESPACE);

        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() with both properties set")
                .isEqualTo(
                        customHome.resolve(NAMESPACE)
                                .toAbsolutePath()
                                .normalize()
                );
    }

    @Test
    void aBlankNamespacePropertyIsTreatedAsAbsent() {
        System.setProperty(NAMESPACE_PROPERTY, "   ");

        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() with a blank namespace property")
                .isEqualTo(
                        Path.of(
                                        System.getProperty(USER_HOME_PROPERTY),
                                        BENTOFX_DIRECTORY_NAME
                                )
                                .toAbsolutePath()
                                .normalize()
                );
    }

    @Test
    void aNamespacePropertyContainingASeparatorIsRejected() {
        System.setProperty(NAMESPACE_PROPERTY, "nested/namespace");

        assertThatThrownBy(LayoutStorageLocations::resolveBentoFxHome)
                .describedAs("resolveBentoFxHome() with a namespace containing a separator")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NAMESPACE_PROPERTY)
                .hasMessageContaining("nested/namespace");
    }

    @Test
    void configureHomeSetsThePropertyResolveBentoFxHomeReads(@TempDir final Path customHome) {
        LayoutStorageLocations.configureHome(customHome);

        assertThat(System.getProperty(HOME_DIRECTORY_PROPERTY))
                .describedAs("home property after configureHome")
                .isEqualTo(customHome.toString());
        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() after configureHome")
                .isEqualTo(customHome.toAbsolutePath().normalize());
    }

    @Test
    void configureNamespaceSetsThePropertyResolveBentoFxHomeReads() {
        LayoutStorageLocations.configureNamespace(NAMESPACE);

        assertThat(System.getProperty(NAMESPACE_PROPERTY))
                .describedAs("namespace property after configureNamespace")
                .isEqualTo(NAMESPACE);
        assertThat(LayoutStorageLocations.resolveBentoFxHome())
                .describedAs("resolveBentoFxHome() after configureNamespace")
                .isEqualTo(
                        Path.of(
                                        System.getProperty(USER_HOME_PROPERTY),
                                        BENTOFX_DIRECTORY_NAME,
                                        NAMESPACE
                                )
                                .toAbsolutePath()
                                .normalize()
                );
    }

    @Test
    void configureNamespaceRejectsAnUnusableValueImmediately() {
        assertThatThrownBy(() -> LayoutStorageLocations.configureNamespace("nested/namespace"))
                .describedAs("configureNamespace with a separator")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NAMESPACE_PROPERTY);

        assertThat(System.getProperty(NAMESPACE_PROPERTY))
                .describedAs("namespace property after a rejected configureNamespace call")
                .isNull();
    }

    @Test
    void configureNamespaceRejectsABlankValue() {
        assertThatThrownBy(() -> LayoutStorageLocations.configureNamespace(""))
                .describedAs("configureNamespace with a blank value")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NAMESPACE_PROPERTY);
    }

    @Test
    void configureHomeAndConfigureNamespaceRejectNull() {
        assertThatThrownBy(() -> LayoutStorageLocations.configureHome(null))
                .describedAs("configureHome(null)")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("home");

        assertThatThrownBy(() -> LayoutStorageLocations.configureNamespace(null))
                .describedAs("configureNamespace(null)")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("namespace");
    }

    @Test
    void readSettingFallsBackToTheEnvironmentVariableWhenThePropertyIsUnset() {
        final String variableValue = System.getenv(RELIABLE_ENV_VARIABLE);
        assumeTrue(
                variableValue != null,
                RELIABLE_ENV_VARIABLE + " is not set in this environment"
        );
        System.clearProperty(UNSET_TEST_PROPERTY);

        assertThat(
                LayoutStorageLocations.readSetting(
                        UNSET_TEST_PROPERTY,
                        RELIABLE_ENV_VARIABLE
                )
        )
                .describedAs("readSetting with the property unset")
                .isEqualTo(variableValue);
    }

    @Test
    void readSettingPrefersThePropertyOverTheEnvironmentVariable() {
        final String variableValue = System.getenv(RELIABLE_ENV_VARIABLE);
        assumeTrue(
                variableValue != null,
                RELIABLE_ENV_VARIABLE + " is not set in this environment"
        );

        System.setProperty(UNSET_TEST_PROPERTY, "property-wins");
        try {
            assertThat(
                    LayoutStorageLocations.readSetting(
                            UNSET_TEST_PROPERTY,
                            RELIABLE_ENV_VARIABLE
                    )
            )
                    .describedAs("readSetting with both the property and the variable set")
                    .isEqualTo("property-wins");
        } finally {
            System.clearProperty(UNSET_TEST_PROPERTY);
        }
    }

    @Test
    void readSettingReturnsNullWhenNeitherIsSet() {
        System.clearProperty(UNSET_TEST_PROPERTY);

        assertThat(LayoutStorageLocations.readSetting(
                UNSET_TEST_PROPERTY,
                "BENTOFX_TEST_ENV_DOES_NOT_EXIST_XYZ123"
        ))
                .describedAs("readSetting with neither the property nor the variable set")
                .isNull();
    }

    /**
     * A utility class with only static members has no reason to be
     * instantiated; the private constructor exists to say so rather than to
     * silently allow it.
     */
    @Test
    void utilityClassConstructorThrowsIllegalStateException() throws Exception {
        final Constructor<LayoutStorageLocations> constructor =
                LayoutStorageLocations.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .describedAs("reflective instantiation of the utility class")
                .isInstanceOf(InvocationTargetException.class)
                .cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Utility class");
    }

    private static void setOrClear(final String property, final String value) {
        if (value == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, value);
        }
    }
}
