# SH-07 SOURCE resource lifetime amendment V1

## Purpose and decision

The direct human instructed, "Get the source owner to implement the proposal",
then "Dont hold anything, get it all done." This supersedes earlier human
implementation, execution, native and performance holds. It preserves exact
ownership, contract conformance, independent reviews, finite resource admission
and concrete coordinator GO for each new execution. Failed attempts stay spent.

This narrowly amends the integrated SOURCE entrypoint and identity contracts.
One explicit child-owned resource lifetime crosses five fixed SOURCE boundaries.
A sixth rejection-only boundary authenticates its actual invocation reference.
The already allocated parent enforces the child hard deadline and observed OS
termination. There is no child Runtime.halt or Thread.stop policy.

This document records architecture only. It does not claim implemented code,
final line fit, complete cold-path counts, ordinary equivalence, native products,
runtime retirement, implementation acceptance or integration. The same sole
SOURCE owner remains 01a13b8d-509d-7771-b618-06a24933391b. Coordinator alone
integrates eligible candidates and wakes downstream work on actual prerequisites.

```json gravity-architecture-authority-v1
{"schema":"gravity/architecture-authority-v1","workstream_id":"sh07-b51-source-resource-lifetime-amendment-v1","invariant_family":"architecture/self-hosting-sh07-b51-source-resource-lifetime-v1","report_path":"docs/artifacts/phase-15/reports/sh07-b51-source-resource-lifetime-amendment-v1.md","status":"draft","base_commit":"0384e839e9bd33dea821b5dcf2f84118cbf5485e","dependencies":[{"id":"sh07-b51-nonadmitting-source-evidence-entrypoint-architecture-v1","required_state":"integrated"},{"id":"sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2","required_state":"integrated"},{"id":"sh07-b51-source-session-handoff-closure-v1","required_state":"integrated"},{"id":"sh07-b51-retained-origin-pure-reader-architecture-v1","required_state":"integrated"},{"id":"c6-source-core-mapping-prerequisite-v1","required_state":"integrated"},{"id":"sh07-b51-source-identity-lexical-closure-amendment-v1","required_state":"integrated"}],"historical_references":[],"authority":{"integration_only":true,"release":false,"self_hosting":false,"seed_retirement":false}}
```

## Dependencies and unchanged contracts

Accepted base is `0384e839e9bd33dea821b5dcf2f84118cbf5485e`, tree `9b372af92d9f3313b38344280209cbd24a2f9606`. The six integrated dependencies in the authority block remain mandatory. D0-D3, D6, D8, D9, L1/L2, C2/C5/C6, BOOT1-BOOT8, TEST10/11/13, R6, GOV2/GOV4/GOV6 and retained V9/E1-E5 contracts govern implementation.

The original architecture report (80758 bytes, SHA-256 `b8be4deb30ecdd175c481c46679649ed74edb2e15fd19fb4ec57e15b1afbdce5`) and identity amendment (19205 bytes, SHA-256 `fc6f7b68d8df6aeb0195966051df901a9f6caebe9c0702edf2b0f6105d34d204`) remain unchanged. This amendment supersedes only the SOURCE resource-channel restrictions explicitly listed here. Every ordinary public API, parser, diagnostic, default, output/wire grammar, semantic census, fifteen-row early TCB, 118 data resources and all existing caps remain binding.

## Requirements and exact allocation

The implementation workstream remains `sh07-b51-nonadmitting-source-evidence-entrypoint-implementation-v1`, with its existing twenty-three physical paths. The resource subset is exactly the following fourteen existing paths; parent is the explicit additional resource owner. This does not create a second production writer or transfer parent I9 ownership. Metadata adoption must preserve histories, all accepted ordinals/terminal bytes and append-only parent disposition.

- `bootstrap/clojure/src/gravity/tools/sh07_b51_source_product_evidence_child.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child/03-git-operation-core.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child/04-validate-complete-classpath.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity/01-checker-specs.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity/02-source-product.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00582-p15-s23-stage2-construct-emitted-core-plan.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker-04.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker-17.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00808-sh03-reader-expected-source-byte-count.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00850-sh03-c3-precomputed-c2-verify.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00857-sh05-macro-source-relative-path.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00645-p15-s23-stage2-runtime-artifact-source-relative-path.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-002-p15-s23-sh02-component-safe-source-path-through-p15-s23-sh02-source-binding-inputs.clj`
- `bootstrap/clojure/src/gravity/tools/sh07_b51_source_product_evidence.clj`

