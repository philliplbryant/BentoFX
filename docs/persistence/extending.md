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
    public boolean exists() {
        return Files.exists(path) && !isEmpty(path);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        // Write to a temporary file and move it into place on close, so a
        // failed save leaves the previously stored layout intact.
        return newAtomicOutputStream(path);
    }

    @Override
    public InputStream openInputStream() throws IOException {
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

3. Register the provider so `ServiceLoader` can find it. Both module and classpath declarations are recommended. The JVM only honors one of them, and which one it honors depends on how the consuming application is launched.

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
2. **Override default interface methods.** `isLayoutStored` has a default implementation that opens the storage and calls `exists()`, which works for any implementation. Override it when the storage implementation can answer more efficiently than opening a layout.
3. **`exists()` should specify whether there is a layout to read**, not whether a layout location is present. Empty content is not a layout. If `exisits()` were to return true for an empty layout, the codec would likely attempt to decode it, resulting in an empty layout. As such, an empty layout then erroneously manifests as a decode failure when a failure indicating "nothing stored here" would have resulted in the default layout being used instead.
4. **`close()` releases what the storage owns, and only that.** Whichever provider receives a `LayoutStorage` closes it, so a storage handed a resource it did not create should leave that resource alone.

<h2 id="adding-a-codec">Adding a Codec</h2>

1. Implement three `LayoutCodec` methods:

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
        // Write layout to outputStream but DO NOT CLOSE IT.
        // Whoever opened the stream owns it and should close it.
    }

    @Override
    public PersistableLayout decode(
            final InputStream inputStream
    ) throws BentoStateException {
        // Either read a PersistableLayout back or throw BentoStateException.
    }
}
```

2. Implement `LayoutCodecProvider`, which is the type `ServiceLoader` discovers.

```java
public class YamlLayoutCodecProvider implements LayoutCodecProvider {

    @Override
    public String getIdentifier() {
        return "yaml";
    }

    @Override
    public LayoutCodec getLayoutCodec() {
        return new YamlLayoutCodec();
    }
}
```

3. Register the provider so `ServiceLoader` can find it. Both module and classpath declarations are recommended. The JVM only honors one of them, and which one it honors depends on how the consuming application is launched.

   a. For an application launched on the **module path**, declare it in `module-info.java`:

   ```java
   provides LayoutCodecProvider with YamlLayoutCodecProvider;
   ```

   b. For an application launched on the **class path**, add a provider-configuration file, named after the service interface, containing the fully qualified name of the implementation:

     File:
     ```text
     src/main/resources/META-INF/services/software.coley.bentofx.persistence.core.api.provider.LayoutCodecProvider
     ```
     Text:
     ```text
     software.coley.bentofx.persistence.impl.codec.yaml.provider.YamlLayoutCodecProvider
     ```

   Registering only the `provides` clause is the easier mistake to make, and it fails misleadingly: a class-path application finds no provider at all even though the dependency is present. Keep the two in sync. Renaming one implementation and not the other silently breaks the incorrectly named path.

4. Add the module to the application's runtime dependencies.

   ```kotlin
   runtimeOnly("software.coley.bento-fx:persistence-codec-yaml:${version}")
   ```

   The JAR is discovered on either the module path or the class path depending on how the application is launched.

Additional considerations:

1. **The codec identifier becomes part of how a layout is addressed.** For example, file-backed storage may join it to the layout identifier to form a file name, so it must be compatible that: see [Choosing Stable Identifiers](guide.md#choosing-stable-identifiers). 
2. **Changing codec identifier may orphan layouts** already stored under the old one.
3. **`decode` receives whatever was stored, including nothing useful.** A truncated or invalid encoding must raise `BentoStateException` rather than returning a partly-populated `PersistableLayout`. An exception is treated as "fall back to the default layout" and an incomplete or empty value as "this is the layout".

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

- [Implementation: How a save and a restore exercises codecs and storages, including sequence diagrams](implementation.md)
- [ServiceLoader: The Java 21 API documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)
- [Introduction to the Service Provider Interface](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)
- [Java Service Provider Interface](https://www.baeldung.com/java-spi)
