# Remaining compiled package metadata checks

## Scope and governing contracts

This scope precedes implementation and its early draft PR. D1, D2, D3, D8,
D9, C1, C15, PKG8, PKG9, PKG10, PKG11, PKG12, BOOT8 and TEST2 govern the
slice. Starting source is `7bc6450f69e42e233d2f46fcdacc0f21f1534135`, tree
`c8e2ad5bd2eee4c3487f388515917b4cf97844e4`; published main
`33811b2b5c1a1c57c2243da12719ca0c4edbfdce` is already an ancestor.
Prior source, provider or CI acceptance does not transfer to this successor.

## Proposed Package boundary

Move the five remaining compiled package metadata predicates from 00538 and
00539 into `gravity.package`:

- `check-package-safety! [presence-var fail-var module subject]`: PKG8001
  supplied unsafe-audit metadata presence through the existing P12 operation.
- `check-registry-record! [fail-var module subject]`: PKG9001 literal access flag.
- `check-provenance-record! [fail-var module subject]`: PKG10001 source graph
  hash field truthiness.
- `check-target-matrix! [fail-var module subject]`: PKG11002 literal implicit
  host target flag refusal.
- `check-signing-sbom! [fail-var module subject]`: PKG12002 literal canonical
  signature payload flag.

Bootstrap supplies its existing `p12-present?` Var; the stage must resolve that
operation at the original callee position before observing its argument. Do
not duplicate its `coll?`/`seq`/`some?` behavior as generic truthiness. Each
failure Var is read only at its original failing branch. Trusted native module,
subject and caller-owned ports produce nil or the original callback result or
exception. The stage owns only these decisions. Bootstrap retains presence
semantics, payload/source-span construction, failure publication, aggregate
order, source compilation, plan assembly and final artifact identity.

Package must not require Bootstrap; Bootstrap consumes Package. Existing
Package and Build APIs remain intact. No fixed fixture constructor, P12 final
validator, registry operation, safety analysis, target legality or release
assembly is included.

## Evidence obligations

Extend the existing cold Package and compiled Package test namespaces and the
dedicated paired `.gravity`/`.qst` fixtures. Keep their current CI commands and
test registrations unchanged. Capture the extended accepted fixture and all
ten metadata refusals before moving production bodies, then compare complete
parsed/serialized records after extraction. Emit real deterministic Stage0
plans with source paths/hashes, execute accepted programs, and demonstrate each
refusal before execution. Use only the existing ordinary hosted Stage0 reader
and instruction runner.

Cover literal booleans, absent/empty/nonempty collections, empty strings and
false under P12 presence, provenance truthiness, inverse target predicate,
malformed host inputs, exact lookup and callback order, late presence/failure
roots, callback returns and exact exceptions. Preserve all twelve Package/Build
aggregate positions, earlier five stage checks and existing policy/selector
bodies. Require focused regressions, boundary/ownership, conservation, current
main context and independent semantic/adversarial/provider/CI review before
integration. Report only executed evidence.

## Unsupported boundaries

Native audit/grant/provenance/target/signature labels remain unverified data.
This slice does not authenticate safety records, principals, registries,
provenance hashes, builder identities, signatures, canonical payloads, SBOMs,
attestations, target support or capability authority. It performs no registry
read, network operation, signing, install, lock write, publish, artifact
admission, release, self-hosting or seed retirement.

The held C2-HASH routes, Reader bindings and pins, private package/JVM factories,
backend source builders and package/release admission remain uninvoked and
unchanged. Ordinary hosted compilation is development evidence only.
