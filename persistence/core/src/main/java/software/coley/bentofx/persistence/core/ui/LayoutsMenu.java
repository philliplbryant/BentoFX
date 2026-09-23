package software.coley.bentofx.persistence.core.ui;

import javafx.application.Platform;
import javafx.scene.control.*;
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
import software.coley.bentofx.persistence.core.api.provider.PersistedDockingLayoutOrganizationProvider;
import software.coley.bentofx.persistence.core.api.state.BentoState;
import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifierProblem;
import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers;
import software.coley.bentofx.persistence.core.api.storage.LayoutNames;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers.DEFAULT_LAYOUT_IDENTIFIER;
import static software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER;
import static software.coley.bentofx.persistence.core.ui.LayoutGroups.GroupNameProblem;
import static software.coley.bentofx.persistence.core.ui.LayoutsMenuTextKeys.*;

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
 * <p>This menu owns which named layout is showing, reading and writing which
 * one through its persistence provider's optional
 * {@link PersistedDockingLayoutOrganizationProvider} support. A provider that does not
 * implement it still restores and saves layouts normally, but this menu
 * offers no group management and starts every launch with no named layout
 * active.</p>
 *
 * <p>Users organize their saved layouts into groups from this menu: create one,
 * rename one, move layouts in and out, and delete one without losing the layouts
 * that were in it. A group is stored as a field of its own. A group also exists
 * before there is anything in it.</p>
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
	private final ResourceBundle resourceBundle;

	/**
	 * The custom layout showing now, or {@code null} when no named layout is
	 * active.
	 *
	 * <p>Saving changes and renaming both write the live containers, so both
	 * are offered only for the layout on screen. This is what they write to,
	 * and what the check mark in the menu marks.</p>
	 */
	private @Nullable LayoutPersistenceProfile activeCustomLayoutProfile;

	/**
	 * How the default layout was arranged when it was last applied, read from
	 * storage, or {@code null} when none is recorded or it could not be read.
	 *
	 * <p>Read once and kept, because it changes only when this menu records it
	 * again, which clears {@link #isDefaultArrangementRead}.</p>
	 */
	private @Nullable List<BentoState> defaultArrangement;

	/** Whether {@link #defaultArrangement} has been read since it last changed. */
	private boolean isDefaultArrangementRead;

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
	 * @param resourceBundle every word a user reads from this menu.
	 */
	public LayoutsMenu(
			final Window owner,
			final DockingLayoutRestorable dockingLayoutRestorable,
			final ResourceBundle resourceBundle
	) {
		super(resourceBundle.getString(LAYOUTS_MENU_KEY));

		this.dockingLayoutRestorable = dockingLayoutRestorable;
		this.owner = owner;
		this.resourceBundle = resourceBundle;
		this.activeCustomLayoutProfile = findActiveLayoutProfile();

		// With no session layout saved, a restore fell back to the default
		// layout, so that is what the application is showing.
		if (!isSessionLayoutStored()) {
			recordDefaultArrangementLater();
		}

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
		// Default is checked only when what is showing is arranged as the
		// default layout. Anything else is custom: a named layout, whose own item
		// is checked too, or an arrangement a user made without naming it, such
		// as the session layout the application starts with, which checks
		// Custom and nothing under it.
		final boolean isDefaultShowing =
				activeCustomLayoutProfile == null && isDefaultLayoutShowing();

		final MenuItem defaultItem = new MenuItem(
				markedText(
						getTextFromResourceBundle(DEFAULT_ITEM_KEY),
						isDefaultShowing
				)
		);
		defaultItem.setOnAction(event -> restoreDefaultLayout());

		final Menu customMenu = new Menu(
				markedText(
						getTextFromResourceBundle(CUSTOM_MENU_KEY),
						!isDefaultShowing
				)
		);
		populateCustomMenu(customMenu);

		final MenuItem saveAsNewItem =
				new MenuItem(getTextFromResourceBundle(SAVE_AS_NEW_ITEM_KEY));
		saveAsNewItem.setOnAction(event -> saveCurrentLayoutAsNew());

		getItems().setAll(defaultItem, customMenu, saveAsNewItem);
	}

	/**
	 * Fills the {@code Custom} menu.
	 *
	 * <p>Saving changes is offered only for the layout showing now, because it
	 * writes the containers on screen. Renaming and moving to a group are offered
	 * for any stored layout: both change only what a layout is called, and
	 * {@link DockingLayoutPersistenceProvider#updateStoredLayoutNaming} does that
	 * without reading the scene graph.</p>
	 *
	 * @param customMenu the menu to fill.
	 */
	private void populateCustomMenu(final Menu customMenu) {
		final Menu restoreMenu = new Menu(getTextFromResourceBundle(RESTORE_MENU_KEY));
		final Menu renameMenu = new Menu(getTextFromResourceBundle(RENAME_MENU_KEY));
		final Menu moveToGroupMenu = new Menu(getTextFromResourceBundle(MOVE_TO_GROUP_MENU_KEY));
		final Menu deleteMenu = new Menu(getTextFromResourceBundle(DELETE_MENU_KEY));
		final Menu groupsMenu = new Menu(getTextFromResourceBundle(GROUPS_MENU_KEY));

		final MenuItem saveChangesItem = new MenuItem(getTextFromResourceBundle(SAVE_CHANGES_ITEM_KEY));
		saveChangesItem.setDisable(activeCustomLayoutProfile == null);
		saveChangesItem.setOnAction(event -> saveChangesToActiveLayout());

		// One reading of storage fills every list below. Listing with display
		// names decodes every stored layout, so asking again would decode again.
		final Optional<List<LayoutPersistenceProfile>> storedLayouts =
				findStoredCustomLayouts();
		// flatMap, so that layouts which could not be listed give no groups
		// either: with no layouts to reconcile against, a catalog on its own
		// would report groups for a list of layouts this menu does not have.
		final Optional<List<String>> groupNames =
				storedLayouts.flatMap(this::findGroupNames);

		final List<Menu> layoutMenus =
				List.of(restoreMenu, renameMenu, moveToGroupMenu, deleteMenu);

		if (storedLayouts.isEmpty() || groupNames.isEmpty()) {
			layoutMenus.forEach(
					menu -> addDisabledItem(menu, getTextFromResourceBundle(LIST_FAILED_ITEM_KEY))
			);
			addDisabledItem(groupsMenu, getTextFromResourceBundle(LIST_FAILED_ITEM_KEY));
		} else {
			addLayoutItems(
					restoreMenu,
					storedLayouts.get(),
					groupNames.get(),
					true,
					this::restoreStoredLayout
			);
			addLayoutItems(
					renameMenu,
					storedLayouts.get(),
					groupNames.get(),
					false,
					this::renameStoredLayout
			);
			addLayoutItems(
					deleteMenu,
					storedLayouts.get(),
					groupNames.get(),
					false,
					this::deleteStoredLayout
			);

			// Nothing to move a layout into until a group exists, and an item
			// that opens a dialog offering no choice is worse than one that says
			// so.
			if (groupNames.get().isEmpty()) {
				addDisabledItem(moveToGroupMenu, getTextFromResourceBundle(NO_GROUPS_ITEM_KEY));
			} else {
				addLayoutItems(
						moveToGroupMenu,
						storedLayouts.get(),
						groupNames.get(),
						false,
						this::moveStoredLayoutToGroup
				);
			}

			populateGroupsMenu(groupsMenu, storedLayouts.get(), groupNames.get());
		}

		customMenu.getItems().setAll(
				restoreMenu,
				saveChangesItem,
				renameMenu,
				moveToGroupMenu,
				deleteMenu,
				groupsMenu
		);
	}

	/**
	 * Fills the {@code Groups} menu, which acts on the groups themselves rather
	 * than on any layout.
	 *
	 * <p>FX Application Thread only.</p>
	 *
	 * @param groupsMenu the menu to fill.
	 * @param storedLayouts the layouts a rename or a delete has to carry along.
	 * @param groupNames the groups that exist.
	 */
	private void populateGroupsMenu(
			final Menu groupsMenu,
			final List<LayoutPersistenceProfile> storedLayouts,
			final List<String> groupNames
	) {
		final MenuItem newGroupItem = new MenuItem(getTextFromResourceBundle(NEW_GROUP_ITEM_KEY));
		newGroupItem.setOnAction(event -> createGroup(groupNames));

		final Menu renameGroupMenu = new Menu(getTextFromResourceBundle(RENAME_GROUP_MENU_KEY));
		final Menu deleteGroupMenu = new Menu(getTextFromResourceBundle(DELETE_GROUP_MENU_KEY));

		if (groupNames.isEmpty()) {
			addDisabledItem(renameGroupMenu, getTextFromResourceBundle(NO_GROUPS_ITEM_KEY));
			addDisabledItem(deleteGroupMenu, getTextFromResourceBundle(NO_GROUPS_ITEM_KEY));
		} else {
			for (final String groupName : groupNames) {
				final MenuItem renameItem = layoutItem(groupName);
				renameItem.setOnAction(event ->
						renameGroup(groupName, groupNames, storedLayouts)
				);
				renameGroupMenu.getItems().add(renameItem);

				final MenuItem deleteItem = layoutItem(groupName);
				deleteItem.setOnAction(event ->
						deleteGroup(groupName, storedLayouts)
				);
				deleteGroupMenu.getItems().add(deleteItem);
			}
		}

		groupsMenu.getItems().setAll(
				newGroupItem,
				renameGroupMenu,
				deleteGroupMenu
		);
	}

	/**
	 * Fills a menu with one item per stored layout, nesting the layouts that
	 * belong to a group into a submenu for it.
	 *
	 * <p>Mnemonic parsing is off because a group name is a name a user typed.</p>
	 *
	 * <p>FX Application Thread only, like everything that builds these items.</p>
	 *
	 * @param targetMenu the menu to fill.
	 * @param storedLayouts the layouts to list, in label order.
	 * @param groupNames the groups that exist, in the order to show them.
	 * @param marksActiveLayout whether to mark the layout showing now, which
	 * suits a menu that switches layouts and misleads on one that deletes them.
	 * @param action what to do with the layout an item names.
	 */
	private void addLayoutItems(
			final Menu targetMenu,
			final List<LayoutPersistenceProfile> storedLayouts,
			final List<String> groupNames,
			final boolean marksActiveLayout,
			final Consumer<LayoutPersistenceProfile> action
	) {
		LayoutGroups.groupLayouts(groupNames, storedLayouts)
				.forEach((groupName, groupLayouts) -> targetMenu.getItems().add(
						groupMenu(
								groupName,
								groupLayouts,
								marksActiveLayout,
								action
						)
				));

		for (final LayoutPersistenceProfile ungroupedLayout :
				LayoutGroups.ungroupedLayouts(groupNames, storedLayouts)) {

			targetMenu.getItems().add(
					layoutItemFor(ungroupedLayout, marksActiveLayout, action)
			);
		}

		// An empty menu opens as an empty popup, which reads as a fault rather
		// than as an answer. Reached when nothing is stored and no group exists.
		if (targetMenu.getItems().isEmpty()) {
			addDisabledItem(targetMenu, getTextFromResourceBundle(NO_LAYOUTS_ITEM_KEY));
		}
	}

	/**
	 * {@return one group's submenu, holding an item per layout in it.}
	 *
	 * <p>FX Application Thread only.</p>
	 *
	 * @param groupName the group to build for.
	 * @param groupLayouts the layouts in it, which may be none.
	 * @param marksActiveLayout whether to mark the layout showing now.
	 * @param action what to do with the layout an item names.
	 */
	private Menu groupMenu(
			final String groupName,
			final List<LayoutPersistenceProfile> groupLayouts,
			final boolean marksActiveLayout,
			final Consumer<LayoutPersistenceProfile> action
	) {
		final Menu groupMenu = new Menu(markedText(
				groupName,
				marksActiveLayout
						&& groupLayouts.stream().anyMatch(this::isActiveLayout)
		));
		groupMenu.setMnemonicParsing(false);

		if (groupLayouts.isEmpty()) {
			addDisabledItem(groupMenu, getTextFromResourceBundle(NO_LAYOUTS_ITEM_KEY));

			return groupMenu;
		}

		for (final LayoutPersistenceProfile groupLayout : groupLayouts) {
			groupMenu.getItems().add(
					layoutItemFor(groupLayout, marksActiveLayout, action)
			);
		}

		return groupMenu;
	}

	/**
	 * {@return an item naming one stored layout, which carries out the supplied
	 * action on it.}
	 *
	 * <p>FX Application Thread only.</p>
	 *
	 * @param storedLayout the layout the item names.
	 * @param marksActiveLayout whether to mark the layout showing now.
	 * @param action what to do with the layout when the item is chosen.
	 */
	private MenuItem layoutItemFor(
			final LayoutPersistenceProfile storedLayout,
			final boolean marksActiveLayout,
			final Consumer<LayoutPersistenceProfile> action
	) {
		final MenuItem item = layoutItem(markedText(
				getLayoutLabel(storedLayout),
				marksActiveLayout && isActiveLayout(storedLayout)
		));
		item.setOnAction(event -> action.accept(storedLayout));

		return item;
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
	 * {@return the groups to show, or an empty {@link Optional} when storage
	 * could not be read.}
	 *
	 * <p>The stored catalog together with the groups the layouts themselves name -
	 * see {@link LayoutGroups#mergeGroupNames}. Callers that could not list the
	 * layouts must not call this at all, because a catalog on its own would
	 * report groups for a list of layouts the menu does not have. A provider with
	 * no {@link PersistedDockingLayoutOrganizationProvider} support has no catalog to read, so
	 * this reports only the groups the layouts themselves name.</p>
	 *
	 * @param storedLayouts the layouts storage reported.
	 */
	private Optional<List<String>> findGroupNames(
			final List<LayoutPersistenceProfile> storedLayouts
	) {
		final Optional<PersistedDockingLayoutOrganizationProvider> extendedProvider =
				extendedPersistenceProvider();

		if (extendedProvider.isEmpty()) {
			return Optional.of(
					LayoutGroups.mergeGroupNames(List.of(), storedLayouts)
			);
		}

		try {
			return Optional.of(LayoutGroups.mergeGroupNames(
					extendedProvider.get().getStoredGroups(
							LayoutPersistenceProfile.of(SESSION_LAYOUT_IDENTIFIER)
					),
					storedLayouts
			));
		} catch (final BentoStateException e) {
			logger.warn("Could not list the stored layout groups.", e);
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
			setActiveCustomLayoutProfile(null);
			recordDefaultArrangementLater();
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
			setActiveCustomLayoutProfile(layoutPersistenceProfile);
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
				getTextFromResourceBundle(HEADER_RESTORE_FAILED_ERROR_KEY),
				getTextFromResourceBundle(CONTENT_RESTORE_FAILED_ERROR_KEY)
		);
		return false;
	}

	/**
	 * Asks for a name, asks which group it belongs in, and saves the layout
	 * showing now under both.
	 *
	 * <p>The group is asked for only when there is a group to choose, so an
	 * application whose users never make one never sees a second dialog.</p>
	 */
	private void saveCurrentLayoutAsNew() {
		final Optional<String> displayName = findLayoutName(
				getTextFromResourceBundle(TITLE_SAVE_AS_NEW_DIALOG_KEY),
				getTextFromResourceBundle(PROMPT_SAVE_AS_NEW_DIALOG_KEY),
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
					getTextFromResourceBundle(HEADER_CANNOT_SAVE_NAMED_ERROR_KEY, displayName.get()),
					problemText(problem.get())
			);
			return;
		}

		final Optional<List<String>> groupNames =
				findStoredCustomLayouts().flatMap(this::findGroupNames);

		if (groupNames.isEmpty()) {
			showLayoutError(getTextFromResourceBundle(HEADER_LIST_GROUPS_FAILED_ERROR_KEY), null);
			return;
		}

		final String group;

		if (groupNames.get().isEmpty()) {
			group = null;
		} else {
			final Optional<String> choice = findGroupChoice(
					getTextFromResourceBundle(TITLE_SAVE_AS_NEW_DIALOG_KEY),
					getTextFromResourceBundle(GROUP_PROMPT_SAVE_AS_NEW_DIALOG_KEY),
					null,
					groupNames.get()
			);

			if (choice.isEmpty()) {
				return;
			}

			group = chosenGroup(choice.get());
		}

		final LayoutPersistenceProfile newLayout = newProfile(
				layoutIdentifier,
				displayName.get(),
				group
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
			showLayoutError(getTextFromResourceBundle(HEADER_SAVE_FAILED_ERROR_KEY), e.getMessage());
			return;
		}

		// Two names can derive one identifier, so this catches a collision the
		// user cannot see coming as well as the same name typed twice.
		if (isAlreadyStored && !confirmLayoutAction(
				getTextFromResourceBundle(HEADER_REPLACE_CONFIRM_KEY, layoutIdentifier),
				getTextFromResourceBundle(CONTENT_REPLACE_CONFIRM_KEY)
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
	 * Asks for a new name for a stored layout and stores it under that name.
	 *
	 * <p>The layout identifier does not change, so the layout keeps the place
	 * in storage it already had - a display name is stored with a layout, not
	 * what the layout is addressed by. Nor does the group, so renaming a layout
	 * does not move it.</p>
	 *
	 * <p>Only the name is written. A rename used to be a save as well, because
	 * nothing could rewrite the stored metadata on its own, which both restricted
	 * it to the layout on screen and meant renaming a layout quietly stored
	 * whatever arrangement happened to be showing.
	 * {@link DockingLayoutPersistenceProvider#updateStoredLayoutNaming} rewrites
	 * the name and leaves the docking state alone, so neither is true now.</p>
	 *
	 * @param storedLayout identifies the layout to rename.
	 */
	private void renameStoredLayout(
			final LayoutPersistenceProfile storedLayout
	) {
		final Optional<String> displayName = findLayoutName(
				getTextFromResourceBundle(TITLE_RENAME_DIALOG_KEY),
				getTextFromResourceBundle(PROMPT_RENAME_DIALOG_KEY),
				getLayoutLabel(storedLayout)
		);

		if (displayName.isEmpty()) {
			return;
		}

		if (displayName.get().isBlank()) {
			showLayoutError(
					getTextFromResourceBundle(HEADER_BLANK_NAME_ERROR_KEY),
					getTextFromResourceBundle(CONTENT_BLANK_NAME_ERROR_KEY)
			);
			return;
		}

		updateStoredLayoutNaming(
				storedLayout,
				displayName.get(),
				storedLayout.group()
		);
	}

	/**
	 * Asks which group a stored layout belongs in and moves it there.
	 *
	 * <p>Offered for any stored layout rather than only the one showing, and it
	 * rewrites nothing but the group - a layout does not have to be restored to be
	 * filed.</p>
	 *
	 * @param storedLayout identifies the layout to move.
	 */
	private void moveStoredLayoutToGroup(
			final LayoutPersistenceProfile storedLayout
	) {
		final Optional<List<String>> groupNames =
				findStoredCustomLayouts().flatMap(this::findGroupNames);

		if (groupNames.isEmpty()) {
			showLayoutError(getTextFromResourceBundle(HEADER_LIST_GROUPS_FAILED_ERROR_KEY), null);
			return;
		}

		final Optional<String> choice = findGroupChoice(
				getTextFromResourceBundle(TITLE_MOVE_TO_GROUP_DIALOG_KEY),
				getTextFromResourceBundle(PROMPT_MOVE_TO_GROUP_DIALOG_KEY),
				storedLayout.group(),
				groupNames.get()
		);

		if (choice.isEmpty()) {
			return;
		}

		updateStoredLayoutNaming(
				storedLayout,
				storedLayout.displayName(),
				chosenGroup(choice.get())
		);
	}

	/**
	 * {@return the group a user picked, or an empty {@link Optional} when the
	 * dialog was dismissed.}
	 *
	 * <p>What comes back is the label that was picked, which is
	 * {@code choice.noGroup} when the user chose to leave the layout in no group -
	 * put it through {@link #chosenGroup(String)} to get the group to store.</p>
	 *
	 * <p>A {@link ChoiceDialog} rather than a submenu of groups, because the menu
	 * item this opens from already names a layout and an item cannot both act and
	 * hold a submenu.</p>
	 *
	 * <p>FX Application Thread only, and blocks in a nested event loop until the
	 * dialog is dismissed.</p>
	 *
	 * @param title the dialog's title.
	 * @param prompt what to ask for.
	 * @param currentGroup the group to start on, or {@code null} to start on no
	 * group.
	 * @param groupNames the groups to choose between.
	 */
	private Optional<String> findGroupChoice(
			final String title,
			final String prompt,
			final @Nullable String currentGroup,
			final List<String> groupNames
	) {
		final String noGroupChoice = getTextFromResourceBundle(NO_GROUP_CHOICE_KEY);
		final List<String> choices = new ArrayList<>();
		choices.add(noGroupChoice);
		choices.addAll(groupNames);

		// Starting on a group that is not offered would show the dialog with a
		// selection the list does not hold, so an unknown group starts on none.
		final String startingChoice =
				currentGroup != null && choices.contains(currentGroup)
						? currentGroup
						: noGroupChoice;

		final ChoiceDialog<String> dialog =
				new ChoiceDialog<>(startingChoice, choices);

		dialog.initOwner(owner);
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(prompt);

		return dialog.showAndWait();
	}

	/**
	 * {@return the group to store for a label a user picked, which is
	 * {@code null} when they picked no group.}
	 *
	 * @param choice the label {@link #findGroupChoice} returned.
	 */
	private @Nullable String chosenGroup(final String choice) {
		return choice.equals(getTextFromResourceBundle(NO_GROUP_CHOICE_KEY)) ? null : choice;
	}

	/**
	 * Asks for a name and adds a group under it.
	 *
	 * <p>The group is written to the catalog and holds nothing, which is what
	 * lets it be created before there is a layout to put in it.</p>
	 *
	 * @param groupNames the groups that already exist, which a new name may not
	 * repeat.
	 */
	private void createGroup(final List<String> groupNames) {
		final Optional<String> groupName = findLayoutName(
				getTextFromResourceBundle(TITLE_NEW_GROUP_DIALOG_KEY),
				getTextFromResourceBundle(PROMPT_NEW_GROUP_DIALOG_KEY),
				""
		);

		if (groupName.isEmpty() || !isUsableGroupName(
				groupName.get(),
				groupNames,
				null
		)) {
			return;
		}

		// Read the catalog rather than reusing the merged list: merging folds in
		// the groups the layouts name, and writing those back would quietly
		// promote a group into the catalog that nobody asked to create.
		writeGroupCatalog(catalog -> {
			final List<String> updated = new ArrayList<>(catalog);
			updated.add(groupName.get().trim());
			return updated;
		}, getTextFromResourceBundle(HEADER_GROUP_FAILED_ERROR_KEY));
	}

	/**
	 * Asks for a new name for a group and renames it, along with every layout in
	 * it.
	 *
	 * <p>The catalog is rewritten first and the layouts after. Either order can be
	 * interrupted, and neither loses a layout: the group a layout records and the
	 * catalog are read together as a union, so a layout left behind under the old
	 * name still appears - under that name - rather than vanishing.</p>
	 *
	 * @param groupName the group to rename.
	 * @param groupNames the groups that exist.
	 * @param storedLayouts the layouts to carry across.
	 */
	private void renameGroup(
			final String groupName,
			final List<String> groupNames,
			final List<LayoutPersistenceProfile> storedLayouts
	) {
		final Optional<String> newGroupName = findLayoutName(
				getTextFromResourceBundle(TITLE_RENAME_GROUP_DIALOG_KEY),
				getTextFromResourceBundle(PROMPT_RENAME_GROUP_DIALOG_KEY),
				groupName
		);

		if (newGroupName.isEmpty() || !isUsableGroupName(
				newGroupName.get(),
				groupNames,
				groupName
		)) {
			return;
		}

		final String trimmedName = newGroupName.get().trim();

		final boolean catalogWritten = writeGroupCatalog(
				catalog -> catalog.stream()
						.map(stored -> stored.equalsIgnoreCase(groupName)
								? trimmedName
								: stored)
						.toList(),
				getTextFromResourceBundle(HEADER_RENAME_GROUP_FAILED_ERROR_KEY)
		);

		if (catalogWritten) {
			moveGroupMembers(
					groupName,
					trimmedName,
					storedLayouts,
					getTextFromResourceBundle(HEADER_RENAME_GROUP_FAILED_ERROR_KEY)
			);
		}
	}

	/**
	 * Confirms, then removes a group and takes the layouts in it out of it.
	 *
	 * <p>The layouts are not deleted. They are moved out of the group first and
	 * the catalog is rewritten after, so an interrupted delete leaves a group that
	 * is empty rather than one that appears to hold layouts no longer in it.</p>
	 *
	 * @param groupName the group to remove.
	 * @param storedLayouts the layouts to take out of it.
	 */
	private void deleteGroup(
			final String groupName,
			final List<LayoutPersistenceProfile> storedLayouts
	) {
		if (!confirmLayoutAction(
				getTextFromResourceBundle(HEADER_DELETE_GROUP_CONFIRM_KEY, groupName),
				getTextFromResourceBundle(CONTENT_DELETE_GROUP_CONFIRM_KEY)
		)) {
			return;
		}

		final boolean membersMoved = moveGroupMembers(
				groupName,
				null,
				storedLayouts,
				getTextFromResourceBundle(HEADER_DELETE_GROUP_FAILED_ERROR_KEY)
		);

		if (membersMoved) {
			writeGroupCatalog(
					catalog -> catalog.stream()
							.filter(stored -> !stored.equalsIgnoreCase(groupName))
							.toList(),
					getTextFromResourceBundle(HEADER_DELETE_GROUP_FAILED_ERROR_KEY)
			);
		}
	}

	/**
	 * Moves every layout in a group to another group, or out of any group.
	 *
	 * @param groupName the group whose layouts to move.
	 * @param newGroupName the group to move them to, or {@code null} to take them
	 * out of any group.
	 * @param storedLayouts the layouts to look through.
	 * @param errorHeader what to tell the user when a layout cannot be written.
	 * @return {@code true} when every layout in the group was moved; otherwise,
	 * {@code false}, the user having been told.
	 */
	private boolean moveGroupMembers(
			final String groupName,
			final @Nullable String newGroupName,
			final List<LayoutPersistenceProfile> storedLayouts,
			final String errorHeader
	) {
		for (final LayoutPersistenceProfile storedLayout : storedLayouts) {
			final String layoutGroup = storedLayout.group();

			if (layoutGroup == null || !layoutGroup.equalsIgnoreCase(groupName)) {
				continue;
			}

			try {
				persistenceProvider().updateStoredLayoutNaming(
						storedLayout.withNaming(
								storedLayout.displayName(),
								newGroupName
						)
				);
			} catch (final BentoStateException e) {
				logger.warn(
						"Could not move the stored layout '{}' out of the group "
								+ "'{}'.",
						storedLayout.layoutIdentifier(),
						groupName,
						e
				);
				showLayoutError(errorHeader, e.getMessage());
				return false;
			}
		}

		return true;
	}

	/**
	 * Reads the group catalog, changes it, and writes it back.
	 *
	 * @param change what the catalog should become, given what it holds now.
	 * @param errorHeader what to tell the user when it cannot be read or written,
	 * or when the persistence provider has no catalog to write to.
	 * @return {@code true} when the catalog was written; otherwise, {@code false},
	 * the user having been told.
	 */
	private boolean writeGroupCatalog(
			final UnaryOperator<List<String>> change,
			final String errorHeader
	) {
		final Optional<PersistedDockingLayoutOrganizationProvider> extendedProvider =
				extendedPersistenceProvider();

		if (extendedProvider.isEmpty()) {
			showLayoutError(errorHeader, null);
			return false;
		}

		final PersistedDockingLayoutOrganizationProvider provider = extendedProvider.get();
		final LayoutPersistenceProfile storageProfile =
				LayoutPersistenceProfile.of(SESSION_LAYOUT_IDENTIFIER);

		try {
			provider.setStoredGroups(
					storageProfile,
					change.apply(provider.getStoredGroups(storageProfile))
			);
			return true;
		} catch (final BentoStateException e) {
			logger.warn("Could not write the stored layout groups.", e);
			showLayoutError(errorHeader, e.getMessage());
			return false;
		}
	}

	/**
	 * Stores a different display name and group for a layout already in storage.
	 *
	 * @param storedLayout identifies the layout to rewrite.
	 * @param displayName the name to store.
	 * @param group the group to store, or {@code null} for no group.
	 */
	private void updateStoredLayoutNaming(
			final LayoutPersistenceProfile storedLayout,
			final @Nullable String displayName,
			final @Nullable String group
	) {
		try {
			if (!persistenceProvider().updateStoredLayoutNaming(
					storedLayout.withNaming(displayName, group)
			)) {
				// Deleted from under the menu, which is rebuilt each time it
				// opens, so the next open shows the truth.
				showLayoutError(getTextFromResourceBundle(HEADER_NOT_STORED_ERROR_KEY), null);
			}
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not rewrite the naming of the stored layout '{}'.",
					storedLayout.layoutIdentifier(),
					e
			);
			showLayoutError(getTextFromResourceBundle(HEADER_SAVE_FAILED_ERROR_KEY), e.getMessage());
		}
	}

	/**
	 * {@return {@code true} when the name can be a group; otherwise,
	 * {@code false}, the user having been told why.}
	 *
	 * @param groupName the name the user typed.
	 * @param groupNames the groups that exist.
	 * @param renamedGroup the group being renamed, or {@code null} when one is
	 * being created.
	 */
	private boolean isUsableGroupName(
			final String groupName,
			final List<String> groupNames,
			final @Nullable String renamedGroup
	) {
		final Optional<GroupNameProblem> problem =
				LayoutGroups.findGroupNameProblem(
						groupName,
						groupNames,
						renamedGroup
				);

		if (problem.isEmpty()) {
			return true;
		}

		showLayoutError(
				getTextFromResourceBundle(HEADER_CANNOT_NAME_GROUP_ERROR_KEY, groupName),
				switch (problem.get()) {
					case BLANK -> getTextFromResourceBundle(BLANK_GROUP_PROBLEM_KEY);
					case TOO_LONG -> getTextFromResourceBundle(
							GROUP_TOO_LONG_PROBLEM_KEY,
							LayoutGroups.MAX_GROUP_NAME_LENGTH
					);
					case DUPLICATE -> getTextFromResourceBundle(DUPLICATE_GROUP_PROBLEM_KEY);
				}
		);

		return false;
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
				getTextFromResourceBundle(HEADER_DELETE_CONFIRM_KEY, layoutLabel),
				getTextFromResourceBundle(CONTENT_DELETE_CONFIRM_KEY)
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
			showLayoutError(getTextFromResourceBundle(HEADER_DELETE_FAILED_ERROR_KEY), e.getMessage());
			return;
		}

		if (isActiveLayout(layoutPersistenceProfile)) {
			setActiveCustomLayoutProfile(null);
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
			setActiveCustomLayoutProfile(layoutPersistenceProfile);
		} catch (final BentoStateException e) {
			logger.warn(
					"Could not save the docking layout as '{}'.",
					layoutPersistenceProfile.layoutIdentifier(),
					e
			);
			showLayoutError(getTextFromResourceBundle(HEADER_SAVE_FAILED_ERROR_KEY), e.getMessage());
		}
	}

	/**
	 * {@return a profile naming a layout of the application's own, leaving the
	 * codec and the storage to the framework's selection.}
	 *
	 * @param layoutIdentifier addresses the layout in storage.
	 * @param displayName the name to store with the layout.
	 * @param group the group to store with the layout, or {@code null} for no
	 * group.
	 */
	private static LayoutPersistenceProfile newProfile(
			final String layoutIdentifier,
			final String displayName,
			final @Nullable String group
	) {
		return LayoutPersistenceProfile.named(
				layoutIdentifier,
				displayName,
				null,
				null
		).withNaming(displayName, group);
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
	 * {@return {@code true} when what is showing is arranged as the default
	 * layout was when it was last applied; otherwise, {@code false}.}
	 *
	 * <p>Asked each time the menu is filled, so that rearranging the default
	 * layout clears its check mark and arranging it back restores it. Compares
	 * against the arrangement recorded by {@link #recordDefaultArrangement()}
	 * rather than building the default layout, so asking costs a walk of the
	 * containers on screen and never touches their content. See
	 * {@link LayoutArrangements} for what counts as the arrangement.</p>
	 *
	 * <p>When nothing is recorded, or the persistence provider cannot read it
	 * back, there is no way to tell, and this answers {@code true} - checking
	 * {@code Default} whenever no named layout is active, as this menu did
	 * before it could tell.</p>
	 */
	private boolean isDefaultLayoutShowing() {
		final Optional<PersistedDockingLayoutOrganizationProvider> extendedProvider =
				extendedPersistenceProvider();

		if (extendedProvider.isEmpty()) {
			return true;
		}

		if (!isDefaultArrangementRead) {
			try {
				defaultArrangement = extendedProvider.get()
						.getStoredBentoStates(
								LayoutPersistenceProfile.of(DEFAULT_LAYOUT_IDENTIFIER)
						)
						.orElse(null);
				isDefaultArrangementRead = true;
			} catch (final BentoStateException e) {
				logger.warn("Could not read how the default layout was arranged.", e);
				return true;
			}
		}

		final List<BentoState> arrangement = defaultArrangement;

		return arrangement == null || LayoutArrangements.isShowing(
				arrangement,
				dockingLayoutRestorable.getBentoProvider()
		);
	}

	/**
	 * Records the arrangement showing now as the default layout's, once the
	 * application has finished applying it.
	 *
	 * <p>Deferred to the next pass of the JavaFX event loop because an
	 * application may still be attaching the layout when this is asked for -
	 * at startup this menu is built before the layout it sits beside is on
	 * screen. The user cannot rearrange anything before then, because input
	 * queued behind this task is handled after it.</p>
	 */
	private void recordDefaultArrangementLater() {
		// Only a provider that can read the arrangement back has a use for it.
		if (extendedPersistenceProvider().isPresent()) {
			Platform.runLater(this::recordDefaultArrangement);
		}
	}

	/**
	 * Records the arrangement showing now as the default layout's, under
	 * {@link LayoutIdentifiers#DEFAULT_LAYOUT_IDENTIFIER}, so it survives a
	 * restart. Must be called on the JavaFX Application Thread.
	 */
	private void recordDefaultArrangement() {
		try {
			persistenceProvider().saveLayout(
					LayoutPersistenceProfile.of(DEFAULT_LAYOUT_IDENTIFIER),
					dockingLayoutRestorable.getBentoProvider()
			);
			isDefaultArrangementRead = false;
		} catch (final BentoStateException e) {
			logger.warn("Could not record how the default layout is arranged.", e);
		}
	}

	/**
	 * {@return {@code true} when a session layout is stored, or when that
	 * cannot be read; otherwise, {@code false}.}
	 *
	 * <p>An unreadable answer counts as stored, so that this menu does not
	 * record whatever is showing as the default layout on a guess.</p>
	 */
	private boolean isSessionLayoutStored() {
		try {
			return persistenceProvider().isLayoutStored(
					LayoutPersistenceProfile.of(SESSION_LAYOUT_IDENTIFIER)
			);
		} catch (final BentoStateException e) {
			logger.warn("Could not tell whether a session layout is stored.", e);
			return true;
		}
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
	 * Sets {@link #activeCustomLayoutProfile} and records it through
	 * {@link PersistedDockingLayoutOrganizationProvider}, when the persistence provider
	 * supports it.
	 *
	 * <p>The only way that field changes - every other assignment in this
	 * class goes through here, so a restart never misses a change.</p>
	 *
	 * @param activeCustomLayoutProfile the layout to show as active, or
	 * {@code null} for none.
	 */
	private void setActiveCustomLayoutProfile(
			final @Nullable LayoutPersistenceProfile activeCustomLayoutProfile
	) {
		this.activeCustomLayoutProfile = activeCustomLayoutProfile;
		rememberActiveLayout(activeCustomLayoutProfile);
	}

	/**
	 * {@return the named layout to show as active from the moment this menu
	 * opens, read through {@link PersistedDockingLayoutOrganizationProvider}, or
	 * {@code null} when none is recorded, it no longer exists, or the
	 * persistence provider does not support this.}
	 *
	 * <p>An application that restores the session layout at startup - which
	 * mirrors whatever was last on screen, including the content of a named
	 * layout that was switched to and then left there - has no other way to
	 * know that happened. This is what lets this menu show that layout
	 * selected again after a restart, rather than {@code Default}.</p>
	 */
	private @Nullable LayoutPersistenceProfile findActiveLayoutProfile() {
		final Optional<PersistedDockingLayoutOrganizationProvider> extendedProvider =
				extendedPersistenceProvider();

		if (extendedProvider.isEmpty()) {
			return null;
		}

		try {
			final Optional<String> activeLayoutIdentifier =
					extendedProvider.get().getActiveLayoutIdentifier(
							LayoutPersistenceProfile.of(SESSION_LAYOUT_IDENTIFIER)
					);

			if (activeLayoutIdentifier.isEmpty()) {
				return null;
			}

			final LayoutPersistenceProfile activeLayoutProfile =
					LayoutPersistenceProfile.of(activeLayoutIdentifier.get());

			// Recorded once, but not necessarily still true: the layout may
			// have been deleted since.
			return persistenceProvider().isLayoutStored(activeLayoutProfile)
					? activeLayoutProfile
					: null;
		} catch (final BentoStateException e) {
			logger.warn("Could not read which named layout was active.", e);
			return null;
		}
	}

	/**
	 * Records which named layout is active through
	 * {@link PersistedDockingLayoutOrganizationProvider}, so {@link #findActiveLayoutProfile()}
	 * can read it back on the next launch. Does nothing when the persistence
	 * provider does not support this.
	 *
	 * @param activeLayoutProfile the layout now active, or {@code null} when
	 * the default layout is showing or the active layout was deleted.
	 */
	private void rememberActiveLayout(
			final @Nullable LayoutPersistenceProfile activeLayoutProfile
	) {
		extendedPersistenceProvider().ifPresent(extendedProvider -> {
			try {
				extendedProvider.setActiveLayoutIdentifier(
						LayoutPersistenceProfile.of(SESSION_LAYOUT_IDENTIFIER),
						activeLayoutProfile == null
								? null
								: activeLayoutProfile.layoutIdentifier()
				);
			} catch (final BentoStateException e) {
				logger.warn("Could not record which named layout is active.", e);
			}
		});
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
	 * {@return {@link #persistenceProvider()}, narrowed to
	 * {@link PersistedDockingLayoutOrganizationProvider}, or an empty {@link Optional} when
	 * it does not implement that too.}
	 */
	private Optional<PersistedDockingLayoutOrganizationProvider> extendedPersistenceProvider() {
		return persistenceProvider() instanceof PersistedDockingLayoutOrganizationProvider extendedProvider
				? Optional.of(extendedProvider)
				: Optional.empty();
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
	private String getTextFromResourceBundle(final String key) {
		return resourceBundle.getString(key);
	}

	/**
	 * {@return the text for a key, with its placeholder filled in.}
	 *
	 * <p>Separate from {@link #getTextFromResourceBundle(String)} so that only the values holding a
	 * placeholder go through {@link MessageFormat}. Running every value through
	 * it would make a literal apostrophe an escape character in all of
	 * them, and so make every translated sentence a place to get that
	 * wrong.</p>
	 *
	 * @param key names the text in this menu's {@link ResourceBundle}.
	 * @param argument what to put in place of <code>{0}</code>.
	 */
	private String getTextFromResourceBundle(final String key, final Object argument) {
		return MessageFormat.format(resourceBundle.getString(key), argument);
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
			case BLANK -> getTextFromResourceBundle(BLANK_PROBLEM_KEY);
			case RESERVED -> getTextFromResourceBundle(RESERVED_PROBLEM_KEY);
			case DEVICE_NAME -> getTextFromResourceBundle(DEVICE_NAME_PROBLEM_KEY);
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
		alert.setTitle(getTextFromResourceBundle(TITLE_DIALOG_KEY));
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
		alert.setTitle(getTextFromResourceBundle(TITLE_DIALOG_KEY));
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
	}
}
