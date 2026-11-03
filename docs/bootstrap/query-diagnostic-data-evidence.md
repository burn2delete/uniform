# Query native diagnostic policy data

## Contract boundary

D1, D2, D3, D8, D9, C1, C15, B1, B11, B13, B14, BOOT8 and TEST2 govern
this extraction. Three existing native functions move to gravity.backend.query:
source-overrides, missing-policy and query-id. Native module metadata and
diagnostic ids produce borrowed overrides, missing-policy labels and query
labels. The backend id remains :gravity.backend/query-relational, distinct
from the hosted profile and PostgreSQL target. The stage imports only
clojure.core; Bootstrap consumes it through existing late-interposition wrappers.

The stage owns native projection and rule-context defaults. Refusal decisions,
failure construction, diagnostic streams, source admission and publication
retain their existing owners. Component and ownership records declare exact
source, consumer and test paths. The SH-18 allowlist adds only the explicit
query owner, with actual, wrong-slice and coherent unknown-owner controls.

## Artifacts and checks

Focused tests emit and read back an actual B11-MANIFEST diagnostic for an empty
malformed native artifact. Supplied source context, hosted profile, PostgreSQL
target, effect and capability fields retain their distinct identities. Catalog
rows are compatibility examples, not eleven observed semantic findings.
Tests preserve the single specialized-lowering metadata path, including false
and nil values; other backend keys do not gain precedence. Unknown defaults,
borrowed identity, deterministic serialization and stage/wrapper interposition
remain observable.

Frozen-parent/candidate captures compare six complete original serialized
records, recompute source/input digests and retain eleven actual override
refusals. The baseline ran at the frozen Workflow source before any Query
edit. The subsequent parent differs only by three List-join test/evidence
files; no native Query implementation changed before extraction. No compiler
mode, source pin, callback policy or admission fact changed to obtain evidence.

## Unsupported boundaries

This slice extracts native diagnostic data. Full B11 compilation, verified
relational/schema ingress, SQL/query-plan emission, database execution,
dialect/type/null mapping, prepared bindings, taint/capability/transaction
verification, migrations, result decoding and provider-plan equivalence remain
unsupported. Source authentication, downstream handoff and public diagnostic
publication are outside the slice. Native proof, validated, complete and
capability labels remain data. No held native broker, restricted factory,
default frozen Reader, package/archive/release builder or Attempt30 route
executes. Release, migration, self-hosting and seed retirement are not claimed.
Independent review, own CI/artifacts and exact clean current-main integration
preflight remain required.
