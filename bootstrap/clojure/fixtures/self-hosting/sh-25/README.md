# SH-25 Bounded Component-Build Leaf

This fixture root exercises an early Gravity-owned SH-25 component-build
contract governed by `D1`, `D3`, `D8`, `D9`, `BOOT1`, `BOOT3`, `BOOT5`,
`BOOT7`, `BOOT8`, and `TEST13`.

The Gravity engine freezes the complete 41-module authoritative source
inventory in `docs/self-hosting-slice-ownership.edn` and adds the current
provisional SH-19 runtime leaf, for 42 dependency-ordered component inputs. It
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

## Bounds

- at most 64 components;
- exactly 42 components for this inventory revision;
- at most 16 dependencies per component;
- at most 8 output kinds per component;
- at most 16 diagnostic identifiers per component; and
- at most 16 MiB per declared source;
- at most 1,048,576 carrier nodes;
- at most 128 carrier levels;
- at most 65,536 children in one carrier aggregate; and
- at most 1,048,576 conservative UTF-8 storage units for one scalar; and
- at most 16,777,216 scalar units.

Every request, candidate, and verification carrier is preflighted iteratively
before schema recursion, projection, or complete-result equality. Arbitrary
sequence carriers are rejected before `count`, `first`, `rest`, or realization;
maps, vectors, and sets are traversed through a bounded indexed frontier.
Strings are charged directly from their existing character count at the
maximum four-byte UTF-8 width, so the preflight does not construct a duplicate
string or byte vector.

## Honest Boundary

This leaf validates and packages supplied component build products. It does not
invoke the SH-24 compiler driver, compile the 42 sources, compute source or
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
the bounded supplied records only. The emitted SH-26 projection and every
component in it remain `:pending`; exact reconstruction does not promote
component conformance or SH-25 verification to `:passed`. The projection keeps
the current SH-26 closed top-level schema; outstanding work remains explicit in
the SH-25 policy and complete result. SH-26 must reject this bounded projection
because its status is `:pending`. A future authoritative adapter may emit a
passed projection only after it connects genuine prerequisite products, reruns
their owning verifiers, promotes an authoritative runtime source, executes
every action through the Gravity driver, independently validates the outputs,
and passes the uniform SH-25 gates. This leaf does not claim a self-hosted
compiler, complete component compilation, seed retirement, target support,
independent output validation, or release readiness.
