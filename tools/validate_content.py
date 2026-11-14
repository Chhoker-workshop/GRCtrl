#!/usr/bin/env python3
"""
GRC VANGUARD content validator.

Mirrors the in-app validator (ContentValidator.kt) so that the corpus can be
checked without an Android toolchain, in CI, or while authoring.

Usage:
    python tools/validate_content.py [--root <path to app/src/main/assets/curriculum>]
"""

import argparse
import json
import os
import sys
from collections import defaultdict

VALID_DOMAINS = {
    "GOVERNANCE",
    "RISK_MANAGEMENT",
    "COMPLIANCE",
    "AUDIT",
    "INFORMATION_SECURITY",
    "PRIVACY",
    "BUSINESS_CONTINUITY",
    "THIRD_PARTY",
    "REGULATORY",
    "LEADERSHIP",
}

VALID_DIFFICULTIES = {"BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"}

VALID_QUESTION_TYPES = {
    "MULTIPLE_CHOICE",
    "MULTIPLE_SELECT",
    "TRUE_FALSE",
    "SCENARIO",
    "RISK_CALCULATION",
    "MATCH_CONTROL",
    "EVIDENCE_IDENTIFICATION",
    "COMPLIANCE_GAP",
    "PRIORITISE_REMEDIATION",
    "MANAGEMENT_DECISION",
    "SHORT_ANSWER",
    "PRACTICAL_ASSIGNMENT",
}

VALID_FRAMEWORK_KINDS = {
    "LAW",
    "REGULATION",
    "STANDARD",
    "FRAMEWORK",
    "CODE_OF_PRACTICE",
    "CERTIFICATION",
    "CONTRACTUAL_REQUIREMENT",
    "GUIDANCE",
}

VALID_CONTROL_CATEGORIES = {"ORGANIZATIONAL", "PEOPLE", "PHYSICAL", "TECHNOLOGICAL"}

OPTION_TYPES = {
    "MULTIPLE_CHOICE",
    "MULTIPLE_SELECT",
    "TRUE_FALSE",
    "SCENARIO",
    "EVIDENCE_IDENTIFICATION",
    "COMPLIANCE_GAP",
    "PRIORITISE_REMEDIATION",
    "MANAGEMENT_DECISION",
}

ERROR = "ERROR"
WARNING = "WARNING"


class Report:
    def __init__(self):
        self.errors = []
        self.warnings = []
        self.stats = {}

    def error(self, content_id, message):
        self.errors.append((content_id, message))

    def warn(self, content_id, message):
        self.warnings.append((content_id, message))

    def print(self):
        print("=" * 78)
        print("GRC VANGUARD - CONTENT VALIDATION")
        print("=" * 78)
        print()
        for label, bucket in (("ERRORS", self.errors), ("WARNINGS", self.warnings)):
            if not bucket:
                continue
            print(f"{label} ({len(bucket)}):")
            for content_id, message in bucket:
                print(f"  [{content_id}] {message}")
            print()
        print("-" * 78)
        print("STATISTICS")
        for key, value in self.stats.items():
            print(f"  {key:<26} {value}")
        print("-" * 78)
        if self.errors:
            print(f"RESULT: FAILED - {len(self.errors)} error(s), {len(self.warnings)} warning(s)")
        else:
            print(f"RESULT: PASSED - 0 errors, {len(self.warnings)} warning(s)")
        print("=" * 78)
        return 0 if not self.errors else 1


def load_corpus(root):
    documents = []
    for dirpath, _dirnames, filenames in os.walk(root):
        for filename in sorted(filenames):
            if not filename.endswith(".json"):
                continue
            path = os.path.join(dirpath, filename)
            try:
                with open(path, "r", encoding="utf-8") as handle:
                    payload = json.load(handle)
            except Exception as exc:  # noqa: BLE001
                documents.append((path, None, f"invalid JSON: {exc}"))
                continue
            documents.append((path, payload, None))
    return documents


def collect(items_by_kind, kind):
    out = []
    for rel, payload in items_by_kind.get(kind, []):
        out.extend(payload.get("items", []))
    return out


def check_duplicates(items, key, label, report):
    seen = defaultdict(list)
    for item in items:
        value = item.get(key)
        if value is None:
            report.error("<blank>", f"{label} item has no '{key}'")
            continue
        seen[value].append(item)
    for value, group in seen.items():
        if len(group) > 1:
            report.error(value, f"Duplicate {label} identifier used {len(group)} times")


