# Ownership: persistent seed data and predicates

## Boundary and contracts

D1, D2, D3, D8, D9, L10, SAFE2, SAFE3, SAFE4, SAFE5, C1, C6-C11, C15,
BOOT8 and TEST2 govern this increment. The base is main
3a4d5ea11c946bd374dfdfa09feaa1d78aa6c582. Nine existing native seed algorithms
move from components 00684, 00685 and 00694 into gravity.ownership:
persistent-ownership, forwarded-value-node-ids, propagate-managed-ownership,
shared-owner-domain-id, canonical-owner-id, persistent-aliasing-valid?,
persistent-ownership-expected-keys, persistent-ownership-schema-valid? and
expected-managed-node-ids.

The public Vars declare exact argument lists, artifact inputs/outputs,
diagnostic responsibility, dependency direction and admission:none. Only
clojure.set is imported. Explicit operations retain the existing digest,
forwarding, owner reconstruction and schema dependencies. Bootstrap wrappers
pass Vars and retain the original public arities and one-to-two argument schema
dispatch. The common-key constant remains with its existing seed owner.

These algorithms construct persistent immutable records, propagate managed
provider fields in existing node order, reconstruct owner-domain identity and
check alias/schema data. They introduce no lifetime rule, provider grant or
authority. Existing callers retain C9-MUT-ALIAS, C9-TRANSFER and C6-VERIFY
diagnostics, including source enrichment. No diagnostic construction moves.
Stored ownership facts retain their historical prospective-subject fallback;
they do not become authority. Extra fields still override construction defaults;
construction is not admission. The schema and alias predicates remain separate.

## Actual development evidence

The before producer runs at clean main 3a4d5ea1 in the original analysis
checkout. The after producer runs with working extraction over that same base,
before final registration/tests/documentation. These are development source
observations, not committed-head production or authority bookends.

Both actual SH-10 accepted and rejected fixture spellings compile. The current
Gravity C9 engine executes their normalized requests: many immutable borrows
are accepted and use after move is rejected with C9-USE-AFTER-MOVE. No genuine
source/Core/C8 admission or native execution is attempted. The Gravity engine
continues to own those transition semantics; the extracted native predicates
do not acquire them through this test.

A separately identified retained SH-12 Gravity fixture record supplies five
node/source records for the native data exercise. An explicit view adds the
existing persistent-ownership construction and operation-specific fields.
The view is data, not accepted Core or an authenticated ownership artifact.
Its inherited sh12:branch-join:checked-core identity and proof/status labels
are fixture data. The node set contains no function node, so this view's owner
lookups remain not-applicable; dedicated data tests cover reconstruction,
digest preimage and fallback precedence. No provenance is manufactured.

Thirty-six complete and thirty-six captured text pairs match, including full
compiled fixture plans, source text/hash records, accepted and rejected requests
and results, retained input records, native views, normalized ownership records,
forwarding/owner data and alias/schema refusals. All recorded source text hashes
and declared fixture plan IDs recompute through the existing protocol. Mutating
the view's sharing policy fails the alias predicate; adding a forged ownership
key fails the schema predicate. Original source paths and origin/span records
remain intact. These native-data refusals are boolean predicates; the existing
caller continues to own diagnostic selection and throwing.

The conservation check compares all nine moved bodies with main 3a4, including
the original schema second arity, after explicit operation substitution. Two
unused local bindings are renamed with an underscore while preserving their
lookup/evaluation order. Anonymous reader-generated symbol numbers are
normalized for structural comparison only. Every unmoved form in the three
components is conserved. Runtime identity encoding is unchanged.

## Tests and retained nonpasses

gravity.ownership.pipeline-test covers actual paired normalized Gravity
ownership execution, declared plan identity, persistent construction, direct
and transitive managed forwarding, order dependence, schema/alias tampering,
owner-domain digest/fallback precedence, extra-field precedence, late Var
interposition, one-to-two arity dispatch and host lookup order. Explicit native
test data and mocks confer no authority. Exact EDN/JSON registration associates
gravity.ownership/sh-ownership/SH-10 with its actual Bootstrap consumer and
standalone test. Existing primary component counts and leaf catalog remain
unchanged. Ownership tests reject forged/missing registration records.