No excluded Git runner, support helper, identity facade/03, load-order manifest, Gravity source or new file/fragment/load resource is allocated. The parent is already inside the twenty-three-path reservation and has no new public operation or process role. All production files stay at most250 physical lines. Exact final fit/load/counter/ordinary evidence precedes execution or acceptance; failed fit requires a separate exact amendment.

## Fixed SOURCE signature and seal changes

| Owner | Function | Exact new arguments |
| --- | --- | --- |
| checker03 | `pre-manifest-tcb-for-source!` | `[options resource-lifetime]` |
| checker04 | `validate-complete-classpath-for-source!` | `[options codec resource-lifetime]` |
| identity02 | `source-closure-manifest-for-source` | `[source-authority relative-paths resource-lifetime]` |
| identity02 | `loaded-source-fragments-for-source` | `[source-authority namespace-records resource-lifetime]` |
| collector | `p15-s23-sh07-b51-stage-source-product-evidence` | `[commit tree resource-lifetime]` |

The sixth SOURCE-only boundary is `gravity.tools.sh07-b51-source-product-evidence-child/authenticate-source-resource-lifetime! [candidate]`. It returns nil or throws the stable SOURCE lifetime diagnostic. It compares reference identity before any candidate method/data access. It supplies no owner, parser, product, policy, method, resource cap or semantic authority. Ordinary callers do not gain a resource context. The fixed five SOURCE boundaries are its sole production callers; the private driver already holds its lexical reference.

The exact interface declaration and JVM descriptors are normative:

```clojure
(definterface SourceResourceLifetime (^void checkpoint []) (^Object reserve [^clojure.lang.Keyword unit ^long maximum]) (^Object git [^clojure.lang.Keyword operation ^clojure.lang.IPersistentVector arguments ^long stdoutMaximum]) (^Object readSource [^java.nio.file.Path path ^long maximum]) (^Object register [^clojure.lang.Keyword kind ^Object resource]) (^void retire [^Object ticket]) (^void close []))
```

- `checkpoint()V`
- `reserve(Lclojure/lang/Keyword;J)Ljava/lang/Object;`
- `git(Lclojure/lang/Keyword;Lclojure/lang/IPersistentVector;J)Ljava/lang/Object;`
- `readSource(Ljava/nio/file/Path;J)Ljava/lang/Object;`
- `register(Lclojure/lang/Keyword;Ljava/lang/Object;)Ljava/lang/Object;`
- `retire(Ljava/lang/Object;)V`
- `close()V`

gravity.tools.sh07_b51_source_product_evidence_child.SourceResourceLifetime is the nominal interface. The generated reify class name is not an authority check or semantic identity.

Interface declaration follows ns/import and precedes every type hint/reify use in the existing child source. Exactly one lexical let after private definitions binds state and opaque owner, then defines only fixed validator and -main closures over those references. Helpers accept private state explicitly; there is no Var/Atom containing an accessible owner, no runtime ns-resolve/privateVar access and no extra require/load-file/resource.

Inert namespace evaluation may generate the interface and reify/function classes through the pinned ordinary Clojure compiler DynamicClassLoader only. No AOT/class output, custom loader, classpath classfile or extra physical load resource is created. All generated code is derived from authenticated child source and pinned Clojure/JVM; the existing child early TCB row covers that source. Child resource residence still requires one exact .clj and absence of .cljc/__init.class; all other lexical/physical classpath validation stays complete.

The interface class has the exact binary name/descriptors above. The reify implementation name/loader sequence may vary across fresh JVMs and must not enter H/W/R, semantic source proof or a comparison coordinate. Any unexpected source/AOT provider or manually supplied replacement class rejects. A source method must retain actual class/load provenance in its existing host-TCB method evidence without adding a semantic TCB row or wire field.

One state atom and one opaque owner are created in the same private lexical let; the atom is private mutable state, not an owner publication cell. Validator executes identical? against captured owner before any candidate method, field, hashCode, equals or data access. It returns nil only after checking active phase, retained actual process identity, registered main Thread identity and work deadline. Every nil/foreign/copy/interface imposter rejects before candidate invocation.

