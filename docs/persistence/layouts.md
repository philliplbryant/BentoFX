# Managing Layouts

[&larr; Back to the BentoFX Persistence guide](guide.md)

> <span style="font-size: 1.5em;">💡</span> Everything described here is optional. An application that keeps one layout, restored at startup and saved on exit, needs nothing described here - see [Restoring the Layout](guide.md#restoring-the-layout) and [Saving the Layout](guide.md#saving-the-layout) instead.

To offer users layouts of their own, there are two routes. [A Ready-Made Layouts Menu](#layouts-menu) is a drop-in `Menu` that already does all of it. It also allows application developers the ability to [change the text](#layouts-menu-text) to support specific application requirements.   

[Managing Multiple Layouts](#managing-several-layouts) describes layout naming restrictions, modifying the location where layouts are persisted, and separating layouts from different BentoFX persistence enabled applications running on the same machine. 

And [Building a Custom Presentation for Managing Layouts](#custom-presentation) describes functions provided by the framework that can be used to build a presentation of your own.

<h2 id="layouts-menu">A Ready-Made Layouts Menu</h2>

To build a `Menu`, an application needs the ability to list, filter, and name layouts. The persistence module provides these capabilities. It also provides as the ready-made `LayoutsMenu` that can be used wherever a JavaFX `Menu` can be used - a `MenuBar`, a `Window` menu, or a context menu:

```java
windowMenu.getItems().add(new LayoutsMenu(owner, dockingLayoutRestorable));
```

where `owner` is the `Window` to which dialogs raised by `LayoutMenu` actions belong and `dockingLayoutRestorable` is usually the application, but can be any `DockingLayoutRestorable` implementation whose docking layout these
`LayoutMenu` actions switch and whose providers can be used to read and write layouts.

The `LayoutMenu` provides the ability to:

* Restore the default layout
* Restore layouts a user has saved
* Save layouts
* Rename layouts
* Delete layouts
* Group layouts
* Manage layout groups.
 
A check mark identifies whichever layout is showing. And the menu rebuilds itself each time it opens, so the list and the mark updates itself as the application runs.

As indicated above, the `LayoutMenu` lets users organize their layouts into groups: `Groups > New Group...`, `Rename Group`, and `Delete Group`, with `Move to Group` on each saved layout. Groups appear as submenus wherever layouts are listed, and a group holding the layout on screen is marked so finding it does not require opening each one. 

A group created this way must be created before any layouts can be added to it. And a group survives its last layout being moved out. Deleting a group keeps its layouts and leaves them in ungrouped. 

<h3 id="docking-layout-restorable">The `DockingLayoutRestorable` Interface</h2>

The second of the two arguments used to create a `LayoutMenu` is a `DockingLayoutRestorable` implementation, which is usually the application itself.

```java
public class MyApp extends Application implements DockingLayoutRestorable {

    @Override
    public DockingLayout getDefaultDockingLayout() { ... }

    @Override
    public DockingLayout getDockingLayout(
            LayoutPersistenceProfile profile,
            Supplier<DockingLayout> fallbackLayoutSupplier) { ... }

    @Override
    public boolean switchToLayout(
            Supplier<DockingLayout> dockingLayoutSupplier) { ... }

    @Override
    public DockingLayoutPersistenceProvider getPersistenceProvider() {
        return persistenceProvider;
    }

    @Override
    public BentoProvider getBentoProvider() {
        return bentoProvider;
    }
}
```

The two providers are on the interface rather than passed to the menu separately because an implementation cannot do without them anyway - reading a stored layout means calling `getLayoutRestorer` with a `BentoProvider`, thus anything able to implement `getDockingLayout` already holds both.

`switchToLayout` takes a `Supplier` rather than a `DockingLayout` because reading the layout is part of the switch. An implementation has to stop whatever is saving the arrangement on screen before anything reads a replacement, so it is the implementation that decides when the supplier runs. It returns `false` when nothing was applied, and leaves reporting the result of the switch to the `LayoutMenu`.

The persistence demo's `BoxApp` implements all five methods.

<h3 id="layouts-menu-text">Changing the Text</h3>

All `LayoutMenu` text comes from a `ResourceBundle` or names provided by the user when saving layouts and layout groups. To offer menu text in another language, add `LayoutsMenu_<language>.properties` beside [the existing properties file](../../persistence/core/src/main/resources/software/coley/bentofx/persistence/core/ui/LayoutsMenu.properties) in the `persistence-core` module's resource folder. Java reads these files as UTF-8, so write the target language directly.

To supply text from the application instead, hand over a bundle:

```java
new LayoutsMenu(stage, application, ResourceBundle.getBundle(
        "com.example.myapp.LayoutsMenuTexts", Locale.FRENCH));
```

That bundle needs its own base name, in the application's own package. A `LayoutsMenu_fr.properties` placed in the application's module will not be found: resources in a named module are not visible to another, so the framework's own `getBundle` call cannot see it. Loading it from a class in the module that holds it is what makes it reachable.

A substituted bundle replaces the framework's own rather than falling back to it, so it has to carry every key. A missing one raises `MissingResourceException` the first time the menu opens.

Two things are worth knowing about the values:

1. **Three of them are `MessageFormat` patterns** - the ones holding `{0}`, which is the layout name. In those three only, a literal apostrophe has to be doubled and `{0}` must not be quoted, or the name is dropped. Everywhere else an apostrophe is just an apostrophe.
2. **Mnemonics live in the text.** An underscore marks the following letter, so a translation chooses its own, and has to keep them distinct within one menu. The items naming saved layouts carry no mnemonics because the user names them, and mnemonics are not parsed parsed from user provided names. So, an underscore in a layout or layout group name shows as an underscore.

<h2 id="managing-several-layouts">Managing Multiple Layouts</h2>

An application usually keeps one layout that follows the session, saved automatically and restored at startup. This layout has a reserved identifier, `LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER`, so that an application does not spell the name out and a user cannot take it for a layout of their own:

```java
final LayoutPersistenceProfile sessionProfile =
        LayoutPersistenceProfile.of(LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER);
```

Reserved is not the same as invalid. Every operation accepts it, because saving to it, restoring it, and deleting it (a "reset to defaults") are all things an application legitimately does. What the reservation means is that `LayoutIdentifiers.isReserved(...)` refuses it where a user chose the name, and that a menu of layouts a user may restore leaves it out.

Letting users keep layouts of their own means naming them, listing them, and removing them, and the persistence provider provides the ability to perform each of these tasks:

```java
final LayoutPersistenceProfile profile = LayoutPersistenceProfile.of("review-layout");

// Save the layout showing now, under this name. One write, nothing left running.
persistenceProvider.saveLayout(profile, bentoProvider);

// Populate a menu. The session layout is in here too, so filter it out.
final List<String> storedLayouts =
        persistenceProvider.getStoredLayoutIdentifiers(profile);

// Use these to warn before replacing, and remove on request.
final boolean wouldReplace = persistenceProvider.isLayoutStored(profile);
final boolean wasRemoved = persistenceProvider.deleteLayout(profile);
```

In addition to naming persisted layouts, application developers can change the save location and a workspace into which layouts are saved. Using a named workspace allows multiple applications using BentoFX persistence to run on the same machine and save layouts separately such that layouts from one application do not overlap with or overwrite layouts from another application.


<h2 id="custom-presentation">Building a Custom Presentation for Managing Layouts</h2>

[//]: # (TODO BENOT-13: Continue editing from here)

The following describes functions provided by the framework that can be used to build your own presentation.

To show the user provided names for layouts (rather than their identifiers), use `DockingLayoutPersistenceProvider.getStoredLayouts`, which returns a `LayoutPersistenceProfile` for each stored layout, with the `LayoutPersistenceProfile` containing the display name:

```java
for (final LayoutPersistenceProfile stored :
        persistenceProvider.getStoredLayouts(profile)) {

    if (!LayoutIdentifiers.isReserved(stored.layoutIdentifier())) {
        menu.add(stored.findDisplayName().orElse(stored.layoutIdentifier()), stored);
    }
}
```

Several things are worth knowing:

* **`saveLayout` is not a `LayoutSaver`.** It writes once and returns; nothing is scheduled and no listener is registered. Keep `getLayoutSaver` for the layout that follows the session and use this for a layout a user asked to keep.
* **A display name is stored, not addressed by.** `LayoutPersistenceProfile.named(identifier, displayName, codec, storage)` carries the name into the layout; the identifier still does the addressing. `getStoredLayoutIdentifiers` returns identifiers cheaply, while `getStoredLayouts` reads each layout to recover its name, so use the identifier listing when the names are not needed.
* **The identifier is the application's to choose.** The framework validates one and stores a name, and will derive one for you: `LayoutNames.toIdentifier("Multi-Monitor")` returns `multi-monitor`, keeping only letters and digits so the result cannot be a path, hold a character a filesystem reserves, or end in a space or a period. `LayoutIdentifiers.findUserLayoutProblem(identifier, codec)` reports whether a chosen identifier is usable, including whether it collides with the reserved session name. Pass the identifier alone when the codec is the framework's own selection to make and the application has none to name: that overload applies every rule but the joined-length one, which needs both halves.
* **Restoring a different layout while running is not the same as restoring one at startup.** The containers a restorer hands back are unattached, so the application replaces the scene root and re-shows any drag/drop stages itself, and the switch itself looks like a layout change to a running auto-save. Save the current layout first, or take auto-save down around the switch.
* **Saving before a switch writes the layout the *saver* names, which is not the layout being left.** A `LayoutSaver` writes to the profile it was built for, and that is the session layout. So the arrangement on screen at the moment of a switch goes to the session layout, not to the named layout the user had been working in: whatever they changed since they last saved that layout stays out of it. An application offering named layouts therefore needs a "save changes" of its own, writing the live layout back under the name it came from with `saveLayout`. That is why the persistence demo offers `Save Changes` only for the layout showing.
* **Renaming and grouping do not go through `saveLayout`.** `updateStoredLayoutNaming(profile)` rewrites a stored layout's display name and group and leaves its docking state alone, reading nothing from the scene graph. So a layout does not have to be restored to be renamed or filed, and renaming one never quietly stores the arrangement that happened to be showing. Both values are written as given - a `null` clears - so change one and pass the other through with `LayoutPersistenceProfile.withNaming(displayName, group)`.
* **A group is a field, and it outlives its members.** A layout's group is its own metadata, not something read out of its name, so `TCP/IP Debug` is one layout with a slash in its name and users never learn of a separator. Because a group exists before anything is in it and survives its last layout leaving, the set of groups is kept in a **group catalog**: one stored entry under the reserved identifier `LayoutIdentifiers.GROUP_CATALOG_LAYOUT_IDENTIFIER`, read with `getStoredGroups` and replaced with `setStoredGroups`. Show it together with the groups the layouts themselves name - the union cannot hide a layout in a group the catalog has lost. Deleting a group keeps its layouts and leaves them in no group.

A layout identifier becomes a file name in file-backed storage, so it has to be usable as one: no separators, nothing a filesystem reserves, and at most 255 characters shared with the codec identifier. A name a user types is not automatically usable, which is why an application either maps display names to identifiers or restricts what the user may type.

The persistence demo does all of this, if you would rather read it working than described. `Window > Layouts` in `demos/persistence` saves the layout showing under a name, restores a stored one live, renames it, files it in a group, and deletes it; it reads its text from a `ResourceBundle` and derives identifiers with `LayoutNames.toIdentifier`.

For a dialog, ask instead of being refused. `LayoutIdentifiers.findUserLayoutProblem(...)` returns an empty `Optional` when the pair is usable, and otherwise names the rule that was broken, which identifier broke it, and a message ready to show:

```java
LayoutIdentifiers.findUserLayoutProblem(typedName, codecIdentifier)
        .ifPresentOrElse(
                problem -> nameField.setError(describe(problem.rule(), problem.message())),
                () -> nameField.clearError()
        );
```

Switch on `problem.rule()` to phrase it in your own words or your own language, or show `problem.message()` when there is nothing better to say. `findProblem(...)` is the same check without the reserved-identifier rule, for an identifier your own code chose rather than a user; `requireValid(...)` throws from the same result, so what it reports and what it throws are one sentence.

Application state is not part of a layout. The framework persists structure - which containers exist, where they sit, which dockable is selected - and an application keeps its own state in its own store, under the same identifiers the framework hands back when it asks for a `DockableState`. That separation is deliberate: content state is usually per-document rather than per-layout, so a user with four saved layouts would otherwise carry four drifting copies of it.

The [Ready-Made Layouts Menu](#layouts-menu) described above does all of this already. 
