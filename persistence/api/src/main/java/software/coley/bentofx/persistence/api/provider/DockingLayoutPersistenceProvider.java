package software.coley.bentofx.persistence.api.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public interface DockingLayoutPersistenceProvider {

    /**
     * {@return a {@link LayoutSaver} with the specified identifier, already
     * saving automatically.}
     * @param layoutIdentifier the identifier to use to distinguish the
     * {@link LayoutSaver} from other {@link LayoutSaver}s.
     * @param bentoProvider used to acquire {@code Bento}.
     * @throws BentoStateException when the {@link LayoutSaver} cannot be
     * returned.
     */
    default LayoutSaver getLayoutSaver(
            final String layoutIdentifier,
            final BentoProvider bentoProvider
    ) throws BentoStateException {
        return getLayoutSaver(
                LayoutPersistenceProfile.of(layoutIdentifier),
                bentoProvider
        );
    }

    /**
     * {@return a {@link LayoutSaver} for the specified profile, already saving
     * automatically.}
     *
     * <p>Starting auto-save is the implementation's responsibility: a saver is
     * not permitted to arm itself from its own constructor, so a saver obtained
     * any other way is not auto-saving until the caller asks it to.</p>
     *
     * @param layoutPersistenceProfile identifies the {@link LayoutSaver} to
     * return.
     * @param bentoProvider used to acquire {@code Bento}.
     * @throws BentoStateException when the {@link LayoutSaver} cannot be
     * returned.
     */
    LayoutSaver getLayoutSaver(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoProvider
    ) throws BentoStateException;

    /**
     * {@return a {@code LayoutRestorer} with the specified identifier.}
     * @param layoutIdentifier the identifier to use to distinguish the
     * {@code LayoutRestorer} from other {@code LayoutRestorer}s.
     * @param bentoProvider used to acquire {@code Bento}.
     * @param dockableStateProvider used to acquire {@code DockableState}
     * @param stageIconImageProvider used to acquire {@code Stage} icon
     * {@code Image}s, {@code null} when a restored {@code Stage} should not
     * have its icon {@code Image}s set.
     * @param dockContainerLeafMenuFactoryProvider used to acquire
     * {@code DockContainerLeafMenuFactory}, {@code null} when the
     * {@code DockContainerLeafMenu} should not be set.
     * @throws BentoStateException when the {@code LayoutRestorer} cannot be
     * returned.
     */
    default LayoutRestorer getLayoutRestorer(
            final String layoutIdentifier,
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) throws BentoStateException {
        return getLayoutRestorer(
                LayoutPersistenceProfile.of(layoutIdentifier),
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider
        );
    }

    /**
     * {@return a {@code LayoutRestorer} with the specified identifier.}
     * @param layoutPersistenceProfile identifies the {@link LayoutRestorer} to return.
     * @param bentoProvider used to acquire {@code Bento}.
     * @param dockableStateProvider used to acquire {@code DockableState}
     * @param stageIconImageProvider used to acquire {@code Stage} icon
     * {@code Image}s, {@code null} when a restored {@code Stage} should not
     * have its icon {@code Image}s set.
     * @param dockContainerLeafMenuFactoryProvider used to acquire
     * {@code DockContainerLeafMenuFactory}, {@code null} when the
     * {@code DockContainerLeafMenu} should not be set.
     * @throws BentoStateException when the {@code LayoutRestorer} cannot be
     * returned.
     */
    LayoutRestorer getLayoutRestorer(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) throws BentoStateException;
}
