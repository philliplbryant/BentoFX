# Contributing to BentoFX

## Table of Contents

- [License](#license)
- [Coding Guidelines](#coding-guidelines)
- [Automated Testing](#automated-testing)
  - [Unit Tests](#unit-tests)
  - [Integration Tests](#integration-tests)
  - [Parallel Integration Tests](#parallel-integration-tests)
  - [Functional Tests](#functional-tests)
- [JSpecify Nullness Analysis](#jspecify-nullness-analysis)
- [Code Coverage](#code-coverage)
- [GitHub Workflows](#github-workflows)
  - [Build](#build-workflow)
  - [Automated Dependency Updates](#automated-dependency-updates)
  - [Checking for Dependency Updates](#checking-for-dependency-updates)

## License

BentoFX is licensed under the MIT License. Anything you contribute will thus also be under the MIT license.

## Coding Guidelines

The following is a series of guidelines for contributing to BentoFX.
They're not _"rules"_ per se, rather they're more like goals to strive towards.

**Style**: IDE code formatting rules can be found in the [`/setup` directory](setup).
If using IntelliJ or Eclipse you should be able to import one of the provided files.

**Commits**: Try to keep commits small and focused on a single change. 
This makes it easier for reviewers to understand the context and purpose of each commit.
I know some features are large in scope, just break down what you can where possible.

## Automated Testing

Automated tests are categorized as defined below, based on their purposes and the conditions under which they can be reliably executed.

<h3 id="unit-tests">Unit Tests (Test)</h3> 

Projects that apply the [project convention](./build-logic/src/main/groovy/bento.project.project-convention.gradle) are configured to run unit tests.  
Unit test classes must have names ending with `Test` and be located in the `src/test/java` directory. These tests are run in parallel.

Unit tests:  
- Focus on a single, small piece of functionality, such as a class, function, or method.  
- Run independently of external dependencies (e.g., databases, APIs, or other modules), often using mocks or stubs.  
- Execute very quickly.  
- Must be able to run in parallel reliably.

> 💡 **Tips for Execution**  
> Unit tests are executed as part of the following tasks:  
> - `gradlew build`  
> - `gradlew check`  
> - `gradlew checkAll`

---

<h3 id="integration-tests">Integration Tests (IT)</h3>

Projects that apply the [integration test convention](./build-logic/src/main/groovy/bento.test.integration-test-suite.gradle) are enabled to run integration tests.  
Integration test classes must have names ending with `IT` and be located in the `src/it/java` directory. These tests are not run in parallel.

Integration tests:
- Focus on the interaction among units, modules, or subsystems to ensure proper integration.  
- Often include resource-intensive processes such as API calls to databases or interactions between services.  
- Block or modify external resources temporarily and therefore <u>cannot</u> be run in parallel reliably.

> 💡 **Tips for Execution**  
> Integration tests are executed as part of the following tasks:  
> - `gradlew integrationTest`  
> - `gradlew checkIntegration`  
> - `gradlew checkAll`

---

<h3 id="parallel-integration-tests">Parallel Integration Tests (ITP)</h3>

Projects that apply the [integration test parallel convention](./build-logic/src/main/groovy/bento.test.integration-test-parallel-suite.gradle) are enabled to run integration tests in parallel.  
Parallel integration test classes must have names ending with `ITP` and be located in the `src/itp/java` directory. These tests are run in parallel.

Parallel integration tests:
- Focus on the interaction among units, modules, or subsystems to ensure proper integration.  
- May interact with external resources but do so in a non-blocking way that does not modify resources, allowing concurrent access.
- Must not modify external resources.
- Must be able to run in parallel reliably.

> 💡 **Tips for Execution**  
> Parallel integration tests are executed as part of the following tasks:  
> - `gradlew integrationTestParallel`  
> - `gradlew checkIntegration`  
> - `gradlew checkAll`

---

<h3 id="functional-tests">Functional Tests (FT)</h3>

Projects that apply the [functional test convention](./build-logic/src/main/groovy/bento.test.functional-test-suite.gradle) are enabled to run functional tests.  
Functional test classes must have names ending with `FT` and be located in the `src/ft/java` directory. These tests are not run in parallel.

Functional tests:
- Test user interface components, requiring a graphical environment to run.  
- Often interact with UI components and <u>cannot</u> be run in parallel reliably.

> 💡 **Tips for Execution**  
> Functional tests are executed as part of the following tasks:  
> - `gradlew functionalTest`  
> - `gradlew checkFunctional`  
> - `gradlew checkAll`

---


## JSpecify Nullness Analysis

BentoFX uses JSpecify annotations for nullness contracts and validates those
contracts during Java compilation with Error Prone and NullAway.

NullAway runs in JSpecify mode and treats violations as compilation errors. Code
is checked when it opts in with `@NullMarked`, so the project can enforce
nullness without requiring every package to be fully annotated at once.

Use this command to run the nullness analysis directly:

```shell
gradlew checkJSpecify
```


## Code Coverage

JaCoCo is configured by the test-suite convention plugins, not by the generic project convention. This keeps coverage setup next to the test suite that produces the execution data.

Per-project coverage tasks are available when the matching test-suite convention is applied:

- `gradlew jacocoTestReport` generates unit test coverage.
- `gradlew jacocoIntegrationTestReport` generates integration test coverage when the integration test suite is applied.
- `gradlew jacocoIntegrationTestParallelReport` generates parallel integration test coverage when the parallel integration test suite is applied.
- `gradlew jacocoFunctionalTestReport` generates functional test coverage when the functional test suite is applied.

For example:

```bash
gradlew checkAll
```

The [report aggregation project](./report-aggregation/build.gradle) creates aggregate test and coverage reports for most, but not all projects (e.g. the root project, the report-aggregation project, demo projects, etc.). 

## GitHub Workflows

<h3 id="build-workflow">Build</h3>

A CI-style local build is:

```bash
gradlew build checkAll
```

The GitHub workflow runs this build command, uploads JaCoCo HTML/XML reports as artifacts, and writes source-line and coverage statistics to the workflow summary.

<h3 id="automated-dependency-updates">Automated Dependency Updates</h3>

BentoFX uses GitHub Dependabot to keep Gradle dependencies and GitHub Actions up to date. Dependency update pull requests are reviewed and tested by the project's CI workflows before being merged.

<h3 id="checking-for-dependency-updates">Checking for Dependency Updates</h3>

BentoFX also uses the Gradle Versions Plugin to report newer dependency, plugin, 
and Gradle releases. The `io.github.ben-manes.versions.settings` plugin is 
applied once directly in `settings.gradle`, so the root report covers all 
projects in the main build, including dependencies and plugins declared by the 
settings script.

Run the dependency update report from the repository root:

```shell
gradlew dependencyUpdates
```

The report is printed to the console and written to:

```text
build/dependencyUpdates/report.txt
```

The task reports available updates only; it does not modify build scripts or
`gradle/libs.versions.toml`. Review each suggested release and update the
corresponding version-catalog entry manually.

The root report covers the main BentoFX multi-project build. Gradle treats the
`build-logic` and `settings-logic` included builds as separate builds, so their
internal dependencies are not merged into this report.

Use `--refresh-dependencies` when you need Gradle to ignore cached dependency
metadata while checking for newly published releases:

```shell
gradlew dependencyUpdates --refresh-dependencies
```


For repository administration, release procedures, and maintainer-only workflows, see [MAINTAINERS.md](MAINTAINERS.md).
