# PR721 current-main port receipt

This receipt accounts for every one of PR721's 17 authored paths. The original
candidate remains historical source-review input. The current implementation
preserves the useful Core/session, scalar-Set, source-data and CI-context
semantics against current owners; it does not replay the old parent tree or
claim whole-project acceptance.

## Immutable identities and dependency

| Item | Commit | Tree |
| --- | --- | --- |
| Original PR721 base | `43c69654b3f2daefa05e3902cbe78b469af47d96` | `088aa0c632516704aa552a9fa2a046828661fabc` |
| Original PR721 head | `8cf0651e5d1adaafe4b32325beaf4c47f1d3e894` | `f2b4e596e0ef5f354e2921689d15026710650557` |
| Historical prior current-main base | `d351310d5821a983fac2ad0fee4c0be96711b4b4` | `1d7c95cf054bbc3e1719bc39cb35ac7bbaf13049` |
| Normal PR697 dependency source | `8cad807dbd99317baf0a9da622bb9d9ef99650a1` | `b972c6620b59f753a8c0dca85464e0cfced2a826` |
| PR697 corrected receipt | `c5ca71cd5c0428af72b423d17ba7d62a613b5d7b` | `c8f917f3701f58d2d168f120dd0550a7e3fb46cf` |
| PR697 normal composition merge | `6ae40925dec3ae486dc63c4963a1bfa60ce633ec` | `641beb2f3861c1ab08d56626a9d72a8d1e938603` |
| PR721 source/test checkpoint | `19b7bbe8e6b50d5b74aa3ab3611d81f5ac37c032` | `1510969cd79404f34d6ab1ad8856b1dd96aa4e9f` |
| Final preparation base (main 885) | `b0d949cad61c65b19a9c441669db637a90deccb1` | `7b614c485e7e012c4e7023e83e70cec852ededcd` |
| Final source checkpoint before receipt metadata | `f891c8c1a5962ef45efd358a0c8781756dabf14f` | `572099ea99212c64d47029dec0214ffc459c6d4e` |

### Historical current-main composition frame (19b/ac07)

The candidate was normally composed on the actual PR697 main at
`78db2bc984bd66de920a4abaef02f759f1108605` (tree
`9e435a4f3bbfe02a69ddc3dd71119bc381d06041`). The resulting merge is
`ac07ee623d293a60a6c6c9ad690b9ee587f621e4` (tree
`b046603a9996b377960c0a878dccadc42073dc7d`), with parents
`8b5020748fcf14fb67bcf17f8d0958b73020a063` and the current-main base.
All 17 original paths are represented in this composition; their exact
current Git blob IDs and SHA-256 values are recorded under
`:current-main-composition` in the EDN receipt. This frame is a source
composition checkpoint, not independent acceptance or publication to main.
The historical `19b7bbe8...` checkpoint and its 17-path frame remain
unchanged; the final receipt metadata commit is intentionally not self-hashed.

The PR697 merge is an explicit current-owner dependency, not PR721 roadmap
credit. The historical source checkpoint was clean before its receipt metadata
commit; the historical current-main composition frame records the later clean
source merge. The `:current-main-composition` map is retained as the 19b/ac07
historical frame and is not the final 885 source identity.

### Final current code checkpoint (main 885)

The final preparation base is main 885 at
`b0d949cad61c65b19a9c441669db637a90deccb1` (tree
`7b614c485e7e012c4e7023e83e70cec852ededcd`). A normal merge produced source
checkpoint `f891c8c1a5962ef45efd358a0c8781756dabf14f` (tree
`572099ea99212c64d47029dec0214ffc459c6d4e`), with PR721 composition
`cfc7f73bd0e2b2d38926d3e959d278241bbf28ed` and main 885 as ordered parents.
The worktree was clean before this receipt metadata update.

The new `:current-code-checkpoint` EDN frame records all 17 original useful
source, test, documentation and tooling inputs at their exact current blob IDs,
byte counts and SHA-256 values. The final EDN and Markdown receipt paths are
explicitly excluded from that input set so the receipt does not self-hash. The
merge preserves main 885's relocation selectors and bootstrap-boundary
selection controls while retaining PR721's lint-context loading and namespace
run.

Fresh bounded checks at this checkpoint passed: the pinned CI harness ran 45
tests / 480 assertions, the facade suite ran 35 tests / 559 assertions, and
pinned clj-kondo on `tools/ci` reported zero errors and warnings. Logs and
archived generated CI artifacts are recorded in the EDN frame. These results do
not upgrade hosted-native, C5-C8, migration, release or self-hosting claims.

The final receipt commit/tree must therefore be read externally and is not
self-hashed here.

## Complete original-path accounting

The table gives each original Git blob's SHA-256, the source-checkpoint current
SHA-256, and the disposition. Full Git blob identities for the later
current-main composition are machine-recorded in `pr721-current-port.edn`
beside this report.

