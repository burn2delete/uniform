# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 4

Status: draft; not review-pending; implementation remains stopped

Date: 2026-08-29

## Purpose and disposition of attempts 1, 2, and 3

This is a clean, versioned successor to the integrated B51 v17 architecture
and to the rejected v18 attempt 1. It does not edit, relabel, or extend any
v17 or attempt-1 identity. The attempt-1 report is retained at
`docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18.md`.
Its exact candidate was commit `5e9b14aa9a1bbf6a5e6d6503d38506e728bc78a2`,
tree `afb0a9da7139f121598a202fec1f4aa82871dd43`, based on
`3d7d4b532176841a45db71fa3ca37f7300d2d2c8`. Independent reviewer
`b51_v18_independent_review` rejected that tuple. The rejection is terminal
for that candidate only and grants no implementation or integration authority.

The attempt-2 report is retained at
`docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-2.md`.
Its exact candidate was commit
`26d50d95824a7a478fa58573b366575cd8bc141c`, tree
`1aaf70a7940226c90dc3ef40e2e75ba8b8a427fc`, based on the same authoritative
main commit. Independent reviewer `b51_v18_independent_review` rejected that
exact tuple, and governance commit
`ce2a9b8d5ee8e6a703894e5120dada36dd8ea182` records the terminal rejection,
history, review, and disposition. Attempt 2 is immutable evidence, not an
input whose values may be silently changed.

The attempt-2 reviewer recorded these exact seven blockers:

1. the f473 predecessor closure preimage/filter remained ambiguous;
2. the raw carrier and digest handshake were circular because the carrier
   required derived ids;
3. the v16 verification semantic-id preimage was undefined and the
   observation schema omitted its observation id;
4. template/resolved/output schemas omitted closed module, lineage,
   projection, and nested product schemas;
5. authentic owning top-level root/defn/fragment selection was undefined;
6. authenticated external-binding authorization and unauthorized
   cross-fragment rejection were undefined; and
7. empty-vector width-zero semantics contradicted positive terminal-count
   requirements.

The attempt-3 report is retained at
`docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-3.md`.
Its exact candidate was commit
`f50a0d9aa9407abf108e419d2176ac5f85312652`, tree
`e34d57e603ca62f4a97c0ca0dc395f4f45d192fe`, and report SHA-256
`0f63e986092706e6d0297b0b840f3cfcc81108a0142a16c3b1eb962cb8b17d85`.
Independent reviewer `b51_v18_attempt3_independent_review` rejected that exact
tuple, and governance commit
`5ec786dab8973a29cdc4b1060d54cdf50ec56b17` records its terminal rejection.
The reviewer recorded these exact three blockers verbatim:

```text
the digest tier/subtier contract contradicts product-node Tier 0d and cannot represent declared same-tier dependencies; owning-definition form and syntax ids in slot, extraction, and product-node schemas have no canonical authenticated branch derivation or cross-record equality rule; seven mandatory compile-time resource bounds have no exhaustive legal diagnostic reason, coordinate policy, finalizer behavior, or fixtures.
```

1. "the digest tier/subtier contract contradicts product-node Tier 0d and
   cannot represent declared same-tier dependencies";
2. "owning-definition form and syntax ids in slot, extraction, and
   product-node schemas have no canonical authenticated branch derivation or
   cross-record equality rule"; and
3. "seven mandatory compile-time resource bounds have no exhaustive legal
   diagnostic reason, coordinate policy, finalizer behavior, or fixtures."

Attempt 3 is immutable terminal evidence and grants no implementation,
integration, or SH-07 completion authority. Attempt 4 is this draft
report-only successor. Its governance-history parent is exact commit
`5ec786dab8973a29cdc4b1060d54cdf50ec56b17`; it has not been frozen or
submitted for review. Its semantic implementation base remains M.

The earlier attempt-1 rejection identified six additional exact defects, all
retained as requirements and corrected here:

1. the predecessor outcome id had no own-id-free path-neutral projection and
   mixed physical artifact/report/diagnostic data into semantic identity;
2. the v18 carrier and output artifact kinds and closed schemas were
   incomplete;
3. B47's semantic identity-preimage domain was conflated with its actual core
   artifact and wrapper kinds;
4. legacy-v16 equivalence had no complete key set, mapping, normalization, or
   empty-delta comparison;
5. the whole-file B47 pin was impossible once C adds v18 code to
   `checked_core.gravity`;
6. predecessor authority and outcome ids were circular because all were
   assigned to one Tier-0 level.

Attempt 2 defined an acyclic Tier-0 handshake, exact semantic/physical
projections, a separately measured immutable B47 executable closure and
contract carrier, complete v18 products, and an exact compatibility record.
Attempt 4 retains those decisions and makes the closure preimage, parse-module
filter, raw-carrier boundary, staged digest requests, own-id-free v16
observation, product schemas, owning-fragment algorithm, external-binding
authorization, and empty-vector domains fully closed. No implementation may
start until a future exact attempt-4 candidate is frozen, independently
accepted, and later reconciled to main.

## Governing contracts and inputs

The governing inputs are `AGENTS.md`, D1, D2, D3, D8, D9, L2, L7, L9, C5,
C6, BOOT7, BOOT8, TEST10, TEST11, TEST13,
`docs/self-hosting-slice-backlog.md`, `docs/self-hosting-slice-ownership.edn`,
`docs/workstream-governance.md`, `contracts/workstream-governance.json`, the
integrated C6 authoritative source admission, and the integrated B51 v17
architecture decision. The exact semantic implementation base remains
`M = 3d7d4b532176841a45db71fa3ca37f7300d2d2c8`; the report's governance-history
parent is `5ec786dab8973a29cdc4b1060d54cdf50ec56b17`.

The authenticated SH-06 schema-15 request, its resolution report, top-level
roots, binding/resolution/scope tables, fragment ownership and coverage,
module assembly, macro traces, origins, and lineage are inputs. V18 never
rebuilds, reorders, or host-projects them. Attempt-1 and all v17 WIP are
evidence only; they confer no source or semantic authority.

The implementation decision also governs the existing schema-1 lexical
scope/binding carrier and schema-15 projection in
`bootstrap/clojure/src/gravity/bootstrap.clj` (the current `let`/`loop`
producer and projection branches), plus their dedicated SH-06 vector-leaf
fixtures, tests, and governance records. This is a conformance correction,
not a schema-version bump; no B47 closure or `resolution.gravity` source is an
input to A.

## Why v18 is required

The current B47 v16 producer rejects a positive vector `let` binding at the
outer frontier with `C6-LOWERING-GAP` and reason
`:let-destructuring-deferred`, and rejects a positive vector `loop` binding
with `C6-LOWERING-GAP` and reason `:loop-destructuring-deferred`. Those checks
run before B47 emits an accepted canonical core. Therefore v17's requirement
for an exact accepted v16 artifact for the same positive vector request is
unreachable. A stale artifact, a different request, a host-synthesized
artifact, or a sanitized rejection would be unauthenticated.

V18 admits a closed sum of four actual semantic outcomes derived from two
authenticated mechanical observations of one exact request:

* `:legacy-v16-accepted`: frozen B47 v16 actually accepted the request and
  produced the complete canonical core artifact; or
* `:b51-vector-frontier-rejected`: frozen B47 v16 actually rejected that same
  request at its deterministic outer `let`/`loop` vector frontier; or
* `:legacy-v16-accepted-after-no-form-waiver`: frozen B47 first rejected only
  the exact authenticated no-form namespace shape, then an independent replay
  waiving only that record-shape clause reached an otherwise complete legacy
  acceptance; or
* `:b51-vector-frontier-rejected-after-no-form-waiver`: the same narrow replay
  passed every earlier invariant and stopped at the exact vector frontier.

Either vector-frontier branch authorizes V18 vector lowering. The
accepted-after-waiver branch authorizes only the authenticated namespace
extension and is never described as v16 acceptance. None proves nested
patterns, remainder legality, recur legality, or any other V18 fact; V18
performs a complete independent pass and fails closed.

## Versioned domains and kind separation

Only these new v18 domains are introduced:

```text
:gravity/sh07-b51-raw-adapter-request-v18
:gravity/sh07-b51-predecessor-executable-closure-v18
:gravity/sh07-b51-predecessor-contract-v18
:gravity/sh07-b51-predecessor-outcome-semantic-v18
:gravity/sh07-b51-predecessor-observation-v18
:gravity/sh07-b51-predecessor-observation-preimage-v18
:gravity/sh07-b51-same-request-binding-v18
:gravity/sh07-b51-implementation-semantic-id-preimage-v18
:gravity/sh07-b51-contract-semantic-id-preimage-v18
:gravity/sh07-b51-predecessor-authority-v18
:gravity/sh07-b51-v18-executable-semantic-closure-v18
:gravity/sh07-b51-v18-semantic-closure-hash-v18
:gravity/sh07-b51-v18-executable-closure-carrier-v18
:gravity/sh07-b51-canonical-template-v18
:gravity/sh07-b51-resolved-core-v18
:gravity/sh07-b51-canonical-core-artifact-v18
:gravity/sh07-b51-core-artifact-v18
:gravity/sh07-b51-core-node-v18
:gravity/sh07-b51-core-node-id-preimage-v18
:gravity/sh07-b51-product-node-v18
:gravity/sh07-b51-product-node-id-preimage-v18
:gravity/sh07-b51-binding-slot-v18
:gravity/sh07-b51-binding-slot-id-preimage-v18
:gravity/sh07-b51-binding-extraction-v18
:gravity/sh07-b51-binding-extraction-id-preimage-v18
:gravity/sh07-b51-slot-extraction-transcript-v18
:gravity/sh07-b51-slot-extraction-transcript-id-preimage-v18
:gravity/sh07-b51-core-identity-v18
:gravity/sh07-b51-provenance-binding-v18
:gravity/sh07-b51-independent-verifier-binding-v18
:gravity/sh07-b51-final-artifact-binding-v18
:gravity/sh07-b51-core-identity-preimage-v18
:gravity/sh07-b51-provenance-binding-preimage-v18
:gravity/sh07-b51-independent-verifier-binding-preimage-v18
:gravity/sh07-b51-final-artifact-binding-preimage-v18
:gravity/sh07-b51-legacy-v16-equivalence-v18
:gravity/sh07-b51-c6-diagnostic-v18
:gravity/sh07-b51-c6-diagnostic-id-preimage-v18
:gravity/sh07-b51-rejected-result-v18
:gravity/sh07-b51-rejected-envelope-v18
:gravity/sh07-b51-v16-verification-semantic-v18
:gravity/sh07-b51-runtime-check-v18
:gravity/sh07-b51-runtime-check-id-preimage-v18
:gravity/sh07-b51-runtime-pattern-failure-v18
:gravity/sh07-b51-runtime-check-result-v18
:gravity/sh07-b51-entrypoint-result-v18
:gravity/sh07-b51-entrypoint-abi-v18
:gravity/sh07-b51-template-verification-v18
:gravity/sh07-b51-resolved-verification-v18
:gravity/sh07-b51-publication-event-v18
:gravity/sh07-b51-publication-event-id-preimage-v18
:gravity/sh07-b51-recur-slot-mapping-v18
:gravity/sh07-b51-recur-slot-mapping-id-preimage-v18
```

The following existing B47 names have distinct roles and must never be
interchanged:

```text
:gravity/sh07-b47-canonical-core-v16
  semantic identity-preimage domain only
:gravity/sh07-canonical-core-artifact
  actual canonical core artifact kind emitted by B47
:gravity/sh07-core-artifact
  B47 outer-wrapper kind in routes that construct one; this decision's
  genuine structural predecessor route never constructs it
:gravity/sh07-to-c6-core-products-v16
  B47 adapter contract
```

A legacy-v16-accepted predecessor records the actual core kind and the literal nil
structural predecessor wrapper separately, and records
`:gravity/sh07-b47-canonical-core-v16` only as the semantic identity-preimage
domain. The v18 output kind is the literal
`:gravity/sh07-b51-canonical-core-artifact-v18`; its optional outer wrapper is
the literal `:gravity/sh07-b51-core-artifact-v18`. No v16 or v17 alias is a
v18 output.

## Exact authenticated raw carrier

The adapter request is a raw-only closed map. It carries authenticated inputs
and opaque physical observations; every semantic id, outcome projection,
authority record, slot, extraction, expected map, and digest preimage is
emitted later by Gravity and is forbidden as a carrier input.

```text
{:artifact :gravity/sh07-b51-raw-adapter-request-v18
 :schema-version 18
 :authenticated-sh06-request schema15-request
 :authenticated-sh06-verification-report fresh-schema15-report
 :sh06-resolution-artifact authenticated-sh06-artifact
 :predecessor-executable-descriptor executable-closure-descriptor
 :v18-executable-descriptor v18-executable-closure-carrier
 :predecessor-contract-carrier predecessor-contract-record
 :predecessor-raw-outcome raw-outcome-sum
 :physical-invocation physical-invocation-record
 :scope :sh07-b51-vector-destructuring}
```

This is an exact eleven-key carrier. The `:predecessor-executable-descriptor`
is the immutable B47 descriptor above; `:v18-executable-descriptor` is the
distinct combined-C physical carrier `P` below. No branch, product, internal digest
reference, or derived semantic id is added to this carrier.

The genuine B47 observation is a closed pair of two mechanical invocations on
the byte-identical schema-15 request. One runner cannot produce both layers:
the direct root preserves the raw template result, while the structural runner
performs the host digest chain and either returns or throws.

```text
{:direct-template-observation
 {:entrypoint sh07-build-core-template
  :request schema15-request
  :returned exact-nine-key-raw-template-result
  :thrown nil}
 :structural-runner-observation
 {:entrypoint sh07-core-run-structural-request-for-test
  :request schema15-request
  :returned exact-six-key-accepted-run | nil
  :thrown nil | exact-host-resolved-sixteen-key-b47-diagnostic-ex-data}}
```

The direct return is always the exact nine-key
`:gravity/sh07-core-template-result` with keys
`[:artifact :schema-version :status :core-template :digest-requests
  :diagnostics :bounds :containment :execution-boundary]`. On acceptance the
structural observation has the exact six-key returned map from M lines
155936-155942 and nil throw. On rejection it has nil return and the exact
host-resolved sixteen-key B47 diagnostic ex-data. The verifier requires both
requests byte-equal; direct status equals the structural branch; direct digest
requests equal the requests mechanically resolved by the structural runner;
the accepted canonical artifact and verification maps equal the direct
template's resolution chain; and the rejected thrown diagnostic equals the
direct first diagnostic after only the declared diagnostic-id request is
resolved. Any cross-run mismatch, extra invocation, or host-authored status is
`C6-VERIFY`. The raw nine-key result and structural return/throw remain nested
inside the one `:predecessor-raw-outcome` carrier key, so the outer carrier is
still exactly eleven keys.

The physical invocation record is:

```text
{:source-path string
 :source-byte-count nonnegative-integer
 :source-content-hash digest-id
 :session-id opaque-physical-id
 :attempt-id opaque-physical-id
 :invocation-id opaque-physical-id}
```

`:invocation-id` identifies the one compound observation transaction, not one
of its calls. Within it the direct build observation is step 0 and the
structural runner observation is step 1; their literal entrypoint fields and
ordered placement in the closed raw outcome authenticate that order without
adding carrier keys or semantic ids.

The host may read and transport these values byte-for-byte, but may not select
a fixture, create or rewrite a request, add vector leaves, classify a result,
fabricate a v16 artifact, alter a diagnostic, or manufacture any semantic id.
It resolves only the exact digest requests emitted by Gravity. The complete
raw return and physical facts are retained in the Tier-4 observation below;
the semantic outcome is computed independently from the authenticated request
and that actual return.

## Immutable predecessor executable and contract carriers

### Executable closure

The B47 whole-file pin used by attempt 1 is intentionally removed. On M, the
three retained B47 entrypoints are compiled into the exact stage-2 plan and
form the only roots:

```text
['sh07-build-core-template
 'sh07-verify-core-template
 'sh07-verify-core-resolved]
```

The immutable executable closure record is:

```text
{:domain :gravity/sh07-b51-predecessor-executable-closure-v18
 :schema-version 18
 :base-commit "3d7d4b532176841a45db71fa3ca37f7300d2d2c8"
 :source-relative-path
 "bootstrap/gravity/src/gravity/checked_core.gravity"
 :roots ['sh07-build-core-template
          'sh07-verify-core-template
          'sh07-verify-core-resolved]
 :root-symbols-hash
 "sha256:d92f6a8991c28e6514e2df9f7b4ca6aeb0baccf9f75cdce47779fd0337c9667b"
 :plan-function-count 305
 :function-member-count 298
 :function-names-hash
 "sha256:aba0868e849b735529ded667ba6aadda30451c8af67dd9686abfdb2d7a97b1fc"
 :function-shapes-hash
 "sha256:79509cc6a17101d22220b3f60278d77547612ab49f6b57d719d463f5cefa5500"
 :selected-functions-hash
 "sha256:34bacc392d81d22757dd7d8f75d82a4ff9ddc55dbe8ececc79daa294496283b1"
 :edge-count 849
 :edge-hash
 "sha256:5f7a87d2a3fc9b43469c5a46109f7b1d8edd1860631d20508d6222a2c2f465ef"
 :roots-members-functions-descriptor-hash
 "sha256:13a9131f49c09fd8c62c365fe68442cfee82cc34cdf73679dcf5c90fe46cdb1c"
 :referenced-builtin-name-count 32
 :referenced-builtin-names-hash
 "sha256:c8769b21181d28b6129f6c4f8e2c42bf0860381d0005f67c63f1ef30e844a28e"
 :contract-form-names-hash
 "sha256:20169a279466cae667cb1e41783b862c9f6dd9fe067574e170282846868f5e4a"
 :contract-form-reader-hashes
 ["sha256:d936967af4da29e0fd7f0193d470c0ca17315a48230dcf3b052e0d9ae35cfd26"
  "sha256:f60ca55802342b02f742eb0a7cc2f9fff233b50f2b64cbbb2340ed56e86135ea"
  "sha256:815a4611e6c950a579d10d4c375b06bbdde5cd3f321987a53ca7c950f4935d8b"]
 :contract-form-reader-vector-hash
 "sha256:79995e16228c5217df02171e5833eff49c5e3f03118566f54b36ea02b5887079"
 :contract-core-hash
 "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
 :contract-bounds-hash
 "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
 :contract-diagnostics-hash
 "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"
 :combined-contracts-hash
 "sha256:f8ac391550f7205c2ad0b56689e9109e71927ff948fa7913da80710aaf51e7ff"
 :filtered-module-header-export-hash
 "sha256:e12f3dde6a0afa798e034b9e60311847535832ec9adffc10e221784c3665c58f"
 :executable-closure-descriptor-id
 "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234"
 :excluded-plan-members
 ['sh07-find-alias
  'sh07-lower-top-level-forms
  'sh07-resolve-match-branch-vector
  'sh07-resolve-match-decision-skeleton-vector
  'sh07-resolve-match-pattern-vector
  'sh07-scope-binding-ids
 'sh07-utf8-byte-width]}
```

The `:executable-closure-descriptor-id` is computed from the exact full
descriptor below. The `:base-commit` and `:source-relative-path` fields in the
carrier are Tier-4 source anchors and are not part of this semantic preimage;
the summary-of-hashes carrier above is evidence for, but is not itself, the
descriptor preimage.

```text
{:domain :gravity/sh07-b47-v16-executable-closure-v1
 :roots ['sh07-build-core-template
         'sh07-verify-core-template
         'sh07-verify-core-resolved]
 :module-contract
 {:module gravity.checked-core
  :profile :meta
  :target :jvm
  :effects #{}
  :capabilities #{}
  :safety :safe
  :exports [sh07-core-contract
            sh07-core-bounds
            sh07-core-diagnostic-catalog
            sh07-build-core-template
            sh07-verify-core-template
            sh07-verify-core-resolved]}
 :semantic-contracts
 {'sh07-core-bounds exact-sh07-core-bounds-value
  'sh07-core-contract exact-sh07-core-contract-value
  'sh07-core-diagnostic-catalog exact-sh07-core-diagnostic-catalog-value}
 :members exact-sorted-298-member-vector
 :functions exact-selected-298-member-plan-function-map}
```

The descriptor preimage is literal and closed. `:module-contract` is exactly
the seven-field map above, with the six exports in that order; its canonical
map hash is
`sha256:e12f3dde6a0afa798e034b9e60311847535832ec9adffc10e221784c3665c58f`.
The unfiltered stage-2 plan's raw `:module` value has only the six compiler
fields (no `:exports`) and canonical map hash
`sha256:5094a1df3a7199f8ddb57853137579c45f727d100e9c65cb6875078215016626`.
Those two hashes must remain distinct: the former is the closure's literal
module contract and the latter is evidence for the raw plan. The
`:semantic-contracts` map is sorted by the three symbol names and contains the
complete raw values of those `def` forms, not their hashes or a summary.
`:members` is the complete sorted 298-symbol vector and `:functions` is the
complete raw selected 298-member plan-function map. No field is removed,
sanitized, or replaced by a host summary in this descriptor. Builtin names,
contract reader forms, and physical source fields are evidence carriers only;
they are deliberately excluded from the f473 preimage. The descriptor is
exactly SHA-256 over the C11 canonical `pr-str` encoding of this full map and
has measured id
`sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234`.

The module-contract filter is deterministic and is run before the closure
hash. Parse the exact module form from M, require one `ns` form whose name is
`gravity.checked-core`, then read its declaration clauses in source order.
Require exactly one `:profile :meta`, `:target :jvm`, `:effects #{}`,
`:capabilities #{}`, `:safety :safe`, and one `:exports` vector. Authenticate
the existing `:metadata` clause as a permitted source/governance clause, but
exclude it from the semantic module-contract map; it is not an executable
module field. Require that the export vector is exactly the six symbols in the
module-contract above, with no duplicate or extra symbol. Emit only the seven
literal keys `:module :profile :target :effects :capabilities :safety :exports`,
preserving the export vector order. Reject a missing, duplicate, reordered, or
malformed semantic clause, or a second unrecognized clause, with `C6-VERIFY`;
do not use a plan's already-normalized module map as a substitute. Hash this
emitted map as
`sha256:e12f3dde6a0afa798e034b9e60311847535832ec9adffc10e221784c3665c58f`.
For comparison, the raw stage-2 plan's `:module` map is the six-field map
without exports and hashes to
`sha256:5094a1df3a7199f8ddb57853137579c45f727d100e9c65cb6875078215016626`.
The raw-plan hash is checked as a plan fact; it is never silently promoted to
the filtered module-contract hash.

The closure algorithm is normative. Build the stage-2 plan from the exact M
source and emitter. Starting with the fixed declaration-order root vector
above, walk each function's instruction tree in canonical map-key and
vector-index order. Follow only runtime-dispatched `:function-call` edges; a
quoted in-module symbol is a literal and is never traversed as a call edge.
The target of each explicit call must resolve to `plan[:functions]`. Reject a
dynamic invocation, an unresolved target, a duplicate definition, or an
unexpected instruction shape. The selected function member is the complete raw
canonical function record (including its literal constants, binding metadata,
and every original field) with no fields removed. Builtin calls are not
executable closure members. Referenced
builtin names are bound by the referenced-builtin-name hash; selected function
records bind call sites and arities. Host builtin implementations and runtime
dispatch remain the trusted residual basis and are not authenticated by this
descriptor.
The selected function map and sorted names/shapes are hashed with the stage-2
canonical map encoder. The closure descriptor is the canonical hash of this
complete record; the member count is 298 of the 305 plan functions. Any
closure member, instruction, constant, builtin name, root, plan shape, or edge
drift is a `C6-VERIFY` failure, even when the three public entrypoint hashes
remain unchanged.
The measured traversal observes 1,290 call sites and 849 unique runtime
caller/callee pairs; the canonical sorted-pair vector hashes to
`sha256:5f7a87d2a3fc9b43469c5a46109f7b1d8edd1860631d20508d6222a2c2f465ef`.

The selected function records retain authenticated call-site and arity facts;
those facts are part of the selected-function-map hash. The source-contract
scanner preserves encountered source order, requires exactly one occurrence of
each of the three required contract forms, and compares the required ordered
name vector and all measured hashes. A reorder, duplicate, omission, or
substitution fails closed before a predecessor root is invoked.

### Source contract carrier

Top-level `def` constants are not represented in the executable plan. Their
source forms are separately authenticated in the same predecessor contract:

```text
{:contract-form-count 3
 :contract-form-names
 ['sh07-core-contract 'sh07-core-bounds 'sh07-core-diagnostic-catalog]
 :contract-form-reader-hashes
 ["sha256:d936967af4da29e0fd7f0193d470c0ca17315a48230dcf3b052e0d9ae35cfd26"
  "sha256:f60ca55802342b02f742eb0a7cc2f9fff233b50f2b64cbbb2340ed56e86135ea"
  "sha256:815a4611e6c950a579d10d4c375b06bbdde5cd3f321987a53ca7c950f4935d8b"]
 :contract-form-reader-vector-hash
 "sha256:79995e16228c5217df02171e5833eff49c5e3f03118566f54b36ea02b5887079"
 :contract-value-hashes-by-name
 {'sh07-core-bounds
  "sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9"
  'sh07-core-contract
  "sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f"
  'sh07-core-diagnostic-catalog
  "sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3"}
 :combined-contracts-hash
 "sha256:f8ac391550f7205c2ad0b56689e9109e71927ff948fa7913da80710aaf51e7ff"}
```

The source reader selects exactly one top-level `def` form for each name and
hashes its canonical reader form. Missing, duplicate, reordered, or changed
forms reject. Physical source path, byte count, and whole-file hash are kept
in the Tier-4 provenance record. The contract carrier also retains the exact
B47 adapter contract fields:

```text
{:domain :gravity/sh07-b51-predecessor-contract-v18
 :schema-version 18
 :adapter-contract :gravity/sh07-to-c6-core-products-v16
 :identity-preimage-domain :gravity/sh07-b47-canonical-core-v16
 :actual-core-artifact-kind :gravity/sh07-canonical-core-artifact
 :actual-wrapper-artifact-kind nil
 :request-artifact :gravity/sh07-authenticated-sh06-core-request
 :request-schema-version 15
 :lowering-rule :sh07-b47-function-call-recursion-products
 :entrypoints ['sh07-build-core-template
               'sh07-verify-core-template
               'sh07-verify-core-resolved]
 :source-contract-forms source-contract-record
 :executable-closure-descriptor-id
 "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234"}

```

The three contract value hashes above are distinct from the source-form
hashes. The source-form names are encountered in source order and must occur
exactly once. Their canonical reader `(def name value)` hashes are the three
`:contract-form-reader-hashes` values and their ordered vector hash is
`:contract-form-reader-vector-hash`; the value hashes are the three
`:contract-value-hashes-by-name` values. Reordering, changing a form, or
changing only its value therefore fails the corresponding closure check.

The whole-file M facts remain visible in physical provenance for replay:
source byte count `444325`, source hash
`sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092`,
plan hash `sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd`,
and full function hash
`sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db`.
Those full-file values are not the v18 predecessor executable identity: C is
allowed to change the whole file by adding disjoint v18 definitions. C must
leave the measured 298-function closure and three source contract forms
byte-for-byte semantically unchanged. A combined v18 source receives new
whole-file pins in H; predecessor authentication uses the closure and contract
records above.

### Combined v18 semantic closure and physical carrier

The C child adds a second, disjoint executable closure. Its own-id-free
semantic value `S` is separate from the physical carrier `P`; neither replaces
or mutates the immutable B47 f473 descriptor:

```text
S :=
{:domain :gravity/sh07-b51-v18-executable-semantic-closure-v18
 :schema-version 18
 :roots ['sh07-b51-build-template
         'sh07-b51-verify-template
         'sh07-b51-resolve-digest-preimage
         'sh07-b51-resolve-template
         'sh07-b51-verify-resolved
         'sh07-b51-build-independent-verifier-binding
         'sh07-b51-build-final-artifact
         'sh07-b51-finalize-rejection]
 :root-symbols-hash digest-id
 :members exact-sorted-v18-member-vector
 :functions exact-selected-v18-plan-function-map
 :runtime-call-edges exact-sorted-v18-runtime-edge-vector
 :referenced-builtins exact-sorted-v18-builtin-vector
 :module-contract
 {:module gravity.checked-core
  :profile :meta
  :target :jvm
  :effects #{}
  :capabilities #{}
  :safety :safe
  :exports [sh07-core-contract
            sh07-core-bounds
            sh07-core-diagnostic-catalog
            sh07-build-core-template
            sh07-verify-core-template
            sh07-verify-core-resolved
            sh07-b51-build-template
            sh07-b51-verify-template
            sh07-b51-resolve-digest-preimage
            sh07-b51-resolve-template
            sh07-b51-verify-resolved
            sh07-b51-build-independent-verifier-binding
            sh07-b51-build-final-artifact
            sh07-b51-finalize-rejection]}
 :semantic-contracts
 {'sh07-core-bounds exact-sh07-core-bounds-value
  'sh07-core-contract exact-sh07-core-contract-value
  'sh07-core-diagnostic-catalog exact-sh07-core-diagnostic-catalog-value}}

hC = sha256(C11-pr-str
     {:domain :gravity/sh07-b51-v18-semantic-closure-hash-v18
      :schema-version 18
      :semantic-closure S})

P :=
{:domain :gravity/sh07-b51-v18-executable-closure-carrier-v18
 :schema-version 18
 :semantic-closure S
 :semantic-closure-hash hC
 :source-relative-path
 "bootstrap/gravity/src/gravity/checked_core.gravity"
 :semantic-base-commit "3d7d4b532176841a45db71fa3ca37f7300d2d2c8"
 :governance-parent-commit "5ec786dab8973a29cdc4b1060d54cdf50ec56b17"
 :physical-census
 {:source-byte-count nonnegative-integer
  :source-content-hash digest-id
  :source-revision-id digest-id
  :whole-file-plan-hash digest-id
  :whole-file-function-hash digest-id
  :plan-function-count nonnegative-integer
  :plan-member-count nonnegative-integer
  :runtime-call-site-count nonnegative-integer
  :runtime-edge-count nonnegative-integer}}
```

