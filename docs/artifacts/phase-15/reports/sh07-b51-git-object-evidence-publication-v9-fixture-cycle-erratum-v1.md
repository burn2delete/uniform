# SH-07 B51 Git-Object Publication V9 Fixture-Cycle and A/B Ownership Erratum V1

Status: draft architecture correction; report only; no implementation authority

## Purpose

This report corrects two constructability failures exposed after the integrated
SH06 external-binding erratum for SH-07 B51 Git-object evidence-publication V9.
The first is a content-addressing cycle.
That erratum makes source event 39 carry a complete resolution-plan authority
whose authenticated content includes the frozen I9 commit `C`, tree `T`, and
the raw commit-object proof for `C`. V9 also requires the complete realized
source-event vector, including event 39 and its product identity, inside
`accepted.edn`, even though that fixture is itself one of the exact
`OWNED-C-PATHS-E2` blobs in `T`.

Those two requirements cannot be constructed in the required order. This
correction keeps the accepted and rejected fixtures as immutable C-owned
conformance harnesses, but makes their C-resident content static. Exact
13-descriptor and 40-event realized values remain mandatory and are derived
and authenticated only after `C/T` exists through the already-required Q,
A/B/M, runtime-discharge, E9, and independent-review channels. No dynamic
expected value is accepted from a fixture or caller.

The second failure is an ownership omission. The two exact selected A/B
checker implementations still expose their legacy Attempt30 input and
certificate key sets. They cannot receive the authenticated module transaction
and SH06 plan-authority preimages, and I9 does not own those implementations or
their direct tests. No currently owned adapter is an authentic replacement:
the checker child invokes `produce-certificate` before it sees a certificate
to validate, the materializer consumes already-constructed stage inputs, and
the tier-0 materializer validates and joins supplied products. V9 requires A
and B themselves, in separate processes, to construct the lowering product,
operations, receipts, witness, and only then the certificate.

This report preserves the E2 source and event counts, the complete resolution
plan and C5 authority carriers, the lexical SH06/SH07 seam, unauthorized-edge
rejection, process topology, Git-object publication topology, fixed-fuel
semantics, lifecycle ordering, and every authority nonclaim. It expands only
the exact I9 checker source/test ownership needed to make that already-required
A/B construction executable.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-fixture-cycle-erratum-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-pin-free-publication-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-fixture-cycle-erratum-v1.md",
  "status": "draft",
  "base_commit": "798ebcfc49cf62085e17bf67c764e42aa6c0ffe0",
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

Candidate commit `2acb3b52d4e4f25bb107952ba3aca3ea62164472`, tree
`c3968e69815f44b846ab980cd696d11b675442b9`, was independently rejected
because its 17-key checker handoff omitted the complete source semantic carrier
and semantic-algorithm source authority required before A/B lowering-product
construction. That candidate grants no architecture or implementation
authority. The corrected handoff below supersedes only that rejected
candidate's incomplete handoff definition; its Git history is retained.

## Reproduced identity cycle

Let `F` be the canonical bytes of the accepted fixture, `OF` its Git blob OID,
and `ENTRY(F)` the mode-100644 tree entry for its literal I9-owned path. The
accepted V9/E2 rules require all of these relations at once:

```text
OF = git-sha1("blob " || decimal-byte-count(F) || NUL || F)
T  = git-tree(... ENTRY(F, OF) ...)
C  = git-commit(tree = T, ...)

PLAN-AUTHORITY(C,T) includes:
  :content {:commit C :tree T}
  :content-object-proof {:oid C :raw-bytes raw-commit-bytes(C)}

EVENT-39(C,T) includes:
  :product-id H([:gravity/sh07-b51-source-event-product-id-v1
                 {:ordinal 39
                  :domain :sh06-resolution-binding
                  :consumer
                  gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding
                  :source-raw-sha256 DESCRIPTOR-12[:raw-sha256]
                  :product PLAN-AUTHORITY(C,T)}])

F = W({..., :source-events [... EVENT-39(C,T)], ...})
```

The resulting dependency graph has the strict cycle
`F -> EVENT-39 -> C/T -> ENTRY(F) -> F`. `F` must be fixed before `T` and `C`
can be computed, while the former fixture rule requires `C/T` and the raw
commit bytes before `F` can be computed. Replacing a provisional `C/T` after
commit creation changes `F`, hence `OF`, `T`, and `C`, invalidating the values
just inserted. Rehashing repeats the same cycle.

This is not repaired by copying a prior event, inserting a placeholder, using
a mutable ref, omitting the content-object proof, accepting a self-consistent
but unproven product ID, or searching for a cryptographic fixed point. Each
option violates the exact E2 object-inclusion, event-product, or future-value
rules. The required result is therefore rejected as
`SH07-B51-IDENTITY-CYCLE` until the C-resident fixture is made independent of
realized `C/T` values.

