package software.coley.bentofx.persistence.core.ui;

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
import software.coley.bentofx.persistence.core.api.provider.DockingLayoutRestorable;
import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifierProblem;
import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers;
import software.coley.bentofx.persistence.core.api.storage.LayoutNames;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER;

/**
 * A {@code Layouts} menu for any application that keeps its docking layout in
 * BentoFX persistence: switch between the layout the application builds for
 * itself and the layouts a user has named, and save, rename, and delete those.
 *
 * <p>Nothing here is particular to one application. What an application
 * supplies is a {@link DockingLayoutRestorable} to act on and the window
 * dialogs belong to; the providers that reach storage come from the
 * restorable, which cannot be implemented without them anyway. Drop the menu
 * wherever it belongs - a menu bar, a {@code Window} menu, a context menu:</p>
 *
 * <pre>{@code
 * windowMenu.getItems().add(new LayoutsMenu(application, stage));
 * }</pre>
 *
 * <p>This menu owns which named layout is showing, because its own items are
 * the only thing that changes it. An application that switches layouts by some
 * other route as well would need a say in that, which none does today.</p>
 *
 * <p>Every word a user reads comes from a {@link ResourceBundle}, so an
 * application in another language either drops a translation beside the one
 * this module provides or hands over a bundle of its own. See
 * {@code LayoutsMenu.properties}.</p>
 *
 * @author Phil Bryant
 */
public class LayoutsMenu extends Menu {

	private static final Logger logger =
			LoggerFactory.getLogger(LayoutsMenu.class);

	private static final String CHECK_MARK = "✓";

	/**
	 * The bundle this menu reads when an application does not supply one.
	 *
	 * <p>Resolved against this module, which is where its own
	 * {@code LayoutsMenu.properties} lives. A bundle in the application's own
	 * module cannot be found by this name - resources in a named module are not
	 * visible to another - which is why the way to substitute text is to pass a
	 * {@link ResourceBundle} rather than to shadow this one.</p>
	 */
	private static final String BUNDLE_BASE_NAME =
			"software.coley.bentofx.persistence.core.ui.LayoutsMenu";

	private final DockingLayoutRestorable dockingLayoutRestorable;

	/** The window the dialogs raised from this menu belong to. */
	private final Window owner;

	/** Every word a user reads from this menu. */
	private final ResourceBundle texts;

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
	 * Builds a menu that reads the text this framework provides, in the
	 * default locale.
	 *
	 * @param owner the window the dialogs these items raise belong to.
	 * @param dockingLayoutRestorable the application whose docking layout these
	 * items switch, and whose providers they read and write it through.
	 */
	public LayoutsMenu(
			final Window owner,
			final DockingLayoutRestorable dockingLayoutRestorable
	) {
		this(
				owner,
				dockingLayoutRestorable,
				ResourceBundle.getBundle(BUNDLE_BASE_NAME)
		);
	}

	/**
	 * Builds a menu that reads the supplied text.
	 *
	 * <p>For an application that keeps its own wording, or that supports a
	 * language no translation here covers. The bundle has to carry
	 * every key in {@code LayoutsMenu.properties}: a missing one raises
	 * {@link java.util.MissingResourceException} when the item that needs it is
	 * built, which is the first time the menu opens rather than at
	 * construction.</p>
	 *
	 * @param owner the window the dialogs these items raise belong to.
	 * @param dockingLayoutRestorable the application whose docking layout these
	 * items switch, and whose providers they read and write it through.
	 * @param texts every word a user reads from this menu.
	 */
	public LayoutsMenu(
			final Window owner,
			final DockingLayoutRestorable dockingLayoutRestorable,
			final ResourceBundle texts
	) {
		super(texts.getString("menu.layouts"));

		this.dockingLayoutRestorable = dockingLayoutRestorable;
		this.owner = owner;
		this.texts = texts;

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
				markedText(
						text("item.default"),
						activeCustomLayoutProfile == null
				)
		);
		defaultItem.setOnAction(event -> restoreDefaultLayout());

		final Menu customMenu = new Menu(
				markedText(
						text("menu.custom"),
						activeCustomLayoutProfile != null
				)
		);
		populateCustomMenu(customMenu);

