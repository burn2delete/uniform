# SH-07 B51 Git-Object Evidence Publication Architecture V9

Status: draft architecture only. Base is main
`09c795f0c0854d2d8ab542c92b2116515e096406`, tree
`4d0182601a2b105190c86ca607e24222078c9771`. This report defines no
implementation, acceptance, integration, release, self-hosting,
seed-retirement, hostile-host, or SH-07 completion claim.

```json gravity-architecture-authority-v1
{"schema":"gravity/architecture-authority-v1","workstream_id":"sh07-b51-git-object-evidence-publication-architecture-v9","invariant_family":"architecture/self-hosting-sh07-b51-pin-free-publication-v1","report_path":"docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md","status":"draft","base_commit":"09c795f0c0854d2d8ab542c92b2116515e096406","dependencies":[{"id":"sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32","required_state":"integrated"},{"id":"sh07-b51-first-eval-nested-validation-architecture-v3","required_state":"integrated"},{"id":"sh07-b51-binding-replay-index-architecture-v1","required_state":"integrated"},{"id":"sh07-b51-binding-replay-index-implementation-review-erratum-v1","required_state":"integrated"},{"id":"c2-pass-cache-capacity-withholding-v1","required_state":"integrated"}],"historical_references":[],"authority":{"integration_only":true,"release":false,"self_hosting":false,"seed_retirement":false}}
```

## Purpose and lifecycle

Attempt 32 needs evidence whose source bytes, checker executions, carrier, and
publication transition are independently reviewable without trusting a dirty
worktree, branch name, mutable ref, or already-loaded checker. V9 makes Git
objects the review source, makes checker A and checker B separate fresh JVM
processes, and admits the final literals only after a separately governed
publication tuple is accepted and integrated.

The lifecycle is strict and the words freeze, eligible, accepted, and
integrated are not interchangeable:

1. A9 content is frozen at one immutable report commit. A fresh independent
   architecture review may accept that exact tuple. Separate admission moves
   accepted A9 to `integration-eligible`; only then may A9 be integrated.
2. Integrated A9 permits an I9 implementation candidate. Freezing I9 fixes C
   and T. A separate independent pre-evidence review may mark that exact
   content **evidence-eligible**; it does not accept or integrate I9.
3. Only evidence-eligible I9 may create E9. E9 is a direct sole-parent child of
   C and adds exactly seven evidence blobs. E9 itself grants no acceptance.
4. A fresh independent implementation review replays exact C/T and E9 object
   evidence. Only that review may accept I9; only accepted I9 may be
   admitted as `integration-eligible` and then integrated. Integration produces
   exact commit B/tree TB, proves C is an ancestor of B, and preserves every
   I9-owned C blob byte-for-byte while reconciling unrelated latest-main state.
5. Integrated I9 permits G9. G9 is a direct sole-parent child of B, retains E9
   as the immutable evidence lineage anchored at C, and adds only the publication tuple. A
   post-G9 independent review and lifecycle record bind G9 externally as
   specified below. Separate admission moves accepted G9 to
   `integration-eligible`; only then may G9 be integrated.
6. Integrated G9 permits P9. P9 alone replaces the five provisional literals;
   it has its own freeze, independent review, acceptance,
   `integration-eligible` admission, and integration.

Two review receipts are external to the C/E/B/G content-addressed lineage and
therefore cannot create a commit/tree self-reference:

- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn`
  has exactly `:artifact :schema-version :content :source-descriptors
  :source-descriptors-root :masked-root :contract-discharge :review :state
  :self-id-omits :nonclaims :receipt-id`. Artifact is
  `:gravity/sh07-b51-evidence-eligibility-receipt-v1`, version is 1, content is
  `{:commit C :tree T}`, state is `:evidence-eligible`, and receipt ID is
  `H([:gravity/sh07-b51-evidence-eligibility-receipt-v1 RECEIPT-WITHOUT-ID])`;
  self-id-omits is exactly `[[:receipt-id]]`.
  Source descriptors are the exact closed twelve-DESCRIPTOR vector later
  embedded in E9; source root is
  `H([:gravity/sh07-b51-source-descriptors-root-v1 SOURCE-DESCRIPTORS])`;
  masked root is the complete MASKED-ROOT map; and contract discharge is the
  complete CONTRACT-DISCHARGE map. Every nested C/T equals content C/T, and E9
  must reproduce all four values byte-for-byte rather than merely copying
  their root strings.
  It is published only by the pre-evidence review lifecycle after C freezes;
  it is neither an E9 payload nor an ancestor of E9.
- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-receipt.edn`
  has exactly `:artifact :schema-version :content :evidence :publication-base
  :ancestry-observations
  :content-is-ancestor :evidence-is-ancestor-of-base :owned-c-paths :preserved-blobs :preservation-root
  :absent-paths-at-base :evidence-paths-absent-at-base :evidence-eligibility-id :review
  :state :self-id-omits :receipt-id`.
  Artifact is `:gravity/sh07-b51-i9-integration-receipt-v1`, version is 1,
  content is `{:commit C :tree T}`, evidence is
  `{:commit E :tree TE :parents [C]}`, publication base is
  `{:commit B :tree TB :parents BASE-PARENTS}`. `:content-is-ancestor` is the
  C/B true ANCESTRY-RESULT and `:evidence-paths-absent-at-base` is true.
  `:evidence-is-ancestor-of-base` is the E/B false ANCESTRY-RESULT, established by an exact
  `merge-base --is-ancestor E B` exit-1 observation. `ancestry-observations`
  is exactly the ordered `[C/B-true E/B-false]` ANCESTRY-RESULT vector.
  State is exactly `:integrated`.
  Self-id-omits is exactly
  `[[:receipt-id]]`.
  `OWNED-C-PATHS` is the exact sorted I9 implementation/test/fixture union
  below, sorted by unsigned UTF-8 path bytes with no duplicate. Each
  `PRESERVED-BLOB` has exactly `:path :content-commit :content :base-commit
  :base :equal`; content commit is C and base commit is B.
  `:content` and `:base` each have exactly `:mode :type :oid :byte-count
  :raw-sha256`, mode is `"100644"`, type is `"blob"`, OID is the Git SHA-1,
  raw SHA is `R(blob-bytes)`, and
  every content/base field is equal. `preservation-root` is
  `H([:gravity/sh07-b51-i9-preservation-root-v1 PRESERVED-BLOBS])`. The receipt also proves C is an ancestor of B and every E9
  and G9 evidence path is absent at B. `absent-paths-at-base` is the exact
  seven E9 paths followed by the G9 tuple path; each row has exactly `:path
  :commit :present :mode :type :oid`, equals path/B/false/nil/nil/nil, and is
  joined to the fixed-set ls-tree observation. It is published only after I9 is
  integrated and is external to B and G9.

In every receipt, `REVIEW` has exactly `:reviewer :candidate-commit
:candidate-tree :artifact-path :artifact-raw-sha256 :result :completed-at`.
Reviewer is a nonempty string; candidate commit and tree are 40 lowercase
hexadecimal Git IDs; artifact path is a safe repository-root-relative path
with no empty, `.` or `..` segment; and artifact raw SHA is `R(review-bytes)`.
The eligibility review candidate is exactly C/T, I9 is B/TB, G9 is G/TG,
A9 is its architecture content commit/tree, and P9 is P/TP. Result is the
required lifecycle result;
result is exactly `:evidence-eligible` for the eligibility receipt and
`:accepted` for A9, I9 integration, G9, and P9 receipts;
completed-at is the exact semantic timestamp `"1970-01-01T00:00:00Z"`; actual
review time exists only in the external governance lifecycle and never enters
a V9 receipt ID. Each receipt is exactly `W(v)`;
its raw blob descriptor is stored by the lifecycle record and is not inside
its own ID preimage.

The lifecycle relation fields `:content-is-ancestor`,
`:content-is-ancestor-of-base`, `:evidence-is-ancestor-of-base`,
`:evidence-is-ancestor-of-publication`, `:base-is-ancestor`, and
`:pin-is-ancestor` carry an `ANCESTRY-RESULT`, never a bare boolean. That type
has exactly `:ancestor :descendant :is-ancestor :exit-code`; its nested
`:is-ancestor` is the sole boolean, true uses exit 0, and false uses exit 1.
Its endpoints must match the field's named relation. Git observation
`:parsed-result` uses the same four-key type directly.

A9 and P9 also have closed external lifecycle receipts:

- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/a9-lifecycle-receipt.edn`
  has exactly `:artifact :schema-version :architecture :review :acceptance
  :admission :integration :self-id-omits :nonclaims :receipt-id`. Architecture has exactly
  `:content-commit :content-tree :content-parents :report`; report has exactly
  `:path :mode :type :blob-oid :byte-count :raw-sha256`, with mode `"100644"`,
  type `"blob"`, and raw SHA R(report bytes). Let AC/TA be the architecture
  content commit/tree and AP its exact one-element ordered parent vector; every
  element is a 40-lowercase-hex Git ID. Architecture equals
  `{:content-commit AC :content-tree TA :content-parents AP :report REPORT}`.
  Acceptance
  is `{:state :accepted}`, admission is `{:state :integration-eligible}`, and
  integration has exactly `:state :integrated :commit :tree :parents
  :main-base :content-is-ancestor :report-preservation :reconciled`.
  Let AI/TAI be the integration commit/tree. Main base has exactly `:commit
  :tree :parents`; its parents are an ordered vector of 40-lowercase-hex Git
  IDs, and integration parents are exactly the one-element vector containing
  main-base commit. Report preservation has exactly `:content :integration
  :equal`. Content has exactly `:commit :tree :path :mode :type :blob-oid
  :byte-count :raw-sha256` and equals AC/TA plus every REPORT field.
  Integration has that identical key set and equals AI/TAI plus the same path,
  mode, type, blob OID, byte count, and raw SHA. Equal is true iff all six
  non-commit/tree descriptor fields are pairwise equal; both raw SHAs are
  independently recomputed as R of the respective Git blob bytes, and both
  blob OIDs resolve those exact bytes.
  content-is-ancestor is the architecture-content/integration true
  ANCESTRY-RESULT AC/AI/true/0 and reconciled is true. Integration commit/tree
  are exactly AI/TAI. Artifact is
  `:gravity/sh07-b51-a9-lifecycle-receipt-v1`, version 1, review is REVIEW,
  self-id-omits is `[[:receipt-id]]`, nonclaims are the five V9 nonclaims, and receipt ID H-hashes the exact map
  with only itself omitted. The report blob at integration must equal the
  frozen report bytes.
- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-pin-publication-receipt.edn`
  has exactly `:artifact :schema-version :publication-base :pin-publication
  :replacements :masked-root :joins :review :acceptance :admission
  :integration :self-id-omits :nonclaims :receipt-id`. Artifact is
  `:gravity/sh07-b51-p9-pin-publication-receipt-v1`, version is 1;
  base is `{:commit G :tree TG :parents [B]}`; pin publication is
  `{:commit P :tree TP :parents [G] :base-is-ancestor ANCESTRY-RESULT}`, where
  the result is G/P/true/0. Replacements is the exact
  five-coordinate vector; each row has exactly `:path :load-ordinal
  :form-ordinal :owner :rhs-kind :pre-value :post-value :pre-raw-sha256
  :post-raw-sha256`. Both raw fields are R of the exact RHS byte span. Four post-values are strict H identities and the fifth is
  the exact quoted carrier; all pre-values are
  `:gravity/provisional-no-authority`. Masked root is exactly the G9
  `MASKED-ROOT` schema and recomputes the identical five coordinates, ten span
  observations, two preimage file descriptors, two postimage file descriptors,
  and H domain/payload; it cannot merely copy G9's root strings. Joins has exactly `:g9-lifecycle-id :e9-identity-receipt-id
  :evidence-eligibility-id :i9-integration-id`. Review is REVIEW; acceptance,
  admission, and integration states are respectively `:accepted`,
  `:integration-eligible`, and `:integrated`. Integration has exactly `:state
  :commit :tree :parents :pin-is-ancestor`, equals P/TP/[G] and the G/P true
  ANCESTRY-RESULT, and is
  rechecked after admission; nonclaims remain the five V9
  values; self-id-omits is `[[:receipt-id]]`. Receipt ID H-hashes the map with only itself omitted. An exact
  P9 diff must contain only the two component paths and must reconstruct all
  five rows; any other changed byte rejects.
  Replacement rows 0-4 have the same exact path, load ordinal, form ordinal,
  owner, and RHS kind as `MASKED-ROOT`. Rows 0-3 take post-values from pin-map
  keys `:expected-m3-id`, `:expected-certificate-a-id`,
  `:expected-certificate-b-id`, and `:expected-semantic-root`, respectively.
  Row 4 is exactly `(quote CARRIER)` from
  `linked-carrier-v4-candidate.edn`. The four digest values must equal
  `p15-s23-sh07-b51-v4-candidate-pin-map`.

V1-V8 have no V9 ancestry or authority. V8 is historical nonauthority:
content `76ea5d13a1bb20a8be199a6127d7b60d12e8e9df`, tree
`a177b038ddcddaa25c21ec2a7b0fc25d561deb5c`, review-pending lifecycle
`b51399c6154731ea795d0b63a3df5c82142110a7`, lifecycle tree
`da8c48b0c729f82f51686e82e73795cd40dad67a`, formal review SHA-256
`92d2f17c567579866bdbdea3e9d5f5e2ace1f4e667f9cdfbe6e953057e17bf54`,
rejection commit `a81ce75a`, terminalization commit `e0fa67fd`, and terminal
shard SHA-256
`52a64accb4df9a35238485a971ec97f1f56b36ecfebcd27f06c7bfd9e485b9cc`.

## Governing contracts and explicit discharge

V9 is governed by `AGENTS.md`; D0, D1, D2, D3, D6, D8, and D9; L1 and L2; C2, C6,
and C11; BOOT1 through BOOT8; TEST10, TEST11, and TEST13;
`docs/self-hosting-slice-backlog.md`;
`docs/self-hosting-slice-ownership.edn`;
`docs/workstream-governance.md`;
`contracts/workstream-governance.json`;
`contracts/verification-catalog.edn`;
`bootstrap/gravity/src/gravity/checked_core.gravity`; and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.
The lifecycle record must list every one of those inputs; aliases such as D1
or BOOT7 mean their owning inventory documents, not this report.

The lifecycle and E9 contract-discharge receipt contain one closed row for each
entry below. Every row has the exact path, applicable section names,
disposition, bound artifact/receipt/test IDs, and rejection diagnostic. An
unlisted row, blank binding, invented section, or disposition outside
`:applies`, `:preserved`, and `:not-applicable` rejects as
`SH07-B51-CONTRACT-DISCHARGE`.

| Exact governing path(s) | Applicable sections and disposition | Mandatory V9 binding | Rejection |
| --- | --- | --- | --- |
| `AGENTS.md` | Scope, Start Here, Source Of Truth, Implementation Principles, Validation, Working Discipline; `:applies` | normative-document priority, Clojure-only bootstrap boundary, ownership, evidence, validation, and no-overclaim rules | `SH07-B51-CONTRACT-DISCHARGE` |
| `docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md`; `002-d1-system-architecture-overview.md`; `003-d2-implementation-roadmap-and-milestones.md`; `004-d3-terminology-and-concept-model.md`; `007-d6-performance-philosophy-and-charter.md`; `009-d8-safety-philosophy-and-charter.md`; `010-d9-verifiability-and-mathematical-correctness-charter.md` | Requirements, artifacts, conformance; `:applies` | C/T/B/TB lineage, canonical-pipeline pass/fact/provenance records, safe-semantics nonclaims, evidence-kind labels, replay and invalidation IDs | `SH07-B51-CONTRACT-DISCHARGE` |
| `docs/phase-01-core-language/011-l1-surface-syntax-specification.md` | Source Units, Requirements, Outputs and Artifacts, Diagnostics, Conformance Criteria; `:preserved` | root-relative `.gravity` paths, UTF-8 bytes, source IDs, syntax/spans/origins/metadata; no grammar change | `SH07-B51-GIT-INPUT-BLOB` |
| `docs/phase-01-core-language/012-l2-core-language-semantics.md` | Core Forms, Evaluation Model, Requirements, Outputs and Artifacts, Diagnostics, Conformance Criteria; `:applies` | core semantics, left-to-right order, effects/capabilities/profile facts, core diagnostics and negative fixtures | `SH07-B51-EDGE-AUTHORITY` |
| `docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md` | Requirements, Outputs and Artifacts, Source Unit Record, Incremental Hashing, Diagnostics, Conformance Criteria; `:applies` | source bytes/encoding/root ID/relative path/options/features; source/token/form/syntax hashes, source map and diagnostics | `SH07-B51-GIT-SNAPSHOT` |
| `docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md` | Requirements, Outputs and Artifacts, Core Node Shape, Desugaring Trace, Evaluation Order, Core Verification, Diagnostics, Conformance Criteria; `:applies` | core AST, syntax-to-core map, trace, origins/facts/order, verifier report and mutations | `SH07-B51-EDGE-AUTHORITY` |
| `docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md` | Requirements, Outputs and Artifacts, Module Shape, Operation Schema, Metadata Tables, MIR Verification, Diagnostics, Conformance Criteria; `:not-applicable` | B5.1 ends at verified core; receipt proves no MIR, backend, or stage-support claim or artifact changed | `SH07-B51-CONTRACT-DISCHARGE` |
| `docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md` | Stage Outline, Requirements, Outputs and Artifacts, Conformance Criteria; `:applies` | exact stage, trusted inputs, compiler lineage, environment and conformance | `SH07-B51-TRUSTED-TCB` |
| `docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md` | Implemented Subset, Requirements, Outputs and Artifacts, Conformance Criteria; `:applies` | implemented/excluded documents, host dependencies, runtime assumptions, source/compiler/backend/dependency hashes | `SH07-B51-TRUSTED-TCB` |
| `docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md` | Migration Order, Requirements, Outputs and Artifacts, Conformance Criteria; `:preserved` | module inputs/outputs/effects/facts/tests, `:meta` profile, diagnostic compatibility; no stage advance | `SH07-B51-CONTRACT-DISCHARGE` |
| `docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md` | Module Rules, Requirements, Outputs and Artifacts, Conformance Criteria; `:applies` | closed module manifest, effects/capabilities, deterministic output, fact/provenance preservation, positive/negative/preservation tests | `SH07-B51-FORBIDDEN-VAR` |
| `docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md` | Matrix Columns, Requirements, Outputs and Artifacts, Conformance Criteria; `:preserved` | exact compatibility row and digest remain unchanged; no implied support or stage advancement | `SH07-B51-CONTRACT-DISCHARGE` |
| `docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md`; `209-boot7-self-hosting-validation-and-equivalence-plan.md`; `210-boot8-bootstrap-artifact-provenance-specification.md` | Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | bound already-loaded launcher L plus fresh M/A/B topology, stage comparison, TCB delta, immutable provenance receipts, cleanup and replay | `SH07-B51-CHECKER-PROCESS` |
| `docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md` | Oracles, Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | A/B/main/I9 oracle identity/applicability, profile/target/backend/runtime/numeric mode, compared observables, diagnostics, permitted divergence and reproducers | `SH07-B51-NORMALIZE-DIVERGENCE` |
| `docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md` | Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | assumptions, trusted basis, checker/version/input/claim IDs, invalidation and counterexamples; E9 labels these validation receipts, not formal proof | `SH07-B51-PROOF-SCOPE` |
| `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md` | Stages, Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:preserved` | exact stage/trusted inputs/generated artifacts/source/compiler/lock/target/output identities, expected deltas and TCB delta; no stage advancement | `SH07-B51-PROOF-SCOPE` |
| `bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn` | complete closed EDN contract; `:applies` only within its declared B47/B15 scope | active status, fresh authoritative process, nonauthoritative iteration cache, source/checked-core rechecks, verifier/catalog IDs, immutable receipts, bounds/cache key and cleanup | `SH07-B51-PROOF-SCOPE` |
| `docs/self-hosting-slice-backlog.md`; `docs/self-hosting-slice-ownership.edn` | Countable Backlog, Dependency Waves and Parallel Ownership, Progress Accounting, Outputs and Artifacts, Conformance Criteria; coordinator-owned, future-path-conventions, slice-owners; `:applies` | SH-07 remains partial; exact I9/E9/G9/P9 owner/path/reservation/handoff; core/evaluation/arity/mutation/recursion/exception/pattern exit gate remains open | `SH07-B51-OWNER` |
| `contracts/verification-catalog.edn`; `bootstrap/gravity/src/gravity/checked_core.gravity`; `docs/workstream-governance.md`; `contracts/workstream-governance.json` | complete closed records and Lifecycle/Admission; `:applies` | exact C object identities, catalog schema/version/authority, literal `sh07-checked-core` module and applicable route records, checked-core revision, lifecycle state transitions, review/admission receipts and final cleanup | `SH07-B51-VERIFY-CATALOG` |

