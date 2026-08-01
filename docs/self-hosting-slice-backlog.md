# Self-Hosting Slice Backlog

Status: active coordination plan; no completion status in this file overrides
the governing phase roadmaps or evidence artifacts.

## Purpose

This backlog turns Gravity's self-hosting critical path into a finite,
dependency-ordered set of implementation slices. It is a coordination
projection of `D2`, `BOOT1`, `BOOT3`, `BOOT5`, `BOOT7`, `BOOT8`, `TEST13`, and
the Phase 15 and Phase 18 implementation roadmaps. It is not an additional
normative source document in the 240-document inventory.

## Master Coordinator Goal

Iterate on Gravity through the 30 smallest independently provable vertical
slices in this plan until the language is fully self-hosted, feature-complete
against its documented contracts, and able to compile to every documented
target with accepted and rejected behavior, stable diagnostics, reproducible
artifacts, and seedless public proof.

The first bounded goal is genuine seed retirement at `SH-29`: a public Gravity
compiler must compile its own compiler, runtime subset, standard-library
subset, and package/build path without the Clojure seed in the release
boundary. After `SH-29`, repeat the applicable target gates as a countable
target-expansion matrix until every documented target passes. The first honest
self-hosting claim therefore does not overclaim that every target is already
implemented.

Use one master coordinator with a fixed, reusable pipeline of writer, auditor,
and next-slice planner agents. Maintain a single writer for shared or
monolithic files, prepare the next eligible slice while the current slice is
being validated, and run each expensive native or public proof only once per
stable candidate.

Gradually replace the monolithic bootstrap implementation with contract-aligned,
stage-owned namespaces:

- `gravity.reader`
- `gravity.syntax`
- `gravity.macro`
- `gravity.resolution`
- `gravity.checked-core`
- `gravity.types`
- `gravity.effects`
- `gravity.ownership`
- `gravity.safety`
- `gravity.mir`
- `gravity.optimization`
- `gravity.lowering`
- `gravity.diagnostics`
- `gravity.backend.c`
- `gravity.backend.llvm`
- `gravity.backend.wasm`
- `gravity.backend.jvm`
- `gravity.backend.js`
- `gravity.backend.mlir`
- `gravity.backend.gpu`
- `gravity.backend.hdl`
- `gravity.backend.workflow`
- `gravity.backend.query`
- `gravity.backend.mobile`
- `gravity.runtime`
- `gravity.standard-library`
- `gravity.package`
- `gravity.build`
- `gravity.tooling`
- `gravity.cli`
- `gravity.bootstrap`
- `gravity.release`

Assign each namespace an explicit contract boundary, public API, artifact
inputs and outputs, diagnostic ownership, tests, and dependency direction.
Keep compatibility entrypoints thin while implementation authority moves into
these namespaces. Permit parallel writers only when namespace ownership,
files, tests, and worktrees are genuinely disjoint. Serialize changes to the
shared CLI, diagnostic catalogs, integration manifests, coverage accounting,
completion attestations, and release boundaries.

Every integrated slice must identify its governing contracts, compile or emit
something real, reject something real, preserve provenance and deterministic
identity, pass proportionate regression and adversarial review, state its
unsupported boundaries honestly, and finish as a clean verified commit.

Do not claim target support, feature completeness, self-hosting, seed
retirement, or release readiness before the corresponding documented evidence
gates pass.

## Current Baseline

The repository has a working Clojure stage0, Gravity-authored compiler source
models, bounded stage2/stage3 application paths, and authenticated operation
slices through selected C, LLVM, and Wasm paths. The current governing evidence
still records:

- `:full-language-compiler-self-hosted? false`;
- `:clojure-seed-retired? false`;
- `:clojure-seed-boundary? true`.

Consequently, proof records, source inventories, public `check` bridges, and a
stage3 application demonstration do not by themselves complete a slice below.
A slice completes only when its named behavior is executed by the claimed
Gravity implementation and passes its exit gate.

## Uniform Slice Gate

Every slice from `SH-03` onward must satisfy all applicable items below before
it changes to `complete`:

1. The governing documents and exact claimed subset are named.
2. The implementation is executable Gravity behavior, not only a source model,
   manifest, proof record, fixture classifier, or Clojure implementation.
3. At least one real accepted `.gravity` input and its co-canonical `.qst`
   input traverse the slice.
4. At least one real rejected input per rejection family produces a stable
   structured diagnostic with the actual source path and no raw host exception.
5. Source spans, syntax identity, preserved compiler facts, effects,
   capabilities, safety classifications, and provenance survive as required by
   the governing pass contracts.
6. Artifact identities are deterministic and checkout-path neutral while
   retaining actual-path provenance outside identity inputs.
