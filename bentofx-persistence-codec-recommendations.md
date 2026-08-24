# BentoFX Persistence Codec: Schema & Versioning Recommendations

Context: `enhancement/issue-13` branch, PR #3 (philliplbryant/BentoFX, BENTO-13).

Open questions resolved against the working tree at HEAD `ffc0327`
("Make layout storage location configurable and support multiple applications
on one machine"). The original draft of this document was written against an
older commit and described the architecture as it stood then; the sections
below are corrected to the current tree.

## Current architecture (verified)

- `persistence/core`: the API and live-state layer. `LayoutCodec`,
  `PersistableLayout` (a record of `@Nullable String displayName` plus the
  `BentoState` list), `BentoStateException`, the `*State` classes and their
  builders, and the `LayoutsMenu` UI. `codec/common` depends on this and
  nothing else of substance.
- `persistence/codec/common`: canonical DTOs (`DockingLayoutDto`,
  `LayoutMetadataDto`, `BentoStateDto`, `DockContainerDto`,
  `DockContainerRootBranchDto`, `DockContainerBranchDto`,
  `DockContainerLeafDto`, `DockableDto`, `DragDropStageDto`,
  `DividerPositionDto`), the `BentoStateMapper` that converts between those
  DTOs and `PersistableLayout`, and `ElementNames` (shared wire-name
  constants).
- `persistence/codec/json`: `JsonLayoutCodec` over a plain `ObjectMapper`,
  per-field behavior injected through `*DtoJsonMixin` classes registered from
  `ObjectMapperMixins.mixinsByDto()`. DTOs stay unannotated apart from
  JSpecify nullness.
- `persistence/codec/xml`: `XmlLayoutCodec` over `jackson-dataformat-xml`,
  same pattern with `*DtoXmlMixin` classes and `XmlMapperMixins.mixinsByDto()`.
- `persistence/test-fixtures`: `SampleDockingLayoutDtoFactory`,
  `SampleBentoStateFactory`, `InMemoryLayoutCodec`, `InMemoryLayoutStorage`,
  shared by the codec test suites.
- The JAXB/MOXy removal is complete. `codec/common/build.gradle` declares only
  `api projects.persistenceCore` (plus `javafx.graphics` through the JavaFX
  plugin), and its `module-info.java` has no `jakarta.annotation` requirement
  and an unqualified `opens ...codec.common.mapper.dto;` rather than an
  `opens ... to org.eclipse.persistence.moxy`.

This is the "canonical model + per-format adapters" pattern, and the common
DTOs are the shared schema, expressed as Java types rather than as XSD or JSON
Schema documents.

One property matters for everything below: **neither codec relaxes
`FAIL_ON_UNKNOWN_PROPERTIES`.** Both build their mapper with only
`INDENT_OUTPUT` enabled, so Jackson's strict default applies and an
unrecognized element or property is a decode failure.
`XmlLayoutCodecTest.decodeRejectsAnUnrecognizedProperty` pins that behavior.

## Answers to the open questions

### 1. Yes: `LayoutMetadataDto` exists and schema versioning is already built

The version field is `LayoutMetadataDto.schemaVersion`, and the whole path is
wired end to end:

- `DockingLayoutDto` holds `@Nullable LayoutMetadataDto metadata` and owns
  `private static final int CURRENT_SCHEMA_VERSION = 1`, exposed through
  `getCurrentSchemaVersion()`.
- `LayoutMetadataDto` carries `@Nullable Integer schemaVersion` and
  `@Nullable String displayName`.
- `BentoStateMapper.toDto` always populates metadata, stamping the current
  schema version and the layout's display name.
- `BentoStateMapper.fromDto` calls `validateSupportedMetadata` before anything
  else, rejecting a version below 1 or above the current one with
  `BentoStateException`.
- `LayoutMetadataDtoJsonMixin` and `LayoutMetadataDtoXmlMixin` map
  `schemaVersion` through `ElementNames.SCHEMA_VERSION_ELEMENT_NAME`, and both
  are registered in their format's mixin map.
- Coverage exists on all three levels: `BentoStateMapperTest` exercises the
  current version, 0, and current-plus-one; `JsonLayoutCodecTest` and
  `XmlLayoutCodecTest` each assert the version is written, and the XML suite
  also asserts a future version is rejected through the codec.

On the wire it is an element, not an attribute, in both formats:

```xml
<dockingLayout>
  <metadata>
    <displayName>Multi-Monitor</displayName>
    <schemaVersion>1</schemaVersion>
  </metadata>
  ...
```

So recommendation 3's precondition is satisfied and the "add a version field
before the format stabilizes" advice is already taken. What is still missing
is the migrator, and three details about the existing guard shape the design:

**The absent-metadata branch now rejects.** It previously returned early when
`metadata == null` or `metadata.schemaVersion == null`, accepting an unversioned
payload and mapping it with current-shape logic. Because the persistence
framework is unreleased there are no such payloads in the wild, so
`validateSupportedMetadata` now throws `BentoStateException` with "BentoFX
docking layout declares no schema version" instead. Every layout the framework
reads therefore states its version, and a migrator never has to guess whether an
unversioned file is an old one or a truncated one.

`validateSupportedMetadata` returns the validated metadata now, which let
`fromDto` drop its null-metadata ternary and read `metadata.displayName`
directly.

**Rejection is asymmetric, and deliberately so.** A future version fails loudly
and an older version has nowhere to go yet. Because unknown properties are
already strict, a document from a newer writer would usually fail on an
unrecognized element even without the version check; the version check is what
turns that into a comprehensible error message instead of a Jackson binding
complaint. Worth keeping in mind when writing the migrator: the version guard
is the diagnostic, not the only gate.

**The insertion point is precise.** A `LayoutMigrator` belongs in `fromDto`,
between the `validateSupportedMetadata` call and the loop over
`dockingLayoutDto.bentoStates`. Nothing else in the mapper needs to know more
than the current DTO shape.

One structural note: `CURRENT_SCHEMA_VERSION` lives on `DockingLayoutDto` while
the field it describes lives on `LayoutMetadataDto`. That is harmless now, but
when a migrator arrives the natural owner of "which versions can I read" is the
migrator rather than either DTO.

Separately, `remaining-tasks.md` item 1 adds a requirement this document did not
account for: importing layouts saved by the previous docking framework. That is
not a DTO-tree migration at all, since the source is a foreign format with its
own model. It needs its own reader that produces a current-shape
`DockingLayoutDto`, and it should not be folded into the version-to-version
migrator.

### 2. Bean Validation is still unused, but the version is already managed for you

Nothing in the repo compiles against Jakarta Validation: no `jakarta.validation`
import, no `@NotNull`, `@Size`, or `@Valid`, and no `requires` of a validation
module in any `module-info.java`.

What is present:

- `gradle/libs.versions.toml` pins `hibernate-validator 9.1.3.Final` and
  declares `hibernate-validator-bom`, plus `jakarta-el` mapped to
  `org.glassfish.expressly:expressly 6.0.0`.
- `platform/build.gradle` applies both `jakarta.jakartaee-bom 11.0.0` and the
  Hibernate Validator BOM as strict platforms, and every module gets
  `:platform` through `bento.project.project-convention`.
- `persistence/storage/db/h2` is the only consumer, with
  `implementation libs.hibernate.validator` and `runtimeOnly libs.jakarta.el`,
  for Hibernate ORM's own runtime validation integration. Its
  `dependencyAnalysis` block files `hibernate-validator` under `onRuntimeOnly`,
  which is the plugin correctly observing that nothing there compiles against
  the API.

So adding Bean Validation to `codec/common` is a new dependency, but a cheaper
one than the original draft assumed: because the Jakarta EE and Hibernate
Validator BOMs are already applied repo-wide, a versionless
`jakarta.validation-api` catalog entry would resolve without a new version pin.
The cost is a catalog entry, a dependency on `codec/common`, a
`requires jakarta.validation` in its `module-info.java`, and an implementation
on the runtime classpath wherever validation actually runs.

**The recommendation is to not do it.** Two things changed since the draft:

- Strict unknown-property handling already rejects a structurally wrong
  document, which is most of what annotations would buy at this layer.
- The mapper already validates by hand, and does it well:
  `validateSupportedMetadata` for the version, and `BentoStateException` for a
  missing Bento identifier. That is a deliberate, tested choice, and it sits in
  the one place both codecs pass through.

Every DTO field is a `@Nullable` boxed type, so `@NotNull` on the DTO would
contradict the JSpecify nullness the codecs rely on. The constraints that
actually matter are structural: a leaf's `selectedDockableIdentifier` naming one
of its own dockables, divider indices within range for their branch's child
count. Bean Validation expresses "not null" well and "this identifier resolves
within this subtree" badly, and the latter is where a corrupted layout really
fails. Extend `BentoStateMapper`'s existing checks instead.

### 3. `jackson-dataformat-xml` confirmed

Three independent confirmations:

- `persistence/codec/xml/build.gradle` declares `implementation libs.jackson.xml`
  (`com.fasterxml.jackson.dataformat:jackson-dataformat-xml`, version from the
  Jackson BOM at 2.22.1).
- `XmlLayoutCodec` builds its mapper with `XmlMapper.builder()`.
- The XML mixins use `@JacksonXmlRootElement`, `@JacksonXmlElementWrapper`, and
  `@JacksonXmlProperty`, and `module-info.java` reads
  `com.fasterxml.jackson.dataformat.xml`.

No JAXB or MOXy anywhere on the XML path.

## Revised recommendations

### Don't hand-write a separate XSD / JSON Schema

Unchanged, and now cheaper to test than the draft assumed, because
`persistence/test-fixtures` already provides `SampleDockingLayoutDtoFactory` and
`SampleBentoStateFactory` for both codec suites.

- **JSON Schema**: generate it with a generator that understands Jackson mixins
  (victools `jsonschema-generator` with its Jackson module), configured with the
  same mixin map `ObjectMapperMixins.mixinsByDto()` returns, so the schema
  cannot go stale independently of the codec.
- **XSD**: with JAXB gone there is no clean off-the-shelf DTO-to-XSD generator
  left in the stack. Either hand-maintain one and keep it honest with a test
  that round-trips the sample fixtures through `XmlLayoutCodec` and asserts
  conformance, or skip it. Given that unknown properties are already rejected
  and the mapper validates structurally, skipping it is the reasonable default.

### Validate in the mapper, not with a schema language and not with annotations

See question 2 above. The repo has already chosen this, and the choice is sound.
The work left is extending `BentoStateMapper`'s checks to the structural
invariants, not adopting a validation stack.

### Version migration at the DTO level

Still right, and still the plan of record. Write migrations as plain Java over
the DTO tree rather than XSLT-for-XML and jq-for-JSON document transforms:

```java
DockingLayoutDto migrateV1ToV2(DockingLayoutDtoV1 old) { ... }
```

`BentoStateMapper` only ever needs the current DTO shape, and a `LayoutMigrator`
called from `fromDto` upgrades older DTOs before the mapping loop. One place,
format-agnostic, tested once rather than once per codec. The open decisions are
what an absent version means and where the foreign-format importer lives, both
noted under question 1.

## Smaller findings, now fixed

- **`displayName` was the one wire name not routed through `ElementNames`.** It
  had no constant and no mixin mapping, riding instead on Jackson's default
  public-field auto-detection, so its wire name was the Java field name in both
  formats and renaming the field would have silently changed the wire format.
  `ElementNames` now declares `DISPLAY_NAME_ELEMENT_NAME`, and both
  `LayoutMetadataDtoJsonMixin` and `LayoutMetadataDtoXmlMixin` map the field
  through it. The constant's value matches the old field name, so the encoded
  output is unchanged.
- **XML had no `displayName` test coverage**, and mirroring the two JSON tests
  found a real bug. `XmlLayoutCodecTest` now has
  `encodeThenDecodeRoundTripsTheDisplayName` and
  `aLayoutWithNoDisplayNameRoundTripsWithoutOne`; the second one failed on first
  run, restoring `""` where it expected `null`.
- **A layout saved without a name did not round-trip through XML.** Neither
  metadata mixin set `@JsonInclude(NON_NULL)`, though both root mixins do, so a
  null `displayName` was written as an empty `<displayName/>` element and read
  back as `""`, so `PersistableLayout.displayName()` returned a blank string
  where the layout had no name at all. Both metadata mixins now carry
  `@JsonInclude(NON_NULL)`, which drops the element entirely for an unnamed
  layout and also stops JSON writing `"displayName": null`. Only XML could
  observe the fault, because JSON round-trips an explicit null as null.
