# Checked scalar JavaScript modules

## Supported production route

This increment connects ordinary hosted/safe `:js-ts` Uniform source to
freshly checked Core, verified compositional scalar MIR, B1/C14 target
eligibility, and executable Node 20 / ES2022 ESM modules. Uniform source
owners retain semantic admission, target legality and lowering rules. The
Clojure seed remains the compiler host and transports and publishes artifacts.

The supported language subset is I64 and Bool, checked I64 addition, eager
local bindings, Boolean branches, fixed-arity nonrecursive direct calls,
declared numeric-error effects, actual exports and private helpers. Runtime
I64 arguments and results use JavaScript BigInt with signed 64-bit checks.

The production API is
`gravity.compiler.application/current-source-to-js-module`. Its caller supplies
an ordinary source file, an explicit source policy and emission options. The
API constructs fresh products itself; it accepts no supplied Core, MIR,
verifier report, construction token or target eligibility certificate.

Public CLI dispatch and complete clean-checkout acceptance remain the following
integration increment. This route retains the Clojure/JVM stage0 compiler host.

Governing contracts are D1/D2/D3/D8/D9, C6-C11/C14/C15, B1/B6, P4 and SAFE9.
R1/R4 govern managed runtime services and failures; R10/SAFE7 govern checked
host-call boundaries; R11 governs provider authority. B13/S9/PKG3 and PKG10
govern artifact identities, evidence and provenance. BOOT8 governs retained
stage0 lineage. S7's native binary layout and target-triple fixtures are not
claims of this primitive JavaScript ABI. Historical source-interface admission
and release pins are not renewed.

## Starting state

Latest connected main and fetched Git main both resolve to
`4c0ada9438cbcf20587447541ecfd16ab08e99a7`, the merge of PR #1439.
The isolated implementation branch is `codex/js-checked-scalar-modules`.
PR #1429 supplies the current-source construction boundary and PR #1439
supplies scalar function composition. Earlier execution reports remain tied
to their recorded revisions; this increment reproduces its own prerequisites.

