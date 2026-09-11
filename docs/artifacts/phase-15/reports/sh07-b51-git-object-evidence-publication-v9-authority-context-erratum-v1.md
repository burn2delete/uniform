# SH-07 B51 Git-Object Publication V9 Authority-Context Erratum V1

Status: draft architecture correction; report only; no implementation authority

## Purpose

This report corrects two remaining authority contradictions in the accepted
SH-07 B51 Git-object evidence-publication V9 architecture and its four
integrated errata.

First, the E3 checker handoff carries the complete E2 resolution-plan
authority but drops the exact E2 SH06 authority carrier, including its seven
complete C5 external-binding authorities. The selected checker therefore
cannot reconstruct the exact `LINK-AUTHORITY-ID-E2` or exact final E2 SH06
authority-carrier ID from its declared 20-key input. This report adds one
field whose value is the complete E2 carrier. Its
`:external-binding-authorities` member is the exact seven-element authority
vector in module-ordinal order. The correction does not change the seven-row
`MODULE-TRANSACTIONS` schema, the literal V9 19-key certificate, or the literal
V9 13-key certificate-input map.

Second, the V9 Root1, Root6, and 00947 verifier equations compare L2 owner and
edge arguments with values that, at a compact verification boundary, can only
have come from the same caller-controlled witness or carrier. Rehashing those
projections proves internal consistency, not their origin in the authenticated
C/T-bound Q, resolved-core, M0, M1, and SH06 products. This report introduces
one exact, acyclic, verifier-internal `AUTHORITY-CONTEXT`. Authoritative
validation receives that context lexically and independently rebuilds
`OWNER-REGISTRY`, `EXPECTED`, and `BAD-ACTUAL` before comparing them with a
certificate or linked carrier. The context is never a certificate field, a
witness field, an E9 carrier field, a payload, or an identity preimage.

The correction is deliberately narrow. All unchanged V9/E1/E2/E3/E4 schemas,
identities, bounds, Git observations, process separation, publication
topology, rejection rules, and authority ceilings remain normative. This
report specifies architecture only and makes no implementation or evidence
claim.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-git-object-evidence-publication-v9-authority-context-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v1.md",
  "status": "draft",
  "base_commit": "e79a01b0af146ad277f505e0cd8b7c4b4c757c35",
  "dependencies": [
    {
      "id": "sh07-b51-git-object-evidence-publication-architecture-v9",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-git-object-evidence-publication-v9-pre-evidence-erratum-v1",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-git-object-evidence-publication-v9-sh06-external-binding-erratum-v1",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-git-object-evidence-publication-v9-fixture-cycle-erratum-v1",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-git-object-evidence-publication-v9-root-verifier-e3-erratum-v1",
      "required_state": "integrated"
    }
  ],
  "historical_references": [],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```

## Accepted tuple and E5 supersession

This report is E5. It depends on the integrated V9 architecture and E1 through
E4 errata listed in the authority block. It supersedes only the following
accepted statements:

1. E3's exact checker input and receipted builder return are replaced by the
   exact E5 key sets below.
2. Any V9/E4 statement that a pure one-argument Root1, Root6, or 00947 data
   validator is authority-bearing is narrowed to shape-only validation. The
   context-bearing 00947 boundary and the separately classified one-shot
   runtime receipt consumer defined below provide authority.
3. The caller-supplied `OWNER`, `EXPECTED-AUTHORITY`, and related ambient
   equality checks in V9's L2 verification are replaced by independent
   reconstruction from `AUTHORITY-CONTEXT`.
4. E4's exact 42-path ownership allocation is retained. The correction changes
   values and identities in already-owned paths but adds no code or test path.

All other accepted text remains in force. In particular, the certificate's
architecture report digest remains the raw SHA-256 of the integrated V9 report:

```clojure
(def V9-ARCHITECTURE-REPORT-RAW-SHA256
  "sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd")
```

It is not replaced by the digest of this erratum.

## Complete C5 authority handoff

E2 defines `C5-AUTHORITIES` as the exact vector
`[C5-AUTHORITY_0 ... C5-AUTHORITY_6]` in module-ordinal order. Every member is
the complete E2 map:

```clojure
{:schema :gravity/sh07-b51-c5-external-binding-authority-v1
 :schema-version 1
 :content {:commit C :tree T}
 :module-ordinal i
 :source-path PATH_i
 :sh05-artifact-id SH05-ID_i
 :base-request-id BASE-REQUEST-ID_i
 :imports IMPORTS_i
 :import-bindings IMPORT-BINDINGS_i
 :dependency-edges DEPENDENCY-EDGES_i
 :preimage C5-AUTHORITY-PREIMAGE
 :self-id-omits [[:authority-id]]
 :authority-id SHA}
```

`BASE-REQUEST-ID_i` retains the E2 tagged equation
`H([:gravity/sh07-b51-sh06-base-request-v1 BASE_i])`. The complete preimage
retains its exact E2 closed schema, C/T commit proof, seven source descriptors,
seven source-object inclusion proofs and raw blob bytes, complete M0 vector,
and complete M1 manifest. A vector of seven IDs, seven projections, seven
booleans, or seven maps missing any preimage member is not
`C5-AUTHORITIES`.

The E3 construction already has the lexical C5 preimage, seven complete C5
authorities, and seven strict R/Q/RC transactions while its governed source
session is live. It does not yet provide an authoritative complete E2 carrier
at that boundary: the current complete-carrier helper is dead/post-finalization
and reacquires Git objects, so it cannot supply this handoff. E5 requires
00945 to assemble the exact E2 carrier before the sole finalization using only
cached same-session descriptors, event products, object-proof entries, C5
preimage/authorities, M0/M1, bundle core, and module transactions. That assembly
performs no acquisition and retains the complete carrier for return after the
session finalizes. The object-mode return becomes
`RECEIPTED-BUNDLE-CORE-PRODUCTS-E5`, with exactly these 11 keys:

```clojure
#{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
  :module-transactions :sh06-resolution-plan-authority
  :sh06-authority-carrier :source-semantic-carrier
  :source-read-receipt}
