# BentoFX Maintainers Guide

## Table of Contents

- [Project Health](#project-health)
- [GitHub Workflows](#github-workflows)
  - [Build](#build-workflow)
  - [Qodana](#qodana-workflow)
  - [Update Qodana Baseline](#update-qodana-baseline)
- [Releases](#releases)
- [Repository Administration](#repository-administration)
- [Credentials](#credentials)
  - [Credential Resolution Order](#credential-resolution-order)
  - [Credential Usage Matrix](#credential-usage-matrix)

This guide complements [CONTRIBUTING.md](CONTRIBUTING.md) by documenting repository administration,
release management, CI/CD maintenance, and other tasks requiring repository write access or maintainer judgment.

## Project Health

| Service | Health | Purpose |
|---------|--------|---------|
| **[GitHub Actions](https://github.com/philliplbryant/BentoFX/actions)** | [![Build](https://github.com/philliplbryant/BentoFX/actions/workflows/build.yml/badge.svg)](https://github.com/philliplbryant/BentoFX/actions/workflows/build.yml) | Build and test status |
| **[SonarCloud](https://sonarcloud.io/summary/new_code?id=philliplbryant_BentoFX)** | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=philliplbryant_BentoFX&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=philliplbryant_BentoFX) | Quality Gate, Maintainability, Reliability, and Security |
| **[Codecov](https://codecov.io/gh/philliplbryant/BentoFX)** | [![Codecov](https://codecov.io/gh/philliplbryant/BentoFX/graph/badge.svg)](https://codecov.io/gh/philliplbryant/BentoFX) | Coverage reports and historical trends |
| **[CodeQL](https://github.com/philliplbryant/BentoFX/security/code-scanning)** | **[Security Results](https://github.com/philliplbryant/BentoFX/security/code-scanning)** | GitHub code scanning results |
| **[Qodana](https://github.com/philliplbryant/BentoFX/actions/workflows/qodana.yml)** | **[Inspection Results](https://github.com/philliplbryant/BentoFX/actions/workflows/qodana.yml)** | JetBrains inspections and static analysis |
| **[Maven Central](https://central.sonatype.com/artifact/software.coley.bento-fx/core)** | [![Maven Central](https://img.shields.io/maven-central/v/software.coley.bento-fx/core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/software.coley.bento-fx/core) | Published artifacts and latest release |

## GitHub Workflows

<h3 id="build-workflow">Build</h3>
Ensure the Build workflow passes before merging pull requests.

<h3 id="qodana-workflow">Qodana</h3>
Review new findings and determine whether they should be fixed or intentionally accepted.

<h3 id="update-qodana-baseline">Update Qodana Baseline</h3>
Run this workflow only when intentionally updating the accepted baseline. Review the
generated baseline before committing because accepted findings will no longer be reported.

## Releases

- Update project version(s).
- Publish the release with JReleaser.
- Verify the generated GitHub Release notes.
- Verify publication to Maven Central.

See the [JReleaser documentation](https://jreleaser.org/guide/latest/) for release configuration, signing, and publishing details.
## Repository Administration

- Review Dependabot pull requests.
- Review Codecov coverage trends and CodeQL alerts.
- Maintain branch protection rules and required status checks.

## Credentials

The repository does not store publishing or analysis credentials. Configure them externally for local development or CI.

<h3 id="credential-resolution-order">Credential Resolution Order</h3>

When multiple configuration mechanisms are available, use the following preference order:

1. **GitHub Actions** (repository or organization secrets)
2. **Local Environment** (environment variables)
3. **User Config** (`~/.jreleaser/config.properties` and `~/.gradle/gradle.properties`)
4. **Repository Configuration** (references to credential names only; never secret values)

Never commit credentials, personal access tokens, signing keys, passwords, or IDE workspace files (such as `.idea/workspace.xml`) that may contain local credential configuration.

<h3 id="credential-usage-matrix">Credential Usage Matrix</h3>

| Task / Workflow | Required Credentials | Typical Location |
|-----------------|----------------------|------------------|
| Run SonarCloud analysis | `SONAR_TOKEN` | **Both** |
| Publish a release with JReleaser | `JRELEASER_GITHUB_TOKEN`, `JRELEASER_MAVENCENTRAL_USERNAME`, `JRELEASER_MAVENCENTRAL_PASSWORD`, `JRELEASER_GPG_SECRET_KEY`, `JRELEASER_GPG_PASSPHRASE` | **Both** |
| Publish signed artifacts to Maven Central | `JRELEASER_MAVENCENTRAL_USERNAME`, `JRELEASER_MAVENCENTRAL_PASSWORD`, `JRELEASER_GPG_SECRET_KEY`, `JRELEASER_GPG_PASSPHRASE` | **Both** |
| Create GitHub releases | `JRELEASER_GITHUB_TOKEN` | **Both** |
| Upload coverage to Codecov (if enabled) | `CODECOV_TOKEN` | **GitHub Actions** |
| Local development and testing | None | **None** |

The **Typical Location** column indicates where credentials are typically configured. **Both** indicates that the task may be run either in GitHub Actions using repository secrets or locally using environment variables or user configuration files.
