# PR512 current C3 source port

## Purpose and immutable scope

This port removes the C3 graph and container accounting ceilings while
preserving shape, arity, reference order, uniqueness, Unicode, metadata,
provenance, source identity, native-class, callback, cycle, and tamper checks.
At the historical composed-source tuple recorded below, the two Gravity source
files were byte-identical to original PR512. That historical statement is not
the identity of the current PR512 head.

| Item | Commit | Tree |
| --- | --- | --- |
| Original PR512 | `5b5f0a05efb1453b479d6ab967e42fd30b0663a6` | `0c9b0fa57d98d0d7f9554e5a6cfd06bb91ee1f82` |
| Original patch parent | `d20c9adb648bf26e3d648e296683fe0203271486` | `ff33b028389fa8f7ab9c066dff73c04dfaff4a3c` |
| Explicit current-owner handoff | `45bcf5d3e24e5e6e7cb2254e2be8504883ef7891` | `3013d6cd6e7964c37b718c0ef138ea5e483b9ee9` |
| Gravity source port | `ddd19ef21a7e63d0ddb06206c84aa021149901dd` | `45347de18794dd6da3b961e53f1436f7bcc8848e` |
| Core/C11 affected-consumer repair | `9a04a7afb3fee39a716e3dc5ea375b6a00a4a556` | `34802c4eff7c01dd09fc3ea3f6f81f09011331e1` |
| Actual current main | `43dbdd419c643df42454db42c2cf1b75dea0e292` | `d48baf6d2dfc99ae94f5438a7e6f729d8b8db7b8` |
| Composed source candidate | `b37bcc6262852ef1ebf797fa6cd13136dcfd9c06` | `9d481cddad407a5e3c0526eeb42c5ef67523767f` |

Original ancestry is preserved through the ancestry-only merge
`fe685be1ccadd23b5426969ee8fcdd6c594ca2bf`. The clean named checkout is
`/Users/matt/.codex/worktrees/review-pr-535-port/uniform`, branch
`codex/review-513-current`.

## Explicit development handoff

`gravity.syntax` accepts an explicit current-source selection. It validates
the owned path and strict source snapshot, builds current program identities,
and rechecks the source before and after execution. SH03/C3 and resolution
construction carry the selected binding and authenticity joins. The route is
marked `:development-unadmitted`. Existing calls without this selection retain
the frozen-source admission checks and their stale-source refusals.

An affected integration check with PR453 initially stopped with `C3-ID`:
expected Syntax source bytes `81241`, observed `78739`. The v2 Core route had
omitted its validated Syntax selection. The four-file repair carries the
owner-produced invocation through current Reader selection, fresh C6
construction, private Core ingress, and C2/C3 front-end products. Legacy
arities are preserved. The genuine Core-to-C11 handoff then passed.

The merge of actual main preserved all thirteen PR512 source/test blobs from
that repaired candidate, including the current handoff. It also retained
main's Core/C11 alias cleanups and CI changes. The source evidence is reused
with this explicit conservation; no repeated execution is implied.

## Source identities

The following table is the historical source-port tuple for the composed
candidate recorded above. Its byte counts and hashes are immutable historical
observations; they are not current-head identities.

| Source | Bytes | SHA-256 |
| --- | --- | --- |
| `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity` | 78739 | `327763641df1c00ceb3d2838e6419a7af77b8bae5c85f5ccdd02200583874184` |
| `bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity` | 4062 | `31241b6fafa5ff7ab7eefad272397b6141d9c1166cb12c3529bf6803147cb585` |

These are development source observations. No canonical admission hash was
renewed.

## Validation

- Syntax pipeline: 29 tests / 332 assertions passed.
- SH04 adapter: 22 tests / 1014 assertions passed.
- Latest Reader composition, fresh current C3: 1 test / 13 assertions passed.
- Latest Reader composition, stale pinned/default source refusal: 1 test /
  7 assertions passed.