The following obligations apply and cannot be discharged by a Git hash alone:

- L1/L2 and C6 require the original source order, authentic owning
  top-level definition/root, checked-core revision, core/error-exit shape,
  origin, and diagnostic semantics to survive lowering. V9 binds the exact C
  blobs for L1/L2, `checked_core.gravity`, the seven B5.1 modules, and compiler
  emitter, and carries their source IDs, spans, owners, roots, and fact digests
  through both independent checker products.
- C11 is explicitly not applicable because B5.1 ends at verified core. E9
  proves the absence of MIR/backend artifacts and makes no MIR preservation or
  error-exit artifact claim.
- C2 requires deterministic canonical identities and immutable source
  snapshots. Every authoritative acquisition has exact C/T prechecks and
  postchecks, blob OID, byte count, raw SHA-256, strict UTF-8 decode, and one
  governed event; no live-worktree source byte is accepted at runtime.
- BOOT1-BOOT5 require the seed boundary, stage direction, Gravity coding
  surface, compiler-stage compatibility, and checked-core revision to be
  explicit. The trusted-runtime manifest binds the exact seed/JVM/classpath
  surface; no Clojure helper becomes Gravity semantic authority.
- BOOT6-BOOT8 require fresh authoritative processes, reproducible stage
  comparison, trusting-trust boundaries, immutable receipts, and artifact
  provenance. Launcher L, coordinator M, checker A, and checker B are bound by
  the process and source-closure protocol below. All success and failure paths
  execute the same bounded cleanup transaction in `finally`.
- TEST10/TEST11/TEST13 require differential output/diagnostic comparison,
  independent semantic reconstruction, mutation rejection,
  verification-catalog identity, bounded proof execution, replay, and
  self-hosting evidence. E9 carries those identities and results; this
  architecture expressly does not convert them into aggregate SH-07,
  self-hosting, release, or seed-retirement authority.

