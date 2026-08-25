# Docking Layout Persistence Implementation

For a high-level architectural overview, see the [BentoFX Persistence guide](../../README-PERSISTENCE.md).


This document describes BentoFX layout persistence as implemented by
[DockingLayoutSaver](../../persistence/core/src/main/java/software/coley/bentofx/persistence/core/impl/DockingLayoutSaver.java)
and [DockingLayoutRestorer](../../persistence/core/src/main/java/software/coley/bentofx/persistence/core/impl/DockingLayoutRestorer.java).

For the overarching design, [docking layout persistence diagrams](docking-layout-persistence-diagrams.md) are also
available.

## Scope

This document focuses on persistence orchestration and application integration. It does not describe rendering, docking
UX, or the full Gradle module dependency graph.

## Key concepts

### Domain state model

Persistence is expressed as immutable-ish *state* objects built with builders rather than direct serialization of UI
classes:

- `DockingLayout` is the application-facing restored layout. It contains one or more `BentoLayout` instances.
- `BentoLayout` contains runtime `DockContainerRootBranch` and `DragDropStage` instances for a single `Bento`.
- `BentoState` is the serializable state for a persisted `Bento`.
- `PersistableLayout` is what a codec encodes and decodes: the `BentoState` list a save captured, plus the layout-level
  metadata that travels with it. The one piece of metadata so far is the optional display name.
- `DockContainerRootBranchState`, `DockContainerBranchState`, and `DockContainerLeafState` represent the container tree.
- `DockableState` represents the information needed to reconstruct a runtime `Dockable`.
- `DragDropStageState` represents secondary drag/drop stages and contains a root-branch state.

The important distinction is that persisted state is not the same thing as live JavaFX UI. The codec serializes state.
The restorer uses that state, plus application-provided providers, to recreate runtime BentoFX objects.

### Storage and codec

Round-trip persistence is a multistep, pipelined process:

1. The application adds one or more `LayoutCodecProvider` implementations to make persisted formats available.
2. The application adds one or more `LayoutStorageProvider` implementations to make storage destinations available.
3. The default persistence provider selects codec and storage providers by explicit `LayoutPersistenceProfile` 
   identifiers, by a single available provider, or by a single default provider.
4. `LayoutSaver` walks the current BentoFX container graph through a `BentoProvider`.
5. `LayoutSaver` builds serializable state, encodes it with `LayoutCodec`, and writes it with `LayoutStorage`.
6. `LayoutRestorer` reads state with `LayoutStorage`, decodes it with `LayoutCodec`, and rebuilds runtime layout objects.
7. The application applies the returned `DockingLayout` to its stages.

This decoupling lets applications choose the persisted format, such as XML or JSON, and the storage location, such as a
file or database, without changing the save/restore orchestration. In the simple case, changing providers requires only 
a dependency change. When multiple providers are present, applications can select a specific codec or storage provider 
by identifier with `LayoutPersistenceProfile`.

### Display names

A layout identifier has to be usable as a file name, so it is not what a user would want to read in a menu. A layout
therefore carries an optional display name as well: the label the user gave it.

`LayoutPersistenceProfile.named(identifier, displayName, codec, storage)` supplies one, the saver puts it in the layout
metadata, and `getStoredLayouts` reads it back. The name is stored, never addressed by - the identifier still selects
which layout a save or restore touches - which is why the same layout can be renamed without moving what holds it. A
layout saved without a name, the session layout among them, has none, and `findDisplayName()` returns empty for it.

Renaming a layout writes its metadata and nothing else. `updateStoredLayoutNaming(profile)` decodes the stored layout,
replaces the display name and the group the profile carries, and writes the docking state back untouched. It reads
nothing from the scene graph, so the layout being renamed does not have to be the one on screen. This is what makes
renaming a group possible, since a group of five layouts is five of these calls and re-saving four of them from
whatever happened to be showing would overwrite four arrangements.

Both values are written as given, so a `null` clears rather than leaving what is stored alone. Change one and pass the
other through with `LayoutPersistenceProfile.withNaming(displayName, group)`.

### Groups

