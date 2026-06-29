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


def find_jacoco_xml_reports(root: Path) -> list[Path]:
    reports = []
    for path in root.rglob("*.xml"):
        if not path.is_file():
            continue

        if "jacoco" in path.parts and "reports" in path.parts:
            reports.append(path)
    return sorted(reports)


def collect_jacoco_counters(reports: list[Path]) -> dict[str, tuple[int, int]]:
    counters = {counter_type: (0, 0) for counter_type in JACOCO_COUNTER_TYPES}
    for report in reports:
        if not report.is_file():
            continue

        try:
            root = ElementTree.parse(report).getroot()
        except (ElementTree.ParseError, OSError):
            continue

        for counter in root.findall("counter"):
            counter_type = counter.get("type")
            if counter_type not in JACOCO_COUNTER_TYPES:
                continue

            missed = int(counter.get("missed", "0"))
            covered = int(counter.get("covered", "0"))
            existing_missed, existing_covered = counters[counter_type]
            counters[counter_type] = (
                existing_missed + missed,
                existing_covered + covered,
            )
    return counters


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
    counters = collect_jacoco_counters(reports)

    with Path(summary_path).open("a", encoding="utf-8") as summary:
        summary.write("## BentoFX code statistics\n\n")
        summary.write(f"- Source lines of code: **{source_lines:,}**\n")
        summary.write(f"- JaCoCo XML reports found: **{len(reports)}**\n\n")
        summary.write("| Metric | Covered | Missed | Coverage |\n")
        summary.write("| --- | ---: | ---: | ---: |\n")
        for counter_type in sorted(counters):
            missed, covered = counters[counter_type]
            summary.write(
                f"| {counter_type.title()} | {covered:,} | {missed:,} | {percentage(missed, covered)} |\n"
            )
        summary.write("\n")


if __name__ == "__main__":
    append_summary(Path.cwd())