- Genuine combined PR453 Core/C11 handoff after the consumer repair: 10 tests /
  52 assertions, zero failures and errors. The failed pre-repair attempt is
  retained in `/tmp/uniform-pr512-pr453-current-source-handoff.log`.
- Pinned clj-kondo 2025.10.23 against actual main43: zero inherited warnings
  and zero blocking diagnostics.
- Language boundary: 6 tests / 11 assertions passed.
- Docs: 240 documents / 19 phase indexes passed. Roadmap and governance
  validators passed. `git diff --check` passed.

## Post-composition current-source caller repair

The first hosted validation of the composed candidate was red and is retained
as a defect receipt, not treated as a stale check. Run `41498207973` reported
the following affected jobs:

- Resolution pipeline job `125458841658`: 42 tests / 2144 assertions, 2
  failures and 4 errors. The current-cohort fixture reached C3 with expected
  Syntax bytes `81241` and observed current bytes `78739` because its
  current Reader/Macro invocation omitted the validated Syntax selection.
- SH04 fresh-C2 job `125458841740`: 63 tests / 609 assertions, 2 failures and
  1 error. Its current construction and standalone frozen-entry fixtures had
  the same missing handoff; the standalone entry also exposed the correct
  earlier pinned C3 refusal for a current C2.
- Macro pipeline job `125458841884`: 27 tests / 148 assertions, 12 errors in
  paired expansion, source-mode, and current receipt fixtures for the same
  omitted Syntax selection.
- SH06 unmetered Resolution job `125458841639`: 9 tests / 58 assertions,
  1 error in the actual current-source artifact fixture.

The scoped repair keeps Syntax selection explicit and independent. Resolution,
SH06, SH08's C8 fixture helper, Macro paired fixtures, and SH04 fresh-C2
construction now carry the validated
`:gravity/sh04-development-syntax-selection-v1` binding for their intended
current-source routes. The Resolution Macro-isolation case selects Reader and
Syntax but omits Macro, so it still fails closed at C4 with
`:exact-pinned-macro-source`; a separate Reader-only case omits both selections
and fails closed at C3 with `:exact-pinned-syntax-source`. The SH04 standalone
two-argument public entry likewise records the early frozen C3 refusal when
given an explicitly current C2. No default/pinned call was converted to
current mode and no admission hash was refreshed.

On the repaired local candidate, the affected JVM receipts are:

- `gravity.resolution.pipeline-test`: 43 tests / 2203 assertions passed.
- `gravity.self-hosting.sh06-unmetered-resolution-test`: 9 tests / 78
  assertions passed.
- `gravity.self-hosting.sh04-fresh-c2-construction-test`: 5 tests / 30
  assertions passed.
- `gravity.macro.pipeline-test`: 22 tests / 307 assertions passed.

These are focused development checks, not hosted or canonical admission
authority. The original hosted red result, the exact current-source byte
comparison, and the pinned/default refusal cases remain part of the record.

## Remaining work and nonclaims

The historical source-port tuple above recorded that the Reader revision byte
ceiling `1048576` remained in that then-current source and deferred its removal
to the separate PR566 delta. That statement is historical. At the current
immutable PR512 head, C3 Syntax no longer enforces
`source-byte-count <= 1048576` in `c3-reader-source-revision?`; it retains the
nonnegative-integer and identity/schema checks. The existing PR566 evidence
remains separately scoped to its Reader revision-length controls and does not
prove large-source construction, whole-pipeline acceptance, canonical
admission, release, self-hosting, or seed-retirement. PR544's additional
selected-interface controls and PR580's selected-revision/process evidence
remain separately reviewable.

This receipt grants the finite C3 development evidence above. It grants no
canonical admission, C12, large-Wasm, native-C, migration, release,
self-hosting, or seed-retirement authority. Integration requires independent
acceptance of the exact candidate and an immediate clean integration preflight.

## Final composition on the latest main snapshot

