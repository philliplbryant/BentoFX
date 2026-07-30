# BentoFX Maintainers Guide

## Table of Contents

- [GitHub Workflows](#github-workflows)
  - [Build](#build-workflow)
  - [Qodana](#qodana-workflow)
  - [Update Qodana Baseline](#update-qodana-baseline)
- [Releases](#releases)
- [Repository Administration](#repository-administration)

This guide complements `CONTRIBUTING.md` and documents tasks requiring repository
write access or maintainer judgment.

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