Temporary tooling nonpasses remain retained. The first producer assertion read
the diagnostic id field instead of the existing rule field; the actual Gravity
negative result was rejected. An extraction helper's String.indexOf call failed
before project writes; an inadvertently continued observation still used the
unmodified source and is labeled unmodified-after, not credited as extraction
evidence. A missing require caused compiler startup failure before any changed
Gravity program ran. A registration script delimiter error had written only
contract formatting and the EDN association; the remaining forms were parsed
before execution and completed without replaying the already applied mutation.
The first conservation comparison failed on reader-generated anonymous symbol
numbers, and the first interposition test accidentally replaced the function
whose digest callback it intended to observe. Corrected checks preserve the
same implementation bodies. Logs and Clojure reports remain in /tmp; no denied
semantic/native action, source-authority pin, CI job or external download is
retried or waived.

Focused checks, contextual lint, docs/roadmap/governance, independent exact-head
review, successful candidate CI and a clean current-main integration preflight
are required before integration. This slice does not waive the failed effects
or diagnostics candidates or the separate unavailable Source-Core readiness.

## Current-main composition and checks

Source b14f6af7ca9eabbb6b6e5d526473226afeec5fbe, tree
5bf801e817906f713f3d1beda5e7b72e49bc6f2c, receives conditional acceptance
from both independent reviewers. Focused tests pass 5 / 127; ownership
registration passes 23 / 854; language passes 6 / 11; the C9 leaf regression
passes 4 / 94 with unchanged catalog count 53 and catalog hash
c29a49033c0514f4e84d5de019939a519b456134a7623b4b25404ec8274fada7.
All seven changed Clojure paths pass contextual lint: 3490 unchanged baseline
warnings and zero blockers. Docs validate 240 / 19; roadmap and governance pass.

Main advances through the independently integrated runtime slice to
6d1ed9e58fdcf4c8668c49fc43cbdf26b20184cb, tree
29864483dc74caf5a6bbe00995724fa1f31cd1fc. A normal merge composes that actual
main revision. Four genuine conflicts in Bootstrap requires and shared
registration files are resolved by retaining both stages, preserving the
runtime helper at index 8 and appending ownership at index 9. Runtime source,
tests and evidence remain identical to main; the nine ownership algorithms
and their tests remain identical to b14. Only their common association and
require surfaces compose. No CI runner, workflow, watchdog, cache, source
authority pin or diagnostic policy changes.

Fresh working-composition tests pass both stage suites together: 10 / 215.
Registration passes 24 / 871 and language passes 6 / 11. These execute the
current Gravity normalized ownership kernel through the integrated runtime
wrappers without authenticated source/Core composition. Original paired
evidence keeps its earlier production context. The final composition requires
its own independent review, contextual lint against actual main, successful CI
and clean integration preflight; acceptance of b14 alone is insufficient.

## Composition with main 7019 and fresh development evidence

The earlier composition cd606ded98854da99992a20be69f3e3db9783981, tree
e4ab2be1d0b3da03e9c1248c079c366667984be8, remains nonpassing: its own
CI 41220765999 recorded Macro and Resolution watchdog exits 124 at 600
seconds. Those failures do not identify an active test or cause and are not
retried, repaired, or credited by this composition. The earlier cancelled run
41220457191 remains cancelled evidence. No actor or cancellation cause is
inferred.

A normal merge now incorporates actual main
7019c174703d534e8da0341587a172a9214b9f20, tree
749fd2729a7c9782c08786c8485f067274f5ecfb. Its three native ownership
components are unchanged from the previous integration base. Five genuine
conflicts are resolved by reconstructing complete current-main Bootstrap,
registration and namespace-map files and adding only the existing ownership
association, require, test and row. All incoming main code is retained. Nine
moved bodies, wrappers, ports and focused tests remain conserved; the quoted
contract and map now also name the applicable SAFE2 memory-safety boundary.
CI policy, watchdogs, runners, caches and authority pins remain exactly main's.

