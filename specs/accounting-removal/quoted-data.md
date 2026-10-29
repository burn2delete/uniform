# Quoted-data source successor

Planning: ACC-01/06/07/12/13. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This finite draft disposes of the
quoted-data source family preserved by PR502; it does not close either PR502 or
an ACC criterion. Base: `b7347f6b1b978dbc318e88dfe50e7917f7b1e318`.

## Contracts and implementation

[L1](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md),
[L2](../../docs/phase-01-core-language/012-l2-core-language-semantics.md) and
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
require inert quoted values, complete source joins, type facts and diagnostics.
The accounting-removal spec requires removing artificial traversal limits while
preserving native carrier, callback, metadata, cycle and source validation.

The source in `c7_type_checker_engine.gravity` restores the preserved iterative
quoted-data traversal, ordered collection reconstruction, unique source IDs,
independent quote diagnostics and complete source/type/constraint products from
PR502 head `6a4f87f013d0076b81aba9e0667650e70d28e635`. It removes quoted-data
work/depth/width limits, work-count fields and the two budget entrypoints.
`sh08-vn-project-input` and `sh08-is-cold-infer` no longer consume quote work
telemetry or pass quote allowances. Their separate projection/inference accounting
remains outside this increment and still needs removal by its actual owner.

Five exact preserved native-DATA helpers are included as dependencies. Quoted
entry, result and ledger checks use `sh08-vf-original-data` directly, rather than
main's metered `sh08-carrier-preflight`. The shared carrier preflight is unchanged.
Native headers, metadata, unsupported carriers, foreign Map comparators, invalid
List tails, zero ratio denominators, malformed extents and ancestor cycles remain
explicit rejection conditions. Source-ID uniqueness, parent links, syntax IDs,
ordered child/value equality, inert quote ownership and executable-payload
rejection remain in the source. These are implementation observations, not
executed semantic results. The five helpers do not port verifier comparison
arenas or accept general fresh-verifier behavior.

The restored `sh08-*` definitions retain their historical internal identities for
source/caller reconciliation. They are legacy naming debt, not new planning
identifiers imposed on a new API. No schema, producer or diagnostic ID is renamed.

## Tests and source evidence

[The census](quoted-data-census.edn) binds all three changed source/test files,
the historical head and current base. Seventeen restored definitions match their
complete historical forms, with only the recorded quoted-data preflight-call
adaptation. It records the two additional caller changes and the removed APIs.
All source forms are read with reader evaluation disabled; duplicate definitions
are rejected. No Gravity form is evaluated by this comparison.

The original authentication test now accepts 4,097 children and checks exact
shape size. The two restored preserved tests retain independent diagnostics,
a 192-level quoted value, exact leaf paths, malformed/duplicate/cyclic child IDs,
opaque unused DATA and zero rendering callbacks. Existing recursive value,
source-tamper, executable-payload and unsupported-numeric controls remain.
The recursive-function test retains its accepted Vector type and malformed-quote
rejection; its quote-payment and one-short refusal assertions are removed.

Observed local validation:

- `clojure -M /tmp/acc01-quoted-data-validation.clj`: complete source-form pins,
  seventeen definitions, three preserved test bodies, two caller identities,
  unique definitions and absence of retired quote APIs passed. The generator is
  retained locally; its digest is in the census. This is source evidence only.
- `clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  6 tests / 11 assertions, no failures or errors.
- Contextual clj-kondo with `tools/ci/kondo`: candidate zero errors / 73 warnings;
  corrected base-path comparison has exactly the same finding multiset. The
  initial base capture used incorrect namespace paths and had two path errors;
  that observation remains local and is not called a source failure or pass.
- Whitespace checks passed. No compiler, Bootstrap, authenticated pipeline,
  Effects, emitter, runtime, broker or held selector was launched.

The source-port helper first failed to read a delimiter. A subsequent malformed
intermediate insertion was caught during diff inspection and corrected before
source validation or execution. The first source-census run also rejected a
mistaken caller-name expectation. Corrected complete-form validation passed;
these earlier failures remain historical observations, not semantic runs.

## Finite acceptance still required

Independent source review is pending coordinator routing; no stopped PR569 review
request is replayed or routed through this draft. All genuine checks below are
UNRUN under the current execution hold:

1. The changed authentication, independent-diagnostics and deep/malformed SH08
   kernels, plus the existing recursive, structural and guard quoted-data kernels.
2. The recursive-function cold-quote selector with its preserved successful type
   and malformed-source rejection, without requiring a quote allowance.
3. Existing quoted-data C8 composition, authenticated source acceptance/rejection,
   and affected Vector projection/typed consumer composition, including current
   full facts and coherent tampering. Preserve actual spelling/provenance and
   renew paired composition where source handling changes.
4. Actual native-header controls for callbacks, metadata, cycles, List tails,
   Map comparator/backing data and malformed native extents; compare diagnostics
   and complete type/source products, not only status.
5. Current-main composition, applicable CI, independently accepted results and
   governed integration. No release, safety, self-hosting or seed-retirement
   authority follows from the source comparison or a green routine CI run.

Whole-compiler acceptance beyond old quote limits is not claimed: other carrier,
input, inference and projection accounting is still present. GIS3, verifier
arenas, nil/List joins, ordinary parameter choice, other descriptors and mixed
ledgers remain separate preserved obligations. PR502 stays open.
