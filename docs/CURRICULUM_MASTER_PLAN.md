# GRC VANGUARD — Curriculum Master Plan

**Curriculum version:** 1.0.0
**Total learning hours (accelerator):** ~120 hours over 60 days
**Modules:** 14 (13 core knowledge modules + the GRC Leadership Academy)
**Extended curriculum:** continues past day 60 for sustained professional development

---

## 1. Design principles

1. **Decision-making over definition.** Every lesson must leave the learner able to *do
   something*, not merely recognise a term. Definitions appear only where they are load-bearing.
2. **Enterprise realism.** Examples are drawn from banking, healthcare, government, telecom,
   manufacturing and IT services. Where a worked example is used, the numbers are real enough
   to be defensible.
3. **Original content, attributed sources.** All explanatory prose is written for this
   platform. Standards are cited by clause reference and publisher. No copyrighted standard
   text or proprietary certification content is reproduced.
4. **Uncertainty is stated, not hidden.** Where a regulatory fact may have changed since
   writing, the content says so and tells the reader to verify it against the official source.
5. **Competency requires evidence.** The personal learning engine derives competency from
   assessment performance and practical work. Reading time alone never raises a score.
6. **The 60-day programme is a floor, not a ceiling.** The extended curriculum is explicitly
   designed to continue after day 60, because no professional mastery is guaranteed in 60 days.

---

## 2. Module map

| # | Module id | Title | Competency domain | Est. hours | Lessons |
|---|---|---|---|---|---|
| 1 | `fnd` | GRC Fundamentals | Governance | ~9 | 14 |
| 2 | `isms` | Information Security Management System (ISMS) | Compliance | ~10 | 12 |
| 3 | `nist` | NIST Frameworks and Publications | Information Security | ~10 | 12 |
| 4 | `cisa` | The CISA Professional Domain | Audit | ~9 | 12 |
| 5 | `cism` | The CISM Professional Domain | Governance / Information Security | ~9 | 11 |
| 6 | `cissp` | The CISSP Knowledge Framework | Information Security | ~9 | 11 |
| 7 | `erm` | Enterprise Risk Management | Risk Management | ~10 | 12 |
| 8 | `reg` | Regulatory and Industry Compliance | Regulatory | ~12 | 14 |
| 9 | `priv` | Privacy and Data Protection | Privacy | ~8 | 11 |
| 10 | `tprm` | Third-Party and Supply Chain Risk | Third-Party Risk | ~8 | 11 |
| 11 | `bcm` | Business Continuity and Disaster Recovery | Business Continuity | ~8 | 11 |
| 12 | `cloud` | Cloud and Emerging Technology Governance | Information Security | ~9 | 12+ |
| 13 | `secops` | Security Operations Governance | Information Security | ~8 | 11 |
| 14 | `leadership` | GRC Leadership Academy | Leadership | ~16 | 20 + 6 advanced |

**Advanced lessons** (the extended curriculum) carry ids of the form `<module>.aNN` and are
listed in each module's `advancedLessonIds`. They go beyond the core treatment: the extended
ERM module adds FAIR and cyber quantification; the extended cloud module splits AI/OT/IoT
governance; the Leadership Academy adds six senior lessons on board reporting, regulatory
change management, GRC platform selection, continuous control monitoring, escalation and
maturity transformation.

---

## 3. Module specifications

### Module 1 — GRC Fundamentals (`fnd`) — GOVERNANCE

Beginner to intermediate. The learner leaves able to explain what governance, risk management
and compliance actually are, distinguish them from one another, describe the Three Lines
Model in practice, and explain who is accountable for what.

| Lesson | Title |
|---|---|
| fnd.l01 | What Governance Actually Means |
| fnd.l02 | What Risk Management Actually Means |
| fnd.l03 | What Compliance Actually Means |
| fnd.l04 | Information Security vs Cybersecurity |
| fnd.l05 | GRC Operating Models |
| fnd.l06 | Enterprise Governance Structures and Board Oversight |
| fnd.l07 | Board Risk Committee, Audit Committee and Risk Committee |
| fnd.l08 | Executive Accountabilities: CISO, CIO, CRO, DPO |
| fnd.l09 | The Three Lines Model in Practice |
| fnd.l10 | Internal Audit vs External Audit |
| fnd.l11 | First Line, Second Line and Third Line: Where the Work Sits |
| fnd.l12 | Policies, Standards, Procedures and Guidelines |
| fnd.l13 | Risk Appetite, Risk Tolerance and Risk Limit |
| fnd.l14 | The Enterprise Control Environment, GRC Reporting and Escalation |

