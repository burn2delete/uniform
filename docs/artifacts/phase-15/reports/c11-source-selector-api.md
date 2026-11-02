# Pure C11 source selector API

## Purpose and requirements

This prerequisite for PR552 exposes the fixed producer-owned C11 source
contract, binding and rule helpers without executing or authenticating a
product. The header parser, C11 discriminants and v2/v3 contract DATA are taken
from preserved Native f66d9ff4554815b89ee7d51ee73b7961f0c33a44. Source pins
are unchanged. Legacy public constructor/verifier arities are untouched.

## Source and interfaces

`gravity.source-interface-header/source-interface-version!` and
`gravity.c11-mir.source-interface/interface-version!` select explicit native
v2/v3 headers. The generic `interface-version!` retains its v2-only contract.
Metadata, duplicate header storage, foreign classes, wrong interface names,
extra keys, non-Long versions and mixed artifact versions remain invalid.

The existing `00732` component exposes `p15-s23-c11-mir-contract`,
`p15-s23-c11-mir-contract-binding`, selected builder/verifier functions and
`p15-s23-c11-mir-versioned-record`. `00741` projects those fixed selections in
`p15-s23-c11-mir-source-rule-record`. Legacy bindings without an interface
version retain their historical rule fields. V3 selects the flat-Bool builder,
verifier and semantic family from its preserved contract.

## Dependencies and limits

PR552's policy validation/binding APIs and actual versioned public C11 verifier
arities remain separate producer dependencies. This slice supplies no fallback
implementation, source authentication, constructor, fresh public report or
policy acceptance. Calling a pure helper does not make supplied DATA genuine.
Actual versioned product acceptance and downstream C14/B1 source contracts
remain unaccepted; OWN06 and the existing runtime holds remain unchanged.

## Artifacts and checks

`tools/c11_source_api_test.clj` evaluates only the named pure helper definitions
in an isolated namespace with synthetic legacy constants. It checks fixed v2/v3
identities, changed-identity projection, legacy fields, invalid headers and
mixed versions. It does not load Bootstrap or execute Gravity.

The paired header component registration is reused from PR453; it is unrelated
to PR446's unknown hosted-DATA registration submission. No PR446 registration
action is performed here. Its original acknowledgement and terminal disposition
remain missing.

## Acceptance criteria

Require passing synthetic helper, header, ownership/catalog and language-boundary
checks; an exact source-version bridge for the changed ownership input;
independent review of the immutable pure slice; and normal current-main
composition/CI before integration. Genuine Core/C11 production is not an
acceptance criterion for this pure source-selection increment and receives no
credit from it.

Observed source-only validation: pure helpers 2 tests / 41 assertions;
header and ownership/catalog controls 5 tests / 586 assertions; component
catalog 1 test / 6 assertions; language boundary 6 tests / 11 assertions;
documents 240 / 19 and full-language roadmap passed. Bootstrap remained
unloaded in the synthetic/ownership controls. Initial projection attempts
failed on the predecessor fingerprint and then on the unrebound whole-file
ownership hash; those attempts are not passing evidence. The successor bridge
binds the actual committed ownership file and keeps prior review history.
The corrected crosswalk passed 12 tests / 144 assertions. A draft preparation
initially copied an older bridge adapter; final diff review caught the missing
newer Reader history before publication. The final adapter retains current
main's complete history and changes only the ownership pin, successor
fingerprint/qualification and the historical-versus-current ownership checks.

## Current-main source composition

The local successor normally merges accepted `c15e5be3` with main
`6c7df70327e8ccfe138bd21ffa0db89a11cee99e`. The source merge is
`3f22e4be1ec73b85bec9dd1a47c5dea9732cdd63`; its parents are actual main
and the accepted candidate. The final candidate identity is recorded outside
this document so it cannot claim its own commit hash.

The pure C11 contract, header, selector and rule code retain the accepted
mode/blob identities. Main's C3/Syntax, Reader extraction, loader, development
runner, CI configuration and production dependencies are preserved. The six
overlapping reviewed paths are the ownership test, Stage0 component catalog,
ownership EDN, SH projection, crosswalk adapter and crosswalk tests. Catalog
and ownership counts add the paired header without dropping current helpers.
The three textual conflicts were in reporting projection/adapter/tests.

`pr574-composition-source-version.edn` binds the actual committed ownership
bytes at the source merge and the exact main predecessor. Removing its three
listed header registrations reproduces main's ownership record. The historical
Native successor from PR453 remains in `source-version-bridge.edn`; merged
PR640's Reader history remains in `reader-assembly-source-version.edn`, and
the subsequent Syntax history is unchanged. Both predecessor review maps are
retained. The recomputed fingerprint describes this reporting input only;
all 180 current whole-criterion states remain unassessed.