The earlier V9 statement that the static C fixture contains only a closed
derivation grammar and literal paths already establishes the correct phase
boundary. The E2 sentence requiring complete realized descriptor/event
vectors inside that same fixture crossed the boundary accidentally. BOOT6,
BOOT7, BOOT8, TEST10, TEST11, TEST13, and D9 require the dynamic values to be
reproducible and provenance-bound; none requires them to be embedded in an
ancestor that their identities depend on.

## Static accepted fixture V2

The accepted fixture remains at the exact existing E2 path:

```text
bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn
```

It remains one strict canonical EDN form plus EOF and remains authenticated by
its exact PRE `OWNED-C-DESCRIPTOR`. Its V1 schema is replaced by this exact V2
schema:

```clojure
ACCEPTED-FIXTURE-V2 =
{:schema :gravity/sh07-b51-contract-predicate-fixture-v2
 :row-plan-id ROW-PLAN-ID
 :source-specs SOURCE-SPECS-E2
 :source-event-plan SOURCE-EVENT-PLAN-E2
 :predicate-specs PREDICATE-SPECS
 :predicate-spec-id
 H([:gravity/sh07-b51-contract-predicate-spec-id-v1 PREDICATE-SPECS])
 :self-id-omits [[:fixture-id]]
 :fixture-id
 H([:gravity/sh07-b51-contract-predicate-fixture-v2
    ACCEPTED-FIXTURE-V2-WITHOUT-FIXTURE-ID])}
```

The key set is exactly `:schema :row-plan-id :source-specs
:source-event-plan :predicate-specs :predicate-spec-id :self-id-omits
:fixture-id`. `SOURCE-SPECS-E2` is the accepted complete 13-row coordinate
plan. `SOURCE-EVENT-PLAN-E2` is the accepted complete 40-row coordinate plan.
They contain the literal roles, ordinals, paths, domains, module ordinals,
source ordinals, and consumer symbols needed to derive realized data, but no
realized Git or product value.

The V2 fixture must contain none of the following:

- `C`, `T`, or any candidate-specific commit/tree/raw-commit proof;
- a blob OID, byte count, source raw SHA, source-descriptor root, realized
  source-event root, or realized source-event product ID;
- a complete resolution-plan authority, C5 authority, M0/M1 value, SH06
  context, source-read receipt, eligibility coordinate, runtime discharge,
  E9 payload, or later lifecycle value; or
- a caller-supplied expected realized descriptor, event, root, or product.

`ROW-PLAN-ID`, `PREDICATE-SPECS`, and `PREDICATE-SPEC-ID` retain their exact
V9 definitions. The predicate constructor and its 19 test Vars remain
mandatory. The old tag and H domain
`:gravity/sh07-b51-contract-predicate-fixture-v1` are removed from
`V9-TAG-REGISTRY` and `H-DOMAIN-REGISTRY`; the V2 tag/domain is added in the
same positions. Registry counts do not change. The fixture ID, registry roots,
owned-C root, PRE discharge, eligibility receipt, runtime discharge, and every
dependent identity must be rederived from the V2 bytes.

The fixture is a conformance fixture in D9's sense: it authenticates the
closed derivation plan and predicate program. It is not a replay record for
values that become knowable only after its containing commit exists.

## Post-C realized source authority

The complete E2 source values remain unchanged and mandatory:

- exactly 13 descriptors and `SOURCE-DESCRIPTORS-ROOT-E2`;
- exactly 40 realized events and `SOURCE-EVENT-ROOT-E2`;
- event 39's complete `RESOLUTION-PLAN-AUTHORITY`, including exact `C/T`, raw
  commit proof, resolution-source tree proof, plan binding, and authority ID;
- exactly 13/13/40/1 acquire/decode/event/finalize counters for each of A, B,
  and M; and
- the exact 39/39/120/3 A/B/M aggregate.

They are authenticated after `C/T` is immutable, never read from the accepted
fixture:

1. PRE reconstructs all 13 descriptors from exact C/T Git objects. Q carries
   the complete descriptor vector and descriptor root in the external
   evidence-eligibility receipt, as E1/E2 already require.
2. Each fresh A, B, and M source session independently consumes the same
   authenticated V2 `SOURCE-SPECS-E2` and `SOURCE-EVENT-PLAN-E2`, reads exact
   C/T objects, constructs all products, and emits its complete existing
   `SOURCE-READ-RECEIPT` with 13 descriptors, 40 events, both roots, exact
   counters, and `:finalized true`.