### Module 2 — ISMS (`isms`) — COMPLIANCE

The complete ISO/IEC 27001:2022 implementation lifecycle, clause by clause, plus the wider ISO
family (27002–27006, 19011, 27701, 22301, 31000, 20000-1, 9001, 37001, 37301, 42001).

| Lesson | Title |
|---|---|
| isms.l01 | ISMS Fundamentals and the 27001 Family |
| isms.l02 | Clause 4 — Understanding the Organisational Context |
| isms.l03 | Clause 4 — Interested Parties, Scope and the Information Security Policy |
| isms.l04 | Clause 5 — Leadership, Roles and Responsibilities |
| isms.l05 | Clause 6 — Planning and Risk Assessment Methodology |
| isms.l06 | Clause 6 — Risk Treatment, Acceptance and the Statement of Applicability |
| isms.l07 | Clause 7 — Objectives, Competence and Awareness |
| isms.l08 | Clause 7 and 8 — Documented Information and Operational Controls |
| isms.l09 | Clause 9 — Monitoring, Measurement, Internal Audit and Management Review |
| isms.l10 | Clause 10 — Nonconformity, Corrective Action and Continual Improvement |
| isms.l11 | Annex A Control Architecture: Organisational and People Controls |
| isms.l12 | Annex A Control Architecture: Physical and Technological Controls, Certification Readiness |

**Annex A companion library.** All 93 controls of ISO/IEC 27001:2022 are provided as
first-class content records (`ControlDetail`), each with: objective, why it exists, risk
addressed, implementation guidance, responsible department, evidence examples, audit testing
approach, common implementation gaps, sample interview questions, a practical scenario,
related controls, keywords and cross-framework mappings. Split 37 organisational / 8 people /
14 physical / 34 technological.

### Module 3 — NIST (`nist`) — INFORMATION_SECURITY

| Lesson | Title |
|---|---|
| nist.l01 | NIST SP 800-53 and the Control Catalogue |
| nist.l02 | SP 800-53 Families, Enhancements and Control Baselines |
| nist.l03 | NIST RMF: The Seven-Step Process |
| nist.l04 | SP 800-37 and the System Security Plan |
| nist.l05 | SP 800-30: Risk Assessment Guide |
| nist.l06 | SP 800-34: Contingency Planning, BIA and Recovery Tiers |
| nist.l07 | SP 800-61 Rev. 3: Incident Response Recommendations |
| nist.l08 | SP 800-171 and CMMC: Protecting CUI |
| nist.l09 | SP 800-207: Zero Trust Architecture |
| nist.l10 | SP 800-218 and SSDF: Secure Software Development |
| nist.l11 | Privacy Framework, AI RMF, and the Supporting SP 800 Series |
| nist.l12 | NIST CSF 2.0: The Six Functions, Profiles, Tiers and Gap Assessment |

### Module 4 — CISA (`cisa`) — AUDIT

ISACA's five domains, described in original words: auditing process; governance and
management of IT; acquisition, development and implementation; operations and business
resilience; protection of information assets. Includes audit universe construction, risk-based
allocation, sampling methodology, workpapers, root cause analysis, ITGC and application
control testing, cloud and third-party auditing, and audit communication.

### Module 5 — CISM (`cism`) — GOVERNANCE / INFORMATION_SECURITY

The four CISM domains with a strong bias toward the governance and management decisions a
CISO actually makes: strategy, programme development, metrics, budget, vendor management,
incident governance and executive reporting.

### Module 6 — CISSP (`cissp`) — INFORMATION_SECURITY

All eight ISC2 knowledge areas, weighted toward the GRC-relevant concepts (security
architecture governance, risk management, classification, access governance, cryptographic
governance, secure SDLC, vulnerability management, security assessment, incident management,
continuity and legal/regulatory requirements) rather than exploitation technique.

