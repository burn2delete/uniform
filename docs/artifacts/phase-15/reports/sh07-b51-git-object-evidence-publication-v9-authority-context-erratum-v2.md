# SH-07 B51 Git-Object Publication V9 Authority-Context Erratum V2

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
projections proves internal consistency, not their origin in authenticated
C/T-bound Q, resolved-core, M0, M1, and SH06 products. The seven-key
`AUTHORITY-CONTEXT` below fixes exactly that L2-origin seam by independently
rebuilding `OWNER-REGISTRY`, `EXPECTED`, and `BAD-ACTUAL`.

That L2 correction is necessary but not sufficient to authorize a complete
certificate, certificate pair, linked carrier, publication, independent
review, or runtime receipt. The rejected V1 report incorrectly granted that
broader authority to `AUTHORITY-CONTEXT`. V2 adds a separate dominating E3
full-precertificate gate. That gate starts from an exact locally authenticated
21-key E5 checker input only after its exact 11-key same-session source
products and receipt have validated. It independently rebuilds and requires
byte equality for the complete lowering product, seven operations, 34
measurements and traces, seven fuel receipts, seven final stage witnesses,
enclosing witness, and all certificate joins. Every authoritative production,
M, runtime-admission, and replay boundary must pass both the L2-origin gate and
the full-precertificate gate.

The correction is deliberately narrow. All unchanged V9/E1/E2/E3/E4 schemas,
identities, bounds, Git observations, process separation, publication
topology, rejection rules, and authority ceilings remain normative. This
report specifies architecture only and makes no implementation or evidence
claim. `AUTHORITY-CONTEXT` remains nonserialized and L2-origin-only; the
full-precertificate gate is likewise verifier-internal and nonserialized.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2",
  "invariant_family": "architecture/self-hosting-sh07-b51-git-object-evidence-publication-v9-authority-context-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2.md",
  "status": "draft",
  "base_commit": "d885c3cff09e8efc1693404dfa9303a3050663f9",
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
  "historical_references": [
    {
      "id": "sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v1",
      "terminal_state": "rejected",
      "role": "history"
    }
  ],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```

## Authority lineage, V1 rejection, and E5 supersession

This report is the corrected E5 successor. It depends only on the integrated
V9 architecture and E1 through E4 errata listed in the authority block. V1 is
terminally rejected and appears only in `historical_references`; it is not a
dependency, accepted authority, or implementation input.

V1 correctly selected the complete E2 SH06 carrier, acquisition-free
same-session assembly, L2-origin context, and unchanged 42-path allocation.
It failed reviewer correction A because it allowed the seven-key L2 context to
authorize the whole certificate/pair/carrier path. That context omits the
complete E3 source forms, M2 products, semantic-algorithm descriptors/root,
checker/provenance inputs, complete lowering product, non-L2 operations and
receipts, and six non-L2 final stage witnesses. Rehashing those omitted values
inside a certificate is not independent reconstruction. V2 retains the sound
carrier/L2/count design and adds the separate full-precertificate gate that
authenticates every omitted dependency before whole-object authority exists.

V2 supersedes only the following accepted statements and rejected V1
corrections:

1. E3's exact checker input and receipted builder return are replaced by the
   exact E5 key sets below.
2. Any V9/E4 statement that a pure one-argument Root1, Root6, or 00947 data
   validator is authority-bearing is narrowed to shape-only validation.
   Whole-certificate authority requires the exact E5 source-handoff,
   L2-origin, and full-precertificate gates below.
3. The caller-supplied `OWNER`, `EXPECTED-AUTHORITY`, and related ambient
   equality checks in V9's L2 verification are replaced by independent
   reconstruction from `AUTHORITY-CONTEXT`.
4. E4's exact 42-path ownership allocation is retained. The correction changes
   values and identities in already-owned paths but adds no code or test path.
5. V1's claim that context-bearing validation over the seven-key
   `AUTHORITY-CONTEXT` can authorize an entire certificate, pair, carrier,
   publication/review result, or runtime receipt is rejected. That context
   authenticates L2 owner/edge origin only.

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

The SH06 carrier is source-handoff material and `AUTHORITY-CONTEXT` is
L2-origin material only. Neither is whole-precertificate authority. Adding
either to the certificate, certificate inputs, semantic witness, checker
output, E9 linked carrier, or E9 identity receipt rejects the corresponding
closed schema. No new certificate version is introduced.

## Three required verifier-internal gates

Whole-object authority is the conjunction of three ordered, nonserialized
gates. No gate returns an artifact, hash, token, receipt, cache entry, or value
that a caller can replay as authority. A success value is lexical to one
validation call. Failure at any gate returns rejection and cannot select a
one-argument fallback.

### Gate H: exact source-handoff authentication

The first gate has exactly two arguments:

```clojure
SH07-B51-SOURCE-HANDOFF-GATE-E5(
  RECEIPTED-BUNDLE-CORE-PRODUCTS-E5,
  CHECKER-SEMANTIC-INPUT-E5)
