# Syntax reader view extraction

This increment moves the existing registered-literal occurrence registry,
native projection and projection authentication, current-reader identity view,
revision authentication and reader artifact view into `gravity.syntax`.
Bootstrap retains the original arities and supplies explicit callable Vars and
fixed configuration. The stage never imports or resolves bootstrap. These are
trusted seed composition APIs; injected operations and the retained internal
reader re-verification port do not grant caller proof authority.

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
Full product comparison, adversarial regression and independent review are in
progress on the draft PR.

This slice preserves the existing registered `inst`/`uuid` surface. It adds no
tag, reader extension authority, language feature or resource accounting.
Native carrier validation retains its existing no-quota semantics. Caller
operations are not an authentication certificate. Other SH-04 reader binding,
descriptor/envelope construction, rich-object/stream and graph adaptation,
transactional host indexes and earlier syntax helpers remain open. No
migration, release, self-hosting or seed-retirement claim is made.
