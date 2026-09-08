package software.coley.bentofx.persistence.core.impl.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.core.api.BentoStateException;
import software.coley.bentofx.persistence.core.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.core.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.core.api.codec.PersistableLayout;
import software.coley.bentofx.persistence.core.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.core.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.core.api.state.BentoState;
import software.coley.bentofx.persistence.core.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.provider.InMemoryLayoutStorageProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers.GROUP_CATALOG_LAYOUT_IDENTIFIER;
import static software.coley.bentofx.persistence.testfixtures.codec.state.SampleBentoStateFactory.createBentoStates;

/**
 * Coverage for the two operations that reach a stored layout without reading the
 * scene graph: rewriting what a layout is called, and the catalog of groups.
 *
 * <p>These need storage that keeps what was written to it, which
 * {@code ConfigurableLayoutStorageProvider} deliberately does not - it hands out a fresh,
 * empty storage per call so that a test can watch what a component asks for.
 * {@link InMemoryLayoutStorageProvider} keeps bytes per layout identifier instead,
 * so a read after a write returns what the write put there, which is the whole
 * point of what is under test.</p>
 *
 * @author Phil Bryant
 */
class DefaultDockingLayoutPersistenceProviderNamingTest {

    private static final String LAYOUT_IDENTIFIER = "compact";

    @Test
    void updateStoredLayoutNamingRewritesTheNamingAndKeepsTheState()
            throws BentoStateException {
        final InMemoryLayoutStorageProvider storage =
                new InMemoryLayoutStorageProvider();
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storage, codec);
        final List<BentoState> original = createBentoStates();

        store(storage, codec, LAYOUT_IDENTIFIER, new PersistableLayout("Wide", original));

