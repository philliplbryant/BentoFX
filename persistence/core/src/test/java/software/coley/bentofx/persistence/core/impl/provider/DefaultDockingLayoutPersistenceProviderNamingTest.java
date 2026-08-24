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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers.GROUP_CATALOG_LAYOUT_IDENTIFIER;
import static software.coley.bentofx.persistence.testfixtures.codec.state.SampleBentoStateFactory.createBentoStates;

/**
 * Coverage for the two operations that reach a stored layout without reading the
 * scene graph: rewriting what a layout is called, and the catalog of groups.
 *
 * <p>These need storage that keeps what was written to it, which
 * {@code TestLayoutStorageProvider} deliberately does not - it hands out a fresh,
 * empty storage per call so that a test can watch what a component asks for. The
 * double below keeps bytes per layout identifier instead, so a read after a write
 * returns what the write put there, which is the whole point of what is under
 * test.</p>
 *
 * @author Phil Bryant
 */
class DefaultDockingLayoutPersistenceProviderNamingTest {

    private static final String LAYOUT_IDENTIFIER = "compact";

    @Test
    void updateStoredLayoutNamingRewritesTheNamingAndKeepsTheState()
            throws BentoStateException {
        final PersistingStorageProvider storageProvider =
                new PersistingStorageProvider();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storageProvider);
        final List<BentoState> original = createBentoStates();

        storageProvider.store(
                LAYOUT_IDENTIFIER,
                new PersistableLayout("Wide", original)
        );

