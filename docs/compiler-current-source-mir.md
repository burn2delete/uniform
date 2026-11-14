# Current source to verified scalar MIR

This increment adds the production entrypoint
`gravity.compiler.application/current-source-to-verified-mir`. Its inputs are
an actual `.gravity` or `.qst` source-file path and an explicit source policy:

```clojure
(require '[gravity.compiler.application :as compiler])

(compiler/current-source-to-verified-mir
  "/path/to/add.gravity"
  {:profile :hosted :target :jvm :effects #{:error/raise}
   :capabilities #{} :safety :safe})
```

The policy must match the source declaration. The caller cannot supply Core,
MIR, facts, verifier reports, plans, bindings, or authority tokens. Rejections
throw owned diagnostics with source attribution.

## Construction and scope

The application snapshots the actual source bytes, reconstructs current
Reader, Syntax, expansion and C5 parents, and invokes the existing Uniform
C6-C10 builder. A second parent reconstruction feeds a fresh Uniform verifier.
The existing host transport mechanically resolves the owner's digest requests.
The original opaque C11 construction owner then invokes the actual Uniform
SH12 builder and verifier. Returned report data must match the selected owner's
report structure. Source and compiler owner bytes are rechecked before return.

The supported scope is existing hosted/safe checked-I64 addition and applicable
Boolean scalar families. Other arithmetic and compositional multiple functions
are outside this application increment. Historical interfaces, source pins and
the separate whole-runtime packet are unchanged.

The result includes checked Core, MIR, a projection of the actual Core verifier
report, the actual MIR verifier report, source-policy evidence, and identities
derived from the selected compiler source bytes and executable functions.
Parameter source transport retains its existing Clojure seed plan assembly;
this boundary is recorded without claiming Uniform expression assembly for
that path. The compiler host is the Clojure stage0 seed. Source artifacts remain
JVM-targeted at this intermediate boundary.

No JavaScript admission or emission, compositional multi-function support,
self-hosting, seed retirement, automation changes, or end-to-end compiler
completion is claimed. The next compiler handoff is a separately scoped target
admission and emission increment starting from verified MIR.

## Development evidence

Base: `4023d7657c9bdca8329a7860c17f6d2f0b56148e` (latest main checked before
editing). Work is isolated on `codex/current-source-verified-mir`.

The required source-backed baselines were reproduced before editing:

```sh
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh12-checked-i64-add-producer-test \
  --exact sh12-checked-i64-add-produces-original-bound-facts \
  --exact sh12-checked-i64-add-rejects-undeclared-error-effect --progress
# 2 tests, 27 assertions, 0 failures, 0 errors

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh13-mir-integer-add-bridge-test \
  --exact sh13-checked-i64-add-original-producer-bridge --progress
# 1 test, 112 assertions, 0 failures, 0 errors
```

The production namespace loads in a fresh JVM. Application acceptance,
rejection, affected source/MIR regression, and language-boundary results will
be recorded in the next checkpoint and the draft PR with the exact candidate
SHA. Baseline tests establish checked source/MIR behavior, not target emission.