```

The first argument must have the exact 11-key schema above. The gate validates
the final `:source-read-receipt` as the sole finalized 13-acquisition,
13-decode, 40-event, one-finalization receipt for the local governed C/T
session. It revalidates the receipt's descriptor/event vectors and roots,
commit and tree object proofs, raw source bytes, direct C2 artifacts, seven
R/Q/RC transactions, complete source-semantic carrier, complete plan
authority, complete E2 SH06 carrier, M0, M1, M2, source forms, bundle core,
and execution-bundle-core ID. Assembly of the E2 carrier must already have
been acquisition-free and before finalization.

The second argument must have the exact 21-key schema above and must be built
locally from the same validated return, the same C/T-bound PRE/classpath
preflight, and the selected checker identity. These nine shared values must be
byte-for-byte equal between the two arguments:

```clojure
[:m0s :m1 :m2s :bundle-core :source-forms :module-transactions
 :sh06-resolution-plan-authority :sh06-authority-carrier
 :source-semantic-carrier]
```

The return's `:execution-bundle-core-id` must equal the authenticated ID of
the input's complete `:bundle-core`. The remaining input values are not
caller assertions: `:snapshot-commit`, `:snapshot-tree`, architecture report
digest, predecessor executable descriptor, module source identities/counts,
emitter identities, semantic-algorithm source descriptors/root, selected
`:checker`, and `:provenance` must each join its independently authenticated
C/T, PRE, classpath, or process-identity source under the unchanged V9/E1/E3
rules. A value copied from a certificate, checker output, linked carrier,
fixture, peer process, M response, or existing E object is not a locally
authenticated checker input.

Gate H establishes only that the complete 21-key input is a faithful local
view of one validated 11-key source handoff. It does not authorize a lowering
product, operation, receipt, witness, certificate, pair, carrier,
publication, review result, or runtime receipt.

## Exact verifier-internal authority context

Only after Gate H succeeds does A, B, M, runtime admission, or independent
replay construct this exact seven-key plain map from shared values in those
two arguments:

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

The join from the 21-key input into the context is exact and mandatory:

```clojure
AUTHORITY-CONTEXT[:content :commit]
  = CHECKER-SEMANTIC-INPUT-E5[:snapshot-commit]
AUTHORITY-CONTEXT[:content :tree]
  = CHECKER-SEMANTIC-INPUT-E5[:snapshot-tree]
AUTHORITY-CONTEXT[:module-transactions]
  = CHECKER-SEMANTIC-INPUT-E5[:module-transactions]
AUTHORITY-CONTEXT[:m0s]
  = CHECKER-SEMANTIC-INPUT-E5[:m0s]
AUTHORITY-CONTEXT[:m1]
  = CHECKER-SEMANTIC-INPUT-E5[:m1]
AUTHORITY-CONTEXT[:bundle-core]
  = CHECKER-SEMANTIC-INPUT-E5[:bundle-core]
AUTHORITY-CONTEXT[:sh06-resolution-plan-authority]
  = CHECKER-SEMANTIC-INPUT-E5[:sh06-resolution-plan-authority]
AUTHORITY-CONTEXT[:sh06-authority-carrier]
  = CHECKER-SEMANTIC-INPUT-E5[:sh06-authority-carrier]
```

Every equality is byte equality after exact closed-schema validation. The
same context values must also equal their Gate-H source-product counterparts
where a counterpart exists. A caller may not supply the context as an
unrelated third view and satisfy the gate by recomputing its hashes.

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

Passing this validation establishes Gate L2 only. Gate L2 authenticates the
origin of the L2 owner registry, public-root selection, expected 1,480-edge
census, edge authority, and closed negative result. It cannot authorize the
complete lowering product, any non-L2 operation or fuel receipt, any of the
six non-L2 stage witnesses, the enclosing witness, a certificate, a pair, a
linked carrier, publication/review success, runtime registration, or receipt
minting.

## Acyclic construction order

The required dependency order is:

```text
C/T raw object proofs and governed source session
  -> source descriptors, M0, M1, plan authority, and seven C5 authorities
  -> seven strict R/Q/RC module transactions
  -> exact E2 SH06 authority carrier
  -> exact 11-key source products and finalized receipt
  -> exact locally authenticated 21-key checker input
  -> Gate H
  -> AUTHORITY-CONTEXT -> Gate L2 owner/root/edge/rejection rebuild
  -> independent full receipt-free preproducts and lowering product
  -> operations, measurements/traces, receipts, stage witnesses, and WITNESS
  -> Gate F whole-precertificate comparison
  -> role-specific certificate and checker output
  -> linked-carrier candidate and E9 payloads
