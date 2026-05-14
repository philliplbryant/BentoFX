# Contributing to BentoFX

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

**Unit Tests (Test)**: 

Projects that apply the [project convention](./build-logic/src/main/groovy/bento.project.project-convention.gradle) are configured to run unit tests.  
Unit test classes must have names ending with `Test` and be located in the `src/test/java` directory.  

Unit tests:  
- Focus on a single, small piece of functionality, such as a class, function, or method.  
- Run independently of external dependencies (e.g., databases, APIs, or other modules), often using mocks or stubs.  
- Execute very quickly.  
- Can be run in parallel reliably.

> 💡 **Tips for Execution**  
> Unit tests are executed as part of the following tasks:  
> - `gradlew build`  
> - `gradlew check`  
> - `gradlew checkAll`

---

**Integration Tests (IT)**:

Projects that apply the [integration test convention](./build-logic/src/main/groovy/bento.test.integration-test-suite.gradle) are enabled to run integration tests.  
Integration test classes must have names ending with `IT` and be located in the `src/it/java` directory.

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

**Parallel Integration Tests (ITP)**:

Projects that apply the [integration test parallel convention](./build-logic/src/main/groovy/bento.test.integration-test-parallel-suite.gradle) are enabled to run integration tests in parallel.  
Parallel integration test classes must have names ending with `ITP` and be located in the `src/itp/java` directory.  

Parallel integration tests:
- Focus on the interaction among units, modules, or subsystems to ensure proper integration.  
- May interact with external resources but do so in a non-blocking way that does not modify resources, allowing concurrent access.  
- Because they do not modify external resources, these tests can run in parallel reliably.

> 💡 **Tips for Execution**  
> Parallel integration tests are executed as part of the following tasks:  
> - `gradlew integrationTestParallel`  
> - `gradlew checkIntegration`  
> - `gradlew checkAll`

---

**Functional Tests (FT)**:

Projects that apply the [functional test convention](./build-logic/src/main/groovy/bento.test.functional-test-suite.gradle) are enabled to run functional tests.  
Functional test classes must have names ending with `FT` and be located in the `src/ft/java` directory.  

Functional tests:
- Test user interface components, requiring a graphical environment to run.  
- Often interact with UI components and <u>cannot</u> be run in parallel reliably.

> 💡 **Tips for Execution**  
> Functional tests are executed as part of the following tasks:  
> - `gradlew functionalTest`  
> - `gradlew checkFunctional`  
> - `gradlew checkAll`

---