At each precheck and postcheck the verifier reads the C blob for
`contracts/verification-catalog.edn`, the C blob for
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`, and the
C blob for `bootstrap/gravity/src/gravity/checked_core.gravity`. It records
their path, mode `100644`, blob OID, byte count, and raw SHA-256. Catalog
validation binds schema `:gravity/verification-catalog-v1`, version 1, its
declared authority flags, the literal `sh07-checked-core` module record, and
the exact applicable route records; V9 does not invent a catalog verifier-root
field. The three postcheck rows must equal the
precheck rows byte-for-byte. A missing catalog entry, stale checked-core
revision, changed proof contract, unbounded proof transaction, absent
immutable receipt, or cleanup not reached on rejection fails closed.

The active `sh07_proof_contract.edn` governs B47/B15 and is not silently
expanded to make a B5.1 proof claim. V9 inherits its applicable process,
snapshot, catalog, receipt, bound, and cleanup rules while preserving that
scope. E9 is validation and replay evidence. If I9 needs a new B5.1 proof kind,
proof root, or completion predicate, a separately governed proof-contract
revision must be accepted before I9 freezes; otherwise
`SH07-B51-PROOF-SCOPE` rejects.

## C/T source projection and dual grammar

C is the frozen, independently evidence-eligible I9 content commit and T is its
tree. Neither C nor evidence eligibility is implementation acceptance. The
governed projection has exactly twelve blobs, in this order:

| Ordinal | Role | Module | Path |
| ---: | --- | ---: | --- |
| 0 | `:l1-contract` | absent | `docs/phase-01-core-language/011-l1-surface-syntax-specification.md` |
| 1 | `:l2-contract` | absent | `docs/phase-01-core-language/012-l2-core-language-semantics.md` |
| 2 | `:checked-core` | absent | `bootstrap/gravity/src/gravity/checked_core.gravity` |
| 3 | `:module` | 0 | `bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` |
| 4 | `:module` | 1 | `bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity` |
| 5 | `:module` | 2 | `bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity` |
| 6 | `:module` | 3 | `bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity` |
| 7 | `:module` | 4 | `bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity` |
| 8 | `:module` | 5 | `bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` |
| 9 | `:module` | 6 | `bootstrap/gravity/src/gravity/sh07_b51/root8.gravity` |
| 10 | `:compiler` | absent | `bootstrap/gravity/p15_s23/compiler.gravity` |
| 11 | `:emitter` | absent | `bootstrap/gravity/p15_s23/emitter.gravity` |

The plan-linker component is zero-based load ordinal 808, physical line 810.
Forms 13-16 define exactly these literal holes:

- `p15-s23-sh07-b51-expected-m3-id`
- `p15-s23-sh07-b51-expected-certificate-a-id`
- `p15-s23-sh07-b51-expected-certificate-b-id`
- `p15-s23-sh07-b51-expected-semantic-root`

Form 17, `p15-s23-sh07-b51-expected-execution-bundle-id`, is an authenticated
alias of the first value and is not a hole. The linked-carrier component is
zero-based load ordinal 812, physical line 814. Form 59 defines the fifth hole,
`p15-s23-sh07-b51-authoritative-linked-carrier-value-v4`.

The only valid C preimage has the exact keyword
`:gravity/provisional-no-authority` as all five RHS values. The only valid P9
postimage has four `sha256:` strings with 64 lowercase hex digits and exactly
`(quote <carrier>)`, where `<carrier>` is strict plain EDN accepted by
`p15-s23-sh07-b51-linked-carrier-v4-valid?` and its values equal
`p15-s23-sh07-b51-v4-candidate-pin-map`. The masked root binds both paths, load
ordinals, form ordinals, owners, delimiters, and adjacent bytes and is equal for
C and P9. Mixed states, a sixth hole, alias or adjacent mutation, a different
reader form, or a carrier not reconstructed by the named builder reject.

## Governed source session

I9 makes
`gravity.bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products` the
only new public bootstrap Var. Its session token, event dispatcher, object
reader, and checked-product cache are private and dynamically scoped by the
wrapper; a caller cannot install, replace, or reuse them.

One session prefetches the exact twelve C blobs above, in order, and strictly
decodes each UTF-8 vector once. Products consume exactly 39 events: L1
contract 0, L2 contract 1, checked-core revision 2, module validation 3-9,
census 10-16, link authority 17-23, compiler emitter 24, source-authority
module reads 25-31, and compiler-artifact-binding emitter reads 32-38. The
seven final events are distinct authorized consumers of the one prefetched
emitter byte vector, not repeated acquisitions. The session finalizes once and rejects a missing,
duplicate, reordered, late, or unconsumed event.

I9 replaces every reachable physical read in the B5.1 construction closure.
The L1/L2 and checked-core consumers use domains `:l1-contract`,
`:l2-contract`, and `:checked-core-revision`. The seven
per-module reads in `p15-s23-sh07-b51-module-source-valid*` use domain
`:module-validation`; the seven reads in
`p15-s23-sh07-b51-derive-callsite-census` use domain `:callsite-census`; the
seven reads in `p15-s23-sh07-b51-build-link-authority-from-census` use domain
`:link-authority`; and the compiler read in
`p15-s23-sh07-b51-current-emitter` uses domain `:compiler-emitter`. The seven
module discoveries/reads reachable through
`00943-p15-s23-sh07-host-builtins-and-oracle.clj` use domain
`:source-authority`; the seven emitter reads reachable through
`00581-p15-s23-stage2-compiler-artifact-binding.clj` use domain
`:compiler-artifact-binding`. Every semantic consumer accepts only bytes from the active governed session; direct
`Files/readAllBytes`, `slurp`, directory discovery, or pathname reopening in
this closure rejects. Verification-catalog and proof-contract object checks
belong to the separate governance channel below; they are not source-session
decodes or events. A lexical and call-graph fixture rejects any additional
file/object read reachable from 00943, 00581, 00945, or 00947. Differential
fixtures run unmodified main and I9 on identical
accepted and rejected inputs and require identical products and diagnostics.
Module, compiler, descriptor, event, edge-authority, and five-hole mutations
must remain visible and reject.

Evidence mode performs, for each of twelve acquisitions, a pre-read and
post-read check of the isolated C worktree's exact HEAD, tree, index, clean
state, no untracked files, and full projection: exactly 12 prechecks, 12
postchecks, and zero runtime-object checks. Runtime derives equal C/T from the
E9 certificate/carrier and performs exactly 12 object prechecks and 12 object
postchecks, with zero evidence-worktree checks. It ignores current main HEAD,
index, worktree, ref, and all live `.gravity` source bytes. Its Clojure/JVM/JAR
executable substrate is the separately bound trusted-runtime TCB below, not
Gravity evidence authority. Arbitrary wrapper output grants no
authority; a private 00947 load-local gate validates carrier, pins, receipt,
and session identity. G9 is static publication proof, never runtime input.

Each A, B, and M session records exactly 12 acquisitions, 12 strict decodes, 39
events, and 1 finalize. Accepted `:new` work is `[3 3 3 2 2]`, total 13;
accepted `:existing` work is `[2 2 2 2 2]`, total 10. Sentinels are
nonauthoritative. The authenticated edge census remains exactly 1480.

## Independent checker processes

L is the narrow already-loaded host launcher in
`gravity.tools.sh07-b51-git-evidence`. L may locate `/usr/bin/git`, validate the
clean C worktree, read immutable Git objects, materialize the four-entry
C-derived classpath, and construct exactly one M `ProcessBuilder`. L may not
load the M, A, B, bootstrap, checker, or materializer namespaces; its only
additional project dependency is the shared V9 codec defined below. It may not decode a
Gravity source blob, compute a checker product, author a semantic payload, or
validate a carrier. It may assemble only the closed final process receipt from
M's six staged payloads plus L's retained process observations. Its source
closure, argv, environment, Git observations, classpath
materialization, process handle, bounded stdout/stderr capture, and cleanup
are recorded in `LAUNCHER-RECEIPT`. L is a declared local TCB boundary, never
Gravity semantic or evidence authority.

M is a fresh coordinator JVM launched by L from the exact C-derived classpath
through the minimal
`gravity.tools.sh07-b51-attempt32-coordinator-child` entrypoint at
`bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj`.
That entrypoint has no project namespace in its `ns :require`; it uses only
`clojure.core`, `clojure.edn`, `clojure.string`, and Java platform classes for
its entry preflight. It validates actual process, argv, environment, working
directory, the literal CP entries, C/T syntax, and output-root syntax before
dynamically loading the shared codec from the already byte-validated C-derived
CP. It then validates the canonical classpath manifest root and only afterward
lazily requires the materializer. A project namespace loaded before the first
preflight, or any project namespace other than the codec before the manifest-
root preflight, rejects. M's transitive loaded and
source closure has no require, load, resolve, invoke, alias, Var, or Clojure
symbol reference to either checker namespace or child entrypoint. The child
main class name may occur only as the exact opaque string token in A/B argv;
no code may intern, read, resolve, or invoke that string. The current direct requires in
`sh07_b51_attempt32_tier0_materializer.clj` are the mandatory negative fixture.
This rule covers every helper M loads before or during the run.

The only child entrypoint is
`gravity.tools.sh07-b51-attempt32-checker-child`, at
`bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj`.
Its `ns` form requires no project namespace. It performs the same two-stage
literal-CP then shared-codec/canonical-manifest preflight as M. After strict
request/process validation it lazily loads the C-bound
`gravity.bootstrap` product constructor and then exactly one checker mapping:

| CLI role | EDN role | Checker namespace |
| --- | --- | --- |
| `producer-census` | `:producer-census` | `gravity.tools.sh07-b51-attempt30-producer-census` |
| `independent-verifier-census` | `:independent-verifier-census` | `gravity.tools.sh07-b51-attempt30-independent-census` |

An A child referencing or loading B, or B referencing or loading A, rejects in
its loaded-namespace and source-closure receipt.

The request deliberately carries C/T and roots rather than M-built semantic
products. Each child opens its own twelve-blob governed C/T session, invokes
`gravity.bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products` to
construct its complete `:m0s`, `:m1`, `:m2s`, `:bundle-core`, and
`:source-forms` envelope, finalizes that session, and only then invokes its one
selected checker. The child response binds the constructed envelope IDs and
source receipt. No semantic envelope crosses from M, A, or B into another
participant. M starts its third independent session only after B validates,
reconstructs the same complete envelope, and uses the neutral parent validator
to compare both child results. Static requires, load events, and loaded
namespace receipts prove this order.

The semantic coordinator ordinals are exactly:

| Ordinal | State |
| ---: | --- |
| 0 | M process start |
| 1 | A `ProcessBuilder` constructed |
| 2 | A started |
| 3 | A exit observed |
| 4 | A response fully validated |
| 5 | B `ProcessBuilder` constructed |
| 6 | B started |
| 7 | B exit observed |
| 8 | B response fully validated |
| 9 | M governed source session started |
| 10 | M governed source session finalized |

Before ordinal 4, M cannot materialize B argv, construct B, or load the child or
either checker. A exits and validates before B exists; B exits and validates
before M starts its session. Every authoritative `:new` and `:existing` run
therefore uses fresh A and B JVMs followed by a third independent M session.

`:object-replay-only` is separate, zero-child, diagnostic, and
nonauthoritative. It cannot satisfy `:existing`, runtime admission, lifecycle
join, G9, or publication. Authoritative reload means `:existing` and always
launches fresh A, B, and M. Each authoritative run has exactly two builders,
two starts, two exits, two requests, two responses, zero A/B overlap, and one M
session inside M, plus exactly one L builder/start/exit for M. A missing,
reused, already-loaded, or extra M process rejects.

## Process transport and TCB

Before stdin write or wait, M starts independent incremental stdout and stderr
drains and a separate bounded stdin-writer task. The writer owns the validated
request bytes, writes at most 4194304 bytes, flushes, and closes stdin. Each
drain enforces its cap while reading. M joins writer, child, and both drains
under one shared bounded deadline and parses only after all four terminate. A
blocked writer, overflow, task exception, or deadline atomically fails, closes
all streams, calls `destroy`, then `destroyForcibly` if necessary, and
bounded-joins every task. Sequential drain, caller-thread synchronous write,
unbounded buffering, parse-during-drain, and temporary-file authority are
forbidden.

Git is absolute `/usr/bin/git`, observed as Apple Git 2.50.1. Its record binds
canonical path, file key, byte count, SHA-256, version, and object format; every
Git argv starts with it. Java is canonical
`${System.getProperty("java.home")}/bin/java` and binds the same file identity
plus Java/JVM versions. Observed `java.home` is
`/opt/homebrew/Cellar/openjdk/26.0.1/libexec/openjdk.jdk/Contents/Home`, OpenJDK
26.0.1; this absolute observation is receipt-only.

L derives the external three JAR entries from its observed `java.class.path`;
the complete child CP uses Darwin `:` and has exactly four ordered
semantic entries:

1. `project/bootstrap-clojure-src` at C/T, materialized from Git objects
2. `org.clojure/clojure` 1.12.5, providing `clojure.core`
3. `org.clojure/core.specs.alpha` 0.4.74, providing
   `clojure.core.specs.alpha`
4. `org.clojure/spec.alpha` 0.5.238, providing `clojure.spec.alpha` and
   `clojure.spec.gen.alpha`

Each binds canonical path, logical coordinate, file key, count, and SHA-256. L,
before M can exist, enumerates every mode-100644 blob below
`C:bootstrap/clojure/src` in Git byte-path order and builds exact manifest rows
`[relative-path blob-oid byte-count sha256]`. L materializes those bytes into a
fresh private mode-0700, symlink-free directory and computes entry byte-count
as the sum and entry SHA-256 as the canonical manifest-root hash. M never
creates or mutates classpath entry 0; it independently validates every Git
object, manifest row, materialized byte, mode, and file identity at entry and
exit and around each child. L validates the same closure before M start and
after M exit. The directory path and file keys are receipt-only; the C objects
and manifest root are authority. No
worktree `bootstrap/clojure/src` directory is placed on CP. All blobs, including
the 124 current non-Clojure resources and the numbered component fragments,
remain in the byte manifest and materialized projection.
`provided-namespaces` is derived only from `.clj` or `.cljc` blobs containing an
`ns` form as their first non-comment form. Such a namespace-owning file must
contain exactly one `ns` form. A source/resource or component fragment with no
leading `ns` contributes no namespace and is valid; an `ns` anywhere later is
rejected. Namespace symbols are sorted and unique. Each JAR binds its exact
bytes and namespace residence. Missing, extra, reordered, symlinked, shadowing,
or multiply-owned namespaces reject. The materialized directory is executable
substrate, not evidence authority, and is deleted only after postchecks and
process receipt construction.

Exact M argv, constructed by L without a shell, is:

```clojure
[JAVA "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
 "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
 "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
 "-Duser.home=/nonexistent" "-cp" CP "clojure.main" "-m"
 "gravity.tools.sh07-b51-attempt32-coordinator-child"
 "--mode" MODE
 "--snapshot-commit" C "--snapshot-tree" T
 "--completed-at" "1970-01-01T00:00:00Z"
 "--evidence-relative-root" EVIDENCE-ROOT]
```

`MODE` is exactly `new` or `existing`; `EVIDENCE-ROOT` is the one validated
relative E9 output root. L applies the exact environment map below and the
canonical repository root working directory. Before M starts, L replays every
C classpath manifest row against Git objects and the materialized directory.
At process entry M validates its actual command, arguments, environment,
working directory, `java.class.path`, Java identity, all four classpath
entries, and the complete C-derived namespace/resource manifest before loading
any project namespace other than its entrypoint. Immediately before exit M
repeats the source-closure and materialized-byte checks. After M exits, L
repeats them a third time, validates M's closed response and process receipt,
then deletes the materialized directory in `finally`. Precheck, M-entry,
M-exit, and L-postcheck roots must be identical.

CLI/EDN conversion is one closed bijection: string `"new"` maps only to
keyword `:new`, `"existing"` only to `:existing`, `"producer-census"` only to
`:producer-census`, and `"independent-verifier-census"` only to
`:independent-verifier-census`. Strings never appear in EDN mode/role fields,
keywords never appear in argv, and every other spelling or coercion rejects.

Exact A argv:

```clojure
[JAVA "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
 "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
 "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
 "-Duser.home=/nonexistent" "-cp" CP "clojure.main" "-m"
 "gravity.tools.sh07-b51-attempt32-checker-child"
 "--role" "producer-census"]
```

Exact B argv differs only in the last string:
`"independent-verifier-census"`. `ProcessBuilder` uses no shell and the
repository canonical absolute root as working directory; that observation is
receipt-only. M clears inherited environment and sets exactly:

```clojure
{"LC_ALL" "C"
 "LANG" "C"
 "TZ" "UTC"
 "GIT_CONFIG_NOSYSTEM" "1"
 "GIT_CONFIG_GLOBAL" "/dev/null"
 "GIT_OPTIONAL_LOCKS" "0"
 "GIT_NO_REPLACE_OBJECTS" "1"}
```

`JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS`, `JDK_JAVA_OPTIONS`, `CLJ_CONFIG`, `HOME`,
and `PATH` are absent. Any injection or extra variable rejects.

L uses that identical closed environment for M. `LAUNCHER-RECEIPT` has exactly
`:schema :launcher-source-closure :launcher-process :git :java
:classpath-precheck :m-argv :m-argv-id :environment :working-directory
:m-process-handle-id :m-process :m-process-join :m-response-transport
:m-response-byte-count :m-response-raw-sha256 :m-response-preimage-id
:classpath-postcheck :publication :cleanup :receipt-id`. Schema is
`:gravity/sh07-b51-launcher-receipt-v1`; launcher source closure is the closed
SOURCE-CLOSURE containing only the launcher and shared codec; launcher process
is SELF-PROCESS; M process is RETAINED-PROCESS; and its join is PROCESS-JOIN.
Classpath pre/post checks each have exactly `:phase :entries :manifest-root
:equal-to-entry-root`, with phases `:l-pre-m` and `:l-post-m` and the final
flag true.

`m-response-transport` has exactly `:stdin-byte-count :stdin-raw-sha256
:stdin-closed-before-wait :stdout-cap :stderr-cap :deadline-ms
:stdout-drain-started-before-wait :stderr-drain-started-before-wait
:stdout-drain-complete :stderr-drain-complete :timed-out :destroy-called
:destroy-forcibly-called :tasks-joined`. The values are respectively 0,
`R(empty-bytes)`, true, 1073741824, 1048576, 3600000, true, true, true, true,
false, false, false, true on success. M response bytes are W(response), byte
count is their length, raw SHA is R(bytes), and preimage ID is the response's
domain-bound H value. Publication has exactly `:mode :final-relative-path
:reservation-created :reservation-file-key :files-created
:commit-marker-last :foreign-entry :outcome`. Mode is `:new` or `:existing`;
final relative path is the fixed safe repository-root-relative E9 evidence
directory; reservation-created is a boolean; reservation file key is a
nonempty receipt-only string when created and nil otherwise; files-created is
the exact ordered artifact-path vector in `:new` and empty in `:existing`;
commit-marker-last is true; foreign-entry is false; and outcome is
`:published` or `:reused`, matching mode. The receipt-only normalizer replaces
the reservation file key at this exact field and nowhere else. Cleanup is
exactly `{:finally-entered true
:stdin-closed true :stdout-closed true :stderr-closed true
:process-terminated true :tasks-joined true
:materialized-directory-deleted true :staging-resolved true}`.

M's self receipt must join L's retained process handle, report L as parent, and
reproduce the same normalized argv, environment, and classpath roots. The
launcher receipt is included in E9's identity receipt and its normalized ID is
recomputed during object-only review. L begins independent bounded stdout and
stderr drains before waiting for M, allows no M stdin bytes, and applies the
same caps and deadline/forced-termination rules used for A and B.

M's successful stdout is one canonical closed response with exactly `:schema
:status :mode :content :payload-descriptors :staging-receipt
:coordinator-receipt :source-read-receipt :counters :transport`. Schema is
`:gravity/sh07-b51-m-response-v1`, status is `:accepted`, content is exactly
`{:commit C :tree T}`, mode is `:new` or `:existing`, payload descriptors are
the six non-receipt E9 rows, and `:staging-receipt` binds the private
symlink-free stage root. It has exactly `:staging-path :evidence-relative-root
:directory-mode :symlink-free :payload-names :payload-descriptors`;
directory mode is `"0700"`, symlink-free is true, names are the exact six-file
order, and descriptors equal `:payload-descriptors`. `:staging-path` is a
nonempty receipt-only absolute observation and the evidence root is the stable
validated relative root.
Transport is exactly
`{:response-preimage-id SHA :self-id-omits
[[:transport :response-preimage-id]]}`. The preimage ID uses the exact response
domain below. No final identity-receipt ID/descriptor, launcher process handle,
PID, raw stdout hash, or launcher receipt is present, so L can add those
observations without a cycle.

The six `PAYLOAD-DESCRIPTOR` rows are ordered exactly as
`[[0 "checker-a-identity.edn" :checker-identity]
[1 "checker-b-identity.edn" :checker-identity]
[2 "checker-a-output.edn" :checker-output]
[3 "checker-b-output.edn" :checker-output]
[4 "certificate-pair.edn" :certificate-pair]
[5 "linked-carrier-v4-candidate.edn" :linked-carrier]]`. Each row has exactly
`:ordinal :name :kind :relative-path :wire-codec :byte-count :raw-sha256
:value-id`. Wire codec is `:gravity/sh07-b51-edn-wire-v1`; relative path is the
validated E9 root plus name; byte count is `alength(W(V))`; raw SHA is
`R(W(V))`; and value ID is
`H([:gravity/sh07-b51-staged-payload-value-v1 {:ordinal N :name NAME :kind KIND
:value V}])`. No blob OID exists before E9. An E9 object descriptor adds
exactly `:mode "100644" :type "blob" :blob-oid OID` and requires Git blob
bytes to equal W(V).

`M-COUNTERS` has exactly `:work :source-session :process :ipc`. Work is the
closed WORK value. Source session is 12/12/39/1. Process is exactly
`{:builder-count 2 :child-count 2 :start-count 2 :exit-count 2
:overlap-count 0}`. IPC is exactly `{:request-count 2 :response-count 2
:request-max-bytes 4194304 :response-max-bytes 1073741824
:stderr-max-bytes 1048576 :deadline-ms 3600000}`. The staging receipt and payload descriptors use
the exact schemas above; no open `:counters` or descriptor slot remains.

M retains each `Process` and pipe as authority. It immediately captures PID,
parent PID, command, args, and startInstant. The child reports current PID and
parent plus RuntimeMXBean name matching `^([0-9]+)@(.+)$`, start-time millis,
JVM/Java, and classpath. Reported and retained PIDs join; parent is M; A, B, and
M are distinct. PID reuse or mismatch fails closed. PID, host, and time are
required well-formed receipt-only fields, never authority comparisons; the
semantic ordinals above are ordering authority.

Self-report and parent-observed exit state are distinct closed types.
`SELF-PROCESS` has exactly `:role :pid :parent-pid :command :arguments
:start-instant`; role is `:launcher`, `:m`, `:producer-census`, or
`:independent-verifier-census`, PIDs are positive integers, arguments are a
string vector, and start is an ISO-8601 instant. `RETAINED-PROCESS` has exactly
`:role :process-handle-id :pid :parent-pid :command :arguments :start-instant
:lifecycle :exit-code`. Lifecycle has exactly `:domain :builder-ordinal
:start-ordinal :exit-ordinal`; domain is `:launcher` for M and `:coordinator`
for A/B. The literal ordinal triples are M `[0 1 2]`, A `[1 2 3]`, and B
`[5 6 7]`; exit is 0. Exit state never appears in a child self-report.

`PROCESS-JOIN` has exactly `:self-and-retained-pid-equal
:self-and-retained-parent-pid-equal :self-and-retained-command-equal
:self-and-retained-arguments-equal :self-and-retained-start-instant-equal
:mxbean-pid-equal :mxbean-host-equal`, all true. The complete receipt also has
exactly `:pid-vector [M-PID A-PID B-PID] :pairwise-distinct true
:parent-edges [[L-PID M-PID] [M-PID A-PID] [M-PID B-PID]]`. These joins are
validated from retained handles and self reports before normalization; no PID
is semantic authority.

## Exact canonical identity contract

Every phrase in this report of the form "the canonical hash under DOMAIN of
PAYLOAD" is normative shorthand for the exact function `H([DOMAIN PAYLOAD])`.
`H`, `W`, and `R` are the three public functions of the new shared leaf
`gravity.tools.sh07-b51-v9-codec` at
`bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj`. L, M, A, and B
load this same C-bound leaf; no participant defines, copies, wraps, or selects a
second encoder or digest implementation. The codec statically requires only
`clojure.core`, `gravity.c2-artifact-identity.canonical`, and
`gravity.digest`. It is never a hash of PAYLOAD alone and never a caller's
choice of serializer. The authoritative implementation is the exact C/T Git
object closure containing
`bootstrap/clojure/src/gravity/c2_artifact_identity.clj`,
`bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj`, and
`bootstrap/clojure/src/gravity/digest.clj`. Their mode, blob OID, byte count,
and raw SHA-256 are recorded in every A/B/M TCB and verified before and after
each process.

`H(v)` is exactly the result of this non-interposable leaf composition:

```clojure
(letfn [(rcv [x]
          (gravity.c2-artifact-identity.canonical/reader-canonical-value
           rcv x))]
  (gravity.c2-artifact-identity.canonical/reader-canonical-hash
   gravity.digest/sha256-hex rcv v))
