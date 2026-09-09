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
| `docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md`; `209-boot7-self-hosting-validation-and-equivalence-plan.md`; `210-boot8-bootstrap-artifact-provenance-specification.md` | Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | fresh L/M/A/B topology, stage comparison, TCB delta, immutable provenance receipts, cleanup and replay | `SH07-B51-CHECKER-PROCESS` |
| `docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md` | Oracles, Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | A/B/main/I9 oracle identity/applicability, profile/target/backend/runtime/numeric mode, compared observables, diagnostics, permitted divergence and reproducers | `SH07-B51-NORMALIZE-DIVERGENCE` |
| `docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md` | Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:applies` | assumptions, trusted basis, checker/version/input/claim IDs, invalidation and counterexamples; E9 labels these validation receipts, not formal proof | `SH07-B51-PROOF-SCOPE` |
| `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md` | Stages, Requirements, Outputs and Artifacts, Rejection Rules, Conformance Criteria; `:preserved` | exact stage/trusted inputs/generated artifacts/source/compiler/lock/target/output identities, expected deltas and TCB delta; no stage advancement | `SH07-B51-PROOF-SCOPE` |
| `bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn` | complete closed EDN contract; `:applies` only within its declared B47/B15 scope | active status, fresh authoritative process, nonauthoritative iteration cache, source/checked-core rechecks, verifier/catalog IDs, immutable receipts, bounds/cache key and cleanup | `SH07-B51-PROOF-SCOPE` |
| `docs/self-hosting-slice-backlog.md`; `docs/self-hosting-slice-ownership.edn` | Countable Backlog, Dependency Waves and Parallel Ownership, Progress Accounting, Outputs and Artifacts, Conformance Criteria; coordinator-owned, future-path-conventions, slice-owners; `:applies` | SH-07 remains partial; exact I9/E9/G9/P9 owner/path/reservation/handoff; core/evaluation/arity/mutation/recursion/exception/pattern exit gate remains open | `SH07-B51-OWNER` |
| `contracts/verification-catalog.edn`; `bootstrap/gravity/src/gravity/checked_core.gravity`; `docs/workstream-governance.md`; `contracts/workstream-governance.json` | complete closed records and Lifecycle/Admission; `:applies` | exact C objects/catalog verifier root/checked-core revision, lifecycle state transitions, review/admission receipts and final cleanup | `SH07-B51-VERIFY-CATALOG` |

The following obligations apply and cannot be discharged by a Git hash alone:

- L1/L2 and C6/C11 require the original source order, authentic owning
  top-level definition/root, checked-core revision, MIR/error-exit shape,
  origin, and diagnostic semantics to survive lowering. V9 binds the exact C
  blobs for L1/L2, `checked_core.gravity`, the seven B5.1 modules, and compiler
  emitter, and carries their source IDs, spans, owners, roots, and fact digests
  through both independent checker products.
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
  independent semantic reconstruction, mutation rejection, verifier-root and
  verification-catalog identity, bounded proof execution, replay, and
  self-hosting evidence. E9 carries those identities and results; this
  architecture expressly does not convert them into aggregate SH-07,
  self-hosting, release, or seed-retirement authority.

At each precheck and postcheck the verifier reads the C blob for
`contracts/verification-catalog.edn`, the C blob for
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`, and the
C blob for `bootstrap/gravity/src/gravity/checked_core.gravity`. It records
their path, mode `100644`, blob OID, byte count, raw SHA-256, and the catalog's
declared SH-07 verifier-root identity. The three postcheck rows must equal the
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
governed projection has exactly eight blobs, in this order:

| Ordinal | Role | Module | Path |
| ---: | --- | ---: | --- |
| 0 | `:module` | 0 | `bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` |
| 1 | `:module` | 1 | `bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity` |
| 2 | `:module` | 2 | `bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity` |
| 3 | `:module` | 3 | `bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity` |
| 4 | `:module` | 4 | `bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity` |
| 5 | `:module` | 5 | `bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` |
| 6 | `:module` | 6 | `bootstrap/gravity/src/gravity/sh07_b51/root8.gravity` |
| 7 | `:compiler` | absent | `bootstrap/gravity/p15_s23/compiler.gravity` |

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

