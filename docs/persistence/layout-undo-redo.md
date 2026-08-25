# Layout Undo/Redo Design Note

Status: design note. Nothing here is implemented except the state-equality groundwork
described under [Groundwork already in place](#groundwork-already-in-place).

For how capture and restore work today, see
[Docking Layout Persistence Implementation](docking-layout-persistence.md).

## Goal

Let a user reverse and reapply layout changes: moving a dockable, splitting or
merging a leaf, dragging a divider, collapsing a leaf, reordering tabs, and closing
a pane.

## Scope

Covers change detection, the undo stack, and how a stored state is reapplied. Does
not cover the undo of edits *inside* a dockable's content, which belongs to the
application, nor keyboard binding.

## What the existing API already supports

Undo/redo needs no change to the `core/` docking framework. Both halves are
reachable through public API.

**Observing a change.** `DockEvent` covers structural changes, and it is a `sealed`
interface, so no new event type can be added from outside `core/`. It does not need
one: everything it omits is exposed as a JavaFX property.

| Captured state | How a change is observed |
| --- | --- |
| Container and dockable structure | `DockEvent`, via `Bento.events().addEventListener` |
| Selected dockable | `DockEvent.DockableSelected`, or `DockContainerLeaf.selectedDockableProperty()` |
| Divider positions | `SplitPane.getDividers()`, each divider's `positionProperty()` |
| Branch orientation | `orientationProperty()` |
| Collapsed | `DockContainerLeaf.collapsedProperty()` |
| Header side | `DockContainerLeaf.sideProperty()` |
| Can-split | `DockContainerLeaf.canSplitProperty()` |
| Resize-with-parent | Sampled at capture; `SplitPane` keeps it in the node's property map |
| Uncollapsed size | Sampled at capture; bound to live size while the leaf is expanded |
| Drag/drop stage geometry | `Stage` and `Window` properties |

`AbstractAutoCloseableLayoutSaver` already consumes `DockEvent` this way, with a
matching listener-removal path, and is the pattern to follow.

**Applying a change.** The mutations required are all public:
`DockContainerBranch.addContainer(int, DockContainer)`, `removeContainer`,
`replaceContainer`, `setContainerSizePx`, `setContainerResizable`,
`setContainerCollapsed`, `DockContainerLeaf.addDockable(int, Dockable)`,
`removeDockable`, `selectDockable`, `setSide`, `setUncollapsedSize`, and
`SplitPane.setDividerPosition`.

Because the undo stack holds `BentoState` in memory, no codec or storage is
involved and the persisted format does not change. This is not a breaking change to
saved layouts.

## The design decision: rebuild or patch

`DockingLayoutStateRestorer` builds an entirely new tree from
`DockBuilding.root/branch/leaf/dockable` and returns detached containers.
`LayoutRestorer.restoreLayout` documents that the caller must attach them to a
`Scene`.

**Option A, reuse the existing restorer.** Cheap to write. Every undo destroys and
recreates each dockable's content through `DockableStateProvider`, losing in-content
view state such as scroll position and caret, and the application has to swap roots
into scenes and reopen drag/drop stages on each undo. Restore also defers work,
divider positions through `Platform.runLater` and collapse until a layout pass has
run, so an undo is not complete when the call returns and rapid undo/redo races
itself.

**Option B, diff and patch the live tree.** A new applier walks a target
`BentoState` against the live tree, matches by identifier, and applies only the
differences using the mutation API above, reusing existing `Dockable` instances so
content survives. This is the recommended approach. It is most of the work.

Option A is worth building only as a throwaway probe, not as the delivered feature.

## Work items for Option B

1. **The applier.** Match by identifier, reparent rather than recreate, and respect
   the ordering constraints `DockingLayoutStateRestorer` already documents: divider
   positions after children exist, collapse after a layout pass, uncollapsed size
   after the collapse.
2. **Coalescing.** A divider drag emits a continuous stream of position changes.
   Without a gesture boundary such as mouse release, or a debounce, one drag becomes
   hundreds of undo entries. This is the fiddliest part and has no precedent in the
   module.
3. **A re-entrancy guard.** Applying an undo fires the same events and property
   changes that feed the recorder, so recording must be suppressed while applying.
   This is the classic undo defect; it needs a test that fails without the guard.
4. **Listener lifecycle.** Divider listeners must be re-attached as a branch's
   children change, each with a removal path.
5. **Public API.** An undo manager with a depth limit and observable
   `canUndo`/`canRedo` for menu enablement, documented FX-Application-Thread-only.

## Known limits

Undoing a *close* cannot preserve content. The dockable is gone, and only
`DockableStateProvider` can rebuild it, so undoing a close reopens fresh content.
Every other operation can be made content-preserving.

## Groundwork already in place

The eight state types in
[`api/state`](../../persistence/core/src/main/java/software/coley/bentofx/persistence/core/api/state)
now implement `equals` and `hashCode`, so a freshly captured state can be compared
against the previous one to answer whether the layout actually changed, without
encoding either side. Two properties of that contract matter to the work above:

- Comparison is by **exact runtime type**. `DockContainerRootBranchState` adds no
  field to `DockContainerBranchState` but restores into a different container, so a
  fields-only comparison would wrongly call them equal.
- On `DockableState`, the node and the three functional fields have no value
  equality of their own and so compare by identity. Captured states are unaffected,
  because a captured `DockableState` carries only its identifier.

Each state type has a test covering both, pairing an
[EqualsVerifier](https://jqno.nl/equalsverifier/) check against the full field set
with the hand-written cases that pin the two properties above.

## Open decision

Whether preserving in-content view state across an undo is a requirement. That
single answer chooses between Option A and Option B, and roughly a fivefold
difference in effort.
