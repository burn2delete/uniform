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
  has exactly `:artifact :schema-version :content :freeze :source-descriptors
  :source-descriptors-root :masked-root :pre-evidence-contract-discharge :review :state
  :self-id-omits :nonclaims :receipt-id`. Artifact is
  `:gravity/sh07-b51-evidence-eligibility-receipt-v2`, version is 2, content is
  `{:commit C :tree T}`, state is `:evidence-eligible`, and receipt ID is
  `H([:gravity/sh07-b51-evidence-eligibility-receipt-v2 RECEIPT-WITHOUT-ID])`;
  self-id-omits is exactly `[[:receipt-id]]`. Freeze has exactly `:state
  :candidate-commit :candidate-tree :active-record-ref :frozen-at`; state is
  `:frozen`, candidate is C/T, active-record-ref is the immutable governance
  lifecycle receipt that performed the `draft -> frozen` transition, and
  frozen-at is receipt-only. Q review independently revalidates that record's
  candidate commit/tree, transition, accepted A9 prerequisite, and content
  digest before it may set `:evidence-eligible`.
  `ACTIVE-RECORD-REF` has exactly `:container :value-path :record-id
  :transition`. Container has exactly `:commit :tree :parents :path :mode
  :type :blob-oid :byte-count :raw-sha256`. Its path is literally
  `"contracts/workstream-active/sh07-b51-git-object-evidence-publication-implementation-v9.json"`,
  mode is `"100644"`, type is `"blob"`, and its Git IDs, count, and R of the
  exact bytes are recomputed with show, ls-tree, and cat-file at the immutable
  freeze commit. Value path is `[]` and record ID equals container
  `:raw-sha256`. The bytes must be canonical JSON valid under
  `contracts/workstream-governance.json` and decode to the complete active
  workstream record, not a projection.
  Transition has exactly `:workstream-id :from :to :candidate-commit
  :candidate-tree :architecture-report-sha256 :history-ordinal`. Its
  workstream ID is the literal implementation workstream ID above, from/to are
  `:draft`/`:frozen`, candidate is C/T, architecture digest is the accepted A9
  report digest, and history ordinal identifies the unique final history row.
  The decoded record must have state `:frozen`, the same candidate fields and
  architecture digest, an accepted and integrated A9 dependency, and exactly
  one matching transition row at that ordinal. The freeze commit and record
  may contain C/T but contain no Q/TQ, eligibility receipt, pre-discharge,
  runtime artifact, or descendant. Any missing/extra key, mutable working-file
  observation, alternate path, digest mismatch, duplicate transition, or
  current/later value rejects. E9 `:freeze-record-id` must equal this exact
  record ID.
  Source descriptors are the exact closed twelve-DESCRIPTOR vector later
  embedded in E9; source root is
  `H([:gravity/sh07-b51-source-descriptors-root-v1 SOURCE-DESCRIPTORS])`;
  masked root is the complete MASKED-ROOT map; and pre-evidence contract
  discharge is the complete PRE-EVIDENCE-CONTRACT-DISCHARGE map. Every nested
  C/T equals content C/T, and E9 must reproduce the descriptors, roots, and
  pre-discharge byte-for-byte rather than merely copying their root strings.
  This receipt never embeds RUNTIME-CONTRACT-DISCHARGE or any L/M/A/B artifact.
  It is published only by the pre-evidence review lifecycle after C freezes;
  it is neither an E9 payload nor an ancestor of E9. The lifecycle publishes
  it as the sole added blob in a separate immutable eligibility commit Q, tree
  TQ, with exact parents `[C]`; Q is a sibling-lineage child of C and is not
  E9. `ELIGIBILITY-COORDINATE` has exactly `:schema :commit :tree :parents :path
  :mode :type :blob-oid :byte-count :raw-sha256 :receipt-id` and equals
  schema `:gravity/sh07-b51-external-review-object-v1`, Q/TQ/[C], the literal
  receipt path, `"100644"`, `"blob"`, OQ, its exact byte
  count, R of its W bytes, and its recomputed H receipt ID. Q's diff from C is
  exactly that one added path and C has no entry there.
  L, M, A, and B each independently validate the same coordinate with exactly
  five Git object operations in order: show Q as Q/TQ/[C], name-status diff C
  to Q as the one added receipt path, `ls-tree -z Q -- PATH` as OQ, `cat-file
  blob OQ` as the exact W receipt bytes, and `ls-tree -z C -- PATH` as absent.
  Each operation row has exactly `:ordinal :operation :argv :stdin-byte-count
  :stdin-raw-sha256 :exit-code
  :stdout-byte-count :stdout-raw-sha256 :stderr-byte-count
  :stderr-raw-sha256 :parsed-result`; ordinals are 0-4, stdin and stderr are
  empty/R(empty), exit is 0, and stdout count/R are measured. Operations are exactly
  `[:show-tree :diff-tree-name-status-z :ls-tree-z-requested :cat-file-blob
  :ls-tree-z-requested]`; their argv/result grammars are the closed external
  Git grammars below with X/Y/P/O bound to Q/TQ/PATH/OQ, except the last row
  binds C/PATH and has no returned tree row.
  `ELIGIBILITY-CHECK` has exactly `:coordinate :observations
  :pre-discharge-observations :decode-count :receipt-id-check-count
  :pre-discharge-reconstruction-count :pre-discharge-git-operation-count
  :pre-discharge-document-descriptor-count
  :pre-discharge-source-descriptor-count :pre-discharge-masked-file-count
  :pre-discharge-owned-c-artifact-count
  :pre-discharge-a9-artifact-count :pre-discharge-baseline-boot5-count
  :pre-discharge-static-decode-count :content-match
  :source-descriptors-match :source-descriptors-root-match :masked-root-match
  :owned-c-root-match :pre-discharge-match`; observations are the five closed Q-coordinate rows;
  decode, ID-check, reconstruction, and baseline BOOT5 counts are 1; A9
  artifact count is 3; Git operation/document/source/masked/owned-C/static-decode
  counts are respectively 77/33/12/2/27/63; and all six match booleans are true. Each process
  strictly decodes the receipt, recomputes its receipt ID, reconstructs the
  complete PRE-EVIDENCE-CONTRACT-DISCHARGE from C/T Git objects under the
  algorithms and literal row plan below, recomputes every nested root and ID,
  and requires byte equality of `W` for the reconstructed and received maps.
  Source descriptors and MASKED-ROOT are independently reconstructed from C/T
  and must equal both the receipt fields and their counterparts inside the
  reconstructed pre-discharge. Content match is true only when every nested
  C/T equals the process C/T. No process reads the review path or any
  pre-discharge input from a worktree.

  `SOURCE-ONLY-PATHS` is exactly the following unsigned-UTF-8-sorted vector:

  ```clojure
  ["bootstrap/gravity/p15_s23/compiler.gravity"
   "bootstrap/gravity/p15_s23/emitter.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"]
  ```

  Source ordinals 0, 1, and 2 reuse document ordinals 8, 9, and 28. The exact
  OWNED-C-PATHS vector below includes both masked paths plus all governed I9
  implementation, test, and fixture bytes. The three A9-only
  paths are the architecture report, provisional companion, and lifecycle
  receipt. `PRE-C-PATHS` is the unsigned-UTF-8 sort of the
  distinct union of the 33 DOCUMENT-PATHS, nine SOURCE-ONLY-PATHS, 27
  OWNED-C-PATHS, and three A9-only paths: exactly 72 paths. The baseline-only
  path is BOOT5 at base `09c795f0c0854d2d8ab542c92b2116515e096406`.
  `PRE-DISCHARGE-OBSERVATIONS` is exactly 77 dense operation rows: show C;
  one `ls-tree -z C --` request for all 72 paths; 72 `cat-file blob` rows in
  PRE-C-PATHS order; show base; one base BOOT5 ls-tree row; and one base BOOT5
  cat-file row. Thus each participant performs 2 show, 2 ls-tree, and 73
  cat-file operations. The 33 document, 27 owned-C, and three A9 artifact blobs
  are strictly decoded; the nine source-only blobs remain raw descriptor
  inputs. This channel is separate from the five Q rows, classpath TCB checks,
  semantic source sessions, and G9 observation plan.
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
  :admission :integration :ownership :cleanup :self-id-omits :nonclaims
  :receipt-id`. Architecture has exactly
  `:content-commit :content-tree :content-parents :report :supporting-report`;
  report and supporting-report each have exactly
  `:path :mode :type :blob-oid :byte-count :raw-sha256`, with mode `"100644"`,
  type `"blob"`, and raw SHA R(the named report bytes). Their paths are the
  normative architecture and provisional companion paths, respectively. Let AC/TA be the architecture
  content commit/tree and AP its exact one-element ordered parent vector; every
  element is a 40-lowercase-hex Git ID. Architecture equals
  `{:content-commit AC :content-tree TA :content-parents AP :report REPORT
  :supporting-report SUPPORTING-REPORT}`.
  Acceptance
  is `{:state :accepted}`, admission is `{:state :integration-eligible}`, and
  integration has exactly `:state :integrated :commit :tree :parents
  :main-base :content-is-ancestor :report-preservation
  :supporting-report-preservation :reconciled`.
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
  Supporting-report-preservation has the identical schema and equation with
  SUPPORTING-REPORT substituted for REPORT.
  content-is-ancestor is the architecture-content/integration true
  ANCESTRY-RESULT AC/AI/true/0 and reconciled is true. Integration commit/tree
  are exactly AI/TAI. Artifact is
  `:gravity/sh07-b51-a9-lifecycle-receipt-v1`, version 1, review is REVIEW,
  self-id-omits is `[[:receipt-id]]`, nonclaims are NONCLAIMS, and receipt ID H-hashes the exact map
  with only itself omitted. Both report blobs at integration must equal their
  frozen bytes. Ownership has exactly `:reservation-id :reserved-paths
  :handoff-from :handoff-to :handoff-accepted`; reserved paths are the exact
  report and companion paths, handoff is from `:master-coordinator` to
  `:sh-core`, and accepted is true. Cleanup has exactly `:reservation-released
  :worktree-removed :branch-removed :finally-entered` and all values are true.
  These facts are inside the acyclic A9 lifecycle receipt; PRE need not discover
  or embed a later content-addressed terminal record.
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
  rechecked after admission; nonclaims are NONCLAIMS; self-id-omits is
  `[[:receipt-id]]`. Receipt ID H-hashes the map with only itself omitted. An exact
  P9 diff must contain only the two component paths and must reconstruct all
  five rows; any other changed byte rejects.
  Replacement rows 0-4 have the same exact path, load ordinal, form ordinal,
  owner, and RHS kind as `MASKED-ROOT`. Rows 0-3 take post-values from pin-map
  keys `:expected-m3-id`, `:expected-certificate-a-id`,
  `:expected-certificate-b-id`, and `:expected-semantic-root`, respectively.
  Row 4 is exactly `(quote CARRIER)` from
  `linked-carrier-v4-candidate.edn`. The four digest values must equal
  `p15-s23-sh07-b51-v4-candidate-pin-map`.

Three later external discharge objects complete the row plan without pointing
backward from Q or E9:

- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-discharge.edn`
  has exactly `:artifact :schema-version :phase :row-plan-id
  :pre-discharge-id :runtime-discharge-id :prior-ref :evidence :resolved-rows
  :resolved-root :authority :self-id-omits :discharge-id`. Phase is
  `:post-i9`; prior-ref authenticates the runtime discharge inside E9 as
  specified below. Its top-level runtime-discharge-id equals that selected
  discharge's ID, and pre-discharge-id equals the selected runtime discharge's
  prior-ref discharge ID; any unequal duplicate field rejects. `:evidence` is a nonrecursive PHASE-EVIDENCE map whose
  carriers are exactly `:foundation-lineage :compiler-lineage :test10-result
  :test13-result :provenance :ownership-result :governance-result
  :contract-test-results`. It is
  constructed only after the immutable I9 integration receipt and B/TB exist,
  resolves exactly `:post-i9` rows, and hashes with only its own discharge ID
  omitted.
- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-publication-discharge.edn`
  has exactly `:artifact :schema-version :phase :row-plan-id :prior-ref
  :evidence :resolved-rows :resolved-root :authority :self-id-omits
  :discharge-id`. Phase is `:post-g9`; prior-ref authenticates the complete
  post-I9 discharge. `:evidence` is a nonrecursive PHASE-EVIDENCE map whose
  carriers are exactly `:publication :tuple :g9-lifecycle-receipt :provenance
  :ownership-result :governance-result :contract-test-results`. It is
  constructed only after G/TG and
  the immutable G9 lifecycle receipt, binds G/TG/[B], the sole G9 tuple
  descriptor and exact B/G and E/G ancestry, and resolves exactly `:post-g9`
  rows.
- `docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-final-discharge.edn`
  has exactly `:artifact :schema-version :phase :row-plan-id :prior-ref
  :evidence :resolved-rows :resolved-root :phase-roots
  :all-obligations-resolved :authority :self-id-omits :discharge-id`. Phase is
  `:post-p9`; prior-ref authenticates the complete post-G9 discharge.
  `:evidence` is a nonrecursive PHASE-EVIDENCE map whose carriers are exactly
  `:pin-publication :replacements :masked-root :p9-lifecycle-receipt
  :provenance :ownership-result :governance-results :final-lifecycle
  :contract-test-results`. It is
  constructed only after P/TP and the immutable P9 lifecycle receipt, binds
  P/TP/[G], the exact five replacements, and resolves exactly `:post-p9` rows.
  `:phase-roots` is the exact ordered vector
  `[[:pre-evidence PRE-RESOLVED-ROOT] [:runtime RUNTIME-RESOLVED-ROOT]
  [:post-i9 POST-I9-RESOLVED-ROOT] [:post-g9 POST-G9-RESOLVED-ROOT]
  [:post-p9 POST-P9-RESOLVED-ROOT]]`; every value is recomputed by following
  prior-ref leftward, strictly decoding the five phase objects, and rehashing
  their dense row vectors. The post-P9 value equals this object's
  `:resolved-root`.

Each external discharge uses artifact/schema version respectively
`:gravity/sh07-b51-i9-integration-discharge-v1`,
`:gravity/sh07-b51-g9-publication-discharge-v1`, and
`:gravity/sh07-b51-p9-final-discharge-v1`, version 1. `self-id-omits` is always
`[[:discharge-id]]`; discharge ID is H under the matching artifact domain of
the whole map without itself. `PHASE-EVIDENCE` has exactly `:schema :phase
:row-plan-id :carriers`; schema is `:gravity/sh07-b51-phase-evidence-v1`, phase
and row-plan ID equal the enclosing discharge, and carriers are the exact
phase-specific map above. PHASE-EVIDENCE contains no enclosing discharge,
enclosing discharge ID, or later object. `PRIOR-REF` has exactly `:phase
:container :value-path :discharge-id :lifecycle-ref
:forbidden-path-observations`; container has exactly `:commit :tree
:parents :path :mode :type :blob-oid :byte-count :raw-sha256`, uses mode
`"100644"` and type `"blob"`, and is independently verified by show, ls-tree,
and cat-file. Value-path selects the complete earlier discharge from the
strictly decoded container: runtime selects the pre-evidence discharge at
`[:pre-evidence-contract-discharge]` inside Q; post-I9 selects the runtime
discharge at `[:runtime-contract-discharge]` inside the E9 identity receipt;
post-G9 selects the prior I9 discharge at `[]`; and post-P9 selects the prior
G9 discharge at `[]`. Lifecycle ref is an immutable container/value/receipt-ID
reference to the selected phase's accepted or evidence-immutable lifecycle
record. Forbidden-path observations are a dense exact ls-tree result vector for
the current and every later discharge/lifecycle path, all absent from the prior
container. The later phase's external acceptance record, not the discharge
preimage, proves the lifecycle dependency edge and commit ancestry from the
prior integrated stage to the later candidate. Final validation follows those
records and requires the exact stage order, true ancestry, and no skipped or
duplicate lifecycle state. Recomputed W, R, blob OID, byte count, phase,
row-plan ID, root, discharge ID, lifecycle receipt ID, and absence observations
must all agree. No enclosing commit/tree enters its own discharge preimage.

The preceding PRIOR-REF lifecycle and absence fields are closed as follows.
`LIFECYCLE-REF` has exactly `:stage :path-ref :container :value-path
:required-result :receipt-id`. Its container has the same exact descriptor
keyset and Git-object validation as PRIOR-REF container; value path is `[]`,
and receipt ID is recomputed from the complete decoded receipt under that
receipt's named H equation. The exact per-phase values are:

```clojure
{:runtime {:stage :pre-evidence :path-ref [:i9-suffix 0]
           :required-result :evidence-eligible}
 :post-i9 {:stage :runtime :path-ref [:i9-suffix 1]
           :required-result :integrated}
 :post-g9 {:stage :post-i9 :path-ref [:i9-suffix 1]
           :required-result :integrated}
 :post-p9 {:stage :post-g9 :path-ref [:g9 1]
           :required-result :accepted}}
```

Path ref resolves by exact index into E9-PATHS, the exact I9 suffix,
G9-LIFECYCLE-PATHS, or P9-LIFECYCLE-PATHS. The resolved receipt must have the
required lifecycle result/state, candidate and predecessor coordinates matching
the selected discharge, a true required ancestry result, and a unique accepted
transition. An alternate index, receipt, result, candidate, or ancestry edge
rejects. These external receipt references do not enter their own receipt
preimages.

`FORBIDDEN-PATH-REFS` is literally:

```clojure
{:runtime
 [[:e9 0] [:e9 1] [:e9 2] [:e9 3] [:e9 4] [:e9 5] [:e9 6]
  [:i9-suffix 1] [:i9-suffix 2] [:i9-suffix 3]
  [:g9 0] [:g9 1] [:g9 2] [:g9 3] [:g9 4]
  [:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]]
 :post-i9
 [[:i9-suffix 1] [:i9-suffix 2] [:i9-suffix 3]
  [:g9 0] [:g9 1] [:g9 2] [:g9 3] [:g9 4]
  [:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]]
 :post-g9
 [[:g9 0] [:g9 1] [:g9 2] [:g9 3] [:g9 4]
  [:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]]
 :post-p9
 [[:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]]}
```

The exact counts are respectively 21, 14, 11, and 6. A path ref resolves
through the literal vectors below. A governance path spec resolves after its
final record bytes exist to directory + `"/"` + workstream ID + `"-"` +
lowercase SHA-256 of those bytes + `".json"`; final validation then queries
that concrete path in the earlier immutable commit. No path is guessed.

Each forbidden observation has exactly `:ordinal :path-ref :resolved-path
:argv :exit-code :stdout-byte-count :stdout-raw-sha256 :parsed-result`.
Ordinal is its zero-based position in the phase vector; path-ref and resolved
path are exact; argv is
`["/usr/bin/git" "-C" REPO "ls-tree" "-z" PRIOR-COMMIT "--" RESOLVED-PATH]`;
exit code is 0, stdout byte count is 0, stdout raw SHA is R(empty), and parsed
result is `[]`. The vector's path-ref projection must equal exactly
FORBIDDEN-PATH-REFS at PRIOR-REF phase. A missing, present, additional,
reordered, unresolved, or duplicate path rejects.

Final validation follows all lifecycle refs and requires the exact stage order,
true ancestry, no skipped or duplicate state, and the literal absence vectors.
Every referenced receipt's W, R, blob OID, byte count, phase, candidate,
predecessor, result, and receipt ID must recompute. This exact closure, not the
earlier prose summary, governs `:lifecycle-ref` and
`:forbidden-path-observations`.

`FOUNDATION-LINEAGE` has exactly `:content-is-ancestor
:evidence-is-ancestor-of-base :replay-id :invalidation-id`. The ancestry
values are the C/B true and E/B false ANCESTRY-RESULT values; replay and
invalidation IDs equal the authenticated runtime discharge. `COMPILER-LINEAGE`
has exactly `:source-compiler-id :content-commit :integration-commit
:preserved-paths-root :diagnostics-preserved :artifact-schemas-preserved` and
is derived from C, B, the SELF-HOSTING-RUNTIME compiler identity, and the I9
preservation receipt; both booleans are true.

`TEST10-POST-I9-RESULT` has exactly `:schema :content :integration
:runtime-discharge-id :i9-integration-receipt-id :oracles :preservation-root
:replay-id :status :self-id-omits :result-id`. Schema is
`:gravity/sh07-b51-test10-post-i9-result-v1`; content is C/T, integration is
B/TB/BASE-PARENTS, and oracles are exactly two maps for `:main` then `:i9`
with keys `:ordinal :role :oracle-id :applicability :basis :observation-id`.
Applicability is `:preservation-replay`; bases are the frozen main baseline and
C/T candidate. For oracle ordinal N, observation ID is
`H([:gravity/sh07-b51-test10-preservation-observation-id-v1
{:ordinal N :role ROLE :basis BASIS :preserved-blobs PRESERVED-BLOBS}])`, where
PRESERVED-BLOBS is the exact I9 integration receipt vector; no free observation
ID is admitted. Status is
`:passed`; self-id-omits is `[[:result-id]]`; result ID H-hashes the complete
map without itself under its schema domain.

`TEST13-POST-I9-RESULT` has exactly `:schema :content :integration
:runtime-discharge-id :i9-integration-receipt-id :stage
:expected-preserved-c-paths :preserved-blobs :observed-preserved-c-paths
:observed-deltas :unexpected-deltas :identity-receipt-object
:generated-artifact-value-ids :tcb-delta :diagnostics-preserved
:artifact-schemas-preserved :stage-advanced :self-id-omits :result-id`. Schema
is `:gravity/sh07-b51-test13-post-i9-result-v1`; content is C/T; integration is
B/TB/BASE-PARENTS; expected and observed preserved paths are OWNED-C-PATHS;
preserved blobs are the exact I9 receipt rows; observed and unexpected deltas
are empty; the identity-receipt object is the exact E descriptor; the six
generated IDs and TCB delta equal the runtime discharge; both preservation
booleans are true and stage-advanced is false. Self-id-omits is
`[[:result-id]]`; result ID H-hashes the map without itself under its schema
domain.

`PHASE-PROVENANCE` has exactly `:phase :input-ids :output-ids :receipt-refs
:provenance-root`; all vectors are ordered and nonempty and root is
`H([:gravity/sh07-b51-phase-provenance-root-v1 MAP-WITHOUT-ROOT])`.
`OWNERSHIP-RESULT` has exactly `:stage :plan-id
:owner :path-specs :paths :reservation :handoff :changed-paths :equal-to-plan`
and binds the exact V9-OWNERSHIP-PLAN allocation; plan ID equals the plan's
recomputed `:plan-id`. Path specs equal its static
allocation; paths are the resolved string vector. Reservation has exactly
`:receipt-ref :state :path-spec-root :actual-path-root`, state is `:held`, and
path-spec root is `OWNERSHIP-PATH-ROOT(:path-specs, PATH-SPECS)` while actual
path root is `OWNERSHIP-PATH-ROOT(:actual-paths, PATHS)`. Handoff has exactly
`:receipt-ref :from :to :accepted`; receipt refs use the immutable container
schema of PRIOR-REF, owners equal the allocation and semantic reviewer as
applicable, and accepted is true. Changed paths are exact strings, every path
matches one spec exactly once, and equal-to-plan is true. `GOVERNANCE-RESULT` has exactly `:stage
:candidate :review-receipt :acceptance :admission :integration :cleanup` and
binds actual, already-immutable review/admission/integration evidence.
Candidate has exactly `:commit :tree`; review-receipt is an immutable
container/value reference with recomputed receipt ID; acceptance and admission
are exactly `{:state :accepted}` and `{:state :integration-eligible}`.
Integration has exactly `:state :commit :tree :parents :content-is-ancestor
:preservation-paths :preservation-descriptors :preservation-root`, state is
`:integrated`, ancestry is the matching true ANCESTRY-RESULT, preservation paths
are the exact resolved phase-owned path vector, descriptors are dense maps with
exactly `:path :before :after :equal` whose before/after values have exactly
`:commit :tree :mode :type :blob-oid :byte-count :raw-sha256`, every equality is
true, and preservation root is
`H([:gravity/sh07-b51-governance-preservation-root-v1
{:paths PRESERVATION-PATHS :descriptors PRESERVATION-DESCRIPTORS}])`.
Cleanup has exactly `:reservation-released :worktree-removed :branch-removed
:finally-entered`; an earlier phase records only facts actually attained, and
the final P9 result requires all four values true.
`A9-RESULT` is constructed exactly from A9-LIFECYCLE and has the
GOVERNANCE-RESULT keyset: stage `:a9`; candidate equal the accepted architecture
content commit/tree; review, acceptance, admission, and integration copied and
revalidated from its lifecycle receipt; preservation paths exactly the report
and companion paths; preservation descriptors exactly the two lifecycle
preservation values; and cleanup exactly `{:reservation-released true
:worktree-removed true :branch-removed true :finally-entered true}` from that
receipt. Ownership is revalidated from the receipt's exact reservation and
accepted handoff fields; a historical A9 receipt without them cannot satisfy
V9.
`GOVERNANCE-RESULTS` is exactly `[A9-RESULT I9-RESULT G9-RESULT P9-RESULT]`,
ordered by stage, with no duplicate. `FINAL-LIFECYCLE` has exactly
`:governance-results :all-required-integrated :final-cleanup`; it contains that
same vector and both booleans are true. No
external discharge is embedded in an earlier object or in the receipt it
references.

Every discharge `AUTHORITY` has exactly `:phase :attained :deferred :grants
:aggregate-sh07 :nonclaims`. Let MILESTONES be exactly
`[:a9-architecture-integrated :c-content-frozen :q-evidence-eligible
:fresh-runtime-complete :e9-evidence-immutable :i9-integrated :g9-integrated
:p9-integrated :final-cleanup-complete]`. For phases in exact order
`[:pre-evidence :runtime :post-i9 :post-g9 :post-p9]`, attained is respectively
the MILESTONES prefix of length `[1 4 6 7 9]` and deferred is the remaining
suffix. Grants is exactly `[:contract-discharge-only]`; aggregate SH-07 is
PROGRESS; nonclaims are NONCLAIMS. A phase may attain a milestone only when
its carriers bind the immutable receipt/result proving it; otherwise the
phase rejects rather than moving a deferred state into attained.

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
`docs/workstream-ledger-v2.md`;
`contracts/workstream-ledger.json`;
`contracts/verification-catalog.edn`;
`bootstrap/gravity/src/gravity/checked_core.gravity`; and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.
The lifecycle record must list every one of those inputs; aliases such as D1
or BOOT7 mean their owning inventory documents, not this report.

The pre-evidence discharge contains one static row plan for each entry below;
after Q is immutable, E9's runtime discharge resolves every PRE/runtime binding
to the exact post-Q artifact value; 14 bindings remain explicitly deferred to
post-I9/G9/P9. Every row has the exact path, applicable
section names, disposition, bound artifact/receipt/test IDs, and rejection diagnostic. An
unlisted row, blank binding, invented section, or disposition outside
`:applies`, `:preserved`, and `:not-applicable` rejects as
`SH07-B51-CONTRACT-DISCHARGE`.

| Exact governing path(s) | Applicable sections and disposition | Mandatory V9 binding | Rejection |
| --- | --- | --- | --- |
| `AGENTS.md` | Source Language Boundary, Start Here, Source Of Truth, Implementation Principles, Agent Guardrails: Preventing Over-Engineering, Documentation Work, Validation, Working Discipline, Workstream Lifecycle And Integration; `:applies` | normative-document priority, Clojure-only bootstrap boundary, ownership, evidence, validation, and no-overclaim rules | `SH07-B51-CONTRACT-DISCHARGE` |
| `docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md`; `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`; `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`; `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`; `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`; `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`; `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md` | Requirements, Outputs and Artifacts, Conformance Criteria; `:applies` | C/T/B/TB lineage, canonical-pipeline pass/fact/provenance records, safe-semantics nonclaims, evidence-kind labels, replay and invalidation IDs | `SH07-B51-CONTRACT-DISCHARGE` |
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
| `docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md`; `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`; `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md` | Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | bound already-loaded launcher L plus fresh M/A/B topology, stage comparison, TCB delta, immutable provenance receipts, cleanup and replay | `SH07-B51-CHECKER-PROCESS` |
| `docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md` | Oracles, Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | A/B/main/I9 oracle identity/applicability, profile/target/backend/runtime/numeric mode, compared observables, diagnostics, permitted divergence and reproducers | `SH07-B51-NORMALIZE-DIVERGENCE` |
| `docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md` | Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | assumptions, trusted basis, checker/version/input/claim IDs, invalidation and counterexamples; E9 labels these validation receipts, not formal proof | `SH07-B51-PROOF-SCOPE` |
| `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md` | Stages, Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:preserved` | exact stage/trusted inputs/generated artifacts/source/compiler/lock/target/output identities, expected deltas and TCB delta; no stage advancement | `SH07-B51-PROOF-SCOPE` |
| `bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn` | whole document with no Markdown headings; `:applies` only within its declared B47/B15 scope | active status, fresh authoritative process, nonauthoritative iteration cache, source/checked-core rechecks, verifier/catalog IDs, immutable receipts, bounds/cache key and cleanup | `SH07-B51-PROOF-SCOPE` |
| `docs/self-hosting-slice-backlog.md`; `docs/self-hosting-slice-ownership.edn` | Countable Backlog, Dependency Waves and Parallel Ownership, Progress Accounting, Outputs and Artifacts, Conformance Criteria for the Markdown document; whole ownership EDN document with no Markdown headings; `:applies` | SH-07 remains partial; exact I9/E9/G9/P9 owner/path/reservation/handoff; core/evaluation/arity/mutation/recursion/exception/pattern exit gate remains open | `SH07-B51-OWNER` |
| `contracts/verification-catalog.edn`; `bootstrap/gravity/src/gravity/checked_core.gravity`; `docs/workstream-governance.md`; `contracts/workstream-governance.json`; `docs/workstream-ledger-v2.md`; `contracts/workstream-ledger.json` | whole catalog EDN, checked-core Gravity, governance JSON, and ledger manifest documents with no Markdown headings; `## Lifecycle` and `## Admission` for governance Markdown; the ledger-v2 manifest-linkage requirements for ledger Markdown; `:applies` | exact C object identities, catalog schema/version/authority, literal `sh07-checked-core` module and applicable route records, checked-core revision, lifecycle/ledger state transitions, review/admission receipts and final cleanup | `SH07-B51-VERIFY-CATALOG` |

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
wrapper; a caller cannot install, replace, or reuse them. Its exact public
arity is two, `(C,T)`, and both arguments must be the 40-lowercase-hex commit
and tree IDs already bound by the lifecycle. It returns exactly
`:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
:module-transactions :source-read-receipt`; `:bundle-core` is the authentic
six-key 00945 inner value and `:execution-bundle-core-id` is its authenticated
sibling ID. `:module-transactions` is the complete seven-row R/Q/T/D/Z/RC
vector constructed from the same prefetched module bytes before the private
session is finalized. No path, reader, cache, session, or product override
argument exists.

```clojure
RECEIPTED-BUNDLE-CORE-PRODUCTS =
  gravity.bootstrap/p15-s23-sh07-b51-build-receipted-bundle-core-products(C,T)
```

One session prefetches the exact twelve C blobs above, in order, and strictly
decodes each UTF-8 vector once. Products consume exactly 39 events: L1
contract 0, L2 contract 1, checked-core revision 2, module validation 3-9,
census 10-16, link authority 17-23, compiler emitter 24, source-authority
module reads 25-31, and compiler-artifact-binding emitter reads 32-38. The
seven final events are distinct authorized consumers of the one prefetched
emitter byte vector, not repeated acquisitions. The session finalizes once and rejects a missing,
duplicate, reordered, late, or unconsumed event.

The module transaction builder consumes the already authenticated decoded
values from module-validation events 3-9 in the same dynamic session; it does
not acquire or decode a source again and therefore adds no source event. Each
transaction's resolution artifact and Q/RC descendants bind that event's
descriptor raw SHA and the same cached bytes. Returning the transactions after
session finalization is permitted because they contain semantic values and
identities only, never raw bytes, readers, host/absolute/checked-out paths,
handles, or the private cache. Their identity-bound project-relative descriptor
and Q provenance path fields remain exactly as specified below.

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
The clean C source worktree is never the evidence publication worktree. In
`:new`, L also validates a distinct registered publication worktree at exact
C/T, clean and with the final evidence path absent, before allowing writes.
In `:existing`, no publication worktree or mutable evidence directory is read:
L alone reloads the seven blobs from an exact supplied E/TE Git-object
coordinate whose commit has parents `[C]` and exact seven-addition delta. E/TE
and publication mode are launcher-side storage/replay authority only and never
enter M, A, B, their requests or responses, a source session, a work counter,
or a semantic identity preimage.

Each A, B, and M session records exactly 12 acquisitions, 12 strict decodes, 39
events, and 1 finalize. Every authoritative run uses work vector `[3 3 3 2 2]`,
total 13. Sentinels are nonauthoritative. The authenticated edge census remains
exactly 1480.

## Independent checker processes

L is the narrow already-loaded host launcher in
`gravity.tools.sh07-b51-git-evidence`. L may locate `/usr/bin/git`, validate the
clean C worktree, read immutable Git objects, materialize the four-entry
C-derived classpath, and construct exactly one M `ProcessBuilder`. L may not
load the M, A, B, bootstrap, checker, or materializer namespaces; its only
additional project closure is the shared V9 codec plus its exact canonical and
digest leaves defined below. L validates those three C blobs and its own
launcher blob before using the codec. It may not decode a
Gravity source blob, compute a checker product, or author a semantic payload.
It is explicitly authorized to perform only the nonsemantic closure work needed
after M exits: reconstruct PRE from immutable C objects, validate M's closed
response and six staged payload descriptors, run the literal contract-discharge
predicates embedded in its authenticated launcher blob, construct
CONTRACT-TEST-RESULTS, construct RUNTIME-CONTRACT-DISCHARGE, and assemble the
closed final process receipt. This authority cannot change, repair, or infer a
checker product; every predicate consumes already-authenticated carrier values
and fails closed. Its source
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
its entry preflight. Before any dynamic project load it validates actual
process, argv, environment, working directory, the literal CP entries, C/T
syntax, and the role-specific `PRE-MANIFEST-TCB` described below. Only then may
it dynamically load the shared codec and the codec's already-validated
canonical and digest leaves from the C-derived CP. The codec validates the
complete canonical classpath manifest root; only afterward may M lazily require
the materializer. Any project namespace outside the exact pre-manifest TCB
before the manifest-root preflight rejects. M's transitive loaded and
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
literal-CP/pre-manifest-TCB then shared-codec/canonical-manifest preflight as
M. After strict
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
construct its complete `:m0s`, `:m1`, `:m2s`, `:bundle-core`,
`:source-forms`, and `:module-transactions` envelope, finalizes that session,
and only then invokes its one
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
nonauthoritative. It cannot satisfy authoritative reload, runtime admission,
lifecycle join, G9, or publication. Authoritative reload always launches fresh
A, B, and M with the same semantic inputs as initial publication. Each
authoritative run has exactly two builders,
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

`PRE-MANIFEST-TCB` has exactly `:entrypoint :codec-tcb`, not a permission to
load an arbitrary helper. Entrypoint is the M coordinator-child or A/B
checker-child row. `CODEC-TCB` is the exact three-row vector, in load order,
for `bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj`,
`bootstrap/clojure/src/gravity/digest.clj`, and
`bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj`. Each row has
exactly `:ordinal :path :namespace
:mode :blob-oid :byte-count :raw-sha256 :materialized-raw-sha256
:requires-project-namespaces`. Mode is `"100644"`; OID/count/raw SHA are read
from exact C/T Git objects; materialized raw SHA must equal the object raw SHA.
The entrypoint, canonical, and digest rows have an empty project-require vector.
The codec row has exactly
`[gravity.c2-artifact-identity.canonical gravity.digest]`, in that order.

The already-loaded entrypoint performs this check using only Clojure core/EDN,
Java NIO, `MessageDigest`, and the already-validated absolute Git executable.
It reads the four C blobs represented by PRE-MANIFEST-TCB with Git, reads the matching materialized files
without following symlinks, strictly decodes UTF-8, checks the first namespace
form and exact project-require vector, and computes raw SHA-256 directly with
`MessageDigest`. Only after all four rows agree may it dynamically require the
codec. Loading the codec necessarily loads only the two rows already admitted.
The codec then recomputes H/W/R parity and the full classpath manifest. L's
closed already-loaded source TCB is the analogous ordered four-row vector of
launcher, codec, canonical, and digest. A hidden transitive namespace, loading
before row validation, alternate source blob, or disagreement between the
pre-manifest rows and the full manifest rejects with
`SH07-B51-TRUSTED-TCB`. `CODEC-TCB` itself has exactly `:schema :load-order
:members`, schema `:gravity/sh07-b51-codec-tcb-v1`, load order
`[gravity.c2-artifact-identity.canonical gravity.digest
gravity.tools.sh07-b51-v9-codec]`, and members equal its three rows.

L's closed publication input has exactly `:mode :source-worktree
:publication-worktree :existing-evidence :staging-root
:eligibility-coordinate`. A worktree coordinate has exactly `:canonical-path
:git-common-directory :file-key :commit :tree :index-clean :untracked-count
:evidence-paths-absent`; source equals its absolute path/common-directory/key,
C/T, true, 0, and true. Staging root is a fresh private mode-0700
directory outside every registered worktree and is receipt-only. In `:new`,
publication-worktree is a distinct registered worktree at exact C/T, clean at
entry, sharing the same Git common directory, with the fixed seven evidence
paths absent; existing-evidence is nil. In `:existing`, publication-worktree
is nil and existing-evidence is exactly `{:commit E :tree TE :parents [C]}`;
the seven evidence paths are loaded from E objects only. Both absolute roots
and their file keys are receipt-only. Equal source/publication paths, a target
outside the shared object database, a dirty entry state, an E ref or branch
lookup, or a worktree read of existing evidence rejects.

Exact M argv, constructed by L without a shell, is:

```clojure
[JAVA "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
 "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
 "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
 "-Duser.home=/nonexistent" "-cp" CP "clojure.main" "-m"
 "gravity.tools.sh07-b51-attempt32-coordinator-child"
 "--snapshot-commit" C "--snapshot-tree" T
 "--completed-at" "1970-01-01T00:00:00Z"
 "--evidence-relative-root" EVIDENCE-ROOT
 "--staging-root" STAGING-ROOT
 "--eligibility-review-commit" Q
 "--eligibility-review-tree" TQ
 "--eligibility-receipt-path" ELIGIBILITY-PATH
 "--eligibility-receipt-blob" OQ
 "--eligibility-receipt-byte-count" NQ
 "--eligibility-receipt-raw-sha256" RQ
 "--eligibility-receipt-id" HQ]