```

For M, runtime admission, or an independent object reviewer, an
already-constructed certificate and linked carrier are comparison values only.
They never enter source-product reconstruction, checker-input authentication,
context construction, or full-precertificate reconstruction. The context does
not contain a lowering-product ID, operation, fuel receipt, stage witness,
certificate, checker output, linked carrier, E9 blob, publication commit, or
validation result. Gate F takes the certificate only after its expected
precertificate has been independently rebuilt. No rightward value points back
into any earlier gate. This is the required acyclic boundary.

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

Gate L2 stops at the complete receipt-free L2 pre-stage, including the owner
selection, edge authority, closed rejection, diagnostic preimage, negative
fixture preimage, and receipt-free roots. It does not obtain the L2 operation,
trace, fuel receipt, receipt-dependent roots, final L2 witness, or enclosing
witness from the seven-key context. Those values are constructed only by Gate
F from the complete 21-key input. The old six-argument
`VERIFY-UNAUTHORIZED-CROSS-FRAGMENT(QS,M0,M1,OWNER,EXPECTED,CANDIDATE)` is not
an authoritative compact-verifier API. Its pure equation may remain a private
shape helper, but production authority is the zero-projection-input E5 rebuild
from `AUTHORITY-CONTEXT`.

The exact Gate-L2 call is an adjacent part of the whole gate:

```clojure
SH07-B51-L2-ORIGIN-GATE-E5(
  CERTIFICATE,
  RECEIPTED-BUNDLE-CORE-PRODUCTS-E5,
  CHECKER-SEMANTIC-INPUT-E5)
```

It first invokes Gate H, derives the seven-key context from byte-equal shared
input values, rebuilds the receipt-free L2 values above without reading the
certificate, and only then compares the certificate witness's L2 projections.
Its three
arguments are exact; nil, a missing argument, an extra loose projection, or a
two-argument `(CERTIFICATE, AUTHORITY-CONTEXT)` substitute rejects.

### Gate F: independent full-precertificate reconstruction

The dominating whole-precertificate gate has two exact adjacent
three-argument forms. Selected checker A or B uses the production form before
returning any accepted output:

```clojure
SH07-B51-FULL-PRECERTIFICATE-PRODUCTION-GATE-E5(
  PRECERTIFICATE-CANDIDATE-E5,
  RECEIPTED-BUNDLE-CORE-PRODUCTS-E5,
  CHECKER-SEMANTIC-INPUT-E5)
```

M, runtime admission, existing-object replay, and independent review use the
validation form on the externally obtained certificate:

```clojure
SH07-B51-FULL-PRECERTIFICATE-VALIDATION-GATE-E5(
  CERTIFICATE,
  RECEIPTED-BUNDLE-CORE-PRODUCTS-E5,
  CHECKER-SEMANTIC-INPUT-E5)
```

`PRECERTIFICATE-CANDIDATE-E5` is verifier-internal scratch with exactly these
seven keys:

```clojure
#{:certificate :lowering-product :operation-manifest :traces
  :fuel-receipts :stage-witnesses :semantic-witness}
```

`:operation-manifest` is the exact seven-row V9 vector. `:traces` is exactly
`(mapv :trace FUEL-RECEIPTS)` in operation-ordinal order; concatenating those
seven dense trace vectors yields exactly 34 rows, whose observed values are
the 34 measurements. `:fuel-receipts` is the exact seven-row vector containing
those same traces. `:stage-witnesses` is exactly:

```clojure
[WITNESS[:l1] WITNESS[:l2] WITNESS[:c2] WITNESS[:c6]
 WITNESS[:boot2] WITNESS[:boot3] WITNESS[:boot4]]