		final MenuItem saveAsNewItem =
				new MenuItem(text("item.saveAsNew"));
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
		final Menu restoreMenu = new Menu(text("menu.restore"));
		final Menu deleteMenu = new Menu(text("menu.delete"));

		final MenuItem saveChangesItem = new MenuItem(text("item.saveChanges"));
		saveChangesItem.setDisable(activeCustomLayoutProfile == null);
		saveChangesItem.setOnAction(event -> saveChangesToActiveLayout());

		final MenuItem renameItem = new MenuItem(text("item.rename"));
		renameItem.setDisable(activeCustomLayoutProfile == null);
		renameItem.setOnAction(event -> renameActiveLayout());

		// One reading of storage fills both lists. Listing with display names
		// decodes every stored layout, so asking twice would decode twice.
		final Optional<List<LayoutPersistenceProfile>> storedLayouts =
				findStoredCustomLayouts();

		if (storedLayouts.isEmpty()) {
			addDisabledItem(restoreMenu, text("item.listFailed"));
			addDisabledItem(deleteMenu, text("item.listFailed"));
		} else if (storedLayouts.get().isEmpty()) {
			addDisabledItem(restoreMenu, text("item.noLayouts"));
			addDisabledItem(deleteMenu, text("item.noLayouts"));
		} else {
			for (final LayoutPersistenceProfile storedLayout :
					storedLayouts.get()) {

				final MenuItem restoreItem = layoutItem(markedText(
						getLayoutLabel(storedLayout),
						isActiveLayout(storedLayout)
				));
				restoreItem.setOnAction(
						event -> restoreStoredLayout(storedLayout)
				);
				restoreMenu.getItems().add(restoreItem);

				final MenuItem deleteItem =
						layoutItem(getLayoutLabel(storedLayout));
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
					persistenceProvider().getStoredLayouts(
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
				text("error.restoreFailed.header"),
				text("error.restoreFailed.content")
		);
		return false;
	}

	/**
	 * Asks for a name and saves the layout showing now under it.
	 */
	private void saveCurrentLayoutAsNew() {
		final Optional<String> displayName = findLayoutName(
				text("dialog.saveAsNew.title"),
				text("dialog.saveAsNew.prompt"),
				""
		);

		if (displayName.isEmpty()) {
			return;
		}

		final String layoutIdentifier =
				LayoutNames.toIdentifier(displayName.get());

		// The name a user types is not an identifier, and deriving one is this
		// menu's step rather than the framework's. What the framework does is
		// say which rule a derived identifier breaks.
		final Optional<LayoutIdentifierProblem> problem =
				LayoutIdentifiers.findUserLayoutProblem(layoutIdentifier);

		if (problem.isPresent()) {
			showLayoutError(
					text("error.cannotSaveNamed.header", displayName.get()),
					problemText(problem.get())
			);
			return;
		}

		final LayoutPersistenceProfile newLayout = newProfile(
				layoutIdentifier,
				displayName.get()
		);

		final boolean isAlreadyStored;

		try {
			isAlreadyStored = persistenceProvider().isLayoutStored(newLayout);
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not tell whether the layout '{}' is stored.",
					layoutIdentifier,
					e
			);
			showLayoutError(text("error.saveFailed.header"), e.getMessage());
			return;
		}

		// Two names can derive one identifier, so this catches a collision the
		// user cannot see coming as well as the same name typed twice.
		if (isAlreadyStored && !confirmLayoutAction(
				text("confirm.replace.header", layoutIdentifier),
				text("confirm.replace.content")
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
				text("dialog.rename.title"),
				text("dialog.rename.prompt"),
				getLayoutLabel(activeLayout)
		);

		if (displayName.isEmpty()) {
			return;
		}

		if (displayName.get().isBlank()) {
			showLayoutError(
					text("error.blankName.header"),
					text("error.blankName.content")
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
				text("confirm.delete.header", layoutLabel),
				text("confirm.delete.content")
		)) {
			return;
		}

		try {
			persistenceProvider().deleteLayout(layoutPersistenceProfile);
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not delete the docking layout '{}'.",
					layoutPersistenceProfile.layoutIdentifier(),
					e
			);
			showLayoutError(text("error.deleteFailed.header"), e.getMessage());
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
			persistenceProvider().saveLayout(
					layoutPersistenceProfile,
					bentoProvider()
			);
			activeCustomLayoutProfile = layoutPersistenceProfile;
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not save the docking layout as '{}'.",
					layoutPersistenceProfile.layoutIdentifier(),
					e
			);
			showLayoutError(text("error.saveFailed.header"), e.getMessage());
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
	 * {@return the provider layouts are read, written, and listed through.}
	 *
	 * <p>Asked of the application at each use rather than held here, so that
	 * this menu cannot be the reason a stale provider stays reachable.</p>
	 */
	private DockingLayoutPersistenceProvider persistenceProvider() {
		return dockingLayoutRestorable.getPersistenceProvider();
	}

	/**
	 * {@return the provider naming what a save captures.}
	 *
	 * @see #persistenceProvider()
	 */
	private BentoProvider bentoProvider() {
		return dockingLayoutRestorable.getBentoProvider();
	}

	/**
	 * {@return an item naming one stored layout.}
	 *
	 * <p>Mnemonic parsing off, because the label is a name a user typed rather
	 * than one this framework wrote. Left on, an underscore in that name would
	 * be eaten as a mnemonic marker and the layout a user called
	 * {@code My_Layout} would be listed as {@code MyLayout}. There is nothing
	 * to lose by it: these items are as many as the user has saved layouts, so
	 * no mnemonic could be assigned to them ahead of time anyway.</p>
	 *
	 * @param label what the item says.
	 */
	private static MenuItem layoutItem(final String label) {
		final MenuItem item = new MenuItem(label);
		item.setMnemonicParsing(false);
		return item;
	}

	/**
	 * {@return the text for a key.}
	 *
	 * @param key names the text in this menu's {@link ResourceBundle}.
	 */
	private String text(final String key) {
		return texts.getString(key);
	}

	/**
	 * {@return the text for a key, with its placeholder filled in.}
	 *
	 * <p>Separate from {@link #text(String)} so that only the values holding a
	 * placeholder go through {@link MessageFormat}. Running every value through
	 * it would make a literal apostrophe an escape character in all of
	 * them, and so make every translated sentence a place to get that
	 * wrong.</p>
	 *
	 * @param key names the text in this menu's {@link ResourceBundle}.
	 * @param argument what to put in place of <code>{0}</code>.
	 */
	private String text(final String key, final Object argument) {
		return MessageFormat.format(texts.getString(key), argument);
	}

	/**
	 * {@return why a name a user typed cannot address a layout.}
	 *
	 * <p>Rendered here rather than shown as {@link
	 * LayoutIdentifierProblem#message()}, which is the framework's own sentence
	 * and comes in one language. Only the three rules below can be broken by an
	 * identifier that came out of {@link LayoutNames#toIdentifier(String)},
	 * which keeps nothing but letters and digits; the rest are about characters
	 * that cannot survive it. The framework's sentence is still the default
	 * arm, because a rule added later would otherwise arrive here as
	 * nothing.</p>
	 *
	 * @param problem which rule the derived identifier broke.
	 */
	private String problemText(final LayoutIdentifierProblem problem) {
		return switch (problem.rule()) {
			case BLANK -> text("problem.blank");
			case RESERVED -> text("problem.reserved");
			case DEVICE_NAME -> text("problem.deviceName");
			default -> problem.message();
		};
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
	 * <p>Mnemonic parsing off for the same reason as on a stored layout's item:
	 * nothing can be navigated to on a disabled item, so an underscore a
	 * translation happened to contain would be eaten for nothing.</p>
	 *
	 * @param menu the menu to add to.
	 * @param text what the item says.
	 */
	private static void addDisabledItem(final Menu menu, final String text) {
		final MenuItem item = new MenuItem(text);
		item.setMnemonicParsing(false);
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
		alert.setTitle(text("dialog.title"));
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
		alert.setTitle(text("dialog.title"));
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
	}
}
