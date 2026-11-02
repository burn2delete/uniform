# First OWN positive request proposal

This is preparation, not launch or adoption. OWN-02's first exact selection is
`gravity.self-hosting.authenticated-artifact-pipeline-test/real-owner-gravity-prefix-passes-fresh-replay`.
The immutable request is
`/private/tmp/own-first-request-20261029-e2debad9-15b6-4c43-ac5e-c959671e42e6/first-request.edn`,
SHA-256 `3de1a75eed6d33bbe14fb98bb7a5a2f09ce3752a74352c5d5794982688651757`.
Its operation ID is `own-first-positive-4d3ddf23-0f7c-4ee6-bd3d-a5d5e4d50cab`.
There is no attempt claim, lease, process, result or genuine owner receipt.

## Actual source and runtime bindings

No registered worktree matched child `05e9f8332c6faebb3fd0ada762573d643349ad96`.
Managed creation `ba339a50-ee01-49ce-a807-bacd16357c71` completed once at
`/Users/matt/.codex/worktrees/own-first-child-05e9/uniform`, tree
`3d115ebedfcd23078d3981a43bb1453c10c30095`. This is the clean child CWD, used
read-only for preparation. It is separate from the controller/adapter checkout.
No existing ref or original checkout was reset or relabeled.

The request freezes SHA-256 hashes for all 10,064 tracked child inputs, the
3,127 actual parent loader files/JARs and 303 JDK files, configuration files,
and every inherited environment value (hashes only). The ordered child
classpath is its absolute `bootstrap/clojure/test`, then `bootstrap/clojure/src`,
then the actual Clojure 1.12.5, core.specs.alpha 0.4.74 and spec.alpha 0.5.238
JARs under `/Users/matt/.m2/repository/org/clojure/`. The parent prepends only
the controller/adapter checkout's absolute test root. Full ordered paths and
per-file hashes are in the request. New/missing loader resources, file changes,
source commit/tree/cleanliness, CWD, runtime home/version, classpath, command,
request or environment drift refuse before real port resolution or launch.
The manifest hash itself must be authenticated by the later accepting owner;
self-consistent saved labels do not confer authority.

The actual JVM is
`/opt/homebrew/Cellar/openjdk/26.0.1/libexec/openjdk.jdk/Contents/Home/bin/java`,
runtime version `26.0.1`. Child argv is this absolute executable, `-Xmx2g`,
`-cp`, the frozen child classpath, `clojure.main -m gravity.development-test-runner`,
then exactly `--namespace gravity.self-hosting.authenticated-artifact-pipeline-test
--exact real-owner-gravity-prefix-passes-fresh-replay --fail-fast`.
This direct-Java proposal avoids a shell/CLI launcher child. The unchanged
controller's logical argv remains the reviewed `clojure -J-Xmx2g -M:dev-test`
selection; the OWN start adapter verifies it and the CWD before translating to
the pinned direct-Java argv. Source/method review and parent acceptance must
assess that concrete translation, not infer equivalence from an earlier run.

The selected child remains historically bound to `05e9f8332c6faebb3fd0ada762573d643349ad96`
and its original broker/runner source bytes. The current parent controller is
`f43e77992fe86b6dfb55df3516b27cfcdea7fa40`, file SHA-256
`0c40752d7d63c50008df621f38fa6a966be7eaa0ab17dad369aff0a5fe3451d8`.
The current parent broker and runner source joins are respectively
`6c0c1f3e173453e2b1d455ddcb5ed61b65e0c7e2` /
`fead17573a7d5fa7d1a9f30582e4e5e583b7480bd2a5d4cedb4b893ff9e96dfd` and
`4df59ab107d5865e3afbf9173041cc9401227542` /
`2c56243ced930f7cd253b9b10f2444267010f168fdd3e8e4b99b87e3c2f3e16c`.
The runner source is checked against the controller's current public
`start-process`, `process-result` and `finalize-owned-process!` signatures;
the broker source is checked against its current acquire, validate and release
arities before Vars are resolved. The historical child snapshot and current
parent source snapshot are kept as separate fields, not treated as equivalent.
New functions live in `gravity.artifact-pipeline.owner-request`; loading it
performs no IO, normal namespace resolution or launch. The immutable request
pins its actual file hash and `assemble-ports` closure identity/port slots;
callers do not provide arbitrary operational functions or identity labels.

## Proposed order, functions and coverage

| Stage | Concrete binding | Authority/status |
| --- | --- | --- |
| Admission | OWN adapter refuses with `:own-profile-unaccepted` | Current parent must accept this exact OWN profile; RV admission is not transferred |
| C | `/Users/matt/code/burn2delete/uniform/.git/gravity-development-loop-v1/broker`, derived from the child Git-common directory; one `:exclusive` lease | Proposed C then L order; no acquisition or availability observation |
| Broker ports | Current `acquire!` with `[{:coordination-root C :timeout-ms remaining} :exclusive]`, `validate-live-lease!` with `[raw-lease C :exclusive]`, `release!` with `[raw-lease]` | Current parent broker source SHA-256 `fead17573a7d5fa7d1a9f30582e4e5e583b7480bd2a5d4cedb4b893ff9e96dfd` and three arities are source-bound; historical child hash remains `0126b1d96f11d7e6908ea693f7f29a6d3d139cf30f22471ff6525229ca5ece20` |
| L | `/private/tmp/gravity-sh07-heavy.lock`; `canonical-acquire!`, `canonical-validate!`, `canonical-release!` | Existing FileChannel CREATE/WRITE/NOFOLLOW_LINKS and immediate tryLock method, with original channel/lock retained and released once |
| Child/capture | Historical child selection plus current parent `start-process` and retained-token `process-result`; exact argument adapters | Historical child runner SHA-256 is `72d8ea13e3cfebefc55f4e6350fcabc5bab58050f3f2a85b7058bf2ef097e0af`; current parent runner SHA-256 is `2c56243ced930f7cd253b9b10f2444267010f168fdd3e8e4b99b87e3c2f3e16c` |
| Finalizer | Capture the current retained token and raw handles before wait/release; consume current finalizer at most once and report unknown | The current runner can finalize retained handles, but cannot prove global descendant completeness; no operational pass is fabricated |
| Bookends | `verify!` before binding/start and after terminal observation | Full source/loader/runtime/environment identities; no holder or all-process scan |
| Outputs | Unique request directory above; attempt/stdout/stderr/final paths fixed in request | CREATE_NEW claim before guards; capture/final writes CREATE_NEW after release; one attempt, no overwrite/retry |

