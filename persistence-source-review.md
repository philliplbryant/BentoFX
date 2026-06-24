# BentoFX Persistence Source Review

## Executive Summary

The highest-value improvements are architectural rather than cosmetic:

1. Make codec/storage provider selection explicit and deterministic.
2. Separate JavaFX-thread work from storage/codec background work more cleanly.
3. Split `DockingLayoutSaver` and `DockingLayoutRestorer` into smaller collaborators.
4. Remove implicit fallback identifiers based on `System.identityHashCode`.
5. Add version/schema metadata to persisted layouts.
6. Improve lifecycle management for auto-save and provider-discovered resources.
7. Reduce mapper/DTO boilerplate with records, MapStruct, Immutables, or Jackson configuration where appropriate.

---

## 1. Provider Discovery and Configuration

### Current Design

`DefaultDockingLayoutPersistenceProvider` discovers `LayoutCodecProvider` and `LayoutStorageProvider` using `ServiceLoader` and uses the first provider returned for each service type.

### Concern

This is simple, but it has usability and determinism risks:

- If both JSON and XML codec modules are on the runtime path, provider selection depends on `ServiceLoader` order.
- If both file and database storage modules are present, storage selection is similarly implicit.
- Missing providers currently fail late and non-descriptively through `iterator().next()`.
- There is no obvious way for applications to select a codec or storage implementation by identifier.

### Recommendation

Introduce explicit selection while keeping the simple default.

Possible API direction:

```java
public interface LayoutCodecProvider {
    String getIdentifier();
    LayoutCodec getLayoutCodec();
}
```

```java
public interface LayoutStorageProvider {
    String getIdentifier();
    LayoutStorage getLayoutStorage(String layoutIdentifier, String codecIdentifier);
}
```

Then add one or more configuration-aware factory methods:

```java
public final class PersistenceOptions {
    private final String codecIdentifier;
    private final String storageIdentifier;
}
```

```java
public DefaultDockingLayoutPersistenceProvider(PersistenceOptions options)
```

The no-arg constructor can keep the current behavior for demos and simple usage, but it should fail with a clear exception if zero or multiple providers are discovered and no explicit selection was supplied.

### Suggested Priority

High.

This is the most important usability improvement before the API is widely used.

---

## 2. ServiceLoader Error Handling

### Current Design

The default provider calls:

```java
ServiceLoader.load(LayoutCodecProvider.class).iterator().next();
ServiceLoader.load(LayoutStorageProvider.class).iterator().next();
```

### Concern

If no provider exists, `NoSuchElementException` will be thrown with little context.

### Recommendation

Wrap provider discovery in a small helper that provides clear diagnostics:

```java
private static <T> T requireSingleProvider(Class<T> serviceType) {
    List<T> providers = ServiceLoader.load(serviceType)
            .stream()
            .map(ServiceLoader.Provider::get)
            .toList();

    if (providers.isEmpty()) {
        throw new BentoStateConfigurationException(
                "No " + serviceType.getSimpleName() + " found. Add a runtime provider module."
        );
    }

    if (providers.size() > 1) {
        throw new BentoStateConfigurationException(
                "Multiple " + serviceType.getSimpleName() + " providers found. Configure one explicitly."
        );
    }

    return providers.getFirst();
}
```

A dedicated unchecked configuration exception would be useful:

```java
public class BentoStateConfigurationException extends RuntimeException {
    ...
}
```

### Suggested Priority

High.

This improves developer experience immediately.

---

## 3. Auto-Save Lifecycle and Threading

### Current Design

`AbstractAutoCloseableLayoutSaver`:

- Enables auto-save in the constructor.
- Creates its own scheduled executor.
- Registers as a `DockEventListener` on all current `Bento` instances.
- Uses `Cleaner` to trigger cleanup.
- Calls `saveLayout()` from the scheduler thread.

### Concerns

1. Starting background behavior from the constructor makes the class harder to test and harder to reason about.
2. The saver only registers listeners for Bentos available at construction time. Later-added Bentos may not be observed.
3. `saveLayout()` reads JavaFX object state. If it is called from the scheduler thread, it may access JavaFX objects off the JavaFX Application Thread.
4. `Cleaner` is usually a last-resort cleanup mechanism. Explicit lifecycle management is clearer here.
5. `ScheduledExecutorService.close()` requires newer Java. If compatibility ever changes, `shutdown()` is more common and explicit.
6. `enableAutoSave(...)` can be called repeatedly and may re-register the same listener multiple times.