```

It recursively projects maps, sets, vectors, and sequences with
`reader-canonical-value`; deterministically orders the decorated map/set
entries exactly as the C implementation does; then binds `*print-length*` and
`*print-level*` to nil and `*print-meta*` to true, applies `pr-str`, hashes the
resulting UTF-8 bytes with SHA-256, renders 64 lowercase hexadecimal digits,
and prefixes `sha256:`. `H` and
`gravity.bootstrap/reader-canonical-hash` must return byte-identical strings
for every admitted identity preimage and every mutation fixture. Maps with an
extra or omitted key, a non-vector domain wrapper, metadata drift, alternate
map/set ordering, alternate scalar spelling, platform-default encoding,
unprefixed digest, or a different omission set reject.

In compact notation, `H(v) = "sha256:" +
lowercaseHex(SHA256(UTF8(pr-str(RCV(v)))))` under those exact dynamic print
bindings. The bound C closure also contains and records
`bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00798-reader-canonical-value.clj`,
`bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00799-c2-artifact-identity-ops.clj`,
`bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00801-c2-artifact-identity-call.clj`,
`bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00802-reader-canonical-value.clj`,
the corresponding ordered rows in
`bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn`, and the SH-07
declarations/runtime in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle.clj`
and
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00944-p15-s23-sh07-runtime-internal-builtins.clj`.
Facade interposition, a
second encoder, reordered load entries, or disagreement among these composed
paths and the canonical leaf rejects.

Wire bytes are the codec's separate exact function `W(v)`. It first applies
the codec-local `ordered-value`. Its comparator is `compare` over
`pr-str(rcv(x))`; it recursively emits maps as `sorted-map-by` values and sets
as `sorted-set-by` values under that comparator, vectors with `mapv`,
sequences with `list`, and scalars unchanged. It then binds `*print-length*` and
`*print-level*` to nil and `*print-meta*` to true, emits `pr-str`, appends
exactly one LF byte, and UTF-8 encodes. Strict reload decodes UTF-8 without
replacement, binds `*read-eval*` false, uses `:readers {}` and a rejecting
`:default` tag handler, parses exactly one plain-EDN form followed by EOF,
reproduces `v`, and reproduces `W(v)`
byte-for-byte. Every V9 E9 and G9 EDN blob and every IPC request/response is
exactly `W(v)`; no prose-only "canonical EDN" encoding is permitted. Raw byte
identity is the codec's `R(b) = "sha256:" +
gravity.digest/sha256-bytes-hex(b)`. Every field named `:sha256` or
`*-sha256` is explicitly either `R(bytes)` or an `H` identity in its closed
schema; an untyped SHA field rejects. L and every fresh M/A/B process run the same
closed parity vector for every H, W, and R identity family before evidence is
eligible.

The UTF-8 decoder uses `CharsetDecoder` with both malformed-input and
unmappable-character actions set to `CodingErrorAction/REPORT`; constructing a
String directly from bytes is forbidden. The strict EDN reader rejects
duplicate map keys and any comparator-key collision between distinct admitted
encoded values before W or H is evaluated.

Raw blob, stdin, stdout, and stderr SHA-256 fields are deliberately different:
they hash the literal byte arrays with
`gravity.digest/sha256-bytes-hex` and add the `sha256:` prefix. They never use
the reader-canonical projection. This distinction is part of every receipt and
mutation fixture.

The V9-created canonical identities use exactly these domains and payloads:

| Identity | Domain | Payload |
| --- | --- | --- |
| classpath manifest root | `:gravity/sh07-b51-classpath-manifest-root-v1` | exact ordered classpath manifest rows |
| loaded namespace root | `:gravity/sh07-b51-loaded-namespace-root-v1` | closed loaded-namespace map with `:root` omitted |
| host surface root | `:gravity/sh07-b51-host-surface-root-v1` | closed host-surface map with `:root` omitted |
| source descriptor root | `:gravity/sh07-b51-source-descriptors-root-v1` | exact ordered descriptor vector |
| I9 preservation root | `:gravity/sh07-b51-i9-preservation-root-v1` | exact UTF-8-byte-sorted C/B preserved-blob vector |
| source event root | `:gravity/sh07-b51-source-event-root-v1` | exact dense event vector |
| source event product ID | `:gravity/sh07-b51-source-event-product-id-v1` | exact event coordinate plus parsed consumer product |
| request ID | `:gravity/sh07-b51-checker-request-id-v1` | exact closed request map |
| argv ID | `:gravity/sh07-b51-process-argv-id-v1` | exact argv string vector |
| response preimage ID | `:gravity/sh07-b51-response-preimage-id-v1` | exact closed response with only `[:transport :response-preimage-id]` omitted |
| process handle ID | `:gravity/sh07-b51-process-handle-coordinate-id-v1` | the exact closed coordinate printed below; M has nil request ID |
| coordinator event ID | `:gravity/sh07-b51-coordinator-event-id-v1` | one event row with only `:event-id` omitted |
| coordinator event root | `:gravity/sh07-b51-coordinator-event-root-v1` | exact 11-row event vector |
| source session ID | `:gravity/sh07-b51-governed-source-session-id-v1` | the exact closed session map printed below |
| TCB manifest ID | `:gravity/sh07-b51-checker-child-tcb-manifest-v1` | closed TCB map with only `:manifest-id` omitted |
| masked source root | `:gravity/sh07-b51-masked-source-root-v1` | exact ordered path/load/form/owner/delimiter/adjacent-byte projection with only the five RHS values masked |
| normalized payload semantic root | `:gravity/sh07-b51-payload-semantic-root-v1` | exact ordered six parsed sibling payload projections after receipt-only replacement |
| launcher receipt ID | `:gravity/sh07-b51-launcher-receipt-id-v1` | normalized launcher receipt with only `:receipt-id` omitted |
| evidence receipt ID | `:gravity/sh07-b51-checker-process-identity-receipt-id-v1` | normalized whole evidence receipt with only `:receipt-id` omitted |
| contract document event root | `:gravity/sh07-b51-contract-document-event-root-v1` | exact dense 31-row contract-read event vector |
| contract discharge row root | `:gravity/sh07-b51-contract-discharge-row-root-v1` | exact literal 19-row discharge vector |
| evidence-eligibility receipt ID | `:gravity/sh07-b51-evidence-eligibility-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| I9 integration receipt ID | `:gravity/sh07-b51-i9-integration-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| G9 lifecycle receipt ID | `:gravity/sh07-b51-g9-lifecycle-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| A9 lifecycle receipt ID | `:gravity/sh07-b51-a9-lifecycle-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| P9 publication receipt ID | `:gravity/sh07-b51-p9-pin-publication-receipt-v1` | exact external receipt with only `:receipt-id` omitted |

The existing checker artifact, semantic, executable, dependency-closure, and
identity-manifest domains remain the literal role-specific domains in
`sh07_b51_attempt32_checker_identity/checker-specs`. The result artifact/output
domains remain the two literal domains printed in the response schema. No
domain is inferred from a field name.

Field identity typing is closed. Every `:source-sha256`,
`:architecture-report-sha256`, executable/JAR `:sha256`, and other field whose
enclosing schema calls it a byte SHA is R of the exact named bytes. Classpath
entry 0's `:sha256` is the H classpath-manifest root, while entries 1-3 are R
of JAR bytes. Every `*-id`, `*-root`, `:artifact-id`, `:semantic-id`,
`:executable-id`, `:dependency-closure-id`, `:manifest-id`, `:claim-id`,
`:normalized-census-id`, `:output-id`, and `:certificate-id` is H under the
literal domain declared here or in the immutable role-specific checker spec.
Git `:oid`/`:blob-oid` fields are 40-hex SHA-1 object names, never H or R.
There is no untyped digest slot and no conversion between these families.

## Closed IPC request

The request is one strict canonical EDN form plus EOF:

```clojure
{:schema :gravity/sh07-b51-checker-child-request-v2
 :role :producer-census|:independent-verifier-census
 :content {:commit C :tree T}
 :evidence {:mode :new|:existing
            :architecture-report-sha256 SHA
            :profile-bounds
            {:certificate-maximum-nodes 2048
             :certificate-maximum-depth 12
             :certificate-maximum-width 35
             :certificate-maximum-collection-count 64
             :certificate-maximum-scalar-utf8 4096
             :certificate-maximum-canonical-reader-bytes 262144
             :outer-request-maximum-collection-count 65536
             :carrier-maximum-nodes 8388608
             :carrier-maximum-depth 256
             :carrier-maximum-width 65536
             :carrier-maximum-collection-count 524288
             :carrier-partition-maximum-collection-count 65536
             :carrier-maximum-scalar-utf8 65536
             :offline-transcript-maximum-nodes 262144
             :offline-transcript-maximum-records 262144
             :offline-transcript-maximum-call-rows 65536
             :offline-transcript-maximum-bytes 268435456}}
 :limits {:request-max-bytes 4194304
          :response-max-bytes 1073741824
          :stderr-max-bytes 1048576
          :deadline-ms 3600000}}
```

C/T are 40 lowercase hex Git IDs and SHA is `sha256:` plus 64 lowercase hex.
All key sets are exactly shown. Role and mode are the shown keyword
alternatives. Bounds have exactly 17 keys; limits are the shown keyed map.
The child independently derives `source-descriptors-root` and `masked-root`
from its own governed session; neither is a caller-supplied request field.

## Closed IPC response

The response is one strict canonical EDN form plus EOF:

```clojure
{:schema :gravity/sh07-b51-checker-child-response-v1
 :role ROLE
 :transport {:argv-id SHA
             :request-id SHA
             :response-preimage-id SHA
             :self-id-omits [[:transport :response-preimage-id]]}
 :result {:checker-identity CHECKER-IDENTITY
          :checker-output CHECKER-OUTPUT
          :source-read-receipt SOURCE-READ-RECEIPT
          :production PRODUCTION
          :sentinel SENTINEL}
 :counters {:work WORK
            :source-session {:acquire-count 12
                             :decode-count 12
                             :event-count 39
                             :finalize-count 1}
            :process {:child-count 1
                      :start-count 1
                      :exit-count 1
                      :overlap-count 0}
            :ipc {:request-count 1
                  :response-count 1
                  :request-max-bytes 4194304
                  :response-max-bytes 1073741824
                  :stderr-max-bytes 1048576
                  :deadline-ms 3600000}}
 :tcb {:manifest-id SHA
       :role ROLE
       :process SELF-PROCESS
       :runtime-mxbean RUNTIME-MXBEAN
       :java EXECUTABLE
       :git EXECUTABLE
       :classpath CLASSPATH
       :environment ENVIRONMENT
       :source-closure SOURCE-CLOSURE
       :loaded-namespace-receipt LOADED-NAMESPACES
       :host-surface HOST-SURFACE}
 :diagnostics DIAGNOSTICS}
