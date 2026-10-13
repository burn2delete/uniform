# Iteration Cache Catalog Routing v1

Status: bounded tooling candidate; exact admission authority is recorded
separately after freeze. Full-suite acceptance is unclaimed.

## Purpose and Scope

Add a catalog-backed `--route LABEL` selector to the production iteration-cache
runner on main base `39b96cabf540d65111a63f8868a0242768f431bf`, tree
`a534e32211d2973ca95f692571d5b684cc9bbfbe`. The user explicitly selected
"Add catalog-backed --route labels" after investigation showed that the current
runner has no historical fixed route catalog. The earlier HOLD is preserved
in lifecycle history and was resumed to draft after that clarification.

The candidate is `sh07-iteration-catalog-routing-v1` in invariant family
`tooling/sh07-manifest-routing`. One writer uses the managed worktree on
`codex/iteration-catalog-routing`. The saved project's dirty README,
documentation, public documentation, and target outputs are preserved.
PR #9 and its source-layout branch are outside this work.

## Governing Contracts

AGENTS.md, D1, D2, D3, D6, D9, TEST2, BOOT7, BOOT8, workstream governance,
and `sh07-unified-execution-plan-architecture-v1.md` govern this development
routing change. The accepted parallel resolver retirement is an integrated
dependency. Ownership is recorded in the candidate reservation.

## Selection Contract

The canonical verification catalog declares `:iteration-cache-runner` with
`:explicit-catalog-selection` membership, route IDs `sh07-b13-accepted` and
`sh07-conformance-table`, and the `sh07-` stripping policy. The labels are
`b13-accepted` and `conformance-table`. The first selects the current three B13
fragment vars in their existing order; the second selects the current five
conformance vars in their existing order. Independent literal test oracles
pin these selections against production resolution.

This is new additive selection. It does not assert historical runnable
membership, complete replacement, or new authority. The broad
`:replacement-for` inventory remains a separate migration projection.
Parallel membership remains exactly nine runnable routes versus eleven broad
migration claims. The historical unreviewed branch at `da3c7f80` added a
catalog declaration and tests without a runner consumer or accepted lifecycle;
no tuple was cherry-picked or treated as authority.

UEP projects a label into its existing normal fast plan. The transport retains
the full catalog identity, selector vector, resource and cache policies,
execution mode, and non-authoritative ceiling. It is authenticated against the
current catalog before execution. Unknown keys, mechanisms, labels, altered
plans, stale catalog identity, and changed expanded units fail closed.

The iteration parser expands one `--route LABEL` through the existing
`--test-var` parser. `--max-cache-entries` and `--fail-fast` retain their existing
behavior. A route cannot be repeated or mixed with `--namespace` or
`--test-var`; those combinations have explicit duplicate/conflict diagnostics.
Generic namespace and exact-var selection remain available with unchanged
ownership, resolution, duplicate rejection, and ordering.

The unified CLI's mechanism list/check path consumes the same declaration and
uses the iteration runner's namespace-intern ownership rule for resolution.
The parallel resolver path is unchanged. No runner inventory or test-var
resolver was deleted because no duplicate iteration route inventory existed.

## Preserved Behavior and Identity Delta

Route execution uses the existing exact-var batch, bounded process-local cache,
fixtures, per-var telemetry, failure results, fail-fast skipped tails, cache
selection units, resource behavior, and non-authoritative result map. The new
plan transport is checked before entering that path and is not added to its
receipts. Synthetic tests replace only selected test bodies and fixture
metadata, restore them in `finally`, and compare real runner results against
explicit selector calls. They build no compiler proof artifacts.

The declaration legitimately changes the complete UEP catalog content identity.
Exact-base comparison reproduces the original golden receipt hash
`560657c66294ae325a97af63d627844c5496ab47f813081bb2a24ed54674f5f6` and the
candidate hash `c1f543003e1c90b41ca869bd03598bdc4900e618658b937b6581445215fd86d5`.
The owning golden test is updated to that verified candidate identity. No
metadata, label, membership, or policy input is excluded from authentication;
old UEP plans correctly become stale. This accepted-delta proposal concerns
UEP content identities, while iteration result structure and selection/cache
identity remain equal to explicit selection.

## Evidence and Validation

`sh07-iteration-catalog-routing-v1.edn` retains the genuine baseline
selection-only observations and new production route parsing/resolution,
list/check output, source hashes, plan policy, and exact-base identity comparison.
Its route invocations resolve all eight real test vars without executing their
compiler tests. The baseline lacked the new label selector and rejected the
iteration legacy mechanism; those observations remain historical evidence.

Baseline checks passed 24 CLI tests/203 assertions and 36 iteration tests/246
assertions. Four directly selected iteration cases also passed 4/34 for argument
ownership, skipped tails, cross-namespace ordering, and fixture-error telemetry.
A nested synthetic zero-test summary is part of the iteration telemetry test;
it does not mean the enclosing 36-test namespace was skipped.

The new routing gate covers independent membership/selector oracles, real
consumer equality, malformed declarations/labels/selectors, unsupported
namespaces/vars, missing/extra/duplicate/reordered selections, stale identities,
policy ceilings, authority escalation, fixtures, cache reuse, and skipped tails.
The production runner's original 36 tests, unified CLI's 24 tests, and parallel
manifest's eight tests are retained. Scoped UEP checks retain schema, deterministic
plans, golden receipt integrity, broad projections, exact parallel membership,
and stale projection rejection. The final four-namespace gate passed 74 tests
and 720 assertions; the seven scoped UEP vars passed 7 tests and 62 assertions.
Final metadata gates and command results are recorded with exact admission
evidence after freeze.

The source LOC validator reproduces 24 unrelated `LOC003` violations in
unchanged bootstrap source files. No source-layout change, baseline weakening,
cache redesign, warm architecture, compiler semantics, SH-07 materialization,
or broad runner consolidation is included. Changed Clojure files are under the
test source root, outside the owning LOC contract's implementation source root;
the new test file also stays below 250 physical lines.

## Admission and Nonclaims

The user authorized bounded pure/scoped tests and genuine production selection
invocations, PR creation, independent review, and merging admitted work. No full
SH-07 suite, long compiler proof, full Clojure suite, or fresh full-suite
integration lane is run under this task's explicit bounded verification scope.
Scoped successes and the development receipt supply no full-suite conclusion.

Freeze and independent semantic/code review must bind the exact base, candidate
commit, and tree. Lifecycle recording occurs outside the immutable candidate
commit and is published through the guarded terminal transaction after verified
integration; a commit cannot contain its own future Git identity. Reconcile and
refresh affected review if main advances. No self-audit confers admission.

Progress remains 7/30 slices complete, 6 executable, 7 Clojure-boundary,
4 blocked, continuous prefix SH00-SH06, and full-language completion 0/240.
SH-07 proof, safety, performance, replacement, self-hosting, seed retirement,
and release claims remain unchanged. Clojure and the JVM remain loader,
selection, test, cache, process, and tooling boundaries.
