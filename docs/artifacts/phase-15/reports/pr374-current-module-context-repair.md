# PR374 current ordinary-module context repair

## Frozen checkpoint

This working repair starts from branch `codex/review-374-preview-main5ecc` at
candidate commit `42ed8803389116f56e7aea73f932799ef6d1de61`, tree
`6416564d1bb28fe6fd5a41bde5df058cd2c94bca`. The historical genuine run and
its failure remain immutable; this report does not replace that evidence.

The historical run is recorded in
`/tmp/uniform-pr374-preview-current-contextual.log`. C11/C5, C13/C5, and
C14/C5 completed before module construction failed with `BOOT2006`
`:verified-ordinary-c5-required` in
`00580-p15-s23-stage2-module-source.clj` at the one-argument verifier call.

## Diagnosis

The module source consumer used
`sh06-resolution-artifact-verification` with its no-context, pinned-source
arity. PR374's SH17 producer creates a genuine current-source C5 artifact with
an explicit Reader/Syntax/Macro invocation. Those are distinct source
contracts: the artifact is accepted, but the pinned verifier correctly reports
the current-source request as stale. No artifact status, hash, or verification
result is rewritten here.

A bounded direct-source C5 reproduction used the ordinary
`sh-12/branch-join/accepted/zero_arity_bool_true_source.gravity` fixture and
produced artifact `sha256:457a09a6e91f27394e1943b0337dbf4529d6e07a3af93ae6e7864b5bc66de49b`.
For that actual artifact:

* the default verifier returned `:failed` with stale/default request checks;
* `sh06-resolution-artifact-verification artifact
  :gravity.bootstrap/legacy invocation` returned `:passed` with no failed
  checks;
* the artifact remained an ordinary accepted C5 artifact, not a serialized
  or synthetic passed label.

## Repair

The optional explicit current Reader invocation is threaded through the
ordinary module construction and validation path:

* `p15-s23-stage2-module-source` keeps its one-argument pinned/default behavior
  and adds an explicit-invocation arity that performs the authenticated SH06
  verification before reconstructing C2 source text;
* module-bundle construction and bundle reconstruction forward that invocation
  to every closure artifact;
* compiler-artifact plan construction and runtime module validation/execution
  expose the same optional invocation;
* the SH17 boundary producer passes the authenticated current invocation to
  C5 source construction, module-plan construction, and runtime execution.

The invocation is never inferred from artifact metadata and is not stored as
an admission fact in the module bundle. Existing no-context callers remain on
the pinned verifier and therefore continue to fail closed for a current-source
artifact.

## Bounded checks

Passed on the dirty working tree:

```text
clojure -M -e "(require '[gravity.bootstrap :as b]) ..."
  production bootstrap namespace loaded

clojure -M:dev-test --namespace gravity.self-hosting.sh17-literal-bool-input-boundary-test \
  --exact contextual-diagnostic-helper-preserves-canonical-fields --fail-fast
  1 test, 8 assertions, 0 failures, 0 errors

clojure -M:dev-test --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-closure-development-kernel \
  --exact stage2-module-transitive-routing-development-kernel \
  --exact stage2-module-instruction-traversal-development-kernel --fail-fast
  3 tests, 28 assertions, 0 failures, 0 errors

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
  6 tests, 11 assertions, 0 failures, 0 errors

git diff --check
  passed
```

An attempted second direct module fixture was stopped after approximately
five minutes while unrelated JVM test workers were active; it produced no
result and is not counted as evidence. The historical direct C5 reproduction
above is the bounded genuine artifact evidence for the verifier diagnosis.

## Nonclaims and next gate

This repair has not rerun the multi-hour genuine C11/C13/C14 contextual
construction, and it does not claim C5/C14 completion, admission, self-hosting,
release readiness, or canonical source migration. The coordinator must compose
the latest main, run the ordinary module callee/caller acceptance path with the
explicit invocation, and independently review the resulting immutable
candidate before any integration decision.