```

Capitalized tokens above are typed slots, not unresolved implementation
placeholders. Their exact closed definitions are:

- `ROLE` is the request role.
- `WORK` is exactly `{:vector [3 3 3 2 2] :total 13 :mode :new}` or
  `{:vector [2 2 2 2 2] :total 10 :mode :existing}`.
- `CHECKER-IDENTITY` has exactly `:artifact :schema-version :role
  :source-relative-path :source-byte-count :checker :dependencies :runtime
  :identity-manifest-id`. `:checker` has exactly `:artifact-id :source-sha256
  :semantic-id :executable-id :dependency-closure-id :algorithm-role`.
  `:dependencies` has exactly `:local-source-records
  :external-runtime-records`; a local record has exactly `:relative-path
  :byte-count :source-sha256`. An external record has either exactly
  `:namespace :resource :byte-count :source-sha256`, or exactly `:namespace
  :resource` with `:resource :host-provided`. `:runtime` has exactly
  `:clojure-version :java-version :java-vendor :java-vm-name :java-vm-version
  :os-name :os-arch`. The value equals the parent's recomputation by
  `sh07_b51_attempt32_checker_identity` for C and ROLE.
- `CHECKER-OUTPUT` is the C-bound closed accepted output for ROLE. Producer A
  has exactly `:artifact :schema-version :status :certificate
  :raw-source-transcript-evidence :m2-transcript-evidence
  :commitment-evidence`. Checker B has exactly `:status :certificate
  :raw-source-transcript-artifact :m2-transcript-artifact`. Every nested key set
  is the literal V9 census schema below, not an implementation-selected map.
  I9 places a neutral structural validator in a parent-safe namespace. M
  invokes it without loading a checker; differential fixtures require agreement
  with the original role validator for accepted and every single-field,
  extra-key, missing-key, and reordered-vector mutation. Only `:accepted`
  output succeeds.
- `SOURCE-READ-RECEIPT` has exactly `:schema :role :mode :commit :tree
  :source-descriptors :source-descriptors-root :events :event-root :counters
  :finalized`. Schema is `:gravity/sh07-b51-source-read-receipt-v2`; the twelve
  descriptors are closed below; counters equal response source counters;
  `:events` is the exact
  dense 39-row vector below; finalized is true.
- `PRODUCTION` and `SENTINEL` each have exactly `:schema :role :status
  :artifact-id :output-id :authority`. Schemas are respectively
  `:gravity/sh07-b51-checker-production-v1` and
  `:gravity/sh07-b51-checker-sentinel-v1`; status is `:accepted`; authority is
  true for production and false for sentinel. For either map, `artifact-id` is
  the canonical hash, under domain
  `:gravity/sh07-b51-checker-result-artifact-id-v1`, of the map with both
  `:artifact-id` and `:output-id` omitted. `output-id` is the canonical hash,
  under domain `:gravity/sh07-b51-checker-result-output-id-v1`, of the map with
  only `:output-id` omitted, so it includes the already-derived `artifact-id`.
  This order is mandatory and acyclic.
- `SELF-PROCESS` is the exact self-report type above. Its role equals ROLE;
  command/arguments equal the retained handle and start is receipt-only.
- `RUNTIME-MXBEAN` has exactly `:name :pid :host :start-time-millis :vm-name
  :vm-vendor :vm-version :java-version :java-vendor :java-class-path`. Name
  parses to the same positive PID and a nonempty host; time is nonnegative and
  receipt-only.
- An `EXECUTABLE` has exactly `:logical-role :canonical-path :file-key
  :byte-count :sha256 :version-lines`; Git additionally has `:object-format`.
  Roles are `:java` and `:git`; counts are positive; path/key are nonempty;
  hashes are strict; version-lines is a nonempty string vector.
- `CLASSPATH` is exactly four maps in CP order. Each has exactly `:ordinal
  :kind :logical-coordinate :canonical-path :file-key :byte-count :sha256
  :provided-namespaces`; ordinals are 0-3, kind is `:directory` at 0 and `:jar`
  otherwise, and coordinates/namespaces are the four entries above.
- `ENVIRONMENT` is exactly `{:working-directory STRING :variables MAP}`;
  variables equal the seven-entry environment and directory is the canonical
  repository-root observation.
- `SOURCE-CLOSURE` has exactly `:role :entrypoint :static-project-namespaces
  :dynamic-project-namespaces :source-records :forbidden-namespaces :root`.
  Static and dynamic vectors are sorted distinct symbols, source records use
  the closed loaded-namespace source-record schema, forbidden namespaces are
  absent from both vectors, and root H-hashes the other fields. For A/B the
  bootstrap constructor precedes exactly the selected checker in the recorded
  dynamic-load event vector. For M, the checker and child symbols are absent
  except for the child main's opaque argv string, which is not a namespace
  value.
- `LOADED-NAMESPACES` has exactly `:role :project-namespaces
  :external-namespaces :source-records :forbidden-overlap :root`. Namespace
  vectors are sorted distinct symbols; source records have exactly
  `:namespace :logical-coordinate :relative-path :blob-oid :byte-count
  :sha256`; forbidden-overlap is exactly `[]`; root hashes the other fields.
  Producer A includes its checker and excludes checker B; B includes its
  checker and excludes A. M never has this response field because M is not a
  child, but its separately persisted receipt excludes both.
- `HOST-SURFACE` has exactly `:policy :macro-vars :interop-members
  :state-primitives :dynamic-load-primitives :class-definitions :root`.
  Policy is `:gravity/sh07-b51-trusted-clojure-host-surface-v1`. The five
  vectors are sorted, distinct, fully qualified symbols or strings extracted
  from the exact entry-0 source manifest after reader expansion. Macro-vars are
  Vars resolved with `:macro true`; interop members are canonical
  `class/member` strings after macro expansion; class-definitions are generated
  class names. State primitives are the exact used intersection with
  `[atom swap! reset! compare-and-set! volatile! vreset! vswap! delay force
  locking promise deliver deref alter-var-root set!]`. Dynamic-load primitives
  are the exact used intersection with `[require load load-file resolve
  requiring-resolve eval macroexpand macroexpand-1]`; only the child entrypoint's
  single role-lazy `require`/`requiring-resolve` is allowed after request
  validation, and `load-file`/`eval` are always absent. Root canonically hashes
  the other six fields. Static extraction, instrumented load events, and the
  final loaded-namespace receipt must agree exactly. Missing, extra, reordered,
  or late host behavior rejects.
- `DIAGNOSTICS` is a bounded vector of maps with exactly `:id :status
  :details`. ID is in the stable family below, status is one of `:rejected
  :overflow :timeout :invalid`, and details always has exactly `:phase :reason
  :ordinal :expected :actual`; phase/reason are keywords, ordinal is a
  nonnegative integer or nil, and expected/actual are strict plain-EDN values
  individually bounded to 131072 canonical bytes. Successful response is
  exactly `[]`.

`CHECKER-IDENTITY.identity-manifest-id` is the canonical hash under the
role-specific manifest domain of the entire checker-identity map with only
`:identity-manifest-id` omitted. `TCB.manifest-id` is the canonical hash under
`:gravity/sh07-b51-checker-child-tcb-manifest-v1` of the entire TCB map with
only `:manifest-id` omitted. Those omission paths are fixed and no other field
is omitted.

A source descriptor has exactly `:ordinal :kind :module-ordinal :path :mode
:blob-oid :byte-count :raw-sha256`. Ordinal, kind, module ordinal (`nil` for every
non-module kind), and path match the table; mode is `"100644"`. OID is 40 lowercase hex, count nonnegative,
and raw SHA is R(blob bytes). Descriptor and event roots canonically hash exact ordered
vectors.

An event has exactly `:ordinal :domain :source-ordinal :module-ordinal
:consumer :source-raw-sha256 :product-id`. Events 0, 1, and 2 use source ordinals
0, 1, and 2, nil module ordinal, domains `:l1-contract`, `:l2-contract`, and
`:checked-core-revision`, and consumers
`gravity.bootstrap/p15-s23-sh07-b51-l1-contract`,
`gravity.bootstrap/p15-s23-sh07-b51-l2-contract`, and
`gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract`. Events 3-9 use
domain `:module-validation`, source ordinals 3-9, module ordinals 0-6, and
consumer `gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*`; events
10-16 use `:callsite-census` with the same source/module ordering and consumer
`gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census`; events 17-23 use
`:link-authority` with that ordering and consumer
`gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census`. Event
24 uses `:compiler-emitter`, source ordinal 10, nil module ordinal, and consumer
`gravity.bootstrap/p15-s23-sh07-b51-current-emitter`. Events 25-31 use
`:source-authority`, source ordinals 3-9, module ordinals 0-6, and consumer
`gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority`. Events
32-38 use `:compiler-artifact-binding`, source ordinal 11, module ordinals 0-6,
and consumer `gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding`.
Within every seven-row group module order is 0-6. Both hashes are strict
SHA-256. `source-raw-sha256` is R of the descriptor bytes; `product-id` is
`H([:gravity/sh07-b51-source-event-product-id-v1 {:ordinal N :domain DOMAIN
:consumer CONSUMER :source-raw-sha256 SHA :product PRODUCT}])`. The event root
hashes this exact dense vector. No descriptor or event
may be synthesized from a live pathname.

### Literal V9 census schema

The common certificate has exactly `:artifact :domain :schema-version :status
:role :architecture-report-sha256 :claim-id :claim :assumptions :checker
:inputs :result :provenance :raw-source-transcript :m2-transcript
:profile-bounds :evidence-root :certificate-id`. Its checker map has exactly
`:artifact-id :source-sha256 :semantic-id :executable-id
:dependency-closure-id :algorithm-role`. Inputs have exactly
`:architecture-report-sha256 :snapshot-commit :snapshot-tree
:predecessor-executable-descriptor-id :module-source-identities
:module-source-byte-counts :emitter-source-id :emitter-semantic-id :m0-ids
:m1-id :m2-ids :bundle-core-id`. Provenance has exactly `:invocation-id
:tool-closure-id :reproduction-command-id :evidence-relative-path
:completed-at :raw-transcript-artifact-id :m2-transcript-artifact-id`.

Claim has exactly `:domain :normalized-census-id
:external-bijection-commitment :builtin-parity-commitments
:provenance-completeness-commitment :physical-census :category-schedule
:category-commitments`. Assumptions have exactly `:sha256-collision-resistance
:canonical-reader-hash-id :source-emitter-bytes-immutable
:checker-closures-independent :host-pin-boundary-id
:runtime-token-boundary-id`. Result has exactly `:status
:normalized-census-id :definition-count :local-count :external-count
:builtin-count :semantic-row-count :external-bijection-complete
:builtin-parity-complete :provenance-complete :ordering-complete
:reachability-complete`. A transcript descriptor has exactly `:artifact
:schema-version :node-count :row-count :artifact-id :record-root`.

Physical census has exactly `:module-count :module-censuses
:physical-definition-count :reachable-definition-count
:physical-local-runtime-callsite-count :reachable-local-runtime-callsite-count
:physical-external-runtime-callsite-count
:reachable-external-runtime-callsite-count
:physical-referenced-builtin-occurrence-count
:reachable-referenced-builtin-occurrence-count :semantic-row-count
:module-binding-occupancies :maximum-binding-occupancy :binding-ceiling`.
Each of seven module censuses has exactly `:module-ordinal :module
:logical-source-path :owner :source-byte-count :source-sha256
:module-source-id :plan-id :physical-definition-count
:reachable-definition-count :physical-local-runtime-callsite-count
:reachable-local-runtime-callsite-count :physical-external-runtime-callsite-count
:reachable-external-runtime-callsite-count
:physical-referenced-builtin-occurrence-count
:reachable-referenced-builtin-occurrence-count :core-binding-count
:lexical-binding-count :external-binding-count :aggregate-binding-count`.
Each of seven category commitments has exactly `:category :row-count
:leaf-count :fold-level-count :root-digest`.

Category schedule is exactly:

```clojure
{:rows-per-leaf 16 :fold-fanout 16 :maximum-fold-levels 3
 :category-count 7 :maximum-total-rows 65536
 :maximum-row-nodes 48 :maximum-row-depth 6 :maximum-row-width 8
 :maximum-row-scalar-utf8 4096 :maximum-row-c11-bytes 8192
 :maximum-row-canonical-reader-bytes 9216
 :maximum-leaf-nodes 1024 :maximum-leaf-depth 12 :maximum-leaf-width 16
 :maximum-leaf-scalar-utf8 4096 :maximum-leaf-c11-bytes 196608
 :maximum-leaf-canonical-reader-bytes 212992
 :maximum-fold-nodes 512 :maximum-fold-depth 8 :maximum-fold-width 16
 :maximum-fold-scalar-utf8 4096 :maximum-fold-c11-bytes 65536
 :maximum-fold-canonical-reader-bytes 73728
 :maximum-commitment-nodes 64 :maximum-commitment-depth 6
 :maximum-commitment-width 16 :maximum-commitment-scalar-utf8 4096
 :maximum-commitment-c11-bytes 16384
 :maximum-commitment-canonical-reader-bytes 18432
 :maximum-root-nodes 512 :maximum-root-depth 8 :maximum-root-width 64
 :maximum-root-scalar-utf8 4096 :maximum-root-c11-bytes 65536
 :maximum-root-canonical-reader-bytes 73728}
