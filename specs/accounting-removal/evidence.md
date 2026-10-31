# Accounting removal evidence

## ACC-05 PL inventory and finite successor - 2026-10-28 UTC

Main `19db4a52c6c3a354374acca78354adc738280b5e`, tree
`de6fa80413b146f593efe13a59d66d28d3b00462`, contains the integrated LR helper
unit. The PL source inventory was originally recorded at `c2eaa507` with this
identical full tree; that is retained provenance, not a new compiler execution.
Read-only AST and whole-source symbol inspection agree on twelve definitions
and nineteen calls, with no first-class helper aliases. Nine direct call sites
inject helper costs into caller reports. The tenth external call is semantic
only. The three dispatchers make the remaining nine calls.

The debit consumers are C7 `lsi-insertion`, `pl-derive` and its three Set
inferred-call wrappers, Effects `va-context` and `lsi-insertion`, and emitter
`typed-compile-prepend` and `lsi-insertion`. Downstream C7
`tc-products-with-source-forms` consumes the parent reports, so removal occurs
at the helper-debit sites rather than deleting the parent's independent pool.
Three `pl-source` builders consume status/result/family/argument/extent fields.
Effects' `append-context.type-checks` is a semantic cache passed to
`body-pl-call-valid?`; loop-row solving instead invokes a fresh insertion
helper. Other construction/query `type-checks` contexts are distinct.

Independent source/concept inspection requires a native closed semantic-cell
guard before any family lookup, plus fresh current-argument derivation at the
Effects cache consumer. Schema checking alone cannot reject a coherently
forged cached result, descriptor, row and fact. Supported member domains,
complete validation, Never behavior and source/equality checks must survive.
STD3's persistent Set insertion and List prepend contracts retain real
allocation/profile obligations. Their runtime value-work declarations remain
separately unresolved and are not silently removed with compiler type costs.

The old-family baseline harness has independent static acceptance at SHA-256
`c60e05875f896191fe2d1e448816806f6f796b1b8191b73b2f6b9d490e26e1f4`.
Its intended scope is three owners by four families: small accepted inputs,
family-specific budget-one refusal and the old 1,030-member width refusal.
An 11-form read at 256 MiB passed without evaluation. Actual semantic execution
and implementation acceptance remain pending; none is inferred from this read.

### Actual PL successor results and held disposition

The preceding paragraph records the initial harness review. Subsequent actual
results are below; the original artifacts retain their exact provenance.
Independent source and retained-result inspection accepted the finite PL
helper unit at `e8c8a14f92ccd6f5775139da603d42f9cadd86ee`, tree
`2edb41b1b9771a5f0be6602edc6bb143414f4b07`. This is not a fresh execution of
every selector on that reconciled head. Incoming PR508 changed only its Reader
fixture, SH03 test and report; consumed PL production, owned tests, Bootstrap,
contracts and dependencies are unchanged from the tested tuples.

| Actual stage | Original source | Result and boundary | Receipt SHA-256 |
| --- | --- | --- | --- |
| Old four-family allowance baseline | `704eb24d`, tree `c7908747`; production identical to main `19db4a52` | 1 test / 36 assertions; natural 0 / 19,272 ms; actual 2 GiB; complete 105 stdout / 0 stderr bytes | `20d5cf3c3d71acef005b85fa9711fea41ee9604c474488df54d5ecca578ec556` |
| First helper/cache/C7 panel | `58fa90a4`, tree `9c6706bf` | NONPASS: 14 tests / 1,392 passing assertions / 2 failures / 0 errors; natural 1 / 23,182 ms; complete 2,287 / 0 bytes. Both failures were one foreign-member-vector diagnostic oracle | `e354ae8b43db331ac2800282cdecf37dad6d3c541b09ce660d8891b16632ffbf` |
| Corrected Symbol var plus boundary | `385677d1`, tree `b95a3a86` | 7 tests / 334 assertions; natural 0 / 14,412 ms; complete 966 / 0 bytes. Only the expected native outer-type reason changed to `malformed-type`; rejection and zero reads remain | `2db80c7d6542620c31115b9fb9e9cc0c633adec0bbaae55bd5dd00ba1d140147` |
| Original consumer panel | `385677d1`, tree `b95a3a86` | NONPASS: 26 tests / 3,691 passing assertions / 88 failures / 24 errors; natural 1 / 47,877 ms; stdout 197,128 observed / 65,536 captured, truncated; stderr empty. No complete per-selector acceptance inferred | `688c6d5dc0841394005c5f0f71582be3f77c942e509b51dbce99269a76e9c6a1` |
| Remaining-consumer localization | `385677d1`, tree `b95a3a86` | NONPASS: 20 tests / 2,285 passing assertions / 88 failures / 24 errors; natural 1 / 26,101 ms; complete 3,798 / 0 bytes and durable per-var counts. Only two existing Symbol-membership kernels fail | `fab36ed2eaeba54a8d2838e69a0add4d3e82f0af22cf4b80cad947a1460573fb` |
| Minimal actual old membership diagnostic | `704eb24d`, tree `c7908747` | 1 test / 5 assertions; natural 0 / 12,864 ms; complete 104 / 0 bytes. Confirms one foreign `:kind` lookup, four count calls and zero sequence calls; it is evidence of the old gap, not validator safety | `1a076a138389984a23ba3462be263b0f330a5288d8a87af8206fcdce78d62a66` |
| Three actual loop-insertion callers plus boundary | `e8c8a14f`, tree `2edb41b1` | 9 tests / 191 assertions; natural 0 / 26,025 ms; actual 2 GiB; complete 1,237 / 0 bytes | `91a28df4801b825c3a6a46975c19a3135b07d53fbb0f228843507528b31f2c3e` |

Every actual child above used the existing 2 GiB / 120-second guarded route,
clean identical bookends, complete cleanup and a dead root process. Capture
truncation is retained explicitly for the first consumer batch. The materially
changed localization harness preserved real failure/error counters, omitted
six already fully captured runtime selectors and persisted bounded failure
locations before proceeding to another var. Its compiler-free capture control
preserved the expected one-test, 50-pass, two-failure, one-error fixture with
zero opaque printing callbacks. An initial doubled test-counter control failed
and was corrected before any diagnostic source execution.

Successful unaffected selectors from the first panel retain `58fa90a4`
provenance; only the changed Symbol var and boundary were freshly rerun at
`385677d1`. The localization gives separate zero-failure per-var records for
the other eighteen selected vars. No assertion totals are summed, no failed
panel is relabeled passing and no globally fresh pass is claimed. Cheap source
conservation passed 2 tests / 58 assertions without Bootstrap after its initial
three failures exposed missing native preclassification changes. Source and
all test forms read successfully without evaluation; old helper names have no
remaining production or dynamic test references.

The two residual kernels are
`body-symbol-set-carrier-contains-development-kernel` and
`current-gravity-symbol-set-carrier-contains-development-kernel`. Each has
44 failures / 12 errors exclusively at its existing non-three-field foreign
map block. Their membership classifiers, domains and type predicates are
unchanged from `704eb24d`; the Effects and emitter closures also match after
owner-prefix normalization. The minimal actual old emitter diagnostic binds
that static finding to execution. Keep these genuine negative controls and
their ACC-06 validation requirement open; they are not PL insertion debits.
They hold the ready/merge workflow, so this successor remains draft.

Fresh repository gates passed at `414b3c67f04460352d68a1a2bd27446f832d892c`,
tree `dbc1bafd6f6c34a311483bf46a46447fddd0149c`. Boundary/LOC passed 11 tests /
23 assertions with 2,035 source files, document validation passed 240 docs /
19 indexes and roadmap validation passed. The bounded 256 MiB normal-broker
child exited naturally 0 in 2,248 ms, complete 456 stdout / 0 stderr bytes,
clean exact bookends and complete cleanup. Receipt SHA-256:
`ced283379cae75342b1b41517ee541f458f61209acccf25f302501ab334f7f9a`.

The unchanged CI-pinned lint policy at the same tuple reports 223 unchanged
baseline warnings and zero blockers against main `656f545f`. It exited
naturally 0 in 14,912 ms at 256 MiB, complete 76 stdout / 0 stderr bytes,
clean exact bookends and complete cleanup. Receipt SHA-256:
`c92d5aa9b2be9d5d3d8ce74b760a7c9870b3534128f5f5a53b28184936698c80`.
These cheap passes do not change the two membership failures or authorize a
ready transition. Public CI and draft readback remain separate pending stages.

| Tested source/test path | SHA-256 |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | `9c5e77a7bc179a21f5c76eea5009d90c29c9c85ac550728dc70e3c3f5d6b2a32` |
| `bootstrap/gravity/src/gravity/effects.gravity` | `5f99401adaaed5464916339af2d981212afe93089190bc60d0e7a3709b2fad74` |
| `bootstrap/gravity/p15_s23/emitter.gravity` | `20d526a01ccdb9073284a7932932d7eb742e09e26779f9da221a1d6174082bd7` |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | `cafa971fec30e4dc57655e4e2f8ce2c08045a9e2ded21c840430198f1ee18e63` |
| `bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj` | `8522e0775e0d5c80e4ea37689c3458e5a22c1a1c1a86a9a8b4b2b070386357de` |
| `bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj` | `c6b39e601920db5429921020dc58a5727c39b8838681bd6f1e7df26424c95a02` |

Actual authenticated-source replay, Core, public admission, C12 and release
are unrun for this unit. Outer source/inference/lowering pools, runtime value
work, physical profile requirements and the held runtime/Native chain remain
separate open obligations. The original thirteen-row denominator is unchanged.

### Membership hold resolution and composed native-root evidence

The preceding draft hold is historical. Exact source/result review accepted
its demonstrated root-entry resolution at `a674a3e991ce417727daac87af4684da6567c393`,
tree `f3541519c4ffbe3d882ea68fb0eabb5e03d37909`. Main remains `656f545f` at
reconciliation. No earlier failed record is rewritten or reclassified.

First, Effects and emitter received the three-field descriptor-schema guard
already present in C7. At `c71da7c3`, tree `92ac0fad`, the two unchanged failing
kernels, minimal old case and boundary passed 9 tests / 1,008 assertions,
natural 0 / 26,147 ms, actual 2 GiB, complete 1,186 stdout / 0 stderr bytes.
Receipt SHA-256:
`92f310c4483dd44e52bde4079c892ccb6ea409027338c8c751be926d42efb646`.
This proves the tested zero field/sequence reads, not zero count callbacks or
arbitrary three-field foreign-map safety. It did not alone close native ingress.