A layout also carries an optional group, the section a user filed it under. It is metadata of its own rather than
something read out of the display name, so a layout called `TCP/IP Debug` is one layout whose name contains a slash and
not a `TCP` group holding `IP Debug`. Users never have to know that a separator exists, because there is none.

A group outlives its members: a user creates one before there is a layout to put in it, and it survives its last layout
being moved out. The set of groups therefore cannot be read from the layouts alone, so it is kept in a **group catalog** -
one more stored entry, under the reserved identifier `LayoutIdentifiers.GROUP_CATALOG_LAYOUT_IDENTIFIER`, holding nothing
but the list of names. Because it is an ordinary layout entry, every storage implementation already holds it: a file
beside the layouts, a row beside the rows, with nothing added to `LayoutStorage`.

`getStoredGroups(profile)` reads the catalog and `setStoredGroups(profile, names)` replaces it. A caller listing groups
for a user should show the catalog **together with** the groups the stored layouts themselves name. The union costs
nothing and means a rename or a delete interrupted part way through cannot leave a layout inside a group that no longer
appears, which would be a stored layout the user can neither see nor delete.

Deleting a group does not delete the layouts in it. They are moved out and end up in no group.

Deriving the identifier from the name is still the application's step; see "Deriving a layout identifier from a display
name" under the capabilities below.

### Which layout a switch writes to

An application that lets a user move between named layouts has to write the outgoing arrangement before it takes the
outgoing tree apart, both because the arrangement would otherwise be lost and because auto-save has to come down for the
switch. What it writes to is worth being deliberate about, because it is not the layout the user was working in.

A `LayoutSaver` writes to the profile it was built for. The saver an application keeps running is the one for the session
layout, so releasing it on the way into a switch writes the session layout - and any rearranging the user did inside a
named layout since they last saved it lands there rather than with the name they associate it with. That is not a defect
to work around: the session layout is meant to be whatever was last on screen, so that a restart comes back to it. It
does mean a named layout needs a save of its own, which is why `saveLayout` exists as a one-shot write next to
`getLayoutSaver`, and why the demo has a `Save Changes` item that does nothing more than call it with the active
profile.


## Internal orchestration collaborators

`DockingLayoutSaver` and `DockingLayoutRestorer` are intentionally thin orchestration classes. The JavaFX threading
boundaries remain in those public entry points, while the detailed work is delegated to smaller package-private
collaborators:

| Collaborator | Responsibility | Threading expectation |
|--------------|----------------|-----------------------|
| `BentoLayoutStateCaptor` | Walks live BentoFX objects and builds serializable `BentoState` instances. | JavaFX application thread. |
| `LayoutStateWriter` | Encodes captured state with `LayoutCodec` and writes it with `LayoutStorage`. | Off the JavaFX application thread. |
| `LayoutStateReader` | Reads persisted bytes with `LayoutStorage` and decodes them with `LayoutCodec`. | Off the JavaFX application thread. |
| `DockingLayoutStateRestorer` | Rebuilds live BentoFX objects from decoded state and application providers. | JavaFX application thread. |

This split keeps the public saver/restorer API stable while making the persistence pipeline easier to test. Unit tests can
cover storage/codec error handling through `LayoutStateReader` and `LayoutStateWriter` without creating JavaFX stages,
and graphical integration tests can continue to cover full end-to-end save and restore behavior.

## Application integration model

The persistence framework can restore layout structure, but it cannot know how to recreate application-specific content
unless the application provides that knowledge.

A persistent application should therefore avoid treating the default layout as a one-time tree of anonymous runtime
objects. Instead, it should make each restorable object addressable by a stable identifier and provide lookup/factory
logic for that identifier.

### Basic demo model

The basic demo constructs the layout and dockables directly during startup:

```java
leafWorkspaceHeaders.addDockables(
        buildDockable(builder, 0, 0, "Class 1"),
        buildDockable(builder, 0, 1, "Class 2")
);
```

That pattern is simple and correct for an in-memory demo, but it has no external way to recreate the same dockables in a
later application run. The dockables are known only to the startup method that created them.

### Persistence demo model

The persistence demo separates dockable identity, dockable reconstruction, and dockable placement.

