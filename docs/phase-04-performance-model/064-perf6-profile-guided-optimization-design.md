# PERF6 - Profile-Guided Optimization Design

Sequence: 64
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Profile-guided optimization uses measured execution data to guide inlining,
layout, branch ordering, specialization, code placement, cache behavior, and
variant selection. Gravity accepts PGO only when profile data identity,
staleness, privacy, reproducibility, and safety preservation are explicit.

This document defines PGO data schemas, decision logs, staleness policy, hot/cold
maps, privacy handling, and validation requirements.

## Requirements

- Profile data must be keyed by source hash, typed artifact hash, MIR hash,
  compiler version, profile, target, provider versions, and workload identity.
- PGO decisions must preserve type, effect, capability, profile, safety, taint,
  numeric, and unsafe-audit evidence.
- Builds must record profile-data status: accepted, advisory, stale, rejected, or
  required-missing.
- PGO data containing user or production inputs must be redacted, summarized, or
  governed by data policy.
- PGO decisions must be reproducible from profile data and compiler version.
- Stale profile data must not silently drive release optimizations.

## Dependencies

- `PERF1` defines optimization evidence.
- `PERF5` defines benchmark and workload governance.
- `L12` defines build artifact inputs.
- `SAFE11` defines taint and privacy for production traces.
- `SAFE15` defines proof preservation.
- Compiler phases define MIR hashes and pass decision logs.

## Outputs and Artifacts

- Profile-data schema.
- Profile-data identity record.
- Hot/cold path map.
- PGO decision log.
- Staleness report.
- Privacy and redaction report.
- Reproducibility record.
- PGO diagnostics and conformance results.

## Profile Data Identity

```clojure
{:profile-data/id :checkout-prod-like
 :source-hash "..."
 :typed-core-hash "..."
 :mir-hash "..."
 :compiler "gravityc 0.1"
 :profile :native
 :target :llvm-x86_64
 :workload :checkout
 :provider-versions {:allocator "arena-1" :runtime "native-1"}
 :collected-at "2026-06-22T00:00:00Z"
 :privacy :aggregated}
```

The optimizer rejects required PGO data whose identity does not match policy.

## Data Kinds

PGO data may include:

- Function counts.
- Branch probabilities.
- Hot paths.
- Cold paths.
- Allocation frequencies.
- Type distributions.
- Protocol target distributions.
- Value shape distributions.
- Cache miss summaries.
- Lock contention summaries.
- Error path frequency.
- Tool or model call frequency when relevant.

Raw sensitive values should not be stored. Aggregated or redacted summaries are
preferred.

## Decisions

PGO may guide:

- Inlining.
- Hot/cold splitting.
- Block ordering.
- Layout selection.
- Specialization.
- Variant selection.
- Branch prediction hints.
- Dispatch table layout.
- Allocation strategy.
- Error path outlining.

Each decision records profile data used, pass id, expected benefit, preserved
facts, invalidated proofs, and emitted artifacts.

## Staleness Policy

Staleness modes:

- `:reject-if-source-differs`
- `:reject-if-mir-hash-differs`
- `:advisory-if-similar`
- `:require-exact`
- `:ignore`

Release builds default to rejecting mismatched source or MIR hashes unless a
policy explicitly accepts advisory data.

## Privacy

PGO traces from production may contain tainted or secret-derived information.
The profile-data artifact records redaction, aggregation, retention, and access
policy. Trace data must not leak user input, secrets, prompts, tool outputs, or
customer data into public build artifacts.

## Diagnostics

PGO diagnostics use `PERF6` identifiers:

- `PERF6-DATA-MISSING` for required profile data not supplied.
- `PERF6-STALE` for profile data rejected by staleness policy.
- `PERF6-IDENTITY` for missing source, MIR, compiler, target, or workload
  identity.
- `PERF6-PRIVACY` for trace data violating taint, secret, or retention policy.
- `PERF6-DECISION` for PGO decisions without decision-log records.
- `PERF6-SAFETY` for transformed code losing safety evidence.
- `PERF6-REPRO` for decisions that cannot be reproduced from inputs.
- `PERF6-WORKLOAD` for workload mismatch.

Diagnostics must include profile-data id, policy, source/MIR hash, workload,
pass id, profile, target, and missing or stale field.

## Rejected Designs

Gravity rejects anonymous PGO blobs.

Gravity rejects stale profile data silently driving release builds.

Gravity rejects PGO traces that leak secrets or user data into public artifacts.

Gravity rejects PGO decisions without pass decision logs.

Gravity rejects PGO as justification for losing safety evidence.

## Conformance Criteria

A conforming PGO implementation must demonstrate:

- Profile-data identity and staleness checks.
- Accepted, advisory, stale, rejected, and missing-required statuses.
- Hot/cold maps and decision logs.
- Safety and proof preservation after PGO.
- Privacy/redaction handling for tainted trace data.
- Reproducible decisions from profile data.
- Workload mismatch diagnostics.