The eight roots are fixed in the vector order shown. Their member, function,
edge, builtin, call-site, and whole-file census values are prospective values
that C/H must measure from the exact implementation; this architecture does
not fabricate or predeclare those numbers or hashes. C's closure walk follows
only runtime-dispatched `:function-call` edges, preserves canonical map/vector
order, and binds every reachable raw plan-function record, call edge, builtin,
and retained three-value semantic contract. Quoted symbols remain literals.
The combined module export vector has exactly fourteen symbols: the unchanged
six predecessor exports followed by these eight v18 roots in the same order.
The source module's effects and capabilities are the literal empty sets shown;
schema-15 and v18 output module records instead preserve q's declarations as
vectors (empty vectors only when q declared none). A set/vector substitution
in either location is `C6-VERIFY`.

The raw eleven-key adapter request carries complete `P`. The 0a implementation
preimage binds only `S`, `hC`, and the full inherited f473 semantic descriptor;
it never binds P's source/base/census fields. Tier 4 binds complete P only by
hashing the exact raw carrier. H authenticates P and then separately projects
the first six exports, three old roots, 298 selected B47 functions, and three
contract values to reproduce f473. A missing, extra, reordered, unresolved, or
dynamically dispatched closure member fails before any v18 request.

In the preimage schemas below, `exact-f473-executable-descriptor` means the
literal full descriptor map in the B47 closure section, including complete
members/functions maps. `exact-b47-contract-carrier` and
`exact-predecessor-source-contract-record` mean the literal closed maps in the
source-contract section. These are values, not hashes, summaries, or open
aliases.

## Path-neutral same-request and outcome projections

The same-request projection uses the exact frozen M normalizer
`sh05-path-neutral-semantic-value`, not a host `dissoc`, key sort, or new v18
normalizer. It recursively maps maps, vectors, sets, and seqs. In a map it
omits only `:actual-source-path`, `:workspace-root`, and `:invocation-root`.
After recursively normalizing a map that contains `:byte-start` and either
`:source` or `:file`, it computes a local value named `semantic-source` as the
already-normalized truthy `:file` value, otherwise the already-normalized
`:source` value. It then sets each already-present `:source` and/or `:file` key
to that local value. It preserves those keys and never adds a
`:semantic-source` key. It preserves vector order, set type, seq/list type,
and scalar values. It performs no other deletion, rename, or sort. Closed v18
schemas separately reject unknown keys.

The same-request semantic binding is the sole request-identity 0a record. The
authenticated schema-15 request `q` already carries the canonical projection
binding `p`:

```text
sh07-core-projection-binding-input(q) :=
{:domain :gravity/sh07-authenticated-sh06-core-projection-v15
 :request
 (sh05-path-neutral-semantic-value
  (dissoc q :projection-binding :provenance))}

p = (reader-canonical-hash (sh07-core-projection-binding-input q))
```

Gravity recomputes `p` and rejects any mismatch before emitting the closed 0a
record:

```text
{:domain :gravity/sh07-b51-same-request-binding-v18
 :schema-version 18
 :request-artifact :gravity/sh07-authenticated-sh06-core-request
 :request-schema-version 15
 :request-semantic-id p
 :source-revision-id digest-id
 :top-level-root-semantic-ids vector-of-digest-id}
```

The own-id-free preimage has exactly the keys shown above; the materialized
same-request digest itself is not included. `:request-semantic-id` is the
existing schema-15 `:projection-binding` value `p`, not a fifth 0a request.
Gravity recomputes the exact frozen input and requires
`q.:projection-binding = p` before any v18 digest request is emitted. Thus `p`
transitively authenticates q's roots, forms, bindings,
resolutions, fragments, origins, and module assembly; the small 0a preimage
does not duplicate q or pretend that its root vector is the full request. It
contains no outcome, artifact, fresh verification report, path, session, slot,
extraction, verifier, or final id.

The physical observation is split into an own-id-free Tier-4 preimage and a
materialized record. The preimage is closed and may refer only to already
resolved semantic ids:

```text
{:domain :gravity/sh07-b51-predecessor-observation-preimage-v18
 :schema-version 18
 :semantic-identity-id digest-id
 :raw-adapter-request exact-raw-adapter-request}
```

The materialized Tier-4 observation adds the literal artifact kind and the
resolved `:observation-id`; it does not feed that id back into any semantic
preimage:

```text
{:artifact :gravity/sh07-b51-predecessor-observation-v18
 :schema-version 18
 :observation-id digest-id
 :preimage exact-observation-preimage}
```

The observation is accepted only when it transports the exact joined direct
template and structural-runner observations from the measured closure.
`:raw-adapter-request` is the entire
eleven-key carrier above, byte-for-byte: it includes the authenticated request
and fresh report, both executable descriptors, the contract carrier, the raw
two-invocation observation pair, and the physical invocation record. No summary,
projection, or selected subset may replace it. Paths, bytes, session/attempt
handles, and complete artifact/report/diagnostic payloads therefore occur only
in this Tier-4 observation and never in the Tier-0--3 semantic outcome
identity.

The path-neutral predecessor semantic projection is a closed tagged union.
Its own-id-free preimage is exactly one of the following maps:

```text
;; accepted branch
{:domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
 :schema-version 18
 :outcome-kind :legacy-v16-accepted
 :status :accepted
 :same-request-semantic-id digest-id
 :implementation-semantic-id digest-id
 :contract-semantic-id digest-id
 :identity-preimage-domain :gravity/sh07-b47-canonical-core-v16
 :actual-core-artifact-kind :gravity/sh07-canonical-core-artifact
 :actual-wrapper-artifact-kind nil
 :v16-semantic-artifact-id digest-id
 :v16-verification-semantic-id digest-id
 :authorization :legacy-v16-equivalence}

;; rejected vector-frontier branch
{:domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
 :schema-version 18
 :outcome-kind :b51-vector-frontier-rejected
 :status :rejected
 :same-request-semantic-id digest-id
 :implementation-semantic-id digest-id
 :contract-semantic-id digest-id
 :identity-preimage-domain nil
 :actual-core-artifact-kind nil
 :actual-wrapper-artifact-kind nil
 :frontier-owner :let | :loop
 :frontier-rule "C6-LOWERING-GAP"
 :frontier-reason :let-destructuring-deferred
                     | :loop-destructuring-deferred
 :frontier-binding-kind :vector
 :frontier-form-id digest-id
 :frontier-coordinate {:fragment-id digest-id
                        :form-ordinal nonnegative-integer}
 :semantic-artifact-id nil
 :authorization :v18-vector-lowering}

;; no-form waiver reaches otherwise complete legacy acceptance
{:domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
 :schema-version 18
 :outcome-kind :legacy-v16-accepted-after-no-form-waiver
 :status :accepted
 :same-request-semantic-id digest-id
 :implementation-semantic-id digest-id
 :contract-semantic-id digest-id
 :identity-preimage-domain nil
 :actual-core-artifact-kind nil
 :actual-wrapper-artifact-kind nil
 :frontier-rule "C6-VERIFY"
 :frontier-reason :authoritative-record-shape
 :failing-binding-ids vector-of-digest-id
 :failing-declarations vector-of-no-form-namespace-semantic-projection
 :replay-status :all-other-b47-invariants-passed
 :semantic-artifact-id nil
 :authorization :v18-authenticated-no-form-namespace}

;; no-form waiver reaches the exact vector frontier
{:domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
 :schema-version 18
 :outcome-kind :b51-vector-frontier-rejected-after-no-form-waiver
 :status :rejected
 :same-request-semantic-id digest-id
 :implementation-semantic-id digest-id
 :contract-semantic-id digest-id
 :identity-preimage-domain nil
 :actual-core-artifact-kind nil
 :actual-wrapper-artifact-kind nil
 :waived-rule "C6-VERIFY"
 :waived-reason :authoritative-record-shape
 :failing-binding-ids vector-of-digest-id
 :failing-declarations vector-of-no-form-namespace-semantic-projection
 :replay-status :all-invariants-before-expected-vector-stop-passed
 :frontier-owner :let | :loop
 :frontier-rule "C6-LOWERING-GAP"
 :frontier-reason :let-destructuring-deferred
                     | :loop-destructuring-deferred
 :frontier-binding-kind :vector
 :frontier-form-id digest-id
 :frontier-coordinate {:fragment-id digest-id
                        :form-ordinal nonnegative-integer}
 :semantic-artifact-id nil
 :authorization :v18-no-form-namespace-and-vector-lowering}
```

The accepted projection is derived from the complete freshly verified B47
artifact's exact enumerated 37-key semantic identity preimage and the exact
passed template/resolved verification maps below. The vector-rejected
projection is derived from the actual first diagnostic and authenticated form
at the same outer frontier. The two no-form projections are derived from the
actual first diagnostic, independent replay with exactly one waived predicate,
and the exact authenticated declaration vector. No
physical path, complete artifact, complete report, complete diagnostic map,
session, attempt, or own `:outcome-id` occurs in any outcome preimage. The outcome
semantic id is the canonical digest of this projection plus its literal domain
and schema. The physical observation id is recorded separately at Tier 4.

The actual B47 rejection must be the deterministic first failure for the same
request: `:let-destructuring-deferred` with `:binding-kind :vector`, or
`:loop-destructuring-deferred` whose offending authenticated form is a vector,
under rule `C6-LOWERING-GAP`. Any earlier or different B47 rejection receives
`:authorization :none` and V18 rejects. A frontier rejection never proves a
nested unsupported form, duplicate, unauthorized edge, malformed remainder,
or invalid recur; the full V18 pass must find those facts.

The frontier form id is the exact authenticated offending vector form. Its
`:form-ordinal` is the unique zero-based index of that id in the selected
fragment's authenticated `:form-ids`; a syntax-order surrogate, duplicate
membership, or host-derived ordinal does not authorize the branch.

The no-form branches are authorized only when the transported first B47 failure
is exactly `C6-VERIFY` with reason `:authoritative-record-shape`, the failing
binding-id vector is nonempty and in B47 encounter order, every failing id
joins exactly one SH-06 authenticated `:namespace` explicit-candidate
declaration with no form/fragment, and Q/A/U/B/K agree on every field required
by external authorization. Independent replay replaces only that single
no-form `:authoritative-record-shape` predicate and resumes at the following
B47 check. It must return exactly
`:legacy-v16-accepted-after-no-form-waiver` or
`:b51-vector-frontier-rejected-after-no-form-waiver`; the latter binds the
exact owner, reason, vector kind, form, and coordinate. Every invariant before
the resulting acceptance or expected vector stop must pass. One additional
failure, empty/ambiguous declarations, form-backed evidence, or a second
waiver rejects V18. Neither branch is v16 acceptance or carries a v16
artifact/equivalence id.

For the legacy-v16-accepted branch, the v16 verifier result is projected before its own
id is assigned. The own-id-free projection is closed and path-neutral:

```text
{:domain :gravity/sh07-b51-v16-verification-semantic-v18
 :schema-version 18
 :status :passed
 :template-verification
 {:artifact :gravity/sh07-core-template-verification
  :schema-version 16
  :status :passed
  :rule nil
  :reason nil
  :details {}}
 :resolved-verification
 {:artifact :gravity/sh07-core-resolved-verification
  :schema-version 16
  :status :passed
  :rule nil}
 :resolved-v16-identity-preimage exact-enumerated-37-key-identity-preimage-map}
```

These are the exact passed maps returned by authoritative M: the template
verification has the six literal keys shown (including exact empty
`:details {}`), and resolved verification has four. The template key vector is
exactly
`[:artifact :schema-version :status :rule :reason :details]` (count 6); the
resolved key vector is exactly
`[:artifact :schema-version :status :rule]` (count 4).
The resolved identity
preimage has exactly the enumerated 37 keys from the compatibility section.
No normalized-product/detail/request-sequence alias is admitted. All controlled
references have already been mechanically substituted by their resolved
digests before this preimage is built; an internal reference anywhere is
`C6-VERIFY`. It excludes the
v16 verification's `:artifact-id`, provenance, source paths, source bytes,
session/attempt/invocation handles, complete raw report, diagnostics, and its
own `:verification-semantic-id`. The canonical digest of this map plus its
literal domain/schema is the `:v16-verification-semantic-id` in the accepted
outcome. A rejected predecessor has no v16 verification projection or id.

## Acyclic staged digest handshake

The attempt-1 single Tier-0 level was circular. V18 retains seven major tiers,
divides Tier 0 into four literal subtiers, and divides every tier into literal
ordered batches. Every request has one dense global ordinal and one dense
batch ordinal within its selected batch. Reference legality is decided by the
exact pair `[batch-rank batch-ordinal]` together with the strictly smaller
global ordinal; there is no informal "earlier tier" shortcut and no exception
list.

```text
0a  implementation semantic id, contract semantic id, same-request semantic id,
    and (legacy-v16-accepted branch only) v16-verification semantic id
0b  predecessor outcome semantic id, from 0a plus raw normalized outcome
0c  neutral predecessor authority id, from 0a and 0b
0d  supplemental product-node ids and (rejected branch only) ordinary
    core-node ids, from 0c plus raw authenticated node coordinates
1   slot ids, then extraction ids
2   runtime checks, publications, recur mappings, then transcript
3   path-neutral v18 semantic identity
4   physical provenance binding
5   independent verifier binding
6   final artifact binding
```

The 0a implementation preimage contains the literal closure descriptor, its
roots, measured members/counts/hashes, and semantic contract-form carrier. The
0a contract, request, and accepted v16-verification preimages are own-id-free;
every non-v16 branch has no verification id. The 0b preimage contains only the
normalized union branch and the resolved 0a ids; it contains no physical
observation. The 0c preimage contains resolved 0a/0b ids, branch,
authorization, and nil-versus-non-nil semantic artifact id. The 0d
product/core-node preimages contain resolved authority id and source-semantic
coordinate. Core-node requests precede supplemental product requests within
0d, and postorder child references have smaller request ordinals. No
0a id refers to 0b, 0c, or 0d; no 0b id refers to itself; and no authority id
is used to compute itself. Physical artifact/report/diagnostic facts first
enter Tier 4. This is a closed DAG, not an eighth semantic tier.

The neutral authority record is:

```text
{:domain :gravity/sh07-b51-predecessor-authority-v18
 :schema-version 18
 :implementation-semantic-id digest-id
 :contract-semantic-id digest-id
 :same-request-semantic-id digest-id
 :outcome-semantic-id digest-id
 :outcome-kind :legacy-v16-accepted | :b51-vector-frontier-rejected
               | :legacy-v16-accepted-after-no-form-waiver
               | :b51-vector-frontier-rejected-after-no-form-waiver
 :semantic-artifact-id digest-id | nil
 :authorization :legacy-v16-equivalence | :v18-vector-lowering
                | :v18-authenticated-no-form-namespace
                | :v18-no-form-namespace-and-vector-lowering | :none}
```

Tier, subtier, batch, rank, batch ordinal, and global ordinal exist only in the
nine-key request envelope. They never enter this authority preimage or any
other declared semantic preimage; schedule changes cannot silently change a
semantic id.

Only this neutral authority id is admitted into slot, extraction, product,
template, and semantic identity preimages. Rejection is never called an
artifact. Paths, reports, diagnostic maps, attempts, sessions, and transport
ids are provenance only.

### Ordered digest protocol and host boundary

The v18 pass has one ordered internal digest-request stream and these exact
eight Gravity entrypoints (their names/signatures are part of the v18 schema):

```text
1. sh07-b51-build-template(raw-carrier)
2. sh07-b51-verify-template(raw-carrier, template, digest-requests)
3. sh07-b51-resolve-digest-preimage(request, digest-requests, resolved-prefix)
4. sh07-b51-resolve-template(template, digest-requests, resolved-digests)
5. sh07-b51-verify-resolved(raw-carrier, resolved-core, digest-requests,
                            resolved-digests)
6. sh07-b51-build-independent-verifier-binding(raw-carrier, resolved-core,
                                               predecessor-observation,
                                               provenance-binding,
                                               digest-requests, resolved-digests)
7. sh07-b51-build-final-artifact(raw-carrier, resolved-core,
                                 predecessor-observation, provenance-binding,
                                 verifier-binding, digest-requests,
                                 resolved-digests)
8. sh07-b51-finalize-rejection(pending-rejected-envelope,
                               resolved-diagnostic-id)
```

Every public root returns one exact six-key ABI envelope; no bare map, throw,
nil, or host exception is a valid return:

```text
abi-envelope :=
{:artifact :gravity/sh07-b51-entrypoint-result-v18
 :domain :gravity/sh07-b51-entrypoint-abi-v18
 :schema-version 18
 :status :accepted | :pending-rejection | :boundary-rejected
 :tag exact-row-tag
 :value exact-row-value}

public-abi :=
[{root sh07-b51-build-template
  args [raw-carrier]
  success {:status :accepted :tag :template-built
           :value {:template canonical-template-record
                   :digest-requests exact-ordered-digest-request-vector}}
  failure {:status :pending-rejection :tag :template-rejected
           :value pending-rejected-envelope}}
 {root sh07-b51-verify-template
  args [raw-carrier template digest-requests]
  success {:status :accepted :tag :template-verified
           :value exact-passed-template-verification-v18}
  failure {:status :pending-rejection :tag :template-verification-rejected
           :value pending-rejected-envelope}}
 {root sh07-b51-resolve-digest-preimage
  args [request digest-requests resolved-prefix]
  success {:status :accepted :tag :digest-preimage-resolved
           :value digest-preimage-resolution-accepted}
  failure {:status :boundary-rejected :tag :digest-preimage-boundary-rejected
           :value digest-preimage-resolution-boundary-rejection}}
 {root sh07-b51-resolve-template
  args [template digest-requests resolved-digests]
  success {:status :accepted :tag :template-resolved
           :value {:resolved-core resolved-core-record
                   :predecessor-observation predecessor-observation-record
                   :provenance-binding provenance-binding-record}}
  failure {:status :pending-rejection :tag :template-resolution-rejected
           :value pending-rejected-envelope}}
 {root sh07-b51-verify-resolved
  args [raw-carrier resolved-core digest-requests resolved-digests]
  success {:status :accepted :tag :resolved-verified
           :value exact-passed-resolved-verification-v18}
  failure {:status :pending-rejection :tag :resolved-verification-rejected
           :value pending-rejected-envelope}}
 {root sh07-b51-build-independent-verifier-binding
  args [raw-carrier resolved-core predecessor-observation provenance-binding
        digest-requests resolved-digests]
  success {:status :accepted :tag :independent-verifier-bound
           :value independent-verifier-binding-record}
  failure {:status :pending-rejection :tag :independent-verifier-rejected
           :value pending-rejected-envelope}}
 {root sh07-b51-build-final-artifact
  args [raw-carrier resolved-core predecessor-observation provenance-binding
        verifier-binding digest-requests resolved-digests]
  success {:status :accepted :tag :final-artifact-built
           :value {:canonical-artifact canonical-output-record
                   :outer-wrapper canonical-wrapper-record | nil}}
  failure {:status :pending-rejection :tag :final-artifact-rejected
           :value pending-rejected-envelope}}
 {root sh07-b51-finalize-rejection
  args [pending-rejected-envelope resolved-diagnostic-id]
  success {:status :accepted :tag :rejection-finalized
           :value failure-resolver-return}
 failure {:status :boundary-rejected :tag :rejection-finalizer-boundary-rejected
           :value finalizer-boundary-rejection}}]

exact-passed-template-verification-v18 :=
{:artifact :gravity/sh07-b51-template-verification-v18
 :schema-version 18 :status :passed :rule nil :reason nil :details {}}

exact-passed-resolved-verification-v18 :=
{:artifact :gravity/sh07-b51-resolved-verification-v18
 :schema-version 18 :status :passed :rule nil}
```

Each row expands `success` or `failure` into the six ABI keys above, and the
row's `:status`, `:tag`, and exact closed value are literal. The two passed
verification values are closed to their named artifact/schema/status/rule/
reason/details contracts; the canonical and binding values are the exact
closed schemas below. Each `args` vector is the exact positional arity and
closed type sequence; optional, keyword, reordered, or extra arguments are a
row failure. A tag/value borrowed from another row is invalid.

The execution states are `R` raw-carrier admission, `T` template construction,
`D` controlled digest resolution, `C` resolved-core construction, `V`
producer-side resolved verification, `I` independent verifier binding, and
`O` final artifact/provenance assembly; `X` is failure-only rejection
finalization after the host returns a diagnostic hash. On success, entry point
4 returns one closed
resolution bundle exactly of
`{:resolved-core resolved-core-record
  :predecessor-observation predecessor-observation-record
  :provenance-binding provenance-binding-record}`;
the latter two are Tier-4 observation/provenance materializations and are not
fed into the Tier-3 core identity. Within `D`, the host may perform only
entrypoint 3's opaque resolution for exact requests. Success invokes
entrypoints 1-7 in order and never invokes 8; failure invokes the detecting
entrypoint, then 3 for the exact diagnostic request, then 8. The host never constructs a preimage,
product, outcome, or semantic id. B47's retained roots are invoked
mechanically inside the raw predecessor observation before entrypoint 1 and
their returns are transported unchanged.

Entrypoint 3 has this exact ABI. Its first argument is the exact nine-key
request at `digest-requests[request.ordinal]`; its second argument is the
complete exact request plan. Its third argument is literally `[]` for the
failure stream and the exact already-resolved dense prefix for the success
stream:

```text
digest-preimage-resolution-accepted :=
{:status :accepted
 :request exact-nine-key-request | failure-digest-request
 :digest-requests exact-complete-success-request-vector
                  | [failure-digest-request]
 :resolved-prefix vector-of-digest-id
 :hash-input {:domain :gravity/sh07-declared-digest-v1
              :purpose request.purpose
              :preimage request.preimage}}
digest-preimage-resolution-boundary-rejection :=
{:status :boundary-rejected
 :boundary :sh07-b51-resolve-digest-preimage
 :reason :malformed-request | :malformed-request-plan | :prefix-mismatch
         | :schedule-mismatch | :cardinality-mismatch
 :recursive-diagnostic-forbidden true}

host-digest-receipt :=
{:hash-input exact-accepted-hash-input
 :digest-id sha256-of-C11-pr-str-hash-input}
```

Gravity validates the exact request keyset, vector-index/global ordinal,
purpose/domain/tier/subtier/batch/rank/cardinality plan, typed reference
targets, and prefix, then returns the accepted hash input; the host performs
only the C11 hash and returns the
closed receipt. Malformed root-3 input returns the boundary-rejected variant,
not a diagnostic request or throw. Entrypoint 8 accepts only an exact pending
envelope plus the receipt's digest and returns `failure-resolver-return`.
Failed authentication returns exactly
```text
finalizer-boundary-rejection :=
{:status :boundary-rejected :boundary :sh07-b51-finalize-rejection
  :reason :pending-envelope-mismatch | :resolved-id-shape
  :recursive-diagnostic-forbidden true}
```
It never invokes root 3 or 8 again.
Mutation probes require malformed ABI inputs to reach these closed boundary
rejections with no host exception and no diagnostic recursion.

Only `request.purpose` and `request.preimage` enter `:hash-input`. Ordinal,
tier, subtier, subtier ordinal, batch, batch rank, batch ordinal, and the
complete request plan are admission/scheduling evidence and never semantic
hash fields.

The two additional 0a preimages are closed and are resolved before any
outcome, authority, or product request:

```text
implementation-semantic-id-preimage-v18 :=
{:domain :gravity/sh07-b51-implementation-semantic-id-preimage-v18
 :schema-version 18
 :predecessor-executable-descriptor exact-f473-executable-descriptor
 :v18-semantic-closure S
 :v18-semantic-closure-hash hC}

contract-semantic-id-preimage-v18 :=
{:domain :gravity/sh07-b51-contract-semantic-id-preimage-v18
 :schema-version 18
 :predecessor-contract-carrier exact-b47-contract-carrier
 :retained-source-contract exact-predecessor-source-contract-record}
```

The implementation preimage carries complete S, hC, and the full f473 map,
never P or a host-generated census summary. The contract preimage carries the
complete retained three-form record rather than an undefined v18 contract
alias. The two preimages are independent and own-id-free, so neither 0a id can
refer to the other or to a later branch.

Every v18 request is a closed map with exactly these keys:

```text
{:ordinal nonnegative-integer
 :tier 0 | 1 | 2 | 3 | 4 | 5 | 6
 :subtier :0a | :0b | :0c | :0d | nil
 :subtier-ordinal nonnegative-integer | nil
 :batch :0a-implementation | :0a-contract | :0a-same-request
        | :0a-v16-verification | :0b-outcome | :0c-authority
        | :0d-core-node | :0d-product-node | :t1-slot | :t1-extraction
        | :t2-runtime-check | :t2-publication | :t2-recur-mapping
        | :t2-transcript | :t3-core-identity | :t4-observation
        | :t4-provenance | :t5-verifier | :t6-final
 :batch-rank integer-in-0-through-18
 :batch-ordinal nonnegative-integer
 :purpose purpose-catalog-key
 :preimage exact-purpose-preimage-selected-by-19-row-catalog}
```

`purpose-catalog-key` is closed to this exact 19-row purpose/domain/tier/
subtier/batch/batch-rank/branch/cardinality catalog. The resolver checks
every column rather than looking up a merely corresponding domain:

```text
purpose-catalog :=
{:sh07-b51-implementation-semantic-id
 {:preimage-domain :gravity/sh07-b51-implementation-semantic-id-preimage-v18
  :tier 0 :subtier :0a :batch :0a-implementation :batch-rank 0
  :branch :both :cardinality :one-per-C}
 :sh07-b51-contract-semantic-id
 {:preimage-domain :gravity/sh07-b51-contract-semantic-id-preimage-v18
  :tier 0 :subtier :0a :batch :0a-contract :batch-rank 1
  :branch :both :cardinality :one}
 :sh07-b51-same-request-semantic-id
 {:preimage-domain :gravity/sh07-b51-same-request-binding-v18
  :tier 0 :subtier :0a :batch :0a-same-request :batch-rank 2
  :branch :both :cardinality :one}
 :sh07-b51-v16-verification-semantic-id
 {:preimage-domain :gravity/sh07-b51-v16-verification-semantic-v18
  :tier 0 :subtier :0a :batch :0a-v16-verification :batch-rank 3
  :branch :accepted :cardinality :one}
 :sh07-b51-predecessor-outcome-semantic-id
 {:preimage-domain :gravity/sh07-b51-predecessor-outcome-semantic-v18
  :tier 0 :subtier :0b :batch :0b-outcome :batch-rank 4
  :branch :both :cardinality :one}
 :sh07-b51-predecessor-authority-id
 {:preimage-domain :gravity/sh07-b51-predecessor-authority-v18
  :tier 0 :subtier :0c :batch :0c-authority :batch-rank 5
  :branch :both :cardinality :one}
 :sh07-b51-core-node-id
 {:preimage-domain :gravity/sh07-b51-core-node-id-preimage-v18
  :tier 0 :subtier :0d :batch :0d-core-node :batch-rank 6 :branch :rejected
  :cardinality :per-rejected-core-node}
 :sh07-b51-product-node-id
 {:preimage-domain :gravity/sh07-b51-product-node-id-preimage-v18
  :tier 0 :subtier :0d :batch :0d-product-node :batch-rank 7
  :branch :both :cardinality :per-supplemental-product}
 :sh07-b51-binding-slot-id
 {:preimage-domain :gravity/sh07-b51-binding-slot-id-preimage-v18
  :tier 1 :subtier nil :batch :t1-slot :batch-rank 8
  :branch :both :cardinality :per-slot}
 :sh07-b51-binding-extraction-id
 {:preimage-domain :gravity/sh07-b51-binding-extraction-id-preimage-v18
  :tier 1 :subtier nil :batch :t1-extraction :batch-rank 9
  :branch :both :cardinality :per-extraction}
 :sh07-b51-runtime-check-id
 {:preimage-domain :gravity/sh07-b51-runtime-check-id-preimage-v18
  :tier 2 :subtier nil :batch :t2-runtime-check :batch-rank 10
  :branch :both :cardinality :per-vector-node-use}
 :sh07-b51-publication-event-id
 {:preimage-domain :gravity/sh07-b51-publication-event-id-preimage-v18
  :tier 2 :subtier nil :batch :t2-publication :batch-rank 11
  :branch :both :cardinality :per-event}
 :sh07-b51-recur-slot-mapping-id
 {:preimage-domain :gravity/sh07-b51-recur-slot-mapping-id-preimage-v18
  :tier 2 :subtier nil :batch :t2-recur-mapping :batch-rank 12
  :branch :both :cardinality :per-recur}
 :sh07-b51-slot-extraction-transcript-id
 {:preimage-domain :gravity/sh07-b51-slot-extraction-transcript-id-preimage-v18
  :tier 2 :subtier nil :batch :t2-transcript :batch-rank 13
  :branch :both :cardinality :one}
 :sh07-b51-core-identity-id
 {:preimage-domain :gravity/sh07-b51-core-identity-preimage-v18
  :tier 3 :subtier nil :batch :t3-core-identity :batch-rank 14
  :branch :both :cardinality :one}
 :sh07-b51-predecessor-observation-id
 {:preimage-domain :gravity/sh07-b51-predecessor-observation-preimage-v18
  :tier 4 :subtier nil :batch :t4-observation :batch-rank 15
  :branch :both :cardinality :one}
 :sh07-b51-provenance-binding-id
 {:preimage-domain :gravity/sh07-b51-provenance-binding-preimage-v18
  :tier 4 :subtier nil :batch :t4-provenance :batch-rank 16
  :branch :both :cardinality :one}
 :sh07-b51-independent-verifier-binding-id
 {:preimage-domain :gravity/sh07-b51-independent-verifier-binding-preimage-v18
  :tier 5 :subtier nil :batch :t5-verifier :batch-rank 17
  :branch :both :cardinality :one}
 :sh07-b51-final-artifact-binding-id
 {:preimage-domain :gravity/sh07-b51-final-artifact-binding-preimage-v18
  :tier 6 :subtier nil :batch :t6-final :batch-rank 18
  :branch :both :cardinality :one}}
```

