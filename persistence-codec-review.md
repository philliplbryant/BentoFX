# Review: `persistence/codec`

Scope: all of `persistence/codec/common`, `persistence/codec/json`, and
`persistence/codec/xml` (main sources, module descriptors, and build files), plus
the tests those three modules own and the one shared fixture they all depend on,
`persistence/test-fixtures/.../codec/dto/SampleDockingLayoutDtoFactory.java`.
`persistence/api` and `core/` were read only where the codec's correctness
depends on their contracts: what the captor puts into a `*State`, what the
restorer takes back out, and what `LayoutCodec.decode` promises its callers.

Line numbers refer to the files as they stand on `enhancement/issue-13` at
`ffb48eb`.

Nothing is open. Every finding is either fixed or closed as won't fix, the three
won't-fixes being M1, M10 and T7. A status cell carries the date and, once the fix
is committed, the commit that closed it; the six minors dated 2026-08-16 with no
hash are in the working tree.

**N10 removed the last Jackson type from the mix-in registries.** Both now offer
only a `Map<Class<?>, Class<?>>` of DTO to mix-in, and each codec registers those
on the mapper it built. Nothing outside a codec names an `ObjectMapper`, so the
type no longer appears in a signature reachable from code that cannot see
jackson-databind, and the cast [N6](#n6) complained about has nowhere left to be.

**N2 is per-property, not per-class.** `NON_EMPTY` on each list keeps an empty root
branch from writing a line for every collection it does not have. Setting it on the
mix-in classes instead would also have suppressed empty strings, which is a
different decision about a different kind of field.

**N5 took slf4j with it.** The try/catch around the branch orientation could not
fire once the DTO field became a typed enum, and its warning was the only thing in
`codec.common` that logged, so the module no longer requires slf4j or declares the
dependency.

**N7 aligned a test with the codec.** `@JsonRootName` did nothing while root
wrapping was off, and the only mapper that enabled root wrapping was the
compatibility test's own - so the test was checking a document shape the codec
never produces. The annotation is gone and the test now configures its mapper the
way the codec does, which is what makes the JSON document's top level part of what
that test pins.

T1 needed nothing of its own: the Javadoc and the `final` parameter arrived with
M6, which rewrote that method. T4 is why `DockContainerDto` is now sealed - with
both hierarchies sealed, all three dispatch sites are exhaustive pattern switches
the compiler checks, rather than one switch and two chains of `instanceof`.

**T7 is closed as won't fix.** Every container XML mix-in now states its own
`identifier` and `pruneWhenEmpty` attributes, the branch one included, so all four
read alike and none depends on inheritance to describe what a container writes.
Removing them from the leaf was measured first and changed nothing, attribute
order included, so this is a readability choice rather than a behavioral one. The
mix-in that cannot avoid declaring them is the root branch's, because
`DockContainerRootBranchDto` is the one container DTO outside the
`DockContainerDto` hierarchy; folding it in would give the root branch a type id
and change the file format, so it stays outside.

**M1 is closed as won't fix**, because it cannot be done by annotation and the
alternative costs more than the nesting does. The document no longer reads
`<branch><branch/></branch>`; since B1 it reads `<children><leaf/></children>`
per child, because Jackson XML names each list item after its property and the
`WRAPPER_OBJECT` type id on `DockContainerDto` then writes `<leaf>` or `<branch>`
inside it. Turning the element wrapper on was measured and adds a third level
rather than removing one. Losing the extra level means moving the type id out of
an element and into a `type` attribute, which drops `<leaf>` and `<branch>` as
element names - the names that make the document readable, and the ones the
element-name test asserts. The nesting is verbose, not lossy: the whole-layout
round-trip added for [M4](#m4) passes through it unchanged.

**M10 is closed as won't fix**, because the only mechanism that narrows an export
is to name the modules it goes to, and `codec.common` is not allowed to know that
the JSON and XML codecs exist. Qualifying the exports was tried and works - a
module requiring only `codec.json` still round-trips a layout on a real module
path, and can no longer compile against the DTOs - but it puts each codec's
module name in the shared module's descriptor, which inverts the dependency the
module boundary exists to enforce, and it costs a `module not found` warning per
name on every compile because neither codec can be on that module's compile path
without a cycle. An unqualified export of a package named `impl` is the weaker
guarantee, and it is the one that keeps the direction right.

One part of the attempt is worth keeping and stays: the DTO package is now opened
unqualified, so a codec's object mapper can reflect over the DTOs in a modular
runtime without the module naming a serialization library. That replaced the dead
`opens ... to org.eclipse.persistence.moxy`, since a package cannot have both an
unqualified and a qualified `opens`, which is how [M9](#m9) lost its first part.

**M9's remaining three parts went together.** Nothing in `codec.common` uses MOXy
or jakarta.annotation, and no build puts MOXy on a path, so the two `compileOnly`
dependencies and the `requires static jakarta.annotation` described a mapper that
was never wired up. Both dependencies were the last reference to their catalog
entries, so those went too, and the mapper's Javadoc no longer offers the DTOs to
JAXB.

**M6 covered two paths, and they turned out to be different problems.** A
`DockableState`'s title, tooltip text, drag group mask and closability are
restored by `DockingLayoutStateRestorer` but were not carried by `DockableDto`,
so a saved layout came back with its tab titles and tooltips gone. Those four are
now in the DTO, the mapper and both mix-ins. The dockables a branch holds
directly were never a codec gap: `BentoLayoutStateCaptor` only ever hands
dockables to a leaf and the restorer only ever reads them from one, so nothing in
the persistence path produced or consumed them, and a DTO field would have been a
wire contract for state that cannot arise. `addChildDockableState` is therefore
gone from `DockContainerBranchStateBuilder` and
`DockContainerRootBranchStateBuilder`, so the state model no longer offers what
the format cannot keep.

## Status

**Three blockers, ten majors, twelve minors, eleven nits.** The three blockers
are independent of one another: one loses layout data on an ordinary save, one
breaks the declared exception contract on decode, one makes the published
artifacts unresolvable.

Everything marked **Measured** was settled by running the code, not by reading
it. The probes were temporary tests in each codec module, run once and deleted;
each measurement below quotes the output it came from. Four candidate findings
did not survive that step and are recorded at the end under
[Withdrawn](#withdrawn) rather than dropped, because two of them look correct
from the source alone and would be raised again by the next reader.

The single most useful change is not any one fix: it is **a round-trip test that
compares a whole layout instead of three identifiers** ([M4](#m4)). B1, M1 and
M3 all shipped through a suite that cannot see them, and any fix to B1 will need
that test before it can be trusted.

That test now exists in both codec modules, comparing a decoded layout against
the states a fixture built rather than against a re-encoding of itself, which
would hide a symmetric loss. It is bounded by [M6](#m6): the two paths no DTO
carries are the ones the fixture leaves unset, so the comparison covers
everything the codec claims to persist and nothing it does not.

### BLOCKER

| | Finding | Status |
|---|---|---|
| [B1](#b1) | A root branch encodes at most one leaf child; the rest are silently dropped and the survivors reordered | **Fixed** 2026-08-15 (`afbd3c4`) |
| [B2](#b2) | `decode` lets unchecked exceptions escape, so a missing identifier surfaces as a bare `NullPointerException` | **Fixed** 2026-08-15 (`fad5954`) |
| [B3](#b3) | `persistence.codec.common` publishes javafx-graphics at version `Optional[21.0.12]` | **Fixed** 2026-08-15 (`2de813f`) |

### MAJOR

| | Finding | Status |
|---|---|---|
| [M1](#m1) | Every polymorphic container is written twice-nested in XML: `<branch><branch/></branch>` | **Won't fix** 2026-08-15; see below |
| [M2](#m2) | One concept, three wire names: a root's `branches` plus `leaf`, a nested branch's `children` | **Fixed** 2026-08-15 (`afbd3c4`) |
| [M3](#m3) | A JSON mix-in field matches no DTO field, so its `@JsonProperty` is inert | **Fixed** 2026-08-15 (`11a3859`) |
| [M4](#m4) | JSON has no round-trip test; XML's asserts metadata and three identifiers | **Fixed** 2026-08-15 (`11a3859`) |
| [M5](#m5) | The shared fixture aliases one leaf into two parents, encoding a layout no capture can produce | **Fixed** 2026-08-15 (`1a9a2d2`) |
| [M6](#m6) | DTO coverage is narrower than the state model on two public paths | **Fixed** 2026-08-15 (`1a9a2d2`) |
| [M7](#m7) | `FAIL_ON_UNKNOWN_PROPERTIES` is disabled in both codecs, for a compatibility case the version gate already rejects | **Fixed** 2026-08-15 (`1a9a2d2`) |
| [M8](#m8) | `codec.common` requires `javafx.controls`, uses none of it, and its build file declares `javafx.graphics` | **Fixed** 2026-08-15 (`eb4a579`) |
| [M9](#m9) | Dead MOXy/JAXB scaffolding: two `compileOnly` dependencies, a `requires static`, and an `opens` to an absent module | **Fixed** 2026-08-16 (`eb4a579`, `e884304`) |
| [M10](#m10) | `codec.common` exports both `impl` packages unqualified | **Won't fix** 2026-08-15; see below |

### MINOR

| | Finding | Status |
|---|---|---|
| [N1](#n1) | Divider positions are emitted in map-iteration order, so encoded files are not stable | **Fixed** 2026-08-16 (`b6b5f9a`) |
| [N2](#n2) | `NON_NULL` on never-null lists writes every empty collection to the file | **Fixed** 2026-08-16 |
| [N3](#n3) | A missing identifier is replaced by the element name, so anonymous siblings collide | **Fixed** 2026-08-16 (`b6b5f9a`) |
| [N4](#n4) | No `requireNonNull` in the mapper carries a message; the observed NPE message is `null` | **Fixed** 2026-08-16 |
| [N5](#n5) | The orientation try/catch is unreachable now the DTO field is a typed enum | **Fixed** 2026-08-16 (`b6b5f9a`) |
| [N6](#n6) | `XmlLayoutCodec` casts its own mapper back to `XmlMapper` | **Fixed** 2026-08-16 (`b6b5f9a`) |
| [N7](#n7) | `@JsonRootName` is inert with `WRAP_ROOT_VALUE` disabled | **Fixed** 2026-08-16 (`b6b5f9a`) |
| [N8](#n8) | `@JsonTypeName` on the root-branch mix-in, which is not in the polymorphic hierarchy | **Fixed** 2026-08-16 (`b6b5f9a`) |
| [N9](#n9) | jackson-annotations is `compileOnly` but read reflectively at runtime; the two modules declare it differently | **Fixed** 2026-08-16 |
| [N10](#n10) | `ObjectMapperMixins.registerAll` exposes `ObjectMapper` from a `runtime`-scoped dependency | **Fixed** 2026-08-16 |
| [N11](#n11) | `encode` catches `Exception` while `decode` catches `IOException`, in the same file | **Fixed** 2026-08-16 |
| [N12](#n12) | Four `toDto` overloads skip the `requireNonNull` their siblings have | **Fixed** 2026-08-16 |

### NIT

| | Finding | Status |
|---|---|---|
| [T1](#t1) | `toDto(DockableState)` has no Javadoc and no `final` parameter | **Fixed** 2026-08-15 (`1a9a2d2`) |
| [T2](#t2) | `ElementNames` is not `final`, and its constants sit after its constructor | **Fixed** 2026-08-16 (`76bf756`) |
| [T3](#t3) | Mixed tabs and spaces inside `BentoStateMapper` and the ITP | **Fixed** 2026-08-16 (`76bf756`) |
| [T4](#t4) | Three different forms of the same container dispatch in one class | **Fixed** 2026-08-16 (`76bf756`) |
| [T5](#t5) | JSON mix-ins are `public abstract`; the XML ones are package-private | **Fixed** 2026-08-16 (`76bf756`) |
| [T6](#t6) | `XmlMapperMixins` Javadoc has no `@author` | **Fixed** 2026-08-16 (`76bf756`) |
| [T7](#t7) | The leaf XML mix-in re-declares two fields the base mix-in already covers | **Won't fix** 2026-08-16 (`76bf756`); each container mix-in states its own attributes |
| [T8](#t8) | Stale `// "HORIZONTAL" or "VERTICAL"` comment on a typed enum field | **Fixed** 2026-08-16 (`76bf756`) |
| [T9](#t9) | Test class modifiers differ across the two codec modules | **Fixed** 2026-08-16 (`76bf756`) |
| [T10](#t10) | The XML element-name test asserts `<branch` as a prefix, so it passes on the doubled form | **Fixed** 2026-08-16 (`76bf756`) |
| [T11](#t11) | The JSON mix-ins package-info is missing the `@NullMarked` its XML counterpart has | **Fixed** 2026-08-16 (`76bf756`) |

Every identifier in the four tables links to that finding's own section. The
anchors are explicit rather than derived from the heading text, matching
`persistence-api-review.md`, so that appending an outcome to a heading later
does not break the link.

---
