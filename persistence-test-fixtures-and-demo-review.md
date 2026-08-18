# Review: layout persistence as a whole

Scope: every module under `persistence` - `api`, `codec/common`, `codec/json`, `codec/xml`,
`storage/file`, `storage/db/h2`, `test-fixtures` - plus `demos/persistence`, the
persistence sections of `README.md`, and both documents under `docs/persistence`.

`core` and `demos/basic` were read where the persistence framework depends on them
and are otherwise left alone. Nothing in either blocks this framework, so neither
carries a finding here.

This pass reads the framework the way a migrating application will: as one API to
learn, against the JIDE docking framework it is meant to replace, and against the
plan to let users create and switch named layouts at runtime. It therefore looks at
the public surface and the documents that describe it rather than at
implementation detail.

Line numbers refer to the files as they stand on `enhancement/issue-13` at
`f8eec95`.

## Status

**No blockers, two majors, seven minors, four nits.** Eleven are fixed: every
documentation and consistency defect, and the missing catalog operations, which were
implemented after this pass rather than only recommended. What remains open is three
API decisions and one that is decided but not yet built.

The documentation was the substance of this pass. Three of the persistence
framework's behaviors changed recently, and the README and implementation document
still described the previous ones, including in code an application would copy. A
library's documentation being wrong about its own lifecycle costs more than most
defects in the library, because it is what a reader trusts instead of reading the
source.

Everything marked **Measured** was settled by running a tool over the artifacts,
not by reading about them: the JIDE method counts come from `javap` over the jars
in the Gradle cache, and the BentoFX counts from `javap` over the built classes.

