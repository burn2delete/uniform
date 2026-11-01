# Versioned pinned C11 construction source

## Purpose and dependencies

OWN-06 requires the actual selected C11 builder and verifier between fresh Core
authentication and final public replay. PR605 supplies lexical Core
authentication. This finite Native increment restores lexical builder and
authenticated-construction helpers 04/05 byte-identically from preserved
f66d9ff4554815b89ee7d51ee73b7961f0c33a44. Its baseline is PR605 head
b17e639db90a4c46af6e9d953ce53140833b3eb0.

The preserved private flat-produced verifier is placed in existing 00740,
before its policy helpers. This order retains existing source-context tooling
and introduces no component registration. Authority-holder/classifier/public
definitions and cleanup stay unchanged. No source pins or Gravity programs
change. No active workstream reservation names these changed helper paths.

## Behavior and artifacts

The builder still requires the captured opaque token. It selects the actual
Gravity builder from the authenticated source binding's interface version.
Version 3 preflights the produced carrier, then invokes the selected Gravity
flat-Bool verifier on that Core/MIR pair using the same source plan. Its host
checks require the complete native report shape, source-Core and MIR identities,
scope, successful status, exact checks/preservation/nonclaim vectors and no
diagnostics. Existing builder rejection and exception handling remain.

Authenticated construction retains its token check and legacy overload. The
selected version is passed into actual source binding, final artifact creation
and final artifact validation. Independent low-level MIR validation still runs.
The resulting artifact keeps the public replay requirement and false live
invocation claim; this private construction is not public verifier acceptance.

## Conformance evidence and limits

`tools/c11_pinned_construction_test.clj` runs 6 tests / 53 assertions after the
relative-policy 4/54 and artifact-data 6/58 controls. Actual lexical helpers,
token identity, selected function dispatch, native carrier/report checks,
relative finalization, policy binding and final-artifact validation execute.
Source binding, Gravity runtime and low-level MIR verification are declared
oracles. Core/MIR/report products and diagnostic transport are synthetic. The
private holder is instantiated in a test namespace and confers no production
authority. Bootstrap remains unloaded; no actual source authentication or
Gravity execution occurs.

Controls cover selected versions 1/2/3, exact builder/verifier calls, the two
low-level verification invocations in construction/final validation, public
replay nonclaims, token refusal before owner calls, nine flat-report
substitutions, builder carrier/rejection refusal before the verifier, source
binding tamper at final validation and legacy overload parity.

Standalone test lint is clean. Non-evaluating source-context comparison against
the exact PR605 baseline finds no new findings and three unchanged lexical
binder warnings in helpers 04/05. No lint suppression or repository diagnostic
baseline is introduced. Preserved helper and flat-verifier bytes are checked.

## Remaining acceptance

Public C11 constructor/report/verifier version dispatch and genuine replay
remain. Actual supported-product construction, fresh Core authentication and
actual Gravity positives/negatives remain unaccepted. Exact independent source
review, applicable CI, current-main composition and coordinator integration
remain separate. Do not replay identical or tree-equivalent prerequisites or
interpret earlier reviews as acceptance of this delta or entire ancestry.

PR453 original-v2 evidence, PR574's unresolved final independent disposition,
PR446 UNKNOWN and operation-specific holds remain unchanged. No denied message,
unknown registration or held runtime/controller operation is retried. No PR552
completion, OWN-06 closure, v3 product acceptance, backend, release or
self-hosting authority is claimed.
