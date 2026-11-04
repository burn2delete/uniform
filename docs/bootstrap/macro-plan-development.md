# SH-05 macro plan development boundary

## Purpose

This slice separates one Uniform-language compiler component from the
Clojure-powered bootstrap. It builds the current
language/compiler/src/gravity/macro.gravity SH-05 plan with the existing
Clojure stage0 builder, persists the plan as EDN data, reloads it in a fresh
Clojure child, and invokes sh05-expand-macro-template through the existing
stage2 runtime.

The slice is a development component producer. It is not a replacement
compiler, a native backend, or a self-hosting proof.

## Requirements

- The declared macro source is exactly 54,374 bytes with content identity
  sha256:0a6e58a8134c6f9dbd139617fda376cd8978223820562c1f5b214b658f1d8db1.
- The persisted plan must be EDN data, contain the existing compiler-artifact
  plan shape, and expose the exact arity-one request entry shape.
- Existing plan identity computation must agree with the reloaded plan body.
  The insertion-order function-name digest is recorded but is not compared
  after EDN map reload because that order is not a semantic identity.
- The persisted `:plan-id` must be recomputed from the canonical plan body and
  a substituted or tampered ID must be rejected before execution.
- The producer summary, persisted plan bytes, source identity, and child
  execution input are bound before the execution child starts.
- The accepted and rejected fixture bytes are checked against their declared
  SHA-256 identities, decoded once as an explicit lossless UTF-8 snapshot, and
  that same snapshot is passed to C3 before invoking the loaded entry.
- The accepted fixture must retain the exact `(def add-one (fn [x] (+ x 1)))`
  expansion datum.
- The rejected fixture must retain the complete structured C4 diagnostic and
  provenance: macro identity/version, definition and call-site spans, origin
  chain, profile/target, effects/capabilities, hygiene, facts, remediation,
  and the authenticated diagnostic digest preimage. The diagnostic span and
  preimage identities must be bound to a data-only projection observed from
  the trusted fresh C3 request. The record must bind the
  source/compiler/toolchain inputs while they remain stable across producer
  and execution children.
- Outputs are created with CREATE_NEW, read back, and hashed. A failed run
  retains every published output because a portable public-path delete cannot
  be coupled atomically to the ownership observation. The failure diagnostic
  records the retention policy and every output path registered before a child
  starts. Each path is reported as published with its observed file key, size,
  and digest, as a replacement/changed-after-publication with current and
  last-known observations, or explicitly as not published or unavailable;
  existing, raced, and replacement files are never overwritten or deleted by
  cleanup.

## Dependencies

The implementation reuses gravity.macro/compute-plan-identities,
gravity.macro/execute-plan!, gravity.bootstrap/sh05-macro-build-binding!,
gravity.bootstrap/compiler-c3-syntax-source-artifact, the existing C3
development request factories, and the existing Clojure stage2 runtime.
No C3 artifact, Reader authority, operation closure, or native authority is
serialized.

## Outputs and artifacts

The public entry point is:

    (gravity.bootstrap.rebuild.macro-plan/run-development-macro-plan!
      project-root output-root)

It writes:

- target/bootstrap-macro-plan/macro-plan.edn, the persisted data-only plan;
- target/bootstrap-macro-plan/macro-record.edn, the child execution record.

The record contains the accepted SH-05 macro template and its data-only digest
requests, the exact accepted expansion datum, and the rejected C4-RETURN
observation. It records the loaded plan ID/hash, the producer's observed
compiler revision and dirty status, the child seed/toolchain facts, and a
data-only C3 provenance projection used to bind the diagnostic. It does not
serialize a C3 artifact, Reader authority, or operation closure, and does not
claim digest resolution, downstream artifact assembly, or native execution.
The provenance also records the authenticated macro source identity and the
stable-input result checked around both child processes.
The record labels the producer as Clojure stage0 and the fresh consumer as the
generic Clojure stage2 rule runner; both labels are live-worktree development
provenance, not self-hosting authority.
On failure, the thrown diagnostic includes `:failure-output-policy
:retain-published-outputs` and a `:retained-outputs` receipt for every output
registered by that run, including publish-then-fail and ordinary I/O paths.

## Conformance and acceptance

The focused test namespace is
gravity.bootstrap.rebuild.macro-plan-test. It checks plan identity
substitution and canonical plan-ID tampering, exact function-shape rejection,
lossless UTF-8 fixture snapshots, exact accepted expansion output, child
observation/summary substitution, full structured diagnostics and C3-bound
provenance, source/compiler/toolchain drift, output-collision,
publish-then-fail/ordinary-I/O/replacement preservation, EDN readback,
accepted template output, and the rejected C4-RETURN path.

For a direct development run:

    clojure -Srepro -M -e '(require (quote gravity.bootstrap.rebuild.macro-plan)) (prn (gravity.bootstrap.rebuild.macro-plan/run-development-macro-plan! "." "/tmp/uniform-macro-plan-output"))'

This evidence remains Clojure-seed-hosted and development-only. It does not
retire the Clojure bootstrap, authenticate the complete compiler closure,
establish migration or release authority, or prove native/C conformance.
