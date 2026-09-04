# Provider Interfaces

[&larr; Back to the BentoFX Persistence guide](guide.md)

Applications supply provider implementations to save and restore docking layouts. These providers act as factories or lookup services for runtime objects that cannot be serialized directly.

> <span style="font-size: 1.5em;">💡</span>  `DockableStateProvider` is the only required provider interface for which the framework does not include a default implementation.

- [The DockableStateProvider Interface](#dockablestateprovider)
- [Inline Creation Versus a Provider](#inline-vs-provider)
- [All Provider Interfaces](#provider-interfaces)
- [Provider Responsibilities](#provider-responsibilities)
- [Provider Lifecycle](#provider-lifecycle)

<h2 id="dockablestateprovider">The `DockableStateProvider` Interface</h2>

`DockableStateProvider` has a single method: `Optional<DockableState> resolveDockableState(String id);`

A persisted layout records *which* dockables were open, never what was inside them, so the restorer works through the saved identifiers and asks the application for each one in turn. An empty `Optional` means the identifier cannot be reconstructed, and the restorer continues without that dockable.

The returned `DockableState` is not itself a `Dockable`. The framework builds the `Dockable` with `DockBuilding.dockable(identifier)` and applies values carried by the state to the `Dockable`, leaving unspecified state attributes at their default values. All eight are optional:

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
// Creates a Dockable
private Dockable buildDockable(DockBuilding builder, int s, int i, String title) {
    Dockable dockable = builder.dockable();
    dockable.setTitle(title);
    dockable.setIconFactory(d -> makeIcon(s, i));
    dockable.setNode(new Label("<" + title + ":" + i + ">"));
    // ...
    return dockable;

// Creates Dockables inline and hands them straight to the layout
leafWorkspaceTools.addDockables(
    buildDockable(builder, 1, 0, "Workspace"),
    buildDockable(builder, 1, 1, "Bookmarks"),
    buildDockable(builder, 1, 2, "Modifications")
);
```

This works because the application is the only thing that ever needs a dockable, and it needs each dockable exactly once. Identity is implied by the position the Dockable is placed at the call site and nothing else can ask for "the Workspace dockable", because nothing recorded its identity or position.

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
3. **State must be built fresh on each call, not cached.** A `DockableState` carries one node instance, and a JavaFX node has only one parent. Handing the same state to two restored components would move the node into the second layout (parent) and leave the first layout (parent) showing a blank panel. Nothing prevents a `Node` from being cached, but the `DocakableState` must be built fresh on every call.

The demo's provider does all three: it looks the identifier up in `DockableProperties`, and builds a new `DockableState` per call through `DockableStateBuilder`.

> <span style="font-size: 1.5em;">💡</span>  Applications are not required to abandon inline creation. A persistent application can still build its initial layout inline, as long as the same content is also reachable by identifier through a provider when a saved layout is restored later.

<h2 id="provider-interfaces">All Provider Interfaces</h2>

The complete set of provider interfaces:

| Provider | Purpose | Required                               | Framework implementation |
|----------|---------|----------------------------------------|--------------------------|
| `BentoProvider` | Supplies the `Bento` instances whose layouts should be saved and restored. | Required, both methods | `DefaultBentoProvider` in `persistence-core` |
| `DockableStateProvider` | Resolves a persisted `Dockable` identifier to a `DockableState` that can be used to reconstruct the runtime `Dockable`. | Required, restore only | None |
| `DockableMenuFactoryProvider` | Looks up a `DockableMenuFactory` by identifier, for application code building a `DockableState`. | Not used by the framework | None |
| `DockContainerLeafMenuFactoryProvider` | Supplies `DockContainerLeafMenuFactory` instances when restored leaves need context menus. | Optional, restore only | None |
| `StageIconImageProvider` | Supplies stage icons for restored drag/drop stages. | Optional, restore only | None |
| `DockingLayoutPersistenceProvider` | Supplies the application-facing `LayoutSaver` and `LayoutRestorer`. | Not passed; the entry point | `DefaultDockingLayoutPersistenceProvider` in `persistence-core` |
| `LayoutCodecProvider` | Supplies the codec used to encode and decode persisted layout state and exposes a stable provider identifier. | Not passed; discovered | `JsonLayoutCodecProvider` in `persistence-codec-json`, `XmlLayoutCodecProvider` in `persistence-codec-xml` |
| `LayoutStorageProvider` | Supplies the storage destination used to read and write persisted layout state and exposes a stable provider identifier. | Not passed; discovered | `FileLayoutStorageProvider` in `persistence-storage-file`, `DatabaseLayoutStorageProvider` in `persistence-storage-db-h2` |

The two methods the `Required` column refers to are `getLayoutSaver` and `getLayoutRestorer`. `Optional` means the parameter is annotated `@Nullable`: pass `null` when restored leaves need no context menus, or when restored drag/drop stages need no icon.

**Not passed; discovered** applies to the codec and storage providers. Both must be present, but neither is passed in: they are selected from the runtime dependencies on the module path, so an application including exactly one of each need not name either. Name them explicitly with a `LayoutPersistenceProfile` when more than one is present.

**Not used by the framework** applies to `DockableMenuFactoryProvider` alone. A restored dockable takes its context menu from `DockableState.dockableMenuFactory`, which the application sets while building the state. The interface exists because looking up a menu factory by identifier is a common thing to need at that point, but an application can set the factory directly and never implement it. The persistence demo does implement one, and passes it to `BoxAppDockableStateProvider` as a `@Nullable` constructor argument.

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

<h2 id="provider-responsibilities">Provider Responsibilities</h2>

The persistence framework restores the BentoFX layout structure. Application providers restore application-specific runtime objects that cannot be serialized safely.

Providers can create objects statically, dynamically, eagerly, lazily, through dependency injection, or by any other mechanism. The important requirement is that a persisted identifier must resolve to the same kind of runtime object whenever the layout is restored. Which providers exist, which are optional, and which already have framework implementations are listed under [Provider Interfaces](providers.md#provider-interfaces).

When providers create JavaFX objects, they must follow JavaFX threading rules. The persistence demo's `DockableState` values contain JavaFX controls, and a JavaFX `Application` constructor runs on the JavaFX-Launcher thread, so the demo builds them on first use rather than during construction. Both callers of `resolveDockableState` - the application while it starts, and the restorer through the persistence framework - are already on the JavaFX Application Thread:

```java
@Override
public Optional<DockableState> resolveDockableState(String id) {
    if (dockableStateMap.isEmpty()) {
        putDockableStates();
    }

    return Optional.ofNullable(dockableStateMap.get(id));
}
```

Scheduling the same work with `Platform.runLater(...)` from a constructor also puts it on the right thread, but it leaves the map empty until the queued task runs, so every lookup then depends on the order in which JavaFX drains its queue. Building on first use removes that question.

<h2 id="provider-lifecycle">Provider Lifecycle</h2>

Provider implementations are typically application singletons or long-lived services created during startup.

Applications generally create providers before layout restoration and reuse them for the lifetime of the application. Providers should not assume that layout restoration occurs only once. A provider may be called repeatedly whenever layouts are restored, when a default layout is created, or when users to switch saved layouts.

Providers should also avoid storing stale JavaFX objects when those objects are meant to be recreated. If a provider caches runtime content, the cache lifecycle should match the application lifecycle and JavaFX threading rules.
