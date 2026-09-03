# SH-07 B51 Once-Per-Carrier Validation Provisional Evidence V1

Status: provenance record only; provisional observations; no implementation or
performance authority

Date: 2026-09-02

## Purpose and classification

This artifact records the observations that motivated
`sh07-b51-once-per-carrier-validation-architecture-v1`. It is deliberately
separate from the architecture decision. The two long-running probes were made
in a dirty implementation worktree before the observed final source bytes were
committed, and no immutable stdout transcript was retained. They are therefore
diagnostic observations, not clean reproducible receipts and not implementation
acceptance evidence.

The implementation branch, its commits, and its tests grant no authority to
this architecture. The only architecture dependency is integrated Attempt 32.
The implementation source is used here to localize a fixed-fuel failure and to
state what a later implementation candidate must reproduce cleanly.

## Observed implementation lineage

The observations were made in the separately governed implementation worktree
whose final relevant commits are:

| Role | Commit | Tree | Parent | Subject |
| --- | --- | --- | --- | --- |
| one-time admission authority | `5bb3e6ea48507cfc528d824dd41087f3f21aa3ee` | `77dd63955eb215aaf1f0d2d4c39c49cae0470b9a` | `c198708b648002f6b57086dbd171d081d6898f02` | `sh07: bind runtime authority once at admission` |
| indexed Gravity joins | `45556a3f447a1262cd5f01afe5630571ee759d18` | `e3f917afc6c2d81e805bfc86e3a185f078ff73e2` | `5bb3e6ea48507cfc528d824dd41087f3f21aa3ee` | `sh07: index governed edge joins within fixed fuel` |

At evidence authoring time that implementation worktree was clean at
`45556a3f447a1262cd5f01afe5630571ee759d18`. Git establishes the committed
objects below. The probe observer reported that the dirty-worktree source bytes
used by both probes were committed unchanged in the second commit. Because the
pre-commit worktree and raw probe stdout were not retained, that historical
byte-equality assertion cannot be independently reconstructed from Git and
remains provisional.

## Exact committed source-byte identities

The following values were recomputed from Git objects, not from the later
working tree. Byte count is the blob content length; SHA-256 is over those exact
bytes.

| Commit | Repository path | Bytes | SHA-256 | Git blob |
| --- | --- | ---: | --- | --- |
| `5bb3e6ea48507cfc528d824dd41087f3f21aa3ee` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00944-p15-s23-sh07-runtime-internal-builtins.clj` | 12,157 | `d238a85f04b31d654bf2be8e43d41cf1e2d8c1b97e06a920efd41fa191a8ab9d` | `8719cad76818b3e6815a4a3470ec4ad62462990d` |
| `5bb3e6ea48507cfc528d824dd41087f3f21aa3ee` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj` | 73,732 | `a6a475dff060e7bf8c6b8b623e54db569e907ceb5dca5fcb64d348a9267e3acf` | `454fba609faa91fb4e2bad714c352e2d2cf2c3af` |
| `5bb3e6ea48507cfc528d824dd41087f3f21aa3ee` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj` | 61,716 | `e327145e5d40e2a20a5bf365d2f3264878a591023d8a0c7a82ff1599cd9f77d0` | `0b475a122c25b7978cba1583e0c29070cde174bf` |
| `5bb3e6ea48507cfc528d824dd41087f3f21aa3ee` | `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj` | 8,674 | `1280b8df984b80974f4cbd13e276a29b809431eb3f9acf3fe2df731d839afbe4` | `6b9e598b1f9566ffab58cdb58242d7565cbe126b` |
| `45556a3f447a1262cd5f01afe5630571ee759d18` | `bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` | 450,047 | `94388c90571225c55a1e846044564bd4a84471ab62a5b806db9ee5f2fabbd647` | `dd7a580d3bb3a7c1a401882d758652a9c481e4c2` |
| `45556a3f447a1262cd5f01afe5630571ee759d18` | `bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` | 332,193 | `830a6b75112b1b26affe6e106321c722dc3ade833eb3e1068ef3a05f2dacb7c4` | `70928d55c6e7c71213c15d59cbef60932e6ac497` |
| `45556a3f447a1262cd5f01afe5630571ee759d18` | `bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_indexed_join_test.clj` | 5,594 | `0cac7b359e0b95c7a908da69b8f22249f40a07f00844aabe95b513a2b64eee05` | `a705e6036c14fd9e85360a9ddf434c68738d997c` |

The three host source blobs from `5bb3e6ea` are inherited byte-identically by
`45556a3f`. The second commit changes only the two Gravity sources and the new
indexed-join test.

## Clean focused test observations

The following commands were rerun on the clean implementation worktree at
`45556a3f447a1262cd5f01afe5630571ee759d18`:

```text
clojure -M:test --namespace gravity.self-hosting.sh07-b51-attempt32-runtime-admission-test
PASS: 3 tests, 120 assertions, 0 failures, 0 errors

