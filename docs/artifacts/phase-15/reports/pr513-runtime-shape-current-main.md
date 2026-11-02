# PR513 runtime-shape current-main selective port

## Purpose and scope

This report records the bounded current-main composition of the useful PR513
runtime-shape evidence. It is a source-port and development-test record only.
It does not accept the PR as a whole, renew source authentication, or claim
native, seedless, self-hosting, release, or full-language support.

The reviewed PR is burn2delete/uniform#513, "Remove size admission from runtime
shape validators". The exact remote head was queried before composition:
`codex/runtime-shape-admission` at `3867de472efb7433b62c6f90e94e755996d64ec1`.

## Immutable source identities

| role | commit | tree |
| --- | --- | --- |
| fetched current main | `2d934162b30c62ef85e0182fc2933522104b31de` | `d3cbf90a532975d2392d414ebf72fd8e32a24929` |
| exact PR513 source head | `3867de472efb7433b62c6f90e94e755996d64ec1` | `2eca42de700dd4fb4ffe3d806c369be6a45aced5` |
| ancestry-preserving ours merge | `c7aa78dbe681ae4ee871c8f9741d07c0c271b5e2` | `d3cbf90a532975d2392d414ebf72fd8e32a24929` |
| scoped source port | `000a7d9e230b3f93b993fc2d458237e4d1853218` | `54f4fae13d0a4d151d203bdb8968bdc2375eff72` |

The ancestry-preserving merge has parents `2d934162b30c62ef85e0182fc2933522104b31de`
and `3867de472efb7433b62c6f90e94e755996d64ec1`; its tree remains exactly the
fetched main tree. The port commit changes only the seven paths listed below.
No GitHub mutation, push, merge, or PR disposition was performed.

## Complete unique-value accounting

The authored PR diff from its merge-base (`cbf5c210544a0033e9c755d7ef8358f01f1ad57c`) is
11 files, 340 insertions and 25 deletions. Each path is accounted for here:

| PR513 path | disposition in current-main composition |
| --- | --- |
| `bootstrap/clojure/test/gravity/self_hosting/runtime_shape_test.clj` | Ported as a four-test SH-19 fixture. The 256 unrelated loop-target case now accepts; the same-target containment case still rejects. The fixture remains an unsupported direct execution surface. |
| `bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj` | Ported the 257-character recur-target positive and wrong-target diagnostic negative, plus 65537-handler positive and malformed-final-handler negatives. Candidate-only removal of obsolete negatives was already present on current main. |
| `bootstrap/clojure/test/gravity/self_hosting/sh01_impact_test_planner.clj` | Ported only the runtime-shape namespace into the existing SH-19 owner-wrapper unsupported set. The existing private-collection addition was already on current main. |
| `bootstrap/clojure/test/gravity/self_hosting/sh01_impact_test_planner_test.clj` | Ported only the runtime-shape path/namespace into the existing catalog guard loop. The existing private-collection test was already on current main. |
| `bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner_test.clj` | Ported only the runtime-shape symbol/string entries into both existing zero-launch guard loops. The existing private-collection test was already on current main. |
| `contracts/stage0-incremental-test-dependencies.edn` | Added the fresh deterministic `runtime-shape` standalone entry; preserved all existing entries. |
| `docs/self-hosting-slice-ownership.edn` | Added the SH-19 `runtime_shape_test.clj` owner record; preserved all existing ownership records. |
| `bootstrap/gravity/src/gravity/runtime.gravity` | Not ported. The four quota removals are already in current main; the stale PR source still contains the removed 256-target cap and would regress current semantics. |
| `specs/sh-family-crosswalk/crosswalk.edn` | Not ported. The changes are stale metadata/source hashes and historical review records, not runtime semantics. |
| `specs/sh-family-crosswalk/source-version-bridge.edn` | Not ported. Its old candidate/tree pins are lineage history, explicitly not implementation acceptance. |
| `tools/sh_crosswalk.clj` | Not ported. It is broad fingerprint/source-pin regeneration with unrelated history, not a necessary runtime-shape source change. |

The scoped port is therefore 114 insertions and 4 deletions in seven paths,
with no runtime implementation source copied from the stale candidate.

## Semantic controls

The port preserves concrete validation rather than work admission:

- The executed typed-catch kernel accepts 65,537 valid handlers and rejects
  malformed final handler type, name, body, and extra-field cases.
- The executed recur kernel accepts a 257-character target when correctly
  used and rejects the wrong target with `P15S23X002`.
- The unexecuted standalone fixture encodes the 65K parameter, instruction,
  binding, and recur-shape cases, 256 unrelated loop targets, same-target
  containment refusal, and their malformed-member controls. These encoded
  assertions are not execution or acceptance evidence.

The standalone `runtime_shape_test.clj` fixture was not executed. It calls the
stage2 runtime directly without the committed owner wrapper and remains
classified as `SH01-IMPACT-EXTERNAL-WRAPPER` / `:unsupported`, requiring
`:common-host-broker`, `:host-exclusive`, and `:canonical-heavy-lock`.

## Validation receipt

All commands below used fresh JVMs and the exact selectors shown.

| check | result |
| --- | --- |
| `runtime-module-read-owner-wrapper-is-required-before-scheduling` | 1 test, 39 assertions, zero failures/errors |
| `direct-process-entrypoints-require-the-runtime-owner-wrapper` | 1 test, 109 assertions, zero failures/errors |
| `current-gravity-function-recur-runtime-development-kernel` | 1 test, 66 assertions, zero failures/errors |
| `current-gravity-typed-catch-runtime-development-kernel` | 1 test, 88 assertions, zero failures/errors |
| language-boundary gate | 6 tests, 11 assertions, zero failures/errors |
| gravity document validator | 240 docs, 19 phase indexes, ASCII, no placeholders |
| full-language roadmap validator | structural claim validation passed |
| workstream governance validator | 185 workstreams validated |
| staged diff check | passed |

The later current-main composition at
`f088d26551a6a13e0ce8306a9235e4a7eec4b535`, tree
`7ebbad39b1e59c7cf1df63a3e3df66ea84fd79bb`, incorporates base
`7e297a3df1d3163f27077651a96f1d847cf1c95b`, tree
`b3005b0b58239ea6ee4ed0a88b7853cb5a3b2088`. The same four permitted
selectors passed together on that immutable composition: 4 tests, 302
assertions, zero failures/errors. Boundary, docs, roadmap, governance and
whitespace checks also passed. The standalone fixture remains unexecuted.

The focused catalog test did not reach its assertions because current main
already fails `SH01-STANDALONE-TEST-CONTRACT` with 23 unrelated
ownership-only paths. The identical one-test/one-error result was reproduced
on an immutable archive of unmodified `origin/main`; the listed drift is not
caused by the runtime-shape entry and is not waived here. No direct
runtime-shape execution, generic launcher bypass, native-C operation, or
restricted operation was attempted.

## Nonclaims and remaining gates

This record does not claim full PR513 acceptance, production-validator source
change, native or Wasm evidence, source authentication, authoritative
self-hosting, release readiness, or completion of the broader SH-19 slice.
Historical PR metadata, failed gates, and source-version records remain
historical and were not rewritten.

Independent review of the exact `000a7d9e230b3f93b993fc2d458237e4d1853218`
tree, coordinator integration preflight against its exact base, and any
applicable current-head CI remain required. PR513 must remain open until this
valuable delta is actually incorporated into accepted main; this branch has
not performed that integration.
