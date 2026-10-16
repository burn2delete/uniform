# SH-07 SOURCE stage-to-child DATA boundary clarification V1

## Purpose and scope

This clarification closes the in-process return left unspecified by E87 in
`sh07-b51-nonadmitting-source-evidence-entrypoint-architecture-v1`. It selects
Route C: complete immutable DATA passes directly from the fixed SOURCE stage
to its authenticated child. This is architecture, not implementation or runtime
evidence. Route A is not declared impossible; this choice makes no uniqueness,
minimum-fit or performance claim.

The accepted resource amendment R54-64 governs the existing public SOURCE stage
`p15-s23-sh07-b51-stage-source-product-evidence [commit tree resource-lifetime]`.
Only the authenticated SOURCE child private driver calls it, holding the one
sealed child owner. Identity/purpose/thread/process/C/T checks precede semantic
work. The ordinary builder remains `[C T]` under R140-142. No getter, owner
publication cell, dynamic lifetime binding, callback, mode, expected-products
argument, second invocation or extra public operation is introduced.

```json gravity-architecture-authority-v1
{"schema":"gravity/architecture-authority-v1","status":"draft","workstream_id":"sh07-b51-source-stage-child-data-boundary-clarification-v1","invariant_family":"architecture/self-hosting-sh07-b51-nonadmitting-source-evidence-entrypoint-v1","report_path":"docs/artifacts/phase-15/reports/sh07-b51-source-stage-child-data-boundary-clarification-v1.md","base_commit":"a66ccb937282eab93c986019eecf8d9eae30debd","dependencies":[{"id":"sh07-b51-nonadmitting-source-evidence-entrypoint-architecture-v1","required_state":"integrated"},{"id":"sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2","required_state":"integrated"},{"id":"sh07-b51-source-session-handoff-closure-v1","required_state":"integrated"},{"id":"sh07-b51-retained-origin-pure-reader-architecture-v1","required_state":"integrated"},{"id":"c6-source-core-mapping-prerequisite-v1","required_state":"integrated"},{"id":"sh07-b51-source-identity-lexical-closure-amendment-v1","required_state":"integrated"},{"id":"sh07-b51-source-resource-lifetime-amendment-v1","required_state":"integrated"},{"id":"sh07-b51-source-to-fresh-sh06-consumer-compatibility-amendment-v1","required_state":"integrated"},{"id":"sh07-b51-source-parent-tcb-witness-ownership-amendment-v1","required_state":"integrated"}],"historical_references":[],"authority":{"integration_only":true,"release":false,"seed_retirement":false,"self_hosting":false}}
```

## Dependencies and unchanged boundaries

The authority block lists nine integrated dependencies. Registration uses the
accepted main base `a66ccb937282eab93c986019eecf8d9eae30debd`, tree
`4970ff512276b8f979ffc67122127ad840b816c1`. E134-222 and E985-1016 govern private
retention and controls; E650-697 and E822-851 govern response/TCB/counters;
I98-124 governs child-only identity calls; R governs lifetime; P governs parent
witness/process/transport ownership. D0-D3, D6, D8 and D9 remain binding.

The same SOURCE owner is `01a13b8d-509d-7771-b618-06a24933391b`. The existing
23 physical implementation paths, every production file's 250 physical-line
ceiling, 118-resource census, fifteen child early TCB rows, literal loads,
identity domains, capacities, deadlines and ordinary API/diagnostic laws are
unchanged. This report allocates no production resource or source load.

## Exact stage return

The direct return has schema `:gravity/sh07-b51-source-stage-data-v1`, role
`:source-product-staging`, authority `:none`, and exactly these eleven keys:

```clojure
#{:schema :role :content :products :source-object-proofs
  :emission-observations :affected-c2 :validation-controls
  :physical-observations :dependency-materializations :authority}
```

It is fully realized immutable plain in-process DATA with no self-ID. It is
neither the private seven-key retained view nor the final twelve-key response.
It is never serialized, written to an output, accepted by an ordinary API, or
supplied by an external caller. Products keeps its existing exact eleven keys
and distinct ten-key SOURCE final receipt. Existing final wire/output grammars
and child-only identity02 ownership do not change.

In the following equations S is this direct stage return and F is the child's
existing final response. Equality means complete original DATA equality,
including every governing metadata/order/type constraint; it is not equality
of selected roots or copied success flags.