### Recommendation

Refactor auto-save into a separate collaborator:

```java
public final class LayoutAutoSaveService implements AutoCloseable {
    void start();
    void stop();
    void markDirty();
}
```

Then have `DockingLayoutSaver` focus only on saving.

The auto-save service should accept its dependencies:

```java
public LayoutAutoSaveService(
        LayoutSaver layoutSaver,
        BentoProvider bentoProvider,
        ScheduledExecutorService executor,
        Duration interval
)
```

This improves testability by allowing tests to inject a fake executor or manually trigger save attempts.

### JavaFX Thread Recommendation

If `saveLayout()` reads JavaFX state, make the threading contract explicit.

Possible approaches:

1. Require callers to invoke `saveLayout()` on the JavaFX Application Thread.
2. Have the saver internally marshal layout capture to the JavaFX Application Thread.
3. Split save into:
   - capture layout state on the JavaFX Application Thread
   - encode/write off the JavaFX Application Thread

The third option is the cleanest long-term design:

```text
JavaFX Application Thread:
    BentoFX runtime graph -> List<BentoState>

Background thread:
    List<BentoState> -> codec -> storage
```

### Suggested Priority

High.

The current scheduled save behavior is useful, but the thread boundary should be clarified and enforced.

---

## 4. Split Saver and Restorer Responsibilities

### Current Design

`DockingLayoutSaver` and `DockingLayoutRestorer` contain most of the persistence logic directly:

- walking Bento/container graphs
- building state objects
- reading/writing storage
- invoking codecs
- creating JavaFX runtime objects
- applying stage geometry
- applying divider positions and collapsed state
- resolving providers

### Concern

These classes are doing too much. They are difficult to test in isolation and are likely to grow as versioning, migration, named layouts, or additional state handling is added.

### Recommendation

Split into smaller classes with explicit responsibilities.

Possible collaborators:

```text
DockingLayoutSaver
  -> BentoStateCollector
  -> DragDropStageStateCollector
  -> LayoutWriter

DockingLayoutRestorer
  -> LayoutReader
  -> BentoLayoutRestorationService
  -> DockContainerRestorer
  -> DockableRestorer
  -> DragDropStageRestorer
  -> StageBoundsService
```

A smaller split would still help:

- `BentoStateCollector`
- `BentoLayoutRestorer`
- `DockableRestorer`
- `LayoutIO`

### Benefits

- More focused tests.
- Lower cyclomatic complexity.
- Easier future versioning/migration support.
- Easier to document extension points.
- Better adherence to the Single Responsibility Principle.

### Suggested Priority

Medium-high.

This is not required for a first release, but it will pay off quickly.

---

## 5. Avoid Runtime-Generated Fallback Identifiers

### Current Design

The saver falls back to identifiers like:

```java
"branch-" + System.identityHashCode(branch)
"leaf-" + System.identityHashCode(leaf)
```

### Concern

This contradicts the persistence documentation's stable identifier guidance. These identifiers are not stable across application executions and can make saved layouts unrecoverable or partially recoverable in surprising ways.

### Recommendation

For persistence, missing identifiers should probably be treated as a configuration problem rather than silently replaced.

Possible options:

1. Throw `BentoStateException` when a persistent container has no stable identifier.
2. Log a warning and skip that object.
3. Allow a configurable `IdentifierStrategy`.

Best long-term option:

```java
public interface IdentifierStrategy {
    String requireIdentifier(Identifiable identifiable, String role)
            throws BentoStateException;
}
```

Default behavior should be strict. A demo/debug strategy can generate fallback IDs if needed.

### Suggested Priority

High.

Silent unstable identifiers can create confusing persistence bugs.

---

## 6. Add Persisted Schema and Version Metadata

### Current Design

The serialized root is effectively a collection of `BentoState` DTOs under a layout root. There does not appear to be explicit persistence format version metadata.

### Concern

Without schema/version metadata, future changes are harder:

- renamed fields
- changed enum names
- removed dockables
- new layout state fields
- changed defaults
- migration from old layouts

### Recommendation

Add metadata at the serialized root:

```java
public final class DockingLayoutMetadata {
    private int schemaVersion;
    private String codecIdentifier;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
```

or minimally:

