# Implementation plan: Auth workflow migration

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. The [constitution](../../.specify/memory/constitution.md)
and [development workflow](../../docs/development-verification.md) retain their
semantic, provenance and execution boundaries. This new documentation migration
does not reopen historical accepted increments.

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

Use the existing project-local spec/plan/tasks overrides. Add only this package's
four Markdown artifacts. Preserve existing feature packages and historical
receipts. A central migration index is integrated separately; no shared index
or component page is edited here.

- [Implementation task source](../../docs/component-roadmaps/auth.md#remaining-tasks-and-evidence): select and update implementation work there, never by copying its checklist into this package.
- [Component acceptance](../../docs/component-roadmaps/auth.md#acceptance-and-integration): retains the semantic finish line.
- [Component release outcome](../../docs/component-roadmaps/auth.md#release-outcome): remains open independently of migration status.

Early artifact authentication consumes source/artifact identity and trust policy independently. Capability analysis follows Types, Effects and profile legality, then supplies ownership/safety and MIR. Build/Release retain signing and custody ownership.

## Validation and evidence

Validate all four artifacts, relative paths and heading fragments, ASCII text,
semantic directory names, no copied implementation checklist, and no private
execution paths or raw logs. Negative checks must detect a missing link target
and an unsupported acceptance label. Run existing structural documentation and
full-language roadmap validators; these prove structure, not language semantics.
No compiler execution, external Specify CLI invocation or automatic command
discovery is claimed. Record commands/results and limits in evidence.md.

## Delivery and remaining work

Keep partial work in a draft PR. Independent review must inspect the exact
candidate, link ownership and separation of migration from implementation
status. Integration records a verified merge receipt without rewriting earlier
validation provenance. Component implementation then continues from its canonical
list, not from an inferred all-feature completion state.

## Names, callers and compatibility

Only `auth-workflow` and the four standard artifact filenames are new.
No caller, import, load order, manifest hash, diagnostic or wire contract changes.
