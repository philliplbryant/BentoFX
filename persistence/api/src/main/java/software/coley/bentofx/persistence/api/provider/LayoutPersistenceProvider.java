/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public interface LayoutPersistenceProvider {

    @NotNull LayoutSaver getLayoutSaver(
            final @NotNull BentoProvider bentoProvider,
            final @NotNull String layoutIdentifier
    );

    @NotNull LayoutRestorer getLayoutRestorer(
            final @NotNull BentoProvider bentoprovider,
            final @NotNull String layoutIdentifier,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    );
}
