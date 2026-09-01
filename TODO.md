[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") ") "; counter-increment: item; }
</style>

# Status

1. Published [snapshots](https://nexus.jre.saic.com/#browse/browse:jre-central-snapshots:software%2Fcoley%2Fbento-fx) of all BentoFX artifacts to JRE Nexus repo for others to use for testing persistence.  
2. Pushed modifications to a branch off the original Gradle PR branch so:
   1. Col-E can merge them to master
   2. I can rebase my master branch to match his
   3. I can more easily merge his changes from master to the enhancement/issue-13 branch

# Remaining Tasks

1. Revisit [LayoutMenu](./persistence/core/src/main/java/software/coley/bentofx/persistence/core/ui/LayoutsMenu.java)
2. Talk to Rich about comments recently added to JRE-17522 and make any required changes.
3. Read and update all documentation - <u>***NOT USING AI***</u>
4. Update the JReleaser Gradle plugin once it is released with the changes I requested. 
   1. I submitted [Issue 2150](https://github.com/jreleaser/jreleaser/issues/2150) with an approach to fixing remaining issues.
5. Submit a PR to Col-E for [Issue 43: Static Code Analysis and Quality Gates](https://github.com/Col-E/BentoFX/issues/43)
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
