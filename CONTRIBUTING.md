# Contributing to BentoFX

The following is a series of guidelines for contributing to BentoFX.
They're not _"rules"_ per say, rather they're more like goals to strive towards.

## License

BentoFX is licensed under the MIT License. Anything you contribute will thus also be under the MIT license.

## Coding Guidelines

**Style**: IDE code formatting rules can be found in the [`/setup` directory](setup).
If using IntelliJ or Eclipse you should be able to import one of the provided files.

**Commits**: Try to keep commits small and focused on a single change. 
This makes it easier for reviewers to understand the context and purpose of each commit.
I know some features are large in scope, just break down what you can where possible.

## Automated Tests
Automate tests should be separated into categories, as defined below, based on their purposes and the conditions under which they can be reliably run.

**Unit Tests**: 
> <span style="font-size: 1.5em;">💡</span>Unit tests run as part of the following tasks <ul><li>`gradlew build`</li><li>`gradlew check`</li><li>`gradlew checkAll`</li>

 - Focus on a single, small piece of functionality, such as a single class, function, or method. 
 - Test the code independently, often using mocks or stubs to replace external dependencies like databases, APIs, or other modules. 
 - Run very quickly.
 - Can be run in parallel.

**Integration Tests (IT)**:
> <span style="font-size: 1.5em;">💡</span>Integration tests run as part of the following tasks <ul><li>`gradlew integrationTest`</li><li>`gradlew checkIntegration`</li><li>`gradlew checkAll`</li>

- Focus on the interaction between units, modules, or subsystems to ensure they integrate properly.
- Include interactions between components, such as an API making a database call or one service calling another, often interacting with and (temporarily) blocking or modifying resources.
- Because they block or modify external resources, integration tests <u>cannot</u>  be run in parallel reliably.

**Integration Tests, Parallel (ITP)**:
> <span style="font-size: 1.5em;">💡</span>Integration tests, parallel run as part of the following tasks <ul><li>`gradlew integrationTestParallel`</li><li>`gradlew checkIntegration`</li><li>`gradlew checkAll`</li>

- Also, focus on the interaction between units, modules, or subsystems to ensure they integrate properly.
- May interact with external resources, but do not lock or modify them, and allow other tests to concurrently access the same resources.
- Because they do not modify external resources, integration tests parallel can be run in parallel reliably.

**Functional Tests**:
> <span style="font-size: 1.5em;">💡</span>Functional tests run as part of the following tasks <ul><li>`gradlew functionalTest`</li><li>`gradlew checkFunctional`</li><li>`gradlew checkAll`</li>

- Instantiate user interface components and therefore cannot be run without a graphics environment.
- Often interact with user interface components and therefore cannot be run in parallel reliably.
