# Checked bare-Set equality source port

This finite source increment belongs to the preserved DSE family under the
existing ACC-04/09/12 obligations. It does not change the canonical task ledger
or ACC01's PR502 disposition. The governing [spec](spec.md), [plan](plan.md),
[tasks](tasks.md), and [29-path disposition](evidence.md)
remain authoritative. No original ACC criterion is closed here.

## Baseline and source census

The isolated, clean baseline is `6276c94b0b753c888ef8fecc794bcc81245ea3b3`,
tree `9f464ed0f03866bd4978e4213a488f3b1ae48683`. The Uniform project association,
linked worktree and chat attachment were checked through the supported app.
The implementation branch is `codex/dse-bare-set-equality-01a18531`.

PR502 remains immutable history at
`6a4f87f013d0076b81aba9e0667650e70d28e635`, tree
`f8440ffcf2d2fa7e2f898178ee299ac64ef59d32`. Its 29-path delta is not this patch.
Only its DSE source, descriptor/fact identities, source joins and related tests
were used as inputs. Main intentionally lacks that full feature. Descriptor
field deletion or alias parity would not implement it.

[The exact census](set-equality-census.edn) records each historical DSE
definition, the new definitions and direct source callgraph, every source hunk,
source/dependency hashes and both original fixture blobs. It is a source map,
not evidence that Gravity accepted the implementation.

| Boundary | Existing driver and ported behavior |
| --- | --- |
| C7 production | `sh08-ae-step` and `sh08-kse-derive` reach the existing equality dispatch. The new `types-set-equality-*` functions reconstruct a complete source-bound descriptor, result type and solved constraint for two bare Set operands, including Set/Never transfers. |
| C7 fresh checks/facts | `sh08-ft-call-constraints-with-context`, `sh08-tc-products-with-source-forms`, intrinsic classification and `sh08-ft-call-facts` recognize the DSE descriptor and use fresh reconstruction and the DSE call-fact domain. |
| Effects | `body-ae-context`, `body-kse-call-valid?` and `body-ae-call-valid?` reach the existing equality dispatch. `effects-set-equality-*` reconstructs original calls, edges and nodes, complete descriptor/constraint/fact equality, source forms and unrefined types. |
| Typed emission | `p15-s23-typed-compile-call`, equality/Set equality dispatch and the new `emit-set-equality-*` functions validate the complete join, then lower each original operand once in source order through the existing child compiler. The resulting instruction is the existing binary `=` builtin. |
| Runtime dependency | Existing runtime `=` dispatch calls `p15-s23-runtime-keyword-set-equality-work`, which calls the joint Set domain and atomic equality helpers before returning a result. This provider already exists on main and is unchanged here. |
| Native dependency | Existing read-only native carrier headers, identity, and the three owners' native view/List helpers establish class, visible extent, cycle and callback premises before comparison. No Native registration or access-policy operation is attempted. |

The existing `*-bka-*` names remain compatibility-bearing dispatch locations.
Their new first branch selects DSE; the prior anchored-Set branch is preserved.
New implementation functions and the test namespace use semantic names. Stable
`:sh08-checked-bare-set-equality-v1`, constraint/fact domains and dispatch IDs
are retained. Renaming the existing dispatcher family is outside this slice.

## Semantic extent and dependency closure

The source guard traverses complete native DATA without node, depth, width,
scalar or work quotas. It rejects unsupported classes, metadata-bearing carriers,
foreign comparators, invalid views/List spines and ancestor cycles before
structural equality or ID hashing. Shared acyclic values remain valid. Native
header inspection does not invoke foreign count, equality, hash or spelling.
The finite supported runtime member domain remains joint homogeneous Keyword
or String Sets; this does not grant general Set equality or hashing support.

The header retains exact core binding/package, profile/target membership,
source/callee/evaluation order, original node-index agreement and unique
function-owner records. Both operand types must be bare Set or Never. A normal
call returns Bool; a transfer returns Never without invocation. No operand
member/cardinality/refinement fact is invented. Equality has no new direct
effects or capabilities; ordinary operand effects remain an upstream obligation.

