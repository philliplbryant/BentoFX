# Docking Layout Persistence Implementation

This document describes BentoFX layout persistence as implemented
by [DockingLayoutSaver](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutSaver.java)
and  [DockingLayoutRestorer](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutRestorer.java).

For the overarching design, [docking layout persistence diagrams](docking-layout-persistence-diagrams.md) are also
available.

## Scope

This document focuses on persistence orchestration, not rendering, docking UX, or module dependency graphs.

## Key concepts

### Domain state model

Persistence is expressed as immutable-ish *state* objects (built via builders) rather than direct serialization of UI
classes:

- `DockingLayout` is the persistence root, containing `BentoState`.
- `BentoState` define the structure for persisting `Bento`, encompassing container trees.
- `DockContainerRootBranchState`, `DockContainerBranchState`, `DockContainerLeafState` represent a container tree.
- `DockableState` represents a dockable identity in a container/leaf and contains references to factories used to
  reconstruct a dockable.
- `DragDropStageState` represents secondary (drag/drop) stages and contains a root-branch state.

### Storage and codec

Round-trip persistence is a multistep, pipelined process:

1. Declare the `LayoutCodec` to use for encoding/decoding the `BentoState`.
2. Declare the `LayoutStorage` to use to stream the encode state to/from persisted storage.
3. Use a `LayoutSaver` to build the in-memory `BentoState` elements, encode, and persist them to storage. The `LayoutSaver`:
    * Uses the `LayoutCodec` to encode the `BentoState`.
    * Uses the `LayoutStorage` to stream the encoded the `BentoState` to persisted storage.
4. Use a `LayoutRestorer` to stream and decode the persisted `BentoState` elements. The `LayoutRestorer`:
    * Uses the `LayoutStorage` to stream the persisted `BentoState` from storage.
    * Uses the `LayoutCodec` to decode the `BentoState`.
5. Use the restored `BentoState` to update the docking layout.

This decoupling lets you choose the persisted format (XML,JSON, or custom implementation), and the storage location (file, database, or custom implementation) without changing the save/restore logic. One need only change the runtime dependencies to change the desired implementations of the persistence API.

## Saving the layout design

### High-level algorithm

[LayoutSaver.saveLayout()](../persistence/api/src/main/java/software/coley/bentofx/persistence/api/LayoutSaver.java):

The default implementation, [DockingLayoutSaver](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutSaver.java), extends [AbstractAutoCloseableLayoutSaver](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/AbstractAutoCloseableLayoutSaver.java), which implements [LayoutSaver](../persistence/api/src/main/java/software/coley/bentofx/persistence/api/LayoutSaver.java).   

[AbstractAutoCloseableLayoutSaver](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/AbstractAutoCloseableLayoutSaver.java) is responsible for automatically saving the docking layout at scheduled intervals and can be called when the application exits. To efficiently autosave, this class listens for `DockEvent` to track whether changes have been made to the layout and only saves when changes have actually been made. Because this class also implements `AutoCloseable`, it can be used in a try-with-resources block to automatically call `close()` to save the docking layout when the try block exits.

### How the container tree is captured

The saver use the `BentoProvider` to walk each `Bento`'s container graph and converts each runtime container to its corresponding `*State`:

- `Bento` → `BentoState`
- `DragDropStage` → `DragDropStageState`
- `DockContainerRootBranch` → `DockContainerRootBranchState`
- `DockContainerBranch` → `DockContainerBranchState`
- `DockContainerLeaf` → `DockContainerLeafState`
- `Dockable` → `DockableState`

### Error handling philosophy

- Saver attempts to build each state independently; failure to build one one state should not prevent building others.
- Encoding and streaming failures are treated as fatal and reported via `BentoStateException`.

## Restoring the layout design

### High-level algorithm

[LayoutRestorer.restoreLayout(Supplier\<DockingLayout>  defaultLayoutSupplier)](../persistence/api/src/main/java/software/coley/bentofx/persistence/api/LayoutRestorer.java):

The default implementation, [DockingLayoutRestorer.restoreLayout(Supplier\<DockingLayout>  defaultLayoutSupplier)](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutRestorer.java):

