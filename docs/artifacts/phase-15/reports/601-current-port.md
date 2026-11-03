# PR601 Current Port: Versioned Expression-Binding Metadata

## Purpose

This report records the bounded current-source port of PR601's useful
versioned expression-binding handoff.  It is a source and focused-test
checkpoint, not an admission, bootstrap, self-hosting, or release claim.

## Identity and composition

- Original PR601 common ancestor: `f0be2a55cc348fd1c9ce8eca377b2427541643b7`
  (tree `5f7ed31ee0c2a342b8ee053a3dcaa9f6ef0db2ce`).
- Original PR601 head: `5efe6d8928679bd4551980de80329879d934aaa7`
  (tree `64607f6ed8f4ff38df22c605909d8cbe3fb6d902`).
- Original first unique implementation checkpoint:
  `0a7bdfe1f8442404fa915c0259b7997a02225161` (tree
  `b795ad020f4f1e0098ca4f20fe5ac8932d1a40b1`).
- Current composed checkout before this uncommitted slice:
  `a3533c9cb10abe280bb8ad3431f74d7ccbcbadf7` (tree
  `d5dc2d420e5c3652f956bab4d2eea6a46c25e9db`).
- Current main used by the composition: `97445f364d938004618f2ac13d4d4c6e1ca32946`
  (tree `bde2ef38e3ff53c9a61a88b9ea2ff5d2be1a7bc2`).

The current split composition uses the actual `00581a` fixed-v2 helper,
`00581` legacy/compiler-artifact binding, and `00582` plan construction.  It
does not reintroduce the old inline helper or import the inherited `00723`
implementation.  The only production source change in this slice is the
owned `00582` plan-construction metadata handoff.

## Contract and implementation

When the compiler-artifact binding carries a native Clojure `Long` whose value
is exactly `2`, `00582` copies these observed binding fields into the plan's
compiler metadata:

- `:expression-lowering-source-contract-version`
- `:expression-lowering-functions-semantic-hash`
- `:expression-lowering-function-count`

The type check is intentional: boxed `Integer` 2 and `nil` take the unchanged
legacy route and omit all three fields.  The values are copied from the
observed binding after its owning validation path; no hash, count, source
version, or source-mode value is renewed or invented by plan construction.
Fixed-v2 and development-current
bindings remain distinguishable through their observed source-binding values;
the development marker is not silently promoted to compiler metadata.

## Focused evidence

From the composed checkout, without staging or committing:

```text
clojure -M tools/expression_binding_test.clj
  c11-relative-policy-test: 4 tests, 54 assertions, 0 failures, 0 errors
  expression-binding-test: 8 tests, 63 assertions, 0 failures, 0 errors

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
  6 tests, 11 assertions, 0 failures, 0 errors

git diff --check
  passed
```

The new test evaluates the actual current `00581a`/`00581`/`00582` forms and
checks version, target, session, emitter, source-read, source-drift,
symlink, complete-program, plan-identity, native-carrier, foreign-callback,
legacy-arity, v1, boxed-Integer, nil, and fixed-v2/development-current
controls.  Digest and native-carrier checks use actual repository helpers.
The source-reader, compiler, emitter, and assembler seams are deliberately
declared oracles with synthetic fixture data.

## Limits and nonclaims

This checkpoint does not execute the bootstrap, construct a real Gravity/Core
program, prove C6/C11/C5 admission, establish source-authentication authority,
or prove backend execution.  The relative-policy sub-harness reports
synthetic fixture authority and `:source-authentication false`; those results
are retained as bounded controls rather than promoted to migration evidence.
Root-owned `tools/c6_private_ingress_test.clj` and
`tools/c6_reader_policy_test.clj` changes are outside this slice and were not
modified by this work.
