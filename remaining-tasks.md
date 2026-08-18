[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") " "; counter-increment: item; }
</style>

# Remaining Tasks

1. Add a menu item to allow saving/restoring multiple layouts.
2. Configure `JReleaser` plugin to publish snapshots.
   1. https://nexus.jre.saic.com/#browse/browse:jre-central-snapshots
   2. Use environment variables
      1. `NEXUS_USERNAME`
      2. `NEXUS_PASSWORD`
3. Analyze code coverage reports.
   1. Exclude files in `core` and `demos`.
   2. Create additional tests to increase coverage.
   3. Consider coverage for both normal and exceptional flows.
4. Read and update all documentation - <u>***NOT USING AI***</u>
5. Create a code review with Todd, Rich, Bobby, Yongbo, and Ian