7. Mutation, substitution, stale-input, and malformed-graph probes fail closed
   at authenticated boundaries.
8. Focused tests, relevant regression tests, document validation, roadmap
   validation, coverage self-tests, and `git diff --check` pass.
9. The evidence names the remaining trusted computing base and makes no broader
   profile, target, release, performance, safety, or self-hosting claim.

Target-emission slices additionally require execution or validation by an
independent target toolchain. Bootstrap slices additionally require canonical
stage comparison, reproducible rebuild, and compiler-lineage evidence.

## Countable Backlog

Status meanings:

- `partial`: useful implementation evidence exists, but the exit gate is not
  met.
- `queued`: no current claim is made that the executable exit gate is met.
- `blocked`: its dependency gate is not yet complete.
- `complete`: all entry, exit, validation, and evidence gates passed. Only the
  coordinator may assign this state after reviewing the evidence bundle.

| ID | Slice | Depends on | Current state | Executable exit gate |
| --- | --- | --- | --- | --- |
| `SH-00` | Stabilize the current authenticated Wasm comparison work and restore a clean baseline | none | complete | The current bounded C11-C14-B1-B4 comparison path is committed, externally executed, adversarially reviewed, and the worktree is clean. |
| `SH-01` | Establish parallel-safe module and test ownership boundaries | `SH-00` | complete | Central routing, compiler modules, target modules, fixtures, tests, generated evidence, and coverage accounting have explicit single owners; new tests no longer require unrelated edits to the monolithic bootstrap test file. |
| `SH-02` | Generalize authenticated pass and artifact envelopes | `SH-00` | complete | One reusable contract verifies source revision, semantic inputs, preserved facts, artifact lineage, deterministic identity, graph bounds, and stale or substituted inputs across at least two different compiler stages. |
| `SH-03` | Complete the bootstrap reader and literal policy | `SH-01`, `SH-02` | complete | The Gravity reader, rather than a Clojure reread, handles the complete claimed bootstrap syntax and literal subset with Unicode, newline, delimiter, abbreviation, metadata, extension, and malformed-literal diagnostics. |
| `SH-04` | Complete syntax objects, hygiene, and origin chains | `SH-03` | complete | Gravity constructs and serializes syntax objects with stable identity, scopes, marks, metadata, source spans, generated origins, and adversarial graph validation. |
| `SH-05` | Implement the bootstrap macroexpander in Gravity | `SH-04` | complete | Gravity expands the macro subset required by all compiler sources, preserves hygiene and origins, rejects phase/profile/capability violations, and matches accepted stage0 diagnostics. |
| `SH-06` | Implement namespace and binding resolution in Gravity | `SH-05` | complete | Gravity resolves compiler namespaces, aliases, vars, lexical bindings, imports, visibility, cycles, and unresolved references for the complete bootstrap source set. |
| `SH-07` | Implement core-form semantics and lowering in Gravity | `SH-06` | partial | Every core form required to compile the compiler lowers to a canonical core artifact with stable evaluation order, arity, mutation, recursion, exception, and pattern diagnostics. |
| `SH-08` | Implement the bootstrap type checker in Gravity | `SH-07` | partial | Compiler sources receive genuine type facts for primitives, functions, collections, control flow, calls, records/unions, and required meta-programming values; ill-typed variants fail closed. |
| `SH-09` | Implement effect, capability, and profile checking in Gravity | `SH-08` | partial | The compiler's `:meta` and release-target paths reject undeclared effects, missing authority, ambient access, hidden services, and illegal profile/target assumptions before backend lowering. |
| `SH-10` | Implement ownership, lifetime, initialization, and memory checks in Gravity | `SH-08` | partial | The bootstrap subset has explicit ownership and memory facts, rejects invalid aliasing, use-after-move, uninitialized values, invalid escapes, and unsupported allocation regimes, and preserves facts into MIR. |
| `SH-11` | Implement the safety and numeric-safety classifier in Gravity | `SH-09`, `SH-10` | partial | Each dangerous operation reaches exactly one D8 outcome; bounds, overflow, division, casts, unsafe islands, and residual checks are represented in checked core and diagnostics. |
| `SH-12` | Implement MIR construction and verification in Gravity | `SH-11` | partial | Gravity builds verified C11 MIR for the whole bootstrap subset, including blocks, values, calls, branches, loops, errors, effects, ownership, safety facts, and source provenance; malformed MIR is rejected. |
| `SH-13` | Close functions, calls, recursion, and control flow through MIR | `SH-12` | partial | Multiple functions with typed parameters and results, direct and indirect calls required by the compiler, recursion, branches, loops, joins, and error exits execute through verified MIR. |
| `SH-14` | Close compiler-required data, layout, and allocation through MIR | `SH-12` | queued | The bootstrap subset can construct and access the strings, symbols, keywords, tuples, records, variants, maps, sets, vectors, and bounded mutable storage used by the compiler with explicit layout and allocation facts. |
| `SH-15` | Complete compiler diagnostics as Gravity data | `SH-13`, `SH-14` | partial | All bootstrap-stage rejection families originate from Gravity diagnostic construction and preserve stable ids, facts, spans, origin chains, severity, and remediation across stage comparison. |
| `SH-16` | Implement required MIR optimization passes in Gravity | `SH-15` | partial | Gravity executes the passes needed by the bootstrap compiler, verifies declared preserved/invalidated/regenerated facts, and never erases a check without surviving proof or a residual runtime check. |
| `SH-17` | Complete the target-lowering and backend interface boundary | `SH-16` | partial | A verified, authenticated, target-independent MIR artifact is accepted by a reusable C14/B1 interface that selects a declared target, ABI, runtime, providers, and artifact policy without source reinterpretation. |
| `SH-18` | Emit and run one complete native compiler target | `SH-17` | queued | A genuine `:native` program containing typed arguments, arithmetic, functions, collections, and explicit stdout lowers through C or LLVM to an independently verified and executed native artifact. |
| `SH-19` | Implement the minimal self-hosting runtime subset | `SH-11`, `SH-17` | queued | Startup, panic/error reporting, bounded allocation or explicit no-allocation paths, strings/bytes, file reads required by the compiler, stdout/stderr, and process exit are provided through declared effects and capabilities outside Clojure. |
| `SH-20` | Implement release-grade artifact emission and provenance | `SH-17` | partial | Gravity emits objects/executables, source maps, manifests, compiler identities, dependency graphs, safety summaries, and canonical provenance consumable by independent tools. |
| `SH-21` | Make the compiler source legal and executable under `:meta` | `SH-15`, `SH-17`, `SH-19` | queued | Every authoritative compiler module compiles under the declared `:meta` contract with no ambient filesystem, network, shell, registry, or undeclared host authority. |
| `SH-22` | Implement the bootstrap standard-library core | `SH-13`, `SH-14`, `SH-19` | queued | The Gravity implementations of collections, text, numeric helpers, errors, IO wrappers, artifact data, and compiler meta-programming helpers needed by the compiler pass conformance and safe-wrapper gates. |
| `SH-23` | Implement the bootstrap package and hermetic build subset | `SH-20`, `SH-21`, `SH-22` | queued | Gravity reads the project/lock inputs, resolves the fixed bootstrap dependency graph, enforces build effects and capabilities, and deterministically builds compiler components without Clojure orchestration. |
| `SH-24` | Implement the self-hosted compiler driver and CLI subset | `SH-18`, `SH-19`, `SH-20`, `SH-23` | queued | A Gravity executable owns source loading, pass orchestration, diagnostics, target selection, artifact writing, `check`, `compile`, and the compiler-internal verification commands needed for bootstrap. |
| `SH-25` | Compile every authoritative compiler component with Gravity | `SH-21`, `SH-22`, `SH-24` | queued | The Gravity compiler builds the reader, syntax, macro, analyzer, checked-core, MIR, optimizer, lowering, backend, diagnostic, runtime-subset, standard-library-subset, and package/build components from their Gravity sources. |
| `SH-26` | Rebuild the complete bootstrap compiler with the prior Gravity stage | `SH-25` | blocked | Stage N compiles the complete stage N+1 compiler and its required runtime/library/build components without invoking Clojure in the candidate compiler, runtime, or build path. |
| `SH-27` | Prove stage equivalence and deterministic fixed-point behavior | `SH-26` | blocked | Two clean rebuilds and the stage N/stage N+1 comparison pass the declared artifact, manifest, diagnostic, conformance, runtime-output, IR-modulo-id, and reviewed-delta modes. |
| `SH-28` | Close trusting-trust, provenance, and TCB evidence | `SH-27` | blocked | Diverse or independently anchored rebuild evidence, acyclic compiler lineage, environment/lock/build identities, SBOM, signatures, revocation checks, unsafe audit, and a TCB delta show the Clojure seed outside the release boundary. |
| `SH-29` | Emit and verify the public seedless Gravity release | `SH-28` | blocked | The public `gravity` binary builds and verifies the compiler, runtime subset, standard-library subset, package/build path, and release executable; full bootstrap conformance passes; final artifacts record `:full-language-compiler-self-hosted? true`, `:clojure-seed-retired? true`, and `:clojure-seed-boundary? false`. |