3. The existing runtime contract discharge carries the three complete source
   sessions. The E9 identity receipt authenticates that runtime discharge and
   its process-isolation joins. No new evidence path or schema is introduced.
4. Fresh independent implementation review replays exact C/T and E9 objects,
   reconstructs the descriptor vector from Git, executes the three fresh
   sessions, and requires all descriptor vectors, event vectors, roots, and
   counters to agree byte-for-byte.

For one valid A, B, or M `SOURCE-READ-RECEIPT`, define this nonserialized test
projection:

```clojure
REALIZED-SOURCE-CLOSURE =
{:content {:commit SOURCE-READ-RECEIPT[:commit]
           :tree SOURCE-READ-RECEIPT[:tree]}
 :source-descriptors SOURCE-READ-RECEIPT[:source-descriptors]
 :source-descriptors-root SOURCE-READ-RECEIPT[:source-descriptors-root]
 :source-events SOURCE-READ-RECEIPT[:events]
 :source-event-root SOURCE-READ-RECEIPT[:event-root]}
```

It has exactly `:content :source-descriptors :source-descriptors-root
:source-events :source-event-root`. It is valid only when the enclosing
receipt passes every E2 schema, object-proof, product, root, order, counter,
and finalization check. Projecting each realized event to exactly `:ordinal
:domain :source-ordinal :module-ordinal :consumer` must reproduce
`SOURCE-EVENT-PLAN-E2` byte-for-byte. Descriptor coordinates projected to the
four keys `:ordinal :kind :module-ordinal :path` must reproduce
`SOURCE-SPECS-E2` byte-for-byte. Q's descriptor vector/root and all three
realized projections must match. Event products and product IDs are always
recomputed from authenticated C/T products; agreement among supplied strings
alone is insufficient.

There is no new post-C expected-value artifact. Q, the three existing source
receipts, the runtime discharge, E9, and independent replay already provide
the required provenance graph. Adding another copy would widen ownership and
create an unnecessary authority surface.

## Static rejected mutation harness

The rejected fixture remains at its exact E2 path and retains its exact
two-key top-level schema `:accepted-schema :cases`. `:accepted-schema` becomes
`:gravity/sh07-b51-contract-predicate-fixture-v2`. Every case retains exactly
`:expected-reason :family :id :operation :target :value`; a case is only a
mutation recipe and never an expected dynamic result.

The first six generic source cases now exercise the static plans:

| Case IDs | Family | Target | Required rejection |
| --- | --- | --- | --- |
| `:source-omitted`, `:source-extra`, `:source-reordered` | `:source-plan` | `[:source-specs]` | exact 13-row census/order failure |
| `:event-omitted`, `:event-extra`, `:event-reordered` | `:source-plan` | `[:source-event-plan]` | exact 40-row census/order failure |

Their operations remain respectively drop/duplicate/swap as already defined.
The predicate cases continue to mutate the static V2 fixture. All other V9/E1
mutation families retain their existing authenticated in-memory subjects and
semantics.

The twelve E2 resolution-specific cases remain mandatory: missing, extra,
reordered, stale, and malformed descriptor 12, plus missing, extra, reordered,
stale, malformed, and wrong-product event 39. Those cases are applied only to
`REALIZED-SOURCE-CLOSURE` constructed after C/T from a valid fresh source
session. The unmodified closure must validate before any single mutation is
applied. The harness must then execute the real validator and compare the
observed reason with `:expected-reason`; the recipe's expected reason cannot
cause or authorize rejection.

No rejected recipe may carry a valid current C/T, complete current object
proof, current realized descriptor/event vector, current source/event root, or
current product ID as its `:value`. Invalid literal sentinels such as an
all-`f` digest or malformed object ID remain permitted because they do not
purport to be authority. The rejected fixture therefore remains static and
does not reintroduce the accepted-fixture cycle.

The rejected fixture additionally carries static mutation recipes for the
checker-local handoff. Their exact IDs and targets are:

| Case IDs | Target family |
| --- | --- |
| `:checker-source-carrier-missing`, `:checker-source-carrier-extra` | `[:checker-semantic-input :source-semantic-carrier]` exact presence/key set |
| `:checker-source-carrier-descriptor-mismatch`, `:checker-source-carrier-event-mismatch` | complete carrier vectors versus the finalized checker-local receipt |
| `:checker-source-carrier-c2-ordinal-2`, `:checker-source-carrier-c2-ordinal-10`, `:checker-source-carrier-c2-ordinal-11` | each direct strict C2 artifact and its C/T source-object join |
| `:checker-algorithm-descriptor-missing`, `:checker-algorithm-descriptor-extra`, `:checker-algorithm-descriptor-reordered` | exact seven-row algorithm descriptor vector |
| `:checker-algorithm-descriptor-object-mismatch`, `:checker-algorithm-root-mismatch`, `:checker-algorithm-cross-content` | descriptor mode/OID/count/raw-SHA, root equation, and common C/T authority |
| `:checker-precertificate-second-session`, `:checker-precertificate-worktree-read`, `:checker-precertificate-caller-value` | forbidden acquisition/authority source |

