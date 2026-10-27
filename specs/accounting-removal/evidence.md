# Accounting removal evidence

## Private-return path admission after recovery

Accepted implementation: `3e94a6318bc1d598a109cb174d0fdf02944538b7` / tree
`ff3335194403ec54972babed997cb4fe616fc027`, based on main `12407ca1` / tree
`2ac12a3b`. The three actual return-witness owners had identical 128-node
admission. One coordinated change removes that branch, validates queued
keyword/nonempty-string IDs before lookup/hash and canonical stored IDs before
equality, and propagates per-path ancestor sets through both branch and
sequence enqueue paths. The diagnostic `:private-self-return-cycle` represents
a repeated canonical ID, not a replacement size allowance. Shared DAG leaves
retain both witnesses. Conditions, normal prefixes, Never, pending positions,
return conflicts, full paths/control records and all work formulas remain.

The old-source baseline completed 9 observations: every owner accepted full
1/128-node paths, then refused 129 with `:private-self-return-path-bound` at
33,925 work units, below the existing 65,536 allowance. Natural exit 0 /
18,457 ms / actual 2 GiB; complete 1,170-byte stdout, empty stderr, exact clean
bookends and complete cleanup. Receipt SHA-256:
`0636eb04b24f41a7b0b24115c3ccf6d753fd98d78db9c54d7b44b0c175595cdd`.

Actual candidate validation selected four new kernels, six affected regressions
and six boundary vars: 16 tests / 494 assertions, zero failures/errors, natural
exit 0 / 30,574 ms / actual 2 GiB. Complete capture was 2,129-byte stdout and
empty stderr, exact clean source bookends, child gone and cleanup complete.
Receipt SHA-256:
`49cd136f1e521b3cb01d487e8124b396be552882a1269594226032544141d512`.

The new cases exercise all three actual functions at path lengths 1/128/129/130,
self/two-node cycles, missing/mismatched canonical nodes and malformed queued
IDs with zero opaque hash/equality callbacks. Shared-leaf paths remain distinct;
unknown/Never/prefix/conflicting returns preserve their outcomes. The old
integer-ID 131-node refusal fixture is replaced with a complete normalized-ID
positive and equally deep malformed endpoint negative. Existing small cases
remain. Effects and both emitter context constructors accept their ordinary
private proofs, then reject altered witness paths or control records with
unchanged return types and coherently rebuilt proof-bearing ledger rows.
No arbitrary foreign-container or new source-authentication theorem is claimed.

| Accepted path | SHA-256 |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | `6bf0eb72e0efe43593a9fea32a751bae5e820f6335cb0649dd7636395f1d3c86` |
| `bootstrap/gravity/src/gravity/effects.gravity` | `bfccf21c8ce5bbb2214a0af627dfb9372bb2f6843b7eb8c89891073090a706ce` |
| `bootstrap/gravity/p15_s23/emitter.gravity` | `19dce9a30020688bc4b83327113dc36ddc7b05e24038be75fa54e2ad73e42d0f` |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | `0e8ff033eeb1c4ea485da0b762b73aa7cd5c7e6c75ac94cb62b70c358c282009` |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_recursive_function_type_test.clj` | `2d8bc000ff5cb28c8fb564d28081eb5a8ccf96a3bdf6212965114a0994fcf0fd` |

Independent review inspected source, driver, actual counts/capture/receipts and
their hashes; the reviewer did not execute a duplicate batch. Cheap checks
included seven compiler-free fixture assertions and reading all five changed
source/test files without loading Bootstrap. An initial inline fixture-control
command ended with a command-reader EOF; the corrected saved control passed.
It is not semantic execution or a candidate acceptance failure.

Normal reconciliation `66d2422b` / tree `fabe1027` preserves all 16 incoming CI
PR488 paths from main `bf7893da`. All Gravity, selected semantic tests and
dependency configuration are identical to accepted `3e94a631`. Darwin native
publication changes are a namespace-load dependency but their native probes
execute only when opening publication targets, outside this selected route.
CI/catalog/ownership additions do not replace explicit test selection.
Independent consumed-input review accepts retention of the original semantic
receipt, subject to fresh cold Bootstrap load and current-tree repository gates.
That reconciliation is not fresh execution of 16/494; repository-scanning
boundary tests are renewed separately. Current PR checks retain their ordinary
authority; no CI policy is changed by this accounting increment.

The recovery history showed the prior turn completed after PR497 and no
intervening admitted accounting command. All preserved accounting checkouts
matched recorded clean heads; the process snapshot found no JVM. No uncertain
operation was repeated and no held Native/Set registration or publication was
retried. Final gates and integration belong to the associated PR. ACC-02/03
remain closed; the other eleven original criteria, including full ACC-07/13,
remain open. Genuine authentication, language-runtime execution, C12 and product
release are unrun. Physical broker, heap, watchdog and platform limits survive.

Fresh composed validation on `0453a469` / tree `44dd1637` passed cold Bootstrap
loading followed by 11 tests / 23 assertions, actual LOC validation for 2,035
files, 240 documents / 19 indexes and roadmap. Exact-base preflight passed on
main `bf7893da`; natural exit 0 / 9,162 ms / actual 2 GiB, complete 637-byte
stdout, empty stderr, exact clean bookends and cleanup. Receipt SHA-256:
`df6d900ace441d843551d2523940d376213855e290736a3272528f091b1418b1`.

The initial [PR499 CI static job](https://github.com/burn2delete/uniform/actions/runs/40546272191/job/122337633073)
is NONPASS: changed-file clj-kondo returned zero errors / 76 warnings. One new
unused test binding was removed in `fb02ad96` without changing an initializer,
assertion or production code. Independent review accepted original semantic
evidence reuse for that correction. The remaining 75 pre-existing warnings
comprise 64 unused bindings, seven private helpers consumed dynamically by
other tests, two redundant lets, one unused import and one runtime-only
namespace require. Removing those helpers merely to quiet static analysis would
break actual consumers. The existing CI owner retains its policy; no accounting
worker suppresses or bypasses the check. The failed run remains failed, and
the draft cannot be integrated until the normal check has a reviewed resolution.

CI-owner PR500 supplies that separate reviewed resolution at main `a43facc7` /
tree `981e6b38`. The exact first-parent baseline `bf7893da` and tested merge
`764fe0f8` both produce 75 warnings / zero errors; all warnings match unchanged
mapped spans under the same pinned clj-kondo. The historical pre-fix `0453a469`
still blocks exactly its new unused binding (76 current / 75 baseline). New
warnings and every current error block; changed or incomplete spans fail closed.
The CI owner reports independently reviewed 9 tests / 70 assertions and seven
routine main jobs passing; this is attributed CI evidence, not accounting,
source-Core or reviewer execution credit. Explicit unavailable Core readiness
remains nonpassing on main.

Normal reconciliation `0ed0d800` / tree `3bea5fd9` preserves exactly four
incoming paths: `docs/ci.md` and `tools/ci/{lint,run,run_test}.clj`. Independent
consumed-input review confirms no change to the selected compiler, Bootstrap
load or manual guarded runner routes. Original semantic 16/494 remains at
`3e94a631`; cold-load/composed 11/23 remains at `0453a469`. No semantic or cold
load repetition is implied by this CI-only merge. Fresh repository gates,
exact preflight and automatic current-head CI precede normal integration.

## Vector-origin binding-chain admission

The accepted implementation is `903e330ed28dd0fc5e56380d144d48bc67138944`, tree
`54c49d1c97803493a3f942aabb5016b94bbfb99d`, based on main `4fa8575b` / tree
`d85107f9f443ee60fd87c8863c200bf68a3670c1`. Exactly two implementation/test paths
change. No Native, Reader, MIR, runner or catalog source is changed.

The old-source diagnostic completed naturally with exit 0 in 14,128 ms at
actual 2 GiB: full canonical 1/128-link origins passed their closed consumer;
the producer refused a valid 129-link chain, and actual successor derivation
rejected it with `:bounded-successor-range-proof-required`. The rejected producer
does not supply independent 129-link consumer evidence. Capture was complete
(346-byte stdout, empty stderr), exact clean bookends and cleanup passed.
Receipt SHA-256:
`3fea56edede016d7d81373e994ca0bc22c6617e1476aceee723d5c226a7ff685`.

The candidate replaces producer recursion with an iterative canonical-ID walk,
rejects repeated IDs, and reverses collected links at constructor termination
to preserve inner-to-outer proof order. The consumer walks that exact sequence
independently, rejects repeated reference IDs and reconstructs the constructor
endpoint with canonical immutable-record and type equality. Both production
callers and the direct test migrate to the depth-free arity. Source-form
validation remains the producer's existing responsibility; the consumer is not
claimed to establish a new source-authentication theorem.

Actual candidate execution selected three new chain kernels, seven affected
regressions and six language-boundary vars: 16 tests / 460 assertions, zero
failures/errors, natural exit 0 / 15,700 ms / actual 2 GiB. All 1/128/129/130-link
origins retain complete ordered records; actual 129-link successor and nth/get
derivations and fresh proof consumers pass. Late source/mutability/type/node
changes, missing/duplicate bindings, cycles, wrong endpoints, reordered/missing/
orphan witnesses and malformed entry IDs reject. The malformed-ID control
observes zero hash callbacks; this is not arbitrary-container ingress authority.
Capture was complete (2,019-byte stdout, empty stderr), source bookends were
exact and clean, and cleanup completed with the child gone. Receipt SHA-256:
`27b2c8088719d005788712af3528d6c883b6e8bfcf071982b55ca467f1d9344a`.

Independent review inspected the exact source, driver and retained records;
it did not execute a second semantic batch. Source hashes are:

| Path | SHA-256 |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | `a89c885244593d2359e8e93825a31951cf4aa2a6234e2a548f91f15c75286bad` |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | `12894ec7b266330c9fc3c0892067e41227d9ee4320244db59ddc1369a1d13923` |

Extent 1,024 and `:maximum-vector-extent`, the remaining type/collection/work
policies, structural projections and other ACC-13 mechanisms remain open.
Runtime checks and the prohibition on check elision are retained. Genuine
authenticated-source admission, language-runtime execution, C12 and product
release were unrun. The failed first form-reader command had an extra command
delimiter after reading all 1,085 source forms; the corrected form-reader
completed without Bootstrap execution. It is not semantic evidence or a
candidate failure. Final gates and normal integration are recorded by the
associated pull request; whole ACC-13 and accounting completion remain open.

Repository gates on documentation successor `82292a36` passed 11 tests / 23
assertions, actual LOC validation for 2,035 files, 240 documents / 19 indexes and
the roadmap; natural exit 0 / 2,573 ms, complete 456-byte stdout, empty stderr,
clean bookends and cleanup. Receipt SHA-256:
`19ecf603062138bfd52b1915f19a516a9e79cef616fd2429b47f3f461c60bab7`.

The initial integration preflight detected main advancing to Reader PR496 and
failed with `WORKTREE-CANDIDATE-BASE-MISMATCH`; that record is retained as
nonpassing. Normal reconciliation at `4ed9939b` / tree `5466a877` preserves all
six incoming paths from main `2766511b`. Its sole production delta changes C2
literal escape offsets. The selected compiler-plan route uses the unchanged
host-oracle form reader with authenticated records unbound; it does not invoke
that C2 literal-record producer. All Gravity source, the selected SH08 test and
dependency configuration are identical to tested `903e330e`. Original semantic
receipt provenance is retained; reconciliation is not a fresh semantic run.

## Observed baseline

2026-10-26 GitHub fetch: main
`176c4ee65bb3ceb545f51edfaa599902030a0cb7`, tree
`a887eae096998881ef3aef4fa18a6458ebb6ad20`. Isolated checkout initially clean.
Open PR list observed drafts 359, 370, 378, 383 and 421. The source baseline
is not the published accounting candidate and is not MIR421's validated tree.

Source inspection confirms live C7 inference options/pools and per-round debits,
Effects cumulative module pools, runtime cost/budget hooks, syntax fixed graph
and container maxima, host canonical-schema reservations/debits, and macro
expansion depth. Search is discovery evidence, not an exhaustive disposition
or proof of semantic removal. No production source was edited during this
initial source inspection.

## Attributed existing-owner evidence

PR359 owner reports published head
`4c5aac01190e52fe013909437987071826492d5e`, tree
`f26326954ebeb71bf807a7177257242e776f87eb`. Separate keyword guard commit
`5d003d736456b86e6085b9608acfee2c6245b4ef`, tree
`08943e9e78f5be69f198edb6e838019eaf6f53e0` is independently code-reviewed,
unpublished and untested at handoff. The writer retains C7/Effects/emitter and
owning tests through guard plus four Map/List consumer-field removals. No
ownership release yet. LR and PL remain unimplemented by that slice.

MIR owner reports PR421 head
`8b9dd46ea38eafba1d3d7a49d8c6ca6b6488563f`, tree
`ed9d118e18f5f1a3f04138603e282ade9fb70701`, C11 source SHA-256
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`
(567399 bytes). Genuine two-case source selection timed out at 120 seconds,
exit 124, without completed test counts. Helper/gates 36 tests/327 assertions,
harness controls 10/52 and controlled shared-codec probe 10/92 passed in their
reported scopes. They do not establish original/independent source replay,
current-main or public acceptance. Shared codec remains with Native owner.
No MIR source/compiler child or lease was launched by this accounting audit.