```

Its added field is exactly:

```clojure
:sh06-authority-carrier SH06-AUTHORITY-CARRIER-E2
```

The selected checker input becomes `CHECKER-SEMANTIC-INPUT-E5`, with exactly
these 21 keys:

```clojure
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :predecessor-executable-descriptor-id :module-source-identities
  :module-source-byte-counts :emitter-source-id :emitter-semantic-id
  :m0s :m1 :m2s :bundle-core :source-forms :module-transactions
  :sh06-resolution-plan-authority :sh06-authority-carrier
  :source-semantic-carrier :semantic-algorithm-source-descriptors
  :semantic-algorithm-source-root :checker :provenance}
```

The added checker field is the same complete carrier returned by the same
checker-local builder. The child first requires its exact E2 16-key final
shape, including the 15-key base and only `:carrier-id` added. It requires
`carrier[:resolution-plan-authority]` to equal the sibling
`:sh06-resolution-plan-authority` byte-for-byte. It then selects
`C5-AUTHORITIES = carrier[:external-binding-authorities]`, requires exactly
seven members whose ordinals are `[0 1 2 3 4 5 6]`, requires their paths to
match the seven module descriptors and their IDs to be unique, and revalidates
every complete authority against the child's C/T, raw object proofs, SH05
artifact, base request, authenticated M0/M1, and plan authority. It then
reconstructs the exact E2 link and carrier identities:

```clojure
LINK-AUTHORITY-ID-E2 =
H([:gravity/sh07-b51-sh06-link-authority-id-e2-v1
   {:resolution-artifact-ids SH06-RESOLUTION-ARTIFACT-IDS
    :semantic-projection-ids SH06-SEMANTIC-PROJECTION-IDS
    :event-product-ids (mapv :product-id (subvec SOURCE-EVENTS-E2 17 24))
    :manifest-id AUTHENTIC-M1[:manifest-id]
    :resolution-plan-authority RESOLUTION-PLAN-AUTHORITY
    :external-binding-authorities C5-AUTHORITIES}])

SH06-AUTHORITY-CARRIER-ID-E2 =
H([:gravity/sh07-b51-sh06-authority-carrier-id-e2-v1
   SH06-AUTHORITY-CARRIER-BASE-E2])
```

`SH06-AUTHORITY-CARRIER-BASE-E2` retains its exact 15-key E2 map:

```clojure
#{:content :source-descriptors :bundle-core-id
  :resolution-artifact-paths :resolution-artifacts
  :resolution-verifications :semantic-projection-paths
  :semantic-projection-ids :module-source-descriptors :link-manifest
  :link-authority-event-product-ids :link-authority-id
  :expected-edge-census :resolution-plan-authority
  :external-binding-authorities}
```

It includes the complete plan authority and complete `C5-AUTHORITIES`; the
final carrier adds only `:carrier-id`. The complete field must also join its source
descriptors, M0, M1, resolution artifacts, contextual verification reports,
Q semantic projection IDs, event product IDs, expected edge census, and
bundle-core ID to the sibling checker inputs and seven
`MODULE-TRANSACTIONS` rows. A digest-only reconstruction, a detached C5
vector, or an E2 carrier built without byte-equal complete C5 members rejects.

There is no second new top-level `:external-binding-authorities` field in
either E5 map. That ordered vector occurs exactly once inside the complete
`:sh06-authority-carrier`; adding a detached sibling copy rejects the 11-key or
21-key schema.

This handoff performs no new source acquisition, source decode, source event,
session finalization, PRE Git operation, or G9 operation. It carries one
already-constructed field across two existing same-process maps.

## Schemas that do not change

`MODULE-TRANSACTIONS` remains the exact seven-row module-ordinal vector. Every
row still has exactly these 11 keys:

```clojure
#{:module-ordinal :source-descriptor :resolution-artifact
  :c2-reader-artifact :authenticated-core-request :resolved-digests
  :module-core :template-carrier :generated-digest-carrier
  :resolved-core-carrier :module-digest-requests}
```

Neither `:sh06-authority-carrier` nor `:authority-context` may be inserted
there. The literal V9 certificate remains exactly 19 top-level keys:

```clojure
#{:artifact :domain :schema-version :status :role
  :architecture-report-sha256 :claim-id :claim :assumptions :checker
  :inputs :result :semantic-witness :provenance :raw-source-transcript
  :m2-transcript :profile-bounds :evidence-root :certificate-id}
```

Its `:inputs` map remains exactly 13 keys:

```clojure
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :lowering-product-id :predecessor-executable-descriptor-id
  :module-source-identities :module-source-byte-counts
  :emitter-source-id :emitter-semantic-id :m0-ids :m1-id
  :m2-ids :bundle-core-id}
```

The SH06 carrier and `AUTHORITY-CONTEXT` are pre-certificate verification
authority. Adding either to the certificate, certificate inputs, semantic
witness, checker output, E9 linked carrier, or E9 identity receipt rejects the
corresponding closed schema. No new certificate version is introduced.

## Exact verifier-internal authority context

After the E5 builder return has passed all same-session receipt and object
joins, A, B, or M constructs this exact seven-key plain map:

```clojure
AUTHORITY-CONTEXT =
{:content {:commit C :tree T}
 :module-transactions MODULE-TRANSACTIONS
 :m0s AUTHENTIC-M0
 :m1 AUTHENTIC-M1
 :bundle-core BUNDLE-CORE
 :sh06-resolution-plan-authority RESOLUTION-PLAN-AUTHORITY
 :sh06-authority-carrier SH06-AUTHORITY-CARRIER-E2}
```

`AUTHORITY-CONTEXT` is an in-memory verifier argument, not an artifact. It has
no schema tag, ID, self-ID omission, serialization, canonical bytes, payload
descriptor, evidence path, source event, certificate field, witness field,
E9 coordinate, runtime token, callback, function, Var, cache key, or ambient
binding. Adding or omitting a key rejects. Supplying nil rejects; nil never
selects a shape-only fallback.

The context validator independently requires all of the following before any
owner or edge projection is used:

- `:content` is exactly the enclosing authenticated C/T pair;
- `:module-transactions` is the exact seven-row E2 vector in ordinal order;
- `:m0s` and `:m1` are the complete, closed, independently source-derived
  values, not their IDs or certificate projections;
- the seven transaction source descriptors and all M0/M1 source IDs join
  byte-for-byte to the C/T source session;
- `:bundle-core` is the complete checker-local V9 bundle core and its ID joins
  the sibling builder value and every transaction-derived plan/root input;
- `:sh06-resolution-plan-authority` fully revalidates its C/T commit and tree
  inclusion proofs, governed `resolution.gravity` descriptor, plan binding,
  and authority ID;
- the equal plan-authority value occurs inside
  `:sh06-authority-carrier`; and
- the complete SH06 carrier passes every E2 base-key, carrier-ID, link-ID,
  C5, artifact, contextual verification, Q projection, M0/M1, expected-edge,
  source-descriptor, event-product, and bundle-core join.

Equality between two caller fields is never enough. For example, changing the
sibling plan authority and the carrier's plan authority together still
rejects against the authenticated C/T source event. Likewise, changing M0,
M1, the C5 preimages, the seven transactions, and all their descendant IDs
together still rejects against the raw commit, tree, and module blob proofs.

For transaction ordinal `i`, the context validator selects exactly
`C5-AUTHORITY_i` from the carrier and constructs the exact E2 two-key
`SH06-CONTEXT_i`:

```clojure
{:external-binding-authority C5-AUTHORITY_i
 :resolution-plan-authority RESOLUTION-PLAN-AUTHORITY}
