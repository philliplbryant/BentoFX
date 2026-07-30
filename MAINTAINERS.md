# BentoFX Maintainers Guide

This guide complements `CONTRIBUTING.md` and documents tasks requiring repository
write access or maintainer judgment.

## GitHub Workflows

### Build
Ensure the Build workflow is passing before merging pull requests.

### Qodana
Review new findings and determine whether they should be fixed or intentionally accepted.

### Update Qodana Baseline
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