All interface methods operate on the state lexically captured by that exact owner. Public SOURCE boundaries validate candidate once before any work; methods additionally check thread permission/lifecycle per operation. Tickets are fresh Object identities retained privately in the owner registry, not caller maps or completion booleans. Wrong/stale/retired/foreign tickets reject before use.

The threat model excludes arbitrary Java reflection/Unsafe/native agents/Var mutation as admitted SOURCE inputs; this fence is not a sandbox against trusted seed corruption. Existing actual SOURCE role/argv/env/C/T/Qs, classpath and construction authority remain separately checked.

## Closed private resource operations

### checkpoint! [owner]

Check active owner identity, current registered SOURCE worker/process and unchanged monotonic deadline. Denial only; no parser/product/method authority.

### reserve! [owner unit maximum] -> reservation-ticket

Charge a fixed nonnegative conservative amount against the owning unchanged unit ceiling BEFORE work. Units and callsites are closed by the reviewed method; no caller cap/deadline, refill, refund or success credit. Sub-reservations consume the same ticket rather than resetting budgets. Actual observations are retained separately.

### git! [owner operation arguments stdout-maximum] -> original physical Git operation result

Implemented privately by the child using its existing raw Git machinery. Owner derives fixedcwd/closedenv/absoluteGit from the actual invocation. Accept only the original literal SOURCE Git operations show/ls-tree/cat-file/rev-parse with exact argument grammar and authenticated coordinates; no executable, environment, cwd, callback or arbitrary command parameter. Before spawn reserve/cap; immediately retain Process/streams/tasks before waiting. Use min30000ms/remainingchild and original bounded drains/cleanup. Output has the existing helper physical-operation result fields, not a new wire schema. Raw/Bootstrap callers select stdout bytes from that checked result.

### read-source! [owner absolute-path maximum] -> byte-array

Fixed physical byte acquisition only, using bounded stream/channel I/O and immediate resource registration, at most min(the original owning byte ceiling, SOURCE1MiB for semantic/dependency source or SOURCE16MiB for classpath source). Caller independently authenticates containment/C object/purpose and preserves original pre/post file-state and verification checks. No source parsing, decoding, semantic expectation, cache or path discovery is delegated.

### register! [owner kind live-host-resource] -> ticket

Closed typed process/stream/channel/JarFile/task registrations from the fixed internal route only. Reserve a slot before acquisition and retain the actual handle immediately after creation before dependent reads/waits. No function, observer, resolver or semantic DATA argument. Git process registration stays internal to git!; this operation cannot launch commands.

### retire! [owner ticket] and close! [owner]

Controller inspects actual host handles and performs close/cancel/destroy/bounded joins; caller cannot report a Boolean completion or release as authority. Preserve original Throwable, suppression and interruption. Final snapshot occurs after retirement; failed/unobserved cleanup never produces success.

All operation limits are independently clamped by the owner; no caller-supplied executable, environment, cwd, callback or budget is accepted. One owner is created per child before first raw work and retained through raw/canonical/identity/native construction, all controls/postchecks/encoding and once-only retirement. Parent ownership is separate. No owner or ticket is serialized, hashed into products, passed through files/IPC, reused or copied across children.

## Before-work propagation and physical acquisition

child: private child main/driver -> create one owner -> preload-source-entry![owner] -> process-options!/raw-residence!/rawGit[owner] -> canonical-source-preflight![owner] -> the five fixed SOURCE boundaries -> child postchecks/48controls/encoding -> close![owner].

Raw preload no longer owns/retire-resets a separate executor. Its existing Git implementation is the single SOURCE physical runner inside owner.git!. The outer driver retires it once. No public main/getter is implemented by this proposal.

checker03: pre-manifest-tcb-for-source![options owner] -> private source-git-operation-core![options owner op args max] -> owner.git!; private source-git-path-object-core![options owner path max] factors the existing tree/blob grammar and calls that operation; private source-materialized-source-core![options owner path max] retains existing path checks then owner.read-source!.

The SOURCE route MUST NOT call excluded checker02/run-project-free-process!. Ordinary git-operation-core! and ordinary materialized-source-core! keep their original route and public behavior. No private Var rebinding, callback runner argument or duplicated metadata parser.

