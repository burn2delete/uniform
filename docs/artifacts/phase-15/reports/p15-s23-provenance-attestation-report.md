# P15-S23 Bootstrap Provenance Attestation Report

Date: 2026-06-30

Task: `P15-S23`

Status: implemented for the current Clojure-seed candidate; implementation
incomplete for whole-language compiler self-hosting

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-provenance-attestation.edn`

Artifact id: `sha256:b01ec777d0a32981fc202cf4fbddab712fe9a6b27567127d65068369f2250163`

Proof id: `sha256:f75f691df72dac681568a06409613995133eae970398e1456d501e6a61847f64`

## Capability Proven

`p15-s23-provenance-attestation` verifies the Gravity-authored
`:gravity/bootstrap-provenance-attestation` contract in
`bootstrap/gravity/p15_s23/compiler.gravity`. The proof links the current
compiler source inventory, compiler pipeline manifest, reproducible rebuild
log, stage comparison report, and self-hosting conformance report.

The artifact records BOOT8 provenance fields, a compiler lineage graph that
answers which compiler compiled this compiler, a canonical provenance payload
with a deterministic stage0 attestation signature, a required evidence link
table, a revocation check report, and an auditor query index. It keeps release
eligibility, whole-language compiler self-hosting, and Clojure seed retirement
false.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-provenance-attestation bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:b01ec777d0a32981fc202cf4fbddab712fe9a6b27567127d65068369f2250163`, proof id `sha256:f75f691df72dac681568a06409613995133eae970398e1456d501e6a61847f64`, provenance record id `sha256:7e3d2dadef4b0cc1d98071e1e442ab089bb71d24e95b339c8d9f4701f591769c`, canonical payload id `sha256:3d763f4224a512d92463c24edec3ee142840117b2b7d155d188c55d95d865be1`, 16 required fields, 5 required evidence links, `:lineage-traversable-to-seed? true`, signature status `:verified`, `:revocation-clear? true`, `:auditor-query-passed? true`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: records provenance attestation evidence for `P15S23011`, records TCB delta evidence for `P15S23012`, records unsafe audit evidence for `P15S23013`, reports 1 remaining missing evidence category, and points next to `:retire_clojure_seed_boundary`.

## Rejected Proofs

The verifier rejects internal provenance candidates with stable diagnostics:

- `P15S23P001`: missing provenance attestation contract.
- `P15S23P002`: missing BOOT8 fields or preservation facts.
- `P15S23P003`: compiler lineage gap, cycle, or failed traversal to seed.
- `P15S23P004`: missing required evidence link.
- `P15S23P005`: noncanonical payload or invalid deterministic signature.
- `P15S23P006`: revoked input or failed auditor query.
- `P15S23P007`: unsupported full self-hosting or seed-retirement claim.

## Remaining Phase Work

This does not implement a whole-language self-hosted compiler, retire the
Clojure seed, create release eligibility, or satisfy governance/package release
evidence. The P15-S23 gate remains incomplete.