        final boolean rewritten = provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Renamed", "Debugging")
        );

        assertThat(rewritten)
                .describedAs("updateStoredLayoutNaming for a stored layout")
                .isTrue();

        final PersistableLayout stored = read(storage, codec, LAYOUT_IDENTIFIER);

        assertThat(stored.displayName())
                .describedAs("display name after the rewrite")
                .isEqualTo("Renamed");
        assertThat(stored.group())
                .describedAs("group after the rewrite")
                .isEqualTo("Debugging");
        assertThat(stored.bentoStates())
                .describedAs("docking state after the rewrite")
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    /**
     * The layout is read to completion before anything is opened for writing.
     *
     * <p>Opening for write truncates a file-backed layout, so reading and writing
     * through one open storage would empty the layout being renamed - it would
     * decode nothing and store that. Asserting on the order catches a reordering
     * here, which no assertion on the result can: an in-memory double survives
     * either order.</p>
     */
    @Test
    void updateStoredLayoutNamingReadsBeforeItOpensAnythingForWriting()
            throws BentoStateException {
        final RecordingStorageProvider storage = new RecordingStorageProvider();
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storage, codec);

        store(storage, codec, LAYOUT_IDENTIFIER,
                new PersistableLayout("Wide", createBentoStates()));
        storage.streamsOpened().clear();

        provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Renamed", null)
        );

        assertThat(storage.streamsOpened())
                .describedAs("streams opened while rewriting the naming")
                .containsExactly(
                        "read:" + LAYOUT_IDENTIFIER,
                        "write:" + LAYOUT_IDENTIFIER
                );
    }

    /**
     * A rename has to be able to clear a group, which is how a layout is taken out
     * of one, so a {@code null} is written rather than skipped.
     */
    @Test
    void updateStoredLayoutNamingClearsAGroupWhenGivenNone()
            throws BentoStateException {
        final InMemoryLayoutStorageProvider storage =
                new InMemoryLayoutStorageProvider();
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storage, codec);

        store(storage, codec, LAYOUT_IDENTIFIER,
                new PersistableLayout(
                        "Wide",
                        createBentoStates(),
                        "Debugging",
                        List.of()
                ));

        provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Wide", null)
        );

        assertThat(read(storage, codec, LAYOUT_IDENTIFIER).group())
                .describedAs("group after being cleared")
                .isNull();
    }

    /**
     * Nothing stored means nothing written. Creating an entry here would leave a
     * layout holding no docking state, which nothing could restore.
     */
    @Test
    void updateStoredLayoutNamingWritesNothingWhenNothingIsStored()
            throws BentoStateException {
        final InMemoryLayoutStorageProvider storage =
                new InMemoryLayoutStorageProvider();
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storage, codec);

        final boolean rewritten = provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Renamed", null)
        );

        assertThat(rewritten)
                .describedAs("updateStoredLayoutNaming for a layout not stored")
                .isFalse();
        assertThat(storage.getLayoutIdentifiers(codec.getIdentifier()))
                .describedAs("layouts in storage afterwards")
                .isEmpty();
    }

    @Test
    void groupCatalogRoundTripsThroughItsReservedEntry()
            throws BentoStateException {
        final InMemoryLayoutStorageProvider storage =
                new InMemoryLayoutStorageProvider();
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storage, codec);
        final LayoutPersistenceProfile profile =
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER);

        provider.setStoredGroups(profile, List.of("Debugging", "Presentation"));

        assertThat(provider.getStoredGroups(profile))
                .describedAs("group catalog read back")
                .containsExactly("Debugging", "Presentation");
        assertThat(storage.getLayoutIdentifiers(codec.getIdentifier()))
                .describedAs("what the catalog is stored under")
                .containsExactly(GROUP_CATALOG_LAYOUT_IDENTIFIER);
    }

    /**
     * A group with no layouts in it is the case the catalog exists for, so an
     * empty catalog has to be a real answer rather than a fault.
     */
    @Test
    void reportsNoGroupsWhenTheCatalogWasNeverWritten()
            throws BentoStateException {
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(new InMemoryLayoutStorageProvider(), new InMemoryLayoutCodec());

        assertThat(provider.getStoredGroups(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
        ))
                .describedAs("group catalog that was never written")
                .isEmpty();
    }

    @Test
    void emptyingTheCatalogLeavesNoGroups() throws BentoStateException {
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(new InMemoryLayoutStorageProvider(), new InMemoryLayoutCodec());
        final LayoutPersistenceProfile profile =
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER);

        provider.setStoredGroups(profile, List.of("Debugging"));
        provider.setStoredGroups(profile, List.of());

        assertThat(provider.getStoredGroups(profile))
                .describedAs("group catalog after being emptied")
                .isEmpty();
    }

    /**
     * The group lives inside the layout, so listing has to decode to find it. A
     * caller building a menu reads it off the profiles it gets back.
     */
    @Test
    void listingStoredLayoutsCarriesTheGroupOntoEachProfile()
            throws BentoStateException {
        final InMemoryLayoutStorageProvider storage =
                new InMemoryLayoutStorageProvider();
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storage, codec);

        store(storage, codec, LAYOUT_IDENTIFIER,
                new PersistableLayout(
                        "TCP/IP Debug",
                        createBentoStates(),
                        "Debugging",
                        List.of()
                ));

        assertThat(provider.getStoredLayouts(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
        ))
                .describedAs("stored layouts listed as profiles")
                .singleElement()
                .satisfies(profile -> {
                    assertThat(profile.displayName())
                            .describedAs("listed display name")
                            .isEqualTo("TCP/IP Debug");
                    assertThat(profile.group())
                            .describedAs("listed group")
                            .isEqualTo("Debugging");
                });
    }

    /**
     * {@return a provider wired to the supplied storage and to one codec that keeps
     * what it encoded.}
     *
     * @param storage the storage to write through.
     * @param codec the codec whose output is stored.
     */
    private static DefaultDockingLayoutPersistenceProvider providerFor(
            final LayoutStorageProvider storage,
            final LayoutCodec codec
    ) {
        return new DefaultDockingLayoutPersistenceProvider(
                List.of(new SingleCodecProvider(codec)),
                List.of(storage)
        );
    }

    /**
     * Encodes and writes a layout the way an earlier save would have left it, so a
     * test can start from a stored layout.
     */
    private static void store(
            final LayoutStorageProvider storage,
            final LayoutCodec codec,
            final String layoutIdentifier,
            final PersistableLayout layout
    ) throws BentoStateException {
        try (final OutputStream outputStream =
                     storage.getLayoutStorage(layoutIdentifier, codec.getIdentifier())
                             .openOutputStream()) {

            codec.encode(layout, outputStream);
        } catch (final IOException e) {
            throw new BentoStateException("Could not store the layout.", e);
        }
    }

    /**
     * {@return the stored layout, decoded.}
     */
    private static PersistableLayout read(
            final LayoutStorageProvider storage,
            final LayoutCodec codec,
            final String layoutIdentifier
    ) throws BentoStateException {
        try (final InputStream inputStream =
                     storage.getLayoutStorage(layoutIdentifier, codec.getIdentifier())
                             .openInputStream()) {

            return codec.decode(inputStream);
        } catch (final IOException e) {
            throw new BentoStateException("Could not read the layout.", e);
        }
    }

    /**
     * Wraps {@link InMemoryLayoutStorageProvider} to record the order in which
     * streams are opened, so a test can assert a read happened before a write
     * rather than only that both did. The recording is why this is not the general
     * fixture: only the read-before-write test needs it.
     */
    private static final class RecordingStorageProvider implements LayoutStorageProvider {

        private final InMemoryLayoutStorageProvider delegate =
                new InMemoryLayoutStorageProvider();
        private final List<String> streamsOpened = new ArrayList<>();

        @Override
        public String getIdentifier() {
            return delegate.getIdentifier();
        }

        @Override
        public boolean isDefault() {
            return delegate.isDefault();
        }

        @Override
        public List<String> getLayoutIdentifiers(final String codecIdentifier) {
            return delegate.getLayoutIdentifiers(codecIdentifier);
        }

        @Override
        public boolean isLayoutStored(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            return delegate.isLayoutStored(layoutIdentifier, codecIdentifier);
        }

        @Override
        public boolean deleteLayout(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            return delegate.deleteLayout(layoutIdentifier, codecIdentifier);
        }

        @Override
        public LayoutStorage getLayoutStorage(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            final LayoutStorage inner =
                    delegate.getLayoutStorage(layoutIdentifier, codecIdentifier);

            return new LayoutStorage() {
                @Override
                public boolean exists() {
                    return inner.exists();
                }

                @Override
                public OutputStream openOutputStream() throws IOException {
                    streamsOpened.add("write:" + layoutIdentifier);
                    return inner.openOutputStream();
                }

                @Override
                public InputStream openInputStream() throws IOException {
                    streamsOpened.add("read:" + layoutIdentifier);
                    return inner.openInputStream();
                }
            };
        }

        private List<String> streamsOpened() {
            return streamsOpened;
        }
    }

    /**
     * A {@link LayoutCodecProvider} handing out one codec instance, so that what
     * one call encoded a later call can decode.
     */
    private record SingleCodecProvider(LayoutCodec codec)
            implements LayoutCodecProvider {

        @Override
        public String getIdentifier() {
            return codec.getIdentifier();
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public LayoutCodec getLayoutCodec() {
            return codec;
        }
    }
}
