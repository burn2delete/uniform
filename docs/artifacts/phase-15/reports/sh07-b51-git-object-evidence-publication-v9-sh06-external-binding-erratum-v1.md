# SH-07 B51 Git-Object Publication V9 SH06 External-Binding Erratum V1

Status: draft architecture correction; report only; no implementation authority

## Purpose

This report corrects an implementation impossibility in the independently
accepted and integrated SH-07 B51 Git-object evidence-publication architecture
V9, including its pre-evidence erratum. V9 requires each of the seven B51
fragments to obtain SH06 artifacts that accept authenticated M0/M1
cross-fragment bindings, remain acceptable to a fresh SH06 verifier, and reject
unauthorized cross-fragment edges. At the same time, V9 makes the generic SH06
constructor, verifier, and SH07 authenticated-request consumer read-only and
permits I9 to edit only the existing 27 paths.

The current SH06 request constructor derives imports solely from declarations
in the authenticated SH05 source. The seven fixed B51 fragments contain no
such cross-fragment import declarations. M0 and M1 are available to the 00945
module-transaction builder, but SH06 exposes no lexical input through which
that authority can enter construction and later fresh verification. The
requirements therefore cannot all be satisfied by an authentic I9 candidate.

This correction adds one immutable, explicit, self-contained B51 authority
carrier, one C/T-authenticated SH06 plan binding, and a narrow lexical
SH06/SH07 seam. It preserves the seven B51 Gravity modules, generic load order,
ordinary SH06 behavior, V9 process topology, codec, publication model,
lifecycle, and all authority nonclaims. The governed source session gains the
previously missing `resolution.gravity` input and one exact consumer event.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-sh06-external-binding-erratum-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-pin-free-publication-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-sh06-external-binding-erratum-v1.md",
  "status": "draft",
  "base_commit": "f320269cebe180db18bc849ac1a3b8029153a870",
  "dependencies": [
    {
      "id": "sh07-b51-git-object-evidence-publication-architecture-v9",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-git-object-evidence-publication-v9-pre-evidence-erratum-v1",
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

## Reproduced contradiction

At authoritative main `f320269cebe180db18bc849ac1a3b8029153a870`,
`producer_a.gravity` calls `sh07-b51-success-digest-replay-exact?`, whose sole
definition is in `producer_c.gravity`. The freshly derived M0/M1 authority
contains the unique provider and its authorized caller edge, but the stock
SH06 request for producer A contains no import or import binding for that
symbol. Authentic construction therefore rejects it with
`C5-UNRESOLVED`, stage `:name-resolution`, reason `:no-legal-binding`, and an
empty candidate vector.

A process-local or dynamically rebound `sh06-resolution-request` can make one
construction observe synthesized imports, but it cannot satisfy V9. The
returned artifact ceases to verify when the binding is removed, an identical
concurrent caller can observe authority it did not receive, a fresh process has
no reconstruction input, and `sh07-core-authenticated-request` invokes the
ordinary one-argument verifier again. Mutable Var state, a cache keyed by SH05
identity, caller-supplied accepted artifacts, and source rewrites are all
rejected solutions.

## Exact C5 authority carrier

Define the common complete preimage:

```clojure
C5-AUTHORITY-PREIMAGE =
{:schema :gravity/sh07-b51-c5-authority-preimage-v1
 :content {:commit C :tree T}
 :content-object-proof CONTENT-OBJECT-PROOF
 :source-descriptors (subvec SOURCE-DESCRIPTORS 3 10)
 :module-source-inputs MODULE-SOURCE-INPUTS
 :module-source-descriptors AUTHENTIC-M0
 :link-manifest AUTHENTIC-M1}
```

For module ordinal `i`, I9 constructs `C5-AUTHORITY_i` before SH06
construction from that complete preimage, the exact SH05 artifact, and
`BASE_i`, the unchanged two-argument
`sh06-resolution-request(PATH_i, SH05_i)` result.

`C5-AUTHORITY_i` has exactly:

```clojure
{:schema :gravity/sh07-b51-c5-external-binding-authority-v1
 :schema-version 1
 :content {:commit C :tree T}
 :module-ordinal i
 :source-path PATH_i
 :sh05-artifact-id SH05-ID_i
 :base-request-id H(BASE_i)
 :imports IMPORTS_i
 :import-bindings IMPORT-BINDINGS_i
 :dependency-edges DEPENDENCY-EDGES_i
 :preimage C5-AUTHORITY-PREIMAGE
 :self-id-omits [[:authority-id]]
 :authority-id SHA}
```

`CONTENT-OBJECT-PROOF` has exactly `:oid :raw-bytes`; OID is C and raw bytes is
the complete commit object payload as an unsigned byte vector. The pure
validator requires
`git-sha1("commit " || decimal-byte-count || NUL || raw-bytes) = C`, strictly
parses exactly one leading `tree ` header with 40 lowercase hex value T, and
rejects malformed, duplicate, conflicting, or trailing tree headers.

`SOURCE-OBJECT-PROOF(PATH)` has exactly `:schema :path :root-tree :tree-chain
:blob-bytes`. Schema is
`:gravity/sh07-b51-git-tree-inclusion-proof-v1`, path is the exact safe
repository-relative path, root-tree is T, and blob bytes is the complete leaf
payload as unsigned integers 0 through 255. `:tree-chain` contains one row per
path segment and each row has exactly `:tree-oid :tree-bytes :component
:selected-mode :selected-oid`. The first tree OID is T. Each tree byte vector
is the complete raw tree-object payload and must satisfy
`git-sha1("tree " || decimal-byte-count || NUL || tree-bytes) = tree-oid`.
A bounded strict Git-tree parser reads the canonical sequence
`MODE || SP || NAME || NUL || 20-BYTE-OID`, rejects malformed UTF-8 names,
duplicate names, noncanonical Git tree ordering, symlink/submodule modes, and
requires exactly one entry matching `component`. For every nonfinal segment,
selected mode is the raw tree mode `"40000"` and selected OID equals the next
row's tree OID. The final mode is `"100644"`; its selected OID equals the
descriptor blob OID and
`git-sha1("blob " || decimal-byte-count || NUL || blob-bytes)`.
Descriptor path, mode, blob OID, byte count, and raw SHA must equal the proven
leaf and independent SHA-256 of the bytes.

`MODULE-SOURCE-INPUTS` is the seven exact two-key maps
`{:descriptor SOURCE-DESCRIPTOR
:source-object-proof SOURCE-OBJECT-PROOF(PATH)}` for source-session ordinals 3
through 9 in module order. They contain no path handle, decoder, callback, or
worktree reference. The pure validator strictly UTF-8-decodes each proven
blob, reruns `read-forms`, `parse-module`, function-table,
M0 definition/external-binding derivation, complete callsite census, and M1
sealing, and requires the results to equal `AUTHENTIC-M0` and `AUTHENTIC-M1`.
A self-consistent M0/M1 mutation with recomputed IDs therefore rejects against
the unchanged C/T bytes.

`AUTHENTIC-M0` is the complete seven-row vector and `AUTHENTIC-M1` is the
complete 1,480-edge manifest, not IDs, summaries, acceptance booleans,
callbacks, or mutable lookups. The seven source descriptors bind M0 paths,
ordinals, modes, blob OIDs, byte counts, and raw hashes back to C/T.
`authority-id` is
`H([:gravity/sh07-b51-c5-external-binding-authority-v1
AUTHORITY-WITHOUT-ID])`.

`C5-AUTHORITY-PREIMAGE` has exactly `:schema :content :content-object-proof
:source-descriptors :module-source-inputs :module-source-descriptors
:link-manifest`. Content has
exactly `:commit :tree`. Source descriptors are the complete seven exact
semantic-source descriptor rows at ordinals 3 through 9, in ordinal order;
module-source-descriptors and link-manifest retain their complete V9 M0/M1
closed schemas. `BASE-REQUEST-ID_i` is exactly
`H([:gravity/sh07-b51-sh06-base-request-v1 BASE_i])`; no untagged digest is
accepted.

The derived `IMPORTS_i` vector is grouped by ascending callee module ordinal.
Each row has exactly:

```clojure
#{:namespace :alias :kind :profile :targets :visibility :refer :effects
  :capabilities :safety :boundary :semantic-span :source-span
  :dependency-artifact-id :foreign-record-complete?}
```

Alias is nil, kind is `:namespace`, visibility is `:public`, targets is the
one-element callee target vector, refer is the selected symbols in the
caller's M0 external-binding order, effects and capabilities are sorted callee
vectors, and foreign-record-complete is true. Semantic span has exactly
`:manifest-id :caller-module-source-id :callee-module-source-id
:authorized-edge-ordinals`; source span is exactly
`{:source CALLER-LOGICAL-SOURCE-PATH}`. Dependency artifact ID is
`H([:gravity/sh07-b51-authenticated-sh06-import-v1 COMPLETE-GROUP-JOIN])`,
where the join contains the manifest ID, caller and callee source IDs, and for
each referred symbol its complete M0 external-binding row plus exact ordered
M1 edge ordinals.

`IMPORT-BINDINGS_i` follows the caller's M0 external-binding row order. Each
row has exactly:

```clojure
#{:name :kind :namespace :package :binding-class :visibility :profile-set
  :target-set :type-ref :effects :capabilities :safety :semantic-span
  :source-span :definition-syntax-id :definition-artifact-id}
```

Kind is `:function`, binding class is `:import`, visibility is `:public`, type
ref is `:gravity.type/function`, and the profile/target sets are one-element
callee vectors. Semantic span is the exact ten-key coordinate
`:manifest-id :caller-module-source-id :callee-module-source-id
:callee-module :callee-source-sha256 :callee-definition-source-hash :symbol
:arity :owner-edge :authorized-edge-ordinals`. Source span has exactly
`:source :external-binding`. Definition syntax ID is
`H([:gravity/sh07-b51-authenticated-sh06-binding-v1 COORDINATE])`; definition
artifact ID is the callee module source ID.

`DEPENDENCY-EDGES_i` follows `IMPORTS_i` order. Each row has exactly
`:from :to :kind :boundary :semantic-span :source-span`; it projects caller
module, import namespace, `:namespace`, and the exact boundary/span values of
the corresponding import. Missing, extra, reordered, or duplicate rows in any
of the three vectors reject.

The carrier validator is a pure C/T-carrier validator: it performs no file,
Var, cache, registry, or process lookup. It independently reruns the complete
existing 00945 M0/M1 shape, source, definition-hash, arity, owner-edge,
authorization, manifest-ID, edge-coverage, and uniqueness checks. It selects
only edges whose caller
source ID equals M0[i], groups providers by callee module ordinal, and derives
`IMPORTS_i`, `IMPORT-BINDINGS_i`, and `DEPENDENCY-EDGES_i` exactly. Those three
values must equal the carrier byte-for-byte. No caller-supplied projection is
trusted.

Every external symbol has exactly one M0 provider definition and one provider
module, one M0 external-binding row, and one or more exact M1 callsite edges.
The caller M0 external-binding row plus each M1 row's
`:authenticated-b51-namespace-external` authorization is the explicit B51
visibility/export grant; a provider definition's ordinary module-private
visibility alone neither grants nor defeats that cross-fragment authority.
No symbol lacking this complete grant may be promoted to a public SH06 import.
All selected edges have authorization
`:authenticated-b51-namespace-external`, the exact permitted owner edge, exact
caller/provider source IDs and function source hashes, and matching arity.
The selected edge ordinals are unique and their set equals the complete M1
caller-edge set. A missing or extra symbol, provider, definition, callsite,
edge ordinal, import, import binding, dependency edge, or a mixed C/T, SH05,
M0, or M1 value rejects with `C5-UNRESOLVED`; there is no partial projection or
fallback.

The applied request is exactly `BASE_i` with the three derived vectors appended
to `:imports`, `:import-bindings`, and `:dependency-edges`. Existing names in
BASE_i may not intersect the derived external symbol set. All other BASE_i
fields remain byte-equal.

## Lexical SH06 and SH07 seam

The generic two-argument `sh06-resolution-request`, one-argument
`sh06-resolution-artifact-verification`, ordinary candidate/finalizer paths,
and one-argument `sh07-core-authenticated-request` retain their current
behavior and identities for every ordinary artifact.

I9 adds strict context-bearing arities through the existing construction and
verification chain. Every existing arity delegates to its current behavior;
it must not manufacture a context. The exact added arities and lexical calls
are:

```clojure
;; 00873
sh06-resolution-c5-authority-request!(PATH, SH05, BASE, CONTEXT) ; arity 4
sh06-resolution-request(PATH, SH05, CONTEXT)                 ; arity 3

;; 00879
sh06-resolution-context-plan-binding!(PATH, CONTEXT)         ; arity 2
sh06-resolution-artifact-verification-bounded*(R, BUILD?, CONTEXT) ; arity 3

;; 00880
sh06-resolution-context-validation(R, CONTEXT)               ; arity 2
sh06-resolution-context-root(CONTEXT)                        ; arity 1
sh06-resolution-artifact-verification*(R, BUILD?, CONTEXT)   ; arity 3
sh06-resolution-artifact-verification-contained(R, BUILD?, CONTEXT) ; arity 3
sh06-resolution-artifact-verification(R, CONTEXT)            ; arity 2
sh06-resolution-capability-based-proof-for-construction(R, CONTEXT) ; arity 2
sh06-authenticated-source-snapshot!(PATH, C2, PHASE, LEGACY?, CONTEXT) ; arity 5
sh06-source-snapshot-stable!(PATH, BEFORE, C2, LEGACY?, CONTEXT) ; arity 5
sh06-resolution-source-artifact-candidate(PATH, SH05, LEGACY?, CONTEXT) ; arity 4

;; 00881
sh06-resolution-finalize-candidate(PATH, CANDIDATE, CONTEXT) ; arity 3
sh06-resolution-artifact-from-sh05-artifact(PATH, SH05, CONTEXT) ; arity 3

;; 00890
sh07-core-authenticated-request(R, CONTEXT)                  ; arity 2

;; 00945
p15-s23-sh07-b51-current-sh06-resolution-binding(ENTRY, EMITTER) ; arity 2
p15-s23-sh07-b51-c5-external-binding-authority(i, SOURCE, SH05, BASE, PREIMAGE) ; arity 5
p15-s23-sh07-b51-sh06-context(C5-AUTHORITY, PLAN-AUTHORITY)  ; arity 2
p15-s23-sh07-b51-build-module-transaction(C, T, i, SOURCE,
                                           C5-PREIMAGE,
                                           PLAN-AUTHORITY)    ; arity 6
```

`SH06-CONTEXT_i` has exactly `:external-binding-authority` and
`:resolution-plan-authority`. The first value is `C5-AUTHORITY_i`. The second
is the following complete plain-EDN carrier:

```clojure
{:schema :gravity/sh07-b51-sh06-resolution-plan-authority-v1
 :content {:commit C :tree T}
 :content-object-proof CONTENT-OBJECT-PROOF
 :source-descriptor SOURCE-DESCRIPTORS[12]
 :source-object-proof SOURCE-OBJECT-PROOF(
   "bootstrap/gravity/src/gravity/resolution.gravity")
 :plan-binding FULL-SH06-BINDING-WITHOUT-SOURCE-TEXT
 :self-id-omits [[:authority-id]]
 :authority-id SHA}
```

The source event constructs the full current SH06 executable binding from the
governed `resolution.gravity` bytes. `plan-binding` retains the executable
canonical plan, source byte count/hash, plan/function identities, public
function hashes/shapes, and execution-boundary fields required by SH06, but
omits decoded source text. Its source-object proof carries the raw bytes and
complete T inclusion chain needed for later pure verification. Both object
proofs validate under the exact rules above before any plan identity is
trusted. `authority-id` is exactly
`H([:gravity/sh07-b51-sh06-resolution-plan-authority-v1
PLAN-AUTHORITY-WITHOUT-AUTHORITY-ID])`. Descriptor path, byte count, and raw hash,
all plan identities, source identity, and existing SH06 pins must agree. A
hash-only plan reference is not sufficient.

`SH06-CONTEXT_i` has exactly the two keys above; it contains no callback or
function. Its external authority source path, SH05 ID, base-request ID, and
C/T must match the call arguments and plan authority C/T. Both complete
authority values and their self-IDs are revalidated at every public boundary.
Every strict arity rejects nil as a missing context; nil never selects the
legacy implementation. Only invocation of the separately named legacy arity
selects context-free behavior.

The exact construction flow is:

```text
00945 build-receipted-bundle-core-products(C,T)
  -> governed source event 39 -> RESOLUTION-PLAN-AUTHORITY
  -> p15-s23-sh07-b51-build-module-transaction(C,T,i,SOURCE_i,
                                                C5-PREIMAGE,
                                                RESOLUTION-PLAN-AUTHORITY)
  -> derive tagged BASE-ID, C5-AUTHORITY_i, and SH06-CONTEXT_i
  -> sh06-resolution-artifact-from-sh05-artifact(PATH_i,SH05_i,CONTEXT_i)
  -> sh06-resolution-source-artifact-candidate(PATH_i,SH05_i,false,CONTEXT_i)
  -> sh06-authenticated-source-snapshot!(PATH_i,C2_i,PHASE,false,CONTEXT_i)
  -> sh06-resolution-request(PATH_i,SH05_i,CONTEXT_i)
  -> sh06-resolution-finalize-candidate(PATH_i,CANDIDATE_i,CONTEXT_i)
  -> sh06-resolution-capability-based-proof-for-construction(CANDIDATE_i,CONTEXT_i)
  -> sh06-resolution-artifact-verification-contained(CANDIDATE_i,true,CONTEXT_i)
  -> sh06-resolution-artifact-verification*(CANDIDATE_i,true,CONTEXT_i)
  -> sh06-resolution-artifact-verification-bounded*(CANDIDATE_i,true,CONTEXT_i)
  -> sh06-resolution-request(PATH_i,SH05_i,CONTEXT_i)
```

The exact later fresh-verification/Q flow is:

```text
sh07-core-authenticated-request(R_i,CONTEXT_i)
  -> sh06-resolution-artifact-verification(R_i,CONTEXT_i)
  -> sh06-resolution-artifact-verification-contained(R_i,false,CONTEXT_i)
  -> sh06-resolution-artifact-verification*(R_i,false,CONTEXT_i)
  -> sh06-resolution-artifact-verification-bounded*(R_i,false,CONTEXT_i)
  -> sh06-resolution-request(PATH_i,SH05_i,CONTEXT_i)
  -> existing SH07 Q projection
```

The context-bearing bounded verifier takes `PLAN-BINDING` solely from the
lexical plan authority, reruns `sh06-resolution-run-request!`, and compares
every current stored product exactly as the legacy verifier does. It must not
call `sh06-resolution-current-binding!`. File 00874 and all its functions
remain byte-identical and read-only; they are reachable only from legacy
context-free paths. No dynamic Var, global root mutation, process cache,
registry, function-valued carrier, or hidden default is permitted.

The 00873 and 00879 strict helpers implement the pure validation locally; they
do not dynamically resolve or call later-loaded 00945 Vars. The 00945 arity-5
builder and the 00873 validator are two implementations of the same closed
equations and their outputs must agree byte-for-byte. The 00879 plan helper
validates and returns the exact 18-key binding from the complete lexical plan
authority. Unchanged `sh06-resolution-run-request!` and
`sh06-resolution-execute!` receive that binding explicitly. Public capability
proof, source/file artifact, serialization/deserialization, and contained
failure helpers retain their ordinary arities and do not accept B51 authority.

The context-bearing source-snapshot helper likewise performs no live source
read. It joins PATH and the authenticated SH05 C2 source-unit path,
byte-count, and bytes-hash to the caller source descriptor inside the complete
C5 preimage. It returns exactly `{:canonical-path PATH :byte-count COUNT
:bytes-hash SHA :immutable-git-object? true}`. The contextual stability helper
requires byte equality of that complete immutable projection before and after
construction. The context-free four-argument snapshot and stability helpers
retain their current bounded live-file behavior for ordinary SH06. Thus a B51
transaction remains valid with every worktree read primitive replaced by a
throwing sentinel, not only the `resolution.gravity` reader.

Before the strict two-argument public verifier calls
`sh07-proof-transaction-report`, `sh06-resolution-context-validation` performs
the complete raw-module-byte/M0/M1/C5, plan-binding, C/T, SH05, request, and
self-ID validation above without consulting a proof cache. Only a complete
passing result permits computation of:

```clojure
SH06-CONTEXT-ROOT_i =
H([:gravity/sh07-b51-sh06-context-root-v1 SH06-CONTEXT_i])
```

The strict verifier's proof-transaction verifier epoch is the unchanged legacy
epoch map extended with exactly `:sh06-context-root`,
`:resolution-plan-authority-id`, and `:external-binding-authority-id`. The
root is over the complete validated context, not a vector of claimed IDs. A
wrong context therefore cannot reuse an artifact-only proof receipt. The
contextual verification checks add exactly the Boolean keys
`:resolution-plan-authority-current?` and
`:external-binding-authority-current?`; each is true only when the complete
carrier validation passed and its recomputed ID equals the epoch field. Digest
strings themselves never appear as truthy check values. The legacy one-argument
verifier epoch and check keyset remain byte-identical. The new context-root
domain is added to both E2 identity registries and every dependent verifier
receipt/identity is regenerated.

The three-argument request constructor first recomputes BASE from PATH and
SH05, then fully validates the complete authority preimage and returns the
exact appended request.
The authority-bearing verifier independently recomputes that same request and
requires it to equal
`R[:gravity-resolution-boundary :authenticated-resolution-request]`
byte-for-byte before accepting any stored run product or proof. It uses the
lexical plan binding and must not call `sh06-resolution-current-binding!` or
read a live source file. The strict
SH07 consumer first invokes that verifier with the same authority and only then
projects Q. Passing nil, the wrong module authority, a hash-only carrier, a
different but self-consistent projection, or a carrier not revalidated from
complete M0/M1 fails closed.

For a B51 artifact the ordinary one-argument verifier and one-argument SH07
consumer are not authority-bearing alternatives and must reject the augmented
request rather than silently reconstruct BASE. Every V9 equation that
previously used `sh06-resolution-artifact-verification(R_i)` or
`sh07-core-authenticated-request(R_i)` is replaced by the corresponding strict
call with freshly rederived `C5-AUTHORITY_i`.

00945 constructs one immutable authority carrier per module and one governed
plan authority, passes the exact context through candidate construction, final
proof, fresh verification, and Q projection, then discards only the per-call
context wrapper. Later V9 validation rederives the equal C5 carrier from the
complete M0/M1 and reuses the complete immutable plan-authority product; it
never loads authority from a returned SH06 artifact, ambient process, live
source path, or mutable cache. `MODULE-TRANSACTIONS` retains its existing exact
keyset.

I9 defines the V9-required
`p15-s23-sh07-b51-build-receipted-bundle-core-products(C,T)` in 00945 as the
object-mode wrapper around the existing execution-bundle builder. Its product
map changes from eight to nine exact return keys by adding
`:sh06-resolution-plan-authority`. Source
event 39 produces that value. The value contains no source text, reader,
handle, function, absolute path, checked-out path, or mutable cache. It remains
available after the private source session finalizes, so the external
classifier and independent replay can perform genuinely fresh contextual
SH06 verification.

The strict verification report binds the exact Boolean checks
`:resolution-plan-authority-current?` and
`:external-binding-authority-current?`; the two exact recomputed authority IDs
remain verifier-epoch metadata, not check values.
Let `C5-AUTHORITIES` be the seven complete `C5-AUTHORITY_i` maps in module
ordinal order. `LINK-AUTHORITY-ID-E2` is exactly:

```clojure
H([:gravity/sh07-b51-sh06-link-authority-id-e2-v1
   {:resolution-artifact-ids SH06-RESOLUTION-ARTIFACT-IDS
    :semantic-projection-ids SH06-SEMANTIC-PROJECTION-IDS
    :event-product-ids (mapv :product-id (subvec SOURCE-EVENTS-E2 17 24))
    :manifest-id AUTHENTIC-M1[:manifest-id]
    :resolution-plan-authority RESOLUTION-PLAN-AUTHORITY
    :external-binding-authorities C5-AUTHORITIES}])
```

`SH06-AUTHORITY-CARRIER-BASE-E2` is the V9 base with
`:resolution-plan-authority RESOLUTION-PLAN-AUTHORITY`,
`:external-binding-authorities C5-AUTHORITIES`, and
`:link-authority-id LINK-AUTHORITY-ID-E2`. Its exact keyset is:

```clojure
#{:content :source-descriptors :bundle-core-id
  :resolution-artifact-paths :resolution-artifacts
  :resolution-verifications :semantic-projection-paths
  :semantic-projection-ids :module-source-descriptors :link-manifest
  :link-authority-event-product-ids :link-authority-id
  :expected-edge-census :resolution-plan-authority
  :external-binding-authorities}
```

The final carrier adds only `:carrier-id`, computed as
`H([:gravity/sh07-b51-sh06-authority-carrier-id-e2-v1
SH06-AUTHORITY-CARRIER-BASE-E2])`. Both new E2 domains replace their V9
counterparts in the tag and H-domain registries. Carrier validation requires
the complete plan authority and all seven complete C5 authorities to revalidate
and join byte-for-byte to C/T, source descriptors, M0/M1, artifacts,
verifications, and Q projections. IDs without their complete carriers reject.

Parallel or nested constructions carry disjoint lexical values. Two equal
inputs must produce byte-equal requests and artifacts; two different inputs
cannot affect one another. Tests must run overlapping ordinary and B51 SH06
calls and prove identical results to serial execution.

## Corrected ownership and PRE counts

The following six paths move into I9 ownership:

```text
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00873-sh06-resolution-dependency-edges.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00879-sh06-resolution-artifact-verification-bounded.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00880-sh06-resolution-artifact-verification.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00881-sh06-resolution-finalize-candidate.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00890-sh07-b13-module-assembly-manifest.clj
bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_sh06_external_binding_seam_test.clj
```

No Gravity source, load-order file, central runner, generic governance file, or
other SH06/SH07 path becomes writable. `resolution.gravity` remains read-only
and is acquired only from the governed C/T source session. The I9 allowed set and
`OWNED-C-PATHS` are the unsigned-UTF-8-sorted union of the existing 27 paths
and these six paths, for exactly 33 paths.

`OWNED-C-PATHS-E2` is exactly:

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
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
 "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn"
 "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn"
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
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj"]
```

`I9-LIFECYCLE-PATHS-E2` is exactly the 33 strings in
`OWNED-C-PATHS-E2`, followed in order by:

```clojure
["docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-receipt.edn"
 "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-discharge.edn"
 {:kind :content-addressed-terminal-record
  :directory "contracts/workstream-records/terminal"
  :workstream-id "sh07-b51-git-object-evidence-publication-implementation-v9"
  :record-digest-domain :sha256
  :filename-rule :workstream-id-hyphen-record-sha256-dot-json}]
```

`READ-ONLY-PATHS-E2` is exactly V9/E1 `READ-ONLY-PATHS` with the five SH06
source paths above removed. The new test was not previously present and is not
a member of `READ-ONLY-PATHS`. No other member is removed.

`V9-OWNERSHIP-PLAN-E2` replaces the accepted E1 plan, not the superseded V9
plan, and is exactly:

```clojure
{:schema :gravity/sh07-b51-v9-ownership-plan-e2-v1
 :allocations [[0 :a9 :master-coordinator A9-PATHS-E1]
               [1 :i9 :master-coordinator I9-LIFECYCLE-PATHS-E2]
               [2 :e9 :master-coordinator E9-PATHS]
               [3 :g9 :master-coordinator G9-LIFECYCLE-PATHS]
               [4 :p9 :master-coordinator P9-LIFECYCLE-PATHS]]
 :semantic-review-owner :sh-core
 :reservation-required-stages [:i9 :e9 :g9 :p9]
 :handoff-required-stages [:i9 :g9 :p9]
 :read-only-prerequisite-stages [:a9]
 :read-only-paths READ-ONLY-PATHS-E2
 :open-exit-gates [:core :evaluation :arity :mutation :recursion
                   :exception :pattern]
 :aggregate-sh07-state :partial
 :self-id-omits [[:plan-id]]
 :plan-id PLAN-ID-E2}
```

The plan has exactly `:schema :allocations :semantic-review-owner
:reservation-required-stages :handoff-required-stages
:read-only-prerequisite-stages :read-only-paths :open-exit-gates
:aggregate-sh07-state :self-id-omits :plan-id`. Each allocation tuple expands
to the accepted E1 exact four-key map with `:ordinal :stage :owner :paths`.
Allocations 0, 2, 3, and 4 and every stage-scope value remain byte-equal to E1.
Allocation 1 retains ordinal 1, stage `:i9`, owner `:master-coordinator`, and
has exactly `I9-LIFECYCLE-PATHS-E2`. `PLAN-ID-E2` is
`H([:gravity/sh07-b51-v9-ownership-plan-e2-v1 PLAN-E2-WITHOUT-PLAN-ID])`.
This E2 schema and digest replace the E1 ownership-plan schema and digest in
every downstream carrier, root, fixture, check, receipt, and certificate.
The E1 ownership-plan tag and H domain are removed from both registries and
`:gravity/sh07-b51-v9-ownership-plan-e2-v1` is added to both.

The six owned paths and the new read-only source input were not members of
V9's prior distinct PRE union. Therefore the corrected distinct path union is
79, the PRE Git operation count is 84, the PRE strict static-decode count is
69, and the owned-C artifact count is 33.
All exact schemas, participant eligibility checks, fixtures, counters,
contract rows, roots, and IDs that carried the former values 72, 77, 63, and
27 are replaced by 79, 84, 69, and 33 respectively. The five Q Git operations,
33 document descriptors, two PRE-masked files, three A9 artifacts, and one
baseline BOOT5 value remain unchanged.

`SOURCE-ONLY-PATHS-E2` replaces the V9/E1 nine-path vector and is exactly:

```clojure
["bootstrap/gravity/p15_s23/compiler.gravity"
 "bootstrap/gravity/p15_s23/emitter.gravity"
 "bootstrap/gravity/src/gravity/resolution.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"]
```

These ten source-only paths remain raw descriptor inputs in PRE. They are not
members of the 69 PRE strict-static-decode set. The later private semantic
source session performs one strict UTF-8 decode for each of its 13 acquired
entries, including `resolution.gravity`; this is the source-session decode
counter, not a PRE static decode.

`SOURCE-SPECS-E2` is the complete V9 source-spec vector with this exact final
row appended:

```clojure
{:ordinal 12 :kind :sh06-resolution :module-ordinal nil
 :path "bootstrap/gravity/src/gravity/resolution.gravity"}
```

Every E2 source acquisition first obtains and validates the exact raw commit
object and path tree chain under the proof grammar above, then executes the
existing bounded source-session sequence: exact `ls-tree -z C -- PATH`,
`cat-file blob OID`, a post-acquisition C/T assertion, and strict UTF-8
decoding. The two acquisition routes must agree on path, mode, blob OID, byte
count, raw SHA, C, and T. The private session entry gains exactly
`:content-object-proof` and `:source-object-proof` in addition to its existing
`:descriptor :bytes :text`; no proof is loaded from the worktree. Commit/tree
object reads occur inside the same bounded acquisition and do not add a source
descriptor or semantic event. Their parsing work is added to the existing
source-acquisition fuel subject; the number and ordering of fuel subjects does
not change. Because the path is read-only, descriptor 12 at every eligible C/T
is:

```clojure
{:ordinal 12
 :kind :sh06-resolution
 :module-ordinal nil
 :path "bootstrap/gravity/src/gravity/resolution.gravity"
 :mode "100644"
 :blob-oid "16ef2b638298a8e934fcd0b39131072305fbcfd5"
 :byte-count 77209
 :raw-sha256
 "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56"}
```

`p15-s23-sh07-b51-current-sh06-resolution-binding`, added only in the already
owned 00945 path, has arity two and receives the exact private source-session
entry `{:descriptor DESCRIPTOR-12 :bytes BYTES :text TEXT
:content-object-proof CONTENT-OBJECT-PROOF
:source-object-proof SOURCE-OBJECT-PROOF}` plus the exact
authenticated emitter already produced by source event 24 and retained in the
same lexical source session. It requires the entry's exact five-key shape,
`BYTES` to be the acquired 77,209-byte vector, `TEXT` to be its unique strict
UTF-8 decode, and the descriptor byte count and raw SHA to equal independent
recomputation from `BYTES`. The emitter must be identical to the checked
event-24 product. It does not call any 00874 reader/cache or obtain another
emitter from host state.

The function runs the same pinned plan construction algorithm as legacy
`sh06-resolution-build-binding!`, but from `DESCRIPTOR-12[:path]`, `TEXT`, and
the governed event-24 emitter: it calls
`p15-s23-stage2-compiler-artifact-plan`, recomputes
`sh06-resolution-plan-identities`, requires every current SH06 source,
semantic, function-count, function-name, function-shape, public-function hash,
and public-function-shape pin, reconstructs the authenticated plan ID, and
returns `PLAN-BINDING`. `PLAN-BINDING` has exactly:

```clojure
#{:source-path :source-byte-count :source-content-hash
  :plan-semantic-hash :functions-semantic-hash :function-count
  :function-names-hash :function-shapes-hash
  :public-function-hashes :public-function-shapes
  :artifact :status :semantic-authority :compiled-by :executed-by
  :generic-bridge-residual? :self-hosted? :plan}
```

Its values are exactly those produced by the current legacy builder from the
same bytes, except it never constructs or retains `:source-text` and its
`:source-path` is the repository-relative descriptor path. `:artifact` is
`:gravity/sh06-pinned-resolution-plan-binding`, status is `:complete`,
semantic authority is `:gravity-source`, compiled-by is
`:clojure-stage0-seed`, executed-by is
`:clojure-stage2-generic-rule-runner`, generic-bridge-residual is true,
self-hosted is false, and `:plan` is the complete authenticated executable
plan. Thus the binding is executable data, not a function, cache handle, or
hash-only reference.

The function wraps that binding in the exact eight-key resolution-plan authority
defined above, verifies its self-ID, and returns the authority as event product
39. `SOURCE-EVENT-PLAN-E2` is the complete V9 vector with exactly:

```clojure
{:ordinal 39 :domain :sh06-resolution-binding :source-ordinal 12
 :module-ordinal nil
 :consumer
 gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}
```

The actual event row has exactly the five coordinate keys above plus
`:source-raw-sha256` equal to descriptor 12's raw SHA and `:product-id` equal
to:

```clojure
H([:gravity/sh07-b51-source-event-product-id-v1
   {:ordinal 39
    :domain :sh06-resolution-binding
    :consumer
    gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding
    :source-raw-sha256 DESCRIPTOR-12[:raw-sha256]
    :product RESOLUTION-PLAN-AUTHORITY}])
```

`SOURCE-DESCRIPTORS-ROOT-E2` remains
`H([:gravity/sh07-b51-source-descriptors-root-v1 SOURCE-DESCRIPTORS-E2])` and
`SOURCE-EVENT-ROOT-E2` remains
`H([:gravity/sh07-b51-source-event-root-v1 SOURCE-EVENTS-E2])`, over the now
13-row and 40-row complete vectors. No event ID or root is copied from V9.

Source descriptors change from 12 to 13, source events from 39 to 40, and the
session counters from 12/12/39/1 to 13/13/40/1. The additional source is
prefetched, object-checked, strictly decoded, compiled, and consumed exactly
once. Every subsequent module construction and verification retains the
lexically returned plain-EDN authority product without another source event or
physical read.

Every source-session precheck, postcheck, object check, materialized-byte
check, source-descriptor projection, BOOT2 projection, M counter, A/B child
response, coordinator receipt, evidence/runtime equality check, and final
review literal that was exactly 12 descriptors or 39 events is exactly 13 or
40 under E2. Each A, B, and M session is independently 13/13/40/1.
The aggregate runtime `SOURCE` value is exactly:

```clojure
{:sessions [{:participant :a :acquire-count 13 :decode-count 13
             :event-count 40 :finalize-count 1}
            {:participant :b :acquire-count 13 :decode-count 13
             :event-count 40 :finalize-count 1}
            {:participant :m :acquire-count 13 :decode-count 13
             :event-count 40 :finalize-count 1}]
 :acquire-count 39
 :decode-count 39
 :event-count 120
 :finalize-count 3}
```

`ELIGIBILITY-COUNTERS-E2` retains the accepted E1 key names and is exactly:

```clojure
{:participants [:l :m :a :b]
 :q-coordinate-git-operation-count 20
 :pre-discharge-git-operation-count 336
 :git-operation-count 356
 :q-coordinate-decode-count 4
 :receipt-id-check-count 4
 :pre-discharge-reconstruction-count 4
 :pre-discharge-static-decode-count 276
 :pre-discharge-document-descriptor-count 132
 :pre-discharge-source-descriptor-count 52
 :pre-discharge-pre-masked-file-count 8
 :pre-discharge-owned-c-artifact-count 132
 :pre-discharge-a9-artifact-count 12
 :pre-discharge-baseline-boot5-count 4
 :content-match-count 4
 :source-descriptors-match-count 4
 :source-descriptors-root-match-count 4
 :pre-masked-root-match-count 4
 :owned-c-root-match-count 4
 :pre-discharge-match-count 4}
```

The equations are `4*5=20`, `4*84=336`, and `20+336=356`. Each participant's
PRE reconstruction is the exact 84-row plan; no old 77-row child plan remains.

The G9 source-object observation plan changes from 47 to 49 dense rows and no
other operation changes: ordinals 0 through 19 remain V9; ordinals 20 through
32 are the 13 C source `ls-tree` rows in `SOURCE-SPECS-E2` order; ordinals 33
through 45 are their corresponding `cat-file blob` rows; and ordinals 46, 47,
and 48 are the independent rev-C, rev-E, and C-to-E ancestry postchecks.
`GIT-COUNTERS-E2` is exactly
`{:operation-count 49 :stdin-byte-count 0 :stdout-byte-count N
:stderr-byte-count 0}`, where N is the sum of all 49 recorded stdout byte
counts. The post-G9 lifecycle observation receipt remains its separate
unchanged 24-operation plan.

The six newly owned implementation/test paths change C/T; adding the existing
read-only resolution source to the authenticated projection changes the source
descriptor/event roots. Together these change OWNED-C-ROOT, PRE discharge,
eligibility receipt, participant checks, runtime discharge, fixtures, semantic
algorithm products, and all dependent IDs. I9 must rederive them; no old
literal is retained as authority. PRE-MASKED-ROOT
and RUNTIME-MASKED-ROOT still govern only the two existing pin-bearing
bootstrap files and retain their E1 split.

## Required tests and rejection evidence

In addition to every unchanged V9 and E1 test, I9 must prove:

- ordinary SH06 construction, verification, and SH07 projection are
  byte-identical without a C5 authority carrier;
- all seven B51 requests, artifacts, fresh verifications, and Q projections
  succeed only with their exact rederived carrier;
- the producer-A to producer-C call resolves to the authentic provider;
- missing, extra, duplicated, reordered, mixed-C/T, stale-source,
  stale-definition, wrong-arity, wrong-owner, unauthorized, wrong-caller, and
  wrong-provider M0/M1 mutations reject before a Q value is returned;
- a fabricated import/import-binding/dependency-edge projection rejects even
  when its self-ID is recomputed;
- a self-consistent mutated M0/M1/source-descriptor family with every derived
  ID recomputed rejects against the complete C/T module byte vectors;
- wrong commit bytes, tree bytes, path component, tree ordering, mode, object
  ID, blob bytes, or C/T in either module or resolution inclusion proof rejects
  before plan or M0/M1 authority is accepted;
- one-argument verification of an augmented B51 artifact rejects;
- a valid artifact presented with the wrong module context cannot reuse a
  proof-transaction receipt and rejects before Q projection;
- the strict path succeeds when every live source read primitive is replaced
  by a throwing sentinel, proving it uses the governed plan binding;
- serial, nested, and overlapping ordinary/B51 calls are deterministic and
  cannot observe another call's authority; and
- complete in-memory PRE-to-runtime and later lifecycle replay uses the
  corrected 33/79/84/69 and 13/13/40/1 values and rederived identities.

The independent implementation review must execute an authentic source-derived
seven-module transaction. A generation-only tail stub, synthetic accepted
artifact, ambient binding, cached prior request, or focused schema test cannot
substitute for that result.

## Normative supersession map

This report makes the following exhaustive substitutions. Symbolic references
to a replaced value use the E2 value even where V9 or E1 repeated the old
literal in a schema, equation, fixture, counter, root, or acceptance paragraph.

| V9/E1 authority | Exact E2 replacement |
| --- | --- |
| `OWNED-C-PATHS`, 27 owned-C descriptors, and the I9 allocation paths | `OWNED-C-PATHS-E2`, 33 owned-C descriptors, and `I9-LIFECYCLE-PATHS-E2` |
| `SOURCE-ONLY-PATHS` with nine rows | `SOURCE-ONLY-PATHS-E2` with ten rows |
| `PRE-C-PATHS` union of 72 | The unsigned-UTF-8 distinct union of the unchanged 33 document paths, ten E2 source-only paths, 33 E2 owned-C paths, and the unchanged three E1 A9-only paths: exactly 79 |
| 77 PRE operations | 84 dense rows: show C; one 79-path C `ls-tree -z`; 79 C `cat-file blob` rows; show base; one base BOOT5 `ls-tree`; one base BOOT5 `cat-file` |
| 63 PRE strict static decodes | 69: unchanged 33 documents, 33 E2 owned-C blobs, and unchanged three E1 A9 artifacts; all ten source-only blobs remain raw in PRE |
| `SOURCE-SPECS`, 12 descriptors, descriptor root | `SOURCE-SPECS-E2`, 13 descriptors including exact descriptor 12, and `SOURCE-DESCRIPTORS-ROOT-E2` |
| private source-session entry with descriptor/bytes/text only | Exact five-key E2 entry adding C commit-object proof and per-path tree-inclusion proof; all source acquisitions validate both proof and existing ls-tree/cat-file routes |
| `SOURCE-EVENT-PLAN`, 39 events, event root | `SOURCE-EVENT-PLAN-E2`, 40 events including exact event 39, and `SOURCE-EVENT-ROOT-E2` |
| source counters 12/12/39/1 | 13/13/40/1 for acquire/decode/event/finalize |
| aggregate A/B/M `SOURCE` 36/36/117/3 | Exact E2 aggregate 39/39/120/3 above |
| source-read receipt vectors and accepted/rejected fixture source vectors | Complete 13-descriptor/40-event E2 vectors and regenerated roots/IDs; rejected fixtures add missing, extra, reordered, stale, malformed, and wrong-product mutations for row 12/event 39 |
| V9/E1 semantic-source carrier and C2 source snapshot | Add exact descriptor 12 and the governed plan-authority product; no worktree source or live 00874 binding is admitted |
| 27-path `OWNED-C-ROOT` and all PRE/eligibility participant checks | Regenerate from 33 exact descriptors; participant checks remain five Q operations followed by 84 PRE operations |
| V9/E1 `ELIGIBILITY-COUNTERS` values based on four 77-row reconstructions | Exact `ELIGIBILITY-COUNTERS-E2` above: 20 Q, 336 PRE, 356 total, 276 static decodes, 132 documents, 52 source descriptors, 8 pre-masked files, 132 owned-C artifacts, 12 A9 artifacts, 4 baseline values, and all six match counts equal 4 |
| G9 47-row observation plan and `GIT-COUNTERS` | Exact 49-row E2 plan and `GIT-COUNTERS-E2`; the external post-G9 24-row plan is unchanged |
| E1 `V9-OWNERSHIP-PLAN-E1`, its tag/domain, and unchanged read-only set | Exact `V9-OWNERSHIP-PLAN-E2`, E2 tag/domain, E2 I9 allocation, and `READ-ONLY-PATHS-E2` |
| one-argument SH06 verification and SH07 authenticated-request equations for B51 transactions | Exact context-bearing construction, construction-proof, fresh-verification, and Q flow above; legacy equations remain only for ordinary artifacts |
| SH06 proof-transaction verifier epoch keyed only by artifact/legacy verifier facts | Strict B51 epoch adds the complete validated context root and both recomputed authority IDs; legacy epoch is unchanged |
| SH06 authority carrier without external/plan authority | Add complete resolution plan authority and the ordered seven complete C5 authorities under the exact E2 carrier keyset; every contract binding and identity is regenerated |
| `LINK-AUTHORITY-ID` input | Add the complete resolution plan authority and complete ordered seven C5 authority carriers, not merely acceptance booleans or unjoined IDs |
| fixed-fuel semantic algorithm/source roots and fuel-subject inputs that include I9/source closure | Regenerate over the six newly owned paths, descriptor 12, event 39, E2 ownership plan, and changed carrier algorithms; operation topology and fuel bounds do not change |
| every downstream PRE/runtime/post-I9/post-G9/post-P9 discharge, phase root, receipt, certificate, evidence identity, fixture ID, and final pin dependent on a replaced value | Regenerate transitively; no V9/E1 literal dependent on an old value remains authority |
| any statement that `R_i` alone reconstructs M0/M1-authorized bindings | `R_i` plus freshly rederived `SH06-CONTEXT_i` is required; an augmented `R_i` fails closed without it |
| any M0/M1 validation based only on self-hashed semantic rows and source descriptors | Pure validation rederives M0/M1 from the seven complete C/T-authenticated module byte vectors before accepting any carrier or context root |

Every other V9 requirement and every E1 correction remains byte-for-byte
normative. In particular, this report does not change the seven Gravity
modules, public roots, owner selection, exact 1,480-row M1 census,
unauthorized-cross-fragment equation,
process isolation, evidence publication, lifecycle ordering, mask split, or
identity-cycle rejection.

## Implementation and lifecycle consequence

The current draft I9 candidate must not freeze while this correction is absent
or merely draft. After this report is independently accepted and integrated,
I9 must reconcile onto that authoritative main, add this workstream as an
integrated dependency, expand its exact owned-path set, implement only the
lexical seam above, and regenerate every affected count, fixture, root, and ID.

This report grants architecture authority only. It does not establish I9
implementation correctness, evidence eligibility, SH-07 completion,
self-hosting, hostile-host proof, release, performance authority, or seed
retirement.

## Acceptance criteria

This correction is acceptable only if independent review confirms:

1. the stock SH06 request cannot derive the authentic B51 external bindings;
2. the previous owned-path set cannot implement durable fresh verification;
3. the carrier includes and revalidates complete M0/M1 preimages;
4. construction, proof, fresh verification, and Q projection receive the same
   authority lexically with no ambient state;
5. ordinary one-argument behavior remains unchanged and augmented artifacts
   fail closed without authority;
6. exactly six new owned paths, one read-only source input, and the
   33/79/84/69, 13/13/40/1, aggregate 39/39/120/3, eligibility 356,
   and G9 49-operation count cascades are complete;
7. no unchanged V9/E1 invariant is weakened; and
8. all authority nonclaims remain explicit.