```java
public class DockingLayoutDto {
    public int schemaVersion = 1;
    public List<BentoStateDto> bentoStates = new ArrayList<>();
}
```

Then introduce:

```java
public interface LayoutMigration {
    int fromVersion();
    int toVersion();
    DockingLayoutDto migrate(DockingLayoutDto input);
}
```

This can remain internal to codecs at first.

### Suggested Priority

Medium-high.

Adding version metadata early is much easier than adding it after users have persisted layouts.

---

## 7. Make DTOs Records or Generated Value Types

### Current Design

The codec common module uses DTO classes with mutable public fields, plus separate mapper methods and Jackson mix-ins.

### Concern

This is workable, but there is a lot of custom boilerplate:

- DTO classes
- mix-in classes
- manual mapper logic
- many null-handling paths
- repeated list copying
- tests required for mapping/mix-in compatibility

### Options

#### Option A: Java records for DTOs

Use records where possible:

```java
public record DockableDto(String identifier) {}
```

Records reduce boilerplate and work well with Jackson, including Jackson XML with the correct modules/configuration.

#### Option B: Immutables.org

Immutables can generate builders, immutable value types, and Jackson integration.

Pros:
- Less custom builder code.
- Strong immutability.
- Good generated equals/hashCode/toString.
- Jackson support.

Cons:
- Adds annotation processor dependency.
- Generated sources may be less appealing for a small project.

#### Option C: MapStruct

Use MapStruct for state/DTO mapping.

Pros:
- Reduces custom mapper code.
- Compile-time generated mappers.
- Good for repetitive object mapping.

Cons:
- Tree/polymorphic mapping still needs custom handling.
- Adds annotation processor complexity.

#### Option D: Jackson directly with mix-ins

Keep the current mix-in approach, but consider whether DTOs are necessary for every type. Jackson can serialize many immutable types with constructors, `@JsonCreator` through mix-ins, and modules.

Pros:
- Fewer DTOs.
- Less mapper code.

Cons:
- Harder to keep API model completely serialization-independent.

### Recommendation

For this project, the safest incremental improvement is:

1. Keep DTOs.
2. Convert simple DTOs to records where practical.
3. Keep `BentoStateMapper` initially.
4. Later evaluate MapStruct if mapper complexity grows.

### Suggested Priority

Medium.

Current code works, but mapper/DTO/mix-in boilerplate is already substantial.

---

## 8. Reconsider JavaFX Types in the Persistence API State Model

### Current Design

`DockableState` contains JavaFX `Node`, icon factories, menu factories, and consumers. It is not itself serialized directly, but it lives in the persistence API state package.

### Concern

The name `DockableState` can be confusing because some of its fields are not persisted state; they are runtime reconstruction data.

This creates two conceptual categories:

1. Persisted state:
   - identifiers
   - container placement
   - selected dockables
   - divider positions
   - stage geometry

2. Runtime reconstruction data:
   - JavaFX `Node`
   - `Tooltip`
   - menu factories
   - icon factories
   - consumers

### Recommendation

Consider separating persisted state from runtime reconstruction descriptions.

Possible naming options:

```java
PersistedDockableState
DockableDescriptor
DockableDefinition
DockableRestorationSpec
```

Example:

```java
public record DockableReference(String identifier) {}
```

```java
public final class DockableDefinition {
    private final String identifier;
    private final Supplier<Node> nodeSupplier;
    private final Supplier<Tooltip> tooltipSupplier;
    ...
}
```

This would make it clearer that codec state and application-provided reconstruction data are different.

### Suggested Priority

Medium.

This is a design clarity issue, not necessarily a functional issue.

---

## 9. Prefer Lazy JavaFX Suppliers Over Eager JavaFX Objects

### Current Design

`DockableState` can contain a concrete `Node`.

The demo uses `Platform.runLater(...)` to create `DockableState` values because those values contain JavaFX controls.

### Concern

Eager JavaFX object creation creates timing and threading constraints for providers. It can also make repeated restoration harder if the same `Node` instance is reused.

### Recommendation

Prefer suppliers/factories for JavaFX objects:

```java
public final class DockableState {
    private final Supplier<Node> nodeSupplier;
}
```

or:

```java
public interface DockableNodeFactory {
    Node createNode(String dockableIdentifier);
}
```

Then the restorer can call the supplier on the JavaFX Application Thread and get a fresh node for each restored dockable.

### Benefits