The supported production ingress census then found fifteen definitions and
sixteen direct calls: twelve internal dispatch calls and four external callers
of the three `ksm-domain` roots. Each root now first invokes the existing
canonical `lc-validate-types` on its original arguments. Accepted domain bodies
are structurally identical to their original bodies; original vector arity and
arguments are retained. Native classes, default comparators, view links,
cycles and complete type children are checked before internal selectors or
member access. The walk adds no debit. Failed ingress uses the existing
operand-entry rejection with a defined `work-count` of 2 and canonical
`type-check-reason`; valid domain and budget results remain unchanged.

Former foreign-vector routing/throw positives now use supported native
SubVectors and retain semantic result, exact argument and owning-consumer
checks. Foreign classes receive separate zero-callback negatives. This is an
explicit native-domain strengthening, not unchanged foreign admission or
accounting-only deletion of a positive. Compiler-free conservation passed
2 tests / 21 assertions and binds all three old accepted bodies, only the three
root changes, seven migrated old test vars and two new native-root test vars.

| Actual stage | Original source | Result and boundary | Receipt SHA-256 |
| --- | --- | --- | --- |
| Full native-root and composed PL panel | `90a80f5a`, tree `6090a861` | NONPASS: 18 tests / 2,700 passing assertions / 9 failures / 0 errors; natural 1 / 26,162 ms; actual 2 GiB; complete 4,139 stdout / 0 stderr bytes. All failures belong to one missed duplicate foreign-vector positive | `41f4bb58e6d87f9f5b7ee80e48b48b43f2ab878846df6a2979de30659904d601` |
| Corrected native carrier var plus boundary | `a674a3e9`, tree `f3541519` | 7 tests / 581 assertions; zero failures/errors; natural 0 / 14,017 ms; actual 2 GiB; complete 981 stdout / 0 stderr bytes | `67d1a41476bb3c09cbb7de59dc1866d11db60ad2036ea3b2d553838cc0609e57` |

Both used the unchanged guarded 120-second route with clean exact bookends and
complete cleanup/root dead. Only the duplicate test fixture changed after the
full panel; production, shared inputs and eleven unaffected nonboundary
selector bodies remain identical. Their successful complete-native, five-family
consumer, formerly failing membership and composed PL results retain
`90a80f5a` provenance. The changed var and boundary have fresh `a674a3e9`
provenance. There is no combined assertion total or fresh full-panel claim.

| Native-root successor source/test path | SHA-256 |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | `ea26756f3fb856601c4f9ee7b074a0928549f151dc32b4950104d5283778cbf7` |
| `bootstrap/gravity/src/gravity/effects.gravity` | `63057a87073c7ae1d005bf57f2d416cda14b4cae976d1ec3b2cfc0eda3d4589c` |
| `bootstrap/gravity/p15_s23/emitter.gravity` | `037f4ab9a14b920f71aa266d51e9f03b99bbd3415f25ee83a1dba018dc3cecd7` |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | `b72337873f1ced345a5eb6c6c0ec3942a870e3adb7f7fb1a51418a0cca1b9b98` |

The other two owned test files retain the original PL table's hashes. This root
boundary does not certify arbitrary facts or the earlier Effects context
classification, and does not close the existing membership cost models, outer
pools, whole ACC-05/06, authentication or release. C12 remains unrun.

The first published draft head `fa55744d`, tree `d1714479`, passed all seven
routine jobs in CI run 40611569118; source-Core readiness and experimental
prerelease were skipped. That is the old published tuple, not CI acceptance of
this later source repair. Current-head CI, publication readback and
independent integration review remain pending.

Fresh composed repository gates passed at `71f5f8a393943c6f1406bc4d727f5b6dadba3982`,
tree `6408a80f9f940a4413bf45da6b4c198abf192e25`: boundary/LOC 11 tests / 23
assertions, 2,035-file census, docs 240/19 and roadmap. Natural exit 0 / 2,280 ms,
256 MiB, complete 456 stdout / 0 stderr bytes, clean exact bookends and complete
cleanup. Receipt SHA-256:
`e87b92951c6f89772d0cb4d75f4abfb04e591568654b019abbdc11261c0ecf6f`.
At the same tuple, CI-pinned changed-file lint passed against `656f545f` with
223 unchanged baseline warnings and zero blockers, natural 0 / 14,505 ms,
256 MiB, complete 76 / 0 bytes and clean cleanup. Receipt SHA-256:
`d9fa2949a2c8691e85e976ef8655b0e027af72910e37a02d8ed94eb69944574b`.
Public current-head CI/readback and independent integration review are still
required; these repository checks do not substitute for them.

## ACC-06 LR validator successor - 2026-10-27 UTC

The source baseline is main `9c093bc671036beb097c37e636082172330cfb5c`, tree
`4fb4101eb593a69937cd2f8abeb4db751ab7a8be`. Source inspection finds exactly
three `lr-type-work` definitions and three production calls, one in each
matching `lr-derive`; remaining references are dynamic tests in the same three
owned namespaces. The helpers' `1 + member-count` reservation can reject
otherwise valid relations solely because a caller supplied a smaller budget.
Their `member-work` has no production reader, while the derives consume
`work-count`, relation and the semantic checked-member extent.

The finite successor retires only those helper allowances/reports, including
the three caller debits. Declaration and actual-type native/grammar validation
must precede homogeneous-type hashing, member reads and equality. Existing
semantic `type-check-reason`, Never and checked-member extent behavior survive.
The derive's own source allowance and every full v2 premise/row/source/census
obligation remain. Independent code and retained-result review accepted this
finite implementation at `ccc2702da5895c8bbe7bc62fefd390c629553383`, tree
`6dd6af2eba362c0e964c75d755b9814da61a035d`. Ordinary public CI and integration
remain separate gates; no whole original row closes from these results.

| Actual stage | Exact source | Result | Receipt SHA-256 |
| --- | --- | --- | --- |
| Old allowance baseline | `e2e6b8c0c18b90f9b15f4a8334dc8e19aa843990`, tree `68611686daec70df7e3b3b2b6b2df8f4240a7b0c`; production identical to main `9c093bc6` | 1 test / 39 assertions, zero failures/errors, natural exit 0 / 18,576 ms, actual 2 GiB, complete 105 stdout / 0 stderr bytes | `89b91aafc31fde1f3fabf1be283e1730a3911040fa4764f2e8a4ea6e88330dc6` |
| Three-owner helper/caller and proof controls | `ccc2702d`, tree `6dd6af2e` | 23 tests / 1,186 assertions, zero failures/errors, natural exit 0 / 32,926 ms, actual 2 GiB, complete 3,010 stdout / 401 stderr bytes | `239da57d219c94c9e414603f434339d786677c27a67fe8e9f110b25ae1e348dc` |
| Current C7 large relations | `ccc2702d`, tree `6dd6af2e` | 1 test / 2 assertions, zero failures/errors, natural exit 0 / 60,173 ms, actual 2 GiB, complete 479 stdout / 0 stderr | `06821aa41f7a2aea4378b0b130a6b79189860134aef55fe3f616a3ea71da2aa6` |
| Local strict changed-file lint | `ccc2702d`, tree `6dd6af2e`, versus `9c093bc6` | Existing policy and CI-pinned clj-kondo 2025.10.23: 223 unchanged baseline warnings, zero blockers; natural exit 0 / 16,546 ms, bounded 256 MiB child, complete 76 stdout / 0 stderr | `7a98a9459b2180238af359184fa303dc56da3ed7d755eafa73b3d72472e7e7e0` |

Every receipt has exact clean bookends, complete capture, no timeout/wait/capture
failure, completed cleanup and a terminated root. Semantic jobs used the
existing normal Git-common broker, host-exclusive broker, canonical lock and
unchanged 120-second child bound. The ordinary stderr is only the deliberately
reflected native SubVector cycle fixture warning; it is not empty or a failure.
Local lint used the normal broker and 60-second non-Bootstrap bound. No source,
dependency, lint policy or physical resource limit was changed for validation.

All three old helpers accepted native 1,030-member inputs with budget 65,536
and refused the same inputs with budget 1,030 because their reservation was
1,031. Mismatch and malformed-type baseline results remain separate semantic
observations. The new two-argument helpers expose no supplied allowance or cost
report. Their declarations undergo complete canonical native/type validation
before the homogeneous predicate can hash an element type, and actual types
are fully validated before member access or equality. Invalid declarations,
incompatible actual types and semantic `type-check-reason` remain distinct.
Checked extents remain zero for an empty positional list and nil for identity
or Never; Never cannot bypass an invalid declaration.

The 23 selected variables cover the four new relation/native/graph/source
controls, nine existing C7 LR consumers, two Effects joins/contexts, two emitter
contexts/typed definitions and six boundary variables. Every v2 declaration,
source binding, full premise/row comparison, old/mixed/extra-row rejection and
census obligation stays intact. Native callback controls reject foreign,
opaque, cyclic and malformed-view inputs without invoking callbacks. Visible
native views, unused backing data and inert foreign/cyclic metadata preserve
their established semantics. Nested DAGs pass canonical physical validation
but do not become compatible with an atomic homogeneous List declaration.

The separately invoked C7 large helper allocates only inside its manual body;
no registered test invokes it. It accepted 65,537 matching members with exact
checked extent, then rejected a valid-type mismatch at index 65,536 with the
semantic incompatibility reason. Completed scalar observations were persisted
before assertions. Phase observations were 26,320.305875 and 22,760.221583 ms;
they make no performance claim. Effects/emitter large executions remain unrun;
exact three-owner helper parity and fresh ordinary controls support that
narrower scope. No counts from different jobs are summed into a suite.

Compiler-free reader/interface/conservation controls passed 2 tests / 41
assertions before application. They compare every unaffected definition,
semantic argument order, relation expression, checked extent, output/debit
closure, expected test migrations and three-owner parity. An initial private
control implementation retained both full syntax trees and exhausted its
256 MiB heap before compiler execution. It was replaced by streaming readers
that retain canonical definition strings; the complete checks passed at the
same heap. That preparation failure is not semantic acceptance or a reason to
raise a source/runtime allowance.

