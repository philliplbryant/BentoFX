[//]: # (TODO BENTO-13 Delete this file)

<style>
  ol { list-style-type: none; counter-reset: item; }
  ol li { display: block; }
  ol li:before { content: counters(item, ".") " "; counter-increment: item; }
</style>

# BentoFX Persistence Source Review

1. Configure Qodana workflow on GitHub to publish results
2. Configure SonarQube workflow on GitHub
3. Analyze code coverage reports
    1. Consider coverage for both normal and exceptional flows
    2. Create additional tests to increase coverage
4. Rerun source review
    1. Exclude files in core and demos/basic
    2. Include Gradle build files