Current focused checks passed: reporting conservation and negative controls
14 tests / 192 assertions; affected ownership/Reader/Syntax/catalog checks
4 / 594; leaf catalog checks 3 / 25; language boundary 6 / 11; governance and
document/roadmap validation. An initial added-test parenthesis error was
corrected before these passing results. Accepted synthetic API controls were
not rerun. No Bootstrap, Gravity compiler, source authentication or genuine
Core/C11 product was executed.

The private candidate-bound source closure records every tracked mode/blob
and every source/test classpath-root file, actual resource multiplicity,
namespace declarations, classpath dependencies and load-manifest units. It
must include exactly one `gravity.reader` resource from this worktree. Static
closure inspection does not establish equivalence to PR453's historical
physical runtime inputs or execute that closure. Source-contract DATA pins
are unchanged; no latest-source or v3 product acceptance is inferred.

This is a local source freeze awaiting one affected composition review routed
through Reconciliation. No push, CI dispatch, integration preflight, GitHub
approval or merge is performed by this step. Current-composition CI and a
fresh exact-base preflight remain gates for any later coordinator integration.

Main subsequently advanced to `bf570797d32c0bd78d81cd51d034376d019c6dc1`
through PR651. A conflict-free local merge preserves its ten incoming paths,
including the Syntax plan-runtime extraction and expanded syntax CI selection,
byte-for-byte. No incoming path overlaps this candidate's owned delta. The
ownership bytes and predecessor histories are unchanged by that incoming
main; their existing source equation remains valid. Final static closure and
focused checks bind the resulting new tuple, without executing PR651's genuine
authentication/runtime tests or treating prior runtime evidence as current.

## Published successor conflict composition

The published `d15f5795` source acceptance is preserved as bounded history.
Its publication against newer main encountered real reporting conflicts and
did not start PR CI. The next local source merge is
`c581bfb8465b1fcb066e51d3e335d14ae465dc72`, with parents published
`d15f5795a51e6dee3b5699d633d11dcda3af5bb4` and actual main
`e8f6fe3bf1971503bcf1d705d88c7432b95fdcca`.

The crosswalk projection, adapter and tests combine both predecessor histories.
`pr574-conflict-composition-source-version.edn` binds the committed ownership
union and exact source-file references for both predecessor review records.
It adds only the accepted three header registrations to current main, retaining
the current macro helper, Reader, Syntax and Native ownership entries. Older
source-version tests compare their historical committed inputs; the new
positive and negative composition control owns the current live-byte check.
The fresh reporting fingerprint remains pending independent review and grants
no implementation or runtime acceptance. All 180 whole-criterion states stay
unassessed.

All seven accepted pure source/test blobs remain identical to published d15
and accepted c15. Every other production source, loader input and configured
CI input is conserved from the new main. Reuse of the 9/102 synthetic controls
is limited to those identical pure inputs; it makes no claim of unchanged
historical PR453 runtime closure. The new candidate's private source/resource
equation and focused control results bind its final exact commit/tree.

No genuine Native, Bootstrap, Reader, source authentication, MIR, runtime,
lease or process operation is part of this conflict resolution. Publishing a
changed successor requires the affected independent disposition and actual
publication/CI gate checks. Normal PR CI and a later fresh coordinator
integration preflight remain separate from local source validation.

## Material macro-artifacts main successor

Accepted source `609cc3c8` was composed with the three historical verifier
census additions as `4e919402`. That tuple obtained a final conditional
applicability PASS and successful configured CI. Its immediate merge preflight
then failed the base identity check because main advanced to
`71a63ce23755f6ca84f9af6c2f5a7ead95b233f0`. The failed preflight remains
NONPASS; neither the prior CI nor the old-base disposition permits merging
against that material new base.

The new source merge `3cd79ed80a3c3808f0471dfbbf68dab8ca3a0aa4` has
parents `4e919402` and that main. It preserves the incoming macro artifact
extraction, retired semantic helper deletions, loader changes and configured
separate required macro CI mode. Only the three reporting text conflicts need
resolution; no Native contract or selector implementation changes.

`pr574-macro-artifacts-composition.edn` pins the exact current ownership union
and immutable predecessor review references. Current main's macro run-storage
consumer now points to the macro stage; the union preserves that field and
every other main ownership entry while adding only the accepted three header
registrations. All Native453/Reader640/Syntax/macro/compiler-domain histories
remain available unchanged. Older live-union tests now inspect their committed
historical snapshots, and a new current-union control owns the live check.
The reporting fingerprint is not a compiler migration or source-contract pin
refresh, and all 180 whole-criterion states remain unassessed.

The seven pure source/test blobs are still identical to accepted c15 and
published d15/609. Other production and CI inputs must be conserved from
actual main71a63, not relabelled as unchanged from the older physical runtime
closure. The private final tuple binds a new static root/resource/loader
equation for this material input change. Prior pure controls remain reusable
only at matching unchanged dependencies; genuine Native/source authentication,
runtime, PR446 and broader acceptance remain outside this increment.