| Accepted source/test input | SHA-256 |
| --- | --- |
| C7 engine | `551541720e1da66ea95aaccc05298a23d0681c28cd883d9d8968f89b39a474b6` |
| Effects | `fc3acf26319ec5cde0ec05fcca83b4a5d50841dab2f03edb318db739747e0b7c` |
| Emitter | `e57c8384a5dd056698943b83b6b8584ddd2b8683a1a26949f69b140bc3c22db5` |
| SH08 tests | `fc536d2c3a45a20efe0a9fe9ffc12b4191606fe6f2fab9da1bce5fc8c098bf48` |
| SH09 tests | `47fa4b9c4569dcefe65e4664661dad464e4e346ad3c3959ebf3b29756c876d7c` |
| Runtime iteration tests | `143aa49bbff6fb8917dfefc2690cba40b0bf6f41b6c615b55d2105b03b807da4` |

Fresh main remains `9c093bc6`. Each derive's existing source prefix and budget,
context/inference pools, PL construction and PE/PC ceilings remain open
accounting work. Whole-plan foreign ingress, authenticated source, Core, C12,
public runtime admission and release remain unrun. No held Native/shared
registration or runtime source is imported. Only ACC-02/03 are closed; the
other eleven original criteria remain open or partial.

Fresh repository gates at documentation successor
`59efea4e0cee81ecd8d6f76aa77ee6713d5f078a`, tree
`6a04cbea28c6125a8d2681d8413432e60b08822d`, passed boundary/LOC 11 tests /
23 assertions, the actual 2,035-file source census, docs 240/19 and roadmap.
The normal-broker non-Bootstrap child used 256 MiB and the existing 60-second
bound, exited naturally with zero in 2,418 ms, complete 456 stdout / 0 stderr,
exact clean bookends, complete cleanup and a terminated root. Receipt SHA-256:
`0e03d7a3f852e596941224dd6164f971a9a238e6e8ec6f19c9a5659ec0d0939d`.
This bookkeeping successor changes only the accounting task/evidence records;
all six accepted source/test inputs and the gate's consumed normative docs,
tooling, roadmap and Clojure sources are unchanged. Receipts keep their
original identities; no semantic or gate rerun is implied by this note.

The preceding PR506 finite caller-interface increment is integrated at that
baseline. Normal squash merge `9c093bc6` has exactly reviewed candidate
`5ef17137`'s tree, and its branch is retained. Corrected PR CI `40597498166`
passed all seven routine jobs. Actual main run `40598401746` passed the same
seven routine jobs plus the source-only prerelease. Only the explicit
Core-unavailable job is nonpassing: `C6-VERIFY/:actual-selected-current-reader-products`,
with no genuine compiler execution attempted. Independent source/result and
final integration/readback review accepted the finite unit. The original
semantic NONPASS receipts, first failed CI and narrower C7 large-owner evidence
below keep their original provenance. No whole ACC-06, authentication, Core,
C12, language-runtime or release acceptance follows.

## ACC-06 caller budgets and result plumbing - 2026-10-27 22:21 UTC

Independent code and retained-result review accepted this finite implementation
at `3bb8e754384ec80d78515ab043f7c3314e787d29`, tree
`ecbcef76f4368a5ac89b0e57e580b674c1e67e1d`. Current-head gates, public CI and
normal integration remain separate acceptance stages.

The canonical three-owner validator now takes only the original arguments and
returns status/reason. Its 115 direct calls in 109 containing functions, 39
source-cell publishers and the escaped Vector-first consumer are migrated.
Nine pure PE selected/link/check APIs and their 14 calls also lose their
unused budgets, carried zero counters and cost reports. Exact call checks
compare every semantic argument against the original ordered arguments with
only the final budget removed. A private preliminary rewrite with eleven
misremoved arguments was rejected before application or compiler execution;
arity-only checks were insufficient. The strengthened checks pass.

All four canonical/pure-check families have identical three-owner forms after
owner-name normalization. Reader/interface controls passed 2 tests / 25
assertions; the stronger argument/order/output/parity controls passed 3/41.
These controls execute no compiler and are distinct from semantic acceptance.

Source cells accept the exact status-only shape, with the existing semantic
head-proof variant confined to query validation. Old cost fields and unknown
fields reject; no field stripping or old-wire compatibility is provided.
Descriptors, identity requests, call facts and rows are freshly rederived and
compared in full. Semantic family identifiers stay unchanged under existing
source-bound invalidation. Checked extents, source/callee/definition binding,
complete type validation, Never placement, Map parity and native/callback/
cycle controls are retained. Actual independent construction/source costs
remain explicit unfinished work, not replacement LC charges.

| Actual stage | Exact source | Observed result and capture | Receipt SHA-256 |
| --- | --- | --- | --- |
| Initial smoke | `2690745b25bc8599dc5c54f278d9c03ce5654479`, tree `76bd7374b9d0502a02fc5a6b54bd4aa7341d3162` | NONPASS: 20 tests, 1,414 passing assertions, 3 failures, 0 errors; natural exit 1 / 23,302 ms; 3,342 stdout / 394 stderr bytes | `43e20f2fe0a54169f332c264977da5d7af72f1882b04ad710e39f4828cf3e9f8` |
| Two changed smoke vars plus boundary | `bb377d23d82de5ff27db8cd69d01433a24bcf74e`, tree `169decf9b60c52a1fecc65603173a22b47c2840d` | 8 tests / 111 assertions, zero failures/errors; natural exit 0 / 19,234 ms; 1,156 stdout / 0 stderr | `06c28935b1bb5ee08fc0d2b5ff6837e71260781273f5b842db434f8779a76326` |
| Family composition | `6ebf029ee0f7367caf0bc2eab0d182c7d1246941`, tree `953c5dc2601979ac13d96e3babc6546b0cbce74f` | NONPASS: 27 tests, 2,840 passing assertions, 23 failures, 0 errors; natural exit 1 / 34,908 ms; 8,182 stdout / 0 stderr | `122d0249120cc1626f70cb8f68829d0acbd282cfd415818b95e9e993590746f5` |
| Two changed family vars plus boundary | `3bb8e754`, tree `ecbcef76` | 8 tests / 376 assertions, zero failures/errors; natural exit 0 / 18,435 ms; 1,202 stdout / 0 stderr | `a5139d8e58cf22c3d4317b7aed0b0742afe7e9180290d9bde08cc93ec144f85c` |
| Three-owner PC composition plus boundary | `3bb8e754`, tree `ecbcef76` | 9 tests / 130 assertions, zero failures/errors; natural exit 0 / 26,737 ms; 1,278 stdout / 0 stderr | `9c6a185117cdc9d6fc330c6ce9c04a5eceba9157987ff24519aa5245691ed407` |
| Current unary C7 large owner | `3bb8e754`, tree `ecbcef76` | 1 test / 3 assertions, zero failures/errors; natural exit 0 / 87,077 ms; 648 stdout / 0 stderr | `277e3dcd44389d0632309f1ec2f32dd4c24593a57b02f54f1563ff65339ed9c6` |

Every semantic receipt records actual 2 GiB heap, complete capture, exact clean
bookends, cleanup completion and root termination under the existing normal
Git-common broker, host-exclusive broker, canonical lock and 120-second child
watchdog. The first smoke stderr contains only the controlled reflective
logical-List fixture warning. The reviewer inspected actual records and did
not repeat compiler execution.

The smoke failures were obsolete LC debit expectations in two existing test
vars. Their correction preserves full result equality and the actual remaining
construction charge. Twelve unchanged non-boundary selectors retain original
`2690745b` provenance; their consumed definitions/fixtures do not enter the
later PE/PC source changes. The two corrected vars retain `bb377d23` provenance.

The family failures were twenty-two emitter positives in the new matrix, whose
fixture confused remapped node IDs with preserved binding IDs, and one obsolete
partial-debit expectation. Test-only `3bb8e754` supplies the correct binding
identity to emitter locals/parameter types and keeps the full independent
reservation report. Its actual matrix positives and negatives now pass. The
earlier vacuous emitter negatives confer no acceptance. Nineteen unaffected
non-boundary family selectors retain `6ebf029e` provenance. Fresh boundaries
are separately recorded. Neither original NONPASS is promoted to a fresh
whole-suite pass, and counts from separate runs are not summed.

PC composition executes C7, Effects and emitter parent/link/project consumers,
including fresh and reused Effects parents and full-row negatives. The current
unary C7 large check accepts 65,537 operands and the 67,585-node pending family,
then rejects its last invalid leaf with `unsupported-type`. These are fixture
extents, not a production work report or a progress percentage. Each completed
scalar observation was saved before its assertion. Only C7 was executed at
that scale; Effects/emitter large executions remain unrun, with exact walk
parity and fresh ordinary three-owner controls supplying the stated narrower
acceptance. Earlier PR504 large results retain their historical source identity.

| Accepted implementation input | SHA-256 |
| --- | --- |
| C7 engine | `ca7d9874b4d8fe060c7399b73de165bfcc3ea0a2f4457a2a0a9817702aea7b98` |
| Effects | `60230bcdc6b441584403d1f783d913e829d05a0cb40f5086d613c2b9ee8867d9` |
| Emitter | `a00b6a2caf40586745967d10e427209208910e85b40746a7e4d3120dd2bbf097` |
| SH08 tests | `2ec464c76f7dd66b97431eb7cd691980014c8808a109e3e8eb70749d8ee3ab01` |
| SH09 tests | `6ac257dd8dfec9082ebb54ddb0fbb1817a1970a28e26a718b6518a014f53d620` |
| Runtime iteration tests | `f9e615b30a60d8770d0e0c4632b2bc9bdd53336e521fa5c01cf7cba159405ebf` |

Main `637d5f070d3e5546f24fabe13407728da03b2bd2` adds only
`docs/end-to-end-contracts.md` after the integrated PR504 base. Normal merge
`f9c6d6af586905607ac6c36d94c0f28445cc74d4` preserves all semantic inputs;
later family, correction, PC and current large receipts include that main.
No Native registration or held runtime branch is imported. Public source
authentication, Core, C12, release and whole ACC-06/accounting acceptance are
unrun. PL/Set construction, LR member/source allowances and existing PE-part /
PC-link ceilings remain unresolved accounting, not relabeled safety guards.
ACC-02/03 remain the only closed original rows; eleven remain open or partial.

