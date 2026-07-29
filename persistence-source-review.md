[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") " "; counter-increment: item; }
</style>

# BentoFX Persistence Source Review

1. Remove options/properties for running in CI mode and collecting coverage reports.
    1. Always run in CI mode.
   2. Always collect coverage reports.
2. Configure SonarQube workflow on GitHub.
3. Analyze code coverage reports.
    1. Consider coverage for both normal and exceptional flows.
    2. Create additional tests to increase coverage.
4. Run code review in Claude Code.
    1. Exclude files in core and demos/basic.
    2. Include Gradle build files.