```

The scratch map has no schema, identity, serialization, output coordinate, or
authority after the call. It exists only at A or B and contains the values
just constructed by that selected checker. It is never transported to M,
runtime admission, or replay and is never reconstructed from E.

Both Gate-F forms first require Gate H and Gate L2. In a separate scratch
derivation that reads neither the production candidate nor the external
certificate, the gate uses the complete locally authenticated 21-key input to
repeat the unchanged E3/V9 construction:

1. derive all receipt-free carriers, `FOUNDATION-PREPRODUCT`, `PRE-STAGES`,
   `FUEL-SUBJECTS`, and the complete `LOWERING-PRODUCT`;
2. construct the exact seven operations and operation-schedule ID;
3. execute all 34 bounded measurements, produce their exact traces, and
   construct the exact seven fuel receipts;
4. construct the final stage witnesses in order
   `[:l1 :l2 :c2 :c6 :boot2 :boot3 :boot4]` and the complete enclosing
   `WITNESS`; and
5. rerun the selected source-first or M2-first checker algorithm to construct
   its claim, result, raw/M2 transcripts, certificate inputs, provenance joins,
   evidence root, and role-specific 19-key certificate.

For the production form, the independently rebuilt complete lowering product,
seven operations, 34 measurement rows and traces, seven receipts, seven final
stage witnesses, complete witness, and complete certificate must each equal
the corresponding A/B candidate value byte-for-byte. An ID-only or
selected-field comparison is insufficient at this local production boundary.

For the validation form, no report pretends that E transports a complete
lowering product: the closed certificate does not. The local full rebuild is
compared directly and byte-for-byte with every complete value the external
certificate does carry: `:semantic-witness :operation-manifest`, all seven
`:fuel-receipts` including all 34 trace rows and observed measurements, the
seven stage-witness maps, and the complete `:semantic-witness`. The complete
local lowering product's ID must equal certificate input
`:lowering-product-id`; its full fuel subjects, stage-semantic preimages, and
all downstream product joins must reproduce the carried operations, receipts,
and witnesses. The gate also independently constructs the expected 19-key
certificate and requires it to equal the external certificate byte-for-byte.
Any checker output or linked carrier at the adjacent boundary must join that
exact certificate and its independently rebuilt values byte-for-byte.

Both forms repeat every unchanged V9/E3 cross-join, including the
operation-to-product, receipt-to-operation/bounds/trace, stage-to-subject,
stage-to-operation, witness-to-stage/root, claim/result/transcript,
profile-bound, evidence-root, certificate-ID, and checker-output joins.

Every certificate input shared with the 21-key input is joined to its local
source rather than trusted from the certificate: architecture report digest,
C, T, predecessor executable descriptor ID, seven module source identities
and byte counts, emitter source and semantic IDs, seven M0 IDs, M1 ID, seven
M2 IDs, bundle-core ID, and independently rebuilt lowering-product ID. The
certificate's checker and every shared provenance value likewise equal the
selected checker and authenticated provenance values in the 21-key input.
The exact eight 21-key-input-to-L2-context equalities above remain mandatory;
Gate F cannot substitute certificate fields for any of them.

Every unchanged E4 witness rule remains mandatory: the exact witness key set,
split witness and certificate bounds, operation schedule and operation IDs,
seven fuel receipts, seven stage witnesses, all member/root equations, the
complete lowering-product join, and final witness and certificate IDs. E5
changes no schema or identity equation. It changes which independent local
derivation must dominate a whole-object authority decision.

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

### Full-gated authoritative 00947 validators

00947 owns the shared whole-object boundary. It adds these exact
three-argument semantic APIs; ordinary language arity syntax may differ, but
their argument meaning may not:

```clojure
p15-s23-sh07-b51-compile-census-certificate-v1-authoritative?
  (CERTIFICATE, SOURCE-PRODUCTS, CHECKER-INPUT)

p15-s23-sh07-b51-compile-census-certificate-pair-v1-authoritative?
  (CERTIFICATES, SOURCE-PRODUCTS, CHECKER-INPUTS)

p15-s23-sh07-b51-linked-carrier-v4-authoritative?
  (CARRIER, SOURCE-PRODUCTS, CHECKER-INPUTS)

p15-s23-sh07-b51-build-linked-carrier-v4-candidate-authoritatively
  (PRODUCTS, CERTIFICATES, AUTHORITY-SOURCES)
