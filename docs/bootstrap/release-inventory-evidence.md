# Release JAR inventory extraction

## Purpose and governing contracts

Move the existing native packaged-source and whole-JAR inventory report
algorithms from Bootstrap component00906 into `gravity.release`, correcting the
expected source-entry order to match the existing sorted archive reader.
D1/D2/D3/D8/D9,
C1/C15, PKG3/PKG7/PKG10/PKG12, T1, BOOT8 and TEST2 govern the boundary. Phase18
P18-T02 supplies the existing temporary JVM packaging implementation; this
extraction does not reopen or complete a release milestone.

## Declared boundary

The two report APIs consume observed entry names and explicit late owner Vars
for expected inventory. Whole-file report ordering, frequencies, missing and
unexpected entries, exact membership and duplicate refusal are preserved.
Expected source entries now use the same canonical order as observed entries.
They perform no IO and introduce no entry-count allowance. Bootstrap retains its
compatibility arities, inventory definition, filesystem reads, Java compilation,
JAR invocation and P18T02006 diagnostic construction. Producers do not import
the release namespace; Bootstrap alone supplies the existing inventory ports.

## Evidence obligations

Initial base is612367e981ec37072cc3d3525f09263bdc91b264. Before extraction,
capture complete native reports and actual disposable JAR outputs from the
existing builder. Retain entry content hashes, source material and the actual
JDK/compiler invocation. Reject a real JAR containing an unexpected entry and
cover omissions, duplicates, wrong entry order and non-string entries. Compare
complete report values and serialization, preserve the entire native component
except the two compatibility wrappers and Bootstrap's single require, and retain late Var lookup
and dependency exceptions.

Run focused packaging/inventory and affected stage tests, language boundary,
ownership, docs/roadmap/governance, contextual lint and independent review.
Maintain an early draft PR. Own CI and exact clean current-main preflight are
required before integration. This scope commit supplies no implementation
credit; evidence results must distinguish actual artifact creation from any
unrun launcher or broader release claims.

## Actual baseline refusal and corrected behavior

The first genuine disposable build emits a JAR, then rejects it as P18T02006.
The whole-file report is valid with no missing or unexpected members. The source
report has the same members on both sides, but expected declaration order puts
diagnostics before Darwin publication while the existing archive reader sorts
Darwin publication before diagnostics. This is a false source-order refusal,
not evidence of an accepted package. The original log, full diagnostic and
rejected archive remain in `/tmp/uniform-release-inventory-evidence/` and
`/tmp/uniform-release-inventory-before-control.log`.

The only rule correction sorts expected source names before comparison.
Observed order, duplicate detection, non-string handling, missing/unexpected
members and diagnostic construction retain their existing behavior. Both
complete report bodies move through explicit late Var ports; the whole native
component otherwise conserves. Bootstrap gains only the release require.

The corrected builder emits a real disposable JAR and both reports accept it.
A second genuine build has an unexpected class entry added by the actual JDK
archive command before the builder's existing inventory gate; the gate rejects
P18T02006 with the exact unexpected member. This is trusted host test
interposition around real archive creation, not authenticated source admission.
The launcher is compiled but not executed. New focused tests pass6/65; the
existing inventory regression passes1/59. Ownership39/1107 and language6/11
pass. Coupled Release/CLI/Build/Reader/runtime/package/library/literal tests
pass41/573; docs240/19, roadmap and governance185 pass. The new standalone
helper uses the coordinator's SH-00 association;
primary component counts, completion formulas and migration pins are unchanged.

Controls are `/tmp/uniform-release-inventory-control.clj` and the read-only
`/tmp/uniform-release-inventory-verify.clj`. The original failure and accepted
and tampered archive snapshots retain raw archive and entry hashes. Changed
Bootstrap source changes the archive hash; unchanged launcher and other native
materials remain byte-identical. No cross-revision archive identity equality or
PKG7 reproducibility claim is made. Remaining coupled checks, lint, exact review,
own CI and clean current-base preflight still govern integration.

## Verified Reader main composition

The next normal composition uses actual main
ec15f95051e6e9e674974d43f5a14e6555cf176a/tree
118c6a279533a1e796d34287dc2fba526a92991f, the verified Reader PR739 merge.
Reader implementation, compatibility component, tests and evidence remain
byte-identical to main. The shared map preserves Reader's row and merges the
Release row without conflict. Both Release algorithms, the canonical-order
correction, wrappers, tests and helper ownership remain exactb905dc6b.

Fresh coupled tests include current Reader decoding. Real archive controls and
whole-component/hash verification use `reader-main-*`, preserving the original
false refusal as immutable history. New exact-source review, contextual lint,
own CI and clean current-base preflight remain gates. Older base checks do not
confer current-base acceptance or release authority.

## Leaf CI dependency main composition

Actual main09e48072fbb69131dbe3a7826b4dbbc4c9bb45f0/tree
c63848d2d311935d3b0764c64a2bb0e6675f70e6 changes exactly four CI workflow,
runner/test and documentation paths. All four are preserved byte-for-byte.
Release algorithms, native components, fixtures and executed archive inputs
are unchanged from eedff158, with no implementation changes from this composition.
Unknown or cross-cutting leaf changes retain the new selector's full fallback.
Source-Core readiness remains omitted, not passed. Current-base archive and
identity/conservation controls, affected checks, independent acceptance, own CI
and clean integration preflight remain gates. Older green does not transfer.

## CI harness environment evidence

The current CI runner tests pass20/221 with the installed linter directory on
PATH. The initial invocation lacked that directory and recorded20/192 with two
missing-linter errors; its nonpass log remains at
`/tmp/uniform-leaf-main-ci-runner-missing-linter-nonpass.log`. Correcting the
command environment changes no repository source, guard, policy or watchdog.
Terminal harness receipts are archived outside the source checkout.

## Unsupported boundaries

Expected inventory and status fields are trusted native data, not artifact
authentication or proof authority. This slice does not implement canonical
release assembly, signing, SBOMs, certificate/identity/revocation policy,
hermetic or reproducible-build proof, packaged dependency closure, runnable
launcher conformance, source/Core authentication, self-hosting, seed retirement
or final release. Host JDK, process, filesystem and archive-format restrictions
remain real platform boundaries. Historical denied Reader/H6/native/full
compatibility routes and migration/admission pins remain untouched.
