# GRC VANGUARD Content Schema (authoritative)

All curriculum content lives under `app/src/main/assets/curriculum/`.

## File envelope

Every JSON file has the same envelope:

```json
{
  "schemaVersion": "1.0.0",
  "kind": "LESSONS",
  "items": [ ... ]
}
```

`kind` must be one of:

| kind | Item type | Notes |
|---|---|---|
| `MODULES` | `CurriculumModule` | One file recommended |
| `LESSONS` | `Lesson` | Split by module for maintainability |
| `PATHS` | `LearningPath` | |
| `QUESTIONS` | `Question` | Split by domain recommended |
| `FRAMEWORKS` | `FrameworkDefinition` | |
| `MAPPINGS` | `MappingPair` | |
| `CONTROLS` | `ControlDetail` | ISO 27001:2022 Annex A, split by category |
| `SCENARIOS` | `Scenario` | |
| `TOOLS` | `ToolDefinition` | Optional extension of the built-in registry |
| `TEMPLATES` | `TemplateDocument` | |
| `INTERVIEWS` | `InterviewTrack` | |
| `PROJECTS` | `CapstoneProject` | |
| `ACCELERATOR` | `AcceleratorDay` | Exactly 60 items, `day` 1..60 |
| `GLOSSARY` | `GlossaryTerm` | |

The app parses these with kotlinx.serialization. **Unknown fields are ignored;
missing optional fields use defaults; missing required fields cause the item to be
skipped and reported in `ContentIndex.loadErrors`.**

---

## MODULES — `CurriculumModule`

```json
{
  "id": "fnd",
  "order": 1,
  "title": "GRC Fundamentals",
  "subtitle": "Governance, risk and compliance from first principles",
  "description": "One paragraph describing what the learner will be able to do.",
  "iconKey": "shield",
  "estimatedMinutes": 240,
  "competencyDomain": "GOVERNANCE",
  "lessonIds": ["fnd.l01", "fnd.l02"],
  "advancedLessonIds": []
}
```

`lessonIds` must reference existing lessons. `advancedLessonIds` is for the extended
post-60-day curriculum; those lessons still appear in the module screen.

---

## LESSONS — `Lesson`

```json
{
  "id": "fnd.l01",
  "moduleId": "fnd",
  "topic": "Governance",
  "subtopic": "Definitions",
  "title": "What Governance Actually Means",
  "summary": "Two or three sentences that can be read on their own.",
  "difficulty": "BEGINNER",
  "objectives": ["Define governance", "Distinguish governance from management"],
  "estimatedMinutes": 45,
  "sections": [
    {
      "heading": "Definition",
      "body": "Two to five paragraphs of teaching prose. Be specific and operational.",
      "bullets": ["Optional bullet list"],
      "callout": "Optional highlighted takeaway.",
      "table": [["Column A", "Column B"], ["row 1", "row 2"]]
    }
  ],
  "examples": [
    {
      "title": "Board oversight in a mid-sized bank",
      "context": "Describe the situation and what a learner would actually do.",
      "outcome": "What a good outcome looks like.",
      "sector": "Banking"
    }
  ],
  "practicalExercise": {
    "title": "Draft a governance account for a fictional company",
    "brief": "Two or three sentences of instruction.",
    "steps": ["Step one", "Step two"],
    "deliverable": "What the learner produces.",
    "evaluationCriteria": ["How it will be judged"],
    "estimatedMinutes": 30
  },
  "assessmentQuestionIds": ["q.fnd.001"],
  "references": ["ISO 31000:2018 Clause 4", "IIA Global Internal Audit Standards"],
  "competencyDomain": "GOVERNANCE",
  "version": "1.0.0",
  "lastReviewed": "2026-01-15",
  "tags": ["governance", "basics"]
}
```

**Rules**
- `id` must be unique and must follow `<module>.<nn>` (lessons) or `<module>.a<nn>` (advanced lessons).
- A lesson with no `sections` and no `practicalExercise` is a validation ERROR.
- `sections` should be substantial. A lesson must fill 30-60 minutes of reading plus work.
- `assessmentQuestionIds` must resolve to questions in the bank.