```

It calls the strict context-bearing
`sh06-resolution-artifact-verification(R_i, SH06-CONTEXT_i)` and
`sh07-core-authenticated-request(R_i, SH06-CONTEXT_i)`. The first must accept
the stored complete `R_i`; the second must equal the stored complete `Q_i`
byte-for-byte. It then reruns every unchanged V9 `MODULE-TRANSACTIONS` adapter
equation through the complete resolved-core carrier `RC_i`, including node,
function, definition, evaluation-order, root, and self-ID checks. The ordinary
one-argument SH06 verifier and SH07 request projection are forbidden on this
path. A stored `R_i`, `Q_i`, or `RC_i` is not authentic merely because its own
ID recomputes.

## Acyclic construction order

The required dependency order is:

```text
C/T raw object proofs and governed source session
  -> source descriptors, M0, M1, plan authority, and seven C5 authorities
  -> seven strict R/Q/RC module transactions
  -> exact E2 SH06 authority carrier
  -> AUTHORITY-CONTEXT
  -> independently rebuilt owner/root/edge/rejection values
  -> lowering product, operations, receipts, stage witnesses, and WITNESS
  -> role-specific certificate and checker output
  -> linked-carrier candidate and E9 payloads
```

For M or an independent object reviewer, the already-constructed certificate
and linked carrier are values to validate after a separate context has been
rebuilt from C/T; they are never inputs to context construction. The context
does not contain a lowering-product ID, operation, fuel receipt, stage witness,
certificate, checker output, linked carrier, E9 blob, publication commit, or
validation result. No value to the right of `AUTHORITY-CONTEXT` points back
into it. This is the required acyclic boundary.

## Independent L2 authority reconstruction

Authoritative verification does not accept `OWNER-REGISTRY`, `EXPECTED`, or
`BAD-ACTUAL` as authority inputs. Define:

```clojure
AUTHENTIC-QS =
  (mapv :authenticated-core-request
        AUTHORITY-CONTEXT[:module-transactions])

AUTHENTIC-RCS =
  (mapv :resolved-core-carrier
        AUTHORITY-CONTEXT[:module-transactions])

AUTHENTIC-M0 = AUTHORITY-CONTEXT[:m0s]
AUTHENTIC-M1 = AUTHORITY-CONTEXT[:m1]
```

Only after the complete context and each fresh strict SH06 replay pass may the
verifier derive the following values in private scratch storage.

### Owner registry and public-root selection

`OWNER-REGISTRY-E5` is rebuilt from `AUTHENTIC-QS` and `AUTHENTIC-RCS` in
module order and executable top-level Q form order. For each top-level defn,
the verifier independently performs the V9 unique macro-trace, introduced-fn,
resolved function-record, definition-record, body-child, `TOPIDX`, and `EIDX`
joins. Its row base remains exactly:

```clojure
{:ordinal GLOBAL-DENSE-ORDINAL
 :source-id Q[:lineage :sh06-semantic-projection-id]
 :module-ordinal i
 :top-level-form-ordinal t
 :declared-name FR[:definition-name]
 :arity FR[:fixed-arity]
 :defn-id FR[:definition-core-node-id]
 :sh05-expanded-def-id X[:output-def-syntax-id]
 :sh05-expanded-fn-id X[:introduced-fn-syntax-id]
 :declared-body-root-id FR[:body-core-node-id]}
```

The row adds only the unchanged
`MID(:l2-owner-registry, REGISTRY-BASE)` registry ID. The verifier obtains the
eight root specifications and bundle public-root projection from the
authenticated bundle core and fixed accepted root plan, then independently
rebuilds candidate vectors, unique selections, rejected `[]`, and the owner
registry root. It does not begin with
`WITNESS[:l2][:owner-selection][:registry]` or any caller-provided root
projection. After reconstruction, every complete witness owner-selection
member must equal the rebuilt value byte-for-byte.

### Pairing and expected edge authority

From the independently rebuilt owner registry, complete Q external
occurrences, complete M0 provider definitions, and complete M1 edges, the
verifier repeats the unchanged V9 normalization and unique joins. It requires
exactly 1,480 Q/M1 pairs, dense M1 ordinals 0 through 1479, unique normalized
keys, and byte-equal M1 edge order. It constructs every `EDGE-BASE` and then:

```clojure
EXPECTED-E5 =
  (mapv #(assoc % :edge-id (MID :l2-expected-edge-census %))
        EDGE-BASES-IN-M1-ORDINAL-ORDER)

EXPECTED-ROOT-E5 =
  (MR :l2-expected-edge-census EXPECTED-E5)
```

The verifier requires exactly 1,480 dense, unique rows and requires
`(mapv :m1-edge EXPECTED-E5)` to equal both authenticated M1 edges and the
complete E2 carrier's raw expected edge census. It derives accepted edge IDs,
the authority receipt, and the edge-authority carrier from these rebuilt rows.
It does not accept the witness census, receipt, accepted IDs, or edge root as
an expected value.

### Closed rejected edge

`SEED-E5` is the first rebuilt edge base. `BAD-BASE-E5` is derived internally
by the unchanged V9 swap of its four consumer/provider endpoint pairs, leaving
every other field unchanged. No caller supplies the seed or candidate. The
verifier recomputes `BAD-EDGE-ID-E5`, proves the candidate is absent from the
rebuilt expected census, and produces exactly:

```clojure
BAD-ACTUAL-E5 =
{:status :rejected
 :code "SH07-B51-EDGE-AUTHORITY"
 :rule "L2-UNKNOWN-CORE-FORM"
 :reason :unauthenticated-b51-external-link
 :input-id (:edge-id (first EXPECTED-E5))
 :mutated-id BAD-EDGE-ID-E5
 :expected (CONSUMER-ENDPOINT SEED-E5)
 :actual (CONSUMER-ENDPOINT BAD-BASE-E5)
 :committed-row nil
 :output nil}