Count: 30 slices (`SH-00` through `SH-29`). The first self-hosting claim is
permitted only after all 30 are complete. A task may be implemented early, but
it cannot be credited before its dependencies and uniform gate pass.

## Dependency Waves and Parallel Ownership

The backlog permits parallel implementation while retaining one integration
owner:

| Wave | Coordinator-owned integration | Parallel lane A | Parallel lane B | Parallel lane C |
| --- | --- | --- | --- | --- |
| 0 | `SH-00` to `SH-02`; central bootstrap routing and coverage | Read-only adversarial review | Native toolchain fixture design | TCB and equivalence inventory |
| 1 | `SH-03`, `SH-04`; source/syntax integration | `SH-05`, `SH-06` after syntax contracts stabilize | `SH-08` type data model and fixtures | `SH-18` native ABI/toolchain harness without shared lowering edits |
| 2 | `SH-07`, `SH-12`; checked-core/MIR integration | `SH-08`, `SH-09` | `SH-10`, `SH-11` | `SH-19`, `SH-20` runtime/artifact leaf modules |
| 3 | `SH-15` to `SH-17`; pass and backend integration | `SH-13` functions/control | `SH-14` data/layout | `SH-18` native emission |
| 4 | `SH-21`, `SH-24`; compiler-driver integration | `SH-22` standard-library core | `SH-23` package/build subset | Stage comparison and mutation suites |
| 5 | `SH-25` to `SH-29`; bootstrap/release authority | Independent rebuild | Diverse verification | Provenance, SBOM, signing, and governance review |