Fresh repository gates at documentation head
`666efaa5ca25a71e4d19260e700fe8d6b3b27b3e`, tree
`1cff8823ed82832b01d91401efd6082a52229f96`, passed LOC 5 tests / 12 assertions,
the actual 2,035-file source census, docs 240/19 and the full-language roadmap.
The non-Bootstrap 256 MiB child ended naturally in 2,218 ms, complete 396-byte
stdout and empty stderr, exact clean bookends and complete cleanup. Receipt
SHA-256: `88d86de0db0f7afae0c7edb5d0ade7ac46929582f12a008e124e2c000fa0f517`.
Exact integration preflight against `637d5f07` is all-pass with no diagnostics.
This follow-up changes only these two accounting records. LOC validation reads
Clojure sources; document validation reads normative docs/tooling; roadmap
validation reads its own roadmap. None consumes these accounting records, so
their passing evidence retains its original head rather than being rerun or
relabeled fresh. Final source, CI and integration review/readback remain open.

### Published CI and test-only correction

Draft PR506 published `e4d3c80c6664ef00a6a05ddc53a37902f67ab024`, tree
`0b6aaca6fb61cd2ed1fe5b4bee359ee5e996236d`, against `637d5f07`. Its automatic
run `40595990230` completed with six routine jobs passing and static CI failing.
The actual changed-file lint report contains nine blocking warnings: five
redundant nested lets, three redundant dos and one unused visit-count binding.
The 223 unchanged baseline warnings were separately recognized by the existing
lint policy. No lint configuration or policy change is made.

Test-only correction `bbfcd9f3d1841d5ba62c470d6b07319878d51877`, tree
`8e6b2d4362c439895cfa3fea2611ccc7b12671b8`, changes exactly the three existing
test namespaces, 25 insertions and 25 deletions. The redundant wrappers are
removed with binding order, assertion order and all five Vector-first cases
preserved. Independent exact-diff review accepted the correction. Structural
reader/normalized-form controls pass 1 test / 6 assertions. Production source,
shared runtime implementation, contracts and dependencies are unchanged, so
the original semantic receipts above retain their original provenance; no
compiler rerun or globally fresh semantic pass is claimed.

Fresh bounded non-Bootstrap gates at `bbfcd9f3` passed boundary/LOC 11 tests /
23 assertions, source census 2,035, docs 240/19 and roadmap. The child exited
naturally with zero in 2,399 ms; complete stdout was 456 bytes, stderr empty,
exact bookends clean and equal, cleanup complete and root terminated. Receipt
SHA-256: `bef34fb6ad4febfd3ddbfb86b8bbc7c63dadccd5a5698f19bf3d2b572c16cf7f`.
The first published CI failure remains a failure. Fresh corrected-head CI,
exact readback and normal integration remain required; no original ACC row
closes from this test correction.

## ACC-06 finite intrinsic-limit acceptance - 2026-10-27 13:59 MDT

Source candidate `cedbfd3b785d9f50676c9eb40a3fe1a9da8e0334`, tree
`39d9e9d65f03515624a968be40e95df1f65edd40`, completed 29 tests with 3,255
passing assertions and one failure, natural exit 1 in 37,396 ms. Its receipt
SHA-256 is `ce0df39d42aaa9e8b37e32846218c19f1643736af8cf300a61843b9096aa62d5`.
This batch remains NONPASS. The sole failure was an existing VectorRest oracle
that still rejected a well-formed 1,025-member Vector solely at the removed
recursive width cap. All new native/metadata/view/cycle/grammar tests, actual
LC/PL/LR consumer tests and selected Effects joins passed in that batch.

Test-only correction `b3172528bf22a73dc83397d8aa26892976a4c990`, tree
`72989337f13683a565a81bba48d00e97b3e78179`, adds that input to the existing
complete VectorRest output/inference/descriptor loop and replaces the obsolete
refusal with a last-member unknown type and its exact type diagnostic. No
production code or shared helper changed. The changed var plus six boundary
tests passed 7/254, natural exit 0 in 15,352 ms, complete 1,007-byte stdout and
empty stderr. Receipt SHA-256:
`7cbca6c7e881d7f56e916008eaaf4445e9ea7d4fb7c3d4262823f78609826fb4`.
Twenty-two unchanged non-boundary semantic selectors retain `cedbfd3b`
provenance; six boundary tests are fresh at `b3172528`. This is composed
acceptance, not a summed or globally fresh 29-test pass. The original batch's
393-byte stderr is only the controlled reflective final-field fixture warning.

Each owner then completed its own manually guarded batch on exact `b3172528`:

| Owner | Actual selected result | Natural elapsed | Complete stdout/stderr bytes | Receipt SHA-256 |
| --- | --- | --- | --- | --- |
| C7 | 1 test / 3 assertions, zero failures/errors | 88,305 ms | 536 / 0 | `a2d52ce64e9ed133334ac8581dd54ef4a158f3f422c341c8a4a8de28609bc1ae` |
| Effects | 1 test / 3 assertions, zero failures/errors | 90,687 ms | 561 / 0 | `7dc4b29af98e7097d142ae8db0a1e57291938fdefcaf1b1f5a4473f363b7663d` |
| Emitter | 1 test / 3 assertions, zero failures/errors | 89,428 ms | 560 / 0 | `8daaf2740b99e8f2387b4f14239dc0e6933ae950b859a191a3382e14e90956bd` |

Every receipt records actual 2 GiB heap, natural exit zero, complete capture,
exact clean bookends and complete cleanup/root termination. Completed scalar
phase observations were persisted before assertions. Each owner accepts all
65,537 primitive operands and the full 67,585-node pending family; a final
unknown type rejects with `unsupported-type` at visited count 67,585. These
are separate owner receipts, not automatic/generic CI execution authority.
Independent review inspected source, drivers and actual retained records;
the reviewer did not run compiler semantics again.

| Accepted source input at `b3172528` | SHA-256 |
| --- | --- |
| C7 engine | `178db8bd722e905ef8a2073717a367678de26787e280f80e8844feba6d82148d` |
| Effects | `ed37e584222606abd7fe27e94b73b7e428b13c6f162ecb1b2a3bdb3fabcf3480` |
| Emitter | `1ae3a25e376b5797bb3ed9fb6218811140707025fd29816c73dabee23dfa2f83` |
| Existing SH08 namespace | `b8c1e1dd06bb035e468a8a7bf0246b8584993cd563c079884988e4f887919432` |

Normal current-main composition `bef14d553d3c886a13c0d3deac38b869bdefe4c2`,
tree `d6130be19704151b3e422d3798f65a3d050b5b42`, includes main
`190bc35c7c4dbb4e80053813a2372048c8863d9e`. All Gravity, Bootstrap production
source, SH08/SH09 bodies, contracts and `deps.edn` are unchanged from accepted
inputs. Incoming changes concern CI lint context and two compatibility-suite
batch/cache policies. The explicit-command owner wrapper uses unchanged
process APIs and guards; it invokes neither optional child-cache admission nor
normal batch grouping. No unchanged semantic run is repeated for those deltas.
Fresh repository gates at documentation successor
`105cc4deaf2d623934470377dc45028d6a19c7ec`, tree
`2608e19cb663621e0bf71cce16c2b51055a943d9`, passed 5 LOC tests / 12 assertions,
actual repository LOC census 2,035, docs 240/19 and roadmap. This 256 MiB
non-Bootstrap child exited naturally in 2,104 ms, complete 396-byte stdout and
empty stderr, exact clean bookends and complete cleanup. Receipt SHA-256:
`0b5ba30f3b91c5cf1323a38bf373ee98f698cc90ef9ef366627dfe1abf7868dc`.
PR504 subsequently integrated the exact independently reviewed tree at
`6e0f6b849d554f5b3cc9aa01701778ed25194c60`, tree
`01e78a324b90038f4b15206a3d5cb7b12ee0dab2`. PR run 40574699092 passed all seven
routine jobs. Main run 40575952086 passed all seven routine jobs and source-only
prerelease; only the explicit unavailable Core readiness gate failed with
`C6-VERIFY/actual-selected-current-reader-products`, without genuine Core
execution. This later bookkeeping correction changes no semantic inputs.

This closes the integrated finite intrinsic used/pending/depth/width removal.
Supplied budgets/work-count APIs and their 115 caller/debit interfaces were
unfinished at that integration; the later caller-interface record above
tracks their successor separately. Outer constructor policies and full ACC-06
remain unfinished. No public source authentication, C12, release, Native registration
or whole-accounting completion is claimed. ACC-02/03 remain closed; eleven
original criteria remain open or partial, with the historical unmeasured
50-percent estimate unchanged.

## ACC-06 intrinsic recursive validation baseline - 2026-10-27 13:44 MDT

Exact base `00cdaf5513d356a0c58ce02ab263e4765b713eed`, tree
`d75f42d1a6c445423efa7f12699ee667683f9a25`, completed one read-only old-source
batch: natural exit zero, 47,922 ms, actual 2 GiB heap, complete 4,642-byte
stdout and empty stderr, exact clean bookends and complete cleanup/root
termination. Receipt SHA-256:
`73811130a9f66fffc1c6f2a10fac9d6d56018e7e113c5a9c55db01994797ef54`.
Independent retained-record review accepted diagnosis only, not candidate
behavior or whole ACC-06 completion.

Forty actual calls produced 43 durable observations. All owners accept depth
128 and width 1,024 but reject 129/1,025; all reject the prospective pending
family. C7 rejects 65,537 primitive operands at its intrinsic work ceiling
despite supplied budget 100,000. Cyclic type DATA stops at the old depth limit,
which is not native cycle evidence. Foreign type maps are accepted after
count/sequence/lookup callbacks. Foreign and cyclic metadata remain inert,
with zero metadata callbacks. Nil, native List/Cons and visible SubVector
ordinary cases are accepted. No root-spine cycle was executed on the old
unbounded normalization path.

Candidate source removes the intrinsic limits coherently from all three LC
owners, retaining supplied-budget/work-count APIs and all caller debits as
unfinished work. It uses existing Meta/JVM native headers and identity, not
the held hosted Native API. Small registered controls cover grammar and
physical representation; large visited/pending/late-invalid cases are invoked
only through a manual owner helper under the established guarded route. No
candidate semantic result is claimed by this baseline. Preliminary fixture-only
controls initially passed 1/21 without Bootstrap; the amended List fixtures
passed 1/24, with a controlled reflective final-field fixture warning.
Form/resource-registration controls passed 1/87 without
Bootstrap. These are fixture/structural evidence, not compiler acceptance.

