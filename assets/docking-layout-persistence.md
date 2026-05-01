# Docking Layout Persistence Implementation

This document describes BentoFX layout persistence as implemented by [DockingLayoutSaver](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutSaver.java) and  [DockingLayoutRestorer](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutRestorer.java).


For the overarching design, [docking layout persistence diagrams](docking-layout-persistence-diagrams.md) are also available.

## Scope

This document focuses on persistence orchestration, not rendering, docking UX, or module dependency graphs.

## Key concepts

### Domain state model

Persistence is expressed as immutable-ish *state* objects (built via builders) rather than direct serialization of UI classes:

- `DockingLayout` is the persistence root.
- `BentoLayout` represent the layout for persisting `Bento`, which contain container trees.
- `DockContainerRootBranchState`, `DockContainerBranchState`, `DockContainerLeafState` represent a container tree.
- `DockableState` represents a dockable identity in a container/leaf and contains references to factories used to reconstruct a dockable.
- `DragDropStageState` represents secondary (drag/drop) stages and contains a root-branch state.

### Storage and codec

Persistence is a two-step pipeline:

1. Build a `BentoState` snapshot (in-memory).
2. Encode/decode via a `LayoutCodec` to/from a `LayoutStorage` stream.

This decoupling lets you choose the persisted format (XML,JSON, or custom implementation), and the storage location (file, database, or custom implementation) without changing the save/restore logic.

## Save layout design

### High-level algorithm

[DockingLayoutSaver.saveLayout()](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutSaver.java):

1. Create a new `BentoStateBuilder`.
2. Iterate all JavaFX stages (`StageUtils.getAllStages()`).
3. For each stage:
   - If it is a `DragDropStage`, build `DragDropStageState` including its `DockContainerRootBranchState`.
   - Otherwise, attempt to build a `DockContainerRootBranchState` for the stage (if it contains a Bento root container).
4. Build the final `BentoState`.
5. Persist it: `layoutStorage.openOutputStream()` then `codec.encode(state, out)`.

### How the container tree is captured

The saver walks the Bento container graph and converts each runtime container to its corresponding `*State`:

- `Bento` → `BentoState`
- `DragDropStage` → `DragDropStageState`
- `DockContainerRootBranch` → `DockContainerRootBranchState`
- `DockContainerBranch` → `DockContainerBranchState`
- `DockContainerLeaf` → `DockContainerLeafState`
- `Dockable` → `DockableState`

Branch properties such as orientation, divider positions, prune rules, and child containers are captured by `setCommonDockContainerBranchProperties(...)`.

Leaf properties include:
- prune rules
- side
- resizable-with-parent
- split capability
- collapsed state and uncollapsed size
- selected dockable id
- child dockables

### Error handling philosophy

- Saver attempts to save each stage independently; failure to save one stage should not prevent saving others.
- Encoding failures are treated as fatal and reported via `BentoStateException`.

## Restore layout design

### High-level algorithm
[DockingLayoutRestorer.restoreLayout(Supplier\<DockingLayout>  defaultLayoutSupplier)](../persistence/api/src/main/java/software/coley/bentofx/persistence/impl/DockingLayoutRestorer.java):


1. Hide primary stage and close any other existing stages.
2. Decode the persisted `BentoState` from storage **off the JavaFX application thread** using a scheduled executor and a `CompletableFuture`.
3. Select a primary root branch:
   - Prefer the root branch with no parent stage state.
   - If none exist, fall back to an empty root branch.
4. Restore that root branch into runtime containers via `DockBuilding`.
5. Restore any saved `DragDropStageState` instances as secondary stages.
6. Return the restored `DockContainerRootBranch` for the primary stage.

### How dockables are restored

Restoration resolves dockables by identifier:

- Each persisted `DockableState` provides `identifier`.
- `restoreDockableState(id)` (resolver) obtains the runtime `DockableState`.
- The dockable is reconstructed from the `DockableState` and added to the correct container/leaf.
- Selected dockable id is applied after `Dockable` addition.

If a dockable cannot be resolved, the restorer logs a warning and continues.

### Property restoration order

The restorer applies persisted properties in this rough order:

- Container instance creation (via `DockBuilding`)
- Prune rules / orientation / split flags
- Dockables insertion
- Selected dockable selection
- Divider positions / sizes / collapsed state (for leaves)

> <span style="font-size: 1.5em;">💡</span> Leaf collapse is noted as a known issue (`BENTO-13`) where the runtime leaf does not visually collapse after state restoration.

## Design patterns used

- **Builder**: `BentoStateBuilder`, `DockContainer*StateBuilder`, `DragDropStageStateBuilder`.
- **Composite**: Runtime dock container graph (`DockContainerBranch` + `DockContainerLeaf`) and its mirrored state graph.
- **Strategy**: `LayoutCodec` provides interchangeable encoding/decoding strategies (XML, JSON, etc.). `LayoutStorage` provides interchangeable storage strategies (Database, File, etc.).
- **Adapter / Mapper** (in codec implementations): codecs often map between domain states and DTOs for JAXB/Jackson friendliness.
- **Factory**: `DockBuilding` acts as a factory for container instances during restore. `DockContainerLeafMenuFactory` and `DockableMenuFactory` act as factories for `DockContainerLeaf` and `Dockable` context menus.
- **Service Locator pattern**: `ServiceLocator` is used to discover and load implementations matching Service Provider Interfaces (SPIs): `LayoutPersistenceProvider`, `LayoutCodecProvider`,and `LayoutStorageProvider`.

## Extension points

- Users may implement additional storage destinations (Google Remote Procedure Call (gRPC), web service, cloud, additional database implementations, etc.).
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
