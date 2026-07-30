[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") " "; counter-increment: item; }
</style>

# Remaining BentoFX Persistence Tasks

1. Create a snapshots repo in JRE Nexus to test the JReleaser plugin.
2. Configure SonarQube workflow on GitHub.
3. Use Clause Code to analyze code coverage reports.
   1. Create additional tests to increase coverage.
   2. Consider coverage for both normal and exceptional flows.
4. Run code review in Claude Code.
   1. Exclude files in core and demos/basic.
   2. Include Gradle build files.
