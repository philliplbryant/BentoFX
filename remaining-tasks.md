[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") " "; counter-increment: item; }
</style>

# Remaining Tasks

1. Configure JReleaser plugin to publish snapshots.
   1. https://nexus.jre.saic.com/#browse/browse:jre-central-snapshots
   2. Use environment variables
      1. NEXUS_USERNAME
      2. NEXUS_PASSWORD
2. Update all documentation.
3. Claude Code 
   1. Perform a code review.
      1. Exclude files in core and demos/basic.
      2. Include documentation.
      3. Include Gradle build files.
      4. Include GitHub workflows.
      5. Initially perform on a per-module basis
         - [x] persistenc/api
         - [ ] persistence/codec
         - [ ] persistence/storage
         - [ ] persistence/test-fixtures
         - [ ] demos/persistence
   2. Analyze code coverage reports.
      1. Exclude files in core and demos/basic.
      2. Create additional tests to increase coverage.
      3. Consider coverage for both normal and exceptional flows.
   3. Analyze code coverage reports for files in core and demos/basic.
      1. Create additional tests to increase coverage.
      2. Consider coverage for both normal and exceptional flows.
