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
3. Run `clojure -J-Xmx256m -Sdeps '{:paths ["tools"]}' -M -m result-handoff PLAN.edn`. The command reads
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

The library file has no automatic dispatch, including when its caller has
nonempty `*command-line-args*`. The command above explicitly invokes `-main`
through Clojure's `-m` entrypoint; direct script loading is not a CLI invocation.
The byte reader enforces its existing 16 MiB host capture limit on actual reads,
including growth after open. It never sizes an allocation from a changing file
size. It reads at most one extra byte to distinguish EOF from overflow, and
hashes the exact captured bytes it decodes. This is not a promise of an immutable
filesystem snapshot; expected receipt digests still bind the retained input.

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

The actual no-argument script control subsequently found that both `seq` and
`some?` guards alone can silently skip `-main`: Clojure supplies nil for an
argument-free script. Candidates `cdbd9d4ff7ad5ca041d619cd5e1338f0f08ef7dd`
(tree `549403dd25b63e7c65bab11512e22a937501d145`) and
`5b429435a621f3efa368783c2aab4372c5c43d26` (tree
`6a96f3f47b9d087294008c7d3b900d95919c1226`) each failed 1 of 56 assertions;
those failures remain historical. Their document and language gates passed.
The corrected guard recognizes the canonical direct `clojure.main` script path;
load-file as a library remains inactive. Actual subprocess controls exercise
both a valid plan and a missing plan. No new operation runner is introduced.

Final tested source is `eda135df4b07a4a164a1b0e6136758e513e7ed45`, tree
`7fe7ddfe7ecea46f0ba3ed7fd4cab2d56d718861`. With identical commands and broker
profile to the table above, the focused gate passed 4 tests / 56 assertions in
1756 ms, docs passed 240/19 in 1364 ms, and language boundary passed 6 tests /
11 assertions in 1103 ms. All natural exits were 0, no timeout occurred, and
before/after commit/tree and empty Git status matched. Tool SHA256 is now
`ab096aa60938e2e5354d7118138eae162b072c8eeafe272b43b300c7c2312c54`, Git blob
`eb1bfa952a1d97fb41a394c363202c1f4dd6dae9`; test SHA256 is
`c538ad77a62472040f4f75dea957b521aa845ee18c36e46b01a202261fd5d140`, Git blob
`322d0e62ffd5d2caecfa9ee0eadd6e2a298eba6b`. Projection and reader functions
are unchanged from the actual retained-receipt consumption above; that evidence
is reused with its original provenance. Independent review of this corrected
entry and subprocess controls remains pending at this receipt revision.


## Narrow review corrections and remaining consumer boundary

Cloud review of frozen head `58ad2375fc735ba5afdeca518ae3a99bfe92295a`
identified two limitations in that revision: inherited nonempty command-line
arguments could dispatch the CLI during library loading, and repeated size
observations did not make the 16 MiB capture bound safe against growth. Its
validation and retained receipts remain historical. The successor removes
implicit dispatch entirely in favor of explicit `-m result-handoff` and counts
actual captured bytes with fixed-size buffers. Cheap controls exercise inherited
arguments, input that continues growing, an exact-limit read and an oversized
file. No compiler tests or producer operations are needed for these corrections.

The six retained-receipt readback checks above are producer-side checks. The
independent peer reviewer received selected facts in prose and statically
reviewed source/test bytes; this was not independent consumption or
authentication of original portable packet bytes. CO4 remains open until the
actual bytes have reached an independent consumer through an authorized channel
and the consumer has assessed the declared handoff behavior. A public disclosure
attempt was rejected and created no comment; no alternate transfer has been
used. No preexisting private artifact supplies the held packet bytes. Source
review, counts, a merge or a checksum alone cannot close this boundary.

## Final finite disposition

Independent cloud review approved corrected source
`3569fe974788d195ff3c3f48d69f4a21ddc37f53`, tree
`c7698433349a62b07943e8f8b30067e07b85fdb3`. Its actual focused controls passed
6 tests / 62 assertions; documents passed 240/19; language boundary passed
6 tests / 11 assertions. All exits were naturally 0, with clean identical
bookends and existing normal brokers. The first broker timeout started no
validation child and remains nonexecution history.

The parent separately authorized private local independent consumption. An
independent laptop reviewer read both original receipts, plans and packets,
used an independent digest/path/type oracle, and consumed the plans through the
actual tool. All 18 positive-case and 9 historical failure-case fields matched
the originals; both actual outputs exactly matched retained packet bytes. Failed
exit, capacity nonexecution, absent whole-source authentication and unpublished
state remained intact. This verifies consumption for these two real handoffs;
it does not reproduce the producer operations or authenticate other receipts.
Private contents and review records remain private.

[PR 428](https://github.com/burn2delete/uniform/pull/428) merged normally by
squash at `ef55fe3b58dbf17359e36df5d418730264074408`, tree
`0e422f7addb6a281e23598c5bcdb4dccfc8d63e7`. That entire tree matches the
integration candidate. Source/test bytes remain identical to the reviewed,
tested successor above; exact integration preflight passed over current main.
The remote branch and evidence links are retained. No producer or compiler
operation was replayed for consumption or integration.

CO1 extraction, CO2 captured-byte/provenance binding, CO3 reusable owner handoff
and CO4 independent consumer use plus reviewed integration are closed for this
finite tooling outcome. This is not universal receipt compatibility, compiler
acceptance or platform routing repair. Explicit field selection, public-safety
review, genuine independent acceptance, single-writer coordination and host
controls remain necessary; unfamiliar receipt layouts require their own checks.
The earlier public disclosure remains denied and unexecuted. Authorized private
local review made that publication unnecessary; no held payload was uploaded or
transferred through another external route.