checker03/checker04: validate-complete-classpath-for-source![options codec owner] -> private full-classpath core with fixedsource purpose and explicitowner -> source-git-operation-core!/source-materialized-source-core!; sourceJAR helpers register JarFile and every entry stream before enumeration/read; source-metadata-context!/source-manifest-entries! receive owner privately.

The shared private full-classpath core may add an internal owner argument with ordinaryfalse/nil branch preserving the existing ordinary route. All required03/04 bytes/metadata/output fields retained. No source fallback to the excluded runner.

identity01/identity02: Both public SOURCE identity wrappers[existingDATA owner] -> source-identity-context![source-authority owner] -> source-metadata-context![source-authority jars owner] -> header/full source-lexical-forms! and manifest parsing. Context carries the same owner privately into source-closure-manifest-core/source-product-core/dependency-manifest/loaded-source-fragments-core/load-order-paths/source-read-forms and every recursive visit.

Check/reserve before each bounded decode/form read/hash/recursive visit and retain existing local AST limits. The owner never enters returned forms, forms metadata, source authority or closure output. Ordinary nil-context APIs/readers remain unchanged. This is a propagation design, not a complete work-count proof.

collector/source_pipeline/acquisition: Child -> p15-s23-sh07-b51-stage-source-product-evidence[C T owner] in00582 -> private build-receipted-bundle-core-products*[true C T owner] in017 -> new-source-collector![C T owner] BEFORE13acquisitions -> private pipeline*[true C T owner] -> open-source-product-session[C T owner]/open-source-session-core[true C T owner] in004 -> acquire-source-entry/object-proofs/assert-content-coordinate/ls-tree helpers with explicitowner.

Existing ordinary public builder[C T] and privatefalse branch pass no SOURCE owner and keep original acquisition/verification behavior. No third-argument method/role override; method and construction authority still authenticated internally. Existing constructor collector binding may retain the explicit owner in its private state, not a new global/dynamic lifetime cell.

acquisition: Private source-git-bytes[owner arguments maximum] calls owner.git! and selects actual stdout bytes. Every SOURCE Git call in object proof, commit/tree assertions,ls-tree and duplicateblob route uses it. Shared proof/grammar code is factored once; an internal owner-aware private arity dispatches nilordinary calls to the original p15-s23-sh07-b51-git-bytes.

SOURCE MUST NOT call excluded Bootstrap linker03 Git runner or consult linker02 one-hour limits. Both excluded files remain read-only. No source proof/parser algorithm duplication or changed13entry order.

collector and five physical byte-read owners: Existing physical-hook identity/phase guards -> owner.reserve! BEFORE containment/probes -> controlled read bridge in00582 -> owner.read-source!/typedregistration -> ORIGINAL byte/hash/file-state/decode/parser/verification code -> existing read/decode completion observations.

Only the five listed physical byte-read owners gain SOURCE-only controlled acquisition/registration; ordinary reads retain the exact existing operation. No extra source/decode/event, emitter/C11read or parser is introduced. Unregistered cold-load reads outside the constructor remain HOLD.

support/physical-path![root relative actual] remains unchanged and excluded from this proposed edit subset. Collector00582 must not call it before reserving its bound.

Before each physical-path! call reserve a conservative1088metadata units: at most512component attribute probes plus512component-walk units plus64fixed path/shape checks, under the existing4096-character and512component input guards. The method must validate this counting convention against the actual helper; if more operations are reachable, HOLD, not an increased cap.

The returned component count is an actual partial measurement, not a second charge or a refund. Keep reserved and observed quantities distinct; failed calls consume their conservative reservation. Global reservations can cause earlier SOURCE resource rejection; no positive fixture fit is asserted.

Root discovery/File.isFile and other unhooked work BEFORE the hook require a complete conservative phase reservation at the SOURCE facade before invoking the original constructor. That static cold-path expansion remains unproved, so no constructor may run on this proposal alone.

Reader/compiler InputStream and SH02 channel handles can be registered around their original byte acquisition. Syntax/macro Files.readAllBytes hides the stream: propose only their SOURCE byte-acquisition branch use owner.read-source!, leaving ordinary Files.readAllBytes and all original before/after validation, decode, parsing, initialization and diagnostics unchanged. This is an explicit containment-permission exception, not permission to replace semantic parsing.