These recipes use only paths, operations, and invalid sentinels; they embed no
current C/T carrier, C2 artifact, algorithm descriptor, or root. At test time
the harness first derives one valid checker-local 20-key input, applies exactly
one recipe, invokes the real child/checker validator, and requires
`SH07-B51-CHECKER-CHILD` with the recipe's observed reason before certificate
construction. The accepted V2 fixture gains no dynamic handoff field.

## Authentic A/B witness-construction ownership

At this report's base, both selected checker sources still declare the same
legacy Attempt30 input key set:

```clojure
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :predecessor-executable-descriptor-id :module-source-identities
  :module-source-byte-counts :emitter-source-id :emitter-semantic-id
  :m0s :m1 :m2s :bundle-core :source-forms :checker :provenance}
```

Their certificate-input key sets omit `:lowering-product-id`, and their
certificate key sets omit `:semantic-witness`. The I9 checker child likewise
constructs the legacy input, calls the selected checker's
`produce-certificate`, and only afterward checks for those two V9 fields. An
adapter at that later check cannot satisfy the V9 dependency order, because
LOWERING-PRODUCT and WITNESS must exist before the checker certificate and
checker output. Moving construction to the common checker child would also
replace the required two independently executed A/B constructions with one
shared role-neutral producer. The tier-0 materializer is a parent-side
validator/joiner, and the pure materializer's currently implemented entrypoints
consume already-derived `:pre-stages` or `:collections-by-owner`; neither owns
an authentic A/B certificate-production boundary.

The smallest correction keeps the selected checker identities and adds their
existing sources and direct tests to I9 ownership. It also closes the
same-process handoff from each child-owned authenticated source session and
object preflight into its selected checker. The selected checker input becomes
this exact 20-key map, called `CHECKER-SEMANTIC-INPUT-E3`:

```clojure
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :predecessor-executable-descriptor-id :module-source-identities
  :module-source-byte-counts :emitter-source-id :emitter-semantic-id
  :m0s :m1 :m2s :bundle-core :source-forms :module-transactions
  :sh06-resolution-plan-authority :source-semantic-carrier
  :semantic-algorithm-source-descriptors
  :semantic-algorithm-source-root :checker :provenance}
```

The checker child obtains all five fields added to the legacy Attempt30 input
from its own authenticated C/T work. None may be supplied by M, the launcher,
a fixture, an ambient Var, or a worktree read. `:module-transactions` is the
exact complete seven-row E2 value. `:sh06-resolution-plan-authority` is the
exact complete E2 event-39 product and must revalidate against the same C/T and
the transaction contexts before either checker begins semantic witness
construction.

The object-mode builder return becomes `RECEIPTED-BUNDLE-CORE-PRODUCTS-E3`,
with this exact ten-key set:

```clojure
#{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
  :module-transactions :sh06-resolution-plan-authority
  :source-semantic-carrier :source-read-receipt}
```

During its one private source session, after the complete 13 descriptors, 40
events, and seven module transactions exist but before finalization, the
builder constructs the exact V9/E2 `SOURCE-SEMANTIC-CARRIER`. Its descriptor
and event fields are the complete session vectors. For source ordinals 3
through 9, its C2 artifacts join byte-for-byte to the seven module
transactions. For ordinals 2, 10, and 11, it constructs the required strict C2
artifacts from the already-prefetched C/T blob bytes and the exact explicit
project contexts defined by V9; no path is reopened and no second source
session is started. All L1 and C2 members and roots are computed under the
existing V9 equations. The carrier remains receipt-free and contains no
session ID, process value, operation, fuel receipt, lowering product, witness,
certificate, or checker output.

The session then finalizes exactly once. The builder returns that carrier and
the final source-read receipt together. Before invoking `produce-certificate`,
the checker child requires the receipt to be exact 13/13/40/1 and finalized,
requires carrier descriptors/events to equal the receipt vectors byte-for-byte,
recomputes their roots, revalidates the three direct C2 artifacts, and repeats
every transaction/artifact join. This is a same-session immutable handoff, not
an extra acquisition or transported semantic claim from another participant.