One session prefetches the exact eight C blobs above, in order, and strictly
decodes each UTF-8 vector once. Products consume exactly 22 events: module
validation 0-6, census 0-6, link authority 0-6, and compiler emitter once. The
session finalizes once and rejects a missing, duplicate, reordered, late, or
unconsumed event.

I9 replaces exactly four existing physical-read domains in 00945: the seven
per-module reads in `p15-s23-sh07-b51-module-source-valid*` use domain
`:module-validation`; the seven reads in
`p15-s23-sh07-b51-derive-callsite-census` use domain `:callsite-census`; the
seven reads in `p15-s23-sh07-b51-build-link-authority-from-census` use domain
`:link-authority`; and the compiler read in
`p15-s23-sh07-b51-current-emitter` uses domain `:compiler-emitter`. There is no
fifth physical-read site or domain. All four consume the one prefetched session
rather than invoking their current `Files/readAllBytes` or `slurp`. A lexical
and call-graph fixture rejects any additional file/object read reachable from
00945 or 00947. Differential fixtures run unmodified main and I9 on identical
accepted and rejected inputs and require identical products and diagnostics.
Module, compiler, descriptor, event, edge-authority, and five-hole mutations
must remain visible and reject.

Evidence mode performs, for each of eight acquisitions, a pre-read and
post-read check of the isolated C worktree's exact HEAD, tree, index, clean
state, no untracked files, and full projection: exactly 8 prechecks, 8
postchecks, and zero runtime-object checks. Runtime derives equal C/T from the
E9 certificate/carrier and performs exactly 8 object prechecks and 8 object
postchecks, with zero evidence-worktree checks. It ignores current main HEAD,
index, worktree, ref, and all live `.gravity` source bytes. Its Clojure/JVM/JAR
executable substrate is the separately bound trusted-runtime TCB below, not
Gravity evidence authority. Arbitrary wrapper output grants no
authority; a private 00947 load-local gate validates carrier, pins, receipt,
and session identity. G9 is static publication proof, never runtime input.

Each A, B, and M session records exactly 8 acquisitions, 8 strict decodes, 22
events, and 1 finalize. Accepted `:new` work is `[3 3 3 2 2]`, total 13;
accepted `:existing` work is `[2 2 2 2 2]`, total 10. Sentinels are
nonauthoritative. The authenticated edge census remains exactly 1480.

## Independent checker processes

L is the narrow already-loaded host launcher in
`gravity.tools.sh07-b51-git-evidence`. L may locate `/usr/bin/git`, validate the
clean C worktree, read immutable Git objects, materialize the four-entry
C-derived classpath, and construct exactly one M `ProcessBuilder`. L may not
load the M, A, B, bootstrap, checker, or materializer namespaces, decode a
Gravity source blob, compute a checker product, author a semantic payload, or
validate a carrier. It may assemble only the closed final process receipt from
M's six staged payloads plus L's retained process observations. Its source
closure, argv, environment, Git observations, classpath
materialization, process handle, bounded stdout/stderr capture, and cleanup
are recorded in `LAUNCHER-RECEIPT`. L is a declared local TCB boundary, never
Gravity semantic or evidence authority.

M is a fresh coordinator/materializer JVM launched by L from the exact
C-derived classpath and entrypoint specified below. M's transitive loaded and
source closure has
no require, load, resolve, invoke, alias, Var, or symbol reference to either
checker namespace or entrypoint. The current direct requires in
`sh07_b51_attempt32_tier0_materializer.clj` are the mandatory negative fixture.
This rule covers every helper M loads before or during the run.

The only child entrypoint is
`gravity.tools.sh07-b51-attempt32-checker-child`, at
`bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj`.
After strict validation it lazily loads exactly one mapping:

