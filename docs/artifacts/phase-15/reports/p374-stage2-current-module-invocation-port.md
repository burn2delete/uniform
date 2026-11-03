# PR374 Stage2 Current-Invocation Port

## Purpose and boundary

This report records a bounded ordinary-development port from PR374: Stage2
compiler-artifact modules may receive an explicit current Reader/Syntax/Macro
invocation when their C5 products have already been selected by that caller.
The existing one-argument APIs retain the pinned/default route. This report is
not an admission, migration, self-hosting, release, or C14 result.

The full PR374 C14 increment remains open elsewhere. Its C13 exports, C14
source, C14 tests, C14 report, and pending genuine C14 evidence are not part of
this port.

## Identity and source accounting

- Frozen ordinary base: `78db2bc984bd66de920a4abaef02f759f1108605`, tree
  `9e435a4f3bbfe02a69ddc3dd71119bc381d06041`.
- Source freeze: `a7631ecd48a6ce86cba3d1b5cab2714a78b8941e`, tree
  `de4043ff7f630c4c07cdbda6026c59eae6f06802`, parent the frozen base.
- Original PR374 source checkpoint reviewed for this slice:
  `6393977e7ffa7cdf500b5802cb393e7713ea33a6`, tree
  `e40828a65868e84b43464b114943a28efd2f6a16`.
- Original PR374 branch ancestry and C14 work are retained as history only;
  no old branch was replayed and no authority fingerprint was refreshed.

The source freeze changes four existing Stage2 production fragments and one
existing Stage2 test namespace. The production changes are limited to
optional invocation forwarding through:

1. compiler-artifact plan construction;
2. module bundle source reconstruction;
3. module-source verification;
4. bundle reconstruction during validation; and
5. module runtime validation.

The explicit value is passed lexically to the selected source/verifier path.
It is not copied into the bundle boundary or used as admission metadata.

## Contract-preserving behavior

The new arities accept a caller-supplied invocation containing the actual
Reader source path, explicit current Syntax selection, authenticated envelope
options, and explicit Macro source mode. Stage2 then calls the existing SH06
artifact verifier with that invocation for every transitive module. The
ordinary one-argument source, validator, runtime, and four-argument bundle
arities continue to call the pinned/default verifier. No mode is inferred from
an artifact and no current source is silently selected for an ordinary call.

The Stage2 boundary remains `:development-unadmitted`, with
`:self-hosted? false` and effects still `:not-checked`. Existing module shape,
route, identity, declaration, arity, provenance, Unicode, metadata, callback,
and tamper checks remain in the owning helpers.

## Focused evidence

Source, test, lint, and boundary commands ran against the clean source freeze
before this report was added; the document and roadmap validators ran with
this report present and no other source changes.

```text
clojure -M:dev-test --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-fixture-parity --fail-fast
  1 test, 8 assertions, 0 failures, 0 errors

clojure -M:dev-test --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-closure-development-kernel \
  --exact stage2-module-transitive-routing-development-kernel --fail-fast
  2 tests, 26 assertions, 0 failures, 0 errors

clojure -M:dev-test --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-explicit-reader-invocation-plumbing --fail-fast
  1 test, 7 assertions, 0 failures, 0 errors

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
  6 tests, 11 assertions, 0 failures, 0 errors

PATH=/tmp/acc08-controls-01a183dd/kondo-2025.10.23:$PATH \
  clojure -M tools/ci/lint.clj 78db2bc984bd66de920a4abaef02f759f1108605 \
  <the five changed source/test paths>
  changed-file lint: 0 unchanged baseline warnings; 0 blocking warnings/errors

git diff --check 78db2bc984bd66de920a4abaef02f759f1108605 HEAD
  passed
```

An exploratory full current-source three-module C5 test was started before
the finite seam test was finalized:

```text
clojure -J-Xmx2g -M:dev-test --namespace \
  gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-current-reader-invocation --fail-fast
```

Its Java process was PID 83316. A thread sample first showed genuine Macro
registry expansion and a later sample showed `clojure.core/pr_on` while the
process was serializing a large structure. There was no progress reporter in
this invocation, no durable failure output, and no observed `:fail`, `:error`,
or `ExceptionInfo` result. SIGINT (exit 130) was sent after approximately 15
minutes when no completion or bounded result was available. This is a manual
no-result interruption, not a timeout, host-boundary diagnosis, pass, failure,
or authority evidence. No second attempt or full C14/native route was started.

The separate existing one-artifact verifier selector
`gravity.resolution.pipeline-test/verifier-requires-fresh-replay-current-products-and-public-proof`
was also run once with `--progress`:

```text
clojure -J-Xmx2g -M:dev-test --namespace gravity.resolution.pipeline-test \
  --exact verifier-requires-fresh-replay-current-products-and-public-proof \
  --progress --fail-fast
```

Its Java process was PID 27162. The durable progress stream reached namespace
load, fixture entry, test-var entry, and `:resolution-test-start`; it emitted
no `:fail`, `:error`, test-var return, or `:resolution-test-end` event before
SIGINT (exit 130). A thread sample showed `clojure.core/pr_on`, which may be
canonical artifact serialization; no failure code can be inferred from it.
The process was manually stopped after approximately six minutes because no
bounded completion/result was available. This is also a no-result observation,
not a timeout, host-boundary diagnosis, pass, failure, or authority evidence.
No retry was made.

The finite Stage2 test uses synthetic module carriers only to prove invocation
forwarding, legacy dispatch, transitive ordering, boundary preservation, and
absence of invocation storage. It does not claim that a current full-source
C5 artifact or C14 consumer has passed. Actual current-source C5 and C14
evidence remains owned by their separate tests and review records.

## Provenance, nonclaims, and integration

The explicit invocation is caller-owned development input. Source bytes,
paths, hashes, fresh verifier products, and source-mode checks remain owned by
the existing C2-C5 pipeline. This port does not create a source authority,
renew a pin, or convert a development result into migration evidence.

This candidate does not claim:

- full-source C5 acceptance or a complete Reader/Syntax release;
- C6-C14 construction, target lowering, native execution, or self-hosting;
- migration, public admission, release readiness, or seed retirement; or
- that the interrupted exploratory C5 run passed or failed.

The remote `origin/main` advanced after the frozen base during this local
review. Any later integration must normal-merge or otherwise compose against
the exact then-current `main`, rerun the affected checks, and retain this
source checkpoint as history. No GitHub write, push, merge, or PR state change
was performed here.

## Subsequent coordinator composition

Checkpoint `dc8276a8653d8092d2a84a593e22c5c49a570824`, tree
`ca72bfc057c21f767bd86e19085996dc7e46ad61`, normally composes main
`f7184c2e2e236018c600c417ab1d5f01f166ca6e`. All five source/test
inputs remain unchanged from `ac777f3`; this report was extended here with
the composition receipt.
The four finite selectors above passed together at this composition:
4 tests, 41 assertions, zero failures and zero errors. The durable log is
`/tmp/uniform-module-invocation-current-finite.log`.

This later check proves forwarding and legacy dispatch only. It does not
replace the incomplete real-source experiments or the separate pending C14
evidence. Whole PR374 remains open.
