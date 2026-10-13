# SH-07 B51 Binding-Replay Implementation Review Erratum V1

Status: append-only post-integration correction; no implementation authority

## Purpose

This report preserves the immutable integrated lifecycle of
`sh07-b51-binding-replay-index-implementation-v1` while recording a later fresh
independent review that invalidates its publication-safety and fail-fast
claims. The implementation bytes remain in Git history. They must not be used
as evidence-publication or fixed-fuel completion authority until a separately
governed successor fixes the findings and receives fresh independent review.

This erratum changes no compiler, runtime, materializer, fixture, pin, or
evidence bytes. It does not rewrite or deny the earlier independent acceptance.
It records the ordering of both review facts and the exact counterexample that
was discovered after integration.

## Exact immutable history

| Item | Exact identity |
| --- | --- |
| Original base | `5d2dc100d29a8a2bbb1eef38eb1ce5f1f7f2dc69` |
| Challenged implementation candidate | `133075931ee3797bf7f928371d2e6044bb8e16d2` |
| Challenged candidate tree | `c0fbde0d6338022a5b13bdb52833bc961b8b922f` |
| Frozen/review-pending lifecycle head reviewed by the later reviewer | `69fa1c49fc728f380b10f562bc45eb7bd6a2a202` |
| Earlier accepted/admitted lineage head | `b52c84c6324a56688aed413b0c08d2accf6de3a6` |
| Integrated lifecycle publication on main | `da8f2071d4ab6f672fed76be0f7a9e827008ef83` |
| Integrated main tree | `093bce697fab34461411c8bb358fbd4fe716024f` |
| Immutable integrated record SHA-256 | `6bd310175fc9cbaeeb8975cc5b68949004b3b006bbd5c961ef89032c6c2a8132` |

The immutable integrated record remains at
`contracts/workstream-records/terminal/sh07-b51-binding-replay-index-implementation-v1-6bd310175fc9cbaeeb8975cc5b68949004b3b006bbd5c961ef89032c6c2a8132.json`.
It records the earlier independent `ACCEPT` by
`/root/sh07_binding_replay_review`. That review happened and is not rewritten.

The later independent reviewer `/root/binding_replay_impl_review` returned:

```text
REJECT exact frozen content candidate
133075931ee3797bf7f928371d2e6044bb8e16d2, tree
c0fbde0d6338022a5b13bdb52833bc961b8b922f; lifecycle HEAD
69fa1c49fc728f380b10f562bc45eb7bd6a2a202.
```

## Reproduced publication counterexample

`publish-files!` first observes that the final target is absent. It creates and
validates a staging directory, then calls `Files/move` with `ATOMIC_MOVE`. On
this host, if another actor creates an empty directory at the target after the
absence check and before the move, the move replaces that directory instead of
throwing. The exception-only `:evidence-output-race` branch is therefore not
entered.

The bounded reproduction instrumented only `reload-validate-output!` and
`validate-snapshot!`; the latter created the empty target in the exact window
before the real `Files/move`. The exact candidate returned:

```clojure
{:result :published, :racing-empty-directory-was-replaced true}
```

The existing race fixture creates a nonempty target containing an `owner`
file. It therefore exercises a different host behavior and does not cover the
empty-directory replacement counterexample. The claim that publication never
overwrites a racing target is false for the integrated implementation.

## Fail-fast ordering contradiction

In `materialize-current`, the exact candidate evaluates these operations in
order:

1. `checker-a/produce-certificate`;
2. `checker-b/produce-certificate`;
3. `validate-certificate-pair!`.

Checker A acceptance and validity are first tested inside step 3. Therefore a
rejected or invalid checker A result does not prevent checker B from executing.
The integrated lifecycle wording claiming fail-fast incremental materializer
validation is not supported by the implementation.

## Related source-identity boundary

Source and evidence paths are checked for regular-file and symlink-free status
and then read through a separate pathname operation. A same-user replacement
between those operations remains a check-then-read TOCTOU boundary. Existing
symlink fixtures prove rejection of links present at the time of the check;
they do not prove stable identity across the later read.

## Preserved positive evidence

The later review also verified that the base, candidate, tree, ancestry,
ownership projection, governance hashes, and fixed limits were unchanged. The
authorized producer-B scan-to-existing-index source cutpoint remains intact.
Governance and diff validation passed. The later review did not run authentic
long-root execution; a bounded materializer-suite attempt was stopped after
approximately 4.5 minutes without output and was not counted as a failure.

These positive facts do not repair the counterexample or restore publication
authority.

## Required successor boundary

Publication repair must follow a separately accepted evidence-publication
architecture successor. A later implementation must also make checker A
validation precede any checker B execution and must explicitly resolve or
narrow the source-identity TOCTOU boundary. Any implementation successor needs
a new exact candidate/tree, bounded positive and negative fixtures for the
empty-target race and checker ordering, and fresh independent acceptance.

This erratum grants no retroactive rejection mutation of the immutable
integrated record, no implementation acceptance, no evidence admission, and no
roadmap credit. It establishes no authentic fixed-fuel Root-1, Root-6, or
Root-8 completion; no eight-root evidence; no aggregate SH-07 completion; no
self-hosting; no release; no performance claim; and no seed retirement.

Overall SH-07 remains at four of eight tracked milestones.

## Conformance and acceptance criteria

This erratum is complete only when an independent reviewer confirms that:

1. every Git and content identity above matches authoritative history;
2. the empty-target race reproduces the recorded `:published` result;
3. checker B precedes validation of checker A in the exact candidate;
4. the check-then-read source boundary is stated without overstating exploit
   coverage;
5. the integrated record is unchanged and preserved by reference;
6. the erratum diff is report-only before lifecycle records are added; and
7. all SH-07, self-hosting, release, performance, and seed-retirement nonclaims
   remain explicit.