The coordinator exclusively owns shared dispatch, generated release artifacts,
coverage accounting, completion attestations, and final status changes.
Parallel workers own leaf Gravity modules, new fixtures, and dedicated test
namespaces. A worker must not edit another lane's files or regenerate global
artifacts.

## Progress Accounting

Progress is reported as four numbers rather than a percentage inferred from
commit count:

```text
complete slices / 30
executable Gravity-owned slices / 30
slices with Clojure in their claimed execution boundary / 30
blocked slices / 30
```

The coordinator should also report the highest continuous completed prefix.
For example, completion of `SH-18` does not advance the prefix past `SH-07` if
`SH-08` remains incomplete.

Current reviewed accounting after `SH-06`:

```text
complete slices: 7 / 30
executable Gravity-owned slices: 6 / 30
slices with Clojure in their claimed execution boundary: 7 / 30
blocked slices: 4 / 30
highest continuous completed prefix: SH-00 through SH-06
```

`SH-02` credit is intentionally bounded to envelopes whose explicit reference
closure has at most 128 nodes, 128 edges, and shortest-discovery depth 64. The
Gravity module owns descriptor validation, semantic template construction, and
fresh template replay for both C13 and B1. Canonical encoding, SHA-256 request
resolution, identity equality enforcement, source reads, and final contextual
reconstruction remain in the declared Clojure stage0 boundary; `SH-02` does not
retire that seed or claim release-signature or verifier-correctness proof.

`SH-03` credit is bounded to the bootstrap reader and literal subset exercised
by its co-canonical fixture matrix and authoritative reader-source proof. The
Gravity reader owns Unicode-scalar decoding, newline and delimiter structure,
the ordered token stream, recursive form graph, bootstrap literals,
abbreviations, metadata, registered `inst` and `uuid` extensions, bounded
semantic-value reconstruction, and structured rejection. The public read, C2,
C3, P15, and check ingress consumes the authenticated Gravity result without a
second target-source read. The Clojure stage0 boundary still owns the initial
byte snapshot, compiler-plan execution, canonical digest resolution, the C2
compatibility adapter, central routing, and final artifact construction. The
compatibility facade is explicitly uncredited, and the remaining C2 boundaries
are the full-language literal surface, full-language abbreviation surface,
full-language extension registry, and host/seed retirement. This slice does not
claim overall C2 completion, compiler self-hosting, seed retirement, or release
readiness.

`SH-04` credit is bounded to the syntax-object, hygiene, metadata, source-span,
generated-origin, serialization, and graph-validation subset exercised by its
co-canonical fixture matrix. The Gravity syntax module constructs and verifies
the claimed products, preserves deferred ratio descriptors and exact SH-03/P15
reader lineage, and emits checkout-path-neutral syntax and artifact identities
while retaining actual paths in provenance. The SH-03 descriptor is freshly
verified through the reusable SH-02 envelope contract and independently bound
back to the current C2 semantic products; paired provenance alteration,
same-path cross-source replacement, stale-input, and malformed-graph probes
fail closed. The Clojure stage0 boundary still owns compiler-plan execution,
digest resolution, envelope binding, the C2/C3 compatibility adapter, and
central routing. This slice does not claim overall C3 completion, full compiler
self-hosting, seed retirement, package refresh, or release readiness.

