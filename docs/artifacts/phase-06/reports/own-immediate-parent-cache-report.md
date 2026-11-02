# OWN immediate-parent cache admission

Base: `dfa4c5b653d8049ac78dd30962172e0d91c768e2`.
Governing contract: `docs/end-to-end-contracts.md`, OWN-02; C16 incremental
compilation and C18 compiler verification. This report grants no OWN completion credit.

## Repair and evidence

`bootstrap/clojure/src/gravity/authenticated_artifact_pipeline/04-envelope-valid.clj`
already compares newly constructed payloads with the selected immediate parent.
Its cached branch now invokes the same `authenticate-embedded-parent!` before
owner replay. A foreign, absent or facade-domain parent causes eviction and
reconstruction. An invalid reconstructed parent still raises `ARTIFACT-PARENT`
with stage, expected parent and observed parent; it is not published to cache.
Source and Reader retain their existing admission checks.

`bootstrap/clojure/test/gravity/artifact_pipeline/parent_cache_test.clj` evaluates
the actual production parent and stage-admission functions in isolated
namespaces. Envelope integrity, owner verification, payload construction and
cache plumbing are explicit stand-ins. These controls do not demonstrate an
owner-authentication bypass or authenticate compiler artifacts. They deliberately
allow other gates to pass so the selected-parent comparison is observable.

After correcting the initial fixture's missing actual-path provenance, the
unmodified source failed 84 assertions in 3 tests (116 assertions). The repair
and expanded controls passed 5 tests and 134 assertions. Controls cover all four
embedded-parent stages, valid cache hits, owner rejection, Source/Reader routes,
foreign/missing/facade-domain parents, and failed fast/fresh reconstruction.
The test command asserts that `gravity.bootstrap` is absent before and after:

```sh
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/test"]}' -M -e '(require (quote gravity.artifact-pipeline.parent-cache-test)) (assert (nil? (find-ns (quote gravity.bootstrap)))) (let [r (clojure.test/run-tests (quote gravity.artifact-pipeline.parent-cache-test))] (assert (nil? (find-ns (quote gravity.bootstrap)))) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The language boundary passed 6 tests and 11 assertions. No compiler owner,
runtime, provider or broker was loaded by these controls.

## Finite remaining gap assessment

| Contract | Concrete interface and remaining evidence |
| --- | --- |
| OWN-01 | Reader/Syntax scalar coordinates, source identity, actual paths and expansion origins remain governed by the original paired-source rows. The accepted Reader receipts and finite decimal/lineage handoff do not populate every original source/diagnostic row or authenticate full-C11 C3/C5 consumption. Preserve Reader/C14 and selected-source ownership. |
| OWN-02 | The facade stages in `authenticated_artifact_pipeline/04-envelope-valid.clj` end at ordinary SH07 canonical Core, through `sh07-core-from-resolution-artifact`. This repair checks cached edges; actual owner-produced Source -> Reader -> Syntax -> Expand -> Resolve -> Core population and current module inputs still need genuine construction/replay evidence. PR388's paired-source pilot remains finite. |
| OWN-03 | `bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity` and the Native `00726-p15-s23-c6c10-gravity-candidate-verification.clj` component carry the fused producer boundary. The retained Bool fact substitution is rejected by `C11-TYPE/:id-indexed-c7-type-facts`. Every claimed effect/profile/grant/ownership/safety/origin fact still needs actual owner population and substitution at MIR; fixture declarations cannot supply it. |
| OWN-05 | `bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity` and `c14_target_lowering_architecture.gravity` must consume the same accepted MIR, with post-pass verification, invalidated/regenerated facts and actual target/ABI/provider identities. Stale-input and proofless-elision negatives remain required. Verified unoptimized MIR is allowed. Draft PR370 and contextual PR374 confer no contextual C14 acceptance. |
| OWN-06 | The current base's `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00744-opaque-c11-construction-token-helper-02-invoke-checked-core-verifier.clj` invokes the unversioned verifier; its `03-authenticate-checked-core-ingress.clj` checks semantic authority with `C11-MODULE`. The separate v3 successor and versioned downstream candidates are not established by this base or repair. Genuine current-source public construction/fresh report parity and version propagation through `00743-p15-s23-c11-mir-validate-final-artifact.clj` remain necessary, including source/context/policy/report/artifact negatives. |

OWN-04's accepted Bool development adapter is unchanged. ACC10 owns actual Core
producer/harness work; ACC11 owns the versioned downstream consumer; ACC13 owns
its selected-source work. This patch changes none of their implementation files.

## Next acceptance operation

Freeze and independently review this source patch and its compiler-free controls.
The subsequent semantic evidence operation remains real immediate-parent
construction/replay over the required source population through the existing
owner's authorized harness. This report neither executes nor authorizes held
runtime operations, migration, integration, final admission or whole OWN closure.
