[![Build](https://github.com/Col-E/BentoFX/actions/workflows/build.yml/badge.svg)](https://github.com/Col-E/BentoFX/actions/workflows/build.yml)
[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=philliplbryant_BentoFX&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=philliplbryant_BentoFX)
[![Codecov](https://codecov.io/gh/Col-E/BentoFX/graph/badge.svg)](https://codecov.io/gh/Col-E/BentoFX)
[![Maven Central](https://img.shields.io/maven-central/v/software.coley.bento-fx/core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/software.coley.bento-fx/core)

# BentoFX

A docking system for JavaFX.

## Table of Contents

- [Requirements](#requirements)
- [Core Framework](#core-framework)
  - [Usage](#core-usage)
    - [Gradle (Groovy DSL)](#core-gradle-groovy-dsl)
    - [Gradle (Kotlin DSL)](#core-gradle-kotlin-dsl)
    - [Maven](#core-maven)
  - [Overview](#overview)
    - [Containers](#containers)
    - [Controls](#controls)
    - [Dockables](#dockables)
  - [Basic Example](#basic-example)
    - [Construct the Default Docking Layout](#construct-the-default-layout)
    - [Show the Layout](#show-it)
- [Persistence Framework](#persistence)
  - [Usage](#persistence-usage)
    - [Gradle (Groovy DSL)](#persistence-gradle-groovy-dsl)
    - [Gradle (Kotlin DSL)](#persistence-gradle-kotlin-dsl)
    - [Maven](#persistence-maven)
  - [Overview](#persistence-overview)
    - [Provider Interfaces](#provider-interfaces)
    - [Application Responsibilities](#application-responsibilities)
    - [Application Design for Persistence](#application-design-for-persistence)
    - [Choosing Stable Identifiers](#choosing-stable-identifiers)
    - [Provider Responsibilities](#provider-responsibilities)
    - [Provider Lifecycle](#provider-lifecycle)
    - [Recommended Application Startup Flow](#recommended-application-startup-flow)
    - [Saving the Layout](#saving-the-layout)
    - [Restoring the Layout](#restoring-the-layout)
    - [Managing Several Layouts](#managing-several-layouts)
    - [Runtime Considerations](#runtime-considerations)
      - [JavaFX Application Thread](#javafx-application-thread)
      - [Application Evolution](#application-evolution)
    - [Basic Demo vs Persistence Demo](#basic-demo-vs-persistence-demo)
  - [Extending Persistence](#extending-persistence)
  - [Persistence Example](#persistence-example)
- [Next Steps](#next-steps)
    - [Contributing Guide](CONTRIBUTING.md)
    - [Maintainers Guide](MAINTAINERS.md)

## Requirements

- JavaFX 21+
- Java 21+

## Core Framework

The [core](./core) module is a framework of user interface controls that can be used to group, dock, and undock other user interface controls using drag and drop. 

<h3 id="core-usage">Usage</h3>

<h4 id="core-gradle-groovy-dsl">Gradle (Groovy DSL)</h4>

```groovy
implementation 'software.coley.bento-fx:core:${version}'
```

<h4 id="core-gradle-kotlin-dsl">Gradle (Kotlin DSL)</h4>

```kotlin
implementation("software.coley.bentofx:core:${version}")
```

<h4 id="core-maven">Maven</h4>

```xml
<dependency>
    <groupId>software.coley.bento-fx</groupId>
    <artifactId>core</artifactId>
    <version>${version}</version>
</dependency>
```

<h3 id="overview">Overview</h3>

![overview](assets/overview.png)

In terms of hierarchy, the `Node` structure of Bento goes like:

- `DockContainerRootBranch`
    - `DockContainerBranch` _(Nesting levels depends on which kind of implementation used)_
        - `DockContainerLeaf`
            - `Dockable` _(Zero or more)_

Each level of `*DockContainer` in the given hierarchy and `Dockable` instances can be constructed via a `Bento`
instance's builder offered by `bento.dockBuilding()`.

<h4 id="containers">Containers</h4>

![containers](assets/containers.png)

Bento has a very simple model of branches and leaves. Branches hold additional child containers. Leaves
display `Dockable` items and handle drag-n-drop operations.

| Container type        | Description                                                                                                                                                             |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DockContainerBranch` | Used to show multiple child `DockContainer` instances in a `SplitPane` display. Orientation and child node scaling are thus specified the same way as with `SplitPane`. |
| `DockContainerLeaf`   | Used to show any number of `Dockable` instance rendered by a `HeaderPane`.                                                                                              |

<h4 id="controls">Controls</h4>

![controls](assets/controls.png)

Bento comes with a few custom controls that you will want to create a custom stylesheet for to best fit the intended
look and feel of your application.

An example reference sheet _(which is included in the dependency)_ can be found
in [`bento.css`](demos/basic/src/main/resources/bento.css).

| Control                     | Description                                                                                                                                       |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `Header`                    | Visual model of a `Dockable`.                                                                                                                     |
| `HeaderPane`                | Control that holds multiple `Header` children, and displays the currently selected `Header`'s associated `Dockable` content.                      |
| `Headers`                   | Child of `HeaderPane` that acts as a `HBox`/`VBox` holding multiple `Headers`.                                                                    |
| `ButtonHBar` / `ButtonVBar` | Child of `HeaderPane` used to show buttons for the `DockContainerLeaf` for things like context menus and selection of overflowing `Header` items. |

<h4 id="dockables">Dockables</h4>

The `Dockable` can be thought of as the model behind each of a `HeaderPane`'s `Header` _(Much like a `Tab` of
a `TabPane`)_.
It outlines capabilities like whether the `Header` can be draggable, where it can be dropped, what text/graphic to
display,
and the associated JavaFX `Node` to display when placed into a `DockContainerLeaf`.

<h3 id="basic-example">Basic Example</h3>
![containers](assets/example.png)

In this example we create a layout structure that loosely models how an IDE is laid out.
There are tool-tabs on the left and bottom sides. The primary content like Java sources files
reside in the middle and occupy the most space. The tool tabs are intended to be smaller and not
automatically scale when we resize the window since we want the primary content to take up all
of the available space when possible.

We'll first create a vertically split container and put tools like logging/terminal at the bottom.
The bottom section will be set to not resize with the parent for the reason mentioned previously.

The top of the vertical split will hold our primary docking leaf container and the remaining tools.
The tools will go on the left, and the main container on the right via a horizontally split container.
The first item in this horizontal split will show up on the left, so that's where we'll put the tools.
Then the second item will be our primary docking container.

Our primary docking container is a glorified tab-pane, and we'll fill it up with some dummy items as if we
were in the midst of working on some project. These tabs won't have any special properties,
but we'll want to make sure the tools have some additional values set.

All tool tabs will be constructed such that they are not closable and all belong to a shared
drag group called `TOOLS`. Since these tabs all have a shared group they can be dragged
among one another. However, the primary docking container tabs with our _"project files"_ cannot be
dragged into the areas housing our tools. If you try this out in IntelliJ you'll find it
follows the same behavior.

```java
Bento bento = new Bento();
bento.placeholderBuilding().setDockablePlaceholderFactory(dockable -> new Label("Empty Dockable"));
bento.placeholderBuilding().setContainerPlaceholderFactory(container -> new Label("Empty Container"));
bento.events().addEventListener(System.out::println);
DockBuilding builder = bento.dockBuilding();
DockContainerBranch branchRoot = builder.root("root");
DockContainerBranch branchWorkspace = builder.branch("workspace");
DockContainerLeaf leafWorkspaceTools = builder.leaf("workspace-tools");
DockContainerLeaf leafWorkspaceHeaders = builder.leaf("workspace-headers");
DockContainerLeaf leafTools = builder.leaf("misc-tools");

// These leaves shouldn't auto-expand. They are intended to be a set size.
DockContainerBranch.setResizableWithParent(leafTools, false);
DockContainerBranch.setResizableWithParent(leafWorkspaceTools, false);

// Root: Workspace on top, tools on bottom
// Workspace: Explorer on left, primary editor tabs on right
branchRoot.setOrientation(Orientation.VERTICAL);
branchWorkspace.setOrientation(Orientation.HORIZONTAL);
branchRoot.addContainers(branchWorkspace, leafTools);
branchWorkspace.addContainers(leafWorkspaceTools, leafWorkspaceHeaders);

// Changing tool header sides to be aligned with application's far edges (to facilitate better collaps
leafWorkspaceTools.setSide(Side.LEFT);
leafTools.setSide(Side.BOTTOM);

// Tools shouldn't allow splitting (mirroring intellij behavior)
leafWorkspaceTools.setCanSplit(false);
leafTools.setCanSplit(false);

// Primary editor space should not prune when empty
leafWorkspaceHeaders.setPruneWhenEmpty(false);

// Set intended sizes for tools (leaf does not need to be a direct child, just some level down in the 
branchRoot.setContainerSizePx(leafTools, 200);
branchRoot.setContainerSizePx(leafWorkspaceTools, 300);

// Make the bottom collapsed by default
branchRoot.setContainerCollapsed(leafTools, true);

// Adding dockables to the leafs
leafWorkspaceTools.addDockables(
		buildDockable(builder, 1, 0, "Workspace"),
		buildDockable(builder, 1, 1, "Bookmarks"),
		buildDockable(builder, 1, 2, "Modifications")
);
leafTools.addDockables(
		buildDockable(builder, 2, 0, "Logging"),
		buildDockable(builder, 2, 1, "Terminal"),
		buildDockable(builder, 2, 2, "Problems")
);
leafWorkspaceHeaders.addDockables(
		buildDockable(builder, 0, 0, "Class 1"),
		buildDockable(builder, 0, 1, "Class 2"),
		buildDockable(builder, 0, 2, "Class 3"),
		buildDockable(builder, 0, 3, "Class 4"),
		buildDockable(builder, 0, 4, "Class 5")
);
```

<h4 id="show-it">Show it</h4>

```java
Scene scene = new Scene(branchRoot);
scene.getStylesheets().add("/bento.css");
stage.setScene(scene);
stage.setOnHidden(e -> System.exit(0));
stage.show();
```

For a more real-world example you can check out [Recaf](https://github.com/Col-E/Recaf/)

![containers](assets/example-recaf.png)

## Persistence

The [persistence](./persistence) modules supplement the [core](#core-framework) module by saving and restoring BentoFX docking layouts across application executions. The persistence framework saves the structure of the docking layout, selected dockables, divider positions, collapsed containers, and drag/drop stages. It does **not** serialize live JavaFX `Node`, `Stage`, menu, or application-domain objects.

Application developers control the serialized format and storage destination by adding runtime dependencies for codec and storage provider implementations. In the common case, changing from one codec or storage implementation to another only requires changing runtime dependencies, not application code.

> <span style="font-size: 1.5em;">💡</span> A saver and a restorer each work with one layout, named by a layout identifier, in one format at one storage destination. An application can use several: the codec and the storage destination are chosen per saver and per restorer with `LayoutPersistenceProfile`. To offer users a list of saved layouts, see [Managing Several Layouts](#managing-several-layouts).

<h3 id="persistence-usage">Usage</h3>

In addition to the `core` module, applications using persistence need:

* `persistence-api`
* one codec implementation, such as `persistence-codec-json` or `persistence-codec-xml`
* one storage implementation, such as `persistence-storage-file` or `persistence-storage-db-h2`

For debugging purposes, applications can also enable logging in the persistence modules by adding an optional [SLF4J runtime dependency](https://www.slf4j.org/manual.html#swapping). Depending on the SLF4J dependency chosen, applications might also need to include a logging configuration file. See [logging.properties](./demos/persistence/src/main/resources/logging.properties) in the persistence demo project for an example Java Utils Logging (JUL) configuration file.

The codec and storage provider implementations are discovered at runtime using the Java [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) and service provider compatible interfaces. When exactly one codec provider and one storage provider are available, the default persistence provider selects them automatically. When multiple providers are available, applications can select providers explicitly using `LayoutPersistenceProfile`; otherwise, the framework uses a single default provider when one is available or fails with a configuration error.

<h4 id="persistence-gradle-groovy-dsl">Gradle (Groovy DSL)</h4>

```groovy
implementation 'software.coley.bento-fx:persistence-api:${version}'
runtimeOnly 'software.coley.bento-fx:persistence-codec-xml:${version}'
runtimeOnly 'software.coley.bento-fx:persistence-storage-file:${version}'
// (optional example to enable persistence logging using JUL)
runtimeOnly 'org.slf4j:slf4j-jdk14:${slf4j-version}'
```

<h4 id="persistence-gradle-kotlin-dsl">Gradle (Kotlin DSL)</h4>

```kotlin
implementation("software.coley.bento-fx:persistence-api:${version}")
runtimeOnly("software.coley.bento-fx:persistence-codec-xml:${version}")
runtimeOnly("software.coley.bento-fx:persistence-storage-file:${version}")
// (optional example to enable persistence logging using JUL)
runtimeOnly("org.slf4j:slf4j-jdk14:${slf4j-version}")
```

<h4 id="persistence-maven">Maven</h4>

```xml
<dependency>
    <groupId>software.coley.bento-fx</groupId>
    <artifactId>persistence-api</artifactId>
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

<h3 id="persistence-overview">Overview</h3>

For additional implementation details and diagrams, see:

- [Docking Layout Persistence Implementation](docs/persistence/docking-layout-persistence.md)
- [Bento Layout Persistence Diagrams](docs/persistence/docking-layout-persistence-diagrams.md)

The persistence framework has two responsibilities:

1. Save the current BentoFX container graph into serializable state.
2. Restore that state into runtime BentoFX objects.

The framework can save and restore BentoFX layout structure, but the application must still know how to create its own runtime content. For that reason, persistent applications should construct dockables through stable identifiers and providers rather than only creating dockables inline. Runtime content can be created statically, dynamically, eagerly, lazily, through dependency injection, or by any other mechanism. During restoration, providers are given the identifier associated with a persisted object and are expected to return the corresponding runtime object, if one can be reconstructed.

<h4 id="provider-interfaces">Provider Interfaces</h4>

As previously mentioned, to persist docking layouts, applications supply provider implementations that act as factories or lookup services for runtime objects that cannot be serialized directly.

| Provider | Purpose                                                                                                                 |
|----------|-------------------------------------------------------------------------------------------------------------------------|
| `BentoProvider` | Supplies the `Bento` instances whose layouts should be saved and restored.                                              |
| `DockableStateProvider` | Resolves a persisted `Dockable` identifier to a `DockableState` that can be used to reconstruct the runtime `Dockable`. |
| `DockableMenuFactoryProvider` | Supplies `DockableMenuFactory` instances when restored dockables need context menus.                                    |
| `DockContainerLeafMenuFactoryProvider` | Supplies `DockContainerLeafMenuFactory` instances when restored leaves need context menus.                              |
| `StageIconImageProvider` | Supplies stage icons for restored drag/drop stages.                                                                     |
| `DockingLayoutPersistenceProvider` | Supplies the application-facing `LayoutSaver` and `LayoutRestorer`.                                                     |
| `LayoutCodecProvider` | Supplies the codec used to encode and decode persisted layout state and exposes a stable provider identifier.             |
| `LayoutStorageProvider` | Supplies the storage destination used to read and write persisted layout state and exposes a stable provider identifier.  |

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
        "json",
        "file"
);

final LayoutSaver layoutSaver = persistenceProvider.getLayoutSaver(
        profile,
        bentoProvider
);
```

This makes simple dependency-only replacement possible while still allowing future application features to save and restore multiple layouts using different codec or storage providers.

<h4 id="application-responsibilities">Application Responsibilities</h4>

The persistence framework is responsible for restoring BentoFX layout
structure and applying restored state to runtime BentoFX objects.

Applications are responsible for supplying application-specific runtime
objects that cannot be serialized directly.

The persistence framework restores:

- Bento layouts
- Container hierarchies
- Divider positions
- Container collapsed state
- Selected dockables
- Drag/drop stages
- Placement relationships between persisted objects

Applications supply:

- Dockable content
- JavaFX `Node` instances
- Tooltips
- Context menu factories
- Leaf menu factories
- Stage icon factories
- Application-specific state

The framework obtains these application-specific objects through
provider interfaces such as `DockableStateProvider`,
`DockableMenuFactoryProvider`,
`DockContainerLeafMenuFactoryProvider`, and
`StageIconImageProvider`.

For example, during restoration the framework recreates a
`DockContainerLeaf`, asks the application for a menu factory through
`DockContainerLeafMenuFactoryProvider`, and then applies that factory
to the restored leaf. Likewise, the framework restores drag/drop
stages and applies icons supplied by `StageIconImageProvider`.

As a general rule, the framework restores structure and placement,
while applications supply the runtime objects that populate that
structure.

<h4 id="application-design-for-persistence">Application Design for Persistence</h4>

A non-persistent BentoFX application can construct the entire layout inline during startup. The basic demo does this by creating leaves and immediately adding newly-created dockables to those leaves:

```java
leafWorkspaceHeaders.addDockables(
        buildDockable(builder, 0, 0, "Class 1"),
        buildDockable(builder, 0, 1, "Class 2")
);
```

That is enough for a one-time in-memory layout, but it does not provide a way to recreate those dockables during a later application execution.

A persistent application should separate two concerns:

* **dockable creation**: how to construct a runtime `Dockable` for a stable identifier
* **dockable placement**: where that dockable appears in the current layout

The persistence demo shows this separation. It statically defines stable dockable identifiers in `DockableProperties` as `enum` values, resolves those identifiers through `BoxAppDockableStateProvider`, and then builds runtime `Dockable` instances from the resolved `DockableState`. 

```java
dockableStateProvider.resolveDockableState(dockableProperties.getIdentifier())
        .ifPresent(dockableState ->
                container.addDockable(buildDockable(dockableState)));
```

This is only one possible implementation strategy. Applications are free to resolve identifiers statically, dynamically, eagerly, lazily, through dependency injection, or by any other mechanism that can consistently reconstruct the appropriate runtime objects from persisted identifiers.

This same provider is also passed to the `LayoutRestorer` so the framework can resolve persisted dockable identifiers while restoring a saved layout.

```java
final LayoutRestorer layoutRestorer =
        persistenceProvider.getLayoutRestorer(
                DEFAULT_LAYOUT_IDENTIFIER,
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider
        );
```

In other words, the default layout and the restored layout should both use the same dockable resolution strategy. The default layout places dockables for the first run; the restored layout uses persisted placement and asks the same provider to recreate each dockable by identifier.

```text
Default layout startup
    Dockable identifier
        -> DockableStateProvider
            -> DockableState
                -> BoxApp.buildDockable(DockableState)
                    -> Dockable
                        -> Added to default container

Saved layout restore
    Persisted dockable identifier
        -> DockableStateProvider
            -> DockableState
                -> DockingLayoutRestorer restores Dockable
                    -> Dockable
                        -> Added to restored container
```

This keeps the default-layout path and restore path aligned. Application code does not need one implementation for first-run dockables and another implementation for restored dockables. If a dockable can be created for the default layout, it can also be recreated when the persisted layout refers to the same identifier.

When creating a persistent application, treat provider-backed reconstruction as part of the application architecture. Avoid hiding dockable construction in one-off startup code unless those dockables never need to be restored.


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

| Provider | Application responsibility |
|----------|----------------------------|
| `BentoProvider` | Return the runtime `Bento` instances whose layouts can be saved or restored. |
| `DockableStateProvider` | Resolve stable dockable identifiers to `DockableState` instances used to reconstruct dockables. |
| `DockableMenuFactoryProvider` | Resolve dockable menu factories when restored dockables need context menus. |
| `DockContainerLeafMenuFactoryProvider` | Resolve leaf menu factories when restored leaves need context menus. |
| `StageIconImageProvider` | Return JavaFX `Image` instances for restored drag/drop stages. |

Providers can create objects statically, dynamically, eagerly, lazily, through dependency injection, or by any other mechanism. The important requirement is that a persisted identifier must resolve to the same kind of runtime object whenever the layout is restored.

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

Applications generally create providers before layout restoration and reuse them for the lifetime of the application. Providers should not assume that layout restoration occurs only once. A provider may be called repeatedly whenever layouts are restored, when a default layout is created, or when future application features allow users to switch layouts.

Providers should also avoid storing stale JavaFX objects when those objects are meant to be recreated. If a provider caches runtime content, the cache lifecycle should match the application lifecycle and JavaFX threading rules.

<h4 id="recommended-application-startup-flow">Recommended Application Startup Flow</h4>

A persistent application should generally follow this startup flow:

1. Create the application's `Bento` with a stable identifier.
2. Register the `Bento` with a `BentoProvider`.
3. Create provider implementations for dockables, dockable menus, leaf menus, and stage icons as needed.
4. Build the application's default `DockingLayout` using the same providers that restoration will use.
5. Ask `LayoutRestorer` to restore the last saved layout, passing the default layout supplier as the fallback.
6. Apply the returned `DockingLayout` to the JavaFX stage, falling back to the default layout when none could be applied.
7. Obtain a `LayoutSaver` and keep it, so that auto-save runs for the session. Do this after the layout is applied, because a save reads the root branches that have a `Scene`.
8. Save the layout, and close the saver, before windows are closed.

The persistence demo follows this pattern by creating a `DockingLayout`, applying the matching `BentoLayout` to the primary stage, and then showing any restored `DragDropStage` instances.

Applying a layout can fail: a stored layout may hold a number of root branches the application does not know how to place. Report whether anything was applied, and fall back to the default layout when nothing was, because a stage that never receives a `Scene` is never shown, and an application whose only exit path runs when its window hides cannot then be closed:

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

Four things are worth knowing:

* **`saveLayout` is not a `LayoutSaver`.** It writes once and returns; nothing is scheduled and no listener is registered. Keep `getLayoutSaver` for the layout that follows the session and use this for a layout a user asked to keep.
* **A display name is stored, not addressed by.** `LayoutPersistenceProfile.named(identifier, displayName, codec, storage)` carries the name into the layout; the identifier still does the addressing. `getStoredLayoutIdentifiers` returns identifiers cheaply, while `getStoredLayouts` reads each layout to recover its name, so use the identifier listing when the names are not needed.
* **The identifier is the application's to choose.** The framework validates one and stores a name, but does not yet turn a display name into an identifier; deriving `sprint-12` from "Sprint 12" is the application's step for now. `LayoutIdentifiers.findUserLayoutProblem(identifier, codec)` reports whether a chosen identifier is usable, including whether it collides with the reserved session name.
* **Restoring a different layout while running is not the same as restoring one at startup.** The containers a restorer hands back are unattached, so the application replaces the scene root and re-shows any drag/drop stages itself, and the switch itself looks like a layout change to a running auto-save. Save the current layout first, or take auto-save down around the switch.

A layout identifier becomes a file name in file-backed storage, so it has to be usable as one: no separators, nothing a filesystem reserves, and at most 255 characters shared with the codec identifier. A name a user types is not automatically usable, which is why an application either maps display names to identifiers or restricts what the user may type.

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

<h4 id="runtime-considerations">Runtime Considerations</h4>

Applications should keep two runtime considerations in mind when using
layout persistence.

<h5 id="javafx-application-thread">JavaFX Application Thread</h5>

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

<h5 id="application-evolution">Application Evolution</h5>

Persisted layouts may outlive individual software releases.

Applications should therefore treat identifiers as long-lived contracts
between persisted layouts and runtime objects. When identifiers,
dockable types, menu structures, or other persisted concepts change,
applications should maintain backward compatibility where practical.

For example, if an identifier changes from `workspace` to `projects`,
the `DockableStateProvider` can continue accepting `workspace` and
return the newer dockable state. This allows previously saved layouts
to continue restoring successfully after an application upgrade.

Providers are the primary mechanism for adapting persisted layouts to
application changes until explicit layout migration support is added.

<h4 id="basic-demo-vs-persistence-demo">Basic Demo vs Persistence Demo</h4>

The BentoFX project includes both a basic demo and a persistence demo.

The basic demo focuses on container construction and docking behavior. The persistence demo builds on the same concepts and demonstrates how applications can save and restore docking layouts across executions using provider-backed reconstruction.

The persistence demo intentionally introduces additional abstractions such as providers and state objects. These abstractions are not required by the core docking framework itself, but become necessary when layouts must be persisted and later restored.

| Concern | Basic demo | Persistence demo |
|---------|------------|------------------|
| Bento creation | Creates a default `Bento`. | Creates a `Bento` with a stable identifier. |
| Dockable creation | Creates dockables inline with `buildDockable(...)`. | Resolves `DockableState` by identifier through `DockableStateProvider`, then builds dockables from that state. |
| Source of truth for dockables | Startup code. | Provider implementations. |
| Dockable placement | Places dockables directly in leaves during startup. | Places dockables in the default layout only; restored placement comes from persisted state. |
| Menus | Sets sample menu factories directly on leaves/dockables. | Supplies menu factories through providers so restored objects can receive the same behavior. |
| Stage setup | Creates a `Scene` directly from the root branch. | Restores or builds a `DockingLayout`, then applies the matching `BentoLayout` to the stage. |
| Drag/drop stages | Not restored across application runs. | Restored from persisted `DragDropStageState` and shown after the primary layout is applied. |
| Automatic saving | Not supported. | Obtains one saver while starting, so periodic saves run for the session and only write when docking events indicate a changed layout. |
| JavaFX threading | Startup creates JavaFX objects directly. | Restore and provider code must create JavaFX objects on the JavaFX Application Thread. |
| Provider implementations | Not required. | Required for application-specific objects that cannot be serialized. |
| Shutdown | Exits when the stage is hidden. | Saves the docking layout on close request, then closes the saver, both while the stages still exist. |

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

Three conventions are worth following in a storage implementation, because the bundled implementations follow them and callers rely on them:

* **Closing the output stream is what stores the layout.** Buffer or stage what is written and publish it only when the stream closes cleanly. A save that fails part way through then leaves the previously stored layout intact instead of replacing it with a fragment.
* **Override the catalog methods when the destination can answer them.** `LayoutStorageProvider.getLayoutIdentifiers`, `isLayoutStored` and `deleteLayout` all have defaults, so a storage implementation stays valid without them, but an application cannot offer users a list of saved layouts unless the storage it uses can enumerate. Both bundled implementations can: one file per layout, or one row per layout and codec.
* **`exists()` answers whether there is a layout to read**, not whether a location is present. Empty content is not a layout: a restorer told that a layout exists will try to decode it, and an empty or truncated payload becomes a decode failure where a clean "nothing stored yet" would have produced the default layout.
* **`close()` releases what the storage owns, and only that.** Whichever saver or restorer receives a `LayoutStorage` closes it, so a storage handed a resource it did not create should leave that resource alone.

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

- [JSON Codec](./persistence/codec/json)
- [XML Codec](./persistence/codec/xml)
- [H2 Database Storage](./persistence/storage/db/h2)
- [File Storage](./persistence/storage/file)

Additional API and usage documentation can be found in [Docking Layout Persistence Implementation](docs/persistence/docking-layout-persistence.md) and [Bento layout persistence diagrams](docs/persistence/docking-layout-persistence-diagrams.md).

The following are also provided for additional information on using `ServiceLoader`:

* https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
* https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html
* https://www.baeldung.com/java-spi

<h3 id="persistence-example">Persistence Example</h3>

The [persistence demo](./demos/persistence) module contains an example application, derived from the [basic demo BoxApp application](./demos/basic/src/main/java/demo/BoxApp.java), that demonstrates using the [persistence](./persistence) framework to save and restore a BentoFX docking layout.

To run the persistence demo:

```bash
./gradlew :demos:persistence:run
```

For details on applying a restored docking layout, refer to `BoxApp.applyDockingLayout(DockingLayout)` in the [persistence demo](./demos/persistence/src/main/java/software/coley/boxfx/demo/persistence/BoxApp.java).

For details on saving the current docking layout, refer to `BoxApp.saveDockingLayout(WindowEvent)` in the [persistence demo](./demos/persistence/src/main/java/software/coley/boxfx/demo/persistence/BoxApp.java).

## Next Steps

- The **[Contributing Guide](CONTRIBUTING.md)** explains how to build BentoFX, develop new features, and submit pull requests.
- The **[Maintainers Guide](MAINTAINERS.md)** covers repository administration, project health, CI/CD, and releases.