clojure -M:test --namespace gravity.self-hosting.sh07-b51-attempt32-indexed-join-test
PASS: 2 tests, 28 assertions, 0 failures, 0 errors
```

The first suite establishes its synthetic one-load order, exact private token
and runtime-context identity checks, repeated admitted-product lookup without
authority reconstruction, and denial of the public compatibility path. It does
not establish authentic Root-1 completion. The second compares indexed Root-1
and Root-6 joins with the host reference across authentic and collision cases
and confirms that the runtime maximum remains 8,388,608 steps. The retained
source and host checks still require exactly 1,480 link edges. These focused
passes do not establish that either authentic public root completes within
fuel.

## Provisional full-fuel Root-1 observation

The observer required the bootstrap and Attempt-30/32 integration support,
entered `with-provisional-authority`, constructed:

```clojure
(linked-raw-carrier "accepted" "nested-empty-wildcard-let")
```

and invoked the equivalent of:

```clojure
(execute-root-with-trace raw 1 [raw])
```

Runtime-machine eval and continuation seams were wrapped only to count and
localize evaluation. The governed maximum remained exactly 8,388,608. The
request passed authentic admission, entered Root 1, and then threw structured
`ExceptionInfo` with reason `:runtime-machine-bound`. The failure record named
`producer_c.gravity` as its current source, the machine reached exactly
8,388,608 steps by the runtime guard, and no result or output was committed.
Elapsed wall time was approximately seven minutes.

This was a dirty-worktree run. No immutable stdout, environment, JVM, wall
clock, RSS, or complete counter receipt was retained. It proves neither a
stable timing nor the exact distribution of work among phases. It is only
evidence that authentic admission was no longer the immediate blocker and that
the unchanged fixed machine bound was exhausted before a Root-1 result.

## Provisional 500,000-step localization observation

A later diagnostic temporarily redefined the machine maximum to 500,000 on the
same reported corrected source bytes and wrapped machine eval/continuation to
count calls and retain the last state. It failed closed. The wrapper observed
500,020 combined eval-plus-continuation calls. The extra 20 calls are wrapper
or accounting overhead and must not be reported as machine steps.

The last retained state was:

```text
source       producer_a.gravity
function     sh07-b51-lowercase-hex?
operation    :local
frame depth  32
phase        certificate/carrier digest-shape validation before indexed route validation
```

The source loops inspected along this path decrease their explicit remaining
collections or indices. No static infinite recursion was found. That is not a
termination proof and does not establish the transition count needed after the
proposed architecture.

This sample is also dirty-worktree, observer-reported, and lacks an immutable
stdout receipt. Its only supported conclusion is localization: substantial
machine fuel was still being spent inside repeated digest, certificate, and
carrier validation before request-specific indexed route work.

## Required clean reproduction

A later implementation review must replace both provisional observations with
one immutable, clean, independently replayable evidence bundle. It must bind
the exact candidate commit, tree, source-byte hashes, JVM/toolchain, command,
fixtures, exit status, stdout/stderr digest or retained transcript, and the
following counters:

- host admission executions and each bounded admission phase;
- certificate A and certificate B validations;
- M0, M1, M2, M3, S30, semantic-root, route, owner, and arity joins;
- runtime receipt mint, successful lookup, rejected lookup, and consumption;
- exact machine transitions, maximum frame depth, authenticated external-call
  count, result/output commit state, wall time, and peak RSS; and
- legacy Gravity carrier/digest/certificate validation calls, which must be
  zero at the two authorized receipt call sites and unchanged elsewhere.

The clean bundle must include accepted, pending, malformed, mutation, exact
limit, and first-plus-one cases for both Root 1 and Root 6. Until then, this
artifact makes no performance, completion, acceptance, or implementation
claim.
