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
