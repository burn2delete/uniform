# Parameter-form value carrier precondition

This source-only investigation starts from immutable parameter publication
`04ceaf61dc0ac7da6bc4e1909a4a744785276563`. It makes no production change,
new semantic acceptance or runtime claim. The existing
[raw-form residual](private-map-parameter-residual.edn) remains unchanged.

## Producer contract and concrete representations

[C2](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md)
requires authenticated flat reader products. Collection/wrapper values are
reconstructed from child and parent form IDs; every source/value/parent/child
join must resolve exactly. Native carrier and metadata rules remain mandatory.
[C3](../../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md)
requires immutable syntax identity, source/generated provenance and stable
serialization. C7 consumes resolved source artifacts; a supplied arbitrary map
is not an authenticated Reader result merely because its keys look correct.

The hosted compatibility producer
`bootstrap/clojure/src/gravity/bootstrap_impl/semantic_mid/010-reader-products-delimited.clj`,
`semantic-mid-reader-read-delimited`, builds the Vector branch with
`(vec values)` and stores that reconstructed object in the node's `:value`.
The authenticated SH03 projection in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00815-sh03-reader-normalization-deferred.clj`,
`sh03-reader-host-values!`, independently reconstructs the `:vector` branch
with `(vec child-values)` after child-link traversal. These files were read as
source; neither producer nor Bootstrap was loaded or invoked.

Thus a source record with `:kind :vector` and Keyword `:value` is outside the
authenticated producer contract. The concrete PMK precondition is an original
parameter Vector form whose value is that reconstructed Vector, whose child IDs
and source records refer to the same authenticated syntax, and whose provenance
survives expansion/resolution. A form-kind tag alone does not prove this.

## Actual consumer path and remaining gap

C7 `sh08-ft-source-join-context` takes an already indexed `:lookup-forms` map
or indexes its rows; `sh08-pmk-index-forms` likewise accepts a map directly.
Effects and emitter take form tables from their existing typed/source contexts.
These local adapters do not independently reconstruct the full Reader product.

The three `pmk-physical` functions validate form IDs, child vectors, kinds and
Symbol/Keyword payloads, but impose no Vector-kind value-class join. The three
`pe-census` functions establish private owner/parameter/call/source relations;
they do not inspect the parameter form's value class. `pmk-parameter` checks
Vector kind and `(vector? :value)`, but its aggregate predicate vector also
constructs an unconditional `(count :value)` equality. A Keyword therefore
raises the already recorded host-model count exception before the aggregate
can return a semantic refusal.

The source investigation does not prove that every actual entrance enforces
the authenticated-carrier precondition. C3's small `c3-form-record?` shape
predicate checks known kind, exact fields and raw String; it is not by itself
a kind/value authenticator. Nor do indexed maps, the physical census or a
constructed fixture establish complete source authentication. This remains
an explicit admission/tamper-containment gap, not a proof that malformed raw
forms are supported or universally unreachable.

No guard fix is introduced in this successor: the exact malformed value is not
an admitted authenticated-reader form, and no broader raw-form admission
contract has been established. If a caller is to admit independently supplied
raw form tables, its required rejection boundary must be assigned explicitly;
the existing PMK aggregate count site is a concrete candidate for a minimal
guard. That possible future correction must remain separate from the combined
04ce source candidate and receive its own affected review. Existing source
review and the recorded exception are not reclassified as runtime evidence.

No Reader/C3/C7/Effects/emitter engine, Bootstrap, provider/native/kernel/runtime,
selector, lease, probe or held retry ran. The execution limit stays CLOSED and
the original genuine-selector goal BLOCKED. This records source preconditions
and an unresolved admission gap; it supplies no full raw/native admission claim.