```

Only after this receipt-free rejection exists does the verifier perform the
unchanged unique L2 operation/fuel-receipt join and rebuild the diagnostic,
negative fixture, L2 owner selection, edge authority, stage roots, and L2
witness. The complete rebuilt values must equal the certificate witness. The
old six-argument
`VERIFY-UNAUTHORIZED-CROSS-FRAGMENT(QS,M0,M1,OWNER,EXPECTED,CANDIDATE)` is not
an authoritative compact-verifier API. Its pure equation may remain a private
shape helper, but production authority is the zero-projection-input E5 rebuild
from `AUTHORITY-CONTEXT`.

Every unchanged E4 witness rule remains mandatory: the exact witness key set,
split witness and certificate bounds, operation schedule and operation IDs,
seven fuel receipts, seven stage witnesses, all member/root equations, the
complete lowering-product join, and the final witness and certificate IDs.
E5 changes the origin of the owner/edge expected values, not their schemas or
identities.

## Validator classes and exact production boundary

E5 distinguishes three classes that must not be conflated.

### Shape-only pure data validators

The existing one-argument pure predicates, including
`sh07-b51-r1-certificate-a-valid?`,
`sh07-b51-r6-c30-checker-b-certificate?`,
`p15-s23-sh07-b51-compile-census-certificate-v1-valid?`,
`p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?`, and
`p15-s23-sh07-b51-linked-carrier-v4-valid?`, validate exact closed schemas,
profiles, internal canonical identities, witness structure, and internal joins.
Their names need not change, but one argument supplies no external source
authority. A true result means only `:shape-and-internal-identity-valid`; it
cannot by itself authorize E9 construction, freeze pins, mint a runtime
receipt, publish evidence, satisfy independent review, or set an acceptance
state.

The Root1 and Root6 compact predicates may continue to call those role-local
shape validators. Their duplicated implementation is still useful negative
evidence for the exact 19/13-key contract and preserves A/B code independence.
It is not a second source of M0/M1/SH06 truth.

### Context-bearing authoritative 00947 validators

00947 owns the shared authoritative boundary. It adds strict context-bearing
arities equivalent to:

```clojure
p15-s23-sh07-b51-compile-census-certificate-v1-valid?
  (CERTIFICATE, AUTHORITY-CONTEXT)

p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
  (CERTIFICATES, AUTHORITY-CONTEXT)

p15-s23-sh07-b51-linked-carrier-v4-valid?
  (CARRIER, AUTHORITY-CONTEXT)

p15-s23-sh07-b51-build-linked-carrier-v4-candidate
  (PRODUCTS, CERTIFICATES, AUTHORITY-CONTEXT)