Independent preliminary review identified that the existing native header uses
`PersistentList.next()`, not a raw backing-tail accessor. The candidate therefore
checks visible count/spine coherence and identity cycles. A count-one List's
hidden raw tail stays inert, including foreign or cyclic DATA; no total
validation of arbitrary reflectively corrupted JVM internals is claimed. New
controls distinguish malformed visible counts from intentionally hidden tails.

## PR359 terminal integration and preserved-source disposition

PR359 merged `00cdaf5513d356a0c58ce02ab263e4765b713eed` with exact independently
reviewed tree `d75f42d1a6c445423efa7f12699ee667683f9a25`. The accepted source
batch below remains at its actual `0c64757d` provenance; subsequent documentation
and current-main reconciliation preserved its consumed source inputs. Final
repository gates passed 11/23, LOC, docs 240/19 and roadmap. PR run 40563266217
passed all seven routine jobs. Main run 40564406756 passed those jobs and
source-only prerelease; only the explicit unavailable Core gate failed at
`C6-VERIFY/:actual-selected-current-reader-products`, without genuine execution.
No failed gate was rerun or promoted.

All retained unique historical source is explicitly owned in open draft PR502,
unchanged `6a4f87f013d0076b81aba9e0667650e70d28e635`, tree
`f8440ffcf2d2fa7e2f898178ee299ac64ef59d32`. That draft requires a reviewed
integration/incorporation/rejection or separately owned implementation-draft
disposition for every unique delta before terminal closure. Historical source
preservation supplies no acceptance credit and does not duplicate the held
runtime implementation drafts. No original ACC row closes from this finite
PR359 increment; ACC-02/03 remain the only closed original rows.

## PR359 finite reconciliation and preserved work - 2026-10-27 12:39 MDT

Finite source `0c64757dcee79d41272dab8486ce042adad93106`, tree
`2c2ac4199c7bc57fca8717324f1f282e368f36b4`, passed its current-main candidate
batch: 21 tests / 1,644 assertions, zero failures/errors, natural exit zero in
22.062 s, actual 2 GiB heap, complete 2,595-byte stdout and empty stderr.
Exact clean bookends and complete cleanup were recorded. Seven new normalized
three-owner tests, eight actual Effects kernels and six language-boundary
tests were selected explicitly. Independent exact-source and retained-result
review accepted the finite removal and private alias routing. Full descriptor
and nested proof equality, rebuilt stale-field rows/facts, source/type/ledger
tampering, public fallback, late opaque validation, Never and construction
effects all remain effective. The receipt SHA-256 is
`0ab80bbdb1dcbd70de3e0f69a081f0fbfaa9c68d570cb2d180d715f4ebbce53d`.
This is a fresh batch, not reuse of historical PR359 tests. Final repository
gates, exact public readback and normal integration remain separate steps.

The read-only current-main baseline receipt SHA-256 is
`fd4d88595fee341f403ff1b3cb49d381a10243f61b12585a37d8cd4b3373e42e`.

| Tested input | SHA-256 |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | `aee721506668fbada2af2d77617117550fc50228bb3d694419b73ad05823d0e7` |
| `bootstrap/gravity/src/gravity/effects.gravity` | `de7090d503549239c27ef22d7f21c0c71e06f5036de49d3f47dcdfae9d1d6729` |
| `bootstrap/gravity/p15_s23/emitter.gravity` | `1583132ef8fefcdcec67687699f6b986db806cb062e1160a9ecdad96a402a863` |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | `f21a1e57354e9e9c0a4fff556041769d1f7083c12616357276ffe103be07efd8` |

Repository gates at documentation successor `7647b2f7` passed 11 tests / 23
assertions, the actual 2,035-file LOC census, all 240 docs / 19 phase indexes
and the full-language roadmap. This 256 MiB child exited naturally in 2.399 s
with complete 456-byte stdout, empty stderr, clean bookends and cleanup.
Independent review then accepted normal reconciliation `b6ece097`, tree
`16f2d7da`, with new main `490b7b57`. The fourteen incoming paths are crosswalk
and dependency documentation/tools; all Bootstrap, compiler/runtime, test,
broker/runner, contracts and `deps.edn` inputs consumed by the semantic batch
are unchanged. Thus the 21/1644 receipt retains its original `0c64757d`
provenance without another semantic run. Fresh final repository-scanning gates,
exact preflight, public readback and routine CI still precede normal integration.

The original draft is preserved at `6a4f87f013d0076b81aba9e0667650e70d28e635`,
tree `f8440ffcf2d2fa7e2f898178ee299ac64ef59d32`. Its common base with current
main is `cfb499405450d479955fa70daa0f14dfa3233ee8`. The authored delta has
29 paths; a direct comparison to current main includes unrelated incoming work
and is not a safe patch. Normal reconciliation starts with main `bffefedf`,
tree `ec9e272e`, preserving the original draft in ancestry and retaining its
branch. Independent inspection confirmed that the prepared index and worktree
exactly matched main before selected edits. No current-main source was lost.

The finite selected outcome is exported ANE/BKA/PMK and four-variant PL
descriptor accounting removal across all three owners, plus exact private Map
constraint/call-fact alias routing. It is not the whole accounting-removal
backlog. Current main already contains the separately integrated ACC-02 guard
and Map/List repair and ACC-03 LR migration; they are not replayed for credit.

Every original authored path has the following disposition. All unselected work
remains **preserved and unintegrated**, not accepted, discarded or made optional.
Accounting retains responsibility under the existing ACC IDs and normal actual
owner acceptance. Historical passing and failed receipts keep their old source
identities; none accepts this new current-main composition.

