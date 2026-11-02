# Sealed pure Reader producer boundary

Task: `reader/finite-remaining-work/extension-policy-closure` / FL-P01-T01.
Input: `e351cd65589037fe70dacfa3b5f35c0d441b316b`, tree
`400564964b945e03038b9135c7e36d4fdc5b36e7`.

This is a SOURCE decision and one consumer refusal correction. It does not
admit a sealed pure product. The original native goal remains blocked; the
E351 normal run and the original PR571 transaction remain unrun. No lifecycle
transition or task completion is inferred.

## Actual producer and verifier

The existing normal path has these distinct steps:

1. `sh03-reader-resolved-result!` executes `sh03-read-source-unit` on the
   selected current Gravity plan and original source bytes, then executes
   `sh03-verify-reader-result` on that same plan. The latter constructs a fresh
   complete raw result and compares it with the submitted raw result.
2. The Clojure adapter validates semantic/source closure and calls
   `sh03-reader-host-values!`. Only here does `dispatch-pure-tag` invoke the
   private Clojure `read-text` and freshly compare its result. Its
   `:implementation` contains the upstream Gravity Reader plan identities;
   those fields do not identify the Clojure handler implementation.
3. `sh03-reader-adapter-summary` hashes the established raw and adapted
   product projections. It does not include `:pure-extension-results`.
   `sh03-reader-sh02-descriptor` names the Gravity source revision, the
   arity-three Reader builder and arity-four raw verifier, and the lineage
   relation `:produced-from-gravity-reader`. The pure companion is attached
   afterward to the returned normal product.
4. The standard C2 constructor fixes `standard-reader-options`. Standard
   precomputed admission rejects the companion; the syntax artifact view
   also refuses its presence before its field whitelist can drop it.

These are source facts, not runtime observations. The raw invocation records
describe registered tags and occurrences, including form/payload ids and span;
they do not contain the private Clojure handler's actual returned seed.
`ae-source-revision-valid?` in
`bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity`
requires the exact source revision schema and `:source-language :gravity`.
Consequently, putting the later Clojure result into the current summary would
not establish its declared producer or make the raw verifier reconstruct it.
Merely adding a host source hash, purity flag or another envelope would not
repair that missing execution join.

## Minimum alternatives and recommendation

| Alternative | First source/API boundary | Compatibility and consequence |
| --- | --- | --- |
| Gravity-owned narrow pure variant (recommended) | Extend the existing `sh03-read-source-unit [source-unit source-bytes reader-policy]` policy selection with a distinct fixed pure registration. Its private Gravity handler executes on a source-bound seed during construction. Record the actual input, returned seed and invocation inside the existing raw extension-invocation product before digest construction. | Existing producer, verifier, plan/source identity, raw result schema slots and envelope can cover the real execution. The old standard and current unsealed host-pure options keep their behavior. This is a new Gravity implementation of the pure text behavior, not authentication of the historical Clojure call. |
| Preserve the Clojure handler as the authoritative producer | Version the owning producer-revision contract to describe the actual Clojure adapter/handler closure and its fresh reconstruction, then expose that producer through the existing normal Reader reverification port. | Requires an explicit C2/SH02 producer contract decision and dependent schema/consumer changes. Current SH02 source revisions cannot express it. No independent host seal or caller-provided source hash is an adequate substitute. |

The first required API change for the recommendation is **data in the existing
third Reader argument**, not a new positional callback or provider protocol:

```clojure
(sh03-read-source-unit source-unit source-bytes reader-policy)
;; proposed new opt-in selector, distinct from the retained host-pure option:
;; reader-policy :extension-policy => :gravity/sealed-pure-text-reader
;; implementation-owned registration => private Gravity read-text seed handler
```

The proposed selector is not implemented or admitted by this candidate. Its
name is a proposed serialized compatibility boundary, not an authentication
token. The private Gravity handler must return the actual seed from the actual
call. A copied expected result or quoted invocation declaration is insufficient.
The initial new variant should admit `text` only; mixed unsupported populations,
including effectful `resource`, must refuse before executing any handler.
The existing standard `inst`/`uuid` path is unchanged. Extending the new variant
to standard host literals needs its own producer/reconstruction join.

