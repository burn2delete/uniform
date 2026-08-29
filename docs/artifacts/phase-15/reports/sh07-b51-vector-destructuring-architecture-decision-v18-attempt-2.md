# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 2

Status: proposed for independent review; implementation remains stopped

Date: 2026-08-29

## Purpose and disposition of attempt 1

This is a clean, versioned successor to the integrated B51 v17 architecture
and to the rejected v18 attempt 1. It does not edit, relabel, or extend any
v17 or attempt-1 identity. The attempt-1 report is retained at
`docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18.md`.
Its exact candidate was commit `5e9b14aa9a1bbf6a5e6d6503d38506e728bc78a2`,
tree `afb0a9da7139f121598a202fec1f4aa82871dd43`, based on
`3d7d4b532176841a45db71fa3ca37f7300d2d2c8`. Independent reviewer
`b51_v18_independent_review` rejected that tuple. The rejection is terminal
for that candidate only and grants no implementation or integration authority.

The rejection identified six exact defects, all corrected here:

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

Attempt 2 defines an acyclic Tier-0 handshake, exact semantic/physical
projections, a separately measured immutable B47 executable closure and
contract carrier, complete v18 products, and an exact compatibility record.
No implementation may start until this exact candidate is independently
accepted and later reconciled to main.

## Governing contracts and inputs

The governing inputs are `AGENTS.md`, D1, D2, D3, D8, D9, L2, L7, L9, C5,
C6, BOOT7, BOOT8, TEST10, TEST11, TEST13,
`docs/self-hosting-slice-backlog.md`, `docs/self-hosting-slice-ownership.edn`,
`docs/workstream-governance.md`, `contracts/workstream-governance.json`, the
integrated C6 authoritative source admission, and the integrated B51 v17
architecture decision. The exact authoritative base is
`M = 3d7d4b532176841a45db71fa3ca37f7300d2d2c8`.

The authenticated SH-06 schema-15 request, its resolution report, top-level
roots, binding/resolution/scope tables, fragment ownership and coverage,
module assembly, macro traces, origins, and lineage are inputs. V18 never
rebuilds, reorders, or host-projects them. Attempt-1 and all v17 WIP are
evidence only; they confer no source or semantic authority.

## Why v18 is required

The current B47 v16 producer rejects a positive vector `let` binding at the
outer frontier with `C6-LOWERING-GAP` and reason
`:let-destructuring-deferred`, and rejects a positive vector `loop` binding
with `C6-LOWERING-GAP` and reason `:loop-destructuring-deferred`. Those checks
run before B47 emits an accepted canonical core. Therefore v17's requirement
for an exact accepted v16 artifact for the same positive vector request is
unreachable. A stale artifact, a different request, a host-synthesized
artifact, or a sanitized rejection would be unauthenticated.

V18 admits a closed sum of two actual outcomes for one exact request:

* `:legacy-v16-accepted`: frozen B47 v16 actually accepted the request and
  produced the complete canonical core artifact; or
* `:b51-vector-frontier-rejected`: frozen B47 v16 actually rejected that same
  request at its deterministic outer `let`/`loop` vector frontier.

The negative branch authorizes V18 to process the exact verified request; it
does not prove nested patterns, remainder legality, recur legality, or any
other V18 fact. V18 performs a complete independent pass and fails closed.

## Versioned domains and kind separation

Only these new v18 domains are introduced:

```text
:gravity/sh07-b51-adapter-request-v18
:gravity/sh07-b51-predecessor-executable-closure-v18
:gravity/sh07-b51-predecessor-contract-v18
:gravity/sh07-b51-predecessor-outcome-semantic-v18
:gravity/sh07-b51-predecessor-observation-v18
:gravity/sh07-b51-predecessor-authority-v18
:gravity/sh07-b51-canonical-template-v18
:gravity/sh07-b51-resolved-core-v18
:gravity/sh07-b51-canonical-core-artifact-v18
:gravity/sh07-b51-core-artifact-v18
:gravity/sh07-b51-product-node-v18
:gravity/sh07-b51-binding-slot-v18
:gravity/sh07-b51-binding-extraction-v18
:gravity/sh07-b51-slot-extraction-transcript-v18
:gravity/sh07-b51-core-identity-v18
:gravity/sh07-b51-provenance-binding-v18
:gravity/sh07-b51-independent-verifier-binding-v18
:gravity/sh07-b51-final-artifact-binding-v18
:gravity/sh07-b51-legacy-v16-equivalence-v18
:gravity/sh07-b51-c6-diagnostic-v18
```