After the source handoff repair, the candidate was normally composed with the
latest main tooling-only merge `69d21cb8aacd0be8e74cbf4c9ad8b12b84d244e8`
(tree `1911689d59beb0d9771d5efc5fe29856177b470e`, parent
`f2ec11752875fb1d15d7fd00fd343139cf0b96e5`). The resulting clean composition
is commit `693aa44ecd1f09dd7e0216ade69a66719ff84345`, tree
`219a4f829de8d0a11fd6b27d9a6c37f7f153e711`, with parents
`2b04ccda3f331d73f88b440b3fb939cdda8002c9` and
`69d21cb8aacd0be8e74cbf4c9ad8b12b84d244e8`. The four incoming tooling
proposal/checker/receipt paths contain no compiler source inputs and were
conserved as a normal merge; their own checks remain separately attributable.

The strict changed-file lint receipt for the PR512 source/test repair used the
pinned clj-kondo 2025.10.23 binary and exact base
`ad5387166ad7ed30a93799a66c6697e4b6ef64d8`: 73 inherited baseline warnings,
zero blocking warnings/errors. The later tooling-only merge did not alter any
of those source/test paths. Worktree identity after composition is clean.

## SH07 construction handoff for explicit current Syntax

The post-composition caller audit found one remaining owner-boundary omission.
SH09's current Reader/Macro fixture and the SH07 current Map-guard fixture now
carry the validated `:gravity/sh04-development-syntax-selection-v1` selection.
The SH07 construction guard correspondingly reads the selected current Syntax
bytes and binding when that selection is present, and keeps the historical
pinned Syntax read and binding when it is absent. The Macro mode remains a
separate authenticated assertion; the current Map-guard fixture explicitly
selects current Reader, Syntax, and Macro because its source route is current.
No default call was silently converted, and no admission pin or hash was
refreshed.

The first attempt with current Syntax and the old pinned Macro default failed
closed at C4 with `:exact-pinned-macro-source`; this was retained as the
incompatible mixed-source observation. After the owner fix and explicit full
current selection, the real Map-guard-to-Core branch replay passed 1 test / 131
assertions, including hostile branch replay rejection. The independent SH07
Macro-mode/source controls passed 2 tests / 9 assertions. The SH09 private
Map-key paired current-source test was started once, but reached a host JVM
resource boundary at about 10 minutes and 3.66 GB RSS before a terminal result;
that run is not claimed as a pass and was stopped without retry.

The source owner change is intentionally narrow: no native-C, C11, PMK
admission, or migration evidence is inferred from these JVM observations.

## Remaining current-caller audit and repair

The composed candidate was re-audited from the clean pre-repair identity
`d88c0e053eaa0c69c6831d665a7fff17df010a47` / tree
`bc206cee028d920f8832c0012d4e2f0e768f420a`, based on main
`69d21cb8aacd0be8e74cbf4c9ad8b12b84d244e8` / tree
`1911689d59beb0d9771d5efc5fe29856177b470e`. The hosted C3 failures were not
isolated to the three SH04 fixtures: every current-source C3/C5/Core caller
was checked once and classified by downstream intent.

The minimal owner repair now carries the validated
`:gravity/sh04-development-syntax-selection-v1` selection through the
remaining current paths. It covers SH04 lossless, handoff, and provenance;
the SH07 current data, loop, quoted-reader, set-order, two-slot, local-callee,
and construction-verification fixtures; the SH08 primitive source-to-Core
fixture; the SH15 current C5 diagnostic producer; the SH07 proof-census
current request; the SH07 B2 required-primitive current composition; the SH03
small current source-to-Core composition; and the current parent-mutation
resolution control. Current C5/Core paths also select
`:macro-source-mode :development-current` when they consume Macro products.
The two direct SH07 request/test adapters strip only that Macro key before
calling the exact SH03 Reader-options callback; the full invocation continues
through SH06 and the Macro owner. No pinned/default call or public option set
was widened, and no admission hash was refreshed.

