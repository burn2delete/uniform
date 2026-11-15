# Compositional scalar source to verified MIR

## Scope and finish line

This increment extends the production
`gravity.compiler.application/current-source-to-verified-mir` entrypoint to
ordinary hosted, safe JVM modules containing first-order, nonrecursive scalar
functions, direct calls, eager computed `let` bindings, checked I64 addition,
and Boolean conditionals. The input remains an actual `.gravity` or `.qst`
source path and explicit policy. The existing Uniform C6-C10 and C11 owners
must construct and freshly verify the actual source-derived products.

Completion requires reference execution of the returned multi-function MIR,
including checked overflow before a false conditional can return another
value. Source artifacts remain genuinely `:jvm`-targeted. JavaScript admission
and emission, public target dispatch, self-hosting, seed retirement, broader
arithmetic, and release work remain separate.

Governing contracts are D1/D2/D3/D8/D9, L2/L3/L5/L6, C5-C11/C15 and SAFE9.
Host code transports current source, authenticated bindings and identities;
Uniform owns semantic checks, Core construction, MIR construction and
verification. Historical source-interface pins are not renewed by this work.

## Starting state and ownership

- Latest main observed before implementation:
  `d8a296e3d8c3befe6c5a735995fde8cac89047e5`.
- Prerequisite PR #1429 was open, no longer draft, at
  `7209382b0282ec423ff102bcc67ad4859fd5a7b0`.
- That prerequisite candidate already composes the observed main.
- This isolated successor branch is `codex/compositional-scalar-mir` and
  starts from that exact prerequisite candidate. It does not claim that
  #1429 was already on main.

