package software.coley.boxfx.demo.persistence;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.persistence.core.api.BentoStateException;
import software.coley.bentofx.persistence.core.api.DockingLayout;
import software.coley.bentofx.persistence.core.api.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.core.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.core.api.provider.BentoProvider;
import software.coley.bentofx.persistence.core.api.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifierProblem;
import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER;

/**
 * A {@code Layouts} menu for any application that keeps its docking layout in
 * BentoFX persistence: switch between the layout the application builds for
 * itself and the layouts a user has named, and save, rename, and delete those.
 *
 * <p>Nothing here is particular to one application. What an application
 * supplies is a {@link DockingLayoutRestorable} to act on, the window dialogs
 * belong to, and the two providers that reach storage. Drop the menu wherever
 * it belongs - a menu bar, a {@code Window} menu, a context menu:</p>
 *
 * <pre>{@code
 * windowMenu.getItems().add(new LayoutsMenu(
 *         application, stage, persistenceProvider, bentoProvider
 * ));
 * }</pre>
 *
 * <p>This menu owns which named layout is showing, because its own items are
 * the only thing that changes it. An application that switches layouts by some
 * other route as well would need a say in that, which none does today.</p>
 *
 * @author Phil Bryant
 */
public class LayoutsMenu extends Menu {

	private static final Logger logger =
			LoggerFactory.getLogger(LayoutsMenu.class);

	private static final String CHECK_MARK = "✓";

	private final DockingLayoutRestorable dockingLayoutRestorable;

	/** The window the dialogs raised from this menu belong to. */
	private final Window owner;

	private final DockingLayoutPersistenceProvider persistenceProvider;

	private final BentoProvider bentoProvider;

	/**
	 * The custom layout showing now, or {@code null} when the default layout
	 * is showing.
	 *
	 * <p>Saving changes and renaming both write the live containers, so both
	 * are offered only for the layout on screen. This is what they write to,
	 * and what the check mark in the menu marks.</p>
	 */
	private @Nullable LayoutPersistenceProfile activeCustomLayoutProfile;

	/**
	 * @param dockingLayoutRestorable the application whose docking layout these
	 * items switch.
	 * @param owner the window the dialogs these items raise belong to.
	 * @param persistenceProvider reads, writes, and lists stored layouts.
	 * @param bentoProvider supplies what a save captures.
	 */
	public LayoutsMenu(
			final DockingLayoutRestorable dockingLayoutRestorable,
			final Window owner,
			final DockingLayoutPersistenceProvider persistenceProvider,
			final BentoProvider bentoProvider
	) {
		super("_Layouts");

		this.dockingLayoutRestorable = dockingLayoutRestorable;
		this.owner = owner;
		this.persistenceProvider = persistenceProvider;
		this.bentoProvider = bentoProvider;

		// Rebuilt every time it opens, and once now so that it has something to
		// open with - a menu with no items never opens, and so would never
		// reach the handler that fills it. Rebuilding is what keeps the check
		// marks and both layout lists agreeing with each other and with what is
		// stored, which changes while the application runs.
		setOnShowing(event -> populate());
		populate();
	}

	/**
	 * Fills this menu from the layout showing now and what is in storage.
	 */
	private void populate() {
		// Checked whenever no saved layout is the one showing, which includes
		// the session layout the application starts with: that is the layout a
		// user has been arranging without naming, and this menu does not
		// remember which named layout it grew out of.
		final MenuItem defaultItem = new MenuItem(
				markedText("_Default", activeCustomLayoutProfile == null)
		);
		defaultItem.setOnAction(event -> restoreDefaultLayout());

		final Menu customMenu = new Menu(
				markedText("_Custom", activeCustomLayoutProfile != null)
		);
		populateCustomMenu(customMenu);

		final MenuItem saveAsNewItem =
				new MenuItem("Save Current Layout as _New...");
		saveAsNewItem.setOnAction(event -> saveCurrentLayoutAsNew());

		getItems().setAll(defaultItem, customMenu, saveAsNewItem);
	}