### Module 7 — Enterprise Risk Management (`erm`) — RISK_MANAGEMENT

ISO 31000 and COSO ERM in depth, qualitative and quantitative analysis, the 5×5 matrix with
worked calculations, inherent/residual/controlling risk, treatment selection, appetite and
tolerance, KRIs, register and heatmap construction, FAIR fundamentals and cyber risk
quantification.

### Module 8 — Regulatory (`reg`) — REGULATORY

Lesson 1 teaches how to read a regulation (law vs regulation vs standard vs framework vs
certification vs contractual requirement). The remaining lessons cover the Indian environment
(IT Act 2000, CERT-In Directions 2022, DPDP Act 2023 and Rules, RBI cyber and IT governance
directions, RBI digital payment security, PCI DSS 4.0.1, SWIFT CSP, SEBI cyber security and
cyber resilience framework, IRDAI requirements, telecom requirements), then the international
set (GDPR, HIPAA, SOX, GLBA, SOC 1, SOC 2, FedRAMP, FISMA, NIS2, DORA, EU AI Act, CCPA/CPRA).

### Module 9 — Privacy (`priv`) — PRIVACY

Privacy governance and the DPO's role, data protection principles, classification, data
inventory and mapping, the processing register, the full data lifecycle, DPIA/PIA, consent,
cross-border transfers, data subject rights, privacy incident management, privacy by design
and default, and vendor privacy assessment.

### Module 10 — Third-Party (`tprm`) — THIRD_PARTY

Lifecycle, tiering, due diligence, questionnaires, scoring, contract and SLA design,
right-to-audit, subcontractor and fourth-party exposure, cloud provider assessment, continuous
monitoring, reassessment triggers, software supply chain and SBOM, concentration risk, and
exit management.

### Module 11 — Continuity (`bcm`) — BUSINESS_CONTINUITY

ISO 22301, BIA method and worked example, critical business functions, MTPD, RTO/RPO and
recovery tiers, strategy options, DRP, crisis management and communications, tabletop design,
recovery validation, integration with incident response, and lessons learned.

### Module 12 — Cloud & Emerging (`cloud`) — INFORMATION_SECURITY / THIRD_PARTY

Shared responsibility across AWS, Azure and GCP; CSA/CIS/NIST cloud frameworks; cloud risk
assessment; cloud compliance evidence; CSPM; SaaS and SSPM governance; identity governance;
Zero Trust; container and Kubernetes governance; DevSecOps governance; AI and generative AI
governance, model governance and data leakage; Shadow AI; OT/ICS and IoT governance.

### Module 13 — Security Operations Governance (`secops`) — INFORMATION_SECURITY

The distinction between *performing* and *governing* security operations, then governance of
SOC, SIEM, incident response, vulnerability management, penetration testing, awareness, PAM and
identity governance, endpoint and detection engineering, threat intelligence, metrics,
exceptions and architecture review.

### Module 14 — GRC Leadership Academy (`leadership`) — LEADERSHIP

20 core lessons plus 6 advanced lessons. Every lesson answers, explicitly:
what a GRC Lead is expected to do; what decisions they can make; what requires escalation;
who must be consulted; what documentation is required; how success is measured; and what
mistakes inexperienced GRC managers make.

---

## 4. The 60-Day Accelerator

**Daily structure (120 minutes):**

| Block | Minutes | Purpose |
|---|---|---|
| Core concepts | 35 | Read the day's concept material and take notes |
| Framework & standard study | 25 | Read the specific clause, control or publication |
| Practical enterprise exercise | 30 | Produce a real artefact (a risk entry, a SoA line, an audit programme) |
| Assessment & scenarios | 20 | Answer the day's questions and a simulator situation |
| Revision & notes | 10 | Spaced-repetition review and note capture |

