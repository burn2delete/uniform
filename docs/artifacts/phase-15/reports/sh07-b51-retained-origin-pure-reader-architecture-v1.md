# Gate H Retained-Origin and Pure-Reader Replay Architecture V1

## Purpose and authority

This architecture closes the missing-input contract identified by independent
Gate H HOLD review on main `4d45fe4cb1b5c91ac3b2c25e5f67539e3e44b0a8`, tree
`702c7c0c0195a8d19a6c05b1002faaf38d19c42c`. The external diagnosis has SHA-256
`9f66b4064a00bb3c665c5f57a59882db893c50f51aa700a5f860e7969c9e81b9`.
The human authorized architecture preparation and independent review on
2026-10-14. This candidate changes no production source, runtime schema,
allocation constant, source counter, pin or accepted terminal record.

```json gravity-architecture-authority-v1
{"schema":"gravity/architecture-authority-v1","workstream_id":"sh07-b51-retained-origin-pure-reader-architecture-v1","invariant_family":"architecture/self-hosting-sh07-b51-source-origin-v1","report_path":"docs/artifacts/phase-15/reports/sh07-b51-retained-origin-pure-reader-architecture-v1.md","status":"draft","base_commit":"4d45fe4cb1b5c91ac3b2c25e5f67539e3e44b0a8","dependencies":[{"id":"sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2","required_state":"integrated"},{"id":"sh07-b51-source-session-handoff-closure-v1","required_state":"integrated"},{"id":"sh07-b51-v9-c6-source-core-map-erratum-v1","required_state":"integrated"},{"id":"c6-source-core-mapping-prerequisite-v1","required_state":"integrated"},{"id":"baseline-loc-contract-repair-v1","required_state":"integrated"}],"historical_references":[],"authority":{"integration_only":true,"release":false,"self_hosting":false,"seed_retirement":false}}
```

D0/D1/D2/D3/D6/D8/D9, L1, C2-C6 and BOOT1-BOOT8 govern this amendment.
The integrated V9 architecture, V2 authority-context erratum, source-fragment
binding amendment and C6 mapping erratum remain normative except for the
explicit Gate H input and PRE/allocation changes below. The original V9
architecture-report digest is unchanged. This report is an additional exact
governing input, never a replacement digest or retrospective acceptance.

Architecture acceptance, integration and implementation authorization are
separate. This preparation/review grant authorizes neither merge nor source
implementation. Parent I9 remains draft. A future implementation child requires
this architecture to be independently accepted and separately integrated,
then an explicit implementation grant and exact path reservation.

## Missing contract and chosen interface

The accepted handoff retains complete proofs/bytes for source ordinals 3-9
through C5 and ordinal 12 through resolution-plan authority. It does not retain
complete origin for 0/1/2/10/11. Direct C2 units 2/10/11 therefore cannot be
replayed from its return. PRE discards compiler/emitter raw bytes, while the
classpath source authority covers only `bootstrap/clojure/src`. Canonical C2
currently rereads `reader.gravity`; its executable plan is omitted from the
returned plan-binding. Rehashed supplied vectors cannot fill these gaps.

The amendment defines H6, a separately versioned in-memory handoff. It adds
exactly `:source-origin` to both source products and local checker input:

```clojure
SOURCE-PRODUCTS-H6-KEYS =
#{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
  :module-transactions :sh06-resolution-plan-authority :sh06-authority-carrier
  :source-semantic-carrier :source-read-receipt :source-origin}

CHECKER-INPUT-H6-KEYS =
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :predecessor-executable-descriptor-id :module-source-identities
  :module-source-byte-counts :emitter-source-id :emitter-semantic-id
  :m0s :m1 :m2s :bundle-core :source-forms :module-transactions
  :sh06-resolution-plan-authority :sh06-authority-carrier
  :source-semantic-carrier :semantic-algorithm-source-descriptors
  :semantic-algorithm-source-root :checker :provenance :source-origin}
```

These are exact 12/22-key types; no optional fields exist. The current 11/21
builders/consumers remain legacy bounded source-handoff support and cannot
confer Gate H admission. They are not widened implicitly. H6 has its own
private construction and validation route; a legacy input rejects at H6 shape
admission rather than being completed by a directory, delay or prior receipt.
V2's "one retained in-memory field" specifically meant the existing SH06
carrier. It did not authorize origin retention or expansion of that E2 carrier.
The E2 carrier remains exactly 16 keys, transactions 11, certificates 19 and
certificate inputs 13. No origin field enters a witness, certificate, response,
E9 payload, runtime receipt or publication tuple.

H6's logical gate has two arguments `(SOURCE-PRODUCTS-H6, CHECKER-INPUT-H6)`.
It executes only inside the private, declared three-input admission operation
`VALIDATE-H6(EXPECTED-ADMISSION, SOURCE-PRODUCTS-H6, CHECKER-INPUT-H6)`.
EXPECTED-ADMISSION is a lexical immutable input to this operation, not a
dynamic Var or recovered source channel. The two-argument gate does not escape
the operation. No closure, callback, token or success value is returned for
reuse. This explicit enclosing input anchors C/T and role independently of the
two untrusted maps; a generic two-map shape predicate has no authority.

EXPECTED-ADMISSION has exactly these eleven keys:

```clojure
#{:content :role :architecture-report-sha256 :amendment-descriptor
  :checker-identity :classpath-receipt :pre-source-descriptors
  :reader-provenance :envelope-provenance :load-binding :request}
```

It is constructed once by the authenticated A/B/M or independent-replay
entrypoint from its own validated request, C/T PRE, complete classpath and
current process identity before source finalization. These remain the exact
V9/E5 request, checker-identity and four-entry classpath types; no caller
context, success flag or verification report may stand in for their inputs.
The operation's caller must be the owning authenticated entrypoint. Public
APIs cannot install EXPECTED-ADMISSION. Fresh independent replay reconstructs
its own expected admission before opening its source session; it never copies
another role's expected admission or an old acceptance report.

