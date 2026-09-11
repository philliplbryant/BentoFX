#!/usr/bin/env python3
"""Write source and coverage statistics to the GitHub Actions step summary."""

from __future__ import annotations

import os
from pathlib import Path
from xml.etree import ElementTree

SOURCE_EXTENSIONS = {
    ".groovy",
    ".java",
}

EXCLUDED_PATH_PARTS = {
    ".git",
    ".gradle",
    "build",
    "build-logic",
    "demos",
    "gradle",
    "report-aggregation",
    "settings-logic",
}

JACOCO_COUNTER_TYPES = {
    "INSTRUCTION",
    "BRANCH",
    "LINE",
    "COMPLEXITY",
    "METHOD",
    "CLASS",
}


def is_source_file(path: Path) -> bool:
    return path.suffix in SOURCE_EXTENSIONS and not EXCLUDED_PATH_PARTS.intersection(path.parts)


def is_code_line(line: str, in_block_comment: bool) -> tuple[bool, bool]:
    stripped = line.strip()
    if not stripped:
        return False, in_block_comment

    if in_block_comment:
        if "*/" in stripped:
            return is_code_line(stripped.split("*/", 1)[1], False)
        return False, True

    if stripped.startswith("/*"):
        if "*/" in stripped:
            return is_code_line(stripped.split("*/", 1)[1], False)
        return False, True

    if stripped.startswith("//"):
        return False, False

    return True, False


def count_source_lines(root: Path) -> int:
    source_lines = 0
    for path in root.rglob("*"):
        if not path.is_file() or not is_source_file(path.relative_to(root)):
            continue

        in_block_comment = False
        with path.open(encoding="utf-8", errors="ignore") as source_file:
            for line in source_file:
                is_code, in_block_comment = is_code_line(line, in_block_comment)
                if is_code:
                    source_lines += 1
    return source_lines


AGGREGATE_REPORT_DIR = Path("report-aggregation") / "build" / "reports" / "jacoco"


def find_jacoco_xml_reports(root: Path) -> list[Path]:
    # Only ':report-aggregation' covers every module, so its reports are the ones
    # worth summarizing. Per-module tasks use the same '<taskName>/<taskName>.xml'
    # layout, so nothing about a path below 'jacoco' distinguishes them: the
    # aggregate directory itself is the only reliable filter.
    aggregate_dir = root / AGGREGATE_REPORT_DIR
    if not aggregate_dir.is_dir():
        return []

    return sorted(
        path
        for path in aggregate_dir.glob("*/*.xml")
        if path.is_file() and path.stem == path.parent.name
    )


def collect_report_counters(reports: list[Path]) -> dict[str, dict[str, tuple[int, int]]]:
    # Keyed by report rather than accumulated across them. Every aggregate report
    # describes the same class set, so adding their counters together multiplies
    # the denominator by the number of suites. Merging suites for real needs a
    # union of covered lines, which counter arithmetic cannot express.
    per_report: dict[str, dict[str, tuple[int, int]]] = {}
    for report in reports:
        try:
            root = ElementTree.parse(report).getroot()
        except (ElementTree.ParseError, OSError):
            continue

        counters = {counter_type: (0, 0) for counter_type in JACOCO_COUNTER_TYPES}
        for counter in root.findall("counter"):
            counter_type = counter.get("type")
            if counter_type not in JACOCO_COUNTER_TYPES:
                continue

            counters[counter_type] = (
                int(counter.get("missed", "0")),
                int(counter.get("covered", "0")),
            )
        per_report[report.parent.name] = counters
    return per_report


def percentage(missed: int, covered: int) -> str:
    total = missed + covered
    if total == 0:
        return "n/a"
    return f"{covered / total:.2%}"


def append_summary(root: Path) -> None:
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if not summary_path:
        return

    source_lines = count_source_lines(root)
    reports = find_jacoco_xml_reports(root)
    per_report = collect_report_counters(reports)

    with Path(summary_path).open("a", encoding="utf-8") as summary:
        summary.write("## BentoFX code statistics\n\n")
        summary.write(f"- Source lines of code: **{source_lines:,}**\n")
        summary.write(f"- JaCoCo XML reports found: **{len(reports)}**\n\n")
        summary.write("| Suite | Lines covered | Lines missed | Line | Branch | Instruction |\n")
        summary.write("| --- | ---: | ---: | ---: | ---: | ---: |\n")
        for name in sorted(per_report):
            counters = per_report[name]
            line_missed, line_covered = counters["LINE"]
            summary.write(
                f"| {name} | {line_covered:,} | {line_missed:,} "
                f"| {percentage(*counters['LINE'])} "
                f"| {percentage(*counters['BRANCH'])} "
                f"| {percentage(*counters['INSTRUCTION'])} |\n"
            )
        summary.write("\n")


if __name__ == "__main__":
    append_summary(Path.cwd())