```

The exact language spelling may use ordinary arity syntax, but the semantic
contract is fixed: each authoritative form first passes the corresponding
one-argument shape checks, then validates the exact context, independently
rebuilds the L2 authority values separately for each certificate, compares the
complete witness and product joins, and rejects nil. The candidate builder
also requires its `PRODUCTS` M0, M1, bundle core, and execution-bundle
projections to equal the context values before constructing the carrier.

No authoritative call may invoke a one-argument form as a fallback after a
missing or failing context. Public one-argument wrappers such as an admission
or frozen-authority lookup are authoritative only if they privately construct
an authenticated context and call these context-bearing forms before returning
success. Merely loading a pinned carrier and rerunning its self-hashes is
insufficient.

### One-shot admitted runtime receipt

The compiler-internal Gravity operation
`sh07-b51-admitted-executable-carrier-validation?` is not a pure one-argument
data validator. It is a live, identity-bound capability consumer. Existing
00945a receipt minting and 00945c receipt consumption require the exact active
runtime context, carrier object, raw wrapper, selected root, selected plan,
argument vector, invocation, owner, scope, thread, certificate IDs, and
semantic-root value to be identical to those admitted by 00947. The receipt
exists only for Root1 or Root6, is private, begins in state `:fresh`, and one
successful compiler-internal occurrence atomically changes it to
`:consumed`. Reuse, copying, direct Gravity invocation, a different thread,
root, plan, carrier, raw wrapper, argument vector, or instruction occurrence
rejects. Cleanup retires and invalidates any unconsumed receipt.

00947 must run the context-bearing certificate-pair and linked-carrier
validation before it calls the existing admitted-runtime registration path
that permits 00945a to mint this receipt. The exact carrier object that passed
validation is the object bound to the receipt. Thus Root1 and Root6 can consume
the prior authoritative 00947 decision without serializing
`AUTHORITY-CONTEXT` into Gravity data or adding a context parameter to a
public Gravity root. A forged carrier that merely passes the Root1/Root6 shape
predicates cannot acquire the private receipt.

This classification is why
`bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` remains source-only
and read-only. Its apparent `carrier-authoritative?` name does not make its
pure certificate projections authoritative; its actual production authority
comes from the compiler-internal receipt call already present in the Root6
request proof. No E5 edit to that file is required.

## Checker A and checker B independence

The selected checker processes remain independent exactly as V9/E3 require.
After its own one governed C/T source session finalizes, A constructs
`AUTHORITY-CONTEXT-A` solely from A's exact E5 builder return and immutable
PRE/classpath checks. B later constructs `AUTHORITY-CONTEXT-B` solely from B's
corresponding values. Neither context, builder return, witness, certificate,
checker output, namespace, Var, process handle, or cached derivation crosses
between them.

Each checker independently replays all seven strict SH06 transactions and
independently rebuilds its owner registry, 1,480-edge pairing and expected
census, closed rejection, lowering product, seven operations, 34 measurements
and receipts, seven stage witnesses, enclosing witness, and role-specific
certificate. Equal C/T inputs require byte-equal common semantic values, but
that equality is an outcome, not an input. A cannot call or load B's selected
checker closure, and B cannot call or load A's.

The terms Root1 and Root6 in the receipt boundary refer to the two compiled
Gravity public roots. They do not permit checker A and checker B to validate
one another. The selected Clojure checker identities, process closure rules,
source-first versus M2-first role distinction, ordered execution, and opposite
checker exclusions remain unchanged.

## M validation order

M still starts no governed semantic source session until A has exited and its
closed response has passed response/process validation, then B has exited and
passed the same validation. E5 makes the validation layers explicit:

1. At coordinator event ordinal 4, M completely validates A's bounded
   transport, exact response schema, role, process/TCB/source receipt,
   certificate and witness shape, internal identities, and declared joins.
   This permits B construction but grants no semantic authority.
2. At ordinal 8, M performs the corresponding complete shape/transport
   validation for B. This permits M's own source session but grants no semantic
   authority.
3. At ordinals 9 and 10, M starts and finalizes its existing one independent
   governed C/T source session. Its exact E5 builder return produces
   `AUTHORITY-CONTEXT-M` without using either child response.
4. After ordinal 10 and before candidate construction, staging, or an accepted
   M response, M calls the context-bearing 00947 validator independently for A
   and then independently for B. It resets all certificate-derived scratch
   state between calls; no owner, edge, witness, or acceptance result from A is
   used as B's expected value. It then calls the context-bearing pair validator
   and three-argument linked-carrier candidate builder using that same
   independently authenticated common context.

The V9 phrases "A response fully validated" and "B response fully validated"
at ordinals 4 and 8 are therefore narrowed only with respect to E5 semantic
authority: they mean complete bounded response/process and internal-shape
validation. The final semantic authority decision is deliberately withheld
until M owns `AUTHORITY-CONTEXT-M`. The literal 11 coordinator events, their
role/operation pairs, event IDs, event root, A-before-B ordering, and
B-before-M-session ordering do not change. The post-ordinal-10 calls are pure
verification work, not a new process event or source acquisition.

M's context is not inserted into its response, coordinator receipt, payload,
certificate pair, or linked carrier. An accepted M response is produced only
if all context-bearing calls returned true and the exact already-normative
response products were constructed. A child self-validation flag, A/B byte
agreement, or the two shape validations without M's later reconstruction is
insufficient.

## L production orchestration

L remains a narrow host launcher and publication orchestrator. It does not
construct `AUTHORITY-CONTEXT`, load 00945/00947, load a checker or
materializer, decode Gravity source, replay Q/RC, or call any one-argument
certificate/carrier predicate as semantic authority. Its production duties
remain:

- validate the source/publication or existing-object coordinates;
- materialize and authenticate the exact C-derived classpath;
- launch exactly one fresh M process with the unchanged closed argv and
  environment;
- drain and validate M's bounded response and process receipt;
- validate the six staged payload descriptors and already-normative contract
  discharge over values M produced only after its context checks;
- in `:new`, perform the exact seven-file publication transaction; and
- in `:existing`, compare the fresh semantic payload bytes with immutable E/TE
  objects and recompute the normalized receipt as already required.

L must fail closed if M reports failure, exits nonzero, omits a payload, or
cannot prove its existing source/process/session ordering. L never upgrades a
shape-only predicate result into semantic authority. This preserves the V9
trust split: M owns semantic reconstruction and validation; L owns bounded
process and publication orchestration.

## Existing-object and independent replay

Authoritative `:existing` mode is not a shortcut around E5. L first reads the
seven expected evidence blobs only from the supplied immutable E/TE coordinate,
then launches fresh M, A, and B exactly as in `:new`. A and B each construct
their own contexts. M later constructs `AUTHORITY-CONTEXT-M`, runs both
context-bearing certificate validations and linked-carrier validation, and
only then may L compare the newly produced first six payload byte vectors with
their E blobs and perform the accepted seventh-receipt normalization. E, TE,
publication mode, and prior payloads do not enter any A/B/M context.

An independent implementation reviewer starting only from accepted Git
objects must likewise begin at C/T. The reviewer strictly reconstructs the
governed source session, complete E5 builder products, seven module
transactions, exact E2 SH06 carrier, and a fresh verifier-internal
`AUTHORITY-CONTEXT-REPLAY`. It then decodes the certificate pair and linked
carrier from E and invokes the context-bearing 00947 validators. Recomputing
only certificate, witness, or E9 carrier IDs is shape review and cannot satisfy
the authoritative replay obligation.

The zero-child `:object-replay-only` mode remains diagnostic and
nonauthoritative. It may report shape and internal-identity results, but it
cannot satisfy authoritative reload, runtime admission, lifecycle join, G9,
publication, implementation acceptance, or this erratum's context
reconstruction requirement.

## Runtime admission and receipt lifecycle

The eventual frozen runtime carrier and pins do not serialize
`AUTHORITY-CONTEXT`. Before any public-root invocation is admitted, 00947 must
use the existing governed object-backed construction path for that exact C/T
to rebuild a private context, call its context-bearing linked-carrier and pair
validators, require the supplied raw carrier to be the exact validated carrier,
and select the exact public root and plan. Only then may it register the live
runtime context and permit 00945a to mint the one-shot receipt consumed by
00945c.

Compatibility admission that returns an unadmitted runtime value has no
receipt or token authority. A direct call into Root1 or Root6, a copied runtime
map, a copied receipt-shaped map, a shape-valid carrier, or a carrier whose
private context has expired cannot enter an authoritative root. The existing
`finally` retirement, identity registry removal, reachability fence, receipt
invalidation, and same-thread restrictions remain mandatory.

This runtime reconstruction uses the existing V9 authenticated runtime object
session and does not authorize a worktree read, mutable cache, environment
lookup, hidden default, or additional publication object. The implementation
must rederive changed identities under the eventual C/T; this draft does not
claim that the current provisional runtime already meets E5.

## Exact ownership and counts

No new implementation or test path is necessary. `OWNED-C-PATHS-E5` is
byte-for-byte `OWNED-C-PATHS-E4`, the following exact 42-string
unsigned-UTF-8-sorted vector:

```clojure
["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00581-p15-s23-stage2-compiler-artifact-binding.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00582-p15-s23-stage2-construct-emitted-core-plan.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00873-sh06-resolution-dependency-edges.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00879-sh06-resolution-artifact-verification-bounded.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00880-sh06-resolution-artifact-verification.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00881-sh06-resolution-finalize-candidate.clj"
 "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00890-sh07-b13-module-assembly-manifest.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
 "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn"
 "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_independent_census_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_linked_carrier_host_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_census_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_compact_verifier_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_root6_compact_verifier_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_checker_child_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_tier0_materializer_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_git_evidence_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_plan_linker_governed_read_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_tcb_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_contract_discharge_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_eligibility_object_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_existing_object_replay_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_process_isolation_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_sh06_external_binding_seam_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"]
