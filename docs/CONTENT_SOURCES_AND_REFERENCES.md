# GRC VANGUARD — Content Sources and References

**Content version:** 1.0.0
**Last framework-registry verification:** 2026-01-15

This document governs what GRC VANGUARD may cite, how it attributes sources, and what it
must never reproduce. It applies to every piece of content in
`app/src/main/assets/curriculum/`.

---

## 1. Source priority

When the platform needs to describe a standard, framework, law or regulatory instrument, it
draws on the following order of authority:

| Priority | Source | Publisher |
|---|---|---|
| 1 | Official standard descriptions and published metadata | ISO, IEC |
| 2 | Official certification domain descriptions and outlines | ISACA (CISA, CISM, CRISC, CGEIT) |
| 3 | Official certification knowledge framework and exam outline | ISC² (CISSP) |
| 4 | Official publications and revisions | NIST |
| 5 | Official directions, circulars and frameworks | Reserve Bank of India |
| 6 | Official directions | CERT-In (Ministry of Electronics and IT, Government of India) |
| 7 | Official circulars and frameworks | SEBI, IRDAI, TRAI |
| 8 | Official publications | MeitY, Government of India |
| 9 | Official regulation text and supervisory guidance | European Commission and national regulators |
| 10 | Official standard and supporting documents | PCI Security Standards Council |
| 11 | Official trust services criteria and SOC guidance | AICPA |
| 12 | Official control benchmarks and benchmarks | Center for Internet Security (CIS) |
| 13 | Official cloud guidance | Cloud Security Alliance |
| 14 | Official guidance | US Cybersecurity and Infrastructure Security Agency (CISA) |

**Local and official URLs.** Every entry in the framework registry carries the publisher's
official URL so a learner can go to the source. The platform does not mirror publisher text
at these URLs.

---

## 2. Copyright policy

### Prohibited

- Reproducing the text of a copyrighted standard, including ISO/IEC 27001:2022, ISO 31000,
  ISO 22301, NIST publications, PCI DSS requirements, the AICPA Trust Services Criteria, the
  CIS Controls, or any ISACA or ISC² publication.
- Reproducing, paraphrasing closely, reconstructing or otherwise circulating leaked, recalled
  or proprietary certification examination questions from ISACA, ISC², PCI SSC, AICPA, IIA or
  any other body.
- Reproducing paid course material, textbooks, or instructor notes.
- Scraping or bulk-copying any publisher's content into the application.
- Presenting the platform as affiliated with, endorsed by, or connected to ISO, ISACA, ISC²,
  NIST, PCI SSC, AICPA, CIS, CSA, RBI, CERT-In, SEBI, IRDAI, TRAI, the European Commission or
  any other regulator.

### Permitted, and what the platform does

- **Short factual identifiers.** A control title such as "Policies for information security"
  or a clause number such as "ISO/IEC 27001:2022 Clause 9.3" is a factual identifier, not
  protected expression. The platform uses these freely.
- **Original explanations.** Every explanation of what a control means, why it exists, how to
  implement it, how to audit it and where organisations fail is written from scratch for
  this platform.
- **Structural summaries.** The *structure* of a framework — its parts, functions,
  categories, domains and their published names — is factual and is described in the
  platform's own words.
- **Paraphrase with attribution.** Where a requirement must be described, the platform
  paraphrases the requirement, attributes it to the publisher and clause, and states clearly
  that the description is a summary rather than the source text.
- **Linking.** Every framework entry links to the publisher's official page.

### Required labelling

Content that summarises a standard must carry an explicit note. The Annex A control screens
carry:

> *"This is an original explanatory summary written for education. It is not a reproduction of
> the ISO standard text. Confirm the exact control wording against the published standard
> before relying on it in an audit."*

Practice examinations carry:

> *"All questions in this platform are original, written for learning. No proprietary
> certification examination content from ISACA, ISC², PCI SSC, AICPA or any other body is
> reproduced here."*

---

## 3. Acronym disambiguation

