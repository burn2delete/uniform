# Native compiled package metadata checks

## Scope and governing contracts

This scope precedes implementation and its early draft PR. D1, D2, D3, D8,
D9, C1, C15, PKG1, PKG3, PKG4, PKG5, PKG6, BOOT8 and TEST2 govern this
extraction. The starting source is
`7a4f961ac34cb9d30b6510b1b0598bd190b2a0d6`, tree
`00dbe18575a15476c3de8b6fe7905dba0afc4d5b`; published main
`33811b2b5c1a1c57c2243da12719ca0c4edbfdce` is already its ancestor.
Prior slice results confer no acceptance on this successor.

## Proposed stage boundary

Move the five existing native compiled package metadata decisions from
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

## Evidence obligations

Before/after controls must compare complete direct and Bootstrap-wrapper
results, diagnostic payloads, callback order and exact exceptions. Cold native
tests cover truthiness, malformed/missing data, deny/request overlap and late
failure lookup. Paired `.gravity` and `.qst` fixtures must compile and emit real
instruction plans, execute the accepted program, and reject each metadata
failure before execution. Their paths, source hashes and deterministic plan
identity remain attached to the emitted plans. This ordinary Stage0 route uses
the existing host reader and instruction runner; it is not authenticated C2
or package admission evidence.

Require boundary/ownership, existing package regressions, complete namespace
CI selection, prior command-vector conservation, document/roadmap checks,
current-main context and independent semantic/adversarial/provider review.
Results will be recorded only after execution.

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