```

`SOURCE-ONLY-PATHS-E5` is byte-for-byte the exact eight-path
`SOURCE-ONLY-PATHS-E4`:

```clojure
["bootstrap/gravity/p15_s23/compiler.gravity"
 "bootstrap/gravity/p15_s23/emitter.gravity"
 "bootstrap/gravity/src/gravity/resolution.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"]
```

`READ-ONLY-PATHS-E5` is byte-for-byte `READ-ONLY-PATHS-E4`.
`I9-LIFECYCLE-PATHS-E5` is the same 42 paths followed by the unchanged
eligibility receipt, I9 integration receipt, I9 integration discharge, and
content-addressed terminal-record path spec. The E4 ownership-plan schema,
tag/domain registry entries, allocation owners, stages, reservations,
handoffs, and read-only set do not change. The E5 report path belongs only to
this architecture workstream and is not added to I9's 33 implementation
document paths.

The receipt seam makes zero path additions safe: already-owned 00947 performs
context-bearing admission, already-owned 00945 owns the authenticated product
construction, already-owned checker/materializer paths perform A/B/M rebuilds,
and the existing read-only 00945a/00945c and `root6_b.gravity` paths already
mint, consume, and retire the identity-bound receipt. Editing those read-only
paths would duplicate rather than strengthen the boundary.

The exact E5 PRE arithmetic therefore remains:

```text
33 document paths + 8 source-only paths + 42 owned-C paths
  + 3 A9-only paths = 86 distinct PRE-C paths

1 show C + 1 C ls-tree + 86 C cat-file operations
  + 1 show base + 1 base BOOT5 ls-tree + 1 base BOOT5 cat-file
  = 91 PRE operations per participant

33 document decodes + 42 owned-C decodes + 3 A9 decodes
  = 78 strict static decodes per participant
```

For four participants `[:l :m :a :b]`, `ELIGIBILITY-COUNTERS-E5` is
byte-for-byte the exact E4 value:

```clojure
{:participants [:l :m :a :b]
 :q-coordinate-git-operation-count 20
 :pre-discharge-git-operation-count 364
 :git-operation-count 384
 :q-coordinate-decode-count 4
 :receipt-id-check-count 4
 :pre-discharge-reconstruction-count 4
 :pre-discharge-static-decode-count 312
 :pre-discharge-document-descriptor-count 132
 :pre-discharge-source-descriptor-count 52
 :pre-discharge-pre-masked-file-count 8
 :pre-discharge-owned-c-artifact-count 168
 :pre-discharge-a9-artifact-count 12
 :pre-discharge-baseline-boot5-count 4
 :content-match-count 4
 :source-descriptors-match-count 4
 :source-descriptors-root-match-count 4
 :pre-masked-root-match-count 4
 :owned-c-root-match-count 4
 :pre-discharge-match-count 4}