The explicit sixth validator and parent containment replace any conflicting five-boundary/thirteen-owner wording above. All ordinary branches remain original; SOURCE bypasses both excluded Git runners through the child owner. No old per-facade hour/executor can reset the lifetime.

## Child atomic lifecycle and work deadlines

storage: One invocation-private Clojure Atom containing an immutable state map; compare-and-set! identity expected-state -> next-state is the only phase transition. Atom/AtomicLong operations delegate to pinned JVM volatile/CAS sequentially consistent semantics, JVM process scope. No unsynchronized phase mutation or caller supplied completion flag.

states: NEW

ARMED

RAW

CANONICAL

CONSTRUCTING

VERIFYING

ENCODING

CLEANING

QUIESCENT

CANCEL_REQUESTED

FAILED

RETIRED

activation: Fixed -main [& argv] first compares the ten passed strings with *command-line-args*, exact flag order, one source-product-staging role and four40hex coordinates, exact24 actual Java arguments/closed7env and retained actual command/pid/start/parent as already enforced by process-options!. The NEW -> ARMED transition binds actual main Thread/process and child local start before the first host probe, then performs raw process/path checks under that same deadline. ARMED grants only fixed raw validation/probes, never five SOURCE boundaries.

After those actual process/path checks succeed, CAS ARMED -> RAW; private raw residence authenticates15rows, then CAS RAW -> CANONICAL before helper require. Later fixed calls transition through CONSTRUCTING, VERIFYING and ENCODING; each transition is main-thread-only and checks time/cumulative caps before work. Any auth/process discrepancy becomes FAILED and ordinary Throwable cleanup; namespace load alone remains NEW and inert.

Owner is created before any raw work, but semantic SOURCE interface calls are admitted only in appropriate active states after raw authentication. This corrects the v1 wording that could confuse inert owner allocation with authority activation. Duplicate main/activation or loading in ordinary process cannot start a constructor or public owner operation.

deadlines: Child D=start+3600000000000; W=D-10000000000. Work/checkpoint/reserve/Git/read/require/parser/constructor/hash/encoding and flush must occur before W. Git uses min(now+30000000000,W). Cleanup-only operations may run before D and cannot charge new semantic work or call public SOURCE boundaries. Existing raw preload no longer retires/recreates executor/deadline; driver closes the one owner once.

normal_cleanup: After complete encoding/write/flush before W, main CAS ENCODING -> CLEANING with work_completed_at_ns<W. CLEANING has failure=nil and cannot execute new work; it retires all streams/JARs/channels/Git processes, then shuts down/joins the one2thread executor and all task records. Only then CAS to QUIESCENT with completed_at_ns<D. Owner becomes RETIRED, and child main may return normally. The parent still requires actual observed exit0/drains/census/joins before Dp; QUIESCENT is never caller success authority.

failure_cleanup: At any work operation time>=W or resource failure, CAS activework -> CANCEL_REQUESTED/FAILED permanently. Preserve original Throwable/deadline cause; transition to failure CLEANING retains failure flag and cannot reach QUIESCENT/normal return. Cleanup checks D, not W. D expiry/unobserved retirement leaves failure for parent OS termination; no in-JVM hard-abort mechanism is introduced.

race_precedence: All successful phase transitions read same immutable expected state and current System.nanoTime before CAS, then validate timestamp immediately after CAS; crossing W/D fails irreversibly even if CAS won. No complete record written at/after its bound is accepted. Once cancellation/failure/retirement wins, subsequent work/complete CAS fails. Parent ABORTING is independent final failure authority; a late child state/output never overrides it.

## Parent hard containment and failure handoff

Delete the proposed child watchdog, Runtime.halt(70), HALTED state and host unsafe-island. Hard termination is a fixed parent process-control operation already admitted by the original process architecture, implemented by a private watcher of each retained actual child Process. Exit70 remains a failed nonzero child status but is not reserved/newly emitted. There is no new child abort API.

The parent captures parent monotonic Dp = before Java ProcessBuilder.start +3600000000000 for each child, starts exactly one retained watcher before dependent output waiting/postchecks, and rejects start/registration/watcher-start failure. This outer clock includes child JVM startup and is at least as strict as the child local hour created before raw work. It is a supervisory bound, never a reset/refill of the child budget.

