# SH-07 B51 Git-Object Publication V9 Pre-Evidence Erratum V1

Status: draft architecture correction; report only; no implementation authority

## Purpose

This report corrects two pre-evidence contradictions in the independently
accepted and integrated SH-07 B51 Git-object evidence-publication architecture
V9. It preserves V9's process isolation, canonical codec, immutable Git-object
reads, publication topology, later I9/G9/P9 lifecycle, fixed-fuel semantics,
all unrelated V9 requirements, and all authority nonclaims. It changes the
pre-Q A9 and masked-source inputs and every downstream closed-schema projection
of those inputs.

The first contradiction names an A9 lifecycle receipt path that is absent from
the accepted candidate, authoritative main, and all Git history. The second
requires Q and PRE to contain postimage values that can exist only after the
fresh A/B/M execution and later publication phases. Both conditions make an
authentic I9 candidate impossible and must fail closed rather than be
fabricated.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-pre-evidence-erratum-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-pin-free-publication-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-pre-evidence-erratum-v1.md",
  "status": "draft",
  "base_commit": "8c39712c80e7da66c23a23a6c1f01ba1f970b682",
  "dependencies": [
    {
      "id": "sh07-b51-git-object-evidence-publication-architecture-v9",
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

## Reproduced contradictions

At authoritative main `8c39712c80e7da66c23a23a6c1f01ba1f970b682`,
V9 defines its three A9-only PRE paths as the architecture report, provisional
companion report, and
`docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/a9-lifecycle-receipt.edn`.
The first two blobs exist. The lifecycle receipt does not exist in the tree,
and `git log --all --` for that exact path is empty. The content-addressed
terminal A9 workstream record does exist at:

```text
contracts/workstream-records/terminal/sh07-b51-git-object-evidence-publication-architecture-v9-461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1.json
```

I9 owns neither the missing review path nor the terminal record. It cannot add
or infer the missing A9 input without violating ownership and immutable-object
reconstruction.

V9 also requires the eligibility receipt and PRE discharge to contain the
complete `MASKED-ROOT`, including `:postimage-spans`, `:postimage-files`, and
`:postimage-root`. Those postimage spans are defined from the four final digest
RHS values and the quoted final carrier. Those values arise only from the
fresh A/B/M execution and later evidence/publication lifecycle. V9 separately
forbids Q and PRE from containing a runtime or future value. The two rules are
simultaneously unsatisfiable.

## Corrected A9 immutable input and counts

The V9 `A9-PATHS` allocation is replaced by `A9-PATHS-E1`, exactly the two
report literals followed by the A9 governance path specification:

```clojure
["docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md"
 "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md"
 {:kind :content-addressed-terminal-record
  :directory "contracts/workstream-records/terminal"
  :workstream-id "sh07-b51-git-object-evidence-publication-architecture-v9"
  :record-digest-domain :sha256
  :filename-rule :workstream-id-hyphen-record-sha256-dot-json}]
```

`A9-ONLY-PATHS-E1` is the following complete concrete three-string vector and
is the literal PRE request input known before any of the 77 operations begins:

```clojure
["docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md"
 "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md"
 "contracts/workstream-records/terminal/sh07-b51-git-object-evidence-publication-architecture-v9-461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1.json"]
```

After the requested blobs have passed `cat-file` validation, the third string
must equal the resolution of `A9-PATHS-E1` through the authenticated ledger
manifest entry and terminal-record bytes. It is never derived dynamically to
construct the already-running Git argv.

The distinct unsigned-UTF-8 union remains exactly 72 paths, so the PRE plan
remains exactly 77 operations and 63 strict decodes. A9 contributes exactly
the two report descriptors plus the terminal JSON descriptor. I9 never creates
the absent receipt path.

## Exact terminal-derived A9 stage replacement

The V9 `A9-LIFECYCLE`, `A9-RESULT`, their external receipt/domain, their
reservation/handoff/cleanup rules, the PRE carrier key `:a9-lifecycle`, and
every downstream reference to those values are replaced. No field is parsed
from `:disposition`, `history[*].reason`, validation-command strings,
residual-boundary prose, or reviewer-name suffixes.

`A9-ARTIFACT-DESCRIPTORS-E1` is the dense three-vector of C/T descriptors for
the two reports and terminal JSON in `A9-ONLY-PATHS-E1` order. Each descriptor
has exactly `:path :mode :type :blob-oid :byte-count :raw-sha256`.
`A9-TERMINAL-COORDINATE-E1` adds exactly `:commit :tree` to the terminal
descriptor. `A9-TERMINAL-MANIFEST-ENTRY-E1` is the exact eight-key ledger row.
`A9-TERMINAL-RECORD-E1` is the complete strict canonical JSON object.

`A9-STAGE-RESULT-E1` has exactly:

```clojure
{:schema :gravity/sh07-b51-a9-stage-result-e1
 :stage :a9
 :content {:commit C :tree T}
 :a9-artifact-descriptors A9-ARTIFACT-DESCRIPTORS-E1
 :terminal-record
 {:coordinate A9-TERMINAL-COORDINATE-E1
  :manifest-entry A9-TERMINAL-MANIFEST-ENTRY-E1
  :record A9-TERMINAL-RECORD-E1}
 :checks
 {:canonical-json true
  :record-shape-valid true
  :filename-sha256-match true
  :manifest-entry-match true
  :workstream-id-match true
  :invariant-family-match true
  :architecture-decision-path-match true
  :owned-paths-match true
  :accepted-fixtures-equal-owned-paths true
  :base-commit-id-syntax-valid true
  :candidate-commit-id-syntax-valid true
  :candidate-differs-from-base true
  :clean-worktree-recorded true
  :independent-accepted-review-present true
  :history-transitions-valid true
  :history-final-state-match true
  :record-state-integrated true
  :authority-ceiling-match true
  :all-checks-pass true}
 :result :terminal-record-integrated
 :historical-claims
 {:candidate-bound-review false
  :candidate-tree false
  :integration-commit false
  :integration-tree false
  :candidate-integration-ancestry false
  :report-preservation-across-integration false
  :supporting-report-preservation-across-integration false
  :ownership-reservation false
  :ownership-handoff false
  :reservation-release false
  :worktree-removal false
  :branch-removal false
  :finally-entered false}
 :nonclaims NONCLAIMS}
```

The terminal record must have the exact workstream ID, invariant family,
architecture-decision path, owned paths, `evidence.accepted_fixtures` vector
equal byte-for-byte and in order to `owned_paths`, base and candidate commit-ID
syntax, clean-worktree flag, integrated state, authority ceiling, and
schema-valid lifecycle transitions. The two commit-ID syntax checks establish
only a 40-lowercase-hex recorded value; they do not authenticate object
existence, trees, or ancestry. `:candidate-differs-from-base` is exact textual
inequality. `:independent-accepted-review-present`
means only that at least one structured review row has kind `independent`,
result `accepted`, and reviewer unequal to the record author. It deliberately
does not claim a candidate-bound review because the generic row lacks that
field. Every named check is computed from closed JSON fields, exact file bytes,
and the governance validator. `:all-checks-pass` is true if and only if the
preceding 18 check values are all true. Every key in the exact
`:historical-claims` map is false because the terminal record does not
authenticate any of those facts.

`A9-STAGE-RESULT-E1` replaces the PRE carrier with key `:a9-stage-result` and
replaces A9's member in every contract row, binding spec, phase-evidence map,
and lifecycle vector. `GOVERNANCE-RESULTS-E1` is the explicitly heterogeneous
`[A9-STAGE-RESULT-E1 I9-RESULT G9-RESULT P9-RESULT]`; the last three retain the
original `GOVERNANCE-RESULT` schema. `FINAL-LIFECYCLE-E1` has exactly
`:governance-results :a9-terminal-record-integrated
:i9-g9-p9-integrated :final-cleanup`; its values are the heterogeneous vector
and three true booleans. Final cleanup is derived only from the exact I9, G9,
and P9 results. It makes no claim for any A9 fact whose exact
`:historical-claims` value is false.

The first milestone is renamed from `:a9-architecture-integrated` to
`:a9-terminal-record-integrated`; phase prefix lengths remain `[1 4 6 7 9]`.
The old A9 lifecycle artifact tag is removed from `V9-TAG-REGISTRY`, and its
old hash domain is removed from `H-DOMAIN-REGISTRY`.
`:gravity/sh07-b51-a9-stage-result-e1` is added to `V9-TAG-REGISTRY` only. It
has no independent H identity. Any old A9 lifecycle receipt, generic A9
governance result, preservation root, ownership result, or cleanup projection
is forbidden.

At authoritative main the three descriptors are fixed evidence, not examples:

| Path | Blob OID | Byte count | Raw SHA-256 |
| --- | --- | ---: | --- |
| `docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md` | `1eef3f25b18b941deed09bdfb178573f08f7928e` | 393221 | `sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd` |
| `docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md` | `fc36dc37d4e45adc7255eb531ae8d312d9eb51da` | 4977 | `sha256:7401846ab6892cc32d4ba4f0d8d0c87be531575d75abc88852f6516cd769ae4d` |
| terminal A9 JSON above | `ddae151f339068955ff9f7df75c975e697962bd6` | 13404 | `sha256:461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1` |

The terminal manifest entry has dependencies equal the exact five integrated
A9 dependency IDs, workstream ID and invariant family equal the literals above,
ordinal 161, the concrete terminal path above, SHA-256
`461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1`,
state `integrated`, and terminal true. C/T must reproduce all three descriptors
and this manifest row or reject.

## Stage-scoped ownership and cleanup

The two global ownership booleans in `V9-OWNERSHIP-PLAN` are replaced by exact
stage scopes. `V9-OWNERSHIP-PLAN-E1` has exactly:

```clojure
{:schema :gravity/sh07-b51-v9-ownership-plan-e1
 :allocations [[0 :a9 :master-coordinator A9-PATHS-E1]
               [1 :i9 :master-coordinator I9-LIFECYCLE-PATHS]
               [2 :e9 :master-coordinator E9-PATHS]
               [3 :g9 :master-coordinator G9-LIFECYCLE-PATHS]
               [4 :p9 :master-coordinator P9-LIFECYCLE-PATHS]]
 :semantic-review-owner :sh-core
 :reservation-required-stages [:i9 :e9 :g9 :p9]
 :handoff-required-stages [:i9 :g9 :p9]
 :read-only-prerequisite-stages [:a9]
 :read-only-paths READ-ONLY-PATHS
 :open-exit-gates [:core :evaluation :arity :mutation :recursion
                   :exception :pattern]
 :aggregate-sh07-state :partial
 :self-id-omits [[:plan-id]]
 :plan-id PLAN-ID-E1}
```

Each allocation expands to the same exact four-key maps and uses the same
literal or governance-path-spec grammar as V9. `READ-ONLY-PATHS` and every
non-A9 allocation are unchanged. Allocation 0 attributes the immutable A9
prerequisite paths only; it is not a reservation, handoff, ownership-result, or
cleanup claim. `PLAN-ID-E1` is
`H([:gravity/sh07-b51-v9-ownership-plan-e1
PLAN-WITHOUT-PLAN-ID])`. The old ownership-plan tag and H domain are removed
from both registries; `:gravity/sh07-b51-v9-ownership-plan-e1` is added to both.

The generic `OWNERSHIP-RESULT` remains normative only for I9, G9, and P9, the
three stages for which V9's row plan selects an authenticated ownership result
and handoff. E9 requires a publication reservation, but has no handoff field or
`OWNERSHIP-RESULT`; its reservation, failure cleanup, immutable freeze, and
object verification remain governed by the launcher/runtime and I9 lifecycle
rules. E9 has no standalone governance-result or final-cleanup slot. No A9 or
E9 `OWNERSHIP-RESULT` may be fabricated.

`GOVERNANCE-RULES-E1` replaces the global final-cleanup boolean. It has exactly
`:permitted-transitions :admission-requirements :external-discharge-order
:final-cleanup-required-stages :aggregate-sh07-state`. Every value is unchanged
except `:final-cleanup-required-stages`, which is exactly `[:i9 :g9 :p9]`.
Consequently, `FINAL-LIFECYCLE-E1` cleanup and the post-P9 final row require
the exact I9, G9, and P9 cleanup results only. Neither a final row nor
`ALL-OBLIGATIONS-RESOLVED` demands an A9 reservation, handoff, ownership
result, or cleanup fact. E9 launcher cleanup remains mandatory through its
unchanged runtime binding and is not weakened by this stage scope.

## Acyclic masked-source split

The V9 pre-evidence `MASKED-ROOT` is replaced by `PRE-MASKED-ROOT`. It has
exactly:

```clojure
{:coordinates PRE-COORDINATES
 :preimage-spans PREIMAGE-SPANS
 :preimage-files PREIMAGE-FILES
 :preimage-root PREIMAGE-ROOT}
```

The five coordinates, delimiter checks, adjacency bytes, source paths, load
ordinals, form ordinals, owners, RHS kinds, mask bytes, and preimage span/file
algorithms are unchanged from V9. Every RHS at C must be exactly
`:gravity/provisional-no-authority`. `PREIMAGE-ROOT` retains V9's
`:gravity/sh07-b51-masked-source-root-v1` domain over the closed coordinates
and two masked preimage file descriptors. PRE contains no postimage span,
postimage file, final digest, final carrier, E9 blob, or descendant identity.

The Q eligibility receipt key formerly named `:masked-root` is renamed
`:pre-masked-root`. The PRE discharge evidence carrier key is likewise
`:pre-masked-root`. The corresponding eligibility boolean is
`:pre-masked-root-match`; its meaning is byte-identical equality between the
strictly decoded receipt value and an independent C/T reconstruction. The
per-participant counter is renamed
`:pre-discharge-pre-masked-file-count` and remains two.

This rename replaces every pre-evidence closed schema, not only the producing
receipt. The evidence-eligibility receipt exact key set contains
`:pre-masked-root` and not `:masked-root`. `ELIGIBILITY-CHECK` contains
`:pre-masked-root-match` and not `:masked-root-match`. The runtime
`EVIDENCE-ELIGIBILITY` carrier contains `:pre-masked-root` between
`:source-descriptors-root` and `:pre-discharge-id`; it must equal Q's complete
authenticated value. `ELIGIBILITY-COUNTERS` contains
`:pre-discharge-pre-masked-file-count 8` and
`:pre-masked-root-match-count 4`; it contains neither superseded counter name.
The runtime phase-evidence and `RUNTIME-CARRIERS` key is renamed
`:runtime-masked-root`, whose value is the complete `RUNTIME-MASKED-ROOT`.
G9 and P9 retain their serialized `:masked-root` fields and bind that same
complete runtime value. No schema contains both pre and full values under an
ambiguous name.

After the fresh A/B/M execution has produced all four digest RHS values and the
quoted carrier, runtime constructs `RUNTIME-MASKED-ROOT` with exactly V9's
original complete key set:

```clojure
{:coordinates COORDINATES
 :preimage-spans PREIMAGE-SPANS
 :postimage-spans POSTIMAGE-SPANS
 :preimage-files PREIMAGE-FILES
 :postimage-files POSTIMAGE-FILES
 :preimage-root PREIMAGE-ROOT
 :postimage-root POSTIMAGE-ROOT
 :equal true}
```

Its preimage projection must equal Q's authenticated `PRE-MASKED-ROOT`
byte-for-byte. Its postimage values are authenticated only in the runtime
discharge and later lifecycle joins. `:equal` is true only when the independently
recomputed preimage and postimage roots are equal. No runtime or postimage value
is moved backward into C, PRE, or Q.

## Normative V9 supersession map

The following map is exhaustive. Line spans name the immutable V9 report at
authoritative main `8c39712c80e7da66c23a23a6c1f01ba1f970b682`; text outside
these spans remains normative. A replacement is limited to the clause named in
the right column unless the row says the span is replaced completely.

| V9 lines or unique clause | E1 replacement |
| --- | --- |
| 47-139, the Q receipt through its PRE reconstruction | Rename the receipt and eligibility pre-mask projections and counters to the exact E1 names; replace its A9 input/result projection. Q topology and five Git operations remain unchanged. |
| 155-170, source/PRE path and count derivation | Use `A9-ONLY-PATHS-E1`; retain exact counts 3/72/77/63. |
| 228-273, the A9 bullet under "A9 and P9 also have closed external lifecycle receipts" | Replace the A9 lifecycle-receipt bullet completely. The P9 bullet beginning at line 274 remains. |
| 335-345, P9 final-discharge paragraph | Values behind `:governance-results` and `:final-lifecycle` become the E1 values; carrier keys and serialized post-runtime `:masked-root` remain. |
| 495-523, `OWNERSHIP-RESULT` and generic `GOVERNANCE-RESULT` | Retain for I9/G9/P9 only; they do not apply to A9. E9 publication evidence remains in the runtime/I9 lifecycle channels defined above. |
| 524-539, `A9-RESULT` through `FINAL-LIFECYCLE` | Replace completely with `A9-STAGE-RESULT-E1`, `GOVERNANCE-RESULTS-E1`, and `FINAL-LIFECYCLE-E1`. |
| 541-551, `AUTHORITY` milestones | Replace only the first milestone with `:a9-terminal-record-integrated`; prefix lengths remain `[1 4 6 7 9]`. |
| 1357-1361, identity-domain table | Delete the A9 lifecycle receipt-ID row. |
| 1363-1485, `V9-TAG-REGISTRY` | Replace the old A9 lifecycle tag with the A9 stage-result tag; replace the old ownership-plan tag with the E1 ownership-plan tag. |
| 1487-1599, `H-DOMAIN-REGISTRY` | Remove the old A9 lifecycle H domain with no stage-result replacement; replace the old ownership-plan H domain with the E1 ownership-plan H domain. |
| 1730-1738, child request eligibility derivation | Derived pre-mask name becomes `pre-masked-root`. |
| 2240-2260, `PRE-EVIDENCE-CONTRACT-DISCHARGE` | Replace `:masked-root` with `:pre-masked-root`, `:a9-lifecycle` with `:a9-stage-result`, and bind the exact E1 values. |
| 2262-2290, `RUNTIME-CONTRACT-DISCHARGE` | Rename the full runtime carrier to `:runtime-masked-root`; nested `EVIDENCE-ELIGIBILITY` uses `:pre-masked-root`. |
| 2417-2429, `A9-LIFECYCLE` | Replace completely with the terminal-derived A9 stage result. |
| 2431-2454, `V9-OWNERSHIP-PLAN` | Replace with `V9-OWNERSHIP-PLAN-E1`, including `A9-PATHS-E1` and exact stage-scoped ownership applicability. |
| 2456-2467, `GOVERNANCE-RULES` | Replace with `GOVERNANCE-RULES-E1` and exact I9/G9/P9 governance cleanup scope. |
| 2469-2485, carrier pools | PRE uses `:a9-stage-result`; runtime uses `:runtime-masked-root`. |
| Unique binding `:agents-final-lifecycle` at 2535-2536 | Selector remains; selected value is `FINAL-LIFECYCLE-E1`. |
| Unique binding `:governance-p9` at 2816-2817 | Selector remains; selected value is `GOVERNANCE-RESULTS-E1`. |
| 6350-6365, G9 tuple literal | Serialized key remains `:masked-root`; its value is `RUNTIME-MASKED-ROOT`. |
| 6393-6434, complete `MASKED-ROOT` schema | Symbolic name becomes `RUNTIME-MASKED-ROOT`; its eight-key schema and equality algorithm remain unchanged. |
| 6509-6529, codec and eligibility counters | Use the exact E1 per-process and aggregate pre-mask counter names; all numeric values and other counters remain unchanged. |
| 6795-6806, exact `A9-PATHS` literal | Replace completely with `A9-PATHS-E1` and its concrete three-string PRE request vector. |
| 6922-6930, final review obligations | Require the stage-specific A9 result plus generic I9/G9/P9 results; do not require historical A9 ownership or cleanup proof. |

For avoidance of doubt, the P9 receipt at lines 274-301, post-P9 serialized
`:masked-root`, full-mask prose at lines 696-704, and the G9/P9 serialized
`:masked-root` keys are not superseded. Each binds `RUNTIME-MASKED-ROOT`.

## Preserved V9 requirements

All requirements not explicitly replaced above remain byte-for-byte normative,
including:

- exactly one L-created M process and the ordered fresh A, B, then M sessions;
- exact 39-string M argv and the two exact checker argv vectors;
- exact public codec functions `H`, `W`, and `R`;
- the five-operation Q check by L, M, A, and B;
- exact 12-source descriptors, 39 source events, and 12/12/39/1 counters;
- exact 77-operation PRE observation count and 33-document session;
- exact contract row plan, predicate specifications, phase discharges, and
  identity-cycle rejection, subject exactly to the E1 replacements in the
  normative supersession map;
- exactly six M-staged payloads plus identity receipt written last;
- Git-object-only existing evidence replay and zero final writes;
- fixed-fuel owner selection, authenticated external binding acceptance, and
  unauthorized cross-fragment rejection; and
- all implementation, aggregate SH-07, self-hosting, release, performance,
  hostile-host, and seed-retirement nonclaims.

This report adds no implementation path, evidence blob, fixture, receipt,
review, pin, or completion authority.

## Implementation and lifecycle consequence

The existing draft I9 implementation must not freeze against uncorrected A9.
After this erratum is independently accepted and integrated, I9 must reconcile
onto that authoritative main, add this erratum as an integrated dependency,
and implement `A9-PATHS-E1`, `A9-ONLY-PATHS-E1`,
`A9-STAGE-RESULT-E1`, `V9-OWNERSHIP-PLAN-E1`, `GOVERNANCE-RULES-E1`,
`FINAL-LIFECYCLE-E1`, `PRE-MASKED-ROOT`, and `RUNTIME-MASKED-ROOT` exactly.
Any implementation that creates the absent A9
receipt, accepts a missing C path, inserts a placeholder postimage, copies a
future value into Q/PRE, parses governance prose into evidence, claims an A9
historical detail whose exact `:historical-claims` value is false, retains an
old A9 lifecycle or generic governance result, applies reservation, handoff, or
cleanup requirements to read-only A9, or grants authority from a partial
carrier rejects.

## Conformance and acceptance criteria

This architecture correction is acceptable only if a fresh independent review
confirms that:

1. the missing A9 path is absent at authoritative main and throughout Git
   history;
2. the named terminal A9 record exists, authenticates its filename digest, and
   is a valid closed governance record whose structured history and terminal
   state end in `integrated`;
3. the corrected three-path input preserves exactly 72 PRE paths, 77 PRE
   operations, three A9 artifacts, and 63 strict decodes;
4. `PRE-MASKED-ROOT` is constructible solely from C/T and contains no runtime,
   E9, final digest, final carrier, or descendant value;
5. `RUNTIME-MASKED-ROOT` preserves V9's full pre/post equality proof without
   moving a future value backward into PRE;
6. every pre-evidence eligibility field and counter uses the exact pre-mask
   name while runtime, G9, and P9 retain the complete masked-root name;
7. A9's old lifecycle/result schemas are fully replaced by exact
   terminal-derived values, every unavailable historical claim has value false
   in the exact `:historical-claims` map, and no prose is parsed into evidence;
8. ownership and final cleanup are stage-scoped exactly as E1 requires, with
   no A9 historical result and no weakening of E9 launcher cleanup;
9. every row in the normative supersession map is implemented exactly and no
   intentionally retained serialized `:masked-root` field is renamed;
10. every unchanged V9 topology, schema, publication, fixed-fuel, evidence, and
   lifecycle requirement remains normative; and
11. the candidate is report-only, passes architecture/governance/document
   validation, receives independent acceptance, and grants no implementation
   or aggregate SH-07 authority.