The explicit refusal classifications remain part of the audit. The resolution
Macro-isolation control selects Reader and Syntax but omits Macro and fails
closed at `C4-TRACE`/`:exact-pinned-macro-source`; the separate Reader-only
control omits both and fails closed at `C3-ID`/`:exact-pinned-syntax-source`.
C2-only tests, routing-only spies, historical/default C5 callers, and the
public `{:reader :development-current}` API contract remain unchanged; the
public route does not silently infer Syntax or Macro source. Its downstream
paired public fixtures therefore remain historical/refusal evidence rather
than being relabeled as current full-source acceptance.

Focused checks on the repaired worktree passed:

- SH04 lossless/handoff/provenance: 1 test / 37, 518, and 99 assertions.
- SH07 current data/loop/quote/set/multislot/local-callee: 1 test / 405,
  133, 50, 179, 89, and 107 assertions; construction-verification focused
  tamper/request controls: 1 test / 8 assertions each.
- SH08 primitive current source-to-Core: 1 test / 532 assertions; SH15 C5
  diagnostics: 1 test / 54 assertions; SH07 B2 current composition: 1 test /
  191 assertions; parent-mutation resolution: 1 test / 20 assertions.
- The two explicit source-choice refusals passed 2 tests / 4 assertions, and
  public ingress option/default pinned controls passed 2 tests / 11 assertions.

The SH03 adapter namespace is now executable after the retired carrier
node/depth/width and source-identity quota tests were removed. The
two-argument trusted-carrier call remains the native-class, cycle and metadata
validation path; no old limit constants were restored. Unicode, native-shape,
digest, provenance and tamper controls remain. No native-C, PMK,
resource-broker workaround, or restricted route was retried. The repaired
candidate remains development evidence only and needs independent acceptance
on its exact final tuple.

## Latest-main composition and SH03 stale-quota cleanup

The clean PR512 repair commit `a6e1880f83253e9b3f5634cb5b612b1c0059e6c4`
was normally merged with main commit
`d97539762108d7e44024164c61bb120044ef684b` (tree
`093a9c284a1e2fc4dd360fa677e43f7a0512dfba`), which includes the current MIR
source-owner extraction and emitter-cycle repair. The merge had no conflicts;
its composition commit was `f6956ae937baf4280002bfc0b8f1fe8b944360a5`,
tree `ec2b021d70167a8da782d93f572ddad6383f4ae8`, with parents
`a6e1880f83253e9b3f5634cb5b612b1c0059e6c4` and
`d97539762108d7e44024164c61bb120044ef684b`. The MIR/emitter paths do not
overlap the repaired Reader/Syntax/Core caller paths; the earlier genuine
current Map-to-Core receipt (1 test / 131 assertions) is reused as prior
evidence, not relabeled as a fresh d975 execution.

The remaining SH03 test file still referenced constants removed by the reader
quota-removal source change. The repair removes the budget-only carrier-depth
and source-identity-size tests, removes the observed-node allowance assertion,
and calls trusted-carrier validation through its current two-argument contract.
It also updates two stale diagnostic expectations from
`:exact-bounded-sh03-reader-instruction` to `:exact-sh03-reader-instruction`
and from the historical plan-identity diagnostic to the current exact-plan
envelope diagnostic. No runtime quota, bound, admission hash or pinned/default
selection changed; the finite depth/stack-safety, Unicode, type/shape, native
carrier, provenance and tamper tests remain.

Evidence for this cleanup is bounded and attributable. The first post-cleanup
namespace run (`clojure -M:test --namespace
gravity.self-hosting.sh03-reader-adapter-test --fail-fast`) loaded and ran 32
tests / 1079 assertions with no errors and one stale expectation failure. After
the two expectation updates, the exact owner test
`sh03-current-reader-plan-checks-keep-historical-priority` passed via
`clojure -M:dev-test --namespace gravity.self-hosting.sh03-reader-adapter-test
--exact gravity.self-hosting.sh03-reader-adapter-test/sh03-current-reader-plan-checks-keep-historical-priority
--fail-fast`: 1 test / 27 assertions, 0 failures, 0 errors. The complete
namespace was not rerun after expectation-only edits; the preceding run is
the evidence that all remaining forms load and execute. No hosted failure
receipt, native lane, PMK lane or migration claim is inferred from this
focused repair.

