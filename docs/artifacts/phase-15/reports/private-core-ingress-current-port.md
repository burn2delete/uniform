# Current private Core ingress controls

PR602's useful ingress behavior is already implemented by current component
`00716-p15-s23-c6c10-normalize-deferred-ratios-in-plan.clj`. This increment
preserves its missing mechanism controls against that current owner. It does
not restore the old versioned production stack.

## Source accounting

The original PR602 authored increment is commit
`cfe934c95a61241ad337f500a08222f79f047f61`, tree
`3de9da6f7b0700fd931bad9efcd9872e463de6bf`, over PR601 commit
`5efe6d8928679bd4551980de80329879d934aaa7`. Its four authored paths are
accounted for separately from its inherited main-relative stack:

| Original path | Current disposition |
| --- | --- |
| Component 00716 | Existing private ingress overloads select the Reader policy, construct one front end, project and validate the carrier, then obtain the Reader envelope receipt. The current sixth argument also preserves explicit Syntax selection and Macro mode. No old production body is replayed. |
| Component 00723 | The annotation removal accompanied a formerly unused helper. The current envelope helper is already used from split owner 00720c; its old component is not restored. |
| `tools/c6_private_ingress_test.clj` | Ported to current v2 routing, current six-argument front-end calls, and the actual current Reader/envelope helper. |
| `versioned-private-core-ingress.md` | This report preserves the current obligations and evidence limits; Git retains the original source and report. |

The inherited PR594 source binding, PR597 Reader policy, and PR601 expression
binding work are separate contributions. Current `00720b/c/d`, `00581a`, and
`00581` replace the old inline owner locations. Their old blobs must not be
replayed to obtain the private ingress tests.

## Current behavior and controls

The tests retain one front-end call, separate zero-arity and parameter plan
routing, carrier validation before receipt production, canonical Reader
selection, upstream refusal, forged Reader commitment refusal, stricter
envelope-owner path refusal, and receipt absence on legacy overloads.

Current public source routing supports version 2. The original version-3
positive iterations are therefore replaced by an early version-3 refusal
before any declared upstream callback. Integer-boxed version 2 and unsupported
versions still fail through the actual contract guard. No fixed source pin is
renewed and no v3 product is introduced.

The current explicit invocation test checks that C2/C3 receives only the
Reader/Syntax selection while the parameter resolver retains explicit Macro
mode. The Reader-policy harness supplies the newly required Syntax-path
resolver as a declared oracle and checks its returned selection schema/path.
This compatibility change does not authenticate a Syntax source.

## Validation scope

The private ingress suite has six tests and 53 assertions. Its nested current
Reader policy controls have six tests and 42 assertions; relative-policy
controls have four tests and 54 assertions. These use actual current ingress,
contract, carrier, digest, and envelope-policy code with declared upstream
producer, compiler, projection, and envelope-verifier oracles. All products
are synthetic. The commands are:

```bash
clojure -J-Xmx256m -M tools/c6_private_ingress_test.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The initial composition test exposed an unbound Syntax-path resolver in the
old Reader-policy harness after the PR512 API composition. That nonpass is
preserved in `/tmp/uniform-pr602-current-private-ingress.log`. The explicit
oracle and selection assertions correct the harness; production is unchanged.
The coordinator records the final source tuple, checks and independent review
separately. This report is not an integration receipt.

No actual Reader, C3/C5/Core producer, source authentication, private numeric
proof, backend, native-C, runtime, migration, release, self-hosting, or seed
retirement acceptance follows from these synthetic mechanism controls.
