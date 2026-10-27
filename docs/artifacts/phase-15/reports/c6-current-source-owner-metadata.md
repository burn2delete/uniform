# Current source owner metadata and strict Core receipts

The current C6 v2 source-policy receipt crosses two different data domains.
Reader and SH02 metadata can contain native maps with the default comparator;
Core data still rejects all sorted maps. The implementation keeps those rules
separate instead of converting whole plans or relaxing the Core carrier guard.
Historical source pins and default public arities remain unchanged.

## Observed failure and source diagnosis

The genuine constructor attempt on d258e5e8 exited naturally with
C6-VERIFY, missing fact `trusted-gravity-checked-core-carrier`, carrier
`current-reader-envelope-witness`, class `clojure.lang.PersistentTreeMap`.
The diagnostic did not retain a nested path. It is not evidence that a particular
nested field caused the runtime refusal, and no Core handoff or fresh verifier
phase followed.

Source inspection finds that the SH02 source owner creates its complete function
shapes with `sorted-map` and retains them in its source rule and actual envelope.
That is a source-grounded cause candidate. The original envelope remains subject
to its existing full descriptor reconstruction and equality checks.

## Owner metadata boundary

Parent product records are inspected through the existing comparator-free native
header inspector before keyword lookup. Full consumed owner metadata then passes
the existing native `default-only` comparator policy before equality, traversal,
or hashing. Foreign classes, custom comparators, metadata and cycles reject.
No authority object, supplied verification result or executable callback enters
this path.

The full witness commits the selected Reader binding, original descriptor and
envelope, retained C3 provenance, projected C2 authentication, source-unit record,
integrity metadata and C2 artifact identity. Its domain-separated content ID uses
the existing owner canonical digest with fixed printing options. This is a
semantic metadata digest, not a claim of concrete collection representation or
source admission.

## Closed exported projection

Explicit field vectors in the owning component define the exported binding,
source revision, source rule, and semantic-product-binding views. Nested function
shape tables and instruction summaries become committed content IDs. The entire
original owner witness is also hashed, so omitted or future fields cannot silently
change without changing that commitment. Template replay checks and diagnostics
are committed in full rather than copied as arbitrary nested carriers.

The actual five-field Reader result-identity view remains distinct from a full
Reader result. Actual development-unadmitted state comes from the original,
verified envelope; the projection does not manufacture a result status.
Expression binding metadata uses a separate digest domain and explicit scalar
fields plus full metadata and export-shape content IDs.

Both the Reader projection and the complete receipt after expression binding
must pass the unchanged strict Core carrier guard. The projection contract names
both policies explicitly. Original owner maps, descriptors and envelopes remain
unmodified.

## Controls and remaining acceptance

Compiler-free controls evaluate the existing pure native carrier, header and
digest definitions, plus the new projection. Four tests and 34 assertions pass at
256 MiB with Bootstrap absent and empty stderr. Realistic native default-sorted
metadata passes its owner policy, the entire projected receipt passes strict
Core validation, and the original sorted witness still fails that Core policy.
Custom comparators, foreign objects, metadata and cycles reject without observed
foreign callbacks. Changes to omitted fields alter the full metadata commitment;
caller print settings do not affect it. The descriptor verifier in these controls
is an explicit oracle, so they do not establish fresh source authentication.

The first control run exposed a shadowed printing macro and remains a separate
nonpass. The correction qualifies that macro. Independent source and harness
review subsequently accepted the changed source at
cb3701556e9082ae8387c18fa1f6396a9d284617, tree
b873b9aa3674622eada97bc0278256f9df584c22. The genuine public constructor and
separately invoked fresh public report both exited naturally with zero status,
actual 2 GiB, complete matching bookends and empty stderr. Independent
retained-result review accepted construction/transport and actual fresh report
verification, including observed development-unadmitted envelope state.

These results bind that exact selected-source tuple. Later C11 construction,
fresh replay and rejection evidence are recorded in the companion
`c11-current-core-source-policy.md` report, preserving their own consumer
identity. Full raw owner metadata was not retained for independent complete
metadata/policy-hash recomputation. Later main expression sources are not
silently admitted by this immutable version-2 contract. Changed consumer host
code, final composition execution and current-head integration review remain
separate obligations. No C12, backend, frozen migration, release or self-hosting
acceptance is claimed here.