```

Producer transcript evidence has exactly `:artifact :schema-version
:checker-algorithm-role :descriptor :records`. A raw record has exactly
`:node-ordinal :module-ordinal :caller :caller-function-source-hash
:source-path :callsite-path :node-kind :quoted-subtree-stop
:possible-call-head :classification :callee :arity :physical-ordinal
:semantic-row`. An M2 record has exactly `:node-ordinal :module-ordinal
:instruction-ordinal :caller :caller-function-source-hash :path :op
:call-classification :callee :arity :external-link :provenance-present
:provenance :occurrence-ordinal :semantic-row`.

Independent-checker transcript artifacts have exactly `:artifact
:schema-version :checker :inputs :node-count :row-count :record-root :records
:artifact-id`. Their raw records have exactly `:record-ordinal :module-ordinal
:caller :path :callsite-path :node-kind :quoted-subtree-stop
:possible-call-head :call-classification :callee :arity`; M2 records have
exactly `:record-ordinal :module-ordinal :instruction-ordinal :caller :path :op
:call-classification :callee :arity :external-link :provenance-present
:provenance`.

Producer commitment evidence has exactly `:external :builtin :provenance
:category-products`. External has exactly `:id :preimage
:source-root-preimage :m1-root-preimage`; its preimage has exactly `:domain
:schema-version :m1-id :physical-external-count
:ordered-source-external-root :ordered-m1-root :bijection-complete`; each root
preimage has exactly `:domain :schema-version :records`. Builtin is exactly
seven maps with `:id :preimage :root-preimage`; preimage has exactly `:domain
:schema-version :module-ordinal :m0-id :m2-id :occurrence-count
:ordered-multiset-root :parity-complete`; root-preimage has exactly `:domain
:schema-version :module-ordinal :occurrences`. Provenance has exactly `:id
:preimage :root-preimage`; preimage has exactly `:domain :schema-version
:m0-ids :m1-id :m2-ids :internal-builtins :occurrence-count :occurrence-root
:provenance-complete`; root-preimage has exactly `:domain :schema-version
:occurrences`.

Category-products is exactly seven maps with `:leaves :folds :commitment`.
Each leaf has exactly `:domain :schema-version :category :shard-ordinal
:first-row-ordinal :row-count :category-row-count :rows`. Each fold has exactly
`:domain :schema-version :category :level :group-ordinal
:first-child-ordinal :child-count :row-count :children`; each child has exactly
`:child-ordinal :first-row-ordinal :row-count :digest`. Commitment has exactly
`:category :row-count :leaf-count :fold-level-count :root-digest`.

Every map-bearing row slot is also closed. A coordinate is exactly
`:module-ordinal :module-source-id :plan-id :function`; a route is exactly
`:edge-ordinal :caller-plan-id :callee-plan-id`; an external link is exactly
`:artifact :manifest-id :edge-ordinal`; compiler-internal provenance is exactly
`:artifact :manifest-id :module-source-id :source-sha256 :function
:function-source-hash :callee`. A provenance occurrence is exactly
`:module-ordinal :instruction-ordinal :provenance`. A builtin occurrence is
exactly `:caller :caller-function-source-hash :builtin :arity
:compiler-internal? :duplicate-ordinal`. An external source/M1 record is
exactly `:ordinal :caller-module-source-id :caller-function
:caller-function-source-hash :callsite-path :callee-module-source-id
:callee-function :callee-function-source-hash :arity :owner-edge
:authorization`.

The seven category-row key sets, in category order, are exactly:

1. roots: `:root-ordinal :coordinate :arity`
2. members: `:member-ordinal :coordinate`
3. functions: `:member-ordinal :coordinate :definition-source-hash`
4. local runtime edges: `:edge-ordinal :caller :callee :arity
   :source-callsite-ordinal`
5. external runtime edges: `:semantic-edge-ordinal :m1-edge-ordinal
   :manifest-id :caller :callee :arity :owner-edge :route`
6. referenced builtins: `:occurrence-ordinal :caller :builtin :arity
   :compiler-internal? :source-callsite-ordinal`
7. module contracts: `:module-ordinal :module-source-id`

Map-valued `:coordinate`, `:caller`, and `:callee` use only the coordinate key
set; `:route`, `:external-link`, and `:provenance` use only their respective
sets above or are nil where the literal role schema permits nil. `:records`,
`:rows`, `:occurrences`, `:children`, and similar collections contain only the
row shape named by their parent domain. There is no generic EDN-map escape.

The fixed seven category order is `[:roots :members :functions
:local-runtime-call-edges :external-runtime-call-edges :referenced-builtins
:module-contracts]`. All ordinals are dense nonnegative integers; count fields
are nonnegative and agree with vectors; ID/root/source fields named as hashes
are strict SHA-256; C/T fields are strict Git IDs; status/artifact/domain/role
and boolean fields equal their role-specific constants. Symbols, strings,
paths, scalar row values, and list/vector/set leaves are strict plain EDN
bounded by the 17 profile limits and joined by certificate, transcript,
category, and output hashes. A map is accepted only when its parent field names
one of the literal key sets in this section. No other map key set is accepted
anywhere in `CHECKER-OUTPUT`.

A byte-level lexical pass rejects duplicate keys in every map before parsing.
UTF-8 is strict. EDN readers are `{}`, default/tagged readers are forbidden,
there is one form plus EOF, every key set/cardinality is closed, and canonical
re-encoding equals input bytes. M writes one request, closes stdin, and drains
both outputs concurrently.

`request-id`, `argv-id`, and `response-preimage-id` use the exact canonical
identity contract and domains in the table above. The response payload omits
only `[:transport :response-preimage-id]` for its preimage. `self-id-omits` is exactly
`[[:transport :response-preimage-id]]` and is not omitted. Raw response count,
SHA-256, and response ID are parent-only receipt fields and absent from the
response, so no identity is self-cyclic.

Caps are 4 MiB request, 1 GiB response, and 1 MiB stderr. The response budget
is compositional: checker output 536870912 bytes; checker identity 16777216;
source receipt 4194304; production 1048576; sentinel 1048576; counters 1048576;
TCB 33554432; diagnostics 1048576; transport/schema/framing 1048576. These
maxima sum to 596639744 bytes, leaving 477102080 bytes of mandatory envelope
headroom inside 1073741824.

The checker-output number is an observed-closed-fixture gate, not a new general
census limit. Because C/T and all twelve inputs are exact, A and B independently
measure the canonical byte count of their one deterministic output before
response construction; each count must be at most 536870912 and is persisted
in the child process receipt. M reconstructs and remeasures both outputs from
E9. The census validators retain all category-schedule maxima and no otherwise
accepted generalized census value is reclassified. I9 is eligible only if the
exact C outputs satisfy this measured gate; failure returns to architecture
instead of weakening evidence. This leaves at least 512 MiB between any
admitted checker output and the 1 GiB transport cap, and the explicit remaining
slot maxima consume exactly 57 MiB of it. Each slot is measured before
response construction, and an overflow rejects. Exit 0 with canonical
stdout and empty stderr is the only success. Child rejection/exception exits 2
with empty stdout and stderr; M creates the stable rejection diagnostic. Every
other exit or stderr byte rejects.

V9 formally supersedes rejected V7's 536870912-byte response cap. The sole V9
transport literal is 1073741824 in the request, parent and child validators,
process receipt, tests, and G9 IPC record. The value 536870912 is the maximum
observed-C checker-output slot and is never the whole-response limit.

## E9 immutable evidence

E9 is a direct sole-parent child of C and adds exactly these mode-100644 paths
under `docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/`:

1. `checker-a-identity.edn`
2. `checker-b-identity.edn`
3. `checker-a-output.edn`
4. `checker-b-output.edn`
5. `certificate-pair.edn`
6. `linked-carrier-v4-candidate.edn`
7. `identity-receipt.edn`

M writes only the first six canonical payloads into a fresh private,
symlink-free staging directory below the validated evidence parent. M closes
and postchecks them before returning its compact response; it cannot construct
the seventh file because it cannot observe its own parent launch without a
cycle. L validates the closed M response and exact six staged byte descriptors,
constructs `LAUNCHER-RECEIPT`, constructs the seventh receipt from those closed
inputs, and publishes with a no-overwrite reservation protocol. L calls
`Files.createDirectory(FINAL)` exactly once without an earlier authoritative
absence check; an existing empty or nonempty target rejects. It records the
new directory file key, then installs the six staged files into FINAL in order
with create-new semantics. It writes `identity-receipt.edn` last with
`StandardOpenOption/CREATE_NEW`; that seventh file is the sole publication
commit marker. A reader treats FINAL as unpublished until that marker exists
and then requires exactly the seven files and byte-validates all of them. This
does not claim an atomic directory rename; it provides atomic no-overwrite
reservation plus fail-closed visibility. In `:existing`, L requires the
already-published seven files to be byte-identical and writes nothing. On every
failure, L's `finally` terminates processes, closes/joins streams, and removes
only an unpublished directory whose recorded file key still matches and whose
entries are a prefix of L's exact create-new writes; a foreign entry or changed
file key is quarantined and rejects without deletion. L records process
provenance but cannot change any of the six semantic payloads. This one-way
M-response -> L-receipt construction has no response/receipt identity cycle
and cannot replace the erratum's racing empty directory.

Review and reload use `/usr/bin/git cat-file blob` on exact E9 objects. Each OID
is 40 lowercase hex and each count/SHA-256 is recomputed. The receipt joins C,
T, and the six other payloads. Its identity preimage excludes E, TE, itself,
G9, descendants, refs, branches, timestamps, host paths, and mutable worktree
observations. E9 has no other tree changes.

`identity-receipt.edn` is the durable process-isolation proof. It has exactly:

```clojure
{:artifact :gravity/sh07-b51-checker-process-identity-receipt-v1
 :schema-version 1
 :content {:commit C :tree T}
 :semantic-ordinals [0 1 2 3 4 5 6 7 8 9 10]
:coordinator-events COORDINATOR-EVENTS
:coordinator-event-root SHA
:child-process-receipts [CHILD-A CHILD-B]
:launcher-receipt LAUNCHER-RECEIPT
:coordinator-receipt COORDINATOR
 :payload-joins PAYLOAD-JOINS
 :evidence-eligibility EVIDENCE-ELIGIBILITY-JOIN
 :contract-discharge CONTRACT-DISCHARGE
 :receipt-id-normalization NORMALIZATION
 :self-id-omits [[:receipt-id]]
 :receipt-id SHA
 :nonclaims [:release :self-hosting :seed-retirement
             :hostile-host :sh07-complete]}
```

`EVIDENCE-ELIGIBILITY-JOIN` has exactly `:path :receipt-id :raw-sha256`.
Path is the literal external eligibility receipt path above; receipt ID is its
recomputed H identity; raw SHA is R of its exact W bytes. The eligibility
receipt cannot reference E9, and E9 references only this already-fixed object,
so the edge is one-way and acyclic.

`CONTRACT-DISCHARGE` has exactly `:schema :content
:document-read-receipt :rows :row-root :proof-scope :progress :nonclaims`.
Schema is `:gravity/sh07-b51-contract-discharge-v1`; content is
`{:commit C :tree T}`; progress is exactly `{:slice :SH-07 :status :partial
:completed-stage-count 4 :total-stage-count 8}`; nonclaims are exactly
`[:formal-proof :sh07-complete :self-hosting :release :seed-retirement]`.

The document-read receipt has exactly `:schema :content :documents :events
:event-root :counters :finalized`. Schema is
`:gravity/sh07-b51-contract-read-receipt-v1`; it contains the 31 unique
mode-100644 C objects named by this report's governing-contract list, in that
literal list order after first-occurrence deduplication, plus 31 dense matching
read events. Counters are exactly `{:acquire-count 31 :decode-count 31
:event-count 31 :finalize-count 1}` and finalized is true. Each document
descriptor has exactly `:ordinal :path :mode :blob-oid :byte-count
:raw-sha256`; each event has exactly `:ordinal :document-ordinal :path
:raw-sha256 :binding-ids`. This separate contract session is not folded into
the 12-source semantic session.

Rows are the literal 19-row vector in this order: `:agents`, `:foundation`,
`:l1`, `:l2`, `:c2`, `:c6`, `:c11`, `:boot1`, `:boot2`, `:boot3`, `:boot4`,
`:boot5`, `:boot6-8`, `:test10`, `:test11`, `:test13`,
`:sh07-proof-contract`, `:backlog-ownership`, and
`:catalog-checked-core-governance`. Each row has exactly `:ordinal :id
:document-ordinals :sections :disposition :bindings :test-ids :diagnostic`.
Sections are nonempty exact heading strings from the corresponding table row;
disposition is `:applies`, `:preserved`, or `:not-applicable`; diagnostic is
the table's literal keyword; and at least one binding is required. Each binding
has exactly `:id :artifact :path :value-id`, where path is an EDN get-in vector
and value ID is `H([:gravity/sh07-b51-contract-binding-v1 {:id ID :artifact
ARTIFACT :path PATH :value (get-in ARTIFACT PATH)}])`. The row vector must
equal an architecture-owned literal row-spec; runtime-selected headings,
bindings, tests, blank vectors, or an unlisted row reject. Row root H-hashes
the exact vector.

Proof scope is exactly `{:evidence-kind :validation :formal-proof false
:candidate-scope :sh07-b51-git-object-publication :proof-contract
{:artifact :gravity/sh07-proof-process-contract :schema-version 2
:status :active :coverage-milestone "SH-07-B47" :task "SH-07-B47"
:input-task "SH-07-B15" :scope :sh07-b15-keyword-map-lookup
:application :preserved-out-of-scope} :verification-catalog
{:schema :gravity/verification-catalog-v1 :authority :non-authoritative
:b51-route-id nil :verifier-root-id nil} :adopted-independent-controls
[:fresh-authoritative-process :immutable-source-snapshot :immutable-receipts
:bounded-execution :finally-cleanup] :b51-proof-contract-revision-required
true}`. This explicitly prevents the B47/B15 contract or catalog from being
misrepresented as B5.1 formal-proof authority.

Each M-authored child receipt has exactly `:schema :role :ordinal-triple :argv
:argv-id :request :response :process :process-join :counters :tcb`. Schema is
`:gravity/sh07-b51-child-process-receipt-v1`; A's triple is `[2 3 4]` and B's
is `[6 7 8]`. Request has exactly `:value :request-id :byte-count
:raw-sha256`, where bytes are W(value), raw SHA is R(bytes), and request ID is
the domain-bound H identity. Response has exactly `:transport :reconstruction
:byte-count :raw-sha256 :response-preimage-id`; its bytes and raw SHA are W
and R. Process is `RETAINED-PROCESS`, process join is `PROCESS-JOIN`, counters
are the exact child counters, and TCB is the exact closed child TCB.
Response reconstruction has exactly `:schema :role
:checker-identity-payload :checker-output-payload :source-read-receipt
:production :sentinel :diagnostics`; the two payload fields are respectively
`:checker-a-identity.edn`/`:checker-a-output.edn` or the B paths, and all other
fields are the literal response values. Substituting the two referenced E9
payload values and the child receipt's counters/TCB into the closed response
schema must reproduce W(response) byte-for-byte and therefore its byte count,
raw SHA, and `response-preimage-id`.

`COORDINATOR-EVENTS` is exactly 11 maps with exactly `:ordinal :event :role
:process-handle-id :argv-id :request-id :response-preimage-id
:source-session-id :prior-event-root :event-id`. The event/role pairs are
exactly `[[:m-process-start :m] [:a-builder :a] [:a-start :a]
[:a-exit-observed :a] [:a-response-validated :a] [:b-builder :b]
[:b-start :b] [:b-exit-observed :b] [:b-response-validated :b]
[:m-session-start :m] [:m-session-finalize :m]]`. An inapplicable coordinate is
nil. A builder row joins its normalized argv ID; a start/exit row joins the
retained process-handle ID; a validation row joins request and response-preimage
IDs; an M-session row joins M's source-session ID. `prior-event-root` is nil at
ordinal 0 and otherwise the previous row's `event-id`. Each `event-id` is the
canonical hash under `:gravity/sh07-b51-coordinator-event-id-v1` of its row with
only `:event-id` omitted. `coordinator-event-root` hashes the exact vector.
Process-handle ID is durable, not Java object identity. It is the canonical
hash under `:gravity/sh07-b51-process-handle-coordinate-id-v1` of exactly
`{:role ROLE :child-sequence :m|:a|:b :normalized-argv-id SHA
:request-id SHA-OR-NIL}`. M uses `:role :m`, `:child-sequence :m`, and nil
request ID; A and B use their role, matching child sequence, and nonnil request
ID.
The retained live `Process` reference and pipes remain in-run authority and
join their raw PID/parent observations, but are not serialized or hashed. The A
exit row must contain exit 0 in its joined child receipt, and the A validation row's
fully recomputed response joins before the B builder row can be appended. The
same rule joins B validation before M session start. A single append-only state
machine owns the vector; skipped, preconstructed, reordered, overwritten, or
overlapping events reject.

`source-session-id` is the canonical hash under
`:gravity/sh07-b51-governed-source-session-id-v1` of exactly `{:role ROLE
:mode MODE :commit C :tree T :source-descriptors-root SHA :event-root SHA
:counters {:acquire-count 12 :decode-count 12 :event-count 39
:finalize-count 1}}`. Every child ID is recomputed from its embedded
`source-read-receipt`; M's ID is recomputed from the coordinator receipt. The
ordinal-9 and ordinal-10 rows carry that same M ID. No object identity, path,
PID, host, or time participates.

Coordinator receipt has exactly `:schema :process :runtime-mxbean :java :git
:classpath-checks :environment :source-closure :load-events
:loaded-namespace-receipt :host-surface :source-read-receipt
:child-process-receipts :process-joins :coordinator-events
:coordinator-event-root :counters :a-validated-before-b-builder
:b-validated-before-m-session :launcher-process-handle-id
:launcher-argv-id`. Schema is `:gravity/sh07-b51-coordinator-receipt-v1`;
process is `SELF-PROCESS`; the two child receipts are exact A then B; process
joins contains the exact pid vector, pairwise-distinct flag, parent edges, and
per-process joins above; and both ordering booleans are true. `load-events` is
a dense vector with exactly `:ordinal :operation :namespace :phase :allowed`;
every allowed value is true and its order proves preflight, codec load,
materializer load, A completion, B completion, then M bootstrap/session load.
Classpath checks are exactly eight rows for phases `[:m-entry :pre-a :post-a
:pre-b :post-b :pre-source-session :post-source-session :m-exit]`; each row
has exactly `:phase :entries :manifest-root :equal-to-entry-root` and the final
flag is true. Its launcher IDs equal the independently recomputed
`LAUNCHER-RECEIPT` values. Its loaded namespace receipt excludes both checker
namespaces and represents the child entrypoint only as the opaque argv string,
not a loaded namespace. Its process counters and source receipt join the G9
aggregates and semantic ordinals.

PAYLOAD-JOINS has exactly `:checker-identity-raw-sha256s
:checker-output-raw-sha256s :certificate-pair-raw-sha256
:linked-carrier-raw-sha256`. The vectors are ordered A then B and every digest
equals a fresh raw hash of the named sibling blob. NORMALIZATION is exactly
`{:algorithm :gravity/sh07-b51-explicit-semantic-projection-v1
:receipt-only :gravity/receipt-only
:child-indexes [0 1] :classpath-indexes [0 1 2 3]
:coordinator-classpath-phases [:m-entry :pre-a :post-a :pre-b :post-b
:pre-source-session :post-source-session :m-exit]
:recompute-order [:payload-value-ids :classpath-roots :argv-ids :request-ids
:tcb-manifest-ids :response-values-and-transport :process-handle-ids
:source-session-ids :coordinator-event-ids :coordinator-event-root
:coordinator-receipt :m-response :launcher-receipt-id
:contract-event-root :contract-row-root :receipt-id]}`. It does not perform a
global map-key walk. It constructs a new normalized receipt from the closed
role-specific functions below, so a same-named key at any unlisted path is
untouched and an unrecognized path rejects.

Let `RO` be `:gravity/receipt-only`. `N-ARGV` replaces only argv index 0 with
`:gravity/java-executable` and index 11 with `:gravity/classpath`.
`N-SELF-PROCESS` replaces exactly `:pid :parent-pid :command :arguments
:start-instant` with RO. `N-RUNTIME-MXBEAN` replaces exactly `:name :pid :host
:start-time-millis :java-class-path` with RO; therefore the embedded PID@host
string cannot leak. `N-EXECUTABLE` replaces exactly `:canonical-path
:file-key`; `N-CLASSPATH` applies that replacement to each of four entries;
`N-ENVIRONMENT` replaces exactly `:working-directory`; `N-RETAINED-PROCESS`
replaces exactly `:pid :parent-pid :command :arguments :start-instant` and
recomputes rather than copies its process-handle ID; `N-STAGING` replaces only
`:staging-path`.

Those functions apply only at child receipt paths `[i :tcb :process]`, `[i
:tcb :runtime-mxbean]`, `[i :tcb :java]`, `[i :tcb :git]`, `[i :tcb
:classpath j]`, `[i :tcb :environment]`, and `[i :process]`; coordinator paths
`:process`, `:runtime-mxbean`, `:java`, `:git`, `[:classpath-checks k :entries
j]`, and `:environment`; and launcher paths `:launcher-process`, `:java`,
`:git`, `[:classpath-precheck :entries j]`, `[:classpath-postcheck :entries
j]`, `:environment`, `:working-directory`, `:m-process`,
`:m-response-transport`, and `:cleanup`. Here i is 0 or 1, j is 0-3, and k is
one of the eight literal phases. Receipt-only booleans/counters remain present;
raw path/PID/time/process-control values at the listed paths become RO.

Recomputation follows the literal order in NORMALIZATION: six payload value
IDs; classpath manifest and semantic roots; normalized M/A/B argv IDs; A/B
request IDs; normalized A/B/M TCB IDs; normalized A/B/M responses with W byte
counts, R hashes, and response-preimage IDs; M/A/B process-handle IDs; three
source-session IDs; coordinator event IDs left-to-right and event root;
coordinator receipt; M response with W count/R hash/preimage ID; launcher
receipt ID; contract event/row roots; and final receipt ID. Raw argv IDs,
response IDs/hashes/counts, process-handle IDs, event IDs/roots, TCB/root IDs,
and payload raw hashes are never copied into the normalized projection.

All checker envelopes and provenance use the exact semantic
string `"1970-01-01T00:00:00Z"` for `:completed-at` before any invocation,
reproduction, certificate, evidence, carrier, output, or pin ID is computed.
No wall-clock value enters a payload or payload-derived identity. Receipt ID is
`H([:gravity/sh07-b51-checker-process-identity-receipt-id-v1
NORMALIZED-RECEIPT-WITHOUT-RECEIPT-ID])`. E/TE, own blob OID/raw SHA, G9,
refs, branches, paths outside the seven payloads, wall-clock timestamps, PIDs, host names,
file keys, and absolute host paths therefore do not enter the identity
preimage, while their original receipt-only observations remain available for
response reconstruction and review. Thus an object-only
review can reconstruct both canonical child responses and independently verify
A exit/validation before B construction, process parentage/distinctness,
argv/request/response joins, TCB and loaded-namespace separation, both source
sessions, and M's later independent session.

## G9 publication tuple

G9 adds exactly
`docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/g9-publication-tuple.edn`.
It is strict canonical plain EDN with exactly:

```clojure
{:artifact :gravity/sh07-b51-git-object-publication-tuple-v1
 :schema-version 1
 :object-format "sha1"
 :content CONTENT
 :evidence EVIDENCE
 :publication-base PUBLICATION-BASE
 :lineage-relations LINEAGE-RELATIONS
 :git-observations OBSERVATIONS
 :artifact-descriptors ARTIFACT-DESCRIPTORS
 :source-descriptors SOURCE-DESCRIPTORS
 :masked-root MASKED-ROOT
 :counters COUNTERS
 :receipt-joins RECEIPT-JOINS
 :governance GOVERNANCE
 :nonclaims NONCLAIMS}
