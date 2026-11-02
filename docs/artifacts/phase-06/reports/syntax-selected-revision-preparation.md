# Selected Syntax revision preparation

## Purpose and scope

This report records the useful, additive portion of PR580 for the finite
syntax/selected-reader-revision C2-to-C3 review. It does not admit a
compiler, a C3 child, a host operation, or a release claim.

The reviewed original PR580 head was
032c2aae4bf2e287d5487e7c282f741b094f50c7, based on
e9ec1c599614a99e3779b5e3a329e8d9a4b8fa80. Its selected Syntax owner/data
rewrites overlap the current PR512/PR544 owner composition and were not
copied here. In particular, this port does not replace the current Reader to
Syntax handoff, reintroduce a second DATA/export authority, or route Macro or
Reader products implicitly.

The isolated current composition is:

- base d97539762108d7e44024164c61bb120044ef684b, tree
  093a9c284a1e2fc4dd360fa677e43f7a0512dfba;
- port commit 127f97ca221ae16434d187013bfd53a4091cdb6a, tree
  b00eed074ace0d52ed47570f311d8ac6ab0e00b3;
- original PR580 ancestry is preserved only as historical review input.

## Additive controls

The port adds only the following source-owned preparation and control paths:

- selected_revision_acceptance.clj and its test: one explicit owner entry
  point that requires the complete source/head/tree/classpath/configuration
  closure, checks the current Reader revision and selected Syntax source, and
  exercises positive C3 evidence plus binding, physical-source, revision and
  envelope substitutions;
- selected_revision_request.clj and its test: inert request preparation,
  current runner source/signature binding, source/configuration rechecks,
  environment digesting, and explicit non-launchable child wiring;
- selected_revision_git.clj and its test: selected Git bookend protocol
  controls retaining the raw Process/token/handles, preserving original
  wait/cleanup/interruption/fatal errors, and keeping unknown descendant
  coverage unknown;
- selected_revision_parent.clj and its test: fake-port C3 order and
  failure-custody controls for C-before-L, L-before-C release, watchdog,
  capture terminality, parent rechecks, launch-attempt recording and
  same-token finalization;
- selected_git_configuration.clj and its test: inactive, pure comparison
  of opaque configuration bytes. It is not wired into Git selection and does
  not claim effective configuration or native-helper coverage.

The request binds the actual current-base runner blob
2c56243ced930f7cd253b9b10f2444267010f168fdd3e8e4b99b87e3c2f3e16c.
It checks the public signatures of start-process,
retain-owned-process!, retained-process-handles, two- and three-argument
process-result, and one- and two-argument finalize-owned-process!.
Changing that source or signature set fails closed. The proposed host heap,
timeouts, capture limits and coordination protocol remain host restrictions,
not compiler input quotas or project accounting.

## Focused evidence

The compiler-free panels pass 39 tests / 1,554 assertions:

- selected Git configuration: 4 / 37;
- selected C3 acceptance inputs: 6 / 28;
- request preparation and current runner binding: 4 / 51;
- selected Git lifecycle controls: 12 / 241;
- parent sequence and failure custody: 13 / 1,197.

These tests use fake owner/runner/lock/watchdog ports and inert filesystem
fixtures. They do not load Bootstrap, call a genuine compiler or Reader,
start a child, invoke Git, acquire a broker or lock, or run native C/host
operations. Genuine selected C3 producer acceptance remains open and must be
run only after the current PR512/PR544 owner composition supplies its
validated source handoff.

## Conformance and nonclaims

All retained lifecycle observations preserve raw values in memory and expose
only serializable facts. Unknown launch, capture, cleanup, descendant and
creation coverage remains unknown and cannot authorize release. No process
retry, denied-operation workaround, fresh permission prerequisite, source
hash refresh, project budget, compiler quota, or replacement accounting was
introduced. The original PR580 owner/data/export rewrites and their historical
evidence remain review history rather than current authority.