The remaining two fields come from the child-local object preflight already
completed for the same C/T. The child selects the exact seven
`SEMANTIC-ALGORITHM-SOURCE-DESCRIPTORS-E3` rows from the independently
reconstructed `OWNED-C-DESCRIPTORS-E3`, preserves the exact path order below,
and requires each selected mode, OID, byte count, and raw SHA-256 to equal the
same C-bound classpath source object. It then recomputes
`SEMANTIC-ALGORITHM-SOURCE-ROOT-E3` with the unchanged V9 domain. The exact
seven-row vector and root, not a caller-provided digest or a source-closure
projection, enter the checker input. This selection performs no new Git read,
strict decode, source event, or source session and therefore changes no E3
counter.

The handoff is acyclic. `SOURCE-SEMANTIC-CARRIER` depends only on finalized
source-session preimages and products that precede LOWERING-PRODUCT. The seven
algorithm descriptors and root depend only on immutable PRE/classpath C
objects. None contains or depends on a lowering-product ID, operation, fuel
receipt, final stage witness, certificate, or checker output. The selected
checker may consume the five fields but may not replace, augment, or rederive
them from ambient state.

Within its own process and after its own finalized governed C/T source session,
each selected checker must independently execute the already-normative V9/E2
construction order: derive all receipt-free carriers, FOUNDATION-PREPRODUCT,
PRE-STAGES, FUEL-SUBJECTS, and the complete LOWERING-PRODUCT; construct the
seven operations; execute and bind all 34 measurements, traces, and fuel
receipts; construct the seven final stage witnesses and enclosing WITNESS; and
only then construct its role-specific certificate and checker output. The
existing authenticated pure constructors in the owned Attempt32 materializer
may be invoked as common normative primitives, but the inputs and results are
constructed independently inside A and B. Neither checker may import, invoke,
or accept the output of the other.

The common certificate remains the literal V9 schema. Its `:inputs` key set
must include `:lowering-product-id`, equal to the checker-local complete
LOWERING-PRODUCT `:product-id`, and its top-level key set must include
`:semantic-witness`, equal to that checker's complete WITNESS. These are not
post-certificate annotations. Certificate ID, evidence root, output identity,
transcripts, and every dependent value are computed only after both fields are
present. M independently reconstructs the same pre-certificate products from
its later C/T session and validates each child value; byte agreement between A
and B without M's reconstruction remains insufficient.

Because the selected checker sources now participate directly in semantic
witness construction, the exact role-neutral algorithm source vector becomes:

```clojure
SEMANTIC-ALGORITHM-SOURCE-PATHS-E3 =
["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
 "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"]
```

`SEMANTIC-ALGORITHM-SOURCE-DESCRIPTORS-E3` is the exact projection of
`OWNED-C-DESCRIPTORS-E3` at those paths in that order. The existing
`:gravity/sh07-b51-semantic-algorithm-source-root-v1` equation is applied to
that seven-row vector. Both children and M authenticate all seven descriptors
from C objects without loading the forbidden checker namespace. This changes
the root and every dependent product, operation, receipt, witness, certificate,
and evidence identity, but it does not merge the role-specific checker source
closures or checker identities.

## Counts, ownership, and downstream identities

These four existing paths move from read-only prerequisite status into I9
ownership:

```text
bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj
bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj
bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_independent_census_test.clj
bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_census_test.clj
```

No other implementation, test, fixture, load-order, runner, governance, or
evidence path becomes writable. `OWNED-C-PATHS-E3` is this exact 37-string
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
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_census_test.clj"
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

`I9-LIFECYCLE-PATHS-E3` is the exact 37 strings above followed by the unchanged
E2 eligibility receipt, I9 integration receipt, I9 integration discharge, and
content-addressed I9 terminal-record path spec, in that order.
`READ-ONLY-PATHS-E3` is exactly `READ-ONLY-PATHS-E2` with the four paths above
removed; no other read-only path changes.

`V9-OWNERSHIP-PLAN-E3` replaces the E2 plan. It has the exact E2 key set and
values except schema is `:gravity/sh07-b51-v9-ownership-plan-e3-v1`, I9
allocation paths are `I9-LIFECYCLE-PATHS-E3`, read-only paths are
`READ-ONLY-PATHS-E3`, and plan ID is
`H([:gravity/sh07-b51-v9-ownership-plan-e3-v1
PLAN-E3-WITHOUT-PLAN-ID])`. Allocation owner, ordinal, stage, every other
allocation, semantic-review owner, reservation/handoff/read-only stage sets,
open exit gates, aggregate `:partial` state, and self-ID omission remain
byte-equal to E2. The E2 ownership-plan tag and H domain are removed from
`V9-TAG-REGISTRY` and `H-DOMAIN-REGISTRY`; the E3 tag/domain is added at the
same positions. Registry counts do not change.