**CISA** is used in this platform with two distinct meanings, and they are never mixed in the
same sentence:

| Meaning | Expansion | Usage |
|---|---|---|
| Certification | Certified Information Systems Auditor, issued by ISACA | `cisa` module, CISA-aligned examinations, the CISA interview track |
| Agency | Cybersecurity and Infrastructure Security Agency, United States Department of Homeland Security | SP 800-61, SP 800-137, CISA advisories, national infrastructure guidance |

The content registry distinguishes them by the publisher field and by tagging.

**Other acronyms that are disambiguated in the glossary:** ISACA (the professional body) vs
the CISA certification vs the CISA agency; SOC (service organisation control) reports vs
Security Operations Centre; GRC (governance, risk and compliance) vs SOC 2 (a framework vs a
reporting framework); PAV/NEPTUNE (logical access control models); COBIT (governance) vs
CO2 (emissions); ISMS (the management system) vs ISO/IEC 27001 (the requirement standard) vs
ISO/IEC 27002 (the guidance standard).

---

## 4. Regulatory currency

Regulatory content is the highest-risk material in the platform, so it carries explicit
currency handling:

1. **Every regulatory lesson states the issuing body and the instrument name.** A learner must
   always be able to tell *who* requires something and *which document*.
2. **Effective dates are stated with a verification caveat** where the platform cannot be
   certain of the current consolidated version. The standard phrasing is:
   *"as published in the consolidated version available at the time of writing; verify against
   the issuer's current publication before relying on it."*
3. **The framework registry records `lastVerified` and `status`.** A superseded or withdrawn
   instrument is flagged as such rather than silently presented as current.
4. **The regulatory change tracker exists precisely because the platform cannot track change
   automatically.** It is the learner's tool for doing so, and the Settings screen explains
   that regulatory facts require verification.
5. **Nothing regulatory is presented as professional advice.** Where a decision has legal
   consequences, the content says to take legal advice.

**Specific caution applied to Indian instruments.** RBI, CERT-In, SEBI, IRDAI and TRAI publish
frequently and their requirements are consolidated into master directions that are revised.
Every entry that touches those instruments carries a note pointing the learner to the
issuer's current consolidated publication.

---

## 5. Framework registry

The registry (`assets/curriculum/frameworks/frameworks.json`) records for each entry:
`id`, `title`, `publisher`, `kind`, `edition`, `publicationDate`, `applicability`,
`officialUrl`, `lastVerified`, `status`, `notes` and `tags`.

`kind` distinguishes:

| Kind | Meaning | Examples |
|---|---|---|
| `LAW` | Enacted statute | IT Act 2000, DPDP Act 2023, SOX, HIPAA |
| `REGULATION` | Binding subordinate instrument | RBI directions, CERT-In directions, EU AI Act, NIS2, DORA |
| `STANDARD` | Published voluntary consensus standard | ISO/IEC 27001:2022, ISO 31000, ISO 22301 |
| `FRAMEWORK` | Structured approach or model | NIST CSF 2.0, NIST RMF, COSO ERM, COBIT 2019 |
| `CODE_OF_PRACTICE` | Industry guidance | CIS Controls, CSA CCM, FAIR |
| `CERTIFICATION` | A certification scheme | CISA, CISM, CISSP, ISO/IEC 27001 certification, SOC 2 attestation |
| `CONTRACTUAL_REQUIREMENT` | Binding by contract | SWIFT Customer Security Programme, PCI DSS (where contractually adopted), customer security schedules |
| `GUIDANCE` | Non-binding official guidance | CISA advisories, NIST informative guidance, regulator FAQs |

**Superseded and withdrawn instruments** are retained in the registry with
`status: "SUPERSEDED"` or `"WITHDRAWN"` and a note naming the successor. They are not
deleted, because learners need to recognise what they may encounter in older documents.

---

## 6. References used in the curriculum