```

`EVIDENCE-ROOT` is the one validated relative E9 output root; the eligibility
values are the closed immutable coordinate above. M receives no publication
mode, E, or TE. L alone validates any supplied E/TE object coordinate and does
so outside the semantic M/A/B execution. L applies the exact environment map below and the
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

CLI/EDN conversion is one closed bijection for checker roles:
`"producer-census"` maps only to `:producer-census`, and
`"independent-verifier-census"` only to `:independent-verifier-census`.
Strings never appear in EDN role fields, keywords never appear in argv, and
every other spelling or coercion rejects. Publication mode is parsed and used
only inside L and never crosses the M/A/B semantic boundary.

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
`:schema :launcher-source-closure :launcher-process :git :java :pre-manifest-tcb
:eligibility-check :publication-input
:classpath-precheck :m-argv :m-argv-id :environment :working-directory
:m-process-handle-id :m-process :m-process-join :m-response-transport
:m-response-byte-count :m-response-raw-sha256 :m-response-preimage-id
:classpath-postcheck :publication :cleanup :contract-validation :receipt-id`. Schema is
`:gravity/sh07-b51-launcher-receipt-v1`; launcher source closure is the closed
SOURCE-CLOSURE containing only the launcher and the three-member codec TCB;
pre-manifest-tcb and eligibility-check are the exact closed maps above; publication
input is L's closed mode/source/publication/existing/staging input; launcher process
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
domain-bound H value. Publication has exactly `:mode :logical-root
:source-worktree :publication-worktree :existing-evidence :staging-root
:reservation-created :reservation-file-key :files-created
:identity-receipt-created-last :final-write-count :foreign-entry :outcome`.
Mode is `:new` or `:existing`; logical root is the fixed safe E9 path prefix;
the four coordinate fields equal L's closed publication input. In `:new`,
reservation-created is true, reservation file key is the nonempty receipt-only
key of the newly created final directory, files-created is the exact seven
artifact paths, identity-receipt-created-last is true, final-write-count is 7,
foreign-entry is false, and outcome is `:staged-new`. In `:existing`, those
values are false/nil/empty/false/0/false/`:verified-existing`; no final
filesystem path is read or written and every comparison uses the exact E
objects. The receipt-only normalizer applies the exact
`N-PUBLICATION-INPUT` and `N-PUBLICATION` projections below; no launcher-side
mode, E/TE coordinate, write count, or outcome enters semantic identity. Cleanup is
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
:status :content :payload-descriptors :staging-receipt
:coordinator-receipt :source-read-receipt :counters :transport`. Schema is
`:gravity/sh07-b51-m-response-v1`, status is `:accepted`, content is exactly
`{:commit C :tree T}`, payload descriptors are the six non-receipt E9 rows,
and `:staging-receipt` binds the private
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

M deliberately does not produce CONTRACT-TEST-RESULTS or a runtime discharge.
After M exits successfully, L's authenticated launcher implementation invokes
its closed data-only contract validator over PRE plus the six strictly decoded
M payload values and L/M/A/B receipts. No project test namespace is loaded in
the authoritative run. The test namespace named by TEST-ID-MAP is a repository
harness that must exercise the same literal PREDICATE-SPECS against fixtures;
its authenticated C bytes are provenance and implementation-review input, not
an executable-authority dependency. L's validator output is the complete dense
CONTRACT-TEST-RESULTS vector and its construction is recorded in
LAUNCHER-RECEIPT as `:contract-validation`.

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

`LAUNCHER-RECEIPT[:contract-validation]` has exactly `:validator-source
:predicate-spec-id :pre-discharge-id :runtime-input-ids :result-count
:result-root :status`. Validator source is L's C object descriptor;
predicate-spec ID is
`H([:gravity/sh07-b51-contract-predicate-spec-id-v1 PREDICATE-SPECS])`;
runtime input IDs are the six payload value IDs followed by M/A/B receipt IDs
and `LAUNCHER-VALIDATION-INPUT-ID`. `LAUNCHER-VALIDATION-INPUT` is
`N-LAUNCHER-VALIDATION-INPUT`, the exact normalized LAUNCHER-RECEIPT projection
omitting only `:contract-validation` and `:receipt-id`; its ID is
`H([:gravity/sh07-b51-launcher-validation-input-v1
LAUNCHER-VALIDATION-INPUT])`. It is fixed before validation begins and contains
no test result or discharge. Result count is 19; result root is
`H([:gravity/sh07-b51-contract-test-result-root-v1 CONTRACT-TEST-RESULTS])`;
and status is `:passed`. This field is part of the normalized launcher receipt
and therefore of E9 identity. Missing, extra, reordered, or unexecuted
predicates reject before RUNTIME-CONTRACT-DISCHARGE can be constructed.

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
choice of serializer. The later full identity manifest is the exact C/T Git
object closure containing
`bootstrap/clojure/src/gravity/c2_artifact_identity.clj`,
`bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj`, and
`bootstrap/clojure/src/gravity/digest.clj`. Their mode, blob OID, byte count,
and raw SHA-256 are recorded in every A/B/M TCB and verified before and after
each process. `gravity.c2-artifact-identity` is not a member of
`PRE-MANIFEST-TCB` or `CODEC-TCB`; only the canonical leaf and digest are in the
three-member pre-manifest codec closure, and the facade is admitted later by
the full identity manifest.

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

The table below records the core V9 identity families. It is not an exhaustive
allowlist by itself; the exhaustive H-DOMAIN-REGISTRY immediately following the
table is the allowlist, and every registry member's sole payload grammar is the
right-hand side of its named H equation in this report.

| Identity | Domain | Payload |
| --- | --- | --- |
| classpath manifest root | `:gravity/sh07-b51-classpath-manifest-root-v1` | exact ordered classpath manifest rows |
| C2 project-root ID | `:gravity/sh07-b51-c2-project-root-id-v1` | exact C/T-bound explicit C2 project scope |
| loaded namespace root | `:gravity/sh07-b51-loaded-namespace-root-v1` | closed loaded-namespace map with `:root` omitted |
| host surface root | `:gravity/sh07-b51-host-surface-root-v1` | closed host-surface map with `:root` omitted |
| source closure root | `:gravity/sh07-b51-source-closure-root-v1` | closed SOURCE-CLOSURE map with `:root` omitted |
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
| contract document event root | `:gravity/sh07-b51-contract-document-event-root-v1` | exact dense 33-row contract-read event vector |
| contract row-plan ID | `:gravity/sh07-b51-contract-row-plan-id-v1` | exact literal 19-row plan vector |
| pre-evidence discharge ID | `:gravity/sh07-b51-pre-evidence-contract-discharge-v1` | exact C/T-only pre-discharge with only `:discharge-id` omitted |
| runtime discharge ID | `:gravity/sh07-b51-runtime-contract-discharge-v1` | exact post-Q runtime discharge with only `:discharge-id` omitted |
| post-I9 row root | `:gravity/sh07-b51-post-i9-resolved-row-root-v1` | exact dense post-I9 resolved row vector |
| post-G9 row root | `:gravity/sh07-b51-post-g9-resolved-row-root-v1` | exact dense post-G9 resolved row vector |
| post-P9 row root | `:gravity/sh07-b51-post-p9-resolved-row-root-v1` | exact dense post-P9 resolved row vector |
| post-I9 discharge ID | `:gravity/sh07-b51-i9-integration-discharge-v1` | exact post-I9 discharge with only `:discharge-id` omitted |
| post-G9 discharge ID | `:gravity/sh07-b51-g9-publication-discharge-v1` | exact post-G9 discharge with only `:discharge-id` omitted |
| post-P9 discharge ID | `:gravity/sh07-b51-p9-final-discharge-v1` | exact post-P9 discharge with only `:discharge-id` omitted |
| evidence-eligibility receipt ID | `:gravity/sh07-b51-evidence-eligibility-receipt-v2` | exact external receipt with only `:receipt-id` omitted |
| I9 integration receipt ID | `:gravity/sh07-b51-i9-integration-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| G9 lifecycle receipt ID | `:gravity/sh07-b51-g9-lifecycle-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| A9 lifecycle receipt ID | `:gravity/sh07-b51-a9-lifecycle-receipt-v1` | exact external receipt with only `:receipt-id` omitted |
| P9 publication receipt ID | `:gravity/sh07-b51-p9-pin-publication-receipt-v1` | exact external receipt with only `:receipt-id` omitted |

`V9-TAG-REGISTRY` is the unsigned-UTF-8-sorted, duplicate-free vector below. It
contains every V9 H domain plus schema, artifact, codec, and policy tags; mere
membership does not make a tag an H domain.

```clojure
[:gravity/sh07-b51-a9-lifecycle-receipt-v1
 :gravity/sh07-b51-authentic-lowering-witness-v1
 :gravity/sh07-b51-boot2-compiler-id-v1
 :gravity/sh07-b51-boot2-source-id-v1
 :gravity/sh07-b51-boot2-witness-v1
 :gravity/sh07-b51-boot3-witness-v1
 :gravity/sh07-b51-boot4-witness-v1
 :gravity/sh07-b51-boot5-compatibility-row-v1
 :gravity/sh07-b51-bounded-operation-id-v1
 :gravity/sh07-b51-bundle-core-public-roots-id-v1
 :gravity/sh07-b51-c2-project-root-id-v1
 :gravity/sh07-b51-c2-witness-v1
 :gravity/sh07-b51-c6-witness-v1
 :gravity/sh07-b51-checker-child-request-v4
 :gravity/sh07-b51-checker-child-response-v1
 :gravity/sh07-b51-checker-child-tcb-manifest-v1
 :gravity/sh07-b51-checker-process-identity-receipt-id-v1
 :gravity/sh07-b51-checker-process-identity-receipt-v1
 :gravity/sh07-b51-checker-production-v1
 :gravity/sh07-b51-checker-request-id-v1
 :gravity/sh07-b51-checker-result-artifact-id-v1
 :gravity/sh07-b51-checker-result-output-id-v1
 :gravity/sh07-b51-checker-sentinel-v1
 :gravity/sh07-b51-child-process-receipt-v1
 :gravity/sh07-b51-classpath-manifest-root-v1
 :gravity/sh07-b51-codec-tcb-v1
 :gravity/sh07-b51-contract-binding-v2
 :gravity/sh07-b51-contract-document-event-root-v1
 :gravity/sh07-b51-contract-predicate-fixture-v1
 :gravity/sh07-b51-contract-predicate-id-v1
 :gravity/sh07-b51-contract-predicate-spec-id-v1
 :gravity/sh07-b51-contract-read-receipt-v1
 :gravity/sh07-b51-contract-row-plan-id-v1
 :gravity/sh07-b51-contract-test-evidence-v1
 :gravity/sh07-b51-contract-test-result-root-v1
 :gravity/sh07-b51-coordinator-event-id-v1
 :gravity/sh07-b51-coordinator-event-root-v1
 :gravity/sh07-b51-coordinator-receipt-v1
 :gravity/sh07-b51-edge-authority-receipt-v1
 :gravity/sh07-b51-edn-wire-v1
 :gravity/sh07-b51-eligibility-check-root-v1
 :gravity/sh07-b51-evidence-eligibility-receipt-v2
 :gravity/sh07-b51-explicit-semantic-projection-v1
 :gravity/sh07-b51-external-review-object-v1
 :gravity/sh07-b51-fixed-fuel-bounds-v1
 :gravity/sh07-b51-fixed-fuel-carrier-root-v1
 :gravity/sh07-b51-fixed-fuel-receipt-v1
 :gravity/sh07-b51-fixed-fuel-stage-semantic-root-v1
 :gravity/sh07-b51-fixed-fuel-subject-v1
 :gravity/sh07-b51-fixed-fuel-trace-root-v1
 :gravity/sh07-b51-foundation-contract-root-v1
 :gravity/sh07-b51-foundation-fact-root-v1
 :gravity/sh07-b51-foundation-pass-root-v1
 :gravity/sh07-b51-foundation-provenance-root-v1
 :gravity/sh07-b51-g9-lifecycle-receipt-v1
 :gravity/sh07-b51-g9-publication-discharge-v1
 :gravity/sh07-b51-git-object-publication-tuple-v1
 :gravity/sh07-b51-governance-preservation-root-v1
 :gravity/sh07-b51-governed-source-session-id-v1
 :gravity/sh07-b51-host-surface-root-v1
 :gravity/sh07-b51-i9-integration-discharge-v1
 :gravity/sh07-b51-i9-integration-receipt-v1
 :gravity/sh07-b51-i9-preservation-root-v1
 :gravity/sh07-b51-l1-witness-v1
 :gravity/sh07-b51-l2-witness-v1
 :gravity/sh07-b51-launcher-receipt-id-v1
 :gravity/sh07-b51-launcher-receipt-v1
 :gravity/sh07-b51-launcher-validation-input-v1
 :gravity/sh07-b51-loaded-namespace-root-v1
 :gravity/sh07-b51-lowering-product-v1
 :gravity/sh07-b51-m-response-v1
 :gravity/sh07-b51-masked-rhs-v1
 :gravity/sh07-b51-masked-source-root-v1
 :gravity/sh07-b51-no-backend-v1
 :gravity/sh07-b51-operation-schedule-v1
 :gravity/sh07-b51-owned-c-descriptors-root-v1
 :gravity/sh07-b51-ownership-path-root-v1
 :gravity/sh07-b51-p9-final-discharge-v1
 :gravity/sh07-b51-p9-pin-publication-receipt-v1
 :gravity/sh07-b51-payload-semantic-root-v1
 :gravity/sh07-b51-phase-evidence-v1
 :gravity/sh07-b51-phase-provenance-root-v1
 :gravity/sh07-b51-post-g9-resolved-row-root-v1
 :gravity/sh07-b51-post-i9-resolved-row-root-v1
 :gravity/sh07-b51-post-p9-resolved-row-root-v1
 :gravity/sh07-b51-pre-evidence-contract-discharge-v1
 :gravity/sh07-b51-pre-resolved-row-root-v1
 :gravity/sh07-b51-process-argv-id-v1
 :gravity/sh07-b51-process-handle-coordinate-id-v1
 :gravity/sh07-b51-public-root-specs-id-v1
 :gravity/sh07-b51-response-preimage-id-v1
 :gravity/sh07-b51-runtime-contract-discharge-v1
 :gravity/sh07-b51-runtime-invalidation-v1
 :gravity/sh07-b51-runtime-output-root-v1
 :gravity/sh07-b51-runtime-replay-v1
 :gravity/sh07-b51-runtime-resolved-row-root-v1
 :gravity/sh07-b51-runtime-stage-comparison-v1
 :gravity/sh07-b51-runtime-tcb-delta-v1
 :gravity/sh07-b51-semantic-algorithm-source-root-v1
 :gravity/sh07-b51-semantic-witness-member-id-v1
 :gravity/sh07-b51-semantic-witness-member-root-v1
 :gravity/sh07-b51-semantic-witness-runtime-v1
 :gravity/sh07-b51-sh06-authority-carrier-id-v1
 :gravity/sh07-b51-sh06-link-authority-id-v1
 :gravity/sh07-b51-source-closure-root-v1
 :gravity/sh07-b51-source-descriptors-root-v1
 :gravity/sh07-b51-source-event-product-id-v1
 :gravity/sh07-b51-source-event-root-v1
 :gravity/sh07-b51-source-read-receipt-v3
 :gravity/sh07-b51-staged-payload-value-v1
 :gravity/sh07-b51-test10-post-i9-result-v1
 :gravity/sh07-b51-test10-preservation-observation-id-v1
 :gravity/sh07-b51-test13-post-i9-result-v1
 :gravity/sh07-b51-trusted-clojure-host-surface-v1
 :gravity/sh07-b51-v9-ownership-plan-v1
 :gravity/sh07-b51-verification-invalidation-v1
 :gravity/sh07-b51-verified-core-target-id-v1]
```

`H-DOMAIN-REGISTRY` has exactly `:domains :member-kinds`. Its domains are the
following exact unsigned-UTF-8-sorted subset of V9-TAG-REGISTRY; every member
has exactly one named H payload equation in this report. Every H domain defined
by V9 must occur here. An identity under such a domain whose registry member is
absent, whose domain has two payload grammars, or whose payload does not match
that equation rejects. The only closed exceptions are the inherited
role-specific checker-spec domains and the three inherited source domains
named below: declared digest, projected-SH06 binding, and execution bundle
core. Their products are recomputed during V9, but V9 does not define their
domains or payload grammars.

```clojure
[:gravity/sh07-b51-a9-lifecycle-receipt-v1
 :gravity/sh07-b51-authentic-lowering-witness-v1
 :gravity/sh07-b51-boot2-compiler-id-v1
 :gravity/sh07-b51-boot2-source-id-v1
 :gravity/sh07-b51-boot2-witness-v1
 :gravity/sh07-b51-boot3-witness-v1
 :gravity/sh07-b51-boot4-witness-v1
 :gravity/sh07-b51-boot5-compatibility-row-v1
 :gravity/sh07-b51-bounded-operation-id-v1
 :gravity/sh07-b51-bundle-core-public-roots-id-v1
 :gravity/sh07-b51-c2-project-root-id-v1
 :gravity/sh07-b51-c2-witness-v1
 :gravity/sh07-b51-c6-witness-v1
 :gravity/sh07-b51-checker-child-tcb-manifest-v1
 :gravity/sh07-b51-checker-process-identity-receipt-id-v1
 :gravity/sh07-b51-checker-request-id-v1
 :gravity/sh07-b51-checker-result-artifact-id-v1
 :gravity/sh07-b51-checker-result-output-id-v1
 :gravity/sh07-b51-child-process-receipt-v1
 :gravity/sh07-b51-classpath-manifest-root-v1
 :gravity/sh07-b51-contract-binding-v2
 :gravity/sh07-b51-contract-document-event-root-v1
 :gravity/sh07-b51-contract-predicate-fixture-v1
 :gravity/sh07-b51-contract-predicate-id-v1
 :gravity/sh07-b51-contract-predicate-spec-id-v1
 :gravity/sh07-b51-contract-row-plan-id-v1
 :gravity/sh07-b51-contract-test-evidence-v1
 :gravity/sh07-b51-contract-test-result-root-v1
 :gravity/sh07-b51-coordinator-event-id-v1
 :gravity/sh07-b51-coordinator-event-root-v1
 :gravity/sh07-b51-coordinator-receipt-v1
 :gravity/sh07-b51-edge-authority-receipt-v1
 :gravity/sh07-b51-eligibility-check-root-v1
 :gravity/sh07-b51-evidence-eligibility-receipt-v2
 :gravity/sh07-b51-fixed-fuel-bounds-v1
 :gravity/sh07-b51-fixed-fuel-carrier-root-v1
 :gravity/sh07-b51-fixed-fuel-receipt-v1
 :gravity/sh07-b51-fixed-fuel-stage-semantic-root-v1
 :gravity/sh07-b51-fixed-fuel-subject-v1
 :gravity/sh07-b51-fixed-fuel-trace-root-v1
 :gravity/sh07-b51-foundation-contract-root-v1
 :gravity/sh07-b51-foundation-fact-root-v1
 :gravity/sh07-b51-foundation-pass-root-v1
 :gravity/sh07-b51-foundation-provenance-root-v1
 :gravity/sh07-b51-g9-lifecycle-receipt-v1
 :gravity/sh07-b51-g9-publication-discharge-v1
 :gravity/sh07-b51-governance-preservation-root-v1
 :gravity/sh07-b51-governed-source-session-id-v1
 :gravity/sh07-b51-host-surface-root-v1
 :gravity/sh07-b51-i9-integration-discharge-v1
 :gravity/sh07-b51-i9-integration-receipt-v1
 :gravity/sh07-b51-i9-preservation-root-v1
 :gravity/sh07-b51-l1-witness-v1
 :gravity/sh07-b51-l2-witness-v1
 :gravity/sh07-b51-launcher-receipt-id-v1
 :gravity/sh07-b51-launcher-validation-input-v1
 :gravity/sh07-b51-loaded-namespace-root-v1
 :gravity/sh07-b51-lowering-product-v1
 :gravity/sh07-b51-masked-source-root-v1
 :gravity/sh07-b51-no-backend-v1
 :gravity/sh07-b51-operation-schedule-v1
 :gravity/sh07-b51-owned-c-descriptors-root-v1
 :gravity/sh07-b51-ownership-path-root-v1
 :gravity/sh07-b51-p9-final-discharge-v1
 :gravity/sh07-b51-p9-pin-publication-receipt-v1
 :gravity/sh07-b51-payload-semantic-root-v1
 :gravity/sh07-b51-phase-provenance-root-v1
 :gravity/sh07-b51-post-g9-resolved-row-root-v1
 :gravity/sh07-b51-post-i9-resolved-row-root-v1
 :gravity/sh07-b51-post-p9-resolved-row-root-v1
 :gravity/sh07-b51-pre-evidence-contract-discharge-v1
 :gravity/sh07-b51-pre-resolved-row-root-v1
 :gravity/sh07-b51-process-argv-id-v1
 :gravity/sh07-b51-process-handle-coordinate-id-v1
 :gravity/sh07-b51-public-root-specs-id-v1
 :gravity/sh07-b51-response-preimage-id-v1
 :gravity/sh07-b51-runtime-contract-discharge-v1
 :gravity/sh07-b51-runtime-invalidation-v1
 :gravity/sh07-b51-runtime-output-root-v1
 :gravity/sh07-b51-runtime-replay-v1
 :gravity/sh07-b51-runtime-resolved-row-root-v1
 :gravity/sh07-b51-runtime-stage-comparison-v1
 :gravity/sh07-b51-runtime-tcb-delta-v1
 :gravity/sh07-b51-semantic-algorithm-source-root-v1
 :gravity/sh07-b51-semantic-witness-member-id-v1
 :gravity/sh07-b51-semantic-witness-member-root-v1
 :gravity/sh07-b51-semantic-witness-runtime-v1
 :gravity/sh07-b51-sh06-authority-carrier-id-v1
 :gravity/sh07-b51-sh06-link-authority-id-v1
 :gravity/sh07-b51-source-closure-root-v1
 :gravity/sh07-b51-source-descriptors-root-v1
 :gravity/sh07-b51-source-event-product-id-v1
 :gravity/sh07-b51-source-event-root-v1
 :gravity/sh07-b51-staged-payload-value-v1
 :gravity/sh07-b51-test10-post-i9-result-v1
 :gravity/sh07-b51-test10-preservation-observation-id-v1
 :gravity/sh07-b51-test13-post-i9-result-v1
 :gravity/sh07-b51-v9-ownership-plan-v1
 :gravity/sh07-b51-verification-invalidation-v1
 :gravity/sh07-b51-verified-core-target-id-v1]
```

Member kinds are exactly
`[:boot2-dependencies :boot2-jvm :boot3-artifact :boot3-diagnostics
:boot3-facts :boot3-stage-comparison :boot3-tests
:boot4-capabilities :boot4-effects :boot4-module-manifest
:boot4-module-outputs :boot4-preservation-tests :c2-diagnostics :c2-forms
:c2-source-map :c2-tokens :c6-ast :c6-desugaring-trace :c6-evaluation-order
:c6-facts :c6-origins :c6-syntax-core-map :l1-syntax :l2-capabilities
:l2-core-form
:l2-edge-authority :l2-edge-diagnostics :l2-edge-negative-fixtures :l2-effects
:l2-evaluation-order :l2-expected-edge-census :l2-external-edges
:l2-owner-registry
:lowering-facts :lowering-modules :lowering-passes :lowering-provenance]`,
also unsigned-UTF-8 sorted and duplicate-free.

For avoidance of table-only domain inference, these eight root payload
equations are explicit and exclusive:

- `CLASSPATH-MANIFEST-ROOT =
  H([:gravity/sh07-b51-classpath-manifest-root-v1 CLASSPATH-MANIFEST-ROWS])`,
  where rows are the exact ordered entry-0 Git manifest rows.
- `PROJECT-ROOT-ID =
  H([:gravity/sh07-b51-c2-project-root-id-v1
  {:content {:commit C :tree T} :scope :sh07-b51-c2-explicit-project}])`.
- `LOADED-NAMESPACE-ROOT =
  H([:gravity/sh07-b51-loaded-namespace-root-v1
  LOADED-NAMESPACES-WITHOUT-ROOT])`.
- `HOST-SURFACE-ROOT =
  H([:gravity/sh07-b51-host-surface-root-v1 HOST-SURFACE-WITHOUT-ROOT])`.
- `SOURCE-EVENT-ROOT =
  H([:gravity/sh07-b51-source-event-root-v1 SOURCE-EVENTS])`.
- `COORDINATOR-EVENT-ROOT =
  H([:gravity/sh07-b51-coordinator-event-root-v1 COORDINATOR-EVENTS])`.
- `OWNERSHIP-PATH-ROOT(KIND, PATHS) =
  H([:gravity/sh07-b51-ownership-path-root-v1
  {:kind KIND :paths PATHS}])`, where KIND is exactly `:path-specs` or
  `:actual-paths` and PATHS is the corresponding dense ordered vector.
- `PAYLOAD-SEMANTIC-ROOT =
  H([:gravity/sh07-b51-payload-semantic-root-v1
  NORMALIZED-SIX-PAYLOAD-PROJECTIONS])`, where projections are in the exact
  six-payload artifact order and have every receipt-only field replaced.

No other payload grammar is valid for any of these domains. The later schema
paragraphs must reproduce these values rather than introducing another
equation.

The existing checker artifact, semantic, executable, dependency-closure, and
identity-manifest domains remain the literal role-specific domains in
`sh07_b51_attempt32_checker_identity/checker-specs`. The result artifact/output
domains remain the two literal domains printed in the response schema. No
domain is inferred from a field name.

The inherited `:gravity/sh07-declared-digest-v1` domain used by `Z_i` is not a
V9-defined domain and therefore is not a V9 tag or H-registry member. It is
the existing checked-core adapter domain executed only by the exact
`sh07-core-digest-requests` equation below, whose payload has exactly `:domain
:purpose :preimage`; changing its domain, purpose, preimage resolver, prior-Z
prefix, or canonical reader hash rejects. The inherited
`:gravity/sh07-projected-sh06-binding-v1` domain is likewise not a V9 tag or
H-registry member. V9 may execute it only while reproducing the authentic SH06
binding projection, with the sole payload grammar
`{:source-revision-id Q_i[:lineage :source-revision-id]
:binding (dissoc SEM(A,R_i,Q_i)
                 :upstream-binding-id :definition-artifact-id)}` and
only after `Q-REPLAY_i = Q_i` byte-for-byte. This classification does not
permit another unregistered V9 identity domain. The inherited
`:gravity/sh07-b51-execution-bundle-core-id-v1` domain is the literal `:domain`
of the authentic six-key 00945 BUNDLE-CORE. Its sole source-defined payload is
that complete BUNDLE-CORE value, and its identity is the sibling
`:execution-bundle-core-id` returned by the object-mode wrapper. V9 recomputes
that sibling with the governed 00945 `p15-s23-sh07-b51-canonical-id` algorithm,
equivalently H of the complete six-key core; the domain is not a V9 tag or
H-registry member. No fifth inherited or otherwise unregistered identity
domain is permitted.

Field identity typing is closed. Every `:source-sha256`,
`:architecture-report-sha256`, executable/JAR `:sha256`, and other field whose
enclosing schema calls it a byte SHA is R of the exact named bytes. Classpath
entry 0's `:sha256` is the H classpath-manifest root, while entries 1-3 are R
of JAR bytes. Every semantic identity field (`*-id`, `:artifact-id`,
`:semantic-id`, `:executable-id`, `:dependency-closure-id`, `:manifest-id`,
`:claim-id`, `:normalized-census-id`, `:output-id`, and `:certificate-id`) and
every root explicitly registered as an H identity is H under the literal domain
declared here or in the immutable role-specific checker spec. Path-valued
`:logical-root`, `:staging-root`, `:project-root-path`, and any `:project-root`
whose enclosing lifecycle schema explicitly calls it a path, plus receipt-only
host roots, are repository/absolute path strings and are not H identities. In
contrast, C2 source-unit `:project-root` and `:project-root-id` are exactly the
registered PROJECT-ROOT-ID H identity; a path string in either slot rejects.
Any other `*-root` is untyped and rejects.
Git `:oid`/`:blob-oid` fields are 40-hex SHA-1 object names, never H or R.
There is no untyped digest slot and no conversion between these families.

## Closed IPC request

The request is one strict canonical EDN form plus EOF. `CHECKER-REQUEST` is
the complete following map after replacing the typed slots, and
`CHECKER-REQUEST-ID` is exactly
`H([:gravity/sh07-b51-checker-request-id-v1 CHECKER-REQUEST])`:

```clojure
{:schema :gravity/sh07-b51-checker-child-request-v4
 :role :producer-census|:independent-verifier-census
 :content {:commit C :tree T}
 :eligibility ELIGIBILITY-COORDINATE
 :evidence {:architecture-report-sha256 SHA
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
All key sets are exactly shown. Role is one of the two shown keyword
alternatives. Bounds have exactly 17 keys; limits are the shown keyed map.
Eligibility is the exact immutable Q/TQ descriptor already validated by L and
M; each child repeats the five-operation Q-coordinate check and the separate
77-operation static PRE reconstruction before loading any project bootstrap
namespace.
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
       :pre-manifest-tcb PRE-MANIFEST-TCB
       :eligibility-check ELIGIBILITY-CHECK
       :environment ENVIRONMENT
       :source-closure SOURCE-CLOSURE
       :loaded-namespace-receipt LOADED-NAMESPACES
       :host-surface HOST-SURFACE}
 :diagnostics DIAGNOSTICS}
```

Capitalized tokens above are typed slots, not unresolved implementation
placeholders. Their exact closed definitions are:

- `ROLE` is the request role.
- `WORK` is exactly `{:vector [3 3 3 2 2] :total 13}` in every authoritative
  initial-publication or object-replay execution.
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
- `SOURCE-READ-RECEIPT` has exactly `:schema :role :commit :tree
  :source-descriptors :source-descriptors-root :events :event-root :counters
  :finalized`. Schema is `:gravity/sh07-b51-source-read-receipt-v3`; the twelve
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
  absent from both vectors, and root is exactly
  `H([:gravity/sh07-b51-source-closure-root-v1
  SOURCE-CLOSURE-WITHOUT-ROOT])`. For A/B the
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
:inputs :result :semantic-witness :provenance :raw-source-transcript :m2-transcript
:profile-bounds :evidence-root :certificate-id`. Its checker map has exactly
`:artifact-id :source-sha256 :semantic-id :executable-id
:dependency-closure-id :algorithm-role`. Inputs have exactly
`:architecture-report-sha256 :snapshot-commit :snapshot-tree :lowering-product-id
:predecessor-executable-descriptor-id :module-source-identities
:module-source-byte-counts :emitter-source-id :emitter-semantic-id :m0-ids
:m1-id :m2-ids :bundle-core-id`. Provenance has exactly `:invocation-id
:tool-closure-id :reproduction-command-id :evidence-relative-path
:completed-at :raw-transcript-artifact-id :m2-transcript-artifact-id`.
Lowering product ID is the role's already-fixed LOWERING-PRODUCT ID; the
certificate contains no reverse edge from that product.

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

M writes only the first six canonical payloads into L's fresh private,
symlink-free staging directory outside every worktree. M closes
and postchecks them before returning its compact response; it cannot construct
the seventh file because it cannot observe its own parent launch without a
cycle. L validates the closed M response and exact six staged byte descriptors,
constructs `LAUNCHER-RECEIPT`, and constructs the seventh receipt from those
closed inputs.

In `:new`, L publishes only into the separate validated publication worktree.
It calls `Files.createDirectory(FINAL)` exactly once without using the earlier
absence observation as publication authority; an existing empty or nonempty
target rejects. It records the new directory file key, installs the six staged
files with create-new semantics, and writes `identity-receipt.edn` last with
`StandardOpenOption/CREATE_NEW`. That seventh file is the sole publication
commit marker. A reader treats FINAL as unpublished until it exists and then
requires exactly seven files. On failure, cleanup removes only an unpublished
directory whose recorded file key still matches and whose entries are a prefix
of L's exact writes; a foreign entry or changed key is quarantined and rejects
without deletion. A separate governed E9 freeze commits that publication
worktree without changing the clean C source worktree, then verifies exact
E/TE/parents `[C]`, seven added paths, modes, OIDs, counts, and W bytes before
the worktree or commit can become evidence.

In `:existing`, L never resolves FINAL in any filesystem. It verifies exact
E/TE and reads all seven blobs through absolute Git object commands. Fresh
A/B/M output must reproduce the first six W byte vectors byte-for-byte. L
constructs a fresh seventh receipt in memory; because process IDs, host paths,
file keys, and transport observations are receipt-only, raw seventh-file bytes
need not match. Instead strict reload must reproduce the same normalized
semantic projection and final receipt ID as E's seventh blob. Any semantic
field difference rejects. L writes zero final bytes in this mode.

L records process provenance but cannot change the six semantic payloads. The
one-way M-response -> L-receipt construction has no response/receipt identity
cycle and the separate worktree plus create-directory reservation cannot
replace the erratum's racing empty directory.

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
 :pre-discharge-id SHA
 :runtime-contract-discharge RUNTIME-CONTRACT-DISCHARGE
 :receipt-id-normalization NORMALIZATION
 :self-id-omits [[:receipt-id]]
 :receipt-id SHA
 :nonclaims NONCLAIMS}
```

`EVIDENCE-ELIGIBILITY-JOIN` is exactly the complete
`ELIGIBILITY-COORDINATE`, including Q/TQ/[C], path, mode, type, OQ, count, raw
SHA, and receipt ID. Every field is preserved from the independently repeated
L/M/A/B object checks; receipt ID is recomputed H and raw SHA is R of exact W
bytes. The eligibility
receipt cannot reference E9, and E9 references only this already-fixed object,
so the edge is one-way and acyclic.
The identity receipt's `:pre-discharge-id` equals the strictly decoded Q
pre-discharge `:discharge-id`; RUNTIME-CONTRACT-DISCHARGE's prior-ref selects
that same map and ID. Runtime discharge contains neither this identity receipt
nor its E9 object descriptor, so both joins remain one-way.

`PROGRESS` is exactly `{:slice :SH-07 :status :partial
:completed-stage-count 4 :total-stage-count 8}`. `NONCLAIMS` is the single
normative nonclaim vector used everywhere in V9 and is exactly
`[:formal-proof :sh07-complete :self-hosting :release :seed-retirement
:hostile-host]`.

`PHASES` is exactly `[:pre-evidence :runtime :post-i9 :post-g9 :post-p9]`.
`ROW-PLAN-ID` is
`H([:gravity/sh07-b51-contract-row-plan-id-v1 ROW-PLAN])`. The corresponding
phase-root domains, in PHASES order, are exactly
`[:gravity/sh07-b51-pre-resolved-row-root-v1
:gravity/sh07-b51-runtime-resolved-row-root-v1
:gravity/sh07-b51-post-i9-resolved-row-root-v1
:gravity/sh07-b51-post-g9-resolved-row-root-v1
:gravity/sh07-b51-post-p9-resolved-row-root-v1]`.

`PRE-EVIDENCE-CONTRACT-DISCHARGE` uses the uniform discharge shape and has
exactly `:artifact :schema-version :phase :row-plan-id :prior-ref :evidence
:resolved-rows :resolved-root :authority :self-id-omits :discharge-id`.
Artifact is `:gravity/sh07-b51-pre-evidence-contract-discharge-v1`, version is
1, phase is `:pre-evidence`, row-plan-id is ROW-PLAN-ID, and prior-ref is nil.
Evidence is PHASE-EVIDENCE; its carriers have exactly `:content
:document-read-receipt :source-descriptors :source-descriptors-root
:masked-root :owned-c-descriptors :owned-c-root :row-plan :foundation-static
:boot5-compatibility :ownership-plan
:catalog-projection :governance-rules :identity-domain-registry
:publication-boundary :proof-scope
:progress :nonclaims :a9-lifecycle`. Content is `{:commit C :tree T}`; row
plan is the complete literal ROW-PLAN; the three source/masked values equal the
independently reconstructed Q fields; and a9-lifecycle is the immutable A9
integration coordinate/result defined below. It is constructed entirely from
already-immutable C/T and A9 inputs before Q and contains no Q/runtime/future
value. Resolved rows are the dense pre-evidence vector and resolved root is
`H([:gravity/sh07-b51-pre-resolved-row-root-v1 RESOLVED-ROWS])`.
Self-id-omits is `[[:discharge-id]]`; discharge ID is
`H([:gravity/sh07-b51-pre-evidence-contract-discharge-v1
DISCHARGE-WITHOUT-ID])`.

`RUNTIME-CONTRACT-DISCHARGE` has that same uniform exact key set. Artifact is
`:gravity/sh07-b51-runtime-contract-discharge-v1`, version is 1, phase is
`:runtime`, and row-plan-id is ROW-PLAN-ID. Prior-ref authenticates the
pre-evidence discharge at `[:pre-evidence-contract-discharge]` in Q. Evidence
is PHASE-EVIDENCE; its carriers have exactly `:content :evidence-eligibility
:source-session-a :source-session-b :source-session-m :launcher :coordinator
:checker-a :checker-b :masked-root :foundation-runtime :bootstrap-runtime
:differential-runtime :verification-runtime :self-hosting-runtime
:semantic-witness-runtime :contract-test-results :proof-scope :progress
:nonclaims`. Every carrier is
the complete normalized nonrecursive value used by the runtime binding specs;
`:launcher` is specifically LAUNCHER-AUTHORITY and never the containing
launcher receipt, so a later
review can recompute every runtime binding ID from this discharge alone.
Evidence eligibility is the complete ELIGIBILITY-COORDINATE plus the decoded
  receipt ID and check roots. The carrier has exactly `:coordinate :receipt-id
  :freeze-state :freeze-record-id
:source-descriptors-root :masked-root :pre-discharge-id
  :participant-check-roots`; freeze state is `:frozen`, freeze record ID is the
  recomputed immutable active-record receipt ID from Q, participant roots are L/M/A/B order and each is
`H([:gravity/sh07-b51-eligibility-check-root-v1 ELIGIBILITY-CHECK])` for that
process. Every other field must equal the strictly decoded Q receipt. It is
constructed only after L/M/A/B have validated Q and completed, resolves
exactly `:runtime` bindings, and contains no E/B/G/P,
enclosing identity-receipt ID, or post-phase value. Resolved root is
`H([:gravity/sh07-b51-runtime-resolved-row-root-v1 RESOLVED-ROWS])`;
self-id-omits is `[[:discharge-id]]`; discharge ID is
`H([:gravity/sh07-b51-runtime-contract-discharge-v1
DISCHARGE-WITHOUT-ID])`.

The three external discharge objects, separate from the lifecycle receipts
they authenticate, resolve respectively the exact `:post-i9`, `:post-g9`, and
`:post-p9` binding specs. The final P9 discharge carries the exact PHASE-ROOTS
vector above, validates the chained PRE and RUNTIME discharge IDs plus the
three post-phase discharge IDs, and proves every planned binding ID is resolved exactly
once. An earlier phase containing a later receipt, commit, tree, discharge, or
binding value rejects as `SH07-B51-IDENTITY-CYCLE`.

