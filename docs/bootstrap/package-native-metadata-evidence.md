# Native compiled package metadata checks

## Scope and governing contracts

This scope was committed before implementation and early draft PR #1278.
D1, D2, D3, D8,
D9, C1, C15, PKG1, PKG3, PKG4, PKG5, PKG6, BOOT8 and TEST2 govern this
extraction. The starting source is
`7a4f961ac34cb9d30b6510b1b0598bd190b2a0d6`, tree
`00dbe18575a15476c3de8b6fe7905dba0afc4d5b`; published main
`33811b2b5c1a1c57c2243da12719ca0c4edbfdce` is already its ancestor.
Prior slice results confer no acceptance on this successor.

## Stage boundary

The five existing native compiled package metadata decisions move from
`00537-validate-stage0-compiled-ai-provider.clj` into `gravity.package`:

- `check-project-manifest!`: PKG1006 lockfile-complete flag.
- `check-artifact-manifest!`: PKG3005 safety-evidence presence.
- `check-package-operation!`: PKG4001 download-verified flag.
- `check-resolution-report!`: PKG5002 capability-compatible flag.
- `check-capability-manifest!`: PKG6004 deny/request intersection.

Each API takes `[fail-var module subject]`, preserving the existing predicate
and dereferencing the original failure Var only at its old call site. Trusted
native module/metadata and a caller-owned diagnostic Var produce nil or the
unchanged callback result/exception. The stage owns these five decisions;
Bootstrap retains failure payload construction, publication, aggregate package
validation order, source compilation/assembly and outer identity. Existing
package policy/selection and Build checks remain unchanged.

## Executed evidence

The cold native namespace passes 8 tests and 146 assertions. The compiled
metadata namespace and existing package pipeline pass 8 tests and 165
assertions together; the unchanged package pipeline baseline was 3/38. Existing
Build package policy/selection regressions pass 16 tests and 243 assertions.
The old two policy/selector bodies remain unchanged; their documented arglists
retain descriptive parameter names while tests check their actual arities.
The five new APIs expose exact native arglists.

Before extraction, paired `.gravity` and `.qst` fixtures produced two real
instruction plans and ten metadata refusals. After extraction, every complete
parsed and serialized plan, source path/text/hash, output and diagnostic record
matches that baseline. Accepted programs print `package-metadata`; each invalid
fixture refuses with its original PKG diagnostic before execution. The baseline
receipt SHA256 is
`e1ded1c0ca55010395578175ff6d3ae35c247efd5725492fe2afa421e664d839`;
the complete after-extraction receipt is
`6ca13dadc85a32c236c8255a2dc29d04c77ca5dc784915a86c72507b9b9c7cfd`.
Each plan identity is recomputed over its complete content without `:plan-id`.
The ordinary existing host reader and instruction executor are the evidence
route; no authenticated C2 input or package-admission artifact is constructed.

Native tests preserve literal `true` checks, safety-evidence truthiness,
empty/missing capability sets, callback result and exception identity, and late
failure Var reads after subject observation. Compiled tests verify complete
Bootstrap diagnostic parity and the unchanged interleaving of Package and
Build checks, including a returning failure callback and first refusal.

Language/ownership checks pass 48 tests and 1202 assertions. The CI harness
passes 54 tests and 650 assertions, including complete namespace selection for
both appended commands. Document and roadmap validators pass. An initial test
asserted exact symbolic arglists for the old destructured policy APIs; that
overstrict assertion was corrected to their existing documented arities.
The initial harness run exposed its old nineteen-command expected list; the
list now includes the two appended Package commands. Both earlier nonpasses
remain historical evidence rather than being reported as successful runs.

Both complete appended namespaces pass under the existing 2g heap and
180-second host watchdog. Cold loading finds all seven public Package APIs
without loading Bootstrap. Conservation verifies exactly 24 owned source paths,
the previous policy/selector body text, all nonmoved 00537 forms, all nineteen
prior command vectors and eleven protected paths. Bootstrap's only change is
the explicit Package require. The original Build checks, package aggregate,
P12 final validator, report/source assembly, Reader source/pins/guards and CI
workflow remain intact. Seventeen generated evidence files are archived with
hashes outside the clean source tree. The conservation receipt SHA256 is
`3b889cd0988ebb2f89f59289ef5562d14bafe3be722c7d3f8fdb425c531d7eba`.

## Remaining acceptance

Before/after controls must compare complete direct and Bootstrap-wrapper
results, diagnostic payloads, callback order and exact exceptions. Cold native
tests cover truthiness, malformed/missing data, deny/request overlap and late
failure lookup. Paired `.gravity` and `.qst` fixtures must compile and emit real
instruction plans, execute the accepted program, and reject each metadata
failure before execution. Their paths, source hashes and deterministic plan
identity remain attached to the emitted plans. This ordinary Stage0 route uses
the existing host reader and instruction runner; it is not authenticated C2
or package admission evidence.

A clean source commit, complete current-main context, independent semantic/adversarial/provider
acceptance, own CI audit and final integration remain separately recorded
after execution. The early draft and local tests confer no integration credit.

## Unsupported boundaries

Supplied lockfile/download/capability flags and safety-evidence values are
native metadata, not authenticated files, downloads, evidence or authority.
These checks do not implement canonical project, artifact, package-manager,
resolver, effect-analysis or deployment permission semantics. They perform no
registry/network operation, install, lock write, artifact admission, signature
or SBOM verification, release, self-hosting or seed retirement.

The held C2-HASH source-artifact route, frozen Reader bindings, private package
or JVM factories, backend source builders and package/release admission remain
uninvoked. No Reader rebinding/repinning, upstream substitution or alternate
admission path is part of this slice.