Consumers rebuild every DSE row and fact. They reject duplicates, orphaned or
stale joins, mismatched map keys, forged descriptors and missing/mislabeled
reserved DSE tags. Ordinary local-call rows and facts may omit their optional
producer/dispatch fields; explicit malformed fields remain invalid. Complete
fresh equality rejects accounting fields added back to a descriptor.

Historical DSE local reservations, debit propagation, publication/storage work
fields and accounting-only tests are not ported. No counter is zeroed and no
larger allowance or unbounded sentinel is introduced. Existing child-lowering
accounting remains an external interface; the DSE compiler forwards its actual
child result without fabricating local telemetry. Other outer inference,
Effects and lowering mechanisms remain their existing owners' obligations.

Runtime equality still has its existing work/width policies. Their removal and
runtime acceptance are not established by deleting descriptor fields. The
ACC09 initializer/literal/conversion composition and Native446 registration
remain separate. The active ACC09 state was read before sending one finite
equality-interface question; no runtime source or runtime job is duplicated.

## Preserved fixtures and inherited controls

The `.gravity` and `.qst` `local-runtime-bare-set-equality` fixture pair stays
at the preserved head, absent from the current accepted fixture directory.
Neither spelling was consumed or accepted. The census pins both identical
contents and original Git blobs. The other four preserved source/verifier
pairs, PMK descriptor/closure, and initializer/Set literal/conversion features
are outside this port.

Historical `sh08-checked-dual-bare-set-equality-development-kernel` and
`sh08-checked-dual-bare-set-equality-current-source-composition`, descriptor
reconstruction, checked-bare-Set mixed-ledger, body-effect and runtime iteration
tests were inspected as dependency evidence. Their old accounting expectations
and receipts are not transferred to this source. The new tests reuse only the
existing normalized `integral-ordering-input` fixture constructor.

## Validation and remaining acceptance gate

`gravity.types.set-equality-test` reads the three owning Gravity files and
evaluates selected definitions in an explicitly labeled Clojure model. Native
header primitives use their existing read-only implementation under a synthetic
context. Typed child compilation is injected and cannot execute a real child.
Unselected producer fallbacks remain unbound and fail if reached. This is source
model/control evidence, not Gravity interpretation, type/effect production,
source authentication, emitted execution, runtime acceptance or migration.

The controls cover complete three-owner descriptor parity; normal and Never
results; type/profile/effect/capability/source/owner negatives; late invalid
records after 1,025 valid records; mixed ordinary/DSE rows and facts in both
orders and map/vector forms; coherent descriptor/fact forgeries; original-node
substitution; tag stripping; zero-callback foreign values; shared values;
metadata, cyclic-tail and beyond-former-width/depth DATA; source forms; and
injected lowering order with zero child launches on invalid evidence.

Development checks executed against these source bytes:

| Command | Observed scope |
| --- | --- |
| `clojure -J-Xmx512m -M:dev-test --namespace gravity.types.set-equality-test` | Passed, 6 tests / 233 assertions, natural exit 0; source-bound model and injected controls only. |
| `clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Passed, 6 tests / 11 assertions. |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Passed, 240 docs / 19 phase indexes, ASCII and structural checks. |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Passed. |
| `git diff --check` | Passed. |

Private paths and raw logs stay local. Structural gates do not establish the
compiler/runtime semantics described above.

The finite genuine acceptance gate remains:

1. Independent source review of this exact candidate and its dependency census,
   routed by the coordinator after checking pending review state.
2. A permitted current-source C7 producer/fresh-check/constraint/fact run joined
   to the real Effects context and typed emitter, with the same positive and
   type/effect/fact/source/mixed-ledger negatives and stable diagnostics.
3. Actual emitted runtime execution for empty, equal/reordered and differing
   Keyword/String Sets, late invalid/domain/foreign inputs with zero callbacks,
   operand effects/transfers and original-value preservation under the existing
   Native boundary. Resolve the actual provider accounting separately.
4. Consume the preserved source fixture pair only through the permitted source
   composition. Keep the executed spelling and paired parser/byte checks
   distinct from paired typed/emitted/runtime acceptance.

No held producer/semantic run, broker/cache/lock/process probe, unknown historical
mode retry, restricted C12, security-setting change or denial bypass is part of
these gates. Until the permitted evidence and independent acceptance exist,
the PR remains draft; no integration, whole-ACC, release or self-hosting credit
is claimed.