The producer change belongs in
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, at
`sh03-construct-parsed-source-unit`, before
`sh03-reader-extension-invocation-records` feeds `sh03-digest-requests`.
Use the existing extension invocation record slot, with a versioned opt-in
record shape containing policy selection, actual private handler identity,
original source/content and form/payload/span references, input seed, actual
returned seed and invocation. Bind the complete new record into the existing
extension-invocation digest, and thus the Reader result identity.
The entire current plan binding supplies implementation identity externally;
do not embed a circular hash of the containing Reader source in its own result.

`sh03-verify-reader-result` must independently reconstruct that new record from
all original bytes and the same selected policy. Then
`sh03-reader-adapt-evidence!` must project it without dropping the result,
`sh03-reader-host-values!` must derive the tagged value from that verified
returned seed, and `sh03-reader-adapter-summary` must include its complete
identity. The adapter must not execute the historical Clojure handler a second
time and attribute that call to Gravity. A narrow C2 entrypoint can select the
fixed new options while leaving all existing C2 arities/defaults unchanged.
The normal opaque Reader reverification port must validate the new variant
and its reconstructed product before a consumer can admit it.

No authentic producer for the existing Clojure companion exists in the inspected
contract. This candidate therefore leaves both existing presence guards intact.
The choice between migrating the pure implementation and changing the producer
contract is now explicit; it is not a dependency on all of Build, Bootstrap
migration, release, seed retirement or effectful replay.

## Concrete source correction

The direct `sh03-c3-precomputed-c2-verify!` port previously had no companion
presence refusal of its own. It could accept a C2 artifact bearing an unsealed
companion if its standard authority, binding and envelope checks passed;
the separate artifact-view guard did not protect callers of that port.
It now rejects any `:pure-extension-results` key, including nil/empty values,
with `C2-HASH` / `[:authenticated-pure-extension-results]` before source
selection or envelope verification. Absent-key standard behavior and all
existing checks remain unchanged. This closes an admission omission; it does
not authenticate or promote a companion.

## Verification and remaining acceptance

`gravity.reader.product-admission-test` reads named source definitions with
reader evaluation disabled. It evaluates only the new guard with a throwing
synthetic failure port and a continuation sentinel. Both source extensions,
nil/empty/nonempty values, absent-field continuation, retained standard checks,
existing guards and guard removal are covered. Application namespaces are not
loaded. These modeled checks establish refusal placement, not normal parsing,
authentic envelope acceptance, handler execution or fresh reconstruction.

[Paired fixture definitions](sealed-pure-producer-definitions.edn) specify the
future positive and tamper/refusal obligations against genuine constructors
and the existing current-source invocation. They remain unrun and do not
create a provider, seal, process, lease or execution receipt. Current checked
companions remain unsealed. The native runtime/resource hold remains with its
existing owner and was not probed or modified.

Independent acceptance, current-main composition, CI and publication are not
performed. E351 and its consumed independent review remain preserved. No
coordinator ledger, resource helper, admission root or historical receipt is
edited.

Observed validation on this successor:

```sh
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.reader.product-admission-test) (quote gravity.self-hosting.sh01-language-boundary-test)) (let [r (clojure.test/run-tests (quote gravity.reader.product-admission-test) (quote gravity.self-hosting.sh01-language-boundary-test))] (println (pr-str {:evidence-class :source-modeled :bootstrap-loaded? (boolean (find-ns (quote gravity.bootstrap))) :syntax-loaded? (boolean (find-ns (quote gravity.syntax))) :dispatch-loaded? (boolean (find-ns (quote gravity.reader.dispatch)))})) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
git diff --check
```

Result: 10 tests / 55 assertions, zero failures/errors, exit 0. Of those,
product admission/definition checks account for 4 tests / 44 assertions;
the language-boundary gate accounts for 6 / 11. All three printed namespace
load indicators were false. The earlier focused guard run passed 3 tests /
26 assertions before the paired-definition check was added. No genuine normal
Reader/Bootstrap/handler/provider/runtime invocation was performed.

New implementation naming: `gravity.reader.product-admission-test` describes
the behavior under test. Existing numbered component filename and private
entrypoint are compatibility-bearing legacy names, conserved rather than
renamed. The production delta is solely the direct precomputed C3 refusal.