## Current acceptance boundaries

All ACC tasks remain open. Inventory is incomplete; exact Native/opt paths and
additional per-guard dispositions are pending confirmation. Parent confirmed no
identified active runtime/library writer and authorized precise preserved-state
takeover. The existing library accounting checkout was clean at
`2b533de0692859016207fb629cdd8a4b96e7c10b`, tree
`d90ca938bc76ce7e1b40fd2e165553d8234025dc`. No other checkout was changed.
PR359 confirms no overlap with its current runtime/test paths. Historical
candidate evidence remains scoped and conservatively invalidated.

## Discovery census

A local Clojure textual census on ledger commit
`57429a9ca2225ec829ed870f616f1eaac06e574e`, tree
`4ee6147fcbd5928605d4e2fe6edeea976874e3f9`, scanned 2585 tracked implementation,
test and tooling files. It grouped 4103 candidate definitions in 1186 files.
Every candidate is unclassified. Comments/history are included, and unnamed
ceilings/transitive callsites can be missed; these are not coverage numbers.
The census is a local discovery artifact, not a replacement status ledger.
Artifact SHA-256: `acfc975412cc78f3c9f2280002c1cf2a174842c0cdb5f182892429b2bbd665af`;
script SHA-256: `a92c9892829b8ac8b183cd7acff8a2bfa7de703f9320619338acfa0bfab9bb83`.

## Runtime module-read increment

Tested source commit `a722ba820d5fd4c362296752857c3c3592185f83`, tree
`8554ba879a6b7d562d459ca05dcbf643c8700a30`.
Runtime source SHA-256:
`840c2ce49c42c0e200b5ea170d1f6b59d6c1ee7d4463a4fc7d718c380fd1e99f`.
New test SHA-256:
`1b1894af4ef46cbdb7e08e393d657d3476a3db6d364e8739d033d00b8900950f`.

| Stage | Actual result | Scope and limits |
| --- | --- | --- |
| Structural document validation | Exit 0, 240 documents/19 indexes | Ledger baseline; structural only |
| Clojure form-read and whitespace checks | Passed | Runtime and both affected test files parse; no semantic acceptance |
| Actual-owner baseline | Natural exit 0, 8282 ms | Original traversal accepts ordinary valid input but refuses wide/deep valid and deep invalid read with work-limit; expected defect observations |
| Candidate semantic and module execution | Natural exit 0, 63949 ms, 10 tests/123 assertions, zero failures/errors | Three new helper tests, existing normalized module-plan execution selector and six language-boundary tests |
| Authenticated source compilation, public runtime admission, release | Unrun | Helper/normalized plan evidence cannot close these boundaries |
| C12 and original C7 publication operation | Unexecuted | Restrictions preserved |

Both executed stages used the existing Git-common normal broker, host exclusive
broker and canonical lock, with a 2 GiB child, 120-second watchdog and complete
65536-byte/character capture per stream. Both exact before/after commit/tree
bookends were clean and unchanged. All processes/leases completed; the brief
environment disconnect did not cause a duplicate run. An initial sandbox lock
open failure started no child; supported escalation then ran the baseline.

The candidate covers valid depth 130, width 65537, invalid reads beyond either
old limit, every function, preserved executable child positions, quote data,
and ten source/definition substitutions. The existing module fixture exercises
actual Gravity initialization, fresh instance value identity and semantic
rejections on normalized plans. Other runtime quotas remain and are not
declared accepted merely because their preexisting tests pass.