| CLI role | EDN role | Checker namespace |
| --- | --- | --- |
| `producer-census` | `:producer-census` | `gravity.tools.sh07-b51-attempt30-producer-census` |
| `independent-verifier-census` | `:independent-verifier-census` | `gravity.tools.sh07-b51-attempt30-independent-census` |

An A child referencing or loading B, or B referencing or loading A, rejects in
its loaded-namespace and source-closure receipt.

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

CP comes from `java.class.path`, uses Darwin `:`, and has exactly four ordered
semantic entries:

1. `project/bootstrap-clojure-src` at C/T, materialized from Git objects
2. `org.clojure/clojure` 1.12.5, providing `clojure.core`
3. `org.clojure/core.specs.alpha` 0.4.74, providing
   `clojure.core.specs.alpha`
4. `org.clojure/spec.alpha` 0.5.238, providing `clojure.spec.alpha` and
   `clojure.spec.gen.alpha`

Each binds canonical path, logical coordinate, file key, count, and SHA-256. For
entry 0, M enumerates every mode-100644 blob below
`C:bootstrap/clojure/src`, in Git byte-path order, and builds an exact manifest
row `[relative-path blob-oid byte-count sha256]`. It materializes those bytes
into a fresh private mode-0700, symlink-free directory, checks every row before
child start and after child exit, and computes entry byte-count as the sum and
entry SHA-256 as the canonical manifest-root hash. The directory path and file
keys are receipt-only; the C objects and manifest root are authority. No
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
 "gravity.tools.sh07-b51-attempt32-materialize"
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
`:launcher-source-closure :git :java :classpath-precheck :m-argv :m-argv-id
:environment :working-directory :m-process-handle-id :m-process
:m-response-transport :m-response-byte-count :m-response-sha256
:m-response-preimage-id :classpath-postcheck :cleanup :receipt-id`. `:m-process` has the
same PID, parent, command, arguments, runtime-MXBean, start, exit, and distinct
process fields required of A and B. `:cleanup` is exactly
`{:finally-entered true :stdin-closed true :stdout-closed true
:stderr-closed true :process-terminated true :tasks-joined true
:materialized-directory-deleted true :staging-resolved true}`. M's self receipt must join L's retained
process handle, report L as parent, and reproduce the same normalized argv,
environment, and classpath roots. The launcher receipt is included in E9's
identity receipt and its normalized ID is recomputed during object-only review.
L begins independent bounded stdout and stderr drains before waiting for M,
allows no M stdin bytes, and applies the same 1 GiB stdout, 1 MiB stderr, and
shared deadline/forced-termination rules used for A and B.

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

M retains each `Process` and pipe as authority. It immediately captures PID,
parent PID, command, args, and startInstant. The child reports current PID and
parent plus RuntimeMXBean name matching `^([0-9]+)@(.+)$`, start-time millis,
JVM/Java, and classpath. Reported and retained PIDs join; parent is M; A, B, and
M are distinct. PID reuse or mismatch fails closed. PID, host, and time are
required well-formed receipt-only fields, never authority comparisons; the
semantic ordinals above are ordering authority.

## Exact canonical identity contract

Every phrase in this report of the form "the canonical hash under DOMAIN of
PAYLOAD" is normative shorthand for the exact call
`gravity.c2-artifact-identity/reader-canonical-hash` on the two-element vector
`[DOMAIN PAYLOAD]`. It is never a hash of PAYLOAD alone and never a caller's
choice of serializer. The authoritative implementation is the exact C/T Git
object closure containing
`bootstrap/clojure/src/gravity/c2_artifact_identity.clj`,
`bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj`, and
`bootstrap/clojure/src/gravity/digest.clj`. Their mode, blob OID, byte count,
and raw SHA-256 are recorded in every A/B/M TCB and verified before and after
each process.