| Original path | Original SHA-256 | Current SHA-256 | Disposition |
| --- | --- | --- | --- |
| `00897-sh07-core-construction-upstream-guard.clj` | `679deccbeff2911e00185d9fe9d36ccaed0c2488e3010b5299fcc852711fa52e` | `352feb1af8de7500594f16bb1cd2d709e261e20a183db1da16ae4dd51a8b425b` | Current raw-root/effective-binding port |
| `00898-sh07-core-from-authenticated-request.clj` | `62201e58302fb0e07f5a01dd6f1dfff740442d44b4e0322013abc32f9baa2cb6` | `b92924334557b1ee371fee4b771524e4c0a55c15f93d9a510c4d8a4c582e435f` | Current private binding-fact port |
| `00899-sh07-core-verification-session.clj` | `1d00cfb1132313e5ebb3506ad6476f6c19fd9112e1ee4e33b18800598494fed8` | `f85093054ec85fae612bbc558ec81bb2c19af1b272d077aefa8621aeb234f587` | Current Throwable/diagnostic port |
| `core_construction_binding_test.clj` | `2128c36ebdb4208510b1dd8f1814aa4ca1dbce364f6a7926c0deec320bcbdc2e` | `53b26dc9d6e625a674ce8f6bf2c8f13e9a3fe0b9ca0ae3678b40104c6e5c24a3` | Current binding/source controls |
| `core_session_invocation_test.clj` | `9d52ec6bb75de1e7725bcd7243f6345d6f27be568356dad0b4d624017f00cdc5` | `bac87ee1e16f2e18ca15cb2923f0ce696d12bfa85c35da095989cabd620e0b5a` | Current invocation/failure controls |
| `hosted_native_data_preflight_test.clj` | `b219a95189275c7fbd4437b05ce48195433b3945bd4c23ddc7869193b4c8e96f` | `b219a95189275c7fbd4437b05ce48195433b3945bd4c23ddc7869193b4c8e96f` | Byte-identical current equivalent |
| `module_census_test.clj` | `d28659a94990bff68d9faadd9542ec8df4d0994c6dfc6c8e8b624e6c97078959` | `e9fc4499e28d2652dfbba152127dae7459fde4eed6a4f41c6f0ddd5d272a6f62` | Foreign Seqable/no-partial-result controls |
| `sh01_stage2_runtime_iteration_test.clj` | `32144ed245cc7fb708d17f61230ece6fa2601197ce3e7368a66e13ac2a3db513` | `2f536c69c27e79a5102227074f474050fb1b5c775e9a91248337d50f1850f068` | Scalar-Set identity/native/metadata controls |
| `docs/hosted-native-data-preflight.md` | `b0f7986f4056e7a4f0b76072b8d334f9347dd2a4909ae4060ed21373d394cebc` | `b0f7986f4056e7a4f0b76072b8d334f9347dd2a4909ae4060ed21373d394cebc` | Byte-identical current equivalent |
| `core-construction-root-binding.md` | `34ffa6e546a73b3d7c0b016eb94ffd0e29e01d6d3a7d3c05b6f32c4a4f69c4b8` | `8f774cf026ca88902728dd99db1286e8999b0f817a5638b65677f4f5eef37947` | Current receipt and Set evidence |
| `core-session-invocation.md` | `1b1c1d08f79e97de72df30aa907badd7d62927523c2308e54a7303606f87d422` | `d3d180af2d357861be39d5fb8a412608b1e0bfb6b081c21a3c7d786881005673` | Historical/current Throwable reconciliation |
| `core-session-known-diagnostic.md` | `fe18eb7ddb8ac8bdc0a3b029ab45d6d6fe637875e8d6b9b23d3a060ef2491765` | `df7b3d7aa6ee846b67b5546fbc6c97d8dd6d7268df1cbd92416f6127e797cca9` | Closed diagnostic/nonclaim receipt |
| `expected-core-failure-source-trace.edn` | `28c9e8313b1536fad123905aa32abd3cfe133ba720e406280403649a38bcd580` | `9068ff7bc776a133642c7934dd352651eac634ccde25ee85ff3fc16efb793b13` | Data-only allowlist receipt |
| `expected-core-failure-source-trace.md` | `6511892647b979b1fa03fb08a7935ad8ad4577ba7f9939ccd8ce33c1bc9ed8a1` | `7a0a72238bf084268ecc9c5bf8c071933ff90d9116f191ac506a268083d81fdd` | Historical trace/nonclaims receipt |
| `tools/ci/lint_context.clj` | `4a05e7b7e6fd89c60f56cf208aa0543a93030ba3355bf344d41db8e3d8a60feb` | `e76ea789088260edd7ac326a0af301f6d1770d913da29f09826024ac1a14a3bc` | Current 697-owner archived-plan port |
| `tools/ci/lint_context_test.clj` | `5f9292dcbc8ce8c6003e3f20201779af29965f824d87d1b4265aa9472cb235b4` | `8e03f6beeb0739986cf260a6ca4f24ba24b2217c6ad8208c49531d719283f770` | Adapted current rich-context test |
| `tools/ci/run_test.clj` | `3ffe66ffb6eadd10015df32550b0e4cca125e27fc1254eace42f486547c94c67` | `c3bf942d086c570471f278b24c2360f14ffe003eae0fbdd7ffd9d8d08cefa3a1` | Loads/runs current archival test namespace |

