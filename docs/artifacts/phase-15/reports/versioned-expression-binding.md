# Versioned expression-owner binding prerequisite

## Purpose, ownership and requirements

The preserved fresh C6 constructor requests an explicit expression-owner
version, but the PR597 baseline exposes only the legacy three-argument API.
This finite Native increment restores that genuine owner dependency before
public Core construction/report wiring. OWN-06, C6 and BOOT8 require actual
source/implementation provenance and keep program binding separate from Core
or MIR acceptance.

The baseline is 6a5977cd34d8db438226424fe38618ebe9989498. Source is restored
from preserved Native f66d9ff4554815b89ee7d51ee73b7961f0c33a44: components
00580 and 00582, with 00581a placed before 00581 in the existing 00581 file.
No component registration or source pin changes. Existing typed-lowering
functions and the legacy governed-source route are retained.

The earlier expression component now establishes the shared contract/header
aliases. Remove the two redundant later requires in 00702 and 00723; their
function bodies stay unchanged. Initial CI found those duplicate requires;
the original failure remains historical evidence.

The historical B51 parent draft lists the shared files; its recorded current
disposition has no production writer on this expression path. Its existing
source delegation targets separate checker-identity files. This proposal
transfers no B51 ownership or authority. Explicit expression version 2 refuses
a governed-source session; the legacy route remains unchanged. No held source,
controller or runtime operation is invoked.

## Behavior and artifacts

The new fourth argument accepts native expression versions 1 or 2. Version 1
delegates to the existing route. Expression version 2 remains distinct from
C6/C11 source-interface version 3; it authenticates the fixed expression
program already recorded in the source contract.

Selected source must be a regular non-symlink file with exact bytes/hash and
stable file metadata. Compilation is bracketed by matching source snapshots.
The emitter is native carrier DATA and must join the fixed actual emitter
owner. Complete program checks bind source identity, semantic/artifact/function
hashes, count/names, instruction summary, every required export and declared
compiler labels, with an independently recomputable plan ID. The returned
binding records `:invoked? false`: binding is not expression execution.

Versioned record predicates preflight native headers and metadata before
lookup. Plan matching inspects compiler metadata only; unconsumed function
payloads remain inert and acquire no plan-admission or safety authority.
Constructed plans propagate the selected version, function hash and count only
for the native Long version-2 tag. Legacy entrypoints and metadata stay on their
existing contract. Byte/function counts authenticate fixed program identity;
they are not user-input work allowances.

## Conformance evidence and limits

`tools/expression_binding_test.clj` runs relative-policy controls (4 tests /
54 assertions), then 7 tests / 59 assertions for this increment. Native
carrier/header checks, source file I/O/SHA-256, complete identity validation,
metadata predicates and construction metadata routing are actual code.
Compiler, compatibility source reader, emitter owner, assembler and constructor
identifier are declared oracles. Fixture contracts and programs are synthetic;
diagnostic transport is synthetic. No real expression program, actual source
authentication, actual assembler, fresh Core or Gravity replay runs. Bootstrap
stays unloaded.

Controls cover exact selected binding, false execution claim, version/host/
governed-context refusal before source reads, changed source before and during
compilation, symlink refusal, complete program/plan-ID tampering, native
metadata refusal with zero callbacks, unconsumed plan payload, unchanged legacy
routing, and native-version metadata propagation. Successful metadata matching
of a fixture with inert foreign function payload explicitly demonstrates that
the predicate is not executable-plan admission.

## Remaining completion requirements

Private ingress, fresh C6 construction/receipt production and public Core report
arities remain required, then C11 lexical authentication/builder and genuine
public replay. The source-level expression arity dependency is implemented by
this proposal; adoption and actual successful product execution are separate.
Independent exact-source review, current-main composition and applicable CI
remain required before coordinator integration, including no replay of identical
or tree-equivalent prerequisites.

PR453 original-v2 current-consumer evidence, PR574's unresolved final independent
disposition, PR446's unknown registration and existing denied/runtime holds are
unchanged. No retry or replacement occurs. No PR552 completion, OWN-06 closure,
v3 product acceptance, backend, release or self-hosting credit is claimed.