- `DockableProperties` defines stable identifiers and sample metadata for each dockable, including the shape and color of its icon, so that one loop over the enum builds every dockable state.
- `BoxAppDockableStateProvider` maps each stable identifier to a `DockableState`.
- `BoxApp` asks the provider for `DockableState` instances when building the default layout.
- `DockingLayoutRestorer` asks the same provider for `DockableState` instances when restoring a saved layout.

For the default layout, the demo places dockables by resolving state through the provider:

```java
dockableStateProvider.resolveDockableState(dockableProperties.getIdentifier())
        .ifPresentOrElse(
                dockableState ->
                        container.addDockable(buildDockable(dockableState)),
                () -> logger.warn("Could not add dockable {}.", dockableProperties)
        );
```

For a restored layout, the restorer receives the same provider:

```java
try (final LayoutRestorer layoutRestorer =
             persistenceProvider.getLayoutRestorer(
                     DEFAULT_LAYOUT_IDENTIFIER,
                     bentoProvider,
                     dockableStateProvider,
                     stageIconImageProvider,
                     dockContainerLeafMenuFactoryProvider
             )) {

    return layoutRestorer.restoreLayout(this::getDefaultDockingLayout);
}
```

This is the key application pattern: the default layout and restored layout should both rely on the same provider-backed
construction strategy. The default layout decides initial placement. The restored layout gets placement from persisted
state and uses providers to rebuild the objects referenced by that state.

## What application developers must implement

The persistence framework supplies the saver, restorer, state model, codec abstraction, and storage abstraction. Applications supply the runtime objects that are specific to the application.

| Required | Interface | When it is needed |
|----------|-----------|-------------------|
| Yes | `BentoProvider` | Always needed so the saver and restorer can find the runtime `Bento` instances by identifier. |
| Yes | `DockableStateProvider` | Needed whenever persisted layouts contain dockables that must be reconstructed by identifier. |
| Optional | `DockableMenuFactoryProvider` | Needed when restored dockables should receive application-specific context menus. |
| Optional | `DockContainerLeafMenuFactoryProvider` | Needed when restored leaves should receive application-specific context menus. |
| Optional | `StageIconImageProvider` | Needed when restored drag/drop stages should receive application-specific icons. |

Applications must also make at least one codec provider and one storage provider available at runtime by adding the appropriate modules to the runtime module path or classpath. If exactly one provider of each type is available, the framework selects them automatically. If multiple providers are available, applications can select specific providers by identifier with `LayoutPersistenceProfile`, rely on a single provider marked as default, or fail fast when provider selection is ambiguous.

Provider implementations should be available before calling `LayoutRestorer.restoreLayout(...)`. A provider may be called while restoring persisted state, while building the fallback default layout, or by future application features that restore layouts repeatedly.

### Stable identifier guidance

Identifiers are the bridge between persisted state and runtime objects. They should be stable across application restarts and, where practical, across application versions.

Identifiers may represent application views, tools, documents, domain objects, or any other concept that can be consistently reconstructed by the application.

Examples include:

- `projects`
- `terminal`
- `editor`
- `workspace-explorer`
- `document:12345`
- `customer:98765`
- `order:ABC123`

Avoid identifiers based on runtime object instances, memory addresses, hash codes, generated UUIDs that change between executions, timestamps, localized display labels, or other values that change between application executions.

Changing an identifier means older persisted layouts may no longer resolve the corresponding dockable. If a dockable is renamed, the `DockableStateProvider` can preserve compatibility by accepting the old identifier and returning the new runtime dockable state.

Applications should also consider how identifiers are resolved when the underlying object is no longer available. For example, a persisted identifier may refer to a document, record, or domain object that no longer exists when a layout is restored. In such cases, providers may choose to return an alternative dockable, a placeholder dockable, or no dockable at all, depending on the application's requirements.

### Provider lifecycle

Providers are usually long-lived application services. They should be created during startup, registered or wired into the persistence provider, and reused for both default layout creation and restoration.

Providers should not assume they are called only once. Restoration may be retried after a failed decode, future application features may allow users to switch saved layouts, and the default layout may use the same providers before any persisted layout exists.

### Codec and storage provider selection

`LayoutCodecProvider` and `LayoutStorageProvider` implementations expose stable provider identifiers. These identifiers allow applications to select a provider explicitly when more than one implementation is available.

