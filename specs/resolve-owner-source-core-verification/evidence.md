# Evidence: complete Resolve owner source through fresh Core

## Tested revision and identity

Observed 2026-10-26; final independent acceptance at 08:00:16 UTC.
Tested [frozen cohort](https://github.com/burn2delete/uniform/tree/1927aaee826766ea3bf61af609b2333ccd3b5c43):
`1927aaee826766ea3bf61af609b2333ccd3b5c43`, tree
`433c9b66bebace4591fa3c7fc96af962f053249d`.
Host source tree `449814ab4e03a7664f28f056bf94048594afed3b`;
`deps.edn` blob `f05e98ad6df21dca09d9183a82cb8d0ec30c23de`.
Java 21 and Clojure 1.12.6; CLI 1.12.6.1673.

This documentation candidate starts from main
`c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e`. That is NOT the tested compiler cohort.
Historical source identities are not repinned to the documentation base.

## Source closure

Paths below are relative to the tested repository revision. All bytes and
SHA-256 values were checked before and after every phase.

| Source | Bytes | SHA-256 |
| --- | ---: | --- |
| `bootstrap/gravity/src/gravity/resolution.gravity` | 94395 | `6243c361dc86902e40965c1fcbeabaadea7d898b587d8ba987842b3c8c28b408` |
| `bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity` | 55315 | `def0be3cab408079b0520539f2d572905da9c938e0b86017a80c462e8ba63656` |
| `bootstrap/gravity/src/gravity/bootstrap/reader.gravity` | 231576 | `d61a4224b6928b80d66c0f99e3a26f58ed8daa171d088bcecef0062c8913d146` |
| `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity` | 81241 | `afcab42f39743e1609657e389ee478a79f8e98cabcc6fe331c2168106e584553` |
| `bootstrap/gravity/src/gravity/macro.gravity` | 56488 | `19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549` |
| `bootstrap/gravity/src/gravity/checked_core.gravity` | 503426 | `455f2d496e410ba8c5f4d17b2e4e558c6fb62306b614a6b9643107c8e1c82770` |
| `bootstrap/gravity/p15_s23/compiler.gravity` | 128447 | `be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854` |
| `bootstrap/gravity/p15_s23/emitter.gravity` | 1670330 | `23d49699bf187d56714257f88056bc0805ed158c4a76e7ea85af660c41d39d9c` |

The subject contains 114 top-level forms: namespace, 111 functions and two DATA
definitions, with five exports and no module imports or qualified live calls.
Quoted symbols remain DATA. Their inventory was checked independently against
genuine Reader output, not evaluated or substituted for that output.

## Observed staged execution

Thirteen construction, coverage and control invocations exited naturally with
zero status and unchanged clean closure checks. All products were captured in
full before acceptance, with parsed equality, byte hashing and no incomplete
capture remaining. Independent review checked the actual retained artifacts,
identities, coverage and negative reports, not only this summary.

| Stage | Accepted finite coverage/result |
| --- | --- |
| Reader/Syntax | 114 ordered source roots plus one generated conformance object; exact original C2 nested in C3. |
| Macro | 114 roots, 113 namespace definitions, 111 generated functions, namespace plus two DATA forms retained. |
| C5 | 113 definitions, 871 bindings, 4315 references, 413 scopes and five exports; all three actual Macro construction checks passed. |
| Fresh C5 | Generic public verifier passed; two fresh Macro checks passed. |
| Binding negative | Genuine namespace binding name changed; outer identity recomputed/current. Binding semantics and embedded proof rejected. Identity projection remained unchanged, not a hash collision claim. |
| Caller selection negative | Changed caller AE source mode rejected at exact early C2-HASH explicit source-selection boundary; no deeper-authentication claim. |
| Core | 114 source forms, 111 functions, 113 definition roots; 10391 retained request occurrences partition into 8521 node-mapped and 1870 retained-only, with 8521 Core nodes. |
| Fresh Core | Generic verifier passed with no failed checks; no supplied expected Core or verification session. |
| Core negative | Genuine Bool node 38 changed false to true, coherent semantic identity recomputed; canonical, authoritative-product and module replay rejected while identity, fresh upstream, request, template and stored-proof checks remained true. |

Core original semantic ID:
`sha256:4778a85c00db3879f56c841a2e35281b24c5798e032ab2200308d7b9a2053e8f`.
Negative candidate ID:
`sha256:13d4c42954d142b2d82837757adbd8531d7065eb039126da137b72c5f5e7067a`.

## Complete artifact and report receipts

These are identities of retained execution captures, not files included in this
small documentation package. Absolute source locations embedded in captures can
make their serialized hashes workspace-specific; the source closure above is
separately pinned. This package is a reviewed evidence record, not a standalone
replay harness or a distribution of the large captured products.

| Capture | Bytes | SHA-256 |
| --- | ---: | --- |
| C2 | 53877410 | `d331c3a5708ce10234e840b276abe24d4ea6292b72561fb045755216ef2554bc` |
| C3 | 44537651 | `e6d85eb4b2e19830e745811783863ed76006a825c6b05af00710e2494b0e426d` |
| Macro | 52704243 | `91120e18514dc3908f2a7d8976167438749e32c84caef4aaff067d7a62017aa5` |
| C5 | 101995718 | `45f2192e4f704f0ae3804a3333c6288c25146bd0bfb3ac9c3ab29a23be704495` |
| Core | 499446343 | `5d1f34e229d3ebb156d92d227172eb68adb5fc7da7230623106b0ebdd741779d` |
| Fresh C5 report | 30917 | `46b81241c850368658e3df4d55a9e0b800082c9f1898a8bd3ce800e71441a08a` |
| Binding negative report | 31139 | `de78f055f1b81da2d462406e01aa0be862de435e825b38de5f0229c1b26ecba6` |
| Caller selection report | 5019 | `809c6133f8a04b0917099b1031803b59d86989c2ee4c8e2a2196819851935150` |
| Fresh Core report | 33324 | `f3a27fe9e445b3a1e64c017b0809371578eae4737343d2d83b6142518a1fe047` |
| Core negative report | 33857 | `b4332f635bdbac08b33777652bc8064425531854696ea1e7b67bf0e095451ff5` |

## Machine profile and timing scope

Reader through C5 used a 2GiB heap. Core used a reviewed 3GiB single-heavy-JVM
profile before its first attempt, preserving full serialization and equality.
The initial Core host guard was 1080 seconds construction plus 120 capture;
fresh and negative verification used 1800 plus 120 before their first attempts,
based on observed construction work. These are host safeguards, not language
quotas, speed claims or evidence of arbitrary resource safety. The cgroup hard
limit was unknown; prelaunch available memory was approximately 7.6GiB.

Core construction returned at cumulative harness elapsed 843.428s and completed
capture at 910.269s (whole process 912.556s). Fresh Core returned at cumulative
1011.328s (whole process 1016.591s); negative returned at cumulative 1015.343s
(whole process 1019.103s). These event times include setup/read activity and are
not exclusive verifier durations or cross-cohort performance comparisons.

## Acceptance and documentation integration

Execution independently accepted at 2026-10-26 08:00:16 UTC after final negative
completed at 07:59:40 UTC. Source/host closure stayed unchanged, all 13 execution
logs had natural zero exit and closure checks, and no incomplete captures remained.

Independent documentation candidate review and repository integration
are pending. No PR number, merge, current-main execution, complete component,
full SH07, C7-C10, all-function runtime execution or seedless completion is claimed.

## Documentation candidate validation

Observed 2026-10-26 after preparing this four-file package:

- Local Clojure artifact/link check passed: four required ASCII artifacts,
  15 relative file/heading links; missing-file and missing-heading controls rejected.
- `tools/validate_gravity_docs.clj` passed: 240 docs and 19 phase indexes.
- `tools/validate_full_language_roadmap.clj` passed.
- `git diff --check` passed.

The first structural-validator command omitted the repository classpath and
stopped before validation with a missing tooling namespace. Correcting the
classpath to include repository source/test roots produced the passes above;
the setup failure is not counted as a passing check. No compiler rerun was
performed for this documentation-only candidate.