The following existing B47 names have distinct roles and must never be
interchanged:

```text
:gravity/sh07-b47-canonical-core-v16
  semantic identity-preimage domain only
:gravity/sh07-canonical-core-artifact
  actual canonical core artifact kind emitted by B47
:gravity/sh07-core-artifact
  actual B47 wrapper artifact kind, when the outer route supplies one
:gravity/sh07-to-c6-core-products-v16
  B47 adapter contract
```

An accepted predecessor records the actual core kind and optional wrapper kind
separately, and records `:gravity/sh07-b47-canonical-core-v16` only as the
semantic identity-preimage domain. The v18 output kind is the literal
`:gravity/sh07-b51-canonical-core-artifact-v18`; its optional outer wrapper is
the literal `:gravity/sh07-b51-core-artifact-v18`. No v16 or v17 alias is a
v18 output.

## Exact authenticated carrier

The v18 adapter request is a closed map with exactly these keys:

```text
{:artifact :gravity/sh07-b51-adapter-request-v18
 :schema-version 18
 :authenticated-sh06-request <exact schema-15 request>
 :authenticated-sh06-verification-report <exact fresh report>
 :predecessor-executable-closure <closure record>
 :predecessor-contract <contract record>
 :same-request-binding <record>
 :predecessor-outcome <semantic and physical outcome record>
 :predecessor-authority <record>
 :request-provenance <Tier-4 physical binding>
 :scope :sh07-b51-vector-destructuring}
```

The coordinator may invoke the frozen B47 entrypoint and transport its exact
return value mechanically. It may not select a fixture, create a request,
rewrite forms or bindings, classify a result, fabricate a v16 artifact, alter
a diagnostic, or manufacture any digest, slot, extraction, expected map, or
semantic id. The Gravity core emits digest requests for every derived id; the
host resolves only those exact requests.

The raw B47 return and its complete artifact/report or diagnostic are retained
in `:gravity/sh07-b51-predecessor-observation-v18`. They are physical facts
and are bound at Tier 4. The semantic outcome projection is a separate
own-id-free record computed from the authenticated request and the actual
return value.

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
 :edge-count 851
 :edge-hash
 "sha256:f01c42e33f7128ea5d9544c561d89e4001daf363a001ba4a34f982e0d4d86b97"
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
 :module-contract <exact filtered v16 module/header/export record>
 :semantic-contracts
 {'sh07-core-bounds <exact value of sh07-core-bounds>
  'sh07-core-contract <exact value of sh07-core-contract>
  'sh07-core-diagnostic-catalog <exact value of sh07-core-diagnostic-catalog>}
 :members <exact sorted vector of the 298 selected function symbols>
 :functions <exact selected complete 298-member plan-function map>}
```

`p15-s23-c11-mir-digest` is exactly SHA-256 over the C11 canonical `pr-str`
encoding of this full descriptor preimage. The module contract is the actual
filtered v16 module/header/export record bound by
`sha256:e12f3dde6a0afa798e034b9e60311847535832ec9adffc10e221784c3665c58f`;
the semantic-contracts map is canonical sorted-by-name and is bound by the
three measured value hashes and combined contracts hash above. The members
vector is the actual sorted 298-symbol vector (names hash above), and the
functions map is the actual selected complete plan-function map (selected-map
hash above). No host map printer, summary substitution, or
implementation-specific serialization is permitted. The resulting descriptor
id is the measured value
`sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234`.

The closure algorithm is normative. Build the stage-2 plan from the exact M
source and emitter. Starting with the fixed declaration-order root vector
above, walk each function's
instruction tree in canonical map-key and vector-index order. Follow every
explicit `:function-call` target and every quoted in-module function symbol;
the target must resolve to `plan[:functions]`. Reject a dynamic invocation,
an unresolved target, a duplicate definition, or an unexpected instruction
shape. The selected function member is the complete canonical function record
(including its literal constants and binding metadata) with physical source
fields removed. Builtin calls are not executable closure members. Referenced
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
 :actual-wrapper-artifact-kind :gravity/sh07-core-artifact
 :request-artifact :gravity/sh07-authenticated-sh06-core-request
 :request-schema-version 15
 :lowering-rule :sh07-b47-function-call-recursion-products
 :entrypoints ['sh07-build-core-template
               'sh07-verify-core-template
               'sh07-verify-core-resolved]
 :source-contract-forms <record above>
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

## Path-neutral same-request and outcome projections

All semantic ids use an explicit normalizer, not a host `dissoc` guess. The
normalizer admits only the closed schema keys below, recursively sorts map keys
by their canonical schema order, preserves vector order, and rejects any
unknown key. It removes only physical keys at their declared provenance
locations: `:actual-source-path`, `:source-path`, `:project-root`,
`:checkout-path`, `:session-id`, `:attempt-id`, `:invocation-id`, `:host`,
`:wall-clock`, and transport handles. Fragment id, local syntax ordinal,
semantic form digest, source revision bytes id, and authenticated projection
id are semantic. A field is never silently renamed or sanitized.

The same-request semantic binding is a closed 0a record:

```text
{:domain :gravity/sh07-b51-same-request-binding-v18
 :schema-version 18
 :request-artifact :gravity/sh07-authenticated-sh06-core-request
 :request-schema-version 15
 :request-semantic-id digest-id
 :projection-binding digest-id
 :authenticated-sh06-artifact-id digest-id
 :source-revision-id digest-id
 :top-level-root-semantic-ids vector-of-digest-id}
