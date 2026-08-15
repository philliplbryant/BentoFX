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

All three blockers are fixed; every other finding below is open. A fixed status
carries the date, and the commit that closed it once that fix is committed. B3
is fixed and verified in the working tree, not yet committed.

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

### BLOCKER

| | Finding | Status |
|---|---|---|
| [B1](#b1) | A root branch encodes at most one leaf child; the rest are silently dropped and the survivors reordered | **Fixed** 2026-08-15 (`afbd3c4`) |
| [B2](#b2) | `decode` lets unchecked exceptions escape, so a missing identifier surfaces as a bare `NullPointerException` | **Fixed** 2026-08-15 (`fad5954`) |
| [B3](#b3) | `persistence.codec.common` publishes javafx-graphics at version `Optional[21.0.12]` | **Fixed** 2026-08-15 |

### MAJOR

| | Finding | Status |
|---|---|---|
| [M1](#m1) | Every polymorphic container is written twice-nested in XML: `<branch><branch/></branch>` | **Open** |
| [M2](#m2) | One concept, three wire names: a root's `branches` plus `leaf`, a nested branch's `children` | **Open** |
| [M3](#m3) | A JSON mix-in field matches no DTO field, so its `@JsonProperty` is inert | **Open** |
| [M4](#m4) | JSON has no round-trip test; XML's asserts metadata and three identifiers | **Open** |
| [M5](#m5) | The shared fixture aliases one leaf into two parents, encoding a layout no capture can produce | **Open** |
| [M6](#m6) | DTO coverage is narrower than the state model on two public paths | **Open** |
| [M7](#m7) | `FAIL_ON_UNKNOWN_PROPERTIES` is disabled in both codecs, for a compatibility case the version gate already rejects | **Open** |
| [M8](#m8) | `codec.common` requires `javafx.controls`, uses none of it, and its build file declares `javafx.graphics` | **Open** |
| [M9](#m9) | Dead MOXy/JAXB scaffolding: two `compileOnly` dependencies, a `requires static`, and an `opens` to an absent module | **Open** |
| [M10](#m10) | `codec.common` exports both `impl` packages unqualified | **Open** |

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
