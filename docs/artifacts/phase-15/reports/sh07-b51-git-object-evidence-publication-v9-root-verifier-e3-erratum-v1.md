# SH-07 B51 Git-Object Publication V9 Root-Verifier E3 Erratum V1

Status: draft architecture correction; report only; no implementation authority

## Purpose

This report corrects one remaining executable contradiction in the accepted
SH-07 B51 Git-object evidence-publication V9 architecture and its three
integrated errata. The fixture-cycle erratum requires checker A and checker B
to construct the literal V9 certificate: exactly 19 top-level keys including
`:semantic-witness`, with exactly 13 certificate-input keys including
`:lowering-product-id`. The current Gravity Root1 and Root6 compact verifiers
still recognize only the legacy Attempt30 certificate: 18 top-level keys, 12
input keys, no witness, no lowering-product join, and the stale report digest
`sha256:b105b395ba03a29e5bbd75ded681f21cdbb0375e4df73df9f9fbccf9aafa78db`.

The mismatch is executable, not editorial. A newly constructed E3 checker-A
certificate can pass the existing bounded Root1 preflight but is rejected by
`sh07-b51-r1-certificate-a-valid?`. A newly constructed E3 checker-B
certificate can pass the existing bounded Root6 preflight but is rejected by
`sh07-b51-r6-c30-checker-b-certificate?`. The shared linked-carrier host
fixture and both direct compact-verifier suites also construct or expect the
legacy shape. The already-owned 00947 carrier validator currently declares
the same legacy certificate and input key sets.

This correction makes the two Gravity compact verifiers and the three direct
test/fixture paths part of I9 ownership. It requires one strict V9-only
certificate contract at every compact boundary. It does not introduce a new
certificate schema, witness schema, digest domain, profile limit, source
descriptor, source event, Git observation, publication step, or G9 operation.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-git-object-evidence-publication-v9-root-verifier-e3-erratum-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-pin-free-publication-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-v9-root-verifier-e3-erratum-v1.md",
  "status": "draft",
  "base_commit": "14ff61809a5dbb4aa23499f14d5f7a288f76889e",
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

## Reproduced Root1 and Root6 contradiction

The integrated fixture-cycle erratum normatively retains the literal V9
certificate schema. Its complete top-level key set is:

```clojure
#{:artifact :domain :schema-version :status :role
  :architecture-report-sha256 :claim-id :claim :assumptions :checker
  :inputs :result :semantic-witness :provenance :raw-source-transcript
  :m2-transcript :profile-bounds :evidence-root :certificate-id}
```

Its complete `:inputs` key set is:

```clojure
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :lowering-product-id :predecessor-executable-descriptor-id
  :module-source-identities :module-source-byte-counts
  :emitter-source-id :emitter-semantic-id :m0-ids :m1-id
  :m2-ids :bundle-core-id}
```

At this report's base, Root1 in
`bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` instead requires
the same sets with `:semantic-witness` and `:lowering-product-id` omitted. Its
certificate preimage also omits the witness. Root6 in
`bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity` repeats those three
legacy choices. Both sources require the stale Attempt30 report digest rather
than the raw SHA-256 of the integrated V9 architecture report:

```text
sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd
```

This is not repaired by making the new fields optional, stripping them before
compact validation, validating only their digest syntax, or preserving two
accepted schemas. Those alternatives allow a legacy certificate to cross a
boundary that V9 requires to prove a complete checker-local lowering product
and semantic witness before certificate identity construction. They also
make 00947 and the Gravity roots disagree about which object is authoritative.

The Root6 failure is not a speculative stale assertion. Root6 owns the actual
checker-B certificate predicate and its certificate identity equation. The
delegating `root6_b.gravity` source adds no second certificate schema and need
not become writable. Root6 A and its direct compact test are therefore the
minimal independent-verifier correction.

## Literal E4 certificate contract

E4 introduces no version after V9. `V9-CERTIFICATE-E4` is the existing
literal V9 certificate with the exact 19-key top-level set and exact 13-key
`:inputs` set above. Every existing V9 literal sub-schema, role constant,
claim equation, transcript equation, evidence-root equation, category
schedule, assumption map, checker identity constraint, provenance constraint,
result constraint, and canonical encoding rule remains unchanged.