The watcher only observes the fixed retained child Process and its process tree, plus atomic terminal state. It cannot parse source/products, call constructors, accept callbacks/commands, alter caps or authorize a successor. It polls at most10ms bounded by remaining Dp and retains exact ProcessHandle pid/start identity for every observed descendant before any termination. At most16 live tracked processes including launcher/child/Git are allowed; excess or missing identity/census fails.

At Dp, an uncommitted zero-exit/complete-drain/descendant-retirement state atomically becomes ABORTING. Any worker completion CAS after ABORTING fails permanently; it cannot produce a successful first child, second launch or final result marker. A late zero exit or late receipt is failure. The watcher first stops success admission by nonblocking CAS/cancellation flags, then immediately signals the retained child and already retained descendants. It performs destroy/wait250ms/force/wait1000ms before any potentially blocking close or join. Bounded census of further descendants, repeated retained-handle signals and observed retirement remain inside the unchanged outer cleanup10000ms; a blocked census/close/join cannot prevent the independent external guard from killing the invocation.

The watcher must not block on a pipe close, reader join or product code before issuing termination. Retained process termination uses fixed Process/ProcessHandle methods only; potentially blocking close/join occurs as bounded owned cleanup work after stop signals. Parent main and external supervisor independently retain direct process/group handles, so a stuck watcher cannot prevent the external exact method deadline/cleanup guard from killing the whole invocation. No interruption/future-cancel is accepted as proof of termination.

A child blocked in Files/InputStream/FileChannel/JarFile/LispReader/require/native/CPU work is ended by observed OS process termination. OS exit releases its in-process file/channel/JAR resources; this does not invent successful per-handle close, preserved Throwable or semantic observations. Actual exit/signal and missing/truncated output are failed/unobserved in existing diagnostics. Parent retains and separately terminates/joins observed Git descendants; a missing census or any unobserved process/pipe/task/lease retirement blocks a further run.

Normal parent completion CAS is allowed only after observed child exit0, complete bounded stdout/stderr drains and retirement of every retained descendant/reader task strictly before Dp. It carries completed_at_ns from the same parent clock; Dp watcher may skip abort only for such an immutable COMPLETE record with completed_at_ns < Dp. Then main joins watcher before postchecks, second launch or parent success. If watcher join fails/late or an external deadline check fails, normal COMPLETE is downgraded to method failure; no accepted source result is published.

The external actual SOURCE method must freeze/independently review the outer supervisor before execution: exact parent/child Java, command/group and C/T/source/runtime provenance, parent deadline7260000ms and distinct supervisor deadline7380000ms, per-child watcher contract above, concurrent bounded drains, max16 census, heavy/broker leases and cleanup10000ms. A missing/late parent/child/watcher/census or supervisor abort cannot allow a success marker or automatic retry. This architecture contract defines the handoff; actual bytes and observed retirement are still a mandatory method gate.

The exact failure classification and checked-host safety record are:

```json
{
  "failure_classification": {
    "nonzero_child": "failure including70; preserve actual exit/signal and bounded partial diagnostics",
    "zero_late_exit": "failure",
    "missing_truncated_response": "failure/unobserved, never a completed source product",
    "surviving_or_unobserved_descendant_task_pipe_lease": "incomplete failure and no successor",
    "ordinary_Throwable": "primary, cleanup errors suppressed, interruption restored on every non-kill path",
    "lost_Throwable_on_OS_abort": "unobserved failure; do not fabricate primary exception or complete cleanup"
  },
  "safety_record": {
    "mode": ":runtime-checked trusted-host process boundary",
    "owner_chat": "01a13b8d-509d-7771-b618-06a24933391b",
    "host_effects": [
      ":process",
      ":filesystem",
      ":timer",
      ":concurrency"
    ],
    "capability": "Existing exact SOURCE launcher authority over only its retained actual invocation process tree and broker leases; no shell/arbitrary command or foreign process id authority.",
    "wrapper": "Private fixed child watcher/cleanup in allocated parent; public run-source-product-evidence! arity5 and actual-process checks unchanged.",
    "source_span": "Existing parent source, named private watcher/retirement definitions; actual candidate line spans and hash must be bound in implementation review before execution.",
    "diagnostics": [
      "SH07-B51-SOURCE-PROCESS-DEADLINE",
      "SH07-B51-SOURCE-PROCESS-CLEANUP",
      "R6-TASK",
      "R6-CANCEL"
    ],
    "negative_fixtures": [
      "blocked child I/O/CPU timeout",
      "watcher start/late/join failure",
      "foreign/reused process handle",
      "missing or late exit0",
      "surviving descendant",
      "pipe failure/overflow",
      "interrupted cleanup and suppressed Throwable fidelity"
    ],
    "audit": "Both fresh independent exact design and implementation/method reviews; coordinator GO and terminal integration. Runtime.halt and Thread.stop are absent."
  }
}
```

