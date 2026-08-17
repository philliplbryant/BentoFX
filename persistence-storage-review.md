# Review: `persistence/storage`

Scope: both modules under `persistence/storage` - `persistence/storage/file` and
`persistence/storage/db/h2` - covering main sources, module descriptors, build
files, the H2 persistence unit, and the tests those two modules own.
`persistence/api` was read where the storage implementations' correctness depends
on its contracts: what `LayoutStorage` promises its callers, and the order in
which `LayoutStateWriter` and `LayoutStateReader` use it.

Line numbers refer to the files as they stand on `enhancement/issue-13` at
`736d93f`.

Both blockers are fixed, along with M1, M3, M4, every minor except N4, and all
eight nits. The nits are committed; M1 and M3 are verified in the working tree
and not yet committed. N4 is closed as won't fix, and T7 needed no change because
the test [B2](#b2) brought with it already covers it. What remains open is M2, M5
and M6.

M1 and M3 went together, because they are one question asked twice: who owns the
`EntityManagerFactory`. The provider now creates one and keeps it; the storages it
hands out borrow it and no longer close it.

M4 went with [N5](#n5) rather than on its own account: dropping a shipped
credential is only safe once nothing is listening for other processes to use it.

**Two claims in B1 as first written were wrong, and the fix is wider than it
described.** It said the database storage did not have this problem and stood as
the model for the fix. It does have it: its stream commits on `close()`, and
`close()` runs on the failure path too, because the caller closes the stream in a
try-with-resources whose body threw. Measured against a stored layout, with the
codec throwing part way through:

```
PROBE2 saved-good=true
PROBE2 encode-threw=encode failed halfway
PROBE2 stored-after-failure=PARTIAL
```

So both storages replaced a good layout with a fragment, for one shared reason:
`LayoutStateWriter` handed the codec the storage stream. The fix is there - encode
into a buffer, then open storage with the bytes already in hand - and it covers
both implementations. The file storage additionally stages its bytes beside the
target and moves them over it, which is what covers a failure during the write
itself rather than during encoding.

It also said the move would settle [M6](#m6). It does not. No new save leaves a
truncated file behind, but `exists()` still answers for a zero-byte file or a
directory that was there already, so M6 stands on its own.

## Status

**Two blockers, six majors, nine minors, eight nits.** The two blockers are one
per module and unrelated to each other: the file storage destroys the previous
layout when a save fails, and the database storage cannot store a layout whose
identifier is longer than 24 characters.

Everything marked **Measured** was settled by running the code, not by reading
it. The probes were temporary integration tests in each module, run once and
deleted; each measurement below quotes the output it came from. Three candidate
findings did not survive that step and are recorded at the end under
[Withdrawn](#withdrawn) rather than dropped, because two of them look correct
from the source alone and would be raised again by the next reader.

The single most useful change is not any one fix: it is **encoding a layout before
opening storage, and staging the bytes a file-backed save writes** ([B1](#b1)). It
is the only finding here that loses a layout the user already had.

The two implementations disagree more than they agree. Where the database
storage buffers a whole payload and commits it in one transaction, the file
storage streams straight onto the target. Where the database storage tests
`payload.length > 0` before saying a layout exists, the file storage asks the
filesystem. Where `FileLayoutStorage` checks its constructor argument,
`DatabaseLayoutStorage` does not. Six of the findings below are one
implementation being right and the other being wrong, and the fix in each case
is to copy the one that is right.

### BLOCKER

| | Finding | Status |
|---|---|---|
| [B1](#b1) | A failed save leaves the file storage holding a truncated layout, and `exists()` still reports it | **Fixed** 2026-08-16 |
| [B2](#b2) | A layout identifier longer than 24 characters can never be saved to the database | **Fixed** 2026-08-16 |

### MAJOR

| | Finding | Status |
|---|---|---|
| [M1](#m1) | `DatabaseLayoutStorage` closes an `EntityManagerFactory` it did not create | **Fixed** 2026-08-17 |
| [M2](#m2) | A missing row makes `openInputStream` throw `NullPointerException`, not `IOException` | **Open** |
| [M3](#m3) | One `EntityManagerFactory`, and one connection pool, per storage instance | **Fixed** 2026-08-17 |
| [M4](#m4) | `AUTO_SERVER=TRUE` lets other processes connect to the layout database | **Fixed** 2026-08-16 |
| [M5](#m5) | The layout identifier goes into a file path unchecked, so it can leave the directory | **Open** |
| [M6](#m6) | `exists()` is true for an empty file and for a directory | **Open** |

### MINOR

| | Finding | Status |
|---|---|---|
| [N1](#n1) | A `File` with no parent makes `openOutputStream` throw `NullPointerException` | **Fixed** 2026-08-16 |
| [N2](#n2) | `DatabaseLayoutStorage`'s constructor checks nothing, while `FileLayoutStorage`'s checks everything | **Fixed** 2026-08-16 |
| [N3](#n3) | `exists()` reads the whole payload to ask whether it is empty, and the annotation meant to prevent that is inert | **Fixed** 2026-08-16 |
| [N4](#n4) | `hibernate.hbm2ddl.auto=update` migrates the schema of whatever database it finds | **Won't fix** 2026-08-17; see below |
| [N5](#n5) | The persistence unit ships a user name and a password | **Fixed** 2026-08-16 |
| [N6](#n6) | Nothing exercises the shipped persistence unit or the provider that reads it | **Fixed** 2026-08-16 |
| [N7](#n7) | The two modules configure the JavaFX plugin differently for the same reason | **Fixed** 2026-08-16 |
| [N8](#n8) | A database output stream that is never closed saves nothing and says nothing | **Fixed** 2026-08-16 |
| [N9](#n9) | `exists()` can throw from the database storage where the file storage cannot | **Fixed** 2026-08-16 |

### NIT

| | Finding | Status |
|---|---|---|
| [T1](#t1) | `requires transitive java.logging` in a module that does not log | **Fixed** 2026-08-17 |
| [T2](#t2) | `requires static org.hibernate.orm.core` beside a non-static requirement that needs it | **Fixed** 2026-08-17 |
| [T3](#t3) | The entity exposes public mutable fields, including its payload array | **Fixed** 2026-08-17 |
| [T4](#t4) | The inner output stream tracks closure with a plain field, inside a class that uses `AtomicBoolean` | **Fixed** 2026-08-17 |
| [T5](#t5) | `@NullMarked` is applied at three levels, and the module level already covers the rest | **Fixed** 2026-08-17 |
| [T6](#t6) | The two integration tests obtain a temporary directory differently | **Fixed** 2026-08-17 |
| [T7](#t7) | The database integration test's codec identifier is exactly the column width | **Closed by B2** 2026-08-17; see below |
| [T8](#t8) | `DatabaseLayoutStorageIT` dereferences `@Nullable` static fields without checking them | **Fixed** 2026-08-17 |

Every identifier in the four tables links to that finding's own section. The
anchors are explicit rather than derived from the heading text, matching
`persistence-api-review.md` and `persistence-codec-review.md`, so that appending
an outcome to a heading later does not break the link.

---

## BLOCKER

### <a id="b1"></a>B1. A failed save leaves the file storage holding a truncated layout, and `exists()` still reports it - MEASURED

`impl/storage/file/FileLayoutStorage.java:37-40`, with
`persistence/api/.../impl/LayoutStateWriter.java:43-45`

`openOutputStream` calls `Files.newOutputStream(file.toPath())`, whose default
options are `CREATE`, `TRUNCATE_EXISTING` and `WRITE`. The target is emptied the
moment the stream opens. The caller then encodes into it:

```java
try (final OutputStream out = layoutStorage.openOutputStream()) {
    layoutCodec.encode(bentoStateList, out);
}
```

So the previous layout is gone before the new one is known to be writable. Any
failure between those two lines - a codec error, a full disk, the process being
killed, the machine losing power - leaves the file holding however many bytes got
through.

Measured with an existing file holding `PREVIOUS-GOOD-LAYOUT`, opening the stream
the way the writer does, writing part of a payload and then throwing:

```
PROBE encode-threw=encode failed halfway
PROBE after-failed-save-content=partial
PROBE still-reports-exists=true
```

The second line is the data loss: a layout the user had is replaced by a fragment
of one they do not. The third is what makes it worse than a plain failure. On the
next start the restorer asks `doesLayoutExist()`, is told yes, and decodes the
fragment instead of falling back to the default layout, so the user meets a
decode error rather than a working window.

The database storage has the same defect by a different route - see the correction
at the top of this document - so the fix went to the one place both share.
`LayoutStateWriter` now encodes into a buffer and opens storage only once it holds
the finished bytes, so a codec failure never reaches either implementation.
`FileLayoutStorage.openOutputStream` additionally writes to a file beside the
target and moves it over the target when the stream closes cleanly, which is what
covers a failure during the write rather than during encoding: an atomic move
where the filesystem offers one, a plain replace where it does not.

### <a id="b2"></a>B2. A layout identifier longer than 24 characters can never be saved to the database - MEASURED

`impl/storage/db/DockingLayoutEntityCompositeKey.java:18-22`

The composite key's two columns are declared `length = 24` and `length = 4`:

```java
@Column(name = "layout_id", nullable = false, length = 24)
public @Nullable String layoutIdentifier;

@Column(name = "codec_id", nullable = false, length = 4)
public @Nullable String codecIdentifier;
```

Nothing tells the application about either limit. A layout identifier is
whatever the application calls its Bento, and 24 characters is short enough that
an ordinary descriptive name exceeds it. Measured with a 25-character
identifier:

```
PROBE long-layout-id(25)=IOException root: JdbcSQLDataException :
Value too long for column "LAYOUT_ID CHARACTER VARYING(24)":
"'aaaaaaaaaaaaaaaaaaaaaaaaa' (25)"; SQL statement:
insert into docking_layout (payload,updated_at,codec_id,layout_id) values (?,?,?,?)
```

Every save of that layout fails, for the lifetime of the application, with a SQL
error about a column the application has never heard of. It fails loudly - the
stream's `close()` wraps it in `IOException` and `LayoutStateWriter` turns that
into `BentoStateException` - so no data is lost, but nothing is ever stored
either.

The codec identifier's four characters are tighter still, and only escape notice
because both bundled codecs fit: `json` is exactly four and `xml` is three. A
fifth character is enough:

```
PROBE long-codec-id(8)=JdbcSQLDataException :
Value too long for column "CODEC_ID CHARACTER VARYING(4)": "'protobuf' (8)"
```

So the first codec anyone adds with a longer name cannot use database storage at
all. See [T7](#t7) for why the module's own integration test could not have
caught this.

Both columns are now 255, which is what a path component allows on every
mainstream filesystem and therefore the only width these two identifiers have to
respect - they are a file name and its extension in file-backed storage. An
existing database picks the change up as well: `hibernate.hbm2ddl.auto=update`
widens the columns in place, measured by creating the table at the old widths,
opening the persistence unit against it, and reading the width back:

```
PROBE3 existing-layout-id-width=255
PROBE3 existing-save=ok
```

What is still missing is a single statement of what a valid identifier is. 255 is
a ceiling, not a rule: the file storage joins the two with a `.` into one path
component, so the pair has to fit in 255 together rather than each on its own, and
neither storage rejects a separator or a reserved name. See [M5](#m5), which needs
the same validator.

---

## MAJOR

### <a id="m1"></a>M1. `DatabaseLayoutStorage` closes an `EntityManagerFactory` it did not create

`impl/storage/db/DatabaseLayoutStorage.java:34-42, 161-166`

The constructor takes an `EntityManagerFactory` and `close()` closes it:

```java
@Override
public void close() {
    if (closed.compareAndSet(false, true)) {
        emf.close();
    }
}
```

The class is public and exported, so a caller can hand it a factory it is still
using elsewhere - and closing a factory takes its connection pool down with it.
Taking a resource as a constructor argument and then disposing of it is the kind
of ownership that has to be stated, and `LayoutStorage#close()` states the
opposite: it says the storage releases *its* resources.

The module's own integration test demonstrates the hazard by working around it.
`DatabaseLayoutStorageIT` creates one factory in `@BeforeAll`, builds a fresh
`DatabaseLayoutStorage` around it in each `@BeforeEach`, and never calls
`close()` on any of them - it closes the factory itself in `@AfterAll`. A test
that closed the storage under test would break every test after it.

The `close()` override is gone rather than rewritten. Every entity manager the
class opens is closed where it is opened, so once the factory is not its to close
there is nothing left to release, and `LayoutStorage.close()` already defaults to
doing nothing. The class Javadoc and the constructor's `@param` now say the factory
stays the caller's, which is the statement of ownership this finding asked for.

The test the workaround stood in for exists now:
`closingOneStorageLeavesTheSharedFactoryOpen` stores a layout, builds a second
storage on the same factory, closes it in a try-with-resources the way an owning
component would, and then reads the layout back through the first. Measured against
the previous `close()`, it fails on the factory and names the consequence in the
suppressed exception:

```
[the factory after a storage that used it was closed]
Expecting value to be true but was false
	Suppressed: java.lang.IllegalStateException: EntityManagerFactory is closed
```

### <a id="m2"></a>M2. A missing row makes `openInputStream` throw `NullPointerException`, not `IOException` - MEASURED

`impl/storage/db/DatabaseLayoutStorage.java:84-90`

`em.find` returns `null` when there is no row, and the next line dereferences it:

```java
final DockingLayoutEntity entity = em.find(DockingLayoutEntity.class, key);

return new ByteArrayInputStream(entity.payload);
```

Measured against a database with no matching row:

```
PROBE exists=false
PROBE missing-row-read=java.lang.NullPointerException :
Cannot read field "payload" because "entity" is null
```

`LayoutStorage.openInputStream` declares `IOException` for exactly this, and the
file implementation honours it - a missing file arrives as `NoSuchFileException`,
which `LayoutStateReader` wraps into `BentoStateException`. The database
implementation instead lets an unchecked exception past a reader that catches
only `IOException`, so the caller gets a bare `NullPointerException` whose
message is about a field name.

`DockingLayoutRestorer.restoreLayout` does gate on `doesLayoutExist()`, so this
is not reached on the ordinary path. Two things reach it. A caller using
`LayoutStorage` directly, which the interface is public and exported for. And the
gap between the `exists()` query and the `openInputStream` query, which is a real
window rather than a theoretical one, because [M4](#m4) configures the database
for access from more than one process at a time.

### <a id="m3"></a>M3. One `EntityManagerFactory`, and one connection pool, per storage instance

`impl/storage/db/provider/DatabaseLayoutStorageProvider.java:27-38`, with
`META-INF/persistence.xml:29-32`

The provider builds a factory on every call:

```java
final EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("bentoLayout");

return new DatabaseLayoutStorage(entityManagerFactory, layoutIdentifier, codecIdentifier);
```

`LayoutStorageProvider.getLayoutStorage` requires a fresh `LayoutStorage` per
call, because the saver and the restorer each close the one they were given. The
provider satisfies that by also building a fresh factory, and a factory is the
expensive thing: it starts Hibernate, and the persistence unit gives each one a
Hikari pool with `minimumIdle` 2 and `maximumPoolSize` 10.

An application with a saver and a restorer therefore runs two Hibernate
factories and two pools, holding at least four idle connections and permitting
twenty, against a single embedded database that one connection would serve. What
has to be per-component is the `LayoutStorage`, not the factory underneath it.

The provider now creates the factory on the first request and keeps it, handing
each caller a fresh `DatabaseLayoutStorage` around the same one. `getLayoutStorage`
is `synchronized` because that first call is what creates the factory and a saver
and a restorer need not be built on one thread. This only works because [M1](#m1)
went with it - a shared factory that any storage may close is worse than a factory
each.

Measured by pool, since Hikari numbers each one it starts. Running the provider
test that asks one provider for two storages, before and after:

```
BEFORE  HikariPool-1 - Start
        HikariPool-2 - Start
AFTER   HikariPool-1 - Start
```

What this does not add is a way to close the factory before the JVM exits. There
is no earlier moment available: the storages belong to components that outlive
individual saves, and `LayoutStorageProvider` has no shutdown to hook into. One
factory for the life of the application is the intended cost, and the provider's
Javadoc says so; giving the API a shutdown is a separate change affecting every
provider, including the file one that needs nothing of the sort.

### <a id="m4"></a>M4. `AUTO_SERVER=TRUE` lets other processes connect to the layout database

`META-INF/persistence.xml:17-18`

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:h2:file:${user.home}/.bentofx/bento-layouts;AUTO_SERVER=TRUE"/>
```

`AUTO_SERVER=TRUE` puts H2 into automatic mixed mode: the first process to open
the database also starts a TCP server for it and records the port in the lock
file beside the database, so that other processes can connect. For a store whose
entire contents are one desktop application's window layout, that trades a
listening socket and a shared credential ([N5](#n5)) for a capability the
application does not appear to want.

It also turns [M2](#m2) from a latent defect into a reachable one, since two
processes can now read and write the same row, and makes the single-writer
assumption behind `exists()`-then-read a question rather than a given.

### <a id="m5"></a>M5. The layout identifier goes into a file path unchecked, so it can leave the directory - MEASURED

`impl/storage/file/provider/FileLayoutStorageProvider.java:33-47`

The identifier is concatenated into a filename with no validation:

```java
final File layoutFile = new File(
        DEFAULT_BENTO_DIRECTORY,
        layoutIdentifier + "." + normalizedFileExtension
);
```

`new File(parent, child)` resolves `..` in the child, so an identifier that
contains a path segment writes outside the directory the provider chose.
Measured with the provider's own construction, against a directory standing in
for `~/.bentofx`:

```
PROBE traversal-target=C:\Dev\Temp\escaped.json
PROBE traversal-inside-bento-directory=false
```

The codec identifier gets a normalisation pass for a leading `.` two lines
above, so the code is already thinking about what these strings contain; the
identifier that decides the filename gets none. A separator or a `..` segment
should be rejected, or the resolved path checked against the directory before it
is used.

### <a id="m6"></a>M6. `exists()` is true for an empty file and for a directory - MEASURED

`impl/storage/file/FileLayoutStorage.java:32-34`

```java
@Override
public boolean exists() {
    return file.exists();
}
```

`File.exists()` answers a different question from the one the restorer asks.
`DockingLayoutRestorer.restoreLayout` uses it to choose between restoring a saved
layout and building the default one, so what it needs to know is whether there
is a layout to read. Measured:

```
PROBE empty-file-exists=true
PROBE directory-exists=true
PROBE directory-read=AccessDeniedException
```

A zero-byte file - which is what [B1](#b1) leaves behind if a save fails before
any bytes are written - reports as a layout, and the user gets a decode failure
instead of a default layout. A directory with the layout's name reports as a
layout too, and then fails on read.

The database implementation gets this right, and the contrast is one line:
`entity != null && entity.payload.length > 0`
(`impl/storage/db/DatabaseLayoutStorage.java:63`). The file implementation needs
the same `isFile()` and non-empty test.

---

## MINOR

### <a id="n1"></a>N1. A `File` with no parent makes `openOutputStream` throw `NullPointerException` - MEASURED

`impl/storage/file/FileLayoutStorage.java:38`

```java
Files.createDirectories(file.toPath().getParent());
```

`getParent()` is `null` for a path with a single name element, and
`createDirectories(null)` throws. Measured with `new File("layout.json")`:

```
PROBE no-parent=java.lang.NullPointerException :
Cannot invoke "java.nio.file.Path.getFileSystem()" because "path" is null
```

The bundled provider always builds an absolute path, so this needs a caller
constructing `FileLayoutStorage` directly - which is what the class being public
and exported invites, and a bare filename is an ordinary thing to hand a
file-backed store.

Closed by [B1](#b1) rather than on its own account: staging the write needs the
target's directory, so the method resolves the path with `toAbsolutePath()` before
asking for the parent, and a bare filename now writes to the working directory
instead of throwing.

### <a id="n2"></a>N2. `DatabaseLayoutStorage`'s constructor checks nothing, while `FileLayoutStorage`'s checks everything

`impl/storage/db/DatabaseLayoutStorage.java:34-42` against
`impl/storage/file/FileLayoutStorage.java:27-29`

The file storage rejects a null file at construction. The database storage
assigns all three of its arguments unchecked, so a null factory surfaces later as
a `NullPointerException` from whichever method is called first, and a null
identifier surfaces from inside the key's own `requireNonNull` with no indication
of which of the two identifiers was missing. The codec review settled the same
question the other way, giving each check the name of what it rejected.

All three arguments are checked now, each named, and the constructor has the
Javadoc it was missing.

### <a id="n3"></a>N3. `exists()` reads the whole payload to ask whether it is empty, and the annotation meant to prevent that is inert

`impl/storage/db/DatabaseLayoutStorage.java:54-63`, with
`impl/storage/db/DockingLayoutEntity.java:27-30`

`exists()` loads the entity and then reads `entity.payload.length`. The payload
is annotated to load lazily:

```java
@Lob
@Basic(fetch = FetchType.LAZY)
@Column(name = "payload", nullable = false)
public byte[] payload = new byte[0];
```

Lazy fetching of a basic attribute needs bytecode enhancement, which no build in
this repository configures, so Hibernate ignores the hint and fetches the column
with the row. Asking whether a layout exists therefore pulls the entire encoded
layout out of the database and discards it.

Both halves are fixed together. `exists()` now asks the database for the
payload's length and never materialises it, and `@Basic(fetch = FetchType.LAZY)`
is gone rather than left reading as though it does something.

The length query is native, because JPQL's `length` takes a string and Hibernate
rejects it on a blob:

```
FunctionArgumentException: Parameter 1 of function 'character_length()' has type
'STRING_OR_CLOB', but argument is of type 'byte[]' mapped to 'BLOB'
```

This is the H2 module, so H2-specific SQL is at home in it, and the statement is
assembled from the table and column names the mapping declares so the two cannot
drift apart.

### <a id="n4"></a>N4. `hibernate.hbm2ddl.auto=update` migrates the schema of whatever database it finds

`META-INF/persistence.xml:41-42`

`update` compares the mapping against the database at startup and issues the DDL
it thinks is missing. For the embedded default that is a convenience; against any
database an application points this persistence unit at, it is a schema change
made without review, and one that never removes or narrows anything, so a
database that has drifted stays drifted. `validate`, with the schema created
deliberately, is the setting that matches what this module actually needs, which
is one table.

**Closed as won't fix.** `update` is what creates that one table on a first run,
and it is also what carried [B2](#b2) to databases that already existed - measured,
it widened `layout_id` from 24 to 255 in place. Replacing it means shipping a
script that both creates the table and alters the columns, which is a migration
file to maintain for a module with one table and no released version. The exposure
it leaves is an application repointing this persistence unit at a database it
shares with something else, which is not a use this module offers: the URL names a
private file under the user's home.

### <a id="n5"></a>N5. The persistence unit ships a user name and a password

`META-INF/persistence.xml:19-22`

```xml
<property name="jakarta.persistence.jdbc.user" value="sa"/>
<property name="jakarta.persistence.jdbc.password" value="password"/>
```

For an embedded file database these are not much of a secret, but they are
published in the artifact, they are the same for every installation, and
[M4](#m4) makes them the credential on a listening socket. They also cannot be
changed without replacing the persistence unit, since nothing reads them from a
property or the environment.

Both properties are gone, and so is `AUTO_SERVER=TRUE` with them: a credential is
worth having when something can connect using it, and nothing should be connecting
to one application's own layout file. What guards the database now is the
permissions on the file, which is what guarded it in practice all along. The
persistence unit says as much where the URL is declared.

Nothing existing has to be migrated, because `persistence` has never shipped -
`persistence/` is absent from both `master` and `upstream/master`. Anyone holding a
database created with the old credentials would have to delete it, which is worth
knowing if that includes a development machine.

### <a id="n6"></a>N6. Nothing exercises the shipped persistence unit or the provider that reads it

`impl/storage/db/provider/DatabaseLayoutStorageProvider.java`, with
`src/it/.../DatabaseLayoutStorageIT.java:53-60`

The integration test overrides the JDBC URL to a temporary directory, which is
right for a test, and means the shipped URL - placeholder, `AUTO_SERVER`,
credentials and all - is never opened by the build. `DatabaseLayoutStorageProvider`
has no test at all, so the one line that reads the persistence unit by name is
unexercised too. Compare the file module, which at least asserts the path its
provider constructs.

That the shipped configuration works was established here by measurement rather
than by the suite - see [Withdrawn](#withdrawn), W1.

`DatabaseLayoutStorageProviderIT` now covers it. It points `user.home` at a
temporary directory, asks the provider for storage, writes and reads a layout
through it, and checks that the database was created under that home - so the
placeholder, the absence of credentials, the schema creation and the provider's own
line are all exercised. It also pins the contract that each call hands back a
fresh instance, since two components closing the same one is what
`LayoutStorageProvider` warns against.

### <a id="n7"></a>N7. The two modules configure the JavaFX plugin differently for the same reason

`storage/file/build.gradle:11-16` against `storage/db/h2/build.gradle:10-13`

Neither storage module uses JavaFX. Both need it on a path only because the
persistence API depends on `core`, which declares `javafx.controls` as
`compileOnlyApi`. The file module says so in a comment and asks for
`javafx.graphics` on `compileOnly`; the h2 module asks for `javafx.controls` on
`runtimeOnly` and says nothing. One of the two is wrong about which module and
which configuration the dependency needs, and the comment only exists in the one
that is easier to justify.

Both were wrong, as it turns out: neither module needs JavaFX at all. Measured by
deleting the plugin and its configuration from each in turn - both compile, both
run their integration tests, and the whole build passes with them gone. `core`
requires the JavaFX modules without `transitive`, so nothing that only reads
`persistence.api` has to resolve them, and the application supplies them anyway.
The h2 module also stops publishing a JavaFX runtime dependency it never used.

### <a id="n8"></a>N8. A database output stream that is never closed saves nothing and says nothing

`impl/storage/db/DatabaseLayoutStorage.java:95-158`

The returned stream buffers into memory and does all its work in `close()`.
A caller that writes and never closes has silently not saved, with no error and
no row. The interface does say the caller owns the stream, and
`LayoutStateWriter` uses try-with-resources, so this is latent rather than live -
but it is a mode the file implementation does not have, where bytes written are
bytes on disk, and it deserves a line of documentation on the override.

The override now says it. Note that the file storage acquired the same property
with [B1](#b1): staging means closing is what promotes there too, so the two
implementations now agree, and the one line documents what both do.

### <a id="n9"></a>N9. `exists()` can throw from the database storage where the file storage cannot

`impl/storage/db/DatabaseLayoutStorage.java:45-65`

`exists()` returns `boolean` and declares nothing. The database implementation
opens an `EntityManager` and runs a query, so an unreachable database, a bad
credential or a failed schema update all leave it throwing an unchecked
`PersistenceException` through a method that reads like a question about state.
`DockingLayoutRestorer.doesLayoutExist` passes it straight to the application.
Either the interface should declare what the answer costs, or the implementation
should treat a database it cannot reach as "no layout here".

The interface declares it. Swallowing the failure was the alternative and it is
worse: an implementation that answers "no layout" when it cannot reach its store
has the restorer build a default layout, and the next save writes that over a
layout that was there all along. `LayoutStorage.exists()` now says that `false`
means no layout rather than no answer, and that an unreachable store throws.

---

## NIT

### <a id="t1"></a>T1. `requires transitive java.logging` in a module that does not log

`storage/file/src/main/java/module-info.java:17`

No source in the module imports `java.util.logging`, and the module has no
logging of its own. The requirement is dead, and `transitive` passes it to every
consumer.

Gone. Nothing in the module referred to it, so nothing had to change with it.

### <a id="t2"></a>T2. `requires static org.hibernate.orm.core` beside a non-static requirement that needs it

`storage/db/h2/src/main/java/module-info.java:18, 27`

`org.hibernate.orm.core` is optional at runtime, and
`org.hibernate.orm.hikaricp` two lines below it is not - and hikaricp is a
Hibernate module that cannot resolve without core. The `static` therefore
describes something that is never true.

The requirement is now unqualified, sitting with the other runtime requirements
and carrying a line saying why it cannot be optional. The Gradle dependency stays
`compileOnly`: hibernate-core reaches the runtime path transitively through
hibernate-hikaricp, which is an `implementation` dependency, so `buildHealth` has
nothing to say about the change and the integration tests run against the same
resolution the application gets.

### <a id="t3"></a>T3. The entity exposes public mutable fields, including its payload array

`impl/storage/db/DockingLayoutEntity.java:24-33`

`key`, `payload` and `updatedAt` are public, and `payload` is a `byte[]`, so any
holder of the entity can change the stored layout in place. The DTOs in
`persistence/codec` are public-field carriers too, which is a defensible choice
for a mapping target, but they are not handed out by a public API the way this
entity is.

The class and its three fields are package-private now, which costs nothing:
`DatabaseLayoutStorage` is the only code that touches them and it sits in the same
package, so no accessors had to be written. Field-access mapping is unaffected,
and Hibernate still reaches the class through the `opens` directive the module
descriptor already had. Measured by the module's own integration tests, which
store and read layouts through the shipped persistence unit and pass unchanged.

`DockingLayoutEntityCompositeKey` still has public mutable fields. It is not
reachable from outside either - nothing hands one out, and the two identifiers are
copied into it at construction - so it is left alone rather than dragged into a
nit about the entity.

### <a id="t4"></a>T4. The inner output stream tracks closure with a plain field, inside a class that uses `AtomicBoolean`

`impl/storage/db/DatabaseLayoutStorage.java:98, 30`

The anonymous `ByteArrayOutputStream` guards double-close with
`private boolean closed`, ten lines below a class that guards its own with
`AtomicBoolean`. Streams are not usually shared between threads, so this is
about the file reading consistently rather than about a race.

The stream now uses an `AtomicBoolean` and the same `compareAndSet` idiom as the
enclosing class, which also folds the test and the assignment into one line.

### <a id="t5"></a>T5. `@NullMarked` is applied at three levels, and the module level already covers the rest

`storage/file/src/main/java/module-info.java:12`,
`impl/storage/file/provider/package-info.java:6`,
`impl/storage/file/package-info.java`, `impl/storage/db/package-info.java`

Both modules are `@NullMarked`, which covers their packages. On top of that, one
of the four packages repeats it and three do not, and the h2 provider package has
no `package-info.java` at all. Whichever level the project wants, the other two
should be consistent.

Module level is the one the rest of `persistence` uses - every module descriptor
carries `@NullMarked`, and the only main packages that repeat it are the two codec
`mixins` packages - so the repeat came off the file provider package. The h2
provider package got the `package-info.java` it was missing, with the Javadoc the
other packages have and no annotation. NullAway is configured `onlyNullMarked`, so
the check still covers both modules through their descriptors; the build confirms
it.

### <a id="t6"></a>T6. The two integration tests obtain a temporary directory differently

`src/it/.../DatabaseLayoutStorageIT.java:46-47` against
`src/it/.../FileLayoutStorageIT.java:22-41`

The database test uses JUnit's `@TempDir` and lets the framework clean up. The
file test calls `Files.createTempDirectory` and then deletes the file and the
directory by hand in `@AfterEach`, which is more code for less: a failure that
leaves an extra file behind also leaves the directory undeleted.

The file test takes a `@TempDir` too, and its `@AfterEach` is gone: eighteen lines
of hand-rolled cleanup replaced by a field, and the staged writes an abandoned
stream leaves behind are cleaned up because JUnit removes the directory
recursively.

### <a id="t7"></a>T7. The database integration test's codec identifier is exactly the column width

`src/it/.../DatabaseLayoutStorageIT.java:30`

```java
private static final String TEST_CODEC_IDENTIFIER = "none";
```

Four characters, against a `length = 4` column. The test could not have found
[B2](#b2) with either of its identifiers, and the layout identifier it uses is
`test-layout`, eleven characters against a limit of 24. Fixture values that sit
inside a limit by coincidence are how a limit stays undiscovered.

**Closed by [B2](#b2), with no change here.** That fix widened both columns to 255,
so neither shared fixture value sits at a column width any more, and it added
`storesDescriptiveIdentifiers` to this same test class: a 53-character layout
identifier and a five-character codec identifier, stored and read back, each
checked against `MAX_COMPOSITE_KEY_LENGTH`. That test fails at the old widths,
which is what this nit asked for. Lengthening the shared fixtures on top of it
would change the values the other three tests use and cover nothing new.

### <a id="t8"></a>T8. `DatabaseLayoutStorageIT` dereferences `@Nullable` static fields without checking them

`src/it/.../DatabaseLayoutStorageIT.java:46-51`, used at `88`, `122`, `157`

`temporaryDirectory`, `entityManagerFactory` and `storage` are declared
`@Nullable` and then used directly. The annotation is doing the opposite of its
job: it says these may be absent while the code assumes they are not. The
lifecycle guarantees they are set; the declaration should say so.

All three declarations drop the annotation, and the import goes with them. The one
place that did check, `tearDownAll`'s `entityManagerFactory != null`, is gone too,
since a factory that failed to open is a setup failure worth seeing rather than one
to step around - which is how `DatabaseLayoutStorageArgumentIT` already treats it.
The two other integration tests in these modules keep their `@Nullable` fields;
this nit named only this class, and the same argument applies to them whenever
someone wants it applied.

---

## <a id="withdrawn"></a>Withdrawn

**W1. `${user.home}` in the JDBC URL does resolve.** The persistence unit's URL
contains `${user.home}`, and the JPA specification says nothing about expanding
placeholders in `persistence.xml`, so the database looked like it might be
created in a literal `${user.home}` directory beside the working directory.
Hibernate resolves it from system properties. Measured by pointing `user.home` at
a temporary directory and opening the shipped persistence unit by name:

```
PROBE emf-open=true
PROBE db-under-fake-home=true
```

with no stray path anywhere under the working directory. The placeholder works;
[N6](#n6) stands only because the build never checks it.

**W2. `openInputStream` does not read a lazy field after its `EntityManager`
closed.** The method returns `new ByteArrayInputStream(entity.payload)` from
inside `try (final EntityManager em = ...)`, and the payload is annotated
`FetchType.LAZY`, which reads like a lazy load racing the close. It is not: the
array is read inside the try block, and per [N3](#n3) the lazy hint is inert, so
the bytes are already in memory. The integration test's read path passes for this
reason.

**W3. A found row cannot make `exists()` throw.** `exists()` reads
`entity.payload.length` after a null check on the entity, so the remaining
question is whether `payload` can be null on a row that exists. It cannot: the
field is initialised to `new byte[0]` and the column is `nullable = false`, so a
row with a null payload cannot be written through this entity.