The architecture report digest in the certificate and its inputs is exactly:

```clojure
(def V9-ARCHITECTURE-REPORT-RAW-SHA256
  "sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd")
```

It is `R` of the integrated
`sh07-b51-git-object-evidence-publication-architecture-v9.md` bytes. It is not
the digest of this erratum, a prior candidate, a merged tree, or a copied
constant. Root1, Root6, 00947, the shared host fixture, and both direct test
suites must use that one value.

For certificate A, Root1 requires role `:producer-census`, domain
`:gravity/sh07-b51-producer-census-certificate-v1`, and checker algorithm role
`:source-first-exhaustive`. For certificate B, Root6 requires role
`:independent-verifier-census`, domain
`:gravity/sh07-b51-independent-verifier-census-certificate-v1`, and checker
algorithm role `:m2-first-independent-exhaustive`. The opposite certificate
remains opaque at each compact root except for the already-normative ordered
certificate-ID joins in the carrier. E4 does not merge the two checker
closures or permit one checker to validate or construct the other's witness.

For either role:

1. `certificate[:inputs][:lowering-product-id]` is a strict H identity and
   equals the complete checker-local `LOWERING-PRODUCT[:product-id]` already
   constructed before the certificate.
2. `certificate[:semantic-witness]` is the complete checker-local `WITNESS`,
   not an ID, projection, wrapper, fixture value, or post-certificate
   annotation.
3. `certificate[:semantic-witness][:content]` equals exactly
   `{:commit certificate[:inputs][:snapshot-commit]
   :tree certificate[:inputs][:snapshot-tree]}`.
4. The certificate claim ID and evidence root retain their literal V9
   equations over the exact complete inputs and other named preimages.
5. The certificate ID is exactly the role-specific existing H operation over
   the complete 19-key certificate with only `:certificate-id` omitted.
   `:semantic-witness` remains in that preimage.

No verifier may accept the 18-key top-level certificate, the 12-key input map,
a missing witness, a missing lowering-product ID, an extra key, or an optional
legacy/new union. Rejection occurs before the carrier is authoritative.

## Exact semantic-witness validation

Root1, Root6, and 00947 must validate the same literal witness rather than
merely checking that `:semantic-witness` is a map or that `:witness-id` looks
like a digest. Its top-level key set is exactly:

```clojure
#{:schema :content :operation-manifest :operation-schedule-id
  :fuel-receipts :l1 :l2 :c2 :c6 :boot2 :boot3 :boot4 :witness-id}
```

Schema is exactly `:gravity/sh07-b51-authentic-lowering-witness-v1` and content
is the certificate C/T pair. The complete map with only `:witness-id` omitted
must rehash under that schema domain to `:witness-id`.

All existing V9 member schemas and identity rules remain mandatory. In
particular, the verifier must:

- require the exact seven-row dense operation manifest in owner order
  `[:l1 :l2 :c2 :c6 :boot2 :boot3 :boot4]` and require its schedule ID to
  rehash from that exact vector;
- require exactly seven dense fuel receipts in the same order, with one
  receipt for each manifest operation and no duplicate, missing, reordered,
  or extra owner/ordinal;
- recompute each operation ID from its complete operation preimage and require
  each receipt's operation ID to select that one operation;
- require every operation's `:lowering-product-id` to equal the certificate
  input `:lowering-product-id`, and require the operation's algorithm source
  root and subject ID to equal their already-recomputed V9 values;
- validate all 34 fixed-fuel measurements, counter keys, measured values,
  bounds, traces, and receipt roots through the existing V9 equations;
- validate the exact `:l1`, `:l2`, `:c2`, `:c6`, `:boot2`, `:boot3`, and
  `:boot4` final stage witnesses, their dense member vectors, member IDs,
  member roots, typed references, postorder child relations, and exact
  construction/evaluation order through the existing V9 schemas;