        final boolean rewritten = provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Renamed", "Debugging")
        );

        assertThat(rewritten)
                .describedAs("updateStoredLayoutNaming for a stored layout")
                .isTrue();

        final PersistableLayout stored =
                storageProvider.read(LAYOUT_IDENTIFIER);

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
        final PersistingStorageProvider storageProvider =
                new PersistingStorageProvider();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storageProvider);

        storageProvider.store(
                LAYOUT_IDENTIFIER,
                new PersistableLayout("Wide", createBentoStates())
        );
        storageProvider.streamsOpened.clear();

        provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Renamed", null)
        );

        assertThat(storageProvider.streamsOpened)
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
        final PersistingStorageProvider storageProvider =
                new PersistingStorageProvider();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storageProvider);

        storageProvider.store(
                LAYOUT_IDENTIFIER,
                new PersistableLayout(
                        "Wide",
                        createBentoStates(),
                        "Debugging",
                        List.of()
                )
        );

        provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Wide", null)
        );

        assertThat(storageProvider.read(LAYOUT_IDENTIFIER).group())
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
        final PersistingStorageProvider storageProvider =
                new PersistingStorageProvider();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storageProvider);

        final boolean rewritten = provider.updateStoredLayoutNaming(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
                        .withNaming("Renamed", null)
        );

        assertThat(rewritten)
                .describedAs("updateStoredLayoutNaming for a layout not stored")
                .isFalse();
        assertThat(storageProvider.storedIdentifiers())
                .describedAs("layouts in storage afterwards")
                .isEmpty();
    }

    @Test
    void groupCatalogRoundTripsThroughItsReservedEntry()
            throws BentoStateException {
        final PersistingStorageProvider storageProvider =
                new PersistingStorageProvider();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storageProvider);
        final LayoutPersistenceProfile profile =
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER);

        provider.setStoredGroups(profile, List.of("Debugging", "Presentation"));

        assertThat(provider.getStoredGroups(profile))
                .describedAs("group catalog read back")
                .containsExactly("Debugging", "Presentation");
        assertThat(storageProvider.storedIdentifiers())
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
                providerFor(new PersistingStorageProvider());

        assertThat(provider.getStoredGroups(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER)
        ))
                .describedAs("group catalog that was never written")
                .isEmpty();
    }

    @Test
    void emptyingTheCatalogLeavesNoGroups() throws BentoStateException {
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(new PersistingStorageProvider());
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
        final PersistingStorageProvider storageProvider =
                new PersistingStorageProvider();
        final DefaultDockingLayoutPersistenceProvider provider =
                providerFor(storageProvider);

        storageProvider.store(
                LAYOUT_IDENTIFIER,
                new PersistableLayout(
                        "TCP/IP Debug",
                        createBentoStates(),
                        "Debugging",
                        List.of()
                )
        );

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
     * {@return a provider wired to the supplied storage and to one codec that
     * keeps what it encoded.}
     *
     * @param storageProvider the storage to write through.
     */
    private static DefaultDockingLayoutPersistenceProvider providerFor(
            final PersistingStorageProvider storageProvider
    ) {
        return new DefaultDockingLayoutPersistenceProvider(
                List.of(storageProvider.codecProvider),
                List.of(storageProvider)
        );
    }

    /**
     * A {@link LayoutStorageProvider} that keeps what was written to it, keyed by
     * layout identifier.
     *
     * <p>Each call still hands out a fresh {@link LayoutStorage}, as the contract
     * requires, but every one of them reads and writes the same map - which is
     * what a real file or a real row does and what a read-after-write needs.</p>
     */
    private static final class PersistingStorageProvider
            implements LayoutStorageProvider {

        private final Map<String, byte[]> storedBytes = new LinkedHashMap<>();
        private final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        private final LayoutCodecProvider codecProvider =
                new SingleCodecProvider(codec);

        /**
         * Every stream opened, in order, so that a test can assert a read
         * happened before a write rather than only that both did.
         */
        private final List<String> streamsOpened = new ArrayList<>();

        @Override
        public String getIdentifier() {
            return "persisting";
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public LayoutStorage getLayoutStorage(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            return new LayoutStorage() {

                @Override
                public boolean exists() {
                    final byte[] bytes = storedBytes.get(layoutIdentifier);
                    return bytes != null && bytes.length > 0;
                }

                @Override
                public OutputStream openOutputStream() {
                    streamsOpened.add("write:" + layoutIdentifier);

                    final ByteArrayOutputStream buffer =
                            new ByteArrayOutputStream();

                    // Written back on close, so a stream opened and abandoned
                    // leaves what was there alone.
                    return new FilterOutputStream(buffer) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            storedBytes.put(
                                    layoutIdentifier,
                                    buffer.toByteArray()
                            );
                        }
                    };
                }

                @Override
                public InputStream openInputStream() {
                    streamsOpened.add("read:" + layoutIdentifier);

                    return new ByteArrayInputStream(
                            storedBytes.getOrDefault(
                                    layoutIdentifier,
                                    new byte[0]
                            )
                    );
                }
            };
        }

        @Override
        public List<String> getLayoutIdentifiers(final String codecIdentifier) {
            return new ArrayList<>(storedBytes.keySet());
        }

        @Override
        public boolean isLayoutStored(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            final byte[] bytes = storedBytes.get(layoutIdentifier);
            return bytes != null && bytes.length > 0;
        }

        /**
         * Puts a layout in storage the way an earlier save would have left it.
         *
         * @param layoutIdentifier addresses the layout.
         * @param layout what to store.
         */
        private void store(
                final String layoutIdentifier,
                final PersistableLayout layout
        ) throws BentoStateException {
            try (final OutputStream outputStream =
                         getLayoutStorage(layoutIdentifier, codec.getIdentifier())
                                 .openOutputStream()) {

                codec.encode(layout, outputStream);
            } catch (final IOException e) {
                throw new BentoStateException("Could not store the layout.", e);
            }
        }

        /**
         * {@return the layout in storage, decoded.}
         *
         * @param layoutIdentifier addresses the layout.
         */
        private PersistableLayout read(final String layoutIdentifier)
                throws BentoStateException {
            try (final InputStream inputStream =
                         getLayoutStorage(layoutIdentifier, codec.getIdentifier())
                                 .openInputStream()) {

                return codec.decode(inputStream);
            } catch (final IOException e) {
                throw new BentoStateException("Could not read the layout.", e);
            }
        }

        /**
         * {@return what storage holds, in the order it was written.}
         */
        private List<String> storedIdentifiers() {
            return new ArrayList<>(storedBytes.keySet());
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
