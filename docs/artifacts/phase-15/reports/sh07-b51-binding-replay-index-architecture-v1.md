# SH-07 B51 Binding-Replay Indexed Lookup Architecture V1

Status: draft architecture candidate; no implementation authority

Date: 2026-09-07

## Purpose and narrow decision

The integrated Attempt-32 carrier architecture and integrated first-eval
nested-validation V3 architecture admit authentic Root 1, Root 6, and selected
Root 8 execution without transferring host semantic authority into Gravity.
The V3 implementation checkpoint demonstrates one-shot receipt consumption,
but authentic Root-8-to-Root-1 and declared-external-binding execution still
exhaust the unchanged 8,388,608-transition machine bound during retained
Gravity replay.

Bounded localization identifies one redundant operation. In
`sh07-b51-derived-no-form-failing-binding-ids`, producer B scans the complete
request `:forms` vector once for every binding to recover the multiplicity of
`:definition-syntax-id`. The same replay invocation has already constructed a
closed Gravity index from that exact immutable request and has already rejected
duplicate primary and secondary form and binding keys.

This decision permits exactly that one scan to be replaced by the existing
Gravity function `sh07-b51-find-form-by-syntax` over the locally constructed
`indexes/:forms/:by-secondary` map. Presence becomes multiplicity one and
absence becomes multiplicity zero only after the existing duplicate guards and
binding-index guards succeed. No other lookup, replay, authorizer, Root-6
function, runtime receipt seam, limit, result, diagnostic, or authority boundary
may change.

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-binding-replay-index-architecture-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-binding-replay-index-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-binding-replay-index-architecture-v1.md",
  "status": "draft",
  "base_commit": "0a391faa3ea0cc299e60b476df0c3718d4b5af8c",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-first-eval-nested-validation-architecture-v3",
      "required_state": "integrated"
    }
  ],
  "historical_references": [],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```

## Governing contracts and dependencies

This decision is governed by `AGENTS.md`, D0, D1, D2, D3, D6, D8, D9, L2,
C2, C6, C11, BOOT1, BOOT3, BOOT6, BOOT7, BOOT8, TEST10, TEST11, TEST13,
`docs/self-hosting-slice-backlog.md`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`, and the SH-07 proof contract.

The two authority dependencies remain unchanged:

- Attempt 32 owns the exact carrier, both independent certificates, physical
  census, M0/M1/seven-M2/M3-v2/S30 joins, semantic root, eight public roots,
  1,480-edge external-link census, admission order, and compositional bounds.
- V3 owns first-counted-eval receipt minting, Root-1 and Root-6 role-specific
  facts, caller-blind selected-Root-8 child entry, object-identity binding,
  one-shot consumption, cleanup, and unchanged per-machine metering.

Neither dependency grants implementation acceptance. This decision acts after
the V3 nested-proof cutpoint, inside retained Gravity record-shape replay. It
adds no receipt fact and accepts no host-computed semantic result.

## Exact source cutpoint

The only permitted semantic source edit is in:

```text
bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity
function sh07-b51-derived-no-form-failing-binding-ids
```

The reference loop performs:

```clojure
(sh07-b51-find-by
 (get request :forms)
 :syntax-id
 (get binding :definition-syntax-id))
```

The successor may instead perform:

```clojure
(sh07-b51-find-form-by-syntax
 indexes
 (get binding :definition-syntax-id))
```

and derive `declaration-count` as one when the returned value is a map and zero
when it is nil. No other multiplicity is representable after the mandatory
duplicate guards. The lookup stays before
`sh07-b51-v16-binding-record-base-shape?`, preserving evaluation and rejection
order. That shape predicate, including all four SHA-256 identifier checks and
lowercase-hex validation, remains byte-for-byte unchanged.

No other occurrence of `sh07-b51-find-by`, `sh07-b51-index-request`,
`sh07-b51-index-records`, `sh07-b51-find-form-by-syntax`, record-shape replay,
external-binding authorization, Root-6 replay, or the V3 runtime and receipt
implementation is authorized to change under this decision.

## Index provenance and closed schema

The index is ephemeral Gravity data built by `sh07-b51-index-request` from the
exact immutable authenticated request in the same replay invocation. Its closed
shape is:

