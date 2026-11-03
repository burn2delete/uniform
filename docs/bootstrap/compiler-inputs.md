# Bootstrap Compiler Inputs

## Purpose

The bootstrap replacement project must name the exact compiler source closure,
seed identity and build declaration used for a rebuild. The declaration at
`contracts/bootstrap-inputs.edn` is consumed by
`gravity.bootstrap.rebuild.inputs`; it is not a release claim or a self-hosting
certificate.

## Requirements

- The source language and temporary seed language are explicit.
- The source closure uses deterministic roots, extension filters, sorted paths,
  file sizes and SHA-256 content identities.
- Direct non-source inputs, including `deps.edn`, carry their expected digest.
- Profiles, targets, supported compiler modules, accepted fixtures and rejected
  fixtures are stated separately from the source closure.
- Backend, runtime and residual host boundaries are explicit.
- Comparison modes and output artifact kinds are explicit.
- Missing, substituted, malformed or incompatible inputs fail before the
  rebuild executor is called.
- A rebuild result must bind its artifact identity to the verified source graph.

## Dependency and execution boundary

The validator is bootstrap tooling written in Clojure. It authenticates source
and policy data, then passes the verified record to an explicitly supplied
executor. The executor retains process, filesystem and publication policy; the
validator does not grant authority or certify the executor's implementation.

The current declaration describes the hosted stage0 path: Gravity compiler
source and Clojure seed source are consumed for a JVM target using the Clojure
stage0 runtime. Its accepted and rejected fixture lists describe the declared
conformance scope; they do not imply whole-language coverage.

## Outputs and artifacts

`load-inputs!` returns the authenticated manifest, observed source graph, graph
identity, scope, execution boundary and comparison declaration. `run-rebuild!`
returns that record together with the executor result after checking that the
result names the same source graph.

The source graph identity is content-based and changes when a declared file is
added, removed or substituted. Historical evidence from other revisions is not
renewed by this declaration.

## Conformance criteria

- The repository declaration loads successfully from the current checkout.
- A direct input digest mismatch is rejected with
  `BOOT-INPUTS-DIRECT-DIGEST-MISMATCH`.
- A missing source root is rejected with `BOOT-INPUTS-MISSING-ROOT`.
- A changed source graph is rejected with
  `BOOT-INPUTS-SOURCE-GRAPH-MISMATCH`.
- An incompatible execution target or backend is rejected with
  `BOOT-INPUTS-INCOMPATIBLE`.
- A rebuild executor cannot run before input authentication and its result must
  carry the authenticated source graph identity.

This boundary establishes explicit inputs for a bounded bootstrap rebuild. It
does not establish semantic compiler correctness, stage equivalence, seed
retirement, self-hosting or release readiness.
