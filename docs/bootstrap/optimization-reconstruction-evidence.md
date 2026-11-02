# Optimization: native integer-equality reconstruction

## Boundary and governing contracts

D1, D2, D3, D6, D8, D9, C1, C11, C13, C15, SAFE15, PERF1, PERF2,
PERF10, BOOT8 and TEST2 govern this increment. The source base is main
6d1ed9e58fdcf4c8668c49fc43cbdf26b20184cb. The existing six public algorithms
and their private helpers move from the Bootstrap C13 comparison component
into gravity.optimization. Public APIs are selection, rewrite, restore,
output-id, extend-evidence and translated-record. Their metadata declares exact
arguments, artifact inputs/outputs, diagnostics, dependency direction and
admission:none.

The stage selects one equality of two representable i64 constants, rewrites
its operation and use/edge rows, reconstructs the original rows, preserves the
existing output-identity preimage and assembles the existing translation data.
It imports no namespace. Explicit digest/path-neutral operations and
selection/rewrite/restore/identity Vars preserve existing late interposition.
gravity.bootstrap-c13-comparison remains the compatibility consumer with its
original namespace, six public arities and load-order resource boundary.
The stage does not discover Bootstrap Vars or import the orchestrator.

Input is native MIR-shaped data, not authenticated C11 ingress. Passed fact
labels, replay-result labels and supplied callbacks are data/dependencies, not
authority. Existing holders retain genuine input admission, C11 verification,
artifact sealing and diagnostic publication. Reconstruction/output-identity
mismatches retain the real C13-VERIFY refusal record. Nil selection/restoration
retains its historical meaning; no new diagnostic is invented.

## Removed accounting and retained legality

The repository's project-accounting removal applies to this moved code. Generic
256-operation/definition/value/use and 65536-edge quotas, the 256-character
identifier quota, the aggregate input quotas and fixed 65535 index ceilings
are deleted. No constant is raised and no replacement allowance is added.
The old private quota predicate is replaced by collection-shape checks only.

Independently meaningful conditions remain: nonempty string identifiers,
representable signed-i64 constants, exact two operands and two reconstruction
edges/uses, unique operation/result identities, actual collection-relative
indices, coherent source/fact joins, pure effects/capabilities, target/profile
context and the existing one-block/four-block data family. The two-addition
row condition corresponds to the equality's two operands, not a work limit.
Forged or out-of-range reconstruction indices still fail against actual rows.
No safety check, proof obligation, runtime check or policy check is elided.

## Actual development evidence

The before producer executes at clean main 6d1ed9e5 in the original imports
checkout. The after producer executes with working extraction, before final
registration/tests/docs. The actual current Gravity C13 source compiles and
executes on the existing development comparison data for equal, unequal,
signed-i64 endpoint and repeated-operand cases. Each result is accepted and
equals the native expected record. Altering the output identity makes both
the actual Gravity program and native record builder reject with C13-VERIFY.
Each rewritten MIR reconstructs its exact input; source maps remain unchanged.

All 37 complete and captured-text before/after pairs match: inputs, evidence,
selections, rewritten/reconstructed MIR, actual Gravity results, native expected
records, both real refusals and the compiled source/plan record. Source text
hash and declared plan ID recompute through the existing canonical protocol.
No identity encoding changes. The immutable-base comparison accounts only for
the explicit callback ports and enumerated quota deletions, and checks the
entire stage source and compatibility wrappers against that transformation.

The existing development data contains supplied passed labels and an inherited
/development/comparison.gravity span. That path is fixture data, not an actual
source compiler ingress. The actual compiled file is
bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity.
No paired .qst compiler source, genuine C11 products, backend output, native
execution or source authority bookend is claimed. These are working-source
observations, not execution at a later committed head.

## Tests and registration

gravity.optimization.pipeline-test covers actual current Gravity/native parity,
four equality cases, output-identity refusal, exact reconstruction, unchanged
source/runtime-check maps, invalid effect/type/origin/i64/arity premises,
forged delta shape and indices, and Var interposition. Explicit native data
with 260 operations/definitions/values and a 300-character identifier verifies
removal of the former quotas while retaining exact reconstruction and source
maps. These larger cases exercise the native data helper only; they establish
neither general C13 support nor new Gravity source/admission conformance.

