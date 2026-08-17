[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") " "; counter-increment: item; }
</style>

# Remaining Tasks

1. Claude Code 
   1. Perform a code review.
      1. Exclude files in `core`, `demos/basic`, and `demos/persistence/resources`.
      2. Include documentation.
      3. Include Gradle build files.
      4. Include GitHub workflows.
      5. Initially perform on a per-module basis
         1. [x] `persistenc/api`
         2. [x] `persistence/codec`
         3. [x] `persistence/storage`
         4. [x] `persistence/test-fixtures`
         5. [x] `demos/persistence`
         6. [ ] Everything as is combined
2. Update all documentation.
3. Configure `JReleaser` plugin to publish snapshots.
   1. https://nexus.jre.saic.com/#browse/browse:jre-central-snapshots
   2. Use environment variables
      1. `NEXUS_USERNAME`
      2. `NEXUS_PASSWORD`
4. Analyze code coverage reports.
   1. Exclude files in `core` and `demos`.
   2. Create additional tests to increase coverage.
   3. Consider coverage for both normal and exceptional flows.
5. Create a code review with Todd, Rich, Bobby, Yongbo, and Ian
