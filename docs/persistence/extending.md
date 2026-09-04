# Extending Persistence

[&larr; Back to the BentoFX Persistence guide](guide.md)

For writing a new codec or storage implementation. Applications that use the bundled codecs and storage need nothing here - see [Usage](guide.md#persistence-usage) instead.

The `DefaultDockingLayoutPersistenceProvider` uses `ServiceLoader` to acquire `LayoutCodecProvider` and `LayoutStorageProvider` implementations from the runtime module path, or from the classpath for non-modularized applications. Provider identifiers allow applications to select a specific codec or storage implementation when more than one implementation is available.

To add a storage destination:

1. Implement the `LayoutStorage` interface:

```java
public class SystemLayoutStorage implements LayoutStorage {
    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public OutputStream openOutputStream() {
        return System.out;
    }

    @Override
    public InputStream openInputStream() {
        return System.in;
    }
}
```

2. Implement the `LayoutStorageProvider` service provider interface:

```java
public class SystemLayoutStorageProvider implements LayoutStorageProvider {
    @Override
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        return new SystemLayoutStorage();
    }
}
```

Four conventions are worth following in a storage implementation, because the bundled implementations follow them and callers rely on them:

1. **Closing the output stream is what stores the layout.** Buffer or stage what is written and publish it only when the stream closes cleanly. A save that fails part way through then leaves the previously stored layout intact instead of replacing it with a fragment.
2. **Override the catalog methods when the destination can answer them.** `LayoutStorageProvider.getLayoutIdentifiers`, `isLayoutStored` and `deleteLayout` all have defaults, so a storage implementation stays valid without them, but an application cannot offer users a list of saved layouts unless the storage it uses can enumerate. Both bundled implementations can: one file per layout, or one row per layout and codec.
3. **`exists()` answers whether there is a layout to read**, not whether a location is present. Empty content is not a layout: a restorer told that a layout exists will try to decode it, and an empty or truncated payload becomes a decode failure where a clean "nothing stored yet" would have produced the default layout.
4. **`close()` releases what the storage owns, and only that.** Whichever saver or restorer receives a `LayoutStorage` closes it, so a storage handed a resource it did not create should leave that resource alone.

The `LayoutStorageProvider` implementation is the type discovered by `ServiceLoader`. It must expose a stable identifier and be compatible with Java's Service Provider Interface (SPI) mechanism. In practice, the provider implementation should:

* be a public concrete class
* have a public no-argument constructor, or an implicit default constructor
* return a stable provider identifier from `getIdentifier()`
* optionally return `true` from `isDefault()` when it should be selected automatically from multiple providers
* be registered with a `provides` clause in the module descriptor

The `LayoutStorage` implementation itself is not discovered directly by `ServiceLoader`; it is created by the `LayoutStorageProvider`. This allows a storage implementation to use constructor arguments or other setup logic when the provider creates it.

3. Register the provider implementation with the module descriptor:

```java
provides LayoutStorageProvider with SystemLayoutStorageProvider;
```

4. Add the module to the application's runtime module path:

```kotlin
runtimeOnly("software.coley.bento-fx:persistence-storage-system:${version}")
```

Codecs are extended similarly by implementing `LayoutCodecProvider` and `LayoutCodec`, registering the provider with the implementation module's descriptor, and adding the module to the application's runtime module path. The same SPI compatibility and identifier requirements apply to the `LayoutCodecProvider`; the `LayoutCodec` implementation is created by the provider and does not need to be directly discoverable by `ServiceLoader`.

For complete examples, refer to these modules:

- [JSON Codec](../../persistence/codec/json)
- [XML Codec](../../persistence/codec/xml)
- [H2 Database Storage](../../persistence/storage/db/h2)
- [File Storage](../../persistence/storage/file)

Additional API and usage documentation can be found in [Docking Layout Persistence Implementation](implementation.md) and [Bento layout persistence diagrams](diagrams.md).

The following are also provided for additional information on using `ServiceLoader`:

* https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
* https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html
* https://www.baeldung.com/java-spi