## Global process domain and supervisor boundary

Parent maximum lifetime is 7260000 ms. The external supervisor maximum lifetime
is the distinct existing 7380000 ms; neither is a replacement for each child
3600000 ms maximum or the separate10000 ms cleanup bound. The one maximum of16
tracked live processes covers the whole invocation: external supervisor,
launcher parent, active serial child, Git and any other owned descendant or
helper. It is not16 per role, child or subprocess subtree.

ProcessHandle samples record actual retained identities and observed exits;
they do not prove absence of every historical or detached process between
samples. Static closed launch routes and actual process-group/retained-handle
cleanup are independently required. An unavailable or incomplete census stays
unobserved failure and prevents a further run. No count is fabricated zero.

At timeout, cancellation is a nonblocking state transition. Kill signals to the
retained actual process tree precede potentially blocking close, reader/task
join or diagnostic formatting. Success requires observed complete retirement;
interrupt or Future.cancel alone is insufficient. Final supervisor evidence
must authenticate the full invocation domain and actual deadlines, failure
transport, task/stream/process/lease release and output-last publication.

## R6 task ownership and synchronization

The following closed task/role contract is normative for the trusted host bridge. It creates no new Gravity runtime support or semantic output schema. Private observations must join existing diagnostics/provenance without fabricated fields or successful retirement.

```json
{
  "models": [
    "structured fixed JVM threads",
    "private atomics",
    "blocking bounded host I/O"
  ],
  "scheduler": "Pinned JVM Thread + one fixed2thread ExecutorService; no shared Clojure future/agent pool",
  "tree": [
    {
      "id": ":source-invocation",
      "parent": null,
      "owner": "parent/source child fixed method",
      "children": [
        ":parent-main",
        ":child-0",
        ":child-1 serially"
      ]
    },
    {
      "id": ":child-n",
      "parent": ":source-invocation",
      "owner": "parent retained Process",
      "children": [
        ":child-main",
        ":child-watcher-n in parent",
        ":stdout-reader-n in parent",
        ":stderr-reader-n in parent",
        ":executor-worker-0",
        ":executor-worker-1",
        ":git-k direct processes and two drain tasks"
      ]
    },
    {
      "id": ":git-k-drain-out/:git-k-drain-err",
      "parent": ":git-k",
      "owner": "child owner registry",
      "result": "bounded raw byte array or original Throwable",
      "captures": "assigned retained stream, operation deadline, inherited counters; no semantic authority or callback",
      "registration": "Create FutureTask before execute, retain ticket/FutureTask/assigned stream under parent git scope, then executor.execute. At Runnable start bind actual Thread role before first read; completion records Throwable/result before actual task join. Future cancellation alone never marks a task joined."
    }
  ],
  "permissions": {
    "main": [
      "five SOURCE boundary entries",
      "checkpoint/reserve/git/read/register/retire/close within phase"
    ],
    "git-drain": [
      "private assigned-stream read and metadata/bytes accounting against its operation/W deadline only; no public SOURCE entry/git/parser/register arbitrary resource"
    ],
    "executor-worker": [
      "exact registered drain task only"
    ],
    "child-watcher-in-parent": [
      "observe fixed Process/descendants/deadline",
      "CAS abort/complete decision",
      "fixed process termination signals",
      "no semantic code or callbacks"
    ],
    "parent-reader": [
      "assigned transport stream bounded drain only"
    ],
    "cleanup-main": [
      "retire/close/cancel/destroy/join existing registered resources before D; no newwork"
    ]
  },
  "registry": "Reserve typed slot before acquisition; retain actual Process/stream/channel/JarFile/FutureTask/Thread immediately after creation and before dependent read/wait/execute. Failed acquisition consumes accounting; it never fabricates completed record. Private Object ticket identity joins role, parent, physical owner source span, process/thread identity, deadline, class, state and original Throwable. Completed/retired remains retained until final audit; no ticket reuse/refund.",
  "failure_mapping": "R6-TASK/R6-CANCEL with exact task id/source owner/host runtime/effect/capability and existing SOURCE diagnostic cause. Preserve original failure and suppressed cleanup; interruption retained. Unobserved close/task/process/lease is failure, not released=true. No record is serialized into semantic products or new wire keys.",
  "replay": "No durable task replay, fallback or restart. Every attempt consumes one exact coordinator grant."
}
```

