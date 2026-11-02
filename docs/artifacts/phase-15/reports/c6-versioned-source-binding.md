# Versioned C6 compiler source binding prerequisite

## Purpose and dependencies

PR552's genuine versioned public C11 verifier must call a fresh versioned C6
owner. The existing C6 source-binding layer accepts only legacy compiler
identities. This finite ACC10 increment restores selected compiler binding
before changing fresh Core construction or public verifier arities. C6 and D9
govern source provenance, versioned lowering invalidation and evidence scope.

The baseline is PR590 head f0be2a55cc348fd1c9ce8eca377b2427541643b7. Source is
restored from preserved Native f66d9ff4554815b89ee7d51ee73b7961f0c33a44:
00719, the versioned functions from 00720, its lexical cache from 00720a, and
the private-input version selector from 00701a. The cache remains in existing
component 00720 and the selector follows the existing contract selector in
00702. No component registration or load-order change is needed.

## Behavior and artifacts

Existing helper arities select version 1. Explicit versions select the fixed
C6 contracts already exposed by PR584. No source pin or expected semantic
identity is renewed. Source snapshots retain regular-file/non-symlink,
exact-byte/hash, stable file identity/size/time and strict UTF-8 checks.
The actual emitter owner is still called between emitter file bookends.

Compiled program keys include the selected source contract, all selected
expected semantic identities, physical source and emitter identities, and the
v3 family where applicable. Compilation still calls the actual emitter plan
compiler and derives function manifests and selected export shapes. Validation
checks the native version tag, family, fixed source and semantic identities,
function shapes and the governing manifest layout.

Byte, function and chunk comparisons authenticate the fixed compiler artifact
and its serialized manifest. They are not user-input work allowances. Existing
metric events remain observations; they do not admit inputs or enforce budgets.

The one-entry cache retains its lexical unforgeable token. Source and emitter
inputs are authenticated before every lookup. Cache hits reuse only the
compiled compiler program; request provenance is rebound. User ingress,
builder/verifier execution and sealing remain fresh. Authentication or compiler
failure does not return a different version or substitute a cached status.

## Conformance evidence

`tools/c6_source_binding_test.clj` first runs the existing fixed-contract
controls (3 tests / 49 assertions), then adds 10 tests / 55 assertions. Actual
code covers version-specific keys/validation, cache identity and reuse, native
version refusal before source resolution or foreign callbacks, snapshot file
I/O/SHA-256/UTF-8 checks, symlink/missing-file refusal, and emitter file
bookends. It checks request rebinding, a warm-cache input failure and a
version-switch compilation failure without cross-version fallback. A compiled
binding for the wrong version is rejected before cache publication.

Input-owner and compiler callbacks in cache controls, fixture source contracts,
the emitter owner and diagnostic transports are declared oracles. Positive
fixtures are synthetic. The emitter bookend controls use actual temporary
files but do not invoke the actual emitter owner. No real compiler, fresh Core
product, Reader, Gravity replay, source authentication, controller or broker
operation runs; Bootstrap stays unloaded.

Affected C11 artifact controls passed 6 tests / 58 assertions plus their
relative policy controls (4 / 54). The language boundary passed 6 / 11;
documents (240 documents, 19 phase indexes), full-language roadmap and diff
checks passed. Git source readback confirms the restored binding functions.
Prerequisite PR590 CI40926933502 passed seven routine jobs with two optional
skips; this does not establish the new C6 increment's CI or acceptance.

## Remaining acceptance requirements

Versioned Reader binding/envelope and metadata projection, private ingress,
fresh construction/receipt production and public Core report arities remain
required. The C11 lexical authentication/builder path and public replay also
remain incomplete. A private source-binding mechanism is not public Core or
MIR authentication evidence. Independent source review, current-main
composition and applicable successful CI are required before integration.

Existing PR574 review/integration disposition, PR453 original-v2 runtime and
independent result evidence, PR446's unknown registration, denied messages and
operation-specific runtime holds are unchanged. No retry or replacement of
those operations occurs. No PR552 completion, OWN-06 closure, v3 product
acceptance, release, backend or self-hosting credit is claimed.

## Current-main composition

The tested current-main composition is the clean normal merge
`b3401ea495ba8f44bb7ca4b89de63fb29e357fc4`, tree
`2e40ffd481479cb95e70363de6239c4cd652dff6`, whose main parent is
`2c469ab08b1eebe8ae59bb249dc325e4a6be822f`, tree
`31c162d7f9233a9cd2497a554c943bbeccf21d48`. Its current-main delta is exactly
six paths (three C6 components, two focused data harnesses, and this report),
with 430 insertions and 51 deletions. The increment exposes only the v1/v2/v3
source-contract DATA and versioned binding/cache mechanism; source identity
failure remains fail-closed, and no live source authentication, fresh Core or
Reader construction, or C11 admission is claimed.

The earlier five-file composition onto
`32fe90b1e6a41df5a4083d0abe3917e0cb2643e1` is historical preparation context,
not the current base or acceptance identity. Historical dependency and PR-status
observations above remain history.

The affected C11 data harness initially failed to compile because it omitted
the native view helper now called by actual carrier validation. The harness
now loads that exact helper from its owner instead of substituting a stub.
The C6 controls passed 3/49 and 10/55; affected C11 artifact/policy controls
passed 6/58 and 4/54. Boundary, docs, roadmap, governance, pinned clj-kondo and
whitespace checks passed. These controls retain their declared synthetic
inputs and diagnostic/compiler oracles; genuine source authentication,
fresh Core, Reader execution and Gravity replay are not established.