- require every nested reference to resolve to exactly one permitted earlier
  or explicit postorder member and reject missing targets, duplicates, forward
  parent edges, non-postorder child edges, cycles, and IDs unequal to their
  literal equations; and
- require the seven stage witnesses, operations, receipts, source roots,
  algorithm root, lowering product, and enclosing witness to join
  byte-for-byte to the same checker-local pre-certificate construction.

Hash agreement among caller-supplied strings is insufficient. A document-only
projection, copied peer-checker value, top-level scan substituted for the
authentic owning defn/root, fixture-supplied witness, and witness produced after
the certificate all reject.

## Split bounded profiles

The V9 request's exact 17-key `:profile-bounds` map is unchanged. No numeric
limit is increased, decreased, renamed, removed, or added.

The certificate profile is applied to:

```clojure
(dissoc V9-CERTIFICATE-E4 :semantic-witness)
```

That value must satisfy the existing six certificate ceilings: 2048 nodes,
depth 12, width 35, 64 collections, maximum scalar UTF-8 4096 bytes, and
maximum canonical-reader size 262144 bytes. The witness is not squeezed into
those legacy compact-certificate collection limits. It independently satisfies
the existing carrier ceilings: 8388608 nodes, depth 256, width 65536, 524288
collections, and maximum scalar UTF-8 65536 bytes. Its canonical bytes remain
inside the existing strict response/framing and checker-output slot limits.

This split is structural, not an exemption. The complete 19-key certificate
still must pass strict schema validation, strict UTF-8, one canonical EDN form
plus EOF, duplicate-key rejection, exact identity equations, and the full
witness validator. Applying the certificate ceilings to the witness would
reject the witness architecture V9 already requires. Applying only carrier
ceilings to the small certificate shell would weaken the compact boundary.
Applying both sets to the entire combined value is also rejected.

## Shared fixture and direct verifier tests

The shared test fixture at
`bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_linked_carrier_host_test.clj`
must construct deterministic literal V9 certificate values, including the
checker-local lowering-product ID and complete semantic witness, before it
constructs the linked carrier. It may invoke the existing authenticated pure
constructors but may not synthesize fake C/T identities, omit witness members,
copy certificate A's checker-local output into B, or add a post-certificate
upgrade step. Its certificate IDs, execution-bundle certificate-ID vector,
semantic manifest, carrier root, and all downstream identities must derive
from the resulting complete values.

The Root1 direct suite at
`bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_compact_verifier_test.clj`
and the Root6 direct suite at
`bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_root6_compact_verifier_test.clj`
must execute their real Gravity predicates against the shared V9 fixture.
Each suite must prove:

- its role-specific exact V9 certificate is accepted and the legacy 18/12-key
  certificate is rejected;
- missing or extra top-level/input/witness keys reject;
- wrong report SHA, C/T, lowering-product ID, witness content, witness ID,
  operation schedule, operation ID, fuel receipt join, stage witness member,
  stage root, or certificate ID rejects;
- every operation whose lowering-product ID differs from the certificate input
  rejects;
- the certificate shell passes at each exact certificate-profile boundary and
  rejects immediately beyond it, while the witness independently passes or
  rejects the unchanged carrier bounds; and
- accepted and rejected compact paths never invoke the retired exhaustive
  census scans or the opposite role's checker closure.

The tests must use authentic, recomputed objects. Replacing a field with a
sentinel is valid negative evidence; replacing an omitted witness with a fake
digest or treating a constructor's own expected value as validation authority
is not positive evidence.

## Exact E4 ownership

These five paths are added to I9 ownership:

```clojure
["bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_linked_carrier_host_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_compact_verifier_test.clj"
 "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_root6_compact_verifier_test.clj"
 "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
 "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"]
```

No other path changes allocation. `root6_b.gravity` remains source-only and
read-only because it delegates the checker-B certificate decision to Root6 A
and contains no competing certificate key set or identity equation.

`OWNED-C-PATHS-E4` is the following exact 42-string unsigned-UTF-8-sorted
vector:

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

`SOURCE-ONLY-PATHS-E4` is exactly:

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