A `LayoutPersistenceProfile` groups the layout identifier with optional codec and storage provider identifiers:

```java
LayoutPersistenceProfile profile = new LayoutPersistenceProfile(
        "main-layout",
        "json",
        "file"
);
```

If a profile does not specify codec or storage identifiers, the default persistence provider selects providers from runtime dependencies. A single available provider is selected automatically. If multiple providers are available, exactly one provider may be marked as default. Ambiguous provider sets fail with a configuration exception.

This keeps the common dependency-only replacement workflow simple while allowing future application features to save and restore multiple layouts with different codecs or storage destinations.

## Provider responsibilities

### `BentoProvider`

`BentoProvider` tells the saver and restorer which `Bento` instances exist.

During save, `DockingLayoutSaver` uses `BentoProvider.getAllBentos()` to find the BentoFX layouts that should be
persisted.

During restore, `DockingLayoutRestorer` uses `BentoProvider.getBento(identifier)` to find the runtime `Bento` that
matches the persisted `BentoState` identifier. The `Bento` must therefore have a stable identifier, and the application
must register it before restoration.

### `DockableStateProvider`

`DockableStateProvider` is the main application integration point for dockable restoration.

The persisted layout contains dockable identifiers and placement, not serialized JavaFX controls. When restoration sees a
persisted dockable identifier, it calls:

```java
Optional<DockableState> resolveDockableState(String id);
```

The returned `DockableState` contains the application-provided information needed to reconstruct a runtime `Dockable`,
including optional node, title, tooltip text, icon factory, menu factory, drag group mask, closable flag, and consumer.

Applications should treat persisted dockable identifiers as durable IDs. Renaming or removing identifiers can prevent
older saved layouts from restoring those dockables.

### `DockableMenuFactoryProvider`

`DockableMenuFactoryProvider` supplies menu factories for dockables. The persistence demo uses
`BoxAppDockableMenuFactoryProvider` when building `DockableState` instances so newly-created and restored dockables get
the same menu behavior.

### `DockContainerLeafMenuFactoryProvider`

`DockContainerLeafMenuFactoryProvider` supplies context-menu factories for restored leaves. The persistence demo uses
this provider both when building the default leaves and when restoring leaves from persisted state.

### `StageIconImageProvider`

`StageIconImageProvider` supplies icons for restored drag/drop stages. This is optional, but without it restored
secondary stages will not receive application-specific stage icons.

### `LayoutPersistenceProvider`

`LayoutPersistenceProvider` creates the application-facing `LayoutSaver` and `LayoutRestorer`. The default
`DockingLayoutPersistenceProvider` discovers codec and storage providers using `ServiceLoader` and selects providers by explicit profile identifiers, by a single available provider, or by a single default provider.

It also answers what is already stored, so that an application offering users a choice of layouts does not repeat that
selection logic:

| Operation | Purpose |
|-----------|---------|
| `saveLayout(profile, bentoProvider)` | Writes the layout showing now, once. Nothing is scheduled and no listener is registered, which is what distinguishes it from a `LayoutSaver`. |
| `getStoredLayoutIdentifiers(profile)` | Lists every layout the destination holds. The profile selects the codec and storage to ask; its layout identifier is not used. |
| `getStoredLayouts(profile)` | The same catalog with display names: a profile per stored layout, carrying the identifier and whatever name was saved with it. |
| `isLayoutStored(profile)` | Reports whether a layout is stored, without building a restorer to ask. |
| `deleteLayout(profile)` | Removes a stored layout, reporting whether there was one. |

`getStoredLayoutIdentifiers`, `isLayoutStored`, and `deleteLayout` delegate to `LayoutStorageProvider`, where they have
default implementations: a storage destination that cannot enumerate reports no layouts, and one that cannot delete
reports that it removed nothing. Both bundled implementations answer all three, since file storage keeps one file per
layout and database storage one row per layout and codec.

