# BentoFX

A docking system for JavaFX.

Information for contributing to the BentoFX project can be found [here](./CONTRIBUTING.md).

## Table of Contents

* [Requirements](#requirements)
* [Core Framework](#core-framework)
  * [Usage](#core-usage)
    * [Gradle (Groovy DSL)](#core-gradle-groovy-dsl)
    * [Gradle (Kotlin DSL)](#core-gradle-kotlin-dsl)
    * [Maven](#core-maven)
  * [Overview](#overview)
    * [Containers](#containers)
    * [Controls](#controls)
    * [Dockables](#dockables)
  * [Example](#example)
    * [Construct the Default Docking Layout](#construct-the-default-layout)
    * [Show the Layout](#show-it)
* [Persistence Framework](#persistence)
  * [Usage](#persistence-usage)
    * [Gradle (Groovy DSL)](#persistence-gradle-groovy-dsl)
    * [Gradle (Kotlin DSL)](#persistence-gradle-kotlin-dsl)
    * [Maven](#persistence-maven)
  * [Overview](#persistence-overview)
  * [Extending Persistence](#extending-persistence)
  * [Example](#persistence-demo)

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

### Overview

![overview](assets/overview.png)

In terms of hierarchy, the `Node` structure of Bento goes like:

- `DockContainerRootBranch`
    - `DockContainerBranch` _(Nesting levels depends on which kind of implementation used)_
        - `DockContainerLeaf`
            - `Dockable` _(Zero or more)_

Each level of `*DockContainer` in the given hierarchy and `Dockable` instances can be constructed via a `Bento`
instance's builder offered by `bento.dockBuilding()`.

#### Containers

![containers](assets/containers.png)

Bento has a very simple model of branches and leaves. Branches hold additional child containers. Leaves
display `Dockable` items and handle drag-n-drop operations.

| Container type        | Description                                                                                                                                                             |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DockContainerBranch` | Used to show multiple child `DockContainer` instances in a `SplitPane` display. Orientation and child node scaling are thus specified the same way as with `SplitPane`. |
| `DockContainerLeaf`   | Used to show any number of `Dockable` instance rendered by a `HeaderPane`.                                                                                              |

#### Controls

![controls](assets/controls.png)

Bento comes with a few custom controls that you will want to create a custom stylesheet for to best fit the intended
look and feel of your application.

An example reference sheet _(which is included in the dependency)_ can be found
in [`bento.css`](core/src/main/resources/bento.css).

| Control                     | Description                                                                                                                                       |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `Header`                    | Visual model of a `Dockable`.                                                                                                                     |
| `HeaderPane`                | Control that holds multiple `Header` children, and displays the currently selected `Header`'s associated `Dockable` content.                      |
| `Headers`                   | Child of `HeaderPane` that acts as a `HBox`/`VBox` holding multiple `Headers`.                                                                    |
| `ButtonHBar` / `ButtonVBar` | Child of `HeaderPane` used to show buttons for the `DockContainerLeaf` for things like context menus and selection of overflowing `Header` items. |

#### Dockables

The `Dockable` can be thought of as the model behind each of a `HeaderPane`'s `Header` _(Much like a `Tab` of
a `TabPane`)_.
It outlines capabilities like whether the `Header` can be draggable, where it can be dropped, what text/graphic to
display,
and the associated JavaFX `Node` to display when placed into a `DockContainerLeaf`.

### Example
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
amongst one another. However, the primary docking container tabs with our _"project files"_ cannot be
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

#### Show it

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

The [persistence](./persistence) modules create a framework that can be used to supplement the [core](#core-module) module, allowing BentoFX docking layouts that have been customized at runtime to be to saved and restored across application executions.  Application developers control the format and storage destination by adding runtime dependencies to implementations of codec and storage interfaces as noted below.

> <span style="font-size: 1.5em;">💡</span> The persistence framework is currently limited to saving and restoring a single format at a single storage destination.

<h3 id="persistence-framework-usage">Usage</h3>
The persistence framework has dependencies on the following modules:

* Persistence API  
  The persistence API contains core classes for saving and restoring docking layouts using the format and storage destination implementations discovered at runtime.
    * `persistence-api`
* Codec implementations  
  The codec implementations contain classes for encoding and decoding the docking layout in the format defined by each implementation. The BentoFX persistence framework includes codec implementations with the following artifact names:
    * JavaScript Object notation (JSON)   
      * `persistence-codec-json`
    * eXtensible Markup Language (XML)  
      * `persistence-codec-xml`
* Storage implementations  
  The storage implementations contain classes for reading and writing the docking layout to input and output streams as defined by each implementation. The BentoFX persistence framework includes storage implementations artifacts with the following artifact names:
    * File  
      * `persistence-storage-file`
    * H2 Database  
      * `persistence-storage-db-h2`

<h4 id="persistence-gradle-groovy-dsl">Gradle (Groovy DSL)</h4>   

```groovy  
implementation 'software.coley.bento-fx:persistence-api:${version}'
runtimeOnly 'software.coley.bento-fx:persistence-codec-xml:${version}'
runtimeOnly 'software.coley.bento-fx:persistence-storage-file:${version}'
```

<h4 id="persistence-gradle-kotlin-dsl">Gradle (Kotlin DSL)</h4>  

```kotlin  
implementation("software.coley.bento-fx:persistence-api:${version}")
runtimeOnly("software.coley.bento-fx:persistence-codec-xml:${version}")
runtimeOnly("software.coley.bento-fx:persistence-storage-file:${version}")
```

<h4 id="persistence-maven">Maven</h4>   

```xml  
<dependency>
    <groupId>software.coley.bento-fx</groupId>
    <artifactId>persistence-api</artifactId>
    <version>${version}</version>
</dependency>
<dependency>
    <groupId>software.coley.bento*fx</groupId>
    <artifactId>persistence-codec-xml</artifactId>
    <version>${version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>software.coley.bento*fx</groupId>
    <artifactId>persistence-storage-file</artifactId>
    <version>${version}</version>
    <scope>runtime</scope>
</dependency>
```

<h3 id=persistence-overview>Overview</h3>

The primary interface for interacting with persistence framework is the `LayoutPersistenceProvider`, which provides access to a `LayoutSaver` and `LayoutRestorer` that can be used to persist and restore a docking layout.  

`DockingLayoutPersistenceProvider`, the default `LayoutPersistenceProvider` implementation, can be acquired in one of the following ways: 

1. Manual construction
```java
final LayoutPersistenceProvider provider =   
  new DockingLayoutPersistenceProvider();
```

2. Using `ServiceLocator`
```java
final Iterable<LayoutPersistenceProvider> persistenceProviders =
        ServiceLoader.load(LayoutPersistenceProvider.class);

final Iterator<LayoutPersistenceProvider> persistenceProviderIterator =
        persistenceProviders.iterator();

if (persistenceProviderIterator.hasNext()) {

    final LayoutPersistenceProvider persistenceProvider =
            persistenceProviderIterator.next();
}
```

Once the `LayoutPersistenceProvider` is acquired, it can be used to acquire `LayoutSaver` and `LayoutRestorer` implementations:  

```java
final LayoutSaver layoutSaver = persistenceProvider.getLayoutSaver(
        bentoProvider,
        layoutIdentifier
);

final LayoutRestorer layoutRestorer = persistenceProvider.getLayoutRestorer(
        bentoProvider,
        layoutIdentifier,
        dockableStateProvider,
        stageIconImageProvider,               // Nullable
        dockContainerLeafMenuFactoryProvider  // Nullable
);
```

#### LayoutSaver
The `LayoutSaver` is used to persist the current docking layout. The default implementation auto-saves the layout every five minutes. Applications should also save the layout directly when exiting, similar to the following:

```java
private void saveDockingLayout() {
    try {
        final LayoutSaver layoutSaver =
                persistenceProvider.getLayoutSaver(
                        bentoProvider,
                        DEFAULT_LAYOUT_IDENTIFIER
                );

        layoutSaver.saveLayout();
    } catch (BentoStateException e) {
        logger.warn("Could not save the docking layout.", e);
    }
}

...

// Save the docking layout on close request because the stage is (and all other 
// windows are) no longer available after they are closed and, as such, will 
// not be discoverable when saving the docking layout.     
stage.setOnCloseRequest(event -> {
    saveDockingLayout();
});
```

#### LayoutRestorer
The `LayoutRestorer` is used to restore the last saved docking layout, similar to the following:

```java
/**
 * @return if a prior {@link DockingLayout} has been saved, restores and
 * returns it. Otherwise, returns the default {@link DockingLayout}.
 * @see #getDefaultDockingLayout()
 */
private DockingLayout getDockingLayout() {

    final LayoutRestorer layoutRestorer =
            persistenceProvider.getLayoutRestorer(
                    bentoProvider,
                    layoutIdentifier,
                    dockableStateProvider,
                    stageIconImageProvider,
                    dockContainerLeafMenuFactoryProvider
            );

    return layoutRestorer.restoreLayout(
            this::getDefaultDockingLayout
    );
}
```

### Extending Persistence
The `DockingLayoutPersistenceProvider` uses the `ServiceLoader` to acquire `LayoutCodecProvider` and `LayoutStorageProvider` implementations from the module path at runtime.  

Albeit otherwise not very useful, the following example is provided to demonstrate extending the persistence framework to use a `LayoutStorage` other than the default implementations provided:

1. Implement the `LayoutStorage` interface:
```java
public class SystemLayoutStorage implements LayoutStorage {
  @Override
   public boolean exists() {
       // For our example, just return false; otherwise, 
       // the LayoutRestorer will attempt to read a 
       // previously persisted layout from the command prompt.
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

2. Implement the `LayoutStorageProvider` interface to return the implementation:
```java
public class SystemLayoutStorageProvider implements LayoutStorageProvider {
   @Override
   public LayoutStorage createLayoutStorage(
      final String layoutIdentifier,
      final String codecIdentifier
   ) {
       // Normally, we might use the layout and codec identifiers to construct 
       // a file name, use them as keys in a database table, etc. We'll just 
       // ignore them here,  
       return new SystemLayoutStorage();
   }
}
```
 
3. Register the provider implementation with the module's descriptor:
```java
provides LayoutStorageProvider with SystemLayoutStorageProvider;
``` 

4. Add the module to the application's module path:
```kotlin
runtimeOnly("software.coley.bentofx:persistence-storage-system:${version}")
``` 

Codecs are similarly extended by implementing the `LayoutCodecProvider` and  `LayoutCodec` interfaces, registering the `LayoutCodecProvider` implementation with the module's descriptor and adding the module to the application's module path.     

For complete examples, refer to these modules:  
[JSON Codec](./persistence/codec/json)  
[XML Codec](./persistence/codec/xml)  
[H2 Database Storage](./persistence/storage/db/h2)  
[File Storage](./persistence/storage/file)  

API and usage documentation can be found [here](assets/docking-layout-persistence.md) and [here](assets/docking-layout-persistence-diagrams.md).

The following are also provided for additional information on using `ServiceLoader`:
https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html   
https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html   
https://www.baeldung.com/java-spi   

## Example
The [persistence-demo](./demos/persistence-demo) module contains an example application, derived from the [basic-demo BoxApp application](./demos/basic-demo/src/main/java/software/coley/boxfx/demo/basic/BoxApp.java), that demonstrates using the [persistence](./persistence) framework to save and restore a BentoFX docking layout.

To run the persistence demo, use `./gradlew :demos:persistence:run`

For details on applying a restored docking layout, refer to `BoxApp.applyDockingLayout(DockingLayout)`. 

For details on saving the current docking layout, refer to `BoxApp.saveDockingLayout()`.