```

Its preimage retains the exact authenticated request semantic projection,
including root ids, forms, bindings, resolutions, fragments, origins, and
module assembly after physical fields are removed. It contains no outcome,
artifact, report, path, session, slot, extraction, verifier, or final id.

The raw observation is a separate closed physical record:

```text
{:domain :gravity/sh07-b51-predecessor-observation-v18
 :schema-version 18
 :same-request-binding-id digest-id
 :invocation-id opaque-physical-id
 :attempt-id opaque-physical-id
 :session-id opaque-physical-id
 :source-path string
 :source-byte-count nonnegative-integer
 :source-content-hash digest-id
 :result-status :accepted | :rejected
 :actual-core-artifact-kind keyword | nil
 :actual-wrapper-artifact-kind keyword | nil
 :complete-artifact <exact B47 core/wrapper> | nil
 :complete-verification-report <exact fresh report> | nil
 :exact-rejection-diagnostic <exact B47 diagnostic> | nil}
```

The physical record is accepted only when it is the exact transported return
from the measured closure invocation. Its `:observation-id` is a Tier-4
provenance id over the complete bytes and physical facts; it is not an input to
semantic outcome identity.

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
 :actual-wrapper-artifact-kind :gravity/sh07-core-artifact | nil
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
 :frontier-owner :let | :loop
 :frontier-rule "C6-LOWERING-GAP"
 :frontier-reason :let-destructuring-deferred
                     | :loop-destructuring-deferred
 :frontier-binding-kind :vector
 :frontier-form-semantic-id digest-id
 :frontier-coordinate {:fragment-id digest-id
                        :local-syntax-ordinal nonnegative-integer}
 :semantic-artifact-id nil
 :authorization :v18-vector-lowering}
```

The accepted projection is derived from the complete freshly verified B47
artifact's normalized semantic identity preimage and its normalized v16
verification products. The rejected projection is derived from the actual
first diagnostic and the authenticated form at the same outer frontier. No
physical path, complete artifact, complete report, complete diagnostic map,
session, attempt, or own `:outcome-id` occurs in either preimage. The outcome
semantic id is the canonical digest of this projection plus its literal domain
and schema. The physical observation id is recorded separately at Tier 4.

The actual B47 rejection must be the deterministic first failure for the same
request: `:let-destructuring-deferred` with `:binding-kind :vector`, or
`:loop-destructuring-deferred` whose offending authenticated form is a vector,
under rule `C6-LOWERING-GAP`. Any earlier or different B47 rejection receives
`:authorization :none` and V18 rejects. A frontier rejection never proves a
nested unsupported form, duplicate, unauthorized edge, malformed remainder,
or invalid recur; the full V18 pass must find those facts.

## Acyclic staged Tier-0 handshake

The attempt-1 single Tier-0 level was circular. V18 retains seven major tiers
but divides Tier 0 into four ordered subtiers. Each subtier has a dense
global ordinal and a dense ordinal within that subtier. A preimage may refer
only to raw authenticated input or an earlier subtier.