	/**
	 * Fills the {@code Custom} menu.
	 *
	 * <p>Saving changes and renaming are offered only for the layout showing
	 * now, because both write the containers on screen; there is no way to
	 * rewrite a layout that is only in storage.</p>
	 *
	 * @param customMenu the menu to fill.
	 */
	private void populateCustomMenu(final Menu customMenu) {
		final Menu restoreMenu = new Menu("_Restore");
		final Menu deleteMenu = new Menu("_Delete");

		final MenuItem saveChangesItem = new MenuItem("_Save Changes");
		saveChangesItem.setDisable(activeCustomLayoutProfile == null);
		saveChangesItem.setOnAction(event -> saveChangesToActiveLayout());

		final MenuItem renameItem = new MenuItem("R_ename...");
		renameItem.setDisable(activeCustomLayoutProfile == null);
		renameItem.setOnAction(event -> renameActiveLayout());

		// One reading of storage fills both lists. Listing with display names
		// decodes every stored layout, so asking twice would decode twice.
		final Optional<List<LayoutPersistenceProfile>> storedLayouts =
				findStoredCustomLayouts();

		if (storedLayouts.isEmpty()) {
			addDisabledItem(restoreMenu, "Could not list saved layouts");
			addDisabledItem(deleteMenu, "Could not list saved layouts");
		} else if (storedLayouts.get().isEmpty()) {
			addDisabledItem(restoreMenu, "No saved layouts");
			addDisabledItem(deleteMenu, "No saved layouts");
		} else {
			for (final LayoutPersistenceProfile storedLayout :
					storedLayouts.get()) {

				final MenuItem restoreItem = new MenuItem(markedText(
						getLayoutLabel(storedLayout),
						isActiveLayout(storedLayout)
				));
				restoreItem.setOnAction(
						event -> restoreStoredLayout(storedLayout)
				);
				restoreMenu.getItems().add(restoreItem);

				final MenuItem deleteItem =
						new MenuItem(getLayoutLabel(storedLayout));
				deleteItem.setOnAction(
						event -> deleteStoredLayout(storedLayout)
				);
				deleteMenu.getItems().add(deleteItem);
			}
		}

		customMenu.getItems().setAll(
				restoreMenu,
				saveChangesItem,
				renameItem,
				deleteMenu
		);
	}

	/**
	 * {@return the layouts a user may act on, or an empty {@link Optional} when
	 * storage could not be read.}
	 *
	 * <p>The profile this asks with selects only the codec and the storage; its
	 * layout identifier is not used, so the session profile serves rather than
	 * a second one being invented. The session layout is dropped from the
	 * result, because it is the layout the application saves to on its own and
	 * not one a user restores or deletes by name.</p>
	 */
	private Optional<List<LayoutPersistenceProfile>> findStoredCustomLayouts() {
		try {
			return Optional.of(
					persistenceProvider.getStoredLayouts(
							LayoutPersistenceProfile.of(
									SESSION_LAYOUT_IDENTIFIER
							)
					).stream()
							.filter(LayoutsMenu::isUserLayout)
							.sorted(Comparator.comparing(
									LayoutsMenu::getLayoutLabel,
									String.CASE_INSENSITIVE_ORDER
							))
							.toList()
			);
		} catch (final BentoStateException e) {
			logger.warn("Could not list the stored docking layouts.", e);
			return Optional.empty();
		}
	}

	/**
	 * {@return {@code true} when the layout is one a user named; otherwise,
	 * {@code false}.}
	 *
	 * @param layoutPersistenceProfile identifies the layout to test.
	 */
	private static boolean isUserLayout(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) {
		return !LayoutIdentifiers.isReserved(
				layoutPersistenceProfile.layoutIdentifier()
		);
	}

	/**
	 * Switches to the layout the application builds for itself.
	 */
	private void restoreDefaultLayout() {
		if (switchToLayout(
				dockingLayoutRestorable::getDefaultDockingLayout
		)) {
			activeCustomLayoutProfile = null;
		}
	}

	/**
	 * Switches to a layout in storage.
	 *
	 * @param layoutPersistenceProfile identifies the layout to switch to. Comes
	 * from the catalog, so it already carries the codec and storage it was
	 * listed with.
	 */
	private void restoreStoredLayout(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) {
		final boolean isSwitched = switchToLayout(
				() -> dockingLayoutRestorable.getDockingLayout(
						layoutPersistenceProfile,

						// An empty layout rather than the default one. A read
						// that fails should leave what is on screen alone;
						// falling back here would answer "restore this" with a
						// different layout.
						() -> new DockingLayoutBuilder().build()
				)
		);

		if (isSwitched) {
			activeCustomLayoutProfile = layoutPersistenceProfile;
		}
	}

	/**
	 * {@return {@code true} when the layout was applied; otherwise,
	 * {@code false}, having told the user that what is on screen was left as it
	 * is.}
	 *
	 * @param dockingLayoutSupplier supplies the layout to switch to.
	 */
	private boolean switchToLayout(
			final Supplier<DockingLayout> dockingLayoutSupplier
	) {
		if (dockingLayoutRestorable.switchToLayout(dockingLayoutSupplier)) {
			return true;
		}

		showLayoutError(
				"Could not restore that layout.",
				"The layout could not be read, or does not fit this "
						+ "application. The layout showing now has been left "
						+ "as it is."
		);
		return false;
	}