The single most useful change is not any one fix: it is **giving the framework the
operations a layout catalog needs** ([M2](#m2)). Named layouts are the next feature,
JIDE advertises the same capability as two of its selling points, and until this pass
the API could express neither without an application reaching around it to list a
directory or query a table itself.

### BLOCKER

None. Nothing in the framework loses a layout, and nothing in `core` or
`demos/basic` blocks work on it.

### MAJOR

| | Area | Finding | Status |
|---|---|---|---|
| [M1](#m1) | docs | The README and implementation document described lifecycle behavior the framework no longer has, in code samples meant to be copied | **Fixed** 2026-08-18 |
| [M2](#m2) | api | Nothing lists, tests for, or deletes a stored layout, so user-managed named layouts cannot be built on this API | **Fixed** 2026-08-18 |

### MINOR

| | Area | Finding | Status |
|---|---|---|---|
| [N1](#n1) | api | The identifier rule refuses by throwing, with no way to ask, and user-typed layout names are the next caller | **Open** |
| [N2](#n2) | api | A user-visible layout name is not usable as a storage identifier, and nothing says who converts one to the other | **Decided** 2026-08-18; see below |
| [N3](#n3) | api | The session layout shares one namespace with the layouts users will name | **Open** |
| [N4](#n4) | api | No side channel for application data, which JIDE applications commonly rely on | **Open** |
| [N5](#n5) | docs | The implementation document's restorer sample had its first two arguments transposed | **Fixed** 2026-08-18 |
| [N6](#n6) | docs | The README understated what a profile already selects and misstated the actual limit | **Fixed** 2026-08-18 |
| [N7](#n7) | docs | Neither document mentioned closing a saver or a restorer, which is how listeners and storage are released | **Fixed** 2026-08-18 |

### NIT

| | Area | Finding | Status |
|---|---|---|---|
| [T1](#t1) | docs | British spellings in comments and documents | **Fixed** 2026-08-18 |
| [T2](#t2) | code | Two comments referred to the process that produced them rather than to the code | **Fixed** 2026-08-18 |
| [T3](#t3) | docs | The startup diagram showed a saver auto-saving from its constructor | **Fixed** 2026-08-18 |
| [T4](#t4) | docs | The storage extension example omitted the three conventions a storage implementation has to follow | **Fixed** 2026-08-18 |

Every identifier in the tables links to that finding's own section. The anchors are
explicit rather than derived from the heading text, matching the other documents in
this series.

---

## MAJOR

### <a id="m1"></a>M1. The README and implementation document described lifecycle behavior the framework no longer has

`README.md`, `docs/persistence/docking-layout-persistence.md`

Three descriptions had fallen behind the code, and all three appeared as samples an
application would copy:

- **Auto-save.** Both documents said `AbstractAutoCloseableLayoutSaver` enables
  automatic saving when it is constructed. It deliberately does not: starting a
  scheduler from a constructor published a partly-built object to a scheduler thread
  and to every `Bento` event bus, so arming moved to `startAutoSave(...)`, which the
  persistence provider calls once construction is complete.
- **Provider initialization.** Both showed a `Platform.runLater(...)` block filling a
  `DockableState` map from a provider constructor. That leaves the map empty until
  the queued task runs, so every lookup depends on the order in which JavaFX drains
  its queue. The demo builds the states on first use instead, which is where both
  callers already are: the JavaFX Application Thread.
- **Saving and restoring.** The README's save sample obtained a saver inside the
  close handler and abandoned it, and its restore sample abandoned the restorer.
  Abandoning a saver leaves a `DockEventListener` registered on every `Bento` and a
  scheduler running; abandoning a restorer leaves the `LayoutStorage` it owns
  unreleased.

All three are corrected, and the surrounding prose now says why each shape is what
it is rather than only what to type. The README also gained the ordering constraint
the samples imply: obtain the saver after the layout is applied, because a capture
reads the root branches that have a `Scene`.

### <a id="m2"></a>M2. Nothing lists, tests for, or deletes a stored layout

`api/provider/LayoutStorageProvider.java`, `api/provider/DockingLayoutPersistenceProvider.java`

A layout is addressed by identifier, and a `LayoutPersistenceProfile` chooses the
codec and the storage destination, so an application can already read and write as
many layouts as it likes. It cannot discover them. Three operations have no home:

- list the layout identifiers a storage destination holds
- report whether one layout is stored, without building a restorer to ask it
- delete a stored layout

The middle one is derivable today, awkwardly, through
`getLayoutStorage(...).exists()`. The first and third are not derivable at all, so
an application that lets users manage named layouts has to reach around the API and
list a directory or query a table itself - which also means it has to know which
storage implementation is in use, the one thing the framework exists to hide.

Both bundled implementations could answer all three cheaply. File storage keeps one
path component per layout, so listing is a directory scan filtered by extension and
deleting is a file delete. Database storage keys rows by layout and codec
identifier, so listing is a `select` of one column and deleting is a `delete` on the
composite key.

This is a major rather than a minor because it is the difference between the stated
plan being buildable and not. A migrating application will expect it as well: JIDE
sells "List available layouts" and "Instantly switch layout" as features on its
product page.

**Implemented.** `LayoutStorageProvider` gained the three operations as `default`
methods, so a storage implementation that cannot enumerate or delete stays valid:

```java
default List<String> getLayoutIdentifiers(String codecIdentifier);
default boolean isLayoutStored(String layoutIdentifier, String codecIdentifier);
default boolean deleteLayout(String layoutIdentifier, String codecIdentifier);
```

Only `isLayoutStored` has a default that answers usefully, by asking the storage
itself. The other two report "nothing" and "nothing removed", and both bundled
implementations override all three: file storage lists the directory entries ending
in the codec's extension and deletes one file, and database storage selects the
layout identifiers on rows with a payload and deletes by composite key. Both skip
empty content, which is the rule `LayoutStorage.exists()` already applies.

`DockingLayoutPersistenceProvider` exposes them to applications as
`getStoredLayoutIdentifiers`, `isLayoutStored` and `deleteLayout`, each taking a
profile so that codec and storage selection stays in one place. A one-shot
`saveLayout(profile, bentoProvider)` came with them: a named save is one write, not
a session, so it must not arm a five-minute scheduler and register a listener on
every `Bento` that it then has to take down.

Note the layering the display-name decision forces. Identifiers can be listed from
the storage layer alone; display names live inside each layout, so listing them means
decoding, which is why `getLayoutIdentifiers` deals only in identifiers and a
name-aware listing will sit beside it on the persistence provider, where a codec is
available.

Covered by tests against both real destinations - `FileLayoutStorageCatalogIT` and
`DatabaseLayoutStorageProviderIT` - plus provider-level tests that the profile's
storage identifier picks which destination is asked, and `OneShotLayoutSaveFT`, which
pins that one call encodes and writes exactly once and that a call with nothing
attached leaves the stored layout alone.

---

## MINOR

### <a id="n1"></a>N1. The identifier rule refuses by throwing, with no way to ask

`api/storage/LayoutIdentifiers.java`

`requireValid(layoutIdentifier, codecIdentifier)` is the shared rule both storages
apply, and throwing is right for it: the two providers must refuse a pair they
cannot store, and a message naming the parameter and the rule is worth more than a
boolean.

That reasoning assumed identifiers come from application code. Once users name
their own layouts, the same rule meets a text field. An application asking "may I
offer to save this as *Quarterly Review: v2*" wants an answer, not an exception to
catch, and a dialog that validates as the user types cannot afford one per
keystroke.

The predicate is three lines over the existing rule. What matters more is deciding
which of the two shapes the API offers - a boolean, or something that reports which
rule failed so a dialog can say why - because a boolean cannot be widened later
without a second method.

### <a id="n2"></a>N2. A user-visible layout name is not usable as a storage identifier

`storage/file/.../provider/FileLayoutStorageProvider.java`, `api/storage/LayoutIdentifiers.java`

File-backed storage joins the layout identifier and the codec identifier into one
path component, so the identifier is a file name. That is why the shared rule
rejects separators, reserved device names, and characters no filesystem accepts,
and why the pair is bounded at 255 characters together.

None of those constraints are ones an end user should meet. "Sprint 12: UI work" is
a reasonable thing to call a layout and an unreasonable thing to call a file.

**Decided: the framework will carry the display name.** A name goes into the layout
metadata, the identifier is generated from it, and the catalog reports both, so that
no application writes the same mapping twice. Not yet implemented; three consequences
shape how it lands:

- Listing names means decoding each stored layout, while listing identifiers is a
  directory scan or one query. That is why the identifiers already ship on
  `LayoutStorageProvider` and the name-aware listing will be added beside it on
  `DockingLayoutPersistenceProvider`, where a codec is available. The existing
  `getStoredLayoutIdentifiers` keeps its meaning; names are an addition.
- The metadata field is a schema change, so it touches both codecs and the schema
  version.
- Generating an identifier means deciding what happens when two display names reduce
  to the same one.

### <a id="n3"></a>N3. The session layout shares one namespace with the layouts users will name

`demos/persistence/.../BoxApp.java:65`

The demo saves the most recent layout under the identifier `recent`, which is the
right shape - the session layout is just a layout with a well-known name. Once
users name layouts, one of them can choose that name, and the automatic save then
overwrites what the user saved.

Three options: reserve the identifier and publish the constant, give the session
layout its own storage identifier so the two never share a namespace, or leave one
namespace and let the catalog list the session layout like any other so the
collision is at least visible. The last is the smallest and, for a framework, the
most honest; it needs the constant published either way, so an application can
exclude it from a "restore layout" menu.

### <a id="n4"></a>N4. No side channel for application data

`codec/common/.../mapper/dto/LayoutMetadataDto.java`

The framework persists layout structure and nothing else, which is a good boundary:
the codec DTOs stay closed, and an application's own state stays in the
application's own store.

JIDE applications will not arrive expecting that boundary. `LayoutPersistence`
exposes `setSaveCallback` and `setLoadCallback`, each taking a callback whose single
method receives an `org.w3c.dom.Document` and an `Element`, so writing extra data
into the layout file is a documented JIDE technique. Code being migrated may well
use it.

Either outcome is defensible, and the decision is worth making explicitly rather
than by omission:

- **Keep the boundary** and say so in the README, so a migrating application knows
  up front that its extra data needs its own store.
- **Offer a narrow channel**, such as a `Map<String, String>` per layout in the
  metadata. Format-neutral, unlike JIDE's DOM-typed callbacks, and it survives a
  codec swap.

### <a id="n5"></a>N5. The implementation document's restorer sample had its first two arguments transposed

`docs/persistence/docking-layout-persistence.md`

One of the two restorer samples passed `bentoProvider` before the layout identifier,
which does not compile, while the other sample in the same document had them the
right way round. Corrected, and both samples now show the restorer as a
try-with-resources resource.

### <a id="n6"></a>N6. The README understated what a profile already selects

`README.md`

The README told readers the framework "is currently limited to saving and restoring
a single format at a single storage destination". A `LayoutPersistenceProfile`
already chooses a codec and a storage destination per saver and per restorer, so an
application can use several of each.

The note now says what is actually true: a saver and a restorer each work with one
layout in one format at one destination, an application may use several, and the
real gap is the catalog from [M2](#m2).

### <a id="n7"></a>N7. Neither document mentioned closing a saver or a restorer

`README.md`, `docs/persistence/docking-layout-persistence.md`

`LayoutSaver` and `LayoutRestorer` are both `AutoCloseable`, and closing is not
housekeeping: it is what removes the saver's listener from each `Bento`, stops its
scheduler, and releases the `LayoutStorage` the component was handed. Neither
document said so, and the README's samples showed both components being abandoned.

Both now cover it, including the constraint that makes the placement matter: an
application that exits with `System.exit(...)` never runs `Application.stop()`, so
a window's close request handler is the last point at which closing still happens.

---

## NIT

### <a id="t1"></a>T1. British spellings in comments and documents

Fixed in `demos/persistence`, `persistence/api`, `CONTRIBUTING.md`, `README.md`, and
the other documents in this series: *colour*, *recognise*, *initialising*,
*serialised*, *behaviour*, *organised*, *judgement*, *normalisation*, *amongst*, and
*towards* where American English prefers *toward*.

`core` keeps `DockEvent.cancelled`, which is a field and method name rather than
prose, and is out of scope.

### <a id="t2"></a>T2. Two comments referred to the process that produced them

`persistence/api/build.gradle:16`, `persistence/api/src/test/.../DockContainerRootBranchStateBuilderTest.java:19`

One comment ended "survived review here" and one cited an item identifier. Both now
describe the problem without referring to how it was found, which is what a reader
of the code needs.

### <a id="t3"></a>T3. The startup diagram showed a saver auto-saving from its constructor

`docs/persistence/docking-layout-persistence-diagrams.md`

The startup sequence had the saver constructing, auto-saving and writing before the
application had asked for anything, and never showed either component being closed.
It now follows the order an application actually uses - restore, apply, then obtain
the saver and keep it - and shows arming, the interval loop, the explicit save on
close request, and both `close()` calls.

The class diagram gained `exists()` and `close()` on `LayoutStorage`, `close()` on
`LayoutSaver`, and `doesLayoutExist()` and `close()` on `LayoutRestorer`. The
"Applying a DockingLayout" diagram was two arrows; it now shows the per-`BentoLayout`
decision and the fallback when nothing could be applied.

### <a id="t4"></a>T4. The storage extension example omitted the conventions a storage implementation has to follow

`README.md`

The example showed the three methods and nothing about the behavior callers rely
on. Three conventions are now stated with it: closing the output stream is what
stores the layout, `exists()` answers whether there is a layout to read rather than
whether a location is present, and `close()` releases what the storage owns and
nothing it was handed. Each is a property the bundled implementations have and a
new implementation would otherwise have to infer.

---

## <a id="footprint-compared-with-jide"></a>Footprint compared with JIDE - MEASURED

Measured against the jars in the Gradle cache, JIDE 3.7.10: `jide-dock` (426 KB),
`jide-common` (1.7 MB), `jide-components`.

**In JIDE, persistence is not a separable surface.** An application holds a
`com.jidesoft.docking.DockingManager`, an interface with 259 abstract methods, and
44 of those come from `com.jidesoft.swing.LayoutPersistence`, which it extends. A
reference held to arrange docking is the same reference that saves layouts, chooses
the format, chooses the directory, and enumerates what was saved.

```
javap com.jidesoft.docking.DockingManager      -> 259 abstract methods
javap com.jidesoft.swing.LayoutPersistence     ->  44 abstract methods
javap com.jidesoft.swing.RootPanePersistence   ->   1 abstract method
```

**In BentoFX, an application saving and restoring one layout touches six types and
about a dozen methods.** Counted over the built classes:

| Type | Public methods | Needed for |
|---|---|---|
| `DockingLayoutPersistence` | 2 | finding the provider |
| `DockingLayoutPersistenceProvider` | 5 | obtaining a saver and a restorer |
| `LayoutSaver` | 3 | saving, closing |
| `LayoutRestorer` | 4 | asking, restoring, closing |
| `BentoProvider` | 4 | telling the framework which `Bento`s exist |
| `DockableStateProvider` | 2 | rebuilding dockable content |

`LayoutStorage` (5) and `LayoutCodec` (4) are touched only when writing a new
storage destination or format. The exported packages hold 28 top-level types in
total, most of them the `*State` objects and their builders, which an application
meets only if it inspects persisted state itself.

**Where the two differ in kind, not just in size:**

| Concern | JIDE | BentoFX |
|---|---|---|
| Choosing the format | `setXmlFormat(boolean)`, `setXmlEncoding(String)` on the docking manager | a runtime dependency, or a codec identifier in a profile |
| Choosing the destination | `setUsePref(boolean)`, `setLayoutDirectory(String)`, `saveLayoutDataToFile(String)` | a runtime dependency, or a storage identifier in a profile |
| A new destination or format | not an extension point | implement two interfaces of 3-5 methods and register a service provider |
| Application data in the layout | `setSaveCallback`/`setLoadCallback`, typed to `org.w3c.dom.Document` | not offered - see [N4](#n4) |
| Naming layouts | `saveLayoutDataAs(String)`, `loadLayoutDataFrom(String)` | a layout identifier per saver and restorer, plus a one-shot `saveLayout(profile, bentoProvider)` |
| Listing layouts | `getAvailableLayouts()` | `getStoredLayoutIdentifiers(profile)` |
| Testing for one layout | `isLayoutAvailable(String)` | `isLayoutStored(profile)` |
| Deleting a layout | `removeLayout(String)` | `deleteLayout(profile)` |
| Restoring the default | `resetToDefault()` | the application's own default layout supplier, which the restorer falls back to |
| Partial restore | `setUseFrameState(boolean)`, `setUseFrameBounds(boolean)` | not offered; persisted state is applied as a whole |
| Version handling | `getVersion()`/`setVersion(short)`, `isLayoutDataVersionValid(String)` | a schema version in the layout metadata, with no migration step yet |

The four rows JIDE's product page advertises - "Load and save layout using javax pref
package", "Load and save layout using file", "List available layouts", "Instantly
switch layout" - are now all covered, the last two by the operations added in this
pass. What remains uncovered is deliberate: no partial restore, no application-data
channel, and no migration step, each of which is a decision recorded above rather
than an omission.

One migration note that is worth more than any API change: **applications should
depend on `DockingLayoutPersistenceProvider`, not on a docking manager.**
JIDE-shaped Swing code reaches for the manager for everything, so the cheapest
preparation available today is to put an application-owned interface in front of
JIDE's persistence calls on the Swing side. That interface then maps onto BentoFX's
provider almost method for method, and the JavaFX migration stops being a
find-and-replace across every window class.

## <a id="api-changes-worth-making-now"></a>API changes

**Done.** The four operations named under [M2](#m2) now exist: three on
`LayoutStorageProvider` as defaults, overridden by both bundled implementations, and
their application-facing counterparts plus a one-shot `saveLayout` on
`DockingLayoutPersistenceProvider`. Together they map onto what a JIDE application
calls today:

| JIDE | BentoFX |
|---|---|
| `saveLayoutData()` | `getLayoutSaver(...)`, which auto-saves for the session |
| `saveLayoutDataAs(String)` | `saveLayout(profile, bentoProvider)` |
| `loadLayoutData()`, `loadLayoutDataFrom(String)` | `getLayoutRestorer(profile, ...)` then `restoreLayout(...)` |
| `getAvailableLayouts()` | `getStoredLayoutIdentifiers(profile)` |
| `isLayoutAvailable(String)` | `isLayoutStored(profile)` |
| `removeLayout(String)` | `deleteLayout(profile)` |
| `resetToDefault()` | the application's default layout supplier, which the restorer already falls back to |

**Still to decide, in the order they will be needed.**

1. **Display names ([N2](#n2)) - decided, not built.** The framework will carry the
   name and generate the identifier. It is a schema change, so it is a piece of work
   rather than a signature.
2. **The identifier predicate ([N1](#n1)).** Needed by the first dialog that
   validates a typed name; decide then whether it reports which rule failed.
3. **The session-layout namespace ([N3](#n3)).** `getStoredLayoutIdentifiers` reports
   the session layout like any other, so an application filters it out today. Worth
   reserving the identifier before users can pick it.
4. **The application-data boundary ([N4](#n4)).** One line of documentation, or a
   narrow format-neutral channel. Cheaper to settle than to change later.

## <a id="multiple-layouts"></a>Multiple layouts, storages, and codecs

The framework is closer to the goal than it looks, because the addressing is already
there. `LayoutPersistenceProfile` carries the layout identifier, the codec
identifier and the storage identifier, and the storage provider is asked for a
storage per layout and codec pair, so "the most recent layout in JSON on disk" and
"a user's custom layout in XML on disk" are two profiles, not two frameworks.

What a runtime layout manager needs on top of that:

- the catalog operations, to populate a menu and to delete an entry - **now present**
- a one-shot save, so that "Save layout as..." does not arm a session-long saver -
  **now present**
- display names, because the menu shows one and storage uses the other - decided,
  still to build
- one reserved or separated identifier for the session layout - still to decide

What it does not need, and should not grow: a second saver lifecycle. The session
saver stays exactly as it is - obtained at startup, auto-saving, closed on exit -
and named layouts are one-shot writes and reads alongside it. Keeping those two
paths distinct is what stops a "save as" from quietly becoming the layout that
auto-save then overwrites.

One consequence worth planning for: restoring a different layout while the
application is running is not the same operation as restoring one at startup. The
containers a restorer hands back are unattached, and the application has to replace
the `Scene`'s root and re-show any drag/drop stages, while the session saver is
still listening. An application should suspend auto-save around a layout switch, or
the switch itself will look like a layout change worth persisting.

## <a id="documentation-updated"></a>Documentation and diagrams updated

- `README.md` - the limitation note, the provider-initialization sample, the save
  and restore samples, the apply-with-fallback sample, the startup flow, the
  auto-save description, the storage conventions, the demo comparison table, and one
  heading level that disagreed with the table of contents.
- `docs/persistence/docking-layout-persistence.md` - the transposed restorer
  arguments, the provider-initialization sample, the auto-save description, the
  fourth fallback case, the demo comparison table, and the future-capabilities
  section, which now states the two decisions that come with named layouts.
- `docs/persistence/docking-layout-persistence-diagrams.md` - the class diagram's
  missing lifecycle methods, the startup sequence, and the apply sequence.

The catalog operations were documented as they were added: a "Managing Several
Layouts" section in the README with the four calls and the three things worth knowing
about them, a table of the operations in the implementation document, and the two
providers in the class diagram.

The three documents have different audiences and now stay in their lanes: the README
tells a library user how to use the framework, the implementation document explains
how it works and what an application must supply, and the diagrams carry the
sequences. `demos/basic` remains the how-to for building a layout, and
`demos/persistence` mirrors it while showing only what persistence adds.

## <a id="withdrawn"></a>Withdrawn

**W1. The demos do not disagree about `DockableProperties`.** The persistence demo
now carries icon shape and color on that enum while the basic demo passes the same
two integers positionally, which reads like the two demos drifting apart. It is not
drift: the basic demo has no state provider to drive, so it has nothing to move onto
an enum, and the persistence demo builds twelve states from one loop precisely
because it does. The mirroring the two demos are meant to preserve is the layout
they build and the order they build it in, and that is unchanged.

**W2. `LayoutPersistenceProfile` does not need a per-layout storage directory.**
JIDE has `setLayoutDirectory(String)`, and its absence here looked like a gap for
applications that keep user layouts somewhere other than the default. It is not:
the directory is the storage implementation's business, and an application that
needs a second location registers a second `LayoutStorageProvider` with its own
identifier and names it in a profile. That is the same mechanism as choosing between
file and database storage, which is why no new API is warranted.
