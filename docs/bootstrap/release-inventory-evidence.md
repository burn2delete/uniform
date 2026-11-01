# Release JAR inventory extraction

## Purpose and governing contracts

Move the existing native packaged-source and whole-JAR inventory report
algorithms from Bootstrap component00906 into `gravity.release`. D1/D2/D3/D8/D9,
C1/C15, PKG3/PKG7/PKG10/PKG12, T1, BOOT8 and TEST2 govern the boundary. Phase18
P18-T02 supplies the existing temporary JVM packaging implementation; this
extraction does not reopen or complete a release milestone.

## Declared boundary

The two report APIs consume observed entry names and explicit late owner Vars
for expected inventory. They preserve expected/observed ordering, frequencies,
missing and unexpected entries, exact membership and duplicate refusal. They
perform no IO and introduce no entry-count allowance. Bootstrap retains its
compatibility arities, inventory definition, filesystem reads, Java compilation,
JAR invocation and P18T02006 diagnostic construction. Producers do not import
the release namespace; Bootstrap alone supplies the existing inventory ports.

## Evidence obligations

Initial base is612367e981ec37072cc3d3525f09263bdc91b264. Before extraction,
capture complete native reports and actual disposable JAR outputs from the
existing builder. Retain entry content hashes, source material and the actual
JDK/compiler invocation. Reject a real JAR containing an unexpected entry and
cover omissions, duplicates, wrong entry order and non-string entries. Compare
complete report values and serialization, preserve both entire native
components except the two compatibility wrappers, and retain late Var lookup
and dependency exceptions.

Run focused packaging/inventory and affected stage tests, language boundary,
ownership, docs/roadmap/governance, contextual lint and independent review.
Maintain an early draft PR. Own CI and exact clean current-main preflight are
required before integration. This scope commit supplies no implementation
credit; evidence results must distinguish actual artifact creation from any
unrun launcher or broader release claims.

## Unsupported boundaries

Expected inventory and status fields are trusted native data, not artifact
authentication or proof authority. This slice does not implement canonical
release assembly, signing, SBOMs, certificate/identity/revocation policy,
hermetic or reproducible-build proof, packaged dependency closure, runnable
launcher conformance, source/Core authentication, self-hosting, seed retirement
or final release. Host JDK, process, filesystem and archive-format restrictions
remain real platform boundaries. Historical denied Reader/H6/native/full
compatibility routes and migration/admission pins remain untouched.