	/**
	 * Asks for a name and saves the layout showing now under it.
	 */
	private void saveCurrentLayoutAsNew() {
		final Optional<String> displayName = findLayoutName(
				"Save Current Layout as New",
				"Name for this layout:",
				""
		);

		if (displayName.isEmpty()) {
			return;
		}

		final String layoutIdentifier =
				LayoutNames.toIdentifier(displayName.get());

		// The name a user types is not an identifier, and deriving one is this
		// menu's step rather than the framework's. What the framework does is
		// say why a derived identifier will not do, in a sentence worth showing
		// as it is.
		final Optional<LayoutIdentifierProblem> problem =
				LayoutIdentifiers.findUserLayoutProblem(layoutIdentifier);

		if (problem.isPresent()) {
			showLayoutError(
					"Cannot save a layout named \"" + displayName.get() + "\".",
					problem.get().message()
			);
			return;
		}

		final LayoutPersistenceProfile newLayout = newProfile(
				layoutIdentifier,
				displayName.get()
		);

		final boolean isAlreadyStored;

		try {
			isAlreadyStored = persistenceProvider.isLayoutStored(newLayout);
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not tell whether the layout '{}' is stored.",
					layoutIdentifier,
					e
			);
			showLayoutError("Could not save the layout.", e.getMessage());
			return;
		}

		// Two names can derive one identifier, so this catches a collision the
		// user cannot see coming as well as the same name typed twice.
		if (isAlreadyStored && !confirmLayoutAction(
				"Replace the layout stored as \"" + layoutIdentifier + "\"?",
				"A layout is already stored under that name. Replacing it "
						+ "cannot be undone."
		)) {
			return;
		}

