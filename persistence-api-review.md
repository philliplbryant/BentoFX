# Review: `persdnd-bento;1;dockable:OJMLWCUDistence/api`dnd-bento;0;dockable:OYRTTEFJ

Scope: `persistence/api/src/main/java` (all 42 files). `core/` was read for API
verification only; it has since received one additive change, under one-off
permission, to make the B7 fix possible — see B7. Test and FT sources were read
only where they clarified intent (they are not reviewed here).

Line numbers refer to the files as they stand on `enhancement/issue-13`. Findings
fixed after the initial review are marked and dated; their line numbers refer to
the code as it was when the finding was raised.

## Status

**All 10 blockers closed: 9 fixed, 1 withdrawn as not a defect. 6 of 12 majors fixed;
2 of 10 minors fixed, 1 made moot; 14 of 16 nits closed — 3 resolved incidentally by
blocker fixes and 11 swept deliberately.** Every fix carries a regression test that
was confirmed to fail with the fix reverted, with two exceptions, each explained in
its own entry: B2/M2, where the defect is a memory-visibility race that cannot be
reproduced on demand, and M4, where the fix is a sealed hierarchy and the compiler
refuses to build the defect back in.

With M12 closed, **nothing outstanding can abort a save, lose data, or hang a
caller.** The six remaining majors are API design and error-reporting problems; the
minor and nit lists are hygiene.

Three findings turned out to be partly or wholly wrong once tested, and every
correction is recorded in its own entry rather than quietly dropped:

- **B10 was not a defect at all.** Measured behavior is correct, and the predicted
  symptom was the opposite of what actually happens.
- **B9's hazard needs the stage to be *shown***, not merely to exist — my first test
  for it was vacuous as a result.
- **B7's rendered geometry** does not match its stored value to the pixel, so its
  test needs two tolerances rather than one.

That is one false positive and two overstated mechanisms out of twenty-two findings.
The pattern is consistent: reasoning across a module boundary — into `core`'s
deferred layout machinery in every one of these three cases — was where this review
was least reliable, and the only thing that settled any of them was running code.

### BLOCKER