Content has exactly `:commit :tree`, both 40 lowercase hex Git IDs, and is the
validated request content. EXPECTED-ADMISSION role has exactly
`:source-session-role :checker-role`. Checker role is exactly
`:producer-census` or `:independent-verifier-census`. A/B source-session role
equals its checker role. M source-session role is exactly `:m`; M performs one
local H6 check for each checker role using its own same M origin, without
loading either checker or claiming A/B execution. Independent replay has its
own A/B entrypoint/session and cannot reuse M or peer origin. L has no semantic
source session and does not construct an H6 origin. These relations come from
actual entrypoint/selected static checker identity, not an origin role claim.
The aliases in the counter model are exactly l=the launcher participant (which
has no source-session role), m=:m,
a=:producer-census and b=:independent-verifier-census. For every local check,
expected checker-identity's `:role` equals expected checker-role, while input
`:checker` equals that manifest's exact six-key `:checker` value. Algorithm-role
is `:source-first-exhaustive` for:producer-census and
`:m2-first-independent-exhaustive` for:independent-verifier-census, exactly the
authenticated checker-specs mapping. Input provenance retains the existing V9
schema/domain for that selected checker role, rederived through the existing
checker-provenance equation from expected request/identity: invocation uses
role-name:producer/:independent; tool closure uses the corresponding
producer-census/independent-census materializer-tool-closure-id-v2 domain and
checker role. Its content and algorithm source identities equal the
independently captured expected identities. For M this describes
the statically selected checker identity, while process/load/source-session
evidence remains role:m; it never impersonates an A/B process or loads its
namespace. No nonexistent checker `:role` field is added.
Pre-source-descriptors are the existing exact 13-vector.
Amendment-descriptor has exactly `:path :mode :blob-oid :byte-count
:raw-sha256`, selecting this report at C/T and joining its integrated governing
record. The owning pre-final entrypoint rehashes the retained amendment bytes
and validates the integrated architecture/governance dependency against actual
C/T PRE object observations before constructing this descriptor. This
governing-policy anchor is distinct from the subject source-origin proofs;
H6 cannot accept a descriptor manufactured by its data caller.
Reader-provenance has exactly `:logical-path :supplied-path :byte-count
:raw-sha256 :workspace-root :invocation-root`. It records the authenticated
pre-final reader source-contract materialization and explicit process-path
provenance. These paths must match the role's materialized C/T and observed
provenance before finalization. They are provenance only, never source
authority or arguments to a post-final physical read or environment lookup.
Reader descriptor path equals reader-provenance logical-path; reader supplied
path equals reader-provenance supplied-path; descriptor/proof count and raw
SHA-256 equal reader-provenance byte-count/raw-sha256. Unit source-provenance
is separately exactly `:source-path :workspace-root :invocation-root`: its
source-path is that unit's descriptor path, and both roots equal the captured
reader-provenance roots. It cannot use the reader's logical path as unit path.
Envelope-provenance has the same six-key type, with literal logical path
`bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity`.
Its supplied path/count/hash join the independently observed pre-final
envelope materialization and proved Git blob. Its workspace/invocation roots
equal reader-provenance roots; neither dependency path replaces a unit path.

LOAD-BINDING is exactly `:source-closure :loaded-namespaces :self-process`.
These are the existing closed V2 eight-key source-closure, seven-key
loaded-namespaces and V9 six-key self-process records, with their existing
roots/identity equations and explicit fragment/source records; no new domain
or serialized field is introduced. Checker `06-execute-request` must capture
and validate its fresh source/fragment identity manifest and loaded namespace
observation after loading the selected authenticated checker but BEFORE the
H6 builder opens/finalizes its source session. M's existing source-closure and
loaded-namespace observation owners must do the same with role:m and their
existing prohibition on A/B loaded namespaces. The self-process is the actual
owning process, not a caller-supplied PID record. Every project source/fragment
record is joined to the retained complete Clojure bytes, loaded owner/load
order and expected checker identity under V2; external records join retained
JARs and the independent classpath receipt. Source-closure and loaded record
roles and self-process role equal source-session-role; selected
checker-identity role equals checker-role under the mapping above.
The pre-final entrypoint authenticates this TCB binding and hands the immutable
record lexically to VALIDATE-H6. H6 validates its closed types, roots and byte
joins without rediscovering live namespaces or trusting a success receipt.
Until this capture/verification/handoff exists, implementation must HOLD.

## Closed retained-origin type

SOURCE-ORIGIN is an immutable data map with exactly seven keys:

```clojure
#{:version :content :role :content-object-proof :source-entries
  :reader-input :implementation-inputs}
```

Version is exactly 1. `origin[:content] = expected[:content]` equals both maps'
C/T and the finalized receipt's C/T. `origin[:role]` equals
`expected[:role :source-session-role]` and the receipt's role, while checker
identity/provenance separately join `expected[:role :checker-role]`.
Both product/input origin values must be canonically equal, and every shared
field must join before expensive replay. The shared field vector is exactly
the existing nine V2 fields followed by `:source-origin`. Each origin copy is
shape/bound checked and joined to expected admission; replay is performed
once from the equal raw input, never from transaction/artifact projections.
This is a private input type, not an artifact/receipt.
It has no artifact tag, semantic ID, acceptance bit, verification report,
omission list or self-hash. All nested values are persistent plain data;
mutable `byte[]`, records, lazy/infinite sequences, objects, functions, handles,
atoms and delays reject. Every non-nil host metadata map, including an empty
map, rejects recursively on both H6 input maps and EXPECTED-ADMISSION; metadata
is not an ignored channel. Named semantic metadata fields remain governed
data. If a legitimate existing product requires host metadata, implementation
must HOLD for an explicit contract resolution rather than strip it silently.

CONTENT-OBJECT-PROOF is the existing exact `#{:oid :raw-bytes}` type. Raw bytes
are a persistent vector of integers 0-255. Recompute Git's exact typed/length
commit OID and parse the commit tree header; require its OID/selected tree to
equal expected C/T. No mutable ref, directory identity or supplied Boolean
authenticates this proof.

SOURCE-ENTRIES is exactly 13 rows in ordinal order 0-12. Each row has exactly
`:descriptor :source-object-proof :text`. Descriptor is the existing eight-key
type `#{:ordinal :kind :module-ordinal :path :mode :blob-oid :byte-count
:raw-sha256}`. Every row equals the literal V2 source spec and the matching
receipt/PRE descriptor. The fixed path/kind/module roles are:

| Ordinal | Role | Path |
| --- | --- | --- |
| 0 | L1 contract | `docs/phase-01-core-language/011-l1-surface-syntax-specification.md` |
| 1 | L2 contract | `docs/phase-01-core-language/012-l2-core-language-semantics.md` |
| 2 | checked core | `bootstrap/gravity/src/gravity/checked_core.gravity` |
| 3-6 | modules 0-3 | `bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` through `producer_d.gravity`, in letter order |
| 7-8 | modules 4-5 | `bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity`, `root6_b.gravity` |
| 9 | module 6 | `bootstrap/gravity/src/gravity/sh07_b51/root8.gravity` |
| 10 | compiler | `bootstrap/gravity/p15_s23/compiler.gravity` |
| 11 | emitter | `bootstrap/gravity/p15_s23/emitter.gravity` |
| 12 | resolution | `bootstrap/gravity/src/gravity/resolution.gravity` |

SOURCE-OBJECT-PROOF has exactly `:schema :path :root-tree :tree-chain
:blob-bytes`. Schema is the existing
`:gravity/sh07-b51-git-tree-inclusion-proof-v1`. Each chain row has exactly
`:tree-oid :tree-bytes :component :selected-mode :selected-oid`. Recompute
every Git tree OID and select exactly one component from the raw tree bytes.
Require dense root-to-leaf order, no cycle/reused link, exact parent/child OID
joins, directory mode `40000` and leaf `100644`. Blob bytes are the sole raw
source payload: recompute Git blob OID, exact count and raw SHA-256. Reject
unsafe/traversing/absolute paths, symlinks, missing/extra links or components.
Strict UTF-8 decode must equal retained text; strict reencoding equals bytes.
Text must contain Unicode scalars without replacement or unpaired surrogates.