The document-read receipt has exactly `:schema :content :documents :events
:event-root :counters :finalized`. Schema is
:gravity/sh07-b51-contract-read-receipt-v1`; it contains the 33 unique
mode-100644 C objects in this exact ordinal/path vector (no other prose list
controls the order):

```clojure
[[0 "AGENTS.md"]
 [1 "docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md"]
 [2 "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"]
 [3 "docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md"]
 [4 "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md"]
 [5 "docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md"]
 [6 "docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md"]
 [7 "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"]
 [8 "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"]
 [9 "docs/phase-01-core-language/012-l2-core-language-semantics.md"]
 [10 "docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md"]
 [11 "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"]
 [12 "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md"]
 [13 "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"]
 [14 "docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md"]
 [15 "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"]
 [16 "docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md"]
 [17 "docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md"]
 [18 "docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md"]
 [19 "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"]
 [20 "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"]
 [21 "docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md"]
 [22 "docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md"]
 [23 "docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md"]
 [24 "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn"]
 [25 "docs/self-hosting-slice-backlog.md"]
 [26 "docs/self-hosting-slice-ownership.edn"]
 [27 "contracts/verification-catalog.edn"]
 [28 "bootstrap/gravity/src/gravity/checked_core.gravity"]
 [29 "docs/workstream-governance.md"]
 [30 "contracts/workstream-governance.json"]
 [31 "docs/workstream-ledger-v2.md"]
 [32 "contracts/workstream-ledger.json"]]
```

Each pair expands to the exact document descriptor `{:ordinal N :path PATH
:mode "100644" :blob-oid O :byte-count COUNT :raw-sha256 R}`, with O/count/R
recomputed from C/T. The receipt also contains 33 dense matching read events.
Counters are exactly `{:acquire-count 33 :decode-count 33
:event-count 33 :finalize-count 1}` and finalized is true. Each document
descriptor has exactly `:ordinal :path :mode :blob-oid :byte-count
:raw-sha256`; each event has exactly `:ordinal :document-ordinal :path
:raw-sha256 :binding-ids`. For descriptor ordinal N, event ordinal and
document-ordinal are both N and its path/raw SHA are byte-identical to that
descriptor. Event binding IDs are exactly the `:id` fields of binding specs, in row
then binding-spec order, of every literal row below whose document-ordinals contain
the event ordinal. Event root is exactly
`H([:gravity/sh07-b51-contract-document-event-root-v1 EVENTS])`. This separate contract session is not folded into the
12-source semantic session.

`OWNED-C-DESCRIPTORS` is the exact 27-row vector in OWNED-C-PATHS order. Each
row has exactly `:ordinal :path :mode :blob-oid :byte-count :raw-sha256`, is
independently reconstructed by PRE-DISCHARGE-OBSERVATIONS, and binds the C
blob with mode `"100644"`. `OWNED-C-ROOT` is
`H([:gravity/sh07-b51-owned-c-descriptors-root-v1 OWNED-C-DESCRIPTORS])`.
Runtime and post-phase tests may load only these authenticated bytes through
the bounded materializer; they may not resolve the same paths from a worktree.

`PUBLICATION-BOUNDARY` has exactly `:content :scope :created-paths
:created-kinds :mir-artifacts :backend-artifacts :stage-support-claims`.
Content is `{:commit C :tree T}`, scope is `:verified-core`, created paths are
the exact seven E9 paths in artifact order, created kinds are exactly
`[:checker-identity :checker-identity :checker-output :checker-output
:certificate-pair :linked-carrier :identity-receipt]`, and the final three
vectors are empty. This is the changed-path and no-MIR/backend/stage-claim
carrier, not an inference from a missing key.

`FOUNDATION-STATIC` has exactly `:content :evidence-kind
:canonical-pipeline-contract-id :safe-semantics :replay-key-fields
:invalidation-key-fields`. Content is C/T; evidence kind is `:validation`;
canonical-pipeline contract ID is
`H([:gravity/sh07-b51-foundation-contract-root-v1 SEVEN-DESCRIPTORS])`;
safe semantics is exactly `{:allowed-outcomes [:proven-safe
:runtime-checked :rejected :unsafe-island] :claims []}`; replay key fields are
exactly `[:content-commit :content-tree :source-root :checker-root
:architecture-report-sha256]`; invalidation key fields append
`:governing-document-root`. It defines identity inputs, not future IDs.

`BOOT5-COMPATIBILITY` has exactly `:baseline-document :candidate-document
:document-equal :compatibility-row :compatibility-digest :explicit-gaps
:stage-support-claims :stage-advanced`.
Baseline and candidate descriptors are independently read Git-object
descriptors for BOOT5 at base `09c795f0c0854d2d8ab542c92b2116515e096406`
and C; every mode/OID/count/raw-SHA field is equal and `document-equal` is true.
The compatibility row has exactly `:stage :compiler-owner :implemented-documents
:language-subset :profiles :backends :runtime-services
:package-build-features :standard-library-subset :conformance-suites
:known-gaps :release-eligible` and equals:

```clojure
{:stage :seed-hosted-validation
 :compiler-owner :clojure-seed
 :implemented-documents [:L1 :L2 :C2 :C6]
 :language-subset :sh07-b51-verified-core
 :profiles [:meta]
 :backends []
 :runtime-services [:jvm-process-isolation :git-object-source]
 :package-build-features []
 :standard-library-subset []
 :conformance-suites [:sh07-b51-v9-contract-discharge]
 :known-gaps [:self-hosted-compiler :mir :backend :release]
 :release-eligible false}
