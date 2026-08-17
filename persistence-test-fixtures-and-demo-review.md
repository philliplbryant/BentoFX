# Review: `persistence/test-fixtures` and `demos/persistence`

Scope: two areas that are not production code but are read as though they were -
`persistence/test-fixtures`, whose fixtures four other modules build their tests
on, and `demos/persistence`, which is the worked example of the persistence API.
Covers the fixture sources and the tests that module owns, the demo's
application, providers, module descriptor and build file.

`demos/basic` was read for comparison only. `persistence/api` was read where a
finding depends on its contracts: who closes a `LayoutSaver`, which thread
resolves a `DockableState`, and what `PersistenceThreading` does when the caller
is already on the JavaFX application thread.

Line numbers refer to the files as they stand on `enhancement/issue-13` at
`f67689c`.

All four majors are fixed, along with every finding against the demo - N1 to N5 and
T7 to T9. What remains open is the seven fixture minors, N6 to N12, and the six
fixture nits, T1 to T6. All of it is verified in the working tree and not yet
committed.

`demos/basic` is untouched. Only one fix makes the two demos differ where they
previously agreed: [T7](#t7) removes a duplicated line the persistence demo had
inherited, so that line now appears twice in the basic demo and once here.
Everything else sits in code the basic demo has no counterpart for.

The demo fixes are **not exercised**. That demo has no test source set, and giving
it one would hand it something `demos/basic` does not have, so what stands behind
them is the compiler, NullAway, and the reading below. Two exceptions: M4 has a
regression test that fails against the previous fixture, and [T8](#t8)'s
transcription was measured, because moving twelve hand-written argument pairs onto
an enum is the kind of change that silently swaps two of them.

## Status

**No blockers, four majors, twelve minors, nine nits.** The majors were three in
the demo and one in the fixtures, unrelated to each other, and each finding below
records what was done about it. The demo's were all about lifecycle: the only
`LayoutSaver` it created was created while the application was closing, neither the
saver nor the restorer was ever closed, and a restored layout it could not apply
left the process running with no window. The fixtures' was that
`ThreadRecordingLayoutStorage` destroyed what it had stored the moment a new output
stream was opened, which is the defect
[B1](persistence-storage-review.md#b1) removed from both real storages.

Everything marked **Measured** was settled by running the code, not by reading it.
The probes were one temporary test class in `persistence/test-fixtures`, run once
and deleted; each measurement below quotes the output it came from. Four candidate
findings did not survive that step, or survived reading the API they depend on,
and are recorded at the end under [Withdrawn](#withdrawn) rather than dropped -
three of them look correct from the source alone and would be raised again by the
next reader.

The single most useful change was not any one fix: it was **giving the demo the
lifecycle it is meant to demonstrate** ([M1](#m1), [M2](#m2)). The framework's
auto-save, its listener removal and its storage release are all reached through
`close()`, and the demo called it nowhere - so the one artifact a user copies from
showed none of them. It now creates its saver at startup, closes it while the
windows still exist, and closes the restorer it borrows.

There is a pattern behind the fixture findings. Two generations of fixture live in
this module: `InMemoryLayoutStorage`, `InMemoryLayoutCodec` and
`SampleBentoStateFactory` are written to be substitutes for the real thing, with
defensive copies, `@author` tags and Javadoc on their public methods;
`TestLayoutStorage`, `TestLayoutCodec`, `ThreadRecording*` and
`SampleDockingLayoutDtoFactory` are written to be probes for one narrow question,
with none of those. Both kinds are reasonable. The trouble is that nothing in the
second kind says so, so the fixtures that quietly store nothing, refuse to
round-trip, or lose the previous layout look interchangeable with the ones that
behave.

### BLOCKER

None. Nothing in either area loses a layout a user had, and nothing in
`demos/basic` blocks work on the persistence demo - see
[demos/basic](#demos-basic).

### MAJOR

| | Module | Finding | Status |
|---|---|---|---|
| [M1](#m1) | demo | The only `LayoutSaver` is built inside the close handler and never closed, so auto-save never runs and its listener is never removed | **Fixed** 2026-08-17 |
| [M2](#m2) | demo | The `LayoutRestorer` is never closed, so the `LayoutStorage` it owns is never released | **Fixed** 2026-08-17 |
| [M3](#m3) | demo | A restored layout the demo cannot apply leaves the application running with no window and no way to exit | **Fixed** 2026-08-17 |
| [M4](#m4) | fixtures | `ThreadRecordingLayoutStorage` destroys the stored bytes when an output stream is opened | **Fixed** 2026-08-17 |

### MINOR

| | Module | Finding | Status |
|---|---|---|---|
| [N1](#n1) | demo | `applyBentoLayout` is public in a class whose every other member is private | **Fixed** 2026-08-17 |
| [N2](#n2) | demo | `rootBranches` holds a branch that is never displayed once a layout is restored | **Fixed** 2026-08-17 |
| [N3](#n3) | demo | `Runner` calls `printStackTrace`, which this project's standards rule out | **Fixed** 2026-08-17 |
| [N4](#n4) | demo | The dockable states are published from a queued task, so every read depends on JavaFX queue ordering | **Fixed** 2026-08-17 |
| [N5](#n5) | demo | `BoxAppDockableMenuFactoryProvider` names its parameter after the wrong kind of identifier | **Fixed** 2026-08-17 |
| [N6](#n6) | fixtures | `InMemoryLayoutStorage.exists()` is true for empty content, which neither real storage reports any more | **Open** |
| [N7](#n7) | fixtures | `ThreadRecordingLayoutCodec` does not round-trip: what `encode` records is not what `decode` returns | **Open** |
| [N8](#n8) | fixtures | The two provider fixtures record what they were called with in fields no thread boundary protects | **Open** |
| [N9](#n9) | fixtures | `SampleDockingLayoutDtoFactory` puts one divider instance in two parents | **Open** |
| [N10](#n10) | fixtures | `InMemoryLayoutStorage` mixes `volatile` with `synchronized`, and the write that matters holds neither | **Open** |
| [N11](#n11) | fixtures | Two Javadoc conventions across one module, and half the fixtures have no `@author` | **Open** |
| [N12](#n12) | fixtures | `AbstractTestLayoutProvider` satisfies the provider interface by name without declaring it | **Open** |

### NIT

| | Module | Finding | Status |
|---|---|---|---|
| [T1](#t1) | fixtures | `import java.io.*` in a module whose every other file imports explicitly | **Open** |
| [T2](#t2) | fixtures | The anonymous output stream calls `toByteArray()`, which the enclosing class also declares | **Open** |
| [T3](#t3) | fixtures | `TestLayoutStorage` accepts a write, stores nothing, and says nothing | **Open** |
| [T4](#t4) | fixtures | `new DragDropStageStateBuilder(true)` passes an unnamed boolean | **Open** |
| [T5](#t5) | fixtures | Truncated `describedAs` strings, ending mid-word in an ellipsis | **Open** |
| [T6](#t6) | fixtures | `SampleBentoStateFactory` claims every persistable property is set; two of its containers set a few | **Open** |
| [T7](#t7) | demo | `setPruneWhenEmpty(false)` is called twice on the same leaf, mirrored from `demos/basic` | **Fixed** 2026-08-17 |
| [T8](#t8) | demo | Twelve near-identical `put` blocks that the enum they read from could drive | **Fixed** 2026-08-17 |
| [T9](#t9) | demo | The two menu-factory providers declare their `factory` field below the method that returns it | **Fixed** 2026-08-17 |

Every identifier in the tables links to that finding's own section. The anchors
are explicit rather than derived from the heading text, matching the other three
reviews, so that appending an outcome to a heading later does not break the link.

---

## MAJOR

### <a id="m1"></a>M1. The only `LayoutSaver` is built inside the close handler and never closed, so auto-save never runs and its listener is never removed

`demos/persistence/.../BoxApp.java:189, 290-302`, with
`persistence/api/.../impl/AbstractAutoCloseableLayoutSaver.java:155-164, 281-308`

The demo builds a saver in one place, and that place is the window's close
handler:

```java
stage.setOnCloseRequest(this::saveDockingLayout);
```

```java
final LayoutSaver layoutSaver =
        persistenceProvider.getLayoutSaver(
                DEFAULT_LAYOUT_IDENTIFIER,
                bentoProvider
        );

layoutSaver.saveLayout();
```

Three things follow, and each of them costs the demo something it exists to show.

**Auto-save never runs.** `DefaultDockingLayoutPersistenceProvider.getLayoutSaver`
returns its saver through `AbstractAutoCloseableLayoutSaver.startAutoSave`, which
arms a five-minute timer and registers a `DockEventListener` on every `Bento`. The
demo asks for that saver as the window is closing, so the timer is armed for an
application that is about to exit and has never had one while the user was editing
the layout. The framework's headline feature is present, configured, and never
exercised.

**The listener is never removed.** `close()` is what unregisters
`dockEventListener` from each `Bento`'s event bus and shuts the scheduler down. The
demo never calls it, so the registration outlives the object that made it. That
this project cares about the pattern is not in doubt - the saver holds the listener
in a field precisely so add and remove can be handed the same instance, with a
comment saying why - and the demo is the artifact a reader copies.

**The final save is the one thing that does work**, because `saveLayout()` is
called directly. Note that `close()` would not be a drop-in replacement: it routes
through `autoSave(true)`, which returns without saving when no dock event has been
received. For a demo, saving unconditionally is the better behavior; the point is
that the demo demonstrates neither the try-with-resources form the class
documentation promotes nor any release of what it built.

The saver is now created once, in `start`, after the layout has been applied, and
kept in a field. Auto-save therefore runs for the session, which is what the demo
was meant to show, and building it after the layout is applied matters: a capture
only sees root branches that have a `Scene`. The close handler saves explicitly and
then releases the saver:

```java
final LayoutSaver saver = layoutSaver;

try (saver) {
    if (saver == null) {
        return;
    }
    saver.saveLayout();
} catch (final BentoStateException e) {
    logger.warn("Could not save the docking layout.", e);
}
```

Closing there rather than from a `stop()` override is not a style preference. This
demo exits through `System.exit(0)` from `setOnHidden`, mirrored from
`demos/basic`, and that call never lets `stop()` run - so a `stop()` override would
have looked like a fix and released nothing. The close handler also runs while the
windows still exist, which is the constraint the original comment on this method
already recorded. Saving explicitly before the close is deliberate: `close()` saves
only when a dock event has arrived since the last save, and a demo should write a
layout on exit whether or not the user moved anything.

Not exercised - see the note in [Status](#status).

### <a id="m2"></a>M2. The `LayoutRestorer` is never closed, so the `LayoutStorage` it owns is never released

`demos/persistence/.../BoxApp.java:310-329`, with
`persistence/api/.../storage/LayoutStorage.java:10-18` and
`persistence/api/.../impl/DockingLayoutRestorer.java:158-160`

`getDockingLayout` builds a restorer, calls `restoreLayout`, and drops it:

```java
final LayoutRestorer layoutRestorer =
        persistenceProvider.getLayoutRestorer(...);

return layoutRestorer.restoreLayout(
        this::getDefaultDockingLayout
);
```

`LayoutRestorer` is `AutoCloseable`, and `DockingLayoutRestorer.close()` closes the
`LayoutStateReader`, which closes the `LayoutStorage`. The interface states the
ownership plainly: an instance "is owned by whichever component it is handed to",
and that component "closes it when that component is closed". The demo is the one
worked example of that contract and it does not honour it.

With the file storage the demo ships with, releasing the storage costs nothing -
there is no handle to give back. That is what makes this worth writing down rather
than harmless: the demo's `build.gradle` offers the H2 storage as a one-line swap,
and the reader who takes that swap inherits a usage the storage documentation
tells them not to write.

The restorer is now a try-with-resources resource, so the storage is released on
every path out of the method. Closing it after `restoreLayout` returns is safe
because the layout is fully built by then - the containers are in memory and no
longer need the storage - and `DockingLayoutRestorer.close()` declares no checked
exception, so the existing `catch (BentoStateException)` around
`getLayoutRestorer` is still the only one needed.

Not exercised - see the note in [Status](#status).

### <a id="m3"></a>M3. A restored layout the demo cannot apply leaves the application running with no window and no way to exit

`demos/persistence/.../BoxApp.java:190, 380-409`

`applyBentoLayout` decides between four branches, and only the last one puts
anything on screen:

```java
if (bentoRootBranches.size() != 1) {
    logger.error(...);
} else if (stage == null) {
    logger.error(...);
} else if (!bentoLayout.matchesIdentity(bento)) {
    logger.warn(...);
} else {
    final Scene scene = new Scene(bentoRootBranches.getFirst());
    ...
    stage.setScene(scene);
    stage.show();
}
```

The first three branches log and return. No scene is set, `stage.show()` is never
called, and because the demo's only exit path is
`stage.setOnHidden(e -> System.exit(0))`, a stage that was never shown can never be
hidden. The JavaFX toolkit keeps running with no window, so what the user sees is a
process that started and did nothing, ended only from the task manager. The log
line explaining why goes to a console the user of a windowed application is not
reading.

`getDefaultDockingLayout` always produces exactly one root branch, so this needs a
persisted layout with a different count - which is reachable, since what gets
persisted is whatever the previous run's `Bento` had registered. A demo is allowed
to be strict about what it can apply. What it should not do is fail closed into an
invisible process.

Both methods now report whether they applied anything, and `start` falls back:

```java
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

The else-if chain became guard clauses, each returning `false`, which is what makes
the outcome reportable at all - and it also removes a hazard the chain was hiding,
since three of its four branches shared one exit into code that dereferences
`stage` and calls `getFirst()` on a list it has just been told does not hold one
element.

The fallback is reachable in the case that matters and cannot loop: the default
layout is built from `rootBranches`, which holds exactly one root branch, and it
carries this `Bento`'s own identifier, so the two conditions that rejected the
restored layout cannot reject it. The remaining `logger.error` covers only
`stage == null`, which `start` has already ruled out.

The drag/drop stages moved inside the success path. Showing them from a layout
whose root branch could not be applied would have put floating windows on screen
with nothing behind them, which is a worse outcome than the one this finding is
about.

Not exercised - see the note in [Status](#status).

### <a id="m4"></a>M4. `ThreadRecordingLayoutStorage` destroys the stored bytes when an output stream is opened - MEASURED

`persistence/test-fixtures/.../storage/ThreadRecordingLayoutStorage.java:19, 28-32`

The fixture holds one `ByteArrayOutputStream` for its lifetime and hands the same
instance to every caller, resetting it first:

```java
private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

@Override
public synchronized OutputStream openOutputStream() {
    openOutputStreamThread.set(Thread.currentThread());
    outputStream.reset();
    return outputStream;
}
```

So the previously stored layout is gone the moment a save opens its stream,
whatever happens next. Measured by storing three bytes, then opening a second
stream the way a save that fails part way through would, and abandoning it:

```
PROBE recording-stored-after-save=3
PROBE recording-stored-after-reopen=0
PROBE recording-exists-after-reopen=false
```

This is exactly the behavior [B1](persistence-storage-review.md#b1) removed from both
real storages, which now stage or buffer their bytes and only publish them when the
stream closes cleanly. A fixture is entitled to be simpler than what it stands in
for, but this one is simpler in the one dimension the blocker was about, and it
gives no sign of it. A test that used this fixture to check that a failed save
leaves the previous layout alone would fail for the fixture's reasons, and a test
that asserted the fragment was visible would pass for them.

Each call now gets a buffer of its own, and closing the stream is what stores its
bytes - which is what `InMemoryLayoutStorage` already does in the same package, and
what both real storages do by staging or buffering. `exists()` answers from the
stored bytes rather than from a buffer a save is still filling, so a write in
flight no longer reports a layout that is not there yet. The stored bytes live in an
`AtomicReference`, which also retires the `synchronized` methods: the fixture's
whole purpose is to be called from more than one thread.

The regression test is `previouslyStoredBytesSurviveAnAbandonedStream`, next to
this fixture's existing tests and modelled on
`FileLayoutStorageIT.previousLayoutSurvivesAStreamThatIsNeverClosed`. Measured
against the previous fixture, it fails - and the failure is worse than this finding
first described:

```
[storage.toByteArray() after an abandoned save]
Expecting actual:
  [9]
to contain exactly (and in same order):
  [1, 2, 3]
```

The abandoned fragment had not merely displaced the stored layout, it had *become*
the stored layout, because the caller was writing into the storage's own buffer.
That is [B1](persistence-storage-review.md#b1)'s data loss exactly, in the fixture
a test would use to check for it.

The one behavior change to know about: bytes written but not yet flushed and closed
are no longer visible through `exists()` or `toByteArray()`. `DockingLayoutSaverFT`,
the only other user of this fixture, asserts on recorded threads and passes
unchanged - re-run against the new fixture rather than taken from cache.

---

## MINOR

### <a id="n1"></a>N1. `applyBentoLayout` is public in a class whose every other member is private

`demos/persistence/.../BoxApp.java:380`

`buildDockable`, `handleDockableClosing`, `addDockable`, `saveDockingLayout`,
`getDockingLayout`, `applyDockingLayout` and `getDefaultDockingLayout` are all
private. `applyBentoLayout` is public, and its only caller is
`applyDockingLayout` four lines up. Nothing outside the class can usefully call it
either - it reads `stage`, which only `start` sets. This project's standards ask
for accidental public surface to be flagged, and a demo is where a reader learns
which members were meant to be reachable.

Private now. [M3](#m3) had already changed its signature, so this was the moment to
settle its visibility as well.

### <a id="n2"></a>N2. `rootBranches` holds a branch that is never displayed once a layout is restored

`demos/persistence/.../BoxApp.java:69-73, 178, 359-373`

The field says what it is for:

```java
/**
 * Collect the {@link DockContainerRootBranch} so they can be persisted.
 */
private final List<DockContainerRootBranch> rootBranches =
        new ArrayList<>();
```

It is not what gets persisted. A save captures whatever each `Bento` has
registered, which is the branch that has a `Scene` - and on a run that restores a
layout, that is the branch `applyBentoLayout` took from the restored
`BentoLayout`, not the default branch `start` put in this list. The list's only
real use is `getDefaultDockingLayout`, the supplier handed to `restoreLayout` for
when there is nothing to restore.

Nothing breaks, because the default supplier is called before any of it is
attached. But the comment tells a reader that this collection is the persistence
input, which is the one thing it is not, and after a successful restore the demo
holds a root branch that will never be shown.

The field is `defaultRootBranches` now, and its documentation says what it feeds -
`getDefaultDockingLayout`, for when there is nothing to restore - and states that a
capture reads the root branches each `Bento` knows about instead, so the branch in
here is not the one that gets persisted.

### <a id="n3"></a>N3. `Runner` calls `printStackTrace`, which this project's standards rule out

`demos/persistence/.../Runner.java:35-37`

```java
} catch (Exception e) {
    e.printStackTrace(System.err);
}
```

The agent instructions for these modules are explicit - no swallowed exceptions,
no empty catch blocks, no `printStackTrace` - and the class already carries a
`@SuppressWarnings("java:S106")` acknowledging that it writes to the standard
streams on purpose.

The purpose is defensible: this runs before `LogManager` has read its
configuration, so a logger here would be the thing that just failed. The `else`
branch immediately above already handles that case with
`System.err.println(...)`, so the fix is to say what happened in the same voice
rather than to dump a stack trace, or to state in a comment why this one call is
the exception. As written, the file both bans and performs the same act.

It now reports the failure the way the branch above it does, naming the resource and
including the exception, and a comment says why this one place writes to the standard
error stream rather than to a log. The stack trace goes: for a malformed logging
configuration the exception's type and message are what identify it.

### <a id="n4"></a>N4. The dockable states are published from a queued task, so every read depends on JavaFX queue ordering

`demos/persistence/.../provider/BoxAppDockableStateProvider.java:45-46, 54-167`,
read from `BoxApp.java:78-81, 162-176`

The constructor populates a `HashMap` inside a `Platform.runLater`, for a reason it
states - the states hold JavaFX nodes, and the constructor runs on the
JavaFX-Launcher thread where those cannot be built. The consequence is that the map
is empty when the constructor returns, and the field initializer that calls it
completes before `start` begins.

`start` then resolves every dockable through it:

```java
addDockable(WORKSPACE, dockableStateProvider, leafWorkspaceTools);
```

and `addDockable` treats a miss as a warning and moves on:

```java
() -> logger.warn("Could not add dockable {}.", dockableProperties)
```

This works, and works for a reason the code does not state: the queued task was
submitted before the launcher queued `start`, and the JavaFX event queue runs them
in that order. Nothing in the JavaFX contract promises that relationship between an
`Application` constructor and `start`, and if it ever failed to hold, the demo
would come up with an empty layout and twelve warnings in a log rather than an
error anyone would notice. See [Withdrawn](#withdrawn), W3, for the sharper
version of this that was raised and did not survive: the map is not read across a
thread boundary.

The states are built on demand now, inside `resolveDockableState`, so there is no
queued task and nothing to order. Both callers - the application while it starts,
and the restorer through the persistence API - are on the JavaFX application thread,
which is what makes building JavaFX components there safe; the comment on the method
says so, because that is the assumption the next reader needs and the one the
constructor's `Platform.runLater` was standing in for. `demos/basic` builds its
dockables inline in `start`, so this moves the persistence demo toward it rather
than away.

### <a id="n5"></a>N5. `BoxAppDockableMenuFactoryProvider` names its parameter after the wrong kind of identifier

`demos/persistence/.../provider/BoxAppDockableMenuFactoryProvider.java:18-25`

```java
public Optional<DockableMenuFactory> getDockableMenuFactory(
        final String dockContainerLeafIdentifier
) {
```

The argument is a dockable's identifier; the name came from
`BoxAppDockContainerLeafMenuFactoryProvider`, where it is correct. Both providers
ignore the argument and return one shared factory, so nothing misbehaves - but a
reader matching the demo against the interface it implements is told the wrong
thing about what the framework passes. The class is also the only one in the demo
with no `@author` tag.

The parameter is `dockableIdentifier` now, and the class has the `@author` tag its
siblings carry.

### <a id="n6"></a>N6. `InMemoryLayoutStorage.exists()` is true for empty content, which neither real storage reports any more - MEASURED

`persistence/test-fixtures/.../storage/InMemoryLayoutStorage.java:37-47, 57-60`

The behavior is deliberate and documented: the byte-array constructor marks the
storage as existing "even when the supplied byte array is empty, which allows
tests to distinguish between 'missing' and 'existing but empty' storage".
Measured:

```
PROBE in-memory-default-exists=false
PROBE in-memory-empty-exists=true
```

That distinction no longer exists in either implementation. The database storage
asks for the payload's length and answers `false` unless it is greater than zero;
the file storage, since [M6](persistence-storage-review.md#m6), answers
`file.isFile() && file.length() > 0`. So "existing but empty" is a state a test can
construct only with this fixture, and a test that exercises what the reader does
with it - decode an empty layout, report a failure - is exercising a path no real
storage produces.

Keeping the constructor is fine; what is missing is the sentence saying that the
state it creates is the fixture's own, so the next person to reach for it knows
they are testing the fixture rather than the contract.

### <a id="n7"></a>N7. `ThreadRecordingLayoutCodec` does not round-trip: what `encode` records is not what `decode` returns - MEASURED

`persistence/test-fixtures/.../codec/ThreadRecordingLayoutCodec.java:21-22, 30-45, 58-64`

Two fields, written by two different methods:

```java
private List<BentoState> encodedStates = List.of();
private List<BentoState> decodedStates = List.of();
```

`encode` fills the first. `decode` returns the second, and reads nothing from the
`InputStream` it is handed. Only `writeEncoded` fills `decodedStates`, so encoding
a layout and decoding it back yields an empty list. Measured:

```
PROBE recording-codec-encoded=1
PROBE recording-codec-decoded-after-encode=0
```

For recording which thread called which method, this is enough, and the
`writeEncoded` seam is documented. What is not documented is the trap: a test that
saves through this codec and then restores gets an empty layout and no error, and
the natural reading of "codec" is that the two halves are inverses.
`InMemoryLayoutCodec`, in the same package, is the fixture that does round-trip -
the class documentation should point at it.

### <a id="n8"></a>N8. The two provider fixtures record what they were called with in fields no thread boundary protects

`persistence/test-fixtures/.../provider/TestLayoutCodecProvider.java:11, 20-24`
and `.../provider/TestLayoutStorageProvider.java:14-15, 24-32`

```java
private int createdCodecCount;
...
createdCodecCount++;
```

```java
private @Nullable String layoutIdentifier;
...
this.layoutIdentifier = layoutIdentifier;
```

Both are written by the framework calling the provider and read afterwards by the
test asserting on it. The persistence API deliberately moves work between the
JavaFX application thread and its own I/O thread, so which thread performs the
write is the framework's business, not the test's, and there is no ordering
between that write and the test's read. A read that misses the write shows up as
`0` or `null` in an assertion that ran too early - the failure a test author will
call flaky and re-run.

The two `ThreadRecording*` fixtures in this module already use `AtomicReference`
for exactly this, which is what makes the inconsistency worth a line: the module
knows the answer in one place and not in the other. An `AtomicInteger` and two
`AtomicReference`s cost nothing here.

### <a id="n9"></a>N9. `SampleDockingLayoutDtoFactory` puts one divider instance in two parents - MEASURED

`persistence/test-fixtures/.../codec/dto/SampleDockingLayoutDtoFactory.java:18-23, 72-88`

The class documentation promises that "every container appears in exactly one
parent, and every identifier is distinct". One `DividerPositionDto` breaks the
spirit of it:

```java
final DividerPositionDto divider = new DividerPositionDto();
divider.index = DIVIDER_INDEX;
divider.position = DIVIDER_POSITION;

final DockContainerBranchDto branch = new DockContainerBranchDto();
...
branch.dividerPositions.add(divider);

final DockContainerRootBranchDto root = new DockContainerRootBranchDto();
...
root.dividerPositions.add(divider);
```

Measured:

```
PROBE root-divider-count=1
PROBE branch-divider-count=1
PROBE divider-is-same-instance=true
```

The DTOs are public-field carriers, so this is one mutable object reachable through
two paths. A mapper that normalises a divider in place would change both parents at
once, and a round-trip that lost the branch's divider and duplicated the root's
would still compare equal on the values. Two instances with distinct positions
would make the fixture say what its documentation says.

### <a id="n10"></a>N10. `InMemoryLayoutStorage` mixes `volatile` with `synchronized`, and the write that matters holds neither

`persistence/test-fixtures/.../storage/InMemoryLayoutStorage.java:17-18, 63-72`

`exists` and `bytes` are `volatile`; `openOutputStream`, `openInputStream`,
`write`, `delete` and `toByteArray` are `synchronized`. The two strategies overlap
everywhere except in the one place that publishes a saved layout:

```java
public synchronized OutputStream openOutputStream() {
    return new ByteArrayOutputStream() {
        @Override
        public void close() throws IOException {
            super.close();
            bytes = toByteArray();
            exists = true;
        }
    };
}
```

`close()` runs whenever the caller closes the stream, which is not inside the
`synchronized` method that returned it. So the monitor on `openOutputStream`
protects nothing about the write, and what makes the assignment visible to a later
reader is the `volatile` on the fields. That is sound, and it is not what the
method signatures suggest. Either the fields carry the safety and the
`synchronized` keywords are noise on `openOutputStream`/`openInputStream`, or the
`close()` body should take the monitor and the fields need no `volatile`. A fixture
that other modules' concurrency tests lean on is worth being explicit in.

### <a id="n11"></a>N11. Two Javadoc conventions across one module, and half the fixtures have no `@author`

`persistence/test-fixtures/.../storage/TestLayoutStorage.java:10-13`,
`.../storage/ThreadRecordingLayoutStorage.java:12-15`,
`.../codec/TestLayoutCodec.java:11-14`,
`.../codec/ThreadRecordingLayoutCodec.java:15-17, 47-57`,
`.../provider/AbstractTestLayoutProvider.java:3-6`,
`.../codec/dto/SampleDockingLayoutDtoFactory.java:18-23`

`InMemoryLayoutStorage`, `InMemoryLayoutCodec` and `SampleBentoStateFactory` carry
an `@author` tag, a Javadoc sentence on each public method, and the
`@param name description.` form used everywhere else in this repository. The six
files above carry no `@author`, no method Javadoc, and where they do document a
parameter they use a different form:

```java
     * @param bentoStates
     *        States to return when decoding.
```

The fixtures are not published - `bento.test.test-fixtures` applies
`java-library` and `java-test-fixtures` and no publishing convention - so the
standard that every public method carries Javadoc is looser here than in a shipped
module. The inconsistency is the finding rather than the absence: one module,
two conventions, and no way for a reader to tell which one is current.

### <a id="n12"></a>N12. `AbstractTestLayoutProvider` satisfies the provider interface by name without declaring it

`persistence/test-fixtures/.../provider/AbstractTestLayoutProvider.java:6-25`

```java
public abstract class AbstractTestLayoutProvider {
    ...
    public final String getIdentifier() { ... }
    public final boolean isDefault() { ... }
}
```

`getIdentifier()` and `isDefault()` are `LayoutPersistenceComponentProvider`'s
methods, and each subclass implements a sub-interface of it separately. The base
class supplies both by coincidence of name: nothing checks that these signatures
still match the interface, so a change to the interface breaks the subclasses with
no error at the class that actually implements the methods. Declaring
`implements LayoutPersistenceComponentProvider` on the base costs one clause and
makes the relationship the compiler's business.

---

## NIT

### <a id="t1"></a>T1. `import java.io.*` in a module whose every other file imports explicitly

`persistence/test-fixtures/.../storage/InMemoryLayoutStorage.java:5`

The four other fixture files that need the same types list them one per line.
`TestLayoutStorage`, in the same package and needing exactly the same four, is the
direct contrast.

### <a id="t2"></a>T2. The anonymous output stream calls `toByteArray()`, which the enclosing class also declares

`persistence/test-fixtures/.../storage/InMemoryLayoutStorage.java:68, 100-102`

Inside the anonymous `ByteArrayOutputStream`, `bytes = toByteArray()` resolves to
the stream's own inherited method, which is what is wanted. The enclosing class
declares a public `toByteArray()` of its own that returns a defensive copy of the
stored bytes. The line is correct and reads as though it might not be; qualifying
it, or naming one of the two differently, settles the question for the reader.

### <a id="t3"></a>T3. `TestLayoutStorage` accepts a write, stores nothing, and says nothing - MEASURED

`persistence/test-fixtures/.../storage/TestLayoutStorage.java:13-28`

`openOutputStream` returns a fresh `ByteArrayOutputStream` that nothing reads,
`openInputStream` returns an empty stream, and `exists()` is always `false`.
Measured:

```
PROBE test-storage-exists-after-write=false
PROBE test-storage-read-back=0
```

For the provider-selection tests it is named for, that is all it needs to do. One
sentence saying it discards what it is given would stop the next reader using it
for a round trip - the same gap as [N7](#n7), in the storage half.

### <a id="t4"></a>T4. `new DragDropStageStateBuilder(true)` passes an unnamed boolean

`persistence/test-fixtures/.../codec/state/SampleBentoStateFactory.java:130`

Every other value in this factory is either named by its setter or pulled from a
constant. This one is a bare `true` in a constructor, and a reader has to open
`DragDropStageState` to learn which property it sets.

### <a id="t5"></a>T5. Truncated `describedAs` strings, ending mid-word in an ellipsis

`persistence/test-fixtures/src/test/.../codec/InMemoryLayoutCodecTest.java:57`

```java
.describedAs("exception thrown by () -> reader.decode(new ByteArrayInputStream(outputStream.toByteArr...")
```

The description is the code that follows it, cut off at a fixed width. When the
assertion fails, the message repeats the call AssertJ already reports and stops
mid-identifier. The same pattern appears in `persistence/api`'s
`DefaultDockingLayoutPersistenceProviderTest`, so it is a habit rather than a
slip: a phrase describing what was expected would carry more than a truncated
echo.

### <a id="t6"></a>T6. `SampleBentoStateFactory` claims every persistable property is set; two of its containers set a few

`persistence/test-fixtures/.../codec/state/SampleBentoStateFactory.java:28-31, 118-127, 149-162`

The class documentation says "Every persistable property is set to a value
distinct from its neighbors', so a round-trip that drops one, or crosses two,
shows up as an inequality". `createLastLeafState` sets no selected dockable and
holds no dockables, and the leaf inside `createStageRootBranchState` sets only its
side. Both are useful cases to have in the fixture - an empty leaf and a sparse one
are exactly what a round-trip should survive - so the documentation is what needs
adjusting, not the layout.

### <a id="t7"></a>T7. `setPruneWhenEmpty(false)` is called twice on the same leaf, mirrored from `demos/basic`

`demos/persistence/.../BoxApp.java:118-119`, mirroring
`demos/basic/.../BoxApp.java:46-47`

```java
leafTools.setPruneWhenEmpty(false);
leafTools.setPruneWhenEmpty(false);
```

Harmless, and faithful: the duplicate is in the basic demo and was carried across
with everything else.

Removed from the persistence demo on request. `demos/basic` keeps its pair, so this
is the one place the two demos now differ where they previously agreed - worth
knowing when the next comparison between them is made, and worth removing there too
whenever that demo is next touched.

### <a id="t8"></a>T8. Twelve near-identical `put` blocks that the enum they read from could drive

`demos/persistence/.../provider/BoxAppDockableStateProvider.java:59-166`

Every entry has the same six lines, differing in the `DockableProperties` constant
and two integers. Those two integers are the shape and color of the dockable's
icon, and `DockableProperties` already carries the per-dockable data - so two more
enum fields and a loop over `values()` would replace a hundred lines and make
adding a dockable a one-line change. A demo has some licence to be explicit
instead of clever; at twelve repetitions of six lines, the repetition is what a
reader has to hold rather than the pattern.

That is what it does now: the icon's shape and color sit on the enum, one loop over
`values()` builds every state, and `createSecondDockableState` folded into the same
builder - the undecorated dockable is the one the enum reports as
`isDecorated() == false`, which is also how it keeps its plainer label and its
absence of an icon and a menu.

**Measured**, because moving twelve hand-written argument pairs onto an enum is
exactly the change that silently swaps two of them. The enum's mapping printed and
compared against the twelve original pairs:

```
PROP WORKSPACE      decorated=true  shape=1 color=0
PROP BOOKMARKS      decorated=true  shape=1 color=1
PROP MODIFICATIONS  decorated=true  shape=1 color=2
PROP LOGGING        decorated=true  shape=2 color=0
PROP TERMINAL       decorated=true  shape=2 color=1
PROP PROBLEMS       decorated=true  shape=2 color=2
PROP CLASS_1        decorated=true  shape=0 color=0
PROP CLASS_2        decorated=true  shape=0 color=1
PROP CLASS_3        decorated=true  shape=0 color=2
PROP CLASS_4        decorated=true  shape=0 color=3
PROP CLASS_5        decorated=true  shape=0 color=4
PROP SOMETHING_ELSE decorated=false shape=-1 color=0
```

All twelve match. The last line also settles a trap this change had to avoid: the
undecorated shape mode is written as the literal `-1` rather than as a named
constant, because an enum's static fields are not initialized until after its
constants, so a constant read from the constructor would have arrived as zero - and
zero is a valid shape mode.

### <a id="t9"></a>T9. The two menu-factory providers declare their `factory` field below the method that returns it

`demos/persistence/.../provider/BoxAppDockableMenuFactoryProvider.java:20-32` and
`.../provider/BoxAppDockContainerLeafMenuFactoryProvider.java:23-32`

Both classes read `return Optional.of(factory);` before the reader has met
`factory`. Every other class in the demo and in the fixtures declares its static
fields at the top.

Both now declare it there too.

---

## <a id="demos-basic"></a>demos/basic

Read for comparison only, and **no blocker was found there** - nothing in the
basic demo stands in the way of the persistence demo or of the findings above.

Two observations, neither proposed as a change:

**The duplicated `setPruneWhenEmpty(false)`** at `demos/basic/.../BoxApp.java:46-47`
is the origin of [T7](#t7). It is harmless in both demos.

**`DockContainerBranch.setResizableWithParent` and
`SplitPane.setResizableWithParent` are the same method**, which is worth stating
because the two demos call it by different names and that reads like a divergence.
See [Withdrawn](#withdrawn), W1.

Everything else the persistence demo does differently from the basic demo is
accounted for by what it demonstrates: a named `Bento`, the stage-building flags,
providers standing in for the inline factories, `DockContainerRootBranch` where
basic uses `DockContainerBranch`, and a layout obtained from a restorer rather than
built in place. None of it looks like drift.

---

## <a id="withdrawn"></a>Withdrawn

**W1. The two demos configure resizing through the same method.** The basic demo
calls `DockContainerBranch.setResizableWithParent(leafTools, false)` and the
persistence demo calls `SplitPane.setResizableWithParent(leafTools, false)`, which
looked like the persistence demo bypassing a framework wrapper - and would have
mattered, because the property is persisted.
`core/.../DockContainerBranch.java:27` settles it:

```java
public non-sealed class DockContainerBranch extends SplitPane implements DockContainer {
```

There is no wrapper. Both calls are the one static method `SplitPane` declares,
and `BentoLayoutStateCaptor:377-383` captures the same property through
`SplitPane.isResizableWithParent`, with a comment explaining that this - not
`leaf.isResizable()` - is what the framework applies. The demos differ in which
class name they qualify the call with, and in nothing else.

**W2. The demo's save-on-close does not deadlock the JavaFX thread.**
`saveDockingLayout` runs in a close-request handler, so it is on the JavaFX
application thread, and saving hands the capture back to that same thread and
waits for it. That is a self-deadlock in the general case, bounded only by a
ten-second timeout, and it would mean the demo's final save never completes.
`PersistenceThreading` handles it: `callOnFxThread` runs the task inline when
`Platform.isFxApplicationThread()` is already true (line 141-143), and
`callOffFxThread` does the converse for the I/O half (line 204-206). The class
documentation states both. What remains true, and is documented there rather than
being a defect, is that the save freezes the UI for as long as the storage write
takes.

**W3. The demo's dockable-state map is not read across a thread boundary.** The
map is written inside a `Platform.runLater` and handed to a restorer that decodes
off the JavaFX thread, which reads like a `HashMap` published without
synchronization. It is not: `DockingLayoutRestorer:110` wraps state restoration in
`PersistenceThreading.callOnFxThread`, and `DockingLayoutStateRestorer:512` calls
`resolveDockableState` from inside that task, so both the write and every read
happen on the JavaFX application thread. The ordering question survives as
[N4](#n4); the data race does not.

**W4. The check mark in the demo's leaf menu is not an encoding hazard.**
`BoxAppDockContainerLeafMenuFactoryProvider:37` contains a literal `✓`, and
`demos/basic` contains the same character, so a source encoding that differed from
the one the file was written in would turn both into mojibake.
`bento.project.project-convention.gradle:149` sets `encoding = 'UTF-8'` for
compilation, so the source encoding is pinned rather than inherited from the
platform.