```

`SOURCE-PRODUCTS` is exactly the validated 11-key E5 return.
`CHECKER-INPUT` is exactly one locally authenticated 21-key input.
`CHECKER-INPUTS` is exactly `[CHECKER-INPUT-A CHECKER-INPUT-B]` in role order;
both entries are built locally and each passes Gate H against
`SOURCE-PRODUCTS`. `AUTHORITY-SOURCES` is exactly the private two-key map
`{:source-products SOURCE-PRODUCTS :checker-inputs CHECKER-INPUTS}`. It is not
serialized or retained.

The certificate API runs the one-argument shape predicate, Gate H, Gate L2,
and the Gate-F validation form and returns true only if all pass. Selected A/B
production additionally runs the Gate-F production form on its local exact
seven-key candidate before the accepted checker output can escape. The pair
API invokes the complete validation chain separately for A and B, clearing
all rebuild scratch between roles, before applying the unchanged pair joins.
The carrier API first invokes the pair API and then rebuilds and compares every
carrier/product/certificate join. The candidate builder first invokes the pair
API, requires `PRODUCTS` and its complete M0, M1, M2, bundle-core,
execution-bundle, source, plan, and SH06 projections to join the authenticated
source products and both full-gate results, and only then constructs the
carrier candidate.

No authoritative API accepts an `AUTHORITY-CONTEXT` as a substitute for the
11-key products or 21-key input. No two-argument
`(CERTIFICATE, AUTHORITY-CONTEXT)` API is authoritative. No call may invoke a
one-argument form as a fallback after a missing or failing gate. A public
one-argument admission or frozen-authority wrapper is authoritative only if it
privately reconstructs the exact source products and role-specific checker
inputs and completes these three-argument APIs before returning success.
Loading a pin and rerunning self-hashes is insufficient.

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

00947 must reconstruct the exact 11-key products and both exact 21-key
role-specific inputs, then pass Gate H, Gate L2, Gate F, and the exact
three-argument pair/carrier APIs before it calls the existing admitted-runtime
registration path that permits 00945a to mint this receipt. The exact carrier
object that passed that whole chain is the object bound to the receipt. Thus
Root1 and Root6 can consume the prior authoritative 00947 decision without
serializing the source products, checker input, `AUTHORITY-CONTEXT`, or full
rebuild scratch into Gravity data or adding a public-root parameter. A forged
carrier that merely passes Root1/Root6 shape predicates or Gate L2 cannot
acquire the private receipt.

This classification is why
`bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` remains source-only
and read-only. Its apparent `carrier-authoritative?` name does not make its
pure certificate projections authoritative; its actual production authority
comes from the compiler-internal receipt call already present in the Root6
request proof. No E5 edit to that file is required.

## Checker A and checker B independence

The selected checker processes remain independent exactly as V9/E3 require.
After its own one governed C/T source session finalizes, A validates its exact
11-key return, constructs its exact locally authenticated 21-key A input, and
passes Gate H. It derives `AUTHORITY-CONTEXT-A`, passes Gate L2, independently
constructs its complete seven-key precertificate candidate, and passes Gate F
before returning a certificate or checker output. B later repeats that entire
sequence using only B's session, B's E5 return, B's PRE/classpath preflight,
and B's selected identity. Neither products, input, context, gate result,
candidate, rebuild scratch, witness, certificate, checker output, namespace,
Var, process handle, nor cached derivation crosses between them.

Each checker independently replays all seven strict SH06 transactions and
independently rebuilds its owner registry, 1,480-edge pairing and expected
census, closed rejection, complete lowering product, seven operations, 34
measurements/traces, seven receipts, seven stage witnesses, enclosing witness,
and role-specific certificate. It then independently rebuilds them again in
Gate F and requires byte equality. Equal C/T inputs require byte-equal common
semantic values, but that equality is an outcome, not an input. A cannot call
or load B's selected checker closure, and B cannot call or load A's.

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
   governed C/T source session. Without using either child response, it
   validates the exact 11-key E5 return and its finalized source receipt.
4. After ordinal 10, M constructs exact local 21-key
   `CHECKER-INPUT-A-M` and `CHECKER-INPUT-B-M`. Their nine common source fields
   come only from M's validated 11-key return; their checker/provenance fields
   join the separately authenticated A and B process identities and closed
   responses. Both inputs independently pass Gate H. M derives a separate
   seven-key L2 context for each call and requires all input-to-context byte
   joins.
5. Before carrier-candidate construction, staging, or an accepted M response,
   M invokes the exact three-argument authoritative certificate API for A and
   then B. Each call constructs fresh local expected-rebuild scratch and passes
   Gate L2 and the Gate-F validation form. It joins the complete local lowering
   product by ID and all of its downstream preimages to the certificate, and
   compares the carried operation manifest, measurements/traces, receipts,
   seven stage witnesses, complete witness, and complete certificate directly
   with the independent expectations. M clears all role-local scratch between
   calls; no A value or decision is a B expected value.
6. M then invokes the exact three-argument pair API and the authoritative
   candidate builder with
   `{:source-products SOURCE-PRODUCTS-M
     :checker-inputs [CHECKER-INPUT-A-M CHECKER-INPUT-B-M]}`. It validates the
   resulting linked carrier through the exact three-argument carrier API before
   any payload is staged.

The V9 phrases "A response fully validated" and "B response fully validated"
at ordinals 4 and 8 are therefore narrowed only with respect to E5 semantic
authority: they mean complete bounded response/process and internal-shape
validation. The final semantic authority decision is deliberately withheld
until M owns validated source products, both locally authenticated 21-key
inputs, and successful H/L2/F results. The literal 11 coordinator events, their
role/operation pairs, event IDs, event root, A-before-B ordering, and
B-before-M-session ordering do not change. The post-ordinal-10 calls are pure
verification work, not a new process event or source acquisition.

M's products, checker inputs, contexts, full-rebuild scratch, and gate
results are not inserted into its response, coordinator receipt, payload,
certificate pair, or linked carrier. An accepted M response is produced only
if all three gates and all three-argument APIs returned success and the exact
already-normative response products were constructed. A child self-validation
flag, A/B byte agreement, Gate L2 alone, or the two shape validations without
M's later full reconstruction is insufficient.

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
  discharge over values M produced only after all H/L2/F checks;
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
then launches fresh M, A, and B exactly as in `:new`. A and B each validate
their own 11-key handoff, construct their own 21-key input, and pass H/L2/F.
M later constructs its own 11-key products and both role-specific 21-key
inputs and runs the three-argument certificate, pair, candidate, and carrier
APIs. Only then may L compare the newly produced first six payload byte vectors
with their E blobs and perform the accepted seventh-receipt normalization. E,
TE, publication mode, and prior payloads do not enter any A/B/M products,
inputs, contexts, or precertificate rebuilds.

An independent implementation reviewer starting only from accepted Git
objects must likewise begin at C/T. The reviewer strictly reconstructs the
governed source session, exact 11-key E5 builder products and receipt, seven
module transactions, exact E2 SH06 carrier, and exact locally authenticated
21-key A and B inputs. It passes Gate H, derives a fresh L2 context per role,
passes Gate L2 and Gate F independently, then decodes the certificate pair and
linked carrier from E only as comparison values and invokes the exact
three-argument 00947 APIs. Recomputing only certificate, witness, E9 carrier,
or L2-context identities is shape review and cannot satisfy the authoritative
replay obligation.

The zero-child `:object-replay-only` mode remains diagnostic and
nonauthoritative. It may report shape and internal-identity results, but it
cannot satisfy authoritative reload, runtime admission, lifecycle join, G9,
publication, implementation acceptance, or this erratum's H/L2/F
reconstruction requirement.

## Runtime admission and receipt lifecycle

The eventual frozen runtime carrier and pins do not serialize source products,
checker inputs, `AUTHORITY-CONTEXT`, precertificate candidates, or gate
results. Before any public-root invocation is admitted, 00947 must use the
existing governed object-backed construction path for that exact C/T to
rebuild and validate the exact 11-key products, construct both exact
role-specific 21-key inputs, and pass H/L2/F plus the three-argument pair and
linked-carrier APIs. It then requires the supplied raw carrier to be the exact
validated carrier and selects the exact public root and plan. Only after that
full chain may it register the live runtime context and permit 00945a to mint
the one-shot receipt consumed by 00945c.

Compatibility admission that returns an unadmitted runtime value has no
receipt or token authority. A direct call into Root1 or Root6, a copied runtime
map, a copied receipt-shaped map, a shape-valid carrier, a Gate-L2-only
carrier, or a carrier whose private full-gate admission has expired cannot
enter an authoritative root. The existing
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

Zero path additions are sufficient only if the required tests prove the
specified call graph: already-owned 00947 performs the H/L2/F-gated
three-argument admission, already-owned 00945 owns authenticated product
construction, and already-owned checker/materializer paths perform A/B/M full
rebuilds. The existing read-only 00945a/00945c and `root6_b.gravity` paths
already mint, consume, and retire the identity-bound receipt. Editing those
read-only paths is neither required nor authorized by this report.

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

### Gate H, Gate L2, and independent L2 reconstruction

- Missing or extra context keys, nil context, a wrong C/T pair, or values mixed
  across two individually valid C/T sessions reject. Nil never invokes a
  one-argument fallback.
- Every one of the nine shared 11/21-key fields is mutated separately in one
  argument while the other remains authentic; Gate H rejects before L2 or
  full-precertificate work. Mutating both copies and recomputing their local
  IDs still rejects against the independent receipt, C/T, PRE, classpath, or
  selected-checker authority.
- Each of the exact eight checker-input-to-context joins is mutated separately.
  Two self-consistent but nonidentical views reject. Passing a seven-key
  context without the exact 11-key and 21-key Gate-H arguments rejects and
  cannot invoke any whole-object API.
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
  one-argument shape validator, but it must fail the H/L2/F-gated
  three-argument validator. A shape validator's rejection is not a substitute
  for this authority test.

### Gate F full-precertificate rejection matrix

- A valid production candidate accepts only when an independent Gate-F rebuild
  is byte-equal for the complete lowering product, all seven operations, all 34
  measurements and trace rows, all seven receipts, all seven final stage
  witnesses, the complete semantic witness, and the complete certificate.
- Mutating each full lowering-product field in turn, then recomputing its ID,
  all seven operation IDs, schedule, traces, receipts, stage witnesses,
  enclosing witness, certificate, checker output, and carrier identities,
  rejects against the locally authenticated 21-key input. At A/B this is a
  complete-product byte comparison; at M/runtime/replay the independent local
  complete product joins by ID and all downstream preimages because E does not
  transport the complete product.
- Mutating each of the seven operation rows, each of the 34 measurement/trace
  rows, each of the seven receipts, and each of the seven stage witnesses is
  tested separately. The attacker recomputes every descendant identity; Gate F
  still rejects against its independent operation, bounded traversal, receipt,
  and stage derivations.
- Mutating the complete enclosing witness, any of the 13 certificate inputs,
  checker, shared provenance, claim/result/transcript join, profile bounds,
  evidence root, or certificate ID and recomputing every attacker-controlled
  descendant rejects against the independently constructed expected 19-key
  certificate.
- A self-consistent non-L2 forgery changes M2, source forms,
  semantic-algorithm descriptors/root, non-L2 receipt-free carriers, all six
  non-L2 operations/receipts/stage witnesses, the enclosing witness,
  certificate, checker output, and carrier while leaving the authentic
  seven-key L2 context unchanged. The fixture must demonstrate that Gate L2
  alone is not whole authority and that Gate F rejects the family.
- Missing, nil, reordered, mixed-session, or extra arguments reject every
  three-argument API. In particular, no `(CERTIFICATE, AUTHORITY-CONTEXT)` or
  context-only adapter may reach a successful certificate, pair, carrier,
  candidate, publication, review, registration, or receipt-minting result.

### M, replay, and receipt capability

- M proves ordinal-4/8 shape/transport validation, the unchanged A-before-B
  and B-before-M-session order, construction of its own exact 11-key products
  and both exact 21-key inputs after ordinal 10, independent H/L2/F validation
  of A then B, and full-gated three-argument pair/candidate/carrier validation
  before any accepted response or staged payload.
- A and B use distinct processes and private source sessions and never receive
  M's or the peer's context. Swapping child roles, contexts, source receipts,
  or outputs rejects. M does not load either checker namespace.
- Both `:new` and `:existing` launch fresh A/B/M and take the same H/L2/F path.
  Existing-mode E/TE payloads cannot enter source products, checker inputs,
  contexts, or rebuild scratch. The fresh first six payloads equal E object
  bytes only after full-gated M validation.
- Independent object replay reconstructs exact 11-key products and exact
  21-key role inputs from C/T and invokes the H/L2/F-gated three-argument 00947
  APIs. One-argument carrier/certificate replay reports only shape.
  `:object-replay-only` remains nonauthoritative.
- A production call-graph test proves that every 00947 runtime registration and
  00945a receipt mint is dominated by Gate H, Gate L2, both Gate-F validation
  calls, and successful three-argument pair/carrier validation for the
  identical carrier object. Neither shape-only nor Gate-L2-only success can
  reach registration.
- The 00945c compiler-internal receipt accepts exactly once in the intended
  Root1 or Root6 instruction occurrence. Reuse, copied/rebuilt receipt-shaped
  data, copied runtime maps, different carrier identity, raw wrapper, root,
  plan, argument vector, invocation, owner, scope, thread, instruction, or
  expired full-gate admission rejects without a committed result or output.
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
  exact source-handoff authentication, L2-origin reconstruction, dominating
  full-precertificate reconstruction, and the live one-shot receipt.
- D8's safe-semantics boundary requires the forged or unauthorized edge to be
  rejected with no committed row or output. E5 independently reconstructs the
  exact closed `BAD-ACTUAL` rather than trusting its witness projection.
- D9's evidence charter requires provenance and recomputable proof inputs.
  Gate H binds the exact 21-key input to raw C/T/PRE/classpath sources, Gate L2
  binds owner/edge evidence through the exact E2 carrier, and Gate F requires
  complete independent reconstruction during production and review.
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
  validated 11-key products, two exact 21-key role inputs, and successful
  H/L2/F chain.
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
`sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2` as a
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
  H/L2/F-gated three-argument 00947 admission;
- that a valid seven-key L2 context, by itself, authorizes any lowering
  product, operation, receipt, complete witness, certificate, pair, carrier,
  publication, review result, registration, or runtime receipt;
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
| Caller/ambient owner and edge projections can be compared with themselves | Gate L2 rebuilds owner, roots, 1,480 expected edges, and rejection from an exact seven-key context joined to Gate H |
| Rejected V1 treats the seven-key L2 context as whole-certificate authority | L2 authority is local to L2; separate Gate F independently rebuilds the complete E3 precertificate from the exact authenticated 21-key input |
| Pure one-argument compact validators are described as authority-bearing | Pure one-argument forms are shape-only; only the exact H/L2/F-gated three-argument 00947 APIs carry whole-object authority |
| Root1/Root6 data predicates appear to be the runtime authority source | The already-present private 00945a/00945c single-use receipt conveys the prior full-gated 00947 decision to the exact admitted invocation |
| M response validation wording does not distinguish source authority timing | Ordinals 4/8 complete shape/transport validation; semantic authority follows M's own ordinal-9/10 source session, exact local 11/21-key inputs, and H/L2/F chain |
| Existing-object bytes can be decoded and rehashed | Authoritative existing/reviewer replay first rebuilds exact 11/21-key inputs from C/T, passes H/L2/F, then validates E certificates/carrier through three-argument APIs |
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
5. Gate H validates the exact 11-key source products and finalized 13/13/40/1
   receipt, authenticates every remaining 21-key checker input field locally,
   and requires byte equality for all nine shared source-product fields.
6. The exact seven-key acyclic `AUTHORITY-CONTEXT` contains complete C/T,
   R/Q/RC transactions, M0, M1, bundle core, plan authority, and E2 SH06
   carrier, has no identity, rejects nil or mixed origins, and joins all eight
   shared checker-input coordinates byte-for-byte.
7. Gate L2 freshly replays context-bearing SH06/Q, validates
   RC, independently rebuilds the owner/root selections, 1,480-row pairing and
   expected census, and exact `BAD-ACTUAL` before comparing witness values; it
   is explicitly insufficient for whole-object authority.
8. Gate F independently rebuilds from the exact authenticated 21-key input the
   complete lowering product, seven operations, 34 measurements/traces, seven
   receipts, seven stage witnesses, complete witness, and complete certificate.
   A/B compare complete local candidates byte-for-byte; M/runtime/replay compare
   all externally carried witness/certificate bytes and join the complete local
   lowering product by ID and all downstream preimages without claiming E
   carries that product.
9. Pure one-argument Root1, Root6, and 00947 data validators are explicitly
   shape-only and cannot authorize construction, admission, publication, or
   review.
10. Exact three-argument certificate, pair, carrier, and candidate APIs require
    the 11-key products and one or two exact 21-key inputs; no context-only,
    two-argument, nil, or shape fallback is authoritative.
11. 00947's complete H/L2/F chain dominates runtime registration, while
   the existing private 00945a/00945c one-shot identity receipt safely conveys
   that decision into the exact Root1/Root6 invocation without serializing the
   products, inputs, context, or gate scratch.
12. A and B construct independent products, inputs, contexts, full rebuilds,
   and witnesses with no peer or M input; M later constructs its own products
   and two role inputs, validates A and B separately, and withholds accepted
   output until full-gated pair/carrier validation succeeds.
13. L remains process/publication orchestration only, and both `:new` and
    authoritative `:existing` take the fresh A/B/M path. Independent E-object
    review reconstructs exact products/inputs from C/T and passes H/L2/F;
    `:object-replay-only` remains
    nonauthoritative.
14. The implementation allocation remains exactly 42 owned-C paths, eight
    source-only paths, 86 distinct PRE paths, 91 PRE operations per
    participant, 78 strict static decodes per participant, and the exact
    unchanged aggregate counters printed above. There are zero new code or
    test paths.
15. The complete mutation matrix distinguishes shape, Gate-H source origin,
    Gate-L2 origin, and Gate-F whole-precertificate authority; rejects
    recomputed self-consistent L2 and non-L2 families; proves same-session
    acquisition-free assembly; and proves one-shot receipt consumption and
    full call-graph dominance.
16. No unchanged V9/E1/E2/E3/E4 schema, identity equation, profile bound,
    process-isolation rule, Git topology, publication rule, G9 count, or
    authority ceiling is weakened.
17. The report remains ASCII, owns only its unique architecture report path,
    passes architecture and document validation, and is integrated through a
    separate governance action.
18. I9 remains draft and unfrozen until E5 is integrated, added as a required
    dependency, implemented within the exact allocation, and supported by
    fresh reviewable evidence.