def validate(root):
    report = Report()
    documents = load_corpus(root)

    parsed = []
    for path, payload, err in documents:
        rel = os.path.relpath(path, root)
        if err:
            report.error(rel, err)
            continue
        if "kind" not in payload:
            report.error(rel, "Missing 'kind'")
            continue
        if "items" not in payload:
            report.error(rel, "Missing 'items'")
            continue
        if not isinstance(payload["items"], list):
            report.error(rel, "'items' is not an array")
            continue
        parsed.append((rel, payload))

    by_kind = defaultdict(list)
    for rel, payload in parsed:
        by_kind[payload["kind"]].append((rel, payload))

    # ------------------------------------------------------------------ modules
    modules = collect(by_kind, "MODULES")
    check_duplicates(modules, "id", "module", report)
    module_ids = {m.get("id") for m in modules}

    # ------------------------------------------------------------------ lessons
    lessons = collect(by_kind, "LESSONS")
    check_duplicates(lessons, "id", "lesson", report)
    lesson_ids = {l.get("id") for l in lessons}
    for lesson in lessons:
        lid = lesson.get("id", "<blank>")
        if not lesson.get("title"):
            report.error(lid, "Lesson title is empty")
        if not lesson.get("sections") and not lesson.get("practicalExercise"):
            report.error(lid, "Lesson has no content sections and no exercise")
        if not lesson.get("objectives"):
            report.warn(lid, "Lesson has no learning objectives")
        if not lesson.get("estimatedMinutes", 0) > 0:
            report.warn(lid, "Lesson estimated duration must be positive")
        if not lesson.get("version"):
            report.warn(lid, "Lesson has no content version")
        if lesson.get("difficulty") not in VALID_DIFFICULTIES:
            report.error(lid, f"Invalid difficulty '{lesson.get('difficulty')}'")
        if lesson.get("moduleId") not in module_ids:
            report.error(lid, f"Belongs to unknown module '{lesson.get('moduleId')}'")
        for section in lesson.get("sections", []):
            if not (section.get("body") or section.get("bullets") or section.get("table")):
                report.warn(lid, "Empty content section")
        for qid in lesson.get("assessmentQuestionIds", []):
            pass  # checked after questions are loaded

    for module in modules:
        mid = module.get("id", "<blank>")
        if not (module.get("lessonIds") or module.get("advancedLessonIds")):
            report.error(mid, "Module has no lessons")
        for lid in module.get("lessonIds", []) + module.get("advancedLessonIds", []):
            if lid not in lesson_ids:
                report.error(mid, f"References unknown lesson '{lid}'")

    # ---------------------------------------------------------------- questions
    questions = collect(by_kind, "QUESTIONS")
    check_duplicates(questions, "id", "question", report)
    question_ids = {q.get("id") for q in questions}
    for question in questions:
        qid = question.get("id", "<blank>")
        qtype = question.get("type")
        if qtype not in VALID_QUESTION_TYPES:
            report.error(qid, f"Unknown question type '{qtype}'")
            continue
        if question.get("domain") not in VALID_DOMAINS:
            report.error(qid, f"Invalid competency domain '{question.get('domain')}'")
        if not question.get("stem"):
            report.error(qid, "Question stem is empty")
        if not question.get("explanation"):
            report.warn(qid, "Question has no explanation")
        if question.get("difficulty") not in VALID_DIFFICULTIES:
            report.error(qid, f"Invalid difficulty '{question.get('difficulty')}'")
        options = question.get("options", [])
        correct = question.get("correctOptionIds", [])
        if qtype in OPTION_TYPES:
            if not options:
                report.error(qid, f"Question type {qtype} requires options")
            if not correct:
                report.error(qid, "Question has no correct answer")
            option_ids = {o.get("id") for o in options}
            for cid in correct:
                if cid not in option_ids:
                    report.error(qid, f"Correct answer '{cid}' has no matching option")
            flagged = {o.get("id") for o in options if o.get("correct")}
            if flagged and flagged != set(correct):
                report.error(qid, "Option 'correct' flags disagree with correctOptionIds")
            for option in options:
                if not option.get("rationale"):
                    report.warn(qid, f"Option '{option.get('id')}' has no rationale")
            if qtype == "TRUE_FALSE" and len(options) != 2:
                report.error(qid, "True/false question must have exactly two options")
            if qtype == "MULTIPLE_SELECT" and len(correct) < 2:
                report.warn(qid, "Multiple-select question has fewer than two correct options")
        elif qtype == "RISK_CALCULATION":
            if not question.get("numericAnswer"):
                report.error(qid, "Risk calculation question has no numeric answer")
            if not options:
                report.error(qid, "Risk calculation question requires candidate answers")
        elif qtype == "MATCH_CONTROL":
            if not question.get("matchPairs"):
                report.error(qid, "Matching question has no pairs")
            if not correct:
                report.error(qid, "Matching question has no correct mapping")
        elif qtype == "SHORT_ANSWER":
            if not question.get("modelAnswer"):
                report.error(qid, "Short answer question has no model answer")
        elif qtype == "PRACTICAL_ASSIGNMENT":
            if not question.get("modelAnswer") and not question.get("explanation"):
                report.error(qid, "Practical assignment has neither model answer nor explanation")

    for lesson in lessons:
        for qid in lesson.get("assessmentQuestionIds", []):
            if qid not in question_ids:
                report.error(lesson.get("id", "<blank>"), f"References unknown question '{qid}'")

    # ----------------------------------------------------------------- controls
    controls = collect(by_kind, "CONTROLS")
    check_duplicates(controls, "id", "control", report)
    check_duplicates(controls, "controlId", "control", report)
    for control in controls:
        cid = control.get("id", "<blank>")
        if control.get("category") not in VALID_CONTROL_CATEGORIES:
            report.error(cid, f"Invalid control category '{control.get('category')}'")
        for field in ("objective", "whyItExists", "riskAddressed", "implementationGuidance",
                      "responsibleDepartment", "auditTesting"):
            if not control.get(field):
                report.error(cid, f"Control has no '{field}'")
        for field in ("evidenceExamples", "commonGaps", "interviewQuestions"):
            if not control.get(field):
                report.warn(cid, f"Control lists no '{field}'")
        if not control.get("scenario"):
            report.warn(cid, "Control has no practical scenario")

    control_ids = {c.get("controlId") for c in controls}
    expected = (
        [f"A.5.{i}" for i in range(1, 38)]
        + [f"A.6.{i}" for i in range(1, 9)]
        + [f"A.7.{i}" for i in range(1, 15)]
        + [f"A.8.{i}" for i in range(1, 35)]
    )
    missing = [cid for cid in expected if cid not in control_ids]
    if missing:
        report.error("<controls>", f"Missing {len(missing)} Annex A controls: {', '.join(missing[:12])}"
                     + (" ..." if len(missing) > 12 else ""))

    # --------------------------------------------------------------- frameworks
    frameworks = collect(by_kind, "FRAMEWORKS")
    check_duplicates(frameworks, "id", "framework", report)
    framework_ids = {f.get("id") for f in frameworks}
    for framework in frameworks:
        fid = framework.get("id", "<blank>")
        if not framework.get("title"):
            report.error(fid, "Framework has no title")
        if not framework.get("publisher"):
            report.error(fid, "Framework has no publisher")
        if framework.get("kind") not in VALID_FRAMEWORK_KINDS:
            report.error(fid, f"Invalid framework kind '{framework.get('kind')}'")
        if not framework.get("officialUrl"):
            report.warn(fid, "Framework has no official source URL")
        if not framework.get("lastVerified"):
            report.warn(fid, "Framework has no last-verified date")

    # ----------------------------------------------------------------- mappings
    mappings = collect(by_kind, "MAPPINGS")
    check_duplicates(mappings, "id", "mapping", report)
    for pair in mappings:
        pid = pair.get("id", "<blank>")
        for key in ("frameworkA", "frameworkB"):
            ref = pair.get(key)
            if ref and framework_ids and ref not in framework_ids:
                report.warn(pid, f"References unknown framework id '{ref}'")
        if not pair.get("entries"):
            report.warn(pid, "Mapping pair has no entries")
        for entry in pair.get("entries", []):
            if not entry.get("commonObjective"):
                report.error(pid, f"Mapping entry '{entry.get('aRef')}' has no common objective")

    # ---------------------------------------------------------------- scenarios
    scenarios = collect(by_kind, "SCENARIOS")
    check_duplicates(scenarios, "id", "scenario", report)
    for scenario in scenarios:
        sid = scenario.get("id", "<blank>")
        options = scenario.get("options", [])
        if len(options) < 3:
            report.warn(sid, "Scenario offers fewer than three options")
        if not any(o.get("score", 0) >= 80 for o in options):
            report.error(sid, "Scenario has no high-scoring (best-practice) option")
        if not scenario.get("bestPractice"):
            report.error(sid, "Scenario has no best-practice explanation")
        if not scenario.get("context"):
            report.error(sid, "Scenario has no business context")
        for option in options:
            if not option.get("consequence"):
                report.warn(sid, f"Option '{option.get('id')}' has no consequence")
        for competency in scenario.get("competencies", []):
            if competency not in VALID_DOMAINS:
                report.error(sid, f"Invalid competency '{competency}'")

    # ----------------------------------------------------------------- templates
    templates = collect(by_kind, "TEMPLATES")
    check_duplicates(templates, "id", "template", report)
    for template in templates:
        tid = template.get("id", "<blank>")
        if not template.get("body"):
            report.error(tid, "Template has no body content")
        if not template.get("purpose"):
            report.error(tid, "Template has no stated purpose")
        if not template.get("disclaimer"):
            report.warn(tid, "Template is missing the educational-use disclaimer")
        if not template.get("instructions"):
            report.warn(tid, "Template has no instructions")

    # ---------------------------------------------------------------- interviews
    interviews = collect(by_kind, "INTERVIEWS")
    check_duplicates(interviews, "id", "interview track", report)
    for track in interviews:
        tid = track.get("id", "<blank>")
        if not track.get("questions"):
            report.error(tid, "Interview track has no questions")
        for question in track.get("questions", []):
            if not question.get("modelAnswer"):
                report.warn(tid, f"Question '{question.get('id')}' has no model answer")

    # ----------------------------------------------------------------- projects
    projects = collect(by_kind, "PROJECTS")
    check_duplicates(projects, "id", "project", report)
    for project in projects:
        pid = project.get("id", "<blank>")
        if not project.get("deliverables"):
            report.error(pid, "Project has no deliverables")
        if not project.get("evaluationCriteria"):
            report.error(pid, "Project has no evaluation criteria")
        if not project.get("modelSolution"):
            report.error(pid, "Project has no model solution")
        if not project.get("managementFeedback"):
            report.warn(pid, "Project has no management feedback")

    # --------------------------------------------------------------- accelerator
    accelerator = collect(by_kind, "ACCELERATOR")
    days = {d.get("day") for d in accelerator}
    for day in range(1, 61):
        if day not in days:
            report.error("accelerator", f"Accelerator day {day} is missing")
    for entry in accelerator:
        did = f"accelerator.day.{entry.get('day')}"
        if not entry.get("exercise"):
            report.warn(did, "Day has no practical exercise")
        if not entry.get("objectives"):
            report.warn(did, "Day has no objectives")
        if not entry.get("revision"):
            report.warn(did, "Day has no revision prompts")
        for lid in entry.get("lessonIds", []):
            if lid not in lesson_ids:
                report.warn(did, f"References unknown lesson '{lid}'")
        for qid in entry.get("assessmentIds", []):
            if qid not in question_ids:
                report.warn(did, f"References unknown question '{qid}'")

    # ------------------------------------------------------------------ glossary
    glossary = collect(by_kind, "GLOSSARY")
    check_duplicates(glossary, "id", "glossary term", report)
    glossary_ids = {g.get("id") for g in glossary}
    for term in glossary:
        gid = term.get("id", "<blank>")
        if not term.get("definition"):
            report.error(gid, "Glossary term has no definition")
        for rel in term.get("relatedIds", []):
            if rel not in glossary_ids:
                report.warn(gid, f"References unknown glossary term '{rel}'")

    # -------------------------------------------------------------------- paths
    paths = collect(by_kind, "PATHS")
    check_duplicates(paths, "id", "path", report)
    for path in paths:
        pid = path.get("id", "<blank>")
        if not path.get("moduleIds"):
            report.error(pid, "Learning path has no modules")
        for mid in path.get("moduleIds", []):
            if mid not in module_ids:
                report.error(pid, f"References unknown module '{mid}'")

    report.stats = {
        "documents": len(parsed),
        "modules": len(modules),
        "lessons": len(lessons),
        "questions": len(questions),
        "controls": len(controls),
        "frameworks": len(frameworks),
        "mappings": len(mappings),
        "scenarios": len(scenarios),
        "templates": len(templates),
        "interview tracks": len(interviews),
        "projects": len(projects),
        "accelerator days": len(accelerator),
        "glossary terms": len(glossary),
        "learning paths": len(paths),
        "lesson minutes": sum(l.get("estimatedMinutes", 0) for l in lessons),
    }
    return report


def main():
    parser = argparse.ArgumentParser(description="Validate the GRC VANGUARD content corpus.")
    default_root = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "app", "src", "main", "assets", "curriculum",
    )
    parser.add_argument("--root", default=default_root, help="Path to the curriculum assets directory")
    args = parser.parse_args()

    if not os.path.isdir(args.root):
        print(f"Content root not found: {args.root}", file=sys.stderr)
        return 2

    report = validate(args.root)
    return report.print()


if __name__ == "__main__":
    sys.exit(main())