The two moved Gravity sources retain their existing source-spec ordinals,
descriptor acquisition, and source-event participation. Moving them from
source-only to owned-C changes only allocation and strict PRE decoding; it does
not remove or duplicate their raw source descriptors. The three direct test
paths were absent from every E3 PRE category and are newly allocated; they do
not move from source-only.

`READ-ONLY-PATHS-E4` is exactly `READ-ONLY-PATHS-E3` with
`producer_a.gravity` and `root6_a.gravity` removed. The three test paths were
not E3 read-only members. No other read-only path changes.

`I9-LIFECYCLE-PATHS-E4` is the exact 42 strings above followed by the unchanged
E3 eligibility receipt, I9 integration receipt, I9 integration discharge, and
content-addressed I9 terminal-record path spec, in that order.

## E4 ownership plan and corrected counts

`V9-OWNERSHIP-PLAN-E4` replaces the E3 ownership plan. It retains the exact E3
key set and every unchanged value except:

- schema is `:gravity/sh07-b51-v9-ownership-plan-e4-v1`;
- I9 allocation paths are `I9-LIFECYCLE-PATHS-E4`;
- read-only paths are `READ-ONLY-PATHS-E4`; and
- plan ID is
  `H([:gravity/sh07-b51-v9-ownership-plan-e4-v1
  PLAN-E4-WITHOUT-PLAN-ID])`.

The E3 ownership-plan tag/domain is removed from `V9-TAG-REGISTRY` and
`H-DOMAIN-REGISTRY`; the E4 tag/domain is added at the same positions.
Registry counts do not change. Allocation owner, ordinal, stage, every other
allocation, semantic-review owner, reservation/handoff/read-only stage sets,
open exit gates, aggregate `:partial` state, and self-ID omission remain
byte-equal to E3.

The exact E4 PRE arithmetic is:

```text
33 document paths + 8 source-only paths + 42 owned-C paths
  + 3 A9-only paths = 86 distinct PRE-C paths

1 show C + 1 C ls-tree + 86 C cat-file operations
  + 1 show base + 1 base BOOT5 ls-tree + 1 base BOOT5 cat-file
  = 91 PRE operations per participant

33 document decodes + 42 owned-C decodes + 3 A9 decodes
  = 78 strict static decodes per participant
```

The four participants remain `[:l :m :a :b]`. Therefore the corrected
`ELIGIBILITY-COUNTERS-E4` is exactly:

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

The equations are `4*5=20`, `4*91=364`, `20+364=384`, `4*78=312`, and
`4*42=168`. Source descriptors remain exactly 52 aggregate, because moving two
source paths into owned-C does not change the 13-row source plan. The source
session remains exactly 13 acquisitions, 13 decodes, 40 events, and one
finalization per A/B/M, with aggregate 39/39/120/3. Q remains five Git
operations. G9 remains exactly 49 operations and its separate post-G9 plan
remains exactly 24 operations.

## Strict 00947 convergence