```text
{:forms
 {:by-primary {form-id form-record}
  :by-secondary {syntax-id form-record}
  :duplicate-primary? boolean
  :duplicate-secondary? boolean}
 :bindings
 {:by-primary {binding-id binding-record}
  :by-secondary {definition-syntax-id binding-record}
  :duplicate-primary? boolean
  :duplicate-secondary? boolean}
 :resolutions
 {:by-primary {reference-syntax-id resolution-record}
  :by-secondary {}
  :duplicate-primary? boolean
  :duplicate-secondary? false}
 :aliases
 {:by-primary {alias alias-record}
  :by-secondary {}
  :duplicate-primary? boolean
  :duplicate-secondary? false}}
```

Every current replay caller constructs `indexes` locally from its exact
`request`: producer B `sh07-b51-no-form-outcome-authenticated?`, producer C
`sh07-b51-v16-post-waiver-terminal-replay`, producer C
`sh07-b51-no-form-accepted-outcome-for-carrier?`, and producer C
`sh07-b51-producer-outcome-kind`.

A caller-supplied, serialized, cached, stale, cloned, host-built, cross-request,
or `request/:internal-indexes` value has no authority. Direct internal function
invocation with a forged index is not a public admission path. Any source,
function, plan, route, manifest, or carrier mutation that changes index
construction must fail existing authenticated admission before replay.

## Duplicate and missing laws

Before the changed loop, the existing code must retain these checks and
diagnostics:

1. duplicate form primary or secondary key rejects with
   `:form-index-duplicate`;
2. duplicate binding primary or secondary key rejects with
   `:binding-index-duplicate`.

The map's last-write behavior must never convert a duplicate into authority.
Only after both guards accept may an indexed form value mean count one and nil
mean count zero.

After the unchanged binding-shape predicate:

- `:core` and `:import` bindings continue without requiring a local form;
- one matching form continues for every binding class;
- zero matching forms for `:namespace` append that binding id exactly once in
  binding-table order;
- zero matching forms for any other class reject with
  `:non-waivable-missing-binding-form` and `:partial-products []`.

The indexed lookup does not authenticate a no-form namespace binding. All
subsequent laws remain mandatory: unique resolution selection, exact upstream
and declaration agreement, allowed current or fully-qualified namespace
resolution order, external membership exactly `[0 1]`, and terminal result
`:authenticated-external`. Missing or malformed declarations, local
membership, zero or multiple resolutions, disagreement, unauthorized order,
or an unauthorized cross-fragment edge still reject through their existing
diagnostic and atomic output boundary.

The independent Root-6 replay remains separately authored and may not consume
this producer index or its presence? result as a witness.

## Authenticated edge and identity rebuild

The changed source occurrence replaces one producer-B-to-producer-A external
call edge:

```text
reference callee  sh07-b51-find-by
reference arity   3
successor callee  sh07-b51-find-form-by-syntax
successor arity   2
owner             :producer-internal
dynamic calls     exactly one per binding, in binding-table order
```

The successor must rebuild producer B's source and semantic identities, the
affected emitted plan/function/callsite identities, the exact external edge
row, route and manifest products, M0, M1, all affected M2 products, M3-v2,
S30, semantic root, both certificates, physical census, carrier, host pins,
checker pins, and evidence identities. The static external-link census remains
exactly 1,480 rows: one old row is substituted by one new row, not added or
removed.

Every other static edge row and every unaffected runtime trace row must be
reference-equivalent. Dynamic traces may differ only at occurrences of this
one rebuilt row, retaining caller, owner, binding-table order, call cardinality,
and relative position while changing the authenticated callee, arity, callsite,
source, function, and plan identities required by the rebuilt code.

Unauthorized caller, callee, owner, arity, callsite, source hash, function hash,
plan, route, manifest, duplicated row, or removed row rejects through the
existing external-link diagnostic with no committed result or output.

## Fixed metering and resource laws

All existing limits remain exact:

```text
machine transitions       8,388,608 per external machine
live frames                4,096
authenticated calls        65,536
linked external edges      1,480
```

The successor may remove only transitions attributable to the eliminated
per-binding full-form scan. Index construction remains real counted Gravity
work. No limit increase, reset, refund, negative fuel, hidden transition,
fused evaluator iteration, independent-machine aggregation, host answer, or
metering reclassification is permitted.

Boundary evidence must prove the final legal recurrence from 8,388,607 to
8,388,608, rejection before work at the next guarded iteration, and rejection
of work requiring transition 8,388,609. It must also prove 4,096 frames and
65,536 calls accept while 4,097 frames and call 65,537 reject.

