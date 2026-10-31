# C11 relative Core policy DATA validation

## Purpose and dependencies

This Native ACC10 increment restores the relative policy validation and binding
helpers consumed by PR552. It composes PR574's fixed C11 contract selection
(c15e5be3c455d7499128fabe61e156f9cdea1b97) and PR584's fixed C6 contract DATA
(38a9ae7f8446ceabf435486146ebe2e671033acf). Neither prerequisite is represented
as integrated or independently accepted beyond its recorded disposition.

C6, C11 and D9 govern checked Core provenance, MIR input coherence and evidence
attribution. The implementation is the complete preserved
`00740a-p15-s23-c11-current-core-policy.clj` from
f66d9ff4554815b89ee7d51ee73b7961f0c33a44, appended to the existing 00740
component. The existing component order is unchanged. Its forward declarations
refer to the existing ingress helpers in 00741.

## Requirements and outputs

The validator accepts only coherent relative policy DATA for the selected fixed
C6/C11 contract. It preserves native-carrier validation before policy traversal,
closed policy and receipt keys, native version classes, source and expression
identities, development-unadmitted status, metadata projection contract, and a
Core/context-bound digest of the supplied receipt.

The C6 receipt binding uses `:gravity/c6-c10-source-policy-v2` or
`:gravity/c6-c10-source-policy-v3`. The C11 hash of the complete supplied policy
uses `:gravity/c11-checked-core-policy-v2` or
`:gravity/c11-checked-core-policy-v3`. The two identities are distinct. Hashing
uses the existing canonical encoder and streaming digest; no encoding or pin
is replaced. Projection and ingress helpers retain their relative DATA scope.

No existing constructor, public verifier, default execution path or source
pin changes. In particular, a coherent supplied or projected policy is not
fresh authentication of a report, checked Core, Reader or C11 product. The
public verifier must still invoke the actual fresh Core owner and actual
Gravity replay; those missing versioned public arities remain separate work.

## Conformance and evidence scope

`tools/c11_relative_policy_test.clj` evaluates selected actual definitions in an
isolated namespace. Native field access, carrier validation, fixed contract
selection, canonical encoding, streaming digest, relative validation and
projection are actual code. Diagnostic transport and diagnostic path selection
are synthetic. Fixture Core, context and reports are synthetic DATA, explicitly
without source or report authority. Bootstrap remains unloaded.

Four tests / 54 assertions cover coherent v2/v3 selection, distinct digest
domains, map-order independence, changed Core/context, policy/receipt/source/
expression/version tampering, projection without authentication, metadata and
foreign-class/comparator rejection with zero foreign callbacks. The language
boundary passed 6 tests / 11 assertions. Exact source review and independent
integration disposition remain required; these controls grant no authentication,
PR552 completion, OWN-06 closure, release or self-hosting credit.

PR453 still needs actual original-v2 consumer evidence through its existing
MIR owner. PR446's original registration remains unknown. Existing denied
messages and runtime holds are unchanged; this increment performs no retry,
replacement registration, producer, authentication, broker or controller action.