| Week | Days | Theme | Module |
|---|---|---|---|
| 1 | 1–7 | GRC Foundations | `fnd` |
| 2 | 8–14 | ISO 27001 and ISMS | `isms` |
| 3 | 15–21 | Risk Management | `erm` |
| 4 | 22–28 | Audit and CISA | `cisa` |
| 5 | 29–35 | CISM, CISSP and Security Governance | `cism` |
| 6 | 36–42 | NIST and Regulatory Compliance | `nist`, `reg` |
| 7 | 43–49 | Advanced GRC | `tprm`, `bcm`, `cloud`, `priv` |
| 8 | 50–56 | GRC Leadership | `leadership` |
| Capstone | 57–60 | Final four days | `leadership` + all |

Each day carries: objectives, 2–4 linked lessons, a written practical exercise, 3–5 assessment
items, 1–3 deliverables, and 3–5 revision prompts. Every day is built to fill a full session;
where the concept material alone would not, the day carries additional exercise and revision
load.

**Honest positioning.** The 60-day programme is designed to move a complete beginner to a
point where they can contribute under supervision in a GRC team and defend their reasoning in
a structured setting. It does not, and cannot, make someone a qualified GRC lead. The
Extended Professional Curriculum path is provided for exactly that reason.

---

## 5. Extended professional curriculum (post day 60)

| Path | Module coverage | Purpose |
|---|---|---|
| Extended Professional Curriculum | All 14 modules including every advanced lesson | The full body of knowledge, ~120+ hours |
| Enterprise GRC Leadership Academy | `leadership` only (26 lessons) | For people already technical who need the executive layer |
| ISO 27001 Practitioner | `isms` + `controls` + relevant `nist` | Certification-aligned implementation depth |
| IT Audit / CISA-aligned | `cisa` + `audit` tooling + `nist` | Audit career track |
| GRC Analyst Foundations | `fnd` + `erm` + `priv` + `reg` | Entry-level structured route |
| AI-assisted GRC | `secops` + `cloud` + `leadership` | Modern GRC with AI governance |

---

## 6. Assessment strategy

### Item taxonomy

| Question type | Count target | Purpose |
|---|---|---|
| Multiple choice | bulk | Core concept discrimination |
| Multiple select | substantial | "Select all that apply" — tests discrimination between similar controls |
| True/False | moderate | Fast confirmation of a specific rule |
| Scenario | large | Applied judgement in context |
| Management decision | large | Executive-level decisions, mirror of the simulator |
| Evidence identification | moderate | What proves a control operates |
| Compliance gap | moderate | Identify the gap against a stated requirement |
| Prioritise remediation | moderate | Ordering and sequencing under constraint |
| Risk calculation | moderate | Numeric reasoning: inherent/residual, ALE, sampling |
| Match the control | moderate | Mapping a requirement to the right control |
| Short answer | smaller | Written reasoning, self-assessed against a model answer |
| Practical assignment | smaller | Multi-part applied work |

### Examination structure

- **10 domain examinations** — one per competency domain, 25 items, 35 minutes, 70% pass.
- **5 certification-aligned practice examinations** — CISA, CISM, CISSP, ISO 27001, and
  Enterprise GRC Leadership. Original items aligned to the *published domain structure* only.
- **5 mock examinations** — 35 to 65 items, 50 to 90 minutes, escalating difficulty and pass
  marks.
- **60 daily assessments** — one per accelerator day, closing the session.
- **2 practice modes** — randomised mixed set, and weak-area remediation drawn from the
  learner's incorrect answers.

### Item quality rules

Every item carries: the stem, options each with its own rationale, a full explanation, a
per-option reason for why a wrong answer is weaker, a framework or clause reference, a
learning objective, a revision pointer, a difficulty and a competency domain. All content is
original. No leaked, recalled or copyrighted certification examination content is reproduced,
and the platform states this to the learner in the assessment screen.

---

## 7. Practical toolkit (30 instruments)

| Category | Tools |
|---|---|
| Risk | Enterprise Risk Register; Risk Assessment Calculator; Risk Heatmap; Risk Treatment Planner; Risk Acceptance Form |
| Compliance & controls | Control Assessment Register; Statement of Applicability Builder; ISO 27001 Gap Assessment; NIST CSF Gap Assessment; Policy Management Register; Exception Management Register |
| Audit | Internal Audit Planner; Audit Checklist Builder; Audit Finding Register; CAPA Tracker; Corrective Action Register; Audit Evidence Register |
| Third party | Vendor Risk Assessment; Third-Party Security Questionnaire |
| Continuity | Business Impact Analysis Tool; RTO/RPO Planning Tool; Business Continuity Checklist |
| Governance | Asset Register; Information Classification Register; Security Awareness Tracker; Management Review Report Builder |
| Reporting | GRC KPI/KRI Dashboard; Compliance Obligation Register; Regulatory Change Tracker |
| Leadership | GRC Maturity Assessment |