The canonical method is reused from the reviewed RV parent source at
`/private/tmp/rv1-resolved-operand-attempt-20261029/parent.clj`, SHA-256
`2e2492646b93e388ca105b94a80e45868796bb0bd630b0606c7635247a4943fd`.
Only that file-lock method is reused. Its nested broker sequence, workload
admission, leases and results are not copied or accepted. The adapter closes
the channel if immediate acquisition fails; release closes the original channel
even if lock release throws. Broker release keeps the raw semantic receipt.

Bounded participants are the OWN outer controller, direct development runner,
and direct owner chain. Only the outer controller acquires C and L; there is no
child broker reacquisition. At this same C, an exclusive lease excludes all
normal, memory-heavy and exclusive standard broker callers, including the
Git-common-root development-loop caller. Callers configured at other roots are
outside C's exclusion. The same L serializes cooperative legacy controllers
using this exact canonical file, including the reviewed RV method; no claim is
made about a currently live controller. No UEP fresh/warm launcher/session,
SH07 launcher or pass profiler is invoked or scheduled in this selected method.
There is no all-OS session/root registry. Native current-API work has no
controller/pending handle per current-parent readback; historical MIR unknowns,
R4/22232 and Reader/DSE restrictions remain unchanged, without clearance inference.

## Proposed bounds and exact remaining interfaces

The existing OWN selector report proposes a 2 GiB child and 1,200-second host
watchdog. They remain proposals. The parent heap proposal is 256 MiB, matching
the compiler-free controller/adapter validation JVM, not a measured genuine
owner requirement. Runner source supplies 1 MiB per-stream byte/character
capture defaults, 5,000 ms capture wait per stream, 100 ms post-close wait per
stream, 1,500 ms cleanup interval and 100 ms root post-cleanup wait. The proposed
child maximum is therefore 1,188,200 ms, reserving those fixed 11,800 ms within
1,200,000 ms. Actual child wait uses the lesser of that maximum and remaining
whole time minus the same reserve; insufficient time refuses before supervision.
Broker admission receives remaining whole time. None of these numbers accepts
capacity or imposes compiler input accounting. RV's 120-second child bound is
not reused.

These are accounted deadlines. Native startup, filesystem operations, JVM
initialization, ProcessHandle enumeration/reaping and callbacks/publication may
block before control returns. Reaping is per observed handle; fork/reparent races
and unobserved escapes are not proved contained. Normal timeout/interruption
cleanup is best effort and remains nonpassing. No absolute wall or cross-session
containment guarantee is made.

Three operational claims remain unavailable, with concrete owners rather than
empty configuration fields: current-parent acceptance of exact order/profile/
coverage and bounds (`assemble-ports` admission currently refuses); global
retained-descendant completeness after natural exit (the current token and
finalizer report only their observed handle scope, so the adapter remains
unknown); and later platform action supervision for the proposed host watchdog
(not an outer controller guarantee). Proposal preparation cannot be promoted
into genuine acceptance.

## Validation and history

Eleven new source-only/fake-port tests and 94 assertions pass; together with
the current parent controller's 25 tests and 556 assertions, the two focused
namespaces pass 36 tests and 650 assertions. The checks verify current source
hashes and runner/broker arities, historical/current source separation,
broker arities/raw handles, separate canonical dispatch, exact argv/CWD before
launch, retained-token and raw-handle capture before supervision, capture-key
and time mapping, interruption identity, fatal finalizer identity, output write
ordering/no retry, refusal without parent acceptance, exactly-once unknown
finalization, and request/command drift before resolution. The namespace
fixture intentionally preloads `gravity.bootstrap` and compares BEFORE/AFTER
sets, rejecting only new loads; it does not launch a child or acquire a broker
lease. These checks do not prove a genuine owner operation, admission,
descendant completeness, or operational profile adoption. Independent affected
source/method review remains a separate obligation.

During preparation, one mistaken command entered the development runner CLI:
`clojure -J-Xmx256m -M:dev-test -e ''`. It exited 2 with
`-e requires a non-empty value` during argument parsing, before namespace/test
selection. This was not a source-only success and is not a genuine owner result.
It was reported immediately and not repeated. No normal process-result,
broker acquisition, owner constructor or exact selector was executed.

Public-facade current-Reader/invocation propagation, cache identity and replay
arity remain original OWN dependencies. The direct-owner selectors and this
request adapter do not implement them. The same OWN writer advances that source
interface after this finite preparation slice; ACC10 producer and ACC11 consumer
responsibilities are preserved. No OWN closure, v3, release, self-hosting,
integration eligibility or operational profile adoption follows.
