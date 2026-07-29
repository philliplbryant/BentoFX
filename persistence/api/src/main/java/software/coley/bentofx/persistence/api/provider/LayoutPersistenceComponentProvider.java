package software.coley.bentofx.persistence.api.provider;

/**
 * Common contract for persistence component providers that are discovered at
 * runtime.
 * <p>
 * Provider identifiers are stable names that applications can use to select a
 * specific provider when more than one implementation is available. When only
 * one provider of a given type is available, the default persistence provider
 * can select it automatically.
 *
 * @author Phil Bryant
 */
public interface LayoutPersistenceComponentProvider {

    /**
     * Returns the stable identifier for this provider.
     *
     * @return the stable provider identifier
     */
    String getIdentifier();

    /**
     * Indicates whether this provider should be preferred when more than one
     * provider of the same type is available and the application has not
     * explicitly selected one.
     *
     * @return {@code true} when this provider should be preferred by default
     */
    default boolean isDefault() {
        return false;
    }
}