`getStoredLayouts` is the one that cannot delegate. A display name lives inside the layout, so recovering it means
opening and decoding every stored layout, which needs the codec that storage alone does not have. That is the cost of
the name-aware listing, and the reason the identifier listing stays: use identifiers when the names are not needed.
Each returned profile carries the codec and storage identifiers it was asked with, so it can be handed straight back to
`getLayoutRestorer` or `deleteLayout`. Like the identifier listing, it includes the session layout.

## Application design for persistence

The persistence framework changes how applications should organize docking layout construction.

A non-persistent application can create runtime docking components directly in its startup code. The basic demo does this by calling `buildDockable(...)` and immediately adding the returned `Dockable` objects to leaves.

```java
leafWorkspaceHeaders.addDockables(
        buildDockable(builder, 0, 0, "Class 1"),
        buildDockable(builder, 0, 1, "Class 2")
);
```

That style is simple, but the dockable creation logic only exists in the startup path. A later application execution has no general way to reconstruct a dockable when a saved layout says that the dockable with identifier `Class 1` should appear in a restored leaf.

A persistent application should separate three responsibilities:

1. Define stable identifiers for Bentos, containers, and dockables.
2. Use providers to resolve runtime objects and factories by identifier.
3. Use the same provider-backed reconstruction path for both default layout creation and restored layout creation.

### Default layout creation using providers

The persistence demo still builds a default layout, but it does not treat dockable construction as throwaway startup code. It resolves dockables through `DockableStateProvider`:

```java
private void addDockable(
        final DockableProperties dockableProperties,
        final DockableStateProvider dockableStateProvider,
        final DockContainer container
) {
    dockableStateProvider.resolveDockableState(dockableProperties.getIdentifier())
            .ifPresentOrElse(
                    dockableState ->
                            container.addDockable(buildDockable(dockableState)),
                    () -> logger.warn("Could not add dockable {}.", dockableProperties)
            );
}
```

`buildDockable(DockableState)` is the adapter between persistence state and the runtime BentoFX `Dockable`:

```java
private Dockable buildDockable(final DockableState dockableState) {
    final DockBuilding dockBuilding = bento.dockBuilding();
    final Dockable dockable = dockBuilding.dockable(dockableState.getIdentifier());

    dockableState.getDockableNode().ifPresent(dockable::setNode);
    dockableState.getTitle().ifPresent(dockable::setTitle);
    dockableState.getTooltipText().ifPresent(text -> dockable.setTooltip(new Tooltip(text)));
    dockableState.getDockableIconFactory().ifPresent(dockable::setIconFactory);
    dockableState.getDockableMenuFactory().ifPresent(dockable::setContextMenuFactory);

    return dockable;
}
```

### Restored layout creation using the same providers

During restore, the saved layout supplies the placement. The application supplies the same provider used by the default layout:

```java
try (final LayoutRestorer layoutRestorer =
             persistenceProvider.getLayoutRestorer(
                     DEFAULT_LAYOUT_IDENTIFIER,
                     bentoProvider,
                     dockableStateProvider,
                     stageIconImageProvider,
                     dockContainerLeafMenuFactoryProvider
             )) {

    return layoutRestorer.restoreLayout(this::getDefaultDockingLayout);
}
```

The restorer reads persisted identifiers from storage and asks the providers to reconstruct runtime objects. This means the default path and restored path use the same source of truth for dockables, menus, and stage icons.

```text
Default layout startup
    application chooses default container
    application resolves dockable identifier through provider
    application builds Dockable from DockableState
    application adds Dockable to default container

Saved layout restore
    persisted layout chooses restored container
    restorer resolves dockable identifier through provider
    restorer builds Dockable from DockableState
    restorer adds Dockable to restored container
```

This is the recommended mental model for applications using layout persistence: placement may come from the default layout or from persisted state, but runtime objects should come from providers in both cases.

### Provider threading responsibilities

The persistence framework creates or updates BentoFX runtime objects on the JavaFX Application Thread when those objects are restored by the framework. Application providers are responsible for following the same rule for objects that they create.

This matters because provider implementations commonly create JavaFX objects such as:

- `Node`
- `Label`
- `Tooltip`
- `ContextMenu`
- `MenuItem`
- `Image`
- `Stage`