- Cleaner provider lifecycle.
- Better repeated restore behavior.
- Less need for providers to pre-initialize with `Platform.runLater(...)`.
- More natural support for domain-object-backed dockables.

### Suggested Priority

Medium-high.

This aligns well with the provider-based architecture.

---

## 10. Use `Path` Instead of `File` in File Storage

### Current Design

`FileLayoutStorage` uses `java.io.File`.

### Concern

`Path` is the modern NIO abstraction and makes path composition, normalization, and testing cleaner.

### Recommendation

Change:

```java
public FileLayoutStorage(File file)
```

to:

```java
public FileLayoutStorage(Path path)
```

and have the provider use:

```java
Path layoutFile = baseDirectory.resolve(layoutIdentifier + "." + extension);
```

Also consider sanitizing `layoutIdentifier` to prevent accidental path traversal or invalid filenames.

### Suggested Priority

Medium.

This is a straightforward modernization.

---

## 11. Make File Storage Location Configurable

### Current Design

File storage writes under:

```text
${user.home}/.bentofx
```

### Concern

Applications may need different storage locations:

- app-specific configuration directories
- test directories
- portable mode
- user-selected workspace directories
- OS-specific application data directories

### Recommendation

Provide configuration through:

- system properties
- constructor injection
- a `PersistenceOptions` object
- environment variables only as a fallback

Example:

```java
public FileLayoutStorageProvider(Path baseDirectory)
```

For Java desktop apps, consider `io.github.g00fy2:versioncompare`? No — for directories, better options are:

- `dev.dirs:directories`
- `net.harawata:appdirs`

These libraries help locate OS-appropriate config/data directories.

### Suggested Priority

Medium.

This improves real application usability.

---

## 12. Database Storage Resource and Null Handling

### Current Design

`DatabaseLayoutStorage.openInputStream()` assumes the entity exists and has a payload.

### Concern

If storage disappears between `exists()` and `openInputStream()`, or if the DB row is corrupt, this can produce `NullPointerException`.

### Recommendation

Handle missing rows explicitly:

```java
if (entity == null || entity.payload == null) {
    throw new IOException("No persisted layout found for " + key);
}
```

Because `LayoutStorage.openInputStream()` already allows `IOException`, the database implementation should use it for storage failures.

### Suggested Priority

Medium-high.

This is a robustness fix.

---

## 13. Database Storage: Consider Simpler Persistence for a Single BLOB

### Current Design

The H2 storage implementation uses Jakarta Persistence, Hibernate, Hikari, entity classes, composite keys, and module requirements.

### Concern

For storing a single layout payload by `(layoutIdentifier, codecIdentifier)`, JPA/Hibernate may be much heavier than necessary.

### Alternatives

#### Option A: JDBC + HikariCP

Use plain JDBC with `JdbcTemplate`-style helper methods or a very small DAO.

Pros:
- Much less module complexity.
- Fewer runtime dependencies.
- Easier to understand and test.
- Good fit for simple BLOB storage.

#### Option B: jOOQ

Good if the storage schema grows and type-safe SQL is desired.

Pros:
- Excellent SQL modeling.
- Good for complex schemas.

Cons:
- Likely overkill here.

#### Option C: Keep JPA

Keep JPA if the intent is to support richer layout metadata, named layouts, query APIs, and history later.

### Recommendation

If H2 storage is only intended to store a serialized payload, strongly consider replacing JPA/Hibernate with JDBC + HikariCP.

### Suggested Priority

Medium.

This could reduce third-party dependency complexity substantially.

---

## 14. Stage Geometry and Screen Bounds

### Current Design

`StageUtils` computes aggregate screen bounds and clamps restored X/Y.

### Concern

The current implementation uses `Double.MIN_VALUE` as the initial max value. In Java, `Double.MIN_VALUE` is the smallest positive double, not the most negative double. This can be wrong for multi-monitor setups with negative coordinates.

### Recommendation

Use:

```java
double maxX = -Double.MAX_VALUE;
double maxY = -Double.MAX_VALUE;
```

or:

```java
double maxX = Double.NEGATIVE_INFINITY;
double maxY = Double.NEGATIVE_INFINITY;
```

Also consider clamping width/height if restored dimensions exceed current screen bounds.

### Suggested Priority

High.

This is a likely correctness bug for multi-monitor setups.

---

## 15. JavaFX Application Thread Checks

### Current Design

