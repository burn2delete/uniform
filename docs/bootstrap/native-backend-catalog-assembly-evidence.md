# Native backend catalog assembly

## Contract boundary

D1, D2, D3, D8, D9, C1, C15, B1, B9, B10, B11, B12, B13, B14,
BOOT8 and TEST2 govern this extraction. Four existing catalog assemblers move
to gravity.backend.hdl, gravity.backend.workflow, gravity.backend.query and
gravity.backend.mobile. Each adds diagnostic-stream(source-path, input-id,
operations) alongside its three existing native lookup operations. The stages
import clojure.core and clojure.string; Bootstrap consumes them.

Inputs are a native source path, supplied input identity and explicit native
operations for spans, diagnostic ids and the two backend context lookups.
Outputs are the existing ordered native catalog records. Stages own catalog
assembly; existing owners retain refusal decisions, failure construction,
source admission and publication. Namespace contracts record both actual
consumer component paths, public APIs, artifact inputs/outputs and dependency
direction. Existing component/ownership registrations and focused test paths
remain valid; this adds no ownership category or admission mechanism.

Wrappers retain their two-argument signatures. Operation closures resolve
existing Bootstrap vars when invoked, including after capture. The diagnostic
id operation runs once per assembly, matching the former vector lookup;
span/context operations remain observable for each row. Supplied operations
are implementation dependencies, not proofs or authenticated compiler inputs.

## Evidence

The frozen baseline records all four native Gravity source texts and hashes,
their actual source paths, supplied input ids, complete catalog streams, four
actual malformed-manifest refusals and 41 actual override refusals. Catalogs
contain 41 compatibility rows; those rows are not observed semantic findings.
Before/after comparisons retain complete original serialized records, and
source/input digests are recomputed. Implementation identity sidecars remain
separate from native record identity; a changed implementation is not hidden
behind an old source fingerprint.

The initial after capture ran during source editing. Its HEAD-only sidecar is
work-in-progress metadata, not an immutable candidate identity. Original files
are retained unchanged. A fresh clean-candidate capture records the actual
commit/tree, clean state and host versions before final evidence acceptance.

Focused tests retain existing real refusals and serialization checks. Added
tests exercise borrowed source spans, explicit ids/context operations, ordered
invocation, late stage and Bootstrap interposition, and exact propagation of a
failed native span operation. Whole-file conservation checks compare all four
stage files and all four consumers against the frozen parent, including every
unrelated consumer prefix and existing lookup body. Only the moved function
name, arity and explicit dependency calls change in the catalog bodies.

The first test generator failed on a format placeholder before writing tests.
Its original helper, log and exception remain nonpass history. The corrected
generator changes the formatting mechanism, not fixture expectations.

## Composition and unsupported boundaries

The baseline completed at clean Mobile source 56b2daa before catalog edits.
Normal composition d4489869 includes main 9381b2db. That main tree equals the
Mobile source parent's tree; four shared-file conflicts were resolved only
after that identity check. The entire resulting tree equals the frozen Mobile
tree. No provider commit was adopted, and no failed CI was rerun or transferred.
Mobile PR866's Resolution timeout remains a nonpass with no established cause.
This new extraction is not a timeout fix.

These are legacy native catalogs. Canonical C15 schema, resolved diagnostic
identity, semantic findings, input authentication, public redaction/binding and
publication remain unsupported. Existing synthetic ids, spans, proof, complete,
policy and capability labels remain data. Full B9/B10/B11/B12 compilation,
verified IR ingress, backend generation/execution, platform or device checks,
and certificates are outside the slice. The Workflow budget-bearing label adds
no project accounting enforcement. No held native broker, restricted factory,
default frozen Reader, package/archive/release builder or Attempt30 route
executes. Migration, release, self-hosting and seed retirement are not claimed.
Independent acceptance, own CI/artifacts and exact clean current-main preflight
remain required before integration.