The four paths were not members of the E2 distinct PRE union. Consequently
`PRE-C-PATHS-E3` is the unsigned-UTF-8-sorted distinct union of the unchanged
33 document paths, ten `SOURCE-ONLY-PATHS-E2`, 37 `OWNED-C-PATHS-E3`, and the
unchanged three E1 A9-only paths: exactly 83 paths. PRE is exactly 88 Git
operations: show C; one 83-path C `ls-tree -z`; 83 C `cat-file blob` rows;
show base; one base BOOT5 `ls-tree`; and one base BOOT5 `cat-file`. The 33
documents, 37 owned-C blobs, and three A9 artifacts are strictly decoded, for
exactly 73 PRE strict static decodes; all ten source-only blobs remain raw in
PRE.

`ELIGIBILITY-COUNTERS-E3` retains the E2 key set and is exactly:

```clojure
{:participants [:l :m :a :b]
 :q-coordinate-git-operation-count 20
 :pre-discharge-git-operation-count 352
 :git-operation-count 372
 :q-coordinate-decode-count 4
 :receipt-id-check-count 4
 :pre-discharge-reconstruction-count 4
 :pre-discharge-static-decode-count 292
 :pre-discharge-document-descriptor-count 132
 :pre-discharge-source-descriptor-count 52
 :pre-discharge-pre-masked-file-count 8
 :pre-discharge-owned-c-artifact-count 148
 :pre-discharge-a9-artifact-count 12
 :pre-discharge-baseline-boot5-count 4
 :content-match-count 4
 :source-descriptors-match-count 4
 :source-descriptors-root-match-count 4
 :pre-masked-root-match-count 4
 :owned-c-root-match-count 4
 :pre-discharge-match-count 4}
```

The equations are `4*5=20`, `4*88=352`, and `20+352=372`. Every participant
reconstructs the exact 88-row PRE plan. Source-only paths and source sessions
remain exactly E2: ten source-only paths, 13 descriptors, 40 events,
13/13/40/1 per A/B/M, and aggregate 39/39/120/3. Q remains five operations;
G9 remains 49 operations and the separate post-G9 plan remains 24 operations.

Because both fixture blobs and both selected checker sources/tests change,
their Git OIDs, byte counts, and raw hashes
must be derived from the final static bytes. `OWNED-C-ROOT`, PRE discharge,
eligibility receipt, participant check roots, runtime discharge, E9 identities,
semantic algorithm/source roots, fixed-fuel subjects, contract evidence,
certificates, and every downstream identity that includes either fixture or
the V2/E3 registry values must be rederived. No V1 fixture ID, E2 ownership
plan ID, five-path algorithm source root, legacy certificate ID, or dependent
root survives as authority.

The architecture erratum itself owns only this report. Its active governance
reservation and compact ledger row are coordination metadata, not an
additional member of `OWNED-C-PATHS-E3`. If this report is independently accepted
and integrated, the implementation workstream must add its terminal workstream
ID as an integrated dependency and reconcile onto the resulting authoritative
main before I9 can freeze.

## Normative supersession map

The substitutions below are exhaustive. All symbolic references to a replaced
fixture, checker-input, ownership, count, or algorithm-source value use the
E3 value defined here even where V9, E1, or E2 repeats the old literal in a
schema, registry, root, fixture, test, or acceptance paragraph.

