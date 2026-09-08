# Extending Persistence

[&larr; Back to the BentoFX Persistence guide](guide.md)

This document describes writing new codecs and storage implementations. Applications that use the codec and storage implementations provided by the framework do not need anything described herein - see [Usage](guide.md#persistence-usage) instead.

## Table of Contents

- [How Discovery Works](#how-discovery-works)
- [Adding a Storage Destination](#adding-a-storage-destination)
  - [Storage Implementation Conventions](#storage-implementation-conventions)
- [Adding a Codec](#adding-a-codec)
- [What ServiceLoader Requires of a Provider](#serviceloader-requirements)
- [Complete Examples](#complete-examples)
- [See Also](#see-also)

<h2 id="how-discovery-works">How Discovery Works</h2>

`DefaultDockingLayoutPersistenceProvider` uses [ServiceLoader](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html) to acquire `LayoutCodecProvider` and `LayoutStorageProvider` implementations from the runtime module path (or the classpath for non-modularized applications). Each provider exposes a stable identifier, which is how an application selects a specific codec or storage implementation when more than one is available.

Only the *provider* is discovered. The `LayoutCodec` and `LayoutStorage` it returns are created by the provider, so an implementation is free to take constructor arguments or run setup logic that `ServiceLoader` could not supply.

<h2 id="adding-a-storage-destination">Adding a Storage Destination</h2>

1. Implement `LayoutStorage`. The interface is four methods: `exists()`, `openOutputStream()`, `openInputStream()`, and a default `close()`.

```java
public class SystemLayoutStorage implements LayoutStorage {

    private final Path path;

    SystemLayoutStorage(final Path path) {
        this.path = path;
    }

    @Override
    public boolean exists() throws BentoStateException {
        return Files.exists(path) && !isEmpty(path);
    }

    @Override
    public OutputStream openOutputStream() throws BentoStateException {
        // Write to a temporary file and move it into place on close, so a
        // failed save leaves the previously stored layout intact.
        return newAtomicOutputStream(path);
    }

    @Override
    public InputStream openInputStream() throws BentoStateException {
        return Files.newInputStream(path);
    }
}
```

2. Implement `LayoutStorageProvider`, which is the type `ServiceLoader` discovers.

```java
public class SystemLayoutStorageProvider implements LayoutStorageProvider {

    @Override
    public String getIdentifier() {
        return "system";
    }

    @Override
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        return new SystemLayoutStorage(resolvePath(layoutIdentifier, codecIdentifier));
    }
}
```

3. Register the provider so `ServiceLoader` can find it. Both declarations are recommended. The JVM only honors one of them, and which one it honors depends on how the consuming application is launched.

   a. For an application launched on the **module path**, declare it in `module-info.java`:

   ```java
   provides LayoutStorageProvider with SystemLayoutStorageProvider;
   ```

   b. For an application launched on the **class path**, add a provider-configuration file, named after the service interface, containing the fully qualified name of the implementation: <br/><br/>   
  
     File:
     ```text
     src/main/resources/META-INF/services/software.coley.bentofx.persistence.core.api.provider.LayoutStorageProvider
     ```
     Text:
     ```text
     software.coley.bentofx.persistence.impl.storage.system.provider.SystemLayoutStorageProvider
     ```

   All four bundled implementations carry both. Registering only the `provides` clause is the easier mistake to make, and it fails misleadingly: a class-path application finds no provider at all and reports `No LayoutStorageProvider implementation was found. Add a runtime dependency that provides one.` even though the dependency is present. Keep the two in sync. Renaming one implementation and not the other silenly breaks the incorrectly named path.

4. Add the module to the application's runtime dependencies.

   ```kotlin
   runtimeOnly("software.coley.bento-fx:persistence-storage-system:${version}")
   ```

   The JAR is discovered on either the module path or the class path depending on how the application is launched.

<h3 id="storage-implementation-conventions">Storage Implementation Conventions</h3>

To ensure consistent behavior among storage implementations, the following conventions are recommended, the implementations provided by the framework follow them, and callers rely on them:

1. **Closing the output stream stores the layout.** Buffer what is written and publish it only when the stream closes cleanly. This ensures a save that fails part way through leaves the previously stored layout intact instead of replacing it with a partial layout.
2. **Override default method implementations when subclasses can do so.** `LayoutStorageProvider.getLayoutIdentifiers`, `isLayoutStored` and `deleteLayout` all have defaults, so a storage implementation stays valid without them, but an application cannot offer users a list of saved layouts unless the storage it uses can enumerate. Both bundled implementations can: one file per layout, or one row per layout and codec.
3. **`exists()` answers whether there is a layout to read**, not whether a location is present. Empty content is not a layout: a restorer told that a layout exists will try to decode it, and an empty or truncated payload becomes a decode failure where a clean "nothing stored yet" would have produced the default layout.
4. **`close()` releases what the storage owns, and only that.** Whichever saver or restorer receives a `LayoutStorage` closes it, so a storage handed a resource it did not create should leave that resource alone.

<h2 id="adding-a-codec">Adding a Codec</h2>

A codec is the same three steps with a different pair of interfaces. `LayoutCodec` is three methods:

```java
public class YamlLayoutCodec implements LayoutCodec {

    @Override
    public String getIdentifier() {
        return "yaml";
    }

    @Override
    public void encode(
            final PersistableLayout layout,
            final OutputStream outputStream
    ) throws BentoStateException {
        // Write layout to outputStream. Do not close it: whoever opened it owns it.
    }

    @Override
    public PersistableLayout decode(
            final InputStream inputStream
    ) throws BentoStateException {
        // Read a PersistableLayout back, or throw BentoStateException.
    }
}
```

`LayoutCodecProvider` adds one method, `getLayoutCodec()`, and inherits its identifier contract from `LayoutPersistenceComponentProvider` exactly as the storage provider does. Register it both ways, for the same reason:

```java
provides LayoutCodecProvider with YamlLayoutCodecProvider;
```

```text
src/main/resources/META-INF/services/software.coley.bentofx.persistence.core.api.provider.LayoutCodecProvider
```

Two things a codec has to get right:

1. **The codec identifier becomes part of how a layout is addressed.** File-backed storage joins it to the layout identifier to form a file name, so it must survive that: see [Choosing Stable Identifiers](guide.md#choosing-stable-identifiers). Changing it later orphans every layout already stored under the old one.
2. **`decode` receives whatever was stored, including nothing useful.** A truncated or foreign payload must raise `BentoStateException` rather than returning a partly-populated `PersistableLayout`, because the restorer treats a thrown exception as "fall back to the default layout" and a returned value as "this is the layout".

<h2 id="serviceloader-requirements">What ServiceLoader Requires of a Provider</h2>

Both provider interfaces are discovered the same way, so both implementations must:

* be a public concrete class
* have a public no-argument constructor, or an implicit default constructor
* return a stable identifier from `getIdentifier()`
* optionally return `true` from `isDefault()` to be selected automatically when several providers are present
* be registered twice: with a `provides` clause in `module-info.java` for module-path launches, and with a `META-INF/services` file for class-path launches

<h2 id="complete-examples">Complete Examples</h2>

Four working implementations, two of each:

- [JSON Codec](../../persistence/codec/json)
- [XML Codec](../../persistence/codec/xml)
- [H2 Database Storage](../../persistence/storage/db/h2)
- [File Storage](../../persistence/storage/file)

<h2 id="see-also">See Also</h2>

- [Implementation: How a save and a restore drive the codec and storage](implementation.md)
- [Diagrams: Where the codec and storage sit in the class structure](diagrams.md)
- [ServiceLoader: The Java 21 API documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)
- [Introduction to the Service Provider Interface](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)
- [Java Service Provider Interface](https://www.baeldung.com/java-spi)