READER-INPUT has exactly `:descriptor :source-object-proof :supplied-path`.
Descriptor is the five-key amendment-descriptor type above, with literal path
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`. The complete proof
uses the same C/T and raw byte rules. Supplied path equals reader-provenance;
its count/hash equal both the proof and that independent pre-final observation.
This dependency is outside the 13 source-session entries and has no source
ordinal. It cannot replace ordinal 2, 10 or 11 or become a fourteenth session
entry. Decode reader text freshly from its authenticated blob when deriving
the reader plan; no separate text claim is accepted.

IMPLEMENTATION-INPUTS has exactly `:clojure-parent-tree :source-objects
:external-classpath :load-order-path :envelope-input`. Envelope-input has the
same three-key type as READER-INPUT; descriptor path is the fixed SH02 path
above, proof authenticates its raw bytes at C/T, supplied path/count/hash join
expected envelope-provenance. It is outside the 13 semantic entries. From
source-entry2's proved gravity tree select its compiler directory, authenticate
the additional raw compiler tree, then select authenticated_envelope.gravity.
Shared ancestor tree OIDs/raw bytes equal the corresponding proved objects;
selected-component fields at the shared gravity tree differ. This is a full
inclusion proof, not an independent source/path claim.
Load-order-path is literally
`bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn`, subject to the
actual source-controlled load resource identity check; no alternate manifest
or caller-selected resource is permitted.
Clojure-parent-tree has exactly `:tree-oid :tree-bytes`, the raw
`bootstrap/clojure` tree whose OID is selected by the already proved bootstrap
ancestor. Select the root-tree row in source-entry2's proof and require its
`bootstrap` child OID/mode to join the next proved row. From that proved raw
bootstrap tree select exactly one `clojure` entry with mode40000; its OID must
equal Clojure-parent-tree's recomputed Git tree OID. From this proved parent
select exactly one `src` directory OID; require the complete reconstruction
below to equal that OID. No standalone parent tree or ls-tree claim suffices.
For the reader, use source-entry2's proved root/bootstrap/gravity/src/gravity
rows, require reader-input's chain to reproduce the shared tree OIDs and raw
tree bytes byte-for-byte (the last shared row selects a different component), then
select `bootstrap` and join the additional proved raw reader-parent tree and
its `reader.gravity` leaf. This is a complete root-to-reader proof, not a
descriptor-only dependency. SOURCE-OBJECTS is the complete UTF-8-path-sorted classpath source
vector below `bootstrap/clojure/src`, each row exactly `:descriptor :bytes`,
descriptor the five-key type above and bytes persistent unsigned integers.
No path is optional; completeness is checked cryptographically as follows.

Reconstruct the entire `bootstrap/clojure/src` Git subtree bottom-up from all
source paths, modes and recomputed blob OIDs. Use exact Git tree ordering
(directory names compare as if suffixed with `/`), raw UTF-8 names, NUL records
and 20-byte child OIDs. Reject duplicate/colliding file/directory names,
nonregular leaves and unsafe paths. The derived subtree OID must equal the
`src` entry of the proved Clojure-parent tree. Missing, extra or coherently
rehashed dependencies therefore cannot reproduce the expected root. Recompute
the existing classpath manifest/root from the same rows and compare with the
independent four-entry classpath receipt. Literal require/load order and all
fragment owner records are rederived under source-fragment binding V2; no
provided closure list or namespace identity substitutes for actual bytes.

EXTERNAL-CLASSPATH is exactly three rows in V9 classpath ordinals 1/2/3. Each
row has exactly `:descriptor :bytes`; descriptor has exactly `:ordinal :kind
:logical-coordinate :byte-count :sha256 :provided-namespaces`. Kind is `:jar`;
coordinate has exactly `:artifact :version` and equals the V9 fixed external
classpath specification at that ordinal. Rehash complete retained JAR bytes;
compare count/hash/coordinate/namespace inventory with the independently
authenticated pre-final classpath receipt. Decode the declared dependency
source records from those bytes in memory using V9's exact source-record types
(four-key namespace/resource/count/hash, or namespace/host-provided), with
duplicate/shadow and byte/entry/expanded-byte bounds. No resource lookup,
filesystem reopen or accepting prior parsed dependency records is allowed.
The JVM/JAR execution substrate remains explicit TCB, not hostile-host proof.

The checker's existing `03-git-operation-core.clj` and coordinator's
`04-pre-manifest-tcb.clj` JAR acquisition, checker
`04-validate-complete-classpath.clj` / M
`05-validate-complete-classpath.clj` owners must retain the actual raw JAR and
Clojure bytes before finalization. The H6-only private preflight return adds
`:origin-classpath-inputs`, exactly `:source-objects :external-classpath`, using
the persistent rows above; existing serialized classpath receipts/source
records are unchanged. Memory ZIP parsing replaces physical JarFile/resource
lookup only in H6 replay. The loaded Clojure/Stage2 helpers and JAR substrate
are matched to these authenticated bytes by the role's existing fresh
pre-final load identity and fragment checks; raw source rows do not authorize
arbitrary ambient loaded functions. H6 rehashes/reconstructs the retained
closure and compares those exact anchored identities. Host classpath fields
canonical-path/file-key remain pre-final provenance, not semantic authority.
These private inputs never cross IPC: V9's 4MiB request and 1GiB response limits
are unchanged. Strip SOURCE-ORIGIN before every existing response/emission.

## Producer, retention and pure replay

The future private H6 builder runs alongside, rather than silently modifying,
the legacy builder. It receives explicit independently authenticated PRE/TCB
inputs from its owning entrypoint, opens the existing private C/T source
session, and consumes exactly the existing 40 events. It constructs all module
transactions, source carrier and complete SH06 carrier using that same
session. Before the sole finalization it copies the 13 immutable entry
descriptors/proofs/text into SOURCE-ORIGIN, joins reader/PRE/TCB inputs and
constructs the complete 12-key return. The local 22-key input is built from that
return and its own expected admission. Neither map contains a session handle.

The existing 13 acquisitions/13 initial UTF-8 decodes/40 events/one finalization
remain the source-session counters. Retained UTF-8 revalidation and independent
in-memory replay are explicitly additional bounded checks, not additional
source acquisitions or a second semantic source session. They do not mint or
alter the finalized source receipt. A/B/M each run their own session and
retained-origin reconstruction. Independent replay also does so from the
declared immutable object inputs; a previously accepted report is never input.

Pure reader replay derives executable plans; SOURCE-ORIGIN contains no supplied
reader/emitter/compiler plan. The exact future private operations are:

1. Derive the compiler's `p15-s23-stage2-plan-emitter` value/rule from ordinal10
   bytes through the existing strict, non-evaluating source parser.
2. Derive the emitter/compiler artifact contract from ordinal11 bytes and
   ordinal10 rules, checking their source/semantic identities against the
   expected literals in the authenticated C/T helper closure, not a second
   table introduced in this report.
3. Parse proved `reader.gravity`, select the existing `sh03-` function closure,
   and compile it with those freshly derived rules through the authenticated
   stage0/Stage2 helper TCB. No source constructor resolves a physical path.
4. Apply the current static instruction audit and exact SH03 plan validator.
   Recompute source hash/count, plan ID, function/name/shape/builtin hashes,
   entrypoint/verifier hashes and instruction summary. Pins in authenticated
   C/T helper bytes constrain the result; they cannot replace source derivation.
5. Independently derive the SH02 authenticated-envelope plan from proved
   envelope bytes and the fresh compiler10 plan-emitter rule. Use the existing
   pure compile-source-binding constructor in00750-003 with its exact locally
   constructed request (`:source-text :emitter-rule :inputs`), explicit source
   path/count/hash and emitter target:jvm/source identity/rule hash. Require all
   existing SH02 source, plan, functions, builder, verifier, count and required
   function-shape pins from the authenticated C/T helper bytes. No physical
   source-binding, cache or authority token is accepted as a replay input.
6. Invoke `sh03-read-source-unit` and `sh03-verify-reader-result` with explicit
   source unit, retained source-byte vector and standard reader policy. Resolve
   the actual declared digest requests, adapt C2 products and construct C2's
   complete artifact through the common owning artifact constructor. Its SH02
   envelope is built and freshly verified using the locally derived envelope
   binding, existing bounded template execution/digest resolution and exact
   owning descriptor. Internal existing lexical SH02 authority may guard the
   authenticated helper TCB execution; it never authenticates caller inputs or
   escapes into origin/API parameters. It cannot authorize source reopening.

The future private pure API contract is exact; none accepts a reader, callback,
cache, success flag or user-selected semantic implementation:
The table names internal operations and exact lexical call signatures, not
mapped/public admission APIs. Fresh-binding arguments are locally constructed
within VALIDATE-H6 and its captured owning helpers; external callers can
supply only the declared raw admission/product/input maps. In particular the
SH02 binding/constructor/validator are private lexical helpers in018, captured
by the owning pure adapter/validator route. They derive binding from proved
bytes internally; no mapped/public caller can install a binding or token.

| Operation | Exact arguments | Owning existing physical helper |
| --- | --- | --- |
| `compiler-plan-emitter-from-origin` | `(compiler-entry, target)` | `backends/00644` |
| `emitter-artifact-binding-from-origin` | `(emitter-entry, compiler-entry, implementation-inputs)` | already-owned00945 route reuses unchanged pure00581 source constructor |
| `sh03-build-plan-from-origin` | `(reader-input, compiler-entry, emitter-entry, implementation-inputs, reader-provenance)` | `self-hosting-sh03-sh07/00809` |
| `sh03-binding-from-origin` | `(reader-input, compiler-entry, emitter-entry, implementation-inputs, reader-provenance)` | `self-hosting-sh03-sh07/00811` |
| `sh02-binding-from-origin` | `(envelope-input, compiler-entry, implementation-inputs, envelope-provenance)` | `bootstrap-p15/00750-018`, reuses unchanged pure003 constructor |
| `sh02-envelope-from-origin` | `(descriptor)` | `bootstrap-p15/00750-018`, reuses unchanged bounded009 constructor |
| `sh02-envelope-from-origin-verify!` | `(artifact, descriptor)` | `bootstrap-p15/00750-018` |
| `sh03-resolved-result-from-origin` | `(source-entry, project-context, reader-policy, source-provenance)` | `self-hosting-sh03-sh07/00813` |
| `sh03-descriptor-from-provenance` | `(source-path, project-context, resolved, summary, source-provenance)` | `self-hosting-sh03-sh07/00816` |
| `sh03-adapt-products-from-origin` | `(source-path, source-text, source-bytes, reader-options, project-context, resolved, source-provenance)` | `semantic_late/146156-sh03-reader-adapt-install` |
| `sh03-adapt-boundary-from-origin` | `(state, source-provenance)` | `semantic_late/146156-sh03-reader-adapt-boundary` |
| `sh03-products-from-origin-verify!` | `(source-entry, project-context, source-provenance, products)` | `self-hosting-sh03-sh07/00828` |
| `c2-products-from-origin` | `(source-entry, project-context, reader-policy, source-provenance)` | canonical adapter in `compiler/00819` |
| `c2-artifact-from-origin` | `(source-entry, project-context, source-provenance)` | `compiler/00829` common artifact constructor |
| `replay-l1-carrier` | `(fresh-c2-artifacts, source-entry0, source-entry1)` | already-owned `00945-16` |

Fresh-binding is a transient locally derived plain value produced once inside
this validation by sh03-binding-from-origin; fresh-envelope-binding is likewise
derived once by sh02-binding-from-origin. These exact two values are private
lexical let bindings in the admission operation, captured only by its owning
internal helpers; they are not caller arguments in the signatures above.
SH02's stage/artifact-kind are fixed:c2-reader/:gravity/sh03-reader-products;
its implementation source path equals envelope-provenance supplied-path.
Stage descriptor source-path remains the unit's declared source-provenance
source-path, which its descriptor/diagnostic path must equal. These are distinct
values with distinct purposes, neither obtained through path resolution.
Neither binding is an origin field or
accepted caller binding. Source-entry is selected only from the validated
origin ordinal; source-provenance is derived from explicit unit path/project
context and pre-final process provenance. Every byte-consuming operation
validates the strict text/byte round trip before use and consumes the retained
bytes directly. The exact call order is emitter derivation, reader-plan/binding
derivation, input-unit construction, raw reader and verifier execution, digest
resolution, owning adapter, common C2 constructor, full-artifact comparison,
then PN/MR/L1 projection. There is no invocation of legacy current-binding,
00808/00645 physical readers or 00828's legacy precomputed sentinel path.
00828 is future-owned only to factor its owning product-validation relation
into a pure retained-input helper used by the canonical constructor; the
legacy token does not authorize H6. `00819`'s Clojure oracle remains
differential-only; only its SH03-backed canonical adapter participates here.
Compiler-entry and emitter-entry are validated SOURCE-ENTRY rows10/11, not
text-only claims; target is exactly:jvm and reader-policy is the exact existing
`sh03-reader-input-policy(standard-reader-options)` value. The unchanged pure
`sh03-reader-input-source-unit(source-path, source-bytes, project-context)` in
00812 consumes explicit inputs; it requires no allocation or mutation. The
provenance constructor replaces 00816's ambient user.dir reads. The existing
semantic install/boundary fragments factor their common adapter construction
to thread that explicit record into it. The unchanged 00817 evidence adapter
and other existing semantic steps remain pure, reused and read-only. State
is the unchanged intermediate map constructed locally by the common adapter,
never an origin/admission input. Legacy callers retain their current wrapper;
H6 uses the explicit seven-argument adapter and two-argument boundary, with
the freshly derived envelope binding captured lexically. Factor
the existing boundary symbol with an additional pure arity; install captures
that same stage function lexically before the unchanged cleanup unmaps it.
No new mapped semantic-stage helper Var or load-order entry is introduced.
The products validator
uses this locally derived fresh-binding and fresh reader/verifier/digest data;
it accepts no Object sentinel or supplied verification success as authority.
Any temporary byte arrays/ZIP streams exist only within bounded conversion,
hashing or execution, copy results into immutable data, close resources and
never escape into origin/admission/returned products as retained authority.
Standard-reader-options is the fixed authenticated C/T options value; only its
derived policy enters the Gravity reader, while the canonical adapter/common
C2 body uses those same options. Neither is caller-selected. Every pure source
constructor derives one strict text from its retained bytes and validates the
round trip before use. It bypasses legacy00819's .getBytes route and all
00828/00829 fallback/precomputed arities. Emitter11 contract derivation uses
the existing pure `p15-s23-stage2-compiler-artifact-binding-from-source!`
constructor in00581 with the freshly derived compiler10 emitter rule,
target:jvm and the fixed emitter logical path/text; it never calls the physical
compiler-artifact-binding! wrapper or its source-path resolver.
All owning003/00581/004/009/00644 parser, verifier and diagnostic request paths
are fixed safe logical paths selected by authenticated dependency/unit role.
Supplied physical paths are retained provenance data only: no diagnostic,
parser, path resolver or filesystem operation consumes them. The pure plan
constructors parse at the fixed logical dependency path, then set the existing
returned binding's source-path provenance field from the independently anchored
supplied-path as data; no artifact field is erased or hash formula changed.
SH02 source-rule is path-neutral under its existing00749 equation. Outer H6
failures use only the fixed logical diagnostic coordinates defined below.

The pin projection is the literal values of the authenticated C/T 00808
symbols `sh03-reader-expected-source-byte-count`,
`sh03-reader-expected-source-content-hash`,
`sh03-reader-expected-plan-semantic-hash`,
`sh03-reader-expected-functions-semantic-hash`,
`sh03-reader-expected-function-count`,
`sh03-reader-expected-function-names-hash`,
`sh03-reader-expected-function-shapes-hash`,
`sh03-reader-expected-entrypoint-semantic-hash`,
`sh03-reader-expected-verifier-semantic-hash` and
`sh03-reader-expected-builtin-functions-hash`, together with its declared
reader relative-path/entrypoint/verifier/function-prefix and bounds literals.
The owning 00809/00811 validators rederive the instruction summary and compare
all these pin projections using their existing equations. Literal extraction
is bounded and non-evaluating from the complete C/T source closure; arbitrary
reader code cannot change the expected pin table or raise its bounds. Legacy
host functions are callable only after the explicit pre-final load-binding
joins their loaded sources/fragments to those exact authenticated bytes.
SH02's readonly00748 projection is exactly its source-relative-path,
source-byte-count, expected-source-content-hash, expected-plan-semantic-hash,
expected-functions-semantic-hash, expected-builder-semantic-hash,
expected-verifier-semantic-hash, expected-function-count, builder-function,
verifier-function and required-functions symbols, all with prefix
`p15-s23-sh02-`. The pure003 constructor's existing comparisons and exact
derived binding/plan-ID equations apply without replacing literals. H6 applies
its static plan-carrier/instruction audit before template execution, and the
existing SH02 bounds/result/request-graph/verifier grammar independently
bound both construction and verification. Stage and artifact-kind are fixed
to:c2-reader and:gravity/sh03-reader-products for this route. Internal018
construction uses only its own freshly derived binding; callers cannot
directly install a binding into the admission operation. Boundary and00828
invoke only the pure018 constructor/validator, never their legacy wrappers.
Before invoking the unchanged pure003 constructor,018 rehashes the actual
retained envelope byte vector, strict-decodes and re-encodes it, and builds
count/hash/source-text fields from those same bytes. No independent text or
metadata claim passes through. All seed source parsing/plan construction in
H6 executes with `*authenticated-source-form-records*` explicitly nil: the
legacy authenticated-record branch canonicalizes paths and is forbidden.
The named nil binding is input sanitization, not injected authority. Additional
bootstrap-target and plan-emission contexts must also be absent; no dynamic
source records, target additions, cache or user-selected operation override
can affect this route. If another ambient branch is needed, implementation
must HOLD rather than substitute a weaker parser.
The owning admission initializes `*macro-expansion-ops-context*` and
`gravity.macro-expansion/*normalized-ops-context*` to nil;
`*additional-bootstrap-targets*` to#{},
`*p15-s23-stage2-plan-emission-context*`,
`*p15-s23-artifact-build-context*`,
`*p15-s23-stage2-compiler-artifact-binding*`,
`*p15-s23-sh07-b51-governed-source-session*`,
`*sh07-proof-transaction-context*` and its cleanup observer to nil.
The five C2 leaf namespaces gravity.c2-source-identity,
gravity.c2-reader-diagnostics, gravity.c2-reader-product-projection,
gravity.c2-lexical-validation and gravity.c2-artifact-identity enter with
their `*operations*` equal{}, and active/bypass operation-key sets equal#{}
where declared. Their bootstrap C2 identity/diagnostic/projection/lexical
leaf-call flags enter false; any internal recursion guard may be established
only by the authenticated owning wrapper, never by caller input. Existing
canonical bounds bind only to the authenticated fixed SH03/SH02 values. These
fixed guards use already-owned admission code and readonly leaf APIs/Vars;
no new ownership of the leaf modules is authorized. Sentinels cover each named
context and every actual reachable dynamic branch. If fixed contexts cannot
be installed without editing an unowned helper, implementation must HOLD.
Seed parsing also binds clojure.core/*read-eval* false,
clojure.core/*data-readers*{} and clojure.core/*default-data-reader-fn* nil.
Only authenticated fixed builtin reader tags/options are allowed; ambient
data-reader mappings or default handlers never participate. Any internal
macro/C2 operation map is reconstructed solely by the existing authenticated
owning wrapper with its exact fixed functions/constants, not caller callbacks.
Before Stage2 invocation require the exact compiler-artifact plan context:
module gravity.compiler.authenticated-envelope for SH02 (the declared SH03
module for reader), profile:meta, target:jvm, safety:safe, empty effects and
capabilities, exact pinned plan/functions/instructions/builtin set, no external
links and no runtime-artifact/runtime-context/atom channel. The fixed runtime
contains only the existing compiler bridge engine/compiler-artifact marker.
The owning pre-final entrypoint must realize and validate the three immutable
00587 pin-table/table-valid/index delays against authenticated source literals
before session finalization. Post-final execution may read those already
realized immutable TCB tables through the unchanged context predicate, but
may not execute a delay thunk or obtain a source-derived cached plan. Sentinel
evidence distinguishes this literal-table read from source/cache acquisition.
An executable `realized?` guard for all three delays runs before every H6
Stage2 context check; false rejects before dereference. The pre-final owner
also validates their realized immutable values against retained authenticated
literal pins. No delayed thunk or supplied table can substitute for that check.

The new00644 operation is a distinct retained-byte emitter-rule constructor:
select only validated SOURCE-ENTRY10, strictly decode/reencode its blob, check
count/raw source hash against authenticated compiler pins, parse its declared
plan-emitter definition non-evaluating, and recompute complete rule-record/hash.
H6 never calls the physical `c-backend-stage2-plan-emitter-source-rule!`, its
path resolver or `p15-s23-stage2-compiler-pinned-source!`. The pure00809 arity
consumes this exact local rule instead of invoking the legacy00644 method.
The pure00829 arity constructs products through the new canonical pure00819
route, validates them with the pure00828 relation, then factors the existing
common artifact-body/identity constructor. It never calls any legacy/default
compiler-c2-reader-source-artifact/c2-reader-products arity, current binding,
precomputed Object sentinel, directory-derived context or physical fallback.
Plan construction here uses the existing pure00580 seed compiler plan path,
not00582 construct-emitted-core-plan; no emitted runtime/core plan construction
or dynamic compiler-artifact acquisition belongs to this origin admission.

The executable reader plan has exactly the existing 11 keys `:kind
:compiler-artifact-plan? :entrypoint :source :compiler :module :functions
:instruction-summary :effect-summary :sh03-reader :plan-id`. Functions have
exactly `:name :params :arity :body-form-count :binding :instructions`; bindings
have exactly `:name :kind :namespace :profile :target :visibility :effects
:capabilities`. Existing closed instruction schemas, SH03 contract, result
schema, digest schedule, semantic work budget and verifier grammar remain
unchanged. The plan and scratch verification values die with this call.

Explicit C/T project context is exactly `{:project-root-id PROJECT-ROOT-ID
:project-root-path nil :project-relative-path DESCRIPTOR-PATH}`, with the
existing `:gravity/sh07-b51-c2-project-root-id-v1` equation. All ten units are
freshly replayed, including 3-9; every replayed module C2 joins its transaction.
For 2/10/11 use the same canonical reader directly without a transaction.
Derive every UNIT-DESCRIPTOR, PN token/form/span/seed/diagnostic/literal/extension
projection and MR root exactly as V9, then compare all ten C2/L1 units and
their aggregates byte-for-byte. No field is dropped beyond V9's named PN
operations. Project-root records, identity inputs, reader options/features,
source order, roots, source IDs, bounds and authenticated reader revision join
the full C2 artifacts before projection. Reader binding source-path provenance
joins the explicit pre-final reader-provenance; pure reconstruction uses that
supplied path as data only and can neither resolve nor open it.

FULL-C2-COMPARATOR is a transient complete equality check, not the unit
projection predicate. For ordinals3-9 require `W(FRESH-ARTIFACT) =
W(TRANSACTION[:c2-reader-artifact])` over the entire validated artifact,
including artifact ID, reader-product integrity/incremental hashes, full
gravity-reader-boundary, plan-binding, all retained resolved reader result data,
authenticated envelope/descriptor and adapter contract. No artifact field is
omitted or normalized by this comparator. Both are produced/replayed with the
same declared logical unit path, project context and explicit anchored
reader/process provenance, so physical-path fields must equal, not be erased.
Require source/reader revision counts and hashes; plan/function/name/shape/
entrypoint/verifier/builtin identities and instruction summary; compiler and
emitter source/semantic/rule identities; module declarations, roots, bounds,
source maps, literal/extension records, diagnostics and generated origins.
Validate the existing complete C2 schema, authenticated envelope/digest graph
and canonical artifact identity for both values. For 2/10/11 run that same full
validation and canonical identity recomputation on fresh artifacts, then
compare their PN unit descriptors/products against the supplied carrier before
discarding them; there is no retained full-artifact target for2/10/11. A mutation
in plan-binding/envelope/integrity/provenance that leaves PN units unchanged
must reject. Transaction artifacts are comparison targets, never replay inputs.

The pure L1 operation constructs the exact ordinal0 document descriptor,
ordinal2..11 source units/source order/syntax from the fresh validated C2
artifacts, and grammar-change exactly:none. It joins ordinal0/1 strict decoded
contract text to their existing source events; it does not parse Markdown as
Gravity or confer L2 semantic reconstruction. Ordinal11 is both the emitter
subject unit and the source of the plan-emitter artifact data; ordinal10
provides the compiler's plan-emitter rule. Each consumer must identify its
precise entry and recompute its existing source/semantic/rule identities.

Ordinal0/1 contract text event products, ordinal2 checked-core form product,
ordinal10 compiler-emitter value, ordinal11 compiler-artifact bindings and
ordinal12 resolution-plan authority are rebuilt from these same authenticated
inputs under the existing event-product equations. Module event products and
seven C5 authorities join existing M0/M1/R/Q/RC construction and verification.
The amendment adds no macro/lowering rule, C6 map disposition, sidecar purpose,
owner/root/edge reconstruction or full-precertificate rule. The existing
no-weaker transaction and SH06 replay obligations remain mandatory in Gate H;
an origin-only success cannot stand in for full Gate H acceptance.

`current-binding!`, cached binding delays, file readers, path resolvers, Git
subprocesses, classpath/JAR resource lookup, live namespace discovery,
environment/CWD reads and `with-redefs`/dynamic authority injection are absent
from the post-final pure call graph. The precomputed-products route cannot
escape this rule. A new private retained-input route must factor the owning
constructors/validators instead of replacing them with mirrored constants or
weaker shape checks. Legacy file routes remain outside H6.

## Bounds, order and failure

Origin admission first performs bounded plain-data shape traversal before any
conversion, hash, decode, sorting or plan execution. New origin caps are:

```json gravity-gate-h-origin-contract-v1
{"artifact_tag_delta":0,"checker_key_count":22,"diagnostic_order":["origin-shape-bound","admission-anchor","handoff-shared-join","origin-object-proof","origin-source-binding","origin-implementation-binding","origin-reader-contract","origin-reader-replay","origin-semantic-mismatch"],"identity_domain_delta":0,"legacy_checker_key_count":21,"legacy_product_key_count":11,"logical_algorithms":7,"origin_bounds":{"max_classpath_bytes":16777216,"max_classpath_files":4096,"max_commit_bytes":65536,"max_depth":64,"max_jar_bytes":16777216,"max_nodes":67108864,"max_origin_pair_nodes":134217728,"max_origin_pair_raw_bytes":100663296,"max_path_segments":32,"max_path_utf8_bytes":4096,"max_proof_bytes":2097152,"max_source_entry_bytes":1048576,"max_total_raw_bytes":50331648,"max_width":16777216},"origin_key_count":7,"origin_proof_operations_per_source_session":3,"origin_proof_participants":["m","a","b"],"owned_c_after":172,"owned_c_before":160,"owned_c_delta":12,"participants":4,"physical_fragment_delta":0,"pre_decodes_after":208,"pre_decodes_before":196,"pre_operations_after":223,"pre_operations_before":209,"pre_paths_after":218,"pre_paths_before":204,"product_key_count":12,"pure_admission_guards":{"additional_bootstrap_targets":[],"artifact_build_context":null,"authenticated_source_form_records":null,"c2_external_operation_maps":{},"compiler_artifact_binding_context":null,"data_readers":{},"default_data_reader":null,"fixed_logical_diagnostic_paths":true,"fresh_bindings_are_lexical":true,"legacy_source_routes_forbidden":true,"macro_operation_context":null,"plan_emission_context":null,"read_eval":false,"reject_host_metadata":true,"reject_unrealized_pin_delays":true,"strict_source_roundtrip":true},"q_operations_per_participant":5,"q_participants":["l","m","a","b"],"source_counters":[13,13,40,1],"source_entry_count":13,"source_only_after":10,"source_only_before":8,"unit_ordinals":[2,3,4,5,6,7,8,9,10,11],"version":1}
```

Max-source-entry-bytes applies to each of 13 blobs and both dependency blobs. Classpath
bytes and JAR bytes are cumulative separate caps; max-proof-bytes is cumulative
commit/tree bytes including repeated bytes in transported proofs. Total raw
bytes counts all retained blob/JAR/commit/tree occurrences, not deduplicated
allocation hints. Text and path strings also contribute nodes/scalars under
the origin traversal; their UTF-8 round trips must stay within corresponding
byte caps. Each origin copy independently satisfies these bounds. Count both
logical copies, without a shared-reference discount, against the exact pair
caps100663296 raw bytes/134217728 nodes. Canonical equality uses bounded
structural comparison or streaming W equality; it cannot allocate two
unbounded canonical buffers or count shared host object identity as authority.
Max-width applies per container, max-nodes cumulatively, depth at
each edge; enforce path/scalar limits before host encoding. No lazy collection
is realized to discover its size. Bounds narrow origin admission without
raising any existing carrier/reader/transport bound or promising universal
capacity preservation.

SH03 plan caps remain 524288 nodes, depth512, width131072. Reader/result caps,
source/token/form/container/normalization-work and semantic-work-unit counters
are derived from the authenticated `sh03-reader-bounds`/`sh03-bounds-record`
contract and must agree. Both reader and verifier independently enforce them.
No H6 fuel allowance can override them. H6 replays exactly ten subject units
once per validation; diagnostic or budget exhaustion stops before the next
unit. Compiler/reader plan construction must enforce the existing static
carrier/instruction limits before recursively walking/compiling input; if an
owning constructor lacks that check, implementation must HOLD rather than
introduce an unbounded bootstrap path. JAR decoding bounds total ZIP entries
at16384, declared source entries at4096 and
total expanded source bytes at16MiB; exceedance rejects before expansion grows.

Failures use `SH07-B51-00945-GOVERNED-READ` with the ordered reasons in the
machine block. Details have exactly `:ordinal :path :field :owner-id :limit`;
absent positions are nil, limit is nil or exactly `:name :maximum :observed`.
Field is nil or a vector of at most32 declared field keywords/nonnegative
in-range ordinals. Path is nil or the fixed safe logical source/dependency
path, never supplied hostile text. No raw bytes/text or partial products appear
in details. Shape/key/aggregate bounds precede anchor checks; all ten shared
joins and bundle-ID equality precede proof/replay work. Inside each phase use
source ordinal, dependency UTF-8 path, unit ordinal, then these field vectors:
origin `[version content role content-object-proof source-entries reader-input
implementation-inputs]`; descriptor `[ordinal kind module-ordinal path mode
blob-oid byte-count raw-sha256]`; proof `[schema path root-tree tree-chain
blob-bytes]`; chain `[tree-oid tree-bytes component selected-mode selected-oid]`;
semantic `[descriptor tokens forms source-map diagnostics literal-records
reader-extensions syntax source-order]`. Fields absent from a narrower
descriptor are skipped. Proof/object format errors use origin-object-proof;
byte/text/descriptor/event joins use origin-source-binding; complete dependency
tree/JAR/load mismatches use origin-implementation-binding; plan/pin/constructor
contract failures use origin-reader-contract; owning reader/verifier failures
use origin-reader-replay; complete artifact or PN/MR mismatch uses
origin-semantic-mismatch. Earliest phase/coordinate/field wins. The owning C2 diagnostic ID
is retained in owner-id for malformed UTF-8/literal/reader/resource failure.
Owner-id is nil or an exact member of `diagnostic-ids` in the authenticated C/T
`gravity/c2_reader_diagnostics/catalog.clj` (including C2-HASH); extract that
finite literal vector non-evaluating from the retained source closure. Other
owner diagnostics are preserved in their owning internal execution result but
map to nil in this bounded H6 detail. Exception messages,
class names and caller-selected strings are never owner IDs. Unknown owning
failures map to the declared H6 reason with owner-id nil.
An interruption propagates with thread interruption restored. No fallback,
accepted partial carrier, success report or retained scratch cache exists.
Retry reconstructs independent inputs and fresh plans; failures leave no
process-global acceptance or persistent cache state.

## Explicit future allocation and counter changes

Current architecture ownership is only this report and its active lifecycle
record, the parent's delegation paragraph and the necessary two manifest rows,
counts and aggregate digest. All peer records and terminal bytes are preserved.
No production writer is activated by this document.

For a separately authorized implementation, OWNED-C-H6 is exactly the current
literal 160-path OWNED-C vector plus this report and these eleven existing
physical helper paths, unsigned-UTF-8 sorted without duplicates:

```text
bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00644-c-backend-stage2-plan-emitter-source-rule.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-018-p15-s23-stage2-sh02-authenticated-envelope-verification-report-through-p15-s23-c-backend-diagnostic-stage.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00819-c2-reader-products-clojure-oracle.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00829-compiler-c2-reader-source-artifact.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00809-sh03-reader-plan-instruction-summary.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00811-sh03-reader-plan-validate.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00813-sh03-reader-canonical-identity.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00816-sh03-reader-raise-rejection.clj
bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00828-sh03-reader-internal-product-authority.clj
bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/146156-sh03-reader-adapt-boundary.clj
bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/146156-sh03-reader-adapt-install.clj
```

These additions provide only pure retained-input plan/C2 construction and
verification. `00808` pins/bounds, `00645` pinned filesystem reader,
`reader.gravity`, `authenticated_envelope.gravity`, compiler/emitter Gravity
sources, `00748` SH02 pins, `00750-003` pure compilation, `00750-004`
invocation and `00750-009` bounded envelope constructors,
C3-C6 semantic producers,
shared Stage2 interpreter and all public runtime entrypoints remain read-only.
The pure route supplies freshly derived compiler rules to the existing plan
builder; it never calls `00645`'s physical reader. Existing 00945 fragments,
checker/M private validation/envelope fragments and Git-evidence PRE/allocation
fragments may change only as needed for this source-origin plumbing, within
their current owned-C memberships. This explicitly includes checker `03`/M
`04` JAR retention/memory parsing and checker `04`/M `05` classpath retention,
checker/M source-return/envelope stripping and preflight literal counts,
Git-evidence `00-owned-source-fragments`, `01-git-executable`,
`10-build-source-descriptors`, `15-reconstruct-pre-object-snapshot`,
`16-validate-pre-discharge-envelope`, `17-validate-git-observation`,
`18-build-evidence-eligibility` and `20-validate-eligibility-object`, plus the
existing owned contract-discharge/PRE predicate and transport-counter owners.
Each H6 metadata constructor/validator selects the separately versioned exact
H6 allocation/count constants; it cannot globally widen legacy E5 predicates
or silently replace a recorded209 operation plan. Serialized key sets and IPC
byte limits remain unchanged; H6's explicitly amended counts/roots are
recomputed, not relabeled. Future tests extend the existing owned
governed-read, SH06 seam, checker-child and source-closure tests, not new files.
The implementation child must reserve the actual subset of these paths and
the exact parent handoff before any edits. No speculative blanket I9 writer.

No source file or load resource is added/moved. The 250-line production ceiling
and seven logical algorithm descriptor slots remain unchanged. The load-order
manifest stays byte-identical; helpers remain their existing enclosing
`gravity.bootstrap` components, not extra algorithm rows. If factoring cannot
fit these physical files or needs a C3/other helper edit, stop for a further
independently governed allocation amendment; do not silently add a fragment.

Add exactly `reader.gravity` and `authenticated_envelope.gravity` at the fixed
paths above to SOURCE-ONLY-H6. These are reader/envelope dependencies, not new
semantic source specs. Counts change explicitly:

```text
33 document paths + 10 source-only + 172 owned-C + 3 A9-only = 218 PRE paths
1 show C + 1 C ls-tree + 218 C cat-file + 3 baseline operations = 223 PRE ops
33 document decodes + 172 owned-C decodes + 3 A9 decodes = 208 static decodes
4*223 = 892 participant PRE ops; plus 4*5 Q ops = 912
4*208 = 832 static decodes; 4*172 = 688 owned-C descriptors
```

The added report is an owned-C static governing blob; document receipt remains
33 and A9-only remains3. PRE retains reader/envelope raw bytes as an explicit new
private return member `:origin-static-inputs`, exactly `:reader-object
:envelope-object :amendment-object`, each a five-key descriptor plus raw byte vector in a
two-key `:descriptor :bytes` row. These raw inputs are not PRE discharge or
receipt members and do not enter their existing closed serialized schemas.
PRE still authenticates them through actual requested Git objects and raw
hashes; H6 then checks complete inclusion proofs against C/T.

The additional origin-proof channel performs exactly three pre-final `cat-file
tree` operations per source-session participant `[:m :a :b]` (the exact aliases
above): raw `bootstrap/clojure`, `bootstrap/gravity/src/gravity/bootstrap`
and `bootstrap/gravity/src/gravity/compiler` trees. The existing13 session proofs
already contain root/bootstrap/gravity/src/gravity ancestors; all classpath
src trees are reconstructed from the complete retained leaf manifest. No
additional commit/blob operation is needed. Requested tree OIDs are derived
from proved parent tree bytes, never supplied or predicted. Record the three
actual operations in existing Git observation grammar, ordered classpath,
reader, envelope, separately from PRE and semantic source counters. The owning pre-final
entrypoint enforces exactly3 operations with dense ordinals0/1/2 and joins their
derived OIDs/raw bytes to the retained proof inputs; these observations are
internal evidence, not an H6 report/approval field. H6 revalidates their raw
proof consequences, not unverifiable operation counters. Three participants
add9 proof operations. L performs only its PRE/Q channels and has no origin
proof/source session. This is921 combined PRE+Q+origin-proof operations,
excluding unchanged classpath/other V9 channels, not a relabeling of PRE's912
aggregate. Each source-session participant's existing checker/M preflight
owner additionally performs exactly two bounded NOFOLLOW physical reads of
reader.gravity and authenticated_envelope.gravity at the authenticated
materialized C/T dependency paths BEFORE its
H6 session finalization. It requires a regular `100644` file, a maximum 1MiB read,
and byte-for-byte equality with the PRE Git blob; it captures supplied path,
workspace root and invocation root at that point. Its private observation has
exactly `:ordinal :operation :role :path :materialized-path :byte-count
:raw-sha256`: dense ordinals0/1, operations:reader-materialization-read and
:envelope-materialization-read, the actual source-session role, the respective
fixed dependency logical path, captured materialized path, and observed
count/hash equal to PRE. The owning preflight enforces these two new
observations and aborts on mismatch before constructing expected admission.
These are two additional measured reads, not a claim about all existing legacy
pre-final TCB reads or a change to source-session13/13/40/1 counters. Its raw
observation is internal evidence, not a post-final verification report field;
retained JAR/source bytes reuse the role's existing classpath acquisition.
After finalization every channel performs zero acquisition.

No new H domain or artifact tag is introduced: origin/expected-admission are
plain private typed inputs with no IDs. Existing C/T/raw source, project root,
classpath, expanded source/fragment, plan, C2 and MR identity equations remain
in force. Actual changed implementation/report bytes require fresh derivation
of affected OWNED-C/PRE/source-closure/checker/algorithm/certificate/evidence
identities later; no concrete future hash is predicted. The five pin-mask slots,
C6 mapping sidecar/purpose/record rules and performance leaf sort are preserved.

## Required implementation evidence and lifecycle exit

Future execution evidence must run actual source acquisition, immutable proof
authentication, pure plan construction, reader/verifier/adaptation and owning
source-semantic consumer. A synthetic shape model or an exit0 is not acceptance.

- Fresh bounded real13-entry producer, both A/B roles and independent origin
  consumer; exact12/22, one finalization and all ten fresh C2/L1 unit joins.
- Omit/add/rename every nested field and entry; wrong ordinal, path, kind, mode,
  project root, reader feature/options, byte count/hash, proof link or C/T.
- Coherently mutate both origin copies and carrier/transaction/root families:
  forms, tokens, spans, source maps, diagnostics, literal/extension records,
  source order, complete unit swaps, source/reader/compiler/emitter revisions,
  function instructions, static bounds, generated origins and fragment owners.
- Replace a whole valid origin family with another valid C/T or role. It must
  reject against independently selected expected admission, even after all
  exposed IDs, event roots, C5/link/carrier identities are recomputed.
- Missing/extra classpath source, raw JAR/source substitutions, tree encoding
  order/mode/ancestor changes, loaded helper mismatch, fragment/load-order drift,
  external dependency namespace/shadow/coordinate mutations and ZIP bounds.
- Arm all Git/filesystem/source/JAR/resource/current-binding/delay-thunk/path-resolver
  sentinels after finalization. Exercise success, failure and retry; observe
  zero calls and no persistent cache or escaped closure/receipt authority.
- Exact shape/depth/width/node/byte/work/plan/result boundaries, interruption,
  stable diagnostic precedence, no partial commit/output and distinct process
  scratch isolation. Preserve existing positive/negative C6 mapping evidence.

Architecture exit requires exact report/base/candidate/tree identities,
governance/strict authority/docs/roadmap/diff/preflight validation, bounded
executable checks of this contract's key counts/path membership/counter
arithmetic and mutations, and fresh independent semantic plus adversarial
acceptance. These architecture checks grant no production evidence. Main
advancement invalidates affected exact reviews; refreeze and review changes.
No merge, terminal publication or implementation begins under the current
grant. Return the accepted integration-ready tuple to Uniform for the separate
completion direction and implementation authority decision.

## Residual boundaries and nonclaims

H6 origin admission is a prerequisite, not full Gate H or SH07 completion.
Gate L2 owner/root/edge reconstruction, Gate F whole precertificate, A/B/M
runtime/replay propagation and materialized governed acceptance remain open.
Clojure/JVM/Git, seed parsing and Stage2 execution remain explicit TCB.
No hostile-host proof, runtime/public certificate admission, performance,
full-language, full I9, self-hosting, seed retirement, seedless or release
authority follows. Progress remains7/30,6 executable,7 Clojure boundary,4
blocked,prefixSH00-SH06; SH07 partial/recorded4-of8 and full-language0/240.