## Final composition with the authenticated owner-prefix witness

The clean SH03 cleanup commit was normally composed with verified main commit
`2d0844f8f7f21321d920b9245341a2b98adc642e` (tree
`c5f5941ca3db4ce38695f870276e1b345a323339`, parent
`d97539762108d7e44024164c61bb120044ef684b`). The normal merge had no
conflicts. Before the final owner-witness repair, the merge identity was
`e99f27c094065329181e647577cfbe916011cfc5`, tree
`13153d4714300d3aadb2834dd1ea08b2a38e6986`, with parents
`97929ea6c53484a66a20bae8f9b9e43c005991e6` and
`2d0844f8f7f21321d920b9245341a2b98adc642e`.

The new authenticated owner-prefix witness now carries the explicit
`:gravity/sh04-development-syntax-selection-v1` selection and
`:macro-source-mode :development-current` in its full Reader-to-Core
invocation. SH03's exact Reader invocation contract intentionally receives
only the Reader plus Syntax selection; SH05 receives the same authenticated
Reader/Syntax map with the Macro source mode as its explicit source-mode
argument; SH06 and SH07 retain the full invocation for their downstream
binding and verification. This is the smallest stage-local adaptation and
does not widen the default or pinned route. A first run failed closed at C2
because the full Macro selector was incorrectly forwarded into SH03; that
failure led directly to the stage-local fix and was not relabeled as a pass.
The corrected real owner-prefix replay passed 1 test / 35 assertions with
zero failures and errors. The ten other owner selectors were not run.

The requested fresh semantic witness then passed on this composed source:
`gravity.self-hosting.sh07-current-map-guard-test/current-map-guard-source-to-core-and-branch-substitution-replay`, 1 test / 131 assertions,
zero failures and errors, including hostile branch replay rejection.
Focused negative controls also passed: the two Reader-only source-choice
refusals in `gravity.resolution.pipeline-test` passed 2 tests / 4 assertions
(C4 `:exact-pinned-macro-source` and C3
`:exact-pinned-syntax-source`), and the Syntax pinned/default plus
selection-mode/source-tamper controls in `gravity.syntax.pipeline-test`
passed 2 tests / 12 assertions. Pinned clj-kondo 2025.10.23 reported zero
findings on the changed authenticated owner-prefix test. The full hosted
lanes, native-C, PMK, broker and migration claims remain outside this
receipt; no restricted route was retried and no admission hash was refreshed.

## Final immutable PR512 source identity

The current immutable PR512 candidate is commit
`c46ddb408e9503e6960fd12ff6daedf403ee895c`, tree
`e71c1899a3f3c5985f1084895b9204764c4bca07`. At this exact tuple, the current
Gravity source identities are:

| Source | Bytes | SHA-256 |
| --- | --- | --- |
| `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity` | 78643 | `a9252e729c2bf830c157708352b9f760e6cb959bf621eb4d5893fdc968074ffa` |
| `bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity` | 4062 | `31241b6fafa5ff7ab7eefad272397b6141d9c1166cb12c3529bf6803147cb585` |

The current Syntax source removes the `source-byte-count <= 1048576` admission
check from `c3-reader-source-revision?` while retaining its nonnegative integer,
exact-key, schema, artifact, function, digest, and binding/revision identity
checks. The historical table and ceiling statement above remain preserved as
evidence for their original tuple. The existing PR566 report at
`docs/artifacts/phase-06/reports/syntax-reader-revision-length-current-port.md`
is evidence only for its separately owned Reader revision-length controls and
their focused negative cases; it is not a claim of whole-pipeline,
canonical-admission, release, self-hosting, or seed-retirement authority.