```text
0a  implementation semantic id, contract semantic id,
    same-request semantic id
0b  predecessor outcome semantic id, from 0a plus raw normalized outcome
0c  neutral predecessor authority id, from 0a and 0b
0d  product-node ids, from 0c plus raw authenticated node coordinates
1   slot and extraction ids
2   slot/extraction transcript
3   path-neutral v18 semantic identity
4   physical provenance binding
5   independent verifier binding
6   final artifact binding
```

The 0a implementation preimage contains the literal closure descriptor, its
roots, measured members/counts/hashes, and semantic contract-form carrier. The
0a contract and request preimages are own-id-free. The 0b preimage contains
only the normalized union branch and the resolved 0a ids; it contains no
physical observation. The 0c preimage contains resolved 0a/0b ids, branch,
authorization, and nil-versus-non-nil semantic artifact id. The 0d product
preimage contains resolved authority id and source-semantic coordinate. No
0a id refers to 0b, 0c, or 0d; no 0b id refers to itself; and no authority id
is used to compute itself. Physical artifact/report/diagnostic facts first
enter Tier 4. This is a closed DAG, not an eighth semantic tier.

The neutral authority record is:

```text
{:domain :gravity/sh07-b51-predecessor-authority-v18
 :schema-version 18
 :tier 0
 :subtier :0c
 :global-ordinal nonnegative-integer
 :subtier-ordinal nonnegative-integer
 :implementation-semantic-id digest-id
 :contract-semantic-id digest-id
 :same-request-semantic-id digest-id
 :outcome-semantic-id digest-id
 :outcome-kind :legacy-v16-accepted | :b51-vector-frontier-rejected
 :semantic-artifact-id digest-id | nil
 :authorization :legacy-v16-equivalence | :v18-vector-lowering | :none}
```

Only this neutral authority id is admitted into slot, extraction, product,
template, and semantic identity preimages. Rejection is never called an
artifact. Paths, reports, diagnostic maps, attempts, sessions, and transport
ids are provenance only.

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
 :pending-fact-families :binding-slots :binding-extractions
 :slot-extraction-transcript :runtime-checks :publication-events
 :recur-slot-mappings :legacy-v16-equivalence]
```

No open `:products` map is permitted. Each key is required with a vector or
closed record of the schema specified by the corresponding v18 product
domain. An absent category, an extra category, an unbound digest reference,
or an out-of-order product is `C6-VERIFY`.

### Canonical template schema

`:gravity/sh07-b51-canonical-template-v18` is a closed map with exactly:

```text
{:artifact :gravity/sh07-b51-canonical-template-v18
 :schema-version 18
 :adapter-request-id digest-id
 :same-request-binding-id digest-id
 :predecessor-authority-id digest-id
 :module <path-neutral authenticated module>
 :lineage <path-neutral authenticated lineage>
 :projection-binding digest-id
 :fragment-manifest vector
 :fragment-coverage vector
 :module-assembly-manifest map
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
 :binding-slots vector
 :binding-extractions vector
 :slot-extraction-transcript map
 :runtime-checks vector
 :publication-events vector
 :recur-slot-mappings vector
 :legacy-v16-equivalence record | nil
 :digest-requests vector
 :provenance-binding-preimage record
 :diagnostics vector}
```

### Resolved core schema

`:gravity/sh07-b51-resolved-core-v18` is the same closed product set after all
digest references resolve, with exactly these additional identity fields and
no request fields:

```text
{:artifact :gravity/sh07-b51-resolved-core-v18
 :schema-version 18
 :template-id digest-id
 :adapter-request-id digest-id
 :same-request-binding-id digest-id
 :predecessor-authority-id digest-id
 :semantic-identity-id digest-id
 :provenance-binding-id digest-id
 :independent-verifier-binding-id digest-id
 :fragment-manifest vector
 :fragment-coverage vector
 :module-assembly-manifest map
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
 :binding-slots vector
 :binding-extractions vector
 :slot-extraction-transcript map
 :runtime-checks vector
 :publication-events vector
 :recur-slot-mappings vector
 :legacy-v16-equivalence record | nil
 :diagnostics []}