`SH-05` credit is bounded to the compiler-required `defn` expansion subset
executed across the 36 authoritative compiler modules and their measured 1,276
top-level function definitions. The Gravity macro module owns expansion
recipes, hygiene marks, generated origins, build-effect and capability policy,
ordered expansion traces, and all ten structured C4 rejection families. Exact
stage0 body comparison, both source extensions, path-neutral identities,
literal-map preservation, replay and substitution checks, and complete
first/middle/last stream reconstruction passed. SH-04's implementation-local
syntax-stream ceiling is now 2,048 products, the exact capacity of its current
16-item chunks under the canonical width limit of 128; 2,049 is rejected before
downstream use, and aggregate stream carriers are checked separately. The
Clojure stage0 boundary still owns source reads, compiler-plan execution,
canonical digest resolution, envelope binding, compatibility routing, and final
artifact assembly. User-defined `defmacro`, the full macro surface, hierarchical
syntax-stream paging beyond 2,048 products, retained standalone carrier-audit
records, compiler self-hosting, seed retirement, packaged CLI refresh, and
release readiness remain outside this credit.

`SH-06` credit is bounded to the namespace and binding resolution subset
executed across the 41-path authoritative compiler-source inventory and the
co-canonical accepted/rejected fixture matrix. The Gravity
`gravity.resolution` module owns namespace, alias, import, var, lexical
binding, visibility, shadowing, dependency-cycle, profile, target, capability,
and foreign-boundary policy; deterministic binding and product identities; and
all ten structured C5 rejection families. Fresh authenticated SH-05 input,
exact Gravity replay, complete resolution-product reconstruction,
checkout-path-neutral identities with actual-path provenance, substitution and
mutation rejection, public C6 consumption, and bounded serialization passed.
The largest measured authentic artifact contained 19,445,399 carrier nodes at
depth 45 and maximum width 66,339 and serialized to 284,437,819 UTF-8 bytes.
Per-component transport limits are 33,554,432 nodes, depth 64, and width
131,072; whole-artifact limits are 67,108,864 nodes, depth 64, width 131,072,
and 1,073,741,824 serialized bytes. The Clojure stage0 boundary still owns initial
source reads, compiler-plan execution, canonical digest resolution,
authenticated-envelope binding, compatibility adaptation and central routing,
and final artifact assembly. The 41-module inventory is the current bootstrap
source set, not a broader full-language claim; the compatibility C5 facade
receives no implementation-authority credit. This slice does not claim overall
C5 or C15 completion, full compiler self-hosting, Clojure seed retirement,
packaged CLI refresh, or release readiness.

