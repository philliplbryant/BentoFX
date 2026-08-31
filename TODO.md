[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") ") "; counter-increment: item; }
</style>

# Remaining Tasks

1. Update the enhancement/issue-13 branch to branch from the original Gradle PR branch.
   1. https://github.com/Col-E/BentoFX/pull/36 is the original pull request containing the Gradle changes.
   2. These changes were reverted from master.
   3. I have made additional Gradle changes since then. 
   4. col-e wants me to "toss it up on a branch off of a prior commit for gradle changes" 
2. Revisit [LayoutMenu](./persistence/core/src/main/java/software/coley/bentofx/persistence/core/ui/LayoutsMenu.java)
3. Talk to Rich about comments recently added to JRE-17522 and make any required changes.
4. Read and update all documentation - <u>***NOT USING AI***</u>
5. Update the JReleaser Gradle plugin once it is released with the changes I requested.
6. Create a code review with Todd, Rich, Bobby, Yongbo, and Ian.

# Once the JReleaser Gradle plugin update is published

The project isolation work is committed: the `ReportAggregationSettingsPlugin`
and `report-aggregation` changes. Everything remaining depends on the JReleaser
Gradle plugin fixes/updates. When the fix is released, do the following.

1. In `gradle/libs.versions.toml`, bump `jreleaser` from `1.25.0` to the
   released version. Confirm it resolves from Maven Central.
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
5. Run a Gradle scan on all JReleaser tasks that I can run without keys.