| Prior authority | Exact E3 replacement |
| --- | --- |
| V9 `accepted.edn` V1 schema containing complete realized source descriptors and events | Exact static `ACCEPTED-FIXTURE-V2` containing `SOURCE-SPECS-E2`, `SOURCE-EVENT-PLAN-E2`, and the unchanged predicate plan/specification values |
| V9 fixture schema tag/H domain | Replace V1 with `:gravity/sh07-b51-contract-predicate-fixture-v2` in both registries without changing registry counts |
| E2 statement that accepted/rejected fixture source vectors contain complete 13-descriptor/40-event realized vectors | Static fixtures contain exact 13-row/40-row derivation plans; complete realized vectors and roots are authenticated after C/T through Q, A/B/M, runtime discharge, E9, and fresh review |
| Any accepted-fixture expected descriptor, blob, object proof, C/T, source root, realized event product, product ID, or event root | Forbidden from both static fixtures; reconstruct it from exact C/T and validate it in the existing post-C evidence channels |
| First six rejected source/event cases against accepted fixture realized vectors | Apply them to `:source-specs` and `:source-event-plan` in the static V2 fixture |
| E2 row-12/event-39 rejected cases against an ancestor fixture baseline | Apply them to a freshly validated post-C `REALIZED-SOURCE-CLOSURE` baseline |
| V9 statement that the contract-discharge test starts complete replay from an accepted source-closure value fixture | Start from the authenticated static plans, construct immutable test C/T, derive the realized closure from those Git objects, then perform the unchanged PRE-to-post-P9 replay |
| Fixture identity transitively dependent on E2 realized source/event roots or product IDs | Fixture identity depends only on the static V2 plan and predicate fields; dynamic roots and IDs retain their existing Q/runtime/E9 identities |
| E2 `OWNED-C-PATHS-E2`, I9 allocation, and read-only checker paths | Exact 37-path `OWNED-C-PATHS-E3`, `I9-LIFECYCLE-PATHS-E3`, and `READ-ONLY-PATHS-E3`; only the two selected checker sources and two direct tests move into ownership |
| E2 ownership-plan tag/domain and plan ID | Exact `:gravity/sh07-b51-v9-ownership-plan-e3-v1` tag/domain and `V9-OWNERSHIP-PLAN-E3`; registry counts are unchanged |
| 33 owned-C paths, 79 PRE paths, 84 PRE operations, and 69 PRE static decodes | Exactly 37 owned-C paths, 83 PRE paths, 88 PRE operations, and 73 PRE static decodes |
| E2 eligibility counters based on four 84-row PRE reconstructions | Exact `ELIGIBILITY-COUNTERS-E3`: 352 PRE operations, 372 total operations, 292 static decodes, and 148 owned-C artifacts; all unchanged fields retain their E2 values |
| E2 nine-key receipted builder return | Exact ten-key `RECEIPTED-BUNDLE-CORE-PRODUCTS-E3`, adding the complete receipt-free `:source-semantic-carrier` constructed from the same private 13-descriptor/40-event session before its sole finalization |
| Legacy Attempt30 checker input key set, and the rejected 17-key candidate handoff | Exact 20-key `CHECKER-SEMANTIC-INPUT-E3`, adding complete `:module-transactions`, `:sh06-resolution-plan-authority`, `:source-semantic-carrier`, `:semantic-algorithm-source-descriptors`, and `:semantic-algorithm-source-root` from the checker-local source session/PRE/classpath object preflight |
| Legacy certificate inputs without lowering product and certificate without witness | Literal V9 certificate input includes checker-local `:lowering-product-id`; literal V9 certificate includes complete checker-local `:semantic-witness`; both exist before certificate identity construction |
| Five-row `SEMANTIC-ALGORITHM-SOURCE-PATHS` and derived descriptors/root | Exact seven-row E3 vector additionally binding both selected checker source blobs; rederive the unchanged-domain algorithm root and every dependent identity |
| Any post-`produce-certificate` checker-child enrichment | Forbidden; A and B each construct products, operations, receipts, witness, and certificate in that order inside their separately selected checker execution |
| Any second source session, post-finalization source acquisition, worktree read, or M/launcher/fixture-supplied pre-certificate handoff | Forbidden; the child passes only the exact same-session carrier and the exact same-C PRE/classpath seven-descriptor authority after independently validating their complete preimages |

Every other V9 requirement and both prior errata remain normative. In
particular, this report does not change source descriptor/event schemas,
event-product equations, C/T object proofs, M0/M1 reconstruction, plan
authority, seven C5 authorities, unauthorized-cross-fragment rejection,
carrier products, fixed-fuel subjects, phase discharges, E9/G9/P9 topology,
or publication pins.

## Required tests and rejection evidence

In addition to every unchanged V9/E1/E2 test, I9 must prove:

- strict decoding of `accepted.edn` yields exact
  `ACCEPTED-FIXTURE-V2`, its static plan vectors equal the accepted E2 vectors,
  and both fixture/predicate identities recompute;
- the accepted fixture blob descriptor equals its C/T PRE owned-C descriptor,
  while its decoded value contains no C/T-dependent realized field;
- two temporary Git commits with distinct C/T and the same fixture blob retain
  the same fixture OID and valid static fixture, while each derives its own
  C/T-bound realized source closure;
- inserting C, T, a commit proof, a realized descriptor vector, event 39, a
  realized root, or product ID into the accepted fixture rejects exact fixture
  equality and uses `SH07-B51-IDENTITY-CYCLE` where the inserted value creates
  a back-edge;
- all three fresh A/B/M `SOURCE-READ-RECEIPT` values have exact
  13/13/40/1 counters, agree byte-for-byte on descriptors, events, and roots,
  and their coordinate projections equal the static plans;
- Q's independently reconstructed descriptor vector/root equals all three
  source receipts, and event 39's complete plan authority and product ID
  independently revalidate against exact C/T Git objects;
- the first six plan mutation cases reject the static fixture for the recorded
  reason; and
- every E2 descriptor-12/event-39 mutation is applied to a valid dynamically
  derived closure, changes the relevant vector/root, and is rejected by the
  real validator for the observed recorded reason;
