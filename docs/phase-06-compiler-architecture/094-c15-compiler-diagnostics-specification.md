# C15 - Compiler Diagnostics Specification

Sequence: 94
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Compiler diagnostics are structured artifacts. They are rendered as terminal
messages, IDE messages, CI annotations, safety reports, package reports, and
conformance fixtures, but the authoritative representation is machine-readable
data linked to source, generated origins, compiler artifacts, profile, target,
facts, and remediation.

Diagnostics are part of the compiler contract. A failure that cannot explain
the violated rule and responsible artifact is not acceptable for a language
whose compiler output is auditable data.

## Requirements

- Every diagnostic must include stable rule id, severity, primary location,
  related locations, origin chain, compiler stage, profile, target, involved
  artifacts, relevant facts, and remediation.
- Diagnostics for generated code must identify both generated form and source
  producer unless package privacy policy redacts producer internals.
- Diagnostics must be deterministic for the same source, profile, target,
  compiler version, dependencies, and build grants.
- Diagnostic ids must be stable across wording changes.
- Text rendering must be a view over structured diagnostic data.
- Diagnostics must support golden JSON fixtures, IDE ranges, CI annotations,
  quick fixes, explain pages, and localization.
- Secret values must not appear in public diagnostics.

## Dependencies

- `C2` and `C3` define source spans, syntax ids, and origin chains.
- `C4` defines macro expansion traces.
- `C5` through `C14` produce compiler diagnostics.
- Phase 13 tooling consumes diagnostic streams.
- Phase 14 testing consumes diagnostic golden fixtures.
- Package, governance, and security documents define redaction and publication
  policy.

## Outputs and Artifacts

- Diagnostic schema.
- Diagnostic stream.
- Diagnostic catalog.
- Related-span map.
- Remediation and quick-fix records.
- Redaction report.
- IDE/CLI rendering records.
- Golden diagnostic fixtures.

## Diagnostic Shape

```clojure
{:artifact :gravity/diagnostic
 :diagnostic-id diagnostic-hash
 :rule :C8-CAPABILITY
 :severity :error
 :stage :effect-check
 :message-key :effect.missing-capability
 :primary {:span source-span
           :syntax-id syntax-id
           :artifact core-node-id}
 :related [{:role :required-capability
            :span manifest-span
            :artifact capability-proof-id}
           {:role :generated-by
            :span macro-call-span
            :artifact expansion-step-id}]
 :origin-chain origin-chain
 :profile :kernel
 :target :llvm
 :facts {:effect :network/http
         :capability :http/client
         :denied-by :profile}
 :remediation [{:kind :remove-effect}
               {:kind :move-behind-facade}
               {:kind :change-profile :to :hosted}]
 :redactions []}
```

The diagnostic id is stable over rule, primary artifact, stage, and semantic
facts. It is not stable over display wording.

## Severity and Lifecycle

Severity values:

- `:error`: compilation or artifact emission cannot proceed.
- `:warning`: compilation can proceed, but behavior is risky, deprecated, or
  policy-sensitive.
- `:info`: explanatory note tied to an artifact.
- `:hint`: optional improvement or quick-fix opportunity.
- `:internal-error`: compiler invariant failure; should include bug-report
  artifact context.

Diagnostics also carry lifecycle:

- `:active`,
- `:suppressed-by-policy`,
- `:redacted`,
- `:resolved-by-fix`,
- `:stale-after-edit`.

Suppression never removes the diagnostic from internal audit artifacts.

## Locations and Origins

Locations include:

- source span,
- generated span,
- macro definition span,
- macro call-site span,
- manifest entry,
- package dependency edge,
- MIR operation,
- domain IR node,
- backend artifact,
- runtime manifest entry.

Origin chains allow tools to explain how a user form became generated code or
target artifact. Diagnostics should prefer the most actionable source span as
primary and include generated or downstream artifacts as related entries.

## Facts and Remediation

Facts are typed fields, not prose blobs. Common fact families include:

- expected and actual types,
- inferred and declared effects,
- missing capability,
- active profile and denied policy layer,
- ownership, lifetime, and region path,
- safety outcome,
- proof or certificate id,
- target feature,
- provider selection,
- artifact id.

Remediation entries are structured categories. Renderers may localize or expand
them, but tests assert category and target.

## Redaction

Diagnostics may mention secret names, private package internals, proprietary
macro expansions, or security-sensitive target details. Redaction policy must:

- remove secret values,
- preserve enough structure to fix the issue,
- record that redaction occurred,
- keep full diagnostics only in authorized private artifact stores.

Public diagnostics must never include secret values or raw model/tool outputs
classified as private.

## Diagnostic Streams

Compiler stages emit ordered diagnostic streams. A stream record includes:

- stage,
- input artifact,
- output artifact if any,
- diagnostics,
- summary counts by severity,
- deterministic ordering key,
- redaction policy,
- rendering version.

Streams are attached to pipeline manifests and target artifact manifests.

## Golden Fixtures

Golden diagnostic tests assert:

- rule id,
- severity,
- primary span,
- related span roles,
- stage,
- profile and target,
- fact keys and values,
- remediation categories,
- redaction markers,
- stable ordering.

Golden tests should not fail merely because human wording improves.

## Diagnostics

Diagnostic-system diagnostics use `C15` identifiers:

- `C15-SCHEMA` for malformed diagnostic records.
- `C15-ID` for unstable or duplicate diagnostic ids.
- `C15-SPAN` for missing or invalid primary location.
- `C15-ORIGIN` for missing generated-origin chain.
- `C15-FACTS` for unstructured or missing fact payloads.
- `C15-REMEDIATION` for missing remediation on actionable diagnostics.
- `C15-REDACTION` for secret or private data leaked in diagnostics.
- `C15-ORDER` for nondeterministic stream ordering.
- `C15-GOLDEN` for diagnostic fixture mismatch.

Diagnostics about diagnostics must include stage, offending diagnostic id, schema
field, artifact id, and remediation.

## Rejected Designs

Gravity rejects diagnostics as plain strings.

Gravity rejects generated-code diagnostics that hide the source producer.

Gravity rejects diagnostic ids tied to wording.

Gravity rejects diagnostics that leak secret values.

Gravity rejects golden tests that depend only on localized prose.

## Conformance Criteria

A conforming diagnostic system must demonstrate:

- structured diagnostics from reader through target lowering,
- generated-code primary and related spans,
- stable ids across wording changes,
- CLI, IDE, and CI rendering from the same schema,
- remediation records and quick-fix categories,
- redaction of secret values,
- deterministic stream ordering,
- golden JSON fixtures for representative failures,
- internal-error diagnostics for compiler invariant failures.
