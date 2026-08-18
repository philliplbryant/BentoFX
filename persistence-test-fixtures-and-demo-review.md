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

**No blockers, two majors, seven minors, four nits.** Nine are fixed - every
documentation and consistency defect - and six are recommendations that change the
public API, left open because they are decisions rather than repairs.

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
three operations a layout catalog needs** ([M2](#m2)). Named layouts are the next
feature, JIDE advertises the same capability as two of its selling points, and the
current API cannot express either without an application reaching around it.

### BLOCKER

None. Nothing in the framework loses a layout, and nothing in `core` or
`demos/basic` blocks work on it.

### MAJOR

| | Area | Finding | Status |
|---|---|---|---|
| [M1](#m1) | docs | The README and implementation document described lifecycle behavior the framework no longer has, in code samples meant to be copied | **Fixed** 2026-08-18 |
| [M2](#m2) | api | Nothing lists, tests for, or deletes a stored layout, so user-managed named layouts cannot be built on this API | **Open** |

### MINOR

| | Area | Finding | Status |
|---|---|---|---|
| [N1](#n1) | api | The identifier rule refuses by throwing, with no way to ask, and user-typed layout names are the next caller | **Open** |
| [N2](#n2) | api | A user-visible layout name is not usable as a storage identifier, and nothing says who converts one to the other | **Open** |
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
plan being buildable and not. See
[API changes worth making now](#api-changes-worth-making-now) for the shape
recommended, and [Footprint compared with JIDE](#footprint-compared-with-jide) for
why a migrating application will expect it: JIDE sells "List available layouts" and
"Instantly switch layout" as features on its product page.

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

Two ways out, and the choice belongs to whoever owns the API rather than to each
application:

- **Applications map display names to identifiers.** The framework stays smaller,
  and every application writes the same slug function.
- **The framework stores a display name with the layout and generates the
  identifier.** Applications get names for free, at the cost of a field in the
  layout metadata and a lookup to resolve a name back to an identifier - which the
  catalog in [M2](#m2) would have to return, making the two changes one change.

Worth settling before the catalog is built, because the catalog's return type
depends on the answer: a list of identifiers, or a list of name-and-identifier
pairs.

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
| Naming layouts | `saveLayoutDataAs(String)`, `loadLayoutDataFrom(String)` | a layout identifier per saver and restorer |
| Listing layouts | `getAvailableLayouts()` | not offered - see [M2](#m2) |
| Testing for one layout | `isLayoutAvailable(String)` | `LayoutRestorer.doesLayoutExist()`, after building a restorer |
| Deleting a layout | `removeLayout(String)` | not offered - see [M2](#m2) |
| Restoring the default | `resetToDefault()` | the application's own default layout supplier, which the restorer falls back to |
| Partial restore | `setUseFrameState(boolean)`, `setUseFrameBounds(boolean)` | not offered; persisted state is applied as a whole |
| Version handling | `getVersion()`/`setVersion(short)`, `isLayoutDataVersionValid(String)` | a schema version in the layout metadata, with no migration step yet |

BentoFX is ahead on the first three rows and behind on rows five through eight,
which are exactly the rows the JIDE product page advertises: "Load and save layout
using javax pref package", "Load and save layout using file", "List available
layouts", "Instantly switch layout".

One migration note that is worth more than any API change: **applications should
depend on `DockingLayoutPersistenceProvider`, not on a docking manager.**
JIDE-shaped Swing code reaches for the manager for everything, so the cheapest
preparation available today is to put an application-owned interface in front of
JIDE's persistence calls on the Swing side. That interface then maps onto BentoFX's
provider almost method for method, and the JavaFX migration stops being a
find-and-replace across every window class.

## <a id="api-changes-worth-making-now"></a>API changes worth making now

Ordered by cost. The first two are additive and cover the stated plan; the rest are
decisions that get more expensive to reverse once applications depend on them.

**1. Three capability methods on `LayoutStorageProvider`, with defaults.**

```java
default List<String> getLayoutIdentifiers(String codecIdentifier);
default boolean isLayoutStored(String layoutIdentifier, String codecIdentifier);
default boolean deleteLayout(String layoutIdentifier, String codecIdentifier);
```

Defaults keep every existing storage implementation compiling and let one that
cannot enumerate say so. Both bundled implementations override all three cheaply.

**2. An application-facing view of them on `DockingLayoutPersistenceProvider`.**
Applications should not repeat the codec and storage selection logic the provider
already owns, so the catalog belongs beside `getLayoutSaver` and
`getLayoutRestorer` - either three methods taking a `LayoutPersistenceProfile`, or a
small `LayoutCatalog` type obtained from one. The second reads better at the call
site and gives the three operations one place to grow.

Either way the result maps onto JIDE's `getAvailableLayouts()`,
`isLayoutAvailable(String)` and `removeLayout(String)` one for one, which is what
makes a migration mechanical.

**3. A one-shot save, for "save as".** Today a named save means obtaining a
`LayoutSaver`, which arrives with a five-minute scheduler armed and a listener
registered on every `Bento`, using it once, and closing it. That works and invites
the leak. A `saveLayout(profile, bentoProvider)` on the persistence provider, which
opens storage, writes and closes, says what a "save as" is: one write, no session
lifetime. JIDE's `saveLayoutDataAs(String)` is the same call.

**4. Decide the display-name question ([N2](#n2)) before building the catalog**, since
it decides what the catalog returns.

**5. Add the identifier predicate ([N1](#n1)) when the first dialog needs it**, and
decide then whether it reports a reason.

**6. Settle the session-layout namespace ([N3](#n3)) and the application-data
boundary ([N4](#n4)).** Both are one-line decisions now and breaking changes later.

## <a id="multiple-layouts"></a>Multiple layouts, storages, and codecs

The framework is closer to the goal than it looks, because the addressing is already
there. `LayoutPersistenceProfile` carries the layout identifier, the codec
identifier and the storage identifier, and the storage provider is asked for a
storage per layout and codec pair, so "the most recent layout in JSON on disk" and
"a user's custom layout in XML on disk" are two profiles, not two frameworks.

What a runtime layout manager needs on top of that:

- the catalog operations above, to populate a menu and to delete an entry
- a one-shot save, so that "Save layout as..." does not arm a session-long saver
- a decision about names versus identifiers, because the menu shows one and storage
  uses the other
- one reserved or separated identifier for the session layout

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
