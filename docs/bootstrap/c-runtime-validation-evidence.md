# Native C runtime-plan validation

## Contracts and boundary

D1/D2/D3/D6/D8/D9, C1/C14/C15/C18, B1/B2/B13/B14, BOOT8 and TEST2
govern this extraction. Original source parent is
9477c53a73e9e339c22f1872bbf681baf392eaf3, tree
e20fe5a2c37aba542a79641d3168c4f304f855fd. Published Main
e1d81c973ea9219d174d336941af2d017295166f is already an ancestor.
A fresh five-path source claim covers only the C stage, component 00642,
its complete existing pipeline tests, this evidence and the namespace map.

Move the existing iterative c-backend-validate-runtime-plan! body into
gravity.backend.c/validate-runtime-plan!. Preserve statement/value position,
lexical scalar locals, direct println str restrictions, malformed-shape and
unsupported-operation refusals, stable parent diagnostic subjects, traversal
order, original exceptions and per-use live operation selection. Source-path
resolution in the same component remains in Bootstrap without changes.

## Inputs, outputs and dependency direction

The internal API consumes source path, requested target and a caller-owned
native instruction plan, with explicit native recognizer/failure operations
and the existing supported-instruction catalog. It returns the existing
:passed label or propagates the actual B2-UNSUPPORTED owner refusal. That
label describes the existing closed runtime-C subset; it authenticates no
plan and grants no B1 admission, profile, effect, capability, safety, proof
or native execution authority. The stage imports no Bootstrap namespace.

The stage owns iterative subset decisions. Bootstrap keeps compatibility
arity, instruction catalog, scalar/local recognizers, failure construction
and publication, source compilation, authenticated packets, source-path
resolution, native staging, process execution and all admission gates.
Existing C11 runtime source emission remains a downstream consumer. Source
spans and diagnostic target/operation/missing-fact data must remain intact.

## Verification

Retain every existing C pipeline test and CI command. Add positive/negative
runtime-plan controls, actual deterministic C source emission after validation,
deep iterative traversal, malformed bindings and unresolved locals, direct
println str legality, provenance/metadata immutability, live catalog/function
interposition, callable-before-argument selection, original exception identity
and returning-failure behavior. Compare the immutable original validator with
the new wrapper on identical live inputs and complete outputs/refusals.
Verify exact source-body conservation and all isolated callers/dependency
guards. Run focused regression, language boundary, complete contextual lint,
document validators and independent semantic/adversarial review, followed by
own hosted CI/provenance and exact coordinator integration.

## Unsupported boundaries

This slice does not implement canonical verified-MIR admission, general C
lowering, new instructions, effect/capability/profile legality, proof discharge,
untrusted host-carrier validation, scalar representation changes, native
compiler execution, broker/staging/controller recovery, Gate A/B, private
C6/C11 authentication or replay, release, migration, self-hosting or seed
retirement. Existing native heap/stack/platform boundaries remain explicit.
Synthetic plan controls and emitted C text are not authenticated artifacts or
native executable conformance evidence.