- each checker-local builder returns the exact ten-key E3 product, constructs
  SOURCE-SEMANTIC-CARRIER before its one finalization, uses no second session,
  and joins direct strict C2 artifacts for source ordinals 2, 10, and 11 to the
  already-prefetched C/T objects;
- the checker child passes exactly the five authenticated fields added to the
  legacy input and rejects a missing, extra, reordered, cross-C/T, altered, or
  caller-supplied transaction, plan-authority, source-carrier, algorithm
  descriptor, or algorithm-root value before `produce-certificate` succeeds;
- source-carrier descriptors/events equal the finalized checker-local receipt,
  all eleven carrier members and roots recompute, and the seven algorithm
  descriptors equal both PRE owned-C rows and classpath object-preflight rows;
- attempts to obtain any handoff member from a second session, post-finalization
  Git read, worktree path, M, launcher, fixture, peer checker, or ambient state
  reject and do not produce a certificate;
- A and B independently construct their complete lowering product, operations,
  34 measurements, traces, fuel receipts, seven stage witnesses, and enclosing
  witness before their certificate IDs, with no output imported from the other
  checker;
- each certificate's lowering-product ID and complete witness rehash and join
  to its checker-local pre-certificate values, while M's independently
  reconstructed values match both; and
- the two direct Attempt30 checker test namespaces exercise accepted exact
  20-key E3 inputs and reject every new static handoff mutation recipe,
  missing/extra input keys, wrong product/witness joins, post-certificate
  enrichment, and forbidden peer-checker access; and
- accepted and rejected handoff tests prove the same 37/83/88/73 PRE counts,
  13/13/40/1 session counters, and zero additional handoff acquisition count.

The independent implementation review still must execute the authentic
source-derived seven-module transaction required by E2. Static fixture
validation, mutation-recipe validation, or agreement among copied identities
cannot substitute for that transaction.

## Implementation and lifecycle consequence

The current I9 implementation must not freeze against the cyclic fixture rule
or the unowned legacy checker boundary.
This draft report grants no permission to change implementation. Only after
this exact correction is independently reviewed, accepted, admitted, and
integrated may I9 reconcile onto authoritative main, add the dependency, and
change only `OWNED-C-PATHS-E3`: replace the two fixture values and their
narrowly associated validators/tests, update the two selected checker sources
and direct tests, and make the already-owned checker-child/materializer/test
seams conform to the exact ten-key builder return, same-session handoff,
20-key E3 input, and construction order. I9 then must
rederive every changed identity and complete all unchanged gates.

This report grants architecture authority only. It does not establish I9
implementation correctness, evidence eligibility, implementation acceptance,
integration eligibility, SH-07 completion, self-hosting, hostile-host proof,
release, performance authority, or seed retirement.

## Acceptance criteria

This correction is acceptable only if fresh independent review confirms:

1. the exact fixture/event-39/C/T dependency cycle above follows from the
   integrated V9/E1/E2 contracts;
2. the V2 accepted fixture is entirely static, closed, exact, and still binds
   every derivation coordinate and predicate program needed by the harness;
3. complete 13-descriptor/40-event values are not weakened or omitted but move
   only to existing post-C authenticated channels;
4. Q, A/B/M, runtime discharge, E9, and independent replay together prove the
   dynamic vectors, roots, counters, object inclusion, and event 39 product;
5. rejected mutation recipes contain no current dynamic authority and every
   E2 row-12/event-39 mutation runs against a fresh valid post-C baseline;
6. the ownership correction is exactly the two selected checker sources and
   two direct tests, with no post-certificate adapter substituted for authentic
   A/B construction;
7. exact ten-key `RECEIPTED-BUNDLE-CORE-PRODUCTS-E3` constructs the complete
   source semantic carrier, including direct strict C2 artifacts 2/10/11,
   inside the one checker-local source session and before its sole finalization;
8. exact 20-key `CHECKER-SEMANTIC-INPUT-E3`, including the complete carrier
   and seven algorithm descriptors/root, plus the V9 lowering-product/witness
   certificate fields close every required A/B dependency before certificate
   construction without a second read or transported authority;
9. the exact 37/83/88/73 ownership/PRE counts, 13/13/40/1 session counts,
   aggregate 39/39/120/3 counters, eligibility 372 operations, and G9 49
   operations are complete;
10. no other V9/E1/E2 topology, schema, authority carrier, rejection rule,
   lifecycle gate, or nonclaim is weakened;
11. the candidate is report-only, owns one unique report path, passes
   architecture/governance/document validation, and grants no implementation
   or aggregate SH-07 authority; and
12. the current I9 candidate remains draft until this report is independently
   accepted, integrated, and reconciled into its exact base and dependency
   set.