```

The equations remain `4*5=20`, `4*91=364`, `20+364=384`, `4*78=312`,
and `4*42=168`. The source-session counts remain exactly 13 acquisitions,
13 decodes, 40 events, and one finalization per A/B/M, with aggregate
39/39/120/3. Source descriptors remain 52 aggregate. Q remains five Git
operations. G9 remains exactly 49 operations and its post-G9 plan remains
exactly 24 operations. Adding one retained in-memory field and private pure
validation calls changes none of those counters.

## Identity and registry consequences

E5 introduces no serialized schema, digest domain, tag-registry member,
H-domain-registry member, source descriptor, source event, certificate field,
witness field, E9 payload, G9 artifact, process event, or publication path.
`AUTHORITY-CONTEXT` deliberately has no identity. The E2 SH06 link and carrier
domains and all literal V9 certificate/witness domains remain unchanged.

Implementation will nevertheless change bytes in already-owned sources.
Therefore an eventual I9 candidate must rederive the affected C/T source
descriptors, owned-C root, PRE discharge, source-event and source-semantic
roots, semantic-algorithm source root, checker identities, lowering products,
operations, receipts, stage witnesses, certificates, checker outputs,
linked-carrier candidate, E9 payload descriptors, normalized process receipt,
and every downstream evidence/publication identity. It may not retain a stale
value merely because E5 adds no domain. The exact set of actually changed
identities follows the final implementation diff and accepted equations; this
architecture report does not predict concrete hashes.

The existing E4 ownership plan can retain its schema, key set, allocation, and
plan-ID equation because its path/category values do not change. Its eventual
C-bound descriptor/root joins still rederive normally. There is no E5
ownership-plan tag replacement and no registry-count delta.

## Required mutation and call-graph evidence

The existing 42-path allocation contains every required production and test
seam. No new test file is authorized. The implementation must extend the
already-owned SH06 seam, checker child, selected-checker, materializer,
linked-carrier host, direct compact-verifier, runtime-admission, existing-object
replay, process-isolation, source-closure, Git-evidence, and contract-discharge
tests to prove all of the following from a freshly valid baseline.

### E5 handoff and carrier assembly

- Exact E5 11-key builder returns and 21-key checker inputs accept; a missing,
  extra, renamed, duplicated, or non-map `:sh06-authority-carrier` rejects
  before lowering-product or certificate construction.
- The authoritative builder assembles the carrier from cached same-session
  values before finalization. Replacing every post-finalization Git/worktree
  read and the old reacquiring carrier helper with throwing sentinels leaves a
  valid build unchanged. Any attempt to invoke the dead/reacquiring helper as
  authority rejects.
- A carrier with fewer or more than seven C5 authorities, reordered ordinals,
  duplicate ordinals or IDs, a wrong module path, or a projection/ID in place
  of a complete C5 map rejects.
- Mutating C/T, a raw commit payload, a tree row, a leaf blob payload, SH05 ID,
  base-request ID, imports, import bindings, dependency edges, M0, or M1 and
  recomputing every exposed C5/link/carrier ID still rejects against the
  immutable object proofs and independently derived values.
- A missing or extra E2 carrier key, a changed link event-product ID,
  resolution artifact, contextual verification report, Q projection,
  expected-edge row, source descriptor, bundle-core ID, link-authority ID, or
  carrier ID rejects.
- Changing only the sibling plan authority rejects. Changing both sibling and
  carrier plan authorities to the same self-consistent forged value also
  rejects against source event 39 and the governed `resolution.gravity`
  object proof.
- Inserting `:sh06-authority-carrier` or `:authority-context` into a
  `MODULE-TRANSACTIONS` row, certificate, certificate-input map, witness,
  checker output, or E9 carrier rejects the unchanged exact schemas. The
  certificate remains exactly 19/13 keys.

### Context origin and independent reconstruction

- Missing or extra context keys, nil context, a wrong C/T pair, or values mixed
  across two individually valid C/T sessions reject. Nil never invokes a
  one-argument fallback.
- Mutating any stored R, Q, or RC value and recomputing its local and descendant
  IDs rejects the fresh strict SH06 replay or Q/RC joins.
- A self-consistent forged family that changes Q, RC, M0, M1, C5, the plan
  authority, SH06 carrier, owner registry, expected census, witness, and
  certificate IDs rejects against the raw C/T source objects.
- Mutating a witness owner row, root selection, candidate vector, registry
  root, Q external occurrence, expected edge, accepted ID, authority receipt,
  edge-authority root, `BAD-ACTUAL`, diagnostic, or negative fixture, then
  recomputing every descendant witness/certificate/carrier identity, rejects
  when compared with the context-derived rebuild.
- The authentic rebuild contains exactly seven Q/RC pairs, the accepted owner
  registry and eight unique root selections, exactly 1,480 normalized Q/M1
  pairs and expected rows, dense ordinals 0 through 1479, and the exact closed
  `BAD-ACTUAL-E5` with nil committed row and output.
- At least one closed, internally self-consistent attacker certificate family
  must demonstrate the classification boundary: it may pass the applicable
  one-argument shape validator, but it must fail the context-bearing validator.
  A shape validator's rejection is not a substitute for this authority test.

### M, replay, and receipt capability

- M proves ordinal-4/8 shape/transport validation, the unchanged A-before-B
  and B-before-M-session order, construction of its own context after ordinal
  10, independent authoritative validation of A then B, pair validation, and
  context-bearing carrier construction before any accepted response or staged
  payload.
- A and B use distinct processes and private source sessions and never receive
  M's or the peer's context. Swapping child roles, contexts, source receipts,
  or outputs rejects. M does not load either checker namespace.
- Both `:new` and `:existing` launch fresh A/B/M and take the same M context
  path. Existing-mode E/TE payloads cannot enter a context. The fresh first six
  payloads must equal E object bytes only after authoritative M validation.
- Independent object replay reconstructs a fresh context from C/T and invokes
  context-bearing 00947 validation. One-argument carrier/certificate replay
  reports only shape. `:object-replay-only` remains nonauthoritative.
- A production call-graph test proves that every 00947 runtime registration and
  00945a receipt mint is dominated by successful context-bearing pair and
  carrier validation for the identical carrier object. No shape-only result
  can reach registration.
- The 00945c compiler-internal receipt accepts exactly once in the intended
  Root1 or Root6 instruction occurrence. Reuse, copied/rebuilt receipt-shaped
  data, copied runtime maps, different carrier identity, raw wrapper, root,
  plan, argument vector, invocation, owner, scope, thread, instruction, or
  expired context rejects without a committed result or output.
- Root1 and Root6 direct compact tests label isolated one-argument results
  shape-only. Their production public-root tests succeed only through the
  admitted one-shot receipt and fail if that receipt gate is bypassed.
- Sentinels prove that L neither constructs a context nor loads/calls 00947,
  checkers, or materializers; L publishes only a closed successful M response.

Every mutation test must start with a complete authentic value, make the named
mutation, recompute all identities that the attacker is allowed to control,
and observe rejection at the claimed real boundary. A missing fake digest,
constructor echo, unrecomputed stale ID, or fixture assertion is not evidence
for independent authority reconstruction.

## Governing-contract traceability

This correction follows the repository-wide architecture and verification
contracts as follows:

- D1's canonical pipeline requires the same semantic facts and validation
  boundaries to remain observable even when implementation helpers are fused.
  E5 therefore changes the origin check at the compact boundary without
  changing the witness or certificate carrier.
- D3's artifact/authority distinction forbids treating a self-identifying data
  shape as the capability to authorize it. E5 separates pure shape validation,
  authenticated context validation, and the live one-shot receipt.
- D8's safe-semantics boundary requires the forged or unauthorized edge to be
  rejected with no committed row or output. E5 independently reconstructs the
  exact closed `BAD-ACTUAL` rather than trusting its witness projection.
- D9's evidence charter requires provenance and recomputable proof inputs.
  E5 binds owner/edge evidence back to raw C/T object proofs through the exact
  E2 carrier and requires independent reconstruction during review.
- The V9 architecture continues to own process isolation, semantic witness,
  certificate, E9/G9 topology, counters, and publication. E5 changes none of
  those schemas.
- E1 continues to own pre-evidence discharge and immutable-object eligibility.
  E5 adds no PRE read or eligibility counter.
- E2 continues to own C5, strict lexical SH06 contexts, the link-authority
  equation, and the exact SH06 carrier. E5 transports that complete accepted
  carrier rather than defining a rival projection.
- E3 continues to own the static-fixture cycle repair, complete 13/13/40/1
  checker-local source session, algorithm-source closure, lowering/witness
  construction order, and A/B/M separation. E5 adds one acquisition-free
  same-session product and delays M semantic authority until M has its own
  context.
- E4 continues to own the literal 19/13-key certificate, complete witness and
  split profiles, direct Root1/Root6 shape implementations, 00947 convergence,
  and exact 42-path allocation. E5 narrows the authority meaning of pure
  one-argument validation and retains that allocation.

If an implementation choice cannot satisfy all of those joins simultaneously,
it must report a new architecture conflict. It may not silently weaken E2
carrier completeness, E3 process independence, E4 certificate exactness, or
the V9 publication boundary.

## Implementation and lifecycle consequence

This report is a draft architecture correction. It changes no implementation,
governance record, active-workstream shard, terminal record, ownership
reservation, evidence object, pin, or release state. The current I9 candidate
must not freeze, publish, or claim implementation acceptance while its checker
handoff cannot carry the exact E2 SH06 authority carrier or its production
validators use witness-derived owner/edge authority.

Only after this report is independently reviewed, accepted, admitted, and
integrated may I9 reconcile onto authoritative main, add
`sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v1` as a
required integrated dependency, and implement the correction within the exact
42-path allocation. Reconciliation must use the then-current accepted base,
preserve all five dependency relations in this report, regenerate all changed
identities, rerun the complete unchanged V9/E1/E2/E3/E4 gates plus the E5
tests, and record the exact resulting implementation base and evidence.

Architecture acceptance of E5 does not accept an implementation. Implementation
completion does not itself establish evidence eligibility or integration.
Integration of an implementation does not establish SH-07 completion,
self-hosting, hostile-host proof, release, performance authority, or seed
retirement. Those remain separate lifecycle decisions with their own required
evidence.

## Residual boundaries and nonclaims

This report does not claim:

- that the current provisional I9 implementation or any existing evidence
  bundle satisfies E5;
- that a one-argument certificate, witness, compact-carrier, or linked-carrier
  pure predicate authenticates source origin;
- that a pinned hash, two equal checker outputs, or an M response substitutes
  for independent C/T reconstruction;
- that `AUTHORITY-CONTEXT` is or may become a serialized proof object;
- that the runtime receipt proves semantics independently of the preceding
  context-bearing 00947 admission;
- that `:object-replay-only` is authoritative;
- that L is a semantic checker;
- that read-only 00945a, 00945c, or `root6_b.gravity` may be edited under this
  architecture workstream;
- that the current dead/post-finalization carrier helper becomes authoritative;
- that an unchanged path count means downstream hashes remain unchanged;
- that any path, counter, certificate key, witness key, digest domain, or
  registry entry beyond those explicitly stated changes; or
- that this architecture workstream grants release, self-hosting, performance,
  or seed-retirement authority.

The accepted collision-resistance assumptions remain assumptions. E5 does not
upgrade SHA-1 Git object identity or SHA-256 semantic identities into a proof
against cryptographic compromise. It requires the same explicit V9/E2 object
validation and identity assumptions and makes no broader hostile-host claim.

## Consolidated supersession summary

| Accepted contract before E5 | E5 correction |
| --- | --- |
| E3 ten-key receipted builder return | Exact 11-key return adding complete `:sh06-authority-carrier` assembled acquisition-free before finalization |
| E3 20-key checker input | Exact 21-key input adding the same complete carrier |
| Complete C5 values exist only lexically during transaction construction | The exact ordered seven-member vector remains inside the complete carried E2 SH06 carrier |
| Caller/ambient owner and edge projections can be compared with themselves | Authoritative 00947 validation rebuilds owner, roots, 1,480 expected edges, and rejection from private authenticated context |
| Pure one-argument compact validators are described as authority-bearing | Pure one-argument forms are shape-only; context-bearing 00947 forms carry semantic authority |
| Root1/Root6 data predicates appear to be the runtime authority source | The already-present private 00945a/00945c single-use receipt conveys the prior 00947 decision to the exact admitted invocation |
| M response validation wording does not distinguish source authority timing | Ordinals 4/8 complete shape/transport validation; semantic authority follows M's own ordinal-9/10 source session and precedes output |
| Existing-object bytes can be decoded and rehashed | Authoritative existing/reviewer replay first rebuilds context from C/T, then validates E certificates/carrier |
| E4 owns 42 paths | E5 still owns exactly those 42 paths; zero code/test path delta and unchanged PRE/G9 counts |

No row in this table permits a legacy/new union. At every E5 boundary there is
one exact data schema and one explicitly classified authority path.

## Acceptance criteria

This architecture correction is acceptable only if fresh independent review
confirms all of the following:

1. The exact E5 builder return has 11 keys and the exact E5 checker input has
   21 keys, adding only complete `:sh06-authority-carrier`.
2. That carrier is assembled without acquisition before the sole session
   finalization from cached same-session values; the current post-finalization
   reacquiring helper is not authority.
3. The carrier's complete ordered seven-member C5 vector and sibling complete
   plan authority reconstruct the exact E2 link and carrier IDs and revalidate
   against C/T, transactions, M0/M1, Q, artifacts, event products, expected
   census, and bundle core.
4. `MODULE-TRANSACTIONS`, the 19-key certificate, and its 13-key input map are
   byte-schema unchanged, and neither the SH06 carrier nor context is
   serialized into the certificate, witness, checker output, or E9 carrier.
5. The exact seven-key acyclic `AUTHORITY-CONTEXT` contains complete C/T,
   R/Q/RC transactions, M0, M1, bundle core, plan authority, and E2 SH06
   carrier, has no identity, and rejects nil or mixed origins.
6. Authoritative validation freshly replays context-bearing SH06/Q, validates
   RC, independently rebuilds the owner/root selections, 1,480-row pairing and
   expected census, and exact `BAD-ACTUAL` before comparing witness values.
7. Pure one-argument Root1, Root6, and 00947 data validators are explicitly
   shape-only and cannot authorize construction, admission, publication, or
   review.
8. 00947's context-bearing validation dominates runtime registration, while
   the existing private 00945a/00945c one-shot identity receipt safely conveys
   that decision into the exact Root1/Root6 invocation without serializing the
   context.
9. A and B construct independent contexts and witnesses with no peer or M
   input; M later constructs its own context, validates A and B separately,
   and withholds accepted output until pair/carrier validation succeeds.
10. L remains process/publication orchestration only, and both `:new` and
    authoritative `:existing` take the fresh A/B/M path. Independent E-object
    review reconstructs context from C/T; `:object-replay-only` remains
    nonauthoritative.
11. The implementation allocation remains exactly 42 owned-C paths, eight
    source-only paths, 86 distinct PRE paths, 91 PRE operations per
    participant, 78 strict static decodes per participant, and the exact
    unchanged aggregate counters printed above. There are zero new code or
    test paths.
12. The complete mutation matrix distinguishes internal shape from external
    authority, rejects recomputed self-consistent owner/edge families, proves
    same-session acquisition-free assembly, and proves one-shot receipt
    consumption and call-graph dominance.
13. No unchanged V9/E1/E2/E3/E4 schema, identity equation, profile bound,
    process-isolation rule, Git topology, publication rule, G9 count, or
    authority ceiling is weakened.
14. The report remains ASCII, owns only its unique architecture report path,
    passes architecture and document validation, and is integrated through a
    separate governance action.
15. I9 remains draft and unfrozen until E5 is integrated, added as a required
    dependency, implemented within the exact allocation, and supported by
    fresh reviewable evidence.