### ISO and IEC
ISO/IEC 27001:2022 · ISO/IEC 27002:2022 · ISO/IEC 27003 · ISO/IEC 27004 · ISO/IEC 27005 ·
ISO/IEC 27006-1 · ISO/IEC 27701 · ISO 19011 · ISO 22301:2019 · ISO 31000:2018 ·
ISO/IEC 20000-1 · ISO 9001:2015 · ISO 37001:2018 · ISO 37301:2020 · ISO/IEC 42001:2023 ·
ISO 19600 (compliance management concepts)

### NIST
SP 800-37 Rev. 2 · SP 800-53 Rev. 5 · SP 800-30 Rev. 1 · SP 800-34 Rev. 1 ·
SP 800-39 · SP 800-61 Rev. 3 · SP 800-63 · SP 800-137 · SP 800-161 Rev. 1 · SP 800-171 Rev. 3 ·
SP 800-180 · SP 800-190 · SP 800-207 · SP 800-218 · SP 800-82 Rev. 3 · SP 800-86 ·
SP 800-160 · SP 800-171 (CUI) · CSF 2.0 · Privacy Framework · AI RMF (AI 100-1) ·
SP 800-218 SSDF

### ISACA and ISC²
ISACA CISA, CISM, CRISC, CGEIT domain structures · ISC² CISSP eight-domain knowledge
framework

### Regulatory and supervisory
IT Act 2000 (India) · CERT-In Directions (2022) · Digital Personal Data Protection Act 2023
and its Rules/notifications · RBI Master Directions on cyber security framework, IT
governance, digital payment security and data protection · SEBI cyber security and cyber
resilience framework for market intermediaries · IRDAI information and cyber security
guidelines · TRAI telecom cyber security rules · PCI DSS 4.0.1 · SWIFT Customer Security
Programme · GDPR · NIS2 · DORA · EU AI Act · CCPA/CPRA · HIPAA · SOX · GLBA · FedRAMP ·
FISMA

### Assurance and governance
AICPA SOC 1 / SOC 2 / SOC for Cybersecurity and the Trust Services Criteria · IIA Global
Internal Audit Standards · IIA Three Lines Model · COSO ERM (2017) · COSO Internal Control
(2013) · COBIT 2019 · ISO 19600 · CIS Controls v8 · CSA CCM and STAR Registry · FAIR (Factor
Analysis of Information Risk) · TOGAF

### Indian regulators
Reserve Bank of India · CERT-In · SEBI · IRDAI · TRAI · MeitY · PNAB (for certification
accreditation context)

---

## 7. Original content authorship

All instructional prose, scenarios, exercises, templates, control explanations, questions,
model answers and case studies in GRC VANGUARD are original works authored for this platform.

Where a lesson teaches a published domain structure — for example the ISACA CISA domain
list — the *structure* is described in the platform's own words with attribution, and the
lesson explicitly states that it is not official certification material and carries no
endorsement.

The simulator organisation, **Vanguard Financial Services Ltd.**, is fictional. Its people,
systems, board names, incident numbers and financial figures are invented for training. The
platform states this on the simulator landing screen.

---

## 8. Review and currency maintenance

| Content type | Review trigger | Owner |
|---|---|---|
| Lessons (non-regulatory) | Annual, or on a framework revision | Content team |
| Annex A control library | On any ISO/IEC 27001 revision | Content team |
| Framework registry entries | Quarterly | Content team |
| Regulatory lessons | Quarterly, and immediately on a published change | Content team with regulatory review |
| Assessment items | Annually, and on a framework change | Content team |
| Templates | Annually | Content team |
| Simulator scenarios | Annually | Content team |

Every content record carries `version` and `lastReviewed` so the review state is inspectable
rather than assumed.

---

## 9. Attribution statement shown to learners

> GRC VANGUARD is an independent educational platform. It is not affiliated with, endorsed by,
> or connected to ISO, IEC, ISACA, ISC², NIST, PCI Security Standards Council, AICPA, CIS,
> CSA, COSO, IIA, or any government or financial regulator. Standards, frameworks and
> regulations are the intellectual property of their publishers and are referenced here for
> identification and education. Always consult the current published source before relying on
> any requirement.
