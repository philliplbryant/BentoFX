#!/usr/bin/env python3
"""Write source and coverage statistics to the GitHub Actions step summary."""

from __future__ import annotations

import os
from collections.abc import Iterator
from pathlib import Path
from xml.etree import ElementTree
from xml.etree.ElementTree import Element

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

# Marks the union row and the note explaining it.
FOOTNOTE_MARK = "†"


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


def iter_report_lines(reports: list[Path]) -> Iterator[tuple[str, Element]]:
    # Yields every line of every report keyed by package, source file and number.
    # That key is the line's identity, which is what makes the union exact.
    for report in reports:
        try:
            root = ElementTree.parse(report).getroot()
        except (ElementTree.ParseError, OSError):
            continue

        for package in root.iter("package"):
            for source_file in package.findall("sourcefile"):
                for line in source_file.findall("line"):
                    key = f"{package.get('name')}|{source_file.get('name')}|{line.get('nr')}"
                    yield key, line


def track_bounds(
    bounds: dict[str, list[int]], key: str, line: Element, covered_attr: str, missed_attr: str
) -> None:
    # Records [total, best single suite, sum across suites] for one line, which is
    # all the XML supports for branches and instructions: it says how many were
    # covered, never which ones, so the same branch covered by two suites cannot be
    # told from two different branches.
    covered = int(line.get(covered_attr, "0"))
    total = covered + int(line.get(missed_attr, "0"))
    if total == 0:
        return

    entry = bounds.setdefault(key, [total, 0, 0])
    entry[1] = max(entry[1], covered)
    entry[2] += covered


def collect_union(reports: list[Path]) -> dict[str, object]:
    # A line counts once however many suites report it, and counts as covered when
    # any suite covered an instruction on it, which is JaCoCo's own definition. An
    # exact branch figure would need the '.exec' binaries merged before reporting,
    # where probe identity survives; see 'TODO BENTO-13' in TODO.md.
    covered_lines: set[str] = set()
    all_lines: set[str] = set()
    branch: dict[str, list[int]] = {}
    instruction: dict[str, list[int]] = {}

    for key, line in iter_report_lines(reports):
        all_lines.add(key)
        if int(line.get("ci", "0")) > 0:
            covered_lines.add(key)
        track_bounds(branch, key, line, "cb", "mb")
        track_bounds(instruction, key, line, "ci", "mi")

    return {
        "lines_covered": len(covered_lines),
        "lines_total": len(all_lines),
        "branch": branch,
        "instruction": instruction,
    }


def bounded_percentage(bounds: dict[str, list[int]]) -> str:
    # The midpoint of the window, not a measurement: where the true figure falls
    # depends on how far the suites overlap, which is what the XML omits. Reported
    # as one number because the window is under a percentage point either way.
    total = sum(entry[0] for entry in bounds.values())
    if total == 0:
        return "n/a"

    lower = sum(entry[1] for entry in bounds.values())
    upper = sum(min(entry[0], entry[2]) for entry in bounds.values())
    return f"{(lower + upper) / 2 / total:.2%}"


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
    union = collect_union(reports)

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

        lines_covered = union["lines_covered"]
        lines_total = union["lines_total"]
        summary.write(
            f"| **Union of all suites**{FOOTNOTE_MARK} | **{lines_covered:,}** "
            f"| **{lines_total - lines_covered:,}** "
            f"| **{percentage(lines_total - lines_covered, lines_covered)}** "
            f"| **{bounded_percentage(union['branch'])}** "
            f"| **{bounded_percentage(union['instruction'])}** |\n"
        )
        summary.write(f"\n{FOOTNOTE_MARK} ")
        summary.write(
            "A line counts once in the union however many suites cover it. "
            "Branch and instruction coverage are the average of the\n"
            "coverage range. JaCoCo XML reports how many branches/instructions "
            "were covered on each line, not which ones, so the\n"
            "same branch covered twice cannot be distinguished from two "
            "different branches. The high end assumes no overlap. The \n"
            "low end assumes maximum overlap, taking the best single suite for "
            "each line. The number shown is the average of the two.\n"
            "\n"
        )


if __name__ == "__main__":
    append_summary(Path.cwd())