The restorer and providers are expected to create JavaFX objects on the JavaFX Application Thread, but enforcement appears limited.

### Recommendation

Add utility checks at key boundaries:

```java
private static void requireFxApplicationThread(String operation) {
    if (!Platform.isFxApplicationThread()) {
        throw new IllegalStateException(operation + " must run on the JavaFX Application Thread");
    }
}
```

Use this in methods that create or mutate JavaFX objects:

- restore drag/drop stage
- restore root branch
- restore branch
- restore leaf
- restore dockable
- apply divider positions
- collapse leaves

For a friendlier API, offer:

```java
CompletionStage<DockingLayout> restoreLayoutAsync(...)
```

where decoding happens off-thread and UI creation happens on the FX thread.

### Suggested Priority

Medium-high.

This makes the threading contract testable and less error-prone.

---

## 16. Replace `@Nullable` Parameters With Null Object Defaults Where Practical

### Current Design

Some provider dependencies are nullable:

- `StageIconImageProvider`
- `DockContainerLeafMenuFactoryProvider`

### Concern

Nullable collaborators spread null checks through the implementation.

### Recommendation

Introduce default no-op providers:

```java
public final class NoOpStageIconImageProvider implements StageIconImageProvider {
    public List<Image> getStageIcons() {
        return List.of();
    }
}
```

```java
public final class NoOpDockContainerLeafMenuFactoryProvider
        implements DockContainerLeafMenuFactoryProvider {
    public Optional<DockContainerLeafMenuFactory> getDockContainerLeafMenuFactory(String id) {
        return Optional.empty();
    }
}
```

Then normalize nulls at construction time.

### Suggested Priority

Low-medium.

This improves readability and testability.

---

## 17. Make State Classes More Immutable Internally

### Current Design

Some constructors store builder lists directly, while getters return `List.copyOf(...)`.

### Concern

If a builder is reused after `build()`, the already-built state object can be mutated indirectly because it holds the same mutable list reference.

Example pattern:

```java
this.rootBranchStates = requireNonNull(rootBranchStates);
```

### Recommendation

Copy inputs in constructors:

```java
this.rootBranchStates = List.copyOf(rootBranchStates);
this.dragDropStageStates = List.copyOf(dragDropStageStates);
```

Do this consistently for lists and maps. Then getters can return the stored immutable collection directly.

### Suggested Priority

High.

This is a correctness and immutability fix.

---

## 18. Consider Records for Simple State Types

### Current Design

State types use hand-written classes and nested builders.

### Recommendation

Do not replace all builders immediately. Some state objects have many optional properties, so builders are useful.

However, simple types can be records:

```java
public record DividerPosition(int index, double position) {}
```

DTOs are better initial candidates for records than public API state classes.

### Suggested Priority

Low-medium.

Useful, but not urgent.

---

## 19. Testability Improvements

### Current Test Coverage

The project already includes:

- unit tests for state builders
- functional tests for saver/restorer
- integration tests for file and database storage
- codec tests
- mapper compatibility tests
- in-memory codec/storage test fixtures

### Recommended Additional Tests

Add tests for:

1. Missing `LayoutCodecProvider`
2. Missing `LayoutStorageProvider`
3. Multiple codec/storage providers on the runtime path
4. Auto-save does not save without a dock event
5. Auto-save saves after a dock event
6. Auto-save listener registration is not duplicated
7. `close()` disables auto-save and removes listeners
8. Save invoked from scheduler does not violate JavaFX threading rules
9. Missing persisted dockable identifier is skipped and logged
10. Missing database row between `exists()` and `openInputStream()`
11. Stage bounds with negative monitor coordinates
12. Layout identifier sanitization for file storage
13. Builder immutability after `build()`
14. Provider-created JavaFX object failure handling
15. Round-trip compatibility with older schema versions after versioning is added

### Suggested Priority

High for items 1, 2, 8, 10, 11, and 13.

---

## 20. Module Boundaries

### Current Design

The API module exports both API and implementation packages:

```java
exports software.coley.bentofx.persistence.impl.provider;
exports software.coley.bentofx.persistence.impl;
```

### Concern

Exporting implementation packages from the API module makes those classes part of the public surface. It may be harder to refactor later.

### Recommendation

Consider one of these:

1. Keep implementation exported for now but document it as provisional.
2. Move default implementations to a separate module, such as:
   - `bento.fx.persistence.default`
   - `bento.fx.persistence.runtime`
