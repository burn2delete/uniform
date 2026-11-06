# Native LLVM document manifest ownership

## Governing contracts and scope

D1/D2/D3/D6/D8/D9, C1/C11/C14/C15, B1/B3/B13/B14 and TEST2 govern this
increment. The implementation lineage begins at
9ca96ae5d166cf977469a469fa31cfae2fdbee98, tree
90c9c0a5520c71ce266c672150210726cfead4ca, which contains published Main
41aaea100091dc80067328e9b066f7378845eca3. Earlier increments retain their
separate source reviews, CI results and integration status.

Move the three existing B3 document manifest, structural validator and
coverage-report bodies from Bootstrap into gravity.backend.llvm. Preserve
their current behavior, including legacy status labels and incomplete
structural checks. This does not implement canonical LLVM emit/verify or
authenticate MIR, proofs, target configuration or backend ingress.

## Public boundary and dependency direction

The proposed APIs are native-manifest [operations input-id],
validate-native-manifest! [operations source-path artifact], and
native-coverage-report [operations artifact]. The explicit operation ports
retain late reads of artifact hashing, fixture LLVM text, diagnostic IDs and
the existing failure publisher. Bootstrap keeps its existing public function
names as adapters and depends on gravity.backend.llvm. The stage depends only
on clojure.string and caller-supplied operations.

Inputs are the existing input identifier and native document-coverage artifact
maps, not authenticated backend packets. Outputs are the existing LLVM
manifest, :complete structural-check result and coverage record. The manifest
carries actual fixture LLVM bytes, their existing content hash, the input
identifier and the original source/debug/proof/provider declarations.

The stage owns the moved B3-MANIFEST, B3-TARGET, B3-METADATA, B3-UB,
B3-POINTER, B3-NUMERIC, B3-ATOMIC, B3-RUNTIME, B3-ABI and B3-PASS conditions.
Bootstrap retains failure publication, source spans, source parsing, upstream
lowering, source overrides, fixed diagnostic/IR values and artifact assembly.
No admission, publication, source authority or certificate moves with the data.

## Planned regression and adversarial evidence

Add a native-manifest test namespace and register it in the existing focused
test inventory and contracts CI commands. Exercise every moved check, original
refusal ordering, returning/throwing callbacks, late Var reads, exception
identity and known structural vacuity. Keep the old scalar LLVM tests intact;
this increment does not select their authenticated Stage2 builder route.

Compare full old/new records and exceptions in the same live context, with all
three original function roots rebound together. Verify exact source-body
conservation and all surrounding parser/admission/diagnostic/assembly bytes.
Use the existing ordinary Stage0 reader and document-artifact path for accepted
and rejected source, emit the real fixed .ll fixture, and verify provenance,
hashes and deterministic record identity. Record current execution separately
from migration or release evidence.

Run focused native checks, cold stage loading, affected composition, CI harness,
language/ownership boundaries, lint, documentation validators and independent
semantic/adversarial review. Open and maintain an early draft PR; successful
hosted CI and exact coordinator integration remain separate requirements.

## Unsupported boundaries

The legacy LLVM fixture is fixed text; it does not lower the supplied program's
MIR. Existing :complete, :passed and proof-named fields remain document-coverage
data and do not establish B1/C14 ingress or full B3 conformance. Shape checks
do not validate arbitrary LLVM, bind all manifest fields or authenticate proof
labels. No LLVM toolchain, native execution, private source factory, frozen
Reader route, source-pin refresh, release, migration, self-hosting or seed
retirement support is claimed.