| Stage field | Same-invocation actual origin | Existing final projection |
| --- | --- | --- |
| `:schema`, `:role`, `:authority` | Fixed literals above after successful native construction and controls | Stage schema is not copied into F; F retains its original schema/role/authority |
| `:content` | Independently authenticated actual C/T shared by private construction and expected anchor; exactly `{:commit C :tree T}` | `(:content F) = (:content S)` |
| `:products` | Complete original 017 constructor products and sole SOURCE finalizer receipt, fully joined against private independent expected DATA | `(:products F) = (:products S)` |
| `:source-object-proofs` | All thirteen original acquired dense proof rows, complete commit/tree/blob bytes, ancestors and descriptors in original order | `(:source-object-proofs F) = (:source-object-proofs S)` |
| `:emission-observations` | All 21 original before-final call/return/raw-plan observations, complete arguments/results/assembled values in original order | `(:emission-observations F) = (:emission-observations S)` |
| `:affected-c2` | Three original ordered eight-key rows, complete artifacts/PN/MR and actual owning validation results | `(:affected-c2 F) = (:affected-c2 S)` |
| `:validation-controls` | Actual private retained-validator baseline and all 48 frozen direct/coherent outcomes | `(get-in F [:tcb :counters :validation-controls]) = (:validation-controls S)` |
| `:physical-observations` | Completed original pre-final owning byte-read/decode observations and existing bound declarations | `(get-in F [:tcb :counters :physical]) = (:physical-observations S)` |
| `:dependency-materializations` | All seven original complete six-key rows captured by private native purpose authority before semantic acquisitions, with full C proofs and physical byte joins | `(get-in F [:tcb :dependency-materializations]) = (:dependency-materializations S)` |

Physical-observations is the existing five-key MAP, exactly `:limits
:read-observations :decode-observations :metadata-bound :work-bound`, with its
existing exact nested grammars and five physical kinds. It is not a new row
vector, counter key, resource authority or exact measured total of all probes.
Failed/unobserved work cannot become a completed observation or zero.

Validation-controls has exactly `:accepted-baselines :rejected-mutants :records`.
Counts are 1 and 48. All 48 dense six-key records retain the E985-1016 family and
direct/coherent order, original exact complete expected/actual diagnostics,
rejection precedence and status `:rejected`. Baseline failure, unexpected
success, no-op alteration, incomplete descendant rehash or diagnostic mismatch
rejects the invocation. No planned record or passed Boolean supplies evidence.

The source-session counter projection equals the complete original final
receipt's `:counters`: acquire/decode/event/finalize counts 13/13/40/1 and exactly
one finalization. The physical and control projections add no new counter key.
The child independently supplies its required actual current identity,
classpath/request/method and IPC observations under the same owner. Parent and
supervisor retain their independent actual process, future-exit, stream/drain,
cleanup, lease/lock release and final-result observations. Stage DATA cannot
satisfy or predict those obligations.

## Construction, disposal and child sealing requirements

The original shared SOURCE native pipeline runs once. Before finalization it
captures every required original constructor/owning validation/plan/origin
result and independently copies the complete private expected DATA. Neither
caller DATA nor the later candidate/mutant supplies that expected value.
After the sole finalization, the real private retained validator checks the
complete seven-key baseline and all 48 mandatory mutations using retained DATA
and original permitted pure joining equations. No extra semantic acquisition,
constructor/emitter call, source read, owning verifier/Q/RC initialization or
filesystem work is introduced by the controls or stage-to-child handoff.

Only after actual matrix success may S be constructed from the complete
validated actual values. Every reachable returned value passes the existing
plain-data, slot and nested bounds. Mutable byte arrays/buffers become complete
immutable byte vectors as the owning DATA grammar permits. No lazy sequence,
record, atom, function, owner, ticket, live host resource, private expected
value/closure or mutation state escapes or aliases S. Relevant complete
metadata/order/type laws remain intact. Semantic collector/session tokens,
private expected copies/closures and mutation work state are dropped on both
return and throw, before a successful stage DATA return. Construction handles
are retired at their owning boundary; any still-live registered resource stays
private to the lifetime owner, never in S.

This disposal does not retire or replace the resource owner. The same lexical
owner remains active through the child's required post-stage identity02 calls,
complete current TCB assembly, postchecks, original H/W encoding and response
write/flush, then closes exactly once under the existing work/retirement
clocks. Failure preserves the original Throwable, suppression and interruption;
failed/unobserved retirement produces no success.

The private child accepts only the direct S from that one fixed call. It checks
closed keys/types/literals/C/T and all complete field shapes and cross-joins,
then builds the existing final response and request binding. It remains the
sole SOURCE identity02 caller. Required acquisition-free original final H/W,
shape and join validation remain permitted and required: response-ID hashes
the complete final response with only `:response-id` omitted. All owning bounds
precede those operations. No semantic repair, reacquisition, proof cache,
expected-from-candidate computation or private Var escape is permitted.

## Outputs and conformance

The only new architecture outputs are this report, its same-owner lifecycle
record and ledger row. No intermediate DATA output/file, new final response
field, product key, physical resource, TCB row, public acceptor or authority is
created. Existing parent/supervisor output ownership remains unchanged.

Implementation acceptance requires fresh exact code/resource/line/closure fit,
ordinary positive and negative preservation, genuine same-invocation field
origins and complete bounds, real baseline plus 48 diagnostic controls,
deep immutable return and semantic-state disposal, same-owner lifetime through
encoding/flush and actual retirement, and all independently observed parent
and supervisor obligations. Every executable method still requires both fresh
independent method accepts, coordinator authentication and concrete GO, one
actual execution, both fresh actual reviews and coordinator acceptance. This
clarification proves none of those results and grants no native, performance,
release, self-hosting or seed-retirement authority.