That implementation recursively projects maps, sets, vectors, and sequences
with `reader-canonical-value`; deterministically orders the decorated map/set
entries exactly as the C implementation does; then binds `*print-length*` and
`*print-level*` to nil and `*print-meta*` to true, applies `pr-str`, hashes the
resulting UTF-8 bytes with SHA-256, renders 64 lowercase hexadecimal digits,
and prefixes `sha256:`. The C implementation and
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

Wire bytes are a separate exact function `W(v)`: recursively order maps and
sets using the C-derived `ordered-value` comparator from
`gravity.tools.sh07-b51-attempt32-materialize`, bind the same three print Vars,
emit `pr-str`, append exactly one LF byte, then UTF-8 encode. Strict reload must
reproduce `v` and `W(v)` byte-for-byte. Raw byte identity is
`R(b) = "sha256:" + lowercaseHex(SHA256(b))` using
`gravity.digest/sha256-bytes-hex`. Every fresh M/A/B process runs the same
closed parity vector for every H, W, and R identity family before evidence is
eligible.

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
| source event root | `:gravity/sh07-b51-source-event-root-v1` | exact dense event vector |
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

The existing checker artifact, semantic, executable, dependency-closure, and
identity-manifest domains remain the literal role-specific domains in
`sh07_b51_attempt32_checker_identity/checker-specs`. The result artifact/output
domains remain the two literal domains printed in the response schema. No
domain is inferred from a field name.

## Closed IPC request

The request is one strict canonical EDN form plus EOF:

```clojure
{:schema :gravity/sh07-b51-checker-child-request-v1
 :role :producer-census|:independent-verifier-census
 :content {:commit C :tree T :source-descriptors-root SHA :masked-root SHA}
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
          :stderr-max-bytes 1048576}}
```

