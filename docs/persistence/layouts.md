# Managing Layouts

[&larr; Back to the BentoFX Persistence guide](guide.md)

Everything here is optional. An application that keeps one layout, restored at startup and saved on exit, needs none of it - see [Restoring the Layout](guide.md#restoring-the-layout) and [Saving the Layout](guide.md#saving-the-layout) instead.

To offer users layouts of their own, there are two routes. [A Ready-Made Layouts Menu](#layouts-menu) is a drop-in `Menu` that already does all of it. The calls in [Managing Several Layouts](#managing-several-layouts) are for building a presentation of your own.

- [Managing Several Layouts](#managing-several-layouts)
- [A Ready-Made Layouts Menu](#layouts-menu)
  - [Changing the Text](#layouts-menu-text)

<h2 id="managing-several-layouts">Managing Several Layouts</h2>

An application usually keeps one layout that follows the session, saved automatically and restored at startup. That one has a reserved identifier, `LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER`, so that an application does not spell the name out and a user cannot take it for a layout of their own:

```java
final LayoutPersistenceProfile sessionProfile =
        LayoutPersistenceProfile.of(LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER);
```

Reserved is not the same as invalid. Every operation accepts it, because saving to it, restoring it, and deleting it (a "reset to defaults") are all things an application legitimately does. What the reservation means is that `LayoutIdentifiers.isReserved(...)` refuses it where a user chose the name, and that a menu of layouts a user may restore leaves it out.

Letting users keep layouts of their own means naming them, listing them, and removing them, and the persistence provider answers all three:

```java
final LayoutPersistenceProfile profile = LayoutPersistenceProfile.of("review-layout");

// Save the layout showing now, under this name. One write, nothing left running.
persistenceProvider.saveLayout(profile, bentoProvider);

// Populate a menu. The session layout is in here too, so filter it out.
final List<String> storedLayouts =
        persistenceProvider.getStoredLayoutIdentifiers(profile);

// Warn before replacing, and remove on request.
final boolean wouldReplace = persistenceProvider.isLayoutStored(profile);
final boolean wasRemoved = persistenceProvider.deleteLayout(profile);
```

To show users the names they chose rather than identifiers, list with `getStoredLayouts`, which returns a profile per stored layout carrying its display name:

```java
for (final LayoutPersistenceProfile stored :
        persistenceProvider.getStoredLayouts(profile)) {

    if (!LayoutIdentifiers.isReserved(stored.layoutIdentifier())) {
        menu.add(stored.findDisplayName().orElse(stored.layoutIdentifier()), stored);
    }
}
```

[A Ready-Made Layouts Menu](#layouts-menu) below does all of this already. Reach for the calls above when you want a presentation of your own.

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

<h2 id="layouts-menu">A Ready-Made Layouts Menu</h2>

The listing, filtering, and naming above is what an application needs if it builds its own menu. It does not have to: the persistence module provides that menu.

`LayoutsMenu` is a `javafx.scene.control.Menu`, so it goes wherever a menu goes - a `MenuBar`, a `Window` menu, a context menu:

```java
windowMenu.getItems().add(new LayoutsMenu(stage, application));
```

It offers the default layout, the layouts a user has saved, and saving, renaming, and deleting those. A check mark marks whichever is showing. The menu rebuilds itself each time it opens, so the list and the mark keep up with storage while the application runs.

It also lets users organize their layouts into groups: `Groups > New Group...`, `Rename Group`, and `Delete Group`, with `Move to Group` on each saved layout. Groups appear as submenus wherever layouts are listed, ahead of the layouts in none, and a group holding the layout on screen is marked so finding it does not mean opening each one. A group created this way exists before anything is in it and survives its last layout being moved out; deleting one keeps its layouts and leaves them in no group. Nothing here asks a user to type a separator, because a layout's group is stored as a field of its own.

The two arguments are the window its dialogs belong to, and the application. The application implements `DockingLayoutRestorable`:

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

The two providers are on the interface rather than passed to the menu because an implementation cannot do without them anyway - reading a stored layout means calling `getLayoutRestorer` with a `BentoProvider`. Anything able to implement `getDockingLayout` already holds both.

`switchToLayout` takes a `Supplier` rather than a `DockingLayout` because reading the layout is part of the switch. An implementation has to stop whatever is saving the arrangement on screen before anything reads a replacement, so it is the implementation that decides when the supplier runs. It returns `false` when nothing was applied, and leaves telling the user to the menu.

The persistence demo's `BoxApp` is the worked version of all five methods.

<h3 id="layouts-menu-text">Changing the Text</h3>

Every word a user reads comes from a `ResourceBundle`. To offer another language, add `LayoutsMenu_<language>.properties` beside the one in this module. Java reads these files as UTF-8, so write the target language directly.

To supply text from the application instead, hand over a bundle:

```java
new LayoutsMenu(stage, application, ResourceBundle.getBundle(
        "com.example.myapp.LayoutsMenuTexts", Locale.FRENCH));
```

That bundle needs its own base name, in the application's own package. A `LayoutsMenu_fr.properties` placed in the application's module will not be found: resources in a named module are not visible to another, so the framework's own `getBundle` call cannot see it. Loading it from a class in the module that holds it is what makes it reachable.

A substituted bundle replaces the framework's own rather than falling back to it, so it has to carry every key. A missing one raises `MissingResourceException` the first time the menu opens.

Two things are worth knowing about the values:

1. **Three of them are `MessageFormat` patterns** - the ones holding `{0}`, which is the layout name. In those three only, a literal apostrophe has to be doubled and `{0}` must not be quoted, or the name is dropped. Everywhere else an apostrophe is just an apostrophe.
2. **Mnemonics live in the text.** An underscore marks the following letter, so a translation chooses its own, and has to keep them distinct within one menu. The items naming saved layouts carry none: a user names those, and mnemonic parsing is off on them so an underscore in a layout name shows as an underscore.