1. Hides the primary stage and closes any other existing stages.
2. If the persisted layout <em>does not</em> exist:
    1. Uses the `Supplier<DockingLayout>` to get the default docking layout.
3. If the persisted layout <em>does</em> exist:
   1. Decodes the persisted `BentoState` from storage, **off the JavaFX application thread**, using a scheduled executor and
      a `CompletableFuture`.
   2. Creates a `DockingLayout` containing the `BentoState`. 
   3. For each `BentoState`:
       - Creates a `BentoLayout`.
       - Uses the `BentoProvider` to get the `Bento` for its identifier.
       - Uses the `Bento` to get its `DockBuilding`.
       - For each `DockContainerRootBranchState`:
           - Restores the `DockContainerRootBranch`
               - Restores child `DockContainer` and `Dockable`
               - Adds the child to the `DockContainerRootBranch`
           - Adds the `DockContainerRootBranch` to the `BentoLayout`.
       - Adds the `BentoLayout` to the `DockingLayout`.
   4. For each `DragDropStageState`:
       - Restores the `DragDropStage`.
           - Restores the `DockContainerRootBranch`.
           - Adds the `DockContainerRootBranch` to the `DragDropStage`
           - Uses the `StageIconImageProvider` to restore the `DragDropStage` icons.
           - Applies the remaining `DragDropStageState` properties to the `DragDropStage`.
       - Adds the `DragDropStage` to the `BentoLayout`.
   5. Adds the `BentoLayout` to the `DockingLayout`.
4. Returns the `DockingLayout` for the client application to use to update the docking layout.

### How dockables are restored

Restoration resolves dockables by identifier:

- Each persisted `DockableState` provides an `identifier`.
- `DockableStateProvider.restoreDockableState(identifier)` resolves the runtime `DockableState`.
- The `Dockable` is reconstructed from the `DockableState` and added to the correct container/leaf.
- Selected `Dockable` identifier is applied after `Dockable` additions.

### Error handling philosophy

- If a persisted layout cannot be found, the default layout supplier is used to return the default layout.
- LayoutRestorer attempts to restore each docking component independently; failure to restore one docking component should not prevent restoring others.
- Decoding failures are treated as fatal and the default layout supplier is used to return the default layout.
- If a dockable cannot be resolved, the restorer logs a warning and continues.

## Design patterns used

- **Builder**: `BentoStateBuilder`, `DockContainer*StateBuilder`, `DragDropStageStateBuilder`.
- **Composite**: Runtime dock container graph (`DockContainerBranch` + `DockContainerLeaf`) and its mirrored state
  graph.
- **Strategy**: `LayoutCodec` provides interchangeable encoding/decoding strategies (XML, JSON, etc.). `LayoutStorage`
  provides interchangeable storage strategies (Database, File, etc.).
- **Adapter / Mapper** (in codec implementations): codecs often map between domain states and DTOs for JAXB/Jackson
  friendliness.
- **Factory**: `DockBuilding` acts as a factory for container instances during restore. `DockContainerLeafMenuFactory`
  and `DockableMenuFactory` act as factories for `DockContainerLeaf` and `Dockable` context menus.
- **Service Locator pattern**: `ServiceLocator` is used to discover and load implementations matching Service Provider
  Interfaces (SPIs): `LayoutPersistenceProvider`, `LayoutCodecProvider`,and `LayoutStorageProvider`.

## Extension points

- Users may implement additional storage destinations (Google Remote Procedure Call (gRPC), web service, cloud,
  additional database implementations, etc.).
- Users may implement additional codec formats (Protobuf, YAML, etc.) or versioned schemas.

## Additional capabilities under consideration

- Add layout versioning and migration (recommend in the codec layer).
- Create a service provider with methods to:
    - Save layouts as named entries and codec identifiers.
    - Return a list of saved layouts by name and codec identifier.
    - Restore a layout by name and codec identifier.
- Update `BoxApp` with menu items to:
    - Save the current layout without exiting.
    - Restore previously persisted layouts by name and codec identifier.
