# SH-25 Component-Build Leaf

This fixture root exercises an early Gravity-owned SH-25 component-build
contract governed by `D1`, `D3`, `D8`, `D9`, `BOOT1`, `BOOT3`, `BOOT5`,
`BOOT7`, `BOOT8`, and `TEST13`.

The Gravity engine freezes the complete 42-module authoritative source
inventory in `docs/self-hosting-slice-ownership.edn` and adds the current
provisional SH-19 runtime leaf, for 43 dependency-ordered component inputs. It
validates:

- the exact component identifier, category, source path, and dependency order;
- exact current source byte counts and SHA-256 identities frozen into the
  Gravity engine;
- closed SH-02 envelope descriptors contextually bound to component, source
  revision, compiler identity, semantic root, provenance root, and verifier;
- closed SH-21 legality records bound to the same component, source, compiler,
  and prerequisite product;
- complete supplied artifact, manifest, diagnostic, and provenance records
  bound to the output content identity;
- accepted `:meta` legality with no effects, capabilities, or ambient authority;
- exact SH-21, SH-22, and SH-24 artifact, source-revision, semantic-root, and
  verifier descriptors;
- deterministic, checkout-path-neutral identity inputs;
- separate physical-path provenance; and
- an exact ten-field component projection consumable by the current SH-26
  boundary only after the complete SH-25 result verification passes.

The rejected fixtures alter the catalog, dependency order, source identity,
authenticated input, output set, meta legality, prerequisite verification, and
top-level effects. Complete-result alteration is rejected by exact
recomputation. A paired source/envelope/legality/output substitution is rejected
against the Gravity-owned source revision catalog.

## Carrier validation and catalog authority

This inventory revision requires exactly 43 components with the declared
dependency vectors, source byte counts and source hashes. These equality checks
define catalog authority; there are no additional population ceilings.

Every request, candidate, and verification carrier is preflighted iteratively
before schema recursion, projection, or complete-result equality. Arbitrary
sequence carriers are rejected before `count`, `first`, `rest`, or realization;
maps, vectors, and sets are traversed through an indexed frontier. Carrier
node, depth, width and scalar-unit accounting is removed. Unsupported scalars
and integers outside the signed 64-bit domain remain rejected independently of
input size. Host heap and process-watchdog restrictions remain separate.

Successful preflight returns only `{:status :accepted}`. The legacy
`:request-census` and `:candidate-census` field names carry that validation
result without estimates. SH-26 still compares those fields with its own
accounting preflight, so authenticated SH-25-to-SH-26 composition needs a
separate consumer update before it can pass. Its integration assertions remain
in place; this slice does not change SH-26 or manufacture replacement evidence.

Carrier-kernel tests exercise finite inputs beyond former depth, width and
scalar-unit limits, late unsupported entries and sequence non-realization.
Separate complete-request and replay tests retain the 43-component population,
catalog order, identity and output/provenance joins. Neither group establishes
actual component compilation or authenticated composition evidence.

## Honest Boundary

This leaf validates and packages supplied component build products. It does not
invoke the SH-24 compiler driver, compile the 43 sources, compute source or
artifact digests, or independently establish the supplied conformance records.
The focused Clojure test independently checks each frozen source revision
against current repository bytes. Source loading, SHA-256, stage2 module
execution, construction of the supplied SH-02/SH-21/SH-22/SH-24 records, and
final orchestration remain in the Clojure seed boundary. Their verifier records
therefore state `:external-recomputation :pending`; this leaf validates their
closed shape and contextual consistency but does not claim to rerun their
owning verifiers.

The runtime component still lives under the SH-19 fixture root and is explicitly
marked provisional because it is not present in the authoritative ownership
inventory. SH-21, SH-22, and SH-24 products are supplied descriptors rather
than coordinator-routed authenticated artifacts.

This is executable Gravity-owned validation and deterministic construction, not
SH-25 completion. `sh25-verify-component-build` proves exact reconstruction of
the supplied records only. The emitted SH-26 projection and every
component in it remain `:pending`; exact reconstruction does not promote
component conformance or SH-25 verification to `:passed`. The projection keeps
the current SH-26 closed top-level schema; outstanding work remains explicit in
the SH-25 policy and complete result. SH-26 must reject this projection
because its status is `:pending`. A future authoritative adapter may emit a
passed projection only after it connects genuine prerequisite products, reruns
their owning verifiers, promotes an authoritative runtime source, executes
every action through the Gravity driver, independently validates the outputs,
and passes the uniform SH-25 gates. This leaf does not claim a self-hosted
compiler, complete component compilation, seed retirement, target support,
independent output validation, or release readiness.