EDN/JSON registration associates gravity.optimization/sh-optimization/SH-16
with the actual gravity.bootstrap-c13-comparison consumer and standalone test.
The primary component counts and existing leaf catalog remain unchanged.
Adversarial association checks reject missing or forged source, owner,
consumer, namespace, test and registration data.

The first focused test run has a test expectation nonpass: one endpoint case
omitted its fourth expected-result value. The actual program and native helper
both returned false correctly. The corrected test adds that expected false;
no implementation change or diagnostic waiver is involved. The original log
is retained at /tmp/uniform-optimization-reconstruction-focused-expectation-nonpass.log.
An adapted registration edit initially gave the retained runtime row a duplicate
mir-stage-path key, so the ownership test namespace could not load. The key is
corrected to runtime-stage-path; the actual registration data is unchanged.
The initial load/parse nonpass logs are retained separately.
Initial contextual lint reports two unresolved-namespace warnings at the
literal compatibility Var references and test calls. Explicit namespace aliases
name the already loaded compatibility/Bootstrap namespaces, preserving the
same Vars without importing Bootstrap from the stage or creating a require
cycle. No warning configuration is changed. A direct standalone lint probe
reports the pre-existing component filename/namespace mismatch; the repository
contextual checker remains the governing lint route.
No denied/restricted source, native, Maven, CI job or authority operation is
retried, and no source-authority fingerprints are renewed.

Focused regressions, contextual lint, language/ownership registration,
docs/roadmap/governance, independent exact-head review, successful candidate CI
and clean current-main integration preflight remain required. Failed effects
and diagnostics candidates and separate main-CI nonpasses receive no waiver.

## Source checks and static correction

Initial source 1fad85bcb8cd2cb545ffdbca722f2c3062d66d3e, tree
aa2d328ef8bde0c44abd694b79718234a999c097, changes eight paths. Its CI run
41222039868 static job reports the same two unresolved namespace warnings as
the local checker. Tested merge 3b6d3a992937d4aef30e36ba7ec4818e236ca4af
has verified parents main 6d1ed9e5 plus source 1fad, with that exact source tree.
All retained static manifest entries verify; SHA256SUMS digest is
eb1f8dba63568fa7684afac7f4d2003f4174442be85779642cf63e1850a9b953.
That static nonpass is preserved, not waived or rerun.

The source correction adds explicit aliases only in the compatibility component
and test. The optimization stage body, schema, rule and identity protocol remain
unchanged. Corrected focused tests pass 5 / 110 and language passes 6 / 11.
The existing exact normalized C13/C14 equality kernel passes 1 / 332; the C13
leaf regression passes 4 / 71 with catalog count 53 and hash
c29a49033c0514f4e84d5de019939a519b456134a7623b4b25404ec8274fada7 unchanged.
Registration passes 24 / 871. Docs validate 240 / 19; roadmap and governance
pass. Initial corrected four-source-path contextual lint reports zero baseline
warnings and zero blockers, without configuration or waiver changes.

An actual producer with the working alias correction over 1fad again executes
all four current Gravity cases and their real C13-VERIFY refusals. This is a
working-tree observation, not clean or committed-head execution. All 37
complete/text pairs, source hash and declared plan ID verify again. The exact
immutable-base transformation check includes the literal aliases and verifies
both complete stage source and compatibility wrappers. Final source still
requires independent acceptance, its own CI and clean current-main preflight.

## Unsupported boundaries

This is partial temporary Clojure data machinery. Canonical optimize/verify,
general pass registry and arbitrary MIR optimization, public native ingress,
callback authentication, genuine C11 admission, proof-provider validation,
safety-check elision, backend/native execution, performance certification,
migration, release, self-hosting and seed retirement are not provided. Gravity
retains optimization semantics. Existing accepted/passed/status records confer
no additional authority, and this extraction is not performance evidence.
