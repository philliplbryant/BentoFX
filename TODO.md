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
3. Created an issue and branch (not PR) for CI actions in my fork that aren't in Col-E's
   1. He has zero tests or build infrastructure in place to run GUI based tests
   2. He has zero static code analysis or quality gates in place
   3. Offered for his use
      1. Told him the Gradle and CI would remain in my fork either way
      2. The Gradle and CI changes make merging his changes into my fork more costly

# Remaining Tasks

1. Read and update all documentation - <u>***NOT USING AI***</u>
2. Talk to Rich about comments recently added to JRE-17522 and make any required changes.
3. Create a code review with Todd, Rich, Bobby, Yongbo, and Ian.
4. Follow up with Col-E:
   1. Restore Gradle configurations - [enhancement/BENTO-35-update](https://github.com/philliplbryant/BentoFX/tree/enhancement/BENTO-35-update) 
   2. [Issue 43: Static Code Analysis and Quality Gates](https://github.com/Col-E/BentoFX/issues/43)
5. Add a merged JaCoCo report to `bento.report.jacoco-aggregation.gradle`: one
   `JacocoReport` task taking `executionData` from every suite's `.exec` files,
   alongside the existing four. Upload only that one to Codecov.
   1. `JacocoMerge` was removed in Gradle 7. Feeding one `JacocoReport` all the
      exec files is the replacement, and JaCoCo unions the probe data itself.
   2. Two payoffs: exact branch coverage rather than a bounded range, since
      branch identity survives in the binary but not in the XML, and one file to
      upload instead of four, which removes the `files:` parsing fragility that
      silently dropped three of them.
   3. Resolve the exec files through the `aggregateCodeCoverageReportResults`
      configuration, not by reading sibling tasks at configuration time, or it
      will break the configuration cache.
   4. Keep the four per-suite reports. They are what showed
      `integrationTestGraphical` carries most of the coverage.
   5. Wait until Col-E answers on `enhancement/BENTO-35-update` and issue 43,
      then do it as its own commit so that branch's diff stays clean.
6. Update the JReleaser Gradle plugin once it is released with the changes for [Issue 2150](https://github.com/jreleaser/jreleaser/issues/2150).

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
