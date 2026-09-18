package software.coley.bentofx.persistence.core.api.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.core.api.BentoStateException;
import software.coley.bentofx.persistence.core.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.core.ui.LayoutsMenu;

import java.util.List;
import java.util.Optional;

/**
 * Storage beyond {@link DockingLayoutPersistenceProvider} that only
 * {@link LayoutsMenu} needs: the catalog of layout groups, and which named
 * layout is active.
 *
 * <p>Kept apart from {@link DockingLayoutPersistenceProvider} so that an
 * implementation with no use for this menu is not forced to implement it.
 * {@link LayoutsMenu} tests for this with {@code instanceof} on whatever
 * {@link DockingLayoutRestorable#getPersistenceProvider()} returns; a
 * provider that does not implement it still restores and saves layouts
 * normally, but the menu offers no group management and starts every launch
 * on {@code Default}.</p>
 *
 * @author Phil Bryant
 */
public interface DockingLayoutOrganizationProvider {

	/**
	 * {@return the groups that exist, in the order they were stored.}
	 *
	 * <p>An empty list means no group has been created, or that the storage
	 * implementation cannot enumerate.</p>
	 *
	 * @param layoutPersistenceProfile selects the codec and storage to ask; its
	 * layout identifier is not used.
	 *
	 * @throws BentoStateException when the codec or storage cannot be selected,
	 * or the catalog cannot be read.
	 */
	List<String> getStoredGroups(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) throws BentoStateException;

	/**
	 * Replaces the stored group catalog with the supplied names.
	 *
	 * @param layoutPersistenceProfile selects the codec and storage to write to.
	 * @param groups all groups that exist.
	 *
	 * @throws BentoStateException when the codec or storage cannot be selected,
	 * or the catalog cannot be written.
	 */
	void setStoredGroups(
			final LayoutPersistenceProfile layoutPersistenceProfile,
			final List<String> groups
	) throws BentoStateException;

	/**
	 * {@return the identifier of the named layout that is active, or an empty
	 * {@link Optional} when none is.}
	 *
	 * <p>An application that restores the session layout at startup - which
	 * mirrors whatever was last on screen, including the content of a named
	 * layout switched to and left there - has no other way to know that
	 * arrangement came from a named layout. This is what lets {@link LayoutsMenu}
	 * show the right one selected again after a restart.</p>
	 *
	 * @param layoutPersistenceProfile selects the codec and storage to ask; its
	 * layout identifier is not used.
	 *
	 * @throws BentoStateException when the codec or storage cannot be selected,
	 * or the entry cannot be read.
	 */
	Optional<String> getActiveLayoutIdentifier(
			final LayoutPersistenceProfile layoutPersistenceProfile
	) throws BentoStateException;

	/**
	 * Records which named layout is active, or clears it.
	 *
	 * @param layoutPersistenceProfile selects the codec and storage to write to.
	 * @param layoutIdentifier the identifier to record, or {@code null} when no
	 * named layout is active.
	 *
	 * @throws BentoStateException when the codec or storage cannot be selected,
	 * or the entry cannot be written.
	 */
	void setActiveLayoutIdentifier(
			final LayoutPersistenceProfile layoutPersistenceProfile,
			final @Nullable String layoutIdentifier
	) throws BentoStateException;
}