No authored path was dropped. The old `lint_context_test.clj` was not copied
whole because its pre-697 selected-plan assumptions were stale; the current
adaptation covers bootstrap, facade, checker, C2 pass-cache, loader SHA and
prefix-alias boundaries. The optional large Set control remains incomplete and
was not retried.

## Focused evidence

- Historical ac07 `ci.run-test` plus the new namespace: 38 tests / 366 assertions, zero
  failures/errors, with pinned kondo on `PATH`.
- Archived-plan focused controls: 3 tests / 21 assertions, zero
  failures/errors.
- Historical ac07 facade/context suite: 27 tests / 494 assertions, zero
  failures/errors.
- Core binding/session: 7 / 89 and 12 / 328, zero failures/errors.
- SH01 list/membership/literal Set selectors: 1 / 100, 1 / 147, and 1 / 87,
  zero failures/errors. The three restored scalar-Set controls also pass:
  helper arity 1 / 47, direct native view 1 / 261, and construction
  observation 1 / 75, zero failures/errors. They cover both real helper
  `L2-FUNCTION-ARITY` entries, direct insertion identity, benign/cyclic
  metadata refusal, and the `conj` hit/miss construction seam; retired
  comparison/insertion and work-accounting fields remain absent.
- Pinned clj-kondo 2025.10.23 on the three changed CI files: 0 errors, 0
  warnings. Pinned cljfmt 0.13.4 reports all `tools/ci` files formatted.
- Language boundary: 6 tests / 11 assertions, zero failures/errors.
- Document validator: 240 docs / 19 phase indexes passed. Full-language
  roadmap validator passed; its dated coverage snapshot remains
  non-authoritative.

At the final main-885 code checkpoint, the fresh pinned CI harness passed 45
tests / 480 assertions and the fresh facade suite passed 35 tests / 559
assertions, both with zero failures/errors. Pinned clj-kondo on `tools/ci`
reported zero errors and warnings. These fresh receipts are recorded under
`:current-code-checkpoint` and do not self-hash the final receipt files.

The historical ac07 post-composition current-main checks independently pass:
reader binding and session 19 / 417, scalar-Set controls 3 / 383, CI runner
38 / 366, and facade/context suite 27 / 494, all with zero failures/errors.
The current `tools/ci` tree has zero pinned-kondo errors or warnings; current
boundary, document and roadmap logs are recorded in the EDN receipt. These
checks are bounded Clojure/source/data controls and do not upgrade any
hosted-native or full C5-C8 claim.

These are finite Clojure source/data/fixture controls. Hosted-native files are
byte-identical current equivalents; no native execution was performed. No
actual C5/C6/C7/C8, public admission, migration, release or self-hosting claim
is made. Historical failed runs and the stopped large-result attempt remain
nonpasses; no retry, heap workaround, or source-pin renewal is implied.

## Additional global macro repair/adoption frame

The accepted PR2516 netpatch is a separate current test repair. It is not one
of PR721's 17 authored inputs and does not replace, rewrite, or reclassify any
of those inputs. It strengthens the public macro receipt interposition checks
by using a real source-backed `defn-basic` artifact and the real verifier report
while retaining the existing cache, stage, seed-operation, and invocation
guards.

| Item | Identity or result |
| --- | --- |
| Repair base | `1a3ea15c0ccb140e6420f6d447b82bcf826c05f9` / tree `ae84d7d87404d5949e0c805853a8c589349e7b0c` |
| Code-only checkpoint | `d0f2049c6754b45d2d139efadb4d5d95be9bdb31` / tree `624e5f528ece12e9e1929c6e339627a92dbd6a6e` |
| Changed path | `bootstrap/clojure/test/gravity/macro/pipeline_test.clj` |
| Original file blob | `c1e961617a2e0795516091a25e1a338f54004b78` |
| Repaired file blob | `2670c12834328613a0f72ba025ec9fc42c383181` |
| Accepted patch SHA-256 | `91d7f9e8b039728e674ea97259915774781f5f760aa35d5a9c633fa30795ce78` |

The origin receipt `2516` remains attributed only to its three focused controls:
197, 27, and 8 assertions respectively. Exact-head CI is pending, and the
full local macro cohort was not rerun. The actual PR721 900-second timeout
artifact `13264510655` remains historical nonpass evidence; it is not waived or
reclassified by this repair.

The 19b source checkpoint, ac07 historical composition, f891 code checkpoint,
and all 17 PR721 source inputs remain unchanged. Receipt paths are excluded
from the input set and are not self-hashed. This additional test repair claims
no hosted-native execution, C5-C8 acceptance, public admission, migration,
release, self-hosting, or seed-retirement result.

## Disposition

The 17 authored paths are fully accounted for and the useful current-source
delta is frozen on the final main-885 code checkpoint, but this candidate
remains `HOLD` pending independent review, integration preflight, and
coordinator publication. The PR697 owner composition is a dependency only; it
is not credited as PR721 work.