Before composition, an ordinary development producer runs at clean main 7019.
The same producer runs on the working composition. Both spellings compile the
complete accepted and rejected fixtures and execute normalized C9 requests:
immutable sharing is accepted and use after move yields C9-USE-AFTER-MOVE.
Thirty-six complete and serialized record pairs match. All captured source
texts match actual files, their hashes recompute, and both fixture plan IDs
recompute without normalization. The native view remains unauthenticated data.
Fresh ownership, runtime, optimization, lowering and normalized C9 regressions
pass 27 tests / 996 assertions; registration passes 33 / 1012 and the language
boundary passes 6 / 11.

The complete C9 engine plan is separately captured: 101 functions, source SHA
717358b45c98dcb5d8c86afea58a3c0a85a34ea66bb9e2dc72c2fd5b8c77de43,
plan ID sha256:367d262bb4d51904c26ebabee1151155e8f8ef750147ab8c99c8f079d3b40496.
Execution uses the existing fixed Stage2 compiler seed
bootstrap/gravity/p15_s23/compiler.gravity, SHA
57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527,
and native Clojure gravity.runtime plus component 00593. This does not execute
main's changed Gravity emitter.gravity or runtime.gravity, authenticate
source/Core inputs, renew migration evidence or claim full C9 conformance.
Captured compiler provenance records their actual hashes and the inherited
compiler stamps; no stamp or identity is fabricated.

The first new control launch supplied a script path to the self-hosting test
runner, which rejected its arguments before any fixture ran. A corrected
ordinary Clojure script invocation produced the evidence above. A temporary
parse-check command also had an extra closing delimiter after fully reading
both evidence scripts; neither script was evaluated by that command. These
nonpasses remain recorded separately from successful execution.

Evidence remains in /tmp/uniform-ownership-data-evidence and the corresponding
accounting-main command logs. Fresh contextual lint, docs/roadmap/governance,
exact independent review, this composed head's own CI and clean current-main
integration preflight remain required. Neither old acceptance nor a later
green run supplies old-head or old-watchdog credit.

## Package-main composition

Package PR724 advances actual main to
6dcba8de85797f4f19b865db89e44ef4bd0c5dba, tree
cb42bf633ac8db611758abda10f4e2e55a7d13da. A normal merge composes that main
with accepted source 92f86314bac1e3bac027e71a32787ea6699e1721.
Three registration conflicts are reconstructed from complete main files,
retaining Package and appending the existing Ownership association and test.
The namespace map and Bootstrap require also retain their complete main inputs.
All Package code, tests and evidence remain byte-identical to main; Ownership
algorithms, wrappers, ports, contract and focused tests remain identical to
92f86314. No CI policy or authority input changes.

Fresh Ownership/Package/Runtime/Lowering tests pass 19 / 353. Registration passes
34 / 1030 and the language boundary passes 6 / 11. Nine moved bodies and all
unmoved component forms conserve. Fresh paired producer and compiler provenance
records use the package-main prefix and the same fixed-seed/native boundary;
the original clean-7019 control remains explicitly historical evidence.

Both reviewers accepted 92f86314 conditionally, but that tuple is now stale for
integration. Its own CI 41255462495 remains a separate observation. The new
composition requires its own exact reviews, lint, CI and clean current-main
preflight; none of those gates is conferred by earlier acceptance or failures.

## Unsupported boundaries

This is partial temporary Clojure organization. Canonical C9 check/verify,
general ownership analysis, mutable borrowing/lifetimes, regions, arena
generations, linear resources, task/FFI transfer, runtime borrow checks,
provider grants, public input/callback authentication, full safety or MIR
admission, native/backend execution, performance certification, migration,
release, self-hosting and seed retirement are not supplied by this namespace.
Gravity remains the semantic owner. Existing compiler legality remains a
compile-time requirement, independent of runtime/provider behavior.
