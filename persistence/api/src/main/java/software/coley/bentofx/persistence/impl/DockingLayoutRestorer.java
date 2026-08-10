package software.coley.bentofx.persistence.impl;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerLeafMenuFactory;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.BentoStateTimeoutException;
import software.coley.bentofx.persistence.api.DockingLayout;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Restores persisted {@link DockingLayout}s.
 *
 * @author Phil Bryant
 */
public class DockingLayoutRestorer implements LayoutRestorer {

    private static final Logger logger =
            LoggerFactory.getLogger(DockingLayoutRestorer.class);

    private final LayoutStorage layoutStorage;
    private final LayoutStateReader layoutStateReader;
    private final DockingLayoutStateRestorer dockingLayoutStateRestorer;

    /**
     * Constructs a {code DockingLayoutRestorer}.
     *
     * @param layoutCodec                          the {@link LayoutCodec} to use to decode the persisted
     *                                             layout.
     * @param layoutStorage                        the {@link LayoutStorage} to use to read the
     *                                             persisted layout.
     * @param bentoProvider                        the {@link BentoProvider} to use to get {@link Bento}
     *                                             instances
     *                                             from their identifier.
     * @param dockableStateProvider                the {@link DockableStateProvider} to use to
     *                                             get {@link software.coley.bentofx.dockable.Dockable} instances from their
     *                                             identifier.
     * @param stageIconImageProvider               the {@link StageIconImageProvider} to use
     *                                             to get icons for
     *                                             restored {@link DragDropStage} instances.
     * @param dockContainerLeafMenuFactoryProvider the
     *                                             {@link DockContainerLeafMenuFactoryProvider} to use to get
     *                                             {@link DockContainerLeafMenuFactory} for restored
     *                                             {@link DockContainerLeaf} instances.
     */
    public DockingLayoutRestorer(
            final LayoutCodec layoutCodec,
            final LayoutStorage layoutStorage,
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) {
        this(
                layoutStorage,
                new LayoutStateReader(layoutCodec, layoutStorage),
                new DockingLayoutStateRestorer(
                        bentoProvider,
                        dockableStateProvider,
                        stageIconImageProvider,
                        dockContainerLeafMenuFactoryProvider
                )
        );
    }

    DockingLayoutRestorer(
            final LayoutStorage layoutStorage,
            final LayoutStateReader layoutStateReader,
            final DockingLayoutStateRestorer dockingLayoutStateRestorer
    ) {
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.layoutStateReader = Objects.requireNonNull(layoutStateReader);
        this.dockingLayoutStateRestorer = Objects.requireNonNull(dockingLayoutStateRestorer);
    }

    @Override
    public boolean doesLayoutExist() {
        return layoutStorage.exists();
    }

    @Override
    public DockingLayout restoreLayout(
            final Supplier<DockingLayout> defaultLayoutSupplier
    ) {

        if (!doesLayoutExist()) {
            return getDefaultLayout(defaultLayoutSupplier);
        }

        try {
            final List<BentoState> bentoStateList =
                    PersistenceThreading.callOffFxThread(
                            layoutStateReader::readLayoutState
                    );

            return PersistenceThreading.callOnFxThread(() ->
                    dockingLayoutStateRestorer.restoreDockingLayout(
                            bentoStateList
                    )
            );

        } catch (final BentoStateTimeoutException e) {
            // Deliberately not handled like the failure below. A timeout means
            // the JavaFX thread never ran the restore, not that the persisted
            // layout is bad. Substituting the default layout here would discard
            // a layout that is very likely fine, and the next automatic save
            // would then write that default over the saved copy. Fail loudly
            // instead and leave the persisted layout untouched.
            throw new IllegalStateException(
                    "Timed out restoring the docking layout",
                    e
            );
        } catch (final BentoStateException e) {
            logger.warn(
                    "An error occurred while attempting to restore the layout",
                    e
            );

            return getDefaultLayout(defaultLayoutSupplier);
        }
    }

    /**
     * Gets the fallback/default layout on the JavaFX application thread.
     *
     * @param defaultLayoutSupplier default layout supplier.
     * @return default layout.
     */
    private DockingLayout getDefaultLayout(
            final Supplier<DockingLayout> defaultLayoutSupplier
    ) {
        try {
            return PersistenceThreading.callOnFxThread(defaultLayoutSupplier::get);
        } catch (final BentoStateException e) {
            throw new IllegalStateException(
                    "Could not create default docking layout",
                    e
            );
        }
    }


    @Override
    public void close() {
        layoutStateReader.close();
    }
}