The persistence demo's `BoxAppDockableStateProvider` builds its `DockableState` objects on first use, because they contain JavaFX controls and factories that create JavaFX objects, and a JavaFX `Application` constructor runs on the JavaFX-Launcher thread rather than the JavaFX Application Thread.

```java
@Override
public Optional<DockableState> resolveDockableState(String id) {
    if (dockableStateMap.isEmpty()) {
        putDockableStates();
    }

    return Optional.ofNullable(dockableStateMap.get(id));
}
```

Both callers of `resolveDockableState` are on the JavaFX Application Thread: the application while it builds the default layout, and `DockingLayoutStateRestorer` while it restores a saved one. Scheduling the same work from a constructor with `Platform.runLater(...)` also reaches the right thread, but leaves the map empty until the queued task runs, making each lookup depend on JavaFX queue ordering that no contract states.

Applications may choose eager, lazy, static, dynamic, or dependency-injected providers, but providers that create JavaFX objects must ensure that creation happens on the JavaFX Application Thread.

## Saving the layout design

### High-level algorithm

`LayoutSaver.saveLayout()` persists the current state of all `Bento` instances returned by the `BentoProvider`.

The default implementation, [DockingLayoutSaver](../../persistence/core/src/main/java/software/coley/bentofx/persistence/core/impl/DockingLayoutSaver.java),
extends [AbstractAutoCloseableLayoutSaver](../../persistence/core/src/main/java/software/coley/bentofx/persistence/core/impl/AbstractAutoCloseableLayoutSaver.java),
which implements [LayoutSaver](../../persistence/core/src/main/java/software/coley/bentofx/persistence/core/api/LayoutSaver.java).

`AbstractAutoCloseableLayoutSaver` can automatically save at scheduled intervals. To avoid unnecessary writes, it listens
for `DockEvent` changes and only saves when the layout has changed. Because it implements `AutoCloseable`, applications
can also use it in a try-with-resources block or close it explicitly during shutdown.

Applications should explicitly save on close request before stages are closed, and close the saver there as well:

```java
stage.setOnCloseRequest(this::saveDockingLayout);
```

The persistence demo does this for two reasons: closed stages are no longer discoverable when the saver walks open
windows, and closing the saver is what removes its listener from each `Bento` and stops its scheduler. An application
that exits with `System.exit(...)` never runs `Application.stop()`, so a close request handler is the last point at which
that cleanup still happens.

### Automatic scheduled saving

Auto-save is running on any saver obtained from a `DockingLayoutPersistenceProvider`. The default interval is five
minutes.

`AbstractAutoCloseableLayoutSaver` deliberately does not start auto-save from its constructor. Doing so published a
partly-built object to a scheduler thread and to every `Bento` event bus before subclass fields were assigned, so a save
firing in that window could observe a half-built saver. The provider calls `startAutoSave(...)` once construction is
complete instead; a directly constructed saver arms itself with `enableAutoSave(long, TimeUnit)`.

Because auto-save lives as long as the saver, an application obtains one saver while starting and keeps it. A saver built
where the layout is saved arms a scheduler for an application that is already exiting, and never auto-saves during the
session it was meant to protect.

The auto-save task is intentionally change-aware. The saver registers as a `DockEventListener` for every `Bento` supplied by the `BentoProvider`. When a docking event occurs, the saver records that the layout has changed. On each scheduled interval, the saver writes the layout only if a docking event has been observed since the previous save attempt.

This avoids repeatedly writing the same layout when the user has not changed the docking structure.

Applications should still call `saveLayout()` during shutdown. The scheduled task may not run after the user's final layout change, and closed stages are no longer discoverable by the saver.

Applications that keep a `LayoutSaver` for a long-lived scope should close it when it is no longer needed. The default implementation implements `AutoCloseable`; closing it performs final cleanup and disables the scheduled task.

### How the container tree is captured

The saver uses `BentoProvider` to walk each `Bento` container graph and converts runtime objects to state objects:

- `Bento` -> `BentoState`
- `DragDropStage` -> `DragDropStageState`
- `DockContainerRootBranch` -> `DockContainerRootBranchState`
- `DockContainerBranch` -> `DockContainerBranchState`
- `DockContainerLeaf` -> `DockContainerLeafState`
- `Dockable` -> `DockableState`

### Error handling philosophy