Authentic direct Root 1, direct Root 6, and selected Root 8 calling the exact
public Root 1 must complete under those same limits and publish exact headroom.
Selected Root 8 must retain its general raw validator, caller-blind V3 child
mint, exactly one child receipt consumption, and zero receipt-created
recurrences.

## Required evidence bundle

A conforming implementation successor must freeze one clean immutable source
snapshot and publish:

1. exact base, candidate, tree, owned paths, source hashes, function and plan
   identities, route, manifest, carrier and certificate identities;
2. independent reference-scan and successor-index checkers with disjoint
   executable closures and complete closure manifests;
3. accepted scan/index parity for one-form, no-form core, no-form import,
   authenticated no-form namespace, and ordered multiple namespace bindings;
4. rejection parity for missing nonwaivable lexical/other bindings, duplicate
   form id, duplicate form syntax id, duplicate binding id, and duplicate
   binding definition syntax id;
5. rejection of stale, missing, replaced, or cross-request secondary indexes
   and false duplicate flags through public source/plan/provenance admission;
6. no-form namespace mutations covering missing/multiple resolution, local
   membership, unauthorized order, declaration/upstream disagreement, and
   malformed declaration;
7. the complete unauthorized-edge mutation family described above;
8. authentic Root-1, Root-6, and Root-8 accepted, pending, malformed,
   exact-limit, and first-plus-one execution, including declared external and
   unauthorized cross-fragment fixtures;
9. V3 copied identity, replay, cross-thread, cross-invocation, losing-CAS,
   fatal cleanup, compatibility, physical `{}`, and first-eval mutations; and
10. atomic publication receipts that bind every carrier, certificate,
    transcript, checker identity, command, result, and output artifact and
    reload-validate them from the final clean snapshot.

For reference-completing fixtures, status, result, diagnostic, partial
products, output, and trace modulo the one edge substitution must be exact. For
the previously exhausting authentic paths, evidence must bind the identical
observable reference prefix, independently verify the complete successor
result, prove every retained predicate executed, and report fixed-limit
headroom.

## D6 and D9 measurement obligations

The evidence must record semantic transitions by `:eval` and `:value`, source,
function, and phase; binding iterations; form records visited; index builds;
lookup calls; binding-shape, SHA-256, and lowercase-hex calls; maximum frames;
authenticated calls; and all V3 pending, mint, scope, receipt lookup,
consumption, cleanup, and rejection counters.

It must also bind the command, toolchain/JVM, wall time, CPU time, allocation or
peak RSS, cold/warm classification, repetition count, and variance. Removed
work must be attributed only to the old full-form scan. A result that completes
only by omitting another predicate, changing a limit, reusing host facts,
changing machine partitioning, or using cached authority rejects.

The historical 250,000- and 500,000-transition probes are localization only.
They demonstrate repeated scan work and one V3 receipt consumption but confer
no correctness, bound, benchmark, acceptance, or integration authority.

## Independent acceptance criteria

Independent exact review must reject the successor if any of these are true:

- the lookup occurs before duplicate guards or changes binding-shape order;
- presence is treated as unique without the locally rebuilt duplicate facts;
- any caller- or host-supplied index grants authority;
- core, import, namespace, local, missing, duplicate, or external-binding laws
  change;
- owning public-root selection or unauthorized cross-fragment rejection
  changes;
- any source cutpoint beyond the single producer-B occurrence changes;
- Root 6 consumes producer evidence or the V3 receipt projection expands;
- the edge census differs from 1,480 or any unexplained trace row changes;
- fuel, frame, or call bounds change or work is hidden, refunded, or reset;
- checker independence, closure completeness, final-snapshot atomicity, or
  reload validation is missing; or
- evidence claims aggregate SH-07 completion, performance, self-hosting,
  release, or seed retirement.

Acceptance of this architecture authorizes only a separately governed
implementation candidate after reconciliation with then-current authoritative
main. It does not accept implementation bytes, test results, certificates,
materializer tooling, evidence, integration, or SH-07 completion.

## Nonclaims and SH-07 status

This decision does not claim that the indexed successor completes within the
fixed bound; authentic evidence must prove that. It does not claim a general
map-lookup optimization, cache, compiler performance improvement, broader
core-form coverage, Clojure seed retirement, self-hosting, release readiness,
or aggregate SH-07 completion.

Overall SH-07 remains partial. In the current eight-stage completion sequence,
four stages are complete. This candidate addresses the architecture gate inside
stage five only.
