# Provider Interfaces

[&larr; Back to the BentoFX Persistence guide](guide.md)

Applications supply provider implementations to persist docking layouts. These providers act as factories or lookup services for runtime objects that cannot be serialized directly. Of the eight, only `DockableStateProvider` has no framework implementation, so it is the one an application must write for itself.

- [The Provider Interfaces](#provider-interfaces)
- [Inline Creation Versus a Provider](#inline-vs-provider)

<h2 id="provider-interfaces">The Provider Interfaces</h2>

As previously mentioned, applications supply provider implementations to persist docking layouts. These providers act as factories or lookup services for runtime objects that cannot be serialized directly.

> <span style="font-size: 1.5em;">💡</span>  `DockableStateProvider` is the only required provider interface for which the framework does not include a default implementation.

`DockableStateProvider` has a single method: `Optional<DockableState> resolveDockableState(String id);`

A persisted layout records *which* dockables were open, never what was inside them, so the restorer works through the saved identifiers and asks the application for each one in turn. An empty `Optional` means the identifier cannot be reconstructed, and the restorer continues without that dockable.

The returned `DockableState` is not itself a `Dockable`. The framework builds the `Dockable` with `DockBuilding.dockable(identifier)` and applies values the state carries onto it, leaving the rest at their defaults. All eight are optional:

| Carried value | Kind |
|---------------|------|
| `title`, `tooltip`, `dragGroupMask`, `isClosable` | Plain data, copied onto the dockable |
| `dockableNode` | A live JavaFX `Node`, handed to the dockable and then owned by it |
| `dockableIconFactory`, `dockableMenuFactory` | Live factories the dockable calls as needed |
| `dockableConsumer` | A live `Consumer<Dockable>` applied to the finished dockable |

Carrying a live node rather than instructions for building one has two consequences that affect how a provider is written:

1. A state cannot be created before JavaFX is ready.
2. A state instance can be used exactly once.

Both are covered in additional detail below.

<h2 id="inline-vs-provider">Inline Creation Versus a Provider</h2>

The basic demo creates dockables inline and hands them straight to the layout:

```java
private Dockable buildDockable(DockBuilding builder, int s, int i, String title) {
    Dockable dockable = builder.dockable();
    dockable.setTitle(title);
    dockable.setIconFactory(d -> makeIcon(s, i));
    dockable.setNode(new Label("<" + title + ":" + i + ">"));
    // ...
    return dockable;
}
```

`BoxApp.buildDockable` is called once per dockable and `Dockable`s are added to the layout in-line, according to their position:

```java
leafWorkspaceTools.addDockables(
    buildDockable(builder, 1, 0, "Workspace"),
    buildDockable(builder, 1, 1, "Bookmarks"),
    buildDockable(builder, 1, 2, "Modifications")
);
```

This works because the application is the only thing that ever needs a dockable, and it needs each dockable exactly once. Identity is implied at the call site: nothing else ask for "the Workspace dockable", because nothing recorded its identity.

The persistence demo inverts the direction. Instead of the application creating content and pushing it into a layout, the framework asks for content by identifier and the application answers:

```java
@Override
public Optional<DockableState> resolveDockableState(String id) {
    return DockableProperties.findByIdentifier(id)
            .map(this::buildDockableState);
}
```

Three consequences follow, and they are the substance of the difference:

1. **Identity must be explicit and stable.** The identifier is the only thing that survives a save, so a dockable has to be restorable using its identifier rather than its position. See [Choosing Stable Identifiers](guide.md#choosing-stable-identifiers).
2. **Construction must be deferred.** A provider is typically built during application startup, where the constructor may run on the JavaFX-Launcher thread and JavaFX components cannot be created. Build the state when `resolveDockableState` is called, not when the provider is constructed.
3. **State must be built fresh on each call, not cached.** A `DockableState` carries one node instance, and a JavaFX node has one parent. Handing the same state to two restored components would move the node into the second layout and leave the first showing a blank panel. Nothing prevents a `Node` from being cached, but the `DocakableState` must be built fresh on each call.

The demo's provider does all three: it looks the identifier up in `DockableProperties`, and builds a new `DockableState` per call through `DockableStateBuilder`.

Applications are not obliged to abandon inline creation. A persistent application can still build its initial layout inline, as long as the same content is also reachable by identifier through a provider when a saved layout is restored later.

The complete set of provider interfaces:

| Provider | Purpose | Required                               | Framework implementation |
|----------|---------|----------------------------------------|--------------------------|
| `BentoProvider` | Supplies the `Bento` instances whose layouts should be saved and restored. | Yes                                    | `DefaultBentoProvider` in `persistence-core` |
| `DockableStateProvider` | Resolves a persisted `Dockable` identifier to a `DockableState` that can be used to reconstruct the runtime `Dockable`. | Yes, to restore                        | None |
| `DockableMenuFactoryProvider` | Looks up a `DockableMenuFactory` by identifier, for application code building a `DockableState`. | Optional, never called by the framework | None |
| `DockContainerLeafMenuFactoryProvider` | Supplies `DockContainerLeafMenuFactory` instances when restored leaves need context menus. | Optional, nullable                     | None |
| `StageIconImageProvider` | Supplies stage icons for restored drag/drop stages. | Optional, nullable                     | None |
| `DockingLayoutPersistenceProvider` | Supplies the application-facing `LayoutSaver` and `LayoutRestorer`. | Yes                                    | `DefaultDockingLayoutPersistenceProvider` in `persistence-core` |
| `LayoutCodecProvider` | Supplies the codec used to encode and decode persisted layout state and exposes a stable provider identifier. | Yes, discoverable                      | `JsonLayoutCodecProvider` in `persistence-codec-json`, `XmlLayoutCodecProvider` in `persistence-codec-xml` |
| `LayoutStorageProvider` | Supplies the storage destination used to read and write persisted layout state and exposes a stable provider identifier. | Yes, discoverable                      | `FileLayoutStorageProvider` in `persistence-storage-file`, `DatabaseLayoutStorageProvider` in `persistence-storage-db-h2` |

The `Required` column describes what `getLayoutSaver` and `getLayoutRestorer` expect:

* **Yes** means the parameter is non-null. `BentoProvider` is required by both methods; `DockableStateProvider` is required by `getLayoutRestorer` and is not a parameter of `getLayoutSaver`, since saving needs no dockable resolution.
* **Optional, nullable** means the parameter is annotated `@Nullable` and may be omitted. Pass `null` when restored leaves need no context menus, or when restored drag/drop stages need no icon.
* **Yes, discoverable** means the provider must be present but is not passed in. The codec and storage providers are selected from the runtime dependencies on the module path, so an application including exactly one of each need not name them. Name them explicitly with a `LayoutPersistenceProfile` when more than one is present.
* **Optional, never called by the framework** applies to `DockableMenuFactoryProvider` alone, and means what it says: no persistence code calls it, and it is not a parameter of `getLayoutSaver` or `getLayoutRestorer`. A restored dockable gets its context menu from `DockableState.dockableMenuFactory`, which the application sets while building the state. The interface is offered because looking up a menu factory by identifier is a common thing to need at that point, but an application can set the factory directly and never implement it. The persistence demo does implement one, and passes it to `BoxAppDockableStateProvider` as a `@Nullable` constructor argument.

The four providers with no framework implementation are only answerable by the application, since they map persisted identifiers back onto the application's own runtime objects.

`DefaultBentoProvider` collects `Bento` instances against their identifiers and holds them by weak reference. Instances can be passed to the constructor or added later with `addBento`.

The primary interface for interacting with persistence framework is the `DockingLayoutPersistenceProvider`, which provides access to a `LayoutSaver` and `LayoutRestorer`.

`DefaultDockingLayoutPersistenceProvider`, the default `DockingLayoutPersistenceProvider` implementation provided by the persistence framework, can be constructed directly:

```java
final DockingLayoutPersistenceProvider persistenceProvider =
        new DefaultDockingLayoutPersistenceProvider();
```

The `DefaultDockingLayoutPersistenceProvider` uses `ServiceLoader` to discover `LayoutCodecProvider` and `LayoutStorageProvider` implementations available on the runtime module path (or classpath for non-modularized applications). The provider is selected deterministically: an explicitly requested provider identifier wins, otherwise a single available provider is used, otherwise a single provider marked as default is used. Ambiguous configurations fail with a `BentoStateException` that lists the available provider identifiers.

Once the `DockingLayoutPersistenceProvider` is acquired, it can be used to acquire `LayoutSaver` and `LayoutRestorer` implementations. The simplest form uses the codec and storage providers selected from runtime dependencies:

```java
final LayoutSaver layoutSaver = persistenceProvider.getLayoutSaver(
        layoutIdentifier,
        bentoProvider
);

final LayoutRestorer layoutRestorer = persistenceProvider.getLayoutRestorer(
        layoutIdentifier,
        bentoProvider,
        dockableStateProvider,
        stageIconImageProvider,               // Nullable
        dockContainerLeafMenuFactoryProvider  // Nullable
);
```

Applications that include multiple codec or storage implementations can select specific providers with a `LayoutPersistenceProfile`:

```java
final LayoutPersistenceProfile profile = new LayoutPersistenceProfile(
        layoutIdentifier,
        "json",            // codec identifier
        "file"             // storage identifier
);

final LayoutSaver layoutSaver = persistenceProvider.getLayoutSaver(
        profile,
        bentoProvider
);
```

This makes simple dependency-only replacement possible while still allowing future application features to save and restore multiple layouts using different codec or storage providers.