C/T are 40 lowercase hex Git IDs and SHA is `sha256:` plus 64 lowercase hex.
All key sets are exactly shown. Role and mode are the shown keyword
alternatives. Bounds have exactly 17 keys; limits are the shown keyed map.

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
            :source-session {:acquire-count 8
                             :decode-count 8
                             :event-count 22
                             :finalize-count 1}
            :process {:child-count 1
                      :start-count 1
                      :exit-count 1
                      :overlap-count 0}
            :ipc {:request-count 1
                  :response-count 1
                  :request-max-bytes 4194304
                  :response-max-bytes 1073741824
                  :stderr-max-bytes 1048576}}
 :tcb {:manifest-id SHA
       :role ROLE
       :process PROCESS
       :runtime-mxbean RUNTIME-MXBEAN
       :java EXECUTABLE
       :git EXECUTABLE
       :classpath CLASSPATH
       :environment ENVIRONMENT
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
  :source-descriptors :source-descriptors-root :event-root :counters
  :finalized`. Schema is `:gravity/sh07-b51-source-read-receipt-v1`; the eight
  descriptors are closed below; counters equal response source counters;
  finalized is true.
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
- `PROCESS` has exactly `:pid :parent-pid :command :arguments :start-instant`.
  PIDs are positive integers; command/args equal the retained handle; start is
  ISO-8601 and receipt-only.
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

A source descriptor has exactly `:ordinal :role :module-ordinal :path
:blob-oid :byte-count :sha256`. Ordinal, role, module ordinal (`nil` only for
compiler), and path match the table. OID is 40 lowercase hex, count nonnegative,
and SHA strict. Descriptor and event roots canonically hash exact ordered
vectors.

An event has exactly `:ordinal :domain :source-ordinal :module-ordinal
:consumer :source-sha256 :product-id`. Events 0-6 use domain
`:module-validation`, source/module ordinals 0-6, and consumer
`gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*`; events 7-13 use
`:callsite-census`, source/module ordinals 0-6, and consumer
`gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census`; events 14-20 use
`:link-authority`, source/module ordinals 0-6, and consumer
`gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census`; event 21
uses domain `:compiler-emitter`, source ordinal 7, nil module ordinal, and
consumer `gravity.bootstrap/p15-s23-sh07-b51-current-emitter`. Both hashes are
strict SHA-256. The event root hashes this exact dense vector.

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
census limit. Because C/T and all eight inputs are exact, A and B independently
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
inputs, writes it once, reloads all seven blobs, and atomically renames the
complete directory to the E9 relative root. In `:existing`, L requires the
already-published seven files to be byte-identical and writes nothing. On every
failure, L's `finally` terminates processes, closes/joins streams, and deletes
an unpublished stage. L records process provenance but cannot change any of
the six semantic payloads. This one-way M-response -> L-receipt construction
has no response/receipt identity cycle.

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
 :receipt-id-normalization NORMALIZATION
 :receipt-id SHA
 :nonclaims [:release :self-hosting :seed-retirement
             :hostile-host :sh07-complete]}
```

Each child receipt has exactly `:role :ordinal-triple :argv :argv-id :request
:request-id :response-transport :response-byte-count :response-sha256
:response-reconstruction :exit-code :counters :tcb`. A's triple is `[2 3 4]`
and B's `[6 7 8]`. Request, counters, and TCB are the exact closed values above.
Response transport is the exact transport map. Response reconstruction has
exactly `:schema :role :checker-identity-payload :checker-output-payload
:source-read-receipt :production :sentinel :diagnostics`; the two payload
fields are respectively `:checker-a-identity.edn`/`:checker-a-output.edn` or
the B paths, and all other fields are the literal response values. Substituting
the two referenced E9 payload values and the child receipt's counters/TCB into
the closed response schema must reproduce canonical stdout byte-for-byte and
therefore its byte count, SHA-256, and `response-preimage-id`. Exit code is 0.

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
:counters {:acquire-count 8 :decode-count 8 :event-count 22
:finalize-count 1}}`. Every child ID is recomputed from its embedded
`source-read-receipt`; M's ID is recomputed from the coordinator receipt. The
ordinal-9 and ordinal-10 rows carry that same M ID. No object identity, path,
PID, host, or time participates.

Coordinator receipt has exactly `:process :runtime-mxbean :java :git
:classpath :environment :loaded-namespace-receipt :host-surface
:source-read-receipt :counters :a-validated-before-b-builder
:b-validated-before-m-session :launcher-process-handle-id
:launcher-argv-id`; both booleans are true. Its launcher IDs equal the
independently recomputed `LAUNCHER-RECEIPT` values. Its loaded namespace
receipt excludes both checker namespaces and child entrypoint. Its process
counters and source receipt join the G9 aggregates and semantic ordinals.

PAYLOAD-JOINS has exactly `:checker-identity-sha256s
:checker-output-sha256s :certificate-pair-sha256
:linked-carrier-sha256`. The vectors are ordered A then B and every digest
equals a fresh raw hash of the named sibling blob. NORMALIZATION is exactly
`{:replace-map-keys [:canonical-path :working-directory :file-key :pid
:parent-pid :host :start-instant :start-time-millis :command :arguments
:java-class-path :response-byte-count :staging-path]
:argv-replacements [:java-executable :classpath :repository-root]
:recompute-order [:payload-semantic-sha256s :classpath-manifest-root
:loaded-namespace-root :host-surface-root :tcb-manifest-id :normalized-argv-id
:normalized-response-preimage-id :normalized-response-byte-count
:normalized-response-sha256
:process-handle-id :m-argv-id :m-response-preimage-id :m-process-handle-id
:launcher-receipt-id :coordinator-event-ids :coordinator-event-root
:receipt-id]}`. All checker envelopes and provenance use the exact semantic
string `"1970-01-01T00:00:00Z"` for `:completed-at` before any invocation,
reproduction, certificate, evidence, carrier, output, or pin ID is computed.
No wall-clock value enters a payload or payload-derived identity. First, every
value at a listed map key is replaced by
`:gravity/receipt-only`; exact JAVA, CP, and REPO strings in every argv are
replaced by their three listed keywords, longest string first. Replacing
`:command`, `:arguments`, and `:java-class-path` removes their duplicate raw
JAVA/CP strings; normalized argv remains the semantic command coordinate.
Replacing `:working-directory` and `:canonical-path` removes every duplicate
REPO scalar. Next, every
derived value named in `:recompute-order` is recomputed from the already
normalized direct inputs, left to right. `:payload-semantic-sha256s` hashes the
six parsed sibling values after the same recursive key replacement; raw
payload SHA-256 values remain review observations but are replaced by these
semantic hashes in the receipt-ID projection. Normalized response hashes are
computed by reconstructing the whole response with normalized TCB, argv, and
payload values. `:normalized-response-byte-count` is measured from those
canonical bytes before their SHA-256. Raw `argv-id`, TCB/root IDs, response
byte count/IDs/SHA, process-handle
IDs, event IDs/roots, and payload hashes are never copied into a later
normalized preimage. No other value changes. `receipt-id` is the canonical hash under
`:gravity/sh07-b51-checker-process-identity-receipt-id-v1` of that normalized
whole receipt with only `:receipt-id` omitted. E/TE, own blob OID/raw SHA, G9,
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
- `EVIDENCE` is exactly `{:commit E :tree TE :parent C}`.
- `OBSERVATIONS` is the exact 39-entry dense-ordinal vector specified below.
  Each entry has exactly
  `:ordinal :operation :argv :stdin-byte-count :stdin-sha256 :exit-code
  :stdout-byte-count :stdout-sha256 :stderr-byte-count :stderr-sha256
  :parsed-result`. Operation is one of `:rev-parse-commit :show-tree :ls-tree
  :cat-file-blob :merge-base-is-ancestor :diff-tree`. The validator constructs
  the sole allowed absolute Git argv. Counts/hashes/output parsing and required
  success code recompute. Stdin is always empty: count 0 and SHA-256
  `sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`.
- `ARTIFACT-DESCRIPTORS` is exactly seven ordered
  `[path oid count sha256]` vectors for E9.
- `SOURCE-DESCRIPTORS` is exactly the eight ordered closed source maps.
- `MASKED-ROOT` is exactly `{:coordinates COORDINATES :preimage-root SHA
  :postimage-root SHA :equal true}`. `COORDINATES` is exactly:

```clojure
[{:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
  :load-ordinal 808 :form-ordinal 13
  :owner gravity.bootstrap/p15-s23-sh07-b51-expected-m3-id
  :rhs-kind :digest-pin}
 {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
  :load-ordinal 808 :form-ordinal 14
  :owner gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-a-id
  :rhs-kind :digest-pin}
 {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
  :load-ordinal 808 :form-ordinal 15
  :owner gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-b-id
  :rhs-kind :digest-pin}
 {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
  :load-ordinal 808 :form-ordinal 16
  :owner gravity.bootstrap/p15-s23-sh07-b51-expected-semantic-root
  :rhs-kind :digest-pin}
 {:path "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
  :load-ordinal 812 :form-ordinal 59
  :owner gravity.bootstrap/p15-s23-sh07-b51-authoritative-linked-carrier-value-v4
  :rhs-kind :quoted-carrier-v4}]