`SH-07` remains partial. Its current executable evidence covers 28 of the 41
authoritative compiler modules, including the complete bootstrap reader source.
For that reader source, Gravity consumes the authentic SH-06 fragment,
binding, and resolution products for 22,209 forms in 298 fragments, lowers 20
qualified definitions and 30 qualified calls, and preserves 24 quoted forms as
data rather than executable references or calls. The measured authenticated
request contains 1,446,007 carrier nodes at depth 25 and maximum width 22,209;
its exact UTF-8 scalar payload is 24,579,750 bytes. The generated core template
contains 9,172,831 nodes and 522,603,428 scalar bytes, and its generated digest
requests contain 6,695,903 nodes and 372,860,236 scalar bytes. Request ingress
is bounded at 8,388,608 nodes and 268,435,456 scalar bytes; template and
resolved-core output are independently bounded at 16,777,216 nodes and
536,870,912 scalar bytes; generated digest output is independently bounded at
8,388,608 nodes and 536,870,912 scalar bytes. All four boundaries also enforce
depth 256 and width 65,536. Request ingress is checked before lowering, and
each generated product is checked before downstream recursive hashing,
verification, or use. Stable path-neutral core identities, actual-path
provenance, fresh digest resolution, replay, and altered input, graph, binding,
resolution, template, digest, and resolved-product rejection are required by
the authoritative proof. The Gravity C6 core-lowering source is also covered
directly: its 9,109 source bytes authenticate into 7 fragments containing 348
forms, 289 bindings including 27 local bindings, and 13 resolutions, then lower
to 220 canonical core nodes with 7 definitions and no calls, references, or
keyword lookups. Its cache-free authoritative proof passed with 32,888 carrier
nodes at depth 8, width 348, and 498,205 UTF-8 scalar bytes; co-canonical
`.gravity` and `.qst` inputs retain equal semantic identities and distinct
actual-path provenance. This independent zero-lookup source does not broaden
the existing nine-module, 63-lookup B16 cohort. The Gravity L2 core-language
semantics source is covered by the same path: its 17,557 bytes authenticate
into 9 fragments containing 600 forms, 286 bindings including 24 local
bindings, and 15 resolutions, then lower to 465 canonical core nodes with 9
definitions and no calls, references, or keyword lookups. Its cache-free
authoritative proof passed with 44,353 carrier nodes at depth 8, width 600, and
706,674 UTF-8 scalar bytes; its co-canonical source-extension and provenance
gates also passed. The C4 compatibility source adds 4,053 authenticated source
bytes in 4 fragments containing 232 forms, 270 bindings including 8 local
bindings, and 70 resolutions. It lowers to 201 canonical core nodes with 4
definitions, 31 calls, 50 references, 13 conditionals, and 3 functions. Its 11
`get` operators are ordinary resolved symbol calls rather than keyword-headed
map lookups, so its canonical keyword-lookup product count is zero. The
cache-free authoritative proof passed with 29,163 request carrier nodes at
depth 17, width 270, and 427,463 UTF-8 scalar bytes. Co-canonical extension,
provenance, replay, and alteration gates passed, and the B16 cohort remains
unchanged. The C3 compatibility source adds 4,149 authenticated source bytes in
4 fragments containing 238 forms, 270 bindings including 8 local bindings, and
70 resolutions. It lowers to 207 canonical core nodes with 4 definitions, 31
calls, 50 references, 13 conditionals, and 3 functions. Its 11 `get` operators
are likewise ordinary resolved symbol calls with zero keyword-lookup products.
The cache-free proof passed with 29,439 request carrier nodes at depth 17,
width 270, and 432,437 UTF-8 scalar bytes. The source remains explicitly
compatibility-only: it grants no authentication credit, does not supply the
authoritative syntax result, and retains the `gravity.bootstrap.syntax` route.
The C5 compatibility source adds 4,154 authenticated bytes with the same
4-fragment, 232-form, 270-binding, 70-resolution structure as C4. It lowers to
201 canonical core nodes with 4 definitions, 31 calls, 50 references, 13
conditionals, and 3 functions. Its 11 `get` operators are ordinary resolved
calls with zero keyword-lookup products. Its cache-free proof passed with
29,163 request carrier nodes at depth 17, width 270, and 428,299 UTF-8 scalar
bytes. The source remains compatibility-only, denies authentication and
authoritative resolution credit, and retains the `gravity.resolution` route.
The C15 diagnostics source adds 6,580 authenticated bytes in 5 fragments
containing 218 forms, 279 bindings including 17 local bindings, and 9
resolutions. It lowers to 122 canonical core nodes with 5 definitions, 2
functions, and 2 quoted descriptor bodies, with no calls, references, or
keyword-lookup products. Its keyword-rich diagnostic maps therefore remain
quoted data rather than executable lookup operations. The cache-free proof
passed with 26,264 request carrier nodes at depth 8, width 279, and 382,555
UTF-8 scalar bytes. This proves source representation only: Clojure stage0
remains the declared seed/compiler/verifier boundary and no executable
diagnostic, rendering, redaction, stream-verification, or runtime-authority
credit is taken. The C16 incremental-compilation source adds 24,810
authenticated bytes in 17 fragments containing 833 forms, 331 bindings
including 69 local bindings, and 29 resolutions. It lowers to 547 canonical
core nodes with 17 definitions: 11 data contracts and 6 quoted function
bodies. Its keyword-rich descriptors produce no calls, references, or
keyword-lookup products. The cache-free proof passed with 57,611 request
carrier nodes at depth 8, width 833, and 945,725 UTF-8 scalar bytes. This
proves source-model representation only and grants no cache construction,
storage, invalidation, revalidation, proof reuse, filesystem/network, or
release-grade incremental authority. The C17 plugin/pass API source adds 25,254
authenticated bytes in 17 fragments containing 878 forms, 329 bindings
including 67 local bindings, and 27 resolutions. It lowers to 603 canonical
core nodes with 17 definitions: 12 data contracts and 5 quoted function bodies,
with no calls, references, or keyword-lookup products. The cache-free proof
passed with 59,392 request carrier nodes at depth 8, width 878, and 974,774
UTF-8 scalar bytes. This proves source-model representation only: production
plugin loading and pass execution remain disabled, central integration remains
pending, and no sandbox, trust, whitelist, domain, cache, filesystem/network,
or release authority is credited. The B16 cohort remains unchanged. The
SH-21 meta-compiler-legality source adds 28,374 authenticated bytes in 42
fragments containing 2,778 forms, 419 bindings, and 1,109 resolutions. It
lowers to 2,292 canonical core nodes with 42 definitions, 459 calls, 848
references, 151 conditionals, 26 local bindings, and 42 function forms. Its 161
`get` calls divide into 139 literal-keyword calls and 22 dynamic-key or index
calls; all are ordinary resolved calls, so the canonical keyword-lookup product
count remains zero and the B16 cohort remains frozen at nine modules and 63
lookups. The cache-free proof passed with 199,593 request carrier nodes at depth
27, width 2,778, and 3,301,719 UTF-8 scalar bytes. It also proves exact
forward/mutual-recursion binding identity, co-canonical extension parity,
path-neutral identity, actual-path provenance, replay, and altered-product
containment. This is bounded source execution evidence only: authenticated
SH-15 diagnostics, SH-17 lowering, SH-19 runtime services, whole authoritative
compiler execution under `:meta`, and seedless execution remain pending. The
C18 compiler-verification source adds 30,982 authenticated bytes in 22
fragments containing 1,148 forms, 394 bindings, and 42 resolutions. It lowers
to 640 canonical core nodes with 22 definitions: 12 data contracts and 10
quoted function bodies, with no calls, references, or keyword-lookup products.
The cache-free proof passed with 75,762 request carrier nodes at depth 8, width
1,148, and 1,266,488 UTF-8 scalar bytes. The proof pins all quoted artifact
schemas, their actual omissions from declared required fields, the absent
replay builder, the nine-entry diagnostic catalog, the source-owned verifier
checklist, co-canonical extension parity, path-neutral identity, actual-path
provenance, replay, and altered-product containment. This proves source-model
representation only: production verifier execution, schema enforcement,
certificate and evidence checking, translation validation and replay, release
decisions, and plugin/backend conformance execution remain pending. The B16
cohort remains unchanged. The C7 type-checker source adds 39,567 authenticated
bytes in 47 fragments containing 3,320 forms, 480 bindings, and 1,205
resolutions. It lowers to 2,656 canonical core nodes with 47 definitions, 39
executable SH-08 functions, three quoted source-model functions, 466 calls, 928
references, 133 conditionals, 24 `let` forms, 13 loops, and 15 recurs. Its
159 `get` calls divide into 152 literal-key calls and seven dynamic-key calls;
all are ordinary resolved calls rather than keyword-headed lookup forms, so
the canonical keyword-lookup product count remains zero and B16 stays frozen.
The cache-free proof passed with 229,955 request carrier nodes at depth 24,
width 3,320, and 3,827,443 UTF-8 scalar bytes. The proof pins exact contracts,
bounds, diagnostics, pending work, executable/quoted boundaries, structural
limitations, co-canonical extension parity, path-neutral identity, actual-path
provenance, replay, and altered-product containment. This proves bounded source
execution and reconstruction only: production type-checker execution,
authenticated coordinator adaptation, resolved typed-artifact identity,
complete inference and constraint solving, dynamic/layout/schema/ownership
legality, complete diagnostic execution, and SH-08 completion remain pending.
The C8 effect-checker source adds 44,102 authenticated bytes in 40 fragments
containing 3,301 forms, 410 bindings, and 1,078 resolutions. It lowers to 2,788
canonical core nodes with 40 definitions: 34 executable SH-09 functions and
three quoted source-model functions. Its 178 `get` calls divide into 169
literal-key calls and nine dynamic-key calls; all are ordinary resolved calls
rather than keyword-headed lookup forms, so the canonical keyword-lookup
product count remains zero and B16 stays frozen. The cache-free authoritative
proof passed with exact source revision
`sha256:8be72ed8adbe830992ee990ba0cb23bb06ce7d29859360afa7d937f0833e0096`,
complete capability proof, and no failed checks. The bounded source recognizes
`:error/raise` as a declared language-level effect with no capability,
provider, grant, or resource subject. This does not establish a production
effect checker, complete schema enforcement, an authenticated SH-08 adapter,
effect inference, latent or transitive call effects, handlers, namespace or
module summaries, complete runtime-profile policy, MIR preservation, or SH-09
completion.
The C9 ownership-checker source adds 35,894 authenticated bytes in 31
fragments containing 2,320 forms, 370 bindings, and 741 resolutions. It lowers
to 1,964 canonical core nodes with 31 definitions: 21 executable SH-10
functions and three quoted source-model functions. Its 134 `get` calls divide
into 129 literal-key calls and five dynamic-key calls; all are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The cache-free
authoritative proof passed with exact source revision
`sha256:b4fdf1022eb6eb25d091f1c918332c7b1393b6850acf4bb6988d8b8dbb2269e0`,
complete capability proof, and no failed checks. The bounded executable surface
covers owned-mutable initialization, read, immutable and mutable borrow,
borrow end, move, consume, and one function-return escape policy with exact
ordered facts and structured rejection reasons. Request and event maps remain
shallow non-exact schemas; digest values are shape-checked only; whole-carrier
cycle, depth, width, and scalar preflight is absent; the 1,024-event uniqueness
and execution paths remain recursive; lifetime endpoints are generic numbers;
the capability proof identifier is preserved but not authenticated; and the
verifier is same-implementation recomputation. Persistent-copy semantics,
field and range splitting, regions, arenas, linear resources, task, actor, and
FFI transfer, runtime borrow checks, unsafe-audit execution, authenticated
SH-08/SH-09 adaptation, MIR preservation, and SH-10 completion remain pending.
The C10 safety-analysis source adds 68,327 authenticated bytes in 73 fragments
containing 5,652 forms, 542 bindings, and 2,046 resolutions. Its calibrated
lowering contains 4,709 canonical core nodes with 73 definitions: 63
executable SH-11 functions and three quoted source-model functions. Its 337
`get` calls divide into 334 literal-key calls and three dynamic-key calls; all
are ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 380,720 carrier nodes at depth 38, maximum width 5,652,
and 6,314,034 exact UTF-8 scalar bytes; every unchanged B16 bound passes. The
bounded executable surface classifies index bounds, integer overflow, division,
numeric casts, and shifts into exactly one of `:proven-safe`,
`:runtime-checked`, `:rejected`, or `:unsafe-island`, with structured reason
surfaces and recomputing verification. Production safety-analysis authority,
complete contract and diagnostic schema enforcement, memory and lifetime
safety, regions, linear resources, FFI, concurrency, taint, generated-code
safety, floating-point and elementary-function safety, optimization
invalidation, authenticated SH-09/SH-10 convergence, MIR preservation, and
SH-11 completion remain pending.
The C12 domain-IR source adds 61,946 authenticated bytes in 85 fragments
containing 5,711 forms, 569 bindings, and 2,073 resolutions. Its calibrated
lowering contains 4,591 canonical core nodes with 85 definitions: 72
executable SH-14 functions and four quoted source-model functions. Its 254
`get` calls divide into 209 literal-key calls and 45 dynamic-key or indexed
calls; all are ordinary resolved calls rather than keyword-headed lookup forms,
so the canonical keyword-lookup product count remains zero and B16 stays
frozen. The SH-06 request contains 383,280 carrier nodes at depth 23, maximum
width 5,711, and 6,372,295 exact UTF-8 scalar bytes; every unchanged B16 bound
passes. The bounded executable layout surface now enforces exact per-kind and
nested schemas, canonical bounded identifiers and payloads, iterative
structural preflight, bounded origin chains, portable ASCII symbol and keyword
sizes, supported profile/target pairs, path-neutral identity, and contained
candidate verification. Production domain-IR authority, complete C12 contract
and diagnostic schema enforcement, authenticated SH-12 MIR input,
target-specific layout, actual allocation, field-offset calculation, pointer
and lifetime layouts, and SH-14 completion remain pending.
The Clojure stage0 boundary still owns initial source reads, compiler-plan
execution, canonical digest resolution, authenticated-envelope assembly,
central routing, and final artifact construction. The remaining 13 compiler
modules and the complete core-form, mutation, recursion, exception, and pattern
surface must pass the same executable gates before `SH-07` can receive
completion credit.

