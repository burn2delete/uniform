# Checked scalar JavaScript modules

## Supported production route

The public compiler connects ordinary hosted/safe `:js-ts` Uniform source to
freshly checked Core, verified compositional scalar MIR, B1/C14 target
eligibility, and executable Node 20 / ES2022 ESM modules. Uniform source
owners retain semantic admission, target legality and lowering rules. The
Clojure seed remains the compiler host and transports and publishes artifacts.

The supported language subset is I64 and Bool, checked I64 addition, eager
local bindings, Boolean branches, fixed-arity nonrecursive direct calls,
declared numeric-error effects, actual exports and private helpers. Runtime
I64 arguments and results use JavaScript BigInt with signed 64-bit checks.

Use `bin/gravity compile <source> --target js -o <fresh-directory>`, or the
explicit current-source launcher `clojure -J-Xmx2g -M:gravity compile` with
the same arguments. The CLI derives source policy from the actual source during
the first authoritative Reader/Syntax and module ingress, then calls
`gravity.compiler.application/current-source-to-js-module`. Users supply no
Clojure API expression, policy map, intermediate IR or compilation harness.
The constructor replays those inputs before verification; policy extraction
alone does not accept the program. The producer accepts no supplied Core, MIR,
verifier report, construction token or target eligibility certificate.

## Compile and run the example

The recorded toolchain for this example is OpenJDK 21.0.13, Clojure CLI
1.12.0.1530, Clojure 1.12.0 and Node 20.20.2. Put Node 20 on `PATH` for both
compilation and execution. From the repository root, prepare the compiler
dependencies with `clojure -P -M:gravity`. The example declares its profile,
target, effects and exports in [acceptance.gravity](../examples/checked-js/acceptance.gravity).

```bash
bin/gravity compile examples/checked-js/acceptance.gravity \
  --target js -o target/acceptance-js

clojure -J-Xmx2g -M:gravity compile \
  examples/checked-js/acceptance.gravity \
  --target js-ts -o target/acceptance-js-explicit
```

Each `-o` argument is a required fresh output directory, not an executable
filename. Existing destinations and paths outside the output policy are
rejected without overwriting an artifact. Successful compilation reports the
importable module and actual artifact paths. Compilation checks the actual
Node major version and generated ESM syntax; it never invokes `score` to
discover an answer. Use a new relative path under `target/` for ordinary work.
The public command also permits a single relative directory name. Absolute
output paths and paths containing a `..` segment are rejected.

The compiler obtains profile, target, effects, capabilities and safety from
the actual source. Arithmetic modules must declare the numeric-error effect;
Boolean-only modules may use `(:effects #{})`. No effects or capabilities are
added to make a source pass. The canonical source target is `:js-ts`; both CLI
spellings `js` and `js-ts` select it. Provenance preserves the requested spelling
alongside the canonical target. The alias does not rewrite a source declaration
of `:jvm`. The source, checked Core, MIR, runtime-check policy, target request
and manifest remain `:js-ts`; the compiler host remains the JVM seed.

### Lowering selection and diagnostics

| Request | Route |
| --- | --- |
| `compile source --target js -o directory` | Checked source-to-JavaScript module producer. |
| `compile source --target js-ts -o directory` | Same checked producer with canonical requested target. |
| `compile source --target js --lowering runtime-derived -o directory` | Existing legacy JavaScript route, with its own actual results or diagnostics. |
| Another explicit lowering value for JavaScript | Structured refusal, with no silent fallback. |
| Another target or no target | Existing target/default behavior. |

The checked producer does not fall back to legacy packets, seed execution or
precomputed stdout when checking or emission fails. Source errors retain their
owning diagnostic and useful source attribution, return a nonzero process exit
and publish no new target directory. CLI option errors report command facts
without fabricated source spans. An unrelated stale migration pin is not
evidence that a malformed program was correctly rejected. The disabled native-C
verified-MIR command remains a separate surface.

### Execute the exported functions

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

Expected stdout is:

```text
10
7
18014398509481987
```

Additional calls use this same module, without recompilation. Standalone
acceptance copies the entire output directory outside the checkout, changes
the Node working directory there and imports `./program.mjs`.
The generated module has no imports or runtime reference to Clojure or the
repository; the source map embeds the original source text.

## Construction authority and lowering

Compiler callers can exercise the producer directly with a source path,
source policy, and emission options. Use a fresh directory for the output.

```clojure
(require '[gravity.compiler.application :as compiler])
(compiler/current-source-to-js-module
  "examples/checked-js/acceptance.gravity"
  {:profile :hosted :target :js-ts :effects #{:error/raise}
   :capabilities #{} :safety :safe}
  {:output-path "target/acceptance-js-api" :target :js-ts
   :runtime :node20 :language :es2022 :module-format :esm})
```

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

## Verification

Choose the command that reaches the behavior you changed. Run from the
repository root with Node 20 on `PATH` for tests that execute the target.

| Change | Focused command |
| --- | --- |
| Checked addition and its original overflow guard in B6 lowering | `clojure -J-Xmx2g -M:dev-test --namespace gravity.backend.js-checked-mir-test --exact checked-add-requires-original-guard` |
| Public CLI dispatch through module execution | `clojure -J-Xmx2g -M:dev-test --namespace gravity.compiler.public-js-smoke-test --exact public-js-compiler-smoke` |
| Backend artifact, reference execution and standalone module guarantees | `clojure -J-Xmx2g -M:dev-test --namespace gravity.backend.js-checked-mir-test --exact scalar-module-target-execution` |
| Full public command acceptance, including alias, diagnostic and publication behavior | `clojure -J-Xmx2g -M:dev-test --namespace gravity.compiler.application-test --exact public-js-compiler-acceptance` |

When changing the wrapper help, regenerate its two resources from the Clojure
CLI presentation and run `checked-in-shell-help-is-rendered-by-the-cli-presentation`
in `gravity.cli-test`.

```bash
clojure -M -e '(require (quote gravity.cli)) (doseq [[path launcher] [["bootstrap/clojure/resources/gravity/cli/help-source.txt" :source] ["bootstrap/clojure/resources/gravity/cli/help-archived-packaged-jvm.txt" :archived-packaged-jvm-cli]]] (spit path (gravity.cli/p18-cli-help-resource-text launcher)))'
```

For historical results, see the [producer acceptance record](compiler-checked-js-modules-receipt.edn)
and the [public CLI acceptance record](compiler-public-js-cli-receipt.edn).
Both describe the revisions they tested. The [development workflow](development-verification.md)
describes how to verify a change in the current checkout.
