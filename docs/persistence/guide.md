# BentoFX Persistence

[&larr; Back to the BentoFX README](../../README.md)

The [persistence](../../persistence) modules, herein referred to as the "persistence framework", or just "framework", supplements docking by saving and restoring BentoFX docking layouts across application executions. The framework saves the structure of the docking layout, selected dockables, divider positions, collapsed containers, and drag/drop stages. The framework does **not** serialize non-docking components such as JavaFX `Node`, `Stage`, and `Menu` nor does it serialize application-domain objects.

To further support persisting docking layouts, the framework offers a ready-to-use [LayoutsMenu](#layouts-menu) for applications to include in their own menu bars, allowing users to save, restore, and manage custom layouts. Menu text comes from a `ResourceBundle`, so applications can translate or otherwise replace wording to suit application specific requirements.

Application developers control the serialized format and storage destination by adding runtime dependencies for codec and storage provider implementations. In the common case, changing from one codec or storage implementation to another only requires changing runtime dependencies, not application code.

> <span style="font-size: 1.5em;">💡</span> A saver and a restorer each work with one layout, named by a layout identifier, in one format at one storage destination. Applications can use multiple codecs and storage locations because the codec and the storage destination are chosen per saver and per restorer with `LayoutPersistenceProfile`. To offer users a list of saved layouts, see [Managing Several Layouts](#managing-several-layouts).

## Table of Contents

- [Usage](#persistence-usage)
  - [Gradle (Groovy DSL)](#persistence-gradle-groovy-dsl)
  - [Gradle (Kotlin DSL)](#persistence-gradle-kotlin-dsl)
  - [Maven](#persistence-maven)
- [Quick Start](#persistence-quick-start)
- [Concepts](#persistence-concepts)
  - [Provider Interfaces](#provider-interfaces)
    - [Inline Creation Versus a Provider](#inline-vs-provider)
  - [Application Design for Persistence](#application-design-for-persistence)
  - [Choosing Stable Identifiers](#choosing-stable-identifiers)
  - [Provider Responsibilities](#provider-responsibilities)
  - [Provider Lifecycle](#provider-lifecycle)
- [Common Tasks](#common-tasks)
  - [Configuring Storage Location](#configuring-storage-location)
  - [Restoring the Layout](#restoring-the-layout)
  - [Saving the Layout](#saving-the-layout)
  - [Managing Several Layouts](#managing-several-layouts)
  - [A Ready-Made Layouts Menu](#layouts-menu)
    - [Changing the Text](#layouts-menu-text)
- [Runtime Considerations](#runtime-considerations)
  - [JavaFX Application Thread](#javafx-application-thread)
  - [Application Evolution](#application-evolution)
- [Extending Persistence](#extending-persistence)

For internals and diagrams, see [Implementation](implementation.md), [Diagrams](diagrams.md), and the [Undo/Redo design note](undo-redo.md).

<h3 id="persistence-usage">Usage</h3>

In addition to the `core` module, applications using persistence need:

* `persistence-core`
* one codec implementation, such as `persistence-codec-json` or `persistence-codec-xml`
* one storage implementation, such as `persistence-storage-file` or `persistence-storage-db-h2`

For debugging purposes, applications can also enable logging in the persistence modules by adding an optional [SLF4J runtime dependency](https://www.slf4j.org/manual.html#swapping). Depending on which SLF4J implementation is used, applications might also need to include a logging configuration file. See [logging.properties](../../demos/persistence/src/main/resources/logging.properties) in the persistence demo project for an example Java Utils Logging (JUL) configuration file.

The codec and storage provider implementations are discovered at runtime using the Java [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) and Service Provider Interfaces (SPIs). When exactly one codec provider and one storage provider are available, the default persistence provider selects them automatically. When multiple providers are available, applications can select providers explicitly using `LayoutPersistenceProfile`; otherwise, the framework uses a single default provider when one is available or fails with a configuration error.

<h4 id="persistence-gradle-groovy-dsl">Gradle (Groovy DSL)</h4>

```groovy
implementation 'software.coley.bento-fx:persistence-core:${version}'
runtimeOnly 'software.coley.bento-fx:persistence-codec-xml:${version}'
runtimeOnly 'software.coley.bento-fx:persistence-storage-file:${version}'
// (optional example to enable persistence logging using JUL)
runtimeOnly 'org.slf4j:slf4j-jdk14:${slf4j-version}'
```

<h4 id="persistence-gradle-kotlin-dsl">Gradle (Kotlin DSL)</h4>

```kotlin
implementation("software.coley.bento-fx:persistence-core:${version}")
runtimeOnly("software.coley.bento-fx:persistence-codec-xml:${version}")
runtimeOnly("software.coley.bento-fx:persistence-storage-file:${version}")
// (optional example to enable persistence logging using JUL)
runtimeOnly("org.slf4j:slf4j-jdk14:${slf4j-version}")
```

<h4 id="persistence-maven">Maven</h4>

```xml
<dependency>
    <groupId>software.coley.bento-fx</groupId>
    <artifactId>persistence-core</artifactId>
    <version>${version}</version>
</dependency>
<dependency>
    <groupId>software.coley.bento-fx</groupId>
    <artifactId>persistence-codec-xml</artifactId>
    <version>${version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>software.coley.bento-fx</groupId>
    <artifactId>persistence-storage-file</artifactId>
    <version>${version}</version>
    <scope>runtime</scope>
</dependency>
<!-- optional example to enable persistence logging using JUL -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-jdk14</artifactId>
    <version>${slf4j-version}</version>
    <scope>runtime</scope>
</dependency>
```

<h3 id="persistence-quick-start">Quick Start</h3>

A persistent application generally follows this startup flow:

1. Create the application's `Bento` with a stable identifier.
2. Register the `Bento` with a `BentoProvider`.
3. Create provider implementations for dockable states, dockable menus, leaf menus, and stage icons as needed. Only [`DockableStateProvider`](#provider-interfaces) has no framework implementation, so it is the one an application must implement for itself.
4. Build the application's default `DockingLayout` using the same providers that restoration will use.
5. Ask [`LayoutRestorer`](#restoring-the-layout) to restore the last saved layout, passing the default layout supplier as the fallback when one does not exist or cannot be restored.
6. Apply the returned `DockingLayout` to the JavaFX stage.
7. Obtain a [`LayoutSaver`](#saving-the-layout) and keep it. The default `LayoutSaver` provided by the framework runs auto-save for the duration of the application execution. Accordingly, the LayoutSaver should be acquired/instantiated after the layout is applied, because a save reads the root branches that have a `Scene`.
8. Save the layout and close the saver before windows are closed.

An application that does not want layouts to be auto-saved skips steps 7 and 8, and calls `DockingLayoutPersistenceProvider.saveLayout(...)` instead, which saves once and holds nothing. See [Saving the Layout](#saving-the-layout).

The [persistence demo](../../demos/persistence) follows exactly this pattern and is the fastest way to see it working:

```bash
./gradlew :demos:persistence:run
```

Its `BoxApp` is derived from the [basic demo](../../demos/basic/src/main/java/demo/BoxApp.java), so a diff between the two shows what persistence adds. `BoxApp.applyDockingLayout(DockingLayout)` and `BoxApp.saveDockingLayout(WindowEvent)` are the two methods worth reading first.

The basic demo focuses on container construction and docking behavior. The persistence demo adds provider-backed reconstruction so layouts survive across executions, and deliberately introduces abstractions the core docking framework does not need, such as providers and state objects. For a row-by-row comparison, see [Basic demo vs persistence demo](implementation.md#basic-demo-vs-persistence-demo).

<h3 id="persistence-concepts">Concepts</h3>

For additional implementation details and diagrams, see:

- [Docking Layout Persistence Implementation](implementation.md)
- [Bento Layout Persistence Diagrams](diagrams.md)

The persistence framework has two responsibilities:

1. Save the current BentoFX container graph into serializable state.
2. Restore that state into runtime BentoFX objects.

The framework can save and restore BentoFX layout structure, but the application must still know how to create its own runtime content. For that reason, persistent applications should construct dockable states through stable identifiers and providers rather than only creating dockables inline. Runtime content can be created statically, dynamically, eagerly, lazily, through dependency injection, or by any other mechanism chosen by the application developer. During restoration, providers are given the identifier associated with a persisted object and are expected to return the corresponding runtime object, if one can be reconstructed.

<h4 id="provider-interfaces">Provider Interfaces</h4>

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

<h5 id="inline-vs-provider">Inline Creation Versus a Provider</h5>

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

1. **Identity must be explicit and stable.** The identifier is the only thing that survives a save, so a dockable has to be restorable using its identifier rather than its position. See [Choosing Stable Identifiers](#choosing-stable-identifiers).
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

<h4 id="application-design-for-persistence">Application Design for Persistence</h4>

A persistent application should separate two concerns:

1. **dockable creation**: how to construct a runtime `Dockable` for a stable identifier
2. **dockable placement**: where that dockable appears in the current layout

The persistence demo shows this separation. It statically defines stable dockable identifiers in `DockableProperties` as `enum` values, resolves those identifiers through `BoxAppDockableStateProvider`, and then builds runtime `Dockable` instances from the resolved `DockableState`.

```java
dockableStateProvider.resolveDockableState(dockableProperties.getIdentifier())
        .ifPresent(dockableState ->
                container.addDockable(buildDockable(dockableState)));
```

That same provider is passed to the `LayoutRestorer`, so the default layout and the restored layout use one dockable resolution strategy between them. The default layout places dockables for the first run; the restored layout uses persisted placement and asks the same provider to recreate each dockable by identifier.

<table>
<tr><th align="left">Default layout startup</th><th align="left">Saved layout restore</th></tr>
<tr valign="top"><td><pre>
Dockable identifier
    -> DockableStateProvider
        -> DockableState
            -> BoxApp.buildDockable(DockableState)
                -> Dockable
                    -> Added to default container
</pre></td><td><pre>
Persisted dockable identifier
    -> DockableStateProvider
        -> DockableState
            -> LayoutRestorer builds the Dockable
                -> Dockable
                    -> Added to restored container
</pre></td></tr>
</table>

The middle of each flow is identical, and that is the whole point. Only where the identifier comes from, and who builds the `Dockable`, differ.

Application code therefore needs no separate implementation for first-run dockables and restored ones. If a dockable can be created for the default layout, it can also be recreated when the persisted layout refers to the same identifier.

<h4 id="choosing-stable-identifiers">Choosing Stable Identifiers</h4>

Identifiers should remain stable across application executions and software upgrades.

Identifiers may represent application views, tools, documents, domain objects, or any other concept that can be consistently reconstructed by the application.

Examples include:

- `projects`
- `terminal`
- `editor`
- `workspace-explorer`
- `document:12345`
- `customer:98765`
- `order:ABC123`

Avoid identifiers based on:

- runtime object instances
- memory addresses
- hash codes
- generated UUIDs that change between executions
- timestamps
- localized display labels
- values that change between application executions

Changing an identifier effectively creates a new dockable from the perspective of the persistence framework and may prevent previously saved layouts from restoring correctly. If an application intentionally renames an identifier, the `DockableStateProvider` should consider mapping the old identifier to the new runtime object when backward compatibility is required.

Applications should also consider how identifiers are resolved when the underlying object is no longer available. For example, a persisted identifier may refer to a document, record, or domain object that no longer exists when a layout is restored. In such cases, providers may choose to return an alternative dockable, a placeholder dockable, or no dockable at all, depending on the application's requirements.

<h4 id="provider-responsibilities">Provider Responsibilities</h4>

The persistence framework restores the BentoFX layout structure. Application providers restore application-specific runtime objects that cannot be serialized safely.

Providers can create objects statically, dynamically, eagerly, lazily, through dependency injection, or by any other mechanism. The important requirement is that a persisted identifier must resolve to the same kind of runtime object whenever the layout is restored. Which providers exist, which are optional, and which already have framework implementations are listed under [Provider Interfaces](#provider-interfaces).

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

<h4 id="provider-lifecycle">Provider Lifecycle</h4>

Provider implementations are typically application singletons or long-lived services created during startup.

Applications generally create providers before layout restoration and reuse them for the lifetime of the application. Providers should not assume that layout restoration occurs only once. A provider may be called repeatedly whenever layouts are restored, when a default layout is created, or when users to switch saved layouts.

Providers should also avoid storing stale JavaFX objects when those objects are meant to be recreated. If a provider caches runtime content, the cache lifecycle should match the application lifecycle and JavaFX threading rules.

<h3 id="common-tasks">Common Tasks</h3>

<h4 id="configuring-storage-location">Configuring Storage Location</h4>

By default, the included `persistence-storage-file` and `persistence-storage-db-h2` providers keep their data under `<user.home>/.bentofx`. Two situations call for changing that:

* an application wants its data somewhere more appropriate than the user's home directory, such as a platform-specific application-data directory in a packaged desktop build.
* more than one BentoFX-based application runs on the same machine. Left unconfigured, they share the same default directory and the same H2 database file, so one application's layouts collide with another's.

Both providers resolve their location through `software.coley.bentofx.persistence.core.api.storage.LayoutStorageLocations`, which reads two settings fresh on every call. Each can be given either as a `System` property or as an environment variable of the matching name - the property always wins when both are set:

| System property | Environment variable | Effect |
|------------------|-----------------------|--------|
| `bentofx.persistence.home` | `BENTOFX_PERSISTENCE_HOME` | Overrides the base directory, in place of `<user.home>/.bentofx`. |
| `bentofx.persistence.namespace` | `BENTOFX_PERSISTENCE_NAMESPACE` | Names a subdirectory of the resolved home that is this application's own, so a different BentoFX-based application on the same machine does not share it. |

The environment variable form needs no application code at all - set it before the process starts, the same way `JAVA_HOME` or `GRADLE_USER_HOME` work, and the next storage provider that resolves its location picks it up. The persistence demo's [Runner.java](../../demos/persistence/src/main/java/software/coley/boxfx/demo/persistence/Runner.java) sets its namespace this way, in code, to show the alternative: `LayoutStorageLocations.configureNamespace("persistence-demo")`, along with `configureHome(Path)`, are typed alternatives to calling `System.setProperty` directly. Whichever way, this has to happen before the first save, restore, or catalog call - in practice, before `DockingLayoutPersistence.provider()` is first called - since that is when a storage provider actually reads the location:

```java
LayoutStorageLocations.configureNamespace("my-app");

final DockingLayoutPersistenceProvider persistence =
        DockingLayoutPersistence.provider();
```

<h4 id="restoring-the-layout">Restoring the Layout</h4>

The `LayoutRestorer` restores the last saved layout when one exists. If no persisted layout exists, or if decoding fails, the default layout supplier is used.

A restorer owns the `LayoutStorage` it was given and closes it, so obtain it as a resource. The layout it returns is fully built by the time `restoreLayout` returns, so closing the storage afterwards costs nothing:

```java
private DockingLayout getDockingLayout() {
    try (final LayoutRestorer layoutRestorer =
                 persistenceProvider.getLayoutRestorer(
                         DEFAULT_LAYOUT_IDENTIFIER,
                         bentoProvider,
                         dockableStateProvider,
                         stageIconImageProvider,
                         dockContainerLeafMenuFactoryProvider
                 )) {

        return layoutRestorer.restoreLayout(this::getDefaultDockingLayout);
    } catch (BentoStateException e) {
        logger.warn("Could not create the docking layout restorer.", e);
        return getDefaultDockingLayout();
    }
}
```

Unlike a saver, a restorer holds no scheduler and no listeners, so building one per restore is inexpensive.

Applying the returned layout can still fail. For example, a stored layout may hold root branches the application does not know how to place. In such instances, report whether anything was applied and fall back to the default layout, because a stage that never receives a `Scene` is never shown, and an application whose only exit path runs when its window hides cannot then be closed:

```java
DockingLayout dockingLayout = getDockingLayout();

if (!applyDockingLayout(dockingLayout)) {
    logger.warn(
            "Could not apply the restored docking layout; " +
                    "applying the default docking layout instead."
    );

    if (!applyDockingLayout(getDefaultDockingLayout())) {
        logger.error("Could not apply the default docking layout.");
    }
}
```

`applyDockingLayout` above is not framework API. It is the application's own helper, and it returns whether it managed to apply anything, which is what makes the fallback decision above possible:

```java
private boolean applyDockingLayout(final DockingLayout dockingLayout) {
    boolean isApplied = false;

    for (final BentoLayout bentoLayout : dockingLayout.getBentoLayouts()) {
        if (bentoLayout.matchesIdentity(bento)) {
            isApplied |= applyBentoLayout(bentoLayout);
        }
    }

    return isApplied;
}
```

The persistence demo applies the matching `BentoLayout` to the primary stage this way, then shows any restored `DragDropStage` instances.

<h4 id="saving-the-layout">Saving the Layout</h4>

The default `LayoutSaver` implementation also supports automatic scheduled saves. A saver obtained from a `DockingLayoutPersistenceProvider` arrives with auto-save running: it listens for `DockEvent` notifications from every `Bento` supplied by the `BentoProvider` and schedules a save task at a default interval of five minutes. `AbstractAutoCloseableLayoutSaver` deliberately does not start auto-save from its constructor, because that would hand a partly-built object to a scheduler thread and to every `Bento` event bus; a saver constructed directly starts auto-save with `enableAutoSave(long, TimeUnit)`.

The scheduled task does not write the layout on every interval. It first checks whether any `DockEvent` was received since the previous save attempt. If no docking changes were detected, the task skips the save operation.

Because auto-save runs for as long as the saver exists, obtain one saver while the application starts and keep it. A saver built where the layout is saved arms a scheduler for an application that is already exiting.

Applications should still save explicitly during shutdown, because the scheduled task may not have run after the user's most recent layout change. Save before the primary stage and any secondary stages are closed, since closed windows are no longer discoverable by the saver.

Closing the saver is what removes its listener from each `Bento`, stops the scheduler, and releases the storage it was given. Close it while the windows still exist. An application whose exit path calls `System.exit(...)` never runs `Application.stop()`, so the close request handler is the last point at which closing still happens:

```java
stage.setOnCloseRequest(this::saveDockingLayout);
```

```java
private void saveDockingLayout(final WindowEvent windowEvent) {
    final LayoutSaver saver = layoutSaver;

    try (saver) {
        if (saver == null) {
            return;
        }
        saver.saveLayout();
    } catch (final BentoStateException e) {
        logger.warn("Could not save the docking layout.", e);
    }
}
```

Saving explicitly before the close is deliberate: closing saves only when a `DockEvent` has arrived since the last save, and an application usually wants a layout written on exit either way.

<h4 id="managing-several-layouts">Managing Several Layouts</h4>

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

<h4 id="layouts-menu">A Ready-Made Layouts Menu</h4>

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

<h5 id="layouts-menu-text">Changing the Text</h5>

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

<h3 id="runtime-considerations">Runtime Considerations</h3>

Applications should keep two runtime considerations in mind when using
layout persistence.

<h4 id="javafx-application-thread">JavaFX Application Thread</h4>

Restoration separates persistence operations from JavaFX object creation.
Storage access and codec decoding can occur independently of the JavaFX
Application Thread, but JavaFX objects must still be created and modified
according to normal JavaFX threading rules.

The persistence framework restores BentoFX runtime objects on the JavaFX
Application Thread when those objects are created by the framework.

Application providers have the same responsibility for the objects they
supply. If a provider creates JavaFX objects such as `Node`, `Tooltip`,
`ContextMenu`, `MenuItem`, `Image`, or `Stage`, those objects should be
created on the JavaFX Application Thread.

The persistence demo builds its `DockableState` instances on first use,
because those states contain JavaFX controls and the thread that
constructs a provider is not necessarily the JavaFX Application Thread.
Applications using lazy loading, dependency injection, or other dynamic
resolution strategies should apply the same principle whenever providers
create JavaFX runtime objects.

<h4 id="application-evolution">Application Evolution</h4>

Persisted layouts may outlive individual software releases.

Applications should therefore treat identifiers as long-lived contracts
between persisted layouts and runtime objects. When identifiers,
dockable types, menu structures, or other persisted concepts change,
applications should maintain backward compatibility where practical.

For example, if an identifier changes from `workspace` to `projects`,
the `DockableStateProvider` can continue accepting `workspace` and
return the newer dockable state. This allows previously saved layouts
to continue restoring successfully after an application upgrade.

The stored format carries a schema version of its own, and the framework
reads every version up to the one it writes. **The current version is 1**,
and it is the only version there has been, so it covers everything the stored
metadata holds, a layout's group and the group catalog included. A layout
written by a newer framework is refused by an older one, with a message naming
the version rather than a parse failure, so downgrading an application strands
the layouts saved by the newer one.

Providers are the primary mechanism for adapting persisted layouts to
application changes until explicit layout migration support is added.

<h3 id="extending-persistence">Extending Persistence</h3>

The `DefaultDockingLayoutPersistenceProvider` uses `ServiceLoader` to acquire `LayoutCodecProvider` and `LayoutStorageProvider` implementations from the runtime module path, or from the classpath for non-modularized applications. Provider identifiers allow applications to select a specific codec or storage implementation when more than one implementation is available.

To add a storage destination:

1. Implement the `LayoutStorage` interface:

```java
public class SystemLayoutStorage implements LayoutStorage {
    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public OutputStream openOutputStream() {
        return System.out;
    }

    @Override
    public InputStream openInputStream() {
        return System.in;
    }
}
```

2. Implement the `LayoutStorageProvider` service provider interface:

```java
public class SystemLayoutStorageProvider implements LayoutStorageProvider {
    @Override
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        return new SystemLayoutStorage();
    }
}
```

Four conventions are worth following in a storage implementation, because the bundled implementations follow them and callers rely on them:

1. **Closing the output stream is what stores the layout.** Buffer or stage what is written and publish it only when the stream closes cleanly. A save that fails part way through then leaves the previously stored layout intact instead of replacing it with a fragment.
2. **Override the catalog methods when the destination can answer them.** `LayoutStorageProvider.getLayoutIdentifiers`, `isLayoutStored` and `deleteLayout` all have defaults, so a storage implementation stays valid without them, but an application cannot offer users a list of saved layouts unless the storage it uses can enumerate. Both bundled implementations can: one file per layout, or one row per layout and codec.
3. **`exists()` answers whether there is a layout to read**, not whether a location is present. Empty content is not a layout: a restorer told that a layout exists will try to decode it, and an empty or truncated payload becomes a decode failure where a clean "nothing stored yet" would have produced the default layout.
4. **`close()` releases what the storage owns, and only that.** Whichever saver or restorer receives a `LayoutStorage` closes it, so a storage handed a resource it did not create should leave that resource alone.

The `LayoutStorageProvider` implementation is the type discovered by `ServiceLoader`. It must expose a stable identifier and be compatible with Java's Service Provider Interface (SPI) mechanism. In practice, the provider implementation should:

* be a public concrete class
* have a public no-argument constructor, or an implicit default constructor
* return a stable provider identifier from `getIdentifier()`
* optionally return `true` from `isDefault()` when it should be selected automatically from multiple providers
* be registered with a `provides` clause in the module descriptor

The `LayoutStorage` implementation itself is not discovered directly by `ServiceLoader`; it is created by the `LayoutStorageProvider`. This allows a storage implementation to use constructor arguments or other setup logic when the provider creates it.

3. Register the provider implementation with the module descriptor:

```java
provides LayoutStorageProvider with SystemLayoutStorageProvider;
```

4. Add the module to the application's runtime module path:

```kotlin
runtimeOnly("software.coley.bento-fx:persistence-storage-system:${version}")
```

Codecs are extended similarly by implementing `LayoutCodecProvider` and `LayoutCodec`, registering the provider with the implementation module's descriptor, and adding the module to the application's runtime module path. The same SPI compatibility and identifier requirements apply to the `LayoutCodecProvider`; the `LayoutCodec` implementation is created by the provider and does not need to be directly discoverable by `ServiceLoader`.

For complete examples, refer to these modules:

- [JSON Codec](../../persistence/codec/json)
- [XML Codec](../../persistence/codec/xml)
- [H2 Database Storage](../../persistence/storage/db/h2)
- [File Storage](../../persistence/storage/file)

Additional API and usage documentation can be found in [Docking Layout Persistence Implementation](implementation.md) and [Bento layout persistence diagrams](diagrams.md).

The following are also provided for additional information on using `ServiceLoader`:

* https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
* https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html
* https://www.baeldung.com/java-spi