Draft PR [#1441](https://github.com/burn2delete/uniform/pull/1441) was opened
before implementation. The observed main was fetched again after the first
implementation checkpoint and remained unchanged. Relevant open-PR file lists
and workstream ownership were inspected; the older module-analysis extraction
candidates were treated as historical proposals, and unrelated changes were
preserved.

The scope excludes recursion, closures, imports, collections, additional
arithmetic, source I/O, other backends, optimization work, self-hosting, seed
retirement, automation changes and release packaging.

## Exact source-to-module invocation

The checked-in ordinary source is
[`examples/checked-js/acceptance.gravity`](../examples/checked-js/acceptance.gravity):

```clojure
(ns acceptance.scalar
  (:profile :hosted)
  (:target :js-ts)
  (:effects #{:error/raise})
  (:capabilities #{})
  (:safety :safe)
  (:exports [score]))

(defn add [left :- I64 right :- I64]
  (+ left right))

(defn score [choose-sum :- Bool left :- I64 right :- I64]
  (let [sum (add left right)]
    (if choose-sum
      sum
      left)))
```

Its UTF-8 source SHA-256 is
`6f6d2b94071cf17eee814d176d1dcf5ce9b6906ddbd79c6ac42ed472d7d11162`.
From the repository root, with Clojure and actual Node 20 on `PATH`, run:

```sh
clojure -J-Xmx2g -M -e '
(require (quote [gravity.compiler.application :as compiler]))
(prn
  (select-keys
    (compiler/current-source-to-js-module
      "examples/checked-js/acceptance.gravity"
      {:profile :hosted :target :js-ts :effects #{:error/raise}
       :capabilities #{} :safety :safe}
      {:output-path "target/acceptance-js" :target :js-ts
       :runtime :node20 :language :es2022 :module-format :esm})
    [:status :paths :runtime-observation]))
'
```

This command uses production namespaces only. The output directory must be
fresh. Existing destinations and paths outside the existing output policy are
rejected. Compilation checks the actual Node major version and generated ESM
syntax; it never invokes `score` to discover an answer.

The explicit source policy has exactly the five keys shown above. Its target
must agree with the actual source declaration. The canonical target is
`:js-ts`; the existing `:js` alias is accepted in emission options and recorded
as the requested target alongside the canonical target in provenance. It does
not rewrite the source declaration or permit a `:jvm` policy to masquerade as
JavaScript. The source, checked Core, MIR, runtime-check policy, target request
and manifest remain `:js-ts`; `:compiler-host` continues to name the JVM seed.

After this single compilation, supply runtime arguments to Node:

```sh
node --input-type=module -e '
  import {score} from "./target/acceptance-js/program.mjs";
  console.log(score(true, 3n, 7n).toString());
  console.log(score(false, 7n, 3n).toString());
  console.log(
    score(true, 9007199254740993n, 9007199254740994n).toString()
  );
'
```

The actual Node `v20.20.2` stdout was:

```text
10
7
18014398509481987
```

Additional calls use this same module, without recompilation. The standalone
acceptance copies the entire output directory to a fresh directory under
`/tmp`, changes the Node working directory there and imports `./program.mjs`.
The generated module has no imports or runtime reference to Clojure or the
repository; the source map embeds the original source text.

The documented production API command completed with exit 0 and
`:status :emitted`. Manual execution of that exact output also returned `15`
for `score(true, -10n, 25n)` and `100` for `score(false, 100n, -50n)`, then the
maximum I64, minimum I64 and `-1` for the boundary combinations. Both required
stdout and the additional success/error observations matched byte-for-byte
after copying the directory outside the checkout. The copy ran with a cleared
environment containing only Node's PATH; it required no Clojure or repository
file access.

## Construction authority and lowering

The source route extends the existing namespace/module gate and the actual
Uniform C6-C10/C11 contracts. JavaScript admission is restricted to the
supported hosted/safe scalar route with empty external capabilities and either
empty effects or `#{:error/raise}`. Checked addition requires the declared
numeric-error effect. Unsupported forms still fail the existing semantic
pipeline. The JVM path remains supported and separately tested.

The original source constructor creates checked Core and compositional MIR,
invokes their real verifiers, and hands the genuine application to the backend
inside a live lexical construction context. The raw MIR retains
`:verification-status :pending`. Its separate actual `:mir-verification` is
validated and preserved. No raw flag is changed into an acceptance certificate.

The context compares the exact original application and current source and
compiler-owner bytes. Arbitrary callbacks, substituted products and expired
contexts cannot supply that authority. The initialized backend retains its
captured constructor/authenticator functions; tested public-Var substitutions
do not replace them. Authority is checked at entry and again immediately before
publication. The handoff registration is removed in `finally`. This is a
trusted seed-host boundary, not a claim against hostile replacement of the
host loader before initialization.

The current Uniform target owners provide these builder/verifier pairs:

| Owner | Builder | Verifier | Responsibility |
| --- | --- | --- | --- |
| C14 | `c14-build-checked-js-lowering` | `c14-verify-checked-js-lowering` | Source/target agreement, scalar operation legality, checked guard relations, complete semantic evidence and eligible acyclic CFGs. |
| B1 | `b1-build-checked-js-input` | `b1-verify-checked-js-input` | Independently reconstructed exported ABI, runtime/provider policy and target input contract. |
| B6 | `b6-build-checked-js-program` | `b6-verify-checked-js-program` | Static scalar statements, calls, explicit block edges, joins and error/source records. |

Their selected current source bytes, function identities, arities and semantic
identities are captured. Every actual verifier reconstructs its candidate; the
host checks the complete expected report structure. Missing or substituted
reports fail. The adapters do not refresh historical whole-runtime instruction
packet pins or use that packet to reach emission.

The Clojure printer emits named functions and statically named block functions
from B6's verified program. Explicit successor arguments fill join-block
parameters. `:conditional-join` copies the already selected value. Direct calls
follow verified callee and binding identities, including a source-defined `+`.
There is no emitted MIR interpreter, opcode loop, answer table or source
evaluator. A checked addition computes a BigInt sum, verifies its I64 range,
then exposes the checked value to the succeeding operation. Eager helper calls
therefore execute before the conditional that consumes their results.

## JavaScript ABI and errors

The public signature of the example is equivalent to:

```typescript
score(chooseSum: boolean, left: bigint, right: bigint): bigint
```

I64 values must be primitive BigInts in the inclusive range
`-9223372036854775808n` to `9223372036854775807n`. Bool values must be primitive
Booleans. Export wrappers check exact argument count first, then each argument's
type and range in declaration order. They perform no coercion. Boxed values,
Numbers, null, undefined and non-Boolean conditions fail the boundary. Only
actual namespace exports receive wrappers and declarations; `add` stays private.

Errors are real JavaScript `Error` instances. The following contract fields are
enumerable own properties. EDN keyword values use colon-prefixed strings in
JavaScript. Error messages are explanatory; callers should inspect the typed
fields.

| Numeric error property | Value |
| --- | --- |
| `name` | `"UniformNumericError"` |
| `type` | `":error/numeric"` |
| `kind` | `":overflow"` |
| `numericMode` | `":checked"` |
| `width` | `64` |
| `signed` | `true` |
| `operation` | `":add"` |
| `operationId` | Original guarded-addition identity, equal to `source.mirInstructionId`. |
| `checkId` | Actual C11 runtime-check identity. |
| `operands` | The two already evaluated primitive BigInt operands, in MIR operand order. |
| `source` | Actual source path, content hash, original span, Core node identity and MIR instruction identity. |

For `score(false, 9223372036854775807n, 1n)`, checked overflow belongs to
`(+ left right)` on line 10, inside the eager private helper. It must be raised
before the false branch can return `left`.

These properties preserve the complete scalar C11 numeric-error payload using
the documented JavaScript names. The `operands` array retains native BigInts;
there is no automatic JSON conversion. A caller recording JSON must explicitly
choose a representation, for example `error.operands.map(String)`. Acceptance
checks the original primitive types before using that decimal-string projection
to compare with the actual reference error.

ABI failures have `name: "UniformABIError"`, `type: ":error/abi"`, and these
properties:

| Property | Contract |
| --- | --- |
| `kind` | `":arity"`, `":type"` or `":range"`. |
| `function` | Actual exported name, such as `"score"`. |
| `argumentIndex` | Zero-based parameter index; null for an arity failure. |
| `expected` | Required arity, `"bigint"`, `"boolean"` or `"signed I64"`. |
| `actualType` | JavaScript `typeof`, with null reported as `"null"`; null for an arity failure. |
| `actualArity` | Present for arity failures only. |
| `source` | Original exported function's path, content hash, span, Core node identity and MIR identity. |

The source record's JavaScript keys are `path`, `contentHash`, `span`,
`coreNodeId` and `mirInstructionId`. Numeric errors identify the actual guarded
addition, rather than a generated helper location. The Error host provider and
its `:error/raise` behavior are declared in the runtime contract. Source effects
and failures of invalid foreign arguments are represented separately. Primitive
values are immutable, module startup only defines functions, and the runtime
requires no external source capability, import, I/O service or polyfill.

## Artifact and provenance inventory

The production publisher assembles all six files before publishing the fresh
directory through the existing sibling staging directory and atomic move.
Rejected compilation publishes no new partial output. A staging failure cleans
up the staging directory.

| File | Contents |
| --- | --- |
| `program.mjs` | Standalone Node 20 / ES2022 ESM functions, static CFG, checked arithmetic and checked exported wrappers. |
| `program.d.ts` | Actual exported signatures using `bigint` and `boolean`; no private helper exports. |
| `program.mjs.map` | Standard v3 mappings plus original function, operation, guard, terminator, Core, syntax and generated-origin identities; embedded source text. |
| `package.json` | ESM entry, declarations entry, private package, definitions-only side-effect policy and Node `>=20 <21` engine. |
| `manifest.edn` | Versioned target/ABI/runtime/numeric policy, source and compiler identities, fresh eligibility/report evidence, semantic fact tables and emitted artifact byte identities. |
| `provenance.edn` | Current source/policy, selected source and target owners, host transports, verified input, actual verifier reports, target product digests, requested/canonical target and primary artifact byte identities. |

Source Map v3 columns use UTF-16 units; the printer converts C2's Unicode-scalar
columns and retains the original span's unit. Representative acceptance decodes
the standard VLQ mappings and compares them with generated-origin coordinates.
A separate Boolean source fixture uses an astral Unicode character in a
parameter name and checks the generated-origin UTF-16 conversion.

The manifest hashes the four primary files and provenance by exact UTF-8 bytes.
Its canonical `:artifact-id` binds its contents without a circular self-hash.
The API's returned `:artifact-inventory` additionally records the final
`manifest.edn` byte hash and count. EDN sidecars preserve the owners' sets,
vectors and maps. Owner source observations are current development evidence;
the host transport files are captured at backend load and rechecked before
publication. They are not a self-hosted or seedless release certificate.

The [recorded production receipt](compiler-checked-js-modules-receipt.edn)
binds the actual documented invocation's source, verified application/Core
identities, compiler bindings, source policy, reports, artifact bytes, Node
results, error objects and standalone comparison. Its six output files are:

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| `program.mjs` | 8,838 | `6fa1c638c4db0fe879d2b9e3a2ffa3459b2991458b47a24b3139c376665960aa` |
| `program.d.ts` | 294 | `2df2f99cafd5c40543bb0847b8908cfe18695b4cc2746033dcba5a59b1aaff27` |
| `program.mjs.map` | 33,114 | `aeb2120b923d7f7ed7bbede290043918a75d7124557b31890b9addbf3c1aa87e` |
| `package.json` | 215 | `c6d0db1953749cca827eada497f3ef5b39926d147218fadc9863585bd91a88cc` |
| `manifest.edn` | 215,061 | `373f44a3d6e32cabbbf6592b6839434633684f3368abbcbba2c5daccb357334b` |
| `provenance.edn` | 39,997 | `a07c29b06822ff08ca60fd17c474ab156665753dd66dc51a86ffec56899a3d20` |

These are execution-specific identities: original absolute source paths and
selected compiler source identities are part of the evidence. The receipt
preserves the corresponding exact development context rather than claiming
path-independent artifact bytes.

## Reproduced boundary and failed attempts

The untouched JVM baseline selector
`gravity.compiler.application-test/scalar-functions-and-control-flow` passed
1 test and 135 assertions. The representative genuine JavaScript request first
failed at `C11-MODULE`, missing fact
`:application-explicit-hosted-safe-jvm-policy`, before target lowering.

Subsequent attempts remain separate evidence:

| Attempt | Terminal observation and correction |
| --- | --- |
| First changed source probe | `L2-UNKNOWN-SYMBOL` for a newly introduced target default binding; corrected the actual source binding. |
| Second changed source probe | Genuine `:js-ts` source, Core, MIR and runtime-check facts; fresh reports passed and raw MIR stayed pending. |
| First full backend selector | `B1-INPUT`, missing `:current-target-owner-source-and-api`; reduced compiler plans omit module exports, so the host checked a nonexistent field. Corrected the owner source/function binding check. No target directory was published. |
| First corrected backend selector | 424 reported checks: 423 passed and one error when the standalone-copy test helper passed a `java.nio.Path` directly to `io/file`. Corrected the helper conversion; this failed selector was not counted as passing. |
| First remaining regression batches | Stopped with exit 130 for the numeric-payload correction. Boolean execution had returned successfully; the genuine-tamper selector had one attribution assertion failure because its token mutation made a `SubVector` carrier. Replaced that fixture with a plain persistent vector so the intended semantic mutation reaches the genuine-product check. |
| Numeric-payload review | Initial Node execution passed the listed error fields, but review found C11's check identity and evaluated operands were missing. Added them to the Uniform B6 contract and printer, with exact differential assertions. Prior successful files and logs remain bound to their original checkpoint. |
| Former legacy JS rejection test | Six assertions still expected the old supported-target list. Updated that list while retaining rejection of the legacy unsupported source route; the complete 10-test namespace then passed 315 assertions. |
| Expired launcher | The previous scratch-owned Clojure launcher disappeared and returned exit 127. Restored the official CLI locally; this was distinct from a compiler rejection. |

Compiler checkpoint `7c39d7bd63f61fe131b08e57e37919997b4e096d` has tree
`4ed21f422cd2ccc69eaa47aeb8c7ddbadeb8fe06`, identical to local checkpoint
`e0333bbfe47f1ee685600d21fc316f8827acc726`. The subsequent local corrected
checkpoint is `1fc31d91d5df267f6cd1a25ae695f7491fba6f06`, published as
`398b631408cc465ca24bc8b2f197226a995b63fd` with the identical tree
`8bfbea214574ae2d97f17b9ea68c28d88e23aab0`. The test-helper-only correction is
published as `142f6cf58f2beaaf218eb5ff67cb4bc53279ab3b`, tree
`acfe22ed2335289f9be09f1c13927c5c68a183e6`, equivalent to local
`ce305f2d4d3e5f36a5b10d8c6fb781581c7fb67b`. Test launch snapshots retain exact
candidate identity and source/test hashes; production bytes did not change
between the corrected invocation and that test-helper correction.

The complete numeric payload and corrected tamper fixture are published at
`4470a4fbe8fe104b114310cb31d7fd916d77b458`, tree
`6c3faf0c9b1839ab0bf6faa7c06999760007637b`, equivalent to local
`63b89baa605605dc82a1bf504cfb2f464f2ed106`. The source-admission owners stayed
unchanged. This is the frozen compiler/test checkpoint for final focused
execution and the regenerated documented output.

## Focused verification

The required selector is:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.backend.js-checked-mir-test \
  --exact scalar-module-target-execution \
  --progress
```

The remaining selectors in that namespace are
`scalar-module-runtime-rejections`, `scalar-module-boolean-execution`,
`scalar-module-composed-qst-and-shadowed-call`,
`scalar-module-rejects-source-and-target-mismatch`, and
`scalar-module-rejects-genuine-product-tampering`.

They compile ordinary source, execute actual Node modules with runtime inputs,
and compare supported results and numeric errors with the corresponding actual
C11 reference execution. Genuine-product tampering covers source, policy, Core,
MIR, compiler owners, verification, signatures, exports, guards, safety,
effects, ownership, proof and provenance. Separate direct owner tests begin
with the same genuine product and exercise C14/B1/B6 rejections without relying
on the outer handoff to short-circuit them. Forged and expired handoffs and
substituted reports are also rejected.

The actual runtime used for this increment is Node `v20.20.2`; Clojure is
`1.12.0`. The official Node archive was verified against its SHA-256 manifest.
The Node 24 executable initially on the environment's default PATH is not used
as Node 20 acceptance evidence.

All six focused JavaScript selectors passed on the final numeric checkpoint:
6 tests, 924 assertions, no failures or errors. The actual Node results include
renamed three-function `.qst` composition (`20`, `10`), a source-defined `+`
(`7`, `1`, maximum I64), Boolean results and the exact I64 literal above Number's
safe-integer range. These are target executions with corresponding genuine C11
reference/replay checks.

Completed checks are:

| Check | Terminal result |
| --- | --- |
| Exact production source-to-module command | Exit 0; all six files emitted, observed Node `v20.20.2`. |
| Required `scalar-module-target-execution` selector at final numeric checkpoint | 1 test, 426 assertions, no failures or errors; six Node results, six corresponding C11 reference/replay checks, artifact/map checks and standalone execution. |
| Remaining runtime/Boolean/composition selectors | 3 tests, 277 assertions, no failures or errors; complete numeric error payload, twelve runtime rejection cases, `.qst`, shadowed calls, exact literals and Unicode source positions. |
| JavaScript source/target and genuine-product rejection selectors | 2 tests, 221 assertions, no failures or errors; twelve source/target refusals, fourteen direct target-owner negatives and genuine application/authority/verifier tampering. |
| Manual runtime calls on the documented output | Required three values, five additional/boundary values and twelve exact numeric/ABI rejections passed; private helper absent. |
| Manual copy outside checkout | Required stdout and success/error records matched the original under a cleared environment. |
| Source application positive/policy/authority group | 5 tests, 280 assertions, no failures or errors; includes retained JVM execution and genuine JS source target. |
| Original source/MIR invalid and tamper group | 3 tests, 209 assertions, no failures or errors; six source/binding substitutions, ten invalid programs and genuine Core/MIR tampering. |
| Direct module-analysis regression | 10 tests, 143 assertions, no failures or errors. |
| Bootstrap module/profile compatibility | 11 tests, 81 assertions, no failures or errors. |
| Existing JavaScript backend namespace | 10 tests, 315 assertions, no failures or errors. |
| Language boundary | 6 tests, 11 assertions, no failures or errors. |
| Documentation structure | 240 documents and 19 phase indexes passed. |
| Full-language roadmap structure | Passed; its coverage snapshot is non-authoritative. |
| Changed-file lint | 14 Clojure paths, no blocking warnings or errors; 3 unchanged baseline warnings. |
| Whitespace validation | `git diff --check` passed. |

The final three-selector runtime group completed in 1502.69 seconds; the
two-selector rejection group completed in 1235.79 seconds. Both exited 0.
The required selector completed in 390.20 seconds and exited 0. These durations
describe this recorded environment and are not performance claims.

The final numeric compiler checkpoint's
[GitHub CI run 43778464329](https://github.com/burn2delete/uniform/actions/runs/43778464329)
completed with 11 successful jobs, two skipped jobs and one failed `macro` job.
Its `macro-pipeline-cohort` command reached the existing 900-second watchdog
and ended `:timeout`, exit 124. The following `macro-receipt-controls` and
`macro-bootstrap-compatibility` commands both passed. The cause of the cohort
timeout is not established; it is an unresolved hosted-CI result, not a passing
check or an attributed baseline failure. No CI configuration or timeout was
changed. Local focused acceptance is complete; hosted CI and normal integration
review remain separate from these development results.

Required repository gates:

```sh
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
git diff --check
```

## Next integration handoff

Connect ordinary public compile-command dispatch to this production API and
its canonical target/runtime options. Carry the existing diagnostics, six-file
atomic publication, explicit source policy and fresh construction authority
through that dispatch. Then run the same positive, negative, standalone and
provenance acceptance from a clean checkout through the public command, with
the supported toolchain established independently.

This increment's API and Node evidence do not by themselves complete that
public CLI or clean-checkout boundary. Broader language support, other backends,
self-hosting, seed retirement and release conformance remain separate work.