The catalog is printed in increasing `:batch-rank` order. It contains exactly
one literal batch per purpose: ranks 0 through 18 are implementation,
contract, same-request, accepted-only v16 verification, outcome, authority,
rejected core node, product node, slot, extraction, runtime check,
publication, recur mapping, transcript, core identity, observation,
provenance, verifier, and final binding. Tier 0 requests use their declared
0a/0b/0c/0d subtier; every later request has `:subtier nil`. A request whose
purpose, preimage domain, tier, subtier, batch, batch rank, branch, or
cardinality does not match this literal catalog is `C6-VERIFY`.
`:one-per-C` means exactly one implementation-semantic-id request for the one
admitted combined-C semantic closure pair `(S,hC)` in the raw carrier, emitted
at global, subtier, and batch ordinal zero; a second closure, missing request, or one
request per root/member is invalid. In a rejected core-node preimage the only
controlled references are `:predecessor-authority-id`, every `:children`
entry, and every `:evaluated-children` entry. `:resolved-binding-ids` and
`:binding-context` are concrete authenticated upstream ids and are never
looked up in the v18 digest vector.
In the catalog, `:both` means all four closed predecessor outcomes,
`:accepted` means only `:legacy-v16-accepted`, and `:rejected` means every
other literal outcome (including the semantically accepted-after-waiver
extension, which is not v16 acceptance); no fifth branch is implied.

This is also the exhaustive digest-class partition. The nineteen rows above
are the complete success DAG and no twentieth success purpose exists. `hC` is
a direct pre-admission C11 hash of the exact v18 semantic closure `S`, not an
ordered declared-digest purpose. `f473...` is the inherited pinned direct C11
predecessor descriptor hash, likewise not a v18 purpose. On the accepted
branch, the B47 semantic artifact id retains its inherited purpose
`:sh07-core-artifact-id`; V18 verifies and carries it but never reissues it.
The failure-only `:sh07-b51-c6-diagnostic-id` purpose and its exact preimage
domain are outside this success stream. Every other digest-bearing field is
either an authenticated upstream/inherited digest, one of these direct closure
hashes, or the resolved value of exactly one cataloged request. Any
unclassified digest or purpose substitution is `C6-VERIFY`.

The 0a `:subtier-ordinal` is also literal and branch-sensitive:

```text
accepted  [[:sh07-b51-implementation-semantic-id 0]
           [:sh07-b51-contract-semantic-id 1]
           [:sh07-b51-same-request-semantic-id 2]
           [:sh07-b51-v16-verification-semantic-id 3]]
rejected  [[:sh07-b51-implementation-semantic-id 0]
           [:sh07-b51-contract-semantic-id 1]
           [:sh07-b51-same-request-semantic-id 2]]
```

The legacy-v16-accepted branch has four 0a requests and every non-v16 branch
exactly three; no other request may claim `:0a`. Each 0a purpose is nevertheless
its own batch, so its `:batch-ordinal` is zero. The 0b and 0c singleton batches
also use subtier ordinal zero. In 0d, rejected core-node subtier ordinals are
their postorder batch ordinals, and product-node subtier ordinal is
`core-node-batch-count + product-node-batch-ordinal`; accepted outcomes have
zero core nodes and begin products at zero. All nil-subtier requests have nil
`:subtier-ordinal`.

For active batch `b` and zero-based request `i`, `:batch-ordinal = i` and
`:ordinal = (sum of the cardinalities of every active batch with smaller
batch-rank) + i`. Static batch-rank 3 remains reserved when the accepted-only
v16 verification batch is inactive, while active global ordinals remain dense.
The vector index equals `:ordinal`; every batch ordinal is dense from zero.

A controlled reference is legal iff its registry entry names the target
purpose, the target request is the unique request at that ordinal, target
`:ordinal` is strictly smaller than source `:ordinal`, and
`[target.batch-rank target.batch-ordinal]` is lexicographically strictly less
than `[source.batch-rank source.batch-ordinal]`. There are no exceptions.
Within the core-node batch, left-to-right DFS postorder makes every child and
evaluated-child batch ordinal strictly smaller than its parent. No other
same-batch controlled reference exists. Extraction-to-slot,
publication-to-check/product/slot, mapping-to-publication/check/product/slot/
extraction, transcript-to-all preceding Tier-1/2 products, and
provenance-to-observation are therefore ordinary earlier-batch edges, not
special cases. Forward, equal-coordinate, purpose-substituted, and cyclic
mutations are `C6-VERIFY`.

The purpose-level dependency summary is literal and exhaustive (a repeated
same purpose means zero or more earlier requests in that batch):

```text
batch-dependency-catalog :=
{:sh07-b51-implementation-semantic-id []
 :sh07-b51-contract-semantic-id []
 :sh07-b51-same-request-semantic-id []
 :sh07-b51-v16-verification-semantic-id []
 :sh07-b51-predecessor-outcome-semantic-id
 [:sh07-b51-implementation-semantic-id :sh07-b51-contract-semantic-id
  :sh07-b51-same-request-semantic-id
  {:accepted-only :sh07-b51-v16-verification-semantic-id}]
 :sh07-b51-predecessor-authority-id
 [:sh07-b51-implementation-semantic-id :sh07-b51-contract-semantic-id
  :sh07-b51-same-request-semantic-id
  :sh07-b51-predecessor-outcome-semantic-id]
 :sh07-b51-core-node-id
 [:sh07-b51-predecessor-authority-id
  {:same-batch-earlier :sh07-b51-core-node-id}]
 :sh07-b51-product-node-id [:sh07-b51-predecessor-authority-id]
 :sh07-b51-binding-slot-id
 [:sh07-b51-predecessor-authority-id
  {:non-v16-only :sh07-b51-core-node-id}]
 :sh07-b51-binding-extraction-id
 [:sh07-b51-predecessor-authority-id :sh07-b51-binding-slot-id]
 :sh07-b51-runtime-check-id
 [:sh07-b51-predecessor-authority-id
  {:non-v16-only :sh07-b51-core-node-id}
  :sh07-b51-binding-slot-id :sh07-b51-binding-extraction-id]
 :sh07-b51-publication-event-id
 [:sh07-b51-predecessor-authority-id
  {:non-v16-only :sh07-b51-core-node-id}
  :sh07-b51-product-node-id :sh07-b51-binding-slot-id
  :sh07-b51-runtime-check-id]
 :sh07-b51-recur-slot-mapping-id
 [:sh07-b51-predecessor-authority-id
  {:non-v16-only :sh07-b51-core-node-id}
  :sh07-b51-product-node-id :sh07-b51-binding-slot-id
  :sh07-b51-binding-extraction-id :sh07-b51-runtime-check-id
  :sh07-b51-publication-event-id]
 :sh07-b51-slot-extraction-transcript-id
 [:sh07-b51-predecessor-authority-id :sh07-b51-same-request-semantic-id
  :sh07-b51-binding-slot-id :sh07-b51-binding-extraction-id
  :sh07-b51-runtime-check-id :sh07-b51-publication-event-id
  :sh07-b51-recur-slot-mapping-id]
 :sh07-b51-core-identity-id
 [:sh07-b51-same-request-semantic-id :sh07-b51-predecessor-authority-id
  {:non-v16-only :sh07-b51-core-node-id} :sh07-b51-product-node-id
  :sh07-b51-binding-slot-id :sh07-b51-binding-extraction-id
  :sh07-b51-runtime-check-id :sh07-b51-publication-event-id
  :sh07-b51-recur-slot-mapping-id :sh07-b51-slot-extraction-transcript-id]
 :sh07-b51-predecessor-observation-id [:sh07-b51-core-identity-id]
 :sh07-b51-provenance-binding-id
 [:sh07-b51-core-identity-id :sh07-b51-predecessor-observation-id]
 :sh07-b51-independent-verifier-binding-id
 [:sh07-b51-core-identity-id :sh07-b51-provenance-binding-id]
 :sh07-b51-final-artifact-binding-id
 [:sh07-b51-core-identity-id :sh07-b51-provenance-binding-id
  :sh07-b51-independent-verifier-binding-id]}
```

Requests are dense in `:ordinal`, dense in each batch, dense in each non-nil
subtier under the formula above, and topologically ordered. A request's tier,
subtier, batch, and rank must equal its catalog row; no request can mention its
own or a descendant id. The
single ordered vector includes fixed Tier-5 check commitments and the Tier-6
binding commitment as candidate requests before resolution. These candidates
are not acceptance results: only entrypoints 6 and 7 may validate and
materialize/promote their already-resolved values after `V`/`I`. The
controlled reference emitted for each request uses the existing internal
reference schema exactly:

```text
{:artifact :gravity/sh07-internal-digest-reference
 :schema-version 1
 :ordinal nonnegative-integer
 :authority :sh07-digest-resolver}
```

For each request Gravity returns the exact accepted hash input and the host
computes only `sha256(C11-pr-str hash-input)` and returns the exact receipt; it
does not sort, delete, add, rename, or otherwise derive fields. A missing,
duplicate, reordered, out-of-range, or
cross-tier reference is `C6-VERIFY`. The host loop is therefore mechanically
`request -> exact resolver input -> exact resolved value -> Gravity`, with no
host semantic construction. `R -> T -> D -> C -> V -> I -> O` is an execution
ordering record, not an additional semantic tier. A missing candidate, failed
check, or mismatched expected-pass commitment prevents promotion and yields a
closed diagnostic; it cannot be repaired by issuing a new host request.

There is no separate adapter-request semantic id. The schema-15
`:projection-binding` and the `:same-request-semantic-id` emitted in 0a are the
canonical request identity. Gravity derives that one own-id-free,
path-neutral request projection from the authenticated schema-15 request
(root ids, forms, bindings, resolutions, fragments, origins, and module
assembly). It does not include the raw B47 outcome, physical invocation,
report, artifact, path, or its own id. The template, resolved core, and output
therefore carry only `:same-request-semantic-id`; no host-supplied request id or
separately counted fifth 0a request is admitted. In the 0a record,
`:same-request-semantic-id` is the one digest carried through every product
schema; it is not a separate binding id or request.

## Complete v18 carrier, template, resolved core, and output

### Product closure

V18 preserves the complete B47 semantic product family and adds its own
products. The fixed semantic product key order is:

```text
[:fragment-manifest :fragment-coverage :module-assembly-manifest
 :root-core-node-ids :definitions :nodes :evaluation-order :control-flow
 :reference-uses :var-references :mutations :error-transfers :error-handlers
 :match-branch-records :match-decision-skeletons :match-pattern-records
 :calls :function-records :call-edges :recursion-components :keyword-lookups
 :lexical-bindings :loop-bindings :recur-targets :recur-transfers :source-map
 :binding-table :declared-alias-table :resolution-table
 :macro-expansion-trace :macro-origin-traces :macro-origin-expectation
 :pending-fact-families :destructuring-product-nodes :binding-slots
 :binding-extractions
 :slot-extraction-transcript :runtime-checks :publication-events
 :recur-slot-mappings :legacy-v16-equivalence]
```

This product vector has exactly 41 keys: the first 33 are inherited B47
product categories through `:pending-fact-families`, followed by exactly eight
B51 categories from `:destructuring-product-nodes` through
`:legacy-v16-equivalence`. The v16 semantic projection has 37 fields because
it prepends the four top-level fields `:domain`, `:lineage`,
`:projection-binding`, and `:module` to those 33 inherited products. It never
mistakes 37 semantic fields for 37 product keys.

No open `:products` map is permitted. Each key is required with a vector or
closed record of the schema specified by the corresponding v18 product
domain. An absent category, an extra category, an unbound digest reference,
or an out-of-order product is `C6-VERIFY`.

The word `vector` in the carrier schemas is not an open host-language vector.
It is a closed alias selected by this registry; each named B47 schema is the
exact key set, element type, cardinality, ordering, and invariant from the
freshly admitted M resolver, and each B51 schema is defined below:

```text
inherited-product-schema-registry :=
{:fragment-manifest exact-b47-fragment-manifest-record
 :fragment-coverage exact-b47-fragment-coverage-record
 :module-assembly-manifest exact-b47-module-assembly-manifest-record
 :root-core-node-ids exact-b47-root-core-node-id-vector
 :definitions exact-b47-definition-record-vector
 :nodes exact-branch-tagged-core-node-record-vector
 :evaluation-order exact-b47-evaluation-order-record-vector
 :control-flow exact-b47-control-flow-record-vector
 :reference-uses exact-b47-reference-use-record-vector
 :var-references exact-b47-var-reference-record-vector
 :mutations exact-b47-mutation-record-vector
 :error-transfers exact-b47-error-transfer-record-vector
 :error-handlers exact-b47-error-handler-record-vector
 :match-branch-records exact-b47-match-branch-record-vector
 :match-decision-skeletons exact-b47-match-decision-skeleton-record-vector
 :match-pattern-records exact-b47-match-pattern-record-vector
 :calls exact-b47-call-record-vector
 :function-records exact-b47-function-record-vector
 :call-edges exact-b47-call-edge-record-vector
 :recursion-components exact-b47-recursion-component-record-vector
 :keyword-lookups exact-b47-keyword-lookup-record-vector
 :lexical-bindings accepted:exact-b47-lexical-binding-record-vector
                   | rejected:exact-v18-lexical-slot-record-vector
 :loop-bindings accepted:exact-b47-loop-binding-record-vector
                | rejected:exact-v18-loop-slot-record-vector
 :recur-targets accepted:exact-b47-recur-target-record-vector
                | rejected:exact-v18-mixed-recur-target-record-vector
 :recur-transfers accepted:exact-b47-recur-transfer-record-vector
                  | rejected:exact-v18-mixed-recur-transfer-record-vector
 :source-map exact-b47-source-map-record-vector
 :binding-table exact-b47-binding-table-record-vector
 :declared-alias-table exact-b47-declared-alias-record-vector
 :resolution-table exact-b47-resolution-record-vector
 :macro-expansion-trace exact-b47-macro-expansion-record-vector
 :macro-origin-traces exact-b47-macro-origin-trace-record-vector
 :macro-origin-expectation exact-b47-macro-origin-expectation-record
 :pending-fact-families exact-b47-pending-fact-family-vector
 :destructuring-product-nodes exact-v18-product-node-record-vector
 :binding-slots exact-v18-binding-slot-record-vector
 :binding-extractions exact-v18-binding-extraction-record-vector
 :slot-extraction-transcript exact-v18-transcript-record
 :runtime-checks exact-v18-runtime-check-record-vector
 :publication-events exact-v18-publication-event-record-vector
 :recur-slot-mappings exact-v18-recur-mapping-record-vector
 :legacy-v16-equivalence exact-v18-legacy-equivalence-record | nil}
```

Each `exact-b47-*` alias is pinned to the M `sh07-resolve-*` validator and
rejects a missing, extra, renamed, or wrongly typed field. Each
`exact-v18-*` alias is the closed schema in this report. The registry is
validated independently before template references are substituted, so a
generic host vector/map cannot satisfy a product position. The
`:nodes` alias selects the concrete B47 validator with unchanged ids on the
accepted branch and the closed `core-node-v18` validator with v18 ids on the
rejected branch; no mixed vector is valid.

The three shared records embedded by reference in every template, resolved
core, and accepted output are closed as follows. Their semantic projections
retain source-revision and authenticated SH-06 ids but contain no physical
path or transport fields:

```text
module :=
{:namespace symbol
 :profile :meta
 :target :jvm
 :safety :safe
 :effects vector-of-keyword
 :capabilities vector-of-keyword
 :exports vector-of-symbol
 :source-revision-id digest-id}

lineage :=
{:sh06-artifact-id digest-id
 :authenticated-sh06-artifact-id digest-id
 :sh06-semantic-projection-id digest-id
 :sh06-analysis-artifact-id digest-id
 :source-revision-id digest-id
 :sh05-artifact-id digest-id
 :expanded-syntax-stream-id digest-id
 :macro-expansion-trace-id digest-id
 :binding-table-id digest-id
 :alias-table-id digest-id
 :resolution-table-id digest-id
 :lexical-scope-graph-id digest-id
 :authenticated-envelope-id digest-id}

fragment-coverage :=
{:root-form-count nonnegative-integer
 :form-count nonnegative-integer
 :local-binding-count nonnegative-integer
 :resolution-count nonnegative-integer
 :fragment-count nonnegative-integer
 :covered-root-form-ids vector-of-id
 :covered-form-ids vector-of-id
 :covered-local-binding-ids vector-of-id
 :covered-resolution-reference-syntax-ids vector-of-id}

module-assembly-manifest :=
{:ordered-fragment-ids vector-of-id
 :root-form-ids vector-of-id
 :source-revision-id digest-id
 :sh06-semantic-projection-id digest-id
 :alias-table-id digest-id
 :content-id digest-id
 :module-id digest-id}

```

`module` and `lineage` are embedded by value in the schemas below. The
`:projection-binding` field in each schema is the unchanged authenticated
schema-15 request value `p` defined in the same-request section; it is not a
new v18 record or domain. A missing/extra key, wrong profile/target/safety,
set/vector substitution, or physical field in any shared record is
`C6-VERIFY`.

The rejected branch cannot reuse B47's leaf-count-as-arity records. Its three
branch-specific inherited-category schemas are closed and keep slot identity
separate from leaf bindings:

```text
v18-lexical-slot-record :=
{:let-core-node-id digest-id
 :slot-id digest-id
 :slot-ordinal nonnegative-integer
 :leaf-ordinal nonnegative-integer
 :name symbol
 :binding-id digest-id
 :definition-form-id digest-id
 :definition-syntax-id digest-id
 :binding-scope-id digest-id
 :initializer-form-id digest-id
 :initializer-syntax-id digest-id
 :initializer-scope-id digest-id
 :initializer-node-id digest-id
 :visible-prior-binding-ids vector-of-digest-id
 :mutability :immutable}

v18-loop-slot-record :=
{:loop-core-node-id digest-id
 :slot-id digest-id
 :slot-ordinal nonnegative-integer
 :leaf-ordinal nonnegative-integer
 :name symbol
 :binding-id digest-id
 :definition-form-id digest-id
 :definition-syntax-id digest-id
 :binding-scope-id digest-id
 :initializer-form-id digest-id
 :initializer-syntax-id digest-id
 :initializer-scope-id digest-id
 :initializer-node-id digest-id
 :visible-prior-binding-ids vector-of-digest-id
 :mutability :immutable}

v18-function-recur-target-record :=
{:target-id digest-id
 :target-kind :function
 :owner-core-node-id digest-id
 :owner-form-id digest-id
 :owner-syntax-id digest-id
 :arity nonnegative-integer
 :binding-ids vector-of-digest-id
 :body-scope-id digest-id
 :parent-target-id digest-id | nil
 :type-compatibility :pending-sh08}

v18-loop-slot-recur-target-record :=
{:target-id digest-id
 :target-kind :loop
 :owner-core-node-id digest-id
 :owner-form-id digest-id
 :owner-syntax-id digest-id
 :slot-count nonnegative-integer
 :slot-ids vector-of-digest-id
 :slot-ordinals vector-of-nonnegative-integer
 :binding-id-vectors vector-of-vector-of-digest-id
 :body-scope-id digest-id
 :parent-target-id digest-id | nil
 :type-compatibility :pending-sh08}

v18-mixed-recur-transfer-record :=
{:recur-core-node-id digest-id
 :recur-form-id digest-id
 :recur-syntax-id digest-id
 :target-id digest-id
 :target-kind :function | :loop
 :arity nonnegative-integer
 :argument-node-ids vector-of-digest-id
 :tail-position true
 :evaluation-order :arguments-left-to-right
 :transfer-policy :nearest-lexical-recur-target
 :type-compatibility :pending-sh08}
```

Lexical/loop records are ordered by slot then leaf, and each record's slot id
and ordinal join the exact slot product. `:leaf-ordinal` is dense only within
that slot. The recur target has one slot id, slot ordinal, and binding-id vector
per slot; `:slot-count` is the length of all three aligned vectors and no leaf
count is called arity. A rejected whole-request rebuild uses the tagged union
of function targets and loop-slot targets: the function variant preserves the
exact B47 field set, flat binding-id vector, and arity, changing only the
controlled owner-node reference while unresolved; the loop variant uses the
slot-aligned schema. Transfers retain the exact B47 field set and tag and join
one unique target. Function transfers require argument count = function arity
and emit no B51 slot/check/publication/mapping products. Loop transfers require
argument count = slot count and emit the aligned B51 products below. Nearest
target selection replays the complete mixed lexical target stack, so a nested
function inside a loop or loop inside a function cannot capture the wrong
target. Accepted records remain byte-for-byte B47 records under the same
category keys. Mixed branch schemas, a flattened leaf/slot ordinal, or B51
products attached to a function target are `C6-VERIFY`.

### Canonical template schema

Before digest resolution, every new v18 id position in the template is the
exact `internal-digest-reference` record used by the ordered request stream;
it is never a host placeholder or a concrete digest guessed from an ordinal.
Authenticated upstream ids, including schema-15 `:projection-binding` `p`,
remain concrete digest ids. Resolved-core and output schemas below require
concrete digest ids everywhere and reject an internal reference.

```text
internal-digest-reference :=
{:artifact :gravity/sh07-internal-digest-reference
 :schema-version 1
 :ordinal nonnegative-integer
 :authority :sh07-digest-resolver}
```

`:gravity/sh07-b51-canonical-template-v18` is a closed map with exactly:

```text
{:artifact :gravity/sh07-b51-canonical-template-v18
 :schema-version 18
 :predecessor-outcome-kind :legacy-v16-accepted | :b51-vector-frontier-rejected
                           | :legacy-v16-accepted-after-no-form-waiver
                           | :b51-vector-frontier-rejected-after-no-form-waiver
 :same-request-semantic-id internal-digest-reference
 :predecessor-authority-id internal-digest-reference
 :module module-record
 :lineage lineage-record
 :projection-binding digest-id
 :fragment-manifest vector
 :fragment-coverage fragment-coverage-record
 :module-assembly-manifest module-assembly-manifest-record
 :root-core-node-ids accepted:vector-of-digest-id
                     | rejected:vector-of-internal-digest-reference
 :definitions vector
 :nodes vector
 :evaluation-order vector
 :control-flow vector
 :reference-uses vector
 :var-references vector
 :mutations vector
 :error-transfers vector
 :error-handlers vector
 :match-branch-records vector
 :match-decision-skeletons vector
 :match-pattern-records vector
 :calls vector
 :function-records vector
 :call-edges vector
 :recursion-components vector
 :keyword-lookups vector
 :lexical-bindings vector
 :loop-bindings vector
 :recur-targets vector
 :recur-transfers vector
 :source-map vector
 :binding-table vector
 :declared-alias-table vector
 :resolution-table vector
 :macro-expansion-trace vector
 :macro-origin-traces vector
 :macro-origin-expectation map
 :pending-fact-families vector
 :destructuring-product-nodes vector
 :binding-slots vector
 :binding-extractions vector
 :slot-extraction-transcript map
 :runtime-checks vector
 :publication-events vector
 :recur-slot-mappings vector
 :legacy-v16-equivalence record | nil
 :digest-requests vector
 :diagnostics vector}
```

The same internal-reference rule applies recursively to new v18 ids inside
the product vectors, transcript, checks, publications, and recur mappings in
this template; the resolved-core conversion replaces each with the exact
resolved digest returned for its request. No request ordinal is accepted as a
semantic id. The branch tag governs this rule: on
`:legacy-v16-accepted`, all 33 inherited B47 product categories and their concrete
v16 ids are copied byte-for-byte into the template and are never rewritten;
only supplemental B51 positions use internal references, and the v18
core-node request set is empty. On every other outcome, including both
no-form-waiver outcomes, the
inherited products are nil until the whole request is rebuilt and every
generated product/core-node id is an internal reference to its ordered
request. A mixed branch, an accepted inherited id represented by an internal
reference, or a rejected inherited product copied from a partial B47 result is
`C6-VERIFY`.

The recursive reference positions are closed and typed. `:each` means every
element of the schema-bounded vector; `:optional` means exactly nil or one
reference. The inherited registry is:

```text
inherited-product-reference-paths :=
[[:fragment-manifest :each :root-node-ids :each]
 [:root-core-node-ids :each]
 [:definitions :each :core-node-id]
 [:definitions :each :value-node-id]
 [:nodes :each :node-id]
 [:nodes :each :children :each]
 [:nodes :each :evaluation :order :each :core-node-id]
 [:evaluation-order :each :core-node-id]
 [:evaluation-order :each :children :each]
 [:control-flow :each :core-node-id]
 [:control-flow :each :condition-node-id :if-kind]
 [:control-flow :each :branches :if-kind :each :core-node-id]
 [:control-flow :each :ordered-child-node-ids :do-kind :each]
 [:control-flow :each :result-policy :do-kind :core-node-id]
 [:reference-uses :each :core-node-id]
 [:var-references :each :core-node-id]
 [:mutations :each :core-node-id]
 [:mutations :each :value-core-node-id]
 [:mutations :each :evaluated-children :each]
 [:error-transfers :each :core-node-id]
 [:error-transfers :each :value-core-node-id]
 [:error-transfers :each :evaluated-children :each]
 [:error-handlers :each :core-node-id]
 [:error-handlers :each :protected-core-node-id]
 [:error-handlers :each :handler-core-node-id]
 [:error-handlers :each :candidate-error-transfers :each :core-node-id]
 [:match-branch-records :each :core-node-id]
 [:match-branch-records :each :scrutinee-core-node-id]
 [:match-branch-records :each :branch-core-node-id]
 [:match-decision-skeletons :each :core-node-id]
 [:match-decision-skeletons :each :scrutinee-core-node-id]
 [:match-decision-skeletons :each :branch-core-node-ids :each]
 [:match-pattern-records :each :core-node-id]
 [:calls :each :core-node-id]
 [:calls :each :operator-node-id]
 [:calls :each :argument-node-ids :each]
 [:calls :each :ordered-evaluation-node-ids :each]
 [:function-records :each :function-core-node-id]
 [:function-records :each :body-core-node-id]
 [:function-records :each :definition-core-node-id :optional]
 [:call-edges :each :call-core-node-id]
 [:call-edges :each :caller-function-core-node-id :optional]
 [:call-edges :each :callee-function-core-node-id :optional]
 [:call-edges :each :argument-core-node-ids :each]
 [:call-edges :each :ordered-evaluation-node-ids :each]
 [:recursion-components :each :function-core-node-ids :each]
 [:keyword-lookups :each :core-node-id]
 [:keyword-lookups :each :keyword-node-id]
 [:keyword-lookups :each :map-node-id]
 [:keyword-lookups :each :ordered-evaluation-node-ids :each]
 [:lexical-bindings :each :let-core-node-id]
 [:lexical-bindings :each :initializer-node-id]
 [:loop-bindings :each :loop-core-node-id]
 [:loop-bindings :each :initializer-node-id]
 [:recur-targets :each :owner-core-node-id]
 [:recur-transfers :each :recur-core-node-id]
 [:recur-transfers :each :argument-node-ids :each]
 [:source-map :each :core-node-id]]

template-top-level-reference-paths :=
[[:same-request-semantic-id]
 [:predecessor-authority-id]]

b51-noncore-generated-reference-paths :=
[[:destructuring-product-nodes :each :node-id]
 [:destructuring-product-nodes :each :predecessor-authority-id]
 [:destructuring-product-nodes :each :slot-id :optional]
 [:destructuring-product-nodes :each :extraction-id :optional]
 [:destructuring-product-nodes :each :runtime-check-id :optional]
 [:destructuring-product-nodes :each :publication-event-id :optional]
 [:destructuring-product-nodes :each :recur-slot-mapping-id :optional]
 [:destructuring-product-nodes :each :required-check-ids :each]
 [:binding-slots :each :slot-id]
 [:binding-slots :each :predecessor-authority-id]
 [:binding-slots :each :vector-node-extraction-ids :each]
 [:binding-extractions :each :extraction-id]
 [:binding-extractions :each :predecessor-authority-id]
 [:binding-extractions :each :slot-id]
 [:slot-extraction-transcript :transcript-id]
 [:slot-extraction-transcript :predecessor-authority-id]
 [:slot-extraction-transcript :same-request-semantic-id]
 [:slot-extraction-transcript :slot-ids :each]
 [:slot-extraction-transcript :extraction-ids :each]
 [:slot-extraction-transcript :runtime-check-ids :each]
 [:slot-extraction-transcript :publication-event-ids :each]
 [:slot-extraction-transcript :recur-slot-mapping-ids :each]
 [:runtime-checks :each :id]
 [:runtime-checks :each :predecessor-authority-id]
 [:runtime-checks :each :slot-id]
 [:runtime-checks :each :extraction-id]
 [:publication-events :each :id]
 [:publication-events :each :predecessor-authority-id]
 [:publication-events :each :slot-id :optional]
 [:publication-events :each :slot-ids :each]
 [:publication-events :each :projection-ids :each]
 [:publication-events :each :projection-id-vectors :each :each]
 [:publication-events :each :required-check-ids :each]
 [:publication-events :each :required-check-id-vectors :each :each]
 [:publication-events :each :product-node-ids :each]
 [:publication-events :each :product-node-id-vectors :each :each]
 [:recur-slot-mappings :each :mapping-id]
 [:recur-slot-mappings :each :predecessor-authority-id]
 [:recur-slot-mappings :each :commit-publication-id]
 [:recur-slot-mappings :each :slot-ids :each]
 [:recur-slot-mappings :each :extraction-id-vectors :each :each]
 [:recur-slot-mappings :each :required-check-id-vectors :each :each]
 [:recur-slot-mappings :each :projection-id-vectors :each :each]]

b51-ordinary-core-terminal-paths :=
[[:destructuring-product-nodes :each :owner-core-node-id]
 [:destructuring-product-nodes :each :recur-coordinate
  :recur-core-node-id :optional]
 [:destructuring-product-nodes :each :argument-node-ids :each]
 [:destructuring-product-nodes :each :child-node-ids :each]
 [:destructuring-product-nodes :each :evaluated-child-node-ids :each]
 [:binding-slots :each :owner-core-node-id]
 [:binding-slots :each :initializer-core-node-id]
 [:runtime-checks :each :recur-coordinate :recur-core-node-id :optional]
 [:publication-events :each :recur-coordinate :recur-core-node-id :optional]
 [:recur-slot-mappings :each :loop-core-node-id]
 [:recur-slot-mappings :each :recur-core-node-id]
 [:recur-slot-mappings :each :target-loop-core-node-id]
 [:recur-slot-mappings :each :argument-node-ids :each]]

ordinary-core-terminal-policy :=
{:legacy-v16-accepted
 {:representation :concrete-inherited-b47-digest
  :concrete b51-ordinary-core-terminal-paths
  :generated [] :target-purpose nil}
 :b51-vector-frontier-rejected
 {:representation :internal-digest-reference
  :concrete [] :generated b51-ordinary-core-terminal-paths
  :target-purpose :sh07-b51-core-node-id}
 :legacy-v16-accepted-after-no-form-waiver
 {:representation :internal-digest-reference
  :concrete [] :generated b51-ordinary-core-terminal-paths
  :target-purpose :sh07-b51-core-node-id}
 :b51-vector-frontier-rejected-after-no-form-waiver
 {:representation :internal-digest-reference
  :concrete [] :generated b51-ordinary-core-terminal-paths
  :target-purpose :sh07-b51-core-node-id}}

legacy-generated-reference-paths :=
[[:legacy-v16-equivalence :same-request-semantic-id]
 [:legacy-v16-equivalence :predecessor-outcome-semantic-id]
 [:legacy-v16-equivalence :v16-verification-semantic-id]]

legacy-inherited-artifact-reference-path :=
[:legacy-v16-equivalence :inherited-v16-semantic-artifact-id]

control-flow-reference-traversal :=
{:if {:exact-keys #{:core-node-id :kind :condition-node-id :branches
                    :branch-exclusivity :truthiness :result-policy}
      :reference-paths [[:core-node-id] [:condition-node-id]
                        [:branches :each :core-node-id]]
      :branch-exact-keys #{:role :predicate :core-node-id}}
 :do {:exact-keys #{:core-node-id :kind :ordered-child-node-ids
                    :evaluation-order :result-policy}
      :reference-paths [[:core-node-id] [:ordered-child-node-ids :each]
                        [:result-policy :core-node-id]]
      :result-policy-exact-keys #{:kind :core-node-id}}}

rejected-branch-category-reference-paths :=
[[:lexical-bindings :each :slot-id]
 [:loop-bindings :each :slot-id]
 [:recur-targets :each :slot-ids :target-kind=:loop :each]]

tier3-product-reference-paths-by-outcome :=
{:legacy-v16-accepted
 {:concrete (concat inherited-product-reference-paths
                    b51-ordinary-core-terminal-paths
                    [legacy-inherited-artifact-reference-path])
  :generated (concat b51-noncore-generated-reference-paths
                     legacy-generated-reference-paths)}
 :b51-vector-frontier-rejected
 {:concrete []
  :generated (concat inherited-product-reference-paths
                     rejected-branch-category-reference-paths
                     b51-noncore-generated-reference-paths
                     b51-ordinary-core-terminal-paths)}
 :legacy-v16-accepted-after-no-form-waiver
 {:concrete []
  :generated (concat inherited-product-reference-paths
                     rejected-branch-category-reference-paths
                     b51-noncore-generated-reference-paths
                     b51-ordinary-core-terminal-paths)}
 :b51-vector-frontier-rejected-after-no-form-waiver
 {:concrete []
  :generated (concat inherited-product-reference-paths
                     rejected-branch-category-reference-paths
                     b51-noncore-generated-reference-paths
                     b51-ordinary-core-terminal-paths)}}

preimage-reference-path-registry :=
{:sh07-b51-implementation-semantic-id []
 :sh07-b51-contract-semantic-id []
 :sh07-b51-same-request-semantic-id []
 :sh07-b51-v16-verification-semantic-id []
 :sh07-b51-predecessor-outcome-semantic-id
 [[:implementation-semantic-id] [:contract-semantic-id]
  [:same-request-semantic-id]
  [:outcome-tagged
   {:legacy-v16-accepted [[:v16-verification-semantic-id]]
    :b51-vector-frontier-rejected []
    :legacy-v16-accepted-after-no-form-waiver []
    :b51-vector-frontier-rejected-after-no-form-waiver []}]]
 :sh07-b51-predecessor-authority-id
 [[:implementation-semantic-id] [:contract-semantic-id]
  [:same-request-semantic-id] [:outcome-semantic-id]]
 :sh07-b51-core-node-id
 [[:predecessor-authority-id]
  [:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [[:children :each] [:evaluated-children :each]]
    :legacy-v16-accepted-after-no-form-waiver
    [[:children :each] [:evaluated-children :each]]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [[:children :each] [:evaluated-children :each]]}]]
 :sh07-b51-product-node-id [[:predecessor-authority-id]]
 :sh07-b51-binding-slot-id
 [[:predecessor-authority-id]
  [:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [[:owner-core-node-id] [:initializer-core-node-id]]
    :legacy-v16-accepted-after-no-form-waiver
    [[:owner-core-node-id] [:initializer-core-node-id]]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [[:owner-core-node-id] [:initializer-core-node-id]]}]]
 :sh07-b51-binding-extraction-id
 [[:predecessor-authority-id] [:slot-id]]
 :sh07-b51-runtime-check-id
 [[:predecessor-authority-id] [:slot-id] [:extraction-id]
  [:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [[:recur-coordinate :recur-core-node-id :optional]]
    :legacy-v16-accepted-after-no-form-waiver
    [[:recur-coordinate :recur-core-node-id :optional]]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [[:recur-coordinate :recur-core-node-id :optional]]}]]
 :sh07-b51-publication-event-id
 [[:predecessor-authority-id] [:slot-id :optional] [:slot-ids :each]
  [:projection-ids :each] [:projection-id-vectors :each :each]
  [:required-check-ids :each] [:required-check-id-vectors :each :each]
  [:product-node-ids :each] [:product-node-id-vectors :each :each]
  [:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [[:recur-coordinate :recur-core-node-id :optional]]
    :legacy-v16-accepted-after-no-form-waiver
    [[:recur-coordinate :recur-core-node-id :optional]]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [[:recur-coordinate :recur-core-node-id :optional]]}]]
 :sh07-b51-recur-slot-mapping-id
 [[:predecessor-authority-id] [:commit-publication-id] [:slot-ids :each]
  [:extraction-id-vectors :each :each]
  [:required-check-id-vectors :each :each]
  [:projection-id-vectors :each :each]
  [:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [[:loop-core-node-id] [:recur-core-node-id]
     [:target-loop-core-node-id] [:argument-node-ids :each]]
    :legacy-v16-accepted-after-no-form-waiver
    [[:loop-core-node-id] [:recur-core-node-id]
     [:target-loop-core-node-id] [:argument-node-ids :each]]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [[:loop-core-node-id] [:recur-core-node-id]
     [:target-loop-core-node-id] [:argument-node-ids :each]]}]]
 :sh07-b51-slot-extraction-transcript-id
 [[:predecessor-authority-id] [:same-request-semantic-id]
  [:slot-ids :each] [:extraction-ids :each] [:runtime-check-ids :each]
  [:publication-event-ids :each] [:recur-slot-mapping-ids :each]]
 :sh07-b51-core-identity-id
 [[:template-top-level template-top-level-reference-paths]
  [:products :outcome-tagged tier3-product-reference-paths-by-outcome]]
 :sh07-b51-predecessor-observation-id [[:semantic-identity-id]]
 :sh07-b51-provenance-binding-id [[:semantic-identity-id] [:observation-id]]
 :sh07-b51-independent-verifier-binding-id
 [[:semantic-identity-id] [:provenance-binding-id]]
 :sh07-b51-final-artifact-binding-id
 [[:semantic-identity-id] [:provenance-binding-id]
 [:independent-verifier-binding-id]]}
```

The bare vectors above are only the finite structural path component. Before
use, each active path is paired with exactly one target purpose by this closed
typed registry (repeated purposes align positionally with the printed path
vector; tagged/group paths expand before alignment):

```text
preimage-reference-target-purpose-registry :=
{:sh07-b51-implementation-semantic-id []
 :sh07-b51-contract-semantic-id []
 :sh07-b51-same-request-semantic-id []
 :sh07-b51-v16-verification-semantic-id []
 :sh07-b51-predecessor-outcome-semantic-id
 [:sh07-b51-implementation-semantic-id
  :sh07-b51-contract-semantic-id
  :sh07-b51-same-request-semantic-id
  {:outcome-tagged
   {:legacy-v16-accepted :sh07-b51-v16-verification-semantic-id
    :b51-vector-frontier-rejected nil
    :legacy-v16-accepted-after-no-form-waiver nil
    :b51-vector-frontier-rejected-after-no-form-waiver nil}}]
 :sh07-b51-predecessor-authority-id
 [:sh07-b51-implementation-semantic-id
  :sh07-b51-contract-semantic-id
  :sh07-b51-same-request-semantic-id
  :sh07-b51-predecessor-outcome-semantic-id]
 :sh07-b51-core-node-id
 [:sh07-b51-predecessor-authority-id
  {:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [:sh07-b51-core-node-id :sh07-b51-core-node-id]
    :legacy-v16-accepted-after-no-form-waiver
    [:sh07-b51-core-node-id :sh07-b51-core-node-id]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [:sh07-b51-core-node-id :sh07-b51-core-node-id]}}]
 :sh07-b51-product-node-id [:sh07-b51-predecessor-authority-id]
 :sh07-b51-binding-slot-id
 [:sh07-b51-predecessor-authority-id
  {:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [:sh07-b51-core-node-id :sh07-b51-core-node-id]
    :legacy-v16-accepted-after-no-form-waiver
    [:sh07-b51-core-node-id :sh07-b51-core-node-id]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [:sh07-b51-core-node-id :sh07-b51-core-node-id]}}]
 :sh07-b51-binding-extraction-id
 [:sh07-b51-predecessor-authority-id :sh07-b51-binding-slot-id]
 :sh07-b51-runtime-check-id
 [:sh07-b51-predecessor-authority-id :sh07-b51-binding-slot-id
  :sh07-b51-binding-extraction-id
  {:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected [:sh07-b51-core-node-id]
    :legacy-v16-accepted-after-no-form-waiver [:sh07-b51-core-node-id]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [:sh07-b51-core-node-id]}}]
 :sh07-b51-publication-event-id
 [:sh07-b51-predecessor-authority-id
  :sh07-b51-binding-slot-id :sh07-b51-binding-slot-id
  :sh07-b51-product-node-id :sh07-b51-product-node-id
  :sh07-b51-runtime-check-id :sh07-b51-runtime-check-id
  :sh07-b51-product-node-id :sh07-b51-product-node-id
  {:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected [:sh07-b51-core-node-id]
    :legacy-v16-accepted-after-no-form-waiver [:sh07-b51-core-node-id]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [:sh07-b51-core-node-id]}}]
 :sh07-b51-recur-slot-mapping-id
 [:sh07-b51-predecessor-authority-id
  :sh07-b51-publication-event-id :sh07-b51-binding-slot-id
  :sh07-b51-binding-extraction-id :sh07-b51-runtime-check-id
  :sh07-b51-product-node-id
  {:outcome-tagged
   {:legacy-v16-accepted []
    :b51-vector-frontier-rejected
    [:sh07-b51-core-node-id :sh07-b51-core-node-id
     :sh07-b51-core-node-id :sh07-b51-core-node-id]
    :legacy-v16-accepted-after-no-form-waiver
    [:sh07-b51-core-node-id :sh07-b51-core-node-id
     :sh07-b51-core-node-id :sh07-b51-core-node-id]
    :b51-vector-frontier-rejected-after-no-form-waiver
    [:sh07-b51-core-node-id :sh07-b51-core-node-id
     :sh07-b51-core-node-id :sh07-b51-core-node-id]}}]
 :sh07-b51-slot-extraction-transcript-id
 [:sh07-b51-predecessor-authority-id
  :sh07-b51-same-request-semantic-id :sh07-b51-binding-slot-id
  :sh07-b51-binding-extraction-id :sh07-b51-runtime-check-id
  :sh07-b51-publication-event-id :sh07-b51-recur-slot-mapping-id]
 :sh07-b51-core-identity-id
 {:template-top-level
  {[:same-request-semantic-id] :sh07-b51-same-request-semantic-id
   [:predecessor-authority-id] :sh07-b51-predecessor-authority-id}
  :products exact-outcome-selected-product-path-to-purpose-map}
 :sh07-b51-predecessor-observation-id
 [:sh07-b51-core-identity-id]
 :sh07-b51-provenance-binding-id
 [:sh07-b51-core-identity-id :sh07-b51-predecessor-observation-id]
 :sh07-b51-independent-verifier-binding-id
 [:sh07-b51-core-identity-id :sh07-b51-provenance-binding-id]
 :sh07-b51-final-artifact-binding-id
 [:sh07-b51-core-identity-id :sh07-b51-provenance-binding-id
  :sh07-b51-independent-verifier-binding-id]}

exact-outcome-selected-product-path-to-purpose-map :=
the exact expanded outcome path map above, with each generated terminal typed
by its closed record family. On `:legacy-v16-accepted`, every path in
`b51-ordinary-core-terminal-paths` is concrete and has no target purpose. On
each other outcome those same paths are generated and target
`:sh07-b51-core-node-id`. Destructuring product node ->
:sh07-b51-product-node-id; slot -> :sh07-b51-binding-slot-id; extraction ->
:sh07-b51-binding-extraction-id; runtime check ->
:sh07-b51-runtime-check-id; publication ->
:sh07-b51-publication-event-id; recur mapping ->
:sh07-b51-recur-slot-mapping-id; transcript ->
:sh07-b51-slot-extraction-transcript-id; same-request/outcome/v16-verification
and authority terminals -> their identically named catalog purposes. The
legacy inherited artifact path is concrete and has no success-request target.
No other terminal field is a controlled reference.
```

The complete static path-descriptor census, after grouped, control-flow,
target-kind, legacy, and ordinary-core outcome selectors are expanded while
`:each` and `:optional` remain their exact bounded traversal tokens, is:

```text
outcome-controlled-reference-census :=
{:legacy-v16-accepted
 {:controlled-request-paths 94
  :non-tier3-paths 46
  :tier3-paths 48
  :tier3-top-level-paths 2
  :tier3-generated-product-paths 46
  :dormant-zero-cardinality-core-authority-descriptors 1
  :ordinary-core-controlled-paths 0
  :ordinary-core-concrete-paths b51-ordinary-core-terminal-paths}
 :b51-vector-frontier-rejected
 {:controlled-request-paths 174
  :non-tier3-paths 55 :tier3-paths 119
  :tier3-top-level-paths 2 :tier3-generated-product-paths 117
  :ordinary-core-controlled-paths b51-ordinary-core-terminal-paths
  :ordinary-core-concrete-paths []}
 :legacy-v16-accepted-after-no-form-waiver
 {:controlled-request-paths 174
  :non-tier3-paths 55 :tier3-paths 119
  :tier3-top-level-paths 2 :tier3-generated-product-paths 117
  :ordinary-core-controlled-paths b51-ordinary-core-terminal-paths
  :ordinary-core-concrete-paths []}
 :b51-vector-frontier-rejected-after-no-form-waiver
 {:controlled-request-paths 174
  :non-tier3-paths 55 :tier3-paths 119
  :tier3-top-level-paths 2 :tier3-generated-product-paths 117
  :ordinary-core-controlled-paths b51-ordinary-core-terminal-paths
  :ordinary-core-concrete-paths []}}
```

The static accepted total is exactly `46 + (2 + 46) = 94`. Its non-Tier-3
count includes the unconditional authority descriptor belonging to the
dormant zero-cardinality core-node request schema. The realized active
accepted traversal would contain 93 paths after that dormant descriptor is
removed, but realized traversal is not the metric selected by this contract;
all acceptance and mutation evidence uses the static literal registry count
94. Each non-v16 total is exactly `55 + (2 + 117) = 174`.

This census is separate from the 56 purpose-dependency edges: one purpose edge
can govern many structural paths. Root 3 recomputes the selected outcome row,
requires the exact 94-or-174 static path census, pairs every controlled path with its
exact target purpose, and never looks up a concrete accepted ordinary-core
field. Producer/verifier fixtures cover all four positive rows. Mutations flip
one accepted concrete core digest to an internal reference, one non-v16 core
reference to a concrete digest, swap an outcome tag, add/remove a core-node
request edge, or preserve the field while changing its target purpose; every
mutation fails before hashing.

The typed registry is a map from each exact expanded numeric-capable path to
one exact target purpose, not a heuristic based on digest shape or field name.
Expansion must be duplicate-free and total. A missing type, a second target
purpose, a purpose substitution, or a controlled reference outside this map is
`C6-VERIFY` before resolution. The static closed expansion contains 56
purpose edges. The producer and independent verifier recompute all 56, require
zero forward/equal-coordinate edges and zero cycles, and separately verify
that the only same-batch edges are strict core-node postorder child edges.

The top-level Tier-3 entry expands without a `:products` prefix. The single
product entry selects exactly one outcome row, concatenates its path vectors
in the printed order, verifies the selected final vector is duplicate-free,
then prefixes every path with `[:products]`. The three rejected-only additions
are disjoint from the inherited-common vector; duplicate rejection applies
only to an accidental schema overlap, never as a substitute for selecting the
right row. These are notation for a finite path vector, not keys present in
the preimage. The target selector reads exact
`:target-kind`: a function target traverses only owner-core, while a loop
target traverses owner-core plus every slot id; unknown/mismatched fields
reject. The legacy registry is selected by exact predecessor outcome: its
three generated and one inherited path exist only for
`:legacy-v16-accepted`; the nil legacy field under each other outcome has zero
descendant paths. Positive fixtures traverse every expected path under each of
the four outcomes. Mutation fixtures inject one omitted, extra, wrong-tag,
wrong-target-kind, concrete-as-generated, and generated-as-concrete path into
each outcome and require rejection. All other purpose rows spell their
literal paths directly. In particular, slot and extraction preimages contain
no product-node-id path, and product-node seeds contain only authority among
ancestor digests.
The control-flow selector always visits the flat `:control-flow` vector, reads
each record's literal `:kind`, requires the corresponding exact key set above,
and traverses only that variant's paths. An if record with any do-only field,
a do record with any if-only field, a wrong branch/result-policy shape, or an
unknown tag is `C6-VERIFY`; there are no synthetic `:if`/`:do` container keys.
Tier-3 mutation probes independently swap every call-edge caller/callee field,
keyword node/map field, control-flow tag, branch vector, ordered child, and
result-policy reference and require identity/verifier rejection.

The outcome-tagged product registry exhausts
all digest-reference positions in the canonical template. On legacy-v16
accepted input, its concrete vector validates inherited digests rather than
generated references. Digest-request preimages separately use exactly the
typed ancestor positions in `preimage-reference-path-registry` plus
`preimage-reference-target-purpose-registry`; therefore the old claim
that the template registry was the only reference location is not made.
Accepted template roots and inherited product paths contain concrete verified
v16 digests. The accepted branch's B51 supplemental records also carry their
ordinary owner/initializer/child/recur core terminals as those concrete
verified B47 digests, while their B51 slot/extraction/check/publication/product
ids remain generated. Each non-v16 branch uses exact internal references for
every generated ordinary-core and B51 path. Resolved cores and outputs require concrete digest ids
at every path and globally reject internal references. Copied upstream
binding/macro/module fields and physical carrier P contain none.

### Resolved core schema

`:gravity/sh07-b51-resolved-core-v18` is the same closed product set after all
digest references resolve, with exactly these additional identity fields and
no request fields:

```text
{:artifact :gravity/sh07-b51-resolved-core-v18
 :schema-version 18
 :predecessor-outcome-kind :legacy-v16-accepted | :b51-vector-frontier-rejected
                           | :legacy-v16-accepted-after-no-form-waiver
                           | :b51-vector-frontier-rejected-after-no-form-waiver
 :same-request-semantic-id digest-id
 :predecessor-authority-id digest-id
 :module module-record
 :lineage lineage-record
 :projection-binding digest-id
 :semantic-identity-id digest-id
 :fragment-manifest vector
 :fragment-coverage fragment-coverage-record
 :module-assembly-manifest module-assembly-manifest-record
 :root-core-node-ids vector-of-digest-id
 :definitions vector
 :nodes vector
 :evaluation-order vector
 :control-flow vector
 :reference-uses vector
 :var-references vector
 :mutations vector
 :error-transfers vector
 :error-handlers vector
 :match-branch-records vector
 :match-decision-skeletons vector
 :match-pattern-records vector
 :calls vector
 :function-records vector
 :call-edges vector
 :recursion-components vector
 :keyword-lookups vector
 :lexical-bindings vector
 :loop-bindings vector
 :recur-targets vector
 :recur-transfers vector
 :source-map vector
 :binding-table vector
 :declared-alias-table vector
 :resolution-table vector
 :macro-expansion-trace vector
 :macro-origin-traces vector
 :macro-origin-expectation map
 :pending-fact-families vector
 :destructuring-product-nodes vector
 :binding-slots vector
 :binding-extractions vector
 :slot-extraction-transcript map
 :runtime-checks vector
 :publication-events vector
 :recur-slot-mappings vector
 :legacy-v16-equivalence record | nil
:diagnostics []}
```

The same branch type applies recursively to fragment-manifest
`:root-node-ids` and every inherited path in the typed reference registry:
accepted inherited values are already concrete verified v16 digests, while
rejected generated values are controlled references until resolution. No
template vector may mix the two representations.

The resolved core may not contain its own request/result, physical paths,
complete verifier report, or final artifact binding. Its identity preimage is
Tier 3 and excludes Tier 4-6 data.

### Canonical output artifact schema

The literal output kind is `:gravity/sh07-b51-canonical-core-artifact-v18`.
The output artifact is a closed map with exactly:

```text
{:artifact :gravity/sh07-b51-canonical-core-artifact-v18
 :schema-version 18
 :status :accepted
 :predecessor-outcome-kind :legacy-v16-accepted | :b51-vector-frontier-rejected
                           | :legacy-v16-accepted-after-no-form-waiver
                           | :b51-vector-frontier-rejected-after-no-form-waiver
 :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
 :task "SH-07-B51"
 :semantic-identity-id digest-id
 :provenance-binding-id digest-id
 :independent-verifier-binding-id digest-id
 :final-artifact-binding-id digest-id
 :same-request-semantic-id digest-id
 :predecessor-authority-id digest-id
 :module module-record
 :lineage lineage-record
 :projection-binding digest-id
 :fragment-manifest vector
 :fragment-coverage fragment-coverage-record
 :module-assembly-manifest module-assembly-manifest-record
 :root-core-node-ids vector-of-digest-id
 :definitions vector
 :nodes vector
 :evaluation-order vector
 :control-flow vector
 :reference-uses vector
 :var-references vector
 :mutations vector
 :error-transfers vector
 :error-handlers vector
 :match-branch-records vector
 :match-decision-skeletons vector
 :match-pattern-records vector
 :calls vector
 :function-records vector
 :call-edges vector
 :recursion-components vector
 :keyword-lookups vector
 :lexical-bindings vector
 :loop-bindings vector
 :recur-targets vector
 :recur-transfers vector
 :source-map vector
 :binding-table vector
 :declared-alias-table vector
 :resolution-table vector
 :macro-expansion-trace vector
 :macro-origin-traces vector
 :macro-origin-expectation map
 :pending-fact-families vector
 :destructuring-product-nodes vector
 :binding-slots vector
 :binding-extractions vector
 :slot-extraction-transcript map
 :runtime-checks vector
 :publication-events vector
 :recur-slot-mappings vector
 :legacy-v16-equivalence record | nil
 :diagnostics []}
```

The optional outer wrapper is also closed:

```text
{:kind :gravity/sh07-b51-core-artifact-v18
 :schema-version 18
 :status :accepted
 :slice :SH-07
 :task "SH-07-B51"
 :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
 :artifact-id digest-id
 :final-artifact-binding-id digest-id
 :canonical-core-artifact exact-output-artifact
 :provenance {:binding-id digest-id}
 :pass {:name :c6-gravity-core-lowering-b51-v18
        :input :authenticated-sh06-resolution
        :output :gravity/sh07-b51-canonical-core-artifact-v18}
 :execution-boundary {:owner :master-coordinator
                      :invocation :mechanical
                      :host-semantic-construction :forbidden
                      :digest-resolution :opaque-requests-only
                      :physical-provenance :recorded}
 :downstream-fact-statuses {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
 :pending-lowering-families vector
 :sh07-complete? false
 :self-hosted? false}
```

The Tier-6 final binding contains the literal output kind, resolved semantic
identity, Tier-4 provenance id, and Tier-5 verifier id only. It is terminal.
The wrapper's `:artifact-id` is exactly its `:final-artifact-binding-id`; the
canonical output's `:final-artifact-binding-id` is the same Tier-6 id.
The structural predecessor route has no outer wrapper: its predecessor wrapper
kind and id are always nil. Entry point 7 produces one canonical map with final
binding `F`; the structural V18 route returns that map with an absent wrapper,
while H may embed the exact map in the optional V18 wrapper. If H emits that
wrapper, its `:artifact-id` and `:final-artifact-binding-id` both equal `F`,
and its `:provenance :binding-id` equals the canonical provenance binding id.
Wrapper presence never changes `F` or any semantic product.

## B51 pattern, slot, extraction, and execution semantics

The admitted slot grammar preserves the frozen B47 simple-symbol domain and
adds vector patterns:

```text
slot-pattern := legacy-binding-symbol | vector-pattern
legacy-binding-symbol := symbol
vector-pattern := "[" vector-element* "]"
vector-element := vector-binding-symbol | _ | vector-pattern
vector-binding-symbol := symbol excluding _ and &
unsupported-vector-marker := &
```

`legacy-binding-symbol` is the existing B47/SH-06 simple-symbol slot and is
preserved byte-for-byte and behavior-for-behavior, including the ordinary
simple symbols `_` and `&`. Only descendants of `vector-pattern` reinterpret
`_` as a zero-binding wildcard. A vector-contained `&` is recognized only so
that V18 can reject the unsupported rest marker before extraction; it is not a
grammar member. Other vector leaves are authenticated SH-06 lexical bindings; a repeated admitted
name is a V18 `L7-DUP-BINDING` error. Vectors can be recursively nested or
empty. Maps, lists, sets, records, constructors, aliases, guards, defaults,
rest outside this explicit vector marker, variable-width vectors, parameter
destructuring, and general `match` expansion remain deferred.

### Authentic owner, enclosing fragment, and top-level root

The owner of every authenticated enclosing `E` is selected from the
authenticated fragment manifest, forms, roots, resolution/binding tables,
macro trace, and per-form origins. `E` must be a list whose child 0 is an
authenticated `let` or `loop` operator reference. That operator has exactly
one projected resolution `K` and binding `B`; K and B join by binding id and
upstream id, and B is exactly core/public/`gravity.core` with resolution order
`:profile-allowed-core-binding`. A host-recognized symbol without that join is
not an owner.

This applies to every such enclosing `E`, not only to a top-level form. A
source path, name, or host traversal order is never sufficient. For each `E`,
walk a unique reciprocal `:parent-form-id` chain, bounded at 256 links: each
parent must name the child in its exact `:child-form-ids`, every visited id is
new, and no form has two parents. Require exactly one terminal root `R` with
`:parent-form-id nil`; R occurs exactly once in authenticated
`:top-level-form-ids`. Select exactly one fragment `F` satisfying all of:

```text
F.fragment-id       = R.form-id
F.content-id        = R.form-id
F.root-form-ids     = [R.form-id]
F.root-node-ids     = [R.form-id]
E.form-id            in F.form-ids
```