Each tool provides meaningful fields with help text, step-by-step usage guidance, a realistic
sample record (labelled as a sample everywhere it appears), save and edit, and CSV and PDF
export where meaningful.

---

## 8. Simulator design

One simulated organisation: **Vanguard Financial Services Ltd.** — 5,000 employees, multiple
offices, cloud infrastructure, customer financial data, internet banking, third-party
providers, a 180-person internal IT operation, a 24×7 SOC and multiple regulatory obligations.

30 scenarios across four difficulty levels, each with business context, the situation, the
information actually available (including genuine ambiguity), four plausible options,
consequences for each, the best-practice approach, management coaching, stakeholder positions
and the competencies exercised. Scenarios are drawn from the situations named in the brief:
surveillance audit pressure, access-control weaknesses, a failed vendor assessment, an
unpatched critical vulnerability, a business unit refusing a control, a regulatory evidence
request, a data protection incident, cloud migration, a management-requested risk acceptance
and a board risk dashboard.

---

## 9. Documentation library (30 templates)

Policies (16): Information Security; Access Control; Acceptable Use; Password and
Authentication; Incident Response; Vendor Security; Data Classification; Business Continuity;
Disaster Recovery; Risk Management; Change Management; Asset Management; Vulnerability
Management; Cloud Security; AI Usage; Secure Development.

Governance and assurance (10): Internal Audit Charter; Annual Audit Plan; Audit Report; Risk
Assessment Report; Risk Treatment Plan; Statement of Applicability; Vendor Assessment Report;
Business Impact Analysis; Management Review Minutes; Corrective Action Plan.

Reporting (4): GRC Monthly Report; Board Risk Report; Security Exception Form; Risk
Acceptance Form.

Every template states its purpose, instructions, required information, worked examples,
ownership, review frequency, approval requirements and an explicit educational-use disclaimer.

---

## 10. Capstone projects (10)

ISO/IEC 27001 implementation; enterprise cybersecurity risk assessment; NIST CSF 2.0 gap
assessment and roadmap; internal ITGC audit; third-party risk programme; business continuity
and DR programme; SOC 2 readiness; Indian financial-sector compliance register; enterprise GRC
operating model; board risk report presentation. Each carries background, scope, objectives,
starting information, activities, deliverables, evaluation criteria, a detailed model solution
and management-level feedback.

---

## 11. Interview and career preparation (12 tracks)

GRC Analyst, IT Auditor, Information Security Consultant, ISO 27001 Consultant, GRC
Specialist, Risk Manager, Compliance Manager, GRC Manager, Senior GRC Manager, Enterprise GRC
Lead, Head of GRC, CISO. Each track carries 8–12 questions across technical, framework,
scenario, management, leadership, stakeholder and board types, with model answers, evaluation
points, common mistakes, negotiation guidance and the evaluation dimensions used to score
answers.

---

## 12. Competency model

Ten domains: Governance, Risk Management, Compliance, Audit, Information Security, Privacy,
Business Continuity, Third-Party Risk, Regulatory Management, Leadership.

**Scoring.** A domain score is `weightedAccuracy × 0.70 + breadth × 0.30`, multiplied by a
recency factor (evidence older than 30/90/180 days is discounted by 10/20/30%), plus a small
credit for completed practical work. Difficulty weights: beginner 0.8, intermediate 1.0,
advanced 1.3, expert 1.6. Breadth requires at least five items in a domain before a domain can
be called strong or weak.

**Confidence.** Reported separately from the score: `evidence × 0.6 + repetition × 0.4`,
scaled by recency. A high score on four questions shows a low confidence band, and the
platform says so.

**Never derived from time.** Study minutes are tracked for streaks and goal adherence only.
They contribute nothing to a competency score.