```

Nested schemas are closed:

- `CONTENT` is exactly `{:commit C :tree T}`.
- `EVIDENCE` is exactly `{:commit E :tree TE :parents [C]}`.
- `PUBLICATION-BASE` is exactly `{:commit B :tree TB :parents
  BASE-PARENTS}` and is copied only from the external accepted I9 integration
  receipt. `LINEAGE-RELATIONS` is exactly `{:content-is-ancestor-of-base
  C/B-TRUE-ANCESTRY-RESULT :evidence-is-ancestor-of-base
  E/B-FALSE-ANCESTRY-RESULT}`; both values are copied from and
  revalidated against the external I9 integration receipt, including its
  expected-fail E/B ancestry observation. B/TB and ordered parents are known
  before G9 and therefore create no self-cycle; G/TG/OG are forbidden here.
- `OBSERVATIONS` is the exact 47-entry dense-ordinal vector specified below.
  Each entry has exactly
  `:ordinal :operation :argv :stdin-byte-count :stdin-raw-sha256 :exit-code
  :stdout-byte-count :stdout-raw-sha256 :stderr-byte-count :stderr-raw-sha256
  :parsed-result`. Operation is one of `:rev-parse-commit :show-tree :ls-tree
  :cat-file-blob :merge-base-is-ancestor :diff-tree`. The validator constructs
  the sole allowed absolute Git argv. Counts/hashes/output parsing and required
  success code recompute. Stdin is always empty: count 0 and SHA-256
  `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`.
- `ARTIFACT-DESCRIPTORS` is exactly seven ordered maps with exactly `:path
  :mode :type :blob-oid :byte-count :raw-sha256` for E9; mode is `"100644"`,
  type is `"blob"`, and raw SHA is R(blob bytes).
- `SOURCE-DESCRIPTORS` is exactly the twelve ordered closed source maps.
- `MASKED-ROOT` has exactly `:coordinates :preimage-spans :postimage-spans
  :preimage-files :postimage-files :preimage-root :postimage-root :equal`.
  The five coordinates retain the exact paths, load ordinals, form ordinals,
  owners, and RHS kinds in this literal vector:
  `[[00945-PATH 808 13 gravity.bootstrap/p15-s23-sh07-b51-expected-m3-id
  :digest-pin] [00945-PATH 808 14
  gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-a-id :digest-pin]
  [00945-PATH 808 15
  gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-b-id :digest-pin]
  [00945-PATH 808 16
  gravity.bootstrap/p15-s23-sh07-b51-expected-semantic-root :digest-pin]
  [00947-PATH 812 59
  gravity.bootstrap/p15-s23-sh07-b51-authoritative-linked-carrier-value-v4
  :quoted-carrier-v4]]`, where 00945-PATH is exactly
  `"bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"`
  and 00947-PATH is exactly
  `"bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"`.
  Each coordinate adds exactly `:left-delimiter
  :right-delimiter :preceding-byte :following-byte :masked-rhs-byte-count`.
  For owner O, left delimiter is the literal string
  `"(def ^:private " + str(O) + "\n  "`, right delimiter is `")"`, preceding
  and following bytes are both decimal 10, and the mask is UTF-8
  `:gravity/sh07-b51-masked-rhs-v1`. Each coordinate must have exactly one full
  byte occurrence `LF || LEFT(O) || RHS || ")" || LF` at the declared top-level
  form ordinal.

  Each pre/post span has exactly `:coordinate-ordinal :form-start-byte
  :rhs-start-byte :rhs-end-byte :form-end-byte :rhs-byte-count
  :rhs-raw-sha256`. Offsets are observational and excluded from the stable
  root. Preimage RHS is exactly UTF-8
  `:gravity/provisional-no-authority`; postimage digest RHS is exactly UTF-8
  `pr-str(SHA)` and carrier RHS is `drop-final-LF(W((quote CARRIER)))`.
  The five RHS spans are replaced in descending byte-offset order. Each of the
  two file descriptors has exactly `:path :masked-byte-count
  :masked-raw-sha256`. Pre/post roots are each
  `H([:gravity/sh07-b51-masked-source-root-v1 {:coordinates
  FIVE-CLOSED-COORDINATES :files ORDERED-TWO-MASKED-FILE-DESCRIPTORS}])` and
  must be equal. Spans are excluded, but paths, load/form ordinals, owners,
  kinds, delimiters, adjacent bytes, mask byte count, and masked file
  descriptors are all inside H. This binds every
  non-RHS byte, including delimiters and adjacent bytes, without putting the
  changed RHS lengths into the stable identity.
- `COUNTERS` is exactly `{:work WORK :source SOURCE :process PROCESS-COUNTERS
  :ipc IPC-COUNTERS :git GIT-COUNTERS}`. Its literal schemas and aggregation
  are specified below.
- `RECEIPT-JOINS` is exactly `{:content-commit C :content-tree T
  :evidence-commit E :evidence-tree TE :evidence-eligibility-id SHA
  :evidence-eligibility-raw-sha256 SHA :i9-integration-id SHA
  :i9-integration-raw-sha256 SHA :identity-receipt-raw-sha256 SHA
  :payload-raw-sha256s VECTOR :excluded VECTOR}`. Receipt IDs are their
  domain-bound H values; every raw SHA is R of the named
  blob bytes; payload hashes are the six
  non-receipt artifacts. Excluded is exactly `[:evidence-commit :evidence-tree
  :identity-receipt-self :publication-tuple :descendants :refs :branches
  :timestamps :pids :host-names :file-keys :host-paths]` for the receipt
  identity preimage.
- `GOVERNANCE` is exactly
  `{:id "sh07-b51-git-object-evidence-publication-governance-v9"
  :invariant "architecture/self-hosting-sh07-b51-pin-free-publication-v1"
  :required-terminal :integrated}`.
- `NONCLAIMS` is exactly `[:release :self-hosting :seed-retirement
  :hostile-host :sh07-complete]`.

For Git operation variables, `REPO` is the canonical repository-root
observation, `X` a bound commit, `Y` its bound tree, `P` a literal safe path,
and `O` its bound blob OID. Every commit, tree, parent, and blob OID scalar is
exactly 40 lowercase hexadecimal characters. `ZERO-OID` is exactly 40 ASCII
`0` characters. Parent vectors preserve Git's observed order and are never
sorted. This table governs only the tuple's 47 pre-G9 C/E
observations. The external 24-row receipt has the separate closed enum and
NUL grammars below. The only internal argv/result pairs are:

| Operation | Exact argv | Exact parsed-result keys |
| --- | --- | --- |
| `:rev-parse-commit` | `[/usr/bin/git -C REPO rev-parse --verify X^{commit}]` | `{:commit X}` |
| `:show-tree` | `[/usr/bin/git -C REPO show -s --format=%H%n%T%n%P X]` | `{:commit X :tree Y :parents VECTOR}` |
| `:diff-tree` | `[/usr/bin/git -C REPO diff-tree --no-commit-id --name-status -r X^ X]` | `{:parent PARENT :commit X :entries VECTOR}` |
| `:merge-base-is-ancestor` | `[/usr/bin/git -C REPO merge-base --is-ancestor ANCESTOR DESCENDANT]` | `{:ancestor ANCESTOR :descendant DESCENDANT :is-ancestor true :exit-code 0}` |
| `:ls-tree` | `[/usr/bin/git -C REPO ls-tree -z X -- P]` | `{:commit X :path P :mode "100644" :type "blob" :oid O}` |
| `:cat-file-blob` | `[/usr/bin/git -C REPO cat-file blob O]` | `{:oid O :byte-count COUNT :raw-sha256 SHA}` |

Every token in the argv table is a separate string; the rendered table omits
quotes only for readability. Parent vectors and diff entries are closed vectors
of 40-hex IDs and exactly `{:status :added|:modified|:deleted :path STRING}`
maps. V9 imposes no parent identity on C beyond the accepted I9 lifecycle and
required integrated A9 ancestry. E has exactly `[C]`; the E diff has exactly
the seven `:added` evidence paths. The observation plan is: ordinals
0-1 rev/show C; 2-3 rev/show E; 4 diff E; 5 ancestry C-to-E; 6-12 E `ls-tree`
in artifact order; for each `j` in 0-6, ordinal `13+j` is `cat-file` of the OID
returned at ordinal `6+j`; 20-31 C `ls-tree` in source order; for each `j` in
0-11, ordinal `32+j` is `cat-file` of the OID returned at ordinal `20+j`; 44
rev C, 45 rev E, and 46 ancestry C-to-E as independent postchecks. Any other
count/order/argv/result rejects. The tuple does not observe its own G9 object or
bytes; the lifecycle join below supplies that external binding and avoids a
self-cycle.

G9 `WORK` is exactly
`{:mode MODE :sessions WORK-SESSIONS :total TOTAL}`. `WORK-SESSIONS` is exactly three
ordered maps for participants `[:a :b :m]`, each with exactly `:participant
:vector :total`; vector/total are `[3 3 3 2 2]`/13 in `:new` or
`[2 2 2 2 2]`/10 in `:existing`; TOTAL is 39 or 30. `SOURCE` is exactly
`{:sessions SOURCE-SESSIONS :acquire-count 36 :decode-count 36 :event-count 117
:finalize-count 3}`; `SOURCE-SESSIONS` is exactly three ordered maps with
`:participant :acquire-count :decode-count :event-count :finalize-count`, each
equal to 12/12/39/1. `PROCESS-COUNTERS` is exactly
`{:launcher-count 1 :coordinator-count 1 :builder-count 3 :child-count 3
:start-count 3 :exit-count 3 :m-launch-count 1 :a-b-overlap-count 0
:m-parent :launcher :checker-parent :m
:semantic-ordinals [0 1 2 3 4 5 6 7 8 9 10]}`. `IPC-COUNTERS` is exactly
`{:checker-request-count 2 :checker-response-count 2 :m-response-count 1
:rejection-count 0 :request-max-bytes 4194304
:response-max-bytes 1073741824 :stderr-max-bytes 1048576
:deadline-ms 3600000}`. L applies the
response and stderr caps to M under the same bounded process deadline; M has no
stdin payload because its closed request is the exact argv above.
`GIT-COUNTERS` is exactly `{:operation-count 47
:stdin-byte-count 0 :stdout-byte-count N :stderr-byte-count 0}`, where N is the
sum of the 47 recorded stdout byte counts. No participant, counter, or aggregate
may be missing or added. These are the tuple's pre-G9 C/E observations. The
post-G9 lifecycle observation receipt separately has exactly 24 operations and
cannot be folded into, omitted from, or preseeded in this counter.

`gravity.tools.sh07-b51-git-evidence --mode g9-lifecycle-join` reads the tuple
from exact G9, the external evidence-eligibility receipt, and the external I9
integration receipt. The static C fixture contains only the closed derivation
grammar and literal paths; it contains no E/B/G commit, tree, OID, count, or
hash. The tool writes exactly W of
`docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-lifecycle-receipt.edn`.
That receipt has exactly `:artifact :schema-version :content :evidence
:publication-base :publication :tuple :observations :result :pre-post-equal
:joins :review :acceptance :admission :integration :self-id-omits :nonclaims
:receipt-id`. Artifact is
`:gravity/sh07-b51-g9-lifecycle-receipt-v1`, version is 1, content is
`{:commit C :tree T}`, evidence is `{:commit E :tree TE :parents [C]}`, base is
`{:commit B :tree TB :parents BASE-PARENTS}`, and publication is
`{:commit G :tree TG :parents [B]}`. Tuple has exactly `:path :mode :type :oid
:byte-count :raw-sha256 :value`, with mode `"100644"`, type `"blob"`, value
equal to the strictly reloaded tuple, and raw SHA R(tuple bytes). Joins has exactly `:evidence-eligibility-id
:i9-integration-id :e9-identity-receipt-id`; review is the closed REVIEW map;
acceptance is exactly `{:state :accepted}`; admission is exactly
`{:state :integration-eligible}`; integration is exactly `{:state :integrated
:commit G :tree TG :parents [B] :base-is-ancestor ANC}`, where ANC is the
B/G true ANCESTRY-RESULT;
self-id-omits is exactly `[[:receipt-id]]`; nonclaims are the five V9
nonclaims; and pre-post-equal is true. Receipt ID is
`H([:gravity/sh07-b51-g9-lifecycle-receipt-v1 RECEIPT-WITHOUT-ID])`; its Git
blob OID/raw hash and the review/lifecycle commits are external and never enter
that preimage.

`observations` is exactly 24 dense rows. A row has exactly `:ordinal
:operation :argv :stdin-byte-count :stdin-raw-sha256 :exit-code
:stdout-byte-count :stdout-raw-sha256 :stderr-byte-count :stderr-raw-sha256
:parsed-result`. Stdin is always empty and R(empty); stderr is empty and
R(empty). The exact operation vector is `[:show-tree
:merge-base-is-ancestor :ls-tree-z-requested :show-tree :show-tree
:merge-base-is-ancestor :merge-base-is-ancestor :diff-tree-name-status-z
:diff-tree-raw-z :ls-tree-z-requested :cat-file-blob
:ls-tree-z-requested]`. It occurs at ordinals 0-11 and is
repeated byte-for-byte at 12-23 after tuple/receipt reconstruction:

1. `show -s --format=%H%n%T%n%P B` -> exactly B, TB, ordered BASE-PARENTS.
2. `merge-base --is-ancestor C B` -> exit 0 and true.
3. `ls-tree -z B -- REQUESTED-PATHS` -> every I9-owned C path present with the
   exact preserved descriptor and every E9/G9 evidence path absent.
4. `show -s --format=%H%n%T%n%P E` -> exactly E, TE, `[C]`.
5. `show -s --format=%H%n%T%n%P G` -> exactly G, TG, `[B]`.
6. `merge-base --is-ancestor B G` -> exit 0 and true.
7. `merge-base --is-ancestor E G` -> exit 1 and false.
8. `diff-tree --no-commit-id --name-status -r -z B G` -> exactly one added
   tuple path.
9. `diff-tree --no-commit-id --raw -r -z B G` -> exactly old mode `000000`, new mode
   `100644`, all-zero old OID, OG, status A, and tuple path.
10. `ls-tree -z G -- TUPLE-PATH` -> present mode `100644`, type blob, OG.
11. `cat-file blob OG` -> exactly W(parsed tuple), its byte count, and R(bytes).
12. `ls-tree -z E -- TUPLE-PATH` -> absent.

The external `:operation` enum is exactly `:show-tree`,
`:merge-base-is-ancestor`, `:ls-tree-z-requested`,
`:diff-tree-name-status-z`, `:diff-tree-raw-z`, or `:cat-file-blob`. Its sole
argv/result shapes are:

| Operation | Exact argv after `/usr/bin/git -C REPO` | Exact parsed result |
| --- | --- | --- |
| `:show-tree` | `show -s --format=%H%n%T%n%P X` | `{:commit X :tree TX :parents PARENTS}` |
| `:merge-base-is-ancestor` | `merge-base --is-ancestor X Y` | `{:ancestor X :descendant Y :is-ancestor BOOLEAN :exit-code 0-OR-1}` |
| `:ls-tree-z-requested` | `ls-tree -z X -- P1 ... PN` | `{:commit X :requested-paths [P1 ... PN] :rows ROWS}` |
| `:diff-tree-name-status-z` | `diff-tree --no-commit-id --name-status -r -z B G` | `{:parent B :commit G :entries [{:status :added :path TUPLE-PATH}]}` |
| `:diff-tree-raw-z` | `diff-tree --no-commit-id --raw -r -z B G` | `{:parent B :commit G :entries [{:old-mode "000000" :new-mode "100644" :old-oid ZERO-OID :new-oid OG :status :added :path TUPLE-PATH}]}` |
| `:cat-file-blob` | `cat-file blob OG` | `{:oid OG :byte-count N :raw-sha256 SHA :value TUPLE}` |

Every argv token is a distinct string and no alternate flag ordering is
accepted. False ancestry is allowed only for the literal E/G row and requires
exit 1; all other rows require exit 0.

`REQUESTED-PATHS` is exactly the already-defined UTF-8-byte-sorted
`OWNED-C-PATHS`, followed by the seven E9 paths in artifact order and then the
tuple path. The three groups are disjoint; a duplicate rejects rather than
being removed. `:show-tree` parses exactly three LF-terminated
lines into `{:commit X :tree Y :parents VECTOR}`. `:merge-base-is-ancestor`
accepts only exit 0/true or exit 1/false and requires empty stdout/stderr.
`:ls-tree-z-requested` parses only NUL-terminated records `MODE SP TYPE SP OID TAB PATH
NUL`, rejects duplicate/unrequested paths, and projects every requested path
to exactly `{:path P :present BOOLEAN :mode STRING-OR-NIL :type
STRING-OR-NIL :oid OID-OR-NIL}`. `:diff-tree-name-status-z` parses only
`A NUL PATH NUL` into one `{:status :added :path P}`. `:diff-tree-raw-z`
parses only `ASCII-COLON OLDMODE SP NEWMODE SP OLDOID SP NEWOID SP A NUL PATH
NUL`, where ASCII-COLON is byte `0x3a`, into
one map with exactly `:old-mode :new-mode :old-oid :new-oid :status :path`.
`:cat-file-blob` parses strict W and returns exactly `{:oid OG :byte-count N
:raw-sha256 SHA :value TUPLE}`. Any missing final NUL/LF, alternate token,
extra record, non-UTF-8 path, quoted-path mode, or parser remainder rejects.

The external `RESULT` has exactly `:publication-base
:publication-base-tree :publication-base-parents :content-commit :content-tree
:content-is-ancestor-of-base :evidence-commit :evidence-tree
:evidence-parents :evidence-is-ancestor-of-base
:evidence-is-ancestor-of-publication :publication-commit
:publication-tree :publication-parents :base-is-ancestor :preserved-blobs
:diff :raw-diff :tuple :tuple-absent-at-base :tuple-absent-at-evidence
:pre-post-equal`. It records B/TB/BASE-PARENTS and the C/B true, E/B false,
E/G false, and B/G true ANCESTRY-RESULT maps, including their exit codes. It
records C/T, E/TE/[C], G/TG/[B], the
exact I9 preservation rows, the parsed one-entry diffs, the
tuple map exactly equal to the receipt's closed `:tuple` map (including
descriptor and parsed value), both absence booleans true, and pre/post equal
true. No value is caller-supplied. Generic governance remains shape-only; the
workstream-specific independent review enforces this receipt and records its
path, receipt ID, raw SHA, G/TG/OG, and exact candidate tree before acceptance.

These checks are external because the tuple cannot contain its own G9 identity
without a cycle. They prove the exact sole-parent topology, base relation,
tree, tuple blob, and one-entry delta. A tuple graft, extra parent, merge
commit, parent other than B, extra file, modified/deleted file, alternate mode,
or substituted tuple bytes rejects as `SH07-B51-GIT-PUBLICATION-DELTA` before
acceptance. The artifacts never modify C, E, or G9 and are not tuple identity
inputs. Any attempt to preseed future values in C rejects as
`SH07-B51-IDENTITY-CYCLE`. G9 is not a historical-schema extension.
Before accepting those strings it also reads the exact identity-receipt blob,
reconstructs both child responses from the six sibling payloads, reruns every
receipt-ID normalization/hash, verifies A/B/M isolation and ordinals, and joins
its digest to `RECEIPT-JOINS`. A tuple carrying counters without that process
proof rejects with `SH07-B51-CHECKER-PROCESS`.

## Owned implementation and tests

Ownership is closed for the entire lifecycle. `:master-coordinator` owns A9
reports, the two `gravity.bootstrap` integration components, all
`gravity.tools` launcher/coordinator/checker-process files, generated E9/G9
artifacts, workstream records, reconciliation, and P9 pin publication.
`:sh-core` owns the SH-07 semantic review and the dedicated SH-07 tests and
fixtures below. The seven Gravity B5.1 modules, `checked_core.gravity`, generic
load order, central runners, backlog, ownership file, generic governance, and
verification catalog are read-only inputs. Crossing this allocation requires a
separate recorded reservation and owner handoff before edit; otherwise
`SH07-B51-OWNER` rejects.

I9's allowed path set is the exact union of the implementation, test, and
fixture paths listed in this section. It may change no other path. The
implementation subset is:

- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00581-p15-s23-stage2-compiler-artifact-binding.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00582-p15-s23-stage2-construct-emitted-core-plan.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle.clj`

- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj`

Exact tests:

- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_git_evidence_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_tier0_materializer_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_plan_linker_governed_read_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_checker_child_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_process_isolation_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_test.clj`

Fixtures are exactly
`bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn`
and
`bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn`.
The accepted fixture contains the exact twelve descriptors and 39 events. The
rejected fixture contains one case for every omitted/extra/reordered source or
event, direct-read escape, preflight/static-load violation, malformed UTF-8,
wire divergence, PID/host-only normalization mutation, empty-target race, and
checker-A-before-B fail-fast violation.
Load order and generic governance schema/validator bytes are unchanged.

E9 may add only its seven evidence paths. G9 may add only
`docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/g9-publication-tuple.edn`
relative to B. Its post-G9 reviewer may add only the formal review report and
governed lifecycle record. P9 may modify only the four literal definitions in
00945 and the one quoted-carrier definition in 00947; it may add its own
review/admission records but may not change I9 logic, E9 payloads, the G9 tuple,
tests, fixtures, contracts, catalog, load order, or unrelated latest-main
paths.

The exact final owner set is:

- `gravity.bootstrap/p15-s23-sh07-b51-module-specs`
- `gravity.bootstrap/read-forms`
- `gravity.bootstrap/p15-s23-sh07-b51-attempt32-architecture-report-sha256`
- `gravity.bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?`
- `gravity.bootstrap/p15-s23-sh07-b51-current-bundle-core-products`
- `gravity.bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate`
- `gravity.bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid?`
- `gravity.bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map`
- `gravity.bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products`

Former direct-reader, caller-session, checker-in-M, worktree/ref authority,
unreceipted external binding, and unauthorized cross-fragment families reject.

## Diagnostics and acceptance

Stable diagnostic IDs are exactly:

- `SH07-B51-GIT-SNAPSHOT`
- `SH07-B51-GIT-INPUT-BLOB`
- `SH07-B51-GIT-PUBLICATION-DELTA`
- `SH07-B51-GIT-TUPLE`
- `SH07-B51-GIT-RELOAD`
- `SH07-B51-RECEIPT-JOIN`
- `SH07-B51-IDENTITY-CYCLE`
- `SH07-B51-00945-GOVERNED-READ`
- `SH07-B51-00947-QUOTED-CARRIER`
- `SH07-B51-CHECKER-ORDER`
- `SH07-B51-NORMALIZE-DIVERGENCE`
- `SH07-B51-AUTHORITY-TAINT`
- `SH07-B51-TRUSTED-TCB`
- `SH07-B51-FORBIDDEN-VAR`
- `SH07-B51-EDGE-AUTHORITY`
- `SH07-B51-G9-TUPLE`
- `SH07-B51-CHECKER-CHILD`
- `SH07-B51-CHECKER-IPC`
- `SH07-B51-CHECKER-PROCESS`
- `SH07-B51-CONTRACT-DISCHARGE`
- `SH07-B51-PROOF-SCOPE`
- `SH07-B51-OWNER`
- `SH07-B51-VERIFY-CATALOG`

Each negative fixture has one primary ID and closed details. Implementation
review confirms all sources, schemas, bounds, semantic ordinals, two independent
child closures, 12/12/39/1 sessions, 13/10 totals, seven E9 blobs, G9, dual
grammar, equal masked root, 1480 edges, object-only runtime authority, and
unchanged generic governance/load-order bytes. It runs targeted positive and
negative tests, the governed SH-07 evidence suite, documentation validation,
and independent exact-tree review before acceptance.

All contract-discharge rows above remain mandatory. Architecture acceptance and
integration only permit I9 creation. I9 content freeze only permits the named
evidence-eligibility review; that review only permits E9 construction. I9
acceptance, admission, and integration are separate. Integrated I9 only permits
G9 construction; G9 external review, acceptance, admission, and integration are
separate. Only integrated G9 permits P9. None alone completes SH-07.