`F`'s authenticated `:ordinal` is the sole value copied into
`:fragment-ordinal`; no separately invented fragment ordinal is admitted. A
second matching fragment, missing root/content id, or root/form order mismatch
is `C6-VERIFY`. The root may be any authenticated top-level form;
source-defined `(def x (let ...))` roots therefore remain in the admitted B51
boundary.

Let `Tmatches` be the authenticated macro-trace entries whose
`:output-def-syntax-id` equals `R.syntax-id`. Before selecting a branch,
Gravity authenticates `source-def?(R)`: R is a list with exactly three
children; child 0 is the symbol `def` with exactly one projected
core/public/`gravity.core`/`:profile-allowed-core-binding` K-to-B-to-upstream
catalog join; child 1 is the unique declared-name form and joins exactly one
F-local namespace binding by definition syntax id, name, and request module;
and E is a strict descendant of child 2. No printed `def` symbol without those
joins is a source definition.

The owner branch is exactly one of these three, in this precedence:

1. **Expanded defn.** `Tmatches` contains exactly one T. Require
   `source-def?(R)`, require `R`'s exact singleton `:introduced-def` origin,
   and require child 2 to be an authenticated `fn` with
`T.introduced-fn-syntax-id = child[2].syntax-id`, and `E` to be a strict
descendant of that `fn`. Filter `R`'s own `:generated-origin` vector for the
exact singleton `:introduced-def` macro-expansion entry matching `T`'s
generated-def origin, input syntax id, and role; filter `child[2]`'s own
`:generated-origin` vector for the exact singleton `:introduced-fn` entry
matching `T`'s generated-fn origin, input syntax id, and role. There is no
   separate or inferred origin table. Set def form/syntax to R, defn-input to
   T's authenticated input syntax, and fn form/syntax to child 2.
2. **Source def.** `Tmatches` and R's introduced-def selection are empty and
   `source-def?(R)` is true. Set def form/syntax to R and set defn-input and fn
   form/syntax nil, even when child 2 happens to be an `fn`.
3. **Non-definition root.** `Tmatches` and R's introduced-def selection are
   empty and `source-def?(R)` is false. Set all five def/defn/fn fields nil.

More than one T, any T/origin disagreement, or introduced-def origin without T
is `C6-ORIGIN`. A selected definition branch with the wrong authenticated
shape is `C6-CORE-SHAPE`; it never falls back to the next branch.

Every slot, extraction, product node, runtime check, publication, and recur
mapping uses the exact closed coordinate appropriate to that record. The
three coordinate schemas are distinct; fields are never flattened or copied
between them:

```text
digest-id := string matching exactly ^sha256:[0-9a-f]{64}$
id := digest-id
vector-of-id := vector-of-digest-id
vector-of-vector-of-id := vector-of-vector-of-digest-id
owner-coordinate-record := owner-coordinate
slot-coordinate-record := slot-coordinate
recur-coordinate-record := recur-coordinate
```

These are literal aliases only. `id` never includes an ordinal, syntax object,
internal reference, opaque host id, or nil. Every controlled-reference
position is separately enumerated in the two registries above; aliases do not
classify a value as resolvable.

```text
owner-coordinate :=
{:fragment-id id
 :fragment-ordinal nonnegative-integer
 :root-form-id id
 :root-syntax-id id
 :owning-definition-form-id id | nil
 :owning-definition-syntax-id id | nil
 :def-form-id id | nil
 :def-syntax-id id | nil
 :defn-input-syntax-id id | nil
 :fn-form-id id | nil
 :fn-syntax-id id | nil
 :enclosing-form-id id
 :enclosing-syntax-id id}

slot-coordinate :=
{:owner-coordinate owner-coordinate-record
 :slot-ordinal nonnegative-integer
 :pattern-form-id id
 :pattern-syntax-id id
 :initializer-form-id id
 :initializer-syntax-id id}

recur-coordinate :=
{:recur-form-id id
 :recur-syntax-id id
 :recur-core-node-id digest-id}
```

The canonical owning-definition pair is exactly
`[owner-coordinate.def-form-id owner-coordinate.def-syntax-id]`. It equals
`[R.form-id R.syntax-id]` on expanded-defn and source-def branches and
`[nil nil]` on the non-definition branch. The two owning-definition fields
are mutually nonnil or mutually nil and must equal that pair byte-for-byte;
no other authenticated id is eligible.

The aggregate output contains coordinates only within per-item products; it
has no singular owner, slot, recur, or top-level coordinate. Repeated
fragment/root/enclosing fields in a full record must equal its embedded
owner-coordinate, and repeated slot/recur fields must equal the corresponding
slot-coordinate/recur-coordinate byte-for-byte.

For both definition branches, `root-form-id = def-form-id =
owning-definition-form-id` and `root-syntax-id = def-syntax-id =
owning-definition-syntax-id`. Only the expanded-defn branch has nonnil
defn-input/fn fields. A non-definition root has all seven definition fields
nil. Coordinates are joined to the exact
authenticated forms, fragment manifest, enclosing parent, optional macro
trace, and source origins; they are not reconstructed from a symbol or
filename. Any missing, duplicate, cross-fragment, non-ancestor, or reordered
coordinate, or a field outside its exact three-branch matrix, fails closed.

Every binding-slot, binding-extraction, and product-node record repeats
`:owning-definition-form-id` and `:owning-definition-syntax-id`; both values
must equal its embedded owner-coordinate pair byte-for-byte. All such records
for one owner therefore carry the same pair. A slot/extraction product's owner
must also equal `slot-coordinate.owner-coordinate`; a recur product's owner
must equal its selected enclosing owner coordinate. Extraction-leaf
`:definition-form-id`/`:definition-syntax-id` describe the leaf declaration
and are explicitly unrelated to the owning-definition pair. These upstream
authenticated ids are concrete, never controlled digest references. Pair
nilness, branch substitution, cross-owner copying, and arbitrary authenticated
id mutations are rejected independently.

### Ordinary v18 core nodes and branch-tagged inheritance

The ordinary executable `:nodes` category remains the authenticated B47 node
family on the legacy-v16-accepted branch. Every non-v16 outcome starts with an empty v18
node state and lowers the entire admitted request; it never consumes a B47
rejected partial state. The rejected branch emits one v18 ordinary node for
each executable core operation in fragment/root order, expressions
left-to-right, with ordinary nodes in postorder. A vector `let`/`loop` emits
one ordinary owner node after its initializer and body; a `recur` emits one
ordinary recur node. Destructuring-specific records are supplemental and are
placed in `:destructuring-product-nodes`, not substituted for ordinary nodes.

The exact v18 ordinary node schema is the B47 `sh07-add-node` key set with
literal v18 identity fields. The named nested records are the closed B47
records pinned by the predecessor contract; no rest map or physical source
field is admitted:

```text
semantic-span-record :=
{:byte-start nonnegative-integer :byte-end nonnegative-integer
 :line-start nonnegative-integer :column-start nonnegative-integer
 :line-end nonnegative-integer :column-end nonnegative-integer
 :scalar-start nonnegative-integer :scalar-end nonnegative-integer}

metadata-semantic-value :=
nil | boolean | bounded-integer | exact-bounded-ratio | exact-BigDecimal
| Unicode-character | bounded-string | keyword | symbol
| bounded-vector-of-metadata-semantic-value
| bounded-list-of-metadata-semantic-value
| bounded-set-of-metadata-semantic-value
| bounded-map-of-metadata-semantic-value-to-metadata-semantic-value

b47-authenticated-metadata-map :=
bounded-map-with-at-most-64-entries,
outer keys keyword | symbol,
values metadata-semantic-value

core-node-source-record :=
{:syntax-id digest-id :form-id digest-id
 :semantic-span semantic-span-record
 :origin-chain source-origin-chain
 :generated-origin source-generated-origin}

core-node-evaluation-order-entry :=
{:index nonnegative-integer :core-node-id digest-id}

core-node-evaluation-record :=
{:kind keyword :region keyword
 :owner-function-syntax-id digest-id | nil
 :order vector-of-core-node-evaluation-order-entry}

core-node-module-record :=
{:namespace symbol :profile :meta :target :jvm}

core-node-v18 :=
{:artifact :gravity/sh07-b51-core-node-v18
 :schema-version 18
 :node-id digest-id
 :core-form keyword
 :children vector-of-digest-id
 :attributes branch-tagged-core-node-attributes-record
 :source core-node-source-record
 :binding-context digest-id | nil
 :generated? boolean
 :resolved-binding-ids vector-of-digest-id
 :evaluation core-node-evaluation-record
 :module core-node-module-record
 :metadata b47-authenticated-metadata-map
 :preserved-declarations
 {:effects vector-of-keyword :capabilities vector-of-keyword :safety :safe}
 :pending-fact-families vector-of-keyword
 :lowering-rule-version :sh07-b51-v18}

core-node-id-preimage-v18 :=
{:domain :gravity/sh07-b51-core-node-id-preimage-v18
 :schema-version 18
 :predecessor-authority-id digest-id
 :core-form keyword
 :children vector-of-digest-id
 :attributes branch-tagged-core-node-attributes-record
 :source core-node-source-record
 :resolved-binding-ids vector-of-digest-id
 :evaluation-kind keyword
 :evaluation-region keyword
 :evaluation-owner-function-syntax-id digest-id | nil
 :evaluated-children vector-of-digest-id
 :namespace symbol :profile :meta :target :jvm
 :metadata b47-authenticated-metadata-map
 :binding-context digest-id | nil
 :generated? boolean
 :preserved-declarations
 {:effects vector-of-keyword :capabilities vector-of-keyword :safety :safe}
 :pending-fact-families vector-of-keyword}
```

The recursive algebra above is the declared SH-04 reader algebra: nested map
keys may be any canonical semantic value, but only the outer metadata map has
the keyword/symbol key restriction and 64-entry bound. Aggregate canonical
depth/width/node/scalar/integer/ratio/decimal bounds apply, and duplicate
canonical map keys reject. However, authoritative fresh SH-06-to-B47 M
currently makes only `{}` reachable here: nonempty host `IObj` metadata fails
SH-06 `C5-UNRESOLVED` / `:carrier-metadata`, and SH-04 strips host metadata.
Therefore v18 parity copies the exact freshly verified predecessor metadata,
currently `{}`; nonempty metadata support is a separate governed producer
repair/extension and is not claimed here.

Controlled-reference discovery and unresolved-reference rejection visit only
the finite structural paths in the registries above and never descend into
`:metadata`. A digest-reference-shaped metadata map is opaque authenticated
by-value data. The exact M path-neutral normalizer applies before the by-value
comparison, but no resolver substitution, reference-shape rejection, or host
interpretation occurs within metadata. Function/host objects, physical paths
outside the normalizer's fields, or values outside the declared SH-04 algebra
remain forbidden.

The other inherited fields rendered as `vector` or `map` in the aggregate
template are not generic aliases: each is validated by its exact named M
registry entry. In particular `:macro-origin-expectation` and
`:slot-extraction-transcript` use their closed named schemas, module/lineage/
coverage/assembly maps are enumerated above, and diagnostics is literal `[]`.
No other inherited generic-map escape hatch remains.

`branch-tagged-core-node-attributes-record` selects exactly one validator by
branch and `:core-form`. For rejected-frontier `let`, `loop`, and `recur`, it
selects these exact tagged variants; all other core forms select their exact
pinned B47 attribute validator:

```text
rejected-let-attributes-v18 :=
{:binding-vector-form-id digest-id
 :binding-vector-syntax-id digest-id
 :outer-scope-id digest-id
 :body-scope-id digest-id
 :binding-count nonnegative-integer
 :introduced-binding-count nonnegative-integer
 :slot-pattern-kinds vector-of-(:legacy-simple-symbol | :vector-pattern)
 :body-count positive-integer
 :initializer-child-indexes vector-of-nonnegative-integer
 :body-child-indexes vector-of-nonnegative-integer
 :evaluation-order :initializers-then-body-left-to-right
 :result-policy {:kind :last-body :child-index nonnegative-integer}}

rejected-loop-attributes-v18 :=
{:target-id digest-id
 :binding-vector-form-id digest-id
 :binding-vector-syntax-id digest-id
 :outer-scope-id digest-id
 :body-scope-id digest-id
 :binding-count nonnegative-integer
 :introduced-binding-count nonnegative-integer
 :slot-pattern-kinds vector-of-(:legacy-simple-symbol | :vector-pattern)
 :body-count positive-integer
 :initializer-child-indexes vector-of-nonnegative-integer
 :body-child-indexes vector-of-nonnegative-integer
 :evaluation-order :initializers-then-body-left-to-right
 :result-policy {:kind :last-body :child-index nonnegative-integer}}

rejected-recur-attributes-v18 :=
{:target-id digest-id
 :target-kind :loop | :function
 :arity nonnegative-integer
 :semantic-rule "L2-RECUR-TARGET"
 :type-compatibility :pending-sh08}
```

For let/loop, `:binding-count` is the slot count and equals initializer count;
`:introduced-binding-count` is the concatenated count of unique non-wildcard
leaf binding ids; `:slot-pattern-kinds` has one entry per slot. Children are
exactly slot initializers followed by the nonempty body, so initializer indexes
are `[0,binding-count)` and body indexes are
`[binding-count,binding-count+body-count)`. `:resolved-binding-ids` is the
slot-ordered concatenation of unique admitted leaf ids, not the slot count.
For recur, `:arity` and child count equal the target loop's slot count for a
loop target, never its leaf count; for a function target they equal the exact
B47 function target's flat binding-id arity. A rejected ordinary node request is emitted postorder before
any supplemental B51 product-node request in 0d; later core-node requests may
refer only to smaller global core-node ordinals.

On `:legacy-v16-accepted`, `:nodes` and every other inherited B47 product
are copied from the freshly verified concrete v16 result, preserving their
v16 ids; the v18 core-node request cardinality is zero and the legacy
equivalence record is non-nil. On every non-v16 outcome, the v18
ordinary node category is complete for the whole request, inherited v16 ids
and legacy equivalence are nil, and all generated references resolve through
the v18 request stream. Supplemental B51 metadata is permitted on both
branches but is excluded from the 37-key legacy projection. A template,
resolved core, or output carrying a mixed tag, mixed authority, regenerated
accepted node id, partial rejected node set, or wrong legacy-equivalence
state fails `C6-VERIFY`.

The two origin vectors used below are closed, path-neutral vectors of the
authenticated schema-15 origin entries. The entry shape is the exact
`sh07-origin-shape?` shape in `checked_core.gravity`; no span, source path,
checkout, or other physical field is admitted:

```text
origin-entry :=
{:origin-id digest-id
 :kind keyword
 :from-syntax-id digest-id
 :role keyword}

source-origin-chain := vector-of-origin-entry
source-generated-origin := vector-of-origin-entry
```

For the authenticated schema-15 form selected by the enclosing coordinate,
`:source-origin-chain` is exactly the form's `:origin-chain` and
`:source-generated-origin` is exactly the form's `:generated-origin`, each
preserving authenticated order. They are never concatenated. Every entry must
have exactly the four keys above and the two id fields must be authenticated
digest references; an absent vector is `[]`. For a product node,
`form` is the authenticated owner form identified by its owner form/syntax
ids, except that a recur-site product selects the authenticated recur form from
its recur coordinate; for an extraction it is the authenticated form identified
by that record's `:form-id`/`:syntax-id`. The same two selected vectors are copied into
the id seed and full record byte-for-byte. Any
selection mismatch, extra key, reordered entry, or physical field is
`C6-ORIGIN`.

Each authenticated enclosing `let`/`loop` form `E` contributes one slot per
pattern-initializer pair. Slots are source ordered within `E`; the exact
authenticated owner coordinate identifies which `E` owns each slot. The
initializer runs exactly once with only earlier slots visible. Vector nodes
are checked preorder with exact authenticated width;
terminal projections use authenticated paths without reevaluation. All leaves
publish simultaneously only after every check passes. A non-vector or wrong
width value emits `L7-PATTERN-TYPE`; it publishes no current slot and evaluates
no later initializer/body. Empty vectors remain explicit vector-node records.

All module-global ordinals are projections of one reconstructible total
occurrence order. Gravity and the independent verifier build the same ordered
stream without consulting emitted ids:

```text
global-occurrence-key :=
{:record-kind :ordinary-core-node | :supplemental-product | :binding-slot
              | :binding-extraction | :runtime-check | :publication-event
              | :recur-slot-mapping | :module-transcript
 :root-index nonnegative-integer | :na
 :fragment-ordinal nonnegative-integer | :na
 :form-preorder-index nonnegative-integer | :na
 :enclosing-occurrence-index nonnegative-integer | :na
 :core-postorder-index nonnegative-integer | :na
 :record-kind-rank nonnegative-integer
 :event-kind-rank nonnegative-integer | :na
 :event-form-preorder-index nonnegative-integer | :na
 :slot-ordinal nonnegative-integer | :na
 :pattern-preorder-index nonnegative-integer | :na
 :product-kind-rank nonnegative-integer | :na
 :local-use-index nonnegative-integer | :na
 :traversal-emission-index nonnegative-integer}

event-kind-rank := {:initializer 0 :recur 1}
record-kind-rank :=
{:ordinary-core-node 0 :supplemental-product 1 :binding-slot 2
 :binding-extraction 3 :runtime-check 4 :publication-event 5
 :recur-slot-mapping 6 :module-transcript 7}
product-kind-rank :=
{:slot-initializer 0 :vector-check 1 :terminal-projection 2
 :slot-publication 3 :recur-argument 4 :recur-commit 5}

applicability :=
{:ordinary-core-node
 [:root-index :fragment-ordinal :form-preorder-index
  :core-postorder-index :record-kind-rank]
 :supplemental-product
 {:slot-initializer
  [:root-index :fragment-ordinal :form-preorder-index
   :enclosing-occurrence-index :record-kind-rank :event-kind-rank
   :event-form-preorder-index :slot-ordinal :product-kind-rank
   :local-use-index]
  :vector-check
  [:root-index :fragment-ordinal :form-preorder-index
   :enclosing-occurrence-index :record-kind-rank :event-kind-rank
   :event-form-preorder-index :slot-ordinal :pattern-preorder-index
   :product-kind-rank :local-use-index]
  :terminal-projection
  [:root-index :fragment-ordinal :form-preorder-index
   :enclosing-occurrence-index :record-kind-rank :event-kind-rank
   :event-form-preorder-index :slot-ordinal :pattern-preorder-index
   :product-kind-rank :local-use-index]
  :slot-publication
  [:root-index :fragment-ordinal :form-preorder-index
   :enclosing-occurrence-index :record-kind-rank :event-kind-rank
   :event-form-preorder-index :slot-ordinal :product-kind-rank
   :local-use-index]
  :recur-argument
  [:root-index :fragment-ordinal :form-preorder-index
   :enclosing-occurrence-index :record-kind-rank :event-kind-rank
   :event-form-preorder-index :product-kind-rank :local-use-index]
  :recur-commit
  [:root-index :fragment-ordinal :form-preorder-index
   :enclosing-occurrence-index :record-kind-rank :event-kind-rank
   :event-form-preorder-index :product-kind-rank :local-use-index]}
 :binding-slot
 [:root-index :fragment-ordinal :form-preorder-index
  :enclosing-occurrence-index :record-kind-rank :slot-ordinal]
 :binding-extraction
 [:root-index :fragment-ordinal :form-preorder-index
  :enclosing-occurrence-index :record-kind-rank :slot-ordinal
  :pattern-preorder-index]
 :runtime-check
 [:root-index :fragment-ordinal :form-preorder-index
  :enclosing-occurrence-index :record-kind-rank :event-kind-rank
  :event-form-preorder-index :slot-ordinal :pattern-preorder-index
  :local-use-index]
 :publication-event
 [:root-index :fragment-ordinal :form-preorder-index
  :enclosing-occurrence-index :record-kind-rank :event-kind-rank
  :event-form-preorder-index :local-use-index]
 :recur-slot-mapping
 [:root-index :fragment-ordinal :form-preorder-index
  :enclosing-occurrence-index :record-kind-rank :event-kind-rank
  :event-form-preorder-index :local-use-index]
 :module-transcript [:record-kind-rank]}
```

Every key is present. Fields not listed for the record kind are the literal
sentinel `:na`; listed fields and the always-required
`:traversal-emission-index` are nonnegative integers. The index is derived,
not accepted from a producer counter: independently replay the recursive
lowering traversal in Q root order; for each expression recurse through
children left-to-right, emit each ordinary core node after all children, then
emit that source occurrence's supplemental/slot/extraction/use records in the
literal record/event/product rank orders. A recur event is visited at its
authenticated form position and its checks precede its one publication and
mapping. After all source occurrences, emit the single module transcript.
Number this replay stream densely from zero. Comparison uses only that index;
all remaining coordinate fields authenticate how the index was derived and
must match the applicability matrix. No host keyword or map ordering is used.
Supplemental applicability is selected first by `:record-kind`, then by the
literal `:product-kind`. The whole-recur `:recur-argument` and
`:recur-commit` variants always set both `:slot-ordinal` and
`:pattern-preorder-index` to `:na`; argument order is their dense
`:local-use-index`, while commit has local-use zero. A per-slot fact belongs in
the aligned mapping/check/projection records, not in those whole-recur tuples.
`root-index` is the
exact Q top-level-root order. For each root, fragments use
module-assembly order and authenticated `F.:ordinal`; forms use deterministic
preorder over each root and its `child-form-ids`, with each form required at
its unique index in `F.:form-ids`. Enclosing occurrences are let/loop forms in
that traversal. Initializer events precede all recur events of their owner;
recur events use the recur form's traversal index. Slots are source order,
pattern nodes preorder, and uses are initializer then recur-site order.
Ordinary rejected core nodes have a unique dense `:core-postorder-index`
assigned by left-to-right DFS; every child index is strictly less than its
parent, and both that index and the traversal-emission index preserve this
inequality even when child and parent have different form-preorder indexes.
They are ranked before supplemental products at the same source occurrence. The single
module transcript uses the all-maximal sentinel source/event coordinates and
record rank 7 with every other coordinate literally `:na`, so it follows every
mapped record by rank without an undefined numeric sentinel. Each
category filters this single stream and assigns dense zero-based global
ordinals; per-event check/product ordinals are the dense subsequence for that
event. No hash, map iteration, fragment-local counter, or emission timing may
break ties. The transcript verifier reconstructs the stream and every filtered
ordinal/range independently; any partial order or alternate tie-break is
`C6-EVAL-ORDER`.

Loop-target recur arity is slot count, never leaf count. Its arguments evaluate
once left to right, map by slot ordinal, run all checks and projections, and
commit all next slot values atomically only after every check passes. A
function-target recur preserves B47 flat arity and emits no B51 slot transfer.
Recur remains tail-only and targets the nearest compatible entry in the mixed
function/loop stack. Wrong target, mapping, arity, or partial commit emits
`L2-RECUR-TARGET` or `C6-EVAL-ORDER`.

The closed slot schema is:

```text
{:schema :gravity/sh07-b51-binding-slot-v18
 :slot-id digest-id
 :predecessor-authority-id digest-id
 :global-slot-ordinal nonnegative-integer
 :owner-kind :let | :loop
 :owner-form-id id
 :owner-syntax-id id
 :owner-core-node-id id
 :owner-coordinate owner-coordinate-record
 :slot-coordinate slot-coordinate-record
 :enclosing-form-id id
 :enclosing-syntax-id id
 :fragment-id id
 :fragment-ordinal nonnegative-integer
 :owning-root-form-id id
 :owning-root-syntax-id id
 :owning-definition-form-id id | nil
 :owning-definition-syntax-id id | nil
 :def-form-id id | nil
 :def-syntax-id id | nil
 :defn-input-syntax-id id | nil
 :fn-form-id id | nil
 :fn-syntax-id id | nil
 :slot-ordinal nonnegative-integer
 :pattern-form-id id
 :pattern-syntax-id id
 :pattern-scope-id id
 :initializer-form-id id
 :initializer-syntax-id id
 :initializer-scope-id id
 :initializer-core-node-id id
 :first-global-extraction-ordinal nonnegative-integer
 :extraction-count positive-integer
 :terminal-count nonnegative-integer
 :leaf-count nonnegative-integer
 :vector-node-count nonnegative-integer
 :vector-node-extraction-ids vector-of-digest-id
 :visible-prior-binding-ids vector-of-id
 :introduced-binding-ids vector-of-id
 :runtime-policy :legacy-direct-binding-no-check
                 | :exact-width-runtime-checked
 :mutability :immutable
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}
```

The runtime policy is a closed union. A legacy simple-symbol slot, including
simple `_` and `&`, uses `:legacy-direct-binding-no-check`, emits exactly one
`:binding-leaf` extraction and no vector-node or runtime-check records, and
publishes through one direct atomic slot event. Its extraction has ordinal 0,
nil parent, terminal ordinal 0, leaf ordinal 0, path `[]`; its counts are
extraction/terminal/leaf `1` and vector-node `0`, with an empty
`:vector-node-extraction-ids`. A vector slot uses
`:exact-width-runtime-checked`, emits one
width check for every vector-node extraction (including an empty width-0
node), and publishes only under the guarded all-or-none policy. A branch with
the wrong policy for its pattern or with a check attached to a legacy slot is
`C6-VERIFY`.

The extraction schema is:

```text
{:schema :gravity/sh07-b51-binding-extraction-v18
 :extraction-id digest-id
 :predecessor-authority-id digest-id
 :slot-id digest-id
 :slot-coordinate slot-coordinate-record
 :global-ordinal nonnegative-integer
 :global-slot-ordinal nonnegative-integer
 :slot-ordinal nonnegative-integer
 :pattern-node-ordinal nonnegative-integer
 :parent-pattern-node-ordinal nonnegative-integer | nil
 :terminal-ordinal nonnegative-integer | nil
 :leaf-ordinal nonnegative-integer | nil
 :kind :vector-node | :binding-leaf | :wildcard-leaf
 :path vector-of-nonnegative-integer
 :form-id id
 :syntax-id id
 :scope-id id
 :enclosing-form-id id
 :enclosing-syntax-id id
 :pattern-form-id id
 :pattern-syntax-id id
 :initializer-form-id id
 :initializer-syntax-id id
 :expected-width nonnegative-integer | nil
 :binding-id id | nil
 :binding-name symbol | nil
 :definition-form-id id | nil
 :definition-syntax-id id | nil
 :binding-scope-id id | nil
 :fragment-id id
 :fragment-ordinal nonnegative-integer
 :owning-root-form-id id
 :owning-root-syntax-id id
 :owning-definition-form-id id | nil
 :owning-definition-syntax-id id | nil
 :owner-coordinate owner-coordinate-record
 :defn-input-syntax-id id | nil
 :fn-form-id id | nil
 :fn-syntax-id id | nil
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}
```

Every vector-pattern node contributes exactly one extraction; the legacy
simple-symbol case contributes the single binding extraction specified above.
All extraction keys are present and this variant matrix is exact:

```text
kind           parent-pattern-node  terminal/leaf  expected-width  five binding fields
vector-node    nil iff root, else n nil/nil        nonnegative     all nil
binding-leaf   nil iff legacy, else n n/n          nil             all required
wildcard-leaf  nonnegative          n/n            nil             all nil
```

The five binding fields are `:binding-id`, `:binding-name`,
`:definition-form-id`, `:definition-syntax-id`, and `:binding-scope-id`.
Required means non-nil and authenticated; all-nil means all five are nil.
A vector node includes
empty vectors, nonempty vectors have expected width and no terminal ordinal,
and terminal/leaf/path/parent ordinal domains are distinct and dense. A vector
containing `&` rejects before any extraction is emitted. An empty
vector contributes exactly one `:vector-node` extraction with
`:expected-width 0`, nil terminal/leaf/binding ordinals, one extraction, one
vector node, zero terminals and zero leaves, and one guarded width-0 runtime
check. `:terminal-count` is nonnegative and counts only binding and wildcard
leaves; it is zero for an empty vector. Slot and extraction ids use the exact
Tier-1 seed/preimage maps below with the neutral authority id, complete owner
coordinate, authenticated forms, path, width, and ordinal facts; they do not
contain their own or descendant requests/results.

### Closed product, transcript, check, publication, and recur schemas

Each derived product node is a closed record. The product kind controls the
following required/nil matrix; fields not named as required are nil or empty,
never omitted:

```text
kind               required variant fields                  nil/empty variant fields
slot-initializer   slot-id, child-node-ids,                 extraction-id, runtime-check-id,
                   evaluated-child-node-ids,                 publication-event-id,
                   evaluation-order                         recur-slot-mapping-id,
                                                            loop/target ids, argument-node-ids,
                                                            required-check-ids, binding-ids,
                                                            path, expected-width, commit-policy
vector-check       slot-id, extraction-id, path,             publication-event-id,
                   expected-width, runtime-check-id,        recur-slot-mapping-id,
                   evaluation-order                         loop/target ids, argument-node-ids,
                                                            commit-policy; child vectors empty
terminal-projection slot-id, extraction-id, path,            runtime-check-id,
                    binding-ids, evaluation-order            publication-event-id,
                                                            recur-slot-mapping-id, loop/target ids,
                                                            argument-node-ids, required-check-ids,
                                                            expected-width, commit-policy;
                                                            child vectors empty
slot-publication   slot-id, publication-event-id,            extraction-id, runtime-check-id,
                   binding-ids, commit-policy,               recur-slot-mapping-id,
                   evaluation-order                          loop/target ids, argument-node-ids,
                                                            required-check-ids, path,
                                                            expected-width; child vectors empty
recur-argument     loop/target ids, argument-node-ids,       slot-id, extraction-id, path,
                   recur-slot-mapping-id,                    expected-width,
                   evaluation-order                          publication-event-id, runtime-check-id,
                                                            commit-policy, required-check-ids;
                                                            child vectors and binding-ids empty
recur-commit       loop/target ids, recur-slot-mapping-id,   slot-id, extraction-id, path,
                   publication-event-id, commit-policy,      expected-width, runtime-check-id,
                   evaluation-order                          argument-node-ids, binding-ids;
                                                            child vectors empty
```