```

Compatibility digest is
`H([:gravity/sh07-b51-boot5-compatibility-row-v1 COMPATIBILITY-ROW])`;
explicit gaps equal the row's exact `:known-gaps` vector, stage-support claims
are empty, and stage-advanced is false. Thus row 11 binds a machine-readable
row and digest, not only equality of the surrounding Markdown bytes.

`A9-LIFECYCLE` has exactly `:coordinate :receipt :architecture-integrated`.
Coordinate has exactly `:commit :tree :path :mode :type :blob-oid :byte-count
:raw-sha256`, binds C/T and the A9 lifecycle receipt path, and is reconstructed
by the PRE C-object observation plan. Receipt is the complete strictly decoded
A9 lifecycle receipt defined above; its own ID, frozen architecture and
companion descriptors, acceptance, admission, integration, ancestry, and
preservation equations are all revalidated. Architecture-integrated is true
iff its state is integrated, its report and companion bytes equal the PRE C
descriptors, its accepted candidate commit/tree and review receipt recompute,
its integration preservation rows prove those same two paths byte-equal at C,
and its no-overclaim fields remain false. No ancestry claim is made from an
unrecorded operation. This is the sole PRE carrier allowed to support the
`:a9-architecture-integrated` authority milestone.

`V9-OWNERSHIP-PLAN` has exactly `:schema :allocations
:semantic-review-owner :reservation-required :handoff-required
:read-only-paths :open-exit-gates :aggregate-sh07-state :self-id-omits
:plan-id`. Schema is
`:gravity/sh07-b51-v9-ownership-plan-v1`; allocations are the dense vector
`[[0 :a9 :master-coordinator A9-PATHS]
[1 :i9 :master-coordinator I9-LIFECYCLE-PATHS]
[2 :e9 :master-coordinator E9-PATHS]
[3 :g9 :master-coordinator G9-LIFECYCLE-PATHS]
[4 :p9 :master-coordinator P9-LIFECYCLE-PATHS]]`, expanded to maps with exactly
`:ordinal :stage :owner :paths`. Every path element is either an exact literal
repository path string or a closed `GOVERNANCE-PATH-SPEC` with exactly
`:kind :directory :workstream-id :record-digest-domain :filename-rule`.
Kind is `:content-addressed-terminal-record`, directory is
`"contracts/workstream-records/terminal"`, digest domain is `:sha256`, and
filename rule is `:workstream-id-hyphen-record-sha256-dot-json`. The path spec
is static; the phase result replaces it with the one actual path whose record
bytes recompute the filename digest. The exact vectors are defined in Owned
implementation and tests below. Semantic review owner is `:sh-core`;
both booleans are true; read-only paths are the exact generic source/catalog/
governance paths named below; open exit gates are exactly `[:core :evaluation
:arity :mutation :recursion :exception :pattern]`; aggregate state is
`:partial`; self-id-omits is `[[:plan-id]]`; plan ID is
`H([:gravity/sh07-b51-v9-ownership-plan-v1 PLAN-WITHOUT-PLAN-ID])`.

`GOVERNANCE-RULES` has exactly `:permitted-transitions
:admission-requirements :external-discharge-order :final-cleanup-required
:aggregate-sh07-state`. Permitted transitions are the exact lifecycle vector
`[:draft :frozen :review-pending :accepted :integration-eligible
:integrated]`; admission requirements are exactly `[:exact-base
:exact-candidate :clean-named-branch :owned-paths :governing-contracts
:validation :independent-acceptance :residual-boundaries :nonclaims]`;
external discharge order is `[:pre-evidence :runtime :post-i9 :post-g9
:post-p9]`; final cleanup is true; aggregate state is `:partial`. This records
rules only and is reconstructed from document descriptors 29, 30, 31, and 32,
including the ledger-v2/manifest linkage. Attained states and receipt IDs appear exclusively in their later
external phase discharge.

The carrier pools are disjoint. `PRE-CARRIERS` has exactly
`:document-read-receipt :proof-scope :progress :publication-boundary
:governance-rules :identity-domain-registry :foundation-static
:boot5-compatibility :ownership-plan
:catalog-projection :a9-lifecycle :owned-c-descriptors :owned-c-root`.
`RUNTIME-CARRIERS` has exactly `:source-session-a
:source-session-b :source-session-m :launcher :coordinator :checker-a
:checker-b :masked-root :foundation-runtime :bootstrap-runtime
:differential-runtime :verification-runtime :self-hosting-runtime
:semantic-witness-runtime :contract-test-results`. The three
source sessions are bound respectively to
`CHILD-A[:response :reconstruction :source-read-receipt]`,
`CHILD-B[:response :reconstruction :source-read-receipt]`, and
`COORDINATOR[:source-read-receipt]`; launcher is LAUNCHER-AUTHORITY, exactly
N-LAUNCHER-VALIDATION-INPUT; coordinator is the complete closed
receipt; checker A/B are the two complete parsed checker-output
payloads; and the remaining keys bind the named complete maps above and below.
`POST-I9-CARRIERS`, `POST-G9-CARRIERS`, and `POST-P9-CARRIERS` each have exactly
one key, `:phase-evidence`, whose value is the complete nonrecursive
PHASE-EVIDENCE map for that phase. PRE contains no Q/runtime/future value;
RUNTIME may bind Q but no E/B/G/P or containing receipt ID; each POST pool
binds only values already immutable at that phase. No pool contains its
enclosing discharge or discharge ID.

`PHASE` is exactly `:pre-evidence`, `:runtime`, `:post-i9`, `:post-g9`, or
`:post-p9`. A carrier selector must be in the exact pool for its phase. A binding path is a
vector containing only keywords for map access and nonnegative integers for
vector access; `[]` selects the complete artifact. Every step must exist with
the required container type and the final value must be nonnil. A missing key,
invalid index, nil selector, extra carrier key, duplicate binding ID, or
permissive `get-in` default rejects as `SH07-B51-CONTRACT-DISCHARGE`.

For compactness in the literal vector below, `B(ID, PHASE, CARRIER, PATH,
OBLIGATIONS)` is normative shorthand for exactly `{:id ID :phase PHASE
:carrier CARRIER :path PATH :obligations OBLIGATIONS}`. Every obligations
vector is nonempty, sorted by the literal row order rather than at runtime, and
contains distinct keywords. `RESOLVE(ROW-ORDINAL, B, VALUE)` adds exactly
`:value-id H([:gravity/sh07-b51-contract-binding-v2 {:row-ordinal ROW-ORDINAL
:id ID :phase PHASE :carrier CARRIER :path PATH :obligations OBLIGATIONS
:value VALUE}])`; VALUE is the strict path resolution in that phase's pool.
The plan never contains VALUE or `:value-id`. `S(N, HEADINGS,
WHOLE)` is exactly `{:document-ordinal N :headings HEADINGS :whole-document
WHOLE}`. HEADINGS contains literal complete Markdown heading lines; a
non-Markdown whole-document input has `[]` and true. No runtime constructor
may vary these values.

The architecture-owned literal `ROW-PLAN` of 19 rows is exactly:

```clojure
[{:ordinal 0 :id :agents :document-ordinals [0]
  :sections [(S 0 ["## Source Language Boundary" "## Start Here"
                   "## Source Of Truth" "## Implementation Principles"
                   "## Agent Guardrails: Preventing Over-Engineering"
                   "## Documentation Work" "## Validation" "## Working Discipline"
                   "## Workstream Lifecycle And Integration"] false)]
  :disposition :applies
  :obligations [:source-language-boundary :normative-priority :ownership
                :validation :no-overclaim :governed-lifecycle]
  :binding-specs
  [(B :agents-contract-bytes :pre-evidence :document-read-receipt [:documents 0]
      [:normative-priority])
   (B :agents-launcher-boundary :runtime :launcher []
      [:source-language-boundary :validation])
   (B :agents-coordinator-boundary :runtime :coordinator [] [:validation])
   (B :agents-ownership-plan :pre-evidence :ownership-plan [] [:ownership])
   (B :agents-progress :pre-evidence :progress [] [:no-overclaim])
   (B :agents-final-lifecycle :post-p9 :phase-evidence
      [:carriers :final-lifecycle] [:governed-lifecycle])]
  :test-ids [:sh07-b51-v9-contract-agents]
  :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
 {:ordinal 1 :id :foundation :document-ordinals [1 2 3 4 5 6 7]
  :sections [(S 1 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)
             (S 2 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)
             (S 3 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)
             (S 4 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)
             (S 5 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)
             (S 6 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)
             (S 7 ["## Requirements" "## Outputs and Artifacts" "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:lineage :pipeline-records :fact-provenance :safe-semantics
                :evidence-kind :replay :invalidation]
  :binding-specs
  [(B :foundation-static :pre-evidence :foundation-static []
      [:safe-semantics :evidence-kind :replay :invalidation])
   (B :foundation-runtime :runtime :foundation-runtime []
      [:pipeline-records :fact-provenance :replay :invalidation])
   (B :foundation-lineage :post-i9 :phase-evidence
      [:carriers :foundation-lineage] [:lineage])]
  :test-ids [:sh07-b51-v9-contract-foundation]
  :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
 {:ordinal 2 :id :l1 :document-ordinals [8]
  :sections [(S 8 ["## Source Units" "## Requirements" "## Outputs and Artifacts"
                   "## Diagnostics" "## Conformance Criteria"] false)]
  :disposition :preserved
  :obligations [:source-bytes :source-order :source-id :syntax-origin-metadata
                :no-grammar-change]
  :binding-specs
  [(B :l1-contract :pre-evidence :document-read-receipt [:documents 8]
      [:no-grammar-change])
   (B :l1-source-descriptor :runtime :source-session-m [:source-descriptors 0]
      [:source-bytes :source-order :source-id])
   (B :l1-semantic-witness :runtime :semantic-witness-runtime [:witness :l1]
      [:syntax-origin-metadata :no-grammar-change])]
  :test-ids [:sh07-b51-v9-contract-l1]
  :diagnostic :SH07-B51-GIT-INPUT-BLOB}
 {:ordinal 3 :id :l2 :document-ordinals [9]
  :sections [(S 9 ["## Core Forms" "## Evaluation Model" "## Requirements"
                   "## Outputs and Artifacts" "## Diagnostics" "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:core-semantics :evaluation-order :effects-capabilities-profile
                :diagnostics :negative-fixtures :authentic-owning-root
                :authenticated-external-bindings
                :unauthorized-cross-fragment-rejection]
  :binding-specs
  [(B :l2-semantic-witness :runtime :semantic-witness-runtime [:witness :l2]
      [:core-semantics :evaluation-order :effects-capabilities-profile
       :diagnostics :negative-fixtures :authentic-owning-root
       :authenticated-external-bindings
       :unauthorized-cross-fragment-rejection])]
  :test-ids [:sh07-b51-v9-contract-l2]
  :diagnostic :SH07-B51-EDGE-AUTHORITY}
 {:ordinal 4 :id :c2 :document-ordinals [10]
  :sections [(S 10 ["## Requirements" "## Outputs and Artifacts" "## Source Unit Record"
                    "## Incremental Hashing" "## Diagnostics" "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:immutable-source-snapshot :encoding :source-unit-identities
                :source-map :diagnostics]
  :binding-specs
  [(B :c2-semantic-witness :runtime :semantic-witness-runtime [:witness :c2]
      [:immutable-source-snapshot :encoding :source-unit-identities
       :source-map :diagnostics])]
  :test-ids [:sh07-b51-v9-contract-c2]
  :diagnostic :SH07-B51-GIT-SNAPSHOT}
 {:ordinal 5 :id :c6 :document-ordinals [11]
  :sections [(S 11 ["## Requirements" "## Outputs and Artifacts" "## Core Node Shape"
                    "## Desugaring Trace" "## Evaluation Order" "## Core Verification" "## Diagnostics"
                    "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:core-ast :syntax-core-map :trace :origins-facts-order
                :verification :mutations]
  :binding-specs
  [(B :c6-semantic-witness :runtime :semantic-witness-runtime [:witness :c6]
      [:core-ast :syntax-core-map :trace :origins-facts-order :verification
       :mutations])]
  :test-ids [:sh07-b51-v9-contract-c6]
  :diagnostic :SH07-B51-EDGE-AUTHORITY}
 {:ordinal 6 :id :c11 :document-ordinals [12]
  :sections [(S 12 ["## Requirements" "## Outputs and Artifacts" "## Module Shape"
                    "## Operation Schema" "## Metadata Tables" "## MIR Verification"
                    "## Diagnostics" "## Conformance Criteria"] false)]
  :disposition :not-applicable
  :obligations [:verified-core-boundary :no-mir-artifacts
                :no-backend-artifacts :no-stage-support-claims]
  :binding-specs
  [(B :c11-publication-boundary :pre-evidence :publication-boundary []
      [:verified-core-boundary :no-mir-artifacts :no-backend-artifacts
       :no-stage-support-claims])
   (B :c11-proof-scope :pre-evidence :proof-scope [] [:verified-core-boundary])]
  :test-ids [:sh07-b51-v9-contract-c11-nonclaim]
  :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
 {:ordinal 7 :id :boot1 :document-ordinals [13]
  :sections [(S 13 ["## Stage Outline" "## Requirements" "## Outputs and Artifacts"
                    "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:stage :trusted-inputs :compiler-lineage :environment :conformance]
  :binding-specs
  [(B :boot1-compatibility :pre-evidence :boot5-compatibility [] [:stage])
   (B :boot1-launcher :runtime :launcher [] [:trusted-inputs :environment])
   (B :boot1-coordinator :runtime :coordinator [] [:trusted-inputs :environment :conformance])
   (B :boot1-lineage :post-i9 :phase-evidence
      [:carriers :compiler-lineage] [:compiler-lineage])]
  :test-ids [:sh07-b51-v9-contract-boot1]
  :diagnostic :SH07-B51-TRUSTED-TCB}
 {:ordinal 8 :id :boot2 :document-ordinals [14]
  :sections [(S 14 ["## Implemented Subset" "## Requirements" "## Outputs and Artifacts"
                    "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:implemented-excluded-documents :host-dependencies
                :runtime-assumptions :source-compiler-backend-dependency-hashes]
  :binding-specs
  [(B :boot2-compatibility :pre-evidence :boot5-compatibility []
      [:implemented-excluded-documents])
   (B :boot2-launcher-tcb :runtime :launcher [:pre-manifest-tcb]
      [:host-dependencies :runtime-assumptions])
   (B :boot2-coordinator-tcb :runtime :coordinator [:pre-manifest-tcb]
      [:host-dependencies :runtime-assumptions])
   (B :boot2-runtime :runtime :semantic-witness-runtime [:witness :boot2]
      [:source-compiler-backend-dependency-hashes])]
  :test-ids [:sh07-b51-v9-contract-boot2]
  :diagnostic :SH07-B51-TRUSTED-TCB}
 {:ordinal 9 :id :boot3 :document-ordinals [15]
  :sections [(S 15 ["## Migration Order" "## Requirements" "## Outputs and Artifacts"
                    "## Conformance Criteria"] false)]
  :disposition :preserved
  :obligations [:module-io :effects-facts :tests :meta-profile
                :diagnostic-compatibility :no-stage-advance]
  :binding-specs
  [(B :boot3-semantic-witness :runtime :semantic-witness-runtime [:witness :boot3]
      [:module-io :effects-facts :tests :diagnostic-compatibility])
   (B :boot3-proof-scope :pre-evidence :proof-scope [] [:meta-profile])
   (B :boot3-compatibility :pre-evidence :boot5-compatibility [] [:no-stage-advance])]
  :test-ids [:sh07-b51-v9-contract-boot3]
  :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
 {:ordinal 10 :id :boot4 :document-ordinals [16]
  :sections [(S 16 ["## Module Rules" "## Requirements" "## Outputs and Artifacts"
                    "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:closed-module-manifest :effects-capabilities
                :deterministic-output :fact-provenance
                :positive-negative-preservation-tests]
  :binding-specs
  [(B :boot4-source-closure :runtime :coordinator [:source-closure]
      [:closed-module-manifest])
   (B :boot4-host-surface :runtime :coordinator [:host-surface]
      [:effects-capabilities])
   (B :boot4-semantic-witness :runtime :semantic-witness-runtime [:witness :boot4]
      [:effects-capabilities :deterministic-output :fact-provenance
       :positive-negative-preservation-tests])]
  :test-ids [:sh07-b51-v9-contract-boot4]
  :diagnostic :SH07-B51-FORBIDDEN-VAR}
 {:ordinal 11 :id :boot5 :document-ordinals [17]
  :sections [(S 17 ["## Matrix Columns" "## Requirements" "## Outputs and Artifacts"
                    "## Conformance Criteria"] false)]
  :disposition :preserved
  :obligations [:compatibility-row :compatibility-digest :explicit-gaps
                :no-stage-advance]
  :binding-specs
  [(B :boot5-compatibility :pre-evidence :boot5-compatibility []
      [:compatibility-row :compatibility-digest :explicit-gaps :no-stage-advance])
   (B :boot5-runtime :runtime :self-hosting-runtime [] [:no-stage-advance])]
  :test-ids [:sh07-b51-v9-contract-boot5]
  :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
 {:ordinal 12 :id :boot6-8 :document-ordinals [18 19 20]
  :sections [(S 18 ["## Requirements" "## Outputs and Artifacts" "## Rejection Rules" "## Conformance Criteria"] false)
             (S 19 ["## Requirements" "## Outputs and Artifacts" "## Rejection Rules" "## Conformance Criteria"] false)
             (S 20 ["## Requirements" "## Outputs and Artifacts" "## Rejection Rules" "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:fresh-processes :stage-comparison :tcb-delta :provenance
                :cleanup :replay]
  :binding-specs
  [(B :boot6-8-launcher :runtime :launcher [] [:fresh-processes :provenance :cleanup])
   (B :boot6-8-coordinator :runtime :coordinator [] [:fresh-processes :provenance])
   (B :boot6-8-runtime :runtime :bootstrap-runtime []
      [:stage-comparison :tcb-delta :provenance :cleanup :replay])
   (B :boot6-8-i9 :post-i9 :phase-evidence [:carriers :provenance] [:provenance])
   (B :boot6-8-g9 :post-g9 :phase-evidence [:carriers :provenance] [:provenance])
   (B :boot6-8-p9 :post-p9 :phase-evidence [:carriers :provenance] [:provenance])]
  :test-ids [:sh07-b51-v9-contract-boot6-8]
  :diagnostic :SH07-B51-CHECKER-PROCESS}
 {:ordinal 13 :id :test10 :document-ordinals [21]
  :sections [(S 21 ["## Oracles" "## Requirements" "## Outputs and Artifacts"
                    "## Rejection Rules" "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:oracle-identities :applicability :execution-context
                :observables :diagnostics :permitted-divergence :reproducer]
  :binding-specs
  [(B :test10-runtime :runtime :differential-runtime []
      [:oracle-identities :applicability :execution-context :observables
       :diagnostics :permitted-divergence :reproducer])
   (B :test10-main-i9 :post-i9 :phase-evidence [:carriers :test10-result]
      [:oracle-identities :applicability])]
  :test-ids [:sh07-b51-v9-contract-test10]
  :diagnostic :SH07-B51-NORMALIZE-DIVERGENCE}
 {:ordinal 14 :id :test11 :document-ordinals [22]
  :sections [(S 22 ["## Requirements" "## Outputs and Artifacts" "## Rejection Rules"
                    "## Conformance Criteria"] false)]
  :disposition :applies
  :obligations [:assumptions :trusted-basis :checker-input-claim-ids
                :invalidation :counterexamples :validation-not-proof]
  :binding-specs
  [(B :test11-proof-scope :pre-evidence :proof-scope [] [:validation-not-proof])
   (B :test11-runtime :runtime :verification-runtime []
      [:assumptions :trusted-basis :checker-input-claim-ids
       :invalidation :counterexamples])]
  :test-ids [:sh07-b51-v9-contract-test11]
  :diagnostic :SH07-B51-PROOF-SCOPE}
 {:ordinal 15 :id :test13 :document-ordinals [23]
  :sections [(S 23 ["## Stages" "## Requirements" "## Outputs and Artifacts"
                    "## Rejection Rules" "## Conformance Criteria"] false)]
  :disposition :preserved
  :obligations [:stage-trusted-inputs :generated-artifacts
                :source-compiler-lock-target-output-ids :expected-deltas
                :tcb-delta :no-stage-advance]
  :binding-specs
  [(B :test13-runtime :runtime :self-hosting-runtime []
      [:stage-trusted-inputs :generated-artifacts
       :source-compiler-lock-target-output-ids :expected-deltas :tcb-delta
       :no-stage-advance])
   (B :test13-evidence :post-i9 :phase-evidence
      [:carriers :test13-result] [:generated-artifacts])]
  :test-ids [:sh07-b51-v9-contract-test13]
  :diagnostic :SH07-B51-PROOF-SCOPE}
 {:ordinal 16 :id :sh07-proof-contract :document-ordinals [24]
  :sections [(S 24 [] true)] :disposition :applies
  :obligations [:active-scope :fresh-process :nonauthoritative-cache
                :source-rechecks :catalog-ids :bounds :cleanup]
  :binding-specs
  [(B :proof-contract-document :pre-evidence :document-read-receipt [:documents 24]
      [:active-scope :nonauthoritative-cache :bounds])
   (B :proof-contract-projection :pre-evidence :proof-scope [:proof-contract]
      [:active-scope])
   (B :proof-contract-runtime :runtime :verification-runtime []
      [:fresh-process :source-rechecks :catalog-ids :cleanup])]
  :test-ids [:sh07-b51-v9-proof-contract-scope]
  :diagnostic :SH07-B51-PROOF-SCOPE}
 {:ordinal 17 :id :backlog-ownership :document-ordinals [25 26]
  :sections [(S 25 ["## Countable Backlog" "## Dependency Waves and Parallel Ownership"
                    "## Progress Accounting" "## Outputs and Artifacts"
                    "## Conformance Criteria"] false)
             (S 26 [] true)]
  :disposition :applies
  :obligations [:partial-progress :allocations :reservations :handoffs
                :open-exit-gates]
  :binding-specs
  [(B :backlog-document :pre-evidence :document-read-receipt [:documents 25]
      [:partial-progress])
   (B :ownership-plan :pre-evidence :ownership-plan []
      [:allocations :open-exit-gates])
   (B :backlog-progress :pre-evidence :progress [] [:partial-progress])
   (B :ownership-i9 :post-i9 :phase-evidence
      [:carriers :ownership-result] [:reservations :handoffs])
   (B :ownership-g9 :post-g9 :phase-evidence
      [:carriers :ownership-result] [:reservations :handoffs])
   (B :ownership-p9 :post-p9 :phase-evidence
      [:carriers :ownership-result] [:reservations :handoffs])]
  :test-ids [:sh07-b51-v9-backlog-ownership]
  :diagnostic :SH07-B51-OWNER}
 {:ordinal 18 :id :catalog-checked-core-governance :document-ordinals [27 28 29 30 31 32]
  :sections [(S 27 [] true) (S 28 [] true)
             (S 29 ["## Lifecycle" "## Admission"] false)
             (S 30 [] true) (S 31 [] true) (S 32 [] true)]
  :disposition :applies
  :obligations [:catalog-schema-authority :checked-core-revision
                :lifecycle-transitions :review-admission-receipts :final-cleanup]
  :binding-specs
  [(B :catalog-document :pre-evidence :document-read-receipt [:documents 27]
      [:catalog-schema-authority])
   (B :catalog-projection :pre-evidence :catalog-projection []
      [:catalog-schema-authority])
   (B :governance-rules :pre-evidence :governance-rules [] [:lifecycle-transitions])
   (B :checked-core-source :runtime :source-session-m [:source-descriptors 2]
      [:checked-core-revision])
   (B :catalog-launcher-cleanup :runtime :launcher [:cleanup] [:final-cleanup])
   (B :governance-i9 :post-i9 :phase-evidence [:carriers :governance-result]
      [:review-admission-receipts])
   (B :governance-g9 :post-g9 :phase-evidence [:carriers :governance-result]
      [:review-admission-receipts])
   (B :governance-p9 :post-p9 :phase-evidence [:carriers :governance-results]
      [:review-admission-receipts :final-cleanup])]
  :test-ids [:sh07-b51-v9-catalog-governance]
  :diagnostic :SH07-B51-VERIFY-CATALOG}]
```

Every plan row has exactly `:ordinal :id :document-ordinals :sections
:disposition :obligations :binding-specs :test-ids :diagnostic`; every section
and binding spec expands exactly as defined above. Its sole plan identity is
ROW-PLAN-ID. For each phase, `RESOLVED-ROWS` contains exactly one map per row
having a binding in that phase, in ascending row ordinal order, with exactly
`:row-ordinal :row-plan-id :phase :resolved-obligations :bindings :status
:diagnostic`. Row-plan ID equals ROW-PLAN-ID; bindings
are in their binding-spec order filtered to that phase and are the exact
RESOLVE outputs; resolved obligations are the row's planned obligations named
by those bindings, in original obligation order, without duplicate. Status is
`:bindings-resolved` only when all phase-local bindings resolve and hash.

Each non-PRE PHASE-EVIDENCE carrier map contains CONTRACT-TEST-RESULTS, a dense
vector. Runtime contains one result for every one of the 19 rows and tests all
`:pre-evidence` plus `:runtime` obligations after M has exited and L's
authenticated data-only contract validator has received the complete closed
carrier pool. Each post phase contains one result for every row with a binding
in that phase and tests only that phase's obligations. PRE contains no
executable test result, so Q reconstruction remains a static C/T calculation
and does not load project test namespaces before the pre-manifest TCB boundary.
No authoritative phase loads a project test namespace.

Each test result has exactly `:row-ordinal :phase :test-id :test-var :test-path
:test-blob-oid :test-byte-count :test-raw-sha256 :input-ids :assertions :status
:evidence-id`. Row/phase equal the containing vector; test ID, Var, and path
equal TEST-ID-MAP; the blob descriptor equals the corresponding PRE
OWNED-C-DESCRIPTOR and is provenance only; its bytes are rehashed after bounded
materialization but are not loaded in the authoritative process; and input
IDs are the selected value IDs for every binding of that row in the tested
phases, in phase then binding order. Assertions are a dense vector of maps with exactly `:ordinal
:obligation :predicate-id :subject-ids :result`; every result is true, every
tested obligation appears at least once, no unplanned obligation appears,
and predicate ID is the exact PREDICATE-SPECS entry for the row, phase, and
obligation, implemented as a closed case in L's authenticated launcher blob
rather than a caller-supplied function. The test Var must exercise the same
case during repository validation but grants no runtime authority. Status is
`:passed` iff every exact predicate relation holds. For an assertion,
`SUBJECTS` is exactly the resolved binding maps for that row and phase whose
literal obligation vector contains the assertion's obligation, in binding
order. `:subject-ids` must equal `(mapv :value-id SUBJECTS)` byte-for-byte;
input IDs must equal the corresponding all-obligation binding vector described
above. A caller-supplied, reordered, omitted, duplicated, or additional subject
ID rejects before predicate evaluation.
Evidence ID is
`H([:gravity/sh07-b51-contract-test-evidence-v1 {:row-plan-id ROW-PLAN-ID
:row-ordinal ROW-ORDINAL :phase PHASE :test-result
TEST-RESULT-WITHOUT-EVIDENCE-ID}])`.

`PREDICATE-SPECS` is the exact dense vector obtained in ROW-PLAN row order,
PHASES order, and each row's obligation order by emitting one map for every
row/phase/obligation triple having at least one binding at that phase whose
obligation vector contains the obligation. Each map has exactly `:ordinal
:row-ordinal :row-id :phase :obligation :program :predicate-id
:required-subject-paths`. Ordinal is the zero-based emitted position; required
subject paths are the exact binding paths for that triple in binding order.
Predicate ID is
`H([:gravity/sh07-b51-contract-predicate-id-v1
{:row-plan-id ROW-PLAN-ID :row-ordinal ROW-ORDINAL :phase PHASE
 :obligation OBLIGATION :program PROGRAM}])`. PROGRAM is computed by the exact
constructor below, never selected by a fixture or implementation.

`PATH` is a dense vector containing only keywords and nonnegative integers.
`ENV` has exactly `:context :subjects :carriers`; context has exactly
`:row-ordinal :row-id :phase :obligation`, subjects is SUBJECTS, and carriers
is the already strictly validated phase carrier pool. AST values are dense
vectors and have this complete grammar:

```text
BOOL := [:and BOOL BOOL+] | [:equal TERM TERM]
      | [:exact-keys TERM [:literal KEYWORD-VECTOR]]
      | [:h-equal [:literal DOMAIN] TERM TERM [:literal OMIT-PATHS]]
      | [:r-equal TERM TERM] | [:every TERM BOOL]
      | [:not BOOL] | [:lte INT-TERM INT-TERM]
TERM := [:lookup SOURCE PATH] | [:literal VALUE] | [:count TERM]
INT-TERM := [:count TERM] | [:literal NONNEGATIVE-INTEGER]
SOURCE := :context | :subjects | :carriers | :item
```

DOMAIN is one literal member of H-DOMAIN-REGISTRY. VALUE is one canonical EDN
value whose complete node/depth/width/scalar-byte census is within FIXED-FUEL;
NONNEGATIVE-INTEGER is an integer from zero through
`Long/MAX_VALUE`. KEYWORD-VECTOR is a dense duplicate-free keyword vector.
OMIT-PATHS is a dense duplicate-free vector of PATH values. Every digest value
accepted or produced by this grammar is exactly the ASCII string `"sha256:"`
followed by 64 lowercase hexadecimal characters.

`:and` evaluates left-to-right, requires at least two boolean children, and
returns their conjunction. `:equal` is Clojure value equality over canonical
EDN values. `:exact-keys` requires a map and returns true iff its key set equals
the literal distinct keyword vector as a set. `:count` accepts only a dense
vector or map and returns its cardinality. `:h-equal` removes exactly the
literal distinct PATH values from its map payload (every path must exist), then
compares H of `[DOMAIN PAYLOAD]` with an exact prefixed digest term.
`:r-equal` compares R of a byte-vector term with an exact prefixed digest term.
`:every` requires a nonempty dense vector, binds `:item` successively to each
element with no nesting or shadowing, and conjoins its boolean body. `:not`
negates one boolean. `:lte` accepts two nonnegative integers. `:lookup` starts
at the named ENV value (or current item), follows every PATH segment with exact
container typing, and rejects missing or nil targets. `:literal` returns its
one canonical-EDN operand. No other node length, source, operator, coercion,
host call, symbol resolution, eval, wildcard, missing-value default, or
default-true case exists.

For a triple, `PROGRAM-FOR(ROW, PHASE, OBLIGATION)` is literally
`[:and CONTEXT-CHECK SUBJECT-CHECK-0 ... SUBJECT-CHECK-(N-1)]`, where N is the
nonzero subject count. CONTEXT-CHECK is literally
`[:and [:equal [:lookup :context [:row-ordinal]] [:literal ROW-ORDINAL]]
[:equal [:lookup :context [:row-id]] [:literal ROW-ID]]
[:equal [:lookup :context [:phase]] [:literal PHASE]]
[:equal [:lookup :context [:obligation]] [:literal OBLIGATION]]]`.
For subject index J and its literal binding spec B, SUBJECT-CHECK-J is
literally:

```clojure
[:and
 [:exact-keys [:lookup :subjects [J]]
  [:literal [:row-ordinal :id :phase :carrier :path :obligations :value :value-id]]]
 [:equal [:lookup :subjects [J :row-ordinal]] [:literal ROW-ORDINAL]]
 [:equal [:lookup :subjects [J :id]] [:literal B-ID]]
 [:equal [:lookup :subjects [J :phase]] [:literal PHASE]]
 [:equal [:lookup :subjects [J :carrier]] [:literal B-CARRIER]]
 [:equal [:lookup :subjects [J :path]] [:literal B-PATH]]
 [:equal [:lookup :subjects [J :obligations]] [:literal B-OBLIGATIONS]]
 [:equal [:lookup :subjects [J :value]]
         [:lookup :carriers [B-CARRIER ...B-PATH]]]
 [:h-equal [:literal :gravity/sh07-b51-contract-binding-v2]
  [:lookup :subjects [J]] [:lookup :subjects [J :value-id]]
  [:literal [[:value-id]]]]]
```

The constructor substitutes only the named literal ROW-PLAN fields and emits
subjects in binding order; it has no obligation-specific branch. Before
constructing ENV, L must run every closed carrier schema, root, ID, cross-field
equation, and phase-exclusion check defined in this report. The program proves
that every obligation is backed by its exact authenticated binding values; the
carrier validators prove those values' substantive contract relations.

The accepted fixture is one strict canonical EDN form plus EOF with exactly
`:schema :row-plan-id :source-descriptors :source-events :predicate-specs
:predicate-spec-id :self-id-omits :fixture-id`. Schema is
`:gravity/sh07-b51-contract-predicate-fixture-v1`; row-plan ID is ROW-PLAN-ID;
source descriptors equal the complete twelve-DESCRIPTOR vector and source
events equal the complete 39-row EVENTS vector, byte-for-byte. Predicate specs
equal the complete freshly
constructed PREDICATE-SPECS byte-for-byte; predicate-spec ID is
`H([:gravity/sh07-b51-contract-predicate-spec-id-v1 PREDICATE-SPECS])`;
self-id-omits is `[[:fixture-id]]`; and fixture ID is
`H([:gravity/sh07-b51-contract-predicate-fixture-v1
FIXTURE-WITHOUT-FIXTURE-ID])`. Its blob descriptor must equal the PRE
OWNED-C-DESCRIPTOR for the literal accepted-fixture path. L's authenticated
launcher contains the same constructor and recomputes the vector, both IDs,
and exact fixture equality before evaluation. An empty subject vector, unknown
or duplicate triple, missing or extra spec, changed ordinal/path/program, bad
ID, ill-typed AST, nonexhaustive constructor, or implementation-selected
program rejects. An implementation-defined always-true predicate therefore
cannot be conforming evidence.

The root for each phase is H of its complete dense row vector under the
corresponding domain in PHASE-ROOT-DOMAINS. Final completeness follows all
four prior refs and strictly decodes the five actual row vectors; phase roots
authenticate those vectors but are never treated as sets. Planned bindings are
enumerated in row order then binding order. `PLANNED-BINDING-COUNT` is exactly
62 and must be recomputed from the literal vector. Actual bindings are
enumerated in PHASES order, row order, then
binding order. The final predicate requires the same PLANNED-BINDING-COUNT
unique binding IDs, each resolved at its declared phase with its exact
row/spec/value ID and no extra; it requires exactly the planned row/phase
pairs, the runtime-all-rows and post-phase test vectors, and every planned test
ID; and for each row the union of resolved obligations in
planned obligation order equals its obligations vector.
`ALL-OBLIGATIONS-RESOLVED` is defined as the conjunction of those exact checks,
not an independently supplied boolean. A valid POST-P9 discharge requires
`:all-obligations-resolved true` and equality to
ALL-OBLIGATIONS-RESOLVED; false or omission rejects. A changed ordinal, path, heading,
whole-document flag, obligation, binding spec, test ID/result, disposition,
diagnostic, row order, phase order, or root rejects.

`TEST-ID-MAP` is exact and maps every row test ID above to the common path
`bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_contract_discharge_test.clj`
and these distinct test Vars, in row order:

```clojure
[[ :sh07-b51-v9-contract-agents contract-agents]
 [ :sh07-b51-v9-contract-foundation contract-foundation]
 [ :sh07-b51-v9-contract-l1 contract-l1]
 [ :sh07-b51-v9-contract-l2 contract-l2]
 [ :sh07-b51-v9-contract-c2 contract-c2]
 [ :sh07-b51-v9-contract-c6 contract-c6]
 [ :sh07-b51-v9-contract-c11-nonclaim contract-c11-nonclaim]
 [ :sh07-b51-v9-contract-boot1 contract-boot1]
 [ :sh07-b51-v9-contract-boot2 contract-boot2]
 [ :sh07-b51-v9-contract-boot3 contract-boot3]
 [ :sh07-b51-v9-contract-boot4 contract-boot4]
 [ :sh07-b51-v9-contract-boot5 contract-boot5]
 [ :sh07-b51-v9-contract-boot6-8 contract-boot6-8]
 [ :sh07-b51-v9-contract-test10 contract-test10]
 [ :sh07-b51-v9-contract-test11 contract-test11]
 [ :sh07-b51-v9-contract-test13 contract-test13]
 [ :sh07-b51-v9-proof-contract-scope proof-contract-scope]
 [ :sh07-b51-v9-backlog-ownership backlog-ownership]
 [ :sh07-b51-v9-catalog-governance catalog-governance]]
```

Each vector row is exactly `[TEST-ID TEST-VAR]`; the path is not caller
selectable. The implementation test must execute all 19 Vars and mutation
fixtures must prove that every row field changes the row and event roots.

Proof scope is exactly `{:evidence-kind :validation :formal-proof false
:candidate-scope :sh07-b51-git-object-publication :lowering-boundary
:verified-core :mir-artifacts [] :backend-artifacts [] :stage-support-claims []
:proof-contract
{:artifact :gravity/sh07-proof-process-contract :schema-version 2
:status :active :coverage-milestone "SH-07-B47" :task "SH-07-B47"
:input-task "SH-07-B15" :scope :sh07-b15-keyword-map-lookup
:application :preserved-out-of-scope} :verification-catalog
CATALOG-PROJECTION :adopted-independent-controls
[:fresh-authoritative-process :immutable-source-snapshot :immutable-receipts
:bounded-execution :finally-cleanup] :b51-proof-contract-revision-required
true}`. This explicitly prevents the B47/B15 contract or catalog from being
misrepresented as B5.1 formal-proof authority.

`CATALOG-PROJECTION` has exactly `:schema :version :authority :authoritative?
:source-descriptor :module :applicable-routes`. The first four values equal the
strictly parsed C catalog's top-level fields; source descriptor is document 27
from `document-read-receipt`; module is the complete value at
`[:modules "sh07-checked-core"]`; and applicable routes is the exact vector of
`[ID ROUTE]` pairs for these literal IDs, with each ROUTE the complete value at
`[:routes ID]`:

```clojure
["sh07-authoritative" "sh07-b10-regression" "sh07-b11-contract"
 "sh07-b13-accepted" "sh07-b8-regression" "sh07-cached-accepted"
 "sh07-cached-contract" "sh07-cached-identity" "sh07-cached-rejected"
 "sh07-cached-verification" "sh07-conformance-parity"
 "sh07-conformance-table" "sh07-parallel-accepted"
 "sh07-parallel-b12-contract-accepted"
 "sh07-parallel-b12-identity-verification"
 "sh07-parallel-b12-rejected-regression"
 "sh07-parallel-identity-lineage" "sh07-parallel-regression-a"
 "sh07-parallel-regression-b" "sh07-parallel-rejected"
 "sh07-parallel-verification"]
```

The ID vector is unsigned-UTF-8 sorted and equals the complete set of catalog
routes whose `:module-id` is `"sh07-checked-core"`; a missing, extra, reordered,
or partial route record rejects. This binds catalog schema/version/authority,
the literal module, every applicable route, and exact C source bytes without
inventing a catalog proof root.

`FOUNDATION-RUNTIME` has exactly `:lowering-product-ids :pass-records
:fact-records :provenance-records :pass-root :fact-root :provenance-root
:replay :invalidation`. A and B each construct a `LOWERING-PRODUCT` before any
certificate, checker output, staged payload, or semantic witness exists. It has
exactly `:schema :content :source-authority :module-products :pass-records
:fact-records :provenance-records :fuel-subjects :semantic-roots :product-id`.
Content is C/T;
source authority has exactly `:source-descriptors-root :event-root` and is the
role-neutral value common to A and B. Module products are the seven dense
pre-certificate lowering results with exactly `:ordinal :module-id :source-id
:core-root :diagnostic-root :effect-root :capability-root`.

For MODULE-TRANSACTIONS row M at ordinal i, `SOURCE-ID_i` is
`M[:authenticated-core-request :lineage :sh06-semantic-projection-id]`. The
four component roots are exactly:

```clojure
CORE-ROOT_i =
  MR(:lowering-modules,
     [[:core i SOURCE-ID_i M[:module-core]]])
DIAGNOSTIC-ROOT_i =
  MR(:lowering-modules,
     [[:diagnostics i SOURCE-ID_i
       M[:resolved-core-carrier :diagnostics]]])
EFFECT-ROOT_i =
  MR(:lowering-modules,
     [[:effects i SOURCE-ID_i
       M[:authenticated-core-request :module :effects]]])
CAPABILITY-ROOT_i =
  MR(:lowering-modules,
     [[:capabilities i SOURCE-ID_i
       M[:authenticated-core-request :module :capabilities]]])
```

`MODULE-PRODUCT_i` is the exact row
`{:ordinal i :module-id MODULE-ID_i :source-id SOURCE-ID_i :core-root
CORE-ROOT_i :diagnostic-root DIAGNOSTIC-ROOT_i :effect-root EFFECT-ROOT_i
:capability-root CAPABILITY-ROOT_i}`. `MODULE-ID_i` is
`MID(:lowering-modules, dissoc(MODULE-PRODUCT_i, :module-id))`.
MODULE-PRODUCTS is the seven rows in module ordinal order. A free root, missing
row, changed order, or root computed from a later witness rejects.

For each module product P, FOUNDATION-FACT-ROWS(P) is the following four-row
vector in printed order, where each row's `:id` is
`MID(:lowering-facts, ROW-WITHOUT-ID)`:

```clojure
[{:id CORE-FACT-ID :kind :core
  :subject-id P[:module-id] :value-id P[:core-root]}
 {:id DIAGNOSTIC-FACT-ID :kind :diagnostic
  :subject-id P[:module-id] :value-id P[:diagnostic-root]}
 {:id EFFECT-FACT-ID :kind :effect
  :subject-id P[:module-id] :value-id P[:effect-root]}
 {:id CAPABILITY-FACT-ID :kind :capability
  :subject-id P[:module-id] :value-id P[:capability-root]}]
```

FOUNDATION-PROVENANCE-ROWS(P) has four rows in the same kind/root order. Each
row has exactly `:id :producer-id :input-ids :output-id`; producer ID is
SEMANTIC-ALGORITHM-SOURCE-ROOT, input IDs are exactly
`[P[:source-id] P[:module-id]]`, output ID is the corresponding component
root, and ID is `MID(:lowering-provenance, ROW-WITHOUT-ID)`. FACT-RECORDS and
PROVENANCE-RECORDS are the dense module-order concatenations of those vectors.
Their roots are exactly the FOUNDATION-FACT-ROOT and
FOUNDATION-PROVENANCE-ROOT equations below.

PASS-RECORDS has one row per module product in ordinal order. Row i has exactly
`:ordinal :pass-id :input-id :output-id :invalidates :preserves :fact-root
:provenance-root`; ordinal is i, input ID is P `:source-id`, output ID is P
`:core-root`, invalidates is `[]`, preserves is exactly
`[P[:diagnostic-root] P[:effect-root] P[:capability-root]]`, fact/provenance
roots are the complete roots above, and pass ID is
`MID(:lowering-passes, ROW-WITHOUT-PASS-ID)`. PASS-ROOT is the exact
FOUNDATION-PASS-ROOT equation below.

`FOUNDATION-PREPRODUCT` is constructed before LOWERING-PRODUCT and is exactly
`{:pass-records PASS-RECORDS :fact-records FACT-RECORDS :provenance-records
PROVENANCE-RECORDS :pass-root PASS-ROOT :fact-root FACT-ROOT :provenance-root
PROVENANCE-ROOT}`. It is the sole producer of those six BUILD-PRE-STAGES input
values. The complete later FOUNDATION-RUNTIME must carry those six values
byte-for-byte and may only add its separately governed product/replay/
invalidation fields.

Semantic roots has
exactly `:modules :passes :facts :provenance`; each is MR of its literal kind
and exact vector. Product ID is
`H([:gravity/sh07-b51-lowering-product-v1 PRODUCT-WITHOUT-ID])`. Role-specific
source-session IDs remain only in the enclosing child receipts and join to the
same source-authority roots. Fuel subjects are the exact seven role-neutral,
receipt-free intermediate carrier projections defined in the fixed-fuel
section; each is produced during lowering before product ID construction. The
product contains no operation ID, fuel receipt, checker certificate,
witness, checker-output value ID, E9 descriptor, or later object. The two
independently constructed product values and product IDs are byte-equal.

Pass records are constructed only from the pre-certificate module products as
specified above and have
exactly `:ordinal :pass-id :input-id :output-id :invalidates :preserves
:fact-root :provenance-root`. Fact records have exactly `:id :kind :subject-id
:value-id`; provenance records have exactly `:id :producer-id :input-ids
:output-id`. Pass, fact, and provenance roots are respectively
`H([:gravity/sh07-b51-foundation-pass-root-v1 PASS-RECORDS])`,
`H([:gravity/sh07-b51-foundation-fact-root-v1 FACT-RECORDS])`, and
`H([:gravity/sh07-b51-foundation-provenance-root-v1 PROVENANCE-RECORDS])`.
A/B must independently produce byte-equal record vectors and roots before
certificate construction. No projection from a certificate is permitted.
Replay has
exactly `:inputs :replay-id`; invalidation has exactly `:inputs :status
:invalidation-id`, status `:fresh`. Their IDs are respectively
`H([:gravity/sh07-b51-runtime-replay-v1 INPUTS])` and
`H([:gravity/sh07-b51-runtime-invalidation-v1 INPUTS])`; inputs bind C/T,
source/session roots, architecture SHA, both checker IDs, and document root.

`BOOTSTRAP-RUNTIME` has exactly `:launcher-id :coordinator-id
:checker-process-ids :source-session-ids :stage-comparison :tcb-delta
:provenance-receipts :cleanup :replay`. Process and session IDs are the
normalized exact L/M/A/B values. Stage comparison has exactly `:left :right
:observables :equal :accepted-deltas :comparison-id`; left/right bind A/B
oracle and artifact IDs, observables are `[:normalized-census-id :certificate
:diagnostics :source-session :payload-wire-bytes]`, equal is true, deltas are
empty, and comparison ID is
`H([:gravity/sh07-b51-runtime-stage-comparison-v1
STAGE-COMPARISON-WITHOUT-ID])`. TCB delta has exactly `:before-manifest-ids :after-manifest-ids
:observed-differences :allowed-differences :unexpected-differences :delta-id`;
before is L, after is `[M A B]`, observed equals allowed, unexpected is empty,
and ID hashes the other fields under
`:gravity/sh07-b51-runtime-tcb-delta-v1`. Provenance receipts are exact
normalized L, M, A, B receipt IDs; cleanup is normalized launcher cleanup.
Replay has exactly `:inputs :status`; inputs equal FOUNDATION-RUNTIME replay
inputs and status is `:required-post-e9` until the post-I9 phase resolves it.

`DIFFERENTIAL-RUNTIME` has exactly `:oracles :execution-context
:compared-observables :comparison-report :diagnostic-comparison
:permitted-divergences :reproducers :status`. Oracles are the dense vector of
four maps having exactly `:ordinal :role :oracle-id :applicability :basis`:
A and B are `:executed`; main is `:preservation-baseline` bound to base
`09c795f0c0854d2d8ab542c92b2116515e096406`; I9 is
`:candidate-pre-integration` bound to C/T. No main/I9 execution claim is made
until POST-I9 resolves the preservation and replay binding. Execution context
is exactly `{:profile :meta :target :verified-core :backend :not-applicable
:runtime [A-RUNTIME-ID B-RUNTIME-ID M-RUNTIME-ID] :numeric-mode
:not-applicable :not-applicable-reasons {:backend :verified-core-boundary
:numeric-mode :no-numeric-observable}}`. Compared observables equal the stage
comparison observables. Comparison report binds the A/B equal report;
diagnostic comparison is exactly `{:left A-DIAGNOSTICS :right B-DIAGNOSTICS
:equal true}`; permitted divergences are empty; reproducers equal TEST-ID-MAP;
status is `:passed-runtime-scope`.

`VERIFICATION-RUNTIME` has exactly `:evidence-kind :formal-proof :assumptions
:trusted-basis :checkers :input-ids :claim-ids :source-rechecks :catalog-ids
:invalidation :counterexamples :counterexample-status :cleanup :status`. Evidence kind is `:validation`; formal proof
is false. Assumptions are the exact ordered A/B certificate assumption maps;
trusted basis binds CODEC-TCB, classpath root, Q coordinate, C/T, and document
root; checkers are the complete A/B checker identity/version records; input and
claim IDs are the exact certificate values. Invalidation has exactly
`:trigger-ids :status :report-id`; triggers are source root, compiler ID,
document root, architecture SHA, and A/B checker IDs, status is `:fresh`, and
report ID is
`H([:gravity/sh07-b51-verification-invalidation-v1
INVALIDATION-WITHOUT-REPORT-ID])`. Counterexamples are empty only for the
accepted run, counterexample status is `:no-formal-proof-claim`, and status is
`:validated`. Source rechecks are the exact A/B/M descriptor/event roots;
catalog IDs are the CATALOG-PROJECTION source and route value IDs; cleanup is
the normalized launcher cleanup. Rejected mutation fixtures must populate a nonempty bounded
counterexample vector and may never convert validation into formal proof.

`SELF-HOSTING-RUNTIME` has exactly `:stage :stage-manifests :trusted-inputs
:generated-artifact-value-ids :identity-receipt-object-disposition :identities
:expected-integration-preservation :integration-observation-status :tcb-delta
:runtime-diagnostics-equal :runtime-artifact-schemas-valid :stage-advanced`.
Stage is
`:sh07-b51-verified-core-validation`; manifests are BOOT5-COMPATIBILITY and the
three normalized source/TCB manifests; trusted inputs bind C/T, Q, and
BOOTSTRAP-RUNTIME; generated IDs are the six payload value IDs; containing
identity-receipt object disposition is `:deferred-post-i9`. Identities have
exactly `:source-id :compiler-id :dependency-lock-id :target-id :output-id`;
dependency lock is the classpath manifest root, target ID is
`H([:gravity/sh07-b51-verified-core-target-id-v1 :verified-core])`, and output
ID is `H([:gravity/sh07-b51-runtime-output-root-v1
SIX-PAYLOAD-VALUE-IDS])`. Expected integration preservation has exactly
`:relation :expected-preserved-c-paths :allowed-integration-changes`; relation
is `:future-c-to-b`, expected paths are OWNED-C-PATHS, and allowed changes are
empty. Integration observation status is `:deferred-post-i9`. This map contains
no B/TB, integration receipt, observed or unexpected delta, preservation
result, or identity-receipt object. TCB delta is BOOTSTRAP-RUNTIME's exact
value; both runtime booleans are true; stage-advanced is false. Actual
preservation and deltas exist only in TEST13-POST-I9-RESULT.

`SEMANTIC-WITNESS-RUNTIME` is not a document-presence assertion. Checker A and
checker B each independently construct the same semantic witness from their
own immutable C source session and authentic fixed-fuel lowering products; the
common certificate embeds that complete witness. M requires byte equality of
the two witness values before constructing the runtime wrapper. The wrapper
has exactly `:schema :content :checker-ids :witness :equal
:runtime-witness-id`; schema is `:gravity/sh07-b51-semantic-witness-runtime-v1`,
content is C/T, checker IDs are `[A-CHECKER-ID B-CHECKER-ID]`, equal is true,
and runtime witness ID is
`H([:gravity/sh07-b51-semantic-witness-runtime-v1
WRAPPER-WITHOUT-RUNTIME-WITNESS-ID])`.

The nested `WITNESS` has exactly `:schema :content :operation-manifest
:operation-schedule-id
:fuel-receipts :l1 :l2 :c2 :c6 :boot2 :boot3 :boot4 :witness-id`; schema is
`:gravity/sh07-b51-authentic-lowering-witness-v1`, content is C/T, and witness
ID H-hashes the map without itself under that schema domain. Every vector is
dense and in source/evaluation order. `MR(KIND, ENTRIES)` is normative shorthand
for exactly `H([:gravity/sh07-b51-semantic-witness-member-root-v1
{:kind KIND :entries ENTRIES}])`; every literal KIND used below occurs exactly
once in H-DOMAIN-REGISTRY's member-kind set. Each nested witness ID H-hashes its complete
map without that ID under its literal schema domain. A/B must derive the same
bytes independently; a caller-provided witness, document-only projection,
top-level scan substituted for the authentic owning defn/root, or unbounded
execution rejects.

`MID(KIND, ENTRY)` is normative shorthand for exactly
`H([:gravity/sh07-b51-semantic-witness-member-id-v1
{:kind KIND :entry ENTRY}])`. Every semantic-witness entry field ending in
`-id` which identifies that entry equals `MID(KIND, ENTRY-WITH-THAT-ID-OMITTED)`,
where KIND is the literal member kind of the enclosing entries vector. Every
other nested `-id` is a typed reference: it equals the already recomputed ID
of exactly one entry in the named enclosing or upstream vector and is never
hashed independently. `:source-id`, `:compiler-id`, `:plan-id`,
`:manifest-id`, `:artifact-id`, `:projection-id`, `:receipt-id`,
`:operation-id`, and the root fields retain their separately named equations
and are not MID values, except BOOT3 pre-stage artifact IDs, which use the
explicit `AD` MID equation below. C6 AST `:node-id`, `:children`, and
syntax-core `:core-id` are typed upstream resolved checked-core IDs and are
also governed by their explicit exception below. Parent/child references may target only an earlier
source/evaluation-order entry except an explicit child vector, whose child IDs
must already have been computed by postorder traversal. A missing target,
duplicate ID, forward parent edge, non-postorder child edge, reference cycle,
or a supplied ID unequal to its equation rejects. This rule closes the
`syntax-id`, `span-id`, `origin-id`, `metadata-id`, `token-id`,
`form-id`, `literal-id`, `reader-extension-id`, `diagnostic-id`,
`defn-id`, `declared-body-root-id`, `binding-id`, `edge-id`,
fact, pass, output, test, and stage-comparison member identities named below.

`FIXED-FUEL` is the complete `:bounds` map from the strictly decoded C proof
contract, never a caller-provided integer.
`FIXED-FUEL-EXECUTABLE-ALIASES` is literally:

```clojure
{:maximum-module-forms :maximum-forms
 :maximum-fragment-resolutions :maximum-resolutions
 :maximum-module-carrier-nodes :maximum-carrier-nodes
 :maximum-module-carrier-depth :maximum-carrier-depth
 :maximum-module-carrier-width :maximum-carrier-width
 :maximum-module-scalar-bytes :maximum-scalar-bytes
 :maximum-module-digest-requests :maximum-digest-requests}
```

Before any module transaction, each proof-contract key is replaced by its
alias above when present and otherwise retained byte-for-byte. The resulting
`FIXED-FUEL-EXECUTABLE` map must have exactly 34 distinct keys and equal the
same 34-key projection of the freshly evaluated C checked-core
`sh07-bounds-value` map. The remaining checked-core bounds are out of this
proof-contract projection and cannot substitute for, mask, or weaken one of
the 34 entries. A missing, extra, colliding, renamed, or numerically unequal
entry rejects before any subject or operation is constructed.

`FIXED-FUEL-OPERATION-SPECS` is literally:

```clojure
[{:ordinal 0 :owner :l1 :kind :l1-source-syntax-walk
  :subject-id L1-FUEL-SUBJECT-ID
  :counter-keys [:maximum-module-forms :maximum-top-level-forms
                 :maximum-fragment-forms :maximum-form-children
                 :maximum-form-depth :maximum-origin-entries
                 :maximum-metadata-entries :maximum-alias-records]}
 {:ordinal 1 :owner :c2 :kind :c2-token-form-source-map-walk
  :subject-id C2-FUEL-SUBJECT-ID
  :counter-keys [:maximum-module-carrier-nodes :maximum-module-carrier-depth
                 :maximum-module-carrier-width :maximum-module-scalar-bytes]}
 {:ordinal 2 :owner :l2 :kind :l2-owner-effect-capability-walk
  :subject-id L2-FUEL-SUBJECT-ID
  :counter-keys [:maximum-fragments :maximum-module-bindings :maximum-bindings
                 :maximum-fragment-resolutions :maximum-module-resolutions
                 :maximum-effects :maximum-capabilities :maximum-exports]}
 {:ordinal 3 :owner :c6 :kind :c6-core-lowering-walk
  :subject-id C6-FUEL-SUBJECT-ID
  :counter-keys [:maximum-module-core-nodes
                 :maximum-resolved-core-carrier-nodes
                 :maximum-resolved-core-carrier-depth
                 :maximum-resolved-core-carrier-width
                 :maximum-resolved-core-scalar-bytes]}
 {:ordinal 4 :owner :boot2 :kind :boot2-template-carrier-walk
  :subject-id BOOT2-FUEL-SUBJECT-ID
  :counter-keys [:maximum-template-carrier-nodes
                 :maximum-template-carrier-depth
                 :maximum-template-carrier-width
                 :maximum-template-scalar-bytes]}
 {:ordinal 5 :owner :boot3 :kind :boot3-generated-digest-walk
  :subject-id BOOT3-FUEL-SUBJECT-ID
  :counter-keys [:maximum-generated-digest-carrier-nodes
                 :maximum-generated-digest-carrier-depth
                 :maximum-generated-digest-carrier-width
                 :maximum-generated-digest-scalar-bytes]}
 {:ordinal 6 :owner :boot4 :kind :boot4-module-digest-walk
  :subject-id BOOT4-FUEL-SUBJECT-ID
 :counter-keys [:maximum-module-digest-requests]}]
```

`SEMANTIC-ALGORITHM-SOURCE-PATHS` is the exact ordered vector:

```clojure
["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"]
```

`SEMANTIC-ALGORITHM-SOURCE-DESCRIPTORS` is the exact projection of
OWNED-C-DESCRIPTORS at those paths, preserving this order; each descriptor and
blob is independently revalidated by A and B from C through their 77-row PRE
object plan. The role-neutral authority root is
`SEMANTIC-ALGORITHM-SOURCE-ROOT =
H([:gravity/sh07-b51-semantic-algorithm-source-root-v1
SEMANTIC-ALGORITHM-SOURCE-DESCRIPTORS])`. It contains no role, loaded-namespace
set, A/B checker identity, process receipt, host path, or role-specific source
closure. Both children derive it from common C objects rather than accepting
it from the launcher; M independently recomputes it before witness comparison.
Role-specific SOURCE-CLOSURE roots remain only in TCB and provenance receipts.

`FUEL-SOURCE-ROOTS` has exactly `:source-descriptors-root :event-root
:semantic-algorithm-source-root :sh06-authority-carrier-id :bundle-core-id`.
Its first two values are the common role-neutral roots in LOWERING-PRODUCT
`:source-authority`, its algorithm value is SEMANTIC-ALGORITHM-SOURCE-ROOT, and
its final two values are the independently reconstructed
SH06-AUTHORITY-CARRIER `:carrier-id` and authenticated BUNDLE-CORE ID. The SH06
carrier is constructed and verified from source ordinals 3-9 before any fuel
subject; it contains no operation, receipt, stage witness, or certificate.
`STAGE-SEMANTIC-SPECS` is the following exact dense vector:

```clojure
[{:ordinal 0 :owner :l1 :stage-key :l1
  :projection {:kind :exact-keys
               :keys [:document :source-units :grammar-change]}}
 {:ordinal 1 :owner :c2 :stage-key :c2
  :projection {:kind :exact-keys
               :keys [:snapshot :units :source-map :diagnostics
                      :literal-records :reader-extensions]}}
 {:ordinal 2 :owner :l2 :stage-key :l2
  :projection {:kind :exact-keys
               :keys [:owner-selection :core-forms :evaluation-order-root
                      :effects-root :capabilities-root]}}
 {:ordinal 3 :owner :c6 :stage-key :c6
  :projection {:kind :exact-keys
               :keys [:ast :syntax-core-map :desugaring-trace
                      :origins-facts-order]}}
 {:ordinal 4 :owner :boot2 :stage-key :boot2
  :projection {:kind :exact-keys
               :keys [:source :compiler :backend :dependencies
                      :runtime-assumptions]}}
 {:ordinal 5 :owner :boot3 :stage-key :boot3
  :projection {:kind :boot3-module-exact-keys
               :module-keys [:ordinal :module :input-artifacts
                             :output-artifacts :effects :facts]
               :keys [:modules :profile :stage-advanced]}}
 {:ordinal 6 :owner :boot4 :stage-key :boot4
  :projection {:kind :exact-keys
               :keys [:module-manifest :effects :capabilities
                      :fact-provenance]}}]
```

For an `:exact-keys` row J and authenticated receipt-free stage prefix S,
`PROJECT-STAGE-SEMANTIC(J, S)` constructs a map containing exactly J's
`:keys`, with values selected from S in printed order; a missing key rejects.
For the sole `:boot3-module-exact-keys` row, it constructs exactly
`{:modules MODULES :profile (:profile S) :stage-advanced
(:stage-advanced S)}`, where MODULES preserves S `:modules` order and replaces
each module by a map containing exactly J `:module-keys` with the corresponding
values in printed order. Additional keys in a final BOOT3 module are ignored
by this projection, while a missing named key rejects. Separately, every
authenticated receipt-free BOOT3 pre-stage module has exactly the six printed
module keys, so it cannot smuggle a later field into the pre-product root.
These are the only projection grammars.

`SOURCE-SEMANTIC-CARRIER` is assembled after the complete C source session and
after MODULE-TRANSACTIONS has produced its seven authenticated C2 reader
artifacts, but before LOWERING-PRODUCT. It has exactly
`:source-descriptors :source-events :l1-document :l1-source-units
:l1-grammar-change :c2-snapshot :c2-units :c2-source-map :c2-diagnostics
:c2-literal-records :c2-reader-extensions`. Source descriptors and events are
the complete authenticated 12-row and 39-row vectors, not their one-way roots.
The remaining values are the exact receipt-free values specified by the L1
and C2 witness schemas below, with every nested ID and MR root recomputed from
the descriptor Git blobs and strict decode events. L1 source units and C2 units
both enumerate source ordinals 2 through 11. For ordinals 3 through 9, each C2
unit's reader value must join byte-for-byte to the matching
MODULE-TRANSACTIONS `:c2-reader-artifact`; ordinals 2, 10, and 11 are built and
verified by the same strict C2 reader directly from their descriptor objects
but do not become lowering module transactions.

`PROJECT-ROOT-ID` is the registered C/T-bound H identity above. For every
module transaction M it must equal both
`M[:c2-reader-artifact :source-unit-record :project-root]` and
`M[:c2-reader-artifact :source-unit-record :identity-inputs
:project-root-id]`; the source-unit `:project-root-record` must be exactly
`{:path nil :project-root-id PROJECT-ROOT-ID}`. A missing or differing value
rejects. For source ordinal N, `PROJECT-CONTEXT_N` is exactly
`{:project-root-id PROJECT-ROOT-ID :project-root-path nil
:project-relative-path SOURCE-DESCRIPTORS[N][:path]}`. The C2 reader for
ordinals 2, 10, and 11 is invoked with this explicit context and the descriptor
blob bytes, never with a host path or an inferred worktree root. All ten C2
source-unit projections omit `:project-root-record` and any canonical or
supplied path.

Let `A_N` be the authentic C2 artifact: for N 3 through 9 it is exactly
`MODULE-TRANSACTIONS[N-3][:c2-reader-artifact]`; for N 2, 10, and 11 it is the
direct strict-reader result above. Let `D_N = SOURCE-DESCRIPTORS[N]`,
`U_N = A_N[:source-unit-record]`, and `I_N = U_N[:identity-inputs]`.
`UNIT-DESCRIPTOR_N` is exactly:

```clojure
{:path D_N[:path]
 :extension U_N[:extension]
 :project-relative-path U_N[:project-relative-path]
 :encoding I_N[:encoding]
 :source-id U_N[:source-id]
 :bytes-hash U_N[:bytes-hash]
 :project-root U_N[:project-root]
 :reader-options U_N[:reader-options]
 :features U_N[:enabled-features]
 :source-kind U_N[:source-kind]}
```

D path, U project-relative path, and I project-relative path must be equal;
U bytes hash must equal D raw SHA; project root must equal PROJECT-ROOT-ID.
`PN-SPAN(S)` removes only S `:source` when present. `PN-TOKEN(T)` removes only
`:source-path` and applies PN-SPAN to `:span`. `PN-FORM(F)` removes only
`:source-path`, applies PN-SPAN to `:span` and `:surface-span`, removes only
`:source-path` from `:origin`, and applies PN-SPAN to every generated-origin
`:from` span. `PN-SEED(S)` applies PN-SPAN to `:span` and, when present, to
each generated-origin `:from`. `PN-LITERAL(L)` applies PN-SPAN only to
`:span`. `PN-EXTENSION(E)` is the exact
`gravity.c2-artifact-identity/extension-hash-input` row projection: it removes
`:source-path`, removes `:source` and `:file` only from its direct or nested
invocation spans, and changes nothing else. `PN-DIAGNOSTIC(D)` is the exact
`diagnostic-hash-input` row projection: it applies PN-SPAN to `:source-span`,
primary span, related spans, and origin-chain spans and removes only an
origin-chain `:path`. No field other than these named host/path members is
removed, renamed, synthesized, or sorted.

For N, `C2-TOKEN-ENTRIES_N = (mapv PN-TOKEN A_N[:token-stream])` and
`C2-TOKENS_N = {:entries C2-TOKEN-ENTRIES_N
:root MR(:c2-tokens, C2-TOKEN-ENTRIES_N)}`. Likewise,
`C2-FORM-ENTRIES_N = (mapv PN-FORM A_N[:form-tree])` and
`C2-FORMS_N = {:entries C2-FORM-ENTRIES_N
:root MR(:c2-forms, C2-FORM-ENTRIES_N)}`.
`C2-SOURCE-MAP-ENTRIES_N` is exactly the `mapv` projection of
`A_N[:reader-source-map]` that applies PN-FORM's path/span operations
(equivalently, changes each row only by applying PN-SPAN to `:span`), and
`C2-SOURCE-MAP_N` is exactly
`{:entries C2-SOURCE-MAP-ENTRIES_N
:root MR(:c2-source-map, C2-SOURCE-MAP-ENTRIES_N)}`.
`C2-DIAGNOSTIC-ENTRIES_N = (mapv PN-DIAGNOSTIC
A_N[:reader-diagnostics])` and `C2-DIAGNOSTICS_N` is exactly
`{:entries C2-DIAGNOSTIC-ENTRIES_N
:root MR(:c2-diagnostics, C2-DIAGNOSTIC-ENTRIES_N)}`.
Literal records and reader extensions are exactly `mapv PN-LITERAL` over
`:literal-decoding-records` and `mapv PN-EXTENSION` over
`:reader-extension-invocation-records`.

`C2-UNIT_N` has exactly `:ordinal` plus the ten UNIT-DESCRIPTOR keys and
`:tokens :forms :source-map :diagnostics :literal-records
:reader-extensions`; ordinal is N and values are the exact projections above.
`C2-UNITS` is `[C2-UNIT_2 ... C2-UNIT_11]`. `C2-SOURCE-MAP`,
`C2-DIAGNOSTICS`, `C2-LITERAL-RECORDS`, and `C2-READER-EXTENSIONS` are the
dense N=2..11 concatenations of the corresponding per-unit entries; the first
two have their distinct entries/MR-root maps and the latter two are exact
vectors. `C2-SNAPSHOT` is exactly `{:commit C :tree T
:source-descriptors-root SOURCE-DESCRIPTORS-ROOT :event-root
SOURCE-EVENT-ROOT}`.

`L1-UNIT_N` has exactly `:ordinal :descriptor :source-order :syntax`.
Descriptor is UNIT-DESCRIPTOR_N; source order is
`mapv :form-id A_N[:syntax-seed-stream]`; syntax is exactly
`{:entries (mapv PN-SEED A_N[:syntax-seed-stream])
:root MR(:l1-syntax, ENTRIES)}`. `L1-SOURCE-UNITS` is the same N=2..11 order,
and `L1-DOCUMENT` is exactly SOURCE-DESCRIPTORS[0]. The complete
SOURCE-SEMANTIC-CARRIER is then the exact eleven-key map named above with
these values, SOURCE-DESCRIPTORS, SOURCE-EVENTS, and the exact
L1-GRAMMAR-CHANGE below. No nested ID, root, source unit, or aggregate may be
caller supplied. Thus the two one-way roots in
FUEL-SOURCE-ROOTS are checks of this full carrier and are never used as a
substitute for it.

The remaining receipt-free stage values are closed by the following exact
construction. `QFORM(Q, ID)` is the unique row of `Q[:forms]` whose
`:form-id` is ID. `RCNODE(RC, F)` is the unique row of `RC[:nodes]` whose
`[:source :form-id]` equals `F[:form-id]`. `TOP(Q, F)` follows
`:parent-form-id` through QFORM until `nil` and returns that final complete form
row; every link must exist, the walk must be acyclic, and the returned row's
form ID must occur exactly once in `Q[:top-level-form-ids]`. `TOPIDX(Q, F)` is
the zero-based position of that form ID in the latter vector. `EIDX(RC, N)` is the unique zero-based position of an
`RC[:evaluation-order]` row whose `:core-node-id` equals `N[:node-id]`.
Evaluation-order node IDs must be a permutation of RC node IDs without a
missing or duplicate value. These joins are recomputed rather than supplied.

`EXECUTABLE-TOP-LEVEL-FORMS(Q)` is
`(mapv #(QFORM Q %) Q[:top-level-form-ids])` in exact source order. The
authenticated `sh07-core-authenticated-request` construction removes the
source `(ns ...)` syntax before building Q trees/forms, so no namespace root or
descendant occurs in Q. For these seven governed modules, every remaining
source root is a defn expansion. Therefore the vector count equals the count
of `Q[:macro-origin-traces]`, and every Fdef has exactly one trace whose
`:output-def-syntax-id = Fdef[:syntax-id]` and `:macro = 'defn`; conversely
every such trace selects exactly one Fdef. Any namespace or other untraced
top-level form in Q rejects rather than being filtered.

`OWNER-REGISTRY` is constructed in module order and then
EXECUTABLE-TOP-LEVEL-FORMS source order. For every Fdef, X is the unique
`Q[:macro-origin-traces]` row whose `:output-def-syntax-id` equals
`Fdef[:syntax-id]`; Ffn is the unique Q form whose syntax ID equals
`X[:introduced-fn-syntax-id]`; FR is the unique RC function record whose
function form/syntax IDs equal Ffn's IDs and whose `:definition-kind` is
`:named-top-level`; and DR is the unique RC definition joined by FR's
definition binding/core IDs. Missing or multiply matching rows reject. The
registry base is exactly:

```clojure
{:ordinal GLOBAL-DENSE-ORDINAL
 :source-id Q[:lineage :sh06-semantic-projection-id]
 :module-ordinal i
 :top-level-form-ordinal t
 :declared-name FR[:definition-name]
 :arity FR[:fixed-arity]
 :defn-id FR[:definition-core-node-id]
 :sh05-expanded-def-id X[:output-def-syntax-id]
 :sh05-expanded-fn-id X[:introduced-fn-syntax-id]
 :declared-body-root-id FR[:body-core-node-id]}
```

The row adds only `:registry-id`, equal to
`MID(:l2-owner-registry, REGISTRY-BASE)`. FR's definition core ID must equal
DR's core node ID, FR's definition binding ID must equal DR's binding ID, and
FR's body core node must be the sole child of the corresponding function node.
Every Q form must reach exactly one registry row through TOPIDX. The
namespace form was removed before Q existed and cannot enter this census. This
admits the authentic owning top-level defn and rejects nested same-named forms,
an untraced top-level root, or a scan-first substitute.

For each ROOT-SPECS row in root-ordinal order, candidates are exactly the
OWNER-REGISTRY IDs whose module ordinal, declared name, and arity equal that
row. Exactly one candidate is required. Its selection is exactly
`{:root-spec SPEC :registry-id RID :defn-id DID
:declared-body-root-id BID}` using the selected registry row, and rejected is
`[]`; zero or multiple candidates rejects rather than selecting by scan order.
`L2-OWNER-SELECTION` is exactly
`{:root-specs ROOT-SPECS :root-specs-source-id ROOT-SPECS-SOURCE-ID
:bundle-core-public-roots-projection BUNDLE-ROOT-SPECS
:bundle-core-public-roots-id BUNDLE-ROOT-SPECS-ID :registry OWNER-REGISTRY
:candidates CANDIDATE-VECTORS :selections SELECTIONS :rejected []
:registry-root MR(:l2-owner-registry,
[ROOT-SPECS OWNER-REGISTRY CANDIDATE-VECTORS SELECTIONS []])}`.

`LOWERED-Q-FORMS(Q,RC)` is exactly `Q[:forms]` in Q form order. Every F must
have exactly one RCNODE, every RC node must join exactly one F, their counts
must be equal, and projecting RCNODE node IDs must be a permutation of all RC
node IDs. This equality is valid because Q already excludes the source
namespace tree; the more general checked-core namespace skip is a reject guard
that is never exercised by an authentic SH07 Q. A duplicate/missing RC form ID
or a namespace/untraced root in Q rejects.

`L2-CORE-FORMS` is the module-order, LOWERED-Q-FORMS order vector obtained by
joining each F to its unique registry row REG, its RCNODE N, and EIDX. Its
zero-based global ordinal is dense over the complete Q/RC bijection. Each row
base is exactly:

```clojure
{:ordinal g
 :source-id Q[:lineage :sh06-semantic-projection-id]
 :top-level-form-ordinal REG[:top-level-form-ordinal]
 :owning-defn-id REG[:defn-id]
 :form-id F[:form-id]
 :owning-root-id REG[:declared-body-root-id]
 :evaluation-index EIDX(RC, N)
 :effects N[:preserved-declarations :effects]
 :capabilities N[:preserved-declarations :capabilities]
 :profile N[:module :profile]}
```

The row adds only `:semantic-id`, equal to
`MID(:l2-core-form, ROW-BASE)`. Node profile/effects/capabilities must equal
the corresponding Q module declarations byte-for-byte. The three root entry
vectors preserve L2-CORE-FORMS order and select exactly, respectively,
`[:ordinal :source-id :form-id :semantic-id :evaluation-index]`,
`[:ordinal :source-id :form-id :semantic-id :effects]`, and
`[:ordinal :source-id :form-id :semantic-id :capabilities]`. Their roots are
`L2-EVALUATION-ORDER-ROOT = MR(:l2-evaluation-order, EVALUATION-ENTRIES)`,
`L2-EFFECTS-ROOT = MR(:l2-effects, EFFECT-ENTRIES)`, and
`L2-CAPABILITIES-ROOT = MR(:l2-capabilities, CAPABILITY-ENTRIES)`.

The C6 projection preserves checked-core identity instead of inventing a
parallel core graph. C6 rows are in module order and then RC `:nodes` order;
the Q-form/RC-node bijection and EIDX equations above are mandatory. A C6 AST
row has exactly:

```clojure
{:ordinal g
 :node-id N[:node-id]
 :kind N[:core-form]
 :children N[:children]
 :source-span N[:source :semantic-span]
 :origin-chain N[:source :origin-chain]
 :generated-origin N[:source :generated-origin]
 :evaluation-index EIDX(RC, N)
 :binding-context N[:binding-context]
 :profile N[:module :profile]
 :metadata N[:metadata]
 :fact-ids []
 :generated N[:generated?]}
```

Here `:node-id` and every child are typed upstream resolved checked-core IDs,
not MID values. Every child ID must name exactly one earlier row in the same
module, matching checked-core postorder. `C6-AST` is exactly
`{:entries AST-ENTRIES :root MR(:c6-ast, AST-ENTRIES)}`. This is the sole
exception to the generic MID rule for C6 AST node IDs.

The corresponding syntax-core row is exactly
`{:ordinal g :syntax-id N[:source :syntax-id] :core-id N[:node-id]
:origin-chain N[:source :origin-chain]
:generated-origin N[:source :generated-origin]}`. `C6-SYNTAX-CORE-MAP` is
`{:entries SYNTAX-CORE-ENTRIES
:root MR(:c6-syntax-core-map, SYNTAX-CORE-ENTRIES)}`.

`C6-DESUGARING-TRACE` is derived from the authentic macro trace rather than a
free pass ID. In module and Q macro-trace order, each trace T emits two rows,
def then fn, with one dense global ordinal:

```clojure
{:ordinal g :input-id T[:input-syntax-id]
 :output-id T[:output-def-syntax-id]
 :pass-id T[:macro-expansion-trace-id]
 :fact-ids [] :origin-id T[:def-generated-origin-id]}
{:ordinal (inc g) :input-id T[:input-syntax-id]
 :output-id T[:introduced-fn-syntax-id]
 :pass-id T[:macro-expansion-trace-id]
 :fact-ids [] :origin-id T[:fn-generated-origin-id]}
```

Every output and origin ID must occur in the corresponding Q form/source
origins and the macro-origin expectation must validate the complete trace
census. Its map is exactly `{:entries TRACE-ENTRIES
:root MR(:c6-desugaring-trace, TRACE-ENTRIES)}`.

`C6-ORIGIN-ENTRIES` has one row per AST row, in the same order, with exactly
`:ordinal :source-id :source-node-id :form-id :syntax-id :source-span
:origin-chain :generated-origin`. For AST/RC node N in module i, ordinal is
the AST row ordinal, source ID is
`Q_i[:lineage :sh06-semantic-projection-id]`, source-node ID is N `:node-id`,
and the final five fields are exactly N
`[:source :form-id]`, `[:source :syntax-id]`, `[:source :semantic-span]`,
`[:source :origin-chain]`, and `[:source :generated-origin]`, respectively.
Its root is
`MR(:c6-origins, C6-ORIGIN-ENTRIES)`. `C6-EVALUATION-ENTRIES` has one row per
RC evaluation row, in module/evaluation order, with exactly `:ordinal
:module-ordinal :source-id :core-node-id :kind :region
:owner-function-syntax-id :children`; the final five values are copied from
the RC row and its root is `MR(:c6-evaluation-order,
C6-EVALUATION-ENTRIES)`. Because C6 leaves type, effect, ownership, and safety
facts pending, AST and trace fact-ID vectors are exactly empty and
`C6-FACT-ROOT = MR(:c6-facts, [])`; checked-core
`:pending-fact-families` remains visible in the authenticated RC carrier but
is not mislabeled as a solved fact. `C6-ORIGINS-FACTS-ORDER` is exactly
`{:origin-root C6-ORIGIN-ROOT :fact-root C6-FACT-ROOT
:evaluation-order-root C6-EVALUATION-ORDER-ROOT}`.

For participant X, `JAVA-SEM(X)` is the exact projection of X TCB `:java` to
`:logical-role :byte-count :sha256 :version-lines`. `CP-SEM(X)` is the
ordinal-preserving projection of X TCB `:classpath` entries to exactly
`:ordinal :kind :logical-coordinate :byte-count :sha256
:provided-namespaces`. `RUNTIME-BASIS(X)` is exactly
`{:java-semantic JAVA-SEM(X) :classpath-semantic-rows CP-SEM(X)
:git-object-format X[:tcb :git :object-format]}`. Object format must be
`"sha1"`; A, B, and M must produce byte-equal W values before PRE-STAGES, and
L must independently match at final validation. Role, canonical path,
file-key, PID, host, start time, environment, receipt, process identity, and
working directory are forbidden from this basis. `RUNTIME-BASIS` denotes the
common value.

`BOOT2-SOURCE` is exactly `{:descriptors SOURCE-DESCRIPTORS
:descriptor-root SOURCE-DESCRIPTORS-ROOT
:source-id H([:gravity/sh07-b51-boot2-source-id-v1
SOURCE-DESCRIPTORS-ROOT])}`. `BOOT2-COMPILER` is exactly
`{:descriptor (nth SOURCE-DESCRIPTORS 10)
:compiler-id H([:gravity/sh07-b51-boot2-compiler-id-v1
(nth SOURCE-DESCRIPTORS 10)])}` and `BOOT2-BACKEND` is exactly
`{:status :not-applicable
:identity H([:gravity/sh07-b51-no-backend-v1 :verified-core])}`.
`BOOT2-JVM-ID = MID(:boot2-jvm, RUNTIME-BASIS[:java-semantic])` and
`BOOT2-CLOJURE-SEED-ID = MR(:boot2-dependencies,
RUNTIME-BASIS[:classpath-semantic-rows])`. `BOOT2-DEPENDENCIES` is exactly
`{:entries RUNTIME-BASIS[:classpath-semantic-rows]
:root BOOT2-CLOJURE-SEED-ID}`. `BOOT2-RUNTIME-ASSUMPTIONS` is exactly:

```clojure
{:jvm-id BOOT2-JVM-ID
 :clojure-seed-id BOOT2-CLOJURE-SEED-ID
 :git-object-format "sha1"
 :fixed-fuel-bounds-id H([:gravity/sh07-b51-fixed-fuel-bounds-v1 FIXED-FUEL])
 :network :forbidden}
```

For module i, `SID_i = Q_i[:lineage :sh06-semantic-projection-id]` and
`AD(i, K, V)` is exactly
`{:artifact-id MID(:boot3-artifact,
{:module-ordinal i :kind K :source-id SID_i :value V})
:kind K :source-id SID_i :raw-sha256 R(W(V))}`. BOOT3 input artifacts are
exactly `[AD(i, :authenticated-core-request, Q_i)]`; output artifacts are
exactly `[AD(i, :core-template, T_i) AD(i, :digest-requests, D_i)
AD(i, :resolved-digests, Z_i) AD(i, :resolved-core, RC_i)]`. Let P_i be
MODULE-PRODUCTS row i and let `F_i` be exactly
`subvec(FOUNDATION-PREPRODUCT[:fact-records], 4*i, 4*i+4)`; it must equal
`FOUNDATION-FACT-ROWS(P_i)` byte-for-byte. `BOOT3-FACTS_i` is exactly
`{:entries F_i :root MR(:boot3-facts, F_i)}`. `BOOT3-PRE-MODULE_i` is exactly:

```clojure
{:ordinal i
 :module Q_i[:module]
 :input-artifacts INPUT_i
 :output-artifacts OUTPUT_i
 :effects Q_i[:module :effects]
 :facts BOOT3-FACTS_i}
```

`BOOT3-PRE-MODULES` is the seven rows in module order. A final BOOT3 module
must preserve those six fields byte-for-byte and may add only its governed
receipt-dependent `:test-ids` and `:diagnostics` fields.

For module product P_i, `BOOT4-MODULE-ENTRY_i` is exactly
`{:ordinal i :module-id P_i[:module-id] :source-id P_i[:source-id]
:input-artifact-ids (mapv :artifact-id INPUT_i)
:output-artifact-ids (mapv :artifact-id OUTPUT_i) :profile :meta}`.
`BOOT4-MODULE-MANIFEST` is exactly `{:entries ENTRIES
:root MR(:boot4-module-manifest, ENTRIES)}` in module order. BOOT4 effect and
capability entries are in module then RC node order and are respectively
`{:module-id P_i[:module-id] :node-id N[:node-id]
:values N[:preserved-declarations :effects]}` and the same row with
`:capabilities`; those values must equal Q_i's module declarations. Their maps
are exactly `{:entries ENTRIES :root MR(:boot4-effects, ENTRIES)}` and
`{:entries ENTRIES :root MR(:boot4-capabilities, ENTRIES)}`.
`BOOT4-FACT-PROVENANCE` is exactly
`{:facts FOUNDATION-PREPRODUCT[:fact-records]
:provenance FOUNDATION-PREPRODUCT[:provenance-records]
:fact-root FOUNDATION-PREPRODUCT[:fact-root]
:provenance-root FOUNDATION-PREPRODUCT[:provenance-root]}`. All of these
values precede LOWERING-PRODUCT identity construction.

`BUILD-PRE-STAGES` is the exact pure constructor in the authenticated
`sh07_b51_attempt32_materialize.clj` source covered by
SEMANTIC-ALGORITHM-SOURCE-ROOT. Its input has exactly
`:source-semantic-carrier :module-transactions :bundle-core :root-specs
:sh06-authority-carrier :fixed-fuel :runtime-basis
:foundation-preproduct`, populated by the complete values named in this
report. `FOUNDATION-PREPRODUCT` has exactly `:pass-records :fact-records
:provenance-records :pass-root :fact-root :provenance-root`; it is constructed
before LOWERING-PRODUCT and contains no lowering-product ID, replay or
invalidation input, checker ID, operation, receipt, or later value. It is not
the later complete FOUNDATION-RUNTIME map.
It has no filesystem, environment, clock, random, process, network, global-var,
or caller-supplied fallback read. It returns exactly this map:

```clojure
{:l1 {:document L1-DOCUMENT
      :source-units L1-SOURCE-UNITS
      :grammar-change L1-GRAMMAR-CHANGE}
 :c2 {:snapshot C2-SNAPSHOT
      :units C2-UNITS
      :source-map C2-SOURCE-MAP
      :diagnostics C2-DIAGNOSTICS
      :literal-records C2-LITERAL-RECORDS
      :reader-extensions C2-READER-EXTENSIONS}
 :l2 {:owner-selection L2-OWNER-SELECTION
      :core-forms L2-CORE-FORMS
      :evaluation-order-root L2-EVALUATION-ORDER-ROOT
      :effects-root L2-EFFECTS-ROOT
      :capabilities-root L2-CAPABILITIES-ROOT}
 :c6 {:ast C6-AST
      :syntax-core-map C6-SYNTAX-CORE-MAP
      :desugaring-trace C6-DESUGARING-TRACE
      :origins-facts-order C6-ORIGINS-FACTS-ORDER}
 :boot2 {:source BOOT2-SOURCE
         :compiler BOOT2-COMPILER
         :backend BOOT2-BACKEND
         :dependencies BOOT2-DEPENDENCIES
         :runtime-assumptions BOOT2-RUNTIME-ASSUMPTIONS}
 :boot3 {:modules BOOT3-PRE-MODULES
         :profile :meta
         :stage-advanced false}
 :boot4 {:module-manifest BOOT4-MODULE-MANIFEST
         :effects BOOT4-EFFECTS
         :capabilities BOOT4-CAPABILITIES
         :fact-provenance BOOT4-FACT-PROVENANCE}}
```

L1 and C2 named values are the corresponding fields of
SOURCE-SEMANTIC-CARRIER. L2 values are constructed from the authenticated
ROOT-SPECS, BUNDLE-CORE, SH06-AUTHORITY-CARRIER, and Q_i form/fragment/binding
records by the exact owner/core-form/root equations below. C6 values are the
exact MR-rooted projections of the seven RC_i values and their authenticated
Q_i source identities described by the C6 schema below. BOOT2 values are the
exact source-descriptor, compiler, path-free runtime-basis, no-backend, and
bounds projections above. BOOT3-PRE-MODULES has seven rows in module order; each row
has exactly `:ordinal :module :input-artifacts :output-artifacts :effects
:facts`, with inputs from Q_i lineage and outputs from T_i, D_i, Z_i, and RC_i.
BOOT4 values are the exact seven-module projections and
FOUNDATION-PREPRODUCT fact/provenance rows and roots described below. Every named value must
pass that later exact sub-schema before BUILD-PRE-STAGES returns; the later
text does not authorize a second constructor or freer value.

`PRE-STAGES` is exactly the result of BUILD-PRE-STAGES on those eight inputs.
`RECEIPT-FREE-STAGE-PREFIX(J)` is exactly
`PRE-STAGES[(:stage-key J)]`. A, B, and M independently reconstruct the input
values from their immutable Git objects, run this same source-authenticated
constructor, and require byte-equal PRE-STAGES. No process may transport a
PRE-STAGES value as authority.

The seven prefixes contain no fuel-subject ID, operation or schedule ID,
trace, receipt, checker ID, certificate, witness/member ID, product ID,
diagnostic compatibility, stage-comparison, mutation receipt, preservation
result, or deterministic output receipt. In particular, L2 external-binding,
edge-authority, diagnostic, and negative-fixture rows; C6 verification and
mutation rows; BOOT3 test/diagnostic/cross-role rows; and BOOT4
deterministic-output and preservation-test rows are final-stage values and
cannot enter a pre-product semantic root. They remain mandatory and are hashed
and checked by their complete final stage witness after receipts exist.

For row J, `PRE-STAGE-SEMANTIC(J)` is
`PROJECT-STAGE-SEMANTIC(J, RECEIPT-FREE-STAGE-PREFIX(J))`, and
`STAGE-SEMANTIC-ROOT(J)` is exactly:

```clojure
H([:gravity/sh07-b51-fixed-fuel-stage-semantic-root-v1
   {:ordinal (:ordinal J)
    :owner (:owner J)
    :stage-key (:stage-key J)
    :semantic (PRE-STAGE-SEMANTIC J)}])
```

This is the sole payload grammar of that H domain. The seven named values
`L1-STAGE-SEMANTIC-ROOT`, `C2-STAGE-SEMANTIC-ROOT`,
`L2-STAGE-SEMANTIC-ROOT`, `C6-STAGE-SEMANTIC-ROOT`,
`BOOT2-STAGE-SEMANTIC-ROOT`, `BOOT3-STAGE-SEMANTIC-ROOT`, and
`BOOT4-STAGE-SEMANTIC-ROOT` are the results for ordinals 0 through 6.
`FUEL-SUBJECTS` is the following exact dense seven-map vector in
operation-owner order:

```clojure
[{:ordinal 0 :owner :l1 :kind :l1-source-syntax-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:module-forms L1-MODULE-FORMS
                :top-level-forms L1-TOP-LEVEL-FORMS
                :fragment-forms L1-FRAGMENT-FORMS
                :form-children L1-FORM-CHILDREN
                :form-depths L1-FORM-DEPTHS
                :origin-entries L1-ORIGIN-ENTRIES
                :metadata-entries L1-METADATA-ENTRIES
                :alias-records L1-ALIAS-RECORDS}
  :collection-roots L1-FUEL-CARRIER-ROOTS
  :stage-semantic-root L1-STAGE-SEMANTIC-ROOT
  :subject-id L1-FUEL-SUBJECT-ID}
 {:ordinal 1 :owner :c2 :kind :c2-token-form-source-map-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:module-carriers C2-MODULE-CARRIERS}
  :collection-roots C2-FUEL-CARRIER-ROOTS
  :stage-semantic-root C2-STAGE-SEMANTIC-ROOT
  :subject-id C2-FUEL-SUBJECT-ID}
 {:ordinal 2 :owner :l2 :kind :l2-owner-effect-capability-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:fragments L2-FRAGMENTS
                :module-bindings L2-MODULE-BINDINGS
                :bindings L2-BINDINGS
                :fragment-resolutions L2-FRAGMENT-RESOLUTIONS
                :module-resolutions L2-MODULE-RESOLUTIONS
                :effects L2-EFFECTS
                :capabilities L2-CAPABILITIES
                :exports L2-EXPORTS}
  :collection-roots L2-FUEL-CARRIER-ROOTS
  :stage-semantic-root L2-STAGE-SEMANTIC-ROOT
  :subject-id L2-FUEL-SUBJECT-ID}
 {:ordinal 3 :owner :c6 :kind :c6-core-lowering-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:module-core C6-MODULE-CORE
                :resolved-core-carriers C6-RESOLVED-CORE-CARRIERS}
  :collection-roots C6-FUEL-CARRIER-ROOTS
  :stage-semantic-root C6-STAGE-SEMANTIC-ROOT
  :subject-id C6-FUEL-SUBJECT-ID}
 {:ordinal 4 :owner :boot2 :kind :boot2-template-carrier-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:template-carriers BOOT2-TEMPLATE-CARRIERS}
  :collection-roots BOOT2-FUEL-CARRIER-ROOTS
  :stage-semantic-root BOOT2-STAGE-SEMANTIC-ROOT
  :subject-id BOOT2-FUEL-SUBJECT-ID}
 {:ordinal 5 :owner :boot3 :kind :boot3-generated-digest-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:generated-digest-carriers BOOT3-GENERATED-DIGEST-CARRIERS}
  :collection-roots BOOT3-FUEL-CARRIER-ROOTS
  :stage-semantic-root BOOT3-STAGE-SEMANTIC-ROOT
  :subject-id BOOT3-FUEL-SUBJECT-ID}
 {:ordinal 6 :owner :boot4 :kind :boot4-module-digest-walk
  :source-roots FUEL-SOURCE-ROOTS
  :collections {:module-digest-requests BOOT4-MODULE-DIGEST-REQUESTS}
  :collection-roots BOOT4-FUEL-CARRIER-ROOTS
  :stage-semantic-root BOOT4-STAGE-SEMANTIC-ROOT
  :subject-id BOOT4-FUEL-SUBJECT-ID}]
```

Each subject has exactly `:ordinal :owner :kind :source-roots :collections
:collection-roots :stage-semantic-root :subject-id`. Its collection-roots map
has exactly the same keys as collections. For collection key K and value V in
subject S, the K root
equals `H([:gravity/sh07-b51-fixed-fuel-carrier-root-v1
{:owner (:owner S) :kind (:kind S) :collection K :value V}])`. This is the sole
payload grammar of that H domain. `FUEL-CARRIER-ROOTS(OWNER, KIND,
COLLECTIONS)` constructs the complete canonical map by applying this equation
to those three arguments only; it cannot read a subject, subject ID, operation,
receipt, or later value. The seven named `*-FUEL-CARRIER-ROOTS` values are
computed from their printed owner, kind, and collection map before the subject
row is assembled. For subject S, `:subject-id` equals
`H([:gravity/sh07-b51-fixed-fuel-subject-v1 (dissoc S :subject-id)])`.
The seven named subject IDs in FIXED-FUEL-OPERATION-SPECS equal the respective
values above, and LOWERING-PRODUCT `:fuel-subjects` equals this complete vector.
Each `:stage-semantic-root` equals the corresponding ordinal's named root
above. Ordinal, owner, kind, collection key set, collection order, semantic
root, or source-root substitution rejects.

The named carriers are materialized inside the object-mode wrapper during one
authenticated pre-certificate transaction per module.
`MODULE-TRANSACTIONS =
RECEIPTED-BUNDLE-CORE-PRODUCTS[:module-transactions]` byte-for-byte. It is an
exact seven-row vector in module-ordinal order; every row has exactly `:module-ordinal :source-descriptor
:resolution-artifact :c2-reader-artifact :authenticated-core-request
:resolved-digests :module-core :template-carrier :generated-digest-carrier
:resolved-core-carrier :module-digest-requests`. For row i:

- module ordinal is i and source descriptor is source-session descriptor
  `3 + i`;
- `R_i`, the resolution artifact, is the complete SH06 artifact freshly built
  from that descriptor's C/T blob by the governed bundle-core builder in
  object mode, with the descriptor's project-relative `:path` supplied as the
  source path at construction time and `PROJECT-CONTEXT_(3+i)` supplied as
  the C2 project context (including nil project-root path), and accepted by
  `sh06-resolution-artifact-verification`; no checked-out, absolute, or host
  path is supplied and no later path normalization is permitted;
- C2 reader artifact is exactly
  `R_i[:sh05-macro-artifact :gravity-macro-boundary
  :authenticated-sh04-artifact :c2-reader-artifact]`;
- authenticated core request `Q_i` is exactly the receipt-free value returned
  by `sh07-core-authenticated-request(R_i)`; its
  `[:provenance :actual-source-path]` already equals the same descriptor
  project-relative `:path` when Q_i is first constructed, and it has the closed
  request fields
  `:module :forms :top-level-form-ids :binding-table :alias-table
  :resolution-table :fragment-manifest` used below; and
- the remaining row values are constructed only by the following normative
  adapter equations, where `PATH_i` is that descriptor's project-relative
  `:path`:

```clojure
RAW_i = sh07-build-core-template(Q_i)
require RAW_i[:status] = :accepted
T_i = RAW_i[:core-template]
D_i = RAW_i[:digest-requests]
require sh07-verify-core-template(Q_i, T_i, D_i)[:status] = :passed

Z_i = sh07-core-digest-requests(PATH_i, D_i)
Z_i[j] = reader-canonical-hash(
  {:domain :gravity/sh07-declared-digest-v1
   :purpose D_i[j][:purpose]
   :preimage sh07-core-resolve-digest-preimage!(
     PATH_i, D_i[j][:purpose], D_i[j][:preimage], subvec(Z_i, 0, j))})

RESOLUTION_i = sh07-resolve-core-template(T_i, Z_i)
require RESOLUTION_i[:status] = :accepted
RC_i = RESOLUTION_i[:value]
PASS-EXPECTED =
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :passed :rule nil}
REJECT-EXPECTED =
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :rejected :rule "C6-VERIFY"}
require RC_i[:module :namespace] != 'gravity.altered
BAD-RC_i = assoc-in(RC_i, [:module :namespace], 'gravity.altered)
PASS-ACTUAL_i = sh07-verify-core-resolved(Q_i, RC_i, D_i, Z_i)
REJECT-ACTUAL_i = sh07-verify-core-resolved(Q_i, BAD-RC_i, D_i, Z_i)
require PASS-ACTUAL_i = PASS-EXPECTED
require REJECT-ACTUAL_i = REJECT-EXPECTED
require RC_i[:diagnostics] = []

M_i[:module-core] = RC_i[:nodes]
M_i[:resolved-digests] = Z_i
M_i[:template-carrier] = T_i
M_i[:generated-digest-carrier] = D_i
M_i[:resolved-core-carrier] = RC_i
M_i[:module-digest-requests] = D_i
```

`sh07-core-digest-requests` validates dense request ordinals and the exact
`:ordinal :purpose :preimage` keyset before computing each Z_i entry. No
resolved digest, template, request, resolved core, or module-core value may be
caller supplied. These six row fields are emitted from Q_i by this
authenticated adapter before any LOWERING-PRODUCT ID, operation, receipt,
stage witness, or certificate exists. Within each row,
`:generated-digest-carrier` and `:module-digest-requests` are byte-equal, not
merely equivalent after rewriting. PASS/REJECT expected values, BAD-RC, and
both actual verifier outcomes are constructed receipt-free before
LOWERING-PRODUCT, but none is inserted into PRE-STAGES, FUEL-SUBJECTS, or
LOWERING-PRODUCT. The accepted verifier's existing source-coverage mutation is
therefore replayed without creating a receipt back-edge. Additionally,
`count(RC_i[:nodes]) = count(Q_i[:forms])`; every Q_i form ID occurs exactly
once as an RC_i node `:form-id`, no other node form ID exists, and duplicate
form-to-node coverage rejects. This joins the proof-census predicted core-node
count to the actual resolved C6 node carrier without replacing either value.
The digest-count reconciliation is exactly
`count(D_i) <= count(Q_i[:forms]) + 4 <=
FIXED-FUEL[:maximum-module-digest-requests]`; operation BOOT4 still measures
the actual D_i vector. Thus the proof census's source prediction and the
checked-core adapter's actual requests must both fit the same governed bound;
the older checked-core preflight detail text `forms + 2` is not treated as a
count oracle or authority.

The constructor and every transitive project namespace it loads are covered by
the role-specific SOURCE-CLOSURE; its five V9 entry/codec sources are covered
by SEMANTIC-ALGORITHM-SOURCE-ROOT. The transaction accepts no caller-provided
R_i, Q_i, form tree, table, carrier, product, or ID. Each descriptor raw SHA,
R_i artifact ID and verification, C2 artifact ID, Q_i lineage IDs, and
BUNDLE-CORE module-plan join are recomputed before row admission. A missing,
extra, reordered, differently sourced, or multiply produced row rejects.

The carrier equations are literal. `M` below ranges over MODULE-TRANSACTIONS in
module-ordinal order; `F` ranges over `M[:authenticated-core-request :forms]`
in form order; and `G` ranges over
`M[:authenticated-core-request :fragment-manifest]` in fragment order.
`MODULE-COORD(M)` is exactly `[(:module-ordinal M)
(get-in M [:authenticated-core-request :lineage
:sh06-semantic-projection-id])]`.
`RF-Q(M)` is exactly `M[:authenticated-core-request]`, without replacement,
normalization, dissociation, or rewriting. Its
`[:provenance :actual-source-path]` must already equal M source descriptor
`:path`. A checked-out, absolute, or host path anywhere in R_i, Q_i,
`:module-core`, `:template-carrier`, `:generated-digest-carrier`,
`:resolved-core-carrier`, or any digest-request preimage rejects before the row
is admitted.
`FORM-DEPTHS(Q, C)` is the unique dense `[[C FORM-ID] DEPTH]` vector obtained by
postorder-validating every Q form's `:child-form-ids` and walking exactly Q
`:top-level-form-ids` at depth one. After that validation pass, output rows are
emitted in Q `:forms` vector order as
`(mapv (fn [F] [[C (:form-id F)] (get DEPTH-BY-ID (:form-id F))])
(:forms Q))`; traversal worklist order never determines identity. A missing,
duplicate, unreachable, cyclic, or multiply reached form rejects.

```clojure
L1-MODULE-FORMS =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)]
            [[[C :forms] (mapv :form-id (:forms Q))]
             [[C :macro-expansion-trace] (:macro-expansion-trace Q)]]))
        MODULE-TRANSACTIONS))
L1-TOP-LEVEL-FORMS =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)
                X (:macro-origin-expectation Q)]
            (concat
             [[[C :top-level-form-ids] (:top-level-form-ids Q)]]
             (mapcat
              (fn [G]
                [[[[C (:fragment-id G)] :root-form-ids] (:root-form-ids G)]
                 [[[C (:fragment-id G)] :root-node-ids] (:root-node-ids G)]])
              (:fragment-manifest Q))
             [[[C :macro-origin-traces] (:macro-origin-traces Q)]
              [[C :expected-input-syntax-ids]
               (:expected-input-syntax-ids X)]
              [[C :expected-output-def-syntax-ids]
               (:expected-output-def-syntax-ids X)]
              [[C :expected-introduced-fn-syntax-ids]
               (:expected-introduced-fn-syntax-ids X)]])))
        MODULE-TRANSACTIONS))
L1-FRAGMENT-FORMS =
  (vec (mapcat (fn [M]
                 (mapv (fn [G] [[(MODULE-COORD M) (:fragment-id G)]
                                (:form-ids G)])
                       (get-in M [:authenticated-core-request :fragment-manifest])))
               MODULE-TRANSACTIONS))
L1-FORM-CHILDREN =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)
                RC (:resolved-core-carrier M)]
            (concat
             (mapv (fn [F]
                     [[[C (:form-id F)] :child-form-ids]
                      (:child-form-ids F)])
                   (:forms Q))
             (mapcat
              (fn [J CALL]
                [[[[C :call J] :argument-node-ids]
                  (:argument-node-ids CALL)]
                 [[[C :call J] :ordered-evaluation-node-ids]
                  (:ordered-evaluation-node-ids CALL)]])
              (range) (:calls RC)))))
        MODULE-TRANSACTIONS))
L1-FORM-DEPTHS =
  (vec (mapcat (fn [M] (FORM-DEPTHS (:authenticated-core-request M)
                                     (MODULE-COORD M)))
               MODULE-TRANSACTIONS))
L1-ORIGIN-ENTRIES =
  (vec (mapcat (fn [M]
                 (mapcat (fn [F] [[[(MODULE-COORD M) :origin-chain
                                     (:form-id F)] (:origin-chain F)]
                                  [[(MODULE-COORD M) :generated-origin
                                    (:form-id F)] (:generated-origin F)]])
                         (get-in M [:authenticated-core-request :forms])))
               MODULE-TRANSACTIONS))
L1-METADATA-ENTRIES =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)]
            (concat
             (mapv (fn [F]
                     [[C :form (:form-id F)]
                      (W-CANONICAL-MAP-ENTRIES (:metadata F))])
                   (:forms Q))
             (mapv (fn [B]
                     [[C :binding (:binding-id B)]
                      (W-CANONICAL-MAP-ENTRIES (:semantic-span B))])
                   (:binding-table Q)))))
        MODULE-TRANSACTIONS))
L1-ALIAS-RECORDS =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)]
            (concat
             [[[C :module-alias-table] (:alias-table Q)]]
             (mapv (fn [G]
                     [[C :fragment-aliases (:fragment-id G)]
                      (:alias-names G)])
                   (:fragment-manifest Q)))))
        MODULE-TRANSACTIONS))

C2-MODULE-CARRIERS =
  (vec (mapcat
        (fn [M]
          [[[(MODULE-COORD M) :authenticated-request] (RF-Q M)]
           [[(MODULE-COORD M) :resolved-digests] (:resolved-digests M)]])
        MODULE-TRANSACTIONS))

L2-FRAGMENTS =
  (mapv (fn [M] [(MODULE-COORD M)
                 (mapv :fragment-id
                       (get-in M [:authenticated-core-request :fragment-manifest]))])
        MODULE-TRANSACTIONS)
L2-MODULE-BINDINGS =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)
                RC (:resolved-core-carrier M)]
            [[[C :binding-table] (mapv :binding-id (:binding-table Q))]
             [[C :function-records] (:function-records RC)]
             [[C :recursion-components] (:recursion-components RC)]]))
        MODULE-TRANSACTIONS))
L2-BINDINGS =
  (vec (mapcat (fn [M]
                 (mapcat (fn [G]
                           [[[(MODULE-COORD M) :local (:fragment-id G)]
                             (:local-binding-ids G)]
                            [[(MODULE-COORD M) :external (:fragment-id G)]
                             (:external-binding-ids G)]])
                         (get-in M [:authenticated-core-request :fragment-manifest])))
               MODULE-TRANSACTIONS))
L2-FRAGMENT-RESOLUTIONS =
  (vec (mapcat (fn [M]
                 (mapv (fn [G] [[(MODULE-COORD M) (:fragment-id G)]
                                (:resolution-reference-syntax-ids G)])
                       (get-in M [:authenticated-core-request :fragment-manifest])))
               MODULE-TRANSACTIONS))
L2-MODULE-RESOLUTIONS =
  (mapv (fn [M] [(MODULE-COORD M)
                 (mapv :reference-syntax-id
                       (get-in M [:authenticated-core-request :resolution-table]))])
        MODULE-TRANSACTIONS)
L2-EFFECTS =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)]
            (concat
             [[[C :module-effects] (get-in Q [:module :effects])]]
             (mapcat
              (fn [B]
                [[[C :binding (:binding-id B) :effects] (:effects B)]
                 [[C :binding (:binding-id B) :profile-set]
                  (:profile-set B)]
                 [[C :binding (:binding-id B) :target-set]
                  (:target-set B)]])
              (:binding-table Q)))))
        MODULE-TRANSACTIONS))
L2-CAPABILITIES =
  (vec (mapcat
        (fn [M]
          (let [C (MODULE-COORD M)
                Q (:authenticated-core-request M)]
            (concat
             [[[C :module-capabilities]
               (get-in Q [:module :capabilities])]]
             (mapv
              (fn [B]
                [[C :binding (:binding-id B) :capabilities]
                 (:capabilities B)])
              (:binding-table Q))
             (mapv
              (fn [A]
                [[C :alias (:alias A) :targets] (:targets A)])
              (:alias-table Q)))))
        MODULE-TRANSACTIONS))
L2-EXPORTS =
  (mapv (fn [M] [(MODULE-COORD M)
                 (get-in M [:authenticated-core-request :module :exports])])
        MODULE-TRANSACTIONS)

C6-MODULE-CORE =
  (mapv (fn [M] [(MODULE-COORD M) (:module-core M)]) MODULE-TRANSACTIONS)
C6-RESOLVED-CORE-CARRIERS =
  (mapv (fn [M] [(MODULE-COORD M) (:resolved-core-carrier M)])
        MODULE-TRANSACTIONS)
BOOT2-TEMPLATE-CARRIERS =
  (mapv (fn [M] [(MODULE-COORD M) (:template-carrier M)])
        MODULE-TRANSACTIONS)
BOOT3-GENERATED-DIGEST-CARRIERS =
  (mapv (fn [M] [(MODULE-COORD M) (:generated-digest-carrier M)])
        MODULE-TRANSACTIONS)
BOOT4-MODULE-DIGEST-REQUESTS =
  (mapv (fn [M] [(MODULE-COORD M) (:module-digest-requests M)])
        MODULE-TRANSACTIONS)

L1-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :l1 :l1-source-syntax-walk
    {:module-forms L1-MODULE-FORMS :top-level-forms L1-TOP-LEVEL-FORMS
     :fragment-forms L1-FRAGMENT-FORMS :form-children L1-FORM-CHILDREN
     :form-depths L1-FORM-DEPTHS :origin-entries L1-ORIGIN-ENTRIES
     :metadata-entries L1-METADATA-ENTRIES :alias-records L1-ALIAS-RECORDS})
C2-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :c2 :c2-token-form-source-map-walk
    {:module-carriers C2-MODULE-CARRIERS})
L2-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :l2 :l2-owner-effect-capability-walk
    {:fragments L2-FRAGMENTS :module-bindings L2-MODULE-BINDINGS
     :bindings L2-BINDINGS :fragment-resolutions L2-FRAGMENT-RESOLUTIONS
     :module-resolutions L2-MODULE-RESOLUTIONS :effects L2-EFFECTS
     :capabilities L2-CAPABILITIES :exports L2-EXPORTS})
C6-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :c6 :c6-core-lowering-walk
    {:module-core C6-MODULE-CORE
     :resolved-core-carriers C6-RESOLVED-CORE-CARRIERS})
BOOT2-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :boot2 :boot2-template-carrier-walk
    {:template-carriers BOOT2-TEMPLATE-CARRIERS})
BOOT3-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :boot3 :boot3-generated-digest-walk
    {:generated-digest-carriers BOOT3-GENERATED-DIGEST-CARRIERS})
BOOT4-FUEL-CARRIER-ROOTS =
  (FUEL-CARRIER-ROOTS :boot4 :boot4-module-digest-walk
    {:module-digest-requests BOOT4-MODULE-DIGEST-REQUESTS})
```

`W-CANONICAL-MAP-ENTRIES` returns the map's complete key/value rows in W key
order. Every L1/L2 collection is therefore a dense vector of exactly
`[OWNER-COORDINATE ORDERED-VALUE]`; ORDERED-VALUE is a vector except for the
nonnegative integer value in each form-depth row. Effects and capabilities
retain raw keywords, exports retain raw symbols, and no value is falsely typed
as an ID. For each module, `:expanded-defn-count` in its authenticated
macro-origin expectation equals the cardinality of each of
`:expected-input-syntax-ids`, `:expected-output-def-syntax-ids`, and
`:expected-introduced-fn-syntax-ids`; a mismatch rejects before measurement.
The module/top-level/form-child/metadata/alias/module-binding/effect/capability
collections are the printed typed-row unions, not summaries: they include
every authenticated checked-core use governed by the corresponding shared
FIXED-FUEL counter. Removing any union row or moving it to another counter
rejects. The exact request-side measurements in `sh07_proof_census.clj` are a
required subset of these rows. Where checked-core reuses the same bound for a
resolved call, function, or recursion carrier, that actual-use row is retained
as well. The operation's observed value is the maximum across the full union;
it need not equal the narrower source-census observation, but it must be no
smaller than every source-census measurement mapped to that bound and no
greater than FIXED-FUEL. This distinction is checked explicitly and prevents
either a source-census omission or a later checked-core use from being hidden.
For every fragment-resolution row, its values equal that fragment's exact
`:resolution-reference-syntax-ids` and each joins exactly once to the same
module Q_i `:resolution-table`. Every external binding ID joins exactly once
to the same Q_i `:binding-table` and is authorized by the exact Q resolution
classifier defined below. Q externality is not equated with an SH06 M1 edge:
core bindings, declared imports, declared namespace aliases, and same-module
references are present in Q but are not all cross-module calls. Only the
filtered seven-module operator-call subset participates in the exact Q-to-M1
bijection below. An unauthorized cross-fragment ID cannot enter either
projection.

Each module-core value is the dense vector of complete canonical core-node
records. C2-MODULE-CARRIERS has exactly two typed rows per module in printed
order: the complete logical-path authenticated Q_i request followed by the
complete Z_i resolved-digest vector. Each resolved-core, template, and
generated-digest carrier is the complete strict canonical EDN value at its
literal MODULE-TRANSACTIONS field.
Each module-digest-requests value is byte-equal to that same module's complete
BOOT3 generated-digest carrier and is a dense vector of maps with exactly
`:ordinal :purpose :preimage`; ordinals are dense, purpose is the authenticated
closed lowering purpose, and preimage is the complete canonical output
preimage. No ID-only summary, rewritten request, or additional request is
admitted.

All IDs referenced by a carrier are recomputed from the authenticated C/T
source session and governed SH02-SH06 inputs before the carrier is admitted.
The carriers contain no role, process, host path, LOWERING-PRODUCT ID,
operation or schedule ID, fuel receipt or receipt ID, certificate, stage
witness, enclosing WITNESS, checker-output value ID, or later Git object.
Consequently every subject and the enclosing LOWERING-PRODUCT are fixed before
any operation identity exists.

`MEASUREMENT-SPECS` is literally the following dense vector. Each row is
exactly `[GLOBAL-ORDINAL OWNER LOCAL-ORDINAL COUNTER-KEY COLLECTION-PATH
DERIVATION]`:

```clojure
[[0 :l1 0 :maximum-module-forms
  [:collections :module-forms] :maximum-row-cardinality]
 [1 :l1 1 :maximum-top-level-forms
  [:collections :top-level-forms] :maximum-row-cardinality]
 [2 :l1 2 :maximum-fragment-forms
  [:collections :fragment-forms] :maximum-row-cardinality]
 [3 :l1 3 :maximum-form-children
  [:collections :form-children] :maximum-row-cardinality]
 [4 :l1 4 :maximum-form-depth
  [:collections :form-depths] :maximum-row-value]
 [5 :l1 5 :maximum-origin-entries
  [:collections :origin-entries] :maximum-row-cardinality]
 [6 :l1 6 :maximum-metadata-entries
  [:collections :metadata-entries] :maximum-row-cardinality]
 [7 :l1 7 :maximum-alias-records
  [:collections :alias-records] :maximum-row-cardinality]
 [8 :c2 0 :maximum-module-carrier-nodes
  [:collections :module-carriers] :maximum-row-carrier-node-count]
 [9 :c2 1 :maximum-module-carrier-depth
  [:collections :module-carriers] :maximum-row-carrier-depth]
 [10 :c2 2 :maximum-module-carrier-width
  [:collections :module-carriers] :maximum-row-carrier-width]
 [11 :c2 3 :maximum-module-scalar-bytes
  [:collections :module-carriers] :maximum-row-executable-scalar-cost]
 [12 :l2 0 :maximum-fragments
  [:collections :fragments] :maximum-row-cardinality]
 [13 :l2 1 :maximum-module-bindings
  [:collections :module-bindings] :maximum-row-cardinality]
 [14 :l2 2 :maximum-bindings
  [:collections :bindings] :maximum-row-cardinality]
 [15 :l2 3 :maximum-fragment-resolutions
  [:collections :fragment-resolutions] :maximum-row-cardinality]
 [16 :l2 4 :maximum-module-resolutions
  [:collections :module-resolutions] :maximum-row-cardinality]
 [17 :l2 5 :maximum-effects
  [:collections :effects] :maximum-row-cardinality]
 [18 :l2 6 :maximum-capabilities
  [:collections :capabilities] :maximum-row-cardinality]
 [19 :l2 7 :maximum-exports
  [:collections :exports] :maximum-row-cardinality]
 [20 :c6 0 :maximum-module-core-nodes
  [:collections :module-core] :maximum-row-cardinality]
 [21 :c6 1 :maximum-resolved-core-carrier-nodes
  [:collections :resolved-core-carriers] :maximum-row-carrier-node-count]
 [22 :c6 2 :maximum-resolved-core-carrier-depth
  [:collections :resolved-core-carriers] :maximum-row-carrier-depth]
 [23 :c6 3 :maximum-resolved-core-carrier-width
  [:collections :resolved-core-carriers] :maximum-row-carrier-width]
 [24 :c6 4 :maximum-resolved-core-scalar-bytes
  [:collections :resolved-core-carriers] :maximum-row-executable-scalar-cost]
 [25 :boot2 0 :maximum-template-carrier-nodes
  [:collections :template-carriers] :maximum-row-carrier-node-count]
 [26 :boot2 1 :maximum-template-carrier-depth
  [:collections :template-carriers] :maximum-row-carrier-depth]
 [27 :boot2 2 :maximum-template-carrier-width
  [:collections :template-carriers] :maximum-row-carrier-width]
 [28 :boot2 3 :maximum-template-scalar-bytes
  [:collections :template-carriers] :maximum-row-executable-scalar-cost]
 [29 :boot3 0 :maximum-generated-digest-carrier-nodes
  [:collections :generated-digest-carriers] :maximum-row-carrier-node-count]
 [30 :boot3 1 :maximum-generated-digest-carrier-depth
  [:collections :generated-digest-carriers] :maximum-row-carrier-depth]
 [31 :boot3 2 :maximum-generated-digest-carrier-width
  [:collections :generated-digest-carriers] :maximum-row-carrier-width]
 [32 :boot3 3 :maximum-generated-digest-scalar-bytes
  [:collections :generated-digest-carriers] :maximum-row-executable-scalar-cost]
 [33 :boot4 0 :maximum-module-digest-requests
  [:collections :module-digest-requests] :maximum-row-cardinality]]
```

Global ordinals are dense 0 through 33. For every owner, local ordinals are
dense from zero and the counter-key projection of its rows in local order
equals the corresponding FIXED-FUEL-OPERATION-SPECS `:counter-keys`
byte-for-byte. Across all owners the keys are unique and equal the complete
FIXED-FUEL key set after unsigned-UTF-8 sorting both sides. Every selected
collection is a dense vector of exact `[COORD VALUE]` rows with unique COORD.
Before measurement, every VALUE is recursively validated as strict canonical
EDN composed only of nil, booleans, integers, strings, characters, keywords,
symbols, maps, vectors, sets, and lists. Lazy sequences, queues, records,
tagged host objects, ratios, floating values, and every other `coll?` or host
type reject, even though a generic proof-census helper could traverse them.
Within this validated domain, a list is exactly the canonical EDN value for
which checked-core `seq?` is true, so the accepted aggregate predicate is
equivalent to checked-core's map/vector/set/`seq?` predicate.
`CARRIER-MEASURE(VALUE)` is the sole carrier walker. It initializes a frontier
with `[VALUE 0]`. A value is aggregate only when it is a map, vector, set, or
list. A map's children are its alternating key then value components in W key
order and its width is `2 * count(map)`. Vector and list children retain index
order and have width `count(value)`; set children use W-byte order and have
width `count(set)`. Every visited aggregate and scalar increments node count
once; depth is the maximum visited root-relative depth, with the root at depth
zero; width is the maximum checked-core aggregate width; and every scalar adds
exactly `4 * (count (str scalar))` to executable scalar cost, so nil contributes
zero. Encountering another collection or host value type rejects. Traversal
order is deterministic even where only aggregate totals are returned.

`:maximum-row-cardinality` is `max(count VALUE)`;
`:maximum-row-value` is `max(VALUE)` for nonnegative integer VALUE; and
`:maximum-row-carrier-node-count`, `:maximum-row-carrier-depth`,
`:maximum-row-carrier-width`, and `:maximum-row-executable-scalar-cost` walk
each VALUE alone with `CARRIER-MEASURE` and take the maximum of the resulting
per-row measurements.
For `:maximum-row-executable-scalar-cost`, each scalar contributes exactly
`4 * (count (str scalar))`, where Clojure `count` counts UTF-16 code units;
costs are summed within one VALUE before the maximum is taken. W map-key order
still governs deterministic traversal, but W encoded scalar byte length is not
the cost rule. Every derivation returns zero only when the selected row vector
is empty. For BOOT4, each VALUE must first
validate as a dense vector of maps with exactly `:ordinal :purpose :preimage`
before its cardinality is measured.

The dependency and construction order is exact: A and B each derive all seven
receipt-free carriers from their own authenticated C source session; construct
FUEL-SUBJECTS and subject IDs; construct and hash the complete
LOWERING-PRODUCT containing them; construct the seven operation rows and IDs;
execute the 34 measurements; construct the seven traces and fuel receipts;
insert each owner's sole operation ID and fuel receipt or receipt ID wherever
the later stage-witness schema requires it; compute every receipt-dependent
member/root, the seven final stage-witness IDs, and then the enclosing WITNESS
and certificate. M independently repeats the pre-certificate construction from
the immutable C/T Git objects, validates the complete subject vector and both
child lowering-product IDs, and then validates every operation, trace, receipt,
stage witness, and the common WITNESS. Reversing an edge, deriving a subject
from a final stage witness, or accepting a transported subject without this
reconstruction rejects.

Every TRACE coordinate is the exact MEASUREMENT-SPECS collection path and must
resolve inside its authenticated FUEL-SUBJECT. It never selects a stage witness,
receipt-dependent field, descriptor outside the subject, or caller-provided
value. The subject rehashes to the manifest subject ID before traversal. A
missing or extra collection, path, row, referenced ID, or preimage; a different
derivation; or a coordinate outside `:collections` rejects. No fuel receipt,
operation ID, or receipt-dependent enclosing root is reachable from a subject
ID, so the identity graph is acyclic.

The concatenation of
all counter-key vectors must equal the unsigned-UTF-8-sorted complete key set
of FIXED-FUEL after sorting both sides, and its count is exactly 34, so no
bound is absent or duplicated. `OPERATION-MANIFEST` is the seven-row vector
obtained by joining operation-spec row i to
`LOWERING-PRODUCT[:fuel-subjects i]`, requiring equal ordinal, owner, kind, and
subject ID, and then adding exactly `:algorithm-source-root
SEMANTIC-ALGORITHM-SOURCE-ROOT`, `:lowering-product-id
LOWERING-PRODUCT[:product-id]`, and `:operation-id`. Each final operation row
therefore has exactly `:ordinal :owner :kind :subject-id :counter-keys
:algorithm-source-root :lowering-product-id :operation-id`. No caller may
supply or replace the subject ID or lowering-product ID. Operation ID is
`H([:gravity/sh07-b51-bounded-operation-id-v1
ROW-WITHOUT-OPERATION-ID])`. Operation schedule ID is
`H([:gravity/sh07-b51-operation-schedule-v1 OPERATION-MANIFEST])` and equals
WITNESS `:operation-schedule-id`. Reusing an otherwise identical operation row
with a different lowering product therefore changes the operation ID and
rejects at the schedule and receipt joins.

Every kind denotes one fixed traversal. The L1 kind walks source units, forms,
children, origins, metadata, and aliases in descriptor/source order. The L2
kind walks modules, fragments, bindings, resolutions, effects, capabilities,
and exports in authenticated module/evaluation order. The C2, C6, BOOT2, and
BOOT3 carrier kinds walk the final carrier in each module row in canonical EDN
depth-first order: map entries in canonical W key order and vector entries by
index. Within a row, `-nodes` is the CARRIER-MEASURE visited aggregate-plus-
scalar node count, `-depth` is its maximum root-relative depth, `-width` is its
checked-core aggregate width (including two components per map entry), and
`-scalar-bytes` is the sum of exact executable scalar costs
`4 * (count (str scalar))`; the
observed counter is the maximum across the seven module rows. C6 additionally
measures the cardinality of each module's core-node vector and retains the
maximum. BOOT4 walks request rows in module then request ordinal order and
retains the maximum per-module request cardinality. The remaining semantic
counters are the exact per-module, per-fragment, or per-form maxima named by
their literal MEASUREMENT-SPECS row; no counter may be incremented from a
different collection or combined across module boundaries.

Each traversal emits a dense `TRACE` whose rows have exactly `:ordinal
:counter-key :coordinate :observed-value :derivation`. Ordinal is zero-based.
There is exactly one row for every operation counter key in counter-key order.
Coordinate is the literal path or ordered path vector to the measured
collection in the authenticated subject. Observed value is the recomputed
nonnegative per-module or per-fragment maximum.
Derivation is exactly one of `:maximum-row-cardinality :maximum-row-value
:maximum-row-carrier-node-count :maximum-row-carrier-depth
:maximum-row-carrier-width :maximum-row-executable-scalar-cost` and must match the literal
MEASUREMENT-SPECS row. Trace root is
`H([:gravity/sh07-b51-fixed-fuel-trace-root-v1 TRACE])`.

`FUEL-RECEIPT` has exactly `:operation-id :bounds-id :trace :trace-root
:operation-counts :within-bounds :receipt-id`. Bounds ID is
`H([:gravity/sh07-b51-fixed-fuel-bounds-v1 FIXED-FUEL])`. Operation counts are
the exact map from each trace counter key to its observed value; keys equal the
operation counter keys and every value is no greater than FIXED-FUEL at that
key. Within-bounds is true. Receipt ID is
`H([:gravity/sh07-b51-fixed-fuel-receipt-v1
RECEIPT-WITHOUT-RECEIPT-ID])`. A and B independently reconstruct identical
manifests, traces, roots, counts, and resulting witness bytes.

`FUEL-RECEIPTS` is the complete seven-row vector of those constructed receipts
in operation ordinal order; WITNESS stores that exact vector at
`:fuel-receipts`. It corresponds one-to-one with OPERATION-MANIFEST. Every nested witness carries the exact ordered
`:operation-ids` subsequence whose owner equals that witness. An empty or
fabricated trace, zero count contradicted by the subject traversal, missing or
extra operation, unknown kind, wrong subject/source root, counter mismatch,
unbounded walk, output not reproducible from the measured traversal, or fuel
exhaustion rejects and cannot produce accepted evidence.

Every nested stage witness also carries its owner's exact `:fuel-subject-id`.
`STAGE-SUBJECT-JOIN-SPECS` is byte-for-byte `STAGE-SEMANTIC-SPECS`; there is no
second projection table. For join row J, final
`WITNESS[(:stage-key J) :fuel-subject-id]` equals
`LOWERING-PRODUCT[:fuel-subjects (:ordinal J) :subject-id]`, its sole operation
ID equals OPERATION-MANIFEST at that ordinal, and that operation's
`:lowering-product-id` equals the enclosing child LOWERING-PRODUCT
`:product-id`.

M computes `POST-STAGE-SEMANTIC(J)` by applying the exact
`PROJECT-STAGE-SEMANTIC` grammar above to the final stage witness, then computes
`POST-STAGE-SEMANTIC-ROOT(J)` with the exact registered H equation above. It
requires `POST-STAGE-SEMANTIC(J) = PRE-STAGE-SEMANTIC(J)` byte-for-byte and
`POST-STAGE-SEMANTIC-ROOT(J) =
LOWERING-PRODUCT[:fuel-subjects (:ordinal J) :stage-semantic-root]`. A and B
perform the same recomputation before returning their certificates. No
separately supplied stage-semantic map, subject collection, root, or second
parse is admitted. The complete final witness still validates before this
projection is taken. Changing any selected final field, subject collection,
semantic root, subject ID, or product join therefore rejects even when both
children agree; the deliberately unselected receipt-dependent fields remain
governed by their exact post-receipt schemas and roots below.

The L1 witness has exactly `:schema :fuel-subject-id :document :source-units
:grammar-change :operation-ids :witness-id`; schema is
`:gravity/sh07-b51-l1-witness-v1`;
document is SOURCE-DESCRIPTORS ordinal 0. DOCUMENT-DESCRIPTORS ordinal 8 from
the separate contract-document session must have the same path/mode/OID/count/
raw SHA byte-for-byte; neither value is SOURCE-DESCRIPTORS ordinal 8. Each
source unit has exactly `:ordinal :descriptor :source-order :syntax`;
descriptor has exactly
`:path :extension :project-relative-path :encoding :source-id :bytes-hash
:project-root :reader-options :features :source-kind`, with encoding `:utf-8`,
bytes hash equal the descriptor raw SHA, and all other values derived from the
strict C snapshot. Source units enumerate exactly GRAVITY-SOURCE-ORDINALS
`[2 3 4 5 6 7 8 9 10 11]`; descriptors 0 and 1 remain contract-document
authority and are not misclassified as Gravity source units. Source order and
syntax are exactly the PN-SEED projection and MR equation above; the syntax
entries retain the actual `:form :form-id :span :metadata :reader-origin
:generated-origin` fields and invent no span, origin, metadata, or syntax ID.
Grammar change is exactly
`{:changed false :baseline-document-raw-sha256
"sha256:e31900e670779fdc908d2059e43df84dd9f5543b73aefa88114cc13d0bcc9c39"
:candidate-document-raw-sha256 SOURCE-DESCRIPTORS[0][:raw-sha256]}` and the two
raw hashes must be equal. The baseline literal is pinned to main commit
`09c795f0c0854d2d8ab542c92b2116515e096406`, path
`docs/phase-01-core-language/011-l1-surface-syntax-specification.md`, blob OID
`8a1940c320275fafef0703862149dd6a87af2b43`, and byte count `12627`; it is an
R value rather than a free semantic ID.

The C2 witness has exactly `:schema :fuel-subject-id :snapshot :units
:source-map :diagnostics :literal-records :reader-extensions :operation-ids
:witness-id`; schema is
`:gravity/sh07-b51-c2-witness-v1`; snapshot has exactly
`:commit :tree :source-descriptors-root :event-root`. Each unit has exactly
`:ordinal :path :extension :project-relative-path :encoding :source-id
:bytes-hash :project-root :reader-options :features :source-kind :tokens :forms
:source-map :diagnostics :literal-records :reader-extensions`; encoding is
`:utf-8`. Tokens, forms, source map, and diagnostics have exactly `:entries
:root`; entries are the complete PN-TOKEN, PN-FORM, PN-FORM-applied source-map,
and PN-DIAGNOSTIC values defined above, and roots use their four distinct
literal MR kinds. Literal records and reader extensions are the complete
PN-LITERAL and PN-EXTENSION vectors. The outer source-map/diagnostics maps and
literal/extension vectors are the exact dense per-unit concatenations above;
they are not independently supplied summaries. Snapshot roots equal the
authenticated source-session descriptor and event roots; units enumerate
exactly GRAVITY-SOURCE-ORDINALS once, in ordinal order. No invented token,
syntax, span, origin, literal, extension, handler, or diagnostic identity is
admitted.

The L2 witness has exactly `:schema :fuel-subject-id :owner-selection :core-forms
:evaluation-order-root
:effects-root :capabilities-root :external-bindings :edge-authority
:diagnostics :negative-fixtures :operation-ids :witness-id`;
schema is `:gravity/sh07-b51-l2-witness-v1`. Each core form has exactly
`:ordinal :source-id :top-level-form-ordinal :owning-defn-id :form-id
:owning-root-id :evaluation-index :effects :capabilities :profile
:semantic-id`; profile is `:meta`, and effect/capability vectors remain
distinct. Owning defn/root selection walks only the source-ordered top-level
forms of the descriptor's parsed module, selects the unique accepted Attempt
32 defn owner and its declared body root, and then descends from that root; a
nested same-named form, scan-first match, missing owner, or ambiguity rejects.
Owner selection has exactly `:root-specs :root-specs-source-id
:bundle-core-public-roots-projection :bundle-core-public-roots-id :registry :candidates :selections
:rejected :registry-root`; root specs are the exact eight rows keyed by
`[:root-ordinal :function :arity :module-ordinal]`. Registry rows have exactly
`:ordinal :source-id :module-ordinal :top-level-form-ordinal :declared-name
:arity :defn-id :sh05-expanded-def-id :sh05-expanded-fn-id
:declared-body-root-id :registry-id`. For each
root spec, selections contains exactly one map with `:root-spec :registry-id
:defn-id :declared-body-root-id`; it selects the unique source-ordered top-level
defn plus its authenticated SH05 expanded def/fn and declared body root.
Candidates contain every matching top-level registry ID; exactly one is
required for every ROOT-SPECS row and rejected is therefore exactly empty.
Missing, duplicate, nested, scan-first, or ambiguous selection rejects.
`OWNER-REGISTRY-ROOT` is exactly
`MR(:l2-owner-registry,
[ROOT-SPECS REGISTRY CANDIDATES SELECTIONS REJECTED])`. Every core form's
`owning-defn-id` and `owning-root-id` equal its unique full OWNER-REGISTRY row;
the eight selections are the public-root subset rather than an assertion that
the modules contain only eight defns. No free H value is admitted. The exact
registry, core-form, evaluation, effect, and capability equations are the
pre-stage equations above.

`ROOT-SPECS` is literally the authenticated 00945 value:

```clojure
[{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
  :module-ordinal 3}
 {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
  :module-ordinal 3}
 {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
  :module-ordinal 2}
 {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
  :module-ordinal 3}
 {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
  :module-ordinal 3}
 {:root-ordinal 6
  :function 'sh07-b51-build-independent-verifier-binding :arity 6
  :module-ordinal 5}
 {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
  :module-ordinal 3}
 {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
  :module-ordinal 6}]
```

Every row has exactly `:root-ordinal :function :arity :module-ordinal`.
`SOURCE-ROOT-SPECS` is the value obtained by strictly reading
`p15-s23-sh07-b51-public-root-specs` from the authenticated 00945 C object;
it must equal ROOT-SPECS byte-for-byte. Root-specs source ID is
`H([:gravity/sh07-b51-public-root-specs-id-v1 SOURCE-ROOT-SPECS])`.

`BUNDLE-ROOT-SPECS` is computed from `BUNDLE-CORE[:public-roots]` in root-ordinal
order. Each bundle root has exactly `:root-ordinal :function :arity :plan-id`.
For each row, the unique `BUNDLE-CORE[:module-plans]` row whose `:plan-id`
equals the root's `:plan-id` supplies `:module-ordinal`; zero or multiple
matches reject. The projection is exactly
`(mapv (fn [root] {:root-ordinal (:root-ordinal root)
:function (:function root) :arity (:arity root)
:module-ordinal (:module-ordinal UNIQUE-MODULE-PLAN)})
BUNDLE-CORE[:public-roots])` and must equal ROOT-SPECS byte-for-byte.
Bundle-core public-roots ID is
`H([:gravity/sh07-b51-bundle-core-public-roots-id-v1
BUNDLE-ROOT-SPECS])`. Owner selection stores that complete projection at
`:bundle-core-public-roots-projection`. These IDs and ROOT-SPECS enter
MR(:l2-owner-registry, ...); a different eight-row vector cannot satisfy owner
selection.

L2 external resolution authority and SH06 cross-module call authority are two
different projections. They are never equated. The first admits core,
declared-import, and declared-namespace resolutions that legitimately have no
provider fragment in the consumer Q_i. The second proves the exact subset of
operator resolutions that crosses between the seven governed B51 modules and
therefore has both a consumer and provider fragment.

For each module Q_i, `Q-REFERENCE-ROWS_i` is constructed by visiting Q_i
`:fragment-manifest` in vector order and, within each fragment G, visiting
G `:resolution-reference-syntax-ids` in vector order. For each visited ID there
must be exactly one resolution R in Q_i `:resolution-table`, exactly one binding
B in Q_i `:binding-table` whose `:binding-id` is R `:binding-id`, exactly one
form F whose `:syntax-id` is R `:reference-syntax-id`, and exactly one containing
fragment G. The resolution and binding have their complete existing closed Q
keysets. R's symbol, binding IDs, source span, and qualified/unqualified name
relationship must agree with B and F under the exact checked-core index,
binding-shape, binding-name, and resolution-record predicates below. The older
Root6 shortcut `sh07-b51-r6-resolution-record-authentic?` is not used because
it incorrectly equates every qualified reference spelling with an unqualified
binding name. Zero or multiple joins reject.

For each `(Q_i,G,R,B)`, `LOCAL-COUNT` and `EXTERNAL-COUNT` are the exact
membership counts of B `:binding-id` in G `:local-binding-ids` and
`:external-binding-ids`. Let `R_i` be the same module transaction's complete
`:resolution-artifact`, `RAW_i` the complete value at
`R_i[:gravity-resolution-boundary :authenticated-resolution-request]`, and
`AN_i` the complete value at
`R_i[:gravity-resolution-boundary :resolved-analysis]`. The fresh SH06 verifier
must accept R_i, and `AN_i[:module-contract]` must equal `RAW_i[:module]`
byte-for-byte. Define
`Q-REPLAY_i = sh07-core-authenticated-request(R_i)` and require
`Q-REPLAY_i = Q_i` byte-for-byte before selecting any row. `ANALYSIS-BINDING`,
written A below, is the unique row in `AN_i[:binding-table]` whose `:binding-id`
is B `:upstream-binding-id`; B must be the unique row in
`Q-REPLAY_i[:binding-table]` with that upstream ID. External classification
never admits a lexical A. `DEF-SYNTAX(A,R_i,Q_i)` is exactly the existing
`sh07-b51-r6-project-definition-syntax-id` result when called with carrier
`{:authenticated-sh06-request Q_i :sh06-resolution-artifact R_i}`, A, and
Q_i source revision. The exact replay is:

```clojure
DEF-SYNTAX(A,R_i,Q_i) =
  sh07-b51-r6-project-definition-syntax-id(
    {:authenticated-sh06-request Q_i
     :sh06-resolution-artifact R_i},
    A, Q_i[:lineage :source-revision-id])
SEM(A,R_i,Q_i) =
  {:capabilities A[:capabilities] :package A[:package] :name A[:name]
   :target-set A[:target-set] :upstream-binding-id A[:binding-id]
   :type-ref A[:type-ref]
   :definition-artifact-id A[:definition-artifact-id]
   :effects A[:effects] :safety A[:safety] :kind A[:kind]
   :semantic-span A[:semantic-span] :scope-id nil
   :definition-syntax-id (DEF-SYNTAX A R_i Q_i)
   :profile-set A[:profile-set]
   :namespace A[:namespace] :visibility A[:visibility]
   :binding-class A[:binding-class]}
B-EXPECTED(A,R_i,Q_i) =
  (assoc SEM(A,R_i,Q_i) :binding-id
    H([:gravity/sh07-projected-sh06-binding-v1
       {:source-revision-id Q_i[:lineage :source-revision-id]
        :binding (dissoc SEM(A,R_i,Q_i)
                         :upstream-binding-id :definition-artifact-id)}]))
require B = B-EXPECTED(A,R_i,Q_i)
```

DEF-SYNTAX must be the same unique value in Q-REPLAY_i's complete
form/declaration projection; absence or ambiguity rejects. The full
upstream and projected binding-ID maps must both be bijective. No locally
restated partial semantic map or caller-supplied syntax/scope ID is admitted.

`DECLARATION-BODY(A)` is exactly `(dissoc A :artifact :binding-id)`. Define the
three declaration selections as rows byte-equal to DECLARATION-BODY(A) in,
respectively, `RAW_i[:definitions]`, `RAW_i[:import-bindings]`, and
`RAW_i[:core-bindings]`. Their counts are `DEFINITION-COUNT`, `IMPORT-COUNT`,
and `CORE-COUNT`. These are the only declared-binding membership sets. They are
derived from the authenticated SH06 request; the architecture neither reads nor
invents the latent Root6 names `:declared-import-binding-ids`,
`:declared-alias-binding-ids`, or `:declared-core-binding-ids`, because those
keys are absent from the exact SH06 module-contract schema.

`DEPENDENCY-CANDIDATES` is the selection from `RAW_i[:imports]` whose
`:namespace` equals A namespace and whose `:dependency-artifact-id` equals A
definition artifact ID. `LOCAL-PROVIDERS` is the Q_i fragment selection
containing B `:binding-id` in `:local-binding-ids`. `ALIAS-CANDIDATES` is the
Q_i alias-table selection whose `:alias` equals the namespace portion of R
symbol, whose `:namespace` equals A and B namespace, whose
`:dependency-artifact-id` equals A and B definition artifact ID, whose profile
occurs in B profile set, and whose target vector is a subset of B target set;
its alias must also occur exactly once in G `:alias-names`. Alias rows retain
their authentic exact `:alias :dependency-artifact-id :kind :namespace
:profile :targets` keyset. `MEMBERSHIP-COUNT(V,X)` is exactly
`sh07-b51-r6-vector-membership-count(V,X)`, the bounded equality count over V;
it is not caller supplied.

The external classifier is a literal function of one authentic reference
tuple. `SEM`, `B-EXPECTED`, `DECLARATION-BODY`, and `MEMBERSHIP-COUNT` are the
exact equations already printed. Its only successful branches are:

```clojure
EXTERNAL-CLASSIFICATION([M Q G fragment-resolution-ordinal R B F]) =
  (let [R_i (:resolution-artifact M)
        RAW_i (get-in R_i
                      [:gravity-resolution-boundary
                       :authenticated-resolution-request])
        AN_i (get-in R_i
                     [:gravity-resolution-boundary :resolved-analysis])
        Q-REPLAY_i (sh07-core-authenticated-request R_i)
        CHECKED-INDEXES (sh07-index-request Q-REPLAY_i)
        AUTHORITY-INDEXES (sh07-b51-index-request Q-REPLAY_i)
        A (UNIQUE-BY :binding-id (:upstream-binding-id B)
                     (:binding-table AN_i))
        DEFINITION-ROWS
        (filterv #(= % (DECLARATION-BODY A)) (:definitions RAW_i))
        IMPORT-ROWS
        (filterv #(= % (DECLARATION-BODY A)) (:import-bindings RAW_i))
        CORE-ROWS
        (filterv #(= % (DECLARATION-BODY A)) (:core-bindings RAW_i))
        DEFINITION-COUNT (count DEFINITION-ROWS)
        IMPORT-COUNT (count IMPORT-ROWS)
        CORE-COUNT (count CORE-ROWS)
        LOCAL-COUNT
        (MEMBERSHIP-COUNT (:local-binding-ids G) (:binding-id B))
        EXTERNAL-COUNT
        (MEMBERSHIP-COUNT (:external-binding-ids G) (:binding-id B))
        DEPENDENCY-CANDIDATES
        (filterv #(and (= (:namespace %) (:namespace A))
                       (= (:dependency-artifact-id %)
                          (:definition-artifact-id A)))
                 (:imports RAW_i))
        LOCAL-PROVIDERS
        (filterv #(= 1 (MEMBERSHIP-COUNT (:local-binding-ids %)
                                        (:binding-id B)))
                 (:fragment-manifest Q-REPLAY_i))
        ALIAS-CANDIDATES
        (filterv
          #(and (= (:alias %) (some-> R :symbol namespace symbol))
                (= (:namespace %) (:namespace A) (:namespace B))
                (= (:dependency-artifact-id %)
                   (:definition-artifact-id A)
                   (:definition-artifact-id B))
                (= 1 (MEMBERSHIP-COUNT (:profile-set B) (:profile %)))
                (sh07-vector-subset? (:targets %) (:target-set B))
                (= 1 (MEMBERSHIP-COUNT (:alias-names G) (:alias %))))
          (:alias-table Q-REPLAY_i))
        BASE-VALID?
        (and (= :passed
                (:status (sh06-resolution-artifact-verification R_i)))
             (= Q-REPLAY_i Q)
             (= (:module-contract AN_i) (:module RAW_i))
             (= B (B-EXPECTED A R_i Q-REPLAY_i))
             (= [LOCAL-COUNT EXTERNAL-COUNT] [0 1])
             (= (:binding-class A) (:binding-class B))
             (= (:name A) (:name B))
             (= (:namespace A) (:namespace B))
             (= (:definition-artifact-id A)
                (:definition-artifact-id B))
             (= 1 (+ DEFINITION-COUNT IMPORT-COUNT CORE-COUNT))
             (sh07-binding-shape? B)
             (every? sh07-alias-shape? (:alias-table Q-REPLAY_i))
             (sh07-resolution-records-valid?
               Q-REPLAY_i CHECKED-INDEXES)
             (= :authenticated-external
                (sh07-b51-authorize-resolution
                  R_i Q-REPLAY_i AUTHORITY-INDEXES R)))]
    (if BASE-VALID?
      (cond
  (and (= :current-namespace-binding (:resolution-order R))
       (= :namespace (:binding-class A) (:binding-class B))
       (contains? #{:public :private} (:visibility A))
       (= 1 DEFINITION-COUNT)
       (= 0 IMPORT-COUNT CORE-COUNT)
       (= (:namespace A) (get-in RAW_i [:module :namespace]))
       (= (:namespace A) (get-in Q [:module :namespace]))
       (or (= 1 (count LOCAL-PROVIDERS))
           (and (= 0 (count LOCAL-PROVIDERS))
                (sh07-b51-no-form-namespace-declaration?
                  (first DEFINITION-ROWS) A))))
  :authenticated-current-namespace

  (and (= :alias-qualified-required-binding (:resolution-order R))
       (= :import (:binding-class A) (:binding-class B))
       (= :public (:visibility A))
       (= 0 DEFINITION-COUNT CORE-COUNT)
       (= 1 IMPORT-COUNT)
       (= 0 (count LOCAL-PROVIDERS))
       (= 1 (count ALIAS-CANDIDATES))
       (= 1 (count DEPENDENCY-CANDIDATES))
       (= (:alias (first DEPENDENCY-CANDIDATES))
          (symbol (namespace (:symbol R)))))
  :authenticated-alias

  (and (= :fully-qualified-namespace-binding (:resolution-order R))
       (contains? #{:namespace :import} (:binding-class A))
       (= (:binding-class A) (:binding-class B))
       (= 1 (+ DEFINITION-COUNT IMPORT-COUNT))
       (= 0 CORE-COUNT)
       (= (str (:namespace A) "/" (:name A)) (str (:symbol R)))
       (or (and (= :namespace (:binding-class A))
                (contains? #{:public :private} (:visibility A))
                (= 1 DEFINITION-COUNT)
                (= (:namespace A)
                   (get-in RAW_i [:module :namespace])
                   (get-in Q [:module :namespace]))
                (or (= 1 (count LOCAL-PROVIDERS))
                    (and (= 0 (count LOCAL-PROVIDERS))
                         (sh07-b51-no-form-namespace-declaration?
                           (first DEFINITION-ROWS) A))))
           (and (= :import (:binding-class A))
                (= :public (:visibility A))
                (= 1 IMPORT-COUNT)
                (= 0 (count LOCAL-PROVIDERS))
                (= 1 (count DEPENDENCY-CANDIDATES)))))
  :authenticated-fully-qualified

  (and (= :profile-allowed-core-binding (:resolution-order R))
       (= :core (:binding-class A) (:binding-class B))
       (= :public (:visibility A))
       (= 'gravity.core (:namespace A))
       (= 0 DEFINITION-COUNT IMPORT-COUNT)
       (= 1 CORE-COUNT)
       (= 0 (count LOCAL-PROVIDERS)))
  :authenticated-core

  (and (= :profile-allowed-core-binding (:resolution-order R))
       (= :import (:binding-class A) (:binding-class B))
       (= :public (:visibility A))
       (= 0 DEFINITION-COUNT CORE-COUNT)
       (= 1 IMPORT-COUNT)
       (= 0 (count LOCAL-PROVIDERS))
       (= 1 (count DEPENDENCY-CANDIDATES))
       (= :public (:visibility (first DEPENDENCY-CANDIDATES)))
       (or (= [:all] (:refer (first DEPENDENCY-CANDIDATES)))
           (= 1 (MEMBERSHIP-COUNT
                 (:refer (first DEPENDENCY-CANDIDATES)) (:name A)))))
  :authenticated-referred-import

  (and (= :explicit-foreign-import-binding (:resolution-order R))
       (= :import (:binding-class A) (:binding-class B))
       (= :foreign (:kind A) (:kind B))
       (= :public (:visibility A))
       (= 0 DEFINITION-COUNT CORE-COUNT)
       (= 1 IMPORT-COUNT)
       (= 0 (count LOCAL-PROVIDERS))
       (= 1 (count DEPENDENCY-CANDIDATES))
       (= :foreign (:kind (first DEPENDENCY-CANDIDATES)))
       (= true (:foreign-record-complete?
                 (first DEPENDENCY-CANDIDATES))))
  :authenticated-import

        :else :rejected)
      :rejected))
```

Every branch additionally requires the exact
`sh07-b51-authorize-resolution` replay to return `:authenticated-external`.
That governed predicate authenticates the raw reference qualifier, dependency
analysis product, form-backed or no-form namespace declaration, import/core
visibility, foreign completeness, binding projection, target, alias, and
resolution record from R_i/Q bytes. The separately printed binding-shape,
alias-shape, and resolution-table checks must also pass. `ANALYSIS-BINDING` must exist
exactly once, A and B must have equal class/name/namespace/definition-artifact
coordinates, and exactly one of the three declaration counts must be one.
This classifier uses only fields present in authenticated SH06/Q bytes. Every
Q_i fragment's
`:external-binding-ids` must equal the stable first-occurrence-distinct
projection of the binding IDs of its successfully classified external
reference rows. A lexical external reference, an ambiguous local provider or
alias/dependency, a row absent from its authenticated declaration source, an
unverified import/core binding, or a binding/name mismatch rejects.

Each `Q-EXTERNAL-OCCURRENCES` row has exactly
`:ordinal :consumer-module-ordinal :consumer-source-id :consumer-fragment-id
:fragment-ordinal :fragment-resolution-ordinal :reference-syntax-id
:binding-id :upstream-binding-id :binding-class :symbol :position
:resolution-order :authority-basis`. Rows are the successfully classified Q
external reference rows in module/fragment/resolution order;
`:ordinal` is dense from zero, fragment-resolution ordinal is the visited ID's
position in G `:resolution-reference-syntax-ids`, consumer source ID is the
module's authenticated SH06 semantic projection ID, and authority basis is
exactly the successful classifier result from the six closed values above.
Define:

```clojure
Q-REFERENCE-ROWS =
  (vec
    (for [M MODULE-TRANSACTIONS
          :let [Q (:authenticated-core-request M)]
          G (:fragment-manifest Q)
          fragment-resolution-ordinal
          (range (count (:resolution-reference-syntax-ids G)))
          :let [reference-id
                (nth (:resolution-reference-syntax-ids G)
                     fragment-resolution-ordinal)
                R (UNIQUE-BY :reference-syntax-id
                             reference-id (:resolution-table Q))
                B (UNIQUE-BY :binding-id (:binding-id R)
                             (:binding-table Q))
                F (UNIQUE-BY :syntax-id reference-id (:forms Q))]]
      [M Q G fragment-resolution-ordinal R B F]))
AUTHENTIC-EXTERNAL-REFERENCES =
  (filterv #(not= :rejected (EXTERNAL-CLASSIFICATION %)) Q-REFERENCE-ROWS)
Q-EXTERNAL-OCCURRENCES =
  (mapv
    (fn [ordinal [M Q G fragment-resolution-ordinal R B F]]
      {:ordinal ordinal
       :consumer-module-ordinal (:module-ordinal M)
       :consumer-source-id
       (get-in Q [:lineage :sh06-semantic-projection-id])
       :consumer-fragment-id (:fragment-id G)
       :fragment-ordinal (:ordinal G)
       :fragment-resolution-ordinal fragment-resolution-ordinal
       :reference-syntax-id (:reference-syntax-id R)
       :binding-id (:binding-id B)
       :upstream-binding-id (:upstream-binding-id B)
       :binding-class (:binding-class B)
       :symbol (:symbol R)
       :position (:position R)
       :resolution-order (:resolution-order R)
       :authority-basis (EXTERNAL-CLASSIFICATION
                          [M Q G fragment-resolution-ordinal R B F])})
    (range) AUTHENTIC-EXTERNAL-REFERENCES)
Q-EXTERNAL-ROOT = MR(:l2-external-edges, Q-EXTERNAL-OCCURRENCES)
L2-EXTERNAL-BINDINGS =
  {:entries Q-EXTERNAL-OCCURRENCES :root Q-EXTERNAL-ROOT}
```

`UNIQUE-BY(K,V,ROWS)` returns the sole complete row whose K value is V and
rejects zero or multiple matches. `EXTERNAL-CLASSIFICATION` is exactly the
closed cond above, applied to the tuple's authentic joins; it has no other
successful value.

Q-EXTERNAL-OCCURRENCES is the complete per-reference census; a fragment's
`:external-binding-ids` remains its stable-distinct binding projection rather
than being misrepresented as an edge census.

The only literal link-product invocation is the I9 object-mode wrapper already
defined by this architecture. Its `(C,T)` arguments are the immutable Git
commit/tree pair; the wrapper opens, finalizes, and returns the receipt for its
own governed source session. Calling the legacy 00945 zero-argument entrypoint
directly is forbidden because its current implementation can resolve live
paths.

```clojure
require (set (keys RECEIPTED-BUNDLE-CORE-PRODUCTS)) =
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id
    :source-forms :module-transactions :source-read-receipt}
AUTHENTIC-M0 = RECEIPTED-BUNDLE-CORE-PRODUCTS[:m0s]
AUTHENTIC-M1 = RECEIPTED-BUNDLE-CORE-PRODUCTS[:m1]
BUNDLE-CORE = RECEIPTED-BUNDLE-CORE-PRODUCTS[:bundle-core]
BUNDLE-CORE-ID =
  RECEIPTED-BUNDLE-CORE-PRODUCTS[:execution-bundle-core-id]
require (set (keys BUNDLE-CORE)) =
  #{:domain :schema-version :manifest-id :module-plans :routes :public-roots}
require BUNDLE-CORE[:domain] =
  :gravity/sh07-b51-execution-bundle-core-id-v1
require BUNDLE-CORE-ID =
  p15-s23-sh07-b51-canonical-id(BUNDLE-CORE) = H(BUNDLE-CORE)
require (mapv :source-sha256 AUTHENTIC-M0) =
  (mapv :raw-sha256 (subvec SOURCE-DESCRIPTORS 3 10))
require (count AUTHENTIC-M1[:edges]) = 1480
require AUTHENTIC-M1[:manifest-id] = BUNDLE-CORE[:manifest-id]
```

During this invocation, the wrapper's seven `:callsite-census` reads and seven
`:link-authority` reads are exactly SOURCE-EVENTS ordinals 10-23, and each read
is supplied only the C/T blob bound by the corresponding SOURCE-DESCRIPTORS
ordinal 3-9. The same session first performs the seven module-validation reads
at event ordinals 3-9. A direct live-path read, legacy zero-argument invocation,
or invocation outside the wrapper's private session rejects. The returned
keyset is exact. `:source-forms` is a semantic envelope member and the session's
host-only source handles are not returned; neither enters an identity preimage
except through the separately specified normalized transcript. AUTHENTIC-M0 is the seven complete freshly recomputed
module-source descriptors and AUTHENTIC-M1 is the complete freshly recomputed
`:gravity/sh07-b51-link-manifest-v1`. M1 has exactly `:artifact :schema-version :manifest-id
:module-source-ids :edges`; its edge rows have exactly `:ordinal
:caller-module-source-id :caller-function :caller-function-source-hash
:callsite-path :callee-module-source-id :callee-function
:callee-function-source-hash :arity :owner-edge :authorization`. Existing M1
verification proves dense ordinals, canonical order, exact source hashes,
`:authenticated-b51-namespace-external` authorization, and the closed
1,480-edge governed census.

The carrier binds all seven module transactions rather than a singular
unrelated SH06 artifact. Define these complete vectors in module order:

```clojure
SH06-RESOLUTION-ARTIFACTS =
  (mapv :resolution-artifact MODULE-TRANSACTIONS)
SH06-RESOLUTION-ARTIFACT-IDS =
  (mapv :artifact-id SH06-RESOLUTION-ARTIFACTS)
SH06-RESOLUTION-VERIFICATIONS =
  (mapv sh06-resolution-artifact-verification SH06-RESOLUTION-ARTIFACTS)
SH06-SEMANTIC-PROJECTION-IDS =
  (mapv #(get-in % [:authenticated-core-request :lineage
                    :sh06-semantic-projection-id]) MODULE-TRANSACTIONS)
RESOLUTION-ARTIFACT-PATHS =
  (mapv #(vector :resolution-artifacts %) (range 7))
SEMANTIC-PROJECTION-PATHS =
  (mapv #(vector :semantic-projection-ids %) (range 7))
LINK-AUTHORITY-EVENT-PRODUCT-IDS =
  (mapv :product-id (subvec SOURCE-EVENTS 17 24))
LINK-AUTHORITY-ID =
  H([:gravity/sh07-b51-sh06-link-authority-id-v1
     {:resolution-artifact-ids SH06-RESOLUTION-ARTIFACT-IDS
      :semantic-projection-ids SH06-SEMANTIC-PROJECTION-IDS
      :event-product-ids LINK-AUTHORITY-EVENT-PRODUCT-IDS
      :manifest-id AUTHENTIC-M1[:manifest-id]}])
SH06-AUTHORITY-CARRIER-BASE =
  {:content C/T
   :source-descriptors (subvec SOURCE-DESCRIPTORS 3 10)
   :bundle-core-id BUNDLE-CORE-ID
   :resolution-artifact-paths RESOLUTION-ARTIFACT-PATHS
   :resolution-artifacts SH06-RESOLUTION-ARTIFACTS
   :resolution-verifications SH06-RESOLUTION-VERIFICATIONS
   :semantic-projection-paths SEMANTIC-PROJECTION-PATHS
   :semantic-projection-ids SH06-SEMANTIC-PROJECTION-IDS
   :module-source-descriptors AUTHENTIC-M0
   :link-manifest AUTHENTIC-M1
   :link-authority-event-product-ids LINK-AUTHORITY-EVENT-PRODUCT-IDS
   :link-authority-id LINK-AUTHORITY-ID
   :expected-edge-census AUTHENTIC-M1[:edges]}
SH06-AUTHORITY-CARRIER-ID =
  H([:gravity/sh07-b51-sh06-authority-carrier-id-v1
     SH06-AUTHORITY-CARRIER-BASE])
SH06-AUTHORITY-CARRIER =
  (assoc SH06-AUTHORITY-CARRIER-BASE
         :carrier-id SH06-AUTHORITY-CARRIER-ID)
CARRIER-KEYSET =
  #{:content :source-descriptors :bundle-core-id
    :resolution-artifact-paths :resolution-artifacts
    :resolution-verifications :semantic-projection-paths
    :semantic-projection-ids :module-source-descriptors :link-manifest
    :link-authority-event-product-ids :link-authority-id
    :expected-edge-census :carrier-id}
CARRIER-VALID? =
  (and (= (set (keys SH06-AUTHORITY-CARRIER)) CARRIER-KEYSET)
       (= (dissoc SH06-AUTHORITY-CARRIER :carrier-id)
          SH06-AUTHORITY-CARRIER-BASE)
       (= (:carrier-id SH06-AUTHORITY-CARRIER)
          SH06-AUTHORITY-CARRIER-ID)
       (= SH06-AUTHORITY-CARRIER-ID
          H([:gravity/sh07-b51-sh06-authority-carrier-id-v1
             SH06-AUTHORITY-CARRIER-BASE]))
       (= 7 (count SH06-RESOLUTION-ARTIFACTS)
          (count SH06-RESOLUTION-VERIFICATIONS)
          (count SH06-SEMANTIC-PROJECTION-IDS))
       (every? #(= :passed (:status %)) SH06-RESOLUTION-VERIFICATIONS)
       (= (:resolution-artifacts SH06-AUTHORITY-CARRIER)
          SH06-RESOLUTION-ARTIFACTS)
       (= (:resolution-verifications SH06-AUTHORITY-CARRIER)
          SH06-RESOLUTION-VERIFICATIONS)
       (= (:semantic-projection-ids SH06-AUTHORITY-CARRIER)
          SH06-SEMANTIC-PROJECTION-IDS)
       (= (:module-source-descriptors SH06-AUTHORITY-CARRIER) AUTHENTIC-M0)
       (= (:link-manifest SH06-AUTHORITY-CARRIER) AUTHENTIC-M1)
       (= (:link-authority-id SH06-AUTHORITY-CARRIER) LINK-AUTHORITY-ID)
       (= (:expected-edge-census SH06-AUTHORITY-CARRIER)
          AUTHENTIC-M1[:edges]))
```

Every artifact has the existing closed SH06 outer keyset and recomputed
artifact ID; every verification is the complete fresh passing result for the
artifact at the same ordinal. Every semantic projection ID equals the ID in the
same transaction's Q_i lineage. `AUTHENTIC-M0` equals the carrier's complete
`:module-source-descriptors`, `AUTHENTIC-M1` equals its complete
`:link-manifest`, and their source IDs agree in module order. No summary,
caller-supplied row, separately sorted copy, singular-artifact substitution, or
path value outside these two literal seven-row path vectors is admitted.

For each Q external occurrence X, F is the unique Q form whose syntax ID is X
`:reference-syntax-id` and P is the unique parent form. A Q cross-call candidate
requires X position `:operator`, P kind `:list`, F as the first P child, and
arity `(dec (count (:child-form-ids P)))`. G must satisfy
`G[:root-form-ids] = [Q_i[:top-level-form-ids][G[:ordinal]]]` byte-for-byte,
and its sole root must be the unique ancestor root reached from P. `TOPIDXc` is
that root's unique index in Q_i `:top-level-form-ids`. `REGc` is the unique full
OWNER-REGISTRY row with X consumer module ordinal, top-level-form ordinal
TOPIDXc, and matching declared defn/root coordinates; it supplies caller
function. An ordinal-only fragment-to-owner shortcut rejects. `M0c` is the unique M0 descriptor
at that module ordinal and must have module equal Q module namespace and source
SHA equal Q lineage source revision ID. Exactly one D in M0c
`:external-bindings` must have `D[:symbol] = B[:name]`; R is joined through the
already required checked-core binding-name predicate, so alias-qualified and
fully-qualified spellings are not falsely equated with the unqualified binding
name. D must also have a different callee module ordinal and the same arity.
Rows failing any condition remain in
Q-EXTERNAL-OCCURRENCES but are not SH06 cross-module calls.

The call-list form P has a unique child-index path `QP` from its Q_i top-level
root, reconstructed by following `:parent-form-id` links and finding each child
exactly once in its parent's `:child-form-ids`. The owning root must be the
authenticated SH05 expansion `(def NAME (fn PARAMS BODY...))`: the fn form is
root child 2, has the recorded introduced-fn origin, and P descends from its
child q where q is at least 2. This follows the literal governed expansion
`(defn name parameters & body) -> (def name (fn parameters & body))`; the body
forms are transported unchanged.

`Q-RAW-CALLSITE-PATH(P)` begins with `[(- q 2)]` and appends the descendant
coordinates from fn child q to P. For list and vector parents, each coordinate
is the child index. For a map parent, pair its Q children as key/value entries,
locate the selected pair uniquely after sorting complete entries by
`pr-str(reader-canonical-value(entry))`, and append the pair's canonical rank
followed by 0 for its key or 1 for its value. For a set parent, locate the
selected complete child uniquely after sorting values by
`pr-str(reader-canonical-value(value))` and append its canonical rank. The
complete child value is reconstructed from the closed Q form subtree; a missing,
duplicate, non-descendant, malformed map pair, or canonical comparison collision
rejects. This is exactly the recursive coordinate grammar used by the raw M1
source callsite census, including its map and set ordering.

`UNIQUE-WHERE(PRED,ROWS)` evaluates PRED once on every complete row, returns the
sole row for which it is true, and rejects unless exactly one such row exists.
`UNIQUE-INDEX(V,X)` returns the sole zero-based index at which V equals X and
rejects unless there is exactly one. With those selectors, `Q-CROSS-CALLS` is
the following literal vector in X `:ordinal` order. `TOP` and `TOPIDX` are the
acyclic unique-parent selectors already defined for OWNER-REGISTRY. The
successful `Q-RAW-CALLSITE-PATH` call includes the authenticated def/fn/body
checks and canonical collection-coordinate checks stated above; it returns no
value on rejection.

```clojure
Q-CROSS-CALLS =
  (vec
    (for [X Q-EXTERNAL-OCCURRENCES
          :let [[M Q G fragment-resolution-ordinal R B F]
                (nth AUTHENTIC-EXTERNAL-REFERENCES (:ordinal X))]
          :when (= X (nth Q-EXTERNAL-OCCURRENCES (:ordinal X)))
          :when (= [(:fragment-resolution-ordinal X)
                    (:reference-syntax-id X) (:binding-id X)]
                   [fragment-resolution-ordinal
                    (:reference-syntax-id R) (:binding-id B)])
          :when (= :operator (:position X) (:position R))
          :let [P (UNIQUE-BY :form-id (:parent-form-id F) (:forms Q))]
          :when (and (= :list (:kind P))
                     (= (:form-id F) (first (:child-form-ids P))))
          :let [TOPIDXc (TOPIDX Q P)
                Fdef (TOP Q P)]
          :when (= (:root-form-ids G)
                   [(nth (:top-level-form-ids Q) (:ordinal G))]
                   [(:form-id Fdef)])
          :let [REGc
                (UNIQUE-WHERE
                  #(and (= (:module-ordinal %) (:consumer-module-ordinal X))
                        (= (:top-level-form-ordinal %) TOPIDXc)
                        (= (:sh05-expanded-def-id %) (:syntax-id Fdef)))
                  OWNER-REGISTRY)
                TRACEc
                (UNIQUE-WHERE
                  #(and (= 'defn (:macro %))
                        (= (:output-def-syntax-id %)
                           (:sh05-expanded-def-id REGc))
                        (= (:introduced-fn-syntax-id %)
                           (:sh05-expanded-fn-id REGc)))
                  (:macro-origin-traces Q))
                Ffn (UNIQUE-BY :syntax-id (:introduced-fn-syntax-id TRACEc)
                               (:forms Q))]
          :when (and (= (:parent-form-id Ffn) (:form-id Fdef))
                     (= (:form-id Ffn) (nth (:child-form-ids Fdef) 2))
                     (= (:sh05-expanded-def-id REGc) (:syntax-id Fdef))
                     (= (:sh05-expanded-fn-id REGc) (:syntax-id Ffn))
                     (some? (Q-RAW-CALLSITE-PATH P)))
          :let [M0c
                (UNIQUE-BY :module-ordinal (:consumer-module-ordinal X)
                           AUTHENTIC-M0)]
          :when (and (= (:module M0c) (get-in Q [:module :namespace]))
                     (= (:source-sha256 M0c)
                        (get-in Q [:lineage :source-revision-id])))
          :let [D
                (UNIQUE-WHERE
                  #(and (= (:symbol %) (:name B))
                        (not= (:callee-module-ordinal %)
                              (:consumer-module-ordinal X))
                        (= (:arity %)
                           (dec (count (:child-form-ids P)))))
                  (:external-bindings M0c))
                M0p (UNIQUE-BY :module-ordinal (:callee-module-ordinal D)
                               AUTHENTIC-M0)
                DP (UNIQUE-WHERE
                     #(and (= (:name %) (:symbol D))
                           (= (:arity %) (:arity D)))
                     (:definitions M0p))]
          :when (and (= (:module M0p) (:callee-module D))
                     (= (:source-sha256 M0p) (:callee-source-sha256 D))
                     (= (:source-hash DP)
                        (:callee-definition-source-hash D)))]
      [X R B F P G REGc M0c D M0p]))
```

Every X/R/B/F/G coordinate therefore comes from the exact indexed
AUTHENTIC-EXTERNAL-REFERENCES tuple; P, REGc, M0c, D, and M0p each have exactly
one match. No reconstituted summary map or omitted tuple member is admitted.
`Q-NORMALIZED-ROW` below accepts one such tuple only.

For a Q cross call, M0p is the unique AUTHENTIC-M0 row whose module ordinal is
D `:callee-module-ordinal`; its module-source ID and module name must equal D's
callee source/name coordinates. Define the two normalized constructors and
the sole sort operator exactly:

```clojure
Q-NORMALIZED-ROW([X R B F P G REGc M0c D M0p]) =
  {:caller-module-source-id M0c[:module-source-id]
   :caller-function REGc[:declared-name]
   :callsite-path (Q-RAW-CALLSITE-PATH P)
   :callee-module-source-id M0p[:module-source-id]
   :callee-function D[:symbol]
   :arity (dec (count P[:child-form-ids]))
   :owner-edge (OWNER-EDGE M0c[:owner] M0p[:owner])}
M1-NORMALIZED-ROW(E) =
  {:caller-module-source-id E[:caller-module-source-id]
   :caller-function E[:caller-function]
   :callsite-path E[:callsite-path]
   :callee-module-source-id E[:callee-module-source-id]
   :callee-function E[:callee-function]
   :arity E[:arity]
   :owner-edge E[:owner-edge]}
UNSIGNED-W-SORT(KEY, ROWS) =
  (let [ENCODED-KEYS (mapv #(vec (W(KEY(%)))) ROWS)]
    (require (= (count ENCODED-KEYS)
                (count (set ENCODED-KEYS))))
    (vec
      (sort (fn [A B]
              (UNSIGNED-LEXICOGRAPHIC-BYTES
                (W(KEY(A))) (W(KEY(B)))))
            ROWS)))
```

`UNSIGNED-LEXICOGRAPHIC-BYTES` compares each octet as 0 through 255 and uses
the shorter vector first only when it is a proper prefix; it returns a strict
negative/zero/positive comparator result. Equal encoded keys reject before
sorting, so the comparator never chooses between distinct tied rows. The
sorted result is realized as a vector. W is the already
closed wire codec; no host comparator, locale, printed-map order, or alternate
serialization is admitted.

`OWNER-EDGE` is the literal three-case 00945 function: equal owners map
`:r1-producer` to `:producer-internal` and `:r6-independent-verifier` to
`:root6-internal`; caller `:r8-finalizer` plus callee `:r1-producer` maps to
`:root8-to-producer-interface`; every other pair rejects. D `:owner-edge` must
equal that derived value. Each M1 edge E emits the same exact keyset by selecting
its seven corresponding fields, and E `:owner-edge` must equal both D
`:owner-edge` and the derived OWNER-EDGE value.

Define the two complete values before any edge or receipt exists:

```clojure
Q-CROSS-NORMALIZED =
  (UNSIGNED-W-SORT NORMALIZED-KEY
    (mapv Q-NORMALIZED-ROW Q-CROSS-CALLS))
M1-NORMALIZED =
  (UNSIGNED-W-SORT NORMALIZED-KEY
    (mapv M1-NORMALIZED-ROW AUTHENTIC-M1[:edges]))
NORMALIZED-KEY(row) =
  [row[:caller-module-source-id] row[:caller-function]
   row[:callsite-path] row[:callee-module-source-id]
   row[:callee-function] row[:arity] row[:owner-edge]]
```

Both complete vectors must be byte-equal, contain exactly 1,480 rows, and have
1,480 distinct NORMALIZED-KEY values. The pre-receipt pairing constructor is
literal:

```clojure
PAIRING =
  (mapv
    (fn [E]
      (let [QC
            (UNIQUE-WHERE
              #(= (M1-NORMALIZED-ROW E) (Q-NORMALIZED-ROW %))
              Q-CROSS-CALLS)]
        [E QC]))
    AUTHENTIC-M1[:edges])
```

PAIRING is therefore the vector, in M1 ordinal order, of exact `[E QC]` pairs,
where QC is one complete ten-value Q cross-call tuple. It must consume every
M1 edge and every Q cross call exactly once.
In particular `Q-RAW-CALLSITE-PATH(P) = E[:callsite-path]` byte-for-byte; no
K0 occurrence-rank fallback exists. A zero, duplicate, omitted,
alias-normalized, path-rewritten, or reordered join rejects.

The provider module Q_j and M0p are selected by D callee module ordinal and E
`:callee-module-source-id`. M0p module/source SHA and D callee
source/definition hashes must match the authentic M1/M0 values. The unique full
OWNER-REGISTRY row `REGp` in Q_j whose declared name and arity equal D symbol
and arity supplies the provider top-level form ordinal, defn ID, and declared
body root. `PROVIDER-TOP-ID` is
`Q_j[:top-level-form-ids][REGp[:top-level-form-ordinal]]` and
`PROVIDER-FRAGMENT` is the unique Q_j fragment containing PROVIDER-TOP-ID in
`:root-form-ids`. `FRp` is the unique RC_j function row used to construct REGp:
its module ordinal, declared name, arity, defn ID, function-form ID, and declared
body-root ID must reproduce REGp's source, top-level, SH05, and body coordinates
byte-for-byte. FRp supplies `:definition-binding-id`; zero or multiple FRp rows
reject. `PROVIDER-BINDING` is the unique Q_j binding Bp with
that ID; it must have class `:namespace`, name D symbol, namespace equal both
Q_j module namespace and D callee module, and exactly one membership in
PROVIDER-FRAGMENT `:local-binding-ids`. `CONSUMER-FRAGMENT` is the unique Q_i fragment containing
the paired Q reference syntax ID. Consumer and provider projected binding IDs
are deliberately not compared because each hashes its own module source
revision and definition syntax.

The provider selector is literal. `require` rejects construction when its
predicate is false; it does not return a value. Every UNIQUE selector rejects
zero or multiple matches.

```clojure
PROVIDER-JOIN(E,X,D,M0p) =
  (let [T (UNIQUE-BY :module-ordinal (:callee-module-ordinal D)
                     MODULE-TRANSACTIONS)
        Q_j (:authenticated-core-request T)
        RC_j (:resolved-core-carrier T)
        REGp
        (UNIQUE-WHERE
          #(and (= (:module-ordinal %) (:callee-module-ordinal D))
                (= (:declared-name %) (:symbol D))
                (= (:arity %) (:arity D)))
          OWNER-REGISTRY)
        FRp
        (UNIQUE-WHERE
          #(and (= :named-top-level (:definition-kind %))
                (= (:definition-name %) (:declared-name REGp))
                (= (:fixed-arity %) (:arity REGp))
                (= (:function-syntax-id %)
                   (:sh05-expanded-fn-id REGp))
                (= (:definition-core-node-id %) (:defn-id REGp))
                (= (:body-core-node-id %)
                   (:declared-body-root-id REGp)))
          (:function-records RC_j))
        PROVIDER-TOP-ID
        (nth (:top-level-form-ids Q_j)
             (:top-level-form-ordinal REGp))
        PROVIDER-TOP
        (UNIQUE-BY :form-id PROVIDER-TOP-ID (:forms Q_j))
        TRACEp
        (UNIQUE-WHERE
          #(and (= 'defn (:macro %))
                (= (:output-def-syntax-id %)
                   (:sh05-expanded-def-id REGp))
                (= (:introduced-fn-syntax-id %)
                   (:sh05-expanded-fn-id REGp)))
          (:macro-origin-traces Q_j))
        Ffnp
        (UNIQUE-BY :syntax-id (:introduced-fn-syntax-id TRACEp)
                   (:forms Q_j))
        PROVIDER-FRAGMENT
        (UNIQUE-WHERE #(= (:root-form-ids %) [PROVIDER-TOP-ID])
                      (:fragment-manifest Q_j))
        Bp (UNIQUE-BY :binding-id (:definition-binding-id FRp)
                      (:binding-table Q_j))
        DP (UNIQUE-WHERE
             #(and (= (:name %) (:symbol D))
                   (= (:arity %) (:arity D)))
             (:definitions M0p))]
    (require
      (and (= (:module-ordinal T) (:callee-module-ordinal D))
           (= (get-in Q_j [:module :namespace]) (:callee-module D)
              (:module M0p))
           (= (get-in Q_j [:lineage :source-revision-id])
              (:source-sha256 M0p) (:callee-source-sha256 D))
           (= (:module-source-id M0p) (:callee-module-source-id E))
           (= (:source-id REGp)
              (get-in Q_j [:lineage :sh06-semantic-projection-id]))
           (= (:symbol D) (:callee-function E))
           (= (:arity D) (:arity E) (:fixed-arity FRp))
           (= (:source-hash DP) (:callee-definition-source-hash D)
              (:callee-function-source-hash E))
           (= (:owner-edge D) (:owner-edge E)
              (OWNER-EDGE (:owner (UNIQUE-BY
                                    :module-ordinal
                                    (:consumer-module-ordinal X)
                                    AUTHENTIC-M0))
                          (:owner M0p)))
           (= :authenticated-b51-namespace-external (:authorization E))
           (= (:syntax-id PROVIDER-TOP) (:sh05-expanded-def-id REGp))
           (= (:form-id Ffnp) (:function-form-id FRp))
           (= (:syntax-id Ffnp) (:function-syntax-id FRp)
              (:sh05-expanded-fn-id REGp))
           (= (:parent-form-id Ffnp) (:form-id PROVIDER-TOP))
           (= (:form-id Ffnp) (nth (:child-form-ids PROVIDER-TOP) 2))
           (= :namespace (:binding-class Bp))
           (= (:name Bp) (:symbol D))
           (= (:namespace Bp) (get-in Q_j [:module :namespace])
              (:callee-module D))
           (= 1 (MEMBERSHIP-COUNT (:local-binding-ids PROVIDER-FRAGMENT)
                                  (:binding-id Bp)))))
    [Q_j REGp FRp PROVIDER-TOP-ID PROVIDER-FRAGMENT Bp]))
```

The returned value is exactly that six-value vector; T, RC_j, PROVIDER-TOP,
and DP are checked intermediates and are not omitted caller inputs. Zero or
multiple matches or any failed equality reject.

`EDGE-BASE` has exactly `:ordinal :manifest-id :m1-edge
:consumer-module-ordinal :consumer-source-id :consumer-fragment-id
:consumer-reference-syntax-id :consumer-binding-id :symbol :resolution-order
:provider-module-ordinal :provider-source-id :provider-fragment-id
:provider-binding-id`. `:ordinal` is E ordinal, manifest ID is M1 manifest ID,
`:m1-edge` is the complete exact E row, consumer fields come from X, and
provider fields come from Q_j,
PROVIDER-FRAGMENT, and Bp. Define, before any authority receipt exists:

```clojure
EDGE-BASE([E [X R B F P G REGc M0c D M0p]]) =
  (let [[Q_j REGp FRp PROVIDER-TOP-ID PROVIDER-FRAGMENT Bp]
        (PROVIDER-JOIN E X D M0p)]
    {:ordinal E[:ordinal]
     :manifest-id AUTHENTIC-M1[:manifest-id]
     :m1-edge E
     :consumer-module-ordinal X[:consumer-module-ordinal]
     :consumer-source-id X[:consumer-source-id]
     :consumer-fragment-id X[:consumer-fragment-id]
     :consumer-reference-syntax-id X[:reference-syntax-id]
     :consumer-binding-id X[:binding-id]
     :symbol X[:symbol]
     :resolution-order X[:resolution-order]
     :provider-module-ordinal D[:callee-module-ordinal]
     :provider-source-id
     Q_j[:lineage :sh06-semantic-projection-id]
     :provider-fragment-id PROVIDER-FRAGMENT[:fragment-id]
     :provider-binding-id Bp[:binding-id]}))
EDGE-BASES-IN-M1-ORDINAL-ORDER =
  (mapv EDGE-BASE PAIRING)
EDGE-ID = MID(:l2-expected-edge-census, EDGE-BASE)
EXPECTED = (mapv #(assoc % :edge-id (MID :l2-expected-edge-census %))
                 EDGE-BASES-IN-M1-ORDINAL-ORDER)
EXPECTED-ROOT = MR(:l2-expected-edge-census, EXPECTED)
ACCEPTED-EDGE-IDS = (mapv :edge-id EXPECTED)
```

All edge IDs are therefore typed edge IDs; no Q binding ID is placed in either
partition. EXPECTED has dense ordinals 0 through 1479 and unique edge IDs; its
`(mapv :m1-edge EXPECTED)` must equal AUTHENTIC-M1 `:edges`
byte-for-byte. Duplicate edge IDs, an M1 edge without exactly one Q pair, a Q
pair without exactly one M1 edge, or any authority row not in EXPECTED rejects.

The mandatory unauthorized-cross-fragment candidate is receipt-free. `SEED` is
the first EDGE-BASE in M1 order; the authentic 1,480-row census proves it
exists. Its consumer and provider module, source, fragment, and binding
coordinates must differ. `BAD-BASE` swaps the four endpoint pairs
`:consumer-module-ordinal`/`:provider-module-ordinal`,
`:consumer-source-id`/`:provider-source-id`,
`:consumer-fragment-id`/`:provider-fragment-id`, and
`:consumer-binding-id`/`:provider-binding-id`, leaving `:m1-edge`,
`:consumer-reference-syntax-id`, and every other field unchanged. Define:

`CONSUMER-ENDPOINT(X)` has exactly `:module-ordinal :source-id :fragment-id
:binding-id` and takes those values from X's four `:consumer-*` fields.
`PROVIDER-ENDPOINT(X)` has the same exact keyset and takes its values from X's
four `:provider-*` fields.

```clojure
SEED = (first EDGE-BASES-IN-M1-ORDINAL-ORDER)
BAD-BASE =
  (assoc SEED
    :consumer-module-ordinal (:provider-module-ordinal SEED)
    :consumer-source-id (:provider-source-id SEED)
    :consumer-fragment-id (:provider-fragment-id SEED)
    :consumer-binding-id (:provider-binding-id SEED)
    :provider-module-ordinal (:consumer-module-ordinal SEED)
    :provider-source-id (:consumer-source-id SEED)
    :provider-fragment-id (:consumer-fragment-id SEED)
    :provider-binding-id (:consumer-binding-id SEED))
CONSUMER-ENDPOINT(X) =
  {:module-ordinal X[:consumer-module-ordinal]
   :source-id X[:consumer-source-id]
   :fragment-id X[:consumer-fragment-id]
   :binding-id X[:consumer-binding-id]}
PROVIDER-ENDPOINT(X) =
  {:module-ordinal X[:provider-module-ordinal]
   :source-id X[:provider-source-id]
   :fragment-id X[:provider-fragment-id]
   :binding-id X[:provider-binding-id]}
BAD-EDGE-ID = MID(:l2-expected-edge-census, BAD-BASE)

PAIRING-VALID? =
  (and (= Q-CROSS-NORMALIZED M1-NORMALIZED)
       (= 1480 (count Q-CROSS-NORMALIZED) (count M1-NORMALIZED)
          (count PAIRING))
       (= 1480 (count (set (map NORMALIZED-KEY Q-CROSS-NORMALIZED))))
       (= (mapv first PAIRING) AUTHENTIC-M1[:edges])
       (every? #(= (Q-NORMALIZED-ROW (second %))
                   (M1-NORMALIZED-ROW (first %))) PAIRING))

REBUILT-EXPECTED =
  (mapv (fn [PAIR]
          (let [BASE (EDGE-BASE PAIR)]
            (assoc BASE :edge-id (MID :l2-expected-edge-census BASE))))
        PAIRING)

EXPECTED-VALID? =
  (and (= 1480 (count EXPECTED))
       (= EXPECTED REBUILT-EXPECTED)
       (= (mapv :ordinal EXPECTED) (vec (range 1480)))
       (= 1480 (count (set (map :edge-id EXPECTED))))
       (every? #(= (:edge-id %)
                   (MID :l2-expected-edge-census (dissoc % :edge-id)))
               EXPECTED)
       (= EXPECTED-ROOT (MR :l2-expected-edge-census EXPECTED))
       (= (mapv :m1-edge EXPECTED) AUTHENTIC-M1[:edges])
       (= (mapv :m1-edge EXPECTED)
          SH06-AUTHORITY-CARRIER[:expected-edge-census]))

AUTHENTIC-QS = (mapv :authenticated-core-request MODULE-TRANSACTIONS)
EDGE-ENDPOINT-KEYS =
[:consumer-module-ordinal :consumer-source-id :consumer-fragment-id
 :consumer-binding-id :provider-module-ordinal :provider-source-id
 :provider-fragment-id :provider-binding-id]
BAD-ACTUAL-KEYSET =
#{:status :code :rule :reason :input-id :mutated-id :expected :actual
  :committed-row :output}

VERIFY-UNAUTHORIZED-CROSS-FRAGMENT
(QS, M0, M1, OWNER, EXPECTED-AUTHORITY, CANDIDATE) =
  (let [M1-ROW
        (UNIQUE-WHERE #(= % (:m1-edge CANDIDATE)) (:edges M1))
        EXPECTED-ROW
        (UNIQUE-WHERE #(= (:m1-edge %) M1-ROW) EXPECTED-AUTHORITY)
        AUTHORIZED-BASE (dissoc EXPECTED-ROW :edge-id)
        CANDIDATE-ID (MID :l2-expected-edge-census CANDIDATE)]
    (require
      (and CARRIER-VALID? PAIRING-VALID? EXPECTED-VALID?
           (= QS AUTHENTIC-QS)
           (= M0 AUTHENTIC-M0)
           (= M1 AUTHENTIC-M1)
           (= OWNER OWNER-REGISTRY)
           (= EXPECTED-AUTHORITY EXPECTED)
           (= (set (keys CANDIDATE)) (set (keys AUTHORIZED-BASE)))
           (= (:edge-id EXPECTED-ROW)
              (MID :l2-expected-edge-census AUTHORIZED-BASE))
           (= (apply dissoc CANDIDATE EDGE-ENDPOINT-KEYS)
              (apply dissoc AUTHORIZED-BASE EDGE-ENDPOINT-KEYS))
           (not= (CONSUMER-ENDPOINT AUTHORIZED-BASE)
                 (PROVIDER-ENDPOINT AUTHORIZED-BASE))
           (= (CONSUMER-ENDPOINT CANDIDATE)
              (PROVIDER-ENDPOINT AUTHORIZED-BASE))
           (= (PROVIDER-ENDPOINT CANDIDATE)
              (CONSUMER-ENDPOINT AUTHORIZED-BASE))
           (not= CANDIDATE AUTHORIZED-BASE)
           (not= CANDIDATE-ID (:edge-id EXPECTED-ROW))
           (not (contains? (set (map :edge-id EXPECTED-AUTHORITY))
                           CANDIDATE-ID))))
    {:status :rejected
     :code "SH07-B51-EDGE-AUTHORITY"
     :rule "L2-UNKNOWN-CORE-FORM"
     :reason :unauthenticated-b51-external-link
     :input-id (:edge-id EXPECTED-ROW)
     :mutated-id CANDIDATE-ID
     :expected (CONSUMER-ENDPOINT AUTHORIZED-BASE)
     :actual (CONSUMER-ENDPOINT CANDIDATE)
     :committed-row nil
     :output nil}))

BAD-ACTUAL =
  VERIFY-UNAUTHORIZED-CROSS-FRAGMENT(
    AUTHENTIC-QS, AUTHENTIC-M0, AUTHENTIC-M1,
    OWNER-REGISTRY, EXPECTED, BAD-BASE)
BAD-ACTUAL-EXPECTED =
{:status :rejected
 :code "SH07-B51-EDGE-AUTHORITY"
 :rule "L2-UNKNOWN-CORE-FORM"
 :reason :unauthenticated-b51-external-link
 :input-id (:edge-id (first EXPECTED))
 :mutated-id BAD-EDGE-ID
 :expected (CONSUMER-ENDPOINT SEED)
 :actual (CONSUMER-ENDPOINT BAD-BASE)
 :committed-row nil
 :output nil}
require BAD-ACTUAL = BAD-ACTUAL-EXPECTED

DIAGNOSTIC-OBSERVATION-KEYS =
[:code :status :rule :reason :input-id :mutated-id :expected :actual]
DIAGNOSTIC-BASE =
  (select-keys BAD-ACTUAL DIAGNOSTIC-OBSERVATION-KEYS)
DIAGNOSTIC-ID = MID(:l2-edge-diagnostics, DIAGNOSTIC-BASE)
DIAGNOSTIC = (assoc DIAGNOSTIC-BASE :diagnostic-id DIAGNOSTIC-ID)
L2-DIAGNOSTICS =
{:entries [DIAGNOSTIC]
 :root (MR :l2-edge-diagnostics [DIAGNOSTIC])}
L2-OPERATION = UNIQUE-BY(:owner, :l2, OPERATION-MANIFEST)
L2-FUEL-RECEIPT =
  UNIQUE-BY(:operation-id, L2-OPERATION[:operation-id], FUEL-RECEIPTS)
L2-FUEL-RECEIPT-ID = L2-FUEL-RECEIPT[:receipt-id]
FIXTURE-BASE =
{:input-id BAD-ACTUAL[:input-id]
 :mutation {:operation :swap-endpoints
            :paths (mapv vector EDGE-ENDPOINT-KEYS)
            :before [(CONSUMER-ENDPOINT SEED)
                     (PROVIDER-ENDPOINT SEED)]
            :after [(CONSUMER-ENDPOINT BAD-BASE)
                    (PROVIDER-ENDPOINT BAD-BASE)]}
 :mutated-id BAD-ACTUAL[:mutated-id]
 :diagnostic-id DIAGNOSTIC-ID
 :status BAD-ACTUAL[:status]
 :fuel-receipt-id L2-FUEL-RECEIPT-ID}
FIXTURE-ID = MID(:l2-edge-negative-fixtures, FIXTURE-BASE)
FIXTURE = (assoc FIXTURE-BASE :fixture-id FIXTURE-ID)
L2-NEGATIVE-FIXTURES =
{:entries [FIXTURE]
 :root (MR :l2-edge-negative-fixtures [FIXTURE])}
REJECTED-EDGE-IDS = []
```

`VERIFY-UNAUTHORIZED-CROSS-FRAGMENT` is the exact independent, receipt-free
verifier. `UNIQUE-WHERE` rejects zero or multiple matches. It rebuilds the sole
authorized base from the authentic M1 row and EXPECTED authority, checks all
Q/M0/M1/owner inputs byte-for-byte against the immutable values, proves that
only the eight endpoint fields changed, and emits the closed BAD-ACTUAL value.
It consumes no fuel receipt, diagnostic, fixture, authority receipt, witness,
certificate, host path, process session, or caller-supplied expected row.
BAD-ACTUAL proves that rejection committed neither a row nor an output.

The construction selects L2-FUEL-RECEIPT only after BAD-ACTUAL by joining its
`:operation-id` to the unique OPERATION-MANIFEST row whose owner is `:l2`; zero
or multiple operation or receipt matches reject. The verifier does not obtain
that receipt through WITNESS. DIAGNOSTIC is derived from BAD-ACTUAL, and FIXTURE
is derived from BAD-ACTUAL and the later fuel-receipt join. Diagnostic ID
precedes fixture ID, the fixture contains the diagnostic ID, and the diagnostic
contains no fixture ID, so no identity cycle exists.

The authority receipt has exactly `:schema :content :source-descriptors
:authenticated-sh06-artifact-ids :sh06-semantic-projection-ids
:link-authority-id :owner-registry-root :q-external-root :expected-edge-root
:expected-edge-census :accepted-edge-ids :rejected-edge-ids :self-id-omits
`:receipt-id`; schema is `:gravity/sh07-b51-edge-authority-receipt-v1`. The
exact receipt construction is:

```clojure
RECEIPT-KEYSET =
  #{:schema :content :source-descriptors
    :authenticated-sh06-artifact-ids :sh06-semantic-projection-ids
    :link-authority-id :owner-registry-root :q-external-root
    :expected-edge-root :expected-edge-census :accepted-edge-ids
    :rejected-edge-ids :self-id-omits :receipt-id}
RECEIPT-BASE =
  {:schema :gravity/sh07-b51-edge-authority-receipt-v1
   :content C/T
   :source-descriptors
   SH06-AUTHORITY-CARRIER[:source-descriptors]
   :authenticated-sh06-artifact-ids
   (mapv :artifact-id SH06-AUTHORITY-CARRIER[:resolution-artifacts])
   :sh06-semantic-projection-ids
   SH06-AUTHORITY-CARRIER[:semantic-projection-ids]
   :link-authority-id SH06-AUTHORITY-CARRIER[:link-authority-id]
   :owner-registry-root OWNER-REGISTRY-ROOT
   :q-external-root Q-EXTERNAL-ROOT
   :expected-edge-root EXPECTED-ROOT
   :expected-edge-census EXPECTED
   :accepted-edge-ids ACCEPTED-EDGE-IDS
   :rejected-edge-ids []
   :self-id-omits [[:receipt-id]]}
RECEIPT-ID =
  H([:gravity/sh07-b51-edge-authority-receipt-v1 RECEIPT-BASE])
RECEIPT = (assoc RECEIPT-BASE :receipt-id RECEIPT-ID)
```

Content is C/T and source descriptors are the seven complete rows at ordinals
3-9, not invented descriptor IDs. Artifact and semantic-projection vectors are
the literal carrier-path projections above and contain seven values each in
module order. Q external root is Q-EXTERNAL-ROOT, expected values are
EXPECTED-ROOT and EXPECTED, accepted IDs are exactly ACCEPTED-EDGE-IDS,
rejected IDs are exactly empty, and `:self-id-omits` is exactly
`[[:receipt-id]]`. Thus
`RECEIPT-ID = H([:gravity/sh07-b51-edge-authority-receipt-v1 RECEIPT-BASE])`.
The receipt is the base associated with that ID and is independently derived by
A and B from immutable source sessions before WITNESS or either certificate.
It contains no final external-binding row, diagnostic ID, fuel receipt, edge
authority root, witness, or certificate.

The carrier's seven resolution artifacts, embedded SH05 artifacts, semantic
projections, M0 descriptors, M1 manifest, and bundle core are all freshly
verified under their existing domains. M0 source IDs must equal M1
`:module-source-ids`, M1 manifest ID must equal BUNDLE-CORE manifest ID,
BUNDLE-CORE routes must equal the dense M1 edge-ordinal projection, and carrier
`:expected-edge-census` must equal M1 `:edges` byte-for-byte. The link-authority
and carrier IDs are exactly LINK-AUTHORITY-ID and SH06-AUTHORITY-CARRIER-ID as
constructed above; no `ARTIFACT-ID`, `EVENT-PRODUCT-IDS`, or
`CARRIER-WITHOUT-CARRIER-ID` metavariable exists. Receipt artifact IDs,
semantic-projection IDs, link-authority ID, and source descriptors must equal
their four literal carrier projections byte-for-byte, and
`(mapv :m1-edge EXPECTED)` must equal the carrier's raw expected edge census.

Only after receipt construction, `ACCEPTED` is
`(mapv #(assoc % :authority-receipt-id RECEIPT-ID :authorized true) EXPECTED)`.
The existing edge ID is a typed reference to its unique EXPECTED row; the
post-receipt row receives no new MID identity. Define the final predicates and
value literally:

```clojure
RECEIPT-VALID? =
  (and (= (set (keys RECEIPT)) RECEIPT-KEYSET)
       (= (:schema RECEIPT)
          :gravity/sh07-b51-edge-authority-receipt-v1)
       (= (dissoc RECEIPT :receipt-id) RECEIPT-BASE)
       (= (:receipt-id RECEIPT) RECEIPT-ID)
       (= RECEIPT-ID
          H([:gravity/sh07-b51-edge-authority-receipt-v1 RECEIPT-BASE]))
       (= (:source-descriptors RECEIPT)
          (:source-descriptors SH06-AUTHORITY-CARRIER))
       (= (:authenticated-sh06-artifact-ids RECEIPT)
          (mapv :artifact-id
                (:resolution-artifacts SH06-AUTHORITY-CARRIER)))
       (= (:sh06-semantic-projection-ids RECEIPT)
          (:semantic-projection-ids SH06-AUTHORITY-CARRIER))
       (= (:link-authority-id RECEIPT)
          (:link-authority-id SH06-AUTHORITY-CARRIER))
       (= (:owner-registry-root RECEIPT) OWNER-REGISTRY-ROOT)
       (= (:q-external-root RECEIPT) Q-EXTERNAL-ROOT)
       (= (:expected-edge-root RECEIPT) EXPECTED-ROOT)
       (= (:expected-edge-census RECEIPT) EXPECTED)
       (= (:accepted-edge-ids RECEIPT) ACCEPTED-EDGE-IDS)
       (= (:rejected-edge-ids RECEIPT) [])
       (= (:self-id-omits RECEIPT) [[:receipt-id]]))

ACCEPTED-VALID? =
  (and (= 1480 (count ACCEPTED))
       (= (mapv :ordinal ACCEPTED) (vec (range 1480)))
       (= (mapv :edge-id ACCEPTED) ACCEPTED-EDGE-IDS
          (:accepted-edge-ids RECEIPT))
       (every? #(= (:authority-receipt-id %) RECEIPT-ID) ACCEPTED)
       (every? #(= (:authorized %) true) ACCEPTED))

ACTUAL-REJECTED = []
PRE-COMPLETE =
  (and CARRIER-VALID? RECEIPT-VALID? PAIRING-VALID?
       EXPECTED-VALID? ACCEPTED-VALID?
       (= BAD-ACTUAL BAD-ACTUAL-EXPECTED)
       (= (set (keys BAD-ACTUAL)) BAD-ACTUAL-KEYSET)
       (= (:committed-row BAD-ACTUAL) nil)
       (= (:output BAD-ACTUAL) nil)
       (= DIAGNOSTIC-BASE
          (select-keys BAD-ACTUAL DIAGNOSTIC-OBSERVATION-KEYS))
       (= DIAGNOSTIC
          (assoc DIAGNOSTIC-BASE :diagnostic-id DIAGNOSTIC-ID))
       (= L2-DIAGNOSTICS
          {:entries [DIAGNOSTIC]
           :root (MR :l2-edge-diagnostics [DIAGNOSTIC])})
       (= FIXTURE
          (assoc FIXTURE-BASE :fixture-id FIXTURE-ID))
       (= L2-NEGATIVE-FIXTURES
          {:entries [FIXTURE]
           :root (MR :l2-edge-negative-fixtures [FIXTURE])})
       (= (set (keys BAD-BASE)) (set (keys SEED)))
       (not= (CONSUMER-ENDPOINT SEED) (PROVIDER-ENDPOINT SEED))
       (= (CONSUMER-ENDPOINT BAD-BASE) (PROVIDER-ENDPOINT SEED))
       (= (PROVIDER-ENDPOINT BAD-BASE) (CONSUMER-ENDPOINT SEED))
       (= BAD-EDGE-ID (MID :l2-expected-edge-census BAD-BASE))
       (not (contains? (set ACCEPTED-EDGE-IDS) BAD-EDGE-ID))
       (= ACTUAL-REJECTED [])
       (= (:rejected-edge-ids RECEIPT) ACTUAL-REJECTED))
EDGE-AUTHORITY-ROOT =
  (MR :l2-edge-authority [EXPECTED ACCEPTED ACTUAL-REJECTED])
EDGE-AUTHORITY-KEYSET =
  #{:receipt :expected-census :accepted :rejected :complete :root}
EDGE-AUTHORITY =
  {:receipt RECEIPT
   :expected-census EXPECTED
   :accepted ACCEPTED
   :rejected ACTUAL-REJECTED
   :complete PRE-COMPLETE
   :root EDGE-AUTHORITY-ROOT}
EDGE-AUTHORITY-VALID? =
  (and (= (set (keys EDGE-AUTHORITY)) EDGE-AUTHORITY-KEYSET)
       (= (:receipt EDGE-AUTHORITY) RECEIPT)
       (= (:expected-census EDGE-AUTHORITY) EXPECTED)
       (= (:accepted EDGE-AUTHORITY) ACCEPTED)
       (= (:rejected EDGE-AUTHORITY) ACTUAL-REJECTED)
       (= (:complete EDGE-AUTHORITY) PRE-COMPLETE)
       (= (:root EDGE-AUTHORITY) EDGE-AUTHORITY-ROOT))
COMPLETE =
  (and PRE-COMPLETE EDGE-AUTHORITY-VALID?
       (= true (:complete EDGE-AUTHORITY)))
require COMPLETE = true
```

`RECEIPT-KEYSET` is exactly the printed receipt keyset above. Edge authority
has exactly `:receipt :expected-census :accepted :rejected :complete :root` and
must equal EDGE-AUTHORITY byte-for-byte; `:complete` is recomputed
PRE-COMPLETE, and terminal COMPLETE additionally proves the full map and that
stored value true. It is not a supplied boolean. No external resolution or cross-module edge absent from
the authenticated projections may enter the core graph.

The executed verifier's exact rejection is rule `"L2-UNKNOWN-CORE-FORM"` and
reason `:unauthenticated-b51-external-link`; the stable enclosing diagnostic
code is `"SH07-B51-EDGE-AUTHORITY"`. DIAGNOSTIC has exactly `:code :status
:rule :reason :input-id :mutated-id :expected :actual :diagnostic-id`. FIXTURE
has exactly `:input-id :mutation :mutated-id :diagnostic-id :status
:fuel-receipt-id :fixture-id`. Both one-entry roots are constructed above and
recomputed as mandatory PRE-COMPLETE conjuncts.

Rejection commits no external row, edge-authority row, graph row, or output
artifact. The acyclic construction order is Q/M0/M1/OWNER, Q external
census/root, normalized cross-call PAIRING/EXPECTED, BAD-BASE, receipt-free
BAD-ACTUAL, DIAGNOSTIC, the pre-existing fuel-receipt join, FIXTURE and both
roots, authority RECEIPT, ACCEPTED, PRE-COMPLETE and edge-authority root, then
the L2 witness. No receipt enters an edge preimage, and BAD-ACTUAL is an
internal verifier observation rather than a witness member or identity domain.

The C6 witness has exactly `:schema :fuel-subject-id :ast :syntax-core-map :desugaring-trace
:origins-facts-order :verification :mutations :operation-ids :witness-id`; schema is
`:gravity/sh07-b51-c6-witness-v1`. AST, syntax-core map, and desugaring trace
each have exactly `:entries :root`; AST entries have exactly `:ordinal :node-id
 :kind :children :source-span :origin-chain :generated-origin
:evaluation-index :binding-context :profile :metadata :fact-ids :generated`;
node and child IDs are the exact resolved checked-core IDs, children are exact
ordered node-ID vectors, profile is `:meta`, and generated is boolean. Trace entries have exactly
`:ordinal :input-id :output-id :pass-id :fact-ids :origin-id`; syntax-core rows
have exactly `:ordinal :syntax-id :core-id :origin-chain :generated-origin`.
The trace is the two-row-per-defn authenticated macro projection defined
above; fact-ID vectors are empty because C6 facts remain pending. Every root
is MR of its literal kind and complete entries.
Origins-facts-order has exactly `:origin-root :fact-root
:evaluation-order-root`; verification has exactly `:checker-ids :verified-root
:input-ast-root :status`, status `:passed`, and verified root and input AST root
both equal AST root. Mutations are dense maps with exactly `:mutation-id
:subject-id :subject-root :expected-diagnostic :actual-diagnostic :status
:fuel-receipt-id`; subject ID exists in AST and subject root equals AST root;
status is `:rejected-as-expected`.

The BOOT2 witness has exactly `:schema :fuel-subject-id :source :compiler :backend
:dependencies :runtime-assumptions :operation-ids :witness-id`; schema is
`:gravity/sh07-b51-boot2-witness-v1`. Source has exactly `:descriptors
:descriptor-root :source-id`; descriptors equal the 12 source-session rows,
descriptor root equals the source session root, and source ID is
`H([:gravity/sh07-b51-boot2-source-id-v1 DESCRIPTOR-ROOT])`. Compiler has
exactly `:descriptor :compiler-id`; descriptor is source ordinal 10 and compiler
ID is `H([:gravity/sh07-b51-boot2-compiler-id-v1 DESCRIPTOR])`. Backend is
exactly `{:status :not-applicable
:identity H([:gravity/sh07-b51-no-backend-v1 :verified-core])}`; dependencies
has exactly `:entries :root`; entries are the four path-free CP-SEM rows and
root is `MR(:boot2-dependencies, ENTRIES)`. Only entry zero's `:sha256` equals
the classpath manifest root; absolute canonical paths and file keys are not
stage semantics. Runtime assumptions has exactly
`:jvm-id :clojure-seed-id :git-object-format :fixed-fuel-bounds-id :network`,
uses the exact recomputed RUNTIME-BASIS equations above, and network is
`:forbidden`.

The BOOT3 witness has exactly `:schema :fuel-subject-id :modules :profile :tests
:diagnostic-compatibility :stage-comparison :operation-ids :stage-advanced
:witness-id`; schema is
`:gravity/sh07-b51-boot3-witness-v1`, profile is `:meta`, stage-advanced is
false. Let `BOOT3-FUEL-RECEIPT` be the unique fuel receipt joined to the
operation whose owner is `:boot3`; its receipt ID is recomputed before any
BOOT3 final-stage value is constructed. The exact accepted source-coverage
mutation above supplies two authentic verifier outcomes per module rather than
a hash-self-test. For module i, let
`ARG-IDS_i = [INPUT_i[0][:artifact-id] OUTPUT_i[3][:artifact-id]
OUTPUT_i[1][:artifact-id] OUTPUT_i[2][:artifact-id]]`; this is exact Q/RC/D/Z
argument order.

`DIAG-BASE_i` is exactly:

```clojure
{:ordinal i
 :module-id P_i[:module-id]
 :source-id SID_i
 :fixture-id OUTPUT_i[3][:artifact-id]
 :expected {:rule "C6-VERIFY" :source-span {:source PATH_i}}
 :actual {:rule REJECT-ACTUAL_i[:rule]
          :source-span {:source Q_i[:provenance :actual-source-path]}}
 :status :matched}
```

Its actual and expected values must be byte-equal. `DIAG_i` is exactly
`(assoc DIAG-BASE_i :diagnostic-id
MID(:boot3-diagnostics, DIAG-BASE_i))`;
`BOOT3-DIAGNOSTIC-ENTRIES_i = [DIAG_i]`; and `BOOT3-DIAGNOSTICS_i` is exactly
`{:entries [DIAG_i] :root MR(:boot3-diagnostics, [DIAG_i])}`. This is an exact
path-neutral adapter observation joining the raw four-key verifier result to
its authenticated source context; it does not claim that the raw verifier
result emitted a span. Accepted `RC_i[:diagnostics]` remains exactly empty and
is not misrepresented as already containing diagnostic IDs.

The positive and negative BOOT3 test bases are exactly:

```clojure
POS-BASE_i =
{:module-ordinal i
 :fixture-id OUTPUT_i[3][:artifact-id]
 :kind :positive
 :input-artifact-ids ARG-IDS_i
 :mutation {:operation :identity :path []}
 :expected PASS-EXPECTED
 :actual PASS-ACTUAL_i
 :status :passed
 :fuel-receipt-id BOOT3-FUEL-RECEIPT[:receipt-id]}

NEG-BASE_i =
{:module-ordinal i
 :fixture-id OUTPUT_i[3][:artifact-id]
 :kind :negative
 :input-artifact-ids ARG-IDS_i
 :mutation {:operation :assoc-in
            :path [:module :namespace]
            :before RC_i[:module :namespace]
            :after 'gravity.altered}
 :expected REJECT-EXPECTED
 :actual REJECT-ACTUAL_i
 :diagnostic-id DIAG_i[:diagnostic-id]
 :status :rejected-as-expected
 :fuel-receipt-id BOOT3-FUEL-RECEIPT[:receipt-id]}
```

`POS_i = (assoc POS-BASE_i :test-id MID(:boot3-tests, POS-BASE_i))` and
`NEG_i = (assoc NEG-BASE_i :test-id MID(:boot3-tests, NEG-BASE_i))`.
`BOOT3-TEST-ENTRIES_i` is exactly `[POS_i NEG_i]`, and `BOOT3-TESTS_i` is
exactly `{:entries [POS_i NEG_i]
:root MR(:boot3-tests, [POS_i NEG_i])}`. Fixture and input artifact IDs,
diagnostic ID, and fuel receipt ID are typed upstream references; only
`:test-id` is the enclosing test member identity.

Each of the seven final module rows is exactly
`(assoc BOOT3-PRE-MODULE_i
:test-ids (mapv :test-id BOOT3-TEST-ENTRIES_i)
:diagnostics BOOT3-DIAGNOSTICS_i)` and therefore preserves all six pre-stage
fields byte-for-byte and adds only the two permitted receipt-dependent fields.
`BOOT3-TEST-ENTRIES` and `BOOT3-DIAGNOSTIC-ENTRIES` are respectively the dense
module-order concatenations of the seven per-module vectors, giving exactly 14
tests and seven normalized diagnostic observations. WITNESS
`:boot3 :tests` is exactly
`{:entries BOOT3-TEST-ENTRIES
:root MR(:boot3-tests, BOOT3-TEST-ENTRIES)}`. Its diagnostic compatibility is
exactly `{:a-root A-BOOT3-DIAGNOSTIC-ROOT
:b-root B-BOOT3-DIAGNOSTIC-ROOT :equal true}`, where A and B independently
compute both named roots as
`MR(:boot3-diagnostics, BOOT3-DIAGNOSTIC-ENTRIES)` and byte equality is
required.

For module i, `BOOT3-OBSERVABLE_i` is exactly
`{:ordinal i :output-root MR(:boot3-artifact, OUTPUT_i)
:fact-root BOOT3-FACTS_i[:root]
:diagnostic-root BOOT3-DIAGNOSTICS_i[:root]
:test-root BOOT3-TESTS_i[:root]}`. Stage comparison has exactly
`:left-lowering-product-id :right-lowering-product-id :observables :equal
:equivalence-root`; IDs are the independent pre-certificate lowering product
IDs, observables are the seven `BOOT3-OBSERVABLE_i` rows in module order,
equal is true, and equivalence root is
`MR(:boot3-stage-comparison, COMPARISON-WITHOUT-EQUIVALENCE-ROOT)`.

The BOOT4 witness has exactly `:schema :fuel-subject-id :module-manifest :effects
:capabilities :deterministic-output :fact-provenance :preservation-tests
:operation-ids :witness-id`; schema is `:gravity/sh07-b51-boot4-witness-v1`.
Module manifest has exactly `:entries :root`; each of seven entries has exactly
`:ordinal :module-id :source-id :input-artifact-ids :output-artifact-ids
:profile`, profile `:meta`, and root is MR of `:boot4-module-manifest` and the
entries. Effects and capabilities each have exact `:entries :root`; entries
have `:module-id :node-id :values`, and roots use distinct literal kinds.
Deterministic output has exactly `:a-lowering-product-id
:b-lowering-product-id :observable-root :equal`; these IDs hash the independent
pre-certificate LOWERING-PRODUCT values, never a checker output, certificate,
payload, or witness. For module i,
`PV_i = subvec(FOUNDATION-PREPRODUCT[:provenance-records], 4*i, 4*i+4)` and
`BOOT4-MODULE-OUTPUT-PAYLOAD_i` is exactly:

```clojure
{:ordinal i
 :source-id P_i[:source-id]
 :module-id P_i[:module-id]
 :pass-id PASS-RECORDS[i][:pass-id]
 :compiler-id BOOT2-COMPILER[:compiler-id]
 :input-artifact-ids (mapv :artifact-id INPUT_i)
 :output-artifact-ids (mapv :artifact-id OUTPUT_i)
 :output-artifact-root MR(:boot3-artifact, OUTPUT_i)
 :fact-ids (mapv :id F_i)
 :fact-root BOOT3-FACTS_i[:root]
 :provenance-ids (mapv :id PV_i)
 :diagnostic-root BOOT3-DIAGNOSTICS_i[:root]
 :test-root BOOT3-TESTS_i[:root]}
```

`BOOT4-MODULE-OUTPUT_i` is exactly
`(assoc BOOT4-MODULE-OUTPUT-PAYLOAD_i :output-id
MID(:boot4-module-outputs, BOOT4-MODULE-OUTPUT-PAYLOAD_i))`.
`BOOT4-MODULE-OUTPUT-ENTRIES` is the seven rows in module order and
`BOOT4-MODULE-OUTPUT-ROOT` is exactly
`MR(:boot4-module-outputs, BOOT4-MODULE-OUTPUT-ENTRIES)`. Every referenced
artifact, fact, provenance, pass, and compiler ID resolves exactly once and
rehashes to its named upstream row. Deterministic output `:observable-root`
equals BOOT4-MODULE-OUTPUT-ROOT and `:equal` is true. Fact provenance has
exactly `:facts :provenance :fact-root
:provenance-root`; fact/provenance rows use the FOUNDATION-RUNTIME exact
keysets, are copied from FOUNDATION-PREPRODUCT before LOWERING-PRODUCT, and
equal the corresponding rows and roots later carried by FOUNDATION-RUNTIME.
Preservation tests has exactly `:positive :negative :result-root :status`.
`BOOT4-POSITIVE = [POS_0 ... POS_6]` and
`BOOT4-NEGATIVE = [NEG_0 ... NEG_6]`, both in module order, and
`BOOT4-PRESERVATION-ROOT = MR(:boot4-preservation-tests,
[BOOT4-POSITIVE BOOT4-NEGATIVE])`. The two fields equal those vectors,
`:result-root` equals that root, and status is `:passed` iff all 14 actual
values equal their expected values and every row has its prescribed status.
BOOT4
contains no certificate, checker-output, staged-payload, E9, or enclosing
witness identity, so its identity graph is acyclic.

The exact BOOT3/BOOT4 dependency order is Q/T/D/Z/RC, the two receipt-free
verifier outcomes, LOWERING-PRODUCT, operations and fuel receipts, BOOT3
diagnostic/test IDs and roots, the BOOT3 final witness, BOOT4 output IDs and
preservation root, the BOOT4 witness, then the enclosing WITNESS and
certificate. No pre-stage or fuel subject contains either verifier outcome or
any post-receipt row; reversing any edge rejects.

Each M-authored child receipt has exactly `:schema :role :ordinal-triple :argv
:argv-id :request :response :process :process-join :counters :tcb
:self-id-omits :receipt-id`. Schema is
`:gravity/sh07-b51-child-process-receipt-v1`; A's triple is `[2 3 4]` and B's
is `[6 7 8]`. Request has exactly `:value :request-id :byte-count
:raw-sha256`, where value is `CHECKER-REQUEST`, bytes are W(value), raw SHA is
R(bytes), and request ID equals `CHECKER-REQUEST-ID`. Response has exactly
`:transport :reconstruction
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
raw SHA, and `response-preimage-id`. Self-id-omits is `[[:receipt-id]]`;
receipt ID is `H([:gravity/sh07-b51-child-process-receipt-v1
NORMALIZED-CHILD-RECEIPT-WITHOUT-ID])`.

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
:commit C :tree T :source-descriptors-root SHA :event-root SHA
:counters {:acquire-count 12 :decode-count 12 :event-count 39
:finalize-count 1}}`. Every child ID is recomputed from its embedded
`source-read-receipt`; M's ID is recomputed from the coordinator receipt. The
ordinal-9 and ordinal-10 rows carry that same M ID. No object identity, path,
PID, host, or time participates.

Coordinator receipt has exactly `:schema :process :runtime-mxbean :java :git
:pre-manifest-tcb :eligibility-check :classpath-checks :environment :source-closure :load-events
:loaded-namespace-receipt :host-surface :source-read-receipt
:child-process-receipts :process-joins :coordinator-events
:coordinator-event-root :counters :a-validated-before-b-builder
:b-validated-before-m-session :launcher-process-handle-id
:launcher-argv-id :self-id-omits :receipt-id`. Schema is
`:gravity/sh07-b51-coordinator-receipt-v1`;
pre-manifest-tcb and eligibility-check are the exact closed maps independently
reconstructed before A construction; process is `SELF-PROCESS`; the two child receipts are exact A then B; process
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
aggregates and semantic ordinals. Self-id-omits is `[[:receipt-id]]`; receipt
ID is `H([:gravity/sh07-b51-coordinator-receipt-v1
NORMALIZED-COORDINATOR-RECEIPT-WITHOUT-ID])`.

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
:coordinator-receipt :m-response :launcher-validation-input
:contract-validation :launcher-receipt-id
:contract-document-event-root :contract-row-plan-id :pre-resolved-row-root
:pre-discharge-id :runtime-resolved-row-root
:runtime-discharge-id :receipt-id]}`. It does not perform a
global map-key walk. It constructs a new normalized receipt from the closed
role-specific functions below, so a same-named key at any unlisted path is
untouched and an unrecognized path rejects.

Let `RO` be `:gravity/receipt-only`. `N-ARGV` replaces argv index 0 with
`:gravity/java-executable` and index 11 with `:gravity/classpath`; for M's
exact 39-string argv it additionally replaces index 24, the value following
`--staging-root`, with RO. A/B argv have no staging index. No other argv value
is normalized.
`N-SELF-PROCESS` replaces exactly `:pid :parent-pid :command :arguments
:start-instant` with RO. `N-RUNTIME-MXBEAN` replaces exactly `:name :pid :host
:start-time-millis :java-class-path` with RO; therefore the embedded PID@host
string cannot leak. `N-EXECUTABLE` replaces exactly `:canonical-path
:file-key`; `N-CLASSPATH` applies that replacement to each of four entries;
`N-ENVIRONMENT` replaces exactly `:working-directory`; `N-RETAINED-PROCESS`
replaces exactly `:pid :parent-pid :command :arguments :start-instant` and
recomputes rather than copies its process-handle ID; `N-STAGING` replaces only
`:staging-path`. `N-WORKTREE-COORDINATE` replaces exactly `:canonical-path
:git-common-directory :file-key` and preserves C/T and all clean/absence
facts. `N-PUBLICATION-INPUT` preserves only the normalized source-worktree,
staging root as RO, and immutable eligibility coordinate; it replaces mode,
publication-worktree, and existing-evidence with RO. `N-PUBLICATION` preserves
only the logical root, normalized source-worktree, staging root as RO, and
foreign-entry false; it replaces mode, publication-worktree,
existing-evidence, reservation fields, files-created, final-write-count,
identity-receipt-created-last, and outcome with RO. Thus initial publication
and object replay retain their original launcher provenance but have one
identical normalized semantic projection. Q/TQ/path/OQ/count/raw SHA/receipt ID
in every eligibility coordinate are immutable authority and are never
normalized.

Those functions apply only at child receipt paths `[i :tcb :process]`, `[i
:tcb :runtime-mxbean]`, `[i :tcb :java]`, `[i :tcb :git]`, `[i :tcb
:classpath j]`, `[i :tcb :environment]`, and `[i :process]`; coordinator paths
`:process`, `:runtime-mxbean`, `:java`, `:git`, `[:classpath-checks k :entries
j]`, and `:environment`; and launcher paths `:launcher-process`, `:java`,
`:git`, `[:classpath-precheck :entries j]`, `[:classpath-postcheck :entries
j]`, `:environment`, `:working-directory`, `:m-process`,
`:m-response-transport`, `:m-argv`, `:m-argv-id`, `:publication-input`,
`:publication`, and `:cleanup`. Launcher `:m-argv` is replaced by `N-ARGV`;
`:m-argv-id` is never copied and is recomputed as
`H([:gravity/sh07-b51-process-argv-id-v1 N-ARGV-VALUE])`. The coordinator's
`:launcher-argv-id` is recomputed from that same normalized vector and must
equal the launcher's recomputed `:m-argv-id`.
The M response staging receipt also uses `N-STAGING`. Here i is 0 or 1, j is 0-3, and k is
one of the eight literal phases. Receipt-only booleans/counters remain present;
raw path/PID/time/process-control values at the listed paths become RO.

`N-LAUNCHER-VALIDATION-INPUT` first removes exactly `:contract-validation` and
`:receipt-id`, then applies every launcher-path normalizer in the preceding
paragraph. Every PID, host, time, absolute path, file key, publication mode,
E/TE coordinate, and existing-mode observation is RO before
LAUNCHER-VALIDATION-INPUT-ID is computed. `N-CONTRACT-VALIDATION` preserves
validator source, predicate-spec ID, pre-discharge ID, result count/root, and
status; it replaces runtime input IDs with the six semantic payload value IDs
followed by normalized M/A/B receipt IDs and
LAUNCHER-VALIDATION-INPUT-ID. Runtime LAUNCHER-AUTHORITY is exactly
N-LAUNCHER-VALIDATION-INPUT. An unnormalized value rejects.

Recomputation follows the literal order in NORMALIZATION: six payload value
IDs; classpath manifest and semantic roots; normalized M/A/B argv IDs; A/B
request IDs; normalized A/B/M TCB IDs; normalized A/B/M responses with W byte
counts, R hashes, and response-preimage IDs; M/A/B process-handle IDs; three
source-session IDs; coordinator event IDs left-to-right and event root;
coordinator receipt; M response with W count/R hash/preimage ID;
N-LAUNCHER-VALIDATION-INPUT and its ID; N-CONTRACT-VALIDATION; launcher receipt
ID; contract document-event root and ROW-PLAN-ID; the reconstructed
pre-evidence rows and discharge ID; runtime rows and
runtime discharge ID; and final receipt ID. Raw argv IDs,
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
  :ipc IPC-COUNTERS :codec-tcb CODEC-TCB-COUNTERS :eligibility
  ELIGIBILITY-COUNTERS :git GIT-COUNTERS}`. Its literal schemas and aggregation
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
- `NONCLAIMS` is the single six-value vector defined in the discharge section;
  this tuple, every lifecycle receipt, and every authority map use those exact
  bytes and order.

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
`{:sessions WORK-SESSIONS :total 39}`. `WORK-SESSIONS` is exactly three
ordered maps for participants `[:a :b :m]`, each with exactly `:participant
:vector :total`; vector/total are always `[3 3 3 2 2]`/13. `SOURCE` is exactly
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
`CODEC-TCB-COUNTERS` is exactly `{:participants [:l :m :a :b]
:member-count 12 :object-precheck-count 12
:materialized-byte-check-count 12 :namespace-load-count 12
:manifest-root-check-count 4}`: every participant validates and loads the
three-member codec closure once. `ELIGIBILITY-COUNTERS` is exactly
`{:participants [:l :m :a :b] :q-coordinate-git-operation-count 20
:pre-discharge-git-operation-count 308 :git-operation-count 328
:q-coordinate-decode-count 4 :receipt-id-check-count 4
:pre-discharge-reconstruction-count 4 :pre-discharge-static-decode-count 252
:pre-discharge-document-descriptor-count 132
:pre-discharge-source-descriptor-count 48 :pre-discharge-masked-file-count 8
:pre-discharge-owned-c-artifact-count 108
:pre-discharge-a9-artifact-count 12 :pre-discharge-baseline-boot5-count 4
:content-match-count 4 :source-descriptors-match-count 4
:source-descriptors-root-match-count 4 :masked-root-match-count 4
:owned-c-root-match-count 4
:pre-discharge-match-count 4}`: every participant repeats the five-operation
immutable Q check and the separate 77-operation PRE reconstruction exactly
once. The equations are `4*5=20`, `4*77=308`, and `20+308=328`. These are
separate TCB/review-input channels and never change the three 12/12/39/1
governed source sessions.
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
:eligibility-coordinate :eligibility-observations
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
self-id-omits is exactly `[[:receipt-id]]`; nonclaims are NONCLAIMS; and
pre-post-equal is true. Receipt ID is
`H([:gravity/sh07-b51-g9-lifecycle-receipt-v1 RECEIPT-WITHOUT-ID])`; its Git
blob OID/raw hash and the review/lifecycle commits are external and never enter
that preimage.

Eligibility coordinate is copied from E's strictly reloaded identity receipt
and equals the complete Q descriptor. Eligibility observations are the exact
five-operation Q check defined above, repeated by the external G9 reviewer
before the 24 publication operations. They are a separate review-input channel
and do not alter, prefix, or enter the dense ordinals of `observations`.

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

A9's exact report change set is this normative architecture report plus
`docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md`.
The companion remains a nonauthority summary, but its counts must equal this
report and both blobs must be preserved through A9 integration. It grants no
additional architecture or lifecycle authority.

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

I9 implements the exact pure `VERIFY-UNAUTHORIZED-CROSS-FRAGMENT` equation in
the already-owned `sh07_b51_attempt32_materialize.clj`. The already-owned
checker-child and coordinator-child paths must invoke that same implementation,
and the existing checker-child and runtime-admission tests plus accepted and
rejected fixtures must prove the exact BAD-ACTUAL equality, nil committed row,
nil output, and derived diagnostic/fixture roots. The changed algorithm-source
bytes must rederive SEMANTIC-ALGORITHM-SOURCE-ROOT and every dependent product,
fuel, witness, and certificate identity. This adds no owned path, identity
domain, operation, counter, or witness member.

Exact tests:

- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_git_evidence_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_tier0_materializer_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_plan_linker_governed_read_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_checker_child_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_process_isolation_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_tcb_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_existing_object_replay_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_eligibility_object_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_contract_discharge_test.clj`

Fixtures are exactly
`bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn`
and
`bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn`.
The accepted fixture contains the exact twelve descriptors, 39 events, and the
complete literal PREDICATE-SPECS program vector. The
rejected fixture contains one case for every omitted/extra/reordered source or
event, direct-read escape, preflight/static-load violation, malformed UTF-8,
wire divergence, PID/host-only normalization mutation, empty-target race, and
checker-A-before-B fail-fast violation. It also contains mutations for every
PRE reconstruction count/path/root; every L1/L2/C2/C6/BOOT2/BOOT3/BOOT4
semantic-witness field; every fuel-subject ordinal, owner, kind, source root,
collection key/value/root, stage-semantic root, subject ID, stage projection,
construction-time host-path rejection, shared-bound union row, counter path,
derivation, row order, lowering-product operation join, and forbidden
receipt/product back-edge; fuel
exhaustion and missing fuel counters; a whole
discharge selected as a binding value; every prior-ref container/value path;
each phase-root domain/order; missing, duplicate, extra, or wrong-phase binding
and test result; deferred authority promoted to attained; each ownership path
spec/reservation/handoff; each TEST10/TEST13 post-I9 field; and every
governance result/cleanup bit. From the accepted source-closure fixture, the
contract-discharge test constructs one complete in-memory
PRE->Q->RUNTIME->E9->post-I9->post-G9->post-P9 replay whose five phase roots
and 62 bindings recompute byte-for-byte; post-phase coordinates are immutable
test Git objects, never invented fields in the source-closure fixture.
Load order and generic governance schema/validator bytes are unchanged.

`OWNED-C-PATHS` is the following exact 27-string unsigned-UTF-8-sorted vector:

```clojure
["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00581-p15-s23-stage2-compiler-artifact-binding.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00582-p15-s23-stage2-construct-emitted-core-plan.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
 "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn"
 "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_checker_child_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_tier0_materializer_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_git_evidence_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_plan_linker_governed_read_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_tcb_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_contract_discharge_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_eligibility_object_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_existing_object_replay_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_process_isolation_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj"]
```

The four governance path specs are literal maps differing only in workstream
ID: `sh07-b51-git-object-evidence-publication-architecture-v9`,
`sh07-b51-git-object-evidence-publication-implementation-v9`,
`sh07-b51-git-object-evidence-publication-g9-v9`, and
`sh07-b51-git-object-evidence-publication-p9-v9`, respectively. Every other
field equals the GOVERNANCE-PATH-SPEC constants above.

`A9-PATHS` is exactly:

```clojure
["docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md"
 "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/a9-lifecycle-receipt.edn"
 {:kind :content-addressed-terminal-record
  :directory "contracts/workstream-records/terminal"
  :workstream-id "sh07-b51-git-object-evidence-publication-architecture-v9"
  :record-digest-domain :sha256
  :filename-rule :workstream-id-hyphen-record-sha256-dot-json}]
```

`I9-LIFECYCLE-PATHS`
expands to exactly the 27 OWNED-C-PATHS strings followed by, in order, the
eligibility receipt, I9 integration receipt, I9 integration discharge, and I9
governance path spec. `E9-PATHS` is exactly:

```clojure
["docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/checker-a-identity.edn"
 "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/checker-b-identity.edn"
 "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/checker-a-output.edn"
 "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/checker-b-output.edn"
 "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/certificate-pair.edn"
 "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/linked-carrier-v4-candidate.edn"
 "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/identity-receipt.edn"]
```

The exact I9 suffix is:

```clojure
["docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-receipt.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-discharge.edn"
 {:kind :content-addressed-terminal-record
  :directory "contracts/workstream-records/terminal"
  :workstream-id "sh07-b51-git-object-evidence-publication-implementation-v9"
  :record-digest-domain :sha256
  :filename-rule :workstream-id-hyphen-record-sha256-dot-json}]
```

`G9-LIFECYCLE-PATHS` is exactly:

```clojure
["docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/g9-publication-tuple.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-lifecycle-receipt.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-formal-review.md"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-publication-discharge.edn"
 {:kind :content-addressed-terminal-record
  :directory "contracts/workstream-records/terminal"
  :workstream-id "sh07-b51-git-object-evidence-publication-g9-v9"
  :record-digest-domain :sha256
  :filename-rule :workstream-id-hyphen-record-sha256-dot-json}]
```

`P9-LIFECYCLE-PATHS` is exactly:

```clojure
["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-pin-publication-receipt.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-formal-review.md"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-final-discharge.edn"
 {:kind :content-addressed-terminal-record
  :directory "contracts/workstream-records/terminal"
  :workstream-id "sh07-b51-git-object-evidence-publication-p9-v9"
  :record-digest-domain :sha256
  :filename-rule :workstream-id-hyphen-record-sha256-dot-json}]
```

The exact
fixed paths for every named receipt/discharge are those declared at the top of
this report. A governance path spec resolves only after its record bytes are
final; the result must equal directory + `/` + workstream ID + `-` + lowercase
SHA-256(record bytes) + `.json`. It is never guessed or preseeded.

E9 may add only E9-PATHS. G9 may add only its tuple relative to B; its external
review lifecycle may add only the other G9-LIFECYCLE-PATHS. P9 may modify only
the four literal definitions in 00945 and the one quoted-carrier definition in
00947; its external review lifecycle may add only the remaining
P9-LIFECYCLE-PATHS. It may not change I9 logic, E9 payloads, the G9 tuple,
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

Each negative fixture has one primary ID and closed details. Future
implementation review must confirm all sources, schemas, bounds, fixed-fuel receipts, semantic
ordinals, two independent
child closures, 12/12/39/1 sessions, 13-per-session totals, seven E9 blobs, G9, dual
grammar, equal masked root, 1480 edges, object-only runtime authority, and
unchanged generic governance/load-order bytes. It also reconstructs all five
nonrecursive phase discharges, exact ownership and governance results, the
dual-checker semantic witness, all contract test evidence IDs, and final
obligation completeness. That review must run targeted positive and
negative tests, the governed SH-07 evidence suite, documentation validation,
and independent exact-tree review before acceptance.

Every row-plan binding and phase-local discharge above remains mandatory.
Architecture acceptance and integration only permit I9 creation. I9 content
freeze only permits the named evidence-eligibility review; that review only
permits E9 construction. I9 acceptance, admission, and integration are
separate. Integrated I9 only permits G9 construction; G9 external review,
acceptance, admission, and integration are separate. Only integrated G9 permits
P9. None alone completes SH-07.