```

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
 :artifact-kind :gravity/sh07-b51-canonical-core-artifact-v18
 :task "SH-07-B51"
 :semantic-identity-id digest-id
 :provenance-binding-id digest-id
 :independent-verifier-binding-id digest-id
 :adapter-request-id digest-id
 :same-request-binding-id digest-id
 :predecessor-authority-id digest-id
 :module <path-neutral module>
 :lineage <path-neutral lineage>
 :projection-binding digest-id
 :fragment-manifest vector
 :fragment-coverage vector
 :module-assembly-manifest map
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
 :canonical-core-artifact <exact output artifact>
 :provenance {:binding-id digest-id}
 :pass {:name :c6-gravity-core-lowering-b51-v18
        :input :authenticated-sh06-resolution
        :output :gravity/sh07-b51-canonical-core-artifact-v18}
 :execution-boundary <closed ownership record>
 :downstream-fact-statuses {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
 :pending-lowering-families vector
 :sh07-complete? false
 :self-hosted? false}
```

The Tier-6 final binding contains the literal output kind, resolved semantic
identity, Tier-4 provenance id, and Tier-5 verifier id only. It is terminal.

## B51 pattern, slot, extraction, and execution semantics

The admitted grammar is exactly:

```text
pattern := binding-symbol | _ | [pattern*]
```

Symbols are authenticated SH-06 lexical bindings, exclude `_`, `&`, and
reserved names, and occur once per complete slot. Vectors can be recursively
nested or empty. Maps, lists, sets, records, constructors, aliases, guards,
defaults, rest, variable-width vectors, parameter destructuring, and general
`match` expansion remain deferred.

Each top-level `let`/`loop` pattern-initializer pair is one slot. Slots are
source ordered. The initializer runs exactly once with only earlier slots
visible. Vector nodes are checked preorder with exact authenticated width;
terminal projections use authenticated paths without reevaluation. All leaves
publish simultaneously only after every check passes. A non-vector or wrong
width value emits `L7-PATTERN-TYPE`; it publishes no current slot and evaluates
no later initializer/body. Empty vectors remain explicit vector-node records.

Loop/recur arity is slot count, never leaf count. Recur arguments evaluate once
left to right, map by slot ordinal, run all checks and projections, and commit
all next slot values atomically only after every check passes. Recur remains
tail-only and targets the nearest compatible loop. Wrong target, mapping,
arity, or partial commit emits `L2-RECUR-TARGET` or `C6-EVAL-ORDER`.

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
 :terminal-count positive-integer
 :leaf-count nonnegative-integer
 :vector-node-count nonnegative-integer
 :vector-node-extraction-ids vector-of-digest-id
 :visible-prior-binding-ids vector-of-id
 :introduced-binding-ids vector-of-id
 :runtime-policy :exact-width-runtime-checked
 :mutability :immutable}
```

The extraction schema is:

```text
{:schema :gravity/sh07-b51-binding-extraction-v18
 :extraction-id digest-id
 :predecessor-authority-id digest-id
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
 :expected-width nonnegative-integer | nil
 :binding-id id | nil
 :binding-name symbol | nil
 :definition-form-id id | nil
 :definition-syntax-id id | nil
 :binding-scope-id id | nil
 :source-origin origin-record}