A non-nil field outside this matrix, or a required field with the wrong
domain, is `C6-VERIFY`. A product-node id is assigned from its Tier-0d seed
only; the full record may later carry the resolved back-references named in
the matrix without feeding them into that seed.

```text
{:schema :gravity/sh07-b51-product-node-v18
 :node-id digest-id
 :global-product-ordinal nonnegative-integer
 :product-kind :slot-initializer | :vector-check | :terminal-projection
                | :slot-publication | :recur-argument | :recur-commit
 :predecessor-authority-id digest-id
 :fragment-id id
 :fragment-ordinal nonnegative-integer
 :owning-root-form-id id
 :owning-root-syntax-id id
 :owning-definition-form-id id | nil
 :owning-definition-syntax-id id | nil
 :owner-kind :let | :loop
 :owner-form-id id
 :owner-syntax-id id
 :owner-core-node-id id
 :owner-coordinate owner-coordinate-record
 :slot-coordinate slot-coordinate-record | nil
 :recur-coordinate recur-coordinate-record | nil
 :slot-id digest-id | nil
 :extraction-id digest-id | nil
 :runtime-check-id digest-id | nil
 :publication-event-id digest-id | nil
 :recur-slot-mapping-id digest-id | nil
 :loop-form-id id | nil
 :loop-syntax-id id | nil
 :target-loop-form-id id | nil
 :target-loop-syntax-id id | nil
 :argument-node-ids vector-of-digest-id
 :required-check-ids vector-of-digest-id
 :child-node-ids vector-of-digest-id
 :evaluated-child-node-ids vector-of-digest-id
 :binding-ids vector-of-id
 :path vector-of-nonnegative-integer | nil
 :expected-width nonnegative-integer | nil
 :evaluation-order nonnegative-integer
 :commit-policy keyword | nil
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}
```

The product matrix also governs coordinates. Slot kinds require the exact
`:slot-coordinate`; recur kinds require the exact `:recur-coordinate`.
`:vector-check` at an initializer has nil recur coordinate, while its recur-use
counterpart requires one. A coordinate for an unrelated slot or recur site is
never admitted.

The ordered slot/extraction transcript is a closed record whose own id is not
part of its preimage:

```text
{:schema :gravity/sh07-b51-slot-extraction-transcript-v18
 :transcript-id digest-id
 :predecessor-authority-id digest-id
 :same-request-semantic-id digest-id
 :slot-count nonnegative-integer
 :extraction-count nonnegative-integer
 :runtime-check-count nonnegative-integer
 :publication-event-count nonnegative-integer
 :recur-slot-mapping-count nonnegative-integer
 :slot-ids vector-of-digest-id
 :extraction-ids vector-of-digest-id
 :runtime-check-ids vector-of-digest-id
 :publication-event-ids vector-of-digest-id
 :recur-slot-mapping-ids vector-of-digest-id
 :global-slot-range {:first 0 :count nonnegative-integer}
 :global-extraction-range {:first 0 :count nonnegative-integer}
 :global-runtime-check-range {:first 0 :count nonnegative-integer}
 :global-publication-range {:first 0 :count nonnegative-integer}
 :global-recur-range {:first 0 :count nonnegative-integer}}
```

This is one module-wide transcript. Every one of its five range records has
`:first 0`, including a zero-count category. For each category, the id-vector
length equals both its declared count and its range count, and ordinals are
exactly `0...(count-1)`. Concatenated per-fragment/local ranges are forbidden.

Every exact-width runtime check is:

```text
{:schema :gravity/sh07-b51-runtime-check-v18
 :id digest-id
 :global-runtime-check-ordinal nonnegative-integer
 :predecessor-authority-id digest-id
 :fragment-id id
 :fragment-ordinal nonnegative-integer
 :root-form-id id
 :root-syntax-id id
 :owner-kind :let | :loop
 :owner-form-id id
 :owner-syntax-id id
 :owner-coordinate owner-coordinate-record
 :slot-coordinate slot-coordinate-record
 :site-kind :init | :recur
 :recur-coordinate recur-coordinate-record | nil
 :slot-id digest-id
 :extraction-id digest-id
 :path vector-of-nonnegative-integer
 :check-kind :exact-vector-shape
 :expected-kind :vector
 :expected-width nonnegative-integer
 :check-order nonnegative-integer
 :guarded-publication-ordinal nonnegative-integer
 :guarded-event-kind :slot-atomic-publication | :loop-atomic-transfer
 :failure-rule "L7-PATTERN-TYPE"
 :reasons [:vector-required :vector-width-mismatch]
 :guard-policy :publish-after-all-checks
 :fail-closed true
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}
```

For every vector-node extraction there is one check at initialization and one
distinct check at every `recur` site targeting that loop. Initializer checks
have `:site-kind :init`, nil recur coordinate, and guard the slot event;
recur checks have `:site-kind :recur`, a required exact recur coordinate, and
guard the one loop-transfer event. Check ordinals are dense within each event
in slot order then vector-extraction preorder. The check refers to the later
publication only by its authenticated global ordinal, never by its id. A
legacy simple-symbol extraction has no check.

Every publication event is a closed tagged union. The common record and the
two branch matrices are:

```text
publication-event-v18 :=
{:schema :gravity/sh07-b51-publication-event-v18
 :id digest-id
 :global-publication-ordinal nonnegative-integer
 :predecessor-authority-id digest-id
 :fragment-id id
 :fragment-ordinal nonnegative-integer
 :root-form-id id
 :root-syntax-id id
 :owner-kind :let | :loop
 :owner-form-id id
 :owner-syntax-id id
 :owner-coordinate owner-coordinate-record
 :slot-coordinate slot-coordinate-record | nil
 :slot-coordinates vector-of-slot-coordinate-record
 :recur-coordinate recur-coordinate-record | nil
 :slot-id digest-id | nil
 :slot-ids vector-of-digest-id
 :event-kind :slot-atomic-publication | :loop-atomic-transfer
 :introduced-binding-ids vector-of-id
 :introduced-binding-id-vectors vector-of-vector-of-id
 :projection-ids vector-of-digest-id
 :projection-id-vectors vector-of-vector-of-digest-id
 :required-check-ids vector-of-digest-id
 :required-check-id-vectors vector-of-vector-of-digest-id
 :product-node-ids vector-of-digest-id
 :product-node-id-vectors vector-of-vector-of-digest-id
 :visible-before-binding-ids vector-of-id
 :visible-before-binding-id-vectors vector-of-vector-of-id
 :visible-after-binding-ids vector-of-id
 :visible-after-binding-id-vectors vector-of-vector-of-id
 :commit-order nonnegative-integer
 :all-or-none true
 :publish-none boolean
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}
```

For `:slot-atomic-publication`, `:slot-id` is non-nil, `:slot-ids` is the
one-element vector containing it, and every per-slot vector has one element
matching its singular vector. `:event-kind` is the slot event and all
introduced/projection/check/product/before/after values are for that slot;
`:slot-coordinate` is non-nil, `:slot-coordinates` is its one-element vector,
and `:recur-coordinate` is nil;
`:publish-none` is true exactly when its introduced-binding and projection
vectors are both empty. For `:loop-atomic-transfer`, `:slot-id` is nil,
`:slot-ids` contains every loop slot in ordinal order, all singular vectors
are empty, and each per-slot vector has one vector for each slot (including
empty vectors). `:slot-coordinate` is nil, `:slot-coordinates` aligns exactly
with `:slot-ids`, and `:recur-coordinate` is the exact non-nil recur site.
`:event-kind` is the loop event and one commit makes all
slot values visible together; `:publish-none` is true exactly when every
per-slot introduced-binding and projection vector is empty. A publication
never contains a recur-mapping id; the later recur mapping binds this event
through its `:commit-publication-id`. Any mixed singular/per-slot shape,
partial commit, or wrong publish-none value is `C6-EVAL-ORDER`.

Every recur mapping is:

```text
{:schema :gravity/sh07-b51-recur-slot-mapping-v18
 :mapping-id digest-id
 :global-recur-mapping-ordinal nonnegative-integer
 :predecessor-authority-id digest-id
 :fragment-id id
 :fragment-ordinal nonnegative-integer
 :root-form-id id
 :root-syntax-id id
 :loop-form-id id
 :loop-syntax-id id
 :loop-core-node-id id
 :recur-form-id id
 :recur-syntax-id id
 :recur-core-node-id id
 :target-loop-form-id id
 :target-loop-syntax-id id
 :target-loop-core-node-id id
 :commit-publication-id digest-id
 :owner-coordinate owner-coordinate-record
 :recur-coordinate recur-coordinate-record
 :slot-count nonnegative-integer
 :argument-count nonnegative-integer
 :slot-ordinals vector-of-nonnegative-integer
 :argument-ordinals vector-of-nonnegative-integer
 :slot-ids vector-of-digest-id
 :slot-coordinates vector-of-slot-coordinate-record
 :argument-node-ids vector-of-digest-id
 :extraction-id-vectors vector-of-vector-of-digest-id
 :required-check-id-vectors vector-of-vector-of-digest-id
 :projection-id-vectors vector-of-vector-of-digest-id
 :binding-id-vectors vector-of-vector-of-id
 :evaluation-order vector-of-nonnegative-integer
 :commit-policy :simultaneous-after-all-checks
 :all-or-none true
 :tail-only true
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}
```

The recur coordinate equals the mapping's repeated recur ids, and the slot
coordinate vector aligns one-for-one with slot ordinals and slot ids. Each
recur site receives distinct check, vector-check product, publication, and
mapping ids. Arguments evaluate exactly once left-to-right; checks run in
slot/preorder and failure publishes nothing. On success, projections run,
then the single loop-transfer event commits all slots, then control transfers.
Transcript counts count these per-use checks/products and per-site events.

The following seven id-seed/preimage maps are closed. Product nodes occupy
the literal Tier-0d batch at rank 7; slot/extraction and Tier-2 batches have
`:subtier nil`. Every one uses its distinct purpose batch from the 19-row
catalog:

The five shorthand clauses for an existing full record below are exact
mechanical map substitutions, not open or inherited maps. Instantiate every
key and type of the named closed full schema, remove its own id and its
`:schema` key, insert the literal two-key header
`{:domain exact-clause-domain :schema-version 18}` using the exact domain
spelled by each clause below, and add
only the explicitly required key named by the clause. No other key, including
the original `:schema` key, may remain or be introduced. Thus every such
preimage has one exact preimage domain and the literal schema version 18.

```text
product-node-id-preimage :=
{:domain :gravity/sh07-b51-product-node-id-preimage-v18
 :schema-version 18
 :predecessor-authority-id digest-id
 :global-product-ordinal nonnegative-integer
 :product-kind :slot-initializer | :vector-check | :terminal-projection
                | :slot-publication | :recur-argument | :recur-commit
 :owner-coordinate owner-coordinate-record
 :owning-definition-form-id id | nil
 :owning-definition-syntax-id id | nil
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}

binding-slot-id-preimage :=
{:domain :gravity/sh07-b51-binding-slot-id-preimage-v18
 :schema-version 18
 :predecessor-authority-id digest-id
 :global-slot-ordinal nonnegative-integer
 :owner-kind :let | :loop
 :owner-form-id id
 :owner-syntax-id id
 :owner-core-node-id id
 :owner-coordinate owner-coordinate-record
 :owning-definition-form-id id | nil
 :owning-definition-syntax-id id | nil
 :slot-coordinate slot-coordinate-record
 :slot-ordinal nonnegative-integer
 :pattern-form-id id
 :pattern-syntax-id id
 :pattern-scope-id id
 :initializer-form-id id
 :initializer-syntax-id id
 :initializer-scope-id id
 :initializer-core-node-id id
 :source-origin-chain source-origin-chain
 :source-generated-origin source-generated-origin}

binding-extraction-id-preimage :=
exact binding-extraction-v18 record above, with the `:schema` key replaced by
`{:domain :gravity/sh07-b51-binding-extraction-id-preimage-v18
  :schema-version 18}`, `:extraction-id` omitted, and `:slot-id` required;
no runtime-check, publication, recur-mapping, or transcript id is admitted.

runtime-check-id-preimage :=
exact runtime-check-v18 record above, with the `:schema` key replaced by
`{:domain :gravity/sh07-b51-runtime-check-id-preimage-v18
  :schema-version 18}`, `:id` omitted, and
`:guarded-publication-ordinal` required; no publication id is admitted.

publication-event-id-preimage :=
exact publication-event-v18 record above, with the `:schema` key replaced by
`{:domain :gravity/sh07-b51-publication-event-id-preimage-v18
  :schema-version 18}` and `:id` omitted; its required check/product/slot ids
are earlier ordered-batch values and `:publish-none` is the closed boolean
from the full record.

recur-slot-mapping-id-preimage :=
exact recur-slot-mapping-v18 record above, with the `:schema` key replaced by
`{:domain :gravity/sh07-b51-recur-slot-mapping-id-preimage-v18
  :schema-version 18}`, `:mapping-id` omitted, and
`:commit-publication-id` required; publication/check/product/slot/extraction
ids are earlier ordered-batch values.

slot-extraction-transcript-id-preimage :=
exact slot-extraction-transcript-v18 record above, with the `:schema` key
replaced by
`{:domain :gravity/sh07-b51-slot-extraction-transcript-id-preimage-v18
  :schema-version 18}` and `:transcript-id` omitted; all slot, extraction,
check, publication, and recur vectors are earlier ordered-batch values.
```

The `exact ... record above` clauses are closed references, not open rest
maps: they mean precisely every key in the named schema with `:schema`
replaced by the exact `:domain`/`:schema-version 18` header stated above,
minus the listed id, plus the explicitly added key. Product-node ids use only
the first seed and therefore never use slot, extraction, check, publication,
recur, or descendant ids. The repeated owning-definition pair in the product
and slot seeds, and in the inherited extraction preimage, must equal the
embedded owner-coordinate pair before hashing. Tier 1 emits all slot seeds before extraction
preimages; Tier 2 emits
checks, then publications, then recur mappings, then the transcript. Full
records, including reverse/back-links, are selected into the Tier-3 products
map and bound there. For a slot event, `:publish-none` is true iff both
singular introduced-binding and projection vectors are empty; for a loop event
it is true iff every per-slot introduced-binding and projection vector is
empty. It is false otherwise. Empty vectors have no terminal projection or
introduced binding, but do have a vector-node extraction, a width-0 check at
initialization and each applicable recur use, and a guarded all-or-none
publication event with empty projection/binding vectors.

## Legacy v16 equivalence

A legacy-v16-accepted predecessor emits exactly one closed
`:gravity/sh07-b51-legacy-v16-equivalence-v18` record:

```text
{:domain :gravity/sh07-b51-legacy-v16-equivalence-v18
 :schema-version 18
 :legacy-artifact-kind :gravity/sh07-canonical-core-artifact
 :legacy-schema-version 16
 :legacy-wrapper-kind nil
 :legacy-identity-preimage-domain :gravity/sh07-b47-canonical-core-v16
 :same-request-semantic-id digest-id
 :predecessor-outcome-semantic-id digest-id
 :inherited-v16-semantic-artifact-id digest-id
 :inherited-v16-artifact-purpose :sh07-core-artifact-id
 :v16-semantic-projection exact-enumerated-37-key-identity-preimage-map
 :v18-compatibility-projection exact-enumerated-37-key-identity-preimage-map
 :v16-semantic-key-order exact-37-key-vector
 :field-mapping vector
 :normalization-policy :v16-resolved-semantic-canonical-v1
 :missing-fields []
 :unexpected-fields []
 :delta []
 :equal? true
 :v16-verification-semantic-id digest-id}
```

The exact v16 semantic key vector is the following 37-key closed vector (not
an ordinal shorthand):

```text
[:domain :lineage :projection-binding :module
 :fragment-manifest :fragment-coverage :module-assembly-manifest
 :root-core-node-ids :definitions :nodes :evaluation-order :control-flow
 :reference-uses :var-references :mutations :error-transfers :error-handlers
 :match-branch-records :match-decision-skeletons :match-pattern-records
 :calls :function-records :call-edges :recursion-components :keyword-lookups
 :lexical-bindings :loop-bindings :recur-targets :recur-transfers :source-map
 :binding-table :declared-alias-table :resolution-table
 :macro-expansion-trace :macro-origin-traces :macro-origin-expectation
 :pending-fact-families]
```

Both projection values are maps with exactly the 37 keys in that vector; they
are the identity-preimage maps themselves, not artifact maps, product
summaries, or maps with metadata. The artifact identity validation is separate
and closed:
`:artifact-kind` is `:gravity/sh07-canonical-core-artifact`, `:schema-version`
is `16`, and `:identity-preimage-domain` is
`:gravity/sh07-b47-canonical-core-v16`. The derived `:artifact-id` and
`:provenance-binding-id`, complete `:provenance`, diagnostics, source paths,
and transport/session fields are not semantic projection fields. `:module`,
`:lineage`, and `:projection-binding` are retained through the authoritative
location-specific semantic transformation below. This is not a generic
path-neutral recursive normalization. Unknown keys, missing keys, or an
attempted rename are rejected.

After all controlled references are resolved, both 37-key views independently
run authoritative M's `sh07-core-semantic-identity-preimage` transformation in
this exact order:

1. choose the first truthy semantic projection id from
   `lineage.sh06-semantic-projection-id`, top-level
   `sh06-semantic-projection-id`, then
   `attributes.sh06-semantic-projection-id`;
2. if present, overwrite top-level `:projection-binding` and
   `lineage.authenticated-sh06-artifact-id` with that id;
3. map `:binding-table`, replacing each truthy upstream binding id with its
   binding id and each present definition artifact id with the projection id;
4. map `:resolution-table`, replacing each truthy upstream binding id with
   its binding id;
5. map `:var-references`, normalizing upstream binding, definition artifact,
   and authenticated SH-06 artifact ids with the exact M helper;
6. map `:mutations`, replacing target-upstream with target-binding and
   normalizing target-definition/authenticated artifact ids;
7. map `:error-transfers` and `:error-handlers`, normalizing their present
   authenticated SH-06 artifact ids;
8. map `:nodes`; only `:var`, `:set!`, `:throw`, and `:try` attributes receive
   the corresponding helper; and
9. if the preimage itself is one of those four core forms, normalize its
   top-level attributes likewise.

Every conditional is the exact M truthy/contains test, all other fields are
preserved, and vectors retain order. The 37-key vector records mapping order
only; C11 canonical map ordering governs the hash. The separate path-neutral
helper is used only to compute schema-15 projection binding p from `(dissoc q
:projection-binding :provenance)` and is never applied to these identity maps.

The field mapping is generated, not inferred. Its `:domain` entry is exactly
`{:v16-path [:domain] :v18-path [:domain]
  :normalization :constant-source
  :constant :gravity/sh07-b47-canonical-core-v16}`. For each of the other 36
keys it contains `{:v16-path [k] :v18-path [k]
:normalization :direct-resolved-semantic}`; for every nested vector/map it
recursively records every leaf path in canonical map-key/vector order. The
mapping is closed and has no wildcard path. The normalizer unwraps the B47
structural return (whose predecessor wrapper is nil), selects the actual
`:gravity/sh07-canonical-core-artifact`, resolves every controlled reference,
applies the exact location-specific transformation above, and emits the exact
semantic key vector. The inherited v16 semantic
artifact id is the freshly verified B47 `:sh07-core-artifact-id`; V18 verifies
and carries it but never reissues it as a v18 declared-digest purpose.

The two projections are complete 37-key identity-preimage maps carried by value, not uncataloged
projection ids. The v18 compatibility projection contains exactly the mapped
v16 keys and artifact identity literals above, and no B51 slot, extraction, runtime,
provenance, verifier, final, or artifact-envelope metadata fields. Semantic
node metadata already required inside the `:nodes` identity-preimage value is
retained exactly. The v16 side must reproduce its freshly verified artifact id
under `:sh07-core-artifact-id`; the v18 view is independently derived from the
complete v18 products and then projected/transformed, never copied from v16.
Both sides are compared after the same transformation. `:missing-fields`,
`:unexpected-fields`, and `:delta` must all
be empty and `:equal?` must be true. Every node, definition, evaluation,
control-flow, reference, var, mutation, error, match, call, function, edge,
recursion, keyword lookup, lexical/loop binding, recur target and transfer,
source map, module assembly, origin, and pending-fact field is therefore
preserved value-for-value. Simple-symbol slots are the degenerate V18 metadata
extension and do not alter the v16 view.

On every non-v16 branch `:legacy-v16-equivalence` is exactly nil, no
v16 product or v16 node id is copied, and the ordinary node/product family is
the complete rejected-branch v18 lowering. The nil record is an explicit
branch value, not an omitted field or an equivalence claim.

## Seven-tier digest DAG and provenance

Tier 0 uses the 0a/0b/0c/0d handshake above. Tier 1 contains slot and
extraction ids. Tier 2 contains the complete ordered slot/extraction
transcript, ordinal reconstruction, visibility, checks, publications, and
recur mappings. Tier 3 is the semantic core identity. Tier 4 is split into
the predecessor observation (4a) and its physical provenance binding (4b).
Tier 5 is the independent verifier binding. Tier 6 is the terminal artifact
binding and literal output kind. Every preimage has a closed key set, a literal
domain/schema, and ancestor-only references. Semantic ids for byte-identical
fixtures are equal while their physical provenance ids differ; physical paths,
bytes, reports, and diagnostics never enter Tier 0-3 semantic preimages.

The exact tier records are emitted only after their predecessor values have
resolved. Tier 3 binds the complete products map once; it does not duplicate
those products as top-level fields:

```text
Tier-3 core identity preimage
{:domain :gravity/sh07-b51-core-identity-preimage-v18
 :schema-version 18
 :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
 :task "SH-07-B51"
 :scope :sh07-b51-vector-destructuring
 :predecessor-outcome-kind :legacy-v16-accepted | :b51-vector-frontier-rejected
                           | :legacy-v16-accepted-after-no-form-waiver
                           | :b51-vector-frontier-rejected-after-no-form-waiver
 :same-request-semantic-id digest-id
 :predecessor-authority-id digest-id
 :module module-record
 :lineage lineage-record
 :projection-binding digest-id
 :product-key-order exact-41-key-vector
 :products exact-41-key-complete-resolved-products-map
 :diagnostics []}

Tier-3 materialized identity
{:artifact :gravity/sh07-b51-core-identity-v18
 :schema-version 18
 :semantic-identity-id digest-id
 :preimage exact-tier-3-preimage}

The `:predecessor-outcome-kind` in this preimage is authoritative for product
inheritance. The legacy-v16-accepted value requires concrete copied B47 products,
non-nil legacy equivalence, and zero core-node requests; every non-v16 value
requires a complete v18 ordinary-node/product set, nil legacy equivalence,
and no inherited rejected partial state. The resolver never rewrites an
accepted v16 id and rejects any mixed branch before hashing the identity.

Tier-4a predecessor observation preimage
{:domain :gravity/sh07-b51-predecessor-observation-preimage-v18
 :schema-version 18
 :semantic-identity-id digest-id
 :raw-adapter-request exact-raw-adapter-request}

Tier-4a materialized observation
{:artifact :gravity/sh07-b51-predecessor-observation-v18
 :schema-version 18
 :observation-id digest-id
 :preimage exact-tier-4a-preimage}

Tier-4b provenance preimage
{:domain :gravity/sh07-b51-provenance-binding-preimage-v18
 :schema-version 18
 :semantic-identity-id digest-id
 :observation-id digest-id}

Tier-4b materialized provenance
{:artifact :gravity/sh07-b51-provenance-binding-v18
 :schema-version 18
 :provenance-binding-id digest-id
 :preimage exact-tier-4b-preimage}

Tier-5 independent-verifier preimage
{:domain :gravity/sh07-b51-independent-verifier-binding-preimage-v18
 :schema-version 18
 :verifier :sh07-b51-independent-verifier-v18
 :verifier-entrypoint 'sh07-b51-build-independent-verifier-binding
 :semantic-identity-id digest-id
 :provenance-binding-id digest-id
 :check-order
 [:raw-carrier-exact-shape :authenticated-sh06-membership
  :fresh-sh06-verification :predecessor-closure :predecessor-contract
  :same-request :predecessor-outcome :predecessor-authority
  :owning-root-fragment :external-binding-authorization
  :vector-pattern-grammar :slot-extraction-reconstruction
  :empty-vector-width :visibility-and-publication :runtime-check-order
  :loop-recur-atomicity :semantic-product-closure
  :legacy-v16-equivalence :digest-dag :tier3-identity
  :tier4-observation-provenance]
 :checks
 [{:ordinal 0 :check :raw-carrier-exact-shape :status :passed}
  {:ordinal 1 :check :authenticated-sh06-membership :status :passed}
  {:ordinal 2 :check :fresh-sh06-verification :status :passed}
  {:ordinal 3 :check :predecessor-closure :status :passed}
  {:ordinal 4 :check :predecessor-contract :status :passed}
  {:ordinal 5 :check :same-request :status :passed}
  {:ordinal 6 :check :predecessor-outcome :status :passed}
  {:ordinal 7 :check :predecessor-authority :status :passed}
  {:ordinal 8 :check :owning-root-fragment :status :passed}
  {:ordinal 9 :check :external-binding-authorization :status :passed}
  {:ordinal 10 :check :vector-pattern-grammar :status :passed}
  {:ordinal 11 :check :slot-extraction-reconstruction :status :passed}
  {:ordinal 12 :check :empty-vector-width :status :passed}
  {:ordinal 13 :check :visibility-and-publication :status :passed}
  {:ordinal 14 :check :runtime-check-order :status :passed}
  {:ordinal 15 :check :loop-recur-atomicity :status :passed}
  {:ordinal 16 :check :semantic-product-closure :status :passed}
  {:ordinal 17 :check :legacy-v16-equivalence :status :passed}
  {:ordinal 18 :check :digest-dag :status :passed}
  {:ordinal 19 :check :tier3-identity :status :passed}
  {:ordinal 20 :check :tier4-observation-provenance :status :passed}]
 :status :passed}

Tier-5 materialized verifier binding
{:artifact :gravity/sh07-b51-independent-verifier-binding-v18
 :schema-version 18
 :independent-verifier-binding-id digest-id
 :preimage exact-tier-5-preimage}

Tier-6 final binding preimage
{:domain :gravity/sh07-b51-final-artifact-binding-preimage-v18
 :schema-version 18
 :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
 :semantic-identity-id digest-id
 :provenance-binding-id digest-id
 :independent-verifier-binding-id digest-id
 :status :accepted}

Tier-6 materialized final binding
{:artifact :gravity/sh07-b51-final-artifact-binding-v18
 :schema-version 18
 :final-artifact-binding-id digest-id
 :preimage exact-tier-6-preimage}
```

The tier-3 identity is the only semantic identity input to the resolved core;
Tier 4 observation/provenance, Tier 5 verification, and Tier 6 final binding
are never fed backward. The 21 Tier-5 checks are a fixed, pre-resolved
expected-pass commitment in the digest request stream; entrypoint 6 alone
independently reconstructs them and promotes the candidate only after all pass.
Entry point 7 similarly selects the already-resolved Tier-6 candidate only
after verifier success. A rejected predecessor still carries all 21 checks;
`:legacy-v16-equivalence` passes as an explicitly inapplicable nil branch.

## Independent verifier and acceptance of external bindings

V18 has a separately authored verifier. It may share only scalar predicates
that return no predecessor outcome, slot, extraction, path, width, ordinal,
visibility, recur mapping, digest preimage, or identity. It may not call the
B47 producer, B47 template/resolved verifier, v18 lowerer, slot/extraction
builder, descriptor helper, executor, fixture/expected-result helper, or
producer digest constructor.

Starting with raw authenticated SH-06 facts and the transported measured
predecessor observation, it independently reconstructs the 0a closure and
contract ids, same-request projection, actual outcome branch, frontier
diagnostic/form or no-form namespace replay, owning roots and every enclosing let/loop form, all v18
slots/nodes/extractions,
binding authentication, nested widths, visibility/order, runtime checks,
recur slot mappings and atomic transfer, complete semantic product closure,
and Tier 0-4 preimages. It compares the legacy-v16-accepted branch's exact v16
compatibility record or the rejected branch's exact frontier classification.
Transported status, artifact, rejection, diagnostic, expected maps, and ids
are values to check, never authority.

The derived edge classification is closed to exactly
`:owning-fragment-local` and `:authenticated-external`. The underlying
authenticated binding class and resolution order remain facts used by the
verifier; they are not additional classification values. Let `S` be the exact
raw SH-06 resolution artifact in the carrier, `Q` be
`S[:gravity-resolution-boundary :authenticated-resolution-request]`, and `A`
be `S[:gravity-resolution-boundary :resolved-analysis]`. These are the only
raw paths used for authorization.

