# Remaining compiled package metadata checks

## Scope and governing contracts

This scope was committed before implementation and early draft PR #1279.
D1, D2, D3, D8,
D9, C1, C15, PKG8, PKG9, PKG10, PKG11, PKG12, BOOT8 and TEST2 govern the
slice. Starting source is `7bc6450f69e42e233d2f46fcdacc0f21f1534135`, tree
`c8e2ad5bd2eee4c3487f388515917b4cf97844e4`; published main
`33811b2b5c1a1c57c2243da12719ca0c4edbfdce` is already an ancestor.
Prior source, provider or CI acceptance does not transfer to this successor.

## Package boundary

The five remaining compiled package metadata predicates move from 00538 and
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

## Executed evidence

The existing native Package namespace now passes 13 tests and 285 assertions.
The compiled metadata namespace and existing Package pipeline together pass
9 tests and 285 assertions. These extend the existing tests and registrations;
no new test namespace or CI command is added. Cold loading finds all twelve
public APIs with Bootstrap unloaded.

The extended paired fixture set produces 22 complete before/after records:
two instruction plans emit and execute, while twenty invalid cases refuse
before execution with the ten original metadata diagnostics. Every complete
parsed and serialized plan, source path/text/hash, output and diagnostic record
matches the pre-extraction baseline. Baseline SHA256 is
`e2825849994005491fd39acddb60d292013bc303c84aac089f6d97d3939dcd1e`;
after-extraction SHA256 is
`0d746af03c3f830bdbf9cd11f0196fd2999eef819acd16c9bd64e1caee5812fa`.
Each plan identity is recomputed over complete plan content without `:plan-id`.
Accepted programs print `package-metadata`. This is ordinary hosted Stage0
evidence, with no authenticated C2 or package-admission artifact.

The tests preserve literal registry/signature booleans, provenance truthiness,
the inverse target predicate, empty/nonempty collection and false/string P12
presence behavior, lookup/callback order, late presence and failure roots,
callback returns and original exception identity. The existing Package/Build
aggregate test now checks all twelve unchanged diagnostic positions.
Language/ownership checks pass 48 tests and 1202 assertions. The unchanged CI
harness passes 54 tests and 650 assertions. Existing Build package policy and
selection regressions pass 16 tests and 243 assertions. Document and roadmap
validators pass.

Both unchanged complete Package CI commands pass at the existing 2g heap and
180-second host watchdog. Conservation confirms exactly twenty owned source
paths, twenty-eight protected paths, the previous seven API body texts, all
nonmoved 00539 forms, the original P12 presence helper, aggregate/payload/source
assembly and all twenty-one prior command vectors. The workflow and test
registrations remain unchanged. Seventeen generated evidence files are
archived with hashes outside the source tree. Conservation receipt SHA256 is
`5a696a57660f7c0faa30feddf45cf320c3ee8c2abf1bc1b5f719b24420c7356f`.

## Remaining acceptance

Conservation, exact existing command execution, a clean source commit, complete
current-main context, independent semantic/adversarial/provider acceptance, own
CI audit and final integration remain separately recorded after execution.
The draft and local tests confer no integration credit. The earlier Package
slice and its evidence remain immutable history rather than acceptance for this
successor.

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
