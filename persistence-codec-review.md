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

All three blockers are fixed, along with M2 through M8. M1 and M10 are closed as
won't fix, and M9 has lost one of its four parts. Every other finding below is
open. A fixed status carries the date, and the commit that closed it once that fix
is committed. M3 through M9 are verified in the working tree, not yet committed.

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
unqualified and a qualified `opens`.

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
| [M3](#m3) | A JSON mix-in field matches no DTO field, so its `@JsonProperty` is inert | **Fixed** 2026-08-15 |
| [M4](#m4) | JSON has no round-trip test; XML's asserts metadata and three identifiers | **Fixed** 2026-08-15 |
| [M5](#m5) | The shared fixture aliases one leaf into two parents, encoding a layout no capture can produce | **Fixed** 2026-08-15 |
| [M6](#m6) | DTO coverage is narrower than the state model on two public paths | **Fixed** 2026-08-15 |
| [M7](#m7) | `FAIL_ON_UNKNOWN_PROPERTIES` is disabled in both codecs, for a compatibility case the version gate already rejects | **Fixed** 2026-08-15 |
| [M8](#m8) | `codec.common` requires `javafx.controls`, uses none of it, and its build file declares `javafx.graphics` | **Fixed** 2026-08-15 |
| [M9](#m9) | Dead MOXy/JAXB scaffolding: two `compileOnly` dependencies, a `requires static`, and an `opens` to an absent module | **Open**; the `opens` half went with [M10](#m10) |
| [M10](#m10) | `codec.common` exports both `impl` packages unqualified | **Won't fix** 2026-08-15; see below |

### MINOR

| | Finding | Status |
|---|---|---|
| [N1](#n1) | Divider positions are emitted in map-iteration order, so encoded files are not stable | **Open** |
| [N2](#n2) | `NON_NULL` on never-null lists writes every empty collection to the file | **Open** |
| [N3](#n3) | A missing identifier is replaced by the element name, so anonymous siblings collide | **Open** |
| [N4](#n4) | No `requireNonNull` in the mapper carries a message; the observed NPE message is `null` | **Open** |
| [N5](#n5) | The orientation try/catch is unreachable now the DTO field is a typed enum | **Open** |
| [N6](#n6) | `XmlLayoutCodec` casts its own mapper back to `XmlMapper` | **Open** |
| [N7](#n7) | `@JsonRootName` is inert with `WRAP_ROOT_VALUE` disabled | **Open** |
| [N8](#n8) | `@JsonTypeName` on the root-branch mix-in, which is not in the polymorphic hierarchy | **Open** |
| [N9](#n9) | jackson-annotations is `compileOnly` but read reflectively at runtime; the two modules declare it differently | **Open** |
| [N10](#n10) | `ObjectMapperMixins.registerAll` exposes `ObjectMapper` from a `runtime`-scoped dependency | **Open** |
| [N11](#n11) | `encode` catches `Exception` while `decode` catches `IOException`, in the same file | **Open** |
| [N12](#n12) | Four `toDto` overloads skip the `requireNonNull` their siblings have | **Open** |

### NIT

| | Finding | Status |
|---|---|---|
| [T1](#t1) | `toDto(DockableState)` has no Javadoc and no `final` parameter | **Open** |
| [T2](#t2) | `ElementNames` is not `final`, and its constants sit after its constructor | **Open** |
| [T3](#t3) | Mixed tabs and spaces inside `BentoStateMapper` and the ITP | **Open** |
| [T4](#t4) | Three different forms of the same container dispatch in one class | **Open** |
| [T5](#t5) | JSON mix-ins are `public abstract`; the XML ones are package-private | **Open** |
| [T6](#t6) | `XmlMapperMixins` Javadoc has no `@author` | **Open** |
| [T7](#t7) | The leaf XML mix-in re-declares two fields the base mix-in already covers | **Open** |
| [T8](#t8) | Stale `// "HORIZONTAL" or "VERTICAL"` comment on a typed enum field | **Open** |
| [T9](#t9) | Test class modifiers differ across the two codec modules | **Open** |
| [T10](#t10) | The XML element-name test asserts `<branch` as a prefix, so it passes on the doubled form | **Open** |
| [T11](#t11) | The JSON mix-ins package-info is missing the `@NullMarked` its XML counterpart has | **Open** |

Every identifier in the four tables links to that finding's own section. The
anchors are explicit rather than derived from the heading text, matching
`persistence-api-review.md`, so that appending an outcome to a heading later
does not break the link.

---