For every projected resolution `K` in the complete request, first find exactly
one projected binding `B` by `K.:binding-id`. This applies even when the
reference is outside every admitted let/loop owner. Derive exactly one source
fragment `Fs`: `K.:reference-syntax-id` must occur in that fragment's
authenticated reference membership and in the unique authenticated form
membership containing the reference. No owner coordinate chooses `Fs`.
Require `B.:binding-id` to occur in exactly one of `Fs`'s authenticated
local-binding-id or external-binding-id memberships (exclusive xor); both or
neither is invalid. The verifier requires identical non-nil upstream binding ids,
then finds the unique raw resolved binding `U` in `A.:binding-table` with
`U.:binding-id = B.:upstream-binding-id`. It replays Q's references in exact
authenticated tree order through the frozen raw-to-projected syntax and scope
maps to establish K; projected and raw reference ids are never compared
directly. Namespace/core/import declarations come only from Q's
`:definitions`, `:core-bindings`, and `:import-bindings`, respectively. A
missing, duplicate, path-only, or cross-table match fails `C6-VERIFY`.

For a lexical U, find exactly one raw scope `Rs` in Q's `:lexical-scopes` by
U's scope id and exactly one binding `L` in `Rs.:bindings` by U's definition
syntax id. L has exactly
`#{:name :kind :semantic-span :source-span :binding-syntax-id
   :allow-shadow?}` and every value agrees with U's derived lexical fields.
The same binding and scope must occur in A's authenticated
`:lexical-scope-graph`; the frozen raw-to-projected scope/syntax maps must map
them to B and K. A direct raw/projected id equality is forbidden. The lexical
definition form must belong to `Fs`; an external lexical edge is never
accepted.

A local namespace reference uses U class `:namespace` with order
`:current-namespace-binding` for an unqualified symbol or
`:fully-qualified-namespace-binding` for an exact namespace/name, and its
unique Q definition is form-backed in `Fs`. A same-module sibling namespace external uses
the same two orders and public or private visibility, but its projected
definition is form-backed in exactly one authenticated fragment `G` with
`G != Fs`. The
module, upstream id, declaration syntax/artifact, lineage, projection, and
fragment joins must all agree; private visibility grants no cross-module edge.

A declared namespace definition with no form-backed fragment is a separate
closed branch. It requires exactly one Q `:definitions` declaration with no
fragment membership and exact Q/A/U/B/K agreement on upstream and binding id,
name, namespace, definition kind, visibility, profile, targets, declaration
syntax id, declaration artifact id, and resolution order. It may not also
match a local or sibling form-backed branch. Missing, duplicate, or dual
form/no-form declaration evidence is `C6-VERIFY`.

```text
exact-q-no-form-namespace-declaration :=
{:name symbol
 :kind keyword
 :namespace symbol
 :package {:name symbol :version "workspace"}
 :binding-class :namespace
 :visibility :public | :private
 :profile-set vector-of-keyword
 :target-set vector-of-keyword
 :type-ref :gravity.type/value
 :effects vector-of-keyword
 :capabilities vector-of-keyword
 :safety :safe
 :semantic-span {:explicit-candidate nonnegative-integer
                 :name symbol :namespace symbol | nil}
 :source-span {:source bounded-string :form-index 0}
 :definition-syntax-id digest-id
 :definition-artifact-id digest-id}

no-form-namespace-semantic-projection :=
{:binding-id digest-id
 :upstream-binding-id digest-id
 :name symbol
 :namespace symbol
 :kind keyword
 :binding-class :namespace
 :visibility :public | :private
 :profile-set vector-of-keyword
 :target-set vector-of-keyword
 :definition-syntax-id digest-id
 :definition-artifact-id digest-id
 :explicit-candidate-ordinal nonnegative-integer
 :resolution-order :current-namespace-binding
                   | :fully-qualified-namespace-binding}
```

The Q declaration has exactly the sixteen authentic producer keys above. Its
source span is exactly the compact two-argument `source-span` result produced
by M for an explicit candidate: the two keys `:source` and `:form-index`, with
the latter literally zero. No byte/line coordinate or additional span key is
permitted. No
form or fragment key is present. The declaration vector in either no-form
waiver outcome
contains only the exact path-neutral semantic projection, in failing-binding
encounter order, and no source span, physical value, or open Q/A/U/B/K record.
The verifier joins every projection field back to the complete Q declaration
and corresponding A/U/B/K values.
This is an intentional v18 acceptance extension for SH-06's authenticated
explicit-candidate namespace class. B47 v16 permits declaration-less
`:core`/`:import` externals but rejects this namespace class; this decision
does not attribute the new branch to B47. Legacy equivalence is required only
when the exact input was actually accepted by v16, so the new namespace branch
cannot manufacture an accepted-v16 parity claim.

An authenticated core external is exactly
`[:core :public 'gravity.core :profile-allowed-core-binding upstream-id]`.
U, B, K, A, and the unique Q `:core-bindings` declaration must agree on name,
namespace, profile/target sets, visibility, order, declaration ids, and
upstream id. A local core binding is invalid.

An authenticated import is exactly `[:import :public upstream-id]`. Its
unique declaration is in Q's `:import-bindings`, its unique dependency `D` is
in Q's `:imports`, and both must match the derived dependency record `P` in A.
An unqualified referred import uses order `:profile-allowed-core-binding` and
requires D.`:refer` to be `[:all]` or contain the name. An alias-qualified
import uses `:alias-qualified-required-binding` and requires one exact alias
`X` in both A's alias table and the schema-15 alias table; X's keys are exactly
`#{:alias :namespace :kind :profile :targets :dependency-artifact-id}`, its
fields agree with D/P, and `X.:alias` occurs in `Fs.:alias-names`. A fully
qualified import uses `:fully-qualified-namespace-binding` and exact
namespace/name equality. An explicit foreign import uses
`:explicit-foreign-import-binding`, requires D/P's complete foreign record and
an exact D namespace-or-alias qualifier, and requires neither refer membership
nor `Fs` alias membership. D uses only the authentic SH-06 import fields; no
invented export table, module, edge, or target field is admitted.

Every emitted reference edge must have a source K, exactly one `Fs`, exactly
one local/external membership, and exactly one authorized class. Unsupported
class/order/visibility, lexical externality, local
core/import, missing/nonunique scope/declaration/dependency/alias/fragment/
lineage/projection joins, ambiguous form-backed versus no-fragment
declarations, wrong namespace/profile, or unauthorized
cross-fragment edges fail `C6-VERIFY`. A fully authenticated external edge is
accepted; externality alone is not a rejection reason.

## Bounds, diagnostics, and evidence

V18 enforces at most 1,024 slots per module/owner, 1,024 pattern nodes per
slot, 65,536 extractions per module, 2,048 leaves per module, vector depth 256,
vector width 1,024, and path length 256, with saturating preflight before
allocation. Stable diagnostics are `C6-CORE-SHAPE`, `C6-LOWERING-GAP`,
`L7-DUP-BINDING`, `L7-PATTERN-TYPE`, `C6-EVAL-ORDER`, `C6-ORIGIN`,
`L2-RECUR-TARGET`, `C6-EFFECT-DROP`, `C6-UNSAFE-DROP`, and `C6-VERIFY`.
The original B47 frontier diagnostic is
transported unchanged; it is not sanitized or relabeled.

The limits and their diagnostic ownership are closed by this table. A
saturating counter stops at `maximum + 1`; larger inputs retain that sentinel
and never allocate a partial B51 product. "Masked" means authoritative M
rejects the carrier before B51 owner/product construction, so V18 preserves
the earlier raw/authentication diagnostic instead of inventing a B51 bound
failure.

```text
resource-bound-catalog :=
[{:check :owner-slot-limit
  :unit :owner-slots :maximum 1024 :crossing 1025
  :source :authenticated-owner-slot-pairs
  :boundary-owner :predecessor-b47 :terminal-reason :request-shape
  :diagnostic-schema :b47-13-key-preimage-and-16-key-emitted
  :coordinate-policy :frozen-b47-request-shape
  :finalizer :frozen-b47-diagnostic-request}
 {:check :module-slot-limit
  :unit :module-slots :maximum 1024 :crossing 1025
  :source :authenticated-module-owner-slot-pairs
  :boundary-owner :v18 :terminal-reason :module-slot-limit-exceeded
  :diagnostic-schema :v18-29-key-preimage-and-30-key-materialized
  :coordinate-policy :pattern-preflight
  :finalizer :sh07-b51-finalize-rejection}
 {:check :vector-width-limit
  :unit :vector-immediate-children :maximum 1024 :crossing 1025
  :source :authenticated-vector-child-form-ids
  :boundary-owner :predecessor-b47 :terminal-reason :request-shape
  :diagnostic-schema :b47-13-key-preimage-and-16-key-emitted
  :coordinate-policy :frozen-b47-request-shape
  :finalizer :frozen-b47-diagnostic-request}
 {:check :slot-pattern-node-limit
  :unit :slot-pattern-nodes :maximum 1024 :crossing 1025
  :source :authenticated-pattern-preorder
  :boundary-owner :predecessor-b47 :terminal-reason :request-shape
  :diagnostic-schema :b47-13-key-preimage-and-16-key-emitted
  :coordinate-policy :frozen-b47-request-shape
  :finalizer :frozen-b47-diagnostic-request}
 {:check :module-extraction-limit
  :unit :module-prospective-extractions :maximum 65536 :crossing 65537
  :source :authenticated-module-pattern-preorder
  :boundary-owner :predecessor-b47 :terminal-reason :request-shape
  :diagnostic-schema :b47-13-key-preimage-and-16-key-emitted
  :coordinate-policy :frozen-b47-request-shape
  :finalizer :frozen-b47-diagnostic-request}
 {:check :module-binding-leaf-limit
  :unit :module-binding-leaves :maximum 2048 :crossing 2049
  :source :authenticated-binding-leaf-preorder
  :boundary-owner :v18 :terminal-reason :module-binding-leaf-limit-exceeded
  :diagnostic-schema :v18-29-key-preimage-and-30-key-materialized
  :coordinate-policy :pattern-preflight
  :finalizer :sh07-b51-finalize-rejection}
 {:check :vector-depth-limit
  :unit :vector-nesting-depth :maximum 256 :crossing 257
  :source :authenticated-vector-ancestry
  :boundary-owner :predecessor-b47 :terminal-reason :form-depth-or-cycle
  :diagnostic-schema :b47-13-key-preimage-and-16-key-emitted
  :coordinate-policy :frozen-b47-graph-offender
  :finalizer :frozen-b47-diagnostic-request}
 {:check :extraction-path-length-limit
  :unit :pattern-path-indexes :maximum 256 :crossing 257
  :source :authenticated-pattern-path
  :boundary-owner :predecessor-b47 :terminal-reason :form-depth-or-cycle
  :diagnostic-schema :b47-13-key-preimage-and-16-key-emitted
  :coordinate-policy :frozen-b47-graph-offender
  :finalizer :frozen-b47-diagnostic-request}]

resource-bound-expected :=
{:unit exact-row-unit :maximum exact-row-maximum}

resource-bound-observed :=
{:unit exact-row-unit :saturating-count exact-row-crossing}
```

Slots count admitted pattern/initializer pairs. Pattern nodes count the root
and every vector, binding, and wildcard node. One prospective extraction is
counted per pattern node. Binding leaves include legacy simple symbols and
vector binding leaves but exclude vector wildcards. Vector width is the count
of immediate authenticated children. The outer vector has depth 0 and each
nested vector increments depth by one. Path length is the count of path
indexes. Counters consume the single module-global occurrence order defined
above; owner counters reset only at a new authenticated owner, and module
counters never reset.

Authoritative M caps form children at 1024, fragment-local bindings at 2048,
module bindings at 2440, fragment forms at 1024, form-graph depth at 256, and
module forms at 65536.
Consequently owner slot 1025, width 1025, pattern node 1025, module extraction
65537, vector depth 257, and path length 257 cannot occur in an admitted
schema-15 Q. Those six rows are
defensive V18 assertions and synthetic counter mutation targets only. Their
real end-to-end `+1` fixtures must fail earlier as exact
`:raw-carrier-shape` or `:authentication-membership`, with the upstream limit
source, no B51 owner coordinate, no partial products, and no relabeling.
Attempt 4 does not expand source admission or lower a mandated B51 limit.
Distributed small owners can reach module slot 1025 without exceeding one
fragment/owner cap. Likewise, 2049 binding leaves distributed across fragments
remain below M's 2440 module-binding cap. Those two V18 numeric rows use the
compile-time failure finalizer below. A 1025-node single-slot pattern remains
masked because its owner/initializer/body scaffolding crosses M's
fragment-form cap.

Both V18-owned module preflights count before emitting any success request or
B51 product. The slot offender is the pattern root of the 1025th admitted slot
in module-global order, with relative path `[]`. The leaf offender is the
2049th authenticated binding leaf in the same total pattern preorder, with its
exact nonempty-or-empty relative pattern path. In each case the
`:pattern-preflight` matrix copies that authenticated Q form/id/ordinal/syntax/
span/generated-origin, its unique F ordinal and selected owner coordinate;
core, slot, extraction, and recur ids are nil because none has been allocated.
Expected/observed values and the sole related owner-root id are exactly the
selected catalog row.
After all earlier structural `:verify` reasons and owner selection succeed,
`:module-slot-limit-exceeded` then
`:module-binding-leaf-limit-exceeded` are the next verify reasons and therefore
precede every lower-priority core-shape/origin/lowering/evaluation failure.
Each produces the exact pending envelope, failure request with empty prefix,
and root-8 finalized 29/30-key diagnostic; no truncated product map is
observable.

Bound evidence is also exact about reachability:

```text
resource-bound-fixtures :=
{:module-slot-limit
 {:end-to-end-positive :exactly-1024-distributed-slots
  :end-to-end-negative :1025th-distributed-slot-v18-finalized
  :mutations [:counter-source :off-by-one :owner-reset :module-reset
              :first-offender :later-offender :expected :observed
              :remediation :all-15-coordinate-fields
              :partial-product :failure-request :empty-prefix
              :resolved-id :finalizer-only-insertion]}
 :module-binding-leaf-limit
 {:end-to-end-positive :exactly-2048-leaves-distributed-across-fragments
  :end-to-end-negative :2049th-distributed-leaf-v18-finalized
  :mutations [:counter-source :off-by-one :fragment-reset :module-reset
              :wildcard-inclusion :legacy-leaf-inclusion
              :first-offender :later-offender :expected :observed
              :remediation :all-15-coordinate-fields
              :partial-product :failure-request :empty-prefix
              :resolved-id :finalizer-only-insertion]}
 :predecessor-owned
 {:checks [:owner-slot-limit :vector-width-limit :slot-pattern-node-limit
           :module-extraction-limit :vector-depth-limit
           :extraction-path-length-limit]
  :end-to-end-positive :authoritative-M-exact-limit
  :end-to-end-negative :authoritative-M-plus-one-B47-rejection
  :expected-reasons [:request-shape :form-depth-or-cycle]
  :mutations [:boundary-owner :B47-reason :B47-13-to-16-transform
              :no-v18-coordinate :no-partial-product :unchanged-transport]
  :synthetic-B51-counter-tests :unit-and-mutation-only
  :synthetic-tests-are-end-to-end-evidence false}}
```

Each masked fixture asserts M's actual request-shape diagnostic has nil
form/syntax/semantic span, empty generated origins, source-only emitted span,
and the frozen generic B47 remediation; the graph-depth row uses the same
shape with reason `:form-depth-or-cycle`. No saturating count or offending B51
coordinate survives that earlier boundary. Claiming otherwise would require a
separately governed source-admission/predecessor change outside this report.

The predecessor B47 diagnostic has two distinct exact shapes. Its identity
preimage has exactly these thirteen keys:

```text
b47-diagnostic-identity-preimage :=
{:domain :gravity/sh07-b47-c6-diagnostic-v16
 :rule string
 :syntax-id digest-id | nil
 :form-id digest-id | nil
 :semantic-source-span semantic-span-record | nil
 :origin-chain source-origin-chain
 :generated-origin-chain source-generated-origin
 :namespace symbol
 :profile :meta
 :target :jvm
 :source-revision-id digest-id
 :sh06-artifact-id digest-id
 :facts b47-diagnostic-facts-value}

b47-emitted-source-span :=
  {:source bounded-string}
| (semantic-span-record plus exactly {:source bounded-string})

b47-emitted-diagnostic :=
{:artifact :gravity/sh07-core-diagnostic
 :rule string
 :severity :error
 :stage :core-lowering
 :syntax-id digest-id | nil
 :form-id digest-id | nil
 :core-node-id nil
 :source-span b47-emitted-source-span
 :generated-origin-chain source-generated-origin
 :namespace symbol
 :profile :meta
 :target :jvm
 :lowering-rule :sh07-b47-function-call-recursion-products
 :facts {:reason keyword
         :semantic-rule string | nil
         :rule-specific b47-diagnostic-facts-value
         :source-revision-id digest-id
         :sh06-artifact-id digest-id
         :fail-closed true}
 :remediation string
 :diagnostic-id-request internal-digest-reference | digest-id}
```

The mapping is explicit, not "thirteen plus three." Rule, syntax id, form id,
generated origins, namespace, profile, and target copy directly. The semantic
span becomes `:source-span` with the physical source added only in the emitted
observation. `:origin-chain` is omitted from the emitted map but remains
authenticated in the raw nine-key result's diagnostic request preimage.
Source revision and SH-06 artifact move into emitted `:facts`; preimage facts
become `:rule-specific` and also deterministically supply reason and semantic
rule. Artifact, severity, stage, nil core node, lowering rule, remediation,
and diagnostic-id request are emitted additions. Nothing else is dropped,
renamed, or inferred.

`b47-diagnostic-facts-value` is recursively closed to nil, booleans, bounded
integers/strings, keywords, symbols, digest ids, exact internal-digest-reference
records, bounded vectors, and bounded maps with keyword keys; depth, width,
node, and scalar-byte limits are the exact M generated-digest-carrier bounds.
Traversal visits vector indexes in order and map entries in M canonical key
order. Every exact controlled reference in `:facts` is resolved by ordinal;
no reference-shaped partial map or reference in a key is admitted. The host
replaces only the value of `:diagnostic-id-request` with the resolved digest;
it never adds a `:diagnostic-id` key. Therefore the rejected raw outcome binds
both the exact nine-key result (including the 13-key preimage and origin chain)
and the exact resolved sixteen-key thrown map.

Every new compile-time v18 failure first builds this exact 29-key own-id-free semantic
record; every key is present even
when its value is nil:

```text
diagnostic-remediation :=
{:action :fix-source | :supply-authenticated-declaration
         | :repair-carrier | :report-implementation-defect
 :owner :source-author | :master-coordinator | :sh-core
 :required-evidence vector-of-keyword}

diagnostic-semantic-value :=
{:artifact :gravity/sh07-b51-c6-diagnostic-v18
 :schema-version 18
 :status :rejected
 :diagnostic-kind :core-shape | :lowering-gap | :duplicate-binding
                  | :pattern-type | :evaluation-order | :origin
                  | :recur-target | :effect-drop | :unsafe-drop | :verify
 :rule exact-kind-rule-string
 :severity :error
 :stage :core-lowering
 :reason diagnostic-reason-catalog-key
 :form-id digest-id | nil
 :form-ordinal nonnegative-integer | nil
 :fragment-ordinal nonnegative-integer | nil
 :syntax-id digest-id | nil
 :source-span semantic-span-record | nil
 :generated-origin-chain source-generated-origin
 :core-node-id digest-id | nil
 :owner-coordinate owner-coordinate-record | nil
 :slot-id digest-id | nil
 :extraction-id digest-id | nil
 :recur-form-id digest-id | nil
 :recur-syntax-id digest-id | nil
 :path vector-of-nonnegative-integer | nil
 :expected catalog-selected-expected-value
 :observed catalog-selected-observed-value
 :related-semantic-ids catalog-selected-related-id-vector
 :lowering-rule :sh07-b51-vector-destructuring-v18
 :profile :meta
 :target :jvm
 :remediation diagnostic-remediation
 :fail-closed true}

kind-rule-map :=
{:core-shape "C6-CORE-SHAPE"
 :lowering-gap "C6-LOWERING-GAP"
 :duplicate-binding "L7-DUP-BINDING"
 :pattern-type "L7-PATTERN-TYPE"
 :evaluation-order "C6-EVAL-ORDER"
 :origin "C6-ORIGIN"
 :recur-target "L2-RECUR-TARGET"
 :effect-drop "C6-EFFECT-DROP"
 :unsafe-drop "C6-UNSAFE-DROP"
 :verify "C6-VERIFY"}

kind-remediation-map :=
{:core-shape {:action :report-implementation-defect :owner :sh-core
              :required-evidence [:authenticated-form :core-shape]}
 :lowering-gap {:action :fix-source :owner :source-author
                :required-evidence [:offending-form :supported-grammar]}
 :duplicate-binding {:action :fix-source :owner :source-author
                     :required-evidence [:binding-paths]}
 :pattern-type {:action :fix-source :owner :source-author
                :required-evidence [:runtime-value-kind :expected-width]}
 :evaluation-order {:action :report-implementation-defect :owner :sh-core
                    :required-evidence [:event-transcript]}
 :origin {:action :repair-carrier :owner :master-coordinator
          :required-evidence [:authenticated-origin-chain]}
 :recur-target {:action :fix-source :owner :source-author
                :required-evidence [:recur-coordinate :target-slots]}
 :effect-drop {:action :report-implementation-defect :owner :sh-core
               :required-evidence [:authenticated-effects
                                   :preserved-declarations]}
 :unsafe-drop {:action :report-implementation-defect :owner :sh-core
               :required-evidence [:authenticated-safety
                                   :capabilities :unsafe-island]}
 :verify {:action :repair-carrier :owner :master-coordinator
          :required-evidence [:failed-invariant]}}

The `:module-slot-limit-exceeded` and
`:module-binding-leaf-limit-exceeded` reason-specific remediations in the
catalog below replace the generic `:verify` remediation exactly for those two
reasons; no other reason may use them.

diagnostic-reason-catalog :=
{:core-shape
 {:reasons [:invalid-owning-core-shape :invalid-defn-trace-shape]
  :expected :authenticated-owning-core-shape
  :observed :let | :loop | :def | :fn | :other
  :related-id-count 0}
 :lowering-gap
 {:reasons [:unsupported-vector-rest :unsupported-nested-pattern]
  :expected :legacy-symbol-or-fixed-vector-pattern
  :observed :rest-marker | :unsupported-form
  :related-id-count 0}
 :duplicate-binding
 {:reasons [:duplicate-vector-binding-name]
  :expected :unique-vector-leaf-name
  :observed symbol
  :related-id-count 2}
 :pattern-type
 {:reasons [:malformed-authenticated-pattern-shape]
  :expected :authenticated-fixed-vector-pattern
  :observed :malformed-pattern
  :related-id-count 0}
 :evaluation-order
 {:reasons [:initializer-order-mismatch :check-order-mismatch
            :publication-order-mismatch]
  :expected nonnegative-integer
  :observed nonnegative-integer
  :related-id-count 2}
 :origin
 {:reasons [:missing-origin :ambiguous-origin :origin-mismatch]
  :expected :one-authenticated-origin-chain
  :observed :missing | :multiple | :mismatched
  :related-id-count 0}
 :recur-target
 {:reasons [:missing-recur-target :ambiguous-recur-target
            :recur-arity-mismatch :recur-not-tail]
  :expected :nearest-compatible-function-or-loop-target
  :observed :missing | :multiple | nonnegative-integer | :non-tail
  :related-id-count 1
  :reason-overrides
  {:recur-arity-mismatch
   {:expected nonnegative-integer
    :observed nonnegative-integer
    :related-id-count 1
    :value-sources
    {:expected
     {:target-kind-dispatch
      {:function [:selected-target :arity]
       :loop [:selected-target :slot-count]}}
     :observed
     {:derive :count-rest-child-form-ids
      :form [:authenticated-sh06-request :forms :selected-recur]
      :expression (count (rest (:child-form-ids selected-recur-form)))}
     :related-semantic-ids [[:selected-target :target-id]]}}}}
 :effect-drop
 {:reasons [:effect-omitted :effect-reordered :effect-weakened]
  :expected vector-of-keyword
  :observed vector-of-keyword
  :related-id-count 1}
 :unsafe-drop
 {:reasons [:safety-weakened :capability-omitted :unsafe-island-omitted]
  :expected :safe | :unsafe-island | vector-of-keyword
  :observed :safe | :unsafe-island | vector-of-keyword | nil
  :related-id-count 1}
 :verify
 {:reasons [:raw-carrier-shape :authentication-membership
            :branch-mismatch :digest-protocol :schema-shape
            :fragment-membership :owner-selection
            :module-slot-limit-exceeded
            :module-binding-leaf-limit-exceeded]
  :expected :authenticated-closed-value
  :observed :missing | :duplicate | :mismatched | :malformed
  :related-id-count 0
  :reason-overrides
  {:module-slot-limit-exceeded
   {:expected {:unit :module-slots :maximum 1024}
    :observed {:unit :module-slots :saturating-count 1025}
    :related-id-count 1
    :related-semantic-ids [owner-coordinate.root-form-id]
    :remediation
    {:action :fix-source :owner :source-author
     :required-evidence [:resource-bound :first-offending-coordinate]}}
   :module-binding-leaf-limit-exceeded
   {:expected {:unit :module-binding-leaves :maximum 2048}
    :observed {:unit :module-binding-leaves :saturating-count 2049}
    :related-id-count 1
    :related-semantic-ids [owner-coordinate.root-form-id]
    :remediation
    {:action :fix-source :owner :source-author
     :required-evidence [:resource-bound :first-offending-coordinate]}}}}}

diagnostic-priority :=
[:verify :core-shape :origin :lowering-gap :duplicate-binding
 :recur-target :evaluation-order :effect-drop :unsafe-drop :pattern-type]

diagnostic-coordinate-matrix-record :=
{:source-selector exact-authenticated-record-path
 :selection-priority exact-ordered-selector-vector
 :form-id :required-equal-source-form | nil
 :form-ordinal :required-equal-Q-global-index | nil
 :syntax-id :required-equal-source-syntax | nil
 :source-span :required-equal-source-semantic-span | nil
 :generated-origin-chain :required-equal-source-generated-origin | []
 :fragment-ordinal :required-equal-unique-F-ordinal | nil
 :owner-coordinate :required-equal-selected-owner | nil
 :core-node-id :required-equal-selected-core-node | nil
 :slot-id :required-equal-selected-slot | nil
 :extraction-id :required-equal-selected-extraction | nil
 :recur-form-id :required-equal-selected-recur-form | nil
 :recur-syntax-id :required-equal-selected-recur-syntax | nil
 :path :required-equal-selected-extraction-path | nil}

coordinate-template :=
{:none
 {:form nil :fragment nil :owner nil :core nil :slot nil :extraction nil
  :recur nil :path nil}
 :form
 {:form required :fragment unique-if-present :owner nil :core nil :slot nil
  :extraction nil :recur nil :path nil}
 :owner
 {:form required :fragment required :owner required :core nil
  :slot nil :extraction nil :recur nil :path nil}
 :slot
 {:form required :fragment required :owner required :core owner-core
  :slot required :extraction nil :recur nil :path nil}
 :extraction
 {:form required :fragment required :owner required :core owner-core
  :slot required :extraction required :recur nil :path extraction-path}
 :recur
 {:form required :fragment required :owner required :core recur-core
  :slot nil :extraction nil :recur required :path nil}
 :recur-form
 {:form required :fragment required :owner required :core nil
  :slot nil :extraction nil :recur required :path nil}
 :recur-slot
 {:form required :fragment required :owner required :core recur-core
  :slot required :extraction nil :recur required :path nil}
 :pattern-preflight
 {:form required :fragment required :owner required :core nil
  :slot nil :extraction nil :recur nil :path authenticated-pattern-path}
 :semantic-source
 {:form source-if-form :fragment unique-if-form :owner source-if-owner
  :core source-if-core :slot nil :extraction nil :recur nil :path nil}}

diagnostic-coordinate-policy :=
{:raw-carrier-shape
 {:template :none :source [:raw-adapter-request]
  :priority [[:raw-adapter-request]]}
 :authentication-membership
 {:template :form :source [:authenticated-sh06-request :forms :offending]
  :priority [[:authenticated-sh06-request :forms :offending]
             [:sh06-resolution-artifact]]}
 :branch-mismatch
 {:template :semantic-source :source [:predecessor-raw-outcome]
  :priority [[:predecessor-raw-outcome :direct-template-observation]
             [:predecessor-raw-outcome :structural-runner-observation]]}
 :digest-protocol
 {:template :none :source [:digest-requests :offending-ordinal]
  :priority [[:digest-requests :offending-ordinal]]}
 :schema-shape
 {:template :semantic-source :source [:first-malformed-authenticated-record]
  :priority [[:authenticated-sh06-request] [:resolved-core] [:products]]}
 :fragment-membership
 {:template :form :source [:authenticated-sh06-request :forms :offending]
  :priority [[:authenticated-sh06-request :forms :offending]]
  :override {:fragment nil}}
 :owner-selection
 {:template :form :source [:authenticated-sh06-request :forms :enclosing]
  :priority [[:authenticated-sh06-request :forms :enclosing]]}
 :module-slot-limit-exceeded
 {:template :pattern-preflight
  :source [:authenticated-sh06-request :forms
           :module-slot-limit-first-crossing-pattern-root]
  :priority [[:authenticated-sh06-request :forms
              :module-slot-limit-first-crossing-pattern-root]]
  :expected {:unit :module-slots :maximum 1024}
  :observed {:unit :module-slots :saturating-count 1025}
  :related-semantic-ids [[:selected-owner-coordinate :root-form-id]]
  :expanded-coordinate-constraints
  {:core-node-id nil :slot-id nil :extraction-id nil
   :recur-form-id nil :recur-syntax-id nil :path []}}
 :module-binding-leaf-limit-exceeded
 {:template :pattern-preflight
  :source [:authenticated-sh06-request :forms
           :module-binding-leaf-limit-first-crossing-leaf]
  :priority [[:authenticated-sh06-request :forms
              :module-binding-leaf-limit-first-crossing-leaf]]
  :expected {:unit :module-binding-leaves :maximum 2048}
  :observed {:unit :module-binding-leaves :saturating-count 2049}
  :related-semantic-ids [[:selected-owner-coordinate :root-form-id]]
  :expanded-coordinate-constraints
  {:core-node-id nil :slot-id nil :extraction-id nil
   :recur-form-id nil :recur-syntax-id nil
   :path :required-equal-authenticated-pattern-path}}
 :invalid-owning-core-shape
 {:template :form :source [:authenticated-sh06-request :forms :enclosing]
  :priority [[:authenticated-sh06-request :forms :enclosing]]}
 :invalid-defn-trace-shape
 {:template :form :source [:authenticated-sh06-request :forms :root]
  :priority [[:authenticated-sh06-request :forms :root]
             [:authenticated-sh06-request :macro-expansion-trace]]}
 :unsupported-vector-rest
 {:template :owner :source [:authenticated-sh06-request :forms :rest-marker]
  :priority [[:authenticated-sh06-request :forms :rest-marker]]}
 :unsupported-nested-pattern
 {:template :owner :source [:authenticated-sh06-request :forms :pattern]
  :priority [[:authenticated-sh06-request :forms :pattern]]}
 :duplicate-vector-binding-name
 {:template :extraction :source [:binding-extractions :first-duplicate]
  :priority [[:binding-extractions :first-duplicate]
             [:binding-extractions :second-duplicate]]}
 :malformed-authenticated-pattern-shape
 {:template :owner :source [:authenticated-sh06-request :forms :pattern]
  :priority [[:authenticated-sh06-request :forms :pattern]]}
 :initializer-order-mismatch
 {:template :slot :source [:binding-slots :first-mismatch]
  :priority [[:binding-slots :first-mismatch]]}
 :check-order-mismatch
 {:template :extraction :source [:runtime-checks :first-mismatch]
  :priority [[:runtime-checks :first-mismatch]]}
 :publication-order-mismatch
 {:event-source [:publication-events :first-mismatch]
  :event-kind-dispatch
  {:slot-atomic-publication
   {:template :slot
    :source [:authenticated-sh06-request :forms
             :matching-event-owner-enclosing-form]
    :priority [[:publication-events :first-mismatch]
               [:authenticated-sh06-request :forms
                :matching-event-owner-enclosing-form]]
    :required-event-fields
    {:event-kind :slot-atomic-publication
     :slot-id digest-id
     :recur-coordinate nil}
    :expanded-coordinate-constraints
    {:slot-id :required-equal-event-slot
     :recur-form-id nil :recur-syntax-id nil
     :extraction-id nil :path nil}}
   :loop-atomic-transfer
   {:template :recur
    :source [:authenticated-sh06-request :forms
             :matching-event-recur-coordinate]
    :priority [[:publication-events :first-mismatch]
               [:authenticated-sh06-request :forms
                :matching-event-recur-coordinate]]
    :required-event-fields
    {:event-kind :loop-atomic-transfer
     :slot-id nil
     :recur-coordinate recur-coordinate-record}
    :expanded-coordinate-constraints
    {:slot-id nil :extraction-id nil :path nil
     :recur-form-id :required-equal-event-recur-form
     :recur-syntax-id :required-equal-event-recur-syntax}}}}
 :missing-origin
 {:template :form :source [:authenticated-sh06-request :forms :offending]
  :priority [[:authenticated-sh06-request :forms :offending]]}
 :ambiguous-origin
 {:template :form :source [:authenticated-sh06-request :forms :offending]
  :priority [[:authenticated-sh06-request :forms :offending]]}
 :origin-mismatch
 {:template :form :source [:authenticated-sh06-request :forms :offending]
  :priority [[:authenticated-sh06-request :forms :offending]]}
 :missing-recur-target
 {:template :recur-form :source [:authenticated-sh06-request :forms :recur]
  :priority [[:authenticated-sh06-request :forms :recur]]}
 :ambiguous-recur-target
 {:template :recur-form :source [:authenticated-sh06-request :forms :recur]
  :priority [[:authenticated-sh06-request :forms :recur]]}
 :recur-arity-mismatch
 {:template :recur-form
  :source [:authenticated-sh06-request :forms :recur]
  :priority [[:authenticated-sh06-request :forms :recur]]
  :target-selector
  [:authenticated-recur-target-stack :unique-nearest-compatible-target]
  :expected
  {:target-kind-dispatch
   {:function [:selected-target :arity]
    :loop [:selected-target :slot-count]}}
  :observed
  {:derive :count-rest-child-form-ids
   :form [:authenticated-sh06-request :forms :selected-recur]
   :expression (count (rest (:child-form-ids selected-recur-form)))}
  :related-semantic-ids [[:selected-target :target-id]]
  :expanded-coordinate-constraints
  {:core-node-id nil :slot-id nil :extraction-id nil :path nil
   :recur-form-id :required-equal-source-form
   :recur-syntax-id :required-equal-source-syntax}}
 :recur-not-tail
 {:template :recur-form :source [:authenticated-sh06-request :forms :recur]
  :priority [[:authenticated-sh06-request :forms :recur]]}
 :effect-omitted
 {:template :semantic-source :source [:first-effect-bearing-record]
  :priority [[:nodes] [:module] [:products]]}
 :effect-reordered
 {:template :semantic-source :source [:first-effect-bearing-record]
  :priority [[:nodes] [:module] [:products]]}
 :effect-weakened
 {:template :semantic-source :source [:first-effect-bearing-record]
  :priority [[:nodes] [:module] [:products]]}
 :safety-weakened
 {:template :semantic-source :source [:first-safety-bearing-record]
  :priority [[:nodes] [:module] [:products]]}
 :capability-omitted
 {:template :semantic-source :source [:first-capability-bearing-record]
  :priority [[:nodes] [:module] [:products]]}
:unsafe-island-omitted
 {:template :semantic-source :source [:first-unsafe-island-record]
  :priority [[:nodes] [:module] [:products]]}}

The policy notation is a closed compile-time expansion, not a materialized
alias. `:offending`, `:first-mismatch`, and every `:first-*` selector resolve
to the smallest module-global occurrence from the total-order reconstruction;
`:priority` breaks cross-family ties left-to-right. The expanded
`:source-selector` is the exact realized vector path with numeric indexes and
contains none of those selector keywords. Expanding the selected template
produces all fifteen matrix keys. Required form fields equal the selected Q
form's ids, global Q index, semantic span, and generated origin. Required
fragment equals its unique F ordinal; required owner/slot/extraction/core/recur
fields equal the selected closed record; required path equals the selected
extraction path, except `:pattern-preflight`, whose path equals the exact
authenticated pattern-relative path before any extraction exists.
`:recur-form-id` and `:recur-syntax-id` are both required or
both nil. Every field not required by the expanded template is literally nil
(or `[]` for generated origin), with no inferred nearby id.

For `:recur-arity-mismatch`, Gravity replays the authenticated mixed
function/loop target stack at the selected Q recur form. Compatibility is
decided from the authenticated target tag; the target with the greatest
lexical depth is selected. Zero compatible targets is
`:missing-recur-target`; more than one compatible target at that greatest
depth is `:ambiguous-recur-target`. Only one uniquely selected nearest target
can produce `:recur-arity-mismatch`. Its expected value is that target's
authenticated `:arity` only when `:target-kind` is `:function`, or its
authenticated `:slot-count` only when `:target-kind` is `:loop`. No shared
target-arity selector exists. To derive the observed value, Gravity requires
the selected Q form to be the authenticated list form whose authenticated
first child is the resolved `recur` operator, then computes exactly
`(count (rest (:child-form-ids selected-recur-form)))`. There is no
`:argument-count` field lookup. The sole related semantic id is the selected
target id. The diagnostic source never comes from a successful
`:recur-slot-mappings` product. The `:recur-form` template makes
`:core-node-id`, `:slot-id`, `:extraction-id`, and `:path` literally nil while
requiring the authenticated recur form and syntax ids.

Recur-arity fixtures have distinct positive function and loop variants. The
function fixture reads only the selected function target's `:arity`; the loop
fixture reads only the selected loop target's `:slot-count`; both derive the
observed count from the authenticated recur list children. Mutations swap the
target-kind dispatch, read the other variant's field, remove or alter the
variant field, change the authenticated child-form vector, change the list
kind or resolved first-child operator, and replace the derived count with an
invented `:argument-count` selector. Every mutation is rejected.

For `:publication-order-mismatch`, the selected event's literal `:event-kind`
chooses exactly one dispatch branch. A `:slot-atomic-publication` selects the
unique Q enclosing form named by the event owner coordinate and the event's
one nonnil singular slot; both recur ids, extraction, and path are nil. A
`:loop-atomic-transfer` selects the unique Q recur form named by its nonnil
recur coordinate; the singular slot, extraction, and path are nil, and the
recur form/syntax/core fields equal that coordinate. Any other event kind,
missing or extra branch field, nonunique Q join, or coordinate inconsistent
with the selected event is a schema failure rather than a fallback. Positive
fixtures cover both tags. Mutations swap either tag, remove the required slot
from the slot branch, add a singular slot to the loop branch, remove or alter
the loop recur coordinate, and mutate every expanded coordinate equality.

When several records implicate one reason, the printed priority and then
smallest global ordinal select exactly one source. The independent verifier
recomputes the expansion and every equality from Q and resolved products.
For every literal reason, fixtures independently mutate each of the exact
fifteen matrix keys `:source-selector`, `:selection-priority`, `:form-id`,
`:form-ordinal`, `:syntax-id`, `:source-span`, `:generated-origin-chain`,
`:fragment-ordinal`, `:owner-coordinate`, `:core-node-id`, `:slot-id`,
`:extraction-id`, `:recur-form-id`, `:recur-syntax-id`, and `:path`. Each
field whose exact expanded value is nil is tested with an illegal
nil-to-nonnil flip. Each field whose exact expanded value is nonnil is tested
with an illegal nonnil-to-nil flip and, independently, substitution of a
different authenticated value. Empty generated-origin is likewise mutated to
a nonempty vector, and a required nonempty origin is mutated to `[]`. There is
no aggregate `form`, `fragment`, `owner`,
`span`, or `origin` shorthand in this mutation set. Positive fixtures assert
the exact expanded matrix for every reason and both publication event tags.

runtime-vector-failure :=
{:artifact :gravity/sh07-b51-runtime-pattern-failure-v18
 :schema-version 18
 :stage :runtime-vector-check
 :rule "L7-PATTERN-TYPE"
 :reason :vector-required | :vector-width-mismatch
 :runtime-check-id digest-id
 :guarded-publication-ordinal nonnegative-integer
 :expected-kind :vector
 :expected-width nonnegative-integer
 :observed-kind :vector | :non-vector
 :observed-width nonnegative-integer | nil
 :publish-none true}

runtime-check-abi :=
{:artifact :gravity/sh07-b51-runtime-check-result-v18
 :schema-version 18
 :status :passed
 :tag :publication-authorized
 :value {:publication-event-id digest-id}}
|
{:artifact :gravity/sh07-b51-runtime-check-result-v18
 :schema-version 18
 :status :rejected
 :tag :pattern-failed
 :value runtime-vector-failure}

diagnostic-id-preimage :=
{:domain :gravity/sh07-b51-c6-diagnostic-id-preimage-v18
 :schema-version 18
 :diagnostic-semantic-value exact-29-key-diagnostic-semantic-value}

failure-digest-request :=
{:purpose :sh07-b51-c6-diagnostic-id
 :preimage diagnostic-id-preimage}

diagnostic-id := exact digest returned by the declared-digest resolver for
failure-digest-request

materialized-diagnostic :=
exact-29-key-diagnostic-semantic-value plus
{:diagnostic-id diagnostic-id}

rejected-result :=
{:artifact :gravity/sh07-b51-rejected-result-v18
 :schema-version 18
 :status :rejected
 :diagnostic materialized-diagnostic}

pending-rejected-result :=
{:artifact :gravity/sh07-b51-rejected-result-v18
 :schema-version 18
 :status :rejected
 :diagnostic-semantic-value exact-29-key-diagnostic-semantic-value
 :diagnostic-id-request failure-digest-request}

pending-rejected-envelope :=
{:artifact :gravity/sh07-b51-rejected-envelope-v18
 :schema-version 18
 :pending-result pending-rejected-result}

rejected-envelope :=
{:artifact :gravity/sh07-b51-rejected-envelope-v18
 :schema-version 18
 :result rejected-result}

failure-resolver-return :=
{:status :accepted
 :purpose :sh07-b51-c6-diagnostic-id
 :digest-id diagnostic-id
 :envelope rejected-envelope}
```

`:effect-drop` is emitted exactly when any authenticated effect declaration is
omitted, reordered, or weakened in a node/module/product projection.
`:unsafe-drop` is emitted exactly when authenticated safety, capability, or
`:unsafe-island` boundary evidence is omitted or weakened. `:verify` is
reserved for structural/authentication/schema failures and cannot stand in for
either semantic-preservation rule. `:rule` must equal
`kind-rule-map[:diagnostic-kind]` and `:remediation` must
equal `kind-remediation-map[:diagnostic-kind]`. No facts/details rest
map, exception object, physical path, session, or other physical value is
admitted. `:lowering-rule` is a distinct literal from the diagnostic `:rule`.
When an authenticated syntax/form is available, `:form-id`, `:form-ordinal`,
`:syntax-id`, `:source-span`, and `:generated-origin-chain` are non-nil/exact:
the form id is that offending authenticated Q form, `:form-ordinal` is its
unique module-global index in Q's authenticated form vector, the span is its
authenticated semantic span, and the origin vector is its ordered
authenticated `:generated-origin`. `:fragment-ordinal` is non-nil only after
the form has exactly one authenticated fragment membership; it is nil for
missing, duplicate, cross-fragment, or pre-owner failures and otherwise equals
that fragment's `:ordinal`. Owner-coordinate is non-nil only after unique owner
selection. Thus missing/duplicate/cross-fragment/owner diagnostics preserve
form identity/span/origin without inventing a fragment/owner coordinate. With
no form, all nullable identity/span/coordinate fields are nil and the origin
vector is `[]`. Remediation is selected deterministically by the closed
kind/reason table and has no prose/details escape hatch.
The compiler enumerates applicable failures, selects the first kind in
`diagnostic-priority`, then the first applicable reason in that kind's literal
`:reasons` vector. `:expected` and `:observed` must match that catalog row's
literal/type union exactly; `:related-semantic-ids` has exactly the declared
count, in authenticated encounter order. The selected reason must also match
its exact `diagnostic-coordinate-policy`; the policy tokens expand to the
complete fifteen-key source/equality/nullability matrix stated above. No unlisted
reason, generic keyword, arbitrary diagnostic value, or alternative priority
is admitted.
The compile-time failure-only digest/finalizer protocol applies only to the
29-key semantic diagnostic. A runtime width/type guard emits exactly
`runtime-vector-failure` at the check site and performs the already-bound
publish-none action; it never invokes the compile-time digest resolver or
finalizer. Conversely, a compile-time malformed pattern never masquerades as
a runtime value failure.
At runtime `:observed-kind` is `:vector` iff the guarded value is a vector and
`:non-vector` otherwise; `:observed-width` is exactly `(count value)` in the
vector case and nil otherwise. The runtime ABI returns either the prebound
successful publication/transfer value or this exact failure record. The
independent verifier and differential fixtures replay both outcomes, and
mutations of kind, width, check id, publication ordinal, or publish-none must
fail before any binding becomes visible.
`:sh07-b51-c6-diagnostic-id` is a declared-digest purpose in a separate
failure-only request stream using the same opaque declared-digest resolver;
the failure request has no success tier, subtier, or ordinal and cannot carry
an internal success reference. It and its preimage never enter or extend the
19-row success Tier-0--6 catalog. The raw predecessor B47 diagnostic remains
its unchanged older schema and is bound only through the raw observation: its
semantic diagnostic projection has the exact 13 B47 fields and its emitted
diagnostic has the different exact 16-field schema and explicit transform
enumerated above. Neither is coerced into the v18 29/30-key schema.
On failure, producer/verifier entrypoints 1, 2, 4, 5, 6, and 7 return exactly
`pending-rejected-envelope`; they never fabricate or guess the id. The host
mechanically invokes entrypoint 3 with its exact `failure-digest-request` and
`resolved-prefix []` by passing the request, the exact one-element vector
`[failure-digest-request]`, and `[]` as the three arguments. Gravity returns
`digest-preimage-resolution`, the host computes
exactly the receipt's canonical declared-digest hash, and returns
`host-digest-receipt`. It then invokes entrypoint 8 with the exact pending
envelope and that receipt's resolved id. The finalizer authenticates the pending
envelope/result, diagnostic semantic value, purpose/domain, and own-id-free preimage,
then inserts only the supplied resolved id into the materialized diagnostic,
result, and envelope and returns `failure-resolver-return`. It does not hash,
recompute, semantically interpret, or alter any other field. Thus every
producer/verifier entrypoint has the same closed pending branch and the exact
failure-only finalizer has the sole closed terminal branch. The id hashes only
the path-neutral 29-key semantic value; the materialized id and envelope never
feed back into its preimage.
The independent verifier reconstructs the offending form/ordinal, span,
generated-origin order, rule/lowering-rule distinction, profile/target, exact
remediation, diagnostic preimage/request, 30-key materialized diagnostic,
pending result, resolver return, terminal result, and envelope from
authenticated inputs. It verifies one consistent opaque host-resolved id at
every post-resolution position but does not recompute the hash. Its
`:digest-dag` and `:semantic-product-closure` checks cover this
failure stream even though it is not a success tier.

Evidence must include byte-identical `.gravity`/`.qst` pairs for accepted
legacy simple-symbol programs (B47/V18 exact empty delta), rejected-frontier
vector `let`/`loop` programs accepted only by V18, nested/empty/wildcard
patterns, runtime non-vector and wrong-width failures, successful and failing
recur transfers, positive authenticated no-form explicit-candidate namespace
programs whose first B47 failure is the exact authorized shape rejection and
whose narrow replay reaches each of the two closed post-waiver results,
near-miss shape failures with one unrelated B47 invariant failure, and every
unsupported/malformed/duplicate/unauthorized
external case. Mutation probes must swap branches, attach an artifact to a
rejection, attach a rejection to an accepted branch, alter pins/closure
members, change paths/session facts in semantic preimages, delete/reorder
products or ordinal records, forge external bindings, add self/descendant
edges, alter diagnostic form ordinal/span/origin/lowering-rule/profile/target/
remediation, inject a physical path into diagnostic semantics, or coordinate
producer and expected-result mutations. Every probe must
fail closed and no host exception may be passing evidence.

## Atomic implementation and integration

The SH-06 vector-leaf omission is a conformance bug, not a schema-version
change. Schema 1 lexical scope/binding vectors and schema 15 requests already
admit nested leaf records; the producer must populate the records before B47.
This architecture decision is first governed, independently accepted, and
integrated/reconciled to authoritative main as workstream id
`sh07-b51-v18-architecture-attempt-4`, invariant family
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`, the exact
existing failed family retained from attempts 1 and 2 without relabeling. Its
lifecycle must advance through independently `accepted`, then
`integration-eligible`, then `integrated`; no direct draft-to-integrated jump
is permitted. Call that resulting governance/main
baseline `G`. Its semantic implementation source remains
`M = 3d7d4b532176841a45db71fa3ca37f7300d2d2c8`; G adds governance/report
authority, not compiler semantics. Only then is implementation one governed
stack over G:

```text
G -> A (master-coordinator SH-06 vector-leaf conformance correction)
  -> C (sh-core v18 semantics, products, verifier)
  -> H (coordinator mechanical invocation and assembly)
```

The implementation has exactly one ledger identity:

```text
id               sh07-b51-v18-atomic-stack
invariant_family self-hosting/sh07-b51-v18-atomic-stack
base_commit      G
dependencies     [sh07-b51-v18-architecture-attempt-4]
candidate        H only
```

A and C are exact checkpoint commits/trees inside that one workstream, not
ledger workstreams, dependency states, or integration candidates. Each has a
separate independent review artifact bound to its exact commit, tree, clean
worktree, reviewer identity, invariant checklist, and evidence hash. H must
descend from the reviewed A commit and then the reviewed C commit. Only H is
the ledger candidate and integration-eligible; authoritative main stays G
until one integration advances G to H.

H evidence must run exactly:

```text
clojure -M tools/validate_sh07_b51_v18_atomic_stack.clj \
  contracts/workstream-ledger.json \
  docs/artifacts/phase-15/evidence/sh07-b51-v18-atomic-stack.edn
```

That dedicated validator checks baseline/main equals G; H ancestry contains
the exact reviewed A then C commits in order; every A/C/H commit/tree tuple and
worktree is clean; review identities are independent and match their evidence;
owned paths/invariants and measured closure tuples match; and no source,
schema, candidate, evidence, or main drift occurred. The current governance-v1
validator does not itself enforce internal A/C checkpoint ancestry; this
dedicated command and H's independent reviewer do. H's reviewer reruns and
independently rechecks the complete result. Any G/main, A/C/H tuple/tree,
review, source, closure, schema, or evidence drift requires a fresh affected
checkpoint and H candidate.

This draft does not add a ledger row. After an exact report commit exists, the
coordinator first creates a separate closed architecture-draft governance
record whose `base_commit` is the terminal attempt-3 governance parent
`5ec786dab8973a29cdc4b1060d54cdf50ec56b17` and whose candidate/tree are that
exact report tuple. Only after all report obligations are closed may a later
separate lifecycle commit freeze that tuple and mark it review-pending for an
independent reviewer. The architecture tuple must then be accepted and
integrated/reconciled to main to establish G before the single implementation
workstream is created. Draft creation, freezing, architecture review/
integration, internal A/C reviews, H admission, and H integration are distinct
events; no placeholder reviewer, candidate, tree, or disposition is permitted.

A is coordinator-owned and may edit only the SH-06 producer/projection paths in
`bootstrap/clojure/src/gravity/bootstrap.clj`, dedicated SH-06 vector-leaf
fixtures/tests, and their governance/evidence records. It changes only a
vector binding slot: the analyzer and projection branches at the current
`let`/`loop` binding sites must use identical preorder vector-leaf enumeration,
evaluate each initializer under the prior active scope chain, then publish one
scope/slot containing every retained non-`_`/`&` leaf and map each upstream
binding syntax id to its exact nested leaf syntax id/path. Empty vectors publish
an explicit zero-leaf scope record. For duplicate vector names, a
non-authoritative error-recovery projection retains the first preorder
occurrence, omits every later occurrence, and uses that same retained path
vector in both analyzer and projection; the raw pattern is unchanged and C/V
must emit `L7-DUP-BINDING` before any accepted artifact. Simple-symbol slots
are outside this recovery: B47/SH-06 behavior and bytes remain identical,
including ordinary simple-symbol `_` and `&`, with parity probes for both.
The adapter must preserve schema 1 and schema 15 versions and all existing
fields. It computes only the existing schema-1 lexical-scope/binding ids and
schema-15 projection-binding/request ids with their canonical upstream
formulas; it introduces no new semantic-id domain and never alters raw
patterns. A must demonstrate fresh accepted nested references, bindings,
resolutions, scope visibility, projection replay, and the exact unmodified
B47 frontier on the same request. A is never integration-eligible alone and
is independently reviewed.

C is an exact child of A. It may add disjoint v18 definitions to
`checked_core.gravity` and paired fixtures/tests. C must not change the measured
B47 298-function closure, its three contract forms, or B47 lowering behavior;
its combined source receives fresh whole-file v18 source-byte, source-hash,
plan, and function pins in H. Those fresh pins are physical v18 provenance and
do not replace the immutable closure descriptor. C owns no coordinator files
and is never integration-eligible alone.

H is an exact child of C. H may update only mechanical v18 invocation, measured
source/closure/contract census, raw-carrier transport, opaque digest resolution,
and lifecycle records. Before invocation H authenticates the immutable closure
and contract records, then mechanically records the direct build-root result
and separately runs the structural route (which invokes the three genuine v16
roots) on the byte-identical schema-15 request emitted by A. It transports and
joins both actual observations exactly as specified above.
It cannot implement vector semantics, synthesize leaves/products, classify
outcomes, or manufacture ids. A, C, and H each require independent exact
review. Main remains G until H, then advances once from G to H. Any intervening
main change, closure drift, source identity change, A/C/H review rejection, or
schema/field drift requires a fresh tuple.

## Ownership, residual boundaries, and nonclaims

`:master-coordinator` owns the A SH-06 vector-leaf conformance correction and
the H mechanical B47 invocation, closure/contract pin transport, opaque digest
resolution, physical provenance, and final assembly. `:sh-core` owns C v18
pattern semantics, complete product schemas, compatibility projection, and
independent verification. Clojure/JVM remain source reader, strict decoder,
SH-06/B47 host, plan executor, digest resolver, runtime-check host, and
observer. The verifier is evidence and is not a proof of itself.

This decision does not claim map/list/set/record/constructor/schema/resource or
variable-width patterns, defaults/rest/guards, parameter destructuring, general
match coverage, type/effect/ownership/safety completion, MIR/optimization,
complete exception semantics, public routing, aggregate SH-07 completion,
self-hosting, release, performance, or seed retirement. It does not modify
B47 behavior, v17 history, attempt-1 history, or WIP authority.

## Independent acceptance criteria

Before this draft may become review-pending, it must be frozen as a clean exact
candidate descended from governance parent `5ec786d...`. An independent
reviewer of that future tuple must confirm:

1. The v17 contradiction and exact attempt-1 rejection tuple are preserved;
   attempt 2's exact `26d50d9...` / `1aaf70a...` tuple is terminally rejected
   by the independent review recorded at governance commit `ce2a9b8...`, with
   all seven blockers retained without relabeling; and attempt 3's exact
   `f50a0d9...` / `e34d57...` / report SHA `0f63e...` tuple is terminally
   rejected by the independent review recorded at governance parent
   `5ec786d...`, with all three blocker strings retained verbatim.
2. The carrier is the closed eleven-key schema-18 domain and transports one
   exact fresh SH-06 request/report, immutable B47 descriptor, combined v18
   descriptor, contract carrier, physical invocation, and actual same-request
   B47 outcome.
3. B47 semantic identity-preimage, actual core artifact, wrapper, and v18
   output kinds are distinct literal values.
4. The measured predecessor closure has exact roots, 298 reachable members,
   and the separately labeled 305-function M plan census, plus exact names,
   shapes, referenced builtin-name/call-site, contract-form, and descriptor
   hashes; closure drift fails before invocation. The combined C whole-file
   census and eight-root closure are separately measured and are not constrained
   to M's 305-function total.
5. All four semantic outcome projections are closed, own-id-free,
   path-neutral, and exclude complete physical artifact/report/diagnostic
   payloads; those payloads bind only Tier 4.
6. The 19 success purposes have exact one-purpose batches ranked 0 through 18;
   each nine-key request has the cataloged tier/subtier/batch/rank, dense
   batch/global ordinals, and every typed controlled reference targets the
   exact purpose at a strictly earlier `[batch-rank batch-ordinal]` and global
   ordinal. Tier-0d core postorder, product nodes, same-tier extraction/
   publication/mapping/transcript dependencies, and Tier-4 observation-to-
   provenance order are acyclic without exceptions. The selected Tier-3 and
   preimage registries contain exactly 94 static controlled path descriptors for
   `:legacy-v16-accepted` and 174 for each non-v16 outcome; accepted ordinary
   core terminals are concrete inherited B47 digests with zero request edge,
   while non-v16 ordinary core terminals target the core-node purpose.
7. Template, resolved core, canonical output artifact, and wrapper have the
   exact closed schemas above; Tier 6 names the literal output kind.
8. Accepted legacy requests compare every listed v16 product through the exact
   field mapping and authoritative location-specific semantic transformation,
   yielding an empty-delta record and the reproduced v16 artifact id.
9. V18 independently reconstructs Tier 0-4 and accepts only authenticated
   external bindings while rejecting unauthorized cross-fragment edges.
10. Slot/extraction products retain empty vectors, all ordinals, origins,
    exact runtime width, simultaneous visibility, slot-based recur arity, and
    atomic transfer.
11. All seven resource-bound families have literal limits, saturating sources,
    deterministic ownership, diagnostics, coordinate/finalizer behavior, and
    fixtures: reachable distributed module-slot and module-binding-leaf
    overflow use the exact V18 29/30-key failure stream, while the six M-masked
    owner/width/node/extraction/depth/path checks preserve the exact earlier
    B47 request-shape/depth diagnostic without fabricated B51 counts,
    coordinates, or partial products.
12. The architecture retains invariant family
    `architecture/self-hosting-sh07-b51-vector-destructuring-v18` and reaches
    accepted, integration-eligible, then integrated before establishing G.
    After that, G -> A -> C -> H is
    atomic: A is the independently reviewed schema-1/15
    producer correction, C is its exact sh-core child, H is C's exact
    coordinator child, no child can land alone, all three tuples receive
    independent checkpoint review within the single atomic-stack workstream,
    and main advances once only from G to H after the architecture establishes G.
13. v17, attempt 1, and WIP retain terminal meanings; this report grants no
    implementation, integration, roadmap, or SH-07 completion authority until
    its own independent lifecycle reaches acceptance and integration.

The author may report defects but cannot accept this decision or confer
integration eligibility.