- Saver attempts to build each state independently; failure to build one state should not prevent building others.
- Encoding and streaming failures are treated as fatal and reported through `BentoStateException`.

## Restoring the layout design

### High-level algorithm

`LayoutRestorer.restoreLayout(Supplier<DockingLayout> defaultLayoutSupplier)` returns an application-facing
`DockingLayout`.

The default implementation:

1. Checks whether persisted layout storage exists.
2. If persisted layout storage does not exist, returns the layout from `defaultLayoutSupplier`.
3. If persisted layout storage exists, reads and decodes `BentoState`.
4. For each `BentoState`:
   - resolves the matching runtime `Bento` through `BentoProvider`
   - uses the `Bento`'s `DockBuilding` to recreate root branches, branches, leaves, and dockables
   - uses `DockableStateProvider` to resolve dockables by identifier
   - uses `DockContainerLeafMenuFactoryProvider` to restore leaf menu factories when available
   - creates a `BentoLayout` containing restored root branches and drag/drop stages
5. For each `DragDropStageState`:
   - restores the drag/drop stage root branch
   - uses `StageIconImageProvider` to restore stage icons when available
   - applies persisted stage geometry and other stage properties
6. Returns a `DockingLayout` containing the restored `BentoLayout` instances.

The application is responsible for applying the returned `DockingLayout`. In the persistence demo, `BoxApp` selects the
`BentoLayout` whose identifier matches its `Bento`, creates a scene from the restored root branch, and shows any restored
drag/drop stages.

### How dockables are restored

Restoration resolves dockables by identifier:

1. Each persisted dockable state supplies an identifier.
2. `DockableStateProvider.resolveDockableState(identifier)` returns the application-owned `DockableState`.
3. The restorer creates a runtime `Dockable` using the current `Bento`'s `DockBuilding`.
4. Optional `DockableState` values are applied to the runtime `Dockable`.
5. The dockable is added to the restored leaf.
6. Selected dockable identifiers are applied after dockables have been added.

This means applications should keep dockable identifiers stable across versions. If an application intentionally removes
or renames a dockable, the restorer can continue restoring the rest of the layout, but the missing dockable will be
skipped.

### Default layout fallback

The default layout supplier is more than a convenience. It is the first-run layout and the recovery layout.

It is used when:

- no persisted layout exists
- persisted layout storage cannot be read
- persisted layout state cannot be decoded

There is a fourth case the framework cannot detect, and the application owns it: a layout that restores cleanly but that
the application cannot apply, such as one holding a different number of root branches than the application knows how to
place. An application should report whether it applied anything and fall back to the default layout when it did not.
Otherwise a stage never receives a `Scene` and is never shown, and an application whose exit path runs when its window
hides can never be closed either.

The default layout should be built with the same identifiers and provider-backed dockable construction strategy used for
restoration. That keeps first-run behavior and restored behavior consistent.

### Error handling philosophy

- If persisted layout cannot be found, the default layout supplier is used.
- If persisted layout cannot be decoded, the default layout supplier is used.
- If one dockable cannot be resolved, the restorer logs a warning and continues restoring the rest of the layout.
- Layout restoration attempts to restore components independently where possible.

## Basic demo vs persistence demo

| Concern | Basic demo | Persistence demo |
|---------|------------|------------------|
| Bento identity | Uses a default `Bento`. | Uses a `Bento` with a stable identifier. |
| Layout creation | Builds the runtime container tree directly in `start(Stage)`. | Builds a default `DockingLayout`, then restores or applies a `DockingLayout`. |
| Dockable creation | Creates dockables inline. | Resolves `DockableState` by stable identifier through `DockableStateProvider`. |
| Source of truth for dockables | Startup code. | Provider implementations. |
| Dockable placement | Placement is hard-coded during startup. | Default placement is hard-coded, but restored placement comes from persisted layout state. |
| Menus | Menu factories are set directly. | Menu factories are supplied by providers so restored objects receive the same behavior. |
| Stage handling | Creates and shows the primary scene directly. | Applies the restored `BentoLayout` to the stage and shows restored drag/drop stages, falling back to the default layout when none can be applied. |
| Shutdown behavior | Exits on hidden. | Saves the docking layout on close request, then closes the saver, both before stages are closed. Also offers `File > Exit`, which saves explicitly because closing a stage in code raises no close request. |
| Application menus | None. The scene root is the docking root branch. | A `MenuBar` above the docking area, so the scene root is a `VBox`. The basic demo has no menu bar because it has no application-level commands to offer. |
| Named layouts | None. | `Window > Layouts` saves the layout showing under a name, and restores, renames or deletes a stored one. Switching happens live, so the demo also owns taking auto-save down around the switch, and writing the outgoing arrangement to the session layout on the way out. |