| Original authored paths | Finite disposition and residual obligation |
| --- | --- |
| `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`, `bootstrap/gravity/src/gravity/effects.gravity`, `bootstrap/gravity/p15_s23/emitter.gravity` | Start from current main and port only selected descriptor fields and private aliases. Preserve other unique DSE, private entry/source proof, predicate/query/construction, verifier, quoted-data, inference and recursive-validator changes at the original head. Those remain original ACC-01/04/05/06/07/08/12/13 work with complete source/type/effect/proof and actual consumer acceptance required. DSE is a full absent feature, not a two-field edit. |
| `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | Retain all current-main tests. Port selected normalized ANE/BKA/PMK/PL regressions with prefixed helpers. Other unique old tests remain preserved; old unmetered depth expectations are not imported into still-metered current-main helpers. |
| `bootstrap/clojure/test/gravity/self_hosting/descriptor_reconstruction_test.clj`, `prepend_premise_test.clj`, `private_map_route_contract_test.clj` | Selected complete descriptor/proof, alias, typed-call and stale-field controls move into existing SH08 ownership. DSE and any remaining old-only assertions stay preserved; no new orphan test namespace or metadata writer is introduced. |
| `bootstrap/clojure/test/gravity/self_hosting/compatibility_premise_test.clj`, `list_return_premise_test.clj` | ACC-02/03 have separate current-main closure. Preserve remaining old test differences until exact equivalence/disposition is established; do not replay old fixtures as current acceptance. |
| `bootstrap/clojure/test/gravity/self_hosting/list_construction_contract_test.clj`, `list_query_contract_test.clj`, `predicate_descriptor_family_test.clj` | Preserved-unintegrated schema/source-family work under ACC-04/05/06/12; requires coherent actual owners and their positive/negative acceptance before integration. |
| `bootstrap/clojure/test/gravity/self_hosting/sh01_checked_bare_set_equality_mixed_ledger_test.clj`, `sh01_list_set_mixed_ledger_test.clj`, `sh01_map_keys_mixed_ledger_test.clj`, `sh01_stage2_runtime_iteration_test.clj`, `sh09_body_effect_test.clj` | Preserve old-only DSE, conversion/query and mixed-ledger feature acceptance. Current-main versions remain authoritative; all corresponding source/runtime/type/effect joins must reconcile together under ACC-04/09/12. |
| Ten files under `bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/`: the `.gravity` and `.qst` pairs named `compact-source-proof-interval`, `compact-source-proof-minimum`, `function-verifier-qualified-map-get`, `function-verifier-work-ledger`, `local-runtime-bare-set-equality` | Preserve all five pairs unintegrated with their original source/verifier/DSE obligations. No fixtures are claimed consumed or accepted by this finite descriptor change. |
| `specs/accounting-removal/tasks.md`, `specs/accounting-removal/evidence.md` | Use current main as sole task truth and record this reconciliation, finite PR checklists, source-pinned evidence and residual work here. Historical old records remain accessible at the original head. |

The read-only baseline on clean `bffefedf` used unchanged guarded 2 GiB / 120 s
execution and completed naturally in 21.177 s, with 159 stdout bytes, empty
stderr, exact clean before/after identities and complete cleanup. Actual heap
was 2,147,483,648 bytes. Five ANE/BKA and sixteen PL cases produced matching
complete three-owner descriptors with the exported cost fields still present.
Thus this current-main batch removes bookkeeping, not a newly demonstrated
descriptor-parity defect. Earlier defect evidence remains historical. PMK,
the selected removal and current consumer negatives still require candidate
acceptance. Genuine source authentication, public B47, C12 and release were
not executed.

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

## PR509 exact integration readback - 2026-10-27 MDT

Normal squash integration completed at
`4a998729ec03513b137d7b40d4bb3d8be285a9f4`, with parent
`4e443579a95e3273c7b3cd4b931ff84eb6acbe46`. Fresh remote fetch and complete
tree comparison confirm tree `d07eb74722f8bd43b5bdcb6adc2c1bee1f71c3e6`,
identical to independently reviewed head `a4504d43`. The branch is retained.
All consumed source/test inputs remain those of accepted semantic source
`a674a3e991ce417727daac87af4684da6567c393`, tree
`f3541519c4ffbe3d882ea68fb0eabb5e03d37909`; the intervening reconciliation
added only the incoming MIR task record. No semantic rerun was claimed for it.

Independent exact-source, retained-result, outgoing-public-content and
current-main integration review accepted the finite helper/native-root scope.
Literal preflight was clean, all-pass and had no diagnostics (receipt SHA256
`139e7658b3341007b4e28d8b951acb1aea33a7602507d9b88d3c2760857e963d`).
Reconciled-head CI run `40618144784` completed successfully with all seven
routine jobs passing; Core readiness and experimental prerelease were skipped
(receipt SHA256
`724413a0c1ace48ff0147fa13a4044d9f9b952e6210b1b9d40eaff458bbb888a`).

Main-push run `40619016852` is terminal: seven routine jobs and the source-only
experimental prerelease passed. Its overall conclusion is NONPASS because the
explicit unavailable Core-readiness stage exits with
`C6-VERIFY/:actual-selected-current-reader-products`. The actual failed log
states that no genuine compiler execution was attempted. This is not a new
compiler failure or source/Core acceptance (status receipt SHA256
`b5f795ed1762477bf3495a072f0928040bd6aa95ba9cf4fcba7ad3d80b94b4c2`).

The original failed/truncated helper and consumer batches, corrected-var
results and reused unaffected selectors keep the distinct identities recorded
above. Nothing is summed into a fresh whole-panel pass. ACC-05 and ACC-06
remain open as original criteria; runtime value-work policies, outer pools,
whole-plan ingress, authenticated-source replay, Core, C12, public admission
and release receive no acceptance from this finite merge.

## PR518 insertion descriptor integration - 2026-10-28 UTC

PR518 merged as `1e784ac42b13a05cfcd0be35072c70239098d103` over
`d20c9adb648bf26e3d648e296683fe0203271486`. The fetched merged tree
`61b636e8b99808ea3e5c55878d80a62228bce59e` exactly matches reviewed head
`7fe81d4cf43ab78ede1055ad6d8e9bcd25d9bf36`; the source branch is retained.
Independent source, test, retained-result and integration review accepted this
finite nine-branch descriptor change. Genuine allocation/storage, type,
member, source, effect, profile and complete descriptor/fact equality checks
remain. Tests require rejection of coherently reconstructed stale accounting
fields and tags in the selected owners and consumers.

The focused owner batch at the reviewed head passed 20 tests and 2,958
assertions, with zero failures or errors, natural exit zero in 33.771 seconds,
actual 2 GiB, complete capture, identical clean bookends and completed cleanup.
Its receipt SHA-256 is
`855be07d766f8c2e81e823ff0526a0075a2b9c0c752a307853808ad51ec96f83`.
The separate compiler-free source-conservation controls passed 2 tests and 12
assertions. Final compiler-free repository gates passed LOC 5/12, 2,035 source
files, docs 240/19, roadmap and changed-file lint with zero blockers; their
receipt SHA-256 is
`8f245b3ece870eee60077a01492e43c53a0561f4c47da22e9b766a8e3f69f8cc`.
A sandboxed first gate attempt failed at broker admission before its child
started; that result is retained separately from the supported successful run.
PR CI `40626387706` passed all seven routine jobs; Core readiness and
experimental prerelease were skipped. Main-push CI `40627419194` was queued at
the time of this record; no result is implied.

Two changed authenticated-composition assertions were not run. Runtime Set
value work, outer accounting pools, authenticated source, public runtime, Core,
C12 and release remain outside this finite result. ACC-05 remains partial; no
other original criterion receives closure credit.

## ACC-13 C3 finite Guest comparison

At local candidate `5b5f0a05efb1453b479d6ab967e42fd30b0663a6`, tree
`0c9b0fa57d98d0d7f9554e5a6cfd06bb91ee1f82`, the reviewed C3 source change
replaces the duplicate lookup of a validated SHA-256 string with membership in
the graph's already-maintained depth map. The original prior-reference checks
remain. The owner compiled the old and changed Guest plans and compared their
complete reports using explicit normalized data, actual builder and digest
resolution and fresh resolved verification.

The independently reviewed original run exited naturally in 26.244 seconds
with actual 2 GiB, complete captures, equal clean source bookends and completed
cleanup. It recorded 48 full old/new result comparisons, 12 finite ABBA
observations and four rejected foreign-input cases with zero hash, equality,
stringification or metadata callbacks. Its 107-byte stderr contained a Clojure
namespace replacement warning; it was not empty. Receipt SHA-256:
`40575aeff708a5f19b529465e4994789351f9a59fef0b97255d239a02e78bfcd`.
The independent result review is limited to this retained comparison.

The earlier PR512 head was `9a362cbf29c179f47bf80108d47e234d36033ec1`.
Automatic approval review blocked the later candidate push before execution;
after new relevant authorization, the original owner used the same supported
route successfully. PR512 now publishes the reviewed `5b5f0a05` candidate as
a draft, without integration. No authenticated C2/Syntax binding, genuine C5,
whole-source speedup, public integration or ACC-13 completion follows from
the normalized comparison. Existing B51 authority, source pin and C12 holds
remain separate.

## PR517 finite B51 test artifact restoration

PR517 merged as `068de7405b6096d62e1b1636b973e0f250bdc1a6` over
`a394d4f04313414ee0d397882e2fd2cb0c4b47c7`. The merged tree
`936e808632488816fd9185f10728054f75d6fdee` exactly matches independently
reviewed head `f00ac77455cfdd3419c2e6ee8bafc07c449a67d2`; its remote branch
is retained. The single-file restoration preserves 121 test names and 1,584
ordered assertions, with an effective conservation mutation control. The
fresh composed fixture and host UTF-8 selection passed 2 tests and 155
assertions, natural exit zero in 9.054 seconds; strict changed-file lint and
seven routine CI jobs also passed. Core readiness and prerelease were skipped.

The original truncated source, authentic historical restoration, intervening
failed lint CI and corrected test candidate retain their own provenance. The
full 121-case suite, current seven-shard assembly, public B51 roots, source
authentication, Core, C12 and quota-source migration remain unassessed. This
finite test artifact repair does not close ACC-12 or ACC-13.

## PR521 BKA anchor-width integration

PR521 merged as `38daf99ecd4abf1c5904d03e3a9a4cdec72af2a3` over
`068de7405b6096d62e1b1636b973e0f250bdc1a6`. Its fetched tree
`17a65169831d60c20938a918ea11b8c146f1a0f0` exactly matches independently
reviewed head `f74e4b357782597118f4a74d82e9605f747c7815`; the branch is
retained. The three producer guards remove only the upper anchor-width cap;
the nonempty, exact-schema and full keyword/type checks remain.

At the original source candidate `47bce8564e698b6f74b7e2e701564447206b10f6`,
the independently reviewed normalized owner/consumer panel passed 12 tests
and 448 assertions, with zero failures or errors, natural exit zero in 20.627
seconds, actual 2 GiB, complete capture, clean identical bookends and
completed cleanup. Receipt SHA-256:
`7b18760a8d44ae9efd5d6eae5dc5fc8e4a2248ccaf61832fc8baecc4bcdbacf7`.
The earlier old-source baseline remains a separate 1-test/9-assertion result.
Reconciliation introduced the unrelated SH07 test restoration while conserving
the consumed source, selected tests, contracts, dependencies and lint tooling;
the semantic result retains its original candidate provenance. Reconciled
repository gates passed 11 tests/23 assertions, the source census, docs and
roadmap; exact-head CI `40630208940` passed seven routine jobs, with Core and
prerelease skipped. Independent review accepted the merge and exact tree.

This is a finite anchor-width result, not arbitrary foreign-container safety,
authenticated source, runtime, C12 or whole ACC-04 acceptance. BKA header
accounting, outer pools, runtime policy, PMK and ANE remain open.

## PR511 private collection chain integration

PR511 merged as `0ea44f0c0fe0c5e89ecbbed8148552810164b7dd` over
`38daf99ecd4abf1c5904d03e3a9a4cdec72af2a3`. The fetched main tree
`e89fd1934a641b2b029f02cb0663524ac1f93455` equals independently reviewed
candidate `afb7c4560b3441303a4c1aee34d43073756fc61f`; the branch is retained.
The six C7, Effects and emitter changes remove the paired 128-link admissions
while preserving canonical-ID cycle checks, full source/link schemas,
mutation and immutable-binding controls and fresh proof reconstruction.

The post-PR521 composition at `afb7c456` passed 14 tests and 266 assertions
with natural exit zero, actual 2 GiB, complete capture, clean identical
bookends and completed cleanup. Its separate repository gates passed 11
tests and 23 assertions plus the source census, docs and roadmap. Earlier
old-source baseline and three owner results retain their original source and
receipt identities; they are not relabeled as new executions. Exact-head CI
`40632122067` passed all seven routine jobs, with Core and prerelease skipped.
Independent review accepted the exact merge and candidate tree.

Prior ready-transition denials remain historical; the owner subsequently
completed the same supported transition following new authorization. This
increment does not establish full typed long-chain, authenticated-source,
runtime, Core, C12, release or whole ACC-06 acceptance. Independent work
pools and other private collection limits remain open.

A later PR513 compiler-free metadata gate rejected the new
`gravity.self-hosting.private-collection-chain-test` namespace as an unknown
non-slice namespace. Its owners are coordinating the required impact-catalog
registration; that gate remains nonpassing and does not replace the original
PR511 semantic or CI receipts.

## PR522 operative documentation integration

PR522 merged as `4a798d86b78f14599f7acfbdb809cbd08e74ef1e` over
`3a8b90c190c5756e1cce551c0a92503b5bbc5d3a`. The fetched tree
`4b356262cc2c7226818edc96976c7c13fab75e0a` equals independently reviewed
head `8639657fb6a55e66f1a5de6f21d500464aee8b77`; the source branch is
retained. The two documentation spans remove conflicting quota/work-limit
instructions and require coherent source-owner migration while preserving
source identity, type, effect, transfer, index and extent legality checks.

The independently reviewed documentation gate passed 240 documents, 19 phase
indexes and roadmap at the earlier composed `763521e2` head. Its unchanged
consumed inputs were verified at the final candidate; no fresh execution is
claimed. The earlier cancelled CI remains nonpassing history. Final-head CI
`40634871650` passed seven routine jobs, with Core and prerelease skipped.
The live membership/vector-index accounting consumers and original ACC-12
dependencies remain open; this documentation-only result does not establish
source, B51, Core, C12 or release acceptance.

## PR524 BKA header and first-debit integration

PR524 merged as `d63e6096d61d8662bcff0f20b33ad80c633fbfa8` over
`4a798d86b78f14599f7acfbdb809cbd08e74ef1e`. The merged tree
`6119e7a642ec1d157396e7c96e26f374bf9889ea` matches the independently reviewed
candidate `f8a77dd856e0c2160d08b2df53b6088c5f477966`. The source branch is
retained. C7, Effects and emitter removed BKA header allowance and first-debit
accounting while retaining source and semantic validation. Native authority
views are validated before the profile and target vectors are traversed.

The corrected normalized three-owner panel passed 18 tests and 692 assertions
with natural exit zero, actual 2 GiB, complete capture, identical clean
bookends and completed cleanup. It retained the final-field mutation warning
in stderr. Its receipt SHA-256 is
`eac8892a2dc42dc655abf5aacc472576d248c8d3120daab6b3271d7b4b6997cf`.
The earlier candidate panel failed with six failures and one error and
truncated stdout; it remains a separate nonpassing result. Final-head CI
`40637458409` passed seven routine jobs, with Core and prerelease skipped.
Independent review accepted the merge and exact tree.

Other work pools and BKA runtime policy remain open, as do PMK, ANE and the
separate DSE and historical mode boundaries. No authenticated-source, runtime,
Core, C12, release or whole ACC-04 acceptance follows from this finite result.
## Coherent hosted initializer validation

The census increment merged in PR444 at
`6b2e63b32f215b0c5f963fadfc018624837685b0`, with full tree equal to reviewed
`68609de1259c27a1f461abd8cd2d86ce1de0f949`; its branch was retained.
This next unit began from main
`f51a43ae267736a81587d81ffe52839c86c02b2d`, tree
`ddf3e0d60dc0fdb137cb04ca8984df2dd62efd20`.

All real initializer-helper callers were inspected: one production census call,
the function-arity inventory and 24 direct runtime test calls. The META-only
native header/identity primitives cannot be used by this hosted runtime. Their
profile guards were preserved. A separate, explicitly hosted/JVM native DATA
preflight was implemented and independently reviewed by its owning Native work.
It supplies representation validation of the same original input, not grammar,
typing, authentication, a transferable proof or public runtime authority.

### Old-source diagnostic evidence

The first diagnostic is **NONPASS**, natural exit 1 in 15704 ms: the foreign Map
fixture lacked lookup methods, causing a get/valAt failure before observations
were written. It is not evidence of a native guard rejection.

A separately corrected fixture passed its cheap 1-test/9-assertion controls.
The next actual diagnostic is also **NONPASS**, natural exit 1 in 16444 ms.
Its completed, durably recorded phases established that a 10-element Vector
initializer was accepted with work count 21; a 33000-element initializer was
refused at 65537, while the actual direct engine evaluated all 33000 elements
in order with empty output. A foreign Map was admitted and invoked lookup,
count and sequence once each. The diagnostic then failed an exception-shape
assertion: the controlled foreign-cycle fixture returned its raw probe instead
of a wrapper message. Neither failed run is promoted or summed with later work.

A minimal supplement reran only cycle attribution and previously unreached
domain questions. It passed naturally in 8073 ms, with exact clean old-source
bookends, 1200 complete stdout bytes, empty stderr and complete cleanup. The
foreign-cycle observation was the raw probe after 17 next calls; termination
belonged to the fixture, not the validator, and does not prove native-cycle
rejection. Native metadata and a visible SubVector with unused opaque backing
payloads were accepted and directly quoted with exact object/metadata identity,
no constructor occurrences and no opaque callbacks. Foreign metadata was also
admitted by the old implementation; its rejection by the new hosted contract is
an explicit native-domain strengthening, not preserved old admission. A native
depth-130 quote was refused by initializer validation despite direct quoting
completing with the same object. Independent actual-record review accepted these
engineering observations with their separate original provenance.

### New producer/consumer interface

The accepted Native dependency is PR446 commit
`0d3061965f34322bf4e9ec03084b8e4614133689`, tree
`b0fcc7c62949408d65a7480db88650c567bdb11a`, reconciled with main
`06a256fdb97f49b5fedea36a6803483302631bbc`. Its seven source/test/document
blobs were imported normally and remain unchanged by this consumer. The Native
owner retains those paths. API catalog/registration and integration remain a
dependency of the consumer's main acceptance.

Consumer `22898a2915bf1bb0e3a5ef880230f48769a92501`, tree
`941042daf7d80a26462b93dcc8f6e332160b39d5`, replaces the old two-argument
work helper with `runtime-validate-initializer [instruction]`. Its first operation
checks the original hosted DATA before any field access. It then validates the
complete initializer grammar and semantic constructor census. All callers and
the arity inventory migrate together; the old API has no fallback. Work/depth/
size allowances, reservation arithmetic, attempts and provider-cost fields are
removed. Semantic constructor counts and duplicate-initializer reasons remain.

Root Set members retain exact literal schemas and a complete homogeneous
String/Keyword preflight before duplicate equality. Map literal keys remain
unique Keywords. Quote descendants remain DATA, including opcode-shaped maps;
native metadata/views retain identity, and quoted containers are not counted
as constructors. Foreign carriers, native cycles, cyclic metadata and invalid
views reject through the hosted representation boundary before field access.
Whole-plan foreign ingress is outside this interface and remains open.

The actual bounded consumer batch passed 18 tests/1019 assertions, zero failures
or errors, natural exit 0 in 48204 ms on exact clean bookends. It selected all
five census/initializer tests, seven affected runtime caller kernels and six
language tests. Coverage includes complete 33000-element closed execution and
late invalid data, depth-130 quoted execution, preserved metadata/view identity,
foreign/native-cycle controls with zero callbacks, semantic constructor reports,
all migrated calls and explicit downstream policy distinctions. The 1025-member
Set initializer validates but the separate collection type limit still refuses
it; 80000-unit members validate but actual Set construction still hits its own
work limit. Those policies are unresolved accounting, not removed safety guards.

The configured 2 GiB child, 120-second watchdog, existing three resource guards
and capture bounds were unchanged. Stdout was complete at 2440 bytes; stderr
was complete at 379 bytes and contained the JVM warning from controlled native
cycle fixtures. No VM flags were changed. Cleanup completed. Independent static
source and actual retained driver/counts/stdout/stderr/receipt review accepted
this finite scope; the reviewer did not rerun semantic tests.

Separate cheap checks on that same candidate passed 11 tests/23 assertions and
the actual repository LOC gate: 2031 source files, no exceptions or violations,
maximum 250 physical lines. The normal-broker child used 256 MiB/60 seconds,
exited naturally in 1519 ms, with 489 complete stdout bytes, empty stderr, clean
bookends and complete cleanup. This is separate evidence, not a summed count.

| Mechanism | Disposition in this unit |
| --- | --- |
| Initializer caller budget, cumulative work, depth/size admission, reservation/attempt/provider fields | Removed coherently from helper and callers |
| Complete member/entry traversal, homogeneous domain, duplicate checks, closed IR schemas | Retained semantic validation |
| Constructor occurrence counts, definition/source/type/order checks | Retained semantic evidence |
| Native class/cycle/metadata/view boundary | Explicit hosted preflight; original META guards unchanged |
| Set type width and collection construction/equality/conversion work policies | Open, explicitly exercised as remaining refusals |
| Whole-plan native ingress, authenticated/public runtime, C12 and release | Unrun/open; no acceptance credit |

| Evidence | SHA-256 |
| --- | --- |
| First failed diagnostic receipt | `b3de98218660fb0215cb78630f40652535ea572f8ff2ed131078303225d44129` |
| Second failed diagnostic receipt with completed phases | `af9c64b94f552eb1f80b3b260e8c2f70a570130039fe747883fabced71c2054e` |
| Old-domain supplement receipt | `46f0bc31b06da9efa2d70b41f013c960be76e4cc179a20aefa6a76d9787c4125` |
| Accepted initializer consumer receipt | `f37e8fe62804813bab26cad5ce58760753e2b211ccb0fedbef24d9053589f103` |
| Cheap checks receipt | `6137aaabb5afb5dfcdb07ac9d6ba31c388bed0a5c9beeba1a4b248944e044384` |
| Runtime source | `fb1bcb794a2e379bf06e4f4a8438ee7921178501c80f0139723c33085ce55e7a` |
| Census/initializer tests | `959fcb92e2449a1f18e9358e474798f6659d963a7bf1aa57d6dad8b0e3b71e75` |
| Runtime caller tests | `42457123388ccba3e724e613315f33220912eeb150a0e56dfe6870646137d584` |

## Runtime Set literal construction successor

This successor preserves the separate initializer candidate PR450. Its frozen
baseline is `97a525851f528544d390ee9daebbbdbdc2c9bdc0`, tree
`c0ef20fa13115e68d2a2dfdedfac5b39a7e43ac2`, a normal composition of current
main `3bf69cbcd16be840fac8e687adfcf8e52b65696e`, the unchanged Native API
dependency PR446, and the initializer consumer. Neither dependency is granted
integration acceptance by this local successor.

### Actual old-family baseline

An independently reviewed read-only diagnostic exited naturally with code 0 in
8935 ms on exact clean bookends. All four small literal domains, the empty
family, direct Character-empty construction and equivalent Unicode scalar
duplicates exercised actual constructor and closed-IR dispatch paths. Small
positive outputs matched the entire expected Set; duplicate construction made
zero insertions. At 1030 members, every direct constructor returned the exact
width refusal while actual dispatch returned the instruction-shape rejection.
80000-unit String/Keyword members hit the independent content-work refusal.
Native definition DATA with 1030 positional types passed representation checking
while the old root-type join still returned false.

A bounded foreign type-map fixture established a distinct ingress dependency:
the old type join accepted it while invoking three lookups, one count and three
membership callbacks. Checking the same definition as inert quoted DATA through
the existing native boundary rejected it with zero callbacks. That observation
establishes a representation boundary, not type authority. Preflighting only the
initializer instruction would not protect the separate definition/type operand.

The diagnostic requested 2 GiB and the existing 120-second watchdog; it did not
record the actual heap. Stdout was complete at 3260 bytes, stderr empty, cleanup
complete and the process dead. Independent retained-record review accepted this
baseline. Separate fixture controls passed 1 test/10 assertions without a compiler
or provider run. These are separate scopes and counts.

### Coherent runtime producer/consumer change

Candidate `8bda620fcd30866fc3618c78a71257dc24fb5ebf`, tree
`e929c2c389aa6d6e66bba48a185d5e9f825e8280`, changes exactly three runtime/test
paths. `runtime-construct-scalar-set [input]` and
`runtime-construct-character-set [input]` replace both old budget APIs. Their
first operation validates the original native DATA, followed by complete member
domain validation before equality or insertion. All width/content allowances,
pair reservations, budget arguments, work-model/provider-cost fields and caller
debits disappear. Input extents, actual comparison observations, insertion
counts and semantic duplicate reasons remain; none governs an allowance.

The root type join independently checks the original definition and instruction
before reading their fields. The Set instruction branch checks original IR
before reading its item vector. This explicitly rejects foreign/cyclic DATA and
metadata at these interfaces; it is not preserved arbitrary foreign admission.
Earlier generic whole-plan header reads remain outside this finite claim.

Every tracked constructor caller, runtime arity inventory and budget-only test
expectation migrates together. String hash collisions and distinct Unicode
spellings, Keyword namespace/name collisions, Integer equality without formatting,
Character duplicate-codepoint semantics, the existing C2-owned atomic Character
IR premise, quote DATA, invalid side-effecting operands and source/type/constructor
checks survive. The existing Character IR convention is not expanded into a
source-decoding claim. Root Set initializer support remains String/Keyword only.

Independent exact-source review accepted the finite candidate and its test plan.
The actual batch passed 22 tests/1234 assertions with zero failures or errors:
seven census/initializer tests, nine runtime caller kernels and six language
tests. It exited naturally with code 0 in 113865 ms, observed heap 2147483648,
with unchanged 120-second watchdog and resource guards. Complete stdout was
2964 bytes; complete stderr was 379 bytes, only the existing controlled native
cycle fixture warning. No VM flags changed. Bookends were exact and clean;
cleanup completed with no live child. Retained-result independent review is
recorded separately from execution; the reviewer did not rerun semantic tests.

Coverage includes full 1030-member results in all four literal domains, direct
constructor and actual dispatch, complete String/Keyword module execution with
shared initialized object identity, long-content execution, late bad type/source
controls, foreign/metadata/cycle/view/type-operand rejection with zero callbacks,
and no early comparison or insertion after a late opaque member. Broader Set
conversion, membership, insertion and equality policies remain open. ACC-08's
typed leaf/shared lowering allowance remains open; no widened typed-source
positive or public/authenticated runtime acceptance is claimed.

Separate cheap checks on the same candidate passed 11 tests/23 assertions and
the 2031-file repository LOC gate with maximum 250 physical lines. Native PR446's
seven implementation/test/document blobs remain unchanged. Registration,
current-main integration, whole-plan ingress, C12, release and whole accounting
completion remain pending or unrun. No original receipt is relabelled as a new
execution and no counts are summed across scopes.

| Evidence | SHA-256 |
| --- | --- |
| Old Set-family baseline receipt | `c99df07282e1c1867da013c8c3701c86cd29379de98fc8576a484d64b40b97e6` |
| Runtime Set candidate receipt | `2b2511df880adf45050461c5827c832ed0b5ca10013f46772d0cd8e6e852319b` |
| Separate cheap checks receipt | `1cadda87688f2a873bec3140eee4564df56747177f6a05609db46249d4ce735c` |
| Runtime source | `44bae0bd65ca12ca9a9046876e83d286432443ac042141c6d1b00ebb20a0f5d5` |
| Census/initializer tests | `6bd206f8ced0e43a5a4a2c3d487db146fb19599caa327803879149113d75e97f` |
| Runtime caller tests | `e54af3b0eb5a1036b27dbb117b6baa0ea142c21f9b6bf9055651cc6b040b255d` |

## Runtime vector/List Set conversion successor

This is another finite unit of original ACC-09, based on the unchanged literal
successor `93efcde35fce442d9e3ee289331ad421ee8dbe45`, tree
`52825a064bc93f61c54e94249174997afef00e2d`. The original initializer and literal
branches, Native API and pending registration request are not modified.

### Baseline and actual scope

The old runtime diagnostic completed naturally with exit 0 in 8080 ms, observed
heap 2147483648, 1662 complete stdout bytes, empty stderr and exact clean bookends.
Actual direct helpers and builtin dispatch deduplicated small String/Keyword
vectors and Keyword Lists. At 1030 elements, all three routes hit exact width
refusals. Long String/Keyword vector content hit work refusals. The existing
native List carrier passed representation validation while Cons remained outside
the List conversion domain. These observations are a baseline, not removal
acceptance or typed-source admission.

The replacement interfaces validate the original native operand before count,
lookup, first/rest capture, equality or hashing. List capture completes its
homogeneous Keyword validation before constructing a Set; vector conversion
likewise completes String/Keyword validation first. The existing hosted native
boundary detects foreign/cyclic metadata, malformed views and cyclic native
List tails. This is an explicit native-domain requirement, not a claim that old
quotas established acyclicity or that arbitrary foreign admission is preserved.
Native metadata/visible views and the original input retain identity; unused
backing DATA stays inert. Host heap/watchdog/broker/capture limits are unchanged.

Conversion still deduplicates, unlike literal construction. Native String and
Keyword members have the same content/namespace equality used by the former
guarded comparisons. Hash collisions, distinct Unicode spellings, duplicate
values, same-printed Keywords with different namespace/name identity, late
invalid/opaque members, operand evaluation once, throw/catch forwarding, recur
and arity rejection, and function-produced inputs are covered. Provider comparison
counts are no longer fabricated as logical reservations; actual input/capture
extents and unique insertions remain. No Integer/Character conversion or generic
sequence family is added. C7/Effects/emitter budgets remain a separate open unit.

### Failed first batch and exact correction

First candidate `e278ce927e43f1661f3a285dc79784305d7a5945`, tree
`8048cecb5086633cf073719a411cade12c6a60d5`, remains NONPASS: 14 tests,
637 passing assertions and five failures, natural exit 1 in 30338 ms. Every
failure was the same stale runtime-function census cardinality: the migrated
inventory contained 56 functions while the test still expected 57. This count
is a semantic bijection check, not a budget. Native controls and the two unchanged
lowering selectors passed in this original batch; they retain this provenance.
Complete stdout was 3063 bytes, stderr 375 bytes containing only the deliberate
native List-cycle fixture warning. Bookends and cleanup were exact and complete.

Test-only correction `5bcce79ee37194ef14d725c2397ba1626ce355a7`, tree
`c8a23c8f051ce45ab430f557152eb4f610e505bf`, changes only that expected
cardinality to 56. Production/runtime/bootstrap/contract inputs and the native
test body are identical to the first candidate. The five affected runtime caller
kernels plus six language tests passed fresh: 11 tests/451 assertions, zero
failures/errors, natural exit 0 in 25827 ms, observed heap 2147483648, 1615 complete
stdout bytes, empty stderr, exact clean bookends and complete cleanup. This is
not a fresh passing 14-test batch, and counts are not summed with retained scopes.

Separate cheap checks on the first candidate passed 11 tests/23 assertions and
the actual 2031-file repository LOC gate, natural exit 0 in 1392 ms, 489 complete
stdout bytes and empty stderr. The later change affects one test constant only.
Independent review accepted the exact five-path candidate, retained records and
outgoing public payload at `d422f0db341218c02ef78e4032c5222938809f14`, tree
`442ee15d87ee6b93ae81e4e6c8fb1c34bdf2ae9e`. The reviewer verified the source/
receipt hashes, test-only correction and unchanged-input reuse boundaries and
performed no semantic execution. Dependency and current-main integration remain
pending. No authenticated/public runtime, C12, release or
whole accounting acceptance is claimed.

| Evidence | SHA-256 |
| --- | --- |
| Old conversion baseline receipt | `eca22531662daf48c0f072f6427a1dbdd05f25f0f8f4602869a6b20267af963d` |
| First candidate NONPASS receipt | `74608d20b88f04854f5b79f59ec2d2f5766856c1664bc28dc432817974f674dd` |
| Corrected targeted receipt | `c3e21a816e0921b525631cf774819b9f3467d091f3d727828ec1fd4fc2ed178a` |
| Separate cheap checks receipt | `c4621387b119c6cc84b1a072bbfeeea73c6fd6d814bf4da95a45df2ebf3a0c15` |
| Runtime source | `a66d7f94822b6a43e9f46c3951ce3b999e7fefd31d2a76fb5e78ae151bd48fdb` |
| Census/native tests | `e66c216a020776ba7f0540f990ade08994f774b7d34cfb6e13304a27d009afe0` |
| Runtime caller tests | `7f4de1a0b7fdf1af142e2a110789b9908de86a9c9ac84458b5fbbb3a323d0440` |
## Runtime family current-main composition - 2026-10-28

The [finite composition record](runtime-family-composition.edn) binds main
`6276c94b` and the original 446 -> 450 -> 454 -> 462 source identities. This
isolated draft preserves both historical evidence streams and current task IDs.
It does not incorporate any dependency into main or resolve Native446's unknown
registration operation. The separate PR513 review remains unknown and untouched.

Compiler-free reading found all 56 runtime function forms equal to the preserved
PR462 source. Four Native component blobs remain exact; the Native test differs
only by removal of an unused `testing` refer found by CI, and load order
preserves current main plus the original Native entry. All six retired runtime
API names are absent from executable bootstrap source/tests. This establishes
source/interface conservation, not a complete dynamic consumed closure or fresh
semantic acceptance. Historical receipt hashes match; their original counts,
including the first conversion NONPASS and corrected subset, remain separate.

Independent static assessment found a stale initializer-limit comment and two
conversion coverage gaps. The comment is corrected; tests now include a guarded
custom top-level Seqable in zero-callback rejection cases and assert List capture
extents. The new controls are parsed but unrun. Form/interface reading and the
language boundary pass (6 tests / 11 assertions); no compiler/runtime job or
shared broker operation was launched. The local audit's first attempt failed on
an empty merge-commit file inventory; correcting that audit input produced the
passing static result without evaluating Gravity or loading Bootstrap.

Completion still requires verified dependency incorporation in order, an exact
current composition/consumed-input assessment and permitted affected runtime
validation, independent acceptance, routine CI and fresh integration identities.
The known historical results do not close ACC09, public runtime or release.

At `9e40bc14`, CI run `40800178456` found one new unused-`testing` refer warning
in the preserved Native test namespace; 3688 unchanged baseline warnings remain
separate. The correction removes only that unused refer. The portable leaf job
was cancelled during setup before selectors; it is not runtime-test evidence.
Neither result changes Native446's unknown registration or permits a runtime
launch. The original failed check is retained with its original candidate.
Changed-file lint passes with zero errors/warnings after the refer correction;
no new test or rerun was launched for this CI diagnosis. The earlier boundary
result retains its pre-correction source attribution.
