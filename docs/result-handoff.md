# Retained result handoff

This is the sole detailed coordination-overhead cleanup backlog and usage
contract. [Cleanup navigation](component-roadmaps/README.md) links the other
outcomes; their component tasks and acceptance contracts remain authoritative.
This tool is supplemental repository tooling under D1, D9 and
[development verification](development-verification.md). It changes no compiler,
release, independent review, host broker or operation authorization contract.

## Finite audited backlog

The audit used main `176c4ee65bb3ceb545f51edfaa599902030a0cb7`, open PRs
359/370/374/378/383/421 and direct read-only responses from their current owners.
The component task projector was already merged; its migrations remain with
that owner. Raw evidence formats differ and must remain owned by their producers.

| ID | Avoidable step and exact interface | Dependencies | Acceptance evidence |
| --- | --- | --- | --- |
| CO1 | Repeated EDN-to-prose extraction: `tools/result_handoff.clj` selects explicitly named fields into one portable EDN packet | Producer retains original EDN; consumer reviews the selection plan | Real retained-receipt projection and roundtrip; no unselected private paths or repeated count aggregation |
| CO2 | Repeated receipt hashing and ambiguity about provenance: the reader hashes the exact parsed bytes and checks caller-pinned SHA256; each field retains receipt ID and key/index path | Expected digest obtained from retained evidence; before/after and process facts explicitly selected | Digest mismatch, missing paths, duplicate identifiers, malformed EDN and type errors reject; process exit stays attributed producer reporting |
| CO3 | Repeated review handoff assembly: one packet can include owned source selectors, immutable heads/trees, raw artifact digests, operation outcomes and unrun boundaries | Source/test owners supply fields; reviewer owns disposition; broker owns live leases | Failed, uncertain and unrun observations survive unchanged; raw argv is digest-bound without copying private argv; no commands, retries, publication or acceptance inferred |
| CO4 | Parent repeatedly requests the same receipt extraction: versioned plan plus packet supplies the already selected facts and original receipt identities | Independent review of exact source and tests, ordinary draft publication | Focused positive/negative tests, actual retained evidence consumption, language/document gates and independent review before integration |

Completion requires all four demonstrated interfaces, review and integration of
the declared increment. It does not mean zero necessary communication. Cloud
thread routing and private Library delivery remain platform interfaces; this
command cannot repair them. Live lease coordination remains with the existing
brokers. It neither collects dashboard state nor creates a competing task ledger.

## Producer and consumer workflow

1. Preserve the original receipt and known process outcome. Do not rerun a failed,
   restricted, denied or uncertain operation to obtain a more convenient record.
2. Prepare a private EDN selection plan. Pin every receipt's SHA256. Select the
   exact source/test head/tree, clean observations, argv digest, actual process
   outcome, observer validity, individual test counts and pending/unrun fields
   that the reviewer needs. Select original fields rather than copying values.
3. Run `clojure -J-Xmx256m -M tools/result_handoff.clj PLAN.edn`. The command reads
   only; stdout is the packet. Run under the existing resource policy. Keep the
   plan private because it contains local receipt paths. The packet includes its
   SHA256 so the reviewed plan version is identifiable.
4. Give the reviewer the packet and accessible immutable original evidence.
   A local file link alone is insufficient for a remote reviewer. Keep artifact
   version/Library identifiers in the existing delivery channel; do not publish
   private paths, raw logs, credentials or internal task names. Review selected
   labels and selectors before public use; this tool is not a secret detector.
5. Reviewer and publisher use their existing exact-source review, expected-head
   checks and publication readback. No extra parent acknowledgment is added.
   A packet grants no review, merge or execution authority. A saved producer
   exit code cannot prove the actual process exited, and no field authenticates
   an independent reviewer. Missing evidence remains missing.

Example private plan (replace the file and digest with retained evidence):

```clojure
{:receipts [{:id "process" :file "receipt.edn"
             :sha256 "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}]
 :fields [{:label "tested-head" :receipt "process"
           :path [:before :commit] :type :oid}
          {:label "reported-exit" :receipt "process"
           :path [:exit-code] :type :integer}
          {:label "command" :receipt "process"
           :path [:command] :type :argv-digest}]}
```

Supported projections are `:oid`, `:sha256`, `:integer`, `:boolean`, `:status`
(unqualified keyword), `:selector` (public token), `:clean-status` (empty Git
status becomes true), `:argv-digest` and `:counts`. Counts are copied exactly
from one selected map, never summed or interpreted as completion. Select each
scope separately. Missing fields fail closed: choose the original field or
leave that observation explicitly unprovided; do not fabricate defaults.
No free text or raw maps are copied. Clean status is an observation of the
selected producer field, not a fresh Git check.

The EDN reader rejects trailing forms, duplicate keys, unknown tags, invalid
UTF8 and a symlink receipt leaf. It hashes the same bytes it parses and has a
16 MiB host capture limit. It is not a sandbox for hostile filesystem parents
or proof of producer truth. Keep evidence in its existing trusted location.

## Validation and current boundary

`clojure -J-Xmx256m -M tools/result_handoff_test.clj` exercises the standalone
projection and reader, without loading the compiler or running a proof. The
finite increment also requires the language boundary and structural docs gate.
No C2/C7/C12/Native/MIR acceptance execution is needed for this tooling change.
Independent review and integration are pending until an exact candidate and
validation receipt are supplied. No cleanup completion is inferred from this
document or the existence of the packet format.