Baseline observation SHA-256:
`97c16b83aad445af93c892a78ee7c591a34358e1deb112784393bdae2fd150ea`;
baseline process receipt:
`254b0bec4413b065ea080effe5bb98d25e0494334f00bd3fc3969f0e1c1cc5c6`;
candidate process receipt:
`ba015e917c9a10d2c924998b50ffaa1d190cbedfedf2041c55844f5ee823562e`.
The assurance owner independently inspected this exact source/test delta and
retained receipt, verified the receipt hash, and reported no blocking finding
for the declared module-read increment. That review confirms preserved child
selection, iterative full traversal, source validation, quote boundaries and
updated caller/arity expectations. It is code and retained-evidence review,
not a second fresh execution or whole-source/public-admission authentication.
These hashes identify evidence and confer no authority themselves.

## Initial publication denial and supported reassessment

Push of `codex/accounting-completion` and draft PR creation to
`burn2delete/uniform` were denied before execution by automatic approval review.
The stated reason was external publication without explicit destination
authorization. No push or PR occurred, and no alternate tool, actor or retry was
used. The denial remains part of the operation history.

The user subsequently directly instructed that the completion tasks be merged
to main. Following that new evidence, remote branch and PR lookups confirmed
the denied operation had not executed. Independent complete outgoing-content
and PR-body inspection found no private material, and one historical-audit
wording correction was applied. One supported reassessment through the original
actor and git/gh route succeeded: [draft PR433](https://github.com/burn2delete/uniform/pull/433).
Initial published head `6e5cf69e125c24fe5aaf4977a6d076b199e1a7ac` matched fresh
remote branch and PR readback. No administrator bypass, actor substitution,
credential change or branch deletion occurred.

## Current-main reconciliation

Normal merges brought main `48f81c4bf2d7e7ecb01905949586253b797aaf98`, tree
`847cae797ac742d8a394b9c137966f9aae61b3ad`, into candidate
`31f9e87bf716e621138dc62cb686122319fd5563`, tree
`37bd9b2b68962fedd59596586b6886b0c55cf335`.
The only main changes were independently owned Emit workflow/projector and
acceptance documentation/tooling. Runtime source, affected tests, bootstrap
implementations, emitter, broker and process-runner inputs remained identical
to the original tested candidate. Reuse of the three unchanged semantic tests
is limited to that exact consumed input closure.

A fresh current-main affected integration run passed the existing normalized
module-literal execution selector, six language-boundary selectors and the
240-document/19-index validator: natural exit 0 in 17412 ms, 7 tests and 72
assertions, zero failures/errors. The same existing normal/exclusive brokers,
canonical lock, 2 GiB child, 120-second watchdog and 65536-byte/character capture
limits applied. Exact clean before/after bookends matched the above tuple;
stdout was complete at 1034 bytes, stderr empty, and cleanup completed. No
process or lease remains active. This renews affected normalized integration,
not authenticated source/public runtime/release acceptance.

Current-main process receipt SHA-256:
`da1520e70c11bf812666a84a003572185aadd2e00d6788b755eb566f5bdb1630`.

Independent static reconciliation review found no blocking change against
current main; the three runtime/test blobs remained identical to the accepted
original. Independent inspection of the fresh driver, receipt, stdout and
counters accepted the declared current-main scope and verified the receipt
hash. It also verified unchanged bootstrap, dependency and contract inputs
against the original semantic-test tuple. This is retained-evidence review,
not a second fresh execution. Main integration was pending at evidence
preparation and must be established by fresh remote merged-state and
reachability readback.

## PR359 ownership release

The prior writer published and explicitly released C7, Effects, emitter and
its owning tests at PR359 head `972a2126f7eeb149cce0a39a51d26453a84f39a4`, tree
`31bf8d40ff0af0461c750abe9f2bdcd7023f5861`, with no active process/lease or
pending publication. Its checkout remains an immutable audit copy. Accounting
successor ownership is acknowledged; no LR/PL implementation has started.
That release does not integrate PR359 or close ACC-02. The LR three-owner
version/invalidation proposal was read as design, preserving semantic extents,
full equality, complete validation and historical receipt invalidation.

## Finite runtime module-census removal

Earlier sections record historical snapshots. This increment starts from
current main `b45bdeebcac7caafce8e67ee6738ec3cf85dbc06`, tree
`ab7138e11e09e27876727604d26211f7fe923af2`. It is separate from unfinished
C7/Effects/emitter work in draft PR359 and does not give that draft acceptance.

A read-only normalized runtime baseline completed naturally in 15184 ms with
exact clean bookends, 443 bytes of complete stdout and empty stderr. A valid
7-root module passed census and closed execution. A 4100-root module was
rejected by both census and closed execution with
module-initialization-work-limit and an accounting charge of 82002. That
observation establishes the old refusal, not new-source acceptance.

The finite change removes census root/function charges, the shared cumulative
initializer pool, its budget argument and exported census work counts. The
closed executor, all direct test callers and the runtime function-arity census
use the new one-argument API. Each initializer still calls its unchanged helper
with its existing 65536 limit; this is explicitly temporary unresolved accounting,
not an increased or removed helper limit. Initializer-specific reservation,
duplicate/callback/type and own-limit tests remain. Census failures retain the
semantic initializer-reason when present while omitting accounting fields.

All manifest shape/version, ordered roots/initializers, complete function
bijection, source identity, type and validated constructor provenance checks
remain. A static review first caught the lost initializer-reason; that was
corrected before candidate execution. No authoritative source pin, target
runtime artifact or authenticated-source acceptance result was changed.

First executed candidate `2e602ac88418c857b1e488ce0141b50a8bf2bcb8`, tree
`14d5bf8d3e041bff8723e191cde6f563e9e2cd03`, is **NONPASS**: 12 tests,
580 passing assertions, four failures, zero errors, natural exit 1 in 25069 ms.
New duplicate-root controls exposed a pre-existing root guard error, unchanged
from the baseline: three branches returned false inside an invalidity predicate,
allowing duplicate names/bindings/definition-node identities. A duplicate binding
was rejected later for initializer order; a duplicate name reached initialization.
The valid large-module execution and four affected existing runtime kernels
passed. This failed receipt remains failed and is not summed with later results.

Correction `db2e925bd8ae7f5a1596e38768a41fbbc5409f1d`, tree
`45e2ac6410ffd83012a1cebd3f2fb920376700c2`, changes precisely those three
Boolean branches to reject duplicates. It adds coherent root/initializer binding
duplication, both cross-role node collisions and same-root node collision controls.
The existing value-node and same-root collision checks remain. Exact rejection
reasons and zero initialization are required; a positive observer first proves
that it sees actual initialization. This strengthens a genuine uniqueness guard.

The corrected bounded batch passed 12 tests/599 assertions, zero failures or
errors, natural exit 0 in 25545 ms. Actual selectors cover complete census and
closed execution of all 4100 roots, late invalid source/initializer/type/function/
duplicate/orphan records before initialization, preserved duplicate-initializer
diagnostics, literal/Keyword Set/String Set and indexed private runtime kernels,
two individually accepted initializers whose aggregate formerly exceeded the
pool, actual two-constructor execution, runtime arity inventory and six language
boundary tests. Independent static source and actual retained driver/counts/
stdout/receipt review accepted this finite scope; the reviewer did not perform a
second semantic execution. The separate document gate passed 240 documents and
19 phase indexes.

Both candidate runs used configured 2 GiB children, a 120-second watchdog,
65536-byte/character capture bounds, the existing common and host brokers and
canonical lock. Complete stdout was 2672 and 1693 bytes respectively, stderr
empty, exact source/tree bookends clean and cleanup complete. No process or
lease remains active. Current main readback remained the tested baseline.

At initial source publication, fresh test catalog/ownership and generic execution
restrictions for the new module-census namespace still required the verification
owner's metadata increment before main integration. It must remain fresh, with
no cache acceptance; generic execution stays unsupported until its actual
three-guard route is represented.
Authenticated-source/public runtime admission, restricted C12, release and whole
ACC-09/accounting acceptance remain unrun. Initializer helper accounting, runtime
function/source-shape maxima and other collection cost policies remain open.

| Evidence | SHA-256 |
| --- | --- |
| Baseline receipt | `7500cf1669be8a55840a187820f997ea9884ea60b55d6862b9bd5bde27d01dbd` |
| Failed first candidate receipt | `a8a3ed065cc5016d96db0d4088c56ed97b7a629747f1fd480e9334a53165937f` |
| Corrected candidate receipt | `e45362e3d2b5b29c6174031df23efd8929902b6a3243715ed7151bccbebde637` |
| Runtime source | `e1084872868b99eb8dc38a2e23f0b871864529eb107a68ab16dd3825867a13fe` |
| Module census test | `da7394ed6e440480a57d50bfe8ba2fc542aa54a9cb38236d933ccfa74885428c` |
| Runtime iteration test | `887d9b65ee46b71ab4317e5953813bca9f1813e7ba681930086217d863b6ba47` |

### Current-main metadata composition

Normal reconciliation imported main
`a1b991be499d50579a8b4366976c519ae8949224`, tree
`5b62819e511bfc7e6b5eba0527d8c9b5883c5572`.
The main delta contained only verification documentation and three audit-tool
paths; bootstrap/compiler/runtime, dependency and contract inputs were identical
to the tested source candidate at that reconciliation.

The verification owner handed off independently reviewed metadata commit
`88dc0f511b738d3f98f58e321d4cc6be6aa3539b`, tree
`f379df1799194f8cdb517d8babc02b20402f9006`, based on the immutable source
candidate. Normal import produced composition
`452c05ed28e074f3fd6c9f3342c60d25bd78ef89`, tree
`9a7172aef086270bb653fea242b062d88c48890a`. All five metadata/control blobs
match the handed-off commit exactly. Runtime, bootstrap Clojure implementation,
dependency inputs and both semantic test bodies remain identical to tested
`db2e925b`; its 12/599 receipt retains that original provenance.

The new namespace belongs to SH19/sh-runtime and requires fresh execution,
with no cache closure. It remains discoverable, while generic execution is
explicitly unsupported until a committed route represents all three required
resource guards. The existing shared denial policy applies before normalized
planning, single-process and every batch job, including forged support/resource
markers. No guarded route is fabricated, and no runtime wrapper/provider was
executed by these metadata controls.

On the exact clean composition, the planner suite, three injected runner controls
and six language tests passed: 69 tests/671 assertions, zero failures or errors,
natural exit 0 in 11777 ms. The child was configured at 256 MiB with a 60-second
watchdog; stdout was complete at 8291 bytes, stderr empty, bookends exact and
cleanup complete. An injected ordinary-launch positive accompanies zero-launch
denial controls. This is fresh metadata/catalog/control evidence, not another
runtime semantic execution or a combined test count. The document gate passed
240 documents/19 indexes and the full-language roadmap gate passed. Independent
source/import and actual retained-result review accepted the composition.

The finite composition subsequently completed exact-head preflight, public
readback and normal integration in [PR444](https://github.com/burn2delete/uniform/pull/444).
The final reviewed head was `6dcb4f64f82f8ffdab95fd5ef37ba92171122a97`, tree
`68609de1259c27a1f461abd8cd2d86ce1de0f949`. The merge commit is
`6b2e63b32f215b0c5f963fadfc018624837685b0`, with that exact full tree.
Fresh GitHub readback confirms merged state and the merge identity; ancestry
checking confirms the merge is included in current main. The remote branch is
retained. Census allowance/shared-pool removal, semantic uniqueness preservation
and the accompanying fresh/no-cache/generic-denial metadata are therefore
integrated finite acceptance, not pending integration.

The failed `2e602ac8` receipt remains NONPASS. Corrected runtime 12/599 evidence
retains its original `db2e925b` identity, and metadata 69/671 evidence retains
`452c05ed`; this status correction performs no semantic rerun and sums no counts.
It does not close all ACC-09. Initializer-specific allowances, Set construction/
conversion/domain/contains/insertion/equality policies, Vector/List policies,
runtime-shape allowances and exhaustive dispositions remain open at this merged
scope. Separate unmerged successors do not receive main acceptance here.
Authenticated/public runtime, C12, release and broader accounting boundaries
remain unchanged and open.

Composition receipt SHA-256:
`be932baae0aa66458e4548b85677948f9d024d90c9cd8ba444c910421346335e`.

## ACC-08 imported-module aggregate removal

This independent finite increment starts at main
`dcc9849df582ebb177560b41d702071e3481669a`, tree
`f5362aacb21cf4fcb9f6e4cedd5e74c5ac087db3`. Accounting owns the C7 engine and
its SH08 test file through the previously recorded explicit handoff. No held
runtime, Native, metadata or other owner's source is changed. L3's acyclic
module requirement, L5's signature/effect requirements and C7's fresh typed
artifact validation remain the semantic contracts.

The original C7 `sh08-im-work-check` sums invocation-local memo/active usage
against fixed producer, dependency-depth, node and scalar allowances. Its only
production callers are the two checks in `sh08-im-derive-with-allowance`;
`sh08-im-usage` serves only this aggregate. Both checks, both helpers and the
request-vector count cap are removed coherently. Memo entries now retain only
request/result, and active entries retain ID presence for cycle rejection.
This does not remove per-Core carrier limits or ordinary inference allowance.
The actual carrier preflight's accepted/rejected control dependency remains;
it is not a native-class validator or authentication authority.

The source-pinned baseline on `dcc9849d` completed 10 tests / 252 assertions,
natural exit 0 in 15.056 seconds, with actual 2 GiB heap, 1,285 complete stdout
bytes and empty stderr. Source and worktree bookends matched. Receipt SHA-256:
`3775b11a49109f058e8b2d0782398f7a55772d6f2810080ff9ce6220782244f6`.

Production was frozen at `3c71eb01563a3613bb130c5d48337008a73eccb7`, tree
`f95ccb66258d2d06ad10f93e9d45d40d2ae321d7`. Independent pre-run review caught
and corrected a test-only memo expectation before execution. The resulting
`e384b03008fe2f2c8e4a61798ecfeb150aa7ac87`, tree
`275421e2a7021e71868ea6190983d1b2aefa76f6`, is NONPASS: 11 tests, 256 passing
assertions, three failures, no errors, natural exit 1 in 14.133 seconds. Its
Object fixture was accepted by the unchanged carrier preflight, invalidating
the test's rejection premise. Failure diagnostics produced 70,276 observed
stdout bytes, of which 65,536 were captured; stdout is truncated, stderr empty.
The durable counts, source bookends and cleanup are retained. Receipt SHA-256:
`9fae91922eaf5a838f9ebf03ed22e26e2f943d5ba81c934a8c666c85cbbf9860`.

The test-only successor uses a native depth-129 vector to reach the retained
carrier-depth rejection, asserts its exact diagnostic payload, checks the
original Core operand and restricts diagnostic observations. Independent review
corrected the payload key before the next execution. Production is unchanged.
At `f803d9344590bdadfd6238458fcf92932837773a`, tree
`55d1d99878661fd6c23225b2d9fe4c0a8e232e34`, the changed routing var plus six
boundary vars passed: 7 tests / 26 assertions, natural exit 0 in 14.417 seconds,
actual 2 GiB heap, 977 complete stdout bytes, empty stderr, exact clean bookends
and completed cleanup. Receipt SHA-256:
`ef1e522066d18576b4e6376c260c5260b3754b3a1362b6bb0a0e6096e769f38d`.

Four unchanged SH08 selectors retain original `e384b030` provenance: imported
function joins, inferred signatures, scalar catalog and module validation. They
ran before the first failing routing assertion; no failed selector is promoted.
This is not a fresh combined 11-test pass and counts are not summed. Their
source/test inputs and production are unchanged in the test-only correction.
The routing test explicitly supplies normalized authentication and inference
results; actual Gravity derive, carrier/input checks, producer traversal and
memo updates execute, but genuine fresh authenticated-source derivation is
unrun. Source/provenance/signature forgeries, memo substitution, active cycles,
duplicate/undeclared producers and malformed containers remain covered by the
actual normalized owner helpers.

| Tested input | SHA-256 at corrected source |
| --- | --- |
| C7 engine | `fe71a3e26f89927439237817d7eaae08770c6ef0487e93d8aae3651b83144f87` |
| SH08 test source | `66d70f7446dc2c7aef6131934b6b8ef367f4bb19fdb1b0857e3e15731ed49cc2` |

Independent source and retained-result review accepted the finite scope and
the exact correction/reuse boundary without executing another compiler child.
Independent public-payload and finite integration review accepted the four-path
candidate `0e4133842d1af0e8a368c555519532c296d252ec`, tree
`80c14a1f4b70a65ef622b2248b62685a4c394ce1`, against unchanged main `dcc9849d`.
Exact integration preflight passed with matching base/head/tree, clean state and
base ancestry. Boundary/LOC gates passed 11 tests / 23 assertions, actual
repository LOC passed for 2,031 source files, and canonical docs (240 documents /
19 indexes) and roadmap validation passed. That gate receipt SHA-256 is
`2f7f97ca0d87a5051b33fa5d2814a9dbacf91d2f8751160a23c955c6dac20259`.
The subsequent ledger-only successor preserves all tested implementation and
test bodies. [PR467](https://github.com/burn2delete/uniform/pull/467) records the
normal integration outcome of this accepted finite removal.
Per-Core admission limits, ordinary inference, Effects/emitter pools, full ACC-08,
authenticated/public C7, C12 and release remain open or unrun. No source-auth,
Native registration, runtime Set or whole-cleanup acceptance is inferred.

## ACC-08 / ACC-13 Core envelope and binding shape cardinality

This independent increment starts at merged main
`3727e0c9e297374c9fed4a0fa28efa8251893dc9`, tree
`6664054d63707c15948d525ccd669636a816916c`. Accounting retains the previously
released C7 engine and SH08 test ownership. The held Native/runtime/metadata
paths and their requests are untouched. L3, L5 and C7 still require coherent
module identities, source origins, graph structure and type facts.

The finite implementation removes `sh08-bounded-vector?`, migrates every live
keyword/hash vector caller to semantic arity-one helpers, and removes field
cardinality checks in the origin/source/module/node/root/Core-envelope/binding
shape family. Package version and span/map sizes are admission quotas here;
SHA-256's 71-character representation and every actual type/schema predicate
remain. Binding profile/target/effect/capability fields retain their original
vector-only policy; no new member policy is invented. Roots still require
nonempty complete valid IDs, uniqueness and table membership. Node validation
still checks child-before-parent ordering, duplicates, reachable nodes,
declaration equality and source/module matches. The root uniqueness algorithm
is unchanged. The legacy invalid-vector diagnostic name is retained for
compatibility; no Core vector count check remains behind it.

Six descriptor fields have no remaining production consumer and are removed:
root nodes, origin entries, metadata entries, effects, capabilities and exports.
The two inherited import tests now use their same explicit historical fixture
extent 1,025 instead of reading the deleted root allowance. Remaining Core/
child values still serve quote/collection policies; carrier node/depth/width/
scalar limits and ordinary inference allowances remain open accounting.
Those residual limits are not reclassified as semantic or native safety.

The old-source baseline at `3727e0c9` completed 14 diagnostic observations:
small actual Core validation, primitive typing and binding validation succeeded,
while eleven larger field cases reached their old refusals. Natural exit 0,
16.644 seconds, actual 2 GiB heap, complete 987-byte stdout, empty stderr and
clean exact bookends. Receipt SHA-256:
`b54b620096825e34fa2ac5be10a6a684ab4a223f926acac59f3f85583dbb76e1`.

The first candidate `e8b7c18d8b85a98a4bd80baa2ebadd8dfdfc9306`, tree
`045962eaa0025055060d099b70a3fdfd309fc323`, passed 13 tests / 177 assertions,
natural exit 0 in 17.296 seconds, actual 2 GiB heap, 1,721 complete stdout bytes,
empty stderr and clean exact bookends/cleanup. Actual owner-path tests cover
beyond-former-limit fields, late invalid/opaque IDs with zero callbacks,
malformed origins, duplicate/unresolved roots, forward/missing children,
duplicate/orphan nodes, declaration mismatch, binding scope and actual primitive
typing with preserved enlarged source facts. Receipt SHA-256:
`c43b00abe912e468c1d9f828d2d4529dd7f59ebbdd22ec92f052f14a5179d686`.

The stale root descriptor was found after that completed run. Successor
`b93ee3193796dcaa1341947d9f86cd7285e1fce5`, tree
`0dd9552e33330dc8c71d47ccb3d6e36a58e8e40f`, removes it, migrates the two fixture
consumers and checks the exact six remaining policy fields. The three changed
selectors plus six boundary vars passed: 9 tests / 63 assertions, natural exit
0 in 15.377 seconds, actual 2 GiB heap, 1,308 complete stdout bytes, empty stderr,
clean exact bookends and completed cleanup. Receipt SHA-256:
`9ccae734fab692c17ab64f5c92a3714f5f47c61fd26f261f43300cb517440e98`.
Four unchanged binding, graph/typing and imported-signature selectors retain
their original `e8b7c18d` passing provenance. No fresh combined 13-test pass or
summed assertion total is claimed. Independent source and actual retained-result
review accepted both scopes and their exact reuse boundary without rerunning.

Normal reconciliation `42c9a57215b38e842bfd7a8b53aa88fae1767844`, tree
`24206bc60ce2b7491f743c21c80a01110b0f931d`, imports main `82f7e8d`'s unselected
C6 test and two documents. All consumed implementation, SH08, dependency and
contract inputs are unchanged from the actual tested source; independent review
confirmed this closure. No semantic replay is inferred from reconciliation.

| Tested input | SHA-256 at `b93ee319` |
| --- | --- |
| C7 engine | `f5fc78c14db347ba92cac438d98060940f5c83e490b0c37477e177e64e29a02c` |
| SH08 test source | `d2b675634d1a105e4d44f0ee8a4534ab759657d6bcadd612827617dca48317ad` |

This is normalized shape/primitive-typing acceptance. The separate carrier
width still masks admission above 65,536 Core nodes; that behavior is not claimed.
Historical B28/B47 coverage records describe frozen source identities, already
different from current main, and are not rewritten to manufacture migration
acceptance. Authenticated/public source, frozen migration, inference allowance
removal, C12, release and whole ACC-08/ACC-13 remain open or unrun.

The ledger-only `b97115b9` gate run passed boundary/LOC 11/23, actual repository
LOC and docs/roadmap validation, with receipt SHA-256
`3ea8f418c7d6b3feb71ac568d19c035fe08b30a778d55560d0ee229fbb2c4d7c`.
Its exact-base preflight subsequently rejected a concurrent main advance from
`82f7e8d` to `bca9ea29`; that is a retained base-mismatch result, not a semantic
test failure. Normal merge `31001319ad764f53b92cd412b5d5c7af1cae9399`, tree
`3e32dcc58c579e4059985535cd8521eccd341875`, preserves the Reader/Syntax change.
Independent consumed-route review confirmed that SH08's compiler-plan seed path
uses the unchanged host-oracle records and hosted syntax-object-stream wrapper;
the changed C2 product/scalar projection is not invoked. All actual C7/SH08
implementation and semantic-test bodies remain identical to the accepted inputs.
Semantic receipts retain their original identities; composition requires fresh
repository gates and an exact-base preflight rather than an unchanged semantic
replay. No Reader/Syntax or authenticated-source acceptance is inferred.

## ACC-13 Record/Union descriptor and branch cardinality

This finite increment starts at main
`5e84b5acb59f605d8f71cc2d5cf21cc24d15a662`, tree
`05c048d435b15b7b46972daac8a2d265284279b4`. Accounting retains the released C7
engine and SH08 function-test ownership. No Native, registration, held runtime,
Reader or other writer's source is changed. L5's type facts and C7's complete
descriptor/source/equality checks remain the owning semantic requirements.

`sh08-ru-all-record-shapes?` imposes one 64-entry cap on field schemas, field
values, variants and descriptor match branches. `sh08-ram-symbol-vector?`
independently caps typed match branch references at 64. Both are removed
together, preserving vector requirements, symbol-reference nonemptiness and
uniqueness, complete closed entry schemas, supported scalar/pattern types,
constructor/access consistency and exhaustive branch semantics.

Four RU validators previously hashed names/tags for uniqueness before checking
their types. They now finish the complete key/type scan before the unchanged
shared uniqueness helper. Supported declared types are checked as Keywords
before set membership; value classification uses constant-first nil comparison
and the existing Boolean primitive rather than hashing an untyped value.
Native nil/Boolean/Integer/String/Keyword classifications are unchanged. These
local preconditions let newly reached invalid/opaque members reject without
callbacks. They do not establish safety for arbitrary foreign containers.

The verifier's descriptive check changes from
`:closed-descriptor-schema-bounded` to `:closed-descriptor-schema-valid`; no
external consumer was found. Existing preparatory/typed-core scope flags remain
closed-family limitations, not a 64-entry guarantee. Descriptor v1 and typed
result v2 semantic families, complete identity inputs, source/provenance joins,
fresh reconstruction, pending runtime/language features and authentication
requirements remain unchanged. No exported budget or old-policy fallback exists.

The old-source baseline completed 15 observations at `5e84b5ac`: both actual
producers and their fresh verifiers accepted a small family, while 65/129-entry
families and branch references reached the old refusals. The verifiers also
recomputed those rejected results. Authentication was supplied at an explicit
test-only seam; no genuine B47 receipt was produced. Natural exit 0, 13.839
seconds, actual 2 GiB heap, 1,161 complete stdout bytes, empty stderr and clean
exact bookends/cleanup. Receipt SHA-256:
`92c34e3ef15623612d47aeaeea9461a346ba4c77b932dc24d45b9d404a1e0a8b`.

Compiler-free fixture construction and compilation of the new Clojure test
forms passed for extents 2/65/129 with Bootstrap unloaded; that control ran no
semantic tests or Gravity compiler. Boundary/LOC 11/23 plus actual repository LOC
passed before the semantic batch, receipt SHA-256:
`75196e64aab7234dc4287cb0de8e9761f322e5ca8d58cf0179235e23d662fcd7`.

At `0183600583ab43ac9ea169879609f0137ce53f8b`, tree
`d296577c7c3f7c10fab89abcbd8df3f9bd432dbb`, actual validation passed 11 tests /
197 assertions, no failures/errors, natural exit 0 in 17.992 seconds, actual
2 GiB heap, 1,637 complete stdout bytes, empty stderr and exact clean bookends/
completed cleanup. Both producers and both recomputing verifiers accept 65/129
members and retain complete descriptor identity, record/branch facts, pattern
environments, solved constraints and the last members' actual types. Controls
reject late malformed/duplicate/missing/extra members, type conflicts, fact and
provenance substitutions, source-definition changes, path/extension mismatch
and supplied authentication rejection. Opaque names, tags, declared types,
field values, payloads and branch results reject with zero callbacks.
Receipt SHA-256:
`ef695d71167451ebea2c5cebfedb1720eb589404d77089f8846899b57d796aef`.

The sole substituted operation is authentication, restricted to the exact test
thread, engine, selected source plan, request identity and authentication
callee. All descriptor derivation, source joins, semantic checks, fact production
and verifier recomputation execute in Gravity. Independent source and actual
retained-result review accepted this finite boundary without another execution.

| Tested input | SHA-256 |
| --- | --- |
| C7 engine | `6fc9a3f4d06374e898c5ad460b6e69ebfc76b2da4b5438913576526c8b2bb0b3` |
| SH08 test source | `718fa973023217ef13034ae2d382355e20df84a4b8ac6a53c143178783b521e5` |

Main `ea92ccad` adds only a verification-reuse document; normal reconciliation
preserves it with all consumed semantic inputs unchanged. This does not imply
a fresh semantic replay. Genuine authenticated-source/public acceptance,
foreign-container safety, carrier/inference allowance removal, runtime, C12,
release and whole ACC-13 remain open or unrun.

Carrier accounting remains a separate open disposition: its current generic
collection traversal/stringification does not itself demonstrate native-class
or identity-cycle validation. Dropping its depth/size refusals alone would not
establish termination or representation safety. No new permanent quota or
runtime-certification prerequisite is introduced by that observation.

Subsequent normal reconciliation `f1f93901b5192381fa57c56dc4a8b50aba3df52f`,
tree `5a9f09059249e97b47ce944dd5198a02f559634a`, preserves main `6a453ea4`'s
seven lossless C3 projection/fixture/test/report paths. Independent route review
confirmed that SH08 compiler-plan construction still uses host-oracle records
and the unchanged hosted syntax-object-stream; canonical C3 literal projection
is not invoked. The 11/197 semantic receipt retains `01836005` provenance with
all actual consumed inputs unchanged. Fresh composed repository gates and an
exact-base preflight cover the new repository tree; no Reader/C3 or genuine
authenticated-source acceptance is inferred.

## ACC-13 source-proof ancestry admission

The finite ancestry family changes only C7 and its existing SH08 test namespace.
Both ancestry producers and both proof consumers migrate together. Explicit
visited IDs replace the depth cutoff; canonical node records and unique path
IDs precede complete edge checks. Missing/disconnected paths and cycles fail.
The existing loop/function crossing exclusion, source joins, incoming-transfer
census, guard and numeric/index premises, exact proof reconstruction and retained
runtime checks remain. Keyword/String IDs are supported; this is not an
arbitrary identifier-class or foreign-container admission claim. Vector-origin
recursion and binding-link caps, extent checks, inference/work pools and generic
carrier admission are unchanged and remain separate open accounting.

Initial current-main baseline `8cebb9336fa5546090d6b5ac6a4d8dc1930b70fa`, tree
`d0e0d650e67c13de5f71c8a12dd356a2f1fda30f`, exited naturally 1 after 7,472 ms
before any test or observation: Bootstrap could not resolve the
`macro-run-storage` namespace alias during load. Its receipt remains NONPASS,
SHA `d1b1cded91173a3924269e244e643edd5932f3dcf801090cb8cfbd7bc5f5e1de`.
No unrelated source was repaired in this increment.

The separately identified baseline at last verified main
`f3c74ffeac5ccd5c9047d6c436a4886796282d03`, tree
`0415d991c9b59ab1c9098f67693faa8cf8827438`, passed 12 persisted observations
in 15,254 ms, natural exit 0, actual 2 GiB heap. Depth 2 is accepted; depth 140
is refused by both ancestry producers and the direct path validator. Actual
successor derivation is refused, and the nth derivation is downstream of its
rejected invariant, not independent attribution to nth's own cap. C7 and SH08
are identical between the two baseline revisions. Receipt SHA:
`003d29525d01b12b306d3485be3c116f965b71de85d02bfb52e34229956b931e`.

Candidate `d3e17c27e5a2f62a5da4d26a677f251fe1f0f70e`, tree
`2ff4aa2ba69d36c022945d469951aae2b74e1eaf`, passed **12 tests / 294 assertions**,
zero failures/errors, natural exit 0 in **15,088 ms**, actual 2 GiB heap under
the existing two brokers, canonical lock and 120-second watchdog. The complete
1,675-byte stdout and empty stderr, identical clean bookends and complete
cleanup were independently inspected. Receipt SHA:
`2eec3aff0f1fac63e4a3f8c4dfd8aa5a55a681a02808190d4be125acf1aa41c0`.

Selected scope: three new ancestry vars, the existing successor, vector-nth
and vector-get kernels, and six language-boundary vars. Actual Gravity helpers
check paths at depths 2/140/260; the derived successor proof has 141 entries,
and nth/get guard paths have 142. Fresh proof validators accept the complete
results and reject late edge/source/guard changes and duplicated entries.
Cycle, absent parent/node, disconnected root and loop/function crossing controls
remain rejecting. These are normalized source graphs, with no supplied safety
proof or inference result; genuine authenticated-source/public authority is
unrun. Runtime execution, C12 and release are also unrun. No semantic rerun was
performed by the independent reviewer.

| Accepted input | SHA-256 |
| --- | --- |
| C7 engine | `503dd647feddf376e1a0380be4cdc6a87bcb444b180433d4f55fae74fbd10ad0` |
| Existing SH08 test namespace | `efbc7f069f39bcfe6ed93eb3d068a05a5e9032cd39c2784c419f883767d5f31d` |

Compiler-free fixture/test-form controls passed without loading Bootstrap.
An initial private driver-generation delimiter error and a cheap boundary
invocation missing the test classpath both stopped before semantic tests; their
corrected controls are preparation evidence only. The corrected boundary control
passed 6 tests / 11 assertions. None changes the current-main load NONPASS.

This finite implementation/result is independently accepted, but current-main
semantic acceptance and integration remain blocked by the distinct namespace-load
failure. Standalone acceptance must not be promoted to current-main acceptance.
All full original ACC rows, including ACC-13, remain open.

Normal reconciliation `848a4812b4d5360dd1eb4242a9b9d804cb13b33d`, tree
`6e63108c2b30f776c331a311b3f8e828af3b916f`, preserves all seven current-main
Macro/storage/metadata paths. The two accepted C7/SH08 blobs remain identical
to `d3e17c27`. The Bootstrap dependency is changed and is known to fail loading;
therefore the 12/294 receipt is retained only at its standalone provenance,
not reused as composed-main semantic acceptance. Lightweight repository gates
at the preceding documentation successor passed 11 tests / 23 assertions,
actual LOC validation (2,033 files), 240 documents / 19 phase indexes and the
full-language roadmap. Those checks do not execute or accept Bootstrap.

### Repaired-main ancestry composition

PR483 merged the alias-order repair at
`c0c7f9c363c709827e95f166e922fe9d1c07caf2`, tree
`f7f8a02c73e4b69e3032e97789560a09d3e12a9c`. It moves the existing require to
its first semantic consumer; verifier/storage behavior is unchanged. The
owner's independently reviewed cold-load evidence covers namespace loading,
not C7 semantics or initialization-effect closure. The original `8cebb933`
NONPASS remains historical failure evidence.

Normal composition `3e80fc8460309591b1019aae97f763a37354fbc0`, tree
`52e342188a6003679850a59f18c28cec4f36065d`, preserves all Reader/repair paths.
C7 and the owning SH08 test namespace are byte-identical to tested `d3e17c27`.
The hosted seed route uses host-oracle records and macro construction without
invoking the changed SH05 storage verifier. Reader's `form-kind` is consumed;
its added BigDecimal case does not alter the top-level definition forms used by
this route. Canonical C3 expansion-lineage construction is not consumed here.
This is a source-specific dependency assessment, not a blanket unchanged-Reader
or effect-closure claim.

Actual focused composition passed **9 tests / 63 assertions**, zero
failures/errors, natural exit 0 in **14,686 ms**, actual 2 GiB heap under the
unchanged guarded route. The three new ancestry vars and six boundary vars were
selected. Complete stdout is 1,371 bytes; stderr is empty. Exact clean
bookends and complete cleanup/root termination were retained. Receipt SHA:
`f281c9d8c69684a854cc22bc367e774d00dc95ea7c4d1de8b076f0c288cf08f3`.
Driver SHA:
`382b266e67d8b067dbd20c5a326b1a4a3bcae7ee73a4650d4d0646f3a73bec91`.

The original three unchanged regression vars (successor, vector-nth and
vector-get kernels) retain `d3e17c27` provenance. The original 12/294 and fresh
9/63 are not summed and are not described as a fresh full 12-var run. Genuine
authenticated-source/public acceptance, arbitrary foreign containers, runtime,
C12, release and every full original accounting row remain open.

## Current original-criteria reconciliation

Assessment time: **2026-10-26 22:28:21 MDT (America/Edmonton)**, equivalent to
2026-10-27 04:28:21 UTC. Exact assessed main is
`15a5499e97798621d270c36dd2546ce98ddd62a9`, tree
`aa5dd1d419594b31f9f9837ba6d09e39894b3562`. The original-ID disposition table
in `tasks.md` is the current assessment; no dashboard or separate status ledger
was written. The old 50% is retained only as an unmeasured provisional estimate,
not silently promoted to measured current coverage or recomputed from counts.

The ancestry increment PR481 is now integrated by normal squash at that exact
main/tree, identical to reviewed candidate
`95d67b76c423d2856a3eba513af3698baf37a6f0`. Fresh remote merged state, exact
head/body, full-tree equality, main ancestry and retained branch were verified;
independent review accepted the identical merged tree. This resolves the earlier
ancestry integration hold. Original standalone 12/294, fresh composed 9/63,
final repository gates 11/23 and the initial load NONPASS remain separate
provenance. No fresh semantic rerun was needed for the identical merge tree.

Fresh GitHub states at assessment time establish the following exact merged
increments. Their individual earlier evidence scopes remain authoritative; this
table does not turn PR state into semantic acceptance or whole-row completion.

| Finite increment | Merge commit | Original rows advanced |
| --- | --- | --- |
| PR433 module reads | `cfb499405450d479955fa70daa0f14dfa3233ee8` | ACC-09 |
| PR444 module census | `6b2e63b32f215b0c5f963fadfc018624837685b0` | ACC-09 |
| PR467 imported-module aggregate | `3727e0c9e297374c9fed4a0fa28efa8251893dc9` | ACC-08 |
| PR474 Core/binding shapes | `029e0bd2b93542e62854b57eb7d8146e5354cb19` | ACC-08/13 |
| PR479 Record/Union cardinality | `f3c74ffeac5ccd5c9047d6c436a4886796282d03` | ACC-13 |
| PR481 source-proof ancestry | `15a5499e97798621d270c36dd2546ce98ddd62a9` | ACC-13 |
| PR378 MIR guard restoration | `96cec79c0ec1f658d62e96027d4a570e48bff7c7` | ACC-10 finite owner slice |
| PR421 guarded MIR/Native composition | `06a256fdb97f49b5fedea36a6803483302631bbc` | ACC-10 finite owner slice |

Separately held work was read fresh as draft/unmerged: PR359
`6a4f87f013d0076b81aba9e0667650e70d28e635`, PR450
`99b84629bc9b4cb1c30c5f59777ae64f02cf65b0`, PR454
`93efcde35fce442d9e3ee289331ad421ee8dbe45`, PR462
`0cd2f29427ca67ae6127c7983eb51896b564f977`, and PR370
`516f5f6ca77aede73fc7a6d7ec56d5a6da606ee6`. PR383 is closed **unmerged** at
`356ba311afa4ec80afae4188db87d1511b5dc6ce`; imported evidence belongs to the
actual reviewed composition, not an invented separate merge. No new owner
acceptance is inferred for PR370 from its presence in this list.

The precise residual gates and status of every original ACC-01 through ACC-13
are in the dated table. Full closure still requires exhaustive dispositions,
coherent removal in each remaining family, actual permitted producer/consumer
acceptance, independent review and current-main integration. Genuine runtime,
type, source, authentication, representation, cycle and physical host controls
remain. No whole-language completion or restricted C12 run has been added as a
new requirement for accounting closure.

## ACC-02 complete guard and parameter-premise closure

Assessment update: **2026-10-26 23:04:48 MDT (America/Edmonton)**,
2026-10-27 05:04:48 UTC. This current-main successor is independent of the held
Native/Set chain and does not import the broad PR359 implementation. Explicit
source ownership release was received earlier; `release` in that handoff does
not mean product release. The complete original ACC-02 acceptance is eligible
for closure only on normal reviewed main integration and exact readback.

### Actual source contract and baseline

Main `c637326dcf026cfd1697420a3b9b383439cb804a`, tree
`a7a6219b17db67cbd50fa3c10062ad97f3bd7fff`, has six accounting fields:
`operand-type-work` in both `mp-compatibility` and `lp-compatibility` for C7,
Effects and the emitter. The earlier four-consumer-field description assumed
those two producer fields were already absent. Removing all six coherently on
this source preserves the same original producer/consumer obligation.

The accepted baseline passed 20 persisted observations in **25,934 ms**, natural
exit 0, actual 2 GiB. All three owners produced equal full premises and exported
the accounting field; adapted Map/bare List/homogeneous List and identity cases
reached actual typed emission. A bounded opaque object demonstrated that the
old C7 ordering classifier called its hash method before rejecting the type.
Complete 1,996-byte stdout, empty stderr and identical clean bookends/cleanup
were retained. Receipt SHA:
`7d7ed186b73b173678b04b833f2f8a6405eea50fe9feffcc716ea58a96cc3ad1`.
The first launch used digits in a letters-only harness label and failed its
assertion before any child, bookend or lease; the valid-label launch changed no
harness guard, resource limit or baseline source. That preparation failure is
not semantic acceptance.

### Implementation and preserved semantics

Source `d346b9312ab7797e34f0abc66ff6ef45482a22fc`, tree
`0a58737e200d1ba53e46d8d696b6422992eac155`, changes exactly the six premise
fields and guards integral set membership with `keyword?`. Semantic family and
identity domains stay unchanged: full source-bound reconstruction/equality
rejects stale or extra fields; there is no old-wire acceptance or field
stripping. Helper work results, supplied budgets and real full validation are
unchanged and remain explicitly open in their other original criteria.

Three-owner controls preserve Map member-refinement forgetting, bare List
position forgetting and homogeneous List every-element checking. Checked
element count/type, Never/not-applicable, ordinary identity, all preserved
value/provenance/schema/ownership/region/profile/memory properties and actual
Effects context/local checks and typed emission remain effective. Positives
include 1,024-member types. Late unknown/opaque members at position 1,023 lie
within the existing admissible extent and reject without opaque callbacks.
Stale work values (zero and the prior actual value), extra fields, extent/type,
source/declaration, callee, duplicate ledger and argument forgeries reject.
These are actual Gravity owners over normalized declaration/source fixtures,
not genuine authenticated-source/public authority or runtime execution.

### Failed batch and bounded correction

The first source batch is **NONPASS**: 13 tests, 662 passing assertions, zero
failures and **9 errors**, natural exit 1 in **29,367 ms**. All errors are two
existing tests calling `sh08-ft-loop-kernel-transfer-types` with six arguments;
production already required seven in baseline `c637326d`. Complete 36,133-byte
stdout, empty stderr and exact clean bookends/cleanup were retained. Receipt:
`57b92da4518746dff7343947d0e858b2ed1cc254159cd590fd858fc7637a25de`.
This failed batch is not relabeled or summed into a passing result.

Test-only correction `26768f8ec3c9bab87864f5beb371463893ac7407`, tree
`340c05407cfd782f8226ecd6ce1a6adccce7ed86`, adds the empty invariants argument
to all three stale direct test callers. Expected semantic results and all
production sources are unchanged. The changed homogeneous List, bare List and
Map recur controls plus six boundary vars passed **9 tests / 142 assertions**,
zero failures/errors, natural exit 0 in **16,636 ms**, actual 2 GiB. Complete
1,325-byte stdout, empty stderr, identical clean bookends and complete cleanup
were independently inspected. Receipt SHA:
`2e84466ebd6fc010ba1384447ee3ea0741226e0f1e7ef26f7802806e55dd5912`.

Five unchanged successful non-boundary selectors retain original `d346b931`
provenance: the opaque ordering guard, complete three-owner premise/typed-call
control, invalid-member/declaration control, bare Map kernel and mixed Map/List
kernel. Their bodies, shared helpers and consumed production inputs are
unchanged. Boundary acceptance is fresh at `26768f8e`. There is no fresh full
13-var or summed passing count. Independent source and actual-record review
accepted this composed ACC-02 scope and found no further required affected-owner
path in the governing row. No reviewer semantic execution was performed.

| Accepted implementation input | SHA-256 |
| --- | --- |
| C7 engine | `3ceabc93ceaa8899d7274c1eec32838828862a1c9be1109c1b639d45ca4823ee` |
| Effects | `e0e261df37c4af1103b1d73bd9b26008ed232a98f39ff42631fff43fee078578` |
| Emitter | `1c9c242002fbdf8174356974bf4b29c9bdc34f81c27bfc386abefa39a246e7be` |
| Existing SH08 test namespace after correction | `b2c0dd7f07918dcee4b3aa9fba8d853ebd0eb8317ef3f2262abf0a23e46cbd55` |

Full helper/native-cycle/view validation and construction admission remain open
ACC-06 work; a native header by itself is not an unbounded traversal proof.
Inference work/round allowances remain open ACC-07 accounting; finite node IDs
alone do not prove finite-height evolving structured types. These findings do
not reclassify artificial limits as permanent safety guards or require a
replacement budget. Other original rows remain open, as listed in the current
`tasks.md` assessment. Genuine authenticated-source/public admission, arbitrary
foreign-container safety, runtime execution, C12 and product release are unrun.
No percentage or whole-accounting closure follows from this row.

Normal reconciliation `40566675b774fa2e5d185c8955d6cd2a0679ea24`, tree
`474cbc8ab74b7aefa4ed776d83ae0efa7c2925cc`, preserves current main
`254a5b50ea5e2825c424248c9216802cc866c0fd` and its ten compiler-API/release
documentation, projection-tool and task-lineage paths. All bootstrap sources,
own implementation/test bodies, `deps.edn` and contracts remain byte-identical
to accepted `26768f8e`. Those incoming tools are not consumed by the selected
C7/Effects/emitter execution route. Semantic receipts retain their original
provenance; fresh repository gates and exact-base preflight cover the composed
candidate. No compiler-API/release implementation acceptance is inferred here.

## ACC-03 complete LR premise/schema closure

Assessed **2026-10-26 23:45:48 MDT (America/Edmonton)**, equivalent to
2026-10-27 05:45:48 UTC. The original criterion is coherent LR premise/schema
alignment and invalidation, full equality, semantic extents, every-member
controls and typed definitions. The original ACC-06 separately owns recursive
`lc/pl/lr-type-work` admission. This record does not redefine that helper work
away or credit the broader held PR359. Closure is effective only after normal
reviewed current-main integration and exact readback.

### Actual baseline and coordinated migration

Baseline main `a33e0eef2561ec0f522d05763cbdd33b5b01f418`, tree
`790f03c22a3f515551cab108c6e4b8b9dd0c9d31`, passed **30 persisted observations**
in **21,210 ms**, natural exit 0, actual 2 GiB. Empty, singleton, 1,024-member,
homogeneous identity and Never cases reached actual C7/Effects/emitter derivation,
equal full premises, both contexts and actual typed definitions. All owners
exported the two work fields and v1 schema. Complete 3,628-byte stdout, empty
stderr, identical clean bookends and cleanup were independently inspected.
Receipt SHA:
`82e4f1edaca06339b7157ad5d208223cb1731b43ce9d39984a911b8b4f485aac`.

Candidate `01e9ec5f8ae8e39dbc2d0b9a3f52d13db26ab0d0`, tree
`6a21f1aad49a7152c725cfa0c5dd18cee055b8b1`, removes six exported fields and
migrates the three premise versions, producer tags and constraint identity
domains together to v2. Helper `checked-member-count` and exported semantic
`checked-element-count`/type remain; every-member validation, source and binding
joins, actual/declared types, relation and completion/effect/allocation semantics
are unchanged. The helper work results and budgets remain open ACC-06 work.
All existing test expectations referring to the current LR schema migrate.
Tracked-source reference inspection found old v1 literals only in rejecting
census guards and explicit negative tests, not an acceptance fallback.

Before any candidate run, source inspection found that a reserved LR identity
domain could escape a producer/kind-only filter if both labels were changed.
Both context filters now include reserved current and legacy producers **or
identity domains**, as well as the return kind, solely for complete rejecting
census/equality checks. The actual controls append legacy and domain-only rows
with foreign/missing kinds or foreign producers, both with active declarations
and with no LR declaration. They require exact census rejection; empty ordinary
contexts remain accepted. This is schema-invalidation work within ACC-03, not
new authority, field normalization or legacy wire acceptance.

### Actual acceptance

The bounded candidate passed **16 tests / 1,093 assertions**, zero failures or
errors, natural exit 0 in **33,628 ms**, actual 2 GiB under the unchanged brokers,
canonical lock and 120-second watchdog. Complete 2,188-byte stdout, empty stderr,
exact clean bookends and complete cleanup/root termination were independently
inspected. Receipt SHA:
`b01444416478393ccf3cc36c89e2deb4ff8c99e072c0c962af6288c5a4a4bf50`.
Driver SHA:
`673a12b452d45503c3287fdaee29fa249deab44fec544a4cc3dc0fd503d9e956`.

Selected scope: five new LR owner/census vars, the existing C7 LR kernel, two
Effects LR kernels, emitter typed-definition and consumer kernels, and six
boundary vars. Actual three-owner rows match completely. Effective controls
cover positional adaptation, homogeneous identity, Never, checked extents and
types, late invalid/opaque members, coherent stale/extra fields, old/mixed tags
and domains, duplicate/reordered/missing/orphan rows, source/callee bindings and
typed definitions. Opaque callbacks remain zero. This is one fresh complete
selected batch, not a sum or promotion of old PR359 evidence. No reviewer
compiler/semantic execution was performed.

| Accepted input | SHA-256 |
| --- | --- |
| C7 engine | `e81f55f444d5a0e3a131eba719c1a57973c2a84fb227b1a0ee1479088f14c239` |
| Effects | `155e9c3912f3f88d22376ab22efa7fcd4569234966b8b5e57772c6fde1dfe137` |
| Emitter | `4dee31fd3fcec487157a8f3c2432b145f097974f2ac237e50ed682c97e1b9454` |
| Existing SH08 namespace | `aa1ad4e111a2321c2b5de9e58666d19f3619791647529fb76ea6964632523c7f` |
| Existing runtime-iteration namespace | `dd08c863c1cc2ff8b33c6c4c50564c00fc21ec7dea010ae03c937d0b4b419ebe` |

Higher authenticated-source/language-runtime test families whose only changes
are current-tag expectations remain unrun. Their static migration is not claimed
as source authentication, language-runtime execution, C12 or release acceptance.
All helper/recursive budgets and the eleven other original criteria remain open.

### Current-main dependency reconciliation

Normal composition `02a2292defe923a563f8b06dd735094caa9cb66f`, tree
`7bbc7d53a900d737750a09e7f8b047e09c2d2eea`, preserves main
`4325a9f46292472a7f984c5adfef81209874bb5c` and its 17 Reader/Resolve-test,
planner/contract and documentation paths. All five accepted owner/test bodies,
Gravity source and `deps.edn` remain unchanged. The sole incoming Bootstrap
implementation delta fixes the canonical Reader decode invocation path. The
selected compiler construction uses host-oracle records; the H6 route explicitly
binds authenticated source records to nil. It does not invoke that changed Reader
constructor. Planner policy changes concern the unselected Resolve-parent test;
the established explicit-command wrapper and its process APIs/guards are unchanged.
Independent dependency review accepted reuse of the 16/1,093 receipt at `01e9`
provenance with fresh composed repository gates/preflight. No broader Reader,
Resolve or planner acceptance is inferred, and no unchanged semantic run is
repeated merely because main advanced.
