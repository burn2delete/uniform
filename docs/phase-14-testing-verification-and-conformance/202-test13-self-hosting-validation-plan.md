# TEST13 - Self-Hosting Validation Plan

Sequence: 202
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines validation for the self-hosting transition. Gravity intends
to move reader, macroexpander, analyzer, MIR, passes, backends, package tools,
build tools, and standard library into Gravity over time. Self-hosting is
accepted only when staged compilers produce equivalent artifacts, preserve
conformance, and shrink trusted inputs with recorded provenance.

Self-hosting is not complete because source is written in Gravity; it is
complete when the staged compiler is reproducible and equivalent enough for the
declared stage.

## Stages

Validation stages include:

- seed compiler;
- reader and macroexpander in Gravity;
- analyzer and typed core in Gravity;
- MIR and optimization passes in Gravity;
- backend subset in Gravity;
- package/build subset in Gravity;
- standard library subset in Gravity;
- full self-hosted compiler candidate;
- rebuild and compare stage.

## Requirements

- Each stage MUST declare trusted inputs and generated artifacts.
- Stage compilers MUST run the relevant conformance suites.
- Rebuilds MUST record source hash, compiler hash, lockfile hash, target matrix, and output hash.
- Stage-to-stage comparisons MUST identify expected and unexpected differences.
- Self-hosted compiler output MUST preserve diagnostics and artifact schemas.
- Trusting-trust mitigation MUST include reproducible builds or diverse double compilation where applicable.
- Unsafe code in the compiler implementation MUST carry audit metadata.
- Bootstrap provenance MUST be signed or otherwise verifiable for releases.

## Semantic Dependencies

- `BOOT1` through `BOOT8` define bootstrap plans.
- `C1` through `C18` define compiler implementation scope.
- `PKG7` and `PKG10` define reproducibility and provenance.
- `TEST1` through `TEST12` define suites that staged compilers must pass.
- `GOV9` defines unsafe code governance.

## Outputs and Artifacts

Self-hosting validation emits:

- stage manifest;
- stage compiler artifact;
- conformance report;
- rebuild log;
- stage comparison report;
- provenance attestation;
- trusted computing base delta;
- unsafe audit report.

## Example

```clojure
(deftest stage2-rebuild-equivalence
  {:stage :stage2
   :inputs [:gravity-source :stage1-compiler :lockfile]
   :outputs [:stage2-compiler :stdlib]
   :compare [:artifact-hash :diagnostics :conformance-report]})
```

## Rejection Rules

- Reject stage advancement without required conformance suite pass.
- Reject stage artifacts missing provenance.
- Reject unexplained stage output divergence.
- Reject self-hosted compiler diagnostics that lose source spans.
- Reject unreproducible compiler releases when policy requires reproducibility.
- Reject compiler unsafe code without audit metadata.
- Reject claims of reduced trusted computing base without a TCB delta report.

## Diagnostics

- `TEST13001` reports missing stage conformance.
- `TEST13002` reports bootstrap provenance gap.
- `TEST13003` reports unexplained stage divergence.
- `TEST13004` reports diagnostic regression.
- `TEST13005` reports unreproducible compiler artifact.
- `TEST13006` reports compiler unsafe audit gap.
- `TEST13007` reports missing TCB delta.

## Conformance Criteria

- Each bootstrap stage emits manifests, artifacts, provenance, and conformance reports.
- Stage compilers pass the suites required for their implemented subset.
- Rebuild comparisons identify accepted and rejected differences.
- Self-hosted compiler diagnostics retain source spans and stable codes.
- Reproducible or diverse-compiler checks support release trust claims.
- Unsafe compiler internals have audit metadata.
- The trusted computing base delta is explicit at each stage.
