[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") ") "; counter-increment: item; }
</style>

# Remaining Tasks

1. Revisit [LayoutMenu](./persistence/core/src/main/java/software/coley/bentofx/persistence/core/ui/LayoutsMenu.java)
2. [Update JReleaser Gradle plugin](#once-the-jreleaser-gradle-plugin-update-is-published)
3. Talk to Rich about comments recently added to JRE-17522 and make any required changes.
4. Read and update all documentation - <u>***NOT USING AI***</u>
5. Create a code review with Todd, Rich, Bobby, Yongbo, and Ian

# Once the JReleaser Gradle plugin update is published

The project isolation work is committed: the `ReportAggregationSettingsPlugin`
and `report-aggregation` changes. Everything remaining depends on the JReleaser
Gradle plugin fixes/updates. When the fix is released, do the following.

1. In `gradle/libs.versions.toml`, bump `jreleaser` from `1.25.0` to the
   released version. Confirm it resolves from Maven Central with no
   `mavenLocal()` entry in play, then delete the locally built
   `1.26.0-cc-SNAPSHOT` from the local Maven repository so no stale artifact can
   be picked up by a later build.
2. Set `org.gradle.unsafe.isolated-projects=true` in `gradle.properties` and
   delete the `TODO BENTO-13` comment above it.
3. Drop the configuration cache opt-out in `build.gradle`: the
   `tasks.withType(AbstractJReleaserDefaultTask)` block calling
   `notCompatibleWithConfigurationCache`, its `TODO BENTO-13` comment, and the
   then-unused `AbstractJReleaserDefaultTask` import on line 1. Verify with
   `./gradlew jreleaserConfig` twice in a row and check the second run reports
   `Reusing configuration cache`.
4. Verify from Windows, not WSL, since the graphical integration test suite
   blocks under WSL:
   1. `./gradlew build`
   2. `./gradlew checkAll`
   3. `./gradlew jreleaserConfig`
   4. A full Gradle sync in IntelliJ.