| | Finding | Status                                          |
|---|---|-------------------------------------------------|
| [B1](#b1) | `DefaultBentoProvider` weak map never evicts, not thread-safe | **Fixed** 2026-08-10 (`5f99ab8`)                |
| [B2](#b2) | Constructor publishes `this` to executor and event bus | **Fixed** 2026-08-10 (`3bee6dd`)                |
| [B3](#b3) | `RuntimeException` silently kills auto-save | **Fixed** 2026-08-10 (`8af91f0`)                |
| [B4](#b4) | `callOnFxThread` can block its caller forever | **Fixed** 2026-08-10 (`8af91f0`)                |
| [B5](#b5) | Collapse state never restored below the root branch | **Fixed** 2026-08-10 (`57eb2a4`)                |
| [B6](#b6) | Leaf resize state always captures `true` | **Fixed** 2026-08-10 (`d813654`)                |
| [B7](#b7) | `uncollapsedSizePx` captured, never restored | **Fixed** 2026-08-10 (`f718d18`, incl. `core/`) |
| [B8](#b8) | Auto-save scheduler state unguarded across threads | **Fixed** 2026-08-11 (`f94bc20`)                |
| [B9](#b9) | Captor NPEs on a scene-less `Stage`, and the restorer can produce one | **Fixed** 2026-08-11 (`598c742`)                |
| [B10](#b10) | Deferred dividers overwrite synchronously-applied collapse geometry | **Fixed** 2026-08-13 (withdrawn 08-11, reopened by the round-trip test) |

### MAJOR

| | Finding | Status |
|---|---|---|
| [M1](#m1) | `module-info` exports both `impl` packages as public API | **Fixed** 2026-08-11 |
| [M2](#m2) | `close()` does not save when auto-save is disabled | **Fixed** 2026-08-10 (with B2, `3bee6dd`) |
| [M3](#m3) | `callOffFxThread` blocks the FX thread, one thread per call | **Fixed** 2026-08-12 (thread per call); blocking documented, not removed |
| [M4](#m4) | Public `DockContainerStateBuilder` builds state restore discards | **Fixed** 2026-08-12 |
| [M5](#m5) | `LayoutStorage` ownership unspecified; two components each close it | **Fixed** 2026-08-12 (incl. T15) |
| [M6](#m6) | `LayoutStateWriter` mislabels storage failures as encode failures | **Fixed** 2026-08-12 |
| [M7](#m7) | Branch dockables captured recursively, ignored on restore; root branch inverted | **Fixed** 2026-08-12 |
| [M8](#m8) | Restore failures invisible because `boolean` returns are discarded | **Fixed** 2026-08-12 |
| [M9](#m9) | A failing dockable is silently dropped from the saved layout | **Fixed** 2026-08-12 |
| [M10](#m10) | `isShowing` captured but never applied | **Fixed** 2026-08-12 |
| [M11](#m11) | Capture depends on scene attachment; saving before display loses the layout | **Fixed** 2026-08-12 |
| [M12](#m12) | Capturing a leaf with no side throws, aborting the whole save | **Fixed** 2026-08-11 |

### MINOR

| | Finding | Status |
|---|---|---|
| [N1](#n1) | `StageUtils.getAllScreenBounds` uses `Double.MIN_VALUE` for maxima | **Fixed** 2026-08-12 |
| [N2](#n2) | `StageUtils` position helpers take boxed `Double`, NPE on null | **Fixed** 2026-08-12 |
| [N3](#n3) | `BentoStateBuilder` omits the null check its siblings have | **Fixed** 2026-08-13 |
| [N4](#n4) | `DockContainerStateBuilder.setPruneWhenEmpty` takes primitive `boolean` | **Moot** 2026-08-12 - builder deleted by M4 |
| [N5](#n5) | `DockContainerRootBranchStateBuilder` duplicates the branch builder | **Fixed** 2026-08-13 |
| [N6](#n6) | `restoreDockable` discards the resolved state's own identifier | **Fixed** 2026-08-13 |
| [N7](#n7) | `DragDropStageState.isAutoClosedWhenEmpty()` Javadoc contradicts signature | **Fixed** 2026-08-13 |
| [N8](#n8) | `ServiceLoader` cached once at construction, via the TCCL | **Fixed** 2026-08-13 |
| [N9](#n9) | Missing Javadoc across the public API (enumerated in the section) | **Fixed** 2026-08-11 |
| [N10](#n10) | Unterminated `{@link}` will break Javadoc generation | **Fixed** 2026-08-11 |

### NIT

| | Finding | Status                                                |
|---|---|-------------------------------------------------------|
| [T1](#t1) | Duplicated scene-root `instanceof` check in the captor | **Resolved** with B9                                  |
| [T2](#t2) | Double-logged "Attempting to restore null DockContainer" | **Fixed** 2026-08-11                                  |
| [T3](#t3) | "{@return 2n" typo in `DockContainerBranchState` | **Fixed** 2026-08-11                                  |
| [T4](#t4) | "caontaining" typo in `DockContainerLeafMenuFactoryProvider` | **Fixed** 2026-08-11                                  |
| [T5](#t5) | Doubled comma in `DockableState` Javadoc | **Fixed** 2026-08-11                                  |
| [T6](#t6) | Stray `*` in a `BentoState` `@param` | **Fixed** 2026-08-11                                  |
| [T7](#t7) | "the diver positions" typo in the restorer | **Fixed** 2026-08-11                                  |
| [T8](#t8) | `{code X}` missing its `@` in two files | **Fixed** 2026-08-11                                  |
| [T9](#t9) | `enableAutoSave` takes a boxed `Long`; stray double space in `@see` | **Fixed** 2026-08-11                                  |
| [T10](#t10) | Constructor passes its own fields back into `enableAutoSave` | **Resolved** with B2                                  |
| [T11](#t11) | Inconsistent private-constructor convention in the two utility classes | **Fixed** 2026-08-11                                  |
| [T12](#t12) | `java.util.concurrent.Future` written fully qualified | **Resolved** with B4                                  |
| [T13](#t13) | Varargs `requireNonNull` checks the array, not its elements | **Fixed** 2026-08-11                                  |
| [T14](#t14) | `DockEventListener` implemented publicly, exposing `onDockEvent` | **Fixed** 2026-08-13 |
| [T15](#t15) | `DockingLayoutRestorer.layoutStorage` aliases the reader's instance | **Resolved** with M5                                  |
| [T16](#t16) | Tab/space inconsistency in `module-info.java` | **Fixed** 2026-08-11                                  |

Every identifier in the four tables above links to that finding's own numbered
section below. The anchors are explicit rather than derived from the heading text,
because this document appends a finding's outcome to its heading when it is fixed,
and a text-derived link would break silently the moment that happened.

---

## BLOCKER

### <a id="b1"></a>B1. `DefaultBentoProvider`'s `WeakHashMap` can never release an entry, and is not thread-safe — FIXED 2026-08-10

`impl/provider/DefaultBentoProvider.java:20-21, 33, 42`

The map is `WeakHashMap<String, Bento>`. `WeakHashMap` holds its *keys* weakly,
not its values. The key is the identifier string and the value is the `Bento` —
and `Bento` holds that same string in its own `identifier` field
(`core/.../Bento.java:28, 44-47`). So the strongly-held value strongly
references the weakly-held key, the key is never unreachable, and the entry is
never evicted.

Why it matters: the class Javadoc promises retrieval "using a weak reference
their identifiers", so the design intent is that a discarded `Bento` becomes
collectable. It never does. Every `Bento` ever registered is retained for the
provider's lifetime, and the provider is retained by
`AbstractAutoCloseableLayoutSaver.bentoProvider` for the life of the saver. This
is a straight memory leak that also silently defeats the documented contract.

Separately, `WeakHashMap` is unsynchronized. `addBento` runs on the application
thread while `getAllBentos()` is called from `BentoLayoutStateCaptor.captureBentoStates`
on the FX thread and from `AbstractAutoCloseableLayoutSaver.addListeners()` on
whichever thread constructed the saver. Concurrent iteration and mutation of a
hash map risks `ConcurrentModificationException` or a corrupted table.

Smallest fix: use `new ConcurrentHashMap<>()` and drop the "weak" claim from the
Javadoc. If weak semantics are genuinely wanted, store
`Map<String, WeakReference<Bento>>` so the value no longer pins the key, and
filter cleared references in `getBento`/`getAllBentos`.

**Fixed** (commit `5f99ab8`) via the second option: the map is now
`ConcurrentHashMap<String, WeakReference<Bento>>`, with cleared references filtered
in `getBento` and `getAllBentos`. That addresses both halves — the value no longer
pins the key, so a discarded `Bento` is collectable as the Javadoc claims, and the
concurrent map removes the unsynchronized-iteration hazard.

Two leftovers worth a follow-up, neither a blocker:

- Entries whose referent has been collected stay in the map as cleared references.
  Harmless in practice (a `String` key plus an empty reference per discarded
  `Bento`, and identifiers are typically reused), but it means the map only shrinks
  if something prunes it. Sweeping cleared entries inside `getAllBentos` would
  close it.
- `addBento` silently replaces an existing entry with the same identifier. Given
  the `Bento` identity contract is the identifier, a collision means two distinct
  `Bento`s claiming one identity — arguably worth a warning rather than a silent
  overwrite.

### <a id="b2"></a>B2. `AbstractAutoCloseableLayoutSaver` publishes `this` and starts a thread from its constructor — FIXED 2026-08-10

`impl/AbstractAutoCloseableLayoutSaver.java:77` → `:121-130`;
`impl/DockingLayoutSaver.java:54-56`

The constructor calls `enableAutoSave(...)`, which both schedules
`this::autoSave` on a new executor and registers `this` as a `DockEventListener`
on every `Bento` — all before the subclass constructor body has run.
`DockingLayoutSaver` assigns `bentoLayoutStateCaptor` and `layoutStateWriter`
*after* `super(bentoProvider)` returns.

Why it matters: any invocation of `autoSave()` → `saveLayout()` that begins
before those assignments complete dereferences null fields. The scheduled task's
first fire is five minutes out so the window is narrow, but this is an unsafe
publication of a partially-constructed object to both an executor and an
external event bus — the JMM gives no guarantee the subclass's final field
writes are visible to the scheduler thread even later.

Smallest fix: remove the `enableAutoSave(...)` call from the constructor and have
the factory start it — `DefaultDockingLayoutPersistenceProvider.getLayoutSaver`
(`impl/provider/DefaultDockingLayoutPersistenceProvider.java:79`) already has the
fully-constructed instance, so `var saver = new DockingLayoutSaver(...);
saver.enableAutoSave(...); return saver;` closes the hole without an API change.

**Fixed** along those lines, plus one addition and one consequence I had not
anticipated when writing the finding.

What landed:

- The `enableAutoSave(...)` call is gone from the constructor. Class and constructor
  Javadoc now state that auto-save must be started explicitly, and say why.
- New `static <T extends AbstractAutoCloseableLayoutSaver> T startAutoSave(T)`.
  Preferred over the bare `var saver = ...; saver.enableAutoSave(...); return saver;`
  sketch because it keeps the factory to one expression, preserves the concrete type
  so no cast is needed, and gives the construct-then-arm sequence a name that shows
  up at every call site.
- `DefaultDockingLayoutPersistenceProvider.getLayoutSaver` wraps its
  `new DockingLayoutSaver(...)` in `startAutoSave(...)`, so the documented way to
  obtain a saver still returns one that is already auto-saving.

**The consequence: this finding could not be fixed without also fixing M2.**
`close()` was gated on `if (isAutoSaveEnabled)`, which was survivable only because
the constructor unconditionally set that flag. Once the constructor stops arming
auto-save, that gate means a directly constructed saver — exactly what the
`DockingLayoutSaver` public constructor invites, and what three existing FTs do —
silently writes nothing on close. Fixing B2 alone would have traded a narrow race
for a guaranteed silent data loss on every non-provider code path. The gate is
therefore removed and `close()` always attempts the flush; `autoSave` already
short-circuits when no dock events were received, so an unchanged close stays cheap
and still writes nothing. See M2.

**Note this is a source-compatible but behaviourally breaking change** for anyone
constructing `DockingLayoutSaver` directly: they now get a saver that saves on
`close()` but does not save on a timer until they ask. Given the class is in an
`impl` package that arguably should not be exported at all (M1), and the provider
path is unaffected, that seemed the right trade — but it is a real change in
behaviour rather than a pure internal fix, and worth a line in the release notes.

Regression pinned by `ft/.../LayoutSaverConstructionFT.java`, seven tests:

- `constructorDoesNotStartAutoSave` — the direct property.
- `constructorDoesNotRegisterEventListeners` — the property that actually matters:
  fires a `DockEvent` at a `Bento` during the window and asserts the saver did not
  receive it, i.e. it had not yet subscribed.
- `enableAutoSaveAfterConstructionStartsAutoSave` — the capability moved rather than
  vanished.
- `startAutoSaveArmsAndReturnsTheSameSaver` — the helper arms and returns the same
  instance.
- `providerReturnsSaverWithAutoSaveRunning` — keeps the provider honest, since
  arming is now its responsibility.
- `closeSavesEvenWhenAutoSaveWasNeverEnabled` — the M2 half.
- `closeDoesNotSaveWhenNothingChanged` — guards the other direction, so removing the
  gate did not turn every close into an unconditional write.

A note on what these tests can and cannot do. The underlying defect is a memory
visibility race that cannot be triggered reliably by timing, so no test can
demonstrate the corrupted read directly. These assert the structural properties that
make the race impossible instead — nothing scheduled and nothing subscribed until
construction completes. That is weaker evidence than a failing-then-passing race
reproduction, and worth knowing when judging this fix.

Both halves were confirmed to fail independently: restoring the constructor call
failed `constructorDoesNotStartAutoSave` and `constructorDoesNotRegisterEventListeners`
while the other five passed; separately restoring only the `close()` gate failed
`closeSavesEvenWhenAutoSaveWasNeverEnabled` alone.

### <a id="b3"></a>B3. A `RuntimeException` from `saveLayout()` silently kills auto-save forever — FIXED 2026-08-10

`impl/AbstractAutoCloseableLayoutSaver.java:187-210`

`autoSave()` catches only `BentoStateException`. It runs under
`ScheduledExecutorService.scheduleAtFixedRate`, which **suppresses the exception
and cancels all subsequent executions** when a task throws.

Why it matters: one unchecked exception — an NPE from B9, an
`IllegalStateException` from `Platform.runLater` after toolkit shutdown, anything
a codec or storage implementation throws — permanently disables auto-save with
no log line and no signal to the application. The user keeps working, believes
their layout is being saved every five minutes, and loses everything since the
last successful save.

Smallest fix: widen the catch to `Exception` (or add a second
`catch (final RuntimeException e)`) and log at `error`.

**Fixed.** `autoSave` now catches `BentoStateException | RuntimeException` and logs
at `error`. This also underpins the B4 fix: it is what stops a timeout from taking
the scheduler down with it.

### <a id="b4"></a>B4. `callOnFxThread` can block its caller forever — FIXED 2026-08-10

`impl/PersistenceThreading.java:43-45`

When called off the FX thread, the method does `Platform.runLater(futureTask)`
then `get(futureTask)` with no timeout.

Why it matters: if the JavaFX toolkit has already shut down, the submitted
runnable never executes and `future.get()` never returns. The auto-save thread is
a daemon so it won't block JVM exit — but `LayoutSaver.close()` is documented for
try-with-resources and application-exit use, and `DockingLayoutSaver.saveLayout()`
routes through this method. A `close()` called from an application shutdown hook
after `Platform.exit()` hangs that thread indefinitely.

Smallest fix: use `future.get(timeout, unit)` with a bounded timeout and translate
`TimeoutException` into `BentoStateException`. Guarding with
`Platform.isFxApplicationThread()` is not sufficient — the toolkit state, not the
current thread, is the problem.

**Fixed**, with two corrections to the "smallest fix" above that turned out to
matter:

1. `get(timeout)` alone is not enough — it leaves the task queued in `runLater`, so
   it can still run and mutate JavaFX state for a caller that has given up. The fix
   calls `futureTask.cancel(false)` before throwing. `cancel(false)`, not `true`:
   the task is either unstarted or running on the FX thread, which must not be
   interrupted.
2. Reusing plain `BentoStateException` would have been actively harmful.
   `DockingLayoutRestorer.restoreLayout` catches that type and falls back to the
   default layout, so a spurious timeout would discard a good layout and the next
   auto-save would truncate the saved file with the default. A timeout now throws
   `BentoStateTimeoutException` (a `BentoStateException` subtype), which
   `restoreLayout` catches first and rethrows rather than swallowing.

What landed:

- New `api/BentoStateTimeoutException`.
- `PersistenceThreading`: `callOnFxThread(task)` bounded at
  `FX_CAPTURE_TIMEOUT_MILLIS` (10 s), plus a `callOnFxThread(task, timeoutMillis)`
  overload; cancel-then-throw on expiry; `InterruptedException` also cancels.
  Extracted a shared `unwrap(ExecutionException)`. The already-on-FX-thread branch
  stays untimed — there is no other thread to wait for, and offloading to create
  something to time out against would deadlock. `callOffFxThread` stays untimed
  because it owns its executor, so "never picked up" cannot happen.
- `DockingLayoutSaver`: private `saveLayout(long)`; `saveLayout()` uses the capture
  budget and a new `saveLayoutForShutdown()` override uses
  `FX_CLOSE_TIMEOUT_MILLIS` (3 s), since the thread calling `close()` may not be a
  daemon and blocking it delays exit.
- `AbstractAutoCloseableLayoutSaver`: `autoSave(boolean isShuttingDown)`; scheduler
  passes `false`, `close()` passes `true`. New `protected saveLayoutForShutdown()`
  defaulting to `saveLayout()`.
- `api/LayoutSaver`: documented the ordering constraint — close the saver *before*
  `Platform.exit()`. That is the root cause; the budgets only bound the damage.

Regression pinned by four tests in `ft/.../PersistenceThreadingFT.java`: the
timeout itself, the cancel-so-it-never-runs property, a busy-but-recovers case
guarding against over-tightening the budget, and the subtype relationship
`DockingLayoutRestorer`'s catch ordering depends on.

The tests were confirmed to fail without the fix: temporarily restoring the
unbounded `get()` produced `AssertionError: Expecting code to raise a throwable` on
both timeout tests, plus collateral failure of an unrelated test — a fair picture
of the original bug's blast radius. They cannot shut the toolkit down (it is not
restartable and the FT suite shares one JVM), so they reproduce the condition the
hang depends on by occupying the FX thread with a latch, with a `finally` release
and a self-release valve so a failure cannot wedge the suite.

### <a id="b5"></a>B5. Collapse state is never restored for any leaf below the root branch — FIXED 2026-08-10

`impl/DockingLayoutStateRestorer.java:243-247` vs `:319-359`

`restoreRootBranchContainer` calls `conditionallyCollapseLeaves(...)`.
`restoreBranch` — the path taken for every nested `DockContainerBranchState` —
never does. `getLeaves`/`getLeafStates` (`:554-586`) also only walk *direct*
children, so even the root call sees one level.

Why it matters: this breaks restoring previously saved layouts. Any layout deeper
than root→leaf (i.e. root→branch→leaf, which is the normal shape once a user
splits a pane) silently loses the collapsed state of every leaf. The state is
captured correctly by `BentoLayoutStateCaptor.buildLeafState:364`, written, read
back, and then dropped.

Smallest fix: add the same three-line sequence to `restoreBranch` after its child
loop and before/with `applyDividerPositions`:
`conditionallyCollapseLeaves(getLeaves(branch), getLeafStates(branchState), branch);`

**Fixed** exactly as described — the call added to `restoreBranch` after
`applyDividerPositions`, with a comment explaining why both call sites are needed.

Why one call in `restoreBranch` is sufficient, despite `getLeaves`/`getLeafStates`
seeing only direct children: every branch below the root is built by
`restoreBranch` itself, via
`restoreRootBranchContainer` → `restoreChildDockContainers` → `restoreDockContainer`
→ `restoreBranch` (recursive). Each branch collapses its own direct leaves as it is
built, so the root's pre-existing call plus this one cover every leaf at every
depth. Widening the helpers to walk the tree would have double-collapsed.

Regression pinned by `ft/.../NestedCollapseRestoreFT.java`, two tests:

- `restoreCollapsesLeafNestedBelowRootBranch` — a leaf one level below the root
  must come back collapsed. This is the case that regressed.
- `restoreLeavesUncollapsedSiblingExpanded` — its sibling, whose state says
  not-collapsed, must come back expanded. Without this control, the first test
  would also pass against a fix that collapsed every nested leaf indiscriminately.

Getting this test to be meaningful took more care than the fix. `core`'s
`setContainerCollapsed` returns `false` without acting in six separate cases, so a
carelessly built fixture passes whether or not the restorer even attempts the
collapse. The fixture therefore satisfies every precondition deliberately: two
leaves in the branch so a divider exists, the collapsing leaf first, a non-null
side, that side compatible with the branch orientation, and dockables present so a
header pane exists for `getCollapsedSize()` to measure. The tree is also attached
to a shown `Stage`, because collapsing reads laid-out divider geometry and core
defers the work when there is no `Scene`, and the assertions run after a
`robot.interact` fence so the restorer's queued `Platform.runLater` work has
completed.

Confirmed to fail without the fix: `restoreCollapsesLeafNestedBelowRootBranch`
failed on the `isCollapsed()` assertion while the sibling control test passed —
i.e. the test detects this specific defect rather than failing generically.

### <a id="b6"></a>B6. Leaf resize state always captures `true`, overwriting the saved value — FIXED 2026-08-10

`impl/BentoLayoutStateCaptor.java:358`

```java
leafStateBuilder.setResizableWithParent(leaf.isResizable());
```

`core/` defines no `isResizable()` override anywhere — verified by grep across
`core/src/main/java`. `DockContainerLeaf extends StackPane`, so this resolves to
`javafx.scene.layout.Region.isResizable()`, which unconditionally returns `true`.

The restorer applies the value through the correct accessor's setter —
`SplitPane.setResizableWithParent(leaf, ...)` (`impl/DockingLayoutStateRestorer.java:380-385`)
— and `DockingLayoutRestorerFT.java:265` asserts against
`SplitPane.isResizableWithParent(leaf)`, confirming that is the intended property.

Why it matters: a leaf the user pinned as not-resizable-with-parent is saved as
`true` and comes back resizable. The saved value is unconditionally wrong, so the
round trip is lossy for every layout.

Smallest fix: `leafStateBuilder.setResizableWithParent(SplitPane.isResizableWithParent(leaf));`

**Fixed** exactly as described, with a comment naming the trap so nobody
"simplifies" it back to `leaf.isResizable()`.

Regression pinned by `ft/.../LeafResizableWithParentRoundTripFT.java`, three tests:

- `captureRecordsResizableWithParentFalseForPinnedLeaf` — a leaf pinned with
  `SplitPane.setResizableWithParent(leaf, false)` must capture as `false`.
- `captureRecordsResizableWithParentTrueForFlexibleLeaf` — its sibling, left at the
  default, must still capture as `true`. Without this control the fix could have
  been "always capture `false`".
- `capturedThenRestoredLeafKeepsResizableWithParent` — the end-to-end property:
  capture a live tree, restore it, and both leaves keep their flag. This one states
  no expected value by hand; it compares the restored tree against the original.

These tests capture from a **live tree** rather than building state by hand, and
that is the whole point. Hand-built state exercises the restorer against a fixture
encoding whatever the test author believed, so it structurally cannot detect a
capture-side defect. That is why `BentoLayoutStateCaptorFT` — which does capture
live — missed this: it asserts side, `canSplit`, `pruneWhenEmpty` and selection, but
never `isResizableWithParent`. The bug was not a gap in *approach*, only a gap in
*which properties were listed*.

Confirmed to fail without the fix: both pinned-leaf tests failed while the
flexible-leaf control passed — i.e. they detect "always captures `true`"
specifically, not a generic breakage.

### <a id="b7"></a>B7. `uncollapsedSizePx` is captured but never restored — FIXED 2026-08-10

Captured at `impl/BentoLayoutStateCaptor.java:362`; grep for `uncollapsed` across
`impl/DockingLayoutStateRestorer.java` returns nothing.

Why it matters: `core`'s `DockContainerBranch.setContainerCollapsed` reads
`child.getUncollapsedSize()` when expanding
(`core/.../DockContainerBranch.java:413`). A restored leaf carries whatever
default a freshly-built leaf has, so the first time the user expands a
restored-collapsed pane it jumps to the wrong size instead of the size they left
it at. The value is faithfully persisted and then ignored, which is worse than not
persisting it — it reads as supported.

Smallest fix available inside this module: none that fully restores it —
`DockContainerLeaf.updateCollapsedSize` is `protected` and there is no public
setter, so honouring this field needs a small `core/` addition, which is outside
what I may change. Either (a) raise the core API need with the `core/` owner, or
(b) if that is not wanted, drop the field from `DockContainerLeafState` and the
captor so the state model stops advertising fidelity it cannot deliver. Leaving it
as-is is the only option I'd argue against.

**Fixed** via option (a), with one-off permission to edit `core/`.

`core/` change — 28 lines, purely additive, no existing line altered:
`DockContainerLeaf.setUncollapsedSize(double)`, a public counterpart to the
already-public `getUncollapsedSize()`. It returns `boolean` and no-ops when the
tracking properties are still bound, so it cannot silently appear to work.
Deliberately a new method rather than widening `updateCollapsedSize` to `public`:
that name says *collapsed* while it sets the *un*collapsed size, so exposing it
would have published a misleading name on a class other people maintain.

`persistence/api` change — a new `restoreUncollapsedSize` helper called from
`conditionallyCollapseLeaves`, **after** `setContainerCollapsed`.

That ordering is the whole subtlety, and it is why the setter has to no-op rather
than throw. While a leaf is uncollapsed, `uncollapsedWidth`/`uncollapsedHeight` are
*bound* to its live width and height (`DockContainerLeaf.java:70-71, 374-375`), so a
value written then is discarded on the next layout pass. Collapsing unbinds them
(`:370-371`), and only after that can a persisted size stick. Applying the size
before the collapse would look correct in the code and lose the value at runtime.
For a leaf that is not collapsed there is nothing to restore — its size comes from
the layout and the divider positions — so the helper returns early.

Regression pinned by `ft/.../LeafUncollapsedSizeRoundTripFT.java`, three tests:

- `restoredCollapsedLeafKeepsPersistedUncollapsedSize` — the stored value must
  survive restore.
- `restoredUncollapsedLeafTracksItsLiveSize` — an uncollapsed leaf carrying the
  same persisted value must *not* adopt it; its size belongs to the layout. Without
  this control the first test would also pass against an implementation that wrote
  the size unconditionally.
- `expandingRestoredCollapsedLeafReturnsToPersistedSize` — expands the restored
  leaf and checks it lands at the persisted width. This is the ordering guard: an
  implementation that applied the size before collapsing would still pass the first
  test's property read in some arrangements but fail here.

Confirmed to fail without the fix, and the failure values show the user-visible
symptom directly: `getUncollapsedSize()` returned **0.0** instead of 237.0, and
expanding the restored pane landed at **40px** instead of 237px. The uncollapsed
control test passed throughout.

One calibration note, because it is the kind of thing that looks like a fudge. The
expand test uses a 4px tolerance while the other two use 0.5px. The stored property
round-trips *exactly*; the rendered width does not, because core positions dividers
by their center point, so a divider contributes half its width as implicit padding
and the result is pixel-snapped — a stored 237px settles at 236px. Two tolerances
rather than one loose one, so the exact assertions stay exact. 4px is still far
tighter than the failure mode it guards against, which was 40px.

### <a id="b8"></a>B8. Auto-save scheduler state is mutable, shared across threads, and not volatile — FIXED 2026-08-11

`impl/AbstractAutoCloseableLayoutSaver.java:43-45, 105, 138-157, 173`

`isAutoSaveEnabled`, `scheduler`, and `scheduledSaveTask` are plain fields.
`close()` reads `isAutoSaveEnabled` at `:173`; `enableAutoSave`/`disableAutoSave`
write all three; the scheduler thread runs `autoSave()`. `listenerBentos`
(`:40`) is a plain `HashSet` mutated by `addListeners`/`removeListeners`.

Why it matters: `close()` from the FX thread racing `disableAutoSave()` from an
application thread can read a stale `isAutoSaveEnabled` and skip the final save,
or double-shutdown. Interleaved `enableAutoSave`/`disableAutoSave` can leave the
listener set out of sync with the actual registrations, leaking listeners on the
`Bento` event bus after close. The `closed` `AtomicBoolean` guards only the
close-once path, not this state.

Smallest fix: mark the three fields `volatile` and guard the
`enableAutoSave`/`disableAutoSave`/`close` bodies with a single private lock
object; make `listenerBentos` a `ConcurrentHashMap.newKeySet()`.

**Fixed**, but not quite as that sketch described — following it literally would
have deadlocked. Three corrections:

1. **`close()` must not hold the lock across its save.** The sketch says to guard
   the `close()` *body*. But `close()` calls `autoSave(true)` → `saveLayout()`,
   which hands work to the JavaFX application thread and waits; the JavaFX thread
   may itself be calling `enableAutoSave` or `close`, and would then block on the
   same lock. `close()` therefore saves *outside* the lock and only takes it for
   teardown. The `closed` `compareAndSet` already guarantees the save runs once, so
   nothing is lost by leaving it unguarded.
2. **`enableAutoSave` needs one critical section, not two.** It calls
   `disableAutoSave()` to reset before re-arming. Guarding each method separately
   would release the lock between the reset and the re-arm — a window where another
   thread sees auto-save as neither on nor off, or interleaves its own teardown.
   Teardown is now a private `disableAutoSaveInternal()` that assumes the lock, and
   the public `disableAutoSave()` is a thin `synchronized` wrapper.
3. **The `closed` check moved inside the lock.** Read outside, a concurrent
   `close()` could set the flag after the check passed, and the scheduler started
   just afterwards would survive the teardown that had already run.

`listenerBentos` stayed a plain `HashSet` rather than becoming
`ConcurrentHashMap.newKeySet()`. Now that every access is inside the lock, a
concurrent set would add cost without buying anything — and worse, it would suggest
the set is safe to touch unguarded, which it is not: the invariant is that the set
agrees with the registrations actually held on each `Bento` bus, and that spans
several operations rather than one. `isAutoSaveEnabled` is `volatile` as suggested,
because `isAutoSaveEnabled()` is a public read that should not contend for the lock;
the other two fields are lock-guarded only, and each carries a `Guarded by` comment.

Also documented: a class-level thread-safety section stating that
enable/disable/close are mutually exclusive, that `close()` is idempotent, and that
saving is deliberately *not* serialised — so a subclass overriding `saveLayout()`
or `saveLayoutForShutdown()` knows it can be entered from the scheduler thread and
from a `close()` caller concurrently.

Regression pinned by `ft/.../LayoutSaverAutoSaveLifecycleFT.java`, four tests. Three
hammer the lifecycle from four threads and assert end-state invariants — no
surviving `bentofx-layout-auto-save` thread, no listener still registered after
close, and auto-save off after concurrent close/churn. The fourth,
`closeDoesNotDeadlockAgainstFxThreadEnablingAutoSave`, is what guards correction 1:
it closes while the JavaFX thread repeatedly enables auto-save, and fails by
*timeout* rather than assertion.

Verification was stronger than expected. I anticipated intermittent failures, so I
ran the suite six times with synchronization stripped: all three race tests failed
**every** run, with concrete symptoms —
`ConcurrentModificationException` from `listenerBentos`, **five leaked scheduler
threads** where zero were expected, and `RejectedExecutionException` from a task
queued onto an executor that had already been shut down. Six further runs with the
fix in place passed 6/6. The leaked-thread count is the most telling: each
`enableAutoSave` starts a fresh single-thread executor, so a handle lost to a race
leaves a live thread that nothing can ever shut down.

### <a id="b9"></a>B9. The captor NPEs on a scene-less `Stage` — and the restorer can produce one — FIXED 2026-08-11

`impl/BentoLayoutStateCaptor.java:139` (and the duplicate at `:228`)

```java
final Parent sceneRoot = stage.getScene().getRoot();
```

`getAllStages()` (`impl/StageUtils.java:26-36`) returns every `Stage` in
`Window.getWindows()`. `Stage.getScene()` is null until a scene is assigned.

Why it matters: this is reachable from this module's own output.
`DockingLayoutStateRestorer.restoreDragDropStage:151-167` constructs
`new DragDropStage(...)` and only calls `setScene` **inside**
`getDockContainerRootBranchState().ifPresent(...)`. Persisted state with no root
branch therefore yields a live, registered, scene-less `DragDropStage`. The next
auto-save walks it and throws NPE — which then hits B3 and permanently kills
auto-save. That same scene-less stage also NPEs inside core's own
`WINDOW_HIDDEN`/`WINDOW_SHOWN` filters (`core/.../DragDropStage.java:58-70`) if
the application calls `show()`, which the persistence demo does unconditionally
(`demos/persistence/.../BoxApp.java:406`).

Smallest fix, two lines: in the captor, `final Scene scene = stage.getScene(); if
(scene == null) return Optional.empty();`. In the restorer, move `setScene` out of
the `ifPresent` so a stage always gets a scene (falling back to an empty root), or
skip creating the stage at all when the root branch state is absent.

**Fixed**, both halves, essentially as described:

- **Captor** — `getDockContainerRootBranch` now returns `null` for a scene-less
  stage instead of dereferencing. `toDragDropStageRoot` delegates to it rather than
  repeating the `instanceof`, which also retires NIT T1.
- **Restorer** — `setScene` moved out of the `ifPresent`. State with no persisted
  root branch now gets `dockBuilding.root()` as an empty fallback and logs a
  warning. The "skip creating the stage" alternative was not viable:
  `BentoLayout.BentoLayoutBuilder.addDragDropStage` calls `requireNonNull`, so the
  restorer has no way to omit a stage without a wider API change.

Both were verified independently by reverting one at a time, and the results are
worth recording because they contradict part of the original finding.

**Reverting only the captor guard** failed `captureIgnoresSceneLessDragDropStage`
with exactly the predicted `NullPointerException: Cannot invoke
"javafx.scene.Scene.getRoot()" because ... getScene() is null`.

**Reverting only the restorer fix** failed three of the four tests — including the
captor test, because a scene-less stage escaping into the shared JavaFX window list
poisons unrelated captures. That is the cross-contamination the finding predicted,
observed directly.

**Correction to the finding.** It said a scene-less stage is reachable because the
restorer produces one and "the next auto-save walks it". That is only half right:
`Window.getWindows()` lists **only showing** windows, so a scene-less stage that is
never shown is invisible to the captor and harmless to it. The hazard needs the
stage to be shown — which is exactly what callers do with every stage in a restored
layout (`demos/persistence/.../BoxApp.java:406`), so the conclusion stands, but the
mechanism is narrower than stated. My first version of the captor test used an
unshown stage and therefore **passed with the guard removed**; it only became a real
test once the stage was shown. Worth knowing for anyone writing further tests in
this area.

**Confirmed as a genuine core defect, out of scope here.** Core's `DragDropStage`
installs `WINDOW_HIDDEN`/`WINDOW_SHOWN` filters that do `getScene().getRoot()`
unguarded (`core/.../DragDropStage.java:58-70`). Hiding a scene-less stage therefore
throws from inside core, which is observable in this suite: the captor test's own
leftover stage failed a later teardown until the test attached a scene before
closing it. This module can no longer *create* such a stage, so the core path is
unreachable through persistence — but a `DragDropStage` constructed directly and
shown without a scene still breaks on hide. Worth raising with the `core/` owner
separately.

### <a id="b10"></a>B10. Deferred divider positions overwrite synchronously-applied collapse geometry — WITHDRAWN 2026-08-11, REOPENED AND FIXED 2026-08-13

`impl/DockingLayoutStateRestorer.java:239-247` and `:532-545`

`applyDividerPositions` wraps each `setDividerPosition` in its own
`Platform.runLater`. `conditionallyCollapseLeaves` is then called
**synchronously** on the next line.

Why it matters: `core`'s `setContainerCollapsed` resizes the child to its
collapsed size and disables the adjacent divider
(`core/.../DockContainerBranch.java:406-418`), i.e. it moves dividers. Because the
restorer's divider writes are queued and the collapse write is immediate, the
queued writes land *after* the collapse and reset those dividers to the saved
uncollapsed positions. The leaf ends up flagged collapsed but sized as though it
were not. Compounding it, at restore time the containers have no `Scene` yet, so
core's own `setContainerResizable` defers via its internal `addQueue`
(`core/.../DockContainerBranch.java:326-329`) — and the ordering between core's
queue and this module's `runLater` calls is unspecified.

Confidence note: this follows from reading both sides rather than from a run — the
ordering hazard is definite, the exact visual outcome should be pinned by an FT.

Smallest fix: apply dividers and collapse inside a *single* `Platform.runLater`,
in that order, rather than one `runLater` per divider plus a synchronous collapse.

**Withdrawn — no code change made.** That confidence note was the right instinct,
and pinning the outcome with a throwaway probe is what showed the finding does not
hold. Measured on a real capture → restore → expand cycle sourced from a **live**
layout rather than hand-built state, at both tree depths:

| | root→leaf | root→branch→leaf |
|---|---|---|
| source dividers | `[0.0539]` | `[0.0539]` |
| captured | `{0=0.0539}` | `{0=0.0539}` |
| restored | `[0.0539]`, collapsed, 40px | `[0.0539]`, collapsed, 40px |
| after expanding | **395px** | **396px** |

Saved `uncollapsedSizePx` was 396 in both. Divider positions, collapsed flag and
uncollapsed size all survive; expanding returns the pane to the width the user left
it at.

**Why the finding was wrong.** It assumed a collapsed leaf could carry an
*uncollapsed* divider position, so the queued divider write would fight the
collapse. It cannot: capture reads dividers from a live tree, and a collapsed leaf's
divider *is* its collapsed position (`0.0539` above, not `0.75` or `0.5`). The two
writes agree, so their ordering is unobservable.

The ordering hazard is real in the abstract — feeding hand-built state that pairs
`setCollapsed(true)` with `addDividerPosition(0, 0.75)` does discard the 0.75. But
that state is unreachable through capture, and discarding the contradiction is the
correct outcome for a caller who hand-writes one. Note the observed result is also
the *opposite* of the predicted symptom: the leaf ends up correctly collapsed at
40px, not "flagged collapsed but sized as though it were not".

Two specific errors in the original reasoning, worth recording because both are easy
to repeat:

1. I read `Platform.runLater` as landing *after* the synchronous collapse. In fact
   core defers collapse geometry through its own chain — `addQueue` →
   `BentoUtils.scheduleWhenShown` → `Platform.runLater` → `layoutChildren`
   (`core/.../DockContainerBranch.java:520-529`,
   `core/.../BentoUtils.java:171-196`) — which lands *later* than this module's
   queue, not earlier. The "unspecified ordering" caveat was pointing at the right
   uncertainty and I resolved it the wrong way.
2. B7's fix had already removed the consequence I was worried about. Expansion size
   now comes from the restored `uncollapsedSizePx`, not from divider arithmetic, so
   even a wrong divider would not produce the wrong expanded width.

No FT was added. The probe passes against today's code, so it would guard a
regression rather than pin a fix, and it is ~150 lines of timing-sensitive setup for
behaviour that already works. The three round-trip FTs from B5, B6 and B7 already
cover this area from the directions that did find real defects.

**Reopened and FIXED 2026-08-13.** The withdrawal was wrong, and the last paragraph
above is exactly why: declining to write the test is what let this stand. The general
round-trip test (theme 1) found it on its first run.

The withdrawal reasoned about *ordering* between this module's queue and core's, and
that reasoning still holds. It missed a second failure mode entirely: the collapse can
compute the wrong geometry regardless of ordering, because
`DockContainerLeaf.getCollapsedSize()` (`core/.../DockContainerLeaf.java:391-397`)
reads the headers' **live** width or height, and those are `0` until a layout pass has
measured them. `restoreLayout` hands the tree back unattached and the application
attaches it afterwards, so at collapse time there may have been no layout pass at all
- and a `Platform.runLater` does not wait for one, it just waits for the next pulse.

Symptom: a restored collapsed leaf is pinned to roughly the width of a divider - about
3px - instead of its header, so the pane comes back as a sliver.
`LayoutRoundTripFT` reported it as
`dividerPositions.0` differing, actual `0.0037593984962406013` against expected
`0.05388471177944862`, on roughly **one run in four**. That rate is the reason a
one-shot manual probe concluded "works fine".

Fixed by gating only the collapse on real layout bounds: `conditionallyCollapseLeaves`
now runs from `collapseLeavesOnceLaidOut`, which fires immediately when the branch
already has non-empty `layoutBounds` and otherwise attaches a one-shot listener.
Divider positions were deliberately left exactly as they were, deferred by one pulse
and applied whether or not the tree is ever attached - gating those too broke
`DockingLayoutRestorerFT.restoreLayoutBuildsRootBranchesAndDragDropStages`, which
asserts divider positions on a tree it never attaches or shows. That test was the
useful signal that the fix had been scoped too widely.

Measured rather than argued, in both directions: 0 failures in 19 runs with the gate
(10 targeted plus 5 full functional-test suites plus 4 earlier), and 2 failures in 8
runs with the gate removed. At the observed 25% rate, 19 clean runs by luck is about
0.4%.

---

## MAJOR

### <a id="m1"></a>M1. `module-info` exports both `impl` packages, making implementation detail public API — FIXED 2026-08-11

`module-info.java:31-32`

```java
exports software.coley.bentofx.persistence.impl.provider;
exports software.coley.bentofx.persistence.impl;
```

This makes `DockingLayoutSaver`, `DockingLayoutRestorer`,
`AbstractAutoCloseableLayoutSaver`, `StageUtils`, `DefaultBentoProvider`, and
`DefaultDockingLayoutPersistenceProvider` part of the module's compatibility
surface. The whole point of the `api`/`impl` split and of routing construction
through `DockingLayoutPersistenceProvider` is that `impl` can change freely; the
export gives that away. `StageUtils` in particular is a `public` utility class
with no `api` counterpart.

Smallest fix: drop both `exports`. `provides ... with
DefaultDockingLayoutPersistenceProvider` (`:35`) does **not** require the package
to be exported — the module system instantiates the provider reflectively without
it. Consumers who need `DefaultBentoProvider` are the argument for keeping one
export; if so, promote that one class to an `api` package rather than exporting
all of `impl`.

**Fixed** — both `exports` dropped, and no class promoted to `api`.

The census first, because it decided the shape of the fix. Across the whole repo,
exactly **one** compilation unit outside this module imported either package:
`demos/persistence/.../BoxApp.java`, for two classes, both from `impl.provider`.
Nothing anywhere used the plain `impl` package, so that export went with no
consumer at all — `DockingLayoutSaver`, `DockingLayoutRestorer`,
`AbstractAutoCloseableLayoutSaver` and `StageUtils` were public for nobody. (The
many other `software.coley.bentofx.persistence.impl.codec.*` imports in the repo
belong to the codec modules' own `impl` packages and their own `module-info`s;
they are unaffected.)

That left `DefaultBentoProvider` as the only real question. It was first answered by
giving the demo its own `BoxAppBentoProvider`, and then answered better: see the
`ServiceLoader` discussion below, which ends in a `BentoProvider.of(Bento...)` factory
on the exported interface. The demo class is gone, the implementation is still
unexported, and no application needs to write a registry.

`DefaultDockingLayoutPersistenceProvider` needed no replacement, only a different
route to it. The machinery was already in place and unused — this module's `provides`
and the demo's own `uses` clause — so this is the design finally being used as
intended rather than a new mechanism.

The first attempt put the lookup in `BoxApp`, and that was the wrong home. It made
every consuming application declare a `uses` clause, import `ServiceLoader`, and
repeat the same `findFirst().orElseThrow(...)` — pushing a mechanism into every caller
for something that is not even the replaceable-component use case. It is now behind
`api/DockingLayoutPersistence.provider()`, so `BoxApp` reads
`DockingLayoutPersistence.provider()` and the demo's `module-info` needs nothing at
all. The `uses` clause moved into this module, which now both `provides` and `uses` the
same service — legal, and the comment there says why so nobody prunes it as redundant.

Two things came free with the move. The footgun demonstrated below — forget `uses`, get
a `ServiceConfigurationError` — is now impossible for a consumer to trigger, because
the clause lives in the module that does the lookup. And N8's concern about loader
choice now has exactly one place to be right: `provider()` resolves against its own
class's loader rather than the thread context loader, so a container leaving the
context loader pointing elsewhere cannot turn a present implementation into
"none found".

**Re-verified after the move, both directions.** The demo runs clean with no `uses`
clause of its own; commenting out the *api* module's `uses` produces
`ServiceConfigurationError: module bento.fx.persistence.api does not declare 'uses'`
and exit 1. So the lookup demonstrably happens where the comment claims it does.

**Verified by running the demo, not by compiling it.** A compile pass proves nothing
about service resolution across a module boundary, which is the one genuinely new
behaviour here. The demo launched and stayed up with no errors, and the negative
control is what makes that meaningful: commenting out the demo's `uses` clause
produced `ServiceConfigurationError: module bento.fx.demo.persistence does not
declare 'uses'` and exit 1. So the provider really is arriving through the module
system's service machinery from a non-exported package.

One precision about the error paths, since they are easy to conflate: the
`orElseThrow` in `BoxApp` covers "no implementation module on the path" and returns
the same advice as `DefaultDockingLayoutPersistenceProvider`'s own message. A
*missing* `uses` never reaches it — `ServiceLoader` throws `ServiceConfigurationError`
first. Both are now known-reachable rather than assumed.

Note this is a **breaking change for any consumer outside this repo** that imported
either `impl` package — which is the entire point of the finding, and the reason it
was worth doing before more consumers appeared rather than after. It also retires
the concern recorded under B2, that its behavioural change was only observable
because `impl` was exported.

**Why `DefaultBentoProvider` was not given the same `ServiceLoader` treatment as
`DefaultDockingLayoutPersistenceProvider`.** This is the obvious next question — if
every application needs a `BentoProvider`, having each write its own registry is
duplication — and it was asked and settled rather than overlooked. The two providers
are not the same kind of thing:

- `DockingLayoutPersistenceProvider` is a **stateless factory**. A no-arg instance is
  immediately functional and any instance is equivalent to any other, which is exactly
  what `ServiceLoader` supplies.
- `BentoProvider` is a **stateful registry**, and three things rule the mechanism out.
  `addBento` is not on the interface (only `getBento`/`getAllBentos` are), so an
  application could not populate an interface-typed instance handed to it.
  `ServiceLoader.load` caches instances *within* a loader, not across calls, so each
  `load(...).findFirst()` yields a fresh empty registry — populate one, and the saver
  reads another. And the framework never looks one up: every `getLayoutSaver` and
  `getLayoutRestorer` overload takes `BentoProvider` as a **parameter**, and the api
  module declares `uses` only for `LayoutCodecProvider` and `LayoutStorageProvider`, so
  a `provides BentoProvider with ...` clause would have no effect whatsoever.

The sound way to remove the duplication is therefore not a service registration but
exposing a ready-made implementation through the `api` — the alternative this finding
already names. Promoting the *class* into `api.provider` was rejected: that publishes a
concrete type permanently. **What landed instead is a static factory on the interface**,
`BentoProvider.of(Bento...)`, delegating to the still-unexported
`DefaultBentoProvider`. One method rather than a new class or an exported
implementation, and `BoxAppBentoProvider` is deleted — the demo is now
`BentoProvider.of(bento)` at field-initialisation, and the `addBento` call it needed is
gone with it.

A factory rather than a second `DockingLayoutPersistence`-style utility class, because
the two problems are not alike. `DockingLayoutPersistence` earns a class: the
implementation must be *discovered*, and the point is keeping `ServiceLoader` out of
callers. Nothing is discovered here — there is only a constructor to hide, and a static
on an interface that already exists hides it. `List.of` rather than a `Lists` class.

The evidence for doing it at all came from counting rather than arguing:
`DefaultBentoProvider` is constructed about **35 times across 13 test and FT files**,
almost always as `new DefaultBentoProvider(bento)` with everything supplied up front.
That both proves it is the genuinely useful general-purpose registry rather than a demo
convenience, and shows the shape worth exposing — a fixed set, known at start-up.

**Two limits of the factory, both deliberate.** It returns `BentoProvider`, which has no
mutator, so it serves a fixed set only; an application whose `Bento`s come and go
implements the interface itself, which is what the SPI is for. Adding `addBento` to the
interface was the alternative and is worse: it would force every application-written
implementation to support mutation that may mean nothing to it, and the framework never
calls it. Second, the weak references B1 introduced are now reachable from public API,
so `of` documents the consequence out loud — the caller must keep its own reference,
and `BentoProvider.of(new Bento("x"))` with no field for it is a mistake. That hazard
existed before; it is now at least written where a caller will read it.

Pinned by `test/.../api/provider/BentoProviderTest.java`, two tests: every `Bento`
passed in is resolvable by identifier and appears in `getAllBentos`, and the no-argument
form resolves nothing. Confirmed to fail first — making the factory ignore its argument
failed `ofResolvesEveryBentoItWasGiven` at the `getBento` assertion. Small, but this is
public API whose whole body is one delegation, and a delegation is exactly the kind of
line that gets silently rewritten.

**A new finding fell out of settling this**, not in the original review, and it is
wider than the one interface. `api/provider/BentoProvider.java:9` opens "`{@code
ServiceLoader}` compatible Service Provider Interface", which is false — nothing loads
it that way and, per the reasoning above, it cannot usefully become one. Grepping the
package rather than assuming, that same sentence opens **all nine** files in
`api/provider`, and only three of them earn it:

| Accurate | Why |
|---|---|
| `LayoutCodecProvider` | `uses` declared in `module-info:16` |
| `LayoutStorageProvider` | `uses` declared in `module-info:17` |
| `DockingLayoutPersistenceProvider` | `provides` here, `uses` in consumers |

| Inaccurate | Actually |
|---|---|
| `BentoProvider` | constructor/method parameter |
| `DockableStateProvider` | parameter |
| `DockableMenuFactoryProvider` | constructed directly by the caller |
| `DockContainerLeafMenuFactoryProvider` | parameter |
| `StageIconImageProvider` | parameter |
| `package-info.java:2` | blanket claim over the whole package |

So five interfaces and the package summary told a reader to go looking for a
`ServiceLoader` registration that does not exist and would not work. That is a worse
class of documentation defect than the N9/T-series typos: those were cosmetic, this one
sends you down a dead end and, in `BentoProvider`'s case, invites exactly the redesign
rejected above. Same shape as B1's "weak reference" Javadoc — documentation describing
a mechanism the code does not use.

**Fixed** 2026-08-11, per file rather than with one shared sentence, because the truth
differs. `BentoProvider` says it is a registry of live objects the application
populates and hands over. `DockableStateProvider` says a persisted layout records
which `Dockable`s were open but not what was inside them, so rebuilding content is
necessarily the caller's job. `StageIconImageProvider` and
`DockContainerLeafMenuFactoryProvider` say they are optional arguments and what
passing `null` means. `DockableMenuFactoryProvider` gets the sharpest correction,
because investigating it turned up something the finding had not: it is not a parameter
either. No `DockingLayoutPersistenceProvider` method accepts one and nothing in the
module references it — it exists purely as a shape for a `DockableStateProvider`
implementation to use internally, so if the application does not call it, nothing
does.

`package-info.java` now splits the package into the two kinds explicitly, listing
which three interfaces are `ServiceLoader`-discovered and which are supplied by the
caller. That is the file that had been doing the most damage: a blanket claim over all
nine.

The three accurate headers were left alone —
`DockingLayoutPersistenceProvider`, `LayoutCodecProvider` and `LayoutStorageProvider`
earn the description, and the last two already carried a second paragraph explaining
the provider/product split.

Checked with `javadocLint`, which is what made the correction safe to make in bulk: a
`{@link Dockable}` in `DockableMenuFactoryProvider` would not have resolved, since that
file does not import the type, and the lint caught the class of mistake before the
commit rather than after. Full build clean, 75 tests, 0 failures.

**Revised once more, and the revision is the more useful version.** The first pass
corrected each false claim by denying it — "not discovered through `ServiceLoader`" —
which is still a `ServiceLoader` mention on every one of those files, and mentions are
the thing worth reducing. `ServiceLoader` has one job in this framework: letting a
deployment replace the codec or the storage implementation without a code change.
Everything else naming it is noise, and noise is what made five files wrong in the
first place.

So the distinction is now carried structurally rather than repeated in prose.
`LayoutCodecProvider` and `LayoutStorageProvider` extend
`LayoutPersistenceComponentProvider`; nothing else does. That is the marker for
"replaceable at runtime", it cannot drift the way a copied sentence did, and it was
already in the code — `LayoutPersistenceComponentProvider` says "discovered at
runtime" and `LayoutPersistenceProfile` describes selecting "from the runtime
dependencies", both without naming the mechanism. What landed:

- The four negations are gone; each interface states positively who writes it and why.
- `LayoutCodecProvider` and `LayoutStorageProvider` lost their "`ServiceLoader`
  compatible Service Provider Interface" headers too, in favour of what they are for -
  the format a layout is written in, and where it is written to.
- **One** mechanical mention survives, on `LayoutPersistenceComponentProvider`, aimed
  at the only reader who needs to type the word: someone writing a replacement, who
  needs to know it is registered with `provides` in their own `module-info`.
- `package-info` is organised by who writes what rather than by mechanism, with
  `DockingLayoutPersistenceProvider` called out as belonging to neither group.

The count, for the record: `ServiceLoader` appeared in **nine** files across
`api/provider`. It now appears in **two** places outside `impl/` — the entry point that
performs the lookup, and that single how-to-register paragraph. The mentions inside
`impl/provider/DefaultDockingLayoutPersistenceProvider` are load-bearing and were left
alone; that class is the code doing the discovery.

**The same claim had spread to the demo**, which the audit only turned up because the
count was worth checking rather than assuming: five files in
`demos/persistence/.../provider/` described themselves as "`ServiceLoader` compatible
Service Provider implementation" and not one of them is discovered — `BoxApp`
constructs all of them with `new`. Corrected the same way, and that package's
`package-info` now says plainly that the replaceable parts are the codec and storage
modules.

### <a id="m2"></a>M2. `close()` does not save when auto-save is disabled, contradicting its own Javadoc — FIXED 2026-08-10

`impl/AbstractAutoCloseableLayoutSaver.java:166-179`, class Javadoc `:20-29`

The class Javadoc states that try-with-resources will "automatically call
`close()` to save the docking layout when the try block exits". But `close()`
saves only `if (isAutoSaveEnabled)`.

Why it matters: an application that calls `disableAutoSave()` — a documented
public method whose Javadoc says only that it disables *automatic* saving — has
silently also disabled save-on-exit. The two are independent concerns and the
API presents them as such.

Smallest fix: in `close()`, call `autoSave()` unconditionally (it already
short-circuits when no dock events were received), or save unconditionally and
document that `close()` always flushes.

**Fixed** with the first option, as part of B2 rather than on its own — see B2 for
why the two could not be separated. The `if (isAutoSaveEnabled)` gate is gone, with
a comment recording that saving on close and saving on a timer are independent
concerns. Covered by `closeSavesEvenWhenAutoSaveWasNeverEnabled` and
`closeDoesNotSaveWhenNothingChanged` in `ft/.../LayoutSaverConstructionFT.java`.

### <a id="m3"></a>M3. `callOffFxThread` blocks the FX thread and creates a thread per call

`impl/PersistenceThreading.java:59-68`

The branch is only taken when the caller *is* on the FX thread. It then submits
to a fresh single-thread executor and blocks on `future.get()`. Java 19+
`ExecutorService.close()` additionally blocks until termination.

Why it matters: the class Javadoc says the point is "allowing codec and storage
work to run off that thread". When `saveLayout()` is invoked from the FX thread —
the common case for a window close handler or an exit-time `close()` — the codec
encode plus the storage write happen while the FX thread sits blocked, freezing
the UI for the duration of the I/O. The work moves off the FX thread in name only.
The per-call thread creation is secondary but avoidable.

Smallest fix: hold one shared daemon `ExecutorService` in the class for the
off-thread path instead of creating one per call, and have callers that can afford
it use an async form. At minimum document that the FX thread blocks here, because
right now the design reads as though it doesn't.

**Fixed** 2026-08-12, in the two parts this finding actually contains, and only one
of them was a defect.

The thread per call is gone. `PersistenceThreading` now holds one shared
single-thread `ExecutorService` on a named daemon thread
(`bentofx-persistence-io`) and submits to that. Because every caller blocks on its
own result, the work was already serialised, so a thread per call bought nothing
beyond the cost of creating it plus - on Java 19+ - the `close()` that blocks until
that thread terminates. It is deliberately never shut down: one idle daemon thread
for the life of the JVM is cheaper than giving a static utility a lifecycle that
every saver and restorer sharing it would have to agree on, and daemon means a
storage write that never returns still cannot hold up exit. Covered by
`callOffFxThreadReusesOneSharedThreadAcrossCalls` in
`ft/.../PersistenceThreadingFT.java`, which fails with the per-call executor
restored.

**The blocking is documented, not removed**, and that is the deliberate half of
this fix. The hand-off is intended behaviour, not an accident:
`saveLayoutEncodesAndWritesAwayFromFxThreadWhenCalledOnFxThread` in
`ft/.../DockingLayoutSaverFT.java` asserts that the codec and storage run on a
thread that is not the JavaFX thread, which keeps JavaFX state out of reach of
third-party codec and storage implementations. What the finding correctly names is
that off-thread was being conflated with asynchronous. Both entry points are
synchronous by contract, and making `saveLayout()`/`restoreLayout()` async is an
API change - errors would need somewhere to go other than the caller's `throws` -
which is out of scope for a review fix and would need its own decision.

So the Javadoc now says it in the two places a caller reads: the class comment
notes that off that thread is not the same as asynchronous, and
`callOffFxThread`'s own comment states in bold that the calling thread blocks
until the task completes, JavaFX thread included, and that a save from a window
close handler or an exit-time `close()` therefore freezes the UI for the duration
of the I/O. Callers on the JavaFX thread that cannot afford that have to arrange
their own asynchrony.

### <a id="m4"></a>M4. The public `DockContainerStateBuilder` builds state the restorer silently discards

`api/state/DockContainerState.java:47-81`; `impl/DockingLayoutStateRestorer.java:296-307`

`DockContainerState` is a concrete, publicly instantiable class with a public
builder. The restorer's switch handles only `DockContainerBranchState` and
`DockContainerLeafState`; anything else hits `default ->`, logs a warning, and
yields `null`, and the caller drops it (`:275-277`, `:341-350`).

Why it matters: the public API lets a caller construct a `DockContainerState` that
the module will accept, encode, and then silently throw away on restore. Contrast
`core`, where `DockContainer` is `sealed ... permits DockContainerBranch,
DockContainerLeaf` (`core/.../DockContainer.java:23`) — which is exactly why the
captor's matching switch at `impl/BentoLayoutStateCaptor.java:287-290` needs no
`default` and is compiler-checked for exhaustiveness. The state hierarchy mirrors
a sealed hierarchy without being sealed, so the compiler cannot catch the gap.

Named problem this design causes today: the `default ->` arm exists only because
the hierarchy is open, and it converts a programming error into a silent data
loss at restore time.

Smallest fix: make `DockContainerState` `abstract` and delete its public builder
(nothing in `main` constructs one). Sealing it to its two subtypes additionally
lets the restorer's switch drop the `default` arm and be compiler-verified, at the
cost of a `permits` clause.

**Fixed** 2026-08-12, taking the sealing option rather than the minimum, because
`abstract` alone would not have closed the hole: the constructor is `protected`, so
any module could still have subclassed the class directly and the `default ->` arm
would have had to stay to catch it.

`DockContainerState` is now `public abstract sealed class ... permits
DockContainerBranchState, DockContainerLeafState`, and `DockContainerStateBuilder`
is gone. Both subclasses are `non-sealed`, which mirrors `core` exactly - the
finding's own model - and keeps `DockContainerRootBranchState extends
DockContainerBranchState` working unchanged. Nothing in `main`, `test`, `ft` or
`itp` constructed a bare `DockContainerState`, so no caller changed.

That let three pieces of dead handling go with it. The restorer's switch dropped its
`default` arm and `restoreDockContainer` lost its `@Nullable` return, which in turn
emptied the `if (container != null)` guard at both call sites (`:296-307` and inside
`restoreBranch`) - the guards existed only to skip a container the `default` arm had
already decided to lose. `BentoStateMapper:144-158` in `codec/common` had the same
`default -> logger.warn(...)` arm over the same two types; it is unreachable once the
hierarchy is sealed, so it went too. That is the one part of this fix outside the
reviewed module, and leaving a warning that can never fire seemed worse than the
one-file reach.

No new regression test, deliberately, and this is the one finding where that is the
honest answer: **the compiler is the test**. Removing `sealed`, or adding a third
permitted subtype without handling it, makes both pattern switches non-exhaustive and
fails compilation. A reflective assertion that the class is still sealed would only
restate what the build already refuses to let through. Verified by
`:persistence:api:test`, `:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec test suites, and
`checkJSpecify` plus `javadoc` on both touched modules.

### <a id="m5"></a>M5. `LayoutStorage` ownership is unspecified, and two components each close it - FIXED 2026-08-12

`api/storage/LayoutStorage.java:13, 40-43`; `impl/LayoutStateReader.java:54-59`;
`impl/LayoutStateWriter.java:51-56`

`LayoutStateReader.close()` and `LayoutStateWriter.close()` each close the
`LayoutStorage` they were handed. `DockingLayoutSaver` and `DockingLayoutRestorer`
both have **public** constructors taking a `LayoutStorage` directly
(`impl/DockingLayoutSaver.java:37`, `impl/DockingLayoutRestorer.java:60`).

Why it matters: nothing documents that the storage is transferred. An application
that builds a saver and a restorer over one `LayoutStorage` — the natural reading,
since it is one layout file — gets its restorer's storage closed when the saver
closes. The provider path happens to avoid this by calling
`getLayoutStorage(...)` twice (`DefaultDockingLayoutPersistenceProvider.java:73-77,
107-111`), which is itself undocumented and load-bearing.

Smallest fix: document ownership transfer on both public constructors and on
`LayoutStorage.close()` ("closed by whichever saver/restorer it is passed to; do
not share one instance between them").

**Fixed** 2026-08-12, taking the ownership-transfers reading rather than the
caller-keeps-it one, because that is what the code already does on every path -
the alternative would have meant stripping `close()` out of the reader and the
writer and giving the provider something to close instead, which is more code to
say the same thing.

The documentation went where the review asked, on `LayoutStorage`'s own type
header and its `close()`, and on both public constructors. It also went one place
the finding did not name, and that turned out to be the load-bearing half:
`LayoutStorageProvider.getLayoutStorage` now requires a **fresh instance per
call**. Without that, `DefaultDockingLayoutPersistenceProvider` calling it twice
buys nothing - a provider that caches one storage per layout, which is a
perfectly natural thing to write and nothing forbade, hands the same object to
the saver and the restorer and the defect is back with the provider path looking
correct. The two calls are the mechanism; the freshness requirement is what makes
the mechanism work. Both call sites now carry a comment saying so, since a
lone-looking duplicate call is exactly the kind of thing a later reader
"simplifies".

T15 is resolved with it, and it belongs here rather than in the NIT sweep for the
reason held back earlier: once ownership transfers to the component that receives
the storage, `DockingLayoutRestorer` holding its own aliased `layoutStorage`
field contradicts the rule the same class now documents - two references, one
owner. The field, its constructor parameter and its `requireNonNull` are gone;
`doesLayoutExist()` delegates to a new package-private
`LayoutStateReader.layoutExists()`, so the reader is the only thing holding the
instance it closes. No behaviour change - same object, same `exists()` call - and
no call site moved, because the package-private three-argument constructor the
change narrows had no callers outside the class.

One test, `closingAProviderCreatedSaverLeavesTheRestorersStorageOpen`, which is
the round trip the finding describes: take a saver and a restorer from one
provider, close the saver, assert the restorer's storage is still open. It needed
the existing `CloseTrackingLayoutStorageProvider` fixture fixed first - it
returned one cached instance for every call, so it could not have detected
sharing and neither of the two tests already using it would have noticed. It now
hands out a fresh storage per call, the way the contract it stands in for
requires, and records them all. Verified to fail before the fix by making the
provider reuse the saver's instance: the assertion fires and the other nine tests
in the class still pass, so it is specific to this defect. Verified by
`:persistence:api:test`, `:persistence:api:functionalTest`,
`:persistence:codec:common:test`, `checkJSpecify` and `javadoc`.

### <a id="m6"></a>M6. `LayoutStateWriter` mislabels storage failures as encode failures and re-wraps typed exceptions - FIXED 2026-08-12

`impl/LayoutStateWriter.java:42-48`

```java
try (final OutputStream out = layoutStorage.openOutputStream()) {
    layoutCodec.encode(bentoStateList, out);
} catch (final Exception ex) {
    throw new BentoStateException("Failed to encode BentoState", ex);
}
```

`catch (Exception)` covers `openOutputStream()`'s `IOException` (a *storage*
failure — disk full, permission denied, unwritable parent) and reports it as an
encode failure. It also catches `BentoStateException` thrown by `encode` and wraps
it in a second `BentoStateException`, burying the codec's own message one cause
deeper.

Why it matters: this is the diagnostic that reaches the user when saving fails,
and it names the wrong subsystem. `LayoutStateReader:45` is asymmetric — it
catches only `IOException` and lets `BentoStateException` through — so the two
sides of the same round trip report failures differently.

Smallest fix: mirror the reader. Catch `IOException` with a "Could not write
persisted layout state" message and let `BentoStateException` propagate
unwrapped.

**Fixed** 2026-08-12 exactly as sketched, so the two sides of the round trip now
read the same way.

Worth recording why narrowing `catch (Exception)` to `catch (IOException)` costs
nothing, because that is the part that looks risky and is the reason a reviewer
might leave the broad catch alone. The only checked exceptions in the block are
`IOException` from `openOutputStream` and the implicit `close()`, and
`BentoStateException` from `encode`; the broad catch was therefore only *adding*
`RuntimeException`. And it was not adding safety, because
`PersistenceThreading.call` and `unwrap` already convert anything that is not a
`BentoStateException` into `BentoStateException("Persistence task failed", e)` on
both of the paths `writeLayout` runs on. The writer's broad catch was duplicating
that conversion one layer too low, where the only message available to it was the
wrong one. `LayoutStateReader` has always relied on that same outer conversion,
which is precisely why it could afford to catch only `IOException` - so mirroring
it removes an inconsistency rather than creating an exposure.

The immediate payoff is the codec's own diagnostic. `JsonLayoutCodec` and
`XmlLayoutCodec` already throw "Failed to encode BentoState as JSON"/"as XML";
the writer was catching those and re-wrapping them in the strictly less specific
"Failed to encode BentoState", so the message naming the actual format was one
cause deeper than the one the user saw first. It now surfaces directly.

The two existing tests encoded the old behaviour and both had to change, which is
the honest signal that this was a behavioural fix and not a message tidy-up:
`wrapsEncodingFailures` became `propagatesEncodingFailures` and asserts
`isSameAs` the codec's exception, mirroring `propagatesDecodingFailures` in
`LayoutStateReaderTest`; `wrapsStorageFailures` keeps its shape and now expects
the storage-flavoured message. Together they pin both halves of the finding - a
storage failure says storage, and a codec failure arrives intact. Verified to
fail first by restoring the old `catch (Exception)` and message: both fail, the
other two tests in the class pass. Verified by `:persistence:api:test`,
`:persistence:api:functionalTest`, `:persistence:codec:common:test`, the JSON and
XML codec suites, `checkJSpecify` and `javadoc`.

### <a id="m7"></a>M7. Branch state captures the recursive flattened dockable list; the restorer ignores it; the root branch has the mirror-image gap - FIXED 2026-08-12

`impl/BentoLayoutStateCaptor.java:330-332`; `impl/DockingLayoutStateRestorer.java:319-359`, `:237`, `:506-522`

`core`'s `DockContainerBranch.getDockables()` is a *recursive flattened* view —
`childContainers.stream().flatMap(c -> c.getDockables().stream())`
(`core/.../DockContainerBranch.java:431-436`). The captor feeds that straight into
`builder.addChildDockableState(...)`, so every dockable in every descendant leaf is
recorded a second time on each ancestor branch — once per level of nesting.

Meanwhile `restoreBranch` never reads `getChildDockableStates()`, and
`DockContainerBranchDto` (`persistence/codec/common/.../dto/DockContainerBranchDto.java`)
has no dockables field at all, so the codec drops it.

The root branch is the exact inverse: `buildRootBranchState:246-271` never calls
`addChildDockableState`, but `restoreRootBranchContainer:237` calls
`restoreAndAddChildDockables`, which iterates a list that is always empty.

Why it matters today: wasted capture work quadratic in tree depth, and a state
model that misrepresents the tree — `DockContainerBranchState` claims to directly
hold dockables that actually live in its descendant leaves. Restore is currently
unaffected only because two independent layers happen to ignore the field. If any
future codec starts serializing branch dockables, restore will double-add every
dockable.

Smallest fix: delete the loop at `:330-332` (leaves are the only containers that
directly own dockables, and `buildLeafState:376-386` already captures them), and
delete the now-provably-dead `restoreAndAddChildDockables` call at `:237`. If
branch-level dockables are actually intended, the captor needs a non-recursive
source and the DTO needs the field — but nothing in the current code suggests
that.

**Fixed** 2026-08-12 as both deletions, plus the method the second one orphaned:
`restoreAndAddChildDockables` was called from that one site and its parameter is a
`DockContainerRootBranchState`, so nothing else could reach it. A comment now
stands where each loop was, because both read as omissions rather than decisions -
the next person to notice that a branch state has a `getChildDockableStates()`
nobody fills will otherwise "fix" it straight back.

Checked before deleting that the branch-level capture really was inert rather than
merely unused, since inert and unused fail differently. Three independent layers
drop it: `restoreBranch` never reads the accessor, `DockContainerBranchDto` and
`DockContainerRootBranchDto` have no dockables field at all (only
`DockContainerLeafDto` does, and `BentoStateMapper:580` populates only that one),
and every other `addChildDockableState` call across `main`, `test`, `ft` and the
codec's `itp` is on a *leaf* builder. So nothing serialized it and nothing read it
back.

The root-branch call was doubly dead, which is worth recording because it makes
the deletion safe even for a hand-built state that *does* carry root child
dockables through the public `DockContainerRootBranchStateBuilder`. It ran before
`restoreChildDockContainers`, so the branch had no children yet, and
`DockContainerBranch.addDockable` (`core/.../DockContainerBranch.java:438-442`)
walks `childContainers` and returns `false` when none accepts. With no children
there is nothing to accept, so the call could not have added a dockable even with
a non-empty list. Ignored before, ignored now - no behaviour change either way.

`DockContainerBranchStateBuilder.addChildDockableState` is left in place. It is
exported API in `api.state`, removing it is source-incompatible, and the finding
did not ask for it; the accessor is also shared with the leaf state, which uses it
legitimately. Worth a look with N5, which already covers the branch/root-branch
builder duplication.

The test is one assertion rather than a new file, because
`BentoLayoutStateCaptorFT.captureBentoStatesCapturesNestedContainersAndDockables`
already builds the exact tree this finding needs - root branch, intermediate
branch, leaf holding one dockable - and already asserts the *root* branch captured
no child dockables. It never checked the intermediate branch, which is the gap the
defect lived in. It does now. Verified to fail first by reinstating the loop: it
reports `Expecting empty but was: [DockableState@...]`, the duplicate itself.
Verified by `:persistence:api:test`, `:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec suites,
`checkJSpecify` and `javadoc`.

### <a id="m8"></a>M8. Restore failures are invisible because `boolean` returns are discarded - FIXED 2026-08-12

`impl/DockingLayoutStateRestorer.java:601-613`, `:408-411`

`branch.setContainerCollapsed(leaf, isCollapsed)` returns `false` without acting
in six distinct cases — fewer than two children, leaf not first-or-last,
`getSide()` null, orientation/side mismatch, already in the target state, adjacent
sibling already collapsed (`core/.../DockContainerBranch.java:374-404`).
`leaf.selectDockable(dockable)` and `leaf.addDockable(dockable)` likewise return
`boolean`. All are ignored.

Why it matters: a layout can fail to restore its collapse state or its selected
tab for entirely legitimate structural reasons, and the module reports complete
success. There is no log line and no signal, so the only symptom is a user
noticing their layout came back wrong.

Smallest fix: capture the return and `logger.debug`/`warn` when `false`, e.g.
`if (!branch.setContainerCollapsed(leaf, isCollapsed)) logger.warn("Could not
restore collapsed state for leaf {}", leafState.getIdentifier());`

**Fixed** 2026-08-12, but **not** with the sketch above for the collapse call - that
one is wrong, and wrong in the direction that would have made the module noisier
without making it more informative. Fifth of the six `false` cases is *"the child
already has the given collapsed state"*
(`core/.../DockContainerBranch.java:387-389`). A freshly created leaf is
uncollapsed, so restoring the common `isCollapsed=false` asks for a state the leaf
is already in and gets `false` back for a leaf that restored perfectly. Logging on
the return value therefore fires once per uncollapsed leaf on every restore, which
is most leaves in most layouts, and the genuine failures drown in it.

So the collapse site compares the *achieved* state instead:
`branch.setContainerCollapsed(leaf, isCollapsed)` followed by
`if (leaf.isCollapsed() != isCollapsed)`. That reports exactly the condition worth
reporting - the leaf did not end up as persisted - and is structurally immune to
the benign case, because "already in the requested state" means the two are equal
by definition. `isCollapsed()` is the same predicate `core` itself uses in
`isContainerCollapsed`, and `setContainerCollapsed` applies the change
synchronously, so it is accurate immediately after the call.

Measured both ways rather than argued: with the sketch's `if (!ok)` the existing
suite emits six spurious warnings across three FTs, every one naming an
`*-open-*`/uncollapsed leaf that restored correctly - `DockingLayoutRestorerFT`
once, `LeafUncollapsedSizeRoundTripFT` three times, `NestedCollapseRestoreFT`
twice. With the achieved-state comparison the same suites emit none.

The other three calls take the plain `if (!...)` form, because for them `false` has
no benign reading during a restore: `addDockable` refuses only a duplicate or an
out-of-range index (`DockContainerLeaf.java:157-163`) and every dockable here is
freshly created, `selectDockable` refuses anything the leaf does not contain
(`:130-149`), and `addContainer` refuses a duplicate or a bad index
(`DockContainerBranch.java:117-123`). The select is nested inside the successful
add, since a failed add guarantees a failed select and would otherwise produce two
warnings for one fault.

`addContainer` was not named in the finding, and is fixed anyway: both call sites
(`restoreChildDockContainers` for the root branch and `restoreBranch` for nested
branches) discarded it, it is the same defect in the same file, and its `false`
is the most serious of the four - a whole subtree silently absent from the restored
layout rather than a wrong tab. Fixing only the two sites the finding listed would
have left the worst instance in place.

No new test. Asserting on log output needs a Logback `ListAppender`, and the module
has only `slf4j-simple` at `functionalTest` runtime, so this would have meant a new
test dependency to observe four log statements that change no restore behaviour.
The no-spam claim is verified instead from the existing FTs' captured `stderr`,
which is where `slf4j-simple` writes: zero occurrences of any of the four messages
across `NestedCollapseRestoreFT`, `LeafUncollapsedSizeRoundTripFT`,
`DockingLayoutRestorerFT` and `LeafResizableWithParentRoundTripFT` - suites that
between them restore both collapsed and uncollapsed leaves. Verified by
`:persistence:api:test`, `:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec suites,
`checkJSpecify` and `javadoc`.

### <a id="m9"></a>M9. A failing dockable is silently dropped from the saved layout - FIXED 2026-08-12

`impl/BentoLayoutStateCaptor.java:376-386`

The per-dockable `try`/`catch (Exception)` logs at `error` and continues the loop.

Why it matters: the save completes "successfully" and returns normally while the
persisted layout is missing a pane. The user discovers this only on the next
restore, by which point the previous good state has been overwritten —
`FileLayoutStorage.openOutputStream` uses `Files.newOutputStream` with default
options, i.e. truncate
(`persistence/storage/file/.../FileLayoutStorage.java:36-39`). Note `buildDockable`
currently only reads `getIdentifier()` so this is latent rather than active, which
is also the argument for not swallowing: the catch protects against nothing
concrete today and will hide real failures when `buildDockable` grows.

Smallest fix: let the exception propagate (the method already sits under
`saveLayout`'s `BentoStateException` contract), or accumulate failures and throw
after the loop so a partial save is never silently written.

**Fixed** 2026-08-12 by the first option: the `try`/`catch` is gone and the loop is
two lines. Accumulate-and-throw was considered and dropped - the first failure
already aborts the save before anything is written, so collecting the rest buys a
longer message in exchange for a list and a second loop, and one cause with a stack
trace is as actionable as five.

Traced all three save paths before deleting, because "let it propagate" is only safe
if propagation is caught somewhere sensible. It is, on all three: the timer path
goes through `AbstractAutoCloseableLayoutSaver.autoSave`, which catches
`BentoStateException | RuntimeException` and logs at error (B3's fix), so the
scheduler survives; `close()` routes through that same `autoSave(true)` inside a
`try` whose `finally` still runs `disableAutoSave()`, so a throwing capture cannot
leave a saver half-closed; and an explicit `saveLayout()` surfaces it to the caller
as a `BentoStateException`, since `PersistenceThreading.call`/`unwrap` converts
anything that is not one already.

Propagation is the *fix* rather than merely the tidier option because of ordering:
`DockingLayoutSaver.saveLayout` runs the entire capture on the JavaFX thread and
only then hands `writeLayout` to the off-thread executor. A capture that throws
therefore never reaches `FileLayoutStorage.openOutputStream`, the truncating
`Files.newOutputStream` never runs, and the last good file is still on disk.
Swallowing inverted precisely that - it reported success, then overwrote a good
layout with one missing a pane.

One thing this loses, worth stating rather than glossing: the old `logger.error`
named the leaf and a bare propagating exception does not. The stack trace names
`buildLeafState` and `buildDockable`, which is where a future capture step would
fail, and restoring the identifier would mean wrapping in an unchecked type only for
`PersistenceThreading` to wrap it again - two layers for one string. Propagating the
*checked* `BentoStateException` with that context is not available at all:
`captureBentoState` maps `buildRootBranchState` inside a stream, and a checked
exception cannot cross that lambda without restructuring the whole recursion.

A comment now sits where the catch was, saying why the loop is unguarded, and saying
explicitly that the narrow `getUncollapsedSize` guard M12 added a few lines above is
not a precedent for a general catch here - that one covers a single known,
legitimate state and omits an optional field, where this one dropped a whole pane.

New test in `BentoLayoutStateCaptorFT`:
`captureBentoStatesFailsRatherThanSilentlyDropAnUncapturableDockable`. It puts a
`Dockable` subclass whose `getIdentifier()` throws on demand into a leaf and asserts
`captureBentoStates()` throws instead of returning state. Two details keep it aimed
at the loop rather than something adjacent: the failure is armed only after the tree
is built and the stage shown, because core reads identifiers while wiring headers;
and a second, capturable dockable is added first and left selected, so
`buildLeafState`'s selected-identifier lookup succeeds and the throw can only come
from the dockables loop. Verified to fail first by reinstating the catch - it reports
`Expecting actual not to be null`, meaning capture returned normally with the pane
missing, which is the defect stated exactly. Verified by `:persistence:api:test`,
`:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec suites,
`checkJSpecify` and `javadoc`.

### <a id="m10"></a>M10. `isShowing` is captured but never applied - FIXED 2026-08-12

Captured at `impl/BentoLayoutStateCaptor.java:207`; grep for `isShowing`/`show()`
across `impl/DockingLayoutStateRestorer.java` returns nothing.

Why it matters: same class of defect as B7 — the state advertises a property it
does not round-trip. A `DragDropStage` that was hidden when the layout was saved
comes back and is shown, because the consumer shows every stage in
`BentoLayout.getDragDropStages()` unconditionally
(`demos/persistence/.../BoxApp.java:405-406`) with nothing telling it not to.

Smallest fix: either honour it in `restoreDragDropStage` (the stage is not shown by
this module, so the honest form is to expose it so the caller can decide), or
remove `setShowing`/`isShowing` from `DragDropStageState` and the captor.

**Fixed** 2026-08-12 by exposing it, which was a decision rather than a deduction -
there were three coherent answers and they differ in public API, so it was put to
the maintainer rather than picked quietly.

The two rejected options, for the record. *Deleting* `setShowing`/`isShowing` would
have given the smallest state model, but it is a source-incompatible removal from
exported `api.state` and drops a field from the persisted format, to solve an
incoherence that can equally be solved by keeping the promise. *Having the restorer
call `show()`* is by far the smallest diff and needs no API change, but it reverses
a contract this module states deliberately and in two places - root branches come
back unattached and stages come back unshown, both because placement and visibility
are the application's call - and it would mean the module opening windows during a
restore, which is wrong for an application restoring behind a splash screen.

Exposing it keeps that contract and closes the gap the finding actually describes,
which is that the module recorded a value the caller had no way to read:

- `BentoLayout.wasShowing(DragDropStage)` is new and additive.
- `BentoLayoutBuilder.addDragDropStage` now takes the flag alongside the stage.
  Deliberately no one-argument overload: an overload that defaulted the flag is
  exactly how the value gets dropped on the floor again. This is
  **source-incompatible** for an outside caller, same as T9 and T13, and wants the
  same release-note line - though `DockingLayoutStateRestorer` is its only caller in
  this repository.
- `demos/persistence` `BoxApp` now shows only the stages that were showing. That is
  the code the finding names as the actual symptom, so leaving it showing everything
  would have fixed nothing observable.

Two details worth keeping. The set is **identity**-based
(`Collections.newSetFromMap(new IdentityHashMap<>())`, not `Set.copyOf`): the
question is whether *this* stage was showing, `DragDropStage` overrides neither
`equals` nor `hashCode` but is public and non-final, so a subclass that overrode
them would otherwise collapse two distinct stages into one entry. And an **absent**
flag counts as showing, because the captor always records it, so absent means a
hand-built state or a layout written before this was honoured - defaulting to hidden
would silently stop restoring detached windows for exactly those layouts.

New test in `DockingLayoutStateRestorerCollaboratorFT`:
`restoreDockingLayoutCarriesThePersistedShowingFlagToTheCaller` restores three stage
states - `setShowing(false)`, `setShowing(true)`, and no flag at all - and asserts
`wasShowing` per stage, pinning the default as well as the two explicit cases. That
FT constructs `DockingLayoutStateRestorer` directly and calls the exact method the
fix changed. Verified to fail first by making `wasShowing` return `true`
unconditionally, which is the pre-fix world where the caller could only show
everything.

Fixing this broke two existing tests, which is worth recording because the breakage
was in the fixture rather than the behaviour.
`DockingLayoutBuilderTest.dockingLayoutExposesImmutableSnapshotOfBuiltLayouts` and
`builtLayoutIsNotAffectedByLaterBuilderMutation` reached `BentoLayout`'s **private
constructor by reflection** to build an empty fixture, so adding a field failed them
at `getDeclaredConstructor` with a `NoSuchMethodException` - at runtime, not at
compile time, which is why the first full run looked clean until the output was read
properly. Neither test asserts anything about the constructor; both are about
`DockingLayout`. They now build the fixture through the public
`BentoLayoutBuilder`, which is what they should have done and which makes them
immune to the next field. Verified by `:persistence:api:test`,
`:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec suites,
`:demos:persistence:compileJava`, `checkJSpecify` and `javadoc`.

### <a id="m11"></a>M11. Capture depends on scene attachment, so saving before display loses the layout - FIXED 2026-08-12

`impl/BentoLayoutStateCaptor.java:117-120`

`captureBentoStates` sources root branches from `bento.getRootContainers()`. In
`core`, a `DockContainerRootBranch` registers itself with its `Bento` only from a
`sceneProperty()` listener (`core/.../DockContainerRootBranch.java:33-39`).

Why it matters: root branches produced by this module's own restorer are *not*
attached to a scene — `restoreDockingLayout` hands them back in a `BentoLayout`
for the application to place. Between restore and the application attaching them,
`bento.getRootContainers()` is empty, so an auto-save firing in that window
persists an empty layout over a good one (again, truncating). Nothing in the
`LayoutSaver` or `LayoutRestorer` Javadoc mentions this coupling.

Smallest fix: document it on `LayoutSaver.saveLayout()` and on
`LayoutRestorer.restoreLayout` ("returned containers must be attached to a scene
before the next save"), and consider having `saveLayout()` skip the write when
every `Bento` reports zero root containers rather than persisting an empty layout.

**Fixed** 2026-08-12 with both halves, and the "consider" half is the one that
matters - documentation warns, it does not prevent. The hazard survives being
documented because an application cannot reliably avoid the window: restoring a
layout *itself* fires `DockEvent`s on the Bento's bus (`DockableAdded`,
`ContainerChildAdded`), the saver is registered as a listener, so restore sets
`wasDockEventReceived` and the very next timer tick has both a reason to save and
nothing attached to find. That is not a rare interleaving, it is the normal
sequence.

`DockingLayoutSaver.saveLayout` therefore skips the write when the capture found no
root branches and no drag/drop stages anywhere, and logs at debug. `allMatch` over an
empty list is `true`, which is the wanted answer for a provider reporting no Bentos
at all. The case this could plausibly get wrong - an application that legitimately
closed everything - is covered by the observation that a running application showing
anything has at least one attached root branch, so "nothing anywhere" means
not-ready or shutting down; and the costs are asymmetric anyway, since a stale
layout is recoverable and an erased one is not.

Documentation went on `LayoutSaver.saveLayout()` and `LayoutRestorer.restoreLayout`
as prescribed. Worth noting that `BentoLayout.getRootBranches()` already carried this
explanation, so the coupling was documented where the containers are handed over but
on neither interface an application actually calls - which is the gap the finding
names.

**Two existing tests asserted the behaviour this changes**, which is the part worth
reviewing rather than taking on trust. `saveLayoutStillWritesWhenNoBentosExist`
required an empty capture to be written - that *is* the defect, written down as an
expectation - so it is renamed `saveLayoutDoesNotWriteWhenNoBentosExist` with the
storage assertion inverted and a comment recording that it was inverted and why. It
dates from the original bulk import (`724baae`), so it characterised existing
behaviour rather than recording a decision. Separately,
`saveLayoutEncodesAndWritesAwayFromFxThreadWhenCalledOnFxThread` broke for a reason
unrelated to its purpose: it is about *which thread* encodes and writes, but its
fixture built a root branch and never gave it a `Scene`, so under the guard there was
no write left to observe. It now attaches a `Scene` before saving, which its own
subject matter needs anyway.

New test `saveLayoutKeepsThePersistedLayoutWhenNothingIsAttached` covers the M11 case
proper rather than the no-Bentos edge: a `Bento` exists, its root branch was never
attached, a good layout is already in storage, and that layout is still
byte-for-byte intact after the save. Both guard tests were verified to fail with the
guard disabled. Verified by `:persistence:api:test`,
`:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec suites,
`:demos:persistence:compileJava`, `checkJSpecify` and `javadoc`.

### <a id="m12"></a>M12. Capturing a leaf with no side throws, aborting the whole save — FIXED 2026-08-11

`impl/BentoLayoutStateCaptor.java:370` (found while fixing B7, not in the original
review)

```java
leafStateBuilder.setUncollapsedSizePx(leaf.getUncollapsedSize());
```

`core`'s `getUncollapsedSize()` is a `switch` over `getSide()` whose `case null`
arm throws `IllegalStateException("Container with null side should not be
collapsed")` (`core/.../DockContainerLeaf.java:397-403`). `getCollapsedSize()` has
the same shape. But a leaf's side is legitimately nullable — `setSide(@Nullable
Side)` is public and documented as "`null` to not display any headers" — and the
captor calls this unconditionally, whether or not the leaf is collapsed.

Why it matters: one headerless leaf anywhere in the tree throws from
`buildLeafState`, which is outside the per-dockable `try` at `:376-386`, so it
propagates out of `captureBentoStates` and aborts the entire save. Not just that
leaf — every Bento. The message is also actively misleading: the container need not
be collapsed for this to fire.

This is latent rather than active in the demo, which always sets a side, which is
presumably why it has not bitten yet. Note the B7 fix does not make it worse or
better: the call was already unconditional. But B7's restore path now depends on
this value, so it is worth closing before something relies on it.

Smallest fix: only capture the size when it is meaningful —
`if (leaf.getSide() != null) leafStateBuilder.setUncollapsedSizePx(leaf.getUncollapsedSize());`
— which is also consistent with `DockContainerLeafState` treating the field as
optional. Guarding on `leaf.isCollapsed()` would be wrong: an uncollapsed leaf's
size is exactly what we want recorded for a later collapse.

**Fixed** exactly as described, with a comment recording why the guard is on the
side rather than on `isCollapsed()`.

Confirmed reachable before fixing, rather than taken on reading. A probe over a live
tree containing one leaf with `setSide(null)`:

```
side=null  isCollapsedOk=false
getUncollapsedSize=THREW:IllegalStateException(Container with null side should not be collapsed)
capture=THREW:IllegalStateException(Container with null side should not be collapsed)
```

So the throw does escape `captureBentoStates`, and `isCollapsed()` is safe by
contrast — it reads a pseudo-class rather than switching on the side, which is why
guarding on it would not have helped anyway.

The restorer needed no change. `restoreUncollapsedSize` early-returns unless the leaf
is collapsed, core cannot collapse a null-side leaf, and the B7 `setUncollapsedSize`
setter does not switch on the side. Checked rather than assumed, since B7's restore
path is the reason this mattered.

Regression pinned by `ft/.../HeaderlessLeafCaptureFT.java`, three tests:

- `captureSucceedsWithHeaderlessLeaf` — the defect itself.
- `headerlessLeafIsStillCapturedWithoutItsUncollapsedSize` — the leaf is skipped for
  one property, not dropped from the layout; side and size both come back empty and
  its dockable is still there.
- `sidedLeafStillCapturesItsUncollapsedSize` — its sided sibling still records a
  size. Without this control the fix could have been "never capture the uncollapsed
  size", which would silently undo B7.

All three fail with the guard removed, the third being what pins the fix as
narrow rather than blanket.

---

## MINOR

#### <a id="n1"></a>N1. `StageUtils.getAllScreenBounds` uses `Double.MIN_VALUE` for maxima - FIXED 2026-08-12

`impl/StageUtils.java:99-100`. `Double.MIN_VALUE` is the smallest *positive*
double (≈4.9e-324), not the most negative. It works only because real screens
have positive `maxX`/`maxY`. Also returns a nonsense rectangle if
`Screen.getScreens()` is empty. Fix: `Double.NEGATIVE_INFINITY`.

**Fixed** 2026-08-12. The maxima are `Double.NEGATIVE_INFINITY` as prescribed, and
the minima moved to `Double.POSITIVE_INFINITY` as well - `MAX_VALUE` does work as a
starting minimum, but leaving it next to corrected maxima reads as if the pair were
symmetric when it is not.

Two corrections to the finding, both found by checking rather than assuming. First,
the concrete case where the old sentinel bites is narrower and more real than "only
because real screens have positive maxima": `Math.max(bounds.getMaxX(), MIN_VALUE)`
loses only when a screen's `maxX` is *negative*, which happens when a monitor sits
entirely left of, or above, the primary origin - an ordinary multi-monitor
arrangement. Such a screen was clipped to ~0 rather than ignored.

Second, and this is why the prescribed fix was not sufficient on its own: an empty
`Screen.getScreens()` does not return a nonsense rectangle, it **throws**.
`Rectangle2D` rejects negative dimensions - verified with a throwaway probe, which
reported `IllegalArgumentException: Both width and height must be >= 0` - and the
width in that case is `MIN_VALUE - MAX_VALUE`, i.e. about `-1.8e308`. Switching to
`NEGATIVE_INFINITY` keeps it throwing, just with a width of `-Infinity`. So the
sentinel change alone would have left the empty case broken while looking fixed.
`getAllScreenBounds` is now `@Nullable` and returns `null` for no screens, with both
helpers passing the position through unbounded - if there is nothing to bound
against, the honest answer is to leave the coordinate alone. Worth noting the
consequence that made this worth guarding rather than leaving: the throw surfaces
inside `restoreDragDropStage`, which `DockingLayoutRestorer.restoreLayout` catches
as a `BentoStateException` and answers by substituting the **default layout**, so
the visible symptom would have been a silently discarded layout, not a crash.

An empty screen list is unreachable while the JavaFX toolkit is up, so this guard is
deliberately untested - there is no way to empty `Screen.getScreens()` from a test
without mocking a static. The clamping path around it is tested; see N2.

#### <a id="n2"></a>N2. `StageUtils` position helpers take boxed `Double`, NPE on null - FIXED 2026-08-12

`impl/StageUtils.java:47-50, 72-75`.
Both auto-unbox immediately (`double boundedX = stageX;`), so a null argument is
an NPE from a `public` method in an exported package. Fix: primitive `double`.

**Fixed** 2026-08-12: both parameters are `double`. No call site changed - the only
two are in `restoreDragDropStage`, inside `Optional<Double>.ifPresent`, so the
`Double` they hold is already non-null and simply auto-unboxes now.

The "exported package" half of the reasoning is stale, in a way that reduces this to
tidiness: M1 unexported both `impl` packages, so these are public methods on an
internal class rather than API anyone outside can reach. The change is still right -
the module is `@NullMarked`, so the parameter was *already* declared non-null and the
boxed type only made that unenforceable. It is technically source-incompatible for a
caller passing a literal `null`, which now fails to compile instead of throwing at
runtime; that is the improvement rather than a regression, and it needs no
release-note line while `impl` stays unexported.

The genuinely useful part of doing N1 and N2 together: the position helpers had **no
test at all**, and N1 restructured the method they both depend on.
`StageUtilsFT.positionHelpersClampToTheBoundaryEnclosingEveryScreen` now pins the
contract on both axes - a position already on screen comes back untouched, one off
either edge is pulled back to the boundary, with expectations derived from `Screen`
so it holds on any monitor arrangement. Verified to fail by inverting the new
emptiness guard so it returns `null` while screens exist, which switches clamping off
entirely. It deliberately does **not** attempt to catch the `MIN_VALUE` sentinel:
with any screen present `Math.max` beats that sentinel on the first iteration, which
is exactly why the defect stayed latent, and a test would need a monitor placed at
negative coordinates. Verified by `:persistence:api:test`,
`:persistence:api:functionalTest`,
`:persistence:codec:common:integrationTestParallel`, both codec suites,
`:demos:persistence:compileJava`, `checkJSpecify` and `javadoc`.

#### <a id="n3"></a>N3. `BentoStateBuilder` omits the null check its siblings have - FIXED 2026-08-13

`api/state/BentoState.java:53`. Bare assignment where
`DockContainerLeafStateBuilder:97`, `DockContainerBranchStateBuilder:70`,
`DockableStateBuilder:152`, and `DragDropStageStateBuilder:224` all use
`requireNonNull`. Failure surfaces later, at `build()`. Fix: `requireNonNull(identifier)`.

**Fixed** 2026-08-13 exactly as stated; the file already static-imports
`requireNonNull`, so it is one word. No test, deliberately: none of the four sibling
builders has one either, and a test here would restate a one-line constructor check
on a value the module is already `@NullMarked` for rather than exercise anything.

#### <a id="n4"></a>N4. `DockContainerStateBuilder.setPruneWhenEmpty` takes primitive `boolean`

~~`api/state/DockContainerState.java:69` — while all four subclass builders take
`@Nullable Boolean` for the same tri-state property. Inconsistent, and the
primitive form cannot express "unspecified".~~ **Moot** 2026-08-12: M4 deleted the
builder, and with it this setter. The four subclass builders were already
consistent with each other.

#### <a id="n5"></a>N5. `DockContainerRootBranchStateBuilder` duplicates the branch builder - FIXED 2026-08-13

`api/state/DockContainerRootBranchState.java:40-87`
re-declares `identifier`, `childDockableStates`, `pruneWhenEmpty`, `orientation`,
`dividerPositions`, `childDockContainerStates` and all six mutators. They have
already drifted: the parent's mutators carry Javadoc, the root's carry none. Fix:
have the root builder delegate to or extend the branch builder.

**Fixed** 2026-08-13 by delegation. The six fields and their null checks are gone;
the builder now holds a `DockContainerBranchStateBuilder` and forwards to it, and
`build()` builds the branch state and copies it across through its accessors rather
than reaching into the delegate's fields.

Delegation rather than the other option the finding offers, because *extending*
`DockContainerBranchStateBuilder` breaks chaining: every inherited mutator returns
the parent type, so `new DockContainerRootBranchStateBuilder(id).setOrientation(V)
.build()` would yield a `DockContainerBranchState`, and `BentoStateTest` chains
exactly like that. Fixing that needs either covariant overrides of all six mutators -
the duplication back again, wearing `@Override` - or a recursive self-type generic on
an exported builder, which is a steep price for two subclasses. Delegation also
avoids leaving a wrongly-typed `build()` reachable on the subclass. The mutators stay
as one-line forwards for the same chaining reason.

The drift the finding names has already been repaired separately: the root builder's
mutators carry Javadoc now, from N9's sweep. So this was structural duplication only,
and the argument for removing it is future drift rather than present damage.

New test `DockContainerRootBranchStateBuilderTest`, which did not exist - and that
absence is how the duplicate could drift unnoticed in the first place, since the
branch builder had `DockContainerBranchStateBuilderTest` and the root builder had
nothing. It mirrors that test: every field survives the hand-off, the returned
collections are immutable, and a build is unaffected by later builder mutation (worth
pinning, because `build()` now goes through an intermediate state). Verified to fail
by dropping `orientation` in the hand-off.

#### <a id="n6"></a>N6. `restoreDockable` discards the resolved state's own identifier - FIXED 2026-08-13

`impl/DockingLayoutStateRestorer.java:436-451`. It builds the `Dockable` from the
`dockableIdentifier` parameter and never compares it to
`dockableState.getIdentifier()`. A provider returning a mismatched state is
silently accepted. Fix: log a warning on mismatch.

**Fixed** 2026-08-13 as stated: a `logger.warn` naming both identifiers when they
differ. Reporting rather than switching to the resolved state's identifier, because
the persisted layout asked for this identifier and honouring it keeps the restored
tree matching what was saved - substituting would quietly rename a pane instead.

No test, for the reason recorded under M8: asserting on log output needs a Logback
`ListAppender` and this module has only `slf4j-simple` at `functionalTest` runtime, so
it would mean a new test dependency to observe one log statement that changes no
restore behaviour.

#### <a id="n7"></a>N7. `DragDropStageState.isAutoClosedWhenEmpty()` Javadoc contradicts signature - FIXED 2026-08-13

`api/state/DragDropStageState.java:186-192`. The doc describes "an
`Optional` ... an empty `Optional` when unspecified"; the method returns a
non-null `Boolean` that is `requireNonNull`-checked at `:69`. Copy-paste from the
surrounding accessors.

**Fixed** 2026-08-13: the Javadoc now says what the method does and why it is the odd
one out - it is the only mandatory field, taken as the builder's constructor argument
rather than one of its setters, so it has no "unspecified" case to describe.

Deliberately *not* changing `Boolean` to `boolean`, though that is arguably the more
honest signature and is what N2 concluded for a parameter in the same position. The
finding names this a Javadoc contradiction and fixing the doc resolves it; changing
the return type of an exported accessor is source-incompatible for anyone calling a
`Boolean` method on the result, which is a separate decision with its own release-note
cost. Worth doing if the exported surface gets another pass - the boxed type is what
invited the wrong doc.

#### <a id="n8"></a>N8. `ServiceLoader` cached once at construction, via the TCCL - FIXED 2026-08-13

`impl/provider/DefaultDockingLayoutPersistenceProvider.java:123-128`.
Providers registered after the first construction are never seen, and the
no-arg `ServiceLoader.load` form uses the TCCL, which finds nothing in some
container and JPMS layouts — surfacing as the "No LayoutCodecProvider
implementation was found" message at `:136-139` rather than as a loader problem.
Fix: pass an explicit `ClassLoader`, or document the eager one-shot behaviour.

**Fixed** 2026-08-13, all three parts, though one of them turned out to be already
done.

The loader is now explicit: `ServiceLoader.load(providerType,
providerType.getClassLoader())`. The service interface's own loader is the
deterministic choice because it is the module graph declaring the `uses` clause that
is meant to resolve the `provides`, and it removes the dependence on which thread
happened to construct the provider.

Worth being straight about the trade-off, since the TCCL is not simply a mistake: it
is what lets a container expose plugin-loader services to library code. An
implementation living in a loader this module cannot see will no longer be discovered.
That case is covered - it has to be, and already was - by the two-argument constructor
that takes the providers directly, which is now cross-referenced from the no-arg
constructor's Javadoc as the escape hatch.

The misleading message is addressed too, which is the part the finding actually
describes as the symptom: "No X implementation was found" now continues with the
possibility that one is present but in an invisible loader, and points at the explicit
constructor. Phrased to be true regardless of how the instance was built, so it needs
no flag recording whether discovery ran.

The eager one-shot half needed nothing: the no-arg constructor's Javadoc already said
"discovered once, now ... A provider registered after this runs will not be seen",
from N9's sweep. It has gained the loader note beside it.

No test. Exercising the loader change means constructing a class loader that can and
cannot see a provider, which is a JPMS test harness rather than a MINOR fix, and the
existing `failsWhenNoCodecProvidersAreAvailable` already pins the message with
`hasMessageContaining`, so the appended sentence cannot break it.

#### <a id="n9"></a>N9. Missing Javadoc across the public API (enumerated in the section)

~~No doc comments on: all six
`DockContainerLeafState` accessors (`:58-80`) and all nine of its builder methods
(`:100-149`); `DockContainerBranchState.getDividerPositions()`/`getChildDockContainerStates()`
(`:48-54`); `BentoState.getRootBranchStates()`/`getDragDropStageStates()` (`:30-36`);
`IdentifiableState.getIdentifier()` (`:20`); `BentoLayout.getRootBranches()`/`getDragDropStages()`
(`:37-43`) and its whole builder (`:45-78`); `DockingLayout.getBentoLayouts()` (`:22`)
and builder (`:27-42`); `DockingLayout`/`BentoLayout` `build()` methods;
`LayoutSaver.saveLayout()` (`:11`); both `BentoStateException` constructors (`:11-20`);
`DockingLayoutPersistenceProvider.getLayoutSaver` — both overloads (`:17-30`);
`DragDropStageStateBuilder`'s constructor and `build()` (`:221-226`, `:407`).~~
**Fixed** 2026-08-11, and the enumeration above turned out to be both too long and
too short.

Too long by one: `LayoutSaver.saveLayout()` had already been documented by the B4
fix, which added the thread-ordering contract to it.

Too short by rather more, which is the part worth recording. Rather than work the
list, I turned `-missing` on temporarily and let the tool enumerate — and it found
members the hand-written list had missed: every builder *class* declaration
(`BentoStateBuilder`, `DockableStateBuilder`, both `DockContainer*StateBuilder`s,
`DockContainerStateBuilder`), the whole of `DockContainerRootBranchStateBuilder`
(all six members, the N5 duplicate the list overlooked entirely), the `protected`
constructors of `IdentifiableState`, `DockContainerState` and
`DockContainerBranchState`, `DockContainerStateBuilder`'s three `protected` fields,
and `DefaultBentoProvider`/`DefaultDockingLayoutPersistenceProvider`'s public
constructors plus `addBento`. `BentoStateTimeoutException`'s two constructors were
missing too — that class did not exist when the review was written.

So the finding under-counted by roughly two to one. The lesson matches N10's: a
gap that only a tool can enumerate reliably should be enumerated by the tool, and
the reason nobody had is that the build was configured not to look. The sweep is
now verified the same way it was scoped — `-missing` reports **zero** undocumented
public or protected members in this module.

Two judgement calls inside the sweep. `DockContainerStateBuilder` got documented
even though M4 argues for deleting it, because three lines now is cheaper than
leaving the one hole a future `-missing` run would flag; if M4 lands, the docs go
with the class. (M4 landed 2026-08-12, and they did.) `DefaultBentoProvider.addBento` records that a duplicate identifier
silently replaces the existing entry — that is the B1 follow-up, so the behaviour is
now at least written down where a caller will see it.

#### <a id="n10"></a>N10. Unterminated `{@link}` will break Javadoc generation

~~`impl/DockingLayoutStateRestorer.java:282-283`: `{@link DockContainerBranchState`
has no closing brace, so the following `or {@link DockContainerLeafState}` is
swallowed into the tag. Under doclint this is an error, not a warning. (The class
is package-private, so whether it fails your build depends on the Javadoc task's
visibility setting.)~~ **Fixed** 2026-08-11: brace closed, plus the blank `*` line
before `@param` that every sibling comment in the file has.

One correction to the finding while closing it. It would **not** have failed this
build under any visibility setting, because
`build-logic/.../bento.project.project-convention.gradle:89-91` passes
`-Xdoclint:none` to every `javadoc` task, so the whole project generates docs with
linting off. `:persistence:api:javadoc` therefore passed before this fix as well as
after, and the passing task is not evidence the fix works — the evidence is that the
braces now balance. The hazard is real for anyone who generates docs without that
flag, but "will break Javadoc generation" overstated it for this repo.

**The build now checks this, which is the actual fix.** A one-character brace repair
leaves the next one free to happen, so `persistence/api/build.gradle` gained two
things: `Xdoclint:all,-missing` on the published `javadoc` task, and a separate
`javadocLint` task wired into `check`. Two, not one, because doclint on the
published task reads public and protected members only — and this finding was on a
**package-private** class, so that task could not have caught it. Verified rather
than assumed: re-breaking the brace left `javadoc` passing and failed `javadocLint`
with `error: unterminated inline tag`. `javadocLint` runs at private visibility and
discards its output; it exists to fail, not to publish.

Scoped to this module rather than the shared convention on purpose. Enabling doclint
in `bento.project.project-convention` fails `:core:javadoc` with eight pre-existing
`<p/>` self-closing-element errors (`BentoUtils` ×3, `DockableDragDropBehavior` ×2,
`DragDropStage` ×2, `DockContainerLeaf` ×1), and `core/` is not this module's to
change. Each is a one-character fix — `<p/>` to `<p>` — so promoting the check
repo-wide is cheap whenever the `core/` owner wants it, and the comment in
`build.gradle` says so.

---

## NIT

#### <a id="t1"></a>T1. Duplicated scene-root `instanceof` check in the captor

~~`impl/BentoLayoutStateCaptor.java:135-153` and `:225-235` — `toDragDropStageRoot`
and `getDockContainerRootBranch` do the identical scene-root `instanceof` check,
and `captureBentoState` calls the first then `buildAndAddDragDropStage` calls the
second on the same stage. One of the two is redundant.~~ **Resolved** with B9:
`toDragDropStageRoot` now delegates to `getDockContainerRootBranch`, so the check
and its new null-scene guard exist in one place.

#### <a id="t2"></a>T2. Double-logged "Attempting to restore null DockContainer"

~~`impl/DockingLayoutStateRestorer.java:343-350` — logs "Attempting to restore null
DockContainer" for a case `restoreDockContainer:299-305` has already logged.
Double-reported, and the message describes an attempt rather than the skip that
actually happens.~~ **Fixed** 2026-08-11: the `else` branch is gone. The only path
that yields `null` is the `default ->` arm, which already logs the unrecognised
type, so `restoreBranch` now skips silently exactly as its sibling caller
`restoreChildDockContainers` always did — with a comment saying why, so nobody
reads the bare `if` as a missing diagnostic.

#### <a id="t3"></a>T3. "{@return 2n" typo in `DockContainerBranchState`

~~`api/state/DockContainerBranchState.java:40` — "{@return 2n {@link Optional}"
should be "an".~~ **Fixed** 2026-08-11.

#### <a id="t4"></a>T4. "caontaining" typo in `DockContainerLeafMenuFactoryProvider`

~~`api/provider/DockContainerLeafMenuFactoryProvider.java:17` — "caontaining".~~
**Fixed** 2026-08-11.

#### <a id="t5"></a>T5. Doubled comma in `DockableState` Javadoc

~~`api/state/DockableState.java:125` — "docking layout, , an empty".~~
**Fixed** 2026-08-11.

#### <a id="t6"></a>T6. Stray `*` in a `BentoState` `@param`

~~`api/state/BentoState.java:71` — stray `*` inside the `@param` text.~~
**Fixed** 2026-08-11.

#### <a id="t7"></a>T7. "the diver positions" typo in the restorer

~~`impl/DockingLayoutStateRestorer.java:528` — "the diver positions" → "divider".~~
**Fixed** 2026-08-11.

#### <a id="t8"></a>T8. `{code X}` missing its `@` in two files

~~`impl/DockingLayoutRestorer.java:40` and `api/BentoLayout.java:12` — `{code X}`
missing the `@` (`{@code X}`), so it renders as literal text.~~
**Fixed** 2026-08-11, both files.

#### <a id="t9"></a>T9. `enableAutoSave` takes a boxed `Long`; stray double space in `@see`

~~`impl/AbstractAutoCloseableLayoutSaver.java:101-103` — `enableAutoSave(Long, TimeUnit)`
takes a boxed `Long` in a public signature, then `requireNonNull`s it; `long`
would make the contract clearer. Its `@see` at `:136` has a stray double space.~~
**Fixed** 2026-08-11: the parameter is `long`, the `requireNonNull` and the local
it fed are gone, and the five `{@link #enableAutoSave(Long, TimeUnit)}` references
plus the `@see` were updated to match — a Javadoc reference naming the old boxed
signature would no longer resolve. Every existing call site already passed a `1L`
literal, so nothing else changed. Worth noting this is a **source-incompatible
change** for any caller holding a `Long`, which is only reachable at all because
`impl` is exported (M1).

#### <a id="t10"></a>T10. Constructor passes its own fields back into `enableAutoSave`

~~`impl/AbstractAutoCloseableLayoutSaver.java:77` — the constructor passes the
fields `autoSaveInterval`/`autoSaveTimeUnit` into a method that assigns them back
to themselves.~~ **Resolved** with B2: the constructor no longer calls
`enableAutoSave`, and the two fields it fed are gone entirely — the interval now
lives only in the caller's argument and the scheduled task.

#### <a id="t11"></a>T11. Inconsistent private-constructor convention in the two utility classes

~~`impl/StageUtils.java:17-19` throws from its private constructor while
`impl/PersistenceThreading.java:22-24` just comments — pick one convention.~~
**Fixed** 2026-08-11 in favour of throwing, because that is what the rest of the
repo already does — six of the seven private utility constructors across
`persistence/` throw `IllegalStateException("Utility class")`, so
`PersistenceThreading` was the lone outlier and it moved rather than the six.

#### <a id="t12"></a>T12. `java.util.concurrent.Future` written fully qualified

~~`impl/PersistenceThreading.java:84` — `java.util.concurrent.Future` written
fully-qualified in the signature although `java.util.concurrent.*` types are
imported individually above; import `Future`.~~ **Resolved** with B4, which
rewrote that file's imports.

#### <a id="t13"></a>T13. Varargs `requireNonNull` checks the array, not its elements

~~`api/state/BentoState.java:60, 73` — `addRootBranchState`/`addDragDropStageState`
are singular verbs taking varargs; `requireNonNull` there checks the array, not
its elements (`List.of` catches null elements, so this is cosmetic).~~
**Fixed** 2026-08-11 by dropping the varargs rather than renaming the methods, which
closes both halves at once: the singular verb becomes accurate, and
`requireNonNull` now guards the element it appears to guard.

Dropping varargs rather than pluralising the names because singular-and-single-arg is
what the rest of the module already does - `addChildDockableState`,
`addDockContainerState`, `addRootBranch`, `addDragDropStage` and `addBentoLayout` are
all one-argument adders, so these two were the outliers in both respects. Renaming
them to `addRootBranchStates`/`addDragDropStageStates` would have fixed the grammar
and left them the only varargs adders in the module.

No call site changed. All eighteen across `main`, `test`, `ft` and the codec's `itp`
pass exactly one argument, including
`BentoLayoutStateCaptor:123`'s `forEach(stateBuilder::addRootBranchState)` - that
method reference binds to a one-argument `Consumer` either way. Checked before
editing rather than after, since a caller passing two would have made this a
genuinely breaking change instead of a signature tidy-up. It is still
source-incompatible in principle for an outside caller passing two or more, which is
worth a release-note line for the same reason T9 is.

#### <a id="t14"></a>T14. `DockEventListener` implemented publicly, exposing `onDockEvent` - FIXED 2026-08-13

`impl/AbstractAutoCloseableLayoutSaver.java:31` — implementing
`DockEventListener` publicly puts `onDockEvent` on the saver's public surface.

To be clear about scope, since `DockEventListener` is a `core` type and `core` is
deliberately almost untouched by this branch: **fixing this needs no `core`
change.** `EventBus.addEventListener`/`removeEventListener`
(`core/.../EventBus.java:92, 100`) take any `DockEventListener`, so the saver can
hold one in a private final field and register that instead of `this` - the
interface stays exactly as `core` declares it and only the saver's own shape
changes. The one trap is identity: `removeEventListener` is a `List.remove`, so
the listener has to be a stored field, not a fresh lambda per call, or
`removeListeners()` silently stops unregistering and leaks the saver into every
`Bento`'s bus.

Two things have also shrunk it since it was written. M1 unexported both `impl`
packages, so `onDockEvent` is no longer on any *externally* reachable surface -
it is a public method on an internal class, which is a tidiness question rather
than an API one. And the interface has to be public wherever it is implemented,
so this is only ever fixable by composition, never by narrowing the method.
**Fixed** 2026-08-13 by composition, with `core` untouched. The status table briefly
carried this as won't-do "issue is in the core module"; the defect is in how
`persistence` *uses* a `core` type rather than in `core` itself, which is what
reopened it.

`AbstractAutoCloseableLayoutSaver` no longer implements `DockEventListener`. It holds
one in a private final field - `this::markLayoutDirty` - and registers that on each
`Bento`'s bus, so `core`'s interface is unchanged and only the saver's own shape
moved. `onDockEvent` became package-private `markLayoutDirty`, which also names what
it does: set the dirty flag the next auto-save reads.

The identity trap is real and now carries a comment saying so. Because
`EventBus.removeEventListener` is a `List.remove`, add and remove must be handed the
same instance; inlining `this::markLayoutDirty` at the two call sites creates two
distinct lambdas, removal silently does nothing, and the saver leaks into every
`Bento`'s bus past `close()`. This is the one respect in which the composition version
is *more* fragile than what it replaced, where `this` was inherently stable. It is
covered: `concurrentEnableAndDisableLeavesNoListenerRegistered` was verified to fail
when the lambda is created per call, which is exactly the mistake a later edit would
make.

Three test sites used the method, which is why this was never purely an internal
rename. `LayoutSaverConstructionFT:181` and `LayoutSaverAutoSaveLifecycleFT:222` both
call it directly to mark the layout dirty without a live listener - the first cannot
fire a real event instead, since its premise is that auto-save is off and nothing is
listening. `EventCountingSaver` overrides it to count events reaching the saver, and
that has to stay an override rather than become a second bus listener: the test
distinguishes events reaching the *saver* from events reaching the *bus*, which is the
whole point of it. All three needed only the rename, because the FTs share the
package - package-private is enough, and no `protected` extension point had to be
introduced to keep them working.

No new test. The method is one flag write with no branch, and the behaviour that could
actually break - the registration lifecycle - was already covered by the listener test
above, which is what verified the trap.

#### <a id="t15"></a>T15. `DockingLayoutRestorer.layoutStorage` aliases the reader's instance

~~`impl/DockingLayoutRestorer.java:35` — the `layoutStorage` field duplicates the
instance already held by `layoutStateReader`; it exists only for
`doesLayoutExist()`, which reads fine but means two fields alias one object with
only one of them owning `close()`.~~ **Resolved** with M5, which decided the
ownership question this was waiting on: the field is gone and `doesLayoutExist()`
now goes through `LayoutStateReader.layoutExists()`.

#### <a id="t16"></a>T16. Tab/space inconsistency in `module-info.java`

~~`module-info.java:27` — indented with a tab while lines `:28-33` use spaces.~~
**Fixed** 2026-08-11.

---

## Summary

Two themes account for most of the serious findings:

1. **Capture/restore asymmetry.** Six properties are captured and then dropped, or
   restored from something never captured: leaf resize state (B6, now fixed),
   uncollapsed size (B7, now fixed), nested collapse state (B5, now fixed),
   `isShowing` (M10), branch-level dockables (M7), root-branch dockables (M7). A
   single round-trip FT that captures a deliberately non-default layout, saves,
   restores, and asserts field-by-field equality would have caught all six — the
   existing FTs assert individual properties against hand-built state, which cannot
   detect a property that neither side handles.

   Fixing B5, B6 and B7 does not retire this theme. All three tests were written
   against a known defect, so they prove three specific properties round-trip; M7's
   two halves and M10 are each still one property nobody thought to list.
   `LeafResizableWithParentRoundTripFT` is the closest thing to the general test and
   shows the shape it should take — capture from a live tree, restore, compare
   against the original, never hand-write the expected value. Generalising it to
   walk every field of every state type is still the highest-value work left in this
   module. B6 is the cautionary case: the captor already had a live-tree FT, and the
   bug survived purely because `isResizableWithParent` was not among the properties
   it asserted.

   B7 also showed the theme has a second edge. Two of these properties needed more
   than a listing to fix: B7's ordering constraint (the value must be applied after
   the collapse, or binding discards it) means a field-by-field round-trip test can
   pass while the code is still wrong about *when*. A general test should exercise
   state transitions, not just final values.

   **Written 2026-08-13 as `LayoutRoundTripFT`, and it paid for itself immediately.**
   It captures a deliberately non-default tree - nested branches, a collapsed leaf
   with an uncollapsed size, sides, `canSplit`, `pruneWhenEmpty`,
   `resizableWithParent`, a non-first selected tab, real divider positions - restores
   it, re-attaches it, captures again, and compares the two states with
   `usingRecursiveComparison`. Reflective rather than hand-written on purpose: a new
   state field is then compared automatically, where a hand-written comparison would
   skip it and reproduce this exact theme. Confirmed to have teeth by dropping
   `canSplit` from the restorer, which it caught by path even though nothing in it
   mentions `canSplit` explicitly.

   On its first run it found a live defect this review had already dismissed - see
   B10, reopened. That is the theme's own argument landing: the withdrawal there
   rested on "no FT was added, the behaviour already works", and a general round-trip
   test is exactly what separates "works" from "works three times in four".

   Two properties stay out of its scope, both documented on the test. A
   `DragDropStage`'s geometry belongs to the window manager rather than the layout, so
   it does not round-trip exactly; and `isShowing` cannot be tested this way at all,
   because the module never shows a restored stage and an unshown stage is not
   captured - M10's own test covers that one.

2. **Concurrency in the auto-save path — now closed.** The saver started a thread
   from its constructor (B2), shared unguarded mutable state with it (B8), could
   hang on it (B4), silently lost it to any unchecked exception (B3), and reached it
   through a non-thread-safe provider (B1). These compounded: B9's NPE was delivered
   to B3's too-narrow catch, which killed the scheduler B2 started. All five are
   fixed, so that chain no longer exists.

   What this theme cost, and the lesson worth keeping: **not one of these five was
   fixable by the sketch in its own finding.** B4 needed cancel-on-timeout and a
   distinct exception type, or a spurious timeout would have truncated the saved
   file. B2 could not be fixed without also fixing M2, or a directly constructed
   saver would silently stop flushing. B8's "guard the `close()` body" would have
   deadlocked against the JavaFX thread. Each sketch was directionally right and
   locally wrong — which is what makes a concurrency defect expensive even when the
   diagnosis is correct, and why every one of these landed with a test that was
   verified to fail first.

   B9 removed the last input to that chain — the NPE source B3's widened catch had
   been reduced to merely logging. With it fixed, no known path now aborts a save,
   silently disables auto-save, hangs a caller, or corrupts the provider. **M12 is
   the one loose end**: it is the same shape of defect as B9 in the same file (an
   unguarded `core` call that throws from outside any guard), and it is still live.

## Suggested order for the remainder

The blocker-grade work is done, and so is the one decision that gated the rest.

M1 was the one to decide rather than defer: dropping the `impl` exports got harder
the longer consumers could depend on them, and B2's fix had already changed behaviour
for anyone constructing `DockingLayoutSaver` directly — which was only possible
because `impl` was exported. That is now closed, so the remaining work no longer
widens the exposed surface as it goes. It also narrows what several other findings
cost: M4's publicly instantiable `DockContainerState` and the `StageUtils` half of
N1/N2 are no longer *external* API problems, only internal ones.

**Every BLOCKER and MAJOR is now closed.** The general round-trip test argued for in
theme 1 is no longer blocked behind any of them, and would now guard finished
behaviour rather than drive a fix - the better time to write it, and the main piece of
outstanding work on this branch beyond the list below.

The MINOR and NIT lists are closed as well, the MINOR items swept in one pass as
anticipated. **Every finding in this review is now resolved**, so the outstanding work
on this branch is the round-trip test above rather than anything on the list.

The three NIT items held back from the original sweep, because none of them was
mechanical, all landed in the end. T14 (`DockEventListener` on the public surface) was
the API-shape question belonging with M1, and went in by composition once it was clear
it needed no `core` change. T15 (the aliased `layoutStorage` field) waited on M5's
ownership decision and landed with it. T13 was held back with them as cosmetic, but
turned out to have a fix that needed no design call - see its entry.

## Changes made

`demos/basic/` has not been modified. `core/` has exactly one change, described
under B7: a 28-line additive `setUncollapsedSize` on `DockContainerLeaf`, no
existing line altered. `demos/persistence/` has the M1 changes — two field initialisers in `BoxApp`, a
`module-info` clause removed, and corrected Javadoc headers in its `provider` package —
which were unavoidable, since it was the only consumer of the `impl` exports M1 removes.
Everything else is in `persistence/api`.

Committed on `enhancement/issue-13`:

- `5f99ab8` — B1. `DefaultBentoProvider` now uses
  `ConcurrentHashMap<String, WeakReference<Bento>>`.
- `8af91f0` — B3 and B4. `AbstractAutoCloseableLayoutSaver.autoSave` catch widened;
  new `api/BentoStateTimeoutException`; bounded, cancelling waits in
  `PersistenceThreading`; shutdown budget plumbed through `DockingLayoutSaver` and
  `AbstractAutoCloseableLayoutSaver`; timeout excluded from
  `DockingLayoutRestorer`'s default-layout fallback; ordering documented on
  `LayoutSaver`. Adds 4 tests to `ft/.../PersistenceThreadingFT.java` (now 8).
- `57eb2a4` — B5. `conditionallyCollapseLeaves` call added to
  `DockingLayoutStateRestorer.restoreBranch`, plus new
  `ft/.../NestedCollapseRestoreFT.java` (2 tests).

- `d813654` — B6. `BentoLayoutStateCaptor.buildLeafState` now reads
  `SplitPane.isResizableWithParent(leaf)`, plus new
  `ft/.../LeafResizableWithParentRoundTripFT.java` (3 tests).
- `f718d18` — B7. New `core/.../DockContainerLeaf.setUncollapsedSize(double)`; new
  `restoreUncollapsedSize` in `DockingLayoutStateRestorer`, applied after the
  collapse; plus new `ft/.../LeafUncollapsedSizeRoundTripFT.java` (3 tests).
- `3bee6dd` — B2 and M2. `enableAutoSave` removed from the constructor; new static
  `startAutoSave`; `DefaultDockingLayoutPersistenceProvider` arms the saver it
  returns; the `isAutoSaveEnabled` gate removed from `close()`; plus new
  `ft/.../LayoutSaverConstructionFT.java` (7 tests).
- `f94bc20` — B8. `autoSaveLock` guarding the auto-save lifecycle;
  `isAutoSaveEnabled` made `volatile`; teardown split into a lock-assuming
  `disableAutoSaveInternal()`; the `closed` check moved inside the lock; `close()`
  saves outside it; class-level thread-safety documentation; plus new
  `ft/.../LayoutSaverAutoSaveLifecycleFT.java` (4 tests).
- `598c742` — B9 and NIT T1. Captor `getDockContainerRootBranch` guards a null scene
  and `toDragDropStageRoot` delegates to it; restorer always assigns a scene,
  falling back to an empty root branch; plus new
  `ft/.../SceneLessDragDropStageFT.java` (4 tests).

**B10 produced no code change** — see its entry. It was investigated with a
throwaway probe that was deleted once it had answered the question; nothing from that
investigation remains in the tree.

Uncommitted in the working tree:

- M12 — `BentoLayoutStateCaptor.buildLeafState` guards `getUncollapsedSize()` on a
  non-null side, plus new `ft/.../HeaderlessLeafCaptureFT.java` (3 tests).
- The NIT sweep plus N9 and N10 — T2 through T9, T11, T16, N9 and N10, across
  fourteen files in `persistence/api/src/main/java` plus
  `persistence/api/build.gradle`. All but two are documentation or whitespace. The two
  that touch code are T2 (the duplicate `logger.warn` in `restoreBranch` deleted) and
  T9 (`enableAutoSave` now takes a primitive `long`, with its Javadoc references
  updated to the new signature). No tests were added: nothing here has behaviour to
  pin beyond what the existing 75 already cover, and a test asserting the *absence*
  of a log line would pin the logging framework rather than the fix. T12 needed no
  work — B4's rewrite of `PersistenceThreading`'s imports had already closed it.
- T13 — `BentoState`'s two adders take a single argument instead of varargs, so
  `requireNonNull` now checks the element rather than the array. No call site changed.
- M1 — both `impl` exports dropped from `module-info`; new
  `api/DockingLayoutPersistence` entry point (with `uses` moved into this module); new
  `BentoProvider.of(Bento...)` factory plus
  `test/.../api/provider/BentoProviderTest.java` (2 tests); `BoxApp` obtains both
  providers through the api and reaches into no `impl` package.
  **This is the first change to `demos/persistence/`** — the "demos/basic/ has not
  been modified" note above still holds, but the persistence demo no longer reaches
  into `impl`. Verified by running the demo, with a negative control; see M1.
- N9 and N10's build change — `persistence/api/build.gradle` turns doclint on for the
  published `javadoc` task and adds a `javadocLint` task, wired into `check`, that
  lints at private visibility so a malformed comment on a non-public class fails the
  build. This is the closest thing to a test the sweep has, and it was confirmed to
  fail first: re-breaking N10's brace failed `javadocLint` with
  `error: unterminated inline tag` while `javadoc` still passed.
- This review document.

Every new test was confirmed to fail with its fix reverted — for B8, six runs with
synchronization stripped failed 3/4 tests every time, and six runs with it in place
passed 4/4; for B9, each half was reverted separately.

Verified with the project's full build command:

```
./gradlew build buildHealth checkAll checkJSpecify jreleaserConfig \
    --warning-mode all --rerun-tasks
```

`BUILD SUCCESSFUL`, 45 functional tests, 0 failures.

Re-verified after the NIT sweep, N9 and N10 with the same command less
`jreleaserConfig`:

```
./gradlew build buildHealth checkAll checkJSpecify \
    --warning-mode all --rerun-tasks
```

`BUILD SUCCESSFUL`, 563 tasks executed, 75 tests across the unit and functional source
sets, 0 failures, 0 errors. This command now also validates Javadoc: `javadocLint` is a
`check` dependency, so it runs here rather than needing a separate invocation. It was
run separately during development and confirmed to fail on a deliberately broken tag —
see N10.

One caveat about `--rerun-tasks`, and it is now observed rather than assumed. Across
this work the command failed intermittently — roughly one run in five — always with the
`AccessDeniedException` on a `build-cache-1` entry documented below, and always on a
dependency-analysis task in whichever subproject lost the race:

```
Execution failed for task ':persistence:codec:xml:findDeclaredProcsMain'
> Failed to store cache entry ... Couldn't move cache entry into local cache:
  java.nio.file.AccessDeniedException: ...build-cache-1\<hash>.part -> ...\<hash>
```

Earlier in this document I recorded one such failure as unidentified because I had not
captured the text before re-running; it has since been reproduced and read, so the
attribution is no longer a guess. The same command without `--rerun-tasks` passes
consistently, and no failure has ever landed in compilation, a test, `javadoc` or
`javadocLint`. Re-run, or drop the flag.

`jreleaserConfig` is excluded because it now fails on this
machine before it reaches anything this work touches — it writes
`build/jreleaser/trace.log`, keeps the handle open, and its own clean step then cannot
delete the directory. Confirmed environmental rather than a regression: it fails
identically with these changes stashed, on an empty `build/jreleaser`, and after
`gradlew --stop`. Add it to the list below rather than treating it as a result of
this sweep.

Three pre-existing conditions
that command surfaces and that are **not** from this work:

- ~100 `[MissingSummary]` Javadoc warnings in `core/.../Bento.java`.
- A `Project.getProperties` deprecation plus a configuration-cache problem from the
  JReleaser plugin, already acknowledged by a TODO in the root `build.gradle`.
- An `AccessDeniedException` storing a `build-cache-1` entry, from one of the
  dependency-analysis tasks. **This is caused by `--rerun-tasks` itself**, not by
  chance: several of those tasks across different subprojects produce byte-identical
  output and therefore the same cache key, so forcing them all to re-run makes them
  race to create one entry. Observed failing on `:persistence:api`,
  `:report-aggregation` and `:persistence:storage:file` for the same two hashes, and
  it still recurred after deleting those entries. The identical command **without**
  `--rerun-tasks` passes consistently. Re-running usually gets past it; if it
  persists, drop the flag for that run.

One note on warnings: touching `core/` forces it to recompile, which surfaces
pre-existing `[MissingSummary]` Javadoc warnings in `core/.../Bento.java`. Those are
not from this work and are unrelated to B7; they were simply not visible while
`core/` was cached as up-to-date.