		writeLayout(newLayout);
	}

	/**
	 * Saves the layout showing now over the stored layout it came from.
	 */
	private void saveChangesToActiveLayout() {
		final LayoutPersistenceProfile activeLayout = activeCustomLayoutProfile;

		if (activeLayout == null) {
			return;
		}

		writeLayout(activeLayout);
	}

	/**
	 * Asks for a new name for the layout showing now and stores it under that
	 * name.
	 *
	 * <p>The layout identifier does not change, so the layout keeps the place
	 * in storage it already had - a display name is stored with a layout, not
	 * what the layout is addressed by.</p>
	 *
	 * <p>A rename is a save as well, not only a relabelling: a save reads the
	 * live containers, and nothing writes the stored metadata on its own, so
	 * the arrangement on screen goes to storage along with the new name. That
	 * is why this is offered only for the layout showing now - there is no way
	 * to rename a layout without also rewriting it.</p>
	 */
	private void renameActiveLayout() {
		final LayoutPersistenceProfile activeLayout = activeCustomLayoutProfile;

		if (activeLayout == null) {
			return;
		}

		final Optional<String> displayName = findLayoutName(
				"Rename Layout",
				"New name for this layout:",
				getLayoutLabel(activeLayout)
		);

		if (displayName.isEmpty()) {
			return;
		}

		if (displayName.get().isBlank()) {
			showLayoutError(
					"Cannot rename a layout to a blank name.",
					"Type a name for the layout, or cancel to keep the one it "
							+ "has."
			);
			return;
		}

		writeLayout(newProfile(
				activeLayout.layoutIdentifier(),
				displayName.get()
		));
	}

	/**
	 * Removes a layout from storage.
	 *
	 * <p>The layout on screen is not changed, even when it is the one removed;
	 * what changes is that there is no longer anywhere to save it back to.</p>
	 *
	 * @param layoutPersistenceProfile identifies the layout to remove.
	 */
	private void deleteStoredLayout(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) {
		final String layoutLabel = getLayoutLabel(layoutPersistenceProfile);

		if (!confirmLayoutAction(
				"Delete the layout \"" + layoutLabel + "\"?",
				"The stored layout is removed. The layout showing now is not "
						+ "changed."
		)) {
			return;
		}

		try {
			persistenceProvider.deleteLayout(layoutPersistenceProfile);
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not delete the docking layout '{}'.",
					layoutPersistenceProfile.layoutIdentifier(),
					e
			);
			showLayoutError("Could not delete the layout.", e.getMessage());
			return;
		}

		if (isActiveLayout(layoutPersistenceProfile)) {
			activeCustomLayoutProfile = null;
		}
	}

	/**
	 * Writes the layout showing now to storage and makes it the active layout.
	 *
	 * @param layoutPersistenceProfile names the layout to write.
	 */
	private void writeLayout(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) {
		try {
			// A one-shot write rather than through the running saver: this is a
			// layout of its own, not the session layout auto-save keeps up to
			// date.
			persistenceProvider.saveLayout(
					layoutPersistenceProfile,
					bentoProvider
			);
			activeCustomLayoutProfile = layoutPersistenceProfile;
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not save the docking layout as '{}'.",
					layoutPersistenceProfile.layoutIdentifier(),
					e
			);
			showLayoutError("Could not save the layout.", e.getMessage());
		}
	}

	/**
	 * {@return a profile naming a layout of the application's own, leaving the
	 * codec and the storage to the framework's selection.}
	 *
	 * @param layoutIdentifier addresses the layout in storage.
	 * @param displayName the name to store with the layout.
	 */
	private static LayoutPersistenceProfile newProfile(
			final String layoutIdentifier,
			final String displayName
	) {
		return LayoutPersistenceProfile.named(
				layoutIdentifier,
				displayName,
				null,
				null
		);
	}

	/**
	 * {@return what to call the layout in a menu.}
	 *
	 * <p>A layout stored before display names were kept has none, and shows the
	 * identifier it is addressed by rather than a name invented for it.</p>
	 *
	 * @param layoutPersistenceProfile identifies the layout to label.
	 */
	private static String getLayoutLabel(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) {
		return layoutPersistenceProfile.findDisplayName()
				.orElseGet(layoutPersistenceProfile::layoutIdentifier);
	}

	/**
	 * {@return {@code true} when the supplied layout is the one showing now;
	 * otherwise, {@code false}.}
	 *
	 * @param layoutPersistenceProfile identifies the layout to test.
	 */
	private boolean isActiveLayout(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) {
		final LayoutPersistenceProfile activeLayout = activeCustomLayoutProfile;

		return activeLayout != null && activeLayout.layoutIdentifier()
				.equals(layoutPersistenceProfile.layoutIdentifier());
	}

	/**
	 * {@return what a menu item says, marked when it is the one in effect.}
	 *
	 * <p>The mark goes in the item's own text rather than into a {@code Label}
	 * set as the item's graphic. A theme styles the text fill of a menu item's
	 * own label for each state it can be in, and a graphic is not that label,
	 * so a mark put there came out in a fill that only stood out from the
	 * background while the item was under the pointer.</p>
	 *
	 * <p>Nothing at all when unmarked, rather than a blank standing in for the
	 * mark. The graphic slot this replaces was a fixed width, so a stand-in
	 * lined the items up in a column; a leading space is not the width of the
	 * mark, so it only shifted the unmarked items instead.</p>
	 *
	 * @param text what the item says.
	 * @param isChecked whether the item is the one in effect.
	 */
	private static String markedText(
			final String text,
			final boolean isChecked
	) {
		return isChecked ? CHECK_MARK + " " + text : text;
	}

	/**
	 * Adds one disabled item to a menu, so that it has something to show.
	 *
	 * <p>An empty menu opens as an empty popup, which reads as a fault rather
	 * than as an answer.</p>
	 *
	 * @param menu the menu to add to.
	 * @param text what the item says.
	 */
	private static void addDisabledItem(final Menu menu, final String text) {
		final MenuItem item = new MenuItem(text);
		item.setDisable(true);
		menu.getItems().add(item);
	}

	/**
	 * {@return the name the user typed, trimmed, or an empty {@link Optional}
	 * when the dialog was cancelled.}
	 *
	 * @param title the dialog's title.
	 * @param prompt what to ask for.
	 * @param initialName what the field starts with.
	 */
	private Optional<String> findLayoutName(
			final String title,
			final String prompt,
			final String initialName
	) {
		final TextInputDialog dialog = new TextInputDialog(initialName);

		dialog.initOwner(owner);
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(prompt);

		return dialog.showAndWait().map(String::trim);
	}

	/**
	 * Tells the user that something about a layout could not be done.
	 *
	 * @param header what could not be done.
	 * @param content why, in whatever detail is available.
	 */
	private void showLayoutError(
			final String header,
			final @Nullable String content
	) {
		final Alert alert = new Alert(Alert.AlertType.ERROR);

		alert.initOwner(owner);
		alert.setTitle("Docking Layouts");
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * {@return {@code true} when the user agreed; otherwise, {@code false}.}
	 *
	 * <p>Defaults to no, so that dismissing the dialog does not carry out
	 * something that cannot be undone.</p>
	 *
	 * @param header what is being asked.
	 * @param content what it means.
	 */
	private boolean confirmLayoutAction(
			final String header,
			final String content
	) {
		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

		alert.initOwner(owner);
		alert.setTitle("Docking Layouts");
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
	}
}