## Beyond First Self-Hosting

After `SH-29`, target expansion becomes a separate matrix. Each canonical
target must repeat the applicable `SH-17` through `SH-20` target gates plus
backend conformance, runtime/provider, packaging, and external execution gates.
The first seedless compiler therefore establishes the compiler authority needed
to implement every target; it does not claim that every target is already
implemented.

## Outputs and Artifacts

This plan expects each completed slice to produce:

- a slice manifest naming dependencies and governing documents;
- accepted and rejected fixtures;
- executable artifacts and stable diagnostics;
- focused, regression, and adversarial test results;
- authenticated pass/artifact records where applicable;
- a TCB boundary record;
- stage comparison evidence for bootstrap slices;
- an evidence-ledger entry linked from the owning phase roadmap.

## Conformance Criteria

- All 30 slices have unambiguous dependencies and executable exit gates.
- No slice receives completion credit for scaffold, source inventory, proof
  metadata, or public fixture routing alone.
- Parallel work has non-overlapping file ownership and a single integration
  owner for central routing and generated evidence.
- The count and continuous completed prefix can be reproduced from reviewed
  evidence bundles.
- `SH-29` cannot pass while any compiler, runtime, standard-library,
  package/build, verifier, or release path inside the public boundary depends on
  Clojure.
