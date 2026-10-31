# Implementation plan: Auth task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: capability and artifact-trust maintainer.
Input revision: `846644db7402c26a421eb7afd52615d52f145590`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. The [constitution](../../.specify/memory/constitution.md)
and [development workflow](../../docs/development-verification.md) retain their
semantic, provenance and execution boundaries. Only task editing authority changes; historical acceptance is not reopened.

The following retained references are routed here for navigation. Cross-cutting
contracts may also be used by other component packages; inclusion is not
exclusive ownership or semantic conformance acceptance.

- [D8: Safety Philosophy & Charter](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
- [L12: Compile-Time Evaluation Specification](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md)
- [L15: Capability Provider Specification](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md)
- [SAFE10: Capability Security Model](../../docs/phase-02-safety/039-safe10-capability-security-model.md)
- [SAFE11: Taint Tracking and Input Safety](../../docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md)
- [SAFE13: AI Tool Safety](../../docs/phase-02-safety/042-safe13-ai-tool-safety.md)
- [SAFE14: Supply-Chain Safety](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md)
- [SAFE15: Safety Proof and Certificate Model](../../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md)
- [R11: Runtime Capability Enforcement Design](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)
- [S3: Canonical Data Format Specification](../../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md)
- [S9: Artifact Schema Specification](../../docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md)
- [A8: AI Policy and Safety Model](../../docs/phase-11-ai-and-agentic-programming/161-a8-ai-policy-and-safety-model.md)
- [A11: Prompt Injection and Tool Misuse Defense Specification](../../docs/phase-11-ai-and-agentic-programming/164-a11-prompt-injection-and-tool-misuse-defense-specification.md)
- [PKG6: Capability and Permission Manifest Specification](../../docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md)
- [PKG8: Package Safety and Audit Metadata Specification](../../docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md)
- [PKG10: Supply-Chain Security and Provenance Specification](../../docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md)
- [PKG12: Artifact Signing, Verification and SBOM Specification](../../docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md)
- [GOV4: Security Review Process](../../docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md)

## Approach and exact files

Move the five canonical rows to the marked
[implementation region](tasks.md#implementation-tasks), preserving component/
heading/title IDs, titles, Open statuses, evidence baselines and closing
criteria. The roadmap links directly to the sole task ledger. Preserve the entire historical tasks file as a frozen suffix;
migration checkboxes do not indicate implementation completion. Preserve all
five task records and their status/criteria/history; later status updates remain
explicit in the ledger and require checkbox/Accepted; consistency.

Scope is the Auth roadmap and four workflow artifacts.

Three bootstrap links keep their spelling. Runtime-enforcement and provider
contract links use ../../docs/ in the ledger and project back to ../phase-...
in the roadmap, preserving actual targets. Existing file and symlink checks
reject escapes before writing. Fragments, queries, encoded paths, images,
reference links and unsupported schemes remain rejected. HTTPS receipts follow
the existing common grammar. No second link catalog is introduced.

Early artifact authentication remains independently callable from source/
artifact identity and trust policy. Capability analysis follows Types, Effects
and profile legality, then supplies ownership/safety and MIR. Build/Release
retain signing and custody. Effects, signatures and manifests do not grant
authority; hosted compatibility views do not close these outcomes.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Check the five Markdown documents for ASCII and file/heading links, including
missing-file/heading cases. Run whitespace and clean exact-base/head/tree
integration preflight. Compiler semantic suites are outside scope.

## Delivery and remaining work

Independent review inspects the complete public payload and proposed PR body.
Publish a draft for maintainer current-head review, with exact identities and
validation receipts. Normal integration follows maintainer disposition and real
repository gates. All five implementation outcomes remain open.

## Names, callers and compatibility

compiler APIs, runtime/authentication policies, schemas, diagnostics and wire
identities remain unchanged. No legacy rename or compiler source/test edit is
part of this task-authority migration.
