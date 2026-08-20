package software.coley.bentofx.persistence.core.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;
import software.coley.bentofx.persistence.core.api.*;
import software.coley.bentofx.persistence.core.api.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.core.api.provider.*;
import software.coley.bentofx.persistence.core.impl.provider.DefaultBentoProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives {@link LayoutsMenu} through its real menu items and the modal
 * dialogs they raise.
 *
 * <p>{@code activeCustomLayoutProfile} is private to the menu, so these tests
 * cannot set or read it directly; every test instead drives it the only way
 * anything outside the menu can - through {@link #makeActiveLayout()}, which
 * fires a real, successful restore - and asserts on what the menu makes
 * observable from the state it produces: a restored item's checkmark, and
 * whether {@code Save changes}/{@code Rename} are enabled.</p>
 *
 * <p>A menu item's action handler that opens an {@link javafx.scene.control.Alert}
 * or {@link javafx.scene.control.TextInputDialog} calls {@code showAndWait()},
 * which enters a nested JavaFX event loop that blocks the calling frame until
 * the dialog is dismissed. Firing such an item from inside a plain
 * {@code robot.interact(...)} would therefore never return. Instead, {@link #fire}
 * queues each dialog's dismissal with {@link Platform#runLater} <em>before</em>
 * firing the item that opens the first one, all from a single outer task: a
 * dialog's nested loop still pumps the platform's task queue while it runs, so
 * each queued dismissal runs - in order, one dialog at a time - and closes its
 * dialog before {@code showAndWait()} (and eventually {@code item.fire()})
 * returns. A disabled item's {@code fire()} does nothing at all, which is why
 * every test that needs {@code Save changes} or {@code Rename} enabled goes
 * through {@link #makeActiveLayout()} first.</p>
 *
 * @author Phil Bryant
 */
@ExtendWith(ApplicationExtension.class)
class LayoutsMenuFT {

    private static final String OTHER_LAYOUT_ID = "other-layout";
    private static final String ACTIVE_LAYOUT_ID = "active-layout";
    private static final String ACTIVE_LAYOUT_DISPLAY_NAME = "Active Layout";
    private static final String NEW_LAYOUT_DISPLAY_NAME = "My New Layout";
    private static final String NEW_LAYOUT_IDENTIFIER = "my-new-layout";

    private Stage owner;
    private RecordingPersistenceProvider persistenceProvider;
    private RecordingRestorable restorable;
    private LayoutsMenu menu;

    @BeforeEach
    void setUp(final FxRobot robot) {
        persistenceProvider = new RecordingPersistenceProvider();
        restorable = new RecordingRestorable(persistenceProvider);

        robot.interact(() -> {
            owner = new Stage();
            owner.setScene(new Scene(new Pane(), 200, 200));
            owner.show();
            menu = new LayoutsMenu(owner, restorable);
        });
    }

    @AfterEach
    void tearDown(final FxRobot robot) {
        robot.interact(() -> owner.hide());
    }

    @Test
    void defaultItemIsCheckedWhenNoCustomLayoutIsActive() {
        assertThat(topItems().get(0).getText())
                .describedAs("defaultItem.getText()")
                .startsWith("✓");
    }

    @Test
    void customMenuShowsAListFailedItemWhenStorageCannotBeListed() {
        persistenceProvider.listFails = true;
        repopulate();

        assertThat(restoreMenu().getItems())
                .describedAs("restoreMenu.getItems()")
                .hasSize(1);
        assertThat(restoreMenu().getItems().get(0).isDisable())
                .describedAs("restoreMenu list-failed item disabled")
                .isTrue();
        assertThat(deleteMenu().getItems())
                .describedAs("deleteMenu.getItems()")
                .hasSize(1);
    }

    @Test
    void customMenuShowsANoLayoutsItemWhenNothingIsStored() {
        repopulate();

        assertThat(restoreMenu().getItems())
                .describedAs("restoreMenu.getItems()")
                .hasSize(1);
        assertThat(restoreMenu().getItems().get(0).isDisable())
                .describedAs("restoreMenu no-layouts item disabled")
                .isTrue();
    }

    @Test
    void customMenuListsStoredLayoutsSortedByDisplayNameWithTheActiveOneChecked() {
        makeActiveLayout();
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(OTHER_LAYOUT_ID, "Zebra", null, null)
        );
        repopulate();

        assertThat(restoreMenu().getItems())
                .describedAs("restoreMenu.getItems()")
                .hasSize(2);
        assertThat(restoreMenu().getItems().get(0).getText())
                .describedAs("first (sorted) restore item")
                .startsWith("✓")
                .contains(ACTIVE_LAYOUT_DISPLAY_NAME);
        assertThat(restoreMenu().getItems().get(1).getText())
                .describedAs("second (sorted) restore item")
                .doesNotContain("✓")
                .contains("Zebra");
        assertThat(deleteMenu().getItems())
                .describedAs("deleteMenu.getItems()")
                .hasSize(2);
    }

    @Test
    void restoreDefaultLayoutSwitchesAndClearsTheActiveProfile() {
        makeActiveLayout();
        restorable.switchSucceeds = true;

        fire(topItems().get(0));
        repopulate();

        assertThat(topItems().get(0).getText())
                .describedAs("defaultItem.getText() after restoring the default layout")
                .startsWith("✓");
    }

    @Test
    void restoreDefaultLayoutShowsAnErrorAndLeavesTheActiveProfileWhenSwitchingFails() {
        makeActiveLayout();
        restorable.switchSucceeds = false;

        fire(topItems().get(0), dismiss(ButtonType.OK));
        repopulate();

        assertThat(topItems().get(0).getText())
                .describedAs("defaultItem.getText() after a failed default-layout restore")
                .doesNotContain("✓");
        assertThat(restoreMenu().getItems().get(0).getText())
                .describedAs("active layout's restore item text after a failed default-layout restore")
                .startsWith("✓");
    }

    @Test
    void restoreStoredLayoutSwitchesAndSetsTheActiveProfile() {
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(OTHER_LAYOUT_ID, "Other", null, null)
        );
        restorable.switchSucceeds = true;
        repopulate();

        fire(restoreMenu().getItems().get(0));
        repopulate();

        assertThat(restoreMenu().getItems().get(0).getText())
                .describedAs("restore item text after restoring a stored layout")
                .startsWith("✓");
        assertThat(saveChangesItem().isDisable())
                .describedAs("saveChangesItem.isDisable() after restoring a stored layout")
                .isFalse();
    }

    @Test
    void restoreStoredLayoutShowsAnErrorAndLeavesTheActiveProfileWhenSwitchingFails() {
        makeActiveLayout();
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(OTHER_LAYOUT_ID, "Other", null, null)
        );
        restorable.switchSucceeds = false;
        repopulate();

        // Sorted by display name: "Active Layout" then "Other".
        fire(restoreMenu().getItems().get(1), dismiss(ButtonType.OK));
        repopulate();

        assertThat(restoreMenu().getItems().get(0).getText())
                .describedAs("active layout's restore item text after a failed restore of another layout")
                .startsWith("✓");
        assertThat(restoreMenu().getItems().get(1).getText())
                .describedAs("other layout's restore item text after a failed restore attempt")
                .doesNotContain("✓");
    }

    @Test
    void saveAsNewDoesNothingWhenTheDialogIsCancelled() {
        fire(topItems().get(2), dismiss(ButtonType.CANCEL));

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    /**
     * Covers three distinct reasons a typed name can be rejected before
     * anything is saved: blank, derives the framework's reserved identifier,
     * and derives a Windows reserved device name. Each name exercises a
     * different branch of the same validation but the same reaction, so one
     * parameterized test replaces what used to be three copies of it.
     */
    @ParameterizedTest(name = "rejects \"{0}\"")
    @ValueSource(strings = {"   ", "Session", "CON"})
    void saveAsNewShowsAProblemErrorForAnInvalidName(final String invalidName) {
        fire(
                topItems().get(2),
                typeAndDismiss(invalidName, ButtonType.OK),
                dismiss(ButtonType.OK)
        );

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    @Test
    void saveAsNewShowsAnErrorWhenCheckingExistenceFails() {
        persistenceProvider.isLayoutStoredFails = true;

        fire(
                topItems().get(2),
                typeAndDismiss(NEW_LAYOUT_DISPLAY_NAME, ButtonType.OK),
                dismiss(ButtonType.OK)
        );

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    @Test
    void saveAsNewWritesTheLayoutWhenNothingIsStoredUnderTheDerivedIdentifier() {
        fire(topItems().get(2), typeAndDismiss(NEW_LAYOUT_DISPLAY_NAME, ButtonType.OK));
        repopulate();

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .extracting(LayoutPersistenceProfile::layoutIdentifier)
                .containsExactly(NEW_LAYOUT_IDENTIFIER);
        assertThat(saveChangesItem().isDisable())
                .describedAs("saveChangesItem.isDisable() after saving a new layout")
                .isFalse();
    }

    @Test
    void saveAsNewDoesNothingWhenAlreadyStoredAndTheUserDeclinesToReplace() {
        persistenceProvider.storedIdentifiers.add(NEW_LAYOUT_IDENTIFIER);

        fire(
                topItems().get(2),
                typeAndDismiss(NEW_LAYOUT_DISPLAY_NAME, ButtonType.OK),
                dismiss(ButtonType.NO)
        );

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    @Test
    void saveAsNewWritesTheLayoutWhenAlreadyStoredAndTheUserConfirmsReplace() {
        persistenceProvider.storedIdentifiers.add(NEW_LAYOUT_IDENTIFIER);

        fire(
                topItems().get(2),
                typeAndDismiss(NEW_LAYOUT_DISPLAY_NAME, ButtonType.OK),
                dismiss(ButtonType.YES)
        );

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .extracting(LayoutPersistenceProfile::layoutIdentifier)
                .containsExactly(NEW_LAYOUT_IDENTIFIER);
    }

    @Test
    void saveChangesWritesTheActiveLayout() {
        makeActiveLayout();

        fire(saveChangesItem());

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .extracting(LayoutPersistenceProfile::layoutIdentifier)
                .containsExactly(ACTIVE_LAYOUT_ID);
    }

    @Test
    void saveChangesShowsAnErrorWhenTheWriteFails() {
        makeActiveLayout();
        persistenceProvider.saveFails = true;

        fire(saveChangesItem(), dismiss(ButtonType.OK));

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    @Test
    void renameDoesNothingWhenTheDialogIsCancelled() {
        makeActiveLayout();

        fire(renameItem(), dismiss(ButtonType.CANCEL));

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    @Test
    void renameShowsAnErrorForABlankName() {
        makeActiveLayout();

        fire(
                renameItem(),
                typeAndDismiss("   ", ButtonType.OK),
                dismiss(ButtonType.OK)
        );

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .isEmpty();
    }

    @Test
    void renameWritesTheLayoutUnderTheSameIdentifierWithTheNewDisplayName() {
        makeActiveLayout();

        fire(renameItem(), typeAndDismiss("Renamed Layout", ButtonType.OK));

        assertThat(persistenceProvider.savedProfiles)
                .describedAs("persistenceProvider.savedProfiles")
                .hasSize(1);
        assertThat(persistenceProvider.savedProfiles.get(0).layoutIdentifier())
                .describedAs("saved profile's identifier")
                .isEqualTo(ACTIVE_LAYOUT_ID);
        assertThat(persistenceProvider.savedProfiles.get(0).displayName())
                .describedAs("saved profile's display name")
                .isEqualTo("Renamed Layout");
    }

    @Test
    void deleteDoesNothingWhenTheUserDeclinesToConfirm() {
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(OTHER_LAYOUT_ID, "Other", null, null)
        );
        repopulate();

        fire(deleteMenu().getItems().get(0), dismiss(ButtonType.NO));

        assertThat(persistenceProvider.deletedProfiles)
                .describedAs("persistenceProvider.deletedProfiles")
                .isEmpty();
    }

    @Test
    void deleteClearsTheActiveProfileWhenTheDeletedLayoutWasActive() {
        makeActiveLayout();

        fire(deleteMenu().getItems().get(0), dismiss(ButtonType.YES));
        repopulate();

        assertThat(persistenceProvider.deletedProfiles)
                .describedAs("persistenceProvider.deletedProfiles")
                .extracting(LayoutPersistenceProfile::layoutIdentifier)
                .containsExactly(ACTIVE_LAYOUT_ID);
        assertThat(topItems().get(0).getText())
                .describedAs("defaultItem.getText() after deleting the active layout")
                .startsWith("✓");
    }

    @Test
    void deleteLeavesTheActiveProfileWhenTheDeletedLayoutWasNotActive() {
        makeActiveLayout();
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(OTHER_LAYOUT_ID, "Other", null, null)
        );
        repopulate();

        // Sorted by display name: "Active Layout" then "Other".
        fire(deleteMenu().getItems().get(1), dismiss(ButtonType.YES));
        repopulate();

        assertThat(persistenceProvider.deletedProfiles)
                .describedAs("persistenceProvider.deletedProfiles")
                .extracting(LayoutPersistenceProfile::layoutIdentifier)
                .containsExactly(OTHER_LAYOUT_ID);
        assertThat(restoreMenu().getItems().get(0).getText())
                .describedAs("active layout's restore item text after deleting an unrelated layout")
                .startsWith("✓");
    }

    @Test
    void deleteShowsAnErrorWhenTheDeleteFails() {
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(OTHER_LAYOUT_ID, "Other", null, null)
        );
        persistenceProvider.deleteFails = true;
        repopulate();

        fire(
                deleteMenu().getItems().get(0),
                dismiss(ButtonType.YES),
                dismiss(ButtonType.OK)
        );

        assertThat(persistenceProvider.deletedProfiles)
                .describedAs("persistenceProvider.deletedProfiles")
                .isEmpty();
    }

    // --- Helpers -----------------------------------------------------------

    /**
     * Stores and then successfully restores {@code ACTIVE_LAYOUT_ID}, so the
     * menu's own {@code activeCustomLayoutProfile} - unreachable directly from
     * a test - becomes that layout, the only way anything outside the menu
     * can put it in that state.
     */
    private void makeActiveLayout() {
        persistenceProvider.storedLayouts.add(
                LayoutPersistenceProfile.named(
                        ACTIVE_LAYOUT_ID, ACTIVE_LAYOUT_DISPLAY_NAME, null, null
                )
        );
        persistenceProvider.storedIdentifiers.add(ACTIVE_LAYOUT_ID);
        restorable.switchSucceeds = true;
        repopulate();

        fire(restoreMenu().getItems().get(0));
        repopulate();
    }

    private List<MenuItem> topItems() {
        return menu.getItems();
    }

    private Menu customMenu() {
        return (Menu) topItems().get(1);
    }

    private Menu restoreMenu() {
        return (Menu) customMenu().getItems().get(0);
    }

    private MenuItem saveChangesItem() {
        return customMenu().getItems().get(1);
    }

    private MenuItem renameItem() {
        return customMenu().getItems().get(2);
    }

    private Menu deleteMenu() {
        return (Menu) customMenu().getItems().get(3);
    }

    private void repopulate() {
        Platform.runLater(() -> menu.getOnShowing().handle(null));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Fires a menu item's action on the FX thread, dismissing any dialogs it
     * raises with the supplied steps, one dialog at a time and in order - see
     * the class documentation for why queuing them ahead of {@code item.fire()}
     * works.
     */
    private static void fire(final MenuItem item, final Runnable... dialogSteps) {
        Platform.runLater(() -> {
            for (final Runnable step : dialogSteps) {
                Platform.runLater(step);
            }
            item.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** A step that dismisses whatever dialog is currently showing with one button. */
    private static Runnable dismiss(final ButtonType buttonType) {
        return () -> fireDialogButton(findShowingDialogPane(), buttonType);
    }

    /** A step that types into the currently-showing text-input dialog, then dismisses it. */
    private static Runnable typeAndDismiss(final String text, final ButtonType buttonType) {
        return () -> {
            final DialogPane pane = findShowingDialogPane();
            ((TextField) pane.lookup(".text-field")).setText(text);
            fireDialogButton(pane, buttonType);
        };
    }

    private static void fireDialogButton(final DialogPane pane, final ButtonType buttonType) {
        ((Button) pane.lookupButton(buttonType)).fire();
    }

    private static DialogPane findShowingDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(scene -> scene != null && scene.getRoot() instanceof DialogPane)
                .map(scene -> (DialogPane) scene.getRoot())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No dialog is currently showing"));
    }

    /** {@link DockingLayoutRestorable} test double controlling every branch {@link LayoutsMenu} reacts to. */
    private static final class RecordingRestorable implements DockingLayoutRestorable {

        private final DockingLayoutPersistenceProvider persistenceProvider;
        private final BentoProvider bentoProvider = new DefaultBentoProvider();
        private final List<Supplier<DockingLayout>> switchCalls = new ArrayList<>();
        private boolean switchSucceeds = true;

        private RecordingRestorable(final DockingLayoutPersistenceProvider persistenceProvider) {
            this.persistenceProvider = persistenceProvider;
        }

        @Override
        public DockingLayout getDefaultDockingLayout() {
            return new DockingLayoutBuilder().build();
        }

        @Override
        public DockingLayout getDockingLayout(
                final LayoutPersistenceProfile layoutPersistenceProfile,
                final Supplier<DockingLayout> fallbackLayoutSupplier
        ) {
            return fallbackLayoutSupplier.get();
        }

        @Override
        public boolean switchToLayout(final Supplier<DockingLayout> dockingLayoutSupplier) {
            switchCalls.add(dockingLayoutSupplier);
            if (switchSucceeds) {
                @SuppressWarnings("unused")
                final DockingLayout unused = dockingLayoutSupplier.get();
            }
            return switchSucceeds;
        }

        @Override
        public DockingLayoutPersistenceProvider getPersistenceProvider() {
            return persistenceProvider;
        }

        @Override
        public BentoProvider getBentoProvider() {
            return bentoProvider;
        }
    }

    /** {@link DockingLayoutPersistenceProvider} test double; only the members {@link LayoutsMenu} calls do anything. */
    private static final class RecordingPersistenceProvider implements DockingLayoutPersistenceProvider {

        private final List<LayoutPersistenceProfile> storedLayouts = new ArrayList<>();
        private final Set<String> storedIdentifiers = new HashSet<>();
        private final List<LayoutPersistenceProfile> savedProfiles = new ArrayList<>();
        private final List<LayoutPersistenceProfile> deletedProfiles = new ArrayList<>();
        private boolean listFails;
        private boolean isLayoutStoredFails;
        private boolean saveFails;
        private boolean deleteFails;

        @Override
        public LayoutSaver getLayoutSaver(
                final LayoutPersistenceProfile layoutPersistenceProfile,
                final BentoProvider bentoProvider
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LayoutRestorer getLayoutRestorer(
                final LayoutPersistenceProfile layoutPersistenceProfile,
                final BentoProvider bentoProvider,
                final DockableStateProvider dockableStateProvider,
                final @Nullable StageIconImageProvider stageIconImageProvider,
                final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveLayout(
                final LayoutPersistenceProfile layoutPersistenceProfile,
                final BentoProvider bentoProvider
        ) throws BentoStateException {
            if (saveFails) {
                throw new BentoStateException("save failed");
            }
            savedProfiles.add(layoutPersistenceProfile);
        }

        @Override
        public List<String> getStoredLayoutIdentifiers(
                final LayoutPersistenceProfile layoutPersistenceProfile
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<LayoutPersistenceProfile> getStoredLayouts(
                final LayoutPersistenceProfile layoutPersistenceProfile
        ) throws BentoStateException {
            if (listFails) {
                throw new BentoStateException("list failed");
            }
            return List.copyOf(storedLayouts);
        }

        @Override
        public boolean isLayoutStored(
                final LayoutPersistenceProfile layoutPersistenceProfile
        ) throws BentoStateException {
            if (isLayoutStoredFails) {
                throw new BentoStateException("isLayoutStored failed");
            }
            return storedIdentifiers.contains(layoutPersistenceProfile.layoutIdentifier());
        }

        @Override
        public boolean deleteLayout(
                final LayoutPersistenceProfile layoutPersistenceProfile
        ) throws BentoStateException {
            if (deleteFails) {
                throw new BentoStateException("delete failed");
            }
            deletedProfiles.add(layoutPersistenceProfile);
            return true;
        }
    }
}
