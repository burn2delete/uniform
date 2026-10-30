# Syntax reader view extraction

This increment moves the existing registered-literal occurrence registry,
native projection and projection authentication, current-reader identity view,
revision authentication and reader artifact view into `gravity.syntax`.
Bootstrap retains the original arities and supplies explicit callable Vars and
fixed configuration. The stage never imports or resolves bootstrap. These are
trusted seed composition APIs; injected operations and the retained internal
reader re-verification port do not grant caller proof authority.
An explicit invocation triggers fresh current-source reader re-verification.
Nil invocation/boundary cases retain their existing legacy compatibility
behavior; these views and association predicates are not standalone admission
certificates.
View construction expects C2 semantic validation to have run already; its
invocation check authenticates the retained source/envelope binding. Full C2
artifact validation remains in the source constructor before syntax creation.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern this boundary. C2 retains
literal decoding and reader authentication. C3 owns the native syntax view and
its exact joins to the decoded occurrence, extension invocation, source span,
reader revision, semantic binding and actual upstream provenance. Existing
diagnostic ids and logical producer metadata remain unchanged.

| Public stage API | Inputs and outputs |
| --- | --- |
| `registered-literal-fail!` | Source, occurrence, missing fields and facts -> existing C3-FACT-STALE diagnostic |
| `registered-literal-registry` | Source and reader products -> exact occurrence bindings and canonical native descriptors |
| `project-registered-literal-values` | Source, registry and value -> recursively projected native value |
| `registered-literal-projection-authentic?` | Reader view and syntax boundary -> exact projection/provenance association result |
| `current-reader-identity-view` | Source and reader products -> identity view with registered values projected when required |
| `current-reader-revision-authentic?` | Reader products, syntax boundary and invocation -> current revision/envelope association result |
| `reader-artifact-view` | Reader products and invocation -> existing C3 reader view, including exact registered-literal projection when present |

Every API also takes explicit operations last. Required operation keys and
return shapes are recorded in the namespace contract attached to public Vars.
The stage owns C3-FACT-STALE projection refusal through the existing C3 catalog;
upstream authentication still owns reader-source and envelope refusals. Actual
source spelling, raw literals, spans, occurrence ids, native value semantics,
metadata, source hashes, reader revision and deterministic identity survive.
No facts are invalidated by this representation-preserving extraction.

Before extraction, real paired fixtures with metadata, an instant, a UUID and a
repeated UUID emitted two syntax objects, three occurrence bindings and the
same artifact identity:
`sha256:0e62cacb7046ae54171f5a9188beb07794ae58053526a16c763b5cdaed1e92fc`.
Complete C3 artifacts and complete reader views compare equal before and after
extraction for both source spellings. Their artifact and projection identities
match across the pair, while physical path/extension and authenticated envelope
provenance remain distinct. Source metadata and repeated occurrences survive.

Direct tests reject changed decoded instants, malformed occurrence structure,
missing/duplicate literal records, changed raw spelling and literal spans,
missing/extra reader invocations, unbound instants/UUIDs, altered projection
identity, reader revision and upstream provenance. Source/envelope owner and
internal-token substitutions stop view construction before projection. A
coherent descriptor rewrite across every occurrence, with public projection and
artifact ids recomputed, still fails actual reader association. Full syntax
verification rejects that product with C3-SERIALIZE in its existing
serialization-first order. No production verifier ordering was changed to fit
the tests. Late Var interposition, private wrappers and interruption restoration
also survive.

Executed development checks:

| Check | Result |
| --- | --- |
| Full genuine C3 and reader-view comparison, both extensions | Equal across extraction |
| Combined syntax pipeline, C3 compatibility and fresh-C2 suites | 33 tests, 374 assertions; passed |
| C3 construction, verification and artifact identity helpers | 12 tests, 163 assertions; passed |
| Ownership, catalog and language-boundary suites | 50 tests, 1025 assertions; passed |
| Documentation, roadmap and governance validators | Passed; 240 documents, 19 indexes, 185 existing dispositions |
| Changed-file lint and whitespace checks | Passed; no new warnings or errors |
| Cold `gravity.syntax` loading | 25 public operations; bootstrap not loaded |

The existing CI syntax command includes these tests without a new runner or
registration path. The existing syntax ownership/component records remain
unchanged, as do all source/function pins and migration/reporting fingerprints.
Independent exact-source acceptance and final remote checks are tracked on
the pull request before integration.

This slice preserves the existing registered `inst`/`uuid` surface. It adds no
tag, reader extension authority, language feature or resource accounting.
Native carrier validation retains its existing no-quota semantics. Caller
operations are not an authentication certificate. Other SH-04 reader binding,
descriptor/envelope construction, rich-object/stream and graph adaptation,
transactional host indexes and earlier syntax helpers remain open. No
migration, release, self-hosting or seed-retirement claim is made.
