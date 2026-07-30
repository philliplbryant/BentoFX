# BentoFX Maintainers Guide

## Table of Contents

- [Project Health](#project-health)
- [GitHub Workflows](#github-workflows)
  - [Build](#build-workflow)
  - [Qodana](#qodana-workflow)
  - [Update Qodana Baseline](#update-qodana-baseline)
- [Releases](#releases)
- [Repository Administration](#repository-administration)

This guide complements [CONTRIBUTING.md](CONTRIBUTING.md) and documents tasks requiring repository
write access or maintainer judgment.

## Project Health

| Service | Health | Purpose |
|---------|--------|---------|
| **[GitHub Actions](https://github.com/Col-E/BentoFX/actions)** | [![Build](https://github.com/Col-E/BentoFX/actions/workflows/build.yml/badge.svg)](https://github.com/Col-E/BentoFX/actions/workflows/build.yml) | Build and test status |
| **[SonarCloud](https://sonarcloud.io/summary/new_code?id=philliplbryant_BentoFX)** | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=philliplbryant_BentoFX&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=philliplbryant_BentoFX) | Quality Gate, Maintainability, Reliability, and Security |
| **[Codecov](https://codecov.io/gh/Col-E/BentoFX)** | [![Codecov](https://codecov.io/gh/Col-E/BentoFX/graph/badge.svg)](https://codecov.io/gh/Col-E/BentoFX) | Coverage reports and historical trends |
| **[CodeQL](https://github.com/Col-E/BentoFX/security/code-scanning)** | **[Security Results](https://github.com/Col-E/BentoFX/security/code-scanning)** | GitHub code scanning results |
| **[Qodana](https://github.com/Col-E/BentoFX/actions/workflows/qodana.yml)** | **[Inspection Results](https://github.com/Col-E/BentoFX/actions/workflows/qodana.yml)** | JetBrains inspections and static analysis |
| **[Maven Central](https://central.sonatype.com/artifact/software.coley.bento-fx/core)** | [![Maven Central](https://img.shields.io/maven-central/v/software.coley.bento-fx/core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/software.coley.bento-fx/core) | Published artifacts and latest release |

## GitHub Workflows

<h3 id="build-workflow">Build</h3>
Ensure the Build workflow is passing before merging pull requests.

<h3 id="qodana-workflow">Qodana</h3>
Review new findings and determine whether they should be fixed or intentionally accepted.

<h3 id="update-qodana-baseline">Update Qodana Baseline</h3>
Run this workflow only when intentionally updating the accepted baseline. Review the
generated baseline before committing because accepted findings will no longer be reported.

## Releases

- Update versions.
- Verify GitHub Release notes.
- Publish with JReleaser.
- Verify Maven Central publication.

## Repository Administration

- Review Dependabot pull requests.
- Monitor Codecov coverage and CodeQL alerts.
- Maintain branch protection rules and required status checks.
