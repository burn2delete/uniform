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

## Implemented source and local evidence

The existing 220-line validator body now lives in gravity.backend.c with six
per-use recognizer/refusal ports and one live instruction-catalog port. The
stage reuses its existing private owner-value selector; no new abstraction or
Bootstrap import is added. Public APIs increase from 20 to 21. The original
three-argument adapter passes actual Bootstrap Vars, preserving late lookup
and callable-before-argument selection. Recognizers, source-path resolver,
packet ordering, diagnostics and CI remain byte-identical. P15 receipt lineage
continues to name the actual Bootstrap compatibility entrypoint; historical
evidence is neither repinned nor relabelled as direct stage admission.

The immutable original validator at scope commit
f0b8cf79755a58dd73bdb3bbe95680ba2672447e and current wrapper consume
identical live inputs. All 22 complete raw and parsed outcomes match: ten
returns and twelve real B2 refusals. The paired actual Hello Gravity sources
compile through the existing ordinary current compiler route, retain equal
plan identities, and emit identical deterministic runtime C text after
validation. Capture records actual source paths/hashes and compiler metadata.
Inputs, including unsafe/runtime-check/human-review metadata, remain unchanged.
Capture SHA256:
4b634643c144fdbaec67ac920e404aa375362ceeee09a74ab171f24f8e1d0783.
No private C6/C11 factory, packet authentication, source replay or native
process is invoked by this evidence route.

The unchanged complete C pipeline baseline passes 18 tests / 265 assertions
under its existing 2g/180-second child. The expanded complete pipeline passes
25 tests / 365 assertions. Added controls include real paired compilation and
C text emission, actual source-span diagnostics, lexical string locals,
6000 nested do instructions without host recursion, retained large strings,
live catalog/recognizer/refusal Vars, function selection before comparator-
observed argument lookup, original exception identity and returning failures.
The historical ignored statement-literal boundary is pinned without claiming
that a subset-pass label validates arbitrary literal representation.

The first expanded run had nine failures from an incorrect test expectation
for a nonexistent fallback-status diagnostic field. The original diagnostic
owner supplies severity :error. Only the assertion was corrected; production
semantics remain unchanged, and the failed log remains preserved as NONPASS.
Independent reconstruction recovers the exact original validator body and
entire prior stage bytes. All eleven named recognizer, caller, loader, receipt
and CI/workflow files remain byte-identical. No tools directly load component
00642 and no C-specific construction/receipt dependency inventory exists.
No unrelated Core root list or isolated Core caller is changed.

The language boundary passes 6 tests / 11 assertions. Four affected JVM adapter
controls pass 53 assertions without running a native toolchain. Cold loading
exposes exactly 21 declared public APIs/arglists, zero namespace aliases and
no Bootstrap. Changed stage/test lint, ASCII/diff checks and both document
validators pass. Complete current-Main contextual lint, independent review,
hosted CI/artifact audit and exact coordinator integration remain separate
evidence requirements.