The already-owned
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj`
must converge on the same E4 rules. Its certificate key set is exactly the
19-key V9 set, its input key set is exactly the 13-key V9 set, and its report
SHA is the exact V9 raw report SHA above. Its validator must perform the same
split certificate/witness profiles, witness validation, lowering-product
joins, and certificate-ID equation required of Root1 and Root6.

Any implementation branch that accepts either a legacy or V9 certificate,
makes witness/lowering-product validation conditional on field presence, or
strips the witness before certificate identity construction must remove that
compatibility. Attempt30 names may remain where required by historical source
and test namespace identity, but they do not authorize the Attempt30 schema.

## Identity rederivation

The five newly owned files change their Git object descriptors when their
final bytes change. Moving the two Gravity source descriptors into owned-C and
adding the three test descriptors also changes the ownership plan even if one
file's bytes happen to remain equal. The implementation must rederive, never
copy or predict:

- all five mode/OID/byte-count/raw-SHA descriptors from final C/T objects;
- `OWNED-C-ROOT-E4`, `PRE-C-PATHS-E4`, the 91-row PRE observation plan,
  PRE discharge, participant roots, and `ELIGIBILITY-COUNTERS-E4` identities;
- `V9-OWNERSHIP-PLAN-E4`, its plan ID, tag/H registries, and every registry
  root or downstream value that includes the replaced E3 tag/domain;
- source descriptor/event roots and products affected by changed
  `producer_a.gravity` or `root6_a.gravity` bytes;
- both checker-local lowering products, operations, receipts, witnesses,
  certificate inputs, certificate IDs, execution-bundle joins, semantic root,
  carrier ID, runtime discharge, E9 objects, lifecycle discharges, and every
  downstream identity that contains one of those values; and
- C/T themselves after all final I9-owned bytes are frozen.

The implementation may not fake a Git identity, reuse a descriptor from an
earlier candidate, seed a certificate or witness ID from expected test data,
or preserve a dependent root merely because its tag name did not change.

The architecture erratum itself owns only this report. Its active governance
reservation and compact-ledger row are coordination metadata and are not
members of `OWNED-C-PATHS-E4`. The 33 document-path count remains unchanged.

## Normative supersession map

The substitutions below are exhaustive. A repeated E3 or legacy literal in
V9 or any prior erratum is replaced only as shown; every unrelated requirement
remains authority.

| Prior authority | Exact E4 replacement |
| --- | --- |
| Root1 legacy 18-key certificate | Exact literal V9 19-key certificate including complete `:semantic-witness` |
| Root1 legacy 12-key certificate inputs | Exact literal V9 13-key inputs including checker-local `:lowering-product-id` |
| Root6 legacy 18-key certificate | Exact literal V9 19-key certificate including complete `:semantic-witness` |
| Root6 legacy 12-key certificate inputs | Exact literal V9 13-key inputs including checker-local `:lowering-product-id` |
| Stale Attempt30 report SHA in Root1, Root6, 00947, or direct fixtures | Exact V9 architecture-report raw SHA `sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd` |
| Certificate ID computed without witness | Existing role-specific certificate H equation over the complete V9 certificate with only `:certificate-id` omitted |
| Certificate profile applied to the complete witness-bearing certificate | Existing certificate ceilings over `(dissoc certificate :semantic-witness)` plus existing carrier ceilings over the complete witness |
| Any optional legacy/E3 union in 00947 | Strict V9-only 19-key/13-key schema and full witness/lowering-product validation |
| Shared direct fixture constructing legacy certificate values | Deterministic authentic V9 certificates with complete checker-local lowering-product IDs and witnesses constructed before the linked carrier |
| E3 `OWNED-C-PATHS-E3` with both Gravity roots source-only and three direct tests unallocated | Exact 42-path `OWNED-C-PATHS-E4`; move `producer_a.gravity` and `root6_a.gravity` from source-only and add all three tests |
| E3 `SOURCE-ONLY-PATHS-E3` count 10 | Exact eight-path `SOURCE-ONLY-PATHS-E4`; source descriptor/event plan remains unchanged |
| E3 ownership-plan tag/domain and plan ID | Exact `:gravity/sh07-b51-v9-ownership-plan-e4-v1` tag/domain and `V9-OWNERSHIP-PLAN-E4`; registry counts unchanged |
| E3 37/83/88/73 owned/PRE-path/PRE-operation/static-decode counts | Exact E4 42/86/91/78 counts |
| E3 aggregate eligibility values 352 PRE operations, 372 total operations, 292 static decodes, and 148 owned artifacts | Exact E4 364 PRE operations, 384 total operations, 312 static decodes, and 168 owned artifacts |

Every other V9/E1/E2/E3 requirement remains normative. In particular, this
report does not change the static V2 accepted/rejected fixture schemas,
13-descriptor/40-event plans, realized-source closure, resolution-plan and C5
authority, unauthorized-cross-fragment rejection, checker-local same-session
handoff, 20-key checker semantic input, ten-key receipted builder result,
seven-row semantic-algorithm source vector, fixed-fuel value or 34-measurement
schedule, E9/G9/P9 topology, publication pins, or final nonclaims.

## Required rejection evidence

In addition to every unchanged V9 and integrated-erratum test, I9 must prove:

- current E4 ownership, source-only, PRE, decode, eligibility, and registry
  values equal the exact literals and equations in this report;
- Root1 and Root6 each accept only their authentic role-specific V9
  certificate and reject the legacy schema before carrier admission;
- 00947, Root1, and Root6 agree on the V9 report SHA, exact key sets, split
  profiles, witness validity, lowering-product joins, and certificate IDs;
- the complete semantic witness is validated recursively under the existing
  V9 member/root/typed-reference rules, with explicit negative coverage for
  every class named above;
- the shared direct fixture constructs no post-certificate witness or
  lowering-product annotation and no checker imports the peer's output;
- accepted and rejected compact paths do not call the retired exhaustive
  census scans; and
- changed source/test bytes and E4 allocation are reflected in authentic C/T
  Git objects and all dependent identities.

The direct compact tests are conformance evidence, not certificate authority.
Their expected values cannot be copied into the validators or used to bypass
recomputation from the actual certificate and witness.

## Lifecycle consequence

This report is a draft architecture correction and owns only its unique report
path. It grants no permission to edit implementation files. Fresh independent
review must confirm the reproduced Root1 and Root6 failures, the exact
19/13-key V9-only schema, the full witness/lowering-product validation, the
split unchanged profiles, the five-path ownership minimum, corrected counts,
identity cascade, exhaustive supersession scope, and nonclaims.

Only after the exact report candidate is independently accepted, admitted,
and integrated may the I9 implementation reconcile onto authoritative main,
add this workstream as an integrated dependency, update only its accepted E4
owned paths, and rederive all identities from final Git objects. I9 remains
draft and cannot freeze while this architecture dependency is absent or while
any legacy certificate shape is accepted.

## Nonclaims

This report does not establish implementation correctness, an accepted I9
candidate, evidence eligibility, integration eligibility, G9, P9, aggregate
SH-07 completion, self-hosting, seed retirement, release, hostile-host proof,
performance authority, or a reduced trusted computing base. Clojure, the JVM,
and Git remain the temporary bounded codec, immutable-object, process,
publication, and observation boundary defined by V9 and its integrated
errata. SH-07 remains partial.

## Acceptance criteria

This correction is acceptable only if fresh independent review confirms:

1. authentic E3 checker-A and checker-B certificates are rejected by the
   current Root1 and Root6 predicates for the exact legacy-schema reasons;
2. the correction retains the one literal V9 certificate schema and adds no
   optional compatibility path;
3. Root1, Root6, and 00947 validate the exact 19-key certificate, exact 13-key
   inputs, V9 report SHA, complete witness, lowering-product joins, and full
   certificate identity equation;
4. the witness validator enforces the exact top-level schema, seven dense
   operation/receipt pairs, 34 measurements, seven stage witnesses, member
   identities/roots, typed references, construction order, and all C/T,
   source, algorithm, operation, product, and certificate joins;
5. the existing 17 numeric limits are unchanged and the split profiles apply
   the compact certificate ceilings to the witness-free shell and carrier
   ceilings to the witness;
6. the only newly owned paths are the two actual Gravity verifier sources,
   their shared host fixture, and their two direct compact-verifier tests;
7. `root6_b.gravity` remains read-only because it introduces no competing
   certificate validator;
8. exact E4 counts are 42 owned-C paths, eight source-only paths, 86 PRE
   paths, 91 PRE operations and 78 static decodes per participant, with
   aggregate 364 PRE operations, 384 total Git operations, 312 static decodes,
   and 168 owned-C artifacts; G9 remains 49;
9. every changed object and dependent identity is rederived from final C/T
   bytes without a copied, provisional, fixed-point, or test-authoritative
   identity;
10. no other V9 or integrated-erratum schema, carrier, topology, operation,
    lifecycle gate, or nonclaim is weakened;
11. the candidate is report-only, owns one unique report path, passes
    architecture/governance/document and exact-diff validation, and is not
    self-reviewed; and
12. I9 remains draft until this report is independently accepted, integrated,
    and reconciled into its exact current-main base and dependency set.
