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

## Observed implementation receipts

The tested candidate is `81f5002766867de6e5f40b03b14801d9784e18b3`, tree
`4017dc86f4502e7e85d17563df62df21bfc769f3`, based on the audited main above.
Git commit/tree and empty tracked/untracked status matched before and after.
The existing repository-common and host brokers admitted normal-class execution;
no heavy proof was run. The following children exited naturally with no timeout:

| Exact command | Result | Elapsed |
| --- | --- | --- |
| `clojure -J-Xmx256m -M tools/result_handoff_test.clj` | Exit 0; 4 tests, 51 assertions, zero failures/errors | 649 ms |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0; 240 docs, 19 phase indexes, ASCII, no placeholders | 1378 ms |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Exit 0; 6 tests, 11 assertions, zero failures/errors, one namespace attempted, none skipped | 1046 ms |

The exact tool SHA256 is
`2e4e2a3a4331551992db2aa8386837ec0214cfaa7ff96855a4ba49a9d71c357e`;
test SHA256 is
`e94779d1560a66a749ed702eb5f942057a9ea9d004f02d6ec2e5156971a8e8fa`.
Git blobs are `ba97b93e3f93d4e982ed502677f07d4880c2fc06` and
`9fcc23880aaacda721edb8928ffd681592442d73`, respectively. A subsequent
receipt-documentation-only commit does not represent another execution.

An actual retained owner diagnosis was independently selected through `-main`
and read back with six assertions, natural exit 0. It was not replayed. Original
receipt: 1289 bytes, SHA256
`319746b2a20319373f49f3897f03d2aa4e707d2167529c0426a1e7870fec4d89`.
Private selection-plan SHA256:
`7227886aa639abc306389714d1eb7a74cb557e665eee0b38afe648fd14e7fab7`.
Public-safe packet SHA256:
`d04cae3819f4e7328b59441f72232cd29f62205acee97eae01413a14fd18f1d7`.
The packet retains `:authority :none`, `:new-execution false` and
`:observation-source :retained-producer-receipts`. Selected paths/values were:

| Original receipt path | Preserved observation |
| --- | --- |
| `[:baseline :commit]` / `[:baseline :tree]` | `4c5aac01190e52fe013909437987071826492d5e` / `f26326954ebeb71bf807a7177257242e776f87eb` |
| `[:baseline :status]` | Empty producer Git status, projected as true |
| `[:matrix :exit]` / `[:matrix :elapsed-ms]` | Reported exit 1 / 24880 ms |
| `[:matrix :result-sha256]` | `4914ab9362a3e6b4f2e3b3b22796df015994343ec78ae1f38b863de84c512ce6` |
| `[:capacity-nonexecution :child-started]` | False |
| `[:whole-source-authentication]` / `[:publication]` | False / false |

This is consumption of failed and nonexecuted historical observations, not
acceptance of the product or independent reproduction of the producer result.
The synthetic CLI roundtrip also rejects changing the receipt after pinning its
bytes. Private original evidence and paths remain with the producer.

An independent assurance reviewer inspected the initial exact source candidate
`405878a22728ab655987662616a4a027a347696f`, tree
`c8e32cc1731cb6998c040024e58084ac4ff27baa`, finding no blocking defect within
the documented trusted-evidence boundary. That source blob is unchanged in the
tested successor. Review of the added CLI test and final documentation is still
pending at this receipt revision. The review is static; it does not claim an
independent test rerun. [Draft PR 428](https://github.com/burn2delete/uniform/pull/428)
preserves unfinished review/integration work. Full compiler, authoritative
migration, restricted executions, runtime safety and release acceptance are
unrun and outside this tooling increment.
