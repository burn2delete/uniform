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

The controller source remains exactly `5f7eda401162586cb3ace0a49f11e14e79985ee1`,
file SHA-256 `44e1cf0c32eea3b7151cfc4c278ef1fe8ea5634b2fb7b8166f49b07163c3d24a`.
New functions live in `gravity.artifact-pipeline.owner-request`; loading it
performs no IO, normal namespace resolution or launch. The immutable request
pins its actual file hash and `assemble-ports` closure identity/port slots;
callers do not provide arbitrary operational functions or identity labels.

## Proposed order, functions and coverage

| Stage | Concrete binding | Authority/status |
| --- | --- | --- |
| Admission | OWN adapter refuses with `:own-profile-unaccepted` | Current parent must accept this exact OWN profile; RV admission is not transferred |
| C | `/Users/matt/code/burn2delete/uniform/.git/gravity-development-loop-v1/broker`, derived from the child Git-common directory; one `:exclusive` lease | Proposed C then L order; no acquisition or availability observation |
| Broker ports | Existing `acquire!` with `[{:coordination-root C :timeout-ms remaining} :exclusive]`, `validate-live-lease!` with `[raw-lease C :exclusive]`, `release!` with `[raw-lease]` | Broker source SHA-256 `0126b1d96f11d7e6908ea693f7f29a6d3d139cf30f22471ff6525229ca5ece20` matches child and controller checkout |
| L | `/private/tmp/gravity-sh07-heavy.lock`; `canonical-acquire!`, `canonical-validate!`, `canonical-release!` | Existing FileChannel CREATE/WRITE/NOFOLLOW_LINKS and immediate tryLock method, with original channel/lock retained and released once |
| Child/capture | Existing normal `start-process` and private `process-result`; exact argument adapters | Runner source SHA-256 `72d8ea13e3cfebefc55f4e6350fcabc5bab58050f3f2a85b7058bf2ef097e0af` matches both checkouts |
| Finalizer | `owned-finalizer` consumes the original runner cleanup without retry and reports unknown | Normal runner has no retained-owned-descendants-on-natural-exit interface; root exit cannot fill it |
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

Three interfaces remain unavailable, with concrete owners rather than empty
configuration fields: current-parent acceptance of exact order/profile/coverage
and bounds (`assemble-ports` admission currently refuses); retained owned
terminal descendants after natural exit (absent from the pinned normal runner,
so the adapter finalizer cannot pass); and later platform action supervision for
the proposed host watchdog (not an outer controller guarantee). A later source
repair and exact action assessment must address them before any launch. Proposal
preparation cannot be promoted into genuine acceptance.

## Validation and history

Eight new fake-port tests/76 assertions pass; together with the unchanged
controller, 27 tests/476 assertions pass. The original controller 19/400 remains
intact. Language boundary passes 6/11. Controls verify broker arities/raw handles,
separate canonical dispatch, exact argv/CWD before launch, capture-key and time
mapping, output write ordering/no retry, refusal without parent acceptance,
exactly-once unknown finalization, and request/command drift before resolution.
All checked tests keep Bootstrap, normal broker/runner and development-runner
namespaces absent. The preparation freeze also read back all three normal
owner namespaces absent. Independent affected source/method review is still a
separate obligation; test counts do not prove method or operational acceptance.

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