Difficulty values: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`.

---

## QUESTIONS — `Question`

```json
{
  "id": "q.fnd.001",
  "domain": "GOVERNANCE",
  "topic": "Governance definitions",
  "type": "MULTIPLE_CHOICE",
  "difficulty": "INTERMEDIATE",
  "stem": "A board asks the CISO to confirm that the organisation's risk appetite has been applied to a new business line. What is the strongest response?",
  "options": [
    { "id": "a", "text": "Option text", "correct": true, "rationale": "Why this option is correct" },
    { "id": "b", "text": "Option text", "correct": false, "rationale": "Why this option is weaker" }
  ],
  "correctOptionIds": ["a"],
  "explanation": "The full teaching explanation of the answer.",
  "whyOthersWrong": { "b": "Specific reason this option is wrong" },
  "frameworkRef": "ISO 31000:2018 Clause 5.2",
  "learningObjective": "Apply risk appetite to a new business line",
  "revisionRef": "fnd.l03",
  "points": 1,
  "tags": ["ISO 31000"]
}
```

**Domain must be exactly one of:**

`GOVERNANCE`, `RISK_MANAGEMENT`, `COMPLIANCE`, `AUDIT`, `INFORMATION_SECURITY`,
`PRIVACY`, `BUSINESS_CONTINUITY`, `THIRD_PARTY`, `REGULATORY`, `LEADERSHIP`

**Types and their required fields**

| type | requires | notes |
|---|---|---|
| `MULTIPLE_CHOICE` | `options` (>=3), `correctOptionIds` (1) | |
| `MULTIPLE_SELECT` | `options` (>=4), `correctOptionIds` (>=2) | |
| `TRUE_FALSE` | `options` (exactly 2), `correctOptionIds` (1) | |
| `SCENARIO` | `options` (>=4), `correctOptionIds` (1) | stem sets a situation |
| `EVIDENCE_IDENTIFICATION` | `options`, `correctOptionIds` | stem asks what evidence proves a control |
| `COMPLIANCE_GAP` | `options`, `correctOptionIds` | stem asks what gap exists against a requirement |
| `PRIORITISE_REMEDIATION` | `options`, `correctOptionIds` | stem asks what to fix first and why |
| `MANAGEMENT_DECISION` | `options` (>=4), `correctOptionIds` (1) | stem is a management situation |
| `RISK_CALCULATION` | `options`, `numericAnswer` | see below |
| `MATCH_CONTROL` | `matchPairs`, `correctOptionIds` | see below |
| `SHORT_ANSWER` | `modelAnswer` | self-assessed |
| `PRACTICAL_ASSIGNMENT` | `modelAnswer` and/or `explanation` | self-assessed |

`RISK_CALCULATION` adds:

```json
"numericAnswer": {
  "expected": 15,
  "tolerance": 0.01,
  "unit": "score",
  "formula": "likelihood x impact",
  "workedExample": "3 x 5 = 15"
}
```

`options` for a calculation question are the candidate numeric answers, e.g.
`{"id":"a","text":"6","correct":false,"rationale":"..."}`.

`MATCH_CONTROL` adds:

```json
"matchPairs": [
  { "leftId": "l1", "left": "Left item text", "right": "Right item text", "note": "Why" }
],
"correctOptionIds": ["l1"]
```

Here `correctOptionIds` lists the `leftId` values that are correctly matched; the
learner's mapping is scored proportionally.

**Rules**
- `correctOptionIds` must agree exactly with the set of options flagged `"correct": true`.
- Every option must have a non-empty `rationale`.
- `explanation` must be non-empty.
- Questions must be **original**. Never reproduce, paraphrase closely, or reconstruct
  recalled/leaked examination content from ISACA, ISC2, PCI SSC, AICPA or any other body.
- A question must be answerable from the curriculum. Point to the lesson with `revisionRef`.

---

## FRAMEWORKS — `FrameworkDefinition`

```json
{
  "id": "iso27001",
  "title": "ISO/IEC 27001:2022 - Information security management systems - Requirements",
  "publisher": "International Organization for Standardization (ISO) and International Electrotechnical Commission (IEC)",
  "kind": "STANDARD",
  "edition": "Third edition, 2022",
  "publicationDate": "2022-10",
  "applicability": "Any organisation intending to demonstrate systematic management of information security risk.",
  "officialUrl": "https://www.iso.org/standard/27001",
  "lastVerified": "2026-01-15",
  "status": "ACTIVE",
  "notes": "Supersedes ISO/IEC 27001:2013.",
  "tags": ["ISO", "ISMS"]
}
```

`kind` values: `LAW`, `REGULATION`, `STANDARD`, `FRAMEWORK`, `CODE_OF_PRACTICE`,
`CERTIFICATION`, `CONTRACTUAL_REQUIREMENT`, `GUIDANCE`.
`status` values: `ACTIVE`, `SUPERSEDED`, `WITHDRAWN`, `DRAFT`.

---

## MAPPINGS — `MappingPair`

```json
{
  "id": "map_iso_nist",
  "title": "ISO/IEC 27001:2022 vs NIST CSF 2.0",
  "frameworkA": "iso27001",
  "frameworkB": "nist_csf_2_0",
  "summary": "One paragraph on what the mapping is and is not.",
  "scopeDifferences": "How the scope of the two frameworks differs.",
  "warning": "A mapping does not establish compliance with either framework.",
  "entries": [
    {
      "id": "e1",
      "aRef": "ISO 27001:2022 A.5.15",
      "bRef": "NIST CSF 2.0 PR.AA-01",
      "commonObjective": "The shared objective in plain language.",
      "differences": "Where they differ in scope or requirement.",
      "evidenceReuse": "What evidence satisfies both.",
      "additionalRequirements": "What the second framework adds.",
      "implementationNote": "Practical advice for the implementer."
    }
  ]
}
```

---

## CONTROLS — `ControlDetail` (ISO/IEC 27001:2022 Annex A)

```json
{
  "id": "ctl_a_5_1",
  "controlId": "A.5.1",
  "title": "Policies for information security",
  "category": "ORGANIZATIONAL",
  "objective": "What the control achieves.",
  "whyItExists": "The reasoning and history behind the control.",
  "riskAddressed": "The risk this control addresses.",
  "implementationGuidance": "Concrete, practical implementation advice.",
  "responsibleDepartment": "GRC / Information Security",
  "evidenceExamples": ["Approved policy", "Acknowledgement records"],
  "auditTesting": "How an auditor would test this control.",
  "commonGaps": ["Gap one", "Gap two"],
  "interviewQuestions": ["Question one?"],
  "scenario": "A short realistic scenario showing the control in action.",
  "relatedControls": ["A.5.2"],
  "keywords": ["policy", "governance"],
  "mappings": { "nist_csf_2_0": "GV.PO-01", "nist_800_53": "PM-9" }
}
```

**All 93 Annex A controls from ISO/IEC 27001:2022 must be present**, split as:
- 37 `ORGANIZATIONAL` (A.5.1 to A.5.37)
- 8 `PEOPLE` (A.6.1 to A.6.8)
- 14 `PHYSICAL` (A.7.1 to A.7.14)
- 34 `TECHNOLOGICAL` (A.8.1 to A.8.34)

Write original explanations. Do **not** reproduce the published standard text.

---

## SCENARIOS — `Scenario`

```json
{
  "id": "scn_iso_surveillance",
  "title": "Surveillance audit in six weeks and the SoA is not signed",
  "difficulty": "ADVANCED",
  "category": "Certification",
  "context": "Business context paragraph.",
  "situation": "The decision the learner must make.",
  "informationAvailable": ["What the learner knows"],
  "options": [
    {
      "id": "a",
      "text": "Option text",
      "score": 90,
      "consequence": "What happens if this option is chosen.",
      "whyWeaker": "What a stronger option would have done (empty for the best option)."
    }
  ],
  "bestPractice": "The management-correct approach, explained.",
  "managementGuidance": "The coaching point.",
  "competencies": ["COMPLIANCE", "LEADERSHIP"],
  "stakeholderViews": { "CISO": "..." },
  "tags": ["ISO 27001", "audit"]
}
```

**Rules**
- At least 3 options; exactly one option should score 80 or above.
- Every option must have a non-empty `consequence`.
- `difficulty` in `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`.

---

## TEMPLATES — `TemplateDocument`

```json
{
  "id": "tpl_information_security_policy",
  "title": "Information Security Policy",
  "category": "Policies",
  "purpose": "Why this document exists.",
  "instructions": ["How to complete it"],
  "requiredInformation": ["What must be captured"],
  "exampleEntries": ["Worked examples"],
  "ownership": "CISO",
  "reviewFrequency": "Annual",
  "approvalRequired": "Board Risk Committee",
  "disclaimer": "Educational example only...",
  "body": "The actual template text with headings and placeholders in square brackets.",
  "competencyDomain": "GOVERNANCE"
}
```

---

## INTERVIEWS — `InterviewTrack`

```json
{
  "id": "iv_grc_manager",
  "role": "GRC Manager",
  "level": "Manager",
  "summary": "What the interviewer is testing.",
  "competencies": ["..."],
  "questions": [
    {
      "id": "q1",
      "type": "MANAGEMENT",
      "prompt": "The question.",
      "modelAnswer": "A structured model answer with reasoning.",
      "evaluationPoints": ["What a strong answer demonstrates"],
      "commonMistakes": ["What interviewers hear often"]
    }
  ],
  "negotiationGuidance": ["Career and salary negotiation points"],
  "evaluationDimensions": ["Technical accuracy", "Business understanding"],
  "typicalEmployerFocus": "What this employer really wants to test."
}
```

Question `type` values: `TECHNICAL`, `FRAMEWORK`, `SCENARIO`, `MANAGEMENT`,
`LEADERSHIP`, `STAKEHOLDER`, `BOARD`.

---

## PROJECTS — `CapstoneProject`

```json
{
  "id": "prj_iso27001_itco",
  "title": "Implement ISO 27001 in a fictional IT company",
  "difficulty": "ADVANCED",
  "background": "The company and the situation.",
  "scope": "What is in and out of scope.",
  "objectives": ["..."],
  "startingInformation": ["Facts the learner is given"],
  "activities": ["Required activities"],
  "deliverables": ["What the learner produces"],
  "evaluationCriteria": ["How it is judged"],
  "modelSolution": "A worked model answer.",
  "managementFeedback": "Management-level commentary.",
  "estimatedHours": 8,
  "competencyDomain": "COMPLIANCE"
}
```

---

## ACCELERATOR — `AcceleratorDay`

```json
{
  "day": 1,
  "title": "Introduction to enterprise GRC",
  "focus": "One-line description of the day's centre of gravity.",
  "moduleId": "fnd",
  "lessonIds": ["fnd.l01"],
  "exercise": "A practical enterprise exercise written as a paragraph.",
  "assessmentIds": ["q.fnd.001", "q.fnd.002"],
  "deliverables": ["What the learner produces"],
  "revision": ["Revision prompts"],
  "minutesCore": 35,
  "minutesFramework": 25,
  "minutesExercise": 30,
  "minutesAssessment": 20,
  "minutesRevision": 10,
  "objectives": ["..."]
}
```

Exactly 60 items, `day` 1..60, no gaps. The minute fields should sum to 120.

---

## GLOSSARY — `GlossaryTerm`

```json
{
  "id": "gl_risk_appetite",
  "term": "Risk appetite",
  "definition": "The definition.",
  "aliases": ["Risk tolerance"],
  "relatedIds": ["gl_risk_tolerance"],
  "framework": "ISO 31000:2018",
  "note": "A note on common confusion."
}
```

---

## Validation

Run `python tools/validate_content.py` from the repository root. It mirrors the
in-app validator (`ContentValidator.kt`) and reports:
duplicate identifiers, dangling references, questions with no valid answer,
empty lessons, missing objectives, incomplete scenarios and missing accelerator days.

The app also validates the corpus at runtime. Load errors appear in
`ContentIndex.loadErrors` and are surfaced in Settings.
