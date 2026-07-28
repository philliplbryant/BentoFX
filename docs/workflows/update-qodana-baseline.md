# Updating the Qodana Baseline

The **Update Qodana Baseline** workflow regenerates the accepted Qodana
findings for a selected branch. The baseline is stored at:

```text
.github/analysis/qodana-baseline.sarif.json
```

## When to update the baseline

Update the baseline only when the findings remaining on the branch have been
reviewed and intentionally accepted. Do not update it merely to make a failed
quality gate pass, because doing so accepts the new findings as existing
technical debt.

## Update the baseline on a branch

1. Commit and push the code you want Qodana to analyze.
2. Open the repository on GitHub.
3. Select **Actions**.
4. Select **Update Qodana Baseline**.
5. Click **Run workflow**.
6. Choose the branch containing your changes.
7. Leave **Commit the updated baseline back to the selected branch** enabled.
8. Click **Run workflow**.

The workflow checks out the selected branch, runs Qodana without the existing
baseline, and generates a new `qodana.sarif.json`. It then:

- uploads the generated file as the `qodana-baseline` workflow artifact; and
- commits it to the selected branch as
  `.github/analysis/qodana-baseline.sarif.json`.

If the generated baseline is identical to the committed baseline, the workflow
finishes without creating a commit.

## Download the artifact without committing

Disable **Commit the updated baseline back to the selected branch** before
running the workflow. The workflow will still upload the `qodana-baseline`
artifact.

To install it manually:

1. Open the completed workflow run.
2. Download the **qodana-baseline** artifact.
3. Extract `qodana.sarif.json`.
4. Replace `.github/analysis/qodana-baseline.sarif.json` with that file.
5. Review and commit the change on your branch.

## Review the update

Review the baseline change before merging the branch into `master`. Updating
the baseline causes the normal Qodana quality gate to treat those findings as
existing rather than new.