## Design patterns used

- **Builder**: `DockingLayoutBuilder`, `BentoLayoutBuilder`, `BentoStateBuilder`, `DockContainer*StateBuilder`,
  `DragDropStageStateBuilder`.
- **Composite**: Runtime dock container graph (`DockContainerBranch` + `DockContainerLeaf`) and its mirrored state graph.
- **Strategy**: `LayoutCodec` provides interchangeable encoding/decoding strategies. `LayoutStorage` provides
  interchangeable storage strategies.
- **Adapter / Mapper**: Codec implementations map between persistence state and DTOs suitable for JSON or XML binding.
- **Factory**: `DockBuilding` creates runtime container instances during restore. Menu providers supply factories for
  restored leaves and dockables.
- **Service Provider Interface**: `ServiceLoader` discovers codec and storage providers at runtime.

## Extension points

- Applications may implement additional storage destinations, such as other databases, remote services, or cloud storage.
- Applications may implement additional codec formats, such as Protobuf, YAML, or versioned schemas.

## Additional capabilities under consideration

### Deriving a layout identifier from a display name

A layout identifier becomes a file name in file-backed storage, so it cannot be whatever a user types. Storing the name a
user chose is done - see "Display names" above - but deriving the identifier from that name is not: an application that
lets a user save "Multi-Monitor" still picks `multi-monitor` itself.

The persistence demo carries a worked example of that step, in `BoxApp.toLayoutIdentifier`. It keeps only letters and
digits, replaces every run of anything else with a single `-`, and strips the ends, which is enough that its output
cannot break any rule about the *characters* in an identifier. What it cannot rule out is a name that reduces to nothing,
to the reserved session identifier, or to a name Windows resolves to a device - so it still puts the result to
`LayoutIdentifiers.findUserLayoutProblem`. Any derivation the framework grew would have the same three left over.

Two things have to be decided before the framework can do it:

- What a derivation does with the characters an identifier cannot hold. `LayoutIdentifiers.findUserLayoutProblem` reports
  that a name is unusable, which is enough for a dialog that rejects it, but not enough to rewrite it into something
  storable.
- What happens on a collision, since two display names can reduce to the same identifier, and the second save would
  otherwise overwrite the first.

### The reserved session layout

The layout an application saves to while it runs and restores when it starts is identified by
`LayoutIdentifiers.SESSION_LAYOUT_IDENTIFIER`, and `LayoutIdentifiers.isReserved(String)` reports it. Reserving it keeps
a user from naming a layout that the automatic save would then overwrite.

Reserved is not invalid. `requireValid` accepts it, and every operation works with it, because saving to it, restoring it,
and deleting it - which is how an application offers "reset to the default layout" - are all legitimate. Nor can the
framework refuse it selectively: the session save and a user's "save as" reach the same method, so nothing inside can tell
them apart. What the framework provides is the identifier and the test; an application applies the test where it knows the
name came from a user, which is a "save as" dialog today and the display-name generator once that exists.

`getStoredLayoutIdentifiers` reports the session layout like any other, because a catalog that hid a stored layout would
misreport what the destination holds. A menu of layouts a user may restore filters it out.

The comparison is case-insensitive, since a file name is case-insensitive on Windows and macOS: a layout called
`Session` would be the same stored layout as the session's own on two of the three platforms this framework runs on.

### Other capabilities

- Add layout versioning and migration, likely in the codec layer.
- Let an application rewrite a stored layout's display name without applying that layout first. A save reads the live
  containers, so renaming today means renaming the layout on screen - which is why the demo offers `Rename` only for the
  layout showing.