```
- `COUNTERS` is exactly `{:work WORK :source SOURCE :process PROCESS-COUNTERS
  :ipc IPC-COUNTERS :git GIT-COUNTERS}`. Its literal schemas and aggregation
  are specified below.
- `RECEIPT-JOINS` is exactly `{:content-commit C :content-tree T
  :evidence-commit E :evidence-tree TE :identity-receipt-sha256 SHA
  :payload-sha256s VECTOR :excluded VECTOR}`. Payload hashes are the six
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
and `O` its bound blob OID. The only argv/result pairs are:

| Operation | Exact argv | Exact parsed-result keys |
| --- | --- | --- |
| `:rev-parse-commit` | `[/usr/bin/git -C REPO rev-parse --verify X^{commit}]` | `{:commit X}` |
| `:show-tree` | `[/usr/bin/git -C REPO show -s --format=%H%n%T%n%P X]` | `{:commit X :tree Y :parents VECTOR}` |
| `:diff-tree` | `[/usr/bin/git -C REPO diff-tree --no-commit-id --name-status -r X^ X]` | `{:parent PARENT :commit X :entries VECTOR}` |
| `:merge-base-is-ancestor` | `[/usr/bin/git -C REPO merge-base --is-ancestor ANCESTOR DESCENDANT]` | `{:ancestor ANCESTOR :descendant DESCENDANT :is-ancestor true}` |
| `:ls-tree` | `[/usr/bin/git -C REPO ls-tree -z X -- P]` | `{:commit X :path P :mode "100644" :type "blob" :oid O}` |
| `:cat-file-blob` | `[/usr/bin/git -C REPO cat-file blob O]` | `{:oid O :byte-count COUNT :sha256 SHA}` |

Every token in the argv table is a separate string; the rendered table omits
quotes only for readability. Parent vectors and diff entries are closed vectors
of 40-hex IDs and exactly `{:status :added|:modified|:deleted :path STRING}`
maps. V9 imposes no parent identity on C beyond the accepted I9 lifecycle and
required integrated A9 ancestry. E has exactly `[C]`; the E diff has exactly
the seven `:added` evidence paths. The observation plan is: ordinals
0-1 rev/show C; 2-3 rev/show E; 4 diff E; 5 ancestry C-to-E; 6-12 E `ls-tree`
in artifact order; for each `j` in 0-6, ordinal `13+j` is `cat-file` of the OID
returned at ordinal `6+j`; 20-27 C `ls-tree` in source order; for each `j` in
0-7, ordinal `28+j` is `cat-file` of the OID returned at ordinal `20+j`; 36 rev
C, 37 rev E, and 38 ancestry C-to-E as independent postchecks. Any other
count/order/argv/result rejects. The tuple does not observe its own G9 object or
bytes; the lifecycle join below supplies that external binding and avoids a
self-cycle.

G9 `WORK` is exactly
`{:mode MODE :sessions SESSIONS :total TOTAL}`. `SESSIONS` is exactly three
ordered maps for participants `[:a :b :m]`, each with exactly `:participant
:vector :total`; vector/total are `[3 3 3 2 2]`/13 in `:new` or
`[2 2 2 2 2]`/10 in `:existing`; TOTAL is 39 or 30. `SOURCE` is exactly
`{:sessions SESSIONS :acquire-count 24 :decode-count 24 :event-count 66
:finalize-count 3}`; its sessions are exactly three ordered maps with
`:participant :acquire-count :decode-count :event-count :finalize-count`, each
equal to 8/8/22/1. `PROCESS-COUNTERS` is exactly
`{:launcher-count 1 :coordinator-count 1 :builder-count 3 :child-count 3
:start-count 3 :exit-count 3 :m-launch-count 1 :a-b-overlap-count 0
:m-parent :launcher :checker-parent :m
:semantic-ordinals [0 1 2 3 4 5 6 7 8 9 10]}`. `IPC-COUNTERS` is exactly
`{:checker-request-count 2 :checker-response-count 2 :m-response-count 1
:rejection-count 0 :request-max-bytes 4194304
:response-max-bytes 1073741824 :stderr-max-bytes 1048576}`. L applies the
response and stderr caps to M under the same bounded process deadline; M has no
stdin payload because its closed request is the exact argv above.
`GIT-COUNTERS` is exactly `{:operation-count 39
:stdin-byte-count 0 :stdout-byte-count N :stderr-byte-count 0}`, where N is the
sum of the 39 recorded stdout byte counts. No participant, counter, or aggregate
may be missing or added. These are the tuple's pre-G9 C/E observations. The
post-G9 lifecycle observation receipt separately has exactly 24 operations and
cannot be folded into, omitted from, or preseeded in this counter.

`gravity.tools.sh07-b51-git-evidence --mode g9-lifecycle-join` reads the tuple
from exact G9 and proves E9 ancestry and tuple absence from E9. The static C
accepted fixture contains only the closed derivation grammar, literal tuple
path, seven literal payload paths/order, required `100644` modes, and the
string productions `g9-tuple-path:`, `g9-tuple-sha256:`, and `g9-row:`. It
contains no E/G commit, tree, OID, count, or SHA value. The lifecycle tool
derives one exact tuple-path string, one exact tuple-SHA string, and seven exact
row strings from immutable candidate objects after G9 exists and validates them
against that grammar. It then executes the closed external G9 observation plan:

1. `rev-parse --verify B^{commit}` returns B.
2. `show -s --format=%H%n%T%n%P B` returns exactly B, TB, and the parent vector
   recorded by the accepted I9 integration receipt.
3. `merge-base --is-ancestor C B` succeeds.
4. `rev-parse --verify G^{commit}` returns G.
5. `show -s --format=%H%n%T%n%P G` returns exactly G, TG, and parent vector
   `[B]`.
6. `merge-base --is-ancestor B G` succeeds.
7. `diff-tree --no-commit-id --name-status -r B G` returns exactly one
   `:added` entry for the tuple path and no other entry.
8. `diff-tree --raw -z B G` returns one entry with old mode `000000`, new mode
   `100644`, all-zero old OID, new OID OG, status `A`, and TUPLE-PATH.
9. `ls-tree -z G -- TUPLE-PATH` returns exactly mode `100644`, type `blob`, and
   tuple OID OG.
10. `cat-file blob OG` reproduces the exact tuple bytes, byte count, raw
   SHA-256, and parsed value already validated from the tuple.
11. `ls-tree -z B -- TUPLE-PATH` succeeds with empty stdout.
12. `ls-tree -z E -- TUPLE-PATH` succeeds with empty stdout.

After tuple decoding and full receipt reconstruction, operations 1-12 repeat in
the same order as ordinals 12-23 and must reproduce byte-identical parsed
results and stdout hashes. The external plan therefore has exactly 24
operations, not the tuple's 39 pre-G9 observations.

The external result is the closed map
`{:publication-base B :publication-base-tree TB
:publication-base-parents BASE-PARENTS :content-commit C
:content-tree T :content-is-ancestor-of-base true :evidence-commit E
:evidence-tree TE :publication-commit G :publication-tree TG :parents [B]
:base-is-ancestor true :diff [{:status :added :path TUPLE-PATH}]
:diff-stdout-sha256 DIFF-SHA :raw-diff-stdout-sha256 RAW-DIFF-SHA
:tuple-path TUPLE-PATH :tuple-mode "100644" :tuple-oid OG
:tuple-byte-count N :tuple-sha256 SHA :tuple-absent-at-base true
:tuple-absent-at-evidence true :pre-post-equal true}`. The tool derives B/TB
from the integrated I9 lifecycle and derives TG, OG, N, and all hashes; none is
caller supplied. The independently created post-G9 formal review
artifact and the separate
`sh07-b51-git-object-evidence-publication-v9` lifecycle record persist the
exact result plus each command's argv, exit, stdout/stderr count, and raw
SHA-256. That lifecycle record's accepted fixtures must include literal
`publication-base:B`, `publication-base-tree:TB`, `publication-commit:G`,
`publication-tree:TG`, `publication-parent:B`,
`publication-diff-sha256:DIFF-SHA`,
`publication-raw-diff-sha256:RAW-DIFF-SHA`, `tuple-oid:OG`, and
`tuple-sha256:SHA` bindings.
Generic governance remains shape-only; the workstream-specific review enforces
their semantics.

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

I9 may change only:

- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj`

Exact tests:

- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_git_evidence_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_tier0_materializer_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_plan_linker_governed_read_test.clj`
- `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_checker_child_test.clj`

Fixtures are exactly
`bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn`
and
`bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn`.
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
child closures, 8/8/22/1 sessions, 13/10 totals, seven E9 blobs, G9, dual
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