## Compatibility, security and residual trust

This is a SOURCE-only resource-control change. Effects and capability authority remain separate from semantic construction authority. The owner denies work; it cannot authorize C/T/Qs, choose a parser or override source proofs. Actual process and complete raw/lexical physical input validation remain independent. No secret/network/package authority, namespace mutation, private Var bypass, accepted-proof cache, arbitrary callback or default host fallback is introduced.

Seed Clojure/JVM/Git and host scheduling/process APIs remain explicit trust boundaries. The mechanism provides checked bounds and observed termination under its pinned method, not a formal realtime or hostile-host proof. Parent OS termination does not preserve an in-process Throwable or prove each close; lost evidence remains unobserved failure. Source bytes and generated host classes are invalidated and refrozen after implementation. Existing I9 literal160-owned-C, PRE204/209/196 and all semantic13/13/40/1 records are unchanged.

## Conformance, fixtures and acceptance

Positive/negative fixtures must prove nominal owner sealing and wrong/nil/foreign/copied/stale/retired/process/thread rejection before work; once-only activation; fixed command/path/cap grammar; complete cold raw/C/JAR/physical/root/failed routes; before-work counters; all task registrations and retirement; 30second Git deadline within one child clock; W/D and CAS boundary races; blocked child I/O/parser/CPU containment; parent watcher failure/late join; bounded pipes, descendants and lease failure; original Throwable/suppressed/interruption fidelity; exact load order and250line fit; and complete ordinary parser/output/negative diagnostic preservation. No arbitrary catch-any-error or fabricated receipt is accepted.

Every new JVM/parser/SOURCE/native/heavy/synthetic successor has a fresh finite exact code/C-T/runtime/argv/env/CWD/input/output-absence/resources/deadline/cleanup/lock freeze. Both independent pre-run acceptances and concrete root GO precede one launch. The failed collector synthetic remains spent; any corrected successor is a distinct method with unique output paths. Two actual SOURCE children remain serial and independently construct products. Full48 controls, native source products and all original evidence obligations remain mandatory.

Architecture publication requires scoped metadata/governance/strict authority/ASCII/docs/roadmap checks, independent exact candidate reviews and coordinator integration/preflight/terminal/postflight. Production expansion requires explicit existing draft adoption of this accepted decision and all changed identities. No finite metadata result substitutes for SOURCE/native evidence.

## Outputs, provenance and nonclaims

Outputs are this exact normative amendment, its unique lifecycle shard, minimal manifest and append-only parent delegation, bounded metadata evidence and fresh independent exact candidate reviews. Later outputs are actual implementation, positive/negative fixtures, complete identities, independent reviews and authentic runtime/evidence artifacts. No future artifact is predicted.

The preparation lineage includes resource proposal V2 (47913 bytes, SHA-256 `7839e53f8d81e82433eb8b7d8f2d1e3a1eb2dbe609f74192cfce5d35a274c892`) and corrected closure V2 (34865 bytes, SHA-256 `847886df127c8e77e94bede677013419cc16bb857405d4c81754019e45b7c439`). Earlier proposal/HOLD/failed-run artifacts stay immutable. Those artifacts and draft source fba34a1 are historical design inputs, not imported accepted source/native evidence.

No implementation acceptance, authentic native product, performance diagnostic completion, GateH/L2/F/fullI9/fullSH07/H6/M3/public-root/runtime/certificate/release/self-hosting/seed-retirement credit follows. Original dirty project, held source candidates, parked H6, pinned performance runtime and all immutable history remain preserved.