```

Every pattern node contributes exactly one extraction. A vector node includes
empty vectors, nonempty vectors have expected width and no terminal ordinal,
and terminal/leaf/path/parent ordinal domains are distinct and dense. Slot and
extraction ids use own-id-free Tier-1 preimages with the neutral authority id,
owner coordinate, authenticated forms, path, width, and ordinal facts; they do
not contain their own or descendant requests/results.

## Legacy v16 equivalence

An accepted predecessor emits exactly one closed
`:gravity/sh07-b51-legacy-v16-equivalence-v18` record:

```text
{:domain :gravity/sh07-b51-legacy-v16-equivalence-v18
 :schema-version 18
 :legacy-artifact-kind :gravity/sh07-canonical-core-artifact
 :legacy-wrapper-kind :gravity/sh07-core-artifact | nil
 :legacy-identity-preimage-domain :gravity/sh07-b47-canonical-core-v16
 :same-request-semantic-id digest-id
 :predecessor-outcome-semantic-id digest-id
 :v16-semantic-projection-id digest-id
 :v18-compatibility-projection-id digest-id
 :v16-product-key-order vector
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

The artifact identity normalization is also closed and literal:
`:artifact-kind` is `:gravity/sh07-canonical-core-artifact`, `:schema-version`
is `16`, and `:identity-preimage-domain` is
`:gravity/sh07-b47-canonical-core-v16`. The derived `:artifact-id` and
`:provenance-binding-id`, complete `:provenance`, diagnostics, source paths,
and transport/session fields are not semantic projection fields. `:module`,
`:lineage`, and `:projection-binding` are retained after the existing
path-neutral semantic normalization: authenticated source-revision and SH-06
projection ids remain, while physical paths and artifact/report handles are
removed only at their declared provenance locations. Unknown keys, missing
keys, or an attempted rename are rejected.

The field mapping is generated, not inferred. For every one of the 37 keys it
contains `{:v16-path [k] :v18-path [k]
:normalization :resolved-v16-semantic}`; for every nested vector/map it
recursively records every leaf path in canonical map-key/vector order. The
mapping is closed and has no wildcard path. The normalizer unwraps the B47
wrapper, selects the actual `:gravity/sh07-canonical-core-artifact`, resolves
every controlled reference, and emits the exact normalized key vector above.

The v18 compatibility projection contains exactly the mapped v16 keys and
artifact identity literals above, and no B51 slot, extraction, runtime,
provenance, verifier, or final fields. Both sides are compared after the same
normalization. `:missing-fields`, `:unexpected-fields`, and `:delta` must all
be empty and `:equal?` must be true. Every node, definition, evaluation,
control-flow, reference, var, mutation, error, match, call, function, edge,
recursion, keyword lookup, lexical/loop binding, recur target and transfer,
source map, module assembly, origin, and pending-fact field is therefore
preserved value-for-value. Simple-symbol slots are the degenerate V18 metadata
extension and do not alter the v16 view.

## Seven-tier digest DAG and provenance

Tier 0 uses the 0a/0b/0c/0d handshake above. Tier 1 contains slot and
extraction ids. Tier 2 contains the complete ordered slot/extraction
transcript, ordinal reconstruction, visibility, checks, publications, and
recur mappings. Tier 3 `:gravity/sh07-b51-core-identity-v18` contains the
neutral authority, all resolved semantic products, v18 adapter/domain/schema,
and origins with physical fields removed. Tier 4 contains the semantic id,
physical project root/checkout/source paths, exact bytes, B47 observation,
source spans, attempt/session facts, and complete artifact/report/diagnostic
payload. Tier 5 is the independent verifier binding. Tier 6 is the terminal
artifact binding and literal output kind.

Every preimage has a closed key set, dense ordinal, literal domain/schema, and
ancestor-only references. Deleting, inserting, duplicating, reordering,
substituting, or adding self/descendant/cross-tier edges fails `C6-VERIFY`.
Semantic ids for byte-identical `.gravity`/`.qst` fixtures are equal while
their physical provenance ids differ. Paths and diagnostic payloads never
enter Tier 0-3 semantic preimages.

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
diagnostic/form, owning top-level roots, all v18 slots/nodes/extractions,
binding authentication, nested widths, visibility/order, runtime checks,
recur slot mappings and atomic transfer, complete semantic product closure,
and Tier 0-4 preimages. It compares the accepted branch's exact v16
compatibility record or the rejected branch's exact frontier classification.
Transported status, artifact, rejection, diagnostic, expected maps, and ids
are values to check, never authority.

Authenticated external bindings are allowed only when the SH-06 resolution
table marks the binding as an exact external core/catalog/declared binding,
with matching fragment id, namespace, profile, visibility, source revision,
and projection binding. The verifier checks the external binding's immutable
semantic id and permitted edge kind. A cross-fragment edge without that exact
authenticated record, a wrong namespace/profile/visibility, a substituted
binding id, or a path-only match is unauthorized and fails `C6-VERIFY`; V18
does not blanket-reject legitimate authenticated externals.

## Bounds, diagnostics, and evidence

V18 enforces at most 1,024 slots per module/owner, 1,024 pattern nodes per
slot, 65,536 extractions per module, 2,048 leaves per module, vector depth 256,
vector width 1,024, and path length 256, with saturating preflight before
allocation. Stable diagnostics are `C6-CORE-SHAPE`, `C6-LOWERING-GAP`,
`L7-DUP-BINDING`, `L7-PATTERN-TYPE`, `C6-EVAL-ORDER`, `C6-ORIGIN`,
`L2-RECUR-TARGET`, and `C6-VERIFY`. The original B47 frontier diagnostic is
transported unchanged; it is not sanitized or relabeled.

Evidence must include byte-identical `.gravity`/`.qst` pairs for accepted
legacy simple-symbol programs (B47/V18 exact empty delta), rejected-frontier
vector `let`/`loop` programs accepted only by V18, nested/empty/wildcard
patterns, runtime non-vector and wrong-width failures, successful and failing
recur transfers, and every unsupported/malformed/duplicate/unauthorized
external case. Mutation probes must swap branches, attach an artifact to a
rejection, attach a rejection to an accepted branch, alter pins/closure
members, change paths/session facts in semantic preimages, delete/reorder
products or ordinal records, forge external bindings, add self/descendant
edges, or coordinate producer and expected-result mutations. Every probe must
fail closed and no host exception may be passing evidence.

## Atomic implementation and integration

After independent architecture acceptance, implementation remains one stack
over M:

```text
M -> C (sh-core v18 semantics, products, verifier) -> H (coordinator)
```

C may add disjoint v18 definitions to `checked_core.gravity` and paired
fixtures/tests. C must not change the measured B47 298-function closure or the
three contract forms; its own combined source receives fresh whole-file v18
source-byte, source-hash, plan, and function pins in H. Those fresh pins are
physical v18 provenance and do not replace the immutable closure descriptor.
C owns no coordinator files and is never integration-eligible alone.

H is an exact child of C. H may update only mechanical v18 invocation,
measured source/closure/contract census, carrier transport, and lifecycle
records. Before invocation H authenticates the immutable closure and contract
records, then mechanically runs the three genuine v16 roots on the exact
authenticated request and transports their actual outcome. It cannot implement
vector semantics or synthesize products. C and H each require independent exact
review. Main remains M until H, then advances once from M to H. Any intervening
main change, closure drift, source identity change, or review rejection requires
a fresh tuple.

## Ownership, residual boundaries, and nonclaims

`:sh-core` owns v18 pattern semantics, complete product schemas, compatibility
projection, and independent verification. `:master-coordinator` owns only
mechanical B47 invocation, closure/contract pin transport, opaque digest
resolution, physical provenance, and final assembly. Clojure/JVM remain source
reader, strict decoder, SH-06/B47 host, plan executor, digest resolver,
runtime-check host, and observer. The verifier is evidence and is not a proof
of itself.

This decision does not claim map/list/set/record/constructor/schema/resource or
variable-width patterns, defaults/rest/guards, parameter destructuring, general
match coverage, type/effect/ownership/safety completion, MIR/optimization,
complete exception semantics, public routing, aggregate SH-07 completion,
self-hosting, release, performance, or seed retirement. It does not modify
B47 behavior, v17 history, attempt-1 history, or WIP authority.

## Independent acceptance criteria

An independent reviewer must inspect this exact clean candidate and confirm:

1. The v17 contradiction and the exact attempt-1 rejection tuple are real and
   preserved without relabeling.
2. The carrier is the closed schema-18 domain and transports one exact fresh
   SH-06 request/report plus actual same-request B47 outcome.
3. B47 semantic identity-preimage, actual core artifact, wrapper, and v18
   output kinds are distinct literal values.
4. The measured predecessor closure has exact roots, 298/305 count, member,
   names, shapes, referenced builtin-name/call-site, contract-form, and
   descriptor hashes; closure drift fails before invocation. The C whole-file
   v18 pin is separate.
5. The accepted/rejected semantic outcome projections are closed, own-id-free,
   path-neutral, and exclude complete physical artifact/report/diagnostic
   payloads; those payloads bind only Tier 4.
6. The 0a/0b/0c/0d handshake is acyclic, ordinalized, and all preimages refer
   only to earlier subtiers/tiers.
7. Template, resolved core, canonical output artifact, and wrapper have the
   exact closed schemas above; Tier 6 names the literal output kind.
8. Accepted legacy requests compare every listed v16 product through the exact
   field mapping and normalized empty-delta record.
9. V18 independently reconstructs Tier 0-4 and accepts only authenticated
   external bindings while rejecting unauthorized cross-fragment edges.
10. Slot/extraction products retain empty vectors, all ordinals, origins,
    exact runtime width, simultaneous visibility, slot-based recur arity, and
    atomic transfer.
11. Bounds, diagnostics, evidence, replay mutations, provenance separation,
    and host exception containment are executable and complete.
12. M -> C -> H is atomic, C cannot land alone, H is an exact child, both
    tuples receive independent review, and main advances once only to H.
13. v17, attempt 1, and WIP retain terminal meanings; this report grants no
    implementation, integration, roadmap, or SH-07 completion authority until
    its own independent lifecycle reaches acceptance and integration.

The author may report defects but cannot accept this decision or confer
integration eligibility.
