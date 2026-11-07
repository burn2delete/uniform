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

The APIs are native-manifest [operations input-id],
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

## Regression and adversarial evidence

The native-manifest test namespace is registered in the existing focused
test inventory and contracts CI commands. Exercise every moved check, original
refusal ordering, returning/throwing callbacks, late Var reads, exception
identity and known structural vacuity. Keep the old scalar LLVM tests intact;
this increment does not select their authenticated Stage2 builder route.

Compare full old/new records and exceptions in the same live context, with all
three original function roots rebound together. Verify exact source-body
conservation and all surrounding parser/admission/diagnostic/assembly bytes.
Emit the real fixed .ll fixture from the declared data boundary and verify
its input identifier, hashes and deterministic record identity. Any fixture
data remains explicitly unverified, with no source-to-backend admission
or authenticated upstream closure inferred.

Run focused native checks, cold stage loading, affected composition, CI harness,
language/ownership boundaries, lint, documentation validators and independent
semantic/adversarial review. Open and maintain an early draft PR; successful
hosted CI and exact coordinator integration remain separate requirements.

## Retained baseline refusal

The initial baseline called b3-document-file-artifact on the existing
accepted/backend-native-lowering.gravity fixture. Its upstream document
artifact chain reached the frozen C2 Reader boundary and rejected with
C2-HASH, missing-field :exact-regular-pinned-sh03-reader-source: expected
231576 source bytes, observed 246369. No baseline artifact was emitted.
The original log is /tmp/uniform-llvm-manifest-baseline-script.log.

That source-artifact route remains held. Do not retry, repin, rebind its
reader, substitute an upstream result, or reconstruct an admitted artifact
downstream. Independent boundary review assessed the proposed pure data-body
extraction as separate, non-admitting ownership work. It allows only that
isolated data/document extraction, retaining
the refusal as a blocker for source-to-backend claims. Its record is
/tmp/uniform-llvm-manifest-boundary-review.md, SHA256
7e2f875125ae8ab1094a2fcd1814fa807d8b9edbef204f080ddc5e2ff9345a16.

The first baseline command accidentally selected the development test runner
instead of the helper script. It rejected an unknown exact selector and ran
no test; /tmp/uniform-llvm-manifest-baseline.log retains that invocation.
The helper delimiter error was caught by lint before execution.

The native manifest suite passed 10 tests/160 assertions and emitted the
existing 408-byte LLVM fixture with SHA256
ce5f879bde4b82900f36254519ba6f28e307c166363a759c53b1c067f50c48fe.
It covers all original structural conditions and five forbidden IR flag
spellings, callback arguments/refusal order, returning and throwing callbacks,
exception identity, late Var reads and callable selection before argument
evaluation. The first run had two assertion-indexing mistakes on captured
argument lists; the corrected run passes, and the first log remains in
/tmp/uniform-llvm-manifest-native-tests.log. Production behavior did not change
to correct those assertions.

Existing scalar/data tests passed 5 tests/50 assertions before extraction.
The Gravity-plan-realizing test was not selected. A hash callback that rebinds
the fixture IR can still cause the old first-read hash and later-read content
to differ; the extraction preserves that behavior and cannot authenticate such
interposition. Forged target/IR/proof labels and an empty coverage record
retain the original structural vacuity, not proof authority.

The three original Bootstrap function roots were rebound together from scope
commit 1929afcab1bc13352f9700540d5e1bff340e1c88 in one live data context.
All 32 full raw and parsed outcomes match: 12 returns and 20 structural
refusals, including returning/throwing callbacks, late failure and diagnostic
roots and the hash/content interposition case. The record is
/tmp/uniform-llvm-manifest-parity.edn, SHA256
0a460630034db18825499de9151c695cc3a0bd04474a268b54435326ba575923.

Source conservation verifies the three exact body/adapter transformations,
the byte-identical original scalar helper section and twelve protected
Bootstrap/parser/lowering/diagnostic/Reader/test/workflow paths. All edits are
confined to the original ten-path lease. The eleven prior contracts command
vectors are unchanged; one full native-manifest namespace command is appended.
The record is /tmp/uniform-llvm-manifest-conservation.edn, SHA256
d85e2b0fbdcc2213b3a97a872b2b1fd7218cb85176dbc03ea33d0149732868b1.

The appended 2g/180-second command passed 10 tests/160 assertions. The five
existing scalar/data regressions passed 5/50 after extraction. Cold loading
finds 19 public APIs with Bootstrap absent. CI harness checks passed 54/587;
language/ownership checks passed 48/1202. Direct lint, tools/ci formatting,
diff checks, the 240-document/19-index ASCII validator and the structural
roadmap validator passed. These are development checks; final independent
source review, full context, own hosted CI and integration are separate.

## Unsupported boundaries

The legacy LLVM fixture is fixed text; it does not lower the supplied program's
MIR. Existing :complete, :passed and proof-named fields remain document-coverage
data and do not establish B1/C14 ingress or full B3 conformance. Shape checks
do not validate arbitrary LLVM, bind all manifest fields or authenticate proof
labels. No LLVM toolchain, native execution, private source factory, frozen
Reader route, source-pin refresh, release, migration, self-hosting or seed
retirement support is claimed.