During development #1429 merged as
`d6ca6c93059fe85acb53a594a8fedfa0e64362bd`. Its tree is identical to the
preserved prerequisite tree. Commit
`59e3cd07a39dca04ca91e2bd3053e5b2c117cb2b` composes that main into this
successor without changing candidate source bytes. Draft PR
[#1439](https://github.com/burn2delete/uniform/pull/1439) now targets main.
This successor has not been merged.

Connected changed-file checks found no overlapping owned implementation
paths in #1419, #916, #911, #1369, #1372, #1197, #1207, #1420 or #1438.
The six active workstream records also have no intersecting owned paths.
#1429 is the explicit prerequisite. #1420 owns a SAFE9 documentation change;
this increment reads that governing contract without editing it.

## Reproduced first rejection

The following exact ordinary source was written to
`/workspace/scratch/f97e4bae66e6/evidence/scalar-functions.gravity`:

```clojure
(ns acceptance.scalar
  (:profile :hosted)
  (:target :jvm)
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

On base `7209382b0282ec423ff102bcc67ad4859fd5a7b0`, the production API
was invoked with that absolute path and this policy:

```clojure
{:profile :hosted :target :jvm :effects #{:error/raise}
 :capabilities #{} :safety :safe}
```

Command, run from the untouched baseline checkout:

```sh
clojure -J-Xmx2g -M /workspace/scratch/f97e4bae66e6/evidence/probe-scalar.clj
```

The probe invokes only the production API and catches its owned diagnostic
for recording. It returned `L3-UNKNOWN-ALIAS`, message
`stage2 executable requires a main function`, with `:entrypoint main` and
the exact input path in `:source-span`. Its remediation was
`Add (defn main [] ...).` This identifies the executable-only source-plan
handoff as the first observed blocker, before compositional Core and MIR.
The probe process exited 0 because it recorded the exception; this is a
compiler rejection, not an accepted compilation.

Evidence log: `evidence/baseline-scalar.log` in the development workspace.

## Verification checkpoints

The required original application selector completed on untouched base
`7209382b0282ec423ff102bcc67ad4859fd5a7b0`:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact current-source-to-verified-mir --progress
```

Result: **1 test, 234 assertions, 0 failures, 0 errors, exit 0**.
The representative source above has content identity
`sha256:f50c480bef4c286933c1b9b08281f757072c9f6935a5edc38c75bf640b2d0172`.

Preserved candidate checkpoints:

| Checkpoint | Published commit | Exact tree |
| --- | --- | --- |
| Initial implementation | `ff1c9a885d60af6f809180c0d965b86e8ff6f270` | `d117ca3d1e6e0d169cc92d36cbc0bcc60d6b8ae0` |
| Scalar authority, C5 provenance and proof facts | `538fc10049b1d6a087a03cd9eb37819dfba84aad` | `4e312e4792d2646ffd4f12b788284514425d9094` |
| Composition with merged prerequisite | `59e3cd07a39dca04ca91e2bd3053e5b2c117cb2b` | `4e312e4792d2646ffd4f12b788284514425d9094` |
| Actual C5 call bindings and precise owned diagnostics | `6f4b6fdde6847ecf946a16b07edf252248868917` | `a627a38c64350f6fbd3727d3d14d83044abbdc01` |
| Corrected public scope metadata | `88b68430daf8b785d7a7dd47741b014966bcb2cb` | `55c1b7599b8dae2d7e0d2397b40054acbec495be` |
| Stack-stable effect fold and physical rejection spans | `4103a4978be5fd3776f2514e9e1670d9a45775b4` | `c729ad775e991d85a20d57609fee1fdb45a06d72` |

The first implementation probe reached an accepted Uniform multi-function
Core template, then rejected at host `C6-VERIFY` with
`:exact-gravity-builder-result-envelope`. The transport recognized only the
prior family/authority combinations. The next checkpoint added the exact
new scalar family/authority carrier, while retaining digest-graph validation,
fresh parent reconstruction and actual Uniform verifier execution. That
first probe overlapped an owner edit and is only rejection evidence; it is
not promoted as an exact-source acceptance result.

The second checkpoint was tested from local source commit
`6f13b5160e9e90d1c5785a4fd24b52ad0c91c02e`, whose tree exactly equals
published `538fc10049b1d6a087a03cd9eb37819dfba84aad`. The subsequent main
composition has that same tree. Its selected owner identities were:

| Uniform owner | Source content identity |
| --- | --- |
| C6-C10 | `sha256:abccc67d102359b5e109a0588edb2db037e3e84162e41c86e979ff024de88a1e` |
| C11 | `sha256:a803d6b086ea38921998ac37f7fd325a1625c026cd9fc3fd900b945f4a9efcf9` |

Both current owner bindings compiled successfully. The language-boundary
gate passed with **6 tests, 11 assertions, 0 failures, 0 errors**. Four
affected SH13 interpreter selectors passed with **58 assertions, 0 failures,
0 errors** at the initial implementation. These interpreter checks are
separate from source-bridge acceptance.

The recurring production selector is:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.compiler.application-test \
  --exact scalar-functions-and-control-flow --progress
```

Its first complete run on the second checkpoint produced **1 test, 132
assertions, 2 failures, 0 errors**. Both failures were test metadata predicates
that incorrectly required C2 form IDs such as `:form-23` to be SHA256 IDs.
The source construction, fresh Core/MIR verification, results `10`, `7` and
`18014398509481987`, eager false-branch overflow, fresh execution verification,
and private-export rejection assertions completed without failures. The
corrected assertion compares each C2 form ID with the actual Core source
origin and retains SHA256 checks for C5 syntax and scope IDs. This failed
test run is not reported as a passing selector.

That first test used an equivalent one-line `if` source, with identity
`sha256:78fecba9f93bededb6ff1a850e86ce160bb5504f7d0b72ec86a41fd88aa58252`.
The subsequent acceptance source uses the exact multiline representative
above. The first run's returned Core identity was
`sha256:92e9f6b4654d2f66238296f39a060b4672b8a94e49ab0dfdf6d387fdcc51a8d8`,
and the application artifact was
`sha256:9c98237ef91ef3bf35345abe7bf77456dfcb9965b6f7b26c35aed7ccbc97f9fe`.

A real follow-up source declared a private function named `+` that returns
its first I64 argument. C5 resolved the caller's operator to that namespace
function, while the seed plan classified the spelling as a builtin. The
production API rejected with `C6-VERIFY`,
`:missing-fact :resolved-core-i64-add-primitive`, at the actual operator
span, line 11 columns 41-42. Its expected binding was core `gravity.core/+`;
its actual binding was the private namespace function `acceptance.shadow/+`.
This identifies a concrete direct-call classification gap. The correction
uses actual C5 operator bindings for both dependency discovery and call
classification, retaining checked addition only for the true core binding.
It does not change the historical seed's global precedence.

The original rejection and source/MIR regression batches continue against
the frozen second-checkpoint checkout. Subsequent edits use an isolated
successor checkout so those tests cannot observe changing owner bytes.
Pending or interrupted runs are not counted as passing evidence.

### Confirmed representative acceptance

The exact selector above completed successfully on local execution commit
`fbb6cdd642a801a77deddd73ddf67c6acb210fbb`, whose tree exactly equals
published `6f4b6fdde6847ecf946a16b07edf252248868917`:
**1 test, 134 assertions, 0 failures, 0 errors, exit 0**.
Elapsed time including namespace loading was 518.21 seconds. The source was
the exact multiline representative with the `f50c480b...` identity above.
All four required runtime outcomes, original function identities/exports,
fresh Core/MIR verification and fresh execution verification passed.

| Exported call | Observed and asserted outcome |
| --- | --- |
| `score(true, 3, 7)` | I64 `10` |
| `score(false, 7, 3)` | I64 `7` |
| `score(true, 9007199254740993, 9007199254740994)` | I64 `18014398509481987` |
| `score(false, 9223372036854775807, 1)` | `:error/numeric`, kind `:overflow`, before a conditional is selected |

The returned Core identity was
`sha256:440f6785a81b865efae1316ab561d6d4a1a8d2db1e2259a057e0b610fcb2dcc8`;
the application artifact identity was
`sha256:75b427bfe0152384a2545122dfcf9de2dd26ddc3fd22cf8cce7753f2f8350124`.
Its C5 resolution identity was
`sha256:acdf51ef7062e53baf1eb50e12ad683d7bfc66ec01d41c8726e3e8f5f8a2cd99`.
The selected Uniform owner identities, printed by the passing test, were:

| Identity | C6-C10 | C11 |
| --- | --- | --- |
| Source | `sha256:59ee9f6fcaa54e14ca4a55f45aeebda6f289abaf62ecb9f6847261fb90f545e7` | `sha256:85027320c016eaacafe367f1278dde5fdbba0294feace8990a3ad4c6d0a58f97` |
| Plan | `sha256:55c1289be28e2abb3784c3f615bbf4babad90dd50f08a1fd3ebe6bc0988735c4` | `sha256:c077496fbaae7973ceab1cff848547e627f4285702804023b95f7a32fbf65b11` |
| Functions | `sha256:9f63f35ceef7abe216e03962504f2e4d988ece30c906e36d69b8666dbba2a08a` | `sha256:aea5134a8d5350bb24acc438e7f19560efe08ecd8463b5bbb80da9e96eb04b3e` |
| Builder | `sha256:b69281ddcbf8898784b9a0c6dc75b86c1f4987a0ec81d7f27cd18e53d8125b90` | `sha256:fd5fa6edb5a0af19cfd8808f4085d48ff2c03ac170ea2ef7a61124eef41b10a7` |
| Verifier | `sha256:12a8ca5c524e17507d545f946648016532b896bf7af14052cd36492a9516bc26` | `sha256:e96cf156bc51998a285e649a02d6ab745f7d73ecea391eee8b4587e168e2e5fd` |

The next metadata checkpoint removes an obsolete
`:compositional-multi-function` nonclaim from the application envelope and
links the prerequisite API note to this current scope. It leaves these C6-C10
and C11 source bytes unchanged. It has local counterpart
`c15dd60f11c26164a38c99cf4284cca15206e16d`, with the exact published tree
shown above. Subsequent application cases also assert that the stale nonclaim
is absent. The representative artifact identity above belongs to its recorded
execution commit; changing the public envelope changes its artifact digest.

### Negative and interrupted checkpoint outcomes

The first semantic/policy batch on the frozen second checkpoint ended with
**2 tests, 46 assertions, 2 failures, 0 errors, exit 1**. All ten semantic
cases completed without failures: unknown callee, arity, argument type,
unused invalid arithmetic, incompatible branch results, non-Bool condition,
inherited undeclared numeric effect, lexical reference, direct recursion and
mutual recursion. The source-effect-policy mismatch also passed.

Both failures were expectations for a source declaring `:js` with a JVM
request. The existing module owner rejects that source earlier with
`B1-TARGET-UNSUPPORTED`, `:bootstrap-stage :stage0`, `:target :js`,
`:supported #{:jvm}` and the actual source path. The test incorrectly expected
a later `C6-ORIGIN` policy mismatch. The correction retains the real earlier
rejection and asserts its target facts; no production admission is broadened.
The corrected policy selector is rerun before the obsolete scope rejection
is removed.

The first corrected-policy attempt on local
`13876799b8df45b4a6984f52a454bc1e71a63634` (published
`f3c98391caf5a59efddac8c7c10c2e508665df3f`, tree
`1d65ea985adbb143933f1373f841dfafd8832711`) failed namespace loading with
exit 2 because the test edit had one extra closing delimiter. No test ran.
The delimiter was removed, and the full Clojure reader consumed all 39 test
file forms before the fresh retry. Compiler source bytes were unchanged.

Three newly started batches were stopped with exit 130 to reduce overlapping
JVM memory pressure, before any selected test completed. An earlier eight-test
source/MIR batch was externally killed with exit 137: its two current-reader
and replay selectors had returned without failures, but its third test had
not returned and no batch summary existed. These are interrupted attempts,
not passing batches. The six unfinished source/MIR selectors are rerun in a
fresh JVM against the unchanged final Uniform owner bytes.

The corrected policy retry completed on local `d132c58daea3038e6c621e10592d02fbcc4798cd`
(published `d8985233ee98e09f0aefe62afeb7716e2b96e621`) with
**1 test, 8 assertions, 0 failures, 0 errors, exit 0**. After that result and
the representative/semantic rejection results above, the obsolete
multiple-function rejection row was removed. Subtraction and multiplication
remain in that scope selector. The scope-only change is published as
`0ce42bfbc850f0d9e9f52094c4ee6629289948a9`, tree
`f40c091ba862fdaebe91be8bf196c2b05e53a070`, with local counterpart
`6681c275a0d0390e648e9631caedc26f1207f92f`.

The six remaining original checked-add/source-MIR regression selectors
completed on `88b68430daf8b785d7a7dd47741b014966bcb2cb` with
**6 tests, 198 assertions, 0 failures, 0 errors, exit 0**. The two reader/replay
selectors from the interrupted batch subsequently completed on clean
`4103a4978be5fd3776f2514e9e1670d9a45775b4`, tree
`c729ad775e991d85a20d57609fee1fdb45a06d72`, with **2 tests, 25 assertions,
0 failures, 0 errors, exit 0**. The source and test bytes were rechecked
unchanged after the 961.19-second run.

The full handoff/product tamper batch on the passing representative candidate
ended with **2 tests, 161 assertions, 16 failures, 0 errors, exit 1**.
All four production handoff mutations rejected correctly: altered Core type
fact, Core verifier report, MIR guard table and MIR verifier report. Every
one of the sixteen detailed product mutations also rejected with the expected
rule and function attribution, and none executed. The concrete call-signature
comparison survived the host diagnostic conversion, and a substituted
execution result rejected.

All sixteen failures concerned the raw C11 diagnostic's source-span file:
it still held the path-neutral source-content identity copied from Core,
while `:provenance :actual-source-path` correctly held the real path. The
Uniform rejection formatter now overlays that actual path into the span's
`:file` and `:source` fields when present, preserving the original coordinates
and semantic origin IDs. This changes rejection presentation only; Core,
MIR construction and verification decisions remain unchanged. The current
C11 owner compiles with source identity
`sha256:3d64a005277eec4ed0d63fbf2c863dd195578c8aba90be58c7acdc8105fe3af7`.
The actual-path assertions remain intact for the fresh product-tamper rerun.

The renamed `.qst`/shadowed-operator batch on `0ce42bfbc850f0d9e9f52094c4ee6629289948a9`
ended with **2 tests, 110 assertions, 0 failures, 1 error**. The namespace
function named `+` executed correctly, retained its unused parameter, and
had no primitive-add node, overflow guard or numeric effect. The three-function
nested `.qst` source instead rejected in the real C6 builder with
`C6-VERIFY`, `:missing-fact :contained-gravity-source-host-stack`, boundary
`:compiler-application-builder`, and `java.lang.StackOverflowError`.

A small data-only invocation of the actual compiled C6
`c6c10-expression-effects` helper returned the correct union for 26, 38 and
128 effect rows but exhausted the host stack at 512. That newly added fold
used ordinary self-recursion over every accumulated node while expression
mapping frames remained live. Its equivalent `loop`/`recur` implementation
returns the same union for all four inputs, including 512, and the current
owner compiles with source identity
`sha256:fa764b54ec4237f315b0141720d2b13e01c103c4008ee0c8a60bd4b14263c924`.
No compiler quota or JVM stack-limit adjustment was introduced. This helper
probe does not supply accepted Core/MIR and is not compositional acceptance;
the ordinary `.qst` source is rerun through the production API to determine
whether its actual construction failure is resolved.

That ordinary-source retry on local execution commit
`30dfdc855faeb349ebcb497aae694f99550565a0`, whose tree equals published
`4103a4978be5fd3776f2514e9e1670d9a45775b4`, ended with **1 test, 1 assertion,
0 failures, 1 error, exit 1**. The actual remaining failure was still
`C6-VERIFY` / `:contained-gravity-source-host-stack` at
`:compiler-application-builder`. The effect-fold change alone did not resolve
the composed program. A diagnostic-only run of the same ordinary source
records active Uniform function calls at the original overflow and rethrows
the failure unchanged. It preserves the genuine builder input for causal
replay; such instrumented replay is not production acceptance evidence.

The complete ten-case semantic-negative selector also completed on the
scope-checkpoint source/test tree with **1 test, 40 assertions, 0 failures,
0 errors, exit 0**. Its launch HEAD was `d8985233ee98e09f0aefe62afeb7716e2b96e621`
with only the unselected scope-test row removal in the working tree; that
exact tree was committed as local `6681c275a0d0390e648e9631caedc26f1207f92f`
and published as `0ce42bfbc850f0d9e9f52094c4ee6629289948a9`. The selected
negative body and all compiler bytes stayed unchanged throughout the run.

The source/binding-substitution and supplied-authority selectors completed
from clean local `6681c275a0d0390e648e9631caedc26f1207f92f` (published
`0ce42bfbc850f0d9e9f52094c4ee6629289948a9`) with **2 tests, 58 assertions,
0 failures, 0 errors, exit 0**. Both `.gravity` and `.qst` source envelopes,
binding pins and post-envelope file bytes rejected when substituted. The
production API also rejected supplied Core/MIR, verifier reports, bindings,
authority tokens and semantic fact maps. These cases retain actual source
attribution. Where a test mutates a product, the original owner runs first.

The final changed-Clojure lint pass checked all 13 modified Clojure paths
against main `d6ca6c93059fe85acb53a594a8fedfa0e64362bd`. It exited 0 with
**0 blocking warnings or errors** and four unchanged baseline warnings.
The lint configuration was unchanged and the existing macro contract was
admitted. Its complete fresh output is retained in the development workspace
under `evidence/lint-run-final`; the log is `evidence/changed-lint-final.log`.

## Construction and execution ownership

The production API accepts only a source path and the five explicit policy
keys. It reconstructs the actual Reader, Syntax, expansion and C5 products,
transports every declaration and parameter, and binds the complete resolution
projection into the authenticated C6 envelope. An unused parameter is retained
through its C5 declaration identity. Module exports come from the real source;
there is no synthesized `main` entrypoint.

The Uniform C6-C10 owner checks the complete nonrecursive function graph,
including unused functions. It builds parameter and local types, fixed-arity
call signatures, scalar return types, branch compatibility, latent effects,
numeric-error facts and persistent immutable ownership. The scalar scope
contract has no function, form, depth or work quota. Original C3 syntax
identity and expanded C5 function/scope identities remain distinct.

The Uniform C11 owner builds per-function MIR with direct call operations,
explicit blocks, branches and joins, source maps, type/effect/ownership/safety
facts, proof references, definitions and uses. Checked addition has an
original overflow guard and consumes that guard's token. The verifier
reconstructs expected MIR from checked Core and compares the full result,
in addition to checking signatures, dominance and preserved fact tables.

Reference execution calls `sh13-run-scalar-functions-mir` on the unchanged
`checked-core` and `mir-module` returned by the API, plus the actual exported
name and argument vector. The existing SH13 interpreter executes private
frames projected from that verified MIR. These frames do not acquire a new
source target or become replacement input artifacts. The companion
`sh13-verify-scalar-functions-execution` recomputes the execution result.

The eager-overflow case must raise `:error/numeric` with kind `:overflow`
inside the helper before any caller conditional is selected. The existing
reference convention represents this with outer status `:rejected` and inner
execution status `:raised`; a malformed MIR rejection instead has no
execution. Tests distinguish these outcomes.

The host retains its bootstrap transport, current-source selection, identity
computation and composition roles. Semantic checking and construction stay
in Uniform. The source-policy receipt records
`:admission-status :development-unadmitted`,
`:verification-scope :selected-current-source-host-seed` and
`:self-hosted? false`. The expression receipt accurately records that the
module still uses the Clojure seed plan assembler.

## Verification matrix and remaining target boundary

`gravity.compiler.application-test` contains focused selectors for the exact
representative source, renamed `.qst` with three functions and nested calls,
shadowed operator binding, both legacy single-function families, invalid
calls/types/branches/effects/recursion, source-policy mismatch, supplied
authority, genuine-product tampering and source/binding substitution.
The old multiple-function scope rejection is removed only after the corrected
representative selector and new semantic/policy negatives pass. Subtraction,
multiplication and JavaScript policy remain outside this increment.

Affected source/MIR regression selectors cover original parameter
production, fresh resolution replay, checked-add facts, undeclared numeric
effects, authentic source and mode mismatches, original MIR bridging and
upstream fact/check substitution. The source-inspection regression that
previously assumed the old C6 dispatcher was its outermost `if` now checks
the scalar dispatch and then inspects the unchanged legacy export gate.
Its corrected focused run passed **1 test, 16 assertions, 0 failures,
0 errors**; the earlier run's 2 failures and 6 errors remain failed evidence.

Final evidence records terminal results for these selectors, the language
boundary, documentation validators and `git diff --check`. No pending result
establishes completion. The next separate compiler increment is JavaScript
source-target admission and emission; this MIR milestone does not produce a
JavaScript module or establish completed target compilation.

The next concrete failed handoff was reproduced on candidate
`6f4b6fdde6847ecf946a16b07edf252248868917` by changing only the representative
source target to `:js` and making the explicit request policy also use `:js`.
That source identity is
`sha256:a4f939c1ca8a638c5313c07e77c6ef8a6f8bbe0fcb956241c11b2d23904cd8c3`.
The real API rejects at `p15-s23-application-request!` with `C11-MODULE` and
`:missing-fact :application-explicit-hosted-safe-jvm-policy`, attributed to
the actual `scalar-functions-js.gravity` path. The diagnostic's generic
message is `MIR module record is malformed`; the missing-fact field identifies
the precise policy handoff. The recording probe exits 0 after catching that
exception, which is compiler rejection evidence. This is earlier than any
JavaScript emission attempt; no JavaScript output was produced.