3. Export only provider implementation if direct construction is an intentional API.

The cleanest design is:

```text
persistence-api
    public interfaces, state model

persistence-default
    DefaultDockingLayoutPersistenceProvider
    DockingLayoutSaver
    DockingLayoutRestorer
    DefaultBentoProvider
```

### Suggested Priority

Medium.

This affects binary compatibility and public API shape.

---

## 21. Naming and API Clarity

### Current Design

The API uses terms like:

- `DockingLayoutPersistenceProvider`
- `LayoutCodecProvider`
- `LayoutStorageProvider`
- `DockableStateProvider`
- `DockableState`

### Recommendation

The provider naming is mostly good.

The main naming concern is `DockableState`. It acts more like a runtime restoration descriptor than pure persisted state.

Consider future renames only if API compatibility is still flexible:

- `DockableDescriptor`
- `DockableDefinition`
- `DockableRestoreSpec`
- `DockableContentProvider`

If renaming would cause churn, keep the current names and document the distinction clearly.

### Suggested Priority

Low-medium.

---

## 22. Error Handling Philosophy

### Current Design

The saver/restorer often logs and continues for component-level failures, but codec/storage failures are fatal.

### Recommendation

Keep that philosophy, but make it explicit in code by introducing a result or error collector.

Example:

```java
public final class LayoutRestoreReport {
    private final DockingLayout layout;
    private final List<LayoutRestoreWarning> warnings;
}
```

This would let applications surface partial-restore warnings to users instead of relying only on logs.

Possible future API:

```java
LayoutRestoreResult restoreLayoutWithReport(Supplier<DockingLayout> fallback);
```

### Suggested Priority

Low-medium.

Good for usability, but not required for initial release.

---

## 23. Third-Party Libraries Worth Considering

### Mapping and DTOs

- **MapStruct**: reduces manual mapper boilerplate.
- **Immutables.org**: generates immutable value types/builders and Jackson support.
- **AutoValue**: similar value-type generation, less modern than records/Immutables.
- **Jackson records support**: may reduce DTO and mix-in code if records are used.

### Storage

- **JDBC + HikariCP**: likely simpler than JPA/Hibernate for H2 BLOB storage.
- **jOOQ**: useful if storage schema becomes query-heavy.
- **dev.dirs:directories** or **appdirs**: OS-appropriate app data/config directories.

### Testing

- **Testcontainers**: useful if database storage expands beyond embedded H2.
- **Awaitility**: useful for testing auto-save timing without brittle sleeps.
- **ArchUnit**: useful for enforcing module/package boundaries and preventing API from depending on implementation.

### Recommendation

Do not add libraries just to add libraries.

The most compelling near-term candidates are:

1. Awaitility for scheduled auto-save tests.
2. ArchUnit for module/API boundary tests.
3. JDBC + HikariCP instead of JPA/Hibernate if DB storage remains simple.
4. MapStruct only if mapper code continues to grow.

---

## Suggested Roadmap

### Before First Merge / PR Review

1. Fix `StageUtils` `Double.MIN_VALUE` usage.
2. Remove or make strict the `System.identityHashCode` fallback identifiers.
3. Improve ServiceLoader missing-provider errors.
4. Copy collections defensively inside state constructors.
5. Add tests for builder immutability and missing providers.
6. Clarify or enforce JavaFX Application Thread requirements for save/restore.

### Shortly After

1. Add explicit codec/storage selection.
2. Split saver/restorer into smaller services.
3. Refactor auto-save into a separate lifecycle-managed service.
4. Add schema/version metadata.
5. Add layout restore result/report type for partial-restore warnings.

### Later / Optional

1. Move default implementation out of the API module.
2. Convert DTOs to records or generated immutable types.
3. Evaluate MapStruct.
4. Replace H2 JPA storage with JDBC if the schema remains simple.
5. Add migration support.

---

## Most Important Recommendations

If you only make a few changes, I would prioritize these:

1. **Make provider selection deterministic and failure messages clear.**
2. **Remove runtime-generated fallback identifiers from persisted layouts.**
3. **Fix state immutability by copying collections in constructors.**
4. **Clarify and enforce JavaFX Application Thread boundaries.**
5. **Fix multi-monitor stage bounds handling.**
6. **Add schema version metadata before users depend on persisted files.**

These changes improve correctness, usability, and future compatibility without requiring changes to core or the basic demo.
