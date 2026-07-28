# Updating the Qodana baseline

The **Update Qodana Baseline** GitHub Actions workflow regenerates the Qodana baseline for a selected branch. The workflow definition is stored on the repository's default branch so that GitHub displays it in the Actions tab, but the analysis and optional commit are performed on the branch selected when the workflow is started.

The committed baseline is stored at:

```text
.github/analysis/qodana-baseline.sarif.json
```

## Before running the workflow

1. Push all code changes that should be included in the new baseline to the feature branch.
2. Review the current Qodana findings and confirm that any remaining problems are intentionally being accepted.
3. Ensure the feature branch is not protected against pushes from GitHub Actions.

Do not update the baseline merely to make a failing quality gate pass. Updating it accepts the current findings as existing technical debt.

## Run the workflow for a feature branch

1. Open the repository on GitHub.
2. Select **Actions**.
3. In the workflow list, select **Update Qodana Baseline**.
4. Select **Run workflow**.
5. In the branch menu, select the feature branch to analyze.
6. Leave **Commit the updated baseline to the selected branch** enabled. It is enabled by default.
7. Select **Run workflow**.

The workflow checks out the selected branch, runs Qodana without using the existing baseline, and generates a fresh `qodana.sarif.json` file.

## Results

Every run uploads an artifact named `qodana-baseline`. To download it:

1. Open the completed workflow run.
2. Find **Artifacts** on the run summary page.
3. Download **qodana-baseline**.
4. Extract `qodana.sarif.json` from the downloaded archive.

When the commit option is enabled, the workflow also copies the generated file to:

```text
.github/analysis/qodana-baseline.sarif.json
```

It then commits and pushes that file directly to the selected feature branch. If the generated baseline is identical to the committed baseline, no commit is created.

## Review the update

After the workflow finishes:

1. Pull the feature branch locally.
2. Review the baseline commit and the workflow run.
3. Confirm that the accepted findings are appropriate.
4. Include the baseline commit in the feature branch's existing pull request.

When the feature branch is merged, the updated baseline becomes the baseline on the default branch.

## Artifact-only run

Disable **Commit the updated baseline to the selected branch** before starting the workflow to generate only the downloadable artifact. The repository will not be changed.

## Troubleshooting

### The workflow is not shown in Actions

A manually dispatched workflow must exist on the repository's default branch. Confirm that this file has been committed and pushed to the default branch:

```text
.github/workflows/update-qodana-baseline.yml
```

### The workflow cannot push the baseline

Check the following:

- The repository allows GitHub Actions to use read/write workflow permissions.
- The workflow has `contents: write` permission.
- The selected branch permits pushes from GitHub Actions.
- A branch protection rule or ruleset is not blocking the bot's push.

The generated artifact remains available even when the commit step cannot push.

### Qodana reports a failed quality gate

The Qodana scan step is allowed to continue after a nonzero result because baseline generation may intentionally analyze findings that would fail the normal quality gate. The workflow still verifies that the SARIF file was actually generated before uploading or committing it.
